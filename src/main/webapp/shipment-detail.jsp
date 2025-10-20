<%@ page contentType="text/html;charset=UTF-8" %>
<%
  String ctx = request.getContextPath();
  String sid = request.getParameter("id")==null?"":request.getParameter("id");
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Chi tiết vận đơn #<%=sid%></title>
  <style>
    .btn{padding:.45rem .8rem;border:1px solid #ddd;border-radius:.5rem;background:#fff;cursor:pointer}
    .btn.primary{background:#2563eb;color:#fff;border-color:#2563eb}
    .btn.success{background:#16a34a;color:#fff;border-color:#16a34a}
    .btn.light{background:#f3f4f6}
    .card{border:1px solid #e5e7eb;border-radius:.75rem;padding:1rem;background:#fff}
    .wrap{max-width:900px;margin:1.25rem auto;padding:0 1rem}
    .grid{display:grid;gap:1rem}
    .grid-2{grid-template-columns:repeat(2,minmax(0,1fr))}
    input,select,textarea{padding:.45rem .6rem;border:1px solid #ddd;border-radius:.5rem;width:100%}
    .timeline li{border:1px solid #eee;padding:.7rem;border-radius:.5rem}
    .badge{padding:.2rem .5rem;border-radius:.5rem;background:#eef2ff}
    .muted{color:#6b7280}
  </style>
</head>
<body>
<div class="wrap">
  <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem">
    <div>
      <h1 style="margin:0">Vận đơn #<%=sid%></h1>
      <div class="muted">Xem & cập nhật trạng thái</div>
    </div>
    <div>
      <button class="btn light" onclick="location.href='<%=ctx%>/dashboard-shipper.jsp'">⬅️ Dashboard</button>
      <button class="btn" onclick="location.href='<%=ctx%>/shipments.jsp'">Tất cả đơn</button>
    </div>
  </div>

  <div class="grid grid-2">
    <div class="card" id="info">Đang tải...</div>
    <div class="card">
      <div style="font-weight:600;margin-bottom:.5rem">Cập nhật trạng thái</div>
      <label>Trạng thái kế tiếp</label>
      <select id="nextStatus">
        <option>PICKED_UP</option>
        <option>IN_TRANSIT</option>
        <option>OUT_FOR_DELIVERY</option>
        <option>FAILED_DELIVERY</option>
      </select>
      <label style="margin-top:.5rem">Ghi chú</label>
      <textarea id="note" rows="2" placeholder="Ghi chú..."></textarea>
      <label style="margin-top:.5rem">Evidence URL (tuỳ chọn)</label>
      <input id="evidenceUrl" placeholder="/uploads/proof/xxx.jpg">
      <button class="btn primary" style="margin-top:.6rem" onclick="advance()">Cập nhật</button>
    </div>
  </div>

  <div class="card" style="margin-top:1rem">
    <div style="font-weight:600;margin-bottom:.5rem">Đánh dấu đã giao (DELIVERED)</div>
    <div style="display:grid;gap:.6rem;grid-template-columns:repeat(2,minmax(0,1fr))">
      <div>
        <label>Ảnh bằng chứng (bắt buộc)</label>
        <input id="proofUrl" placeholder="/uploads/proof/xxx.jpg">
      </div>
      <div style="display:flex;gap:.5rem;align-items:center">
        <input id="codCollected" type="checkbox" style="width:1.1rem;height:1.1rem">
        <label>Đã thu COD (nếu đơn có COD)</label>
      </div>
    </div>
    <label style="margin-top:.5rem">Ghi chú</label>
    <textarea id="deliverNote" rows="2" placeholder="Ví dụ: Khách nhận 10:05"></textarea>
    <button class="btn success" style="margin-top:.6rem" onclick="markDelivered()">Đánh dấu ĐÃ GIAO</button>
  </div>

  <div class="card" style="margin-top:1rem">
    <div style="font-weight:600;margin-bottom:.5rem">Timeline</div>
    <ul id="timeline" class="timeline" style="display:grid;gap:.5rem"></ul>
  </div>

  <div id="err" style="color:#ef4444;margin-top:.6rem"></div>
</div>

<script>
const apiBase = '<%=ctx%>/api/shipper';
const sid = '<%=sid%>';
if (!sid) document.getElementById('err').textContent = 'Thiếu tham số id';

async function loadDetail(){
  try{
    const res = await fetch(`${apiBase}/shipments/${sid}`, {credentials:'same-origin'});
    if(!res.ok) throw new Error('HTTP '+res.status);
    const data = await res.json();
    const s = data.shipment;

    const info = document.getElementById('info');
    const pickup = s.pickupAt ? s.pickupAt.replace('T',' ').slice(0,19) : '-';
    const delivered = s.deliveredAt ? s.deliveredAt.replace('T',' ').slice(0,19) : '-';
    info.innerHTML = `
      <div><b>Trạng thái:</b> <span class="badge">${s.status}</span></div>
      <div class="muted" style="margin-top:.2rem">Order ID: ${s.orderId ?? '-'}</div>
      <div style="margin-top:.2rem"><b>COD:</b> ${(s.codAmount||0).toLocaleString('vi-VN')} ₫ — <b>Đã thu:</b> ${s.codCollected ? 'Có' : 'Chưa'}</div>
      <div style="margin-top:.2rem"><b>Pickup:</b> ${pickup}</div>
      <div style="margin-top:.2rem"><b>Delivered:</b> ${delivered}</div>
      <div style="margin-top:.2rem"><b>Proof:</b> ${s.proofImageUrl ? `<a href="${s.proofImageUrl}" target="_blank">xem ảnh</a>` : '-'}</div>
    `;

    const ul = document.getElementById('timeline');
    ul.innerHTML = '';
    (data.events||[]).forEach(ev=>{
      const at = ev.createdAt ? ev.createdAt.replace('T',' ').slice(0,19) : '';
      const li = document.createElement('li');
      li.innerHTML = `
        <div style="display:flex;justify-content:space-between;gap:.5rem">
          <div><b>${ev.status}</b> — ${ev.note || ''}</div>
          <div class="muted" style="font-size:.9rem">${at}</div>
        </div>
        ${ev.evidenceUrl ? `<div class="muted" style="margin-top:.2rem">Evidence: <a href="${ev.evidenceUrl}" target="_blank">${ev.evidenceUrl}</a></div>`:''}
        <div class="muted" style="font-size:.85rem;margin-top:.2rem">by ${ev.createdBy||'-'}</div>
      `;
      ul.appendChild(li);
    });
  }catch(e){
    document.getElementById('err').textContent = 'Lỗi tải chi tiết: '+e.message;
  }
}

async function advance(){
  try{
    const status = document.getElementById('nextStatus').value;
    const note = document.getElementById('note').value;
    const evidenceUrl = document.getElementById('evidenceUrl').value;
    const res = await fetch(`${apiBase}/shipments/${sid}/events`, {
      method:'POST', headers:{'Content-Type':'application/json'},
      credentials:'same-origin', body: JSON.stringify({status, note, evidenceUrl})
    });
    if(!res.ok){
      const er = await res.json().catch(()=>({})); throw new Error(er.message || ('HTTP '+res.status));
    }
    await loadDetail(); alert('Cập nhật thành công');
  }catch(e){ alert('Lỗi: '+e.message); }
}

async function markDelivered(){
  try{
    const codCollected = document.getElementById('codCollected').checked;
    const evidenceUrl = document.getElementById('proofUrl').value;
    const note = document.getElementById('deliverNote').value;
    const res = await fetch(`${apiBase}/shipments/${sid}/deliver`, {
      method:'PUT', headers:{'Content-Type':'application/json'},
      credentials:'same-origin', body: JSON.stringify({codCollected, evidenceUrl, note})
    });
    if(!res.ok){
      const er = await res.json().catch(()=>({})); throw new Error(er.message || ('HTTP '+res.status));
    }
    await loadDetail(); alert('Đã đánh dấu DELIVERED');
  }catch(e){ alert('Lỗi: '+e.message); }
}

loadDetail();
</script>
</body>
</html>
