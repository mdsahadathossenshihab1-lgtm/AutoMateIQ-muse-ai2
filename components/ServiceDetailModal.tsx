import React, { useEffect } from 'react';
import { ServiceDetailContent } from '../types';
import { CONTACT_INFO, getServiceDetails } from '../constants';
import { trackPixelEvent } from '../services/pixelService';
import { X, CheckCircle, ArrowRight, Video, Settings, Facebook } from 'lucide-react';
import { useLanguage } from '../LanguageContext';

interface ServiceDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  serviceId: string | null;
}

const ServiceDetailModal: React.FC<ServiceDetailModalProps> = ({ isOpen, onClose, serviceId }) => {
  const { language } = useLanguage();
  
  // Reactive Data
  const serviceDetails = getServiceDetails(language);
  const content: ServiceDetailContent | null = serviceId && serviceDetails[serviceId] 
    ? serviceDetails[serviceId] 
    : serviceId && serviceDetails['default'] 
      ? serviceDetails['default'] 
      : null;

  // Track ViewContent when modal opens
  useEffect(() => {
    if (isOpen && content) {
      trackPixelEvent('ViewContent', {
        content_name: content.title,
        content_ids: [content.id],
        content_type: 'product',
        value: 0, // Viewing has no direct value
        currency: 'BDT'
      });
    }
  }, [isOpen, content?.id]);

  if (!isOpen || !content) return null;

  const handleBookMeeting = () => {
    const message = `Hi, I am interested in ${content.title}. I want to book a meeting to discuss details.`;
    window.open(`https://wa.me/${CONTACT_INFO.whatsapp}?text=${encodeURIComponent(message)}`, '_blank');
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm transition-opacity duration-300">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col animate-fade-in-up transform transition-all">
        
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-900 to-indigo-900 p-6 text-white relative shrink-0">
          <button 
            onClick={onClose} 
            className="absolute top-4 right-4 text-blue-200 hover:text-white bg-white/10 hover:bg-white/20 p-2 rounded-full transition-colors"
          >
            <X size={20} />
          </button>
          <h2 className="text-2xl font-bold mb-2 leading-tight">{content.title}</h2>
          <p className="text-blue-200 text-sm">{content.subtitle}</p>
        </div>

        {/* Scrollable Body */}
        <div className="overflow-y-auto p-6 space-y-8 scrollbar-thin scrollbar-thumb-gray-300">
          
          {/* How It Works */}
          <section>
            <h3 className="text-lg font-bold text-gray-800 flex items-center gap-2 mb-4 border-b pb-2 border-gray-100">
              <Settings className="text-blue-600" size={20} />
              কিভাবে কাজ করে?
            </h3>
            <div className="space-y-6 relative before:absolute before:left-[15px] before:top-2 before:h-full before:w-0.5 before:bg-blue-100">
              {content.howItWorks.map((step, idx) => (
                <div key={idx} className="relative pl-12 group">
                  <span className="absolute left-0 top-0 bg-blue-600 text-white w-8 h-8 rounded-full flex items-center justify-center font-bold text-sm shadow-md z-10 group-hover:scale-110 transition-transform">
                    {idx + 1}
                  </span>
                  <div className="bg-white p-1 rounded-lg">
                    <h4 className="font-bold text-gray-800 text-base mb-1">{step.title}</h4>
                    <p className="text-gray-600 text-sm leading-relaxed">{step.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Benefits */}
          <section className="bg-gradient-to-br from-blue-50 to-indigo-50 rounded-xl p-6 border border-blue-100 shadow-sm">
            <h3 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">
              <CheckCircle className="text-blue-600" size={20} />
              সুবিধাসমূহ
            </h3>
            <ul className="grid grid-cols-1 gap-3">
              {content.benefits.map((benefit, idx) => (
                <li key={idx} className="flex items-start gap-3 text-sm text-gray-700">
                  <ArrowRight size={16} className="text-blue-500 mt-1 shrink-0" />
                  <span className="font-medium">{benefit}</span>
                </li>
              ))}
            </ul>
          </section>

          {/* Tech Specs (if available) */}
          {content.techSpecs && (
            <section>
               <h4 className="text-xs font-bold text-gray-400 uppercase tracking-wider mb-3">টেকনিক্যাল ফিচার</h4>
               <div className="flex flex-wrap gap-2">
                {content.techSpecs.map((spec, idx) => (
                  <span key={idx} className="px-3 py-1.5 bg-gray-50 border border-gray-200 rounded-lg text-xs font-semibold text-gray-600">
                    {spec}
                  </span>
                ))}
              </div>
            </section>
          )}

        </div>

        {/* Footer / CTA */}
        <div className="p-5 border-t border-gray-100 bg-gray-50 shrink-0 flex flex-col md:flex-row gap-3">
          <button 
            onClick={handleBookMeeting}
            className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-bold py-3.5 rounded-xl transition-all shadow-lg hover:shadow-blue-500/30 flex items-center justify-center gap-2"
          >
            <Video size={20} />
            মিটিং বুক করুন (WhatsApp)
          </button>
          
          <a 
            href={CONTACT_INFO.facebookPage}
            target="_blank"
            rel="noopener noreferrer"
            className="flex-1 bg-indigo-800 hover:bg-indigo-900 text-white font-bold py-3.5 rounded-xl transition-all shadow-lg hover:shadow-indigo-700/30 flex items-center justify-center gap-2"
          >
            <Facebook size={20} />
            ফেসবুক পেজ ভিজিট করুন
          </a>
        </div>

      </div>
    </div>
  );
};

export default ServiceDetailModal;