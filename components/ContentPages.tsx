import React from 'react';
import { X } from 'lucide-react';

/**
 * FAQ, Refund/Renewal, and Privacy/Terms pages.
 * Honest content only: prices and services below match the packages
 * already shown on the site (Messenger/Instagram Trial ৳50, Standard
 * ৳700, Premium ৳1,000, One-time ৳5,000; WhatsApp Standard Setup
 * ৳1,500, One-time ৳5,000; Facebook/YouTube Auto Post Monthly ৳1,000,
 * One-time ৳5,000; API Credit Wallet ৳200 / ৳500 / ৳1,000 / ৳2,500).
 * No invented guarantees: delivery depends on the customer
 * submitting order details, payment is verified manually via
 * sender phone + TrxID, and per-message/per-call API rates are not
 * stated on the site — customers are told to ask in the support
 * inbox rather than being given an invented rate.
 */
export type ContentPageId = 'faq' | 'refund' | 'privacy';

export const CONTENT_PAGE_TITLES: Record<ContentPageId, string> = {
  faq: 'FAQ — Frequently Asked Questions',
  refund: 'Refund & Renewal Terms',
  privacy: 'Privacy Policy & Terms of Service',
};

const Section: React.FC<{ title: string; children: React.ReactNode }> = ({ title, children }) => (
  <div className="mb-7">
    <h3 className="font-bold text-lg text-slate-900 mb-2">{title}</h3>
    <div className="text-slate-600 leading-relaxed space-y-2">{children}</div>
  </div>
);

const ContentPages: React.FC<{ page: ContentPageId; onClose: () => void }> = ({ page, onClose }) => (
  <div onClick={onClose} className="fixed inset-0 z-[80] flex items-start justify-center p-4 bg-black/60 backdrop-blur-sm overflow-y-auto">
    <div onClick={(e) => e.stopPropagation()} className="bg-white border border-slate-200 rounded-3xl shadow-2xl w-full max-w-2xl overflow-hidden my-4 sm:my-8 flex flex-col max-h-[calc(100dvh-2rem)] sm:max-h-[calc(100dvh-4rem)]">
      <div className="bg-slate-50 p-6 flex justify-between items-center border-b border-slate-200 shrink-0">
        <h2 className="font-bold text-xl text-slate-900">{CONTENT_PAGE_TITLES[page]}</h2>
        <button onClick={onClose} className="p-2 hover:bg-slate-200 rounded-full shrink-0"><X size={20} /></button>
      </div>
      <div className="p-6 md:p-8 overflow-y-auto min-h-0">
        {page === 'faq' && (
          <>
            <Section title="What does AutoMateIQ do?">
              <p>AutoMateIQ builds automation for small and medium businesses: Messenger and Instagram chatbots, WhatsApp Business bots, and Facebook/YouTube auto-posting, plus an API Credit Wallet for chatbot usage.</p>
            </Section>
            <Section title="How do I order?">
              <p>Choose a package, send money via bKash/Nagad (Personal), then submit your sender phone number and Transaction ID (TrxID). Your order is submitted as Pending in your dashboard. An admin reviews it, then you submit your Page link and business details in the order detail view so the team can set the service up.</p>
            </Section>
            <Section title="How long does setup take?">
              <p>Setup starts after payment verification and after you submit the required details. The site does not promise an exact delivery time — if details are missing, the team will ask for them in your support inbox, which can delay setup.</p>
            </Section>
            <Section title="What is the support inbox for?">
              <p>The Dashboard Inbox is for you (the business customer) to talk to the AutoMateIQ developer team about your order and service details. It is not the inbox for your own customers — your own Messenger customers are handled in Messenger Leads, where you reply to them on Messenger from the website.</p>
            </Section>
            <Section title="What does the API Credit Wallet cover?">
              <p>Wallet recharges are ৳200 / ৳500 / ৳1,000 / ৳2,500, with no expiry stated on the site, on a pay-as-you-go basis. A recharge is only added to your balance after an admin approves it. A per-message or per-call rate is not stated on the site — ask in the support inbox for the current rate before relying on a balance for a campaign.</p>
            </Section>
            <Section title="Do you offer a free trial?">
              <p>Messenger and Instagram chatbots list a 7-day Trial at ৳50, and WhatsApp lists a Trial at ৳50. Trial scope and what happens when it ends are confirmed by the team in your order thread — do not assume a trial automatically converts to a paid plan.</p>
            </Section>
          </>
        )}
        {page === 'refund' && (
          <>
            <Section title="Payment verification">
              <p>Payments are made by Send Money via bKash/Nagad (Personal) and verified manually against the sender phone and TrxID you submit. A payment is not auto-verified, and an order or wallet balance is not activated until an admin approves it.</p>
            </Section>
            <Section title="Refunds">
              <p>Because setup work and chatbot configuration begin after verification, refunds are handled case by case, not automatically. If you believe a payment was made in error, or a service was never set up, contact the team in your support inbox with your order ID, sender phone, and TrxID, and an admin will review it. Approved refunds, if any, are returned by the same Send Money method where possible. One-time setup fees for work already completed are not automatically refundable.</p>
            </Section>
            <Section title="Renewal">
              <p>Monthly packages (Messenger/Instagram Standard ৳700, Premium ৳1,000; Facebook/YouTube Auto Post ৳1,000) do not auto-renew from the website on their own — you renew by placing a new order or recharging as instructed by the team before your current period ends. One-time packages (৳5,000, WhatsApp Setup ৳1,500 where listed) are not subscriptions. Trial packages last 7 days where stated; after a trial ends, service continues only if you order a paid package.</p>
            </Section>
            <Section title="Cancellation">
              <p>You can ask to cancel a pending order in your support inbox before setup begins. Orders already in progress or completed are handled under the refund review above. Cancelling a chatbot service does not automatically delete your Page data — ask the team if you want data removed.</p>
            </Section>
          </>
        )}
        {page === 'privacy' && (
          <>
            <Section title="What data is collected?">
              <p>Account data you provide (name, email, phone), order data (package, amount, payment method, sender phone, TrxID), order detail data (Page link, business details, access notes, follow-up info), support messages, and Messenger lead data for your own business (lead names, messages, and Page IDs for leads who messaged your Page).</p>
            </Section>
            <Section title="How is it used?">
              <p>Data is used only to deliver the service you ordered, to verify payments, to run your chatbot and Messenger Leads, and to reply to you in the support inbox. Your customers' Messenger data is shown only to your business account — other businesses cannot see it — and is not sold or shared for advertising.</p>
            </Section>
            <Section title="Who can see what?">
              <p>Clients see only their own orders, wallet, support thread, and Messenger Leads. Admins (the AutoMateIQ team) can see customer orders and support threads to deliver services, and wallet recharges to verify them. Admin access is enforced in Firestore security rules as well as in the dashboard UI — UI hiding alone is not the security boundary.</p>
            </Section>
            <Section title="Page access and tokens">
              <p>Connecting a Facebook Page uses Meta's official Page connection flow. Page Access Tokens, where used, are stored server-side for the reply-send workflow only and are never put in website code, never shown in the browser, and never included in website reply requests. If you revoke Page access, the bot/reply path for that Page stops.</p>
            </Section>
            <Section title="Cookies and third parties">
              <p>The site uses Firebase for sign-in and data storage, and may use analytics/pixel tools for site measurement. No payment card data is collected by this website — payments are Send Money transfers outside the site, verified only by sender phone and TrxID.</p>
            </Section>
            <Section title="Changes and contact">
              <p>These terms may be updated as services change; the version shown on this site applies. For questions, use the Dashboard support inbox after signing in, or the contact details on the home page.</p>
            </Section>
          </>
        )}
        <p className="text-xs text-slate-400 mt-8">If any statement here conflicts with a price or package shown on the Pricing section, the Pricing section's package details apply for that package, and the team will confirm in your order thread.</p>
      </div>
    </div>
  </div>
);

export default ContentPages;
