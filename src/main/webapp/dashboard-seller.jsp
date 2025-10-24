<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${shop.name != null ? shop.name : 'Seller Dashboard'} - Bookish Bliss Haven</title>
    <script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: linear-gradient(135deg, #F59e0b 0%, #FF8C42 100%);
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
        
        .seller-header-left {
            display: flex;
            align-items: center;
            gap: 20px;
        }
        
        .shop-logo {
            width: 80px;
            height: 80px;
            border-radius: 16px;
            object-fit: cover;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
            background: white;
        }
        
        .shop-info h1 {
            font-size: 28px;
            color: #1a202c;
            margin-bottom: 4px;
        }
        
        .shop-info p {
            color: #718096;
            font-size: 14px;
            margin-bottom: 8px;
        }
        
        .role-badge {
            display: inline-block;
            background: linear-gradient(135deg, #F59e0b 0%, #FF8C42 100%);
            color: white;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 12px;
            font-weight: 600;
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
            background: linear-gradient(135deg, rgba(255, 107, 53, 0.1) 0%, rgba(255, 140, 66, 0.1) 100%);
            color: #F59e0b;
            border-bottom-color: #F59e0b;
        }
        
        .seller-nav a.active {
            background: linear-gradient(135deg, rgba(255, 107, 53, 0.15) 0%, rgba(255, 140, 66, 0.15) 100%);
            color: #F59e0b;
            border-bottom-color: #F59e0b;
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
            background: linear-gradient(135deg, #FFF5F0 0%, #ffffff 100%);
            padding: 30px;
            border-radius: 16px;
            border-left: 5px solid #F59e0b;
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
            background: radial-gradient(circle, rgba(255, 107, 53, 0.1) 0%, transparent 70%);
            transition: transform 0.6s ease;
        }
        
        .stat-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 15px 40px rgba(255, 107, 53, 0.2);
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
            background: linear-gradient(135deg, #F59e0b 0%, #FF8C42 100%);
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
            position: relative;
            z-index: 1;
        }
        
        .welcome-section {
            background: linear-gradient(135deg, #F59e0b 0%, #FF8C42 100%);
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
            background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, transparent 70%);
        }
        
        .welcome-section h3 {
            font-size: 24px;
            margin-bottom: 15px;
            position: relative;
            z-index: 1;
        }
        
        .welcome-section p {
            font-size: 16px;
            opacity: 0.95;
            margin-bottom: 25px;
            position: relative;
            z-index: 1;
        }
        
        .btn-primary {
            display: inline-flex;
            align-items: center;
            gap: 10px;
            background: white;
            color: #F59e0b;
            padding: 14px 28px;
            border-radius: 12px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s ease;
            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
            position: relative;
            z-index: 1;
        }
        
        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.2);
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
            display: flex;
            align-items: center;
            gap: 8px;
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
            
            .seller-header-left {
                flex-direction: column;
            }
            
            .seller-nav ul {
                flex-direction: column;
            }
            
            .stats-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="seller-container">
        <!-- Header với logo shop -->
        <div class="seller-header">
            <div class="seller-header-left">
                <c:choose>
                    <c:when test="${shop != null && shop.logoUrl != null}">
                        <img src="${shop.logoUrl}" alt="${shop.name}" class="shop-logo">
                    </c:when>
                    <c:otherwise>
                        <div class="shop-logo" style="display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #F59e0b 0%, #FF8C42 100%); color: white; font-weight: bold; font-size: 24px;">
                            S
                        </div>
                    </c:otherwise>
                </c:choose>
                
                <div class="shop-info">
                    <h1>${shop != null && shop.name != null ? shop.name : 'Seller Dashboard'}</h1>
                    <p>Chào mừng, <strong>${username}</strong>!</p>
                    <span class="role-badge">
                        <i data-feather="award" style="width: 12px; height: 12px; vertical-align: middle;"></i>
                        Seller
                    </span>
                </div>
            </div>
            <button onclick="logout()" class="logout-btn">
                <i data-feather="log-out"></i>
                Đăng xuất
            </button>
        </div>
        
        <!-- Navigation -->
        <nav class="seller-nav">
            <ul>
                <li>
                    <a href="${pageContext.request.contextPath}/seller-dashboard" class="active">
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
                    <a href="${pageContext.request.contextPath}/seller/profile">
                        <i data-feather="user"></i>
                        <span>Hồ sơ</span>
                    </a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/seller/wallet">
                        <i data-feather="credit-card"></i>
                        <span>Ví</span>
                    </a>
                </li>
            </ul>
        </nav>
        
        <!-- Dashboard Content -->
        <div class="dashboard-content">
            <h2>
                <i data-feather="trending-up"></i>
                Tổng quan hoạt động
            </h2>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>
                        <i data-feather="package" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Tổng sản phẩm
                    </h3>
                    <div class="value">${totalProducts != null ? totalProducts : 0}</div>
                </div>
                <div class="stat-card">
                    <h3>
                        <i data-feather="bell" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Đơn hàng mới
                    </h3>
                    <div class="value">${newOrders != null ? newOrders : 0}</div>
                </div>
                <div class="stat-card">
                    <h3>
                        <i data-feather="dollar-sign" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Doanh thu tháng này
                    </h3>
                    <div class="value">${monthlyRevenue != null ? monthlyRevenue : '0đ'}</div>
                </div>
                <div class="stat-card">
                    <h3>
                        <i data-feather="star" style="width: 14px; height: 14px; vertical-align: middle;"></i>
                        Đánh giá TB
                    </h3>
                    <div class="value">${avgRating != null ? avgRating : '0.0'} ⭐</div>
                </div>
            </div>
            
            <c:choose>
                <c:when test="${totalProducts == 0 || totalProducts == null}">
                    <div class="welcome-section">
                        <h3>
                            <i data-feather="zap" style="width: 24px; height: 24px; vertical-align: middle;"></i>
                            Bắt đầu bán hàng ngay hôm nay!
                        </h3>
                        <p>Bạn chưa có sản phẩm nào trong cửa hàng. Hãy thêm sản phẩm đầu tiên để bắt đầu kinh doanh trên nền tảng của chúng tôi.</p>
                        <a href="${pageContext.request.contextPath}/seller/products" class="btn-primary">
                            <i data-feather="plus-circle"></i>
                            <span>Quản lý sản phẩm</span>
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="welcome-section">
                        <h3>
                            <i data-feather="check-circle" style="width: 24px; height: 24px; vertical-align: middle;"></i>
                            Cửa hàng ${shop != null && shop.name != null ? shop.name : 'của bạn'} đang hoạt động tốt!
                        </h3>
                        <p>Bạn đang có ${totalProducts} sản phẩm và ${newOrders} đơn hàng mới cần xử lý.</p>
                        <a href="${pageContext.request.contextPath}/seller/orders" class="btn-primary">
                            <i data-feather="shopping-cart"></i>
                            <span>Xem đơn hàng</span>
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
    
    <!-- <script>
        feather.replace();
        
        function logout() {
            localStorage.removeItem('seller_token');
            localStorage.removeItem('seller_username');
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_username');
            localStorage.removeItem('admin_token');
            localStorage.removeItem('admin_username');
            window.location.href = '${pageContext.request.contextPath}/login.jsp';
        }
        
        window.addEventListener('load', function() {
            const token = localStorage.getItem('seller_token') || localStorage.getItem('auth_token');
            if (!token) {
                window.location.href = '${pageContext.request.contextPath}/login.jsp';
            }
        });
    </script> -->


    <script>
    feather.replace();
    
    function logout() {
        localStorage.removeItem('seller_token');
        localStorage.removeItem('seller_username');
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_username');
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_username');
        window.location.href = '${pageContext.request.contextPath}/login.jsp';
    }
    
    // ✅ Hàm navigate với token
    function navigateWithToken(url) {
        const token = localStorage.getItem('seller_token') || localStorage.getItem('auth_token');
        if (!token) {
            window.location.href = '${pageContext.request.contextPath}/login.jsp';
            return;
        }
        
        // Tạo form ẩn để gửi token
        const form = document.createElement('form');
        form.method = 'GET';
        form.action = url;
        
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'token';
        input.value = token;
        
        form.appendChild(input);
        document.body.appendChild(form);
        form.submit();
    }
    
    // ✅ Thêm event listener cho tất cả navigation links
    window.addEventListener('load', function() {
        const token = localStorage.getItem('seller_token') || localStorage.getItem('auth_token');
        
        if (!token) {
            window.location.href = '${pageContext.request.contextPath}/login.jsp';
            return;
        }
        
        // Thêm token vào tất cả các link trong nav
        document.querySelectorAll('.seller-nav a').forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                const href = this.getAttribute('href');
                
                // Nếu là link active (dashboard hiện tại), không làm gì
                if (this.classList.contains('active')) {
                    return;
                }
                
                // Tạo URL với token
                const url = new URL(href, window.location.origin);
                url.searchParams.set('token', token);
                
                window.location.href = url.toString();
            });
        });
        
        // Thêm token vào button "Xem đơn hàng" / "Quản lý sản phẩm"
        const actionButtons = document.querySelectorAll('.btn-primary');
        actionButtons.forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const href = this.getAttribute('href');
                const url = new URL(href, window.location.origin);
                url.searchParams.set('token', token);
                window.location.href = url.toString();
            });
        });
    });
</script>


</body>
</html>