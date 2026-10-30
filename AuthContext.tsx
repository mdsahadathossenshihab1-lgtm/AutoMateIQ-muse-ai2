import React, { createContext, useContext, useState, useEffect, ReactNode, useRef } from 'react';
import { User, Order, AuthContextType, SupportMessage, SiteSettings } from './types';
import { db, auth, isEmailAdmin } from './services/firebaseClient';
import { onAuthStateChanged, signOut } from 'firebase/auth';
import { CONTACT_INFO, NOTIFICATION_SOUND_BASE64 } from './constants';
import { getUserLocation } from './services/locationService';

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [users, setUsers] = useState<User[]>([]); 
  const [orders, setOrders] = useState<Order[]>([]);
  const [messages, setMessages] = useState<SupportMessage[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  // Site Settings State
  const [siteSettings, setSiteSettings] = useState<SiteSettings>({
    facebook: CONTACT_INFO.facebookPage,
    youtube: CONTACT_INFO.youtubeChannel
  });
  
  const [toastMessage, setToastMessage] = useState<{title: string, body: string} | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);

  // Audio & Notification Logic
  const initAudio = () => {
    if (!audioContextRef.current) {
      audioContextRef.current = new (window.AudioContext || (window as any).webkitAudioContext)();
    }
    if (audioContextRef.current.state === 'suspended') {
      audioContextRef.current.resume();
    }
  };

  const playNotificationSound = async () => {
    try {
      if (!audioContextRef.current) initAudio();
      const ctx = audioContextRef.current;
      if (!ctx) return;
      const response = await fetch(NOTIFICATION_SOUND_BASE64);
      const arrayBuffer = await response.arrayBuffer();
      const audioBuffer = await ctx.decodeAudioData(arrayBuffer);
      const source = ctx.createBufferSource();
      source.buffer = audioBuffer;
      source.connect(ctx.destination);
      source.start(0);
    } catch (e) {
      console.warn("Audio playback failed", e);
    }
  };

  const requestNotificationPermission = async () => {
    if (!('Notification' in window)) return false;
    try {
      const permission = await Notification.requestPermission();
      initAudio();
      if (permission === 'granted') {
        playNotificationSound();
        setToastMessage({ title: 'Notifications Enabled', body: 'You will now receive alerts.' });
        setTimeout(() => setToastMessage(null), 5000);
        return true;
      }
    } catch (e) {
      console.error(e);
    }
    return false;
  };

  // Data Refresh Logic
  const refreshData = async (currentUser?: User) => {
    const targetUser = currentUser || user;
    if (!targetUser) {
      setOrders([]);
      setUsers([]);
      setMessages([]);
      return;
    }
    try {
      const [settings, profile, msgs] = await Promise.all([
         db.getSystemSettings().catch(() => null),
         db.getUserProfile(targetUser.id).catch(() => null),
         db.getMessages(targetUser.role === 'admin' ? undefined : targetUser.id).catch(() => [])
      ]);

      if (settings) setSiteSettings(prev => ({ ...prev, ...settings }));
      if (profile) setUser(profile);
      if (msgs) setMessages(msgs);

      if (targetUser.role === 'admin') {
        const [dbOrders, dbUsers] = await Promise.all([
           db.getOrders().catch(() => []),
           db.getAllUsers().catch(() => [])
        ]);
        if (dbOrders) setOrders(dbOrders);
        if (dbUsers) setUsers(dbUsers);
      } else {
        const dbOrders = await db.getOrders(targetUser.id).catch(() => []);
        if (dbOrders) setOrders(dbOrders);
      }
    } catch (e) {
      console.error("Data sync error", e);
    }
  };

  // Firebase Auth State Listener
  useEffect(() => {
    let mounted = true;

    const safetyTimer = setTimeout(() => {
      if (mounted && isLoading) {
        setIsLoading(false);
      }
    }, 4000);

    const unsubscribe = onAuthStateChanged(auth, async (fbUser) => {
      if (!mounted) return;

      if (fbUser) {
        try {
          let profile = await db.getUserProfile(fbUser.uid);
          const role: 'admin' | 'user' = isEmailAdmin(fbUser.email) ? 'admin' : (profile?.role || 'user');

          if (!profile) {
            profile = {
              id: fbUser.uid,
              email: fbUser.email || '',
              name: fbUser.displayName || 'User',
              phone: fbUser.phoneNumber || '',
              role: role
            };
            await db.upsertProfile(profile);
          } else if (isEmailAdmin(fbUser.email) && profile.role !== 'admin') {
            profile.role = 'admin';
            await db.upsertProfile(profile);
          }

          setUser(profile);
          await refreshData(profile);
        } catch (err) {
          console.error("Error setting up user profile:", err);
        } finally {
          if (mounted) setIsLoading(false);
        }
      } else {
        setUser(null);
        setOrders([]);
        setMessages([]);
        if (mounted) setIsLoading(false);
      }
    });

    return () => {
      mounted = false;
      clearTimeout(safetyTimer);
      unsubscribe();
    };
  }, []);

  const syncRealtimeOrder = (order: Order, eventType: 'INSERT' | 'UPDATE') => {
    setOrders(prevOrders => {
      if (eventType === 'INSERT') return [order, ...prevOrders];
      if (eventType === 'UPDATE') return prevOrders.map(o => o.id === order.id ? { ...o, ...order } : o);
      return prevOrders;
    });
  };

  // Context Values
  const authMethods: AuthContextType = {
    user, 
    users, 
    orders, 
    messages, 
    siteSettings, 
    isLoading,
    loginUser: async (email: string, pass: string) => {
      const res = await db.login(email, pass);
      if (res.success && res.data) {
        setUser(res.data);
        await refreshData(res.data);
      }
      return res;
    },
    loginWithGoogle: db.loginWithGoogle,
    signupUser: async (data) => {
      const res = await db.signup(data);
      if (res.success && res.data?.profile) {
        setUser(res.data.profile);
        await refreshData(res.data.profile);
      }
      return res;
    },
    verifyEmail: db.verifySignup,
    resendSignupCode: db.resendSignupCode,
    logout: async () => { 
      try {
        await signOut(auth);
      } catch(e) {
        console.error("Logout error:", e);
      } finally {
        localStorage.clear(); 
        sessionStorage.clear();
        setUser(null); 
        setOrders([]);
        setMessages([]);
      }
    },
    addOrder: async (o: any) => { 
      const newOrder: Order = { 
        ...o, 
        id: Date.now().toString(), 
        date: new Date().toISOString(), 
        status: 'pending' 
      };
      setOrders(p => [newOrder, ...p]);
      await db.addOrder(newOrder); 
    },
    updateOrderStatus: async (id: string, s: 'approved' | 'rejected' | 'completed') => { 
      setOrders(p => p.map(o => o.id === id ? {...o, status: s} : o));
      return await db.updateOrderStatus(id, s);
    },
    updateOrderDetails: async (id: string, u: any) => {
      setOrders(p => p.map(o => o.id === id ? {...o, ...u} : o));
      await db.updateOrderDetails(id, u);
    },
    sendSupportMessage: async (text: string, targetId?: string) => {
      if (!user) return false;
      const msg: SupportMessage = { 
        id: Date.now().toString(), 
        user_id: targetId || user.id, 
        sender_role: user.role, 
        message: text, 
        is_read: false, 
        created_at: new Date().toISOString() 
      };
      setMessages(p => [...p, msg]);
      return await db.sendMessage(msg);
    },
    syncRealtimeOrder,
    requestNotificationPermission,
    updateSiteSettings: async (s: SiteSettings) => { 
      setSiteSettings(s); 
      await db.updateSystemSetting('facebook_url', s.facebook); 
      return true; 
    },
    markMessagesRead: async () => {}
  };

  return (
    <AuthContext.Provider value={authMethods}>
      {children}
      {toastMessage && (
        <div className="fixed top-24 right-5 z-[100] animate-bounce-in cursor-pointer" onClick={() => setToastMessage(null)}>
          <div className="bg-white border-l-4 border-blue-600 rounded-xl shadow-2xl p-4 flex items-start gap-3 max-w-sm ring-1 ring-black/5">
             <div className="bg-blue-100 p-2 rounded-full text-blue-600 shrink-0">
               <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/></svg>
             </div>
             <div>
               <h4 className="font-bold text-slate-900 text-sm">{toastMessage.title}</h4>
               <p className="text-xs text-slate-500 mt-1 leading-relaxed">{toastMessage.body}</p>
             </div>
          </div>
        </div>
      )}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};