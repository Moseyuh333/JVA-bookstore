<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.Normalizer"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Locale"%>
<%@ page import="dao.BookDAO"%>
<%!
    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return normalized;
    }

    private String findMatchingCategory(List<String> categories, String... keywords) {
        if (categories == null || categories.isEmpty() || keywords == null) {
            return null;
        }
        for (String category : categories) {
            String normalizedCategory = normalizeText(category);
            for (String keyword : keywords) {
                if (normalizedCategory.contains(normalizeText(keyword))) {
                    return category;
                }
            }
        }
        return null;
    }
%>
<%
    String ctx = request.getContextPath();
    List<String> rawCategories;
    try {
        rawCategories = new ArrayList<>(BookDAO.getAllCategories());
    } catch (Exception ex) {
        rawCategories = new ArrayList<>();
    }

    String novelRaw = findMatchingCategory(rawCategories, "tiá»ƒu thuyáº¿t", "tieu thuyet", "fiction", "van hoc");
    if (novelRaw != null) rawCategories.remove(novelRaw);

    String nonFictionRaw = findMatchingCategory(rawCategories, "phi tiá»ƒu thuyáº¿t", "phi tieu thuyet", "non-fiction", "ki nang", "chuyen de");
    if (nonFictionRaw != null) rawCategories.remove(nonFictionRaw);

    String giftRaw = findMatchingCategory(rawCategories, "quÃ ", "gift", "tháº»", "voucher");
    if (giftRaw != null) rawCategories.remove(giftRaw);

    if (novelRaw == null && !rawCategories.isEmpty()) {
        novelRaw = rawCategories.remove(0);
    }
    if (nonFictionRaw == null && !rawCategories.isEmpty()) {
        nonFictionRaw = rawCategories.remove(0);
    }
    if (giftRaw == null && !rawCategories.isEmpty()) {
        giftRaw = rawCategories.remove(0);
    }

    String catNovel = novelRaw != null ? URLEncoder.encode(novelRaw, "UTF-8") : null;
    String catNonFiction = nonFictionRaw != null ? URLEncoder.encode(nonFictionRaw, "UTF-8") : null;
    String catGifts = giftRaw != null ? URLEncoder.encode(giftRaw, "UTF-8") : null;
%>
<footer class="mt-auto bg-gray-900 text-gray-300 py-12 px-4">
    <div class="container mx-auto">
        <div class="flex justify-center mb-10">
            <span class="inline-flex items-center gap-2 bg-gray-800 text-amber-200 px-4 py-2 rounded-full text-sm shadow-sm">
                <i data-feather="shield" class="w-4 h-4"></i>
                <span>&copy; <span id="year"></span> Bookish Bliss Haven â€“ Má»i quyá»n Ä‘Æ°á»£c báº£o lÆ°u</span>
            </span>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
            <div>
                <h3 class="title-font text-white text-xl font-bold mb-4">Bookish Bliss Haven</h3>
                <p class="mb-4">Nguá»“n sÃ¡ch cháº¥t lÆ°á»£ng vÃ  cáº£m há»©ng vÄƒn há»c Ä‘Ã¡ng tin cáº­y cá»§a báº¡n.</p>
                <div class="flex space-x-4">
                    <a href="https://www.facebook.com/bookishblisshaven" target="_blank" rel="noopener" class="hover:text-white inline-flex items-center gap-1">
                        <i data-feather="facebook" class="w-5 h-5"></i>
                        <span class="sr-only">Facebook</span>
                    </a>
                    <a href="https://x.com/bookishblissvn" target="_blank" rel="noopener" class="hover:text-white inline-flex items-center gap-1">
                        <i data-feather="twitter" class="w-5 h-5"></i>
                        <span class="sr-only">Twitter</span>
                    </a>
                    <a href="https://www.instagram.com/bookishblisshaven" target="_blank" rel="noopener" class="hover:text-white inline-flex items-center gap-1">
                        <i data-feather="instagram" class="w-5 h-5"></i>
                        <span class="sr-only">Instagram</span>
                    </a>
                </div>
            </div>
            <div>
                <h4 class="text-white font-bold mb-4">Mua sáº¯m</h4>
                <ul class="space-y-2">
                    <li><a href="<%=ctx%>/catalog.jsp?sort=new" class="hover:text-white">SÃ¡ch má»›i</a></li>
                    <li><a href="<%=ctx%>/catalog.jsp?sort=best" class="hover:text-white">SÃ¡ch bÃ¡n cháº¡y</a></li>
                    <li><a href="<%=catNovel != null ? ctx + \"/catalog.jsp?category=\" + catNovel : ctx + \"/catalog.jsp\"%>" class="hover:text-white">Tiá»ƒu thuyáº¿t</a></li>
                    <li><a href="<%=catNonFiction != null ? ctx + \"/catalog.jsp?category=\" + catNonFiction : ctx + \"/catalog.jsp\"%>" class="hover:text-white">Phi tiá»ƒu thuyáº¿t</a></li>
                    <li><a href="<%=catGifts != null ? ctx + \"/catalog.jsp?category=\" + catGifts : ctx + \"/catalog.jsp\"%>" class="hover:text-white">Tháº» quÃ  táº·ng</a></li>
                </ul>
            </div>
            <div>
                <h4 class="text-white font-bold mb-4">Há»— trá»£</h4>
                <ul class="space-y-2">
                    <li><a href="<%=ctx%>/support.jsp#faq" class="hover:text-white">CÃ¢u há»i thÆ°á»ng gáº·p</a></li>
                    <li><a href="<%=ctx%>/support.jsp#shipping" class="hover:text-white">Váº­n chuyá»ƒn</a></li>
                    <li><a href="<%=ctx%>/support.jsp#returns" class="hover:text-white">Äá»•i tráº£</a></li>
                    <li>
                        <button type="button" data-support-chat-open class="hover:text-white focus:outline-none focus-visible:ring-2 focus-visible:ring-amber-500 rounded">
                            LiÃªn há»‡ trá»±c tuyáº¿n
                        </button>
                    </li>
                    <li><a href="<%=ctx%>/support.jsp#privacy" class="hover:text-white">ChÃ­nh sÃ¡ch báº£o máº­t</a></li>
                </ul>
            </div>
            <div>
                <h4 class="text-white font-bold mb-4">LiÃªn há»‡</h4>
                <address class="not-italic space-y-2">
                    <div class="flex items-start">
                        <i data-feather="map-pin" class="w-5 h-5 mr-2 mt-0.5"></i>
                        <span>123 ÄÆ°á»ng VÄƒn Há»c, Quáº­n SÃ¡ch, TP.HCM</span>
                    </div>
                    <div class="flex items-center">
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

<div id="cartDrawerOverlay" class="fixed inset-0 bg-black/50 hidden z-[60]"></div>
<aside id="cartDrawer" class="fixed inset-y-0 right-0 w-full max-w-md bg-white shadow-2xl hidden z-[70] flex flex-col">
    <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <div>
            <h2 class="text-lg font-semibold text-gray-800">Giá» hÃ ng cá»§a báº¡n</h2>
            <p class="text-sm text-gray-500">Kiá»ƒm tra sáº£n pháº©m trÆ°á»›c khi thanh toÃ¡n</p>
        </div>
        <button type="button" data-cart-close class="inline-flex items-center justify-center w-9 h-9 rounded-full border border-gray-200 text-gray-500 hover:text-gray-800 hover:border-gray-400 transition">
            <i data-feather="x" class="w-4 h-4"></i>
            <span class="sr-only">ÄÃ³ng giá» hÃ ng</span>
        </button>
    </div>
    <div class="flex-1 overflow-y-auto px-6 py-5 space-y-5" data-cart-scroll>
        <div data-cart-loading class="text-center text-sm text-gray-500 py-10">Äang táº£i giá» hÃ ng...</div>
        <div data-cart-empty class="hidden text-center py-12 text-gray-500">
            <i data-feather="shopping-bag" class="mx-auto mb-3 w-8 h-8 text-amber-600"></i>
            <p class="font-medium">Giá» hÃ ng cá»§a báº¡n Ä‘ang trá»‘ng.</p>
            <p class="text-sm text-gray-400">HÃ£y thÃªm má»™t vÃ i cuá»‘n sÃ¡ch Ä‘á»ƒ tiáº¿p tá»¥c.</p>
        </div>
        <div data-cart-items class="hidden space-y-4"></div>
    </div>
    <div class="px-6 py-4 border-t border-gray-200 space-y-3">
        <div class="flex items-center justify-between text-sm text-gray-600">
            <span>Tá»•ng sáº£n pháº©m</span>
            <span data-cart-count>0</span>
        </div>
        <div class="flex items-center justify-between text-lg font-semibold text-amber-700">
            <span>Táº¡m tÃ­nh</span>
            <span data-cart-subtotal>0&nbsp;â‚«</span>
        </div>
        <div data-cart-feedback class="hidden text-sm"></div>
        <div class="flex items-center justify-between gap-3">
            <button type="button" data-cart-clear class="flex-1 px-4 py-3 rounded-full border border-gray-300 text-sm font-medium text-gray-600 hover:bg-gray-100 transition">XÃ³a giá»</button>
            <button type="button" data-cart-checkout class="flex-1 px-4 py-3 rounded-full bg-amber-600 text-white font-semibold hover:bg-amber-700 transition disabled:opacity-50 disabled:cursor-not-allowed">Thanh toÃ¡n</button>
        </div>
    </div>
</aside>

<button type="button" id="supportChatFab" data-support-chat-open
        class="fixed bottom-6 right-6 z-[85] inline-flex items-center gap-2 rounded-full bg-amber-600 text-white px-4 py-3 shadow-xl hover:bg-amber-700 transition">
    <i data-feather="message-circle" class="w-5 h-5"></i>
    <span>Há»— trá»£</span>
</button>

<div id="supportChatPanel" class="hidden fixed bottom-24 right-6 z-[90] w-full max-w-sm bg-white border border-amber-100 rounded-2xl shadow-2xl overflow-hidden flex flex-col">
    <div class="px-5 py-4 bg-amber-600 text-white flex items-start justify-between gap-4">
        <div>
            <h3 class="text-lg font-semibold">Há»— trá»£ khÃ¡ch hÃ ng</h3>
            <p class="text-sm text-amber-100/80">ChÃºng tÃ´i pháº£n há»“i trong giá» hÃ nh chÃ­nh.</p>
        </div>
        <button type="button" data-support-chat-close class="inline-flex items-center justify-center w-9 h-9 rounded-full border border-amber-200/40 text-white/90 hover:text-white hover:bg-amber-500 transition">
            <i data-feather="x" class="w-4 h-4"></i>
            <span class="sr-only">ÄÃ³ng cá»­a sá»• há»— trá»£</span>
        </button>
    </div>
    <div id="supportChatMessages" class="flex-1 overflow-y-auto bg-amber-50/40 px-4 py-3 space-y-3 text-sm"></div>
    <div class="border-t border-amber-100 bg-white px-4 py-3">
        <form id="supportChatForm" class="space-y-2">
            <label class="sr-only" for="supportChatInput">Ná»™i dung há»— trá»£</label>
            <textarea id="supportChatInput" class="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 resize-none" rows="3" placeholder="Nháº­p cÃ¢u há»i cá»§a báº¡n..."></textarea>
            <div class="flex items-center justify-between gap-3">
                <span id="supportChatStatus" class="text-xs text-gray-500"></span>
                <button type="submit" class="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-amber-600 text-white text-sm font-medium hover:bg-amber-700 transition disabled:opacity-60 disabled:cursor-not-allowed">
                    <i data-feather="send" class="w-4 h-4"></i>
                    <span>Gá»­i</span>
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    window.appConfig = window.appConfig || {};
    window.appConfig.contextPath = '<%=ctx%>';
</script>
<script src="<%=ctx%>/assets/js/app-shell.js"></script>
<script src="<%=ctx%>/assets/js/global-search.js"></script>
<script src="<%=ctx%>/assets/js/api-client.js"></script>
<script src="<%=ctx%>/assets/js/cart-client.js"></script>
<script src="<%=ctx%>/assets/js/cart-ui.js"></script>
<script src="<%=ctx%>/assets/js/support-chat.js"></script>
