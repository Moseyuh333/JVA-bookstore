<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký Shop - Bookish Bliss Haven</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #c96d28 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 40px 20px;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .register-container {
            max-width: 600px;
            width: 100%;
            background: white;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
        }
        
        .register-header {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .register-header h1 {
            font-size: 32px;
            color: #1a202c;
            margin-bottom: 8px;
        }
        
        .register-header p {
            color: #718096;
            font-size: 16px;
        }
        
        .form-group {
            margin-bottom: 24px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #1a202c;
            font-weight: 600;
            font-size: 14px;
        }
        
        .form-group input,
        .form-group textarea {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e5e7eb;
            border-radius: 8px;
            font-size: 14px;
            transition: all 0.3s ease;
        }
        
        .form-group input:focus,
        .form-group textarea:focus {
            outline: none;
            border-color: #c96d28;
            box-shadow: 0 0 0 3px rgba(201, 109, 40, 0.1);
        }
        
        .form-group textarea {
            min-height: 100px;
            resize: vertical;
        }
        
        .btn-submit {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #c96d28 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }
        
        .btn-submit:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(201, 109, 40, 0.4);
        }
        
        .btn-submit:disabled {
            opacity: 0.6;
            cursor: not-allowed;
            transform: none;
        }
        
        .alert {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 14px;
        }
        
        .alert-success {
            background: #d1fae5;
            color: #059669;
            border: 1px solid #059669;
        }
        
        .alert-error {
            background: #fee2e2;
            color: #dc2626;
            border: 1px solid #dc2626;
        }
        
        .back-link {
            text-align: center;
            margin-top: 20px;
        }
        
        .back-link a {
            color: #c96d28;
            text-decoration: none;
            font-weight: 600;
        }
        
        .back-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="register-container">
        <div class="register-header">
            <h1>Đăng ký Shop</h1>
            <p>Bắt đầu kinh doanh với chúng tôi</p>
        </div>
        
        <div id="alertContainer"></div>
        
        <!-- <form id="shopRegisterForm"> -->
        <!-- <form id="registerShopForm" method="POST" action="/api/seller/register-shop"></form>
            <div class="form-group">
                <label for="shopName">Tên Shop <span style="color: red;">*</span></label>
                <input type="text" id="shopName" name="name" required 
                       placeholder="Nhập tên shop của bạn">
            </div>
            
            <div class="form-group">
                <label for="shopAddress">Địa chỉ Shop</label>
                <input type="text" id="shopAddress" name="address" 
                       placeholder="Nhập địa chỉ shop (tùy chọn)">
            </div>
            
            <div class="form-group">
                <label for="shopDescription">Mô tả Shop</label>
                <textarea id="shopDescription" name="description" 
                          placeholder="Giới thiệu về shop của bạn (tùy chọn)"></textarea>
            </div>
            
            <button type="submit" class="btn-submit" id="submitBtn">
                Đăng ký Shop
            </button>
        </form> -->

        <form id="shopRegisterForm" method="POST" action="<%= request.getContextPath() %>/api/seller/register-shop">
    <div class="form-group">
        <label for="shopName">Tên Shop <span style="color: red;">*</span></label>
        <input type="text" id="shopName" name="name" required 
               placeholder="Nhập tên shop của bạn">
    </div>

    <div class="form-group">
        <label for="shopAddress">Địa chỉ Shop</label>
        <input type="text" id="shopAddress" name="address" 
               placeholder="Nhập địa chỉ shop (tùy chọn)">
    </div>

    <div class="form-group">
        <label for="shopDescription">Mô tả Shop</label>
        <textarea id="shopDescription" name="description" 
                  placeholder="Giới thiệu về shop của bạn (tùy chọn)"></textarea>
    </div>

    <button type="submit" class="btn-submit" id="submitBtn">
        Đăng ký Shop
    </button>
</form>

        <div class="back-link">
            <a href="${pageContext.request.contextPath}/index.jsp">← Quay lại trang chủ</a>
        </div>
    </div>
    
    <script>
        const API_URL = '<%= request.getContextPath() %>/api/seller/register-shop';
        //const API_URL = '<%= request.getContextPath() %>/api/seller/request-approval';

        document.getElementById('shopRegisterForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const submitBtn = document.getElementById('submitBtn');
            const formData = new FormData(this);
            
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xử lý...';
            
            try {
                const response = await fetch(API_URL, {
                    method: 'POST',
                    body: new URLSearchParams(formData),
                    credentials: 'include' // ✅ thêm dòng này
                });
                
                const data = await response.json();
                
                // if (data.success) {
                //     showAlert('Shop đã được tạo thành công! Đang chuyển hướng...', 'success');
                //     setTimeout(() => {
                //         window.location.href = '<%= request.getContextPath() %>/seller/dashboard';
                //     }, 1500);

                    if (data.success) {
        showAlert('Yêu cầu đăng ký shop đã được gửi. Vui lòng chờ admin duyệt.', 'success');
        setTimeout(() => {
            window.location.href = '<%= request.getContextPath() %>/index.jsp';
        }, 2000);
    

                } else {
                    showAlert(data.message || 'Có lỗi xảy ra. Vui lòng thử lại.', 'error');
                    submitBtn.disabled = false;
                    submitBtn.textContent = 'Đăng ký Shop';
                }
            } catch (error) {
                console.error('Error:', error);
                showAlert('Lỗi kết nối. Vui lòng thử lại sau.', 'error');
                submitBtn.disabled = false;
                submitBtn.textContent = 'Đăng ký Shop';
            }
        });
        
        function showAlert(message, type) {
            const alertContainer = document.getElementById('alertContainer');
            const alertClass = type === 'success' ? 'alert-success' : 'alert-error';
            alertContainer.innerHTML = `<div class="alert ${alertClass}">${message}</div>`;
        }
    </script>
</body>
</html>
