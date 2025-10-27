<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%@ page import="utils.DBUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Hồ sơ của tôi" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<!-- Load Bootstrap CSS locally for this page (kept for existing layout) -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
<style>
    #addressModal .modal-body {
        max-height: calc(100vh - 220px);
        overflow-y: auto;
    }

    @media (max-width: 991.98px) {
        #addressModal .modal-dialog {
            margin: 1.5rem auto;
        }
    }
</style>

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
                            <a class="nav-link" id="address-tab" data-bs-toggle="pill" href="#address-management" role="tab">
                                <i class="fas fa-map-marker-alt me-2"></i>Địa chỉ giao hàng
                            </a>
                            <a class="nav-link" id="change-password-tab" data-bs-toggle="pill" href="#change-password" role="tab">
                                <i class="fas fa-key me-2"></i>Đổi mật khẩu
                            </a>
                            <a class="nav-link" id="order-history-tab" data-bs-toggle="pill" href="#order-history" role="tab">
                                <i class="fas fa-shopping-bag me-2"></i>Lịch sử đơn hàng
                            </a>
                            <a class="nav-link" id="favorites-tab" data-bs-toggle="pill" href="#favorites" role="tab">
                                <i class="fas fa-heart me-2"></i>Sản phẩm yêu thích
                            </a>
                            <a class="nav-link" id="recent-views-tab" data-bs-toggle="pill" href="#recent-views" role="tab">
                                <i class="fas fa-eye me-2"></i>Đã xem gần đây
                            </a>
                            <a class="nav-link" id="coupons-tab" data-bs-toggle="pill" href="#coupons" role="tab">
                                <i class="fas fa-ticket-alt me-2"></i>Mã giảm giá
                            </a>
                            <a class="nav-link" id="delete-account-tab" data-bs-toggle="pill" href="#delete-account" role="tab">
                                <i class="fas fa-trash me-2"></i>Xóa tài khoản
                            </a>
                            <%
                                String ctxPath = request.getContextPath();
                                String sessionRole = (String) session.getAttribute("role");
                                String usernameSession = (String) session.getAttribute("username");

                                if ((sessionRole == null || sessionRole.isBlank()) && usernameSession != null) {
                                    try {
                                        String dbRole = DBUtil.getUserRole(usernameSession);
                                        if (dbRole != null && !dbRole.isBlank()) {
                                            sessionRole = dbRole;
                                            session.setAttribute("role", dbRole);
                                        }
                                    } catch (Exception ignored) {
                                    }
                                }

                                boolean isSeller = sessionRole != null && sessionRole.equalsIgnoreCase("seller");
                                if (isSeller) {
                            %>
                                <a href="<%= ctxPath %>/seller/dashboard" class="nav-link text-primary fw-semibold">Trang bán hàng</a>
                                <a href="<%= ctxPath %>/seller/products" class="nav-link text-primary fw-semibold">Quản lý sản phẩm</a>
                                <a href="<%= ctxPath %>/seller/orders" class="nav-link text-primary fw-semibold">Quản lý đơn hàng</a>
                                <a href="<%= ctxPath %>/seller/analytics" class="nav-link text-primary fw-semibold">Thống kê doanh thu</a>
                            <%
                                } else {
                            %>
                                <a href="<%= ctxPath %>/seller/register-shop" class="btn btn-primary w-100 mt-3">Đăng ký trở thành người bán</a>
                            <%
                                }
                            %>
                   
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

                    <!-- Address Management Tab -->
                    <div class="tab-pane fade" id="address-management" role="tabpanel">
                        <div class="card">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h5 class="mb-0"><i class="fas fa-map-marker-alt me-2"></i>Địa chỉ giao hàng</h5>
                                <button type="button" class="btn btn-primary btn-sm" id="addAddressBtn">
                                    <i class="fas fa-plus me-1"></i>Thêm địa chỉ
                                </button>
                            </div>
                            <div class="card-body">
                                <div id="addressListLoading" class="text-center py-4 d-none">
                                    <div class="spinner-border text-primary" role="status">
                                        <span class="visually-hidden">Đang tải...</span>
                                    </div>
                                    <p class="mt-2 text-muted">Đang tải danh sách địa chỉ...</p>
                                </div>
                                <div id="addressListError" class="alert alert-danger d-none" role="alert"></div>
                                <div id="addressEmptyState" class="text-center py-4 d-none">
                                    <i class="fas fa-map-marker-alt fa-3x text-muted mb-3"></i>
                                    <p class="text-muted">Bạn chưa có địa chỉ giao hàng nào.</p>
                                    <button type="button" class="btn btn-outline-primary" id="addAddressCtaBtn">
                                        <i class="fas fa-plus me-1"></i>Thêm địa chỉ đầu tiên
                                    </button>
                                </div>
                                <div id="addressListContainer" class="vstack gap-3"></div>
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
                                <div class="d-flex flex-wrap gap-2 mb-3" id="orderStatusFilters"></div>
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

                    <!-- Favorites Tab -->
                    <div class="tab-pane fade" id="favorites" role="tabpanel">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="mb-0"><i class="fas fa-heart me-2"></i>Sản phẩm yêu thích</h5>
                            </div>
                            <div class="card-body" id="favoritesContent">
                                <div class="text-center py-4 text-muted">Đang tải danh sách yêu thích...</div>
                            </div>
                        </div>
                    </div>

                    <!-- Recent Views Tab -->
                    <div class="tab-pane fade" id="recent-views" role="tabpanel">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="mb-0"><i class="fas fa-eye me-2"></i>Sản phẩm đã xem gần đây</h5>
                            </div>
                            <div class="card-body" id="recentViewsContent">
                                <div class="text-center py-4 text-muted">Đang tải dữ liệu...</div>
                            </div>
                        </div>
                    </div>

                    <!-- Coupons Tab -->
                    <div class="tab-pane fade" id="coupons" role="tabpanel">
                        <div class="card">
                            <div class="card-header">
                                <h5 class="mb-0"><i class="fas fa-ticket-alt me-2"></i>Mã giảm giá của tôi</h5>
                            </div>
                            <div class="card-body" id="couponsContent">
                                <div class="text-center py-4 text-muted">Đang tải mã giảm giá...</div>
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

    <!-- Order Detail Modal -->
    <div class="modal fade" id="orderDetailModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title"><i class="fas fa-receipt me-2"></i>Chi tiết đơn hàng</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body" id="orderDetailContent">
                    <div class="text-center py-4 text-muted">Đang tải thông tin đơn hàng...</div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Review Modal -->
    <div class="modal fade" id="reviewModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <form id="reviewForm" novalidate>
                    <div class="modal-header">
                        <h5 class="modal-title" id="reviewModalTitle">Viết bình luận sản phẩm</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div id="reviewFormAlert" class="alert alert-danger d-none" role="alert"></div>
                        <p id="reviewModalSubtitle" class="text-muted small mb-3"></p>
                        <div id="reviewExistingInfo" class="alert alert-info d-none" role="alert"></div>
                        <input type="hidden" id="reviewBookId" name="bookId">
                        <div class="mb-3">
                            <label for="reviewRating" class="form-label">Chấm điểm sản phẩm</label>
                            <select id="reviewRating" name="rating" class="form-select" required>
                                <option value="5">5 - Cực kỳ hài lòng</option>
                                <option value="4">4 - Hài lòng</option>
                                <option value="3">3 - Tạm ổn</option>
                                <option value="2">2 - Chưa hài lòng</option>
                                <option value="1">1 - Rất tệ</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="reviewContent" class="form-label">Nội dung bình luận *</label>
                            <textarea id="reviewContent" name="content" class="form-control" rows="6" minlength="50" required placeholder="Chia sẻ trải nghiệm của bạn (tối thiểu 50 ký tự)"></textarea>
                            <div class="form-text">Chỉ khách đã nhận hàng mới có thể bình luận. Nội dung tối thiểu 50 ký tự.</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">Ảnh/Video minh hoạ</label>
                            <div class="d-flex flex-column gap-2">
                                <div id="reviewMediaPreview" class="border rounded bg-light p-3 d-none"></div>
                                <div class="d-flex flex-wrap gap-2">
                                    <label class="btn btn-outline-secondary mb-0">
                                        <i class="fas fa-upload me-1"></i>Chọn tệp
                                        <input type="file" class="d-none" id="reviewMediaInput" accept="image/*,video/mp4,video/webm,video/ogg">
                                    </label>
                                    <button type="button" class="btn btn-outline-danger d-none" id="reviewMediaRemoveBtn">
                                        <i class="fas fa-times me-1"></i>Xóa nội dung
                                    </button>
                                </div>
                                <div class="form-text">Hỗ trợ ảnh (tối đa 5MB) hoặc video MP4/WebM/Ogg (tối đa 20MB).</div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary" id="reviewSubmitBtn"><i class="fas fa-paper-plane me-1"></i>Gửi bình luận</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Address Modal -->
    <div class="modal fade" id="addressModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog modal-lg modal-dialog-scrollable">
            <div class="modal-content">
                <form id="addressForm" novalidate>
                    <div class="modal-header">
                        <h5 class="modal-title" id="addressModalTitle">Thêm địa chỉ</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div id="addressFormError" class="alert alert-danger d-none" role="alert"></div>
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label for="addressLabel" class="form-label">Ghi chú</label>
                                <input type="text" class="form-control" id="addressLabel" name="label" placeholder="Nhà riêng, cơ quan...">
                            </div>
                            <div class="col-md-6">
                                <label for="addressRecipient" class="form-label">Tên người nhận *</label>
                                <input type="text" class="form-control" id="addressRecipient" name="recipientName" required>
                            </div>
                            <div class="col-md-6">
                                <label for="addressPhone" class="form-label">Số điện thoại *</label>
                                <input type="tel" class="form-control" id="addressPhone" name="phone" required>
                            </div>
                            <div class="col-md-6">
                                <label for="addressLine1" class="form-label">Địa chỉ cụ thể *</label>
                                <input type="text" class="form-control" id="addressLine1" name="line1" required>
                            </div>
                            <div class="col-md-6">
                                <label for="addressLine2" class="form-label">Địa chỉ bổ sung</label>
                                <input type="text" class="form-control" id="addressLine2" name="line2">
                            </div>
                            <div class="col-md-6">
                                <label for="addressWard" class="form-label">Phường/Xã</label>
                                <input type="text" class="form-control" id="addressWard" name="ward">
                            </div>
                            <div class="col-md-6">
                                <label for="addressDistrict" class="form-label">Quận/Huyện</label>
                                <input type="text" class="form-control" id="addressDistrict" name="district">
                            </div>
                            <div class="col-md-6">
                                <label for="addressCity" class="form-label">Thành phố *</label>
                                <input type="text" class="form-control" id="addressCity" name="city" required>
                            </div>
                            <div class="col-md-6">
                                <label for="addressProvince" class="form-label">Tỉnh</label>
                                <input type="text" class="form-control" id="addressProvince" name="province">
                            </div>
                            <div class="col-md-6">
                                <label for="addressPostalCode" class="form-label">Mã bưu chính</label>
                                <input type="text" class="form-control" id="addressPostalCode" name="postalCode">
                            </div>
                            <div class="col-md-6">
                                <label for="addressCountry" class="form-label">Quốc gia</label>
                                <input type="text" class="form-control" id="addressCountry" name="country" value="Việt Nam">
                            </div>
                            <div class="col-12">
                                <label for="addressNote" class="form-label">Ghi chú giao hàng</label>
                                <textarea class="form-control" id="addressNote" name="note" rows="2"></textarea>
                            </div>
                            <div class="col-12">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="addressIsDefault" name="isDefault">
                                    <label class="form-check-label" for="addressIsDefault">Đặt làm địa chỉ mặc định</label>
                                </div>
                            </div>
                        </div>
                        <div class="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">Hủy</button>
                            <button type="submit" class="btn btn-primary" id="addressSubmitBtn">Thêm mới</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Messages -->
    <div id="alertContainer" style="position: fixed; top: 20px; right: 20px; z-index: 1050;"></div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        const contextPath = '<%= request.getContextPath() %>';
        let currentUser = null;
        const addressState = {
            addressList: [],
            loading: false,
            error: null,
            selectedAddressId: null,
            formModal: null,
            form: null,
            listContainer: null,
            emptyStateEl: null,
            loadingMessageEl: null,
            errorMessageEl: null
        };
        const ORDER_STATUS_FILTERS = [
            { key: 'all', label: 'Tất cả', badge: 'secondary' },
            { key: 'new', label: 'Đơn hàng mới', badge: 'info' },
            { key: 'confirmed', label: 'Đã xác nhận', badge: 'primary' },
            { key: 'shipping', label: 'Đang giao', badge: 'warning' },
            { key: 'delivered', label: 'Đã giao', badge: 'success' },
            { key: 'cancelled', label: 'Đã hủy', badge: 'danger' },
            { key: 'failed', label: 'Giao that bai', badge: 'danger' },
            { key: 'returned', label: 'Hoàn trả / Hoàn tiền', badge: 'dark' }
        ];
        const orderHistoryState = {
            status: 'all',
            orders: []
        };
        const AUTH_STORAGE_KEY = 'auth_token';
        function getAuthToken() {
            const raw = localStorage.getItem(AUTH_STORAGE_KEY);
            if (!raw) {
                return null;
            }
            const trimmed = raw.trim();
            return trimmed.length > 0 && trimmed !== 'null' && trimmed !== 'undefined' ? trimmed : null;
        }
        function buildAuthHeaders(base) {
            const headers = Object.assign({}, base || {});
            const token = getAuthToken();
            if (token && !headers.Authorization) {
                headers.Authorization = 'Bearer ' + token;
            }
            return headers;
        }
        const favoritesState = {
            data: [],
            loading: false
        };
        const recentViewsState = {
            data: [],
            loading: false
        };
        const couponsState = {
            data: [],
            loading: false
        };
        let orderDetailModal = null;
        let reviewModal = null;
        const reviewState = {
            bookId: null,
            orderId: null,
            orderItemId: null,
            orderCode: '',
            bookTitle: '',
            loading: false,
            initialMediaUrl: null,
            initialMediaType: null,
            mediaUrl: null,
            mediaType: null,
            mediaFile: null,
            previewObjectUrl: null,
            removeMedia: false,
            reviewId: null
        };

        // Check authentication on page load
        document.addEventListener('DOMContentLoaded', function() {
            const token = localStorage.getItem('auth_token');
            if (!token) {
                // Redirect to login if not authenticated
                alert('Vui lòng đăng nhập để truy cập trang này.');
                window.location.href = `${contextPath}/login.jsp`;
                return;
            }
            
            const modalEl = document.getElementById('orderDetailModal');
            if (modalEl) {
                orderDetailModal = new bootstrap.Modal(modalEl);
            }

            const reviewModalEl = document.getElementById('reviewModal');
            if (reviewModalEl) {
                reviewModal = new bootstrap.Modal(reviewModalEl);
                reviewModalEl.addEventListener('hidden.bs.modal', () => {
                    resetReviewForm();
                    reviewState.bookId = null;
                    reviewState.orderId = null;
                    reviewState.orderItemId = null;
                    reviewState.orderCode = '';
                    reviewState.bookTitle = '';
                });
            }

            const reviewMediaInput = document.getElementById('reviewMediaInput');
            if (reviewMediaInput) {
                reviewMediaInput.addEventListener('change', handleReviewMediaInputChange);
            }
            const reviewMediaRemoveBtn = document.getElementById('reviewMediaRemoveBtn');
            if (reviewMediaRemoveBtn) {
                reviewMediaRemoveBtn.addEventListener('click', handleReviewMediaRemove);
            }

            loadUserProfile();
            initAddressManager();
            initOrderHistoryFilters();
            loadOrderHistory();
            loadFavorites();
            loadRecentViews();
            loadUserCoupons();
        });

        // Profile form submission
        document.getElementById('profileForm').addEventListener('submit', function(e) {
            e.preventDefault();
            updateProfile();
        });

        // Change password form submission
        document.getElementById('changePasswordForm').addEventListener('submit', function(e) {
            e.preventDefault();
            changePassword();
        });

        // Delete account form submission
        document.getElementById('deleteAccountForm').addEventListener('submit', function(e) {
            e.preventDefault();
            deleteAccount();
        });

        document.getElementById('reviewForm').addEventListener('submit', function (event) {
            event.preventDefault();
            submitReview();
        });

        function loadUserProfile() {
            const token = localStorage.getItem('auth_token');
            fetch(`${contextPath}/api/profile`, {
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        currentUser = data.user;
                        document.getElementById('fullName').value = data.user.fullName || '';
                        document.getElementById('email').value = data.user.email || '';
                        document.getElementById('phone').value = data.user.phone || '';
                        document.getElementById('birthDate').value = data.user.birthDate || '';
                        document.getElementById('address').value = data.user.address || '';
                    } else {
                        showAlert('Không thể tải thông tin profile: ' + data.message, 'danger');
                        if (data.message === 'Not authenticated') {
                            window.location.href = `${contextPath}/login.jsp`;
                        }
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    showAlert('Lỗi kết nối. Vui lòng thử lại.', 'danger');
                });
        }

        function updateProfile() {
            const formData = new FormData(document.getElementById('profileForm'));
            const profileData = Object.fromEntries(formData);

            const token = localStorage.getItem('auth_token');
            fetch(`${contextPath}/api/profile`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(profileData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showAlert('Cập nhật thông tin thành công!', 'success');
                } else {
                    showAlert('Lỗi: ' + data.message, 'danger');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showAlert('Lỗi kết nối. Vui lòng thử lại.', 'danger');
            });
        }

        function changePassword() {
            const newPassword = document.getElementById('newPassword').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (newPassword !== confirmPassword) {
                showAlert('Mật khẩu xác nhận không khớp!', 'danger');
                return;
            }

            const formData = new FormData(document.getElementById('changePasswordForm'));
            const passwordData = Object.fromEntries(formData);

            const token = localStorage.getItem('auth_token');
            fetch(`${contextPath}/api/profile/password`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(passwordData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showAlert('Đổi mật khẩu thành công!', 'success');
                    document.getElementById('changePasswordForm').reset();
                } else {
                    showAlert('Lỗi: ' + data.message, 'danger');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showAlert('Lỗi kết nối. Vui lòng thử lại.', 'danger');
            });
        }

        function initOrderHistoryFilters() {
            const container = document.getElementById('orderStatusFilters');
            if (!container) {
                return;
            }
            container.innerHTML = '';
            ORDER_STATUS_FILTERS.forEach(filter => {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'btn btn-sm btn-outline-secondary';
                btn.setAttribute('data-status-filter', filter.key);
                btn.textContent = filter.label;
                container.appendChild(btn);
            });
            container.addEventListener('click', event => {
                const target = event.target.closest('[data-status-filter]');
                if (!target) {
                    return;
                }
                const statusKey = target.getAttribute('data-status-filter');
                if (!statusKey || statusKey === orderHistoryState.status) {
                    return;
                }
                orderHistoryState.status = statusKey;
                updateOrderFilterActive();
                loadOrderHistory();
            });
            updateOrderFilterActive();
        }

        function updateOrderFilterActive() {
            const container = document.getElementById('orderStatusFilters');
            if (!container) {
                return;
            }
            container.querySelectorAll('[data-status-filter]').forEach(btn => {
                const statusKey = btn.getAttribute('data-status-filter');
                if (statusKey === orderHistoryState.status) {
                    btn.classList.remove('btn-outline-secondary');
                    btn.classList.add('btn-primary');
                } else {
                    btn.classList.add('btn-outline-secondary');
                    btn.classList.remove('btn-primary');
                }
            });
        }

        function loadOrderHistory(statusKey) {
            if (statusKey && statusKey !== orderHistoryState.status) {
                orderHistoryState.status = statusKey;
                updateOrderFilterActive();
            }
            const container = document.getElementById('orderHistoryContent');
            if (container) {
                container.innerHTML = `
                    <div class="text-center py-4">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Đang tải...</span>
                        </div>
                        <p class="mt-2">Đang tải lịch sử đơn hàng...</p>
                    </div>
                `;
            }
            let url = `${contextPath}/api/profile/orders`;
            if (orderHistoryState.status && orderHistoryState.status !== 'all') {
                url += `?status=${encodeURIComponent(orderHistoryState.status)}`;
            }
            const headers = buildAuthHeaders();
            fetch(url, {
                headers: headers,
                credentials: 'include'
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        orderHistoryState.orders = Array.isArray(data.orders) ? data.orders : [];
                        displayOrderHistory(orderHistoryState.orders);
                        updateOrderFilterActive();
                    } else {
                        if (container) {
                            container.innerHTML = '<div class="text-center py-4"><p>Không thể tải lịch sử đơn hàng</p></div>';
                        }
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    if (container) {
                        container.innerHTML = '<div class="text-center py-4"><p>Lỗi kết nối</p></div>';
                    }
                });
        }

        function displayOrderHistory(orders) {
            const container = document.getElementById('orderHistoryContent');
            if (!container) {
                return;
            }
            if (!Array.isArray(orders) || orders.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4">
                        <i class="fas fa-shopping-bag fa-3x text-muted mb-3"></i>
                        <p class="text-muted">Bạn chưa có đơn hàng nào</p>
                        <a href="<%= request.getContextPath() %>/" class="btn btn-primary">Mua sắm ngay</a>
                    </div>
                `;
                return;
            }

            let html = '<div class="table-responsive"><table class="table table-striped align-middle">';
            html += '<thead><tr><th>Mã đơn</th><th>Ngày đặt</th><th>Tổng tiền</th><th>Trạng thái</th><th></th></tr></thead><tbody>';

            orders.forEach(order => {
                const orderCode = order.code && order.code.trim() ? order.code.trim() : `#${order.id}`;
                const statusMeta = getStatusMeta(order.status);
                const badgeClass = statusMeta.badge ? `bg-${statusMeta.badge}` : 'bg-secondary';
                html += `
                    <tr>
                        <td class="fw-semibold">${escapeHtml(orderCode)}</td>
                        <td>${formatDate(order.orderDate)}</td>
                        <td class="fw-semibold text-primary">${formatCurrency(order.totalAmount)}</td>
                        <td><span class="badge ${badgeClass}">${escapeHtml(getStatusLabel(order.status))}</span></td>
                        <td class="text-end"><button class="btn btn-sm btn-outline-primary" onclick="viewOrderDetails(${order.id})"><i class="fas fa-eye me-1"></i>Xem</button></td>
                    </tr>
                `;
            });

            html += '</tbody></table></div>';
            container.innerHTML = html;
        }

        async function loadFavorites() {
            const container = document.getElementById('favoritesContent');
            if (!container) {
                return;
            }
            container.innerHTML = '<div class="text-center py-4 text-muted">Đang tải danh sách yêu thích...</div>';
            const token = localStorage.getItem('auth_token');
            try {
                const response = await fetch(`${contextPath}/api/profile/favorites`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                const data = await response.json();
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'Không thể tải danh sách yêu thích.');
                }
                favoritesState.data = Array.isArray(data.favorites) ? data.favorites : [];
                renderFavorites(favoritesState.data);
            } catch (error) {
                console.error('loadFavorites error', error);
                container.innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message || 'Không thể tải danh sách yêu thích.')}</div>`;
            }
        }

        function renderFavorites(favorites) {
            const container = document.getElementById('favoritesContent');
            if (!container) {
                return;
            }
            if (!Array.isArray(favorites) || favorites.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4 text-muted">
                        <i class="fas fa-heart fa-2x mb-3"></i>
                        <p>Bạn chưa thêm sản phẩm yêu thích nào.</p>
                    </div>
                `;
                return;
            }
            let html = '<div class="row g-3">';
            favorites.forEach(item => {
                const image = item.imageUrl && item.imageUrl.trim() ? item.imageUrl.trim() : 'https://placehold.co/300x400';
                html += `
                    <div class="col-md-6 col-lg-4">
                        <div class="card h-100 shadow-sm">
                            <img src="${escapeHtml(image)}" class="card-img-top" alt="${escapeHtml(item.title || 'Sách')}" style="object-fit: cover; height: 200px;">
                            <div class="card-body d-flex flex-column">
                                <h6 class="card-title">${escapeHtml(item.title || 'Sách')}</h6>
                                ${item.author ? `<p class="text-muted small mb-2">${escapeHtml(item.author)}</p>` : ''}
                                <div class="mt-auto d-flex justify-content-between align-items-center">
                                    <span class="fw-semibold text-primary">${formatCurrency(item.price)}</span>
                                    <a class="btn btn-sm btn-outline-primary" href="${contextPath}/books/detail?id=${item.bookId}">Xem sách</a>
                                </div>
                            </div>
                            <div class="card-footer text-muted small">Đã lưu: ${formatDateTime(item.markedAt)}</div>
                        </div>
                    </div>
                `;
            });
            html += '</div>';
            container.innerHTML = html;
        }

        async function loadRecentViews() {
            const container = document.getElementById('recentViewsContent');
            if (!container) {
                return;
            }
            container.innerHTML = '<div class="text-center py-4 text-muted">Đang tải sản phẩm đã xem...</div>';
            const token = localStorage.getItem('auth_token');
            try {
                const response = await fetch(`${contextPath}/api/profile/recent-views?limit=12`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                const data = await response.json();
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'Không thể tải sản phẩm đã xem.');
                }
                recentViewsState.data = Array.isArray(data.data) ? data.data : [];
                renderRecentViews(recentViewsState.data);
            } catch (error) {
                console.error('loadRecentViews error', error);
                container.innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message || 'Không thể tải sản phẩm đã xem.')}</div>`;
            }
        }

        function renderRecentViews(list) {
            const container = document.getElementById('recentViewsContent');
            if (!container) {
                return;
            }
            if (!Array.isArray(list) || list.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4 text-muted">
                        <i class="fas fa-eye fa-2x mb-3"></i>
                        <p>Chưa có sản phẩm nào trong lịch sử xem gần đây.</p>
                    </div>
                `;
                return;
            }
            let html = '<div class="row g-3">';
            list.forEach(item => {
                const image = item.imageUrl && item.imageUrl.trim() ? item.imageUrl.trim() : 'https://placehold.co/300x400';
                html += `
                    <div class="col-md-6 col-lg-4">
                        <div class="card h-100">
                            <div class="row g-0 h-100">
                                <div class="col-4">
                                    <img src="${escapeHtml(image)}" class="img-fluid rounded-start" alt="${escapeHtml(item.title || 'Sản phẩm')}">
                                </div>
                                <div class="col-8">
                                    <div class="card-body p-3 d-flex flex-column h-100">
                                        <h6 class="card-title mb-1">${escapeHtml(item.title || 'Sản phẩm')}</h6>
                                        ${item.author ? `<p class="text-muted small mb-2">${escapeHtml(item.author)}</p>` : ''}
                                        <div class="mt-auto d-flex justify-content-between align-items-center">
                                            <span class="fw-semibold text-primary">${formatCurrency(item.price)}</span>
                                            <a class="btn btn-sm btn-outline-secondary" href="${contextPath}/books/detail?id=${item.bookId}"><i class="fas fa-book-open"></i></a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="card-footer text-muted small">Xem lúc: ${formatDateTime(item.viewedAt)}</div>
                        </div>
                    </div>
                `;
            });
            html += '</div>';
            container.innerHTML = html;
        }

        async function loadUserCoupons() {
            const container = document.getElementById('couponsContent');
            if (!container) {
                return;
            }
            container.innerHTML = '<div class="text-center py-4 text-muted">Đang tải mã giảm giá...</div>';
            const token = localStorage.getItem('auth_token');
            try {
                const response = await fetch(`${contextPath}/api/profile/coupons`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                const data = await response.json();
                if (!response.ok || !data.success) {
                    throw new Error(data.message || 'Không thể tải mã giảm giá.');
                }
                couponsState.data = Array.isArray(data.coupons) ? data.coupons : [];
                renderCoupons(couponsState.data);
            } catch (error) {
                console.error('loadUserCoupons error', error);
                container.innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message || 'Không thể tải mã giảm giá.')}</div>`;
            }
        }

        function renderCoupons(list) {
            const container = document.getElementById('couponsContent');
            if (!container) {
                return;
            }
            if (!Array.isArray(list) || list.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4 text-muted">
                        <i class="fas fa-ticket-alt fa-2x mb-3"></i>
                        <p>Bạn chưa có mã giảm giá nào.</p>
                    </div>
                `;
                return;
            }

            const now = new Date();
            const html = list.map(coupon => {
                const rawValue = Number(coupon.value);
                const percentValue = Number.isFinite(rawValue) ? rawValue : 0;
                const rawMaxDiscount = Number(coupon.maxDiscount);
                const hasMaxDiscount = Number.isFinite(rawMaxDiscount) && rawMaxDiscount > 0;
                const discountLine = coupon.type === 'percentage'
                    ? `Giảm ${percentValue.toLocaleString('vi-VN')}%${hasMaxDiscount ? ` (tối đa ${formatCurrency(coupon.maxDiscount)})` : ''}`
                    : `Giảm ${formatCurrency(coupon.value)}`;
                const minimumOrderLine = coupon.minimumOrder && Number(coupon.minimumOrder) > 0
                    ? `<div class="text-muted small">Đơn tối thiểu: ${formatCurrency(coupon.minimumOrder)}</div>`
                    : '';
                let validityLine = '';
                if (coupon.startDate || coupon.endDate) {
                    const start = coupon.startDate ? formatDate(coupon.startDate) : 'Bất kỳ';
                    const end = coupon.endDate ? formatDate(coupon.endDate) : 'Không giới hạn';
                    validityLine = `<div class="text-muted small">Hiệu lực: ${start} - ${end}</div>`;
                }
                const userStatus = (coupon.userStatus || '').toLowerCase();
                let statusBadge = '<span class="badge bg-success">Sẵn sàng sử dụng</span>';
                if (userStatus === 'used') {
                    statusBadge = '<span class="badge bg-secondary">Đã sử dụng</span>';
                } else if (userStatus === 'expired') {
                    statusBadge = '<span class="badge bg-danger">Hết hạn</span>';
                } else if (coupon.endDate) {
                    const endDate = new Date(coupon.endDate);
                    if (!Number.isNaN(endDate.getTime()) && endDate < now) {
                        statusBadge = '<span class="badge bg-danger">Hết hạn</span>';
                    }
                }
                return `
                    <div class="card shadow-sm mb-3">
                        <div class="card-body d-flex flex-column flex-md-row justify-content-between align-items-start gap-3">
                            <div>
                                <h5 class="card-title mb-1">${escapeHtml(coupon.code)}</h5>
                                ${coupon.description ? `<p class="mb-2 text-muted">${escapeHtml(coupon.description)}</p>` : ''}
                                <div class="small text-muted">${discountLine}</div>
                                ${minimumOrderLine}
                                ${validityLine}
                            </div>
                            <div class="ms-md-auto">${statusBadge}</div>
                        </div>
                    </div>
                `;
            }).join('');

            container.innerHTML = html;
        }

        function initAddressManager() {
            addressState.listContainer = document.getElementById('addressListContainer');
            addressState.emptyStateEl = document.getElementById('addressEmptyState');
            addressState.loadingMessageEl = document.getElementById('addressListLoading');
            addressState.errorMessageEl = document.getElementById('addressListError');
            addressState.form = document.getElementById('addressForm');

            const modalEl = document.getElementById('addressModal');
            if (modalEl) {
                addressState.formModal = new bootstrap.Modal(modalEl);
                modalEl.addEventListener('hidden.bs.modal', resetAddressForm);
            }

            document.getElementById('addAddressBtn')?.addEventListener('click', () => {
                openAddressModal();
            });

            document.getElementById('addAddressCtaBtn')?.addEventListener('click', () => {
                openAddressModal();
            });

            if (addressState.form) {
                addressState.form.addEventListener('submit', handleAddressSubmit);
            }

            if (addressState.listContainer) {
                addressState.listContainer.addEventListener('click', handleAddressListClick);
            }

            loadAddresses();
        }

        function handleAddressListClick(event) {
            const actionBtn = event.target.closest('[data-action]');
            if (!actionBtn) {
                return;
            }

            const addressCard = actionBtn.closest('[data-address-id]');
            if (!addressCard) {
                return;
            }

            const addressId = Number(addressCard.getAttribute('data-address-id'));
            if (!addressId) {
                return;
            }

            const action = actionBtn.getAttribute('data-action');
            switch (action) {
                case 'edit':
                    openAddressModal(addressId);
                    break;
                case 'delete':
                    deleteAddress(addressId);
                    break;
                case 'set-default':
                    setDefaultAddress(addressId);
                    break;
                default:
                    break;
            }
        }

        async function loadAddresses() {
            if (!addressState.listContainer) {
                return;
            }

            addressState.loading = true;
            renderAddressList();

            const token = localStorage.getItem('auth_token');
            if (!token) {
                addressState.error = 'Bạn cần đăng nhập để xem địa chỉ.';
                addressState.loading = false;
                renderAddressList();
                return;
            }

            try {
                const response = await fetch(`${contextPath}/api/profile/addresses`, {
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });
                if (response.status === 401) {
                    addressState.loading = false;
                    renderAddressList();
                    window.location.href = `${contextPath}/login.jsp`;
                    return;
                }
                const data = await response.json();
                if (response.ok && data.success) {
                    addressState.addressList = Array.isArray(data.addresses) ? data.addresses : [];
                    addressState.error = null;
                } else {
                    addressState.error = data.message || 'Không thể tải danh sách địa chỉ.';
                }
            } catch (error) {
                console.error('Error loading addresses:', error);
                addressState.error = 'Lỗi kết nối. Vui lòng thử lại.';
            } finally {
                addressState.loading = false;
                renderAddressList();
            }
        }

        function renderAddressList() {
            if (!addressState.listContainer || !addressState.emptyStateEl || !addressState.loadingMessageEl || !addressState.errorMessageEl) {
                return;
            }

            addressState.listContainer.innerHTML = '';
            addressState.emptyStateEl.classList.add('d-none');
            addressState.loadingMessageEl.classList.add('d-none');
            addressState.errorMessageEl.classList.add('d-none');

            if (addressState.loading) {
                addressState.loadingMessageEl.classList.remove('d-none');
                return;
            }

            if (addressState.error) {
                addressState.errorMessageEl.textContent = addressState.error;
                addressState.errorMessageEl.classList.remove('d-none');
                return;
            }

            if (addressState.addressList.length === 0) {
                addressState.emptyStateEl.classList.remove('d-none');
                return;
            }

            const fragment = document.createDocumentFragment();
            addressState.addressList.forEach(address => {
                fragment.appendChild(buildAddressCard(address));
            });
            addressState.listContainer.appendChild(fragment);
        }

        function buildAddressCard(address) {
            const card = document.createElement('div');
            card.className = 'card mb-3';
            card.setAttribute('data-address-id', address.id);

            const headerBadges = [];
            if (address.isDefault) {
                headerBadges.push('<span class="badge bg-primary ms-2">Mặc định</span>');
            }
            if (address.label) {
                headerBadges.push(`<span class="badge bg-secondary ms-2">${escapeHtml(address.label)}</span>`);
            }

            const addressLines = [
                address.line1,
                address.line2,
                address.ward,
                address.district,
                address.city,
                address.province,
                address.postalCode,
                address.country
            ]
                .filter(part => part && part.trim())
                .join(', ');

            const noteSection = address.note ? `
                <p class="mb-0 text-muted"><small>Ghi chú: ${escapeHtml(address.note)}</small></p>
            ` : '';

            const defaultButton = address.isDefault ? '' : `
                <button class="btn btn-sm btn-outline-primary me-2" data-action="set-default">Đặt mặc định</button>
            `;

            card.innerHTML = `
                <div class="card-body d-flex flex-column flex-md-row justify-content-between align-items-start">
                    <div class="me-md-3">
                        <div class="d-flex align-items-center mb-1">
                            <h6 class="mb-0">${escapeHtml(address.recipientName)}</h6>
                            ${headerBadges.join('')}
                        </div>
                        <p class="mb-1 text-muted">${escapeHtml(address.phone)}</p>
                        <p class="mb-1">${escapeHtml(addressLines)}</p>
                        ${noteSection}
                    </div>
                    <div class="mt-3 mt-md-0 text-md-end">
                        ${defaultButton}
                        <button class="btn btn-sm btn-outline-secondary me-2" data-action="edit">Sửa</button>
                        <button class="btn btn-sm btn-outline-danger" data-action="delete">Xóa</button>
                    </div>
                </div>
            `;

            return card;
        }

        function openAddressModal(addressId) {
            addressState.selectedAddressId = addressId ?? null;
            if (!addressState.formModal || !addressState.form) {
                return;
            }

            const modalTitle = document.getElementById('addressModalTitle');
            if (modalTitle) {
                modalTitle.textContent = addressId ? 'Cập nhật địa chỉ' : 'Thêm địa chỉ mới';
            }

            const submitBtn = document.getElementById('addressSubmitBtn');
            if (submitBtn) {
                submitBtn.textContent = addressId ? 'Cập nhật' : 'Thêm mới';
            }

            if (addressId) {
                const address = addressState.addressList.find(item => item.id === addressId);
                if (address) {
                    populateAddressForm(address);
                } else {
                    showAlert('Không tìm thấy địa chỉ đã chọn.', 'danger');
                    addressState.selectedAddressId = null;
                    return;
                }
            } else {
                resetAddressForm();
            }

            addressState.formModal.show();
        }

        function populateAddressForm(address) {
            if (!addressState.form) {
                return;
            }
            const elements = addressState.form.elements;
            elements['label'].value = address.label || '';
            elements['recipientName'].value = address.recipientName || '';
            elements['phone'].value = address.phone || '';
            elements['line1'].value = address.line1 || '';
            elements['line2'].value = address.line2 || '';
            elements['ward'].value = address.ward || '';
            elements['district'].value = address.district || '';
            elements['city'].value = address.city || '';
            elements['province'].value = address.province || '';
            elements['postalCode'].value = address.postalCode || '';
            elements['country'].value = address.country || 'Việt Nam';
            elements['note'].value = address.note || '';
            elements['isDefault'].checked = Boolean(address.isDefault);
        }

        function resetAddressForm() {
            if (!addressState.form) {
                return;
            }
            addressState.form.reset();
            addressState.selectedAddressId = null;
            const errorEl = document.getElementById('addressFormError');
            if (errorEl) {
                errorEl.classList.add('d-none');
                errorEl.textContent = '';
            }
            const submitBtn = document.getElementById('addressSubmitBtn');
            if (submitBtn) {
                submitBtn.textContent = 'Thêm mới';
            }
            const modalTitle = document.getElementById('addressModalTitle');
            if (modalTitle) {
                modalTitle.textContent = 'Thêm địa chỉ';
            }
        }

        async function handleAddressSubmit(event) {
            event.preventDefault();
            if (!addressState.form) {
                return;
            }

            const submitBtn = addressState.form.querySelector('button[type="submit"]');
            const errorEl = document.getElementById('addressFormError');
            if (errorEl) {
                errorEl.classList.add('d-none');
                errorEl.textContent = '';
            }

            const formData = new FormData(addressState.form);
            const payload = {};
            formData.forEach((value, key) => {
                if (key === 'isDefault') {
                    return;
                }
                payload[key] = typeof value === 'string' ? value.trim() : value;
            });
            payload.isDefault = formData.get('isDefault') === 'on';

            if (!payload.recipientName || !payload.phone || !payload.line1) {
                showFormError('Vui lòng nhập đầy đủ tên người nhận, số điện thoại và địa chỉ.');
                return;
            }

            const token = localStorage.getItem('auth_token');
            if (!token) {
                showFormError('Bạn cần đăng nhập để lưu địa chỉ.');
                return;
            }

            const addressId = addressState.selectedAddressId;
            const url = addressId ? `${contextPath}/api/profile/addresses/${addressId}` : `${contextPath}/api/profile/addresses`;
            const method = addressId ? 'PUT' : 'POST';

            if (submitBtn) {
                submitBtn.disabled = true;
            }

            try {
                const response = await fetch(url, {
                    method,
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify(payload)
                });
                const data = await response.json().catch(() => ({}));
                if (response.status === 401 || data.message === 'Not authenticated') {
                    addressState.formModal?.hide();
                    showAlert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'warning');
                    window.location.href = `${contextPath}/login.jsp`;
                    return;
                }
                if (response.ok && data.success) {
                    addressState.formModal?.hide();
                    showAlert(addressId ? 'Đã cập nhật địa chỉ' : 'Đã thêm địa chỉ mới', 'success');
                    await loadAddresses();
                } else {
                    showFormError(data.message || 'Không thể lưu địa chỉ.');
                }
            } catch (error) {
                console.error('Error saving address:', error);
                showFormError('Lỗi kết nối. Vui lòng thử lại.');
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                }
            }
        }

        function showFormError(message) {
            const errorEl = document.getElementById('addressFormError');
            if (errorEl) {
                errorEl.textContent = message;
                errorEl.classList.remove('d-none');
            } else {
                showAlert(message, 'danger');
            }
        }

        async function deleteAddress(addressId) {
            if (!confirm('Bạn có chắc muốn xóa địa chỉ này?')) {
                return;
            }

            const token = localStorage.getItem('auth_token');
            if (!token) {
                showAlert('Bạn cần đăng nhập để xóa địa chỉ.', 'danger');
                return;
            }

            try {
                const response = await fetch(`${contextPath}/api/profile/addresses/${addressId}`, {
                    method: 'DELETE',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });
                const data = await response.json().catch(() => ({}));
                if (response.status === 401 || data.message === 'Not authenticated') {
                    showAlert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'warning');
                    window.location.href = `${contextPath}/login.jsp`;
                    return;
                }
                if (response.ok && data.success) {
                    showAlert('Đã xóa địa chỉ', 'success');
                    await loadAddresses();
                } else {
                    showAlert(data.message || 'Không thể xóa địa chỉ.', 'danger');
                }
            } catch (error) {
                console.error('Error deleting address:', error);
                showAlert('Lỗi kết nối. Vui lòng thử lại.', 'danger');
            }
        }

        async function setDefaultAddress(addressId) {
            const token = localStorage.getItem('auth_token');
            if (!token) {
                showAlert('Bạn cần đăng nhập để thao tác.', 'danger');
                return;
            }

            try {
                const response = await fetch(`${contextPath}/api/profile/addresses/${addressId}/default`, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    }
                });
                const data = await response.json().catch(() => ({}));
                if (response.status === 401 || data.message === 'Not authenticated') {
                    showAlert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', 'warning');
                    window.location.href = `${contextPath}/login.jsp`;
                    return;
                }
                if (response.ok && data.success) {
                    showAlert('Đã đặt địa chỉ mặc định', 'success');
                    await loadAddresses();
                } else {
                    showAlert(data.message || 'Không thể đặt địa chỉ mặc định.', 'danger');
                }
            } catch (error) {
                console.error('Error setting default address:', error);
                showAlert('Lỗi kết nối. Vui lòng thử lại.', 'danger');
            }
        }

        function escapeHtml(value) {
            if (typeof value !== 'string') {
                return value ?? '';
            }
            return value
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function getStatusMeta(status) {
            if (!status) {
                return { key: 'unknown', label: 'Không xác định', badge: 'secondary' };
            }
            const normalized = String(status).toLowerCase();
            const predefined = ORDER_STATUS_FILTERS.find(item => item.key === normalized);
            if (predefined) {
                return { key: predefined.key, label: predefined.label, badge: predefined.badge };
            }
            switch (normalized) {
                case 'completed':
                    return { key: 'completed', label: 'Hoàn tất', badge: 'success' };
                case 'pending':
                    return { key: 'pending', label: 'Đang xử lý', badge: 'warning' };
                case 'cancelled':
                    return { key: 'cancelled', label: 'Đã hủy', badge: 'danger' };
                default:
                    return { key: normalized, label: normalized.charAt(0).toUpperCase() + normalized.slice(1), badge: 'secondary' };
            }
        }

        function getStatusLabel(status) {
            return getStatusMeta(status).label;
        }

        function getStatusColor(status) {
            return getStatusMeta(status).badge || 'secondary';
        }

        function canCancelOrder(status) {
            if (!status) {
                return false;
            }
            const normalized = status.toString().trim().toLowerCase();
            return normalized === 'new' || normalized === 'confirmed' || normalized === 'shipping';
        }

        function formatCurrency(value) {
            const number = Number(value);
            if (!Number.isFinite(number)) {
                return '0đ';
            }
            return number.toLocaleString('vi-VN') + 'đ';
        }

        function normalizeToDate(value) {
            if (!value) {
                return null;
            }
            if (value instanceof Date) {
                return Number.isNaN(value.getTime()) ? null : value;
            }
            if (typeof value === 'string' || typeof value === 'number') {
                const fromPrimitive = new Date(value);
                if (!Number.isNaN(fromPrimitive.getTime())) {
                    return fromPrimitive;
                }
            }
            if (Array.isArray(value)) {
                const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = value;
                if (Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day)) {
                    const fromArray = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1e6));
                    if (!Number.isNaN(fromArray.getTime())) {
                        return fromArray;
                    }
                }
            }
            if (typeof value === 'object') {
                const dateSource = value.date && typeof value.date === 'object' ? value.date : value;
                const timeSource = value.time && typeof value.time === 'object' ? value.time : value;
                const resolveMonth = input => {
                    if (Number.isFinite(input)) {
                        return input;
                    }
                    if (typeof input === 'string') {
                        const monthNames = ['JANUARY', 'FEBRUARY', 'MARCH', 'APRIL', 'MAY', 'JUNE', 'JULY', 'AUGUST', 'SEPTEMBER', 'OCTOBER', 'NOVEMBER', 'DECEMBER'];
                        const normalized = input.toUpperCase();
                        const index = monthNames.indexOf(normalized);
                        if (index !== -1) {
                            return index + 1;
                        }
                        const parsed = parseInt(normalized, 10);
                        if (Number.isFinite(parsed)) {
                            return parsed;
                        }
                    }
                    return null;
                };
                const toNumber = input => (Number.isFinite(input) ? input : parseInt(input, 10));
                const year = toNumber(dateSource.year);
                const month = resolveMonth(dateSource.monthValue ?? dateSource.month);
                const day = toNumber(dateSource.day ?? dateSource.dayOfMonth);
                if (Number.isFinite(year) && Number.isFinite(month) && Number.isFinite(day)) {
                    const hour = toNumber(timeSource.hour) || 0;
                    const minute = toNumber(timeSource.minute) || 0;
                    const second = toNumber(timeSource.second) || 0;
                    const nano = toNumber(timeSource.nano) || 0;
                    const fromObject = new Date(year, month - 1, day, hour, minute, second, Math.floor(nano / 1e6));
                    if (!Number.isNaN(fromObject.getTime())) {
                        return fromObject;
                    }
                }
            }
            return null;
        }

        function formatDate(value) {
            const date = normalizeToDate(value);
            return date ? date.toLocaleDateString('vi-VN') : '';
        }

        function formatDateTime(value) {
            const date = normalizeToDate(value);
            return date ? date.toLocaleString('vi-VN') : '';
        }

        async function viewOrderDetails(orderId) {
            if (!orderId) {
                return;
            }
            const token = localStorage.getItem('auth_token');
            const container = document.getElementById('orderDetailContent');
            if (container) {
                container.innerHTML = '<div class="text-center py-4 text-muted">Đang tải thông tin đơn hàng...</div>';
            }
            if (orderDetailModal) {
                orderDetailModal.show();
            }
            try {
                const [orderResponse, timelineResponse] = await Promise.all([
                    fetch(`${contextPath}/api/profile/orders/${orderId}`, {
                        headers: { 'Authorization': 'Bearer ' + token }
                    }),
                    fetch(`${contextPath}/api/profile/orders/${orderId}/timeline`, {
                        headers: { 'Authorization': 'Bearer ' + token }
                    })
                ]);
                const orderPayload = await orderResponse.json();
                const timelinePayload = await timelineResponse.json();
                if (!orderResponse.ok || !orderPayload.success) {
                    throw new Error(orderPayload.message || 'Không thể tải chi tiết đơn hàng.');
                }
                if (!timelineResponse.ok || !timelinePayload.success) {
                    throw new Error(timelinePayload.message || 'Không thể tải tiến trình đơn hàng.');
                }
                renderOrderDetail(orderPayload.order, Array.isArray(timelinePayload.timeline) ? timelinePayload.timeline : []);
            } catch (error) {
                console.error('viewOrderDetails error', error);
                if (container) {
                    container.innerHTML = `<div class="alert alert-danger">${escapeHtml(error.message || 'Không thể tải chi tiết đơn hàng')}</div>`;
                }
            }
        }

        async function requestCancelOrder(orderId, orderCode, triggerButton) {
            if (!orderId) {
                return;
            }
            const token = localStorage.getItem('auth_token');
            if (!token) {
                showAlert('Bạn cần đăng nhập để hủy đơn hàng.', 'warning');
                return;
            }
            const confirmMessage = orderCode ? `Bạn có chắc chắn muốn hủy đơn ${orderCode}?` : 'Bạn có chắc chắn muốn hủy đơn hàng này?';
            if (!confirm(confirmMessage)) {
                return;
            }
            let reason = prompt('Nhập lý do hủy đơn (có thể bỏ trống):');
            if (reason) {
                reason = reason.trim();
                if (reason.length > 255) {
                    reason = reason.substring(0, 255);
                }
            }
            if (triggerButton) {
                triggerButton.disabled = true;
                triggerButton.dataset.originalHtml = triggerButton.innerHTML;
                triggerButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang hủy...';
            }
            try {
                const response = await fetch(`${contextPath}/api/profile/orders/${orderId}/cancel`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': 'Bearer ' + token
                    },
                    body: JSON.stringify({ reason: reason || null })
                });
                const payload = await response.json().catch(() => ({ success: false }));
                if (!response.ok || !payload.success) {
                    const message = payload && payload.message ? payload.message : 'Không thể hủy đơn hàng. Vui lòng thử lại.';
                    showAlert(message, 'danger');
                    return;
                }
                showAlert(payload.message || 'Đã hủy đơn hàng.', 'success');
                loadOrderHistory();
                viewOrderDetails(orderId);
            } catch (error) {
                console.error('requestCancelOrder error', error);
                showAlert('Không thể hủy đơn hàng. Vui lòng thử lại sau.', 'danger');
            } finally {
                if (triggerButton) {
                    const original = triggerButton.dataset.originalHtml;
                    triggerButton.innerHTML = original || '<i class="fas fa-times me-1"></i>Hủy đơn';
                    triggerButton.disabled = false;
                    delete triggerButton.dataset.originalHtml;
                }
            }
        }

        function clearReviewError() {
            const alertEl = document.getElementById('reviewFormAlert');
            if (alertEl) {
                alertEl.textContent = '';
                alertEl.classList.add('d-none');
            }
        }

        function showReviewError(message) {
            const alertEl = document.getElementById('reviewFormAlert');
            if (alertEl) {
                alertEl.textContent = message;
                alertEl.classList.remove('d-none');
            }
        }

        function revokeReviewMediaObjectUrl() {
            if (reviewState.previewObjectUrl) {
                URL.revokeObjectURL(reviewState.previewObjectUrl);
                reviewState.previewObjectUrl = null;
            }
        }

        function resetReviewMediaState() {
            revokeReviewMediaObjectUrl();
            reviewState.initialMediaUrl = null;
            reviewState.initialMediaType = null;
            reviewState.mediaUrl = null;
            reviewState.mediaType = null;
            reviewState.mediaFile = null;
            reviewState.removeMedia = false;
            const mediaInput = document.getElementById('reviewMediaInput');
            if (mediaInput) {
                mediaInput.value = '';
            }
            updateReviewMediaPreview();
        }

        function resolveReviewMediaUrl(url) {
            if (!url) {
                return null;
            }
            const trimmed = url.trim();
            if (!trimmed) {
                return null;
            }
            if (trimmed.startsWith('http://') || trimmed.startsWith('https://') || trimmed.startsWith('//')) {
                return trimmed;
            }
            const normalized = trimmed.startsWith('/') ? trimmed : `/${trimmed}`;
            return `${contextPath}${normalized}`;
        }

        function updateReviewMediaPreview() {
            const preview = document.getElementById('reviewMediaPreview');
            const removeBtn = document.getElementById('reviewMediaRemoveBtn');
            if (preview) {
                preview.innerHTML = '';
                preview.classList.add('d-none');
            }
            if (removeBtn) {
                removeBtn.classList.add('d-none');
                removeBtn.disabled = reviewState.loading;
                removeBtn.innerHTML = '<i class="fas fa-times me-1"></i>Xóa nội dung';
            }
            if (!preview || !removeBtn) {
                return;
            }
            if (reviewState.removeMedia && reviewState.mediaUrl) {
                preview.innerHTML = '<div class="text-muted small fst-italic">Tệp đính kèm hiện tại sẽ bị xóa khi bạn lưu.</div>';
                preview.classList.remove('d-none');
                removeBtn.classList.remove('d-none');
                removeBtn.disabled = reviewState.loading;
                removeBtn.innerHTML = '<i class="fas fa-undo me-1"></i>Khôi phục';
                return;
            }
            if (reviewState.mediaFile && reviewState.previewObjectUrl) {
                const file = reviewState.mediaFile;
                const isVideo = file.type && file.type.startsWith('video/');
                if (isVideo) {
                    preview.innerHTML = `<video class="w-100 rounded" controls src="${reviewState.previewObjectUrl}"></video>`;
                } else {
                    preview.innerHTML = `<img class="img-fluid rounded" src="${reviewState.previewObjectUrl}" alt="Tệp đính kèm">`;
                }
                preview.classList.remove('d-none');
                removeBtn.classList.remove('d-none');
                removeBtn.disabled = reviewState.loading;
                removeBtn.innerHTML = '<i class="fas fa-times me-1"></i>Xóa tệp';
                return;
            }
            if (reviewState.mediaUrl) {
                const resolved = resolveReviewMediaUrl(reviewState.mediaUrl);
                if (resolved) {
                    const isVideo = (reviewState.mediaType || '').toLowerCase() === 'video';
                    if (isVideo) {
                        preview.innerHTML = `<video class="w-100 rounded" controls src="${resolved}"></video>`;
                    } else {
                        preview.innerHTML = `<img class="img-fluid rounded" src="${resolved}" alt="Tệp đính kèm">`;
                    }
                    preview.classList.remove('d-none');
                    removeBtn.classList.remove('d-none');
                    removeBtn.disabled = reviewState.loading;
                    removeBtn.innerHTML = '<i class="fas fa-times me-1"></i>Xóa nội dung';
                }
            }
        }

        function handleReviewMediaInputChange(event) {
            if (reviewState.loading) {
                if (event && event.target) {
                    event.target.value = '';
                }
                return;
            }
            clearReviewError();
            const input = event && event.target ? event.target : null;
            const file = input && input.files && input.files.length > 0 ? input.files[0] : null;
            revokeReviewMediaObjectUrl();
            reviewState.mediaFile = null;
            reviewState.previewObjectUrl = null;
            if (!file) {
                reviewState.removeMedia = false;
                updateReviewMediaPreview();
                return;
            }
            const type = file.type || '';
            const isImage = type.startsWith('image/');
            const isVideo = type.startsWith('video/');
            if (!isImage && !isVideo) {
                if (input) {
                    input.value = '';
                }
                showReviewError('Định dạng tệp không được hỗ trợ. Vui lòng chọn ảnh hoặc video.');
                updateReviewMediaPreview();
                return;
            }
            const limit = isImage ? 5 * 1024 * 1024 : 20 * 1024 * 1024;
            if (file.size > limit) {
                if (input) {
                    input.value = '';
                }
                showReviewError(isImage ? 'Ảnh vượt quá dung lượng tối đa 5MB.' : 'Video vượt quá dung lượng tối đa 20MB.');
                updateReviewMediaPreview();
                return;
            }
            reviewState.mediaFile = file;
            reviewState.previewObjectUrl = URL.createObjectURL(file);
            reviewState.removeMedia = false;
            updateReviewMediaPreview();
        }

        function handleReviewMediaRemove(event) {
            if (event) {
                event.preventDefault();
            }
            if (reviewState.loading) {
                return;
            }
            clearReviewError();
            const mediaInput = document.getElementById('reviewMediaInput');
            if (reviewState.removeMedia) {
                reviewState.removeMedia = false;
                updateReviewMediaPreview();
                return;
            }
            if (reviewState.mediaFile) {
                revokeReviewMediaObjectUrl();
                reviewState.mediaFile = null;
                reviewState.previewObjectUrl = null;
                if (mediaInput) {
                    mediaInput.value = '';
                }
                updateReviewMediaPreview();
                return;
            }
            if (reviewState.mediaUrl) {
                reviewState.removeMedia = true;
                if (mediaInput) {
                    mediaInput.value = '';
                }
                updateReviewMediaPreview();
            }
        }

        function resetReviewForm() {
            const form = document.getElementById('reviewForm');
            if (form) {
                form.reset();
            }
            const ratingEl = document.getElementById('reviewRating');
            if (ratingEl) {
                ratingEl.value = '5';
            }
            const contentEl = document.getElementById('reviewContent');
            if (contentEl) {
                contentEl.value = '';
            }
            clearReviewError();
            const existingInfoEl = document.getElementById('reviewExistingInfo');
            if (existingInfoEl) {
                existingInfoEl.textContent = '';
                existingInfoEl.classList.add('d-none');
            }
            resetReviewMediaState();
            reviewState.reviewId = null;
        }

        function setReviewFormLoading(loading) {
            reviewState.loading = loading;
            const submitBtn = document.getElementById('reviewSubmitBtn');
            if (submitBtn) {
                if (loading) {
                    submitBtn.disabled = true;
                    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang lưu...';
                } else {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-paper-plane me-1"></i>Gửi bình luận';
                }
            }
            const ratingEl = document.getElementById('reviewRating');
            const contentEl = document.getElementById('reviewContent');
            if (ratingEl) {
                ratingEl.disabled = loading;
            }
            if (contentEl) {
                contentEl.disabled = loading;
            }
            const mediaInput = document.getElementById('reviewMediaInput');
            if (mediaInput) {
                mediaInput.disabled = loading;
            }
            const removeBtn = document.getElementById('reviewMediaRemoveBtn');
            if (removeBtn) {
                removeBtn.disabled = loading;
            }
            updateReviewMediaPreview();
        }

        async function openReviewModal(order, item, orderCodeDisplay) {
            if (!item || !item.bookId) {
                return;
            }
            resetReviewForm();
            reviewState.bookId = item.bookId;
            reviewState.orderId = order ? order.id : null;
            reviewState.orderItemId = item.id || null;
            reviewState.bookTitle = item.title || 'Sản phẩm';
            reviewState.orderCode = orderCodeDisplay || (order && order.code ? order.code : '#');

            const titleEl = document.getElementById('reviewModalTitle');
            const subtitleEl = document.getElementById('reviewModalSubtitle');
            if (titleEl) {
                titleEl.textContent = 'Viết bình luận sản phẩm';
            }
            if (subtitleEl) {
                subtitleEl.textContent = `Đơn ${orderCodeDisplay} · ${reviewState.bookTitle}`;
            }
            const bookIdEl = document.getElementById('reviewBookId');
            if (bookIdEl) {
                bookIdEl.value = reviewState.bookId;
            }

            if (reviewModal) {
                reviewModal.show();
            }

            const alertEl = document.getElementById('reviewFormAlert');
            if (alertEl) {
                alertEl.classList.add('d-none');
            }

            const token = localStorage.getItem('auth_token');
            if (!token) {
                if (alertEl) {
                    alertEl.textContent = 'Bạn cần đăng nhập để bình luận sản phẩm.';
                    alertEl.classList.remove('d-none');
                }
                return;
            }

            setReviewFormLoading(true);
            try {
                const response = await fetch(`${contextPath}/api/reviews/me?bookId=${reviewState.bookId}`, {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                if (!response.ok) {
                    if (response.status === 401) {
                        if (alertEl) {
                            alertEl.textContent = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
                            alertEl.classList.remove('d-none');
                        }
                        return;
                    }
                    const payload = await response.json().catch(() => ({ message: 'Không thể tải đánh giá của bạn.' }));
                    if (alertEl) {
                        alertEl.textContent = payload.message || 'Không thể tải đánh giá của bạn.';
                        alertEl.classList.remove('d-none');
                    }
                    return;
                }
                const payload = await response.json();
                if (payload && payload.success && payload.review) {
                    if (document.getElementById('reviewRating')) {
                        document.getElementById('reviewRating').value = payload.review.rating || '5';
                    }
                    if (document.getElementById('reviewContent')) {
                        document.getElementById('reviewContent').value = payload.review.content || '';
                    }
                    const existingInfoEl = document.getElementById('reviewExistingInfo');
                    if (existingInfoEl) {
                        existingInfoEl.textContent = 'Bạn đã từng bình luận sản phẩm này. Bạn có thể chỉnh sửa nội dung và gửi lại.';
                        existingInfoEl.classList.remove('d-none');
                    }
                    reviewState.reviewId = payload.review.id || null;
                    reviewState.initialMediaUrl = payload.review.mediaUrl || null;
                    reviewState.initialMediaType = payload.review.mediaType || null;
                    reviewState.mediaUrl = reviewState.initialMediaUrl;
                    reviewState.mediaType = reviewState.initialMediaType;
                    reviewState.mediaFile = null;
                    reviewState.removeMedia = false;
                    revokeReviewMediaObjectUrl();
                    updateReviewMediaPreview();
                }
            } catch (error) {
                console.error('openReviewModal error', error);
                if (alertEl) {
                    alertEl.textContent = 'Không thể tải đánh giá của bạn. Vui lòng thử lại sau.';
                    alertEl.classList.remove('d-none');
                }
            } finally {
                setReviewFormLoading(false);
            }
        }

        async function submitReview() {
            if (!reviewState.bookId || reviewState.loading) {
                return;
            }
            const ratingEl = document.getElementById('reviewRating');
            const contentEl = document.getElementById('reviewContent');
            const rating = ratingEl ? parseInt(ratingEl.value, 10) : 0;
            const content = contentEl ? contentEl.value.trim() : '';

            clearReviewError();
            if (!rating || rating < 1 || rating > 5) {
                showReviewError('Vui lòng chọn số sao hợp lệ.');
                return;
            }
            if (!content || content.length < 50) {
                showReviewError('Nội dung bình luận phải có ít nhất 50 ký tự.');
                return;
            }

            const token = localStorage.getItem('auth_token');
            if (!token) {
                showReviewError('Bạn cần đăng nhập để bình luận sản phẩm.');
                return;
            }

            const formData = new FormData();
            formData.append('bookId', reviewState.bookId);
            formData.append('rating', rating);
            formData.append('content', content);
            if (reviewState.mediaFile) {
                formData.append('media', reviewState.mediaFile);
            } else if (reviewState.mediaUrl && !reviewState.removeMedia) {
                formData.append('mediaUrl', reviewState.mediaUrl);
                if (reviewState.mediaType) {
                    formData.append('mediaType', reviewState.mediaType);
                }
            }
            if (reviewState.removeMedia) {
                formData.append('removeMedia', 'true');
            }

            setReviewFormLoading(true);
            try {
                const response = await fetch(`${contextPath}/api/reviews`, {
                    method: 'POST',
                    headers: {
                        'Authorization': 'Bearer ' + token
                    },
                    body: formData
                });
                const payload = await response.json().catch(() => ({ success: false }));
                if (!response.ok || !payload.success) {
                    const message = payload && payload.message ? payload.message : 'Không thể lưu bình luận. Vui lòng thử lại.';
                    showReviewError(message);
                    return;
                }
                if (payload.review) {
                    reviewState.reviewId = payload.review.id || null;
                    reviewState.initialMediaUrl = payload.review.mediaUrl || null;
                    reviewState.initialMediaType = payload.review.mediaType || null;
                    reviewState.mediaUrl = reviewState.initialMediaUrl;
                    reviewState.mediaType = reviewState.initialMediaType;
                    reviewState.removeMedia = false;
                    reviewState.mediaFile = null;
                    revokeReviewMediaObjectUrl();
                    updateReviewMediaPreview();
                }
                showAlert('Đã lưu bình luận cho sản phẩm.', 'success');
                if (reviewModal) {
                    reviewModal.hide();
                }
            } catch (error) {
                console.error('submitReview error', error);
                showReviewError('Không thể lưu bình luận. Vui lòng thử lại sau.');
            } finally {
                setReviewFormLoading(false);
            }
        }

        function renderOrderDetail(order, timeline) {
            const container = document.getElementById('orderDetailContent');
            if (!container) {
                return;
            }
            if (!order) {
                container.innerHTML = '<div class="text-center py-4 text-muted">Không tìm thấy thông tin đơn hàng.</div>';
                return;
            }
            const orderCode = order.code && order.code.trim() ? order.code.trim() : `#${order.id}`;
            const statusMeta = getStatusMeta(order.status);
            const badgeClass = statusMeta.badge ? `bg-${statusMeta.badge}` : 'bg-secondary';
            const allowReview = statusMeta.key === 'delivered';
            const isCancellable = canCancelOrder(order.status);

            let itemsHtml = '<p class="text-muted mb-0">Danh sách sản phẩm trống.</p>';
            if (Array.isArray(order.items) && order.items.length > 0) {
                const rows = order.items.map((item, idx) => `
                    <tr>
                        <td>
                            <div class="fw-semibold">${escapeHtml(item.title || 'Sản phẩm')}</div>
                            ${item.author ? `<div class="text-muted small">${escapeHtml(item.author)}</div>` : ''}
                            ${item.shopName ? `<div class="text-muted small"><i class="fas fa-store me-1"></i>${escapeHtml(item.shopName)}</div>` : ''}
                        </td>
                        <td class="text-center">${item.quantity}</td>
                        <td class="text-end">${formatCurrency(item.unitPrice)}</td>
                        <td class="text-end">${formatCurrency(item.totalPrice)}</td>
                        ${allowReview ? `<td class="text-end" style="width: 160px;">
                            <button type="button" class="btn btn-sm btn-outline-primary" data-review-trigger data-item-index="${idx}">
                                <i class="fas fa-pen me-1"></i>Bình luận
                            </button>
                        </td>` : ''}
                    </tr>
                `).join('');
                itemsHtml = `
                    <div class="table-responsive">
                        <table class="table table-sm align-middle">
                            <thead>
                                <tr>
                                    <th>Sản phẩm</th>
                                    <th class="text-center" style="width: 80px;">SL</th>
                                    <th class="text-end">Đơn giá</th>
                                    <th class="text-end">Thành tiền</th>
                                    ${allowReview ? '<th class="text-end" style="width: 160px;">Bình luận</th>' : ''}
                                </tr>
                            </thead>
                            <tbody>${rows}</tbody>
                        </table>
                    </div>
                `;
            }

            let timelineHtml = '<p class="text-muted mb-0">Chưa có tiến trình trạng thái.</p>';
            if (Array.isArray(timeline) && timeline.length > 0) {
                timelineHtml = '<div class="list-group list-group-flush">' + timeline.map(entry => {
                    const meta = getStatusMeta(entry.status);
                    const noteLine = entry.note ? `<div class="text-muted small">${escapeHtml(entry.note)}</div>` : '';
                    const createdByLine = entry.createdBy ? `<div class="text-muted small">${escapeHtml(entry.createdBy)}</div>` : '';
                    return `
                        <div class="list-group-item px-0">
                            <div class="d-flex justify-content-between gap-3">
                                <div>
                                    <div class="fw-semibold">${escapeHtml(meta.label)}</div>
                                    ${noteLine}
                                    ${createdByLine}
                                </div>
                                <div class="text-muted small text-end" style="min-width: 140px;">${formatDateTime(entry.createdAt)}</div>
                            </div>
                        </div>
                    `;
                }).join('') + '</div>';
            }

            container.innerHTML = `
                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-3">
                    <div>
                        <h5 class="mb-1">Đơn hàng ${escapeHtml(orderCode)}</h5>
                        <div class="text-muted small">Ngày đặt: ${formatDateTime(order.orderDate)}</div>
                        ${order.paymentMethod ? `<div class="text-muted small">Thanh toán: ${escapeHtml(order.paymentMethod.toUpperCase())}</div>` : ''}
                        ${order.couponCode ? `<div class="text-muted small">Mã giảm giá: ${escapeHtml(order.couponCode)}</div>` : ''}
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        ${isCancellable ? `<button type="button" class="btn btn-outline-danger" data-order-cancel><i class="fas fa-times me-1"></i>Hủy đơn</button>` : ''}
                        <span class="badge ${badgeClass} fs-6">${escapeHtml(statusMeta.label)}</span>
                    </div>
                </div>
                <div class="border rounded-3 p-3 bg-light mb-4">
                    <div class="d-flex justify-content-between mb-1"><span>Tạm tính</span><span>${formatCurrency(order.itemsSubtotal)}</span></div>
                    <div class="d-flex justify-content-between mb-1"><span>Giảm giá</span><span>- ${formatCurrency(order.discountAmount)}</span></div>
                    <div class="d-flex justify-content-between mb-1"><span>Phí vận chuyển</span><span>${formatCurrency(order.shippingFee)}</span></div>
                    <hr class="my-2">
                    <div class="d-flex justify-content-between fw-semibold"><span>Tổng thanh toán</span><span>${formatCurrency(order.totalAmount)}</span></div>
                </div>
                <h6 class="mb-2">Sản phẩm</h6>
                ${itemsHtml}
                <h6 class="mt-4 mb-2">Tiến trình đơn hàng</h6>
                ${timelineHtml}
                ${order.notes ? `<div class="mt-4"><strong>Ghi chú:</strong> ${escapeHtml(order.notes)}</div>` : ''}
            `;

            if (isCancellable) {
                const cancelBtn = container.querySelector('[data-order-cancel]');
                if (cancelBtn) {
                    cancelBtn.addEventListener('click', () => requestCancelOrder(order.id, orderCode, cancelBtn));
                }
            }

            if (allowReview && Array.isArray(order.items) && order.items.length > 0) {
                const buttons = container.querySelectorAll('[data-review-trigger]');
                buttons.forEach(btn => {
                    const index = parseInt(btn.getAttribute('data-item-index'), 10);
                    if (!Number.isInteger(index) || !order.items[index]) {
                        return;
                    }
                    btn.addEventListener('click', () => openReviewModal(order, order.items[index], orderCode));
                });
            }
        }

        function deleteAccount() {
            if (!confirm('Bạn có chắc chắn muốn xóa tài khoản? Hành động này không thể hoàn tác!')) {
                return;
            }

            const formData = new FormData(document.getElementById('deleteAccountForm'));
            const deleteData = Object.fromEntries(formData);

            const token = localStorage.getItem('auth_token');
            fetch(`${contextPath}/api/profile/delete`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                body: JSON.stringify(deleteData)
            })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    showAlert('Tài khoản đã được xóa thành công. Bạn sẽ được chuyển về trang chủ.', 'success');
                    setTimeout(() => {
                        window.location.href = `${contextPath}/`;
                    }, 2000);
                } else {
                    showAlert('Lỗi: ' + data.message, 'danger');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                showAlert('Lỗi kết nối. Vui lòng thử lại.', 'danger');
            });
        }

        function showAlert(message, type) {
            const alertDiv = document.createElement('div');
            alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
            alertDiv.innerHTML = `
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            `;
            
            document.getElementById('alertContainer').appendChild(alertDiv);
            
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 5000);
        }
    </script>
    <%@ include file="/WEB-INF/includes/footer.jsp" %>
    </html>
