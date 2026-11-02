package com.automateiq.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * v8.2 FCM RECEIVER — READY, NOT LIVE YET. This service can receive
 * and display a true instant push the moment a sender exists, and it
 * keeps the device token current on the signed-in user's doc (see
 * onNewToken + MainActivity.storeFcmToken). It holds NO server key,
 * NO service-account secret, and NO sender of its own.
 *
 * HONEST LIMIT: with no sender wired yet (no Cloud Function, no
 * Render Admin-SDK sender), FCM alone sends NOTHING. Until that
 * separate sender step lands, closed-app delivery in v8.2 is the
 * WorkManager periodic check (UpdateWorker), not this service.
 * When the sender does land, it sends DATA messages:
 *   lead:  {type:"lead", docId, name, preview}
 *   order: {type:"order", orderId, service, amount}
 * Tapping opens the matching thread / order detail in the app.
 */
public class AutoMateIQMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "automateiq_updates";

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map<String, String> d = message.getData();
        String type = d != null ? d.get("type") : null;
        String title;
        String body;
        String leadDoc = null;
        String orderId = null;
        if ("lead".equals(type)) {
            title = "নতুন মেসেজ: " + nz(d.get("name"), "কাস্টমার");
            body = nz(d.get("preview"), "");
            leadDoc = d.get("docId");
        } else if ("order".equals(type)) {
            title = "নতুন অর্ডার: " + nz(d.get("service"), "");
            String amount = nz(d.get("amount"), "");
            if (amount.length() > 0 && !amount.startsWith("৳")) amount = "৳" + amount;
            body = amount.length() > 0 ? amount : "নতুন অর্ডার এসেছে";
            orderId = d.get("orderId");
        } else if (message.getNotification() != null) {
            title = nz(message.getNotification().getTitle(), "নতুন খবর");
            body = nz(message.getNotification().getBody(), "");
        } else {
            return;
        }
        postNotification(title, body, leadDoc, orderId);
    }

    @Override
    public void onNewToken(String token) {
        // Best-effort: keep the signed-in user's doc current so a
        // future sender can target this device. Silent on failure.
        try {
            String uid = FirebaseAuthHelper.currentUid();
            if (uid == null || token == null || token.length() == 0) return;
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("fcmToken", token);
            FirebaseAuthHelper.updateUserPageFields(uid, fields,
                new FirebaseAuthHelper.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean ok) { }
                    @Override public void onError(String e) { }
                });
        } catch (Exception ignored) { }
    }

    private static String nz(String v, String fallback) {
        return v == null ? fallback : v;
    }

    private void postNotification(String title, String body, String leadDoc, String orderId) {
        try {
            Context ctx = this;
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
            if (leadDoc != null && leadDoc.length() > 0) intent.putExtra("open_lead_doc", leadDoc);
            if (orderId != null && orderId.length() > 0) intent.putExtra("open_order_id", orderId);
            String tag = leadDoc != null ? "fcm_lead_" + leadDoc
                : orderId != null ? "fcm_order_" + orderId : "fcm_general";
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
