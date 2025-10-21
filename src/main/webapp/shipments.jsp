<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="true" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Đơn được phân công</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <style>
    body{margin:0;background:linear-gradient(135deg,#2b1a12 0%,#20150f 100%);color:#e5e7eb;font-family:system-ui}
    .wrap{max-width:1100px;margin:auto;padding:16px}
    .top{display:flex;justify-content:space-between;align-items:center;padding:12px 16px;background:#190f0a;border:1px solid #3b2a1f;border-radius:14px;margin:16px 0}
    .brand{display:flex;gap:10px;align-items:center}
    .badge{display:inline-flex;align-items:center;justify-content:center;width:34px;height:34px;border-radius:10px;background:#fef3c7;color:#b45309;font-weight:700}
    .btn{padding:8px 12px;border-radius:12px;border:1px solid #3b2a1f;background:#23160f;color:#fff;cursor:pointer}
    .btn.primary{background:#b45309;border-color:#b45309}
    .card{background:#1c1917;border:1px solid #3b2a1f;border-radius:16px;padding:14px}
    .row{display:flex;gap:8px;flex-wrap:wrap;align-items:center}
    select,input{background:#23160f;border:1px solid #3b2a1f;border-radius:12px;color:#e5e7eb;padding:8px 12px}
    table{width:100%;border-collapse:collapse}
    th,td{border-bottom:1px solid #2e241b;padding:10px 12px;text-align:left}
    .pill{padding:.25rem .5rem;border-radius:9999px;background:#fef3c7;color:#b45309}
    .err{color:#ef4444}
  </style>
</head>
<body>
<div class="wrap">
  <div class="top">
    <div class="brand">
      <div class="badge">📋</div>
      <div>
        <div style="font-size:18px;font-weight:800;color:#fcd34d">Đơn được phân công</div>
        <div style="font-size:12px;color:#9ca3af">Quản lý toàn bộ vận đơn của bạn</div>
      </div>
    </div>
    <div style="display:flex;gap:8px">
      <button class="btn" onclick="location.href='<%=ctx%>/dashboard-shipper.jsp'">Dashboard</button>
      <button class="btn primary" onclick="location.href='<%=ctx%>/shipments.jsp'">Tất cả đơn</button>
      <button id="logout" class="btn">Đăng xuất</button>
    </div>
  </div>

  <div class="card">
    <div class="row">
      <select id="fStatus">
        <option value="">Tất cả trạng thái</option>
        <option>ASSIGNED</option><option>PICKED_UP</option><option>IN_TRANSIT</option>
        <option>OUT_FOR_DELIVERY</option><option>DELIVERED</option><option>FAILED_DELIVERY</option>
      </select>
      <input id="fQuery" placeholder="Tìm theo mã đơn / order id">
      <button class="btn" onclick="load(1)">Lọc</button>
      <div id="err" class="err" style="margin-left:auto"></div>
    </div>
  </div>

  <div class="card" style="margin-top:12px">
    <div style="overflow:auto">
      <table>
        <thead><tr style="color:#9ca3af"><th>#</th><th>Order ID</th><th>Trạng thái</th><th>COD</th><th>Cập nhật</th><th></th></tr></thead>
        <tbody id="rows"></tbody>
      </table>
    </div>
    <div class="row" style="justify-content:space-between;margin-top:8px">
      <button class="btn" onclick="if(page>1) load(page-1)">« Trang trước</button>
      <div id="pageInfo" style="color:#9ca3af"></div>
      <button class="btn" onclick="if(page<total) load(page+1)">Trang sau »</button>
    </div>
  </div>
</div>

<script>
  // ===== Auth wrapper (tự gắn JWT & parse an toàn) =====
  const ctx = '<%=ctx%>';
  const TOKEN = localStorage.getItem('auth_token');
  const ROLE  = (localStorage.getItem('auth_role')||'').toLowerCase();
  if (!TOKEN || ROLE !== 'shipper') location.replace(ctx + '/login.jsp');

  async function authFetch(url,opt={}) {
    const headers = new Headers(opt.headers||{});
    headers.set('Authorization','Bearer '+TOKEN);
    headers.set('Accept','application/json');
    const res = await fetch(url,{...opt,headers});
    const ct = res.headers.get('content-type')||'';
    if (!res.ok) {
      const body = await res.text();
      throw new Error(`HTTP ${res.status} – ${ct.includes('json')? body : 'Non-JSON: ' + body.slice(0,120)}`);
    }
    if (ct.includes('json')) return res.json();
    const txt = await res.text();
    throw new Error('Non-JSON response: ' + txt.slice(0,120));
  }

  const apiBase = ctx + '/api/shipper';
  let page=1, size=20, total=1;

  async function load(p=1){
    page=p;
    err.textContent='';
    const st = fStatus.value || '';
    const q  = encodeURIComponent((fQuery.value||'').trim());
    try{
      const data = await authFetch(`${apiBase}/shipments?page=${page}&size=${size}&status=${st}&q=${q}`);
      total = data.totalPages || 1;
      pageInfo.textContent = `Trang ${page}/${total}`;
      rows.innerHTML='';
      (data.items||[]).forEach(it=>{
        const last=(it.lastUpdateAt||'').replace('T',' ').slice(0,19);
        rows.insertAdjacentHTML('beforeend', `
          <tr>
            <td>\${it.id??'-'}</td>
            <td>\${it.orderId??'-'}</td>
            <td><span class="pill">\${it.status??'-'}</span></td>
            <td>\${(it.codAmount||0).toLocaleString('vi-VN')} ₫</td>
            <td>\${last||'-'}</td>
            <td><button class="btn primary" onclick="location.href='\${ctx}/shipment-detail.jsp?id=\${it.id}'">Chi tiết</button></td>
          </tr>`);
      });
    }catch(e){
      err.textContent = 'Lỗi tải: ' + e.message; // sẽ thấy rõ nếu server trả HTML (do 401/404/redirect)
    }
  }

  document.getElementById('logout').onclick=()=>{localStorage.clear();location.href=ctx+'/login.jsp';};
  load(1);
</script>
</body>
</html>
