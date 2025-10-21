<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="true" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Đơn được phân công</title>
  <style>
    .btn{padding:.45rem .8rem;border:1px solid #ddd;border-radius:.5rem;background:#fff;cursor:pointer}
    .btn.primary{background:#2563eb;color:#fff;border-color:#2563eb}
    .btn.light{background:#f3f4f6}
    .toolbar{display:flex;gap:.5rem;flex-wrap:wrap;align-items:center}
    .wrap{max-width:1100px;margin:1.25rem auto;padding:0 1rem}
    .table{width:100%;border-collapse:collapse}
    .table th,.table td{padding:.6rem;border-bottom:1px solid #eee;text-align:left}
    .badge{padding:.2rem .5rem;border-radius:.5rem;background:#eef2ff}
    select,input{padding:.4rem .5rem;border:1px solid #ddd;border-radius:.5rem}
  </style>
</head>
<body>
<div class="wrap">
  <div class="toolbar" style="justify-content:space-between;margin-bottom:1rem">
    <div>
      <h1 style="margin:0">Đơn được phân công</h1>
      <div class="toolbar" style="margin-top:.4rem">
        <button class="btn light" onclick="location.href='<%=ctx%>/dashboard-shipper.jsp'">⬅️ Dashboard</button>
        <button class="btn" onclick="reloadData()">Tải lại</button>
      </div>
    </div>
    <div class="toolbar">
      <label>Trạng thái:</label>
      <select id="status">
        <option value="">(Tất cả)</option>
        <option>ASSIGNED</option>
        <option>PICKED_UP</option>
        <option>IN_TRANSIT</option>
        <option>OUT_FOR_DELIVERY</option>
        <option>DELIVERED</option>
        <option>FAILED_DELIVERY</option>
        <option>RETURNING</option>
        <option>RETURNED</option>
        <option>CANCELLED</option>
      </select>
      <label>Trang:</label><input id="page" type="number" value="1" min="1" style="width:76px">
      <label>Kích thước:</label>
      <select id="size"><option>10</option><option selected>20</option><option>50</option></select>
      <button class="btn primary" onclick="reloadData()">Lọc</button>
    </div>
  </div>

  <div style="overflow:auto">
    <table class="table">
      <thead><tr>
        <th>#</th><th>Order ID</th><th>Trạng thái</th><th>COD</th><th>Cập nhật</th><th></th>
      </tr></thead>
      <tbody id="tbody"></tbody>
    </table>
  </div>

  <div class="toolbar" style="justify-content:space-between;margin-top:1rem">
    <div id="pageInfo" class="muted"></div>
    <div class="toolbar">
      <button class="btn" id="prevBtn">Trước</button>
      <button class="btn" id="nextBtn">Sau</button>
    </div>
  </div>
  <div id="err" style="color:#ef4444;margin-top:.5rem"></div>
</div>

<script>
const apiBase = '<%=ctx%>/api/shipper';
let curPage = 1, totalPages = 1;

async function reloadData() {
  document.getElementById('err').textContent = '';
  const status = document.getElementById('status').value;
  const size = parseInt(document.getElementById('size').value||'20',10);
  const url = new URL(apiBase+'/shipments', window.location.origin);
  url.searchParams.set('page', curPage);
  url.searchParams.set('size', size);
  if (status) url.searchParams.set('status', status);
  try {
    const res = await fetch(url, {credentials:'same-origin'});
    if (!res.ok) throw new Error('HTTP ' + res.status);
    const data = await res.json();
    totalPages = Math.max(1, Math.ceil((data.total||0)/size));
    document.getElementById('page').value = curPage;
    document.getElementById('pageInfo').textContent = `Trang ${curPage}/${totalPages} — Tổng ${data.total||0} đơn`;

    const tb = document.getElementById('tbody'); tb.innerHTML = '';
    (data.items||[]).forEach(it=>{
      const last = (it.lastUpdateAt||'').replace('T',' ').slice(0,19);
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td>${it.id}</td>
        <td>${it.orderId ?? '-'}</td>
        <td><span class="badge">${it.status}</span></td>
        <td>${(it.codAmount||0).toLocaleString('vi-VN')} ₫</td>
        <td>${last}</td>
        <td><button class="btn primary" onclick="location.href='${'<%=ctx%>'}/shipment-detail.jsp?id=${it.id}'">Chi tiết</button></td>
      `;
      tb.appendChild(tr);
    });

    document.getElementById('prevBtn').disabled = curPage<=1;
    document.getElementById('nextBtn').disabled = curPage>=totalPages;
  } catch(e) {
    document.getElementById('err').textContent = 'Lỗi tải: '+e.message+'. Có thể cần đăng nhập.';
  }
}

document.getElementById('prevBtn').onclick = ()=>{ if(curPage>1){curPage--; reloadData();}};
document.getElementById('nextBtn').onclick = ()=>{ if(curPage<totalPages){curPage++; reloadData();}};
document.getElementById('page').addEventListener('change', e=>{
  const p = parseInt(e.target.value||'1',10);
  curPage = Math.max(1, p); reloadData();
});
reloadData();
</script>
</body>
</html>
