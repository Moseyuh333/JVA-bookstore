(function (window, document) {
    'use strict';

    const appShell = window.appShell;
    if (!appShell) {
        return;
    }

    const overlay = document.getElementById('globalSearchOverlay');
    const input = document.getElementById('globalSearchInput');
    const form = document.getElementById('globalSearchForm');
    const message = document.getElementById('globalSearchMessage');
    const resultsContainer = document.getElementById('globalSearchResults');
    if (!overlay || !input || !form || !message || !resultsContainer) {
        return;
    }

    const openers = document.querySelectorAll('[data-open-search]');
    const dismissers = overlay.querySelectorAll('[data-search-dismiss]');
    let abortController = null;
    let debounceId = null;

    appShell.onReady(function () {
        openers.forEach(function (button) {
            button.addEventListener('click', function (event) {
                event.preventDefault();
                openOverlay();
            });
        });

        dismissers.forEach(function (button) {
            button.addEventListener('click', function (event) {
                event.preventDefault();
                closeOverlay();
            });
        });

        overlay.addEventListener('keydown', function (event) {
            if (event.key === 'Escape') {
                closeOverlay();
            }
        });

        form.addEventListener('submit', function (event) {
            event.preventDefault();
            const query = input.value.trim();
            if (query.length >= 2) {
                searchBooks(query);
            } else {
                showMessage('Nhập tối thiểu 2 ký tự để tìm kiếm sách.', false);
            }
        });

        input.addEventListener('input', function () {
            const query = input.value.trim();
            if (debounceId) {
                window.clearTimeout(debounceId);
            }
            if (query.length < 2) {
                resetResults();
                showMessage('Nhập tối thiểu 2 ký tự để tìm kiếm sách.', false);
                return;
            }
            debounceId = window.setTimeout(function () {
                searchBooks(query);
            }, 280);
        });
    });

    function openOverlay() {
        overlay.classList.remove('hidden');
        document.body.classList.add('overflow-hidden');
        input.focus();
        input.select();
        appShell.refreshIcons();
    }

    function closeOverlay() {
        overlay.classList.add('hidden');
        document.body.classList.remove('overflow-hidden');
        if (abortController) {
            abortController.abort();
            abortController = null;
        }
    }

    function showMessage(text, isError) {
        message.textContent = text;
        message.classList.toggle('text-red-600', !!isError);
        message.classList.toggle('text-gray-500', !isError);
        message.classList.toggle('hidden', false);
    }

    function resetResults() {
        resultsContainer.innerHTML = '';
        resultsContainer.classList.add('hidden');
    }

    function searchBooks(query) {
        if (abortController) {
            abortController.abort();
        }
        abortController = new AbortController();
        showMessage('Đang tìm kiếm “' + query + '”...', false);
        resultsContainer.classList.add('hidden');

        const url = appShell.booksApiBase + '/search?q=' + encodeURIComponent(query) + '&limit=15';
        fetch(url, {
            headers: {
                'Accept': 'application/json'
            },
            signal: abortController.signal
        })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('SEARCH_FAILED');
                }
                return response.json();
            })
            .then(function (payload) {
                const books = Array.isArray(payload.data) ? payload.data : [];
                if (books.length === 0) {
                    showMessage('Không tìm thấy sách khớp với từ khóa “' + query + '”.', false);
                    resultsContainer.innerHTML = '';
                    resultsContainer.classList.add('hidden');
                    return;
                }
                renderResults(books);
                showMessage('Tìm thấy ' + books.length + ' kết quả phù hợp.', false);
            })
            .catch(function (error) {
                if (error.name === 'AbortError') {
                    return;
                }
                console.error('Search error', error);
                showMessage('Không thể tìm kiếm lúc này. Vui lòng thử lại sau.', true);
                resultsContainer.classList.add('hidden');
            })
            .finally(function () {
                abortController = null;
            });
    }

    function renderResults(books) {
        const itemsHtml = books.map(function (book) {
            const title = appShell.escapeHtml(book.title || 'Chưa cập nhật');
            const author = appShell.escapeHtml(book.author || 'Đang cập nhật');
            const price = typeof book.price === 'number' ? appShell.formatCurrency(book.price) : 'Liên hệ';
            const totalSold = typeof book.totalSold === 'number' ? book.totalSold : 0;
            const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
            const ratingCount = typeof book.ratingCount === 'number' ? book.ratingCount : 0;
            const image = book.imageUrl || 'https://placehold.co/96x128?text=Book';
            const detailUrl = appShell.contextPath + '/books/detail?id=' + encodeURIComponent(book.id);
            return (
                '<a href="' + detailUrl + '" class="flex gap-4 px-6 py-4 hover:bg-amber-50 transition items-center" role="listitem">' +
                    '<img src="' + image + '" alt="' + title + '" class="w-16 h-20 object-cover rounded-md border border-gray-200" loading="lazy">' +
                    '<div class="flex-1 min-w-0">' +
                        '<h3 class="font-semibold text-gray-800 truncate">' + title + '</h3>' +
                        '<p class="text-sm text-gray-500 truncate">' + author + '</p>' +
                        '<div class="mt-2 flex flex-wrap items-center gap-3 text-xs text-gray-500">' +
                            '<span class="text-amber-700 font-semibold text-sm">' + price + '</span>' +
                            '<span>' + totalSold + ' đã bán</span>' +
                            '<span>' + rating + ' ★ (' + ratingCount + ')</span>' +
                        '</div>' +
                    '</div>' +
                '</a>'
            );
        }).join('');
        resultsContainer.innerHTML = itemsHtml;
        resultsContainer.classList.remove('hidden');
        appShell.refreshIcons();
    }
})(window, document);
