> [!NOTE]
> Hướng dẫn này được giữ lại làm tài liệu phụ. Quy trình cập nhật nhất nằm trong `README.md`.

# Quick Start Guide

## Tổng quan ngắn gọn

You now have:

## What's Already Done in Your Project

1. **Authentication System**
   - JWT-based user authentication
   - OTP email verification
   - Password reset flow
   - MailerToGo SMTP configured

2. **Database**
   - PostgreSQL with auto-migrations
   - Schema extended with all e-commerce tables

3. **Email System**
   - SMTP configured (noreply@nkbookstore.com)
   - Ready for order notifications

## Next Steps - Priority Order

### PHASE 1: Complete DAO Layer (2-3 days)

Create remaining DAO classes (copy the patterns from BookDAO/CartDAO):

**Files to create:**
```
src/main/java/dao/
├── OrderDAO.java           - Order CRUD, status updates
├── RatingDAO.java          - Ratings and reviews
├── CommentDAO.java         - Comments with media
├── WishlistDAO.java        - Add/remove favorites
├── ProductViewDAO.java     - Track recently viewed
├── DeliveryAddressDAO.java - Multiple shipping addresses
└── UserDAO.java            - User profile management
```

**Key methods needed:**

**OrderDAO:**
- `createOrder(userId, items, totalAmount, deliveryAddressId, paymentMethod)`
- `getOrdersByUserId(userId, limit, offset)`
- `getOrderById(orderId)`
- `updateOrderStatus(orderId, status)`
- `getOrdersByStatus(status)` - for admin

**RatingDAO:**
- `addRating(userId, bookId, rating, review, isVerifiedPurchase)`
- `getRatingsByBook(bookId, limit, offset)`
- `getUserRatingForBook(userId, bookId)`
- `updateRating(ratingId, rating, review)`
- `deleteRating(ratingId)`

**CommentDAO:**
- `addComment(bookId, userId, commentText, imageUrl, videoUrl, isVerifiedPurchase)`
- `getCommentsByBook(bookId, limit, offset)`
- `updateComment(commentId, commentText, imageUrl, videoUrl)`
- `deleteComment(commentId)`

**WishlistDAO:**
- `addToWishlist(userId, bookId)`
- `removeFromWishlist(userId, bookId)`
- `getWishlist(userId, limit, offset)`
- `isInWishlist(userId, bookId)`

**ProductViewDAO:**
- `recordView(userId, bookId)`
- `getRecentlyViewed(userId, limit)`
- `clearViewHistory(userId)`

**DeliveryAddressDAO:**
- `addAddress(userId, address)`
- `updateAddress(addressId, address)`
- `deleteAddress(addressId)`
- `getAddresses(userId)`
- `setDefaultAddress(userId, addressId)`

**UserDAO:**
- `updateProfile(userId, fullName, phone, birthDate)`
- `getUserById(userId)`

### PHASE 2: API Endpoints (3-4 days)

Create REST API servlets in `src/main/java/web/`:

**Homepage API** (`/api/books/home`)
```java
@WebServlet("/api/books/home")
public class HomeApiServlet extends HttpServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        int limit = 20;
        int offset = 0;
        
        Map<String, Object> data = new HashMap<>();
        data.put("newest", BookDAO.getNewestBooks(limit, offset));
        data.put("bestSelling", BookDAO.getBestSellingBooks(limit, offset));
        data.put("topRated", BookDAO.getTopRatedBooks(limit, offset));
        data.put("favorites", BookDAO.getFavoriteBooks(limit, offset));
        
        sendJsonResponse(resp, true, "Home data loaded", data);
    }
}
```

**Product API** (`/api/books/*`)
```
GET /api/books                   - List all books with pagination
GET /api/books/{id}              - Get single book with ratings/comments
GET /api/books/category/{cat}    - Books by category
GET /api/books/search?q=keyword  - Search books
```

**Cart API** (`/api/cart/*`)
```
POST /api/cart/add               - Add to cart
POST /api/cart/remove/{itemId}   - Remove from cart
PUT /api/cart/update/{itemId}    - Update quantity
GET /api/cart                    - Get cart contents
GET /api/cart/total              - Get cart total
DELETE /api/cart/clear           - Clear cart
```

**Orders API** (`/api/orders/*`)
```
POST /api/orders                 - Create order
GET /api/orders                  - User's orders
GET /api/orders/{id}             - Order details
PUT /api/orders/{id}/status      - Update status
POST /api/orders/{id}/cancel     - Cancel order
POST /api/orders/{id}/return     - Return order
```

**Payment API** (`/api/payment/*`)
```
POST /api/payment/validate-coupon
POST /api/payment/checkout
POST /api/payment/cod
POST /api/payment/vnpay/create
POST /api/payment/vnpay/return
POST /api/payment/momo/create
POST /api/payment/momo/return
```

**Ratings API** (`/api/ratings/*`)
```
POST /api/books/{id}/ratings     - Add rating
GET /api/books/{id}/ratings      - Get ratings
PUT /api/ratings/{id}            - Update rating
DELETE /api/ratings/{id}         - Delete rating
```

**Comments API** (`/api/comments/*`)
```
POST /api/books/{id}/comments    - Add comment
GET /api/books/{id}/comments     - Get comments
PUT /api/comments/{id}           - Edit comment
DELETE /api/comments/{id}        - Delete comment
```

### PHASE 3: JSP Frontend Views (4-5 days)

Create responsive Bootstrap 5 pages:

```
src/main/webapp/
├── index.jsp                 - Homepage with 4 product carousels
├── category.jsp              - Category page with filters
├── product-detail.jsp        - Product details + ratings + comments
├── cart.jsp                  - Shopping cart
├── checkout.jsp              - Address selection + payment method
├── orders.jsp                - Order history
├── order-detail.jsp          - Single order with tracking
├── profile.jsp               - User profile
├── wishlist.jsp              - Saved products
├── admin/
│   ├── dashboard.jsp
│   ├── products.jsp
│   ├── orders.jsp
│   └── coupons.jsp
└── assets/
    ├── css/
    │   ├── main.css
    │   └── ecommerce.css
    └── js/
        ├── cart.js
        ├── checkout.js
        └── product.js
```

### PHASE 4: Payment Integration (2-3 days)

**COD (Cash on Delivery):**
- Simple order confirmation with pending payment status

**VNPAY Integration:**
```java
// Generate payment link
String vnpayUrl = PaymentUtil.createVNPayLink(orderId, amount, returnUrl);
// Handle callback
PaymentUtil.verifyVNPayCallback(request);
```

**MOMO Integration:**
- Similar to VNPAY with different API endpoints

### PHASE 5: Testing & Deployment (2-3 days)

## Implementation Tips

### 1. Use Consistent JSON Response Format

```java
private void sendJsonResponse(HttpServletResponse resp, boolean success, 
                             String message, Object data) throws IOException {
    Map<String, Object> response = new HashMap<>();
    response.put("success", success);
    response.put("message", message);
    response.put("data", data);
    
    resp.setContentType("application/json");
    resp.getWriter().write(new Gson().toJson(response));
}
```

### 2. Add Pagination Utility

```java
// Create utils/PaginationUtil.java
public class PaginationUtil {
    public static int getPage(HttpServletRequest req) {
        int page = 1;
        String p = req.getParameter("page");
        if (p != null && !p.isEmpty()) {
            try { page = Integer.parseInt(p); }
            catch (NumberFormatException e) { }
        }
        return Math.max(1, page);
    }

    public static int getLimit(HttpServletRequest req, int defaultLimit) {
        int limit = defaultLimit;
        String l = req.getParameter("limit");
        if (l != null && !l.isEmpty()) {
            try { limit = Integer.parseInt(l); }
            catch (NumberFormatException e) { }
        }
        return Math.min(Math.max(1, limit), 100); // Max 100 per page
    }

    public static int getOffset(int page, int limit) {
        return (page - 1) * limit;
    }
}
```

### 3. AJAX Cart Updates (client-side)

```javascript
// assets/js/cart.js
function addToCart(bookId, quantity = 1) {
    fetch('/api/cart/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            bookId: bookId,
            quantity: quantity
        })
    })
    .then(r => r.json())
    .then(data => {
        if (data.success) {
            updateCartIcon();
            showNotification('Added to cart!', 'success');
        } else {
            showNotification(data.message, 'error');
        }
    });
}
```

### 4. Product Listing Template (JSP)

```jsp
<!-- category.jsp -->
<%@ page import="dao.BookDAO, models.Book, utils.PaginationUtil, java.util.*" %>
<%
    int page = PaginationUtil.getPage(request);
    int limit = 20;
    int offset = PaginationUtil.getOffset(page, limit);
    String category = request.getParameter("category");
    String sort = request.getParameter("sort");
    
    List<Book> books = BookDAO.getByCategory(category, sort, limit, offset);
    int total = BookDAO.getCategoryCount(category);
    int totalPages = (total + limit - 1) / limit;
%>
<div class="container mt-4">
    <h1><%= category %></h1>
    
    <!-- Filters -->
    <div class="row mb-4">
        <div class="col-md-3">
            <label>Sort by:</label>
            <select class="form-select" onchange="window.location+='&sort='+this.value">
                <option value="">Newest</option>
                <option value="price_asc">Price Low to High</option>
                <option value="price_desc">Price High to Low</option>
                <option value="rating">Top Rated</option>
            </select>
        </div>
    </div>
    
    <!-- Products Grid -->
    <div class="row">
        <% for (Book book : books) { %>
        <div class="col-md-3 mb-4">
            <div class="card">
                <img src="<%= book.getImageUrl() %>" class="card-img-top" alt="<%= book.getTitle() %>">
                <div class="card-body">
                    <h5 class="card-title"><%= book.getTitle() %></h5>
                    <p class="card-text"><%= book.getAuthor() %></p>
                    <p class="text-muted"><%= book.getCategory() %></p>
                    <h6>₫<%= book.getPrice() %></h6>
                    <% if (book.getRatingCount() > 0) { %>
                    <div class="rating">
                        ⭐ <%= String.format("%.1f", book.getAverageRating()) %> 
                        (<%= book.getRatingCount() %> reviews)
                    </div>
                    <% } %>
                    <button class="btn btn-primary mt-2" 
                            onclick="addToCart(<%= book.getId() %>)">
                        Add to Cart
                    </button>
                </div>
            </div>
        </div>
        <% } %>
    </div>
    
    <!-- Pagination -->
    <nav>
        <ul class="pagination">
            <% for (int p = 1; p <= totalPages; p++) { %>
            <li class="page-item <%= p == page ? "active" : "" %>">
                <a class="page-link" href="?category=<%= category %>&page=<%= p %>"><%= p %></a>
            </li>
            <% } %>
        </ul>
    </nav>
</div>
```

## File Checklist

### Models (DONE ✅)
- [x] Book.java
- [x] CartItem.java
- [x] Order.java
- [x] DeliveryAddress.java
- [x] Rating.java
- [x] Comment.java
- [x] Coupon.java

### DAOs (PARTIALLY DONE)
- [x] BookDAO.java
- [x] CartDAO.java
- [x] CouponDAO.java
- [ ] OrderDAO.java
- [ ] RatingDAO.java
- [ ] CommentDAO.java
- [ ] WishlistDAO.java
- [ ] ProductViewDAO.java
- [ ] DeliveryAddressDAO.java
- [ ] UserDAO.java

### Servlets (TO DO)
- [ ] HomeApiServlet.java
- [ ] ProductApiServlet.java
- [ ] CartApiServlet.java
- [ ] OrderApiServlet.java
- [ ] RatingApiServlet.java
- [ ] CommentApiServlet.java
- [ ] CheckoutApiServlet.java
- [ ] WishlistApiServlet.java

### JSP Pages (TO DO)
- [ ] index.jsp (enhanced homepage)
- [ ] category.jsp
- [ ] product-detail.jsp
- [ ] cart.jsp
- [ ] checkout.jsp
- [ ] orders.jsp
- [ ] profile.jsp
- [ ] wishlist.jsp

### Utilities (TO DO)
- [ ] PaginationUtil.java
- [ ] PaymentUtil.java (VNPAY/MOMO integration)

## Quick Wins to Implement First

1. **Complete remaining DAOs** (Copy patterns from BookDAO)
2. **Create HomeApiServlet** - Returns homepage data
3. **Enhance index.jsp** - Display 4 carousels with AJAX
4. **Create CartApiServlet** - Add/remove/view cart
5. **Create ProductApiServlet** - Search/filter books
6. **Build category.jsp** - Category browsing with filters

## Build & Test

```bash
# Build project
mvn clean package

# Run locally
mvn tomcat:run

# Test homepage
curl http://localhost:8080/api/books/home

# Test add to cart
curl -X POST http://localhost:8080/api/cart/add \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"bookId": 1, "quantity": 2}'
```

## Deployment

```bash
# Build
mvn clean package

# Push to Heroku
git add .
git commit -m "Add e-commerce features: models, DAOs, extended schema"
git push heroku homepage:main

# Verify on production
heroku logs --tail
curl https://jva-bookstore-17d2d34519f8.herokuapp.com/api/books/home
```

## Support

- Need help implementing a specific DAO? Check BookDAO.java pattern
- Need API endpoint example? Check the servlet templates above
- Need JSP pagination? See category.jsp template above

---

**Current Phase:** Model & DAO foundation complete
**Next Phase:** Complete remaining DAOs and implement API endpoints
**Estimated Time:** 2-3 weeks for complete implementation
