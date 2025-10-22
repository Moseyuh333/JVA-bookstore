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
  <!-- Đồng bộ “vibe” trang chủ, không dùng header.jsp -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/feather-icons"></script>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body class="bg-gray-50 text-gray-800 min-h-screen">
<div class="container mx-auto px-4 py-8">

  <div class="mb-8">
    <div class="flex items-center justify-between mb-4">
      <h1 class="text-2xl font-semibold">Bảng điều khiển Shipper</h1>
      <div class="flex items-center gap-2">
        <button id="logout" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
          <i data-feather="log-out" class="w-4 h-4 mr-2"></i>Đăng xuất
        </button>
      </div>
    </div>

    <!-- KPI -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="p-4">
          <div class="text-gray-500 text-sm">Đang giao</div>
          <div id="kpi-in-progress" class="text-2xl font-bold">0</div>
        </div>
      </div>
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="p-4">
          <div class="text-gray-500 text-sm">Đã giao</div>
          <div id="kpi-delivered" class="text-2xl font-bold">0</div>
        </div>
      </div>
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="p-4">
          <div class="text-gray-500 text-sm">Thất bại</div>
          <div id="kpi-failed" class="text-2xl font-bold">0</div>
        </div>
      </div>
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="p-4">
          <div class="text-gray-500 text-sm">Tỷ lệ thành công</div>
          <div id="kpi-success-rate" class="text-2xl font-bold">0%</div>
        </div>
      </div>
    </div>
  </div>

  <!-- Chart -->
  <div class="rounded-xl border border-amber-200 bg-white shadow-sm mb-8">
    <div class="px-4 py-3 border-b border-amber-100">
      <h2 class="text-lg font-medium">Tỷ lệ giao hàng</h2>
    </div>
    <div class="p-4">
      <canvas id="chart-success" height="140"></canvas>
    </div>
  </div>

  <!-- Bảng 10 vận đơn gần nhất -->
  <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
    <div class="px-4 py-3 border-b border-amber-100">
      <h2 class="text-lg font-medium">10 vận đơn gần nhất</h2>
    </div>
    <div class="p-4 overflow-x-auto">
      <table class="min-w-full divide-y divide-gray-200">
        <thead class="bg-gray-50">
          <tr class="hover:bg-gray-50">
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Mã</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Đơn hàng</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Khách</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trạng thái</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Cập nhật</th>
            <th class="px-3 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider"></th>
          </tr>
        </thead>
        <tbody id="recent-shipments">
          <!-- JS render -->
        </tbody>
      </table>
      <p id="err" class="text-sm text-red-600 mt-3"></p>
    </div>
  </div>

</div>

<%@ include file="/WEB-INF/includes/footer.jsp" %>
<script>feather.replace();</script>

<script>
  const ctx = '<%=ctx%>';
  function guardRole(){
    const role = localStorage.getItem('auth_role')||'';
    if(role.toLowerCase()!=='seller'){ location.href = ctx+'/login.jsp'; }
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

  document.getElementById('logout').onclick = () => { localStorage.clear(); location.href=ctx+'/login.jsp'; };

  const apiBase = ctx + '/api/seller';
  let chart;

  async function reload(){
    try{
      // KPI
      const stats = await authFetch(apiBase + '/stats');
      document.getElementById('kpi-in-progress').textContent = stats.inProgress||0;
      document.getElementById('kpi-delivered').textContent  = stats.delivered||0;
      document.getElementById('kpi-failed').textContent     = stats.failed||0;
      document.getElementById('kpi-success-rate').textContent = ((stats.successRate||0)*100).toFixed(0)+'%';

      // Chart
      const el = document.getElementById('chart-success');
      if (chart) chart.destroy();
      chart = new Chart(el, {
        type:'doughnut',
        data:{
          labels:['Thành công','Thất bại','Đang giao'],
          datasets:[{ data:[stats.delivered||0, stats.failed||0, stats.inProgress||0] }]
        }
      });

      // Recent
      const list = await authFetch(apiBase + '/shipments?size=10&page=1');
      const tbody = document.getElementById('recent-shipments');
      tbody.innerHTML = '';
      (list.items||[]).forEach(it=>{
        const lastEvt = (it.lastEventAt || it.updatedAt || it.createdAt || '').toString();
        const last = lastEvt ? new Date(lastEvt).toLocaleString('vi-VN') : '-';
        const badge = `<span class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium bg-gray-100 text-gray-700">${it.status??'-'}</span>`;
        tbody.insertAdjacentHTML('beforeend', `
          <tr class="hover:bg-gray-50">
            <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${it.id}</td>
            <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${it.orderCode||'-'}</td>
            <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${it.receiverName||'-'}</td>
            <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${badge}</td>
            <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${last}</td>
            <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">
              <button class="inline-flex items-center px-3 py-2 rounded-md border border-amber-700 bg-amber-700 text-white hover:bg-amber-600 text-sm"
                      onclick="location.href='${ctx}/shipment-detail.jsp?id=${it.id}'">
                Chi tiết
              </button>
            </td>
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