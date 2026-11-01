package com.automateiq.app;

import android.app.Activity;
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

/**
 * AutoMateIQ Mobile Dashboard v6.0 — IN-APP ORDERS + ORDER MANAGEMENT +
 * SUBSCRIPTION FROM REAL ORDERS + CHAT REFRESH FIX.
 *
 * v6 ADDS (user feedback 2026-11-01 23:29): orders are placed and
 * managed INSIDE the app (no website redirect for ordering), using
 * the same Firestore 'orders' collection/shape as the website;
 * My Subscription is derived from real monthly orders (date + 1
 * month, days remaining); and the chat refresh bug is fixed by
 * making the Render server the single writer of owner messages
 * into Firestore, with the app reloading the lead doc after send.
 *
 * v5 FIXES THE v4 CHAT BUG (user feedback 2026-11-01 22:47): v4 put the WHOLE
 * app in ONE ScrollView and render() scrolled to top, so a thread opened at
 * the OLDEST message, the composer sat below all messages, and after send
 * the view jumped to top again so the sent message was invisible. v5 does
 * NOT do that: the thread has its OWN ScrollView, a PINNED bottom composer,
 * opens at the LATEST message, and stays at the bottom after send.
 * Owner replies also auto-pause the bot now (paused=true, status=handover,
 * written to Firestore too), so the bot cannot double-reply.
 * No WebView anywhere. No phone number or email is shown anywhere.
 *
 * What changed vs v3 (the design the user rejected as "একদম বাজে"):
 * - A real design system, taken from the AutoMateIQ website palette
 *   (primary #2563EB, deep navy #172554, slate text #0F172A, light
 *   surface #EFF6FF, violet #8B5CF6 / cyan #06B6D2 accents), instead of
 *   plain unstyled Views: consistent typography scale, 16dp page
 *   padding, 20dp rounded cards with elevation, a proper top app bar,
 *   a bottom navigation bar with a selected pill state, coloured
 *   status chips, chat bubbles, a subscription progress bar, and a
 *   loading / empty / error state on every list — never a blank screen,
 *   never a dead button.
 * - LOGIN IS IN-APP: "Continue with Google" starts Google Sign-In +
 *   Firebase Auth inside the app (FirebaseAuthHelper). It NEVER opens
 *   the website for login. Without the real google-services.json the
 *   button shows the in-app "Firebase setup pending" screen instead.
 * - LIVE FIRST: leads come from the website Firestore database
 *   (named database, ownerId filter). Demo content exists ONLY behind
 *   the explicit "Demo preview" button, never as the default list.
 * - No WebView anywhere. No phone number or email is shown anywhere.
 *   Verified prices only; Vibe Coding / Combo show no price;
 *   per-message rate is never invented. One-time packages get NO
 *   days-remaining display and NO separate one-time function yet
 *   (user will discuss that later).
 */
public class MainActivity extends Activity {

    // ---- Design tokens (website palette) ----
    private static final int PRIMARY = 0xFF2563EB;
    private static final int PRIMARY_DARK = 0xFF1E3A8A;
    private static final int NAVY = 0xFF172554;
    private static final int VIOLET = 0xFF8B5CF6;
    private static final int CYAN = 0xFF06B6D2;
    private static final int BG = 0xFFF1F5F9;
    private static final int SURFACE = 0xFFFFFFFF;
    private static final int TEXT = 0xFF0F172A;
    private static final int TEXT2 = 0xFF64748B;
    private static final int BORDER = 0xFFE2E8F0;
    private static final int SOFT = 0xFFEFF6FF;
    private static final int SOFT_BLUE = 0xFFDBEAFE;
    private static final int GREEN = 0xFF16A34A;
    private static final int GREEN_SOFT = 0xFFDCFCE7;
    private static final int AMBER = 0xFFD97706;
    private static final int AMBER_SOFT = 0xFFFEF3C7;
    private static final int RED = 0xFFDC2626;
    private static final int RED_SOFT = 0xFFFEE2E2;

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
    private boolean demoMode = false;
    private boolean leadsLoading = false;
    private String leadsError = null;
    private String loginError = null;
    private String notice = null;
    private String currentFilter = "all";
    private LeadsStore.Lead currentLead = null;
    private Service currentService = null;

    // v6 ORDERS: live list from Firestore 'orders' (same as website).
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

    // ---- Services (verified website prices only) ----
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
        new Service("💬", "মেসেঞ্জার চ্যাটবট",
            "ফেসবুক পেজে কাস্টমার মেসেজ করলে ২৪ ঘণ্টা স্বয়ংক্রিয় উত্তর দেবে, মালিক অনলাইনে না থাকলেও।",
            "৳৫০ থেকে শুরু", "#2563EB",
            new String[]{"ট্রায়াল ৭ দিন — ৳৫০", "স্ট্যান্ডার্ড — ৳৭০০/মাস",
                         "প্রিমিয়াম — ৳১,০০০/মাস", "এককালীন — ৳৫,০০০"},
            new String[]{"২৪ ঘণ্টা অটো রিপ্লাই", "মানুষের কাছে হ্যান্ডওভার",
                         "এককালীন প্যাকেজে ৳৫০০ ফ্রি ক্রেডিট"}),
        new Service("📸", "ইনস্টাগ্রাম চ্যাটবট",
            "ইনস্টাগ্রাম ডিএম এবং স্টোরি রিপ্লাই অটোমেশন। বিজনেস গ্রোথ এবং এনগেজমেন্ট বৃদ্ধি।",
            "৳৫০ থেকে শুরু", "#8B5CF6",
            new String[]{"ট্রায়াল — ৳৫০", "স্ট্যান্ডার্ড — ৳৭০০/মাস",
                         "প্রিমিয়াম — ৳১,০০০/মাস", "এককালীন — ৳৫,০০০"},
            new String[]{"ইনস্টাগ্রাম ডিএম অটো রিপ্লাই", "স্টোরি রিপ্লাই অটোমেশন"}),
        new Service("🟢", "হোয়াটসঅ্যাপ বিজনেস বট",
            "হোয়াটসঅ্যাপে কাস্টমারের সাথে স্বয়ংক্রিয় কথোপকথন ও ক্যাটালগ দেখানো।",
            "৳৫০ থেকে শুরু", "#16A34A",
            new String[]{"ট্রায়াল — ৳৫০", "স্ট্যান্ডার্ড সেটআপ — ৳১,৫০০",
                         "এককালীন — ৳৫,০০০"},
            new String[]{"হোয়াটসঅ্যাপে প্রোডাক্ট ক্যাটালগ দেখানো", "স্বয়ংক্রিয় কথোপকথন"}),
        new Service("📘", "ফেসবুক অটো পোস্ট",
            "ফেসবুক পেজে স্বয়ংক্রিয়ভাবে পোস্ট প্রকাশ করা।",
            "৳৫০ থেকে শুরু", "#1D4ED8",
            new String[]{"ট্রায়াল — ৳৫০", "মাসিক — ৳১,০০০", "এককালীন — ৳৫,০০০"},
            new String[]{"নির্ধারিত সময়ে অটো পোস্ট", "কন্টেন্ট শিডিউল"}),
        new Service("▶️", "ইউটিউব অটো পোস্ট",
            "ইউটিউব চ্যানেলে স্বয়ংক্রিয়ভাবে পোস্ট প্রকাশ করা।",
            "৳৫০ থেকে শুরু", "#DC2626",
            new String[]{"ট্রায়াল — ৳৫০", "মাসিক — ৳১,০০০", "এককালীন — ৳৫,০০০"},
            new String[]{"চ্যানেলে অটো পোস্ট", "কন্টেন্ট শিডিউল"}),
        new Service("🤝", "ফেসবুক + ইনস্টাগ্রাম কম্বো",
            "ফেসবুক ও ইনস্টাগ্রাম দুই প্ল্যাটফর্মের অটোমেশন একসাথে। দাম ওয়েবসাইটে দেওয়া নেই — প্রতিনিধি জানিয়ে দেবেন।",
            "দাম জানতে জিজ্ঞাসা করুন", "#06B6D2",
            new String[]{"দাম ওয়েবসাইটে দেওয়া নেই — প্রতিনিধি জানাবেন"},
            new String[]{"দুই প্ল্যাটফর্ম একসাথে", "এক জায়গায় ম্যানেজমেন্ট"}),
        new Service("💻", "ভাইব কোডিং",
            "আইডিয়া থেকে অ্যাপ/ওয়েবসাইট বানানো। দাম জানতে চাইলে শুধু ট্রান্সফার করা হয় — প্রতিনিধি বিস্তারিত জানাবেন।",
            "দাম জানতে জিজ্ঞাসা করুন", "#0F172A",
            new String[]{"দাম জানতে চাইলে প্রতিনিধির কাছে ট্রান্সফার করা হয়"},
            new String[]{"আইডিয়া থেকে প্রোডাক্ট", "দ্রুত ডেলিভারি"}),
        new Service("🪙", "এপিআই ক্রেডিট ওয়ালেট",
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
        // Resume an existing Firebase session in-app (no website involved).
        if (FirebaseAuthHelper.currentUser() != null) {
            signedIn = true;
            demoMode = false;
            loadLiveLeads();
            loadLiveOrders();
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
                        demoMode = false;
                        loginError = null;
                        loadLiveLeads();
                        loadLiveOrders();
                        go(S_DASH, false);
                    }
                    @Override public void onError(String message) {
                        loginError = message;
                        go(S_LOGIN, false);
                    }
                });
        }
    }

    @Override
    public void onBackPressed() {
        if (screen == S_LOGIN) { super.onBackPressed(); return; }
        if (!backStack.isEmpty()) {
            screen = backStack.remove(backStack.size() - 1);
            render();
        } else if (signedIn || demoMode) {
            go(S_DASH, false);
        } else {
            super.onBackPressed();
        }
    }

    private void go(int next, boolean push) {
        if (push) backStack.add(screen);
        screen = next;
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
                    leadsStore.setLiveLeads(live);
                    leadsLoading = false;
                    leadsError = null;
                    if (screen == S_DASH || screen == S_LEADS) render();
                }
                @Override public void onError(String message) {
                    leadsLoading = false;
                    leadsError = message;
                    if (screen == S_DASH || screen == S_LEADS) render();
                }
            });
    }

    private void enterDemoPreview() {
        demoMode = true;
        signedIn = false;
        leadsStore.setLiveLeads(LeadsStore.demoLeads());
        mySubscription = SubscriptionStore.demoSubscription();
        orders = OrderStore.demoOrders();
        ordersLoaded = true;
        leadsLoading = false;
        leadsError = null;
        notice = "ডেমো প্রিভিউ চলছে — এগুলো আসল কাস্টমার নয়।";
        go(S_DASH, false);
    }

    private void logout() {
        FirebaseAuthHelper.signOut(this);
        signedIn = false;
        demoMode = false;
        mySubscription = null;
        orders = new ArrayList<OrderStore.Order>();
        ordersLoaded = false;
        currentOrder = null;
        selectedPackage = null;
        leadsStore.setLiveLeads(new ArrayList<LeadsStore.Lead>());
        backStack.clear();
        go(S_LOGIN, false);
    }

    // ---- Shell: top bar + scroll content + bottom nav ----
    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

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
        threadPane.setBackgroundColor(BG);
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

    private void renderTopBar() {
        topBar.removeAllViews();
        TextView logo = new TextView(this);
        logo.setText("A");
        logo.setTextSize(20);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(Color.WHITE);
        logo.setGravity(Gravity.CENTER);
        GradientDrawable lg = rounded(PRIMARY, 14);
        logo.setBackground(lg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(40), dp(40));
        topBar.addView(logo, lp);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(10), 0, 0, 0);
        TextView t = text("AutoMateIQ", 18, TEXT, true);
        TextView s = text("মোবাইল ড্যাশবোর্ড • v6.0", 12, TEXT2, false);
        titles.addView(t);
        titles.addView(s);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        topBar.addView(titles, tp);

        if (signedIn || demoMode) {
            TextView out = new TextView(this);
            out.setText("লগআউট");
            out.setTextSize(13);
            out.setTypeface(Typeface.DEFAULT_BOLD);
            out.setTextColor(RED);
            out.setPadding(dp(10), dp(6), dp(10), dp(6));
            out.setBackground(rounded(RED_SOFT, 20));
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
        String[][] items = {
            {"🏠", "ড্যাশবোর্ড", String.valueOf(S_DASH)},
            {"💬", "লিডস", String.valueOf(S_LEADS)},
            {"🔗", "কানেক্ট", String.valueOf(S_CONNECT)},
            {"🛍️", "সার্ভিস", String.valueOf(S_SERVICES)},
            {"📦", "অর্ডার", String.valueOf(S_ORDERS)},
            {"📅", "সাবস্ক্রিপশন", String.valueOf(S_SUBS)},
        };
        for (final String[] it : items) {
            final int target = Integer.parseInt(it[2]);
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            item.setPadding(dp(4), dp(6), dp(4), dp(6));
            boolean selected = screen == target
                || (target == S_DASH && (screen == S_DASH))
                || (target == S_LEADS && (screen == S_LEAD_DETAIL))
                || (target == S_ORDERS && (screen == S_ORDERS
                    || screen == S_ORDER_DETAIL || screen == S_ORDER_FORM
                    || screen == S_RECHARGE_FORM))
                || (target == S_SUBS && (screen == S_MY_SUB || screen == S_TOKENS));
            if (selected) item.setBackground(rounded(SOFT_BLUE, 14));
            else item.setBackground(rippleBackground());
            TextView icon = new TextView(this);
            icon.setText(it[0]);
            icon.setTextSize(18);
            icon.setGravity(Gravity.CENTER);
            TextView label = new TextView(this);
            label.setText(it[1]);
            label.setTextSize(11);
            label.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            label.setTextColor(selected ? PRIMARY : TEXT2);
            label.setGravity(Gravity.CENTER);
            item.addView(icon);
            item.addView(label);
            item.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { go(target, false); }
            });
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            bottomNav.addView(item, ip);
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
        content.removeAllViews();
        scroll.scrollTo(0, 0);
        if (notice != null) {
            content.addView(noticeCard(notice));
            notice = null;
        }
        switch (screen) {
            case S_LOGIN: showLogin(); break;
            case S_PENDING: showPending(); break;
            case S_DASH: showDashboard(); break;
            case S_LEADS: showLeads(); break;
            // S_LEAD_DETAIL is handled before the switch in render()
            // (threadPane, not the main ScrollView) — never reaches here.
            case S_CONNECT: showConnect(); break;
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
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(rounded(SURFACE, 20));
        c.setElevation(dp(3));
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(14));
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
        c.setBackground(rounded(bg, 20));
        c.setPadding(dp(10), dp(4), dp(10), dp(4));
        return c;
    }

    private TextView statusChip(LeadsStore.Lead lead) {
        if (lead.paused) return chip("⏸ হ্যান্ডওভার", AMBER_SOFT, AMBER);
        if ("bot_replied".equals(lead.status)) return chip("🤖 বট রিপ্লাই", GREEN_SOFT, GREEN);
        return chip("🆕 নতুন", SOFT_BLUE, PRIMARY);
    }

    private TextView primaryButton(String label, View.OnClickListener onClick) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(16);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setBackground(rounded(PRIMARY, 14));
        b.setPadding(dp(16), dp(13), dp(16), dp(13));
        b.setOnClickListener(onClick);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(lp);
        return b;
    }

    private TextView outlineButton(String label, final int color, View.OnClickListener onClick) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(color);
        b.setGravity(Gravity.CENTER);
        GradientDrawable d = rounded(SURFACE, 14);
        d.setStroke(dp(1), color);
        b.setBackground(d);
        b.setPadding(dp(16), dp(12), dp(16), dp(12));
        b.setOnClickListener(onClick);
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

    // Website payment number, exactly as shown in the website OrderModal
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
        c.setBackground(rounded(SOFT_BLUE, 20));
        c.addView(text("bKash/Nagad (Personal)", 16, NAVY, true));
        TextView num = text(PAYMENT_NUMBER, 22, PRIMARY, true);
        num.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        num.setPadding(0, dp(6), 0, dp(2));
        c.addView(num);
        c.addView(text("এই নম্বরে টাকা পাঠান, তারপর নিচে আপনার Sender নম্বর + TrxID লিখুন।", 13, TEXT2, false));
        c.addView(outlineButton("📋 পেমেন্ট নম্বর কপি করুন", PRIMARY, new View.OnClickListener() {
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
        a.setBackground(rounded(color, 24));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
        a.setLayoutParams(lp);
        return a;
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
            ViewGroup.LayoutParams.MATCH_PARENT, dp(10));
        blp.setMargins(0, dp(8), 0, dp(4));
        bar.setLayoutParams(blp);
        int p = Math.max(0, Math.min(100, percent));
        LinearLayout fill = new LinearLayout(this);
        fill.setBackground(rounded(PRIMARY, 8));
        bar.addView(fill, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, p));
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
        logo.setTextSize(40);
        logo.setTypeface(Typeface.DEFAULT_BOLD);
        logo.setTextColor(Color.WHITE);
        logo.setGravity(Gravity.CENTER);
        logo.setBackground(gradient(PRIMARY, VIOLET));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(dp(88), dp(88));
        llp.gravity = Gravity.CENTER;
        hero.addView(logo, llp);
        TextView name = text("AutoMateIQ", 28, NAVY, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, dp(14), 0, 0);
        hero.addView(name);
        TextView tag = text("আপনার বিজনেস, আপনার হাতে — মোবাইল ড্যাশবোর্ড", 15, TEXT2, false);
        tag.setGravity(Gravity.CENTER);
        hero.addView(tag);
        content.addView(hero);

        LinearLayout c = card();
        c.setPadding(dp(20), dp(20), dp(20), dp(20));
        c.addView(text("স্বাগতম", 20, TEXT, true));
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
        google.setText("G   Continue with Google");
        google.setTextSize(16);
        google.setTypeface(Typeface.DEFAULT_BOLD);
        google.setTextColor(TEXT);
        google.setGravity(Gravity.CENTER);
        google.setBackground(roundedBorder(SURFACE, 14));
        google.setElevation(dp(1));
        google.setPadding(dp(16), dp(14), dp(16), dp(14));
        google.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                // IN-APP LOGIN ONLY. No website redirect, ever, for login.
                if (!FirebaseAuthHelper.isFirebaseReady(MainActivity.this)) {
                    go(S_PENDING, true);
                } else {
                    loginError = null;
                    FirebaseAuthHelper.startGoogleSignIn(MainActivity.this);
                }
            }
        });
        c.addView(google);
        TextView note = text("লগইন অ্যাপের ভিতরেই হবে — ওয়েবসাইটে যাবে না।", 13, TEXT2, false);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(10), 0, 0);
        c.addView(note);
        content.addView(c);

        content.addView(ghostButton("ডেমো প্রিভিউ দেখুন (আসল ডাটা নয়)",
            new View.OnClickListener() {
                @Override public void onClick(View v) { enterDemoPreview(); }
            }));
        TextView about = text("অ্যাপ সম্পর্কে জানুন", 13, TEXT2, false);
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
        c.addView(text("⚠️ Firebase সেটআপ চলছে", 20, TEXT, true));
        TextView body = text("Firebase setup pending — app registration in progress। এই Android অ্যাপটি Firebase-এ রেজিস্টার হওয়ার পর Google লগইন অ্যাপের ভিতরেই চালু হবে। ততক্ষণ লগইন ওয়েবসাইটে যাবে না, কারণ সেটাই আগের সমস্যা ছিল।", 15, TEXT, false);
        body.setPadding(0, dp(8), 0, dp(12));
        c.addView(body);
        content.addView(c);
        content.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (FirebaseAuthHelper.isFirebaseReady(MainActivity.this)) {
                    FirebaseAuthHelper.startGoogleSignIn(MainActivity.this);
                } else {
                    notice = "এখনো Firebase সেটআপ সম্পন্ন হয়নি। কিছুক্ষণ পর আবার চেষ্টা করুন।";
                    render();
                }
            }
        }));
        content.addView(outlineButton("ডেমো প্রিভিউ দেখুন (আসল ডাটা নয়)", PRIMARY,
            new View.OnClickListener() {
                @Override public void onClick(View v) { enterDemoPreview(); }
            }));
        content.addView(ghostButton("← লগইন স্ক্রিনে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_LOGIN, false); }
        }));
    }

    // ---- Screens: Dashboard / Leads ----
    private void showDashboard() {
        if (demoMode) {
            content.addView(bannerCard("🧪 ডেমো প্রিভিউ — এগুলো আসল কাস্টমার বা আসল সাবস্ক্রিপশন নয়", AMBER_SOFT, AMBER));
        } else if (signedIn) {
            content.addView(bannerCard("● লাইভ মোড — ওয়েবসাইটের একই Firebase ডাটাবেস থেকে লিড আসছে", GREEN_SOFT, GREEN));
        }

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(gradient(PRIMARY, VIOLET));
        hero.setElevation(dp(2));
        hero.setPadding(dp(20), dp(20), dp(20), dp(20));
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.setMargins(0, 0, 0, dp(14));
        hero.setLayoutParams(hlp);
        String who = demoMode ? "ডেমো ব্যবহারকারী" : "স্বাগতম";
        hero.addView(text(who, 24, Color.WHITE, true));
        hero.addView(text("আজকের ড্যাশবোর্ড — লিড, হ্যান্ডওভার আর সাবস্ক্রিপশন এক নজরে", 14, Color.WHITE, false));
        content.addView(hero);

        // Stat grid 2x2
        String[][] stats = {
            {"💬", bn(String.valueOf(leadsStore.total())), "মোট লিড"},
            {"⏸", bn(String.valueOf(leadsStore.handoverCount())), "হ্যান্ডওভার"},
            {"🤖", bn(String.valueOf(leadsStore.botRepliedCount())), "বট রিপ্লাই"},
            {"🆕", bn(String.valueOf(leadsStore.newCount())), "নতুন"},
        };
        for (int r = 0; r < 2; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int k = 0; k < 2; k++) {
                final String[] st = stats[r * 2 + k];
                LinearLayout sc = new LinearLayout(this);
                sc.setOrientation(LinearLayout.VERTICAL);
                sc.setBackground(rounded(SURFACE, 20));
                sc.setElevation(dp(2));
                sc.setPadding(dp(16), dp(14), dp(16), dp(14));
                LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                slp.setMargins(k == 0 ? 0 : dp(7), 0, k == 0 ? dp(7) : 0, dp(14));
                sc.setLayoutParams(slp);
                sc.addView(text(st[0] + "  " + st[2], 13, TEXT2, false));
                sc.addView(text(st[1], 26, NAVY, true));
                final String filter = k == 0 && r == 0 ? "all"
                    : (r == 0 ? "handover" : (k == 0 ? "bot_replied" : "new"));
                sc.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        currentFilter = filter;
                        go(S_LEADS, true);
                    }
                });
                row.addView(sc);
            }
            content.addView(row);
        }

        // Subscription card
        if (mySubscription != null) {
            LinearLayout c = card();
            c.addView(text("⭐ আমার সাবস্ক্রিপশন", 16, TEXT, true));
            TextView line = text(mySubscription.service + " • " + mySubscription.pkg
                + " • " + mySubscription.price, 15, TEXT, false);
            line.setPadding(0, dp(6), 0, 0);
            c.addView(line);
            LinearLayout row2 = new LinearLayout(this);
            row2.setOrientation(LinearLayout.HORIZONTAL);
            row2.setPadding(0, dp(10), 0, 0);
            TextView days = text(SubscriptionStore.daysRemainingBn(mySubscription), 22, PRIMARY, true);
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
                + "  •  শেষ: " + SubscriptionStore.formatDate(mySubscription.expiryMillis), 13, TEXT2, false);
            c.addView(dates);
            c.addView(ghostButton("সাবস্ক্রিপশন বিস্তারিত →", new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_MY_SUB, true); }
            }));
            content.addView(c);
        } else {
            LinearLayout c = card();
            c.addView(text("⭐ আমার সাবস্ক্রিপশন", 16, TEXT, true));
            TextView t2 = text("কোনো মাসিক সাবস্ক্রিপশন এখনো যুক্ত হয়নি। মাসিক প্যাকেজ কিনলে এখানে কত দিন বাকি আছে দেখা যাবে।", 14, TEXT2, false);
            t2.setPadding(0, dp(6), 0, 0);
            c.addView(t2);
            c.addView(primaryButton("মাসিক প্যাকেজ দেখুন", new View.OnClickListener() {
                @Override public void onClick(View v) { go(S_SUBS, true); }
            }));
            content.addView(c);
        }

        // Quick actions
        LinearLayout qc = card();
        qc.addView(text("দ্রুত কাজ", 16, TEXT, true));
        qc.addView(outlineButton("💬 লিড দেখুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_LEADS, true); }
        }));
        qc.addView(outlineButton("🔗 ফেসবুক কানেক্ট", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_CONNECT, true); }
        }));
        qc.addView(outlineButton("🪙 টোকেন কিনুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_TOKENS, true); }
        }));
        qc.addView(outlineButton("📦 অর্ডার করুন (অ্যাপের ভিতরেই)", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { selectedPackage = null; go(S_ORDER_FORM, true); }
        }));
        qc.addView(outlineButton("📦 আমার অর্ডার দেখুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ORDERS, true); }
        }));
        content.addView(qc);

        // Recent leads
        LinearLayout recent = card();
        recent.addView(text("সাম্প্রতিক লিড", 16, TEXT, true));
        if (leadsLoading) {
            TextView l = text("লোড হচ্ছে…", 14, TEXT2, false);
            l.setPadding(0, dp(8), 0, 0);
            recent.addView(l);
        } else if (leadsError != null) {
            TextView l = text(leadsError, 14, RED, true);
            l.setPadding(0, dp(8), 0, 0);
            recent.addView(l);
            recent.addView(primaryButton("আবার চেষ্টা করুন", new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveLeads(); render(); }
            }));
        } else if (leadsStore.all().isEmpty()) {
            TextView l = text("এখনো কোনো লিড নেই। কেউ আপনার পেজে মেসেজ করলে এখানে লিড দেখা যাবে।", 14, TEXT2, false);
            l.setPadding(0, dp(8), 0, 0);
            recent.addView(l);
            recent.addView(outlineButton("লিড রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (demoMode) render(); else loadLiveLeads();
                }
            }));
        } else {
            List<LeadsStore.Lead> all = leadsStore.all();
            for (int i = 0; i < Math.min(3, all.size()); i++) {
                recent.addView(leadRow(all.get(i)));
            }
            recent.addView(ghostButton("সব লিড দেখুন →", new View.OnClickListener() {
                @Override public void onClick(View v) { currentFilter = "all"; go(S_LEADS, true); }
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
        content.addView(text("Messenger Leads", 24, NAVY, true));
        TextView count = text("মোট " + bn(String.valueOf(leadsStore.total())) + "টি লিড", 14, TEXT2, false);
        count.setPadding(0, dp(4), 0, dp(12));
        content.addView(count);
        if (demoMode) {
            content.addView(bannerCard("🧪 ডেমো প্রিভিউ — এগুলো আসল কাস্টমার নয়", AMBER_SOFT, AMBER));
        }
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
            foot.addView(outlineButton("📋 সব লিডের সারাংশ কপি করুন", PRIMARY,
                new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        copyText("leads", leadsStore.copyAllLeadsSummary());
                    }
                }));
            foot.addView(outlineButton("🔄 লিড রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (demoMode) render(); else loadLiveLeads();
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

        // Fixed header: back + name + status. Never scrolls away.
        LinearLayout head = card();
        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("← ফিরুন");
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
        head.addView(outlineButton(lead.paused ? "▶ Resume AI (বট আবার চালু করুন)"
                : "⏸ Take Over (বট থামিয়ে নিজে উত্তর দিন)",
            lead.paused ? GREEN : AMBER, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    toggleLeadPause(lead);
                }
            }));
        head.addView(ghostButton("📋 লিড ডিটেইলস কপি করুন",
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    copyText("lead", leadsStore.copyLeadDetails(lead));
                }
            }));
        head.addView(ghostButton("🔄 রিফ্রেশ (সার্ভার থেকে আবার আনুন)",
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
            bubble.setTextSize(15);
            bubble.setTextColor(isOwner ? Color.WHITE : TEXT);
            bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
            bubble.setBackground(rounded(isOwner ? PRIMARY : isBot ? SOFT_BLUE : SURFACE, 16));
            if (!isOwner && !isBot) {
                bubble.setBackground(roundedBorder(SURFACE, 16));
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
                    TextView st = text("⏳ পাঠানো হচ্ছে…", 12, TEXT2, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                } else if ("sent".equals(m.sendStatus)) {
                    TextView st = text("✓ Messenger-এ পাঠানো হয়েছে", 12, GREEN, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                } else if ("sent_pending_save".equals(m.sendStatus)) {
                    TextView st = text("✓ Messenger-এ পাঠানো হয়েছে • চ্যাট হিস্ট্রিতে সেভ হচ্ছে…", 12, AMBER, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                } else if ("failed".equals(m.sendStatus)) {
                    TextView st = text("✗ পাঠানো যায়নি — আবার চেষ্টা করুন", 12, RED, true);
                    st.setGravity(Gravity.END);
                    wrap.addView(st);
                    wrap.addView(ghostButton("↻ এই মেসেজ আবার পাঠান",
                        new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                resendThreadMessage(lead, m);
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
        comp.setBackgroundColor(SURFACE);
        comp.setElevation(dp(8));
        comp.setPadding(dp(16), dp(12), dp(16), dp(12));
        TextView info = text("এখানে লিখলে মেসেজ সরাসরি কাস্টমারের Messenger-এ যাবে (সার্ভারের মাধ্যমে)। "
            + "লেখার সাথে সাথে এই লিডের বট থেমে যাবে — বট আবার চালু করতে উপরের Resume AI চাপুন।",
            13, TEXT2, false);
        info.setPadding(0, 0, 0, dp(8));
        comp.addView(info);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        threadInput = new EditText(this);
        threadInput.setHint("রিপ্লাই লিখুন…");
        threadInput.setTextSize(15);
        threadInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        threadInput.setMinLines(2);
        threadInput.setBackground(roundedBorder(SOFT, 12));
        threadInput.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(threadInput, ip);
        TextView send = new TextView(this);
        send.setText("পাঠান ➤");
        send.setTextSize(15);
        send.setTypeface(Typeface.DEFAULT_BOLD);
        send.setTextColor(Color.WHITE);
        send.setGravity(Gravity.CENTER);
        send.setBackground(rounded(PRIMARY, 12));
        send.setPadding(dp(16), dp(12), dp(16), dp(12));
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.setMargins(dp(8), 0, 0, 0);
        send.setLayoutParams(slp);
        send.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
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
    // v6 SEND FLOW + CHAT REFRESH FIX: the Render server is the
    // SINGLE WRITER of the owner message into Firestore (see the bot
    // server change). The app never writes the owner message itself —
    // that would duplicate it. After a successful send the app RELOADS
    // the lead doc from Firestore and displays the reloaded thread, so
    // Refresh and re-open show exactly what Messenger shows. If the
    // server has not deployed the history-write yet (saved=false),
    // the message stays visible locally with an honest
    // "saving to chat history…" status instead of silently pretending
    // a locally-only message is saved.
    private void sendThreadMessage(final LeadsStore.Lead lead, final String msg) {
        final LeadsStore.Message m = leadsStore.addSendingOwnerMessage(lead, msg);
        if (threadInput != null) threadInput.setText("");
        renderThreadScreen();
        if (demoMode || FirebaseAuthHelper.currentUid() == null) {
            m.sendStatus = "sent";
            Toast.makeText(this, "ডেমো রিপ্লাই যোগ হয়েছে (লাইভে যায়নি)।", Toast.LENGTH_SHORT).show();
            renderThreadScreen();
            return;
        }
        FirebaseAuthHelper.sendLiveReplyWithSave(this, FirebaseAuthHelper.currentUid(), lead, msg,
            new FirebaseAuthHelper.ResultCallback<FirebaseAuthHelper.ReplyOutcome>() {
                @Override public void onSuccess(FirebaseAuthHelper.ReplyOutcome outcome) {
                    if (!outcome.saved) {
                        // Old server: Messenger got it, history did not.
                        m.sendStatus = "sent_pending_save";
                        FirebaseAuthHelper.updateLeadStatus(lead, true, "handover",
                            new FirebaseAuthHelper.ResultCallback<Boolean>() {
                                @Override public void onSuccess(Boolean o) { }
                                @Override public void onError(String e) { }
                            });
                        notice = "Messenger-এ পাঠানো হয়েছে, কিন্তু সার্ভারের পুরনো ভার্সন চ্যাট হিস্ট্রি সেভ করেনি — সার্ভার আপডেট ডেপ্লয় হলে রিফ্রেশে দেখা যাবে।";
                        renderThreadScreen();
                        return;
                    }
                    // Server saved it: pause, then reload the doc so the
                    // thread on screen IS the server-saved thread.
                    FirebaseAuthHelper.updateLeadStatus(lead, true, "handover",
                        new FirebaseAuthHelper.ResultCallback<Boolean>() {
                            @Override public void onSuccess(Boolean o) { reloadLeadIntoThread(lead); }
                            @Override public void onError(String e) { reloadLeadIntoThread(lead); }
                        });
                }
                @Override public void onError(String message) {
                    m.sendStatus = "failed";
                    renderThreadScreen();
                }
            });
    }

    private void resendThreadMessage(final LeadsStore.Lead lead, final LeadsStore.Message m) {
        m.sendStatus = "sending";
        renderThreadScreen();
        if (demoMode || FirebaseAuthHelper.currentUid() == null) {
            m.sendStatus = "sent";
            renderThreadScreen();
            return;
        }
        FirebaseAuthHelper.sendLiveReplyWithSave(this, FirebaseAuthHelper.currentUid(), lead, m.text,
            new FirebaseAuthHelper.ResultCallback<FirebaseAuthHelper.ReplyOutcome>() {
                @Override public void onSuccess(FirebaseAuthHelper.ReplyOutcome outcome) {
                    if (!outcome.saved) {
                        m.sendStatus = "sent_pending_save";
                        renderThreadScreen();
                        return;
                    }
                    FirebaseAuthHelper.updateLeadStatus(lead, true, "handover",
                        new FirebaseAuthHelper.ResultCallback<Boolean>() {
                            @Override public void onSuccess(Boolean o) { reloadLeadIntoThread(lead); }
                            @Override public void onError(String e) { reloadLeadIntoThread(lead); }
                        });
                }
                @Override public void onError(String message) {
                    m.sendStatus = "failed";
                    renderThreadScreen();
                }
            });
    }

    /** Reload ONE lead doc from Firestore and swap it into the list
     *  and the open thread. This is what makes Refresh truthful. */
    private void reloadLeadIntoThread(final LeadsStore.Lead lead) {
        if (demoMode || FirebaseAuthHelper.currentUid() == null) {
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
                    renderThreadScreen();
                }
                @Override public void onError(String message) {
                    Toast.makeText(MainActivity.this,
                        "রিলোড হয়নি: " + message, Toast.LENGTH_LONG).show();
                    renderThreadScreen();
                }
            });
    }

    /** Thread Refresh button: reload from Firestore, honestly. If the
     *  just-sent message is missing, the server history-write has not
     *  deployed yet — say so, instead of showing a stale thread. */
    private void refreshThread(final LeadsStore.Lead lead) {
        if (demoMode || FirebaseAuthHelper.currentUid() == null) {
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
                        notice = "রিফ্রেশে নতুন মেসেজ দেখা যাচ্ছে না — সার্ভারের হিস্ট্রি-সেভ আপডেট এখনো ডেপ্লয় হয়নি হতে পারে। Messenger-এ মেসেজটি ঠিকই পাঠানো হয়েছে।";
                    } else {
                        Toast.makeText(MainActivity.this, "রিফ্রেশ হয়েছে।", Toast.LENGTH_SHORT).show();
                    }
                    renderThreadScreen();
                }
                @Override public void onError(String message) {
                    Toast.makeText(MainActivity.this,
                        "রিফ্রেশ হয়নি: " + message, Toast.LENGTH_LONG).show();
                }
            });
    }

    // ---- v6: ORDERS (in-app, same Firestore 'orders' as website) ----

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
        if (demoMode) return;
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
        if (demoMode) {
            orders = OrderStore.demoOrders();
            ordersLoaded = true;
            ordersLoading = false;
            ordersError = null;
            deriveMySubscription();
            return;
        }
        ordersLoading = true;
        ordersError = null;
        final boolean admin = FirebaseAuthHelper.isAdmin();
        FirebaseAuthHelper.fetchOrders(uid, admin,
            new FirebaseAuthHelper.ResultCallback<List<OrderStore.Order>>() {
                @Override public void onSuccess(List<OrderStore.Order> live) {
                    orders = live;
                    ordersLoaded = true;
                    ordersLoading = false;
                    ordersError = null;
                    deriveMySubscription();
                    if (screen == S_DASH || screen == S_ORDERS || screen == S_MY_SUB) render();
                }
                @Override public void onError(String message) {
                    ordersLoading = false;
                    ordersError = message;
                    if (screen == S_DASH || screen == S_ORDERS || screen == S_MY_SUB) render();
                }
            });
    }

    private TextView orderStatusChip(OrderStore.Order o) {
        if ("in_progress".equals(o.status)) return chip("▶ চলমান", SOFT_BLUE, PRIMARY);
        if ("completed".equals(o.status)) return chip("✓ সম্পন্ন", GREEN_SOFT, GREEN);
        if ("cancelled".equals(o.status) || "rejected".equals(o.status)) return chip("✕ বাতিল", RED_SOFT, RED);
        return chip("⏳ অপেক্ষমাণ", AMBER_SOFT, AMBER);
    }

    private void showOrders() {
        content.addView(text("অর্ডার", 24, NAVY, true));
        final boolean admin = !demoMode && FirebaseAuthHelper.isAdmin();
        if (demoMode) {
            content.addView(bannerCard("🧪 ডেমো প্রিভিউ — এগুলো আসল অর্ডার নয়", AMBER_SOFT, AMBER));
        } else if (admin) {
            content.addView(bannerCard("অ্যাডমিন মোড — সব কাস্টমারের অর্ডার দেখা যাচ্ছে। স্ট্যাটাস শুধু অ্যাডমিন বদলাতে পারবেন।", GREEN_SOFT, GREEN));
            LinearLayout ruleNote = card();
            ruleNote.setBackground(rounded(AMBER_SOFT, 20));
            ruleNote.addView(text("অন্য কাস্টমারের অ্যাকাউন্টে সাবস্ক্রিপশন যোগ করা: এখনো চালু হয়নি — Firestore rules-এ orders create-এ অ্যাডমিন অনুমতি যোগ করতে হবে (আলাদা পেন্ডিং টাস্ক)। এখন অ্যাডমিন শুধু নিজের অ্যাকাউন্টের অর্ডার তৈরি করতে পারবেন; অন্যের userId দিয়ে তৈরি করলে rules বাধা দেবে।", 13, TEXT, false));
            content.addView(ruleNote);
        } else {
            content.addView(bannerCard("শুধু আপনার নিজের অর্ডার দেখা যাচ্ছে। অন্যের অর্ডার এখানে দেখানো হয় না।", SOFT_BLUE, PRIMARY));
        }
        content.addView(primaryButton("＋ নতুন অর্ডার করুন (অ্যাপের ভিতরেই)", new View.OnClickListener() {
            @Override public void onClick(View v) { selectedPackage = null; go(S_ORDER_FORM, true); }
        }));
        // Status filter chips
        LinearLayout filters = card();
        filters.addView(text("ফিল্টার", 15, TEXT, true));
        String[][] fdefs = {{"all", "সব"}, {"pending", "অপেক্ষমাণ"},
            {"in_progress", "চলমান"}, {"completed", "সম্পন্ন"}, {"cancelled", "বাতিল"}};
        for (final String[] f : fdefs) {
            final String key = f[0];
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(4), 0, dp(4));
            TextView lbl = text((orderFilter.equals(key) ? "● " : "○ ") + f[1], 15,
                orderFilter.equals(key) ? PRIMARY : TEXT, orderFilter.equals(key));
            row.addView(lbl);
            row.setBackground(rippleBackground());
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { orderFilter = key; render(); }
            });
            filters.addView(row);
        }
        content.addView(filters);
        if (ordersLoading) {
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
            c.addView(text("এখনো কোনো অর্ডার নেই", 17, TEXT, true));
            TextView t = text("নতুন অর্ডার করলে এখানে দেখা যাবে — স্ট্যাটাস Pending থেকে শুরু হবে, অ্যাডমিন অনুমোদন দিলে চলমান হবে।", 14, TEXT2, false);
            t.setPadding(0, dp(6), 0, 0);
            c.addView(t);
            content.addView(c);
            return;
        }
        for (final OrderStore.Order o : shown) {
            LinearLayout row = card();
            LinearLayout top = new LinearLayout(this);
            top.setOrientation(LinearLayout.HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            TextView nm = text(o.serviceName, 16, TEXT, true);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            top.addView(nm, mp);
            top.addView(orderStatusChip(o));
            row.addView(top);
            row.addView(text(o.packageDetails + "  •  " + o.amount, 14, TEXT, false));
            row.addView(text("কাস্টমার: " + o.userName + "  •  TrxID: " + o.trxId, 13, TEXT2, false));
            if (o.isMonthly()) {
                SubscriptionStore.Subscription sub = SubscriptionStore.fromOrder(
                    o.serviceName, o.packageDetails, o.amount, o.dateMillis, 1);
                row.addView(text("সাবস্ক্রিপশন: " + SubscriptionStore.daysRemainingBn(sub)
                    + "  •  শেষ: " + SubscriptionStore.formatDate(sub.expiryMillis), 13, PRIMARY, true));
            }
            row.setBackground(rippleBackground());
            row.setClickable(true);
            row.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { currentOrder = o; go(S_ORDER_DETAIL, true); }
            });
            content.addView(row);
        }
        content.addView(outlineButton("🔄 অর্ডার রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (demoMode) render(); else loadLiveOrders();
            }
        }));
    }

    private void showOrderDetail() {
        final OrderStore.Order o = currentOrder;
        if (o == null) { go(S_ORDERS, false); return; }
        content.addView(ghostButton("← অর্ডারে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ORDERS, false); }
        }));
        LinearLayout c = card();
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView nm = text(o.serviceName, 20, NAVY, true);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        top.addView(nm, mp);
        top.addView(orderStatusChip(o));
        c.addView(top);
        c.addView(text(o.packageDetails + "  •  " + o.amount, 16, TEXT, false));
        c.addView(text("কাস্টমার: " + o.userName, 14, TEXT, false));
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
        if (o.isMonthly()) {
            SubscriptionStore.Subscription sub = SubscriptionStore.fromOrder(
                o.serviceName, o.packageDetails, o.amount, o.dateMillis, 1);
            LinearLayout sc = card();
            sc.addView(text("সাবস্ক্রিপশন (মাসিক)", 17, TEXT, true));
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
            nc.addView(text("এটি মাসিক সাবস্ক্রিপশন নয় — তাই এখানে দিনের হিসাব দেখানো হয় না। এককালীনের আলাদা ফাংশন পরে আলোচনা করে ঠিক করা হবে।", 13, TEXT, false));
            content.addView(nc);
        }
        final boolean admin = !demoMode && FirebaseAuthHelper.isAdmin();
        if (admin) {
            LinearLayout ac = card();
            ac.addView(text("অ্যাডমিন — স্ট্যাটাস বদলান", 17, TEXT, true));
            if ("pending".equals(o.status)) {
                ac.addView(primaryButton("▶ In Progress করুন", new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "in_progress"); }
                }));
                ac.addView(outlineButton("✕ Cancel করুন", RED, new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "cancelled"); }
                }));
            } else if ("in_progress".equals(o.status)) {
                ac.addView(primaryButton("✓ Completed করুন", new View.OnClickListener() {
                    @Override public void onClick(View v) { changeOrderStatus(o, "completed"); }
                }));
                ac.addView(outlineButton("✕ Cancel করুন", RED, new View.OnClickListener() {
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
                    if (demoMode) {
                        o.adminMessage = msg;
                        notice = "ডেমো: অ্যাডমিন মেসেজ সেভ হয়েছে (লাইভে যায়নি)।";
                        render();
                        return;
                    }
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
            nc.addView(text("স্ট্যাটাস শুধু অ্যাডমিন বদলাতে পারবেন। আপনার কিছু জানার থাকলে লিড চ্যাটে লিখুন।", 13, TEXT2, false));
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
        if (demoMode) {
            o.status = newStatus;
            if ("in_progress".equals(newStatus) && (o.startDate == null || o.startDate.length() == 0))
                o.startDate = "ডেমো";
            if ("completed".equals(newStatus)) o.completionDate = "ডেমো";
            notice = "ডেমো: স্ট্যাটাস " + o.statusBn() + " হয়েছে (লাইভে যায়নি)।";
            render();
            return;
        }
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
        content.addView(ghostButton("← অর্ডারে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ORDERS, false); }
        }));
        content.addView(text("নতুন অর্ডার", 24, NAVY, true));
        final boolean admin = !demoMode && FirebaseAuthHelper.isAdmin();
        if (admin) {
            LinearLayout ruleNote = card();
            ruleNote.setBackground(rounded(AMBER_SOFT, 20));
            ruleNote.addView(text("এই অর্ডার আপনার নিজের অ্যাকাউন্টে তৈরি হবে। অন্য কাস্টমারের অ্যাকাউন্টে অর্ডার/সাবস্ক্রিপশন যোগ করতে Firestore rules আপডেট লাগবে — সেটা আলাদা পেন্ডিং টাস্ক, এখনো করা হয়নি।", 13, TEXT, false));
            content.addView(ruleNote);
        }
        if (selectedPackage == null) {
            // Step 1: pick a package (verified prices only, amount auto-filled).
            content.addView(text("প্যাকেজ বেছে নিন — দাম নিজে থেকে বসবে, বানিয়ে লেখা যাবে না।", 14, TEXT2, false));
            for (final OrderStore.OrderPackage pkg : OrderStore.PACKAGES) {
                LinearLayout c = card();
                c.addView(text(pkg.service, 17, TEXT, true));
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
        // Step 2: payment form (buyer's own payment details, like website).
        final OrderStore.OrderPackage pkg = selectedPackage;
        LinearLayout pc = card();
        pc.addView(text(pkg.service, 18, NAVY, true));
        pc.addView(text(pkg.pkg + "  •  " + pkg.price, 16, PRIMARY, true));
        pc.addView(text("অ্যামাউন্ট নিজে থেকে বসেছে — প্যাকেজের দামই অর্ডারের অ্যামাউন্ট।", 13, TEXT2, false));
        content.addView(pc);
        LinearLayout fc = card();
        // Payment number first, above the payment-method selector and the
        // senderPhone/trxId fields, exactly like the website OrderModal.
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
                if (demoMode || uid == null) {
                    OrderStore.Order demo = new OrderStore.Order(
                        String.valueOf(System.currentTimeMillis()), "demo-user", name,
                        pkg.service, pkg.pkg, pkg.price, method, sender, trx,
                        "pending", "ডেমো তারিখ", System.currentTimeMillis(),
                        pageLink, reqs, pkg.billing);
                    orders.add(0, demo);
                    ordersLoaded = true;
                    deriveMySubscription();
                    currentOrder = demo;
                    selectedPackage = null;
                    notice = "ডেমো অর্ডার তৈরি হয়েছে — স্ট্যাটাস Pending (লাইভে যায়নি)।";
                    go(S_ORDER_DETAIL, true);
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
        content.addView(text("এই ফর্মে আপনার নিজের পেমেন্ট তথ্য (Sender নম্বর + TrxID) নেওয়া হয় — ওয়েবসাইটের অর্ডার ফর্মের মতোই। পেমেন্ট নম্বরটি উপরে দেখানো হয়েছে; এছাড়া AutoMateIQ-এর অন্য কোনো নম্বর/ইমেইল এখানে দেখানো হয় না।", 12, TEXT2, false));
    }

    private void showRechargeForm() {
        content.addView(ghostButton("← টোকেনে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_TOKENS, false); }
        }));
        content.addView(text("টোকেন রিচার্জ — এপিআই ক্রেডিট", 24, NAVY, true));
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
                if (demoMode || uid == null) {
                    notice = "ডেমো রিচার্জ রিকোয়েস্ট তৈরি হয়েছে — স্ট্যাটাস Pending (লাইভে যায়নি)।";
                    go(S_TOKENS, true);
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
        if (demoMode || FirebaseAuthHelper.currentUid() == null) {
            if (toPaused) leadsStore.takeOver(lead);
            else leadsStore.resumeAi(lead);
            renderThreadScreen();
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

    // ---- Screens: Connect / Services / Subscriptions / Tokens / About ----
    private void showConnect() {
        content.addView(text("ফেসবুক কানেক্ট", 24, NAVY, true));
        TextView sub = text("আপনার পেজ কানেক্ট করলে পেজের মেসেজ আর লিড এই অ্যাপেই আসবে।", 14, TEXT2, false);
        sub.setPadding(0, dp(4), 0, dp(12));
        content.addView(sub);

        LinearLayout status = card();
        status.addView(text("পেজ: AutoMateIQ", 17, TEXT, true));
        LinearLayout srow = new LinearLayout(this);
        srow.setOrientation(LinearLayout.HORIZONTAL);
        srow.setPadding(0, dp(8), 0, 0);
        srow.addView(chip("● বট সার্ভারের মাধ্যমে কানেক্টেড", GREEN_SOFT, GREEN));
        status.addView(srow);
        TextView note = text("পেজের টোকেন কখনো অ্যাপের ভিতরে রাখা হয় না, সেটা সার্ভারেই থাকে। অ্যাপ শুধু সার্ভারের মাধ্যমে রিপ্লাই পাঠায়।", 13, TEXT2, false);
        note.setPadding(0, dp(8), 0, 0);
        status.addView(note);
        content.addView(status);

        String[][] steps = {
            {"১", "ফেসবুকে লগইন করুন", "আপনার ফেসবুক অ্যাকাউন্ট দিয়ে ব্রাউজারে লগইন করুন।"},
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
        content.addView(outlineButton("ফেসবুক পেজ খুলুন (ব্রাউজারে)", PRIMARY,
            new View.OnClickListener() {
                @Override public void onClick(View v) { openUrl(LiveDataClient.PAGE_URL); }
            }));
        content.addView(outlineButton("ওয়েবসাইট অ্যাডমিন প্যানেল খুলুন (ব্রাউজারে)", PRIMARY,
            new View.OnClickListener() {
                @Override public void onClick(View v) { openUrl(LiveDataClient.WEBSITE_URL); }
            }));
    }

    private void showServices() {
        content.addView(text("সার্ভিস", 24, NAVY, true));
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
                c.setBackground(rounded(SURFACE, 20));
                c.setElevation(dp(2));
                c.setPadding(dp(14), dp(14), dp(14), dp(14));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                lp.setMargins(k == 0 ? 0 : dp(7), 0, k == 0 ? dp(7) : 0, dp(14));
                c.setLayoutParams(lp);
                TextView icon = text(s.icon, 24, TEXT, false);
                c.addView(icon);
                c.addView(text(s.name, 16, TEXT, true));
                TextView d = text(s.desc, 13, TEXT2, false);
                d.setMaxLines(3);
                d.setPadding(0, dp(4), 0, dp(8));
                c.addView(d);
                c.addView(chip(s.from, SOFT_BLUE, PRIMARY));
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
        content.addView(ghostButton("অ্যাপ সম্পর্কে →", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_ABOUT, true); }
        }));
    }

    private void showServiceDetail() {
        final Service s = currentService;
        if (s == null) { go(S_SERVICES, false); return; }
        content.addView(ghostButton("← সার্ভিসে ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_SERVICES, false); }
        }));
        LinearLayout head = card();
        head.addView(text(s.icon + "  " + s.name, 22, NAVY, true));
        TextView d = text(s.desc, 15, TEXT2, false);
        d.setPadding(0, dp(8), 0, 0);
        head.addView(d);
        head.addView(chip(s.from, SOFT_BLUE, PRIMARY));
        content.addView(head);

        LinearLayout prices = card();
        prices.addView(text("দামের তালিকা", 17, TEXT, true));
        for (String p : s.prices) {
            TextView row = text("•  " + p, 15, TEXT, false);
            row.setPadding(0, dp(6), 0, 0);
            prices.addView(row);
        }
        content.addView(prices);

        LinearLayout ben = card();
        ben.addView(text("যা যা পাবেন", 17, TEXT, true));
        for (String b : s.benefits) {
            TextView row = text("✓  " + b, 15, TEXT, false);
            row.setPadding(0, dp(6), 0, 0);
            ben.addView(row);
        }
        content.addView(ben);

        content.addView(primaryButton("অ্যাপেই অর্ডার করুন", new View.OnClickListener() {
            @Override public void onClick(View v) {
                // v6: ordering happens IN-APP, never via the website.
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
        content.addView(text("সাবস্ক্রিপশন কিনুন", 24, NAVY, true));
        TextView sub = text("শুধু মাসিক প্যাকেজ। এককালীন প্যাকেজ মাসিক নয় — তাই এখানে দিনের হিসাব দেখানো হয় না।", 14, TEXT2, false);
        sub.setPadding(0, dp(4), 0, dp(12));
        content.addView(sub);
        for (final SubscriptionStore.MonthlyPackage p : SubscriptionStore.MONTHLY_PACKAGES) {
            LinearLayout c = card();
            c.addView(text(p.service, 17, TEXT, true));
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
        content.addView(outlineButton("🪙 টোকেন কিনুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_TOKENS, true); }
        }));
        content.addView(outlineButton("⭐ আমার সাবস্ক্রিপশন দেখুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { go(S_MY_SUB, true); }
        }));
    }

    private void showTokens() {
        content.addView(text("টোকেন কিনুন — এপিআই ক্রেডিট", 24, NAVY, true));
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
        rules.addView(text("নিয়ম", 17, TEXT, true));
        TextView r = text("bKash / Nagad পার্সোনাল • মেয়াদ শেষ হয় না • Pay As You Go • অ্যাডমিন অনুমোদনের পর ক্রেডিট যোগ হয় • ৳২০০ থেকে যেকোনো অ্যামাউন্ট নেওয়া যায় • প্রতি-মেসেজ রেট ওয়েবসাইটে দেওয়া নেই, তাই এখানে কোনো রেট লেখা হয়নি", 14, TEXT2, false);
        r.setPadding(0, dp(6), 0, 0);
        rules.addView(r);
        content.addView(rules);
    }

    private void showMySubscription() {
        content.addView(text("আমার সাবস্ক্রিপশন", 24, NAVY, true));
        // v6: subscription is DERIVED FROM REAL ORDERS, never demo by
        // default. Latest monthly, non-cancelled order wins.
        if (!demoMode && ordersLoaded) {
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
        if (!demoMode && signedIn && !ordersLoaded) {
            content.addView(bannerCard("অর্ডার লোড হচ্ছে… রিফ্রেশ চাপলে আবার আনা হবে।", SOFT_BLUE, PRIMARY));
            content.addView(outlineButton("🔄 অর্ডার রিফ্রেশ করুন", PRIMARY, new View.OnClickListener() {
                @Override public void onClick(View v) { loadLiveOrders(); render(); }
            }));
        }
        if (demoMode) {
            content.addView(bannerCard("🧪 ডেমো প্রিভিউ — এটি আসল সাবস্ক্রিপশন নয়", AMBER_SOFT, AMBER));
        }
        if (mySubscription == null) {
            LinearLayout c = card();
            c.addView(text("কোনো মাসিক সাবস্ক্রিপশন নেই", 17, TEXT, true));
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

    private void showAbout() {
        content.addView(ghostButton("← ফিরুন", new View.OnClickListener() {
            @Override public void onClick(View v) { onBackPressed(); }
        }));
        LinearLayout c = card();
        c.addView(text("AutoMateIQ", 24, NAVY, true));
        TextView d = text("AutoMate Your Business — Custom Bots, Seamless Interactions। এই অ্যাপ থেকে আপনার পেজের লিড, মেসেজ, সাবস্ক্রিপশন আর টোকেন — সব মোবাইল থেকে সহজে ম্যানেজ করা যায়।", 15, TEXT2, false);
        d.setPadding(0, dp(8), 0, 0);
        c.addView(d);
        content.addView(c);
        LinearLayout f = card();
        f.addView(text("এই অ্যাপে যা আছে", 17, TEXT, true));
        String[] feats = {"মোবাইল ড্যাশবোর্ড — লিড, হ্যান্ডওভার, বট রিপ্লাই",
            "লিড থ্রেড — অ্যাপ থেকেই রিপ্লাই, Take Over / Resume AI",
            "ফেসবুক কানেক্ট — পেজ কানেক্টের ধাপ",
            "মাসিক সাবস্ক্রিপশন + কত দিন বাকি",
            "এপিআই ক্রেডিট টোকেন রিচার্জ"};
        for (String feat : feats) {
            TextView row = text("✓  " + feat, 14, TEXT, false);
            row.setPadding(0, dp(5), 0, 0);
            f.addView(row);
        }
        content.addView(f);
        content.addView(outlineButton("ফেসবুক পেজ খুলুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { openUrl(LiveDataClient.PAGE_URL); }
        }));
        content.addView(outlineButton("ওয়েবসাইট খুলুন", PRIMARY, new View.OnClickListener() {
            @Override public void onClick(View v) { openUrl(LiveDataClient.WEBSITE_URL); }
        }));
        TextView ver = text("ভার্সন 6.0 • ইন-অ্যাপ Firebase লগইন • ইন-অ্যাপ অর্ডার • লগইন কখনো ওয়েবসাইটে যাবে না", 12, TEXT2, false);
        ver.setGravity(Gravity.CENTER);
        ver.setPadding(0, dp(8), 0, 0);
        content.addView(ver);
    }
}
