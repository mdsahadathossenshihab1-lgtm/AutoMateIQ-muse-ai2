import React, { useState } from 'react';
import { Coins, Zap, ShieldCheck, AlertCircle, Wallet } from 'lucide-react';
import { useLanguage } from '../LanguageContext';

interface ApiTopupSectionProps {
  onOrder: (amount: number) => void;
  onViewDetails: () => void;
}

const ApiTopupSection: React.FC<ApiTopupSectionProps> = ({ onOrder, onViewDetails }) => {
  const { t } = useLanguage();
  const [amount, setAmount] = useState<string>('200');
  const [error, setError] = useState<string>('');

  const QUICK_AMOUNTS = [200, 500, 1000, 2500];

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setAmount(val);
    
    const num = parseInt(val);
    if (!val) {
      setError('');
      return;
    }
    
    if (isNaN(num)) {
      setError(t('topup.error_nan'));
    } else if (num < 200) {
      setError(t('topup.error_min'));
    } else if (num % 10 !== 0) {
      setError(t('topup.error_multiple'));
    } else {
      setError('');
    }
  };

  const handleSubmit = () => {
    const num = parseInt(amount);
    if (!num || num < 200 || num % 10 !== 0) {
      setError(t('topup.error_min'));
      return;
    }
    onOrder(num);
  };

  return (
    <div className="bg-white/60 backdrop-blur-xl rounded-3xl overflow-hidden shadow-2xl border border-white/50 relative">
      {/* Background Shapes */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-blue-100 rounded-full blur-[80px] pointer-events-none opacity-40"></div>
      <div className="absolute bottom-0 left-0 w-96 h-96 bg-indigo-100 rounded-full blur-[80px] pointer-events-none opacity-40"></div>

      <div className="flex flex-col lg:flex-row relative z-10">
        
        {/* Left Info Side */}
        <div className="p-10 lg:p-16 lg:w-1/2 flex flex-col justify-center">
          <div className="inline-flex items-center gap-2 bg-blue-50/50 border border-blue-200 text-blue-600 px-4 py-1.5 rounded-full text-xs font-bold uppercase tracking-wider mb-8 w-fit">
            <Zap size={14} /> {t('topup.badge')}
          </div>
          <h2 className="text-3xl md:text-5xl font-bold mb-6 leading-tight drop-shadow-sm text-slate-900">
            {t('topup.title_start')} <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">{t('topup.title_highlight')}</span>
          </h2>
          <p className="text-slate-600 text-lg mb-10 leading-relaxed max-w-md">
            {t('topup.subtitle')}
          </p>
          
          <div className="space-y-6 mb-10">
            <div className="flex items-center gap-4 group">
              <div className="bg-white/50 border border-slate-200 p-3 rounded-xl group-hover:bg-blue-50 transition-colors text-blue-600">
                <ShieldCheck size={24} />
              </div>
              <div>
                <h4 className="font-bold text-slate-900">{t('topup.feature_1_title')}</h4>
                <p className="text-sm text-slate-500">{t('topup.feature_1_desc')}</p>
              </div>
            </div>
            <div className="flex items-center gap-4 group">
              <div className="bg-white/50 border border-slate-200 p-3 rounded-xl group-hover:bg-cyan-50 transition-colors text-cyan-600">
                <Coins size={24} />
              </div>
              <div>
                <h4 className="font-bold text-slate-900">{t('topup.feature_2_title')}</h4>
                <p className="text-sm text-slate-500">{t('topup.feature_2_desc')}</p>
              </div>
            </div>
          </div>

          <button 
            onClick={onViewDetails}
            className="text-slate-500 hover:text-blue-600 font-bold text-sm uppercase tracking-wider flex items-center gap-2 transition-colors w-fit border-b border-transparent hover:border-blue-200 pb-0.5"
          >
            {t('topup.btn_details')}
          </button>
        </div>

        {/* Right Input Side (Wallet Style) */}
        <div className="lg:w-1/2 bg-white/40 backdrop-blur-sm border-t lg:border-t-0 lg:border-l border-white/50 p-10 lg:p-16 flex flex-col justify-center">
          
          <div className="bg-white/80 backdrop-blur-md rounded-3xl p-8 shadow-xl border border-white/60">
            <div className="flex items-center gap-2 mb-6 text-slate-900">
              <Wallet className="text-blue-600" />
              <h3 className="font-bold text-lg">{t('topup.recharge_amount')}</h3>
            </div>
            
            <div className="relative mb-3">
              <span className="absolute left-6 top-1/2 transform -translate-y-1/2 text-2xl font-bold text-slate-400">৳</span>
              <input 
                type="number" 
                value={amount}
                onChange={handleAmountChange}
                placeholder="200"
                className={`w-full pl-12 pr-6 py-5 text-4xl font-bold text-slate-800 bg-slate-50 border-2 rounded-2xl focus:outline-none focus:ring-4 transition-all ${error ? 'border-red-300 focus:border-red-500 focus:ring-red-100' : 'border-slate-200 focus:border-blue-500 focus:ring-blue-100'}`}
              />
            </div>
            
            {error && (
              <p className="text-red-500 text-xs font-bold flex items-center gap-1 mb-6 animate-pulse bg-red-50 p-2 rounded-lg">
                <AlertCircle size={14} /> {error}
              </p>
            )}

            <div className="flex flex-wrap gap-2 mb-8 mt-4">
              {QUICK_AMOUNTS.map(amt => (
                <button
                  key={amt}
                  onClick={() => { setAmount(amt.toString()); setError(''); }}
                  className="px-4 py-2 bg-white hover:bg-blue-50 text-slate-600 hover:text-blue-700 border border-slate-200 hover:border-blue-300 rounded-lg text-sm font-bold transition-all active:scale-95"
                >
                  ৳{amt}
                </button>
              ))}
            </div>

            <button 
              onClick={handleSubmit}
              className="w-full bg-slate-900 hover:bg-blue-600 text-white font-bold py-4 rounded-xl shadow-lg hover:shadow-blue-500/30 transition-all transform hover:-translate-y-1 active:translate-y-0 text-lg flex justify-center items-center gap-2"
            >
              <Zap size={20} className="fill-white" /> {t('topup.btn_recharge')}
            </button>
            
            <p className="text-center text-xs text-slate-400 mt-6 font-medium">
              {t('topup.secure_note')}
            </p>
          </div>

        </div>
      </div>
    </div>
  );
};

export default ApiTopupSection;