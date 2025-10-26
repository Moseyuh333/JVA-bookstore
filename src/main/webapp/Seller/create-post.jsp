<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng bài viết mới</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: linear-gradient(135deg, #f6d365 0%, #fda085 100%);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
        }
        .container {
            background: white;
            border-radius: 16px;
            padding: 30px;
            max-width: 600px;
            width: 100%;
            box-shadow: 0 5px 30px rgba(0,0,0,0.2);
        }
        h2 { text-align: center; margin-bottom: 20px; color: #333; }
        .form-group { margin-bottom: 20px; }
        label { font-weight: bold; }
        input, textarea {
            width: 100%;
            padding: 10px;
            border-radius: 8px;
            border: 1px solid #ccc;
            font-size: 14px;
        }
        button {
            width: 100%;
            padding: 12px;
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
            border: none;
            border-radius: 8px;
            cursor: pointer;
            font-size: 16px;
            font-weight: bold;
        }
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(0,0,0,0.2);
        }
        .alert {
            margin-bottom: 15px;
            padding: 10px;
            border-radius: 6px;
        }
        .alert-success { background: #dcfce7; color: #166534; }
        .alert-error { background: #fee2e2; color: #991b1b; }
    </style>
</head>
<body>
<div class="container">
    <h2>✍️ Đăng bài viết mới</h2>
    <div id="alertContainer"></div>
    <form id="postForm">
        <div class="form-group">
            <label>Tiêu đề bài viết</label>
            <input type="text" name="title" required placeholder="Nhập tiêu đề...">
        </div>

        <div class="form-group">
            <label>Nội dung</label>
            <textarea name="content" rows="5" required placeholder="Nhập nội dung chi tiết..."></textarea>
        </div>

        <div class="form-group">
            <label>Link ảnh minh họa (tùy chọn)</label>
            <input type="text" name="image_url" placeholder="https://...">
        </div>

        <button type="submit" id="submitBtn">Đăng bài viết</button>
    </form>
</div>

<script>
const API_URL = '<%= request.getContextPath() %>/api/seller/posts';

document.getElementById('postForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const btn = document.getElementById('submitBtn');
    const alertBox = document.getElementById('alertContainer');
    btn.disabled = true;
    btn.textContent = "Đang đăng...";

    const formData = new FormData(this);

    try {
        const res = await fetch(API_URL, {
            method: 'POST',
            body: new URLSearchParams(formData),
            credentials: 'include'
        });
        const data = await res.json();

        if (data.success) {
            alertBox.innerHTML = `<div class="alert alert-success">Đăng bài thành công!</div>`;
            this.reset();
        } else {
            alertBox.innerHTML = `<div class="alert alert-error">${data.message || 'Có lỗi xảy ra'}</div>`;
        }
    } catch (err) {
        alertBox.innerHTML = `<div class="alert alert-error">Không thể kết nối server</div>`;
    } finally {
        btn.disabled = false;
        btn.textContent = "Đăng bài viết";
    }
});
</script>
</body>
</html>
