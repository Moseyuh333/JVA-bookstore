(function (window, document) {
    'use strict';

    var appShell = window.appShell;
    var apiClient = window.apiClient;
    if (!appShell || !apiClient) {
        return;
    }

    var listeners = [];
    var lastCart = null;
    var pendingFetch = null;

    function notify() {
        listeners.forEach(function (listener) {
            try {
                listener(lastCart);
            } catch (error) {
                console.error('cartClient listener error', error);
            }
        });
        updateBadge();
    }

    function onChange(listener) {
        if (typeof listener === 'function') {
            listeners.push(listener);
            if (lastCart) {
                try {
                    listener(lastCart);
                } catch (error) {
                    console.error('cartClient listener error', error);
                }
            }
        }
    }

    function badgeElement() {
        return document.getElementById('cartCountBadge');
    }

    function totalQuantity(cart) {
        if (!cart || !Array.isArray(cart.items)) {
            return 0;
        }
        return cart.items.reduce(function (sum, item) {
            return sum + (item.quantity || 0);
        }, 0);
    }

    function updateBadge() {
        var badge = badgeElement();
        if (!badge) {
            return;
        }
        var count = totalQuantity(lastCart);
        badge.textContent = count > 99 ? '99+' : String(count);
        if (count > 0) {
            badge.classList.remove('hidden');
        } else {
            badge.classList.add('hidden');
        }
    }

    async function fetchCart() {
        if (pendingFetch) {
            return pendingFetch;
        }
        pendingFetch = (async function () {
            try {
                var data = await apiClient.get('/cart');
                if (data && data.success) {
                    lastCart = data.cart;
                    notify();
                }
                return lastCart;
            } catch (error) {
                console.error('cartClient fetchCart error', error);
                throw error;
            } finally {
                pendingFetch = null;
            }
        })();
        return pendingFetch;
    }

    async function addItem(bookId, quantity) {
        quantity = quantity || 1;
        if (!bookId || quantity <= 0) {
            return;
        }
        var body = { bookId: bookId, quantity: quantity };
        try {
            var result = await apiClient.post('/cart/items', body);
            if (result && result.success) {
                lastCart = result.cart;
                notify();
            }
            return result;
        } catch (error) {
            console.error('cartClient addItem error', error);
            throw error;
        }
    }

    async function updateQuantity(cartId, bookId, quantity) {
        if (!bookId || quantity < 0) {
            return;
        }
        try {
            var result = await apiClient.put('/cart/items/' + bookId, { quantity: quantity });
            if (result && result.success) {
                lastCart = result.cart;
                notify();
            }
            return result;
        } catch (error) {
            console.error('cartClient updateQuantity error', error);
            throw error;
        }
    }

    async function removeItem(bookId) {
        if (!bookId) {
            return;
        }
        try {
            var result = await apiClient.del('/cart/items/' + bookId);
            if (result && result.success) {
                lastCart = result.cart;
                notify();
            }
            return result;
        } catch (error) {
            console.error('cartClient removeItem error', error);
            throw error;
        }
    }

    async function clearCart() {
        try {
            var result = await apiClient.del('/cart');
            if (result && result.success) {
                lastCart = result.cart;
                notify();
            }
            return result;
        } catch (error) {
            console.error('cartClient clearCart error', error);
            throw error;
        }
    }

    async function checkout(payload) {
        if (!payload || !payload.addressId) {
            throw new Error('Thiếu địa chỉ giao hàng');
        }
        var requestBody = {
            addressId: payload.addressId,
            paymentMethod: payload.paymentMethod || 'cod',
            couponCode: payload.couponCode || null,
            notes: payload.notes || null
        };
        if (Array.isArray(payload.items) && payload.items.length > 0) {
            requestBody.items = payload.items;
        }
        if (payload.mode) {
            requestBody.mode = String(payload.mode);
        }
        if (payload.paymentDetails) {
            requestBody.paymentDetails = payload.paymentDetails;
        }
        if (payload.shopId) {
            requestBody.shopId = payload.shopId;
        }
        return apiClient.post('/checkout', requestBody);
    }

    async function startBuyNow(bookId, quantity) {
        if (!bookId || quantity <= 0) {
            throw new Error('Thông tin sản phẩm không hợp lệ');
        }
        var body = { bookId: bookId, quantity: quantity };
        return apiClient.post('/checkout/buy-now', body);
    }

    function initAddToCartButtons() {
        document.addEventListener('click', function (event) {
            var button = event.target.closest('[data-add-to-cart]');
            if (!button) {
                return;
            }
            event.preventDefault();
            var bookId = parseInt(button.getAttribute('data-book-id'), 10);
            if (Number.isNaN(bookId) || bookId <= 0) {
                return;
            }
            var qty = parseInt(button.getAttribute('data-quantity') || '1', 10) || 1;
            button.disabled = true;
            button.classList.add('opacity-60');
            addItem(bookId, qty)
                .then(function () {
                    showToast('Đã thêm vào giỏ hàng');
                })
                .catch(function (error) {
                    console.error(error);
                    showToast('Không thể thêm sản phẩm. Vui lòng thử lại.', true);
                })
                .finally(function () {
                    button.disabled = false;
                    button.classList.remove('opacity-60');
                });
        });
    }

    function showToast(message, isError) {
        var container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.style.position = 'fixed';
            container.style.top = '20px';
            container.style.right = '20px';
            container.style.zIndex = '1050';
            document.body.appendChild(container);
        }
        var toast = document.createElement('div');
        toast.textContent = message;
        toast.className = 'mb-2 px-4 py-3 rounded-lg shadow text-sm ' + (isError ? 'bg-red-600 text-white' : 'bg-emerald-600 text-white');
        container.appendChild(toast);
        setTimeout(function () {
            toast.classList.add('opacity-0');
            toast.classList.add('transition');
            toast.classList.add('duration-300');
            setTimeout(function () {
                toast.remove();
            }, 300);
        }, 2500);
    }

    appShell.onReady(function () {
        initAddToCartButtons();
        fetchCart().catch(function (error) {
            console.warn('Unable to prefetch cart', error);
        });
    });

    window.cartClient = {
        fetchCart: fetchCart,
        addItem: addItem,
        updateQuantity: updateQuantity,
        removeItem: removeItem,
        clearCart: clearCart,
        checkout: checkout,
        onChange: onChange,
        startBuyNow: startBuyNow,
        showToast: showToast,
        lastCart: function () { return lastCart; }
    };

})(window, document);
