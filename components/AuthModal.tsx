import React, { useState } from 'react';
import { useAuth } from '../AuthContext';
import { useLanguage } from '../LanguageContext';
import { trackPixelEvent } from '../services/pixelService';
import { X, Mail, User as UserIcon, Phone, ArrowRight, Loader2, ShieldCheck, Lock, LogIn, UserPlus, RefreshCw } from 'lucide-react';

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

const AuthModal: React.FC<AuthModalProps> = ({ isOpen, onClose, onSuccess }) => {
  const { signupUser, verifyEmail, loginUser, resendSignupCode, loginWithGoogle } = useAuth();
  const { t } = useLanguage();
  
  const [view, setView] = useState<'login' | 'register' | 'verify'>('login');
  const [loading, setLoading] = useState(false);
  const [googleLoading, setGoogleLoading] = useState(false);
  const [error, setError] = useState('');
  const [resendStatus, setResendStatus] = useState('');
  const [showExistingUserActions, setShowExistingUserActions] = useState(false);

  // Form Data
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');

  if (!isOpen) return null;

  const handleGoogleLogin = async () => {
    if (!loginWithGoogle) return;
    setError('');
    setGoogleLoading(true);
    const result = await loginWithGoogle();
    setGoogleLoading(false);
    if (result.success) {
      trackPixelEvent('CompleteRegistration');
      onSuccess();
      onClose();
    } else {
      setError(result.message || 'Google sign-in failed.');
    }
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    const result = await loginUser(email, password);
    setLoading(false);
    if (result.success) {
      onSuccess();
      onClose();
    } else {
      setError(result.message || 'Invalid email or password.');
    }
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setShowExistingUserActions(false);
    setLoading(true);
    const result = await signupUser({ name, email, phone, pass: password });
    setLoading(false);
    
    if (result.success) {
      trackPixelEvent('CompleteRegistration');
      onSuccess();
      onClose();
    } else {
      if (result.message && (result.message.includes("already registered") || result.message.includes("already-in-use"))) {
        setError('This email is already registered. Please login.');
        setShowExistingUserActions(true);
      } else {
        setError(result.message || 'Registration failed.');
      }
    }
  };

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    const result = await verifyEmail(email, otp);
    setLoading(false);
    if (result.success) {
      onSuccess();
      onClose();
    } else {
      setError('Invalid code.');
    }
  };

  const handleResend = async () => {
    setResendStatus('Sending...');
    const result = await resendSignupCode(email);
    if (result.success) {
      setResendStatus('Code resent! Check email.');
      if (view === 'register') {
        setView('verify');
        setError('');
        setShowExistingUserActions(false);
      }
    } else {
      setResendStatus('Failed. Please try later.');
    }
    setTimeout(() => setResendStatus(''), 6000);
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm transition-opacity duration-300">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-fade-in-up">
        
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-700 to-indigo-800 p-6 text-center text-white relative">
          <button onClick={onClose} className="absolute top-4 right-4 text-white/80 hover:text-white transition-colors">
            <X size={24} />
          </button>
          <div className="bg-white/20 w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-3 backdrop-blur-sm">
             {view === 'login' && <LogIn size={24} />}
             {view === 'register' && <UserPlus size={24} />}
             {view === 'verify' && <ShieldCheck size={24} />}
          </div>
          <h2 className="text-2xl font-bold mb-1">
            {view === 'login' ? t('auth.login_title') : view === 'register' ? t('auth.register_title') : t('auth.verify_title')}
          </h2>
          <p className="text-blue-100 text-xs">
            {view === 'verify' 
              ? `${t('auth.verify_subtitle')} ${email}`
              : t('auth.login_subtitle')}
          </p>
        </div>

        <div className="p-8">
          {error && <div className="mb-4 p-3 bg-red-50 border border-red-100 text-red-600 text-xs rounded-lg font-medium text-center">{error}</div>}
          
          {/* Google 1-Click Button */}
          {view !== 'verify' && (
            <div className="mb-5">
              <button 
                type="button"
                onClick={handleGoogleLogin}
                disabled={googleLoading || loading}
                className="w-full bg-white hover:bg-slate-50 border border-slate-300 text-slate-700 font-semibold py-3 px-4 rounded-xl shadow-sm hover:shadow transition-all flex items-center justify-center gap-3 active:scale-[0.99]"
              >
                {googleLoading ? (
                  <Loader2 size={18} className="animate-spin text-blue-600" />
                ) : (
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z" />
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z" />
                  </svg>
                )}
                <span className="text-sm">
                  {view === 'login' ? 'Google দিয়ে সরাসরি লগইন করুন' : 'Google দিয়ে সরাসরি একাউন্ট খুলুন'}
                </span>
              </button>

              <div className="flex items-center my-4">
                <div className="flex-1 border-t border-slate-200"></div>
                <span className="px-3 text-xs text-slate-400 font-medium">অথবা ইমেইল দিয়ে</span>
                <div className="flex-1 border-t border-slate-200"></div>
              </div>
            </div>
          )}

          {/* Actions for Existing Users */}
          {showExistingUserActions && view === 'register' && (
             <div className="mb-6 p-4 bg-blue-50 rounded-xl border border-blue-100">
               <p className="text-xs text-blue-800 mb-3 text-center">{t('auth.existing_user')}</p>
               <div className="flex flex-col gap-2">
                 <button 
                    type="button"
                    onClick={() => setView('login')}
                    className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
                 >
                   <LogIn size={16} /> {t('auth.goto_login')}
                 </button>
               </div>
             </div>
          )}

          {/* LOGIN VIEW */}
          {view === 'login' && (
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="relative">
                <Mail className="absolute left-3 top-3.5 text-gray-500" size={18} />
                <input 
                  type="email" 
                  placeholder={t('auth.placeholder_email')}
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-gray-500 outline-none text-sm shadow-sm"
                  required
                />
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-3.5 text-gray-500" size={18} />
                <input 
                  type="password" 
                  placeholder={t('auth.placeholder_pass')}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-gray-500 outline-none text-sm shadow-sm"
                  required
                />
              </div>
              <button 
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white font-bold py-3.5 rounded-xl hover:bg-blue-700 transition-colors shadow-lg flex justify-center items-center gap-2"
              >
                {loading ? <Loader2 size={18} className="animate-spin" /> : t('auth.btn_login')}
              </button>
              
              <div className="text-center text-sm text-gray-500 mt-4">
                {t('auth.no_account')} <button type="button" onClick={() => { setView('register'); setError(''); }} className="text-blue-600 font-bold hover:underline">{t('auth.btn_signup')}</button>
              </div>
            </form>
          )}

          {/* REGISTER VIEW */}
          {view === 'register' && (
            <form onSubmit={handleSignup} className="space-y-4">
              <div className="relative">
                <UserIcon className="absolute left-3 top-3.5 text-gray-500" size={18} />
                <input 
                  type="text" 
                  placeholder={t('auth.placeholder_name')}
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-gray-500 outline-none text-sm shadow-sm"
                  required
                />
              </div>
              <div className="relative">
                <Phone className="absolute left-3 top-3.5 text-gray-500" size={18} />
                <input 
                  type="tel" 
                  placeholder={t('auth.placeholder_phone')}
                  value={phone}
                  onChange={e => setPhone(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-gray-500 outline-none text-sm shadow-sm"
                  required
                />
              </div>
              <div className="relative">
                <Mail className="absolute left-3 top-3.5 text-gray-500" size={18} />
                <input 
                  type="email" 
                  placeholder={t('auth.placeholder_email')}
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-gray-500 outline-none text-sm shadow-sm"
                  required
                />
              </div>
              <div className="relative">
                <Lock className="absolute left-3 top-3.5 text-gray-500" size={18} />
                <input 
                  type="password" 
                  placeholder={t('auth.placeholder_pass_min')}
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-gray-500 outline-none text-sm shadow-sm"
                  required
                  minLength={6}
                />
              </div>
              <button 
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white font-bold py-3.5 rounded-xl hover:bg-blue-700 transition-colors shadow-lg flex justify-center items-center gap-2"
              >
                {loading ? <Loader2 size={18} className="animate-spin" /> : <>{t('auth.btn_signup')} <ArrowRight size={18} /></>}
              </button>

              <div className="text-center text-sm text-gray-500 mt-4">
                {t('auth.have_account')} <button type="button" onClick={() => { setView('login'); setError(''); }} className="text-blue-600 font-bold hover:underline">{t('auth.btn_login')}</button>
              </div>
            </form>
          )}

          {/* VERIFY VIEW */}
          {view === 'verify' && (
            <div className="space-y-4">
              <form onSubmit={handleVerify} className="space-y-4">
                <div className="text-center">
                  <input 
                    type="text" 
                    placeholder="12345678"
                    value={otp}
                    onChange={e => setOtp(e.target.value)}
                    className="w-full mx-auto text-center text-2xl tracking-[0.2em] font-bold px-4 py-3 border-2 border-blue-200 bg-white text-slate-900 rounded-xl focus:border-blue-500 outline-none"
                    maxLength={8}
                    autoFocus
                  />
                  <p className="text-xs text-gray-500 mt-2">{t('auth.verify_subtitle')}</p>
                </div>
                <button 
                  type="submit"
                  disabled={loading}
                  className="w-full bg-blue-600 text-white font-bold py-3.5 rounded-xl hover:bg-blue-700 transition-colors shadow-lg flex justify-center items-center gap-2"
                >
                  {loading ? <Loader2 size={18} className="animate-spin" /> : t('auth.btn_verify')}
                </button>
              </form>

              <div className="text-center pt-2">
                <button 
                  type="button"
                  onClick={handleResend}
                  className="flex items-center justify-center gap-2 mx-auto text-sm font-medium text-gray-600 hover:text-blue-600 transition-colors"
                >
                  <RefreshCw size={14} /> {t('auth.resend')}
                </button>
                {resendStatus && <p className="text-xs text-green-600 mt-1 animate-pulse">{resendStatus}</p>}
              </div>

              <button 
                type="button" 
                onClick={() => setView('register')}
                className="w-full text-center text-sm text-gray-500 hover:text-gray-700 mt-2"
              >
                {t('auth.wrong_email')}
              </button>
            </div>
          )}

        </div>
      </div>
    </div>
  );
};

export default AuthModal;