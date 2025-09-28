<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<style>
    .hero-section {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        min-height: 100vh;
        display: flex;
        align-items: center;
        color: white;
        position: relative;
        overflow: hidden;
    }
    .hero-section::before {
        content: '';
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><pattern id="grain" width="100" height="100" patternUnits="userSpaceOnUse"><circle cx="50" cy="50" r="1" fill="%23ffffff" opacity="0.1"/></pattern></defs><rect width="100" height="100" fill="url(%23grain)"/></svg>') repeat;
        opacity: 0.3;
    }
    .hero-content {
        position: relative;
        z-index: 2;
    }
    .feature-card {
        background: white;
        border-radius: 15px;
        padding: 2rem;
        box-shadow: 0 15px 35px rgba(0,0,0,0.1);
        transition: transform 0.3s ease, box-shadow 0.3s ease;
        border: none;
        height: 100%;
    }
    .feature-card:hover {
        transform: translateY(-10px);
        box-shadow: 0 25px 50px rgba(0,0,0,0.15);
    }
    .feature-icon {
        width: 60px;
        height: 60px;
        background: linear-gradient(135deg, #3949ab, #5c6bc0);
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 24px;
        margin: 0 auto 1.5rem;
    }
    .btn-cta {
        padding: 15px 40px;
        font-size: 18px;
        font-weight: 600;
        border-radius: 50px;
        transition: all 0.3s ease;
        text-transform: uppercase;
        letter-spacing: 1px;
    }
    .btn-cta:hover {
        transform: translateY(-2px);
        box-shadow: 0 10px 25px rgba(0,0,0,0.2);
    }
</style>

<div class="hero-section">
    <div class="container hero-content">
        <div class="row align-items-center">
            <div class="col-lg-6">
                <div class="text-center text-lg-start mb-5 mb-lg-0">
                    <div class="mb-4">
                        <div style="width:100px;height:100px;background:rgba(255,255,255,0.2);border-radius:50%;margin:0 auto;margin-left:0;display:flex;align-items:center;justify-content:center;color:white;font-size:48px;font-weight:bold;backdrop-filter:blur(10px);border:2px solid rgba(255,255,255,0.3);">NK</div>
                    </div>
                    <h1 class="display-3 fw-bold mb-3" style="letter-spacing:2px;">NKbookstore</h1>
                    <p class="lead mb-4" style="opacity:0.9;">Your Digital Library for Knowledge & Discovery</p>
                    <p class="mb-4" style="opacity:0.8;">Built with modern Java technologies - Servlet, JSP/JSTL, Bootstrap, PostgreSQL, JWT Authentication</p>
                    <div class="d-flex gap-3 justify-content-center justify-content-lg-start flex-wrap">
                        <a class="btn btn-light btn-cta" href="login.jsp">Get Started</a>
                        <a class="btn btn-outline-light btn-cta" href="register.jsp">Join Now</a>
                    </div>
                </div>
            </div>
            <div class="col-lg-6">
                <div class="text-center">
                    <div style="width:400px;height:300px;background:rgba(255,255,255,0.1);border-radius:20px;margin:0 auto;display:flex;align-items:center;justify-content:center;backdrop-filter:blur(10px);border:1px solid rgba(255,255,255,0.2);">
                        <div class="text-center">
                            <div style="font-size:80px;margin-bottom:20px;">📚</div>
                            <p class="h5" style="opacity:0.9;">Discover. Learn. Grow.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<section class="py-5" style="background:#f8f9fa;">
    <div class="container">
        <div class="text-center mb-5">
            <h2 class="fw-bold" style="color:#1a237e;">Why Choose NKbookstore?</h2>
            <p class="lead text-muted">Modern features for the best reading experience</p>
        </div>
        <div class="row g-4">
            <div class="col-md-4">
                <div class="feature-card text-center">
                    <div class="feature-icon">
                        <i class="fas fa-shield-alt"></i>🔒
                    </div>
                    <h5 class="fw-bold mb-3" style="color:#3949ab;">Secure Authentication</h5>
                    <p class="text-muted">JWT-based authentication with BCrypt password hashing for maximum security.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="feature-card text-center">
                    <div class="feature-icon">
                        <i class="fas fa-database"></i>💾
                    </div>
                    <h5 class="fw-bold mb-3" style="color:#3949ab;">Reliable Database</h5>
                    <p class="text-muted">PostgreSQL database with optimized queries and Heroku deployment support.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="feature-card text-center">
                    <div class="feature-icon">
                        <i class="fas fa-mobile-alt"></i>📱
                    </div>
                    <h5 class="fw-bold mb-3" style="color:#3949ab;">Responsive Design</h5>
                    <p class="text-muted">Bootstrap-powered responsive design that works perfectly on all devices.</p>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="py-5" style="background:linear-gradient(135deg, #3949ab 0%, #5c6bc0 100%);color:white;">
    <div class="container text-center">
        <h2 class="fw-bold mb-4">Ready to Start Your Journey?</h2>
        <p class="lead mb-4">Join thousands of readers who trust NKbookstore for their digital library needs.</p>
        <div class="d-flex gap-3 justify-content-center flex-wrap">
            <a class="btn btn-light btn-cta" href="register.jsp">Create Account</a>
            <a class="btn btn-outline-light btn-cta" href="${pageContext.request.contextPath}/health">System Status</a>
        </div>
        <div class="mt-4">
            <small style="opacity:0.8;">Developed by bibo090809@gmail.com</small>
        </div>
    </div>
</section>