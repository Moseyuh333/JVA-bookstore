(function (window, document) {
    'use strict';

    const shell = window.appShell || {};
    const contextPath = shell.contextPath || '';
    const cartApi = shell.cartApiBase || (contextPath + '/api/cart');
    const { formatCurrency = (v) => v, escapeHtml = (v) => v, onReady = (fn) => fn } = shell;

    const els = {};

    function queryElements() {
        els.loading = document.getElementById('cartLoading');
        els.error = document.getElementById('cartError');
        els.empty = document.getElementById('cartEmpty');
        els.content = document.getElementById('cartContent');
        els.items = document.getElementById('cartItems');
        els.subtotal = document.getElementById('cartSubtotal');
        els.total = document.getElementById('cartTotal');
        els.count = document.getElementById('cartItemCount');
        els.currency = document.getElementById('cartCurrency');
        els.clearBtn = document.getElementById('clearCartBtn');
        els.checkoutBtn = document.getElementById('checkoutBtn');
    }

    function showSection(target) {
        ['loading', 'error', 'empty', 'content'].forEach((name) => {
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

    function showError(message) {
        if (!els.error) {
            return;
        }
        els.error.textContent = message || 'Đã xảy ra lỗi. Vui lòng thử lại.';
        showSection('error');
    }

    function renderItems(cart) {
        if (!els.items) {
            return;
        }
        els.items.innerHTML = '';
        cart.items.forEach((item) => {
            const wrapper = document.createElement('div');
            wrapper.className = 'bg-white rounded-lg shadow-sm p-4 flex flex-col sm:flex-row sm:items-center gap-4';
            wrapper.dataset.bookId = item.bookId;
            const title = escapeHtml(item.bookTitle || 'Sách chưa có tên');
            const author = escapeHtml(item.bookAuthor || '');
            const price = formatCurrency(item.unitPrice);
            const lineTotal = formatCurrency(item.lineTotal);
            wrapper.innerHTML = `
                <div class="flex-1">
                    <h3 class="text-lg font-semibold text-gray-800">${title}</h3>
                    <p class="text-sm text-gray-500">${author}</p>
                    <p class="text-sm text-gray-500 mt-1">Đơn giá: <span class="font-medium text-gray-800">${price}</span></p>
                </div>
                <div class="flex items-center gap-2">
                    <button type="button" class="quantity-btn" data-action="decrement" aria-label="Giảm số lượng">
                        <i data-feather="minus" class="w-4 h-4"></i>
                    </button>
                    <input type="number" class="w-16 text-center border rounded-md py-1" min="0" value="${item.quantity}" aria-label="Số lượng">
                    <button type="button" class="quantity-btn" data-action="increment" aria-label="Tăng số lượng">
                        <i data-feather="plus" class="w-4 h-4"></i>
                    </button>
                </div>
                <div class="text-right">
                    <p class="text-sm text-gray-500">Thành tiền</p>
                    <p class="text-lg font-semibold text-amber-700">${lineTotal}</p>
                    <button type="button" class="text-sm text-red-500 hover:text-red-600 mt-2" data-action="remove">Xóa</button>
                </div>`;
            els.items.appendChild(wrapper);
        });
        shell.refreshIcons();
    }

    function renderSummary(cart) {
        const subtotal = cart.subtotal != null ? cart.subtotal : 0;
        const total = cart.total != null ? cart.total : subtotal;
        if (els.subtotal) {
            els.subtotal.textContent = formatCurrency(subtotal);
        }
        if (els.total) {
            els.total.textContent = formatCurrency(total);
        }
        if (els.count) {
            els.count.textContent = cart.totalQuantity;
        }
        if (els.currency) {
            els.currency.textContent = cart.currency || 'VND';
        }
    }

    function renderCart(cart) {
        if (!cart || cart.totalQuantity === 0) {
            showSection('empty');
            shell.setCartBadgeCount(0);
            return;
        }
        renderItems(cart);
        renderSummary(cart);
        shell.setCartBadgeCount(cart.totalQuantity);
        showSection('content');
    }

    async function fetchCart() {
        showSection('loading');
        try {
            const response = await fetch(cartApi, { credentials: 'same-origin' });
            if (!response.ok) {
                throw new Error('Unable to load cart');
            }
            const payload = await response.json();
            const cart = payload && payload.cart ? payload.cart : null;
            if (!cart || cart.totalQuantity === 0) {
                showSection('empty');
                shell.setCartBadgeCount(0);
                return;
            }
            renderCart(cart);
            shell.requestCartRefresh();
        } catch (error) {
            console.error(error);
            showError('Không thể tải giỏ hàng. Vui lòng thử lại sau.');
        }
    }

    async function updateQuantity(bookId, quantity) {
        try {
            const body = JSON.stringify({ quantity: quantity });
            const response = await fetch(`${cartApi}/items/${bookId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'same-origin',
                body: body
            });
            if (!response.ok) {
                throw new Error('Update failed');
            }
            const payload = await response.json();
            renderCart(payload.cart);
            shell.requestCartRefresh();
        } catch (error) {
            console.error(error);
            showError('Không thể cập nhật số lượng. Vui lòng thử lại.');
        }
    }

    async function removeItem(bookId) {
        try {
            const response = await fetch(`${cartApi}/items/${bookId}`, {
                method: 'DELETE',
                credentials: 'same-origin'
            });
            if (!response.ok) {
                throw new Error('Remove failed');
            }
            const payload = await response.json();
            renderCart(payload.cart);
            shell.requestCartRefresh();
        } catch (error) {
            console.error(error);
            showError('Không thể xóa sản phẩm. Vui lòng thử lại.');
        }
    }

    async function clearCart() {
        try {
            const response = await fetch(cartApi, {
                method: 'DELETE',
                credentials: 'same-origin'
            });
            if (!response.ok) {
                throw new Error('Clear failed');
            }
            const payload = await response.json();
            renderCart(payload.cart);
            shell.requestCartRefresh();
        } catch (error) {
            console.error(error);
            showError('Không thể làm trống giỏ hàng. Vui lòng thử lại.');
        }
    }

    function bindEvents() {
        if (els.items) {
            els.items.addEventListener('click', function (event) {
                const target = event.target.closest('button[data-action]');
                if (!target) {
                    return;
                }
                const wrapper = event.target.closest('[data-book-id]');
                if (!wrapper) {
                    return;
                }
                const bookId = wrapper.dataset.bookId;
                const input = wrapper.querySelector('input[type="number"]');
                const action = target.dataset.action;
                let next = parseInt(input.value, 10) || 0;
                if (action === 'increment') {
                    next += 1;
                } else if (action === 'decrement') {
                    next = Math.max(0, next - 1);
                } else if (action === 'remove') {
                    removeItem(bookId);
                    return;
                }
                if (action === 'remove') {
                    return;
                }
                if (next <= 0) {
                    removeItem(bookId);
                } else {
                    updateQuantity(bookId, next);
                }
            });
            els.items.addEventListener('change', function (event) {
                const input = event.target;
                if (input && input.matches('input[type="number"]')) {
                    const wrapper = input.closest('[data-book-id]');
                    if (!wrapper) {
                        return;
                    }
                    const bookId = wrapper.dataset.bookId;
                    let next = parseInt(input.value, 10);
                    if (Number.isNaN(next) || next < 0) {
                        next = 0;
                    }
                    if (next === 0) {
                        removeItem(bookId);
                    } else {
                        updateQuantity(bookId, next);
                    }
                }
            });
        }
        if (els.clearBtn) {
            els.clearBtn.addEventListener('click', function () {
                clearCart();
            });
        }
        if (els.checkoutBtn) {
            els.checkoutBtn.addEventListener('click', function () {
                window.location.href = contextPath + '/checkout.jsp';
            });
        }
    }

    onReady(function () {
        queryElements();
        bindEvents();
        fetchCart();
    });
})(window, document);
