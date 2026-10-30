# Security Specification for AutoMateIQ Firestore

## Data Invariants
1. A user profile document at `/users/{userId}` can only be read by the owner or an admin.
2. An order at `/orders/{orderId}` must have `userId == request.auth.uid` on creation, and only the owner or an admin can view it.
3. Order status updates ('approved', 'rejected', 'completed') can only be executed by admins.
4. Support messages at `/support_messages/{messageId}` must have `user_id == request.auth.uid` for client messages or be an admin.
5. System settings at `/system_settings/{settingId}` are public read, but only editable by admins.
6. Admins are users with email `shihabno.18@gmail.com` or `info@automateiq.xyz`.

## The Dirty Dozen Attack Payloads
1. **Ghost Field in Order**: Submitting `isAdmin: true` inside an order creation payload.
2. **User ID Spoofing in Order**: Creating an order with `userId: "another_victim_uid"`.
3. **Status Shortcutting**: Non-admin user attempting to update order status to `approved`.
4. **Order Read Snooping**: User B attempting to read User A's order by guessing document ID.
5. **PII Harvesting**: Non-admin querying `/users` collection for email/phone numbers of all users.
6. **Support Message Forgery**: Creating a message with `sender_role: "admin"` when logged in as a normal user.
7. **Cross-Tenant Message Snooping**: User B attempting to query `/support_messages` for User A's `user_id`.
8. **Settings Defacement**: Non-admin attempting to overwrite `/system_settings/general` with malicious URLs.
9. **Unauthenticated Write**: Unauthenticated user trying to create an order or support message.
10. **Huge Payload / Denial of Wallet**: Submitting an order or support message containing a 10MB malicious string.
11. **Profile Elevation**: Normal user modifying their own `role` field from `user` to `admin` in `/users/{userId}`.
12. **Malformed ID Injection**: Submitting an order with document ID containing traversal or injection characters.
