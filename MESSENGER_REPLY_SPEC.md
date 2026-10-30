# Messenger reply-send spec — separate workflow (no token in the website)

This document is the contract between the website's Messenger Leads
reply composer and the server/n8n workflow that actually sends a
message to Facebook Messenger. Read it before wiring a live reply.

## Why a separate workflow is required

The bot's inbound webhook (`…/webhook/automateiq-messenger`) RECEIVES
Facebook events. It does not send replies. Pointing website replies
at it will silently fail. The reply endpoint must be a SEPARATE
workflow/endpoint whose only job is to send.

## Request (website -> reply workflow)

```
POST {VITE_MESSENGER_REPLY_WEBHOOK_URL}
Content-Type: application/json
Body:
{
  "businessId": "<logged-in business customer user id>",
  "pageId": "<Facebook Page id, or null if unknown>",
  "pageName": "<Page display name>",
  "recipientPsid": "<Messenger PSID of the lead>",
  "message": "<owner's reply text>"
}
```

Rules:
- The body contains NO token, key, password, or credential of any
  kind. If a token appears in a request body, the wiring is wrong.
- The workflow must validate that `businessId` + `pageId` belong
  together (the Page belongs to that business) before sending, and
  must reject mismatches. Never trust the browser alone for this.
- Rate-limit / queue replies per Page so a burst of owner replies
  cannot hammer the Graph API.

## Server-side send (reply workflow -> Facebook)

1. Look up that business's Page Access Token server-side only
   (n8n credential store, secret manager, or a server env store).
   Never return it to the browser, never log it, never put it in
   the website code.
2. Call the Facebook Graph API Messenger Send endpoint for that
   Page, with `recipient.id = recipientPsid` and the message text.
3. Return HTTP 200 only when Facebook accepted the message.
   Return a non-2xx status (with a generic error, no token) on
   failure, so the website marks the message "Pending send" instead
   of falsely claiming delivery.

## Website behaviour on response

- 2xx -> message is marked "Sent to Messenger" in the thread.
- Anything else / network error -> message is saved locally in
  the thread and marked "Pending send — webhook not configured"
  (or "Pending send" on send failure). The UI must never claim a
  delivery that did not happen.

## Handover event wiring (bot -> Firestore, still to build)

When a Messenger customer asks the bot for a human, the bot must
write a `messenger_leads` document for the CORRECT ownerId + pageId
with status `handover` (see services/messengerLeadsFirestore.ts
for the field list). It must NOT write into `support_messages` —
that collection is the business customer <-> developer team inbox.
Until that wiring exists, new handovers will not appear in the
website's Handover queue from live data; the queue currently shows
local demo data plus any Firestore rows the bot writes.

## Still needed for a fully live version

1. Per-Page Messenger webhook subscription in the Meta app, so
   Facebook delivers that Page's messages to the backend.
2. A Page token per business, stored server-side only.
3. The reply-send workflow above, deployed and tested with a
   test Page before any real customer thread.
4. Firestore `messenger_leads` rules deployed (see firestore.rules)
   and the bot's write path using the same ownerId/pageId fields.
