(function (window, document) {
    'use strict';

    var appShell = window.appShell;
    var apiClient = window.apiClient;
    if (!appShell || !apiClient) {
        return;
    }

    var state = {
        conversation: null,
        messages: [],
        messageIndex: new Map(),
        lastTimestamp: 0,
        pollingTimer: null,
        isLoading: false,
        isSending: false,
        initialized: false
    };

    var selectors = {
        panel: null,
        fab: null,
        triggers: [],
        closeBtn: null,
        form: null,
        input: null,
        status: null,
        messages: null,
        submitBtn: null
    };

    function initDom() {
        selectors.panel = document.getElementById('supportChatPanel');
        selectors.fab = document.getElementById('supportChatFab');
        selectors.closeBtn = selectors.panel ? selectors.panel.querySelector('[data-support-chat-close]') : null;
        selectors.form = document.getElementById('supportChatForm');
        selectors.input = document.getElementById('supportChatInput');
        selectors.status = document.getElementById('supportChatStatus');
        selectors.messages = document.getElementById('supportChatMessages');
        selectors.submitBtn = selectors.form ? selectors.form.querySelector('button[type="submit"]') : null;
        selectors.triggers = Array.prototype.slice.call(document.querySelectorAll('[data-support-chat-open]'));
    }

    function guardAuth() {
        var token = window.localStorage.getItem('auth_token');
        if (!token || !token.trim()) {
            var ctx = appShell.contextPath || '';
            window.location.href = ctx + '/login.jsp';
            return false;
        }
        return true;
    }

    function openPanel() {
        if (!selectors.panel) {
            return;
        }
        selectors.panel.classList.remove('hidden');
        selectors.fab && selectors.fab.classList.add('hidden');
        if (!state.initialized) {
            loadConversation();
        } else {
            schedulePolling();
        }
    }

    function closePanel() {
        if (!selectors.panel) {
            return;
        }
        selectors.panel.classList.add('hidden');
        selectors.fab && selectors.fab.classList.remove('hidden');
        stopPolling();
    }

    function setStatus(text) {
        if (!selectors.status) {
            return;
        }
        selectors.status.textContent = text || '';
    }

    function setFormDisabled(disabled) {
        if (selectors.input) {
            selectors.input.disabled = disabled;
        }
        if (selectors.submitBtn) {
            selectors.submitBtn.disabled = disabled;
        }
    }

    function loadConversation() {
        if (state.isLoading) {
            return;
        }
        state.isLoading = true;
        setStatus('Đang tải hội thoại...');
        apiClient.get('/support-chat')
            .then(function (payload) {
                state.initialized = true;
                state.conversation = payload.conversation || null;
                mergeMessages(payload.messages || []);
                scrollToBottom();
                setStatus('Bạn đang trò chuyện với hỗ trợ Bookish Bliss Haven.');
                schedulePolling();
            })
            .catch(function (error) {
                console.error('supportChat load error', error);
                setStatus('Không thể tải kênh hỗ trợ. Vui lòng thử lại sau.');
            })
            .finally(function () {
                state.isLoading = false;
            });
    }

    function mergeMessages(list) {
        if (!Array.isArray(list) || list.length === 0) {
            return;
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
            renderMessage(message);
            var ts = parseTimestamp(message.createdAt);
            if (ts > state.lastTimestamp) {
                state.lastTimestamp = ts;
            }
        });
    }

    function renderMessage(message) {
        if (!selectors.messages) {
            return;
        }
        var wrapper = document.createElement('div');
        var isSupport = message.senderType && message.senderType.toLowerCase() === 'support';
        wrapper.className = 'flex ' + (isSupport ? 'justify-start' : 'justify-end');

        var bubble = document.createElement('div');
        bubble.className = 'max-w-[85%] px-3 py-2 rounded-2xl shadow-sm text-sm leading-relaxed ' +
            (isSupport
                ? 'bg-white text-gray-700 border border-amber-100'
                : 'bg-amber-600 text-white');

        var content = document.createElement('div');
        content.textContent = message.content || '';
        bubble.appendChild(content);

        var meta = document.createElement('div');
        meta.className = 'mt-1 text-[11px] uppercase tracking-wide ' + (isSupport ? 'text-gray-400' : 'text-amber-100/80');
        meta.textContent = formatTime(message.createdAt);
        bubble.appendChild(meta);

        wrapper.appendChild(bubble);
        selectors.messages.appendChild(wrapper);
    }

    function scrollToBottom() {
        if (!selectors.messages) {
            return;
        }
        selectors.messages.scrollTop = selectors.messages.scrollHeight;
    }

    function parseTimestamp(value) {
        if (!value) {
            return 0;
        }
        try {
            return Date.parse(value);
        } catch (e) {
            return 0;
        }
    }

    function formatTime(value) {
        if (!value) {
            return '';
        }
        try {
            return new Date(value).toLocaleString('vi-VN', {
                hour: '2-digit',
                minute: '2-digit',
                day: '2-digit',
                month: '2-digit'
            });
        } catch (e) {
            return value;
        }
    }

    function schedulePolling() {
        stopPolling();
        state.pollingTimer = window.setInterval(fetchUpdates, 12000);
    }

    function stopPolling() {
        if (state.pollingTimer) {
            window.clearInterval(state.pollingTimer);
            state.pollingTimer = null;
        }
    }

    function fetchUpdates() {
        if (!state.initialized || state.isLoading) {
            return;
        }
        var query = '';
        if (state.lastTimestamp > 0) {
            query = '?since=' + state.lastTimestamp;
        }
        apiClient.get('/support-chat' + query)
            .then(function (payload) {
                mergeMessages(payload.messages || []);
                if (payload.conversation) {
                    state.conversation = payload.conversation;
                }
                if (payload.messages && payload.messages.length > 0) {
                    scrollToBottom();
                }
            })
            .catch(function (error) {
                console.warn('supportChat poll error', error);
            });
    }

    function sendMessage(content) {
        if (state.isSending) {
            return;
        }
        var trimmed = content ? content.trim() : '';
        if (!trimmed) {
            setStatus('Vui lòng nhập nội dung tin nhắn.');
            return;
        }
        state.isSending = true;
        setFormDisabled(true);
        setStatus('Đang gửi tin nhắn...');
        apiClient.post('/support-chat', { content: trimmed })
            .then(function (payload) {
                setStatus('Tin nhắn đã được gửi.');
                selectors.input && (selectors.input.value = '');
                if (payload.message) {
                    mergeMessages([payload.message]);
                    scrollToBottom();
                }
            })
            .catch(function (error) {
                console.error('supportChat send error', error);
                setStatus('Không thể gửi tin nhắn. Vui lòng thử lại.');
            })
            .finally(function () {
                state.isSending = false;
                setFormDisabled(false);
            });
    }

    function bindEvents() {
        selectors.triggers.forEach(function (trigger) {
            trigger.addEventListener('click', function (event) {
                event.preventDefault();
                if (!guardAuth()) {
                    return;
                }
                openPanel();
            });
        });

        if (selectors.closeBtn) {
            selectors.closeBtn.addEventListener('click', function () {
                closePanel();
            });
        }

        if (selectors.form) {
            selectors.form.addEventListener('submit', function (event) {
                event.preventDefault();
                if (!guardAuth()) {
                    return;
                }
                var value = selectors.input ? selectors.input.value : '';
                sendMessage(value);
            });
        }
    }

    appShell.onReady(function () {
        initDom();
        if (!selectors.panel) {
            return;
        }
        bindEvents();
        if (window.feather && typeof window.feather.replace === 'function') {
            window.feather.replace();
        }
    });

})(window, document);
