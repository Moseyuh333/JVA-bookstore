<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Collections | Bookish Bliss Haven</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-950 text-gray-100 font-sans">
  <jsp:include page="/WEB-INF/components/header.jsp" />

  <section class="py-16 px-6 container mx-auto">
    <h1 class="text-4xl font-bold text-center text-amber-400 mb-12">Featured Collections</h1>

    <c:if test="${not empty collections}">
      <c:forEach var="entry" items="${collections}">
        <div class="mb-12">
          <h2 class="text-2xl font-bold text-amber-300 mb-6">${entry.key}</h2>
          <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-6">
            <c:forEach var="b" items="${entry.value}">
              <a href="${pageContext.request.contextPath}/books/detail?id=${b.id}" 
                 class="block bg-gray-900 rounded-lg overflow-hidden hover:scale-[1.03] transition">
                <img src="${b.coverImage}" class="w-full h-56 object-cover">
                <div class="p-3">
                  <h3 class="text-lg font-semibold mb-1">${b.title}</h3>
                  <span class="text-amber-400 font-semibold">£${b.price}</span>
                </div>
              </a>
            </c:forEach>
          </div>
        </div>
      </c:forEach>
    </c:if>

    <c:if test="${empty collections}">
      <p class="text-center text-gray-400">No collections available at the moment.</p>
    </c:if>
  </section>

  <jsp:include page="/WEB-INF/components/footer.jsp" />
</body>
</html>
