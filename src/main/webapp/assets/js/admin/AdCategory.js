
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
const getAuthHeaders = () => {
    const token = localStorage.getItem("admin_token");
    return token ? { 'Authorization': `Bearer ${token}` } : {};
};

const api = {
    getCategories: (search = '', searchType = 'all') => {
        const params = new URLSearchParams({
            action: 'list'
        });
        if (search.trim()) {
            params.append('search', search.trim());
            params.append('searchType', searchType);
        }
        return fetch(`${contextPath}/api/admin/categories?${params.toString()}`, {
            headers: getAuthHeaders()
        }).then(r => r.json());
    },
    createCategory: (data) => fetch(`${contextPath}/api/admin/categories?action=create`, {
        method: 'POST',
        headers: { ...getAuthHeaders(), 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(data)
    }).then(r => r.json()),
    getCategory: (id) => fetch(`${contextPath}/api/admin/categories?action=get&id=${id}`, {
        headers: getAuthHeaders()
    }).then(r => r.json()),
    updateCategory: (id, data) => fetch(`${contextPath}/api/admin/categories?action=update&id=${id}`, {
        method: 'POST',
        headers: { ...getAuthHeaders(), 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(data)
    }).then(r => r.json()),
    deleteCategory: (id) => fetch(`${contextPath}/api/admin/categories?action=delete&id=${id}`, {
        method: 'POST',
        headers: getAuthHeaders()
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
            <td>${category.id}</td>
            <td>${escapeHtml(category.name)}</td>
            <td>${category.product_count || 0}</td>
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
    // Luôn xóa danh sách cũ trước khi vẽ mới
    categoryList.innerHTML = "";

    if (!Array.isArray(cats) || cats.length === 0) {
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
const loadCategories = async (searchTerm = '', searchType = 'all') => {
    try {
        showLoading();
        const response = await api.getCategories(searchTerm, searchType);

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

// Filter functions (server-side)
const applyFilters = async () => {
    const searchTerm = searchInput.value.trim();
    const searchType = document.getElementById("searchType")?.value || 'all';
    await loadCategories(searchTerm, searchType);
};

const resetFilters = () => {
    searchInput.value = "";
    loadCategories();
};

// Modal handling
const modalOverlay = document.getElementById('categoryModalOverlay');
const modalBox = document.getElementById('categoryModalBox');
const modalClose = document.getElementById('categoryModalClose');
const modalCancel = document.getElementById('categoryCancel');
const categoryForm = document.getElementById('categoryForm');
const categoryIdInput = document.getElementById('categoryId');
const categoryNameInput = document.getElementById('categoryName');
const categoryModalTitle = document.getElementById('categoryModalTitle');

function openModal() {
    modalOverlay.classList.add('active');
    modalBox.classList.add('active');
    categoryNameInput.focus();
}

function closeModal() {
    modalOverlay.classList.remove('active');
    modalBox.classList.remove('active');
    categoryForm.reset();
    categoryIdInput.value = '';
}

modalClose?.addEventListener('click', closeModal);
modalCancel?.addEventListener('click', (e) => { e.preventDefault(); closeModal(); });
modalOverlay?.addEventListener('click', closeModal);

window.openAddModal = () => {
    categoryModalTitle.textContent = 'Thêm danh mục mới';
    categoryIdInput.value = '';
    categoryNameInput.value = '';
    openModal();
};

window.editCategory = (id) => {
    api.getCategory(id).then(data => {
        if (data.error) return alert('Lỗi: ' + data.error);
        categoryModalTitle.textContent = 'Chỉnh sửa danh mục';
        categoryIdInput.value = data.id || id;
        categoryNameInput.value = data.name || '';
        openModal();
    }).catch(err => {
        console.error(err);
        alert('Lỗi khi tải thông tin danh mục');
    });
};

// Handle form submit for create/update
categoryForm?.addEventListener('submit', function (e) {
    e.preventDefault();
    const id = categoryIdInput.value;
    const name = categoryNameInput.value && categoryNameInput.value.trim();
    if (!name) return alert('Tên danh mục là bắt buộc');

    if (id) {
        api.updateCategory(id, { name })
            .then(res => {
                if (res.message) {
                    alert('Cập nhật danh mục thành công');
                    closeModal();
                    loadCategories();
                } else {
                    alert('Lỗi: ' + (res.error || 'Không thể cập nhật danh mục'));
                }
            }).catch(err => {
                console.error(err);
                alert('Lỗi khi cập nhật danh mục');
            });
    } else {
        api.createCategory({ name })
            .then(res => {
                if (res.message) {
                    const idPart = res.id ? ('ID: ' + res.id + '\n') : '';
                    const createdPart = res.created_at ? ('Created: ' + res.created_at + '\n') : '';
                    const totalPart = typeof res.total_products !== 'undefined' ? ('Total products: ' + res.total_products + '\n') : '';
                    alert('Tạo danh mục thành công\n' + idPart + createdPart + totalPart);
                    closeModal();
                    loadCategories();
                } else {
                    alert('Lỗi: ' + (res.error || 'Không thể tạo danh mục'));
                }
            }).catch(err => {
                console.error(err);
                alert('Lỗi khi tạo danh mục');
            });
    }
});

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
window.applyFilters = applyFilters;
window.resetFilters = resetFilters;

console.log("✓ AdCategory.js initialized");

let debounceTimer;
searchInput.addEventListener("input", () => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(applyFilters, 300);
});
