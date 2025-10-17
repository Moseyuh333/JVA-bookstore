<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thanh toán | Bookish Bliss Haven</title>
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
            <h1 class="text-3xl font-bold text-gray-900">Thanh toán</h1>
            <p class="text-gray-600 mt-2">Hoàn tất thông tin để đặt hàng</p>
        </header>

        <section id="checkoutLoading" class="bg-white border border-gray-200 rounded-lg p-6 text-center text-gray-600">
            Đang chuẩn bị đơn hàng của bạn...
        </section>

        <section id="checkoutEmpty" class="hidden bg-white border border-gray-200 rounded-lg p-10 text-center">
            <div class="max-w-md mx-auto">
                <i data-feather="shopping-cart" class="w-12 h-12 mx-auto text-gray-400"></i>
                <h2 class="text-2xl font-semibold text-gray-900 mt-4">Giỏ hàng trống</h2>
                <p class="text-gray-600 mt-2">Hãy thêm ít nhất một sản phẩm trước khi thanh toán.</p>
                <a href="<%=request.getContextPath()%>/catalog.jsp" class="mt-6 inline-block bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-full">Khám phá sách</a>
            </div>
        </section>

        <section id="checkoutError" class="hidden bg-red-50 border border-red-200 text-red-700 rounded-lg p-6 text-center"></section>
        <section id="checkoutSuccess" class="hidden bg-green-50 border border-green-200 text-green-700 rounded-lg p-6 text-center"></section>

        <section id="checkoutContent" class="hidden grid grid-cols-1 xl:grid-cols-3 gap-6">
            <form id="checkoutForm" class="bg-white border border-gray-200 rounded-lg p-6 space-y-6 xl:col-span-2">
                <div>
                    <h2 class="text-xl font-semibold text-gray-900">Thông tin người nhận</h2>
                    <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label for="fullName" class="block text-sm font-medium text-gray-700">Họ và tên</label>
                            <input id="fullName" name="fullName" type="text" required class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none">
                        </div>
                        <div>
                            <label for="phone" class="block text-sm font-medium text-gray-700">Số điện thoại</label>
                            <input id="phone" name="phone" type="tel" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none">
                        </div>
                        <div>
                            <label for="email" class="block text-sm font-medium text-gray-700">Email</label>
                            <input id="email" name="email" type="email" required class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none">
                        </div>
                        <div>
                            <label for="city" class="block text-sm font-medium text-gray-700">Tỉnh/Thành phố</label>
                            <input id="city" name="city" type="text" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none">
                        </div>
                        <div class="md:col-span-2">
                            <label for="address" class="block text-sm font-medium text-gray-700">Địa chỉ</label>
                            <textarea id="address" name="address" rows="2" required class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none"></textarea>
                        </div>
                        <div>
                            <label for="postalCode" class="block text-sm font-medium text-gray-700">Mã bưu chính</label>
                            <input id="postalCode" name="postalCode" type="text" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none">
                        </div>
                        <div>
                            <label for="country" class="block text-sm font-medium text-gray-700">Quốc gia</label>
                            <input id="country" name="country" type="text" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none" value="Việt Nam">
                        </div>
                    </div>
                </div>

                <div>
                    <h2 class="text-xl font-semibold text-gray-900">Ghi chú & thông tin bổ sung</h2>
                    <div class="mt-4 space-y-4">
                        <div>
                            <label for="notes" class="block text-sm font-medium text-gray-700">Ghi chú giao hàng</label>
                            <textarea id="notes" name="notes" rows="3" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none"></textarea>
                        </div>
                        <div>
                            <label for="customerMessage" class="block text-sm font-medium text-gray-700">Lời nhắn tới cửa hàng</label>
                            <textarea id="customerMessage" name="customerMessage" rows="2" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none"></textarea>
                        </div>
                    </div>
                </div>

                <div>
                    <h2 class="text-xl font-semibold text-gray-900">Phương thức thanh toán</h2>
                    <div class="mt-4 grid grid-cols-1 md:grid-cols-2 gap-4">
                        <label class="border rounded-lg p-4 cursor-pointer hover:border-amber-500">
                            <input type="radio" name="paymentMethod" value="cod" class="mr-2" checked>
                            Thanh toán khi nhận hàng (COD)
                        </label>
                        <label class="border rounded-lg p-4 cursor-pointer hover:border-amber-500">
                            <input type="radio" name="paymentMethod" value="transfer" class="mr-2">
                            Chuyển khoản ngân hàng
                        </label>
                        <div class="md:col-span-2">
                            <label for="paymentReference" class="block text-sm font-medium text-gray-700">Mã giao dịch (nếu có)</label>
                            <input id="paymentReference" name="paymentReference" type="text" class="mt-1 w-full rounded-lg border border-gray-300 px-3 py-2 focus:border-amber-500 focus:outline-none">
                            <input type="hidden" name="paymentProvider" value="manual">
                        </div>
                    </div>
                </div>
                <button id="checkoutSubmit" type="submit" class="w-full bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-full px-4 py-3 transition">Đặt hàng</button>
            </form>

            <aside class="bg-white border border-gray-200 rounded-lg p-6">
                <h2 class="text-xl font-semibold text-gray-900">Đơn hàng của bạn</h2>
                <ul id="checkoutSummaryItems" class="mt-4 space-y-3"></ul>
                <dl class="mt-6 space-y-3 text-sm text-gray-600">
                    <div class="flex justify-between">
                        <dt>Tạm tính</dt>
                        <dd id="checkoutSubtotal" class="font-medium text-gray-900">0</dd>
                    </div>
                    <div class="flex justify-between">
                        <dt>Phí vận chuyển</dt>
                        <dd id="checkoutShipping" class="font-medium text-gray-900">0</dd>
                    </div>
                    <div class="flex justify-between text-base border-t border-gray-200 pt-3">
                        <dt class="font-semibold text-gray-900">Tổng cộng</dt>
                        <dd id="checkoutTotal" class="font-semibold text-amber-700">0</dd>
                    </div>
                </dl>
                <p class="mt-4 text-sm text-gray-500"><span id="checkoutItemCount">0</span> sản phẩm · Đơn vị tiền: <span id="checkoutCurrency">VND</span></p>
                <div class="mt-6 text-sm text-gray-500 bg-amber-50 border border-amber-200 rounded-lg p-4">
                    <p>Phí vận chuyển cố định: 15.000đ. Miễn phí đổi trả trong 7 ngày.</p>
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
    <script src="<%=request.getContextPath()%>/assets/js/checkout-page.js"></script>
</body>
</html>
