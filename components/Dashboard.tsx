import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../AuthContext';
import { useLanguage } from '../LanguageContext';
import { CONTACT_INFO } from '../constants';
import { LogOut, Package, CheckCircle, Clock, XCircle, LayoutDashboard, Users, ShoppingCart, User as UserIcon, Send, Link as LinkIcon, Calendar, X, Mail, MessageSquare, ExternalLink, Copy, Check, Search, ArrowLeft, ArrowRight, Phone, Award, Loader2, Zap, Coins, MessageCircle, Settings, Save, AlertTriangle, Terminal, FileCode, Server, Menu, FileText, HelpCircle, BellRing, Activity, MapPin, BarChart3, AlertOctagon, Volume2 } from 'lucide-react';
import { Order, User, SupportMessage } from '../types';
import Logo from './Logo';

interface DashboardProps {
  onClose: () => void;
  onNavigateToPricing: () => void;
}

// --- Helper Components ---

const CopyButton = ({ text, title = "Copy" }: { text: string, title?: string }) => {
  const [copied, setCopied] = useState(false);
  const handleCopy = () => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };
  
  return (
    <button 
      onClick={handleCopy}
      className={`transition-colors ${copied ? 'text-blue-600' : 'text-slate-400 hover:text-blue-600'}`}
      title={copied ? "Copied!" : title}
    >
      {copied ? <Check size={14} /> : <Copy size={14} />}
    </button>
  );
};

// --- Chat Interface ---
const ChatInterface = ({ messages, currentUserId, onSend, targetUser }: { messages: SupportMessage[], currentUserId: string, onSend: (text: string) => void, targetUser?: User }) => {
  const [input, setInput] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const scrollToBottom = () => { messagesEndRef.current?.scrollIntoView({ behavior: "smooth" }); };
  useEffect(() => { scrollToBottom(); }, [messages]);
  const handleSend = (e: React.FormEvent) => { e.preventDefault(); if (!input.trim()) return; onSend(input); setInput(''); };

  return (
    <div className="flex flex-col h-[700px] bg-white rounded-2xl border border-slate-200 shadow-xl overflow-hidden">
      <div className="bg-slate-50 border-b border-slate-200 p-5 flex items-center gap-4 sticky top-0 z-10">
        <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center text-blue-600 font-bold text-lg border border-blue-200">
          {targetUser ? (targetUser.name || 'U').charAt(0).toUpperCase() : 'S'}
        </div>
        <div>
          <h3 className="font-bold text-slate-900 text-lg">{targetUser ? targetUser.name : 'সাপোর্ট চ্যাট'}</h3>
          <p className="text-sm text-slate-500 flex items-center gap-1">
            <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
            {targetUser ? targetUser.email : 'সরাসরি কথা বলুন'}
          </p>
        </div>
      </div>
      
      <div className="flex-1 overflow-y-auto p-6 space-y-6 scrollbar-thin scrollbar-thumb-slate-200 scrollbar-track-transparent">
        {messages.length === 0 && (
          <div className="flex flex-col items-center justify-center h-full text-slate-400 opacity-70">
            <MessageSquare size={64} className="mb-4 text-slate-300" />
            <p className="text-lg font-medium">কোনো মেসেজ নেই। কথা শুরু করুন!</p>
          </div>
        )}
        
        {messages.map((msg) => {
          const amIAdmin = !!targetUser; 
          const isMe = amIAdmin 
            ? msg.sender_role === 'admin' 
            : msg.sender_role === 'user'; 

          return (
            <div key={msg.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
              <div className={`max-w-[75%] space-y-1`}>
                <div className={`px-5 py-3.5 rounded-2xl text-sm leading-relaxed shadow-sm border ${isMe ? 'bg-blue-600 text-white rounded-br-sm border-blue-600' : 'bg-slate-100 border-slate-200 text-slate-800 rounded-bl-sm'}`}>
                  <p className="whitespace-pre-wrap">{msg.message}</p>
                </div>
                <p className={`text-[10px] ${isMe ? 'text-right text-slate-400' : 'text-left text-slate-400'}`}>
                  {new Date(msg.created_at).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                </p>
              </div>
            </div>
          );
        })}
        <div ref={messagesEndRef} />
      </div>
      
      <form onSubmit={handleSend} className="p-5 bg-slate-50 border-t border-slate-200">
        <div className="flex gap-3 bg-white p-2 rounded-2xl border border-slate-200 focus-within:border-blue-500 focus-within:ring-1 focus-within:ring-blue-200 transition-all shadow-sm">
          <input 
            type="text" 
            value={input} 
            onChange={(e) => setInput(e.target.value)} 
            placeholder="মেসেজ লিখুন..." 
            className="flex-1 px-4 bg-transparent outline-none text-slate-900 placeholder-slate-400 font-medium" 
          />
          <button type="submit" disabled={!input.trim()} className="bg-blue-600 text-white p-3 rounded-xl hover:bg-blue-500 transition-all disabled:opacity-50 hover:shadow-lg active:scale-95">
            <Send size={20} />
          </button>
        </div>
      </form>
    </div>
  );
};

const ApproveModal = ({ order, isOpen, onClose, onApprove }: { order: Order | null, isOpen: boolean, onClose: () => void, onApprove: (data: any) => void }) => {
  const [fbLink, setFbLink] = useState('');
  if (!isOpen || !order) return null;
  const isTopUp = order.serviceName === 'API ক্রেডিট রিচার্জ' || order.serviceName === 'API Credit Top-up';
  
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onApprove(isTopUp ? { adminFbLink: 'N/A', startDate: new Date().toISOString() } : { adminFbLink: fbLink });
  };

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white border border-slate-200 rounded-3xl shadow-2xl w-full max-w-md overflow-hidden animate-fade-in-up">
        <div className="bg-slate-50 p-6 flex justify-between items-center text-slate-900 border-b border-slate-200">
          <h3 className="font-bold text-xl">{isTopUp ? 'টপ-আপ কনফার্মেশন' : 'অর্ডার গ্রহণ করুন'}</h3>
          <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full transition-colors"><X size={20} /></button>
        </div>
        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
            <p className="text-sm text-blue-600 mb-1">অর্ডার সার্ভিস</p>
            <p className="font-bold text-slate-900 text-lg">{order.serviceName}</p>
            {isTopUp && <div className="mt-3 text-blue-700 font-bold bg-blue-100 px-3 py-1 rounded-lg inline-block">৳ {order.amount}</div>}
          </div>
          
          {!isTopUp && (
            <div className="space-y-2">
              <label className="block text-sm font-bold text-slate-700">ফেসবুক প্রোফাইল লিংক (অ্যাডমিন)</label>
              <input type="text" required placeholder="https://facebook.com/..." value={fbLink} onChange={e => setFbLink(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 font-medium placeholder-slate-400" />
            </div>
          )}
          
          <button type="submit" className="w-full bg-blue-600 text-white font-bold py-4 rounded-xl hover:bg-blue-500 transition-all shadow-lg hover:shadow-blue-500/30">
            {isTopUp ? 'টপ-আপ কনফার্ম করুন' : 'লিংক পাঠান'}
          </button>
        </form>
      </div>
    </div>
  );
};

const ReviewModal = ({ order, isOpen, onClose, onAction }: { order: Order | null, isOpen: boolean, onClose: () => void, onAction: (action: 'activate' | 'request_info', data?: any) => void }) => {
  const [mode, setMode] = useState<'activate' | 'request_info'>('activate');
  const [deadline, setDeadline] = useState('');
  const [message, setMessage] = useState('');

  if (!isOpen || !order) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (mode === 'activate') onAction('activate', { completionDate: deadline, startDate: new Date().toISOString() });
    else onAction('request_info', { adminMessage: message });
  };

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
      <div className="bg-white border border-slate-200 rounded-3xl shadow-2xl w-full max-w-lg overflow-hidden animate-fade-in-up">
        <div className="bg-slate-50 p-6 flex justify-between items-center text-slate-900 border-b border-slate-200">
          <h3 className="font-bold text-xl">রিভিউ প্যানেল</h3>
          <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full transition-colors"><X size={20} /></button>
        </div>
        
        <div className="p-6 bg-slate-50 border-b border-slate-200 max-h-64 overflow-y-auto">
           <div className="grid grid-cols-2 gap-3 text-sm">
             <div className="bg-white p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Page Link</span><a href={order.clientPageLink} target="_blank" className="text-blue-600 font-bold hover:underline truncate block">{order.clientPageLink || 'N/A'}</a></div>
             <div className="bg-white p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Doc Link</span><a href={order.clientDocLink} target="_blank" className="text-blue-600 font-bold hover:underline truncate block">{order.clientDocLink || 'N/A'}</a></div>
             <div className="bg-white p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Email</span><span className="text-slate-700 font-medium block truncate">{order.clientEmail || 'N/A'}</span></div>
             <div className="bg-white p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">WhatsApp</span><span className="text-slate-700 font-medium block truncate">{order.clientWhatsapp || 'N/A'}</span></div>
             <div className="col-span-2 bg-white p-3 rounded-xl border border-slate-200"><span className="text-xs text-slate-400 font-bold uppercase block mb-1">Note</span><p className="text-slate-600">{order.clientRequirements || 'None'}</p></div>
           </div>
        </div>

        <form onSubmit={handleSubmit} className="p-8 space-y-6">
          <div className="flex gap-2 bg-slate-100 p-1.5 rounded-xl border border-slate-200">
            <button type="button" onClick={() => setMode('activate')} className={`flex-1 py-2.5 text-sm font-bold rounded-lg transition-all ${mode === 'activate' ? 'bg-blue-600 text-white shadow-md' : 'text-slate-500 hover:text-slate-900'}`}>Activate</button>
            <button type="button" onClick={() => setMode('request_info')} className={`flex-1 py-2.5 text-sm font-bold rounded-lg transition-all ${mode === 'request_info' ? 'bg-orange-600 text-white shadow-md' : 'text-slate-500 hover:text-slate-900'}`}>Request Info</button>
          </div>

          {mode === 'activate' ? (
            <div className="space-y-2">
              <label className="block text-sm font-bold text-slate-700">শেষ হওয়ার সময় (Deadline)</label>
              <input type="datetime-local" required value={deadline} onChange={e => setDeadline(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 font-medium" />
            </div>
          ) : (
            <div className="space-y-2">
              <label className="block text-sm font-bold text-slate-700">কি তথ্য প্রয়োজন?</label>
              <textarea rows={3} required placeholder="লিখুন..." value={message} onChange={e => setMessage(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-orange-500 bg-white text-slate-900 font-medium placeholder-slate-400" />
            </div>
          )}
          
          <button type="submit" className={`w-full text-white font-bold py-4 rounded-xl transition-all shadow-lg ${mode === 'activate' ? 'bg-blue-600 hover:bg-blue-500' : 'bg-orange-600 hover:bg-orange-500'}`}>
            {mode === 'activate' ? 'সাবস্ক্রিপশন চালু করুন' : 'রিকোয়েস্ট পাঠান'}
          </button>
        </form>
      </div>
    </div>
  );
};

const ClientSubmissionForm = ({ order, onSubmit }: { order: Order, onSubmit: (data: any) => void }) => {
  const [docLink, setDocLink] = useState(order.clientDocLink || '');
  const [pageLink, setPageLink] = useState(order.clientPageLink || '');
  const [email, setEmail] = useState(order.clientEmail || '');
  const [whatsapp, setWhatsapp] = useState(order.clientWhatsapp || '');
  const [reqs, setReqs] = useState(order.clientRequirements || '');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (order.serviceName === 'API ক্রেডিট রিচার্জ' || order.serviceName === 'API Credit Top-up') {
    return <div className="mt-6 bg-gradient-to-r from-blue-50 to-indigo-50 border border-blue-100 rounded-3xl p-8 text-slate-800 shadow-md animate-fade-in-up"><h3 className="font-bold text-2xl mb-2 flex items-center gap-3 text-blue-600"><Coins size={28} /> রিচার্জ সফল!</h3><p className="text-slate-600 text-lg">আপনার রিচার্জ রিকোয়েস্টটি অ্যাডমিন অ্যাপ্রুভ করেছেন।</p></div>;
  }

  const handleSubmit = (e: React.FormEvent) => { 
    e.preventDefault(); 
    setIsSubmitting(true); 
    onSubmit({ clientDocLink: docLink, clientPageLink: pageLink, clientEmail: email, clientWhatsapp: whatsapp, clientRequirements: reqs, adminMessage: null }); 
  };

  const isResubmission = !!order.adminMessage;

  return (
    <div className="bg-white rounded-3xl p-8 shadow-xl border border-slate-200 animate-fade-in-up">
      <div className="flex items-center gap-3 mb-6">
        <div className={`w-12 h-12 rounded-full flex items-center justify-center ${isResubmission ? 'bg-orange-50 text-orange-500' : 'bg-blue-50 text-blue-500'}`}>
          {isResubmission ? <AlertTriangle size={24}/> : <CheckCircle size={24}/>}
        </div>
        <div>
          <h3 className="font-bold text-xl text-slate-900">{isResubmission ? 'তথ্য আপডেট প্রয়োজন' : 'অর্ডার সেটআপ'}</h3>
          <p className="text-slate-500 text-sm">অ্যাক্টিভেশনের জন্য তথ্য দিন</p>
        </div>
      </div>
      
      {isResubmission && (
        <div className="bg-orange-50 p-5 rounded-2xl border border-orange-200 mb-8">
          <p className="text-xs font-bold text-orange-600 uppercase tracking-wide mb-2">অ্যাডমিন মেসেজ</p>
          <p className="text-slate-800 font-medium">{order.adminMessage}</p>
        </div>
      )}

      {!isResubmission && (
        <div className="bg-blue-50 p-5 rounded-2xl border border-blue-200 mb-8 flex items-start gap-4">
          <div className="bg-white p-2 rounded-lg text-blue-600 shadow-sm shrink-0 border border-blue-100"><LinkIcon size={20} /></div>
          <div>
            <p className="font-bold text-slate-900 mb-1">অ্যাডমিন প্রোফাইল লিংক</p>
            <a href={order.adminFbLink} target="_blank" rel="noopener noreferrer" className="text-blue-600 font-medium hover:underline break-all">{order.adminFbLink || 'Loading...'}</a>
            <p className="text-xs text-slate-500 mt-2">*এই আইডিতে পেজ এক্সেস দিন</p>
          </div>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2"><label className="text-sm font-bold text-slate-700">পেজ লিংক / নাম</label><input type="text" required value={pageLink} onChange={e => setPageLink(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400" placeholder="facebook.com/page" /></div>
          <div className="space-y-2"><label className="text-sm font-bold text-slate-700">বিজনেস ডিটেইলস (Link)</label><input type="text" required value={docLink} onChange={e => setDocLink(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400" placeholder="Google Doc / Sheet Link" /></div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-2"><label className="text-sm font-bold text-slate-700">ইমেইল</label><input type="email" required value={email} onChange={e => setEmail(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400" placeholder="name@email.com" /></div>
          <div className="space-y-2"><label className="text-sm font-bold text-slate-700">WhatsApp</label><input type="tel" required value={whatsapp} onChange={e => setWhatsapp(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium placeholder-slate-400" placeholder="017..." /></div>
        </div>
        <div className="space-y-2"><label className="text-sm font-bold text-slate-700">অন্যান্য তথ্য</label><textarea rows={3} value={reqs} onChange={e => setReqs(e.target.value)} className="w-full px-4 py-3 border border-slate-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium resize-none placeholder-slate-400" placeholder="আপনার রিকোয়ারমেন্টস..." /></div>
        <button type="submit" disabled={isSubmitting} className="w-full bg-blue-600 text-white font-bold py-4 rounded-xl hover:bg-blue-500 transition-all shadow-lg hover:shadow-blue-500/30 flex items-center justify-center gap-3">
          {isSubmitting ? <Loader2 className="animate-spin" /> : <>{isResubmission ? 'আপডেট করুন' : 'সাবমিট করুন'} <Send size={20} /></>}
        </button>
      </form>
    </div>
  );
};

const Dashboard: React.FC<DashboardProps> = ({ onClose, onNavigateToPricing }) => {
  const { user, users, orders, logout, updateOrderStatus, updateOrderDetails, isLoading, messages, sendSupportMessage, markMessagesRead, siteSettings, updateSiteSettings, requestNotificationPermission } = useAuth();
  const { t } = useLanguage();
  const [activeTab, setActiveTab] = useState<'users' | 'profile' | 'support' | 'settings'>('profile');
  const [searchTerm, setSearchTerm] = useState('');
  
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  
  const [isApproveModalOpen, setIsApproveModalOpen] = useState(false);
  const [isReviewModalOpen, setIsReviewModalOpen] = useState(false);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [selectedChatUser, setSelectedChatUser] = useState<User | null>(null);
  
  const [settingsFb, setSettingsFb] = useState(siteSettings.facebook);
  const [settingsYt, setSettingsYt] = useState(siteSettings.youtube);
  const [isSavingSettings, setIsSavingSettings] = useState(false);
  const [notifPermission, setNotifPermission] = useState<NotificationPermission>('default');

  useEffect(() => {
    if ('Notification' in window) {
       setNotifPermission(Notification.permission);
    }
  }, []);

  const handleRequestPermission = async () => {
    const granted = await requestNotificationPermission();
    if (granted) {
      setNotifPermission('granted');
      alert("সাউন্ড এবং নোটিফিকেশন চালু হয়েছে!");
    } else {
      setNotifPermission('denied');
    }
  };

  useEffect(() => { if (user && user.role !== 'admin' && (activeTab === 'users' || activeTab === 'settings')) { setActiveTab('profile'); } }, [user, activeTab]);
  useEffect(() => { setSettingsFb(siteSettings.facebook); setSettingsYt(siteSettings.youtube); }, [siteSettings]);

  // Actions
  const handleApproveClick = (order: Order) => { setSelectedOrder(order); setIsApproveModalOpen(true); };
  const confirmApprove = async (data: { adminFbLink: string, startDate?: string }) => { if (!selectedOrder) return; await updateOrderStatus(selectedOrder.id, 'approved'); const updates: any = { adminFbLink: data.adminFbLink }; if (data.startDate) updates.startDate = data.startDate; await updateOrderDetails(selectedOrder.id, updates); setIsApproveModalOpen(false); setSelectedOrder(null); };
  const handleReviewClick = (order: Order) => { setSelectedOrder(order); setIsReviewModalOpen(true); };
  const confirmReviewAction = async (action: 'activate' | 'request_info', data: any) => { 
    if (!selectedOrder) return; 
    if (action === 'activate') { 
      await updateOrderDetails(selectedOrder.id, { completionDate: data.completionDate, startDate: data.startDate, adminMessage: undefined }); 
    } else { 
      await updateOrderDetails(selectedOrder.id, { adminMessage: data.adminMessage, isDetailsSubmitted: false }); 
    } 
    setIsReviewModalOpen(false); 
    setSelectedOrder(null); 
  };
  const handleReject = async (orderId: string) => { if (confirm('Reject this order?')) { await updateOrderStatus(orderId, 'rejected'); } };
  const handleComplete = async (orderId: string) => { if (confirm('Mark as Completed?')) { await updateOrderStatus(orderId, 'completed'); } };
  const handleClientSubmit = async (data: any) => { 
    const pendingOrder = orders.find(o => o.status === 'approved' && (!o.isDetailsSubmitted || !!o.adminMessage) && !o.startDate); 
    if (pendingOrder) { 
      await updateOrderDetails(pendingOrder.id, { ...data, isDetailsSubmitted: true, adminMessage: undefined }); 
    } 
  };
  const handleSaveSettings = async () => { setIsSavingSettings(true); const success = await updateSiteSettings({ facebook: settingsFb, youtube: settingsYt }); setIsSavingSettings(false); if (success) alert("Saved!"); };

  const handleLogout = () => {
    logout();
    onClose();
  };

  const handleTestNotification = async () => {
    if (notifPermission !== 'granted') {
      await handleRequestPermission();
    } else {
      const { sendSupportMessage } = useAuth(); // We just simulate a generic alert via AuthContext internal method logic or reuse the sendBrowserNotification if exposed.
      // Since sendBrowserNotification is internal to AuthContext, we trigger it via permission request which plays sound test.
      await requestNotificationPermission(); 
    }
  };

  const uniqueChatUserIds = Array.from(new Set(messages.map(m => m.user_id)));
  const chatUsers = users.filter(u => uniqueChatUserIds.includes(u.id));
  const unreadCounts = chatUsers.reduce<Record<string, number>>((acc, u) => { acc[u.id] = messages.filter(m => m.user_id === u.id && m.sender_role === 'user' && !m.is_read).length; return acc; }, {});
  const totalUnreadCount = (Object.values(unreadCounts) as number[]).reduce((a, b) => a + b, 0);
  
  // Calculate Pending Order Count for Sidebar
  const pendingOrdersCount = orders.filter(o => o.status === 'pending').length;

  const handleSelectChat = (u: User) => { setSelectedChatUser(u); markMessagesRead(u.id); };
  const handleSendMessage = (text: string) => { if (user?.role === 'admin' && selectedChatUser) { sendSupportMessage(text, selectedChatUser.id); } else if (user?.role === 'user') { sendSupportMessage(text); } };

  // Improved Filter Logic
  const filteredUsers = users?.filter(u => {
    const term = searchTerm.toLowerCase();
    const userMatch = (u.name || '').toLowerCase().includes(term) || 
                      (u.email || '').toLowerCase().includes(term) || 
                      (u.phone || '').includes(term);
    
    // Also check if this user has any order matching the search term
    const orderMatch = orders.some(o => o.userId === u.id && (o.id.includes(term) || o.trxId.toLowerCase().includes(term)));
    
    return userMatch || orderMatch;
  });
  
  const userOrders = selectedUser 
    ? orders.filter(o => {
        const matchesUser = o.userId === selectedUser.id;
        const term = searchTerm.toLowerCase();
        const matchesSearch = !searchTerm || (o.id.includes(term) || o.trxId.toLowerCase().includes(term));
        return matchesUser && matchesSearch;
      }) 
    : [];

  const myOrders = orders.filter(o => o.userId === user?.id);
  const orderNeedingAction = myOrders.find(o => o.status === 'approved' && (!o.isDetailsSubmitted || !!o.adminMessage) && !o.startDate);
  
  const isPlanActive = (startDateStr: string | undefined, packageDetails: string) => { 
    if (!startDateStr) return { active: false, daysLeft: 0, duration: 0 };
    const startDate = new Date(startDateStr); 
    const now = new Date(); 
    const diffTime = Math.abs(now.getTime() - startDate.getTime()); 
    const diffDays = Math.ceil(diffTime / (1000 * 3600 * 24));
    const details = packageDetails || '';
    const isTrial = details.toLowerCase().includes('trial') || details.includes('ট্রায়াল');
    const duration = isTrial ? 7 : 30;
    return { active: diffDays <= duration, daysLeft: Math.max(0, duration - diffDays), duration };
  };
  const activeSubscriptions = myOrders.map(o => ({...o, planStatus: isPlanActive(o.startDate, o.packageDetails)})).filter(o => (o.status === 'approved' || o.status === 'completed') && o.planStatus.active && o.serviceName !== 'API ক্রেডিট রিচার্জ' && o.serviceName !== 'API Credit Top-up');

  // Updated Status Badge Logic - Modern Digital Look
  const renderStatusBadge = (order: Order) => {
    switch (order.status) {
      case 'pending': 
        return (
          <span className="px-3 py-1 rounded-full bg-amber-50 text-amber-600 text-xs font-bold border border-amber-100 flex items-center gap-2 shadow-sm">
             <span className="w-2 h-2 rounded-full bg-amber-500 animate-pulse"></span>
             Pending
          </span>
        );
      case 'approved': 
        if (order.startDate) return (
          <span className="px-3 py-1 rounded-full bg-blue-50 text-blue-600 text-xs font-bold border border-blue-100 flex items-center gap-2 shadow-sm">
             <span className="w-2 h-2 rounded-full bg-blue-500 animate-pulse"></span>
             Active
          </span>
        );
        if (order.isDetailsSubmitted) return (
          <span className="px-3 py-1 rounded-full bg-purple-50 text-purple-600 text-xs font-bold border border-purple-100 flex items-center gap-2 shadow-sm">
             <span className="w-2 h-2 rounded-full bg-purple-500 animate-pulse"></span>
             Reviewing
          </span>
        );
        return (
          <span className="px-3 py-1 rounded-full bg-cyan-50 text-cyan-600 text-xs font-bold border border-cyan-100 flex items-center gap-2 shadow-sm">
             <Loader2 size={12} className="animate-spin text-cyan-600"/>
             Processing
          </span>
        );
      case 'completed': 
        return (
          <span className="px-3 py-1 rounded-full bg-slate-100 text-slate-500 text-xs font-bold border border-slate-200 flex items-center gap-2 shadow-sm">
             <CheckCircle size={12} /> Completed
          </span>
        );
      case 'rejected': 
        return (
          <span className="px-3 py-1 rounded-full bg-red-50 text-red-600 text-xs font-bold border border-red-100 flex items-center gap-2 shadow-sm">
             <XCircle size={12} /> Rejected
          </span>
        );
      default: return null;
    }
  };

  const NavButton = ({ id, icon: Icon, label, badge }: any) => (
    <button onClick={() => { setActiveTab(id); setSelectedUser(null); setSelectedChatUser(null); setMobileMenuOpen(false); }} className={`w-full flex items-center gap-3 px-4 py-3.5 rounded-xl transition-all font-medium text-sm mb-1 ${activeTab === id ? 'bg-blue-50 text-blue-600 border border-blue-200 shadow-sm' : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'}`}>
      <Icon size={18} /> {label} {badge > 0 && <span className="ml-auto bg-red-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-md animate-pulse">{badge}</span>}
    </button>
  );

  const SERVER_CODE = `import { serve } from "https://deno.land/std@0.168.0/http/server.ts"; import { createClient } from "https://esm.sh/@supabase/supabase-js@2"; serve(async (req) => { return new Response(JSON.stringify({ message: "SupaBot Active" }), { status: 200, headers: { 'Content-Type': 'application/json' } }); });`;
  const DEPLOY_COMMAND = `npx supabase functions deploy supa-bot-core --project-ref dyqxsqpdomfsfcgtrhmh`;

  return (
    <div className="fixed inset-0 z-50 bg-white/90 backdrop-blur-md flex overflow-hidden font-sans text-slate-900">
      {/* Mobile Overlay */}
      <div 
        className={`fixed inset-0 bg-black/50 z-30 transition-opacity duration-300 md:hidden ${mobileMenuOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'}`}
        onClick={() => setMobileMenuOpen(false)}
      ></div>

      {/* Sidebar */}
      <aside className={`absolute inset-y-0 left-0 z-40 w-72 bg-white border-r border-slate-200 flex flex-col shrink-0 shadow-2xl transform transition-transform duration-300 ease-in-out md:relative md:translate-x-0 ${mobileMenuOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="p-8 pb-0 flex justify-between items-center md:block">
          <div>
            <Logo className="text-2xl" />
            <p className="text-xs text-slate-400 mt-2 font-medium tracking-wide uppercase">{user?.role === 'admin' ? t('dashboard.admin_workspace') : t('dashboard.client_portal')}</p>
          </div>
          <button onClick={() => setMobileMenuOpen(false)} className="md:hidden text-slate-400 hover:text-slate-900"><X size={24}/></button>
        </div>
        <div className="p-6 space-y-1 mt-6 flex-1 overflow-y-auto">
          {user?.role === 'admin' ? (
            <>
              <p className="px-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2">{t('dashboard.manage')}</p>
              <NavButton id="users" icon={Users} label={t('dashboard.users_orders')} badge={pendingOrdersCount} />
              <NavButton id="support" icon={MessageCircle} label={t('dashboard.inbox')} badge={totalUnreadCount} />
              <NavButton id="settings" icon={Settings} label={t('dashboard.settings')} />
            </>
          ) : (
            <>
              <p className="px-4 text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-2">{t('dashboard.menu')}</p>
              <NavButton id="profile" icon={LayoutDashboard} label={t('nav.dashboard')} />
              <NavButton id="support" icon={MessageCircle} label={t('dashboard.inbox')} />
            </>
          )}
        </div>
        <div className="p-6 border-t border-slate-100">
          <div className="flex items-center gap-3 mb-4 px-4">
            <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center text-blue-600 font-bold border border-blue-100">
              {(user?.name || 'U').charAt(0)}
            </div>
            <div className="overflow-hidden">
              <p className="text-sm font-bold text-slate-900 truncate">{user?.name || 'User'}</p>
              <p className="text-xs text-slate-500 truncate">{user?.email}</p>
            </div>
          </div>
          <button onClick={handleLogout} className="w-full flex items-center gap-2 px-4 py-2.5 rounded-lg text-red-500 hover:bg-red-50 font-bold text-xs transition-colors justify-center uppercase tracking-wide border border-red-100 hover:border-red-200">
            <LogOut size={16} /> {t('dashboard.sign_out')}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col relative overflow-hidden bg-slate-50">
        {/* Mobile Header */}
        <div className="md:hidden bg-white border-b border-slate-200 p-4 flex justify-between items-center shadow-sm z-30">
          <div className="flex items-center gap-3">
             <button onClick={() => setMobileMenuOpen(true)} className="text-slate-900 p-1"><Menu size={24}/></button>
             <Logo />
          </div>
          <div className="flex gap-4">
             <button onClick={onClose}><X size={24} className="text-slate-400 hover:text-slate-900"/></button>
          </div>
        </div>
        
        {/* NOTIFICATION PERMISSION BANNER - Always visible if not granted */}
        {notifPermission !== 'granted' && (
          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white px-4 py-3 flex items-center justify-between shadow-md z-30 animate-fade-in-up">
             <div className="flex items-center gap-3">
               <div className="bg-white/20 p-2 rounded-full animate-bounce-slow">
                 <Volume2 size={20} />
               </div>
               <div>
                  <p className="text-sm font-bold">নোটিফিকেশন ও সাউন্ড চালু করুন</p>
                  <p className="text-[10px] text-blue-100">অর্ডার আপডেট পেতে এটি জরুরি</p>
               </div>
             </div>
             <button 
                onClick={handleRequestPermission}
                className="bg-white text-blue-600 px-4 py-2 rounded-lg text-xs font-bold hover:bg-blue-50 transition-colors shadow-lg active:scale-95"
             >
                Enable Now
             </button>
          </div>
        )}

        <div className="flex-1 overflow-y-auto p-6 md:p-12 scrollbar-thin scrollbar-thumb-slate-300">
          <div className="max-w-6xl mx-auto">
            {/* Header Area */}
            <div className="flex justify-between items-end mb-10">
              <div>
                <h1 className="text-3xl font-bold text-slate-900 tracking-tight">
                  {activeTab === 'users' ? t('dashboard.users_orders') : activeTab === 'support' ? t('dashboard.inbox') : activeTab === 'settings' ? t('dashboard.settings') : `${t('dashboard.welcome')}, ${(user?.name || 'User').split(' ')[0]}`}
                </h1>
                <p className="text-slate-500 mt-1">{t('dashboard.manage_account')}</p>
              </div>
              <button onClick={onClose} className="hidden md:flex items-center gap-2 text-sm font-bold text-slate-500 hover:text-slate-900 bg-white px-4 py-2 rounded-full shadow-sm border border-slate-200 transition-all hover:shadow-md">
                <LogOut size={14} className="rotate-180" /> {t('dashboard.exit')}
              </button>
            </div>

            {/* USER PROFILE VIEW */}
            {activeTab === 'profile' && user?.role === 'user' && (
              <div className="space-y-10 animate-fade-in-up">
                
                {/* Notification Tester for User */}
                <div className="flex justify-end">
                   <button onClick={handleTestNotification} className="text-xs font-bold text-blue-600 bg-blue-50 px-3 py-2 rounded-lg hover:bg-blue-100 flex items-center gap-2 transition-colors">
                      <Volume2 size={14} /> Test Sound Check
                   </button>
                </div>

                {activeSubscriptions.length > 0 && (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {activeSubscriptions.map(sub => {
                      const percentage = Math.round((sub.planStatus.daysLeft / sub.planStatus.duration) * 100);
                      
                      return (
                        <div key={sub.id} className="relative group overflow-hidden bg-white rounded-3xl p-6 border border-slate-100 shadow-[0_8px_30px_rgb(0,0,0,0.04)] hover:shadow-[0_20px_40px_rgb(0,0,0,0.08)] transition-all duration-300">
                          {/* Decorative blurred blob */}
                          <div className="absolute -top-10 -right-10 w-40 h-40 bg-blue-50 rounded-full blur-3xl opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>

                          <div className="relative z-10 flex flex-col h-full">
                            <div className="flex justify-between items-start mb-4">
                              <div className="p-3 bg-blue-50 rounded-2xl text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors duration-300">
                                <Zap size={24} />
                              </div>
                              {renderStatusBadge(sub)}
                            </div>

                            <h3 className="text-xl font-bold text-slate-900 mb-1">{sub.serviceName}</h3>
                            <p className="text-sm text-slate-500 mb-6">{sub.packageDetails}</p>

                            <div className="mt-auto">
                              <div className="flex justify-between items-end mb-2">
                                <div>
                                  <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Time Remaining</span>
                                  <div className="flex items-baseline gap-1">
                                    <span className="text-2xl font-bold text-slate-900 font-mono">{sub.planStatus.daysLeft}</span>
                                    <span className="text-xs text-slate-500 font-medium">Days</span>
                                  </div>
                                </div>
                                <span className="text-xs font-bold text-blue-600 bg-blue-50 px-2 py-1 rounded-md">{percentage}%</span>
                              </div>
                              {/* Progress Bar */}
                              <div className="h-2 w-full bg-slate-100 rounded-full overflow-hidden">
                                <div 
                                  className="h-full bg-gradient-to-r from-blue-500 to-indigo-500 rounded-full transition-all duration-1000 ease-out" 
                                  style={{ width: `${percentage}%` }}
                                ></div>
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}

                {orderNeedingAction && <ClientSubmissionForm order={orderNeedingAction} onSubmit={handleClientSubmit} />}
                
                {!orderNeedingAction && myOrders.some(o => o.status === 'approved' && o.isDetailsSubmitted && !o.startDate && !o.adminMessage) && (
                   <div className="bg-purple-50 border border-purple-200 rounded-2xl p-6 shadow-sm flex items-start gap-4">
                      <div className="bg-purple-100 p-3 rounded-full text-purple-600"><Clock size={24}/></div>
                      <div><h3 className="text-lg font-bold text-slate-900">{t('dashboard.review_in_progress')}</h3><p className="text-slate-500">{t('dashboard.review_msg')}</p></div>
                   </div>
                )}

                <div>
                  <h2 className="text-xl font-bold text-slate-900 mb-6 flex items-center gap-2"><BarChart3 className="text-slate-400"/> {t('dashboard.recent_orders')}</h2>
                  
                  {/* Clean List View for Orders */}
                  <div className="grid grid-cols-1 gap-4">
                     {myOrders.length === 0 && (
                        <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200">
                           <Package size={48} className="mx-auto mb-4 opacity-30"/>
                           <p>{t('dashboard.no_orders')}</p>
                        </div>
                     )}

                     {myOrders.map(order => (
                       <div key={order.id} className="bg-white p-6 rounded-2xl border border-slate-100 shadow-[0_2px_10px_rgb(0,0,0,0.02)] hover:shadow-md transition-shadow flex flex-col md:flex-row md:items-center justify-between gap-4">
                          <div className="flex items-center gap-4">
                             <div className="w-12 h-12 bg-slate-50 rounded-xl flex items-center justify-center text-slate-400 border border-slate-100">
                                <Package size={20} />
                             </div>
                             <div>
                                <h4 className="font-bold text-slate-900">{order.serviceName}</h4>
                                <div className="flex items-center gap-2 text-xs text-slate-500 mt-1">
                                  <span className="font-mono">#{order.id.slice(-6)}</span>
                                  <span>•</span>
                                  <span>{new Date(order.date).toLocaleDateString()}</span>
                                </div>
                             </div>
                          </div>
                          
                          <div className="flex items-center gap-6 md:justify-end flex-1">
                             <div>
                                <p className="text-xs text-slate-400 font-bold uppercase tracking-wider mb-1 text-right">Amount</p>
                                <p className="text-slate-900 font-bold font-mono text-right">{order.amount}</p>
                             </div>
                             <div>{renderStatusBadge(order)}</div>
                          </div>
                       </div>
                     ))}
                  </div>
                </div>
              </div>
            )}

            {/* ADMIN USERS VIEW */}
            {activeTab === 'users' && user?.role === 'admin' && (
              <div className="space-y-6 animate-fade-in-up">
                {!selectedUser ? (
                  <div className="bg-white rounded-3xl shadow-sm border border-slate-200 overflow-hidden">
                    <div className="p-6 border-b border-slate-200 flex gap-4">
                      <div className="relative flex-1">
                        <Search className="absolute left-4 top-3.5 text-slate-400" size={18} />
                        <input type="text" placeholder={t('dashboard.search_placeholder')} value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} className="w-full pl-12 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 text-slate-900 font-medium placeholder-slate-400" />
                      </div>
                    </div>
                    <table className="w-full text-left">
                      <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wider font-semibold">
                        <tr>
                          <th className="px-8 py-5">{t('dashboard.user')}</th>
                          <th className="px-8 py-5">{t('dashboard.location')}</th>
                          <th className="px-8 py-5">{t('nav.contact')}</th>
                          <th className="px-8 py-5 text-right">{t('dashboard.action')}</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100">
                        {filteredUsers?.map((u) => {
                          const userPendingCount = orders.filter(o => o.userId === u.id && o.status === 'pending').length;
                          const userTotalOrders = orders.filter(o => o.userId === u.id).length;
                          
                          return (
                            <tr key={u.id} className="hover:bg-blue-50 transition-colors">
                              <td className="px-8 py-5">
                                <div className="flex items-center gap-3">
                                  <div className="w-10 h-10 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 font-bold border border-slate-200">{(u.name || '?').charAt(0)}</div>
                                  <div><p className="font-bold text-slate-900">{u.name || 'Unknown'}</p><p className="text-xs text-slate-500">{u.email}</p></div>
                                </div>
                              </td>
                              <td className="px-8 py-5">
                                <div className="flex items-center gap-1.5 text-sm text-slate-500">
                                  <MapPin size={14} className="text-slate-400" />
                                  <span>{u.location || 'Unknown'}</span>
                                </div>
                              </td>
                              <td className="px-8 py-5 font-mono text-slate-500 text-sm">{u.phone}</td>
                              <td className="px-8 py-5 text-right">
                                <div className="flex items-center justify-end gap-3">
                                  {userPendingCount > 0 && <span className="bg-amber-100 text-amber-600 text-[10px] font-bold px-2 py-1 rounded-md border border-amber-200">{userPendingCount} New</span>}
                                  {userTotalOrders > 0 && <span className="bg-slate-100 text-slate-600 text-[10px] font-bold px-2 py-1 rounded-md">{userTotalOrders} Orders</span>}
                                  <button onClick={() => setSelectedUser(u)} className="text-blue-600 font-bold text-xs bg-blue-50 border border-blue-200 px-4 py-2 rounded-lg hover:bg-blue-100 transition-colors">{t('dashboard.manage')}</button>
                                </div>
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div>
                    <button onClick={() => setSelectedUser(null)} className="mb-6 flex items-center gap-2 text-slate-500 hover:text-slate-900 font-bold text-sm transition-colors"><ArrowLeft size={16}/> {t('dashboard.back_users')}</button>
                    <div className="grid grid-cols-1 gap-6">
                      {userOrders.map(order => (
                        <div key={order.id} className={`bg-white rounded-3xl border p-8 shadow-sm relative overflow-hidden ${order.status === 'pending' ? 'border-amber-400 border-l-8' : 'border-slate-200'}`}>
                          {order.status === 'pending' && <span className="absolute top-4 right-4 bg-amber-100 text-amber-700 text-[10px] font-bold px-2 py-1 rounded uppercase tracking-wider border border-amber-200">New Order</span>}
                          <div className="flex justify-between items-start mb-8">
                            <div>
                              <h3 className="font-bold text-xl text-slate-900">{order.serviceName}</h3>
                              <p className="text-slate-500 text-sm mt-1">{order.packageDetails}</p>
                              {/* Order ID with Copy Button */}
                              <div className="mt-2 flex items-center gap-2 bg-slate-50 border border-slate-200 px-3 py-1 rounded-lg w-fit">
                                <span className="text-xs font-mono text-slate-500 font-bold">ID: {order.id}</span>
                                <CopyButton text={order.id} />
                              </div>
                            </div>
                            {renderStatusBadge(order)}
                          </div>
                          
                          {/* UPDATED ADMIN CARD VIEW TO SHOW DETAILS */}
                          {order.isDetailsSubmitted && (
                             <div className="bg-indigo-50 border border-indigo-200 p-4 rounded-xl mb-6 text-sm">
                               <p className="text-xs font-bold text-indigo-600 uppercase tracking-wide mb-3">Client Submission</p>
                               <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                 <div><span className="block text-xs text-slate-500">Page Link</span><a href={order.clientPageLink} className="text-blue-600 font-bold hover:underline">{order.clientPageLink || '-'}</a></div>
                                 <div><span className="block text-xs text-slate-500">Doc Link</span><a href={order.clientDocLink} className="text-blue-600 font-bold hover:underline">{order.clientDocLink || '-'}</a></div>
                                 <div><span className="block text-xs text-slate-500">Email</span><span className="text-slate-700 font-medium">{order.clientEmail || '-'}</span></div>
                                 <div><span className="block text-xs text-slate-500">WhatsApp</span><span className="text-slate-700 font-medium">{order.clientWhatsapp || '-'}</span></div>
                               </div>
                             </div>
                          )}

                          <div className="bg-slate-50 rounded-2xl p-6 grid grid-cols-2 md:grid-cols-4 gap-6 mb-8 border border-slate-200">
                            <div><p className="text-xs font-bold text-slate-500 uppercase mb-1">{t('dashboard.amount')}</p><p className="text-lg font-bold text-slate-900">{order.amount}</p></div>
                            <div><p className="text-xs font-bold text-slate-500 uppercase mb-1">TrxID</p><p className="text-lg font-mono text-slate-800">{order.trxId}</p></div>
                            <div><p className="text-xs font-bold text-slate-500 uppercase mb-1">Sender</p><p className="text-lg font-mono text-slate-800">{order.senderPhone}</p></div>
                            <div><p className="text-xs font-bold text-slate-500 uppercase mb-1">Date</p><p className="text-lg text-slate-800">{new Date(order.date).toLocaleDateString()}</p></div>
                          </div>
                          <div className="flex flex-wrap gap-3 justify-end">
                            {order.status === 'pending' && <><button onClick={() => handleReject(order.id)} className="px-6 py-3 rounded-xl font-bold text-red-600 bg-red-50 hover:bg-red-100 border border-red-200">Reject</button><button onClick={() => handleApproveClick(order)} className="px-6 py-3 rounded-xl font-bold text-white bg-blue-600 hover:bg-blue-500 shadow-lg">Approve & Send Link</button></>}
                            {order.status === 'approved' && !order.startDate && <div className="w-full flex justify-between items-center">{!order.isDetailsSubmitted ? <span className="text-orange-500 font-bold text-sm bg-orange-50 px-4 py-2 rounded-lg border border-orange-200 animate-pulse">Waiting for Client Submission...</span> : <button onClick={() => handleReviewClick(order)} className="px-6 py-3 rounded-xl font-bold text-white bg-indigo-600 hover:bg-indigo-500 shadow-lg flex items-center gap-2 ml-auto"><FileText size={18}/> Review Client Details</button>}</div>}
                            {(order.startDate || order.status === 'completed') && order.status !== 'completed' && <button onClick={() => handleComplete(order.id)} className="px-6 py-3 rounded-xl font-bold text-slate-500 bg-white hover:bg-slate-50 border border-slate-200">Archive Order</button>}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            )}

            {/* ADMIN SETTINGS VIEW */}
            {activeTab === 'settings' && user?.role === 'admin' && (
              <div className="max-w-2xl mx-auto space-y-8 animate-fade-in-up">
                
                {/* Notification Test Section */}
                <div className="bg-gradient-to-br from-indigo-50 to-blue-50 rounded-3xl border border-indigo-200 p-8 shadow-sm">
                  <div className="flex items-center gap-3 mb-4 text-indigo-600">
                    <BellRing size={24} />
                    <h3 className="font-bold text-xl">{t('dashboard.notifications')}</h3>
                  </div>
                  <p className="text-sm text-slate-600 mb-6 leading-relaxed">
                    ব্রাউজার থেকে সাউন্ড এবং নোটিফিকেশন পেতে অনুমতি প্রয়োজন। নিচের বাটনে ক্লিক করে টেস্ট করুন।
                  </p>
                  <button 
                    onClick={handleTestNotification}
                    className="flex items-center gap-2 bg-indigo-600 text-white font-bold px-6 py-3 rounded-xl hover:bg-indigo-500 transition-all shadow-md active:scale-95"
                  >
                    <BellRing size={18} /> {t('dashboard.test_sound')}
                  </button>
                </div>

                <div className="bg-white rounded-3xl border border-slate-200 p-8 shadow-sm">
                  <h3 className="font-bold text-xl text-slate-900 mb-6">{t('dashboard.social_links')}</h3>
                  <div className="space-y-5">
                    <div><label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-2">Facebook Page</label><input type="text" value={settingsFb} onChange={e => setSettingsFb(e.target.value)} className="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium" /></div>
                    <div><label className="block text-xs font-bold text-slate-500 uppercase tracking-wide mb-2">YouTube Channel</label><input type="text" value={settingsYt} onChange={e => setSettingsYt(e.target.value)} className="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 bg-slate-50 text-slate-900 font-medium" /></div>
                    <button onClick={handleSaveSettings} disabled={isSavingSettings} className="w-full bg-blue-600 text-white font-bold py-4 rounded-xl hover:bg-blue-500 transition-all shadow-lg flex justify-center">{isSavingSettings ? <Loader2 className="animate-spin" /> : t('dashboard.save_changes')}</button>
                  </div>
                </div>
                
                <div className="bg-slate-900 rounded-3xl p-8 border border-slate-800 shadow-xl">
                  <div className="flex items-center gap-3 mb-6"><Terminal className="text-blue-400"/><h3 className="font-bold text-xl text-white">Deployment Helper</h3></div>
                  <div className="space-y-6">
                    <div><label className="text-slate-400 text-xs font-bold uppercase mb-2 block">1. File Content (index.ts)</label><div className="relative"><pre className="bg-black p-4 rounded-xl text-xs text-slate-300 font-mono overflow-x-auto border border-white/10 h-32">{SERVER_CODE}</pre><button onClick={() => navigator.clipboard.writeText(SERVER_CODE)} className="absolute top-2 right-2 p-2 bg-white/10 rounded hover:text-white hover:bg-white/20 transition-colors"><Copy size={14}/></button></div></div>
                    <div><label className="text-slate-400 text-xs font-bold uppercase mb-2 block">2. Deploy Command</label><div className="flex gap-2"><code className="flex-1 bg-black p-4 rounded-xl text-sm text-blue-400 font-mono border border-white/10">{DEPLOY_COMMAND}</code><button onClick={() => navigator.clipboard.writeText(DEPLOY_COMMAND)} className="px-6 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-500">Copy</button></div></div>
                  </div>
                </div>
              </div>
            )}

            {/* MESSAGES VIEW */}
            {activeTab === 'support' && (
              <div className="h-[700px] animate-fade-in-up">
                {user?.role === 'admin' && !selectedChatUser ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {chatUsers.map(u => (
                      <div key={u.id} onClick={() => handleSelectChat(u)} className="bg-white p-6 rounded-3xl border border-slate-200 shadow-sm hover:shadow-lg hover:border-blue-300 transition-all cursor-pointer group">
                        <div className="flex justify-between items-start mb-4">
                          <div className="w-12 h-12 bg-slate-100 rounded-2xl flex items-center justify-center text-slate-500 font-bold group-hover:bg-blue-600 group-hover:text-white transition-colors">{(u.name || '?').charAt(0)}</div>
                          {(unreadCounts[u.id] || 0) > 0 && <span className="bg-red-500 text-white text-xs font-bold px-3 py-1 rounded-full animate-pulse">{unreadCounts[u.id]} New</span>}
                        </div>
                        <h4 className="font-bold text-slate-900 text-lg mb-1">{u.name}</h4>
                        <p className="text-slate-500 text-sm mb-4">{u.email}</p>
                        <span className="text-blue-600 text-sm font-bold flex items-center gap-1 group-hover:gap-2 transition-all">Chat Now <ArrowRight size={16}/></span>
                      </div>
                    ))}
                    {chatUsers.length === 0 && <div className="col-span-full text-center py-20 text-slate-400"><MessageCircle size={48} className="mx-auto mb-4 opacity-30"/><p>No messages yet.</p></div>}
                  </div>
                ) : (
                  <div className="max-w-3xl mx-auto h-full flex flex-col">
                    {user?.role === 'admin' && <button onClick={() => setSelectedChatUser(null)} className="mb-4 text-slate-500 hover:text-slate-900 font-bold flex items-center gap-2 text-sm"><ArrowLeft size={18}/> Back to Inbox</button>}
                    <ChatInterface 
                      messages={user?.role === 'admin' ? messages.filter(m => m.user_id === selectedChatUser?.id) : messages} 
                      currentUserId={user?.id || ''} 
                      onSend={handleSendMessage} 
                      targetUser={user?.role === 'admin' ? selectedChatUser! : undefined} 
                    />
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </main>

      <ApproveModal isOpen={isApproveModalOpen} onClose={() => setIsApproveModalOpen(false)} order={selectedOrder} onApprove={confirmApprove} />
      <ReviewModal isOpen={isReviewModalOpen} onClose={() => setIsReviewModalOpen(false)} order={selectedOrder} onAction={confirmReviewAction} />
    </div>
  );
};

export default Dashboard;