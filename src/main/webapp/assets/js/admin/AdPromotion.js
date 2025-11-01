// ========== ADPROMOTION.JS ==========
// Quản lý chương trình khuyến mãi

let searchInput;
let searchTypeSelect;

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("promotionTable");
    searchInput = document.getElementById("promotionSearchInput");
    searchTypeSelect = document.getElementById("searchType");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let promotions = [];
    let filteredPromotions = [];
    let isSearching = false;
    let isStatusFiltering = false;

    // API functions
    const api = {
        getPromotions: (search = '', searchType = 'all') => {
            const token = localStorage.getItem("admin_token");
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

    const showEmpty = (message = "Không có khuyến mãi nào") => {
        if (emptyState) emptyState.style.display = 'block';
        if (tableContainer) tableContainer.style.display = 'none';
        const emptyMessageEl = document.getElementById('emptyMessage');
        if (emptyMessageEl) emptyMessageEl.textContent = message;
    };

    const hideEmpty = () => {
        if (emptyState) emptyState.style.display = 'none';
        if (tableContainer) tableContainer.style.display = 'block';
    };

    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (list.length === 0) {
            let message = "Không có khuyến mãi nào";
            if (isSearching) {
                message = "Không tìm thấy khuyến mãi nào phù hợp với từ khóa tìm kiếm";
            } else if (isStatusFiltering) {
                message = "Không tìm thấy khuyến mãi nào phù hợp với bộ lọc trạng thái";
            }
            showEmpty(message);
            return;
        }

        hideEmpty();
        // lọc trùng id khác source (vì promotions.id có thể trùng với shop_coupons.id)
        const unique = {};
        list = list.filter(p => {
          const key = `${p.id}_${p.source}`;
          if (unique[key]) return false;
          unique[key] = true;
          return true;
        });
        list.forEach(p => {
            const vnd = n => Number(n).toLocaleString('vi-VN');
            const type = p.type || "-";             // percent / amount
            const kind = p.kind || p.scope || "-";           // product / shipping

            // Xác định nhãn loại khuyến mãi (hiển thị dễ hiểu hơn)
            const typeLabel =
                kind === "shipping" ? "Giảm phí vận chuyển" :
                kind === "product"  ? "Giảm giá sản phẩm" :
                "-";

            // Xử lý giá trị giảm
            let discount = "-";
            if (type === "percent" || type === "percentage")
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
            if (p.source === 'shop') {
                tr.classList.add('table-warning'); // nền vàng nhẹ
            }
            tr.innerHTML = `
                <td>${escapeHtml(p.id.toString())}</td>
                <td>${escapeHtml(code)}</td>
                <td>${escapeHtml(description)}</td>
                <td>${escapeHtml(typeLabel)}</td>
                <td>${discount}</td>
                <td>${escapeHtml(p.shop_name || '-')}</td>
                <td>${valid}</td>
                <td>${active}</td>
                <td class="actions">
                    <button class="btn-icon btn-edit" title="Sửa" data-id="${p.id}" data-source="${p.source}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-icon btn-delete" title="Xóa" data-id="${p.id}" data-source="${p.source}">
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
        const searchType = searchTypeSelect ? searchTypeSelect.value : 'all';
        const statusFilter = document.getElementById('statusFilter') ? document.getElementById('statusFilter').value : 'all';

        if (keyword) {
            isSearching = true;
            // Server-side search
            try {
                showLoading();
                const response = await api.getPromotions(keyword, searchType);
                if (response.promotions) {
                    let filtered = response.promotions;

                    // Apply status filter client-side
                    if (statusFilter !== 'all') {
                        const isActive = statusFilter === 'true';
                        filtered = filtered.filter(p => p.active === isActive);
                    }

                    filteredPromotions = filtered;
                    renderTable(filteredPromotions);

                    // Update stats for filtered results
                    const totalPromoEl = document.getElementById("totalPromo");
                    const activePromoEl = document.getElementById("activePromo");
                    if (totalPromoEl) totalPromoEl.textContent = filtered.length;
                    if (activePromoEl) activePromoEl.textContent = filtered.filter(p => p.active).length;
                } else {
                    console.error("Invalid response format:", response);
                    showEmpty("Không tìm thấy khuyến mãi nào phù hợp với từ khóa tìm kiếm");
                }
            } catch (error) {
                console.error("Error searching promotions:", error);
                showEmpty("Không tìm thấy khuyến mãi nào phù hợp với từ khóa tìm kiếm");
            } finally {
                hideLoading();
            }
        } else {
            // No search, apply status filter client-side
            let filtered = [...promotions];

            // Apply status filter client-side
            if (statusFilter !== 'all') {
                const isActive = statusFilter === 'true';
                filtered = filtered.filter(p => p.active === isActive);
                isStatusFiltering = true;
                isSearching = false;
            } else {
                isSearching = false;
                isStatusFiltering = false;
            }

            filteredPromotions = filtered;
            renderTable(filteredPromotions);
        }
    };
    window.loadPromotions = loadPromotions;

    const resetFilters = () => {
        if (searchInput) searchInput.value = '';
        if (searchTypeSelect) searchTypeSelect.value = 'all';
        const statusFilter = document.getElementById('statusFilter');
        if (statusFilter) statusFilter.value = 'all';
        isSearching = false;
        isStatusFiltering = false;
        loadPromotions();
    };

    // Expose for inline onclick fallback from JSP
    window.resetFilters = resetFilters;
    window.applyFilters = applyFilters;

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }
    if (searchTypeSelect) {
        searchTypeSelect.addEventListener("change", applyFilters);
    }
    document.getElementById('statusFilter')?.addEventListener('change', applyFilters);

    // Init
    loadPromotions();
    // Hook reset button
    document.getElementById('btnReset')?.addEventListener('click', resetFilters);
});

// ===== Promotion modal CRUD =====
const promoOverlay = document.getElementById('promoModalOverlay');
const promoBox = document.getElementById('promoModalBox');
const promoForm = document.getElementById('promoForm');
const promoIdInput = document.getElementById('promoId');
const promoTitleEl = document.getElementById('promoModalTitle');

function show(el){ if(el) el.style.display='block'; }
function hide(el){ if(el) el.style.display='none'; }

function openAddPromo(){
    promoForm.reset(); promoIdInput.value=''; promoTitleEl.textContent='Thêm khuyến mãi'; hide(document.getElementById('promoFeedback'));
    // Set source to system for new promotions and show max discount field
    document.getElementById('promoSource').value = 'system';
    document.getElementById('maxDiscountRow').style.display = 'block';
    show(promoOverlay); show(promoBox);
}



document.getElementById('promoModalClose')?.addEventListener('click', ()=>{ hide(promoOverlay); hide(promoBox); });
document.getElementById('promoCancel')?.addEventListener('click', ()=>{ hide(promoOverlay); hide(promoBox); });

// Close when clicking outside the modal (overlay)
promoOverlay?.addEventListener('click', (e)=>{ if(e.target===promoOverlay){ hide(promoOverlay); hide(promoBox); } });

// Close delete modal by clicking overlay
const promoDeleteOverlayEl = document.getElementById('promoDeleteOverlay');
promoDeleteOverlayEl?.addEventListener('click', (e)=>{ if(e.target===promoDeleteOverlayEl){ hide(promoDeleteOverlayEl); hide(document.getElementById('promoDeleteBox')); deletingPromo=null; } });

// Close modals on Escape
document.addEventListener('keydown', (e)=>{
    if(e.key === 'Escape'){
        if(promoBox && promoBox.style.display==='block'){ hide(promoOverlay); hide(promoBox); }
        if(promoDeleteBox && promoDeleteBox.style.display==='block'){ hide(promoDeleteOverlay); hide(promoDeleteBox); deletingPromo=null; }
    }
});

promoForm?.addEventListener('submit', async (e)=>{
    e.preventDefault();
    const id = promoIdInput.value;
    const fd = new FormData(promoForm);
    const params = new URLSearchParams(); for(const [k,v] of fd.entries()) params.append(k,v);
    const source = document.getElementById('promoSource')?.value || 'system';
    params.append('source', source);
    const token = localStorage.getItem('admin_token');
    try{
        const action = id ? 'update' : 'create'; if(id) params.append('id', id);
        console.log('[Promotions] submitting', action, params.toString());
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=${action}`,{ method:'POST', headers:{ 'Authorization':`Bearer ${token}`, 'Content-Type':'application/x-www-form-urlencoded', 'Accept':'application/json'}, body: params.toString() });
        console.log('[Promotions] response status', res.status);
        const text = await res.text();
        let data = {};
        try{ data = JSON.parse(text); } catch(err){
            console.warn('Could not parse JSON response for promotions:', text);
            alert('Server trả về (POST):\n' + text);
            return;
        }
        console.log('[Promotions] response json', data);
        if(data.error){ const fb=document.getElementById('promoFeedback'); fb.textContent = data.error; fb.style.display='block'; alert('Server trả về lỗi (POST):\n' + data.error); return; }
        // success
        const fb=document.getElementById('promoFeedback'); if(fb){ fb.style.display='none'; }
        hide(promoOverlay); hide(promoBox); loadPromotions(); alert(data.message || 'Thành công');
    }catch(err){ console.error(err); alert('Lỗi khi lưu khuyến mãi'); }
});

// Hook add button
document.getElementById('openCreatePromotionBtn')?.addEventListener('click', openAddPromo);

// Delegate table actions
document.querySelector('#promotionTable')?.addEventListener('click', (e)=>{
    const edit = e.target.closest('.btn-edit'); if(edit && edit.dataset.id){ openEditPromo(edit.dataset.id, edit.dataset.source); return; }
    const del = e.target.closest('.btn-delete'); if(del && del.dataset.id){ openDeletePromo(del.dataset.id); return; }
});

// Delete flow
const promoDeleteOverlay = document.getElementById('promoDeleteOverlay');
const promoDeleteBox = document.getElementById('promoDeleteBox');
let deletingPromo = null;
function openDeletePromo(id){ deletingPromo = id; show(promoDeleteOverlay); show(promoDeleteBox); }
document.getElementById('promoDeleteCancel')?.addEventListener('click', ()=>{ hide(promoDeleteOverlay); hide(promoDeleteBox); deletingPromo=null; });
document.getElementById('promoDeleteClose')?.addEventListener('click', ()=>{ hide(promoDeleteOverlay); hide(promoDeleteBox); deletingPromo=null; });
document.getElementById('promoDeleteConfirm')?.addEventListener('click', async ()=>{
    if(!deletingPromo) return; const token=localStorage.getItem('admin_token'); try{ const res=await fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=delete&id=${deletingPromo}`,{method:'POST', headers:{'Authorization':`Bearer ${token}`}}); const data=await res.json(); if(data.error){ document.getElementById('promoDeleteFeedback').textContent=data.error; document.getElementById('promoDeleteFeedback').style.display='block'; return; } hide(promoDeleteOverlay); hide(promoDeleteBox); deletingPromo=null; loadPromotions(); alert(data.message||'Đã xóa'); }catch(err){ console.error(err); alert('Lỗi khi xóa'); }
});

// Improve openEditPromo logging
async function openEditPromo(id, source){
    try{
        const token = localStorage.getItem('admin_token');
        console.log('[Promotions] fetch get id=', id, 'source=', source);
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/promotions?action=get&id=${id}&source=${source}`,{headers:{'Authorization':`Bearer ${token}`, 'Accept':'application/json'}});
        console.log('[Promotions] get status', res.status);
        const text = await res.text();
        let data = {};
        try{ data = JSON.parse(text); } catch(err){
            console.warn('Could not parse JSON get response:', text);
            alert('Server trả về (GET):\n' + text);
            return;
        }
        if(data.error){ alert('Server trả về lỗi (GET):\n' + data.error); return; }
        promoIdInput.value = data.id || '';
        document.getElementById('promoSource').value = data.source || source || 'system';
        document.getElementById('promoName').value = data.name || '';
        document.getElementById('promoCode').value = data.code || '';
        document.getElementById('promoDescription').value = data.description || '';
        let typeValue = data.type ? data.type.toLowerCase() : 'percent';
        if (typeValue === 'percentage') typeValue = 'percent';
        document.getElementById('promoType').value = typeValue;
        document.getElementById('promoKind').value = data.kind || 'product';
        document.getElementById('promoValue').value = data.discount_value || '';
        // Show max discount field for both system and shop promotions
        const maxDiscountRow = document.getElementById('maxDiscountRow');
        maxDiscountRow.style.display = 'block';
        document.getElementById('promoMaxDiscount').value = data.max_discount_value || '';
        document.getElementById('promoMinOrder').value = data.min_order_value || '';
        if(data.start_at) document.getElementById('promoStart').value = (new Date(data.start_at)).toISOString().slice(0,16);
        if(data.end_at) document.getElementById('promoEnd').value = (new Date(data.end_at)).toISOString().slice(0,16);
        document.getElementById('promoActive').value = data.active ? 'true' : 'false';
        promoTitleEl.textContent='Chỉnh sửa khuyến mãi'; hide(document.getElementById('promoFeedback')); show(promoOverlay); show(promoBox);
    }catch(err){ console.error(err); alert('Lỗi khi lấy khuyến mãi'); }
}
