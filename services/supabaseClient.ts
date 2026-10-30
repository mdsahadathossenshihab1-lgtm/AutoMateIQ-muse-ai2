// Re-export Firebase Client for backwards compatibility
import { db, auth, dbFirestore } from './firebaseClient';

export { db, auth, dbFirestore };
export const supabase = null;
