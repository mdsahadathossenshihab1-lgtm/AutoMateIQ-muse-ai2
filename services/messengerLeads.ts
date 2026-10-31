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

/** Load ONLY this owner's leads. No demo seeds are created anymore:
 *  earlier versions seeded 3 demo leads (Karim/Rahim/Nasrin) per owner
 *  into localStorage, which is why the Messenger Leads page showed
 *  chats that never messaged the Page. Any stored demo leads
 *  (id starting with "demo-") are dropped on load, so a user who
 *  already has the old seeds sees an empty, real-only list. */
export function loadMessengerLeads(ownerId: string): MessengerLead[] {
  if (!ownerId) return [];
  try {
    const raw = localStorage.getItem(storageKey(ownerId));
    if (raw) {
      const parsed = JSON.parse(raw) as MessengerLead[];
      // Defence in depth: drop any record that does not belong here,
      // and drop all old demo seeds — they are not real customers.
      const real = parsed.filter((l) => l.ownerId === ownerId && !l.id.startsWith('demo-'));
      if (real.length !== parsed.length) {
        try { localStorage.setItem(storageKey(ownerId), JSON.stringify(real)); } catch { /* ignore */ }
      }
      return real;
    }
  } catch { /* fall through to empty */ }
  return [];
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
