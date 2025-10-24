<%-- File: /Seller/sellerProduct.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Quản lý Sản phẩm - Shop ID: ${shopId}</title>
    <style>
        /* ... (CSS tùy chỉnh của bạn) ... */
    </style>
</head>
<body>
<div id="wrapper">
    <div id="content" class="container-fluid">
        <h1>Quản lý Sản phẩm</h1>
        <p>Tài khoản Seller: ${username} | Shop ID: <strong>${shopId}</strong></p>

        <div class="stats-container">
            <div class="stat-card total"><div class="stat-icon"><i class="fas fa-boxes"></i></div><div><h3>Tổng sản phẩm</h3><div class="stat-number" id="totalProducts">0</div></div></div>
            <div class="stat-card instock"><div class="stat-icon"><i class="fas fa-box-open"></i></div><div><h3>Còn hàng</h3><div class="stat-number" id="inStock">0</div></div></div>
            <div class="stat-card outstock"><div class="stat-icon"><i class="fas fa-box"></i></div><div><h3>Hết hàng</h3><div class="stat-number" id="outOfStock">0</div></div></div>
        </div>
        
        <div class="card-custom">
             <div class="table-wrapper">
                <div id="loadingState" class="loading-state" style="display: none;">Đang tải dữ liệu...</div>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr><th>ID</th><th>Tên sách</th><th>Tồn kho</th><th>Giá</th><th>Hành động</th></tr>
                        </thead>
                        <tbody id="product">
                            <tr><td colspan="5" class="text-center">Vui lòng đợi...</td></tr>
                        </tbody>
                    </table>
                    <div id="pagination"></div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // 1. NHÚNG BIẾN TỪ SERVER VÀO JAVASCRIPT
    const CONTEXT_PATH = '<%= request.getContextPath() %>';
    const SHOP_ID = '<c:out value="${shopId}" default="0" />'; 
    const API_URL = CONTEXT_PATH + '/api/seller/products';
    const TOKEN = localStorage.getItem('seller_token') || localStorage.getItem('auth_token');

    // Hàm tiện ích để gửi header
    function getAuthHeaders() {
        return { 'Authorization': `Bearer ${TOKEN}` };
    }

    // Hàm tải danh sách sản phẩm qua AJAX
    async function loadProductList() {
        if (SHOP_ID === '0') {
            document.getElementById('product').innerHTML = '<tr><td colspan="5" class="text-center text-danger">Lỗi: Shop ID không hợp lệ.</td></tr>';
            return;
        }

        try {
            // Gọi API để lấy danh sách sản phẩm CỦA SHOP_ID này
            const response = await fetch(`${API_URL}?action=list&shop_id=${SHOP_ID}`, {
                headers: getAuthHeaders()
            });
            
            const data = await response.json();

            if (response.status === 401 || response.status === 403) {
                throw new Error("Unauthorized access. Please re-login.");
            }

            if (data.success) {
                // Cập nhật thống kê và bảng
                document.getElementById('totalProducts').textContent = data.stats.total_books;
                document.getElementById('inStock').textContent = data.stats.in_stock;
                document.getElementById('outOfStock').textContent = data.stats.out_stock;
                renderProductTable(data.products);

            } else {
                document.getElementById('product').innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi: ${data.message}</td></tr>`;
            }
        } catch (error) {
            console.error("Lỗi tải sản phẩm:", error);
            document.getElementById('product').innerHTML = `<tr><td colspan="5" class="text-center text-danger">Lỗi kết nối Server: ${error.message}</td></tr>`;
        }
    }
    
    function renderProductTable(products) {
        const tbody = document.getElementById('product');
        tbody.innerHTML = '';
        if (products.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">Không có sản phẩm nào.</td></tr>';
            return;
        }
        // ... (Logic tạo HTML rows từ products) ...
    }

    document.addEventListener('DOMContentLoaded', loadProductList);
</script>
</body>
</html>