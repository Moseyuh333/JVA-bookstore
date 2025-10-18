// ========== ADACCOUNT.JS ==========
// Quản lý tài khoản người dùng

// Áp dụng bộ lọc tìm kiếm
function applyFilters() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase().trim();
    const rows = document.querySelectorAll('#User tr');
    let visibleCount = 0;

    rows.forEach(row => {
        const text = row.textContent.toLowerCase();
        if (!searchTerm || text.includes(searchTerm)) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });

    const table = document.querySelector('.table-custom');
    const emptyState = document.getElementById('emptyState');

    if (visibleCount === 0) {
        table.style.display = 'none';
        emptyState.style.display = 'block';
    } else {
        table.style.display = 'table';
        emptyState.style.display = 'none';
    }
}

// Reset bộ lọc
function resetFilters() {
    document.getElementById('searchInput').value = '';
    document.querySelectorAll('#User tr').forEach(row => row.style.display = '');
    document.querySelector('.table-custom').style.display = 'table';
    document.getElementById('emptyState').style.display = 'none';
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

// Khởi tạo khi load xong trang
window.addEventListener('load', () => {
    if (typeof feather !== "undefined") feather.replace();
    updateStats();
});
