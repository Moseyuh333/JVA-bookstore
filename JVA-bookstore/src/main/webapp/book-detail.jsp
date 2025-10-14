<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container my-4">
  <h2>${book.title}</h2>
  <p><strong>Tác giả:</strong> ${book.author}</p>
  <p><strong>Thể loại:</strong> ${book.category}</p>
  <p><strong>Giá:</strong> <fmt:formatNumber value="${book.price}" type="currency" currencySymbol="₫"/></p>
  <p><strong>Tồn kho:</strong> ${book.stock}</p>

  <div class="mt-3">
    <img src="assets/img/${book.coverImage}" class="img-fluid rounded" style="max-height:300px;">
  </div>
  <p class="mt-3">${book.description}</p>

  <c:if test="${reviewCount > 0}">
    <p><span class="badge bg-warning">★ ${avgRating}/5 (${reviewCount} đánh giá)</span></p>
  </c:if>

  <hr/>
  <h4>Gợi ý sách liên quan</h4>
  <div class="row">
    <c:forEach var="rb" items="${relatedBooks}">
      <div class="col-md-3 mb-3">
        <div class="card h-100">
          <img src="assets/img/${rb.coverImage}" class="card-img-top" style="height:180px;">
          <div class="card-body">
            <h6 class="card-title">${rb.title}</h6>
            <p class="text-muted">${rb.author}</p>
            <fmt:formatNumber value="${rb.price}" type="currency" currencySymbol="₫"/>
          </div>
          <a href="book?id=${rb.id}" class="stretched-link"></a>
        </div>
      </div>
    </c:forEach>
  </div>

  <hr id="reviews"/>
  <h4>Đánh giá sách</h4>

  <form action="review" method="post" class="border rounded p-3 mb-4">
    <input type="hidden" name="bookId" value="${book.id}">
    <div class="mb-3">
      <label>Tên của bạn:</label>
      <input type="text" name="authorName" class="form-control">
    </div>
    <div class="mb-3">
      <label>Đánh giá (1–5):</label>
      <select name="rating" class="form-select">
        <c:forEach var="r" begin="1" end="5">
          <option value="${r}">${r}</option>
        </c:forEach>
      </select>
    </div>
    <div class="mb-3">
      <label>Nhận xét:</label>
      <textarea name="comment" class="form-control" rows="3"></textarea>
    </div>
    <button class="btn btn-success">Gửi đánh giá</button>
  </form>

  <c:if test="${empty reviews}">
    <p class="text-muted">Chưa có đánh giá nào.</p>
  </c:if>

  <ul class="list-group">
    <c:forEach var="rv" items="${reviews}">
      <li class="list-group-item">
        <strong>${rv.authorName}</strong> - 
        <span class="text-warning">★ ${rv.rating}/5</span><br/>
        <small class="text-muted"><fmt:formatDate value="${rv.createdAt}" pattern="dd/MM/yyyy HH:mm"/></small>
        <p>${rv.comment}</p>
      </li>
    </c:forEach>
  </ul>
</div>
