// ========== ADPRODUCT.JS ==========
// Quản lý danh sách sản phẩm

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const searchInput = document.getElementById("productSearchInput");
    const tableBody = document.getElementById("product");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let products = [];
    let filteredProducts = [];

    // API functions
    const api = {
        getProducts: () => {
            const token = localStorage.getItem("admin_token");
            return fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=list`, {
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

    // Render table
    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (list.length === 0) {
            showEmpty();
            return;
        }
        hideEmpty();
        list.forEach(p => {
            const price = p.price ? new Intl.NumberFormat('vi-VN').format(p.price) + "₫" : "-";            const stock = p.stock_quantity ?? p.stock ?? "-";
            const shop = p.shop_name || "-";
            const commission = p.commission_rate ? p.commission_rate + "%" : "-";
            const tr = document.createElement("tr");
            tr.innerHTML = `
      <td>${escapeHtml(p.id || '-')}</td>
      <td>${escapeHtml(p.title || '-')}</td>
      <td>${escapeHtml(p.author || '-')}</td>
      <td>${escapeHtml(p.category || '-')}</td>
      <td>${price}</td>
      <td>${stock}</td>
      <td>${escapeHtml(shop)}</td>
      <td>${commission}</td>
      <td>
        <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
        <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
      </td>`;
            tableBody.appendChild(tr);
        });
    };


    // Load products from API
    const loadProducts = async () => {
        try {
            showLoading();
            const response = await api.getProducts();

            if (response.products) {
                products = response.products;
                filteredProducts = [...products];
                renderTable(filteredProducts);
            } else {
                console.error("Invalid response format:", response);
                showEmpty();
            }
        } catch (error) {
            console.error("Error loading products:", error);
            showEmpty();
        } finally {
            hideLoading();
        }
    };

    // Search filter
    const applyFilters = () => {
        const keyword = searchInput.value.toLowerCase().trim();
        filteredProducts = products.filter(p =>
            (p.name || p.title || '').toLowerCase().includes(keyword) ||
            (p.author || '').toLowerCase().includes(keyword) ||
            (p.category_name || p.category || '').toLowerCase().includes(keyword)
            (p.shop_name || '').toLowerCase().includes(keyword)
        );
        renderTable(filteredProducts);
    };

    const resetFilters = () => {
        if (searchInput) searchInput.value = '';
        filteredProducts = [...products];
        renderTable(filteredProducts);
    };

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    // Init
    loadProducts();
});
