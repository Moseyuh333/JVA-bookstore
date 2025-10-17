<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Ký - NKbookstore</title>
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
            max-width: 520px;
            width: 90%;
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
        .hidden {
            display: none;
        }
        .otp-inputs {
            display: flex;
            gap: 10px;
            justify-content: center;
        }
        .otp-inputs input {
            width: 50px;
            height: 50px;
            text-align: center;
            font-size: 24px;
            font-weight: bold;
        }
        #resendTimer {
            font-weight: bold;
            color: #3949ab;
        }
    </style>
</head>
<body>
<div class="auth-card">
	<div class="text-center my-4">
		<img src="assets/img/nkbookstore-logo.png" alt="NKbookstore Logo" style="width:64px;height:64px;border-radius:50%;">
		<h2 class="mb-1 mt-3" style="color:#1a237e;font-weight:700;letter-spacing:1px;">NKbookstore</h2>
		<div class="text-muted small">Đăng ký tài khoản mới</div>
	</div>
	
	<div id="step1">
	    <h4 class="mb-3">Bước 1: Nhập Email</h4>
	    <form id="emailForm">
	        <div class="mb-3">
	            <label class="form-label">Email</label>
	            <input class="form-control" type="email" id="emailInput" required />
	            <small class="text-muted">Mã OTP sẽ được gửi đến email này</small>
	        </div>
	        <button class="btn btn-primary w-100" type="submit">Gửi mã OTP</button>
	    </form>
	</div>
	
	<div id="step2" class="hidden">
	    <h4 class="mb-3">Bước 2: Xác nhận OTP</h4>
	    <p class="text-muted">Mã OTP đã được gửi đến <strong id="displayEmail"></strong></p>
	    
	    <form id="otpForm">
	        <div class="mb-3">
	            <label class="form-label">Nhập mã OTP (6 số)</label>
	            <div class="otp-inputs">
	                <input type="text" maxlength="1" class="form-control otp-digit" id="otp1" />
	                <input type="text" maxlength="1" class="form-control otp-digit" id="otp2" />
	                <input type="text" maxlength="1" class="form-control otp-digit" id="otp3" />
	                <input type="text" maxlength="1" class="form-control otp-digit" id="otp4" />
	                <input type="text" maxlength="1" class="form-control otp-digit" id="otp5" />
	                <input type="text" maxlength="1" class="form-control otp-digit" id="otp6" />
	            </div>
	        </div>
	        
	        <div class="mb-3">
	            <label class="form-label">Username</label>
	            <input class="form-control" type="text" id="usernameInput" required />
	        </div>
	        
	        <div class="mb-3">
	            <label class="form-label">Password</label>
	            <input class="form-control" type="password" id="passwordInput" required minlength="6" />
	        </div>
	        
	        <button class="btn btn-primary w-100" type="submit">Xác nhận và Đăng ký</button>
	        
	        <div class="text-center mt-3">
	            <button type="button" class="btn btn-link" id="resendBtn" disabled>
	                Gửi lại mã (<span id="resendTimer">120</span>s)
	            </button>
	        </div>
	    </form>
	</div>
	
	<div id="result" class="mt-3"></div>
	
	<div class="text-center mt-3">
	    <a href="login.jsp">Đã có tài khoản? Đăng nhập</a>
	</div>
</div>

<script>
let currentEmail = '';
let resendCountdown = 120;
let countdownInterval = null;

document.getElementById('emailForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('emailInput').value;
    const btn = e.target.querySelector('button');
    btn.disabled = true;
    btn.textContent = 'Đang gửi...';
    
    try {
        const res = await fetch('/api/auth/send-otp', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams({ email: email })
        });
        
        let data = {};
        try {
            data = await res.json();
        } catch (parseErr) {
            console.error('JSON parse error:', parseErr);
            data = { error: 'Server error: invalid response' };
        }
        
        if (res.ok) {
            currentEmail = email;
            document.getElementById('displayEmail').textContent = email;
            document.getElementById('step1').classList.add('hidden');
            document.getElementById('step2').classList.remove('hidden');
            document.getElementById('otp1').focus();
            startResendCountdown();
            showMessage('success', 'Mã OTP đã được gửi đến email của bạn!');
        } else {
            if (data.remaining) {
                showMessage('danger', 'Vui lòng đợi ' + data.remaining + ' giây trước khi gửi lại OTP');
            } else {
                showMessage('danger', data.error || 'Không thể gửi OTP');
            }
        }
    } catch (err) {
        showMessage('danger', 'Lỗi kết nối: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Gửi mã OTP';
    }
});

document.querySelectorAll('.otp-digit').forEach((input, index, inputs) => {
    input.addEventListener('input', (e) => {
        if (e.target.value.length === 1 && index < inputs.length - 1) {
            inputs[index + 1].focus();
        }
    });
    
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && e.target.value === '' && index > 0) {
            inputs[index - 1].focus();
        }
    });
});

document.getElementById('otpForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const otp = Array.from(document.querySelectorAll('.otp-digit'))
        .map(input => input.value)
        .join('');
    
    if (otp.length !== 6) {
        showMessage('warning', 'Vui lòng nhập đầy đủ 6 số OTP');
        return;
    }
    
    const username = document.getElementById('usernameInput').value;
    const password = document.getElementById('passwordInput').value;
    const btn = e.target.querySelector('button[type="submit"]');
    
    btn.disabled = true;
    btn.textContent = 'Đang xác nhận...';
    
    try {
        const res = await fetch('/api/auth/verify-otp', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams({
                email: currentEmail,
                otp: otp,
                username: username,
                password: password
            })
        });
        
        const data = await res.json();
        
        if (res.ok) {
            showMessage('success', data.message);
            setTimeout(() => window.location.href = 'login.jsp', 2000);
        } else {
            showMessage('danger', data.error || 'Xác nhận thất bại');
            document.querySelectorAll('.otp-digit').forEach(input => input.value = '');
            document.getElementById('otp1').focus();
        }
    } catch (err) {
        showMessage('danger', 'Lỗi kết nối: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Xác nhận và Đăng ký';
    }
});

document.getElementById('resendBtn').addEventListener('click', async () => {
    const btn = document.getElementById('resendBtn');
    btn.disabled = true;
    
    try {
        const res = await fetch('/api/auth/send-otp', {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded'},
            body: new URLSearchParams({ email: currentEmail })
        });
        
        let data = {};
        try {
            data = await res.json();
        } catch (parseErr) {
            console.error('JSON parse error:', parseErr);
            data = { error: 'Server error: invalid response' };
        }
        
        if (res.ok) {
            showMessage('success', 'Mã OTP mới đã được gửi!');
            resendCountdown = 120;
            startResendCountdown();
        } else {
            showMessage('danger', data.error || 'Không thể gửi lại OTP');
        }
    } catch (err) {
        showMessage('danger', 'Lỗi kết nối: ' + err.message);
    }
});

function startResendCountdown() {
    clearInterval(countdownInterval);
    const btn = document.getElementById('resendBtn');
    const timer = document.getElementById('resendTimer');
    
    btn.disabled = true;
    resendCountdown = 120;
    
    countdownInterval = setInterval(() => {
        resendCountdown--;
        timer.textContent = resendCountdown;
        
        if (resendCountdown <= 0) {
            clearInterval(countdownInterval);
            btn.disabled = false;
            timer.textContent = '0';
        }
    }, 1000);
}

function showMessage(type, message) {
    const resultDiv = document.getElementById('result');
    resultDiv.innerHTML = '<div class="alert alert-' + type + '">' + message + '</div>';
}
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>