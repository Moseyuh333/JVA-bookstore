<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bookish Bliss Haven - Admin Panel</title>
    <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://unpkg.com/feather-icons"></script>
    <script>
        window.appConfig = window.appConfig || {};
        window.appConfig.contextPath = '<%=request.getContextPath()%>';
    </script>
    <style>
        @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap&subset=vietnamese');
        body { font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; }
        .hero-bg { background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://static.photos/books/1200x630/42'); background-size: cover; background-position: center; }
        .book-card:hover { transform: translateY(-5px); box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04); }
        .title-font { font-family: 'Playfair Display', serif; }
        .scrollbar-hide { scrollbar-width: none; -ms-overflow-style: none; }
        .scrollbar-hide::-webkit-scrollbar { display: none; }
        .home-scroll-btn { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(255, 255, 255, 0.92); color: #92400e; padding: 0.6rem; border-radius: 9999px; box-shadow: 0 10px 20px -15px rgba(0, 0, 0, 0.35); transition: background 0.2s ease, color 0.2s ease; z-index: 10; }
        .home-scroll-btn:hover { background: #d97706; color: #fff; }
        nav a, nav a:visited, nav a:hover, nav a:active { text-decoration: none ; color: inherit;}
    </style>
</head>
<body class="bg-gray-50">
    <nav class="bg-amber-800 text-white shadow-lg">
        <div class="container mx-auto px-4 py-4">
            <div class="flex justify-between items-center">
                <a href="<%=request.getContextPath()%>/admin-dashboard" class="flex items-center space-x-2">
                    <i data-feather="book-open" class="w-6 h-6"></i>
                    <div>
                        <span class="title-font text-xl font-bold block">Bookish Bliss Haven</span>
                        <span class="text-sm font-normal block">Admin Panel</span>
                    </div>
                </a>
                <div class="flex items-center space-x-4">
                    <!-- Admin Dropdown -->
                    <div class="relative">
                        <button id="adminDropdownBtn" class="inline-flex items-center px-3 py-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none transition">
                            <i data-feather="user" class="w-5 h-5 mr-1"></i>
                            <span id="accountBtnLabel" class="font-medium">Admin</span>
                        </button>
                        <div id="adminDropdown" class="hidden absolute right-0 mt-2 w-52 bg-white rounded-lg shadow-lg border border-gray-200 z-50"></div>            
                    </div>
                    <button class="md:hidden p-2 rounded-full hover:bg-amber-700" aria-label="Menu">
                        <i data-feather="menu" class="w-5 h-5"></i>
                    </button>
                </div>
            </div>
        </div>
    </nav>
