<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý sản phẩm - Bookish Admin</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <script src="https://unpkg.com/feather-icons"></script>
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap&subset=vietnamese" rel="stylesheet">

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

        /* Reuse user-management create button style for consistency */
        .btn-create {
            background: #16a34a;
            color: white;
        }

        .btn-create:hover {
            background: #15803d;
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

        #openCreateProductBtn {
            margin-left: auto;
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

        /* Modal styling - fixed for pointer events and scroll */
        .modal-overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 9998; }
        .modal-overlay.active { display: block; }
        .modal-box { display: none; position: fixed; z-index: 9999; left: 50%; top: 50%; transform: translate(-50%, -50%); width: 600px; max-width: 95vw; max-height: 90vh; overflow-y: auto; background: white; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.2); }
        .modal-box.active { display: block; }
        .modal-header { padding: 18px 22px; border-bottom: 1px solid #eef2f6; display:flex; justify-content:space-between; align-items:center; }
        .modal-title { font-size:18px; font-weight:700; color:#1a202c; }
        .modal-body { padding: 18px 22px; }
        .modal-footer { padding: 14px 22px; border-top: 1px solid #eef2f6; text-align: right; }
        .modal-input { width:100%; padding:10px 12px; border:1px solid #e5e7eb; border-radius:8px; font-size:14px; }
        .btn-primary-modal { background:#92400e; color:white; border:none; padding:10px 18px; border-radius:8px; font-weight:600; cursor:pointer; }
        .btn-secondary-modal { background:#e5e7eb; color:#374151; border:none; padding:10px 16px; border-radius:8px; margin-right:8px; cursor:pointer; }
        .modal-row { display:flex; gap:12px; align-items:center; margin-bottom:12px; }
        .modal-label { width:160px; font-size:14px; color:#374151; }
        .form-feedback { margin-top:12px; padding:10px 12px; border-radius:8px; display:none; }
        .form-feedback.success { display:block; background:#dcfce7; color:#166534; }
        .form-feedback.error { display:block; background:#fee2e2; color:#991b1b; }

        /* Prevent background scroll when modal is open */
        body.modal-open {
            overflow: hidden !important;
        }

        .modal-header {
            padding: 20px 24px;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .modal-title {
            font-size: 20px;
            font-weight: 700;
            color: #1a202c;
        }

        .modal-close {
            font-size: 24px;
            color: #4b5563;
            background: none;
            border: none;
            cursor: pointer;
        }

        .modal-close:hover {
            color: #1a202c;
        }

        .modal-body {
            padding: 24px;
        }

        .modal-row {
            display: flex;
            gap: 16px;
            margin-bottom: 16px;
        }

        .modal-label {
            width: 120px;
            font-weight: 600;
            color: #374151;
            padding-top: 8px;
        }

        .modal-input {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #e5e7eb;
            border-radius: 6px;
            transition: all 0.2s;
        }

        .modal-input:focus {
            border-color: #92400e;
            box-shadow: 0 0 0 3px rgba(146, 64, 14, 0.1);
            outline: none;
        }

        .form-feedback {
            margin: 16px 0;
            padding: 12px;
            border-radius: 6px;
            background: #fee2e2;
            color: #991b1b;
            font-size: 14px;
            display: none;
        }

        .modal-footer {
            padding: 20px 24px;
            border-top: 1px solid #e5e7eb;
            display: flex;
            justify-content: flex-end;
            gap: 12px;
        }

        .modal-warning {
            text-align: center;
            padding: 20px;
            color: #991b1b;
        }

        .btn-primary-modal {
            background: #92400e;
            color: white;
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
        }

        .btn-primary-modal:hover {
            background: #78350f;
        }

        .btn-secondary-modal {
            background: #e5e7eb;
            color: #4b5563;
            padding: 8px 16px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
        }

        .btn-secondary-modal:hover {
            background: #d1d5db;
        }

        /* Table */
        .table-container {
            background: white;
            border-radius: 14px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.06);
            padding: 20px 24px;
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
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 14px;
        }

    </style>
</head>
<body>
<div id="wrapper">
    <%@ include file="/WEB-INF/includes/admin/AdSideBar.jsp" %>
    <div id="content-wrapper">
        <%@ include file="/WEB-INF/includes/admin/header.jsp" %>
        <div id="content">
            <div class="container-fluid">
                <div class="page-title">
                    <h1>Quản lý sản phẩm</h1>
                    <p>Theo dõi và quản lý toàn bộ sản phẩm trong hệ thống</p>
                </div>

                <div class="stats-container">
                    <div class="stat-card total">
                        <div class="stat-icon"><i class="fas fa-boxes"></i></div>
                        <div>
                            <h3>Tổng sản phẩm</h3>
                            <div class="stat-number" id="totalProducts">0</div>
                        </div>
                    </div>
                    <div class="stat-card instock">
                        <div class="stat-icon"><i class="fas fa-box-open"></i></div>
                        <div>
                            <h3>Còn hàng</h3>
                            <div class="stat-number" id="inStock">0</div>
                        </div>
                    </div>
                    <div class="stat-card outstock">
                        <div class="stat-icon"><i class="fas fa-box"></i></div>
                        <div>
                            <h3>Hết hàng</h3>
                            <div class="stat-number" id="outOfStock">0</div>
                        </div>
                    </div>
                </div>

                <div class="card-custom">
                    <div class="card-header-custom">
                        <h2>Danh sách sản phẩm</h2>
                    </div>

                    <div class="filter-bar">
                        <div class="filter-form">
                            <select id="searchType" class="btn-custom" style="background: white; color: #4b5563; border: 1px solid #e5e7eb;">
                                <option value="all">Tất cả</option>
                                <option value="id">ID</option>
                                <option value="title">Tên sách</option>
                                <option value="author">Tác giả</option>
                                <option value="category">Thể loại</option>
                                <option value="status">Trạng thái</option>
                                <option value="shop_name">Tên shop</option>
                            </select>
                            <select id="statusFilter" class="btn-custom" style="background: white; color: #4b5563; border: 1px solid #e5e7eb;">
                                <option value="all">Tất cả trạng thái</option>
                                <option value="active">Hoạt động</option>
                                <option value="inactive">Không hoạt động</option>
                                <option value="pending">Đang chờ duyệt</option>
                            </select>
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Tìm kiếm...">
                            </div>
                            <button type="button" class="btn-custom btn-search" id="searchBtn">
                                <i class="fas fa-search"></i>
                                <span>Tìm kiếm</span>
                            </button>
                            <button type="button" class="btn-custom btn-reset" id="btnReset">
                                <i class="fas fa-redo"></i>
                                <span>Đặt lại</span>
                            </button>
                            <button class="btn-custom btn-create" type="button" id="openCreateProductBtn" onclick="openAddProduct()">
                                <i class="fas fa-plus"></i>
                                <span>Thêm sản phẩm</span>
                            </button>
                        </div>
                    </div>

                    <div class="table-wrapper" id="tableContainer">
                        <div id="loadingState" class="loading-state" style="display: none;">
                            <div class="spinner"></div>
                            <p>Đang tải dữ liệu...</p>
                        </div>
                        <div class="table-container">
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Tên sách</th>
                                        <th>Tác giả</th>
                                        <th>Thể loại</th>
                                        <th>Giá</th>
                                        <th>Tồn kho</th>
                                        <th>Trạng thái</th>
                                        <th>Shop</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody id="product"></tbody>
                            </table>
                            <!-- ⚙️ Phân trang -->
                            <div id="pagination" class="flex justify-center items-center gap-2 mt-4"></div>
                        </div>
                        <div id="emptyState" class="empty-state" style="display: none;">
                            <i class="fas fa-inbox"></i>
                            <h3>Không tìm thấy dữ liệu</h3>
                            <p>Không có sản phẩm nào phù hợp với tiêu chí tìm kiếm</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ include file="/WEB-INF/includes/admin/footer.jsp" %>

<!-- Product modal markup (moved before script include and standardized) -->
<div id="productModalOverlay" class="modal-overlay" style="display:none"></div>
<div id="productModalBox" class="modal-box" style="display:none; z-index:10000; width:720px;">
    <div class="modal-header">
        <div id="productModalTitle" class="modal-title">Thêm sản phẩm</div>
        <button id="productModalClose" class="modal-close" aria-label="Đóng">&times;</button>
    </div>
    <form id="productForm" class="modal-body" autocomplete="off">
        <input type="hidden" id="productId" name="id" />
        <div class="modal-body">
            <div class="modal-row">
                <div class="modal-label">Tiêu đề <span style="color:#ef4444">*</span></div>
                <div style="flex:1"><input id="prodTitle" name="title" class="modal-input" type="text" required placeholder="Tiêu đề" /></div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Tác giả</div>
                <div style="flex:1"><input id="prodAuthor" name="author" class="modal-input" type="text" placeholder="Tác giả" /></div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Giá <span style="color:#ef4444">*</span></div>
                <div style="flex:1"><input id="prodPrice" name="price" class="modal-input" type="number" step="0.01" required placeholder="Giá" /></div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Tồn kho</div>
                <div style="flex:1"><input id="prodStock" name="stock" class="modal-input" type="number" placeholder="Tồn kho" /></div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Thể loại</div>
                <div style="flex:1"><input id="prodCategory" name="category" class="modal-input" type="text" placeholder="Thể loại" /></div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Mô tả</div>
                <div style="flex:1"><textarea id="prodDescription" name="description" class="modal-input" rows="6" placeholder="Mô tả (hiển thị văn bản thuần)" /></textarea></div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Ảnh (URL)</div>
                <div style="flex:1">
                    <input id="prodImage" name="image_url" class="modal-input" type="url" placeholder="URL ảnh" />
                    <div id="prodImagePreview" style="margin-top:8px; min-height:40px;"></div>
                </div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Trạng thái</div>
                <div style="flex:1">
                    <select id="prodStatus" name="status" class="modal-input">
                        <option value="active">Hoạt động</option>
                        <option value="inactive">Không hoạt động</option>
                        <option value="pending">Đang chờ duyệt</option>
                    </select>
                </div>
            </div>
            <div class="modal-row">
                <div class="modal-label">Shop ID <span style="color:#ef4444">*</span></div>
                <div style="flex:1"><input id="prodShopId" name="shop_id" class="modal-input" type="number" required placeholder="Shop ID" /></div>
            </div>
        </div>
        <div id="productFeedback" class="form-feedback" role="alert" style="display:none"></div>
        <div class="modal-footer">
            <button type="button" id="productCancel" class="btn-secondary-modal">Hủy</button>
            <button type="submit" id="productSave" class="btn-primary-modal">Lưu</button>
        </div>
    </form>
</div>

<!-- Product delete confirmation -->
<div id="productDeleteOverlay" class="modal-overlay" style="display:none"></div>
<div id="productDeleteBox" class="modal-box" style="display:none; z-index:10001; width:420px;">
    <div class="modal-header">
        <div class="modal-title">Xóa sản phẩm</div>
        <button id="productDeleteClose" class="modal-close" aria-label="Đóng">&times;</button>
    </div>
    <div class="modal-body">
        <div class="modal-warning">
            <strong>Bạn có chắc muốn xóa sản phẩm này?</strong>
        </div>
        <div id="productDeleteFeedback" class="form-feedback" role="alert" style="display:none"></div>
        <div class="modal-footer">
            <button type="button" id="productDeleteCancel" class="btn-secondary-modal">Hủy</button>
            <button type="button" id="productDeleteConfirm" class="btn-primary-modal">Xóa</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/admin/AdProduct.js"></script>
</body>
</html>
