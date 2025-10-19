<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Bookish Bliss Haven | Danh mục sách" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="bg-gray-50 text-gray-800">
    <section class="catalog-hero text-white py-20 px-4">
        <div class="container mx-auto text-center space-y-4">
            <span class="uppercase tracking-wide text-amber-200 text-xs font-semibold">Bookish Bliss Haven</span>
            <h1 class="title-font text-4xl md:text-5xl font-bold">Khám phá kho sách phong phú</h1>
            <p class="max-w-2xl mx-auto text-amber-100 text-lg">Tìm kiếm, lọc và khám phá những tựa sách được độc giả yêu thích nhất tại cửa hàng của chúng tôi.</p>
        </div>
    </section>

    <section class="py-12 px-4">
        <div class="container mx-auto space-y-8">
            <div class="bg-white rounded-2xl shadow-sm border border-amber-100/60 p-6">
                <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
                    <div class="flex items-center gap-4">
                        <span class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-amber-100 text-amber-700">
                            <i data-feather="filter" class="w-5 h-5"></i>
                        </span>
                        <div>
                            <h2 class="title-font text-2xl font-semibold">Bộ lọc danh mục</h2>
                            <p class="text-gray-500 text-sm">Tùy chỉnh danh sách sách theo nhu cầu đọc của bạn.</p>
                        </div>
                    </div>
                    <div class="flex flex-col sm:flex-row sm:items-center gap-4 text-sm text-gray-600">
                        <div class="flex flex-col gap-2">
                            <label class="font-medium" for="categoryFilter">Danh mục</label>
                            <select id="categoryFilter" class="min-w-[220px] px-4 py-2.5 border border-gray-200 rounded-full focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm">
                                <option value="">Tất cả</option>
                            </select>
                        </div>
                        <div class="flex flex-col gap-2">
                            <label class="font-medium" for="sortSelect">Sắp xếp theo</label>
                            <select id="sortSelect" class="min-w-[220px] px-4 py-2.5 border border-gray-200 rounded-full focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 bg-white text-sm">
                                <option value="new">Sản phẩm mới</option>
                                <option value="best">Bán chạy nhất</option>
                                <option value="rated">Đánh giá cao nhất</option>
                                <option value="favorite">Được yêu thích</option>
                            </select>
                        </div>
                    </div>
                </div>

                <div class="mt-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <p id="resultSummary" class="text-sm text-gray-600">Đang tải danh sách sách...</p>
                    <a href="<%=request.getContextPath()%>/index.jsp" class="inline-flex items-center text-amber-700 hover:text-amber-900 font-medium text-sm">
                        Về trang chủ
                        <i data-feather="arrow-up-right" class="w-4 h-4 ml-1"></i>
                    </a>
                </div>
            </div>

            <section class="space-y-6">
                <div id="catalogGrid" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-4 gap-6"></div>
                <div id="emptyState" class="hidden text-center py-16 bg-white rounded-xl border border-dashed border-amber-200 text-gray-500">
                    Không tìm thấy sách phù hợp với bộ lọc hiện tại.
                </div>
                <div class="flex flex-col items-center gap-3">
                    <button id="loadMoreBtn" class="hidden px-6 py-3 bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-full transition duration-200">
                        Tải thêm 20 sách
                    </button>
                    <p id="catalogStatus" class="text-sm text-gray-500"></p>
                </div>
            </section>
        </div>
    </section>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
<script src="<%=request.getContextPath()%>/assets/js/catalog-page.js"></script>
</body>
</html>
