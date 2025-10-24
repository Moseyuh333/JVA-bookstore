<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cửa hàng - Seller Dashboard</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Feather Icons -->
    <script src="https://unpkg.com/feather-icons"></script>
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">
    
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            background: #f5f5f5;
            font-family: 'Roboto', sans-serif;
        }

        #wrapper {
            display: flex;
            min-height: 100vh;
        }

        #content-wrapper {
            flex: 1;
            margin-left: 0;
            transition: margin-left 0.3s ease;
        }

        #content {
            margin-top: 70px;
            padding: 24px;
        }

        .container-fluid {
            max-width: 1200px;
            margin: 0 auto;
        }

        /* Profile Header */
        .profile-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 16px;
            padding: 40px;
            color: white;
            margin-bottom: 24px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
            position: relative;
            overflow: hidden;
        }

        .profile-header::before {
            content: '';
            position: absolute;
            top: -50%;
            right: -50%;
            width: 200%;
            height: 200%;
            background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
            animation: pulse 15s infinite;
        }

        @keyframes pulse {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.1); }
        }

        .profile-header-content {
            position: relative;
            z-index: 1;
            display: flex;
            align-items: center;
            gap: 24px;
        }

        .profile-avatar {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            background: white;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 40px;
            color: #667eea;
            font-weight: 700;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
        }

        .profile-info h1 {
            font-size: 28px;
            font-weight: 700;
            margin: 0 0 8px 0;
        }

        .profile-info p {
            font-size: 14px;
            margin: 0;
            opacity: 0.9;
        }

        .profile-badge {
            display: inline-block;
            background: rgba(255, 255, 255, 0.2);
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            margin-top: 8px;
        }

        /* Stats Grid */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 24px;
        }

        .stat-card {
            background: white;
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            display: flex;
            align-items: center;
            gap: 16px;
            transition: all 0.3s ease;
        }

        .stat-card:hover {
            box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
            transform: translateY(-4px);
        }

        .stat-icon {
            width: 56px;
            height: 56px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
            color: white;
        }

        .stat-icon.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
        .stat-icon.green { background: linear-gradient(135deg, #00b09b 0%, #96c93d 100%); }
        .stat-icon.orange { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }

        .stat-info h3 {
            font-size: 28px;
            font-weight: 700;
            margin: 0;
            color: #1a202c;
        }

        .stat-info p {
            font-size: 14px;
            margin: 0;
            color: #718096;
        }

        /* Form Section */
        .form-section {
            background: white;
            border-radius: 16px;
            padding: 32px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        .form-section h2 {
            font-size: 24px;
            font-weight: 700;
            color: #1a202c;
            margin: 0 0 24px 0;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .form-section h2 i {
            color: #667eea;
        }

        .form-group {
            margin-bottom: 24px;
        }

        .form-group label {
            font-size: 14px;
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .form-group label i {
            font-size: 16px;
            color: #667eea;
        }

        .form-control {
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            padding: 12px 16px;
            font-size: 14px;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        textarea.form-control {
            min-height: 120px;
            resize: vertical;
        }

        .btn-save {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            padding: 12px 32px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        .btn-save:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        }

        .btn-cancel {
            background: #e2e8f0;
            color: #2d3748;
            border: none;
            border-radius: 8px;
            padding: 12px 32px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-left: 12px;
        }

        .btn-cancel:hover {
            background: #cbd5e0;
        }

        /* Alert Messages */
        .alert {
            border-radius: 8px;
            padding: 16px;
            margin-bottom: 24px;
            border: none;
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .alert-success {
            background: #d4edda;
            color: #155724;
        }

        .alert-danger {
            background: #f8d7da;
            color: #721c24;
        }

        .alert i {
            font-size: 20px;
        }

        /* Loading Spinner */
        .spinner {
            border: 3px solid #f3f3f3;
            border-top: 3px solid #667eea;
            border-radius: 50%;
            width: 20px;
            height: 20px;
            animation: spin 1s linear infinite;
            display: inline-block;
            margin-left: 8px;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Responsive */
        @media (max-width: 768px) {
            .profile-header-content {
                flex-direction: column;
                text-align: center;
            }

            .form-section {
                padding: 20px;
            }

            .btn-save, .btn-cancel {
                width: 100%;
                margin: 8px 0;
            }
        }
    </style>
</head>
<body>
    <div id="wrapper">
        <%@ include file="/WEB-INF/includes/admin/AdSideBar.jsp" %>

        <div id="content-wrapper">
            <%@ include file="/WEB-INF/includes/admin/header.jsp" %>

            <div id="content">
                <div class="container-fluid">
                    <!-- Profile Header -->
                    <div class="profile-header">
                        <div class="profile-header-content">
                            <div class="profile-avatar">
                                <c:choose>
                                    <c:when test="${not empty shop.name}">
                                        ${fn:toUpperCase(fn:substring(shop.name,0,1))}
                                    </c:when>
                                    <c:otherwise>S</c:otherwise>
                                </c:choose>
                            </div>
                            <div class="profile-info">
                                <h1>
                                    <c:choose>
                                        <c:when test="${not empty shop.name}">${shop.name}</c:when>
                                        <c:otherwise>Cửa hàng của bạn</c:otherwise>
                                    </c:choose>
                                </h1>
                                <p><i class="fas fa-store"></i> Tài khoản người bán hàng</p>
                                <span class="profile-badge">
                                    <i class="fas fa-shield-alt"></i> Tài khoản đã xác thực
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- Stats Grid -->
                    <div class="stats-grid">
                        <div class="stat-card">
                            <div class="stat-icon blue">
                                <i class="fas fa-box"></i>
                            </div>
                            <div class="stat-info">
                                <h3 id="totalProducts">--</h3>
                                <p>Tổng sản phẩm</p>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon green">
                                <i class="fas fa-shopping-cart"></i>
                            </div>
                            <div class="stat-info">
                                <h3 id="totalOrders">--</h3>
                                <p>Đơn hàng tháng này</p>
                            </div>
                        </div>
                        <div class="stat-card">
                            <div class="stat-icon orange">
                                <i class="fas fa-star"></i>
                            </div>
                            <div class="stat-info">
                                <h3 id="rating">4.8</h3>
                                <p>Đánh giá trung bình</p>
                            </div>
                        </div>
                    </div>

                    <!-- Alert Messages -->
                    <div id="alertContainer"></div>

                    <!-- Profile Form -->
                    <div class="form-section">
                        <h2><i class="fas fa-edit"></i> Thông tin cửa hàng</h2>
                        <form id="profileForm">
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label><i class="fas fa-store"></i> Tên cửa hàng *</label>
                                        <input type="text" class="form-control" id="shopName" name="name" 
                                               value="${shop.name}" placeholder="Nhập tên cửa hàng" required>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label><i class="fas fa-phone"></i> Số điện thoại</label>
                                        <input type="tel" class="form-control" id="shopPhone" name="phone" 
                                               value="${shop.phone}" placeholder="0901 234 567">
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label><i class="fas fa-envelope"></i> Email liên hệ</label>
                                        <input type="email" class="form-control" id="shopEmail" name="email" 
                                               value="${shop.email}" placeholder="shop@example.com">
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-group">
                                        <label><i class="fas fa-tag"></i> Slogan</label>
                                        <input type="text" class="form-control" id="shopSlogan" name="slogan" 
                                               value="${shop.slogan}" placeholder="Câu khẩu hiệu của cửa hàng">
                                    </div>
                                </div>
                            </div>

                            <div class="form-group">
                                <label><i class="fas fa-map-marker-alt"></i> Địa chỉ</label>
                                <input type="text" class="form-control" id="shopAddress" name="address" 
                                       value="${shop.address}" placeholder="123 Đường ABC, Quận 1, TP.HCM">
                            </div>

                            <div class="form-group">
                                <label><i class="fas fa-align-left"></i> Mô tả cửa hàng</label>
                                <textarea class="form-control" id="shopDescription" name="description" 
                                          placeholder="Giới thiệu về cửa hàng của bạn...">${shop.description}</textarea>
                            </div>

                            <div class="form-group mb-0">
                                <button type="submit" class="btn-save">
                                    <i class="fas fa-save"></i> Lưu thay đổi
                                </button>
                                <button type="button" class="btn-cancel" onclick="window.location.href='${pageContext.request.contextPath}/seller/dashboard'">
                                    <i class="fas fa-times"></i> Hủy
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <%@ include file="/WEB-INF/includes/admin/footer.jsp" %>
        </div>
    </div>

    <!-- jQuery -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <!-- Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/js/bootstrap.bundle.min.js"></script>

    <script>
        const contextPath = "${pageContext.request.contextPath}";

        // Initialize Feather Icons
        if (typeof feather !== "undefined") {
            feather.replace();
        }

        // Load statistics
        $(document).ready(function() {
            loadStats();
        });

        // Determine if shop object is present
        var shopExists = <c:choose><c:when test="${not empty shop}">true</c:when><c:otherwise>false</c:otherwise></c:choose>;

        function loadStats() {
            // Placeholder - you can implement real stats API later
            if (shopExists) {
                $('#totalProducts').text('12');
                $('#totalOrders').text('8');
            } else {
                $('#totalProducts').text('0');
                $('#totalOrders').text('0');
            }
        }

        // Handle form submission
        $('#profileForm').submit(function(e) {
            e.preventDefault();

            const submitBtn = $(this).find('button[type="submit"]');
            const originalText = submitBtn.html();
            submitBtn.prop('disabled', true).html('<i class="spinner"></i> Đang lưu...');

            $.ajax({
                url: contextPath + '/api/seller/profile',
                type: 'POST',
                data: $(this).serialize(),
                success: function(response) {
                    showAlert('success', 'Cập nhật thành công!', 'Thông tin cửa hàng đã được lưu.');
                    setTimeout(() => {
                        location.reload();
                    }, 1500);
                },
                error: function(xhr) {
                    let message = 'Có lỗi xảy ra. Vui lòng thử lại.';
                    if (xhr.responseJSON && xhr.responseJSON.message) {
                        message = xhr.responseJSON.message;
                    }
                    showAlert('danger', 'Lỗi!', message);
                    submitBtn.prop('disabled', false).html(originalText);
                }
            });
        });

        function showAlert(type, title, message) {
            const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
            const html = `
                <div class="alert alert-${type}">
                    <i class="fas ${icon}"></i>
                    <div>
                        <strong>${title}</strong><br>
                        ${message}
                    </div>
                </div>
            `;
            $('#alertContainer').html(html);
            
            setTimeout(() => {
                $('#alertContainer').fadeOut(500, function() {
                    $(this).html('').show();
                });
            }, 5000);
        }
    </script>
</body>
</html>
