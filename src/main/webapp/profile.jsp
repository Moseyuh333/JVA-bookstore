<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ người dùng - NKBookstore</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .profile-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 2rem 0;
            margin-bottom: 2rem;
        }
        .nav-pills .nav-link.active {
            background-color: #667eea;
        }
        .nav-pills .nav-link {
            color: #667eea;
            margin-bottom: 0.5rem;
        }
        .card {
            box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
            border: 1px solid rgba(0, 0, 0, 0.125);
        }
        .btn-danger-outline {
            color: #dc3545;
            border-color: #dc3545;
        }
        .btn-danger-outline:hover {
            background-color: #dc3545;
            color: white;
        }
    </style>
</head>
<body>
    <div class="profile-header">
        <div class="container">
            <div class="row align-items-center">
                <div class="col-md-6">
                    <h1 class="mb-1"><i class="fas fa-user-circle me-2"></i>Hồ sơ người dùng</h1>
                    <p class="mb-0">Quản lý thông tin cá nhân và tài khoản của bạn</p>
                </div>
                <div class="col-md-6 text-md-end">
                    <a href="<%= request.getContextPath() %>/" class="btn btn-light">
                        <i class="fas fa-home me-1"></i>Về trang chủ
                    </a>
                </div>
            </div>
        </div>
    </div>

    <div class="container">
        <div class="row">
            <div class="col-md-3">
                <div class="card">
                    <div class="card-body">
                        <div class="nav flex-column nav-pills" role="tablist">
                            <a class="nav-link active" id="profile-info-tab" data-bs-toggle="pill" href="#profile-info" role="tab">
                                <i class="fas fa-user me-2"></i>Thông tin cá nhân
                            </a>
                            <a class="nav-link" id="change-password-tab" data-bs-toggle="pill" href="#change-password" role="tab">
                                <i class="fas fa-key me-2"></i>Đổi mật khẩu
                            </a>
                            <a class="nav-link" id="order-history-tab" data-bs-toggle="pill" href="#order-history" role="tab">
                                <i class="fas fa-shopping-bag me-2"></i>Lịch sử đơn hàng
                            </a>
                            <a class="nav-link" id="delete-account-tab" data-bs-toggle="pill" href="#delete-account" role="tab">
                                <i class="fas fa-trash me-2"></i>Xóa tài khoản
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-9">
                <div class="tab-content">
                    <!-- Profile Information Tab -->
                    <div class="tab-pane fade show active" id="profile-info" role="tabpanel">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="mb-0"><i class="fas fa-user me-2"></i>Thông tin cá nhân</h5>
                            </div>
                            <div class="card-body">
                                <form id="profileForm">
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="fullName" class="form-label">Họ và tên *</label>
                                            <input type="text" class="form-control" id="fullName" name="fullName" required>
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="email" class="form-label">Email *</label>
                                            <input type="email" class="form-control" id="email" name="email" required readonly>
                                            <div class="form-text">Email không thể thay đổi</div>
                                        </div>
                                    </div>
                                    <div class="row">
                                        <div class="col-md-6 mb-3">
                                            <label for="phone" class="form-label">Số điện thoại</label>
                                            <input type="tel" class="form-control" id="phone" name="phone">
                                        </div>
                                        <div class="col-md-6 mb-3">
                                            <label for="birthDate" class="form-label">Ngày sinh</label>
                                            <input type="date" class="form-control" id="birthDate" name="birthDate">
                                        </div>
                                    </div>
                                    <div class="mb-3">
                                        <label for="address" class="form-label">Địa chỉ</label>
                                        <textarea class="form-control" id="address" name="address" rows="3"></textarea>
                                    </div>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-save me-1"></i>Lưu thay đổi
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- Change Password Tab -->
                    <div class="tab-pane fade" id="change-password" role="tabpanel">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="mb-0"><i class="fas fa-key me-2"></i>Đổi mật khẩu</h5>
                            </div>
                            <div class="card-body">
                                <form id="changePasswordForm">
                                    <div class="mb-3">
                                        <label for="currentPassword" class="form-label">Mật khẩu hiện tại *</label>
                                        <input type="password" class="form-control" id="currentPassword" name="currentPassword" required>
                                    </div>
                                    <div class="mb-3">
                                        <label for="newPassword" class="form-label">Mật khẩu mới *</label>
                                        <input type="password" class="form-control" id="newPassword" name="newPassword" required minlength="6">
                                        <div class="form-text">Mật khẩu phải có ít nhất 6 ký tự</div>
                                    </div>
                                    <div class="mb-3">
                                        <label for="confirmPassword" class="form-label">Xác nhận mật khẩu mới *</label>
                                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required>
                                    </div>
                                    <button type="submit" class="btn btn-primary">
                                        <i class="fas fa-key me-1"></i>Đổi mật khẩu
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>

                    <!-- Order History Tab -->
                    <div class="tab-pane fade" id="order-history" role="tabpanel">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="mb-0"><i class="fas fa-shopping-bag me-2"></i>Lịch sử đơn hàng</h5>
                            </div>
                            <div class="card-body">
                                <div id="orderHistoryContent">
                                    <div class="text-center py-4">
                                        <div class="spinner-border text-primary" role="status">
                                            <span class="visually-hidden">Đang tải...</span>
                                        </div>
                                        <p class="mt-2">Đang tải lịch sử đơn hàng...</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Delete Account Tab -->
                    <div class="tab-pane fade" id="delete-account" role="tabpanel">
                        <div class="card border-danger">
                            <div class="card-header bg-danger text-white">
                                <h5 class="mb-0"><i class="fas fa-exclamation-triangle me-2"></i>Xóa tài khoản</h5>
                            </div>
                            <div class="card-body">
                                <div class="alert alert-warning">
                                    <h6><i class="fas fa-exclamation-triangle me-2"></i>Cảnh báo!</h6>
                                    <p class="mb-0">Việc xóa tài khoản không thể hoàn tác. Tất cả dữ liệu của bạn sẽ bị xóa vĩnh viễn, bao gồm:</p>
                                    <ul class="mb-0 mt-2">
                                        <li>Thông tin cá nhân</li>
                                        <li>Lịch sử đơn hàng</li>
                                        <li>Dữ liệu tài khoản</li>
                                    </ul>
                                </div>
                                <form id="deleteAccountForm">
                                    <div class="mb-3">
                                        <label for="deletePassword" class="form-label">Nhập mật khẩu để xác nhận *</label>
                                        <input type="password" class="form-control" id="deletePassword" name="password" required>
                                    </div>
                                    <div class="mb-3">
                                        <div class="form-check">
                                            <input class="form-check-input" type="checkbox" id="confirmDelete" required>
                                            <label class="form-check-label" for="confirmDelete">
                                                Tôi hiểu rằng việc này không thể hoàn tác
                                            </label>
                                        </div>
                                    </div>
                                    <button type="submit" class="btn btn-danger">
                                        <i class="fas fa-trash me-1"></i>Xóa tài khoản vĩnh viễn
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Messages -->
    <div id="alertContainer" style="position: fixed; top: 20px; right: 20px; z-index: 1050;"></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const token = localStorage.getItem('jwtToken');
            if (!token) {
                window.location.href = 'login.jsp';
                return;
            }

            const API_BASE_URL = '<%= request.getContextPath() %>/api';

            // Function to fetch user profile
            async function fetchUserProfile() {
                try {
                    const response = await fetch(`${API_BASE_URL}/user/profile`, {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });

                    if (response.ok) {
                        const user = await response.json();
                        document.getElementById('fullName').value = user.fullName || '';
                        document.getElementById('email').value = user.email || '';
                        document.getElementById('phone').value = user.phone || '';
                        if (user.birthDate) {
                            // Backend sends date as array [yyyy, mm, dd] or timestamp
                            let birthDate;
                            if (Array.isArray(user.birthDate)) {
                                // Format [yyyy, mm, dd] to yyyy-MM-dd
                                const [year, month, day] = user.birthDate;
                                birthDate = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
                            } else {
                                // Assuming it's a timestamp or a string 'yyyy-mm-dd'
                                birthDate = new Date(user.birthDate).toISOString().split('T')[0];
                            }
                            document.getElementById('birthDate').value = birthDate;
                        }
                        document.getElementById('address').value = user.address || '';
                    } else if (response.status === 401 || response.status === 403) {
                        localStorage.removeItem('jwtToken');
                        window.location.href = 'login.jsp';
                    } else {
                        showToast('Lỗi tải thông tin người dùng.', 'error');
                    }
                } catch (error) {
                    console.error('Error fetching profile:', error);
                    showToast('Có lỗi xảy ra. Vui lòng thử lại.', 'error');
                }
            }

            // Initial fetch
            fetchUserProfile();
        });
    </script>
</body>
</html>