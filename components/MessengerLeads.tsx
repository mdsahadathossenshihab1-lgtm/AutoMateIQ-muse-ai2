import React, { useMemo, useState } from 'react';
import { useAuth } from '../AuthContext';
import { MessengerLead, MessengerLeadStatus } from '../types';
import {
  loadMessengerLeads, saveMessengerLeads, sendMessengerReply,
  isReplyWebhookConfigured, maskPsid, LEAD_STATUS_LABEL,
} from '../services/messengerLeads';
import { useLiveMessengerLeads, updateFirestoreLead } from '../services/messengerLeadsFirestore';
import { Search, Send, ArrowLeft, MessageCircle, BellRing, CheckCircle, XCircle, StickyNote, Eye, EyeOff, AlertTriangle, Hand, Bot } from 'lucide-react';

type Filter = 'all' | 'handover' | MessengerLeadStatus;

const STATUS_STYLE: Record<MessengerLeadStatus, string> = {
  new: 'bg-blue-50 text-blue-600 border-blue-100',
  bot_replied: 'bg-slate-100 text-slate-600 border-slate-200',
  handover: 'bg-orange-50 text-orange-600 border-orange-200',
  owner_replied: 'bg-green-50 text-green-600 border-green-100',
  closed: 'bg-slate-100 text-slate-500 border-slate-200',
};

/**
 * Messenger Leads — the business customer's OWN Messenger customers.
 * Distinct from the support Inbox (customer <-> developer team):
 * this section never shows another business's leads. Isolation is by
 * the logged-in user's id (ownerId) in loadMessengerLeads().
 */
const MessengerLeads: React.FC = () => {
  const { user } = useAuth();
  const ownerId = user?.id || '';
  const { leads: liveLeads, source: liveSource } = useLiveMessengerLeads(ownerId);
  const [leads, setLeads] = useState<MessengerLead[]>(() => loadMessengerLeads(ownerId));
  // Real leads only: when Firestore has live leads, show those by
  // default (not demo seeds, not a hidden banner button). Local state
  // is only for replies/notes on leads already shown.
  const shownLeads = liveSource !== 'local' ? liveLeads : leads;
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState<Filter>('all');
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [reply, setReply] = useState('');
  const [note, setNote] = useState('');
  const [showFullPsid, setShowFullPsid] = useState(false);
  const [sending, setSending] = useState(false);

  const update = (next: MessengerLead[]) => { setLeads(next); saveMessengerLeads(ownerId, next); };
  const handoverCount = shownLeads.filter((l) => l.status === 'handover').length;

  const filtered = useMemo(() => shownLeads.filter((l) => {
    const term = search.toLowerCase();
    const matchesSearch = !term || l.name.toLowerCase().includes(term) || l.lastMessage.toLowerCase().includes(term) || l.sourcePage.toLowerCase().includes(term);
    const matchesFilter = filter === 'all' ? true : filter === 'handover' ? l.status === 'handover' : l.status === filter;
    return matchesSearch && matchesFilter;
  }).sort((a, b) => new Date(b.updated_at).getTime() - new Date(a.updated_at).getTime()), [shownLeads, search, filter]);

  const selected = shownLeads.find((l) => l.id === selectedId) || null;

  const handleTakeOver = async () => {
    if (!selected) return;
    // Human takes over: bot must pause for this PSID. The Firestore
    // paused flag is written here; the bot-side pause check is still
    // to be wired, so until then the bot may still reply too — the
    // warning below says this plainly, it is not hidden.
    await updateFirestoreLead(selected.id, { status: 'handover', paused: true });
    update(leads.map((l) => l.id === selected.id ? { ...l, status: 'handover' as MessengerLeadStatus, updated_at: new Date().toISOString() } : l));
  };

  const handleResumeAI = async () => {
    if (!selected) return;
    await updateFirestoreLead(selected.id, { status: 'bot_replied', paused: false });
    update(leads.map((l) => l.id === selected.id ? { ...l, status: 'bot_replied' as MessengerLeadStatus, updated_at: new Date().toISOString() } : l));
  };

  const handleSendReply = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selected || !reply.trim() || sending) return;
    setSending(true);
    const delivery = await sendMessengerReply(selected, reply.trim());
    const now = new Date().toISOString();
    const next = leads.map((l) => l.id === selected.id ? {
      ...l,
      status: 'owner_replied' as MessengerLeadStatus,
      lastMessage: reply.trim(),
      updated_at: now,
      messages: [...l.messages, { id: Date.now().toString(), sender: 'owner' as const, text: reply.trim(), created_at: now, delivery }],
    } : l);
    update(next); setReply(''); setSending(false);
  };

  const handleResolve = () => {
    if (!selected) return;
    update(leads.map((l) => l.id === selected.id ? { ...l, status: 'closed' as MessengerLeadStatus, updated_at: new Date().toISOString() } : l));
  };

  const handleSaveNote = () => {
    if (!selected) return;
    update(leads.map((l) => l.id === selected.id ? { ...l, note } : l));
  };

  if (!user) return <div className="p-8 text-slate-500">লগইন করুন।</div>;

  return (
    <div className="space-y-6 animate-fade-in-up">
      <div className="bg-amber-50 border border-amber-200 rounded-2xl p-4 flex items-start gap-3">
        <AlertTriangle size={20} className="text-amber-600 shrink-0 mt-0.5" />
        <p className="text-sm text-amber-800 leading-relaxed">
          <b>Messenger Leads — আপনার নিজের কাস্টমার।</b> এখানে শুধু আপনার পেইজে আসলেই মেসেজ করা কাস্টমাররা দেখা যায় — অন্য কোনো ব্যবসার কাস্টমার এখানে আসে না, আর কোনো ডেমো চ্যাট দেখানো হয় না।
          এটি সাপোর্ট Inbox (কাস্টমার ↔ ডেভলপার টিম) থেকে আলাদা।
          {isReplyWebhookConfigured()
            ? ' রিপ্লাই webhook কনফিগার করা আছে, তাই রিপ্লাই Messenger-এ পাঠানো হবে।'
            : ' রিপ্লাই webhook এখনো কনফিগার করা হয়নি, তাই রিপ্লাই শুধু এখানে সেভ হবে এবং "Pending send" দেখাবে — Messenger-এ পৌঁছেছে বলে দেখানো হবে না।'}
          {liveSource !== 'local'
            ? ` লাইভ Firestore থেকে ${liveLeads.length}টি আসল লিড দেখানো হচ্ছে।`
            : ' এখনো কোনো আসল লিড আসেনি — Page-এ কেউ মেসেজ করলেই এখানে দেখা যাবে।'}
        </p>
      </div>

      <div className="bg-orange-50 border border-orange-200 rounded-2xl p-4 text-sm text-orange-800 leading-relaxed">
        <b>সতর্কতা — Handover / Take Over এখনো পুরো লাইভ না:</b> এই পেজে Take Over / Resume AI বোতাম দিয়ে লিডের স্ট্যাটাস + paused ফ্ল্যাগ Firestore-এ লেখা হয়, কিন্তু বট এখনো সেই paused ফ্ল্যাগ চেক করে না — তাই বটের pause-check চালু না হওয়া পর্যন্ত বটও রিপ্লাই দিতে পারে, ডাবল রিপ্লাই হতে পারে। তাই এখনই কাস্টমারকে এখান থেকে রিপ্লাই দেওয়ার আগে বটের pause-check লাইভ হতে হবে। Reply webhook চালু হলে রিপ্লাই Messenger-এ যাবে, না হলে শুধু এখানে সেভ হবে (Pending send)।
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <div className="relative flex-1 min-w-[220px]">
          <Search className="absolute left-4 top-3.5 text-slate-400" size={18} />
          <input type="text" placeholder="লিড খুঁজুন (নাম / মেসেজ / পেইজ)..." value={search} onChange={(e) => setSearch(e.target.value)} className="w-full pl-12 pr-4 py-3 bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 text-slate-900 font-medium placeholder-slate-400" />
        </div>
        {(['all', 'handover', 'new', 'bot_replied', 'owner_replied', 'closed'] as Filter[]).map((f) => (
          <button key={f} onClick={() => setFilter(f)} className={`px-4 py-2.5 rounded-xl text-sm font-bold border transition-colors ${filter === f ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-slate-600 border-slate-200 hover:border-blue-300'}`}>
            {f === 'all' ? 'All' : f === 'handover' ? `Handover (${handoverCount})` : LEAD_STATUS_LABEL[f as MessengerLeadStatus]}
          </button>
        ))}
      </div>

      {!selected ? (
        <div className="grid grid-cols-1 gap-4">
          {filtered.length === 0 && <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200"><MessageCircle size={48} className="mx-auto mb-4 opacity-30" /><p>কোনো আসল লিড পাওয়া যায়নি। Page-এ কেউ মেসেজ করলেই এখানে দেখা যাবে।</p></div>}
          {filtered.map((lead) => (
            <button key={lead.id} onClick={() => { setSelectedId(lead.id); setNote(lead.note || ''); setShowFullPsid(false); }} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-md hover:border-blue-300 transition-all text-left flex flex-col md:flex-row md:items-center gap-4">
              <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 font-bold border border-blue-100 shrink-0">{(lead.name || '?').charAt(0)}</div>
              <div className="flex-1 min-w-0">
                <div className="flex flex-wrap items-center gap-2"><h4 className="font-bold text-slate-900">{lead.name}</h4><span className={`px-2 py-0.5 rounded-full text-[11px] font-bold border ${STATUS_STYLE[lead.status]}`}>{LEAD_STATUS_LABEL[lead.status]}</span>{lead.status === 'handover' && <span className="flex items-center gap-1 text-orange-600 text-xs font-bold"><BellRing size={13} /> Handover</span>}</div>
                <p className="text-sm text-slate-500 truncate mt-1">{lead.lastMessage}</p>
                <p className="text-xs text-slate-400 mt-1">{lead.sourcePage} • PSID {maskPsid(lead.psid)} • {new Date(lead.updated_at).toLocaleString()}</p>
              </div>
              <span className="text-blue-600 text-sm font-bold shrink-0">Open thread →</span>
            </button>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-6 border-b border-slate-200 flex flex-wrap items-center gap-3 justify-between">
            <div>
              <button onClick={() => setSelectedId(null)} className="mb-2 flex items-center gap-2 text-slate-500 hover:text-slate-900 font-bold text-sm"><ArrowLeft size={16} /> Back to leads</button>
              <h3 className="font-bold text-xl text-slate-900">{selected.name}</h3>
              <p className="text-sm text-slate-500 mt-1 flex flex-wrap items-center gap-2">
                {selected.sourcePage} •
                <span className="font-mono">PSID {showFullPsid ? selected.psid : maskPsid(selected.psid)}</span>
                <button onClick={() => setShowFullPsid(!showFullPsid)} className="text-blue-600 font-bold flex items-center gap-1">{showFullPsid ? <EyeOff size={14} /> : <Eye size={14} />}{showFullPsid ? 'Hide' : 'Show full PSID'}</button>
                <span className={`px-2 py-0.5 rounded-full text-[11px] font-bold border ${STATUS_STYLE[selected.status]}`}>{LEAD_STATUS_LABEL[selected.status]}</span>
              </p>
            </div>
            {selected.status === 'handover' && <button onClick={handleResolve} className="flex items-center gap-2 bg-green-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-green-500"><CheckCircle size={18} /> Mark handover resolved</button>}
            {selected.status !== 'closed' && selected.status !== 'handover' && <button onClick={handleResolve} className="flex items-center gap-2 bg-slate-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-slate-500"><XCircle size={18} /> Close lead</button>}
            {selected.status !== 'handover' && <button onClick={handleTakeOver} className="flex items-center gap-2 bg-orange-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-orange-500"><Hand size={18} /> Take Over (Human)</button>}
            {selected.status === 'handover' && <button onClick={handleResumeAI} className="flex items-center gap-2 bg-indigo-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-indigo-500"><Bot size={18} /> Resume AI</button>}
          </div>

          <div className="p-6 space-y-4 max-h-[480px] overflow-y-auto bg-slate-50">
            {selected.messages.map((m) => (
              <div key={m.id} className={`flex ${m.sender === 'owner' ? 'justify-end' : 'justify-start'}`}>
                <div className="max-w-[75%]">
                  <p className="text-[11px] font-bold text-slate-400 mb-1">{m.sender === 'customer' ? selected.name : m.sender === 'bot' ? 'Bot (Auto)' : 'Owner (you)'}</p>
                  <div className={`px-5 py-3.5 rounded-2xl text-sm leading-relaxed shadow-sm border ${m.sender === 'owner' ? 'bg-blue-600 text-white border-blue-600 rounded-br-sm' : m.sender === 'bot' ? 'bg-indigo-50 border-indigo-100 text-slate-800 rounded-bl-sm' : 'bg-white border-slate-200 text-slate-800 rounded-bl-sm'}`}>
                    <p className="whitespace-pre-wrap">{m.text}</p>
                  </div>
                  <p className="text-[10px] text-slate-400 mt-1">{new Date(m.created_at).toLocaleString()}{m.delivery === 'pending_send' ? ' • Pending send — webhook not configured' : m.delivery === 'delivered' ? ' • Sent to Messenger' : ''}</p>
                </div>
              </div>
            ))}
          </div>

          <form onSubmit={handleSendReply} className="p-5 bg-white border-t border-slate-200">
            <div className="flex gap-3 bg-slate-50 p-2 rounded-2xl border border-slate-200 focus-within:border-blue-500">
              <input type="text" value={reply} onChange={(e) => setReply(e.target.value)} placeholder="Messenger-এ রিপ্লাই লিখুন..." className="flex-1 px-4 bg-transparent outline-none text-slate-900 placeholder-slate-400 font-medium" />
              <button type="submit" disabled={!reply.trim() || sending} className="bg-blue-600 text-white p-3 rounded-xl hover:bg-blue-500 disabled:opacity-50 flex items-center gap-2 font-bold"><Send size={18} /> {sending ? 'Sending...' : 'Reply'}</button>
            </div>
            {!isReplyWebhookConfigured() && <p className="text-xs text-amber-600 mt-2">Webhook কনফিগার করা হয়নি — রিপ্লাই শুধু এই থ্রেডে সেভ হবে, Messenger-এ যাবে না। কনফিগার করতে .env-এ VITE_MESSENGER_REPLY_WEBHOOK_URL সেট করুন (কোনো টোকেন কোডে লিখবেন না)।</p>}
          </form>

          <div className="p-5 bg-slate-50 border-t border-slate-200 flex gap-3">
            <StickyNote size={18} className="text-slate-400 shrink-0 mt-3" />
            <input type="text" value={note} onChange={(e) => setNote(e.target.value)} placeholder="এই লিডের জন্য নোট লিখুন..." className="flex-1 px-4 py-3 bg-white border border-slate-200 rounded-xl outline-none text-slate-900 font-medium placeholder-slate-400" />
            <button onClick={handleSaveNote} className="bg-white border border-slate-200 text-slate-700 font-bold px-5 rounded-xl hover:bg-slate-100">Save note</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default MessengerLeads;
