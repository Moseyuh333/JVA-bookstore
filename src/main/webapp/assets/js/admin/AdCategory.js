document.addEventListener("DOMContentLoaded", () => {
    console.log("✓ AdCategory.js loaded");

    const contextPath = window.appConfig?.contextPath || "";
    const categoryList = document.getElementById("categoryList");
    const totalCategoriesEl = document.getElementById("totalCategories");
    const activeCategoriesEl = document.getElementById("activeCategories");
    const searchInput = document.getElementById("searchInput");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");

    let categories = [];
    let filteredCategories = [];

    // API functions
    const api = {
        getCategories: () => fetch(`${contextPath}/api/admin/categories?action=list`).then(r => r.json()),
        createCategory: (data) => fetch(`${contextPath}/api/admin/categories?action=create`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(data)
        }).then(r => r.json()),
        updateCategory: (id, data) => fetch(`${contextPath}/api/admin/categories?action=update&id=${id}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(data)
        }).then(r => r.json()),
        deleteCategory: (id) => fetch(`${contextPath}/api/admin/categories?action=delete&id=${id}`, {
            method: 'POST'
        }).then(r => r.json())
    };

    // Utility functions
    const escapeHtml = (text) => {
        if (!text) return "";
        return text.replace(/[&<>"']/g, (m) => {
            const map = { "&": "&amp;", "<": "<", ">": ">", '"': "\"", "'": "&#39;" };
            return map[m];
        });
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return "";
        const date = new Date(dateStr);
        return date.toLocaleDateString('vi-VN');
    };

    const showLoading = () => {
        loadingState.style.display = "block";
        tableContainer.style.display = "none";
        emptyState.style.display = "none";
    };

    const hideLoading = () => {
        loadingState.style.display = "none";
        tableContainer.style.display = "block";
    };

    const showEmpty = () => {
        emptyState.style.display = "block";
        tableContainer.style.display = "none";
    };

    const hideEmpty = () => {
        emptyState.style.display = "none";
        tableContainer.style.display = "block";
    };

    // Render functions
    const renderCategoryRow = (category) => `
        <tr>
            <td>${escapeHtml(category.name)}</td>
            <td>${escapeHtml(category.description || "")}</td>
            <td>${category.product_count || 0}</td>
            <td><span class="badge-custom badge-active">Hoạt động</span></td>
            <td>${formatDate(category.created_at)}</td>
            <td>
                <div class="actions">
                    <button class="btn-icon btn-edit" title="Chỉnh sửa" onclick="editCategory(${category.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-icon btn-delete" title="Xóa" onclick="deleteCategory(${category.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `;

    const renderCategories = (cats) => {
        if (cats.length === 0) {
            showEmpty();
            return;
        }

        hideEmpty();
        categoryList.innerHTML = cats.map(renderCategoryRow).join("");
    };

    const updateStats = () => {
        const total = categories.length;
        const active = categories.filter(c => c.active !== false).length;

        if (totalCategoriesEl) totalCategoriesEl.textContent = total;
        if (activeCategoriesEl) activeCategoriesEl.textContent = active;
    };

    // Data management
    const loadCategories = async () => {
        try {
            showLoading();
            const response = await api.getCategories();

            if (response.categories) {
                categories = response.categories;
                filteredCategories = [...categories];
                renderCategories(filteredCategories);
                updateStats();
            } else {
                console.error("Invalid response format:", response);
                showEmpty();
            }
        } catch (error) {
            console.error("Error loading categories:", error);
            showEmpty();
        } finally {
            hideLoading();
        }
    };

    // Filter functions
    const applyFilters = () => {
        const searchTerm = searchInput.value.toLowerCase().trim();

        filteredCategories = categories.filter(cat =>
            cat.name.toLowerCase().includes(searchTerm) ||
            (cat.description && cat.description.toLowerCase().includes(searchTerm))
        );

        renderCategories(filteredCategories);
    };

    const resetFilters = () => {
        searchInput.value = "";
        filteredCategories = [...categories];
        renderCategories(filteredCategories);
    };

    // Modal functions
    window.openAddModal = () => {
        // TODO: Implement add modal
        alert("Add category modal - Coming soon!");
    };

    window.editCategory = (id) => {
        // TODO: Implement edit modal
        alert(`Edit category ${id} - Coming soon!`);
    };

    window.deleteCategory = async (id) => {
        if (!confirm("Bạn có chắc muốn xóa danh mục này?")) return;

        try {
            const response = await api.deleteCategory(id);
            if (response.message) {
                alert("Xóa danh mục thành công!");
                loadCategories();
            } else {
                alert("Lỗi: " + (response.error || "Không thể xóa danh mục"));
            }
        } catch (error) {
            console.error("Error deleting category:", error);
            alert("Lỗi khi xóa danh mục");
        }
    };

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener("input", applyFilters);
    }

    // Initialize
    loadCategories();

    console.log("✓ AdCategory.js initialized");
});
