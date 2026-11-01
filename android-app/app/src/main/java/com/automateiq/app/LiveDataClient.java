package com.automateiq.app;

/**
 * LIVE DATA CONSTANTS + EXPLANATION for the v4 dashboard app.
 *
 * v4 is LIVE FIRST: FirebaseAuthHelper reads the website Firestore
 * database and sends replies through the Render server. This class holds
 * only the shared constants and the user-facing explanation — it makes
 * no network call itself and holds no token, key or secret.
 *
 * Website Firestore: project project-d28c75fa-a3af-48dc-ac6, NAMED
 * database ai-studio-automateiq-a1b8237c-c72f-49f9-8d26-7de394824bb1
 * (never the (default) database — the website has no default database
 * and creating one would split bot/website data).
 * Collection: messenger_leads, filtered by ownerId == signed-in UID.
 * Reply contract (Render, same as the website): POST with body
 * { businessId, pageId, recipientPsid, message }.
 * The Page Access Token lives on the Render server only, never in app.
 */
public class LiveDataClient {

    /** Website Firestore named database id (public identifier, not a secret). */
    public static final String DATABASE_ID =
        "ai-studio-automateiq-a1b8237c-c72f-49f9-8d26-7de394824bb1";

    /** Website Firestore collection for Messenger leads. */
    public static final String LEADS_COLLECTION = "messenger_leads";

    /** Render bot server reply endpoint (same contract as the website). */
    public static final String RENDER_REPLY_URL =
        "https://automateiq-messenger-bot.onrender.com/messenger-reply";

    /** AutoMateIQ Page id (public Page identifier, not a secret). */
    public static final String PAGE_ID = "825495483971307";

    /** Public website + Page URLs — used ONLY for secondary "open" buttons, never for login. */
    public static final String WEBSITE_URL = "https://auto-mate-iq-muse-ai2.vercel.app";
    public static final String PAGE_URL = "https://facebook.com/automateiq.xyz";

    /** Bengali explanation shown in the app's Live Data section. */
    public static String liveDataExplanation() {
        return "লাইভ ডাটা: এই অ্যাপ ওয়েবসাইটের একই Firebase ডাটাবেস থেকে লিড পড়ে — "
            + "একই messenger_leads কালেকশন, শুধু আপনার ownerId দিয়ে ফিল্টার করা। "
            + "রিপ্লাই যায় একই Render সার্ভার দিয়ে। পেজের টোকেন কখনো অ্যাপের ভিতরে "
            + "রাখা হয় না, সেটা সার্ভারেই থাকে।";
    }
}
