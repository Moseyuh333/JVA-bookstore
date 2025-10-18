// ========== ADCOMMISSION.JS ==========
// Quản lý chiết khấu (commission) của app cho từng shop

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const tableBody = document.getElementById("commissionTable");
    const searchInput = document.getElementById("commissionSearchInput");

    // Dữ liệu giả lập (demo)
    const commissions = [
        { id: "S001", shop: "BookHaven", rate: "10%", since: "01/01/2024", note: "Cửa hàng uy tín, doanh thu cao" },
        { id: "S002", shop: "MangaWorld", rate: "15%", since: "05/03/2024", note: "Áp dụng chiết khấu tạm thời" },
        { id: "S003", shop: "LightNovelVN", rate: "12%", since: "10/05/2024", note: "Chính sách mặc định" }
    ];

    // Render bảng
    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (list.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Không có dữ liệu chiết khấu</td></tr>`;
            return;
        }

        list.forEach(c => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${c.id}</td>
                <td>${c.shop}</td>
                <td>${c.rate}</td>
                <td>${c.since}</td>
                <td>${c.note}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    };

    // Tìm kiếm chiết khấu
    searchInput?.addEventListener("input", () => {
        const keyword = searchInput.value.toLowerCase().trim();
        const filtered = commissions.filter(c =>
            c.shop.toLowerCase().includes(keyword) ||
            c.rate.toLowerCase().includes(keyword) ||
            c.note.toLowerCase().includes(keyword)
        );
        renderTable(filtered);
    });

    renderTable(commissions);
});
