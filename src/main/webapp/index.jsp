<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <script>
        window.appConfig = {
            contextPath: '<%=request.getContextPath()%>'
        };
    </script>
    <script src="<%=request.getContextPath()%>/assets/js/app-shell.js"></script>
    <script src="<%=request.getContextPath()%>/assets/js/home-page.js"></script>
                            <i data-feather="mail" class="w-5 h-5 mr-2"></i>
                            <a href="mailto:info@bookishhaven.com" class="hover:text-white">info@bookishhaven.com</a>
                        </div>
                        <div class="flex items-center">
                            <i data-feather="phone" class="w-5 h-5 mr-2"></i>
                            <a href="tel:+84901234567" class="hover:text-white">0901 234 567</a>
                        </div>
                    </address>
                </div>
            </div>
        </div>
    </footer>

    <script>
        const contextPath = '<%=request.getContextPath()%>';
        const booksApiBase = `${contextPath}/api/books`;

        document.addEventListener('DOMContentLoaded', () => {
            feather.replace();
            initUserDropdown();
            loadHomeSections();
            updateYearBadge();
        });

        async function loadHomeSections() {
            const container = document.getElementById('homeSectionsContainer');
            const loading = document.getElementById('homeSectionsLoading');
            if (!container) {
                return;
            }
            try {
                const response = await fetch(`${booksApiBase}/sections?limit=8`);
                if (!response.ok) {
                    throw new Error('Failed to load sections');
                }
                const payload = await response.json();
                container.innerHTML = '';
                if (payload.sections && payload.sections.length > 0) {
                    payload.sections.forEach(section => {
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
                feather.replace();
            }
        }

        function renderSection(section) {
            const wrapper = document.createElement('div');
            wrapper.className = 'space-y-6';
            const safeBooks = Array.isArray(section.books) ? section.books.slice(0, 4) : [];
            const sortKey = encodeURIComponent(section.sort || 'new');
            const cardsHtml = safeBooks.length > 0 ? safeBooks.map(renderBookCard).join('') : renderSkeletonCards();
            wrapper.innerHTML = `
                <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <h3 class="title-font text-2xl font-bold">${section.title || 'Danh mục'}</h3>
                        <p class="text-gray-500 text-sm">Những tựa sách nổi bật được độc giả quan tâm</p>
                    </div>
                    <a href="${contextPath}/catalog.jsp?sort=${sortKey}" class="inline-flex items-center text-amber-700 hover:text-amber-900 text-sm font-medium">
                        Xem tất cả
                        <i data-feather="arrow-right" class="w-4 h-4 ml-1"></i>
                    </a>
                </div>
                <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                    ${cardsHtml}
                </div>
            `;
            return wrapper;
        }

        function renderBookCard(book) {
            const title = book.title || 'Sách chưa cập nhật';
            const author = book.author || 'Đang cập nhật';
            const price = formatCurrency(book.price);
            const image = book.imageUrl || 'https://placehold.co/320x420?text=Book';
            const rating = typeof book.averageRating === 'number' ? book.averageRating.toFixed(1) : '0.0';
            const ratingCount = book.ratingCount || 0;
            return `
                <div class="book-card bg-white rounded-xl overflow-hidden shadow-sm border border-gray-100 transition duration-300 flex flex-col">
                    <div class="relative">
                        <img src="${image}" alt="${escapeHtml(title)}" class="w-full h-56 object-cover">
                        <span class="absolute top-3 left-3 bg-white/90 text-amber-700 text-xs font-semibold px-2 py-1 rounded-full shadow-sm">
                            ${rating} ★ (${ratingCount})
                        </span>
                    </div>
                    <div class="p-5 flex flex-col flex-grow">
                        <h4 class="title-font font-semibold text-lg mb-1">${escapeHtml(title)}</h4>
                        <p class="text-gray-500 text-sm mb-3">${escapeHtml(author)}</p>
                        <p class="text-amber-700 font-bold mb-4">${price}</p>
                        <div class="mt-auto flex flex-col gap-2">
                            <button type="button" class="bg-amber-600 hover:bg-amber-700 text-white font-medium py-2 px-4 rounded-full text-sm transition" data-book-id="${book.id}">
                                Thêm vào giỏ
                            </button>
                            <a href="${contextPath}/catalog.jsp?highlight=${book.id}" class="text-center text-sm text-amber-700 hover:text-amber-900 font-medium">
                                Xem chi tiết
                            </a>
                        </div>
                    </div>
                </div>
            `;
        }

        function renderSkeletonCards() {
            return Array.from({ length: 4 }).map(() => `
                <div class="bg-white border border-dashed border-amber-200 rounded-xl h-56 flex items-center justify-center text-amber-400 text-sm">
                    Đang cập nhật
                </div>
            `).join('');
        }

        function renderEmptyState() {
            return `
                <div class="text-center py-16 bg-white rounded-xl border border-dashed border-amber-200 text-gray-500">
                    Chưa có dữ liệu sách để hiển thị. Hãy thêm sách trong kho dữ liệu.
                </div>
            `;
        }

        function formatCurrency(value) {
            if (value === null || value === undefined) {
                return 'Liên hệ';
            }
            try {
                return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
            } catch (error) {
                return value.toString();
            }
        }

        function escapeHtml(text) {
            if (!text) {
                return '';
            }
            return text.replace(/[&<>"']/g, function(match) {
                switch (match) {
                    case '&': return '&amp;';
                    case '<': return '&lt;';
                    case '>': return '&gt;';
                    case '"': return '&quot;';
                    case "'": return '&#39;';
                    default: return match;
                }
            });
        }

        function initUserDropdown() {
            const userDropdownBtn = document.getElementById('userDropdownBtn');
            const userDropdown = document.getElementById('userDropdown');
            const token = localStorage.getItem('auth_token');
            const username = getStoredUsername(token);
            const isLoggedIn = token && token.length > 0;

            if (isLoggedIn) {
                updateDropdownForLoggedInUser(username);
            } else {
                updateDropdownForGuestUser();
            }

            if (userDropdownBtn && userDropdown) {
                userDropdownBtn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    userDropdown.classList.toggle('hidden');
                });

                document.addEventListener('click', function() {
                    userDropdown.classList.add('hidden');
                });

                userDropdown.addEventListener('click', function(e) {
                    e.stopPropagation();
                });
            }
        }

        function updateDropdownForLoggedInUser(username) {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) {
                const safeUsername = username && username.trim().length > 0 ? escapeHtml(username.trim()) : null;
                const greeting = safeUsername ? 'Xin chào, ' + safeUsername + '!' : 'Xin chào!';
                userDropdown.innerHTML =
                    '<div class="py-2">' +
                        '<div class="px-4 py-2 text-sm text-gray-600 border-b flex items-center gap-2">' +
                            '<i data-feather="user" class="w-4 h-4"></i>' +
                            '<span>' + greeting + '</span>' +
                        '</div>' +
                        '<a href="' + contextPath + '/profile.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">' +
                            '<i data-feather="settings" class="w-4 h-4 mr-2"></i>' +
                            'Hồ sơ cá nhân' +
                        '</a>' +
                        '<a href="#" onclick="logout(); return false;" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">' +
                            '<i data-feather="log-out" class="w-4 h-4 mr-2"></i>' +
                            'Đăng xuất' +
                        '</a>' +
                    '</div>';
                const lbl = document.getElementById('accountBtnLabel');
                if (lbl) {
                    lbl.textContent = safeUsername || 'Tài khoản';
                }
                feather.replace();
            }
        }

        function updateDropdownForGuestUser() {
            const userDropdown = document.getElementById('userDropdown');
            if (userDropdown) {
                userDropdown.innerHTML = `
                    <div class="py-2">
                        <a href="${contextPath}/login.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="log-in" class="w-4 h-4 mr-2"></i>
                            Đăng nhập
                        </a>
                        <a href="${contextPath}/register.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="user-plus" class="w-4 h-4 mr-2"></i>
                            Đăng ký
                        </a>
                        <hr class="my-1">
                        <a href="${contextPath}/forgot-password.jsp" class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                            <i data-feather="key" class="w-4 h-4 mr-2"></i>
                            Quên mật khẩu
                        </a>
                    </div>
                `;
                const lbl = document.getElementById('accountBtnLabel');
                if (lbl) {
                    lbl.textContent = 'Tài khoản';
                }
                feather.replace();
            }
        }

        function logout() {
            localStorage.removeItem('auth_token');
            localStorage.removeItem('auth_username');
            updateDropdownForGuestUser();
            alert('Đăng xuất thành công!');
            window.location.reload();
        }

        function getStoredUsername(token) {
            const cached = localStorage.getItem('auth_username');
            if (cached && cached.trim().length > 0) {
                return cached.trim();
            }
            if (!token) {
                return null;
            }
            try {
                const payloadPart = token.split('.')[1];
                if (!payloadPart) {
                    return null;
                }
                const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
                const padded = normalized + '='.repeat((4 - normalized.length % 4) % 4);
                const payload = JSON.parse(atob(padded));
                const subject = payload && typeof payload.sub === 'string' ? payload.sub.trim() : null;
                if (subject && subject.length > 0) {
                    localStorage.setItem('auth_username', subject);
                    return subject;
                }
            } catch (error) {
                console.warn('Không thể đọc username từ token', error);
            }
            return null;
        }

        function updateYearBadge() {
            const badge = document.getElementById('year');
            if (badge) {
                badge.textContent = new Date().getFullYear();
            }
        }
    </script>
</body>
</html>
