// ========== ADCOMMISSION.JS ==========
// Quản lý chiết khấu (commission) của app cho từng shop

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("commissionTable");
    const searchInput = document.getElementById("commissionSearchInput");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let commissions = [];
    let filteredCommissions = [];

    // API functions
    const api = {
        getCommissions: () => {
            const token = localStorage.getItem("admin_token");
            return fetch(`${window.appConfig?.contextPath || ''}/api/admin/commissions?action=list`, {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            }).then(r => r.json());
        }
    };

    // Utility functions
    const escapeHtml = (text) => {
        if (text === null || text === undefined) return "";
        return String(text).replace(/[&<>"']/g, (m) => {
            const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
            return map[m];
        });
    };


    const formatDate = (dateStr) => {
        if (!dateStr) return "-";
        const date = new Date(dateStr);
        return date.toLocaleDateString('vi-VN');
    };

    const showLoading = () => {
        if (loadingState) loadingState.style.display = 'block';
        if (tableContainer) tableContainer.style.display = 'none';
        if (emptyState) emptyState.style.display = 'none';
    };

    const hideLoading = () => {
        if (loadingState) loadingState.style.display = 'none';
        if (tableContainer) tableContainer.style.display = 'block';
    };

    const showEmpty = () => {
        if (emptyState) emptyState.style.display = 'block';
        if (tableContainer) tableContainer.style.display = 'none';
    };

    const hideEmpty = () => {
        if (emptyState) emptyState.style.display = 'none';
        if (tableContainer) tableContainer.style.display = 'block';
    };

    // Render bảng
    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (list.length === 0) {
            showEmpty();
            return;
        }

        hideEmpty();
        list.forEach(c => {
            const rate = c.rate ? c.rate + "%" : "-";
            const since = formatDate(c.since || c.created_at);
            const note = c.note || c.description || "-";

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${escapeHtml(c.id || c.commission_id || '-')}</td>
                <td>${escapeHtml(c.shop || c.shop_name || '-')}</td>
                <td>${rate}</td>
                <td>${since}</td>
                <td>${escapeHtml(note)}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    };

    // Load commissions from API
    const loadCommissions = async () => {
        try {
            showLoading();
            const response = await api.getCommissions();

            if (response.commissions) {
                commissions = response.commissions;
                filteredCommissions = [...commissions];
                renderTable(filteredCommissions);
            } else {
                console.error("Invalid response format:", response);
                showEmpty();
            }
        } catch (error) {
            console.error("Error loading commissions:", error);
            showEmpty();
        } finally {
            hideLoading();
        }
    };

    // Tìm kiếm chiết khấu
    const applyFilters = () => {
        const keyword = searchInput.value.toLowerCase().trim();
        filteredCommissions = commissions.filter(c =>
            (c.shop || c.shop_name || '').toLowerCase().includes(keyword) ||
            (c.rate || '').toString().toLowerCase().includes(keyword) ||
            (c.note || c.description || '').toLowerCase().includes(keyword)
        );
        renderTable(filteredCommissions);
    };

    const resetFilters = () => {
        if (searchInput) searchInput.value = '';
        filteredCommissions = [...commissions];
        renderTable(filteredCommissions);
    };

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    // Init
    loadCommissions();
});
