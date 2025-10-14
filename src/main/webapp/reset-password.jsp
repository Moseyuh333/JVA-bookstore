<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>Reset Password - NKbookstore</title>
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
        animation: fadeIn 0.5s ease-in;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-20px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .form-control:focus {
      border-color: #3949ab;
      box-shadow: 0 0 0 0.2rem rgba(57, 73, 171, 0.25);
    }
    .btn-primary {
      background: #3949ab;
      border: none;
      transition: background 0.3s ease;
    }
    .btn-primary:hover {
      background: #1a237e;
    }
    .alert {
      animation: slideIn 0.3s ease;
    }
    @keyframes slideIn {
      from { transform: translateX(-100%); }
      to { transform: translateX(0); }
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
    <h4 class="mb-3" style="color:#3949ab;">Reset Password</h4>
    <% if ("invalid".equals(request.getParameter("error"))) { %>
      <div class="alert alert-danger">Invalid or expired reset token. Please request a new one.</div>
    <% } else if ("success".equals(request.getParameter("success"))) { %>
      <div class="alert alert-success">Password reset successful! You can now login with your new password.</div>
      <div class="mt-3 text-center">
        <a href="login.jsp" class="btn btn-primary">Go to Login</a>
      </div>
      <% return; %>
    <% } %>
    <form id="resetForm">
      <input type="hidden" name="token" value="<%= request.getParameter("token") != null ? request.getParameter("token") : "" %>" />
      <div class="mb-3">
        <label class="form-label">New Password</label>
        <input class="form-control" name="password" type="password" required minlength="6" />
      </div>
      <div class="mb-3">
        <label class="form-label">Confirm New Password</label>
        <input class="form-control" name="confirmPassword" type="password" required minlength="6" />
      </div>
      <button class="btn btn-primary w-100" type="submit">Reset Password</button>
    </form>
    <div id="resetResult" class="mt-3"></div>
    <div class="mt-3 text-center">
      <a href="login.jsp" style="color:#3949ab;">Back to Login</a>
    </div>
  </div>
  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
  <script>
    document.getElementById('resetForm').addEventListener('submit', async function(e) {
      e.preventDefault();
      const password = document.querySelector('input[name="password"]').value;
      const confirmPassword = document.querySelector('input[name="confirmPassword"]').value;
      if (password !== confirmPassword) {
        document.getElementById('resetResult').innerHTML = '<div class="alert alert-danger">Passwords do not match!</div>';
        return;
      }
      if (password.length < 6) {
        document.getElementById('resetResult').innerHTML = '<div class="alert alert-danger">Password must be at least 6 characters!</div>';
        return;
      }
      const data = new URLSearchParams(new FormData(this));
      const button = this.querySelector('button[type="submit"]');
      button.disabled = true;
      button.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Resetting...';
      const res = await fetch('api/auth/reset', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: data});
      const result = await res.text();
      if (res.ok) {
        window.location.href = 'reset-password.jsp?success=true';
      } else {
        document.getElementById('resetResult').innerHTML = '<div class="alert alert-danger">' + result + '</div>';
      }
      button.disabled = false;
      button.innerHTML = 'Reset Password';
    });
  </script>
</body>
</html>
