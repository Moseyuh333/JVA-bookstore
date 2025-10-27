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
          <img src="<c:out value='${bookImage != null && bookImage ne "" ? bookImage : "https://placehold.co/400x550"}' />"
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
            <span class="text-gray-500 text-sm ml-1">
              (<fmt:formatNumber value="${bookRating}" maxFractionDigits="1" /> / 5 · ${reviewCount} đánh giá)
            </span>
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
              <c:when test="${not empty bookStockLabel}">
                <span class="${empty bookStockCss ? 'text-green-600 font-medium' : bookStockCss}">
                  ${bookStockLabel}
                </span>
                <c:if test="${bookStockQuantity != null && bookStockQuantity > 0}">
                  <span class="text-xs text-gray-500 ml-2">
                    (Kho: <fmt:formatNumber value="${bookStockQuantity}" type="number" />)
                  </span>
                </c:if>
              </c:when>
              <c:otherwise>
                <span class="text-gray-600">Không rõ</span>
              </c:otherwise>
            </c:choose>
          </p>

          <p class="text-sm mb-4 text-gray-700">
            <span class="text-gray-600">Đã bán:</span>
            <span class="text-amber-700 font-medium">
              <fmt:formatNumber value="${soldCount}" type="number" />
            </span>
          </p>

          <!-- Buttons -->
          <div class="flex flex-wrap gap-3">
            <button type="button" class="bg-red-600 hover:bg-red-700 text-white font-semibold px-6 py-3 rounded-md transition" data-buy-now data-book-id="${bookId}">
              Mua ngay
            </button>
            <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-semibold px-6 py-3 rounded-md transition" data-add-to-cart data-book-id="${bookId}">
              Thêm vào giỏ
            </button>
            <button type="button" class="border border-amber-600 text-amber-600 bg-white hover:bg-amber-50 font-semibold px-6 py-3 rounded-md transition" data-toggle-favorite data-book-id="${bookId}" aria-pressed="false">
              Thêm vào yêu thích
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
                  <img src="<c:out value='${b.coverImage != null && b.coverImage ne "" ? b.coverImage : "https://placehold.co/300x400"}' />"
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
            <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-4 border-b border-gray-300 pb-2">
              <h2 class="text-xl font-semibold text-amber-700">Khách hàng đánh giá</h2>
              <c:if test="${userHasReview}">
                <button type="button" class="inline-flex items-center gap-2 px-4 py-2 rounded-md border border-amber-500 text-amber-600 hover:bg-amber-50 transition" data-scroll-own-review="${userReviewDomId}">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                  </svg>
                  Xem đánh giá của tôi
                </button>
              </c:if>
            </div>

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
              <div class="mt-8 divide-y divide-gray-200 w-full">
                <c:forEach var="r" items="${reviews}">
                  <c:set var="ownerClasses" value="${r.isOwner ? 'bg-amber-50 border-l-4 border-amber-500 rounded-md pl-4' : ''}" />
                  <div id="${r.domId}" class="py-5 transition-colors duration-300 ${ownerClasses}">
                    <div class="flex items-center justify-between mb-2">
                      <div class="flex items-center gap-3">
                        <div
                          class="bg-gray-200 text-gray-700 rounded-full h-9 w-9 flex items-center justify-center font-bold uppercase">
                          ${fn:substring(r.authorName, 0, 1)}
                        </div>
                        <div>
                          <p class="text-gray-800 font-semibold">${r.authorName}</p>
                          <p class="text-green-600 text-xs">Đã mua hàng</p>
                          <c:if test="${r.isOwner}">
                            <span class="text-xs font-semibold text-amber-600 uppercase tracking-wide">Đánh giá của bạn</span>
                          </c:if>
                          <c:if test="${not empty r.createdAt}">
                            <p class="text-gray-500 text-xs">${r.createdAt}</p>
                          </c:if>
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
                    <c:if test="${not empty r.mediaUrl}">
                      <div class="mt-3">
                        <c:choose>
                          <c:when test="${fn:toLowerCase(r.mediaType) eq 'video'}">
                            <video class="w-full max-w-md rounded-md shadow-sm" controls preload="metadata">
                              <source src="<c:out value='${r.mediaUrl}'/>" />
                              Trình duyệt của bạn không hỗ trợ phát video.
                            </video>
                          </c:when>
                          <c:otherwise>
                            <img src="<c:out value='${r.mediaUrl}'/>" alt="Ảnh minh hoạ bình luận" class="w-full max-w-md rounded-md shadow-sm object-cover" loading="lazy" />
                          </c:otherwise>
                        </c:choose>
                      </div>
                    </c:if>
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
            showToast('Không thể mua ngay sản phẩm. Vui lòng thử lại.', true);
          })
          .finally(function () {
            button.disabled = false;
            button.classList.remove('opacity-60');
          });
      }

      function showToast(message, isError) {
        var cartClient = window.cartClient;
        if (cartClient && typeof cartClient.showToast === 'function') {
          cartClient.showToast(message, isError);
          return;
        }
        var safeMessage = message || '';
        if (window.appShell && typeof window.appShell.escapeHtml === 'function') {
          safeMessage = window.appShell.escapeHtml(safeMessage);
        }
        if (isError) {
          window.alert(safeMessage);
        } else {
          console.log(safeMessage);
        }
      }

      function initFavoriteButton(bookId) {
        var button = document.querySelector('[data-toggle-favorite]');
        var apiClient = window.apiClient;
        if (!button || !apiClient || Number.isNaN(bookId) || bookId <= 0) {
          return;
        }

        var state = {
          isFavorite: false,
          loading: false,
          initialized: false
        };

        function setLoading(isLoading) {
          state.loading = isLoading;
          button.disabled = isLoading;
          button.classList.toggle('opacity-60', isLoading);
          button.classList.toggle('cursor-not-allowed', isLoading);
        }

        function renderButton() {
          var label = state.isFavorite ? 'Bỏ khỏi yêu thích' : 'Thêm vào yêu thích';
          button.textContent = label;
          button.setAttribute('aria-pressed', state.isFavorite ? 'true' : 'false');
          button.classList.toggle('bg-amber-600', state.isFavorite);
          button.classList.toggle('text-white', state.isFavorite);
          button.classList.toggle('border-amber-600', true);
          button.classList.toggle('text-amber-600', !state.isFavorite);
          button.classList.toggle('bg-white', !state.isFavorite);
        }

        function ensureLoggedIn() {
          var token = window.localStorage.getItem('auth_token');
          if (!token) {
            var contextPath = (window.appShell ? window.appShell.contextPath : '');
            window.location.href = contextPath + '/login.jsp';
            return false;
          }
          return true;
        }

        function toggleFavorite() {
          if (state.loading) {
            return;
          }
          if (!ensureLoggedIn()) {
            return;
          }
          setLoading(true);
          var path = '/profile/favorites/' + bookId;
          var requestPromise = state.isFavorite ? apiClient.del(path) : apiClient.post(path, {});
          requestPromise
            .then(function (result) {
              if (!result || result.success !== true) {
                throw new Error('Thao tác không thành công');
              }
              state.isFavorite = !state.isFavorite;
              renderButton();
              showToast(result.message || (state.isFavorite ? 'Đã thêm vào yêu thích' : 'Đã bỏ khỏi yêu thích'), false);
            })
            .catch(function (error) {
              console.error('Favorite toggle failed', error);
              showToast('Không thể cập nhật danh sách yêu thích. Vui lòng thử lại.', true);
            })
            .finally(function () {
              setLoading(false);
            });
        }

        function loadInitialState() {
          var token = window.localStorage.getItem('auth_token');
          if (!token) {
            renderButton();
            return;
          }
          setLoading(true);
          apiClient.get('/profile/favorites')
            .then(function (response) {
              if (response && response.success && Array.isArray(response.favorites)) {
                state.isFavorite = response.favorites.some(function (item) {
                  return Number(item.bookId) === bookId;
                });
              }
            })
            .catch(function (error) {
              if (error && error.status === 401) {
                state.isFavorite = false;
              } else {
                console.warn('Unable to load favorite state', error);
              }
            })
            .finally(function () {
              renderButton();
              setLoading(false);
              state.initialized = true;
            });
        }

        button.addEventListener('click', toggleFavorite);
        renderButton();
        loadInitialState();
      }

      function recordRecentView(bookId) {
        var apiClient = window.apiClient;
        if (!apiClient || Number.isNaN(bookId) || bookId <= 0) {
          return;
        }
        var token = window.localStorage.getItem('auth_token');
        if (!token) {
          return;
        }
        apiClient.post('/profile/recent-views', { bookId: bookId })
          .catch(function (error) {
            if (error && error.status === 404) {
              return;
            }
            console.debug('Unable to record recent view', error);
          });
      }

      function setup() {
        formatReviewBars();
        var buyNowButton = document.querySelector('[data-buy-now]');
        if (buyNowButton) {
          buyNowButton.addEventListener('click', handleBuyNowClick);
        }

        var anchor = buyNowButton || document.querySelector('[data-add-to-cart]') || document.querySelector('[data-toggle-favorite]');
        var bookId = anchor ? parseInt(anchor.getAttribute('data-book-id'), 10) : NaN;
        if (!Number.isNaN(bookId) && bookId > 0) {
          initFavoriteButton(bookId);
          recordRecentView(bookId);
        }

        var ownReviewButton = document.querySelector('[data-scroll-own-review]');
        if (ownReviewButton) {
          ownReviewButton.addEventListener('click', function () {
            var targetId = ownReviewButton.getAttribute('data-scroll-own-review');
            if (!targetId) {
              return;
            }
            var target = document.getElementById(targetId);
            if (!target) {
              console.warn('Không tìm thấy review với id', targetId);
              return;
            }
            if (typeof target.scrollIntoView === 'function') {
              target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
            target.classList.add('ring-2', 'ring-amber-400', 'ring-offset-2');
            setTimeout(function () {
              target.classList.remove('ring-2', 'ring-amber-400', 'ring-offset-2');
            }, 2400);
          });
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
