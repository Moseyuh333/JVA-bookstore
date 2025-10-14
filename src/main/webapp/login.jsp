<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<decorator:head>
    <title>Login - NKbookstore</title>
</decorator:head>

<div class="container" style="max-width: 480px;">
  <div class="text-center my-4">
    <img src="assets/img/nkbookstore-logo.png" alt="NKbookstore Logo" style="width:64px;height:64px;border-radius:50%;">
    <h2 class="mb-1 mt-3" style="color:#1a237e;font-weight:700;letter-spacing:1px;">NKbookstore</h2>
    <div class="text-muted small">by bibo090809@gmail.com</div>
  </div>
  <h2 class="mb-3">Login</h2>
  <% if ("true".equals(request.getParameter("verified"))) { %>
    <div class="alert alert-success">Email verified successfully! You can now login.</div>
  <% } %>
  <form id="loginForm">
    <div class="mb-3">
      <label class="form-label">Username</label>
      <input class="form-control" name="username" required />
    </div>
    <div class="mb-3">
      <label class="form-label">Password</label>
      <input class="form-control" type="password" name="password" required />
    </div>
    <button class="btn btn-primary" type="submit">Login</button>
    <div class="mt-3 d-flex gap-3">
      <a href="register.jsp">Create account</a>
      <a href="forgot-password.jsp">Forgot password?</a>
    </div>
  </form>
  <hr/>
  <div>
    <button id="loadBooks" class="btn btn-outline-secondary" disabled>Load Protected Books</button>
    <pre id="result" class="mt-3"></pre>
  </div>
</div>

<script>
  let token = null;
  const form = document.getElementById('loginForm');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = new URLSearchParams(new FormData(form));
    const res = await fetch('api/login', {method:'POST', headers:{'Content-Type':'application/x-www-form-urlencoded'}, body: data});
    if(res.ok){
      const json = await res.json();
      token = json.token;
      document.getElementById('loadBooks').disabled = false;
      document.getElementById('result').textContent = 'Login successful. Token acquired.';
    } else {
      const text = await res.text();
      try { const j = JSON.parse(text); document.getElementById('result').textContent = 'Login failed: ' + (j.error || text); }
      catch(e){ document.getElementById('result').textContent = 'Login failed: ' + text; }
    }
  });

  document.getElementById('loadBooks').addEventListener('click', async ()=>{
    const res = await fetch('api/books', {headers:{'Authorization':'Bearer ' + token}});
    const text = await res.text();
    document.getElementById('result').textContent = text;
  });
</script>
