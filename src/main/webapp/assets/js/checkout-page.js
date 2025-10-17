(function (window, document) {
    'use strict';

    const shell = window.appShell || {};
    const contextPath = shell.contextPath || '';
    const cartApi = shell.cartApiBase || (contextPath + '/api/cart');
    const orderApi = contextPath + '/api/orders/checkout';
    const { formatCurrency = (v) => v, escapeHtml = (v) => v, onReady = (fn) => fn } = shell;

    const DEFAULT_SHIPPING_FEE = 15000;

    const els = {};

    function queryElements() {
        els.loading = document.getElementById('checkoutLoading');
        els.empty = document.getElementById('checkoutEmpty');
        els.error = document.getElementById('checkoutError');
        els.success = document.getElementById('checkoutSuccess');
        els.content = document.getElementById('checkoutContent');
        els.summaryItems = document.getElementById('checkoutSummaryItems');
        els.summarySubtotal = document.getElementById('checkoutSubtotal');
        els.summaryShipping = document.getElementById('checkoutShipping');
        els.summaryTotal = document.getElementById('checkoutTotal');
        els.summaryCount = document.getElementById('checkoutItemCount');
        els.currency = document.getElementById('checkoutCurrency');
        els.form = document.getElementById('checkoutForm');
        els.submitBtn = document.getElementById('checkoutSubmit');
    }

    function toggleSection(target) {
        ['loading', 'empty', 'error', 'success', 'content'].forEach((name) => {
            if (!els[name]) {
                return;
            }
            if (name === target) {
                els[name].classList.remove('hidden');
            } else {
                els[name].classList.add('hidden');
            }
        });
    }

    function renderItems(cart) {
        if (!els.summaryItems) {
            return;
        }
        els.summaryItems.innerHTML = '';
        cart.items.forEach((item) => {
            const row = document.createElement('li');
            row.className = 'flex justify-between text-sm text-gray-700';
            row.innerHTML = `
                <span>${escapeHtml(item.bookTitle || 'Sách')}</span>
                <span>${item.quantity} x ${formatCurrency(item.unitPrice)}</span>`;
            els.summaryItems.appendChild(row);
        });
    }

    function renderSummary(cart) {
        const subtotal = cart.subtotal;
        const shipping = DEFAULT_SHIPPING_FEE;
        const total = subtotal + shipping;
        if (els.summarySubtotal) {
            els.summarySubtotal.textContent = formatCurrency(subtotal);
        }
        if (els.summaryShipping) {
            els.summaryShipping.textContent = formatCurrency(shipping);
        }
        if (els.summaryTotal) {
            els.summaryTotal.textContent = formatCurrency(total);
        }
        if (els.summaryCount) {
            els.summaryCount.textContent = cart.totalQuantity;
        }
        if (els.currency) {
            els.currency.textContent = cart.currency || 'VND';
        }
        return { subtotal: subtotal, shipping: shipping, total: total };
    }

    async function loadCart() {
        toggleSection('loading');
        try {
            const response = await fetch(cartApi, { credentials: 'same-origin' });
            if (!response.ok) {
                throw new Error('Không thể tải giỏ hàng');
            }
            const payload = await response.json();
            const cart = payload && payload.cart ? payload.cart : null;
            if (!cart || cart.totalQuantity === 0) {
                toggleSection('empty');
                return null;
            }
            renderItems(cart);
            const totals = renderSummary(cart);
            toggleSection('content');
            return { cart: cart, totals: totals };
        } catch (error) {
            console.error(error);
            showError('Không thể tải dữ liệu giỏ hàng. Vui lòng thử lại.');
            return null;
        }
    }

    function showError(message) {
        if (els.error) {
            els.error.textContent = message || 'Có lỗi xảy ra. Vui lòng thử lại.';
            toggleSection('error');
        }
    }

    function showSuccess(message) {
        if (els.success) {
            els.success.textContent = message || 'Đặt hàng thành công!';
            toggleSection('success');
        }
    }

    function collectFormData() {
        if (!els.form) {
            return null;
        }
        const data = new FormData(els.form);
        return {
            fullName: data.get('fullName') || '',
            email: data.get('email') || '',
            phone: data.get('phone') || '',
            address: data.get('address') || '',
            city: data.get('city') || '',
            postalCode: data.get('postalCode') || '',
            country: data.get('country') || '',
            notes: data.get('notes') || '',
            paymentMethod: data.get('paymentMethod') || 'cod',
            paymentProvider: data.get('paymentProvider') || 'manual',
            paymentReference: data.get('paymentReference') || '',
            customerMessage: data.get('customerMessage') || '',
            shippingFee: DEFAULT_SHIPPING_FEE,
            taxAmount: 0,
            discountAmount: 0,
            currency: 'VND'
        };
    }

    function validatePayload(payload) {
        if (!payload.fullName.trim()) {
            throw new Error('Vui lòng nhập họ tên người nhận.');
        }
        if (!payload.email.trim()) {
            throw new Error('Vui lòng nhập email liên hệ.');
        }
        if (!payload.address.trim()) {
            throw new Error('Vui lòng nhập địa chỉ giao hàng.');
        }
    }

    async function submitCheckout(cartTotals) {
        const payload = collectFormData();
        if (!payload) {
            showError('Không thể đọc dữ liệu biểu mẫu.');
            return;
        }
        try {
            validatePayload(payload);
        } catch (validationError) {
            showError(validationError.message);
            return;
        }
        payload.shippingFee = cartTotals.shipping;
        payload.taxAmount = 0;
        payload.discountAmount = 0;
        payload.currency = 'VND';

        const token = shell.getAuthToken && shell.getAuthToken();
        if (!token) {
            showError('Vui lòng đăng nhập trước khi thanh toán.');
            return;
        }

        if (els.submitBtn) {
            els.submitBtn.disabled = true;
            els.submitBtn.textContent = 'Đang xử lý...';
        }

        try {
            const response = await fetch(orderApi, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + token
                },
                credentials: 'same-origin',
                body: JSON.stringify(payload)
            });
            if (response.status === 401) {
                throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            }
            if (!response.ok) {
                const errorPayload = await response.json().catch(() => null);
                const message = errorPayload && errorPayload.message ? errorPayload.message : 'Không thể hoàn tất thanh toán.';
                throw new Error(message);
            }
            shell.requestCartRefresh();
            showSuccess('Đặt hàng thành công! Đang chuyển đến trang đơn hàng...');
            setTimeout(function () {
                window.location.href = contextPath + '/orders.jsp';
            }, 1500);
        } catch (error) {
            console.error(error);
            showError(error.message || 'Không thể hoàn tất thanh toán.');
        } finally {
            if (els.submitBtn) {
                els.submitBtn.disabled = false;
                els.submitBtn.textContent = 'Đặt hàng';
            }
        }
    }

    onReady(async function () {
        queryElements();
        const loaded = await loadCart();
        if (!loaded || !loaded.cart) {
            return;
        }
        if (els.form) {
            els.form.addEventListener('submit', function (event) {
                event.preventDefault();
                submitCheckout(loaded.totals);
            });
        }
    });
})(window, document);
