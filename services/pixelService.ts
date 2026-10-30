
export const trackPixelEvent = (eventName: string, data?: Record<string, any>) => {
  if (typeof window !== 'undefined' && (window as any).fbq) {
    (window as any).fbq('track', eventName, data);
    console.log(`✅ Facebook Pixel Event: ${eventName}`, data || '');
  } else {
    console.warn(`⚠️ Facebook Pixel not loaded. Event: ${eventName} was not tracked.`);
  }
};
