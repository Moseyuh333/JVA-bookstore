<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>${bookTitle} | Book Details</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="bg-gray-50">

  <nav class="bg-amber-800 text-white shadow-lg">
    <div class="container mx-auto px-4 py-4 flex justify-between items-center">
      <a href="${pageContext.request.contextPath}/index.jsp" class="flex items-center space-x-2">
        <i data-feather="book-open" class="w-6 h-6"></i>
        <span class="title-font text-xl font-bold">Bookish Bliss Haven</span>
      </a>
    </div>
  </nav>

  <div class="container mx-auto py-12 px-4">
    <c:if test="${not empty error}">
      <div class="bg-red-50 border border-red-200 text-red-700 p-4 rounded">${error}</div>
    </c:if>

    <c:if test="${empty error}">
      <div class="bg-white rounded-lg shadow-lg p-8 max-w-4xl mx-auto">
        <div class="flex flex-col md:flex-row gap-8">
          <img
            src="<c:out value='${bookImage != null && bookImage ne "" ? bookImage : "http://static.photos/books/640x480/1"}'/>"
            alt="${bookTitle}"
            class="w-full md:w-1/3 rounded-lg object-cover shadow">

          <div class="flex-1">
            <h2 class="title-font text-3xl font-bold mb-2">${bookTitle}</h2>
            <p class="text-gray-600 mb-2">
              <c:choose>
                <c:when test="${not empty bookAuthor}">by ${bookAuthor}</c:when>
                <c:otherwise><span class="italic text-gray-400">Unknown author</span></c:otherwise>
              </c:choose>
            </p>

            <p class="text-sm text-gray-500 mb-2">
              Category: <span class="font-medium">${bookCategory}</span>
            </p>

            <div class="flex items-center gap-3 mb-4">
              <c:if test="${not empty bookRatingInt}">
                <span class="inline-flex items-center gap-1 bg-amber-100 text-amber-700 px-3 py-1 rounded-full text-sm">
                  ★ ${bookRatingInt}/5
                </span>
              </c:if>
              <span class="text-sm">
                <c:choose>
                  <c:when test="${bookInStock}"><span class="text-green-600 font-medium">${bookStockText}</span></c:when>
                  <c:otherwise><span class="text-red-600 font-medium">Out of stock</span></c:otherwise>
                </c:choose>
              </span>
            </div>

            <p class="text-amber-700 font-bold text-xl mb-4">
              <c:choose>
                <c:when test="${not empty bookPrice}">$${bookPrice}</c:when>
                <c:otherwise>—</c:otherwise>
              </c:choose>
            </p>

            <p class="text-gray-700 leading-relaxed mb-6">
              <c:out value='${bookDescription != null ? bookDescription : "No description available."}'/>
            </p>

            <div class="flex gap-3">
              <a href="#" class="bg-amber-600 hover:bg-amber-700 text-white px-6 py-3 rounded-full font-semibold">Add to Cart</a>
              <c:if test="${not empty bookUrl}">
                <a href="${bookUrl}" target="_blank"
                   class="bg-white border border-gray-300 hover:bg-gray-50 text-gray-800 px-6 py-3 rounded-full font-semibold">
                  View Source
                </a>
              </c:if>
            </div>
          </div>
        </div>
      </div>
    </c:if>
  </div>

  <footer class="bg-gray-900 text-gray-300 text-center py-6">
    <p>&copy; <span id="year"></span> Bookish Bliss Haven · All rights reserved</p>
  </footer>

  <script>
    feather.replace();
    document.getElementById('year').textContent = new Date().getFullYear();
  </script>
</body>
</html>
