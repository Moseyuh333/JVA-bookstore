/**
 * File: /assets/js/seller_dashboard.js
 * Chứa logic AJAX và tương tác chung cho Seller Dashboard.
 */

const BASE_URL = window.contextPath || ''; // contextPath được nhúng từ JSP
const API_BASE = `${BASE_URL}/api/seller`;
const SHOP_ID = window.shopId || (localStorage.getItem('shop_id') || 0);
const TOKEN = localStorage.getItem('seller_token') || localStorage.getItem('auth_token');

// Định dạng tiền tệ
const currencyFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

// =========================================================================
// I. HÀM TIỆN ÍCH
// =========================================================================

function getAuthHeaders() {
    if (!TOKEN) {
        // Nếu token không có, chuyển hướng về đăng xuất để xóa session
        window.location.href = `${BASE_URL}/logout-clear.jsp`;
        return {};
    }
    return {
        'Authorization': `Bearer ${TOKEN}`,
        'Content-Type': 'application/json'
    };
}

function handleAuthError(response) {
    if (response.status === 401 || response.status === 403) {
        alert("Phiên đăng nhập hết hạn hoặc không có quyền. Vui lòng đăng nhập lại.");
        window.location.href = `${BASE_URL}/logout-clear.jsp`;
        return true;
    }
    return false;
}

// =========================================================================
// II. LOGIC TẢI DỮ LIỆU TỔNG QUAN (Cho sellerDashboard.jsp)
// =========================================================================

async function loadDashboardStats() {
    if (SHOP_ID === 0) return;

    try {
        const productStatsPromise = fetch(`${API_BASE}/products?action=stats&shop_id=${SHOP_ID}`, { headers: getAuthHeaders() });
        const orderStatsPromise = fetch(`${API_BASE}/orders?action=stats&shop_id=${SHOP_ID}`, { headers: getAuthHeaders() });

        const [productRes, orderRes] = await Promise.all([productStatsPromise, orderStatsPromise]);

        if (handleAuthError(productRes) || handleAuthError(orderRes)) return;

        const productData = await productRes.json().catch(() => ({}));
        const orderData = await orderRes.json().catch(() => ({}));

        // Cập nhật DOM trong sellerDashboard.jsp
        if (productData.success || productData.total !== undefined) {
            document.getElementById('totalProductsValue').textContent = productData.total || 0;
        }
        if (orderData.success || orderData.monthlyRevenue !== undefined) {
            document.getElementById('newOrdersValue').textContent = orderData.newOrders || 0;
            document.getElementById('monthlyRevenueValue').textContent = currencyFormatter.format(orderData.monthlyRevenue || 0);
            document.getElementById('avgRatingValue').textContent = orderData.avgRating || '0.0';
        }

    } catch (error) {
        console.error("Lỗi tải Dashboard Stats:", error);
        // Hiển thị thông báo lỗi trên Dashboard
    }
}


// =========================================================================
// III. LOGIC TẢI DỮ LIỆU SẢN PHẨM (Cho sellerProduct.jsp)
// =========================================================================

async function loadProductList(page = 1, limit = 20) {
    if (SHOP_ID === 0) return;
    
    const container = document.getElementById('product'); 
    container.innerHTML = '<tr><td colspan="9" class="text-center">Đang tải...</td></tr>';

    try {
        let url = `${API_BASE}/products?action=list&shop_id=${SHOP_ID}&page=${page}&limit=${limit}`;
        // Thêm các tham số tìm kiếm/lọc từ DOM nếu cần

        const response = await fetch(url, { headers: getAuthHeaders() });
        
        if (handleAuthError(response)) return;

        const data = await response.json();
        
        if (data.success) {
            // Cập nhật thống kê chi tiết (nếu trang có)
            if (document.getElementById('totalProducts')) {
                document.getElementById('totalProducts').textContent = data.stats.total_books;
                document.getElementById('inStock').textContent = data.stats.in_stock;
                document.getElementById('outOfStock').textContent = data.stats.out_stock;
            }
            
            renderProductTable(data.products, container);

        } else {
            container.innerHTML = `<tr><td colspan="9" class="text-center text-danger">Lỗi API: ${data.message || 'Không thể tải danh sách sản phẩm.'}</td></tr>`;
        }
    } catch (error) {
        console.error("Lỗi tải Sản phẩm:", error);
        container.innerHTML = '<tr><td colspan="9" class="text-center text-danger">Lỗi kết nối Server.</td></tr>';
    }
}

function renderProductTable(products, container) {
    if (products.length === 0) {
        container.innerHTML = '<tr><td colspan="9" class="text-center">Không tìm thấy sản phẩm nào.</td></tr>';
        return;
    }
    // ... (logic tạo HTML cho bảng sản phẩm, tương tự như ví dụ trước)
    // container.innerHTML = '...'; 
}

// =========================================================================
// IV. LOGIC TẢI DỮ LIỆU KHÁC (Cần triển khai đầy đủ)
// =========================================================================

// async function loadOrdersList(page = 1) { ... }
// async function loadAnalyticsData() { ... }
// async function loadShopProfile() { ... }


// =========================================================================
// V. CHẠY KHI TẢI TRANG
// =========================================================================

// Hàm chạy khi DOM đã sẵn sàng để kiểm tra trang hiện tại và gọi hàm tải dữ liệu thích hợp
document.addEventListener('DOMContentLoaded', () => {
    // 1. Nhúng biến từ JSP vào phạm vi window (Chỉ cần nếu bạn không include JS này sau khi setAttribute)
    // window.contextPath = window.contextPath || '${pageContext.request.contextPath}';
    // window.shopId = window.shopId || ${shopId}; 

    const path = window.location.pathname;

    if (path.endsWith('/seller/dashboard')) {
        loadDashboardStats();
    } else if (path.endsWith('/seller/products')) {
        loadProductList();
    } 
    // Thêm các điều kiện khác cho /seller/orders, /seller/analytics, etc.
});