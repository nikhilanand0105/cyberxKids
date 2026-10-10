package dev.saathi.kids.ui.state;

import android.content.Context;
import android.content.SharedPreferences;

import dev.saathi.kids.ui.model.Lesson;
import dev.saathi.kids.ui.model.SosRequest;
import dev.saathi.kids.ui.model.TrustedAdult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Central, lightweight persistence and reactive application state for Saathi Kids.
 * Stored in app-private SharedPreferences (dev.saathi.kids).
 */
public class KidsAppState {

    private static KidsAppState instance;
    private SharedPreferences prefs;
    private final List<Lesson> allLessons = new ArrayList<>();
    private final List<TrustedAdult> trustedAdults = new ArrayList<>();
    private final List<SosRequest> sosHistory = new ArrayList<>();

    public interface StateChangeListener {
        void onStateChanged();
    }

    private final List<StateChangeListener> listeners = new ArrayList<>();

    private KidsAppState() {}

    public static synchronized KidsAppState getInstance() {
        if (instance == null) {
            instance = new KidsAppState();
        }
        return instance;
    }

    public void initContext(Context context) {
        if (prefs == null) {
            prefs = context.getApplicationContext().getSharedPreferences("saathi_kids_prefs", Context.MODE_PRIVATE);
            loadLessons();
            loadTrustedAdults();
        }
    }

    public void addListener(StateChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (StateChangeListener l : new ArrayList<>(listeners)) {
            l.onStateChanged();
        }
    }

    // --- Onboarding ---
    public boolean isOnboardingCompleted() {
        return prefs != null && prefs.getBoolean("onboarding_completed", false);
    }

    public void setOnboardingCompleted(boolean completed) {
        if (prefs != null) {
            prefs.edit().putBoolean("onboarding_completed", completed).apply();
            notifyListeners();
        }
    }

    // --- Language Preference (English / Hinglish) ---
    public String getLanguage() {
        return prefs != null ? prefs.getString("language_code", "en") : "en";
    }

    public boolean isHinglish() {
        return "hi".equals(getLanguage());
    }

    public void setLanguage(String lang) {
        if (prefs != null) {
            prefs.edit().putString("language_code", lang).apply();
            notifyListeners();
        }
    }

    // --- Accessibility: Typography & Contrast ---
    public boolean isLargeTypography() {
        return prefs != null && prefs.getBoolean("large_typography", false);
    }

    public void setLargeTypography(boolean large) {
        if (prefs != null) {
            prefs.edit().putBoolean("large_typography", large).apply();
            notifyListeners();
        }
    }

    public boolean isHighContrast() {
        return prefs != null && prefs.getBoolean("high_contrast", false);
    }

    public void setHighContrast(boolean high) {
        if (prefs != null) {
            prefs.edit().putBoolean("high_contrast", high).apply();
            notifyListeners();
        }
    }

    // --- Family Pairing ---
    public boolean isFamilyPaired() {
        return prefs != null && prefs.getBoolean("family_paired", true);
    }

    public void setFamilyPaired(boolean paired) {
        if (prefs != null) {
            prefs.edit().putBoolean("family_paired", paired).apply();
            notifyListeners();
        }
    }

    public String getFamilyPairingCode() {
        return "SAATHI-KIDS-4291";
    }

    // --- Check Counter ---
    public int getCheckCount() {
        return prefs != null ? prefs.getInt("check_count", 3) : 3;
    }

    public void incrementCheckCount() {
        if (prefs != null) {
            int current = getCheckCount();
            prefs.edit().putInt("check_count", current + 1).apply();
            notifyListeners();
        }
    }

    // --- Trusted Adults ---
    private void loadTrustedAdults() {
        trustedAdults.clear();
        // Default safe starter trusted contacts
        trustedAdults.add(new TrustedAdult("adult_1", "Maa (Mom)", "Mother", "+91 98765 43210", true));
        trustedAdults.add(new TrustedAdult("adult_2", "Papa (Dad)", "Father", "+91 98765 12345", false));
        trustedAdults.add(new TrustedAdult("adult_3", "Pooja Didi (Sister)", "Elder Sister", "+91 98765 88888", false));
    }

    public List<TrustedAdult> getTrustedAdults() {
        return Collections.unmodifiableList(trustedAdults);
    }

    public void addTrustedAdult(String name, String relation, String contact) {
        String id = "adult_" + System.currentTimeMillis();
        trustedAdults.add(new TrustedAdult(id, name, relation, contact, false));
        notifyListeners();
    }

    public void removeTrustedAdult(String id) {
        for (int i = 0; i < trustedAdults.size(); i++) {
            if (trustedAdults.get(i).getId().equals(id)) {
                trustedAdults.remove(i);
                notifyListeners();
                break;
            }
        }
    }

    // --- SOS Record History ---
    public void recordSosRequest(SosRequest req) {
        sosHistory.add(0, req);
        notifyListeners();
    }

    public List<SosRequest> getSosHistory() {
        return Collections.unmodifiableList(sosHistory);
    }

    // --- Lessons Management ---
    private void loadLessons() {
        allLessons.clear();
        Set<String> completedSet = prefs != null
                ? prefs.getStringSet("completed_lessons", new HashSet<>())
                : new HashSet<>();

        allLessons.add(new Lesson(
                "lesson_prizes",
                "Fake Prizes & Game Rewards",
                "Free Robux, Diamonds, or skin giveaways asking for login",
                "🎁",
                "A player in a game chat sends a link: 'Claim 5,000 free Robux right now before the event ends! Just enter your username and password.'",
                Arrays.asList(
                        "Promises valuable game currency completely for free",
                        "Creates false urgency ('before the event ends')",
                        "Asks for your account password or login credentials"
                ),
                "What is the safest choice?",
                Arrays.asList(
                        "Enter your password quickly to claim the reward",
                        "Do not enter your password; official games never ask for your password on third-party links",
                        "Send the link to your classmates to test if it works"
                ),
                1,
                "Awesome thinking! Game creators never ask for your password to give you items. Entering your details lets thieves take over your gaming account.",
                Arrays.asList(
                        "Never type your password into links sent in game lobbies",
                        "Block or report users who send free currency generators",
                        "If you already entered it, change your password immediately with an adult"
                ),
                completedSet.contains("lesson_prizes")
        ));

        allLessons.add(new Lesson(
                "lesson_links",
                "Suspicious Links & Fake Logins",
                "Web pages that copy real apps to steal account details",
                "🔗",
                "You get a message saying your photo was posted on a popular site with a link: 'check-insta-photo.top'. When you open it, a login screen appears.",
                Arrays.asList(
                        "The website address looks strange (ends with strange .top domain)",
                        "It uses curiosity or fear ('your photo was posted') to make you panic",
                        "It asks you to log in before showing anything"
                ),
                "What should you do?",
                Arrays.asList(
                        "Type your password to see the picture",
                        "Close the page immediately without entering any information",
                        "Forward the link to friends"
                ),
                1,
                "Spot on! Scammers create fake login pages that look almost identical to real apps. If you enter your password, they can steal your account.",
                Arrays.asList(
                        "Always look at the website address bar carefully",
                        "Never log in through links sent by unknown people",
                        "Ask a trusted adult if a web page looks suspicious"
                ),
                completedSet.contains("lesson_links")
        ));

        allLessons.add(new Lesson(
                "lesson_passwords",
                "Passwords, OTPs & Secrets",
                "Why 4-digit and 6-digit codes must NEVER be shared",
                "🔑",
                "Someone messaging you says: 'I sent an accidental verification code to your phone. Can you tell me the 6 digits so I can get my account back?'",
                Arrays.asList(
                        "Someone asks for a verification code sent to YOUR phone",
                        "They give a polite or sad story to make you want to help",
                        "The SMS says: 'Do not share this OTP with anyone'"
                ),
                "How should you reply?",
                Arrays.asList(
                        "Share the code because they said it was an accident",
                        "Do not share any code; OTPs are meant only for your device and accounts",
                        "Share only the first 3 digits"
                ),
                1,
                "Great choice! An OTP (One Time Password) is like the key to your digital door. If someone asks for it, they are trying to log into your account or phone.",
                Arrays.asList(
                        "Keep OTPs private, even from friends",
                        "Real support teams never ask for your verification codes",
                        "Tell a parent if someone repeatedly asks for codes"
                ),
                completedSet.contains("lesson_passwords")
        ));

        allLessons.add(new Lesson(
                "lesson_bullying",
                "Bullying & Hurtful Messages",
                "Dealing with mean comments and group chat pressure",
                "🛡️",
                "In a class group chat, someone starts sharing mean stickers and hurtful comments making fun of another student, telling everyone to join in.",
                Arrays.asList(
                        "Repeated hurtful comments or mean pictures",
                        "Group pressure to laugh or participate",
                        "Someone feeling left out or embarrassed"
                ),
                "What is a brave and safe action?",
                Arrays.asList(
                        "Join in so you don't become the next target",
                        "Do not participate; take a screenshot, support the person privately, and tell a teacher or parent",
                        "Reply with angry insults"
                ),
                1,
                "Well done! Online bullying loses its power when people refuse to join in. Supporting the target privately and reporting it stops the harm.",
                Arrays.asList(
                        "Do not forward or react with laughter to mean messages",
                        "Save evidence with a screenshot if safe",
                        "Talk to a school counselor, parent, or trusted adult"
                ),
                completedSet.contains("lesson_bullying")
        ));

        allLessons.add(new Lesson(
                "lesson_pictures",
                "Pressure for Private Photos",
                "Saying NO when anyone asks for personal pictures",
                "📷",
                "Someone you know from an online community asks you to send a private photo of yourself and promises they won't show anyone else.",
                Arrays.asList(
                        "Asking for private or personal photos online",
                        "Making promises of secrecy",
                        "Making you feel guilty or uncool if you say no"
                ),
                "What is the best way to handle this?",
                Arrays.asList(
                        "Send a photo if they promise to delete it",
                        "Say NO firmly, do not send any pictures, and talk to a trusted adult right away",
                        "Ignore it and hope they don't ask again"
                ),
                1,
                "Absolutely right! Once a photo is sent online, you lose control of where it goes. You have the full right to say NO, and adults are here to protect you.",
                Arrays.asList(
                        "Never share personal or private photos with anyone online",
                        "Remember you are never at fault if someone asks",
                        "Reach out to your trusted adult immediately"
                ),
                completedSet.contains("lesson_pictures")
        ));

        allLessons.add(new Lesson(
                "lesson_secrecy",
                "Worrying Secrets & Isolation",
                "When someone says 'keep this our secret'",
                "🤫",
                "An online contact tells you: 'You are so much more mature than your classmates. Let's keep what we talk about our special secret—your parents wouldn't understand.'",
                Arrays.asList(
                        "Trying to separate you from your parents or friends",
                        "Flattery ('you are more mature than others')",
                        "Demanding secrecy about conversations"
                ),
                "Why is this a major warning sign?",
                Arrays.asList(
                        "Because safe adults and friends never ask children to keep secrets from their parents",
                        "It is fine as long as they are nice to you",
                        "Secrets are just part of online friendships"
                ),
                0,
                "Exactly! Safe friends and trustworthy people will never tell you to hide things from your family. Trying to isolate you is a key sign of manipulation.",
                Arrays.asList(
                        "Talk to your parents whenever someone asks you to keep a secret",
                        "You will not get into trouble for telling an adult",
                        "Block people who make you feel uncomfortable"
                ),
                completedSet.contains("lesson_secrecy")
        ));

        allLessons.add(new Lesson(
                "lesson_strangers",
                "Moving to Private Chats",
                "When someone in a public game wants private DMs",
                "💬",
                "You are playing an online multiplayer game. A player says: 'Let's chat on Discord or Snapchat where no one can see our messages. Add me privately.'",
                Arrays.asList(
                        "Trying to move communication from a public game lobby to private channels",
                        "You don't know this person in real life",
                        "They want a platform with disappearing messages"
                ),
                "What should you do?",
                Arrays.asList(
                        "Add them immediately to get gaming tips",
                        "Keep game chats inside the official game, and politely decline moving to private apps",
                        "Give them your personal phone number instead"
                ),
                1,
                "Smart choice! Public game channels have safety filters. Moving to private apps removes those protections. Keep gaming friends in the game.",
                Arrays.asList(
                        "Play with real-life friends or keep chats in public lobbies",
                        "Never give out social media usernames to game strangers",
                        "If someone becomes pushy, use the game's mute or report feature"
                ),
                completedSet.contains("lesson_strangers")
        ));

        allLessons.add(new Lesson(
                "lesson_asking_help",
                "Asking for Help is Smart",
                "You are never in trouble for seeking support",
                "🤝",
                "You accidentally tapped a link that downloaded a strange file or showed something scary on your phone, and you feel nervous about telling anyone.",
                Arrays.asList(
                        "Feeling scared, nervous, or ashamed about an online mistake",
                        "Worrying that you will lose your phone if you tell your parents",
                        "Wondering how to fix it yourself"
                ),
                "What is the most helpful step?",
                Arrays.asList(
                        "Hide your phone under the pillow and don't tell anyone",
                        "Show your parent or trusted adult right away; accidents happen and they can fix it safely",
                        "Try downloading other cleaning apps from unknown sites"
                ),
                1,
                "Spot on! Online mistakes happen to everyone, even adults. Asking for help quickly protects your phone and lets parents fix things with zero blame.",
                Arrays.asList(
                        "Remember: asking for help shows courage and maturity",
                        "Your safety is always more important than a phone setting",
                        "Use the 'I Need Help' button in Saathi Kids anytime"
                ),
                completedSet.contains("lesson_asking_help")
        ));
    }

    public List<Lesson> getAllLessons() {
        return Collections.unmodifiableList(allLessons);
    }

    public int getCompletedLessonCount() {
        int count = 0;
        for (Lesson l : allLessons) {
            if (l.isCompleted()) count++;
        }
        return count;
    }

    public void markLessonCompleted(String lessonId) {
        for (Lesson l : allLessons) {
            if (l.getId().equals(lessonId)) {
                l.setCompleted(true);
                break;
            }
        }
        if (prefs != null) {
            Set<String> set = new HashSet<>(prefs.getStringSet("completed_lessons", new HashSet<>()));
            set.add(lessonId);
            prefs.edit().putStringSet("completed_lessons", set).apply();
            notifyListeners();
        }
    }

    /**
     * Deterministic safety tip tied to an uncompleted lesson or rotating through them.
     */
    public Lesson getFeaturedLesson() {
        for (Lesson l : allLessons) {
            if (!l.isCompleted()) {
                return l;
            }
        }
        return allLessons.get(0);
    }
}
