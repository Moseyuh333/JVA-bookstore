<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
  <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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

                <!-- Rating + stock -->
                <div class="flex items-center gap-4 mb-4">
                  <c:if test="${not empty bookRatingInt}">
                    <span
                      class="inline-flex items-center bg-amber-700/20 text-amber-400 px-3 py-1 rounded-full text-sm">
                      ★ <%= String.format("%.1f", (request.getAttribute("bookRatingInt") !=null ?
                        Double.parseDouble(request.getAttribute("bookRatingInt").toString()) : 0.0)) %>/5
                    </span>
                  </c:if>
                  <c:choose>
                    <c:when test="${bookInStock}">
                      <span class="text-green-400 font-medium">${bookStockText}</span>
                    </c:when>
                    <c:otherwise>
                      <span class="text-red-400 font-medium">Out of stock</span>
                    </c:otherwise>
                  </c:choose>
                </div>

                <!-- Price -->
                <p class="text-2xl font-bold text-amber-400 mb-6">
                  <c:choose>
                    <c:when test="${not empty bookPrice}">$${bookPrice}</c:when>
                    <c:otherwise>—</c:otherwise>
                  </c:choose>
                </p>

                <!-- Buttons -->
                <div class="flex gap-3">
                  <a href="#" class="bg-amber-600 hover:bg-amber-700 text-white px-6 py-3 rounded-full font-semibold">
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
              <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Product Description</h2>
              <p class="text-gray-300 leading-relaxed text-justify tracking-wide">
                <c:out value='${bookDescription != null ? bookDescription : "No description available."}' />
              </p>
            </div>

            <!-- Product Info Table -->
            <div class="bg-[#1b1b1b] mt-8 p-8 rounded-lg shadow-md">
              <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Product Information</h2>
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
                      <td>$${bookPrice}</td>
                    </tr>
                    <tr>
                      <td class="py-3 font-semibold text-gray-400">Price (incl. tax)</td>
                      <td>$${bookPrice}</td>
                    </tr>
                    <tr>
                      <td class="py-3 font-semibold text-gray-400">Tax</td>
                      <td>$0.00</td>
                    </tr>
                    <tr>
                      <td class="py-3 font-semibold text-gray-400">Availability</td>
                      <td>
                        <c:choose>
                          <c:when test="${not empty bookAvailability}">
                            <c:choose>
                              <c:when test="${fn:containsIgnoreCase(bookAvailability, 'in stock')}">
                                <span class="text-green-400 font-medium">${bookAvailability}</span>
                              </c:when>
                              <c:when test="${fn:containsIgnoreCase(bookAvailability, 'out')}">
                                <span class="text-red-400 font-medium">${bookAvailability}</span>
                              </c:when>
                              <c:otherwise>
                                <span class="text-amber-400">${bookAvailability}</span>
                              </c:otherwise>
                            </c:choose>
                          </c:when>
                          <c:otherwise>
                            <span class="text-gray-400 italic">No info</span>
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