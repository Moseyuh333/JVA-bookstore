<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<style>
    @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap&subset=vietnamese');
    .title-font { font-family: 'Playfair Display', serif; }
    nav a, nav a:visited, nav a:hover, nav a:active { text-decoration: none ; color: inherit;}
</style>
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
