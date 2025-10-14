<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><decorator:title default="Góc Xếp Bookstore"/></title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
    <decorator:head/>
    <style>
        /* Minimal fallback if asset not served */
        body { background-color: #f5f6fb; }
    </style>
    
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-light bg-white shadow-sm sticky-top">
        <div class="container py-2">
            <a class="navbar-brand d-flex align-items-center gap-2 fw-bold text-uppercase text-primary" href="${pageContext.request.contextPath}/">
                <i class="fas fa-layer-group"></i> Góc Xếp Bookstore
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav" aria-controls="nav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="nav">
                <ul class="navbar-nav me-auto align-items-lg-center">
                    <li class="nav-item"><a class="nav-link fw-semibold" href="${pageContext.request.contextPath}/">Trang chủ</a></li>
                    <li class="nav-item"><a class="nav-link" href="#best-seller">Sách bán chạy</a></li>
                    <li class="nav-item"><a class="nav-link" href="#new-arrivals">Sách mới</a></li>
                    <li class="nav-item"><a class="nav-link" href="#promo">Ưu đãi</a></li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="moreDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            Danh mục
                        </a>
                        <ul class="dropdown-menu" aria-labelledby="moreDropdown">
                            <li><a class="dropdown-item" href="#">Văn học</a></li>
                            <li><a class="dropdown-item" href="#">Thiếu nhi</a></li>
                            <li><a class="dropdown-item" href="#">Kỹ năng sống</a></li>
                            <li><a class="dropdown-item" href="#">Sách ngoại ngữ</a></li>
                        </ul>
                    </li>
                </ul>
                <form class="d-flex align-items-center me-lg-4 my-3 my-lg-0" style="max-width: 320px;">
                    <div class="input-group">
                        <span class="input-group-text bg-white border-end-0"><i class="fas fa-search text-muted"></i></span>
                        <input class="form-control border-start-0" type="search" placeholder="Tìm kiếm sách, tác giả..." aria-label="Search">
                    </div>
                </form>
                <ul class="navbar-nav align-items-lg-center gap-lg-3">
                    <li class="nav-item"><a class="nav-link" href="login.jsp"><i class="far fa-user me-1"></i>Đăng nhập</a></li>
                    <li class="nav-item"><a class="nav-link" href="register.jsp"><i class="fas fa-user-plus me-1"></i>Đăng ký</a></li>
                    <li class="nav-item">
                        <a class="nav-link position-relative" href="#">
                            <i class="fas fa-shopping-bag me-1"></i>Giỏ hàng
                            <span class="badge bg-danger rounded-pill position-absolute top-0 start-100 translate-middle">0</span>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    <main class="py-5">
        <decorator:body/>
    </main>
    <footer class="border-top bg-white py-4">
        <div class="container d-flex flex-column flex-lg-row justify-content-between align-items-center gap-3 small text-muted">
            <div>© <span id="year"></span> Góc Xếp Bookstore · Lan tỏa văn hóa đọc Việt Nam</div>
            <div><i class="fas fa-phone-volume me-1"></i> 1900 9999 · <i class="fas fa-envelope me-1"></i> hello@gocxep.vn</div>
        </div>
    </footer>
    <script>
        document.getElementById('year').textContent = new Date().getFullYear();
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
