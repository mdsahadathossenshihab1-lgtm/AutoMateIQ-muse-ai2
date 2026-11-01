package com.automateiq.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Order model for the v6 app — SAME shape as the website Firestore
 * 'orders' collection (see website types.ts Order):
 * id, userId, userName, serviceName, packageDetails, amount,
 * paymentMethod, senderPhone, trxId, status, date,
 * clientPageLink, clientRequirements, adminMessage,
 * startDate, completionDate.
 *
 * Status flow (website rules, new flow only — 'approved'/'rejected'
 * are legacy and are never created by this app):
 * pending -> in_progress -> completed / cancelled.
 *
 * Billing type is derived from the package the customer picked:
 * "monthly" = a monthly subscription package (gets days-remaining),
 * "one_time" / "trial" / "setup" = NOT a subscription (never gets
 * days-remaining; one-time handling is a separate future discussion
 * the user explicitly deferred — nothing one-time-specific is built).
 *
 * PRIVACY: the form collects the BUYER's payment details (their
 * senderPhone + trxId), exactly like the website order form. It must
 * NEVER display AutoMateIQ's own phone/email/contact anywhere.
 */
public class OrderStore {

    public static class Order {
        public String id;
        public String userId;
        public String userName;
        public String serviceName;
        public String packageDetails;
        public String amount;
        public String paymentMethod;
        public String senderPhone;
        public String trxId;
        public String status; // pending | in_progress | completed | cancelled
        public String date; // ISO string, as on the website
        public long dateMillis;
        public String clientPageLink;
        public String clientRequirements;
        public String adminMessage;
        public String startDate;
        public String completionDate;
        /** "monthly" | "one_time" | "trial" | "setup" — app-side only. */
        public String billing;

        public Order(String id, String userId, String userName,
                     String serviceName, String packageDetails, String amount,
                     String paymentMethod, String senderPhone, String trxId,
                     String status, String date, long dateMillis,
                     String clientPageLink, String clientRequirements,
                     String billing) {
            this.id = id;
            this.userId = userId;
            this.userName = userName;
            this.serviceName = serviceName;
            this.packageDetails = packageDetails;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.senderPhone = senderPhone;
            this.trxId = trxId;
            this.status = status;
            this.date = date;
            this.dateMillis = dateMillis;
            this.clientPageLink = clientPageLink == null ? "" : clientPageLink;
            this.clientRequirements = clientRequirements == null ? "" : clientRequirements;
            this.adminMessage = "";
            this.startDate = "";
            this.completionDate = "";
            this.billing = billing == null ? "one_time" : billing;
        }

        public boolean isMonthly() { return "monthly".equals(billing); }

        public String statusBn() {
            if ("in_progress".equals(status)) return "চলমান";
            if ("completed".equals(status)) return "সম্পন্ন";
            if ("cancelled".equals(status)) return "বাতিল";
            if ("approved".equals(status)) return "অনুমোদিত (পুরনো)";
            if ("rejected".equals(status)) return "বাতিল (পুরনো)";
            return "অপেক্ষমাণ";
        }
    }

    /** A purchasable package in the in-app order form. Prices verified. */
    public static class OrderPackage {
        public String service;
        public String pkg;
        public String price; // e.g. "৳৭০০" — becomes the order amount
        public String billing; // monthly | one_time | trial | setup
        public OrderPackage(String service, String pkg, String price, String billing) {
            this.service = service; this.pkg = pkg;
            this.price = price; this.billing = billing;
        }
        public String label() { return service + " — " + pkg + " (" + price + ")"; }
    }

    /** Monthly packages first (these create subscriptions), then the rest. */
    public static final OrderPackage[] PACKAGES = new OrderPackage[] {
        new OrderPackage("মেসেঞ্জার চ্যাটবট", "স্ট্যান্ডার্ড (মাসিক)", "৳৭০০", "monthly"),
        new OrderPackage("ইনস্টাগ্রাম চ্যাটবট", "স্ট্যান্ডার্ড (মাসিক)", "৳৭০০", "monthly"),
        new OrderPackage("মেসেঞ্জার চ্যাটবট", "প্রিমিয়াম (মাসিক)", "৳১,০০০", "monthly"),
        new OrderPackage("ইনস্টাগ্রাম চ্যাটবট", "প্রিমিয়াম (মাসিক)", "৳১,০০০", "monthly"),
        new OrderPackage("ফেসবুক অটো পোস্ট", "মাসিক", "৳১,০০০", "monthly"),
        new OrderPackage("ইউটিউব অটো পোস্ট", "মাসিক", "৳১,০০০", "monthly"),
        new OrderPackage("মেসেঞ্জার চ্যাটবট", "এককালীন", "৳৫,০০০", "one_time"),
        new OrderPackage("ইনস্টাগ্রাম চ্যাটবট", "এককালীন", "৳৫,০০০", "one_time"),
        new OrderPackage("ফেসবুক অটো পোস্ট", "এককালীন", "৳৫,০০০", "one_time"),
        new OrderPackage("ইউটিউব অটো পোস্ট", "এককালীন", "৳৫,০০০", "one_time"),
        new OrderPackage("হোয়াটসঅ্যাপ বিজনেস বট", "এককালীন", "৳৫,০০০", "one_time"),
        new OrderPackage("হোয়াটসঅ্যাপ বিজনেস বট", "স্ট্যান্ডার্ড সেটআপ", "৳১,৫০০", "setup"),
        new OrderPackage("মেসেঞ্জার চ্যাটবট", "ট্রায়াল ৭ দিন", "৳৫০", "trial"),
        new OrderPackage("ইনস্টাগ্রাম চ্যাটবট", "ট্রায়াল", "৳৫০", "trial"),
        new OrderPackage("ফেসবুক অটো পোস্ট", "ট্রায়াল", "৳৫০", "trial"),
        new OrderPackage("ইউটিউব অটো পোস্ট", "ট্রায়াল", "৳৫০", "trial"),
        new OrderPackage("হোয়াটসঅ্যাপ বিজনেস বট", "ট্রায়াল", "৳৫০", "trial"),
    };

    /**
     * Subscription for an account, derived from REAL monthly orders:
     * latest monthly order that is not cancelled. Start = order date,
     * expiry = start + 1 month. Pending orders are included (a placed
     * order shows up as soon as its date is set) — the UI must label
     * a pending one as awaiting admin approval, not as active service.
     */
    public static SubscriptionStore.Subscription subscriptionFromOrders(
            List<Order> orders) {
        Order latest = null;
        for (Order o : orders) {
            if (o == null || !o.isMonthly()) continue;
            if ("cancelled".equals(o.status) || "rejected".equals(o.status)) continue;
            if (latest == null || o.dateMillis > latest.dateMillis) latest = o;
        }
        if (latest == null) return null;
        return SubscriptionStore.fromOrder(latest.serviceName, latest.packageDetails,
            latest.amount, latest.dateMillis, 1);
    }

    /** DEMO PREVIEW ONLY — never the default order list. */
    public static List<Order> demoOrders() {
        List<Order> out = new ArrayList<Order>();
        long now = System.currentTimeMillis();
        Order d = new Order("demo-order-1", "demo-user", "Demo Customer",
            "মেসেঞ্জার চ্যাটবট", "প্রিমিয়াম (মাসিক)", "৳১,০০০",
            "bkash", "01XXXXXXXXX", "DEMO123", "in_progress",
            "ডেমো তারিখ", now - 10L * 24 * 60 * 60 * 1000,
            "", "ডেমো: কোনো বাড়তি তথ্য নেই", "monthly");
        d.adminMessage = "ডেমো: অ্যাডমিন মেসেজ";
        out.add(d);
        return out;
    }
}
