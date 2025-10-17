<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<footer class="bg-gray-900 text-gray-300 mt-20">
  <div class="container mx-auto px-4 py-12">

    <!-- Grid nội dung footer -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-8 mb-10">
      <div>
        <h3 class="title-font text-white text-xl font-bold mb-4">Bookish Bliss Haven</h3>
        <p class="mb-4">Nguồn sách chất lượng và cảm hứng văn học đáng tin cậy của bạn.</p>
        <div class="flex space-x-4">
          <a href="#" class="hover:text-white"><i data-feather="facebook" class="w-5 h-5"></i></a>
          <a href="#" class="hover:text-white"><i data-feather="twitter" class="w-5 h-5"></i></a>
          <a href="#" class="hover:text-white"><i data-feather="instagram" class="w-5 h-5"></i></a>
        </div>
      </div>

      <div>
        <h4 class="text-white font-bold mb-4">Mua sắm</h4>
        <ul class="space-y-2">
          <li><a href="#" class="hover:text-white">Sách mới</a></li>
          <li><a href="#" class="hover:text-white">Sách bán chạy</a></li>
          <li><a href="#" class="hover:text-white">Tiểu thuyết</a></li>
          <li><a href="#" class="hover:text-white">Phi tiểu thuyết</a></li>
          <li><a href="#" class="hover:text-white">Thẻ quà tặng</a></li>
        </ul>
      </div>

      <div>
        <h4 class="text-white font-bold mb-4">Hỗ trợ</h4>
        <ul class="space-y-2">
          <li><a href="#" class="hover:text-white">Câu hỏi thường gặp</a></li>
          <li><a href="#" class="hover:text-white">Vận chuyển</a></li>
          <li><a href="#" class="hover:text-white">Đổi trả</a></li>
          <li><a href="#" class="hover:text-white">Liên hệ</a></li>
          <li><a href="#" class="hover:text-white">Chính sách bảo mật</a></li>
        </ul>
      </div>

      <div>
        <h4 class="text-white font-bold mb-4">Liên hệ</h4>
        <address class="not-italic space-y-2">
          <div class="flex items-start">
            <i data-feather="map-pin" class="w-5 h-5 mr-2 mt-0.5"></i>
            <span>123 Đường Văn Học, Quận Sách, TP.HCM</span>
          </div>
          <div class="flex items-center">
            <i data-feather="mail" class="w-5 h-5 mr-2"></i>
            <a href="mailto:info@bookishhaven.com" class="hover:text-white">info@bookishhaven.com</a>
          </div>
          <div class="flex items-center">
            <i data-feather="phone" class="w-5 h-5 mr-2"></i>
            <a href="tel:+84901234567" class="hover:text-white">0901 234 567</a>
          </div>
        </address>
      </div>
    </div>

    <!-- Dòng bản quyền -->
    <div class="border-t border-gray-700 pt-6 text-center">
      <span class="inline-flex items-center justify-center gap-2 bg-gray-800 text-amber-300 px-5 py-2 rounded-full text-base font-medium shadow-md">
        <i data-feather="shield" class="w-5 h-5"></i>
        <span>&copy; <span id="year"></span> Bookish Bliss Haven · Mọi quyền được bảo lưu</span>
      </span>
    </div>
  </div>
</footer>

<script>
  feather.replace();
  const y = document.getElementById('year');
  if (y) y.textContent = new Date().getFullYear();
</script>
