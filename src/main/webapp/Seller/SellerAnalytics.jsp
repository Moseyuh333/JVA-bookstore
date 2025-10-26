<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Phân tích Bán hàng - ${username}</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    
    <%-- Thư viện biểu đồ (Cần thiết) --%>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.9.4/dist/Chart.min.js"></script>
    <style>
        /* ... (CSS tùy chỉnh) ... */
        .analytics-card { background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 25px; margin-bottom: 20px; }
        .chart-container { position: relative; height: 40vh; width: 100%; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <h1><i class="fas fa-chart-line mr-2"></i>Phân tích Bán hàng</h1>
        <p class="text-muted">Phân tích hiệu suất của Shop ID: <strong>${shopId}</strong></p>

        <%-- Tổng quan số liệu --%>
        <div class="row mb-4" id="statsSummary">
            <div class="col-md-4"><div class="analytics-card"><h5>Doanh thu Tháng này</h5><h3 id="currentRevenue">0đ</h3></div></div>
            <div class="col-md-4"><div class="analytics-card"><h5>Tổng Đơn hàng</h5><h3 id="totalOrders">0</h3></div></div>
            <div class="col-md-4"><div class="analytics-card"><h5>Sản phẩm Bán chạy nhất</h5><h3 id="bestSeller">--</h3></div></div>
        </div>

        <%-- Biểu đồ Doanh thu --%>
        <div class="analytics-card">
            <h2>Doanh thu 7 Ngày gần nhất</h2>
            <div class="chart-container">
                <canvas id="revenueChart"></canvas>
            </div>
        </div>
        
        <%-- Biểu đồ Top Sản phẩm --%>
        <div class="analytics-card">
            <h2>Top 5 Sản phẩm Bán chạy</h2>
            <div class="chart-container" style="height: 30vh;">
                <canvas id="topProductsChart"></canvas>
            </div>
        </div>
    </div>
    
    <script>
        // const API_URL = '<%= request.getContextPath() %>/api/seller/analytics';
        // const SHOP_ID = ${shopId};

        const API_URL = '<%= request.getContextPath() %>/api/seller/profile';

    // SỬA DÒNG 4: Sử dụng JSTL c:out để đảm bảo giá trị shopId luôn là chuỗi (hoặc số) an toàn
        const SHOP_ID = '<c:out value="${shopId}" default="0" />';

        // Hàm vẽ biểu đồ
        function drawChart(elementId, type, labels, data, label) {
            const ctx = document.getElementById(elementId).getContext('2d');
            new Chart(ctx, {
                type: type,
                data: {
                    labels: labels,
                    datasets: [{
                        label: label,
                        data: data,
                        backgroundColor: type === 'bar' ? 'rgba(146, 64, 14, 0.6)' : 'rgba(146, 64, 14, 0.8)',
                        borderColor: type === 'line' ? 'rgb(146, 64, 14)' : undefined,
                        borderWidth: 1,
                        fill: type === 'line' ? false : true,
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
        }

        async function loadAnalyticsData() {
            if (SHOP_ID === 0) return;
            try {
                // Giả định API /api/seller/analytics?action=summary trả về tất cả data
                const response = await fetch(`${API_URL}?action=summary&shop_id=${SHOP_ID}`, { headers: { 'Authorization': `Bearer ${localStorage.getItem('seller_token')}` } });
                const data = await response.json();
                
                if (data.success && data.summary) {
                    const summary = data.summary;
                    document.getElementById('currentRevenue').textContent = formatCurrency(summary.monthlyRevenue);
                    document.getElementById('totalOrders').textContent = summary.totalOrders;
                    document.getElementById('bestSeller').textContent = summary.bestSellerTitle || 'Chưa có';

                    // 1. Biểu đồ Doanh thu (dữ liệu giả định)
                    const revenueLabels = summary.dailySales.map(d => d.date);
                    const revenueData = summary.dailySales.map(d => d.revenue);
                    drawChart('revenueChart', 'line', revenueLabels, revenueData, 'Doanh thu');

                    // 2. Biểu đồ Top Sản phẩm (dữ liệu giả định)
                    const productLabels = summary.topProducts.map(p => p.title);
                    const productData = summary.topProducts.map(p => p.sold);
                    drawChart('topProductsChart', 'bar', productLabels, productData, 'Số lượng bán');

                } else {
                    console.error('Không thể tải dữ liệu phân tích:', data.message);
                }

            } catch (error) {
                console.error('Lỗi mạng/API:', error);
            }
        }

        function formatCurrency(value) {
            const number = Number(value);
            return number.toLocaleString('vi-VN') + 'đ';
        }

        document.addEventListener('DOMContentLoaded', loadAnalyticsData);
    </script>
</body>
</html>