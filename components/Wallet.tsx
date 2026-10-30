import React, { useEffect, useState } from 'react';
import { useAuth } from '../AuthContext';
import { WalletRecharge } from '../types';
import {
  loadLocalWallet, mergeWalletLists, walletBalance, addWalletRecharge, WALLET_AMOUNTS,
} from '../services/wallet';
import { db } from '../services/firebaseClient';
import { Coins, Clock, CheckCircle, XCircle } from 'lucide-react';

/**
 * Logged-in Wallet (client only).
 * Balance = sum of APPROVED recharges only. A new recharge is
 * 'pending' until an admin approves it in the Admin Panel — payment
 * is NOT auto-verified, and no balance is added before approval.
 * Usage deduction is not implemented yet (no usage ledger), so this
 * balance is approved-recharge total, not net balance after usage.
 * Storage is Firestore-first with a per-account localStorage
 * fallback (see services/wallet.ts) — the fallback is single-device
 * demo storage, not a shared backend.
 */
const Wallet: React.FC = () => {
  const { user } = useAuth();
  const userId = user?.id || '';
  const [list, setList] = useState<WalletRecharge[]>(() => loadLocalWallet(userId));
  const [amount, setAmount] = useState<number>(500);
  const [method, setMethod] = useState<'bkash' | 'nagad'>('bkash');
  const [senderPhone, setSenderPhone] = useState('');
  const [trxId, setTrxId] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!userId) return;
    setList(loadLocalWallet(userId));
    // Try Firestore (admin/client rules permitting) and merge.
    db.getWalletRecharges(userId).then((rows) => {
      if (rows.length > 0) {
        const merged = mergeWalletLists(loadLocalWallet(userId), rows);
        setList(merged);
      }
    }).catch(() => { /* local fallback already shown */ });
  }, [userId]);

  if (!user) return <div className="p-8 text-slate-500">লগইন করুন।</div>;

  const balance = walletBalance(list);
  const pendingCount = list.filter((r) => r.status === 'pending').length;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!senderPhone.trim() || !trxId.trim() || submitting) return;
    setSubmitting(true);
    const recharge: WalletRecharge = {
      id: Date.now().toString(),
      userId,
      userName: user.name,
      amount,
      paymentMethod: method,
      senderPhone: senderPhone.trim(),
      trxId: trxId.trim().toUpperCase(),
      status: 'pending',
      date: new Date().toISOString(),
    };
    await addWalletRecharge(recharge);
    setList((prev) => [recharge, ...prev]);
    setSenderPhone('');
    setTrxId('');
    setSubmitting(false);
  };

  const badge = (s: WalletRecharge['status']) => s === 'pending'
    ? <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-amber-50 text-amber-600 border border-amber-100 flex items-center gap-1"><Clock size={12} /> Pending verification</span>
    : s === 'approved'
      ? <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-green-50 text-green-600 border border-green-100 flex items-center gap-1"><CheckCircle size={12} /> Approved</span>
      : <span className="px-2 py-0.5 rounded-full text-[11px] font-bold bg-red-50 text-red-600 border border-red-100 flex items-center gap-1"><XCircle size={12} /> Rejected</span>;

  return (
    <div className="space-y-8 animate-fade-in-up">
      <div className="bg-white rounded-3xl border border-slate-200 p-8 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 bg-blue-50 rounded-2xl flex items-center justify-center text-blue-600 border border-blue-100"><Coins size={28} /></div>
          <div>
            <p className="text-xs font-bold text-slate-400 uppercase tracking-wide">Wallet balance (approved recharges only)</p>
            <p className="text-3xl font-bold text-slate-900 font-mono">৳{balance.toLocaleString()}</p>
            {pendingCount > 0 && <p className="text-sm text-amber-600 font-bold mt-1">{pendingCount} recharge(s) pending verification — not added to balance yet.</p>}
          </div>
        </div>
        <p className="text-sm text-slate-500 max-w-sm leading-relaxed">
          Recharge via bKash/Nagad (Personal), then submit sender phone + TrxID.
          An admin verifies it before the balance increases. Usage deduction
          is not yet tracked, so this is approved-recharge total.
        </p>
      </div>

      <form onSubmit={handleSubmit} className="bg-white rounded-3xl border border-slate-200 p-8 shadow-sm space-y-6">
        <h3 className="font-bold text-xl text-slate-900">Recharge wallet</h3>
        <div>
          <p className="text-sm font-bold text-slate-700 mb-3">Amount</p>
          <div className="flex flex-wrap gap-2">
            {WALLET_AMOUNTS.map((a) => (
              <button key={a} type="button" onClick={() => setAmount(a)} className={`px-6 py-3 rounded-xl font-bold border transition-colors ${amount === a ? 'bg-blue-600 text-white border-blue-600' : 'bg-white text-slate-700 border-slate-200 hover:border-blue-300'}`}>৳{a}</button>
            ))}
          </div>
        </div>
        <div className="flex gap-2">
          <button type="button" onClick={() => setMethod('bkash')} className={`flex-1 py-3 rounded-xl font-bold border ${method === 'bkash' ? 'bg-pink-50 text-pink-700 border-pink-300' : 'bg-white text-slate-600 border-slate-200'}`}>bKash (Personal)</button>
          <button type="button" onClick={() => setMethod('nagad')} className={`flex-1 py-3 rounded-xl font-bold border ${method === 'nagad' ? 'bg-orange-50 text-orange-700 border-orange-300' : 'bg-white text-slate-600 border-slate-200'}`}>Nagad (Personal)</button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Sender phone</label><input required value={senderPhone} onChange={(e) => setSenderPhone(e.target.value)} placeholder="017..." className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400" /></div>
          <div className="space-y-2"><label className="text-sm font-bold text-slate-700">Transaction ID (TrxID)</label><input required value={trxId} onChange={(e) => setTrxId(e.target.value.toUpperCase())} placeholder="e.g. 8N7A6..." className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400 font-mono uppercase" /></div>
        </div>
        <button type="submit" disabled={submitting || !senderPhone.trim() || !trxId.trim()} className="w-full bg-blue-600 text-white font-bold py-4 rounded-xl hover:bg-blue-500 disabled:opacity-50">
          {submitting ? 'Submitting…' : 'Submit recharge for verification'}
        </button>
        <p className="text-xs text-slate-400 text-center">This only submits a verification request. No payment is verified automatically.</p>
      </form>

      <div>
        <h3 className="font-bold text-xl text-slate-900 mb-4">Recharge history</h3>
        <div className="grid grid-cols-1 gap-4">
          {list.length === 0 && <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200"><Coins size={48} className="mx-auto mb-4 opacity-30" /><p>No recharges yet.</p></div>}
          {list.map((r) => (
            <div key={r.id} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div>
                <p className="font-bold text-slate-900">৳{r.amount} • {r.paymentMethod === 'bkash' ? 'bKash' : 'Nagad'} (Personal)</p>
                <p className="text-sm text-slate-500 mt-1 font-mono">Sender {r.senderPhone} • TrxID {r.trxId} • {new Date(r.date).toLocaleString()}</p>
                {r.adminNote && <p className="text-sm text-slate-600 mt-1">Admin note: {r.adminNote}</p>}
              </div>
              {badge(r.status)}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Wallet;
