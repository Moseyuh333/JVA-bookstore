<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
    /* Simple seller sidebar adapted from admin styles */
    .seller-sidebar {
        background: white;
        box-shadow: 2px 0 12px rgba(0,0,0,0.06);
        width: 260px;
        position: fixed;
        left: 0;
        top: 0;
        height: 100vh;
        z-index: 999;
        overflow-y: auto;
        border-right: 1px solid #e5e7eb;
    }
    .seller-sidebar .brand {
        padding: 20px;
        background: linear-gradient(135deg,#92400e,#b45309);
        color: white;
        display: flex;
        gap: 12px;
        align-items: center;
    }
    .seller-sidebar .nav { padding: 12px; }
    .seller-sidebar .nav a { display:block; padding:10px 14px; color:#4b5563; text-decoration:none; border-radius:8px; margin:6px 4px; }
    .seller-sidebar .nav a:hover { background:#f9fafb; color:#92400e; }
    #content-wrapper { margin-left: 0; transition: margin-left .3s; }
    @media(min-width: 900px) { .seller-sidebar ~ #content-wrapper { margin-left: 260px; } }
</style>

<div class="seller-sidebar">
    <div class="brand">
        <div style="width:36px;height:36px;border-radius:6px;background:rgba(255,255,255,0.15);display:flex;align-items:center;justify-content:center">
            <i class="fas fa-book-open" style="color:white"></i>
        </div>
        <div>
            <div style="font-weight:700">Bookish Seller</div>
            <div style="font-size:12px;opacity:0.9">Bảng điều khiển người bán</div>
        </div>
    </div>
    <nav class="nav">
        <a href="${pageContext.request.contextPath}/seller/dashboard"><i class="fas fa-home"></i> Tổng quan</a>
        <a href="${pageContext.request.contextPath}/seller/products"><i class="fas fa-box"></i> Sản phẩm</a>
        <a href="${pageContext.request.contextPath}/seller/orders"><i class="fas fa-shopping-cart"></i> Đơn hàng</a>
        <a href="${pageContext.request.contextPath}/seller/analytics"><i class="fas fa-chart-line"></i> Thống kê</a>
        <a href="${pageContext.request.contextPath}/seller/categories"><i class="fas fa-tags"></i> Danh mục</a>
        <a href="${pageContext.request.contextPath}/seller/settings"><i class="fas fa-cog"></i> Cài đặt</a>
    </nav>
</div>

<!-- Small script to mark active link -->
<script>
    document.addEventListener('DOMContentLoaded', function(){
        var path = window.location.pathname || '';
        document.querySelectorAll('.seller-sidebar .nav a').forEach(function(a){
            try{ if (path.indexOf(a.getAttribute('href')) !== -1) a.style.fontWeight='700'; }catch(e){}
        });
    });
</script>
