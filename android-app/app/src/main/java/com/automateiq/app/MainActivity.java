package com.automateiq.app;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import android.animation.ObjectAnimator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AutoMateIQ Mobile Dashboard v8.2 — SAME-TO-SAME to approved design v3 (UI unchanged from v8.1). v8.2 fixes: send status follows the reply service answer (never the follow-up reload), per-thread in-flight guard, unknown-outcome refresh-first flow, in-app notifications (banner + nav badges + local notification; no closed-app push), and plain-language page onboarding with an optional Page ID and an empty admin profile link placeholder.
 * (2026-11-02): aurora/glass skin, vector icons, v3 typography scale and
 * subtle entry/press/progress animations, with plain user language in
 * every visible string. Backend kept from v7: role gating (admin panel
 * vs customer panel), orders, subscription derived from monthly orders,
 * wallet / API Credit recharge, leads + chat thread, customers, page
 * records with user-step onboarding, and auto-expire on admin load.
 * No sample content exists in this build: empty data shows a designed
 * empty state, and a failed load shows an error state with Retry.
 */
public class MainActivity extends Activity {

    // ---- Design accesss (app palette) ----
    private static final int PRIMARY = 0xFF2563EB;
    private static final int PRIMARY_DARK = 0xFF1E3A8A;
    private static final int NAVY = 0xFF172554;
    private static final int VIOLET = 0xFF8B5CF6;
    private static final int CYAN = 0xFF06B6D2;
    // ---- v3 EXACT colours (design-prototype-v3.html, read 2026-11-02) ----
    private static final int V3_VIOLET_DARK = 0xFF6D28D9; // appbar/hero start
    private static final int V3_VIOLET = 0xFF7C3AED;      // label, nav-selected, btn start
    private static final int V3_BLUE = 0xFF2563EB;        // appbar/hero mid (= PRIMARY)
    private static final int V3_CYAN_DARK = 0xFF0891B2;   // appbar end
    private static final int V3_CYAN = 0xFF06B6D2;        // hero end, btn end
    private static final int V3_PINK = 0xFFEC4899;        // heroTop end, send start
    private static final int V3_BODY_BG = 0xFFF0F4FF;     // phone body
    private static final int V3_GLASS = 0xC7FFFFFF;       // rgba(255,255,255,.78)
    private static final int V3_GLASS_BORDER = 0xD9FFFFFF; // rgba(255,255,255,.85)
    private static final int V3_NAV_GREY = 0xFF94A3B8;
    private static final int V3_INDIGO_TEXT = 0xFF4F46E5; // btn2 text
    private static final int V3_INPUT_BORDER = 0x66A78BFA; // rgba(167,139,250,.4)
    private static final int V3_TIMELINE_LINE = 0x73A78BFA; // rgba(167,139,250,.45)
    private static final int V3_GREY_FG = 0xFF475569;
    private static final int V3_BOT_BG = 0xFFE0F2FE;
    private static final int V3_BOT_BORDER = 0xFFBAE6FD;
    private static final int V3_DANGER_BG = 0xFFFFF1F2;
    private static final int V3_DANGER_BORDER = 0xFFFECDD3;
    private static final int V3_DANGER_FG = 0xFFBE123C;
    private static final int V3_LOGIN_TOP = 0xFF4C1D95;
    private static final int BG = 0xFFF0F4FF;
    private static final int SURFACE = 0xFFFFFFFF;
    private static final int TEXT = 0xFF0F172A;
    private static final int TEXT2 = 0xFF64748B;
    private static final int BORDER = 0xFFE2E8F0;
    private static final int SOFT = 0xFFEFF6FF;
    private static final int SOFT_BLUE = 0xFFDBEAFE;
    private static final int GREEN = 0xFF15803D; // v3 chip fg
    private static final int GREEN_SOFT = 0xFFDCFCE7;
    private static final int AMBER = 0xFFB45309; // v3 chip fg
    private static final int AMBER_SOFT = 0xFFFEF3C7;
    private static final int RED = 0xFFB91C1C; // v3 chip fg
    private static final int TL_GREEN = 0xFF059669; // v3 timeline dot
    private static final int TL_AMBER = 0xFFD97706; // (kept) / v3 dot amber below
    private static final int TL_AMBER_DOT = 0xFFF59E0B; // v3 timeline dot
    private static final int TL_ROSE = 0xFFF43F5E; // v3 timeline dot
    private static final int RED_SOFT = 0xFFFEE2E2;
    private static final int PINK = 0xFFEC4899;
    private static final int FUCHSIA = 0xFFD946EF;
    private static final int TEAL = 0xFF14B8A6;
    private static final int AURORA_TOP = 0xFFEDE9FE;
    private static final int AURORA_MID = 0xFFECFEFF;
    private static final int AURORA_BOT = 0xFFFCE7F3;
    private static final int GLASS = 0xC7FFFFFF; // v3 .78 white

    // ---- Screens ----
    private static final int S_LOGIN = 0;
    private static final int S_PENDING = 1;
    private static final int S_DASH = 2;
    private static final int S_LEADS = 3;
    private static final int S_LEAD_DETAIL = 4;
    private static final int S_CONNECT = 5;
    private static final int S_SERVICES = 6;
    private static final int S_SERVICE_DETAIL = 7;
    private static final int S_SUBS = 8;
    private static final int S_TOKENS = 9;
    private static final int S_MY_SUB = 10;
    private static final int S_ABOUT = 11;
    private static final int S_ORDERS = 12;
    private static final int S_ORDER_DETAIL = 13;
    private static final int S_ORDER_FORM = 14;
    private static final int S_RECHARGE_FORM = 15;
    // v7 ROLE PANELS: admin-only screens. Customer-only screens are
    // S_SERVICES / S_SERVICE_DETAIL / S_SUBS / S_TOKENS / S_MY_SUB /
    // S_ORDER_FORM / S_RECHARGE_FORM. S_ORDERS and S_ORDER_DETAIL are
    // shared but role-filtered inside (admin = all orders + status
    // buttons; customer = own orders only, no status buttons).
    private static final int S_CUSTOMERS = 16;
    private static final int S_CUSTOMER_DETAIL = 17;
    private static final int S_PAGES = 18;
    private static final int S_PAGE_FORM = 19;

    private LinearLayout root;
    private LinearLayout topBar;
    private ScrollView scroll;
    private LinearLayout content;
    private LinearLayout bottomNav;
    // v5 THREAD SHELL: the thread screen does NOT live inside the main
    // ScrollView. threadPane is a separate vertical layout: header (fixed),
    // threadScroll (its own ScrollView, weight 1), composer (fixed, pinned
    // at the bottom). This is what makes "open at latest message" and
    // "composer never scrolls away" possible.
    private LinearLayout threadPane;
    private ScrollView threadScroll;
    private LinearLayout threadMessages;
    private EditText threadInput;

    private int screen = S_LOGIN;
    private List<Integer> backStack = new ArrayList<Integer>();

    private LeadsStore leadsStore = new LeadsStore();
    private SubscriptionStore.Subscription mySubscription = null;

    private boolean signedIn = false;
    private boolean leadsLoading = false;
    private String leadsError = null;
    private String loginError = null;
    private String notice = null;
    private String currentFilter = "all";
    private LeadsStore.Lead currentLead = null;
    private Service currentService = null;

    // v6 ORDERS: live list from database 'orders' (same as app).
    private List<OrderStore.Order> orders = new ArrayList<OrderStore.Order>();
    private boolean ordersLoaded = false;
    private boolean ordersLoading = false;
    private String ordersError = null;
    private String orderFilter = "all";
    private OrderStore.Order currentOrder = null;
    private OrderStore.OrderPackage selectedPackage = null;
    private String selectedTokenAmount = null;
    // Status of the monthly order behind mySubscription (for the
    // "awaiting admin approval" note on a pending subscription).
    private String mySubscriptionOrderStatus = "";
    // v7 ADMIN: customers + page records + auto-expire state.
    private java.util.List<UsersStore.User> users = new java.util.ArrayList<UsersStore.User>();
    private boolean usersLoaded = false;
    private boolean usersLoading = false;
    private String usersError = null;
    private UsersStore.User currentCustomer = null;
    // Page record being edited in S_PAGE_FORM (null = new record).
    private UsersStore.User pendingPageUser = null;
    // Session-only page records whose service save failed or whose
    // userId was not found — shown flagged, never as saved records.
    private java.util.List<UsersStore.User> localPageRecords = new java.util.ArrayList<UsersStore.User>();
    private int autoExpireCount = 0;
    private boolean autoExpireRunForLoad = false;

    // ---- v8.2: SEND GUARD + IN-APP NOTIFICATIONS + ONBOARDING ----
    // Admin profile link, supplied by the user 2026-11-02 08:31: his
    // own Facebook profile. The onboarding button opens EXACTLY this
    // link — never a guessed link, never a screenshot. The empty-link
    // fallback below stays only as a safety net if this is ever
    // cleared again.
    private static final String ADMIN_PROFILE_LINK = "https://www.facebook.com/ShShihab63";
    private static final int RC_NOTIFICATIONS = 9002;
    private static final String NOTIF_CHANNEL_ID = "automateiq_updates";
    // Per-thread in-flight sends (docId set): duplicate taps for the
    // same thread are ignored; other threads are unaffected.
    private java.util.Set<String> sendInFlightDocs = new java.util.HashSet<String>();
    // Unread state for badges (in-memory; recomputed from stored
    // seen/notified fingerprints on every load, so it survives restart).
    private java.util.Set<String> unreadLeadDocIds = new java.util.HashSet<String>();
    private java.util.Set<String> unreadOrderIds = new java.util.HashSet<String>();
    // One banner per new item: shown until tapped or its thread/order
    // is opened. Never re-raised for the same item (see detect methods).
    private String pendingBannerText = null;
    private LeadsStore.Lead pendingBannerLead = null;
    private OrderStore.Order pendingBannerOrder = null;
    // v8.2 closed-app: notification tap targets, delivered via intent
    // extras (worker / FCM service). Opened once the matching load
    // finishes; cleared if the item is not found for this account.
    private String pendingOpenLeadDoc = null;
    private String pendingOpenOrderId = null;

    // ---- Services (verified app prices only) ----
    private static class Service {
        String icon, name, desc, from, color;
        String[] prices;
        String[] benefits;
        Service(String icon, String name, String desc, String from,
                String color, String[] prices, String[] benefits) {
            this.icon = icon; this.name = name; this.desc = desc;
            this.from = from; this.color = color;
            this.prices = prices; this.benefits = benefits;
        }
    }

    private static final Service[] SERVICES = new Service[] {
        new Service("ic_chat", "মেসেঞ্জার চ্যাটবট",
            "ফেসবুক পেজে কাস্টমার মেসেজ করলে ২৪ ঘণ্টা স্বয়ংক্রিয় উত্তর দেবে, মালিক অনলাইনে না থাকলেও।",
            "৳৫০ থেকে শুরু", "#2563EB",
            new String[]{"ট্রায়াল ৭ দিন — ৳৫০", "স্ট্যান্ডার্ড — ৳৭০০/মাস",
                         "প্রিমিয়াম — ৳১,০০০/মাস", "এককালীন — ৳৫,০০০"},
            new String[]{"২৪ ঘণ্টা অটো রিপ্লাই", "মানুষের কাছে হ্যান্ডওভার",
                         "এককালীন প্যাকেজে ৳৫০০ ফ্রি ক্রেডিট"}),
        new Service("ic_camera", "ইনস্টাগ্রাম চ্যাটবট",
            "ইনস্টাগ্রাম ডিএম এবং স্টোরি রিপ্লাই অটোমেশন। বিজনেস গ্রোথ এবং এনগেজমেন্ট বৃদ্ধি।",
            "৳৫০ থেকে শুরু", "#8B5CF6",
            new String[]{"ট্রায়াল — ৳৫০", "স্ট্যান্ডার্ড — ৳৭০০/মাস",
                         "প্রিমিয়াম — ৳১,০০০/মাস", "এককালীন — ৳৫,০০০"},
            new String[]{"ইনস্টাগ্রাম ডিএম অটো রিপ্লাই", "স্টোরি রিপ্লাই অটোমেশন"}),
        new Service("ic_chat", "হোয়াটসঅ্যাপ বিজনেস বট",
            "হোয়াটসঅ্যাপে কাস্টমারের সাথে স্বয়ংক্রিয় কথোপকথন ও ক্যাটালগ দেখানো।",
            "৳৫০ থেকে শুরু", "#16A34A",
            new String[]{"ট্রায়াল — ৳৫০", "স্ট্যান্ডার্ড সেটআপ — ৳১,৫০০",
                         "এককালীন — ৳৫,০০০"},
            new String[]{"হোয়াটসঅ্যাপে প্রোডাক্ট ক্যাটালগ দেখানো", "স্বয়ংক্রিয় কথোপকথন"}),
        new Service("ic_page", "ফেসবুক অটো পোস্ট",
            "ফেসবুক পেজে স্বয়ংক্রিয়ভাবে পোস্ট প্রকাশ করা।",
            "৳৫০ থেকে শুরু", "#1D4ED8",
            new String[]{"ট্রায়াল — ৳৫০", "মাসিক — ৳১,০০০", "এককালীন — ৳৫,০০০"},
            new String[]{"নির্ধারিত সময়ে অটো পোস্ট", "কন্টেন্ট শিডিউল"}),
        new Service("ic_page", "ইউটিউব অটো পোস্ট",
            "ইউটিউব চ্যানেলে স্বয়ংক্রিয়ভাবে পোস্ট প্রকাশ করা।",
            "৳৫০ থেকে শুরু", "#DC2626",
            new String[]{"ট্রায়াল — ৳৫০", "মাসিক — ৳১,০০০", "এককালীন — ৳৫,০০০"},
            new String[]{"চ্যানেলে অটো পোস্ট", "কন্টেন্ট শিডিউল"}),
        new Service("ic_grid", "ফেসবুক + ইনস্টাগ্রাম কম্বো",
            "ফেসবুক ও ইনস্টাগ্রাম দুই প্ল্যাটফর্মের অটোমেশন একসাথে। দাম জানতে চাইলে হ্যান্ডওভার করা হবে — প্রতিনিধি জানিয়ে দেবেন।",
            "দাম জানতে জিজ্ঞাসা করুন", "#06B6D2",
            new String[]{"দাম জানতে চাইলে হ্যান্ডওভার করা হবে — প্রতিনিধি জানাবেন"},
            new String[]{"দুই প্ল্যাটফর্ম একসাথে", "এক জায়গায় ম্যানেজমেন্ট"}),
        new Service("ic_grid", "ভাইব কোডিং",
            "আইডিয়া থেকে অ্যাপ বানানো। দাম জানতে চাইলে শুধু ট্রান্সফার করা হয় — প্রতিনিধি বিস্তারিত জানাবেন।",
            "দাম জানতে জিজ্ঞাসা করুন", "#0F172A",
            new String[]{"দাম জানতে চাইলে প্রতিনিধির কাছে ট্রান্সফার করা হয়"},
            new String[]{"আইডিয়া থেকে প্রোডাক্ট", "দ্রুত ডেলিভারি"}),
        new Service("ic_wallet", "এপিআই ক্রেডিট ওয়ালেট",
            "এপিআই ব্যবহারের জন্য ক্রেডিট রিচার্জ। ৳২০০ থেকে শুরু, যত টাকার এপিআই চান নিতে পারবেন।",
            "৳২০০ থেকে শুরু", "#D97706",
            new String[]{"রিচার্জ — ৳২০০, ৳৫০০, ৳১,০০০, ৳২,৫০০"},
            new String[]{"মেয়াদ শেষ হয় না", "Pay As You Go", "অ্যাডমিন অনুমোদনের পর যোগ হয়"}),
    };

    // ---- Lifecycle ----
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildShell();
        // v8.2 closed-app: remember a notification tap target, and
        // (re)schedule the periodic check. KEEP policy: re-scheduling
        // never duplicates the worker.
        readNotificationIntent(getIntent());
        scheduleUpdateWorker();
        // Resume an existing sign-in session in-app (no app involved).
        if (FirebaseAuthHelper.currentUser() != null) {
            signedIn = true;
            maybeRequestNotificationPermission();
            storeFcmToken();
            loadLiveLeads();
            loadLiveOrders();
            if (FirebaseAuthHelper.isAdmin()) loadLiveUsers();
            go(S_DASH, false);
        } else {
            go(S_LOGIN, false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FirebaseAuthHelper.RC_GOOGLE_SIGN_IN) {
            FirebaseAuthHelper.handleSignInResult(this, data,
                new FirebaseAuthHelper.ResultCallback<com.google.firebase.auth.FirebaseUser>() {
                    @Override public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                        signedIn = true;
                                    loginError = null;
                        maybeRequestNotificationPermission();
                        storeFcmToken();
                        loadLiveLeads();
                        loadLiveOrders();
                        if (FirebaseAuthHelper.isAdmin()) loadLiveUsers();
                        go(S_DASH, false);
                    }
                    @Override public void onError(String message) {
                        loginError = message;
                        go(S_LOGIN, false);
                    }
                });
        }
    }

    // v8.2 closed-app: a notification tap while the app already lives
    // re-delivers here instead of recreating the activity.
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readNotificationIntent(intent);
        if (signedIn) {
            loadLiveLeads();
            loadLiveOrders();
        }
    }

    private void readNotificationIntent(Intent intent) {
        if (intent == null) return;
        String leadDoc = intent.getStringExtra("open_lead_doc");
        String orderId = intent.getStringExtra("open_order_id");
        if (leadDoc != null && leadDoc.length() > 0) pendingOpenLeadDoc = leadDoc;
        if (orderId != null && orderId.length() > 0) pendingOpenOrderId = orderId;
    }

    /** Open a pending notification target once its list has loaded.
     *  Returns true when it opened something. */
    private boolean openPendingLeadIfAny() {
        if (pendingOpenLeadDoc == null) return false;
        for (LeadsStore.Lead l : leadsStore.all()) {
            if (l.docId().equals(pendingOpenLeadDoc)) {
                pendingOpenLeadDoc = null;
                currentLead = l;
                go(S_LEAD_DETAIL, true);
                return true;
            }
        }
        // Not found yet: keep it for the next load instead of
        // dropping the tap target (it can only match this account's list).
        return false;
    }

    private boolean openPendingOrderIfAny() {
        if (pendingOpenOrderId == null) return false;
        for (OrderStore.Order o : orders) {
            if (o.id.equals(pendingOpenOrderId)) {
                pendingOpenOrderId = null;
                currentOrder = o;
                go(S_ORDER_DETAIL, true);
                return true;
            }
        }
        // Not found yet: keep it for the next load (see above).
        return false;
    }

    // v8.2 closed-app: periodic check (minimum 15 minutes, only with
    // network). This is what delivers notifications while the app is
    // closed; it is NOT instant push (see README + FCM service).
    private void scheduleUpdateWorker() {
        try {
            Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
            PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                UpdateWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "automateiq_updates", ExistingPeriodicWorkPolicy.KEEP, request);
        } catch (Exception ignored) { }
    }

    // v8.2: keep the device token current on the signed-in user's doc
    // so a future sender can target this device. Best-effort and
    // silent: a failure changes nothing in the UI.
    private void storeFcmToken() {
        try {
            final String uid = FirebaseAuthHelper.currentUid();
            if (uid == null) return;
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(new com.google.android.gms.tasks.OnSuccessListener<String>() {
                    @Override public void onSuccess(String token) {
                        if (token == null || token.length() == 0) return;
                        java.util.Map<String, Object> fields = new java.util.HashMap<String, Object>();
                        fields.put("fcmToken", token);
                        FirebaseAuthHelper.updateUserPageFields(uid, fields,
                            new FirebaseAuthHelper.ResultCallback<Boolean>() {
                                @Override public void onSuccess(Boolean ok) { }
                                @Override public void onError(String e) { }
                            });
                    }
                });
        } catch (Exception ignored) { }
    }

    // v8.2: notification permission result. Denied changes nothing:
    // login and every screen keep working; only the in-app banner and
    // badges still announce new items.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (screen == S_LOGIN) { super.onBackPressed(); return; }
        if (!backStack.isEmpty()) {
            screen = gateScreen(backStack.remove(backStack.size() - 1));
            render();
        } else if (signedIn) {
            go(S_DASH, false);
        } else {
            super.onBackPressed();
        }
    }

    // v7 ROLE GATE: after login, each role sees ONLY its own panel.
    // Role gate: after login, each role sees ONLY its own panel. No role switcher exists by design.
    private int gateScreen(int next) {
        if (!signedIn) return next;
        if (next == S_LOGIN || next == S_PENDING) return next;
        boolean admin = FirebaseAuthHelper.isAdmin();
        if (admin) {
            switch (next) {
                case S_SERVICES: case S_SERVICE_DETAIL: case S_SUBS:
                case S_TOKENS: case S_MY_SUB: case S_ORDER_FORM:
                case S_RECHARGE_FORM:
                    return S_DASH;
                default: return next;
            }
        } else {
            switch (next) {
                case S_CUSTOMERS: case S_CUSTOMER_DETAIL: case S_PAGES:
                case S_PAGE_FORM: case S_LEADS: case S_LEAD_DETAIL:
                case S_CONNECT:
                    return S_DASH;
                default: return next;
            }
        }
    }

    private void go(int next, boolean push) {
        if (push) backStack.add(screen);
        // v8.2: opening a thread/order marks it read (badges clear).
        if (next == S_LEAD_DETAIL && currentLead != null) markLeadRead(currentLead);
        if (next == S_ORDER_DETAIL && currentOrder != null) markOrderRead(currentOrder);
        if (next == S_LEAD_DETAIL && pendingBannerLead != null && currentLead != null
                && pendingBannerLead.docId().equals(currentLead.docId())) {
            pendingBannerText = null; pendingBannerLead = null;
        }
        if (next == S_ORDER_DETAIL && pendingBannerOrder != null && currentOrder != null
                && pendingBannerOrder.id.equals(currentOrder.id)) {
            pendingBannerText = null; pendingBannerOrder = null;
        }
        screen = gateScreen(next);
        render();
    }

    private void loadLiveLeads() {
        String uid = FirebaseAuthHelper.currentUid();
        if (uid == null) {
            leadsError = "লগইন করা নেই। আবার লগইন করুন।";
            leadsLoading = false;
            return;
        }
        leadsLoading = true;
        leadsError = null;
        FirebaseAuthHelper.fetchLiveLeads(uid,
            new FirebaseAuthHelper.ResultCallback<List<LeadsStore.Lead>>() {
                @Override public void onSuccess(List<LeadsStore.Lead> live) {
                    detectNewLeads(live);
                    leadsStore.setLiveLeads(live);
                    openPendingLeadIfAny();
                    leadsLoading = false;
                    leadsError = null;
                    // v8.2: a banner must appear on whatever screen is
                    // open, not only Dashboard/Leads.
                    if (screen == S_DASH || screen == S_LEADS || pendingBannerText != null) render();
                }
                @Override public void onError(String message) {
                    leadsLoading = false;
                    leadsError = message;
                    if (screen == S_DASH || screen == S_LEADS) render();
                }
            });
    }


    private void logout() {
        FirebaseAuthHelper.signOut(this);
        signedIn = false;
        mySubscription = null;
        orders = new ArrayList<OrderStore.Order>();
        ordersLoaded = false;
        users = new java.util.ArrayList<UsersStore.User>();
        usersLoaded = false;
        usersError = null;
        currentCustomer = null;
        pendingPageUser = null;
        localPageRecords = new java.util.ArrayList<UsersStore.User>();
        autoExpireCount = 0;
        currentOrder = null;
        // v8.2: never carry one account's banners/badges into the next login.
        pendingBannerText = null; pendingBannerLead = null; pendingBannerOrder = null;
        pendingOpenLeadDoc = null; pendingOpenOrderId = null;
        unreadLeadDocIds.clear(); unreadOrderIds.clear(); sendInFlightDocs.clear();
        selectedPackage = null;
        leadsStore.setLiveLeads(new ArrayList<LeadsStore.Lead>());
        backStack.clear();
        go(S_LOGIN, false);
    }

    // ---- Shell: top bar + scroll content + bottom nav ----
    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(V3_BODY_BG);

        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setBackgroundColor(SURFACE);
        topBar.setElevation(dp(4));
        topBar.setPadding(dp(16), dp(12), dp(16), dp(12));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(topBar, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));
        scroll.addView(content, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // v5: thread pane (hidden except on S_LEAD_DETAIL). Same weight as
        // the main scroll area, so the composer sits at the screen bottom.
        threadPane = new LinearLayout(this);
        threadPane.setOrientation(LinearLayout.VERTICAL);
        threadPane.setBackgroundColor(V3_BODY_BG);
        threadPane.setVisibility(View.GONE);
        root.addView(threadPane, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bottomNav = new LinearLayout(this);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setBackgroundColor(SURFACE);
        bottomNav.setElevation(dp(8));
        bottomNav.setPadding(dp(8), dp(8), dp(8), dp(8));
        root.addView(bottomNav, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    // v3 appbar title/subtitle per screen (title 16/800 white, sub 12 #E0E7FF).
    private String[] appbarTitle() {
        boolean admin = signedIn && FirebaseAuthHelper.isAdmin();
        switch (screen) {
            case S_DASH: return admin
                ? new String[]{"অ্যাডমিন প্যানেল", "AutoMateIQ"}
                : new String[]{"কাস্টমার প্যানেল", "AutoMateIQ"};
            case S_ORDERS: return new String[]{"অর্ডার", admin ? "সব কাস্টমারের অর্ডার" : "আমার অর্ডার"};
            case S_ORDER_DETAIL: return new String[]{"অর্ডার ডিটেইলস", ""};
            case S_ORDER_FORM: return new String[]{"অর্ডার করুন", ""};
            case S_RECHARGE_FORM: return new String[]{"রিচার্জ", ""};
            case S_CUSTOMERS: return new String[]{"কাস্টমার", ""};
            case S_CUSTOMER_DETAIL: return new String[]{"কাস্টমার ডিটেইলস", ""};
            case S_PAGES: case S_CONNECT: return new String[]{"পেজ", "নিজের পেজ + কাস্টমার পেজ"};
            case S_PAGE_FORM: return new String[]{"পেজ যোগ করুন", ""};
            case S_LEADS: return new String[]{"লিডস", "মেসেঞ্জার থেকে আসা লিড"};
            case S_LEAD_DETAIL: return new String[]{"লিড চ্যাট", ""};
            case S_SERVICES: return new String[]{"সার্ভিস", "যাচাই করা দাম"};
            case S_SERVICE_DETAIL: return new String[]{"সার্ভিস ডিটেইলস", ""};
            case S_SUBS: case S_MY_SUB: return new String[]{"সাবস্ক্রিপশন", "আমার মেয়াদ"};
            case S_TOKENS: return new String[]{"ওয়ালেট", "API ক্রেডিট রিচার্জ"};
            default: return new String[]{"AutoMateIQ", ""};
        }
    }
    private void renderTopBar() {
        v3AnimIndex = 0;
        if (screen == S_LOGIN || screen == S_PENDING) {
            topBar.setVisibility(View.GONE);
            return;
        }
        topBar.setVisibility(View.VISIBLE);
        topBar.removeAllViews();
        // v3 .appbar: 120deg #6D28D9 -> #2563EB 55% -> #0891B2, padding 12/16.
        GradientDrawable ab = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{V3_VIOLET_DARK, V3_BLUE, V3_CYAN_DARK});
        topBar.setBackground(ab);
        topBar.setElevation(0);
        topBar.setPadding(dp(16), dp(12), dp(16), dp(12));
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        if (!backStack.isEmpty()) {
            ImageView back = iconView(iconRes("ic_back"), Color.WHITE);
            back.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { onBackPressed(); }
            });
            topBar.addView(back);
        } else {
            TextView logo = new TextView(this);
            logo.setText("A");
            logo.setTextSize(20);
            logo.setTypeface(Typeface.DEFAULT_BOLD);
            logo.setTextColor(Color.WHITE);
            logo.setGravity(Gravity.CENTER);
            GradientDrawable lg = rounded(0x38FFFFFF, 12);
            lg.setStroke(dp(1), 0x80FFFFFF);
            logo.setBackground(lg);
            topBar.addView(logo, new LinearLayout.LayoutParams(dp(38), dp(38)));
        }
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10), 0, dp(10), 0);
        String[] tt = appbarTitle();
        TextView t = text(tt[0], 16, Color.WHITE, true);
        titles.addView(t);
        if (tt[1].length() > 0) titles.addView(text(tt[1], 12, 0xFFE0E7FF, false));
        topBar.addView(titles, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (signedIn) {
            topBar.addView(iconView(iconRes("ic_bell"), Color.WHITE));
            TextView av = new TextView(this);
            av.setText("A");
            av.setTextSize(16);
            av.setTypeface(Typeface.DEFAULT_BOLD);
            av.setTextColor(Color.WHITE);
            av.setGravity(Gravity.CENTER);
            GradientDrawable ag = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, new int[]{0xFFF59E0B, V3_PINK});
            ag.setCornerRadius(dp(19));
            ag.setStroke(dp(2), 0xB3FFFFFF);
            av.setBackground(ag);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(38), dp(38));
            alp.setMargins(dp(8), 0, dp(8), 0);
            topBar.addView(av, alp);
            TextView out = new TextView(this);
            out.setText("লগআউট");
            out.setTextSize(12);
            out.setTypeface(Typeface.DEFAULT_BOLD);
            out.setTextColor(Color.WHITE);
            out.setPadding(dp(10), dp(6), dp(10), dp(6));
            out.setBackground(rounded(0x33FFFFFF, 20));
            out.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { logout(); }
            });
            topBar.addView(out);
        }
    }
    private void renderBottomNav() {
        bottomNav.removeAllViews();
        if (screen == S_LOGIN || screen == S_PENDING) {
            bottomNav.setVisibility(View.GONE);
            return;
        }
        bottomNav.setVisibility(View.VISIBLE);
        // v3 .nav: glass white .88, padding 9/6/11, label 11/700, unselected
        // #94A3B8, selected tile 34x34 gradient violet->blue with glow.
        bottomNav.setBackgroundColor(0xE0FFFFFF);
        bottomNav.setElevation(0);
        bottomNav.setPadding(dp(6), dp(9), dp(6), dp(11));
        boolean isAdminNav = signedIn && FirebaseAuthHelper.isAdmin();
        String[][] items = isAdminNav ? new String[][]{
            {"ic_home", "ড্যাশবোর্ড", String.valueOf(S_DASH)},
            {"ic_list", "অর্ডার", String.valueOf(S_ORDERS)},
            {"ic_users", "কাস্টমার", String.valueOf(S_CUSTOMERS)},
            {"ic_globe", "পেজ", String.valueOf(S_PAGES)},
            {"ic_chat", "লিডস", String.valueOf(S_LEADS)},
        } : new String[][]{
            {"ic_home", "হোম", String.valueOf(S_DASH)},
            {"ic_grid", "সার্ভিস", String.valueOf(S_SERVICES)},
            {"ic_cart", "অর্ডার", String.valueOf(S_ORDERS)},
            {"ic_calendar", "সাবস্ক্রিপশন", String.valueOf(S_MY_SUB)},
            {"ic_wallet", "ওয়ালেট", String.valueOf(S_TOKENS)},
        };
        for (final String[] it : items) {
            final int target = Integer.parseInt(it[2]);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(2), dp(4), dp(2));
            boolean selected = screen == target
                || (target == S_LEADS && (screen == S_LEAD_DETAIL))
                || (target == S_CUSTOMERS && (screen == S_CUSTOMER_DETAIL))
                || (target == S_PAGES && (screen == S_PAGE_FORM))
                || (target == S_ORDERS && (screen == S_ORDERS
                    || screen == S_ORDER_DETAIL || screen == S_ORDER_FORM
                    || screen == S_RECHARGE_FORM))
                || (target == S_SERVICES && (screen == S_SERVICE_DETAIL))
                || (target == S_MY_SUB && (screen == S_SUBS))
                || (target == S_TOKENS && (screen == S_RECHARGE_FORM));
            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.CENTER);
            if (selected) {
                GradientDrawable tg = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR, new int[]{V3_VIOLET, V3_BLUE});
                tg.setCornerRadius(dp(11));
                tile.setBackground(tg);
                tile.setElevation(dp(4));
            }
            tile.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
            tile.addView(iconView(iconRes(it[0]), selected ? Color.WHITE : V3_NAV_GREY));
            item.addView(tile);
            LinearLayout labelRow = new LinearLayout(this);
            labelRow.setOrientation(LinearLayout.HORIZONTAL);
            labelRow.setGravity(Gravity.CENTER);
            TextView label = new TextView(this);
            label.setText(it[1]);
            label.setTextSize(11);
            label.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            label.setTextColor(selected ? V3_VIOLET : V3_NAV_GREY);
            label.setGravity(Gravity.CENTER);
            label.setPadding(0, dp(2), 0, 0);
            labelRow.addView(label);
            // v8.2: unread dot on Leads / Orders nav items.
            if ((target == S_LEADS && !unreadLeadDocIds.isEmpty())
                    || (target == S_ORDERS && !unreadOrderIds.isEmpty())) {
                labelRow.addView(navDot());
            }
            item.addView(labelRow);
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { go(target, false); }
            });
            bottomNav.addView(item, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
    }
    private void render() {
        renderTopBar();
        renderBottomNav();
        if (screen == S_LEAD_DETAIL) {
            // v5 THREAD: never touch the main ScrollView here. The thread
            // renders into threadPane and lands on the LATEST message.
            // scroll.scrollTo(0, 0) is deliberately NOT called for threads.
            scroll.setVisibility(View.GONE);
            threadPane.setVisibility(View.VISIBLE);
            renderThreadScreen();
            return;
        }
        threadPane.setVisibility(View.GONE);
        scroll.setVisibility(View.VISIBLE);
        if (screen == S_LOGIN) {
            scroll.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{V3_LOGIN_TOP, V3_BLUE, V3_CYAN}));
        } else {
            scroll.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFFEEF2FF, 0xFFFDF2F8, 0xFFECFEFF}));
        }
        content.removeAllViews();
        scroll.scrollTo(0, 0);
        if (notice != null) {
            content.addView(noticeCard(notice));
            notice = null;
        }
        // v8.2: in-app notification banner (tapping opens the item).
        LinearLayout banner = notificationBannerView();
        if (banner != null) content.addView(banner);
        switch (screen) {
            case S_LOGIN: showLogin(); break;
            case S_PENDING: showPending(); break;
            case S_DASH:
                // v7: admin dashboard and customer home are different
                // admin dashboard and customer home are different screens.
                if (signedIn && FirebaseAuthHelper.isAdmin()) showAdminDashboard();
                else showDashboard();
                break;
            case S_CUSTOMERS: showCustomers(); break;
            case S_CUSTOMER_DETAIL: showCustomerDetail(); break;
            case S_PAGES: showPages(); break;
            case S_PAGE_FORM: showPageForm(); break;
            case S_LEADS: showLeads(); break;
            // S_LEAD_DETAIL is handled before the switch in render()
            // (threadPane, not the main ScrollView) — never reaches here.
            case S_CONNECT: showPages(); break; // v7: Connect lives in the admin Pages screen.
            case S_SERVICES: showServices(); break;
            case S_SERVICE_DETAIL: showServiceDetail(); break;
            case S_SUBS: showSubscriptions(); break;
            case S_TOKENS: showTokens(); break;
            case S_MY_SUB: showMySubscription(); break;
            case S_ORDERS: showOrders(); break;
            case S_ORDER_DETAIL: showOrderDetail(); break;
            case S_ORDER_FORM: showOrderForm(); break;
            case S_RECHARGE_FORM: showRechargeForm(); break;
            case S_ABOUT: showAbout(); break;
            default: showDashboard(); break;
        }
    }

    // ---- Design helpers ----

    // ---- v3: bundled Bengali font (Noto Sans Bengali in assets/fonts) ----
    private Typeface bengaliRegular = null;
    private Typeface bengaliBold = null;
    private int v3AnimIndex = 0;
    private Typeface bengaliTypeface(boolean bold) {
        try {
            if (bold) {
                if (bengaliBold == null) bengaliBold = Typeface.createFromAsset(
                    getAssets(), "fonts/NotoSansBengali-Bold.ttf");
                return bengaliBold;
            }
            if (bengaliRegular == null) bengaliRegular = Typeface.createFromAsset(
                getAssets(), "fonts/NotoSansBengali-Regular.ttf");
            return bengaliRegular;
        } catch (Exception e) { return bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT; }
    }

    // ---- v3 animations: staggered fade + slide-up card entry (200-400ms) ----
    private void animEntry(View v, int index) {
        int delay = Math.min(index, 5) * 70;
        v.setAlpha(0f);
        v.setTranslationY(dp(16));
        v.animate().alpha(1f).translationY(0).setDuration(350)
            .setStartDelay(delay).start();
    }

    // ---- v3: button press scale .97, spring back on release ----
    private void pressScale(final View v) {
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override public boolean onTouch(View view, MotionEvent ev) {
                if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                    view.setScaleX(0.97f); view.setScaleY(0.97f);
                } else if (ev.getAction() == MotionEvent.ACTION_UP
                        || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                    view.setScaleX(1f); view.setScaleY(1f);
                }
                return false;
            }
        });
    }

    // ---- v3: vector-drawable icon (24dp box, 22dp icon, tinted) ----
    private ImageView iconView(int resId, int tint) {
        ImageView iv = new ImageView(this);
        if (resId != 0) iv.setImageResource(resId);
        iv.setColorFilter(tint);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(22), dp(22));
        iv.setLayoutParams(lp);
        return iv;
    }
    private int iconRes(String name) {
        int id = getResources().getIdentifier(name, "drawable", getPackageName());
        return id;
    }

    // ---- v3: glass card background (semi-transparent white + light border) ----
    private GradientDrawable glassDrawable() {
        GradientDrawable d = new GradientDrawable();
        d.setColor(V3_GLASS);
        d.setCornerRadius(dp(22));
        d.setStroke(dp(1), V3_GLASS_BORDER);
        return d;
    }

    // ---- v3: gradient hero, 3 stops, slow shift (gradShift 9-10s alternate) ----
    // v3 heroCard: 125deg #6D28D9 -> #2563EB 45% -> #06B6D2 80%, radius 22.
    // Android has no 125deg multi-stop animation, so the shift animates
    // between the v3 palette and the v3 heroTop palette (#7C3AED/#2563EB/
    // #06B6D2/#EC4899), keeping all stops inside the v3 family.
    private GradientDrawable heroShiftDrawable() {
        final GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{V3_VIOLET_DARK, V3_BLUE, V3_CYAN});
        d.setCornerRadius(dp(22));
        final int[][] stops = {{V3_VIOLET_DARK, V3_BLUE, V3_CYAN},
                               {V3_VIOLET, V3_BLUE, V3_PINK}};
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(9500);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        final ArgbEvaluator ev = new ArgbEvaluator();
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override public void onAnimationUpdate(ValueAnimator a) {
                float t = (Float) a.getAnimatedValue();
                d.setColors(new int[]{
                    (Integer) ev.evaluate(t, stops[0][0], stops[1][0]),
                    (Integer) ev.evaluate(t, stops[0][1], stops[1][1]),
                    (Integer) ev.evaluate(t, stops[0][2], stops[1][2])});
            }
        });
        anim.start();
        return d;
    }
    private GradientDrawable gradient3(int c1, int c2, int c3, int radiusDp) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, new int[]{c1, c2, c3});
        d.setCornerRadius(dp(radiusDp));
        return d;
    }
    // ---- v3: stepped progress (1 package -> 2 payment -> 3 verify) ----
    private LinearLayout stepProgress(int current) {
        String[] labels = {"প্যাকেজ", "পেমেন্ট", "যাচাই"};
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(12));
        for (int i = 0; i < 3; i++) {
            TextView num = new TextView(this);
            num.setText(bn(String.valueOf(i + 1)));
            num.setTextSize(13);
            num.setTypeface(Typeface.DEFAULT_BOLD);
            num.setGravity(Gravity.CENTER);
            num.setTextColor(i + 1 <= current ? Color.WHITE : TEXT2);
            num.setBackground(rounded(i + 1 <= current ? PRIMARY : BORDER, 16));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(28), dp(28));
            row.addView(num, nlp);
            TextView lab = text(labels[i], 13, i + 1 <= current ? PRIMARY : TEXT2, i + 1 == current);
            lab.setPadding(dp(6), 0, dp(14), 0);
            row.addView(lab);
        }
        return row;
    }

    // ---- v3: horizontal quick-action row (icon tile + short label) ----
    private LinearLayout quickAction(String iconName, String label, View.OnClickListener onClick) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setBackground(gradientTile(iconName));
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(48), dp(48));
        tile.setLayoutParams(tlp);
        ImageView iv = iconView(iconRes(iconName), Color.WHITE);
        tile.addView(iv);
        item.addView(tile);
        TextView lab = text(label, 12, TEXT, true);
        lab.setGravity(Gravity.CENTER);
        lab.setPadding(0, dp(6), 0, 0);
        item.addView(lab);
        item.setOnClickListener(onClick);
        pressScale(item);
        return item;
    }

    // v3 gradient tiles per action (not one shared gradient):
    // list=blue->cyan, users=violet->pink, page/globe=teal->cyan,
    // chat=amber->pink, clock=rose->orange, default=violet->blue.
    private GradientDrawable gradientTile(String iconName) {
        int c1 = V3_VIOLET, c2 = V3_BLUE;
        if ("ic_list".equals(iconName)) { c1 = V3_BLUE; c2 = V3_CYAN; }
        else if ("ic_users".equals(iconName)) { c1 = V3_VIOLET; c2 = 0xFFDB2777; }
        else if ("ic_page".equals(iconName) || "ic_globe".equals(iconName)) { c1 = 0xFF059669; c2 = V3_CYAN; }
        else if ("ic_chat".equals(iconName)) { c1 = 0xFFF59E0B; c2 = V3_PINK; }
        else if ("ic_clock".equals(iconName)) { c1 = TL_ROSE; c2 = 0xFFF97316; }
        else if ("ic_calendar".equals(iconName)) { c1 = 0xFF4F46E5; c2 = 0xFF9333EA; }
        else if ("ic_wallet".equals(iconName)) { c1 = 0xFFF59E0B; c2 = V3_PINK; }
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{c1, c2});
        d.setCornerRadius(dp(15));
        return d;
    }
    // ---- v3: timeline row (status dot + content) ----
    private View statusDot(int color) {
        // v3 .tdot: 18px, 3px white border, shadow.
        View dot = new View(this);
        GradientDrawable d = rounded(color, 9);
        d.setStroke(dp(3), Color.WHITE);
        dot.setBackground(d);
        dot.setElevation(dp(2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(18), dp(18));
        lp.setMargins(0, dp(2), dp(10), 0);
        dot.setLayoutParams(lp);
        return dot;
    }
    private int statusColor(String status) {
        // v3 timeline dot colours, not the v8 chip colours.
        if ("in_progress".equals(status)) return V3_BLUE;
        if ("completed".equals(status)) return TL_GREEN;
        if ("cancelled".equals(status) || "rejected".equals(status)) return TL_ROSE;
        return TL_AMBER_DOT;
    }

    // ---- v3: shimmer loading  (alpha pulse, 1.4s) ----
    private View shimmerBar() {
        View v = new View(this);
        v.setBackground(rounded(BORDER, 8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(16));
        lp.setMargins(0, 0, 0, dp(10));
        v.setLayoutParams(lp);
        ObjectAnimator a = ObjectAnimator.ofFloat(v, "alpha", 0.35f, 1f, 0.35f);
        a.setDuration(1400);
        a.setRepeatCount(ValueAnimator.INFINITE);
        a.start();
        return v;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private GradientDrawable roundedBorder(int fill, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radiusDp));
        d.setStroke(dp(1), BORDER);
        return d;
    }

    private GradientDrawable gradient(int c1, int c2) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{c1, c2});
        d.setCornerRadius(dp(20));
        return d;
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(size);
        tv.setTextColor(color);
        tv.setTypeface(bengaliTypeface(bold));
        // v3 line-heights, applied at every size (v8 only did 14).
        float lh = 1.5f;
        if (size == 26) lh = 1.3f;
        else if (size == 18) lh = 1.35f;
        else if (size == 16) lh = 1.4f;
        else if (size == 14) lh = 1.7f;
        else if (size == 22) lh = 1.2f;
        tv.setLineSpacing(0, lh);
        return tv;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(glassDrawable());
        c.setElevation(dp(3));
        animEntry(c, v3AnimIndex++);
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        c.setLayoutParams(lp);
        return c;
    }

    private void add(LinearLayout parent, View v) {
        parent.addView(v);
    }

    private TextView chip(String s, int bg, int fg) {
        TextView c = new TextView(this);
        c.setText(s);
        c.setTextSize(12);
        c.setTypeface(Typeface.DEFAULT_BOLD);
        c.setTextColor(fg);
        c.setBackground(rounded(bg, 100));
        c.setPadding(dp(11), dp(5), dp(11), dp(5));
        return c;
    }

    private TextView statusChip(LeadsStore.Lead lead) {
        if (lead.paused) return chip("হ্যান্ডওভার", AMBER_SOFT, AMBER);
        if ("bot_replied".equals(lead.status)) return chip("বট রিপ্লাই", GREEN_SOFT, GREEN);
        return chip("নতুন", SOFT_BLUE, PRIMARY);
    }

    private TextView primaryButton(String label, View.OnClickListener onClick) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        // v3 .btn: 90deg #7C3AED -> #2563EB -> #06B6D2, radius 14, pad 14.
        b.setBackground(gradient3(V3_VIOLET, V3_BLUE, V3_CYAN, 14));
        b.setElevation(dp(6));
        b.setPadding(dp(16), dp(14), dp(16), dp(14));
        b.setOnClickListener(onClick);
        pressScale(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private TextView outlineButton(String label, final int color, View.OnClickListener onClick) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        // v3 .btn2: white .9, border rgba(167,139,250,.6), text #4F46E5,
        // radius 14, pad 12. RED callers get the v3 danger style instead.
        b.setTextColor(color == RED ? V3_DANGER_FG : V3_INDIGO_TEXT);
        b.setGravity(Gravity.CENTER);
        GradientDrawable d = rounded(color == RED ? V3_DANGER_BG : 0xE6FFFFFF, 14);
        d.setStroke(dp(1), color == RED ? V3_DANGER_BORDER : V3_INPUT_BORDER);
        b.setBackground(d);
        b.setPadding(dp(16), dp(12), dp(16), dp(12));
        b.setOnClickListener(onClick);
        pressScale(b);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private TextView ghostButton(String label, View.OnClickListener onClick) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(PRIMARY);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12), dp(10), dp(12), dp(10));
        b.setOnClickListener(onClick);
        pressScale(b);
        return b;
    }

    private LinearLayout noticeCard(String msg) {
        LinearLayout c = card();
        c.setBackground(rounded(AMBER_SOFT, 16));
        c.addView(text(msg, 14, TEXT, false));
        return c;
    }

    private LinearLayout bannerCard(String msg, int bg, int fg) {
        LinearLayout c = card();
        c.setBackground(rounded(bg, 16));
        TextView t = text(msg, 13, fg, true);
        c.addView(t);
        return c;
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "লিংক খোলা যায়নি।", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyText(String label, String value) {
        try {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText(label, value));
            Toast.makeText(this, "কপি হয়েছে।", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "কপি করা যায়নি।", Toast.LENGTH_SHORT).show();
        }
    }

    // Payment number, shown exactly as on the order form
    // ("bKash/Nagad (Personal)" + Copy button). Shown ONLY in the order
    // form and wallet recharge form below, where the customer must know
    // where to send money before filling senderPhone/trxId. This is the
    // ONLY contact number displayed anywhere in the app; no other phone,
    // email, or contact is shown. (The Messenger bot rule is unchanged:
    // the bot still never gives contact info in chat replies.)
    private static final String PAYMENT_NUMBER = "01842-369496";

    // Payment info card, styled like the other v6 cards. Used ONLY by
    // showOrderForm() Step 2 and showRechargeForm().
    private LinearLayout paymentInfoCard() {
        LinearLayout c = card();
        // v3 .pay: 135deg rgba(255,255,255,.95) -> rgba(224,242,254,.95),
        // 1.5px border rgba(34,211,238,.55), radius 18, padding 14.
        GradientDrawable pd = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{0xF2FFFFFF, 0xF2E0F2FE});
        pd.setCornerRadius(dp(18));
        pd.setStroke(dp(1), 0x8C22D3EE);
        c.setBackground(pd);
        c.setPadding(dp(14), dp(14), dp(14), dp(14));
        c.addView(text("bKash/Nagad (Personal)", 16, NAVY, true));
        TextView num = text(PAYMENT_NUMBER, 22, TEXT, true);
        num.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        num.setLetterSpacing(0.05f);
        num.setPadding(0, dp(6), 0, dp(2));
        c.addView(num);
        c.addView(text("এই নম্বরে টাকা পাঠান, তারপর নিচে আপনার Sender নম্বর + TrxID লিখুন।", 13, TEXT2, false));
        c.addView(outlineButton(" পেমেন্ট নম্বর কপি করুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { copyText("payment", PAYMENT_NUMBER); }
        }));
        return c;
    }

    private String bn(String n) {
        return SubscriptionStore.toBengaliDigits(n);
    }

    private TextView avatar(String initial, int color) {
        TextView a = new TextView(this);
        a.setText(initial);
        a.setTextSize(18);
        a.setTypeface(Typeface.DEFAULT_BOLD);
        a.setTextColor(Color.WHITE);
        a.setGravity(Gravity.CENTER);
        a.setBackground(avatarGradient(color));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
        a.setLayoutParams(lp);
        return a;
    }

    private GradientDrawable serviceTileGradient(String hex) {
        int c1 = V3_VIOLET, c2 = V3_BLUE;
        try {
            int base = Color.parseColor(hex);
            if (base == 0xFF2563EB || base == 0xFF1D4ED8) { c1 = V3_BLUE; c2 = V3_CYAN; }
            else if (base == 0xFF8B5CF6) { c1 = 0xFF4F46E5; c2 = 0xFF9333EA; }
            else if (base == 0xFF16A34A) { c1 = 0xFF059669; c2 = V3_CYAN; }
            else if (base == 0xFFDC2626) { c1 = TL_ROSE; c2 = 0xFFF97316; }
            else if (base == 0xFF06B6D2) { c1 = V3_BLUE; c2 = V3_CYAN; }
            else if (base == 0xFFD97706) { c1 = 0xFFF59E0B; c2 = V3_PINK; }
        } catch (Exception e) { }
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{c1, c2});
        d.setCornerRadius(dp(20));
        return d;
    }
    // v3 avatars are gradients (g-blue/g-amber/g-rose/g-violet/g-teal/g-indigo).
    private GradientDrawable avatarGradient(int color) {
        int c1 = 0xFF4F46E5, c2 = 0xFF9333EA;
        if (color == PRIMARY || color == 0xFF1D4ED8) { c1 = V3_BLUE; c2 = V3_CYAN; }
        else if (color == AMBER || color == 0xFFD97706) { c1 = 0xFFF59E0B; c2 = V3_PINK; }
        else if (color == RED) { c1 = TL_ROSE; c2 = 0xFFF97316; }
        else if (color == VIOLET || color == 0xFF8B5CF6) { c1 = V3_VIOLET; c2 = 0xFFDB2777; }
        else if (color == GREEN || color == 0xFF16A34A) { c1 = 0xFF059669; c2 = V3_CYAN; }
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{c1, c2});
        d.setCornerRadius(dp(24));
        return d;
    }
    // v5 PROFESSIONAL SHELL: a real Material-style ripple on tappable
    // rows, so taps feel like a commercial app instead of a dead layout.
    private RippleDrawable rippleBackground() {
        int[][] states = new int[][]{new int[]{}};
        int[] colors = new int[]{0x1F2563EB};
        return new RippleDrawable(new ColorStateList(states, colors), null, null);
    }

    private LinearLayout progressBar(int percent) {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackground(rounded(BORDER, 8));
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(9));
        blp.setMargins(0, dp(8), 0, dp(4));
        bar.setLayoutParams(blp);
        int p = Math.max(0, Math.min(100, percent));
        LinearLayout fill = new LinearLayout(this);
        // v3 progress fill: 90deg #7C3AED -> #EC4899 -> #06B6D2, grow 1s.
        fill.setBackground(gradient3(V3_VIOLET, V3_PINK, V3_CYAN, 8));
        bar.addView(fill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, p));
        fill.setPivotX(0);
        fill.setScaleX(0f);
        ObjectAnimator.ofFloat(fill, "scaleX", 0f, 1f).setDuration(1000).start();
        LinearLayout rest = new LinearLayout(this);
        bar.addView(rest, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 100 - p));
        return bar;
    }

    // ---- Screens: Login / Pending ----
    private void showLogin() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        hero.setPadding(0, dp(24), 0, dp(8));
        TextView logo = new TextView(this);
        logo.setText("A");
        logo.setTextSize(30);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(Color.WHITE);
        logo.setGravity(Gravity.CENTER);
        // v3 login logo: 78px tile, radius 22, white .2 + white .6 border.
        GradientDrawable lg2 = rounded(0x33FFFFFF, 22);
        lg2.setStroke(dp(1), 0x99FFFFFF);
        logo.setBackground(lg2);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(dp(78), dp(78));
        llp.gravity = Gravity.CENTER;
        hero.addView(logo, llp);
        TextView name = text("AutoMateIQ", 28, Color.WHITE, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(14), 0, 0);
        hero.addView(name);
        TextView tag = text("আপনার ব্যবসার মেসেঞ্জার ড্যাশবোর্ড — মোবাইলে", 15, 0xFFE0E7FF, false);
        tag.setGravity(Gravity.CENTER);
        hero.addView(tag);
        content.addView(hero);

        LinearLayout c = card();
        c.setPadding(dp(20), dp(20), dp(20), dp(20));
        c.addView(text("স্বাগতম", 22, TEXT, true));
        TextView sub = text("লগইন করলে আপনার পেজের লিড, সাবস্ক্রিপশন আর মেসেজ এখানেই দেখা যাবে।", 14, TEXT2, false);
        sub.setPadding(0, dp(6), 0, dp(12));
        c.addView(sub);
        if (loginError != null) {
            LinearLayout err = new LinearLayout(this);
            err.setOrientation(LinearLayout.VERTICAL);
            err.setBackground(rounded(RED_SOFT, 12));
            err.setPadding(dp(12), dp(10), dp(12), dp(10));
            err.addView(text(loginError, 13, RED, true));
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            elp.setMargins(0, 0, 0, dp(10));
            err.setLayoutParams(elp);
            c.addView(err);
        }
        TextView google = new TextView(this);
        google.setText("Google দিয়ে চালিয়ে যান");
        google.setTextSize(15);
        google.setTypeface(Typeface.DEFAULT_BOLD);
        google.setTextColor(Color.WHITE);
        google.setGravity(Gravity.CENTER);
        // v3 login CTA: the gradient .btn (not a white outline button).
        google.setBackground(gradient3(V3_VIOLET, V3_BLUE, V3_CYAN, 14));
        google.setElevation(dp(6));
        google.setPadding(dp(16), dp(14), dp(16), dp(14));
        google.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // IN-APP LOGIN ONLY. No app , ever, for login.
                if (!FirebaseAuthHelper.isFirebaseReady(MainActivity.this)) {
                    go(S_PENDING, true);
                } else {
                    loginError = null;
                    FirebaseAuthHelper.startGoogleSignIn(MainActivity.this);
                }
            }
        });
        c.addView(google);
        content.addView(c);

        // v8.2: notification opt-in. Plain language, never blocks login:
        // the login button above works whether this is on or off.
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
            LinearLayout nc = card();
            nc.addView(text("নতুন মেসেজ বা অর্ডার এলে ফোনে খবর পেতে নোটিফিকেশন চালু করুন।", 14, TEXT, false));
            // v8.2 closed-app, plain language only: checked periodically,
            // including while the app is closed — not instant.
            nc.addView(text("অ্যাপ বন্ধ থাকলেও নতুন খবর কিছুক্ষণ পর পর যাচাই করে জানানো হয়।", 13, TEXT2, false));
            nc.addView(outlineButton("নোটিফিকেশন চালু করুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    seenPrefs().edit().putBoolean("notif_asked", true).apply();
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATIONS);
                }
            }));
            content.addView(nc);
        }

        // v3 login feature row: chat / cart / clock, 3 columns in a glass card.
        LinearLayout feat = card();
        LinearLayout frow = new LinearLayout(this);
        frow.setOrientation(LinearLayout.HORIZONTAL);
        String[][] feats = {{"ic_chat", "মেসেঞ্জার লিড"}, {"ic_cart", "অর্ডার"}, {"ic_clock", "সাবস্ক্রিপশন"}};
        int[] fcol = {V3_VIOLET, V3_BLUE, V3_PINK};
        for (int i = 0; i < feats.length; i++) {
            LinearLayout fc = new LinearLayout(this);
            fc.setOrientation(LinearLayout.VERTICAL);
            fc.setGravity(Gravity.CENTER);
            fc.addView(iconView(iconRes(feats[i][0]), fcol[i]));
            TextView fl = text(feats[i][1], 11, TEXT2, false);
            fl.setGravity(Gravity.CENTER);
            fl.setPadding(0, dp(6), 0, 0);
            fc.addView(fl);
            frow.addView(fc, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        feat.addView(frow);
        content.addView(feat);
        TextView about = text("অ্যাপ সম্পর্কে জানুন", 13, 0xFFE0E7FF, false);
        about.setGravity(Gravity.CENTER);
        about.setPadding(0, dp(8), 0, 0);
        about.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ABOUT, true); }
        });
        content.addView(about);
    }

    private void showPending() {
        LinearLayout c = card();
        c.setBackground(rounded(AMBER_SOFT, 20));
        c.addView(text("লগইন করা যাচ্ছে না", 20, TEXT, true));
        TextView body = text("লগইন সেবাটি এখন চালু হচ্ছে না। কিছুক্ষণ পর আবার চেষ্টা করুন।", 15, TEXT, false);
        body.setPadding(0, dp(8), 0, dp(12));
        c.addView(body);
        content.addView(c);
        content.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (FirebaseAuthHelper.isFirebaseReady(MainActivity.this)) {
                    FirebaseAuthHelper.startGoogleSignIn(MainActivity.this);
                } else {
                    notice = "এখনো লগইন চালু হয়নি। কিছুক্ষণ পর আবার চেষ্টা করুন।";
                    render();
                }
            }
        }));
        
        content.addView(ghostButton("লগইন স্ক্রিনে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_LOGIN, false); }
        }));
    }

    // ---- Screens: Dashboard / Leads ----
    private void showDashboard() {

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(heroShiftDrawable());
        hero.setElevation(dp(2));
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.setMargins(0, 0, 0, dp(14));
        hero.setLayoutParams(hlp);
        String who = "স্বাগতম";
        hero.addView(text(who, 26, Color.WHITE, true));
        hero.addView(text("আজকের ড্যাশবোর্ড — লিড, হ্যান্ডওভার আর সাবস্ক্রিপশন এক নজরে", 14, Color.WHITE, false));
        content.addView(hero);

        // Subscription card
        if (mySubscription != null) {
            LinearLayout c = card();
            c.setBackground(heroShiftDrawable());
            c.addView(text("আমার সাবস্ক্রিপশন", 16, Color.WHITE, true));
            TextView line = text(mySubscription.service + " • " + mySubscription.pkg
                + " • " + mySubscription.price, 15, TEXT, false);
            line.setPadding(0, dp(6), 0, 0);
            c.addView(line);
            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);
            row2.setPadding(0, dp(10), 0, 0);
            TextView days = text(SubscriptionStore.daysRemainingBn(mySubscription), 22, Color.WHITE, true);
            row2.addView(days);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            TextView chipWrap = new TextView(this);
            row2.addView(chipWrap, rp);
            row2.addView(mySubscription.isActive()
                ? chip("চালু আছে", GREEN_SOFT, GREEN)
                : chip("মেয়াদ শেষ", RED_SOFT, RED));
            c.addView(row2);
            c.addView(progressBar(mySubscription.progressPercent()));
            TextView dates = text("শুরু: " + SubscriptionStore.formatDate(mySubscription.startMillis)
                + "  •  শেষ: " + SubscriptionStore.formatDate(mySubscription.expiryMillis), 13, Color.WHITE, false);
            c.addView(dates);
            c.addView(outlineButton("সাবস্ক্রিপশন বিস্তারিত", Color.WHITE, new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_MY_SUB, true); }
            }));
            content.addView(c);
        } else {
            LinearLayout c = card();
            c.addView(text(" আমার সাবস্ক্রিপশন", 16, TEXT, true));
            TextView t2 = text("কোনো মাসিক সাবস্ক্রিপশন এখনো যুক্ত হয়নি। মাসিক প্যাকেজ কিনলে এখানে কত দিন বাকি আছে দেখা যাবে।", 14, TEXT2, false);
            t2.setPadding(0, dp(6), 0, 0);
            c.addView(t2);
            c.addView(primaryButton("মাসিক প্যাকেজ দেখুন", new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_SUBS, true); }
            }));
            content.addView(c);
        }

        // v3 CUSTOMER HOME: horizontal service scroll (gradient tiles).
        content.addView(text("সার্ভিস", 18, NAVY, true));
        HorizontalScrollView sscroll = new HorizontalScrollView(this);
        sscroll.setHorizontalScrollBarEnabled(false);
        LinearLayout srow = new LinearLayout(this);
        srow.setOrientation(LinearLayout.HORIZONTAL);
        for (final Service svc : SERVICES) {
            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.VERTICAL);
            tile.setGravity(Gravity.TOP | Gravity.START);
            tile.setBackground(serviceTileGradient(svc.color));
            tile.setPadding(dp(16), dp(16), dp(16), dp(16));
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(150), dp(110));
            tlp.setMargins(0, 0, dp(10), dp(14));
            tile.setLayoutParams(tlp);
            tile.addView(iconView(iconRes(svc.icon), Color.WHITE));
            TextView tn = text(svc.name, 15, Color.WHITE, true);
            tn.setPadding(0, dp(8), 0, 0);
            tile.addView(tn);
            TextView td = text(svc.desc, 12, 0xE0FFFFFF, false);
            td.setPadding(0, dp(2), 0, 0);
            tile.addView(td);
            tile.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { currentService = svc; go(S_SERVICE_DETAIL, true); }
            });
            pressScale(tile);
            srow.addView(tile);
        }
        sscroll.addView(srow);
        content.addView(sscroll);
        content.addView(primaryButton("নতুন অর্ডার করুন", new View.OnClickListener() {
            @Override public void onClick(View v) { selectedPackage = null; go(S_ORDER_FORM, true); }
        }));

        // v7 CUSTOMER recent: own orders, never leads.
        LinearLayout recent = card();
        recent.addView(text("সাম্প্রতিক আমার অর্ডার", 16, TEXT, true));
        if (ordersLoading) {
            TextView l = text("লোড হচ্ছে…", 14, TEXT2, false);
            l.setPadding(0, dp(8), 0, 0);
            recent.addView(l);
        } else if (ordersError != null) {
            TextView l = text(ordersError, 14, RED, true);
            l.setPadding(0, dp(8), 0, 0);
            recent.addView(l);
            recent.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveOrders(); render(); }
            }));
        } else if (orders.isEmpty()) {
            TextView l = text("এখনো কোনো অর্ডার নেই। নতুন অর্ডার করলে এখানে দেখা যাবে।", 14, TEXT2, false);
            l.setPadding(0, dp(8), 0, 0);
            recent.addView(l);
        } else {
            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                final OrderStore.Order ro = orders.get(i);
                LinearLayout rrow = new LinearLayout(this);
                rrow.setOrientation(LinearLayout.HORIZONTAL);
                rrow.setGravity(Gravity.CENTER_VERTICAL);
                rrow.setPadding(0, dp(8), 0, dp(8));
                TextView rnm = text(ro.serviceName + " • " + ro.amount, 15, TEXT, true);
                LinearLayout.LayoutParams rmp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                rrow.addView(rnm, rmp);
                rrow.addView(orderStatusChip(ro));
                rrow.setBackground(rippleBackground());
                rrow.setClickable(true);
                rrow.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { currentOrder = ro; go(S_ORDER_DETAIL, true); }
                });
                recent.addView(rrow);
            }
            recent.addView(ghostButton("সব অর্ডার দেখুন", new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_ORDERS, true); }
            }));
        }
        content.addView(recent);
    }

    private LinearLayout leadRow(final LeadsStore.Lead lead) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.addView(avatar(lead.initial(), lead.paused ? AMBER : PRIMARY));
        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(12), 0, dp(8), 0);
        TextView nm = text(lead.name, 16, TEXT, true);
        mid.addView(nm);
        TextView lm = text(lead.lastMessage == null ? "" : lead.lastMessage, 13, TEXT2, false);
        lm.setMaxLines(2);
        mid.addView(lm);
        TextView tm = text(lead.updatedAt == null ? "" : lead.updatedAt, 12, TEXT2, false);
        mid.addView(tm);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(mid, mp);
        // v8.2: unread dot on a lead row with a new customer message.
        if (unreadLeadDocIds.contains(lead.docId())) row.addView(navDot());
        row.addView(statusChip(lead));
        row.setBackground(rippleBackground());
        row.setClickable(true);
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                currentLead = lead;
                go(S_LEAD_DETAIL, true);
            }
        });
        return row;
    }

    private void showLeads() {
        content.addView(text("Messenger Leads", 26, NAVY, true));
        TextView count = text("মোট " + bn(String.valueOf(leadsStore.total())) + "টি লিড", 14, TEXT2, false);
        count.setPadding(0, dp(4), 0, dp(12));
        content.addView(count);
        // Filter chips (actually filter)
        String[][] filters = {
            {"all", "সব"}, {"handover", "হ্যান্ডওভার"},
            {"bot_replied", "বট রিপ্লাই"}, {"new", "নতুন"},
        };
        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        chips.setPadding(0, 0, 0, dp(12));
        for (final String[] f : filters) {
            final String key = f[0];
            boolean sel = key.equals(currentFilter);
            TextView c = chip(f[1] + " (" + bn(String.valueOf(
                "all".equals(key) ? leadsStore.total()
                : "handover".equals(key) ? leadsStore.handoverCount()
                : "bot_replied".equals(key) ? leadsStore.botRepliedCount()
                : leadsStore.newCount())) + ")",
                sel ? PRIMARY : SURFACE, sel ? Color.WHITE : TEXT2);
            if (!sel) {
                GradientDrawable d = roundedBorder(SURFACE, 20);
                c.setBackground(d);
            }
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, 0, dp(8), 0);
            c.setLayoutParams(cp);
            c.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    currentFilter = key;
                    render();
                }
            });
            chips.addView(c);
        }
        content.addView(chips);

        if (leadsLoading) {
            LinearLayout c = card();
            c.addView(text("লোড হচ্ছে…", 15, TEXT2, false));
            content.addView(c);
        } else if (leadsError != null) {
            LinearLayout c = card();
            c.addView(text(leadsError, 14, RED, true));
            c.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveLeads(); render(); }
            }));
            content.addView(c);
        } else {
            List<LeadsStore.Lead> list = leadsStore.filtered(currentFilter);
            if (list.isEmpty()) {
                LinearLayout c = card();
                c.addView(text("এই ফিল্টারে কোনো লিড নেই।", 15, TEXT, true));
                TextView h = text("অন্য ফিল্টার বেছে দেখুন, অথবা নতুন মেসেজের জন্য অপেক্ষা করুন।", 13, TEXT2, false);
                h.setPadding(0, dp(6), 0, 0);
                c.addView(h);
                content.addView(c);
            } else {
                LinearLayout c = card();
                for (LeadsStore.Lead l : list) c.addView(leadRow(l));
                content.addView(c);
            }
            LinearLayout foot = card();
            foot.addView(outlineButton(" সব লিডের সারাংশ কপি করুন", PRIMARY,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        copyText("leads", leadsStore.copyAllLeadsSummary());
                    }
                }));
            foot.addView(outlineButton(" লিড রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    loadLiveLeads();
                }
            }));
            content.addView(foot);
        }
    }

    // ---- v5 THREAD SCREEN: fixed header + own ScrollView + pinned composer ----
    private void scrollToThreadBottom() {
        // Post twice: once after layout, once after the messages are drawn,
        // so the thread reliably lands on the LATEST message, not the top.
        if (threadScroll == null) return;
        threadScroll.post(new Runnable() {
            @Override public void run() {
                threadScroll.fullScroll(View.FOCUS_DOWN);
                threadScroll.post(new Runnable() {
                    @Override public void run() {
                        threadScroll.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    private void renderThreadScreen() {
        final LeadsStore.Lead lead = currentLead;
        if (lead == null) { go(S_LEADS, false); return; }
        threadPane.removeAllViews();
        if (notice != null) {
            threadPane.addView(noticeCard(notice));
            notice = null;
        }
        // v8.2: banner also shows inside an open thread (for other threads/orders).
        LinearLayout threadBanner = notificationBannerView();
        if (threadBanner != null) threadPane.addView(threadBanner);

        // Fixed header: back + name + status. Never scrolls away.
        LinearLayout head = card();
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("ফিরুন");
        back.setTextSize(14);
        back.setTypeface(Typeface.DEFAULT_BOLD);
        back.setTextColor(PRIMARY);
        back.setPadding(dp(4), dp(6), dp(12), dp(6));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_LEADS, false); }
        });
        topRow.addView(back);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        TextView spacer = new TextView(this);
        topRow.addView(spacer, sp);
        topRow.addView(statusChip(lead));
        head.addView(topRow);
        LinearLayout hrow = new LinearLayout(this);
        hrow.setOrientation(LinearLayout.HORIZONTAL);
        hrow.setGravity(Gravity.CENTER_VERTICAL);
        hrow.setPadding(0, dp(10), 0, 0);
        hrow.addView(avatar(lead.initial(), lead.paused ? AMBER : PRIMARY));
        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(12), 0, dp(8), 0);
        mid.addView(text(lead.name, 18, TEXT, true));
        mid.addView(text("পেজ: automateiq", 13, TEXT2, false));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        hrow.addView(mid, mp);
        head.addView(hrow);
        // Take Over / Resume AI lives in the header so the state is visible
        // the moment the thread opens — no scrolling needed to find it.
        head.addView(outlineButton(lead.paused ? " Resume AI (বট আবার চালু করুন)"
                : " Take Over (বট থামিয়ে নিজে উত্তর দিন)",
            lead.paused ? GREEN : AMBER, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    toggleLeadPause(lead);
                }
            }));
        head.addView(ghostButton(" লিড ডিটেইলস কপি করুন",
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    copyText("lead", leadsStore.copyLeadDetails(lead));
                }
            }));
        head.addView(ghostButton(" রিফ্রেশ করুন",
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    refreshThread(lead);
                }
            }));
        threadPane.addView(head);

        // Messages area: its OWN ScrollView. This is the v5 fix — messages
        // scroll inside here, the header and composer stay put.
        threadScroll = new ScrollView(this);
        threadMessages = new LinearLayout(this);
        threadMessages.setOrientation(LinearLayout.VERTICAL);
        threadMessages.setPadding(dp(16), dp(8), dp(16), dp(8));
        if (lead.paused) {
            LinearLayout pb = card();
            pb.setBackground(rounded(AMBER_SOFT, 14));
            pb.addView(text("বট এখন থেমে আছে। আপনি নিজে উত্তর দিচ্ছেন। বট আবার চালু করতে Resume AI চাপুন।", 14, TEXT, true));
            threadMessages.addView(pb);
        }
        if (lead.messages.isEmpty()) {
            LinearLayout empty = card();
            empty.addView(text("এই লিডের এখনো কোনো মেসেজ দেখা যাচ্ছে না। নিচে লিখে প্রথম রিপ্লাই পাঠান।", 14, TEXT2, false));
            threadMessages.addView(empty);
        }
        for (final LeadsStore.Message m : lead.messages) {
            boolean isOwner = "owner".equals(m.role);
            boolean isBot = "bot".equals(m.role);
            String roleLabel = isOwner ? "আপনি (ওনার)" : isBot ? "বট" : "কাস্টমার";
            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);
            wrap.setPadding(isOwner || isBot ? dp(48) : 0, dp(6),
                (isOwner || isBot) ? 0 : dp(48), 0);
            TextView role = text(roleLabel, 11, TEXT2, true);
            role.setGravity((isOwner || isBot) ? Gravity.END : Gravity.START);
            wrap.addView(role);
            TextView bubble = new TextView(this);
            bubble.setText(m.text);
            bubble.setTextSize(14);
            bubble.setTextColor(isOwner ? Color.WHITE : TEXT);
            bubble.setPadding(dp(14), dp(11), dp(14), dp(11));
            // v3 bubbles: radius 19 (bottom corner 5 on the tail side is
            // approximated by the 19 radius — Android TextView corners are
            // uniform), owner = violet->blue gradient, bot = #E0F2FE/border.
            if (isOwner) {
                bubble.setBackground(gradient3(V3_VIOLET, V3_BLUE, V3_BLUE, 19));
            } else if (isBot) {
                GradientDrawable bd = rounded(V3_BOT_BG, 19);
                bd.setStroke(dp(1), V3_BOT_BORDER);
                bubble.setBackground(bd);
            } else {
                GradientDrawable ld = rounded(0xE6FFFFFF, 19);
                ld.setStroke(dp(1), V3_GLASS_BORDER);
                bubble.setBackground(ld);
            }
            bubble.setGravity((isOwner || isBot) ? Gravity.END : Gravity.START);
            wrap.addView(bubble);
            if (m.createdAt != null && m.createdAt.length() > 0) {
                TextView ts = text(m.createdAt, 11, TEXT2, false);
                ts.setGravity((isOwner || isBot) ? Gravity.END : Gravity.START);
                wrap.addView(ts);
            }
            // v5 SEND STATUS: unmistakable, under every owner bubble.
            // Historic owner messages (sendStatus empty) show nothing.
            if (isOwner && m.sendStatus != null && m.sendStatus.length() > 0) {
                if ("sending".equals(m.sendStatus)) {
                    TextView st = text(" পাঠানো হচ্ছে…", 12, TEXT2, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                } else if ("sent".equals(m.sendStatus)) {
                    TextView st = text(" Messenger-এ পাঠানো হয়েছে", 12, GREEN, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                } else if ("sent_pending_save".equals(m.sendStatus)) {
                    TextView st = text(" Messenger-এ পাঠানো হয়েছে • ", 12, AMBER, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                } else if ("failed".equals(m.sendStatus)) {
                    TextView st = text("পাঠানো যায়নি — আবার চেষ্টা করুন", 12, RED, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                    // v8.2: resend ONLY here — a confirmed failure.
                    wrap.addView(ghostButton("↻ এই মেসেজ আবার পাঠান",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                resendThreadMessage(lead, m);
                            }
                        }));
                } else if ("unknown".equals(m.sendStatus)) {
                    // v8.2: outcome unknown (e.g. response lost after the
                    // request was sent). NO resend button here — tapping
                    // resend blindly is exactly how duplicates happened.
                    TextView st = text("আগের মেসেজটি পাঠানো হয়েছে কিনা যাচাই হচ্ছে…", 12, AMBER, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                    wrap.addView(ghostButton("রিফ্রেশ করে দেখুন",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                checkUnknownMessage(lead, m);
                            }
                        }));
                }
            }
            threadMessages.addView(wrap);
        }
        threadScroll.addView(threadMessages, new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        threadPane.addView(threadScroll, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // PINNED composer: last child of threadPane, so it never scrolls
        // away and never sits below old messages. Keyboard uses
        // adjustResize (see AndroidManifest) so it stays visible.
        LinearLayout comp = new LinearLayout(this);
        comp.setOrientation(LinearLayout.VERTICAL);
        comp.setBackgroundColor(0xD9FFFFFF);
        comp.setElevation(0);
        comp.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        threadInput = new EditText(this);
        threadInput.setHint("রিপ্লাই লিখুন…");
        threadInput.setTextSize(14);
        threadInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        threadInput.setMinLines(1);
        threadInput.setBackground(roundedBorder(0xFFFFFFFF, 100));
        threadInput.setPadding(dp(15), dp(12), dp(15), dp(12));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(threadInput, ip);
        // v3 send: 46px circle, gradient pink->violet, white send icon, glow.
        ImageView send = new ImageView(this);
        GradientDrawable sg = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR, new int[]{V3_PINK, V3_VIOLET});
        sg.setCornerRadius(dp(23));
        send.setBackground(sg);
        send.setElevation(dp(6));
        send.setImageResource(iconRes("ic_send"));
        send.setColorFilter(Color.WHITE);
        send.setScaleType(ImageView.ScaleType.CENTER);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(dp(46), dp(46));
        slp.setMargins(dp(8), 0, 0, 0);
        send.setLayoutParams(slp);
        // v8.2: while a send for THIS thread is in flight, its send
        // button is dimmed and disabled; other threads are unaffected.
        if (sendInFlightDocs.contains(lead.docId())) {
            send.setEnabled(false);
            send.setAlpha(0.4f);
        }
        send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // v8.2: duplicate taps ignored (guarded again inside send).
                if (sendInFlightDocs.contains(lead.docId())) return;
                String msg = threadInput.getText().toString().trim();
                if (msg.length() == 0) {
                    Toast.makeText(MainActivity.this, "প্রথমে রিপ্লাই লিখুন।", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendThreadMessage(lead, msg);
            }
        });
        row.addView(send);
        comp.addView(row);
        threadPane.addView(comp);

        // v5: land on the LATEST message, every time the thread opens.
        scrollToThreadBottom();
    }

    // v5 SEND FLOW: message appears at the bottom IMMEDIATELY as "sending",
    // then flips to sent/failed. Sending ALSO auto-pauses the bot, so the
    // bot cannot answer the same customer twice.
    // v6 SEND FLOW + CHAT REFRESH FIX: the reply endpoint service is the
    // SINGLE WRITER of the owner message into database (see the bot
    // service change). The app never writes the owner message itself —
    // that would duplicate it. After a successful send the app RELOADS
    // the lead doc from database and displays the reloaded thread, so
    // Refresh and re-open show exactly what Messenger shows. If the
    // service has not deployed the history-write yet (saved=false),
    // the message stays visible locally with an honest
    // "saving to chat history…" status instead of silently pretending
    // a locally-only message is saved.
    // v8.2 SEND FLOW — confirmed mechanism (read in code, not guessed):
    // the reply call used 30s timeouts while the reply service sleeps
    // when idle and can take 50s+ to wake. On a timeout the old code
    // ran onError -> sendStatus "failed" even though the service may
    // already have sent the message via Messenger, and the resend
    // button had NO guard — so a blind resend sent the same text a
    // second time. The old code also never set "sent" on the saved
    // path: it relied on the follow-up reload replacing the thread,
    // so a slow/failed reload left the message stuck on "sending".
    // v8.2 rules: (1) the SERVICE answer decides the status — on
    // success the message is "sent" immediately, and a reload failure
    // never flips it; (2) timeouts are 90s (see helper); (3) a send
    // for a thread is guarded per-thread while in flight; (4) an
    // UNKNOWN outcome (response lost after dispatch) shows a
    // checking state with a refresh-first action and NO resend button;
    // only a CONFIRMED failure (service answered failure, or the
    // request never left) shows the resend button; (5) the service is
    // the single history writer — after success the thread reloads
    // from the service copy and the local copy is dropped, never
    // appended a second time.
    private void sendThreadMessage(final LeadsStore.Lead lead, final String msg) {
        // Per-thread in-flight guard: duplicate taps ignored.
        if (sendInFlightDocs.contains(lead.docId())) return;
        // While a previous send for this thread is unresolved, do not
        // stack another send on top of it — resolve it first.
        for (LeadsStore.Message old : lead.messages) {
            if ("unknown".equals(old.sendStatus)) {
                Toast.makeText(this, "আগের মেসেজটি পাঠানো হয়েছে কিনা যাচাই হচ্ছে…", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final LeadsStore.Message m = leadsStore.addSendingOwnerMessage(lead, msg);
        sendInFlightDocs.add(lead.docId());
        if (threadInput != null) threadInput.setText("");
        renderThreadScreen();
        if (FirebaseAuthHelper.currentUid() == null) {
            sendInFlightDocs.remove(lead.docId());
            m.sendStatus = "failed";
            Toast.makeText(this, "লগইন করা নেই। আবার লগইন করুন।", Toast.LENGTH_SHORT).show();
            renderThreadScreen();
            return;
        }
        FirebaseAuthHelper.sendLiveReplyWithSave(this, FirebaseAuthHelper.currentUid(), lead, msg,
            new FirebaseAuthHelper.ReplyCallback() {
                @Override public void onSuccess(FirebaseAuthHelper.ReplyOutcome outcome) {
                    sendInFlightDocs.remove(lead.docId());
                    handleReplySuccess(lead, m, outcome);
                }
                @Override public void onFailure(boolean unknown, String message) {
                    sendInFlightDocs.remove(lead.docId());
                    handleReplyFailure(lead, m, unknown);
                }
            });
    }

    private void resendThreadMessage(final LeadsStore.Lead lead, final LeadsStore.Message m) {
        // v8.2: resend exists ONLY for a confirmed failure. It refuses
        // to run for "unknown", "sending" or "sent" — never after a
        // timeout, where a blind resend would duplicate the message.
        if (sendInFlightDocs.contains(lead.docId())) return;
        if (!"failed".equals(m.sendStatus)) return;
        sendInFlightDocs.add(lead.docId());
        m.sendStatus = "sending";
        renderThreadScreen();
        if (FirebaseAuthHelper.currentUid() == null) {
            sendInFlightDocs.remove(lead.docId());
            m.sendStatus = "failed";
            renderThreadScreen();
            return;
        }
        FirebaseAuthHelper.sendLiveReplyWithSave(this, FirebaseAuthHelper.currentUid(), lead, m.text,
            new FirebaseAuthHelper.ReplyCallback() {
                @Override public void onSuccess(FirebaseAuthHelper.ReplyOutcome outcome) {
                    sendInFlightDocs.remove(lead.docId());
                    handleReplySuccess(lead, m, outcome);
                }
                @Override public void onFailure(boolean unknown, String message) {
                    sendInFlightDocs.remove(lead.docId());
                    handleReplyFailure(lead, m, unknown);
                }
            });
    }

    /** Shared success path for send + resend. The service answer wins:
     *  "sent" is set HERE, before any pause/reload step, so a slow or
     *  failed reload can never flip a sent message back. */
    private void handleReplySuccess(final LeadsStore.Lead lead, final LeadsStore.Message m,
                                    FirebaseAuthHelper.ReplyOutcome outcome) {
        m.sendStatus = "sent";
        notice = "Messenger-এ পাঠানো হয়েছে।";
        if (!outcome.saved) {
            // Old service: Messenger got it, history did not. Keep the
            // local copy visible with its honest pending-save status.
            m.sendStatus = "sent_pending_save";
            FirebaseAuthHelper.updateLeadStatus(lead, true, "handover",
                new FirebaseAuthHelper.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean o) { }
                    @Override public void onError(String e) { }
                });
            renderThreadScreen();
            return;
        }
        // Service saved it: pause, then reload so the thread on screen
        // IS the service-saved thread (local copy dropped, not doubled).
        FirebaseAuthHelper.updateLeadStatus(lead, true, "handover",
            new FirebaseAuthHelper.ResultCallback<Boolean>() {
                @Override public void onSuccess(Boolean o) { reloadLeadIntoThread(lead, m); }
                @Override public void onError(String e) { reloadLeadIntoThread(lead, m); }
            });
    }

    /** Shared failure path. Unknown is NOT failed: it gets the
     *  checking state, and only a refresh-first check may resolve it. */
    private void handleReplyFailure(final LeadsStore.Lead lead, final LeadsStore.Message m, boolean unknown) {
        m.sendStatus = unknown ? "unknown" : "failed";
        renderThreadScreen();
    }

    /** Reload ONE lead doc from the database and swap it into the list
     *  and the open thread. This is what makes Refresh truthful.
     *  v8.2: if the just-sent local message is already in the reloaded
     *  thread, the service copy wins and the local copy is dropped
     *  (never shown twice). If it is missing, the local "sent" copy
     *  is kept and a plain refresh hint is shown. A reload FAILURE
     *  leaves the "sent" status untouched — it never flips to failed. */
    private void reloadLeadIntoThread(final LeadsStore.Lead lead, final LeadsStore.Message sentLocal) {
        if (FirebaseAuthHelper.currentUid() == null) {
            renderThreadScreen();
            return;
        }
        FirebaseAuthHelper.fetchLead(lead.pageId, lead.psid,
            new FirebaseAuthHelper.ResultCallback<LeadsStore.Lead>() {
                @Override public void onSuccess(LeadsStore.Lead reloaded) {
                    if (sentLocal != null && !containsOwnerText(reloaded, sentLocal.text)) {
                        reloaded.messages.add(sentLocal);
                        notice = "Messenger-এ পাঠানো হয়েছে — তালিকায় না দেখালে রিফ্রেশ করে দেখুন।";
                    }
                    List<LeadsStore.Lead> all = leadsStore.all();
                    for (int i = 0; i < all.size(); i++) {
                        if (all.get(i).docId().equals(reloaded.docId())) {
                            all.set(i, reloaded);
                            break;
                        }
                    }
                    currentLead = reloaded;
                    renderThreadScreen();
                }
                @Override public void onError(String message) {
                    // v8.2: reload failure is NOT a send failure. The
                    // message keeps its "sent" status; plain hint only.
                    if (notice == null) notice = "Messenger-এ পাঠানো হয়েছে।";
                    renderThreadScreen();
                }
            });
    }

    private boolean containsOwnerText(LeadsStore.Lead lead, String text) {
        if (lead == null || text == null) return false;
        for (LeadsStore.Message m : lead.messages) {
            if ("owner".equals(m.role) && text.equals(m.text)) return true;
        }
        return false;
    }

    /** v8.2 UNKNOWN resolution — refresh FIRST, resend never blindly.
     *  Reload the thread: if the service copy already has this text,
     *  it did go (show sent). Only if the reload proves it absent is
     *  the user allowed to send again — as a fresh send: the text is
     *  put back in the composer for one explicit tap, not re-sent
     *  silently behind the refresh. */
    private void checkUnknownMessage(final LeadsStore.Lead lead, final LeadsStore.Message m) {
        if (FirebaseAuthHelper.currentUid() == null) {
            renderThreadScreen();
            return;
        }
        FirebaseAuthHelper.fetchLead(lead.pageId, lead.psid,
            new FirebaseAuthHelper.ResultCallback<LeadsStore.Lead>() {
                @Override public void onSuccess(LeadsStore.Lead reloaded) {
                    List<LeadsStore.Lead> all = leadsStore.all();
                    for (int i = 0; i < all.size(); i++) {
                        if (all.get(i).docId().equals(reloaded.docId())) {
                            all.set(i, reloaded);
                            break;
                        }
                    }
                    currentLead = reloaded;
                    if (containsOwnerText(reloaded, m.text)) {
                        notice = "Messenger-এ পাঠানো হয়েছে।";
                        renderThreadScreen();
                    } else {
                        notice = "মেসেজটি পাঠানো হয়নি — আবার পাঠান।";
                        renderThreadScreen();
                        if (threadInput != null) threadInput.setText(m.text);
                    }
                }
                @Override public void onError(String message) {
                    Toast.makeText(MainActivity.this,
                        "রিফ্রেশ হয়নি — আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /** Thread Refresh button: reload from the database. If the
     *  just-sent message is missing, the service history-write has not
     *  deployed yet — say so, instead of showing a stale thread. */
    private void refreshThread(final LeadsStore.Lead lead) {
        if (FirebaseAuthHelper.currentUid() == null) {
            renderThreadScreen();
            return;
        }
        final int beforeCount = lead.messages.size();
        FirebaseAuthHelper.fetchLead(lead.pageId, lead.psid,
            new FirebaseAuthHelper.ResultCallback<LeadsStore.Lead>() {
                @Override public void onSuccess(LeadsStore.Lead reloaded) {
                    List<LeadsStore.Lead> all = leadsStore.all();
                    for (int i = 0; i < all.size(); i++) {
                        if (all.get(i).docId().equals(reloaded.docId())) {
                            all.set(i, reloaded);
                            break;
                        }
                    }
                    currentLead = reloaded;
                    if (reloaded.messages.size() < beforeCount) {
                        notice = "নতুন মেসেজ দেখা যাচ্ছে না — আবার চেষ্টা করুন। Messenger-এ মেসেজটি পাঠানো হয়েছে।";
                    } else {
                        Toast.makeText(MainActivity.this, "রিফ্রেশ হয়েছে।", Toast.LENGTH_SHORT).show();
                    }
                    renderThreadScreen();
                }
                @Override public void onError(String message) {
                    Toast.makeText(MainActivity.this,
                        "রিফ্রেশ হয়নি — আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show();
                }
            });
    }

    // ---- v6: ORDERS (in-app, same database 'orders' as app) ----

    // ---- v8.2: IN-APP NOTIFICATIONS ----
    // Honest limit (also in the README): this is NOT true closed-app
    // push. Nothing arrives while the app is fully closed. A banner,
    // nav/row badges and a phone notification appear when the app is
    // open (or in the background) and refreshes. True closed-app push
    // needs a cloud-messaging sender on the service and is the
    // documented next step, not built here.
    // Detection runs on every loadLiveLeads/loadLiveOrders refresh and
    // compares against stored fingerprints, so each new item raises
    // exactly one banner and one phone notification — never one per
    // refresh. Two fingerprints per item are kept: "seen" (updated only
    // when the thread/order is opened, so badges survive until read)
    // and "notified" (updated on detection, so nothing re-fires).
    private android.content.SharedPreferences seenPrefs() {
        return getSharedPreferences("automateiq_seen_v82", Context.MODE_PRIVATE);
    }

    private String leadFingerprint(LeadsStore.Lead l) {
        // Customer messages ONLY: owner/bot replies and timestamps
        // must not trip a "new customer message" badge.
        String lastUser = "";
        int userCount = 0;
        for (LeadsStore.Message m : l.messages) {
            if ("user".equals(m.role)) { lastUser = m.text == null ? "" : m.text; userCount++; }
        }
        return userCount + "|" + lastUser;
    }

    private String shortPreview(String text) {
        if (text == null) return "";
        String t = text.trim();
        if (t.length() > 40) return t.substring(0, 40) + "…";
        return t;
    }

    private String lastCustomerText(LeadsStore.Lead l) {
        String last = "";
        for (LeadsStore.Message m : l.messages) {
            if ("user".equals(m.role) && m.text != null) last = m.text;
        }
        return last;
    }

    private void markLeadRead(LeadsStore.Lead lead) {
        String uid = FirebaseAuthHelper.currentUid();
        if (uid == null || lead == null) return;
        // Use the freshest copy in the store when available.
        LeadsStore.Lead fresh = lead;
        for (LeadsStore.Lead l : leadsStore.all()) {
            if (l.docId().equals(lead.docId())) { fresh = l; break; }
        }
        seenPrefs().edit()
            .putString("lead_seen_" + uid + "_" + lead.docId(), leadFingerprint(fresh))
            .apply();
        unreadLeadDocIds.remove(lead.docId());
    }

    private void markOrderRead(OrderStore.Order o) {
        String uid = FirebaseAuthHelper.currentUid();
        if (uid == null || o == null) return;
        java.util.Set<String> seen = prefIdSet("order_seen_" + uid);
        seen.add(o.id);
        prefPutIdSet("order_seen_" + uid, seen);
        unreadOrderIds.remove(o.id);
    }

    private java.util.Set<String> prefIdSet(String key) {
        java.util.Set<String> out = new java.util.HashSet<String>();
        String raw = seenPrefs().getString(key, "");
        if (raw == null || raw.length() == 0) return out;
        for (String part : raw.split(",")) {
            if (part.length() > 0) out.add(part);
        }
        return out;
    }

    private void prefPutIdSet(String key, java.util.Set<String> ids) {
        StringBuilder sb = new StringBuilder();
        for (String id : ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(id);
        }
        seenPrefs().edit().putString(key, sb.toString()).apply();
    }

    private void detectNewLeads(java.util.List<LeadsStore.Lead> live) {
        String uid = FirebaseAuthHelper.currentUid();
        if (uid == null || live == null) return;
        android.content.SharedPreferences prefs = seenPrefs();
        if (!prefs.getBoolean("leads_seeded_" + uid, false)) {
            // First load seeds silently: existing messages are not
            // "new", so no badge storm on first login.
            android.content.SharedPreferences.Editor ed = prefs.edit();
            for (LeadsStore.Lead l : live) {
                String fp = leadFingerprint(l);
                ed.putString("lead_seen_" + uid + "_" + l.docId(), fp);
                ed.putString("lead_notified_" + uid + "_" + l.docId(), fp);
            }
            ed.putBoolean("leads_seeded_" + uid, true);
            ed.apply();
            return;
        }
        android.content.SharedPreferences.Editor ed = prefs.edit();
        boolean changed = false;
        LeadsStore.Lead bannerLead = null;
        String bannerPreview = null;
        for (LeadsStore.Lead l : live) {
            String fp = leadFingerprint(l);
            String notified = prefs.getString("lead_notified_" + uid + "_" + l.docId(), null);
            if (notified == null || !notified.equals(fp)) {
                // Genuinely new customer message (or a brand-new lead).
                ed.putString("lead_notified_" + uid + "_" + l.docId(), fp);
                changed = true;
                unreadLeadDocIds.add(l.docId());
                if (bannerLead == null) { bannerLead = l; bannerPreview = lastCustomerText(l); }
                postLocalNotification("নতুন মেসেজ: " + l.name,
                    shortPreview(lastCustomerText(l)), "lead_" + l.docId());
            }
            String seen = prefs.getString("lead_seen_" + uid + "_" + l.docId(), null);
            if (seen == null || !seen.equals(fp)) unreadLeadDocIds.add(l.docId());
            else unreadLeadDocIds.remove(l.docId());
        }
        if (changed) ed.apply();
        // Prune badges for leads that no longer exist.
        java.util.Set<String> liveIds = new java.util.HashSet<String>();
        for (LeadsStore.Lead l : live) liveIds.add(l.docId());
        unreadLeadDocIds.retainAll(liveIds);
        if (bannerLead != null) {
            pendingBannerLead = bannerLead;
            pendingBannerOrder = null;
            pendingBannerText = "নতুন মেসেজ: " + bannerLead.name + " — " + shortPreview(bannerPreview);
        }
    }

    private void detectNewOrders(java.util.List<OrderStore.Order> live) {
        // v8.2: order notifications are admin-only by design.
        if (!FirebaseAuthHelper.isAdmin()) return;
        String uid = FirebaseAuthHelper.currentUid();
        if (uid == null || live == null) return;
        android.content.SharedPreferences prefs = seenPrefs();
        java.util.Set<String> seen = prefIdSet("order_seen_" + uid);
        java.util.Set<String> notified = prefIdSet("order_notified_" + uid);
        if (!prefs.getBoolean("orders_seeded_" + uid, false)) {
            // First load seeds silently: existing orders are not new.
            for (OrderStore.Order o : live) { seen.add(o.id); notified.add(o.id); }
            prefPutIdSet("order_seen_" + uid, seen);
            prefPutIdSet("order_notified_" + uid, notified);
            prefs.edit().putBoolean("orders_seeded_" + uid, true).apply();
            return;
        }
        OrderStore.Order bannerOrder = null;
        boolean changed = false;
        // live orders arrive newest-first, so the first new pending
        // order is the banner; every new one still gets its own phone
        // notification below (one per item, never per refresh).
        for (OrderStore.Order o : live) {
            if (!"pending".equals(o.status)) {
                if (seen.contains(o.id)) unreadOrderIds.remove(o.id);
                continue;
            }
            if (!notified.contains(o.id)) {
                notified.add(o.id);
                changed = true;
                unreadOrderIds.add(o.id);
                if (bannerOrder == null) bannerOrder = o;
                String amount = o.amount == null ? "" : o.amount;
                if (amount.length() > 0 && !amount.startsWith("৳")) amount = "৳" + amount;
                postLocalNotification("নতুন অর্ডার: " + o.serviceName,
                    amount.length() > 0 ? amount : "নতুন অর্ডার এসেছে", "order_" + o.id);
            }
            if (!seen.contains(o.id)) unreadOrderIds.add(o.id);
            else unreadOrderIds.remove(o.id);
        }
        if (changed) prefPutIdSet("order_notified_" + uid, notified);
        java.util.Set<String> liveOrderIds = new java.util.HashSet<String>();
        for (OrderStore.Order o : live) liveOrderIds.add(o.id);
        unreadOrderIds.retainAll(liveOrderIds);
        if (bannerOrder != null) {
            String amount = bannerOrder.amount == null ? "" : bannerOrder.amount;
            if (amount.length() > 0 && !amount.startsWith("৳")) amount = "৳" + amount;
            pendingBannerOrder = bannerOrder;
            pendingBannerLead = null;
            pendingBannerText = "নতুন অর্ডার: " + bannerOrder.serviceName
                + (amount.length() > 0 ? " — " + amount : "");
        }
    }

    private View navDot() {
        TextView d = new TextView(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(RED);
        d.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(8), dp(8));
        lp.setMargins(dp(4), 0, 0, 0);
        d.setLayoutParams(lp);
        return d;
    }

    /** One tappable banner for the latest new item. Tapping opens its
     *  thread/order (which also marks it read and clears the banner). */
    private LinearLayout notificationBannerView() {
        if (pendingBannerText == null) return null;
        final LeadsStore.Lead bLead = pendingBannerLead;
        final OrderStore.Order bOrder = pendingBannerOrder;
        LinearLayout b = card();
        b.setBackground(rounded(SOFT_BLUE, 16));
        b.addView(text(pendingBannerText, 14, TEXT, true));
        b.addView(text("খুলতে এখানে চাপুন", 12, TEXT2, false));
        b.setClickable(true);
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (bLead != null) {
                    LeadsStore.Lead fresh = bLead;
                    for (LeadsStore.Lead l : leadsStore.all()) {
                        if (l.docId().equals(bLead.docId())) { fresh = l; break; }
                    }
                    currentLead = fresh;
                    pendingBannerText = null; pendingBannerLead = null;
                    go(S_LEAD_DETAIL, true);
                } else if (bOrder != null) {
                    OrderStore.Order fresh = bOrder;
                    for (OrderStore.Order o : orders) {
                        if (o.id.equals(bOrder.id)) { fresh = o; break; }
                    }
                    currentOrder = fresh;
                    pendingBannerText = null; pendingBannerOrder = null;
                    go(S_ORDER_DETAIL, true);
                }
            }
        });
        return b;
    }

    /** Standard phone notification (system sound/vibration only). If
     *  permission is missing or the manager is unavailable, this
     *  silently does nothing — the in-app banner and badges still work. */
    private void postLocalNotification(String title, String body, String tag) {
        try {
            if (Build.VERSION.SDK_INT >= 33
                    && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) return;
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(NOTIF_CHANNEL_ID,
                    "নতুন খবর", NotificationManager.IMPORTANCE_DEFAULT);
                ch.setDescription("নতুন মেসেজ আর অর্ডারের খবর");
                nm.createNotificationChannel(ch);
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder b;
            if (Build.VERSION.SDK_INT >= 26) b = new Notification.Builder(this, NOTIF_CHANNEL_ID);
            else b = new Notification.Builder(this);
            int icon = iconRes("ic_bell");
            if (icon == 0) icon = android.R.drawable.ic_dialog_info;
            b.setContentTitle(title).setContentText(body).setSmallIcon(icon)
                .setContentIntent(pi).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL);
            nm.notify(tag, (Math.abs(tag.hashCode()) % 100000) + 1000, b.build());
        } catch (Exception ignored) { }
    }

    /** Ask once after sign-in / first use. Never blocks login: a
     *  denial only means phone notifications stay off. */
    private void maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return;
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) return;
        if (seenPrefs().getBoolean("notif_asked", false)) return;
        seenPrefs().edit().putBoolean("notif_asked", true).apply();
        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, RC_NOTIFICATIONS);
    }

    private OrderStore.OrderPackage findMonthlyPackage(String service, String pkgLabel) {
        for (OrderStore.OrderPackage p : OrderStore.PACKAGES) {
            if (!"monthly".equals(p.billing)) continue;
            if (!p.service.equals(service)) continue;
            if ("স্ট্যান্ডার্ড".equals(pkgLabel) && p.pkg.contains("স্ট্যান্ডার্ড")) return p;
            if ("প্রিমিয়াম".equals(pkgLabel) && p.pkg.contains("প্রিমিয়াম")) return p;
            if ("মাসিক".equals(pkgLabel) && p.pkg.contains("মাসিক")) return p;
        }
        return null;
    }

    private void deriveMySubscription() {
                // v7: admins have no My Subscription — never derive one for
        // the admin account from the all-orders list.
        if (FirebaseAuthHelper.isAdmin()) { mySubscription = null; mySubscriptionOrderStatus = ""; return; }
        SubscriptionStore.Subscription derived =
            OrderStore.subscriptionFromOrders(orders);
        mySubscription = derived;
        mySubscriptionOrderStatus = "";
        if (derived != null) {
            for (OrderStore.Order o : orders) {
                if (o.isMonthly() && !"cancelled".equals(o.status)
                    && !"rejected".equals(o.status)
                    && o.dateMillis == derived.startMillis) {
                    mySubscriptionOrderStatus = o.status;
                    break;
                }
            }
        }
    }

    private void loadLiveOrders() {
        String uid = FirebaseAuthHelper.currentUid();
        if (uid == null) {
            ordersError = "লগইন করা নেই। আবার লগইন করুন।";
            ordersLoading = false;
            return;
        }
        ordersLoading = true;
        ordersError = null;
        autoExpireRunForLoad = false;
        final boolean admin = FirebaseAuthHelper.isAdmin();
        FirebaseAuthHelper.fetchOrders(uid, admin,
            new FirebaseAuthHelper.ResultCallback<List<OrderStore.Order>>() {
                @Override public void onSuccess(List<OrderStore.Order> live) {
                    detectNewOrders(live);
                    orders = live;
                    openPendingOrderIfAny();
                    ordersLoaded = true;
                    ordersLoading = false;
                    ordersError = null;
                    deriveMySubscription();
                    // v7 AUTO-EXPIRE (admin only, once per load): monthly
                    // orders past expiry are auto-cancelled in database.
                    if (FirebaseAuthHelper.isAdmin()) runAutoExpire();
                    if (pendingBannerText != null) render();
                    else if (screen == S_DASH || screen == S_ORDERS || screen == S_MY_SUB) render();
                }
                @Override public void onError(String message) {
                    ordersLoading = false;
                    ordersError = message;
                    if (screen == S_DASH || screen == S_ORDERS || screen == S_MY_SUB) render();
                }
            });
    }

    private TextView orderStatusChip(OrderStore.Order o) {
        if ("in_progress".equals(o.status)) return chip(" চলমান", SOFT_BLUE, PRIMARY);
        if ("completed".equals(o.status)) return chip(" সম্পন্ন", GREEN_SOFT, GREEN);
        if ("cancelled".equals(o.status) || "rejected".equals(o.status)) return chip(" বাতিল", RED_SOFT, RED);
        return chip(" অপেক্ষমাণ", AMBER_SOFT, AMBER);
    }

    private void showOrders() {
        content.addView(text("অর্ডার", 26, NAVY, true));
        final boolean admin = FirebaseAuthHelper.isAdmin();
        if (admin) {
            content.addView(bannerCard("অ্যাডমিন প্যানেল — সব কাস্টমারের অর্ডার। এখানে অর্ডার ম্যানেজ করা হয়: স্ট্যাটাস বদলানো, বাতিল করা, অ্যাডমিন মেসেজ। অ্যাডমিন এখান থেকে নিজের জন্য অর্ডার করবেন না।", GREEN_SOFT, GREEN));
            if (autoExpireCount > 0) {
                content.addView(bannerCard(" " + bn(String.valueOf(autoExpireCount)) + "টি মাসিক অর্ডারের অফার শেষ হয়েছে — স্বয়ংক্রিয়ভাবে বাতিল করা হয়েছে। বাতিল অর্ডারগুলো তালিকায় দেখা যাবে, মুছে ফেলা হয়নি।", AMBER_SOFT, AMBER));
            }
            LinearLayout ruleNote = card();
            ruleNote.setBackground(rounded(AMBER_SOFT, 20));
            ruleNote.addView(text("অন্য কাস্টমারের অ্যাকাউন্টে নতুন অর্ডার/সাবস্ক্রিপশন তৈরি করা এখনো চালু হয়নি। অ্যাডমিন এখানে অর্ডার ম্যানেজ করবেন, তৈরি করবেন না।", 13, TEXT, false));
            content.addView(ruleNote);
        } else {
            content.addView(bannerCard("শুধু আপনার নিজের অর্ডার দেখা যাচ্ছে। অন্যের অর্ডার এখানে দেখানো হয় না।", SOFT_BLUE, PRIMARY));
        }
        // v7: the order form belongs to customers only. Admins
        // manage orders here; they never place orders for themselves.
        if (!admin) {
            content.addView(primaryButton("＋ নতুন অর্ডার করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { selectedPackage = null; go(S_ORDER_FORM, true); }
            }));
        }
        // v3 orders filter: horizontal chip rail (not a vertical list card).
        HorizontalScrollView fscroll = new HorizontalScrollView(this);
        fscroll.setHorizontalScrollBarEnabled(false);
        LinearLayout frail = new LinearLayout(this);
        frail.setOrientation(LinearLayout.HORIZONTAL);
        frail.setPadding(0, 0, 0, dp(10));
        String[][] fdefs = {{"all", "সব"}, {"pending", "অপেক্ষমাণ"},
            {"in_progress", "চলমান"}, {"completed", "সম্পন্ন"}, {"cancelled", "বাতিল"}};
        for (final String[] f : fdefs) {
            final String key = f[0];
            TextView ch = new TextView(this);
            ch.setText(f[1]);
            ch.setTextSize(12);
            ch.setTypeface(Typeface.DEFAULT_BOLD);
            ch.setPadding(dp(11), dp(5), dp(11), dp(5));
            if (orderFilter.equals(key)) {
                ch.setTextColor(Color.WHITE);
                ch.setBackground(gradient3(V3_VIOLET, V3_BLUE, V3_CYAN, 100));
            } else {
                ch.setTextColor(V3_GREY_FG);
                ch.setBackground(rounded(0xD9FFFFFF, 100));
            }
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.setMargins(0, 0, dp(8), 0);
            ch.setLayoutParams(clp);
            ch.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { orderFilter = key; render(); }
            });
            frail.addView(ch);
        }
        fscroll.addView(frail);
        content.addView(fscroll);
        if (ordersLoading) {
            content.addView(shimmerBar());
            content.addView(shimmerBar());
            content.addView(shimmerBar());
            content.addView(text("লোড হচ্ছে…", 14, TEXT2, false));
            return;
        }
        if (ordersError != null) {
            content.addView(text(ordersError, 14, RED, true));
            content.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveOrders(); render(); }
            }));
            return;
        }
        List<OrderStore.Order> shown = new ArrayList<OrderStore.Order>();
        for (OrderStore.Order o : orders) {
            if ("all".equals(orderFilter) || orderFilter.equals(o.status)) shown.add(o);
        }
        if (shown.isEmpty()) {
            LinearLayout c = card();
            c.addView(text("এখনো কোনো অর্ডার নেই", 16, TEXT, true));
            TextView t = text("নতুন অর্ডার করলে এখানে দেখা যাবে — স্ট্যাটাস Pending থেকে শুরু হবে, অ্যাডমিন অনুমোদন দিলে চলমান হবে।", 14, TEXT2, false);
            t.setPadding(0, dp(6), 0, 0);
            c.addView(t);
            content.addView(c);
            return;
        }
        // v3: date-grouped timeline — today / last 7 days / earlier.
        java.util.LinkedHashMap<String, java.util.List<OrderStore.Order>> groups =
            new java.util.LinkedHashMap<String, java.util.List<OrderStore.Order>>();
        long nowMs = System.currentTimeMillis();
        for (OrderStore.Order go : shown) {
            long age = nowMs - go.dateMillis;
            String bucket = (go.dateMillis <= 0 || age < 0) ? "আগে"
                : (age < 24L * 60 * 60 * 1000) ? "আজ"
                : (age < 7L * 24 * 60 * 60 * 1000) ? "গত ৭ দিন" : "আগে";
            if (!groups.containsKey(bucket)) groups.put(bucket, new java.util.ArrayList<OrderStore.Order>());
            groups.get(bucket).add(go);
        }
        for (java.util.Map.Entry<String, java.util.List<OrderStore.Order>> grp : groups.entrySet()) {
        TextView ghead = text(grp.getKey(), 18, NAVY, true);
        ghead.setPadding(0, dp(6), 0, dp(8));
        content.addView(ghead);
        for (final OrderStore.Order o : grp.getValue()) {
            LinearLayout row = card();
            LinearLayout dotRow = new LinearLayout(this);
            dotRow.setOrientation(LinearLayout.HORIZONTAL);
            dotRow.setGravity(Gravity.CENTER_VERTICAL);
            dotRow.addView(statusDot(statusColor(o.status)));
            TextView dotLab = text(o.dateMillis > 0 ? SubscriptionStore.formatDate(o.dateMillis) : "", 13, TEXT2, false);
            dotRow.addView(dotLab);
            row.addView(dotRow);
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            TextView nm = text(o.serviceName, 16, TEXT, true);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            top.addView(nm, mp);
            // v8.2: unread dot on a newly arrived pending order row.
            if (unreadOrderIds.contains(o.id)) top.addView(navDot());
            top.addView(orderStatusChip(o));
            row.addView(top);
            row.addView(text(o.packageDetails + "  •  " + o.amount, 14, TEXT, false));
            row.addView(text("কাস্টমার: " + o.userName + "  •  TrxID: " + o.trxId, 13, TEXT2, false));
            if (o.isMonthly()) {
                SubscriptionStore.Subscription sub = SubscriptionStore.fromOrder(
                    o.serviceName, o.packageDetails, o.amount, o.dateMillis, 1);
                if (monthlyExpired(o) && !"cancelled".equals(o.status)) {
                    row.addView(text(" মেয়াদ শেষ — স্বয়ংক্রিয়ভাবে বন্ধ", 13, RED, true));
                } else {
                    row.addView(text("সাবস্ক্রিপশন: " + SubscriptionStore.daysRemainingBn(sub)
                        + "  •  শেষ: " + SubscriptionStore.formatDate(sub.expiryMillis), 13, PRIMARY, true));
                }
            }
            row.setBackground(rippleBackground());
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { currentOrder = o; go(S_ORDER_DETAIL, true); }
            });
            content.addView(row);
        }
        }
        content.addView(outlineButton(" অর্ডার রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) {
                loadLiveOrders();
            }
        }));
    }

    private void showOrderDetail() {
        final OrderStore.Order o = currentOrder;
        if (o == null) { go(S_ORDERS, false); return; }
        content.addView(ghostButton("অর্ডারে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ORDERS, false); }
        }));
        LinearLayout c = card();
        c.setBackground(heroShiftDrawable());
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView nm = text(o.serviceName, 20, Color.WHITE, true);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        top.addView(nm, mp);
        top.addView(orderStatusChip(o));
        c.addView(top);
        c.addView(text(o.packageDetails + "  •  " + o.amount, 22, Color.WHITE, true));
        c.addView(text("কাস্টমার: " + o.userName, 14, Color.WHITE, false));
        c.addView(text("পেমেন্ট: " + o.paymentMethod + "  •  Sender: " + o.senderPhone, 14, TEXT, false));
        c.addView(text("TrxID: " + o.trxId, 14, TEXT, false));
        if (o.clientPageLink != null && o.clientPageLink.length() > 0)
            c.addView(text("পেজ লিংক: " + o.clientPageLink, 14, TEXT, false));
        if (o.clientRequirements != null && o.clientRequirements.length() > 0)
            c.addView(text("চাহিদা: " + o.clientRequirements, 14, TEXT, false));
        if (o.startDate != null && o.startDate.length() > 0)
            c.addView(text("শুরু: " + o.startDate, 14, TEXT2, false));
        if (o.completionDate != null && o.completionDate.length() > 0)
            c.addView(text("সম্পন্ন: " + o.completionDate, 14, TEXT2, false));
        content.addView(c);
        if (o.isMonthly() && monthlyExpired(o)) {
            LinearLayout ec = card();
            ec.setBackground(rounded(RED_SOFT, 20));
            ec.addView(text(" অফার শেষ — এই মাসিক অর্ডারের মেয়াদ শেষ হয়েছে।", 15, RED, true));
            ec.addView(text("মেয়াদ শেষ হলে অর্ডার স্বয়ংক্রিয়ভাবে বন্ধ হয়ে যায় (স্ট্যাটাস বাতিল) — অর্ডারটি তালিকায় দেখা যাবে, মুছে ফেলা হয় না।", 13, TEXT, false));
            content.addView(ec);
        }
        if (o.isMonthly()) {
            SubscriptionStore.Subscription sub = SubscriptionStore.fromOrder(
                o.serviceName, o.packageDetails, o.amount, o.dateMillis, 1);
            LinearLayout sc = card();
            sc.addView(text("সাবস্ক্রিপশন (মাসিক)", 16, TEXT, true));
            sc.addView(text(SubscriptionStore.daysRemainingBn(sub), 26, PRIMARY, true));
            sc.addView(progressBar(sub.progressPercent()));
            sc.addView(text("শুরু: " + SubscriptionStore.formatDate(sub.startMillis)
                + "  •  শেষ: " + SubscriptionStore.formatDate(sub.expiryMillis), 14, TEXT2, false));
            sc.addView(sub.isActive() ? chip("চালু আছে", GREEN_SOFT, GREEN)
                : chip("মেয়াদ শেষ", RED_SOFT, RED));
            if ("pending".equals(o.status)) {
                sc.addView(text("অর্ডারটি Pending — অ্যাডমিন অনুমোদন দিলে সার্ভিস সক্রিয় হবে।", 13, AMBER, true));
            }
            content.addView(sc);
        } else {
            LinearLayout nc = card();
            nc.addView(text("এটি মাসিক সাবস্ক্রিপশন নয় — তাই এখানে দিনের হিসাব দেখানো হয় না।", 13, TEXT, false));
            content.addView(nc);
        }
        final boolean admin = FirebaseAuthHelper.isAdmin();
        if (admin) {
            LinearLayout ac = card();
            ac.addView(text("অ্যাডমিন — স্ট্যাটাস বদলান", 16, TEXT, true));
            if ("pending".equals(o.status)) {
                ac.addView(primaryButton(" In Progress করুন", new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "in_progress"); }
                }));
                ac.addView(outlineButton(" Cancel করুন", RED, new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "cancelled"); }
                }));
            } else if ("in_progress".equals(o.status)) {
                ac.addView(primaryButton(" Completed করুন", new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "completed"); }
                }));
                ac.addView(outlineButton(" Cancel করুন", RED, new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "cancelled"); }
                }));
            } else {
                ac.addView(text("এই অর্ডারের স্ট্যাটাস আর বদলানো যাবে না।", 14, TEXT2, false));
            }
            final EditText msgInput = new EditText(this);
            msgInput.setHint("অ্যাডমিন মেসেজ লিখুন…");
            msgInput.setTextSize(15);
            msgInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            msgInput.setMinLines(2);
            msgInput.setBackground(roundedBorder(SOFT, 12));
            msgInput.setPadding(dp(12), dp(10), dp(12), dp(10));
            if (o.adminMessage != null) msgInput.setText(o.adminMessage);
            ac.addView(msgInput);
            ac.addView(primaryButton("অ্যাডমিন মেসেজ সেভ করুন", new View.OnClickListener() {
                @Override public void onClick(View v) {
                    final String msg = msgInput.getText().toString().trim();
                    java.util.Map<String, Object> updates = new java.util.HashMap<String, Object>();
                    updates.put("adminMessage", msg);
                    FirebaseAuthHelper.updateOrderFields(o.id, updates,
                        new FirebaseAuthHelper.ResultCallback<Boolean>() {
                            @Override public void onSuccess(Boolean ok) {
                                o.adminMessage = msg;
                                notice = "অ্যাডমিন মেসেজ সেভ হয়েছে।";
                                render();
                            }
                            @Override public void onError(String message) {
                                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                            }
                        });
                }
            }));
            content.addView(ac);
        } else {
            LinearLayout nc = card();
            nc.addView(text("স্ট্যাটাস শুধু অ্যাডমিন বদলাতে পারবেন। কিছু জানার থাকলে হোম থেকে সাপোর্ট / ইনবক্সে লিখুন।", 13, TEXT2, false));
            content.addView(nc);
        }
        if (o.adminMessage != null && o.adminMessage.length() > 0) {
            LinearLayout mc = card();
            mc.addView(text("অ্যাডমিন মেসেজ", 16, TEXT, true));
            mc.addView(text(o.adminMessage, 14, TEXT, false));
            content.addView(mc);
        }
    }

    private void changeOrderStatus(final OrderStore.Order o, final String newStatus) {
        java.util.Map<String, Object> updates = new java.util.HashMap<String, Object>();
        updates.put("status", newStatus);
        if ("in_progress".equals(newStatus) && (o.startDate == null || o.startDate.length() == 0)) {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            updates.put("startDate", f.format(new java.util.Date()));
        }
        if ("completed".equals(newStatus)) {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            updates.put("completionDate", f.format(new java.util.Date()));
        }
        FirebaseAuthHelper.updateOrderFields(o.id, updates,
            new FirebaseAuthHelper.ResultCallback<Boolean>() {
                @Override public void onSuccess(Boolean ok) {
                    o.status = newStatus;
                    if (updates.containsKey("startDate")) o.startDate = String.valueOf(updates.get("startDate"));
                    if (updates.containsKey("completionDate")) o.completionDate = String.valueOf(updates.get("completionDate"));
                    deriveMySubscription();
                    notice = "স্ট্যাটাস " + o.statusBn() + " হয়েছে।";
                    render();
                }
                @Override public void onError(String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });
    }

    private void showOrderForm() {
        content.addView(ghostButton("অর্ডারে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ORDERS, false); }
        }));
        content.addView(text("নতুন অর্ডার", 26, NAVY, true));
        content.addView(stepProgress(selectedPackage == null ? 1 : 2));
        final boolean admin = FirebaseAuthHelper.isAdmin();
        if (admin) {
            LinearLayout ruleNote = card();
            ruleNote.setBackground(rounded(AMBER_SOFT, 20));
            ruleNote.addView(text("এই অর্ডার আপনার নিজের অ্যাকাউন্টে তৈরি হবে। অন্য কাস্টমারের অ্যাকাউন্টে অর্ডার যোগ করা এখনো চালু হয়নি।", 13, TEXT, false));
            content.addView(ruleNote);
        }
        if (selectedPackage == null) {
            // Step 1: pick a package (verified prices only, amount auto-filled).
            content.addView(text("প্যাকেজ বেছে নিন — দাম নিজে থেকে বসবে।", 14, TEXT2, false));
            for (final OrderStore.OrderPackage pkg : OrderStore.PACKAGES) {
                LinearLayout c = card();
                c.addView(text(pkg.service, 16, TEXT, true));
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dp(6), 0, 0);
                row.addView(chip(pkg.pkg, SOFT_BLUE, PRIMARY));
                TextView pr = text("  " + pkg.price, 18, PRIMARY, true);
                row.addView(pr);
                c.addView(row);
                if ("monthly".equals(pkg.billing)) {
                    c.addView(text("মাসিক সাবস্ক্রিপশন — অর্ডার হলে কত দিন বাকি আছে দেখা যাবে।", 13, TEXT2, false));
                } else if ("one_time".equals(pkg.billing)) {
                    c.addView(text("এককালীন — দিনের হিসাব দেখানো হয় না (আলাদা ফাংশন পরে ঠিক করা হবে)।", 13, TEXT2, false));
                }
                c.addView(primaryButton("এই প্যাকেজ দিয়ে অর্ডার করুন", new View.OnClickListener() {
                    @Override public void onClick(View v) { selectedPackage = pkg; render(); }
                }));
                content.addView(c);
            }
            return;
        }
        // Step 2: payment form (buyer's own payment details, like app).
        final OrderStore.OrderPackage pkg = selectedPackage;
        LinearLayout pc = card();
        pc.addView(text(pkg.service, 18, NAVY, true));
        pc.addView(text(pkg.pkg + "  •  " + pkg.price, 16, PRIMARY, true));
        pc.addView(text("অ্যামাউন্ট নিজে থেকে বসেছে — প্যাকেজের দামই অর্ডারের অ্যামাউন্ট।", 13, TEXT2, false));
        content.addView(pc);
        LinearLayout fc = card();
        // Payment number first, above the payment-method selector and the
        // senderPhone/trxId fields, exactly like the app OrderModal.
        fc.addView(paymentInfoCard());
        final android.widget.Spinner methodSpinner = new android.widget.Spinner(this);
        String[] methods = new String[]{"bkash — bKash", "nagad — Nagad"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, methods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(adapter);
        fc.addView(text("পেমেন্ট মাধ্যম", 14, TEXT, true));
        fc.addView(methodSpinner);
        final EditText senderInput = new EditText(this);
        senderInput.setHint("আপনার Sender নম্বর (যে নম্বর থেকে পাঠিয়েছেন)");
        senderInput.setTextSize(15);
        senderInput.setInputType(InputType.TYPE_CLASS_PHONE);
        senderInput.setBackground(roundedBorder(SOFT, 12));
        senderInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        fc.addView(text("Sender নম্বর *", 14, TEXT, true));
        fc.addView(senderInput);
        final EditText trxInput = new EditText(this);
        trxInput.setHint("TrxID (যেমন: 8N7A6...)");
        trxInput.setTextSize(15);
        trxInput.setInputType(InputType.TYPE_CLASS_TEXT);
        trxInput.setBackground(roundedBorder(SOFT, 12));
        trxInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        fc.addView(text("TrxID *", 14, TEXT, true));
        fc.addView(trxInput);
        final EditText pageInput = new EditText(this);
        pageInput.setHint("আপনার ফেসবুক পেজের লিংক (ঐচ্ছিক)");
        pageInput.setTextSize(15);
        pageInput.setInputType(InputType.TYPE_CLASS_TEXT);
        pageInput.setBackground(roundedBorder(SOFT, 12));
        pageInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        fc.addView(text("পেজ লিংক (ঐচ্ছিক)", 14, TEXT, true));
        fc.addView(pageInput);
        final EditText reqInput = new EditText(this);
        reqInput.setHint("আপনার চাহিদা / নির্দেশনা (ঐচ্ছিক)");
        reqInput.setTextSize(15);
        reqInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        reqInput.setMinLines(2);
        reqInput.setBackground(roundedBorder(SOFT, 12));
        reqInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        fc.addView(text("চাহিদা (ঐচ্ছিক)", 14, TEXT, true));
        fc.addView(reqInput);
        fc.addView(primaryButton("অর্ডার করুন — স্ট্যাটাস Pending হবে", new View.OnClickListener() {
            @Override public void onClick(View v) {
                String sender = senderInput.getText().toString().trim();
                String trx = trxInput.getText().toString().trim().toUpperCase();
                if (sender.length() == 0 || trx.length() == 0) {
                    Toast.makeText(MainActivity.this,
                        "Sender নম্বর আর TrxID — দুটোই লাগবে।", Toast.LENGTH_LONG).show();
                    return;
                }
                String method = methodSpinner.getSelectedItemPosition() == 1 ? "nagad" : "bkash";
                String pageLink = pageInput.getText().toString().trim();
                String reqs = reqInput.getText().toString().trim();
                String uid = FirebaseAuthHelper.currentUid();
                String name = FirebaseAuthHelper.currentDisplayName();
                if (name == null || name.length() == 0) name = "কাস্টমার";
                if (uid == null) {
                    notice = "লগইন করা নেই। আবার লগইন করুন।";
                    return;
                }
                final String fUid = uid;
                final String fName = name;
                final OrderStore.Order order = new OrderStore.Order(
                    String.valueOf(System.currentTimeMillis()), fUid, fName,
                    pkg.service, pkg.pkg, pkg.price, method, sender, trx,
                    "pending", "", System.currentTimeMillis(),
                    pageLink, reqs, pkg.billing);
                FirebaseAuthHelper.createOrder(order,
                    new FirebaseAuthHelper.ResultCallback<Boolean>() {
                        @Override public void onSuccess(Boolean ok) {
                            orders.add(0, order);
                            ordersLoaded = true;
                            deriveMySubscription();
                            currentOrder = order;
                            selectedPackage = null;
                            notice = "অর্ডার করা হয়েছে — স্ট্যাটাস Pending, অ্যাডমিন অনুমোদন দিলে চলমান হবে।";
                            go(S_ORDER_DETAIL, true);
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
            }
        }));
        fc.addView(outlineButton("অন্য প্যাকেজ বেছে নিন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { selectedPackage = null; render(); }
        }));
        content.addView(fc);
        content.addView(text("এই ফর্মে আপনার নিজের পেমেন্ট তথ্য (Sender নম্বর + TrxID) নেওয়া হয়। পেমেন্ট নম্বরটি উপরে দেখানো হয়েছে।", 12, TEXT2, false));
    }

    private void showRechargeForm() {
        content.addView(ghostButton("টোকেনে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_TOKENS, false); }
        }));
        content.addView(text("টোকেন রিচার্জ — এপিআই ক্রেডিট", 26, NAVY, true));
        final String amount = selectedTokenAmount == null ? "৳২০০" : selectedTokenAmount;
        LinearLayout pc = card();
        pc.addView(text("রিচার্জ অ্যামাউন্ট: " + amount, 18, PRIMARY, true));
        pc.addView(text("এটি সাবস্ক্রিপশন নয় — দিনের হিসাব এখানে নেই। অ্যাডমিন অনুমোদনের পর ক্রেডিট যোগ হবে। মেয়াদ শেষ হয় না, Pay As You Go।", 13, TEXT2, false));
        content.addView(pc);
        LinearLayout fc = card();
        // Same payment number as the order form: the customer must send
        // money here first, before filling senderPhone/trxId below.
        fc.addView(paymentInfoCard());
        final android.widget.Spinner methodSpinner = new android.widget.Spinner(this);
        String[] methods = new String[]{"bkash — bKash", "nagad — Nagad"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
            this, android.R.layout.simple_spinner_item, methods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(adapter);
        fc.addView(text("পেমেন্ট মাধ্যম", 14, TEXT, true));
        fc.addView(methodSpinner);
        final EditText senderInput = new EditText(this);
        senderInput.setHint("আপনার Sender নম্বর");
        senderInput.setTextSize(15);
        senderInput.setInputType(InputType.TYPE_CLASS_PHONE);
        senderInput.setBackground(roundedBorder(SOFT, 12));
        senderInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        fc.addView(text("Sender নম্বর *", 14, TEXT, true));
        fc.addView(senderInput);
        final EditText trxInput = new EditText(this);
        trxInput.setHint("TrxID");
        trxInput.setTextSize(15);
        trxInput.setInputType(InputType.TYPE_CLASS_TEXT);
        trxInput.setBackground(roundedBorder(SOFT, 12));
        trxInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        fc.addView(text("TrxID *", 14, TEXT, true));
        fc.addView(trxInput);
        fc.addView(primaryButton("রিচার্জ রিকোয়েস্ট পাঠান (Pending থাকবে)", new View.OnClickListener() {
            @Override public void onClick(View v) {
                String sender = senderInput.getText().toString().trim();
                String trx = trxInput.getText().toString().trim().toUpperCase();
                if (sender.length() == 0 || trx.length() == 0) {
                    Toast.makeText(MainActivity.this,
                        "Sender নম্বর আর TrxID — দুটোই লাগবে।", Toast.LENGTH_LONG).show();
                    return;
                }
                String method = methodSpinner.getSelectedItemPosition() == 1 ? "nagad" : "bkash";
                String digits = amount.replaceAll("[^0-9]", "");
                String uid = FirebaseAuthHelper.currentUid();
                String name = FirebaseAuthHelper.currentDisplayName();
                if (name == null || name.length() == 0) name = "কাস্টমার";
                if (uid == null) {
                    notice = "লগইন করা নেই। আবার লগইন করুন।";
                    return;
                }
                FirebaseAuthHelper.createRecharge(uid, name, digits, method, sender, trx,
                    new FirebaseAuthHelper.ResultCallback<Boolean>() {
                        @Override public void onSuccess(Boolean ok) {
                            notice = "রিচার্জ রিকোয়েস্ট পাঠানো হয়েছে — স্ট্যাটাস Pending, অ্যাডমিন অনুমোদনের পর ক্রেডিট যোগ হবে।";
                            go(S_TOKENS, true);
                        }
                        @Override public void onError(String message) {
                            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                        }
                    });
            }
        }));
        content.addView(fc);
    }

    private void toggleLeadPause(final LeadsStore.Lead lead) {
        final boolean toPaused = !lead.paused;
        if (FirebaseAuthHelper.currentUid() == null) {
            Toast.makeText(this, "লগইন করা নেই। আবার লগইন করুন।", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseAuthHelper.updateLeadStatus(lead, toPaused,
            toPaused ? "handover" : "bot_replied",
            new FirebaseAuthHelper.ResultCallback<Boolean>() {
                @Override public void onSuccess(Boolean ok) {
                    if (toPaused) leadsStore.takeOver(lead);
                    else leadsStore.resumeAi(lead);
                    renderThreadScreen();
                }
                @Override public void onError(String message) {
                    Toast.makeText(MainActivity.this,
                        "স্ট্যাটাস আপডেট হয়নি: " + message, Toast.LENGTH_LONG).show();
                }
            });
    }

    // ---- Screens: Connect / Services / Subscriptions / Accesss / About ----
    private void showConnect() {
        content.addView(text("ফেসবুক কানেক্ট", 26, NAVY, true));
        TextView sub = text("আপনার পেজ কানেক্ট করলে পেজের মেসেজ আর লিড এই অ্যাপেই আসবে।", 14, TEXT2, false);
        sub.setPadding(0, dp(4), 0, dp(12));
        content.addView(sub);

        LinearLayout status = card();
        status.addView(text("পেজ: AutoMateIQ", 16, TEXT, true));
        LinearLayout srow = new LinearLayout(this);
        srow.setOrientation(LinearLayout.HORIZONTAL);
        srow.setPadding(0, dp(8), 0, 0);
        srow.addView(chip(" কানেক্টেড", GREEN_SOFT, GREEN));
        status.addView(srow);
        TextView note = text("কানেক্ট হলে পেজের মেসেজ এখানে দেখা যাবে।", 13, TEXT2, false);
        note.setPadding(0, dp(8), 0, 0);
        status.addView(note);
        content.addView(status);

        String[][] steps = {
            {"১", "ফেসবুকে লগইন করুন", "আপনার ফেসবুক অ্যাকাউন্ট দিয়ে লগইন করুন।"},
            {"২", "পেজ বেছে নিন", "যে পেজের বট চালাতে চান, সেই পেজটি বেছে নিন।"},
            {"৩", "মেসেঞ্জার পারমিশন দিন", "পেজ মেসেজ পড়া ও উত্তর দেওয়ার অনুমতি দিন।"},
            {"৪", "লিড অ্যাপে দেখুন", "কানেক্ট হলে পেজের মেসেজ এই অ্যাপের লিডসে আসবে।"},
        };
        for (String[] st : steps) {
            LinearLayout c = card();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView num = new TextView(this);
            num.setText(st[0]);
            num.setTextSize(17);
            num.setTypeface(Typeface.DEFAULT_BOLD);
            num.setTextColor(Color.WHITE);
            num.setGravity(Gravity.CENTER);
            num.setBackground(rounded(PRIMARY, 20));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(40), dp(40));
            row.addView(num, nlp);
            LinearLayout tx = new LinearLayout(this);
            tx.setOrientation(LinearLayout.VERTICAL);
            tx.setPadding(dp(12), 0, 0, 0);
            tx.addView(text(st[1], 16, TEXT, true));
            tx.addView(text(st[2], 13, TEXT2, false));
            row.addView(tx);
            c.addView(row);
            content.addView(c);
        }
        
        
    }

    private void showServices() {
        content.addView(text("সার্ভিস", 26, NAVY, true));
        TextView sub = text("যেকোনো সার্ভিসে ট্যাপ করলে পুরো দামের তালিকা দেখা যাবে।", 14, TEXT2, false);
        sub.setPadding(0, dp(4), 0, dp(12));
        content.addView(sub);
        for (int i = 0; i < SERVICES.length; i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int k = 0; k < 2; k++) {
                if (i + k >= SERVICES.length) break;
                final Service s = SERVICES[i + k];
                LinearLayout c = new LinearLayout(this);
                c.setOrientation(LinearLayout.VERTICAL);
                c.setBackground(serviceTileGradient(s.color));
                c.setElevation(dp(4));
                c.setPadding(dp(14), dp(14), dp(14), dp(14));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lp.setMargins(k == 0 ? 0 : dp(7), 0, k == 0 ? dp(7) : 0, dp(14));
                c.setLayoutParams(lp);
                ImageView icon = iconView(iconRes(s.icon), Color.WHITE);
                c.addView(icon);
                c.addView(text(s.name, 15, Color.WHITE, true));
                TextView d = text(s.from, 12, 0xE0FFFFFF, false);
                d.setMaxLines(3);
                d.setPadding(0, dp(4), 0, dp(8));
                c.addView(d);
                c.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        currentService = s;
                        go(S_SERVICE_DETAIL, true);
                    }
                });
                row.addView(c);
            }
            content.addView(row);
        }
        content.addView(ghostButton("অ্যাপ সম্পর্কে", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ABOUT, true); }
        }));
    }

    private void showServiceDetail() {
        final Service s = currentService;
        if (s == null) { go(S_SERVICES, false); return; }
        content.addView(ghostButton("সার্ভিসে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_SERVICES, false); }
        }));
        LinearLayout head = card();
        head.addView(iconView(iconRes(s.icon), NAVY));
        head.addView(text(s.name, 22, NAVY, true));
        TextView d = text(s.desc, 15, TEXT2, false);
        d.setPadding(0, dp(8), 0, 0);
        head.addView(d);
        head.addView(chip(s.from, SOFT_BLUE, PRIMARY));
        content.addView(head);

        LinearLayout prices = card();
        prices.addView(text("দামের তালিকা", 16, TEXT, true));
        for (String p : s.prices) {
            TextView row = text("•  " + p, 15, TEXT, false);
            row.setPadding(0, dp(6), 0, 0);
            prices.addView(row);
        }
        content.addView(prices);

        LinearLayout ben = card();
        ben.addView(text("যা যা পাবেন", 16, TEXT, true));
        for (String b : s.benefits) {
            TextView row = text("  " + b, 15, TEXT, false);
            row.setPadding(0, dp(6), 0, 0);
            ben.addView(row);
        }
        content.addView(ben);

        content.addView(primaryButton("অ্যাপেই অর্ডার করুন", new View.OnClickListener() {
            @Override public void onClick(View v) {
                // v6: ordering happens IN-APP, never via the app.
                // Default to this service's first (monthly) package;
                // the form's "other package" button can change it.
                selectedPackage = null;
                for (OrderStore.OrderPackage pkg : OrderStore.PACKAGES) {
                    if (pkg.service.equals(s.name)) { selectedPackage = pkg; break; }
                }
                go(S_ORDER_FORM, true);
            }
        }));
        content.addView(outlineButton("মেসেঞ্জারে জিজ্ঞাসা করুন", PRIMARY,
            new View.OnClickListener() {
                @Override public void onClick(View v) { openUrl(LiveDataClient.PAGE_URL); }
            }));
    }

    private void showSubscriptions() {
        content.addView(text("সাবস্ক্রিপশন কিনুন", 26, NAVY, true));
        TextView sub = text("শুধু মাসিক প্যাকেজ। এককালীন প্যাকেজ মাসিক নয় — তাই এখানে দিনের হিসাব দেখানো হয় না।", 14, TEXT2, false);
        sub.setPadding(0, dp(4), 0, dp(12));
        content.addView(sub);
        for (final SubscriptionStore.MonthlyPackage p : SubscriptionStore.MONTHLY_PACKAGES) {
            LinearLayout c = card();
            c.addView(text(p.service, 16, TEXT, true));
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(6), 0, 0);
            row.addView(chip(p.pkg, SOFT_BLUE, PRIMARY));
            TextView dur = text("  •  ১ মাস", 13, TEXT2, false);
            row.addView(dur);
            c.addView(row);
            c.addView(text(p.price, 22, PRIMARY, true));
            c.addView(primaryButton("কিনুন / অ্যাপেই অর্ডার করুন", new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selectedPackage = findMonthlyPackage(p.service, p.pkg);
                    go(S_ORDER_FORM, true);
                }
            }));
            content.addView(c);
        }
        LinearLayout note = card();
        note.setBackground(rounded(AMBER_SOFT, 20));
        note.addView(text("এককালীন প্যাকেজের আলাদা ফাংশন পরে আলোচনা করে ঠিক করা হবে — এখন এখানে বানানো হয়নি।", 13, TEXT, false));
        content.addView(note);
        content.addView(outlineButton(" টোকেন কিনুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_TOKENS, true); }
        }));
        content.addView(outlineButton(" আমার সাবস্ক্রিপশন দেখুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_MY_SUB, true); }
        }));
    }

    private void showTokens() {
        content.addView(text("টোকেন কিনুন — এপিআই ক্রেডিট", 26, NAVY, true));
        TextView sub = text("৳২০০ থেকে শুরু। যত টাকার এপিআই চান নিতে পারবেন।", 14, TEXT2, false);
        sub.setPadding(0, dp(4), 0, dp(12));
        content.addView(sub);
        for (final String amount : SubscriptionStore.TOKEN_AMOUNTS) {
            LinearLayout c = card();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView price = text(amount, 24, PRIMARY, true);
            row.addView(price);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            TextView spacer = new TextView(this);
            row.addView(spacer, rp);
            row.addView(chip("রিচার্জ", GREEN_SOFT, GREEN));
            c.addView(row);
            c.addView(primaryButton("এই অ্যামাউন্ট রিচার্জ করুন (অ্যাপেই)", new View.OnClickListener() {
                @Override public void onClick(View v) {
                    selectedTokenAmount = amount;
                    go(S_RECHARGE_FORM, true);
                }
            }));
            content.addView(c);
        }
        LinearLayout rules = card();
        rules.addView(text("নিয়ম", 16, TEXT, true));
        TextView r = text("bKash / Nagad পার্সোনাল • মেয়াদ শেষ হয় না • Pay As You Go • অ্যাডমিন অনুমোদনের পর ক্রেডিট যোগ হয় • ৳২০০ থেকে যেকোনো অ্যামাউন্ট নেওয়া যায়", 14, TEXT2, false);
        r.setPadding(0, dp(6), 0, 0);
        rules.addView(r);
        content.addView(rules);
    }

    private void showMySubscription() {
        content.addView(text("আমার সাবস্ক্রিপশন", 26, NAVY, true));
        // Subscription is DERIVED FROM REAL ORDERS. Latest monthly, non-cancelled order wins.
        if (ordersLoaded) {
            SubscriptionStore.Subscription derived =
                OrderStore.subscriptionFromOrders(orders);
            mySubscription = derived;
            mySubscriptionOrderStatus = "";
            if (derived != null) {
                for (OrderStore.Order o : orders) {
                    if (o.isMonthly() && !"cancelled".equals(o.status)
                        && !"rejected".equals(o.status)
                        && o.dateMillis == derived.startMillis) {
                        mySubscriptionOrderStatus = o.status;
                        break;
                    }
                }
            }
        }
        if (signedIn && !ordersLoaded) {
            content.addView(bannerCard("অর্ডার লোড হচ্ছে… রিফ্রেশ চাপলে আবার আনা হবে।", SOFT_BLUE, PRIMARY));
            content.addView(outlineButton(" অর্ডার রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveOrders(); render(); }
            }));
        }
        if (mySubscription == null) {
            LinearLayout c = card();
            c.addView(text("কোনো মাসিক সাবস্ক্রিপশন নেই", 16, TEXT, true));
            TextView t = text("মাসিক প্যাকেজ কিনলে এখানে সার্ভিস, প্যাকেজ, শুরু, শেষ আর কত দিন বাকি আছে — সব দেখা যাবে।", 14, TEXT2, false);
            t.setPadding(0, dp(6), 0, 0);
            c.addView(t);
            c.addView(primaryButton("মাসিক প্যাকেজ দেখুন", new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_SUBS, true); }
            }));
            content.addView(c);
        } else {
            SubscriptionStore.Subscription s = mySubscription;
            LinearLayout c = card();
            c.addView(text(s.service, 20, NAVY, true));
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp(8), 0, 0);
            row.addView(chip(s.pkg, SOFT_BLUE, PRIMARY));
            TextView price = text("  " + s.price, 15, TEXT, false);
            row.addView(price);
            c.addView(row);
            TextView days = text(SubscriptionStore.daysRemainingBn(s), 28, PRIMARY, true);
            days.setPadding(0, dp(12), 0, 0);
            c.addView(days);
            c.addView(progressBar(s.progressPercent()));
            c.addView(text("শুরু: " + SubscriptionStore.formatDate(s.startMillis), 14, TEXT2, false));
            c.addView(text("শেষ: " + SubscriptionStore.formatDate(s.expiryMillis), 14, TEXT2, false));
            LinearLayout srow = new LinearLayout(this);
            srow.setPadding(0, dp(10), 0, 0);
            srow.addView(s.isActive() ? chip("চালু আছে", GREEN_SOFT, GREEN)
                : chip("মেয়াদ শেষ", RED_SOFT, RED));
            c.addView(srow);
            content.addView(c);
            if ("pending".equals(mySubscriptionOrderStatus)) {
                LinearLayout warn = card();
                warn.setBackground(rounded(AMBER_SOFT, 20));
                warn.addView(text("অর্ডারটি Pending — অ্যাডমিন অনুমোদন দিলে সার্ভিস সক্রিয় হবে। দিনের হিসাব অর্ডারের তারিখ থেকেই ধরা হয়েছে।", 13, TEXT, false));
                content.addView(warn);
            }
            content.addView(primaryButton("রিনিউ / আবার অর্ডার করুন (অ্যাপেই)", new View.OnClickListener() {
                @Override public void onClick(View v) { selectedPackage = null; go(S_ORDER_FORM, true); }
            }));
        }
    }


    // ================= v7 ADMIN PANEL =================

    private boolean monthlyExpired(OrderStore.Order o) {
        if (o == null || !o.isMonthly()) return false;
        SubscriptionStore.Subscription sub = SubscriptionStore.fromOrder(
            o.serviceName, o.packageDetails, o.amount, o.dateMillis, 1);
        return sub.expiryMillis < System.currentTimeMillis();
    }

    private void loadLiveUsers() {
        usersLoading = true;
        usersError = null;
        FirebaseAuthHelper.fetchUsers(
            new FirebaseAuthHelper.ResultCallback<java.util.List<UsersStore.User>>() {
                @Override public void onSuccess(java.util.List<UsersStore.User> live) {
                    users = live;
                    usersLoaded = true;
                    usersLoading = false;
                    usersError = null;
                    if (screen == S_DASH || screen == S_CUSTOMERS
                        || screen == S_CUSTOMER_DETAIL || screen == S_PAGES) render();
                }
                @Override public void onError(String message) {
                    usersLoading = false;
                    usersError = message;
                    if (screen == S_DASH || screen == S_CUSTOMERS || screen == S_PAGES) render();
                }
            });
    }

    // v7 AUTO-EXPIRE: on admin load, monthly orders past expiry with a
    // still-open status are auto-cancelled in database. One-time
    // orders are never touched. Cancelled orders stay visible —
    // nothing is ever deleted here.
    private void runAutoExpire() {
        if (autoExpireRunForLoad) return;
        autoExpireRunForLoad = true;
        int candidates = 0;
        for (final OrderStore.Order o : orders) {
            if (!o.isMonthly()) continue;
            if (!("pending".equals(o.status) || "in_progress".equals(o.status)
                || "approved".equals(o.status))) continue;
            if (!monthlyExpired(o)) continue;
            candidates++;
            java.util.Map<String, Object> updates = new java.util.HashMap<String, Object>();
            updates.put("status", "cancelled");
            updates.put("adminMessage", "Offer ended — auto-stopped on expiry");
            FirebaseAuthHelper.updateOrderFields(o.id, updates,
                new FirebaseAuthHelper.ResultCallback<Boolean>() {
                    @Override public void onSuccess(Boolean ok) {
                        o.status = "cancelled";
                        o.adminMessage = "Offer ended — auto-stopped on expiry";
                        if (screen == S_DASH || screen == S_ORDERS) render();
                    }
                    @Override public void onError(String message) {
                        // Local display still shows "মেয়াদ শেষ" via
                        // monthlyExpired(); the write can be retried
                        // from the order detail screen.
                    }
                });
        }
        autoExpireCount = candidates;
        if (candidates > 0) {
            notice = bn(String.valueOf(candidates))
                + "টি মাসিক অর্ডারের অফার শেষ হয়েছে — স্বয়ংক্রিয়ভাবে বাতিল করা হচ্ছে। বাতিল অর্ডারগুলো তালিকায় দেখা যাবে, মুছে ফেলা হয়নি।";
        }
    }

    private void showAdminDashboard() {
        // v3 admin dashboard: greeting (not a separate "Admin panel" hero),
        // then ONE heroCard holding the connected Page + 3 inline stats.
        content.addView(text("শুভ সকাল", 26, TEXT, true));
        TextView gsub = text("আজ আপনার ব্যবসার সারাংশ এক নজরে দেখুন।", 14, TEXT2, false);
        gsub.setPadding(0, 0, 0, dp(12));
        content.addView(gsub);
        int activeSubs0 = 0;
        for (OrderStore.Order o : orders) {
            if (!o.isMonthly()) continue;
            if (!monthlyExpired(o) && !"cancelled".equals(o.status) && !"rejected".equals(o.status)) activeSubs0++;
        }
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(heroShiftDrawable());
        hero.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.setMargins(0, 0, 0, dp(12));
        hero.setLayoutParams(hlp);
        LinearLayout htop = new LinearLayout(this);
        htop.setOrientation(LinearLayout.HORIZONTAL);
        htop.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout htxt = new LinearLayout(this);
        htxt.setOrientation(LinearLayout.VERTICAL);
        htxt.addView(text("কানেক্ট করা পেজ", 11, 0xFFE0E7FF, true));
        htxt.addView(text("AutoMateIQ", 19, Color.WHITE, true));
        htxt.addView(text("facebook.com/automateiq.xyz", 11, 0xFFE0E7FF, false));
        htop.addView(htxt, new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        htop.addView(chip("Connected", GREEN_SOFT, GREEN));
        hero.addView(htop);
        LinearLayout srow = new LinearLayout(this);
        srow.setOrientation(LinearLayout.HORIZONTAL);
        srow.setPadding(0, dp(14), 0, 0);
        String[][] hs = {
            {usersLoaded ? bn(String.valueOf(users.size())) : "—", "মোট ইউজার"},
            {bn(String.valueOf(orders.size())), "মোট অর্ডার"},
            {bn(String.valueOf(activeSubs0)), "অ্যাক্টিভ"},
        };
        for (String[] st : hs) {
            LinearLayout sc = new LinearLayout(this);
            sc.setOrientation(LinearLayout.VERTICAL);
            sc.addView(text(st[0], 22, Color.WHITE, true));
            sc.addView(text(st[1], 11, 0xFFE0E7FF, false));
            srow.addView(sc, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        hero.addView(srow);
        content.addView(hero);
        // Admin stats: users, orders, active subs, expired.
        int activeSubs = 0, expiredN = 0;
        for (OrderStore.Order o : orders) {
            if (!o.isMonthly()) continue;
            if (monthlyExpired(o)) expiredN++;
            else if (!"cancelled".equals(o.status) && !"rejected".equals(o.status)) activeSubs++;
        }
        if (autoExpireCount > 0) {
            content.addView(bannerCard(" " + bn(String.valueOf(autoExpireCount)) + "টি মাসিক অর্ডারের অফার শেষ হয়েছে — স্বয়ংক্রিয়ভাবে বাতিল করা হয়েছে।", AMBER_SOFT, AMBER));
        }
        LinearLayout botNote = card();
        botNote.setBackground(rounded(AMBER_SOFT, 20));
        botNote.addView(text("মেয়াদ শেষ হলে অর্ডার বাতিল দেখাবে। AutoMateIQ-এর নিজের পেজের ক্ষেত্রে এটি প্রযোজ্য নয়।", 13, TEXT, false));
        content.addView(botNote);

        // v3 quick actions: horizontal scroll row, icon tile + short label.
        LinearLayout qcTitle = new LinearLayout(this);
        qcTitle.setOrientation(LinearLayout.VERTICAL);
        qcTitle.addView(text("দ্রুত কাজ", 18, NAVY, true));
        content.addView(qcTitle);
        HorizontalScrollView qscroll = new HorizontalScrollView(this);
        qscroll.setHorizontalScrollBarEnabled(false);
        LinearLayout qrow = new LinearLayout(this);
        qrow.setOrientation(LinearLayout.HORIZONTAL);
        qrow.addView(quickAction("ic_list", "অর্ডার", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ORDERS, true); }
        }));
        qrow.addView(quickAction("ic_users", "কাস্টমার", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_CUSTOMERS, true); }
        }));
        qrow.addView(quickAction("ic_page", "পেজ", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_PAGES, true); }
        }));
        qrow.addView(quickAction("ic_chat", "লিডস", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_LEADS, true); }
        }));
        qscroll.addView(qrow);
        content.addView(qscroll);

        // Recent orders.
        LinearLayout recent = card();
        recent.addView(text("সাম্প্রতিক কার্যক্রম", 18, TEXT, true));
        if (ordersLoading) {
            recent.addView(text("লোড হচ্ছে…", 14, TEXT2, false));
        } else if (ordersError != null) {
            recent.addView(text(ordersError, 14, RED, true));
        } else if (orders.isEmpty()) {
            recent.addView(text("এখনো কোনো অর্ডার নেই।", 14, TEXT2, false));
        } else {
            for (int i = 0; i < Math.min(3, orders.size()); i++) {
                final OrderStore.Order ro = orders.get(i);
                LinearLayout rrow = new LinearLayout(this);
                rrow.setOrientation(LinearLayout.HORIZONTAL);
                rrow.setGravity(Gravity.CENTER_VERTICAL);
                rrow.setPadding(0, dp(8), 0, dp(8));
                LinearLayout mid = new LinearLayout(this);
                mid.setOrientation(LinearLayout.VERTICAL);
                mid.addView(text(ro.serviceName, 16, TEXT, true));
                mid.addView(text(ro.amount + " • কাস্টমার: " + ro.userName, 14, TEXT2, false));
                mid.addView(text("অর্ডার", 11, TEXT2, false));
                LinearLayout.LayoutParams rmp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                rrow.addView(statusDot(statusColor(ro.status)));
                rrow.addView(mid, rmp);
                rrow.addView(orderStatusChip(ro));
                rrow.setBackground(rippleBackground());
                rrow.setClickable(true);
                rrow.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { currentOrder = ro; go(S_ORDER_DETAIL, true); }
                });
                recent.addView(rrow);
            }
            recent.addView(ghostButton("সব অর্ডার দেখুন", new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_ORDERS, true); }
            }));
        }
        content.addView(recent);
        content.addView(outlineButton(" কাস্টমার + অর্ডার রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { loadLiveUsers(); loadLiveOrders(); }
        }));
    }

    private void showCustomers() {
        content.addView(text("কাস্টমার", 26, NAVY, true));
        content.addView(text("শুধু পরিচয়, অর্ডার, পেজ আর সাবস্ক্রিপশন দেখা যায়।", 13, TEXT2, false));
        if (usersLoading) { content.addView(text("লোড হচ্ছে…", 14, TEXT2, false)); return; }
        if (usersError != null) {
            content.addView(text(usersError, 14, RED, true));
            content.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveUsers(); render(); }
            }));
            return;
        }
        if (!usersLoaded) {
            content.addView(primaryButton("কাস্টমার লোড করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveUsers(); render(); }
            }));
            return;
        }
        java.util.List<UsersStore.User> customers = new java.util.ArrayList<UsersStore.User>();
        for (UsersStore.User u : users) {
            if (!u.isAdminUser()) customers.add(u);
        }
        content.addView(text("মোট " + bn(String.valueOf(customers.size())) + " জন কাস্টমার", 14, TEXT2, false));
        if (customers.isEmpty()) {
            LinearLayout c = card();
            c.addView(text("এখনো কোনো কাস্টমার পাওয়া যায়নি", 16, TEXT, true));
            c.addView(text("কেউ সাইন আপ করলে এখানে দেখা যাবে।", 14, TEXT2, false));
            content.addView(c);
            return;
        }
        for (final UsersStore.User u : customers) {
            LinearLayout row = card();
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            top.addView(avatar(u.initial(), PRIMARY));
            LinearLayout mid = new LinearLayout(this);
            mid.setOrientation(LinearLayout.VERTICAL);
            mid.setPadding(dp(12), 0, dp(8), 0);
            mid.addView(text(u.name, 16, TEXT, true));
            mid.addView(text("ID: " + u.id, 12, TEXT2, false));
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            top.addView(mid, mp);
            top.addView(u.hasPage() ? chip(u.pageStatusBn(), GREEN_SOFT, GREEN) : chip("পেজ রেকর্ড নেই", SOFT_BLUE, PRIMARY));
            row.addView(top);
            // Subscription days for this customer, from their orders.
            java.util.List<OrderStore.Order> uo = new java.util.ArrayList<OrderStore.Order>();
            for (OrderStore.Order o : orders) {
                if (u.id.equals(o.userId)) uo.add(o);
            }
            SubscriptionStore.Subscription sub = OrderStore.subscriptionFromOrders(uo);
            if (sub != null) {
                row.addView(text("সাবস্ক্রিপশন: " + SubscriptionStore.daysRemainingBn(sub)
                    + "  •  শেষ: " + SubscriptionStore.formatDate(sub.expiryMillis), 13, PRIMARY, true));
            } else {
                row.addView(text("অর্ডার: " + bn(String.valueOf(uo.size())) + "টি  •  কোনো মাসিক সাবস্ক্রিপশন নেই", 13, TEXT2, false));
            }
            row.setBackground(rippleBackground());
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { currentCustomer = u; go(S_CUSTOMER_DETAIL, true); }
            });
            content.addView(row);
        }
        content.addView(outlineButton(" কাস্টমার রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { loadLiveUsers(); render(); }
        }));
    }

    private void showCustomerDetail() {
        final UsersStore.User u = currentCustomer;
        if (u == null) { go(S_CUSTOMERS, false); return; }
        content.addView(ghostButton("কাস্টমারে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_CUSTOMERS, false); }
        }));
        if (u.localOnly) {
            content.addView(bannerCard(" এই পেজ রেকর্ডটি সংরক্ষণ করা যায়নি। সঠিক userId দিয়ে আবার সংরক্ষণ করুন।", AMBER_SOFT, AMBER));
        }
        LinearLayout head = card();
        LinearLayout hrow = new LinearLayout(this);
        hrow.setOrientation(LinearLayout.HORIZONTAL);
        hrow.setGravity(Gravity.CENTER_VERTICAL);
        hrow.addView(avatar(u.initial(), PRIMARY));
        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        mid.setPadding(dp(12), 0, dp(8), 0);
        mid.addView(text(u.name, 20, NAVY, true));
        mid.addView(text("ID: " + u.id, 13, TEXT2, false));
        if (u.location.length() > 0) mid.addView(text("লোকেশন: " + u.location, 13, TEXT2, false));
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        hrow.addView(mid, mp);
        hrow.addView(chip("কাস্টমার", SOFT_BLUE, PRIMARY));
        head.addView(hrow);
        content.addView(head);

        LinearLayout pg = card();
        pg.addView(text("কাস্টমারের পেজ", 16, TEXT, true));
        if (u.hasPage()) {
            if (u.pageName.length() > 0) pg.addView(text(u.pageName, 16, TEXT, true));
            if (u.pageId.length() > 0) pg.addView(text("Page ID: " + u.pageId, 13, TEXT2, false));
            if (u.pageLink.length() > 0) pg.addView(text("লিংক: " + u.pageLink, 13, TEXT2, false));
            pg.addView(chip(u.pageStatusBn(), GREEN_SOFT, GREEN));
            if (u.pageNotes.length() > 0) pg.addView(text("নোট: " + u.pageNotes, 13, TEXT, false));
            if (u.pageLink.length() > 0) {
                final String link = u.pageLink;
                pg.addView(outlineButton("পেজ লিংক খুলুন (ব্রাউজারে)", PRIMARY, new View.OnClickListener() {
                    @Override public void onClick(View v) { openUrl(link); }
                }));
            }
        } else {
            pg.addView(text("এখনো কোনো পেজ রেকর্ড নেই।", 14, TEXT2, false));
        }
        pg.addView(primaryButton("পেজ রেকর্ড যোগ / সম্পাদনা করুন", new View.OnClickListener() {
            @Override public void onClick(View v) { pendingPageUser = u; go(S_PAGE_FORM, true); }
        }));
        content.addView(pg);

        java.util.List<OrderStore.Order> uo = new java.util.ArrayList<OrderStore.Order>();
        for (OrderStore.Order o : orders) {
            if (u.id.equals(o.userId)) uo.add(o);
        }
        SubscriptionStore.Subscription sub = OrderStore.subscriptionFromOrders(uo);
        LinearLayout sc = card();
        sc.addView(text("সাবস্ক্রিপশন", 16, TEXT, true));
        if (sub != null) {
            sc.addView(text(sub.service + " • " + sub.pkg + " • " + sub.price, 15, TEXT, false));
            sc.addView(text(SubscriptionStore.daysRemainingBn(sub), 24, PRIMARY, true));
            sc.addView(progressBar(sub.progressPercent()));
            sc.addView(text("শুরু: " + SubscriptionStore.formatDate(sub.startMillis)
                + "  •  শেষ: " + SubscriptionStore.formatDate(sub.expiryMillis), 13, TEXT2, false));
            sc.addView(sub.isActive() ? chip("চালু আছে", GREEN_SOFT, GREEN) : chip("মেয়াদ শেষ", RED_SOFT, RED));
        } else {
            sc.addView(text("কোনো মাসিক সাবস্ক্রিপশন নেই। এককালীন অর্ডারের দিনের হিসাব দেখানো হয় না।", 14, TEXT2, false));
        }
        content.addView(sc);

        LinearLayout oc = card();
        oc.addView(text("এই কাস্টমারের অর্ডার (" + bn(String.valueOf(uo.size())) + "টি)", 16, TEXT, true));
        if (uo.isEmpty()) {
            oc.addView(text("এখনো কোনো অর্ডার নেই।", 14, TEXT2, false));
        } else {
            for (final OrderStore.Order o : uo) {
                LinearLayout rrow = new LinearLayout(this);
                rrow.setOrientation(LinearLayout.HORIZONTAL);
                rrow.setGravity(Gravity.CENTER_VERTICAL);
                rrow.setPadding(0, dp(8), 0, dp(8));
                TextView nm = text(o.serviceName + " • " + o.amount, 15, TEXT, true);
                LinearLayout.LayoutParams rmp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                rrow.addView(nm, rmp);
                rrow.addView(orderStatusChip(o));
                rrow.setBackground(rippleBackground());
                rrow.setClickable(true);
                rrow.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { currentOrder = o; go(S_ORDER_DETAIL, true); }
                });
                oc.addView(rrow);
            }
        }
        content.addView(oc);
    }

    private void showPages() {
        content.addView(text("পেজ — কানেক্ট ও অনবোর্ডিং", 26, NAVY, true));
        LinearLayout pc = card();
        pc.addView(text(" কানেক্টেড পেজ — AutoMateIQ", 16, TEXT, true));
        pc.addView(chip(" কানেক্টেড", GREEN_SOFT, GREEN));
        pc.addView(text("পেজ: facebook.com/automateiq.xyz  •  Page ID: 825495483971307", 13, TEXT2, false));
        pc.addView(text("কানেক্টেড পেজের মেসেজ লিডসে দেখা যাবে।", 13, TEXT2, false));
        
        content.addView(pc);

        LinearLayout steps = card();
        steps.addView(text("কাস্টমারের পেজ যেভাবে কানেক্ট হবে", 16, TEXT, true));
        // v8.2: plain-language steps. No IDs are asked for here: only
        // the admin profile link, Page Admin access, and waiting.
        steps.addView(outlineButton("অ্যাডমিনের প্রোফাইল লিংক", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (ADMIN_PROFILE_LINK == null || ADMIN_PROFILE_LINK.length() == 0) {
                    // Link not supplied yet: say so plainly, open nothing.
                    Toast.makeText(MainActivity.this, "লিংকটি অ্যাডমিন জানিয়ে দেবে।", Toast.LENGTH_SHORT).show();
                } else {
                    openUrl(ADMIN_PROFILE_LINK);
                }
            }
        }));
        String[][] st = {
            {"১", "অ্যাডমিনের প্রোফাইল লিংকে চাপুন, এই ব্যক্তিকে আপনার পেজের অ্যাডমিন করুন", "উপরের বোতামে চাপুন — এই লিংকের প্রোফাইলের ব্যক্তিকেই আপনার পেজের অ্যাডমিন করতে হবে। তারপর ফেসবুকে আপনার পেজ খুলে পেজ সেটিংস → পেজ অ্যাক্সেস / পেজ রোলস থেকে সেই ব্যক্তিকে Admin হিসেবে যোগ করুন।"},
            {"২", "অ্যাক্সেস দেওয়ার পর ৫–১০ মিনিট অপেক্ষা করুন", "আপনি অ্যাক্সেস দিলে আপনার পেজ কানেক্ট হতে ৫–১০ মিনিট লাগতে পারে।"},
            {"৩", "কানেক্ট হলে স্বয়ংক্রিয়ভাবে চালু হবে", "একবার কানেক্ট হলে আর কিছু করতে হবে না — বট স্বয়ংক্রিয়ভাবে কাজ শুরু করবে।"},
        };
        for (String[] row : st) {
            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);
            r.setGravity(Gravity.CENTER_VERTICAL);
            r.setPadding(0, dp(6), 0, dp(6));
            TextView num = new TextView(this);
            num.setText(row[0]);
            num.setTextSize(16);
            num.setTypeface(Typeface.DEFAULT_BOLD);
            num.setTextColor(Color.WHITE);
            num.setGravity(Gravity.CENTER);
            num.setBackground(rounded(PRIMARY, 20));
            LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(dp(36), dp(36));
            r.addView(num, nlp);
            LinearLayout tx = new LinearLayout(this);
            tx.setOrientation(LinearLayout.VERTICAL);
            tx.setPadding(dp(12), 0, 0, 0);
            tx.addView(text(row[1], 15, TEXT, true));
            tx.addView(text(row[2], 13, TEXT2, false));
            r.addView(tx);
            steps.addView(r);
        }
        content.addView(steps);
        LinearLayout lim = card();
        lim.setBackground(rounded(AMBER_SOFT, 20));
        lim.addView(text("অন্য ব্যবসার পেজ স্বয়ংক্রিয়ভাবে কানেক্ট করা এই অ্যাপে নেই। এখানে পেজ রেকর্ড রাখা হয়, আর কানেকশন উপরের ধাপগুলো দিয়ে করা হয়।", 13, TEXT, false));
        content.addView(lim);

        content.addView(text("রেকর্ড করা কাস্টমার পেজ", 18, NAVY, true));
        java.util.List<UsersStore.User> pages = new java.util.ArrayList<UsersStore.User>();
        for (UsersStore.User u : users) {
            if (!u.isAdminUser() && u.hasPage()) pages.add(u);
        }
        for (UsersStore.User lu : localPageRecords) pages.add(lu);
        if (pages.isEmpty()) {
            LinearLayout c = card();
            c.addView(text("এখনো কোনো কাস্টমার পেজ রেকর্ড নেই", 16, TEXT, true));
            c.addView(text("নিচের ফর্ম দিয়ে প্রথম পেজটি যোগ করুন।", 14, TEXT2, false));
            content.addView(c);
        } else {
            for (final UsersStore.User u : pages) {
                LinearLayout row = card();
                LinearLayout top = new LinearLayout(this);
                top.setOrientation(LinearLayout.HORIZONTAL);
                top.setGravity(Gravity.CENTER_VERTICAL);
                TextView nm = text(u.pageName.length() > 0 ? u.pageName : "নাম ছাড়া পেজ", 16, TEXT, true);
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                top.addView(nm, mp);
                top.addView(chip(u.pageStatusBn(), u.localOnly ? AMBER_SOFT : GREEN_SOFT, u.localOnly ? AMBER : GREEN));
                row.addView(top);
                row.addView(text("কাস্টমার: " + u.name, 13, TEXT, false));
                if (u.pageId.length() > 0) row.addView(text("Page ID: " + u.pageId, 12, TEXT2, false));
                if (u.localOnly) row.addView(text(" শুধু এই সেশনে দেখা যাচ্ছে — সংরক্ষণ করা যায়নি।", 12, AMBER, true));
                row.setBackground(rippleBackground());
                row.setClickable(true);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { currentCustomer = u; go(S_CUSTOMER_DETAIL, true); }
                });
                content.addView(row);
            }
        }
        content.addView(primaryButton("＋ কাস্টমার পেজ যোগ করুন", new View.OnClickListener() {
            @Override public void onClick(View v) { pendingPageUser = null; go(S_PAGE_FORM, true); }
        }));
    }

    private void showPageForm() {
        final UsersStore.User pre = pendingPageUser;
        content.addView(ghostButton("পেজে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_PAGES, false); }
        }));
        content.addView(text("কাস্টমার পেজ রেকর্ড", 26, NAVY, true));
        // v8.2: plain guidance, no developer wording. The customer's app
        // account is automatic — nobody is asked to find or type an ID.
        LinearLayout note = card();
        note.setBackground(rounded(AMBER_SOFT, 20));
        note.addView(text("আপনার অ্যাকাউন্ট স্বয়ংক্রিয়ভাবে যুক্ত থাকবে — কোনো আইডি লিখতে হবে না।", 13, TEXT, true));
        note.addView(text("শুধু পেজের নাম আর পেজ লিংক লিখুন। লিংক পাবেন ফেসবুকে আপনার পেজ খুলে, উপরের ঠিকানা থেকে কপি করে।", 13, TEXT, false));
        note.addView(text("Page ID না জানলে খালি রাখুন — কানেক্ট হওয়ার পর অ্যাডমিন বসিয়ে দেবে।", 13, TEXT, false));
        content.addView(note);

        // v8.2: NO typed Customer ID field and NO typed customer-name
        // field. When editing from a customer detail screen, the
        // customer is already known internally (pre); when no customer
        // is picked yet, the save button is replaced by a button back
        // to the customer list (see below).
        final EditText pageNameInput = new EditText(this);
        pageNameInput.setHint("পেজের নাম *");
        pageNameInput.setTextSize(15);
        if (pre != null) pageNameInput.setText(pre.pageName);
        final EditText pageLinkInput = new EditText(this);
        pageLinkInput.setHint("পেজ লিংক (ফেসবুকে পেজ খুলে উপরের ঠিকানা থেকে কপি করুন)");
        pageLinkInput.setTextSize(15);
        if (pre != null) pageLinkInput.setText(pre.pageLink);
        final EditText pageIdInput = new EditText(this);
        pageIdInput.setHint("Page ID (ঐচ্ছিক) — না জানলে খালি রাখুন");
        pageIdInput.setTextSize(15);
        if (pre != null) pageIdInput.setText(pre.pageId);
        final EditText notesInput = new EditText(this);
        notesInput.setHint("নোট (ঐচ্ছিক)");
        notesInput.setTextSize(15);
        notesInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        notesInput.setMinLines(2);
        if (pre != null) notesInput.setText(pre.pageNotes);

        LinearLayout form = card();
        form.addView(text("পেজের তথ্য", 16, TEXT, true));
        if (pre != null) form.addView(text("কাস্টমার: " + pre.name, 14, TEXT2, false));
        for (EditText e : new EditText[]{pageNameInput, pageLinkInput, pageIdInput, notesInput}) {
            e.setBackground(roundedBorder(SOFT, 12));
            e.setPadding(dp(12), dp(10), dp(12), dp(10));
            form.addView(e);
        }
        // Status selector: three tappable rows, selection kept in an
        // array so the anonymous listeners can update it without a
        // re-render (which would wipe the typed inputs).
        final String[] sel = new String[]{(pre != null && pre.pageStatus.length() > 0) ? pre.pageStatus : "pending"};
        final String[] keys = new String[]{"pending", "test", "connected"};
        final String[] labels = new String[]{"অ্যাডমিন অ্যাক্সেস অপেক্ষমাণ", "টেস্ট পেজ", "কানেক্টেড"};
        final TextView[] statusLabels = new TextView[3];
        form.addView(text("কানেকশন স্ট্যাটাস", 15, TEXT, true));
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            LinearLayout rrow = new LinearLayout(this);
            rrow.setOrientation(LinearLayout.HORIZONTAL);
            rrow.setGravity(Gravity.CENTER_VERTICAL);
            rrow.setPadding(0, dp(6), 0, dp(6));
            final TextView lbl = text((sel[0].equals(keys[idx]) ? " " : "○ ") + labels[idx], 15,
                sel[0].equals(keys[idx]) ? PRIMARY : TEXT, sel[0].equals(keys[idx]));
            statusLabels[idx] = lbl;
            rrow.addView(lbl);
            rrow.setBackground(rippleBackground());
            rrow.setClickable(true);
            rrow.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    sel[0] = keys[idx];
                    for (int j = 0; j < 3; j++) {
                        statusLabels[j].setText((sel[0].equals(keys[j]) ? " " : "○ ") + labels[j]);
                        statusLabels[j].setTextColor(sel[0].equals(keys[j]) ? PRIMARY : TEXT);
                        statusLabels[j].setTypeface(sel[0].equals(keys[j]) ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    }
                }
            });
            form.addView(rrow);
        }
        content.addView(form);
        // v8.2: with no customer picked yet there is nothing to save
        // to — and nobody is asked to type an ID to create one. Pick
        // the customer from the list first, then edit their record.
        if (pre == null) {
            content.addView(outlineButton("কাস্টমার তালিকা খুলুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_CUSTOMERS, true); }
            }));
            return;
        }
        content.addView(primaryButton("পেজ রেকর্ড সংরক্ষণ করুন", new View.OnClickListener() {
            @Override public void onClick(View v) {
                final String pgName = pageNameInput.getText().toString().trim();
                final String pgLink = pageLinkInput.getText().toString().trim();
                final String pgId = pageIdInput.getText().toString().trim();
                final String pgNotes = notesInput.getText().toString().trim();
                if (pgName.length() == 0) {
                    Toast.makeText(MainActivity.this, "পেজের নাম লিখুন।", Toast.LENGTH_SHORT).show();
                    return;
                }
                // v8.2: the customer is the already-picked record (pre);
                // its id is used internally and never typed or shown as
                // an input. Page ID stays optional (pgId may be empty).
                final UsersStore.User target = pre;
                final java.util.Map<String, Object> fields = new java.util.HashMap<String, Object>();
                fields.put("pageName", pgName);
                fields.put("pageLink", pgLink);
                fields.put("pageId", pgId);
                fields.put("pageStatus", sel[0]);
                fields.put("pageNotes", pgNotes);
                FirebaseAuthHelper.updateUserPageFields(target.id, fields,
                    new FirebaseAuthHelper.ResultCallback<Boolean>() {
                        @Override public void onSuccess(Boolean ok) {
                            target.pageName = pgName;
                            target.pageLink = pgLink;
                            target.pageId = pgId;
                            target.pageStatus = sel[0];
                            target.pageNotes = pgNotes;
                            target.localOnly = false;
                            pendingPageUser = null;
                            notice = "পেজ রেকর্ড সংরক্ষণ হয়েছে।";
                            go(S_PAGES, false);
                        }
                        @Override public void onError(String message) {
                            target.pageName = pgName;
                            target.pageLink = pgLink;
                            target.pageId = pgId;
                            target.pageStatus = sel[0];
                            target.pageNotes = pgNotes;
                            target.localOnly = true;
                            notice = "সংরক্ষণ করা যায়নি: " + message + " — রেকর্ডটি শুধু এই সেশনে দেখা যাচ্ছে।";
                            go(S_PAGES, false);
                        }
                    });
            }
        }));
    }

    private void showAbout() {
        content.addView(ghostButton("ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { onBackPressed(); }
        }));
        LinearLayout c = card();
        c.addView(text("AutoMateIQ", 26, NAVY, true));
        TextView d = text("AutoMate Your Business — Custom Bots, Seamless Interactions। এই অ্যাপ থেকে আপনার পেজের লিড, মেসেজ, সাবস্ক্রিপশন আর টোকেন — সব মোবাইল থেকে সহজে ম্যানেজ করা যায়।", 15, TEXT2, false);
        d.setPadding(0, dp(8), 0, 0);
        c.addView(d);
        content.addView(c);
        LinearLayout f = card();
        f.addView(text("এই অ্যাপে যা আছে", 16, TEXT, true));
        String[] feats = {"মোবাইল ড্যাশবোর্ড — লিড, হ্যান্ডওভার, বট রিপ্লাই",
            "লিড থ্রেড — অ্যাপ থেকেই রিপ্লাই, Take Over / Resume AI",
            "ফেসবুক কানেক্ট — পেজ কানেক্টের ধাপ",
            "মাসিক সাবস্ক্রিপশন + কত দিন বাকি",
            "এপিআই ক্রেডিট টোকেন রিচার্জ"};
        for (String feat : feats) {
            TextView row = text("  " + feat, 14, TEXT, false);
            row.setPadding(0, dp(5), 0, 0);
            f.addView(row);
        }
        content.addView(f);
        
        
        TextView ver = text("ভার্সন 8.1", 12, TEXT2, false);
        ver.setGravity(Gravity.CENTER);
        ver.setPadding(0, dp(8), 0, 0);
        content.addView(ver);
    }
}
