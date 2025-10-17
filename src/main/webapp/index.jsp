<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<<<<<<< HEAD
    <!DOCTYPE html>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Bookish Bliss Haven | Home</title>
        <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
        <script src="https://cdn.tailwindcss.com"></script>
        <script src="https://unpkg.com/feather-icons"></script>
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');

            body {
                font-family: 'Roboto', sans-serif;
            }

            .hero-bg {
                background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('http://static.photos/books/1200x630/42');
                background-size: cover;
                background-position: center;
            }

            .book-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            }

            .title-font {
                font-family: 'Playfair Display', serif;
            }
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
                        <a href="<%=request.getContextPath()%>/index.jsp"
                            class="hover:text-amber-200 font-medium">Home</a>
                        <a href="<%=request.getContextPath()%>/shop.jsp"
                            class="hover:text-amber-200 font-medium">Shop</a>
                        <a href="<%=request.getContextPath()%>/collections.jsp"
                            class="hover:text-amber-200 font-medium">Collections</a>
                        <a href="<%=request.getContextPath()%>/about.jsp"
                            class="hover:text-amber-200 font-medium">About</a>
                    </div>
                    <div class="flex items-center space-x-4">
                        <!-- Right-side visible navigation -->
                        <a href="<%=request.getContextPath()%>/shop.jsp"
                            class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">
                            <i data-feather="shopping-bag" class="w-5 h-5 mr-1"></i>
                            <span class="font-medium">Shop</span>
                        </a>
                        <a href="#"
                            class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">
                            <i data-feather="search" class="w-5 h-5 mr-1"></i>
                            <span class="font-medium">Search</span>
                        </a>

                        <!-- Account / Profile Dropdown -->
                        <div class="relative">
                            <button id="userDropdownBtn"
                                class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
                                <i data-feather="user" class="w-5 h-5 mr-1"></i>
                                <span id="accountBtnLabel" class="font-medium">Account</span>
                            </button>
                            <div id="userDropdown"
                                class="hidden absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
                                <div class="py-2">
                                    <a href="<%=request.getContextPath()%>/login.jsp"
                                        class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                        <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                                        Đăng nhập
                                    </a>
                                    <a href="<%=request.getContextPath()%>/register.jsp"
                                        class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                        <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                                        Đăng ký
                                    </a>
                                    <hr class="my-1">
                                    <a href="<%=request.getContextPath()%>/forgot-password.jsp"
                                        class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
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
                <p class="text-xl mb-8 max-w-2xl mx-auto">Explore our curated collection of timeless classics and
                    contemporary masterpieces</p>
                <div class="flex flex-col sm:flex-row justify-center gap-4">
                    <button
                        class="bg-amber-600 hover:bg-amber-700 text-white font-bold py-3 px-8 rounded-full transition duration-300">
                        Browse Collection
                    </button>
                    <button
                        class="bg-white hover:bg-gray-100 text-amber-800 font-bold py-3 px-8 rounded-full transition duration-300">
                        Join Our Book Club
=======
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bookish Bliss Haven | Home</title>
    <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');
        body { font-family: 'Roboto', sans-serif; }
        .hero-bg { background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('http://static.photos/books/1200x630/42'); background-size: cover; background-position: center; }
        .book-card:hover { transform: translateY(-5px); box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); }
        .title-font { font-family: 'Playfair Display', serif; }
        .scrollbar-hide { scrollbar-width: none; -ms-overflow-style: none; }
        .scrollbar-hide::-webkit-scrollbar { display: none; }
        .home-scroll-btn { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(255, 255, 255, 0.92); color: #92400e; padding: 0.6rem; border-radius: 9999px; box-shadow: 0 10px 20px -15px rgba(0, 0, 0, 0.35); transition: background 0.2s ease, color 0.2s ease; z-index: 10; }
        .home-scroll-btn:hover { background: #d97706; color: #fff; }
    </style>
</head>
<body class="bg-gray-50">
    <nav class="bg-amber-800 text-white shadow-lg">
        <div class="container mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <span class="title-font text-xl font-bold">Bookish Bliss Haven</span>
                </a>
                <div class="hidden md:flex space-x-8">
                    <a href="<%=request.getContextPath()%>/index.jsp" class="hover:text-amber-200 font-medium">Trang chủ</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hover:text-amber-200 font-medium">Danh mục</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=best" class="hover:text-amber-200 font-medium">Bán chạy</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=rated" class="hover:text-amber-200 font-medium">Đánh giá cao</a>
                </div>
                <div class="flex items-center space-x-4">
                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">
                        <i data-feather="shopping-bag" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Danh mục</span>
                    </a>
                    <a href="#" class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">
                        <i data-feather="search" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Search</span>
                    </a>
                    <div class="relative">
                        <button id="userDropdownBtn" class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
                            <i data-feather="user" class="w-5 h-5 mr-1"></i>
                            <span id="accountBtnLabel" class="font-medium">Tài khoản</span>
                        </button>
                        <div id="userDropdown" class="hidden absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-lg border border-gray-200 z-50"></div>
                    </div>
                    <button class="md:hidden p-2 rounded-full hover:bg-amber-700" aria-label="Menu">
                        <i data-feather="menu" class="w-5 h-5"></i>
>>>>>>> main
                    </button>
                </div>
            </div>
        </section>

<<<<<<< HEAD
        <!-- Featured Books -->
        <section class="py-16 px-4">
            <div class="container mx-auto">
                <h2 class="title-font text-3xl font-bold text-center mb-12">Featured Books</h2>
                <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-8">
                    <!-- Book Card 1 -->
                    <div
                        class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300 block hover:-translate-y-1 hover:shadow-lg">
                        <a href="${pageContext.request.contextPath}/books/detail?id=1">
                            <img src="https://books.toscrape.com/../media/cache/2c/da/2cdad67c44b002e7ead0cc35693c0e8b.jpg"
                                alt="Book Cover" class="w-full h-64 object-cover">
                            <div class="p-4">
                                <h3 class="title-font font-bold text-lg mb-1">A Light in the Attic</h3>
                                <p class="text-gray-600 text-sm mb-2">Alex Michaelides</p>
                                <div class="flex justify-between items-center">
                                    <span class="font-bold text-amber-700">£51.77</span>
                                    <span class="text-sm text-amber-600 font-semibold">View Details →</span>
                                </div>
                            </div>
                        </a>
                        <div class="p-4 pt-0">
                            <button
                                class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm w-full">
                                Add to Cart
                            </button>
                        </div>
                    </div>

                    <!-- Book Card 2 -->
                    <div
                        class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300 block hover:-translate-y-1 hover:shadow-lg">
                        <a href="${pageContext.request.contextPath}/books/detail?id=2"
                            class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300 block hover:-translate-y-1 hover:shadow-lg">
                            <img src="https://books.toscrape.com/../media/cache/26/0c/260c6ae16bce31c8f8c95daddd9f4a1c.jpg"
                                alt="Book Cover" class="w-full h-64 object-cover">
                            <div class="p-4">
                                <h3 class="title-font font-bold text-lg mb-1">Tipping the Velvet</h3>
                                <p class="text-gray-600 text-sm mb-2">Tara Westover</p>
                                <div class="flex justify-between items-center">
                                    <span class="font-bold text-amber-700">£53.74</span>
                                    <span class="text-sm text-amber-600 font-semibold">View Details →</span>
                                </div>
                            </div>
                        </a>
                        <div class="p-4 pt-0">
                            <button
                                class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm w-full">
                                Add to Cart
                            </button>
                        </div>
                    </div>

                    <!-- Book Card 3 -->
                    <div
                        class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300 block hover:-translate-y-1 hover:shadow-lg">
                        <a href="${pageContext.request.contextPath}/books/detail?id=3"
                            class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300">
                            <img src="https://books.toscrape.com/../media/cache/3e/ef/3eef99c9d9adef34639f510662022830.jpg"
                                alt="Book Cover" class="w-full h-64 object-cover">
                            <div class="p-4">
                                <h3 class="title-font font-bold text-lg mb-1">Soumission</h3>
                                <p class="text-gray-600 text-sm mb-2">Delia Owens</p>
                                <div class="flex justify-between items-center">
                                    <span class="font-bold text-amber-700">£50.10</span>
                                    <span class="text-sm text-amber-600 font-semibold">View Details →</span>
                                </div>
                            </div>
                        </a>
                        <div class="p-4 pt-0">
                            <button
                                class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm w-full">
                                Add to Cart
                            </button>
                        </div>
                    </div>
                    <!-- Book Card 4 -->
                    <div
                        class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300 block hover:-translate-y-1 hover:shadow-lg">
                        <a href="${pageContext.request.contextPath}/books/detail?id=4"
                            class="book-card bg-white rounded-lg overflow-hidden shadow-md transition duration-300">
                            <img src="https://books.toscrape.com/../media/cache/32/51/3251cf3a3412f53f339e42cac2134093.jpg"
                                alt="Book Cover" class="w-full h-64 object-cover">
                            <div class="p-4">
                                <h3 class="title-font font-bold text-lg mb-1">Sharp Objects</h3>
                                <p class="text-gray-600 text-sm mb-2">James Clear</p>
                                <div class="flex justify-between items-center">
                                    <span class="font-bold text-amber-700">£47.82</span>
                                    <span class="text-sm text-amber-600 font-semibold">View Details →</span>
                                </div>
                            </div>
                        </a>
                        <div class="p-4 pt-0">
                            <button
                                class="bg-amber-600 hover:bg-amber-700 text-white px-3 py-1 rounded-full text-sm w-full">
                                Add to Cart
                            </button>
                        </div>
                    </div>
=======
    <section class="hero-bg text-white py-32 px-4">
        <div class="container mx-auto text-center">
            <h1 class="title-font text-4xl md:text-6xl font-bold mb-6">Discover Your Next Favorite Read</h1>
            <p class="text-xl mb-8 max-w-2xl mx-auto">Explore our curated collection of timeless classics and contemporary masterpieces</p>
            <div class="flex flex-col sm:flex-row justify-center gap-4">
                <a href="<%=request.getContextPath()%>/catalog.jsp" class="bg-amber-600 hover:bg-amber-700 text-white font-bold py-3 px-8 rounded-full transition duration-300">
                    Khám phá danh mục
                </a>
                <a href="#newsletter" class="bg-white hover:bg-gray-100 text-amber-800 font-bold py-3 px-8 rounded-full transition duration-300">
                    Tham gia cộng đồng
                </a>
            </div>
        </div>
    </section>

    <section class="py-16 px-4" id="featuredSections">
        <div class="container mx-auto">
            <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-6 mb-8">
                <div>
                    <h2 class="title-font text-3xl font-bold">Khám phá nổi bật</h2>
                    <p class="text-gray-600 mt-2">Top 20 sách mới, bán chạy, được đánh giá và yêu thích nhất</p>
                </div>
                <a href="<%=request.getContextPath()%>/catalog.jsp" class="inline-flex items-center text-amber-700 hover:text-amber-900 font-medium">
                    Xem toàn bộ danh mục
                    <i data-feather="arrow-right" class="w-4 h-4 ml-2"></i>
                </a>
            </div>
            <div id="homeSectionsContainer" class="space-y-16">
                <div id="homeSectionsLoading" class="text-center py-12 text-gray-500">
                    Đang tải gợi ý sách...
>>>>>>> main
                </div>
            </div>
        </section>

<<<<<<< HEAD
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
                        <p class="text-gray-700">Những cuốn sách được chọn lọc bởi đội ngũ chuyên gia văn học để đảm bảo
                            chất lượng đọc.</p>
=======
    <section class="bg-amber-50 py-16 px-4">
        <div class="container mx-auto">
            <h2 class="title-font text-3xl font-bold text-center mb-12">Tại sao chọn chúng tôi</h2>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
                <div class="text-center p-6">
                    <div class="bg-amber-100 w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-4">
                        <i data-feather="award" class="w-8 h-8 text-amber-700"></i>
>>>>>>> main
                    </div>
                    <div class="text-center p-6">
                        <div class="bg-amber-100 w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-4">
                            <i data-feather="truck" class="w-8 h-8 text-amber-700"></i>
                        </div>
                        <h3 class="title-font font-bold text-xl mb-2">Giao hàng nhanh</h3>
                        <p class="text-gray-700">Nhận sách nhanh chóng với đối tác vận chuyển đáng tin cậy của chúng
                            tôi.</p>
                    </div>
                    <div class="text-center p-6">
                        <div class="bg-amber-100 w-16 h-16 mx-auto rounded-full flex items-center justify-center mb-4">
                            <i data-feather="heart" class="w-8 h-8 text-amber-700"></i>
                        </div>
                        <h3 class="title-font font-bold text-xl mb-2">Cộng đồng đọc sách</h3>
                        <p class="text-gray-700">Tham gia cộng đồng những người yêu sách sôi động để thảo luận và sự
                            kiện.</p>
                    </div>
                </div>
            </div>
        </section>

<<<<<<< HEAD
        <!-- Testimonials -->
        <section class="py-16 px-4 bg-white">
            <div class="container mx-auto">
                <h2 class="title-font text-3xl font-bold text-center mb-12">Độc giả nói gì về chúng tôi</h2>
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    <!-- Testimonial 1 -->
                    <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
                        <div class="flex items-center mb-4">
                            <img src="http://static.photos/people/100x100/1" alt="Reader"
                                class="w-12 h-12 rounded-full mr-4 object-cover">
                            <div>
                                <h4 class="font-bold">Sarah Johnson</h4>
                                <div class="flex text-amber-500">
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                </div>
=======
    <section class="py-16 px-4 bg-white">
        <div class="container mx-auto">
            <h2 class="title-font text-3xl font-bold text-center mb-12">Độc giả nói gì về chúng tôi</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
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
>>>>>>> main
                            </div>
                        </div>
                        <p class="text-gray-700 italic">"Lựa chọn sách tuyệt vời nhất mà tôi tìm thấy trực tuyến! Những
                            gợi ý của họ luôn chính xác."</p>
                    </div>
<<<<<<< HEAD
                    <!-- Testimonial 2 -->
                    <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
                        <div class="flex items-center mb-4">
                            <img src="http://static.photos/people/100x100/2" alt="Reader"
                                class="w-12 h-12 rounded-full mr-4 object-cover">
                            <div>
                                <h4 class="font-bold">Michael Chen</h4>
                                <div class="flex text-amber-500">
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                    <i data-feather="star" class="w-4 h-4 fill-current"></i>
                                </div>
=======
                    <p class="text-gray-700 italic">"Lựa chọn sách tuyệt vời nhất mà tôi tìm thấy trực tuyến! Những gợi ý của họ luôn chính xác."</p>
                </div>
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
>>>>>>> main
                            </div>
                        </div>
                        <p class="text-gray-700 italic">"Giao hàng nhanh và đóng gói tuyệt vời. Sách của tôi luôn đến
                            trong tình trạng hoàn hảo."</p>
                    </div>
<<<<<<< HEAD
                    <!-- Testimonial 3 -->
                    <div class="bg-gray-50 p-6 rounded-lg shadow-sm">
                        <div class="flex items-center mb-4">
                            <img src="http://static.photos/people/100x100/3" alt="Reader"
                                class="w-12 h-12 rounded-full mr-4 object-cover">
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
                        <p class="text-gray-700 italic">"Yêu thích danh sách đọc theo mùa và gợi ý câu lạc bộ sách. Tìm
                            thấy rất nhiều cuốn sách yêu thích mới!"</p>
=======
                    <p class="text-gray-700 italic">"Giao hàng nhanh và đóng gói tuyệt vời. Sách của tôi luôn đến trong tình trạng hoàn hảo."</p>
                </div>
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

    <section class="bg-amber-800 text-white py-16 px-4" id="newsletter">
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

    <footer class="bg-gray-900 text-gray-300 py-12 px-4">
        <div class="container mx-auto">
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
>>>>>>> main
                    </div>
                </div>
            </div>
        </section>

        <!-- Newsletter -->
        <section class="bg-amber-800 text-white py-16 px-4">
            <div class="container mx-auto max-w-4xl text-center">
                <h2 class="title-font text-3xl font-bold mb-4">Cập nhật tin tức văn học</h2>
                <p class="mb-8 text-amber-100 max-w-2xl mx-auto">Đăng ký nhận bản tin để biết về sách mới, ưu đãi độc
                    quyền và gợi ý đọc sách.</p>
                <form class="flex flex-col sm:flex-row gap-4 max-w-md mx-auto sm:max-w-xl">
                    <input type="email" placeholder="Địa chỉ email của bạn"
                        class="flex-grow px-4 py-3 rounded-full text-gray-800 focus:outline-none focus:ring-2 focus:ring-amber-500">
                    <button type="submit"
                        class="bg-white hover:bg-gray-100 text-amber-800 font-bold py-3 px-6 rounded-full transition duration-300 whitespace-nowrap">
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
                    <span
                        class="inline-flex items-center gap-2 bg-gray-800 text-amber-200 px-4 py-2 rounded-full text-sm shadow-sm">
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
                                <a href="mailto:info@bookishhaven.com"
                                    class="hover:text-white">info@bookishhaven.com</a>
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

<<<<<<< HEAD
        <script>
            feather.replace();

            // User dropdown functionality
            document.addEventListener('DOMContentLoaded', function () {
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
                    userDropdownBtn.addEventListener('click', function (e) {
                        e.stopPropagation();
                        userDropdown.classList.toggle('hidden');
                    });

                    // Close dropdown when clicking outside
                    document.addEventListener('click', function () {
                        userDropdown.classList.add('hidden');
                    });

                    // Prevent dropdown from closing when clicking inside it
                    userDropdown.addEventListener('click', function (e) {
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
=======
    <script>
        window.appConfig = {
            contextPath: '<%=request.getContextPath()%>'
        };
    </script>
    <script src="<%=request.getContextPath()%>/assets/js/app-shell.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/home-page.js"></script>
</body>
</html>
>>>>>>> main
