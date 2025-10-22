<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Hồ sơ cửa hàng</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/seller.css">
    <script src="https://unpkg.com/feather-icons"></script>
</head>
<body>
    <jsp:include page="/WEB-INF/includes/seller-header.jsp" />

    <main class="seller-container">
        <h1 class="page-title"><i data-feather="user"></i> Hồ sơ cửa hàng</h1>

        <section id="storeInfo" class="info-section">
            <div class="loading">Đang tải thông tin cửa hàng...</div>
        </section>

        <section id="editStore" class="edit-section" style="display:none;">
            <h2>Chỉnh sửa thông tin cửa hàng</h2>
            <form id="editForm">
                <label>Tên cửa hàng</label>
                <input type="text" id="storeName" name="storeName" required />

                <label>Mô tả</label>
                <textarea id="storeDesc" name="storeDesc"></textarea>

                <label>Ảnh đại diện</label>
                <input type="file" id="avatar" accept="image/*" />

                <label>Ảnh bìa</label>
                <input type="file" id="cover" accept="image/*" />

                <button type="submit">Lưu thay đổi</button>
            </form>
        </section>
    </main>

    <script>
        feather.replace();

        const contextPath = '${pageContext.request.contextPath}';
        const token = localStorage.getItem('auth_token'); // token khi login

        async function loadStoreInfo() {
            const container = document.getElementById('storeInfo');
            try {
                const res = await fetch(contextPath + '/api/vendor/stores', {
                    headers: { 'Authorization': 'Bearer ' + token }
                });

                const data = await res.json();
                if (!res.ok) {
                    container.innerHTML = '<p class="error">Lỗi: ' + (data.error || 'Không thể tải dữ liệu') + '</p>';
                    return;
                }

                if (data.length === 0) {
                    container.innerHTML = '<p>Chưa có cửa hàng nào được liên kết với tài khoản này.</p>';
                    return;
                }

                const shop = data[0]; // mỗi seller có 1 shop
                container.innerHTML = `
                    <div class="store-card">
                        <img src="${shop.cover_image || contextPath + '/assets/images/default-cover.jpg'}" class="cover"/>
                        <div class="store-details">
                            <img src="${shop.avatar || contextPath + '/assets/images/default-avatar.png'}" class="avatar"/>
                            <h2>${shop.name}</h2>
                            <p>${shop.description || 'Chưa có mô tả'}</p>
                            <button id="btnEdit">Chỉnh sửa</button>
                        </div>
                    </div>
                `;

                document.getElementById('btnEdit').addEventListener('click', () => {
                    document.getElementById('editStore').style.display = 'block';
                    document.getElementById('storeName').value = shop.name;
                    document.getElementById('storeDesc').value = shop.description || '';
                });
            } catch (e) {
                console.error(e);
                container.innerHTML = '<p class="error">Không thể kết nối máy chủ.</p>';
            }
        }

        loadStoreInfo();
    </script>

    <style>
        .seller-container { padding: 30px; font-family: Arial, sans-serif; }
        .page-title { display:flex; align-items:center; gap:10px; color:#333; margin-bottom:20px; }
        .store-card { background:#fff; border-radius:12px; box-shadow:0 2px 8px rgba(0,0,0,0.1); overflow:hidden; }
        .store-card .cover { width:100%; height:180px; object-fit:cover; }
        .store-details { padding:20px; text-align:center; }
        .store-details .avatar { width:100px; height:100px; border-radius:50%; object-fit:cover; border:3px solid #eee; margin-bottom:10px; }
        button { background:#d97706; color:white; border:none; padding:10px 16px; border-radius:8px; cursor:pointer; }
        button:hover { background:#b45309; }
        .edit-section { margin-top:40px; background:#f9f9f9; padding:20px; border-radius:12px; }
        label { display:block; margin-top:10px; font-weight:bold; }
        input, textarea { width:100%; padding:8px; border:1px solid #ccc; border-radius:6px; margin-top:4px; }
    </style>
</body>
</html>
