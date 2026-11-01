package com.automateiq.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Lead data layer for the v4 mobile dashboard — LIVE FIRST.
 *
 * Shape matches the website Firestore messenger_leads docs exactly:
 * pageId, psid, name, status, lastMessage, updated_at, messages[{role, text,
 * created_at}], paused, profilePic, ownerId.
 *
 * Role values are explicit and never guessed: "user" = customer,
 * "bot" = bot, "owner" = human/owner reply. The app maps them to
 * কাস্টমার / বট / আপনি and never labels a customer message as owner.
 *
 * v4 CHANGE vs v3: the live list starts EMPTY (with a loading state and
 * then an empty state) — demo leads are NO LONGER the default content.
 * Demo leads exist ONLY behind an explicit "Demo preview" button, via
 * demoLeads(), and every screen that shows them carries a demo banner.
 * Demo content must never be presented as real customers.
 */
public class LeadsStore {

    public static class Message {
        public String role;
        public String text;
        public String createdAt;
        public Message(String role, String text, String createdAt) {
            this.role = role;
            this.text = text;
            this.createdAt = createdAt;
        }
    }

    public static class Lead {
        public String pageId;
        public String psid;
        public String name;
        public String status; // bot_replied | handover | new
        public String lastMessage;
        public String updatedAt;
        public boolean paused;
        public String profilePic;
        public List<Message> messages = new ArrayList<Message>();
        public Lead(String pageId, String psid, String name, String status,
                    String lastMessage, String updatedAt, boolean paused) {
            this.pageId = pageId;
            this.psid = psid;
            this.name = name;
            this.status = status;
            this.lastMessage = lastMessage;
            this.updatedAt = updatedAt;
            this.paused = paused;
            this.profilePic = "";
        }
        public String initial() {
            if (name == null || name.length() == 0) return "?";
            return name.substring(0, 1);
        }
        /** Firestore document id, same convention as the website. */
        public String docId() { return pageId + "_" + psid; }
    }

    private List<Lead> leads = new ArrayList<Lead>();

    /** Replace the whole list with live Firestore leads (live mode). */
    public void setLiveLeads(List<Lead> live) {
        leads = live == null ? new ArrayList<Lead>() : live;
    }

    public List<Lead> all() { return leads; }

    public List<Lead> filtered(String filter) {
        if (filter == null || "all".equals(filter)) return leads;
        List<Lead> out = new ArrayList<Lead>();
        for (Lead l : leads) {
            if ("handover".equals(filter) && l.paused) out.add(l);
            else if ("bot_replied".equals(filter) && "bot_replied".equals(l.status)) out.add(l);
            else if ("new".equals(filter) && "new".equals(l.status)) out.add(l);
        }
        return out;
    }

    public int total() { return leads.size(); }

    public int handoverCount() {
        int n = 0;
        for (Lead l : leads) if (l.paused) n++;
        return n;
    }

    public int botRepliedCount() {
        int n = 0;
        for (Lead l : leads) if ("bot_replied".equals(l.status)) n++;
        return n;
    }

    public int newCount() {
        int n = 0;
        for (Lead l : leads) if ("new".equals(l.status)) n++;
        return n;
    }

    /** Owner replies from the app: appends to the thread and updates the lead. */
    public void appendOwnerReply(Lead lead, String replyText) {
        lead.messages.add(new Message("owner", replyText, "এখন"));
        lead.lastMessage = replyText;
        lead.updatedAt = "এখন";
    }

    /** Take Over: bot stops for this lead (paused=true, status=handover). */
    public void takeOver(Lead lead) {
        lead.paused = true;
        lead.status = "handover";
        lead.messages.add(new Message("bot",
            "আপনাকে একজন মানুষের কাছে ট্রান্সফার করা হচ্ছে, অনুগ্রহ করে অপেক্ষা করুন", "এখন"));
        lead.lastMessage = "মানুষের কাছে ট্রান্সফার করা হয়েছে";
        lead.updatedAt = "এখন";
    }

    /** Resume AI: bot resumes for this lead (paused=false, status=bot_replied). */
    public void resumeAi(Lead lead) {
        lead.paused = false;
        lead.status = "bot_replied";
        lead.updatedAt = "এখন";
    }

    public String copyLeadDetails(Lead lead) {
        return "নাম: " + lead.name + "\nপেজ: automateiq\nস্ট্যাটাস: " + lead.status
            + "\nশেষ মেসেজ: " + lead.lastMessage;
    }

    public String copyAllLeadsSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("AutoMateIQ লিড সারাংশ\n");
        sb.append("মোট: ").append(total()).append(", হ্যান্ডওভার: ").append(handoverCount())
          .append(", বট রিপ্লাই: ").append(botRepliedCount())
          .append(", নতুন: ").append(newCount()).append("\n");
        for (Lead l : leads) {
            sb.append("- ").append(l.name).append(" | ").append(l.status)
              .append(" | ").append(l.lastMessage).append("\n");
        }
        return sb.toString();
    }

    /**
     * DEMO PREVIEW ONLY — returned only when the user explicitly taps
     * "Demo preview". Never used as the default list. Every demo lead is
     * labelled demo and is NOT a real customer.
     */
    public static List<Lead> demoLeads() {
        List<Lead> out = new ArrayList<Lead>();
        Lead d1 = new Lead("825495483971307", "demo-psid-1", "Demo Lead 1",
            "bot_replied", "ডেমো: বটের উত্তর দেওয়া হয়েছে", "আজ, সকাল ১০:৩০", false);
        d1.messages.add(new Message("user",
            "ডেমো: হ্যালো, মেসেঞ্জার চ্যাটবটের দাম কত?", "আজ, সকাল ১০:২৮"));
        d1.messages.add(new Message("bot",
            "ডেমো উত্তর: মেসেঞ্জার চ্যাটবট — ট্রায়াল ৳৫০, স্ট্যান্ডার্ড ৳৭০০/মাস, প্রিমিয়াম ৳১,০০০/মাস, এককালীন ৳৫,০০০।",
            "আজ, সকাল ১০:৩০"));
        out.add(d1);
        Lead d2 = new Lead("825495483971307", "demo-psid-2", "Demo Lead 2",
            "handover", "ডেমো: মানুষের কাছে ট্রান্সফার করা হয়েছে", "আজ, সকাল ১১:০৫", true);
        d2.messages.add(new Message("user",
            "ডেমো: আমি একজন মানুষের সাথে কথা বলতে চাই", "আজ, সকাল ১১:০৩"));
        d2.messages.add(new Message("bot",
            "আপনাকে একজন মানুষের কাছে ট্রান্সফার করা হচ্ছে, অনুগ্রহ করে অপেক্ষা করুন",
            "আজ, সকাল ১১:০৫"));
        out.add(d2);
        Lead d3 = new Lead("825495483971307", "demo-psid-3", "Demo Lead 3",
            "new", "ডেমো: নতুন মেসেজ, এখনো উত্তর দেওয়া হয়নি", "আজ, দুপুর ১২:১৫", false);
        d3.messages.add(new Message("user",
            "ডেমো: এপিআই ক্রেডিট কিভাবে নেব?", "আজ, দুপুর ১২:১৫"));
        out.add(d3);
        return out;
    }
}
