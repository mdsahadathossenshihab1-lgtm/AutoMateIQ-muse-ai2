package com.automateiq.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * v8.2 CLOSED-APP CHECK (WorkManager, periodic, no billing, no new
 * server). Runs about every 15 minutes with network (the minimum
 * WorkManager allows; phones' battery optimisation can delay it
 * further), even when the app is closed, and posts a standard system
 * notification for each genuinely new item:
 * (a) new customer messages on the signed-in admin's leads, and
 * (b) new pending orders (admin only).
 *
 * It shares the app's "notified" keys in SharedPreferences
 * ("automateiq_seen_v82"), so an item notified here is NOT notified
 * again by the app's own refresh, and vice versa. It NEVER touches
 * the "seen" keys: the in-app unread badge clears only when the user
 * actually opens the thread/order in the app.
 *
 * First-run rule (no spam for old data): if neither the app nor this
 * worker has ever seeded this account, the current state is recorded
 * silently and nothing is posted. After that, any change notifies.
 *
 * No signed-in user -> skip silently (success, no notification, no
 * crash). Any failure -> success too: the next scheduled run retries
 * anyway, so a retry loop is pointless here.
 */
public class UpdateWorker extends Worker {

    private static final String PREFS = "automateiq_seen_v82";
    private static final String CHANNEL_ID = "automateiq_updates";

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String uid = FirebaseAuthHelper.currentUid();
            if (uid == null) return Result.success();
            checkLeads(uid);
            if (FirebaseAuthHelper.isAdmin()) checkOrders(uid);
            return Result.success();
        } catch (Exception ignored) {
            return Result.success();
        }
    }

    private SharedPreferences prefs() {
        return getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String str(DocumentSnapshot doc, String field, String fallback) {
        String v = doc.getString(field);
        return v == null ? fallback : v;
    }

    /** Customer-message fingerprint, SAME formula as the app's
     *  leadFingerprint: customer messages only, so owner/bot replies
     *  never trip a notification. */
    private static String leadFingerprint(DocumentSnapshot doc) {
        int userCount = 0;
        String lastUser = "";
        Object msgs = doc.get("messages");
        if (msgs instanceof List) {
            for (Object m : (List<?>) msgs) {
                if (m instanceof Map) {
                    Map<?, ?> mm = (Map<?, ?>) m;
                    Object role = mm.get("role");
                    String r = role == null ? "user" : String.valueOf(role);
                    if ("user".equals(r)) {
                        Object t = mm.get("text");
                        lastUser = t == null ? "" : String.valueOf(t);
                        userCount++;
                    }
                }
            }
        }
        return userCount + "|" + lastUser;
    }

    private static String lastUserText(DocumentSnapshot doc) {
        String last = "";
        Object msgs = doc.get("messages");
        if (msgs instanceof List) {
            for (Object m : (List<?>) msgs) {
                if (m instanceof Map) {
                    Map<?, ?> mm = (Map<?, ?>) m;
                    Object role = mm.get("role");
                    String r = role == null ? "user" : String.valueOf(role);
                    if ("user".equals(r)) {
                        Object t = mm.get("text");
                        if (t != null) last = String.valueOf(t);
                    }
                }
            }
        }
        return last;
    }

    private static String shortPreview(String text) {
        if (text == null) return "";
        String t = text.trim();
        if (t.length() > 40) return t.substring(0, 40) + "…";
        return t;
    }

    private void checkLeads(String uid) throws Exception {
        QuerySnapshot snap = Tasks.await(
            FirebaseAuthHelper.firestore()
                .collection(LiveDataClient.LEADS_COLLECTION)
                .whereEqualTo("ownerId", uid).get(),
            60, TimeUnit.SECONDS);
        SharedPreferences prefs = prefs();
        boolean appSeeded = prefs.getBoolean("leads_seeded_" + uid, false);
        boolean workerSeeded = prefs.getBoolean("worker_leads_seeded_" + uid, false);
        boolean firstRun = !appSeeded && !workerSeeded;
        SharedPreferences.Editor ed = prefs.edit();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            String pageId = str(doc, "pageId", LiveDataClient.PAGE_ID);
            String psid = str(doc, "psid", doc.getId());
            String docId = pageId + "_" + psid;
            String fp = leadFingerprint(doc);
            String key = "lead_notified_" + uid + "_" + docId;
            if (firstRun) {
                // Silent seed: old messages are not "new".
                ed.putString(key, fp);
                continue;
            }
            String old = prefs.getString(key, null);
            if (old == null || !old.equals(fp)) {
                ed.putString(key, fp);
                // Synchronous write: shrink the window where an app
                // refresh running at the same time could re-notify.
                ed.commit();
                ed = prefs.edit();
                postNotification("নতুন মেসেজ: " + str(doc, "name", "কাস্টমার"),
                    shortPreview(lastUserText(doc)),
                    "lead_" + docId, docId, null);
            }
        }
        if (firstRun) {
            ed.putBoolean("worker_leads_seeded_" + uid, true);
            ed.commit();
        }
    }

    private void checkOrders(String uid) throws Exception {
        QuerySnapshot snap = Tasks.await(
            FirebaseAuthHelper.firestore()
                .collection(FirebaseAuthHelper.ORDERS_COLLECTION).get(),
            60, TimeUnit.SECONDS);
        SharedPreferences prefs = prefs();
        boolean appSeeded = prefs.getBoolean("orders_seeded_" + uid, false);
        boolean workerSeeded = prefs.getBoolean("worker_orders_seeded_" + uid, false);
        boolean firstRun = !appSeeded && !workerSeeded;
        Set<String> notified = prefIdSet(prefs, "order_notified_" + uid);
        boolean changed = false;
        for (DocumentSnapshot doc : snap.getDocuments()) {
            if (!"pending".equals(str(doc, "status", "pending"))) continue;
            String id = doc.getId();
            if (firstRun) {
                // Silent seed: old orders are not "new".
                notified.add(id);
                changed = true;
                continue;
            }
            if (!notified.contains(id)) {
                notified.add(id);
                changed = true;
                prefPutIdSet("order_notified_" + uid, notified);
                String amount = str(doc, "amount", "");
                if (amount.length() > 0 && !amount.startsWith("৳")) amount = "৳" + amount;
                postNotification("নতুন অর্ডার: " + str(doc, "serviceName", ""),
                    amount.length() > 0 ? amount : "নতুন অর্ডার এসেছে",
                    "order_" + id, null, id);
            }
        }
        if (changed) prefPutIdSet("order_notified_" + uid, notified);
        if (firstRun) prefs.edit().putBoolean("worker_orders_seeded_" + uid, true).apply();
    }

    private Set<String> prefIdSet(SharedPreferences prefs, String key) {
        Set<String> out = new HashSet<String>();
        String raw = prefs.getString(key, "");
        if (raw == null || raw.length() == 0) return out;
        for (String part : raw.split(",")) {
            if (part.length() > 0) out.add(part);
        }
        return out;
    }

    private void prefPutIdSet(String key, Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        prefs().edit().putString(key, sb.toString()).commit();
    }

    /** Standard system notification (system sound/vibration only).
     *  Tapping opens the app straight into the lead thread / order
     *  detail via intent extras handled by MainActivity. */
    private void postNotification(String title, String body, String tag,
                                  String openLeadDoc, String openOrderId) {
        try {
            Context ctx = getApplicationContext();
            if (Build.VERSION.SDK_INT >= 33
                    && ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) return;
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                    "নতুন খবর", NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription("নতুন মেসেজ আর অর্ডারের খবর");
                nm.createNotificationChannel(ch);
            }
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (openLeadDoc != null) intent.putExtra("open_lead_doc", openLeadDoc);
            if (openOrderId != null) intent.putExtra("open_order_id", openOrderId);
            // Distinct request code per item, so one notification's tap
            // target can never be overwritten by another's.
            int code = (Math.abs(tag.hashCode()) % 100000) + 1000;
            PendingIntent pi = PendingIntent.getActivity(ctx, code, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= 26) b = new Notification.Builder(ctx, CHANNEL_ID);
            else b = new Notification.Builder(ctx);
            int icon = ctx.getResources().getIdentifier("ic_bell", "drawable", ctx.getPackageName());
            if (icon == 0) icon = android.R.drawable.ic_dialog_info;
            b.setContentTitle(title).setContentText(body).setSmallIcon(icon)
                .setContentIntent(pi).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);
            nm.notify(tag, code, b.build());
        } catch (Exception ignored) { }
    }
}
