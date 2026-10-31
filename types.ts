
import { ReactNode } from 'react';

export type Language = 'bn' | 'en';

export interface Service {
  id: string;
  title: string;
  description: string;
  icon: ReactNode;
}

export interface PricingFeature {
  text: string;
  included: boolean;
}

export interface PricingVariant {
  name: string;
  price: string;
}

export interface PricingPackage {
  id: string;
  serviceName: string;
  description: string;
  monthlyPrice: string;
  oneTimePrice: string;
  features: string[];
  recommended?: boolean;
  monthlyVariants?: PricingVariant[];
  hideMonthlyOption?: boolean;
}

export interface ChatMessage {
  role: 'user' | 'model';
  text: string;
  isError?: boolean;
}

export interface ServiceDetailStep {
  title: string;
  desc: string;
}

export interface ServiceDetailContent {
  id: string;
  title: string;
  subtitle: string;
  howItWorks: ServiceDetailStep[];
  benefits: string[];
  techSpecs?: string[];
}

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: 'admin' | 'user';
  location?: string;
}

export type OrderStatus =
  | 'pending'
  | 'in_progress'
  | 'approved'
  | 'rejected'
  | 'completed'
  | 'cancelled';

/** New flow: Pending -> In Progress -> Completed / Cancelled.
 *  'approved' / 'rejected' are legacy statuses from the original app
 *  and are kept for existing orders only; new admin actions should
 *  use in_progress / completed / cancelled. */
export interface Order {
  id: string;
  userId: string;
  userName: string;
  userPhone: string;
  serviceName: string;
  packageDetails: string;
  amount: string;
  paymentMethod: string;
  senderPhone: string;
  trxId: string;
  status: OrderStatus;
  date: string;
  adminFbLink?: string;
  clientDocLink?: string;
  clientRequirements?: string;
  clientPageLink?: string;
  clientEmail?: string;
  clientWhatsapp?: string;
  /** Detail-collection fields for the order detail view. */
  accessNotes?: string;
  followUpInfo?: string;
  isDetailsSubmitted?: boolean;
  startDate?: string;
  completionDate?: string;
  adminMessage?: string;
}

/**
 * Wallet recharge request. A recharge is NEVER auto-verified:
 * it stays 'pending' until an admin approves or rejects it in the
 * Admin Panel. Balance = sum of approved recharges only (usage
 * deduction is not yet implemented — see Wallet.tsx).
 */
export interface WalletRecharge {
  id: string;
  userId: string;
  userName?: string;
  amount: number;
  paymentMethod: 'bkash' | 'nagad';
  senderPhone: string;
  trxId: string;
  status: 'pending' | 'approved' | 'rejected';
  date: string;
  adminNote?: string;
}

export interface SupportMessage {
  id: string;
  user_id: string;
  sender_role: 'user' | 'admin';
  message: string;
  is_read: boolean;
  created_at: string;
}

/* ------------------------------------------------------------------
 * Messenger Leads — NEW, separate from the support inbox.
 * The support inbox (SupportMessage) is business customer <-> the
 * AutoMateIQ developer team. Messenger leads are a business
 * customer's OWN Messenger customers (people who messaged that
 * business's Page). The two must never be mixed: a lead belongs to
 * exactly one business account, identified by ownerId (the logged-in
 * business customer's user id) and, when known, sourcePageId.
 * ------------------------------------------------------------------ */
export type MessengerLeadStatus =
  | 'new'
  | 'bot_replied'
  | 'handover'
  | 'owner_replied'
  | 'closed';

export type MessengerSender = 'customer' | 'bot' | 'owner';

export interface MessengerMessage {
  id: string;
  sender: MessengerSender;
  text: string;
  created_at: string;
  /** Present only when a website reply could not be delivered to
   *  Messenger because no reply webhook is configured. */
  delivery?: 'delivered' | 'pending_send';
}

export interface MessengerLead {
  id: string;
  /** Logged-in business customer who owns this lead. NEVER show a
   *  lead to a different ownerId. */
  ownerId: string;
  /** Facebook Page this lead messaged (display name, e.g. the
   *  business's Page name). */
  sourcePage: string;
  sourcePageId?: string;
  name: string;
  /** Facebook profile picture URL, when the bot has fetched it
   *  (the webhook alone only gives a PSID, not a name/picture). */
  profilePic?: string;
  /** Messenger PSID. Masked in the UI; the full value is only used
   *  internally when posting a reply to the reply webhook. */
  psid: string;
  status: MessengerLeadStatus;
  lastMessage: string;
  updated_at: string;
  messages: MessengerMessage[];
  note?: string;
}

export interface SiteSettings {
  facebook: string;
  youtube: string;
}

export interface AuthContextType {
  user: User | null;
  users: User[];
  orders: Order[];
  messages: SupportMessage[]; 
  siteSettings: SiteSettings;
  signupUser: (data: { name: string; email: string; phone: string; pass: string }) => Promise<{ success: boolean; message?: string }>;
  verifyEmail: (email: string, token: string) => Promise<{ success: boolean; message?: string }>;
  resendSignupCode: (email: string) => Promise<{ success: boolean; message?: string }>;
  loginUser: (email: string, pass: string) => Promise<{ success: boolean; message?: string }>;
  loginWithGoogle?: () => Promise<{ success: boolean; message?: string }>;
  logout: () => void;
  addOrder: (order: Omit<Order, 'id' | 'date' | 'status'>) => Promise<void>;
  updateOrderStatus: (orderId: string, status: OrderStatus) => Promise<boolean>;
  updateOrderDetails: (orderId: string, updates: Partial<Order>) => Promise<void>;
  sendSupportMessage: (text: string, targetUserId?: string) => Promise<boolean>;
  markMessagesRead: (userId: string) => Promise<void>;
  updateSiteSettings: (settings: SiteSettings) => Promise<boolean>;
  requestNotificationPermission: () => Promise<boolean>;
  syncRealtimeOrder: (order: Order, eventType: 'INSERT' | 'UPDATE') => void;
  isLoading: boolean;
}
