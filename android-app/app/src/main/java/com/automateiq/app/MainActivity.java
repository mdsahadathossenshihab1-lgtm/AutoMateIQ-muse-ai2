package com.automateiq.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * AutoMateIQ native app v2.0 — fully native, custom UI, no embedded web content.
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
        if ("detail".equals(current) || "api".equals(current) || "about".equals(current)) {
            showHome();
        } else if ("home".equals(current)) {
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
        String[] labels = new String[] { "হোম", "এপিআই ক্রেডিট", "সম্পর্কে", "লগআউট" };
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
                    if (idx == 0) showHome();
                    else if (idx == 1) showApi();
                    else if (idx == 2) showAbout();
                    else showLogin();
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
                    showHome();
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
}
