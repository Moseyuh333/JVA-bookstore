<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<c:set var="pageTitle" value="Bookish Bliss Haven | Chi tiết sách" />
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="bg-gray-50">
  <div class="max-w-6xl mx-auto px-6 py-12">
    <c:if test="${not empty error}">
      <div class="bg-red-600/70 text-white p-4 rounded">${error}</div>
    </c:if>

    <c:if test="${empty error}">
      <!-- ===== BOOK INFO ===== -->
      <div class="flex flex-col md:flex-row bg-white rounded-lg p-6 gap-8 shadow-md text-gray-800">
        <div class="md:w-1/3 flex justify-center items-center">
          <img src="<c:out value='${imageUrl != null && imageUrl ne "" ? imageUrl : "https://placehold.co/400x550"}' />"
            class="rounded-lg shadow-md object-contain max-w-[320px] max-h-[480px]">
        </div>

        <div class="md:w-2/3 flex flex-col justify-center">
          <h1 class="text-3xl font-bold text-amber-700 mb-3">${bookTitle}</h1>

          <p class="text-gray-700 mb-2">
            Tác giả: <span class="text-gray-900 font-medium">${bookAuthor}</span>
          </p>
          <p class="text-gray-700 mb-2">
            Danh mục: <span class="text-amber-700 font-medium">${bookCategory}</span>
          </p>
          <p class="text-gray-700 mb-2">
            Cửa hàng: <span class="text-amber-700 font-medium">${bookShop}</span>
          </p>

          <!-- Rating -->
          <div class="flex items-center gap-2 mt-2 mb-3">
            <c:forEach var="i" begin="1" end="5">
              <svg xmlns="http://www.w3.org/2000/svg"
                class="h-5 w-5 ${i <= bookRating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-400 fill-gray-400'}"
                viewBox="0 0 20 20" fill="currentColor">
                <path
                  d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975h4.179c.969 0 1.371 1.24.588 1.81l-3.385 2.46 
                  1.287 3.975c.3.921-.755 1.688-1.54 1.118l-3.385-2.46-3.385 2.46c-.784.57-1.838-.197-1.54-1.118
                  l1.287-3.975-3.385-2.46c-.783-.57-.38-1.81.588-1.81h4.179l1.286-3.975z" />
              </svg>
            </c:forEach>
            <span class="text-gray-500 text-sm ml-1">(${bookRating}/5)</span>
          </div>

          <!-- Price -->
          <div class="flex items-baseline gap-3 mb-5">
            <span class="text-3xl font-bold text-amber-700">
              <fmt:formatNumber value="${bookPrice}" type="number" /> đ
            </span>
            <c:if test="${not empty bookOriginalPrice}">
              <span class="line-through text-gray-500 text-lg">
                <fmt:formatNumber value="${bookOriginalPrice}" type="number" /> đ
              </span>
            </c:if>
            <c:if test="${bookDiscount > 0}">
              <c:if test="${bookOriginalPrice > bookPrice}">
                <c:set var="discountPercent"
                  value="${(bookOriginalPrice - bookPrice) * 100 / bookOriginalPrice}" />
                <span class="bg-red-600 text-white text-sm px-2 py-1 rounded">
                  - <fmt:formatNumber value="${discountPercent}" maxFractionDigits="0" />%
                </span>
              </c:if>
            </c:if>
          </div>

          <!-- Stock -->
          <p class="text-sm mb-4 text-gray-700">
            <span class="text-gray-600">Tình trạng:</span>
            <c:choose>
              <c:when
                test="${fn:containsIgnoreCase(bookStock, 'avail') || fn:containsIgnoreCase(bookStock, 'in stock')}">
                <span class="text-green-600 font-medium">Còn hàng</span>
              </c:when>
              <c:when test="${fn:containsIgnoreCase(bookStock, 'out')}">
                <span class="text-red-500 font-medium">Hết hàng</span>
              </c:when>
              <c:otherwise>
                <span class="text-gray-600">Không rõ</span>
              </c:otherwise>
            </c:choose>
          </p>

          <!-- Buttons -->
          <div class="flex flex-wrap gap-3">
            <button type="button" class="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-3 rounded-md transition" data-buy-now data-book-id="${bookId}">
              Mua ngay
            </button>
            <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-md transition" data-add-to-cart data-book-id="${bookId}">
              Thêm vào giỏ
            </button>
          </div>
        </div>
      </div>

      <!-- ===== BOOK DETAILS ===== -->
      <div class="bg-white mt-8 p-8 rounded-lg shadow-md text-gray-800">
        <h2 class="text-xl font-semibold text-amber-700 mb-4 border-b border-gray-200 pb-2">Thông tin chi tiết</h2>
        <table class="w-full text-gray-700">
          <tbody class="divide-y divide-gray-200">
            <c:forEach var="spec" items="${fn:split(bookSpecifications, '|')}">
              <tr>
                <td class="py-3 font-medium w-1/3 text-gray-600">
                  <c:out value="${fn:split(spec, ':')[0]}" />
                </td>
                <td class="py-3">
                  <c:out value="${fn:split(spec, ':')[1]}" />
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>

      <!-- ===== DESCRIPTION ===== -->
      <div class="bg-white mt-8 p-8 rounded-lg shadow-md text-gray-800">
        <h2 class="text-xl font-semibold text-amber-700 mb-4 border-b border-gray-200 pb-2">Mô tả sản phẩm</h2>
        <div class="text-gray-700 leading-relaxed prose max-w-none">${bookDescription}</div>
      </div>

      <!-- ===== RELATED BOOKS ===== -->
      <c:if test="${not empty relatedBooks}">
        <div class="bg-white mt-8 p-8 rounded-lg shadow-md text-gray-800">
          <h2 class="text-xl font-semibold text-amber-700 mb-4 border-b border-gray-200 pb-2">Sản phẩm tương tự</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
            <c:forEach var="b" items="${relatedBooks}">
              <div class="bg-gray-50 rounded-lg overflow-hidden hover:shadow-lg transition">
                <a href="${pageContext.request.contextPath}/books/detail?id=${b.id}">
                  <img src="<c:out value='${b.imageUrl != null && b.imageUrl ne "" ? b.imageUrl : "https://placehold.co/300x400"}' />"
                    alt="${b.title}" class="w-full h-56 object-cover">
                  <div class="p-4">
                    <h3 class="text-amber-700 font-semibold text-lg truncate">${b.title}</h3>
                    <p class="text-gray-600 text-sm mb-2">${b.category}</p>
                    <p class="text-amber-600 font-semibold">
                      <fmt:formatNumber value="${b.price}" type="number" /> đ
                    </p>
                  </div>
                </a>
              </div>
            </c:forEach>
          </div>
        </div>
      </c:if>

      <!-- ===== REVIEWS ===== -->
      <c:choose>
        <c:when test="${not empty reviews}">
          <div class="bg-white mt-10 p-8 rounded-lg text-gray-700 border border-gray-200 mb-20">
            <h2 class="text-xl font-semibold text-amber-700 mb-4 border-b border-gray-300 pb-2">Khách hàng đánh giá</h2>

            <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-8">
              <!-- Cột điểm trung bình -->
              <div class="text-center md:text-left md:w-1/4">
                <div class="text-5xl font-bold text-amber-700">
                  <fmt:formatNumber value="${bookRating}" maxFractionDigits="1" />
                </div>
                <div class="flex justify-center md:justify-start mt-2 mb-1">
                  <c:forEach var="i" begin="1" end="5">
                    <svg xmlns="http://www.w3.org/2000/svg"
                      class="h-6 w-6 ${i <= bookRating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300 fill-gray-300'}"
                      viewBox="0 0 20 20" fill="currentColor">
                      <path
                        d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975h4.18c.969 0 
                        1.371 1.24.588 1.81l-3.39 2.463 1.287 3.974c.3.922-.755 1.688-1.54 
                        1.118L10 13.347l-3.363 2.92c-.785.57-1.84-.196-1.54-1.118l1.287-3.974
                        -3.39-2.463c-.783-.57-.381-1.81.588-1.81h4.18l1.287-3.975z" />
                    </svg>
                  </c:forEach>
                </div>
                <p class="text-gray-500 text-sm">(${fn:length(reviews)} đánh giá)</p>
              </div>

              <!-- Biểu đồ tỷ lệ sao -->
              <div class="flex-1 text-sm flex flex-col-reverse">
                <c:forEach var="s" begin="1" end="5">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="w-10 text-gray-600">${s} sao</span>
                    <div class="flex-1 bg-gray-200 h-2 rounded">
                      <div class="bg-amber-500 h-2 rounded" data-review-progress="<c:out value='${reviewStats[s]}'/>"></div>
                    </div>
                    <span class="w-10 text-gray-600 text-right">${reviewStats[s]}%</span>
                  </div>
                </c:forEach>
              </div>

              <!-- Danh sách đánh giá -->
              <div class="mt-8 divide-y divide-gray-200">
                <c:forEach var="r" items="${reviews}">
                  <div class="py-5">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-3">
                        <div
                          class="bg-gray-200 text-gray-700 rounded-full h-9 w-9 flex items-center justify-center font-bold uppercase">
                          ${fn:substring(r.authorName, 0, 1)}
                        </div>
                        <div>
                          <p class="text-gray-800 font-semibold">${r.authorName}</p>
                          <p class="text-green-600 text-xs">Đã mua hàng</p>
                        </div>
                      </div>
                    </div>

                    <div class="flex items-center mb-2">
                      <c:forEach var="i" begin="1" end="5">
                        <svg xmlns="http://www.w3.org/2000/svg"
                          class="h-4 w-4 ${i <= r.rating ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300 fill-gray-300'}"
                          viewBox="0 0 20 20" fill="currentColor">
                          <path
                            d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975h4.18c.969 0 
                            1.371 1.24.588 1.81l-3.39 2.463 
                            1.287 3.974c.3.922-.755 1.688-1.54 1.118L10 13.347l-3.363 2.92
                            c-.785.57-1.84-.196-1.54-1.118l1.287-3.974
                            -3.39-2.463c-.783-.57-.381-1.81.588-1.81h4.18l1.287-3.975z" />
                        </svg>
                      </c:forEach>
                      <span class="ml-2 text-sm text-amber-700 font-medium">
                        <c:choose>
                          <c:when test="${r.rating >= 5}">Cực kì hài lòng</c:when>
                          <c:when test="${r.rating >= 4}">Hài lòng</c:when>
                          <c:when test="${r.rating >= 3}">Tạm ổn</c:when>
                          <c:otherwise>Không hài lòng</c:otherwise>
                        </c:choose>
                      </span>
                    </div>
                    <p class="text-gray-700 leading-relaxed whitespace-pre-line break-words">
                      <c:out value="${r.comment}" />
                    </p>
                  </div>
                </c:forEach>
              </div>
            </div>
          </div>
        </c:when>

        <c:otherwise>
          <div class="bg-white mt-10 p-8 rounded-lg text-gray-700 border border-gray-200 mb-20">
            <i>Chưa có đánh giá nào cho cuốn sách này.</i>
          </div>
        </c:otherwise>
      </c:choose>
    </c:if>
  </div>

  <script>
    (function (window, document) {
      'use strict';

      function formatReviewBars() {
        document.querySelectorAll('[data-review-progress]').forEach(function (bar) {
          var raw = bar.getAttribute('data-review-progress');
          var value = parseInt(raw, 10);
          if (Number.isNaN(value)) {
            value = 0;
          }
          value = Math.max(0, Math.min(100, value));
          bar.style.width = value + '%';
        });
      }

      function handleBuyNowClick(event) {
        event.preventDefault();
        var button = event.currentTarget;
        var contextPath = (window.appShell ? window.appShell.contextPath : '');
        var bookId = parseInt(button.getAttribute('data-book-id'), 10);
        if (Number.isNaN(bookId) || bookId <= 0) {
          window.location.href = contextPath + '/catalog.jsp#cart';
          return;
        }

        var cartClient = window.cartClient;
        var apiClient = window.apiClient;
        if (!apiClient) {
          window.location.href = contextPath + '/catalog.jsp#cart';
          return;
        }

        button.disabled = true;
        button.classList.add('opacity-60');

        var promise;
        if (cartClient && typeof cartClient.startBuyNow === 'function') {
          promise = cartClient.startBuyNow(bookId, 1);
        } else {
          promise = apiClient.post('/checkout/buy-now', { bookId: bookId, quantity: 1 });
        }

        promise
          .then(function (result) {
            if (!result || result.success !== true) {
              throw new Error('Không thể tạo đơn mua ngay');
            }
            window.location.href = contextPath + '/checkout.jsp?mode=buy-now';
          })
          .catch(function (error) {
            console.error('Buy now error', error);
            if (cartClient && typeof cartClient.showToast === 'function') {
              cartClient.showToast('Không thể mua ngay sản phẩm. Vui lòng thử lại.', true);
            }
          })
          .finally(function () {
            button.disabled = false;
            button.classList.remove('opacity-60');
          });
      }

      function setup() {
        formatReviewBars();
        var buyNowButton = document.querySelector('[data-buy-now]');
        if (buyNowButton) {
          buyNowButton.addEventListener('click', handleBuyNowClick);
        }
      }

      if (window.appShell && typeof window.appShell.onReady === 'function') {
        window.appShell.onReady(setup);
      } else {
        document.addEventListener('DOMContentLoaded', setup);
      }
    })(window, document);
  </script>
  <%@ include file="/WEB-INF/includes/footer.jsp" %>
</main>

</body>
</html>
