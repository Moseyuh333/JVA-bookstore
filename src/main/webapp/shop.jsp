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

                <c:if test="${not empty books}">
                    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
                        <c:forEach var="b" items="${books}">
                            <a href="${pageContext.request.contextPath}/books/detail?id=${b.id}"
                                class="bg-gray-900 rounded-lg overflow-hidden hover:scale-[1.03] transition block">
                                <img src="${b.coverImage}" class="w-full h-64 object-cover" alt="${b.title}">
                                <div class="p-4">
                                    <h3 class="font-semibold mb-1 text-lg">${b.title}</h3>
                                    <p class="text-sm text-gray-400">${b.author}</p>
                                    <span class="text-amber-400 font-semibold">£${b.price}</span>
                                </div>
                            </a>
                        </c:forEach>
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