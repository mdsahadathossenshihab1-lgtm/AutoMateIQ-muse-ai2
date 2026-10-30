import React, { useEffect, useMemo, useState } from 'react';
import { useAuth } from '../AuthContext';
import { Order, User, WalletRecharge } from '../types';
import {
  loadAllLocalWallet, mergeWalletLists, updateWalletRechargeStatus,
} from '../services/wallet';
import { db } from '../services/firebaseClient';
import OrderDetail, { ORDER_STATUS_LABEL, ORDER_STATUS_STYLE } from './OrderDetail';
import { Users, MessageCircle, Package, Coins, Search, ArrowLeft, CheckCircle, XCircle, Clock } from 'lucide-react';

/**
 * Admin Panel — role-gated in the UI by user.role === 'admin'.
 *
 * Admin role comes from the repo's existing detection:
 * AuthContext / firebaseClient isEmailAdmin (admin email list +
 * users/{uid}.role). UI gating alone is NOT security: Firestore
 * rules (firestore.rules) must also enforce it — admins list,
 * orders status changes, messenger_leads and wallet_recharges
 * are admin-gated there. A non-admin who reaches this component
 * sees only "Access denied" and no other customer's data.
 *
 * What it covers:
 * - Customers list (business customers + their orders)
 * - Admin conversation list (support threads per customer, with
 *   that customer's latest order shown as context)
 * - Pending orders queue (Pending -> In Progress -> Completed /
 *   Cancelled, via OrderDetail status actions)
 * - Wallet recharge queue (approve / reject pending recharges)
 *
 * It does NOT replace the existing Dashboard Users/Support tabs;
 * it is the single admin overview that links customers, orders,
 * conversations, and wallet verification together.
 */
const AdminPanel: React.FC<{
  onOpenSupportThread: (customer: User) => void;
}> = ({ onOpenSupportThread }) => {
  const { user, users, orders, messages, updateOrderStatus, updateOrderDetails, sendSupportMessage } = useAuth();
  const [tab, setTab] = useState<'customers' | 'conversations' | 'pending' | 'wallet'>('pending');
  const [search, setSearch] = useState('');
  const [selectedCustomer, setSelectedCustomer] = useState<User | null>(null);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [threadCustomer, setThreadCustomer] = useState<User | null>(null);
  const [reply, setReply] = useState('');
  const [walletList, setWalletList] = useState<WalletRecharge[]>(() => loadAllLocalWallet());

  useEffect(() => {
    // Firestore wallet queue (admin) merged with local fallback.
    db.getWalletRecharges().then((rows) => {
      if (rows.length > 0) setWalletList((prev) => mergeWalletLists(prev, rows));
    }).catch(() => { /* local fallback already shown */ });
  }, []);

  if (!user || user.role !== 'admin') {
    return (
      <div className="p-12 text-center bg-white rounded-3xl border border-slate-200">
        <p className="font-bold text-slate-900 text-lg">Access denied</p>
        <p className="text-slate-500 mt-2">This panel is only for admins. Clients cannot see other customers here.</p>
      </div>
    );
  }

  const customers = users.filter((u) => u.role !== 'admin');
  const filteredCustomers = customers.filter((u) => {
    const term = search.toLowerCase();
    return !term || (u.name || '').toLowerCase().includes(term) || (u.email || '').toLowerCase().includes(term) || (u.phone || '').includes(term);
  });
  const pendingOrders = orders.filter((o) => o.status === 'pending');
  const pendingWallet = walletList.filter((r) => r.status === 'pending');
  const chatUserIds = useMemo(() => Array.from(new Set(messages.map((m) => m.user_id))), [messages]);
  const chatCustomers = customers.filter((u) => chatUserIds.includes(u.id));
  const unreadFor = (id: string) => messages.filter((m) => m.user_id === id && m.sender_role === 'user' && !m.is_read).length;
  const latestOrderFor = (id: string) => orders.filter((o) => o.userId === id).sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())[0];

  const handleWallet = async (r: WalletRecharge, status: 'approved' | 'rejected') => {
    await updateWalletRechargeStatus(r, status);
    setWalletList((prev) => prev.map((x) => (x.id === r.id ? { ...x, status } : x)));
  };

  const threadMessages = threadCustomer ? messages.filter((m) => m.user_id === threadCustomer.id) : [];
  const threadOrder = threadCustomer ? latestOrderFor(threadCustomer.id) : undefined;

  const tabBtn = (id: typeof tab, label: string, badge?: number) => (
    <button key={id} onClick={() => setTab(id)} className={`px-4 py-2.5 rounded-xl text-sm font-bold border transition-colors ${tab === id ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-slate-600 border-slate-200 hover:border-blue-300'}`}>
      {label}{badge ? ` (${badge})` : ''}
    </button>
  );

  return (
    <div className="space-y-6 animate-fade-in-up">
      <div className="bg-amber-50 border border-amber-200 rounded-2xl p-4 text-sm text-amber-800 leading-relaxed">
        <b>Admin Panel — admin only.</b> UI gating here is not the security boundary:
        Firestore rules must also allow admin-only reads/writes for customers,
        order status changes, and wallet approvals. A client account cannot
        open this panel or see another customer's orders, threads, or wallet.
      </div>

      <div className="flex flex-wrap gap-2">
        {tabBtn('pending', 'Pending orders queue', pendingOrders.length)}
        {tabBtn('customers', 'Customers')}
        {tabBtn('conversations', 'Admin conversations')}
        {tabBtn('wallet', 'Wallet verification', pendingWallet.length)}
      </div>

      <div className="relative">
        <Search className="absolute left-4 top-3.5 text-slate-400" size={18} />
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search customers by name / email / phone…" className="w-full pl-12 pr-4 py-3 bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 text-slate-900 font-medium placeholder-slate-400" />
      </div>

      {tab === 'pending' && (
        <div className="grid grid-cols-1 gap-4">
          {pendingOrders.length === 0 && <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200"><Package size={48} className="mx-auto mb-4 opacity-30" /><p>No pending orders.</p></div>}
          {pendingOrders.map((o) => (
            <div key={o.id} className="bg-white p-6 rounded-2xl border border-amber-200 border-l-8 shadow-sm flex flex-col md:flex-row md:items-center gap-4">
              <div className="flex-1">
                <p className="font-bold text-slate-900">{o.serviceName} — {o.packageDetails}</p>
                <p className="text-sm text-slate-500 mt-1">{o.userName} • {o.amount} • {o.paymentMethod} • Sender {o.senderPhone} • TrxID {o.trxId}</p>
                <p className="text-xs text-slate-400 mt-1">{new Date(o.date).toLocaleString()}</p>
              </div>
              <span className={`px-3 py-1 rounded-full text-xs font-bold border ${ORDER_STATUS_STYLE[o.status]}`}>{ORDER_STATUS_LABEL[o.status]}</span>
              <button onClick={() => setSelectedOrder(o)} className="bg-blue-600 text-white font-bold px-5 py-3 rounded-xl hover:bg-blue-500">Open detail</button>
            </div>
          ))}
        </div>
      )}

      {tab === 'customers' && (
        <div className="grid grid-cols-1 gap-4">
          {filteredCustomers.length === 0 && <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200"><Users size={48} className="mx-auto mb-4 opacity-30" /><p>No customers found.</p></div>}
          {filteredCustomers.map((u) => {
            const uOrders = orders.filter((o) => o.userId === u.id);
            const uPending = uOrders.filter((o) => o.status === 'pending').length;
            return (
              <div key={u.id} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <p className="font-bold text-slate-900 text-lg">{u.name}</p>
                    <p className="text-sm text-slate-500">{u.email} • {u.phone} {u.location ? `• ${u.location}` : ''}</p>
                    <p className="text-sm text-slate-600 mt-1">{uOrders.length} order(s){uPending > 0 ? `, ${uPending} pending` : ''} • Services: {uOrders.map((o) => o.serviceName).join(', ') || '—'}</p>
                  </div>
                  <div className="flex gap-2">
                    <button onClick={() => setSelectedCustomer(selectedCustomer?.id === u.id ? null : u)} className="bg-white border border-slate-200 text-slate-700 font-bold px-4 py-2.5 rounded-xl hover:bg-slate-50">Orders</button>
                    <button onClick={() => { setThreadCustomer(u); }} className="bg-blue-600 text-white font-bold px-4 py-2.5 rounded-xl hover:bg-blue-500">Open thread</button>
                  </div>
                </div>
                {selectedCustomer?.id === u.id && (
                  <div className="mt-5 space-y-3 border-t border-slate-100 pt-5">
                    {uOrders.length === 0 && <p className="text-slate-400 text-sm">No orders for this customer yet.</p>}
                    {uOrders.map((o) => (
                      <button key={o.id} onClick={() => setSelectedOrder(o)} className="w-full text-left bg-slate-50 p-4 rounded-xl border border-slate-200 hover:border-blue-300 flex justify-between items-center gap-3">
                        <span className="font-bold text-slate-900 text-sm">{o.serviceName} — {o.packageDetails} • {o.amount}</span>
                        <span className={`px-2 py-0.5 rounded-full text-[11px] font-bold border ${ORDER_STATUS_STYLE[o.status]}`}>{ORDER_STATUS_LABEL[o.status]}</span>
                      </button>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}

      {tab === 'conversations' && !threadCustomer && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {chatCustomers.length === 0 && <div className="col-span-full p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200"><MessageCircle size={48} className="mx-auto mb-4 opacity-30" /><p>No support conversations yet.</p></div>}
          {chatCustomers.map((u) => {
            const lo = latestOrderFor(u.id);
            return (
              <button key={u.id} onClick={() => setThreadCustomer(u)} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-md hover:border-blue-300 text-left">
                <div className="flex justify-between items-start">
                  <p className="font-bold text-slate-900 text-lg">{u.name}</p>
                  {unreadFor(u.id) > 0 && <span className="bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full">{unreadFor(u.id)} New</span>}
                </div>
                <p className="text-sm text-slate-500">{u.email}</p>
                {lo && <p className="text-sm text-slate-600 mt-2 bg-slate-50 border border-slate-200 rounded-lg p-3">Latest order: {lo.serviceName} • {lo.amount} • <span className="font-bold">{ORDER_STATUS_LABEL[lo.status]}</span> — <span className="text-blue-600 font-bold">View order</span></p>}
              </button>
            );
          })}
        </div>
      )}

      {threadCustomer && (
        <div className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
          <div className="p-6 border-b border-slate-200 flex flex-wrap justify-between gap-3">
            <div>
              <button onClick={() => setThreadCustomer(null)} className="mb-2 flex items-center gap-2 text-slate-500 hover:text-slate-900 font-bold text-sm"><ArrowLeft size={16} /> Back to conversations</button>
              <h3 className="font-bold text-xl text-slate-900">{threadCustomer.name}</h3>
              <p className="text-sm text-slate-500">{threadCustomer.email} • {threadCustomer.phone}</p>
              {threadOrder && <button onClick={() => setSelectedOrder(threadOrder)} className="mt-2 text-blue-600 font-bold text-sm hover:underline">Linked order: {threadOrder.serviceName} • {threadOrder.amount} • {ORDER_STATUS_LABEL[threadOrder.status]} → Open detail</button>}
            </div>
            <button onClick={() => onOpenSupportThread(threadCustomer)} className="bg-white border border-blue-200 text-blue-600 font-bold px-4 py-2.5 rounded-xl hover:bg-blue-50 h-fit">Open in full Inbox</button>
          </div>
          <div className="p-6 space-y-4 max-h-[480px] overflow-y-auto bg-slate-50">
            {threadMessages.length === 0 && <p className="text-slate-400 text-center py-8">No messages in this thread yet.</p>}
            {threadMessages.map((m) => (
              <div key={m.id} className={`flex ${m.sender_role === 'admin' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[75%] px-5 py-3.5 rounded-2xl text-sm shadow-sm border ${m.sender_role === 'admin' ? 'bg-blue-600 text-white border-blue-600' : 'bg-white border-slate-200 text-slate-800'}`}>
                  <p className="whitespace-pre-wrap">{m.message}</p>
                  <p className="text-[10px] opacity-70 mt-1">{new Date(m.created_at).toLocaleString()}</p>
                </div>
              </div>
            ))}
          </div>
          <form onSubmit={(e) => { e.preventDefault(); if (!reply.trim()) return; void sendSupportMessage(reply.trim(), threadCustomer.id); setReply(''); }} className="p-5 bg-white border-t border-slate-200 flex gap-3">
            <input value={reply} onChange={(e) => setReply(e.target.value)} placeholder="Reply to this customer in the support inbox…" className="flex-1 px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl outline-none text-slate-900 font-medium placeholder-slate-400" />
            <button type="submit" disabled={!reply.trim()} className="bg-blue-600 text-white font-bold px-6 rounded-xl hover:bg-blue-500 disabled:opacity-50">Send</button>
          </form>
        </div>
      )}

      {tab === 'wallet' && (
        <div className="grid grid-cols-1 gap-4">
          {pendingWallet.length === 0 && <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200"><Coins size={48} className="mx-auto mb-4 opacity-30" /><p>No pending wallet recharges.</p></div>}
          {walletList.map((r) => (
            <div key={r.id} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <p className="font-bold text-slate-900">৳{r.amount} • {r.paymentMethod === 'bkash' ? 'bKash' : 'Nagad'} • {r.userName || r.userId}</p>
                <p className="text-sm text-slate-500 font-mono mt-1">Sender {r.senderPhone} • TrxID {r.trxId} • {new Date(r.date).toLocaleString()}</p>
              </div>
              <div className="flex items-center gap-2">
                <span className={`px-2 py-1 rounded-full text-xs font-bold border ${r.status === 'pending' ? 'bg-amber-50 text-amber-600 border-amber-100' : r.status === 'approved' ? 'bg-green-50 text-green-600 border-green-100' : 'bg-red-50 text-red-600 border-red-100'}`}>{r.status === 'pending' ? 'Pending' : r.status === 'approved' ? 'Approved' : 'Rejected'}</span>
                {r.status === 'pending' && (
                  <>
                    <button onClick={() => void handleWallet(r, 'approved')} className="bg-green-600 text-white font-bold px-4 py-2.5 rounded-xl hover:bg-green-500 flex items-center gap-1"><CheckCircle size={15} /> Approve</button>
                    <button onClick={() => void handleWallet(r, 'rejected')} className="bg-red-50 text-red-600 border border-red-200 font-bold px-4 py-2.5 rounded-xl hover:bg-red-100 flex items-center gap-1"><XCircle size={15} /> Reject</button>
                  </>
                )}
              </div>
            </div>
          ))}
          <p className="text-xs text-slate-400">Approving adds the amount to the customer's wallet balance (approved total). Rejecting does not. No payment is auto-verified.</p>
        </div>
      )}

      {selectedOrder && (
        <OrderDetail
          order={selectedOrder}
          isAdmin
          onClose={() => setSelectedOrder(null)}
          onSaveDetails={async (updates) => { await updateOrderDetails(selectedOrder.id, updates); setSelectedOrder({ ...selectedOrder, ...updates }); }}
          onStatusChange={async (s) => { await updateOrderStatus(selectedOrder.id, s); setSelectedOrder({ ...selectedOrder, status: s }); }}
          onOpenSupportThread={() => { const cust = customers.find((u) => u.id === selectedOrder.userId); if (cust) onOpenSupportThread(cust); setSelectedOrder(null); }}
        />
      )}
    </div>
  );
};

export default AdminPanel;
