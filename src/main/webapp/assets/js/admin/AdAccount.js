// ========== ADACCOUNT.JS ==========
// Quản lý tài khoản người dùng trong bảng điều khiển admin.

const contextPath = window.appConfig?.contextPath || '';
let cachedUsers = [];

/**
 * Tải danh sách người dùng từ API quản trị.
 */
async function loadAdminUsers(searchTerm = '') {
    const tableBody = document.querySelector('#User');
    const loading = document.querySelector('#loadingState');
    const emptyState = document.querySelector('#emptyState');

    if (!tableBody) {
        console.warn('Không tìm thấy phần tử bảng người dùng.');
        return;
    }

    try {
        if (loading) loading.style.display = 'block';
        tableBody.innerHTML = '';
        if (emptyState) emptyState.style.display = 'none';

        const token = localStorage.getItem('admin_token');
        if (!token) {
            cachedUsers = [];
            if (emptyState) emptyState.style.display = 'block';
            updateStats();
            return;
        }

        let url = `${contextPath}/api/admin/users?action=list`;
        if (searchTerm) {
            url += `&search=${encodeURIComponent(searchTerm)}`;
        }

        const response = await fetch(url, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        });

        let payload;
        try {
            payload = await response.json();
        } catch (jsonError) {
            throw new Error('Không thể đọc dữ liệu người dùng từ máy chủ');
        }

        if (!response.ok) {
            throw new Error(payload?.error || `Server trả lỗi: ${response.status}`);
        }

        if (payload?.error) {
            throw new Error(payload.error);
        }

        cachedUsers = Array.isArray(payload?.users) ? payload.users : [];

        if (cachedUsers.length === 0) {
            if (emptyState) emptyState.style.display = 'block';
            updateStats();
            return;
        }

        const rowsHtml = cachedUsers.map(user => {
            const initials = (user.username?.substring(0, 2) || 'U').toUpperCase();
            const fullName = user.full_name || '-';
            const birthDate = user.birth_date || '-';
            const gender = '-';
            const address = user.address || '-';
            const email = user.email || '-';
            const phone = user.phone || '-';
            const role = user.role || 'customer';
            const normalizedRole = role.toLowerCase();
            const roleBadgeClass = normalizedRole === 'admin' ? 'badge-admin' : 'badge-customer';

            return `
                <tr>
                    <td>
                        <div class="user-cell">
                            <div class="avatar">${initials}</div>
                            <div class="user-info-text">
                                <div class="user-name">${user.username}</div>
                            </div>
                        </div>
                    </td>
                    <td>${fullName}</td>
                    <td>${birthDate}</td>
                    <td>${gender}</td>
                    <td>${address}</td>
                    <td>${email}</td>
                    <td>${phone}</td>
                    <td><span class="badge-custom ${roleBadgeClass}">${role}</span></td>
                    <td class="actions">
                        <button class="btn-icon btn-view" title="Xem">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="btn-icon btn-delete" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>`;
        }).join('');

        tableBody.innerHTML = rowsHtml;
        if (emptyState) emptyState.style.display = 'none';
        updateStats();
    } catch (error) {
        console.error('❌ Lỗi khi tải dữ liệu:', error);
        cachedUsers = [];
        if (emptyState) emptyState.style.display = 'block';
        updateStats();
    } finally {
        if (loading) loading.style.display = 'none';
    }
}

/**
 * Áp dụng bộ lọc tìm kiếm từ input.
 */
async function applyFilters() {
    const input = document.getElementById('searchInput');
    const searchTerm = input ? input.value.trim() : '';
    await loadAdminUsers(searchTerm);
}

/**
 * Đặt lại bộ lọc tìm kiếm.
 */
function resetFilters() {
    const input = document.getElementById('searchInput');
    if (input) input.value = '';
    loadAdminUsers();
}

/**
 * Cập nhật số liệu thống kê hiển thị trên dashboard nhỏ.
 */
function updateStats() {
    const totalEl = document.getElementById('totalUsers');
    const activeEl = document.getElementById('activeUsers');

    const total = cachedUsers.length;
    const active = cachedUsers.filter(user => (user.status || '').toLowerCase() === 'active').length;

    if (totalEl) totalEl.textContent = total;
    if (activeEl) activeEl.textContent = active;
}

// Tự động tìm kiếm khi người dùng nhập.
document.getElementById('searchInput')?.addEventListener('input', event => {
    const value = event.target.value.trim();
    if (value.length === 0 || value.length >= 2) {
        applyFilters();
    }
});

/**
 * Thiết lập modal tạo tài khoản mới cho admin.
 */
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
        if (!feedback) return;
        feedback.textContent = '';
        feedback.className = 'form-feedback';
    };

    const setFeedback = (message, type = 'error') => {
        if (!feedback) return;
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
        usernameInput?.focus({ preventScroll: true });
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
        if (fullName) payload.append('full_name', fullName);
        if (phone) payload.append('phone', phone);
        if (role) payload.append('role', role);
        if (status) payload.append('status', status);

        const originalText = submitBtn.textContent;
        submitBtn.disabled = true;
        submitBtn.textContent = 'Đang tạo...';

        try {
            const response = await fetch(`${contextPath}/api/admin/users`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: payload.toString()
            });

            let data = null;
            try {
                data = await response.json();
            } catch (jsonError) {
                // Ignore JSON parse failure; response may not contain body
            }

            if (!response.ok || (data && data.error)) {
                const message = data?.error || `Không thể tạo tài khoản (mã ${response.status})`;
                throw new Error(message);
            }

            setFeedback('Tạo tài khoản thành công.', 'success');
            await loadAdminUsers();

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

// ========================
// 🚀 Khởi tạo khi trang load
// ========================
window.addEventListener('load', () => {
    if (typeof feather !== 'undefined') {
        feather.replace();
    }

    setupCreateUserModal();
    loadAdminUsers();
});
// ========== ADACCOUNT.JS ==========
// Quản lý tài khoản người dùng

// ========================
// 📥 LOAD USERS FROM API
// ========================
async function loadAdminUsers(searchTerm = '') {
    const tableBody = document.querySelector('#User');
    const loading = document.querySelector('#loadingState');
    const emptyState = document.querySelector('#emptyState');

    try {
        if (loading) loading.style.display = 'block';
        if (tableBody) tableBody.innerHTML = '';

        const token = localStorage.getItem("admin_token");
        let url = `${window.appConfig?.contextPath || ''}/api/admin/users?action=list`;
        if (searchTerm) {
            url += `&search=${encodeURIComponent(searchTerm)}`;
        }

        const res = await fetch(url, {
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });
        if (!res.ok) throw new Error("Server trả lỗi: " + res.status);
        const data = await res.json();

        if (data.users.length === 0) {
            emptyState.style.display = 'block';
            return;
        }

        // Ẩn trạng thái trống
        emptyState.style.display = 'none';

        data.users.forEach(u => {
            const initials = (u.username?.substring(0, 2) || 'U').toUpperCase();
            const fullName = u.full_name || '-';
            const birthDate = u.birth_date || '-';
            const gender = '-'; // Not available in DB
            const address = u.address || '-';
            const email = u.email || '-';
            const phone = u.phone || '-';
            const role = u.role || 'customer';
            const roleBadgeClass = role === 'admin' ? 'badge-admin' : 'badge-customer';

            tableBody.innerHTML += `
                <tr>
                    <td>
                        <div class="user-cell">
                            <div class="avatar">${initials}</div>
                            <div class="user-info-text">
                                <div class="user-name">${u.username}</div>
                            </div>
                        </div>
                    </td>
                    <td>${fullName}</td>
                    <td>${birthDate}</td>
                    <td>${gender}</td>
                    <td>${address}</td>
                    <td>${email}</td>
                    <td>${phone}</td>
                    <td><span class="badge-custom ${roleBadgeClass}">${role}</span></td>
                    <td class="actions">
                        <button class="btn-icon btn-view" title="Xem">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="btn-icon btn-delete" title="Xóa">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>`;
        });

        updateStats();
    } catch (err) {
        console.error("❌ Lỗi khi tải dữ liệu:", err);
        emptyState.style.display = 'block';
    } finally {
        if (loading) loading.style.display = 'none';
    }
}


// Áp dụng bộ lọc tìm kiếm (server-side)
async function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.trim();
    await loadAdminUsers(searchTerm);
}

// Reset bộ lọc
function resetFilters() {
    document.getElementById('searchInput').value = '';
    loadAdminUsers();
}

// Cập nhật thống kê tài khoản
function updateStats() {
    const rows = document.querySelectorAll('#User tr');
    document.getElementById('totalUsers').textContent = rows.length;
    document.getElementById('activeUsers').textContent = rows.length;
}

// Auto-search khi nhập
document.getElementById('searchInput')?.addEventListener('input', e => {
    if (e.target.value.length === 0 || e.target.value.length >= 2) applyFilters();
});

// ========================
// 🚀 Khởi tạo khi trang load
// ========================
window.addEventListener('load', () => {
    if (typeof feather !== "undefined") feather.replace();

    // Gọi API lấy dữ liệu user thật từ servlet
    loadAdminUsers();

    // Sau khi load xong thì cập nhật thống kê
    updateStats();
});
