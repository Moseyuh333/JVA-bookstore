<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bookish Bliss Haven - Admin Panel</title>
    <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');

        body {
            font-family: 'Roboto', sans-serif;
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

        .title-font {
            font-family: 'Playfair Display', serif;
        }

        nav a {
            color: inherit;
            text-decoration: none;
        }

        nav a:hover, nav a:focus {
            color: inherit;
            text-decoration: none;
        }

        .admin-dropdown {
            position: relative;
        }

        .admin-dropdown-menu {
            min-width: 220px;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
        }

        .dropdown-item {
            transition: all 0.2s ease;
        }

        .dropdown-divider {
            height: 1px;
            background: #e5e7eb;
            margin: 4px 0;
        }
    </style>
</head>
<body class="bg-gray-100 text-gray-800">
    <!-- Navigation -->
    <nav class="bg-amber-800 text-white shadow-lg relative z-50">
        <div class="container mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <span class="title-font text-xl font-bold">Bookish Bliss Haven 
                        <br>
                        <span class="text-sm font-normal">Admin Panel</span>
                    </span>
                </a>
                <div class="flex items-center space-x-4">
                    <!-- Admin Dropdown -->
                    <div class="admin-dropdown">
                        <button id="adminDropdownBtn"
                            class="flex items-center space-x-2 p-2 rounded-lg hover:bg-amber-700 focus:bg-amber-700 focus:outline-none transition">
                            <i data-feather="user" class="w-5 h-5"></i>
                            <span class="text-sm font-medium">Admin</span>
                            <i data-feather="chevron-down" class="w-4 h-4"></i>
                        </button>
                        <div id="adminDropdown"
                            class="hidden absolute right-0 mt-2 admin-dropdown-menu bg-white rounded-lg border border-gray-200 z-50">
                            <div class="py-2">
                                <!-- Admin Info -->
                                <div class="px-4 py-3 border-b border-gray-200">
                                    <p class="text-sm font-semibold text-gray-900">Tài khoản Admin</p>
                                    <p class="text-xs text-gray-500 mt-1">admin@bookishhaven.com</p>
                                </div>

                                <!-- Menu Items -->
                                <a href="<%=request.getContextPath()%>/admin/profile.jsp"
                                    class="dropdown-item flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i class="fas fa-user-circle w-4 h-4 mr-3"></i>
                                    <span class="text-sm">Thông tin cá nhân</span>
                                </a>
                                <a href="<%=request.getContextPath()%>/admin/settings.jsp"
                                    class="dropdown-item flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i class="fas fa-cog w-4 h-4 mr-3"></i>
                                    <span class="text-sm">Cài đặt hệ thống</span>
                                </a>
                                <a href="<%=request.getContextPath()%>/admin/activity-log.jsp"
                                    class="dropdown-item flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i class="fas fa-history w-4 h-4 mr-3"></i>
                                    <span class="text-sm">Lịch sử hoạt động</span>
                                </a>

                                <div class="dropdown-divider mx-2"></div>

                                <a href="<%=request.getContextPath()%>/admin/change-password.jsp"
                                    class="dropdown-item flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                                    <i class="fas fa-key w-4 h-4 mr-3"></i>
                                    <span class="text-sm">Đổi mật khẩu</span>
                                </a>

                                <div class="dropdown-divider mx-2"></div>

                                <a href="<%=request.getContextPath()%>/logout"
                                    class="dropdown-item flex items-center px-4 py-2 text-red-600 hover:bg-red-50">
                                    <i class="fas fa-sign-out-alt w-4 h-4 mr-3"></i>
                                    <span class="text-sm font-medium">Đăng xuất</span>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </nav>

    <script>
        // Initialize Feather Icons
        feather.replace();

        // Admin Dropdown Toggle
        const adminDropdownBtn = document.getElementById('adminDropdownBtn');
        const adminDropdown = document.getElementById('adminDropdown');

        adminDropdownBtn.addEventListener('click', function(e) {
            e.stopPropagation();
            adminDropdown.classList.toggle('hidden');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function(e) {
            if (!adminDropdownBtn.contains(e.target) && !adminDropdown.contains(e.target)) {
                adminDropdown.classList.add('hidden');
            }
        });

        // Close dropdown when pressing Escape
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                adminDropdown.classList.add('hidden');
            }
        });
    </script>
</body>
</html>