import React from 'react';
import { PricingPackage } from '../types';
import { Check, Clock, Zap, Info, Server, Sparkles } from 'lucide-react';
import { useLanguage } from '../LanguageContext';

interface PricingCardProps {
  pkg: PricingPackage;
  onOrder: (pkg: PricingPackage) => void;
  onViewDetails: (pkgId: string) => void;
}

const PricingCard: React.FC<PricingCardProps> = ({ pkg, onOrder, onViewDetails }) => {
  const { t } = useLanguage();

  return (
    <div className={`relative flex flex-col h-full rounded-3xl transition-all duration-300 backdrop-blur-md ${
      pkg.recommended 
      ? 'bg-white/80 border-2 border-blue-500 shadow-2xl scale-105 z-10' 
      : 'bg-white/60 border border-white/50 shadow-[0_10px_30px_rgb(0,0,0,0.04)] hover:shadow-xl hover:bg-white/80'
    }`}>
      
      {pkg.recommended && (
        <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white text-xs font-bold px-4 py-1.5 rounded-full uppercase tracking-wider shadow-lg flex items-center gap-1">
            <Sparkles size={12} className="text-yellow-300 fill-yellow-300" /> {t('pricing.popular')}
          </div>
        </div>
      )}

      {/* Content */}
      <div className="p-8 flex flex-col h-full">
        {/* Header */}
        <div className="mb-8">
          <h3 className={`text-xl font-bold tracking-tight mb-2 ${pkg.recommended ? 'text-blue-600' : 'text-slate-900'}`}>{pkg.serviceName}</h3>
          <p className="text-sm text-slate-500 leading-relaxed">{pkg.description}</p>
        </div>

        {/* Pricing Options */}
        <div className="space-y-4 mb-8">
          {/* Monthly Option or API Info */}
          {!pkg.hideMonthlyOption ? (
            <div className={`rounded-xl p-4 border transition-colors ${pkg.recommended ? 'bg-blue-50/50 border-blue-100' : 'bg-slate-50/50 border-slate-100'}`}>
              <div className="flex justify-between items-center mb-2">
                <span className="text-xs font-bold text-slate-500 uppercase tracking-wide">
                   {t('pricing.monthly')}
                </span>
                {!pkg.monthlyVariants && (
                  <span className="text-2xl font-bold text-slate-900">{pkg.monthlyPrice}</span>
                )}
              </div>
              
              {pkg.monthlyVariants ? (
                <div className="space-y-2">
                  {pkg.monthlyVariants.map((variant, idx) => (
                    <div key={idx} className="flex justify-between items-center text-sm border-b border-black/5 pb-1 last:border-0 last:pb-0">
                      <span className="text-slate-600 font-medium">{variant.name}</span>
                      <span className="font-bold text-slate-900">{variant.price}</span>
                    </div>
                  ))}
                </div>
              ) : null}
            </div>
          ) : (
            <div className="bg-indigo-50/50 rounded-xl p-4 border border-indigo-100">
               <div className="flex items-center gap-2 mb-2 text-indigo-600 font-bold text-sm uppercase tracking-wide">
                 <Server size={16} />
                 <span>{t('pricing.api_billing')}</span>
               </div>
               <p className="text-xs text-indigo-900/70 leading-relaxed">
                 {t('pricing.api_desc_1')}
               </p>
            </div>
          )}

          {/* One-Time Option */}
          <div className="rounded-xl p-4 border border-white/60 bg-white/40">
            <div className="flex justify-between items-center">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wide">
                 {t('pricing.onetime')}
              </span>
              <span className="text-xl font-bold text-slate-900">{pkg.oneTimePrice}</span>
            </div>
          </div>
        </div>

        {/* Features */}
        <div className="flex-1 mb-8">
          <ul className="space-y-3">
            {pkg.features.map((feature, idx) => (
              <li key={idx} className="flex items-start gap-3">
                <div className="mt-1 shrink-0 bg-green-100 text-green-600 rounded-full p-0.5">
                  <Check className="w-3 h-3" />
                </div>
                <span className="text-sm text-slate-600 font-medium">{feature}</span>
              </li>
            ))}
          </ul>
        </div>

        {/* Actions */}
        <div className="flex gap-3 mt-auto">
          <button 
            onClick={() => onViewDetails(pkg.id)}
            className="px-4 py-3 border border-slate-200 text-slate-600 hover:bg-slate-50 hover:text-slate-900 rounded-xl font-bold text-sm transition-all"
          >
            <Info size={18} />
          </button>
          <button 
            onClick={() => onOrder(pkg)}
            className={`flex-1 py-3 rounded-xl font-bold text-sm transition-all shadow-lg text-white transform hover:-translate-y-0.5 active:translate-y-0 ${
              pkg.recommended 
              ? 'bg-gradient-to-r from-blue-600 to-indigo-600 hover:shadow-blue-500/30' 
              : 'bg-slate-900 hover:bg-slate-800'
            }`}
          >
            {t('pricing.order_btn')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default PricingCard;