<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><decorator:title default="NK Bookstore - Cửa hàng sách trực tuyến"/></title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <decorator:head/>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f8f9fa;
        }
        
        .navbar {
            background: linear-gradient(135deg, #8B4513 0%, #654321 100%) !important;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        }
        
        .navbar-brand, .nav-link {
            color: white !important;
            font-weight: 500;
        }
        
        .nav-link:hover {
            color: #ffd700 !important;
        }
        
        .cart-badge {
            position: absolute;
            top: -8px;
            right: -8px;
            background: #dc3545;
            color: white;
            border-radius: 50%;
            padding: 2px 6px;
            font-size: 12px;
            font-weight: bold;
        }
        
        .footer {
            margin-top: 3rem;
            padding: 2rem 0;
            background: linear-gradient(135deg, #8B4513 0%, #654321 100%);
            color: white;
        }
    </style>
</head>
<body>
    <!-- Navigation Bar -->
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="<%= request.getContextPath() %>/">
                <i class="fas fa-book"></i> NK Bookstore
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="<%= request.getContextPath() %>/"><i class="fas fa-home"></i> Trang chủ</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#"><i class="fas fa-list"></i> Danh mục</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#"><i class="fas fa-search"></i> Tìm kiếm</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link position-relative" href="cart.jsp">
                            <i class="fas fa-shopping-cart"></i> Giỏ hàng
                            <span id="cartCount" class="cart-badge">0</span>
                        </a>
                    </li>
                    
                    <% 
                    String currentUser = (String) session.getAttribute("username");
                    if (currentUser != null) { 
                    %>
                        <li class="nav-item" id="navUserMenu">
                            <a class="nav-link" href="profile.jsp"><i class="fas fa-user"></i> Tài khoản</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="<%= request.getContextPath() %>/auth?action=logout">
                                <i class="fas fa-sign-out-alt"></i> Đăng xuất
                            </a>
                        </li>
                    <% } else { %>
                        <li class="nav-item" id="navLoginItem">
                            <a class="nav-link" href="login.jsp"><i class="fas fa-sign-in-alt"></i> Đăng nhập</a>
                        </li>
                        <li class="nav-item" id="navRegisterItem">
                            <a class="nav-link" href="register.jsp"><i class="fas fa-user-plus"></i> Đăng ký</a>
                        </li>
                    <% } %>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Main Content -->
    <main>
        <decorator:body/>
    </main>

    <!-- Footer -->
    <footer class="footer">
        <div class="container">
            <div class="row">
                <div class="col-md-6">
                    <h5><i class="fas fa-book"></i> NK Bookstore</h5>
                    <p>Cửa hàng sách trực tuyến hàng đầu với hàng ngàn đầu sách hay</p>
                </div>
                <div class="col-md-6 text-end">
                    <p>&copy; 2025 NK Bookstore. Tất cả quyền được bảo lưu.</p>
                </div>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const API_BASE = '<%= request.getContextPath() %>/api';
        
        // Update cart count from API
        async function updateCartCount() {
            try {
                const token = localStorage.getItem('token');
                if (!token) return;
                
                const response = await fetch(`${API_BASE}/cart/count`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                
                if (response.ok) {
                    const data = await response.json();
                    document.getElementById('cartCount').textContent = data.count || 0;
                }
            } catch (error) {
                console.error('Error updating cart count:', error);
            }
        }
        
        // Update navbar based on login status
        function updateNavbar() {
            const token = localStorage.getItem('token');
            const username = localStorage.getItem('username');
            
            // Only update if user is logged in via JWT but session shows logged out
            if (token && username) {
                const loginItem = document.getElementById('navLoginItem');
                const registerItem = document.getElementById('navRegisterItem');
                const userMenu = document.getElementById('navUserMenu');
                
                if (loginItem && !userMenu) {
                    // User logged in via JWT but session not set, reload page
                    window.location.reload();
                }
            }
        }
        
        // Initialize on page load
        document.addEventListener('DOMContentLoaded', function() {
            updateCartCount();
            updateNavbar();
        });
    </script>
</body>
</html>
