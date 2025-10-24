<%-- File: /logout-clear.jsp --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Logging Out...</title>
</head>
<body>
    <p>Session expired. Clearing old token...</p>
    <script>
        // Xóa token khỏi localStorage (token đã gây ra lỗi xác thực)
        localStorage.removeItem('seller_token');
        localStorage.removeItem('seller_username');
        // Xóa các token liên quan khác nếu cần
        localStorage.removeItem('auth_token');
        localStorage.removeItem('auth_username');

        // Sau đó chuyển hướng an toàn về trang login
        window.location.href = '${pageContext.request.contextPath}/login.jsp?expired=true';
    </script>
</body>
</html>