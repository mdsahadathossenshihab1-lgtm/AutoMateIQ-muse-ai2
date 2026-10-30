import { GoogleGenAI } from "@google/genai";
import { SYSTEM_INSTRUCTION } from '../constants';

// We initialize strictly inside the function to avoid top-level "process is not defined" errors in some browser environments
export const sendMessageToGemini = async (userMessage: string): Promise<string> => {
  try {
    // Safe access to API Key
    const apiKey = typeof process !== 'undefined' ? process.env.API_KEY : '';
    
    if (!apiKey) {
      console.warn("Gemini API Key is missing");
      return "আমি দুঃখিত, সিস্টেম কনফিগারেশনে সমস্যা হয়েছে।";
    }

    const aiClient = new GoogleGenAI({ apiKey });

    const response = await aiClient.models.generateContent({
      model: 'gemini-2.5-flash',
      contents: userMessage,
      config: {
        systemInstruction: SYSTEM_INSTRUCTION,
      }
    });

    if (response.text) {
      return response.text;
    }
    return "আমি দুঃখিত, আমি আপনার প্রশ্নের উত্তর দিতে পারছি না।";
  } catch (error) {
    console.error("Error communicating with Gemini:", error);
    return "একটি প্রযুক্তিগত ত্রুটি হয়েছে। দয়া করে পরে আবার চেষ্টা করুন।";
  }
};