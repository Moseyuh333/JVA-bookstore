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
  <jsp:include page="/WEB-INF/components/header.jsp" />

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
              <span class="text-amber-400 font-semibold">£${b.price}</span>
            </div>
          </a>
        </c:forEach>
      </div>
    </c:if>

    <c:if test="${empty books}">
      <p class="text-center text-gray-400">No books available at the moment.</p>
    </c:if>
  </section>

  <jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>
