(function (window, document) {
    'use strict';

    var appShell = window.appShell;
    var cartClient = window.cartClient;
    var apiClient = window.apiClient;
    if (!appShell || !cartClient || !apiClient) {
        return;
    }

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

        bindPlaceOrder();
        bootstrap();
    });

    function bootstrap() {
        Promise.all([loadCart(), loadAddresses()])
            .catch(function (error) {
                console.error('Checkout init error', error);
                showFeedback('error', extractErrorMessage(error) || 'Không thể tải dữ liệu thanh toán.');
            });
    }

    async function loadCart() {
        try {
            var cart = await cartClient.fetchCart();
            renderCart(cart);
        } catch (error) {
            renderCart();
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

    function renderCart(cart) {
        if (!cart || !Array.isArray(cart.items) || cart.items.length === 0) {
            if (orderItemsEl) {
                orderItemsEl.innerHTML = '<p class="text-sm text-gray-500">Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.</p>';
            }
            updateOrderTotals({ subtotal: 0, discount: 0, shipping: 0 });
            if (placeOrderBtn) {
                placeOrderBtn.disabled = true;
                placeOrderBtn.classList.add('opacity-60');
            }
            return;
        }

        if (placeOrderBtn) {
            placeOrderBtn.disabled = false;
            placeOrderBtn.classList.remove('opacity-60');
        }

        var fragment = document.createDocumentFragment();
        var subtotal = 0;
        cart.items.forEach(function (item) {
            var price = toNumber(item.unitPrice);
            var qty = item.quantity || 0;
            var total = price * qty;
            subtotal += total;
            var titleHtml = appShell.escapeHtml(item.title || 'Sách chưa cập nhật');
            var totalLabel = appShell.formatCurrency(total);

            var row = document.createElement('div');
            row.className = 'flex items-start justify-between gap-3 text-sm text-gray-600';
            row.innerHTML = `
                <div class="flex-1">
                    <p class="font-medium text-gray-800">${titleHtml}</p>
                    <p class="text-xs text-gray-400">Số lượng: ${qty}</p>
                </div>
                <div class="text-right font-semibold text-gray-700">${totalLabel}</div>`;
            fragment.appendChild(row);
        });

        if (orderItemsEl) {
            orderItemsEl.innerHTML = '';
            orderItemsEl.appendChild(fragment);
        }
        updateOrderTotals({ subtotal: subtotal, discount: 0, shipping: 0 });
        if (itemsCountEl) {
            itemsCountEl.textContent = cart.items.length + ' sản phẩm';
        }
    }

    function renderAddresses(addresses) {
        if (!addressListEl) {
            return;
        }
        if (!Array.isArray(addresses) || addresses.length === 0) {
            addressListEl.innerHTML = `
                <div class="bg-amber-50 border border-dashed border-amber-200 rounded-xl p-4 text-sm text-amber-800">
                    Chưa có địa chỉ giao hàng. <a href="${(appShell.contextPath || '')}/profile.jsp#addresses" class="font-semibold underline">Thêm địa chỉ ngay</a> để tiếp tục.
                </div>`;
            if (placeOrderBtn) {
                placeOrderBtn.disabled = true;
                placeOrderBtn.classList.add('opacity-60');
            }
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
            option.innerHTML = `
                <input type="radio" class="mt-1 accent-amber-600" name="checkoutAddress" value="${addressId}" ${index === 0 ? 'checked' : ''}>
                <div class="flex-1">
                    <p class="font-semibold text-gray-800">${recipientNameHtml}</p>
                    <p class="text-xs text-gray-500 mb-1">${phoneHtml}</p>
                    <p class="text-sm text-gray-600 leading-6">${labelText}</p>
                </div>`;
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

        if (placeOrderBtn) {
            placeOrderBtn.disabled = false;
            placeOrderBtn.classList.remove('opacity-60');
        }
    }

    function updateOrderTotals(summary) {
        var subtotal = toNumber(summary && summary.subtotal);
        var discount = toNumber(summary && summary.discount);
        var shipping = toNumber(summary && summary.shipping);
        var total = subtotal - discount + shipping;
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
                    notes: notes || null
                });
                if (result && result.success) {
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
})(window, document);
