(function (window, document) {
    'use strict';

    const appShell = window.appShell;
    if (!appShell) {
        return;
    }

    const booksApiBase = appShell.booksApiBase;
    const pageSize = 20;
    let currentPage = 1;
    let totalPages = 0;
    let isLoading = false;
    let currentSort = 'new';
    let currentCategory = '';
    let highlightId = null;
    let loadMoreObserver = null;

    appShell.onReady(function () {
        const grid = document.getElementById('catalogGrid');
        if (!grid) {
            return;
        }
        readInitialParams();
        fetchCategories();
        fetchBooks(true);
        setupLoadMoreObserver();
    });

    function readInitialParams() {
        const params = new URLSearchParams(window.location.search);
        const sortParam = params.get('sort');
        const categoryParam = params.get('category');
        const highlightParam = params.get('highlight');

        if (sortParam) {
            currentSort = sortParam;
            const sortSelect = document.getElementById('sortSelect');
            if (sortSelect) {
                sortSelect.value = sortParam;
            }
        }
        if (categoryParam) {
            currentCategory = categoryParam;
            const categorySelect = document.getElementById('categoryFilter');
            if (categorySelect) {
                categorySelect.value = categoryParam;
            }
        }
        if (highlightParam) {
            const parsed = parseInt(highlightParam, 10);
            if (!Number.isNaN(parsed)) {
                highlightId = parsed;
            }
        }
    }

    async function fetchCategories() {
        try {
            const response = await fetch(booksApiBase + '/categories');
            if (!response.ok) {
                throw new Error('Failed to load categories');
            }
            const payload = await response.json();
            const categories = Array.isArray(payload.data) ? payload.data : [];
            const select = document.getElementById('categoryFilter');
            if (select) {
                categories.forEach(function (category) {
                    const option = document.createElement('option');
                    option.value = category;
                    option.textContent = category;
                    if (category === currentCategory) {
                        option.selected = true;
                    }
                    select.appendChild(option);
                });
                select.addEventListener('change', function () {
                    currentCategory = select.value;
                    resetAndFetch();
                });
            }
            const sortSelect = document.getElementById('sortSelect');
            if (sortSelect) {
                sortSelect.addEventListener('change', function () {
                    currentSort = sortSelect.value;
                    resetAndFetch();
                });
            }
        } catch (error) {
            console.error('Categories error', error);
        }
    }

    function resetAndFetch() {
        currentPage = 1;
        totalPages = 0;
        fetchBooks(true);
        updateUrlState();
    }

    async function fetchBooks(reset) {
        if (isLoading) {
            return;
        }
        if (!reset && totalPages !== 0 && currentPage > totalPages) {
            updateLoadMoreState();
            return;
        }
        isLoading = true;
        toggleLoadingState(true);
        try {
            const params = new URLSearchParams();
            params.append('page', currentPage.toString());
            params.append('size', pageSize.toString());
            params.append('sort', currentSort);
            if (currentCategory) {
                params.append('category', currentCategory);
            }
            const response = await fetch(booksApiBase + '?' + params.toString());
            if (!response.ok) {
                throw new Error('Failed to load books');
            }
            const payload = await response.json();
            totalPages = payload.totalPages || 0;
            updateSummary(payload.totalItems || 0);
            renderBooks(Array.isArray(payload.data) ? payload.data : [], reset);
            updateLoadMoreState();
            currentPage += 1;
        } catch (error) {
            console.error('Books error', error);
            showStatus('Không thể tải danh sách sách. Vui lòng thử lại sau.');
        } finally {
            toggleLoadingState(false);
            isLoading = false;
            appShell.refreshIcons();
        }
    }

    function renderBooks(books, reset) {
        const grid = document.getElementById('catalogGrid');
        const empty = document.getElementById('emptyState');
        if (!grid) {
            return;
        }
        if (reset) {
            grid.innerHTML = '';
        }
        if (books.length === 0 && (reset || grid.children.length === 0)) {
            if (empty) {
                empty.classList.remove('hidden');
            }
            return;
        }
        if (empty) {
            empty.classList.add('hidden');
        }
        const fragment = document.createDocumentFragment();
        books.forEach(function (book) {
            const card = document.createElement('article');
            card.className = 'catalog-card bg-white rounded-xl border border-gray-100 overflow-hidden flex flex-col';
            card.dataset.bookId = book.id;
            card.innerHTML = buildCatalogCard(book);
            fragment.appendChild(card);
        });
        grid.appendChild(fragment);
        highlightIfNeeded();
    }

    function buildCatalogCard(book) {
        const title = appShell.escapeHtml(book.title || 'Sách chưa cập nhật');
        const author = appShell.escapeHtml(book.author || 'Đang cập nhật');
        const price = appShell.formatCurrency(book.price);
        const image = book.imageUrl || 'https://placehold.co/320x420?text=Book';
        const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
        const ratingCount = book.ratingCount || 0;
        const favoriteCount = book.favoriteCount || 0;
        const sold = book.totalSold || 0;
        return `
            <div class="relative">
                <img src="${image}" alt="${title}" class="w-full h-64 object-cover">
                <div class="absolute top-3 left-3 bg-white/90 text-amber-700 text-xs font-semibold px-2 py-1 rounded-full shadow-sm">
                    ${rating} ★ (${ratingCount})
                </div>
            </div>
            <div class="p-5 flex flex-col flex-grow">
                <h3 class="title-font text-xl font-semibold mb-2">${title}</h3>
                <p class="text-gray-500 text-sm mb-3">${author}</p>
                <p class="text-sm text-gray-500 mb-3">Đã bán: <span class="font-medium text-gray-700">${sold}</span> · Yêu thích: <span class="font-medium text-gray-700">${favoriteCount}</span></p>
                <p class="text-amber-700 font-bold text-lg mb-4">${price}</p>
                <div class="mt-auto flex flex-col sm:flex-row gap-3">
                    <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-semibold py-2 px-4 rounded-full text-sm transition" data-add-to-cart data-book-id="${book.id}">
                        Thêm vào giỏ
                    </button>
                    <a href="${appShell.contextPath}/books/detail?id=${book.id}" class="text-center text-sm text-amber-700 hover:text-amber-900 font-medium">
                        Xem chi tiết
                    </a>
                </div>
            </div>
        `;
    }

    function highlightIfNeeded() {
        if (!highlightId) {
            return;
        }
        const grid = document.getElementById('catalogGrid');
        const card = grid ? grid.querySelector('[data-book-id="' + highlightId + '"]') : null;
        if (card) {
            card.classList.add('highlight-card');
            card.scrollIntoView({ behavior: 'smooth', block: 'center' });
            highlightId = null;
        }
    }

    function toggleLoadingState(isLoadingNow) {
        const button = document.getElementById('loadMoreBtn');
        const status = document.getElementById('catalogStatus');
        if (button) {
            button.disabled = isLoadingNow;
            button.textContent = isLoadingNow ? 'Đang tải...' : 'Tải thêm 20 sách';
        }
        if (status && isLoadingNow) {
            status.textContent = 'Đang tải...';
        }
    }

    function updateLoadMoreState() {
        const button = document.getElementById('loadMoreBtn');
        const status = document.getElementById('catalogStatus');
        if (!button || !status) {
            return;
        }
        if (currentPage > totalPages || totalPages === 0) {
            button.classList.add('hidden');
        } else {
            button.classList.remove('hidden');
        }
        status.textContent = totalPages === 0 ? '' : 'Trang ' + Math.min(currentPage, totalPages) + ' trên ' + totalPages;
        button.onclick = function () {
            fetchBooks(false);
        };
    }

    function updateSummary(totalItems) {
        const summary = document.getElementById('resultSummary');
        if (!summary) {
            return;
        }
        if (totalItems === 0) {
            summary.textContent = 'Không có sách nào phù hợp bộ lọc hiện tại.';
        } else {
            summary.textContent = 'Đã tìm thấy ' + totalItems + ' tựa sách.';
        }
    }

    function showStatus(message) {
        const status = document.getElementById('catalogStatus');
        if (status) {
            status.textContent = message;
        }
    }

    function updateUrlState() {
        const params = new URLSearchParams();
        if (currentCategory) {
            params.set('category', currentCategory);
        }
        if (currentSort && currentSort !== 'new') {
            params.set('sort', currentSort);
        }
        const query = params.toString();
        const newUrl = query ? window.location.pathname + '?' + query : window.location.pathname;
        window.history.replaceState({}, '', newUrl);
    }

    function setupLoadMoreObserver() {
        const sentinel = document.getElementById('loadMoreBtn');
        if (!('IntersectionObserver' in window) || !sentinel) {
            return;
        }
        loadMoreObserver = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    fetchBooks(false);
                }
            });
        }, { rootMargin: '200px' });
        loadMoreObserver.observe(sentinel);
    }
})(window, document);
