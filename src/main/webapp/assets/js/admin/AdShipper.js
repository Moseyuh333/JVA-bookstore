// ========== ADSHIPPER.JS ==========
// Quản lý danh sách nhà vận chuyển

// ========================
// 📥 LOAD SHIPPERS FROM API
// ========================
async function loadShippers(search = "", searchType = "all") {
    const tbody = document.querySelector('#ShipperTable');
    const empty = document.querySelector('#emptyState');
    const loading = document.querySelector('#loadingState');

    const token = localStorage.getItem("admin_token");
    if (!token) {
        window.location.href = `${window.appConfig?.contextPath || ''}/login.jsp`;
        return;
    }

    try {
        loading.style.display = 'block';
        empty.style.display = 'none';
        tbody.innerHTML = '';

        let url = `${window.appConfig?.contextPath || ''}/api/admin/shippers?action=list`;
        if (search && search.trim()) {
            url += `&search=${encodeURIComponent(search)}&searchType=${encodeURIComponent(searchType)}`;
        }

        const res = await fetch(url, {
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!res.ok) throw new Error("Server trả lỗi: " + res.status);
        const data = await res.json();

        // Không có dữ liệu
        if (!data.shippers || data.shippers.length === 0) {
            empty.style.display = 'block';
            updateStats(0);
            return;
        }

        // Render bảng
        data.shippers.forEach(s => {
            const baseFee = s.base_fee ? Number(s.base_fee).toLocaleString('vi-VN') + "₫" : "-";
            const created = s.created_at ? new Date(s.created_at).toLocaleDateString('vi-VN') : "-";

            tbody.innerHTML += `
                <tr>
                    <td>${s.name || '-'}</td>
                    <td>${s.phone || '-'}</td>
                    <td>${s.email || '-'}</td>
                    <td>${s.service_area || '-'}</td>
                    <td>${baseFee}</td>
                    <td>${s.estimated_time || '-'}</td>
                    <td>
                        <span class="badge ${s.status === 'active' ? 'badge-success' : 'badge-secondary'}">
                            ${s.status}
                        </span>
                    </td>
                    <td>${created}</td>
                    <td class="actions">
                        <button class="btn-icon btn-edit" title="Sửa" data-id="${s.id}">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn-icon btn-delete" title="Xóa" data-id="${s.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>`;
        });

        updateStats(data.shippers.length);

    } catch (err) {
        console.error("❌ Lỗi khi tải dữ liệu:", err);
        empty.style.display = 'block';
        updateStats(0);
    } finally {
        loading.style.display = 'none';
    }
}

// ========================
// 📊 CẬP NHẬT THỐNG KÊ
// ========================
function updateStats(total) {
    const totalEl = document.getElementById('totalShippers');
    if (totalEl) totalEl.textContent = total || 0;

    const activeEl = document.getElementById('activeShippers');
    if (activeEl) {
        const activeCount = document.querySelectorAll('#ShipperTable tr .badge-success').length;
        activeEl.textContent = activeCount;
    }
}

// ========================
// 🧹 RESET / SEARCH
// ========================
function applyFilter() {
    const search = document.getElementById('searchInput').value.trim();
    const searchType = document.getElementById('searchType') ? document.getElementById('searchType').value : "all";
    loadShippers(search, searchType);
}

function resetFilter() {
    document.getElementById('searchInput').value = '';
    if (document.getElementById('searchType')) document.getElementById('searchType').value = 'all';
    loadShippers();
}

// ========================
// 🚀 KHỞI TẠO KHI LOAD TRANG
// ========================
window.addEventListener('load', () => {
    if (typeof feather !== "undefined") feather.replace();
    loadShippers();

    document.getElementById('searchInput')?.addEventListener('input', e => {
        if (e.target.value.length === 0 || e.target.value.length >= 2) applyFilter();
    });

    document.getElementById('btnReset')?.addEventListener('click', resetFilter);
});
