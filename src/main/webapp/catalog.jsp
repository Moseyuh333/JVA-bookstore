<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><!DOCTYPE html>

<c:set var="pageTitle" value="Bookish Bliss Haven | Danh mục sách" /><html lang="vi">

<!DOCTYPE html><head>

<html lang="vi">    <meta charset="UTF-8">

<%@ include file="/WEB-INF/includes/header.jsp" %>    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Bookish Bliss Haven | Danh mục sách</title>

<main>    <link rel="icon" type="image/x-icon" href="/static/favicon.ico">

    <section class="catalog-hero text-white py-20 px-4">    <script src="https://cdn.tailwindcss.com"></script>

        <div class="container mx-auto text-center">    <script src="https://unpkg.com/feather-icons"></script>

            <span class="uppercase tracking-wide text-amber-200 text-xs font-semibold">Bookish Bliss Haven</span>    <style>

            <h1 class="title-font text-4xl md:text-5xl font-bold my-4">Khám phá kho sách phong phú</h1>        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');

            <p class="max-w-2xl mx-auto text-amber-100">Tìm kiếm, lọc và khám phá những tựa sách được độc giả yêu thích nhất tại cửa hàng của chúng tôi.</p>        body { font-family: 'Roboto', sans-serif; }

        </div>        .title-font { font-family: 'Playfair Display', serif; }

    </section>        .catalog-hero {

            background: linear-gradient(135deg, rgba(120, 53, 15, 0.92), rgba(146, 64, 14, 0.85)), url('http://static.photos/books/1200x630/41');

    <section class="py-12 px-4 bg-gray-50">            background-size: cover;

        <div class="container mx-auto space-y-8">            background-position: center;

            <div class="bg-white rounded-2xl shadow-sm border border-amber-100/60 p-6">        }

                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">        .catalog-card {

                    <div class="flex items-center gap-4">            transition: transform 0.25s ease, box-shadow 0.25s ease;

                        <span class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-amber-100 text-amber-700">        }

                            <i data-feather="filter" class="w-5 h-5"></i>        .catalog-card:hover {

                        </span>            transform: translateY(-6px);

                        <div>            box-shadow: 0 25px 35px -20px rgba(120, 53, 15, 0.5);

                            <h2 class="title-font text-2xl font-semibold">Bộ lọc danh mục</h2>        }

                            <p class="text-gray-500 text-sm">Tùy chỉnh danh sách sách theo nhu cầu đọc của bạn.</p>        .highlight-card {

                        </div>            border-color: #d97706;

                    </div>            box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.4);

                    <div class="flex flex-col md:flex-row gap-4 md:items-center">        }

                        <label class="text-sm font-medium text-gray-600" for="categoryFilter">Thể loại</label>    </style>

                        <select id="categoryFilter" class="w-full md:w-56 rounded-full border-gray-200 focus:border-amber-500 focus:ring-amber-500 text-sm px-4 py-2"></head>

                            <option value="">Tất cả thể loại</option><body class="bg-gray-50">

                        </select>    <nav class="bg-amber-800 text-white shadow-lg">

                    </div>        <div class="container mx-auto px-4 py-4">

                    <div class="flex flex-col md:flex-row gap-4 md:items-center">            <div class="flex justify-between items-center">

                        <label class="text-sm font-medium text-gray-600" for="sortSelect">Sắp xếp theo</label>                <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">

                        <select id="sortSelect" class="w-full md:w-48 rounded-full border-gray-200 focus:border-amber-500 focus:ring-amber-500 text-sm px-4 py-2">                    <i data-feather="book-open" class="w-6 h-6"></i>

                            <option value="new">Mới nhất</option>                    <span class="title-font text-xl font-bold">Bookish Bliss Haven</span>

                            <option value="best">Bán chạy</option>                </a>

                            <option value="rated">Đánh giá cao</option>                <div class="hidden md:flex space-x-8">

                            <option value="priceAsc">Giá thấp đến cao</option>                    <a href="<%=request.getContextPath()%>/index.jsp" class="hover:text-amber-200 font-medium">Trang chủ</a>

                            <option value="priceDesc">Giá cao đến thấp</option>                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hover:text-amber-200 font-medium">Danh mục</a>

                        </select>                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=best" class="hover:text-amber-200 font-medium">Bán chạy</a>

                    </div>                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=rated" class="hover:text-amber-200 font-medium">Đánh giá cao</a>

                </div>                </div>

                <div class="mt-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">                <div class="flex items-center space-x-4">

                    <p id="resultSummary" class="text-sm text-gray-600">Đang tải danh sách sách...</p>                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700">

                    <a href="<%=request.getContextPath()%>/index.jsp" class="inline-flex items-center text-amber-700 hover:text-amber-900 text-sm font-medium">                        <i data-feather="search" class="w-5 h-5 mr-1"></i>

                        Về trang chủ                        <span class="font-medium">Tìm sách</span>

                        <i data-feather="arrow-up-right" class="w-4 h-4 ml-1"></i>                    </a>

                    </a>                    <div class="relative">

                </div>                        <button id="userDropdownBtn" class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">

            </div>                            <i data-feather="user" class="w-5 h-5 mr-1"></i>

                            <span id="accountBtnLabel" class="font-medium">Tài khoản</span>

            <div id="emptyState" class="hidden bg-white border border-dashed border-amber-300 rounded-2xl p-12 text-center">                        </button>

                <div class="flex flex-col items-center gap-4 text-amber-800">                        <div id="userDropdown" class="hidden absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-lg border border-gray-200 z-50"></div>

                    <i data-feather="bookmark" class="w-10 h-10"></i>                    </div>

                    <h3 class="title-font text-xl font-semibold">Chưa có sách phù hợp</h3>                    <button class="md:hidden p-2 rounded-full hover:bg-amber-700" aria-label="Menu">

                    <p class="text-sm text-amber-900/80 max-w-lg">Hãy thử thay đổi bộ lọc hoặc quay lại sau khi chúng tôi cập nhật thêm những tựa sách mới.</p>                        <i data-feather="menu" class="w-5 h-5"></i>

                </div>                    </button>

            </div>                </div>

            </div>

            <div id="catalogGrid" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"></div>        </div>

    </nav>

            <div class="text-center pt-4">

                <button id="loadMoreBtn" type="button" class="inline-flex items-center gap-2 px-6 py-3 rounded-full bg-amber-600 text-white font-semibold hover:bg-amber-700 transition disabled:opacity-60">    <header class="catalog-hero text-white py-16 px-4">

                    <i data-feather="loader" class="w-4 h-4"></i>        <div class="container mx-auto">

                    <span>Tải thêm 20 sách</span>            <div class="max-w-3xl">

                </button>                <span class="uppercase tracking-wide text-amber-200 text-xs font-semibold">Bookish Bliss Haven</span>

                <p id="catalogStatus" class="mt-3 text-sm text-gray-500"></p>                <h1 class="title-font text-4xl md:text-5xl font-bold mt-3 mb-5">Tất cả những cuốn sách bạn yêu thích</h1>

            </div>                <p class="text-amber-100 text-lg">Lọc theo danh mục, sắp xếp theo nhu cầu và khám phá top 20 tựa sách mới nhất, bán chạy nhất, được đánh giá cao và yêu thích nhất trong kho của chúng tôi.</p>

        </div>            </div>

    </section>        </div>

</main>    </header>



<%@ include file="/WEB-INF/includes/footer.jsp" %>    <main class="py-10 px-4">

<script src="<%=request.getContextPath()%>/assets/js/catalog-page.js"></script>        <div class="container mx-auto space-y-8">

</body>            <section class="bg-white rounded-xl shadow-sm border border-gray-100 p-4 md:p-6">

</html>                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">

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
        window.appConfig = {
            contextPath: '<%=request.getContextPath()%>'
        };
    </script>
    <script src="<%=request.getContextPath()%>/assets/js/app-shell.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/catalog-page.js"></script>
</body>
</html>
