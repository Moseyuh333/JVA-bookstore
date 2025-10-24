<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- ... (Các taglib khác) ... --%>
<script>
    // ** CODE ĐÃ SỬA: Đảm bảo biến luôn là một chuỗi hợp lệ **
    // Nếu ${shopId} là null hoặc không được set, nó sẽ mặc định là '0'.
    const SHOP_ID = '<c:out value="${shopId}" default="0" />'; 
    
    // Nếu bạn cần nó là số nguyên trong JS (chú ý: lỗi có thể xảy ra nếu nó là chuỗi rỗng)
    // const SHOP_ID_INT = parseInt('<c:out value="${shopId}" default="0" />', 10);
    
    const API_URL = '${pageContext.request.contextPath}/api/seller/products';
    const currencyFormatter = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

    // Hàm loadProducts...
    async function loadProducts(page = 1, limit = 20) {
        document.getElementById('loadingState').style.display = 'block';
        
        const token = localStorage.getItem('seller_token');
        // SỬ DỤNG SHOP_ID đã được đảm bảo là chuỗi
        if (!token || SHOP_ID === '0') { 
             document.getElementById('product').innerHTML = '<tr><td colspan="9" class="text-center text-danger">Lỗi: Thiếu Shop ID (ID: ' + SHOP_ID + '). Vui lòng kiểm tra lại đăng nhập.</td></tr>';
             document.getElementById('loadingState').style.display = 'none';
             return;
        }

        // Truyền giá trị SHOP_ID
        let url = `${API_URL}?action=list&page=${page}&limit=${limit}&shop_id=${SHOP_ID}`;
        
        try {
            const response = await fetch(url, {
                method: 'GET',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            // ... (Tiếp tục xử lý response) ...
        } catch (error) {
            // ... (Xử lý lỗi) ...
        } finally {
            document.getElementById('loadingState').style.display = 'none';
        }
    }

    // ... (Các hàm updateStats, renderProducts, listeners) ...

    document.addEventListener('DOMContentLoaded', () => {
        loadProducts(1, 20);
        feather.replace(); 
    });
</script>