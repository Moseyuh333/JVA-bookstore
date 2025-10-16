# Delivery Roadmap

This roadmap captures the prioritized plan for evolving JVA Bookstore into a production-ready e-commerce platform. Each phase should be merged only after clean builds and manual verification (auth, cart, checkout).

## Phase 1 · Foundation (in progress)
- Align runtime schema (`DBUtil`) with `schema.sql`
- Clean redundant documentation and keep a single authoritative README
- Establish build stability (`mvn clean package -DskipTests` green)

## Phase 2 · Shopping Experience
- Redesign `index.jsp` with hero sections, featured carousels, and lazy-loading blocks of 20 books (new, best sellers, top rated, wishlisted)
- Implement category browsing with filters (price ranges, rating, availability) and server-side pagination
- Build product detail pages with media gallery, specs, related items, view history tracking
- Improve SEO (meta tags, friendly URLs, structured data)

## Phase 3 · Accounts & Profile
- Harden auth flows (login/register, OTP, reset password, brute-force protection)
- Extend profile management: personal info, password change, multi-address CRUD with defaults
- Persist wishlists and recently viewed books per user

## Phase 4 · Cart & Checkout
- Persist cart server-side for guests and authenticated users; sync on login
- Support quantity updates, coupon application, shipping fee calculation
- Implement multi-step checkout (address → shipping → payment → confirmation) with order review

## Phase 5 · Payments
- Integrate Cash on Delivery workflow (confirmation, fulfillment hand-off)
- Implement VNPAY (redirect, checksum, callback, reconciliation)
- Prepare MoMo integration (sandbox credentials, IPN handler)
- Add payment transaction logging, retries, idempotency, and webhook verification

## Phase 6 · Orders & Back Office
- Build full order lifecycle (new → confirmed → shipping → delivered → cancelled → returned/refunded)
- Customer-facing order history with status tracking, invoice download
- Admin console for order management, inventory adjustments, coupon management, user moderation

## Phase 7 · Engagement
- Allow product reviews/ratings only for fulfilled orders; enforce minimum 50-char text
- Support review media uploads (images/video) via cloud storage
- Enable comments with moderation controls, helpful votes, reporting
- Surface personalised recommendations (wishlist, recent views, cross-sell)

## Phase 8 · Performance & Security
- Introduce caching (Redis) for homepage blocks, category filters
- Optimise DB access patterns (pagination, indices, prepared statements)
- Enforce password policies, JWT rotation/refresh, rate limiting, audit logs
- Schedule security scans and dependency checks

## Phase 9 · Quality & DevOps
- Add unit/integration/e2e tests (JUnit + Selenium/Playwright)
- Automate CI/CD (GitHub Actions → Heroku or container registry)
- Implement database migrations (Flyway/Liquibase) and environment-specific configs
- Add application monitoring, structured logging, alerting

> Adjust priorities as product requirements evolve. Each phase should produce demo-ready increments and updated documentation/tests.
