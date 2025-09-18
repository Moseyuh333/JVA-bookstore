<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<div class="container" style="max-width: 480px;">
  <h2 class="mb-3">Login</h2>
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
