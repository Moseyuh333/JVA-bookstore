// ========== ADPROMOTION.JS ==========
// Quản lý chương trình khuyến mãi

document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("promotionTable");

    const promotions = [
        { id: "KM001", name: "Giảm 20% toàn sàn", type: "Sản phẩm", discount: "20%", valid: "01/10 - 31/10" },
        { id: "KM002", name: "Miễn phí vận chuyển", type: "Vận chuyển", discount: "100%", valid: "01/11 - 15/11" }
    ];

    const renderTable = () => {
        tableBody.innerHTML = "";
        promotions.forEach(p => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${p.id}</td>
                <td>${p.name}</td>
                <td>${p.type}</td>
                <td>${p.discount}</td>
                <td>${p.valid}</td>
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
