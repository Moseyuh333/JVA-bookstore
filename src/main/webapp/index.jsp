<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NK Bookstore - Cửa hàng sách trực tuyến</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-color: #8B4513;
            --secondary-color: #D2691E;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f8f9fa;
        }
        
        .navbar {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        
        .navbar-brand {
            font-weight: 700;
            font-size: 1.5rem;
            color: white !important;
        }
        
        .nav-link {
            color: rgba(255,255,255,0.9) !important;
            margin: 0 10px;
            transition: all 0.3s;
        }
        
        .nav-link:hover {
            color: #ffd700 !important;
            transform: translateY(-2px);
        }
        
        .cart-badge {
            position: absolute;
            top: -8px;
            right: -8px;
            background: #dc3545;
            color: white;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.75rem;
            font-weight: bold;
        }
        
        .hero-section {
            background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
            color: white;
            padding: 60px 20px;
            text-align: center;
            margin-bottom: 40px;
        }
        
        .hero-section h1 {
            font-size: 3rem;
            font-weight: 700;
            margin-bottom: 20px;
        }
        
        .hero-section p {
            font-size: 1.2rem;
            opacity: 0.95;
        }
        
        .section-title {
            font-size: 2rem;
            font-weight: 700;
            color: var(--primary-color);
            margin-bottom: 30px;
            position: relative;
            padding-bottom: 15px;
        }
        
        .section-title::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            width: 60px;
            height: 3px;
            background: var(--secondary-color);
        }
        
        .book-card {
            background: white;
            border: 1px solid #e0e0e0;
            border-radius: 10px;
            overflow: hidden;
            transition: all 0.3s;
            height: 100%;
            display: flex;
            flex-direction: column;
        }
        
        .book-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 10px 25px rgba(0,0,0,0.15);
            border-color: var(--secondary-color);
        }
        
        .book-image {
            width: 100%;
            height: 250px;
            object-fit: cover;
            background: #f0f0f0;
        }
        
        .book-body {
            padding: 15px;
            flex-grow: 1;
            display: flex;
            flex-direction: column;
        }
        
        .book-title {
            font-weight: 600;
            color: var(--primary-color);
            margin-bottom: 8px;
            min-height: 45px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
        }
        
        .book-author {
            font-size: 0.9rem;
            color: #666;
            margin-bottom: 5px;
        }
        
        .book-price {
            font-size: 1.3rem;
            font-weight: 700;
            color: var(--secondary-color);
            margin: 10px 0;
        }
        
        .book-rating {
            font-size: 0.85rem;
            color: #ff9800;
            margin-bottom: 10px;
        }
        
        .book-actions {
            display: flex;
            gap: 8px;
            margin-top: auto;
        }
        
        .btn-add-cart {
            flex: 1;
            background: var(--primary-color);
            color: white;
            border: none;
            padding: 8px 12px;
            border-radius: 5px;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.3s;
        }
        
        .btn-add-cart:hover {
            background: var(--secondary-color);
            transform: scale(1.02);
        }
        
        .btn-wishlist {
            width: 40px;
            height: 40px;
            border: 1px solid #ddd;
            background: white;
            border-radius: 5px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s;
        }
        
        .btn-wishlist:hover {
            background: #f5f5f5;
            border-color: var(--secondary-color);
        }
        
        .products-section {
            margin-bottom: 60px;
        }
        
        .carousel-container {
            position: relative;
        }
        
        .carousel-nav {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            background: rgba(139, 69, 19, 0.8);
            color: white;
            border: none;
            width: 40px;
            height: 40px;
            border-radius: 50%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10;
            transition: all 0.3s;
        }
        
        .carousel-nav:hover {
            background: var(--secondary-color);
        }
        
        .carousel-nav.prev {
            left: -60px;
        }
        
        .carousel-nav.next {
            right: -60px;
        }
        
        .loading-spinner {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 300px;
        }
        
        .footer {
            background: var(--primary-color);
            color: white;
            padding: 40px 20px;
            margin-top: 60px;
        }
    </style>
</head>
<body>
    <%
        // Check user session
        String username = (String) session.getAttribute("username");
        boolean isLoggedIn = (username != null && !username.isEmpty());
    %>
    
    <!-- Navigation -->
    <nav class="navbar navbar-expand-lg navbar-dark">
        <div class="container">
            <a class="navbar-brand" href="index.jsp">
                <i class="fas fa-book"></i> NK Bookstore
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="index.jsp"><i class="fas fa-home"></i> Trang chủ</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#"><i class="fas fa-th"></i> Danh mục</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#"><i class="fas fa-search"></i> Tìm kiếm</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link position-relative" href="cart.jsp">
                            <i class="fas fa-shopping-cart"></i> Giỏ hàng
                            <span class="cart-badge" id="cartCount">0</span>
                        </a>
                    </li>
                    
                    <!-- User menu (will be updated by JavaScript) -->
                    <li class="nav-item" id="navLoginItem">
                        <a class="nav-link" href="login.jsp"><i class="fas fa-sign-in-alt"></i> Đăng nhập</a>
                    </li>
                    <li class="nav-item" id="navRegisterItem">
                        <a class="nav-link" href="register.jsp"><i class="fas fa-user-plus"></i> Đăng ký</a>
                    </li>
                    <li class="nav-item dropdown" id="navUserMenu" style="display: none;">
                        <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="fas fa-user"></i> <span id="navUsername"></span>
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="profile.jsp"><i class="fas fa-user-circle"></i> Hồ sơ</a></li>
                            <li><a class="dropdown-item" href="orders.jsp"><i class="fas fa-box"></i> Đơn hàng</a></li>
                            <li><a class="dropdown-item" href="wishlist.jsp"><i class="fas fa-heart"></i> Yêu thích</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item" href="#" onclick="logout(); return false;"><i class="fas fa-sign-out-alt"></i> Đăng xuất</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <div class="hero-section">
        <div class="container">
            <h1><i class="fas fa-book-open"></i> Chào mừng đến NK Bookstore</h1>
            <p>Khám phá thế giới sách với hàng ngàn đầu sách hay nhất</p>
        </div>
    </div>

    <!-- Main Content -->
    <div class="container">
        <!-- Newest Books -->
        <div class="products-section">
            <h2 class="section-title"><i class="fas fa-star"></i> Sách Mới Nhất</h2>
            <div id="newestBooks" class="row g-4">
                <div class="col-12 loading-spinner">
                    <div class="spinner-border" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Best Selling Books -->
        <div class="products-section">
            <h2 class="section-title"><i class="fas fa-fire"></i> Bán Chạy Nhất</h2>
            <div id="bestSellingBooks" class="row g-4">
                <div class="col-12 loading-spinner">
                    <div class="spinner-border" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Top Rated Books -->
        <div class="products-section">
            <h2 class="section-title"><i class="fas fa-award"></i> Đánh Giá Cao Nhất</h2>
            <div id="topRatedBooks" class="row g-4">
                <div class="col-12 loading-spinner">
                    <div class="spinner-border" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Favorite Books -->
        <div class="products-section">
            <h2 class="section-title"><i class="fas fa-heart"></i> Yêu Thích Nhất</h2>
            <div id="favoriteBooks" class="row g-4">
                <div class="col-12 loading-spinner">
                    <div class="spinner-border" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
        </div>
    </div>

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
        
        // Load books from API
        async function loadBooks(endpoint, containerId) {
            try {
                const response = await fetch(`${API_BASE}/${endpoint}?limit=12&offset=0`);
                if (!response.ok) {
                    console.error(`Failed to load ${endpoint}:`, response.status);
                    throw new Error('Failed to load books');
                }
                
                const books = await response.json();
                displayBooks(books, containerId);
            } catch (error) {
                console.error('Error loading books:', error);
                document.getElementById(containerId).innerHTML = 
                    '<div class="col-12"><p class="text-danger">Không thể tải dữ liệu sách</p></div>';
            }
        }
        
        // Display books in grid
        function displayBooks(books, containerId) {
            const container = document.getElementById(containerId);
            
            if (!books || books.length === 0) {
                container.innerHTML = '<div class="col-12"><p>Không có sách nào</p></div>';
                return;
            }
            
            container.innerHTML = books.map(book => {
                let ratingHtml = '';
                if (book.averageRating > 0) {
                    ratingHtml = `<div class="book-rating">
                        <i class="fas fa-star"></i> \${book.averageRating.toFixed(1)}/5 
                        (\${book.ratingCount} đánh giá)
                    </div>`;
                }
                
                return `<div class="col-lg-3 col-md-4 col-sm-6">
                    <div class="book-card">
                        <img src="\${book.imageUrl || 'https://via.placeholder.com/250x350?text=No+Image'}" 
                             alt="\${book.title}" class="book-image">
                        <div class="book-body">
                            <h5 class="book-title">\${book.title}</h5>
                            <p class="book-author">Tác giả: \${book.author}</p>
                            <div class="book-price">₫\${book.price.toLocaleString('vi-VN')}</div>
                            \${ratingHtml}
                            <div class="book-actions">
                                <button class="btn-add-cart" onclick="addToCart(\${book.id})">
                                    <i class="fas fa-shopping-cart"></i> Thêm
                                </button>
                                <button class="btn-wishlist" onclick="addToWishlist(\${book.id})" 
                                        title="Thêm vào yêu thích">
                                    <i class="far fa-heart"></i>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>`;
            }).join('');
        }
        
        // Add to cart
        async function addToCart(bookId) {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    alert('Vui lòng đăng nhập trước');
                    window.location.href = 'login.jsp';
                    return;
                }
                
                const response = await fetch(`${API_BASE}/cart/add`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ bookId, quantity: 1 })
                });
                
                if (response.ok) {
                    alert('Thêm vào giỏ hàng thành công!');
                    updateCartCount();
                } else {
                    alert('Không thể thêm vào giỏ hàng');
                }
            } catch (error) {
                console.error('Error adding to cart:', error);
                alert('Lỗi khi thêm vào giỏ hàng');
            }
        }
        
        // Add to wishlist
        async function addToWishlist(bookId) {
            try {
                const token = localStorage.getItem('token');
                if (!token) {
                    alert('Vui lòng đăng nhập trước');
                    window.location.href = 'login.jsp';
                    return;
                }
                
                const response = await fetch(`${API_BASE}/wishlist/add`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify({ bookId })
                });
                
                if (response.ok) {
                    alert('Đã thêm vào yêu thích!');
                } else {
                    alert('Không thể thêm vào yêu thích');
                }
            } catch (error) {
                console.error('Error adding to wishlist:', error);
            }
        }
        
        // Update cart count
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
        
        // Load all books on page load
        document.addEventListener('DOMContentLoaded', function() {
            loadBooks('books/newest', 'newestBooks');
            loadBooks('books/best-selling', 'bestSellingBooks');
            loadBooks('books/top-rated', 'topRatedBooks');
            loadBooks('books/favorites', 'favoriteBooks');
            updateCartCount();
            updateNavbar(); // Update navbar based on login status
        });
        
        // Update navbar to show login/logout
        function updateNavbar() {
            const token = localStorage.getItem('token');
            const username = localStorage.getItem('username');
            
            const loginItem = document.getElementById('navLoginItem');
            const registerItem = document.getElementById('navRegisterItem');
            const userMenu = document.getElementById('navUserMenu');
            const usernameSpan = document.getElementById('navUsername');
            
            if (token && username) {
                // User is logged in
                if (loginItem) loginItem.style.display = 'none';
                if (registerItem) registerItem.style.display = 'none';
                if (userMenu) userMenu.style.display = 'block';
                if (usernameSpan) usernameSpan.textContent = username;
            } else {
                // User is not logged in
                if (loginItem) loginItem.style.display = 'block';
                if (registerItem) registerItem.style.display = 'block';
                if (userMenu) userMenu.style.display = 'none';
            }
        }
        
        // Logout function
        function logout() {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            alert('Đã đăng xuất thành công');
            window.location.reload();
        }
    </script>
</body>
</html>
                        <div id="userDropdown" class="hidden absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
                            <div class="py-2">
                                <a href="<%=request.getContextPath()%>/login.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                                    Đăng nhập
                                </a>
                                <a href="<%=request.getContextPath()%>/register.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                                    Đăng ký
                                </a>
                                <hr class="my-1">
                                <a href="<%=request.getContextPath()%>/forgot-password.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i data-feather="key" class="w-4 h-4 mr-2"></i>
                                    Quên mật khẩu
                                </a>
                            </div>
                        </div>
                    </div>

                    <!-- Mobile hamburger (optional) -->
                    <button class="md:hidden p-2 rounded-full hover:bg-amber-700" aria-label="Menu">
                        <i data-feather="menu" class="w-5 h-5"></i>
                    </button>
                </div>
            </div>
        </div>
    </nav>

    <!-- Hero Section -->
    <section class="hero-bg text-white py-32 px-4">
        <div class="container mx-auto text-center">
            <h1 class="title-font text-4xl md:text-6xl font-bold mb-6">Discover Your Next Favorite Read</h1>
            <p class="text-xl mb-8 max-w-2xl mx-auto">Explore our curated collection of timeless classics and contemporary masterpieces</p>
            <div class="flex flex-col sm:flex-row justify-center gap-4">
                <button class="bg-amber-600 hover:bg-amber-700 text-white font-bold py-3 px-8 rounded-full transition duration-300">
                    Browse Collection
                </button>
                <button class="bg-white hover:bg-gray-100 text-amber-800 font-bold py-3 px-8 rounded-full transition duration-300">
                    Join Our Book Club
                </button>
            </div>
        </div>
    </section>

    <!-- Featured Books -->
    <section class="py-16 px-4">
        <div class="container mx-auto">
            <h2 class="title-font text-3xl font-bold text-center mb-12">Featured Books</h2>
            <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
                <!-- Book Card 1 -->
                <div class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300">
                    <img src="http://static.photos/books/320x240/1" alt="Book Cover" class="w-full h-64 object-cover">
                    <div class="p-4">
                        <h3 class="title-font font-bold text-lg mb-1">The Silent Patient</h3>
                        <p class="text-gray-600 text-sm mb-2">Alex Michaelides</p>
                        <div class="flex justify-between items-center">
                            <span class="font-bold text-amber-700">$14.99</span>
                            <button class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
                <!-- Book Card 2 -->
                <div class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300">
                    <img src="http://static.photos/books/320x240/2" alt="Book Cover" class="w-full h-64 object-cover">
                    <div class="p-4">
                        <h3 class="title-font font-bold text-lg mb-1">Educated</h3>
                        <p class="text-gray-600 text-sm mb-2">Tara Westover</p>
                        <div class="flex justify-between items-center">
                            <span class="font-bold text-amber-700">$12.99</span>
                            <button class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
                <!-- Book Card 3 -->
                <div class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300">
                    <img src="http://static.photos/books/320x240/3" alt="Book Cover" class="w-full h-64 object-cover">
                    <div class="p-4">
                        <h3 class="title-font font-bold text-lg mb-1">Where the Crawdads Sing</h3>
                        <p class="text-gray-600 text-sm mb-2">Delia Owens</p>
                        <div class="flex justify-between items-center">
                            <span class="font-bold text-amber-700">$15.99</span>
                            <button class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
                <!-- Book Card 4 -->
                <div class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300">
                    <img src="http://static.photos/books/320x240/4" alt="Book Cover" class="w-full h-64 object-cover">
                    <div class="p-4">
                        <h3 class="title-font font-bold text-lg mb-1">Atomic Habits</h3>
                        <p class="text-gray-600 text-sm mb-2">James Clear</p>
                        <div class="flex justify-between items-center">
                            <span class="font-bold text-amber-700">$16.99</span>
                            <button class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Why Choose Us -->
    <section class="bg-amber-50 py-16 px-4">
        <div class="container mx-auto">
            <h2 class="title-font text-3xl font-bold text-center mb-12">Tại sao chọn chúng tôi</h2>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div class="text-center p-6">
                    <div class="bg-amber-100 w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-4">
                        <i data-feather="award" class="w-8 h-8 text-amber-700"></i>
                    </div>
                    <h3 class="title-font font-bold text-xl mb-2">Tuyển chọn đặc biệt</h3>
                    <p class="text-gray-700">Những cuốn sách được chọn lọc bởi đội ngũ chuyên gia văn học để đảm bảo chất lượng đọc.</p>
                </div>
                <div class="text-center p-6">
                    <div class="bg-amber-100 w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-4">
                        <i data-feather="truck" class="w-8 h-8 text-amber-700"></i>
                    </div>
                    <h3 class="title-font font-bold text-xl mb-2">Giao hàng nhanh</h3>
                    <p class="text-gray-700">Nhận sách nhanh chóng với đối tác vận chuyển đáng tin cậy của chúng tôi.</p>
                </div>
                <div class="text-center p-6">
                    <div class="bg-amber-100 w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-4">
                        <i data-feather="heart" class="w-8 h-8 text-amber-700"></i>
                    </div>
                    <h3 class="title-font font-bold text-xl mb-2">Cộng đồng đọc sách</h3>
                    <p class="text-gray-700">Tham gia cộng đồng những người yêu sách sôi động để thảo luận và sự kiện.</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Testimonials -->
    <section class="py-16 px-4 bg-white">
        <div class="container mx-auto">
            <h2 class="title-font text-3xl font-bold text-center mb-12">Độc giả nói gì về chúng tôi</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                <!-- Testimonial 1 -->
                <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
                    <div class="flex items-center mb-4">
                        <img src="http://static.photos/people/100x100/1" alt="Reader" class="w-12 h-12 rounded-full mr-4 object-cover">
                        <div>
                            <h4 class="font-bold">Sarah Johnson</h4>
                            <div class="flex text-amber-500">
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                            </div>
                        </div>
                    </div>
                    <p class="text-gray-700 italic">"Lựa chọn sách tuyệt vời nhất mà tôi tìm thấy trực tuyến! Những gợi ý của họ luôn chính xác."</p>
                </div>
                <!-- Testimonial 2 -->
                <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
                    <div class="flex items-center mb-4">
                        <img src="http://static.photos/people/100x100/2" alt="Reader" class="w-12 h-12 rounded-full mr-4 object-cover">
                        <div>
                            <h4 class="font-bold">Michael Chen</h4>
                            <div class="flex text-amber-500">
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                            </div>
                        </div>
                    </div>
                    <p class="text-gray-700 italic">"Giao hàng nhanh và đóng gói tuyệt vời. Sách của tôi luôn đến trong tình trạng hoàn hảo."</p>
                </div>
                <!-- Testimonial 3 -->
                <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
                    <div class="flex items-center mb-4">
                        <img src="http://static.photos/people/100x100/3" alt="Reader" class="w-12 h-12 rounded-full mr-4 object-cover">
                        <div>
                            <h4 class="font-bold">Emma Rodriguez</h4>
                            <div class="flex text-amber-500">
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                <i data-feather="star" class="w-4 h-4"></i>
                            </div>
                        </div>
                    </div>
                    <p class="text-gray-700 italic">"Yêu thích danh sách đọc theo mùa và gợi ý câu lạc bộ sách. Tìm thấy rất nhiều cuốn sách yêu thích mới!"</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Newsletter -->
    <section class="bg-amber-800 text-white py-16 px-4">
        <div class="container mx-auto max-w-4xl text-center">
            <h2 class="title-font text-3xl font-bold mb-4">Cập nhật tin tức văn học</h2>
            <p class="mb-8 text-amber-100 max-w-2xl mx-auto">Đăng ký nhận bản tin để biết về sách mới, ưu đãi độc quyền và gợi ý đọc sách.</p>
            <form class="flex flex-col sm:flex-row gap-4 max-w-md mx-auto sm:max-w-xl">
                <input type="email" placeholder="Địa chỉ email của bạn" class="flex-grow px-4 py-3 rounded-full text-gray-800 focus:outline-none focus:ring-2 focus:ring-amber-500">
                <button type="submit" class="bg-white hover:bg-gray-100 text-amber-800 font-bold py-3 px-6 rounded-full transition duration-300 whitespace-nowrap">
                    Đăng ký
                </button>
            </form>
        </div>
    </section>

    <!-- Footer -->
    <footer class="bg-gray-900 text-gray-300 py-12 px-4">
        <div class="container mx-auto">
            <!-- Compact copyright badge moved above footer columns -->
            <div class="flex justify-center mb-10">
                <span class="inline-flex items-center gap-2 bg-gray-800 text-amber-200 px-4 py-2 rounded-full text-sm shadow-sm">
                    <i data-feather="shield" class="w-4 h-4"></i>
                    <span>&copy; <span id="year"></span> Bookish Bliss Haven · Mọi quyền được bảo lưu</span>
                </span>
            </div>
            <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
                <div>
                    <h3 class="title-font text-white text-xl font-bold mb-4">Bookish Bliss Haven</h3>
                    <p class="mb-4">Nguồn sách chất lượng và cảm hứng văn học đáng tin cậy của bạn.</p>
                    <div class="flex space-x-4">
                        <a href="#" class="hover:text-white">
                            <i data-feather="facebook" class="w-5 h-5"></i>
                        </a>
                        <a href="#" class="hover:text-white">
                            <i data-feather="twitter" class="w-5 h-5"></i>
                        </a>
                        <a href="#" class="hover:text-white">
                            <i data-feather="instagram" class="w-5 h-5"></i>
                        </a>
                    </div>
                </div>
                <div>
                    <h4 class="text-white font-bold mb-4">Mua sắm</h4>
                    <ul class="space-y-2">
                        <li><a href="#" class="hover:text-white">Sách mới</a></li>
                        <li><a href="#" class="hover:text-white">Sách bán chạy</a></li>
                        <li><a href="#" class="hover:text-white">Tiểu thuyết</a></li>
                        <li><a href="#" class="hover:text-white">Phi tiểu thuyết</a></li>
                        <li><a href="#" class="hover:text-white">Thẻ quà tặng</a></li>
                    </ul>
                </div>
                <div>
                    <h4 class="text-white font-bold mb-4">Hỗ trợ</h4>
                    <ul class="space-y-2">
                        <li><a href="#" class="hover:text-white">Câu hỏi thường gặp</a></li>
                        <li><a href="#" class="hover:text-white">Vận chuyển</a></li>
                        <li><a href="#" class="hover:text-white">Đổi trả</a></li>
                        <li><a href="#" class="hover:text-white">Liên hệ</a></li>
                        <li><a href="#" class="hover:text-white">Chính sách bảo mật</a></li>
                    </ul>
                </div>
                <div>
                    <h4 class="text-white font-bold mb-4">Liên hệ</h4>
                    <address class="not-italic space-y-2">
                        <div class="flex items-start">
                            <i data-feather="map-pin" class="w-5 h-5 mr-2 mt-0.5"></i>
                            <span>123 Đường Văn Học, Quận Sách, TP.HCM</span>
                        </div>
                        <div class="flex items-center">
                            <i data-feather="mail" class="w-5 h-5 mr-2"></i>
                            <a href="mailto:info@bookishhaven.com" class="hover:text-white">info@bookishhaven.com</a>
                        </div>
                        <div class="flex items-center">
                            <i data-feather="phone" class="w-5 h-5 mr-2"></i>
                            <a href="tel:+84901234567" class="hover:text-white">0901 234 567</a>
                        </div>
                    </address>
                </div>
            </div>
        </div>
    </footer>

    <script>
        feather.replace();
        
        // User dropdown functionality
        document.addEventListener('DOMContentLoaded', function() {
            const userDropdownBtn = document.getElementById('userDropdownBtn');
            const userDropdown = document.getElementById('userDropdown');
            
            // Check if user is logged in
            const token = localStorage.getItem('auth_token');
            const isLoggedIn = token && token.length > 0;
            
            // Update dropdown content based on login status
            if (isLoggedIn) {
                updateDropdownForLoggedInUser();
            } else {
                updateDropdownForGuestUser();
            }
            
            if (userDropdownBtn && userDropdown) {
                userDropdownBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    userDropdown.classList.toggle('hidden');
                });
                
                // Close dropdown when clicking outside
                document.addEventListener('click', function() {
                    userDropdown.classList.add('hidden');
                });
                
                // Prevent dropdown from closing when clicking inside it
                userDropdown.addEventListener('click', function(e) {
                    e.stopPropagation();
                });
            }
        });
        
        function updateDropdownForLoggedInUser() {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) {
                userDropdown.innerHTML = `
                    <div class="py-2">
                        <div class="px-4 py-2 text-sm text-gray-600 border-b">
                            <i data-feather="user" class="w-4 h-4 inline mr-2"></i>
                            Xin chào!
                        </div>
                        <a href="<%=request.getContextPath()%>/profile.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="settings" class="w-4 h-4 mr-2"></i>
                            Hồ sơ cá nhân
                        </a>
                        <a href="#" onclick="logout()" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="log-out" class="w-4 h-4 mr-2"></i>
                            Đăng xuất
                        </a>
                    </div>
                `;
                feather.replace();
                // Update account button label
                const lbl = document.getElementById('accountBtnLabel');
                if (lbl) lbl.textContent = 'Profile';
            }
        }
        
        function updateDropdownForGuestUser() {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) {
                userDropdown.innerHTML = `
                    <div class="py-2">
                        <a href="<%=request.getContextPath()%>/login.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                            Đăng nhập
                        </a>
                        <a href="<%=request.getContextPath()%>/register.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                            Đăng ký
                        </a>
                        <hr class="my-1">
                        <a href="<%=request.getContextPath()%>/forgot-password.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="key" class="w-4 h-4 mr-2"></i>
                            Quên mật khẩu
                        </a>
                    </div>
                `;
                feather.replace();
                // Update account button label
                const lbl = document.getElementById('accountBtnLabel');
                if (lbl) lbl.textContent = 'Account';
            }
        }
        
        function logout() {
            // Remove token from localStorage
            localStorage.removeItem('auth_token');
            
            // Update dropdown to guest user
            updateDropdownForGuestUser();
            
            // Optional: Show logout confirmation
            alert('Đăng xuất thành công!');
            
            // Refresh the page to update UI
            window.location.reload();
        }
            // Set current year for copyright badge
            const y = document.getElementById('year');
            if (y) y.textContent = new Date().getFullYear();
            
            // Simple animation for book cards on scroll
            const bookCards = document.querySelectorAll('.book-card');
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        entry.target.style.opacity = '1';
                        entry.target.style.transform = 'translateY(0)';
                    }
                });
            }, { threshold: 0.1 });
            bookCards.forEach(card => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                card.style.transition = 'all 0.6s ease-out';
                observer.observe(card);
            });
        
    </script>
</body>
</html>
