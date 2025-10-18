(function (window, document) {
    'use strict';

    var appShell = window.appShell;
    var cartClient = window.cartClient;
    var apiClient = window.apiClient;
    if (!appShell || !cartClient || !apiClient) {
        return;
    }

    var SHIPPING_FEE = 26000;
    var MODE_BUY_NOW = 'buy-now';
    var MODE_CART = 'cart';

    var addressListEl;
    var orderItemsEl;
    var itemsCountEl;
    var subtotalEl;
    var discountEl;
    var shippingEl;
    var totalEl;
    var feedbackEl;
    var placeOrderBtn;
    var notesEl;

    var mode = MODE_CART;
    var cartState = {
        items: [],
        selected: new Set()
    };

    appShell.onReady(function () {
        addressListEl = document.querySelector('[data-checkout-address-list]');
        orderItemsEl = document.querySelector('[data-checkout-order-items]');
        itemsCountEl = document.getElementById('checkoutItemsCount');
        subtotalEl = document.getElementById('checkoutSubtotal');
        discountEl = document.getElementById('checkoutDiscount');
        shippingEl = document.getElementById('checkoutShipping');
        totalEl = document.getElementById('checkoutTotal');
        feedbackEl = document.getElementById('checkoutFeedback');
        placeOrderBtn = document.getElementById('placeOrderBtn');
        notesEl = document.getElementById('checkoutNotes');

        mode = getQueryParam('mode') === MODE_BUY_NOW ? MODE_BUY_NOW : MODE_CART;

        bindSelectionEvents();
        bindPlaceOrder();

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
        var loaders = [loadAddresses()];
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

    function renderCartMode(cart) {
        var previousSelection = new Set(cartState.selected);
        var shouldSelectAll = previousSelection.size === 0;
        cartState.items = [];
        cartState.selected = new Set();

        if (!cart || !Array.isArray(cart.items) || cart.items.length === 0) {
            if (orderItemsEl) {
                orderItemsEl.innerHTML = '<p class="text-sm text-gray-500">Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.</p>';
            }
            updateOrderTotals(0, 0);
            itemsCountEl && (itemsCountEl.textContent = '0 sản phẩm');
            disablePlaceOrder();
            return;
        }

        cart.items.forEach(function (item) {
            var normalizedId = normalizeBookId(item.bookId);
            if (normalizedId === null) {
                return;
            }
            var entry = {
                bookId: normalizedId,
                title: item.title || 'Sách chưa cập nhật',
                quantity: item.quantity || 0,
                unitPrice: toNumber(item.unitPrice),
                author: item.author || '',
                imageUrl: item.imageUrl || ''
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
            updateOrderTotals(0, 0);
            itemsCountEl && (itemsCountEl.textContent = '0 sản phẩm');
            disablePlaceOrder();
            return;
        }

        payload.items.forEach(function (item) {
            var normalizedId = normalizeBookId(item.bookId);
            if (normalizedId === null) {
                return;
            }
            var entry = {
                bookId: normalizedId,
                title: item.title || 'Sách chưa cập nhật',
                quantity: parseInt(item.quantity, 10) || 1,
                unitPrice: toNumber(item.unitPrice),
                author: item.author || '',
                imageUrl: item.imageUrl || ''
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
        var shipping = subtotal > 0 ? SHIPPING_FEE : 0;
        updateOrderTotals(subtotal, shipping);
        if (mode === MODE_CART) {
            if (subtotal > 0 && cartState.selected.size > 0) {
                enablePlaceOrder();
            } else {
                disablePlaceOrder();
            }
        }
    }

    function updateOrderTotals(subtotal, shipping) {
        var discount = 0;
        var total = Math.max(0, subtotal - discount + shipping);
        if (subtotalEl) {
            subtotalEl.textContent = appShell.formatCurrency(subtotal);
        }
        if (discountEl) {
            discountEl.textContent = appShell.formatCurrency(discount);
        }
        if (shippingEl) {
            shippingEl.textContent = appShell.formatCurrency(shipping);
        }
        if (totalEl) {
            totalEl.textContent = appShell.formatCurrency(total);
        }
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
            placeOrderBtn.disabled = true;
            placeOrderBtn.classList.add('opacity-60');
            placeOrderBtn.textContent = 'Đang xử lý...';
            showFeedback();
            try {
                var result = await cartClient.checkout({
                    addressId: selectedAddress,
                    paymentMethod: paymentMethod,
                    notes: notes || null,
                    items: payloadItems,
                    mode: mode
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
