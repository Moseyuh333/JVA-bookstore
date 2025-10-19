<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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

    <div id="cartDrawerOverlay" class="fixed inset-0 bg-black/50 hidden z-[60]"></div>
    <aside id="cartDrawer" class="fixed inset-y-0 right-0 w-full max-w-md bg-white shadow-2xl hidden z-[70] flex flex-col">
        <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
            <div>
                <h2 class="text-lg font-semibold text-gray-800">Giỏ hàng của bạn</h2>
                <p class="text-sm text-gray-500">Kiểm tra sản phẩm trước khi thanh toán</p>
            </div>
            <button type="button" data-cart-close class="inline-flex items-center justify-center w-9 h-9 rounded-full border border-gray-200 text-gray-500 hover:text-gray-800 hover:border-gray-400 transition">
                <i data-feather="x" class="w-4 h-4"></i>
                <span class="sr-only">Đóng giỏ hàng</span>
            </button>
        </div>
        <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-cart-scroll>
            <div data-cart-loading class="text-center text-sm text-gray-500 py-10">Đang tải giỏ hàng...</div>
            <div data-cart-empty class="hidden text-center py-12 text-gray-500">
                <i data-feather="shopping-bag" class="mx-auto mb-3 w-8 h-8 text-amber-600"></i>
                <p class="font-medium">Giỏ hàng của bạn đang trống.</p>
                <p class="text-sm text-gray-400">Hãy thêm một vài cuốn sách để tiếp tục.</p>
            </div>
            <div data-cart-items class="hidden space-y-4"></div>
        </div>
        <div class="px-6 py-4 border-t border-gray-200 space-y-3">
            <div class="flex items-center justify-between text-sm text-gray-600">
                <span>Tổng sản phẩm</span>
                <span data-cart-count>0</span>
            </div>
            <div class="flex items-center justify-between text-lg font-semibold text-amber-700">
                <span>Tạm tính</span>
                <span data-cart-subtotal>0&nbsp;₫</span>
            </div>
            <div data-cart-feedback class="hidden text-sm"></div>
            <div class="flex items-center justify-between gap-3">
                <button type="button" data-cart-clear class="flex-1 px-4 py-3 rounded-full border border-gray-300 text-sm font-medium text-gray-600 hover:bg-gray-100 transition">Xóa giỏ</button>
                <button type="button" data-cart-checkout class="flex-1 px-4 py-3 rounded-full bg-amber-600 text-white font-semibold hover:bg-amber-700 transition disabled:opacity-50 disabled:cursor-not-allowed">Thanh toán</button>
            </div>
        </div>
    </aside>

    <script>
        window.appConfig = window.appConfig || {};
        window.appConfig.contextPath = '<%=request.getContextPath()%>';
    </script>
    <script src="<%=request.getContextPath()%>/assets/js/app-shell.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/api-client.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/cart-client.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/cart-ui.js"></script>