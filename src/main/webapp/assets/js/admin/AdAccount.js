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
