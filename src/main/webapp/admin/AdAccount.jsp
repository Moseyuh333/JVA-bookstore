<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý tài khoản - Bookish Admin</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Feather Icons -->
    <script src="https://unpkg.com/feather-icons"></script>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap&subset=vietnamese" rel="stylesheet">
    <script>
        window.appConfig = window.appConfig || {};
        window.appConfig.contextPath = '${pageContext.request.contextPath}';
    </script>
    
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

        /* Layout */
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

        /* Container */
        .container-fluid {
            max-width: 1400px;
            margin: 0 auto;
        }

        /* Page Title */
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

        /* Stats Grid */
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

        /* Main Card */
        .card-custom {
            background: white;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        /* Card Header */
        .card-header-custom {
            padding: 20px 24px;
            border-bottom: 1px solid #e5e7eb;
            background: white;
        }

        .card-header-custom h2 {
            font-size: 18px;
            font-weight: 700;
            color: #1a202c;
            margin: 0;
        }

        /* Filter Bar */
        .filter-bar {
            padding: 20px 24px;
            background: #fafafa;
            border-bottom: 1px solid #e5e7eb;
        }

        .filter-form {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
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

        .btn-create {
            background: #16a34a;
            color: white;
        }

        .btn-create:hover {
            background: #15803d;
        }

        #openCreateUserBtn {
            margin-left: auto;
        }

        /* Table */
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

        /* User Cell */
        .user-cell {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .avatar {
            width: 40px;
            height: 40px;
            border-radius: 8px;
            background: linear-gradient(135deg, #92400e, #b45309);
            color: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 16px;
            flex-shrink: 0;
        }

        .user-info-text {
            min-width: 0;
        }

        .user-name {
            font-weight: 600;
            color: #1a202c;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        /* Badge */
        .badge-custom {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .badge-admin {
            background: #fee2e2;
            color: #991b1b;
        }

        .badge-customer {
            background: #dbeafe;
            color: #1e40af;
        }

        .badge-seller {
            background: #dcfce7;
            color: #047857;
        }

        .badge-shipper {
            background: #ede9fe;
            color: #5b21b6;
        }

        .badge-pending {
            background: #fef3c7;
            color: #92400e;
        }

        .badge-banned {
            background: #fee2e2;
            color: #b91c1c;
        }

        /* Actions */
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

        .detail-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
        }

        .detail-item {
            background: #f9fafb;
            border-radius: 10px;
            padding: 16px;
        }

        .detail-item h4 {
            font-size: 13px;
            font-weight: 600;
            color: #6b7280;
            text-transform: uppercase;
            margin-bottom: 6px;
        }

        .detail-item p {
            font-size: 16px;
            font-weight: 600;
            color: #111827;
            margin: 0;
            word-break: break-word;
        }

        .modal-warning {
            padding: 18px;
            border-radius: 10px;
            background: #fef2f2;
            color: #991b1b;
            margin-bottom: 18px;
        }

        .modal-warning strong {
            display: block;
            margin-bottom: 6px;
        }

        /* Empty State */
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

        /* Loading */
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

        .modal-overlay {
            position: fixed;
            inset: 0;
            display: none;
            align-items: center;
            justify-content: center;
            background: rgba(30, 41, 59, 0.55);
            z-index: 2000;
            padding: 20px;
            pointer-events: none;
        }

        .modal-overlay.show {
            display: flex;
            pointer-events: auto;
        }

        .modal-dialog {
            width: 100%;
            max-width: 560px;
            background: white;
            border-radius: 16px;
            box-shadow: 0 25px 45px -15px rgba(15, 23, 42, 0.35);
            animation: modalSlideIn 0.28s ease;
            pointer-events: auto;
            position: relative;
        }

        .modal-header {
            padding: 20px 24px;
            border-bottom: 1px solid #e5e7eb;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .modal-header h3 {
            margin: 0;
            font-size: 18px;
            font-weight: 700;
            color: #1f2937;
        }

        .modal-close {
            border: none;
            background: transparent;
            cursor: pointer;
            font-size: 18px;
            color: #6b7280;
            padding: 4px;
            border-radius: 6px;
            transition: background 0.2s ease, color 0.2s ease;
        }

        .modal-close:hover {
            background: #f3f4f6;
            color: #111827;
        }

        .modal-body {
            padding: 24px;
        }

        .form-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 6px;
        }

        .form-group.full-width {
            grid-column: 1 / -1;
        }

        .form-group label {
            font-size: 13px;
            font-weight: 600;
            color: #4b5563;
        }

        .form-group input,
        .form-group select {
            height: 42px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            padding: 0 12px;
            font-size: 14px;
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #92400e;
            box-shadow: 0 0 0 3px rgba(146, 64, 14, 0.12);
        }

        .form-feedback {
            margin-top: 16px;
            padding: 12px 16px;
            border-radius: 10px;
            font-size: 14px;
            display: none;
        }

        .form-feedback.success {
            display: block;
            background: #dcfce7;
            color: #166534;
        }

        .form-feedback.error {
            display: block;
            background: #fee2e2;
            color: #991b1b;
        }

        .modal-actions {
            margin-top: 24px;
            display: flex;
            justify-content: flex-end;
            gap: 12px;
        }

        .modal-actions .btn-secondary,
        .modal-actions .btn-primary {
            min-width: 140px;
            height: 42px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            border: none;
            cursor: pointer;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }

        .modal-actions .btn-secondary {
            background: #e5e7eb;
            color: #4b5563;
        }

        .modal-actions .btn-secondary:hover {
            background: #d1d5db;
        }

        .modal-actions .btn-primary {
            background: #92400e;
            color: white;
        }

        .modal-actions .btn-primary:hover {
            background: #78350f;
            box-shadow: 0 10px 18px -12px rgba(146, 64, 14, 0.85);
        }

        .modal-actions button:disabled {
            opacity: 0.7;
            cursor: not-allowed;
            box-shadow: none;
            transform: none;
        }

        @keyframes modalSlideIn {
            from {
                transform: translateY(16px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        body.modal-open {
            overflow: hidden;
        }

        /* Responsive */
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

            #openCreateUserBtn {
                width: 100%;
                margin-left: 0;
            }

            .table-wrapper {
                overflow-x: auto;
            }

            .table-custom {
                min-width: 800px;
            }

            .modal-dialog {
                max-height: 95vh;
                overflow-y: auto;
            }
        }
    </style>
</head>
<body>

<div id="wrapper">
    <!-- Include Sidebar -->
    <%@ include file="/WEB-INF/includes/admin/AdSideBar.jsp" %>

    <div id="content-wrapper">
        <!-- Include Header -->
        <%@ include file="/WEB-INF/includes/admin/header.jsp" %>

        <div id="content">
            <div class="container-fluid">
                <!-- Page Title -->
                <div class="page-title">
                    <h1>Quản lý tài khoản</h1>
                    <p>Quản lý và theo dõi tất cả tài khoản người dùng trong hệ thống</p>
                </div>

                <!-- Stats Grid -->
                <div class="stats-grid">
                    <div class="stat-box total">
                        <div class="stat-icon">
                            <i class="fas fa-users"></i>
                        </div>
                        <div class="stat-content">
                            <h3>Tổng tài khoản</h3>
                            <div class="number" id="totalUsers">0</div>
                        </div>
                    </div>
                    <div class="stat-box active">
                        <div class="stat-icon">
                            <i class="fas fa-user-check"></i>
                        </div>
                        <div class="stat-content">
                            <h3>Đang hoạt động</h3>
                            <div class="number" id="activeUsers">0</div>
                        </div>
                    </div>
                </div>

                <!-- Main Card -->
                <div class="card-custom">
                    <!-- Card Header -->
                    <div class="card-header-custom">
                        <h2>Danh sách người dùng</h2>
                    </div>

                    <!-- Filter Bar -->
                    <div class="filter-bar">
                        <div class="filter-form">
                            <select id="searchType" class="btn-custom" style="background: white; color: #4b5563; border: 1px solid #e5e7eb;">
                                <option value="all">Tất cả</option>
                                <option value="username">Tên đăng nhập</option>
                                <option value="fullname">Họ và tên</option>
                                <option value="email">Email</option>
                                <option value="phone">Số điện thoại</option>
                                <option value="role">Quyền</option>
                            </select>                            
                            <select id="statusFilter" class="btn-custom" style="background: white; color: #4b5563; border: 1px solid #e5e7eb;">
                                <option value="all">Tất cả trạng thái</option>
                                <option value="active">Đang hoạt động</option>
                                <option value="inactive">Tạm khóa</option>
                                <option value="pending">Chờ kích hoạt</option>
                                <option value="banned">Đã khóa vĩnh viễn</option>
                            </select>
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Tìm kiếm theo tên, email, số điện thoại...">
                            </div>

                            <button class="btn-custom btn-search" onclick="applyFilters()">
                                <i class="fas fa-search"></i>
                                <span>Tìm kiếm</span>
                            </button>
                            <button class="btn-custom btn-reset" onclick="resetFilters()">
                                <i class="fas fa-redo"></i>
                                <span>Đặt lại</span>
                            </button>
                            <button class="btn-custom btn-create" type="button" id="openCreateUserBtn">
                                <i class="fas fa-user-plus"></i>
                                <span>Thêm tài khoản</span>
                            </button>
                        </div>
                    </div>

                    <!-- Table Wrapper -->
                    <div class="table-wrapper">
                        <div id="loadingState" class="loading-state" style="display: none;">
                            <div class="spinner"></div>
                            <p>Đang tải dữ liệu...</p>
                        </div>

                        <div id="tableContainer">
                            <table class="table-custom">
                                <thead>
                                    <tr>
                                        <th>Tài khoản</th>
                                        <th>Họ và tên</th>
                                        <th>Ngày sinh</th>
                                        <th>Giới tính</th>
                                        <th>Địa chỉ</th>
                                        <th>Email</th>
                                        <th>Số điện thoại</th>
                                        <th>Quyền</th>
                                        <th>Trạng thái</th>
                                        <th>Thao tác</th>
                                    </tr>
                                </thead>
                                <tbody id="User">
                                    <!-- Data will be loaded dynamically -->
                                </tbody>
                            </table>

                            <div id="emptyState" class="empty-state" style="display: none;">
                                <i class="fas fa-users-slash"></i>
                                <h3>Không tìm thấy dữ liệu</h3>
                                <p>Không có tài khoản nào phù hợp với tiêu chí tìm kiếm</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Include Footer -->
        <%@ include file="/WEB-INF/includes/admin/footer.jsp" %>
    </div>
</div>

<div class="modal-overlay" id="createUserModal" aria-hidden="true" role="dialog" aria-modal="false">
    <div class="modal-dialog" role="document">
        <div class="modal-header">
            <h3>Thêm tài khoản mới</h3>
            <button type="button" class="modal-close" data-close-modal aria-label="Đóng">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <form id="createUserForm" class="modal-body" autocomplete="off">
            <div class="form-grid">
                <div class="form-group">
                    <label for="createUsername">Tên đăng nhập *</label>
                    <input type="text" id="createUsername" name="username" placeholder="Tên đăng nhập" required>
                </div>
                <div class="form-group">
                    <label for="createEmail">Email *</label>
                    <input type="email" id="createEmail" name="email" placeholder="name@example.com" required>
                </div>
                <div class="form-group full-width">
                    <label for="createPassword">Mật khẩu tạm *</label>
                    <input type="password" id="createPassword" name="password" placeholder="Tối thiểu 6 ký tự" required>
                </div>
                <div class="form-group">
                    <label for="createFullName">Họ và tên</label>
                    <input type="text" id="createFullName" name="full_name" placeholder="Nguyễn Văn A">
                </div>
                <div class="form-group">
                    <label for="createPhone">Số điện thoại</label>
                    <input type="tel" id="createPhone" name="phone" placeholder="0987654321">
                </div>
                <div class="form-group">
                    <label for="createRole">Quyền</label>
                    <select id="createRole" name="role">
                        <option value="admin">Quản trị viên</option>
                        <option value="customer" selected>Khách hàng</option>
                        <option value="seller">Người bán</option>
                        <option value="shipper">Nhân viên giao hàng</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="createStatus">Trạng thái</label>
                    <select id="createStatus" name="status">
                        <option value="active" selected>Đang hoạt động</option>
                        <option value="inactive">Tạm khóa</option>
                        <option value="pending">Chờ kích hoạt</option>
                        <option value="banned">Đã khóa vĩnh viễn</option>
                    </select>
                </div>
            </div>
            <div id="createUserFeedback" class="form-feedback" role="alert"></div>
            <div class="modal-actions">
                <button type="button" class="btn-secondary" data-close-modal>Hủy</button>
                <button type="submit" class="btn-primary" id="createUserSubmit">Tạo tài khoản</button>
            </div>
        </form>
    </div>
</div>

<div class="modal-overlay" id="editUserModal" aria-hidden="true" role="dialog" aria-modal="false">
    <div class="modal-dialog" role="document">
        <div class="modal-header">
            <h3>Chỉnh sửa tài khoản</h3>
            <button type="button" class="modal-close" data-close-modal aria-label="Đóng">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <form id="editUserForm" class="modal-body" autocomplete="off">
            <input type="hidden" id="editUserId" name="id">
            <div class="form-grid">
                <div class="form-group">
                    <label for="editUsername">Tên đăng nhập *</label>
                    <input type="text" id="editUsername" name="username" required>
                </div>
                <div class="form-group">
                    <label for="editEmail">Email *</label>
                    <input type="email" id="editEmail" name="email" required>
                </div>
                <div class="form-group">
                    <label for="editFullName">Họ và tên</label>
                    <input type="text" id="editFullName" name="full_name">
                </div>
                <div class="form-group">
                    <label for="editPhone">Số điện thoại</label>
                    <input type="tel" id="editPhone" name="phone">
                </div>
                <div class="form-group">
                    <label for="editRole">Quyền</label>
                    <select id="editRole" name="role">
                        <option value="admin">Quản trị viên</option>
                        <option value="customer">Khách hàng</option>
                        <option value="seller">Người bán</option>
                        <option value="shipper">Nhân viên giao hàng</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="editStatus">Trạng thái</label>
                    <select id="editStatus" name="status">
                        <option value="active">Đang hoạt động</option>
                        <option value="inactive">Tạm khóa</option>
                        <option value="pending">Chờ kích hoạt</option>
                        <option value="banned">Đã khóa vĩnh viễn</option>
                    </select>
                </div>
            </div>
            <div id="editUserFeedback" class="form-feedback" role="alert"></div>
            <div class="modal-actions">
                <button type="button" class="btn-secondary" data-close-modal>Hủy</button>
                <button type="submit" class="btn-primary" id="editUserSubmit">Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<div class="modal-overlay" id="viewUserModal" aria-hidden="true" role="dialog" aria-modal="false">
    <div class="modal-dialog" role="document">
        <div class="modal-header">
            <h3>Chi tiết tài khoản</h3>
            <button type="button" class="modal-close" data-close-modal aria-label="Đóng">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="detail-grid">
                <div class="detail-item">
                    <h4>Tên đăng nhập</h4>
                    <p id="viewUsername">-</p>
                </div>
                <div class="detail-item">
                    <h4>Email</h4>
                    <p id="viewEmail">-</p>
                </div>
                <div class="detail-item">
                    <h4>Họ và tên</h4>
                    <p id="viewFullName">-</p>
                </div>
                <div class="detail-item">
                    <h4>Số điện thoại</h4>
                    <p id="viewPhone">-</p>
                </div>
                <div class="detail-item">
                    <h4>Quyền</h4>
                    <p id="viewRole">-</p>
                </div>
                <div class="detail-item">
                    <h4>Trạng thái</h4>
                    <p id="viewStatus">-</p>
                </div>
                <div class="detail-item">
                    <h4>Xác thực email</h4>
                    <p id="viewVerified">-</p>
                </div>
                <div class="detail-item">
                    <h4>Ngày tạo</h4>
                    <p id="viewCreated">-</p>
                </div>
                <div class="detail-item">
                    <h4>Cập nhật</h4>
                    <p id="viewUpdated">-</p>
                </div>
                <div class="detail-item">
                    <h4>Ngày sinh</h4>
                    <p id="viewBirthDate">-</p>
                </div>
                <div class="detail-item">
                    <h4>Địa chỉ</h4>
                    <p id="viewAddress">-</p>
                </div>
            </div>
            <div class="modal-actions" style="margin-top: 24px;">
                <button type="button" class="btn-secondary" data-close-modal>Đóng</button>
            </div>
        </div>
    </div>
</div>

<div class="modal-overlay" id="deleteUserModal" aria-hidden="true" role="dialog" aria-modal="false">
    <div class="modal-dialog" role="document">
        <div class="modal-header">
            <h3>Xóa tài khoản</h3>
            <button type="button" class="modal-close" data-close-modal aria-label="Đóng">
                <i class="fas fa-times"></i>
            </button>
        </div>
        <div class="modal-body">
            <div class="modal-warning">
                <strong>Bạn có chắc muốn xóa tài khoản này?</strong>
                <div id="deleteUserSummary">Hành động này không thể hoàn tác.</div>
            </div>
            <div id="deleteUserFeedback" class="form-feedback" role="alert"></div>
            <div class="modal-actions">
                <button type="button" class="btn-secondary" data-close-modal>Hủy</button>
                <button type="button" class="btn-primary" id="deleteUserConfirm">Xóa tài khoản</button>
            </div>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/assets/js/admin/AdAccount.js"></script>
</body>
</html>
