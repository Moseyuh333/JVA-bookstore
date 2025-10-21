// ========== ADCOMMISSION.JS ==========
// Quản lý chiết khấu (commission) của app cho từng shop

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("commissionTable");
    const searchInput = document.getElementById("commissionSearchInput");
    const searchTypeSelect = document.getElementById("commissionSearchType");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let currentSearchType = "all";

    let commissions = [];
    let filteredCommissions = [];

    // API functions
    const api = {
        getCommissions: (search, searchType) => {
            const token = localStorage.getItem("admin_token");
            let url = `${window.appConfig?.contextPath || ''}/api/admin/commissions?action=list`;
            if (search && search.trim()) {
                url += `&search=${encodeURIComponent(search.trim())}&searchType=${encodeURIComponent(searchType)}`;
            }
            return fetch(url, {
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
            const map = { '&': '&amp;', '<': '<', '>': '>', '"': '"', "'": '&#39;' };
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
            const createdAt = formatDate(c.created_at);
            const updatedAt = formatDate(c.updated_at);
            const type = c.type || "-";
            const minRevenue = c.min_revenue ? Number(c.min_revenue).toLocaleString('vi-VN') + "₫" : "-";
            const maxRevenue = c.max_revenue ? Number(c.max_revenue).toLocaleString('vi-VN') + "₫" : "∞";
            const status = c.status === 'active' ? '<span class="badge badge-success">Active</span>' : '<span class="badge badge-secondary">Inactive</span>';

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${escapeHtml(c.id.toString())}</td>
                <td>${escapeHtml(c.name)}</td>
                <td>${escapeHtml(type)}</td>
                <td>${minRevenue}</td>
                <td>${maxRevenue}</td>
                <td>${rate}</td>
                <td>${status}</td>
                <td>${createdAt}</td>
                <td>${updatedAt}</td>
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
            const search = searchInput.value.trim();
            const searchType = currentSearchType;
            const response = await api.getCommissions(search, searchType);

            if (response.commissions) {
                commissions = response.commissions;
                filteredCommissions = [...commissions];
                renderTable(filteredCommissions);

                // Update stats
                const totalCommissionEl = document.getElementById("totalCommission");
                const activeCommissionEl = document.getElementById("activeCommission");
                const averageRateEl = document.getElementById("averageRate");
                if (totalCommissionEl) totalCommissionEl.textContent = response.total || 0;
                if (activeCommissionEl) activeCommissionEl.textContent = response.active || 0;
                if (averageRateEl) averageRateEl.textContent = (response.average_rate || 0).toFixed(2) + "%";
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
        const searchType = currentSearchType;

        if (searchType === "all") {
            filteredCommissions = commissions.filter(c =>
                (c.name || '').toLowerCase().includes(keyword) ||
                (c.type || '').toLowerCase().includes(keyword) ||
                (c.rate || '').toString().toLowerCase().includes(keyword)
            );
        } else if (searchType === "name") {
            filteredCommissions = commissions.filter(c =>
                (c.name || '').toLowerCase().includes(keyword)
            );
        } else if (searchType === "type") {
            filteredCommissions = commissions.filter(c =>
                (c.type || '').toLowerCase().includes(keyword)
            );
        } else if (searchType === "rate") {
            filteredCommissions = commissions.filter(c =>
                (c.rate || '').toString().toLowerCase().includes(keyword)
            );
        } else {
            filteredCommissions = [...commissions];
        }
        renderTable(filteredCommissions);
    };

    const resetFilters = () => {
        if (searchInput) searchInput.value = '';
        if (searchTypeSelect) searchTypeSelect.value = 'all';
        currentSearchType = "all";
        filteredCommissions = [...commissions];
        renderTable(filteredCommissions);
    };

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener("input", loadCommissions);
    }
    if (searchTypeSelect) {
        searchTypeSelect.addEventListener("change", (e) => {
            currentSearchType = e.target.value;
            loadCommissions();
        });
    }

    // Init
    loadCommissions();
});
