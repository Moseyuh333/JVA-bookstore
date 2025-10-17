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
                    <a href="<%=request.getContextPath()%>/index.jsp" class="hover:text-amber-200 font-medium">Trang chủ</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hover:text-amber-200 font-medium">Danh mục</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=best" class="hover:text-amber-200 font-medium">Bán chạy</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=rated" class="hover:text-amber-200 font-medium">Đánh giá cao</a>
                </div>
                <div class="flex items-center space-x-4">
                    <!-- Right-side visible navigation -->
                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">
                        <i data-feather="shopping-bag" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Danh mục</span>
                    </a>
                    <a href="#" class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">
                        <i data-feather="search" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Search</span>
                    </a>

                    <!-- Account / Profile Dropdown -->
                    <div class="relative">
                        <button id="userDropdownBtn" class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
                            <i data-feather="user" class="w-5 h-5 mr-1"></i>
                            <span id="accountBtnLabel" class="font-medium">Tài khoản</span>
                        </button>
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
                <a href="<%=request.getContextPath()%>/catalog.jsp" class="bg-amber-600 hover:bg-amber-700 text-white font-bold py-3 px-8 rounded-full transition duration-300">
                    Khám phá danh mục
                </a>
                <a href="#newsletter" class="bg-white hover:bg-gray-100 text-amber-800 font-bold py-3 px-8 rounded-full transition duration-300">
                    Tham gia cộng đồng
                </a>
            </div>
            <div class="mt-6 flex flex-wrap justify-center gap-3 text-sm">
                <a href="<%=request.getContextPath()%>/login.jsp" class="inline-flex items-center gap-2 bg-white/90 hover:bg-white text-amber-800 font-semibold py-2 px-4 rounded-full transition duration-300">
                    <i data-feather="log-in" class="w-4 h-4"></i>
                    Đăng nhập
                </a>
                <a href="<%=request.getContextPath()%>/register.jsp" class="inline-flex items-center gap-2 bg-amber-600/90 hover:bg-amber-600 text-white font-semibold py-2 px-4 rounded-full transition duration-300">
                    <i data-feather="user-plus" class="w-4 h-4"></i>
                    Đăng ký tài khoản
                </a>
                <a href="<%=request.getContextPath()%>/forgot-password.jsp" class="inline-flex items-center gap-2 bg-black/40 hover:bg-black/50 text-white font-semibold py-2 px-4 rounded-full transition duration-300">
                    <i data-feather="key" class="w-4 h-4"></i>
                    Quên mật khẩu
                </a>
            </div>
        </div>
    </section>

    <!-- Dynamic Featured Sections -->
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
        const contextPath = '<%=request.getContextPath()%>';
        const booksApiBase = `${contextPath}/api/books`;

        document.addEventListener('DOMContentLoaded', () => {
            feather.replace();
            initUserDropdown();
            loadHomeSections();
            updateYearBadge();
        });

        async function loadHomeSections() {
            const container = document.getElementById('homeSectionsContainer');
            const loading = document.getElementById('homeSectionsLoading');
            if (!container) {
                return;
            }
            try {
                const response = await fetch(`${booksApiBase}/sections?limit=8`);
                if (!response.ok) {
                    throw new Error('Failed to load sections');
                }
                const payload = await response.json();
                container.innerHTML = '';
                if (payload.sections && payload.sections.length > 0) {
                    payload.sections.forEach(section => {
                        container.appendChild(renderSection(section));
                    });
                } else {
                    container.innerHTML = renderEmptyState();
                }
            } catch (error) {
                console.error('Load sections error', error);
                if (loading) {
                    loading.textContent = 'Không thể tải dữ liệu sách. Vui lòng thử lại sau.';
                }
            } finally {
                feather.replace();
            }
        }

        function renderSection(section) {
            const wrapper = document.createElement('div');
            wrapper.className = 'space-y-6';
            const safeBooks = Array.isArray(section.books) ? section.books.slice(0, 4) : [];
            const sortKey = encodeURIComponent(section.sort || 'new');
            const cardsHtml = safeBooks.length > 0 ? safeBooks.map(renderBookCard).join('') : renderSkeletonCards();
            wrapper.innerHTML = `
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <h3 class="title-font text-2xl font-bold">${section.title || 'Danh mục'}</h3>
                        <p class="text-gray-500 text-sm">Những tựa sách nổi bật được độc giả quan tâm</p>
                    </div>
                    <a href="${contextPath}/catalog.jsp?sort=${sortKey}" class="inline-flex items-center text-amber-700 hover:text-amber-900 text-sm font-medium">
                        Xem tất cả
                        <i data-feather="arrow-right" class="w-4 h-4 ml-1"></i>
                    </a>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                    ${cardsHtml}
                </div>
            `;
            return wrapper;
        }

        function renderBookCard(book) {
            const title = book.title || 'Sách chưa cập nhật';
            const author = book.author || 'Đang cập nhật';
            const price = formatCurrency(book.price);
            const image = book.imageUrl || 'https://placehold.co/320x420?text=Book';
            const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
            const ratingCount = book.ratingCount || 0;
            return `
                <div class="book-card bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 transition duration-300 flex flex-col">
                    <div class="relative">
                        <img src="${image}" alt="${escapeHtml(title)}" class="w-full h-56 object-cover">
                        <span class="absolute top-3 left-3 bg-white/90 text-amber-700 text-xs font-semibold px-2 py-1 rounded-full shadow-sm">
                            ${rating} ★ (${ratingCount})
                        </span>
                    </div>
                    <div class="p-5 flex flex-col flex-grow">
                        <h4 class="title-font font-semibold text-lg mb-1">${escapeHtml(title)}</h4>
                        <p class="text-gray-500 text-sm mb-3">${escapeHtml(author)}</p>
                        <p class="text-amber-700 font-bold mb-4">${price}</p>
                        <div class="mt-auto flex flex-col gap-2">
                            <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-medium py-2 px-4 rounded-full text-sm transition" data-book-id="${book.id}">
                                Thêm vào giỏ
                            </button>
                            <a href="${contextPath}/catalog.jsp?highlight=${book.id}" class="text-center text-sm text-amber-700 hover:text-amber-900 font-medium">
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                </div>
            `;
        }

        function renderSkeletonCards() {
            return Array.from({ length: 4 }).map(() => `
                <div class="bg-white border border-dashed border-amber-200 rounded-xl h-56 flex items-center justify-center text-amber-400 text-sm">
                    Đang cập nhật
                </div>
            `).join('');
        }

        function renderEmptyState() {
            return `
                <div class="text-center py-16 bg-white rounded-xl border border-dashed border-amber-200 text-gray-500">
                    Chưa có dữ liệu sách để hiển thị. Hãy thêm sách trong kho dữ liệu.
                </div>
            `;
        }

        function formatCurrency(value) {
            if (value === null || value === undefined) {
                return 'Liên hệ';
            }
            try {
                return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
            } catch (error) {
                return value.toString();
            }
        }

        function escapeHtml(text) {
            if (!text) {
                return '';
            }
            return text.replace(/[&<>"']/g, function(match) {
                switch (match) {
                    case '&': return '&amp;';
                    case '<': return '&lt;';
                    case '>': return '&gt;';
                    case '"': return '&quot;';
                    case "'": return '&#39;';
                    default: return match;
                }
            });
        }

        function initUserDropdown() {
            const userDropdownBtn = document.getElementById('userDropdownBtn');
            const userDropdown = document.getElementById('userDropdown');
            const token = localStorage.getItem('auth_token');
            const isLoggedIn = token && token.length > 0;

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

                document.addEventListener('click', function() {
                    userDropdown.classList.add('hidden');
                });

                userDropdown.addEventListener('click', function(e) {
                    e.stopPropagation();
                });
            }
        }

        function updateDropdownForLoggedInUser() {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) {
                userDropdown.innerHTML = `
                    <div class="py-2">
                        <div class="px-4 py-2 text-sm text-gray-600 border-b">
                            <i data-feather="user" class="w-4 h-4 inline mr-2"></i>
                            Xin chào!
                        </div>
                        <a href="${contextPath}/profile.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="settings" class="w-4 h-4 mr-2"></i>
                            Hồ sơ cá nhân
                        </a>
                        <a href="#" onclick="logout(); return false;" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="log-out" class="w-4 h-4 mr-2"></i>
                            Đăng xuất
                        </a>
                    </div>
                `;
                const lbl = document.getElementById('accountBtnLabel');
                if (lbl) {
                    lbl.textContent = 'Hồ sơ';
                }
                feather.replace();
            }
        }

        function updateDropdownForGuestUser() {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) {
                userDropdown.innerHTML = `
                    <div class="py-2">
                        <a href="${contextPath}/login.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                            Đăng nhập
                        </a>
                        <a href="${contextPath}/register.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                            Đăng ký
                        </a>
                        <hr class="my-1">
                        <a href="${contextPath}/forgot-password.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="key" class="w-4 h-4 mr-2"></i>
                            Quên mật khẩu
                        </a>
                    </div>
                `;
                const lbl = document.getElementById('accountBtnLabel');
                if (lbl) {
                    lbl.textContent = 'Tài khoản';
                }
                feather.replace();
            }
        }

        function logout() {
            localStorage.removeItem('auth_token');
            updateDropdownForGuestUser();
            alert('Đăng xuất thành công!');
            window.location.reload();
        }

        function updateYearBadge() {
            const badge = document.getElementById('year');
            if (badge) {
                badge.textContent = new Date().getFullYear();
            }
        }
    </script>
</body>
</html>
