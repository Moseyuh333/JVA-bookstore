# JVA Bookstore - E-Commerce Implementation Guide

## Project Scope

This guide outlines the implementation of a complete e-commerce bookstore system with the following major features:

### Core Features

1. **Home Page** - Display 20 newest, best-selling, top-rated, favorite books with pagination
2. **Product Catalog** - Category browsing with filtering and sorting
3. **Product Details** - Full product information, ratings, reviews, comments
4. **User Profile** - Personal info, multiple delivery addresses
5. **Shopping Cart** - Database-backed cart management
6. **Checkout & Payment** - COD, VNPAY, MOMO payment methods
7. **Order Management** - Order tracking with status (new, confirmed, shipping, delivered, cancelled, returned)
8. **Reviews & Ratings** - 1-5 star ratings with text reviews
9. **Comments** - Text + image/video comments (min 50 chars, from verified purchasers)
10. **Wishlist** - Save favorite books for later
11. **Product View History** - Track recently viewed products
12. **Coupon System** - Apply discount codes at checkout
13. **Admin Dashboard** - Manage products, orders, coupons, users

---

## Database Schema Overview

### Core Tables

| Table | Purpose |
|-------|---------|
| `users` | User accounts with profile info |
| `delivery_addresses` | Multiple shipping addresses per user |
| `books` | Product catalog with ratings/sales tracking |
| `shopping_cart` | One cart per user |
| `cart_items` | Items in cart |
| `orders` | Order records |
| `order_items` | Line items in orders |
| `wishlist` | Favorite books per user |
| `product_views` | Recently viewed products |
| `ratings` | 1-5 star ratings on books |
| `comments` | Text + media comments on books |
| `coupons` | Discount codes |
| `payment_transactions` | Payment records (COD/VNPAY/MOMO) |

---

## Implementation Roadmap

### Phase 1: Core Utilities & DAOs (Week 1)

**Priority:** HIGH - Foundation layer

Create DAO (Data Access Object) classes:
- `BookDAO` - Query books with filters, sorting, pagination
- `CartDAO` - Add/remove/update cart items
- `OrderDAO` - Create orders, update status
- `UserDAO` - User profile management
- `DeliveryAddressDAO` - Multiple address management
- `RatingDAO` - Rating/review operations
- `CommentDAO` - Comment management
- `CouponDAO` - Coupon validation
- `WishlistDAO` - Add/remove from wishlist
- `ProductViewDAO` - Track viewed products

### Phase 2: REST API Endpoints (Week 1-2)

**Priority:** HIGH - Backend services

#### Books API
```
GET /api/books/home               - Get homepage: newest, top-selling, top-rated, favorites (20 each)
GET /api/books/category/{cat}     - Browse by category with filters
GET /api/books/{id}               - Get book details
GET /api/books/search?q=...       - Search books
GET /api/books/top-rated?limit=20 - Top rated books
GET /api/books/best-selling?limit=20 - Best sellers
```

#### Cart API
```
POST /api/cart/add                - Add item to cart
POST /api/cart/remove             - Remove item from cart
PUT /api/cart/update/{itemId}     - Update quantity
GET /api/cart                     - Get cart items
DELETE /api/cart/clear            - Clear cart
GET /api/cart/total               - Get cart total
```

#### Orders API
```
POST /api/orders                  - Create order
GET /api/orders                   - User's orders
GET /api/orders/{id}              - Order details
PUT /api/orders/{id}/status       - Update order status
POST /api/orders/{id}/cancel      - Cancel order
POST /api/orders/{id}/return      - Return order
GET /api/orders/track             - Track delivery (integration with shipping provider)
```

#### Payment API
```
POST /api/payment/validate-coupon - Validate and apply coupon
POST /api/payment/checkout        - Process checkout
POST /api/payment/cod             - Confirm COD payment
POST /api/payment/vnpay/create    - Create VNPAY payment link
POST /api/payment/vnpay/return    - VNPAY callback
POST /api/payment/momo/create     - Create MOMO payment link
POST /api/payment/momo/return     - MOMO callback
```

#### User Profile API
```
GET /api/profile                  - Get profile info
PUT /api/profile                  - Update profile
GET /api/profile/addresses        - List delivery addresses
POST /api/profile/addresses       - Add address
PUT /api/profile/addresses/{id}   - Update address
DELETE /api/profile/addresses/{id} - Delete address
GET /api/profile/wishlist         - Get wishlist
GET /api/profile/recently-viewed  - Get view history
```

#### Reviews & Ratings API
```
POST /api/books/{id}/ratings      - Add rating/review
GET /api/books/{id}/ratings       - Get ratings for book
PUT /api/ratings/{id}             - Update rating
DELETE /api/ratings/{id}          - Delete rating
POST /api/books/{id}/comments     - Add comment
GET /api/books/{id}/comments      - Get comments
PUT /api/comments/{id}            - Edit comment
DELETE /api/comments/{id}         - Delete comment
```

#### Wishlist API
```
POST /api/wishlist/add            - Add to wishlist
DELETE /api/wishlist/remove       - Remove from wishlist
GET /api/wishlist                 - Get wishlist
```

### Phase 3: Frontend Views (JSP Pages) (Week 2-3)

**Priority:** HIGH - User-facing features

```
index.jsp (home)                  - Homepage with 20 products each (newest, best-selling, etc.)
category.jsp                      - Category browse with filters, sorting, pagination
product-detail.jsp               - Product details, ratings, comments, add to cart/wishlist
cart.jsp                          - Shopping cart with coupon input
checkout.jsp                      - Delivery address selection, payment method choice
payment-cod.jsp                   - COD order confirmation
payment-vnpay.jsp                 - VNPAY payment gateway redirect
payment-momo.jsp                  - MOMO payment gateway redirect
profile.jsp                       - User profile dashboard
addresses.jsp                     - Manage delivery addresses
orders.jsp                        - Order history with status tracking
order-detail.jsp                  - Single order details with tracking
wishlist.jsp                      - Wishlist management
history.jsp                       - Recently viewed products
admin-dashboard.jsp               - Admin panel
admin-books.jsp                   - Manage book inventory
admin-orders.jsp                  - Order management
admin-coupons.jsp                 - Coupon management
admin-users.jsp                   - User management
```

### Phase 4: Payment Integration (Week 3)

**Priority:** MEDIUM

- **COD** - Simple order confirmation
- **VNPAY** - Payment gateway integration (Vietnam national payment)
- **MOMO** - Mobile payment integration

### Phase 5: Advanced Features (Week 4)

**Priority:** MEDIUM

- Shipping provider integration for tracking
- Email notifications for order status
- Analytics dashboard
- Product recommendations

### Phase 6: Testing & Optimization (Week 4)

**Priority:** HIGH

- Unit testing for DAOs
- API testing (Postman collection)
- Performance optimization
- Security review

---

## Development Guidelines

### 1. DAO Pattern

All database operations should use DAO pattern:

```java
public class BookDAO {
    public List<Book> getNewBooks(int limit, int offset) { ... }
    public List<Book> getBestSellers(int limit, int offset) { ... }
    public List<Book> getByCategory(String category, int limit, int offset) { ... }
    public List<Book> search(String query, int limit, int offset) { ... }
    public Book getById(int id) { ... }
    public void updateAverageRating(int bookId) { ... }
    public void incrementSalesCount(int bookId, int quantity) { ... }
}
```

### 2. API Response Format

Standardized JSON responses:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 150,
    "totalPages": 8
  }
}
```

### 3. Authentication & Authorization

- All API endpoints except `/api/books/*` and `/api/payment/*` require JWT auth
- Admin endpoints require `admin` role
- Use `JwtFilter` for request validation
- Whitelist public endpoints in filter

### 4. Error Handling

Implement consistent error responses:

```json
{
  "success": false,
  "error": "ERROR_CODE",
  "message": "User-friendly error message",
  "details": "Technical details"
}
```

### 5. Pagination

Implement pagination for all list endpoints:
- Default limit: 20 items per page
- Parameters: `page` (1-indexed), `limit`
- Response includes total count and total pages

### 6. Validation

- Server-side validation for all inputs
- Coupon code format and expiry validation
- Payment amount validation
- Inventory check before order creation

---

## File Structure After Implementation

```
src/main/java/
├── dao/
│   ├── BookDAO.java
│   ├── CartDAO.java
│   ├── OrderDAO.java
│   ├── UserDAO.java
│   ├── DeliveryAddressDAO.java
│   ├── RatingDAO.java
│   ├── CommentDAO.java
│   ├── CouponDAO.java
│   ├── WishlistDAO.java
│   └── ProductViewDAO.java
├── models/
│   ├── Book.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── User.java
│   ├── DeliveryAddress.java
│   ├── Rating.java
│   ├── Comment.java
│   ├── Coupon.java
│   ├── PaymentTransaction.java
│   └── Wishlist.java
├── web/
│   ├── BooksApiServlet.java (existing - enhance)
│   ├── CartApiServlet.java
│   ├── OrderApiServlet.java
│   ├── PaymentApiServlet.java
│   ├── UserProfileServlet.java
│   ├── RatingApiServlet.java
│   ├── CommentApiServlet.java
│   ├── WishlistApiServlet.java
│   └── AdminServlet.java (existing - enhance)
├── utils/
│   ├── DBUtil.java (existing)
│   ├── EmailUtil.java (existing)
│   ├── JwtUtil.java (existing)
│   ├── PaymentUtil.java (new - VNPAY/MOMO)
│   └── PaginationUtil.java (new)
└── filters/
    └── JwtFilter.java (existing - enhance)

src/main/webapp/
├── index.jsp
├── category.jsp
├── product-detail.jsp
├── cart.jsp
├── checkout.jsp
├── profile.jsp
├── orders.jsp
├── order-detail.jsp
├── wishlist.jsp
├── admin/
│   ├── dashboard.jsp
│   ├── books.jsp
│   ├── orders.jsp
│   ├── coupons.jsp
│   └── users.jsp
└── assets/
    ├── css/
    │   └── ecommerce.css
    └── js/
        ├── cart.js
        ├── checkout.js
        └── product.js
```

---

## Key Implementation Notes

### 1. Product Ranking

Home page displays top products by category:
- **Newest**: Ordered by `created_at DESC`
- **Best Sellers**: Ordered by `sales_count DESC`
- **Top Rated**: Ordered by `average_rating DESC`
- **Favorites**: Ordered by wishlist count DESC (JOIN with wishlist table)

### 2. Cart Management

- Each logged-in user has exactly one shopping cart
- Cart items stored in database (survives session logout)
- Session-based cart for anonymous users (optional)
- Cart updates should refresh `updated_at` timestamp

### 3. Order Status Workflow

```
PENDING → CONFIRMED → SHIPPING → DELIVERED
                   ↘ CANCELLED
                   ↘ RETURNED → REFUNDED
```

### 4. Payment Integration Points

**COD (Cash on Delivery):**
- Simple order confirmation
- Payment status = "pending" until delivery

**VNPAY:**
- Generate payment link with order amount
- Redirect user to VNPAY gateway
- Handle callback on return to update payment status

**MOMO:**
- Similar to VNPAY
- Generate MOMO payment link
- Handle callback

### 5. Coupon Application

- Validate coupon code exists and is active
- Check minimum purchase amount
- Check expiry date (valid_from <= now <= valid_until)
- Check usage limits (usage_count < max_usage_count)
- Calculate discount (percent or fixed amount)
- Update final_total = total_amount - discount_amount

### 6. Reviews & Ratings

- Only verified purchasers can rate/review
- One rating per user per product
- Average rating calculated and cached in `books.average_rating`
- Comment text must be >= 50 characters
- Comments can include image/video URLs

---

## Quick Start Commands

```bash
# 1. Update database schema
mvn clean install

# 2. Create DAO classes (Week 1)
# Use template in dao/ folder

# 3. Create model classes (Week 1)
# Use template in models/ folder

# 4. Implement API servlets (Week 1-2)
# Route all requests through Servlet methods

# 5. Create JSP views (Week 2-3)
# Bootstrap 5 templates with AJAX for cart updates

# 6. Test locally
mvn tomcat:run

# 7. Deploy to Heroku
git push heroku homepage:main
```

---

## Testing Checklist

- [ ] Add books to database (50+ books across 5+ categories)
- [ ] Test homepage pagination for all 4 top lists
- [ ] Test category browsing with filters
- [ ] Test product detail page with ratings/comments
- [ ] Test cart add/remove/update
- [ ] Test checkout flow with delivery address selection
- [ ] Test coupon application with discount calculation
- [ ] Test COD payment
- [ ] Test VNPAY integration
- [ ] Test MOMO integration
- [ ] Test order tracking
- [ ] Test order status updates
- [ ] Test order cancellation/return flow
- [ ] Test rating/review creation by verified purchasers
- [ ] Test comment creation with character limit validation
- [ ] Test wishlist add/remove
- [ ] Test recently viewed products
- [ ] Test user profile update
- [ ] Test admin dashboard features
- [ ] Test search functionality
- [ ] Performance test with 100+ products

---

## Next Steps

1. **Start with Phase 1**: Create DAO classes and model classes
2. **Move to Phase 2**: Implement API endpoints
3. **Phase 3**: Build JSP frontend
4. **Phase 4**: Integrate payments
5. **Phase 5**: Add advanced features
6. **Phase 6**: Test comprehensively

Each phase has specific deliverables and can be worked on incrementally. The architecture is designed to be modular and testable.

---

## Estimated Timeline

- **Phase 1 (DAOs + Models)**: 3-4 days
- **Phase 2 (REST APIs)**: 4-5 days
- **Phase 3 (JSP Views)**: 5-6 days
- **Phase 4 (Payments)**: 2-3 days
- **Phase 5 (Advanced)**: 3-4 days
- **Phase 6 (Testing)**: 2-3 days

**Total: 3-4 weeks** for a complete e-commerce system

---

## Support References

- Bootstrap 5 Docs: https://getbootstrap.com/docs/5.0/
- Jakarta EE (Servlets): https://jakarta.ee/learn/documentation/
- PostgreSQL: https://www.postgresql.org/docs/
- VNPAY Integration: https://sandbox.vnpayment.vn/
- MOMO Integration: https://developers.momo.vn/
