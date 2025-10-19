<%@ page contentType="text/html; charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin - Quản lý trạng thái đơn hàng</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <style>
        body { background-color: #f6f7fb; }
        .timeline { position: relative; padding-left: 1.5rem; }
        .timeline::before { content: ""; position: absolute; top: 0; bottom: 0; left: 0.45rem; width: 2px; background-color: #dee2e6; }
        .timeline-item { position: relative; margin-bottom: 1.25rem; padding-left: 1.5rem; }
        .timeline-item::before { content: ""; position: absolute; left: -0.62rem; top: 0.25rem; width: 0.75rem; height: 0.75rem; border-radius: 50%; background-color: #0d6efd; }
        .table-hover tbody tr { cursor: pointer; }
        .order-row-active { background-color: #e7f1ff; }
        .badge-status { text-transform: capitalize; }
        .card-shadow { box-shadow: 0 1rem 2.5rem rgba(18, 38, 63, 0.05); }
        .sticky-actions { position: sticky; top: 0; z-index: 10; background-color: #fff; }
    </style>
</head>
<body>
    <div class="container py-4">
        <div class="d-flex flex-wrap align-items-center gap-3 mb-4">
            <div>
                <h1 class="h3 mb-0">Bảng điều khiển trạng thái đơn hàng</h1>
                <p class="text-muted mb-0">Xem, cập nhật nhanh trạng thái để kiểm thử quy trình giao hàng và đánh giá.</p>
            </div>
            <div class="ms-auto">
                <a class="btn btn-outline-secondary" href="<%= request.getContextPath() %>/" target="_blank"><i class="fas fa-external-link-alt me-2"></i>Mở cửa hàng</a>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-7">
                <div class="card card-shadow border-0 h-100">
                    <div class="card-body">
                        <div class="row g-3 align-items-end mb-3">
                            <div class="col-md-4">
                                <label for="statusFilter" class="form-label">Lọc trạng thái</label>
                                <select id="statusFilter" class="form-select">
                                    <option value="all">Tất cả</option>
                                    <option value="new">Đơn hàng mới</option>
                                    <option value="confirmed">Đã xác nhận</option>
                                    <option value="shipping">Đang giao</option>
                                    <option value="delivered">Đã giao</option>
                                    <option value="cancelled">Đã hủy</option>
                                    <option value="returned">Hoàn trả</option>
                                </select>
                            </div>
                            <div class="col-md-5">
                                <label for="searchInput" class="form-label">Tìm kiếm (mã đơn, email, tên)</label>
                                <input type="search" id="searchInput" class="form-control" placeholder="Ví dụ: ODABC123, user@gmail.com">
                            </div>
                            <div class="col-md-3">
                                <button id="refreshButton" class="btn btn-primary w-100"><i class="fas fa-rotate me-2"></i>Tải lại</button>
                            </div>
                        </div>

                        <div id="ordersFeedback" class="mb-3 small text-muted"></div>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0" id="ordersTable">
                                <thead class="table-light">
                                    <tr>
                                        <th class="text-nowrap">Mã đơn</th>
                                        <th>Khách hàng</th>
                                        <th class="text-nowrap">Ngày tạo</th>
                                        <th class="text-end">Tổng cộng</th>
                                        <th class="text-center">Trạng thái</th>
                                    </tr>
                                </thead>
                                <tbody id="ordersBody">
                                    <tr><td colspan="5" class="text-center py-4 text-muted">Chưa có dữ liệu</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="card card-shadow border-0 h-100">
                    <div class="card-header bg-white sticky-actions">
                        <div class="d-flex align-items-center justify-content-between">
                            <h2 class="h5 mb-0">Chi tiết đơn hàng</h2>
                            <span id="detailStatusBadge"></span>
                        </div>
                    </div>
                    <div class="card-body" id="detailBody">
                        <div class="text-center py-4 text-muted">
                            <i class="fas fa-file-invoice fa-2x mb-3"></i>
                            <p>Chọn một đơn hàng từ danh sách để xem chi tiết.</p>
                        </div>
                    </div>
                    <div class="card-footer bg-white border-0" id="updateFooter" hidden>
                        <form id="statusForm" class="row g-2 align-items-center">
                            <div class="col-md-6">
                                <label for="statusSelect" class="form-label">Cập nhật trạng thái</label>
                                <select id="statusSelect" class="form-select" required>
                                    <option value="">-- Chọn trạng thái --</option>
                                    <option value="new">Đơn hàng mới</option>
                                    <option value="confirmed">Đã xác nhận</option>
                                    <option value="shipping">Đang giao</option>
                                    <option value="delivered">Đã giao</option>
                                    <option value="cancelled">Đã hủy</option>
                                    <option value="returned">Hoàn trả</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="noteInput" class="form-label">Ghi chú</label>
                                <input type="text" id="noteInput" class="form-control" placeholder="Ví dụ: Đã giao thành công">
                            </div>
                            <div class="col-12 text-end">
                                <button type="submit" class="btn btn-success" id="updateButton"><i class="fas fa-save me-2"></i>Lưu trạng thái</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const contextPath = '<%= request.getContextPath() %>';
        const state = {
            status: 'all',
            query: '',
            orders: [],
            loading: false,
            selectedOrderId: null,
            timeline: [],
            selectedOrder: null
        };
        const STATUS_LABELS = {
            new: { label: 'Đơn mới', badge: 'info' },
            confirmed: { label: 'Đã xác nhận', badge: 'primary' },
            shipping: { label: 'Đang giao', badge: 'warning' },
            delivered: { label: 'Đã giao', badge: 'success' },
            cancelled: { label: 'Đã hủy', badge: 'danger' },
            returned: { label: 'Hoàn trả', badge: 'dark' }
        };

        document.addEventListener('DOMContentLoaded', () => {
            document.getElementById('statusFilter').addEventListener('change', onFilterChange);
            document.getElementById('searchInput').addEventListener('keyup', evt => {
                if (evt.key === 'Enter') {
                    onFilterChange();
                }
            });
            document.getElementById('refreshButton').addEventListener('click', evt => {
                evt.preventDefault();
                onFilterChange();
            });
            document.getElementById('statusForm').addEventListener('submit', onSubmitStatusUpdate);
            loadOrders();
        });

        function onFilterChange() {
            state.status = document.getElementById('statusFilter').value || 'all';
            state.query = document.getElementById('searchInput').value || '';
            loadOrders();
        }

        async function loadOrders() {
            state.loading = true;
            renderOrders();
            const params = new URLSearchParams();
            params.set('action', 'list');
            if (state.status && state.status !== 'all') {
                params.set('status', state.status);
            }
            if (state.query && state.query.trim() !== '') {
                params.set('q', state.query.trim());
            }
            const feedback = document.getElementById('ordersFeedback');
            feedback.textContent = 'Đang tải danh sách đơn hàng...';
            try {
                const response = await fetch(`${contextPath}/api/admin/orders?${params.toString()}`);
                const data = await response.json();
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'Không thể tải danh sách đơn hàng');
                }
                state.orders = Array.isArray(data.orders) ? data.orders : [];
                feedback.textContent = `Hiển thị ${state.orders.length} đơn hàng`;
                renderOrders();
                if (state.selectedOrderId) {
                    const stillExists = state.orders.some(order => order.id === state.selectedOrderId);
                    if (!stillExists) {
                        resetDetail();
                    }
                }
            } catch (error) {
                console.error('loadOrders error', error);
                feedback.innerHTML = `<span class="text-danger">${escapeHtml(error.message || 'Không thể tải danh sách đơn hàng')}</span>`;
                state.orders = [];
                renderOrders();
            } finally {
                state.loading = false;
            }
        }

        function renderOrders() {
            const tbody = document.getElementById('ordersBody');
            if (!Array.isArray(state.orders) || state.orders.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Không có đơn hàng phù hợp.</td></tr>';
                return;
            }
            let rows = '';
            state.orders.forEach(order => {
                const statusMeta = STATUS_LABELS[normalizeKey(order.status)] || { label: order.status || 'N/A', badge: 'secondary' };
                const isActive = state.selectedOrderId === order.id;
                rows += `
                    <tr class="${isActive ? 'order-row-active' : ''}" data-order-id="${order.id}">
                        <td class="fw-semibold text-nowrap">${escapeHtml(order.code || ('#' + order.id))}</td>
                        <td>${escapeHtml(order.customerName || order.customerEmail || 'Ẩn danh')}</td>
                        <td class="text-nowrap">${formatDateTime(order.orderDate || order.createdAt)}</td>
                        <td class="text-end fw-semibold text-primary">${formatCurrency(order.totalAmount)}</td>
                        <td class="text-center"><span class="badge bg-${statusMeta.badge} badge-status">${escapeHtml(statusMeta.label)}</span></td>
                    </tr>`;
            });
            tbody.innerHTML = rows;
            tbody.querySelectorAll('tr[data-order-id]').forEach(row => {
                row.addEventListener('click', () => {
                    const id = Number(row.getAttribute('data-order-id'));
                    selectOrder(id);
                });
            });
        }

        async function selectOrder(orderId) {
            if (!orderId || state.selectedOrderId === orderId) {
                return;
            }
            state.selectedOrderId = orderId;
            renderOrders();
            const detailBody = document.getElementById('detailBody');
            detailBody.innerHTML = '<div class="text-center py-4 text-muted">Đang tải chi tiết đơn hàng...</div>';
            try {
                const response = await fetch(`${contextPath}/api/admin/orders?action=detail&id=${orderId}`);
                const data = await response.json();
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'Không thể tải chi tiết đơn hàng');
                }
                state.selectedOrder = data.order || null;
                state.timeline = Array.isArray(data.timeline) ? data.timeline : [];
                renderDetail();
            } catch (error) {
                console.error('selectOrder error', error);
                detailBody.innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message || 'Không thể tải chi tiết đơn hàng')}</div>`;
            }
        }

        function renderDetail() {
            const detailBody = document.getElementById('detailBody');
            const footer = document.getElementById('updateFooter');
            const badgeContainer = document.getElementById('detailStatusBadge');
            if (!state.selectedOrder) {
                detailBody.innerHTML = '<div class="text-center py-4 text-muted"><p>Không có dữ liệu đơn hàng.</p></div>';
                footer.hidden = true;
                badgeContainer.innerHTML = '';
                return;
            }
            const order = state.selectedOrder;
            const statusMeta = STATUS_LABELS[normalizeKey(order.status)] || { label: order.status || 'N/A', badge: 'secondary' };
            badgeContainer.innerHTML = `<span class="badge bg-${statusMeta.badge} badge-status">${escapeHtml(statusMeta.label)}</span>`;
            const items = Array.isArray(order.items) ? order.items : [];
            const itemsHtml = items.length === 0
                ? '<p class="text-muted mb-0">Không có sản phẩm nào được ghi nhận.</p>'
                : items.map(item => `
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <div class="fw-semibold">${escapeHtml(item.title || 'Sản phẩm')}</div>
                            <div class="text-muted small">SL: ${item.quantity} | ${escapeHtml(item.author || '')}</div>
                        </div>
                        <div class="text-end">
                            <div class="fw-semibold text-primary">${formatCurrency(item.totalPrice)}</div>
                            <div class="text-muted small">${formatCurrency(item.unitPrice)} / sản phẩm</div>
                        </div>
                    </div>`).join('');
            const timelineHtml = renderTimeline(state.timeline);
            detailBody.innerHTML = `
                <div class="mb-3">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <h3 class="h5 mb-1">${escapeHtml(order.code || ('#' + order.id))}</h3>
                            <div class="text-muted small">Mã đơn nội bộ: #${order.id}</div>
                        </div>
                        <div class="text-end">
                            <div class="fw-semibold fs-5 text-primary">${formatCurrency(order.totalAmount)}</div>
                            <div class="text-muted small">Phí vận chuyển: ${formatCurrency(order.shippingFee)}</div>
                        </div>
                    </div>
                </div>
                <div class="mb-3">
                    <div class="text-muted small">Khách hàng</div>
                    <div class="fw-semibold">${escapeHtml(order.customerName || order.customerEmail || 'Ẩn danh')}</div>
                    <div class="text-muted small">Email: ${escapeHtml(order.customerEmail || 'Chưa rõ')}</div>
                    <div class="text-muted small">Ngày đặt: ${formatDateTime(order.orderDate)}</div>
                    <div class="text-muted small">Thanh toán: ${escapeHtml(order.paymentMethod || 'cod')} · ${escapeHtml(order.paymentStatus || '')}</div>
                </div>
                <div class="mb-3">
                    <h4 class="h6 mb-2">Sản phẩm (${items.length})</h4>
                    <div class="border rounded p-3 bg-light">${itemsHtml}</div>
                </div>
                <div>
                    <h4 class="h6 mb-2">Tiến trình trạng thái</h4>
                    ${timelineHtml}
                </div>`;
            footer.hidden = false;
            document.getElementById('statusSelect').value = normalizeKey(order.status) || '';
            document.getElementById('noteInput').value = '';
        }

        function renderTimeline(timeline) {
            if (!Array.isArray(timeline) || timeline.length === 0) {
                return '<p class="text-muted">Chưa có lịch sử trạng thái.</p>';
            }
            return `<div class="timeline">${timeline.map(entry => {
                const meta = STATUS_LABELS[normalizeKey(entry.status)] || { label: entry.status || 'N/A', badge: 'secondary' };
                const note = entry.note ? escapeHtml(entry.note) : 'Không có ghi chú';
                return `
                    <div class="timeline-item">
                        <div class="d-flex justify-content-between">
                            <span class="fw-semibold">${escapeHtml(meta.label)}</span>
                            <span class="text-muted small">${formatDateTime(entry.createdAt)}</span>
                        </div>
                        <div class="text-muted small">${note}</div>
                        <div class="text-muted small fst-italic">Thực hiện bởi: ${escapeHtml(entry.createdBy || 'Hệ thống')}</div>
                    </div>`;
            }).join('')}</div>`;
        }

        function resetDetail() {
            state.selectedOrderId = null;
            state.selectedOrder = null;
            state.timeline = [];
            renderOrders();
            document.getElementById('detailBody').innerHTML = '<div class="text-center py-4 text-muted"><p>Chọn một đơn hàng từ danh sách để xem chi tiết.</p></div>';
            document.getElementById('updateFooter').hidden = true;
            document.getElementById('detailStatusBadge').innerHTML = '';
        }

        async function onSubmitStatusUpdate(event) {
            event.preventDefault();
            if (!state.selectedOrderId) {
                return;
            }
            const status = document.getElementById('statusSelect').value;
            if (!status) {
                alert('Vui lòng chọn trạng thái cần cập nhật.');
                return;
            }
            const note = document.getElementById('noteInput').value;
            const button = document.getElementById('updateButton');
            button.disabled = true;
            button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang lưu...';
            try {
                const response = await fetch(`${contextPath}/api/admin/orders`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ action: 'update-status', orderId: state.selectedOrderId, status, note })
                });
                const data = await response.json();
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'Không thể cập nhật trạng thái');
                }
                if (data.order) {
                    state.selectedOrder = data.order;
                    state.selectedOrderId = data.order.id;
                }
                state.timeline = Array.isArray(data.timeline) ? data.timeline : state.timeline;
                await loadOrders();
                renderDetail();
                document.getElementById('noteInput').value = '';
            } catch (error) {
                console.error('update status error', error);
                alert(error.message || 'Không thể cập nhật trạng thái đơn hàng');
            } finally {
                button.disabled = false;
                button.innerHTML = '<i class="fas fa-save me-2"></i>Lưu trạng thái';
            }
        }

        function normalizeKey(value) {
            if (!value) { return ''; }
            return String(value).trim().toLowerCase();
        }

        function escapeHtml(value) {
            if (value === null || value === undefined) { return ''; }
            return String(value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function normalizeToDate(value) {
            if (!value) { return null; }
            if (value instanceof Date) { return Number.isNaN(value.getTime()) ? null : value; }
            if (typeof value === 'string' || typeof value === 'number') {
                const parsed = new Date(value);
                return Number.isNaN(parsed.getTime()) ? null : parsed;
            }
            if (Array.isArray(value)) {
                const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = value;
                if (Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day)) {
                    const parsed = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1e6));
                    return Number.isNaN(parsed.getTime()) ? null : parsed;
                }
                return null;
            }
            if (typeof value === 'object') {
                const dateSource = value.date && typeof value.date === 'object' ? value.date : value;
                const timeSource = value.time && typeof value.time === 'object' ? value.time : value;
                const resolveMonth = input => {
                    if (Number.isFinite(input)) { return input; }
                    if (typeof input === 'string') {
                        const names = ['JANUARY','FEBRUARY','MARCH','APRIL','MAY','JUNE','JULY','AUGUST','SEPTEMBER','OCTOBER','NOVEMBER','DECEMBER'];
                        const upper = input.toUpperCase();
                        const index = names.indexOf(upper);
                        if (index !== -1) { return index + 1; }
                        const parsed = parseInt(upper, 10);
                        if (Number.isFinite(parsed)) { return parsed; }
                    }
                    return null;
                };
                const toNumber = input => (Number.isFinite(input) ? input : parseInt(input, 10));
                const year = toNumber(dateSource.year);
                const month = resolveMonth(dateSource.monthValue ?? dateSource.month);
                const day = toNumber(dateSource.day ?? dateSource.dayOfMonth);
                if (Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day)) {
                    const hour = toNumber(timeSource.hour) || 0;
                    const minute = toNumber(timeSource.minute) || 0;
                    const second = toNumber(timeSource.second) || 0;
                    const nano = toNumber(timeSource.nano) || 0;
                    const parsed = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1e6));
                    return Number.isNaN(parsed.getTime()) ? null : parsed;
                }
            }
            return null;
        }

        function formatDateTime(value) {
            const date = normalizeToDate(value);
            return date ? date.toLocaleString('vi-VN') : '';
        }

        function formatCurrency(value) {
            const number = Number(value);
            if (!Number.isFinite(number)) { return '0đ'; }
            return number.toLocaleString('vi-VN') + 'đ';
        }
    </script>
</body>
</html>
