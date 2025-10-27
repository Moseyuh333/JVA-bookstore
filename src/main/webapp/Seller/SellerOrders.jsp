<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý đơn hàng - Bookish Admin</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Feather Icons -->
    <script src="https://unpkg.com/feather-icons"></script>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap&subset=vietnamese" rel="stylesheet">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: #f5f5f5;
            font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
        }

        #wrapper {
            display: flex;
            min-height: 100vh;
        }

        #content-wrapper {
            flex: 1;
            margin-left: 0;
            transition: margin-left 0.3s ease;
        }

        #content {
            margin-top: 70px;
            padding: 24px;
        }

        .container-fluid {
            max-width: 1400px;
            margin: 0 auto;
        }

        .page-title {
            margin-bottom: 24px;
        }

        .page-title h1 {
            font-size: 28px;
            font-weight: 700;
            color: #1a202c;
            margin: 0 0 8px 0;
        }

        .page-title p {
            font-size: 14px;
            color: #718096;
            margin: 0;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }

        .stat-box {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            display: flex;
            align-items: center;
            gap: 16px;
            transition: all 0.3s ease;
        }

        .stat-box:hover {
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            transform: translateY(-2px);
        }

        .stat-icon {
            width: 56px;
            height: 56px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            flex-shrink: 0;
        }

        .stat-box.total .stat-icon {
            background: #fef3c7;
            color: #92400e;
        }

        .stat-box.active .stat-icon {
            background: #d1fae5;
            color: #059669;
        }

        .stat-content h3 {
            font-size: 13px;
            font-weight: 600;
            color: #718096;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin: 0 0 4px 0;
        }

        .stat-content .number {
            font-size: 32px;
            font-weight: 700;
            color: #1a202c;
            line-height: 1;
        }

        .card-custom {
            background: white;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .card-header-custom {
            padding: 20px 24px;
            border-bottom: 1px solid #e5e7eb;
            background: white;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .card-header-custom h2 {
            font-size: 18px;
            font-weight: 700;
            color: #1a202c;
            margin: 0;
        }

        .btn-add {
            background: #92400e;
            color: white;
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 600;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .btn-add:hover {
            background: #78350f;
        }

        .filter-bar {
            padding: 20px 24px;
            background: #fafafa;
            border-bottom: 1px solid #e5e7eb;
        }

        .filter-form {
            display: flex;
            gap: 12px;
            align-items: center;
        }

        .search-box {
            flex: 1;
            position: relative;
        }

        .search-box input {
            width: 100%;
            height: 40px;
            padding: 0 16px 0 40px;
            border: 1px solid #e5e7eb;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.2s;
        }

        .search-box input:focus {
            outline: none;
            border-color: #92400e;
            box-shadow: 0 0 0 3px rgba(146, 64, 14, 0.1);
        }

        .search-box i {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            color: #9ca3af;
            font-size: 16px;
        }

        .btn-custom {
            height: 40px;
            padding: 0 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s;
            display: flex;
            align-items: center;
            gap: 8px;
            white-space: nowrap;
        }

        .btn-search {
            background: #92400e;
            color: white;
        }

        .btn-search:hover {
            background: #78350f;
        }

        .btn-reset {
            background: #e5e7eb;
            color: #4b5563;
        }

        .btn-reset:hover {
            background: #d1d5db;
        }

        .table-wrapper {
            padding: 24px;
        }

        .table-custom {
            width: 100%;
            border-collapse: collapse;
        }

        .table-custom thead {
            background: #fafafa;
        }

        .table-custom th {
            padding: 12px 16px;
            text-align: left;
            font-size: 12px;
            font-weight: 700;
            color: #4b5563;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 2px solid #e5e7eb;
        }

        .table-custom td {
            padding: 16px;
            font-size: 14px;
            color: #1a202c;
            border-bottom: 1px solid #f3f4f6;
        }

        .table-custom tbody tr {
            transition: background 0.2s;
        }

        .table-custom tbody tr:hover {
            background: #fafafa;
        }

        .badge-custom {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .badge-active {
            background: #d1fae5;
            color: #059669;
        }

        .badge-inactive {
            background: #fee2e2;
            color: #991b1b;
        }

        .actions {
            display: flex;
            gap: 8px;
        }

        .btn-icon {
            width: 32px;
            height: 32px;
            border: none;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 14px;
        }

        .btn-view {
            background: #fef3c7;
            color: #92400e;
        }

        .btn-view:hover {
            background: #fde68a;
        }

        .btn-edit {
            background: #dbeafe;
            color: #1e40af;
        }

        .btn-edit:hover {
            background: #bfdbfe;
        }

        .btn-delete {
            background: #fee2e2;
            color: #991b1b;
        }

        .btn-delete:hover {
            background: #fecaca;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
        }

        .empty-state i {
            font-size: 64px;
            color: #d1d5db;
            margin-bottom: 16px;
        }

        .empty-state h3 {
            font-size: 18px;
            font-weight: 600;
            color: #4b5563;
            margin-bottom: 8px;
        }

        .empty-state p {
            font-size: 14px;
            color: #9ca3af;
        }

        .loading-state {
            text-align: center;
            padding: 60px 20px;
        }

        .spinner {
            width: 40px;
            height: 40px;
            border: 4px solid #f3f4f6;
            border-top-color: #92400e;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 16px;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .seller-footer {
            margin: 32px 24px 24px;
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            padding: 24px;
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            color: #4b5563;
            font-size: 14px;
        }

        .seller-footer .footer-brand {
            display: flex;
            align-items: center;
            gap: 12px;
            font-weight: 600;
            color: #1a202c;
        }

        .seller-footer .footer-brand-icon {
            width: 40px;
            height: 40px;
            border-radius: 10px;
            background: linear-gradient(135deg, #c96d28 0%, #764ba2 100%);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 18px;
        }

        .seller-footer .footer-note {
            font-size: 13px;
            font-weight: 400;
            color: #6b7280;
            margin-top: 4px;
        }

        .seller-footer .footer-links {
            display: flex;
            flex-wrap: wrap;
            gap: 16px;
        }

        .seller-footer .footer-links a {
            color: #4b5563;
            text-decoration: none;
            font-weight: 500;
        }

        .seller-footer .footer-links a:hover {
            color: #c96d28;
        }

        @media (max-width: 768px) {
            #content {
                padding: 16px;
            }

            .stats-grid {
                grid-template-columns: 1fr;
            }

            .filter-form {
                flex-direction: column;
            }

            .search-box {
                width: 100%;
            }

            .btn-custom {
                width: 100%;
                justify-content: center;
            }

            .card-header-custom {
                flex-direction: column;
                align-items: flex-start;
                gap: 16px;
            }

            .table-wrapper {
                overflow-x: auto;
            }

            .table-custom {
                min-width: 800px;
            }

            .seller-footer {
                flex-direction: column;
                align-items: flex-start;
                margin: 24px 16px;
            }

            .seller-footer .footer-links {
                width: 100%;
            }
        }
    </style>
</head>
<body>

<div id="wrapper">
    <div id="content-wrapper">

        <div id="content">
            <div class="container-fluid">
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/seller/dashboard" style="color: #c96d28; text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; transition: all 0.3s ease;">
                        <i class="fas fa-arrow-left"></i> Quay lại
                    </a>
                </div>
                <div class="page-title">
                    <h1>Quản lý đơn hàng</h1>
                    <p>Quản lý và theo dõi đơn hàng của cửa hàng</p>
                </div>

                <div class="stats-grid">
                    <div class="stat-box total">
                        <div class="stat-icon">
                            <i class="fas fa-list"></i>
                        </div>
                        <div class="stat-content">
                            <h3>Tổng đơn hàng</h3>
                            <div class="number" id="totalOrders">0</div>
                        </div>
                    </div>
                    <div class="stat-box active">
                        <div class="stat-icon">
                            <i class="fas fa-check-circle"></i>
                        </div>
                        <div class="stat-content">
                            <h3>Đơn hàng đã giao</h3>
                            <div class="number" id="deliveredOrders">0</div>
                        </div>
                    </div>
                </div>

                <div class="card-custom">
                    <div class="card-header-custom">
                        <h2>Danh sách đơn hàng</h2>
                    </div>

                    <div class="filter-bar">
                        <div class="filter-form">
                            <select id="searchType" class="btn-custom" style="background: white; color: #4b5563; border: 1px solid #e5e7eb;">
                                <option value="all">Tất cả</option>
                                <option value="orderId">Mã đơn hàng</option>
                                <option value="customerName">Tên khách hàng</option>
                                <option value="status">Trạng thái</option>
                            </select>
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Tìm kiếm theo mã đơn hàng...">
                            </div>
                            <button type="button" class="btn-custom btn-search" onclick="applyFilters()">
                                <i class="fas fa-search"></i>
                                <span>Tìm kiếm</span>
                            </button>
                            <button type="button" class="btn-custom btn-reset" onclick="resetFilters()">
                                <i class="fas fa-redo"></i>
                                <span>Đặt lại</span>
                            </button>
                        </div>
                    </div>

                    <div class="table-wrapper">
                        <div id="loadingState" class="loading-state" style="display: none;">
                            <div class="spinner"></div>
                            <p>Đang tải dữ liệu...</p>
                        </div>

                        <div id="tableContainer">
                            <table class="table-custom">
                                <thead>
                                    <tr>
                                        <th>Mã đơn hàng</th>
                                        <th>Khách hàng</th>
                                        <th>Tổng tiền</th>
                                        <th>Trạng thái</th>
                                        <th>Ngày đặt</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody id="orderList">
                                    <!-- Data will be loaded dynamically -->
                                </tbody>
                            </table>

                            <div id="emptyState" class="empty-state" style="display: none;">
                                <i class="fas fa-inbox"></i>
                                <h3>Không tìm thấy dữ liệu</h3>
                                <p>Không có đơn hàng nào phù hợp với tiêu chí tìm kiếm</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <footer class="seller-footer">
            <div class="footer-brand">
                <div class="footer-brand-icon">
                    <i class="fas fa-store"></i>
                </div>
                <div>
                    <div>Bookish Seller Center</div>
                    <div class="footer-note">Quan ly don hang va theo doi hieu qua cua cua hang.</div>
                </div>
            </div>
            <div class="footer-links">
                <a href="${pageContext.request.contextPath}/seller/dashboard">Trang chu seller</a>
                <a href="${pageContext.request.contextPath}/seller/orders">Don hang</a>
                <a href="mailto:seller-support@bookishhaven.com">Ho tro</a>
                <a href="${pageContext.request.contextPath}/seller/settings">Cai dat</a>
            </div>
        </footer>
    </div>
</div>

<div class="modal fade" id="orderDetailModal" tabindex="-1" role="dialog" aria-labelledby="orderDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="orderDetailModalLabel">Chi tiết đơn hàng</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body" id="orderDetailContent">
                <p class="text-muted mb-0">Đang tải chi tiết đơn hàng...</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Đóng</button>
            </div>
        </div>
    </div>
</div>

<script>
    window.appConfig = window.appConfig || {};
    window.appConfig.contextPath = '${pageContext.request.contextPath}';
</script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/seller/SellerOrders.js"></script>
<script>
    if (typeof feather !== "undefined") feather.replace();
</script>
</body>
</html>
