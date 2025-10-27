document.addEventListener("DOMContentLoaded", function() {
    if (typeof feather !== 'undefined') {
        feather.replace();
    }

    const contextPath = window.appConfig?.contextPath || "";

    function getAdminToken() {
        const adminToken = localStorage.getItem("admin_token");
        if (adminToken && adminToken.trim().length > 0) {
            return adminToken.trim();
        }
        const authToken = localStorage.getItem("auth_token");
        if (authToken && authToken.trim().length > 0) {
            return authToken.trim();
        }
        return null;
    }

    function handleUnauthorized() {
        alert("Phiên đăng nhập quản trị đã hết hạn. Vui lòng đăng nhập lại.");
        localStorage.removeItem("admin_token");
        localStorage.removeItem("admin_username");
        const loginPath = contextPath + "/login.jsp";
        window.location.href = loginPath;
    }

    // Load dashboard data from API
    loadDashboardData();

    async function loadDashboardData() {
        try {
            const token = getAdminToken();
            if (!token) {
                handleUnauthorized();
                return;
            }

            const response = await fetch(`${contextPath}/api/admin/dashboard`, {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`
                }
            });

            if (response.status === 401) {
                handleUnauthorized();
                return;
            }

            const data = await response.json();

            if (response.ok && data.success) {
                updateStats(data.stats || {});
                updateRevenueChart(data.revenue || {});
                updateOrderStatusChart(data.orderStatus || {});
                updateTopSellers(data.topSellers || {});
            } else {
                console.error("Failed to load dashboard data:", data.error || response.statusText);
            }
        } catch (error) {
            console.error("Error loading dashboard data:", error);
        }
    }

    function updateStats(stats) {
        // Update stat cards with real data
        const userEl = document.querySelector(".stat-card.users .stat-number");
        const productEl = document.querySelector(".stat-card.products .stat-number");
        const orderEl = document.querySelector(".stat-card.orders .stat-number");
        const revenueEl = document.querySelector(".stat-card.revenue .stat-number");

        if (userEl) userEl.textContent = stats.totalUsers || 0;
        if (productEl) productEl.textContent = stats.totalProducts || 0;
        if (orderEl) orderEl.textContent = stats.totalOrders || 0;
        if (revenueEl) revenueEl.textContent = formatCurrency(stats.totalRevenue || 0);
    }

    function formatCurrency(amount) {
        if (amount >= 1000000) {
            return (amount / 1000000).toFixed(1) + "M";
        } else if (amount >= 1000) {
            return (amount / 1000).toFixed(0) + "K";
        }
        return amount.toString();
    }

    function updateRevenueChart(revenueData) {
        const ctxRevenue = document.getElementById("revenueChart");
        if (!ctxRevenue) return;

        const labels = Object.values(revenueData.labels || {});
        const data = Object.values(revenueData.data || {});

        new Chart(ctxRevenue, {
            type: "bar",
            data: {
                labels: labels.length > 0 ? labels : [],
                datasets: [{
                    label: "Doanh thu (VNĐ)",
                    data: data.length > 0 ? data.map(v => v / 1000000) : [],
                    backgroundColor: [
                        "rgba(245, 158, 11, 0.8)",
                        "rgba(245, 158, 11, 0.7)",
                        "rgba(245, 158, 11, 0.8)",
                        "rgba(245, 158, 11, 0.7)",
                        "rgba(245, 158, 11, 0.8)",
                        "rgba(245, 158, 11, 0.9)"
                    ],
                    borderRadius: 8,
                    borderSkipped: false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            callback: function(v) { return v + "M"; }
                        }
                    }
                }
            }
        });
    }

    function updateOrderStatusChart(orderStatusData) {
        const ctxStatus = document.getElementById("statusChart");
        if (!ctxStatus) return;

        const labels = Object.values(orderStatusData.labels || {});
        const data = Object.values(orderStatusData.data || {});

        new Chart(ctxStatus, {
            type: "doughnut",
            data: {
                labels: labels.length > 0 ? labels : [],
                datasets: [{
                    data: data.length > 0 ? data : [],
                    backgroundColor: [
                        "#10b981",
                        "#f59e0b",
                        "#ef4444"
                    ],
                    borderColor: "white",
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: "bottom",
                        labels: {
                            padding: 15,
                            font: { size: 13, weight: 600 },
                            usePointStyle: true
                        }
                    }
                }
            }
        });
    }

    function updateTopSellers(topSellersData) {
        const tbody = document.querySelector(".table-custom tbody");
        if (!tbody) return;

    const sellers = topSellersData.sellers || {};

        // Clear existing rows
        tbody.innerHTML = "";

        // Add new rows
        Object.values(sellers).forEach((seller, index) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td><span class="rank-badge rank-${index + 1}">${index + 1}</span></td>
                <td><strong>${seller.store_name || "N/A"}</strong></td>
                <td>${seller.total_orders || 0}</td>
                <td><strong>${formatCurrency(seller.revenue || 0)}₫</strong></td>
                <td><span class="badge-percentage">${seller.commission_rate ?? 0}%</span></td>
                <td><span class="badge-percentage">Hoạt động</span></td>
            `;
            tbody.appendChild(row);
        });

        // If no data, show empty state
        if (Object.keys(sellers).length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; padding: 40px; color: #718096;">
                        <i class="fas fa-store fa-2x" style="margin-bottom: 10px; display: block;"></i>
                        Chưa có dữ liệu cửa hàng
                    </td>
                </tr>
            `;
        }
    }
});
