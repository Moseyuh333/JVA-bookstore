<%@ page contentType="text/html;charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Shipper Dashboard</title>
  <!-- Chart.js CDN -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
  <style>
    .btn{padding:.5rem .8rem;border:1px solid #ddd;border-radius:.5rem;background:#fff;cursor:pointer}
    .btn.primary{background:#2563eb;color:#fff;border-color:#2563eb}
    .btn.success{background:#16a34a;color:#fff;border-color:#16a34a}
    .btn.light{background:#f3f4f6}
    .card{border:1px solid #e5e7eb;border-radius:.75rem;padding:1rem;background:#fff}
    .grid{display:grid;gap:1rem}
    .grid-3{grid-template-columns:repeat(3,minmax(0,1fr))}
    .table{width:100%;border-collapse:collapse}
    .table th,.table td{padding:.6rem;border-bottom:1px solid #eee;text-align:left}
    .badge{padding:.2rem .5rem;border-radius:.5rem;background:#eef2ff}
    .toolbar{display:flex;gap:.5rem;flex-wrap:wrap;align-items:center}
    .muted{color:#6b7280;font-size:.9rem}
    .wrap{max-width:1100px;margin:1.25rem auto;padding:0 1rem}
  </style>
</head>
<body>
<div class="wrap">
  <div class="toolbar" style="justify-content:space-between;margin-bottom:1rem">
    <div>
      <h1 style="margin:0">🚚 Shipper Dashboard</h1>
      <div class="muted">Tổng quan các đơn được phân công cho bạn</div>
    </div>
    <div class="toolbar">
      <button class="btn light" onclick="location.href='<%=ctx%>/dashboard-shipper.jsp'">Dashboard</button>
      <button class="btn" onclick="location.href='<%=ctx%>/shipments.jsp'">Tất cả đơn</button>
    </div>
  </div>

  <!-- Cards -->
  <div class="grid grid-3" style="margin-bottom:1rem">
    <div class="card">
      <div class="muted">Đang giao</div>
      <div id="cInProgress" style="font-size:2rem;font-weight:700">0</div>
    </div>
    <div class="card">
      <div class="muted">Đã giao</div>
      <div id="cDelivered" style="font-size:2rem;font-weight:700">0</div>
    </div>
    <div class="card">
      <div class="muted">Thất bại/Hoàn</div>
      <div id="cFailed" style="font-size:2rem;font-weight:700">0</div>
    </div>
  </div>

  <!-- Chart + CTA -->
  <div class="grid" style="grid-template-columns: 1fr 320px; align-items:stretch; margin-bottom:1rem">
    <div class="card">
      <canvas id="pieChart" height="180"></canvas>
    </div>
    <div class="card" style="display:flex;flex-direction:column;gap:.75rem">
      <div style="font-weight:600">Hành động nhanh</div>
      <button class="btn primary" onclick="location.href='<%=ctx%>/shipments.jsp'">Xem tất cả đơn được phân công</button>
      <button class="btn" onclick="reloadData()">Tải lại số liệu</button>
      <div class="muted">* Số liệu theo user đăng nhập hiện tại.</div>
      <div id="err" class="muted" style="color:#ef4444"></div>
    </div>
  </div>

  <!-- Latest table -->
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:.5rem">
      <div style="font-weight:600">10 đơn cập nhật gần nhất</div>
      <button class="btn" onclick="location.href='<%=ctx%>/shipments.jsp'">Xem tất cả</button>
    </div>
    <div style="overflow:auto">
      <table class="table">
        <thead>
        <tr>
          <th>#</th>
          <th>Order ID</th>
          <th>Trạng thái</th>
          <th>COD</th>
          <th>Cập nhật</th>
          <th></th>
        </tr>
        </thead>
        <tbody id="tblBody"></tbody>
      </table>
    </div>
  </div>
</div>

<script>
const apiBase = '<%=ctx%>/api/shipper';

let pie;
async function reloadData() {
  document.getElementById('err').textContent = '';
  try {
    // 1) Stats for Cards + Pie
    const sRes = await fetch(apiBase + '/stats', {credentials: 'same-origin'});
    if (!sRes.ok) throw new Error('Stats HTTP ' + sRes.status);
    const st = await sRes.json();
    const inProgress = st.inProgress || 0;
    const delivered = st.delivered || 0;
    const failed = st.failed || 0;

    document.getElementById('cInProgress').textContent = inProgress;
    document.getElementById('cDelivered').textContent = delivered;
    document.getElementById('cFailed').textContent = failed;

    const ctx = document.getElementById('pieChart');
    const data = {
      labels: ['Đang giao', 'Đã giao', 'Thất bại'],
      datasets: [{ data: [inProgress, delivered, failed] }]
    };
    if (pie) { pie.destroy(); }
    pie = new Chart(ctx, { type:'pie', data });

    // 2) Latest 10 shipments
    const lRes = await fetch(apiBase + '/shipments?page=1&size=10', {credentials:'same-origin'});
    if (!lRes.ok) throw new Error('List HTTP ' + lRes.status);
    const list = await lRes.json();
    const tb = document.getElementById('tblBody');
    tb.innerHTML = '';
    (list.items||[]).forEach(it=>{
      const tr = document.createElement('tr');
      const last = (it.lastUpdateAt||'').replace('T',' ').slice(0,19);
      tr.innerHTML = `
        <td>${it.id}</td>
        <td>${it.orderId ?? '-'}</td>
        <td><span class="badge">${it.status}</span></td>
        <td>${(it.codAmount||0).toLocaleString('vi-VN')} ₫</td>
        <td>${last}</td>
        <td><button class="btn success" onclick="location.href='${'<%=ctx%>'}/shipment-detail.jsp?id=${it.id}'">Chi tiết</button></td>
      `;
      tb.appendChild(tr);
    });
  } catch(e) {
    document.getElementById('err').textContent = 'Lỗi tải: ' + e.message + '. Có thể cần đăng nhập lại.';
  }
}
reloadData();
</script>
</body>
</html>
