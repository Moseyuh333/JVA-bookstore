<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bookish Bliss Haven | Home</title>
    <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');
        body { font-family: 'Roboto', sans-serif; }
        .hero-bg { background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('http://static.photos/books/1200x630/42'); background-size: cover; background-position: center; }
        .book-card:hover { transform: translateY(-5px); box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); }
        .title-font { font-family: 'Playfair Display', serif; }
    </style>
</head>
<body class="bg-gray-50">
    <!-- Navigation -->
    <nav class="bg-amber-800 text-white shadow-lg">
        <div class="container mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <span class="title-font text-xl font-bold">Bookish Bliss Haven</span>
                </a>
                <div class="hidden md:flex space-x-8">
                    <a href="<%=request.getContextPath()%>/index.jsp" class="hover:text-amber-200 font-medium">Home</a>
                    <a href="<%=request.getContextPath()%>/shop.jsp" class="hover:text-amber-200 font-medium">Shop</a>
                    <a href="<%=request.getContextPath()%>/collections.jsp" class="hover:text-amber-200 font-medium">Collections</a>
                    <a href="<%=request.getContextPath()%>/about.jsp" class="hover:text-amber-200 font-medium">About</a>
                </div>
                <div class="flex items-center space-x-4">
                    <!-- User Dropdown -->
                    <div class="relative">
                        <button id="userDropdownBtn" class="p-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
                            <i data-feather="user" class="w-5 h-5"></i>
                        </button>
                        <div id="userDropdown" class="hidden absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
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
                    
                    <button class="p-2 rounded-full hover:bg-amber-700">
                        <i data-feather="search" class="w-5 h-5"></i>
                    </button>
                    <button class="p-2 rounded-full hover:bg-amber-700">
                        <i data-feather="shopping-cart" class="w-5 h-5"></i>
                        <span class="sr-only">Cart</span>
                    </button>
                    <button class="md:hidden p-2 rounded-full hover:bg-amber-700">
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
        });
    </script>
</body>
</html>
    <div class="container">
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-3">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-primary-subtle text-primary p-3 fs-5"><i class="fas fa-truck"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Giao nhanh toàn quốc</h6>
                            <p class="mb-0 small">Miễn phí từ 299.000đ</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-success-subtle text-success p-3 fs-5"><i class="fas fa-shield-heart"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Đổi trả 7 ngày</h6>
                            <p class="mb-0 small">Hoàn tiền 100% nếu sách lỗi</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-warning-subtle text-warning p-3 fs-5"><i class="fas fa-gift"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Quà tặng hấp dẫn</h6>
                            <p class="mb-0 small">Tích điểm - voucher mỗi tuần</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-info-subtle text-info p-3 fs-5"><i class="fas fa-credit-card"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Thanh toán linh hoạt</h6>
                            <p class="mb-0 small">Momo, ZaloPay, chuyển khoản</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5" id="best-seller">
    <div class="container">
        <div class="section-heading">
            <h2>Sách bán chạy trong tuần</h2>
            <a href="#">Xem tất cả <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-4">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Hot</span>
                            <small class="text-muted">Bản in mới</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Nhà Giả Kim</h5>
                        <p class="small flex-grow-1">Hành trình tìm kiếm kho báu và khám phá bản ngã đầy cảm hứng.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>129.000đ</span>
                            <del>169.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Hot</span>
                            <small class="text-muted">Top tháng 6</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Dế Mèn Phiêu Lưu Ký</h5>
                        <p class="small flex-grow-1">Tựa sách tuổi thơ với minh họa mới, tăng cường kỹ năng đọc hiểu.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>85.000đ</span>
                            <del>105.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Combo</span>
                            <small class="text-muted">Tiết kiệm 30%</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Tư Duy Nhanh &amp; Chậm</h5>
                        <p class="small flex-grow-1">Bản dịch mới nhất kèm sổ tay ghi chú và bookmark giới hạn.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>199.000đ</span>
                            <del>259.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Mới</span>
                            <small class="text-muted">Bản đặc biệt</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Lược Sử Thời Gian</h5>
                        <p class="small flex-grow-1">Bản bìa cứng cập nhật, tặng kèm poster sơ đồ vũ trụ.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>245.000đ</span>
                            <del>295.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5 bg-white" id="new-arrivals">
    <div class="container">
        <div class="section-heading">
            <h2>Sách mới cập nhật</h2>
            <a href="#">Khám phá thêm <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-4">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Ra mắt</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Chuyện Nghìn Lẻ Một Đêm</h5>
                        <p class="small flex-grow-1">Bản dịch mới với tranh minh họa màu tuyệt đẹp.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>175.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Độc quyền</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Sống Tối Giản</h5>
                        <p class="small flex-grow-1">Những bí quyết sắp xếp không gian và cân bằng cuộc sống.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>142.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Tặng kèm</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Khám Phá Vũ Trụ</h5>
                        <p class="small flex-grow-1">Bộ sticker 3D về các hành tinh dành cho bé từ 6+</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>119.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Tái bản</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Phi Lý Trí</h5>
                        <p class="small flex-grow-1">Phân tích thói quen tiêu dùng với ví dụ thực tế sinh động.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>189.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5" id="promo">
    <div class="container">
        <div class="card border-0 text-white promo-card shadow-lg" style="background: linear-gradient(135deg, #172742 0%, #1b3151 45%, #f1592a 100%);">
            <div class="card-body p-4 p-lg-5">
                <div class="row gy-4 align-items-center">
                    <div class="col-lg-6">
                        <span class="badge bg-light text-primary fw-semibold mb-3">Flash Sale cuối tháng</span>
                        <h3 class="fw-bold display-6">Giảm thêm 10% cho đơn sách thiếu nhi</h3>
                        <p class="mb-0">Nhập mã <strong>KIDBOOK10</strong> khi thanh toán. Áp dụng cho 500 đơn đầu tiên, đừng bỏ lỡ nhé!</p>
                    </div>
                    <div class="col-lg-4">
                        <div class="promo-countdown justify-content-center justify-content-lg-start">
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-days">02</div>
                                <div class="countdown-label">Ngày</div>
                            </div>
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-hours">04</div>
                                <div class="countdown-label">Giờ</div>
                            </div>
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-minutes">18</div>
                                <div class="countdown-label">Phút</div>
                            </div>
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-seconds">42</div>
                                <div class="countdown-label">Giây</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-2 text-lg-end text-center">
                        <a href="#" class="btn btn-light btn-lg px-4 fw-semibold">Đặt mua ngay</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5 bg-white">
    <div class="container">
        <div class="section-heading">
            <h2>Tin tức &amp; góc đọc giả</h2>
            <a href="#">Xem blog <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row row-cols-1 row-cols-md-3 g-4">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 position-relative news-card">
                    <div class="card-header">30.05.2024</div>
                    <div class="card-body">
                        <h5 class="card-title fw-semibold text-secondary">Workshop: Đọc sách cùng con</h5>
                        <p>Bí quyết tạo thói quen đọc sách cho trẻ với chuyên gia giáo dục nổi tiếng.</p>
                        <a href="#" class="stretched-link">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 position-relative news-card">
                    <div class="card-header">26.05.2024</div>
                    <div class="card-body">
                        <h5 class="card-title fw-semibold text-secondary">5 tựa sách kinh doanh nên đọc</h5>
                        <p>Cập nhật xu hướng quản trị 2024 cùng những case study đáng học hỏi.</p>
                        <a href="#" class="stretched-link">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 position-relative news-card">
                    <div class="card-header">20.05.2024</div>
                    <div class="card-body">
                        <h5 class="card-title fw-semibold text-secondary">Một ngày ở Góc Xếp Bookstore</h5>
                        <p>Trải nghiệm không gian đọc sách, cà phê và góc sáng tạo dành cho bạn.</p>
                        <a href="#" class="stretched-link">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
const flashSaleEnd = Date.now() + (2 * 24 * 60 * 60 * 1000);

function updatePromoTimer() {
    const now = Date.now();
    const distance = flashSaleEnd - now;

    if (distance <= 0) {
        document.getElementById("timer-days").textContent = "00";
        document.getElementById("timer-hours").textContent = "00";
        document.getElementById("timer-minutes").textContent = "00";
        document.getElementById("timer-seconds").textContent = "00";
        return;
    }

    const days = Math.floor(distance / (1000 * 60 * 60 * 24));
    const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((distance % (1000 * 60)) / 1000);

    document.getElementById("timer-days").textContent = String(days).padStart(2, "0");
    document.getElementById("timer-hours").textContent = String(hours).padStart(2, "0");
    document.getElementById("timer-minutes").textContent = String(minutes).padStart(2, "0");
    document.getElementById("timer-seconds").textContent = String(seconds).padStart(2, "0");
}

updatePromoTimer();
setInterval(updatePromoTimer, 1000);
</script>
