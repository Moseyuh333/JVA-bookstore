# TODO: Add Status Column to Books

## Backend Updates
- [x] Update Book.java model to include status field
- [x] Update BookDAO.java to include status in BASE_SELECT and mapRow
- [x] Update AdminProductsServlet.java to include status in all SQL queries and JSON responses

## Frontend Updates
- [x] Update AdProduct.jsp to display status in table and add to modal form
- [x] Update AdProduct.js to render status in table and handle status in form

## Testing
- [x] Test list, create, update, delete products with status
- [x] Verify frontend displays and edits status correctly
- [x] Verify new products default to "active" status
- [x] Verify search by status works correctly
