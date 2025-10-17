(function (window, document) {
    'use strict';

    const shell = window.appShell || {};
    const contextPath = shell.contextPath || '';
    const ordersApi = contextPath + '/api/orders';
    const { formatCurrency = (v) => v, onReady = (fn) => fn } = shell;

    const els = {};

    function queryElements() {
        els.loading = document.getElementById('ordersLoading');
        els.empty = document.getElementById('ordersEmpty');
        els.error = document.getElementById('ordersError');
        els.list = document.getElementById('ordersList');
    }

    function toggleState(state) {
        ['loading', 'empty', 'error'].forEach((key) => {
            if (!els[key]) {
                return;
            }
            if (state === key) {
                els[key].classList.remove('hidden');
            } else {
                els[key].classList.add('hidden');
            }
        });
        if (els.list) {
            if (state === 'loading' || state === 'error' || state === 'empty') {
                els.list.classList.add('hidden');
            } else {
                els.list.classList.remove('hidden');
            }
        }
    }

    function formatDate(value) {
        if (!value) {
            return '—';
        }
        try {
            const date = new Date(value);
            return date.toLocaleString('vi-VN');
        } catch (error) {
            return value;
        }
    }

    function renderOrderItem(order) {
        const container = document.createElement('article');
        container.className = 'rounded-lg border border-gray-200 bg-white p-4 shadow-sm transition hover:shadow-md';

        const header = document.createElement('div');
        header.className = 'flex items-start justify-between gap-4';
        const totalAmount = order.total || order.totalAmount || 0;
        header.innerHTML = `
            <div>
                <h3 class="text-base font-semibold text-gray-900">Đơn hàng #${order.id}</h3>
                <p class="text-sm text-gray-500">${formatDate(order.createdAt)}</p>
            </div>
            <div class="text-right">
                <p class="text-sm text-gray-500">Tổng tiền</p>
                <p class="text-lg font-semibold text-gray-900">${formatCurrency(totalAmount)}</p>
            </div>`;

        container.appendChild(header);

        const meta = document.createElement('dl');
        meta.className = 'mt-3 grid grid-cols-2 gap-2 text-sm text-gray-600';
        const paymentMethod = order.paymentMethod || (order.payments && order.payments[0] && order.payments[0].method) || 'COD';
        const shippingData = order.shipping || {};
        const shippingAddress = shippingData.address || order.shippingAddress || 'Không có';
        meta.innerHTML = `
            <div>
                <dt class="font-medium text-gray-500">Trạng thái</dt>
                <dd>${order.status || 'Đang xử lý'}</dd>
            </div>
            <div>
                <dt class="font-medium text-gray-500">Phương thức thanh toán</dt>
                <dd>${paymentMethod}</dd>
            </div>
            <div class="col-span-2">
                <dt class="font-medium text-gray-500">Địa chỉ giao hàng</dt>
                <dd>${shell.escapeHtml(shippingAddress)}</dd>
            </div>`;

        container.appendChild(meta);

        if (Array.isArray(order.items) && order.items.length > 0) {
            const itemsList = document.createElement('ul');
            itemsList.className = 'mt-4 divide-y divide-gray-200 rounded-lg border border-gray-100 bg-gray-50';
            order.items.forEach((item) => {
                const li = document.createElement('li');
                li.className = 'flex items-center justify-between gap-2 p-3 text-sm text-gray-700';
                const title = shell.escapeHtml(item.bookTitle || item.title || 'Sách');
                const unitPrice = formatCurrency(item.unitPrice || item.totalPrice);
                const totalPrice = formatCurrency(item.totalPrice || 0);
                li.innerHTML = `
                    <span class="flex-1 font-medium text-gray-900">${title}</span>
                    <span class="w-20 text-right">${item.quantity} x ${unitPrice}</span>
                    <span class="w-24 text-right font-semibold">${totalPrice}</span>`;
                itemsList.appendChild(li);
            });
            container.appendChild(itemsList);
        }

        return container;
    }

    function renderOrders(data) {
        if (!els.list) {
            return;
        }
        els.list.innerHTML = '';
        data.forEach((order) => {
            const node = renderOrderItem(order);
            els.list.appendChild(node);
        });
    }

    async function loadOrders() {
        toggleState('loading');
        try {
            const token = shell.getAuthToken && shell.getAuthToken();
            if (!token) {
                throw new Error('Vui lòng đăng nhập để xem đơn hàng.');
            }
            const response = await fetch(ordersApi, {
                headers: {
                    'Authorization': 'Bearer ' + token
                },
                credentials: 'same-origin'
            });
            if (response.status === 401) {
                throw new Error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
            }
            if (!response.ok) {
                throw new Error('Không thể tải danh sách đơn hàng.');
            }
            const payload = await response.json();
            const orders = payload && payload.orders ? payload.orders : [];
            if (orders.length === 0) {
                toggleState('empty');
                return;
            }
            renderOrders(orders);
            toggleState('ready');
        } catch (error) {
            console.error(error);
            if (els.error) {
                els.error.textContent = error.message || 'Không thể tải đơn hàng.';
            }
            toggleState('error');
        }
    }

    onReady(function () {
        queryElements();
        loadOrders();
    });
})(window, document);
