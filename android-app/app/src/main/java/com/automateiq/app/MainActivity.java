package com.automateiq.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * AutoMateIQ mobile dashboard app v3.0 — fully native, custom UI, no embedded web content.
 *
 * v3 adds the website dashboard inside the app: Dashboard Home, Messenger
 * Leads, Lead Thread with Reply / Take Over / Resume AI, Facebook Connect,
 * and a Leads collection view. v3 runs on local demo leads (LeadsStore)
 * so the mobile flow is testable; live data plugs in via LiveDataClient
 * (TODO, not claimed as done).
 *
 * Flow (login-first, as requested):
 *   Launch -> Login screen ONLY. No landing page, no hero page, no services
 *   before login. Tapping the login button opens the live website login in
 *   the user's external browser (orders and login stay on the website
 *   system, so data stays in one place), and the app then shows Home.
 *   In-app Firebase Google login is NOT in v1; login is via the website.
 *
 * No phone number and no email address are shown anywhere in this app.
 * A contact request is shown as a transfer to a representative.
 * Prices are only the verified website prices; services with no website
 * price show no price at all, only "ask the representative".
 */
public class MainActivity extends Activity {

    private static final String SITE_URL = "https://auto-mate-iq-muse-ai2.vercel.app";
    private static final String PAGE_URL = "https://facebook.com/automateiq.xyz";

    private static final int COLOR_DARK = 0xFF0B2A6B;
    private static final int COLOR_BLUE = 0xFF2563EB;
    private static final int COLOR_BG = 0xFFF1F5F9;
    private static final int COLOR_TEXT = 0xFF0F172A;
    private static final int COLOR_MUTED = 0xFF64748B;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GREEN = 0xFF15803D;

    private String current = "login";
    private Service currentService = null;
    private LeadsStore store = new LeadsStore();
    private SubscriptionStore subStore = new SubscriptionStore();
    private String leadFilter = "all";
    private LeadsStore.Lead currentLead = null;

    private static class Service {
        String letter;
        String name;
        String desc;
        String start;
        String[] prices;
        String[] benefits;
        boolean noPrice;
        boolean isApi;

        Service(String letter, String name, String desc, String start,
                String[] prices, String[] benefits, boolean noPrice, boolean isApi) {
            this.letter = letter;
            this.name = name;
            this.desc = desc;
            this.start = start;
            this.prices = prices;
            this.benefits = benefits;
            this.noPrice = noPrice;
            this.isApi = isApi;
        }
    }

    private static final Service[] SERVICES = new Service[] {
        new Service("ম", "মেসেঞ্জার চ্যাটবট",
            "ফেসবুক পেজে কাস্টমার মেসেজ করলে ২৪ ঘণ্টা স্বয়ংক্রিয় উত্তর দেবে, মালিক অনলাইনে না থাকলেও।",
            "৳৫০ থেকে শুরু",
            new String[] {
                "ট্রায়াল ৭ দিন — ৳৫০",
                "স্ট্যান্ডার্ড — ৳৭০০/মাস",
                "প্রিমিয়াম — ৳১,০০০/মাস",
                "এককালীন — ৳৫,০০০"
            },
            new String[] {
                "২৪/৭ অটো রিপ্লাই",
                "প্রোডাক্টের ছবি ও ডিটেইলস গ্যালারি আকারে",
                "মেসেজেই প্রোডাক্ট সিলেক্ট করে অর্ডার",
                "আগের কথোপকথন মনে রাখা",
                "বট না পারলে মানুষের কাছে হ্যান্ডওভার",
                "প্রিমিয়ামে ভয়েস মেসেজ সাপোর্ট",
                "এককালীন প্যাকেজে ৳৫০০ ফ্রি ক্রেডিট"
            }, false, false),
        new Service("ই", "ইনস্টাগ্রাম চ্যাটবট",
            "ইনস্টাগ্রাম ডিএম এবং স্টোরি রিপ্লাই অটোমেশন। বিজনেস গ্রোথ এবং এনগেজমেন্ট বৃদ্ধি।",
            "৳৫০ থেকে শুরু",
            new String[] {
                "ট্রায়াল — ৳৫০",
                "স্ট্যান্ডার্ড — ৳৭০০/মাস",
                "প্রিমিয়াম — ৳১,০০০/মাস",
                "এককালীন — ৳৫,০০০"
            },
            new String[] {
                "ইনস্টাগ্রাম ডিএম অটো রিপ্লাই",
                "স্টোরি রিপ্লাই অটোমেশন",
                "২৪/৭ সার্ভিস"
            }, false, false),
        new Service("হো", "হোয়াটসঅ্যাপ বিজনেস বট",
            "হোয়াটসঅ্যাপে কাস্টমারের সাথে স্বয়ংক্রিয় কথোপকথন ও ক্যাটালগ দেখানো।",
            "৳৫০ থেকে শুরু",
            new String[] {
                "ট্রায়াল — ৳৫০",
                "স্ট্যান্ডার্ড সেটআপ — ৳১,৫০০",
                "এককালীন — ৳৫,০০০"
            },
            new String[] {
                "হোয়াটসঅ্যাপে প্রোডাক্ট ক্যাটালগ দেখানো",
                "স্বয়ংক্রিয় উত্তর",
                "নোট: এপিআই চার্জ ৭ ডলার থেকে শুরু হয়, প্রতি-মেসেজ রেট সাইটে দেওয়া নেই"
            }, false, false),
        new Service("ফে", "ফেসবুক অটো পোস্ট",
            "ফেসবুক পেজে স্বয়ংক্রিয়ভাবে পোস্ট প্রকাশ করা।",
            "৳৫০ থেকে শুরু",
            new String[] {
                "ট্রায়াল — ৳৫০",
                "মাসিক — ৳১,০০০",
                "এককালীন — ৳৫,০০০"
            },
            new String[] {
                "স্বয়ংক্রিয় পোস্ট প্রকাশ",
                "নিয়মিত কনটেন্ট"
            }, false, false),
        new Service("ইউ", "ইউটিউব অটো পোস্ট",
            "ইউটিউব চ্যানেলে স্বয়ংক্রিয়ভাবে পোস্ট প্রকাশ করা।",
            "৳৫০ থেকে শুরু",
            new String[] {
                "ট্রায়াল — ৳৫০",
                "মাসিক — ৳১,০০০",
                "এককালীন — ৳৫,০০০"
            },
            new String[] {
                "স্বয়ংক্রিয় পোস্ট প্রকাশ",
                "নিয়মিত কনটেন্ট"
            }, false, false),
        new Service("ক", "ফেসবুক + ইনস্টাগ্রাম কম্বো",
            "ফেসবুক ও ইনস্টাগ্রাম দুই প্ল্যাটফর্মের অটোমেশন একসাথে।",
            "দাম জানতে প্রতিনিধিকে জিজ্ঞাসা করুন",
            new String[] {},
            new String[] {
                "দুই প্ল্যাটফর্মের অটোমেশন একসাথে"
            }, true, false),
        new Service("ভা", "ভাইব কোডিং",
            "আইডিয়া থেকে কাজের প্রোডাক্ট। রেসপন্সিভ ওয়েবসাইট, ড্যাশবোর্ড এবং ওয়ার্কফ্লো ইন্টারফেস তৈরি করা হয়।",
            "দাম জানতে প্রতিনিধিকে জিজ্ঞাসা করুন",
            new String[] {},
            new String[] {
                "রেসপন্সিভ ওয়েবসাইট",
                "ড্যাশবোর্ড তৈরি",
                "ওয়ার্কফ্লো ইন্টারফেস"
            }, true, false),
        new Service("এ", "এপিআই ক্রেডিট ওয়ালেট",
            "এপিআই ব্যবহারের জন্য ক্রেডিট রিচার্জ। ৳২০০ থেকে শুরু, যত টাকার এপিআই চান নিতে পারবেন।",
            "৳২০০ থেকে শুরু",
            new String[] {
                "রিচার্জ — ৳২০০, ৳৫০০, ৳১,০০০, ৳২,৫০০"
            },
            new String[] {
                "পেমেন্ট: বিকাশ / নগদ (পার্সোনাল)",
                "মেয়াদ শেষ হয় না",
                "Pay As You Go",
                "রিচার্জ অ্যাডমিন অনুমোদন করলে যোগ হয়",
                "যত টাকার এপিআই চান নিতে পারবেন, কোনো সমস্যা নেই",
                "প্রতি-মেসেজ রেট সাইটে দেওয়া নেই"
            }, false, true),
        new Service("ফ্রি", "ফ্রি কনসালটেশন",
            "আপনার ব্যবসার অটোমেশন নিয়ে বিনামূল্যে পরামর্শ। মিটিং বুক করে কথা বলুন।",
            "বিনামূল্যে, মিটিং বুক করে",
            new String[] {},
            new String[] {
                "বিনামূল্যে পরামর্শ",
                "মিটিং বুক করে কথা বলা যায়"
            }, true, false)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showLogin();
    }

    @Override
    public void onBackPressed() {
        if ("leadDetail".equals(current)) {
            showLeads();
        } else if ("detail".equals(current) || "api".equals(current)) {
            showHome();
        } else if ("subscriptions".equals(current) || "tokens".equals(current)
                || "mySubscription".equals(current)) {
            showDashboard();
        } else if ("home".equals(current) || "leads".equals(current)
                || "connect".equals(current) || "about".equals(current)) {
            showDashboard();
        } else if ("dashboard".equals(current)) {
            showLogin();
        } else {
            super.onBackPressed();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(COLOR_BG);
        return l;
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(null, bold ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable d = new GradientDrawable();
        d.setColor(COLOR_WHITE);
        d.setCornerRadius(dp(16));
        c.setBackground(d);
        c.setElevation(dp(3));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(16), dp(8), dp(16), dp(8));
        c.setLayoutParams(p);
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        return c;
    }

    private void addSpace(LinearLayout parent, int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(h)));
        parent.addView(v);
    }

    private Button button(String label, int bg, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(COLOR_WHITE);
        b.setTextSize(16);
        GradientDrawable d = new GradientDrawable();
        d.setColor(bg);
        d.setCornerRadius(dp(24));
        b.setBackground(d);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(dp(16), dp(8), dp(16), dp(8));
        b.setLayoutParams(p);
        b.setPadding(dp(16), dp(14), dp(16), dp(14));
        b.setOnClickListener(listener);
        return b;
    }

    private void openBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(i);
    }

    private LinearLayout header(String title) {
        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            new int[] { COLOR_DARK, COLOR_BLUE });
        h.setBackground(d);
        h.setPadding(dp(16), dp(16), dp(16), dp(16));
        TextView t = text("AutoMateIQ", 20, COLOR_WHITE, true);
        h.addView(t);
        TextView sub = text("  •  " + title, 14, COLOR_WHITE, false);
        h.addView(sub);
        return h;
    }

    private LinearLayout bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setBackgroundColor(COLOR_WHITE);
        nav.setPadding(dp(8), dp(8), dp(8), dp(8));
        String[] labels = new String[] { "ড্যাশবোর্ড", "লিডস", "কানেক্ট", "সার্ভিস", "সম্পর্কে" };
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            Button b = new Button(this);
            b.setText(labels[i]);
            b.setTextSize(13);
            b.setTextColor(COLOR_DARK);
            b.setBackgroundColor(Color.TRANSPARENT);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            b.setLayoutParams(p);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (idx == 0) showDashboard();
                    else if (idx == 1) showLeads();
                    else if (idx == 2) showConnect();
                    else if (idx == 3) showHome();
                    else showAbout();
                }
            });
            nav.addView(b);
        }
        return nav;
    }

    private void showFrame(LinearLayout content, String title) {
        LinearLayout root = column();
        root.addView(header(title));
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.addView(content);
        root.addView(scroll);
        root.addView(bottomNav());
        setContentView(root);
    }

    // ---------- LOGIN (first screen, no landing page before it) ----------

    private void showLogin() {
        current = "login";
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(COLOR_BG);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(24), dp(24), dp(24), dp(24));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER);
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[] { COLOR_DARK, COLOR_BLUE });
        d.setCornerRadius(dp(24));
        hero.setBackground(d);
        hero.setPadding(dp(24), dp(32), dp(24), dp(32));

        TextView brand = text("AutoMateIQ", 32, COLOR_WHITE, true);
        brand.setGravity(Gravity.CENTER);
        hero.addView(brand);
        TextView tag = text("AutoMate Your Business", 16, COLOR_WHITE, false);
        tag.setGravity(Gravity.CENTER);
        hero.addView(tag);
        TextView tagBn = text("আপনার ব্যবসার অটোমেশন, এক জায়গায়", 15, COLOR_WHITE, false);
        tagBn.setGravity(Gravity.CENTER);
        hero.addView(tagBn);
        root.addView(hero);

        addSpace(root, 24);
        TextView loginTitle = text("লগইন করুন", 22, COLOR_TEXT, true);
        loginTitle.setGravity(Gravity.CENTER);
        root.addView(loginTitle);
        addSpace(root, 8);

        Button loginBtn = button("গুগল দিয়ে চালিয়ে যান", COLOR_BLUE,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Login happens on the website system in the browser.
                    // After tapping, the app shows Home / services.
                    openBrowser(SITE_URL);
                    showDashboard();
                }
            });
        root.addView(loginBtn);
        addSpace(root, 12);

        TextView note = text(
            "লগইন ওয়েবসাইটের সিস্টেম দিয়ে হবে। বাটনে চাপ দিলে ওয়েবসাইট আপনার ব্রাউজারে খুলবে, লগইন শেষে অ্যাপে ফিরে আসুন।",
            14, COLOR_MUTED, false);
        note.setGravity(Gravity.CENTER);
        root.addView(note);
        addSpace(root, 12);
        TextView note2 = text(
            "এই অ্যাপে আলাদা লগইন নেই — লগইন ও অর্ডার ওয়েবসাইটে হয়, তাই আপনার তথ্য এক জায়গায় থাকে।",
            13, COLOR_MUTED, false);
        note2.setGravity(Gravity.CENTER);
        root.addView(note2);

        setContentView(root);
    }

    // ---------- HOME ----------

    private void showHome() {
        current = "home";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout hero = card();
        TextView h1 = text("AutoMateIQ", 26, COLOR_DARK, true);
        hero.addView(h1);
        TextView h2 = text("সার্ভিস বেছে নিন, দাম দেখুন, ওয়েবসাইট থেকে অর্ডার করুন।",
            15, COLOR_MUTED, false);
        hero.addView(h2);
        content.addView(hero);

        TextView sec = text("আমাদের সার্ভিস", 18, COLOR_TEXT, true);
        sec.setPadding(dp(16), dp(8), dp(16), dp(4));
        content.addView(sec);

        for (int i = 0; i < SERVICES.length; i++) {
            final Service s = SERVICES[i];
            LinearLayout c = card();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView badge = text(s.letter, 20, COLOR_WHITE, true);
            badge.setGravity(Gravity.CENTER);
            GradientDrawable bd = new GradientDrawable();
            bd.setColor(COLOR_BLUE);
            bd.setCornerRadius(dp(24));
            badge.setBackground(bd);
            badge.setPadding(dp(12), dp(12), dp(12), dp(12));
            row.addView(badge);
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(12), 0, 0, 0);
            info.addView(text(s.name, 17, COLOR_TEXT, true));
            info.addView(text(s.desc, 13, COLOR_MUTED, false));
            info.addView(text(s.start, 14, COLOR_BLUE, true));
            row.addView(info);
            c.addView(row);
            c.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (s.isApi) showApi();
                    else showDetail(s);
                }
            });
            content.addView(c);
        }

        showFrame(content, "হোম");
    }

    // ---------- SERVICE DETAIL ----------

    private void showDetail(final Service s) {
        current = "detail";
        currentService = s;
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout c = card();
        c.addView(text(s.name, 22, COLOR_DARK, true));
        c.addView(text(s.desc, 15, COLOR_MUTED, false));
        content.addView(c);

        if (s.noPrice) {
            LinearLayout n = card();
            n.addView(text("দাম", 17, COLOR_TEXT, true));
            n.addView(text("এই সার্ভিসের দাম ওয়েবসাইটে দেওয়া নেই। দাম জানতে প্রতিনিধিকে জিজ্ঞাসা করুন — প্রতিনিধি বিস্তারিত বলবেন।",
                15, COLOR_TEXT, false));
            content.addView(n);
        } else {
            LinearLayout p = card();
            p.addView(text("দামের তালিকা", 17, COLOR_TEXT, true));
            for (int i = 0; i < s.prices.length; i++) {
                p.addView(text("•  " + s.prices[i], 15, COLOR_TEXT, false));
            }
            content.addView(p);
        }

        LinearLayout b = card();
        b.addView(text("যা যা পাবেন", 17, COLOR_TEXT, true));
        for (int i = 0; i < s.benefits.length; i++) {
            b.addView(text("•  " + s.benefits[i], 15, COLOR_TEXT, false));
        }
        content.addView(b);

        content.addView(button("ওয়েবসাইট থেকে অর্ডার করুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(SITE_URL);
                }
            }));
        content.addView(button("মেসেঞ্জারে জিজ্ঞাসা করুন", COLOR_GREEN,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(PAGE_URL);
                }
            }));
        if (s.noPrice) {
            TextView t = text("দাম জানতে চাইলে প্রতিনিধির কাছে ট্রান্সফার করা হবে, তিনি বিস্তারিত বলবেন।",
                13, COLOR_MUTED, false);
            t.setPadding(dp(16), dp(8), dp(16), dp(8));
            content.addView(t);
        }

        showFrame(content, "সার্ভিস");
    }

    // ---------- API CREDIT ----------

    private void showApi() {
        current = "api";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout c = card();
        c.addView(text("এপিআই ক্রেডিট ওয়ালেট", 22, COLOR_DARK, true));
        c.addView(text("এপিআই ব্যবহারের জন্য ক্রেডিট রিচার্জ। ৳২০০ থেকে শুরু।",
            15, COLOR_MUTED, false));
        content.addView(c);

        LinearLayout amounts = card();
        amounts.addView(text("রিচার্জ অ্যামাউন্ট", 17, COLOR_TEXT, true));
        amounts.addView(text("৳২০০  •  ৳৫০০  •  ৳১,০০০  •  ৳২,৫০০", 18, COLOR_BLUE, true));
        amounts.addView(text("যত টাকার এপিআই চান নিতে পারবেন, কোনো সমস্যা নেই।",
            15, COLOR_TEXT, false));
        content.addView(amounts);

        LinearLayout info = card();
        info.addView(text("নিয়ম", 17, COLOR_TEXT, true));
        info.addView(text("•  পেমেন্ট: বিকাশ / নগদ (পার্সোনাল)", 15, COLOR_TEXT, false));
        info.addView(text("•  মেয়াদ শেষ হয় না", 15, COLOR_TEXT, false));
        info.addView(text("•  Pay As You Go", 15, COLOR_TEXT, false));
        info.addView(text("•  রিচার্জ অ্যাডমিন অনুমোদন করলে যোগ হয়", 15, COLOR_TEXT, false));
        info.addView(text("•  প্রতি-মেসেজ রেট সাইটে দেওয়া নেই, তাই এখানে কোনো রেট দেওয়া হয়নি",
            15, COLOR_TEXT, false));
        content.addView(info);

        content.addView(button("ওয়েবসাইট থেকে রিচার্জ করুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(SITE_URL);
                }
            }));

        showFrame(content, "এপিআই ক্রেডিট");
    }

    // ---------- ABOUT ----------

    private void showAbout() {
        current = "about";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout c = card();
        c.addView(text("AutoMateIQ সম্পর্কে", 22, COLOR_DARK, true));
        c.addView(text("AutoMateIQ আপনার ব্যবসার জন্য কাস্টম বট ও অটোমেশন সার্ভিস দেয় — মেসেঞ্জার, ইনস্টাগ্রাম, হোয়াটসঅ্যাপ চ্যাটবট, ফেসবুক ও ইউটিউব অটো পোস্ট, এপিআই ক্রেডিট ওয়ালেট এবং ভাইব কোডিং।",
            15, COLOR_TEXT, false));
        content.addView(c);

        LinearLayout w = card();
        w.addView(text("ওয়েবসাইটের সাথে যুক্ত", 17, COLOR_TEXT, true));
        w.addView(text("এই অ্যাপ ওয়েবসাইটের সিস্টেমের সাথে যুক্ত। লগইন ও অর্ডার ওয়েবসাইটে হয়, তাই আপনার তথ্য এক জায়গায় থাকে, আলাদা ডাটাবেসে যায় না।",
            15, COLOR_TEXT, false));
        content.addView(w);

        LinearLayout f = card();
        f.addView(text("ফ্রি কনসালটেশন", 17, COLOR_TEXT, true));
        f.addView(text("বিনামূল্যে পরামর্শের জন্য মিটিং বুক করে কথা বলুন। এর কোনো দাম নেই।",
            15, COLOR_TEXT, false));
        content.addView(f);

        content.addView(button("ওয়েবসাইট খুলুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(SITE_URL);
                }
            }));
        content.addView(button("মেসেঞ্জারে জিজ্ঞাসা করুন", COLOR_GREEN,
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openBrowser(PAGE_URL);
                }
            }));

        showFrame(content, "সম্পর্কে");
    }

    // ---------- DASHBOARD HOME (after login) ----------

    private void showDashboard() {
        current = "dashboard";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout demo = card();
        demo.addView(text("ডেমো ডাটা দেখছেন — আসল কাস্টমার নয়", 15, COLOR_BLUE, true));
        demo.addView(text("লাইভ ডাটা যুক্ত হলে এখানেই ওয়েবসাইটের আসল লিড আসবে।",
            13, COLOR_MUTED, false));
        content.addView(demo);

        LinearLayout hero = card();
        hero.addView(text("ড্যাশবোর্ড", 24, COLOR_DARK, true));
        hero.addView(text("মোবাইল থেকে মেসেঞ্জার লিড দেখুন, রিপ্লাই দিন, হ্যান্ডওভার করুন।",
            15, COLOR_MUTED, false));
        content.addView(hero);

        String[][] stats = new String[][] {
            { "মোট লিড", String.valueOf(store.total()) },
            { "হ্যান্ডওভার", String.valueOf(store.handoverCount()) },
            { "বট রিপ্লাই", String.valueOf(store.botRepliedCount()) },
            { "নতুন আজ", String.valueOf(store.newCount()) }
        };
        for (int i = 0; i < stats.length; i++) {
            LinearLayout c = card();
            c.addView(text(stats[i][0], 14, COLOR_MUTED, false));
            c.addView(text(stats[i][1], 26, COLOR_DARK, true));
            content.addView(c);
        }

        content.addView(button("লিডস দেখুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showLeads(); }
            }));
        content.addView(button("ফেসবুক কানেক্ট", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showConnect(); }
            }));
        content.addView(button("সার্ভিস দেখুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showHome(); }
            }));
        content.addView(button("সাবস্ক্রিপশন কিনুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showSubscriptions(); }
            }));
        content.addView(button("টোকেন কিনুন (এপিআই ক্রেডিট)", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showTokens(); }
            }));
        content.addView(button("আমার সাবস্ক্রিপশন দেখুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showMySubscription(); }
            }));

        SubscriptionStore.Subscription demoSub = subStore.demoSubscription();
        LinearLayout subCard = card();
        subCard.addView(text("আমার সাবস্ক্রিপশন (ডেমো)", 17, COLOR_TEXT, true));
        subCard.addView(text(demoSub.service + " — " + demoSub.pkg, 16, COLOR_DARK, true));
        subCard.addView(text("দাম: " + demoSub.price + "  •  স্ট্যাটাস: " + demoSub.statusBn(), 14, COLOR_TEXT, false));
        subCard.addView(text("শুরু: " + SubscriptionStore.formatDate(demoSub.startMillis)
            + "  •  শেষ: " + SubscriptionStore.formatDate(demoSub.expiryMillis), 14, COLOR_TEXT, false));
        subCard.addView(text(SubscriptionStore.daysRemainingBn(demoSub), 18, COLOR_BLUE, true));
        subCard.addView(text("ডেমো সাবস্ক্রিপশন — আসল কাস্টমারের নয়।", 13, COLOR_BLUE, true));
        content.addView(subCard);

        LinearLayout live = card();
        live.addView(text("লাইভ ডাটা", 17, COLOR_TEXT, true));
        live.addView(text(LiveDataClient.liveDataExplanation(), 14, COLOR_TEXT, false));
        content.addView(live);

        showFrame(content, "ড্যাশবোর্ড");
    }

    // ---------- LEADS LIST + COLLECTION ----------

    private String statusLabel(LeadsStore.Lead l) {
        if (l.paused) return "হ্যান্ডওভার";
        if ("bot_replied".equals(l.status)) return "বট রিপ্লাই";
        if ("new".equals(l.status)) return "নতুন";
        return l.status;
    }

    private void showLeads() {
        current = "leads";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout demo = card();
        demo.addView(text("ডেমো লিড — আসল কাস্টমার নয়", 15, COLOR_BLUE, true));
        content.addView(demo);

        LinearLayout filters = card();
        filters.addView(text("ফিল্টার: " + ("all".equals(leadFilter) ? "সব" : "handover".equals(leadFilter) ? "হ্যান্ডওভার" : "বট রিপ্লাই"),
            15, COLOR_TEXT, true));
        filters.addView(text("বাটনে চাপ দিয়ে ফিল্টার বদলান:", 13, COLOR_MUTED, false));
        content.addView(filters);
        content.addView(button("সব লিড", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { leadFilter = "all"; showLeads(); }
            }));
        content.addView(button("শুধু হ্যান্ডওভার", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { leadFilter = "handover"; showLeads(); }
            }));
        content.addView(button("শুধু বট রিপ্লাই", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { leadFilter = "bot_replied"; showLeads(); }
            }));

        java.util.List<LeadsStore.Lead> list = store.filtered(leadFilter);
        if (list.isEmpty()) {
            LinearLayout e = card();
            e.addView(text("এই ফিল্টারে কোনো লিড নেই।", 15, COLOR_MUTED, false));
            content.addView(e);
        }
        for (int i = 0; i < list.size(); i++) {
            final LeadsStore.Lead l = list.get(i);
            LinearLayout c = card();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView badge = text(l.initial(), 20, COLOR_WHITE, true);
            badge.setGravity(Gravity.CENTER);
            GradientDrawable bd = new GradientDrawable();
            bd.setColor(l.paused ? 0xFFB45309 : COLOR_BLUE);
            bd.setCornerRadius(dp(24));
            badge.setBackground(bd);
            badge.setPadding(dp(12), dp(12), dp(12), dp(12));
            row.addView(badge);
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setPadding(dp(12), 0, 0, 0);
            info.addView(text(l.name, 17, COLOR_TEXT, true));
            info.addView(text("পেজ: automateiq  •  " + statusLabel(l)
                + (l.paused ? "  •  হ্যান্ডওভার ব্যাজ" : ""), 13, COLOR_MUTED, false));
            info.addView(text(l.lastMessage, 14, COLOR_TEXT, false));
            info.addView(text(l.updatedAt, 12, COLOR_MUTED, false));
            row.addView(info);
            c.addView(row);
            c.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { showLeadDetail(l); }
            });
            content.addView(c);
        }

        content.addView(button("সব লিডের সারাংশ কপি করুন (কালেকশন)", COLOR_GREEN,
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    copyToClipboard(store.copyAllLeadsSummary());
                    Toast.makeText(MainActivity.this, "লিড সারাংশ কপি হয়েছে", Toast.LENGTH_SHORT).show();
                }
            }));

        showFrame(content, "লিডস");
    }

    private void copyToClipboard(String s) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("AutoMateIQ lead", s));
    }

    // ---------- LEAD DETAIL / THREAD ----------

    private void showLeadDetail(final LeadsStore.Lead lead) {
        current = "leadDetail";
        currentLead = lead;
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout head = card();
        head.addView(text(lead.name, 22, COLOR_DARK, true));
        head.addView(text("পেজ: automateiq  •  স্ট্যাটাস: " + statusLabel(lead)
            + (lead.paused ? "  •  বট পজ করা আছে" : ""), 14, COLOR_MUTED, false));
        head.addView(text("ডেমো লিড — আসল কাস্টমার নয়", 13, COLOR_BLUE, true));
        content.addView(head);

        for (int i = 0; i < lead.messages.size(); i++) {
            LeadsStore.Message m = lead.messages.get(i);
            String who;
            if ("user".equals(m.role)) who = "কাস্টমার";
            else if ("bot".equals(m.role)) who = "বট";
            else if ("owner".equals(m.role)) who = "আপনি (ওনার)";
            else who = "কাস্টমার";
            LinearLayout c = card();
            c.addView(text(who + "  •  " + m.createdAt, 13, COLOR_MUTED, true));
            c.addView(text(m.text, 15, COLOR_TEXT, false));
            content.addView(c);
        }

        LinearLayout replyBox = card();
        replyBox.addView(text("রিপ্লাই লিখুন", 17, COLOR_TEXT, true));
        final EditText input = new EditText(this);
        input.setHint("মেসেজ লিখুন...");
        input.setTextSize(15);
        replyBox.addView(input);
        content.addView(replyBox);
        content.addView(button("রিপ্লাই পাঠান", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    String t = input.getText().toString().trim();
                    if (t.length() == 0) {
                        Toast.makeText(MainActivity.this, "আগে মেসেজ লিখুন", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.appendOwnerReply(lead, t);
                    Toast.makeText(MainActivity.this, "রিপ্লাই থ্রেডে যোগ হয়েছে (ডেমো ডাটা)", Toast.LENGTH_SHORT).show();
                    showLeadDetail(lead);
                }
            }));
        content.addView(button("Take Over (বট বন্ধ করুন)", 0xFFB45309,
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    store.takeOver(lead);
                    Toast.makeText(MainActivity.this, "Take Over হয়েছে — বট এই লিডে আর উত্তর দেবে না", Toast.LENGTH_SHORT).show();
                    showLeadDetail(lead);
                }
            }));
        content.addView(button("Resume AI (বট আবার চালু করুন)", COLOR_GREEN,
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    store.resumeAi(lead);
                    Toast.makeText(MainActivity.this, "Resume AI হয়েছে — বট আবার উত্তর দেবে", Toast.LENGTH_SHORT).show();
                    showLeadDetail(lead);
                }
            }));
        content.addView(button("লিড ডিটেইলস কপি করুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) {
                    copyToClipboard(store.copyLeadDetails(lead));
                    Toast.makeText(MainActivity.this, "লিড ডিটেইলস কপি হয়েছে", Toast.LENGTH_SHORT).show();
                }
            }));

        showFrame(content, "লিড");
    }

    // ---------- FACEBOOK CONNECT ----------

    private void showConnect() {
        current = "connect";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout c = card();
        c.addView(text("ফেসবুক কানেক্ট", 22, COLOR_DARK, true));
        c.addView(text("অ্যাপ থেকে ফেসবুক পেজ কানেক্ট করার ধাপ:", 15, COLOR_TEXT, false));
        content.addView(c);

        LinearLayout steps = card();
        steps.addView(text("১. ব্রাউজারে ফেসবুকে লগইন করুন", 15, COLOR_TEXT, false));
        steps.addView(text("২. আপনার পেজটি বেছে নিন", 15, COLOR_TEXT, false));
        steps.addView(text("৩. মেসেঞ্জার পারমিশন দিন (পেজ মেসেজ পড়া ও পাঠানো)", 15, COLOR_TEXT, false));
        steps.addView(text("৪. কানেক্ট শেষ হলে অ্যাপের ড্যাশবোর্ডে লিড আসবে", 15, COLOR_TEXT, false));
        content.addView(steps);

        LinearLayout safe = card();
        safe.addView(text("নিরাপত্তা — গুরুত্বপূর্ণ", 17, COLOR_TEXT, true));
        safe.addView(text("পেজের টোকেন অ্যাপের ভিতরে রাখা হয় না এবং অ্যাপে দেখানোও হয় না। "
            + "টোকেন সার্ভারে (Render বট সার্ভারে) থাকে, অ্যাপ শুধু সার্ভারের মাধ্যমে মেসেজ দেখে ও পাঠায়। "
            + "এই ভার্সনে অ্যাপের ভিতরে সরাসরি ফেসবুক লগইন + টোকেন নেওয়া নেই — "
            + "সেটা করতে Meta-র App Review ও Business Verification লাগে, তাই সেটা পরের ধাপ।",
            14, COLOR_TEXT, false));
        content.addView(safe);

        content.addView(button("ফেসবুক পেজ খুলুন (ব্রাউজারে)", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { openBrowser(PAGE_URL); }
            }));
        content.addView(button("ওয়েবসাইট অ্যাডমিন প্যানেল খুলুন (ব্রাউজারে)", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { openBrowser(SITE_URL); }
            }));

        LinearLayout live = card();
        live.addView(text("লাইভ ডাটা", 17, COLOR_TEXT, true));
        live.addView(text(LiveDataClient.liveDataExplanation(), 14, COLOR_TEXT, false));
        content.addView(live);

        showFrame(content, "কানেক্ট");
    }

    // ---------- SUBSCRIPTION PURCHASE (monthly packages only) ----------

    private void showSubscriptions() {
        current = "subscriptions";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout head = card();
        head.addView(text("সাবস্ক্রিপশন কিনুন", 22, COLOR_DARK, true));
        head.addView(text("শুধু মাসিক প্যাকেজ। প্রতিটি প্যাকেজের মেয়াদ ১ মাস। "
            + "ট্রায়াল ও এককালীন প্যাকেজ এখানে সাবস্ক্রিপশন হিসেবে দেখানো হয় না।",
            14, COLOR_MUTED, false));
        content.addView(head);

        for (int i = 0; i < SubscriptionStore.MONTHLY_PACKAGES.length; i++) {
            final SubscriptionStore.MonthlyPackage mp = SubscriptionStore.MONTHLY_PACKAGES[i];
            LinearLayout c = card();
            c.addView(text(mp.service + " — " + mp.pkg, 17, COLOR_TEXT, true));
            c.addView(text("দাম: " + mp.price, 15, COLOR_BLUE, true));
            c.addView(text("মেয়াদ: ১ মাস", 14, COLOR_TEXT, false));
            content.addView(c);
            content.addView(button("কিনুন / ওয়েবসাইট থেকে অর্ডার করুন — " + mp.service + " " + mp.pkg, COLOR_BLUE,
                new View.OnClickListener() {
                    @Override public void onClick(View v) { openBrowser(SITE_URL); }
                }));
        }

        LinearLayout note = card();
        note.addView(text("অর্ডার ওয়েবসাইটের সিস্টেম দিয়ে হবে, তাই আপনার তথ্য এক জায়গায় থাকে।",
            14, COLOR_MUTED, false));
        content.addView(note);

        showFrame(content, "সাবস্ক্রিপশন");
    }

    // ---------- TOKEN PURCHASE (API Credit Wallet) ----------

    private void showTokens() {
        current = "tokens";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        LinearLayout head = card();
        head.addView(text("টোকেন কিনুন — এপিআই ক্রেডিট", 22, COLOR_DARK, true));
        head.addView(text("৳২০০ থেকে শুরু। যত টাকার এপিআই চান নিতে পারবেন।",
            15, COLOR_MUTED, false));
        content.addView(head);

        for (int i = 0; i < SubscriptionStore.TOKEN_AMOUNTS.length; i++) {
            final String amt = SubscriptionStore.TOKEN_AMOUNTS[i];
            LinearLayout c = card();
            c.addView(text("রিচার্জ: " + amt, 18, COLOR_BLUE, true));
            content.addView(c);
            content.addView(button("কিনুন / ওয়েবসাইট থেকে রিচার্জ করুন — " + amt, COLOR_BLUE,
                new View.OnClickListener() {
                    @Override public void onClick(View v) { openBrowser(SITE_URL); }
                }));
        }

        LinearLayout rules = card();
        rules.addView(text("নিয়ম", 17, COLOR_TEXT, true));
        rules.addView(text("•  পেমেন্ট: বিকাশ / নগদ (পার্সোনাল)", 15, COLOR_TEXT, false));
        rules.addView(text("•  মেয়াদ শেষ হয় না", 15, COLOR_TEXT, false));
        rules.addView(text("•  Pay As You Go", 15, COLOR_TEXT, false));
        rules.addView(text("•  রিচার্জ অ্যাডমিন অনুমোদন করলে যোগ হয়", 15, COLOR_TEXT, false));
        rules.addView(text("•  সর্বনিম্ন ৳২০০, এর বেশি যেকোনো অ্যামাউন্ট নেওয়া যায়", 15, COLOR_TEXT, false));
        rules.addView(text("•  প্রতি-মেসেজ রেট সাইটে দেওয়া নেই, তাই এখানে কোনো রেট দেওয়া হয়নি",
            15, COLOR_TEXT, false));
        content.addView(rules);

        showFrame(content, "টোকেন");
    }

    // ---------- MY SUBSCRIPTION STATUS ----------

    private void showMySubscription() {
        current = "mySubscription";
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(COLOR_BG);

        SubscriptionStore.Subscription sub = subStore.demoSubscription();
        LinearLayout head = card();
        head.addView(text("আমার সাবস্ক্রিপশন", 22, COLOR_DARK, true));
        head.addView(text("ডেমো সাবস্ক্রিপশন — আসল কাস্টমারের নয়।", 14, COLOR_BLUE, true));
        content.addView(head);

        LinearLayout c = card();
        c.addView(text("সার্ভিস: " + sub.service, 17, COLOR_TEXT, true));
        c.addView(text("প্যাকেজ: " + sub.pkg, 17, COLOR_TEXT, true));
        c.addView(text("দাম: " + sub.price, 15, COLOR_BLUE, true));
        c.addView(text("শুরুর তারিখ: " + SubscriptionStore.formatDate(sub.startMillis), 15, COLOR_TEXT, false));
        c.addView(text("শেষের তারিখ: " + SubscriptionStore.formatDate(sub.expiryMillis), 15, COLOR_TEXT, false));
        c.addView(text("স্ট্যাটাস: " + sub.statusBn(), 15, COLOR_TEXT, false));
        c.addView(text(SubscriptionStore.daysRemainingBn(sub), 20, COLOR_BLUE, true));
        content.addView(c);

        LinearLayout live = card();
        live.addView(text("লাইভ স্ট্যাটাস", 17, COLOR_TEXT, true));
        live.addView(text("লাইভ সাবস্ক্রিপশন স্ট্যাটাস এখনো যুক্ত হয়নি। "
            + "লাইভ হলে ওয়েবসাইটের অর্ডার থেকে আসবে — অর্ডারের তারিখ + মাসিক মেয়াদ (১ মাস) "
            + "দিয়ে শুরুর তারিখ, শেষের তারিখ ও দিন বাকি হিসাব করে এখানে দেখানো হবে। "
            + "এককালীন প্যাকেজের জন্য দিন বাকি দেখানো হবে না — সেটা পরে আলাদাভাবে আলোচনা করে ঠিক করা হবে।",
            14, COLOR_TEXT, false));
        content.addView(live);

        content.addView(button("সাবস্ক্রিপশন কিনুন", COLOR_BLUE,
            new View.OnClickListener() {
                @Override public void onClick(View v) { showSubscriptions(); }
            }));

        showFrame(content, "সাবস্ক্রিপশন");
    }
}
