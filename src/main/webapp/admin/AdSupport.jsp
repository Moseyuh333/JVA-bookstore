<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Ho tro khach hang - Bookish Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Roboto:wght@300;400;500;700&display=swap&subset=vietnamese" rel="stylesheet">
    <style>
        body { background: #f5f5f5; font-family: 'Roboto', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif; }
        .card-chat { border-radius: 18px; border: none; box-shadow: 0 12px 35px -18px rgba(15, 23, 42, 0.35); }
        .conversation-item { transition: background .15s ease; border-left: 4px solid transparent; }
        .conversation-item:hover { background: #fff7ed; }
        .conversation-item.active { background: #fef3c7; border-left-color: #d97706; }
        .message-bubble { border-radius: 18px; padding: 12px 16px; font-size: 0.92rem; line-height: 1.45; }
        .message-meta { font-size: 0.7rem; letter-spacing: 0.05em; margin-top: 6px; }
        .sticky-header { position: sticky; top: 0; z-index: 3; background: inherit; }
        .no-focus-outline:focus { box-shadow: none; }
    </style>
</head>
<body>

<div id="wrapper">
    <%@ include file="/WEB-INF/includes/admin/AdSideBar.jsp" %>

    <div id="content-wrapper">
        <%@ include file="/WEB-INF/includes/admin/header.jsp" %>

        <div id="content" class="py-4">
            <div class="container-fluid">
                <div class="d-flex flex-wrap align-items-center gap-3 mb-4">
                    <div>
                        <h1 class="h3 mb-1">Ho tro khach hang</h1>
                        <p class="text-muted small mb-0">Theo doi hoi thoai va phan hoi tin nhan cua khach hang.</p>
                    </div>
                    <div class="ms-auto">
                        <button type="button" class="btn btn-outline-secondary btn-sm" id="adminSupportRefreshBtn">
                            <i class="fas fa-rotate me-2"></i>Tai lai
                        </button>
                    </div>
                </div>

                <div class="row g-4">
                    <div class="col-lg-4">
                        <div class="card card-chat h-100">
                            <div class="card-header bg-white border-0 sticky-header">
                                <h5 class="card-title mb-0">Danh sach hoi thoai</h5>
                                <p class="text-muted small mb-0">Chon hoi thoai de xem chi tiet va tra loi.</p>
                            </div>
                            <div id="adminSupportConversations" class="list-group list-group-flush" style="max-height:70vh; overflow-y:auto;">
                                <div class="text-center text-muted small py-5" id="adminSupportConversationEmpty">
                                    Dang tai danh sach...
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="col-lg-8">
                        <div class="card card-chat h-100 d-flex flex-column">
                            <div class="card-header bg-white border-0 sticky-header">
                                <h5 class="card-title mb-1" id="adminSupportConversationTitle">Chon mot hoi thoai</h5>
                                <p class="text-muted small mb-0" id="adminSupportConversationMeta">Danh sach tin nhan se hien o day.</p>
                            </div>

                            <div id="adminSupportMessages" class="card-body bg-light overflow-auto" style="flex:1 1 auto;">
                                <div class="text-muted small">Chua co du lieu.</div>
                            </div>

                            <div class="card-footer bg-white border-0">
                                <form id="adminSupportReplyForm" class="d-flex flex-column gap-2">
                                    <textarea id="adminSupportReplyInput"
                                              class="form-control no-focus-outline"
                                              rows="3"
                                              placeholder="Nhap noi dung phan hoi..."
                                              disabled></textarea>
                                    <div class="d-flex justify-content-between align-items-center">
                                        <small class="text-muted" id="adminSupportFormStatus"></small>
                                        <button type="submit" class="btn btn-warning text-white">
                                            <i class="fas fa-paper-plane me-2"></i>Gui phan hoi
                                        </button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <%@ include file="/WEB-INF/includes/admin/footer.jsp" %>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="<%=ctx%>/assets/js/admin/AdSupportChat.js"></script>
</body>
</html>

