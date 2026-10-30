import { MessengerLead, MessengerLeadStatus } from '../types';

/**
 * Messenger Leads — reply configuration.
 *
 * Set this in a .env.local file (see .env.example):
 *   VITE_MESSENGER_REPLY_WEBHOOK_URL=https://your-n8n/webhook/messenger-reply
 *
 * IMPORTANT — do NOT point this at the bot's inbound webhook
 * (…/webhook/automateiq-messenger). That endpoint RECEIVES Facebook
 * events; it does not send replies. The reply webhook must be a
 * separate endpoint (for example an n8n workflow) that takes
 * { businessId, pageId, recipientPsid, message } and calls the
 * Facebook Graph API Messenger Send endpoint with that business's
 * own Page token, stored server-side. No token or key is ever put
 * in this code, in the request body, or in localStorage.
 *
 * When this is empty (the default), replies are NOT claimed to be
 * delivered to Messenger: they are saved locally to the lead's
 * thread and marked "Pending send — webhook not configured".
 */
export const MESSENGER_REPLY_WEBHOOK_URL: string =
  (import.meta as any).env?.VITE_MESSENGER_REPLY_WEBHOOK_URL || '';

export const isReplyWebhookConfigured = () => MESSENGER_REPLY_WEBHOOK_URL.length > 0;

/** Show first 4 + last 4 characters only, e.g. 1234••••7890. */
export function maskPsid(psid: string): string {
  if (!psid || psid.length <= 8) return '••••';
  return `${psid.slice(0, 4)}••••${psid.slice(-4)}`;
}

const storageKey = (ownerId: string) => `automateiq_messenger_leads_${ownerId}`;

/** Demo seed data. Clearly labelled DEMO in the UI; never real customers.
 *  Seeded per ownerId, so one business can never see another's leads. */
function seedLeads(ownerId: string): MessengerLead[] {
  const now = Date.now();
  const iso = (minsAgo: number) => new Date(now - minsAgo * 60000).toISOString();
  return [
    {
      id: `demo-${ownerId}-1`,
      ownerId,
      sourcePage: 'Your Facebook Page',
      name: 'Rahim Uddin (demo)',
      psid: '1234567890123456',
      status: 'handover',
      lastMessage: 'আমি মানুষের সাথে কথা বলতে চাই',
      updated_at: iso(12),
      messages: [
        { id: 'm1', sender: 'customer', text: 'Messenger Chatbot Premium এর দাম কত?', created_at: iso(40) },
        { id: 'm2', sender: 'bot', text: 'Premium মাসিক ৳১,০০০, One-Time ৳৫,০০০। 24/7 অটো রিপ্লাই, ড্যাশবোর্ড, Premium-এ ভয়েস সাপোর্ট আছে।', created_at: iso(38) },
        { id: 'm3', sender: 'customer', text: 'আমি মানুষের সাথে কথা বলতে চাই', created_at: iso(12) },
      ],
    },
    {
      id: `demo-${ownerId}-2`,
      ownerId,
      sourcePage: 'Your Facebook Page',
      name: 'Nasrin Akter (demo)',
      psid: '9876543210987654',
      status: 'bot_replied',
      lastMessage: 'Standard প্যাকেজে কি কি আছে?',
      updated_at: iso(90),
      messages: [
        { id: 'm1', sender: 'customer', text: 'Standard প্যাকেজে কি কি আছে?', created_at: iso(95) },
        { id: 'm2', sender: 'bot', text: 'Standard-এ 24/7 অটো রিপ্লাই, ড্যাশবোর্ড ও API ইন্টিগ্রেশন আছে। বিস্তারিত জানতে বলুন।', created_at: iso(90) },
      ],
    },
    {
      id: `demo-${ownerId}-3`,
      ownerId,
      sourcePage: 'Your Facebook Page',
      name: 'Karim Mia (demo)',
      psid: '5555000011112222',
      status: 'new',
      lastMessage: 'হ্যালো, আপনাদের সার্ভিস সম্পর্কে জানতে চাই',
      updated_at: iso(5),
      messages: [
        { id: 'm1', sender: 'customer', text: 'হ্যালো, আপনাদের সার্ভিস সম্পর্কে জানতে চাই', created_at: iso(5) },
      ],
    },
  ];
}

/** Load ONLY this owner's leads. The ownerId filter is the isolation
 *  boundary: callers must pass the logged-in user's id, never 'all'. */
export function loadMessengerLeads(ownerId: string): MessengerLead[] {
  if (!ownerId) return [];
  try {
    const raw = localStorage.getItem(storageKey(ownerId));
    if (raw) {
      const parsed = JSON.parse(raw) as MessengerLead[];
      // Defence in depth: drop any record that does not belong here.
      return parsed.filter((l) => l.ownerId === ownerId);
    }
  } catch { /* fall through to seed */ }
  const seeded = seedLeads(ownerId);
  try { localStorage.setItem(storageKey(ownerId), JSON.stringify(seeded)); } catch { /* private mode */ }
  return seeded;
}

export function saveMessengerLeads(ownerId: string, leads: MessengerLead[]): void {
  try {
    localStorage.setItem(storageKey(ownerId), JSON.stringify(leads.filter((l) => l.ownerId === ownerId)));
  } catch { /* ignore */ }
}

export function getHandoverCount(ownerId: string): number {
  return loadMessengerLeads(ownerId).filter((l) => l.status === 'handover').length;
}

export const LEAD_STATUS_LABEL: Record<MessengerLeadStatus, string> = {
  new: 'New',
  bot_replied: 'Bot replied',
  handover: 'Handover',
  owner_replied: 'Replied by owner',
  closed: 'Closed',
};

/**
 * Send a website reply. Returns 'delivered' only when the configured
 * webhook answered OK; otherwise 'pending_send' and the message is
 * still saved locally with that flag, so the UI never claims a
 * Messenger delivery that did not happen. No token is sent.
 */
export async function sendMessengerReply(lead: MessengerLead, text: string): Promise<'delivered' | 'pending_send'> {
  if (!isReplyWebhookConfigured()) return 'pending_send';
  try {
    const res = await fetch(MESSENGER_REPLY_WEBHOOK_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        businessId: lead.ownerId,
        pageId: lead.sourcePageId || null,
        pageName: lead.sourcePage,
        recipientPsid: lead.psid,
        message: text,
      }),
    });
    return res.ok ? 'delivered' : 'pending_send';
  } catch {
    return 'pending_send';
  }
}
