<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
        <!DOCTYPE html>
        <html lang="en">

        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>All Books | Bookish Bliss Haven</title>
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

        <body class="bg-gray-950 text-gray-100 font-sans">
            <!-- Header -->
            <jsp:include page="/WEB-INF/includes/header.jsp" />

            <!-- Main content -->
            <section class="py-16 px-6 container mx-auto">
                <h1 class="text-4xl font-bold text-center text-amber-400 mb-10">All Books</h1>
                <c:if test="${not empty error}">
                    <div class="bg-red-900 text-white p-4 rounded mb-4">
                        ${error}
                    </div>
                </c:if>

                <c:if test="${not empty relatedBooks}">
                  <div class="bg-[#1b1b1b] mt-10 p-8 rounded-lg shadow-md">
                    <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">
                      Related Books
                    </h2>
                    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
                      <c:forEach var="b" items="${relatedBooks}">
                        <div class="bg-[#222] rounded-lg overflow-hidden shadow hover:shadow-lg transition">
                          <a href="${pageContext.request.contextPath}/books/detail?id=${b.id}">
                            <img src="<c:out value='${b.coverImage != null && b.coverImage ne "" ? b.coverImage : "
                              https://placehold.co/300x400"}' />"
                            alt="${b.title}" class="w-full h-56 object-cover">
                            <div class="p-4">
                              <h3 class="text-amber-400 font-semibold text-lg truncate">${b.title}</h3>
                              <p class="text-gray-400 text-sm mb-2">${b.category}</p>
                              <p class="text-amber-300 font-semibold">£
                                <fmt:formatNumber value="${b.price}" type="number" minFractionDigits="2" />
                              </p>
                            </div>
                          </a>
                        </div>
                      </c:forEach>
                    </div>
                  </div>
                </c:if>

                <c:if test="${empty books}">
                    <p class="text-center text-gray-400 mt-12">No books available at the moment.</p>
                </c:if>
            </section>

            <!-- Footer -->
            <jsp:include page="/WEB-INF/includes/footer.jsp" />
        </body>

        </html>