(function (window, document) {
    'use strict';

    var appShell = window.appShell || {};
    var contextPath = appShell.contextPath || '';

    var ADMIN_SECRET = (function () {
        var params = new URLSearchParams(window.location.search);
        var fromQuery = params.get('secret');
        if (fromQuery && fromQuery.trim().length > 0) {
            return fromQuery.trim();
        }
        return 'dev-secret-key-change-me';
    })();

    var els = {
        conversationList: document.getElementById('adminSupportConversations'),
        conversationEmpty: document.getElementById('adminSupportConversationEmpty'),
        conversationTitle: document.getElementById('adminSupportConversationTitle'),
        conversationMeta: document.getElementById('adminSupportConversationMeta'),
        refreshBtn: document.getElementById('adminSupportRefreshBtn'),
        messages: document.getElementById('adminSupportMessages'),
        replyForm: document.getElementById('adminSupportReplyForm'),
        replyInput: document.getElementById('adminSupportReplyInput'),
        formStatus: document.getElementById('adminSupportFormStatus')
    };

    var state = {
        conversations: [],
        selectedId: null,
        messages: [],
        messageIndex: new Map(),
        lastTimestamp: 0,
        pollTimer: null,
        loadingConversations: false,
        loadingMessages: false,
        sending: false
    };

    function selectConversation(id) {
        if (state.selectedId === id) {
            return;
        }
        state.selectedId = id;
        state.messages = [];
        state.messageIndex.clear();
        state.lastTimestamp = 0;
        renderConversations();
        renderConversationHeader();
        renderMessages();
        loadMessages(true);
    }

    function renderConversations() {
        if (!els.conversationList) {
            return;
        }
        if (!Array.isArray(state.conversations) || state.conversations.length === 0) {
            els.conversationList.innerHTML = '';
            if (els.conversationEmpty) {
                els.conversationEmpty.textContent = state.loadingConversations ? 'Đang tải danh sách...' : 'Chưa có hội thoại nào.';
                els.conversationEmpty.classList.remove('hidden');
            }
            return;
        }
        if (els.conversationEmpty) {
            els.conversationEmpty.classList.add('hidden');
        }
        var html = state.conversations.map(function (item) {
            var active = item.id === state.selectedId;
            return '' +
                '<article class="px-5 py-4 transition cursor-pointer ' + (active ? 'bg-amber-50 border-l-4 border-amber-500' : 'hover:bg-gray-50') + '" data-conversation-id="' + item.id + '">' +
                    '<div class="flex items-center justify-between gap-3">' +
                        '<div>' +
                            '<p class="font-semibold text-gray-800">' + escapeHtml(item.username || ('Khách #' + item.userId)) + '</p>' +
                            '<p class="text-xs text-gray-500 mt-0.5">' + formatStatus(item.status) + '</p>' +
                        '</div>' +
                        '<span class="text-xs text-gray-400">' + formatDateTime(item.lastMessageAt || item.updatedAt) + '</span>' +
                    '</div>' +
                    (item.lastMessage ? '<p class="text-sm text-gray-600 mt-2 line-clamp-2">' + escapeHtml(item.lastMessage) + '</p>' : '<p class="text-sm text-gray-500 mt-2 italic">Chưa có tin nhắn.</p>') +
                '</article>';
        }).join('');
        els.conversationList.innerHTML = html;
        els.conversationList.querySelectorAll('[data-conversation-id]').forEach(function (node) {
            node.addEventListener('click', function () {
                var id = Number(node.getAttribute('data-conversation-id'));
                if (!Number.isNaN(id)) {
                    selectConversation(id);
                }
            });
        });
    }

    function renderConversationHeader() {
        if (!els.conversationTitle || !els.conversationMeta) {
            return;
        }
        if (!state.selectedId) {
            els.conversationTitle.textContent = 'Chọn một hội thoại';
            els.conversationMeta.textContent = 'Danh sách tin nhắn sẽ hiển thị tại đây.';
            if (els.replyInput) {
                els.replyInput.disabled = true;
                els.replyInput.value = '';
            }
            if (els.formStatus) {
                els.formStatus.textContent = '';
            }
            return;
        }

        var summary = state.conversations.find(function (item) {
            return item.id === state.selectedId;
        });

        if (!summary) {
            els.conversationTitle.textContent = 'Hội thoại #' + state.selectedId;
            els.conversationMeta.textContent = '';
        } else {
            els.conversationTitle.textContent = 'Hội thoại #' + summary.id + ' · ' + (summary.username || 'Khách #' + summary.userId);
            els.conversationMeta.textContent = 'Trạng thái: ' + formatStatus(summary.status) + ' · Cập nhật lần cuối ' + formatDateTime(summary.updatedAt);
        }
        if (els.replyInput) {
            els.replyInput.disabled = false;
            els.replyInput.focus();
        }
    }

    function renderMessages() {
        if (!els.messages) {
            return;
        }
        if (!state.selectedId) {
            els.messages.innerHTML = '<div class="text-sm text-gray-500">Chưa chọn hội thoại nào.</div>';
            return;
        }
        if (!Array.isArray(state.messages) || state.messages.length === 0) {
            els.messages.innerHTML = '<div class="text-sm text-gray-500">Chưa có tin nhắn trong hội thoại này.</div>';
            return;
        }
        var html = state.messages.map(function (message) {
            var isSupport = message.senderType && message.senderType.toLowerCase() === 'support';
            return '' +
                '<div class="flex ' + (isSupport ? 'justify-start' : 'justify-end') + '">' +
                    '<div class="max-w-[80%] px-3 py-2 rounded-2xl shadow-sm text-sm leading-relaxed ' +
                        (isSupport ? 'bg-white text-gray-700 border border-amber-100' : 'bg-amber-600 text-white') + '">' +
                        '<div>' + escapeHtml(message.content || '') + '</div>' +
                        '<div class="mt-1 text-[11px] uppercase tracking-wide ' + (isSupport ? 'text-gray-400' : 'text-amber-100/80') + '">' +
                            formatDateTime(message.createdAt) +
                        '</div>' +
                    '</div>' +
                '</div>';
        }).join('');
        els.messages.innerHTML = html;
        els.messages.scrollTop = els.messages.scrollHeight;
    }

    function loadConversations() {
        if (state.loadingConversations) {
            return;
        }
        state.loadingConversations = true;
        if (els.conversationEmpty) {
            els.conversationEmpty.textContent = 'Đang tải danh sách...';
        }
        fetch(buildUrl('/api/admin/support-chat', { action: 'conversations' }))
            .then(handleJson)
            .then(function (data) {
                if (!data.ok || !Array.isArray(data.conversations)) {
                    throw new Error(data.message || 'Không thể tải danh sách hội thoại');
                }
                state.conversations = data.conversations;
                renderConversations();
                if (state.selectedId) {
                    var exists = state.conversations.some(function (item) { return item.id === state.selectedId; });
                    if (!exists) {
                        state.selectedId = null;
                        state.messages = [];
                        renderConversationHeader();
                        renderMessages();
                    }
                }
            })
            .catch(function (error) {
                console.error('admin support conversations error', error);
                if (els.conversationEmpty) {
                    els.conversationEmpty.textContent = error.message || 'Không thể tải danh sách hội thoại';
                }
            })
            .finally(function () {
                state.loadingConversations = false;
            });
    }

    function loadMessages(reset) {
        if (!state.selectedId || state.loadingMessages) {
            return;
        }
        state.loadingMessages = true;
        var params = { action: 'messages', conversationId: state.selectedId, limit: 200 };
        if (!reset && state.lastTimestamp > 0) {
            params.since = state.lastTimestamp;
        }
        fetch(buildUrl('/api/admin/support-chat', params))
            .then(handleJson)
            .then(function (data) {
                if (!data.ok || !Array.isArray(data.messages)) {
                    throw new Error(data.message || 'Không thể tải tin nhắn');
                }
                if (data.conversation) {
                    updateConversationSummary(data.conversation);
                }
                mergeMessages(data.messages, reset);
                renderMessages();
            })
            .catch(function (error) {
                console.error('admin support load messages error', error);
                if (els.formStatus) {
                    els.formStatus.textContent = error.message || 'Không thể tải tin nhắn.';
                }
            })
            .finally(function () {
                state.loadingMessages = false;
            });
    }

    function mergeMessages(list, reset) {
        if (reset) {
            state.messages = [];
            state.messageIndex.clear();
            state.lastTimestamp = 0;
        }
        list.forEach(function (message) {
            if (!message || typeof message.id !== 'number') {
                return;
            }
            if (state.messageIndex.has(message.id)) {
                return;
            }
            state.messageIndex.set(message.id, true);
            state.messages.push(message);
            var ts = parseTimestamp(message.createdAt);
            if (ts > state.lastTimestamp) {
                state.lastTimestamp = ts;
            }
        });
        state.messages.sort(function (a, b) {
            return parseTimestamp(a.createdAt) - parseTimestamp(b.createdAt);
        });
    }

    function updateConversationSummary(summary) {
        if (!summary) {
            return;
        }
        var index = state.conversations.findIndex(function (item) {
            return item.id === summary.id;
        });
        if (index >= 0) {
            state.conversations[index] = summary;
        } else {
            state.conversations.unshift(summary);
        }
        renderConversations();
        if (state.selectedId === summary.id) {
            renderConversationHeader();
        }
    }

    function handleReplySubmit(event) {
        event.preventDefault();
        if (!state.selectedId || state.sending) {
            return;
        }
        var value = els.replyInput ? els.replyInput.value.trim() : '';
        if (!value) {
            setFormStatus('Vui lòng nhập nội dung phản hồi.');
            return;
        }
        state.sending = true;
        setFormStatus('Đang gửi phản hồi...');
        fetch(contextPath + '/api/admin/support-chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Admin-Secret': ADMIN_SECRET
            },
            body: JSON.stringify({
                action: 'reply',
                conversationId: state.selectedId,
                content: value
            })
        })
            .then(handleJson)
            .then(function (data) {
                if (!data.ok || !data.message) {
                    throw new Error(data.message || 'Không thể gửi phản hồi.');
                }
                if (els.replyInput) {
                    els.replyInput.value = '';
                }
                setFormStatus('Đã gửi phản hồi.');
                mergeMessages([data.message], false);
                renderMessages();
                updateConversationSummary({
                    id: state.selectedId,
                    userId: data.message.conversationId,
                    username: getCurrentConversationUsername(),
                    status: 'open',
                    createdAt: data.message.createdAt,
                    updatedAt: data.message.createdAt,
                    lastMessage: data.message.content,
                    lastMessageAt: data.message.createdAt
                });
            })
            .catch(function (error) {
                console.error('admin support reply error', error);
                setFormStatus(error.message || 'Không thể gửi phản hồi.');
            })
            .finally(function () {
                state.sending = false;
            });
    }

    function getCurrentConversationUsername() {
        var summary = state.conversations.find(function (item) {
            return item.id === state.selectedId;
        });
        return summary && summary.username ? summary.username : '';
    }

    function stopPolling() {
        if (state.pollTimer) {
            window.clearInterval(state.pollTimer);
            state.pollTimer = null;
        }
    }

    function startPolling() {
        stopPolling();
        state.pollTimer = window.setInterval(function () {
            loadConversations();
            loadMessages(false);
        }, 5000);
    }

    function handleJson(response) {
        return response.json().catch(function () {
            return {};
        });
    }

    function buildUrl(base, params) {
        var url = new URL(contextPath + base, window.location.origin);
        if (params) {
            Object.keys(params).forEach(function (key) {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    url.searchParams.set(key, params[key]);
                }
            });
        }
        url.searchParams.set('secret', ADMIN_SECRET);
        return url.toString();
    }

    function escapeHtml(text) {
        if (!text) {
            return '';
        }
        return String(text).replace(/[&<>"']/g, function (match) {
            return ({
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#39;'
            })[match];
        });
    }

    function formatDateTime(value) {
        if (!value) {
            return '';
        }
        try {
            var date = new Date(value);
            return date.toLocaleString('vi-VN', {
                hour: '2-digit',
                minute: '2-digit',
                day: '2-digit',
                month: '2-digit'
            });
        } catch (error) {
            return value;
        }
    }

    function parseTimestamp(value) {
        if (!value) {
            return 0;
        }
        try {
            return new Date(value).getTime();
        } catch (error) {
            return 0;
        }
    }

    function formatStatus(status) {
        if (!status) {
            return 'unknown';
        }
        var normalized = String(status).toLowerCase();
        switch (normalized) {
            case 'open':
                return 'Đang mở';
            case 'pending':
                return 'Đang chờ';
            case 'resolved':
                return 'Đã xử lý';
            case 'closed':
                return 'Đã đóng';
            default:
                return status;
        }
    }

    function setFormStatus(message) {
        if (els.formStatus) {
            els.formStatus.textContent = message || '';
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        if (els.refreshBtn) {
            els.refreshBtn.addEventListener('click', function () {
                loadConversations();
                loadMessages(true);
            });
        }

        if (els.replyForm) {
            els.replyForm.addEventListener('submit', handleReplySubmit);
        }

        loadConversations();
        startPolling();
    });
})(window, document);
