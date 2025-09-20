<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<header class="hero">
    <div class="container text-center">
        <div class="my-4">
            <img src="assets/img/nkbookstore-logo.png" alt="NKbookstore Logo" style="height:80px;">
            <h1 class="display-4 mb-1" style="color:#1a237e;font-weight:700;letter-spacing:1px;">NKbookstore</h1>
            <div class="text-muted mb-3">by bibo090809@gmail.com</div>
        </div>
        <p class="lead">Servlet + JSP/JSTL + Bootstrap + JDBC + PostgreSQL + Sitemesh + JWT</p>
        <div class="d-flex gap-3 justify-content-center flex-wrap mb-3">
            <a class="btn btn-primary btn-lg" style="background:#3949ab;border:none;" href="login.jsp">Login</a>
            <a class="btn btn-outline-primary btn-lg" style="color:#3949ab;border-color:#3949ab;" href="register.jsp">Register</a>
            <a class="btn btn-light btn-lg" href="${pageContext.request.contextPath}/health">Check Health</a>
        </div>
    </div>
</header>

<section class="features container my-5">
    <div class="row g-4">
        <div class="col-md-4">
            <div class="card p-3 h-100">
                <h5>Clean Layout</h5>
                <p>Decorator-based layout with Sitemesh for consistent headers and footers.</p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card p-3 h-100">
                <h5>PostgreSQL Ready</h5>
                <p>JDBC utility and Heroku DATABASE_URL support for easy deployment.</p>
            </div>
        </div>
        <div class="col-md-4">
            <div class="card p-3 h-100">
                <h5>JWT Auth</h5>
                <p>Utility helpers to issue/validate JSON Web Tokens for API endpoints.</p>
            </div>
        </div>
    </div>
</section>