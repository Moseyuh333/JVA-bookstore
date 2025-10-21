<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String ctx = request.getContextPath();
  String sid = request.getParameter("id")==null?"":request.getParameter("id");
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chi tiết vận đơn #<%=sid%></title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Đồng bộ “vibe” trang chủ, không dùng header.jsp -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="bg-gray-50 text-gray-800 min-h-screen">
<div class="container mx-auto px-4 py-8">

  <div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-semibold">Vận đơn #<span id="ship-id"><%=sid%></span></h1>
    <a href="<%=ctx%>/shipments.jsp" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
      « Quay lại danh sách
    </a>
  </div>

  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    <!-- Thông tin -->
    <div class="lg:col-span-2 space-y-6">
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="px-4 py-3 border-b border-amber-100">
          <h2 class="text-lg font-medium">Thông tin</h2>
        </div>
        <div class="p-4">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <div class="text-gray-500 text-sm">Đơn hàng</div>
              <div id="order-code" class="font-medium">—</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">Khách hàng</div>
              <div id="receiver-name" class="font-medium">—</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">SĐT</div>
              <div id="receiver-phone" class="font-medium">—</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">Địa chỉ</div>
              <div id="receiver-address" class="font-medium">—</div>
            </div>
            <div>
              <div class="text-gray-500 text-sm">Trạng thái</div>
              <span id="status-badge" class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-700">—</span>
            </div>
            <div>
              <div class="text-gray-500 text-sm">Thu hộ (COD)</div>
              <div id="cod-amount" class="font-medium">0</div>
            </div>
          </div>
        </div>
      </div>

      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="px-4 py-3 border-b border-amber-100">
          <h2 class="text-lg font-medium">Dòng thời gian</h2>
        </div>
        <div class="p-4">
          <ul id="events" class="space-y-3">
            <!-- JS render -->
          </ul>
          <p id="err" class="text-sm text-red-600 mt-3"></p>
        </div>
      </div>
    </div>

    <!-- Cập nhật -->
    <div class="space-y-6">
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="px-4 py-3 border-b border-amber-100">
          <h2 class="text-lg font-medium">Cập nhật trạng thái</h2>
        </div>
        <div class="p-4 space-y-4">
          <select id="evt-status" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full">
            <option value="PICKED_UP">Đã lấy hàng</option>
            <option value="IN_TRANSIT">Đang vận chuyển</option>
            <option value="OUT_FOR_DELIVERY">Đang giao</option>
            <option value="FAILED_DELIVERY">Giao thất bại</option>
            <option value="RETURNING">Đang hoàn</option>
            <option value="RETURNED">Đã hoàn</option>
          </select>

          <input id="evt-note" type="text" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full" placeholder="Ghi chú (tuỳ chọn)">

          <div class="flex items-center justify-end gap-2">
            <button id="btnAddEvent" class="inline-flex items-center px-3 py-2 rounded-md border border-amber-700 bg-amber-700 text-white hover:bg-amber-600 text-sm">
              Ghi sự kiện
            </button>
          </div>
        </div>
      </div>

      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="px-4 py-3 border-b border-amber-100">
          <h2 class="text-lg font-medium">Xác nhận đã giao</h2>
        </div>
        <div class="p-4 space-y-3">
          <input id="proofUrl" type="text" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full" placeholder="URL ảnh bằng chứng (bắt buộc)">
          <label class="flex items-center gap-2">
            <input id="codCollected" type="checkbox">
            <span>Đã thu COD</span>
          </label>
          <button id="btnDeliver" class="inline-flex items-center px-3 py-2 rounded-md border border-red-600 bg-red-600 text-white hover:bg-red-500 text-sm w-full">
            Đánh dấu DELIVERED
          </button>
        </div>
      </div>
    </div>
  </div>

</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
<script>feather.replace();</script>

<script>
  const ctx = '<%=ctx%>';
  const id = '<%=sid%>';
  function guardRole(){
    const role = localStorage.getItem('auth_role')||'';
    if(role.toLowerCase()!=='shipper'){ location.href = ctx+'/login.jsp'; }
  }
  guardRole();

  async function authFetch(url,opt={}){
    const token = localStorage.getItem('auth_token')||'';
    const headers = new Headers(opt.headers||{});
    if (token) headers.set('Authorization','Bearer '+token);
    const res = await fetch(url,{...opt, headers});
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

  function setBadge(status){
    const el = document.getElementById('status-badge');
    if(!el) return;
    el.textContent = status||'-';
  }

  async function load(){
    try{
      const d = await authFetch(`${apiBase}/shipments/${id}`);
      document.getElementById('order-code').textContent = d.orderCode||'-';
      document.getElementById('receiver-name').textContent = d.receiverName||'-';
      document.getElementById('receiver-phone').textContent = d.receiverPhone||'-';
      document.getElementById('receiver-address').textContent = d.receiverAddress||'-';
      document.getElementById('cod-amount').textContent = (d.codAmount||0).toLocaleString('vi-VN') + ' ₫';
      setBadge(d.status);

      const evts = await authFetch(`${apiBase}/shipments/${id}/events`);
      const ul = document.getElementById('events');
      ul.innerHTML = '';
      (evts||[]).forEach(e=>{
        const t = e.createdAt ? new Date(e.createdAt).toLocaleString('vi-VN') : '-';
        ul.insertAdjacentHTML('beforeend', `
          <li class="rounded-lg border border-gray-200 bg-white px-3 py-2">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium">${e.status}</span>
              <span class="text-xs text-gray-500">${t}</span>
            </div>
            ${e.note ? `<div class="text-sm text-gray-700 mt-1">${e.note}</div>` : ``}
            ${e.evidenceUrl ? `<a class="text-sm text-amber-700 hover:underline" href="${e.evidenceUrl}" target="_blank">Xem bằng chứng</a>` : ``}
          </li>`);
      });
    }catch(e){
      document.getElementById('err').textContent = e.message;
    }
  }

  document.getElementById('btnAddEvent').onclick = async ()=>{
    try{
      const status = document.getElementById('evt-status').value;
      const note = document.getElementById('evt-note').value;
      await authFetch(`${apiBase}/shipments/${id}/events`, {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({ status, note })
      });
      await load();
    }catch(e){
      alert(e.message);
    }
  };

  document.getElementById('btnDeliver').onclick = async ()=>{
    try{
      const proofUrl = document.getElementById('proofUrl').value.trim();
      const codCollected = document.getElementById('codCollected').checked;
      await authFetch(`${apiBase}/shipments/${id}/deliver`, {
        method:'PUT',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({ proofUrl, codCollected })
      });
      await load();
    }catch(e){
      alert(e.message);
    }
  };

  load();
</script>
</body>
</html>
