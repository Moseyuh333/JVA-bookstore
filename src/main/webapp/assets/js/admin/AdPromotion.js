// ========== ADPROMOTION.JS ==========
// Quản lý chương trình khuyến mãi

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("promotionTable");
    const searchInput = document.getElementById("promotionSearchInput");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let promotions = [];
    let filteredPromotions = [];

    // API functions
    const api = {
        getPromotions: () => {
            const token = localStorage.getItem("admin_token");
            return fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=list`, {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            }).then(r => r.json());
        }
    };

    // Utility functions
    const escapeHtml = (text) => {
        if (!text) return "";
        return text.replace(/[&<>"']/g, (m) => {
            const map = { '&': '&amp;', '<': '<', '>': '>', '"': '"', "'": '&#39;' };
            return map[m];
        });
    };

    const formatDateRange = (startDate, endDate) => {
        if (!startDate || !endDate) return "-";
        const start = new Date(startDate).toLocaleDateString('vi-VN');
        const end = new Date(endDate).toLocaleDateString('vi-VN');
        return `${start} - ${end}`;
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

    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (list.length === 0) {
            showEmpty();
            return;
        }

        hideEmpty();
        list.forEach(p => {
            const discount = p.discount_value ? p.discount_value + (p.discount_type === 'percentage' ? '%' : '₫') : "-";
            const valid = formatDateRange(p.start_date, p.end_date);
            const type = p.type || p.promotion_type || "-";

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${escapeHtml(p.id || p.promotion_id || '-')}</td>
                <td>${escapeHtml(p.name || p.title || '-')}</td>
                <td>${escapeHtml(type)}</td>
                <td>${discount}</td>
                <td>${valid}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    };

    // Load promotions from API
    const loadPromotions = async () => {
        try {
            showLoading();
            const response = await api.getPromotions();

            if (response.promotions) {
                promotions = response.promotions;
                filteredPromotions = [...promotions];
                renderTable(filteredPromotions);
            } else {
                console.error("Invalid response format:", response);
                showEmpty();
            }
        } catch (error) {
            console.error("Error loading promotions:", error);
            showEmpty();
        } finally {
            hideLoading();
        }
    };

    // Search filter
    const applyFilters = () => {
        const keyword = searchInput.value.toLowerCase().trim();
        filteredPromotions = promotions.filter(p =>
            (p.name || p.title || '').toLowerCase().includes(keyword) ||
            (p.type || p.promotion_type || '').toLowerCase().includes(keyword) ||
            (p.discount_value || '').toString().toLowerCase().includes(keyword)
        );
        renderTable(filteredPromotions);
    };

    const resetFilters = () => {
        if (searchInput) searchInput.value = '';
        filteredPromotions = [...promotions];
        renderTable(filteredPromotions);
    };

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    // Init
    loadPromotions();
});
