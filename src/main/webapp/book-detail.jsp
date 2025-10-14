<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
      <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

        <!DOCTYPE html>
        <html lang="en">

        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>${bookTitle} | Book Details</title>
          <link rel="icon" type="image/x-icon" href="/static/favicon.ico">
          <script src="https://cdn.tailwindcss.com"></script>
          <script src="https://unpkg.com/feather-icons"></script>
          <script src="https://cdn.jsdelivr.net/npm/feather-icons/dist/feather.min.js"></script>
          <style>
            @import url('https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500&display=swap');

            body {
              font-family: 'Roboto', sans-serif;
            }

            .hero-bg {
              background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('http://static.photos/books/1200x630/42');
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
          </style>
        </head>

        <body class="bg-[#111] text-gray-100 font-sans">

          <%@ include file="/WEB-INF/includes/header.jsp" %>

            <!-- Main content -->
            <div class="container mx-auto py-12 px-6">
              <c:if test="${not empty error}">
                <div class="bg-red-800/60 text-red-200 px-4 py-3 rounded">${error}</div>
              </c:if>

              <c:if test="${empty error}">
                <!-- Book header section -->
                <div class="flex flex-col md:flex-row bg-[#1b1b1b] rounded-lg shadow-lg overflow-hidden">
                  <!-- Book cover -->
                  <div class="md:w-1/3 flex justify-center items-center bg-[#2b2b2b] p-6">
                    <img src="<c:out value='${bookImage != null && bookImage ne "" ? bookImage : "
                      https://placehold.co/400x550"}' />"
                    alt="${bookTitle}"
                    class="rounded-lg shadow-md object-contain w-full max-w-[320px] max-h-[480px] mx-auto">
                  </div>

                  <!-- Book info -->
                  <div class="md:w-2/3 p-8 flex flex-col justify-center">
                    <h1 class="text-3xl font-bold text-amber-400 mb-3">${bookTitle}</h1>

                    <p class="text-sm text-gray-400 mb-2">
                      by
                      <c:choose>
                        <c:when test="${not empty bookAuthor}">${bookAuthor}</c:when>
                        <c:otherwise><span class="italic text-gray-500">Unknown author</span></c:otherwise>
                      </c:choose>
                    </p>

                    <p class="text-gray-300 mb-3">
                      <span class="font-semibold text-amber-500">Category:</span> ${bookCategory}
                    </p>

                    <!-- Stock status -->
                    <div class="flex items-center gap-2 mb-3">
                      <c:choose>
                        <c:when test="${bookInStock}">
                          <span class="text-green-400 font-medium flex items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-green-400" fill="none"
                              viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                              <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
                            </svg>
                            In stock (${bookStockText})
                          </span>
                        </c:when>
                        <c:otherwise>
                          <span class="text-red-400 font-medium flex items-center">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 mr-1 text-red-400" fill="none"
                              viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                              <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                            Out of stock
                          </span>
                        </c:otherwise>
                      </c:choose>
                    </div>

                    <!-- Rating Stars -->
                    <c:if test="${not empty bookRating}">
                      <div class="flex items-center mb-4">
                        <c:forEach var="i" begin="1" end="5">
                          <c:choose>
                            <c:when test="${i <= bookRating}">
                              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-400 fill-yellow-400"
                                viewBox="0 0 20 20" fill="currentColor">
                                <path
                                  d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975a1 1 0 00.95.69h4.179c.969 0 1.371 1.24.588 1.81l-3.385 2.46a1 1 0 00-.364 1.118l1.287 3.975c.3.921-.755 1.688-1.54 1.118l-3.385-2.46a1 1 0 00-1.176 0l-3.385 2.46c-.784.57-1.838-.197-1.54-1.118l1.287-3.975a1 1 0 00-.364-1.118L2.046 9.402c-.783-.57-.38-1.81.588-1.81h4.179a1 1 0 00.95-.69l1.286-3.975z" />
                              </svg>
                            </c:when>
                            <c:otherwise>
                              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-500 fill-gray-500"
                                viewBox="0 0 20 20" fill="currentColor">
                                <path
                                  d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975a1 1 0 00.95.69h4.179c.969 0 1.371 1.24.588 1.81l-3.385 2.46a1 1 0 00-.364 1.118l1.287 3.975c.3.921-.755 1.688-1.54 1.118l-3.385-2.46a1 1 0 00-1.176 0l-3.385 2.46c-.784.57-1.838-.197-1.54-1.118l1.287-3.975a1 1 0 00-.364-1.118L2.046 9.402c-.783-.57-.38-1.81.588-1.81h4.179a1 1 0 00.95-.69l1.286-3.975z" />
                              </svg>
                            </c:otherwise>
                          </c:choose>
                        </c:forEach>

                        <span class="ml-2 text-gray-400 text-sm">
                          (
                          <fmt:formatNumber value="${bookRating}" minFractionDigits="1" /> / 5)
                        </span>
                      </div>
                    </c:if>

                    <!-- Price -->
                    <p class="text-2xl font-bold text-amber-400 mb-6">
                      <c:choose>
                        <c:when test="${not empty bookPrice}">£${bookPrice}</c:when>
                        <c:otherwise>—</c:otherwise>
                      </c:choose>
                    </p>

                    <!-- Buttons -->
                    <div class="flex gap-3">
                      <a href="#"
                        class="bg-amber-600 hover:bg-amber-700 text-white px-6 py-3 rounded-full font-semibold">
                        Add to Cart
                      </a>
                      <c:if test="${not empty bookUrl}">
                        <a href="${bookUrl}" target="_blank"
                          class="border border-amber-600 hover:bg-amber-700/20 text-amber-400 px-6 py-3 rounded-full font-semibold">
                          View Source
                        </a>
                      </c:if>
                    </div>
                  </div>
                </div>

                <!-- Product Description -->
                <div class="bg-[#1b1b1b] mt-10 p-8 rounded-lg shadow-md">
                  <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Product Description
                  </h2>
                  <p class="text-gray-300 leading-relaxed text-justify tracking-wide">
                    <c:out value='${bookDescription != null ? bookDescription : "No description available."}' />
                  </p>
                </div>

                <!-- Product Info Table -->
                <div class="bg-[#1b1b1b] mt-8 p-8 rounded-lg shadow-md">
                  <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Product Information
                  </h2>
                  <div class="overflow-x-auto">
                    <table class="min-w-full border-collapse">
                      <tbody class="divide-y divide-[#333] text-gray-300">
                        <tr>
                          <td class="py-3 font-semibold w-1/3 text-gray-400">UPC</td>
                          <td>${bookUpc}</td>
                        </tr>
                        <tr>
                          <td class="py-3 font-semibold text-gray-400">Product Type</td>
                          <td>Books</td>
                        </tr>
                        <tr>
                          <td class="py-3 font-semibold text-gray-400">Price (excl. tax)</td>
                          <td>£${bookPrice}</td>
                        </tr>
                        <tr>
                          <td class="py-3 font-semibold text-gray-400">Price (incl. tax)</td>
                          <td>£${bookPrice}</td>
                        </tr>
                        <tr>
                          <td class="py-3 font-semibold text-gray-400">Tax</td>
                          <td>£0.00</td>
                        </tr>
                        <tr>
                          <td class="py-3 font-semibold text-gray-400">Availability</td>
                          <td>
                            <c:choose>
                              <c:when test="${not empty bookAvailability}">
                                ${bookAvailability}
                              </c:when>
                              <c:otherwise>
                                Out of stock
                              </c:otherwise>
                            </c:choose>
                          </td>
                        </tr>
                        <tr>
                          <td class="py-3 font-semibold text-gray-400">Number of reviews</td>
                          <td>${reviewCount}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                </div>
              </c:if>
            </div>

            <%@ include file="/WEB-INF/includes/footer.jsp" %>

        </body>


        </html>