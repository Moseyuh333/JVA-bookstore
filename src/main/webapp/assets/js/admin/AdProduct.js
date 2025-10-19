// ========== ADPRODUCT.JS ==========
// Quản lý danh sách sản phẩm (phân trang + tìm kiếm toàn DB)

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("product");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");
    const totalEl = document.getElementById("totalProducts");
    const inStockEl = document.getElementById("inStock");
    const outOfStockEl = document.getElementById("outOfStock");
    const paginationEl = document.getElementById("pagination");
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");

    let products = [];
    let currentPage = 1;
    const limit = 20;
    let currentSearch = "";

    // ===== Utility =====
    const escapeHtml = (text) => {
        if (text === null || text === undefined) return "";
        return String(text).replace(/[&<>"']/g, (m) => {
            const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
            return map[m];
        });
    };

    const showLoading = () => {
        loadingState?.classList.remove("hidden");
        tableContainer?.classList.add("hidden");
        emptyState?.classList.add("hidden");
    };

    const hideLoading = () => {
        loadingState?.classList.add("hidden");
        tableContainer?.classList.remove("hidden");
    };

    const showEmpty = () => {
        emptyState?.classList.remove("hidden");
        tableContainer?.classList.add("hidden");
    };

    const hideEmpty = () => {
        emptyState?.classList.add("hidden");
        tableContainer?.classList.remove("hidden");
    };

    // ===== Render Table =====
    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (!list || list.length === 0) {
            showEmpty();
            return;
        }
        hideEmpty();
        list.forEach((p) => {
            const price = p.price
                ? new Intl.NumberFormat("vi-VN").format(p.price) + "₫"
                : "-";
            const stock = p.stock_quantity ?? p.stock ?? "-";
            const shop = p.shop_name || "-";
            const commission = p.commission_rate ? p.commission_rate + "%" : "-";

            const tr = document.createElement("tr");
            tr.innerHTML = `
        <td>${escapeHtml(p.id || "-")}</td>
        <td>${escapeHtml(p.title || "-")}</td>
        <td>${escapeHtml(p.author || "-")}</td>
        <td>${escapeHtml(p.category || "-")}</td>
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

    // ===== Update Stats =====
    const updateStats = (data) => {
        if (totalEl) totalEl.textContent = data.total || 0;
        const inStock = data.products.filter((p) => p.stock_quantity > 0).length;
        const outOfStock = data.products.filter(
            (p) => !p.stock_quantity || p.stock_quantity <= 0
        ).length;
        if (inStockEl) inStockEl.textContent = inStock;
        if (outOfStockEl) outOfStockEl.textContent = outOfStock;
    };

    // ===== Pagination =====
    const updatePagination = (total, page, limit) => {
        const totalPages = Math.ceil(total / limit);
        paginationEl.innerHTML = `
      <button class="btn btn-light btn-sm" ${page <= 1 ? "disabled" : ""} onclick="changePage(${page - 1})">← Trước</button>
      <span class="mx-2">Trang ${page} / ${totalPages}</span>
      <button class="btn btn-light btn-sm" ${page >= totalPages ? "disabled" : ""} onclick="changePage(${page + 1})">Sau →</button>
    `;
    };

    window.changePage = (page) => {
        currentPage = page;
        loadProducts(currentPage, currentSearch);
    };

    // ===== Load Products =====
    const loadProducts = async (page = 1, search = "") => {
        try {
            showLoading();
            const token = localStorage.getItem("admin_token"); // ✅ lấy token từ localStorage

            const res = await fetch(
                `/api/admin/products?action=list&page=${page}&limit=${limit}&search=${encodeURIComponent(search)}`,
                {
                    headers: {
                        "Authorization": `Bearer ${token}`, // ✅ gửi token kèm request
                        "Content-Type": "application/json"
                    }
                }
            );

            if (res.status === 401) {
                console.error("❌ Unauthorized — token hết hạn hoặc chưa đăng nhập");
                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                window.location.href = "/login.jsp"; // hoặc trang login tương ứng
                return;
            }

            const data = await res.json();
            if (!data.products || data.products.length === 0) return showEmpty();

            products = data.products;
            renderTable(products);
            updateStats(data);
            updatePagination(data.total, data.page, data.limit);
        } catch (err) {
            console.error("Error loading:", err);
            showEmpty();
        } finally {
            hideLoading();
        }
    };

    const loadStats = async () => {
        try {
            const res = await fetch(`/api/admin/products?action=stats`);
            const data = await res.json();
            document.getElementById("totalProducts").textContent = data.total || 0;
            document.getElementById("inStock").textContent = data.in_stock || 0;
            document.getElementById("outOfStock").textContent = data.out_stock || 0;
        } catch (err) {
            console.error("Error loading stats:", err);
        }
    };


    // ===== Search toàn DB =====
    if (searchBtn && searchInput) {
        searchBtn.addEventListener("click", () => {
            currentSearch = searchInput.value.trim();
            currentPage = 1;
            loadProducts(currentPage, currentSearch);
        });

        // Enter = search
        searchInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                currentSearch = searchInput.value.trim();
                currentPage = 1;
                loadProducts(currentPage, currentSearch);
            }
        });
    }

    // ===== Init =====
    loadProducts();
    loadStats();
});
