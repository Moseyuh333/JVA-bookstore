// ========== ADPRODUCT.JS ==========
// Quản lý danh sách sản phẩm (phân trang + tìm kiếm toàn DB)

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("product");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");
    const tableWrapper = document.querySelector(".table-container");
    const totalEl = document.getElementById("totalProducts");
    const inStockEl = document.getElementById("inStock");
    const outOfStockEl = document.getElementById("outOfStock");
    const paginationEl = document.getElementById("pagination");
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const statusFilter = document.getElementById("statusFilter");

    const statusLabels = {
        active: 'Active',
        inactive: 'Inactive',
        pending: 'Pending',
        banned: 'Banned'
    };

    let products = [];
    let currentPage = 1;
    const limit = 20;
    let currentSearch = "";
    let currentSearchType = "all";
    let currentStatusFilter = "all";

    // ===== Utility =====
    const escapeHtml = (text) => {
        if (text === null || text === undefined) return "";
        return String(text).replace(/[&<>"']/g, (m) => {
            const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
            return map[m];
        });
    };

    const showLoading = () => {
        if (loadingState) loadingState.style.display = "block";
        if (tableWrapper) tableWrapper.style.display = "none";
        if (emptyState) emptyState.style.display = "none";
    };

    const hideLoading = () => {
        if (loadingState) loadingState.style.display = "none";
        if (tableWrapper) tableWrapper.style.display = "block";
    };

    const showEmpty = () => {
        if (emptyState) emptyState.style.display = "block";
        const tableBox = document.querySelector(".table-container");
        if (tableBox) tableBox.style.display = "none";
        const pagination = document.getElementById("pagination");
        if (pagination) pagination.style.display = "none";
    };

    const hideEmpty = () => {
        if (emptyState) emptyState.style.display = "none";
        const tableBox = document.querySelector(".table-container");
        if (tableBox) tableBox.style.display = "block";
        const pagination = document.getElementById("pagination");
        if (pagination) pagination.style.display = "flex"; // để hiện lại bình thường
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
            const stock = p.stock ?? p.stock_quantity ?? 0;
            const shop = p.shop_name || "-";
            const commission = p.commission_rate ? p.commission_rate + "%" : "-";

            const status = p.status || "active"; // Default to active if not set
            const statusKey = (status || '').toLowerCase();

            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${escapeHtml(p.id || "-")}</td>
                <td>${escapeHtml(p.title || "-")}</td>
                <td>${escapeHtml(p.author || "-")}</td>
                <td>${escapeHtml(p.category || "-")}</td>
                <td>${price}</td>
                <td>${stock}</td>
                <td><span class="badge ${statusKey === 'active' ? 'badge-success' : statusKey === 'inactive' ? 'badge-secondary' : statusKey === 'banned' ? 'badge-danger' : 'badge-warning'}">${statusLabels[statusKey] || status}</span></td>
                <td>${escapeHtml(shop)}</td>
                <td class="actions">
                    <button class="btn-icon btn-edit" title="Sửa" data-id="${p.id}">
                    <i class="fas fa-edit"></i>
                    </button>
                        <button class="btn-icon btn-delete" title="Xóa" data-id="${p.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
`;
            tableBody.appendChild(tr);
        });
    };

    // ===== Update Stats =====
    const updateStats = (data) => {
        if (!data) return;

        if (data.stats) {
            if (totalEl) totalEl.textContent = data.stats.total_books ?? 0;
            if (inStockEl) inStockEl.textContent = data.stats.in_stock ?? 0;
            if (outOfStockEl) outOfStockEl.textContent = data.stats.out_stock ?? 0;
            return;
        }

        if (totalEl) totalEl.textContent = data.total || 0;
        const inStock = data.products?.filter((p) => (p.stock ?? p.stock_quantity) > 0)?.length || 0;
        const outOfStock = data.products?.filter((p) => (p.stock ?? p.stock_quantity) <= 0)?.length || 0;
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
        loadProducts(currentPage, currentSearch, currentSearchType, currentStatusFilter);
    };

    // ===== Load Products =====
    const loadProducts = async (page = 1, search = "", searchType = "all", statusFilter = "all") => {
        try {
            showLoading();
            const token = localStorage.getItem("admin_token"); // lấy token từ localStorage

            let url = `/api/admin/products?action=list&page=${page}&limit=${limit}`;
            if (search && search.trim()) {
                url += `&search=${encodeURIComponent(search)}&searchType=${encodeURIComponent(searchType)}`;
            }
            if (statusFilter && statusFilter !== "all") {
                url += `&status=${encodeURIComponent(statusFilter)}`;
            }

            const res = await fetch(url, {
                headers: {
                    "Authorization": `Bearer ${token}`, // gửi token kèm request
                    "Content-Type": "application/json"
                }
            });

            if (res.status === 401) {
                console.error("❌ Unauthorized — token hết hạn hoặc chưa đăng nhập");
                alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                window.location.href = "/login.jsp";
                return;
            }

            const data = await res.json();

            if (!data.products || data.products.length === 0) {
                tableBody.innerHTML = "";
                showEmpty();
            } else {
                hideEmpty();
                products = data.products;
                renderTable(products);
                updateStats(data);
                updatePagination(data.total, data.page, data.limit);
            }

        } catch (err) {
            console.error("Error loading:", err);
            showEmpty();
        } finally {
            hideLoading();
        }
    };
    window.loadProducts = loadProducts;

    const loadStats = async () => {
        try {
            const token = localStorage.getItem("admin_token"); // ✅ lấy token đã lưu
            const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=stats`, {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });
            if (!res.ok) throw new Error("Unauthorized or failed request");

            const data = await res.json();
            document.getElementById("totalProducts").textContent = data.total || 0;
            document.getElementById("inStock").textContent = data.in_stock || 0;
            document.getElementById("outOfStock").textContent = data.out_stock || 0;
        } catch (err) {
            console.error("Error loading stats:", err);
        }
    };

    // ===== Search and Reset =====
    const resetBtn = document.getElementById("btnReset");
    const searchType = document.getElementById("searchType");

    if (searchBtn) {
        searchBtn.addEventListener("click", () => {
            const searchValue = searchInput ? searchInput.value.trim() : "";
            const type = searchType ? searchType.value : "all";
            const status = statusFilter ? statusFilter.value : "all";
            currentSearch = searchValue;
            currentSearchType = type;
            currentStatusFilter = status;
            currentPage = 1;
            loadProducts(currentPage, currentSearch, currentSearchType, currentStatusFilter);
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            if (searchInput) searchInput.value = "";
            if (searchType) searchType.value = "all";
            if (statusFilter) statusFilter.value = "all";
            currentSearch = "";
            currentSearchType = "all";
            currentStatusFilter = "all";
            currentPage = 1;
            loadProducts(currentPage, currentSearch, currentSearchType, currentStatusFilter);
        });
    }

    if (searchInput) {
        searchInput.addEventListener("input", () => {
            const searchValue = searchInput.value.trim();
            const type = searchType ? searchType.value : "all";
            const status = statusFilter ? statusFilter.value : "all";
            currentSearch = searchValue;
            currentSearchType = type;
            currentStatusFilter = status;
            currentPage = 1;
            loadProducts(currentPage, currentSearch, currentSearchType, currentStatusFilter);
        });
        searchInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                const searchValue = searchInput.value.trim();
                const type = searchType ? searchType.value : "all";
                const status = statusFilter ? statusFilter.value : "all";
                currentSearch = searchValue;
                currentSearchType = type;
                currentStatusFilter = status;
                currentPage = 1;
                loadProducts(currentPage, currentSearch, currentSearchType, currentStatusFilter);
            }
        });
    }

    if (statusFilter) {
        statusFilter.addEventListener("change", () => {
            const searchValue = searchInput ? searchInput.value.trim() : "";
            const type = searchType ? searchType.value : "all";
            const status = statusFilter.value;
            currentSearch = searchValue;
            currentSearchType = type;
            currentStatusFilter = status;
            currentPage = 1;
            loadProducts(currentPage, currentSearch, currentSearchType, currentStatusFilter);
        });
    }

    // ===== Init =====
    loadProducts();
    loadStats();
});

// ===== Product modal CRUD =====
const productOverlay = document.getElementById('productModalOverlay');
const productBox = document.getElementById('productModalBox');
const productForm = document.getElementById('productForm');
const productIdInput = document.getElementById('productId');
const productTitleEl = document.getElementById('productModalTitle');
const prodImageInput = document.getElementById('prodImage');
const prodImagePreview = document.getElementById('prodImagePreview');

function updateImagePreview(url) {
    if (!prodImagePreview) return;
    prodImagePreview.innerHTML = '';
    if (url && url.trim()) {
        // Basic URL validation
        if (/^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)$/i.test(url.trim())) {
            prodImagePreview.innerHTML = `<img src="${url.trim()}" alt="Ảnh sản phẩm" style="max-width:120px;max-height:80px;border-radius:6px;border:1px solid #eee;box-shadow:0 2px 8px rgba(0,0,0,0.06);" />`;
        } else {
            prodImagePreview.innerHTML = `<span style="color:#9ca3af;font-size:13px;">URL không hợp lệ hoặc không phải ảnh</span>`;
        }
    } else {
        prodImagePreview.innerHTML = `<span style="color:#9ca3af;font-size:13px;">Chưa có ảnh</span>`;
    }
}

prodImageInput?.addEventListener('input', (e) => {
    updateImagePreview(e.target.value);
});

function showEl(el) { if (el) el.style.display = 'block'; }
function hideEl(el) { if (el) el.style.display = 'none'; }

function populateProductForm(data) {
    productIdInput.value = data.id || '';
    document.getElementById('prodTitle').value = data.title || '';
    document.getElementById('prodAuthor').value = data.author || '';
    document.getElementById('prodPrice').value = data.price || '';
    document.getElementById('prodStock').value = data.stock || '';
    document.getElementById('prodCategory').value = data.category || '';
    // Strip HTML tags for description textarea to display plain text
    const desc = data.description || '';
    const tmp = document.createElement('div'); tmp.innerHTML = desc; const plain = tmp.textContent || tmp.innerText || '';
    document.getElementById('prodDescription').value = plain;
    document.getElementById('prodImage').value = data.image_url || '';
    updateImagePreview(data.image_url || '');
    document.getElementById('prodStatus').value = data.status || 'active';
    document.getElementById('prodShopId').value = data.shop_id || '';
}

function openAddProduct() {
    productForm.reset();
    productIdInput.value = '';
    document.getElementById('prodStatus').value = 'pending'; // Default to active for new products
    productTitleEl.textContent = 'Thêm sản phẩm';
    hideEl(document.getElementById('productFeedback'));
    showEl(productOverlay);
    showEl(productBox);
}

async function openEditProduct(id) {
    try {
        const token = localStorage.getItem('admin_token');
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=get&id=${id}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        const data = await res.json();
        if (data.error) {
            const fb = document.getElementById('productFeedback');
            fb.textContent = data.error;
            showEl(fb);
            return;
        }
        populateProductForm(data);
        productTitleEl.textContent = 'Chỉnh sửa sản phẩm';
        hideEl(document.getElementById('productFeedback'));
        showEl(productOverlay);
        showEl(productBox);
    } catch (err) {
        console.error(err);
        const fb = document.getElementById('productFeedback');
        fb.textContent = 'Lỗi khi lấy thông tin sản phẩm';
        showEl(fb);
    }
}

document.getElementById('productModalClose')?.addEventListener('click', () => {
    hideEl(productOverlay);
    hideEl(productBox);
});
document.getElementById('productCancel')?.addEventListener('click', () => {
    hideEl(productOverlay);
    hideEl(productBox);
});

productForm?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = productIdInput.value;
    const fd = new FormData(productForm);
    const params = new URLSearchParams();
    for (const [k, v] of fd.entries()) params.append(k, v);
    const token = localStorage.getItem('admin_token');
    const fb = document.getElementById('productFeedback');
    hideEl(fb);

    try {
        const action = id ? 'update' : 'create';
        if (id) params.append('id', id);

        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=${action}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        });

        const data = await res.json();
        if (data.error) {
            fb.textContent = data.error;
            showEl(fb);
            return;
        }

        loadProducts(); // refresh table first

        // If create returned id, use it for auto-open edit
        if (!id && data.id) {
            populateProductForm(data);
            productTitleEl.textContent = 'Chỉnh sửa sản phẩm';
            hideEl(fb);
            return; // keep modal open
        }

        // For updates, close modal and show success
        hideEl(productOverlay);
        hideEl(productBox);
        alert(data.message || 'Thành công');
    } catch (err) {
        console.error(err);
        fb.textContent = 'Lỗi khi lưu sản phẩm';
        showEl(fb);
    }
});

// Hook add button
document.getElementById('openCreateProductBtn')?.addEventListener('click', openAddProduct);

// Delegate table actions (edit/delete)
document.querySelector('table')?.addEventListener('click', (e) => {
    const editBtn = e.target.closest('.btn-warning, .btn-edit');
    const delBtn = e.target.closest('.btn-danger, .btn-delete');
    if (editBtn) {
        const tr = editBtn.closest('tr'); const id = tr?.querySelector('td')?.textContent?.trim(); if (id) openEditProduct(id); return;
    }
    if (delBtn) {
        const tr = delBtn.closest('tr'); const id = tr?.querySelector('td')?.textContent?.trim(); if (id) openDeleteProduct(id); return;
    }
});

// Delete flow
const productDeleteOverlay = document.getElementById('productDeleteOverlay');
const productDeleteBox = document.getElementById('productDeleteBox');
let deletingProduct = null;
function openDeleteProduct(id) { deletingProduct = id; showEl(productDeleteOverlay); showEl(productDeleteBox); }
document.getElementById('productDeleteCancel')?.addEventListener('click', () => { hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct = null; });
document.getElementById('productDeleteClose')?.addEventListener('click', () => { hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct = null; });
document.getElementById('productDeleteConfirm')?.addEventListener('click', async () => {
    if (!deletingProduct) return; const token = localStorage.getItem('admin_token'); try { const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=delete&id=${deletingProduct}`, { method: 'POST', headers: { 'Authorization': `Bearer ${token}` } }); const data = await res.json(); if (data.error) { document.getElementById('productDeleteFeedback').textContent = data.error; document.getElementById('productDeleteFeedback').style.display = 'block'; return; } hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct = null; loadProducts(); alert(data.message || 'Đã xóa'); } catch (err) { console.error(err); alert('Lỗi khi xóa'); }
});

// Close when clicking overlay
productOverlay?.addEventListener('click', (e) => {
    if (e.target === productOverlay) {
        hideEl(productOverlay);
        hideEl(productBox);
    }
});
productDeleteOverlay?.addEventListener('click', (e) => { if (e.target === productDeleteOverlay) { hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct = null; } });

// Close on Escape
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        if (productBox && productBox.classList.contains('active')) {
            hideEl(productOverlay);
            hideEl(productBox);
            hideEl(document.getElementById('productFeedback'));
        }
        if (productDeleteBox && productDeleteBox.style.display === 'block') {
            hideEl(productDeleteOverlay);
            hideEl(productDeleteBox);
            hideEl(document.getElementById('productDeleteFeedback'));
            deletingProduct = null;
        }
    }
});
