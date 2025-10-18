document.addEventListener("DOMContentLoaded", function() {
    // Revenue Bar Chart
    const ctxRevenue = document.getElementById("revenueChart");
    if (ctxRevenue) {
        new Chart(ctxRevenue, {
            type: "bar",
            data: {
                labels: ["Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10"],
                datasets: [{
                    label: "Doanh thu (Triệu VNĐ)",
                    data: [45, 52, 60, 70, 85, 90],
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
                        max: 100,
                        ticks: {
                            callback: function(v) { return v + "M"; }
                        }
                    }
                }
            }
        });
    }

    // Order Status Pie Chart
    const ctxStatus = document.getElementById("statusChart");
    if (ctxStatus) {
        new Chart(ctxStatus, {
            type: "doughnut",
            data: {
                labels: ["Hoàn thành", "Đang xử lý", "Hủy"],
                datasets: [{
                    data: [320, 450, 109],
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
});