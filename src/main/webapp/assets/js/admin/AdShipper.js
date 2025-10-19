// ========== ADSHIPPER.JS ==========
// Quản lý danh sách nhà vận chuyển

// ========================
// 📥 LOAD SHIPPERS FROM API
// ========================

async function loadShippers() {
    const tbody = document.querySelector('#ShipperTable');
    const empty = document.querySelector('#emptyState');
    const loading = document.querySelector('#loadingState');

    const token = localStorage.getItem("admin_token");
    if (!token) {
        window.location.href = `${window.appConfig?.contextPath || ''}/login.jsp`;
        return;
    }

    try {
        loading.style.display = 'block';
        const res = await fetch(`${window.appConfig?.contextPath || ''}/api/admin/shippers`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) throw new Error("Server trả lỗi " + res.status);
        const data = await res.json();

        tbody.innerHTML = '';
        if (data.shippers.length === 0) {
            empty.style.display = 'block';
            return;
        }
        empty.style.display = 'none';

        data.shippers.forEach(s => {
            tbody.innerHTML += `
                <tr>
                    <td>${s.id}</td>
                    <td>${s.name}</td>
                    <td>${s.phone}</td>
                    <td>${s.email}</td>
                    <td>${s.base_fee.toLocaleString()}₫</td>
                    <td>${s.service_area}</td>
                    <td>${s.estimated_time}</td>
                    <td>${s.status}</td>
                </tr>`;
        });

    } catch (err) {
        console.error(err);
        empty.style.display = 'block';
    } finally {
        loading.style.display = 'none';
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("shipperTable");

    const shippers = [
        { id: "SH001", name: "Giao Hàng Nhanh", price: "20,000₫", hotline: "1900 636 677" },
        { id: "SH002", name: "Viettel Post", price: "25,000₫", hotline: "1900 8095" }
    ];

    const renderTable = () => {
        tableBody.innerHTML = "";
        shippers.forEach(s => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${s.id}</td>
                <td>${s.name}</td>
                <td>${s.price}</td>
                <td>${s.hotline}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    };

    renderTable();
});

// ========================
// 🚀 Khởi tạo khi trang load
// ========================
window.addEventListener('load', () => {
    if (typeof feather !== "undefined") feather.replace();

    // Gọi API lấy dữ liệu user thật từ servlet
    loadShippers();

    // Sau khi load xong thì cập nhật thống kê
    updateStats();
});

