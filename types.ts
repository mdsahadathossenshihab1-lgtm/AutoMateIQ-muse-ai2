
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
  status: 'pending' | 'approved' | 'rejected' | 'completed';
  date: string;
  adminFbLink?: string;
  clientDocLink?: string;      
  clientRequirements?: string; 
  clientPageLink?: string;     
  clientEmail?: string;        
  clientWhatsapp?: string;     
  isDetailsSubmitted?: boolean; 
  startDate?: string;
  completionDate?: string;
  adminMessage?: string;
}

export interface SupportMessage {
  id: string;
  user_id: string;
  sender_role: 'user' | 'admin';
  message: string;
  is_read: boolean;
  created_at: string;
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
  updateOrderStatus: (orderId: string, status: 'approved' | 'rejected' | 'completed') => Promise<boolean>;
  updateOrderDetails: (orderId: string, updates: Partial<Order>) => Promise<void>;
  sendSupportMessage: (text: string, targetUserId?: string) => Promise<boolean>;
  markMessagesRead: (userId: string) => Promise<void>;
  updateSiteSettings: (settings: SiteSettings) => Promise<boolean>;
  requestNotificationPermission: () => Promise<boolean>;
  syncRealtimeOrder: (order: Order, eventType: 'INSERT' | 'UPDATE') => void;
  isLoading: boolean;
}
