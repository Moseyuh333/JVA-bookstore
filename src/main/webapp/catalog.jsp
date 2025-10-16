<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bookish Bliss Haven | Danh mục sách</title>
    <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');
        body { font-family: 'Roboto', sans-serif; }
        .title-font { font-family: 'Playfair Display', serif; }
        .catalog-hero {
            background: linear-gradient(135deg, rgba(120, 53, 15, 0.92), rgba(146, 64, 14, 0.85)), url('http://static.photos/books/1200x630/41');
            background-size: cover;
            background-position: center;
        }
        .catalog-card {
            transition: transform 0.25s ease, box-shadow 0.25s ease;
        }
        .catalog-card:hover {
            transform: translateY(-6px);
            box-shadow: 0 25px 35px -20px rgba(120, 53, 15, 0.5);
        }
        .highlight-card {
            border-color: #d97706;
            box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.4);
        }
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
                        <i data-feather="search" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Tìm sách</span>
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
                    </button>
                </div>
            </div>
        </div>
    </nav>

    <header class="catalog-hero text-white py-16 px-4">
        <div class="container mx-auto">
            <div class="max-w-3xl">
                <span class="uppercase tracking-wide text-amber-200 text-xs font-semibold">Bookish Bliss Haven</span>
                <h1 class="title-font text-4xl md:text-5xl font-bold mt-3 mb-5">Tất cả những cuốn sách bạn yêu thích</h1>
                <p class="text-amber-100 text-lg">Lọc theo danh mục, sắp xếp theo nhu cầu và khám phá top 20 tựa sách mới nhất, bán chạy nhất, được đánh giá cao và yêu thích nhất trong kho của chúng tôi.</p>
            </div>
        </div>
    </header>

    <main class="py-10 px-4">
        <div class="container mx-auto space-y-8">
            <section class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 md:p-6">
                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                    <div class="flex flex-col md:flex-row md:items-center gap-4">
                        <div>
                            <label for="categoryFilter" class="block text-sm font-medium text-gray-600 mb-1">Danh mục</label>
                            <select id="categoryFilter" class="min-w-[220px] px-4 py-2.5 border border-gray-200 rounded-full focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm">
                                <option value="">Tất cả</option>
                            </select>
                        </div>
                        <div>
                            <label for="sortSelect" class="block text-sm font-medium text-gray-600 mb-1">Sắp xếp theo</label>
                            <select id="sortSelect" class="min-w-[220px] px-4 py-2.5 border border-gray-200 rounded-full focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm">
                                <option value="new">Sản phẩm mới</option>
                                <option value="best">Bán chạy nhất</option>
                                <option value="rated">Đánh giá cao nhất</option>
                                <option value="favorite">Được yêu thích</option>
                            </select>
                        </div>
                    </div>
                    <div class="flex items-center gap-3 text-sm text-gray-500">
                        <span id="resultSummary">Đang tải dữ liệu...</span>
                    </div>
                </div>
            </section>

            <section class="space-y-6">
                <div id="catalogGrid" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-4 gap-6"></div>
                <div id="emptyState" class="hidden text-center py-16 bg-white rounded-xl border border-dashed border-amber-200 text-gray-500">
                    Không tìm thấy sách phù hợp với bộ lọc hiện tại.
                </div>
                <div class="flex flex-col items-center gap-3">
                    <button id="loadMoreBtn" class="hidden px-6 py-3 bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-full transition duration-200">
                        Tải thêm 20 sách
                    </button>
                    <p id="catalogStatus" class="text-sm text-gray-500"></p>
                </div>
            </section>
        </div>
    </main>

    <footer class="bg-gray-900 text-gray-300 py-12 px-4">
        <div class="container mx-auto">
            <div class="flex justify-center mb-10">
                <span class="inline-flex items-center gap-2 bg-gray-800 text-amber-200 px-4 py-2 rounded-full text-sm shadow-sm">
                    <i data-feather="shield" class="w-4 h-4"></i>
                    <span>&copy; <span id="year"></span> Bookish Bliss Haven · Mọi quyền được bảo lưu</span>
                </span>
            </div>
            <div class="text-center text-sm text-gray-500">
                Khám phá, đọc và chia sẻ niềm đam mê sách của bạn cùng chúng tôi.
            </div>
        </div>
    </footer>

    <script>
        const contextPath = '<%=request.getContextPath()%>';
        const booksApiBase = `${contextPath}/api/books`;
        const pageSize = 20;

        let currentPage = 1;
        let totalPages = 0;
        let isLoading = false;
        let currentSort = 'new';
        let currentCategory = '';
        let highlightId = null;

        document.addEventListener('DOMContentLoaded', () => {
            feather.replace();
            initUserDropdown();
            readInitialParams();
            fetchCategories();
            fetchBooks(true);
            updateYearBadge();
            setupLoadMoreObserver();
        });

        function readInitialParams() {
            const params = new URLSearchParams(window.location.search);
            const sortParam = params.get('sort');
            const categoryParam = params.get('category');
            const highlightParam = params.get('highlight');

            if (sortParam) {
                currentSort = sortParam;
                const sortSelect = document.getElementById('sortSelect');
                if (sortSelect) {
                    sortSelect.value = sortParam;
                }
            }
            if (categoryParam) {
                currentCategory = categoryParam;
                const categorySelect = document.getElementById('categoryFilter');
                if (categorySelect) {
                    categorySelect.value = categoryParam;
                }
            }
            if (highlightParam) {
                const parsed = parseInt(highlightParam, 10);
                if (!Number.isNaN(parsed)) {
                    highlightId = parsed;
                }
            }
        }

        async function fetchCategories() {
            try {
                const response = await fetch(`${booksApiBase}/categories`);
                if (!response.ok) {
                    throw new Error('Failed to load categories');
                }
                const payload = await response.json();
                const categories = Array.isArray(payload.data) ? payload.data : [];
                const select = document.getElementById('categoryFilter');
                if (select) {
                    categories.forEach(category => {
                        const option = document.createElement('option');
                        option.value = category;
                        option.textContent = category;
                        if (category === currentCategory) {
                            option.selected = true;
                        }
                        select.appendChild(option);
                    });
                    select.addEventListener('change', () => {
                        currentCategory = select.value;
                        resetAndFetch();
                    });
                }
                const sortSelect = document.getElementById('sortSelect');
                if (sortSelect) {
                    sortSelect.addEventListener('change', () => {
                        currentSort = sortSelect.value;
                        resetAndFetch();
                    });
                }
            } catch (error) {
                console.error('Categories error', error);
            }
        }

        function resetAndFetch() {
            currentPage = 1;
            totalPages = 0;
            fetchBooks(true);
            updateUrlState();
        }

        async function fetchBooks(reset) {
            if (isLoading) {
                return;
            }
            if (!reset && totalPages !== 0 && currentPage > totalPages) {
                updateLoadMoreState();
                return;
            }
            isLoading = true;
            toggleLoadingState(true);
            try {
                const params = new URLSearchParams();
                params.append('page', currentPage.toString());
                params.append('size', pageSize.toString());
                params.append('sort', currentSort);
                if (currentCategory) {
                    params.append('category', currentCategory);
                }
                const response = await fetch(`${booksApiBase}?${params.toString()}`);
                if (!response.ok) {
                    throw new Error('Failed to load books');
                }
                const payload = await response.json();
                totalPages = payload.totalPages || 0;
                updateSummary(payload.totalItems || 0);
                renderBooks(Array.isArray(payload.data) ? payload.data : [], reset);
                updateLoadMoreState();
                currentPage += 1;
            } catch (error) {
                console.error('Books error', error);
                showStatus('Không thể tải danh sách sách. Vui lòng thử lại sau.');
            } finally {
                toggleLoadingState(false);
                isLoading = false;
                feather.replace();
            }
        }

        function renderBooks(books, reset) {
            const grid = document.getElementById('catalogGrid');
            const empty = document.getElementById('emptyState');
            if (!grid) {
                return;
            }
            if (reset) {
                grid.innerHTML = '';
            }
            if (books.length === 0 && (reset || grid.children.length === 0)) {
                empty.classList.remove('hidden');
                return;
            }
            empty.classList.add('hidden');
            const fragment = document.createDocumentFragment();
            books.forEach(book => {
                const card = document.createElement('article');
                card.className = 'catalog-card bg-white rounded-xl border border-gray-100 overflow-hidden flex flex-col';
                card.dataset.bookId = book.id;
                card.innerHTML = buildCatalogCard(book);
                fragment.appendChild(card);
            });
            grid.appendChild(fragment);
            highlightIfNeeded();
        }

        function buildCatalogCard(book) {
            const title = escapeHtml(book.title || 'Sách chưa cập nhật');
            const author = escapeHtml(book.author || 'Đang cập nhật');
            const price = formatCurrency(book.price);
            const image = book.imageUrl || 'https://placehold.co/320x420?text=Book';
            const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
            const ratingCount = book.ratingCount || 0;
            const favoriteCount = book.favoriteCount || 0;
            const sold = book.totalSold || 0;
            return `
                <div class="relative">
                    <img src="${image}" alt="${title}" class="w-full h-64 object-cover">
                    <div class="absolute top-3 left-3 bg-white/90 text-amber-700 text-xs font-semibold px-2 py-1 rounded-full shadow-sm">
                        ${rating} ★ (${ratingCount})
                    </div>
                </div>
                <div class="p-5 flex flex-col flex-grow">
                    <h3 class="title-font text-xl font-semibold mb-2">${title}</h3>
                    <p class="text-gray-500 text-sm mb-3">${author}</p>
                    <p class="text-sm text-gray-500 mb-3">Đã bán: <span class="font-medium text-gray-700">${sold}</span> · Yêu thích: <span class="font-medium text-gray-700">${favoriteCount}</span></p>
                    <p class="text-amber-700 font-bold text-lg mb-4">${price}</p>
                    <div class="mt-auto flex flex-col sm:flex-row gap-3">
                        <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-semibold py-2 px-4 rounded-full text-sm transition" data-book-id="${book.id}">
                            Thêm vào giỏ
                        </button>
                        <a href="${contextPath}/catalog.jsp?highlight=${book.id}" class="text-center text-sm text-amber-700 hover:text-amber-900 font-medium">
                            Xem chi tiết
                        </a>
                    </div>
                </div>
            `;
        }

        function highlightIfNeeded() {
            if (!highlightId) {
                return;
            }
            const grid = document.getElementById('catalogGrid');
            const card = grid ? grid.querySelector(`[data-book-id="${highlightId}"]`) : null;
            if (card) {
                card.classList.add('highlight-card');
                card.scrollIntoView({ behavior: 'smooth', block: 'center' });
                highlightId = null;
            }
        }

        function toggleLoadingState(isLoadingNow) {
            const button = document.getElementById('loadMoreBtn');
            const status = document.getElementById('catalogStatus');
            if (button) {
                button.disabled = isLoadingNow;
                if (isLoadingNow) {
                    button.textContent = 'Đang tải...';
                } else {
                    button.textContent = 'Tải thêm 20 sách';
                }
            }
            if (status && isLoadingNow) {
                status.textContent = 'Đang tải...';
            }
        }

        function updateLoadMoreState() {
            const button = document.getElementById('loadMoreBtn');
            const status = document.getElementById('catalogStatus');
            if (!button || !status) {
                return;
            }
            if (currentPage > totalPages || totalPages === 0) {
                button.classList.add('hidden');
            } else {
                button.classList.remove('hidden');
            }
            if (totalPages === 0) {
                status.textContent = '';
            } else {
                status.textContent = `Trang ${Math.min(currentPage, totalPages)} trên ${totalPages}`;
            }
            button.onclick = () => {
                fetchBooks(false);
            };
        }

        function updateSummary(totalItems) {
            const summary = document.getElementById('resultSummary');
            if (!summary) {
                return;
            }
            if (totalItems === 0) {
                summary.textContent = 'Không có sách nào phù hợp bộ lọc hiện tại.';
            } else {
                summary.textContent = `Đã tìm thấy ${totalItems} tựa sách.`;
            }
        }

        function showStatus(message) {
            const status = document.getElementById('catalogStatus');
            if (status) {
                status.textContent = message;
            }
        }

        function updateUrlState() {
            const params = new URLSearchParams();
            if (currentCategory) {
                params.set('category', currentCategory);
            }
            if (currentSort && currentSort !== 'new') {
                params.set('sort', currentSort);
            }
            const query = params.toString();
            const url = query ? `${window.location.pathname}?${query}` : window.location.pathname;
            window.history.replaceState({}, '', url);
        }

        function setupLoadMoreObserver() {
            const button = document.getElementById('loadMoreBtn');
            if (!('IntersectionObserver' in window) || !button) {
                return;
            }
            const observer = new IntersectionObserver(entries => {
                entries.forEach(entry => {
                    if (entry.isIntersecting && !isLoading && currentPage <= totalPages) {
                        fetchBooks(false);
                    }
                });
            }, { root: null, threshold: 0.25 });
            observer.observe(button);
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
            return text.replace(/[&<>"']/g, match => {
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
