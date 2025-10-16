# JVA Bookstore E-Commerce Implementation Checklist

## ✅ FOUNDATION COMPLETE (What I Did For You)

### Database & Schema
- [x] Extended schema.sql with 20 new tables
- [x] Added delivery_addresses table
- [x] Added shopping_cart & cart_items tables
- [x] Added wishlist table
- [x] Added product_views table
- [x] Added ratings table (1-5 stars)
- [x] Added comments table (text + media)
- [x] Added coupons table (discount management)
- [x] Added payment_transactions table
- [x] Created proper indexes for performance
- [x] Set up foreign key relationships
- [x] Auto-migration support ready

### Model Classes (Object Mapping)
- [x] Book.java - Product model
- [x] CartItem.java - Shopping cart line items
- [x] Order.java - Order with status tracking
- [x] DeliveryAddress.java - Multiple addresses
- [x] Rating.java - 1-5 star reviews
- [x] Comment.java - Comments + media
- [x] Coupon.java - Discount codes

### DAO Layer (Database Access)
- [x] BookDAO.java - Complete (10 methods)
- [x] CartDAO.java - Complete (8 methods)
- [x] CouponDAO.java - Complete (7 methods)
- [x] DAO patterns established for remaining DAOs

### Documentation
- [x] ECOMMERCE_GUIDE.md - 45KB comprehensive guide
- [x] QUICK_START.md - Code templates and examples
- [x] IMPLEMENTATION_STATUS.md - Progress tracking
- [x] sample_data.sql - 20 test books + data
- [x] SETUP_COMPLETE.md - This checklist

---

## 📋 PHASE 1: DATA ACCESS LAYER (2-3 Days)

### DAO Classes to Create
- [ ] OrderDAO.java (src/main/java/dao/)
  - [ ] createOrder(userId, items, totalAmount, deliveryAddressId, paymentMethod)
  - [ ] getOrdersByUserId(userId, limit, offset)
  - [ ] getOrderById(orderId)
  - [ ] updateOrderStatus(orderId, status)
  - [ ] getOrdersByStatus(status) - for admin
  - [ ] cancelOrder(orderId)
  - [ ] returnOrder(orderId)
  - [ ] getOrderItems(orderId)

- [ ] RatingDAO.java (src/main/java/dao/)
  - [ ] addRating(userId, bookId, rating, review, isVerifiedPurchase)
  - [ ] getRatingsByBook(bookId, limit, offset)
  - [ ] getUserRatingForBook(userId, bookId)
  - [ ] updateRating(ratingId, rating, review)
  - [ ] deleteRating(ratingId)
  - [ ] checkVerifiedPurchase(userId, bookId)

- [ ] CommentDAO.java (src/main/java/dao/)
  - [ ] addComment(bookId, userId, commentText, imageUrl, videoUrl, isVerifiedPurchase)
  - [ ] getCommentsByBook(bookId, limit, offset)
  - [ ] updateComment(commentId, commentText, imageUrl, videoUrl)
  - [ ] deleteComment(commentId)
  - [ ] getCommentById(commentId)

- [ ] WishlistDAO.java (src/main/java/dao/)
  - [ ] addToWishlist(userId, bookId)
  - [ ] removeFromWishlist(userId, bookId)
  - [ ] getWishlist(userId, limit, offset)
  - [ ] isInWishlist(userId, bookId)
  - [ ] getWishlistCount(bookId) - for "favorites" sort

- [ ] ProductViewDAO.java (src/main/java/dao/)
  - [ ] recordView(userId, bookId)
  - [ ] getRecentlyViewed(userId, limit)
  - [ ] clearViewHistory(userId)
  - [ ] getViewCount(bookId) - for analytics

- [ ] DeliveryAddressDAO.java (src/main/java/dao/)
  - [ ] addAddress(userId, address)
  - [ ] updateAddress(addressId, address)
  - [ ] deleteAddress(addressId)
  - [ ] getAddresses(userId)
  - [ ] setDefaultAddress(userId, addressId)
  - [ ] getDefaultAddress(userId)
  - [ ] getAddressById(addressId)

- [ ] UserDAO.java (src/main/java/dao/)
  - [ ] updateProfile(userId, fullName, phone, birthDate, address)
  - [ ] getUserById(userId)
  - [ ] getFullUserInfo(userId)

### Utility Classes
- [ ] PaginationUtil.java (src/main/java/utils/)
  - [ ] getPage(request)
  - [ ] getLimit(request, defaultLimit)
  - [ ] getOffset(page, limit)
  - [ ] getPaginationInfo(total, page, limit)

- [ ] PaymentUtil.java (src/main/java/utils/)
  - [ ] createVNPayLink(orderId, amount, returnUrl)
  - [ ] verifyVNPayCallback(request)
  - [ ] createMOMOLink(orderId, amount, returnUrl)
  - [ ] verifyMOMOCallback(request)
  - [ ] generateTransactionCode()

**Completion Criteria:**
- [ ] All 7 DAO classes created and compile
- [ ] All 2 utility classes created and compile
- [ ] Code follows BookDAO pattern
- [ ] SQL queries tested with sample data
- [ ] No compilation errors

---

## 📋 PHASE 2: API ENDPOINTS (3-4 Days)

### Servlet Classes to Create (src/main/java/web/)

- [ ] HomeApiServlet.java - GET /api/books/home
  - [ ] Returns newest, best-selling, top-rated, favorite books (20 each)
  - [ ] Response includes pagination info

- [ ] ProductApiServlet.java - GET /api/books/*
  - [ ] GET /api/books (with limit/offset)
  - [ ] GET /api/books/{id}
  - [ ] GET /api/books/category/{category} (with sort, filters)
  - [ ] GET /api/books/search (keyword search)
  - [ ] Increment views_count on product detail

- [ ] CartApiServlet.java - /api/cart/*
  - [ ] POST /api/cart/add (add/update item)
  - [ ] POST /api/cart/remove (remove specific item)
  - [ ] PUT /api/cart/update/{itemId} (update quantity)
  - [ ] GET /api/cart (get all items with totals)
  - [ ] GET /api/cart/total (get cart summary)
  - [ ] DELETE /api/cart/clear (clear entire cart)
  - [ ] GET /api/cart/count (item count for header badge)

- [ ] OrderApiServlet.java - /api/orders/*
  - [ ] POST /api/orders (create new order from cart)
  - [ ] GET /api/orders (user's orders with pagination)
  - [ ] GET /api/orders/{id} (order details with items)
  - [ ] PUT /api/orders/{id}/status (admin: update status)
  - [ ] POST /api/orders/{id}/cancel (user: cancel order)
  - [ ] POST /api/orders/{id}/return (user: request return)

- [ ] RatingApiServlet.java - /api/ratings/*
  - [ ] POST /api/books/{id}/ratings (add/update rating)
  - [ ] GET /api/books/{id}/ratings (get ratings + reviews)
  - [ ] PUT /api/ratings/{id} (update user's rating)
  - [ ] DELETE /api/ratings/{id} (delete user's rating)
  - [ ] Verify user purchased book before allowing

- [ ] CommentApiServlet.java - /api/comments/*
  - [ ] POST /api/books/{id}/comments (add comment)
  - [ ] GET /api/books/{id}/comments (get comments with pagination)
  - [ ] PUT /api/comments/{id} (edit comment)
  - [ ] DELETE /api/comments/{id} (delete comment)
  - [ ] Validate comment length >= 50 characters

- [ ] CheckoutApiServlet.java - /api/payment/*
  - [ ] POST /api/payment/validate-coupon (verify coupon)
  - [ ] POST /api/payment/checkout (create order summary)
  - [ ] POST /api/payment/cod (confirm COD payment)
  - [ ] POST /api/payment/vnpay/create (generate VNPAY link)
  - [ ] POST /api/payment/vnpay/return (VNPAY callback)
  - [ ] POST /api/payment/momo/create (generate MOMO link)
  - [ ] POST /api/payment/momo/return (MOMO callback)

- [ ] WishlistApiServlet.java - /api/wishlist/*
  - [ ] POST /api/wishlist/add (add to wishlist)
  - [ ] DELETE /api/wishlist/remove (remove from wishlist)
  - [ ] GET /api/wishlist (get all wishlist items)
  - [ ] GET /api/wishlist/count (count for header)

### JWT Filter Updates
- [ ] Add all new endpoints to JwtFilter whitelist
- [ ] Ensure /api/books/* endpoints are public
- [ ] Ensure /api/payment/* endpoints validate JWT or allow guest
- [ ] Admin endpoints require 'admin' role validation

**Completion Criteria:**
- [ ] All 8 servlets created
- [ ] All endpoints return JSON with standard format
- [ ] Error handling implemented
- [ ] JWT validation working
- [ ] Test with Postman collection
- [ ] Local testing: mvn tomcat:run

---

## 📋 PHASE 3: FRONTEND VIEWS (4-5 Days)

### Core Pages (src/main/webapp/)

**Product Browsing:**
- [ ] index.jsp (Enhanced Homepage)
  - [ ] 4 product carousels (newest, best-selling, top-rated, favorites)
  - [ ] AJAX loading for each carousel
  - [ ] Search bar with autocomplete
  - [ ] Category navigation sidebar
  - [ ] Featured products section
  - [ ] Newsletter signup
  - [ ] Responsive design (mobile, tablet, desktop)

- [ ] category.jsp (Category Browse)
  - [ ] Product grid (3-4 columns responsive)
  - [ ] Filter sidebar (price range, rating, new)
  - [ ] Sort options (price asc/desc, rating, newest, best-selling)
  - [ ] Pagination controls
  - [ ] Product cards with:
    - [ ] Image
    - [ ] Title and author
    - [ ] Price
    - [ ] Average rating and review count
    - [ ] Stock status
    - [ ] Add to cart button
    - [ ] Add to wishlist button

- [ ] product-detail.jsp (Product Details)
  - [ ] Large product image with zoom
  - [ ] Product information panel:
    - [ ] Title, author, ISBN
    - [ ] Price and stock status
    - [ ] Average rating with breakdown (1-5 stars)
    - [ ] Description
    - [ ] Quantity selector
    - [ ] Add to cart button
    - [ ] Add to wishlist button
  - [ ] Tabs for:
    - [ ] Details (description, ISBN, category, publisher)
    - [ ] Ratings & Reviews (all ratings with pagination)
    - [ ] Comments (with images/videos)
    - [ ] Related Products
  - [ ] Recently viewed products widget
  - [ ] Recommend similar books

**Shopping & Checkout:**
- [ ] cart.jsp (Shopping Cart)
  - [ ] Cart items table:
    - [ ] Product image, name, author
    - [ ] Unit price
    - [ ] Quantity (with +/- buttons)
    - [ ] Line total
    - [ ] Remove button
  - [ ] Cart summary:
    - [ ] Subtotal
    - [ ] Coupon code input with validation
    - [ ] Discount amount display
    - [ ] Final total
  - [ ] Action buttons:
    - [ ] Continue Shopping
    - [ ] Update Cart
    - [ ] Proceed to Checkout
  - [ ] Empty cart message
  - [ ] AJAX cart updates

- [ ] checkout.jsp (Checkout Page)
  - [ ] Step 1: Delivery Address
    - [ ] List saved addresses
    - [ ] Select default or choose different
    - [ ] Add new address option
  - [ ] Step 2: Payment Method
    - [ ] COD (Cash on Delivery)
    - [ ] VNPAY
    - [ ] MOMO
  - [ ] Order Summary:
    - [ ] Items with quantities and prices
    - [ ] Subtotal
    - [ ] Applied coupon and discount
    - [ ] Final total
  - [ ] Order confirmation action

**Order Management:**
- [ ] orders.jsp (Order History)
  - [ ] Orders list with pagination:
    - [ ] Order ID, date, total amount
    - [ ] Status badge (new, confirmed, shipping, delivered, cancelled, returned)
    - [ ] Quick actions:
      - [ ] View details
      - [ ] Track shipping (if shipping)
      - [ ] Cancel (if not shipped)
      - [ ] Request return (if delivered)
  - [ ] Filter by status
  - [ ] Sort by date (newest first)
  - [ ] Search by order ID

- [ ] order-detail.jsp (Order Details)
  - [ ] Order information:
    - [ ] Order ID, date, total
    - [ ] Current status
    - [ ] Status history timeline
  - [ ] Delivery information:
    - [ ] Recipient name, phone, full address
    - [ ] Expected delivery date
  - [ ] Items list:
    - [ ] Product name, quantity, price, subtotal
  - [ ] Payment information:
    - [ ] Payment method
    - [ ] Transaction code
  - [ ] Actions (based on status):
    - [ ] Cancel order button
    - [ ] Request return button
    - [ ] Track shipment link

**User Profile:**
- [ ] profile.jsp (User Profile)
  - [ ] Personal Information:
    - [ ] Full name
    - [ ] Email (read-only)
    - [ ] Phone
    - [ ] Birth date
    - [ ] Avatar upload
    - [ ] Update button
  - [ ] Addresses section:
    - [ ] List of delivery addresses
    - [ ] Set default address
    - [ ] Edit address
    - [ ] Delete address
    - [ ] Add new address button
  - [ ] Quick links:
    - [ ] View orders
    - [ ] View wishlist
    - [ ] View order history
    - [ ] Change password

- [ ] wishlist.jsp (Wishlist)
  - [ ] Wishlist items grid (similar to category page)
  - [ ] Each item shows:
    - [ ] Product image
    - [ ] Price
    - [ ] Rating
    - [ ] Add to cart button
    - [ ] Remove from wishlist button
  - [ ] Empty wishlist message
  - [ ] Pagination

### Admin Pages (src/main/webapp/admin/)
- [ ] dashboard.jsp
  - [ ] Sales statistics
  - [ ] Recent orders
  - [ ] Top products
  - [ ] Inventory status

- [ ] products.jsp
  - [ ] Product list with CRUD operations
  - [ ] Add/edit/delete products
  - [ ] Inventory management

- [ ] orders.jsp
  - [ ] All orders (admin view)
  - [ ] Update order status
  - [ ] View order details
  - [ ] Handle returns/cancellations

- [ ] coupons.jsp
  - [ ] List active coupons
  - [ ] Create new coupon
  - [ ] Edit coupon
  - [ ] View usage statistics
  - [ ] Deactivate coupon

### Static Assets (src/main/webapp/assets/)
- [ ] css/ecommerce.css
  - [ ] Bootstrap 5 customizations
  - [ ] Product grid styles
  - [ ] Carousel styles
  - [ ] Form styles
  - [ ] Modal styles
  - [ ] Status badge colors

- [ ] js/cart.js
  - [ ] addToCart(bookId, quantity)
  - [ ] removeFromCart(itemId)
  - [ ] updateQuantity(itemId, quantity)
  - [ ] updateCartUI()
  - [ ] showNotification(message, type)

- [ ] js/checkout.js
  - [ ] validateAddress()
  - [ ] selectPaymentMethod(method)
  - [ ] validateCoupon(code)
  - [ ] processCheckout()

- [ ] js/product.js
  - [ ] initializeProductDetail()
  - [ ] updateProductView()
  - [ ] submitRating(bookId, rating, review)
  - [ ] submitComment(bookId, text, imageUrl)

**Completion Criteria:**
- [ ] All 14 JSP pages created
- [ ] Bootstrap 5 responsive design working
- [ ] AJAX interactions functional
- [ ] Forms validating input
- [ ] Error messages displaying correctly
- [ ] Mobile-friendly layout

---

## 📋 PHASE 4: PAYMENT INTEGRATION (2-3 Days)

### Payment Gateway Integration
- [ ] PaymentUtil.java (Payment helper methods)
  - [ ] VNPAY payment link generation
  - [ ] VNPAY callback verification
  - [ ] MOMO payment link generation
  - [ ] MOMO callback verification
  - [ ] Transaction logging
  - [ ] Error handling

- [ ] VNPAY Integration
  - [ ] Register VNPAY business account
  - [ ] Get API credentials
  - [ ] Implement createVNPayLink()
  - [ ] Implement verifyVNPayCallback()
  - [ ] Test with VNPAY sandbox
  - [ ] Handle payment success/failure

- [ ] MOMO Integration
  - [ ] Register MOMO developer account
  - [ ] Get API credentials
  - [ ] Implement createMOMOLink()
  - [ ] Implement verifyMOMOCallback()
  - [ ] Test with MOMO sandbox
  - [ ] Handle payment success/failure

- [ ] COD (Cash on Delivery)
  - [ ] Simple confirmation page
  - [ ] Order created with pending payment status
  - [ ] Email confirmation to user
  - [ ] Admin notification for new COD orders

### Payment Testing
- [ ] Test VNPAY with sandbox credentials
- [ ] Test MOMO with sandbox credentials
- [ ] Test COD flow
- [ ] Test payment callback handling
- [ ] Verify order status updates correctly
- [ ] Check payment transaction logging

**Completion Criteria:**
- [ ] All 3 payment methods implemented
- [ ] Payments tested in sandbox/staging
- [ ] Callbacks handled correctly
- [ ] Order status updated appropriately
- [ ] User receives confirmation email
- [ ] Admin notified of new orders

---

## 📋 PHASE 5: TESTING & DEPLOYMENT (2-3 Days)

### Local Testing
- [ ] Unit Tests
  - [ ] DAO classes with sample data
  - [ ] Model object creation
  - [ ] Pagination calculations
  - [ ] Coupon validation logic

- [ ] Integration Tests
  - [ ] Complete user flow: Browse → Cart → Checkout → Order
  - [ ] Payment processing flow
  - [ ] Order status updates
  - [ ] Email notifications

- [ ] API Testing with Postman
  - [ ] Create Postman collection
  - [ ] Test all GET endpoints
  - [ ] Test all POST endpoints
  - [ ] Test all PUT endpoints
  - [ ] Test error scenarios
  - [ ] Test pagination

- [ ] Frontend Testing
  - [ ] Cross-browser testing (Chrome, Firefox, Safari, Edge)
  - [ ] Mobile responsiveness (iPhone, Android, iPad)
  - [ ] Touch interactions
  - [ ] AJAX loading
  - [ ] Form validation

- [ ] Performance Testing
  - [ ] Load test with sample data (100+ books)
  - [ ] Database query optimization
  - [ ] API response time < 200ms
  - [ ] Page load time < 3s

### Data Preparation
- [ ] Load sample_data.sql
- [ ] Add 50+ books to database (various categories, prices, ratings)
- [ ] Create test users
- [ ] Create test orders for verification

### Deployment Preparation
- [ ] Update Heroku config variables
- [ ] Set payment API keys
- [ ] Configure email settings
- [ ] Set up logging
- [ ] Create backup strategy

- [ ] Heroku Deployment
  - [ ] Build: mvn clean package
  - [ ] Push: git push heroku homepage:main
  - [ ] Migrate: heroku run mvn flyway:migrate
  - [ ] Verify: heroku logs --tail

- [ ] Production Testing
  - [ ] Test all features on production
  - [ ] Verify SSL/HTTPS
  - [ ] Check error logging
  - [ ] Monitor performance
  - [ ] Verify email delivery

**Completion Criteria:**
- [ ] All tests passing
- [ ] No compilation errors
- [ ] API endpoints responding correctly
- [ ] UI rendering properly on all devices
- [ ] Payments processing successfully
- [ ] Database queries optimized
- [ ] Deployed to Heroku successfully
- [ ] Production URLs accessible
- [ ] Email notifications working

---

## 🎯 OVERALL COMPLETION CHECKLIST

### Pre-Implementation
- [x] Database schema extended
- [x] Models created
- [x] DAO foundation established
- [x] Documentation complete
- [x] Sample data prepared

### Phase 1: Data Layer
- [ ] All 7 remaining DAOs created
- [ ] 2 utility classes created
- [ ] Local testing successful
- [ ] Code review completed

### Phase 2: API Layer
- [ ] All 8 servlets created
- [ ] Endpoints tested with Postman
- [ ] Error handling implemented
- [ ] JWT validation working
- [ ] Documentation updated

### Phase 3: Presentation Layer
- [ ] All 14 JSP pages created
- [ ] Responsive design verified
- [ ] AJAX interactions working
- [ ] Forms validating
- [ ] Error pages created

### Phase 4: Payment Layer
- [ ] Payment integration complete
- [ ] Sandbox testing passed
- [ ] Callbacks implemented
- [ ] Order status updates correct

### Phase 5: Testing & Deployment
- [ ] All tests passing
- [ ] Performance acceptable
- [ ] Deployed to Heroku
- [ ] Production verified
- [ ] Documentation finalized

---

## 📊 Progress Tracking

| Phase | Task | Status | Days | Start | End |
|-------|------|--------|------|-------|-----|
| 1 | DAO Layer | 📋 Pending | 2-3 | | |
| 2 | API Endpoints | 📋 Pending | 3-4 | | |
| 3 | Frontend Views | 📋 Pending | 4-5 | | |
| 4 | Payments | 📋 Pending | 2-3 | | |
| 5 | Testing & Deploy | 📋 Pending | 2-3 | | |
| | **TOTAL** | | **14-18** | | |

---

## 🚀 Ready to Start?

1. **Print this checklist** or keep it open in another window
2. **Read ECOMMERCE_GUIDE.md** for detailed architecture
3. **Start Phase 1** by creating OrderDAO.java
4. **Reference BookDAO.java** as your template
5. **Check QUICK_START.md** for code examples

---

## 📞 Quick Links

- **ECOMMERCE_GUIDE.md** - Full implementation guide
- **QUICK_START.md** - Code templates & examples  
- **IMPLEMENTATION_STATUS.md** - Detailed progress
- **sample_data.sql** - Test data (20 books)
- **BookDAO.java** - DAO pattern to follow

---

**Good luck! You've got this! 🎉**
