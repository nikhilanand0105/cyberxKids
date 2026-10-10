package dev.saathi.kids.ui.screens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import dev.saathi.kids.ui.model.CheckResult;
import dev.saathi.kids.ui.model.CheckVerdict;
import dev.saathi.kids.ui.state.KidsAppState;
import dev.saathi.sense.HinglishContextEvaluator;
import dev.saathi.shield.network.PhishingUrlScanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Saathi Kids Check Screen.
 * 3-Tab interactive safety verification engine for:
 * 1. Messages & Chats (scams, bullying, secrecy pressure, OTP theft)
 * 2. Links & URLs (suspicious domains, phishing, fake prize pages)
 * 3. Downloaded Files & APKs (unknown packages, third-party download risks)
 */
public class CheckScreenController {

    public interface CheckNavigationHost {
        void showSosFlow();
        void requestFilePick();
    }

    private final Activity activity;
    private final CheckNavigationHost host;
    private final float density;

    private ScrollView rootView;
    private LinearLayout containerContent;

    // Mode: 0 = Message, 1 = Link, 2 = File
    private int currentMode = 0;

    // Views for Mode Switching
    private TextView tabMessage;
    private TextView tabLink;
    private TextView tabFile;

    // Input fields
    private EditText etInput;
    private LinearLayout containerScenarios;
    private LinearLayout containerResult;

    // Last evaluated result
    private CheckResult lastResult;

    public CheckScreenController(Activity activity, CheckNavigationHost host) {
        this.activity = activity;
        this.host = host;
        this.density = activity.getResources().getDisplayMetrics().density;
        createView();
    }

    public View getView() {
        return rootView;
    }

    public void setMode(int mode) {
        if (mode >= 0 && mode <= 2) {
            this.currentMode = mode;
            updateModeTabs();
            resetInputs();
        }
    }

    public void setPastedText(String text) {
        setMode(0);
        if (etInput != null) {
            etInput.setText(text);
            runCheck(text);
        }
    }

    public void setPastedUrl(String url) {
        setMode(1);
        if (etInput != null) {
            etInput.setText(url);
            runCheck(url);
        }
    }

    private void createView() {
        rootView = new ScrollView(activity);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootView.setFillViewport(true);
        rootView.setBackgroundColor(Color.parseColor("#F7F7F5"));

        containerContent = new LinearLayout(activity);
        containerContent.setOrientation(LinearLayout.VERTICAL);
        containerContent.setPadding(dp(20), dp(20), dp(20), dp(60));

        // 1. Header
        containerContent.addView(buildHeader());

        // 2. Mode Selector (Message / Link / File)
        containerContent.addView(buildModeSelector());

        // 3. Main Input Card
        containerContent.addView(buildInputCard());

        // 4. Quick Scenario Chips (for quick testing)
        containerScenarios = buildScenariosSection();
        containerContent.addView(containerScenarios);

        // 5. Result Container (initially hidden until checked)
        containerResult = new LinearLayout(activity);
        containerResult.setOrientation(LinearLayout.VERTICAL);
        containerResult.setVisibility(View.GONE);
        containerContent.addView(containerResult);

        rootView.addView(containerContent);
        updateModeTabs();
    }

    private View buildHeader() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(4), 0, dp(14));

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Safety Check 🔍");
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        tvTitle.setTextColor(Color.parseColor("#141414"));
        box.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText("Paste a message, test a web link, or check a downloaded file. We'll help you spot warning signs.");
        tvSub.setTextSize(13.5f);
        tvSub.setTextColor(Color.parseColor("#555555"));
        tvSub.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(4);
        box.addView(tvSub, subLp);

        return box;
    }

    private View buildModeSelector() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackground(createCardDrawable(Color.parseColor("#ECECE8"), Color.parseColor("#E0E0DC"), dp(12)));
        row.setPadding(dp(4), dp(4), dp(4), dp(4));

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(16);
        row.setLayoutParams(rowLp);

        tabMessage = createTabButton("💬 Message", 0);
        tabLink = createTabButton("🔗 Link", 1);
        tabFile = createTabButton("📁 File", 2);

        row.addView(tabMessage);
        row.addView(tabLink);
        row.addView(tabFile);

        return row;
    }

    private TextView createTabButton(String title, int mode) {
        TextView tab = new TextView(activity);
        tab.setText(title);
        tab.setTextSize(13f);
        tab.setTypeface(Typeface.DEFAULT_BOLD);
        tab.setGravity(Gravity.CENTER);
        tab.setPadding(0, dp(10), 0, dp(10));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tab.setLayoutParams(lp);

        tab.setOnClickListener(v -> setMode(mode));

        return tab;
    }

    private void updateModeTabs() {
        int activeBg = Color.WHITE;
        int activeText = Color.parseColor("#141414");
        int inactiveText = Color.parseColor("#777777");

        tabMessage.setBackground(currentMode == 0 ? createCardDrawable(activeBg, Color.parseColor("#DCDCD8"), dp(8)) : null);
        tabMessage.setTextColor(currentMode == 0 ? activeText : inactiveText);

        tabLink.setBackground(currentMode == 1 ? createCardDrawable(activeBg, Color.parseColor("#DCDCD8"), dp(8)) : null);
        tabLink.setTextColor(currentMode == 1 ? activeText : inactiveText);

        tabFile.setBackground(currentMode == 2 ? createCardDrawable(activeBg, Color.parseColor("#DCDCD8"), dp(8)) : null);
        tabFile.setTextColor(currentMode == 2 ? activeText : inactiveText);

        if (etInput != null) {
            if (currentMode == 0) {
                etInput.setHint("Paste a suspicious chat, game message, or text here...");
                etInput.setMinLines(3);
            } else if (currentMode == 1) {
                etInput.setHint("Paste a website link (e.g., https://game-rewards.top)...");
                etInput.setMinLines(1);
            } else {
                etInput.setHint("File name or details (e.g., mod_game_v2.apk)...");
                etInput.setMinLines(2);
            }
        }

        if (containerScenarios != null) {
            containerScenarios.setVisibility(currentMode == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private View buildInputCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        etInput = new EditText(activity);
        etInput.setHint("Paste a message, game chat, or text here...");
        etInput.setTextSize(14f);
        etInput.setTextColor(Color.parseColor("#141414"));
        etInput.setHintTextColor(Color.parseColor("#999999"));
        etInput.setGravity(Gravity.TOP | Gravity.START);
        etInput.setBackground(createCardDrawable(Color.parseColor("#F9F9F8"), Color.parseColor("#E4E4DF"), dp(10)));
        etInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        etInput.setMinLines(3);
        card.addView(etInput);

        // Buttons row
        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams btnRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnRowLp.topMargin = dp(12);

        Button btnCheck = new Button(activity);
        btnCheck.setText("Check Now  →");
        btnCheck.setTextColor(Color.WHITE);
        btnCheck.setTextSize(13.5f);
        btnCheck.setTypeface(Typeface.DEFAULT_BOLD);
        btnCheck.setBackground(createSolidDrawable(Color.parseColor("#141414"), dp(10)));
        LinearLayout.LayoutParams checkLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 2.0f);
        checkLp.rightMargin = dp(8);
        btnCheck.setLayoutParams(checkLp);
        btnCheck.setOnClickListener(v -> {
            String text = etInput.getText() != null ? etInput.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                runCheck(text);
            }
        });
        btnRow.addView(btnCheck);

        Button btnClear = new Button(activity);
        btnClear.setText("Clear");
        btnClear.setTextColor(Color.parseColor("#555555"));
        btnClear.setTextSize(13f);
        btnClear.setTypeface(Typeface.DEFAULT_BOLD);
        btnClear.setBackground(createCardDrawable(Color.parseColor("#F0F0EE"), Color.parseColor("#DCDCD8"), dp(10)));
        LinearLayout.LayoutParams clearLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        btnClear.setLayoutParams(clearLp);
        btnClear.setOnClickListener(v -> resetInputs());
        btnRow.addView(btnClear);

        card.addView(btnRow, btnRowLp);

        // File pick button for File mode
        if (currentMode == 2) {
            Button btnPickFile = new Button(activity);
            btnPickFile.setText("📁  Pick a Downloaded File from Phone");
            btnPickFile.setTextColor(Color.parseColor("#141414"));
            btnPickFile.setTextSize(13f);
            btnPickFile.setTypeface(Typeface.DEFAULT_BOLD);
            btnPickFile.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#DCDCD8"), dp(10)));
            LinearLayout.LayoutParams pickLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pickLp.topMargin = dp(8);
            btnPickFile.setLayoutParams(pickLp);
            btnPickFile.setOnClickListener(v -> {
                if (host != null) host.requestFilePick();
            });
            card.addView(btnPickFile);
        }

        return card;
    }

    private LinearLayout buildScenariosSection() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        box.setLayoutParams(lp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Or try a practice example 💡");
        tvTitle.setTextSize(13f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#555555"));
        box.addView(tvTitle);

        HorizontalScrollView hScroll = new HorizontalScrollView(activity);
        hScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hlp.topMargin = dp(8);

        LinearLayout chipsRow = new LinearLayout(activity);
        chipsRow.setOrientation(LinearLayout.HORIZONTAL);

        chipsRow.addView(createScenarioChip("🎁 Free Robux Scam",
                "Claim 5000 free Robux right now before the event ends! Send your username and password to claim: http://robux-claim.top"));
        chipsRow.addView(createScenarioChip("🔑 Secrecy & OTP Request",
                "Mummy papa ko mat batana, send the 6-digit OTP code received on your phone to unlock secret VIP mode. Don't tell anyone."));
        chipsRow.addView(createScenarioChip("⚡ Mean Message / Bully",
                "You are the worst player ever, nobody in class likes you. Delete your account right now."));
        chipsRow.addView(createScenarioChip("🤫 Move to Telegram & Photo",
                "Telegram pe message kar aur apni photo bhej, secret between us, keep this secret from parents."));
        chipsRow.addView(createScenarioChip("✅ Safe Homework Chat",
                "Hey! Are you done with the science project homework? Let's review it tomorrow at school during study hour."));

        hScroll.addView(chipsRow);
        box.addView(hScroll, hlp);

        return box;
    }

    private View createScenarioChip(String label, String presetText) {
        TextView chip = new TextView(activity);
        chip.setText(label);
        chip.setTextSize(12f);
        chip.setTypeface(Typeface.DEFAULT_BOLD);
        chip.setTextColor(Color.parseColor("#141414"));
        chip.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E0E0DC"), dp(20)));
        chip.setPadding(dp(12), dp(8), dp(12), dp(8));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = dp(8);
        chip.setLayoutParams(lp);

        chip.setOnClickListener(v -> {
            if (etInput != null) {
                etInput.setText(presetText);
                runCheck(presetText);
            }
        });

        return chip;
    }

    public void runCheck(String text) {
        KidsAppState.getInstance().incrementCheckCount();

        if (currentMode == 0) {
            lastResult = evaluateMessage(text);
        } else if (currentMode == 1) {
            lastResult = evaluateLink(text);
        } else {
            lastResult = evaluateFile(text);
        }

        renderResult(lastResult);
    }

    private CheckResult evaluateMessage(String text) {
        String lower = text.toLowerCase();
        HinglishContextEvaluator.ContextEvaluation eval = HinglishContextEvaluator.evaluate(text);

        List<String> warnings = new ArrayList<>();
        CheckVerdict verdict = CheckVerdict.LOOKS_OKAY;
        String summary;
        String whyItMatters;
        List<String> nextSteps = new ArrayList<>();

        // Secrecy or platform hopping
        if (eval.hasSecrecy) {
            warnings.add("Demands secrecy ('don't tell anyone', 'mummy papa ko mat batana')");
        }
        if (eval.hasPlatformHopping) {
            warnings.add("Asks to switch to private channels (Telegram, Snapchat, private room)");
        }
        if (eval.hasGamingScam) {
            warnings.add("Promises free game currency (Robux, diamonds, redeem code)");
        }
        if (eval.hasPrivacyInvasion) {
            warnings.add("Asks for camera, private photo, or private video call");
        }

        // OTP / Password theft
        boolean asksPassword = lower.contains("password") || lower.contains("login") || lower.contains("credentials");
        boolean asksOtp = lower.contains("otp") || lower.contains("pin") || lower.contains("code");
        if (asksPassword || asksOtp) {
            warnings.add("Asks for secret login credentials, passwords, or one-time codes (OTP)");
        }

        // Bullying / Harassment
        boolean isMean = lower.contains("worst player") || lower.contains("nobody likes you") ||
                lower.contains("hate you") || lower.contains("loser") || lower.contains("delete your account");
        if (isMean) {
            warnings.add("Contains hurtful, mean, or bullying language intended to make you feel bad");
        }

        if (asksPassword || asksOtp || eval.hasPrivacyInvasion) {
            verdict = CheckVerdict.WARNING_SIGNS;
            summary = "This message is asking for sensitive private information or pressuring you into dangerous secrets.";
            whyItMatters = "Real games, teachers, and companies never ask for your password or SMS codes. Anyone asking you to keep secrets from your parents is not following safe boundaries.";
            nextSteps.add("Do not send any passwords, OTP codes, or photos");
            nextSteps.add("Do not feel guilty or scared—you did nothing wrong");
            nextSteps.add("Show this message to a parent, elder sibling, or trusted teacher right away");
            nextSteps.add("Block the sender and report them in the app");
        } else if (eval.hasSecrecy || eval.hasGamingScam || eval.hasPlatformHopping || isMean) {
            verdict = CheckVerdict.BE_CAREFUL;
            summary = "There are red flags in this message that you should be careful about.";
            whyItMatters = "Scammers often use fake rewards or emotional pressure to trick people. Free item generators almost always steal accounts.";
            nextSteps.add("Pause before replying or clicking anything");
            nextSteps.add("Remember that genuine friends don't demand dangerous secrets");
            nextSteps.add("Check with a trusted adult before taking any action");
        } else if (eval.hasBenignCounterSignal || lower.contains("homework") || lower.contains("project") || lower.contains("school")) {
            verdict = CheckVerdict.LOOKS_OKAY;
            summary = "This looks like a regular, friendly school or personal conversation.";
            whyItMatters = "No scam words, secrecy pressure, or suspicious requests were detected.";
            nextSteps.add("You can continue the conversation normally");
            nextSteps.add("If things ever turn strange later, you can always check again");
        } else {
            verdict = CheckVerdict.CANNOT_TELL;
            summary = "There are no obvious scam keywords, but context matters most.";
            whyItMatters = "We can check text patterns, but how the message makes you feel is what counts.";
            nextSteps.add("If something feels strange or gives you an uncomfortable feeling in your gut, ask a trusted adult");
            nextSteps.add("Never share personal details like your address, school name, or passwords");
        }

        return new CheckResult(
                verdict,
                summary,
                warnings,
                whyItMatters,
                nextSteps,
                false,
                "This check is an on-device helper tool, not a guarantee. If anything feels uncomfortable, talk to an adult you trust."
        );
    }

    private CheckResult evaluateLink(String url) {
        String lower = url.toLowerCase().trim();
        List<String> warnings = new ArrayList<>();
        CheckVerdict verdict = CheckVerdict.LOOKS_OKAY;
        String summary;
        String whyItMatters;
        List<String> nextSteps = new ArrayList<>();

        boolean hasHttpOnly = lower.startsWith("http://");
        boolean hasSuspiciousTld = lower.contains(".top") || lower.contains(".xyz") ||
                lower.contains(".buzz") || lower.contains(".click") || lower.contains(".tk") || lower.contains(".gq");
        boolean hasScamWords = lower.contains("robux") || lower.contains("free-skin") ||
                lower.contains("login") || lower.contains("verify") || lower.contains("claim") || lower.contains("gift");
        boolean isRawIp = lower.matches(".*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b.*");

        if (hasSuspiciousTld) {
            warnings.add("Uses an uncommon domain extension often seen in temporary scam links (.top, .xyz)");
        }
        if (isRawIp) {
            warnings.add("Direct numbers instead of an official web address (Raw IP address)");
        }
        if (hasScamWords) {
            warnings.add("Web link contains keywords common in fake login and giveaway scams");
        }
        if (hasHttpOnly) {
            warnings.add("Not secure (HTTP instead of HTTPS; connection is unencrypted)");
        }

        if (hasSuspiciousTld || isRawIp || (hasScamWords && !lower.contains("google.com") && !lower.contains("roblox.com"))) {
            verdict = CheckVerdict.WARNING_SIGNS;
            summary = "This web link has multiple warning signs of a fake website or phishing trap.";
            whyItMatters = "Scammers set up fake websites that look like real games to steal passwords and phone numbers.";
            nextSteps.add("Do not open the link or enter any login info on that page");
            nextSteps.add("Never type your gaming account password on external links");
            nextSteps.add("Show the link to an adult if a friend sent it to you");
        } else if (hasHttpOnly || lower.length() > 100) {
            verdict = CheckVerdict.BE_CAREFUL;
            summary = "Be cautious with this link. It does not use secure encryption or has unusual length.";
            whyItMatters = "Always make sure a website is verified before typing anything private.";
            nextSteps.add("Only visit websites that start with https://");
            nextSteps.add("If you are not sure who sent this link, don't tap it");
        } else {
            verdict = CheckVerdict.LOOKS_OKAY;
            summary = "No obvious fake scam patterns or deceptive address formats were found.";
            whyItMatters = "The address structure appears normal, but remember to never share passwords on the web.";
            nextSteps.add("You can visit the page if it's from a person or website you trust");
            nextSteps.add("If the website asks for your password or phone number, close it immediately");
        }

        return new CheckResult(
                verdict,
                summary,
                warnings,
                whyItMatters,
                nextSteps,
                false,
                "Local heuristic check only. Live web reputation was not checked."
        );
    }

    private CheckResult evaluateFile(String fileName) {
        String lower = fileName.toLowerCase().trim();
        List<String> warnings = new ArrayList<>();
        CheckVerdict verdict;
        String summary;
        String whyItMatters;
        List<String> nextSteps = new ArrayList<>();

        boolean isApk = lower.endsWith(".apk") || lower.contains(".apk");
        boolean isExecutable = lower.endsWith(".exe") || lower.endsWith(".bat") || lower.endsWith(".zip");
        boolean isModded = lower.contains("mod") || lower.contains("hack") || lower.contains("unlimited") || lower.contains("crack");

        if (isApk) {
            warnings.add("Android App Package (.apk) downloaded from outside Google Play");
        }
        if (isModded) {
            warnings.add("Promoted as a modified or hacked game file (high malware risk)");
        }
        if (isExecutable) {
            warnings.add("Archive or executable file type");
        }

        if (isApk || isModded) {
            verdict = CheckVerdict.WARNING_SIGNS;
            summary = "This is an app installer from the internet. Installing unknown apps carries serious security risks.";
            whyItMatters = "Apps downloaded from web browsers bypass Google Play's built-in security scans. They can contain hidden tools that steal your photos, read your messages, or slow down your phone.";
            nextSteps.add("Do not tap 'Install' or give it permission to install unknown apps");
            nextSteps.add("Delete the downloaded file from your Downloads folder");
            nextSteps.add("Only download apps through the official Google Play Store");
            nextSteps.add("Show the download to a parent or adult so they can help you inspect it");
        } else {
            verdict = CheckVerdict.BE_CAREFUL;
            summary = "Always be cautious with files downloaded from unknown web pages or group chats.";
            whyItMatters = "Files sent by strangers can be disguised to look innocent.";
            nextSteps.add("Make sure you know who sent the file");
            nextSteps.add("Check with an adult if you didn't specifically search for this download");
        }

        return new CheckResult(
                verdict,
                summary,
                warnings,
                whyItMatters,
                nextSteps,
                false,
                "On-device file extension inspection. Unknown packages should never be installed without adult guidance."
        );
    }

    private void renderResult(CheckResult res) {
        if (containerResult == null || res == null) return;

        containerResult.removeAllViews();
        containerResult.setVisibility(View.VISIBLE);

        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(20), dp(20), dp(20));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(8);
        card.setLayoutParams(lp);

        // 1. Verdict Badge
        CheckVerdict v = res.getVerdict();
        LinearLayout badgeBox = new LinearLayout(activity);
        badgeBox.setOrientation(LinearLayout.HORIZONTAL);
        badgeBox.setGravity(Gravity.CENTER_VERTICAL);
        badgeBox.setBackground(createCardDrawable(
                Color.parseColor(v.getBgColorHex()),
                Color.parseColor(v.getTextColorHex()),
                dp(12)
        ));
        badgeBox.setPadding(dp(14), dp(8), dp(14), dp(8));
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.bottomMargin = dp(14);

        TextView tvEmoji = new TextView(activity);
        tvEmoji.setText(v.getBadgeEmoji());
        tvEmoji.setTextSize(16f);
        tvEmoji.setPadding(0, 0, dp(8), 0);
        badgeBox.addView(tvEmoji);

        TextView tvVerdict = new TextView(activity);
        tvVerdict.setText(v.getLabel());
        tvVerdict.setTextSize(14f);
        tvVerdict.setTypeface(Typeface.DEFAULT_BOLD);
        tvVerdict.setTextColor(Color.parseColor(v.getTextColorHex()));
        badgeBox.addView(tvVerdict);

        card.addView(badgeBox, badgeLp);

        // 2. Summary
        TextView tvSummary = new TextView(activity);
        tvSummary.setText(res.getSummary());
        tvSummary.setTextSize(15f);
        tvSummary.setTypeface(Typeface.DEFAULT_BOLD);
        tvSummary.setTextColor(Color.parseColor("#141414"));
        tvSummary.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams sumLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sumLp.bottomMargin = dp(12);
        card.addView(tvSummary, sumLp);

        // 3. Warning Signs (if any)
        if (!res.getWarningSigns().isEmpty()) {
            TextView tvWsTitle = new TextView(activity);
            tvWsTitle.setText("Warning signs detected:");
            tvWsTitle.setTextSize(12.5f);
            tvWsTitle.setTypeface(Typeface.DEFAULT_BOLD);
            tvWsTitle.setTextColor(Color.parseColor("#855A12"));
            LinearLayout.LayoutParams wsTitleLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            wsTitleLp.bottomMargin = dp(6);
            card.addView(tvWsTitle, wsTitleLp);

            for (String sign : res.getWarningSigns()) {
                TextView tvSign = new TextView(activity);
                tvSign.setText("•  " + sign);
                tvSign.setTextSize(13f);
                tvSign.setTextColor(Color.parseColor("#333333"));
                tvSign.setLineSpacing(dp(2), 1.15f);
                LinearLayout.LayoutParams signLp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                signLp.bottomMargin = dp(4);
                signLp.leftMargin = dp(4);
                card.addView(tvSign, signLp);
            }
        }

        // 4. Why It Matters
        TextView tvWhyTitle = new TextView(activity);
        tvWhyTitle.setText("Why this matters:");
        tvWhyTitle.setTextSize(12.5f);
        tvWhyTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvWhyTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams whyTitleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        whyTitleLp.topMargin = dp(10);
        whyTitleLp.bottomMargin = dp(4);
        card.addView(tvWhyTitle, whyTitleLp);

        TextView tvWhy = new TextView(activity);
        tvWhy.setText(res.getWhyItMatters());
        tvWhy.setTextSize(13f);
        tvWhy.setTextColor(Color.parseColor("#555555"));
        tvWhy.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams whyLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        whyLp.bottomMargin = dp(12);
        card.addView(tvWhy, whyLp);

        // 5. Next Steps
        TextView tvStepsTitle = new TextView(activity);
        tvStepsTitle.setText("What you can do right now:");
        tvStepsTitle.setTextSize(12.5f);
        tvStepsTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvStepsTitle.setTextColor(Color.parseColor("#386B57"));
        LinearLayout.LayoutParams stepsTitleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stepsTitleLp.bottomMargin = dp(6);
        card.addView(tvStepsTitle, stepsTitleLp);

        for (String step : res.getNextSteps()) {
            TextView tvStep = new TextView(activity);
            tvStep.setText("✔  " + step);
            tvStep.setTextSize(13f);
            tvStep.setTextColor(Color.parseColor("#222222"));
            tvStep.setLineSpacing(dp(2), 1.15f);
            LinearLayout.LayoutParams stepLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            stepLp.bottomMargin = dp(4);
            stepLp.leftMargin = dp(4);
            card.addView(tvStep, stepLp);
        }

        // 6. Honest Limitation Note
        TextView tvLimitation = new TextView(activity);
        tvLimitation.setText("ℹ️ " + res.getLimitationsNote());
        tvLimitation.setTextSize(11.5f);
        tvLimitation.setTextColor(Color.parseColor("#888888"));
        tvLimitation.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams limLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        limLp.topMargin = dp(12);
        limLp.bottomMargin = dp(14);
        card.addView(tvLimitation, limLp);

        // 7. Action Button: Ask a Trusted Adult
        Button btnAskAdult = new Button(activity);
        btnAskAdult.setText("🚨  Ask a Trusted Adult for Help");
        btnAskAdult.setTextColor(Color.WHITE);
        btnAskAdult.setTextSize(13.5f);
        btnAskAdult.setTypeface(Typeface.DEFAULT_BOLD);
        btnAskAdult.setBackground(createSolidDrawable(Color.parseColor("#A63232"), dp(10)));
        btnAskAdult.setOnClickListener(v1 -> {
            if (host != null) host.showSosFlow();
        });
        card.addView(btnAskAdult);

        containerResult.addView(card);

        // Smooth scroll to result
        rootView.post(() -> rootView.smoothScrollTo(0, card.getTop()));
    }

    private void resetInputs() {
        if (etInput != null) etInput.setText("");
        if (containerResult != null) {
            containerResult.removeAllViews();
            containerResult.setVisibility(View.GONE);
        }
        lastResult = null;
    }

    private int dp(int value) {
        return Math.round(value * density);
    }

    private GradientDrawable createCardDrawable(int bgColor, int strokeColor, int radiusPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setStroke(dp(1), strokeColor);
        gd.setCornerRadius(radiusPx);
        return gd;
    }

    private GradientDrawable createSolidDrawable(int bgColor, int radiusPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(radiusPx);
        return gd;
    }
}
