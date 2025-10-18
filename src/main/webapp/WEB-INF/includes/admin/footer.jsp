<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!-- Add required CSS/JS libraries -->
<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>

<footer class="bg-gray-800 text-gray-300 py-6 px-4 mt-12" style="margin-left: 0;">
    <div class="container mx-auto">
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
            <!-- Left: Brand & Copyright -->
            <div class="flex items-center gap-2 bg-gray-800 text-amber-200 px-4 py-2 rounded-full text-sm shadow-sm">
                <i data-feather="shield" class="w-4 h-4"></i>
                <span>&copy; <span id="year"></span> Bookish Bliss Haven · Mọi quyền được bảo lưu</span>
            </div>

            <!-- Center: Navigation Links -->
            <div class="flex gap-8 text-sm">
                <a href="<%=request.getContextPath()%>/admin/dashboard" class="hover:text-white transition">Dashboard</a>
                <a href="<%=request.getContextPath()%>/admin/products" class="hover:text-white transition">Sản phẩm</a>
                <a href="<%=request.getContextPath()%>/admin/accounts" class="hover:text-white transition">Tài khoản</a>
                <a href="<%=request.getContextPath()%>/admin/shipping" class="hover:text-white transition">Vận chuyển</a>
            </div>

            <!-- Right: Contact Info -->
            <div class="flex flex-col md:flex-row md:items-center gap-4 text-sm">
                <div class="flex items-center gap-2">
                    <i data-feather="map-pin" class="w-4 h-4"></i>
                    <span>123 Đường Văn Học, Quận Sách, TP.HCM</span>
                </div>
                <span class="hidden md:inline text-gray-500">|</span>
                <div class="flex items-center gap-2">
                    <i data-feather="mail" class="w-4 h-4"></i>
                    <a href="mailto:info@bookishhaven.com" class="hover:text-white transition">info@bookishhaven.com</a>
                </div>
                <span class="hidden md:inline text-gray-500">|</span>
                <div class="flex items-center gap-2">
                    <i data-feather="phone" class="w-4 h-4"></i>
                    <a href="tel:+84901234567" class="hover:text-white transition">0901 234 567</a>
                </div>
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
<script src="${pageContext.request.contextPath}/assets/js/app-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/admin/admin.js"></script>

</body>