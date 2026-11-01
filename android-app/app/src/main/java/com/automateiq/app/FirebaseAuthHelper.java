package com.automateiq.app;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * REAL IN-APP Firebase wiring for AutoMateIQ v4.
 *
 * LOGIN (never a website redirect): Google Sign-In runs inside the app
 * and signs into Firebase Auth with the Google ID token. The login
 * button in MainActivity calls startGoogleSignIn() and handles the
 * result with handleSignInResult() — it never opens a browser for login.
 *
 * FIREBASE CONFIG GATE: isFirebaseReady() is true only when the
 * google-services plugin generated default_web_client_id, i.e. only
 * when the REAL google-services.json from Firebase Console is present
 * in app/ at build time. Without it the app shows the in-app
 * "Firebase setup pending" message and does NOT fall back to the
 * website. Adding google-services.json later needs NO code change.
 *
 * FIRESTORE (same as the website): project
 * project-d28c75fa-a3af-48dc-ac6, NAMED database
 * ai-studio-automateiq-a1b8237c-c72f-49f9-8d26-7de394824bb1, collection
 * messenger_leads, filtered by ownerId == signed-in Firebase UID.
 * Never the (default) database. Never any token/key in the app.
 *
 * REPLY: owner replies go through the Render /messenger-reply endpoint
 * with { businessId, pageId, recipientPsid, message } — the same
 * contract as the website. The Page Access Token lives on the server.
 */
public class FirebaseAuthHelper {

    public static final int RC_GOOGLE_SIGN_IN = 9001;

    /** Website Firestore named database id (see LiveDataClient). */
    public static final String DATABASE_ID = LiveDataClient.DATABASE_ID;

    public interface ResultCallback<T> {
        void onSuccess(T value);
        void onError(String message);
    }

    /** True only when the real google-services.json was present at build time. */
    public static boolean isFirebaseReady(Activity activity) {
        int id = activity.getResources().getIdentifier(
            "default_web_client_id", "string", activity.getPackageName());
        return id != 0;
    }

    /** Signed-in Firebase user, or null. Email is never displayed by the app. */
    public static FirebaseUser currentUser() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }

    public static String currentUid() {
        FirebaseUser u = currentUser();
        return u == null ? null : u.getUid();
    }

    public static String currentDisplayName() {
        FirebaseUser u = currentUser();
        if (u == null) return "";
        String n = u.getDisplayName();
        return n == null ? "" : n;
    }

    /** Start in-app Google Sign-In. Call only after isFirebaseReady(). */
    public static void startGoogleSignIn(Activity activity) {
        int id = activity.getResources().getIdentifier(
            "default_web_client_id", "string", activity.getPackageName());
        String webClientId = activity.getString(id);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build();
        GoogleSignInClient client = GoogleSignIn.getClient(activity, gso);
        activity.startActivityForResult(client.getSignInIntent(), RC_GOOGLE_SIGN_IN);
    }

    /** Handle the Google Sign-In result; signs into Firebase Auth in-app. */
    public static void handleSignInResult(Activity activity, Intent data,
                                          final ResultCallback<FirebaseUser> cb) {
        Task<GoogleSignInAccount> task =
            GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                cb.onError("Google লগইন সম্পন্ন হয়নি। আবার চেষ্টা করুন।");
                return;
            }
            AuthCredential credential =
                GoogleAuthProvider.getCredential(account.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override public void onSuccess(AuthResult result) {
                        cb.onSuccess(result.getUser());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override public void onFailure(Exception e) {
                        cb.onError("Firebase লগইন হয়নি: " + e.getMessage());
                    }
                });
        } catch (Exception e) {
            // ApiException (cancelled/failed Google sign-in) and any other
            // failure from getResult land here; the user-facing message
            // stays the same either way.
            cb.onError("Google লগইন বাতিল বা ব্যর্থ হয়েছে। আবার চেষ্টা করুন।");
        }
    }

    public static void signOut(Activity activity) {
        try { FirebaseAuth.getInstance().signOut(); } catch (Exception ignored) { }
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN).build();
            GoogleSignIn.getClient(activity, gso).signOut();
        } catch (Exception ignored) { }
    }

    /** Named-database Firestore instance for the website database. */
    public static FirebaseFirestore firestore() {
        return FirebaseFirestore.getInstance(
            FirebaseApp.getInstance(), DATABASE_ID);
    }

    private static String safeString(DocumentSnapshot doc, String field, String fallback) {
        String v = doc.getString(field);
        return v == null ? fallback : v;
    }

    private static String formatUpdatedAt(DocumentSnapshot doc) {
        Object o = doc.get("updated_at");
        if (o instanceof String) return (String) o;
        if (o instanceof Timestamp) {
            Date d = ((Timestamp) o).toDate();
            return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(d);
        }
        return "";
    }

    /** Live leads for this owner only (ownerId == uid). No orderBy in the
     *  query (avoids a composite-index requirement); results are sorted
     *  client-side only if timestamps parse, otherwise kept in server order. */
    public static void fetchLiveLeads(String uid,
                                      final ResultCallback<List<LeadsStore.Lead>> cb) {
        firestore().collection(LiveDataClient.LEADS_COLLECTION)
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override public void onSuccess(QuerySnapshot snap) {
                    List<LeadsStore.Lead> out = new ArrayList<LeadsStore.Lead>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String pageId = safeString(doc, "pageId", LiveDataClient.PAGE_ID);
                        String psid = safeString(doc, "psid", doc.getId());
                        String name = safeString(doc, "name", "কাস্টমার");
                        String status = safeString(doc, "status", "new");
                        String last = safeString(doc, "lastMessage", "");
                        Boolean pausedObj = doc.getBoolean("paused");
                        boolean paused = pausedObj != null && pausedObj;
                        if (paused) status = "handover";
                        LeadsStore.Lead lead = new LeadsStore.Lead(
                            pageId, psid, name, status, last,
                            formatUpdatedAt(doc), paused);
                        Object msgs = doc.get("messages");
                        if (msgs instanceof List) {
                            for (Object m : (List<?>) msgs) {
                                if (m instanceof Map) {
                                    Map<?, ?> mm = (Map<?, ?>) m;
                                    Object role = mm.get("role");
                                    Object text = mm.get("text");
                                    Object created = mm.get("created_at");
                                    lead.messages.add(new LeadsStore.Message(
                                        role == null ? "user" : String.valueOf(role),
                                        text == null ? "" : String.valueOf(text),
                                        created == null ? "" : String.valueOf(created)));
                                }
                            }
                        }
                        out.add(lead);
                    }
                    cb.onSuccess(out);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override public void onFailure(Exception e) {
                    cb.onError("লিড লোড হয়নি: " + e.getMessage());
                }
            });
    }

    /** Update paused/status on the lead document (Take Over / Resume AI). */
    public static void updateLeadStatus(LeadsStore.Lead lead, boolean paused,
                                        String status,
                                        final ResultCallback<Boolean> cb) {
        firestore().collection(LiveDataClient.LEADS_COLLECTION)
            .document(lead.docId())
            .update("paused", paused, "status", status)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override public void onSuccess(Void v) { cb.onSuccess(true); }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override public void onFailure(Exception e) {
                    cb.onError("স্ট্যাটাস আপডেট হয়নি: " + e.getMessage());
                }
            });
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    /** Send an owner reply via Render (server holds the Page token). */
    public static void sendLiveReply(final Activity activity, final String businessId,
                                     final LeadsStore.Lead lead, final String message,
                                     final ResultCallback<Boolean> cb) {
        new Thread(new Runnable() {
            @Override public void run() {
                boolean ok = false;
                String err = "";
                try {
                    URL url = new URL(LiveDataClient.RENDER_REPLY_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setDoOutput(true);
                    String body = "{\"businessId\":\"" + jsonEscape(businessId)
                        + "\",\"pageId\":\"" + jsonEscape(lead.pageId)
                        + "\",\"recipientPsid\":\"" + jsonEscape(lead.psid)
                        + "\",\"message\":\"" + jsonEscape(message) + "\"}";
                    OutputStream os = conn.getOutputStream();
                    os.write(body.getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    int code = conn.getResponseCode();
                    ok = code >= 200 && code < 300;
                    if (!ok) err = "সার্ভার থেকে উত্তর আসেনি (কোড " + code + ")";
                    conn.disconnect();
                } catch (Exception e) {
                    err = "রিপ্লাই পাঠানো যায়নি: " + e.getMessage();
                }
                final boolean result = ok;
                final String error = err;
                activity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        if (result) cb.onSuccess(true);
                        else cb.onError(error);
                    }
                });
            }
        }).start();
    }
}
