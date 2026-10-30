
export interface UserLocation {
  city: string;
  country_name: string;
  ip: string;
}

// Pure IP-based location fetch (No Pop-ups, No Permissions needed)
export const getUserLocation = async (): Promise<string> => {
  // Strategy 1: ipapi.co (Most reliable for simple JSON)
  try {
    const response = await fetch('https://ipapi.co/json/');
    const data = await response.json();
    if (data.city && data.country_name) {
      const loc = `${data.city}, ${data.country_name}`;
      console.log("✅ Location detected via IP:", loc);
      if (typeof sessionStorage !== 'undefined') {
        sessionStorage.setItem('cached_location', loc);
      }
      return loc;
    }
  } catch (error) {
    console.warn("ipapi.co failed, trying backup...", error);
  }

  // Strategy 2: ipwho.is (Excellent free backup)
  try {
    const response = await fetch('https://ipwho.is/');
    const data = await response.json();
    if (data.success && (data.city || data.country)) {
      const loc = `${data.city || ''}, ${data.country || ''}`;
      console.log("✅ Location detected via ipwho.is:", loc);
      if (typeof sessionStorage !== 'undefined') {
        sessionStorage.setItem('cached_location', loc);
      }
      return loc;
    }
  } catch (error) {
    console.warn("ipwho.is failed, trying last resort...", error);
  }

  // Strategy 3: ipinfo.io (Standard backup)
  try {
    const response = await fetch('https://ipinfo.io/json');
    const data = await response.json();
    if (data.city || data.country) {
      const loc = `${data.city}, ${data.country}`;
      return loc;
    }
  } catch (error) {
    console.error("❌ All location services failed:", error);
  }

  // Fallback if everything fails (e.g. adblockers)
  return 'Unknown Location';
};
