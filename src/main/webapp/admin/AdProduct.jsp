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

    <style>
        body {
            background: #f8f8f8;
            font-family: 'Roboto', sans-serif;
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

        /* Search bar */
        .search-bar {
            display: flex;
            gap: 12px;
            margin-bottom: 20px;
        }

        .search-bar input {
            flex: 1;
            height: 42px;
            border-radius: 10px;
            border: 1px solid #e5e7eb;
            padding: 0 16px;
            font-size: 14px;
        }

        .search-bar input:focus {
            border-color: #92400e;
            box-shadow: 0 0 0 3px rgba(146,64,14,0.1);
            outline: none;
        }

        .search-bar button {
            border: none;
            border-radius: 10px;
            padding: 0 20px;
            font-weight: 600;
            cursor: pointer;
            height: 42px;
            transition: background 0.2s;
        }

        .btn-search { background: #92400e; color: white; }
        .btn-search:hover { background: #78350f; }
        .btn-add { background: #b45309; color: white; }
        .btn-add:hover { background: #92400e; }

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

                <div class="search-bar">
                    <input type="text" id="searchInput" placeholder="Tìm kiếm sản phẩm theo tên, tác giả, thể loại...">
                    <button class="btn-search"><i class="fas fa-search"></i></button>
                    <button class="btn-add" data-toggle="modal" data-target="#addProductModal"><i class="fas fa-plus"></i> Thêm</button>
                </div>

                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Mã</th>
                                <th>Tên sách</th>
                                <th>Tác giả</th>
                                <th>Thể loại</th>
                                <th>Giá</th>
                                <th>Số lượng</th>
                                <th>Người bán</th>
                                <th>Thao tác</th>
                            </tr>
                        </thead>
                        <tbody id="product"></tbody>
                    </table>
                </div>

            </div>
        </div>
        <%@ include file="/WEB-INF/includes/admin/footer.jsp" %>
    </div>
</div>
<script src="${pageContext.request.contextPath}/assets/js/admin/AdProduct.js"></script>
</body>
</html>
