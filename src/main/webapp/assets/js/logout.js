function logout() {
    // Xóa tất cả tokens
    localStorage.removeItem('seller_token');
    localStorage.removeItem('seller_username');
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_username');
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_username');

    console.log('Logging out, redirecting to login page');

    // Clear session storage
    sessionStorage.clear();

    // Redirect về login
    const contextPath = '<%= request.getContextPath() %>';
    window.location.href = contextPath + '/login.jsp';
}
