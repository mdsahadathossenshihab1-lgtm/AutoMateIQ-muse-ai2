import { initializeApp, getApps, getApp } from 'firebase/app';
import { 
  getAuth, 
  signInWithPopup, 
  GoogleAuthProvider, 
  createUserWithEmailAndPassword, 
  signInWithEmailAndPassword, 
  signOut,
  updateProfile
} from 'firebase/auth';
import { 
  getFirestore, 
  doc, 
  getDoc, 
  setDoc, 
  updateDoc, 
  collection, 
  query, 
  where, 
  orderBy, 
  getDocs,
  getDocFromServer
} from 'firebase/firestore';
import firebaseConfig from '../firebase-applet-config.json';
import { User, Order, SupportMessage, SiteSettings } from '../types';

// Initialize Firebase
const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
export const dbFirestore = getFirestore(app, firebaseConfig.firestoreDatabaseId);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();

export enum OperationType {
  CREATE = 'create',
  UPDATE = 'update',
  DELETE = 'delete',
  LIST = 'list',
  GET = 'get',
  WRITE = 'write',
}

export interface FirestoreErrorInfo {
  error: string;
  operationType: OperationType;
  path: string | null;
  authInfo: {
    userId?: string | null;
    email?: string | null;
    emailVerified?: boolean | null;
    isAnonymous?: boolean | null;
    tenantId?: string | null;
    providerInfo?: {
      providerId?: string | null;
      email?: string | null;
    }[];
  };
}

export function handleFirestoreError(error: unknown, operationType: OperationType, path: string | null): never {
  const errInfo: FirestoreErrorInfo = {
    error: error instanceof Error ? error.message : String(error),
    authInfo: {
      userId: auth.currentUser?.uid,
      email: auth.currentUser?.email,
      emailVerified: auth.currentUser?.emailVerified,
      isAnonymous: auth.currentUser?.isAnonymous,
      tenantId: auth.currentUser?.tenantId,
      providerInfo: auth.currentUser?.providerData?.map(provider => ({
        providerId: provider.providerId,
        email: provider.email,
      })) || []
    },
    operationType,
    path
  };
  console.error('Firestore Error: ', JSON.stringify(errInfo));
  throw new Error(JSON.stringify(errInfo));
}

// Validate Connection to Firestore on boot
async function testConnection() {
  try {
    await getDocFromServer(doc(dbFirestore, 'test', 'connection'));
  } catch (error) {
    if (error instanceof Error && error.message.includes('the client is offline')) {
      console.warn("Please check your Firebase configuration or network status.");
    }
  }
}
testConnection();

const ADMIN_EMAILS = ['shihabno.18@gmail.com', 'info@automateiq.xyz', 'admin@gmail.com'];

export const isEmailAdmin = (email?: string | null): boolean => {
  if (!email) return false;
  return ADMIN_EMAILS.includes(email.trim().toLowerCase());
};

export const db = {
  // 1. Google 1-Click Login / Signup
  loginWithGoogle: async (): Promise<{ success: boolean; data?: any; message?: string }> => {
    try {
      const result = await signInWithPopup(auth, googleProvider);
      const fbUser = result.user;
      const role: 'admin' | 'user' = isEmailAdmin(fbUser.email) ? 'admin' : 'user';
      
      const userDoc: User = {
        id: fbUser.uid,
        name: fbUser.displayName || 'User',
        email: fbUser.email || '',
        phone: fbUser.phoneNumber || '',
        role: role
      };

      await setDoc(doc(dbFirestore, 'users', fbUser.uid), userDoc, { merge: true });
      return { success: true, data: userDoc };
    } catch (error: any) {
      console.error('Google Sign-In Error:', error);
      return { success: false, message: error.message || 'Google sign-in failed' };
    }
  },

  // 2. Email & Password Sign Up
  signup: async (data: { email: string; pass: string; name: string; phone: string; location?: string }) => {
    try {
      const userCredential = await createUserWithEmailAndPassword(auth, data.email, data.pass);
      const fbUser = userCredential.user;
      
      await updateProfile(fbUser, { displayName: data.name });

      const role: 'admin' | 'user' = isEmailAdmin(data.email) ? 'admin' : 'user';
      const userProfile: User = {
        id: fbUser.uid,
        name: data.name,
        email: data.email,
        phone: data.phone,
        role: role,
        location: data.location || 'Unknown'
      };

      await setDoc(doc(dbFirestore, 'users', fbUser.uid), userProfile);
      return { success: true, data: { user: fbUser, profile: userProfile } };
    } catch (error: any) {
      console.error('Signup Error:', error);
      let msg = error.message;
      if (error.code === 'auth/email-already-in-use') {
        msg = 'This email is already registered. Please login.';
      } else if (error.code === 'auth/weak-password') {
        msg = 'Password should be at least 6 characters.';
      } else if (error.code === 'auth/operation-not-allowed') {
        msg = 'Email/password sign-in is not enabled. Please use Google Login or enable it in Firebase Console.';
      }
      return { success: false, message: msg };
    }
  },

  // 3. Email & Password Login
  login: async (email: string, pass: string) => {
    const trimmedEmail = email.trim().toLowerCase();
    const isAdminAccount = (trimmedEmail === 'admin@gmail.com' && pass === 'Admin12');

    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, pass);
      const fbUser = userCredential.user;
      const role: 'admin' | 'user' = isEmailAdmin(fbUser.email) ? 'admin' : 'user';
      
      const userProfile: User = {
        id: fbUser.uid,
        name: isAdminAccount ? 'Admin' : (fbUser.displayName || 'User'),
        email: fbUser.email || email,
        phone: fbUser.phoneNumber || '',
        role: role
      };

      await setDoc(doc(dbFirestore, 'users', fbUser.uid), userProfile, { merge: true });
      return { success: true, data: userProfile };
    } catch (error: any) {
      console.warn('Login primary attempt error:', error.code, error.message);

      // If it's the requested admin account, auto-register or provide instant admin login
      if (isAdminAccount) {
        try {
          const createRes = await createUserWithEmailAndPassword(auth, email, pass);
          const fbUser = createRes.user;
          await updateProfile(fbUser, { displayName: 'System Administrator' });
          const adminProfile: User = {
            id: fbUser.uid,
            name: 'System Administrator',
            email: 'admin@gmail.com',
            phone: '01800000000',
            role: 'admin'
          };
          await setDoc(doc(dbFirestore, 'users', fbUser.uid), adminProfile);
          return { success: true, data: adminProfile };
        } catch (createErr: any) {
          console.warn('Auto admin creation in Firebase Auth failed, using guaranteed admin session:', createErr.code);
          const fallbackAdmin: User = {
            id: 'admin-main-id',
            name: 'System Administrator',
            email: 'admin@gmail.com',
            phone: '01800000000',
            role: 'admin'
          };
          try {
            await setDoc(doc(dbFirestore, 'users', 'admin-main-id'), fallbackAdmin, { merge: true });
          } catch {}
          return { success: true, data: fallbackAdmin };
        }
      }

      let msg = error.message;
      if (error.code === 'auth/user-not-found' || error.code === 'auth/wrong-password' || error.code === 'auth/invalid-credential') {
        msg = 'Invalid email or password.';
      }
      return { success: false, message: msg };
    }
  },

  // 4. Verify & Resend (kept for interface compatibility)
  verifySignup: async (_email: string, _token: string) => {
    return { success: true };
  },

  resendSignupCode: async (_email: string) => {
    return { success: true };
  },

  // 5. User Profiles
  upsertProfile: async (user: User) => {
    try {
      await setDoc(doc(dbFirestore, 'users', user.id), user, { merge: true });
    } catch (e) {
      console.warn('Profile upsert failed:', e);
    }
  },

  getUserProfile: async (userId: string): Promise<User | null> => {
    try {
      const docSnap = await getDoc(doc(dbFirestore, 'users', userId));
      if (docSnap.exists()) {
        return docSnap.data() as User;
      }
      return null;
    } catch (e) {
      console.warn('Get profile error:', e);
      return null;
    }
  },

  getAllUsers: async (): Promise<User[]> => {
    try {
      const snap = await getDocs(collection(dbFirestore, 'users'));
      return snap.docs.map(d => d.data() as User);
    } catch (e) {
      console.warn('Get all users error:', e);
      return [];
    }
  },

  // 6. Orders
  getOrders: async (userId?: string): Promise<Order[]> => {
    try {
      const ordersCol = collection(dbFirestore, 'orders');
      let q = query(ordersCol, orderBy('date', 'desc'));
      if (userId) {
        q = query(ordersCol, where('userId', '==', userId), orderBy('date', 'desc'));
      }
      const snap = await getDocs(q);
      return snap.docs.map(d => d.data() as Order);
    } catch (error) {
      try {
        const ordersCol = collection(dbFirestore, 'orders');
        let q = query(ordersCol);
        if (userId) {
          q = query(ordersCol, where('userId', '==', userId));
        }
        const snap = await getDocs(q);
        const list = snap.docs.map(d => d.data() as Order);
        return list.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
      } catch (e) {
        console.warn('Get orders error:', e);
        return [];
      }
    }
  },

  addOrder: async (order: Order): Promise<boolean> => {
    try {
      await setDoc(doc(dbFirestore, 'orders', order.id), order);
      return true;
    } catch (e) {
      handleFirestoreError(e, OperationType.CREATE, `orders/${order.id}`);
      return false;
    }
  },

  updateOrderStatus: async (orderId: string, status: string): Promise<boolean> => {
    try {
      await updateDoc(doc(dbFirestore, 'orders', orderId), { status });
      return true;
    } catch (e) {
      handleFirestoreError(e, OperationType.UPDATE, `orders/${orderId}`);
      return false;
    }
  },

  updateOrderDetails: async (orderId: string, updates: Partial<Order>): Promise<boolean> => {
    try {
      await updateDoc(doc(dbFirestore, 'orders', orderId), updates);
      return true;
    } catch (e) {
      handleFirestoreError(e, OperationType.UPDATE, `orders/${orderId}`);
      return false;
    }
  },

  // 7. Support Messages
  getMessages: async (userId?: string): Promise<SupportMessage[]> => {
    try {
      const messagesCol = collection(dbFirestore, 'support_messages');
      let q = query(messagesCol, orderBy('created_at', 'asc'));
      if (userId) {
        q = query(messagesCol, where('user_id', '==', userId), orderBy('created_at', 'asc'));
      }
      const snap = await getDocs(q);
      return snap.docs.map(d => d.data() as SupportMessage);
    } catch (error) {
      try {
        const messagesCol = collection(dbFirestore, 'support_messages');
        let q = query(messagesCol);
        if (userId) {
          q = query(messagesCol, where('user_id', '==', userId));
        }
        const snap = await getDocs(q);
        const list = snap.docs.map(d => d.data() as SupportMessage);
        return list.sort((a, b) => new Date(a.created_at).getTime() - new Date(b.created_at).getTime());
      } catch (e) {
        console.warn('Get messages error:', e);
        return [];
      }
    }
  },

  sendMessage: async (msg: Partial<SupportMessage>): Promise<boolean> => {
    try {
      const id = msg.id || Date.now().toString();
      await setDoc(doc(dbFirestore, 'support_messages', id), {
        ...msg,
        id
      });
      return true;
    } catch (e) {
      handleFirestoreError(e, OperationType.CREATE, `support_messages/${msg.id}`);
      return false;
    }
  },

  markMessagesRead: async (_userId: string) => {},

  // 8. Site Settings
  getSystemSettings: async (): Promise<SiteSettings | null> => {
    try {
      const snap = await getDoc(doc(dbFirestore, 'system_settings', 'general'));
      if (snap.exists()) {
        return snap.data() as SiteSettings;
      }
      return null;
    } catch {
      return null;
    }
  },

  updateSystemSetting: async (key: string, value: string): Promise<boolean> => {
    try {
      await setDoc(doc(dbFirestore, 'system_settings', 'general'), { [key]: value }, { merge: true });
      return true;
    } catch {
      return false;
    }
  }
};
