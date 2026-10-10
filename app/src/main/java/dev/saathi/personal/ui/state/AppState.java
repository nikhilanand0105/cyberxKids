package dev.saathi.personal.ui.state;

import dev.saathi.engine.SaathiApkIntelligence;
import dev.saathi.model.ThreatSeverity;
import dev.saathi.personal.ui.model.ActivityEvent;
import dev.saathi.personal.ui.model.ChatMessage;
import dev.saathi.personal.ui.model.ChildProfile;
import dev.saathi.personal.ui.model.NavigationTab;
import dev.saathi.sense.SaathiSenseEngine;
import dev.saathi.shield.network.LocalFirewallEngine;
import dev.saathi.shield.network.PhishingUrlScanner;
import dev.saathi.storage.LocalStorageRepository;
import dev.saathi.storage.TrustedHelper;

import java.util.*;

public class AppState {

    private static AppState instance;

    public static synchronized AppState get() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    private final SaathiApkIntelligence apkEngine;
    private final SaathiSenseEngine senseEngine;
    private final PhishingUrlScanner phishingScanner;
    private final LocalFirewallEngine firewallEngine;
    private final LocalStorageRepository storageRepository;

    private ChildProfile currentProfile = ChildProfile.AARAV;
    private NavigationTab currentTab = NavigationTab.HOME;
    private boolean isSeniorMode = false;
    private boolean isHindi = false;
    private int safetyScore = 96;

    private final List<ActivityEvent> activityEvents = new ArrayList<>();
    private final List<ChatMessage> chatMessages = new ArrayList<>();
    private final List<Runnable> stateListeners = new ArrayList<>();

    private AppState() {
        this.apkEngine = new SaathiApkIntelligence();
        this.senseEngine = new SaathiSenseEngine();
        this.phishingScanner = new PhishingUrlScanner();
        this.firewallEngine = new LocalFirewallEngine();
        this.storageRepository = new LocalStorageRepository();

        // Seed initial trusted helpers
        storageRepository.addTrustedHelper(new TrustedHelper("th-1", "Sunita (Mother)", "PRIMARY_PARENT", "+91 98101 23456", true));
        storageRepository.addTrustedHelper(new TrustedHelper("th-2", "Rajesh (Father)", "GUARDIAN", "+91 98202 34567", true));

        // Seed initial realistic activity events
        seedInitialActivityEvents();

        // Seed initial assistant greeting
        seedInitialChatMessages();
    }

    private void seedInitialActivityEvents() {
        activityEvents.add(new ActivityEvent(
                "evt-101",
                new Date(System.currentTimeMillis() - 1000 * 60 * 25), // 25 mins ago
                ActivityEvent.EventCategory.APK,
                ThreatSeverity.CRITICAL,
                "Malicious Sideload Blocked: GB_WhatsApp_v19.apk",
                "App requested BIND_ACCESSIBILITY_SERVICE and SMS reading. Neutralized before installation.",
                List.of(
                        "DEX Hook: findAccessibilityNodeInfosByViewId",
                        "Permissions: RECEIVE_SMS, SYSTEM_ALERT_WINDOW",
                        "Certificate: Untrusted Self-Signed Debug Key"
                ),
                "View DEX Evidence"
        ));

        activityEvents.add(new ActivityEvent(
                "evt-102",
                new Date(System.currentTimeMillis() - 1000 * 60 * 85), // 85 mins ago
                ActivityEvent.EventCategory.SENSE,
                ThreatSeverity.HIGH,
                "Secrecy & Camera Demand Flagged in Discord Chat",
                "Stranger asked Aarav: 'Video call pe aa ja, kisi ko mat batana'. Sense flagged grooming cue.",
                List.of(
                        "Category: GROOMING_MANIPULATION",
                        "Matched Linguistic Patterns: 'kisi ko mat batana', 'camera on kar'",
                        "Recommendation: Advised child not to share images & warned parents"
                ),
                "Review Conversation"
        ));

        activityEvents.add(new ActivityEvent(
                "evt-103",
                new Date(System.currentTimeMillis() - 1000 * 60 * 240), // 4 hrs ago
                ActivityEvent.EventCategory.PHISHING,
                ThreatSeverity.HIGH,
                "Fake BGMI Free UC Link Neutralized",
                "Received SMS with link: http://free-bgmi-uc-rewards.top/claim. Phishing kit identified.",
                List.of(
                        "Domain: free-bgmi-uc-rewards.top",
                        "High-Risk TLD: .top (Common disposable phishing host)",
                        "Lure: Child Financial Gaming Scam (Account Stealing)"
                ),
                "Block Domain"
        ));

        activityEvents.add(new ActivityEvent(
                "evt-104",
                new Date(System.currentTimeMillis() - 1000 * 60 * 600), // 10 hrs ago
                ActivityEvent.EventCategory.FIREWALL,
                ThreatSeverity.INFO,
                "Local DNS Firewall Blocked 14 Ad/Tracker Requests",
                "Background telemetry to known user-tracking endpoints blocked without cloud relaying.",
                List.of(
                        "Destinations: telemetry-ads.tracking-network.com, analytics-leak.net",
                        "Zero battery drain: Snapdragon 8 Elite hardware-accelerated rule table"
                ),
                "View Firewall Log"
        ));
    }

    private void seedInitialChatMessages() {
        chatMessages.add(new ChatMessage(
                false,
                "Namaste! I am Saathi AI, your private Guardian on Snapdragon 8 Elite.\n\nI continuously monitor downloaded APKs, child communication secrecy cues, and fake payment links. How can I help secure your family today?",
                "Saathi Guardian AI"
        ));
    }

    public SaathiApkIntelligence getApkEngine() {
        return apkEngine;
    }

    public SaathiSenseEngine getSenseEngine() {
        return senseEngine;
    }

    public PhishingUrlScanner getPhishingScanner() {
        return phishingScanner;
    }

    public LocalFirewallEngine getFirewallEngine() {
        return firewallEngine;
    }

    public LocalStorageRepository getStorageRepository() {
        return storageRepository;
    }

    public ChildProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(ChildProfile currentProfile) {
        this.currentProfile = currentProfile;
        this.safetyScore = currentProfile.getDefaultSafetyScore();
        if (currentProfile == ChildProfile.DADAJI) {
            this.isSeniorMode = true;
        }
        notifyListeners();
    }

    public NavigationTab getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(NavigationTab currentTab) {
        this.currentTab = currentTab;
        notifyListeners();
    }

    public boolean isSeniorMode() {
        return isSeniorMode;
    }

    public void setSeniorMode(boolean seniorMode) {
        this.isSeniorMode = seniorMode;
        notifyListeners();
    }

    public boolean isHindi() {
        return isHindi;
    }

    public void setHindi(boolean hindi) {
        this.isHindi = hindi;
        notifyListeners();
    }

    public int getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(int safetyScore) {
        this.safetyScore = safetyScore;
        notifyListeners();
    }

    public List<ActivityEvent> getActivityEvents() {
        return Collections.unmodifiableList(activityEvents);
    }

    public void addActivityEvent(ActivityEvent event) {
        activityEvents.add(0, event);
        notifyListeners();
    }

    public List<ChatMessage> getChatMessages() {
        return Collections.unmodifiableList(chatMessages);
    }

    public void addChatMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyListeners();
    }

    public void addListener(Runnable listener) {
        if (!stateListeners.contains(listener)) {
            stateListeners.add(listener);
        }
    }

    public void removeListener(Runnable listener) {
        stateListeners.remove(listener);
    }

    public void notifyListeners() {
        for (Runnable r : stateListeners) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }
}
