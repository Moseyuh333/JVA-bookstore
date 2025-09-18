<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<header class="hero">
    <div class="container">
        <h1 class="display-5">Welcome to JVA Bookstore</h1>
        <p class="lead">Servlet + JSP/JSTL + Bootstrap + JDBC + PostgreSQL + Sitemesh + JWT</p>
        <a class="btn btn-light btn-lg" href="${pageContext.request.contextPath}/health">Check Health</a>
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