<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

    <!-- Add required CSS/JS libraries -->
    <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>

    <footer class="bg-gray-900 text-gray-300 py-6 px-8 mt-16">
        <div
            class="container mx-auto flex flex-col lg:flex-row items-center justify-between gap-4 text-sm text-gray-300">
            <!-- Left -->
            <div
                class="flex items-center gap-2 bg-gray-800 text-amber-300 px-4 py-2 rounded-full shadow-md whitespace-nowrap">
                <i data-feather="shield" class="w-4 h-4"></i>
                <span>&copy; <span id="year"></span> Bookish Bliss Haven · Mọi quyền được bảo lưu</span>
            </div>

            <!-- Center -->
            <div class="flex flex-wrap justify-center lg:justify-center gap-x-8 gap-y-2 text-center whitespace-nowrap">
                <a href="<%=request.getContextPath()%>/admin-dashboard" class="hover:text-white">Dashboard</a>
                <a href="<%=request.getContextPath()%>/admin-product" class="hover:text-white">Sản phẩm</a>
                <a href="<%=request.getContextPath()%>/admin-account" class="hover:text-white">Tài khoản</a>
                <a href="<%=request.getContextPath()%>/admin-shippers" class="hover:text-white">Vận chuyển</a>
                <a href="<%=request.getContextPath()%>/admin-support" class="hover:text-white">Hỗ trợ</a>
            </div>

            <!-- Right -->
            <div class="flex flex-wrap items-center justify-center gap-x-4 gap-y-2 text-gray-400 text-center">
                <div class="flex items-center gap-2 whitespace-nowrap">
                    <i data-feather="map-pin" class="w-4 h-4 text-white"></i>
                    <span>123 Đường Văn Học, Quận Sách, TP.HCM</span>
                </div>
                <div class="hidden lg:block w-px h-4 bg-gray-600"></div>
                <div class="flex items-center gap-2 whitespace-nowrap">
                    <i data-feather="mail" class="w-4 h-4 text-white"></i>
                    <a href="mailto:info@bookishhaven.com" class="hover:text-white">info@bookishhaven.com</a>
                </div>
                <div class="hidden lg:block w-px h-4 bg-gray-600"></div>
                <div class="flex items-center gap-2 whitespace-nowrap">
                    <i data-feather="phone" class="w-4 h-4 text-white"></i>
                    <a href="tel:+84901234567" class="hover:text-white">0901 234 567</a>
                </div>
            </div>
        </div>
    </footer>

    <!-- ====== JS Framework ====== -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="https://unpkg.com/feather-icons"></script>

    <!-- Chart.js -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <!-- ====== JS Chung cho Admin ====== -->
    <script src="<%= request.getContextPath() %>/assets/js/admin/admin.js"></script>
