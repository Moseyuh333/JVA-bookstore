<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Forgot Password - NKbookstore</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
  <style>
    body { 
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        min-height: 100vh;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .auth-card {
        background: white;
        border-radius: 15px;
        box-shadow: 0 15px 35px rgba(0,0,0,0.1);
        padding: 2rem;
    }
  </style>
</head>
<body>
  <div class="auth-card" style="max-width: 480px; width: 90%;">
    <div class="text-center my-4">
      <img src="assets/img/nkbookstore-logo.png" alt="NKbookstore Logo" style="width:64px;height:64px;border-radius:50%;">
      <h2 class="mb-1 mt-3" style="color:#1a237e;font-weight:700;letter-spacing:1px;">NKbookstore</h2>
      <div class="text-muted small">by bibo090809@gmail.com</div>
    </div>
    <h4 class="mb-3" style="color:#3949ab;">Forgot Password</h4>
    <form id="forgotForm">
      <div class="mb-3">
        <label class="form-label">Email</label>
        <input class="form-control" name="email" type="email" required />
      </div>
  <button class="btn btn-primary w-100" style="background:#3949ab;border:none;" type="submit">Send Reset Link</button>
    </form>
    <div id="forgotResult" class="mt-3"></div>
    <div class="mt-3 text-center">
      <a href="index.jsp" style="color:#3949ab;">🏠 Home</a> | <a href="login.jsp" style="color:#3949ab;">Back to Login</a>
    </div>
  </div>
  <script>
    document.getElementById('forgotForm').addEventListener('submit', async function(e) {
      e.preventDefault();
      const data = new URLSearchParams(new FormData(this));
      const res = await fetch('api/auth/reset-password', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: data});
      document.getElementById('forgotResult').textContent = await res.text();
    });
  </script>
</body>
</html>