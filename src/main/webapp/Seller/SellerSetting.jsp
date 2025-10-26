
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- Giả định biến shopId và username đã được set từ Servlet --%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cài đặt Shop - ${username}</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        /* Thêm CSS tùy chỉnh cho card và form */
        .setting-card { background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 30px; margin-bottom: 25px; }
        .setting-card h2 { font-size: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <h1><i class="fas fa-cog mr-2"></i>Cài đặt Shop</h1>
        <p class="text-muted">Quản lý thông tin và cấu hình thanh toán/vận chuyển của Shop ID: <strong>${shopId}</strong></p>

        <%-- Thông báo chung --%>
        <div id="alertContainer"></div>

        <%-- Card 1: Cài đặt Thông tin Cơ bản --%>
        <div class="setting-card">
            <h2>Thông tin Cơ bản</h2>
            <form id="shopProfileForm">
                <input type="hidden" name="shopId" value="${shopId}">
                
                <div class="form-group">
                    <label for="shopName">Tên Shop</label>
                    <input type="text" class="form-control" id="shopName" name="name" required>
                </div>
                
                <div class="form-group">
                    <label for="shopAddress">Địa chỉ Shop</label>
                    <input type="text" class="form-control" id="shopAddress" name="address">
                </div>
                
                <div class="form-group">
                    <label for="shopDescription">Mô tả Shop</label>
                    <textarea class="form-control" id="shopDescription" name="description" rows="3"></textarea>
                </div>

                <button type="submit" class="btn btn-primary"><i class="fas fa-save mr-2"></i>Lưu Thay Đổi</button>
            </form>
            <div id="loadingProfile" class="spinner-border spinner-border-sm mt-3 d-none"></div>
        </div>

        <%-- Card 2: Cài đặt Thanh toán/Chiết khấu (Chỉ hiển thị, thường được Admin quản lý) --%>
        <div class="setting-card">
            <h2>Cấu hình Chiết khấu/Thanh toán</h2>
            <p class="text-muted small">Chiết khấu/Phí dịch vụ thường được cố định và quản lý bởi hệ thống.</p>
            <div class="row">
                <div class="col-md-6">
                    <strong>Tỷ lệ Chiết khấu (Commission):</strong> <span id="commissionRate">--</span>
                </div>
                <div class="col-md-6">
                    <strong>Phương thức Thanh toán:</strong> <span class="badge badge-success">Thanh toán khi nhận hàng (COD)</span>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        // const API_URL = '<%= request.getContextPath() %>/api/seller/profile';
        // const SHOP_ID = ${shopId};

        const API_URL = '<%= request.getContextPath() %>/api/seller/profile';

    // SỬA DÒNG 4: Sử dụng JSTL c:out để đảm bảo giá trị shopId luôn là chuỗi (hoặc số) an toàn
        const SHOP_ID = '<c:out value="${shopId}" default="0" />';

        async function loadShopProfile() {
            const token = localStorage.getItem('seller_token');
            if (!token || SHOP_ID === 0) return;

            document.getElementById('loadingProfile').classList.remove('d-none');
            
            try {
                // Giả định bạn có một API để lấy chi tiết Shop
                const response = await fetch(`${API_URL}?action=get&shop_id=${SHOP_ID}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                const data = await response.json();
                
                if (data.success && data.shop) {
                    // Điền dữ liệu vào form
                    document.getElementById('shopName').value = data.shop.name || '';
                    document.getElementById('shopAddress').value = data.shop.address || '';
                    document.getElementById('shopDescription').value = data.shop.description || '';
                    document.getElementById('commissionRate').textContent = (data.shop.commissionRate * 100).toFixed(2) + '%';
                } else {
                    showAlert('Không thể tải thông tin Shop: ' + (data.message || 'Lỗi kết nối.'), 'danger');
                }
            } catch (error) {
                showAlert('Lỗi mạng khi tải hồ sơ.', 'danger');
            } finally {
                document.getElementById('loadingProfile').classList.add('d-none');
            }
        }

        // Handle form submit
        document.getElementById('shopProfileForm').addEventListener('submit', async function(e) {
            e.preventDefault();

            const formData = new FormData(this);
            const shopData = Object.fromEntries(formData.entries());

            try {
                const token = localStorage.getItem('seller_token') || localStorage.getItem('auth_token');
                const response = await fetch(`${API_URL}?action=update`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(shopData)
                });

                const result = await response.json();
                if (result.success) {
                    showAlert('Cập nhật thông tin Shop thành công!', 'success');
                } else {
                    showAlert('Lỗi: ' + (result.message || 'Không thể cập nhật thông tin Shop'), 'danger');
                }
            } catch (error) {
                console.error('Error updating shop profile:', error);
                showAlert('Có lỗi xảy ra khi cập nhật thông tin Shop', 'danger');
            }
        });

        function showAlert(message, type) {
            document.getElementById('alertContainer').innerHTML = 
                `<div class="alert alert-${type} alert-dismissible fade show" role="alert">${message}<button type="button" class="close" data-dismiss="alert"><span>&times;</span></button></div>`;
        }

        document.addEventListener('DOMContentLoaded', loadShopProfile);
    </script>
</body>
</html>

