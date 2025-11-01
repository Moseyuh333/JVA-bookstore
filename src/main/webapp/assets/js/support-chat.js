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
        locked: false,
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
        selectors.submitBtn = selectors.form ? selectors.form.querySelector('button[type=\"submit\"]') : null;
        selectors.triggers = Array.prototype.slice.call(document.querySelectorAll('[data-support-chat-open]'));
    }

    function guardAuth() {
        var token = window.localStorage.getItem('auth_token');
        if (!token || !token.trim()) {
            redirectToLogin();
            return false;
        }
        return true;
    }

    function openPanel() {
        if (!selectors.panel) {
            return;
        }
        selectors.panel.classList.remove('hidden');
        if (selectors.fab) {
            selectors.fab.classList.add('hidden');
        }
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
        if (selectors.fab) {
            selectors.fab.classList.remove('hidden');
        }
        stopPolling();
    }

    function setStatus(text) {
        if (selectors.status) {
            selectors.status.textContent = text || '';
        }
    }

    function setFormDisabled(disabled) {
        var effective = disabled || state.locked;
        if (selectors.input) {
            selectors.input.disabled = effective;
        }
        if (selectors.submitBtn) {
            selectors.submitBtn.disabled = effective;
        }
    }

    function loadConversation() {
        if (state.isLoading) {
            return;
        }
        state.isLoading = true;
        setStatus('Dang tai hoi thoai...');
        apiClient.get('/support-chat')
            .then(function (payload) {
                state.initialized = true;
                state.conversation = payload.conversation || null;
                state.locked = false;
                setFormDisabled(false);
                mergeMessages(payload.messages || []);
                scrollToBottom();
                setStatus('Ban dang tro chuyen voi ho tro Bookish Bliss Haven.');
                schedulePolling();
            })
            .catch(handleLoadError)
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
        var isSupport = message.senderType && message.senderType.toLowerCase() === 'support';
        var wrapper = document.createElement('div');
        wrapper.className = 'flex ' + (isSupport ? 'justify-start' : 'justify-end');

        var bubble = document.createElement('div');
        bubble.className = 'max-w-[85%] px-3 py-2 rounded-2xl shadow-sm text-sm leading-relaxed ' +
            (isSupport ? 'bg-white text-gray-700 border border-amber-100' : 'bg-amber-600 text-white');

        var body = document.createElement('div');
        body.textContent = message.content || '';
        bubble.appendChild(body);

        var meta = document.createElement('div');
        meta.className = 'mt-1 text-[11px] uppercase tracking-wide ' +
            (isSupport ? 'text-gray-400' : 'text-amber-100/80');
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
        var parsed = Date.parse(value);
        return Number.isNaN(parsed) ? 0 : parsed;
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
        } catch (error) {
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
                if (payload.conversation) {
                    state.conversation = payload.conversation;
                }
                mergeMessages(payload.messages || []);
                if (payload.messages && payload.messages.length > 0) {
                    scrollToBottom();
                }
            })
            .catch(function (error) {
                if (error && error.status === 401) {
                    handleUnauthorized();
                } else {
                    console.warn('supportChat poll error', error);
                }
            });
    }

    function sendMessage(content) {
        if (state.isSending || state.locked) {
            return;
        }
        var trimmed = content ? content.trim() : '';
        if (!trimmed) {
            setStatus('Vui long nhap noi dung tin nhan.');
            return;
        }
        state.isSending = true;
        setFormDisabled(true);
        setStatus('Dang gui tin nhan...');
        apiClient.post('/support-chat', { content: trimmed })
            .then(function (payload) {
                setStatus('Tin nhan da duoc gui.');
                if (selectors.input) {
                    selectors.input.value = '';
                }
                if (payload.message) {
                    mergeMessages([payload.message]);
                }
                if (payload.autoReply) {
                    mergeMessages([payload.autoReply]);
                    setStatus('He thong da phan hoi tu dong cho cau hoi cua ban.');
                }
                scrollToBottom();
            })
            .catch(function (error) {
                if (error && error.status === 401) {
                    handleUnauthorized();
                } else {
                    console.error('supportChat send error', error);
                    setStatus('Khong the gui tin nhan. Vui long thu lai.');
                }
            })
            .finally(function () {
                state.isSending = false;
                setFormDisabled(false);
            });
    }

    function handleLoadError(error) {
        if (error && error.status === 401) {
            handleUnauthorized();
            return;
        }
        console.error('supportChat load error', error);
        setStatus('Khong the tai kenh ho tro. Vui long thu lai sau.');
    }

    function handleUnauthorized() {
        clearAuthTokens();
        state.locked = true;
        setFormDisabled(true);
        setStatus('Phien dang nhap da het han. Dang chuyen toi trang dang nhap...');
        stopPolling();
        redirectToLogin();
    }

    function clearAuthTokens() {
        try {
            window.localStorage.removeItem('auth_token');
            window.localStorage.removeItem('auth_username');
            window.localStorage.removeItem('auth_role');
        } catch (error) {
            console.warn('supportChat clear token error', error);
        }
    }

    function redirectToLogin() {
        var ctx = appShell && appShell.contextPath ? appShell.contextPath : '';
        window.setTimeout(function () {
            window.location.href = ctx + '/login.jsp';
        }, 1200);
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
            selectors.closeBtn.addEventListener('click', closePanel);
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
