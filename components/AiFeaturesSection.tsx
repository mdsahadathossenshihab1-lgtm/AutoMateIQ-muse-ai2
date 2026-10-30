
import React from 'react';
import { useLanguage } from '../LanguageContext';
import { BrainCircuit, Sparkles, TrendingUp, Cpu, Bot, Mic, Zap } from 'lucide-react';

const AiFeaturesSection = () => {
  const { t } = useLanguage();

  const features = [
    {
      icon: <BrainCircuit size={32} />,
      title: t('ai_section.f1_title'),
      desc: t('ai_section.f1_desc'),
      color: "from-blue-500 to-indigo-500",
      delay: "0"
    },
    {
      icon: <Sparkles size={32} />,
      title: t('ai_section.f2_title'),
      desc: t('ai_section.f2_desc'),
      color: "from-purple-500 to-pink-500",
      delay: "100"
    },
    {
      icon: <TrendingUp size={32} />,
      title: t('ai_section.f3_title'),
      desc: t('ai_section.f3_desc'),
      color: "from-green-500 to-teal-500",
      delay: "200"
    },
    {
      icon: <Mic size={32} />,
      title: t('ai_section.f4_title'),
      desc: t('ai_section.f4_desc'),
      color: "from-orange-500 to-red-500",
      delay: "300"
    }
  ];

  return (
    <section className="py-24 px-4 sm:px-6 lg:px-8 relative overflow-hidden">
      {/* Dark Modern Background */}
      <div className="absolute inset-0 bg-slate-900 z-0">
        <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
        <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-b from-transparent to-slate-900/90"></div>
        {/* Glowing Orbs */}
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-600/30 rounded-full blur-[100px] animate-blob"></div>
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-purple-600/30 rounded-full blur-[100px] animate-blob animation-delay-2000"></div>
      </div>

      <div className="max-w-7xl mx-auto relative z-10">
        {/* Header */}
        <div className="text-center mb-16 animate-fade-in-up">
          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/10 backdrop-blur-md border border-white/10 text-blue-300 mb-6 shadow-lg">
             <Cpu size={16} className="animate-pulse" />
             <span className="text-xs font-bold uppercase tracking-widest">{t('ai_section.title')}</span>
          </div>
          <h2 className="text-4xl md:text-5xl font-bold text-white mb-6 leading-tight">
            {t('ai_section.title_highlight')}
          </h2>
          <p className="text-slate-300 text-lg max-w-2xl mx-auto leading-relaxed">
            {t('ai_section.subtitle')}
          </p>
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((item, idx) => (
            <div 
              key={idx}
              className={`group relative bg-white/5 backdrop-blur-lg border border-white/10 rounded-3xl p-8 hover:bg-white/10 transition-all duration-300 hover:-translate-y-2 animate-fade-in-up animation-delay-${item.delay}`}
            >
              {/* Hover Glow */}
              <div className={`absolute inset-0 rounded-3xl bg-gradient-to-br ${item.color} opacity-0 group-hover:opacity-10 transition-opacity duration-500`}></div>
              
              <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${item.color} flex items-center justify-center text-white mb-6 shadow-lg group-hover:scale-110 transition-transform duration-300`}>
                {item.icon}
              </div>
              
              <h3 className="text-xl font-bold text-white mb-3 group-hover:text-blue-300 transition-colors">
                {item.title}
              </h3>
              
              <p className="text-slate-400 text-sm leading-relaxed">
                {item.desc}
              </p>
            </div>
          ))}
        </div>

        {/* Bottom Badge */}
        <div className="mt-16 text-center animate-fade-in-up animation-delay-500">
           <div className="inline-flex items-center gap-3 text-slate-400 text-sm font-medium bg-slate-800/50 px-6 py-3 rounded-2xl border border-white/5">
              <span>Powered by</span>
              <div className="flex items-center gap-2">
                 <span className="text-white font-bold flex items-center gap-1"><Zap size={14} className="text-yellow-400 fill-yellow-400"/> Google Gemini</span>
                 <span className="w-1 h-1 bg-slate-600 rounded-full"></span>
                 <span className="text-white font-bold flex items-center gap-1"><Bot size={14} className="text-green-400"/> GPT-4</span>
              </div>
           </div>
        </div>

      </div>
    </section>
  );
};

export default AiFeaturesSection;
