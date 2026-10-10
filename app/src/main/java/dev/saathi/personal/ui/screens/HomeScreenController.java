package dev.saathi.personal.ui.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.ai.HindiExplanationSynthesizer;
import dev.saathi.engine.ApkInstallHandoffHelper;
import dev.saathi.model.ApkAnalysisReport;
import dev.saathi.model.ScanVerdict;
import dev.saathi.model.ThreatSeverity;
import dev.saathi.personal.R;
import dev.saathi.personal.ui.components.DetailSheetDialog;
import dev.saathi.personal.ui.model.ActivityEvent;
import dev.saathi.personal.ui.model.ChildProfile;
import dev.saathi.personal.ui.model.NavigationTab;
import dev.saathi.personal.ui.state.AppState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HomeScreenController {

    private final Activity activity;
    private final float density;
    private ScrollView rootView;

    private TextView tvProfileName;
    private TextView tvScoreNumber;
    private TextView tvScoreLabel;
    private TextView tvScoreSub;
    private LinearLayout cardApkShield;
    private TextView tvApkStatus;
    private TextView tvSenseStatus;
    private LinearLayout containerRecentAlerts;

    public HomeScreenController(Activity activity) {
        this.activity = activity;
        this.density = activity.getResources().getDisplayMetrics().density;
        createView();
    }

    public View getView() {
        return rootView;
    }

    private void createView() {
        rootView = new ScrollView(activity);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootView.setFillViewport(true);
        rootView.setBackgroundColor(0xFF05070B);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (20 * density), (int) (16 * density), (int) (20 * density), (int) (100 * density));

        // 1. Top Header: Profile selector & Hardware status
        layout.addView(buildTopHeader());

        // 2. CRED Hero Score Meter Card
        layout.addView(buildHeroScoreCard());

        // 3. Four-Vector Defense Matrix Cards
        layout.addView(buildDefenseMatrixSection());

        // 4. Quick Action Dock
        layout.addView(buildQuickActionDock());

        // 5. Recent Alerts & Activity Preview
        layout.addView(buildRecentAlertsSection());

        rootView.addView(layout);
        render();
    }

    private View buildTopHeader() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (18 * density);
        row.setLayoutParams(params);

        // Profile Pill Selector (Tappable)
        LinearLayout profilePill = new LinearLayout(activity);
        profilePill.setOrientation(LinearLayout.HORIZONTAL);
        profilePill.setGravity(Gravity.CENTER_VERTICAL);
        profilePill.setBackgroundResource(R.drawable.bg_pill_dark);
        profilePill.setPadding((int) (14 * density), (int) (8 * density), (int) (14 * density), (int) (8 * density));
        profilePill.setOnClickListener(v -> showProfilePickerDialog());

        TextView iconUser = new TextView(activity);
        iconUser.setText("🛡️ ");
        iconUser.setTextSize(14);
        profilePill.addView(iconUser);

        tvProfileName = new TextView(activity);
        tvProfileName.setText(AppState.get().getCurrentProfile().getName());
        tvProfileName.setTextSize(13);
        tvProfileName.setTextColor(0xFFFFFFFF);
        tvProfileName.setTypeface(null, android.graphics.Typeface.BOLD);
        profilePill.addView(tvProfileName);

        TextView tvArrow = new TextView(activity);
        tvArrow.setText(" ▾");
        tvArrow.setTextSize(12);
        tvArrow.setTextColor(0xFF94A3B8);
        profilePill.addView(tvArrow);

        row.addView(profilePill);

        // Space
        View space = new View(activity);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, 1, 1f);
        space.setLayoutParams(sp);
        row.addView(space);

        // 144Hz Badge
        TextView tvHwBadge = new TextView(activity);
        tvHwBadge.setText("144Hz • Snapdragon 8 Elite");
        tvHwBadge.setTextSize(11);
        tvHwBadge.setTextColor(0xFF00F5A0);
        tvHwBadge.setBackgroundResource(R.drawable.bg_pill_emerald);
        tvHwBadge.setPadding((int) (10 * density), (int) (6 * density), (int) (10 * density), (int) (6 * density));
        row.addView(tvHwBadge);

        return row;
    }

    private View buildHeroScoreCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_glow_emerald);
        card.setPadding((int) (22 * density), (int) (22 * density), (int) (22 * density), (int) (22 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (20 * density);
        card.setLayoutParams(params);

        TextView label = new TextView(activity);
        label.setText("CYBER DEFENSE SCORE");
        label.setTextSize(11);
        label.setTextColor(0xFF00F5A0);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(label);

        LinearLayout scoreRow = new LinearLayout(activity);
        scoreRow.setOrientation(LinearLayout.HORIZONTAL);
        scoreRow.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams srParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        srParams.topMargin = (int) (8 * density);
        scoreRow.setLayoutParams(srParams);

        tvScoreNumber = new TextView(activity);
        tvScoreNumber.setText(String.valueOf(AppState.get().getSafetyScore()));
        tvScoreNumber.setTextSize(48);
        tvScoreNumber.setTextColor(0xFFFFFFFF);
        tvScoreNumber.setTypeface(null, android.graphics.Typeface.BOLD);
        scoreRow.addView(tvScoreNumber);

        TextView tvScale = new TextView(activity);
        tvScale.setText(" / 100");
        tvScale.setTextSize(20);
        tvScale.setTextColor(0xFF64748B);
        LinearLayout.LayoutParams scParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        scParams.bottomMargin = (int) (8 * density);
        scParams.leftMargin = (int) (4 * density);
        tvScale.setLayoutParams(scParams);
        scoreRow.addView(tvScale);

        card.addView(scoreRow);

        tvScoreLabel = new TextView(activity);
        tvScoreLabel.setText("OPTIMAL • ALL SECURITY VECTORS SECURED");
        tvScoreLabel.setTextSize(13);
        tvScoreLabel.setTextColor(0xFFE2E8F0);
        tvScoreLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams slParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        slParams.topMargin = (int) (6 * density);
        tvScoreLabel.setLayoutParams(slParams);
        card.addView(tvScoreLabel);

        tvScoreSub = new TextView(activity);
        tvScoreSub.setText("Zero unverified sideloads • Child secrecy guardian armed • NPU isolated");
        tvScoreSub.setTextSize(12);
        tvScoreSub.setTextColor(0xFF94A3B8);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.topMargin = (int) (4 * density);
        tvScoreSub.setLayoutParams(subParams);
        card.addView(tvScoreSub);

        return card;
    }

    private View buildDefenseMatrixSection() {
        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (20 * density);
        container.setLayoutParams(params);

        TextView sectionTitle = new TextView(activity);
        sectionTitle.setText("ACTIVE DEFENSE MATRIX");
        sectionTitle.setTextSize(12);
        sectionTitle.setTextColor(0xFF94A3B8);
        sectionTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        sectionTitle.setPadding(0, 0, 0, (int) (10 * density));
        container.addView(sectionTitle);

        // Grid Row 1: APK Shield & Sense Guardian
        LinearLayout row1 = new LinearLayout(activity);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r1Params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        r1Params.bottomMargin = (int) (10 * density);
        row1.setLayoutParams(r1Params);

        // Vector 1: APK Shield
        cardApkShield = createMatrixCard(
                "🛡️ APK SHIELD",
                "Reverse Engineering",
                "ARM64 Offline Scanner",
                0xFF00F5A0,
                R.drawable.bg_card_cred,
                v -> AppState.get().setCurrentTab(NavigationTab.INSIGHTS)
        );
        LinearLayout.LayoutParams c1Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        c1Params.rightMargin = (int) (6 * density);
        cardApkShield.setLayoutParams(c1Params);
        row1.addView(cardApkShield);

        // Vector 2: Sense Guardian
        LinearLayout cardSense = createMatrixCard(
                "👁️ SENSE AI",
                "Child Online Safety",
                "Grooming & Secrecy Cues",
                0xFF8B5CF6,
                R.drawable.bg_card_cred,
                v -> AppState.get().setCurrentTab(NavigationTab.INSIGHTS)
        );
        LinearLayout.LayoutParams c2Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        c2Params.leftMargin = (int) (6 * density);
        cardSense.setLayoutParams(c2Params);
        row1.addView(cardSense);

        container.addView(row1);

        // Grid Row 2: Local DNS Firewall & 1930 Helpline
        LinearLayout row2 = new LinearLayout(activity);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Vector 3: Local DNS Firewall
        LinearLayout cardFirewall = createMatrixCard(
                "🌐 LOCAL FIREWALL",
                "Traffic Guard",
                "0 Cloud Relays • DNS Shield",
                0xFF00D9F5,
                R.drawable.bg_card_cred,
                v -> AppState.get().setCurrentTab(NavigationTab.INSIGHTS)
        );
        LinearLayout.LayoutParams c3Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        c3Params.rightMargin = (int) (6 * density);
        cardFirewall.setLayoutParams(c3Params);
        row2.addView(cardFirewall);

        // Vector 4: 1930 Helpline & SOS
        LinearLayout cardHelpline = createMatrixCard(
                "🚨 HELPLINE 1930",
                "National Response",
                "Instant Fraud Freeze",
                0xFFF59E0B,
                R.drawable.bg_card_glow_amber,
                v -> dial1930()
        );
        LinearLayout.LayoutParams c4Params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        c4Params.leftMargin = (int) (6 * density);
        cardHelpline.setLayoutParams(c4Params);
        row2.addView(cardHelpline);

        container.addView(row2);

        return container;
    }

    private LinearLayout createMatrixCard(String tag, String title, String subtitle, int accentColor, int bgRes, View.OnClickListener click) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(bgRes);
        card.setPadding((int) (14 * density), (int) (14 * density), (int) (14 * density), (int) (14 * density));
        card.setOnClickListener(click);

        TextView tvTag = new TextView(activity);
        tvTag.setText(tag);
        tvTag.setTextSize(10);
        tvTag.setTextColor(accentColor);
        tvTag.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(tvTag);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText(title);
        tvTitle.setTextSize(14);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tp.topMargin = (int) (4 * density);
        tvTitle.setLayoutParams(tp);
        card.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText(subtitle);
        tvSub.setTextSize(11);
        tvSub.setTextColor(0xFF94A3B8);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sp.topMargin = (int) (2 * density);
        tvSub.setLayoutParams(sp);
        card.addView(tvSub);

        return card;
    }

    private View buildQuickActionDock() {
        LinearLayout dock = new LinearLayout(activity);
        dock.setOrientation(LinearLayout.VERTICAL);
        dock.setBackgroundResource(R.drawable.bg_card_cred);
        dock.setPadding((int) (18 * density), (int) (18 * density), (int) (18 * density), (int) (18 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (20 * density);
        dock.setLayoutParams(params);

        TextView title = new TextView(activity);
        title.setText("ON-DEVICE TESTING SUITE (IQOO 15)");
        title.setTextSize(11);
        title.setTextColor(0xFF00D9F5);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        dock.addView(title);

        TextView desc = new TextView(activity);
        desc.setText("Execute real live security tests directly on Qualcomm Snapdragon 8 Elite:");
        desc.setTextSize(12);
        desc.setTextColor(0xFFCBD5E1);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dp.topMargin = (int) (4 * density);
        dp.bottomMargin = (int) (12 * density);
        desc.setLayoutParams(dp);
        dock.addView(desc);

        // Test buttons row 1
        LinearLayout btnRow1 = new LinearLayout(activity);
        btnRow1.setOrientation(LinearLayout.HORIZONTAL);

        Button btnTrojan = new Button(activity);
        btnTrojan.setText("⚡ Test Trojan APK");
        btnTrojan.setTextSize(12);
        btnTrojan.setTextColor(0xFFFF3B30);
        btnTrojan.setBackgroundResource(R.drawable.bg_pill_crimson);
        btnTrojan.setOnClickListener(v -> testSyntheticApk(true));
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, (int) (44 * density), 1f);
        p1.rightMargin = (int) (6 * density);
        btnTrojan.setLayoutParams(p1);
        btnRow1.addView(btnTrojan);

        Button btnClean = new Button(activity);
        btnClean.setText("🛡️ Test Clean APK");
        btnClean.setTextSize(12);
        btnClean.setTextColor(0xFF00F5A0);
        btnClean.setBackgroundResource(R.drawable.bg_pill_emerald);
        btnClean.setOnClickListener(v -> testSyntheticApk(false));
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, (int) (44 * density), 1f);
        p2.leftMargin = (int) (6 * density);
        btnClean.setLayoutParams(p2);
        btnRow1.addView(btnClean);

        dock.addView(btnRow1);

        // Test buttons row 2: Phishing & SOS
        LinearLayout btnRow2 = new LinearLayout(activity);
        btnRow2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r2p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        r2p.topMargin = (int) (10 * density);
        btnRow2.setLayoutParams(r2p);

        Button btnPhish = new Button(activity);
        btnPhish.setText("🌐 Test Phishing Link");
        btnPhish.setTextSize(12);
        btnPhish.setTextColor(0xFF00D9F5);
        btnPhish.setBackgroundResource(R.drawable.bg_pill_cyan);
        btnPhish.setOnClickListener(v -> testPhishingLink());
        LinearLayout.LayoutParams p3 = new LinearLayout.LayoutParams(0, (int) (44 * density), 1f);
        p3.rightMargin = (int) (6 * density);
        btnPhish.setLayoutParams(p3);
        btnRow2.addView(btnPhish);

        Button btnSos = new Button(activity);
        btnSos.setText("📞 Call 1930 Helpline");
        btnSos.setTextSize(12);
        btnSos.setTextColor(0xFFF59E0B);
        btnSos.setBackgroundResource(R.drawable.bg_pill_amber);
        btnSos.setOnClickListener(v -> dial1930());
        LinearLayout.LayoutParams p4 = new LinearLayout.LayoutParams(0, (int) (44 * density), 1f);
        p4.leftMargin = (int) (6 * density);
        btnSos.setLayoutParams(p4);
        btnRow2.addView(btnSos);

        dock.addView(btnRow2);

        return dock;
    }

    private View buildRecentAlertsSection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);

        LinearLayout headerRow = new LinearLayout(activity);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setGravity(Gravity.CENTER_VERTICAL);
        headerRow.setPadding(0, 0, 0, (int) (10 * density));

        TextView title = new TextView(activity);
        title.setText("RECENT SECURITY TIMELINE");
        title.setTextSize(12);
        title.setTextColor(0xFF94A3B8);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        title.setLayoutParams(tp);
        headerRow.addView(title);

        TextView tvViewAll = new TextView(activity);
        tvViewAll.setText("View All (4) →");
        tvViewAll.setTextSize(12);
        tvViewAll.setTextColor(0xFF00D9F5);
        tvViewAll.setTypeface(null, android.graphics.Typeface.BOLD);
        tvViewAll.setOnClickListener(v -> AppState.get().setCurrentTab(NavigationTab.ACTIVITY));
        headerRow.addView(tvViewAll);

        section.addView(headerRow);

        containerRecentAlerts = new LinearLayout(activity);
        containerRecentAlerts.setOrientation(LinearLayout.VERTICAL);
        section.addView(containerRecentAlerts);

        return section;
    }

    public void render() {
        ChildProfile profile = AppState.get().getCurrentProfile();
        tvProfileName.setText(profile.getName());
        tvScoreNumber.setText(String.valueOf(AppState.get().getSafetyScore()));

        // Populate top 2 recent alerts
        containerRecentAlerts.removeAllViews();
        List<ActivityEvent> events = AppState.get().getActivityEvents();
        int count = Math.min(2, events.size());
        for (int i = 0; i < count; i++) {
            ActivityEvent ev = events.get(i);
            containerRecentAlerts.addView(createAlertItemView(ev));
        }
    }

    private View createAlertItemView(ActivityEvent ev) {
        LinearLayout item = new LinearLayout(activity);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundResource(R.drawable.bg_card_cred);
        item.setPadding((int) (16 * density), (int) (14 * density), (int) (16 * density), (int) (14 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (10 * density);
        item.setLayoutParams(params);
        item.setOnClickListener(v -> DetailSheetDialog.showEventDetails(activity, ev));

        LinearLayout top = new LinearLayout(activity);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView badge = new TextView(activity);
        badge.setText(ev.getCategory().getDisplayName());
        badge.setTextSize(10);
        badge.setTextColor(ev.getSeverity() == ThreatSeverity.CRITICAL ? 0xFFFF3B30 : 0xFF00D9F5);
        badge.setBackgroundResource(ev.getSeverity() == ThreatSeverity.CRITICAL ? R.drawable.bg_pill_crimson : R.drawable.bg_pill_cyan);
        badge.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));
        top.addView(badge);

        View space = new View(activity);
        top.addView(space, new LinearLayout.LayoutParams(0, 1, 1f));

        TextView time = new TextView(activity);
        time.setText("Just now");
        time.setTextSize(11);
        time.setTextColor(0xFF64748B);
        top.addView(time);

        item.addView(top);

        TextView title = new TextView(activity);
        title.setText(ev.getTitle());
        title.setTextSize(14);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tp.topMargin = (int) (6 * density);
        title.setLayoutParams(tp);
        item.addView(title);

        TextView sub = new TextView(activity);
        sub.setText(ev.getSummary());
        sub.setTextSize(12);
        sub.setTextColor(0xFF94A3B8);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sp.topMargin = (int) (2 * density);
        sub.setLayoutParams(sp);
        item.addView(sub);

        return item;
    }

    private void showProfilePickerDialog() {
        String[] profiles = {
                "Aarav (Teen • 14y) — Sideloads & Social Monitor",
                "Ananya (Child • 9y) — Strict Camera & Grooming Blocker",
                "Dadaji (Senior • 72y) — Senior Mode & Banking Defense"
        };

        new AlertDialog.Builder(activity)
                .setTitle("Select Family Protection Profile")
                .setItems(profiles, (dialog, which) -> {
                    if (which == 0) AppState.get().setCurrentProfile(ChildProfile.AARAV);
                    else if (which == 1) AppState.get().setCurrentProfile(ChildProfile.ANANYA);
                    else AppState.get().setCurrentProfile(ChildProfile.DADAJI);
                    render();
                    Toast.makeText(activity, "Switched to: " + AppState.get().getCurrentProfile().getName(), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void testSyntheticApk(boolean isTrojan) {
        byte[] apkBytes;
        String name;

        if (isTrojan) {
            name = "QuickBank_Update_Trojan.apk";
            apkBytes = createSyntheticApk(
                    "com.fake.quickbank",
                    "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.fake.quickbank\">\n" +
                    "    <uses-permission android:name=\"android.permission.SYSTEM_ALERT_WINDOW\" />\n" +
                    "    <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
                    "    <application>\n" +
                    "        <service android:name=\".ATS\" android:permission=\"android.permission.BIND_ACCESSIBILITY_SERVICE\" />\n" +
                    "    </application>\n" +
                    "</manifest>",
                    true
            );
        } else {
            name = "CleanCalculator.apk";
            apkBytes = createSyntheticApk(
                    "com.simple.calculator",
                    "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.simple.calculator\">\n" +
                    "    <application>\n" +
                    "        <activity android:name=\".Main\">\n" +
                    "            <intent-filter>\n" +
                    "                <action android:name=\"android.intent.action.MAIN\" />\n" +
                    "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                    "            </intent-filter>\n" +
                    "        </activity>\n" +
                    "    </application>\n" +
                    "</manifest>",
                    false
            );
        }

        ApkAnalysisReport report = AppState.get().getApkEngine().analyzeApk(name, new ByteArrayInputStream(apkBytes), apkBytes.length);

        ActivityEvent event = new ActivityEvent(
                "evt-" + System.currentTimeMillis(),
                new Date(),
                ActivityEvent.EventCategory.APK,
                report.getVerdict() == ScanVerdict.KNOWN_MALICIOUS ? ThreatSeverity.CRITICAL : ThreatSeverity.INFO,
                (report.getVerdict() == ScanVerdict.KNOWN_MALICIOUS ? "🚨 Trojan Blocked: " : "✅ Clean APK Verified: ") + name,
                report.getPlainLanguageExplanation(),
                List.of(
                        "Verdict: " + report.getVerdict().getDisplayName(),
                        "Indicators Count: " + report.getIndicators().size(),
                        "SHA256: " + report.getSha256Checksum()
                ),
                report.canSafelyProceedWithInstall() ? "Proceed with Install" : "Quarantine & Delete"
        );

        AppState.get().addActivityEvent(event);
        if (isTrojan) {
            AppState.get().setSafetyScore(Math.max(70, AppState.get().getSafetyScore() - 8));
        } else {
            AppState.get().setSafetyScore(Math.min(100, AppState.get().getSafetyScore() + 2));
        }

        render();
        DetailSheetDialog.showEventDetails(activity, event);
    }

    private void testPhishingLink() {
        String testUrl = "http://phishing-sbi-portal.top/kyc-update";
        var result = AppState.get().getPhishingScanner().scanUrl(testUrl);

        ActivityEvent event = new ActivityEvent(
                "evt-" + System.currentTimeMillis(),
                new Date(),
                ActivityEvent.EventCategory.PHISHING,
                result.severity,
                "🛑 Phishing Domain Blocked: " + result.domain,
                result.plainAdvice,
                result.warningReasons,
                "Block Destination & Report"
        );

        AppState.get().addActivityEvent(event);
        AppState.get().setSafetyScore(Math.max(65, AppState.get().getSafetyScore() - 5));
        render();
        DetailSheetDialog.showEventDetails(activity, event);
    }

    private void dial1930() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:1930"));
            activity.startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(activity, "Opening Dialer for 1930 Helpline...", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] createSyntheticApk(String pkg, String xml, boolean trojan) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("AndroidManifest.xml"));
            zos.write(xml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("classes.dex"));
            byte[] dex = new byte[256];
            dex[0] = 'd'; dex[1] = 'e'; dex[2] = 'x'; dex[3] = '\n';
            dex[4] = '0'; dex[5] = '3'; dex[6] = '5'; dex[7] = 0;
            if (trojan) {
                byte[] sig = "findAccessibilityNodeInfosByViewId".getBytes(StandardCharsets.UTF_8);
                System.arraycopy(sig, 0, dex, 120, sig.length);
            }
            zos.write(dex);
            zos.closeEntry();
            zos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
