const contextPath = window.appConfig?.contextPath || '';
let cachedUsers = [];
let currentSearchTerm = '';

function buildAuthHeaders(base = {}) {
    const headers = { ...base };
    const token = localStorage.getItem('admin_token');
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    return headers;
}

async function loadAdminUsers(searchTerm = currentSearchTerm) {
    const tableBody = document.querySelector('#User');
    const loading = document.querySelector('#loadingState');
    const emptyState = document.querySelector('#emptyState');

    if (!tableBody) {
        console.warn('Không tìm thấy phần tử bảng người dùng.');
        return;
    }

    try {
        if (loading) {
            loading.style.display = 'block';
        }
        tableBody.innerHTML = '';
        if (emptyState) {
            emptyState.style.display = 'none';
        }

        let url = `${contextPath}/api/admin/users?action=list`;
        if (searchTerm) {
            url += `&search=${encodeURIComponent(searchTerm)}`;
        }

        const response = await fetch(url, {
            headers: buildAuthHeaders({ 'Content-Type': 'application/json' })
        });

        const payload = await response.json();
        if (!response.ok || payload?.error) {
            const message = payload?.error || `Server trả lỗi: ${response.status}`;
            throw new Error(message);
        }

        const rawUsers = Array.isArray(payload?.users) ? payload.users : [];
        const seen = new Set();
        cachedUsers = rawUsers.filter(user => {
            const key = user?.id ?? user?.username;
            if (!key || seen.has(key)) {
                return false;
            }
            seen.add(key);
            return true;
        });

        if (cachedUsers.length === 0) {
            if (emptyState) {
                emptyState.style.display = 'block';
            }
            updateStats();
            return;
        }

        const rowsHtml = cachedUsers.map(user => {
            const initials = (user.username?.substring(0, 2) || 'U').toUpperCase();
            const fullName = user.full_name || '-';
            const birthDate = user.birth_date || '-';
            const address = user.address || '-';
            const email = user.email || '-';
            const phone = user.phone || '-';
            const role = user.role || 'customer';
            const roleBadgeClass = (role || '').toLowerCase() === 'admin' ? 'badge-admin' : 'badge-customer';

            return [
                '<tr>',
                '<td>',
                '<div class="user-cell">',
                `<div class="avatar">${initials}</div>`,
                '<div class="user-info-text">',
                `<div class="user-name">${user.username}</div>`,
                '</div>',
                '</div>',
                '</td>',
                `<td>${fullName}</td>`,
                `<td>${birthDate}</td>`,
                '<td>-</td>',
                `<td>${address}</td>`,
                `<td>${email}</td>`,
                `<td>${phone}</td>`,
                `<td><span class="badge-custom ${roleBadgeClass}">${role}</span></td>`,
                '<td class="actions">',
                '<button class="btn-icon btn-view" title="Xem">',
                '<i class="fas fa-eye"></i>',
                '</button>',
                '<button class="btn-icon btn-delete" title="Xóa">',
                '<i class="fas fa-trash"></i>',
                '</button>',
                '</td>',
                '</tr>'
            ].join('');
        }).join('');

        tableBody.innerHTML = rowsHtml;
        if (emptyState) {
            emptyState.style.display = 'none';
        }
        updateStats();
    } catch (error) {
        console.error('❌ Lỗi khi tải dữ liệu:', error);
        cachedUsers = [];
        if (emptyState) {
            emptyState.style.display = 'block';
        }
        updateStats();
    } finally {
        if (loading) {
            loading.style.display = 'none';
        }
    }
}

async function applyFilters() {
    const input = document.getElementById('searchInput');
    currentSearchTerm = input ? input.value.trim() : '';
    await loadAdminUsers(currentSearchTerm);
}

function resetFilters() {
    const input = document.getElementById('searchInput');
    if (input) {
        input.value = '';
    }
    currentSearchTerm = '';
    loadAdminUsers('');
}

function updateStats() {
    const totalEl = document.getElementById('totalUsers');
    const activeEl = document.getElementById('activeUsers');
    const total = cachedUsers.length;
    const active = cachedUsers.filter(user => (user.status || '').toLowerCase() === 'active').length;

    if (totalEl) {
        totalEl.textContent = total;
    }
    if (activeEl) {
        activeEl.textContent = active;
    }
}

document.getElementById('searchInput')?.addEventListener('input', event => {
    const value = event.target.value.trim();
    if (value.length === 0 || value.length >= 2) {
        applyFilters();
    }
});

function setupCreateUserModal() {
    const modal = document.getElementById('createUserModal');
    const openBtn = document.getElementById('openCreateUserBtn');
    const form = document.getElementById('createUserForm');
    const feedback = document.getElementById('createUserFeedback');
    const submitBtn = document.getElementById('createUserSubmit');

    if (!modal || !openBtn || !form || !submitBtn) {
        return;
    }

    const usernameInput = document.getElementById('createUsername');
    const emailInput = document.getElementById('createEmail');
    const passwordInput = document.getElementById('createPassword');
    const fullNameInput = document.getElementById('createFullName');
    const phoneInput = document.getElementById('createPhone');
    const roleInput = document.getElementById('createRole');
    const statusSelect = document.getElementById('createStatus');

    let lastFocusedElement = null;

    const clearFeedback = () => {
        if (!feedback) {
            return;
        }
        feedback.textContent = '';
        feedback.className = 'form-feedback';
    };

    const setFeedback = (message, type = 'error') => {
        if (!feedback) {
            return;
        }
        feedback.textContent = message;
        feedback.className = type === 'success' ? 'form-feedback success' : 'form-feedback error';
    };

    const closeModal = () => {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        modal.setAttribute('aria-modal', 'false');
        document.body.classList.remove('modal-open');
        form.reset();
        clearFeedback();
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            lastFocusedElement.focus();
        }
    };

    const openModal = () => {
        lastFocusedElement = document.activeElement;
        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
        modal.setAttribute('aria-modal', 'true');
        document.body.classList.add('modal-open');
        clearFeedback();
        if (usernameInput && typeof usernameInput.focus === 'function') {
            usernameInput.focus({ preventScroll: true });
        }
    };

    openBtn.addEventListener('click', openModal);

    modal.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', closeModal);
    });

    modal.addEventListener('click', event => {
        if (event.target === modal) {
            closeModal();
        }
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && modal.classList.contains('show')) {
            closeModal();
        }
    });

    form.addEventListener('submit', async event => {
        event.preventDefault();

        const username = usernameInput?.value.trim() || '';
        const email = emailInput?.value.trim() || '';
        const password = passwordInput?.value.trim() || '';
        const fullName = fullNameInput?.value.trim() || '';
        const phone = phoneInput?.value.trim() || '';
        const role = roleInput?.value.trim() || '';
        const status = statusSelect?.value || 'active';

        if (!username || !email || !password) {
            setFeedback('Vui lòng nhập đầy đủ thông tin bắt buộc.', 'error');
            return;
        }

        if (password.length < 6) {
            setFeedback('Mật khẩu phải có ít nhất 6 ký tự.', 'error');
            return;
        }

        const token = localStorage.getItem('admin_token');
        if (!token) {
            setFeedback('Không tìm thấy phiên đăng nhập quản trị. Vui lòng đăng nhập lại.', 'error');
            return;
        }

        const payload = new URLSearchParams();
        payload.append('action', 'create');
        payload.append('username', username);
        payload.append('email', email);
        payload.append('password', password);
        if (fullName) {
            payload.append('full_name', fullName);
        }
        if (phone) {
            payload.append('phone', phone);
        }
        if (role) {
            payload.append('role', role);
        }
        if (status) {
            payload.append('status', status);
        }

        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang tạo...';

        try {
            const response = await fetch(`${contextPath}/api/admin/users`, {
                method: 'POST',
                headers: buildAuthHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' }),
                body: payload.toString()
            });

            let data = null;
            try {
                data = await response.json();
            } catch (jsonError) {
                // ignore
            }

            if (!response.ok || (data && data.error)) {
                const message = data?.error || `Không thể tạo tài khoản (mã ${response.status})`;
                throw new Error(message);
            }

            setFeedback('Tạo tài khoản thành công.', 'success');
            await loadAdminUsers(currentSearchTerm);

            setTimeout(() => {
                closeModal();
            }, 600);
        } catch (error) {
            console.error('❌ Lỗi khi tạo tài khoản:', error);
            setFeedback(error.message || 'Đã xảy ra lỗi khi tạo tài khoản.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });
}

window.addEventListener('load', () => {
    if (typeof feather !== 'undefined') {
        feather.replace();
    }

    setupCreateUserModal();
    loadAdminUsers();
});
