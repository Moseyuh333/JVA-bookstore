<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Hồ sơ của tôi" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<!-- Load Bootstrap CSS locally for this page (kept for existing layout) -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">

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

        // Check authentication on page load
        document.addEventListener('DOMContentLoaded', function() {
            const token = localStorage.getItem('auth_token');
            if (!token) {
                // Redirect to login if not authenticated
                alert('Vui lòng đăng nhập để truy cập trang này.');
                window.location.href = `${contextPath}/login.jsp`;
                return;
            }
            
            loadUserProfile();
            initAddressManager();
            loadOrderHistory();
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

        function loadOrderHistory() {
            const token = localStorage.getItem('auth_token');
            fetch(`${contextPath}/api/profile/orders`, {
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            })
                .then(response => response.json())
                .then(data => {
                    if (data.success) {
                        displayOrderHistory(data.orders);
                    } else {
                        document.getElementById('orderHistoryContent').innerHTML = 
                            '<div class="text-center py-4"><p>Không thể tải lịch sử đơn hàng</p></div>';
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    document.getElementById('orderHistoryContent').innerHTML = 
                        '<div class="text-center py-4"><p>Lỗi kết nối</p></div>';
                });
        }

        function displayOrderHistory(orders) {
            const container = document.getElementById('orderHistoryContent');
            
            if (orders.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4">
                        <i class="fas fa-shopping-bag fa-3x text-muted mb-3"></i>
                        <p class="text-muted">Bạn chưa có đơn hàng nào</p>
                        <a href="<%= request.getContextPath() %>/" class="btn btn-primary">Mua sắm ngay</a>
                    </div>
                `;
                return;
            }

            let html = '<div class="table-responsive"><table class="table table-striped">';
            html += '<thead><tr><th>Mã đơn hàng</th><th>Ngày đặt</th><th>Tổng tiền</th><th>Trạng thái</th><th>Chi tiết</th></tr></thead><tbody>';
            
            orders.forEach(order => {
                html += `
                    <tr>
                        <td>#${order.id}</td>
                        <td>${new Date(order.orderDate).toLocaleDateString('vi-VN')}</td>
                        <td>${order.totalAmount.toLocaleString('vi-VN')}đ</td>
                        <td><span class="badge bg-${getStatusColor(order.status)}">${order.status}</span></td>
                        <td><button class="btn btn-sm btn-outline-primary" onclick="viewOrderDetails(${order.id})">Xem</button></td>
                    </tr>
                `;
            });
            
            html += '</tbody></table></div>';
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

        function getStatusColor(status) {
            switch(status) {
                case 'completed': return 'success';
                case 'pending': return 'warning';
                case 'cancelled': return 'danger';
                default: return 'secondary';
            }
        }

        function viewOrderDetails(orderId) {
            // TODO: Implement order details modal
            showAlert('Chức năng xem chi tiết đơn hàng sẽ được triển khai sau', 'info');
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