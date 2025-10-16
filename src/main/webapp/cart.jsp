<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giỏ hàng - NKBookstore</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        .quantity-input {
            width: 70px;
        }
        .product-image {
            width: 80px;
            height: 100px;
            object-fit: cover;
        }
    </style>
</head>
<body>

    <div class="container py-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1><i class="fas fa-shopping-cart me-2"></i>Giỏ hàng của bạn</h1>
            <a href="${pageContext.request.contextPath}/" class="btn btn-outline-primary">
                <i class="fas fa-plus me-1"></i>Tiếp tục mua sắm
            </a>
        </div>

        <div id="cart-container">
            <!-- Cart items will be loaded here by JavaScript -->
            <div class="text-center" id="loading-spinner">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">Loading...</span>
                </div>
                <p class="mt-2">Đang tải giỏ hàng...</p>
            </div>
        </div>

        <div class="d-flex justify-content-end mt-4" id="checkout-section" style="display: none;">
            <button class="btn btn-primary btn-lg">
                Tiến hành thanh toán <i class="fas fa-arrow-right ms-2"></i>
            </button>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const token = localStorage.getItem('jwtToken');
            if (!token) {
                window.location.href = 'login.jsp?redirect=cart.jsp';
                return;
            }

            const API_BASE_URL = '${pageContext.request.contextPath}/api';
            const cartContainer = document.getElementById('cart-container');
            const loadingSpinner = document.getElementById('loading-spinner');
            const checkoutSection = document.getElementById('checkout-section');

            async function fetchCartItems() {
                try {
                    const response = await fetch(`${API_BASE_URL}/cart`, {
                        headers: { 'Authorization': `Bearer ${token}` }
                    });

                    if (!response.ok) {
                        if (response.status === 401 || response.status === 403) {
                            localStorage.removeItem('jwtToken');
                            window.location.href = 'login.jsp?redirect=cart.jsp';
                        }
                        throw new Error('Failed to load cart');
                    }

                    const items = await response.json();
                    renderCart(items);

                } catch (error) {
                    console.error('Error fetching cart:', error);
                    cartContainer.innerHTML = `<div class="alert alert-danger">Không thể tải giỏ hàng. Vui lòng thử lại.</div>`;
                } finally {
                    loadingSpinner.style.display = 'none';
                }
            }

            function renderCart(items) {
                if (items.length === 0) {
                    cartContainer.innerHTML = `
                        <div class="text-center py-5">
                            <i class="fas fa-shopping-cart fa-3x text-muted mb-3"></i>
                            <h4>Giỏ hàng của bạn đang trống</h4>
                            <p>Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm nhé!</p>
                        </div>`;
                    checkoutSection.style.display = 'none';
                    return;
                }

                let subtotal = 0;
                const itemsHtml = items.map(item => {
                    const itemTotal = item.book.price * item.quantity;
                    subtotal += itemTotal;
                    return `
                        <div class="card mb-3" data-item-id="${item.id}">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-md-2">
                                        <img src="${item.book.imageUrl || 'https://via.placeholder.com/80x100'}" alt="${item.book.title}" class="img-fluid rounded product-image">
                                    </div>
                                    <div class="col-md-4">
                                        <h5 class="mb-1">${item.book.title}</h5>
                                        <p class="text-muted mb-0">${item.book.author}</p>
                                    </div>
                                    <div class="col-md-2">
                                        <strong>${formatCurrency(item.book.price)}</strong>
                                    </div>
                                    <div class="col-md-2">
                                        <div class="d-flex align-items-center">
                                            <input type="number" class="form-control form-control-sm quantity-input text-center" value="${item.quantity}" min="1" data-item-id="${item.id}">
                                        </div>
                                    </div>
                                    <div class="col-md-1 text-end">
                                        <strong>${formatCurrency(itemTotal)}</strong>
                                    </div>
                                    <div class="col-md-1 text-end">
                                        <button class="btn btn-sm btn-outline-danger remove-btn" data-item-id="${item.id}">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('');

                const summaryHtml = `
                    <div class="card">
                        <div class="card-body d-flex justify-content-end align-items-center">
                            <h5 class="mb-0 me-3">Tổng cộng:</h5>
                            <h4 class="mb-0 text-primary"><strong>${formatCurrency(subtotal)}</strong></h4>
                        </div>
                    </div>
                `;

                cartContainer.innerHTML = itemsHtml + summaryHtml;
                checkoutSection.style.display = 'flex';
                addEventListeners();
            }
            
            function addEventListeners() {
                document.querySelectorAll('.quantity-input').forEach(input => {
                    input.addEventListener('change', handleQuantityChange);
                });
                document.querySelectorAll('.remove-btn').forEach(button => {
                    button.addEventListener('click', handleRemoveItem);
                });
            }

            async function handleQuantityChange(event) {
                const cartItemId = event.target.dataset.itemId;
                const newQuantity = parseInt(event.target.value);

                if (newQuantity < 1) {
                    event.target.value = 1; // Reset to 1 if invalid
                    return;
                }
                
                try {
                    const response = await fetch(`${API_BASE_URL}/cart/update`, {
                        method: 'PUT',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${token}`
                        },
                        body: JSON.stringify({ cartItemId: cartItemId, quantity: newQuantity })
                    });

                    if (response.ok) {
                        fetchCartItems(); // Refresh cart
                    } else {
                        alert('Lỗi cập nhật số lượng.');
                    }
                } catch (error) {
                    console.error('Error updating quantity:', error);
                    alert('Có lỗi xảy ra. Vui lòng thử lại.');
                }
            }

            async function handleRemoveItem(event) {
                const cartItemId = event.currentTarget.dataset.itemId;
                
                if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?')) {
                    return;
                }

                try {
                    const response = await fetch(`${API_BASE_URL}/cart/remove/${cartItemId}`, {
                        method: 'DELETE',
                        headers: { 'Authorization': `Bearer ${token}` }
                    });

                    if (response.ok) {
                        fetchCartItems(); // Refresh cart
                    } else {
                        alert('Lỗi xóa sản phẩm.');
                    }
                } catch (error) {
                    console.error('Error removing item:', error);
                    alert('Có lỗi xảy ra. Vui lòng thử lại.');
                }
            }

            function formatCurrency(value) {
                return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
            }

            fetchCartItems();
        });
    </script>
</body>
</html>
