<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng | Bookish Bliss Haven</title>
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
                    <a href="<%=request.getContextPath()%>/catalog.jsp?sort=rated" class="hover:text-amber-200 font-medium">Đánh giá cao</a>
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
        <header class="mb-8 flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
            <div>
                <h1 class="text-3xl font-bold text-gray-900">Giỏ hàng của bạn</h1>
                <p class="text-gray-600 mt-2">Quản lý sản phẩm và chuẩn bị cho bước thanh toán</p>
            </div>
            <a href="<%=request.getContextPath()%>/catalog.jsp" class="inline-flex items-center text-amber-700 hover:text-amber-900 font-medium">
                Tiếp tục mua sắm
                <i data-feather="arrow-right" class="w-4 h-4 ml-2"></i>
            </a>
        </header>

        <section id="cartLoading" class="bg-white border border-gray-200 rounded-lg p-6 text-center text-gray-600">
            Đang tải giỏ hàng...
        </section>

        <section id="cartError" class="hidden bg-red-50 border border-red-200 text-red-700 rounded-lg p-6"></section>

        <section id="cartEmpty" class="hidden bg-white border border-gray-200 rounded-lg p-12 text-center">
            <div class="max-w-md mx-auto">
                <i data-feather="shopping-bag" class="w-12 h-12 mx-auto text-gray-400"></i>
                <h2 class="text-2xl font-semibold text-gray-900 mt-4">Giỏ hàng trống</h2>
                <p class="text-gray-600 mt-2">Hãy khám phá thêm sách và thêm vào giỏ của bạn.</p>
                <a href="<%=request.getContextPath()%>/catalog.jsp" class="mt-6 inline-block bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-full">Khám phá sách</a>
            </div>
        </section>

        <section id="cartContent" class="hidden grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div class="lg:col-span-2 space-y-4" id="cartItems"></div>
            <aside class="bg-white border border-gray-200 rounded-lg p-6 h-fit">
                <h2 class="text-xl font-semibold text-gray-900 mb-4">Tổng kết đơn hàng</h2>
                <dl class="space-y-2 text-sm text-gray-600">
                    <div class="flex justify-between">
                        <dt>Tạm tính</dt>
                        <dd id="cartSubtotal" class="font-medium text-gray-900">0</dd>
                    </div>
                    <div class="flex justify-between">
                        <dt>Phí vận chuyển</dt>
                        <dd class="text-green-600">Miễn phí</dd>
                    </div>
                    <div class="flex justify-between border-t border-gray-200 pt-3 text-base">
                        <dt class="font-semibold text-gray-900">Thành tiền</dt>
                        <dd id="cartTotal" class="font-semibold text-amber-700">0</dd>
                    </div>
                </dl>
                <div class="mt-4 text-sm text-gray-500">
                    <span id="cartItemCount">0</span> sản phẩm · Đơn vị tiền: <span id="cartCurrency">VND</span>
                </div>
                <div class="mt-6 flex flex-col gap-3">
                    <button id="checkoutBtn" class="bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-full px-4 py-3 transition">Tiến hành thanh toán</button>
                    <button id="clearCartBtn" class="border border-gray-300 text-gray-700 hover:border-red-400 hover:text-red-500 rounded-full px-4 py-3 transition">Xóa giỏ hàng</button>
                </div>
            </aside>
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
    <script src="<%=request.getContextPath()%>/assets/js/cart-page.js"></script>
</body>
</html>
