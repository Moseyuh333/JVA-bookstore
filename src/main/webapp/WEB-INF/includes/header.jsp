<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<nav class="bg-amber-800 text-white shadow-lg">
  <div class="container mx-auto px-4 py-4">
    <div class="flex justify-between items-center">
      <a href="<%=request.getContextPath()%>/index.jsp" class="flex items-center space-x-2">
        <i data-feather="book-open" class="w-6 h-6"></i>
        <span class="title-font text-xl font-bold">Bookish Bliss Haven</span>
      </a>
      <div class="hidden md:flex space-x-8">
        <a href="<%=request.getContextPath()%>/index.jsp" class="hover:text-amber-200 font-medium">Home</a>
        <a href="<%=request.getContextPath()%>/shop.jsp" class="hover:text-amber-200 font-medium">Shop</a>
        <a href="<%=request.getContextPath()%>/collections.jsp" class="hover:text-amber-200 font-medium">Collections</a>
        <a href="<%=request.getContextPath()%>/about.jsp" class="hover:text-amber-200 font-medium">About</a>
      </div>
      <div class="flex items-center space-x-4">
        <div class="relative">
          <button id="userDropdownBtn" class="p-2 rounded-full hover:bg-amber-700 focus:bg-amber-700 focus:outline-none">
            <i data-feather="user" class="w-5 h-5"></i>
          </button>
          <div id="userDropdown"
               class="hidden absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 z-50">
            <div class="py-2">
              <a href="<%=request.getContextPath()%>/login.jsp"
                 class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                <i data-feather="log-in" class="w-4 h-4 mr-2"></i> Đăng nhập
              </a>
              <a href="<%=request.getContextPath()%>/register.jsp"
                 class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                <i data-feather="user-plus" class="w-4 h-4 mr-2"></i> Đăng ký
              </a>
              <hr class="my-1">
              <a href="<%=request.getContextPath()%>/forgot-password.jsp"
                 class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                <i data-feather="key" class="w-4 h-4 mr-2"></i> Quên mật khẩu
              </a>
            </div>
          </div>
        </div>
        <button class="p-2 rounded-full hover:bg-amber-700"><i data-feather="search" class="w-5 h-5"></i></button>
        <button class="p-2 rounded-full hover:bg-amber-700"><i data-feather="shopping-cart" class="w-5 h-5"></i></button>
        <button class="md:hidden p-2 rounded-full hover:bg-amber-700"><i data-feather="menu" class="w-5 h-5"></i></button>
      </div>
    </div>
  </div>
</nav>
