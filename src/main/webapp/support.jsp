<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Trung tâm hỗ trợ | Bookish Bliss Haven" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="bg-gray-50 text-gray-800">
    <section class="bg-amber-700 text-white py-16 px-4">
        <div class="container mx-auto max-w-4xl space-y-4 text-center">
            <h1 class="title-font text-4xl font-bold">Trung tâm hỗ trợ Bookish Bliss Haven</h1>
            <p class="text-amber-100 text-lg">Tìm câu trả lời cho câu hỏi thường gặp hoặc liên hệ trực tiếp với đội ngũ hỗ trợ của chúng tôi.</p>
            <button type="button" data-support-chat-open class="inline-flex items-center gap-2 px-5 py-3 rounded-full bg-white text-amber-700 font-semibold hover:bg-amber-100 transition">
                <i data-feather="message-circle" class="w-5 h-5"></i>
                <span>Mở trò chuyện hỗ trợ</span>
            </button>
        </div>
    </section>

    <section class="py-16 px-4">
        <div class="container mx-auto max-w-5xl grid grid-cols-1 lg:grid-cols-3 gap-8">
            <aside class="lg:col-span-1 space-y-3">
                <div class="bg-white border border-amber-100 rounded-2xl shadow-sm p-5">
                    <h2 class="text-lg font-semibold mb-3">Danh mục hỗ trợ</h2>
                    <nav class="space-y-2 text-sm text-amber-700">
                        <a href="#faq" class="flex items-center gap-2 hover:text-amber-900">
                            <i data-feather="help-circle" class="w-4 h-4"></i> Câu hỏi thường gặp
                        </a>
                        <a href="#shipping" class="flex items-center gap-2 hover:text-amber-900">
                            <i data-feather="truck" class="w-4 h-4"></i> Vận chuyển
                        </a>
                        <a href="#returns" class="flex items-center gap-2 hover:text-amber-900">
                            <i data-feather="corner-up-left" class="w-4 h-4"></i> Đổi trả & hoàn tiền
                        </a>
                        <a href="#privacy" class="flex items-center gap-2 hover:text-amber-900">
                            <i data-feather="shield" class="w-4 h-4"></i> Chính sách bảo mật
                        </a>
                        <a href="#contact" class="flex items-center gap-2 hover:text-amber-900">
                            <i data-feather="mail" class="w-4 h-4"></i> Liên hệ trực tiếp
                        </a>
                    </nav>
                </div>
                <div class="bg-white border border-amber-100 rounded-2xl shadow-sm p-5 space-y-3 text-sm">
                    <h3 class="text-base font-semibold text-gray-800">Giờ hỗ trợ</h3>
                    <p class="text-gray-600">Thứ 2 - Thứ 6: 8h00 - 20h00<br/>Thứ 7 - Chủ nhật: 9h00 - 17h00</p>
                    <p class="text-gray-600">Ngoài khung giờ trên, bạn vẫn có thể để lại tin nhắn trong chat. Chúng tôi sẽ phản hồi ngay khi trực.</p>
                </div>
            </aside>

            <div class="lg:col-span-2 space-y-12">
                <section id="faq" class="bg-white border border-amber-100 rounded-2xl shadow-sm p-6 space-y-4">
                    <h2 class="text-2xl font-semibold text-amber-700">Câu hỏi thường gặp</h2>
                    <div class="space-y-4">
                        <article>
                            <h3 class="font-semibold text-gray-800">Tôi có thể theo dõi đơn hàng như thế nào?</h3>
                            <p class="text-gray-600">Sau khi đăng nhập, truy cập mục <strong>Đơn hàng của tôi</strong> hoặc kiểm tra email xác nhận. Bạn cũng có thể xem trạng thái giao hàng chi tiết tại trang <em>Theo dõi vận chuyển</em>.</p>
                        </article>
                        <article>
                            <h3 class="font-semibold text-gray-800">Làm thế nào để nhận được hóa đơn VAT?</h3>
                            <p class="text-gray-600">Trong quá trình thanh toán, chọn yêu cầu xuất hóa đơn và điền thông tin doanh nghiệp. Đội ngũ của chúng tôi sẽ gửi hóa đơn điện tử qua email trong vòng 48 giờ kể từ khi giao hàng thành công.</p>
                        </article>
                        <article>
                            <h3 class="font-semibold text-gray-800">Bookish Bliss Haven có chương trình khách hàng thân thiết không?</h3>
                            <p class="text-gray-600">Có! Bạn sẽ tự động được tích lũy điểm cho mỗi đơn hàng. Điểm có thể dùng để đổi mã giảm giá và quà tặng. Tham khảo chi tiết trong mục <em>Tài khoản &gt; Ưu đãi của tôi</em>.</p>
                        </article>
                    </div>
                </section>

                <section id="shipping" class="bg-white border border-amber-100 rounded-2xl shadow-sm p-6 space-y-4">
                    <h2 class="text-2xl font-semibold text-amber-700">Vận chuyển</h2>
                    <p class="text-gray-600">Bookish Bliss Haven hợp tác với các đơn vị vận chuyển uy tín (Giao hàng nhanh, J&T, Viettel Post) để giao hàng trên toàn quốc.</p>
                    <ul class="list-disc list-inside text-gray-600 space-y-2">
                        <li>Thời gian giao hàng nội thành: 1-2 ngày làm việc.</li>
                        <li>Thời gian giao hàng tỉnh/thành khác: 2-5 ngày làm việc.</li>
                        <li>Đơn hàng từ 500.000₫ trở lên được miễn phí vận chuyển tiêu chuẩn.</li>
                        <li>Bạn có thể chọn giao nhanh trong 24h tại một số khu vực trung tâm.</li>
                    </ul>
                </section>

                <section id="returns" class="bg-white border border-amber-100 rounded-2xl shadow-sm p-6 space-y-4">
                    <h2 class="text-2xl font-semibold text-amber-700">Đổi trả & hoàn tiền</h2>
                    <p class="text-gray-600">Chúng tôi chấp nhận đổi trả trong vòng 7 ngày kể từ khi nhận sách đối với các trường hợp:</p>
                    <ul class="list-disc list-inside text-gray-600 space-y-2">
                        <li>Sách bị lỗi in ấn, bong gáy hoặc hư hỏng do vận chuyển.</li>
                        <li>Giao sai tựa sách hoặc sai số lượng.</li>
                    </ul>
                    <p class="text-gray-600">Vui lòng giữ hóa đơn/biên nhận và chụp ảnh sản phẩm lỗi để gửi qua chat hỗ trợ hoặc email <a href="mailto:support@bookishhaven.com" class="text-amber-600 hover:underline">support@bookishhaven.com</a>.</p>
                </section>

                <section id="privacy" class="bg-white border border-amber-100 rounded-2xl shadow-sm p-6 space-y-4">
                    <h2 class="text-2xl font-semibold text-amber-700">Chính sách bảo mật</h2>
                    <p class="text-gray-600">Bookish Bliss Haven cam kết bảo vệ thông tin cá nhân của khách hàng. Chúng tôi chỉ sử dụng dữ liệu để xử lý đơn hàng, nâng cao trải nghiệm mua sắm và cung cấp ưu đãi phù hợp.</p>
                    <p class="text-gray-600">Dữ liệu thanh toán được mã hóa và xử lý bởi đối tác cổng thanh toán đạt chuẩn PCI-DSS. Bạn có thể yêu cầu cập nhật hoặc xóa thông tin cá nhân bằng cách gửi yêu cầu qua chat hoặc email.</p>
                </section>

                <section id="contact" class="bg-white border border-amber-100 rounded-2xl shadow-sm p-6 space-y-4">
                    <h2 class="text-2xl font-semibold text-amber-700">Liên hệ trực tiếp</h2>
                    <p class="text-gray-600">Nếu cần hỗ trợ nhanh, hãy chọn một trong các kênh dưới đây:</p>
                    <ul class="text-gray-600 space-y-2">
                        <li>Chat trực tuyến: <button type="button" data-support-chat-open class="text-amber-700 hover:underline font-medium">Bắt đầu trò chuyện</button></li>
                        <li>Điện thoại: <a href="tel:+84901234567" class="text-amber-700 hover:underline">0901 234 567</a> (8h - 20h)</li>
                        <li>Email: <a href="mailto:support@bookishhaven.com" class="text-amber-700 hover:underline">support@bookishhaven.com</a></li>
                    </ul>
                </section>
            </div>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
</body>
</html>
