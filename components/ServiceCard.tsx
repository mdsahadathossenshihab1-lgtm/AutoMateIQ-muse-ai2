
import React from 'react';
import { Service } from '../types';
import { ArrowRight } from 'lucide-react';
import { useLanguage } from '../LanguageContext';

interface ServiceCardProps {
  service: Service;
  onClick: () => void;
}

const ServiceCard: React.FC<ServiceCardProps> = ({ service, onClick }) => {
  const { t } = useLanguage();
  const iconElement = service.icon as React.ReactElement<{ className?: string }>;
  
  const originalProps = iconElement.props || {};
  const originalClassName = originalProps.className || '';
  const classNameWithoutSize = originalClassName.replace(/\bw-\d+/g, '').replace(/\bh-\d+/g, '').replace(/text-\w+-\d+/g, '').trim();
  // We ensure the icon uses current text color so parent's group-hover:text-white works
  const finalClassName = `w-7 h-7 ${classNameWithoutSize}`;

  return (
    <div 
      onClick={onClick}
      className="group bg-white/60 backdrop-blur-md h-full rounded-2xl p-8 cursor-pointer border border-white/50 shadow-[0_4px_20px_rgb(0,0,0,0.03)] hover:shadow-[0_10px_40px_rgb(0,0,0,0.08)] hover:-translate-y-1 transition-all duration-300 relative overflow-hidden"
    >
      <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-blue-500 to-indigo-500 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-500 origin-left"></div>
      
      <div className="flex flex-col h-full">
        {/* Icon Container: Default Blue text, Hover White text */}
        <div className="mb-6 w-14 h-14 rounded-xl bg-blue-50/50 flex items-center justify-center group-hover:bg-blue-600 text-blue-600 group-hover:text-white transition-all duration-300">
           {React.cloneElement(iconElement, { className: finalClassName })}
        </div>

        <h3 className="text-xl font-bold text-slate-900 mb-3 tracking-tight group-hover:text-blue-600 transition-colors">
          {service.title}
        </h3>
        
        <p className="text-slate-600 text-sm leading-relaxed mb-8 flex-1">
          {service.description}
        </p>

        <div className="flex items-center text-sm font-bold text-slate-900 group-hover:text-blue-600 transition-colors mt-auto">
          {t('services.details_btn')} 
          <ArrowRight size={16} className="ml-2 opacity-0 -translate-x-2 group-hover:opacity-100 group-hover:translate-x-0 transition-all duration-300" />
        </div>
      </div>
    </div>
  );
};

export default ServiceCard;
