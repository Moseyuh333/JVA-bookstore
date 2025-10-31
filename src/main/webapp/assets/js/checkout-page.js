(function (window, document) {
    'use strict';

    var appShell = window.appShell;
    var cartClient = window.cartClient;
    var apiClient = window.apiClient;
    if (!appShell || !cartClient || !apiClient) {
        return;
    }

    var MODE_BUY_NOW = 'buy-now';
    var MODE_CART = 'cart';
    var shippingState = {
        fee: 0,
        shipper: null,
        loading: false,
        lastAddressId: null,
        error: null,
        requestToken: 0
    };

    var addressListEl;
    var orderItemsEl;
    var itemsCountEl;
    var subtotalEl;
    var discountEl;
    var shippingEl;
    var shippingMethodEl;
    var totalEl;
    var feedbackEl;
    var placeOrderBtn;
    var notesEl;
    var couponInputEl;
    var couponSelectEl;
    var couponApplyBtn;
    var couponFeedbackEl;
    var walletDetailsEl;
    var walletCardNumberEl;
    var walletExpiryMonthEl;
    var walletExpiryYearEl;
    var walletCvvEl;

    var mode = MODE_CART;
    var cartState = {
        items: [],
        selected: new Set(),
        activeShopId: null,
        activeShopName: '',
        lastFetchedShopId: null,
        subtotal: 0
    };

    var couponState = {
        coupons: [],
        selectedCode: null,
        selectedCoupon: null,
        lastSubtotal: 0,
        discount: 0,
        loadedShopId: null
    };

    appShell.onReady(function () {
        addressListEl = document.querySelector('[data-checkout-address-list]');
        orderItemsEl = document.querySelector('[data-checkout-order-items]');
        itemsCountEl = document.getElementById('checkoutItemsCount');
        subtotalEl = document.getElementById('checkoutSubtotal');
        discountEl = document.getElementById('checkoutDiscount');
        shippingEl = document.getElementById('checkoutShipping');
        shippingMethodEl = document.getElementById('checkoutShippingMethod');
        totalEl = document.getElementById('checkoutTotal');
        feedbackEl = document.getElementById('checkoutFeedback');
        placeOrderBtn = document.getElementById('placeOrderBtn');
        notesEl = document.getElementById('checkoutNotes');
        couponInputEl = document.getElementById('checkoutCouponInput');
        couponSelectEl = document.getElementById('checkoutCouponSelect');
        couponApplyBtn = document.getElementById('applyCouponBtn');
        couponFeedbackEl = document.getElementById('couponFeedback');
        walletDetailsEl = document.querySelector('[data-wallet-details]');
        walletCardNumberEl = document.getElementById('walletCardNumber');
        walletExpiryMonthEl = document.getElementById('walletExpiryMonth');
        walletExpiryYearEl = document.getElementById('walletExpiryYear');
        walletCvvEl = document.getElementById('walletCvv');

        mode = getQueryParam('mode') === MODE_BUY_NOW ? MODE_BUY_NOW : MODE_CART;

        bindSelectionEvents();
        bindPaymentMethodEvents();
        bindPlaceOrder();
        bindCouponEvents();
        bindAddressEvents();

        if (mode === MODE_CART && cartClient && typeof cartClient.onChange === 'function') {
            cartClient.onChange(function (cart) {
                if (mode !== MODE_CART) {
                    return;
                }
                renderCartMode(cart);
            });
        }

        bootstrap();
    });

    function bootstrap() {
        showFeedback();
        var loaders = [loadAddresses(), loadCoupons()];
        if (mode === MODE_BUY_NOW) {
            loaders.push(loadBuyNowDraft());
        } else {
            loaders.push(loadCart());
        }
        Promise.all(loaders).catch(function (error) {
            console.error('Checkout init error', error);
            showFeedback('error', extractErrorMessage(error) || 'Không thể tải dữ liệu thanh toán.');
        });
    }

    async function loadCart() {
        try {
            var cart = await cartClient.fetchCart();
            if (!cart && typeof cartClient.lastCart === 'function') {
                cart = cartClient.lastCart();
            }
            renderCartMode(cart);
        } catch (error) {
            renderCartMode(null);
            throw error;
        }
    }

    async function loadBuyNowDraft() {
        try {
            var response = await apiClient.get('/checkout/buy-now');
            if (!response || response.success !== true || !Array.isArray(response.items) || response.items.length === 0) {
                renderBuyNowMode(null);
                if (placeOrderBtn) {
                    placeOrderBtn.disabled = true;
                    placeOrderBtn.classList.add('opacity-60');
                }
                showFeedback('error', 'Không tìm thấy sản phẩm mua ngay. Vui lòng chọn lại sản phẩm.');
                return;
            }
            renderBuyNowMode(response);
        } catch (error) {
            renderBuyNowMode(null);
            throw error;
        }
    }

    async function loadAddresses() {
        try {
            var response = await apiClient.get('/profile/addresses');
            if (!response || response.success !== true) {
                throw new Error('Không thể tải địa chỉ');
            }
            renderAddresses(response.addresses || []);
        } catch (error) {
            renderAddresses([]);
            if (error && error.status === 401) {
                showFeedback('error', 'Bạn cần đăng nhập để thanh toán. Đang chuyển hướng...');
                setTimeout(function () {
                    window.location.href = (appShell.contextPath || '') + '/login.jsp';
                }, 1200);
                return;
            }
            throw error;
        }
    }

    async function loadCoupons() {
        if (!couponSelectEl) {
            return Promise.resolve();
        }
        try {
            var response = await apiClient.get('/profile/coupons');
            if (!response || response.success !== true) {
                throw new Error('Không thể tải mã giảm giá');
            }
            var coupons = Array.isArray(response.coupons) ? response.coupons : [];
            couponState.coupons = coupons.map(function (coupon) {
                return normalizeCoupon(coupon, 'global');
            });
            renderCouponOptions();
            updateCouponFeedback();
        } catch (error) {
            console.error('Checkout loadCoupons error', error);
            updateCouponFeedback('Không thể tải danh sách mã giảm giá.', false);
        }
    }

    function renderCouponOptions() {
        if (!couponSelectEl) {
            return;
        }
        var currentValue = couponState.selectedCode || '';
        couponSelectEl.innerHTML = '<option value="">Chọn mã giảm giá</option>';
        couponState.coupons.forEach(function (coupon) {
            var option = document.createElement('option');
            option.value = coupon.code;
            option.textContent = buildCouponOptionLabel(coupon);
            if (coupon.code === currentValue) {
                option.selected = true;
            }
            couponSelectEl.appendChild(option);
        });
    }

    function buildCouponOptionLabel(coupon) {
        var parts = [coupon.code];
        if (coupon.description) {
            parts.push(coupon.description);
        } else if (coupon.type === 'percentage') {
            parts.push((coupon.value || 0) + '%');
        } else if (coupon.type === 'fixed') {
            parts.push(appShell.formatCurrency(toNumber(coupon.value || 0)));
        }
        if (coupon.shopName) {
            parts.push('Shop: ' + coupon.shopName);
        }
        return parts.join(' - ');
    }

    function bindCouponEvents() {
        if (couponApplyBtn) {
            couponApplyBtn.addEventListener('click', function () {
                applySelectedCoupon();
            });
        }
        if (couponSelectEl) {
            couponSelectEl.addEventListener('change', function () {
                var selectedCode = couponSelectEl.value;
                if (selectedCode && couponInputEl) {
                    couponInputEl.value = selectedCode;
                }
                if (!couponState.selectedCode) {
                    updateCouponFeedback();
                }
            });
        }
        if (couponInputEl) {
            couponInputEl.addEventListener('input', function () {
                if (couponSelectEl) {
                    couponSelectEl.value = '';
                }
            });
            couponInputEl.addEventListener('keypress', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    applySelectedCoupon();
                }
            });
        }
    }

    function applySelectedCoupon() {
        var inputCode = couponInputEl ? couponInputEl.value.trim().toUpperCase() : '';
        var selectedCode = inputCode || (couponSelectEl ? couponSelectEl.value : '');
        
        if (couponState.selectedCode && (!selectedCode || selectedCode === couponState.selectedCode)) {
            clearCouponSelection(true);
            updateTotalsFromSelection();
            return;
        }
        if (!selectedCode) {
            clearCouponSelection(false);
            updateTotalsFromSelection();
            return;
        }
        
        var coupon = couponState.coupons.find(function (c) {
            return c.code === selectedCode;
        });
        
        if (!coupon) {
            fetchAndApplyCoupon(selectedCode);
            return;
        }
        
        couponState.selectedCode = selectedCode;
        couponState.selectedCoupon = coupon;
        if (couponInputEl) {
            couponInputEl.value = selectedCode;
        }
        setCouponButtonState(true);
        updateTotalsFromSelection();
    }
    
    async function fetchAndApplyCoupon(code) {
        if (!code) {
            return;
        }
        
        updateCouponFeedback('Đang kiểm tra mã giảm giá...', false);
        if (couponApplyBtn) {
            couponApplyBtn.disabled = true;
        }
        
        try {
            var params = new URLSearchParams({ code: code });
            if (cartState.activeShopId) {
                params.set('shopId', String(cartState.activeShopId));
            }
            var response = await apiClient.get('/api/coupons/validate?' + params.toString());
            if (!response || !response.success || !response.coupon) {
                throw new Error(response && response.message ? response.message : 'Mã giảm giá không hợp lệ');
            }

            var scope = response.coupon.scope || (response.coupon.shopId ? 'shop' : null);
            var coupon = normalizeCoupon(response.coupon, scope);
            couponState.selectedCode = code;
            couponState.selectedCoupon = coupon;

            if (couponState.coupons.findIndex(function (c) { return c.code === code; }) === -1) {
                couponState.coupons.push(coupon);
                renderCouponOptions();
            }
            
            if (couponInputEl) {
                couponInputEl.value = code;
            }
            if (couponSelectEl) {
                couponSelectEl.value = code;
            }
            
            setCouponButtonState(true);
            updateTotalsFromSelection();
        } catch (error) {
            console.error('Validate coupon error', error);
            var errorMsg = extractErrorMessage(error) || 'Không thể áp dụng mã giảm giá này';
            updateCouponFeedback(errorMsg, false);
            clearCouponSelection(false);
        } finally {
            if (couponApplyBtn) {
                couponApplyBtn.disabled = false;
            }
        }
    }

    function clearCouponSelection(showMessage) {
        couponState.selectedCode = null;
        couponState.selectedCoupon = null;
        couponState.discount = 0;
        if (couponInputEl) {
            couponInputEl.value = '';
        }
        if (couponSelectEl) {
            couponSelectEl.value = '';
        }
        setCouponButtonState(false);
        if (showMessage) {
            updateCouponFeedback('Đã bỏ chọn mã giảm giá.', false);
        } else {
            updateCouponFeedback();
        }
    }

    function evaluateCoupon(coupon, subtotal) {
        var result = { valid: false, discount: 0, message: '' };
        if (!coupon || subtotal <= 0) {
            result.message = 'Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá.';
            return result;
        }
        if (coupon.scope === 'shop') {
            if (!cartState.activeShopId || (coupon.shopId && coupon.shopId !== cartState.activeShopId)) {
                result.message = 'Mã giảm giá chỉ áp dụng cho cửa hàng ' + (coupon.shopName || '');
                return result;
            }
            if (coupon.remaining != null) {
                var remaining = toNumber(coupon.remaining);
                if (Number.isFinite(remaining) && remaining <= 0) {
                    result.message = 'Mã giảm giá của cửa hàng đã hết lượt sử dụng.';
                    return result;
                }
            }
        }
        if (coupon.status && coupon.status !== 'active') {
            result.message = 'Mã giảm giá không còn hiệu lực.';
            return result;
        }
        if (coupon.userStatus && coupon.userStatus === 'used') {
            result.message = 'Bạn đã sử dụng mã giảm giá này.';
            return result;
        }
        if (coupon.minimumOrder) {
            var minimumOrder = toNumber(coupon.minimumOrder);
            if (minimumOrder > 0 && subtotal < minimumOrder) {
                result.message = 'Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã.';
                return result;
            }
        }
        var now = new Date();
        if (coupon.startDate) {
            var startDate = new Date(coupon.startDate);
            if (!Number.isNaN(startDate.getTime()) && now < startDate) {
                result.message = 'Mã giảm giá chưa bắt đầu áp dụng.';
                return result;
            }
        }
        if (coupon.endDate) {
            var endDate = new Date(coupon.endDate);
            if (!Number.isNaN(endDate.getTime()) && now > endDate) {
                result.message = 'Mã giảm giá đã hết hạn.';
                return result;
            }
        }
        var value = toNumber(coupon.value);
        var discount = 0;
        if (coupon.type === 'percentage') {
            discount = subtotal * value / 100;
            var maxDiscount = toNumber(coupon.maxDiscount);
            if (maxDiscount > 0 && discount > maxDiscount) {
                discount = maxDiscount;
            }
        } else if (coupon.type === 'fixed') {
            discount = value;
        } else {
            result.message = 'Loại mã giảm giá không được hỗ trợ.';
            return result;
        }
        if (discount <= 0) {
            result.message = 'Mã giảm giá không áp dụng được cho đơn hàng này.';
            return result;
        }
        if (discount > subtotal) {
            discount = subtotal;
        }
        result.valid = true;
        result.discount = discount;
        return result;
    }

    function calculateCouponDiscount(subtotal) {
        couponState.lastSubtotal = subtotal;
        if (!couponState.selectedCoupon) {
            couponState.discount = 0;
            setCouponButtonState(false);
            if (couponState.selectedCode) {
                updateCouponFeedback('Không thể áp dụng mã giảm giá với giá trị hiện tại của đơn hàng.', false);
            } else {
                updateCouponFeedback();
            }
            return 0;
        }
        var evaluation = evaluateCoupon(couponState.selectedCoupon, subtotal);
        if (!evaluation.valid) {
            couponState.discount = 0;
            updateCouponFeedback(evaluation.message, false);
            setCouponButtonState(true);
            return 0;
        }
        couponState.discount = evaluation.discount;
        updateCouponFeedback('Đã áp dụng mã ' + couponState.selectedCoupon.code + ' giảm ' + appShell.formatCurrency(evaluation.discount) + '.', true);
        setCouponButtonState(true);
        return evaluation.discount;
    }

    function updateCouponFeedback(message, success) {
        if (!couponFeedbackEl) {
            return;
        }
        if (!message) {
            if (couponState.selectedCoupon) {
                couponFeedbackEl.textContent = 'Mã ' + couponState.selectedCoupon.code + ' đang chờ áp dụng.';
                couponFeedbackEl.className = 'text-xs text-gray-600';
            } else {
                couponFeedbackEl.textContent = 'Nhập hoặc chọn mã giảm giá để tiết kiệm hơn.';
                couponFeedbackEl.className = 'text-xs text-gray-500';
            }
            return;
        }
        couponFeedbackEl.textContent = message;
        couponFeedbackEl.className = 'text-xs ' + (success ? 'text-emerald-600' : 'text-red-500');
    }

    function setCouponButtonState(isApplied) {
        if (!couponApplyBtn) {
            return;
        }
        if (isApplied) {
            couponApplyBtn.textContent = 'Bỏ chọn';
        } else {
            couponApplyBtn.textContent = 'Áp dụng';
        }
    }

    function renderCartMode(cart) {
        var previousSelection = new Set(cartState.selected);
        var shouldSelectAll = previousSelection.size === 0;
        cartState.items = [];
        cartState.selected = new Set();

        if (!cart || !Array.isArray(cart.items) || cart.items.length === 0) {
            if (orderItemsEl) {
                orderItemsEl.innerHTML = '<p class="text-sm text-gray-500">Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.</p>';
            }
            updateOrderTotals(0, 0, 0);
            itemsCountEl && (itemsCountEl.textContent = '0 sản phẩm');
            disablePlaceOrder();
            resetShippingState();
            return;
        }

        cart.items.forEach(function (item) {
            var normalizedId = normalizeBookId(item.bookId);
            if (normalizedId === null) {
                return;
            }
            var rawShopId = item.shopId != null ? item.shopId : (item.shop_id != null ? item.shop_id : null);
            var parsedShopId = parseInt(rawShopId, 10);
            var entry = {
                bookId: normalizedId,
                title: item.title || 'Sách chưa cập nhật',
                quantity: item.quantity || 0,
                unitPrice: toNumber(item.unitPrice),
                author: item.author || '',
                imageUrl: item.imageUrl || '',
                shopId: Number.isFinite(parsedShopId) && parsedShopId > 0 ? parsedShopId : null,
                shopName: item.shopName || item.shop_name || ''
            };
            cartState.items.push(entry);
            if (shouldSelectAll || previousSelection.has(entry.bookId)) {
                cartState.selected.add(entry.bookId);
            }
        });

        renderCartItemsWithCheckboxes();
        updateCartSummaryLabel();
        updateTotalsFromSelection();
    }

    function renderBuyNowMode(payload) {
        cartState.items = [];
        cartState.selected = new Set();

        if (!payload || !Array.isArray(payload.items) || payload.items.length === 0) {
            if (orderItemsEl) {
                orderItemsEl.innerHTML = '<p class="text-sm text-gray-500">Không tìm thấy sản phẩm mua ngay.</p>';
            }
            updateOrderTotals(0, 0, 0);
            itemsCountEl && (itemsCountEl.textContent = '0 sản phẩm');
            disablePlaceOrder();
            return;
        }

        payload.items.forEach(function (item) {
            var normalizedId = normalizeBookId(item.bookId);
            if (normalizedId === null) {
                return;
            }
            var rawShopId = item.shopId != null ? item.shopId : (item.shop_id != null ? item.shop_id : null);
            var parsedShopId = parseInt(rawShopId, 10);
            var entry = {
                bookId: normalizedId,
                title: item.title || 'Sách chưa cập nhật',
                quantity: parseInt(item.quantity, 10) || 1,
                unitPrice: toNumber(item.unitPrice),
                author: item.author || '',
                imageUrl: item.imageUrl || '',
                shopId: Number.isFinite(parsedShopId) && parsedShopId > 0 ? parsedShopId : null,
                shopName: item.shopName || item.shop_name || ''
            };
            cartState.items.push(entry);
            cartState.selected.add(entry.bookId);
        });

        if (orderItemsEl) {
            var fragment = document.createDocumentFragment();
            cartState.items.forEach(function (item) {
                var total = item.unitPrice * item.quantity;
                var row = document.createElement('div');
                row.className = 'flex items-start justify-between gap-3 text-sm text-gray-600';
                row.innerHTML = '\n                <div class="flex-1">\n                    <p class="font-medium text-gray-800">' + appShell.escapeHtml(item.title) + '</p>\n                    <p class="text-xs text-gray-400">Số lượng: ' + item.quantity + '</p>\n                </div>\n                <div class="text-right font-semibold text-gray-700">' + appShell.formatCurrency(total) + '</div>';
                fragment.appendChild(row);
            });
            orderItemsEl.innerHTML = '';
            orderItemsEl.appendChild(fragment);
        }

        itemsCountEl && (itemsCountEl.textContent = cartState.items.length + ' sản phẩm (Mua ngay)');
        enablePlaceOrder();
        updateTotalsFromSelection();
    }

    function renderCartItemsWithCheckboxes() {
        if (!orderItemsEl) {
            return;
        }
        var fragment = document.createDocumentFragment();
        cartState.items.forEach(function (item) {
            var total = item.unitPrice * item.quantity;
            var row = document.createElement('label');
            row.className = 'flex items-start justify-between gap-3 text-sm text-gray-600 border border-gray-200 rounded-xl px-3 py-3 mb-2 hover:border-amber-400 transition';
            var isChecked = cartState.selected.has(item.bookId) ? 'checked' : '';
            row.innerHTML = '\n                <div class="flex items-start gap-3">\n                    <input type="checkbox" class="mt-1 accent-amber-600" data-checkout-item value="' + item.bookId + '" ' + isChecked + '>\n                    <div>\n                        <p class="font-medium text-gray-800">' + appShell.escapeHtml(item.title) + '</p>\n                        <p class="text-xs text-gray-400">Số lượng: ' + item.quantity + '</p>\n                    </div>\n                </div>\n                <div class="text-right font-semibold text-gray-700">' + appShell.formatCurrency(total) + '</div>';
            fragment.appendChild(row);
        });
        orderItemsEl.innerHTML = '';
        orderItemsEl.appendChild(fragment);
    }

    function bindSelectionEvents() {
        if (!orderItemsEl) {
            return;
        }
        orderItemsEl.addEventListener('change', function (event) {
            if (mode !== MODE_CART) {
                return;
            }
            var checkbox = event.target.closest('[data-checkout-item]');
            if (!checkbox) {
                return;
            }
            var bookId = normalizeBookId(checkbox.value);
            if (bookId === null) {
                return;
            }
            if (checkbox.checked) {
                cartState.selected.add(bookId);
            } else {
                cartState.selected.delete(bookId);
            }
            updateCartSummaryLabel();
            updateTotalsFromSelection();
        });
    }

    function bindAddressEvents() {
        if (!addressListEl) {
            return;
        }
        addressListEl.addEventListener('change', function () {
            var selectedAddressId = getSelectedAddressId();
            if (selectedAddressId) {
                fetchShippingQuote(selectedAddressId);
            } else {
                resetShippingState();
            }
        });
    }

    function bindPaymentMethodEvents() {
        var inputs = document.querySelectorAll('input[name="paymentMethod"]');
        if (!inputs || inputs.length === 0) {
            return;
        }
        inputs.forEach(function (input) {
            input.addEventListener('change', updatePaymentDetailsVisibility);
        });
        updatePaymentDetailsVisibility();
    }

    function updatePaymentDetailsVisibility() {
        var method = getSelectedPaymentMethod();
        var requiresWalletDetails = method === 'vnpay' || method === 'momo';
        if (walletDetailsEl) {
            if (requiresWalletDetails) {
                walletDetailsEl.classList.remove('hidden');
            } else {
                walletDetailsEl.classList.add('hidden');
                resetWalletFields();
            }
        }
    }

    function resetWalletFields() {
        if (walletCardNumberEl) {
            walletCardNumberEl.value = '';
        }
        if (walletExpiryMonthEl) {
            walletExpiryMonthEl.value = '';
        }
        if (walletExpiryYearEl) {
            walletExpiryYearEl.value = '';
        }
        if (walletCvvEl) {
            walletCvvEl.value = '';
        }
    }

    function collectPaymentDetails(method) {
        if (method !== 'vnpay' && method !== 'momo') {
            return null;
        }
        if (!walletCardNumberEl || !walletExpiryMonthEl || !walletExpiryYearEl || !walletCvvEl) {
            showFeedback('error', 'Không tìm thấy trường nhập thông tin ví điện tử.');
            throw new Error('Missing wallet fields');
        }
        var rawNumber = (walletCardNumberEl.value || '').replace(/\D+/g, '');
        if (rawNumber.length < 12 || rawNumber.length > 19) {
            showFeedback('error', 'Số thẻ/ ví phải từ 12 đến 19 chữ số.');
            throw new Error('Invalid card number');
        }
        var monthRaw = (walletExpiryMonthEl.value || '').trim();
        var yearRaw = (walletExpiryYearEl.value || '').trim();
        if (!/^\d{1,2}$/.test(monthRaw)) {
            showFeedback('error', 'Vui lòng nhập tháng hết hạn hợp lệ (MM).');
            throw new Error('Invalid expiry month');
        }
        var month = parseInt(monthRaw, 10);
        if (!Number.isFinite(month) || month < 1 || month > 12) {
            showFeedback('error', 'Tháng hết hạn phải nằm trong khoảng 01-12.');
            throw new Error('Invalid expiry month range');
        }
        if (!/^\d{2,4}$/.test(yearRaw)) {
            showFeedback('error', 'Vui lòng nhập năm hết hạn hợp lệ (YY hoặc YYYY).');
            throw new Error('Invalid expiry year');
        }
        var normalizedYear = yearRaw.length === 2 ? parseInt('20' + yearRaw, 10) : parseInt(yearRaw, 10);
        if (!Number.isFinite(normalizedYear) || normalizedYear < 2000) {
            showFeedback('error', 'Năm hết hạn không hợp lệ.');
            throw new Error('Invalid expiry year value');
        }
        var today = new Date();
        var expiryDate = new Date(normalizedYear, month - 1, 1);
        if (expiryDate < new Date(today.getFullYear(), today.getMonth(), 1)) {
            showFeedback('error', 'Thẻ/ ví đã hết hạn.');
            throw new Error('Card expired');
        }
        var cvv = (walletCvvEl.value || '').trim();
        if (!/^\d{3,4}$/.test(cvv)) {
            showFeedback('error', 'Mã CVV/CVC phải gồm 3 hoặc 4 chữ số.');
            throw new Error('Invalid CVV');
        }
        return {
            method: method,
            cardNumber: rawNumber,
            expiryMonth: String(month).padStart(2, '0'),
            expiryYear: String(normalizedYear),
            cvv: cvv,
            last4: rawNumber.slice(-4)
        };
    }

    function updateCartSummaryLabel() {
        if (!itemsCountEl) {
            return;
        }
        var selectedCount = cartState.selected.size;
        var totalCount = cartState.items.length;
        itemsCountEl.textContent = selectedCount + ' / ' + totalCount + ' sản phẩm';
    }

    function updateTotalsFromSelection() {
        var subtotal = 0;
        cartState.items.forEach(function (item) {
            if (mode === MODE_BUY_NOW || cartState.selected.has(item.bookId)) {
                subtotal += item.unitPrice * item.quantity;
            }
        });
        cartState.subtotal = subtotal;
        var discount = calculateCouponDiscount(subtotal);
        var shipping = subtotal > 0 ? Math.max(0, toNumber(shippingState.fee)) : 0;
        updateOrderTotals(subtotal, shipping, discount);
        if (mode === MODE_CART) {
            if (subtotal > 0 && cartState.selected.size > 0) {
                enablePlaceOrder();
            } else {
                disablePlaceOrder();
            }
        }
        syncActiveShopContext();
    }

    function updateOrderTotals(subtotal, shipping, discount) {
        var safeSubtotal = Math.max(0, subtotal || 0);
        var safeShipping = Math.max(0, shipping || 0);
        var safeDiscount = Math.max(0, discount || 0);
        var total = Math.max(0, safeSubtotal - safeDiscount + safeShipping);
        if (subtotalEl) {
            subtotalEl.textContent = appShell.formatCurrency(safeSubtotal);
        }
        if (discountEl) {
            discountEl.textContent = appShell.formatCurrency(safeDiscount);
        }
        if (shippingEl) {
            shippingEl.textContent = appShell.formatCurrency(safeShipping);
        }
        if (totalEl) {
            totalEl.textContent = appShell.formatCurrency(total);
        }
        updateShippingDisplay();
    }

    function resolveActiveShopContext() {
        if (mode === MODE_BUY_NOW) {
            var single = cartState.items.length > 0 ? cartState.items[0] : null;
            if (single && single.shopId) {
                return { shopId: single.shopId, shopName: single.shopName || '' };
            }
            return { shopId: null, shopName: '' };
        }
        if (cartState.selected.size === 0) {
            return { shopId: null, shopName: '' };
        }
        var currentId = null;
        var currentName = '';
        var multiShop = false;
        cartState.items.forEach(function (item) {
            if (!cartState.selected.has(item.bookId)) {
                return;
            }
            var itemShopId = typeof item.shopId === 'number' ? item.shopId : null;
            if (!itemShopId) {
                multiShop = true;
                return;
            }
            if (currentId === null) {
                currentId = itemShopId;
                currentName = item.shopName || '';
            } else if (currentId !== itemShopId) {
                multiShop = true;
            }
        });
        if (multiShop) {
            return { shopId: null, shopName: '' };
        }
        return { shopId: currentId, shopName: currentName };
    }

    function syncActiveShopContext() {
        var context = resolveActiveShopContext();
        cartState.activeShopId = context.shopId;
        cartState.activeShopName = context.shopName;
        if (!context.shopId) {
            couponState.loadedShopId = null;
            return;
        }
        if (context.shopId && couponState.loadedShopId !== context.shopId) {
            refreshSellerCoupons(context.shopId);
        }
    }

    async function refreshSellerCoupons(shopId) {
        if (!shopId || couponState.loadedShopId === shopId) {
            return;
        }
        couponState.loadedShopId = shopId;
        try {
            var response = await apiClient.get('/profile/shop-coupons?shopId=' + encodeURIComponent(shopId));
            if (response && response.success && Array.isArray(response.coupons)) {
                mergeCouponList(response.coupons, 'shop');
            }
        } catch (error) {
            console.warn('Không thể tải mã giảm giá của shop', error);
        }
    }

    function mergeCouponList(coupons, scope) {
        if (!Array.isArray(coupons) || coupons.length === 0) {
            return;
        }
        var changed = false;
        coupons.forEach(function (coupon) {
            var normalized = normalizeCoupon(coupon, scope);
            if (!normalized.code) {
                return;
            }
            var index = couponState.coupons.findIndex(function (existing) {
                return existing.code === normalized.code;
            });
            if (index === -1) {
                couponState.coupons.push(normalized);
                changed = true;
            } else {
                couponState.coupons[index] = Object.assign({}, couponState.coupons[index], normalized);
                changed = true;
            }
        });
        if (changed) {
            renderCouponOptions();
            updateCouponFeedback();
        }
    }

    function normalizeCoupon(coupon, scope) {
        if (!coupon) {
            return {};
        }
        var normalizedType = coupon.type || coupon.discountType || 'fixed';
        var normalized = {
            code: coupon.code ? String(coupon.code).toUpperCase() : '',
            description: coupon.description || '',
            type: normalizedType,
            value: toNumber(coupon.value != null ? coupon.value : coupon.discountValue),
            maxDiscount: toNumber(coupon.maxDiscount != null ? coupon.maxDiscount : coupon.maxDiscountValue),
            minimumOrder: toNumber(coupon.minimumOrder != null ? coupon.minimumOrder : coupon.minOrderValue),
            status: coupon.status || 'active',
            userStatus: coupon.userStatus || coupon.user_status || null,
            startDate: coupon.startDate || coupon.start_date || null,
            endDate: coupon.endDate || coupon.end_date || null,
            scope: scope || coupon.scope || 'global',
            shopId: coupon.shopId || coupon.shop_id || null,
            shopName: coupon.shopName || coupon.shop_name || '',
            usageLimit: coupon.usageLimit != null ? coupon.usageLimit : coupon.usage_limit,
            remaining: coupon.remaining != null ? coupon.remaining : null
        };
        return normalized;
    }

    function renderAddresses(addresses) {
        if (!addressListEl) {
            return;
        }
        if (!Array.isArray(addresses) || addresses.length === 0) {
            addressListEl.innerHTML = '\n                <div class="bg-amber-50 border border-dashed border-amber-200 rounded-xl p-4 text-sm text-amber-800">\n                    Chưa có địa chỉ giao hàng. <a href="' + (appShell.contextPath || '') + '/profile.jsp#addresses" class="font-semibold underline">Thêm địa chỉ ngay</a> để tiếp tục.\n                </div>';
            disablePlaceOrder();
            return;
        }

        var defaultId = null;
        var fragment = document.createDocumentFragment();
        addresses.forEach(function (address, index) {
            var addressId = address.id;
            if (address.isDefault || address.default) {
                defaultId = addressId;
            }
            var labelText = buildAddressLabel(address);
            var recipientNameHtml = appShell.escapeHtml(address.recipientName || 'Người nhận chưa cập nhật');
            var phoneHtml = appShell.escapeHtml(address.phone || 'Chưa có số điện thoại');
            var option = document.createElement('label');
            option.className = 'border rounded-2xl p-4 flex gap-3 cursor-pointer hover:border-amber-500 transition';
            option.innerHTML = '\n                <input type="radio" class="mt-1 accent-amber-600" name="checkoutAddress" value="' + addressId + '" ' + (index === 0 ? 'checked' : '') + '>\n                <div class="flex-1">\n                    <p class="font-semibold text-gray-800">' + recipientNameHtml + '</p>\n                    <p class="text-xs text-gray-500 mb-1">' + phoneHtml + '</p>\n                    <p class="text-sm text-gray-600 leading-6">' + labelText + '</p>\n                </div>';
            fragment.appendChild(option);
        });
        addressListEl.innerHTML = '';
        addressListEl.appendChild(fragment);

        if (defaultId !== null) {
            var defaultInput = addressListEl.querySelector('input[value="' + defaultId + '"]');
            if (defaultInput) {
                defaultInput.checked = true;
            }
        }

        enablePlaceOrder();
        var selectedAddressId = getSelectedAddressId();
        if (selectedAddressId) {
            fetchShippingQuote(selectedAddressId);
        } else {
            resetShippingState();
        }
    }

    function bindPlaceOrder() {
        if (!placeOrderBtn) {
            return;
        }
        placeOrderBtn.addEventListener('click', async function () {
            var selectedAddress = getSelectedAddressId();
            if (!selectedAddress) {
                showFeedback('error', 'Vui lòng chọn địa chỉ giao hàng.');
                return;
            }
            var payloadItems = buildSelectedItems();
            if (payloadItems.length === 0) {
                showFeedback('error', 'Vui lòng chọn ít nhất một sản phẩm để thanh toán.');
                return;
            }
            var paymentMethod = getSelectedPaymentMethod();
            var notes = notesEl ? notesEl.value.trim() : '';
            var paymentDetails = null;
            try {
                paymentDetails = collectPaymentDetails(paymentMethod);
            } catch (validationError) {
                console.warn('Payment validation failed', validationError);
                return;
            }
            placeOrderBtn.disabled = true;
            placeOrderBtn.classList.add('opacity-60');
            placeOrderBtn.textContent = 'Đang xử lý...';
            showFeedback();
            try {
                var result = await cartClient.checkout({
                    addressId: selectedAddress,
                    paymentMethod: paymentMethod,
                    notes: notes || null,
                    couponCode: couponState.selectedCode || null,
                    items: payloadItems,
                    mode: mode,
                    paymentDetails: paymentDetails,
                    shopId: cartState.activeShopId || null
                });
                if (result && result.success) {
                    if (mode === MODE_BUY_NOW) {
                        await apiClient.del('/checkout/buy-now');
                    }
                    showFeedback('success', 'Đặt hàng thành công! Chuyển đến lịch sử đơn hàng...');
                    setTimeout(function () {
                        window.location.href = (appShell.contextPath || '') + '/profile.jsp#orders';
                    }, 1500);
                } else {
                    throw new Error(result && result.message ? result.message : 'Không thể hoàn tất đơn hàng.');
                }
            } catch (error) {
                console.error('Checkout error', error);
                showFeedback('error', extractErrorMessage(error) || 'Không thể hoàn tất đơn hàng.');
            } finally {
                placeOrderBtn.disabled = false;
                placeOrderBtn.classList.remove('opacity-60');
                placeOrderBtn.textContent = 'Đặt hàng';
            }
        });
    }

    function buildSelectedItems() {
        var items = [];
        if (mode === MODE_BUY_NOW) {
            cartState.items.forEach(function (item) {
                items.push({ bookId: item.bookId, quantity: item.quantity });
            });
            return items;
        }
        cartState.items.forEach(function (item) {
            if (cartState.selected.has(item.bookId)) {
                items.push({ bookId: item.bookId, quantity: item.quantity });
            }
        });
        return items;
    }

    function normalizeBookId(raw) {
        var parsed = parseInt(raw, 10);
        if (Number.isFinite(parsed) && parsed > 0) {
            return parsed;
        }
        return null;
    }

    function disablePlaceOrder() {
        if (placeOrderBtn) {
            placeOrderBtn.disabled = true;
            placeOrderBtn.classList.add('opacity-60');
        }
    }

    function enablePlaceOrder() {
        if (placeOrderBtn) {
            placeOrderBtn.disabled = false;
            placeOrderBtn.classList.remove('opacity-60');
        }
    }

    function getSelectedAddressId() {
        var input = addressListEl ? addressListEl.querySelector('input[name="checkoutAddress"]:checked') : null;
        if (!input) {
            return null;
        }
        var value = parseInt(input.value, 10);
        return Number.isNaN(value) || value <= 0 ? null : value;
    }

    function getSelectedPaymentMethod() {
        var input = document.querySelector('input[name="paymentMethod"]:checked');
        return input ? input.value : 'cod';
    }

    function buildAddressLabel(address) {
        var parts = [
            address.line1,
            address.line2,
            address.ward,
            address.district,
            address.city,
            address.province,
            address.country,
            address.postalCode
        ].filter(Boolean);
        return appShell.escapeHtml(parts.join(', '));
    }

    function resetShippingState() {
        shippingState.fee = 0;
        shippingState.shipper = null;
        shippingState.loading = false;
        shippingState.error = null;
        shippingState.lastAddressId = null;
        shippingState.requestToken += 1;
        updateShippingDisplay();
        updateTotalsFromSelection();
    }

    async function fetchShippingQuote(addressId) {
        if (!addressId) {
            resetShippingState();
            return;
        }
        shippingState.loading = true;
        shippingState.error = null;
        shippingState.lastAddressId = addressId;
        shippingState.fee = 0;
        shippingState.shipper = null;
        var requestToken = (shippingState.requestToken || 0) + 1;
        shippingState.requestToken = requestToken;
        updateShippingDisplay();
        updateTotalsFromSelection();
        try {
            var response = await apiClient.get('/shipping/quote?addressId=' + encodeURIComponent(addressId));
            if (shippingState.requestToken !== requestToken) {
                return;
            }
            if (!response || response.success !== true) {
                shippingState.loading = false;
                shippingState.fee = 0;
                shippingState.shipper = null;
                shippingState.error = response && response.message ? response.message : 'Khong the tinh phi van chuyen.';
                updateShippingDisplay();
                updateTotalsFromSelection();
                return;
            }
            shippingState.loading = false;
            shippingState.error = null;
            shippingState.fee = toNumber(response.shippingFee);
            if (!Number.isFinite(shippingState.fee) || shippingState.fee < 0) {
                shippingState.fee = 0;
            }
            shippingState.shipper = response.shipper || null;
            updateShippingDisplay();
            updateTotalsFromSelection();
        } catch (error) {
            if (shippingState.requestToken !== requestToken) {
                return;
            }
            shippingState.loading = false;
            shippingState.fee = 0;
            shippingState.shipper = null;
            shippingState.error = extractErrorMessage(error) || 'Khong the tinh phi van chuyen.';
            updateShippingDisplay();
            updateTotalsFromSelection();
        }
    }

    function updateShippingDisplay() {
        if (!shippingMethodEl) {
            return;
        }
        if (shippingState.loading) {
            shippingMethodEl.textContent = 'Dang tinh phi van chuyen...';
            shippingMethodEl.className = 'text-xs text-gray-400';
            shippingMethodEl.removeAttribute('title');
            return;
        }
        if (shippingState.error) {
            shippingMethodEl.textContent = shippingState.error;
            shippingMethodEl.className = 'text-xs text-red-500';
            shippingMethodEl.removeAttribute('title');
            return;
        }
        if (!shippingState.shipper) {
            shippingMethodEl.textContent = 'Chua co thong tin van chuyen.';
            shippingMethodEl.className = 'text-xs text-gray-400';
            shippingMethodEl.removeAttribute('title');
            return;
        }
        var shipper = shippingState.shipper;
        var details = [];
        if (shipper.name) {
            details.push(shipper.name);
        }
        if (shipper.estimatedTime) {
            details.push(shipper.estimatedTime);
        }
        shippingMethodEl.textContent = details.length > 0 ? details.join(' - ') : 'Nha van chuyen';
        shippingMethodEl.className = 'text-xs text-gray-500';
        var tooltip = [];
        if (shipper.serviceArea) {
            tooltip.push(shipper.serviceArea);
        }
        if (shipper.matchLevel) {
            tooltip.push('Match: ' + shipper.matchLevel);
        }
        var tooltipText = tooltip.join(' | ');
        if (tooltipText) {
            shippingMethodEl.setAttribute('title', tooltipText);
        } else {
            shippingMethodEl.removeAttribute('title');
        }
    }

    function showFeedback(type, message) {
        if (!feedbackEl) {
            return;
        }
        if (!type || !message) {
            feedbackEl.className = 'hidden';
            feedbackEl.textContent = '';
            return;
        }
        var tone = type === 'error'
            ? 'bg-red-50 border border-red-200 text-red-600'
            : 'bg-emerald-50 border border-emerald-200 text-emerald-700';
        feedbackEl.className = tone + ' px-4 py-3 rounded-xl';
        feedbackEl.textContent = message;
    }

    function extractErrorMessage(error) {
        if (!error) {
            return '';
        }
        if (error.payload && error.payload.message) {
            return error.payload.message;
        }
        if (error.message) {
            return error.message;
        }
        return '';
    }

    function toNumber(value) {
        if (typeof value === 'number') {
            return value;
        }
        if (typeof value === 'string') {
            var parsed = Number(value);
            return Number.isFinite(parsed) ? parsed : 0;
        }
        if (value && typeof value === 'object' && typeof value.valueOf === 'function') {
            var coerced = Number(value.valueOf());
            return Number.isFinite(coerced) ? coerced : 0;
        }
        return 0;
    }

    function getQueryParam(name) {
        var params = new URLSearchParams(window.location.search);
        return params.get(name);
    }

})(window, document);








