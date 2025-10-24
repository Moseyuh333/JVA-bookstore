<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="true" %>
<%
  String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Danh sách vận đơn</title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- Đồng bộ vibe trang chủ, không dùng header.jsp -->
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="bg-gray-50 text-gray-800 min-h-screen">
<div class="container mx-auto px-4 py-8">

  <div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-semibold">Vận đơn của tôi</h1>
    <div class="flex items-center gap-2">
      <input id="fQuery" type="text" class="border border-gray-300 rounded-md px-3 py-2 text-sm" placeholder="Tìm mã/đơn hàng/khách...">
      <select id="fStatus" class="border border-gray-300 rounded-md px-3 py-2 text-sm">
        <option value="">Tất cả</option>
        <option value="ASSIGNED">Đã phân công</option>
        <option value="PICKED_UP">Đã lấy hàng</option>

        <option value="OUT_FOR_DELIVERY">Đang giao</option>
        <option value="DELIVERED">Đã giao</option>
        <option value="FAILED_DELIVERY">Giao thất bại</option>
        <option value="RETURNING">Đang hoàn</option>
        <option value="RETURNED">Đã hoàn</option>
      </select>
      <button id="btnFilter" class="inline-flex items-center px-3 py-2 rounded-md border border-amber-700 bg-amber-700 text-white hover:bg-amber-600 text-sm">
        <i data-feather="filter" class="w-4 h-4 mr-2"></i>Lọc
      </button>
    </div>
  </div>

  <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
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
        <tbody id="shipments-body">
          <!-- JS render -->
        </tbody>
      </table>

      <div class="mt-4 flex items-center justify-between">
        <button id="prevPage" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
          « Trước
        </button>
        <div id="pageInfo" class="text-sm text-gray-500">Trang 1</div>
        <button id="nextPage" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
          Sau »
        </button>
      </div>
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
    if(role.toLowerCase()!=='shipper'){ location.href = ctx+'/login.jsp'; }
  }
  guardRole();

  async function authFetch(url,opt={}){
    const token = localStorage.getItem('auth_token')||'';
    const headers = new Headers(opt.headers||{});
    if (token) headers.set('Authorization','Bearer '+token);
    const res = await fetch(url,{...opt, headers});
    if (res.status === 401) {
      localStorage.removeItem('auth_token');
      location.href = ctx + '/login.jsp';
      throw new Error('Unauthorized');
    }
    const ct = res.headers.get('content-type')||'';
    if (!res.ok) {
      const body = await res.text();
      throw new Error('HTTP '+res.status+' – ' + (ct.includes('json')? body : ('Non-JSON: ' + body.slice(0,120))));
    }
    if (ct.includes('json')) return res.json();
    const txt = await res.text();
    throw new Error('Non-JSON response: ' + txt.slice(0,120));
  }

  const apiBase = ctx + '/api/shipper';
  let page=1, size=10;

  function esc(s){
    return String(s==null? '': s).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[m]));
  }
  function first(...xs){
    for (const x of xs){ if (x!==undefined && x!==null && x!=='') return x; }
    return null;
  }
  // 👉 định dạng đúng mẫu: HH:mm:ss dd/MM/yyyy
  function fmtTime(iso){
    if(!iso) return '-';
    const d = new Date(iso);
    if (isNaN(+d)) return '-';
    const pad = n => String(n).padStart(2,'0');
    const HH = pad(d.getHours()), mm = pad(d.getMinutes()), ss = pad(d.getSeconds());
    const dd = pad(d.getDate()), MM = pad(d.getMonth()+1), yyyy = d.getFullYear();
    return `${HH}:${mm}:${ss} ${dd}/${MM}/${yyyy}`;
  }

  // màu + NHÃN TIẾNG VIỆT cho badge (giống ảnh mẫu)
  const statusStyle = {
    'ASSIGNED':         {cls:'bg-slate-700 text-blue-200', label:'Đã phân công'},
    'PICKED_UP':        {cls:'bg-blue-700 text-white',     label:'Đã lấy hàng'},

    'OUT_FOR_DELIVERY': {cls:'bg-amber-600 text-white',    label:'Đang giao hàng'},
    'DELIVERED':        {cls:'bg-emerald-700 text-white',  label:'Giao thành công'},
    'FAILED_DELIVERY':  {cls:'bg-rose-700 text-white',     label:'Giao thất bại'},
    'RETURNING':        {cls:'bg-yellow-700 text-white',   label:'Đang hoàn'},
    'RETURNED':         {cls:'bg-yellow-800 text-white',   label:'Đã hoàn'},
    'CANCELLED':        {cls:'bg-slate-600 text-white',    label:'Huỷ đơn'}
  };

  async function load(){
    try{
      const status = document.getElementById('fStatus').value||'';
      const q = (document.getElementById('fQuery').value||'').trim();
      const url = new URL(apiBase + '/shipments', location.origin);
      url.searchParams.set('page', page);
      url.searchParams.set('size', size);
      if (status) url.searchParams.set('status', status);
      if (q) url.searchParams.set('q', q);

      const data = await authFetch(url.toString());
      const tbody = document.getElementById('shipments-body');
      tbody.innerHTML = '';

      (data.items||[]).forEach(function(it){
        // thời điểm cập nhật gần nhất
        const lastIso = first(
          it.lastEventAt, it.last_event_at, it.eventLastAt, it.event_last_at,
          it.lastUpdateAt, it.last_update_at, it.updatedAt, it.updated_at,
          it.createdAt, it.created_at
        );
        const last = fmtTime(lastIso);

        const stKey = (it.status||'').toUpperCase();
        const meta = statusStyle[stKey] || {cls:'bg-gray-600 text-gray-100', label:(it.status||'-')};
        const badge =
          '<span class="inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium '+meta.cls+'">' +
          esc(meta.label) + '</span>';

        const params = new URLSearchParams({
          id: String(it.id),
          page: String(page),
          size: String(size),
          status: status || 'all',
          q: q
        }).toString();
        const detailHref = ctx + '/shipment-detail.jsp?' + params;

        const row =
      '<tr class="hover:bg-gray-800/40">' +
        '<td class="px-3 py-2 whitespace-nowrap text-sm">' + esc(it.id) + '</td>' +
        '<td class="px-3 py-2 whitespace-nowrap text-sm">' + esc(it.orderCode||'-') + '</td>' +
        '<td class="px-3 py-2 whitespace-nowrap text-sm">' + esc(it.receiverName||'-') + '</td>' +
        '<td class="px-3 py-2 whitespace-nowrap text-sm">' + badge + '</td>' +
        '<td class="px-3 py-2 whitespace-nowrap text-sm">' + esc(last) + '</td>' +
        '<td class="px-3 py-2 whitespace-nowrap text-sm">' +
          '<button class="inline-flex items-center px-3 py-2 rounded-md border border-amber-700 bg-amber-700 text-white hover:bg-amber-600 text-sm" ' +
                  'onclick="location.href=\'' + detailHref.replace(/'/g,'&#39;') + '\'">' +
            'Chi tiết' +
          '</button>' +
        '</td>' +
      '</tr>';
        tbody.insertAdjacentHTML('beforeend', row);
      });

      document.getElementById('pageInfo').textContent = 'Trang ' + (data.page||page);
      document.getElementById('prevPage').disabled = (data.page||page) <= 1;
      document.getElementById('nextPage').disabled = (data.items||[]).length < size;

      document.getElementById('err').textContent = '';
    }catch(e){
      document.getElementById('err').textContent = e.message;
    }
  }

  document.getElementById('btnFilter').onclick = ()=>{ page=1; load(); };
  document.getElementById('prevPage').onclick = ()=>{ if(page>1){ page--; load(); } };
  document.getElementById('nextPage').onclick = ()=>{ page++; load(); };

  load();
</script>
</body>
</html>
