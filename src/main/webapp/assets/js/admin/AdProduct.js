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
    let currentSearchType = "all";

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
            const stock = p.stock ?? p.stock_quantity ?? 0;
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
                <td>
                <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
                <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </td>`;
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
        loadProducts(currentPage, currentSearch);
    };

    // ===== Load Products =====
    const loadProducts = async (page = 1, search = "", searchType = "all") => {
        try {
            showLoading();
            const token = localStorage.getItem("admin_token"); // lấy token từ localStorage

            let url = `/api/admin/products?action=list&page=${page}&limit=${limit}`;
            if (search && search.trim()) {
                url += `&search=${encodeURIComponent(search)}&searchType=${encodeURIComponent(searchType)}`;
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
            currentSearch = searchValue;
            currentSearchType = type;
            currentPage = 1;
            loadProducts(currentPage, currentSearch, currentSearchType);
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            if (searchInput) searchInput.value = "";
            if (searchType) searchType.value = "all";
            currentSearch = "";
            currentSearchType = "all";
            currentPage = 1;
            loadProducts(currentPage, currentSearch, currentSearchType);
        });
    }

    if (searchInput) {
        searchInput.addEventListener("keypress", (e) => {
            if (e.key === "Enter") {
                const searchValue = searchInput.value.trim();
                const type = searchType ? searchType.value : "all";
                currentSearch = searchValue;
                currentSearchType = type;
                currentPage = 1;
                loadProducts(currentPage, currentSearch, currentSearchType);
            }
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

function showEl(el){ if(el) el.style.display='block'; }
function hideEl(el){ if(el) el.style.display='none'; }

function openAddProduct(){
    productForm.reset(); productIdInput.value=''; productTitleEl.textContent='Thêm sản phẩm'; hideEl(document.getElementById('productFeedback')); showEl(productOverlay); showEl(productBox);
}

async function openEditProduct(id){
    try{
        const token = localStorage.getItem('admin_token');
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=get&id=${id}`,{headers:{'Authorization':`Bearer ${token}`}});
        const data = await res.json(); if(data.error){ alert(data.error); return; }
        productIdInput.value = data.id || '';
        document.getElementById('prodTitle').value = data.title || '';
        document.getElementById('prodAuthor').value = data.author || '';
        document.getElementById('prodISBN').value = data.isbn || '';
        document.getElementById('prodPrice').value = data.price || '';
        document.getElementById('prodStock').value = data.stock || '';
        document.getElementById('prodCategory').value = data.category || '';
        document.getElementById('prodDescription').value = data.description || '';
        document.getElementById('prodImage').value = data.image_url || '';
        document.getElementById('prodShopId').value = data.shop_id || '';
        productTitleEl.textContent='Chỉnh sửa sản phẩm'; hideEl(document.getElementById('productFeedback')); showEl(productOverlay); showEl(productBox);
    }catch(err){ console.error(err); alert('Lỗi khi lấy sản phẩm'); }
}

document.getElementById('productModalClose')?.addEventListener('click', ()=>{ hideEl(productOverlay); hideEl(productBox); });
document.getElementById('productCancel')?.addEventListener('click', ()=>{ hideEl(productOverlay); hideEl(productBox); });

productForm?.addEventListener('submit', async (e)=>{
    e.preventDefault();
    const id = productIdInput.value;
    const fd = new FormData(productForm);
    const params = new URLSearchParams(); for(const [k,v] of fd.entries()) params.append(k,v);
    const token = localStorage.getItem('admin_token');
    try{
        const action = id ? 'update' : 'create'; if(id) params.append('id', id);
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=${action}`,{ method:'POST', headers:{ 'Authorization':`Bearer ${token}`, 'Content-Type':'application/x-www-form-urlencoded'}, body: params.toString() });
        const data = await res.json();
        if(data.error){ const fb=document.getElementById('productFeedback'); fb.textContent = data.error; fb.style.display='block'; return; }
        // If create returned id, use it (helpful for immediate edit or navigation)
        if(!id && data.id){
            // Optionally open edit view or just inform
            hideEl(productOverlay); hideEl(productBox);
            loadProducts();
            alert('Sản phẩm đã được tạo (ID: ' + data.id + ')');
            return;
        }
        hideEl(productOverlay); hideEl(productBox); loadProducts(); alert(data.message || 'Thành công');
    }catch(err){ console.error(err); alert('Lỗi khi lưu sản phẩm'); }
});

// Hook add button
document.getElementById('openCreateProductBtn')?.addEventListener('click', openAddProduct);

// Delegate table actions (edit/delete)
document.querySelector('table')?.addEventListener('click', (e)=>{
    const editBtn = e.target.closest('.btn-warning, .btn-edit');
    const delBtn = e.target.closest('.btn-danger, .btn-delete');
    if(editBtn){
        const tr = editBtn.closest('tr'); const id = tr?.querySelector('td')?.textContent?.trim(); if(id) openEditProduct(id); return;
    }
    if(delBtn){
        const tr = delBtn.closest('tr'); const id = tr?.querySelector('td')?.textContent?.trim(); if(id) openDeleteProduct(id); return;
    }
});

// Delete flow
const productDeleteOverlay = document.getElementById('productDeleteOverlay');
const productDeleteBox = document.getElementById('productDeleteBox');
let deletingProduct = null;
function openDeleteProduct(id){ deletingProduct = id; showEl(productDeleteOverlay); showEl(productDeleteBox); }
document.getElementById('productDeleteCancel')?.addEventListener('click', ()=>{ hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct=null; });
document.getElementById('productDeleteClose')?.addEventListener('click', ()=>{ hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct=null; });
document.getElementById('productDeleteConfirm')?.addEventListener('click', async ()=>{
    if(!deletingProduct) return; const token=localStorage.getItem('admin_token'); try{ const res=await fetch(`${window.appConfig?.contextPath || ''}/api/admin/products?action=delete&id=${deletingProduct}`,{method:'POST', headers:{'Authorization':`Bearer ${token}`}}); const data=await res.json(); if(data.error){ document.getElementById('productDeleteFeedback').textContent=data.error; document.getElementById('productDeleteFeedback').style.display='block'; return; } hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct=null; loadProducts(); alert(data.message||'Đã xóa'); }catch(err){ console.error(err); alert('Lỗi khi xóa'); }
});

// Close when clicking overlay
productOverlay?.addEventListener('click', (e)=>{ if(e.target===productOverlay){ hideEl(productOverlay); hideEl(productBox); } });
productDeleteOverlay?.addEventListener('click', (e)=>{ if(e.target===productDeleteOverlay){ hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct=null; } });

// Close on Escape
document.addEventListener('keydown', (e)=>{
    if(e.key === 'Escape'){
        if(productBox && productBox.style.display==='block'){ hideEl(productOverlay); hideEl(productBox); }
        if(productDeleteBox && productDeleteBox.style.display==='block'){ hideEl(productDeleteOverlay); hideEl(productDeleteBox); deletingProduct=null; }
    }
});
