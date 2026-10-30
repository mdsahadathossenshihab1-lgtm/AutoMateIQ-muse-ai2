import React, { useState, useEffect } from 'react';
import { PricingPackage } from '../types';
import { CONTACT_INFO, getPricingPackages, getApiTopupPackage } from '../constants';
import { useAuth } from '../AuthContext';
import { useLanguage } from '../LanguageContext';
import { trackPixelEvent } from '../services/pixelService';
import { X, Copy, CreditCard, Smartphone, User as UserIcon, CheckCircle2, Loader2, Clock } from 'lucide-react';

interface OrderModalProps {
  isOpen: boolean;
  onClose: () => void;
  pkgId: string | null;
  customAmount?: number | null;
  onSuccess: () => void;
}

const OrderModal: React.FC<OrderModalProps> = ({ isOpen, onClose, pkgId, customAmount, onSuccess }) => {
  const { user, addOrder } = useAuth();
  const { t, language } = useLanguage();
  
  // Reactive Data
  const packages = getPricingPackages(language);
  const apiTopupPkg = getApiTopupPackage(language);
  
  // Derive current package based on language and ID
  let pkg: PricingPackage | null = null;
  if (pkgId) {
      if (pkgId === 'api-topup') pkg = apiTopupPkg;
      else pkg = packages.find(p => p.id === pkgId) || null;
  }
  
  // Apply Custom Amount override if exists
  if (pkg && customAmount && pkgId === 'api-topup') {
      pkg = {
          ...pkg,
          oneTimePrice: `৳${customAmount}`,
          features: [`Recharge Amount: ৳${customAmount}`, ...apiTopupPkg.features]
      };
  }

  const [selectedType, setSelectedType] = useState<string>('');
  const [senderPhone, setSenderPhone] = useState('');
  const [trxId, setTrxId] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('bkash');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [copied, setCopied] = useState(false);
  const [showSuccessView, setShowSuccessView] = useState(false);

  const PAYMENT_NUMBER = CONTACT_INFO.paymentNumber; 

  useEffect(() => {
    if (isOpen && pkg) {
      setSenderPhone('');
      setTrxId('');
      setPaymentMethod('bkash');
      setIsSubmitting(false);
      setCopied(false);
      setShowSuccessView(false);
      
      if (pkg.hideMonthlyOption) {
        setSelectedType('onetime');
      } else {
        setSelectedType('');
      }

      // Track InitiateCheckout when modal opens
      trackPixelEvent('InitiateCheckout', {
        content_name: pkg.serviceName,
        content_ids: [pkg.id],
        content_type: 'product',
        currency: 'BDT'
      });
    }
  }, [isOpen, pkg?.id]); // Depend on ID to re-track only if ID changes

  if (!isOpen || !pkg || !user) return null;

  const handleCopyNumber = () => {
    navigator.clipboard.writeText(PAYMENT_NUMBER.replace(/-/g, '').replace(/ /g, ''));
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedType || !senderPhone || !trxId) return;

    setIsSubmitting(true);

    let packageDetail = '';
    let amount = '';

    if (selectedType === 'onetime') {
      packageDetail = 'One Time';
      amount = pkg.oneTimePrice;
    } else if (selectedType.startsWith('variant_')) {
      const idx = parseInt(selectedType.split('_')[1]);
      if (pkg.monthlyVariants && pkg.monthlyVariants[idx]) {
        packageDetail = `Monthly - ${pkg.monthlyVariants[idx].name}`;
        amount = pkg.monthlyVariants[idx].price;
      }
    } else {
      packageDetail = 'Monthly';
      amount = pkg.monthlyPrice;
    }

    try {
      await addOrder({
        userId: user.id,
        userName: user.name,
        userPhone: user.phone,
        serviceName: pkg.serviceName,
        packageDetails: packageDetail,
        amount: amount,
        paymentMethod: paymentMethod,
        senderPhone: senderPhone,
        trxId: trxId
      });

      let numericAmount = 0;
      try {
        const englishStr = amount.replace(/[০-৯]/g, (d) => String.fromCharCode(d.charCodeAt(0) - 2534 + 48));
        numericAmount = parseFloat(englishStr.replace(/[^0-9.]/g, '')) || 0;
      } catch (e) { console.error("Price parse error", e); }

      trackPixelEvent('Purchase', {
        value: numericAmount,
        currency: 'BDT',
        content_name: pkg.serviceName,
        content_ids: [pkg.id],
        content_type: 'product'
      });

      setShowSuccessView(true);
    } catch (error) {
      console.error("Order failed:", error);
      alert('Order failed to submit.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCloseSuccess = () => {
    onSuccess();
  };

  if (showSuccessView) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm transition-opacity duration-300">
        <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-fade-in-up text-center p-8">
          <div className="w-20 h-20 bg-blue-100 text-blue-600 rounded-full flex items-center justify-center mx-auto mb-6">
            <CheckCircle2 size={40} />
          </div>
          <h3 className="text-2xl font-bold text-slate-900 mb-2">{t('order.success_title')}</h3>
          <p className="text-slate-600 mb-6">
            {t('order.success_msg')}
          </p>
          
          <div className="bg-blue-50 border border-blue-100 rounded-xl p-4 mb-8">
            <div className="flex items-center justify-center gap-2 text-blue-800 font-bold mb-1">
              <Clock size={20} />
              {t('order.time_limit')}
            </div>
            <p className="text-sm text-blue-700">
              {t('order.wait_msg')} <br/>
              <span className="font-extrabold text-lg">{t('order.wait_hours')}</span> <br/>
              {t('order.wait_end')}
            </p>
          </div>

          <button 
            onClick={handleCloseSuccess}
            className="w-full bg-slate-900 text-white font-bold py-3.5 rounded-xl hover:bg-slate-800 transition-all shadow-lg"
          >
            {t('order.btn_dashboard')}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm transition-opacity duration-300">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[90vh] overflow-hidden flex flex-col animate-fade-in-up">
        {/* Header */}
        <div className="bg-blue-600 p-5 flex justify-between items-center text-white shrink-0">
          <div>
            <h3 className="text-xl font-bold">{t('order.title')}</h3>
            <p className="text-blue-100 text-xs mt-0.5">{t('order.subtitle')}</p>
          </div>
          <button onClick={onClose} className="bg-white/10 hover:bg-white/20 p-2 rounded-full transition-colors">
            <X size={20} />
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="overflow-y-auto p-6 space-y-6 scrollbar-thin scrollbar-thumb-gray-200">
          {/* Package Selection */}
          <div>
            <label className="text-sm font-bold text-gray-700 uppercase tracking-wide mb-3 block">
              {t('order.step_1')}
            </label>
            <div className="space-y-2">
              <div className="p-3 border border-blue-200 bg-blue-50 rounded-lg">
                  <p className="font-bold text-blue-800 text-sm">{pkg.serviceName}</p>
              </div>

              {!pkg.hideMonthlyOption && (
                <>
                  {pkg.monthlyVariants ? (
                    pkg.monthlyVariants.map((v, i) => (
                      <label key={i} className={`flex justify-between items-center p-3 border rounded-lg cursor-pointer transition-all ${selectedType === `variant_${i}` ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-500' : 'hover:bg-gray-50 border-gray-200'}`}>
                        <div className="flex items-center gap-2">
                          <input 
                            type="radio" 
                            name="pkgType" 
                            value={`variant_${i}`}
                            checked={selectedType === `variant_${i}`}
                            onChange={(e) => setSelectedType(e.target.value)}
                            className="text-blue-600 focus:ring-blue-500"
                          />
                          <span className="text-sm font-medium text-gray-800">{v.name}</span>
                        </div>
                        <span className="text-sm font-bold text-blue-700">{v.price}</span>
                      </label>
                    ))
                  ) : (
                    <label className={`flex justify-between items-center p-3 border rounded-lg cursor-pointer transition-all ${selectedType === 'monthly' ? 'border-blue-500 bg-blue-50 ring-1 ring-blue-500' : 'hover:bg-gray-50 border-gray-200'}`}>
                      <div className="flex items-center gap-2">
                        <input 
                          type="radio" 
                          name="pkgType" 
                          value="monthly"
                          checked={selectedType === 'monthly'}
                          onChange={(e) => setSelectedType(e.target.value)}
                          className="text-blue-600 focus:ring-blue-500"
                        />
                        <span className="text-sm font-medium text-gray-800">{t('order.monthly_sub')}</span>
                      </div>
                      <span className="text-sm font-bold text-blue-700">{pkg.monthlyPrice}</span>
                    </label>
                  )}
                </>
              )}

              <label className={`flex justify-between items-center p-3 border rounded-lg cursor-pointer transition-all ${selectedType === 'onetime' ? 'border-purple-500 bg-purple-50 ring-1 ring-purple-500' : 'hover:bg-gray-50 border-gray-200'}`}>
                <div className="flex items-center gap-2">
                  <input 
                    type="radio" 
                    name="pkgType" 
                    value="onetime"
                    checked={selectedType === 'onetime'}
                    onChange={(e) => setSelectedType(e.target.value)}
                    className="text-purple-600 focus:ring-purple-500"
                  />
                  <span className="text-sm font-medium text-gray-800">{t('order.onetime_pkg')}</span>
                </div>
                <span className="text-sm font-bold text-purple-700">{pkg.oneTimePrice}</span>
              </label>
            </div>
          </div>

          {/* Payment Info */}
          <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4">
            <label className="text-sm font-bold text-gray-800 uppercase tracking-wide mb-2 block flex items-center gap-2">
               {t('order.step_2')}
            </label>
            <div className="flex items-center justify-between bg-white border border-yellow-300 rounded-lg p-3 shadow-sm mb-3">
              <div>
                <p className="text-xs text-gray-500 font-medium">bKash/Nagad (Personal)</p>
                <p className="text-lg font-bold text-gray-800 font-mono tracking-wider">{PAYMENT_NUMBER}</p>
              </div>
              <button 
                onClick={handleCopyNumber}
                className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors relative"
                title="Copy Number"
              >
                {copied ? <CheckCircle2 size={20} className="text-blue-600" /> : <Copy size={20} />}
              </button>
            </div>
            
            <div className="flex gap-2 justify-center">
                 <label className={`cursor-pointer border rounded-lg px-4 py-2 text-sm w-full text-center transition-all ${paymentMethod === 'bkash' ? 'border-pink-500 bg-pink-100 text-pink-700 font-bold' : 'border-gray-200 text-gray-500 bg-white'}`}>
                    <input type="radio" name="method" value="bkash" className="hidden" checked={paymentMethod === 'bkash'} onChange={(e) => setPaymentMethod(e.target.value)} />
                    bKash
                 </label>
                 <label className={`cursor-pointer border rounded-lg px-4 py-2 text-sm w-full text-center transition-all ${paymentMethod === 'nagad' ? 'border-orange-500 bg-orange-100 text-orange-700 font-bold' : 'border-gray-200 text-gray-500 bg-white'}`}>
                    <input type="radio" name="method" value="nagad" className="hidden" checked={paymentMethod === 'nagad'} onChange={(e) => setPaymentMethod(e.target.value)} />
                    Nagad
                 </label>
              </div>
          </div>

          {/* Verification Form */}
          <div>
            <label className="text-sm font-bold text-gray-700 uppercase tracking-wide mb-3 block">
              {t('order.step_3')}
            </label>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">{t('order.sender_number')}</label>
                <div className="relative">
                  <Smartphone className="absolute left-3 top-2.5 text-gray-500" size={16} />
                  <input 
                    type="tel"
                    required
                    placeholder="e.g. 017..."
                    value={senderPhone}
                    onChange={(e) => setSenderPhone(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-slate-500 outline-none text-sm"
                  />
                </div>
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-600 mb-1">{t('order.trx_id')}</label>
                <div className="relative">
                  <CreditCard className="absolute left-3 top-2.5 text-gray-500" size={16} />
                  <input 
                    type="text"
                    required
                    placeholder="e.g. 8N7A6..."
                    value={trxId}
                    onChange={(e) => setTrxId(e.target.value.toUpperCase())}
                    className="w-full pl-9 pr-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 bg-white text-slate-900 placeholder-slate-500 outline-none text-sm font-mono uppercase"
                  />
                </div>
              </div>
            </div>
            
            <div className="mt-4 bg-gray-50 p-3 rounded-lg border border-gray-200">
               <label className="block text-xs font-semibold text-gray-600 mb-1">{t('order.client_account')}</label>
               <div className="flex items-center gap-2">
                 <UserIcon className="text-blue-500" size={16} />
                 <div>
                   <p className="text-xs font-bold text-gray-800">{user.name}</p>
                   <p className="text-xs text-gray-500">{user.email}</p>
                 </div>
               </div>
            </div>
          </div>

        </div>

        {/* Footer Action */}
        <div className="p-5 border-t border-gray-100 bg-gray-50 shrink-0">
          <button 
            type="button"
            onClick={handleOrder}
            disabled={!selectedType || !senderPhone || !trxId || isSubmitting}
            className={`w-full py-3.5 rounded-xl font-bold text-white shadow-lg flex items-center justify-center gap-2 transition-all ${
              !selectedType || !senderPhone || !trxId || isSubmitting
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-gradient-to-r from-blue-600 to-indigo-600 hover:shadow-blue-500/30 active:scale-95'
            }`}
          >
            {isSubmitting ? (
              <span className="flex items-center gap-2">
                <Loader2 size={20} className="animate-spin" />
                {t('order.btn_processing')}
              </span>
            ) : (
              <>
                <CheckCircle2 size={20} />
                {t('order.btn_confirm')}
              </>
            )}
          </button>
          <p className="text-center text-gray-400 text-[10px] mt-2">
            {t('order.footer_note')}
          </p>
        </div>

      </div>
    </div>
  );
};

export default OrderModal;