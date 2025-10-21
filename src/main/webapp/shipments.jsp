<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<%@ include file="/WEB-INF/includes/header.jsp" %>

<main class="min-h-screen bg-gradient-to-br from-amber-900/15 via-amber-800/20 to-amber-950/30 pb-20">
  <section class="bg-white/90 backdrop-blur-sm border-b border-amber-100/70">
    <div class="container mx-auto max-w-6xl px-4">
      <div class="flex items-center justify-between py-4">
        <div class="flex items-center gap-3">
          <span class="inline-flex items-center justify-center w-9 h-9 rounded-xl bg-amber-100 text-amber-700">
            <i data-feather="list" class="w-5 h-5"></i>
          </span>
          <div>
            <h1 class="text-xl font-bold text-amber-800 m-0">Đơn được phân công</h1>
            <p class="text-sm text-gray-500">Quản lý toàn bộ vận đơn của bạn</p>
          </div>
        </div>
        <nav class="flex items-center gap-2">
          <a href="<%=ctx%>/dashboard-shipper.jsp" class="px-3 py-2 rounded-xl text-sm font-medium text-amber-800 hover:bg-amber-100">Dashboard</a>
          <a href="<%=ctx%>/shipments.jsp" class="px-3 py-2 rounded-xl text-sm font-medium bg-amber-700 text-white hover:bg-amber-800">Tất cả đơn</a>
        </nav>
      </div>
    </div>
  </section>

  <section class="container mx-auto max-w-6xl px-4">
    <!-- Filters -->
    <div class="bg-white/95 border border-amber-100 rounded-2xl p-4 shadow-sm mt-6 flex flex-wrap gap-3 items-center">
      <select id="fStatus" class="border rounded-xl px-3 py-2">
        <option value="">Tất cả trạng thái</option>
        <option value="ASSIGNED">ASSIGNED</option>
        <option value="PICKED_UP">PICKED_UP</option>
        <option value="IN_TRANSIT">IN_TRANSIT</option>
        <option value="OUT_FOR_DELIVERY">OUT_FOR_DELIVERY</option>
        <option value="DELIVERED">DELIVERED</option>
        <option value="FAILED_DELIVERY">FAILED_DELIVERY</option>
      </select>
      <input id="fQuery" class="border rounded-xl px-3 py-2" placeholder="Tìm theo mã đơn / order id" />
      <button class="px-3 py-2 rounded-xl bg-amber-100 text-amber-800 hover:bg-amber-200" onclick="loadShipments(1)">Lọc</button>
      <div id="err" class="text-sm text-red-600 ml-auto"></div>
    </div>

    <!-- Table -->
    <div class="bg-white/95 border border-amber-100 rounded-2xl p-4 shadow-sm mt-4">
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

      <div class="flex justify-between items-center mt-3">
        <button id="prev" class="px-3 py-1.5 rounded-xl bg-amber-50 text-amber-800 hover:bg-amber-100">« Trang trước</button>
        <div id="pageInfo" class="text-sm text-gray-600"></div>
        <button id="next" class="px-3 py-1.5 rounded-xl bg-amber-50 text-amber-800 hover:bg-amber-100">Trang sau »</button>
      </div>
    </div>
  </section>
</main>

<%@ include file="/WEB-INF/includes/footer.jsp" %>

<script>
  // auth agent
  const ctx = '<%=ctx%>';
  const TOKEN = localStorage.getItem('auth_token');
  const ROLE  = (localStorage.getItem('auth_role') || '').toLowerCase();
  if (!TOKEN || ROLE !== 'shipper') location.replace(ctx + '/login.jsp');

  async function authFetch(url, options = {}) {
    const headers = new Headers(options.headers || {});
    headers.set('Authorization', 'Bearer ' + TOKEN);
    headers.set('Accept', 'application/json');
    const res = await fetch(url, { ...options, headers });
    if (res.status === 401 || res.status === 403) { localStorage.clear(); location.replace(ctx + '/login.jsp'); }
    return res;
  }

  const apiBase = ctx + '/api/shipper';
  let page = 1, size = 20, totalPages = 1;

  async function loadShipments(p = 1) {
    page = p;
    document.getElementById('err').textContent = '';
    const st = document.getElementById('fStatus').value || '';
    const q  = encodeURIComponent((document.getElementById('fQuery').value || '').trim());
    try {
      const url = `${apiBase}/shipments?page=${page}&size=${size}&status=${st}&q=${q}`;
      const r = await authFetch(url);
      const data = await r.json();

      totalPages = data.totalPages || 1;
      document.getElementById('pageInfo').textContent = `Trang ${page}/${totalPages}`;

      const tb = document.getElementById('tblBody');
      tb.innerHTML = '';
      (data.items || []).forEach(it => {
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

  document.getElementById('prev').onclick = () => { if (page > 1) loadShipments(page - 1); };
  document.getElementById('next').onclick = () => { if (page < totalPages) loadShipments(page + 1); };
  document.addEventListener('DOMContentLoaded', () => loadShipments(1));
</script>
</html>
