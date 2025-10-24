document.addEventListener("DOMContentLoaded", () => {
    console.log("✓ SellerOrders.js loaded");

    const contextPath = window.appConfig?.contextPath || "";
    const orderList = document.getElementById("orderList") || document.getElementById("categoryList"); // Fallback to categoryList
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let orders = [];

    // Get auth token from localStorage
    const getAuthToken = () => {
        return localStorage.getItem("auth_token") || localStorage.getItem("seller_token");
    };

    const getAuthHeaders = () => {
        const token = getAuthToken();
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    };

    // API functions
    const api = {
        listOrders: (status = null, keyword = null) => {
            let url = `${contextPath}/api/seller/orders?action=list&limit=50`;
            if (status) url += `&status=${encodeURIComponent(status)}`;
            if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
            
            return fetch(url, {
                headers: getAuthHeaders()
            }).then(r => {
                if (r.status === 401 || r.status === 403) {
                    alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                    window.location.href = contextPath + "/login.jsp";
                    throw new Error("Unauthorized");
                }
                return r.json();
            });
        },
        
        getOrderStats: () => {
            return fetch(`${contextPath}/api/seller/orders?action=stats`, {
                headers: getAuthHeaders()
            }).then(r => {
                if (r.status === 401 || r.status === 403) {
                    alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!");
                    window.location.href = contextPath + "/login.jsp";
                    throw new Error("Unauthorized");
                }
                return r.json();
            });
        },
        
        updateOrderStatus: (orderId, status, note = "") => {
            return fetch(`${contextPath}/api/seller/orders?action=update_status`, {
                method: 'POST',
                headers: {
                    ...getAuthHeaders(),
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({
                    order_id: orderId,
                    status: status,
                    note: note
                })
            }).then(r => r.json());
        }
    };

    // Utility functions
    const escapeHtml = (text) => {
        if (!text) return "";
        return String(text).replace(/[&<>"']/g, (m) => {
            const map = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" };
            return map[m];
        });
    };

    const formatCurrency = (amount) => {
        if (!amount) return "0₫";
        return new Intl.NumberFormat("vi-VN").format(amount) + "₫";
    };

    const formatDate = (dateString) => {
        if (!dateString) return "-";
        const date = new Date(dateString);
        return date.toLocaleDateString("vi-VN");
    };

    const getStatusBadge = (status) => {
        const statusMap = {
            'new': { text: 'Mới', class: 'badge-info' },
            'confirmed': { text: 'Đã xác nhận', class: 'badge-primary' },
            'shipping': { text: 'Đang giao', class: 'badge-warning' },
            'delivered': { text: 'Đã giao', class: 'badge-success' },
            'cancelled': { text: 'Đã hủy', class: 'badge-danger' },
            'returned': { text: 'Đã trả', class: 'badge-secondary' }
        };
        
        const info = statusMap[status] || { text: status, class: 'badge-light' };
        return `<span class="badge ${info.class}">${info.text}</span>`;
    };

    // Show/hide states
    const showLoading = () => {
        if (loadingState) loadingState.style.display = "block";
        if (tableContainer) tableContainer.style.display = "none";
        if (emptyState) emptyState.style.display = "none";
    };

    const hideLoading = () => {
        if (loadingState) loadingState.style.display = "none";
        if (tableContainer) tableContainer.style.display = "block";
    };

    const showEmpty = () => {
        if (emptyState) emptyState.style.display = "block";
        if (tableContainer) tableContainer.style.display = "none";
    };

    // Render orders table
    const renderOrders = (ordersList) => {
        if (!orderList) {
            console.error("Order list container not found");
            return;
        }

        orderList.innerHTML = "";
        
        if (!ordersList || ordersList.length === 0) {
            showEmpty();
            return;
        }

        hideLoading();

        ordersList.forEach(order => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${escapeHtml(order.id || order.orderId || "-")}</td>
                <td>${escapeHtml(order.customerName || order.customer_name || "-")}</td>
                <td>${formatCurrency(order.totalAmount || order.total_amount)}</td>
                <td>${getStatusBadge(order.status)}</td>
                <td>${formatDate(order.createdAt || order.created_at)}</td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="viewOrderDetail(${order.id || order.orderId})">
                        <i class="fas fa-eye"></i> Chi tiết
                    </button>
                    <button class="btn btn-sm btn-success" onclick="updateStatus(${order.id || order.orderId}, 'confirmed')">
                        <i class="fas fa-check"></i> Xác nhận
                    </button>
                </td>
            `;
            orderList.appendChild(row);
        });
    };

    // Load orders
    const loadOrders = async () => {
        try {
            showLoading();
            const data = await api.listOrders();
            
            if (data.success && data.orders) {
                orders = data.orders;
                renderOrders(orders);
            } else {
                console.error("Failed to load orders:", data);
                showEmpty();
            }
        } catch (error) {
            console.error("Error loading orders:", error);
            showEmpty();
        }
    };

    // Load stats
    const loadStats = async () => {
        try {
            const data = await api.getOrderStats();
            
            if (data.success && data.stats) {
                const stats = data.stats;
                document.getElementById("totalCategories").textContent = stats.total || 0;
                document.getElementById("activeCategories").textContent = stats.new || 0;
            }
        } catch (error) {
            console.error("Error loading stats:", error);
        }
    };

    // Global functions
    window.viewOrderDetail = (orderId) => {
        alert(`Xem chi tiết đơn hàng #${orderId}`);
        // TODO: Implement order detail view
    };

    window.updateStatus = async (orderId, status) => {
        if (!confirm(`Xác nhận cập nhật trạng thái đơn hàng #${orderId}?`)) return;
        
        try {
            const data = await api.updateOrderStatus(orderId, status);
            if (data.success) {
                alert("Cập nhật trạng thái thành công!");
                loadOrders(); // Reload orders
            } else {
                alert("Lỗi: " + (data.message || "Không thể cập nhật"));
            }
        } catch (error) {
            console.error("Error updating status:", error);
            alert("Có lỗi xảy ra khi cập nhật trạng thái");
        }
    };

    window.applyFilters = () => {
        const keyword = document.getElementById("searchInput")?.value || "";
        loadOrders(null, keyword);
    };

    window.resetFilters = () => {
        if (document.getElementById("searchInput")) {
            document.getElementById("searchInput").value = "";
        }
        loadOrders();
    };

    // Initialize
    loadOrders();
    loadStats();
});
