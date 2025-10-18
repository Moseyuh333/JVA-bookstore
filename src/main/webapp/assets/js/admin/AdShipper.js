// ========== ADSHIPPER.JS ==========
// Quản lý danh sách nhà vận chuyển

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
