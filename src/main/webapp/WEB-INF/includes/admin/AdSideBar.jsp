<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<style>
    /* Modern Clean Sidebar */
    .sidebar {
        background: white;
        box-shadow: 2px 0 12px rgba(0, 0, 0, 0.08);
        width: 260px;
        position: fixed;
        left: -260px;
        top: 0;
        height: 100vh;
        z-index: 999;
        transition: left 0.3s ease;
        overflow-y: auto;
        border-right: 1px solid #e5e7eb;
        border-top: none;
    }

    /* Show sidebar */
    .sidebar.show {
        left: 0;
    }

    /* Toggle button */
    .sidebar-toggle-btn {
        position: fixed;
        left: 0;
        top: 120px;
        width: 32px;
        height: 48px;
        background: white;
        border: 1px solid #e5e7eb;
        border-left: none;
        border-radius: 0 8px 8px 0;
        cursor: pointer;
        z-index: 998;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #6b7280;
        font-size: 14px;
        box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
        transition: all 0.2s ease;
    }

    .sidebar-toggle-btn:hover {
        background: #f9fafb;
        color: #92400e;
        width: 36px;
    }

    .sidebar-toggle-btn i {
        transition: transform 0.3s ease;
    }

    .sidebar.show ~ .sidebar-toggle-btn {
        left: 260px;
    }

    .sidebar.show ~ .sidebar-toggle-btn i {
        transform: rotate(180deg);
    }

    /* Sidebar Brand */
    .sidebar-brand {
        padding: 24px 20px;
        background: linear-gradient(135deg, #92400e, #b45309);
        border-bottom: none;
        display: flex;
        align-items: center;
        gap: 12px;
        color: white;
    }


    .sidebar-brand-icon {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        background: rgba(255, 255, 255, 0.15);
        color: white;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 20px;
    }

    .sidebar-brand-text {
        color: white;
        font-weight: 700;
        font-size: 16px;
    }

    /* Navigation */
    .sidebar-nav {
        padding: 16px 0;
    }

    .nav-section {
        margin-bottom: 24px;
    }

    .nav-section-title {
        color: #9ca3af;
        font-size: 11px;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 1px;
        padding: 8px 20px;
        margin-bottom: 4px;
    }

    .nav-item {
        margin: 4px 12px;
    }

    .nav-link {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 16px;
        color: #4b5563;
        text-decoration: none;
        border-radius: 8px;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.2s ease;
        position: relative;
    }

    .nav-link:hover {
        background: #f9fafb;
        color: #92400e;
        text-decoration: none;
    }

    .nav-link i {
        font-size: 18px;
        width: 20px;
        text-align: center;
        transition: transform 0.2s ease;
    }

    .nav-link:hover i {
        transform: scale(1.1);
    }

    /* Active state */
    .nav-item.active .nav-link {
        background: #fef3c7;
        color: #92400e;
        font-weight: 600;
    }

    .nav-item.active .nav-link::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 24px;
        background: #92400e;
        border-radius: 0 3px 3px 0;
    }

    .nav-item.active .nav-link i {
        color: #92400e;
    }

    /* Divider */
    .sidebar-divider {
        height: 1px;
        background: #e5e7eb;
        margin: 16px 20px;
    }

    /* Overlay */
    .sidebar-overlay {
        display: none;
        position: fixed;
        top: 70px;
        left: 0;
        width: 100%;
        height: calc(100vh - 70px);
        background: rgba(0, 0, 0, 0.3);
        z-index: 998;
    }

    .sidebar.show ~ .sidebar-overlay {
        display: block;
    }

    /* Scrollbar */
    .sidebar::-webkit-scrollbar {
        width: 6px;
    }

    .sidebar::-webkit-scrollbar-track {
        background: transparent;
    }

    .sidebar::-webkit-scrollbar-thumb {
        background: #d1d5db;
        border-radius: 3px;
    }

    .sidebar::-webkit-scrollbar-thumb:hover {
        background: #9ca3af;
    }

    /* Adjust content when sidebar is open */
    #content-wrapper {
        margin-left: 0;
        transition: margin-left 0.3s ease;
    }

    .sidebar.show ~ #content-wrapper {
        margin-left: 260px;
    }

    /* Responsive */
    @media (max-width: 768px) {
        .sidebar {
            width: 280px;
            left: -280px;
        }

        .sidebar.show {
            left: 0;
        }

        .sidebar.show ~ .sidebar-toggle-btn {
            left: 0;
            opacity: 0;
        }

        .sidebar.show ~ #content-wrapper {
            margin-left: 0;
        }
    }

    /* Badge */
    .nav-badge {
        margin-left: auto;
        background: #fee2e2;
        color: #991b1b;
        font-size: 11px;
        font-weight: 600;
        padding: 2px 8px;
        border-radius: 12px;
    }
</style>

<!-- Toggle Button -->
<button class="sidebar-toggle-btn" id="sidebarToggle" title="Mở/Đóng Menu">
    <i class="fas fa-chevron-right"></i>
</button>

<!-- Sidebar -->
<div class="sidebar" id="accordionSidebar">
    <!-- Brand -->
    <div class="sidebar-brand">
        <div class="sidebar-brand-icon">
            <i class="fas fa-book-open"></i>
        </div>
        <div class="sidebar-brand-text">Bookish Admin</div>
    </div>

    <!-- Navigation -->
    <div class="sidebar-nav">

        <!-- ======= Tổng quan ======= -->
        <div class="nav-section">
            <div class="nav-section-title">Tổng quan</div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-dashboard">
                    <i class="fas fa-chart-line"></i>
                    <span>Dashboard</span>
                </a>
            </div>
        </div>

        <div class="sidebar-divider"></div>

        <!-- ======= Quản lý hệ thống ======= -->
        <div class="nav-section">
            <div class="nav-section-title">Quản lý hệ thống</div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-account">
                    <i class="fas fa-users-cog"></i>
                    <span>Tài khoản người dùng</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-product">
                    <i class="fas fa-book"></i>
                    <span>Sản phẩm</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-categories">
                    <i class="fas fa-tags"></i>
                    <span>Danh mục sản phẩm</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-commission">
                    <i class="fas fa-hand-holding-usd"></i>
                    <span>Chiết khấu cửa hàng</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-promotion">
                    <i class="fas fa-percentage"></i>
                    <span>Chương trình khuyến mãi</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-shippers">
                    <i class="fas fa-truck"></i>
                    <span>Nhà vận chuyển</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin-orders">
                    <i class="fas fa-shopping-cart"></i>
                    <span>Đơn hàng</span>
                </a>
            </div>

            <div class="nav-item">
                <a class="nav-link" href="<%=request.getContextPath()%>/admin/AdSupport.jsp">
                    <i class="fas fa-comments"></i>
                    <span>Hỗ trợ khách hàng</span>
                </a>
            </div>
        </div>

        <div class="sidebar-divider"></div>

        <!-- ======= Cài đặt ======= -->
        <div class="nav-section">
            <div class="nav-item">
                <a class="nav-link" href="javascript:void(0);" id="logoutBtn">
                    <i class="fas fa-sign-out-alt"></i>
                    <span>Đăng xuất</span>
                </a>
            </div>
        </div>
    </div>
</div>

<!-- Overlay -->
<div class="sidebar-overlay" id="sidebarOverlay"></div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('accordionSidebar');
    const sidebarToggle = document.getElementById('sidebarToggle');
    const overlay = document.getElementById('sidebarOverlay');
    
    // Toggle sidebar
    sidebarToggle?.addEventListener('click', e => {
        e.preventDefault();
        sidebar.classList.toggle('show');
    });

    // Overlay click -> close sidebar
    overlay?.addEventListener('click', () => sidebar.classList.remove('show'));

    // ESC key -> close
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape' && sidebar.classList.contains('show')) {
            sidebar.classList.remove('show');
        }
    });

    // Active highlight
    const currentPath = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.includes(href)) {
            link.closest('.nav-item').classList.add('active');
        }
    });

    // =====================
    // 🔸 Client-side Logout
    // =====================
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            console.log('Đăng xuất...');
            localStorage.removeItem('admin_token');
            localStorage.removeItem('admin_username');
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_role');
            localStorage.removeItem('auth_username');
            sessionStorage.clear();

            const contextPath = '<%= request.getContextPath() %>';
            window.location.href = contextPath + '/login.jsp';
        });
    }
});
</script>