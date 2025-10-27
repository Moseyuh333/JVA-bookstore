
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- Giả định biến shopId và username đã được set từ Servlet --%>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cài đặt Shop - ${username}</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@4.6.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        /* Thêm CSS tùy chỉnh cho card và form */
        .setting-card { background: white; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); padding: 30px; margin-bottom: 25px; }
        .setting-card h2 { font-size: 20px; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="container mt-5">
        <div class="mb-3">
            <a href="${pageContext.request.contextPath}/seller/dashboard" style="color: #c96d28; text-decoration: none; font-weight: 600; display: inline-flex; align-items: center; gap: 8px; transition: all 0.3s ease;">
                <i class="fas fa-arrow-left"></i> Quay lại
            </a>
        </div>
        <h1><i class="fas fa-cog mr-2"></i>Cài đặt Shop</h1>
        <p class="text-muted">Quản lý thông tin và cấu hình thanh toán/vận chuyển của Shop ID: <strong>${shopId}</strong></p>

        <%-- Thông báo chung --%>
        <div id="alertContainer"></div>

        <%-- Card 1: Cài đặt Thông tin Cơ bản --%>
        <div class="setting-card">
            <h2>Thông tin Cơ bản</h2>
            <form id="shopProfileForm">
                <input type="hidden" name="shopId" value="${shopId}">
                
                <div class="form-group">
                    <label for="shopName">Tên Shop</label>
                    <input type="text" class="form-control" id="shopName" name="name" required>
                </div>
                
                <div class="form-group">
                    <label for="shopAddress">Địa chỉ Shop</label>
                    <input type="text" class="form-control" id="shopAddress" name="address">
                </div>
                
                <div class="form-group">
                    <label for="shopDescription">Mô tả Shop</label>
                    <textarea class="form-control" id="shopDescription" name="description" rows="3"></textarea>
                </div>

                <button type="submit" class="btn btn-primary"><i class="fas fa-save mr-2"></i>Lưu Thay Đổi</button>
            </form>
            <div id="loadingProfile" class="spinner-border spinner-border-sm mt-3 d-none"></div>
        </div>

        <%-- Card 2: Cấu hình Chiết khấu/Thanh toán --%>
        <div class="setting-card">
            <h2>Cấu hình Chiết khấu/Thanh toán</h2>
            <p class="text-muted small">Thiết lập tỉ lệ chiết khấu áp dụng cho các đơn hàng của shop bạn.</p>
            <div class="mb-3">
                <strong>Tỷ lệ chiết khấu hiện tại:</strong>
                <span id="commissionRateDisplay">--%</span>
            </div>
            <form id="commissionForm" class="row g-3">
                <div class="col-md-6">
                    <label for="commissionRateInput">Tỷ lệ chiết khấu (%)</label>
                    <div class="input-group">
                        <input type="number" min="0" max="100" step="0.1" class="form-control" id="commissionRateInput" name="commissionRate" placeholder="Ví dụ: 10 cho 10%" required>
                        <div class="input-group-append">
                            <span class="input-group-text">%</span>
                        </div>
                    </div>
                </div>
                <div class="col-md-6 d-flex align-items-end">
                    <button type="submit" class="btn btn-outline-primary">
                        <i class="fas fa-save mr-1"></i>Lưu chiết khấu
                    </button>
                </div>
            </form>
        </div>

        <%-- Card 3: Quản lý mã giảm giá --%>
        <div class="setting-card">
            <h2>Quản lý mã giảm giá</h2>
            <p class="text-muted small">Tạo và quản lý các mã khuyến mãi áp dụng riêng cho shop của bạn.</p>

            <form id="couponForm" class="row">
                <div class="form-group col-md-3">
                    <label for="couponCode">Mã giảm giá</label>
                    <input type="text" class="form-control" id="couponCode" name="code" placeholder="VD: SALE10" required>
                </div>
                <div class="form-group col-md-3">
                    <label for="discountType">Loại giảm</label>
                    <select class="form-control" id="discountType" name="discountType">
                        <option value="percentage">Phần trăm (%)</option>
                        <option value="fixed">Số tiền cố định</option>
                    </select>
                </div>
                <div class="form-group col-md-3">
                    <label for="discountValue">Giá trị giảm</label>
                    <input type="number" class="form-control" id="discountValue" name="discountValue" min="0" step="0.01" required>
                </div>
                <div class="form-group col-md-3">
                    <label for="minimumOrder">Đơn hàng tối thiểu</label>
                    <input type="number" class="form-control" id="minimumOrder" name="minimumOrder" min="0" step="0.01">
                </div>
                <div class="form-group col-md-3">
                    <label for="usageLimit">Giới hạn sử dụng</label>
                    <input type="number" class="form-control" id="usageLimit" name="usageLimit" min="1" placeholder="Để trống nếu không giới hạn">
                </div>
                <div class="form-group col-md-3">
                    <label for="startDate">Ngày bắt đầu</label>
                    <input type="date" class="form-control" id="startDate" name="startDate">
                </div>
                <div class="form-group col-md-3">
                    <label for="endDate">Ngày kết thúc</label>
                    <input type="date" class="form-control" id="endDate" name="endDate">
                </div>
                <div class="form-group col-md-6">
                    <label for="couponDescription">Mô tả</label>
                    <input type="text" class="form-control" id="couponDescription" name="description" placeholder="Nội dung mô tả ngắn gọn">
                </div>
                <div class="form-group col-md-12 text-right">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-plus mr-1"></i>Tạo mã giảm giá
                    </button>
                </div>
            </form>

            <div class="table-responsive mt-4">
                <table class="table table-striped table-bordered">
                    <thead class="thead-light">
                        <tr>
                            <th>Mã</th>
                            <th>Loại</th>
                            <th>Giá trị</th>
                            <th>Đơn tối thiểu</th>
                            <th>Giới hạn</th>
                            <th>Hiệu lực</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody id="couponTableBody"></tbody>
                </table>
                <div id="couponEmptyState" class="text-center text-muted" style="display: none;">
                    Chưa có mã giảm giá nào.
                </div>
            </div>
        </div>
    </div>
    
    <script>
        const API_URL = '<%= request.getContextPath() %>/api/seller/profile';
        const COUPON_API_URL = '<%= request.getContextPath() %>/api/seller/coupons';
        const SHOP_ID = '<c:out value="${shopId}" default="0" />';
        const HAS_SHOP = SHOP_ID && SHOP_ID !== '0';
        const commissionDisplay = document.getElementById('commissionRateDisplay');
        const commissionInput = document.getElementById('commissionRateInput');
        const commissionForm = document.getElementById('commissionForm');
        const couponForm = document.getElementById('couponForm');
        const couponsTableBody = document.getElementById('couponTableBody');
        const couponEmptyState = document.getElementById('couponEmptyState');

        function escapeHtml(value) {
            if (value === null || value === undefined) {
                return '';
            }
            return String(value)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        async function loadShopProfile() {
            const loader = document.getElementById('loadingProfile');
            if (loader) {
                loader.classList.remove('d-none');
            }

            if (!HAS_SHOP) {
                showAlert('Bạn chưa hoàn tất đăng ký shop. Vui lòng tạo shop để cập nhật thông tin.', 'warning');
                if (loader) loader.classList.add('d-none');
                return;
            }

            try {
                const response = await fetch(API_URL + '?action=get');
                const data = await response.json();

                if (data.success && data.shop) {
                    document.getElementById('shopName').value = data.shop.name || '';
                    document.getElementById('shopAddress').value = data.shop.address || '';
                    document.getElementById('shopDescription').value = data.shop.description || '';

                    const commissionRaw = data.shop.commissionRate;
                    let rateText = '0.00';
                    if (commissionRaw !== null && commissionRaw !== undefined) {
                        const numeric = Number(commissionRaw);
                        if (!Number.isNaN(numeric)) {
                            const clamped = Math.min(100, Math.max(0, numeric));
                            rateText = clamped.toFixed(2);
                        }
                    }
                    if (commissionDisplay) {
                        commissionDisplay.textContent = rateText + '%';
                    }
                    if (commissionInput) {
                        commissionInput.value = rateText;
                    }
                } else {
                    showAlert('Không thể tải thông tin Shop: ' + (data.message || 'Lỗi kết nối.'), 'danger');
                }
            } catch (error) {
                console.error('loadShopProfile error:', error);
                showAlert('Lỗi mạng khi tải thông tin shop.', 'danger');
            } finally {
                if (loader) {
                    loader.classList.add('d-none');
                }
        }
    }

    async function loadCoupons() {
        if (!HAS_SHOP || !couponsTableBody) {
            return;
        }
        try {
            const response = await fetch(COUPON_API_URL + '?action=list');
            const data = await response.json();
            if (data.success) {
                renderCoupons(data.coupons || []);
            } else {
                showAlert('Không thể tải danh sách mã giảm giá: ' + (data.message || 'Lỗi không xác định'), 'danger');
            }
        } catch (error) {
            console.error('loadCoupons error:', error);
            showAlert('Lỗi mạng khi tải mã giảm giá.', 'danger');
        }
    }

    function renderCoupons(coupons) {
        if (!couponsTableBody) {
            return;
        }
        couponsTableBody.innerHTML = '';
        if (!coupons || coupons.length === 0) {
            if (couponEmptyState) {
                couponEmptyState.style.display = 'block';
            }
            return;
        }
        if (couponEmptyState) {
            couponEmptyState.style.display = 'none';
        }

        coupons.forEach((coupon) => {
            const row = document.createElement('tr');
            const usageText = coupon.usageLimit
                ? (String(coupon.usedCount || 0) + '/' + coupon.usageLimit)
                : (coupon.usedCount || 0);
            const valueText = coupon.discountType === 'percentage'
                ? Number(coupon.discountValue || 0).toFixed(2) + '%'
                : formatCurrency(coupon.discountValue);
            const minOrderText = coupon.minimumOrder ? formatCurrency(coupon.minimumOrder) : '-';
            const dateRange = buildDateRange(coupon.startDate, coupon.endDate);
            const statusText = escapeHtml(coupon.status || 'active');
            const codeText = escapeHtml(coupon.code);
            const dateRangeText = escapeHtml(dateRange);

            row.innerHTML =
                '<td>' + codeText + '</td>' +
                '<td>' + (coupon.discountType === 'percentage' ? 'Phần trăm' : 'Cố định') + '</td>' +
                '<td>' + valueText + '</td>' +
                '<td>' + minOrderText + '</td>' +
                '<td>' + usageText + '</td>' +
                '<td>' + dateRangeText + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' +
                    '<button type="button" class="btn btn-sm btn-danger" onclick="deleteCoupon(' + coupon.id + ')">' +
                        '<i class="fas fa-trash-alt"></i>' +
                    '</button>' +
                '</td>';
            couponsTableBody.appendChild(row);
        });
    }

    function buildDateRange(start, end) {
        const format = function(value) {
            if (!value) return '-';
            const date = new Date(value);
            if (Number.isNaN(date.getTime())) {
                return value;
            }
            return date.toLocaleDateString('vi-VN');
        };
        const startText = format(start);
        const endText = format(end);
        if (startText === '-' && endText === '-') {
            return 'Không giới hạn';
        }
        return startText + ' - ' + endText;
    }

    function formatCurrency(value) {
        const number = Number(value);
        if (Number.isNaN(number)) {
            return value || '0';
        }
        return new Intl.NumberFormat('vi-VN').format(number) + '₫';
    }

        document.getElementById('shopProfileForm').addEventListener('submit', async function (e) {
            e.preventDefault();

            if (!HAS_SHOP) {
                showAlert('Bạn chưa có shop để cập nhật thông tin.', 'warning');
                return;
            }

            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.disabled = true;
            }

            const formData = new FormData(this);
            const payload = new URLSearchParams();
            for (const [key, value] of formData.entries()) {
                payload.append(key, (value || '').toString().trim());
            }

            try {
                const response = await fetch(API_URL + '?action=update', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: payload.toString()
                });

                const result = await response.json();
                if (result.success) {
                    showAlert('Cập nhật thông tin Shop thành công!', 'success');
                    await loadShopProfile();
                } else {
                    showAlert('Lỗi: ' + (result.message || 'Không thể cập nhật thông tin Shop'), 'danger');
                }
            } catch (error) {
                console.error('updateShopProfile error:', error);
                showAlert('Có lỗi xảy ra khi cập nhật thông tin Shop', 'danger');
            } finally {
                if (submitBtn) {
                    submitBtn.disabled = false;
                }
            }
        });

        if (commissionForm) {
            commissionForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                if (!HAS_SHOP) {
                    showAlert('Bạn chưa có shop để cập nhật chiết khấu.', 'warning');
                    return;
                }

                const value = commissionInput ? commissionInput.value : '';
                if (!value) {
                    showAlert('Vui lòng nhập tỉ lệ chiết khấu.', 'warning');
                    return;
                }

                const submitBtn = commissionForm.querySelector('button[type="submit"]');
                if (submitBtn) {
                    submitBtn.disabled = true;
                }

                const payload = new URLSearchParams();
                payload.append('action', 'update_commission');
                payload.append('commissionRate', value);

                try {
                    const response = await fetch(API_URL, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                        },
                        body: payload.toString()
                    });
                    const result = await response.json();
                    if (result.success) {
                        const latestRate = Number(result.commissionRate);
                        if (Number.isNaN(latestRate)) {
                            const fallback = parseFloat(value).toFixed(2);
                            if (commissionDisplay) {
                                commissionDisplay.textContent = `${fallback}%`;
                            }
                            if (commissionInput) {
                                commissionInput.value = fallback;
                            }
                        } else {
                            const rateText = latestRate.toFixed(2);
                            if (commissionDisplay) {
                                commissionDisplay.textContent = `${rateText}%`;
                            }
                            if (commissionInput) {
                                commissionInput.value = rateText;
                            }
                        }
                        showAlert('Cập nhật tỉ lệ chiết khấu thành công!', 'success');
                    } else {
                        showAlert('Lỗi: ' + (result.message || 'Không thể cập nhật chiết khấu'), 'danger');
                    }
                } catch (error) {
                    console.error('updateCommissionRate error:', error);
                    showAlert('Có lỗi xảy ra khi cập nhật chiết khấu', 'danger');
                } finally {
                    if (submitBtn) {
                        submitBtn.disabled = false;
                    }
                }
            });
        }

        if (couponForm) {
            couponForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                if (!HAS_SHOP) {
                    showAlert('Bạn chưa có shop để tạo mã giảm giá.', 'warning');
                    return;
                }

                const submitBtn = couponForm.querySelector('button[type="submit"]');
                if (submitBtn) {
                    submitBtn.disabled = true;
                }

                const formData = new FormData(couponForm);
                const payload = new URLSearchParams();
                payload.append('action', 'create');
                for (const [key, value] of formData.entries()) {
                    payload.append(key, (value || '').toString().trim());
                }

                try {
                    const response = await fetch(COUPON_API_URL, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                        },
                        body: payload.toString()
                    });
                    const result = await response.json();
                    if (result.success) {
                        showAlert('Tạo mã giảm giá thành công!', 'success');
                        couponForm.reset();
                        await loadCoupons();
                    } else {
                        showAlert('Lỗi: ' + (result.message || 'Không thể tạo mã giảm giá'), 'danger');
                    }
                } catch (error) {
                    console.error('createCoupon error:', error);
                    showAlert('Có lỗi xảy ra khi tạo mã giảm giá', 'danger');
                } finally {
                    if (submitBtn) {
                        submitBtn.disabled = false;
                    }
                }
            });
        }

        window.deleteCoupon = async (couponId) => {
            if (!couponId || !HAS_SHOP) {
                return;
            }
            if (!confirm('Bạn có chắc chắn muốn xóa mã giảm giá này?')) {
                return;
            }
            try {
                const payload = new URLSearchParams();
                payload.append('action', 'delete');
                payload.append('couponId', couponId);

                const response = await fetch(COUPON_API_URL, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'
                    },
                    body: payload.toString()
                });
                const result = await response.json();
                if (result.success) {
                    showAlert('Đã xóa mã giảm giá.', 'success');
                    await loadCoupons();
                } else {
                    showAlert('Lỗi: ' + (result.message || 'Không thể xóa mã giảm giá'), 'danger');
                }
            } catch (error) {
                console.error('deleteCoupon error:', error);
                showAlert('Có lỗi xảy ra khi xóa mã giảm giá', 'danger');
            }
        };

        function showAlert(message, type) {
            const safeMessage = escapeHtml(message);
            document.getElementById('alertContainer').innerHTML =
                '<div class="alert alert-' + type + ' alert-dismissible fade show" role="alert">' +
                safeMessage +
                '<button type="button" class="close" data-dismiss="alert"><span>&times;</span></button></div>';
        }

        document.addEventListener('DOMContentLoaded', () => {
            loadShopProfile();
            loadCoupons();
        });
    </script>
</body>
</html>

