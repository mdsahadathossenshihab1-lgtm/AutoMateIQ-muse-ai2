
import React from 'react';
import { MessageCircle, Facebook, Youtube, MessageSquare, Instagram, Sparkles } from 'lucide-react';
import { Service, PricingPackage, ServiceDetailContent, Language } from './types';

// Updated: Louder "Ding" Sound (Base64)
export const NOTIFICATION_SOUND_BASE64 = "data:audio/mpeg;base64,SUQzBAAAAAABAFRYWFQAAAASAAADbWFqb3JfYnJhbmQAbXA0MgBUWFhUAAAAEQAAA21pbm9yX3ZlcnNpb24AMABUWFhUAAAAHAAAA2NvbXBhdGlibGVfYnJhbmRzAGlzb21tcDQyAFRTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//NExAAAAANIAAAAAExBTUUzLjEwMKqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq";

// Centralized Contact Information
export const CONTACT_INFO = {
  phone: '+880 1624-819585',
  whatsapp: '8801624819585',
  paymentNumber: '01842-369496',
  email: 'info@automateiq.xyz',
  facebookPage: 'https://web.facebook.com/automateiq.xyz',
  youtubeChannel: 'https://www.youtube.com/@AutoMateIQ',
  address: 'Dhaka, Bangladesh'
};

export const getServices = (lang: Language): Service[] => [
  {
    id: 'messenger',
    title: lang === 'bn' ? 'মেসেঞ্জার চ্যাটবট' : 'Messenger Chatbot',
    description: lang === 'bn' ? 'আপনার ফেসবুক পেজের জন্য ২৪/৭ অটোমেটেড কাস্টমার সাপোর্ট এবং লিড জেনারেশন।' : '24/7 automated customer support and lead generation for your Facebook page.',
    icon: <MessageCircle className="w-10 h-10" />,
  },
  {
    id: 'instagram',
    title: lang === 'bn' ? 'ইনস্টাগ্রাম চ্যাটবট' : 'Instagram Chatbot',
    description: lang === 'bn' ? 'ইনস্টাগ্রাম ডিএম এবং স্টোরি রিপ্লাই অটোমেশন। বিজনেস গ্রোথ এবং এনগেজমেন্ট বৃদ্ধি।' : 'Automate Instagram DMs and Story replies. Boost business growth and engagement.',
    icon: <Instagram className="w-10 h-10" />,
  },
  {
    id: 'whatsapp',
    title: lang === 'bn' ? 'হোয়াটসঅ্যাপ চ্যাটবট' : 'WhatsApp Chatbot',
    description: lang === 'bn' ? 'বাল্ক মেসেজিং এবং অটো রিপ্লাই এর মাধ্যমে কাস্টমারদের সাথে সরাসরি যোগাযোগ।' : 'Direct communication with customers via Bulk Messaging and Auto-Replies.',
    icon: <MessageSquare className="w-10 h-10" />,
  },
  {
    id: 'fb-post',
    title: lang === 'bn' ? 'ফেসবুক অটো পোস্ট' : 'Facebook Auto Post',
    description: lang === 'bn' ? 'শিডিউল অনুযায়ী আপনার পেজ এবং গ্রুপে অটোমেটিক পোস্ট পাবলিশ করুন।' : 'Automatically publish posts to your pages and groups according to schedule.',
    icon: <Facebook className="w-10 h-10" />,
  },
  {
    id: 'yt-post',
    title: lang === 'bn' ? 'ইউটিউব অটো পোস্ট' : 'YouTube Auto Post',
    description: lang === 'bn' ? 'নতুন ভিডিও আপলোড এবং কমিউনিটি পোস্ট ম্যানেজমেন্ট অটোমেশন।' : 'Automate video uploads and community post management.',
    icon: <Youtube className="w-10 h-10" />,
  },
  {
    id: 'consultation',
    title: lang === 'bn' ? 'ফ্রি কনসালটেশন' : 'Free Consultation',
    description: lang === 'bn' ? 'আপনার বিজনেসের জন্য সেরা সলিউশন কোনটি? আমাদের এক্সপার্টের সাথে কথা বলুন।' : 'Which solution is best for your business? Talk to our experts.',
    icon: <MessageCircle className="w-10 h-10" />,
  },
  {
    id: 'coming-soon',
    title: lang === 'bn' ? 'শীঘ্রই আসছে...' : 'Coming Soon...',
    description: lang === 'bn' ? 'অ্যাডভান্সড AI কলিং এজেন্ট, ইমেইল মার্কেটিং এবং কাস্টম CRM সহ আরও নতুন সব সার্ভিস যুক্ত হচ্ছে।' : 'Advanced AI Calling Agents, Email Marketing, and Custom CRM services are coming soon.',
    icon: <Sparkles className="w-10 h-10 animate-pulse text-yellow-400" />,
  }
];

export const getPricingPackages = (lang: Language): PricingPackage[] => [
  {
    id: 'messenger-pkg',
    serviceName: lang === 'bn' ? 'মেসেঞ্জার চ্যাটবট' : 'Messenger Chatbot',
    description: lang === 'bn' ? 'স্মার্ট AI রিপ্লাই ও অর্ডার ম্যানেজমেন্ট' : 'Smart AI Reply & Order Management',
    monthlyPrice: lang === 'bn' ? '৳৫০ - ৳১,০০০' : '৳50 - ৳1,000',
    oneTimePrice: lang === 'bn' ? '৳৫,০০০' : '৳5,000',
    features: lang === 'bn' ? [
      '২৪/৭ অটো রিপ্লাই (টেক্সট/ইমেজ)',
      'এক্সটার্নাল ড্যাশবোর্ড অ্যাক্সেস',
      'API ইন্টিগ্রেশন সাপোর্ট',
      'অটোমেটেড অর্ডার টেকিং',
      'এককালীন প্যাকেজেও ৫০০ টাকা ফ্রি ক্রেডিট',
      'প্রিমিয়ামে ভয়েস মেসেজ সাপোর্ট'
    ] : [
      '24/7 Auto Reply (Text/Image)',
      'External Dashboard Access',
      'API Integration Support',
      'Automated Order Taking',
      '500 BDT Free Credit in One-time Pkg',
      'Voice Message Support in Premium'
    ],
    recommended: true,
    monthlyVariants: [
      { name: lang === 'bn' ? 'ট্রায়াল (৭ দিন)' : 'Trial (7 Days)', price: '৳50' },
      { name: lang === 'bn' ? 'স্ট্যান্ডার্ড' : 'Standard', price: '৳700' },
      { name: lang === 'bn' ? 'প্রিমিয়াম' : 'Premium', price: '৳1,000' }
    ]
  },
  {
    id: 'instagram-pkg',
    serviceName: lang === 'bn' ? 'ইনস্টাগ্রাম চ্যাটবট' : 'Instagram Chatbot',
    description: lang === 'bn' ? 'ডিএম ও স্টোরি রিপ্লাই অটোমেশন' : 'DM & Story Reply Automation',
    monthlyPrice: lang === 'bn' ? '৳৫০ - ৳১,০০০' : '৳50 - ৳1,000',
    oneTimePrice: lang === 'bn' ? '৳৫,০০০' : '৳5,000',
    features: lang === 'bn' ? [
      'প্রফেশনাল ড্যাশবোর্ড',
      'অটোমেটেড ডিএম রিপ্লাই',
      'স্টোরি মেনশন রিপ্লাই',
      'কুইক রিপ্লাই বাটন',
      'লিড কালেকশন'
    ] : [
      'Professional Dashboard',
      'Automated DM Reply',
      'Story Mention Reply',
      'Quick Reply Buttons',
      'Lead Collection'
    ],
    monthlyVariants: [
      { name: lang === 'bn' ? 'ট্রায়াল (৭ দিন)' : 'Trial (7 Days)', price: '৳50' },
      { name: lang === 'bn' ? 'স্ট্যান্ডার্ড' : 'Standard', price: '৳700' },
      { name: lang === 'bn' ? 'প্রিমিয়াম' : 'Premium', price: '৳1,000' }
    ]
  },
  {
    id: 'whatsapp-pkg',
    serviceName: lang === 'bn' ? 'হোয়াটসঅ্যাপ বিজনেস বট' : 'WhatsApp Business Bot',
    description: lang === 'bn' ? 'অফিসিয়াল API এবং অটোমেশন সলিউশন' : 'Official API & Automation Solution',
    monthlyPrice: lang === 'bn' ? '৳৫০ - $৭' : '৳50 - $7', 
    oneTimePrice: lang === 'bn' ? '৳৫,০০০' : '৳5,000',
    features: lang === 'bn' ? [
      'অফিসিয়াল Cloud API সেটআপ',
      '১ম মাসের প্ল্যাটফর্ম চার্জ ফ্রি',
      'পরবর্তীতে মাত্র $৭/মাস (API চার্জ)',
      'বিজনেস ভেরিফিকেশন সাপোর্ট',
      'আনলিমিটেড ব্রডকাস্ট'
    ] : [
      'Official Cloud API Setup',
      '1st Month Platform Fee Free',
      '$7/Month onwards (API Charge)',
      'Business Verification Support',
      'Unlimited Broadcasts'
    ],
    monthlyVariants: [
      { name: lang === 'bn' ? 'ট্রায়াল (৭ দিন)' : 'Trial (7 Days)', price: '৳50' },
      { name: lang === 'bn' ? 'স্ট্যান্ডার্ড সেটআপ' : 'Standard Setup', price: '৳1,500' }
    ]
  },
  {
    id: 'fb-post-pkg',
    serviceName: lang === 'bn' ? 'ফেসবুক অটো পোস্ট' : 'Facebook Auto Post',
    description: lang === 'bn' ? 'পেজ ও গ্রুপ শিডিউলিং অটোমেশন' : 'Page & Group Scheduling Automation',
    monthlyPrice: lang === 'bn' ? '৳৫০ - ৳১,০০০' : '৳50 - ৳1,000',
    oneTimePrice: lang === 'bn' ? '৳৫,০০০' : '৳5,000',
    features: lang === 'bn' ? [
      'আনলিমিটেড শিডিউলিং',
      'মাল্টি-পেজ ম্যানেজমেন্ট',
      'ইমেজ ও ভিডিও সাপোর্ট',
      'এনালিটিক্স রিপোর্ট',
      'বাল্ক আপলোড'
    ] : [
      'Unlimited Scheduling',
      'Multi-page Management',
      'Image & Video Support',
      'Analytics Report',
      'Bulk Upload'
    ],
    monthlyVariants: [
      { name: lang === 'bn' ? 'ট্রায়াল (৭ দিন)' : 'Trial (7 Days)', price: '৳50' },
      { name: lang === 'bn' ? 'মাসিক প্যাকেজ' : 'Monthly Package', price: '৳1,000' }
    ]
  },
  {
    id: 'yt-pkg',
    serviceName: lang === 'bn' ? 'ইউটিউব অটো পোস্ট' : 'YouTube Auto Post',
    description: lang === 'bn' ? 'ভিডিও আপলোড ও শিডিউলিং অটোমেশন' : 'Video Upload & Scheduling Automation',
    monthlyPrice: lang === 'bn' ? '৳৫০ - ৳১,০০০' : '৳50 - ৳1,000',
    oneTimePrice: lang === 'bn' ? '৳৫,০০০' : '৳5,000',
    features: lang === 'bn' ? [
      'অটো ভিডিও পাবলিশ',
      'ট্যাগ জেনারেশন ও এসইও',
      'ডেসক্রিপশন অপটিমাইজেশন',
      'কমিউনিটি পোস্ট শিডিউলিং',
      'কমেন্ট মনিটরিং'
    ] : [
      'Auto Video Publish',
      'Tag Generation & SEO',
      'Description Optimization',
      'Community Post Scheduling',
      'Comment Monitoring'
    ],
    monthlyVariants: [
      { name: lang === 'bn' ? 'ট্রায়াল (৭ দিন)' : 'Trial (7 Days)', price: '৳50' },
      { name: lang === 'bn' ? 'মাসিক প্যাকেজ' : 'Monthly Package', price: '৳1,000' }
    ]
  },
];

export const getApiTopupPackage = (lang: Language): PricingPackage => ({
  id: 'api-topup',
  serviceName: lang === 'bn' ? 'API ক্রেডিট রিচার্জ' : 'API Credit Top-up',
  description: lang === 'bn' ? 'চ্যাটবটের জন্য অতিরিক্ত ব্যালেন্স কিনুন' : 'Buy extra balance for your chatbot',
  monthlyPrice: lang === 'bn' ? '৳২০০+' : '৳200+',
  oneTimePrice: 'CUSTOM',
  hideMonthlyOption: true,
  features: lang === 'bn' ? [
    'ইনস্ট্যান্ট রিচার্জ',
    'মেয়াদহীন (No Expiry)',
    'যেকোনো প্যাকেজের সাথে ব্যবহারযোগ্য',
    'সহজ পেমেন্ট',
    'ব্যালেন্স শেষ হলে বট কাজ করবে না'
  ] : [
    'Instant Recharge',
    'No Expiry',
    'Usable with any package',
    'Easy Payment',
    'Bot stops without balance'
  ]
});

// Returns the full dictionary for service details based on language
export const getServiceDetails = (lang: Language): Record<string, ServiceDetailContent> => {
  const isBn = lang === 'bn';
  
  return {
    'default': {
      id: 'generic',
      title: isBn ? 'অটোমেশন সার্ভিস বিস্তারিত' : 'Automation Service Details',
      subtitle: isBn ? 'কিভাবে আমরা আপনার কাজ সহজ করে দিই' : 'How we simplify your workflow',
      howItWorks: [
        { 
          title: isBn ? 'রিকোয়ারমেন্ট অ্যানালাইসিস' : 'Requirement Analysis', 
          desc: isBn ? 'আমরা আপনার বিজনেসের প্রয়োজন বুঝি।' : 'We understand your business needs.'
        },
        { 
          title: isBn ? 'সিস্টেম সেটআপ' : 'System Setup', 
          desc: isBn ? 'API এবং সফটওয়্যার কনফিগারেশন করা হয়।' : 'API and software configuration is performed.' 
        },
        { 
          title: isBn ? 'ডেলিভারি' : 'Delivery', 
          desc: isBn ? 'টেস্টিং শেষে বুঝিয়ে দেওয়া হয়।' : 'Delivered after thorough testing.' 
        }
      ],
      benefits: isBn 
        ? ['সময় ও খরচ সাশ্রয়', 'নির্ভুল কাজ', 'প্রফেশনাল সাপোর্ট']
        : ['Save Time & Cost', 'Error-free Operation', 'Professional Support']
    },
    'messenger': {
      id: 'messenger',
      title: isBn ? 'মেসেঞ্জার চ্যাটবট' : 'Messenger Chatbot',
      subtitle: isBn ? 'ফেসবুক পেজের জন্য ২৪/৭ অটোমেটেড অ্যাসিস্ট্যান্ট' : '24/7 Automated Assistant for Facebook Page',
      howItWorks: [
        { 
          title: isBn ? 'অর্ডার গ্রহণ' : 'Order Taking', 
          desc: isBn ? 'কাস্টমার মেসেজে প্রোডাক্ট সিলেক্ট করলে বট অর্ডার নিয়ে নিবে।' : 'Bot takes orders when customers select products.' 
        },
        { 
          title: isBn ? 'রিপ্লাই ও গ্যালারি' : 'Reply & Gallery', 
          desc: isBn ? 'প্রোডাক্টের ছবি ও ডিটেইলস সুন্দর গ্যালারি আকারে দেখাবে।' : 'Shows product images and details in a beautiful gallery.' 
        },
        { 
          title: isBn ? 'ডাটা সেভিং' : 'Data Saving', 
          desc: isBn ? 'অর্ডার ডিটেইলস সরাসরি গুগল শিট বা ড্যাশবোর্ডে সেভ হবে।' : 'Order details saved directly to Google Sheets or Dashboard.' 
        }
      ],
      benefits: isBn
        ? ['সারাদিন কাস্টমার সাপোর্টের ঝামেলা নেই', 'অর্ডার মিস হওয়ার সম্ভাবনা শূন্য', 'ইনস্ট্যান্ট রিপ্লাইয়ে কাস্টমার খুশি থাকে']
        : ['No hassle of all-day support', 'Zero chance of missing orders', 'Customers happy with instant replies'],
      techSpecs: ['Facebook Graph API', 'Webhook Integration', 'AI NLP Support']
    },
    'instagram': {
       id: 'instagram',
       title: isBn ? 'ইনস্টাগ্রাম চ্যাটবট' : 'Instagram Chatbot',
       subtitle: isBn ? 'সোশ্যাল এনগেজমেন্ট ও সেলস বুস্টার' : 'Social Engagement & Sales Booster',
       howItWorks: [
         { title: isBn ? 'স্টোরি রিপ্লাই' : 'Story Reply', desc: isBn ? 'কেউ আপনার স্টোরিতে মেনশন করলে অটো রিপ্লাই যাবে।' : 'Auto-reply when someone mentions you in a story.' },
         { title: isBn ? 'কিওয়ার্ড ট্রিগার' : 'Keyword Trigger', desc: isBn ? 'নির্দিষ্ট শব্দ লিখলে ডিএম-এ অফার পাঠাবে।' : 'Sends offers in DM upon typing specific keywords.' },
         { title: isBn ? 'লিড জেনারেশন' : 'Lead Generation', desc: isBn ? 'ফলোয়ারদের ইমেইল ও ফোন নাম্বার কালেক্ট করবে।' : 'Collects emails and phone numbers from followers.' }
       ],
       benefits: isBn
         ? ['অর্গানিক রিচ বৃদ্ধি', 'ফলোয়ারদের সাথে অটোমেটেড যোগাযোগ', 'সেলস কনভার্শন বৃদ্ধি']
         : ['Increase organic reach', 'Automated communication with followers', 'Increased sales conversion'],
       techSpecs: ['Instagram Graph API', 'Ice Breakers', 'Story Mentions']
    },
    'whatsapp': {
       id: 'whatsapp',
       title: isBn ? 'হোয়াটসঅ্যাপ চ্যাটবট' : 'WhatsApp Chatbot',
       subtitle: isBn ? 'সবচেয়ে জনপ্রিয় মেসেজিং প্ল্যাটফর্মে অটোমেশন' : 'Automation on the most popular messaging platform',
       howItWorks: [
         { title: isBn ? 'মেনু ভিত্তিক চ্যাট' : 'Menu Based Chat', desc: isBn ? '১, ২, ৩ চেপে কাস্টমার সার্ভিস নিতে পারবে।' : 'Customers can get service by pressing 1, 2, 3.' },
         { title: isBn ? 'ব্রডকাস্টিং' : 'Broadcasting', desc: isBn ? 'হাজার হাজার কাস্টমারকে এক ক্লিকে অফার পাঠান।' : 'Send offers to thousands of customers in one click.' },
         { title: isBn ? 'ক্যাটালগ শো' : 'Catalog Show', desc: isBn ? 'সরাসরি হোয়াটসঅ্যাপে প্রোডাক্ট ক্যাটালগ দেখান।' : 'Show product catalogs directly on WhatsApp.' }
       ],
       benefits: isBn
         ? ['৯৮% ওপেন রেট', 'পার্সোনালাইজড অফার পাঠানো সহজ', 'গ্রিন টিক ভেরিফিকেশন সাপোর্ট']
         : ['98% Open Rate', 'Easy to send personalized offers', 'Green Tick Verification Support'],
       techSpecs: ['WhatsApp Cloud API', 'Template Messages', 'Interactive Buttons']
    }
  };
};

export const SYSTEM_INSTRUCTION = `
You are a helpful, professional AI assistant for "AutoMateIQ", a bangladeshi automation agency.
We offer specific packages: Messenger Chatbot, Instagram Chatbot, WhatsApp Chatbot, Facebook Auto Post, and YouTube Auto Post.
Answer in Bengali by default, or English if asked.
`;
