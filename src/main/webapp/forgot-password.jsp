<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<page:title>Forgot Password</page:title>

<div class="container" style="max-width: 520px;">
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
