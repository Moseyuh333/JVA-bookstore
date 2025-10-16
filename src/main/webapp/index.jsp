<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>NK Bookstore · Cửa hàng sách trực tuyến</title>
	<meta name="description" content="NK Bookstore - mua sách trực tuyến, khám phá sách mới, sách bán chạy và đề xuất cá nhân hóa.">
	<meta name="keywords" content="sách, bookstore, mua sách online, sách mới, sách bán chạy">
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
			--surface-color: #FFFFFF;
			--background-color: #F5F6F8;
		}

		* {
			box-sizing: border-box;
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

		.hero-section {
			background: radial-gradient(120% 120% at 10% 0%, rgba(255, 255, 255, 0.24) 0%, rgba(255, 255, 255, 0) 60%),
						linear-gradient(120deg, rgba(139, 69, 19, 0.92), rgba(92, 47, 16, 0.95));
			color: #fff;
			position: relative;
			overflow: hidden;
		}

		.hero-section::after {
			content: '';
			position: absolute;
			inset: 0;
			background: radial-gradient(80% 60% at 90% 20%, rgba(217, 119, 6, 0.35) 0%, rgba(217, 119, 6, 0) 55%);
			pointer-events: none;
		}

		.hero-kicker {
			text-transform: uppercase;
			letter-spacing: 0.2em;
			font-size: 0.8rem;
			font-weight: 600;
			color: rgba(255, 255, 255, 0.7);
		}

		.hero-title {
			font-size: 3rem;
			font-weight: 700;
			line-height: 1.15;
		}

		.hero-subtitle {
			font-size: 1.15rem;
			color: rgba(255, 255, 255, 0.85);
			max-width: 600px;
		}

		.hero-search .form-control {
			border-radius: 14px;
			border: none;
			padding: 0.9rem 1.2rem;
		}

		.btn-cta {
			background: #FBBF24;
			color: #1F2937;
			font-weight: 600;
			border-radius: 14px;
			padding: 0.9rem 1.5rem;
			border: none;
			transition: transform 0.2s ease, box-shadow 0.2s ease;
		}

		.btn-cta:hover {
			transform: translateY(-1px);
			box-shadow: 0 15px 35px rgba(251, 191, 36, 0.28);
		}

		.hero-metrics {
			display: flex;
			flex-wrap: wrap;
			gap: 1.5rem;
			margin-top: 2.5rem;
		}

		.metric-card {
			background: rgba(255, 255, 255, 0.12);
			border-radius: 18px;
			padding: 1.25rem 1.5rem;
			backdrop-filter: blur(8px);
			min-width: 160px;
		}

		.metric-value {
			font-size: 1.75rem;
			font-weight: 700;
			display: block;
		}

		.metric-label {
			font-size: 0.9rem;
			color: rgba(255, 255, 255, 0.75);
		}

		.hero-shelf {
			background: rgba(255, 255, 255, 0.14);
			border-radius: 18px;
			padding: 1.5rem;
			height: 100%;
		}

		.hero-shelf h5 {
			font-weight: 600;
			margin-bottom: 1rem;
		}

		.hero-shelf ul {
			list-style: none;
			margin: 0;
			padding: 0;
		}

		.hero-shelf li + li {
			margin-top: 0.75rem;
		}

		.hero-shelf .badge {
			background: rgba(31, 41, 55, 0.2);
			border-radius: 999px;
			font-weight: 500;
		}

		.section-eyebrow {
			text-transform: uppercase;
			letter-spacing: 0.18em;
			font-size: 0.78rem;
			font-weight: 600;
			color: var(--accent-color);
			margin-bottom: 0.4rem;
		}

		.section-title {
			font-size: 2rem;
			font-weight: 700;
			margin-bottom: 0;
		}

		.category-section {
			background: #fff;
		}

		.category-rail {
			display: flex;
			gap: 0.75rem;
			overflow-x: auto;
			padding-bottom: 0.5rem;
			scrollbar-width: thin;
		}

		.category-rail::-webkit-scrollbar {
			height: 8px;
		}

		.category-rail::-webkit-scrollbar-thumb {
			background: rgba(139, 69, 19, 0.35);
			border-radius: 999px;
		}

		.category-pill {
			border: 1px solid rgba(139, 69, 19, 0.25);
			background: #fff;
			color: var(--primary-dark);
			padding: 0.55rem 1.4rem;
			border-radius: 999px;
			font-weight: 600;
			white-space: nowrap;
			transition: all 0.2s ease;
		}

		.category-pill:hover,
		.category-pill:focus {
			background: var(--primary-color);
			color: #fff;
			border-color: var(--primary-color);
		}

		.products-section {
			padding: 4rem 0;
		}

		.book-card {
			background: var(--surface-color);
			border-radius: 18px;
			border: 1px solid rgba(15, 23, 42, 0.08);
			overflow: hidden;
			transition: transform 0.24s ease, box-shadow 0.24s ease;
			height: 100%;
			display: flex;
			flex-direction: column;
		}

		.book-card:hover {
			transform: translateY(-6px);
			box-shadow: 0 18px 45px rgba(15, 23, 42, 0.12);
		}

		.book-image {
			width: 100%;
			height: 230px;
			object-fit: cover;
			background: linear-gradient(135deg, rgba(139, 69, 19, 0.1), rgba(217, 119, 6, 0.08));
		}

		.book-body {
			padding: 1.2rem 1.25rem 1.5rem;
			display: flex;
			flex-direction: column;
			gap: 0.65rem;
			flex: 1;
		}

		.book-title {
			font-weight: 600;
			font-size: 1.05rem;
			min-height: 48px;
		}

		.book-meta {
			font-size: 0.9rem;
		}

		.book-price {
			font-size: 1.2rem;
			font-weight: 700;
			color: var(--accent-color);
		}

		.book-actions {
			display: flex;
			gap: 0.5rem;
			margin-top: auto;
		}

		.btn-cart {
			flex: 1;
			border-radius: 12px;
			border: none;
			background: var(--primary-color);
			color: #fff;
			font-weight: 600;
			padding: 0.55rem 1rem;
		}

		.btn-cart:hover {
			background: var(--primary-dark);
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

		.load-more-btn {
			border-radius: 14px;
			padding: 0.65rem 1.8rem;
			font-weight: 600;
			border: 1px solid rgba(139, 69, 19, 0.4);
			color: var(--primary-dark);
			background: #fff;
		}

		.load-more-btn:hover {
			background: var(--primary-color);
			color: #fff;
		}

		.empty-state {
			padding: 2.5rem 1rem;
			border-radius: 16px;
			background: rgba(15, 23, 42, 0.04);
		}

		.value-card {
			background: var(--surface-color);
			border-radius: 18px;
			padding: 1.8rem;
			border: 1px solid rgba(15, 23, 42, 0.08);
			height: 100%;
		}

		.value-card i {
			color: var(--accent-color);
			font-size: 1.6rem;
		}

		.newsletter {
			background: linear-gradient(120deg, rgba(139, 69, 19, 0.9), rgba(92, 47, 16, 0.96));
			color: #fff;
			border-radius: 26px;
		}

		footer {
			background: #111827;
			color: rgba(255, 255, 255, 0.78);
		}

		footer a:hover {
			color: #fff;
		}

		@media (max-width: 991px) {
			.hero-title {
				font-size: 2.4rem;
			}

			.hero-section {
				padding: 3.5rem 0;
			}
		}

		@media (max-width: 575px) {
			.hero-metrics {
				flex-direction: column;
			}

			.book-image {
				height: 200px;
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
						<a class="nav-link" href="category.jsp">Danh mục</a>
					</li>
					<li class="nav-item">
						<a class="nav-link" href="#valueSection">Ưu điểm</a>
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

		<section class="hero-section py-5">
			<div class="container position-relative">
				<div class="row g-4 align-items-center">
					<div class="col-lg-7">
						<p class="hero-kicker mb-2">Phiên bản 2025</p>
						<h1 class="hero-title mb-3">Khám phá thế giới sách được tuyển chọn dành riêng cho bạn</h1>
						<p class="hero-subtitle mb-4">Từ những cuốn bestseller được săn đón đến các tác phẩm kinh điển vượt thời gian, NK Bookstore mang lại trải nghiệm đọc sách trực tuyến toàn diện nhất.</p>
						<form class="hero-search" id="heroSearchForm">
							<div class="row g-2">
								<div class="col-sm-8">
									<input type="text" class="form-control" placeholder="Tìm kiếm theo nhan đề, tác giả hoặc chủ đề" id="heroSearchInput">
								</div>
								<div class="col-sm-4 d-grid d-sm-block">
									<button type="submit" class="btn btn-cta w-100">
										<i class="fas fa-search me-2"></i> Tìm sách ngay
									</button>
								</div>
							</div>
						</form>
						<div class="hero-metrics">
							<div class="metric-card">
								<span class="metric-value">500+</span>
								<span class="metric-label">Đầu sách cập nhật liên tục</span>
							</div>
							<div class="metric-card">
								<span class="metric-value">48h</span>
								<span class="metric-label">Giao hàng toàn quốc</span>
							</div>
							<div class="metric-card">
								<span class="metric-value">4.8/5</span>
								<span class="metric-label">Độc giả đánh giá hài lòng</span>
							</div>
						</div>
					</div>
					<div class="col-lg-5">
						<div class="hero-shelf">
							<h5 class="d-flex justify-content-between align-items-center">
								Chủ đề nổi bật tháng 10
								<span class="badge text-bg-dark fw-semibold">Mới</span>
							</h5>
							<ul>
								<li>
									<strong>Bookclub Spotlight:</strong> Những cuốn sách truyền cảm hứng cho hành trình nghề nghiệp.
								</li>
								<li>
									<strong>Thư viện gia đình:</strong> Gợi ý sách thiếu nhi phát triển tư duy sáng tạo.
								</li>
								<li>
									<strong>Tủ sách kỹ năng:</strong> 10 tựa sách giúp nâng cấp bản thân trước 30 tuổi.
								</li>
							</ul>
						</div>
					</div>
				</div>
			</div>
		</section>

		<section class="category-section py-5" id="categorySection">
			<div class="container">
				<div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
					<div>
						<p class="section-eyebrow mb-1">Khám phá theo chủ đề</p>
						<h2 class="section-title">Danh mục được độc giả yêu thích</h2>
					</div>
					<div class="d-flex align-items-center gap-2">
						<button class="btn btn-sm btn-outline-secondary" type="button" id="refreshCategories">
							<i class="fas fa-rotate-right me-1"></i> Gợi ý khác
						</button>
						<button class="btn btn-sm btn-outline-secondary" type="button" id="viewAllCategories">
							<i class="fas fa-layer-group me-1"></i> Xem tất cả
						</button>
					</div>
				</div>
				<div class="category-rail" id="categoryRail">
					<button class="category-pill" type="button" disabled>Đang tải danh mục...</button>
				</div>
			</div>
		</section>

		<section class="container py-4 d-none" id="discoverySection">
			<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
				<div>
					<p class="section-eyebrow mb-1" id="discoveryEyebrow">Kết quả</p>
					<h2 class="section-title" id="discoveryTitle">Kết quả tìm kiếm</h2>
				</div>
				<div class="d-flex align-items-center gap-2">
					<button class="btn btn-sm btn-outline-secondary" type="button" id="clearDiscovery">
						<i class="fas fa-xmark me-1"></i> Đóng
					</button>
				</div>
			</div>
			<div class="row g-4" id="discoveryResultsGrid"></div>
			<div class="empty-state text-center d-none" id="discoveryEmpty">
				<i class="fas fa-book-open-reader fa-2x mb-3 text-warning"></i>
				<p class="mb-0">Chúng tôi chưa có đề xuất phù hợp. Hãy thử từ khóa khác hoặc chọn danh mục khác.</p>
			</div>
			<div class="text-center mt-4">
				<button class="load-more-btn d-none" data-section="discovery" id="discoveryLoadMore" type="button">
					Tải thêm kết quả
				</button>
			</div>
		</section>

		<section class="products-section">
			<div class="container">
				<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
					<div>
						<p class="section-eyebrow mb-1">Đừng bỏ lỡ</p>
						<h2 class="section-title">Sách mới cập bến</h2>
					</div>
				</div>
				<div class="row g-4" id="newestBooksGrid"></div>
				<div class="empty-state text-center d-none" id="newestEmptyState">
					<i class="fas fa-box-open fa-2x mb-3 text-warning"></i>
					<p class="mb-0">Chưa có sách mới nào được thêm gần đây.</p>
				</div>
				<div class="text-center mt-4">
					<button class="load-more-btn" data-section="newest" type="button">Tải thêm 8 sách</button>
				</div>
			</div>
		</section>

		<section class="products-section bg-white">
			<div class="container">
				<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
					<div>
						<p class="section-eyebrow mb-1">Được săn đón</p>
						<h2 class="section-title">Top sách bán chạy</h2>
					</div>
				</div>
				<div class="row g-4" id="bestSellingBooksGrid"></div>
				<div class="empty-state text-center d-none" id="bestSellingEmptyState">
					<i class="fas fa-store fa-2x mb-3 text-warning"></i>
					<p class="mb-0">Hãy thêm đơn hàng đầu tiên để hình thành bảng xếp hạng bán chạy.</p>
				</div>
				<div class="text-center mt-4">
					<button class="load-more-btn" data-section="bestSelling" type="button">Xem thêm tựa sách</button>
				</div>
			</div>
		</section>

		<section class="products-section">
			<div class="container">
				<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
					<div>
						<p class="section-eyebrow mb-1">Được đánh giá cao</p>
						<h2 class="section-title">Sách được độc giả yêu mến</h2>
					</div>
				</div>
				<div class="row g-4" id="topRatedBooksGrid"></div>
				<div class="empty-state text-center d-none" id="topRatedEmptyState">
					<i class="fas fa-star-half-alt fa-2x mb-3 text-warning"></i>
					<p class="mb-0">Chưa có đánh giá nào. Hãy trở thành người đầu tiên chia sẻ cảm nhận.</p>
				</div>
				<div class="text-center mt-4">
					<button class="load-more-btn" data-section="topRated" type="button">Thêm đề xuất</button>
				</div>
			</div>
		</section>

		<section class="products-section bg-white">
			<div class="container">
				<div class="d-flex flex-wrap justify-content-between align-items-center mb-3">
					<div>
						<p class="section-eyebrow mb-1">Dựa trên wishlist</p>
						<h2 class="section-title">Sách được thêm vào yêu thích nhiều nhất</h2>
					</div>
				</div>
				<div class="row g-4" id="favoriteBooksGrid"></div>
				<div class="empty-state text-center d-none" id="favoriteEmptyState">
					<i class="fas fa-heart fa-2x mb-3 text-warning"></i>
					<p class="mb-0">Bạn hãy thêm sách vào danh sách yêu thích để chúng tôi gợi ý chính xác hơn.</p>
				</div>
				<div class="text-center mt-4">
					<button class="load-more-btn" data-section="favorite" type="button">Khám phá thêm</button>
				</div>
			</div>
		</section>

		<section class="py-5" id="valueSection">
			<div class="container">
				<div class="text-center mb-5">
					<p class="section-eyebrow mb-1">Vì sao chọn chúng tôi</p>
					<h2 class="section-title">Trải nghiệm mua sắm sách hiện đại</h2>
				</div>
				<div class="row g-4">
					<div class="col-lg-4 col-md-6">
						<div class="value-card h-100">
							<i class="fas fa-wand-magic-sparkles mb-3"></i>
							<h5 class="fw-semibold">Đề xuất cá nhân hóa</h5>
							<p class="text-muted mb-0">Thuật toán gợi ý dựa trên lịch sử đọc và danh sách yêu thích giúp bạn tìm được tựa sách phù hợp nhất.</p>
						</div>
					</div>
					<div class="col-lg-4 col-md-6">
						<div class="value-card h-100">
							<i class="fas fa-truck-fast mb-3"></i>
							<h5 class="fw-semibold">Giao hàng nhanh chóng</h5>
							<p class="text-muted mb-0">Mạng lưới vận chuyển toàn quốc đảm bảo đơn hàng luôn đến đúng hẹn trong vòng 48 giờ.</p>
						</div>
					</div>
					<div class="col-lg-4 col-md-6">
						<div class="value-card h-100">
							<i class="fas fa-shield-heart mb-3"></i>
							<h5 class="fw-semibold">An tâm bảo hành sách</h5>
							<p class="text-muted mb-0">Sách được đóng gói cẩn thận, hỗ trợ đổi trả miễn phí nếu sản phẩm không đúng mô tả.</p>
						</div>
					</div>
				</div>
			</div>
		</section>

		<section class="py-5">
			<div class="container">
				<div class="newsletter px-4 px-md-5 py-5">
					<div class="row align-items-center g-4">
						<div class="col-lg-7">
							<p class="section-eyebrow mb-2">Ưu đãi độc quyền</p>
							<h2 class="hero-title fs-2">Tham gia bản tin NK Bookstore</h2>
							<p class="hero-subtitle text-white-50 mb-0">Nhận thông tin phát hành mới, các đợt sale theo mùa và gợi ý đọc chọn lọc mỗi tuần.</p>
						</div>
						<div class="col-lg-5">
							<form class="row g-2" id="newsletterForm">
								<div class="col-12">
									<input type="email" class="form-control" placeholder="Địa chỉ email của bạn" required>
								</div>
								<div class="col-12 d-grid">
									<button type="submit" class="btn btn-cta">Đăng ký ngay</button>
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</section>

		<footer class="pt-5 pb-4">
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
					<p class="mb-0 small">Phiên bản beta · Đang tiếp tục hoàn thiện tính năng thanh toán & trải nghiệm cá nhân hóa.</p>
				</div>
			</div>
		</footer>

		<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
		<script>
			const API_BASE = '<%= request.getContextPath() %>/api';

			const sections = {
				newest: {
					endpoint: 'books/newest',
					limit: 8,
					offset: 0,
					hasMore: true,
					loading: false,
					gridId: 'newestBooksGrid',
					emptyId: 'newestEmptyState'
				},
				bestSelling: {
					endpoint: 'books/best-selling',
					limit: 8,
					offset: 0,
					hasMore: true,
					loading: false,
					gridId: 'bestSellingBooksGrid',
					emptyId: 'bestSellingEmptyState'
				},
				topRated: {
					endpoint: 'books/top-rated',
					limit: 8,
					offset: 0,
					hasMore: true,
					loading: false,
					gridId: 'topRatedBooksGrid',
					emptyId: 'topRatedEmptyState'
				},
				favorite: {
					endpoint: 'books/favorites',
					limit: 8,
					offset: 0,
					hasMore: true,
					loading: false,
					gridId: 'favoriteBooksGrid',
					emptyId: 'favoriteEmptyState'
				}
			};

			const discoveryState = {
				type: null,
				key: '',
				offset: 0,
				limit: 12,
				hasMore: false,
				loading: false
			};

			let categoryCache = [];

			document.addEventListener('DOMContentLoaded', () => {
				attachEventHandlers();
				loadCategories();
				Object.keys(sections).forEach(section => loadSection(section, true));
				updateCartCount();
				updateNavbar();
				const yearEl = document.getElementById('currentYear');
				if (yearEl) {
					yearEl.textContent = new Date().getFullYear();
				}
			});

			function attachEventHandlers() {
				const navSearchForm = document.getElementById('navSearchForm');
				if (navSearchForm) {
					navSearchForm.addEventListener('submit', handleSearchSubmit);
				}

				const heroSearchForm = document.getElementById('heroSearchForm');
				if (heroSearchForm) {
					heroSearchForm.addEventListener('submit', handleSearchSubmit);
				}

				document.querySelectorAll('.load-more-btn').forEach(button => {
					button.addEventListener('click', (event) => {
						const section = event.currentTarget.dataset.section;
						if (section === 'discovery') {
							loadDiscoveryResults(false);
						} else {
							loadSection(section, false);
						}
					});
				});

				const logoutBtn = document.getElementById('logoutBtn');
				if (logoutBtn) {
					logoutBtn.addEventListener('click', (event) => {
						event.preventDefault();
						logout();
					});
				}

				const clearDiscovery = document.getElementById('clearDiscovery');
				if (clearDiscovery) {
					clearDiscovery.addEventListener('click', () => closeDiscovery());
				}

				const refreshCategories = document.getElementById('refreshCategories');
				if (refreshCategories) {
					refreshCategories.addEventListener('click', () => {
						if (categoryCache.length > 0) {
							const shuffled = shuffleArray([...categoryCache]);
							renderCategories(shuffled);
						} else {
							loadCategories();
						}
					});
				}

				const viewAllCategories = document.getElementById('viewAllCategories');
				if (viewAllCategories) {
					viewAllCategories.addEventListener('click', () => {
						const targetCategory = categoryCache.length > 0 ? categoryCache[0] : '';
						const query = targetCategory ? `?category=${encodeURIComponent(targetCategory)}` : '';
						window.location.href = `category.jsp${query}`;
					});
				}

				const newsletterForm = document.getElementById('newsletterForm');
				if (newsletterForm) {
					newsletterForm.addEventListener('submit', (event) => {
						event.preventDefault();
						alert('Cảm ơn bạn đã đăng ký! Chúng tôi sẽ gửi bản tin trong thời gian sớm nhất.');
						newsletterForm.reset();
					});
				}
			}

			function handleSearchSubmit(event) {
				event.preventDefault();
				const input = event.target.querySelector('input[type="search"], input[type="text"]');
				const keyword = input ? input.value : '';
				if (keyword) {
					performSearch(keyword);
				} else {
					alert('Vui lòng nhập từ khóa tối thiểu 2 ký tự để tìm kiếm.');
				}
			}

			async function loadCategories() {
				const rail = document.getElementById('categoryRail');
				if (!rail) {
					return;
				}

				rail.innerHTML = '<button class="category-pill" type="button" disabled>Đang tải danh mục...</button>';
				try {
					const response = await fetch(`${API_BASE}/books/categories`);
					if (!response.ok) {
						throw new Error('Không thể tải danh mục');
					}
					const categories = await response.json();
					categoryCache = Array.isArray(categories) ? categories : [];
					renderCategories(categoryCache);
				} catch (error) {
					console.error('Error loading categories:', error);
					rail.innerHTML = '<button class="category-pill" type="button" disabled>Không tải được danh mục</button>';
				}
			}

			function renderCategories(categories) {
				const rail = document.getElementById('categoryRail');
				if (!rail) {
					return;
				}

				if (!categories || categories.length === 0) {
					rail.innerHTML = '<button class="category-pill" type="button" disabled>Chưa có danh mục</button>';
					return;
				}

				rail.innerHTML = '';
				categories.slice(0, 18).forEach(category => {
					const pill = document.createElement('button');
					pill.type = 'button';
					pill.className = 'category-pill';
					pill.textContent = category;
					pill.addEventListener('click', () => openCategory(category));
					rail.appendChild(pill);
				});
			}

			function shuffleArray(source) {
				for (let i = source.length - 1; i > 0; i--) {
					const j = Math.floor(Math.random() * (i + 1));
					[source[i], source[j]] = [source[j], source[i]];
				}
				return source;
			}

			async function loadSection(sectionKey, reset = false) {
				const config = sections[sectionKey];
				if (!config) {
					return;
				}

				if (config.loading) {
					return;
				}

				if (!config.hasMore && !reset) {
					return;
				}

				const grid = document.getElementById(config.gridId);
				const emptyState = document.getElementById(config.emptyId);
				if (!grid) {
					return;
				}

				const loadingId = `${sectionKey}-loading`;

				if (reset) {
					config.offset = 0;
					config.hasMore = true;
					grid.innerHTML = '';
					if (emptyState) {
						emptyState.classList.add('d-none');
					}
				}

				grid.insertAdjacentHTML('beforeend', createLoadingMarkup(loadingId));
				config.loading = true;

				try {
					const url = `${API_BASE}/${config.endpoint}?limit=${config.limit}&offset=${config.offset}`;
					const response = await fetch(url);
					if (!response.ok) {
						throw new Error(`Không thể tải danh sách sách (${sectionKey})`);
					}
					const books = await response.json();
					removeLoadingMarkup(loadingId);

					if (!Array.isArray(books) || books.length === 0) {
						if (config.offset === 0 && emptyState) {
							emptyState.classList.remove('d-none');
						}
						config.hasMore = false;
						toggleLoadMoreButton(sectionKey, false);
					} else {
						appendBooks(grid, books);
						config.offset += books.length;
						config.hasMore = books.length === config.limit;
						toggleLoadMoreButton(sectionKey, config.hasMore);
					}
				} catch (error) {
					console.error(`Error loading section ${sectionKey}:`, error);
					removeLoadingMarkup(loadingId);
					grid.insertAdjacentHTML('beforeend', `<div class="col-12"><div class="alert alert-warning">Không thể tải dữ liệu. Vui lòng thử lại sau.</div></div>`);
					toggleLoadMoreButton(sectionKey, false);
				} finally {
					config.loading = false;
				}
			}

			function createLoadingMarkup(id) {
				return `<div class="col-12 text-center" id="${id}">
						<div class="spinner-border text-warning" role="status">
							<span class="visually-hidden">Loading</span>
						</div>
					</div>`;
			}

			function removeLoadingMarkup(id) {
				const loader = document.getElementById(id);
				if (loader) {
					loader.remove();
				}
			}

			function appendBooks(grid, books) {
				const cards = books.map(book => createBookCard(book)).join('');
				grid.insertAdjacentHTML('beforeend', cards);
			}

			function createBookCard(book) {
				const price = formatCurrency(book.price);
				const imageUrl = book.imageUrl && book.imageUrl.trim().length > 0
					? book.imageUrl
					: 'https://via.placeholder.com/320x480.png?text=NK+Bookstore';
				const ratingMarkup = renderRating(book.averageRating, book.ratingCount);
				const categoryBadge = book.category ? `<span class="badge text-bg-light text-uppercase">${book.category}</span>` : '';

				return `
					<div class="col-xl-3 col-lg-4 col-sm-6">
						<div class="book-card">
							<img src="${imageUrl}" alt="${escapeHtml(book.title)}" class="book-image">
							<div class="book-body">
								${categoryBadge}
								<h3 class="book-title">${escapeHtml(book.title)}</h3>
								<div class="book-meta text-muted">${escapeHtml(book.author || 'Đang cập nhật')}</div>
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

			function renderRating(averageRating, ratingCount) {
				const rating = Number(averageRating);
				const count = Number(ratingCount);
				if (!Number.isFinite(rating) || rating <= 0) {
					return '<span class="text-muted">Chưa có đánh giá</span>';
				}
				const reviews = Number.isFinite(count) && count > 0 ? count : 0;
				return `<span class="text-warning fw-semibold"><i class="fas fa-star me-1"></i>${rating.toFixed(1)}</span>
						<span class="text-muted">(${reviews})</span>`;
			}

			function toggleLoadMoreButton(sectionKey, visible) {
				const button = document.querySelector(`.load-more-btn[data-section="${sectionKey}"]`);
				if (button) {
					button.classList.toggle('d-none', !visible);
				}
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

			function openCategory(category) {
				if (!category) {
					return;
				}
				discoveryState.type = 'category';
				discoveryState.key = category;
				loadDiscoveryResults(true);
			}

			function performSearch(keyword) {
				const trimmed = keyword.trim();
				if (trimmed.length < 2) {
					alert('Vui lòng nhập tối thiểu 2 ký tự để tìm kiếm.');
					return;
				}
				discoveryState.type = 'search';
				discoveryState.key = trimmed;
				loadDiscoveryResults(true);
				const navInput = document.getElementById('navSearchInput');
				if (navInput) {
					navInput.value = trimmed;
				}
				const heroInput = document.getElementById('heroSearchInput');
				if (heroInput) {
					heroInput.value = trimmed;
				}
			}

			async function loadDiscoveryResults(reset = false) {
				const section = document.getElementById('discoverySection');
				const grid = document.getElementById('discoveryResultsGrid');
				const emptyState = document.getElementById('discoveryEmpty');
				const button = document.getElementById('discoveryLoadMore');
				const title = document.getElementById('discoveryTitle');
				const eyebrow = document.getElementById('discoveryEyebrow');

				if (!section || !grid || !discoveryState.type) {
					return;
				}

				if (discoveryState.loading) {
					return;
				}

				if (!discoveryState.hasMore && !reset) {
					return;
				}

				if (reset) {
					discoveryState.offset = 0;
					discoveryState.hasMore = true;
					section.classList.remove('d-none');
					grid.innerHTML = '';
					if (emptyState) {
						emptyState.classList.add('d-none');
					}
				}

				const loadingId = 'discovery-loading';
				grid.insertAdjacentHTML('beforeend', createLoadingMarkup(loadingId));
				discoveryState.loading = true;

				try {
					const encodedKey = encodeURIComponent(discoveryState.key);
					const endpoint = discoveryState.type === 'search'
						? `books/search/${encodedKey}`
						: `books/category/${encodedKey}`;
					const url = `${API_BASE}/${endpoint}?limit=${discoveryState.limit}&offset=${discoveryState.offset}`;
					const response = await fetch(url);
					if (!response.ok) {
						throw new Error('Không thể tải kết quả tìm kiếm');
					}
					const books = await response.json();
					removeLoadingMarkup(loadingId);

					if (reset) {
						if (discoveryState.type === 'search') {
							eyebrow.textContent = 'Kết quả tìm kiếm';
							title.textContent = `"${discoveryState.key}"`;
						} else {
							eyebrow.textContent = 'Bộ sưu tập theo danh mục';
							title.textContent = discoveryState.key;
						}
					}

					if (!Array.isArray(books) || books.length === 0) {
						if (discoveryState.offset === 0 && emptyState) {
							emptyState.classList.remove('d-none');
						}
						discoveryState.hasMore = false;
						if (button) {
							button.classList.add('d-none');
						}
					} else {
						appendBooks(grid, books);
						discoveryState.offset += books.length;
						discoveryState.hasMore = books.length === discoveryState.limit;
						if (button) {
							button.classList.toggle('d-none', !discoveryState.hasMore);
						}
					}
				} catch (error) {
					console.error('Error loading discovery results:', error);
					removeLoadingMarkup(loadingId);
					grid.insertAdjacentHTML('beforeend', `<div class="col-12"><div class="alert alert-warning">Không thể tải kết quả. Bạn vui lòng thử lại sau.</div></div>`);
					if (button) {
						button.classList.add('d-none');
					}
					discoveryState.hasMore = false;
				} finally {
					discoveryState.loading = false;
				}
			}

			function closeDiscovery() {
				const section = document.getElementById('discoverySection');
				if (section) {
					section.classList.add('d-none');
				}
				discoveryState.type = null;
				discoveryState.key = '';
				discoveryState.offset = 0;
				discoveryState.hasMore = false;
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
					if (loginItem) {
						loginItem.style.display = 'none';
					}
					if (registerItem) {
						registerItem.style.display = 'none';
					}
					if (userMenu) {
						userMenu.style.display = 'block';
					}
					if (usernameSpan) {
						usernameSpan.textContent = username;
					}
				} else {
					if (loginItem) {
						loginItem.style.display = 'block';
					}
					if (registerItem) {
						registerItem.style.display = 'block';
					}
					if (userMenu) {
						userMenu.style.display = 'none';
					}
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
