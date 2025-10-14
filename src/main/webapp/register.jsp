<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - NKbookstore</title>
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
	<h2 class="mb-3">Create account</h2>
	<form id="registerForm">
		<div class="mb-3">
			<label class="form-label">Email</label>
			<input class="form-control" type="email" name="email" required />
		</div>
		<div class="mb-3">
			<label class="form-label">Username</label>
			<input class="form-control" name="username" required />
		</div>
		<div class="mb-3">
			<label class="form-label">Password</label>
			<input class="form-control" type="password" name="password" required minlength="6" />
		</div>
		<button class="btn btn-primary" type="submit">Sign up</button>
		<a class="ms-3" href="login.jsp">Already have an account?</a>
	</form>
	<div id="regResult" class="mt-3"></div>
</div>

<script>
	document.getElementById('registerForm').addEventListener('submit', async (e) => {
		e.preventDefault();
		const data = new URLSearchParams(new FormData(e.target));
		const btn = e.target.querySelector('button[type="submit"]');
		btn.disabled = true;
		btn.textContent = 'Submitting...';
		try {
			const res = await fetch('api/auth/register', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: data});
			const text = await res.text();
			if (res.ok) {
				document.getElementById('regResult').innerHTML = '<div class="alert alert-success"><strong>🎉 Đăng ký thành công!</strong><br>Chúng tôi đã gửi email xác nhận đến địa chỉ của bạn. Vui lòng kiểm tra hộp thư và click vào liên kết xác nhận để kích hoạt tài khoản trước khi đăng nhập.</div>';
			} else {
				let msg = text;
				try { const j = JSON.parse(text); msg = j.error || text; } catch(_){}
				document.getElementById('regResult').innerHTML = '<div class="alert alert-danger"><strong>❌ Lỗi đăng ký:</strong> ' + msg + '</div>';
			}
		} catch (err) {
			document.getElementById('regResult').innerHTML = '<div class="alert alert-danger"><strong>❌ Kết nối thất bại:</strong> ' + err.message + '</div>';
		} finally {
			btn.disabled = false;
			btn.textContent = 'Sign up';
		}
  });
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>