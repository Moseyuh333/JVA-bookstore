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

  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/feather-icons"></script>
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
</head>
<body class="bg-gray-50 text-gray-800 min-h-screen">
<div class="container mx-auto px-4 py-8">

  <div class="mb-6 flex items-center justify-between">
    <h1 class="text-2xl font-semibold">Không gian Shipper</h1>
    <button id="logout" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
      <i data-feather="log-out" class="w-4 h-4 mr-2"></i>Đăng xuất
    </button>
  </div>

  <!-- CHART + SHIPPER INFO (chart nhỏ, info bên phải) -->
  <section class="mb-8 grid grid-cols-1 lg:grid-cols-2 gap-4">
    <!-- Card: Tỷ lệ giao hàng -->
    <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
      <div class="px-4 py-3 border-b border-amber-100 flex items-center justify-between">
        <h2 class="text-lg font-medium">Tỷ lệ giao hàng</h2>
        <span id="miniSubtitle" class="text-xs text-gray-500"></span>
      </div>
      <div class="p-4 flex justify-center">
        <div class="w-full max-w-[420px]" style="height: 220px; margin: 0 auto;">
          <canvas id="chart-success"></canvas>
        </div>
      </div>
    </div>

    <!-- Card: Thông tin Shipper -->
    <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
      <div class="px-4 py-3 border-b border-amber-100">
        <h2 class="text-lg font-medium">Thông tin Shipper</h2>
      </div>
      <div class="p-4">
        <div class="flex items-start gap-4">
          <div class="w-12 h-12 rounded-full bg-amber-100 flex items-center justify-center text-amber-700 font-semibold" id="shipperAvatar">S</div>
          <div class="flex-1">
            <div class="font-semibold text-gray-900" id="shipperFullname">—</div>
            <div class="text-sm text-gray-600" id="shipperUsername">—</div>
            <div class="text-sm text-gray-600" id="shipperEmail">—</div>
            <div class="text-sm text-gray-600" id="shipperPhone">—</div>
          </div>
        </div>

        <!-- 4 ô nhỏ: ĐÃ ĐỔI ô đầu thành TỶ LỆ THÀNH CÔNG -->
        <div class="grid grid-cols-2 gap-3 mt-4">
          <div class="rounded-lg border border-gray-100 p-3">
            <div class="text-xs text-gray-500">Tỷ lệ thành công</div>
            <div class="text-xl font-bold text-green-600" id="infoSuccessRate">0%</div>
          </div>
          <div class="rounded-lg border border-gray-100 p-3">
            <div class="text-xs text-gray-500">Đang giao</div>
            <div class="text-xl font-bold" id="infoInProgress">0</div>
          </div>
          <div class="rounded-lg border border-gray-100 p-3">
            <div class="text-xs text-gray-500">Đã giao</div>
            <div class="text-xl font-bold text-green-600" id="infoDelivered">0</div>
          </div>
          <div class="rounded-lg border border-gray-100 p-3">
            <div class="text-xs text-gray-500">Thất bại/Hủy</div>
            <div class="text-xl font-bold text-red-600" id="infoFailed">0</div>
          </div>
        </div>

        <div class="mt-4 text-xs text-gray-500" id="shipperMeta">—</div>
      </div>
    </div>
  </section>

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
          <tr class="skeleton">
            <td colspan="6" class="px-3 py-6">
              <div class="animate-pulse flex items-center gap-3">
                <div class="h-4 w-24 bg-gray-200 rounded"></div>
                <div class="h-4 w-32 bg-gray-200 rounded"></div>
                <div class="h-4 w-20 bg-gray-200 rounded"></div>
                <div class="h-4 w-28 bg-gray-200 rounded"></div>
                <div class="h-4 w-40 bg-gray-200 rounded"></div>
              </div>
            </td>
          </tr>
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

  // Auth guard
  const token = localStorage.getItem('auth_token');
  if (!token) window.location.replace(ctx + '/login.jsp');
  function guardRole(){
    const role = (localStorage.getItem('auth_role') || '').toLowerCase();
    if (role !== 'shipper') window.location.replace(ctx + '/login.jsp');
  }
  guardRole();

  // Helpers
  function showSkeleton(show = true){
    const tbody = document.getElementById('recent-shipments');
    if (show) {
      tbody.innerHTML = `
        <tr class="skeleton">
          <td colspan="6" class="px-3 py-6">
            <div class="animate-pulse flex items-center gap-3">
              <div class="h-4 w-24 bg-gray-200 rounded"></div>
              <div class="h-4 w-32 bg-gray-200 rounded"></div>
              <div class="h-4 w-20 bg-gray-200 rounded"></div>
              <div class="h-4 w-28 bg-gray-200 rounded"></div>
              <div class="h-4 w-40 bg-gray-200 rounded"></div>
            </div>
          </td>
        </tr>`;
    } else {
      tbody.innerHTML = '';
    }
  }
  async function authFetch(url, opt = {}) {
    const headers = new Headers(opt.headers || {});
    headers.set('Authorization', 'Bearer ' + token);
    const res = await fetch(url, { ...opt, headers });
    const ct = res.headers.get('content-type') || '';
    if (!res.ok) {
      if (res.status === 401) { localStorage.clear(); window.location.replace(ctx + '/login.jsp'); return; }
      const body = await res.text();
      throw new Error(`HTTP ${res.status} – ${ct.includes('json') ? body : body.slice(0, 120)}`);
    }
    return ct.includes('json') ? res.json() : res.text();
  }
  function timeago(date){
    if (!date) return '-';
    const d = new Date(date);
    const s = Math.floor((Date.now() - d.getTime())/1000);
    if (s < 60) return 'vừa xong';
    const m = Math.floor(s/60);
    if (m < 60) return m + ' phút trước';
    const h = Math.floor(m/60);
    if (h < 24) return h + ' giờ trước';
    const dd = Math.floor(h/24);
    if (dd < 7) return dd + ' ngày trước';
    return d.toLocaleString('vi-VN');
  }
  function parseJwt(token){
    try{
      const p = token.split('.')[1];
      const json = atob(p.replace(/-/g,'+').replace(/_/g,'/'));
      return JSON.parse(decodeURIComponent(escape(json)));
    }catch(e){ return {}; }
  }

  // Badge trạng thái
  function statusBadge(status){
    const s = (status||'').toUpperCase();
    let cls = 'bg-gray-100 text-gray-700';
    let label = status || '-';
    switch (s) {
      case 'ASSIGNED':         cls = 'bg-blue-100 text-blue-700';    label = 'Đã phân công'; break;
      case 'PICKED_UP':        cls = 'bg-amber-100 text-amber-700';  label = 'Đã lấy hàng'; break;
      case 'OUT_FOR_DELIVERY': cls = 'bg-amber-100 text-amber-700';  label = 'Đang giao hàng'; break;
      case 'DELIVERED':        cls = 'bg-green-100 text-green-700';  label = 'Giao thành công'; break;
      case 'FAILED_DELIVERY':  cls = 'bg-red-100 text-red-700';      label = 'Giao thất bại'; break;
      case 'RETURNING':        cls = 'bg-purple-100 text-purple-700';label = 'Đang hoàn hàng'; break;
      case 'RETURNED':         cls = 'bg-purple-100 text-purple-700';label = 'Đã hoàn hàng'; break;
      case 'CANCELLED':        cls = 'bg-gray-200 text-gray-700';    label = 'Đã hủy'; break;
      default:
        if (s === 'IN_TRANSIT') { cls = 'bg-amber-100 text-amber-700'; label = 'Đang vận chuyển'; break; }
        if (s === 'PENDING')    { cls = 'bg-gray-100 text-gray-700';  label = 'Chờ xử lý'; break; }
    }
    return `<span class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${cls}">${label}</span>`;
  }

  // Shipper info (tạm từ localStorage/JWT)
  function renderShipperInfo(stats){
    const nameLS  = localStorage.getItem('auth_user') || '';
    const emailLS = localStorage.getItem('auth_email') || '';
    const payload = parseJwt(localStorage.getItem('auth_token') || '');

    const username = (payload.sub || payload.username || nameLS || emailLS || 'shipper').toString();
    const email    = emailLS || payload.email || '';
    const display  = nameLS || payload.full_name || username;

    const init = (display || username || 'S').trim().charAt(0).toUpperCase();
    document.getElementById('shipperAvatar').textContent = init;

    document.getElementById('shipperFullname').textContent = display || '—';
    document.getElementById('shipperUsername').textContent = username ? ('@' + username) : '—';
    document.getElementById('shipperEmail').textContent    = email || '—';
    document.getElementById('shipperMeta').textContent     = 'Vai trò: Shipper • Cập nhật: ' + new Date().toLocaleString('vi-VN');

    // set các ô nhỏ bằng stats
    const rate = ((stats.successRate || 0) * 100).toFixed(0) + '%';
    document.getElementById('infoSuccessRate').textContent = rate;
    document.getElementById('infoInProgress').textContent  = stats.inProgress || 0;
    document.getElementById('infoDelivered').textContent   = stats.delivered || 0;
    document.getElementById('infoFailed').textContent      = stats.failed || 0;
  }

  // Nạp profile từ backend (users)
  async function loadProfile(){
    const prof = await authFetch(ctx + '/api/shipper/profile'); // cần endpoint này ở backend
    const display = (prof.fullName && prof.fullName.trim()) ? prof.fullName.trim()
                    : (prof.username || prof.email || 'S');
    const init = (display || 'S').trim().charAt(0).toUpperCase();

    document.getElementById('shipperAvatar').textContent   = init;
    document.getElementById('shipperFullname').textContent = display || '—';
    document.getElementById('shipperUsername').textContent = prof.username ? ('@' + prof.username) : '—';
    document.getElementById('shipperEmail').textContent    = prof.email || '—';
    document.getElementById('shipperPhone').textContent    = prof.phone || '—';
  }

  // Chart (có màu, hỗ trợ dark-mode)
  let chart;
  function renderChart(stats){
    const el = document.getElementById('chart-success');
    if (chart) chart.destroy();
    const isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    const legendColor = isDark ? '#e5e7eb' : '#374151';
    const ringBorder = isDark ? '#111827' : '#ffffff';
    chart = new Chart(el, {
      type: 'doughnut',
      data: {
        labels: ['Thành công', 'Thất bại', 'Đang giao'],
        datasets: [{
          backgroundColor: ['#22c55e', '#ef4444', '#f59e0b'],
          hoverBackgroundColor: ['#16a34a', '#dc2626', '#d97706'],
          borderColor: ringBorder,
          borderWidth: 2,
          data: [stats.delivered || 0, stats.failed || 0, stats.inProgress || 0]
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '62%',
        plugins: {
          legend: { position: 'top', labels: { color: legendColor, boxWidth: 14, boxHeight: 10 } },
          tooltip: { enabled: true }
        }
      }
    });
    const total = (stats.delivered||0) + (stats.failed||0) + (stats.inProgress||0);
    document.getElementById('miniSubtitle').textContent = total ? (total + ' vận đơn') : '';
  }

  // Load & Render
  const apiBase = ctx + '/api/shipper';
  async function reload(){
    try{
      document.getElementById('err').textContent = '';
      showSkeleton(true);

      const stats = await authFetch(apiBase + '/stats');

      renderChart(stats);
      renderShipperInfo(stats); // fill ô nhỏ bằng stats
      await loadProfile();      // điền full_name/phone/email từ DB

      const list = await authFetch(`${apiBase}/shipments?status=all&page=1&size=10`);
      const tbody = document.getElementById('recent-shipments');
      const items = list.items || [];

      showSkeleton(false);
      if (items.length === 0) {
        tbody.insertAdjacentHTML('beforeend',
          `<tr><td colspan="6" class="px-3 py-4 text-center text-gray-500">Không có vận đơn nào.</td></tr>`);
      } else {
        items.forEach(it=>{
          const lastRaw = (it.lastUpdateAt || it.last_update_at || '').toString();
          const last = lastRaw ? timeago(lastRaw) : '-';
          const badge = statusBadge(it.status);
          tbody.insertAdjacentHTML('beforeend', `
            <tr class="hover:bg-gray-50">
              <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">#${it.id}</td>
              <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${it.orderCode||'-'}</td>
              <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${it.receiverName||it.customerName||'-'}</td>
              <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${badge}</td>
              <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">${last}</td>
              <td class="px-3 py-2 whitespace-nowrap text-sm text-gray-700">
                <button class="inline-flex items-center px-3 py-2 rounded-md border border-amber-700 bg-amber-700 text-white hover:bg-amber-600 text-sm"
                        onclick="location.href='${ctx}/shipment-detail.jsp?id=${it.id}'">Chi tiết</button>
              </td>
            </tr>`);
        });
      }
    }catch(e){
      document.getElementById('err').textContent = e.message;
      showSkeleton(false);
    }
  }

  // Logout
  document.getElementById('logout').onclick = () => { localStorage.clear(); window.location.replace(ctx + '/login.jsp'); };

  reload();
</script>
</body>
</html>
