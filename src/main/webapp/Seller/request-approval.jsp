<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Yêu cầu trở thành người bán</title>
    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background-color: #f9f9f9;
            margin: 0;
            padding: 0;
        }
        .container {
            max-width: 500px;
            margin: 80px auto;
            background: #fff;
            padding: 40px 30px;
            border-radius: 16px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            text-align: center;
        }
        h1 {
            color: #333;
            font-size: 22px;
            margin-bottom: 15px;
        }
        p.desc {
            color: #666;
            font-size: 15px;
            margin-bottom: 25px;
        }
        .btn-submit {
            background: #007bff;
            color: #fff;
            border: none;
            padding: 12px 25px;
            border-radius: 8px;
            cursor: pointer;
            font-size: 15px;
            transition: 0.3s;
        }
        .btn-submit:hover {
            background: #0056b3;
        }
        .alert {
            margin-top: 20px;
            padding: 12px 18px;
            border-radius: 6px;
            display: none;
            font-size: 14px;
        }
        .alert.success {
            background: #d4edda;
            color: #155724;
        }
        .alert.error {
            background: #f8d7da;
            color: #721c24;
        }
    </style>
</head>
<body>

<div class="container">
    <h1>Gửi yêu cầu trở thành người bán</h1>
    <p class="desc">
        Sau khi gửi yêu cầu, quản trị viên sẽ xem xét và duyệt.  
        Bạn sẽ nhận được thông báo khi được chấp thuận.
    </p>

    <form id="approvalForm">
        <button type="submit" class="btn-submit" id="submitBtn">
            Gửi yêu cầu
        </button>
    </form>

    <div class="alert" id="alertBox"></div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function() {
    const API_URL = '<%= request.getContextPath() %>/api/seller/request-approval';
    const form = document.getElementById("approvalForm");
    const submitBtn = document.getElementById("submitBtn");
    const alertBox = document.getElementById("alertBox");

    function showAlert(message, type) {
        alertBox.textContent = message;
        alertBox.className = 'alert ' + type;
        alertBox.style.display = 'block';
    }

    form.addEventListener("submit", async function(e) {
        e.preventDefault();
        submitBtn.disabled = true;
        submitBtn.textContent = "Đang gửi...";

        try {
            const response = await fetch(API_URL, {
                method: "POST",
            });
            const data = await response.json();

            if (data.success) {
                showAlert('Yêu cầu của bạn đã được gửi. Vui lòng chờ admin duyệt.', 'success');
                setTimeout(() => {
                    window.location.href = '<%= request.getContextPath() %>/index.jsp';
                }, 1500);
            } else {
                showAlert(data.message || 'Không thể gửi yêu cầu.', 'error');
            }
        } catch (error) {
            showAlert('Lỗi kết nối tới server.', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Gửi yêu cầu";
        }
    });
});
</script>

</body>
</html>
