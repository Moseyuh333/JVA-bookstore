document.addEventListener("DOMContentLoaded", () => {
    if (typeof feather !== "undefined") {
        feather.replace();
    }

    const contextPath = window.appConfig?.contextPath || "";
    const shopId = window.appConfig?.shopId || "";

    const tableBody = document.getElementById("product");
    const loadingState = document.getElementById("loadingState");
    const emptyState = document.getElementById("emptyState");
    const tableContainer = document.getElementById("tableContainer");
    const totalEl = document.getElementById("totalProducts");
    const inStockEl = document.getElementById("inStock");
    const outOfStockEl = document.getElementById("outOfStock");
    const paginationEl = document.getElementById("pagination");
    const searchInput = document.getElementById("searchInput");
    const searchBtn = document.getElementById("searchBtn");
    const searchTypeSelect = document.getElementById("searchType");
    const resetBtn = document.getElementById("btnReset");
    const saveProductBtn = document.getElementById("saveProductBtn");
    const productForm = document.getElementById("productForm");

    let products = [];
    let currentPage = 1;
    const limit = 20;
    let currentSearch = "";
    let currentSearchType = searchTypeSelect ? searchTypeSelect.value : "title";

    const getAuthToken = () =>
        localStorage.getItem("seller_token") ||
        localStorage.getItem("auth_token") ||
        "";

    const authHeaders = () => {
        const token = getAuthToken();
        return token ? { Authorization: `Bearer ${token}` } : {};
    };

    const toggleDisplay = (element, show, displayValue = "block") => {
        if (!element) {
            return;
        }
        element.style.display = show ? displayValue : "none";
    };

    const showLoading = () => {
        toggleDisplay(loadingState, true, "flex");
        toggleDisplay(tableContainer, false);
        toggleDisplay(emptyState, false);
    };

    const showTable = () => {
        toggleDisplay(loadingState, false);
        toggleDisplay(tableContainer, true);
        toggleDisplay(emptyState, false);
    };

    const showEmpty = () => {
        toggleDisplay(loadingState, false);
        toggleDisplay(tableContainer, false);
        toggleDisplay(emptyState, true);
    };

    const escapeHtml = (value) => {
        if (value === null || value === undefined) {
            return "";
        }
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    };

    const formatCurrency = (value) => {
        if (value === null || value === undefined || value === "") {
            return "0₫";
        }
        const number = Number(value);
        if (Number.isNaN(number)) {
            return `${value}₫`;
        }
        const rounded = Math.round(number);
        return `${new Intl.NumberFormat("vi-VN").format(rounded)}₫`;
    };

    const renderTable = (list) => {
        if (!tableBody) {
            return;
        }
        tableBody.innerHTML = "";

        list.forEach((product) => {
            const stockValue = product.stock_quantity ?? product.stock ?? 0;
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${escapeHtml(product.id)}</td>
                <td>${escapeHtml(product.title)}</td>
                <td>${escapeHtml(product.author)}</td>
                <td>${escapeHtml(product.category)}</td>
                <td>${formatCurrency(product.price)}</td>
                <td>${escapeHtml(stockValue)}</td>
                <td>
                    <button class="btn btn-sm btn-warning mr-1" onclick="openEditModal(${product.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteProduct(${product.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });

        showTable();
    };

    const updateStats = (stats) => {
        if (!stats) {
            return;
        }
        if (totalEl) {
            totalEl.textContent = stats.total_books ?? stats.total ?? products.length ?? 0;
        }
        if (inStockEl) {
            inStockEl.textContent = stats.in_stock ?? 0;
        }
        if (outOfStockEl) {
            outOfStockEl.textContent = stats.out_stock ?? 0;
        }
    };

    const updatePagination = (total, page, perPage) => {
        if (!paginationEl) {
            return;
        }

        const safeTotal = Number.isFinite(total) ? total : products.length || 0;
        const totalPages = Math.max(1, Math.ceil(safeTotal / perPage));
        const current = Math.min(Math.max(1, page), totalPages);

        paginationEl.innerHTML = `
            <button class="btn btn-light btn-sm" ${current <= 1 ? "disabled" : ""} onclick="changePage(${current - 1})">« Trước</button>
            <span class="mx-2">Trang ${current} / ${totalPages}</span>
            <button class="btn btn-light btn-sm" ${current >= totalPages ? "disabled" : ""} onclick="changePage(${current + 1})">Sau »</button>
        `;
    };

    window.changePage = (page) => {
        if (page === currentPage || page < 1) {
            return;
        }
        currentPage = page;
        loadProducts();
    };

    const buildListParams = () => {
        const params = new URLSearchParams({
            action: "list",
            page: currentPage,
            limit,
        });
        if (currentSearch) {
            params.append("search", currentSearch);
            params.append("searchType", currentSearchType || "title");
        }
        return params.toString();
    };

    const handleUnauthorized = () => {
        alert("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        window.location.href = `${contextPath}/login.jsp`;
    };

    async function loadProducts() {
        showLoading();
        try {
            const res = await fetch(
                `${contextPath}/api/seller/products?${buildListParams()}`,
                {
                    headers: authHeaders(),
                    credentials: "same-origin",
                }
            );

            if (res.status === 401 || res.status === 403) {
                handleUnauthorized();
                return;
            }

            if (!res.ok) {
                throw new Error(`Yêu cầu thất bại (${res.status})`);
            }

            const data = await res.json();
            if (!data.success) {
                throw new Error(data.message || "Không thể tải danh sách sản phẩm");
            }

            products = data.products || [];
            if (!products.length) {
                showEmpty();
            } else {
                renderTable(products);
            }

            updateStats(data.stats);
            updatePagination(
                data.total ?? products.length,
                data.page ?? currentPage,
                data.limit ?? limit
            );
        } catch (error) {
            console.error("loadProducts error:", error);
            showEmpty();
            alert(error.message || "Không thể tải danh sách sản phẩm");
        }
    }

    async function refreshStats() {
        try {
            const res = await fetch(
                `${contextPath}/api/seller/products?action=stats`,
                {
                    headers: authHeaders(),
                    credentials: "same-origin",
                }
            );
            if (!res.ok) {
                return;
            }
            const data = await res.json();
            if (data.success) {
                updateStats(data.stats ?? data);
            }
        } catch (err) {
            console.warn("refreshStats error:", err);
        }
    }

    window.openAddModal = () => {
        if (!productForm) {
            return;
        }
        productForm.reset();
        const idField = document.getElementById("productId");
        if (idField) {
            idField.value = "";
        }
        const modalLabel = document.getElementById("productModalLabel");
        if (modalLabel) {
            modalLabel.textContent = "Thêm sản phẩm mới";
        }
        if (typeof $ !== "undefined") {
            $("#productModal").modal("show");
        }
    };

    window.openEditModal = (productId) => {
        const product = products.find((item) => Number(item.id) === Number(productId));
        if (!product || !productForm) {
            return;
        }

        const modalLabel = document.getElementById("productModalLabel");
        if (modalLabel) {
            modalLabel.textContent = "Chỉnh sửa sản phẩm";
        }
        const idField = document.getElementById("productId");
        if (idField) idField.value = product.id ?? "";
        const titleField = document.getElementById("title");
        if (titleField) titleField.value = product.title ?? "";
        const authorField = document.getElementById("author");
        if (authorField) authorField.value = product.author ?? "";
        const genreField = document.getElementById("genre");
        if (genreField) genreField.value = product.category ?? "";
        const priceField = document.getElementById("price");
        if (priceField) priceField.value = product.price ?? "";
        const stockField = document.getElementById("stock");
        if (stockField) {
            stockField.value = product.stock_quantity ?? product.stock ?? "";
        }
        const isbnField = document.getElementById("isbn");
        if (isbnField) isbnField.value = product.isbn ?? "";
        const descField = document.getElementById("description");
        if (descField) descField.value = product.description ?? "";

        if (typeof $ !== "undefined") {
            $("#productModal").modal("show");
        }
    };

    const submitProduct = async () => {
        if (!productForm) {
            return;
        }

        const formData = new FormData(productForm);
        const productId = formData.get("productId");
        const isEdit = productId && productId.trim() !== "";

        const payload = new URLSearchParams();
        payload.append("action", isEdit ? "update" : "create");

        if (isEdit) {
            payload.append("id", productId);
        }

        payload.append("title", (formData.get("title") || "").trim());
        payload.append("author", (formData.get("author") || "").trim());
        payload.append("category", (formData.get("genre") || "").trim());
        payload.append("price", (formData.get("price") || "").trim());
        payload.append("stock", (formData.get("stock") || "").trim());
        payload.append("isbn", (formData.get("isbn") || "").trim());
        payload.append("description", (formData.get("description") || "").trim());
        if (shopId) {
            payload.append("shop_id", shopId);
        }

        try {
            const res = await fetch(`${contextPath}/api/seller/products`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    ...authHeaders(),
                },
                body: payload.toString(),
                credentials: "same-origin",
            });

            if (res.status === 401 || res.status === 403) {
                handleUnauthorized();
                return;
            }

            const result = await res.json();
            if (!result.success) {
                throw new Error(result.message || "Không thể lưu sản phẩm");
            }

            if (typeof $ !== "undefined") {
                $("#productModal").modal("hide");
            }
            productForm.reset();
            await loadProducts();
            await refreshStats();
            alert(isEdit ? "Cập nhật sản phẩm thành công!" : "Thêm sản phẩm thành công!");
        } catch (error) {
            console.error("submitProduct error:", error);
            alert(error.message || "Không thể lưu sản phẩm");
        }
    };

    if (saveProductBtn) {
        saveProductBtn.addEventListener("click", submitProduct);
    }

    window.deleteProduct = async (productId) => {
        if (!productId) {
            return;
        }
        if (!confirm("Bạn có chắc chắn muốn xóa sản phẩm này?")) {
            return;
        }

        const payload = new URLSearchParams({
            action: "delete",
            id: productId,
        });

        try {
            const res = await fetch(`${contextPath}/api/seller/products`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    ...authHeaders(),
                },
                body: payload.toString(),
                credentials: "same-origin",
            });

            if (res.status === 401 || res.status === 403) {
                handleUnauthorized();
                return;
            }

            const result = await res.json();
            if (!result.success) {
                throw new Error(result.message || "Không thể xóa sản phẩm");
            }

            await loadProducts();
            await refreshStats();
            alert("Xóa sản phẩm thành công!");
        } catch (error) {
            console.error("deleteProduct error:", error);
            alert(error.message || "Không thể xóa sản phẩm");
        }
    };

    if (searchBtn) {
        searchBtn.addEventListener("click", () => {
            currentSearch = searchInput ? searchInput.value.trim() : "";
            currentSearchType = searchTypeSelect ? searchTypeSelect.value : "title";
            currentPage = 1;
            loadProducts();
        });
    }

    if (searchInput) {
        searchInput.addEventListener("keypress", (event) => {
            if (event.key === "Enter") {
                currentSearch = searchInput.value.trim();
                currentSearchType = searchTypeSelect ? searchTypeSelect.value : "title";
                currentPage = 1;
                loadProducts();
            }
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener("click", () => {
            if (searchInput) {
                searchInput.value = "";
            }
            if (searchTypeSelect) {
                searchTypeSelect.value = "title";
            }
            currentSearch = "";
            currentSearchType = searchTypeSelect ? searchTypeSelect.value : "title";
            currentPage = 1;
            loadProducts();
        });
    }

    loadProducts();
    refreshStats();
});
