package com.automateiq.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Subscription model for the v4 dashboard app.
 *
 * A subscription here means ONLY a monthly package. One-time packages
 * are NOT subscriptions: they are never listed on the subscription
 * purchase screen and they never get a days-remaining display.
 * One-time handling is a separate future discussion, explicitly
 * deferred by the user — nothing one-time-specific is built here.
 *
 * v4 CHANGE vs v3: live subscriptions come from app orders —
 * fromOrder(orderDateMillis, months) computes expiry = order date +
 * months (monthly = 1 month).
 * no sample content is shown, and it is never the default content.
 */
public class SubscriptionStore {

    public static class Subscription {
        public String service;
        public String pkg;
        public String price;
        public long startMillis;
        public long expiryMillis;

        public Subscription(String service, String pkg, String price,
                            long startMillis, long expiryMillis) {
            this.service = service;
            this.pkg = pkg;
            this.price = price;
            this.startMillis = startMillis;
            this.expiryMillis = expiryMillis;
        }

        public boolean isActive() {
            return System.currentTimeMillis() <= expiryMillis;
        }

        public String statusBn() {
            return isActive() ? "চালু আছে" : "মেয়াদ শেষ";
        }

        /** Whole days remaining; 0 if already expired. */
        public long daysRemaining() {
            long diff = expiryMillis - System.currentTimeMillis();
            if (diff <= 0) return 0;
            return diff / (24L * 60L * 60L * 1000L);
        }

        /** 0-100 progress: how much of the subscription period has passed. */
        public int progressPercent() {
            long total = expiryMillis - startMillis;
            if (total <= 0) return 100;
            long elapsed = System.currentTimeMillis() - startMillis;
            if (elapsed <= 0) return 0;
            if (elapsed >= total) return 100;
            return (int) (elapsed * 100 / total);
        }
    }

    /** Live subscription from a website order: expiry = order date + months. */
    public static Subscription fromOrder(String service, String pkg, String price,
                                         long orderMillis, int months) {
        Calendar expiry = Calendar.getInstance();
        expiry.setTimeInMillis(orderMillis);
        expiry.add(Calendar.MONTH, months);
        return new Subscription(service, pkg, price, orderMillis, expiry.getTimeInMillis());
    }

    public static class MonthlyPackage {
        public String service;
        public String pkg;
        public String price;
        public MonthlyPackage(String service, String pkg, String price) {
            this.service = service;
            this.pkg = pkg;
            this.price = price;
        }
    }

    /** ONLY verified monthly packages from the website. No Trial, no One-time. */
    public static final MonthlyPackage[] MONTHLY_PACKAGES = new MonthlyPackage[] {
        new MonthlyPackage("মেসেঞ্জার চ্যাটবট", "স্ট্যান্ডার্ড", "৳৭০০/মাস"),
        new MonthlyPackage("ইনস্টাগ্রাম চ্যাটবট", "স্ট্যান্ডার্ড", "৳৭০০/মাস"),
        new MonthlyPackage("মেসেঞ্জার চ্যাটবট", "প্রিমিয়াম", "৳১,০০০/মাস"),
        new MonthlyPackage("ইনস্টাগ্রাম চ্যাটবট", "প্রিমিয়াম", "৳১,০০০/মাস"),
        new MonthlyPackage("ফেসবুক অটো পোস্ট", "মাসিক", "৳১,০০০/মাস"),
        new MonthlyPackage("ইউটিউব অটো পোস্ট", "মাসিক", "৳১,০০০/মাস")
    };

    /** API Credit token recharge amounts (verified, no per-message rate stated). */
    public static final String[] TOKEN_AMOUNTS = new String[] {
        "৳২০০", "৳৫০০", "৳১,০০০", "৳২,৫০০"
    };

    

    public static String toBengaliDigits(String s) {
        if (s == null) return "";
        char[] out = s.toCharArray();
        for (int i = 0; i < out.length; i++) {
            if (out[i] >= '0' && out[i] <= '9') out[i] = (char) ('০' + (out[i] - '0'));
        }
        return new String(out);
    }

    public static String formatDate(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        return toBengaliDigits(f.format(new Date(millis)));
    }

    public static String daysRemainingBn(Subscription sub) {
        return toBengaliDigits(String.valueOf(sub.daysRemaining())) + " দিন বাকি";
    }
}
