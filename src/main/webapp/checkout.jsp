<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Bookish Bliss Haven | Thanh toán" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="bg-gray-50 text-gray-800">
    <div class="max-w-6xl mx-auto px-4 py-10 space-y-8">
        <header class="space-y-2">
            <p class="uppercase text-xs tracking-wide text-amber-500 font-semibold">Thanh toán</p>
            <h1 class="title-font text-3xl font-bold">Hoàn tất đơn hàng của bạn</h1>
            <p class="text-gray-500">Kiểm tra thông tin giao hàng, chọn phương thức thanh toán và xác nhận đơn.</p>
        </header>

        <section id="checkoutFeedback" class="hidden"></section>

        <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
            <div class="lg:col-span-2 space-y-6">
                <article class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
                    <div class="flex items-center justify-between mb-4">
                        <h2 class="title-font text-xl font-semibold">Địa chỉ giao hàng</h2>
                        <a href="<%=request.getContextPath()%>/profile.jsp#addresses" class="text-sm text-amber-600 hover:text-amber-800">Quản lý địa chỉ</a>
                    </div>
                    <p class="text-sm text-gray-500 mb-4">Chọn một địa chỉ giao hàng để tiếp tục.</p>
                    <div id="addressList" class="space-y-4" data-checkout-address-list>
                        <p class="text-sm text-gray-400">Đang tải địa chỉ...</p>
                    </div>
                </article>

                <article class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6 space-y-4">
                    <h2 class="title-font text-xl font-semibold">Phương thức thanh toán</h2>
                    <p class="text-sm text-gray-500">Chúng tôi hỗ trợ thanh toán khi nhận hàng và ví điện tử phổ biến.</p>
                    <div class="grid gap-3 sm:grid-cols-3" data-checkout-payment-list>
                        <label class="border rounded-xl p-4 flex flex-col gap-2 hover:border-amber-500 transition cursor-pointer">
                            <input type="radio" name="paymentMethod" value="cod" class="accent-amber-600" checked>
                            <span class="font-semibold text-gray-800">COD</span>
                            <span class="text-xs text-gray-500">Thanh toán khi nhận hàng</span>
                        </label>
                        <label class="border rounded-xl p-4 flex flex-col gap-2 hover:border-amber-500 transition cursor-pointer">
                            <input type="radio" name="paymentMethod" value="vnpay" class="accent-amber-600">
                            <span class="font-semibold text-gray-800">VNPAY</span>
                            <span class="text-xs text-gray-500">Thanh toán qua VNPAY QR</span>
                        </label>
                        <label class="border rounded-xl p-4 flex flex-col gap-2 hover:border-amber-500 transition cursor-pointer">
                            <input type="radio" name="paymentMethod" value="momo" class="accent-amber-600">
                            <span class="font-semibold text-gray-800">MOMO</span>
                            <span class="text-xs text-gray-500">Ví điện tử MOMO</span>
                        </label>
                    </div>

                    <div id="walletPaymentDetails" class="space-y-3 hidden" data-wallet-details>
                        <div>
                            <label for="walletCardNumber" class="text-sm font-medium text-gray-700">Số thẻ / ví</label>
                            <input type="text" id="walletCardNumber" maxlength="23" inputmode="numeric" autocomplete="cc-number"
                                   class="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                   placeholder="Nhập số thẻ hoặc số ví">
                        </div>
                        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
                            <div>
                                <label for="walletExpiryMonth" class="text-sm font-medium text-gray-700">Tháng hết hạn</label>
                                <input type="text" id="walletExpiryMonth" inputmode="numeric" maxlength="2" autocomplete="cc-exp-month"
                                       class="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                       placeholder="MM">
                            </div>
                            <div>
                                <label for="walletExpiryYear" class="text-sm font-medium text-gray-700">Năm hết hạn</label>
                                <input type="text" id="walletExpiryYear" inputmode="numeric" maxlength="4" autocomplete="cc-exp-year"
                                       class="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                       placeholder="YYYY">
                            </div>
                            <div>
                                <label for="walletCvv" class="text-sm font-medium text-gray-700">Mã CVV/CVC</label>
                                <input type="password" id="walletCvv" inputmode="numeric" maxlength="4" autocomplete="cc-csc"
                                       class="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                       placeholder="CVV" data-sensitive-field>
                            </div>
                        </div>
                        <p class="text-xs text-gray-500">Thông tin thẻ chỉ dùng để xác nhận giao dịch. Chúng tôi chỉ lưu 4 số cuối cho mục đích đối soát.</p>
                    </div>

                    <div class="space-y-2">
                        <label for="checkoutNotes" class="text-sm font-medium text-gray-700">Ghi chú cho cửa hàng</label>
                        <textarea id="checkoutNotes" rows="3" class="w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500" placeholder="Ví dụ: Giao giờ hành chính, gọi trước khi tới..."></textarea>
                    </div>
                </article>
            </div>

            <aside class="bg-white rounded-2xl border border-gray-100 shadow-lg p-6 space-y-6 sticky top-24 h-fit">
                <h2 class="title-font text-xl font-semibold">Tóm tắt đơn hàng</h2>
                <div class="space-y-4">
                    <div class="flex items-center justify-between text-sm text-gray-500">
                        <span>Tổng sản phẩm</span>
                        <span id="checkoutItemsCount">0</span>
                    </div>
                    <div class="border-t border-dashed pt-4 space-y-3" data-checkout-order-items>
                        <p class="text-sm text-gray-400">Đang tải giỏ hàng...</p>
                    </div>
                    <div class="border border-dashed border-amber-200 rounded-2xl p-4 space-y-3">
                        <div class="flex items-center justify-between">
                            <span class="text-sm font-semibold text-gray-700">Mã giảm giá</span>
                        </div>
                        <div class="space-y-3">
                            <div class="flex gap-3">
                                <input type="text" id="checkoutCouponInput" class="flex-1 min-w-0 w-full border rounded-xl px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500" placeholder="Nhập mã giảm giá">
                                <button type="button" id="applyCouponBtn" class="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white text-sm font-semibold rounded-xl transition whitespace-nowrap">
                                    Áp dụng
                                </button>
                            </div>
                            <div class="space-y-1">
                                <span class="text-xs text-gray-500">Hoặc chọn từ mã của bạn:</span>
                                <select id="checkoutCouponSelect" class="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500">
                                    <option value="">Chọn mã giảm giá</option>
                                </select>
                            </div>
                        </div>
                        <p id="couponFeedback" class="text-xs text-gray-500">Nhập hoặc chọn mã giảm giá để tiết kiệm hơn.</p>
                    </div>
                    <div class="border-t border-gray-200 pt-4 space-y-3 text-sm text-gray-600">
                        <div class="flex justify-between">
                            <span>Tạm tính</span>
                            <span id="checkoutSubtotal">0 đ</span>
                        </div>
                        <div class="flex justify-between">
                            <span>Giảm giá</span>
                            <span id="checkoutDiscount">0 đ</span>
                        </div>
                        <div class="flex justify-between">
                            <span>Phí vận chuyển</span>
                            <span id="checkoutShipping">0 đ</span>
                        </div>
                        <div id="checkoutShippingMethod" class="text-xs text-gray-400">�?ang t���nh phA- v��-n chuy���n...</div>
                        <div class="flex justify-between text-base font-semibold text-gray-800">
                            <span>Tổng thanh toán</span>
                            <span id="checkoutTotal">0 đ</span>
                        </div>
                    </div>
                </div>
                <button id="placeOrderBtn" type="button" class="w-full bg-amber-600 hover:bg-amber-700 text-white font-semibold py-3 rounded-full transition">
                    Đặt hàng
                </button>
                <p class="text-xs text-gray-400 text-center">Bằng việc đặt hàng, bạn đồng ý với điều khoản sử dụng và chính sách bảo mật của chúng tôi.</p>
            </aside>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/checkout-page.js"></script>
</body>
</html>
