<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
  <meta charset="UTF-8">
  <title>Thông tin cửa hàng</title>
  <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">
</head>
<body class="bg-amber-50 min-h-screen flex items-center justify-center">
  <div class="bg-white p-10 rounded-2xl shadow-2xl w-full max-w-2xl space-y-6">
    <div class="flex items-center gap-6">
      <img src="${logoUrl}" alt="Logo" class="w-24 h-24 rounded-full border border-amber-300">
      <div>
        <h1 class="text-2xl font-bold text-amber-800">${shopName}</h1>
        <p class="text-gray-600">${description}</p>
      </div>
    </div>

    <div class="border-t border-amber-100 pt-4 space-y-2 text-sm">
      <p><strong>Mã cửa hàng:</strong> ${shopId}</p>
      <p><strong>Trạng thái:</strong> ${status}</p>
      <p><strong>Ngày tạo:</strong> ${createdAt}</p>
      <p><strong>Phần trăm hoa hồng:</strong> ${commission}%</p>
    </div>

    <div class="text-center mt-6">
      <a href="<%= request.getContextPath() %>/Seller/sellerDashboard.jsp" 
         class="bg-amber-700 text-white px-5 py-2 rounded-lg hover:bg-amber-800">
         ⬅ Quay lại Dashboard
      </a>
    </div>
  </div>
</body>
</html>
