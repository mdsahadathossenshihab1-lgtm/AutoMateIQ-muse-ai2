import React, { useEffect, useState } from 'react';
import { Order, OrderStatus } from '../types';
import { X, Package, MessageCircle, Save, CheckCircle, Clock, XCircle } from 'lucide-react';

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  pending: 'Pending',
  in_progress: 'In Progress',
  approved: 'Approved (legacy)',
  rejected: 'Rejected (legacy)',
  completed: 'Completed',
  cancelled: 'Cancelled',
};

export const ORDER_STATUS_STYLE: Record<OrderStatus, string> = {
  pending: 'bg-amber-50 text-amber-600 border-amber-100',
  in_progress: 'bg-blue-50 text-blue-600 border-blue-100',
  approved: 'bg-cyan-50 text-cyan-600 border-cyan-100',
  rejected: 'bg-red-50 text-red-600 border-red-100',
  completed: 'bg-slate-100 text-slate-500 border-slate-200',
  cancelled: 'bg-red-50 text-red-600 border-red-100',
};

/**
 * Order detail view — clickable from Recent Orders (client) and from
 * the Admin Panel (admin). Shows package, price, payment method,
 * sender phone, TrxID, status, date, and a detail-collection form
 * (Page link, business details, access notes, follow-up info) that
 * the customer fills and the admin can read. The form saves via
 * updateOrderDetails, so both sides see the same saved fields.
 * "Go to support thread" links this order to its support Inbox
 * thread (customer <-> developer team) — it is NOT the Messenger
 * Leads thread for that business's own customers.
 */
const OrderDetail: React.FC<{
  order: Order;
  isAdmin: boolean;
  onClose: () => void;
  onSaveDetails: (updates: Partial<Order>) => Promise<void> | void;
  onStatusChange?: (status: OrderStatus) => Promise<void> | void;
  onOpenSupportThread: () => void;
}> = ({ order, isAdmin, onClose, onSaveDetails, onStatusChange, onOpenSupportThread }) => {
  const [pageLink, setPageLink] = useState(order.clientPageLink || '');
  const [docLink, setDocLink] = useState(order.clientDocLink || '');
  const [accessNotes, setAccessNotes] = useState(order.accessNotes || '');
  const [followUp, setFollowUp] = useState(order.followUpInfo || '');
  const [email, setEmail] = useState(order.clientEmail || '');
  const [whatsapp, setWhatsapp] = useState(order.clientWhatsapp || '');
  const [reqs, setReqs] = useState(order.clientRequirements || '');
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [changingStatus, setChangingStatus] = useState(false);
  const [statusError, setStatusError] = useState('');
  const [saveError, setSaveError] = useState('');

  // Modal must never lock the screen: Escape closes it, and the
  // background page must not scroll while it is open.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') onClose(); };
    document.addEventListener('keydown', onKey);
    const prevOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = prevOverflow;
    };
  }, [onClose]);

  const handleStatusChange = async (status: Parameters<NonNullable<typeof onStatusChange>>[0]) => {
    if (!onStatusChange || changingStatus) return;
    setStatusError('');
    setChangingStatus(true);
    try {
      await onStatusChange(status);
    } catch (e) {
      // Firestore permission errors used to fail silently here:
      // the button did nothing visible and the modal stayed open
      // with the old status, which looked like a frozen screen.
      setStatusError('Status could not be changed. Check your admin login / Firestore permission and try again.');
    } finally {
      setChangingStatus(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setSaveError('');
    try {
      await onSaveDetails({
        clientPageLink: pageLink,
        clientDocLink: docLink,
        accessNotes,
        followUpInfo: followUp,
        clientEmail: email,
        clientWhatsapp: whatsapp,
        clientRequirements: reqs,
        isDetailsSubmitted: true,
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch {
      setSaveError('Details could not be saved. Try again.');
    } finally {
      setSaving(false);
    }
  };

  const fieldCls = 'w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400';

  return (
    // items-start (NOT items-center): with items-center a modal taller
    // than the phone screen gets its top clipped and the close button
    // becomes unreachable — the screen looks locked. The outer layer
    // scrolls, the inner card is capped to the viewport height, and
    // the header stays visible while the body scrolls.
    <div onClick={onClose} className="fixed inset-0 z-[70] flex items-start justify-center p-3 sm:p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
      <div onClick={(e) => e.stopPropagation()} className="bg-white border border-slate-200 rounded-3xl shadow-2xl w-full max-w-2xl overflow-hidden animate-fade-in-up my-4 sm:my-8 flex flex-col max-h-[calc(100dvh-2rem)] sm:max-h-[calc(100dvh-4rem)]">
        <div className="bg-slate-50 p-4 sm:p-6 flex justify-between items-start gap-3 border-b border-slate-200 shrink-0">
          <div className="min-w-0">
            <p className="text-xs font-bold text-slate-400 uppercase tracking-wide flex items-center gap-2"><Package size={14} /> Order #{order.id.slice(-6)}</p>
            <h3 className="font-bold text-lg sm:text-xl text-slate-900 mt-1 break-words">{order.serviceName}</h3>
            <p className="text-sm text-slate-500 break-words">{order.packageDetails}</p>
          </div>
          <div className="flex items-center gap-3 shrink-0">
            <span className={`px-3 py-1 rounded-full text-xs font-bold border ${ORDER_STATUS_STYLE[order.status]}`}>{ORDER_STATUS_LABEL[order.status]}</span>
            <button type="button" aria-label="Close order detail" onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full"><X size={20} /></button>
          </div>
        </div>

        <div className="p-4 sm:p-6 md:p-8 space-y-8 overflow-y-auto min-h-0">
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Amount</span><span className="font-bold text-slate-900">{order.amount}</span></div>
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Payment</span><span className="font-bold text-slate-900">{order.paymentMethod || 'bKash/Nagad'}</span></div>
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Sender phone</span><span className="font-mono text-slate-800">{order.senderPhone}</span></div>
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">TrxID</span><span className="font-mono text-slate-800">{order.trxId}</span></div>
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 sm:col-span-2"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Customer</span><span className="font-bold text-slate-900 break-words">{order.userName} • {order.userPhone}</span></div>
            <div className="bg-slate-50 p-3 rounded-xl border border-slate-200 sm:col-span-2"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Date</span><span className="font-bold text-slate-900">{new Date(order.date).toLocaleString()}</span></div>
          </div>

          {isAdmin && onStatusChange && (
            <div className="bg-blue-50 border border-blue-100 rounded-2xl p-5">
              <p className="text-sm font-bold text-slate-900 mb-3">Admin — change status (Pending → In Progress → Completed / Cancelled)</p>
              <div className="flex flex-wrap gap-2">
                <button type="button" disabled={changingStatus} onClick={() => void handleStatusChange('in_progress')} className="px-4 py-2.5 rounded-xl font-bold text-sm bg-blue-600 text-white hover:bg-blue-500 disabled:opacity-50 flex items-center gap-2"><Clock size={15} /> {changingStatus ? 'Working…' : 'Start (In Progress)'}</button>
                <button type="button" disabled={changingStatus} onClick={() => void handleStatusChange('completed')} className="px-4 py-2.5 rounded-xl font-bold text-sm bg-green-600 text-white hover:bg-green-500 disabled:opacity-50 flex items-center gap-2"><CheckCircle size={15} /> {changingStatus ? 'Working…' : 'Complete'}</button>
                <button type="button" disabled={changingStatus} onClick={() => { if (confirm('Cancel this order?')) void handleStatusChange('cancelled'); }} className="px-4 py-2.5 rounded-xl font-bold text-sm bg-red-50 text-red-600 border border-red-200 hover:bg-red-100 disabled:opacity-50 flex items-center gap-2"><XCircle size={15} /> Cancel</button>
              </div>
              {statusError && <p className="text-sm font-bold text-red-600 mt-3">{statusError}</p>}
              <p className="text-xs text-slate-500 mt-3">Status changes are enforced server-side by Firestore rules (admin only for status) — the UI button alone does not grant access.</p>
            </div>
          )}

          <form onSubmit={handleSave} className="space-y-5">
            <h4 className="font-bold text-lg text-slate-900">Detail collection — fill this so the team can set up your service</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Page link / Page name</label><input value={pageLink} onChange={(e) => setPageLink(e.target.value)} placeholder="facebook.com/yourpage" className={fieldCls} /></div>
              <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Business details (Doc / Sheet link)</label><input value={docLink} onChange={(e) => setDocLink(e.target.value)} placeholder="Google Doc / Sheet link" className={fieldCls} /></div>
              <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Email</label><input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="name@email.com" className={fieldCls} /></div>
              <div className="space-y-2"><label className="text-sm font-bold text-slate-700">WhatsApp</label><input value={whatsapp} onChange={(e) => setWhatsapp(e.target.value)} placeholder="017..." className={fieldCls} /></div>
            </div>
            <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Access notes (what access you have given / will give, Page roles, etc.)</label><textarea rows={3} value={accessNotes} onChange={(e) => setAccessNotes(e.target.value)} placeholder="e.g. Page Admin added, or access to be given after confirmation…" className={fieldCls} /></div>
            <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Follow-up info (anything else the team should know)</label><textarea rows={3} value={followUp} onChange={(e) => setFollowUp(e.target.value)} placeholder="Follow-up details, preferred contact time, etc." className={fieldCls} /></div>
            <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Other requirements</label><textarea rows={3} value={reqs} onChange={(e) => setReqs(e.target.value)} placeholder="Your requirements…" className={fieldCls} /></div>
            <button type="submit" disabled={saving} className="w-full bg-blue-600 text-white font-bold py-4 rounded-xl hover:bg-blue-500 disabled:opacity-50 flex items-center justify-center gap-2"><Save size={18} /> {saving ? 'Saving…' : saved ? 'Saved ✓' : 'Save details'}</button>
            {saveError && <p className="text-sm font-bold text-red-600 text-center">{saveError}</p>}
          </form>

          <button onClick={onOpenSupportThread} className="w-full flex items-center justify-center gap-2 bg-white border border-blue-200 text-blue-600 font-bold py-3.5 rounded-xl hover:bg-blue-50">
            <MessageCircle size={18} /> Go to support thread for this order
          </button>
          <p className="text-xs text-slate-400 text-center">Support thread = you ↔ developer team. It is not the Messenger Leads thread for your own customers.</p>
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;
