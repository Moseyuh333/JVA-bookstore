// ================================
// 📂 admin.js - Script chung cho Admin Panel
// ================================

document.addEventListener("DOMContentLoaded", () => {
    console.log("admin.js loaded");

    // Feather icons
    if (typeof feather !== "undefined") {
        feather.replace();
    }

    // Sidebar toggle (ẩn/hiện khi click menu)
    const sidebarToggle = document.getElementById("sidebarToggle");
    const sidebar = document.getElementById("accordionSidebar");
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener("click", (e) => {
            e.preventDefault();
            document.body.classList.toggle("sidebar-toggled");
            sidebar.classList.toggle("toggled");
        });
    }

    // Highlight menu active
    const currentPath = window.location.pathname;
    document.querySelectorAll(".nav-item a.nav-link").forEach((link) => {
        if (link.href.includes(currentPath)) {
            link.classList.add("active");
            const parent = link.closest(".nav-item");
            if (parent) {
                parent.classList.add("active");
            }
        }
    });

    // Scroll-to-top button
    const btnScrollTop = document.getElementById("btnScrollTop");
    if (btnScrollTop) {
        window.addEventListener("scroll", () => {
            btnScrollTop.style.display = window.scrollY > 300 ? "block" : "none";
        });

        btnScrollTop.addEventListener("click", () => {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
    }

    // Tooltip (nếu dùng Bootstrap)
    if (window.$ && $.fn.tooltip) {
        $('[data-toggle="tooltip"]').tooltip();
    }

    // Collapsible submenu (dropdown)
    document.querySelectorAll(".nav-item.has-dropdown > a").forEach((item) => {
        item.addEventListener("click", (e) => {
            e.preventDefault();
            const submenu = item.nextElementSibling;
            submenu.classList.toggle("show");
            item.classList.toggle("open");
        });
    });

    // Admin Dropdown Toggle
    const contextPath = window.appConfig?.contextPath || "";
    const adminDropdownBtn = document.getElementById("adminDropdownBtn");
    const adminDropdown = document.getElementById("adminDropdown");
    let dropdownInitialized = false;

    function refreshIcons() {
        if (window.feather && typeof window.feather.replace === "function") {
            window.feather.replace();
        }
    }

    function escapeHtml(text) {
        if (!text) return "";
        return text.replace(/[&<>"']/g, (m) =>
            ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[m])
        );
    }

    function readJwtSubject(token) {
        if (!token) return null;
        try {
            const payload = JSON.parse(atob(token.split(".")[1]));
            return payload?.sub || null;
        } catch {
            return null;
        }
    }

    function getStoredUsername(token) {
        const cached = localStorage.getItem("admin_username");
        if (cached) return cached;
        const subject = readJwtSubject(token);
        if (subject) localStorage.setItem("admin_username", subject);
        return subject;
    }

    // 🔹 Hiển thị dropdown khi chưa đăng nhập
    function renderGuestDropdown(container) {
        container.innerHTML = `
            <div class="py-2">
                <a href="${contextPath}/login.jsp"
                    class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="log-in" class="w-4 h-4 mr-2"></i> Đăng nhập
                </a>
                <a href="${contextPath}/register.jsp"
                    class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="user-plus" class="w-4 h-4 mr-2"></i> Đăng ký
                </a>
                <hr class="my-1">
                <a href="${contextPath}/forgot-password.jsp"
                    class="flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="key" class="w-4 h-4 mr-2"></i> Quên mật khẩu
                </a>
            </div>`;
        refreshIcons();
    }

    // 🔹 Hiển thị dropdown khi admin đã đăng nhập
    function renderAdminDropdown(container, username) {
        const safeName = username ? escapeHtml(username.trim()) : "Admin";
        const greeting = safeName ? 'Xin chào, ' + safeName + '!' : 'Xin chào!';
        container.innerHTML = `
            <div class="py-2">
                <div class="px-4 py-2 text-sm text-gray-600 border-b flex items-center gap-2">
                    <i data-feather="user" class="w-4 h-4"></i>
                    <span>${greeting}</span>
                </div>
                <button type="button" data-action="logout"
                    class="w-full text-left flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                    <i data-feather="log-out" class="w-4 h-4 mr-2"></i> Đăng xuất
                </button>
            </div>`;
        refreshIcons();
    }

    function handleLogout(container) {
        localStorage.removeItem("admin_token");
        localStorage.removeItem("admin_username");
        renderGuestDropdown(container);
        container.classList.add("hidden");
        window.location.href = contextPath + "/login.jsp";
    }

    function initAdminDropdown() {
        if (dropdownInitialized) return;
        dropdownInitialized = true;

        if (!adminDropdownBtn || !adminDropdown) return;

        const token = localStorage.getItem("admin_token");
        const username = getStoredUsername(token);

        if (token) {
            renderAdminDropdown(adminDropdown, username);
        } else {
            renderGuestDropdown(adminDropdown);
        }

        adminDropdownBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            adminDropdown.classList.toggle("hidden");
        });

        document.addEventListener("click", (e) => {
            if (
                !adminDropdown.contains(e.target) &&
                !adminDropdownBtn.contains(e.target)
            ) {
                adminDropdown.classList.add("hidden");
            }
        });

        adminDropdown.addEventListener("click", (e) => {
            if (e.target.closest("[data-action='logout']")) {
                e.preventDefault();
                handleLogout(adminDropdown);
            }
            e.stopPropagation();
        });

        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                adminDropdown.classList.add("hidden");
            }
        });
    }

    initAdminDropdown();

    // Initialize Feather Icons
    feather.replace();
    console.log("⚙️ Admin dropdown initialized");
});