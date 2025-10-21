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
        getPromotions: (search = '', searchType = 'all') => {
            const token = localStorage.getItem("admin_token");
            if (!token) {
                alert("Phiên đăng nhập quản trị đã hết hạn. Vui lòng đăng nhập lại.");
                window.location.href = (window.appConfig?.contextPath || '') + "/login.jsp";
                return Promise.reject(new Error("No token"));
            }
            const params = new URLSearchParams({
                action: 'list'
            });
            if (search.trim()) {
                params.append('search', search.trim());
                params.append('searchType', searchType);
            }
            return fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?${params.toString()}`, {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            }).then(r => r.json());
        },
        createPromotion: (data) => fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=create`, {
            method: 'POST',
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("admin_token")}`,
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams(data)
        }).then(r => r.json()),
        updatePromotion: (id, data) => fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=update&id=${id}`, {
            method: 'POST',
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("admin_token")}`,
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams(data)
        }).then(r => r.json()),
        deletePromotion: (id) => fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=delete&id=${id}`, {
            method: 'POST',
            headers: {
                "Authorization": `Bearer ${localStorage.getItem("admin_token")}`,
                "Content-Type": "application/json"
            }
        }).then(r => r.json())
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
            const vnd = n => Number(n).toLocaleString('vi-VN');
            const type = p.type || "-";             // percent / amount
            const scope = p.scope || "-";           // product / shipping

            // Xác định nhãn loại khuyến mãi (hiển thị dễ hiểu hơn)
            const typeLabel =
                scope === "shipping" ? "Giảm phí vận chuyển" :
                scope === "product"  ? "Giảm giá sản phẩm" :
                "-";

            // Xử lý giá trị giảm
            let discount = "-";
            if (type === "percent")
                discount = `${p.discount_value}%`;
            else if (type === "amount")
                discount = `${vnd(p.discount_value)}đ`;

            if (p.max_discount_value && p.max_discount_value > 0)
                discount += ` (tối đa ${vnd(p.max_discount_value)}đ)`;
            if (p.min_order_value && p.min_order_value > 0)
                discount += `, đơn ≥ ${vnd(p.min_order_value)}đ`;

            const valid = formatDateRange(p.start_at, p.end_at);
            const code = p.code || "-";
            const description = p.description || "-";
            const active = p.active
                ? '<span class="badge badge-success">Active</span>'
                : '<span class="badge badge-secondary">Inactive</span>';

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${escapeHtml(p.id.toString())}</td>
                <td>${escapeHtml(code)}</td>
                <td>${escapeHtml(description)}</td>
                <td>${escapeHtml(typeLabel)}</td>
                <td>${discount}</td>
                <td>${valid}</td>
                <td>${active}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1 edit-btn" data-id="${p.id}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger delete-btn" data-id="${p.id}">
                        <i class="fas fa-trash"></i>
                    </button>
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

                // Update stats
                const totalPromoEl = document.getElementById("totalPromo");
                const activePromoEl = document.getElementById("activePromo");
                if (totalPromoEl) totalPromoEl.textContent = response.total || 0;
                if (activePromoEl) activePromoEl.textContent = response.active || 0;
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
    const applyFilters = async () => {
        const keyword = searchInput.value.toLowerCase().trim();
        const searchType = document.getElementById("searchType")?.value || 'all';
        if (keyword) {
            // Server-side search
            try {
                showLoading();
                const response = await api.getPromotions(keyword, searchType);
                if (response.promotions) {
                    filteredPromotions = response.promotions;
                    renderTable(filteredPromotions);

                    // Update stats for filtered results
                    const totalPromoEl = document.getElementById("totalPromo");
                    const activePromoEl = document.getElementById("activePromo");
                    if (totalPromoEl) totalPromoEl.textContent = response.total || 0;
                    if (activePromoEl) activePromoEl.textContent = response.active || 0;
                } else {
                    console.error("Invalid response format:", response);
                    showEmpty();
                }
            } catch (error) {
                console.error("Error searching promotions:", error);
                showEmpty();
            } finally {
                hideLoading();
            }
        } else {
            // No search, load all
            filteredPromotions = [...promotions];
            renderTable(filteredPromotions);
        }
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
