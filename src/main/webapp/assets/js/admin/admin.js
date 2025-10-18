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
            if (parent) parent.classList.add("active");
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

    console.log("Admin panel ready.");
});
