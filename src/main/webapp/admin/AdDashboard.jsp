<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Bookish Admin</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Chart.js -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/3.9.1/chart.min.js"></script>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
            font-family: 'Roboto', sans-serif;
            min-height: 100vh;
        }

        #wrapper {
            display: flex;
            min-height: 100vh;
        }

        #content-wrapper {
            flex: 1;
            margin-left: 0;
            margin-top: 0;
            padding: 30px;
            transition: margin-left 0.3s ease;
        }

        .container-fluid {
            max-width: 1400px;
            margin: 0 auto;
        }

        .page-header {
            margin-bottom: 30px;
        }

        .page-header h1 {
            font-size: 32px;
            font-weight: 700;
            color: #1a202c;
            margin: 0 0 8px 0;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .page-header p {
            font-size: 14px;
            color: #718096;
            margin: 0;
        }

        /* Stat Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 24px;
            margin-bottom: 30px;
        }

        .stat-card {
            background: white;
            border-radius: 16px;
            padding: 24px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            border: 1px solid rgba(0, 0, 0, 0.05);
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;
        }

        .stat-card::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 4px;
            background: linear-gradient(90deg, #f59e0b, #fbbf24);
        }

        .stat-card:hover {
            box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
            transform: translateY(-4px);
        }

        .stat-card.users::before { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
        .stat-card.products::before { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
        .stat-card.orders::before { background: linear-gradient(90deg, #10b981, #34d399); }
        .stat-card.revenue::before { background: linear-gradient(90deg, #ef4444, #f87171); }

        .stat-content {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
        }

        .stat-info h6 {
            font-size: 13px;
            font-weight: 600;
            color: #718096;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .stat-number {
            font-size: 32px;
            font-weight: 700;
            color: #1a202c;
            line-height: 1;
        }

        .stat-icon {
            width: 60px;
            height: 60px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 28px;
            flex-shrink: 0;
        }

        .stat-card.users .stat-icon { background: #fef3c7; color: #f59e0b; }
        .stat-card.products .stat-icon { background: #dbeafe; color: #3b82f6; }
        .stat-card.orders .stat-icon { background: #d1fae5; color: #10b981; }
        .stat-card.revenue .stat-icon { background: #fee2e2; color: #ef4444; }

        /* Charts Section */
        .charts-grid {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 24px;
            margin-bottom: 30px;
        }

        .chart-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            border: 1px solid rgba(0, 0, 0, 0.05);
            overflow: hidden;
        }

        .chart-header {
            padding: 20px 24px;
            border-bottom: 1px solid #e5e7eb;
            background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
            color: white;
        }

        .chart-header h6 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .chart-body {
            padding: 24px;
            position: relative;
            height: 350px;
        }

        .chart-body-small {
            height: auto;
        }

        /* Table Section */
        .table-card {
            background: white;
            border-radius: 16px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
            border: 1px solid rgba(0, 0, 0, 0.05);
            overflow: hidden;
        }

        .table-header {
            padding: 20px 24px;
            border-bottom: 1px solid #e5e7eb;
            background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
            color: white;
        }

        .table-header h6 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .table-body {
            padding: 0;
            overflow-x: auto;
        }

        .table-custom {
            width: 100%;
            margin: 0;
            border-collapse: collapse;
        }

        .table-custom thead {
            background: #fafafa;
        }

        .table-custom th {
            padding: 14px 16px;
            text-align: left;
            font-size: 12px;
            font-weight: 700;
            color: #4b5563;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 2px solid #e5e7eb;
        }

        .table-custom td {
            padding: 14px 16px;
            font-size: 14px;
            color: #1a202c;
            border-bottom: 1px solid #f3f4f6;
        }

        .table-custom tbody tr:hover {
            background: #fafafa;
        }

        .rank-badge {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            color: white;
            font-size: 14px;
        }

        .rank-1 { background: linear-gradient(135deg, #fbbf24, #f59e0b); }
        .rank-2 { background: linear-gradient(135deg, #e5e7eb, #9ca3af); }
        .rank-3 { background: linear-gradient(135deg, #fed7aa, #fdba74); }

        .badge-percentage {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 6px;
            background: #fef3c7;
            color: #92400e;
            font-size: 12px;
            font-weight: 600;
        }

        /* Responsive */
        @media (max-width: 1200px) {
            .charts-grid {
                grid-template-columns: 1fr;
            }
        }

        @media (max-width: 768px) {
            #content-wrapper {
                padding: 16px;
                margin-top: 60px;
            }

            .stats-grid {
                grid-template-columns: 1fr;
                gap: 16px;
            }

            .page-header h1 {
                font-size: 24px;
            }

            .chart-body {
                height: 250px;
            }

            .table-body {
                font-size: 12px;
            }

            .table-custom td {
                padding: 10px;
            }
        }
    </style>
</head>
<body>

<div id="wrapper">
    <%@ include file="/WEB-INF/includes/admin/AdSideBar.jsp" %>

    <div id="content-wrapper">
        <%@ include file="/WEB-INF/includes/admin/header.jsp" %>

        <div class="container-fluid">
            <!-- Page Header -->
            <div class="page-header">
                <h1>
                    <i class="fas fa-chart-line" style="color: #f59e0b;"></i>
                    Dashboard tổng quan
                </h1>
                <p>Tổng hợp thông tin kinh doanh từ hệ thống</p>
            </div>

            <!-- Stats Grid -->
            <div class="stats-grid">
                <div class="stat-card users">
                    <div class="stat-content">
                        <div class="stat-info">
                            <h6>Người dùng</h6>
                            <div class="stat-number" id="userCount">1,254</div>
                        </div>
                        <div class="stat-icon">
                            <i class="fas fa-users"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card products">
                    <div class="stat-content">
                        <div class="stat-info">
                            <h6>Sản phẩm</h6>
                            <div class="stat-number" id="productCount">3,640</div>
                        </div>
                        <div class="stat-icon">
                            <i class="fas fa-book"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card orders">
                    <div class="stat-content">
                        <div class="stat-info">
                            <h6>Đơn hàng</h6>
                            <div class="stat-number" id="orderCount">879</div>
                        </div>
                        <div class="stat-icon">
                            <i class="fas fa-shopping-cart"></i>
                        </div>
                    </div>
                </div>

                <div class="stat-card revenue">
                    <div class="stat-content">
                        <div class="stat-info">
                            <h6>Doanh thu (VNĐ)</h6>
                            <div class="stat-number" id="revenueCount">356M</div>
                        </div>
                        <div class="stat-icon">
                            <i class="fas fa-dollar-sign"></i>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Charts Grid -->
            <div class="charts-grid">
                <!-- Revenue Chart -->
                <div class="chart-card">
                    <div class="chart-header">
                        <h6>
                            <i class="fas fa-chart-bar"></i>
                            Doanh thu 6 tháng gần nhất
                        </h6>
                    </div>
                    <div class="chart-body">
                        <canvas id="revenueChart"></canvas>
                    </div>
                </div>

                <!-- Order Status -->
                <div class="chart-card">
                    <div class="chart-header">
                        <h6>
                            <i class="fas fa-chart-pie"></i>
                            Trạng thái đơn hàng
                        </h6>
                    </div>
                    <div class="chart-body chart-body-small">
                        <canvas id="statusChart" height="180"></canvas>
                    </div>
                </div>
            </div>

            <!-- Top Sellers Table -->
            <div class="table-card">
                <div class="table-header">
                    <h6>
                        <i class="fas fa-store"></i>
                        Top cửa hàng bán chạy
                    </h6>
                </div>
                <div class="table-body">
                    <table class="table-custom">
                        <thead>
                            <tr>
                                <th>Xếp hạng</th>
                                <th>Tên cửa hàng</th>
                                <th>Tổng đơn hàng</th>
                                <th>Doanh thu</th>
                                <th>Chiết khấu</th>
                                <th>Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td><span class="rank-badge rank-1">1</span></td>
                                <td><strong>BookHaven</strong></td>
                                <td>254</td>
                                <td><strong>92,000,000₫</strong></td>
                                <td><span class="badge-percentage">10%</span></td>
                                <td><span class="badge-percentage">Hoạt động</span></td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge rank-2">2</span></td>
                                <td><strong>MangaWorld</strong></td>
                                <td>187</td>
                                <td><strong>68,500,000₫</strong></td>
                                <td><span class="badge-percentage">15%</span></td>
                                <td><span class="badge-percentage">Hoạt động</span></td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge rank-3">3</span></td>
                                <td><strong>LightNovelVN</strong></td>
                                <td>143</td>
                                <td><strong>54,000,000₫</strong></td>
                                <td><span class="badge-percentage">12%</span></td>
                                <td><span class="badge-percentage">Hoạt động</span></td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge" style="background: linear-gradient(135deg, #d1d5db, #6b7280);">4</span></td>
                                <td><strong>NovelCorner</strong></td>
                                <td>98</td>
                                <td><strong>32,100,000₫</strong></td>
                                <td><span class="badge-percentage">8%</span></td>
                                <td><span class="badge-percentage">Hoạt động</span></td>
                            </tr>
                            <tr>
                                <td><span class="rank-badge" style="background: linear-gradient(135deg, #d1d5db, #6b7280);">5</span></td>
                                <td><strong>StorybookHub</strong></td>
                                <td>76</td>
                                <td><strong>24,200,000₫</strong></td>
                                <td><span class="badge-percentage">10%</span></td>
                                <td><span class="badge-percentage">Hoạt động</span></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <%@ include file="/WEB-INF/includes/admin/footer.jsp" %>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/admin/AdDashboard.js"></script>

</body>
</html>