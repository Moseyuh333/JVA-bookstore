// ========== ADPRODUCT.JS ==========
// Quản lý danh sách sản phẩm

document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") feather.replace();

    const searchInput = document.getElementById("productSearchInput");
    const tableBody = document.getElementById("product");

    // Fake Data (demo)
    const products = [
        { id: "B001", name: "Truyện Kiều", author: "Nguyễn Du", category: "Văn học", price: "120000", stock: 35, seller: "Shop Văn Học Việt" },
        { id: "B002", name: "Sherlock Holmes", author: "Arthur Conan Doyle", category: "Trinh thám", price: "95000", stock: 40, seller: "BookWorld" },
    ];

    // Render table
    const renderTable = (list) => {
        tableBody.innerHTML = "";
        if (list.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">Không có sản phẩm nào</td></tr>`;
            return;
        }

        list.forEach(p => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${p.id}</td>
                <td>${p.name}</td>
                <td>${p.author}</td>
                <td>${p.category}</td>
                <td>${parseInt(p.price).toLocaleString("vi-VN")}₫</td>
                <td>${p.stock}</td>
                <td>${p.seller}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-sm btn-danger"><i class="fas fa-trash"></i></button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    };

    // Search filter
    searchInput?.addEventListener("input", () => {
        const keyword = searchInput.value.toLowerCase().trim();
        const filtered = products.filter(p =>
            p.name.toLowerCase().includes(keyword) ||
            p.author.toLowerCase().includes(keyword) ||
            p.category.toLowerCase().includes(keyword)
        );
        renderTable(filtered);
    });

    // Init
    renderTable(products);
});
