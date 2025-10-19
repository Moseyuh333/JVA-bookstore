(function (window) {
    'use strict';

    const appShell = window.appShell;
    if (!appShell) {
        return;
    }

    const booksApiBase = appShell.booksApiBase;

    appShell.onReady(function () {
        const container = document.getElementById('homeSectionsContainer');
        if (!container) {
            return;
        }
        loadHomeSections(container);
    });

    let scrollControlsBound = false;
    const SCROLL_STEP = 320;

    async function loadHomeSections(container) {
        const loading = document.getElementById('homeSectionsLoading');
        try {
            const response = await fetch(booksApiBase + '/sections?limit=20');
            if (!response.ok) {
                throw new Error('Failed to load sections');
            }
            const payload = await response.json();
            container.innerHTML = '';
            if (payload.sections && payload.sections.length > 0) {
                payload.sections.forEach(function (section, index) {
                    container.appendChild(renderSection(section, index));
                });
            } else {
                container.innerHTML = renderEmptyState();
            }
        } catch (error) {
            console.error('Load sections error', error);
            if (loading) {
                loading.textContent = 'Không thể tải dữ liệu sách. Vui lòng thử lại sau.';
            }
        } finally {
            appShell.refreshIcons();
        }
    }

    function renderSection(section, index) {
        const wrapper = document.createElement('section');
        wrapper.className = 'space-y-6';
        const safeBooks = Array.isArray(section.books) ? section.books.slice(0, 20) : [];
        const sortKey = encodeURIComponent(section.sort || 'new');
        const listId = buildListId(section, index);
        const cardsHtml = safeBooks.length > 0 ? safeBooks.map(renderBookCard).join('') : renderSkeletonCards(6);
        ensureScrollControls();
        wrapper.innerHTML = `
            <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                <div>
                    <h3 class="title-font text-2xl font-bold">${appShell.escapeHtml(section.title || 'Danh mục')}</h3>
                    <p class="text-gray-500 text-sm">Những tựa sách nổi bật được độc giả quan tâm</p>
                </div>
                <a href="${appShell.contextPath}/catalog.jsp?sort=${sortKey}" class="inline-flex items-center text-amber-700 hover:text-amber-900 text-sm font-medium">
                    Xem tất cả
                    <i data-feather="arrow-right" class="w-4 h-4 ml-1"></i>
                </a>
            </div>
            <div class="relative">
                <button type="button" class="hidden md:flex absolute left-0 top-1/2 -translate-y-1/2 z-10 bg-white shadow rounded-full w-10 h-10 items-center justify-center text-amber-700 hover:text-amber-900 focus:outline-none" data-scroll-control="prev" data-scroll-target="${listId}">
                    <i data-feather="chevron-left" class="w-5 h-5"></i>
                </button>
                <div id="${listId}" class="flex gap-5 overflow-x-auto scroll-smooth pb-2 snap-x" data-scrollable>
                    ${cardsHtml}
                </div>
                <button type="button" class="hidden md:flex absolute right-0 top-1/2 -translate-y-1/2 z-10 bg-white shadow rounded-full w-10 h-10 items-center justify-center text-amber-700 hover:text-amber-900 focus:outline-none" data-scroll-control="next" data-scroll-target="${listId}">
                    <i data-feather="chevron-right" class="w-5 h-5"></i>
                </button>
            </div>
        `;
        return wrapper;
    }

    function renderBookCard(book) {
        const title = appShell.escapeHtml(book.title || 'Sách chưa cập nhật');
        const author = appShell.escapeHtml(book.author || 'Đang cập nhật');
        const price = appShell.formatCurrency(book.price);
        // Debug: log imageUrl for troubleshooting
        console.log('Book imageUrl:', book.imageUrl, '| title:', book.title);
        const image = book.imageUrl || 'https://placehold.co/320x420?text=Book';
        const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
        const ratingCount = book.ratingCount || 0;
        const detailUrl = appShell.contextPath + '/books/detail?id=' + encodeURIComponent(book.id);
        return `
            <article class="book-card bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 transition duration-300 flex flex-col flex-shrink-0 w-56" style="min-width: 224px;">
                <div class="relative">
                    <img src="${image}" alt="${title}" class="w-full h-56 object-cover">
                    <span class="absolute top-3 left-3 bg-white/90 text-amber-700 text-xs font-semibold px-2 py-1 rounded-full shadow-sm">
                        ${rating} ★ (${ratingCount})
                    </span>
                </div>
                <div class="p-5 flex flex-col flex-grow">
                    <h4 class="title-font font-semibold text-lg mb-1">${title}</h4>
                    <p class="text-gray-500 text-sm mb-3">${author}</p>
                    <p class="text-amber-700 font-bold mb-4">${price}</p>
                    <div class="mt-auto flex flex-col gap-2">
                        <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-medium py-2 px-4 rounded-full text-sm transition" data-add-to-cart data-book-id="${book.id}">
                            Thêm vào giỏ
                        </button>
                        <a href="${detailUrl}" class="text-center text-sm text-amber-700 hover:text-amber-900 font-medium">
                            Xem chi tiết
                        </a>
                    </div>
                </div>
            </article>
        `;
    }

    function renderSkeletonCards(count) {
        return Array.from({ length: count }).map(function () {
            return `
                <div class="bg-white border border-dashed border-amber-200 rounded-xl h-56 flex-shrink-0 w-56 flex items-center justify-center text-amber-400 text-sm" style="min-width: 224px;">
                    Đang cập nhật
                </div>
            `;
        }).join('');
    }

    function renderEmptyState() {
        return `
            <div class="text-center py-16 bg-white rounded-xl border border-dashed border-amber-200 text-gray-500">
                Chưa có dữ liệu sách để hiển thị. Hãy thêm sách trong kho dữ liệu.
            </div>
        `;
    }

    function ensureScrollControls() {
        if (scrollControlsBound) {
            return;
        }
        document.addEventListener('click', function (event) {
            const control = event.target.closest('[data-scroll-control]');
            if (!control) {
                return;
            }
            const targetId = control.getAttribute('data-scroll-target');
            if (!targetId) {
                return;
            }
            const container = document.getElementById(targetId);
            if (!container) {
                return;
            }
            const direction = control.getAttribute('data-scroll-control');
            const offset = direction === 'next' ? SCROLL_STEP : -SCROLL_STEP;
            container.scrollBy({ left: offset, behavior: 'smooth' });
        });
        scrollControlsBound = true;
    }

    function buildListId(section, index) {
        const base = section && section.key ? String(section.key) : 'section-' + index;
        return 'home-section-' + base.replace(/[^a-zA-Z0-9_-]+/g, '-');
    }
})(window);
