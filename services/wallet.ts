import { WalletRecharge } from '../types';
import { db } from './firebaseClient';

/**
 * Wallet — balance and recharge history.
 *
 * IMPORTANT — demo vs live, read before trusting this:
 * - A recharge is NEVER auto-verified. It is created as 'pending'
 *   and only an admin approve in the Admin Panel moves it to
 *   'approved'. Balance = sum of APPROVED recharges only.
 * - Usage deduction (API calls / bot usage eating balance) is NOT
 *   implemented yet: there is no usage ledger in this codebase, so
 *   the balance shown is approved-recharge total, not net balance
 *   after usage. Do not present it as a post-usage balance.
 * - Storage: Firestore-first (wallet_recharges collection, see
 *   firebaseClient.ts). If Firestore is unavailable/denied, this
 *   falls back to per-account localStorage — that fallback is a
 *   single-device demo store, NOT a shared backend, and an admin
 *   on another device will not see it. TODO: remove the local
 *   fallback once Firestore rules for wallet_recharges are
 *   deployed and verified for both roles.
 * - No token, key, or payment credential is stored here. Only the
 *   sender phone + TrxID the customer typed in the form.
 */

const keyFor = (userId: string) => `automateiq_wallet_${userId}`;
const ALL_PREFIX = 'automateiq_wallet_';

export const WALLET_AMOUNTS = [200, 500, 1000, 2500] as const;

export function loadLocalWallet(userId: string): WalletRecharge[] {
  if (!userId) return [];
  try {
    const raw = localStorage.getItem(keyFor(userId));
    if (!raw) return [];
    return (JSON.parse(raw) as WalletRecharge[]).filter((r) => r.userId === userId);
  } catch {
    return [];
  }
}

export function saveLocalWallet(userId: string, list: WalletRecharge[]): void {
  try {
    localStorage.setItem(keyFor(userId), JSON.stringify(list.filter((r) => r.userId === userId)));
  } catch { /* private mode */ }
}

export function loadAllLocalWallet(): WalletRecharge[] {
  const out: WalletRecharge[] = [];
  try {
    for (let i = 0; i < localStorage.length; i++) {
      const k = localStorage.key(i);
      if (k && k.startsWith(ALL_PREFIX)) {
        try { out.push(...(JSON.parse(localStorage.getItem(k) || '[]') as WalletRecharge[])); } catch { /* skip */ }
      }
    }
  } catch { /* ignore */ }
  return out.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
}

export function walletBalance(list: WalletRecharge[]): number {
  return list.filter((r) => r.status === 'approved').reduce((sum, r) => sum + (r.amount || 0), 0);
}

/** Merge two lists by id, preferring the newer date. */
export function mergeWalletLists(a: WalletRecharge[], b: WalletRecharge[]): WalletRecharge[] {
  const map = new Map<string, WalletRecharge>();
  [...a, ...b].forEach((r) => {
    const prev = map.get(r.id);
    if (!prev || new Date(r.date).getTime() >= new Date(prev.date).getTime()) map.set(r.id, r);
  });
  return Array.from(map.values()).sort((x, y) => new Date(y.date).getTime() - new Date(x.date).getTime());
}

export async function addWalletRecharge(recharge: WalletRecharge): Promise<void> {
  const local = loadLocalWallet(recharge.userId);
  saveLocalWallet(recharge.userId, [recharge, ...local]);
  try { await db.addWalletRecharge(recharge); } catch { /* local fallback already saved */ }
}

export async function updateWalletRechargeStatus(
  recharge: WalletRecharge,
  status: 'approved' | 'rejected',
  adminNote?: string,
): Promise<void> {
  const updated: WalletRecharge = { ...recharge, status, adminNote };
  const local = loadLocalWallet(recharge.userId).map((r) => (r.id === recharge.id ? updated : r));
  saveLocalWallet(recharge.userId, local);
  try { await db.updateWalletRecharge(recharge.id, { status, adminNote }); } catch { /* local fallback already saved */ }
}
