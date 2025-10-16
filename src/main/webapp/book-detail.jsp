<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<!DOCTYPE html>
<html lang="vi">

<%@ include file="/WEB-INF/includes/header.jsp" %>

<div class="container mx-auto px-6 py-10">

  <c:if test="${not empty error}">
    <div class="bg-red-700/70 p-4 rounded text-red-100">${error}</div>
  </c:if>

  <c:if test="${empty error}">
    <!-- === Top Section: Cover + Info === -->
    <div class="flex flex-col lg:flex-row gap-10 bg-[#1b1b1b] p-8 rounded-xl shadow-lg">
      <!-- Left: Image -->
      <div class="flex-1 flex justify-center items-start">
        <img src="<c:out value='${bookImage != null && bookImage ne "" ? bookImage : "https://placehold.co/400x550"}'/>"
             alt="${bookTitle}" class="rounded-lg shadow-md w-[320px] object-contain">
      </div>

      <!-- Right: Info -->
      <div class="flex-[2] flex flex-col gap-4">
        <h1 class="text-3xl font-bold text-amber-400">${bookTitle}</h1>
        <p class="text-gray-300 text-sm">Tác giả: 
          <span class="text-amber-300 font-medium">${bookAuthor}</span>
        </p>

        <p class="text-gray-400 text-sm">Nhà xuất bản: 
          <span class="text-amber-300">${bookPublisher}</span>
        </p>

        <p class="text-gray-400 text-sm">Danh mục: 
          <span class="text-amber-300">${bookCategory}</span>
        </p>

        <!-- Rating -->
        <c:if test="${bookRating > 0}">
          <div class="flex items-center gap-1">
            <c:forEach var="i" begin="1" end="5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 
                ${i <= bookRating ? 'text-yellow-400' : 'text-gray-500'}" fill="currentColor" viewBox="0 0 20 20">
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975..."/>
              </svg>
            </c:forEach>
            <span class="ml-2 text-sm text-gray-400">
              <fmt:formatNumber value="${bookRating}" minFractionDigits="1" /> / 5 
              (<fmt:formatNumber value="${reviewCount}" /> đánh giá)
            </span>
          </div>
        </c:if>

        <!-- Price -->
        <div class="text-3xl font-bold text-amber-400">
          <fmt:formatNumber value="${bookPrice}" type="number" /> đ
          <c:if test="${bookDiscount > 0}">
            <span class="text-sm text-gray-400 line-through ml-2">
              <fmt:formatNumber value="${bookOriginalPrice}" type="number" /> đ
            </span>
          </c:if>
        </div>

        <!-- Shop + Stock -->
        <div class="text-sm">
          <span class="text-gray-400">Nhà bán: </span>
          <span class="font-semibold text-amber-300">${bookShop}</span>
        </div>
        <div class="text-sm">
          <span class="text-gray-400">Tình trạng: </span>
          <span class="text-green-400">${bookStock}</span>
        </div>

        <!-- Buttons -->
        <div class="flex gap-4 mt-4">
          <a href="#" class="bg-red-600 hover:bg-red-700 px-6 py-3 rounded-lg font-semibold">Mua ngay</a>
          <a href="#" class="bg-amber-600 hover:bg-amber-700 px-6 py-3 rounded-lg font-semibold">Thêm vào giỏ</a>
          <c:if test="${not empty bookUrl}">
            <a href="${bookUrl}" target="_blank" class="border border-amber-400 px-6 py-3 rounded-lg text-amber-400 hover:bg-amber-400 hover:text-black">
              Xem trên Tiki
            </a>
          </c:if>
        </div>
      </div>
    </div>

    <!-- === Highlights === -->
    <c:if test="${not empty bookHighlights}">
      <div class="card mt-10 p-8">
        <h2 class="section-title">Đặc điểm nổi bật</h2>
        <ul class="list-disc pl-6 text-gray-300 leading-relaxed">
          <c:forEach var="hl" items="${fn:split(bookHighlights, '|')}">
            <li>${hl}</li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <!-- === Specifications === -->
    <c:if test="${not empty bookSpecifications}">
      <div class="card mt-10 p-8">
        <h2 class="section-title">Thông tin chi tiết</h2>
        <table class="w-full text-gray-300 border-t border-[#333]">
          <tbody>
            <c:forEach var="spec" items="${fn:split(bookSpecifications, '|')}">
              <c:set var="pair" value="${fn:split(spec, ':')}" />
              <tr class="border-b border-[#333]">
                <td class="py-2 w-1/3 text-gray-400">${pair[0]}</td>
                <td class="py-2">${pair[1]}</td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </c:if>

    <!-- === Description === -->
    <div class="card mt-10 p-8">
      <h2 class="section-title">Mô tả sản phẩm</h2>
      <div class="text-gray-300 leading-relaxed">
        <c:out value="${bookDescription}" default="Chưa có mô tả cho sản phẩm này." escapeXml="false"/>
      </div>
    </div>

    <!-- === Reviews === -->
    <c:if test="${not empty bookReviews}">
      <div class="card mt-10 p-8">
        <h2 class="section-title">Khách hàng đánh giá</h2>
        <ul class="space-y-4 text-gray-300">
          <c:forEach var="rv" items="${fn:split(bookReviews, '|')}">
            <li class="border-b border-[#333] pb-3">${rv}</li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <!-- === Related Books === -->
    <c:if test="${not empty relatedBooks}">
      <div class="card mt-10 p-8">
        <h2 class="section-title">Sản phẩm tương tự</h2>
        <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6">
          <c:forEach var="b" items="${relatedBooks}">
            <a href="${pageContext.request.contextPath}/books/detail?id=${b.id}" class="block bg-[#222] rounded-lg overflow-hidden hover:scale-[1.02] transition">
              <img src="<c:out value='${b.coverImage != null && b.coverImage ne "" ? b.coverImage : "https://placehold.co/200x250"}'/>"
                   alt="${b.title}" class="w-full h-56 object-cover">
              <div class="p-3">
                <h3 class="font-semibold text-amber-300 truncate">${b.title}</h3>
                <p class="text-sm text-gray-400">
                  <fmt:formatNumber value="${b.price}" type="number" /> đ
                </p>
              </div>
            </a>
          </c:forEach>
        </div>
      </div>
    </c:if>

  </c:if>
</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
</body>
</html>
