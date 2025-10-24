<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Đăng nhập - Bookish Bliss Haven" />
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="min-h-screen bg-gradient-to-br from-amber-900/20 via-amber-800/30 to-amber-950/40 flex items-center justify-center py-16 px-4">
  <section class="w-full max-w-lg">
    <div class="bg-white/95 backdrop-blur-sm rounded-3xl shadow-2xl border border-amber-100/80 p-10 space-y-8">
      <div class="text-center space-y-2">
        <span class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-amber-100 text-amber-700 shadow-inner">
          <i data-feather="log-in" class="w-7 h-7"></i>
        </span>
        <h1 class="title-font text-3xl font-bold text-amber-800">Đăng nhập</h1>
        <p class="text-gray-500 text-sm">Chào mừng bạn trở lại với Bookish Bliss Haven</p>
      </div>

      <form id="loginForm" class="space-y-5">
        <div class="space-y-2">
          <label for="username" class="text-sm font-semibold text-gray-700">Tên đăng nhập</label>
          <input id="username" name="username" type="text" required autocomplete="username"
                 class="w-full rounded-2xl border border-amber-200/70 bg-white px-4 py-3 text-gray-800 shadow-inner focus:border-amber-500 focus:ring-2 focus:ring-amber-400/60" />
        </div>
        <div class="space-y-2">
          <div class="flex items-center justify-between">
            <label for="password" class="text-sm font-semibold text-gray-700">Mật khẩu</label>
            <a href="<%= request.getContextPath() %>/forgot-password.jsp" class="text-sm text-amber-700 hover:text-amber-800 font-medium">Quên mật khẩu?</a>
          </div>
          <input id="password" name="password" type="password" required autocomplete="current-password"
                 class="w-full rounded-2xl border border-amber-200/70 bg-white px-4 py-3 text-gray-800 shadow-inner focus:border-amber-500 focus:ring-2 focus:ring-amber-400/60" />
        </div>
        <button id="loginSubmit" type="submit"
                class="w-full flex items-center justify-center gap-2 rounded-2xl bg-amber-700 text-white font-semibold py-3 shadow-lg shadow-amber-900/20 hover:bg-amber-800 transition disabled:opacity-60 disabled:cursor-not-allowed">
          <i data-feather="arrow-right-circle" class="w-5 h-5"></i>
          <span>Đăng nhập</span>
        </button>
      </form>

      <div id="loginFeedback" class="space-y-2"></div>

      <div class="text-center text-sm text-gray-600">
        <span>Chưa có tài khoản?</span>
        <a href="<%= request.getContextPath() %>/register.jsp" class="font-semibold text-amber-700 hover:text-amber-800">Đăng ký ngay</a>
      </div>
    </div>
  </section>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
<script>
  const contextPath = '<%= request.getContextPath() %>';

  // 🔸 Nếu user đã đăng nhập rồi → tự động chuyển hướng theo role
  (function bootstrapRedirect() {
    const token = localStorage.getItem('auth_token');
    const role  = (localStorage.getItem('auth_role') || '').toLowerCase();
    if (token && role) {
      let target = contextPath + '/';
      if (role === 'shipper')      target = contextPath + '/dashboard-shipper.jsp';
      else if (role === 'admin')   target = contextPath + '/admin/AdDashboard.jsp';
      window.location.replace(target);
    }
  })();

  function decodeJwtPayload(token) {
    try {
      const base64 = token.split('.')[1];
      if (!base64) return null;
      const json = atob(base64.replace(/-/g,'+').replace(/_/g,'/'));
      return JSON.parse(json);
    } catch { return null; }
  }

  (function attachLoginHandler () {
    const form      = document.getElementById('loginForm');
    const feedback  = document.getElementById('loginFeedback');
    const submitBtn = document.getElementById('loginSubmit');

    function showMessage(type, message) {
      if (!feedback) return;
      feedback.innerHTML = '';
      const wrapper = document.createElement('div');
      const base = 'px-4 py-3 rounded-2xl border text-sm font-medium transition';
      let tone = 'bg-red-100 border-red-200 text-red-700';
      if (type === 'success') tone = 'bg-emerald-100 border-emerald-200 text-emerald-800';
      else if (type === 'info') tone = 'bg-amber-50 border-amber-200 text-amber-700';
      wrapper.className = base + ' ' + tone;
      wrapper.innerHTML = message;
      feedback.appendChild(wrapper);
    }

    form.addEventListener('submit', async function (event) {
      event.preventDefault();
      const formData = new FormData(form);
      const payload  = new URLSearchParams(formData);

      submitBtn.disabled = true;
      submitBtn.classList.add('opacity-60','cursor-wait');

      try {
        const res  = await fetch(contextPath + '/api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: payload
        });

        const text = await res.text();
        let data = {};
        if (text) { try { data = JSON.parse(text); } catch {} }

        if (res.ok && data && data.token) {
          const token = data.token;
          localStorage.setItem('auth_token', token);

          // 🔸 Lưu username
          const enteredUsername = (formData.get('username') || '').trim();
          let username = enteredUsername;
          if (!username) {
            const p = decodeJwtPayload(token);
            username = p && (p.username || p.sub || '') || '';
          }
          if (username) localStorage.setItem('auth_username', username);
          else localStorage.removeItem('auth_username');

          // 🔸 Xác định role
          let role = (data.role || '').toLowerCase();
          if (!role) {
            const p = decodeJwtPayload(token) || {};
            role = (p.role || (Array.isArray(p.roles) ? p.roles[0] : '') || '').toLowerCase();
          }
          if (!role) role = 'user';
          localStorage.setItem('auth_role', role);

          // 🔸 Chuyển hướng theo role
          let target = contextPath + '/';
          if (role === 'shipper')      target = contextPath + '/dashboard-shipper.jsp';
          else if (role === 'admin')   target = contextPath + '/admin/AdDashboard.jsp';

          showMessage('success', '✅ Đăng nhập thành công! Đang chuyển hướng...');
          setTimeout(() => { window.location.href = target; }, 800);

        } else {
          localStorage.removeItem('auth_token');
          localStorage.removeItem('auth_username');
          localStorage.removeItem('auth_role');
          const err = (data && data.error) ? data.error : (text || 'Đăng nhập thất bại.');
          showMessage('danger', '❌ ' + err);
        }

      } catch (e) {
        console.error('Login error', e);
        localStorage.clear();
        showMessage('danger', '❌ Lỗi kết nối. Vui lòng thử lại.');
      } finally {
        submitBtn.disabled = false;
        submitBtn.classList.remove('opacity-60','cursor-wait');
      }
    });
  })();
</script>
</body>
</html>
