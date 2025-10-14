<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
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
    </style>
</head>
<body>
<div class="auth-card" style="max-width: 520px; width: 90%;">
	<div class="text-center my-4">
		<img src="assets/img/nkbookstore-logo.png" alt="NKbookstore Logo" style="width:64px;height:64px;border-radius:50%;">
		<h2 class="mb-1 mt-3" style="color:#1a237e;font-weight:700;letter-spacing:1px;">NKbookstore</h2>
		<div class="text-muted small">by bibo090809@gmail.com</div>
	</div>
	<h2 class="mb-3">Forgot Password</h2>
	<p class="text-muted">Enter your email address. If it exists, we'll send you a password reset link.</p>
	<form id="forgotForm">
		<div class="mb-3">
			<label class="form-label">Email</label>
			<input class="form-control" type="email" name="email" required />
		</div>
		<button class="btn btn-primary" type="submit">Send reset link</button>
		<a class="ms-3" href="login.jsp">Back to login</a>
	</form>
	<div id="forgotResult" class="mt-3"></div>
</div>

<script>
	document.getElementById('forgotForm').addEventListener('submit', async (e) => {
		e.preventDefault();
		const data = new URLSearchParams(new FormData(e.target));
		const btn = e.target.querySelector('button[type="submit"]');
		btn.disabled = true;
		btn.textContent = 'Sending...';
		try {
			const res = await fetch('api/auth/reset-password', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: data});
			const text = await res.text();
			if (res.ok) {
				document.getElementById('forgotResult').innerHTML = '<div class="alert alert-success">If the email exists, a reset link has been sent.</div>';
			} else {
				let msg = text;
				try { const j = JSON.parse(text); msg = j.error || text; } catch(_){}
				document.getElementById('forgotResult').innerHTML = '<div class="alert alert-danger">' + msg + '</div>';
			}
		} catch (err) {
			document.getElementById('forgotResult').innerHTML = '<div class="alert alert-danger">Request failed: ' + err.message + '</div>';
		} finally {
			btn.disabled = false;
			btn.textContent = 'Send reset link';
		}
  });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>