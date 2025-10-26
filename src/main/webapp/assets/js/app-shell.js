(function (window, document) {
    'use strict';

    const config = window.appConfig || {};
    const contextPath = config.contextPath || '';
    const booksApiBase = contextPath ? contextPath + '/api/books' : '/api/books';

    let dropdownInitialized = false;

    function onReady(callback) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', callback, { once: true });
        } else {
            callback();
        }
    }

    function refreshIcons() {
        if (window.feather && typeof window.feather.replace === 'function') {
            window.feather.replace();
        }
    }

    function escapeHtml(text) {
        if (text === null || text === undefined) {
            return '';
        }
        return String(text).replace(/[&<>"']/g, function (match) {
            switch (match) {
                case '&': return '&amp;';
                case '<': return '&lt;';
                case '>': return '&gt;';
                case '"': return '&quot;';
                case "'": return '&#39;';
                default: return match;
            }
        });
    }

    function formatCurrency(value) {
        if (value === null || value === undefined) {
            return 'Lien he';
        }
        var numeric = Number(value);
        if (!Number.isFinite(numeric)) {
            return 'Lien he';
        }
        var rounded = Math.round(numeric);
        try {
            return new Intl.NumberFormat('vi-VN').format(rounded) + ' VND';
        } catch (error) {
            return String(rounded) + ' VND';
        }
    }

    function readJwtSubject(token) {
        if (!token) {
            return null;
        }
        try {
            const payloadPart = token.split('.')[1];
            if (!payloadPart) {
                return null;
            }
            const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
            const padded = normalized + '='.repeat((4 - normalized.length % 4) % 4);
            const payload = JSON.parse(atob(padded));
            const subject = payload && typeof payload.sub === 'string' ? payload.sub.trim() : null;
            return subject && subject.length > 0 ? subject : null;
        } catch (error) {
            console.warn('Unable to decode token payload', error);
            return null;
        }
    }

    function getStoredUsername(token) {
        const cached = window.localStorage.getItem('auth_username');
        if (cached && cached.trim().length > 0) {
            return cached.trim();
        }
        const subject = readJwtSubject(token);
        if (subject) {
            window.localStorage.setItem('auth_username', subject);
        }
        return subject;
    }

    function setAccountLabel(text) {
        const label = document.getElementById('accountBtnLabel');
        if (label) {
            label.textContent = text;
        }
    }

    function renderGuestDropdown(container) {
        container.innerHTML = `
            <div class="py-2">
                <a href="${contextPath}/login.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                    Đăng nhập
                </a>
                <a href="${contextPath}/register.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                    Đăng ký
                </a>
                <hr class="my-1">
                <a href="${contextPath}/forgot-password.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="key" class="w-4 h-4 mr-2"></i>
                    Quên mật khẩu
                </a>
            </div>`;
        setAccountLabel('Tài khoản');
        refreshIcons();
    }

    function renderUserDropdown(container, username) {
        const safeName = username && username.trim().length > 0 ? escapeHtml(username.trim()) : null;
        const greeting = safeName ? 'Xin chào, ' + safeName + '!' : 'Xin chào!';
        container.innerHTML = `
            <div class="py-2">
                <div class="px-4 py-2 text-sm text-gray-600 border-b flex items-center gap-2">
                    <i data-feather="user" class="w-4 h-4"></i>
                    <span>${greeting}</span>
                </div>
                <a href="${contextPath}/profile.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="settings" class="w-4 h-4 mr-2"></i>
                    Hồ sơ cá nhân
                </a>
                <button type="button" data-action="logout" class="w-full text-left flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="log-out" class="w-4 h-4 mr-2"></i>
                    Đăng xuất
                </button>
            </div>`;
        setAccountLabel(safeName || 'Tài khoản');
        refreshIcons();
    }

    function initUserDropdown() {
        if (dropdownInitialized) {
            return;
        }
        dropdownInitialized = true;
        const trigger = document.getElementById('userDropdownBtn');
        const dropdown = document.getElementById('userDropdown');
        if (!trigger || !dropdown) {
            return;
        }

        const token = window.localStorage.getItem('auth_token');
        const username = getStoredUsername(token);
        if (token) {
            renderUserDropdown(dropdown, username);
        } else {
            renderGuestDropdown(dropdown);
        }

        trigger.addEventListener('click', function (event) {
            event.stopPropagation();
            dropdown.classList.toggle('hidden');
        });

        document.addEventListener('click', function () {
            dropdown.classList.add('hidden');
        });

        dropdown.addEventListener('click', function (event) {
            if (event.target.closest('[data-action="logout"]')) {
                event.preventDefault();
                handleLogout(dropdown);
                return;
            }
            event.stopPropagation();
        });
    }

    function handleLogout(dropdown) {
        window.localStorage.removeItem('auth_token');
        window.localStorage.removeItem('auth_username');
        // Also remove admin tokens if they exist
        window.localStorage.removeItem('admin_token');
        window.localStorage.removeItem('admin_username');
        renderGuestDropdown(dropdown);
        dropdown.classList.add('hidden');
        window.location.href = contextPath + '/login.jsp';
    }

    function updateYearBadge() {
        const badge = document.getElementById('year');
        if (badge) {
            badge.textContent = new Date().getFullYear();
        }
    }

    onReady(function () {
        refreshIcons();
        initUserDropdown();
        updateYearBadge();
    });

    window.appShell = {
        contextPath: contextPath,
        booksApiBase: booksApiBase,
        escapeHtml: escapeHtml,
        formatCurrency: formatCurrency,
        getStoredUsername: getStoredUsername,
        initUserDropdown: initUserDropdown,
        updateYearBadge: updateYearBadge,
        refreshIcons: refreshIcons,
        onReady: onReady
    };
})(window, document);
