<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
      <%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
        <!DOCTYPE html>
        <html lang="vi">

        <%@ include file="/WEB-INF/includes/header.jsp" %>

          <div class="max-w-6xl mx-auto px-6 py-12">
            <c:if test="${not empty error}">
              <div class="bg-red-600 text-white p-4 rounded">${error}</div>
            </c:if>

            <c:if test="${empty error}">
              <!-- BOOK DETAIL -->
              <div class="flex flex-col md:flex-row bg-[#1b1b1b] rounded-lg p-6 gap-8 shadow-lg">

                <!-- Book Image -->
                <div class="md:w-1/3 flex justify-center items-center">
                  <img src="<c:out value='${bookImage != null && bookImage ne "" ? bookImage : "
                    https://placehold.co/400x550"}' />"
                  alt="${bookTitle}" class="book-img max-w-[320px] max-h-[480px] object-contain">
                </div>

                <!-- Info -->
                <div class="md:w-2/3 flex flex-col justify-center">
                  <h1 class="text-3xl font-bold text-amber-400 mb-3">${bookTitle}</h1>

                  <p class="text-gray-400 mb-2">Tác giả: <span class="text-gray-200 font-medium">${bookAuthor}</span>
                  </p>
                  <p class="text-gray-400 mb-2">Nhà xuất bản: <span
                      class="text-gray-200 font-medium">${bookPublisher}</span></p>
                  <p class="text-gray-400 mb-2">Danh mục: <span
                      class="text-amber-400 font-medium">${bookCategory}</span></p>
                  <p class="text-gray-400 mb-2">Cửa hàng: <span class="text-amber-400 font-medium">${bookShop}</span>
                  </p>

                  <!-- Rating -->
                  <div class="flex items-center gap-2 mt-2 mb-3">
                    <c:forEach var="i" begin="1" end="5">
                      <c:choose>
                        <c:when test="${i <= bookRating}">
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-yellow-400 fill-yellow-400"
                            viewBox="0 0 20 20" fill="currentColor">
                            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975h4.179c.969 0 1.371 1.24.588 1.81l-3.385 2.46
                             1.287 3.975c.3.921-.755 1.688-1.54 1.118l-3.385-2.46-3.385 2.46c-.784.57-1.838-.197-1.54-1.118
                             l1.287-3.975-3.385-2.46c-.783-.57-.38-1.81.588-1.81h4.179l1.286-3.975z" />
                          </svg>
                        </c:when>
                        <c:otherwise>
                          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-gray-600 fill-gray-600"
                            viewBox="0 0 20 20" fill="currentColor">
                            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.286 3.975..." />
                          </svg>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                    <span class="text-gray-400 text-sm ml-1">(${bookRating}/5)</span>
                  </div>

                  <!-- Price -->
                  <div class="flex items-baseline gap-3 mb-5">
                    <span class="text-3xl font-bold text-amber-400">
                      <fmt:formatNumber value="${bookPrice}" type="number" minFractionDigits="0" /> đ
                    </span>
                    <c:if test="${not empty bookOriginalPrice}">
                      <span class="line-through text-gray-500 text-lg">
                        <fmt:formatNumber value="${bookOriginalPrice}" type="number" /> đ
                      </span>
                    </c:if>
                    <c:if test="${bookDiscount > 0}">
                      <span class="bg-red-600 text-white text-sm px-2 py-1 rounded">-${bookDiscount}%</span>
                    </c:if>
                  </div>

                  <!-- Stock -->
                  <p class="text-sm mb-4">
                    <span class="text-gray-400">Tình trạng:</span>
                    <c:choose>
                      <c:when test="${fn:containsIgnoreCase(bookStock, 'avail')}">
                        <span class="text-green-400 font-medium">Còn hàng</span>
                      </c:when>
                      <c:when test="${fn:containsIgnoreCase(bookStock, 'in stock')}">
                        <span class="text-green-400 font-medium">Còn hàng</span>
                      </c:when>
                      <c:when test="${fn:containsIgnoreCase(bookStock, 'out')}">
                        <span class="text-red-500 font-medium">Hết hàng</span>
                      </c:when>
                      <c:otherwise>
                        <span class="text-gray-400">Không rõ</span>
                      </c:otherwise>
                    </c:choose>
                  </p>

                  <!-- Buttons -->
                  <div class="flex gap-4">
                    <button class="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-3 rounded-md">Mua
                      ngay</button>
                    <button class="bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-md">Thêm
                      vào giỏ</button>
                  </div>
                </div>
              </div>

              <!-- Description -->
              <div class="bg-[#1b1b1b] mt-8 p-8 rounded-lg shadow-md">
                <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Mô tả sản phẩm</h2>
                <p class="text-gray-300 leading-relaxed whitespace-pre-line">
                  <c:out value="${bookDescription != null ? bookDescription : 'Chưa có mô tả cho sản phẩm này.'}" />
                </p>
              </div>

              <!-- Specifications -->
              <c:if test="${not empty bookSpecifications}">
                <div class="bg-[#1b1b1b] mt-8 p-8 rounded-lg shadow-md">
                  <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Thông tin chi tiết
                  </h2>
                  <table class="w-full text-gray-300">
                    <tbody class="divide-y divide-[#333]">
                      <c:forEach var="spec" items="${fn:split(bookSpecifications, '|')}">
                        <tr>
                          <td class="py-3 font-medium w-1/3 text-gray-400">
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
              </c:if>

              <!-- Reviews -->
              <c:if test="${not empty reviews}">
                <div class="bg-[#1b1b1b] mt-8 p-8 rounded-lg shadow-md">
                  <h2 class="text-xl font-semibold text-amber-400 mb-4 border-b border-[#333] pb-2">Đánh giá từ người
                    đọc</h2>
                  <c:forEach var="r" items="${reviews}">
                    <div class="border-b border-[#333] py-4">
                      <p class="font-semibold text-gray-200 mb-1">${r.authorName}</p>
                      <p class="text-gray-400 mb-2 text-sm">
                        <fmt:formatDate value="${r.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                      </p>
                      <p class="text-gray-300">${r.comment}</p>
                    </div>
                  </c:forEach>
                </div>
              </c:if>
            </c:if>
          </div>

          <%@ include file="/WEB-INF/includes/footer.jsp" %>
            </body>

        </html>