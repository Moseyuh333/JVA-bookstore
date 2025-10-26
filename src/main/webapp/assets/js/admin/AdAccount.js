const contextPath = window.appConfig?.contextPath || '';
let cachedUsers = [];
let currentSearchTerm = '';
let currentSearchType = 'all';
let currentStatusFilter = 'all';
let deleteTargetUser = null;
let viewModalController = null;
let deleteModalController = null;
let editModalController = null;
let editTargetUser = null;

const statusLabels = {
    active: 'Active',
    inactive: 'Inactive',
    banned: 'Banned',
    pending: 'Pending'
};

const roleLabels = {
    admin: 'Quản trị',
    customer: 'Khách hàng',
    user: 'Khách hàng',
    seller: 'Người bán',
    shipper: 'Nhân viên giao hàng'
};

function getAdminToken() {
    const rawAdminToken = localStorage.getItem('admin_token');
    if (rawAdminToken && rawAdminToken.trim().length > 0) {
        return rawAdminToken.trim();
    }
    const rawAuthToken = localStorage.getItem('auth_token');
    if (rawAuthToken && rawAuthToken.trim().length > 0) {
        return rawAuthToken.trim();
    }
    return null;
}

function buildAuthHeaders(base = {}) {
    const headers = { ...base };
    const token = getAdminToken();
    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }
    return headers;
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
        return value;
    }
    return parsed.toLocaleString('vi-VN', { hour12: false });
}

function formatLabel(value, dictionary, fallback = '-') {
    if (!value) {
        return fallback;
    }
    const key = value.toString().toLowerCase();
    return dictionary[key] || value;
}

function handleUnauthorized() {
    alert('Phiên đăng nhập quản trị đã hết hạn. Vui lòng đăng nhập lại.');
    window.localStorage.removeItem('admin_token');
    window.localStorage.removeItem('admin_username');
    const fallback = window.appConfig?.contextPath || '';
    window.location.href = `${fallback}/login.jsp`;
}

async function loadAdminUsers(searchTerm = currentSearchTerm, searchType = currentSearchType, statusFilter = currentStatusFilter) {
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
        if (searchType && searchType !== 'all') {
            url += `&searchType=${encodeURIComponent(searchType)}`;
        }
        if (statusFilter && statusFilter !== 'all') {
            url += `&status=${encodeURIComponent(statusFilter)}`;
        }

        const response = await fetch(url, {
            headers: buildAuthHeaders({ 'Content-Type': 'application/json' })
        });

        if (response.status === 401) {
            handleUnauthorized();
            return;
        }

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
            const roleKey = (role || '').toLowerCase();
            let roleBadgeClass = 'badge-customer';
            if (roleKey === 'admin') {
                roleBadgeClass = 'badge-admin';
            } else if (roleKey === 'seller') {
                roleBadgeClass = 'badge-seller';
            } else if (roleKey === 'shipper') {
                roleBadgeClass = 'badge-shipper';
            }

            const status = user.status || 'active';
            const statusKey = (status || '').toLowerCase();
            let statusBadgeClass = 'badge-pending';
            if (statusKey === 'active') {
                statusBadgeClass = 'badge-seller'; // green for active
            } else if (statusKey === 'inactive') {
                statusBadgeClass = 'badge-banned'; // red for inactive
            } else if (statusKey === 'banned') {
                statusBadgeClass = 'badge-banned'; // red for banned
            } else if (statusKey === 'pending') {
                statusBadgeClass = 'badge-pending'; // yellow for pending
            }

            return [
                `<tr data-user-id="${user.id}">`,
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
                    `<td><span class="badge ${statusKey === 'active' ? 'badge-success' : statusKey === 'inactive' ? 'badge-secondary' : statusKey === 'banned' ? 'badge-danger' : 'badge-warning'}">${statusLabels[statusKey] || status}</span></td>`,
                    '<td class="actions">',
                    `<button class="btn-icon btn-edit" title="Chỉnh sửa" data-user-id="${user.id}">`,
                    '<i class="fas fa-edit"></i>',
                    '</button>',
                    `<button class="btn-icon btn-view" title="Xem" data-user-id="${user.id}">`,
                    '<i class="fas fa-eye"></i>',
                    '</button>',
                    `<button class="btn-icon btn-delete" title="Xóa" data-user-id="${user.id}">`,
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
    const typeSelect = document.getElementById('searchType');
    const statusSelect = document.getElementById('statusFilter');
    currentSearchTerm = input ? input.value.trim() : '';
    currentSearchType = typeSelect ? typeSelect.value : 'all';
    currentStatusFilter = statusSelect ? statusSelect.value : 'all';
    await loadAdminUsers(currentSearchTerm, currentSearchType, currentStatusFilter);
}

function resetFilters() {
    const input = document.getElementById('searchInput');
    const typeSelect = document.getElementById('searchType');
    const statusSelect = document.getElementById('statusFilter');
    if (input) {
        input.value = '';
    }
    if (typeSelect) {
        typeSelect.value = 'all';
    }
    if (statusSelect) {
        statusSelect.value = 'all';
    }
    currentSearchTerm = '';
    currentSearchType = 'all';
    currentStatusFilter = 'all';
    loadAdminUsers('', 'all', 'all');
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

function findUserById(id) {
    if (!id) {
        return null;
    }
    return cachedUsers.find(user => String(user.id) === String(id));
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

        const token = getAdminToken();
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

            if (response.status === 401) {
                handleUnauthorized();
                return;
            }

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

function createEditUserModalController() {
    const modal = document.getElementById('editUserModal');
    const form = document.getElementById('editUserForm');
    const feedback = document.getElementById('editUserFeedback');
    const submitBtn = document.getElementById('editUserSubmit');
    const idInput = document.getElementById('editUserId');
    const usernameInput = document.getElementById('editUsername');
    const emailInput = document.getElementById('editEmail');
    const fullNameInput = document.getElementById('editFullName');
    const phoneInput = document.getElementById('editPhone');
    const roleSelect = document.getElementById('editRole');
    const statusSelect = document.getElementById('editStatus');

    if (!modal || !form || !submitBtn || !idInput || !usernameInput || !emailInput) {
        return { open: () => {} };
    }

    let lastFocusedElement = null;

    const clearFeedback = () => {
        if (feedback) {
            feedback.textContent = '';
            feedback.className = 'form-feedback';
        }
    };

    const setFeedback = (message, type = 'error') => {
        if (feedback) {
            feedback.textContent = message;
            feedback.className = type === 'success' ? 'form-feedback success' : 'form-feedback error';
        }
    };

    const closeModal = () => {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        modal.setAttribute('aria-modal', 'false');
        document.body.classList.remove('modal-open');
        form.reset();
        clearFeedback();
        editTargetUser = null;
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            lastFocusedElement.focus({ preventScroll: true });
        }
    };

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
        if (!editTargetUser) {
            setFeedback('Không xác định được tài khoản cần chỉnh sửa.');
            return;
        }

        const username = usernameInput.value.trim();
        const email = emailInput.value.trim();
        const fullName = fullNameInput?.value.trim() || '';
        const phone = phoneInput?.value.trim() || '';
        const role = roleSelect?.value || '';
        const status = statusSelect?.value || '';

        if (!username || !email) {
            setFeedback('Tên đăng nhập và email là bắt buộc.');
            return;
        }

        const payload = new URLSearchParams();
        payload.append('action', 'update');
        payload.append('id', String(editTargetUser.id));
        payload.append('username', username);
        payload.append('email', email);
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
        submitBtn.textContent = 'Đang lưu...';

        try {
            const response = await fetch(`${contextPath}/api/admin/users`, {
                method: 'POST',
                headers: buildAuthHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' }),
                body: payload.toString()
            });

            if (response.status === 401) {
                handleUnauthorized();
                return;
            }

            let data = null;
            try {
                data = await response.json();
            } catch (jsonError) {
                // ignore parse errors
            }

            if (!response.ok || (data && data.error)) {
                const message = data?.error || `Không thể cập nhật tài khoản (mã ${response.status})`;
                throw new Error(message);
            }

            setFeedback('Cập nhật tài khoản thành công.', 'success');
            await loadAdminUsers(currentSearchTerm);
            setTimeout(() => {
                closeModal();
            }, 400);
        } catch (error) {
            console.error('❌ Lỗi khi cập nhật tài khoản:', error);
            setFeedback(error.message || 'Đã xảy ra lỗi khi cập nhật tài khoản.');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    });

    const open = user => {
        if (!user) {
            return;
        }
        editTargetUser = user;
        lastFocusedElement = document.activeElement;
        clearFeedback();

        idInput.value = user.id || '';
        usernameInput.value = user.username || '';
        emailInput.value = user.email || '';
        if (fullNameInput) {
            fullNameInput.value = user.full_name || '';
        }
        if (phoneInput) {
            phoneInput.value = user.phone || '';
        }
        if (roleSelect) {
            const normalizedRole = (user.role || 'customer').toLowerCase();
            roleSelect.value = roleSelect.querySelector(`option[value="${normalizedRole}"]`) ? normalizedRole : 'customer';
        }
        if (statusSelect) {
            const normalizedStatus = (user.status || 'active').toLowerCase();
            statusSelect.value = statusSelect.querySelector(`option[value="${normalizedStatus}"]`) ? normalizedStatus : 'active';
        }

        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
        modal.setAttribute('aria-modal', 'true');
        document.body.classList.add('modal-open');
        if (usernameInput && typeof usernameInput.focus === 'function') {
            usernameInput.focus({ preventScroll: true });
        }
    };

    return { open, close: closeModal };
}

function createViewModalController() {
    const modal = document.getElementById('viewUserModal');
    if (!modal) {
        return {
            open: () => {}
        };
    }

    const fields = {
        username: document.getElementById('viewUsername'),
        email: document.getElementById('viewEmail'),
        fullName: document.getElementById('viewFullName'),
        phone: document.getElementById('viewPhone'),
        role: document.getElementById('viewRole'),
        status: document.getElementById('viewStatus'),
        verified: document.getElementById('viewVerified'),
        created: document.getElementById('viewCreated'),
        updated: document.getElementById('viewUpdated'),
        address: document.getElementById('viewAddress'),
        birthDate: document.getElementById('viewBirthDate')
    };

    let lastFocusedElement = null;

    const fillField = (element, value) => {
        if (element) {
            element.textContent = value || '-';
        }
    };

    const closeModal = () => {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        modal.setAttribute('aria-modal', 'false');
        document.body.classList.remove('modal-open');
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            lastFocusedElement.focus({ preventScroll: true });
        }
    };

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

    const open = user => {
        if (!user) {
            return;
        }
        lastFocusedElement = document.activeElement;

        fillField(fields.username, user.username);
        fillField(fields.email, user.email);
        fillField(fields.fullName, user.full_name);
        fillField(fields.phone, user.phone);

        const roleText = user.role
            ? `${formatLabel(user.role, roleLabels)} (${user.role.toLowerCase()})`
            : '-';
        fillField(fields.role, roleText);

        const statusText = user.status
            ? `${formatLabel(user.status, statusLabels)} (${user.status.toLowerCase()})`
            : '-';
        fillField(fields.status, statusText);

        fillField(fields.verified, user.verified ? 'Đã xác thực' : 'Chưa xác thực');
        fillField(fields.created, formatDateTime(user.created));
        fillField(fields.updated, formatDateTime(user.updated));
        fillField(fields.address, user.address || '-');
        fillField(fields.birthDate, user.birth_date || '-');

        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
        modal.setAttribute('aria-modal', 'true');
        document.body.classList.add('modal-open');

        const firstClose = modal.querySelector('[data-close-modal]');
        if (firstClose && typeof firstClose.focus === 'function') {
            firstClose.focus({ preventScroll: true });
        }
    };

    return { open, close: closeModal };
}

function createDeleteModalController() {
    const modal = document.getElementById('deleteUserModal');
    const summary = document.getElementById('deleteUserSummary');
    const feedback = document.getElementById('deleteUserFeedback');
    const confirmBtn = document.getElementById('deleteUserConfirm');

    if (!modal || !summary || !feedback || !confirmBtn) {
        return {
            open: () => {}
        };
    }

    let lastFocusedElement = null;
    let isSubmitting = false;
    const defaultConfirmText = confirmBtn.textContent;

    const clearFeedback = () => {
        feedback.textContent = '';
        feedback.className = 'form-feedback';
    };

    const closeModal = () => {
        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        modal.setAttribute('aria-modal', 'false');
        document.body.classList.remove('modal-open');
        confirmBtn.disabled = false;
        confirmBtn.textContent = defaultConfirmText;
        isSubmitting = false;
        clearFeedback();
        deleteTargetUser = null;
        if (lastFocusedElement && typeof lastFocusedElement.focus === 'function') {
            lastFocusedElement.focus({ preventScroll: true });
        }
    };

    modal.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            if (!isSubmitting) {
                closeModal();
            }
        });
    });

    modal.addEventListener('click', event => {
        if (event.target === modal && !isSubmitting) {
            closeModal();
        }
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && modal.classList.contains('show') && !isSubmitting) {
            closeModal();
        }
    });

    confirmBtn.addEventListener('click', async () => {
        if (!deleteTargetUser || isSubmitting) {
            return;
        }

        isSubmitting = true;
        confirmBtn.disabled = true;
        confirmBtn.textContent = 'Đang xóa...';
        clearFeedback();

        try {
            const payload = new URLSearchParams();
            payload.append('action', 'delete');
            payload.append('id', deleteTargetUser.id);

            const response = await fetch(`${contextPath}/api/admin/users`, {
                method: 'POST',
                headers: buildAuthHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' }),
                body: payload.toString()
            });

            if (response.status === 401) {
                handleUnauthorized();
                return;
            }

            let data = null;
            try {
                data = await response.json();
            } catch (jsonError) {
                // ignore
            }

            if (!response.ok || (data && data.error)) {
                const message = data?.error || `Không thể xóa tài khoản (mã ${response.status})`;
                throw new Error(message);
            }

            await loadAdminUsers(currentSearchTerm);
            closeModal();
        } catch (error) {
            console.error('❌ Lỗi khi xóa tài khoản:', error);
            feedback.textContent = error.message || 'Không thể xóa tài khoản.';
            feedback.className = 'form-feedback error';
        } finally {
            if (modal.classList.contains('show')) {
                isSubmitting = false;
                confirmBtn.disabled = false;
                confirmBtn.textContent = defaultConfirmText;
            }
        }
    });

    const open = user => {
        if (!user) {
            return;
        }

        deleteTargetUser = user;
        lastFocusedElement = document.activeElement;
        summary.textContent = `Tài khoản "${user.username}" (${user.email || 'không có email'}) sẽ bị xóa vĩnh viễn.`;
        clearFeedback();

        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
        modal.setAttribute('aria-modal', 'true');
        document.body.classList.add('modal-open');

        confirmBtn.focus({ preventScroll: true });
    };

    return { open, close: closeModal };
}

function setupTableActions() {
    const tableBody = document.querySelector('#User');
    if (!tableBody) {
        return;
    }

    tableBody.addEventListener('click', event => {
        const editBtn = event.target.closest('.btn-edit');
        if (editBtn) {
            const user = findUserById(editBtn.dataset.userId);
            if (user && editModalController) {
                editModalController.open(user);
            }
            return;
        }

        const viewBtn = event.target.closest('.btn-view');
        if (viewBtn) {
            const user = findUserById(viewBtn.dataset.userId);
            if (user && viewModalController) {
                viewModalController.open(user);
            }
            return;
        }

        const deleteBtn = event.target.closest('.btn-delete');
        if (deleteBtn) {
            const user = findUserById(deleteBtn.dataset.userId);
            if (user && deleteModalController) {
                deleteModalController.open(user);
            }
        }
    });
}

function initAdminAccountPage() {
    if (typeof feather !== 'undefined') {
        feather.replace();
    }

    setupCreateUserModal();
    editModalController = createEditUserModalController();
    viewModalController = createViewModalController();
    deleteModalController = createDeleteModalController();
    setupTableActions();
    loadAdminUsers();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAdminAccountPage);
} else {
    initAdminAccountPage();
}
