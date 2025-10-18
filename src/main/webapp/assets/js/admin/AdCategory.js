// ========== ADCATEGORY.JS ==========
// Quản lý danh mục sản phẩm

document.addEventListener("DOMContentLoaded", () => {
    const tableBody = document.getElementById("categoryTable");

    const categories = [
        { id: "C001", name: "Tiểu thuyết", description: "Sách văn học và truyện dài" },
        { id: "C002", name: "Truyện tranh", description: "Manga, Comic, Graphic novel" }
    ];

    const renderTable = () => {
        tableBody.innerHTML = "";
        categories.forEach(c => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${c.id}</td>
                <td>${c.name}</td>
                <td>${c.description}</td>
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
