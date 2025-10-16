<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Khám phá danh mục sách · NK Bookstore</title>
	<meta name="description" content="Khám phá sách theo danh mục tại NK Bookstore với bộ lọc giá, đánh giá và tình trạng còn hàng.">
	<link rel="preconnect" href="https://fonts.googleapis.com">
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
	<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
	<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjX0k0p0iZr2D7wGv0y4" crossorigin="anonymous">
	<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
	<style>
		:root {
			--primary-color: #8B4513;
			--primary-dark: #5C2F10;
			--accent-color: #D97706;
			--muted-color: #6B7280;
			--background-color: #F5F6F8;
			--surface-color: #FFFFFF;
		}

		body {
			font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
			background-color: var(--background-color);
			color: #1F2937;
			line-height: 1.6;
		}

		a {
			color: inherit;
			text-decoration: none;
		}

		.navbar {
			background: linear-gradient(135deg, var(--primary-dark), var(--primary-color));
		}

		.navbar .nav-link {
			color: rgba(255, 255, 255, 0.92) !important;
			font-weight: 500;
			margin-right: 0.75rem;
			transition: opacity 0.2s ease, transform 0.2s ease;
		}

		.navbar .nav-link:hover,
		.navbar .nav-link:focus {
			opacity: 0.85;
			transform: translateY(-1px);
		}

		.navbar .form-control {
			border-radius: 999px;
			border: none;
			padding-left: 1rem;
		}

		.navbar .btn-outline-light {
			border-radius: 999px;
			border-color: rgba(255, 255, 255, 0.6);
			color: #fff;
		}

		.cart-indicator {
			position: relative;
		}

		.cart-indicator .cart-badge {
			position: absolute;
			top: -6px;
			right: -10px;
			background: #DC3545;
			color: #fff;
			border-radius: 999px;
			padding: 0 6px;
			font-size: 0.75rem;
			font-weight: 600;
		}

		.page-header {
			background: linear-gradient(140deg, rgba(139, 69, 19, 0.95), rgba(92, 47, 16, 0.98));
			color: #fff;
			padding: 3.5rem 0;
			position: relative;
		}

		.page-header::after {
			content: '';
			position: absolute;
			inset: 0;
			background: radial-gradient(120% 120% at 90% 15%, rgba(217, 119, 6, 0.35) 0%, rgba(217, 119, 6, 0) 60%);
			pointer-events: none;
		}

		.section-eyebrow {
			text-transform: uppercase;
			letter-spacing: 0.2em;
			font-size: 0.75rem;
			font-weight: 600;
			color: rgba(255, 255, 255, 0.7);
		}

		.section-title {
			font-size: 2.8rem;
			font-weight: 700;
			line-height: 1.15;
		}

		.filters-card {
			background: var(--surface-color);
			border-radius: 20px;
			padding: 1.8rem;
			border: 1px solid rgba(15, 23, 42, 0.08);
			position: sticky;
			top: 100px;
		}

		.filter-title {
			font-size: 0.92rem;
			font-weight: 600;
			text-transform: uppercase;
			letter-spacing: 0.08em;
			margin-bottom: 0.75rem;
			color: var(--muted-color);
		}

		.filter-separator {
			margin: 1.5rem 0;
			border-top: 1px dashed rgba(15, 23, 42, 0.12);
		}

		.badge-filter {
			border-radius: 999px;
			padding: 0.3rem 0.85rem;
			background: rgba(139, 69, 19, 0.08);
			color: var(--primary-dark);
			font-weight: 500;
			margin-right: 0.4rem;
			margin-bottom: 0.4rem;
			display: inline-flex;
			align-items: center;
			gap: 0.4rem;
		}

		.badge-filter i {
			cursor: pointer;
		}

		.book-card {
			background: #fff;
			border-radius: 20px;
			border: 1px solid rgba(15, 23, 42, 0.08);
			overflow: hidden;
			height: 100%;
			display: flex;
			flex-direction: column;
			transition: transform 0.2s ease, box-shadow 0.2s ease;
		}

		.book-card:hover {
			transform: translateY(-4px);
			box-shadow: 0 20px 45px rgba(15, 23, 42, 0.12);
		}

		.book-image {
			width: 100%;
			height: 260px;
			object-fit: cover;
		}

		.book-body {
			padding: 1.4rem;
			display: flex;
			flex-direction: column;
			gap: 0.7rem;
			flex-grow: 1;
		}

		.book-title {
			font-size: 1.1rem;
			font-weight: 600;
			margin: 0;
		}

		.book-meta {
			font-size: 0.9rem;
			color: var(--muted-color);
		}

		.book-price {
			font-weight: 700;
			color: var(--primary-dark);
			font-size: 1.05rem;
		}

		.book-actions {
			margin-top: auto;
			display: flex;
			gap: 0.6rem;
		}

		.btn-cart {
			flex-grow: 1;
			border-radius: 12px;
			background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
			border: none;
			color: #fff;
			padding: 0.55rem 1rem;
			font-weight: 600;
		}

		.btn-wishlist {
			width: 44px;
			border-radius: 12px;
			border: 1px solid rgba(15, 23, 42, 0.12);
			background: #fff;
			color: var(--primary-color);
		}

		.btn-wishlist:hover {
			background: rgba(139, 69, 19, 0.08);
		}

		.pagination-controls {
			display: flex;
			justify-content: space-between;
			align-items: center;
			gap: 1rem;
		}

		.empty-state {
			padding: 3rem 1rem;
			border-radius: 18px;
			background: rgba(15, 23, 42, 0.04);
		}

		@media (max-width: 991px) {
			.filters-card {
				position: static;
				margin-bottom: 2rem;
			}

			.section-title {
				font-size: 2.2rem;
			}
		}
	</style>
</head>
<body data-server-username="<%= (String) session.getAttribute("username") != null ? (String) session.getAttribute("username") : "" %>">
	<nav class="navbar navbar-expand-lg navbar-dark shadow-sm">
		<div class="container py-2">
			<a class="navbar-brand fw-semibold d-flex align-items-center" href="index.jsp">
				<i class="fas fa-book-open me-2"></i>
				NK Bookstore
			</a>
			<button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMain" aria-controls="navbarMain" aria-expanded="false" aria-label="Toggle navigation">
				<span class="navbar-toggler-icon"></span>
			</button>
			<div class="collapse navbar-collapse" id="navbarMain">
				<ul class="navbar-nav me-auto mb-3 mb-lg-0">
					<li class="nav-item">
						<a class="nav-link" href="index.jsp">Trang chủ</a>
					</li>
					<li class="nav-item">
						<a class="nav-link active" href="category.jsp">Danh mục</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="index.jsp#valueSection">Ưu điểm</a>
					</li>
				</ul>
				<form class="d-flex align-items-center mb-3 mb-lg-0 me-lg-3" id="navSearchForm" role="search">
					<input class="form-control form-control-sm" type="search" placeholder="Tìm kiếm sách" aria-label="Search" id="navSearchInput">
					<button class="btn btn-outline-light btn-sm ms-2" type="submit">
						<i class="fas fa-search"></i>
					</button>
				</form>
				<ul class="navbar-nav align-items-lg-center">
					<li class="nav-item me-lg-3 cart-indicator">
						<a class="nav-link position-relative" href="cart.jsp">
							<i class="fas fa-shopping-cart"></i>
							<span>Giỏ hàng</span>
							<span class="cart-badge" id="cartCount">0</span>
						</a>
					</li>
					<li class="nav-item" id="navLoginItem">
						<a class="nav-link" href="login.jsp">Đăng nhập</a>
					</li>
					<li class="nav-item" id="navRegisterItem">
						<a class="nav-link" href="register.jsp">Đăng ký</a>
					</li>
					<li class="nav-item dropdown" id="navUserMenu" style="display: none;">
						<a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
							<i class="fas fa-user"></i>
							<span id="navUsername"></span>
						</a>
						<ul class="dropdown-menu dropdown-menu-end">
							<li><a class="dropdown-item" href="profile.jsp"><i class="fas fa-user-circle me-1"></i> Hồ sơ</a></li>
							<li><a class="dropdown-item" href="orders.jsp"><i class="fas fa-box me-1"></i> Đơn hàng</a></li>
							<li><a class="dropdown-item" href="wishlist.jsp"><i class="fas fa-heart me-1"></i> Yêu thích</a></li>
							<li><hr class="dropdown-divider"></li>
							<li><a class="dropdown-item" href="#" id="logoutBtn"><i class="fas fa-sign-out-alt me-1"></i> Đăng xuất</a></li>
						</ul>
					</li>
				</ul>
			</div>
		</div>
	</nav>

	<header class="page-header">
		<div class="container position-relative">
			<p class="section-eyebrow mb-2">Bộ lọc thông minh</p>
			<h1 class="section-title mb-3" id="pageTitle">Khám phá danh mục sách</h1>
			<p class="mb-0" id="pageSubtitle">Tìm những cuốn sách phù hợp nhất với nhu cầu của bạn.</p>
		</div>
	</header>

	<main class="py-5">
		<div class="container">
			<div class="row g-4">
				<div class="col-lg-3">
					<div class="filters-card">
						<div class="mb-4">
							<label for="categorySelect" class="filter-title d-block">Danh mục</label>
							<select class="form-select" id="categorySelect"></select>
						</div>

						<div class="filter-separator"></div>

						<div class="mb-4">
							<p class="filter-title">Khoảng giá</p>
							<div class="form-check mb-2">
								<input class="form-check-input" type="radio" name="priceRange" id="priceAll" value="all" checked>
								<label class="form-check-label" for="priceAll">Tất cả mức giá</label>
							</div>
							<div class="form-check mb-2">
								<input class="form-check-input" type="radio" name="priceRange" id="priceUnder100" value="under-100">
								<label class="form-check-label" for="priceUnder100">Dưới 100.000đ</label>
							</div>
							<div class="form-check mb-2">
								<input class="form-check-input" type="radio" name="priceRange" id="price100To200" value="100-200">
								<label class="form-check-label" for="price100To200">100.000đ - 200.000đ</label>
							</div>
							<div class="form-check mb-2">
								<input class="form-check-input" type="radio" name="priceRange" id="price200To400" value="200-400">
								<label class="form-check-label" for="price200To400">200.000đ - 400.000đ</label>
							</div>
							<div class="form-check mb-2">
								<input class="form-check-input" type="radio" name="priceRange" id="price400To700" value="400-700">
								<label class="form-check-label" for="price400To700">400.000đ - 700.000đ</label>
							</div>
							<div class="form-check">
								<input class="form-check-input" type="radio" name="priceRange" id="priceOver700" value="over-700">
								<label class="form-check-label" for="priceOver700">Trên 700.000đ</label>
							</div>
						</div>

						<div class="filter-separator"></div>

						<div class="mb-4">
							<p class="filter-title">Đánh giá tối thiểu</p>
							<div class="btn-group-vertical w-100" role="group" aria-label="Rating filter">
								<button type="button" class="btn btn-outline-secondary text-start rating-filter" data-rating="0">Tất cả</button>
								<button type="button" class="btn btn-outline-secondary text-start rating-filter" data-rating="4">Từ 4 sao trở lên</button>
								<button type="button" class="btn btn-outline-secondary text-start rating-filter" data-rating="3">Từ 3 sao trở lên</button>
								<button type="button" class="btn btn-outline-secondary text-start rating-filter" data-rating="2">Từ 2 sao trở lên</button>
							</div>
						</div>

						<div class="filter-separator"></div>

						<div class="form-check form-switch">
							<input class="form-check-input" type="checkbox" role="switch" id="inStockSwitch">
							<label class="form-check-label" for="inStockSwitch">Chỉ hiển thị sách còn hàng</label>
						</div>

						<div class="mt-4">
							<button class="btn btn-outline-secondary w-100" id="resetFiltersBtn">
								<i class="fas fa-rotate-left me-2"></i>Đặt lại bộ lọc
							</button>
						</div>
					</div>
				</div>

				<div class="col-lg-9">
					<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
						<div>
							<p class="text-muted mb-1" id="resultCount">Đang tải...</p>
							<div id="activeFilters"></div>
						</div>
						<div class="d-flex flex-wrap align-items-center gap-3">
							<div>
								<label for="sortSelect" class="form-label mb-0 small text-muted">Sắp xếp theo</label>
								<select class="form-select" id="sortSelect">
									<option value="created_at">Mới nhất</option>
									<option value="price_asc">Giá tăng dần</option>
									<option value="price_desc">Giá giảm dần</option>
									<option value="rating">Đánh giá cao</option>
									<option value="best_selling">Bán chạy</option>
								</select>
							</div>
							<div>
								<label for="pageSizeSelect" class="form-label mb-0 small text-muted">Hiển thị</label>
								<select class="form-select" id="pageSizeSelect">
									<option value="12">12</option>
									<option value="16">16</option>
									<option value="24">24</option>
								</select>
							</div>
						</div>
					</div>

					<div class="row g-4" id="booksGrid"></div>

					<div class="empty-state text-center d-none" id="emptyState">
						<i class="fas fa-book fa-2x mb-3 text-warning"></i>
						<p class="mb-0">Chúng tôi chưa tìm thấy sách phù hợp với bộ lọc hiện tại.</p>
					</div>

					<div class="mt-4">
						<div class="pagination-controls">
							<button class="btn btn-outline-secondary" id="prevPageBtn"><i class="fas fa-arrow-left me-2"></i>Trang trước</button>
							<div class="text-muted" id="paginationLabel">Trang 1/1</div>
							<button class="btn btn-outline-secondary" id="nextPageBtn">Trang sau<i class="fas fa-arrow-right ms-2"></i></button>
						</div>
					</div>
				</div>
			</div>
		</div>
	</main>

	<footer class="pt-5 pb-4" style="background: #111827; color: rgba(255, 255, 255, 0.78);">
		<div class="container">
			<div class="row g-4">
				<div class="col-lg-4">
					<h5 class="fw-semibold text-white">NK Bookstore</h5>
					<p class="mb-3">Nền tảng mua sách trực tuyến dành cho người yêu sách Việt Nam với trải nghiệm trọn vẹn từ khám phá đến đọc.</p>
					<div class="d-flex gap-3">
						<a href="#" class="text-decoration-none"><i class="fab fa-facebook"></i></a>
						<a href="#" class="text-decoration-none"><i class="fab fa-instagram"></i></a>
						<a href="#" class="text-decoration-none"><i class="fab fa-youtube"></i></a>
					</div>
				</div>
				<div class="col-lg-4 col-md-6">
					<h6 class="text-white">Hỗ trợ khách hàng</h6>
					<ul class="list-unstyled mb-0">
						<li><a href="#">Câu hỏi thường gặp</a></li>
						<li><a href="#">Chính sách vận chuyển</a></li>
						<li><a href="#">Hướng dẫn đổi trả</a></li>
						<li><a href="#">Liên hệ trung tâm hỗ trợ</a></li>
					</ul>
				</div>
				<div class="col-lg-4 col-md-6">
					<h6 class="text-white">Liên hệ</h6>
					<p class="mb-1"><i class="fas fa-location-dot me-2"></i>123 Đường Văn Học, Quận Sách, TP.HCM</p>
					<p class="mb-1"><i class="fas fa-phone me-2"></i><a href="tel:+84901234567">0901 234 567</a></p>
					<p class="mb-0"><i class="fas fa-envelope me-2"></i><a href="mailto:support@nkbookstore.vn">support@nkbookstore.vn</a></p>
				</div>
			</div>
			<hr class="border-secondary my-4">
			<div class="d-flex justify-content-between flex-column flex-md-row">
				<p class="mb-0">&copy; <span id="currentYear"></span> NK Bookstore. Tất cả quyền được bảo lưu.</p>
				<p class="mb-0 small">Phiên bản beta · Đang tiếp tục hoàn thiện trải nghiệm cá nhân hóa.</p>
			</div>
		</div>
	</footer>

	<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
	<script>
		const API_BASE = '<%= request.getContextPath() %>/api';
		const state = {
			category: '',
			sortBy: 'created_at',
			page: 1,
			pageSize: 12,
			minPrice: null,
			maxPrice: null,
			minRating: null,
			inStockOnly: false,
			totalPages: 1,
			totalItems: 0
		};

		const priceRanges = {
			'all': null,
			'under-100': { max: 100000 },
			'100-200': { min: 100000, max: 200000 },
			'200-400': { min: 200000, max: 400000 },
			'400-700': { min: 400000, max: 700000 },
			'over-700': { min: 700000 }
		};

		document.addEventListener('DOMContentLoaded', () => {
			attachEventHandlers();
			loadCategories();
			updateNavbar();
			updateCartCount();
			const yearEl = document.getElementById('currentYear');
			if (yearEl) {
				yearEl.textContent = new Date().getFullYear();
			}
		});

		function attachEventHandlers() {
			const defaultRatingBtn = document.querySelector('.rating-filter[data-rating="0"]');
			if (defaultRatingBtn) {
				defaultRatingBtn.classList.add('active');
			}
			const navSearchForm = document.getElementById('navSearchForm');
			if (navSearchForm) {
				navSearchForm.addEventListener('submit', (event) => {
					event.preventDefault();
					const keyword = document.getElementById('navSearchInput').value.trim();
					if (keyword.length < 2) {
						alert('Vui lòng nhập tối thiểu 2 ký tự để tìm kiếm.');
						return;
					}
					window.location.href = `index.jsp?search=${encodeURIComponent(keyword)}`;
				});
			}

			document.querySelectorAll('input[name="priceRange"]').forEach(radio => {
				radio.addEventListener('change', () => {
					const range = priceRanges[radio.value];
					state.minPrice = range ? range.min ?? null : null;
					state.maxPrice = range ? range.max ?? null : null;
					state.page = 1;
					loadBooks();
					updateActiveFilters();
				});
			});

			document.querySelectorAll('.rating-filter').forEach(button => {
				button.addEventListener('click', () => {
					document.querySelectorAll('.rating-filter').forEach(btn => btn.classList.remove('active'));
					button.classList.add('active');
					const minRating = Number(button.dataset.rating);
					state.minRating = minRating > 0 ? minRating : null;
					state.page = 1;
					loadBooks();
					updateActiveFilters();
				});
			});

			const inStockSwitch = document.getElementById('inStockSwitch');
			if (inStockSwitch) {
				inStockSwitch.addEventListener('change', () => {
					state.inStockOnly = inStockSwitch.checked;
					state.page = 1;
					loadBooks();
					updateActiveFilters();
				});
			}

			const sortSelect = document.getElementById('sortSelect');
			if (sortSelect) {
				sortSelect.addEventListener('change', () => {
					state.sortBy = sortSelect.value;
					state.page = 1;
					loadBooks();
				});
			}

			const pageSizeSelect = document.getElementById('pageSizeSelect');
			if (pageSizeSelect) {
				pageSizeSelect.addEventListener('change', () => {
					state.pageSize = Number(pageSizeSelect.value);
					state.page = 1;
					loadBooks();
				});
			}

			const prevBtn = document.getElementById('prevPageBtn');
			const nextBtn = document.getElementById('nextPageBtn');
			if (prevBtn) {
				prevBtn.addEventListener('click', () => {
					if (state.page > 1) {
						state.page -= 1;
						loadBooks();
					}
				});
			}
			if (nextBtn) {
				nextBtn.addEventListener('click', () => {
					if (state.page < state.totalPages) {
						state.page += 1;
						loadBooks();
					}
				});
			}

			const resetBtn = document.getElementById('resetFiltersBtn');
			if (resetBtn) {
				resetBtn.addEventListener('click', resetFilters);
			}

			const logoutBtn = document.getElementById('logoutBtn');
			if (logoutBtn) {
				logoutBtn.addEventListener('click', (event) => {
					event.preventDefault();
					logout();
				});
			}
		}

		async function loadCategories() {
			try {
				const response = await fetch(`${API_BASE}/books/categories`);
				if (!response.ok) {
					throw new Error('Không thể tải danh mục');
				}
				const categories = await response.json();
				populateCategorySelect(categories);
			} catch (error) {
				console.error('Error loading categories:', error);
				alert('Không thể tải danh mục. Vui lòng thử lại sau.');
			}
		}

		function populateCategorySelect(categories) {
			const select = document.getElementById('categorySelect');
			if (!select) {
				return;
			}
			select.innerHTML = '';

			if (!categories || categories.length === 0) {
				select.innerHTML = '<option>Chưa có danh mục</option>';
				select.disabled = true;
				return;
			}

			const params = new URLSearchParams(window.location.search);
			const requestedCategory = params.get('category');
			categories.forEach(category => {
				const option = document.createElement('option');
				option.value = category;
				option.textContent = category;
				select.appendChild(option);
			});

			state.category = requestedCategory && categories.includes(requestedCategory)
				? requestedCategory
				: categories[0];

			select.value = state.category;
			select.addEventListener('change', () => {
				state.category = select.value;
				state.page = 1;
				updatePageHeader();
				loadBooks();
			});

			updatePageHeader();
			loadBooks();
		}

		function updatePageHeader() {
			const titleEl = document.getElementById('pageTitle');
			const subtitleEl = document.getElementById('pageSubtitle');
			if (titleEl) {
				titleEl.textContent = state.category ? `Sách thuộc danh mục: ${state.category}` : 'Khám phá danh mục sách';
			}
			if (subtitleEl) {
				subtitleEl.textContent = 'Áp dụng bộ lọc để tìm tựa sách phù hợp nhất với nhu cầu của bạn.';
			}
			document.title = state.category ? `${state.category} · NK Bookstore` : 'Danh mục sách · NK Bookstore';
		}

		async function loadBooks() {
			if (!state.category) {
				return;
			}

			const params = new URLSearchParams({
				meta: 'true',
				limit: state.pageSize,
				page: state.page,
				sortBy: state.sortBy
			});

			if (state.minPrice !== null) {
				params.set('minPrice', state.minPrice);
			}
			if (state.maxPrice !== null) {
				params.set('maxPrice', state.maxPrice);
			}
			if (state.minRating !== null) {
				params.set('minRating', state.minRating);
			}
			if (state.inStockOnly) {
				params.set('inStock', 'true');
			}

			const grid = document.getElementById('booksGrid');
			const emptyState = document.getElementById('emptyState');
			const resultCount = document.getElementById('resultCount');
			const paginationLabel = document.getElementById('paginationLabel');
			const prevBtn = document.getElementById('prevPageBtn');
			const nextBtn = document.getElementById('nextPageBtn');

			if (grid) {
				grid.innerHTML = '<div class="col-12 text-center py-5"><div class="spinner-border text-warning" role="status"><span class="visually-hidden">Loading...</span></div></div>';
			}

			try {
				const response = await fetch(`${API_BASE}/books/category/${encodeURIComponent(state.category)}?${params.toString()}`);
				if (!response.ok) {
					throw new Error('Không thể tải sách');
				}
				const data = await response.json();
				const books = Array.isArray(data.items) ? data.items : [];
				const pagination = data.pagination || {};

				state.totalItems = pagination.totalItems || books.length;
				state.totalPages = pagination.totalPages || 1;
				state.page = pagination.page || 1;

				renderBooks(grid, books);
				if (emptyState) {
					emptyState.classList.toggle('d-none', books.length > 0);
				}
				if (resultCount) {
					resultCount.textContent = books.length > 0
						? `Hiển thị ${books.length} / ${state.totalItems} tựa sách`
						: 'Không tìm thấy sách nào với bộ lọc hiện tại.';
				}
				if (paginationLabel) {
					paginationLabel.textContent = `Trang ${state.page}/${state.totalPages || 1}`;
				}
				if (prevBtn) {
					prevBtn.disabled = state.page <= 1;
				}
				if (nextBtn) {
					nextBtn.disabled = state.page >= state.totalPages;
				}
			} catch (error) {
				console.error('Error loading books:', error);
				if (grid) {
					grid.innerHTML = '<div class="col-12"><div class="alert alert-warning">Không thể tải sách. Vui lòng thử lại sau.</div></div>';
				}
			} finally {
				updateActiveFilters();
			}
		}

		function renderBooks(grid, books) {
			if (!grid) {
				return;
			}
			if (!books || books.length === 0) {
				grid.innerHTML = '';
				return;
			}
			const cards = books.map(book => createBookCard(book)).join('');
			grid.innerHTML = cards;
		}

		function createBookCard(book) {
			const price = formatCurrency(book.price);
			const imageUrl = book.imageUrl && book.imageUrl.trim().length > 0
				? book.imageUrl
				: 'https://via.placeholder.com/320x480.png?text=NK+Bookstore';
			const ratingMarkup = renderRating(book.averageRating, book.ratingCount);
			const categoryBadge = book.category ? `<span class="badge text-bg-light text-uppercase">${book.category}</span>` : '';

			return `
				<div class="col-xl-4 col-lg-6 col-md-6">
					<div class="book-card">
						<img src="${imageUrl}" alt="${escapeHtml(book.title)}" class="book-image">
						<div class="book-body">
							${categoryBadge}
							<h3 class="book-title">${escapeHtml(book.title)}</h3>
							<div class="book-meta">${escapeHtml(book.author || 'Đang cập nhật')}</div>
							<div class="book-price">${price}</div>
							<div class="book-meta">${ratingMarkup}</div>
							<div class="book-actions">
								<button type="button" class="btn btn-cart" onclick="addToCart(${Number(book.id)})">
									<i class="fas fa-cart-plus me-2"></i>Thêm vào giỏ
								</button>
								<button type="button" class="btn btn-wishlist" onclick="addToWishlist(${Number(book.id)})" title="Thêm vào yêu thích">
									<i class="far fa-heart"></i>
								</button>
							</div>
						</div>
					</div>
				</div>`;
		}

		function updateActiveFilters() {
			const container = document.getElementById('activeFilters');
			if (!container) {
				return;
			}
			const badges = [];
			if (state.minPrice !== null || state.maxPrice !== null) {
				const min = state.minPrice ? formatCurrency(state.minPrice) : null;
				const max = state.maxPrice ? formatCurrency(state.maxPrice) : null;
				let label = '';
				if (min && max) {
					label = `${min} - ${max}`;
				} else if (min) {
					label = `Từ ${min}`;
				} else if (max) {
					label = `Dưới ${max}`;
				}
				badges.push(createBadge(label, () => {
					document.getElementById('priceAll').checked = true;
					state.minPrice = null;
					state.maxPrice = null;
					state.page = 1;
					loadBooks();
				}));
			}

			if (state.minRating !== null) {
				badges.push(createBadge(`Từ ${state.minRating}★`, () => {
					state.minRating = null;
					document.querySelectorAll('.rating-filter').forEach(btn => btn.classList.remove('active'));
					document.querySelector('.rating-filter[data-rating="0"]').classList.add('active');
					state.page = 1;
					loadBooks();
				}));
			}

			if (state.inStockOnly) {
				badges.push(createBadge('Còn hàng', () => {
					const switchEl = document.getElementById('inStockSwitch');
					if (switchEl) {
						switchEl.checked = false;
					}
					state.inStockOnly = false;
					state.page = 1;
					loadBooks();
				}));
			}

			container.innerHTML = badges.join('');
		}

		function createBadge(label, onRemove) {
			const id = `badge-${Math.random().toString(36).substring(2, 8)}`;
			setTimeout(() => {
				const el = document.getElementById(id);
				if (el) {
					el.addEventListener('click', onRemove);
				}
			}, 0);
			return `<span class="badge-filter"><span>${label}</span><i class="fas fa-xmark" id="${id}"></i></span>`;
		}

		function resetFilters() {
			const priceAll = document.getElementById('priceAll');
			if (priceAll) {
				priceAll.checked = true;
			}
			document.querySelectorAll('.rating-filter').forEach(btn => btn.classList.remove('active'));
			const allRatingBtn = document.querySelector('.rating-filter[data-rating="0"]');
			if (allRatingBtn) {
				allRatingBtn.classList.add('active');
			}
			const inStockSwitch = document.getElementById('inStockSwitch');
			if (inStockSwitch) {
				inStockSwitch.checked = false;
			}
			state.minPrice = null;
			state.maxPrice = null;
			state.minRating = null;
			state.inStockOnly = false;
			state.sortBy = 'created_at';
			state.pageSize = 12;
			state.page = 1;
			const sortSelect = document.getElementById('sortSelect');
			const pageSizeSelect = document.getElementById('pageSizeSelect');
			if (sortSelect) {
				sortSelect.value = 'created_at';
			}
			if (pageSizeSelect) {
				pageSizeSelect.value = '12';
			}
			loadBooks();
			updateActiveFilters();
		}

		function renderRating(averageRating, ratingCount) {
			const rating = Number(averageRating);
			const count = Number(ratingCount);
			if (!Number.isFinite(rating) || rating <= 0) {
				return '<span class="text-muted">Chưa có đánh giá</span>';
			}
			const reviews = Number.isFinite(count) && count > 0 ? count : 0;
			return `<span class="text-warning fw-semibold"><i class="fas fa-star me-1"></i>${rating.toFixed(1)}</span><span class="text-muted"> (${reviews})</span>`;
		}

		function formatCurrency(value) {
			const number = typeof value === 'string' ? Number(value) : value;
			if (!Number.isFinite(number)) {
				return 'Liên hệ';
			}
			return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(number);
		}

		function escapeHtml(value) {
			if (value === null || value === undefined) {
				return '';
			}
			return String(value)
				.replace(/&/g, '&amp;')
				.replace(/</g, '&lt;')
				.replace(/>/g, '&gt;')
				.replace(/"/g, '&quot;')
				.replace(/'/g, '&#039;');
		}

		async function addToCart(bookId) {
			try {
				const token = localStorage.getItem('token');
				if (!token) {
					alert('Vui lòng đăng nhập trước khi thêm sản phẩm vào giỏ hàng.');
					window.location.href = 'login.jsp';
					return;
				}
				const response = await fetch(`${API_BASE}/cart/add`, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						'Authorization': `Bearer ${token}`
					},
					body: JSON.stringify({ bookId, quantity: 1 })
				});
				if (!response.ok) {
					throw new Error('Không thể thêm vào giỏ hàng');
				}
				alert('Đã thêm sách vào giỏ hàng!');
				updateCartCount();
			} catch (error) {
				console.error('Error adding to cart:', error);
				alert('Không thể thêm sách vào giỏ hàng. Vui lòng thử lại.');
			}
		}

		async function addToWishlist(bookId) {
			try {
				const token = localStorage.getItem('token');
				if (!token) {
					alert('Vui lòng đăng nhập để lưu sách vào danh sách yêu thích.');
					window.location.href = 'login.jsp';
					return;
				}
				const response = await fetch(`${API_BASE}/wishlist/add`, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json',
						'Authorization': `Bearer ${token}`
					},
					body: JSON.stringify({ bookId })
				});
				if (!response.ok) {
					throw new Error('Không thể thêm vào yêu thích');
				}
				alert('Đã thêm sách vào danh sách yêu thích!');
			} catch (error) {
				console.error('Error adding to wishlist:', error);
				alert('Không thể thêm vào yêu thích. Vui lòng thử lại.');
			}
		}

		async function updateCartCount() {
			try {
				const token = localStorage.getItem('token');
				if (!token) {
					return;
				}
				const response = await fetch(`${API_BASE}/cart/count`, {
					headers: {
						'Authorization': `Bearer ${token}`
					}
				});
				if (!response.ok) {
					return;
				}
				const data = await response.json();
				const cartCount = document.getElementById('cartCount');
				if (cartCount) {
					cartCount.textContent = data.count || 0;
				}
			} catch (error) {
				console.error('Error updating cart count:', error);
			}
		}

		function updateNavbar() {
			const token = localStorage.getItem('token');
			const storedUsername = localStorage.getItem('username');
			const serverUsername = document.body.dataset.serverUsername;
			const username = storedUsername || serverUsername || '';

			const loginItem = document.getElementById('navLoginItem');
			const registerItem = document.getElementById('navRegisterItem');
			const userMenu = document.getElementById('navUserMenu');
			const usernameSpan = document.getElementById('navUsername');

			if (token && username) {
				if (loginItem) loginItem.style.display = 'none';
				if (registerItem) registerItem.style.display = 'none';
				if (userMenu) userMenu.style.display = 'block';
				if (usernameSpan) usernameSpan.textContent = username;
			} else {
				if (loginItem) loginItem.style.display = 'block';
				if (registerItem) registerItem.style.display = 'block';
				if (userMenu) userMenu.style.display = 'none';
			}
		}

		function logout() {
			localStorage.removeItem('token');
			localStorage.removeItem('username');
			alert('Đăng xuất thành công. Hẹn gặp lại bạn!');
			window.location.reload();
		}
	</script>
</body>
</html>
