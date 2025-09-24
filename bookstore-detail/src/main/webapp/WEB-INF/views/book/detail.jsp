<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${book.title}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .wrap { display: grid; grid-template-columns: 260px 1fr; gap: 24px; }
        .price { font-size: 20px; font-weight: bold; }
        .stock { color: #0a7f15; }
        .card { border: 1px solid #eee; padding: 12px; border-radius: 8px; }
        .grid { display: grid; grid-template-columns: repeat(auto-fill,minmax(160px,1fr)); gap: 12px; }
        .muted { color: #666; font-size: 14px; }
        .badge { background: #f2f2f2; padding: 2px 8px; border-radius: 8px; }
    </style>
</head>
<body>
<h2>${book.title}</h2>
<div class="wrap">
    <div>
        <img src="${book.image}" width="240" alt="${book.title}"/>
    </div>
    <div>
        <p><span class="badge">Tác giả</span> ${book.author}</p>
        <p class="price"><c:out value="${book.price}"/> đ</p>
        <p class="stock">Tồn kho: <c:out value="${book.stock}"/></p>
        <div class="card">
            <b>Mô tả</b>
            <p class="muted"><c:out value="${book.description}"/></p>
        </div>

        <h3>Gợi ý cùng thể loại</h3>
        <div class="grid">
            <c:forEach var="b" items="${sameCategory}">
                <div class="card">
                    <a href="/book/${b.id}"><img src="${b.image}" width="120" /></a>
                    <div><a href="/book/${b.id}">${b.title}</a></div>
                    <div class="muted">${b.author}</div>
                </div>
            </c:forEach>
            <c:if test="${empty sameCategory}"><div class="muted">Không có.</div></c:if>
        </div>

        <h3>Gợi ý cùng tác giả</h3>
        <div class="grid">
            <c:forEach var="b" items="${sameAuthor}">
                <div class="card">
                    <a href="/book/${b.id}"><img src="${b.image}" width="120" /></a>
                    <div><a href="/book/${b.id}">${b.title}</a></div>
                </div>
            </c:forEach>
            <c:if test="${empty sameAuthor}"><div class="muted">Không có.</div></c:if>
        </div>

        <h3>Đánh giá</h3>
        <c:forEach var="r" items="${reviews}">
            <p>⭐ ${r.rating} - <span class="muted">${r.createdAt}</span><br/>${r.comment}</p>
            <hr/>
        </c:forEach>
        <c:if test="${empty reviews}"><p class="muted">Chưa có đánh giá.</p></c:if>

        <form action="/book/${book.id}/review" method="post" class="card">
            <label>Rating (1-5):</label>
            <input type="number" name="rating" min="1" max="5" required />
            <br/>
            <label>Bình luận:</label><br/>
            <textarea name="comment" rows="3" cols="60" required></textarea><br/>
            <button type="submit">Gửi đánh giá</button>
        </form>
    </div>
</div>
</body>
</html>
