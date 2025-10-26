# TODO List for Seller Functionality Updates

## 1. Seller Products Page Enhancements
- [x] Add modal for adding new product in SellerProduct.jsp
- [x] Implement openAddModal function in SellerProduct.js
- [x] Add functionality for edit (pencil) button in SellerProduct.js
- [x] Connect add/edit forms to /api/seller/products API

## 2. Seller Orders Page Updates
- [x] Change all "quản lý danh mục" to "quản lý đơn hàng" in SellerOrders.jsp
- [x] Update table structure from categories to orders
- [x] Update stats from categories to orders
- [x] Implement order management functionality
- [x] Add functionality for "Chi tiết" button to view order details in SellerOrders.js

## 3. Seller Profile API
- [x] Create SellerProfileServlet.java for /api/seller/profile
- [x] Implement GET action to retrieve shop profile
- [x] Implement POST action to update shop profile
- [x] Update ShopDAO.java if needed for update functionality

## 4. Seller Settings Page
- [x] Update SellerSetting.jsp to save changes to database via /api/seller/profile API
- [x] Implement proper form submission and error handling

## 5. Testing and Verification
- [ ] Test product add/edit functionality
- [ ] Test order details view
- [ ] Test shop profile update
- [ ] Verify all changes work correctly
