<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Đăng ký - Bookish Bliss Haven" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="min-h-screen flex items-center justify-center py-12 px-4 bg-amber-50">
  <div class="w-full max-w-2xl">
    <div class="bg-white rounded-3xl p-8 shadow-lg border border-amber-100/80">
      <div class="text-center mb-6">
        <h1 class="title-font text-2xl font-bold text-amber-800">Tạo tài khoản mới</h1>
        <p class="text-sm text-gray-500">Đăng ký nhanh bằng email và xác thực OTP</p>
      </div>

      <div id="step1">
        <form id="emailForm" class="space-y-4">
          <div>
            <label class="text-sm font-medium text-gray-700">Email</label>
            <input id="emailInput" type="email" required class="w-full rounded-md border border-amber-200 px-4 py-3" />
          </div>
          <button type="submit" class="w-full rounded-2xl bg-amber-700 text-white py-3 font-semibold">Gửi mã OTP</button>
        </form>
      </div>

      <div id="step2" class="hidden">
        <div class="mb-4 text-sm text-gray-600">Mã đã gửi tới <strong id="displayEmail"></strong></div>
        <form id="otpForm" class="space-y-3">
          <div class="flex gap-2">
            <input maxlength="1" class="otp-digit w-12 h-12 text-center rounded-md border" />
            <input maxlength="1" class="otp-digit w-12 h-12 text-center rounded-md border" />
            <input maxlength="1" class="otp-digit w-12 h-12 text-center rounded-md border" />
            <input maxlength="1" class="otp-digit w-12 h-12 text-center rounded-md border" />
            <input maxlength="1" class="otp-digit w-12 h-12 text-center rounded-md border" />
            <input maxlength="1" class="otp-digit w-12 h-12 text-center rounded-md border" />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Tên đăng nhập</label>
            <input id="usernameInput" required class="w-full rounded-md border px-4 py-3" />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Mật khẩu</label>
            <input id="passwordInput" type="password" required minlength="6" class="w-full rounded-md border px-4 py-3" />
          </div>
          <button type="submit" class="w-full rounded-2xl bg-amber-700 text-white py-3 font-semibold">Xác nhận và Đăng ký</button>
        </form>
      </div>

    </div>
  </div>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
<script>
const baseUrl = '<%= request.getContextPath() %>';
let currentEmail = '';

(function () {
  const emailForm = document.getElementById('emailForm');
  const otpForm = document.getElementById('otpForm');

  emailForm.addEventListener('submit', async function (e) {
    e.preventDefault();
    const email = document.getElementById('emailInput').value;
    try {
      const res = await fetch(baseUrl + '/api/auth/send-otp', {
        method: 'POST', headers: {'Content-Type':'application/x-www-form-urlencoded'},
        body: new URLSearchParams({ email: email })
      });
      const data = await res.json();
      if (res.ok) {
        currentEmail = email;
        document.getElementById('displayEmail').textContent = email;
        document.getElementById('step1').classList.add('hidden');
        document.getElementById('step2').classList.remove('hidden');
        document.querySelector('.otp-digit').focus();
      } else {
        alert(data.error || 'Không thể gửi OTP');
      }
    } catch (err) {
      alert('Lỗi kết nối: ' + err.message);
    }
  });

  otpForm.addEventListener('submit', async function (e) {
    e.preventDefault();
    const otpDigits = Array.from(document.querySelectorAll('.otp-digit'));
    const otp = otpDigits.map(i => i.value.trim()).join('');
    if (otp.length !== otpDigits.length) {
      alert('Vui lòng nhập đầy đủ mã OTP.');
      return;
    }
    const username = document.getElementById('usernameInput').value;
    const password = document.getElementById('passwordInput').value;
    try {
      const params = new URLSearchParams({
        email: currentEmail,
        otp: otp,
        username: username,
        password: password
      });
      const res = await fetch(baseUrl + '/api/auth/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
      });
      const data = await res.json();
      if (res.ok) {
        alert('Đăng ký thành công');
        window.location.href = baseUrl + '/login.jsp';
      } else {
        alert(data.error || 'Đăng ký thất bại');
      }
    } catch (err) {
      alert('Lỗi kết nối: ' + err.message);
    }
  });
})();
</script>
</body>
</html>
