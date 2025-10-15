<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>All Books | Bookish Bliss Haven</title>
  <script src="https://cdn.tailwindcss.com"></script>
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
             class="bg-gray-900 rounded-lg overflow-hidden hover:scale-[1.03] hover:shadow-lg transition block">
            <img src="<c:out value='${b.coverImage != null && b.coverImage ne "" ? b.coverImage : "https://placehold.co/300x400"}' />"
                 alt="${b.title}" 
                 class="w-full h-64 object-cover">
            
            <!-- Info-->
            <div class="p-4">
              <h3 class="font-semibold mb-1 text-lg line-clamp-2">${b.title}</h3>
              <p class="text-sm text-gray-400 mb-1">
                <c:out value="${b.author}" default="Unknown category" />
              </p>
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
