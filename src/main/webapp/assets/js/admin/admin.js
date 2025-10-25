// ================================
// 📂 admin.js - Script chung cho Admin Panel
// ================================

document.addEventListener("DOMContentLoaded", () => {
    console.log("✓ admin.js loaded");

    // Feather icons
    if (typeof feather !== "undefined") {
        feather.replace();
    }

    const contextPath = window.appConfig?.contextPath || "";

    // =====================
    // Admin Dropdown Handler
    // =====================
    function initAdminDropdown() {
        const adminDropdownBtn = document.getElementById("adminDropdownBtn");
        const adminDropdown = document.getElementById("adminDropdown");

        if (!adminDropdownBtn || !adminDropdown) {
            console.warn("❌ Dropdown elements not found");
            return;
        }

        console.log("✓ Dropdown elements found");

        // Helper functions
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

        function refreshIcons() {
            if (window.feather && typeof window.feather.replace === "function") {
                window.feather.replace();
            }
        }

        // Admin dropdown (đã đăng nhập)
        function renderAdminDropdown(username) {
            const safeName = username ? escapeHtml(username.trim()) : "Admin";
            adminDropdown.innerHTML = `
                <div class="py-2">
                    <div class="px-4 py-2 text-sm text-gray-600 border-b border-gray-200 flex items-center gap-2">
                        <i data-feather="user" class="w-4 h-4"></i>
                        <span>Xin chào, ${safeName}!</span>
                    </div>
                    <button type="button" data-action="logout" class="w-full text-left flex items-center px-4 py-2 text-gray-800 hover:bg-amber-50 hover:text-amber-800">
                        <i data-feather="log-out" class="w-4 h-4 mr-2"></i>
                        Đăng xuất
                    </button>
                </div>`;
            refreshIcons();
        }

        // ======================
        // Logout đầy đủ
        // ======================
        function handleLogout() {
            console.log("🚪 Logging out...");
            // Xóa tất cả token có thể
            localStorage.removeItem("admin_token");
            localStorage.removeItem("admin_username");
            localStorage.removeItem("auth_token");
            localStorage.removeItem("auth_role");
            localStorage.removeItem("auth_username");
            sessionStorage.clear();

            // Ẩn dropdown
            adminDropdown.classList.add("hidden");

            // Chuyển về login.jsp
            setTimeout(() => {
                window.location.href = contextPath + "/login.jsp";
            }, 300);
        }

        // ======================
        // 🔹 Khởi tạo Dropdown
        // ======================
        const token = localStorage.getItem("admin_token");
        const username = getStoredUsername(token);

        console.log("Token:", token ? "✓ exists" : "✗ none");
        console.log("Username:", username || "none");

        if (token && username) {
            renderAdminDropdown(username);
            console.log("✓ Rendered admin dropdown");
        } else {
            console.log("✗ No admin token, redirecting to login");
            window.location.href = contextPath + "/login.jsp";
        }

        // Toggle dropdown on button click
        adminDropdownBtn.addEventListener("click", (e) => {
            e.stopPropagation();
            adminDropdown.classList.toggle("hidden");
            console.log("Dropdown toggled");
        });

        // Close dropdown when clicking outside
        document.addEventListener("click", (e) => {
            if (!adminDropdown.contains(e.target) && !adminDropdownBtn.contains(e.target)) {
                adminDropdown.classList.add("hidden");
            }
        });

        // Handle logout button
        adminDropdown.addEventListener("click", (e) => {
            const logoutBtn = e.target.closest("[data-action='logout']");
            if (logoutBtn) {
                e.preventDefault();
                e.stopPropagation();
                console.log("Logout clicked");
                handleLogout();
            }
        });

        // Close on Escape key
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                adminDropdown.classList.add("hidden");
            }
        });

        console.log("✓ Admin dropdown initialized successfully");
    }

    // ======================
    // 🔹 Initialize dropdown
    // ======================
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initAdminDropdown);
    } else {
        initAdminDropdown();
    }

    // ======================
    // 🔹 Sidebar toggle
    // ======================
    const sidebarToggle = document.getElementById("sidebarToggle");
    const sidebar = document.getElementById("accordionSidebar");
    if (sidebarToggle && sidebar) {
        sidebarToggle.addEventListener("click", (e) => {
            e.preventDefault();
            document.body.classList.toggle("sidebar-toggled");
            sidebar.classList.toggle("toggled");
        });
    }

    // ======================
    // 🔹 Highlight menu active
    // ======================
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

    // ======================
    // 🔹 Scroll-to-top button
    // ======================
    const btnScrollTop = document.getElementById("btnScrollTop");
    if (btnScrollTop) {
        window.addEventListener("scroll", () => {
            btnScrollTop.style.display = window.scrollY > 300 ? "block" : "none";
        });

        btnScrollTop.addEventListener("click", () => {
            window.scrollTo({ top: 0, behavior: "smooth" });
        });
    }

    console.log("✓ Admin panel scripts initialized");
});
