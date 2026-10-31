import { useEffect, useState } from 'react';
import {
  collection, query, where, orderBy, getDocs, onSnapshot, doc, updateDoc,
} from 'firebase/firestore';
import { dbFirestore } from './firebaseClient';
import { MessengerLead } from '../types';
import { loadMessengerLeads } from './messengerLeads';

/**
 * Messenger Leads — Firestore-ready layer (no token/key anywhere).
 *
 * Collection design (see also firestore.rules, messenger_leads):
 *   messenger_leads/{leadId}
 *     ownerId: string        // logged-in business customer id — the
 *                            // isolation boundary. Never show a lead
 *                            // to a different ownerId.
 *     pageId: string | null  // Facebook Page id for this lead
 *     sourcePage: string     // Page display name
 *     name: string
 *     psid: string           // masked in UI; full value only used
 *                            // internally for the reply webhook POST
 *     status: 'new' | 'bot_replied' | 'handover' | 'owner_replied' | 'closed'
 *     lastMessage: string
 *     updated_at: string (ISO)
 *     messages: MessengerMessage[]
 *     note?: string
 *
 * Bot handover wiring (still to be built server-side): when a
 * customer asks the bot for a human, the bot / n8n workflow must
 * write (or upsert) a messenger_leads document for the CORRECT
 * ownerId + pageId with status 'handover'. It must NOT write into
 * the developer-team support inbox (support_messages).
 *
 * Realtime vs polling — what this hook actually does:
 * 1. It tries onSnapshot (Firestore realtime) first.
 * 2. If onSnapshot throws or the collection is empty for this owner,
 *    it keeps the local demo store and polls getDocs every 20s.
 *    An empty Firestore result is treated as "no live data yet",
 *    NOT as "delete the local demo leads" — otherwise a fresh
 *    Firestore project would wipe the demo UI on first load.
 * 3. When Firestore returns non-empty leads, those replace the
 *    local list in the hook's return value and source becomes
 *    'firestore-live' (realtime) or 'firestore-poll'.
 *
 * The full list migration (writing bot leads into Firestore and
 * reading ONLY Firestore in MessengerLeads.tsx) is still TODO:
 * until the bot writes here, the section's thread/reply flow still
 * uses the local store. The handover BADGE, however, already uses
 * this hook, so it will light up from live data as soon as the
 * bot starts writing handover records.
 */

export type LeadsSource = 'firestore-live' | 'firestore-poll' | 'local';

/** Update a lead's status/paused flag in Firestore (website admin side).
 *  This is what Take Over / Resume AI use. It does NOT by itself stop
 *  the bot — the bot must also check this paused flag before AI replies
 *  (bot wiring, still to be built). Until then the UI must say so. */
export async function updateFirestoreLead(leadId: string, updates: { status?: string; paused?: boolean; note?: string }): Promise<boolean> {
  try {
    await updateDoc(doc(dbFirestore, 'messenger_leads', leadId), {
      ...updates,
      updated_at: new Date().toISOString(),
    });
    return true;
  } catch {
    return false;
  }
}

function mapDoc(d: any): MessengerLead {
  const data = d.data();
  return {
    id: d.id,
    ownerId: data.ownerId,
    sourcePage: data.sourcePage || 'Your Facebook Page',
    sourcePageId: data.pageId || undefined,
    name: data.name || 'Unknown',
    psid: data.psid || '',
    status: data.status || 'new',
    lastMessage: data.lastMessage || '',
    updated_at: data.updated_at || new Date().toISOString(),
    messages: Array.isArray(data.messages) ? data.messages : [],
    note: data.note,
  };
}

export async function fetchFirestoreLeads(ownerId: string): Promise<MessengerLead[]> {
  if (!ownerId) return [];
  try {
    const q = query(
      collection(dbFirestore, 'messenger_leads'),
      where('ownerId', '==', ownerId),
      orderBy('updated_at', 'desc'),
    );
    const snap = await getDocs(q);
    return snap.docs.map(mapDoc).filter((l) => l.ownerId === ownerId);
  } catch {
    try {
      const q = query(collection(dbFirestore, 'messenger_leads'), where('ownerId', '==', ownerId));
      const snap = await getDocs(q);
      return snap.docs.map(mapDoc).filter((l) => l.ownerId === ownerId)
        .sort((a, b) => new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime());
    } catch {
      return [];
    }
  }
}

/** Live leads for one owner, with source. Never leaks another owner. */
export function useLiveMessengerLeads(ownerId: string): { leads: MessengerLead[]; source: LeadsSource } {
  const [live, setLive] = useState<MessengerLead[] | null>(null);
  const [source, setSource] = useState<LeadsSource>('local');

  useEffect(() => {
    if (!ownerId) return;
    let cancelled = false;
    let unsubscribe: (() => void) | undefined;

    const poll = async () => {
      const rows = await fetchFirestoreLeads(ownerId);
      if (!cancelled && rows.length > 0) {
        setLive(rows);
        setSource((s) => (s === 'firestore-live' ? s : 'firestore-poll'));
      }
    };

    try {
      const q = query(
        collection(dbFirestore, 'messenger_leads'),
        where('ownerId', '==', ownerId),
        orderBy('updated_at', 'desc'),
      );
      unsubscribe = onSnapshot(
        q,
        (snap) => {
          if (cancelled) return;
          const rows = snap.docs.map(mapDoc).filter((l) => l.ownerId === ownerId);
          if (rows.length > 0) {
            setLive(rows);
            setSource('firestore-live');
          }
        },
        () => { /* realtime failed — polling fallback below */ },
      );
    } catch {
      /* fall through to polling */
    }

    void poll();
    const timer = setInterval(poll, 20000);
    return () => {
      cancelled = true;
      clearInterval(timer);
      if (unsubscribe) unsubscribe();
    };
  }, [ownerId]);

  const local = loadMessengerLeads(ownerId);
  return live && live.length > 0
    ? { leads: live, source }
    : { leads: local, source: 'local' };
}

/** Handover badge count, live-aware: Firestore handover count when
 *  live data exists, otherwise the local store count. */
export function useLiveHandoverCount(ownerId: string): number {
  const { leads } = useLiveMessengerLeads(ownerId);
  return leads.filter((l) => l.status === 'handover').length;
}
