<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!-- Add required CSS/JS libraries -->
<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>

<footer class="bg-gray-900 text-gray-300 py-12 px-4" style="margin-top: 3rem;">
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
                <h4 class="text-white font-semibold mb-3">Quản trị</h4>
                <ul class="space-y-2">
                    <li><a href="<%=request.getContextPath()%>/admin-dashboard" class="hover:text-white">Dashboard</a></li>
                    <li><a href="<%=request.getContextPath()%>/admin-product" class="hover:text-white">Sản phẩm</a></li>
                    <li><a href="<%=request.getContextPath()%>/admin-account" class="hover:text-white">Tài khoản</a></li>
                    <li><a href="<%=request.getContextPath()%>/admin-shipper" class="hover:text-white">Shipper</a></li>
                </ul>
            </div>
            <div>
                <h4 class="text-white font-semibold mb-3">Liên hệ hỗ trợ</h4>
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

<!-- ====== JS Framework ====== -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://unpkg.com/feather-icons"></script>
<!-- Core JS -->
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

<!-- Feather Icons -->
<script src="https://unpkg.com/feather-icons"></script>

<!-- ====== JS Chung cho Admin ====== -->
<script src="${pageContext.request.contextPath}/assets/js/admin/admin.js"></script>

</body>