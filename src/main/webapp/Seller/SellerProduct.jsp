<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%-- 
    GIẢ ĐỊNH: Các biến sau đã được set trong SellerProductsServlet.java 
    ${shopId}
    ${username}
--%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sản phẩm - Shop ID: ${shopId}</title>

    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://unpkg.com/feather-icons"></script>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap&subset=vietnamese" rel="stylesheet">
    
    <%-- Đảm bảo bạn include CSS tùy chỉnh của mình (nếu cần) --%>
    <style>
        body {
            background: #f8f8f8;
            font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
        }

        #content {
            margin-top: 70px;
            padding: 32px;
        }

        .page-title h1 {
            font-size: 28px;
            font-weight: 700;
            color: #1a202c;
            margin-bottom: 4px;
        }

        .page-title p {
            color: #718096;
            margin-bottom: 28px;
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

        /* Stats */
        .stats-container {
            display: flex;
            gap: 20px;
            flex-wrap: wrap;
            margin-bottom: 32px;
        }

        .stat-card {
            flex: 1;
            min-width: 250px;
            background: white;
            border-radius: 14px;
            padding: 22px 26px;
            display: flex;
            align-items: center;
            gap: 16px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.05);
            transition: all 0.2s ease;
        }

        .stat-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        }

        .stat-icon {
            font-size: 28px;
            width: 50px;
            height: 50px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 10px;
        }

        .stat-card.total .stat-icon { background: #fef3c7; color: #92400e; }
        .stat-card.instock .stat-icon { background: #d1fae5; color: #047857; }
        .stat-card.outstock .stat-icon { background: #fee2e2; color: #991b1b; }

        .stat-card h3 {
            font-size: 14px;
            margin: 0;
            color: #4b5563;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .stat-number {
            font-size: 26px;
            font-weight: 700;
            color: #1a202c;
        }

        /* Table */
        .table-container {
            background: white;
            border-radius: 14px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.06);
            padding: 20px 24px;
        }

        .empty-state {
            text-align: center;
            padding: 32px 16px;
            color: #6b7280;
        }

        .empty-state i {
            font-size: 36px;
            color: #d1d5db;
            margin-bottom: 12px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        thead {
            background: #fafafa;
        }

        th, td {
            text-align: left;
            padding: 14px 12px;
            border-bottom: 1px solid #f1f1f1;
            font-size: 14px;
        }

        th {
            text-transform: uppercase;
            font-weight: 700;
            color: #4b5563;
            font-size: 12px;
        }

        tbody tr:hover {
            background: #f9fafb;
        }

        .btn-icon {
            width: 32px;
            height: 32px;
            border: none;
            border-radius: 6px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            cursor: pointer;
        }

        .btn-view { background: #fef3c7; color: #92400e; }
        .btn-edit { background: #dbeafe; color: #1e40af; }
        .btn-delete { background: #fee2e2; color: #991b1b; }
        .btn-view:hover { background: #fde68a; }
        .btn-edit:hover { background: #bfdbfe; }
        .btn-delete:hover { background: #fecaca; }
    </style>
</head>
<body>
<div id="wrapper">
    <%-- INCLUDE SIDEBAR/HEADER DÀNH CHO SELLER --%>
    <%-- Thay thế các include của admin bằng include của seller nếu có --%>
    <div id="content-wrapper">
        <div id="content">
            <div class="container-fluid">
                <div class="mb-3">
                    <a href="${pageContext.request.contextPath}/seller/dashboard" style="color: #c96d28; text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; transition: all 0.3s ease;">
                        <i class="fas fa-arrow-left"></i> Quay lại
                    </a>
                </div>
                <div class="page-title">
                    <h1>Quản lý sản phẩm</h1>
                    <p>Quản lý sản phẩm của Shop: <strong>${username} (ID: ${shopId})</strong></p>
                </div>

                <%-- THỐNG KÊ (Sử dụng lại cấu trúc) --%>
                <div class="stats-container">
                    <div class="stat-card total"><div class="stat-icon"><i class="fas fa-boxes"></i></div><div><h3>Tổng sản phẩm</h3><div class="stat-number" id="totalProducts">0</div></div></div>
                    <div class="stat-card instock"><div class="stat-icon"><i class="fas fa-box-open"></i></div><div><h3>Còn hàng</h3><div class="stat-number" id="inStock">0</div></div></div>
                    <div class="stat-card outstock"><div class="stat-icon"><i class="fas fa-box"></i></div><div><h3>Hết hàng</h3><div class="stat-number" id="outOfStock">0</div></div></div>
                </div>

                <div class="card-custom">
                    <div class="card-header-custom">
                        <h2>Danh sách sản phẩm</h2>
                        <button class="btn-add" onclick="openAddModal()">
                            <i class="fas fa-plus"></i>
                            <span>Thêm sản phẩm</span>
                        </button>
                    </div>

                    <%-- FILTER BAR (Được tinh giản) --%>
                    <div class="filter-bar">
                        <div class="filter-form">
                            <select id="searchType" class="btn-custom" style="background: white; color: #4b5563; border: 1px solid #e5e7eb;">
                                <option value="title">Tên sách</option>
                                <option value="author">Tác giả</option>
                                <option value="isbn">ISBN</option>
                            </select>
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Tìm kiếm theo...">
                            </div>
                            <button class="btn-custom btn-search" id="searchBtn">
                                <i class="fas fa-search"></i>
                                <span>Tìm kiếm</span>
                            </button>
                            <button class="btn-custom btn-reset" id="btnReset">
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

                        <div id="tableContainer" class="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Tên sách</th>
                                        <th>Tác giả</th>
                                        <th>Thể loại</th>
                                        <th>Giá</th>
                                        <th>Tồn kho</th>
                                        <th>Hành động</th>
                                    </tr>
                                </thead>
                                <tbody id="product"></tbody>
                            </table>
                            <div id="emptyState" class="empty-state" style="display: none;">
                                <i class="fas fa-box-open"></i>
                                <p>KhA'ng tA�m th���y s���n ph��cm phA� h���p.</p>
                            </div>
                            <%-- ⚙️ Phân trang --%>
                            <div id="pagination" class="flex justify-center items-center gap-2 mt-4"></div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <%-- <%@ include file="/WEB-INF/includes/seller/footer.jsp" %> --%>
    </div>
</div>

<!-- Modal for Add/Edit Product -->
<div class="modal fade" id="productModal" tabindex="-1" role="dialog" aria-labelledby="productModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="productModalLabel">Thêm sản phẩm mới</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form id="productForm">
                    <input type="hidden" id="productId" name="productId">
                    <div class="form-group">
                        <label for="title">Tên sách</label>
                        <input type="text" class="form-control" id="title" name="title" required>
                    </div>
                    <div class="form-group">
                        <label for="author">Tác giả</label>
                        <input type="text" class="form-control" id="author" name="author" required>
                    </div>
                    <div class="form-group">
                        <label for="genre">Thể loại</label>
                        <input type="text" class="form-control" id="genre" name="genre" required>
                    </div>
                    <div class="form-group">
                        <label for="price">Giá</label>
                        <input type="number" class="form-control" id="price" name="price" step="0.01" required>
                    </div>
                    <div class="form-group">
                        <label for="stock">Tồn kho</label>
                        <input type="number" class="form-control" id="stock" name="stock" required>
                    </div>
                    <div class="form-group">
                        <label for="isbn">ISBN</label>
                        <input type="text" class="form-control" id="isbn" name="isbn">
                    </div>
                    <div class="form-group">
                        <label for="description">Mô tả</label>
                        <textarea class="form-control" id="description" name="description" rows="3"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Hủy</button>
                <button type="button" class="btn btn-primary" id="saveProductBtn">Lưu</button>
            </div>
        </div>
    </div>
</div>

<%-- CHÚ Ý: Sử dụng JS mới là SellerProduct.js, không phải AdProduct.js --%>
<script>
    window.appConfig = window.appConfig || {};
    window.appConfig.contextPath = '${pageContext.request.contextPath}';
    window.appConfig.shopId = '<c:out value="${shopId}" />';
</script>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/seller/SellerProduct.js"></script>
<script>
    feather.replace(); // Khởi tạo icons nếu cần
</script>
</body>
</html>
