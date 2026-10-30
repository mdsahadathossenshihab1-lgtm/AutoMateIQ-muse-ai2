<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/drive/1jhT6k1CvoD11eVrq7-B7jIWixKcJCj92

## Remaining features added (second build pass — uncommitted, ready to publish)

- **Admin Panel** (admin only, Dashboard tab): Customers list,
  admin conversation list with linked-order context, Pending
  orders queue (Pending → In Progress → Completed / Cancelled),
  and a wallet verification queue. See `components/AdminPanel.tsx`.
- **Order detail + status flow:** Recent Orders are clickable and
  open a detail view (package, price, payment method, sender phone,
  TrxID, status, date) with a detail-collection form (Page link,
  business details, access notes, follow-up info) and a link to
  its support thread, plus a status filter on the orders list.
- **Logged-in Wallet:** client Dashboard "Wallet" tab with balance
  (approved recharges only), recharge history, and a recharge form
  (৳200/৳500/৳1,000/৳2,500, bKash/Nagad Personal, sender phone +
  TrxID). Recharges stay Pending until an admin approves them —
  payment is NOT auto-verified, and usage deduction is not yet
  implemented.
- **Messenger Leads live backend wiring (code + docs, no token):**
  Firestore `messenger_leads` design, a realtime/polling hook for
  the handover badge, updated `firestore.rules`, and a separate
  reply-send spec in `MESSENGER_REPLY_SPEC.md`. Local demo data
  is still the fallback until the bot writes live leads.
- **Content pages:** FAQ, Refund & Renewal Terms, and Privacy &
  Terms replace the old "#" footer legal links.
- **Still demo vs live:** wallet recharge is Pending verification,
  not auto-verified; Messenger Leads live data still needs a
  per-Page webhook + per-business token + the reply-send workflow;
  admin role must be enforced server-side in Firestore rules, not
  only in the UI. Full details: see `MESSENGER_LEADS.md`.

## New: Messenger Leads (separate from the support Inbox)

Dashboard → **Messenger Leads** lets a logged-in business customer see
ONLY their own Messenger customers/leads, with a Handover queue + badge,
thread view, reply composer, notes, and resolve/close — all from the
website. It does not replace or change the existing support Inbox
(customer ↔ developer team). Replies go to a configurable webhook
(`VITE_MESSENGER_REPLY_WEBHOOK_URL`, see `.env.example`); with no webhook
configured, replies are saved locally and marked "Pending send" — never
claimed as delivered to Messenger. Demo data is local only. Full details,
files changed, and the backend pieces still needed for a live version:
see [MESSENGER_LEADS.md](MESSENGER_LEADS.md). No token/key is hardcoded.

## Run Locally

**Prerequisites:**  Node.js


1. Install dependencies:
   `npm install`
2. Set the `GEMINI_API_KEY` in [.env.local](.env.local) to your Gemini API key
3. Run the app:
   `npm run dev`
