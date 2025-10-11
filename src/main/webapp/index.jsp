<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<section class="home-hero">
    <div class="container">
        <div class="row g-4 align-items-stretch">
            <div class="col-lg-3 d-none d-lg-block">
                <aside class="category-panel">
                    <h5>Danh mục nổi bật</h5>
                    <ul>
                        <li><a href="#"><i class="fas fa-fire"></i> Sách bán chạy</a></li>
                        <li><a href="#"><i class="fas fa-book"></i> Văn học Việt Nam</a></li>
                        <li><a href="#"><i class="fas fa-globe-americas"></i> Văn học nước ngoài</a></li>
                        <li><a href="#"><i class="fas fa-brain"></i> Kỹ năng sống</a></li>
                        <li><a href="#"><i class="fas fa-rocket"></i> Khám phá - STEM</a></li>
                        <li><a href="#"><i class="fas fa-child"></i> Sách thiếu nhi</a></li>
                        <li><a href="#"><i class="fas fa-graduation-cap"></i> Giáo khoa - tham khảo</a></li>
                        <li><a href="#"><i class="fas fa-percent"></i> Ưu đãi đang diễn ra</a></li>
                    </ul>
                </aside>
            </div>
            <div class="col-lg-6 col-12">
                <div id="heroCarousel" class="carousel slide hero-carousel" data-bs-ride="carousel">
                    <div class="carousel-indicators">
                        <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"></button>
                        <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="1" aria-label="Slide 2"></button>
                        <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="2" aria-label="Slide 3"></button>
                    </div>
                    <div class="carousel-inner">
                        <div class="carousel-item active">
                            <div class="hero-slide slide-one">
                                <div class="slide-content">
                                    <span class="badge">Sách mới</span>
                                    <h2>Bộ sưu tập hè 2024</h2>
                                    <p>Chọn lọc những tựa sách truyền cảm hứng cho hành trình học hỏi và thư giãn của bạn.</p>
                                    <a href="#" class="btn btn-light btn-lg">Khám phá ngay</a>
                                </div>
                            </div>
                        </div>
                        <div class="carousel-item">
                            <div class="hero-slide slide-two">
                                <div class="slide-content">
                                    <span class="badge">Combo ưu đãi</span>
                                    <h2>Giảm đến 45% - Chỉ cuối tuần</h2>
                                    <p>Ưu tiên cho các combo kỹ năng và sách ngoại ngữ. Số lượng quà tặng giới hạn.</p>
                                    <a href="#" class="btn btn-outline-light btn-lg">Đặt mua ngay</a>
                                </div>
                            </div>
                        </div>
                        <div class="carousel-item">
                            <div class="hero-slide slide-three">
                                <div class="slide-content">
                                    <span class="badge">Thành viên</span>
                                    <h2>Góc đọc giả thân thiết</h2>
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
            <div class="col-lg-3 d-none d-lg-flex flex-column">
                <div class="hero-side-card highlight">
                    <span class="badge">-15%</span>
                    <h5>Gói học tập mới</h5>
                    <p>Bộ sách luyện thi THPT Quốc gia 2024 kèm flashcard và đề mẫu.</p>
                    <a href="#" class="btn btn-link">Xem chi tiết</a>
                </div>
                <div class="hero-side-card">
                    <h5>Miễn phí vận chuyển</h5>
                    <p>Áp dụng cho đơn từ 299.000đ tại HCM &amp; Hà Nội - giao nhanh 2h.</p>
                    <div class="bg-soft mt-auto">
                        <i class="fas fa-phone-volume me-2"></i>Hotline: 1900 9999
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="section-padding service-strip">
    <div class="container">
        <div class="row g-3">
            <div class="col-sm-6 col-lg-3">
                <div class="service-card">
                    <div class="service-icon"><i class="fas fa-truck"></i></div>
                    <div>
                        <h6>Giao nhanh toàn quốc</h6>
                        <span>Miễn phí từ 299.000đ</span>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="service-card">
                    <div class="service-icon"><i class="fas fa-shield-heart"></i></div>
                    <div>
                        <h6>Đổi trả 7 ngày</h6>
                        <span>Hoàn tiền 100% nếu sách lỗi</span>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="service-card">
                    <div class="service-icon"><i class="fas fa-gift"></i></div>
                    <div>
                        <h6>Quà tặng hấp dẫn</h6>
                        <span>Tích điểm - voucher mỗi tuần</span>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-lg-3">
                <div class="service-card">
                    <div class="service-icon"><i class="fas fa-credit-card"></i></div>
                    <div>
                        <h6>Thanh toán linh hoạt</h6>
                        <span>Momo, ZaloPay, chuyển khoản</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="product-section" id="best-seller">
    <div class="container">
        <div class="section-heading">
            <h2>Sách bán chạy trong tuần</h2>
            <a href="#"><span>Xem tất cả</span> <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row g-4 product-grid">
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-hot">Hot</span>
                    <div class="product-cover gradient-1">
                        <span>Bản in mới</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Văn học</div>
                        <h5>Nhà Giả Kim</h5>
                        <p>Hành trình tìm kiếm kho báu và khám phá bản ngã đầy cảm hứng.</p>
                        <div class="price">
                            <span>129.000đ</span>
                            <del>169.000đ</del>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-hot">Hot</span>
                    <div class="product-cover gradient-2">
                        <span>Top tháng 6</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Thiếu nhi</div>
                        <h5>Dế Mèn Phiêu Lưu Ký</h5>
                        <p>Tựa sách tuổi thơ với minh họa mới, tăng cường kỹ năng đọc hiểu.</p>
                        <div class="price">
                            <span>85.000đ</span>
                            <del>105.000đ</del>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-hot">Combo</span>
                    <div class="product-cover gradient-3">
                        <span>Tiết kiệm 30%</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Kỹ năng</div>
                        <h5>Tư Duy Nhanh &amp; Chậm</h5>
                        <p>Bản dịch mới nhất kèm sổ tay ghi chú và bookmark giới hạn.</p>
                        <div class="price">
                            <span>199.000đ</span>
                            <del>259.000đ</del>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-hot">Mới</span>
                    <div class="product-cover gradient-4">
                        <span>Bản đặc biệt</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Tri thức</div>
                        <h5>Lược Sử Thời Gian</h5>
                        <p>Bản bìa cứng cập nhật, tặng kèm poster sơ đồ vũ trụ.</p>
                        <div class="price">
                            <span>245.000đ</span>
                            <del>295.000đ</del>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="product-section bg-light" id="new-arrivals">
    <div class="container">
        <div class="section-heading">
            <h2>Sách mới cập nhật</h2>
            <a href="#"><span>Khám phá thêm</span> <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row g-4 product-grid">
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-new">New</span>
                    <div class="product-cover gradient-2">
                        <span>Ra mắt</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Văn học</div>
                        <h5>Chuyện Nghìn Lẻ Một Đêm</h5>
                        <p>Bản dịch mới với tranh minh họa màu tuyệt đẹp.</p>
                        <div class="price">
                            <span>175.000đ</span>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-new">New</span>
                    <div class="product-cover gradient-3">
                        <span>Độc quyền</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Self-help</div>
                        <h5>Sống Tối Giản</h5>
                        <p>Những bí quyết sắp xếp không gian và cân bằng cuộc sống.</p>
                        <div class="price">
                            <span>142.000đ</span>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-new">New</span>
                    <div class="product-cover gradient-1">
                        <span>Tặng kèm</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Thiếu nhi</div>
                        <h5>Khám Phá Vũ Trụ</h5>
                        <p>Bộ sticker 3D về các hành tinh dành cho bé từ 6+</p>
                        <div class="price">
                            <span>119.000đ</span>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
            <div class="col-sm-6 col-md-4 col-lg-3">
                <div class="product-card">
                    <span class="badge badge-new">New</span>
                    <div class="product-cover gradient-4">
                        <span>Tái bản</span>
                    </div>
                    <div class="product-info">
                        <div class="category">Kinh tế</div>
                        <h5>Phi Lý Trí</h5>
                        <p>Phân tích thói quen tiêu dùng với ví dụ thực tế sinh động.</p>
                        <div class="price">
                            <span>189.000đ</span>
                        </div>
                    </div>
                    <div class="product-actions">
                        <button class="btn btn-primary btn-sm"><i class="fas fa-cart-plus me-2"></i>Thêm</button>
                        <button class="btn btn-outline-primary btn-sm">Chi tiết</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="section-padding" id="promo">
    <div class="container">
        <div class="promo-banner">
            <div class="promo-content">
                <span class="badge badge-new">Flash Sale cuối tháng</span>
                <h3>Giảm thêm 10% cho đơn sách thiếu nhi</h3>
                <p>Nhập mã <strong>KIDBOOK10</strong> khi thanh toán. Áp dụng cho 500 đơn đầu tiên, đừng bỏ lỡ nhé!</p>
                <div class="promo-timer" id="promo-timer">
                    <div class="time-block">
                        <strong id="timer-days">02</strong>
                        <span>Ngày</span>
                    </div>
                    <div class="time-block">
                        <strong id="timer-hours">04</strong>
                        <span>Giờ</span>
                    </div>
                    <div class="time-block">
                        <strong id="timer-minutes">18</strong>
                        <span>Phút</span>
                    </div>
                    <div class="time-block">
                        <strong id="timer-seconds">42</strong>
                        <span>Giây</span>
                    </div>
                </div>
                <a href="#" class="btn btn-light btn-lg mt-2">Đặt mua ngay</a>
            </div>
        </div>
    </div>
</section>

<section class="news-section">
    <div class="container">
        <div class="section-heading">
            <h2>Tin tức &amp; góc đọc giả</h2>
            <a href="#"><span>Xem blog</span> <i class="fas fa-arrow-right"></i></a>
        </div>
        <div class="row g-4 news-grid">
            <div class="col-md-4">
                <article class="news-card">
                    <div class="news-cover">
                        <span>30.05.2024</span>
                        <h6>Workshop: Đọc sách cùng con</h6>
                    </div>
                    <div class="news-body">
                        <p>Bí quyết tạo thói quen đọc sách cho trẻ với chuyên gia giáo dục nổi tiếng.</p>
                        <a href="#">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </article>
            </div>
            <div class="col-md-4">
                <article class="news-card">
                    <div class="news-cover">
                        <span>26.05.2024</span>
                        <h6>5 tựa sách kinh doanh nên đọc</h6>
                    </div>
                    <div class="news-body">
                        <p>Cập nhật xu hướng quản trị 2024 cùng những case study đáng học hỏi.</p>
                        <a href="#">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </article>
            </div>
            <div class="col-md-4">
                <article class="news-card">
                    <div class="news-cover">
                        <span>20.05.2024</span>
                        <h6>Một ngày ở Góc Xếp Bookstore</h6>
                    </div>
                    <div class="news-body">
                        <p>Trải nghiệm không gian đọc sách, cà phê và góc sáng tạo dành cho bạn.</p>
                        <a href="#">Đọc tiếp <i class="fas fa-arrow-right"></i></a>
                    </div>
                </article>
            </div>
        </div>
    </div>
</section>

<script>
const flashSaleEnd = Date.now() + (2 * 24 * 60 * 60 * 1000); // 2 days countdown

function updatePromoTimer() {
    const now = Date.now();
    const distance = flashSaleEnd - now;

    if (distance <= 0) {
        document.getElementById("promo-timer").innerHTML = "<strong>Chương trình đã kết thúc</strong>";
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
