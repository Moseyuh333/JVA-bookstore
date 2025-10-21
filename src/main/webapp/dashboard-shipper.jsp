<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="true" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Shipper Dashboard</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
  <style>
    :root{--amber:#92400e;--bg:#12100f;--card:#1c1917;--muted:#9ca3af;--ring:#f59e0b}
    body{margin:0;background:linear-gradient(135deg,#2b1a12 0%,#20150f 100%);color:#e5e7eb;font-family:system-ui,Segoe UI,Roboto}
    .wrap{max-width:1100px;margin:auto;padding:16px}
    .top{display:flex;justify-content:space-between;align-items:center;padding:12px 16px;background:#190f0a;border:1px solid #3b2a1f;border-radius:14px;margin:16px 0}
    .brand{display:flex;gap:10px;align-items:center}
    .badge{display:inline-flex;align-items:center;justify-content:center;width:34px;height:34px;border-radius:10px;background:#fef3c7;color:#b45309;font-weight:700}
    .btn{padding:8px 12px;border-radius:12px;border:1px solid #3b2a1f;background:#23160f;color:#fff;cursor:pointer}
    .btn.primary{background:#b45309;border-color:#b45309}
    .btn.light{background:#2a1a12}
    .grid{display:grid;gap:12px}
    .g3{grid-template-columns:repeat(3,minmax(0,1fr))}
    .card{background:#1c1917;border:1px solid #3b2a1f;border-radius:16px;padding:16px}
    .muted{color:#9ca3af}
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
        <div class="badge">🚚</div>
        <div>
          <div style="font-size:18px;font-weight:800;color:#fcd34d">Shipper Dashboard</div>
          <div class="muted" style="font-size:12px">Tổng quan các đơn được phân công</div>
        </div>
      </div>
      <div style="display:flex;gap:8px">
        <button class="btn light" onclick="location.href='<%=ctx%>/dashboard-shipper.jsp'">Dashboard</button>
        <button class="btn" onclick="location.href='<%=ctx%>/shipments.jsp'">Tất cả đơn</button>
        <button id="logout" class="btn">Đăng xuất</button>
      </div>
    </div>

    <div class="grid g3">
      <div class="card"><div class="muted">Đang giao</div><div id="cIn" style="font:700 28px/1 system-ui">0</div></div>
      <div class="card"><div class="muted">Đã giao</div><div id="cDone" style="font:700 28px/1 system-ui">0</div></div>
      <div class="card"><div class="muted">Thất bại/Hoàn</div><div id="cFail" style="font:700 28px/1 system-ui">0</div></div>
    </div>

    <div class="grid" style="grid-template-columns: 1fr 320px; margin-top:12px">
      <div class="card"><canvas id="pie" height="180"></canvas></div>
      <div class="card">
        <div style="font-weight:700;margin-bottom:8px">Hành động nhanh</div>
        <button class="btn primary" onclick="location.href='<%=ctx%>/shipments.jsp'">Xem tất cả đơn</button>
        <div style="height:8px"></div>
        <button class="btn light" onclick="reload()">Tải lại số liệu</button>
        <div class="muted" style="margin-top:6px;font-size:12px">* Số liệu theo user đăng nhập.</div>
        <div id="err" class="err" style="margin-top:6px"></div>
      </div>
    </div>

    <div class="card" style="margin-top:12px">
      <div style="font-weight:700;margin-bottom:6px">10 đơn cập nhật gần nhất</div>
      <div style="overflow:auto">
        <table>
          <thead><tr class="muted"><th>#</th><th>Order ID</th><th>Trạng thái</th><th>COD</th><th>Cập nhật</th><th></th></tr></thead>
          <tbody id="rows"></tbody>
        </table>
      </div>
    </div>
  </div>

<script>
  // ====== AUTH + FETCH WRAPPER (fix JSON lỗi '<!doctype...') ======
  const ctx = '<%=ctx%>';
  const TOKEN = localStorage.getItem('auth_token');
  const ROLE  = (localStorage.getItem('auth_role')||'').toLowerCase();
  if (!TOKEN || ROLE !== 'shipper') location.replace(ctx + '/login.jsp');

  async function authFetch(url, opt={}) {
    const headers = new Headers(opt.headers||{});
    headers.set('Authorization','Bearer '+TOKEN);
    headers.set('Accept','application/json');
    const res = await fetch(url,{...opt, headers});
    // Nếu server trả HTML (redirect/404), đừng .json() ngay
    const ct = res.headers.get('content-type')||'';
    if (!res.ok) {
      const body = await res.text();
      throw new Error(`HTTP ${res.status} – ${ct.includes('json')? body : 'Non-JSON: ' + body.slice(0,120)}`);
    }
    if (ct.includes('json')) return res.json();
    const txt = await res.text();
    throw new Error('Non-JSON response: ' + txt.slice(0,120));
  }

  document.getElementById('logout').onclick = () => { localStorage.clear(); location.href=ctx+'/login.jsp'; };

  const apiBase = ctx + '/api/shipper';
  let chart;

  async function reload(){
    document.getElementById('err').textContent='';
    try{
      const st = await authFetch(apiBase + '/stats');
      const inP = st.inProgress||0, done=st.delivered||0, fail=st.failed||0;
      cIn.textContent=inP; cDone.textContent=done; cFail.textContent=fail;

      const data = {labels:['Đang giao','Đã giao','Thất bại'], datasets:[{data:[inP,done,fail]}]};
      if (chart) chart.destroy();
      chart = new Chart(document.getElementById('pie'),{type:'pie',data});

      const list = await authFetch(apiBase + '/shipments?page=1&size=10');
      const tb = document.getElementById('rows'); tb.innerHTML='';
      (list.items||[]).forEach(it=>{
        const last=(it.lastUpdateAt||'').replace('T',' ').slice(0,19);
        tb.insertAdjacentHTML('beforeend', `
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
      document.getElementById('err').textContent = e.message;
    }
  }
  reload();
</script>
</body>
</html>
