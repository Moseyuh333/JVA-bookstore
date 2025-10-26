<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Seller Dashboard - Bookish Bliss Haven</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>
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
            padding: 20px;
        }
        
        .seller-container {
            max-width: 1400px;
            margin: 0 auto;
        }
        
        .seller-header {
            background: white;
            padding: 30px;
            border-radius: 20px;
            margin-bottom: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .seller-header-left h1 {
            font-size: 32px;
            color: #1a202c;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .seller-header-left p {
            color: #718096;
            font-size: 16px;
        }
        
        .seller-header-left .role-badge {
            display: inline-block;
            background: linear-gradient(135deg, #c96d28 0%, #764ba2 100%);
            color: white;
            padding: 6px 16px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 600;
            margin-top: 8px;
        }
        
        .seller-nav {
            background: white;
            padding: 0;
            border-radius: 20px;
            margin-bottom: 30px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .seller-nav ul {
            list-style: none;
            display: flex;
            flex-wrap: wrap;
        }
        
        .seller-nav li {
            flex: 1;
            min-width: 150px;
        }
        
        .seller-nav a {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            padding: 20px 15px;
            text-decoration: none;
            color: #4a5568;
            font-weight: 500;
            transition: all 0.3s ease;
            border-bottom: 3px solid transparent;
        }
        
        .seller-nav a:hover {
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
            color: #c96d28;
            border-bottom-color: #c96d28;
        }
        
        .seller-nav a.active {
            background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
            color: #c96d28;
            border-bottom-color: #c96d28;
        }
        
        .dashboard-content {
            background: white;
            padding: 40px;
            border-radius: 20px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
        }
        
        .dashboard-content h2 {
            font-size: 28px;
            color: #1a202c;
            margin-bottom: 30px;
            display: flex;
            align-items: center;
            gap: 12px;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 25px;
            margin-bottom: 40px;
        }
        
        .stat-card {
            background: linear-gradient(135deg, #f6f8fb 0%, #ffffff 100%);
            padding: 30px;
            border-radius: 16px;
            border-left: 5px solid #c96d28;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            position: relative;
            overflow: hidden;
        }
        
        .stat-card::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(102, 126, 234, 0.1) 0%, transparent 70%);
            transition: transform 0.6s ease;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 40px rgba(102, 126, 234, 0.2);
        }
        
        .stat-card:hover::before {
            transform: translate(-25%, -25%);
        }
        
        .stat-card h3 {
            margin: 0 0 12px 0;
            color: #718096;
            font-size: 15px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            position: relative;
            z-index: 1;
        }
        
        .stat-card .value {
            font-size: 42px;
            font-weight: 800;
            background: linear-gradient(135deg, #c96d28 0%, #764ba2 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            position: relative;
            z-index: 1;
        }
        
        .welcome-section {
            background: linear-gradient(135deg, #c96d28 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            border-radius: 16px;
            margin-top: 30px;
            position: relative;
            overflow: hidden;
        }
        
        .welcome-section::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -10%;
            width: 500px;
            height: 500px;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
        }
        
        .welcome-section h3 {
            font-size: 24px;
            margin-bottom: 15px;
            position: relative;
            z-index: 1;
        }
        
        .welcome-section p {
            font-size: 16px;
            opacity: 0.9;
            margin-bottom: 25px;
            position: relative;
            z-index: 1;
        }
        
        .btn-primary {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            background: white;
            color: #c96d28;
            padding: 14px 28px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            position: relative;
            z-index: 1;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
        }
        
        .logout-btn {
            background: linear-gradient(135deg, #f56565 0%, #c53030 100%);
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 12px;
            cursor: pointer;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(245, 101, 101, 0.3);
        }
        
        .logout-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(245, 101, 101, 0.4);
        }
        
        @media (max-width: 768px) {
            .seller-header {
                flex-direction: column;
                gap: 20px;
                text-align: center;
            }
            
            .seller-nav ul {
                flex-direction: column;
            }
            
            .seller-nav li {
                width: 100%;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="seller-container">
        <div class="seller-header">
            <div class="seller-header-left">
                <h1>
                    <i data-feather="shopping-bag"></i>
                    Seller Dashboard
                </h1>
                <p>Chào mừng trở lại, <strong>${username}</strong>!</p>
                <span class="role-badge">
                    <i data-feather="award" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                    ${role}
                </span>
            </div>
            <button onclick="logout()" class="logout-btn">
                <i data-feather="log-out" style="width: 16px; height: 16px; vertical-align: middle;"></i>
                Đăng xuất
            </button>
        </div>
        
        <nav class="seller-nav">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/seller/dashboard" class="active">
                        <i data-feather="home"></i>
                        <span>Tổng quan</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/seller/products">
                        <i data-feather="package"></i>
                        <span>Sản phẩm</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/seller/orders">
                        <i data-feather="shopping-cart"></i>
                        <span>Đơn hàng</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/seller/analytics">
                        <i data-feather="bar-chart-2"></i>
                        <span>Thống kê</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/seller/settings">
                        <i data-feather="settings"></i>
                        <span>Cài đặt</span>
                    </a>
                </li>

                <a href="${pageContext.request.contextPath}/seller/create-post.jsp" class="btn">✍️ Đăng bài viết mới</a>

            </ul>
        </nav>
        
        <div class="dashboard-content">
            <h2>
                <i data-feather="trending-up"></i>
                Tổng quan hoạt động
            </h2>
            <c:out value="${totalProducts}" default="(null)" />
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>
                        <i data-feather="package" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Tổng sản phẩm
                    </h3>
                    <div class="value">${totalProducts}</div>
                </div>
                <div class="stat-card">
                    <h3>
                        <i data-feather="bell" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Đơn hàng mới
                    </h3>
                    <div class="value">${newOrders}</div>
                </div>
                <div class="stat-card">
                    <h3>
                        <i data-feather="dollar-sign" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Doanh thu tháng này
                    </h3>
                    <div class="value">${monthlyRevenue}</div>
                </div>
                <div class="stat-card">
                    <h3>
                        <i data-feather="star" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Đánh giá TB
                    </h3>
                    <div class="value">${avgRating} ⭐</div>
                </div>
            </div>
            
            <div class="welcome-section">
                <h3>
                    <i data-feather="zap" style="width: 24px; height: 24px; vertical-align: middle;"></i>
                    Bắt đầu bán hàng ngay hôm nay!
                </h3>
                <p>Bạn chưa có sản phẩm nào trong cửa hàng. Hãy thêm sản phẩm đầu tiên để bắt đầu kinh doanh trên nền tảng của chúng tôi.</p>
                <a href="${pageContext.request.contextPath}/seller/products/add" class="btn-primary">
                    <i data-feather="plus-circle"></i>
                    <span>Thêm sản phẩm mới</span>
                </a>
            </div>
        </div>
    </div>
    
    <script>
        // Initialize Feather Icons
        feather.replace();
        
        function logout() {
            // Xóa tất cả tokens
            localStorage.removeItem('seller_token');
            localStorage.removeItem('seller_username');
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_username');
            localStorage.removeItem('admin_token');
            localStorage.removeItem('admin_username');
            
            console.log('Logging out, redirecting to login page');
            
            // Redirect về login
            window.location.href = '${pageContext.request.contextPath}/login.jsp';
        }
        
        // Kiểm tra token khi load trang
        window.addEventListener('load', function() {
            const sellerToken = localStorage.getItem('seller_token');
            const authToken = localStorage.getItem('auth_token');
            const token = sellerToken || authToken;
            
            console.log('Seller token:', sellerToken ? 'exists' : 'not found');
            console.log('Auth token:', authToken ? 'exists' : 'not found');
            
            if (!token) {
                console.log('No token found, redirecting to login');
                window.location.href = '${pageContext.request.contextPath}/login.jsp';
                return;
            }
            
            console.log('Token found, page loaded successfully');
        });


        

    </script>

</body>
</html>