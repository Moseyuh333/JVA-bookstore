<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đơn hàng của tôi | Bookish Bliss Haven</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');
        body { font-family: 'Roboto', sans-serif; }
    </style>
</head>
<body class="bg-gray-50 min-h-screen">
    <nav class="bg-amber-800 text-white shadow-lg">
        <div class="container mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <span class="text-xl font-bold">Bookish Bliss Haven</span>
                </a>
                <div class="hidden md:flex space-x-8">
                    <a href="<%=request.getContextPath()%>/catalog.jsp" class="hover:text-amber-200 font-medium">Danh mục</a>
                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=best" class="hover:text-amber-200 font-medium">Bán chạy</a>
                    <a href="<%=request.getContextPath()%>/cart.jsp" class="hover:text-amber-200 font-medium">Giỏ hàng</a>
                </div>
                <div class="flex items-center space-x-4">
                    <a href="<%=request.getContextPath()%>/cart.jsp" class="relative inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700" aria-label="Giỏ hàng">
                        <i data-feather="shopping-cart" class="w-5 h-5"></i>
                        <span data-cart-count class="hidden absolute -top-1 -right-1 text-xs bg-red-500 text-white rounded-full px-1.5 py-0.5" aria-hidden="true">0</span>
                    </a>
                    <div class="relative">
                        <button id="userDropdownBtn" class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
                            <i data-feather="user" class="w-5 h-5 mr-1"></i>
                            <span id="accountBtnLabel" class="font-medium">Tài khoản</span>
                        </button>
                        <div id="userDropdown" class="hidden absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-lg border border-gray-200 z-50"></div>
                    </div>
                </div>
            </div>
        </div>
    </nav>

    <main class="container mx-auto px-4 py-8">
        <header class="mb-8">
            <h1 class="text-3xl font-bold text-gray-900">Đơn hàng của tôi</h1>
            <p class="text-gray-600 mt-2">Theo dõi trạng thái và chi tiết các đơn hàng bạn đã đặt</p>
        </header>

        <section id="ordersLoading" class="bg-white border border-gray-200 rounded-lg p-6 text-center text-gray-600">
            Đang tải danh sách đơn hàng...
        </section>

        <section id="ordersEmpty" class="hidden bg-white border border-gray-200 rounded-lg p-10 text-center text-gray-600">
            <i data-feather="package" class="w-12 h-12 mx-auto text-gray-400"></i>
            <h2 class="text-2xl font-semibold text-gray-900 mt-4">Chưa có đơn hàng nào</h2>
            <p class="mt-2">Khi hoàn tất thanh toán, đơn hàng của bạn sẽ xuất hiện tại đây.</p>
            <a href="<%=request.getContextPath()%>/catalog.jsp" class="mt-6 inline-block bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-full">Bắt đầu mua sắm</a>
        </section>

        <section id="ordersError" class="hidden bg-red-50 border border-red-200 text-red-700 rounded-lg p-6 text-center"></section>

        <section class="mt-6">
            <div id="ordersList" class="hidden grid grid-cols-1 gap-4"></div>
        </section>
    </main>

    <footer class="bg-gray-900 text-gray-300 py-8 mt-12">
        <div class="container mx-auto px-4 text-center">
            <span class="inline-flex items-center gap-2 bg-gray-800 text-amber-200 px-4 py-2 rounded-full text-sm shadow-sm">
                <i data-feather="shield" class="w-4 h-4"></i>
                <span>&copy; <span id="year"></span> Bookish Bliss Haven</span>
            </span>
        </div>
    </footer>

    <script>
        window.appConfig = {
            contextPath: '<%=request.getContextPath()%>'
        };
    </script>
    <script src="<%=request.getContextPath()%>/assets/js/app-shell.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/orders-page.js"></script>
</body>
</html>
