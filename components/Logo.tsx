import React from 'react';

interface LogoProps {
  className?: string;
  isDark?: boolean;
}

const Logo: React.FC<LogoProps> = ({ className = "text-2xl" }) => {
  return (
    <div className={`font-extrabold tracking-tighter select-none ${className} flex items-center gap-2`}>
      {/* Icon: Blue Paper Plane */}
      <svg
        width="36"
        height="36"
        viewBox="0 0 24 24"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="w-8 h-8 md:w-9 md:h-9 transform -rotate-3 drop-shadow-sm"
      >
        <path
          d="M22 2L11 13"
          stroke="#3b82f6" 
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        <path
          d="M22 2L15 22L11 13L2 9L22 2Z"
          fill="#3b82f6" 
          stroke="#3b82f6"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        />
      </svg>

      {/* Text: AutoMate (Dark) + IQ (Blue) */}
      <span className="text-slate-900">
        AutoMate<span className="text-blue-600">IQ</span>
      </span>
    </div>
  );
};

export default Logo;