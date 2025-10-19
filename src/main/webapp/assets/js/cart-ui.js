(function (window, document) {
    'use strict';

    var appShell = window.appShell;
    var cartClient = window.cartClient;
    if (!appShell || !cartClient) {
        return;
    }

    var overlay;
    var drawer;
    var itemsContainer;
    var loadingEl;
    var emptyEl;
    var countEl;
    var subtotalEl;
    var feedbackEl;
    var checkoutBtn;
    var clearBtn;
    var feedbackBaseClass = '';
    var isSetup = false;

    function ensureSetup() {
        if (isSetup) {
            return true;
        }
        overlay = document.getElementById('cartDrawerOverlay');
        drawer = document.getElementById('cartDrawer');
        if (!overlay || !drawer) {
            return false;
        }
        itemsContainer = drawer.querySelector('[data-cart-items]');
        loadingEl = drawer.querySelector('[data-cart-loading]');
        emptyEl = drawer.querySelector('[data-cart-empty]');
        countEl = drawer.querySelector('[data-cart-count]');
        subtotalEl = drawer.querySelector('[data-cart-subtotal]');
        feedbackEl = drawer.querySelector('[data-cart-feedback]');
        checkoutBtn = drawer.querySelector('[data-cart-checkout]');
        clearBtn = drawer.querySelector('[data-cart-clear]');
        if (feedbackEl) {
            feedbackBaseClass = feedbackEl.className.replace(/\bhidden\b/g, '').trim();
        }

        document.querySelectorAll('[data-cart-open]').forEach(function (trigger) {
            trigger.addEventListener('click', function (event) {
                event.preventDefault();
                openDrawer();
            });
        });

        drawer.querySelectorAll('[data-cart-close]').forEach(function (button) {
            button.addEventListener('click', closeDrawer);
        });

        overlay.addEventListener('click', closeDrawer);

        document.addEventListener('keydown', function (event) {
            if (event.key === 'Escape' && isDrawerVisible()) {
                closeDrawer();
            }
        });

        if (itemsContainer) {
            itemsContainer.addEventListener('click', handleItemAction);
        }

        if (clearBtn) {
            clearBtn.addEventListener('click', handleClearCart);
        }

        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', function () {
                var target = (appShell.contextPath || '') + '/checkout.jsp';
                closeDrawer();
                window.location.href = target;
            });
        }

        cartClient.onChange(renderCart);
        isSetup = true;
        return true;
    }

    function isDrawerVisible() {
        return drawer && !drawer.classList.contains('hidden');
    }

    function showLoading() {
        if (loadingEl) {
            loadingEl.classList.remove('hidden');
        }
        if (itemsContainer) {
            itemsContainer.classList.add('hidden');
        }
        if (emptyEl) {
            emptyEl.classList.add('hidden');
        }
    }

    function hideLoading() {
        if (loadingEl) {
            loadingEl.classList.add('hidden');
        }
    }

    function openDrawer() {
        if (!ensureSetup()) {
            return;
        }
        if (!isDrawerVisible()) {
            overlay.classList.remove('hidden');
            drawer.classList.remove('hidden');
            document.body.classList.add('overflow-hidden');
            focusCloseButton();
        }
        showLoading();
        cartClient.fetchCart().catch(function (error) {
            showFeedback('error', 'Không thể tải giỏ hàng.');
            console.warn('cartUI fetch error', error);
            hideLoading();
        });
    }

    function closeDrawer() {
        if (!ensureSetup()) {
            return;
        }
        if (isDrawerVisible()) {
            drawer.classList.add('hidden');
            overlay.classList.add('hidden');
            document.body.classList.remove('overflow-hidden');
        }
    }

    function focusCloseButton() {
        var closeBtn = drawer.querySelector('[data-cart-close]');
        if (closeBtn && typeof closeBtn.focus === 'function') {
            setTimeout(function () {
                closeBtn.focus();
            }, 50);
        }
    }

    function handleItemAction(event) {
        var control = event.target.closest('[data-action]');
        if (!control) {
            return;
        }
        var action = control.getAttribute('data-action');
        var bookId = parseInt(control.getAttribute('data-book-id'), 10);
        if (!bookId || Number.isNaN(bookId)) {
            return;
        }
        var currentCart = cartClient.lastCart ? cartClient.lastCart() : null;
        var targetItem = currentCart && Array.isArray(currentCart.items)
            ? currentCart.items.find(function (item) { return item.bookId === bookId; })
            : null;
        var currentQty = targetItem ? targetItem.quantity || 0 : 0;
        disableControl(control, true);
        showFeedback();
        var promise;
        if (action === 'increment') {
            promise = cartClient.updateQuantity(null, bookId, currentQty + 1);
        } else if (action === 'decrement') {
            if (currentQty <= 1) {
                promise = cartClient.removeItem(bookId);
            } else {
                promise = cartClient.updateQuantity(null, bookId, currentQty - 1);
            }
        } else if (action === 'remove') {
            promise = cartClient.removeItem(bookId);
        } else {
            disableControl(control, false);
            return;
        }
        promise.catch(function (error) {
            console.error('cartUI item action error', error);
            showFeedback('error', extractErrorMessage(error));
        }).finally(function () {
            disableControl(control, false);
        });
    }

    function handleClearCart() {
        if (!cartClient.lastCart || !cartClient.lastCart() || !cartClient.lastCart().items || cartClient.lastCart().items.length === 0) {
            return;
        }
        if (!window.confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) {
            return;
        }
        disableControl(clearBtn, true);
        showFeedback();
        cartClient.clearCart()
            .catch(function (error) {
                console.error('cartUI clear error', error);
                showFeedback('error', extractErrorMessage(error));
            })
            .finally(function () {
                disableControl(clearBtn, false);
            });
    }

    function disableControl(control, state) {
        if (!control) {
            return;
        }
        control.disabled = state;
        if (state) {
            control.classList.add('opacity-50');
            control.classList.add('cursor-not-allowed');
        } else {
            control.classList.remove('opacity-50');
            control.classList.remove('cursor-not-allowed');
        }
    }

    function extractErrorMessage(error) {
        if (!error) {
            return 'Đã có lỗi xảy ra. Vui lòng thử lại.';
        }
        if (error.payload && error.payload.message) {
            return error.payload.message;
        }
        if (error.message) {
            return error.message;
        }
        return 'Đã có lỗi xảy ra. Vui lòng thử lại.';
    }

    function renderCart(cart) {
        if (!ensureSetup()) {
            return;
        }
        hideLoading();
        showFeedback();
        if (!cart || !Array.isArray(cart.items) || cart.items.length === 0) {
            if (itemsContainer) {
                itemsContainer.innerHTML = '';
                itemsContainer.classList.add('hidden');
            }
            if (emptyEl) {
                emptyEl.classList.remove('hidden');
            }
            if (countEl) {
                countEl.textContent = '0 sản phẩm';
            }
            if (subtotalEl) {
                subtotalEl.textContent = formatAmount(0);
            }
            if (checkoutBtn) {
                checkoutBtn.disabled = true;
            }
            if (clearBtn) {
                clearBtn.disabled = true;
            }
            refreshIcons();
            return;
        }

        var fragment = document.createDocumentFragment();
        cart.items.forEach(function (item) {
            fragment.appendChild(buildCartItem(item));
        });
        if (itemsContainer) {
            itemsContainer.innerHTML = '';
            itemsContainer.appendChild(fragment);
            itemsContainer.classList.remove('hidden');
        }
        if (emptyEl) {
            emptyEl.classList.add('hidden');
        }
        var totalQty = cart.items.reduce(function (sum, item) {
            return sum + (item.quantity || 0);
        }, 0);
        if (countEl) {
            countEl.textContent = totalQty + ' sản phẩm';
        }
        if (subtotalEl) {
            subtotalEl.textContent = formatAmount(cart.subtotal);
        }
        if (checkoutBtn) {
            checkoutBtn.disabled = false;
        }
        if (clearBtn) {
            clearBtn.disabled = false;
        }
        refreshIcons();
    }

    function buildCartItem(item) {
        var wrapper = document.createElement('article');
        wrapper.className = 'flex items-start gap-4 border border-gray-100 rounded-xl p-4 shadow-sm';
        wrapper.setAttribute('data-cart-item', String(item.bookId));
        var title = appShell.escapeHtml(item.title || 'Sách chưa cập nhật');
        var author = appShell.escapeHtml(item.author || 'Đang cập nhật');
        var image = item.imageUrl || 'https://placehold.co/120x160?text=Book';
        var unitPrice = formatAmount(item.unitPrice);
        var totalPrice = formatAmount(calculateItemTotal(item));

        wrapper.innerHTML = `
            <div class="w-16 h-20 flex-shrink-0 overflow-hidden rounded-lg bg-amber-50 border border-amber-100">
                <img src="${image}" alt="${title}" class="w-full h-full object-cover" />
            </div>
            <div class="flex-1 space-y-3">
                <div class="flex items-start justify-between gap-4">
                    <div>
                        <p class="font-semibold text-gray-800">${title}</p>
                        <p class="text-sm text-gray-500">${author}</p>
                    </div>
                    <span class="font-semibold text-amber-700 whitespace-nowrap">${unitPrice}</span>
                </div>
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
                    <div class="inline-flex items-center border border-gray-200 rounded-full overflow-hidden">
                        <button type="button" class="px-3 py-2 text-gray-600 hover:bg-gray-100 transition" data-action="decrement" data-book-id="${item.bookId}">
                            <span class="sr-only">Giảm số lượng</span>
                            <i data-feather="minus" class="w-4 h-4"></i>
                        </button>
                        <span class="px-4 font-semibold text-gray-800" data-cart-qty>${item.quantity || 0}</span>
                        <button type="button" class="px-3 py-2 text-gray-600 hover:bg-gray-100 transition" data-action="increment" data-book-id="${item.bookId}">
                            <span class="sr-only">Tăng số lượng</span>
                            <i data-feather="plus" class="w-4 h-4"></i>
                        </button>
                    </div>
                    <div class="flex items-center gap-3 text-sm text-gray-500">
                        <span>Tổng: <span class="font-semibold text-gray-700">${totalPrice}</span></span>
                        <button type="button" class="inline-flex items-center gap-1 text-red-500 hover:text-red-600 transition" data-action="remove" data-book-id="${item.bookId}">
                            <i data-feather="trash-2" class="w-4 h-4"></i>
                            <span>Xóa</span>
                        </button>
                    </div>
                </div>
            </div>`;
        return wrapper;
    }

    function calculateItemTotal(item) {
        var price = toNumber(item && item.unitPrice);
        var quantity = item && item.quantity ? item.quantity : 0;
        return price * quantity;
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

    function formatAmount(value) {
        return appShell.formatCurrency(toNumber(value));
    }

    function showFeedback(type, message) {
        if (!feedbackEl) {
            return;
        }
        if (!type || !message) {
            feedbackEl.className = (feedbackBaseClass ? feedbackBaseClass + ' ' : '') + 'hidden';
            feedbackEl.textContent = '';
            return;
        }
        var tone = type === 'error'
            ? 'bg-red-50 border border-red-200 text-red-600'
            : 'bg-emerald-50 border border-emerald-200 text-emerald-700';
        feedbackEl.className = (feedbackBaseClass ? feedbackBaseClass + ' ' : '') + tone + ' px-3 py-2 rounded-lg';
        feedbackEl.textContent = message;
        feedbackEl.classList.remove('hidden');
    }

    function refreshIcons() {
        if (appShell && typeof appShell.refreshIcons === 'function') {
            appShell.refreshIcons();
        }
    }

    function refreshCart() {
        cartClient.fetchCart().catch(function (error) {
            console.warn('cartUI refresh error', error);
        });
    }

    function bootstrap() {
        if (!ensureSetup()) {
            return;
        }
        refreshCart();
    }

    if (appShell && typeof appShell.onReady === 'function') {
        appShell.onReady(bootstrap);
    } else {
        bootstrap();
    }

    window.cartUI = {
        open: function () {
            openDrawer();
        },
        close: function () {
            closeDrawer();
        },
        refresh: refreshCart
    };
})(window, document);
