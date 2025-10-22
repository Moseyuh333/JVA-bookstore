<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Seller Profile Test</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body class="bg-light">

<div class="container mt-5">
    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white">
            <h4 class="mb-0">Seller Profile Test Page</h4>
        </div>
        <div class="card-body">
            <p>Xin chào, đây là trang <strong>Profile</strong> test dành cho Seller.</p>
            <p>Nếu mày thấy trang này hiển thị được sau khi login bằng tài khoản seller → phần route đã hoạt động!</p>

            <div class="mt-3">
                <a href="${pageContext.request.contextPath}/seller/dashboard" class="btn btn-success">Về Dashboard</a>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger">Đăng xuất</a>
            </div>
        </div>
    </div>
</div>

</body>
</html>
