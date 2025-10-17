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
                payload.sections.forEach(function (section) {
                    container.appendChild(renderSection(section));
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

    function renderSection(section) {
        const wrapper = document.createElement('div');
        wrapper.className = 'space-y-6';
        const safeBooks = Array.isArray(section.books) ? section.books.slice(0, 20) : [];
        const sortKey = encodeURIComponent(section.sort || 'new');
        const cardsHtml = safeBooks.length > 0 ? safeBooks.map(renderBookCard).join('') : renderSkeletonCards();
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
                <button type="button" class="home-scroll-btn left-0 hidden md:flex" data-scroll="prev">
                    <i data-feather="chevron-left" class="w-5 h-5"></i>
                </button>
                <div class="horizontal-scroll flex gap-5 overflow-x-auto pb-3 scrollbar-hide snap-x snap-mandatory" data-scroll-container>
                    ${cardsHtml}
                </div>
                <button type="button" class="home-scroll-btn right-0 hidden md:flex" data-scroll="next">
                    <i data-feather="chevron-right" class="w-5 h-5"></i>
                </button>
            </div>
        `;

        const scroller = wrapper.querySelector('[data-scroll-container]');
        const controls = wrapper.querySelectorAll('[data-scroll]');
        controls.forEach(function (control) {
            control.addEventListener('click', function () {
                if (!scroller) {
                    return;
                }
                const direction = control.getAttribute('data-scroll') === 'next' ? 1 : -1;
                const step = scroller.firstElementChild ? scroller.firstElementChild.getBoundingClientRect().width + 20 : 320;
                scroller.scrollBy({ left: direction * step * 2, behavior: 'smooth' });
            });
        });

        if (scroller) {
            scroller.addEventListener('wheel', function (event) {
                if (Math.abs(event.deltaX) > Math.abs(event.deltaY)) {
                    return;
                }
                event.preventDefault();
                scroller.scrollBy({ left: event.deltaY, behavior: 'smooth' });
            }, { passive: false });
        }

        return wrapper;
    }

    function renderBookCard(book) {
        const title = appShell.escapeHtml(book.title || 'Sách chưa cập nhật');
        const author = appShell.escapeHtml(book.author || 'Đang cập nhật');
        const price = appShell.formatCurrency(book.price);
        // Normalize image URL and provide fallback
        const rawImage = (book.imageUrl || '').trim();
        let image = rawImage && rawImage.length > 0 ? rawImage : 'https://placehold.co/320x420?text=Book';
        if (/^http:\/\//i.test(image)) {
            image = image.replace(/^http:\/\//i, 'https://');
        }
        const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
        const ratingCount = book.ratingCount || 0;
        return `
            <article class="book-card flex-none snap-start bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 transition duration-300 flex flex-col w-64 min-w-[16rem]">
                <div class="relative">
                    <img src="${image}" alt="${title}" class="w-full h-56 object-cover" onerror="this.onerror=null;this.src='https://placehold.co/320x420?text=Book';">
                    <span class="absolute top-3 left-3 bg-white/90 text-amber-700 text-xs font-semibold px-2 py-1 rounded-full shadow-sm">
                        ${rating} ★ (${ratingCount})
                    </span>
                </div>
                <div class="p-5 flex flex-col flex-grow">
                    <h4 class="title-font font-semibold text-lg mb-1 h-14 overflow-hidden">${title}</h4>
                    <p class="text-gray-500 text-sm mb-3 h-10 overflow-hidden">${author}</p>
                    <p class="text-amber-700 font-bold mb-4">${price}
                    </p>
                    <div class="mt-auto flex flex-col gap-2">
                        <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-medium py-2 px-4 rounded-full text-sm transition" data-book-id="${book.id}">
                            Thêm vào giỏ
                        </button>
                        <a href="${appShell.contextPath}/books/detail?id=${book.id}" class="text-center text-sm text-amber-700 hover:text-amber-900 font-medium">
                            Xem chi tiết
                        </a>
                    </div>
                </div>
            </article>
        `;
    }

    function renderSkeletonCards() {
        return Array.from({ length: 6 }).map(function () {
            return `
                <article class="flex-none snap-start bg-white border border-dashed border-amber-200 rounded-xl h-56 w-64 min-w-[16rem] flex items-center justify-center text-amber-400 text-sm">
                    Đang cập nhật
                </article>
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
})(window);
