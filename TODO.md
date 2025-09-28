# Authentication Implementation and UI Redesign TODO

## Overview
This TODO tracks the breakdown of the approved plan to implement missing auth features (register/reset APIs, DB integration, email verification/SMTP) and redesign UI pages. Steps are sequential; each will be marked [x] upon completion. Progress will be updated after each major step.

## Step 1: Update Dependencies (pom.xml)
- [ ] Read current pom.xml to assess existing dependencies.
- [ ] Add Jakarta Mail for SMTP/email (jakarta.mail:jakarta.mail-api and com.sun.mail:jakarta.mail).
- [ ] Add BCrypt for password hashing (org.mindrot:jbcrypt).
- [ ] Ensure JJWT is present for JWT (io.jsonwebtoken:jjwt-api, jjwt-impl, jjwt-jackson).
- [ ] Run `mvn clean install` to verify/update dependencies.
- Followup: Confirm no conflicts; test build.

## Step 2: Database Setup
- [ ] Parse provided DATABASE_URL and update DBUtil.java to use it (already supported; set env if needed).
- [ ] Create users table schema: 
  - Columns: id (SERIAL PRIMARY KEY), username (VARCHAR UNIQUE), email (VARCHAR UNIQUE), password_hash (VARCHAR), verified (BOOLEAN DEFAULT FALSE), verification_token (VARCHAR), reset_token (VARCHAR), reset_expiry (TIMESTAMP), created_at (TIMESTAMP DEFAULT CURRENT_TIMESTAMP).
- [ ] Implement table creation: Create init.sql script and execute via psql or add DBUtil.init() method called on app startup.
- [ ] Update DBUtil.java: Add methods for user operations (createUser, findByUsername, findByEmail, updateVerification, updatePassword, etc.).
- Followup: Test DB connection; insert sample user manually if needed.

## Step 3: Email Service Implementation
- [ ] Create src/main/java/utils/EmailUtil.java: Handle sending emails via Jakarta Mail (config SMTP host/port/user/pass from properties/env).
- [ ] Add email config to src/main/resources/email.properties (placeholders: smtp.host=smtp.gmail.com, smtp.port=587, smtp.user=your-email@gmail.com, smtp.pass=app-password).
- [ ] Methods: sendVerificationEmail(email, token, username), sendResetEmail(email, token).
- [ ] Security: Use TLS/STARTTLS; handle exceptions.
- Followup: Ask user for SMTP details (e.g., Gmail app password) to test; placeholder for now.

## Step 4: Enhance AuthServlet.java
- [ ] Update /api/login (POST): Validate credentials against DB (BCrypt check), generate JWT with username/email.
- [ ] Add /api/auth/register (POST): Hash password, insert user (unverified), generate verification token, send email.
- [ ] Add /api/auth/reset-password (POST): Find user by email, generate reset token (expiry 1hr), send email.
- [ ] Input validation: Sanitize params, check duplicates.
- [ ] Error handling: JSON responses (200 OK, 400 Bad Request, 409 Conflict, etc.).
- Followup: Test APIs with Postman (mock email first).

## Step 5: Add Verification and Reset Servlets
- [ ] Create src/main/java/web/VerifyServlet.java: /api/auth/verify?token=... (GET) - Validate token, set verified=true, redirect to login.
- [ ] Create src/main/java/web/ResetServlet.java: /api/auth/reset (POST) - Validate reset_token/expiry, update password_hash.
- [ ] Update web.xml: Map new servlets.
- Followup: Test full flow (register -> verify -> login).

## Step 6: JWT and Protection Enhancements
- [ ] Update JwtUtil.java: Include email in claims; add token expiry check.
- [ ] Update JwtFilter.java: Extract user from token, optional logging.
- [ ] Add UI protection: Create AuthFilter for JSPs or check token in JS; redirect unauth to login.
- Followup: Test protected /api/books access post-login.

## Step 7: UI Redesign
- [ ] Redesign login.jsp: Add logo (nkbookstore-logo.png), animations (fade-in), better validation, success modal/redirect.
- [ ] Redesign register.jsp: Add email verification message post-submit, improved styling (gradients, icons).
- [ ] Redesign forgot-password.jsp: Add loading spinner on submit, success message.
- [ ] Add new pages: verify-email.jsp (confirmation), reset-password.jsp (new password form after link).
- [ ] Update JS: Handle API errors, store token in localStorage, auto-redirect.
- [ ] Enhance CSS: Use assets/css/style.css for shared styles (modern fonts, responsive).
- Followup: Test UI flows; use browser_action if needed for screenshots.

## Step 8: Testing and Finalization
- [ ] Full E2E Test: Register (check email), verify, login, reset password, access protected routes.
- [ ] Security Audit: Hashing, token expiry, input validation, rate limiting (basic).
- [ ] Deployment Prep: Set env vars (DATABASE_URL, JWT_SECRET, SMTP_*); update Procfile if needed.
- [ ] Cleanup: Remove demo logic from AuthServlet; add README updates.
- [ ] Mark all [x]; attempt_completion.

Current Progress: Starting Step 1.
