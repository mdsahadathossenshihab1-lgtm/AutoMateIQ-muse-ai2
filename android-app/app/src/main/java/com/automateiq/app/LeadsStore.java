package com.automateiq.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Lead data layer for the v4 mobile dashboard — LIVE FIRST.
 *
 * Shape matches the app database messenger_leads docs exactly:
 * pageId, psid, name, status, lastMessage, updated_at, messages[{role, text,
 * created_at}], paused, profilePic, ownerId.
 *
 * Role values are explicit and never guessed: "user" = customer,
 * "bot" = bot, "owner" = human/owner reply. The app maps them to
 * কাস্টমার / বট / আপনি and never labels a customer message as owner.
 *
 * The live list starts EMPTY (with a loading state and then a
 * designed empty state). No sample content is ever shown as real data.
 */
public class LeadsStore {

    public static class Message {
        public String role;
        public String text;
        public String createdAt;
        // v5 SEND STATUS: "" = historic/unknown, "sending" | "sent" | "failed".
        // Only owner messages ever carry a status; customer/bot messages
        // keep "" and show no status line.
        public String sendStatus;
        public Message(String role, String text, String createdAt) {
            this(role, text, createdAt, "");
        }
        public Message(String role, String text, String createdAt, String sendStatus) {
            this.role = role;
            this.text = text;
            this.createdAt = createdAt;
            this.sendStatus = sendStatus == null ? "" : sendStatus;
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

    /**
     * Owner replies from the app: appends to the thread, updates the lead,
     * AND auto-pauses the bot (paused=true, status=handover). v5 FIX: v4
     * left the bot active after an owner reply, so the bot could double-
     * reply and the app never flipped to Handover. An owner reply is
     * a Take Over by definition.
     */
    public void appendOwnerReply(Lead lead, String replyText) {
        lead.messages.add(new Message("owner", replyText, "এখন", "sent"));
        lead.lastMessage = replyText;
        lead.updatedAt = "এখন";
        lead.paused = true;
        lead.status = "handover";
    }

    /**
     * v5 send flow: add the owner message immediately in "sending" state
     * (so it is visible at the bottom at once), already paused. The caller
     * flips sendStatus to "sent" or "failed" when the service answers.
     */
    public Message addSendingOwnerMessage(Lead lead, String replyText) {
        Message m = new Message("owner", replyText, "এখন", "sending");
        lead.messages.add(m);
        lead.lastMessage = replyText;
        lead.updatedAt = "এখন";
        lead.paused = true;
        lead.status = "handover";
        return m;
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

    
}
