// Dữ liệu mẫu cho biểu đồ doanh thu
const ctx = document.getElementById("revenueChart").getContext("2d");
new Chart(ctx, {
    type: "bar",
    data: {
        labels: ["Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10"],
        datasets: [{
            label: "Doanh thu (VNĐ)",
            data: [45, 52, 60, 70, 85, 90],
            backgroundColor: "#f59e0b",
            borderRadius: 6,
        }]
    },
    options: {
        plugins: {
            legend: { display: false }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: { callback: value => value + " triệu" }
            }
        }
    }
});