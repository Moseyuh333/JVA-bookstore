<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="container" style="max-width: 480px;">
  <div class="text-center my-4">
    <img src="assets/img/nkbookstore-logo.png" alt="NKbookstore Logo" style="height:64px;">
    <h2 class="mb-1" style="color:#1a237e;font-weight:700;letter-spacing:1px;">NKbookstore</h2>
    <div class="text-muted small">by bibo090809@gmail.com</div>
  </div>
  <h4 class="mb-3" style="color:#3949ab;">Login</h4>
  <form id="loginForm">
    <div class="mb-3">
      <label class="form-label">Username</label>
      <input class="form-control" name="username" required />
    </div>
    <div class="mb-3">
      <label class="form-label">Password</label>
      <input class="form-control" type="password" name="password" required />
    </div>
  <button class="btn btn-primary w-100" style="background:#3949ab;border:none;" type="submit">Login</button>
  </form>
  <div class="mt-3 text-center">
    <a href="register.jsp" style="color:#3949ab;">Register</a> | <a href="forgot-password.jsp" style="color:#3949ab;">Forgot Password?</a>
  </div>
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
      document.getElementById('result').textContent = 'Token acquired.';
    } else {
      document.getElementById('result').textContent = 'Login failed.';
    }
  });

  document.getElementById('loadBooks').addEventListener('click', async ()=>{
    const res = await fetch('api/books', {headers:{'Authorization':'Bearer ' + token}});
    const text = await res.text();
    document.getElementById('result').textContent = text;
  });
</script>
