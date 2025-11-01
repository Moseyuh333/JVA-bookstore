<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page isELIgnored="true" %>
<%@ page import="java.net.URLEncoder" %>
<%
  String ctx = request.getContextPath();
  String sid = request.getParameter("id")==null?"":request.getParameter("id");

  String pPage   = request.getParameter("page");   if (pPage==null || pPage.isEmpty())   pPage = "1";
  String pSize   = request.getParameter("size");   if (pSize==null || pSize.isEmpty())   pSize = "10";
  String pStatus = request.getParameter("status"); if (pStatus==null || pStatus.isEmpty()) pStatus = "all";
  String pQ      = request.getParameter("q");      if (pQ==null) pQ = "";

  String qs = "page="+URLEncoder.encode(pPage,"UTF-8")
            + "&size="+URLEncoder.encode(pSize,"UTF-8")
            + "&status="+URLEncoder.encode(pStatus,"UTF-8")
            + "&q="+URLEncoder.encode(pQ,"UTF-8");

  String ref = request.getHeader("referer");
  String listUrl = ctx + "/shipments.jsp?" + qs;
  String backHref = (ref!=null && ref.contains("/shipments.jsp")) ? ref : listUrl;
%>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Chi tiết vận đơn #<%=sid%></title>
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="https://unpkg.com/feather-icons"></script>
</head>
<body class="bg-gray-50 text-gray-800 min-h-screen flex flex-col">
<main class="flex-grow">
<div class="container mx-auto px-4 py-8">

  <div class="flex items-center justify-between mb-6">
    <h1 class="text-2xl font-semibold">Vận đơn #<span id="ship-id"><%=sid%></span></h1>
    <div class="flex items-center gap-2">
      <a href="<%=backHref%>" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
        « Quay lại danh sách
      </a>
      <a href="<%=ctx%>/dashboard-shipper.jsp" class="inline-flex items-center px-3 py-2 rounded-md border border-gray-300 bg-white text-gray-700 hover:bg-gray-50 text-sm">
        Trang chủ
      </a>
    </div>
  </div>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
    <!-- ThÃ´ng tin -->
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
          <ul id="events" class="space-y-3"></ul>
          <p id="err" class="text-sm text-red-600 mt-3"></p>
        </div>
      </div>
    </div>

    <!-- Cáº­p nháº­t -->
    <div class="space-y-6">
      <div class="rounded-xl border border-amber-200 bg-white shadow-sm">
        <div class="px-4 py-3 border-b border-amber-100">
          <h2 class="text-lg font-medium">Cập nhật trạng thái</h2>
        </div>
        <div class="p-4 space-y-4">
          <!-- 8 tráº¡ng thÃ¡i chuáº©n -->
          <select id="evt-status" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full">

            <option value="ASSIGNED">Đã phân công</option>
            <option value="PICKED_UP">Đã lấy hàng</option>

            <option value="OUT_FOR_DELIVERY">Đang giao</option>

            <option value="FAILED_DELIVERY">Giao thất bại</option>
            <option value="RETURNING">Đang hoàn</option>
            <option value="RETURNED">Đã hoàn</option>
            <option value="CANCELLED">Huỷ đơn</option>
          </select>

          <input id="evt-note" type="text" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full" placeholder="Ghi chú (tùy chọn)">

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
                <div class="p-4 space-y-4">
          <div class="space-y-2">
            <div id="currentEvidence" class="hidden bg-emerald-50 border border-emerald-200 text-emerald-700 px-3 py-2 rounded-md text-sm flex items-center justify-between gap-2">
              <span>Minh chứng đã gửi:</span>
              <a id="currentEvidenceLink" href="#" target="_blank" class="inline-flex items-center gap-1 text-emerald-900 hover:underline">
                <i data-feather="external-link" class="w-4 h-4"></i>
                <span>Xem ảnh</span>
              </a>
            </div>
            <label class="block text-sm font-medium text-gray-700" for="proofFile">Ảnh minh chứng *</label>
            <input id="proofFile" type="file" accept="image/*" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full bg-white focus:border-amber-500 focus:ring-amber-500">
            <div id="proofPreview" class="hidden space-y-2">
              <img id="proofPreviewImg" src="" alt="Xem trước ảnh minh chứng" class="max-h-64 rounded-md border border-gray-200 object-contain">
              <button type="button" id="proofRemove" class="inline-flex items-center gap-2 px-3 py-2 rounded-md border border-gray-300 text-sm text-gray-700 hover:bg-gray-100">
                <i data-feather="trash-2" class="w-4 h-4"></i>
                <span>Xóa ảnh vừa chọn</span>
              </button>
            </div>
            <p class="text-xs text-gray-500">Hỗ trợ JPG, PNG, GIF, WebP (tối đa 5MB).</p>
          </div>
          <div class="space-y-2">
            <label class="block text-sm font-medium text-gray-700" for="deliverNote">Ghi chú cho khách (tùy chọn)</label>
            <textarea id="deliverNote" class="border border-gray-300 rounded-md px-3 py-2 text-sm w-full focus:border-amber-500 focus:ring-amber-500" rows="3" placeholder="Ví dụ: Đã giao cho bảo vệ tòa nhà."></textarea>
          </div>
          <label class="flex items-center gap-2">
            <input id="codCollected" type="checkbox" class="rounded border-gray-300 text-amber-600 focus:ring-amber-500">
            <span>Đã thu COD</span>
          </label>
          <button id="btnDeliver" class="inline-flex items-center justify-center gap-2 px-3 py-2 rounded-md border border-red-600 bg-red-600 text-white hover:bg-red-500 text-sm w-full transition disabled:opacity-60 disabled:cursor-not-allowed">
            <i data-feather="check-circle" class="w-4 h-4"></i>
            <span>Đánh dấu DELIVERED</span>
          </button>
        </div>
      </div>
    </div>
  </div>

</div>

<script>feather.replace();</script>

<script>
  const ctx = '<%=ctx%>';
  const id = '<%=sid%>';

  function guardRole(){
    const role = localStorage.getItem('auth_role')||'';
    if(role.toLowerCase()!=='shipper'){ location.href = ctx+'/login.jsp'; }
  }
  guardRole();

  async function authFetch(url,opt={}) {
    const token = localStorage.getItem('auth_token')||'';
    const headers = new Headers(opt.headers||{});
    if (token) headers.set('Authorization','Bearer '+token);
    const r = await fetch(url,{...opt, headers});
    if (r.status === 401) {
      localStorage.removeItem('auth_token');
      location.href = ctx + '/login.jsp';
      throw new Error('Unauthorized');
    }
    return r;
  }
  async function fetchJson(url,opt={}){
    const r = await authFetch(url,opt);
    const text = await r.text();
    if(!r.ok) throw new Error(text || ('HTTP '+r.status));
    try { return JSON.parse(text); } catch { return {}; }
  }

  const apiBase = ctx + '/api/shipper';
  const evidenceEls = {
    input: document.getElementById('proofFile'),
    preview: document.getElementById('proofPreview'),
    previewImg: document.getElementById('proofPreviewImg'),
    remove: document.getElementById('proofRemove'),
    current: document.getElementById('currentEvidence'),
    currentLink: document.getElementById('currentEvidenceLink')
  };
  let previewObjectUrl = null;

  function resetEvidenceSelection(clearInput){
    if(clearInput && evidenceEls.input){
      evidenceEls.input.value = '';
    }
    if(previewObjectUrl && window.URL && typeof URL.revokeObjectURL === 'function') {
      URL.revokeObjectURL(previewObjectUrl);
    }
    previewObjectUrl = null;
    if(evidenceEls.preview){
      evidenceEls.preview.classList.add('hidden');
    }
    if(evidenceEls.previewImg){
      evidenceEls.previewImg.src = '';
    }
  }

  function updateEvidencePreview(){
    if(!evidenceEls.input) return;
    const file = evidenceEls.input.files && evidenceEls.input.files[0];
    if(!file){
      resetEvidenceSelection(false);
      return;
    }
    if(previewObjectUrl && window.URL && typeof URL.revokeObjectURL === 'function'){
      URL.revokeObjectURL(previewObjectUrl);
    }
    previewObjectUrl = null;
    if(window.URL && typeof URL.createObjectURL === 'function'){
      previewObjectUrl = URL.createObjectURL(file);
    }
    if(evidenceEls.previewImg){
      if(previewObjectUrl){
        evidenceEls.previewImg.src = previewObjectUrl;
      } else {
        evidenceEls.previewImg.removeAttribute('src');
      }
    }
    if(evidenceEls.preview){
      if(previewObjectUrl){
        evidenceEls.preview.classList.remove('hidden');
      } else {
        evidenceEls.preview.classList.add('hidden');
      }
    }
    if(window.feather && typeof window.feather.replace === 'function'){
      window.feather.replace();
    }
  }

  if(evidenceEls.input){
    evidenceEls.input.addEventListener('change', updateEvidencePreview);
  }
  if(evidenceEls.remove){
    evidenceEls.remove.addEventListener('click', function(){
      resetEvidenceSelection(true);
    });
  }

  function setCurrentEvidence(url){
    const href = resolveEvidenceUrl(url);
    if(!href){
      if(evidenceEls.current) evidenceEls.current.classList.add('hidden');
      if(evidenceEls.currentLink) evidenceEls.currentLink.removeAttribute('href');
      return;
    }
    if(evidenceEls.currentLink){
      evidenceEls.currentLink.href = href;
    }
    if(evidenceEls.current){
      evidenceEls.current.classList.remove('hidden');
    }
  }

  function setBadge(status){
    const el = document.getElementById('status-badge');
    if(!el) return;
    el.textContent = status||'-';
  }

  // Chá»n tráº¡ng thÃ¡i káº¿ tiáº¿p theo luá»“ng
  function setNextStatus(currentStatus){
    const flow = [
      'ASSIGNED',
      'PICKED_UP',
      'OUT_FOR_DELIVERY',
      'FAILED_DELIVERY',
      'RETURNING',
      'RETURNED',
      'CANCELLED'
    ];
    const sel = document.getElementById('evt-status');
    if(!sel) return;
    const i = flow.indexOf(currentStatus||'');
    sel.value = (i>=0 && i+1<flow.length) ? flow[i+1] : (currentStatus||'ASSIGNED');
  }

  function resolveEvidenceUrl(url){
    if(!url) return '';
    const raw = String(url).trim();
    if(!raw) return '';
    if(/^https?:\/\//i.test(raw)) return raw;
    if(ctx && raw.startsWith(ctx + '/')) return raw;
    if(raw.startsWith('/')){
      return ctx ? (ctx + raw) : raw;
    }
    const normalized = raw.replace(/^\/+/, '');
    if(!ctx){
      return '/' + normalized;
    }
    return ctx + '/' + normalized;
  }

  async function load(){
    try{
      // API tráº£ { shipment, events } hoáº·c gá»™p
      const d = await fetchJson(apiBase + '/shipments/' + encodeURIComponent(id));
      const s = d.shipment ?? d;
      const events = d.events ?? [];

      // ThÃ´ng tin
      document.getElementById('order-code').textContent = s.orderCode || '-';
      document.getElementById('receiver-name').textContent = s.receiverName || s.customerName || '-';
      document.getElementById('receiver-phone').textContent = s.receiverPhone || '-';
      document.getElementById('receiver-address').textContent = s.receiverAddress || '-';
      document.getElementById('cod-amount').textContent = ((s.codAmount||0).toLocaleString('vi-VN')) + ' VND';
      setBadge(s.status);
      setCurrentEvidence(s.evidenceUrl);
      if (!['DELIVERED','FAILED_DELIVERY','CANCELLED','RETURNED'].includes(s.status))
          setNextStatus(s.status);
      else
          document.getElementById('evt-status').value = s.status;
      // Timeline
      const ul = document.getElementById('events');
      ul.innerHTML = '';
      (events||[]).forEach(function(e){
        const t = e.createdAt ? new Date(e.createdAt).toLocaleString('vi-VN') : '-';
        const evidenceHref = resolveEvidenceUrl(e.evidenceUrl);
        ul.insertAdjacentHTML('beforeend',
          '<li class="rounded-lg border border-gray-200 bg-white px-3 py-2">' +
            '<div class="flex items-center justify-between">' +
              '<span class="text-sm font-medium">' + (e.status||'-') + '</span>' +
              '<span class="text-xs text-gray-500">' + t + '</span>' +
            '</div>' +
            (e.note ? '<div class="text-sm text-gray-700 mt-1">'+ e.note +'</div>' : '') +
            (evidenceHref ? '<a class="text-sm text-amber-700 hover:underline" href="'+ evidenceHref +'" target="_blank">Xem bằng chứng</a>' : '') +
          '</li>'
        );
      });
      document.getElementById('err').textContent = '';
    }catch(e){
      document.getElementById('err').textContent = e.message || 'Lá»—i táº£i dá»¯ liá»‡u';
    }
  }

  // actions
  document.getElementById('btnAddEvent').onclick = async function(){
    try{
      const status = document.getElementById('evt-status').value;
      const note = document.getElementById('evt-note').value;
      await fetchJson(apiBase + '/shipments/' + encodeURIComponent(id) + '/events', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify({ status: status, note: note })
      });
      await load();
    }catch(e){
      alert('Lá»—i khi cáº­p nháº­t sá»± kiá»‡n: ' + (e.message || e));
    }
  };

  const btnDeliver = document.getElementById('btnDeliver');
  if(btnDeliver){
    btnDeliver.addEventListener('click', async function(){
      if(!evidenceEls.input || !evidenceEls.input.files || evidenceEls.input.files.length === 0){
        alert('Vui lòng chọn ảnh minh chứng trước khi xác nhận.');
        return;
      }
      const file = evidenceEls.input.files[0];
      const codCheckbox = document.getElementById('codCollected');
      const noteEl = document.getElementById('deliverNote');
      const formData = new FormData();
      formData.append('evidence', file);
      formData.append('codCollected', codCheckbox && codCheckbox.checked ? 'true' : 'false');
      if(noteEl){
        const noteValue = noteEl.value.trim();
        if(noteValue){ formData.append('note', noteValue); }
      }
      btnDeliver.disabled = true;
      try{
        await fetchJson(apiBase + '/shipments/' + encodeURIComponent(id) + '/deliver', {
          method:'POST',
          body: formData
        });
        resetEvidenceSelection(true);
        if(codCheckbox){ codCheckbox.checked = false; }
        if(noteEl){ noteEl.value = ''; }
        await load();
      }catch(e){
        alert('Lỗi khi đánh dấu giao hàng: ' + (e.message || e));
      }finally{
        btnDeliver.disabled = false;
      }
    });
  }
  load();
</script>
</main>
<%@ include file="/WEB-INF/includes/footer.jsp" %>
</body>

</html>






