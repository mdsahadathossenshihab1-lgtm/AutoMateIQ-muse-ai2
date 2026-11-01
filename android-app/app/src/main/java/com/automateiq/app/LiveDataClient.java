package com.automateiq.app;

/**
 * LIVE DATA PLUG-IN POINT — TODO, NOT YET CONNECTED.
 *
 * This class is the ONE clearly-marked place where live data plugs in
 * later. Right now it makes NO network call and returns no live data.
 * The v3 app runs on the local LeadsStore demo data so the mobile flow
 * (leads, thread, reply, Take Over, Resume AI) is testable end-to-end.
 *
 * When live data is enabled, the work is:
 *  TODO 1: Read the website Firestore database
 *          (ai-studio-automateiq-a1b8237c-c72f-49f9-8d26-7de394824bb1),
 *          collection "messenger_leads", filtered by ownerId, using the
 *          Firebase Android SDK after registering this Android app
 *          (package com.automateiq.app) in that Firebase project and
 *          adding google-services.json. Do NOT put any token, key, or
 *          password in this app to do that.
 *  TODO 2: Send an owner reply through the Render bot server using the
 *          SAME contract as the website reply flow:
 *          POST RENDER_REPLY_URL with body
 *          { businessId, pageId, recipientPsid, message }.
 *          The server holds the Page Access Token; the app must NEVER
 *          hold or show that token.
 *  TODO 3: Map Take Over to paused=true / status=handover and Resume AI
 *          to paused=false / status=bot_replied on the same document,
 *          exactly as the website Admin Panel does.
 *  TODO 4: Replace LeadsStore demo leads with the live list, keeping
 *          the same Lead/Message shape, and remove the demo banner.
 *
 * Until those TODOs are done, do NOT claim live Facebook/Firestore data.
 */
public class LiveDataClient {

    /** Render bot server reply endpoint (same contract as the website). */
    public static final String RENDER_REPLY_URL =
        "https://automateiq-messenger-bot.onrender.com/messenger-reply";

    /** Website Firestore collection for Messenger leads. */
    public static final String LEADS_COLLECTION = "messenger_leads";

    /** AutoMateIQ Page id (public Page identifier, not a secret). */
    public static final String PAGE_ID = "825495483971307";

    /** True only after TODO 1-4 above are implemented and tested. */
    public static boolean isLiveEnabled() {
        return false;
    }

    /** TODO: fetch live leads from Firestore. Currently returns null. */
    public static java.util.List<LeadsStore.Lead> fetchLiveLeads() {
        // TODO: implement with Firebase Android SDK (see class comment).
        return null;
    }

    /**
     * TODO: send a live owner reply via RENDER_REPLY_URL with
     * { businessId, pageId, recipientPsid, message }.
     * Currently returns false (not sent); the app uses local state instead.
     */
    public static boolean sendLiveReply(String businessId, String pageId,
                                        String recipientPsid, String message) {
        // TODO: implement POST to RENDER_REPLY_URL (see class comment).
        return false;
    }

    /** Bengali explanation shown in the app's Live Data section. */
    public static String liveDataExplanation() {
        return "লাইভ ডাটা এখনো যুক্ত হয়নি। এই অ্যাপ এখন ডেমো লিড দিয়ে চলে, "
            + "যাতে মোবাইলে পুরো ড্যাশবোর্ড ফ্লো পরীক্ষা করা যায়।\n\n"
            + "লাইভ ডাটা যুক্ত হলে এখানেই ওয়েবসাইটের একই লিড আসবে — "
            + "একই Firestore ডাটাবেস (messenger_leads) এবং একই Render রিপ্লাই সার্ভার থেকে। "
            + "সেজন্য লাগবে: (১) Firebase-এ এই Android অ্যাপ রেজিস্টার করা, "
            + "(২) একটি ছোট সার্ভার এন্ডপয়েন্ট দিয়ে লাইভ লিড পড়া ও রিপ্লাই পাঠানো। "
            + "পেজের টোকেন কখনো অ্যাপের ভিতরে রাখা হবে না, সেটা সার্ভারেই থাকবে।";
    }
}
