<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<section class="py-5 home-hero">
    <div class="container">
        <div class="row g-4 align-items-stretch">
            <div class="col-xl-3 d-none d-xl-block">
                <div class="card category-panel shadow-sm border-0 h-100">
                    <div class="card-body">
                        <p class="text-uppercase text-muted small fw-semibold mb-3">Danh mục nổi bật</p>
                        <div class="d-grid gap-2">
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-fire"></i><span>Sách bán chạy</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-book-open"></i><span>Văn học Việt Nam</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-globe-americas"></i><span>Văn học nước ngoài</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-lightbulb"></i><span>Kỹ năng sống</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-rocket"></i><span>Khám phá - STEM</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-child"></i><span>Sách thiếu nhi</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-graduation-cap"></i><span>Giáo khoa - tham khảo</span>
                            </a>
                            <a href="#" class="btn btn-outline-primary btn-sm text-start d-flex align-items-center gap-2">
                                <i class="fas fa-percent"></i><span>Ưu đãi đang diễn ra</span>
                            </a>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-xl-6 col-lg-7">
                <div id="heroCarousel" class="carousel slide" data-bs-ride="carousel">
                    <div class="carousel-indicators">
                        <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"></button>
                        <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="1" aria-label="Slide 2"></button>
                        <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="2" aria-label="Slide 3"></button>
                    </div>
                    <div class="carousel-inner shadow-sm">
                        <div class="carousel-item active" data-bs-interval="6000">
                            <div class="card border-0 text-white hero-slide" style="background: linear-gradient(135deg, #f25f2a 0%, #f6a93a 100%);">
                                <div class="card-body p-5">
                                    <span class="badge text-white">Sách mới</span>
                                    <h2 class="fw-bold display-6 mb-2">Bộ sưu tập hè 2024</h2>
                                    <p>Chọn lọc những tựa sách truyền cảm hứng cho hành trình học hỏi và thư giãn của bạn.</p>
                                    <a href="#" class="btn btn-light btn-lg">Khám phá ngay</a>
                                </div>
                            </div>
                        </div>
                        <div class="carousel-item" data-bs-interval="6000">
                            <div class="card border-0 text-white hero-slide" style="background: linear-gradient(135deg, #1b3151 0%, #314e78 100%);">
                                <div class="card-body p-5">
                                    <span class="badge text-white">Combo ưu đãi</span>
                                    <h2 class="fw-bold display-6 mb-2">Giảm đến 45% - Chỉ cuối tuần</h2>
                                    <p>Ưu tiên cho các combo kỹ năng và sách ngoại ngữ. Số lượng quà tặng giới hạn.</p>
                                    <a href="#" class="btn btn-outline-light btn-lg">Đặt mua ngay</a>
                                </div>
                            </div>
                        </div>
                        <div class="carousel-item" data-bs-interval="6000">
                            <div class="card border-0 text-white hero-slide" style="background: linear-gradient(135deg, #f8b400 0%, #f1592a 100%);">
                                <div class="card-body p-5">
                                    <span class="badge text-white">Thành viên</span>
                                    <h2 class="fw-bold display-6 mb-2">Góc đọc giả thân thiết</h2>
                                    <p>Tích điểm nhanh hơn, nhận voucher độc quyền và ưu đãi sinh nhật hấp dẫn.</p>
                                    <a href="register.jsp" class="btn btn-light btn-lg">Tham gia hội viên</a>
                                </div>
                            </div>
                        </div>
                    </div>
                    <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Previous</span>
                    </button>
                    <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                        <span class="visually-hidden">Next</span>
                    </button>
                </div>
            </div>
            <div class="col-xl-3 d-none d-xl-flex flex-column gap-3">
                <div class="card border-0 shadow-sm flex-grow-1">
                    <div class="card-body">
                        <span class="badge bg-warning-subtle text-warning fw-semibold mb-2">-15%</span>
                        <h5 class="fw-bold text-secondary">Gói học tập mới</h5>
                        <p class="text-muted mb-3">Bộ sách luyện thi THPT Quốc gia 2024 kèm flashcard và đề mẫu.</p>
                        <a href="#" class="btn btn-link text-decoration-none fw-semibold text-primary px-0">Xem chi tiết</a>
                    </div>
                </div>
                <div class="card border-0 shadow-sm flex-grow-1">
                    <div class="card-body d-flex flex-column gap-3">
                        <div>
                            <h5 class="fw-bold text-secondary mb-1">Miễn phí vận chuyển</h5>
                            <p class="text-muted mb-0">Áp dụng cho đơn từ 299.000đ tại HCM &amp; Hà Nội - giao nhanh 2h.</p>
                        </div>
                        <div class="bg-light rounded-3 px-3 py-2 text-secondary fw-semibold">
                            <i class="fas fa-phone-volume me-2"></i>Hotline: 1900 9999
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5 bg-white">
    <div class="container">
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-3">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-primary-subtle text-primary p-3 fs-5"><i class="fas fa-truck"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Giao nhanh toàn quốc</h6>
                            <p class="mb-0 small">Miễn phí từ 299.000đ</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-success-subtle text-success p-3 fs-5"><i class="fas fa-shield-heart"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Đổi trả 7 ngày</h6>
                            <p class="mb-0 small">Hoàn tiền 100% nếu sách lỗi</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-warning-subtle text-warning p-3 fs-5"><i class="fas fa-gift"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Quà tặng hấp dẫn</h6>
                            <p class="mb-0 small">Tích điểm - voucher mỗi tuần</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 service-card">
                    <div class="card-body d-flex align-items-center gap-3">
                        <span class="badge bg-info-subtle text-info p-3 fs-5"><i class="fas fa-credit-card"></i></span>
                        <div>
                            <h6 class="fw-semibold mb-1">Thanh toán linh hoạt</h6>
                            <p class="mb-0 small">Momo, ZaloPay, chuyển khoản</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5" id="best-seller">
    <div class="container">
        <div class="section-heading">
            <h2>Sách bán chạy trong tuần</h2>
            <a href="#">Xem tất cả <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-4">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Hot</span>
                            <small class="text-muted">Bản in mới</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Nhà Giả Kim</h5>
                        <p class="small flex-grow-1">Hành trình tìm kiếm kho báu và khám phá bản ngã đầy cảm hứng.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>129.000đ</span>
                            <del>169.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Hot</span>
                            <small class="text-muted">Top tháng 6</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Dế Mèn Phiêu Lưu Ký</h5>
                        <p class="small flex-grow-1">Tựa sách tuổi thơ với minh họa mới, tăng cường kỹ năng đọc hiểu.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>85.000đ</span>
                            <del>105.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Combo</span>
                            <small class="text-muted">Tiết kiệm 30%</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Tư Duy Nhanh &amp; Chậm</h5>
                        <p class="small flex-grow-1">Bản dịch mới nhất kèm sổ tay ghi chú và bookmark giới hạn.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>199.000đ</span>
                            <del>259.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-danger-subtle text-danger fw-semibold">Mới</span>
                            <small class="text-muted">Bản đặc biệt</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Lược Sử Thời Gian</h5>
                        <p class="small flex-grow-1">Bản bìa cứng cập nhật, tặng kèm poster sơ đồ vũ trụ.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>245.000đ</span>
                            <del>295.000đ</del>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5 bg-white" id="new-arrivals">
    <div class="container">
        <div class="section-heading">
            <h2>Sách mới cập nhật</h2>
            <a href="#">Khám phá thêm <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row row-cols-1 row-cols-sm-2 row-cols-lg-4 g-4">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Ra mắt</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Chuyện Nghìn Lẻ Một Đêm</h5>
                        <p class="small flex-grow-1">Bản dịch mới với tranh minh họa màu tuyệt đẹp.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>175.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Độc quyền</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Sống Tối Giản</h5>
                        <p class="small flex-grow-1">Những bí quyết sắp xếp không gian và cân bằng cuộc sống.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>142.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Tặng kèm</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Khám Phá Vũ Trụ</h5>
                        <p class="small flex-grow-1">Bộ sticker 3D về các hành tinh dành cho bé từ 6+</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>119.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 product-highlight">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-primary-subtle text-primary fw-semibold">New</span>
                            <small class="text-muted">Tái bản</small>
                        </div>
                        <h5 class="fw-semibold text-secondary mb-1">Phi Lý Trí</h5>
                        <p class="small flex-grow-1">Phân tích thói quen tiêu dùng với ví dụ thực tế sinh động.</p>
                        <div class="price d-flex align-items-baseline gap-2">
                            <span>189.000đ</span>
                        </div>
                    </div>
                    <div class="card-footer border-0">
                        <div class="d-flex gap-2">
                            <button class="btn btn-primary flex-grow-1"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                            <button class="btn btn-outline-primary flex-grow-1">Chi tiết</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5" id="promo">
    <div class="container">
        <div class="card border-0 text-white promo-card shadow-lg" style="background: linear-gradient(135deg, #172742 0%, #1b3151 45%, #f1592a 100%);">
            <div class="card-body p-4 p-lg-5">
                <div class="row gy-4 align-items-center">
                    <div class="col-lg-6">
                        <span class="badge bg-light text-primary fw-semibold mb-3">Flash Sale cuối tháng</span>
                        <h3 class="fw-bold display-6">Giảm thêm 10% cho đơn sách thiếu nhi</h3>
                        <p class="mb-0">Nhập mã <strong>KIDBOOK10</strong> khi thanh toán. Áp dụng cho 500 đơn đầu tiên, đừng bỏ lỡ nhé!</p>
                    </div>
                    <div class="col-lg-4">
                        <div class="promo-countdown justify-content-center justify-content-lg-start">
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-days">02</div>
                                <div class="countdown-label">Ngày</div>
                            </div>
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-hours">04</div>
                                <div class="countdown-label">Giờ</div>
                            </div>
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-minutes">18</div>
                                <div class="countdown-label">Phút</div>
                            </div>
                            <div class="countdown-box">
                                <div class="countdown-value" id="timer-seconds">42</div>
                                <div class="countdown-label">Giây</div>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-2 text-lg-end text-center">
                        <a href="#" class="btn btn-light btn-lg px-4 fw-semibold">Đặt mua ngay</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5 bg-white">
    <div class="container">
        <div class="section-heading">
            <h2>Tin tức &amp; góc đọc giả</h2>
            <a href="#">Xem blog <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row row-cols-1 row-cols-md-3 g-4">
            <div class="col">
                <div class="card border-0 shadow-sm h-100 position-relative news-card">
                    <div class="card-header">30.05.2024</div>
                    <div class="card-body">
                        <h5 class="card-title fw-semibold text-secondary">Workshop: Đọc sách cùng con</h5>
                        <p>Bí quyết tạo thói quen đọc sách cho trẻ với chuyên gia giáo dục nổi tiếng.</p>
                        <a href="#" class="stretched-link">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 position-relative news-card">
                    <div class="card-header">26.05.2024</div>
                    <div class="card-body">
                        <h5 class="card-title fw-semibold text-secondary">5 tựa sách kinh doanh nên đọc</h5>
                        <p>Cập nhật xu hướng quản trị 2024 cùng những case study đáng học hỏi.</p>
                        <a href="#" class="stretched-link">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
            <div class="col">
                <div class="card border-0 shadow-sm h-100 position-relative news-card">
                    <div class="card-header">20.05.2024</div>
                    <div class="card-body">
                        <h5 class="card-title fw-semibold text-secondary">Một ngày ở Góc Xếp Bookstore</h5>
                        <p>Trải nghiệm không gian đọc sách, cà phê và góc sáng tạo dành cho bạn.</p>
                        <a href="#" class="stretched-link">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<script>
const flashSaleEnd = Date.now() + (2 * 24 * 60 * 60 * 1000);

function updatePromoTimer() {
    const now = Date.now();
    const distance = flashSaleEnd - now;

    if (distance <= 0) {
        document.getElementById("timer-days").textContent = "00";
        document.getElementById("timer-hours").textContent = "00";
        document.getElementById("timer-minutes").textContent = "00";
        document.getElementById("timer-seconds").textContent = "00";
        return;
    }

    const days = Math.floor(distance / (1000 * 60 * 60 * 24));
    const hours = Math.floor((distance % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
    const minutes = Math.floor((distance % (1000 * 60 * 60)) / (1000 * 60));
    const seconds = Math.floor((distance % (1000 * 60)) / 1000);

    document.getElementById("timer-days").textContent = String(days).padStart(2, "0");
    document.getElementById("timer-hours").textContent = String(hours).padStart(2, "0");
    document.getElementById("timer-minutes").textContent = String(minutes).padStart(2, "0");
    document.getElementById("timer-seconds").textContent = String(seconds).padStart(2, "0");
}

updatePromoTimer();
setInterval(updatePromoTimer, 1000);
</script>
