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

// ===== Modal CRUD for Shipper =====
// Helper to show/hide modal
function showElement(el) { if (el) el.style.display = 'block'; }
function hideElement(el) { if (el) el.style.display = 'none'; }

const shipperOverlay = document.getElementById('shipperModalOverlay');
const shipperBox = document.getElementById('shipperModalBox');
const shipperForm = document.getElementById('shipperForm');
const shipperTitle = document.getElementById('shipperModalTitle');
const shipperIdInput = document.getElementById('shipperId');

// Open Add Modal
function openAddShipper() {
    shipperForm.reset();
    shipperIdInput.value = '';
    shipperTitle.textContent = 'Thêm nhà vận chuyển';
    hideElement(document.getElementById('shipperFeedback'));
    showElement(shipperOverlay);
    showElement(shipperBox);
}

// Open Edit Modal
async function openEditShipper(id) {
    try {
        const token = localStorage.getItem('admin_token');
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/shippers?action=get&id=${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        if (data.error) { alert(data.error); return; }
        // populate form
        shipperIdInput.value = data.id || '';
        document.getElementById('shipperName').value = data.name || '';
        document.getElementById('shipperPhone').value = data.phone || '';
        document.getElementById('shipperEmail').value = data.email || '';
        document.getElementById('shipperBaseFee').value = data.base_fee || '';
        document.getElementById('shipperServiceArea').value = data.service_area || '';
        document.getElementById('shipperEstimatedTime').value = data.estimated_time || '';
        document.getElementById('shipperStatus').value = data.status || 'active';

        shipperTitle.textContent = 'Chỉnh sửa nhà vận chuyển';
        hideElement(document.getElementById('shipperFeedback'));
        showElement(shipperOverlay);
        showElement(shipperBox);
    } catch (err) {
        console.error('Error fetching shipper', err);
        alert('Lỗi khi lấy dữ liệu nhà vận chuyển');
    }
}

// Close handlers
document.getElementById('shipperModalClose')?.addEventListener('click', () => { hideElement(shipperOverlay); hideElement(shipperBox); });
document.getElementById('shipperCancel')?.addEventListener('click', () => { hideElement(shipperOverlay); hideElement(shipperBox); });

// Form submit (create/update)
shipperForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = shipperIdInput.value;
    const formData = new FormData(shipperForm);
    const params = new URLSearchParams();
    for (const [k, v] of formData.entries()) params.append(k, v);
    const token = localStorage.getItem('admin_token');

    try {
        const action = id ? 'update' : 'create';
        if (id) params.append('id', id);
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/shippers?action=${action}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        });
        const data = await res.json();
        if (data.error) {
            const fb = document.getElementById('shipperFeedback'); fb.textContent = data.error; fb.style.display = 'block'; return;
        }
        // success
        hideElement(shipperOverlay); hideElement(shipperBox);
        loadShippers();
        alert(data.message || 'Thao tác thành công');
    } catch (err) {
        console.error('Error saving shipper', err);
        alert('Lỗi khi lưu nhà vận chuyển');
    }
});

// Hook add button
document.getElementById('openCreateShipperBtn')?.addEventListener('click', openAddShipper);

// Delegate edit/delete buttons in table
document.querySelector('#ShipperTable')?.addEventListener('click', (e) => {
    const editBtn = e.target.closest('.btn-edit');
    if (editBtn && editBtn.dataset.id) {
        openEditShipper(editBtn.dataset.id);
        return;
    }
    const deleteBtn = e.target.closest('.btn-delete');
    if (deleteBtn && deleteBtn.dataset.id) {
        openDeleteShipper(deleteBtn.dataset.id);
        return;
    }
});

// ===== Delete flow =====
const deleteOverlay = document.getElementById('shipperDeleteOverlay');
const deleteBox = document.getElementById('shipperDeleteBox');
let deletingId = null;

function openDeleteShipper(id) {
    deletingId = id;
    showElement(deleteOverlay); showElement(deleteBox);
}

document.getElementById('shipperDeleteCancel')?.addEventListener('click', () => { hideElement(deleteOverlay); hideElement(deleteBox); deletingId = null; });
document.getElementById('shipperDeleteClose')?.addEventListener('click', () => { hideElement(deleteOverlay); hideElement(deleteBox); deletingId = null; });

document.getElementById('shipperDeleteConfirm')?.addEventListener('click', async () => {
    if (!deletingId) return;
    const token = localStorage.getItem('admin_token');
    try {
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/shippers?action=delete&id=${deletingId}`, {
            method: 'POST', headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        if (data.error) { document.getElementById('shipperDeleteFeedback').textContent = data.error; document.getElementById('shipperDeleteFeedback').style.display = 'block'; return; }
        hideElement(deleteOverlay); hideElement(deleteBox); deletingId = null; loadShippers(); alert(data.message || 'Đã xóa');
    } catch (err) {
        console.error('Error deleting shipper', err);
        alert('Lỗi khi xóa nhà vận chuyển');
    }
});

