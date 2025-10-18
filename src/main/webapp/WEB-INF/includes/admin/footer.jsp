<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!-- Add required CSS/JS libraries -->
<link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
<script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>

<footer class="bg-gray-900 text-gray-300 py-8 px-6 mt-16">
    <div class="container mx-auto flex flex-col md:flex-row md:items-center md:justify-between gap-6">
        
        <!-- Left: Copyright -->
        <div class="flex items-center gap-2 bg-gray-800 text-amber-300 px-4 py-2 rounded-full text-sm shadow-md">
            <i data-feather="shield" class="w-4 h-4"></i>
            <span>&copy; <span id="year"></span> Bookish Bliss Haven · Mọi quyền được bảo lưu</span>
        </div>

        <!-- Center: Nav Links -->
        <div class="flex flex-wrap justify-center gap-6 text-sm text-gray-300">
            <a href="<%=request.getContextPath()%>/admin/dashboard" class="hover:text-amber-400 transition">Dashboard</a>
            <a href="<%=request.getContextPath()%>/admin/products" class="hover:text-amber-400 transition">Sản phẩm</a>
            <a href="<%=request.getContextPath()%>/admin/accounts" class="hover:text-amber-400 transition">Tài khoản</a>
            <a href="<%=request.getContextPath()%>/admin/shipping" class="hover:text-amber-400 transition">Vận chuyển</a>
        </div>

        <!-- Right: Contact Info -->
        <div class="flex flex-col md:flex-row md:items-center md:gap-6 text-sm text-gray-400">
            <div class="flex items-center gap-2">
                <i data-feather="map-pin" class="w-4 h-4 mr-2 mt-0.5"></i>
                <span>123 Đường Văn Học, Quận Sách, TP.HCM</span>
            </div>
            <div class="hidden md:block w-px h-4 bg-gray-600"></div>
            <div class="flex items-center gap-2">
                <i data-feather="mail" class="w-4 h-4 mr-2"></i>
                <a href="mailto:info@bookishhaven.com" class="hover:text-white">info@bookishhaven.com</a>
            </div>
            <div class="hidden md:block w-px h-4 bg-gray-600"></div>
            <div class="flex items-center gap-2">
                <i data-feather="phone" class="w-4 h-4 mr-2"></i>
                <a href="tel:+84901234567" class="hover:text-white">0901 234 567</a>
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