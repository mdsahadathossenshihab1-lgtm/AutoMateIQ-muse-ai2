
import React, { useState, useRef, useEffect } from 'react';
import { getServices, getPricingPackages, getApiTopupPackage, CONTACT_INFO, getServiceDetails } from './constants';
import ServiceCard from './components/ServiceCard';
import PricingCard from './components/PricingCard';
import ChatAssistant from './components/ChatAssistant';
import OrderModal from './components/OrderModal';
import ServiceDetailModal from './components/ServiceDetailModal';
import Dashboard from './components/Dashboard';
import AuthModal from './components/AuthModal';
import ApiTopupSection from './components/ApiTopupSection';
import AiFeaturesSection from './components/AiFeaturesSection';
import Logo from './components/Logo';
import { AuthProvider, useAuth } from './AuthContext';
import { LanguageProvider, useLanguage } from './LanguageContext';
import { PricingPackage, ServiceDetailContent } from './types';
import { Facebook, Youtube, Mail, Phone, LayoutDashboard, LogIn, LogOut, Menu, Globe, ArrowRight, X, MapPin, Zap, Bot, Database, Globe as GlobeIcon, Code, CheckCircle2, FileCode, Cpu, MessageSquare, Share2, Activity, ShieldCheck, Play, Sparkles, Send, TrendingUp, Users } from 'lucide-react';
import { trackPixelEvent } from './services/pixelService';
import { getUserLocation } from './services/locationService';
import { db } from './services/supabaseClient';

// Inner Component to use Language Hook and Auth
const AppContent: React.FC = () => {
  const { user, isLoading, sendSupportMessage, siteSettings, logout } = useAuth();
  const { language, toggleLanguage, t } = useLanguage();
  
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [isDashboardOpen, setIsDashboardOpen] = useState(false);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  
  // Contact Form
  const [contactName, setContactName] = useState('');
  const [contactPhone, setContactPhone] = useState('');
  const [contactMsg, setContactMsg] = useState('');
  const [isSendingMsg, setIsSendingMsg] = useState(false);
  
  const [selectedPackageId, setSelectedPackageId] = useState<string | null>(null);
  const [selectedServiceId, setSelectedServiceId] = useState<string | null>(null);
  const [customAmount, setCustomAmount] = useState<number | null>(null);

  const pricingSectionRef = useRef<HTMLElement>(null);
  const contactSectionRef = useRef<HTMLElement>(null);

  // Dynamic Data based on Language
  const services = getServices(language);
  const packages = getPricingPackages(language);
  const apiTopupPkg = getApiTopupPackage(language);

  // Location Sync
  useEffect(() => {
    const initLocation = async () => {
      const location = await getUserLocation();
      if (user && (!user.location || user.location === 'Unknown')) {
         if (location && location !== 'Unknown Location') {
             await db.upsertProfile({ ...user, location });
         }
      }
    };
    if (user) {
      setTimeout(initLocation, 1000);
    }
  }, [user?.id]);

  useEffect(() => {
    if (!user) setIsDashboardOpen(false);
  }, [user]);

  const handleLoginClick = () => {
    setSelectedPackageId(null);
    setIsAuthModalOpen(true);
  };

  const handleOrderClick = (pkg: PricingPackage) => {
    setSelectedPackageId(pkg.id);
    if (!user) {
      setIsAuthModalOpen(true);
    } else {
      setIsOrderModalOpen(true);
    }
  };

  const handleCustomApiOrder = (amount: number) => {
    setCustomAmount(amount);
    setSelectedPackageId(apiTopupPkg.id);
    if (!user) {
      setIsAuthModalOpen(true);
    } else {
      setIsOrderModalOpen(true);
    }
  };

  const handleAuthSuccess = () => {
    setIsAuthModalOpen(false);
    if (selectedPackageId) {
       setIsOrderModalOpen(true);
    } else {
       setIsDashboardOpen(true);
    }
  };

  const handleOrderSuccess = () => {
    setIsOrderModalOpen(false);
    setIsDashboardOpen(true);
    setCustomAmount(null);
  };

  const handleContactSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!contactMsg.trim()) return;
    if (!user) {
      alert(t('contact.login_note'));
      setIsAuthModalOpen(true);
      return;
    }
    setIsSendingMsg(true);
    const fullMessage = `Name: ${contactName}, Phone: ${contactPhone}\n\nMessage: ${contactMsg}`;
    const success = await sendSupportMessage(fullMessage);
    if (success) {
      trackPixelEvent('Contact');
      alert("Message sent successfully!");
      setContactMsg('');
      setContactName('');
      setContactPhone('');
      setIsDashboardOpen(true);
    } else {
      alert("Failed to send message.");
    }
    setIsSendingMsg(false);
  };

  const handleViewDetails = (pkgId: string) => {
    const serviceId = pkgId.replace('-pkg', '');
    setSelectedServiceId(serviceId);
    setIsDetailModalOpen(true);
  };

  const handleServiceClick = (serviceId: string) => {
    if (serviceId === 'coming-soon') return; 
    setSelectedServiceId(serviceId);
    setIsDetailModalOpen(true);
  };

  const scrollToPricing = () => {
    pricingSectionRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const scrollToContact = () => {
    contactSectionRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  if (isDashboardOpen && user) {
    return <Dashboard onClose={() => setIsDashboardOpen(false)} onNavigateToPricing={() => { setIsDashboardOpen(false); setTimeout(scrollToPricing, 100); }} />;
  }

  return (
    <div className="min-h-screen font-sans text-slate-900 selection:bg-blue-100 selection:text-blue-900">
      
      {/* Floating Navbar */}
      <div className="fixed top-4 left-0 w-full z-50 px-4 flex justify-center">
        <nav className="w-full max-w-7xl bg-white/70 backdrop-blur-xl border border-white/50 shadow-[0_8px_30px_rgb(0,0,0,0.04)] rounded-full px-6 py-3 transition-all duration-300">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-2 cursor-pointer hover:scale-105 transition-transform" onClick={() => window.scrollTo(0,0)}>
              <Logo />
            </div>

            {/* Desktop Menu */}
            <div className="hidden md:flex items-center gap-1">
              {['home', 'services', 'pricing', 'api_credit', 'contact'].map((item) => (
                <button 
                  key={item}
                  onClick={() => {
                    if (item === 'home') window.scrollTo(0,0);
                    else if (item === 'pricing') scrollToPricing();
                    else if (item === 'contact') scrollToContact();
                    else document.getElementById(item === 'api_credit' ? 'api-topup' : item)?.scrollIntoView({ behavior: 'smooth' });
                  }} 
                  className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-blue-600 hover:bg-blue-50/50 rounded-full transition-all"
                >
                  {t(`nav.${item}`)}
                </button>
              ))}
            </div>

            <div className="hidden md:flex items-center gap-3">
              <button onClick={toggleLanguage} className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-bold bg-white/50 border border-slate-200 text-slate-700 hover:bg-white transition-colors">
                <Globe size={14} />
                {language.toUpperCase()}
              </button>

              {user ? (
                 <div className="flex items-center gap-2 pl-2 border-l border-slate-200">
                    <button 
                      onClick={() => setIsDashboardOpen(true)}
                      className="bg-slate-900 hover:bg-blue-600 text-white px-5 py-2 rounded-full text-sm font-bold transition-all shadow-md flex items-center gap-2"
                    >
                      <LayoutDashboard size={14} /> {t('nav.dashboard')}
                    </button>
                 </div>
              ) : (
                <button 
                  onClick={handleLoginClick}
                  className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-full text-sm font-bold transition-all flex items-center gap-2 shadow-lg shadow-blue-500/20"
                >
                  <LogIn size={16} /> {t('nav.login')}
                </button>
              )}
            </div>

            {/* Mobile Menu Button */}
            <div className="md:hidden flex items-center gap-3">
              <button onClick={toggleLanguage} className="text-xs font-bold bg-slate-100 px-2 py-1 rounded-md">{language.toUpperCase()}</button>
              <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="text-slate-800 p-1">
                {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
              </button>
            </div>
          </div>
        </nav>
      </div>

      {/* Mobile Menu Dropdown */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 z-40 bg-white/95 backdrop-blur-lg pt-24 px-6 animate-fade-in-up md:hidden">
          <div className="flex flex-col gap-4">
             {['home', 'services', 'pricing', 'api_credit', 'contact'].map((item) => (
                <button 
                  key={item}
                  onClick={() => {
                    setMobileMenuOpen(false);
                    if (item === 'home') window.scrollTo(0,0);
                    else if (item === 'pricing') scrollToPricing();
                    else if (item === 'contact') scrollToContact();
                    else document.getElementById(item === 'api_credit' ? 'api-topup' : item)?.scrollIntoView({ behavior: 'smooth' });
                  }} 
                  className="text-2xl font-bold text-slate-800 text-left py-2 border-b border-slate-100/50"
                >
                  {t(`nav.${item}`)}
                </button>
              ))}
              
              <div className="mt-8">
              {user ? (
                <button onClick={() => setIsDashboardOpen(true)} className="w-full bg-blue-600 text-white px-4 py-4 rounded-xl font-bold text-lg flex items-center justify-center gap-2 mb-4">
                  <LayoutDashboard size={20} /> {t('nav.dashboard')}
                </button>
              ) : (
                <button onClick={() => { setMobileMenuOpen(false); handleLoginClick(); }} className="w-full bg-slate-900 text-white px-4 py-4 rounded-xl font-bold text-lg flex items-center justify-center gap-2">
                  <LogIn size={20} /> {t('nav.login')}
                </button>
              )}
              </div>
          </div>
        </div>
      )}

      {/* NEW Centered Hero Section (Modern Glassmorphic Hub) */}
      <section className="relative pt-48 pb-24 px-4 overflow-hidden">
        <div className="max-w-7xl mx-auto text-center relative z-10">
           
           {/* Badge */}
           <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/80 backdrop-blur-sm border border-blue-100 shadow-sm mb-8 animate-fade-in-up">
              <Sparkles size={14} className="text-blue-500 fill-blue-500" />
              <span className="text-xs font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent uppercase tracking-wider">
                 AI Powered Automation v2.0
              </span>
           </div>

           {/* Headline */}
           <h1 className="text-5xl md:text-7xl font-extrabold tracking-tight text-slate-900 mb-8 leading-[1.1] animate-fade-in-up animation-delay-100 max-w-4xl mx-auto drop-shadow-sm">
              {t('hero.title_start')} <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 via-indigo-500 to-purple-500">
                {t('hero.title_highlight')}
              </span>
           </h1>

           {/* Subtitle */}
           <p className="text-lg md:text-xl text-slate-600 mb-10 max-w-2xl mx-auto leading-relaxed animate-fade-in-up animation-delay-200">
              {t('hero.subtitle')}
           </p>

           {/* Buttons */}
           <div className="flex flex-col sm:flex-row gap-4 justify-center mb-20 animate-fade-in-up animation-delay-300">
             <button 
               onClick={scrollToPricing}
               className="px-8 py-4 bg-slate-900 hover:bg-slate-800 text-white font-bold rounded-2xl text-lg transition-all transform hover:-translate-y-1 shadow-[0_20px_40px_-15px_rgba(0,0,0,0.3)] flex items-center justify-center gap-3"
             >
               {t('hero.btn_start')} <ArrowRight size={20} />
             </button>
             <button 
               onClick={() => document.getElementById('services')?.scrollIntoView({ behavior: 'smooth' })}
               className="px-8 py-4 bg-white/60 backdrop-blur-md border border-white/60 text-slate-700 font-bold rounded-2xl text-lg transition-all hover:bg-white flex items-center justify-center gap-2 shadow-sm"
             >
               <Share2 size={18} />
               {t('hero.btn_services')}
             </button>
           </div>

           {/* Modern Glassmorphic Hub Visual */}
           <div className="relative mx-auto max-w-6xl animate-fade-in-up animation-delay-300 perspective-1000">
              {/* Animated Glow Blobs behind the card (Reinforced) */}
              <div className="absolute -top-20 -left-20 w-72 h-72 bg-purple-300 rounded-full mix-blend-multiply filter blur-3xl opacity-50 animate-blob"></div>
              <div className="absolute -top-20 -right-20 w-72 h-72 bg-blue-300 rounded-full mix-blend-multiply filter blur-3xl opacity-50 animate-blob animation-delay-2000"></div>
              <div className="absolute -bottom-32 left-20 w-72 h-72 bg-indigo-300 rounded-full mix-blend-multiply filter blur-3xl opacity-50 animate-blob animation-delay-4000"></div>

              {/* Main Glass Panel */}
              <div className="relative bg-white/40 backdrop-blur-xl border border-white/50 rounded-[2.5rem] shadow-2xl p-6 md:p-10 transform md:rotate-x-6 hover:rotate-x-0 transition-transform duration-700 ease-out z-10">
                 
                 {/* Top Stats Row */}
                 <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                    {/* Stat Card 1 */}
                    <div className="bg-white/60 backdrop-blur-md rounded-2xl p-5 border border-white/40 shadow-sm flex items-center gap-4">
                       <div className="w-12 h-12 bg-green-50 rounded-xl flex items-center justify-center text-green-600">
                          <TrendingUp size={24} />
                       </div>
                       <div className="text-left">
                          <p className="text-xs font-bold text-slate-500 uppercase">Revenue Boost</p>
                          <p className="text-xl font-bold text-slate-900">+32%</p>
                       </div>
                    </div>
                    {/* Stat Card 2 */}
                    <div className="bg-white/60 backdrop-blur-md rounded-2xl p-5 border border-white/40 shadow-sm flex items-center gap-4">
                       <div className="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600">
                          <MessageSquare size={24} />
                       </div>
                       <div className="text-left">
                          <p className="text-xs font-bold text-slate-500 uppercase">Auto Replies</p>
                          <p className="text-xl font-bold text-slate-900">24/7 Active</p>
                       </div>
                    </div>
                    {/* Stat Card 3 */}
                    <div className="bg-white/60 backdrop-blur-md rounded-2xl p-5 border border-white/40 shadow-sm flex items-center gap-4">
                       <div className="w-12 h-12 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600">
                          <Users size={24} />
                       </div>
                       <div className="text-left">
                          <p className="text-xs font-bold text-slate-500 uppercase">Leads Captured</p>
                          <p className="text-xl font-bold text-slate-900">1,240+</p>
                       </div>
                    </div>
                 </div>

                 {/* Main Visualization Area */}
                 <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                    {/* Left: Chat Simulation */}
                    <div className="lg:col-span-2 bg-slate-50/50 backdrop-blur-sm rounded-3xl p-6 border border-white/60 shadow-inner relative overflow-hidden h-64 md:h-80 flex flex-col">
                       <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-400 to-indigo-500"></div>
                       <div className="flex items-center gap-3 mb-6">
                          <div className="w-3 h-3 rounded-full bg-red-400"></div>
                          <div className="w-3 h-3 rounded-full bg-yellow-400"></div>
                          <div className="w-3 h-3 rounded-full bg-green-400"></div>
                          <span className="text-xs font-bold text-slate-400 ml-2">Live Messenger Bot</span>
                       </div>

                       <div className="space-y-4 flex-1 overflow-hidden">
                          {/* Message 1 */}
                          <div className="flex gap-3 animate-fade-in-up">
                             <div className="w-8 h-8 rounded-full bg-slate-200"></div>
                             <div className="bg-white/80 backdrop-blur-sm p-3 rounded-2xl rounded-tl-none text-sm text-slate-600 shadow-sm border border-slate-100 max-w-[80%]">
                                Hi, is the Premium package available?
                             </div>
                          </div>
                          {/* Message 2 (Bot) */}
                          <div className="flex gap-3 flex-row-reverse animate-fade-in-up animation-delay-1000">
                             <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center text-white"><Bot size={16}/></div>
                             <div className="bg-blue-600 p-3 rounded-2xl rounded-tr-none text-sm text-white shadow-md max-w-[80%]">
                                Yes! The Premium package is available for just 1000 BDT. Would you like to order?
                             </div>
                          </div>
                           {/* Message 3 (User) */}
                           <div className="flex gap-3 animate-fade-in-up animation-delay-2000">
                             <div className="w-8 h-8 rounded-full bg-slate-200"></div>
                             <div className="bg-white/80 backdrop-blur-sm p-3 rounded-2xl rounded-tl-none text-sm text-slate-600 shadow-sm border border-slate-100 max-w-[80%]">
                                Yes, please send details.
                             </div>
                          </div>
                       </div>
                       
                       {/* Typing Indicator */}
                       <div className="absolute bottom-6 left-16 flex gap-1">
                          <div className="w-2 h-2 bg-slate-300 rounded-full animate-bounce"></div>
                          <div className="w-2 h-2 bg-slate-300 rounded-full animate-bounce animation-delay-100"></div>
                          <div className="w-2 h-2 bg-slate-300 rounded-full animate-bounce animation-delay-200"></div>
                       </div>
                    </div>

                    {/* Right: Integration Hub */}
                    <div className="bg-slate-900/90 backdrop-blur-md rounded-3xl p-6 text-white relative overflow-hidden flex flex-col justify-between shadow-xl border border-slate-700/50">
                       <div className="absolute top-0 right-0 w-32 h-32 bg-blue-500 rounded-full filter blur-[60px] opacity-30"></div>
                       
                       <div>
                          <div className="flex items-center gap-2 mb-6">
                             <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></div>
                             <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">System Active</span>
                          </div>
                          <h3 className="text-xl font-bold mb-1">Integrations</h3>
                          <p className="text-slate-400 text-sm">Real-time data flow</p>
                       </div>

                       <div className="space-y-4 mt-8">
                          <div className="flex items-center justify-between p-3 bg-white/10 rounded-xl border border-white/5 hover:bg-white/20 transition-colors">
                             <div className="flex items-center gap-3">
                                <Facebook size={18} className="text-blue-400" />
                                <span className="text-sm font-medium">Facebook</span>
                             </div>
                             <CheckCircle2 size={16} className="text-green-400" />
                          </div>
                          <div className="flex items-center justify-between p-3 bg-white/10 rounded-xl border border-white/5 hover:bg-white/20 transition-colors">
                             <div className="flex items-center gap-3">
                                <MessageSquare size={18} className="text-green-400" />
                                <span className="text-sm font-medium">WhatsApp</span>
                             </div>
                             <CheckCircle2 size={16} className="text-green-400" />
                          </div>
                          <div className="flex items-center justify-between p-3 bg-white/10 rounded-xl border border-white/5 hover:bg-white/20 transition-colors">
                             <div className="flex items-center gap-3">
                                <Database size={18} className="text-purple-400" />
                                <span className="text-sm font-medium">Sheets</span>
                             </div>
                             <CheckCircle2 size={16} className="text-green-400" />
                          </div>
                       </div>
                    </div>
                 </div>

                 {/* Floating Badges */}
                 <div className="absolute -top-6 -right-6 bg-white/90 backdrop-blur-sm p-3 rounded-xl shadow-xl border border-slate-100 animate-float hidden md:flex items-center gap-3 z-20">
                    <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center text-green-600">
                       <Zap size={20} />
                    </div>
                    <div>
                       <p className="text-[10px] font-bold text-slate-400 uppercase">Response Time</p>
                       <p className="font-bold text-slate-800">0.5 Seconds</p>
                    </div>
                 </div>

                 <div className="absolute bottom-10 -left-8 bg-slate-900/90 backdrop-blur-sm p-3 rounded-xl shadow-xl animate-float-delayed hidden md:flex items-center gap-3 z-20 border border-slate-700">
                    <div className="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
                    <p className="font-mono text-xs text-white">Bot is typing...</p>
                 </div>

              </div>
           </div>

        </div>
      </section>

      {/* NEW AI Features Section */}
      <AiFeaturesSection />

      {/* Services Section */}
      <section id="services" className="py-24 px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold text-slate-900 mb-4 inline-block tracking-tight">
              {t('services.title')}
            </h2>
            <p className="text-slate-500 text-lg max-w-2xl mx-auto leading-relaxed">{t('services.subtitle')}</p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {services.map((service, idx) => (
              <div key={service.id} className={`animate-fade-in-up animation-delay-${idx * 100}`}>
                <ServiceCard 
                  service={service} 
                  onClick={() => handleServiceClick(service.id)} 
                />
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Pricing Section (Updated: Removed solid bg) */}
      <section ref={pricingSectionRef} className="py-24 px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="max-w-7xl mx-auto">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-5xl font-bold text-slate-900 mb-4 inline-block tracking-tight">
               {t('pricing.title')}
            </h2>
            <p className="text-slate-500 text-lg max-w-2xl mx-auto leading-relaxed">{t('pricing.subtitle')}</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-6xl mx-auto">
            {packages.map((pkg, idx) => (
              <div key={pkg.id} className={`animate-fade-in-up animation-delay-${idx * 100} h-full`}>
                <PricingCard 
                  pkg={pkg} 
                  onOrder={handleOrderClick}
                  onViewDetails={handleViewDetails}
                />
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* API Topup Section */}
      <section id="api-topup" className="py-24 px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="max-w-6xl mx-auto animate-fade-in-up">
           <ApiTopupSection 
             onOrder={handleCustomApiOrder}
             onViewDetails={() => handleViewDetails('whatsapp')}
           />
        </div>
      </section>

      {/* Contact Section */}
      <section ref={contactSectionRef} className="py-24 px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="max-w-5xl mx-auto">
          <div className="bg-white/60 backdrop-blur-xl border border-white/50 rounded-[2.5rem] p-8 md:p-12 shadow-[0_20px_60px_-15px_rgba(0,0,0,0.05)] relative overflow-hidden group">
             
             {/* Glows */}
             <div className="absolute top-0 right-0 w-96 h-96 bg-blue-100 rounded-full blur-[100px] opacity-60 pointer-events-none"></div>
             
             <div className="relative z-10 flex flex-col md:flex-row gap-12">
                <div className="md:w-1/2">
                   <h2 className="text-3xl md:text-4xl font-bold text-slate-900 mb-6 leading-tight">
                    {t('contact.title')} <br/> <span className="text-blue-600">{t('contact.title_highlight')}</span>
                   </h2>
                   <p className="text-slate-500 mb-10 leading-relaxed">{t('contact.subtitle')}</p>

                   <div className="space-y-6">
                      <div className="flex items-center gap-4 group">
                         <div className="w-12 h-12 rounded-2xl bg-blue-50/50 flex items-center justify-center text-blue-600 group-hover:scale-110 transition-transform">
                           <Phone size={20} />
                         </div>
                         <div>
                           <p className="text-xs font-bold text-slate-400 uppercase tracking-wide">Hotline / WhatsApp</p>
                           <p className="text-lg font-bold text-slate-900">{CONTACT_INFO.phone}</p>
                         </div>
                      </div>
                      <div className="flex items-center gap-4 group">
                         <div className="w-12 h-12 rounded-2xl bg-indigo-50/50 flex items-center justify-center text-indigo-600 group-hover:scale-110 transition-transform">
                           <Mail size={20} />
                         </div>
                         <div>
                           <p className="text-xs font-bold text-slate-400 uppercase tracking-wide">Email</p>
                           <p className="text-lg font-bold text-slate-900">{CONTACT_INFO.email}</p>
                         </div>
                      </div>
                      <div className="flex items-center gap-4 group">
                         <div className="w-12 h-12 rounded-2xl bg-purple-50/50 flex items-center justify-center text-purple-600 group-hover:scale-110 transition-transform">
                           <MapPin size={20} />
                         </div>
                         <div>
                           <p className="text-xs font-bold text-slate-400 uppercase tracking-wide">Location</p>
                           <p className="text-lg font-bold text-slate-900">{CONTACT_INFO.address}</p>
                         </div>
                      </div>
                   </div>
                </div>

                <div className="md:w-1/2 bg-white/50 backdrop-blur-md rounded-3xl p-8 border border-white/60 shadow-inner">
                   <form onSubmit={handleContactSubmit} className="space-y-4">
                      <div>
                        <label className="text-xs font-bold text-slate-500 uppercase ml-1 mb-1 block">Your Name</label>
                        <input 
                          type="text" 
                          value={contactName}
                          onChange={e => setContactName(e.target.value)}
                          className="w-full bg-white/80 border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all font-medium placeholder-slate-400"
                          required
                        />
                      </div>
                      <div>
                        <label className="text-xs font-bold text-slate-500 uppercase ml-1 mb-1 block">Phone Number</label>
                        <input 
                          type="tel" 
                          value={contactPhone}
                          onChange={e => setContactPhone(e.target.value)}
                          className="w-full bg-white/80 border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all font-medium placeholder-slate-400"
                          required
                        />
                      </div>
                      <div>
                        <label className="text-xs font-bold text-slate-500 uppercase ml-1 mb-1 block">Message</label>
                        <textarea 
                          rows={4}
                          value={contactMsg}
                          onChange={e => setContactMsg(e.target.value)}
                          className="w-full bg-white/80 border border-slate-200 rounded-xl px-4 py-3 text-slate-900 focus:outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100 transition-all resize-none font-medium placeholder-slate-400"
                          required
                        />
                      </div>
                      <button 
                         type="submit" 
                         disabled={isSendingMsg}
                         className="w-full bg-slate-900 text-white font-bold py-4 rounded-xl transition-all hover:bg-slate-800 shadow-lg flex justify-center gap-2 items-center"
                      >
                        {isSendingMsg ? 'Sending...' : <>{t('contact.btn_send')} <Send size={18}/></>}
                      </button>
                      {!user && <p className="text-xs text-center text-slate-400">{t('contact.login_note')}</p>}
                   </form>
                </div>
             </div>
          </div>
        </div>
      </section>

      {/* Modern Footer */}
      <footer className="bg-slate-900/95 backdrop-blur-xl text-white pt-24 pb-12 relative overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-slate-700 to-transparent"></div>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          
          <div className="flex flex-col md:flex-row justify-between items-center mb-16 gap-8 text-center md:text-left">
             <div>
                <h3 className="text-3xl font-bold mb-2">Ready to scale?</h3>
                <p className="text-slate-400">Start your automation journey today.</p>
             </div>
             <button onClick={scrollToPricing} className="px-8 py-3 bg-blue-600 hover:bg-blue-500 text-white font-bold rounded-full transition-all">
                Get Started Now
             </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-12 border-t border-slate-800 pt-16">
            <div className="col-span-1 md:col-span-1">
              <div className="flex items-center gap-2 font-bold text-2xl mb-6">
                 <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
                    <Zap size={20} className="fill-white" />
                 </div>
                 AutoMateIQ
              </div>
              <p className="text-slate-400 text-sm leading-relaxed mb-6">
                {t('footer.desc')}
              </p>
              <div className="flex gap-4">
                 <a href={siteSettings.facebook || CONTACT_INFO.facebookPage} target="_blank" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 hover:text-white hover:bg-blue-600 transition-all"><Facebook size={18}/></a>
                 <a href={siteSettings.youtube || CONTACT_INFO.youtubeChannel} target="_blank" className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center text-slate-400 hover:text-white hover:bg-red-600 transition-all"><Youtube size={18}/></a>
              </div>
            </div>
            
            <div className="md:col-start-3">
              <h4 className="font-bold text-white mb-6 uppercase tracking-wider text-xs">{t('footer.quick_links')}</h4>
              <ul className="space-y-3">
                <li><button onClick={() => window.scrollTo(0,0)} className="text-slate-400 hover:text-blue-400 text-sm transition-colors">{t('nav.home')}</button></li>
                <li><button onClick={() => document.getElementById('services')?.scrollIntoView()} className="text-slate-400 hover:text-blue-400 text-sm transition-colors">{t('nav.services')}</button></li>
                <li><button onClick={scrollToPricing} className="text-slate-400 hover:text-blue-400 text-sm transition-colors">{t('nav.pricing')}</button></li>
              </ul>
            </div>

            <div>
              <h4 className="font-bold text-white mb-6 uppercase tracking-wider text-xs">Legal</h4>
              <ul className="space-y-3">
                <li><a href="#" className="text-slate-400 hover:text-blue-400 text-sm transition-colors">Privacy Policy</a></li>
                <li><a href="#" className="text-slate-400 hover:text-blue-400 text-sm transition-colors">Terms of Service</a></li>
                <li><a href="#" className="text-slate-400 hover:text-blue-400 text-sm transition-colors">Cookie Policy</a></li>
              </ul>
            </div>
          </div>
          
          <div className="border-t border-slate-800 mt-16 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-slate-500 text-sm">
              &copy; {new Date().getFullYear()} AutoMateIQ. {t('footer.copyright')}
            </p>
            <p className="text-slate-600 text-xs flex items-center gap-1">
              Built with ❤️ by 
              <a 
                href="https://www.shshihab.website/" 
                target="_blank" 
                rel="noopener noreferrer" 
                className="hover:text-blue-500 transition-colors font-bold"
              >
                MD SH SHIHAB
              </a>
            </p>
          </div>
        </div>
      </footer>

      {/* Modals */}
      <AuthModal isOpen={isAuthModalOpen} onClose={() => setIsAuthModalOpen(false)} onSuccess={handleAuthSuccess} />
      <OrderModal isOpen={isOrderModalOpen} onClose={() => setIsOrderModalOpen(false)} pkgId={selectedPackageId} customAmount={customAmount} onSuccess={handleOrderSuccess} />
      <ServiceDetailModal isOpen={isDetailModalOpen} onClose={() => setIsDetailModalOpen(false)} serviceId={selectedServiceId} />
      <ChatAssistant />
    </div>
  );
};

const App: React.FC = () => {
  return (
    <AuthProvider>
      <LanguageProvider>
        <AppContent />
      </LanguageProvider>
    </AuthProvider>
  );
};

export default App;
