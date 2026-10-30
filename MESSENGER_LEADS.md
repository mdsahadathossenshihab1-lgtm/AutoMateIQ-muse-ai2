# Messenger Leads — new feature (added in this build)

## What was added
A NEW "Messenger Leads" / "My Customers" section in the client Dashboard
menu, **alongside** Dashboard and Inbox. The existing support Inbox is
untouched except for this one extra menu item. Pricing, order flow and
support-inbox behaviour are unchanged.

Why two inboxes? They serve two different relationships and must not
be mixed:
- **Inbox (existing)** = business customer ↔ AutoMateIQ developer team,
  for order/service details and support.
- **Messenger Leads (new)** = business customer ↔ *their own* Messenger
  customers — people who messaged that business's Facebook Page, including
  bot handovers where the customer asked for a human.

## Files changed / added
- `components/MessengerLeads.tsx` — NEW: lead list, search, status +
  Handover filter, thread view, reply composer, note, resolve/close.
- `components/Dashboard.tsx` — changed only to add the "Messenger Leads"
  menu item (both client and admin menus, with a handover badge count),
  the page title, and the tab render. No other Dashboard logic changed.
- `types.ts` — added `MessengerLead`, `MessengerMessage`,
  `MessengerLeadStatus`, `MessengerSender` types only.
- `services/messengerLeads.ts` — NEW: PSID masking, per-owner local
  storage, demo seed, and the reply-webhook sender. No token/key anywhere.
- `.env.example` — NEW: documents `VITE_MESSENGER_REPLY_WEBHOOK_URL`.
- This file + a short section in `README.md`.

## Screens / flows (all inside the website, no Messenger app needed)
1. Dashboard → **Messenger Leads**: list of your own leads only, with
   name, masked PSID, last message, time, status
   (New / Bot replied / Handover / Replied by owner / Closed) and source Page.
2. **Search** by name/message/Page; **filter** by status, or **Handover**
   tab for handover leads only. The menu item shows a red badge with the
   number of open Handover leads.
3. Click a lead → **thread view**: full history, labelled Customer /
   Bot (Auto) / Owner (you). PSID is masked by default; "Show full PSID"
   reveals it only in that thread.
4. **Reply** in the thread composer → see "How replies work" below.
5. **Mark handover resolved / Close lead**, and **Save note** on a lead.

## Data isolation
Isolation is by logged-in business account: every lead carries `ownerId`
(the logged-in user's id). `loadMessengerLeads(ownerId)` reads only the
`automateiq_messenger_leads_<ownerId>` local store and drops any record
whose `ownerId` does not match. One business can never open another
business's leads through this UI.

## How replies work — real vs demo (read this before going live)
- **Reply webhook configured** (`VITE_MESSENGER_REPLY_WEBHOOK_URL` set):
  the composer POSTs `{ businessId, pageId, recipientPsid, message }`
  (no token in the body) to that URL and, only on an OK response, marks
  the message "Sent to Messenger".
- **No webhook configured (default)**: the reply is saved locally to
  that lead's thread and marked **"Pending send — webhook not configured"**.
  It is NEVER claimed to have reached Messenger.
- **Demo data**: the first time an owner opens the section, 3 demo leads
  (labelled "(demo)" in names, banner in the UI) are seeded locally so
  the flows can be tested without any backend. They are not real
  customers. Clear them by clearing that owner's localStorage key.

## How to run / test locally
1. `npm install`
2. `npm run dev` (no webhook needed for UI testing — replies stay local
   and show "Pending send").
3. Log in as a client, open Dashboard → Messenger Leads, search/filter,
   open the Handover lead, reply, resolve it, add a note. Reload: the
   thread, status and note persist for that logged-in account only.
4. Log in as a different client account: you will see only that
   account's own (fresh demo) leads, never the first account's.
5. Optional live-send test: set `VITE_MESSENGER_REPLY_WEBHOOK_URL` to a
   test n8n reply webhook, restart dev, reply again, and confirm the
   webhook received the POST (with no token in it) before trusting it.

## What is still needed for a fully live version
1. **Messenger webhook subscription per Page** (Meta app): Facebook
   must deliver that Page's messages/handover events to the backend.
2. **Page token per business**, stored server-side (never in this code):
   the reply endpoint must use the *owning business's* token to send.
3. **A backend store** (e.g. a Firestore `messenger_leads` collection
   with `ownerId` + `pageId` rules, mirroring the existing
   `support_messages` pattern) replacing the localStorage store, plus a
   realtime listener so new handovers raise the badge without reload.
4. **A reply-send step** (n8n or server) that takes the webhook POST and
   calls Facebook Graph API Messenger Send, returning success/failure
   so the UI can mark Delivered vs Pending correctly.
5. **Handover event wiring**: the bot must write a Handover record for
   the right business/Page (not to the developer-team support inbox)
   when a customer asks for a human.

## Security notes
- No password, API key, or Page token is in this code, in `.env.example`,
  or in localStorage. The reply POST carries no credential.
- PSIDs are masked by default in list and thread headers.
- Do not point the reply webhook at the bot's inbound webhook; that
  endpoint receives events, it does not send replies.

## Remaining features added (second build pass — uncommitted, ready for parent to publish)

### 1. Admin Panel (role-gated)
- NEW `components/AdminPanel.tsx`, Dashboard tab "Admin Panel" for
  admins only. It does NOT replace the existing Users/Support/Settings
  tabs; it links them together.
- Customers list: business customers with their orders, services,
  and pending counts; click a customer to expand their orders.
- Admin conversation list: support threads per customer, with that
  customer's latest order shown as context, and a link from a thread
  to its order detail (and from an order detail back to its thread).
- Pending orders queue: Pending -> In Progress -> Completed /
  Cancelled via OrderDetail status actions. Legacy 'approved' /
  'rejected' orders are still readable; new actions use the new
  statuses (filter maps approved -> In Progress, rejected ->
  Cancelled for display).
- Wallet verification queue inside the Admin Panel: approve/reject
  pending wallet recharges. Clients cannot see this panel, other
  customers, or other customers' wallet queues.
- Admin role: UI gate is `user.role === 'admin'`, which comes from
  the repo's existing `isEmailAdmin` check (admin email list +
  `users/{uid}.role`). That UI gate is NOT the security boundary —
  `firestore.rules` was updated to enforce admin-only status
  changes, admin-only wallet status changes, and per-owner
  messenger_leads / wallet_recharges access. Deploy those rules;
  do not rely on hiding a menu item.

### 2. Order detail + status flow
- NEW `components/OrderDetail.tsx`. Recent Orders in the client
  Dashboard are now clickable and open this detail view; admin
  order cards also have a "View detail" button.
- Detail shows: package, price/amount, payment method (bKash/Nagad),
  sender phone, TrxID, status, date, customer, and a
  detail-collection form (Page link, business details Doc/Sheet
  link, access notes, follow-up info, email, WhatsApp, other
  requirements). The form saves via `updateOrderDetails`, so the
  customer and the admin see the same saved fields.
- Status filter on the orders list: All / Pending / In Progress /
  Completed / Cancelled.
- Link both ways: order detail -> "Go to support thread for this
  order", and Admin Panel thread -> "Linked order -> Open detail".
  The support thread is customer <-> developer team, NOT the
  Messenger Leads thread for that business's own customers.
- Orders persist via the existing Firestore `orders` collection
  (AuthContext / firebaseClient) — no new localStorage order store
  was added. New detail fields are `accessNotes` and `followUpInfo`
  on `Order` in `types.ts`.

### 3. Logged-in Wallet
- NEW `components/Wallet.tsx` (client Dashboard tab "Wallet") and
  `services/wallet.ts`, plus Firestore `wallet_recharges`
  methods in `services/firebaseClient.ts`.
- Shows balance, recharge history, and a Recharge form: amounts
  ৳200 / ৳500 / ৳1,000 / ৳2,500, bKash/Nagad (Personal), sender
  phone + TrxID, same verification pattern as orders.
- **Demo vs live — read this:** a recharge is created as Pending
  and stays Pending until an admin approves it in the Admin Panel.
  Payment is NOT auto-verified, and balance = approved recharges
  only. Usage deduction is NOT implemented (no usage ledger yet),
  so the balance is approved-recharge total, not net balance
  after usage. Wallet storage is Firestore-first with a
  per-account localStorage fallback (see `services/wallet.ts`);
  the fallback is single-device demo storage, not a shared
  backend, and an admin on another device will not see it.
  TODO: remove the fallback once Firestore wallet rules are
  deployed and verified for both roles.

### 4. Messenger Leads live backend wiring (code + docs, no live token)
- NEW `services/messengerLeadsFirestore.ts`: Firestore
  `messenger_leads` collection design (`ownerId` + `pageId` fields,
  plus name, psid, status, lastMessage, updated_at, messages, note),
  `fetchFirestoreLeads`, and the hooks `useLiveMessengerLeads` /
  `useLiveHandoverCount`.
- Realtime: the hook tries Firestore `onSnapshot` first; if it
  fails or returns empty, it polls `getDocs` every 20s. An empty
  Firestore result is treated as "no live data yet" and does NOT
  wipe the local demo leads. The Dashboard Messenger Leads badge
  now uses `useLiveHandoverCount`, so it lights up from live
  handover rows as soon as the bot writes them.
- NEW `MESSENGER_REPLY_SPEC.md`: the separate reply-send workflow
  contract — POST `{ businessId, pageId, recipientPsid, message }`
  (no token in body), server validates business+Page, looks up
  that business's Page token server-side only, calls Graph API
  Messenger Send, and returns 2xx only on acceptance.
- `firestore.rules`: added `messenger_leads` and
  `wallet_recharges` rules (per-owner + admin, ownerId/status
  immutability as documented there) and tightened `orders`
  updates so clients cannot change order status themselves.
- LocalStorage fallback is KEPT in Messenger Leads so the UI still
  works without Firestore, and demo seed stays clearly marked.
  The full list migration (bot writes leads to Firestore, UI
  reads only Firestore) is still TODO until the bot writes there.

### 5. Content pages (no "#" legal links left)
- NEW `components/ContentPages.tsx`: FAQ, Refund & Renewal Terms,
  and Privacy Policy & Terms of Service, opened as a modal from
  the footer. Footer Legal links in `App.tsx` now open these
  pages instead of `href="#"`, and FAQ was added to Quick Links.
- Content is honest and matches the site's existing packages:
  no invented guarantees, no invented per-message/per-call rate
  (customers are told to ask in the support inbox), manual
  payment verification, case-by-case refunds, and monthly
  packages that do not auto-renew on their own from the website.

### Files changed in this pass
- NEW: `components/AdminPanel.tsx`, `components/OrderDetail.tsx`,
  `components/Wallet.tsx`, `components/ContentPages.tsx`,
  `services/wallet.ts`, `services/messengerLeadsFirestore.ts`,
  `MESSENGER_REPLY_SPEC.md`.
- CHANGED: `types.ts` (OrderStatus, accessNotes, followUpInfo,
  WalletRecharge, updateOrderStatus signature),
  `AuthContext.tsx` (updateOrderStatus accepts new statuses),
  `services/firebaseClient.ts` (wallet_recharges methods),
  `services/messengerLeads.ts` (unchanged behaviour),
  `components/MessengerLeads.tsx` (banner notes the Firestore-ready
  layer), `components/Dashboard.tsx` (Admin Panel + Wallet tabs,
  live handover badge, clickable orders + status filter, order
  detail modal, new status badges), `App.tsx` (content-page modal,
  footer links), `firestore.rules` (messenger_leads,
  wallet_recharges, admin-only order status), `.env.example`
  (reply-spec reference), `MESSENGER_LEADS.md` (this section),
  `README.md` (remaining-features summary).
- UNCHANGED in behaviour: pricing, the existing support Inbox
  (customer <-> developer team), and the existing Users/Support/
  Settings dashboard tabs, except for the added "View detail"
  button on admin order cards.
