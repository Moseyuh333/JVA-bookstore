<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><decorator:title default="JVA Bookstore"/></title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css" />
    <decorator:head/>
    <style>
        /* Minimal fallback if asset not served */
        body { background-color: #f6f8fb; }
    </style>
    
</head>
<body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
        <div class="container">
            <a class="navbar-brand fw-semibold" href="${pageContext.request.contextPath}/">NK Bookstore</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#nav" aria-controls="nav" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="nav">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/">Home</a></li>
                    <li class="nav-item dropdown">
                        <a class="nav-link dropdown-toggle" href="#" id="categoriesDropdown" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                            Categories
                        </a>
                        <ul class="dropdown-menu" aria-labelledby="categoriesDropdown">
                            <li><a class="dropdown-item" href="#">Fiction</a></li>
                            <li><a class="dropdown-item" href="#">Non-Fiction</a></li>
                            <li><a class="dropdown-item" href="#">Children's Books</a></li>
                            <li><a class="dropdown-item" href="#">Educational</a></li>
                        </ul>
                    </li>
                    <li class="nav-item"><a class="nav-link" href="#deals">Deals</a></li>
                </ul>
                <form class="d-flex me-3" style="max-width: 300px;">
                    <input class="form-control me-2" type="search" placeholder="Search books..." aria-label="Search">
                    <button class="btn btn-outline-light" type="submit"><i class="fas fa-search"></i></button>
                </form>
                <ul class="navbar-nav">
                    <li class="nav-item"><a class="nav-link" href="login.jsp">Login</a></li>
                    <li class="nav-item"><a class="nav-link" href="register.jsp">Register</a></li>
                    <li class="nav-item">
                        <a class="nav-link" href="#"><i class="fas fa-shopping-cart"></i> Cart <span class="badge bg-secondary">0</span></a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>
    <main class="container my-5">
        <decorator:body/>
    </main>
    <footer class="border-top py-3 text-center text-muted small">
        <div class="container">© <span id="year"></span> JVA Bookstore</div>
    </footer>
    <script>
        document.getElementById('year').textContent = new Date().getFullYear();
    </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>