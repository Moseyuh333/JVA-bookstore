document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") {
        feather.replace();
    }

    const contextPath = window.appConfig?.contextPath || "";
    const tableBody = document.getElementById("orderList");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");
    const totalOrdersEl = document.getElementById("totalOrders");
    const deliveredOrdersEl = document.getElementById("deliveredOrders");
    const searchInput = document.getElementById("searchInput");
    const searchTypeSelect = document.getElementById("searchType");
    const detailContent = document.getElementById("orderDetailContent");

    const state = {
        statusFilter: "",
        keyword: "",
        searchType: searchTypeSelect ? searchTypeSelect.value : "all",
    };

    const getAuthToken = () =>
        localStorage.getItem("seller_token") ||
        localStorage.getItem("auth_token") ||
        "";

    const authHeaders = () => {
        const token = getAuthToken();
        return token ? { Authorization: `Bearer ${token}` } : {};
    };

    const toggleDisplay = (element, show, displayValue = "block") => {
        if (!element) {
            return;
        }
        element.style.display = show ? displayValue : "none";
    };

    const showLoading = () => {
        toggleDisplay(loadingState, true, "flex");
        toggleDisplay(tableContainer, false);
        toggleDisplay(emptyState, false);
    };

    const showTable = () => {
        toggleDisplay(loadingState, false);
        toggleDisplay(tableContainer, true);
        toggleDisplay(emptyState, false);
    };

    const showEmpty = () => {
        toggleDisplay(loadingState, false);
        toggleDisplay(tableContainer, false);
        toggleDisplay(emptyState, true);
    };

    const escapeHtml = (value) => {
        if (value === null || value === undefined) {
            return "";
        }
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    };

    const formatCurrency = (value) => {
        if (value === null || value === undefined) {
            return "0₫";
        }
        const number = Number(value);
        if (Number.isNaN(number)) {
            return `${value}₫`;
        }
        const rounded = Math.round(number);
        return `${new Intl.NumberFormat("vi-VN").format(rounded)}₫`;
    };

    const formatDate = (value) => {
        if (!value) {
            return "-";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleDateString("vi-VN");
    };

    const getStatusBadge = (status) => {
        const normalized = (status || "").toLowerCase();
        const map = {
            new: { label: "Mới", cls: "badge-info" },
            confirmed: { label: "Đã xác nhận", cls: "badge-primary" },
            shipping: { label: "Đang giao", cls: "badge-warning" },
            delivered: { label: "Đã giao", cls: "badge-success" },
            cancelled: { label: "Đã hủy", cls: "badge-danger" },
            failed: { label: "Giao thất bại", cls: "badge-danger" },
            returned: { label: "Đã trả", cls: "badge-secondary" },
        };
        const info = map[normalized] || { label: status || "-", cls: "badge-light" };
        return `<span class="badge ${info.cls}">${info.label}</span>`;
    };

    const buildQuery = () => {
        const params = new URLSearchParams({ action: "list", limit: "50" });
        if (state.statusFilter) {
            params.append("status", state.statusFilter);
        }
        if (state.keyword) {
            params.append("keyword", state.keyword);
            params.append("searchType", state.searchType || "all");
        }
        return params.toString();
    };

    const handleUnauthorized = () => {
        alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        window.location.href = `${contextPath}/login.jsp`;
    };

    const fetchJson = async (url, options = {}) => {
        const response = await fetch(url, {
            ...options,
            headers: {
                ...authHeaders(),
                ...(options.headers || {}),
            },
            credentials: "same-origin",
        });

        if (response.status === 401 || response.status === 403) {
            handleUnauthorized();
            throw new Error("Unauthorized");
        }

        if (!response.ok) {
            throw new Error(`Yêu cầu thất bại (${response.status})`);
        }

        return response.json();
    };

    const renderOrders = (orders) => {
        if (!tableBody) {
            return;
        }

        tableBody.innerHTML = "";

        orders.forEach((order) => {
            const row = document.createElement("tr");
            const orderCode = order.code || order.id;
            row.innerHTML = `
                <td>${escapeHtml(orderCode)}</td>
                <td>${escapeHtml(order.customerName || order.customerEmail || "-")}</td>
                <td>${formatCurrency(order.totalAmount)}</td>
                <td>${getStatusBadge(order.status)}</td>
                <td>${formatDate(order.orderDate || order.createdAt)}</td>
                <td>
                    <button class="btn btn-sm btn-primary mr-1" onclick="viewOrderDetail(${order.id})">
                        <i class="fas fa-eye"></i> Chi tiết
                    </button>
                    <button class="btn btn-sm btn-success" onclick="updateStatus(${order.id}, 'confirmed')">
                        <i class="fas fa-check"></i> Xác nhận
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });

        showTable();
    };

    const renderOrderDetail = (order) => {
        if (!detailContent) {
            return;
        }

        const address = order.shippingAddress || "Không có thông tin";
        const itemsHtml = (order.items || [])
            .map(
                (item) => `
                <tr>
                    <td>${escapeHtml(item.title)}</td>
                    <td>${item.quantity}</td>
                    <td>${formatCurrency(item.unitPrice)}</td>
                    <td>${formatCurrency(item.totalPrice)}</td>
                </tr>
            `
            )
            .join("");

        detailContent.innerHTML = `
            <div class="mb-3">
                <h5 class="mb-1">Mã đơn hàng: ${escapeHtml(order.code || order.id)}</h5>
                <div>Trạng thái: ${getStatusBadge(order.status)}</div>
                <div>Thanh toán: ${escapeHtml(order.paymentStatus || "-")} (${escapeHtml(order.paymentMethod || "COD")})</div>
                <div>Ngày đặt: ${formatDate(order.orderDate)}</div>
            </div>
            <div class="mb-3">
                <h6>Thông tin khách hàng</h6>
                <div>Tên: ${escapeHtml(order.customerName || "-")}</div>
                <div>Email: ${escapeHtml(order.customerEmail || "-")}</div>
                <div>Địa chỉ giao hàng: ${escapeHtml(address)}</div>
                <div>Ghi chú: ${escapeHtml(order.notes || "")}</div>
            </div>
            <div class="mb-3">
                <h6>Chi tiết sản phẩm</h6>
                <table class="table table-sm">
                    <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Đơn giá</th>
                            <th>Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${itemsHtml || `<tr><td colspan="4" class="text-center text-muted">Không có sản phẩm</td></tr>`}
                    </tbody>
                </table>
            </div>
            <div class="text-right">
                <div>Tạm tính: <strong>${formatCurrency(order.itemsSubtotal)}</strong></div>
                <div>Giảm giá: <strong>${formatCurrency(order.discountAmount)}</strong></div>
                <div>Phí vận chuyển: <strong>${formatCurrency(order.shippingFee)}</strong></div>
                <div class="h5 mt-2">Tổng cộng: <strong>${formatCurrency(order.totalAmount)}</strong></div>
            </div>
        `;
    };

    const loadOrders = async () => {
        showLoading();
        try {
            const data = await fetchJson(
                `${contextPath}/api/seller/orders?${buildQuery()}`
            );
            if (data.success && Array.isArray(data.orders) && data.orders.length > 0) {
                renderOrders(data.orders);
            } else {
                showEmpty();
            }
        } catch (error) {
            console.error("loadOrders error:", error);
            showEmpty();
            alert(error.message || "Không thể tải danh sách đơn hàng");
        }
    };

    const loadStats = async () => {
        try {
            const data = await fetchJson(
                `${contextPath}/api/seller/orders?action=stats`
            );
            if (data.success && data.stats) {
                if (totalOrdersEl) {
                    totalOrdersEl.textContent = data.stats.total ?? 0;
                }
                if (deliveredOrdersEl) {
                    deliveredOrdersEl.textContent = data.stats.delivered ?? 0;
                }
            }
        } catch (error) {
            console.warn("loadStats error:", error);
        }
    };

    window.applyFilters = () => {
        const keyword = searchInput ? searchInput.value.trim() : "";
        const searchType = searchTypeSelect ? searchTypeSelect.value : "all";

        state.searchType = searchType;
        if (searchType === "status") {
            state.statusFilter = keyword.toLowerCase();
            state.keyword = "";
        } else {
            state.statusFilter = "";
            state.keyword = keyword;
        }

        loadOrders();
    };

    window.resetFilters = () => {
        if (searchInput) {
            searchInput.value = "";
        }
        if (searchTypeSelect) {
            searchTypeSelect.value = "all";
        }
        state.searchType = searchTypeSelect ? searchTypeSelect.value : "all";
        state.statusFilter = "";
        state.keyword = "";
        loadOrders();
    };

    if (searchInput) {
        searchInput.addEventListener("keypress", (event) => {
            if (event.key === "Enter") {
                window.applyFilters();
            }
        });
    }

    window.viewOrderDetail = async (orderId) => {
        try {
            const data = await fetchJson(
                `${contextPath}/api/seller/orders?action=detail&order_id=${orderId}`
            );
            if (!data.success || !data.order) {
                throw new Error(data.message || "Không thể tải chi tiết đơn hàng");
            }
            renderOrderDetail(data.order);
            if (typeof $ !== "undefined") {
                $("#orderDetailModal").modal("show");
            }
        } catch (error) {
            console.error("viewOrderDetail error:", error);
            alert(error.message || "Không thể tải chi tiết đơn hàng");
        }
    };

    window.updateStatus = async (orderId, status) => {
        if (!confirm(`Xác nhận cập nhật trạng thái đơn #${orderId}?`)) {
            return;
        }

        try {
            const body = new URLSearchParams({
                action: "update_status",
                order_id: orderId,
                status,
            });

            const data = await fetchJson(`${contextPath}/api/seller/orders`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                },
                body: body.toString(),
            });

            if (!data.success) {
                throw new Error(data.message || "Không thể cập nhật trạng thái");
            }

            await loadOrders();
            await loadStats();
            alert("Cập nhật trạng thái thành công!");
        } catch (error) {
            console.error("updateStatus error:", error);
            alert(error.message || "Không thể cập nhật trạng thái");
        }
    };

    loadOrders();
    loadStats();
});
