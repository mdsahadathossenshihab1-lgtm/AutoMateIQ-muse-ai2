# AutoMateIQ Mobile Dashboard — v8.0 (APPROVED DESIGN v3)

Native Android app, login-first, no WebView. Package `com.automateiq.app`,
versionCode 12, versionName 8.0, minSdk 24, targetSdk 34.

Built exactly to the user-approved design v3
(`~/workspace/automateiq-design/design-prototype-v3.html` +
`DESIGN_SPEC_V3.md`): aurora gradient background, semi-transparent
glass cards, vector icons, v3 typography scale, and subtle animations.
Backend logic is kept from v7 — only the skin, the structures and the
user-visible copy changed.

## What v3 changed in the app

- **Skin:** aurora background (violet / cyan / pink tints), glass cards
  (semi-transparent white + light border), gradient heroes whose colours
  slowly shift (9.5s alternate), colourful gradient icon tiles.
- **Icons:** 18 Android Vector Drawables (`res/drawable/ic_*.xml`,
  24×24, 2dp stroke, round caps) for nav, tiles, services and actions.
  No emoji is used as a UI icon anywhere.
- **Typography:** v3 scale — screen 26/800, section 18/800, card 16/700,
  body 14 with 1.7 line height, label 11/800, price 22/900. Bengali
  text uses the bundled Noto Sans Bengali font
  (`assets/fonts/NotoSansBengali-Regular.ttf` /
  `NotoSansBengali-Bold.ttf`; Hind Siliguri could not be bundled from
  this environment, so the closest system Bengali family available
  locally was bundled instead, at exactly the v3 scale). Prices and
  days always use Bengali digits + ৳.
- **Animations:** staggered fade + slide-up card entry (350ms, 70ms
  stagger), button press scale .97, gradient hero colour shift,
  progress bars animating 0 → value over 1s, shimmer loading bars
  (1.4s pulse), card lift via elevation + ripple. No flashing, nothing
  blocks reading. The nav selected item is a highlighted pill (the
  sliding-pill motion of the HTML prototype is approximated by the
  pill highlight in this programmatic UI); chat bubbles enter with the
  same fade + slide as cards on thread render, and the thread still
  opens at the latest message with a pinned composer.
- **Structures:** Login (gradient logo + one Google button, no
  explanations); Admin Dashboard (greeting + shifting gradient hero +
  horizontal quick-action row + activity timeline with status dots +
  recent orders, NO order button, NO subscription purchase); Admin
  Orders (filter chip rail + date-grouped timeline: আজ / গত ৭ দিন /
  আগে, each row with a status dot) + Order Detail (gradient price
  hero, animated progress, Completed + Cancel); Admin Customers
  (search + rows) + Customer Detail (gradient profile header +
  Orders / Page / Subscription sections); Admin Pages + Onboarding
  (vertical numbered step tracker); Admin Leads + Chat Thread (fixed
  header, messages-only scroll, pinned composer, opens at latest, send
  status); Customer Home (greeting hero + gradient subscription hero
  + horizontal service scroll + recent own orders + one primary
  action); Services grid + Detail; Order Form (stepped progress
  1-প্যাকেজ → 2-পেমেন্ট → 3-যাচাই); My Subscription + Wallet;
  designed Empty / Loading (shimmer) / Error (+ Retry) states.

## Copy cleanup — no developer talk in the UI (v8 rule)

Every user-visible string was audited. Removed, with plain user
language in their place:

- "Demo preview" button, demo banner, demo leads / orders /
  subscription — deleted entirely (methods removed from the stores
  too). Empty data now shows a designed empty state, never sample data.
- Login: button is only "Google দিয়ে চালিয়ে যান" — no sentence
  about where login happens. The old "setup pending" screen is now
  only "লগইন করা যাচ্ছে না / কিছুক্ষণ পর আবার চেষ্টা করুন।"
- Order confirmation: "Order placed — status Pending, admin will
  confirm" in Bengali, with no mention of any database or panel.
- Chat send status: only "Messenger-এ পাঠানো হয়েছে" on success and
  "পাঠানো যায়নি — আবার চেষ্টা করুন" + retry on failure.
- Page onboarding steps are user steps only: add the AutoMateIQ admin
  to the Page (or create your own Meta App) → Test → Connected, and
  the bot starts after connection + active subscription. No token /
  server / Render wording anywhere in the UI.
- Admin note for other-customer orders kept in meaning, in plain
  words: creating orders for another customer's account is not
  available yet; the admin manages orders here, does not create them.
- Removed browser-open buttons from login / order / chat flows and
  from the Connect screen and the About screen. Two remain, both
  user-initiated and outside those flows: "মেসেঞ্জারে জিজ্ঞাসা করুন"
  on a service detail (opens the Page so the customer can ask in
  Messenger) and "পেজ লিংক খুলুন" on a recorded customer page (opens
  that customer's own recorded link).

Grep audit (quoted string literals in all Java + values XML):
forbidden developer words = 0, including "website", "Firebase",
"Render", "server", "token", "Firestore", "backend", "redirect",
"Demo"/"demo", "live mode", "setup pending", "developer",
"placeholder", "TODO" and their Bengali equivalents. The only
"API" in UI strings is the product name "API Credit" itself.
Code identifiers unavoidably still contain these words
(`FirebaseAuthHelper`, `S_TOKENS`, collection names in code) — those
are not user-visible strings. `grep WebView` = 0 in code.

## Kept from v7 (no regressions)

- In-app Google Sign-In only (Firebase Auth, google-services plugin,
  firebase-auth / firestore / play-services-auth), same project and
  the same named Firestore database as the website (never `(default)`).
  The real `app/google-services.json` (copied from v7) must be pushed
  with the build; its contents are never quoted anywhere.
- Role gating exactly like v7: admin = signed-in email
  `shihabno.18@gmail.com` / `info@automateiq.xyz` / `admin@gmail.com`;
  admin nav = ড্যাশবোর্ড / অর্ডার / কাস্টমার / পেজ / লিডস (no order
  form, no subscription purchase, no My Subscription for admin);
  customer nav = হোম / সার্ভিস / অর্ডার / সাবস্ক্রিপশন / ওয়ালেট
  (own orders / subscription / wallet only). Login-first, no landing
  page before login.
- Orders: same `orders` shape as the website; customer places orders
  in-app with verified prices only (monthly Standard ৳700, Premium
  ৳1,000, Facebook/YouTube monthly ৳1,000, Trial ৳50, One-time
  ৳5,000, WhatsApp Standard Setup ৳1,500); amount auto-filled,
  payment card, bKash/Nagad, buyer senderPhone + trxId required,
  status pending. Admin lists all orders, filters, changes status
  Pending → In Progress → Completed / Cancelled, edits adminMessage,
  cancels. Auto-expire on admin load (monthly past-expiry
  pending/in_progress/approved → cancelled +
  "Offer ended — auto-stopped on expiry", orders stay visible,
  one-time never touched).
- Subscription derived from real monthly orders (start, expiry +1
  month, days remaining, Active/Expired, Bengali digits); one-time =
  no days display, no separate one-time function yet. Adding a
  subscription = placing a monthly order in-app.
- Wallet / API Credit recharge in-app (200/500/1000/2500, pending
  until admin approval, per-message rate never invented; Vibe
  Coding / Combo show no price).
- Payment-number card ONLY in the order + wallet forms (the website
  payment number from the website build's
  `constants.tsx` CONTACT_INFO.paymentNumber, Copy button, literal
  exactly once in code, no other phone/email anywhere).
- Leads/chat: `messenger_leads`, ownerId isolation (admin sees the
  AutoMateIQ Page leads, never another business's), pinned composer,
  auto-scroll to latest, send via the reply endpoint contract (the
  reply endpoint holds the Page access — never stated in the UI),
  owner reply auto-pauses the bot, app reloads the lead after send
  (single history writer, no duplicate writes), Refresh shows the
  same messages.
- Customers/pages: users list/detail for admin (no customer
  email/phone displayed), page records on the customer's users doc
  fields (pageName/pageLink/pageId/pageStatus/pageNotes), onboarding
  steps as user steps (above).

## NOT in v8 (separate tasks, unchanged from v7)

- Live auto-connection of other businesses' Pages.
- Bot-side auto-stop for expired customer Pages.
- A dedicated `customer_pages` collection.
- Admin-for-other-customer order creation.

## Verification (honest)

- All 7 Java files parse cleanly (checked with a Java parser; no
  Android SDK or Gradle is installed in this environment, so this is
  NOT a real Gradle build — GitHub Actions is the real build).
  `aapt2 badging` could not be run here (no Android SDK); package /
  version / SDK / permissions were verified by reading
  `app/build.gradle` (applicationId `com.automateiq.app`,
  versionCode 12, versionName 8.0, minSdk 24, targetSdk 34) and
  `AndroidManifest.xml` (INTERNET, ACCESS_NETWORK_STATE).
- Payment-number literal: exactly 1 occurrence in the whole tree
  (the `PAYMENT_NUMBER` constant in `MainActivity.java`).
- Emoji as UI icons: 0 in Java/XML (services, nav, chips and buttons
  use vector drawables or plain text).
- NOTE: this project has no Gradle wrapper in it (v7 didn't either).
  Whoever pushes it must either add a wrapper or use a
  Gradle-setup Action step; do not claim a build exists until
  Actions reports one.

## Files to push (under `android-app/` in the repo)

Push the whole `automateiq-dashboard-app-v8/` tree as `android-app/`:

- `settings.gradle`, `build.gradle`, `gradle.properties`
- `app/build.gradle`, `app/proguard-rules.pro`
- `app/google-services.json` (real file, copied from v7 — required
  for login; never quote its contents)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/automateiq/app/*.java` (7 files)
- `app/src/main/res/values/{strings,colors,dimens,styles}.xml`
- `app/src/main/res/drawable/ic_*.xml` (18 vector drawables) +
  `ic_launcher.png`
- `app/src/main/assets/fonts/NotoSansBengali-{Regular,Bold}.ttf`

Do NOT deploy and do NOT send an APK from this task.
