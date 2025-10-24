<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
    String contextPath = request.getContextPath();
%>

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><c:out value="${empty pageTitle ? 'Bookish Bliss Haven' : pageTitle}" /></title>
        <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
        <script src="https://cdn.tailwindcss.com"></script>
        <script src="https://unpkg.com/feather-icons"></script>
        <style>
            @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap&subset=vietnamese');

            body {
                font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif;
            }

            .hero-bg {
                background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://static.photos/books/1200x630/42');
                background-size: cover;
                background-position: center;
            }

            .book-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
            }

            .catalog-hero {
                background: linear-gradient(135deg, rgba(120, 53, 15, 0.92), rgba(146, 64, 14, 0.85)), url('http://static.photos/books/1200x630/41');
                background-size: cover;
                background-position: center;
            }

            .catalog-card {
                transition: transform 0.25s ease, box-shadow 0.25s ease;
            }

            .catalog-card:hover {
                transform: translateY(-6px);
                box-shadow: 0 25px 35px -20px rgba(120, 53, 15, 0.5);
            }

            .highlight-card {
                border-color: #d97706;
                box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.4);
            }

            .title-font {
                font-family: 'Playfair Display', serif;
            }
        </style>
    </head>
    <!-- Navigation -->
    <body class="bg-gray-50 text-gray-800">
    <nav class="bg-amber-800 text-white shadow-lg">
        <div class="container mx-auto px-4 py-4">
            <div class="flex items-center justify-between">
                <a href="<%= contextPath %>/index.jsp" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <span class="title-font text-xl font-bold">Bookish Bliss Haven</span>
                </a>
                <div class="hidden md:flex items-center space-x-6">
                    <a href="<%= contextPath %>/index.jsp" class="hover:text-amber-200 font-medium transition">Trang chủ</a>
                    <a href="<%= contextPath %>/catalog.jsp" class="hover:text-amber-200 font-medium transition">Danh mục</a>
                    <a href="<%= contextPath %>/catalog.jsp?sort=best" class="hover:text-amber-200 font-medium transition">Bán chạy</a>
                    <a href="<%= contextPath %>/catalog.jsp?sort=rated" class="hover:text-amber-200 font-medium transition">Đánh giá cao</a>

                    <c:if test="${sessionScope.role == 'seller'}">
                        <a href="<%= contextPath %>/seller/dashboard" 
                        class="hover:text-amber-200 font-medium transition">
                        Trang bán hàng
                        </a>
                    </c:if>
                    


                </div>
                <div class="flex items-center space-x-3">
                    <button type="button"
                            data-open-search
                            class="hidden sm:inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-200">
                        <i data-feather="search" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Tìm kiếm</span>
                    </button>
                          <a href="<%= contextPath %>/catalog.jsp#cart"
                              data-cart-open="true"
                              role="button"
                              class="relative inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-200">
                        <i data-feather="shopping-cart" class="w-5 h-5 mr-1"></i>
                        <span class="font-medium">Giỏ hàng</span>
                        <span id="cartCountBadge"
                              class="ml-2 inline-flex items-center justify-center text-xs font-semibold bg-white text-amber-800 rounded-full px-2 py-0.5 hidden">0</span>
                    </a>
                    <div class="relative">
                        <button id="userDropdownBtn" type="button"
                                class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-200">
                            <i data-feather="user" class="w-5 h-5 mr-1"></i>
                            <span id="accountBtnLabel" class="font-medium">Tài khoản</span>
                            <i data-feather="chevron-down" class="w-4 h-4 ml-1"></i>
                        </button>
                        <div id="userDropdown"
                             class="hidden absolute right-0 mt-2 w-56 bg-white text-gray-800 rounded-lg shadow-lg border border-gray-200 z-50"></div>
                    </div>
                    <button id="mobileMenuButton" type="button"
                            class="md:hidden inline-flex items-center p-2 rounded-full hover:bg-amber-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-200"
                            aria-expanded="false" aria-controls="mobileMenu">
                        <span class="sr-only">Mở menu điều hướng</span>
                        <i data-feather="menu" class="w-5 h-5"></i>
                    </button>
                </div>
            </div>
        </div>
    </nav>
    <div id="mobileMenu" class="md:hidden hidden bg-amber-900/95 text-white border-t border-amber-700">
        <div class="px-4 py-4 space-y-3 text-sm">
            <button type="button" data-open-search class="w-full text-left block font-medium hover:text-amber-200 transition">Tìm kiếm sách</button>
            <a href="<%= contextPath %>/index.jsp" class="block font-medium hover:text-amber-200 transition">Trang chủ</a>
            <a href="<%= contextPath %>/catalog.jsp" class="block font-medium hover:text-amber-200 transition">Danh mục sách</a>
            <a href="<%= contextPath %>/catalog.jsp?sort=best" class="block font-medium hover:text-amber-200 transition">Bán chạy</a>
            <a href="<%= contextPath %>/catalog.jsp?sort=rated" class="block font-medium hover:text-amber-200 transition">Đánh giá cao</a>
            
            <c:if test="${sessionScope.role == 'seller'}">
                <a href="<%= contextPath %>/seller/dashboard" 
                class="block font-medium hover:text-amber-200 transition">
                Trang bán hàng
                </a>
            </c:if>

            




            

            <button type="button" data-cart-open="true"
                    class="w-full text-left block font-medium hover:text-amber-200 transition">Giỏ hàng</button>
            <div class="border-t border-amber-700/60 pt-3 text-amber-200/90">
                <p class="text-xs leading-relaxed">Đăng nhập để quản lý đơn hàng, đánh giá sách và đồng bộ giỏ hàng.</p>
            </div>
        </div>
    </div>

    <div id="globalSearchOverlay" class="fixed inset-0 z-[80] hidden">
        <div class="absolute inset-0 bg-black/60" data-search-dismiss></div>
        <div class="relative mx-auto w-full max-w-3xl px-4 mt-20">
            <div class="bg-white rounded-2xl shadow-2xl overflow-hidden border border-amber-100">
                <div class="px-6 py-4 flex items-center justify-between border-b border-gray-100">
                    <h2 class="text-lg font-semibold text-gray-800">Tìm kiếm sách</h2>
                    <button type="button" data-search-dismiss class="inline-flex items-center justify-center w-9 h-9 rounded-full border border-gray-200 text-gray-500 hover:text-gray-800 hover:border-gray-400 transition">
                        <i data-feather="x" class="w-4 h-4"></i>
                        <span class="sr-only">Đóng tìm kiếm</span>
                    </button>
                </div>
                <form id="globalSearchForm" class="px-6 py-4 border-b border-gray-100" novalidate>
                    <label class="flex items-center gap-3 bg-gray-50 border border-gray-200 rounded-full px-4 py-2 focus-within:ring-2 focus-within:ring-amber-500 focus-within:border-amber-500">
                        <i data-feather="search" class="w-5 h-5 text-amber-600"></i>
                        <input id="globalSearchInput" name="q" type="search" autocomplete="off" minlength="2"
                               placeholder="Nhập tên sách, tác giả hoặc ISBN..." class="flex-1 bg-transparent focus:outline-none text-gray-800 placeholder-gray-400">
                    </label>
                </form>
                <div id="globalSearchMessage" class="px-6 py-3 text-sm text-gray-500">
                    Nhập tối thiểu 2 ký tự để tìm kiếm sách.
                </div>
                <div id="globalSearchResults" class="max-h-80 overflow-y-auto divide-y divide-gray-100 hidden" aria-live="polite"></div>
            </div>
        </div>
    </div>
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            var menuBtn = document.getElementById('mobileMenuButton');
            var mobileMenu = document.getElementById('mobileMenu');
            if (!menuBtn || !mobileMenu) {
                return;
            }
            menuBtn.addEventListener('click', function () {
                var expanded = menuBtn.getAttribute('aria-expanded') === 'true';
                menuBtn.setAttribute('aria-expanded', expanded ? 'false' : 'true');
                mobileMenu.classList.toggle('hidden');
            });
        });
    </script>