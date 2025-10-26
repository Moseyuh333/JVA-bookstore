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
        getCommissions: (search, searchType, statusFilter) => {
            const token = localStorage.getItem("admin_token");
            let url = `${window.appConfig?.contextPath || ''}/api/admin/commissions?action=list`;
            if (search && search.trim()) {
                url += `&search=${encodeURIComponent(search.trim())}&searchType=${encodeURIComponent(searchType)}`;
            }
            if (statusFilter && statusFilter !== 'all') {
                url += `&status=${encodeURIComponent(statusFilter)}`;
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
            <tr>
                <td>${escapeHtml(c.id.toString())}</td>
                <td>${escapeHtml(c.name)}</td>
                <td>${escapeHtml(type)}</td>
                <td>${minRevenue}</td>
                <td>${maxRevenue}</td>
                <td>${rate}</td>
                <td>${status}</td>
                <td>${createdAt}</td>
                <td>${updatedAt}</td>
                <td class="actions">
                    <button class="btn-icon btn-edit" title="Sửa" data-id="${c.id}">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-icon btn-delete" title="Xóa" data-id="${c.id}">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>`;
            tableBody.appendChild(tr);
        });
    };

    // Load commissions from API
    const loadCommissions = async () => {
        try {
            showLoading();
            const search = searchInput.value.trim();
            const searchType = currentSearchType;
            const statusFilter = document.getElementById('commissionStatusFilter') ? document.getElementById('commissionStatusFilter').value : 'all';
            const response = await api.getCommissions(search, searchType, statusFilter);

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
        const statusFilter = document.getElementById('commissionStatusFilter') ? document.getElementById('commissionStatusFilter').value : 'all';

        let filtered = commissions;

        // Apply status filter
        if (statusFilter !== 'all') {
            filtered = filtered.filter(c => c.status === statusFilter);
        }

        // Apply search filter
        if (searchType === "all") {
            filtered = filtered.filter(c =>
                (c.name || '').toLowerCase().includes(keyword) ||
                (c.type || '').toLowerCase().includes(keyword) ||
                (c.rate || '').toString().toLowerCase().includes(keyword)
            );
        } else if (searchType === "name") {
            filtered = filtered.filter(c =>
                (c.name || '').toLowerCase().includes(keyword)
            );
        } else if (searchType === "type") {
            filtered = filtered.filter(c =>
                (c.type || '').toLowerCase().includes(keyword)
            );
        } else if (searchType === "rate") {
            filtered = filtered.filter(c =>
                (c.rate || '').toString().toLowerCase().includes(keyword)
            );
        }

        filteredCommissions = filtered;
        renderTable(filteredCommissions);
    };

    const resetFilters = () => {
        if (searchInput) searchInput.value = '';
        if (searchTypeSelect) searchTypeSelect.value = 'all';
        const statusFilter = document.getElementById('commissionStatusFilter');
        if (statusFilter) statusFilter.value = 'all';
        currentSearchType = "all";
        loadCommissions();
    };

    // Expose for inline onclick fallback from JSP
    window.resetFilters = resetFilters;

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
    document.getElementById('commissionStatusFilter')?.addEventListener('change', loadCommissions);

    // Init
    loadCommissions();
    // Hook reset button
    document.getElementById('btnReset')?.addEventListener('click', resetFilters);
    // Modal wiring
    const modalOverlay = document.getElementById('commissionModalOverlay');
    const modalBox = document.getElementById('commissionModalBox');
    const modalTitle = document.getElementById('commissionModalTitle');
    const modalClose = document.getElementById('commissionModalClose');
    const commissionForm = document.getElementById('commissionForm');
    const commissionId = document.getElementById('commissionId');
    const commissionName = document.getElementById('commissionName');
    const commissionType = document.getElementById('commissionType');
    const commissionMinRevenue = document.getElementById('commissionMinRevenue');
    const commissionMaxRevenue = document.getElementById('commissionMaxRevenue');
    const commissionRate = document.getElementById('commissionRate');
    const commissionStatus = document.getElementById('commissionStatus');
    const commissionFeedback = document.getElementById('commissionFeedback');

    const openModal = (mode = 'create') => {
        commissionFeedback.className = 'form-feedback';
        commissionFeedback.style.display = 'none';
        if (mode === 'create') {
            modalTitle.textContent = 'Thêm chính sách chiết khấu';
            commissionForm.reset();
            commissionId.value = '';
        } else {
            modalTitle.textContent = 'Chỉnh sửa chính sách chiết khấu';
        }
        modalOverlay.classList.add('active');
        modalBox.classList.add('active');
    };

    const closeModal = () => {
        modalOverlay.classList.remove('active');
        modalBox.classList.remove('active');
    };

    modalClose?.addEventListener('click', closeModal);
    document.getElementById('commissionCancel')?.addEventListener('click', closeModal);
    modalOverlay?.addEventListener('click', (e) => { if (e.target === modalOverlay) closeModal(); });

    // API helpers for CRUD
    api.getCommission = (id) => {
        const token = localStorage.getItem('admin_token');
        return fetch(`${window.appConfig?.contextPath || ''}/api/admin/commissions?action=get&id=${encodeURIComponent(id)}`, {
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' }
        }).then(r => r.json());
    };

    api.createCommission = (data) => {
        const token = localStorage.getItem('admin_token');
        const params = new URLSearchParams(Object.entries(data));
        return fetch(`${window.appConfig?.contextPath || ''}/api/admin/commissions?action=create`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }).then(r => r.json().catch(() => ({ message: 'OK' })));
    };

    api.updateCommission = (data) => {
        const token = localStorage.getItem('admin_token');
        const params = new URLSearchParams(Object.entries(data));
        return fetch(`${window.appConfig?.contextPath || ''}/api/admin/commissions?action=update`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }).then(r => r.json().catch(() => ({ message: 'OK' })));
    };

    api.deleteCommission = (id) => {
        const token = localStorage.getItem('admin_token');
        const params = new URLSearchParams({ id });
        return fetch(`${window.appConfig?.contextPath || ''}/api/admin/commissions?action=delete`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params.toString()
        }).then(r => r.json().catch(() => ({ message: 'OK' })));
    };

    // Hook up Add button (we added id openCreateCommissionBtn in JSP)
    document.getElementById('openCreateCommissionBtn')?.addEventListener('click', () => openModal('create'));

    // Handle form submit for create/update
    commissionForm?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = commissionId.value && commissionId.value.trim() ? commissionId.value.trim() : null;
        const payload = {
            name: commissionName.value.trim(),
            type: commissionType.value.trim(),
            min_revenue: commissionMinRevenue.value || '',
            max_revenue: commissionMaxRevenue.value || '',
            rate: commissionRate.value,
            status: commissionStatus.value
        };

        try {
            commissionFeedback.style.display = 'none';
            if (!payload.name || !payload.rate) {
                commissionFeedback.className = 'form-feedback error';
                commissionFeedback.textContent = 'Tên và tỷ lệ là bắt buộc.';
                commissionFeedback.style.display = 'block';
                return;
            }

            if (id) {
                payload.id = id;
                const res = await api.updateCommission(payload);
                if (res && res.message) {
                    commissionFeedback.className = 'form-feedback success';
                    commissionFeedback.textContent = res.message;
                    commissionFeedback.style.display = 'block';
                    loadCommissions();
                    setTimeout(closeModal, 800);
                } else if (res && res.error) {
                    commissionFeedback.className = 'form-feedback error';
                    commissionFeedback.textContent = res.error;
                    commissionFeedback.style.display = 'block';
                }
            } else {
                const res = await api.createCommission(payload);
                if (res && (res.id || res.message)) {
                    commissionFeedback.className = 'form-feedback success';
                    commissionFeedback.textContent = res.message || 'Created';
                    commissionFeedback.style.display = 'block';
                    // If backend returned id, set it (useful for immediate editing)
                    if (res.id) commissionId.value = res.id;
                    loadCommissions();
                    setTimeout(closeModal, 800);
                } else if (res && res.error) {
                    commissionFeedback.className = 'form-feedback error';
                    commissionFeedback.textContent = res.error;
                    commissionFeedback.style.display = 'block';
                }
            }
        } catch (err) {
            console.error('Error saving commission', err);
            commissionFeedback.className = 'form-feedback error';
            commissionFeedback.textContent = 'Lỗi khi lưu dữ liệu.';
            commissionFeedback.style.display = 'block';
        }
    });

    // Delegate edit/delete button clicks
    tableBody.addEventListener('click', async (ev) => {
        const editBtn = ev.target.closest('.btn-edit');
        const delBtn = ev.target.closest('.btn-delete');
        if (editBtn) {
            const tr = editBtn.closest('tr');
            const idCell = tr.querySelector('td');
            const id = idCell ? idCell.textContent.trim() : null;
            if (!id) return;
            try {
                const data = await api.getCommission(id);
                if (data && data.id) {
                    commissionId.value = data.id;
                    commissionName.value = data.name || '';
                    commissionType.value = data.type || '';
                    commissionMinRevenue.value = data.min_revenue || '';
                    commissionMaxRevenue.value = data.max_revenue || '';
                    commissionRate.value = data.rate || '';
                    commissionStatus.value = data.status || 'active';
                    openModal('edit');
                } else {
                    alert('Không thể tải dữ liệu chiết khấu.');
                }
            } catch (err) { console.error(err); alert('Lỗi khi tải dữ liệu.'); }
        } else if (delBtn) {
            const tr = delBtn.closest('tr');
            const idCell = tr.querySelector('td');
            const id = idCell ? idCell.textContent.trim() : null;
            if (!id) return;
            if (!confirm('Bạn có chắc muốn xóa chính sách này?')) return;
            try {
                const res = await api.deleteCommission(id);
                if (res && res.message) {
                    loadCommissions();
                } else if (res && res.error) {
                    alert('Xóa thất bại: ' + res.error);
                }
            } catch (err) { console.error(err); alert('Lỗi khi xóa.'); }
        }
    });
});
