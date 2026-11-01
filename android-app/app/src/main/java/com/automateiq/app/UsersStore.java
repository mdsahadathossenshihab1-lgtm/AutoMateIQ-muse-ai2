package com.automateiq.app;

/**
 * Customer (users) model for the v7 app — SAME shape as the app
 * database 'users' collection (see app types.ts User: id, name,
 * email, phone, role, location) PLUS the page-record fields the v7
 * admin panel records on the customer's users doc:
 * pageName, pageLink, pageId, pageStatus, pageNotes.
 *
 * PRIVACY (deliberate): this model parses name, role, location and
 * the page fields ONLY. It never parses or displays a customer's
 * email or phone number — the admin panel shows identity, orders,
 * pages and subscription days, not contact details. The database
 * rules are the real gate (users list is admin-only).
 *
 * Page status values (admin panel): "pending" = admin access
 * pending, "test" = added as test page, "connected" = connected.
 */
public class UsersStore {

    public static class User {
        public String id;
        public String name;
        public String role;
        public String location;
        public String pageName;
        public String pageLink;
        public String pageId;
        public String pageStatus;
        public String pageNotes;
        /** True only for records kept locally this session because the
         *  service save failed or the userId was not found. Never
         *  presented as a saved service record. */
        public boolean localOnly;

        public User(String id, String name, String role, String location,
                    String pageName, String pageLink, String pageId,
                    String pageStatus, String pageNotes) {
            this.id = id == null ? "" : id;
            this.name = name == null || name.length() == 0 ? "কাস্টমার" : name;
            this.role = role == null ? "" : role;
            this.location = location == null ? "" : location;
            this.pageName = pageName == null ? "" : pageName;
            this.pageLink = pageLink == null ? "" : pageLink;
            this.pageId = pageId == null ? "" : pageId;
            this.pageStatus = pageStatus == null ? "" : pageStatus;
            this.pageNotes = pageNotes == null ? "" : pageNotes;
            this.localOnly = false;
        }

        public String initial() {
            if (name == null || name.length() == 0) return "?";
            return name.substring(0, 1);
        }

        public boolean isAdminUser() { return "admin".equals(role); }

        public boolean hasPage() {
            return pageName.length() > 0 || pageId.length() > 0
                || pageLink.length() > 0;
        }

        public String pageStatusBn() {
            if ("connected".equals(pageStatus)) return "কানেক্টেড";
            if ("test".equals(pageStatus)) return "টেস্ট পেজ";
            if ("pending".equals(pageStatus)) return "অ্যাডমিন অ্যাক্সেস অপেক্ষমাণ";
            if (pageStatus.length() > 0) return pageStatus;
            return "পেজ রেকর্ড নেই";
        }
    }
}
