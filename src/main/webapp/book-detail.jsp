<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>${bookTitle} | Book Details</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-900 text-gray-100">

  <!-- Breadcrumb -->
  <nav class="bg-gray-800 text-sm text-gray-300 py-3 px-6">
    <div class="container mx-auto flex items-center space-x-2">
      <a href="${pageContext.request.contextPath}/index.jsp" class="text-blue-400 hover:underline">Home</a>
      <span>/</span>
      <a href="#" class="text-blue-400 hover:underline">Books</a>
      <span>/</span>
      <a href="#" class="text-blue-400 hover:underline">${bookCategory}</a>
      <span>/</span>
      <span class="text-gray-400">${bookTitle}</span>
    </div>
  </nav>

  <!-- Content -->
  <div class="container mx-auto px-6 py-10">
    <div class="bg-gray-800 rounded-lg shadow-lg p-6 flex flex-col md:flex-row gap-10">

      <!-- Image -->
      <div class="w-full md:w-1/3 flex justify-center">
        <img src="<c:out value='${bookImage}'/>" alt="${bookTitle}" class="rounded-lg shadow-lg object-cover max-h-[500px]">
      </div>

      <!-- Book Info -->
      <div class="flex-1 space-y-4">
        <h1 class="text-3xl font-semibold">${bookTitle}</h1>
        <p class="text-green-400 text-2xl font-bold">£${bookPrice}</p>

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
        <div class="text-yellow-400 text-xl">
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

        <!-- Warning -->
        <div class="bg-amber-900/20 border border-amber-700 text-amber-300 px-4 py-3 rounded">
          <strong>Warning!</strong> This is a demo website for web scraping purposes.
          Prices and ratings here were randomly assigned and have no real meaning.
        </div>

        <!-- Buttons -->
        <div class="flex gap-4 pt-4">
          <button class="bg-blue-700 hover:bg-blue-800 text-white px-6 py-2 rounded">Add to basket</button>
          <a href="${bookUrl}" target="_blank" class="bg-gray-700 hover:bg-gray-600 text-white px-6 py-2 rounded">
            View Source
          </a>
        </div>
      </div>
    </div>

    <!-- Product Description -->
    <div class="mt-10 bg-gray-800 rounded-lg shadow-lg p-6">
      <h2 class="text-xl font-semibold text-amber-400 mb-3">Product Description</h2>
      <p class="text-gray-300 leading-relaxed">
        <c:out value='${bookDescription != null ? bookDescription : "No description available."}'/>
      </p>
    </div>
  </div>

  <footer class="bg-gray-950 text-center text-gray-400 py-6 mt-8">
    &copy; <span id="year"></span> Bookish Bliss Haven · All rights reserved
  </footer>

  <script>
    document.getElementById('year').textContent = new Date().getFullYear();
  </script>
</body>
</html>
