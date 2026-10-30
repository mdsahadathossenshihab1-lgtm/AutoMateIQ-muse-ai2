import React, { useState, useRef, useEffect } from 'react';
import { MessageCircle, X, Send, Bot, Headphones } from 'lucide-react';
import { sendMessageToGemini } from '../services/geminiService';
import { ChatMessage } from '../types';
import { CONTACT_INFO } from '../constants';

const ChatAssistant: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([
    { role: 'model', text: 'স্বাগতম AutoMateIQ-তে! আমি আপনার এআই অ্যাসিস্ট্যান্ট। অটোমেশন সার্ভিস সম্পর্কে আপনার কোনো প্রশ্ন থাকলে করতে পারেন।' }
  ]);
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const toggleChat = () => setIsOpen(!isOpen);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, isOpen]);

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userText = input;
    setInput('');
    
    // Add user message
    setMessages(prev => [...prev, { role: 'user', text: userText }]);
    setIsLoading(true);

    try {
      // Get AI response
      const responseText = await sendMessageToGemini(userText);
      setMessages(prev => [...prev, { role: 'model', text: responseText }]);
    } catch (error) {
      setMessages(prev => [...prev, { role: 'model', text: 'দুঃখিত, কিছু একটা সমস্যা হয়েছে।', isError: true }]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSend();
    }
  };

  const handleAgentClick = () => {
     window.open(`https://wa.me/${CONTACT_INFO.whatsapp}`, '_blank');
  };

  return (
    <div className="fixed bottom-6 right-6 z-50">
      {!isOpen && (
        <button
          onClick={toggleChat}
          className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white rounded-full p-4 shadow-[0_0_20px_rgba(59,130,246,0.5)] transition-all duration-300 transform hover:scale-110 flex items-center gap-2 group border border-blue-400/30"
        >
          <Bot size={28} className="animate-bounce-slow" />
          <span className="max-w-0 overflow-hidden group-hover:max-w-xs transition-all duration-500 ease-in-out whitespace-nowrap font-bold">
            সাহায্য প্রয়োজন?
          </span>
        </button>
      )}

      {isOpen && (
        <div className="bg-white rounded-2xl shadow-2xl w-80 sm:w-96 flex flex-col border border-gray-200 overflow-hidden transition-all duration-300" style={{ maxHeight: '600px', height: '80vh' }}>
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-700 p-4 flex justify-between items-center text-white">
            <div className="flex items-center gap-2">
              <Bot size={24} />
              <div>
                <h3 className="font-bold">AutoMateIQ AI</h3>
                <span className="text-xs text-blue-100 flex items-center gap-1">
                  <span className="w-2 h-2 bg-white rounded-full animate-pulse"></span>
                  অনলাইন
                </span>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {/* Agent Button */}
              <button 
                onClick={handleAgentClick}
                className="bg-white/20 hover:bg-white/30 p-1.5 rounded-lg transition-colors text-xs flex items-center gap-1"
                title="এজেন্টের সাথে কথা বলুন"
              >
                <Headphones size={16} />
              </button>
              <button onClick={toggleChat} className="text-white/80 hover:text-white transition-colors">
                <X size={24} />
              </button>
            </div>
          </div>

          {/* Messages Area */}
          <div className="flex-1 overflow-y-auto p-4 bg-gray-50 space-y-4">
            {messages.map((msg, idx) => (
              <div
                key={idx}
                className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div
                  className={`max-w-[80%] p-3 rounded-2xl text-sm leading-relaxed ${
                    msg.role === 'user'
                      ? 'bg-blue-600 text-white rounded-br-none'
                      : 'bg-white border border-gray-200 text-gray-800 rounded-bl-none shadow-sm'
                  } ${msg.isError ? 'bg-red-50 text-red-600 border-red-200' : ''}`}
                >
                  <div dangerouslySetInnerHTML={{ __html: msg.text.replace(/\n/g, '<br/>') }} />
                </div>
              </div>
            ))}
            {isLoading && (
              <div className="flex justify-start">
                <div className="bg-white border border-gray-200 p-3 rounded-2xl rounded-bl-none shadow-sm">
                  <div className="flex space-x-1">
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
                    <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          {/* Agent CTA inside chat */}
          <div className="bg-blue-50 px-4 py-2 flex justify-between items-center border-t border-blue-100">
             <span className="text-xs text-blue-700">আরো সাহায্য লাগবে?</span>
             <button 
               onClick={handleAgentClick}
               className="text-xs font-bold text-blue-700 flex items-center gap-1 hover:underline"
             >
               <Headphones size={12} /> এজেন্টের সাথে কথা বলুন
             </button>
          </div>

          {/* Input Area */}
          <div className="p-4 bg-white border-t border-gray-100">
            <div className="flex items-center gap-2 bg-gray-100 rounded-full px-4 py-2 border border-transparent focus-within:border-blue-500 focus-within:bg-white transition-colors">
              <input
                type="text"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={handleKeyPress}
                placeholder="প্রশ্ন লিখুন..."
                className="flex-1 bg-transparent outline-none text-sm text-gray-800 placeholder-gray-500"
                disabled={isLoading}
              />
              <button
                onClick={handleSend}
                disabled={!input.trim() || isLoading}
                className={`p-2 rounded-full transition-colors ${
                  input.trim() && !isLoading
                    ? 'text-blue-600 hover:bg-blue-50'
                    : 'text-gray-400 cursor-not-allowed'
                }`}
              >
                <Send size={20} />
              </button>
            </div>
            <div className="text-center mt-2">
              <span className="text-[10px] text-gray-400">Powered by Gemini AI</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ChatAssistant;