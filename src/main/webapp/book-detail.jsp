<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>${bookTitle} | Bookish Bliss Haven</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-[#1a1a1a] text-gray-100 font-sans">

  <!-- Navbar -->
  <nav class="bg-[#5c2e05] text-white shadow-md">
    <div class="container mx-auto px-6 py-4 flex justify-between items-center">
      <a href="${pageContext.request.contextPath}/index.jsp" class="flex items-center space-x-2">
        <i data-feather="book-open" class="w-6 h-6"></i>
        <span class="font-bold text-xl tracking-wide">Bookish Bliss Haven</span>
      </a>
      <div class="flex space-x-6 text-sm">
        <a href="${pageContext.request.contextPath}/books" class="hover:text-amber-300">Shop</a>
        <a href="#" class="hover:text-amber-300">Collections</a>
        <a href="#" class="hover:text-amber-300">About</a>
      </div>
    </div>
  </nav>

  <!-- Breadcrumb -->
  <div class="bg-[#2a2a2a] text-sm text-gray-400 py-3 px-6">
    <div class="container mx-auto">
      <a href="${pageContext.request.contextPath}/index.jsp" class="text-amber-400 hover:underline">Home</a> /
      <a href="${pageContext.request.contextPath}/books" class="text-amber-400 hover:underline">Books</a> /
      <span class="text-gray-300">${bookTitle}</span>
    </div>
  </div>

  <!-- Main Content -->
  <div class="container mx-auto px-6 py-10">
    <div class="grid md:grid-cols-2 gap-10 items-start bg-[#222] rounded-xl p-8 shadow-lg border border-[#3a2a1d]">

      <!-- Book Image -->
      <div class="flex justify-center">
        <img src="<c:out value='${bookImage}'/>"
             alt="${bookTitle}"
             class="rounded-lg shadow-lg object-cover max-h-[480px] border border-[#4b3421]">
      </div>

      <!-- Info Section -->
      <div class="space-y-4">
        <h1 class="text-3xl font-bold text-white">${bookTitle}</h1>
        <p class="text-gray-400 italic">
          <c:choose>
            <c:when test="${not empty bookAuthor}">by ${bookAuthor}</c:when>
            <c:otherwise>Unknown author</c:otherwise>
          </c:choose>
        </p>
        <p class="text-amber-400 text-lg">Category: <span class="text-gray-200">${bookCategory}</span></p>

        <p class="text-3xl font-semibold text-amber-400">£${bookPrice}</p>

        <p class="text-sm">
          <c:choose>
            <c:when test="${bookInStock}">
              <span class="text-green-400">✔ In stock (${bookStockText})</span>
            </c:when>
            <c:otherwise>
              <span class="text-red-500">✖ Out of stock</span>
            </c:otherwise>
          </c:choose>
        </p>

        <!-- Rating -->
        <div class="text-yellow-400 text-xl flex gap-1">
          <%
            Object ratingObj = request.getAttribute("bookRatingInt");
            double rating = 0.0;
            if (ratingObj != null) {
              try { rating = Double.parseDouble(ratingObj.toString()); } catch (Exception ignored) {}
            }
            int fullStars = (int) rating;
            boolean halfStar = (rating - fullStars) >= 0.5;
          %>
          <% for (int i = 0; i < fullStars; i++) { %> ★ <% } %>
          <% if (halfStar) { %> ☆ <% } %>
          <% for (int i = fullStars + (halfStar ? 1 : 0); i < 5; i++) { %> ☆ <% } %>
        </div>

        <!-- Demo Warning -->
        <div class="bg-[#4b2c0a] border border-[#7b5322] text-amber-200 px-4 py-3 rounded-md">
          <strong>⚠ Demo Notice:</strong> Prices and ratings shown are for demonstration purposes only.
        </div>

        <div class="flex gap-4 pt-6">
          <a href="#" class="bg-[#b87333] hover:bg-[#d18f4f] text-white px-6 py-3 rounded-full font-semibold transition">Add to Cart</a>
          <c:if test="${not empty bookUrl}">
            <a href="${bookUrl}" target="_blank" class="bg-transparent border border-[#b87333] text-[#b87333] hover:bg-[#b87333] hover:text-white px-6 py-3 rounded-full font-semibold transition">View Source</a>
          </c:if>
        </div>
      </div>
    </div>

    <!-- Description -->
    <div class="mt-10 bg-[#222] rounded-xl p-8 border border-[#3a2a1d] shadow">
      <h2 class="text-xl font-semibold text-amber-400 mb-4">Product Description</h2>
      <p class="text-gray-300 leading-relaxed">
        <c:out value='${bookDescription != null ? bookDescription : "No description available."}'/>
      </p>
    </div>
  </div>

  <!-- Footer -->
  <footer class="bg-[#111] text-gray-400 text-center py-6 border-t border-[#3a2a1d]">
    &copy; <span id="year"></span> Bookish Bliss Haven · All rights reserved
  </footer>

  <script>
    document.getElementById('year').textContent = new Date().getFullYear();
  </script>
</body>
</html>
