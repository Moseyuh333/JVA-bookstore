<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="min-h-screen bg-gradient-to-br from-amber-900/15 via-amber-800/20 to-amber-950/30 pb-20">

  <!-- Shipper topbar (riêng, nhẹ hơn navbar chính) -->
  <section class="bg-white/90 backdrop-blur-sm border-b border-amber-100/70">
    <div class="container mx-auto max-w-6xl px-4">
      <div class="flex items-center justify-between py-4">
        <div class="flex items-center gap-3">
          <span class="inline-flex items-center justify-center w-9 h-9 rounded-xl bg-amber-100 text-amber-700">
            <i data-feather="truck" class="w-5 h-5"></i>
          </span>
          <div>
            <h1 class="text-xl font-bold text-amber-800 m-0">Shipper Dashboard</h1>
            <p class="text-sm text-gray-500">Tổng quan các đơn được phân công cho bạn</p>
          </div>
        </div>

        <nav class="flex items-center gap-2">
          <a href="<%=ctx%>/dashboard-shipper.jsp" class="px-3 py-2 rounded-xl text-sm font-medium bg-amber-700 text-white hover:bg-amber-800">Dashboard</a>
          <a href="<%=ctx%>/shipments.jsp" class="px-3 py-2 rounded-xl text-sm font-medium text-amber-800 hover:bg-amber-100">Tất cả đơn</a>
          <button id="btnLogout" class="px-3 py-2 rounded-xl text-sm font-medium text-red-700 hover:bg-red-50">Đăng xuất</button>
        </nav>
      </div>
    </div>
  </section>

  <!-- Stats cards -->
  <section class="container mx-auto max-w-6xl px-4">
    <div class="grid md:grid-cols-3 gap-4 mt-6">
      <div class="bg-white/95 border border-amber-100 rounded-2xl p-5 shadow-sm">
        <div class="text-sm text-gray-500">Đang giao</div>
        <div id="cInProgress" class="text-3xl font-bold text-amber-800 mt-1">0</div>
      </div>
      <div class="bg-white/95 border border-amber-100 rounded-2xl p-5 shadow-sm">
        <div class="text-sm text-gray-500">Đã giao</div>
        <div id="cDelivered" class="text-3xl font-bold text-amber-800 mt-1">0</div>
      </div>
      <div class="bg-white/95 border border-amber-100 rounded-2xl p-5 shadow-sm">
        <div class="text-sm text-gray-500">Thất bại/Hoàn</div>
        <div id="cFailed" class="text-3xl font-bold text-amber-800 mt-1">0</div>
      </div>
    </div>

    <!-- Chart + actions -->
    <div class="grid md:grid-cols-[1fr,320px] gap-4 mt-4">
      <div class="bg-white/95 border border-amber-100 rounded-2xl p-4 shadow-sm">
        <canvas id="pieChart" height="180"></canvas>
      </div>
      <div class="bg-white/95 border border-amber-100 rounded-2xl p-4 shadow-sm flex flex-col gap-3">
        <div class="font-semibold text-amber-900">Hành động nhanh</div>
        <a class="px-4 py-2 rounded-xl bg-amber-700 text-white font-medium hover:bg-amber-800 text-center"
           href="<%=ctx%>/shipments.jsp">Xem tất cả đơn được phân công</a>
        <button class="px-4 py-2 rounded-xl bg-amber-100 text-amber-800 font-medium hover:bg-amber-200 text-center"
                onclick="reloadData()">Tải lại số liệu</button>
        <p class="text-xs text-gray-500">* Số liệu theo user đăng nhập hiện tại.</p>
        <div id="err" class="text-sm text-red-600"></div>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-white/95 border border-amber-100 rounded-2xl p-4 shadow-sm mt-4">
      <div class="flex items-center justify-between mb-2">
        <div class="font-semibold text-amber-900">10 đơn cập nhật gần nhất</div>
        <a class="px-3 py-1.5 rounded-xl text-sm bg-amber-50 text-amber-800 hover:bg-amber-100"
           href="<%=ctx%>/shipments.jsp">Xem tất cả</a>
      </div>
      <div class="overflow-auto">
        <table class="w-full text-sm">
          <thead>
          <tr class="text-gray-500 border-b">
            <th class="text-left py-2 pr-3">#</th>
            <th class="text-left py-2 pr-3">Order ID</th>
            <th class="text-left py-2 pr-3">Trạng thái</th>
            <th class="text-left py-2 pr-3">COD</th>
            <th class="text-left py-2 pr-3">Cập nhật</th>
            <th class="text-left py-2"> </th>
          </tr>
          </thead>
          <tbody id="tblBody"></tbody>
        </table>
      </div>
    </div>
  </section>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<!-- Chart.js -->
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>

<script>
  // ====== AUTH AGENT (tự gắn JWT & bảo vệ trang) ======
  const ctx = '<%=ctx%>';
  const TOKEN = localStorage.getItem('auth_token');
  const ROLE  = (localStorage.getItem('auth_role') || '').toLowerCase();
  if (!TOKEN || ROLE !== 'shipper') {
    window.location.replace(ctx + '/login.jsp'); // bảo vệ route
  }
  async function authFetch(url, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set('Authorization', 'Bearer ' + TOKEN);
    headers.set('Accept', 'application/json');
    const res = await fetch(url, { ...options, headers });
    if (res.status === 401 || res.status === 403) {
      localStorage.clear();
      window.location.replace(ctx + '/login.jsp');
      throw new Error('Unauthorized');
    }
    return res;
  }
  document.getElementById('btnLogout').addEventListener('click', function () {
    localStorage.clear();
    window.location.href = ctx + '/login.jsp';
  });

  // ====== DATA + UI ======
  const apiBase = ctx + '/api/shipper';
  let pie;

  async function reloadData() {
    document.getElementById('err').textContent = '';
    try {
      // Stats
      const sRes = await authFetch(apiBase + '/stats');
      const st = await sRes.json();
      const inProgress = st.inProgress || 0, delivered = st.delivered || 0, failed = st.failed || 0;
      document.getElementById('cInProgress').textContent = inProgress;
      document.getElementById('cDelivered').textContent = delivered;
      document.getElementById('cFailed').textContent = failed;

      // Chart
      const data = { labels: ['Đang giao','Đã giao','Thất bại'],
        datasets: [{ data: [inProgress, delivered, failed] }] };
      if (pie) pie.destroy();
      pie = new Chart(document.getElementById('pieChart'), { type: 'pie', data });

      // Latest list
      const lRes = await authFetch(apiBase + '/shipments?page=1&size=10');
      const list = await lRes.json();
      const tb = document.getElementById('tblBody');
      tb.innerHTML = '';
      (list.items || []).forEach(it => {
        const last = (it.lastUpdateAt || '').replace('T',' ').slice(0,19);
        const tr = document.createElement('tr');
        tr.className = 'border-b last:border-0';
        tr.innerHTML = `
          <td class="py-2 pr-3">\${it.id ?? '-'}</td>
          <td class="py-2 pr-3">\${it.orderId ?? '-'}</td>
          <td class="py-2 pr-3"><span class="px-2 py-0.5 rounded-lg bg-amber-50 text-amber-800">\${it.status ?? '-'}</span></td>
          <td class="py-2 pr-3">\${(it.codAmount || 0).toLocaleString('vi-VN')} ₫</td>
          <td class="py-2 pr-3">\${last || '-'}</td>
          <td class="py-2">
            <button class="px-3 py-1.5 rounded-xl bg-emerald-600 text-white hover:bg-emerald-700"
              onclick="location.href='\${ctx}/shipment-detail.jsp?id=\${it.id}'">Chi tiết</button>
          </td>
        `;
        tb.appendChild(tr);
      });

    } catch (e) {
      document.getElementById('err').textContent = 'Lỗi tải: ' + (e.message || e);
    }
  }
  reloadData();
</script>
</html>
