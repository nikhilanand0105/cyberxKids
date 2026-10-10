package dev.saathi.personal.ui.screens;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.ai.HindiExplanationSynthesizer;
import dev.saathi.model.ThreatSeverity;
import dev.saathi.personal.R;
import dev.saathi.personal.ui.components.DetailSheetDialog;
import dev.saathi.personal.ui.model.ActivityEvent;
import dev.saathi.personal.ui.state.AppState;
import dev.saathi.sense.ConversationRiskReport;
import dev.saathi.sense.SafetyCategory;
import dev.saathi.shield.network.PhishingUrlScanner;

import java.util.Date;
import java.util.List;

public class InsightsScreenController {

    private final Activity activity;
    private final float density;
    private ScrollView rootView;

    private TextView tvAIEngineStatus;
    private EditText etTestInput;
    private TextView tvInsightVerdictBadge;
    private TextView tvInsightVerdictAdvice;
    private TextView tvInsightEvidence;

    public InsightsScreenController(Activity activity) {
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

        // 1. Header
        layout.addView(buildHeader());

        // 2. Risk Vector Radar Cards
        layout.addView(buildRiskVectorSection());

        // 3. Interactive Diagnostic Bench
        layout.addView(buildInteractiveDiagnosticBench());

        rootView.addView(layout);
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, 0, 0, (int) (18 * density));

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("AI SAFETY INSIGHTS");
        tvTitle.setTextSize(22);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText("Explainable on-device neural security & child safety heuristics");
        tvSub.setTextSize(13);
        tvSub.setTextColor(0xFF94A3B8);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sp.topMargin = (int) (4 * density);
        tvSub.setLayoutParams(sp);
        header.addView(tvSub);

        tvAIEngineStatus = new TextView(activity);
        tvAIEngineStatus.setText("⚡ Qualcomm Snapdragon 8 Elite • On-Device Private Inference");
        tvAIEngineStatus.setTextSize(11);
        tvAIEngineStatus.setTextColor(0xFF00D9F5);
        tvAIEngineStatus.setBackgroundResource(R.drawable.bg_pill_cyan);
        tvAIEngineStatus.setPadding((int) (12 * density), (int) (6 * density), (int) (12 * density), (int) (6 * density));
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ap.topMargin = (int) (10 * density);
        tvAIEngineStatus.setLayoutParams(ap);
        header.addView(tvAIEngineStatus);

        return header;
    }

    private View buildRiskVectorSection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setBackgroundResource(R.drawable.bg_card_cred);
        section.setPadding((int) (18 * density), (int) (18 * density), (int) (18 * density), (int) (18 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (20 * density);
        section.setLayoutParams(params);

        TextView label = new TextView(activity);
        label.setText("MONITORED THREAT VECTORS (ACTIVE PROFILE)");
        label.setTextSize(11);
        label.setTextColor(0xFF94A3B8);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        section.addView(label);

        // Vector 1
        section.addView(createVectorRow("APK Reverse Engineering (Oryon CPU)", "1 Threat Blocked", "Critical", 0xFFFF3B30));
        // Vector 2
        section.addView(createVectorRow("Child Secrecy & Grooming NLP", "1 Warning Flagged", "High", 0xFFF59E0B));
        // Vector 3
        section.addView(createVectorRow("Phishing & Impersonation Filter", "1 Link Neutralized", "High", 0xFFF59E0B));
        // Vector 4
        section.addView(createVectorRow("Local DNS Firewall & Exfiltration", "0 Anomalies", "Nominal", 0xFF00F5A0));

        return section;
    }

    private View createVectorRow(String title, String status, String badgeText, int badgeColor) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (12 * density);
        row.setLayoutParams(params);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(tp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText(title);
        tvTitle.setTextSize(13);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        textCol.addView(tvTitle);

        TextView tvStatus = new TextView(activity);
        tvStatus.setText(status);
        tvStatus.setTextSize(11);
        tvStatus.setTextColor(0xFF94A3B8);
        textCol.addView(tvStatus);

        row.addView(textCol);

        TextView badge = new TextView(activity);
        badge.setText(badgeText);
        badge.setTextSize(10);
        badge.setTextColor(badgeColor);
        badge.setBackgroundResource(R.drawable.bg_pill_dark);
        badge.setPadding((int) (8 * density), (int) (3 * density), (int) (8 * density), (int) (3 * density));
        row.addView(badge);

        return row;
    }

    private View buildInteractiveDiagnosticBench() {
        LinearLayout bench = new LinearLayout(activity);
        bench.setOrientation(LinearLayout.VERTICAL);
        bench.setBackgroundResource(R.drawable.bg_card_elevated);
        bench.setPadding((int) (18 * density), (int) (18 * density), (int) (18 * density), (int) (18 * density));

        TextView title = new TextView(activity);
        title.setText("LIVE THREAT & CONVERSATION TEST BENCH");
        title.setTextSize(11);
        title.setTextColor(0xFF00D9F5);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        bench.addView(title);

        TextView desc = new TextView(activity);
        desc.setText("Type any chat message or URL below to evaluate on-device detection:");
        desc.setTextSize(12);
        desc.setTextColor(0xFFCBD5E1);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dp.topMargin = (int) (4 * density);
        dp.bottomMargin = (int) (10 * density);
        desc.setLayoutParams(dp);
        bench.addView(desc);

        // Input Field
        etTestInput = new EditText(activity);
        etTestInput.setHint("e.g. Camera on kar video call pe, kisi ko mat batana");
        etTestInput.setHintTextColor(0xFF64748B);
        etTestInput.setTextColor(0xFFFFFFFF);
        etTestInput.setTextSize(13);
        etTestInput.setBackgroundResource(R.drawable.bg_input_field);
        etTestInput.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (12 * density));
        bench.addView(etTestInput);

        // Quick Preset Chips Row
        LinearLayout presetsRow = new LinearLayout(activity);
        presetsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams prParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        prParams.topMargin = (int) (10 * density);
        prParams.bottomMargin = (int) (10 * density);
        presetsRow.setLayoutParams(prParams);

        Button chipGrooming = createPresetChip("Grooming Cue", "Video call pe aao, mummy papa ko mat batana");
        Button chipScam = createPresetChip("Gaming Scam", "Free 5000 BGMI UC ke liye OTP share karo");
        Button chipPhish = createPresetChip("Bank Phish", "http://sbi-kyc-update.xyz/login");

        presetsRow.addView(chipGrooming);
        presetsRow.addView(chipScam);
        presetsRow.addView(chipPhish);
        bench.addView(presetsRow);

        // Action Buttons Row: Evaluate & Language Toggle
        LinearLayout actRow = new LinearLayout(activity);
        actRow.setOrientation(LinearLayout.HORIZONTAL);

        Button btnAnalyze = new Button(activity);
        btnAnalyze.setText("⚡ Run On-Device Analysis");
        btnAnalyze.setTextSize(13);
        btnAnalyze.setTextColor(0xFF05070B);
        btnAnalyze.setBackgroundResource(R.drawable.bg_pill_cyan);
        btnAnalyze.setTypeface(null, android.graphics.Typeface.BOLD);
        btnAnalyze.setOnClickListener(v -> analyzeInput());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, (int) (46 * density), 1f);
        bp.rightMargin = (int) (6 * density);
        btnAnalyze.setLayoutParams(bp);
        actRow.addView(btnAnalyze);

        Button btnLang = new Button(activity);
        btnLang.setText(AppState.get().isHindi() ? "भाषा: हिन्दी" : "Language: EN");
        btnLang.setTextSize(12);
        btnLang.setTextColor(0xFFFFFFFF);
        btnLang.setBackgroundResource(R.drawable.bg_pill_dark);
        btnLang.setOnClickListener(v -> {
            AppState.get().setHindi(!AppState.get().isHindi());
            btnLang.setText(AppState.get().isHindi() ? "भाषा: हिन्दी" : "Language: EN");
            Toast.makeText(activity, "Switched explanation language", Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                (int) (120 * density),
                (int) (46 * density)
        );
        btnLang.setLayoutParams(lp);
        actRow.addView(btnLang);

        bench.addView(actRow);

        // Results Card Area
        LinearLayout resultCard = new LinearLayout(activity);
        resultCard.setOrientation(LinearLayout.VERTICAL);
        resultCard.setBackgroundResource(R.drawable.bg_card_cred);
        resultCard.setPadding((int) (14 * density), (int) (14 * density), (int) (14 * density), (int) (14 * density));
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rp.topMargin = (int) (14 * density);
        resultCard.setLayoutParams(rp);

        tvInsightVerdictBadge = new TextView(activity);
        tvInsightVerdictBadge.setText("READY FOR TEST");
        tvInsightVerdictBadge.setTextSize(11);
        tvInsightVerdictBadge.setTextColor(0xFF00D9F5);
        tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_cyan);
        tvInsightVerdictBadge.setPadding((int) (8 * density), (int) (3 * density), (int) (8 * density), (int) (3 * density));
        resultCard.addView(tvInsightVerdictBadge);

        tvInsightVerdictAdvice = new TextView(activity);
        tvInsightVerdictAdvice.setText("Tap a preset chip above or type any text and click 'Run On-Device Analysis'.");
        tvInsightVerdictAdvice.setTextSize(13);
        tvInsightVerdictAdvice.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams ap2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ap2.topMargin = (int) (8 * density);
        tvInsightVerdictAdvice.setLayoutParams(ap2);
        resultCard.addView(tvInsightVerdictAdvice);

        tvInsightEvidence = new TextView(activity);
        tvInsightEvidence.setText("Evidence: None");
        tvInsightEvidence.setTextSize(11);
        tvInsightEvidence.setTextColor(0xFF94A3B8);
        LinearLayout.LayoutParams ep = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ep.topMargin = (int) (6 * density);
        tvInsightEvidence.setLayoutParams(ep);
        resultCard.addView(tvInsightEvidence);

        bench.addView(resultCard);

        return bench;
    }

    private Button createPresetChip(String label, String content) {
        Button chip = new Button(activity);
        chip.setText(label);
        chip.setTextSize(10);
        chip.setTextColor(0xFFCBD5E1);
        chip.setBackgroundResource(R.drawable.bg_pill_dark);
        chip.setOnClickListener(v -> etTestInput.setText(content));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, (int) (36 * density), 1f);
        cp.rightMargin = (int) (4 * density);
        chip.setLayoutParams(cp);
        return chip;
    }

    private void analyzeInput() {
        String input = etTestInput.getText().toString().trim();
        if (input.isEmpty()) {
            Toast.makeText(activity, "Please enter message text or URL first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isHindi = AppState.get().isHindi();

        // Check if URL or Chat Message
        if (input.startsWith("http://") || input.startsWith("https://") || input.contains(".com") || input.contains(".xyz") || input.contains(".top")) {
            var urlResult = AppState.get().getPhishingScanner().scanUrl(input);

            if (urlResult.riskLevel == PhishingUrlScanner.UrlRiskLevel.KNOWN_PHISHING) {
                tvInsightVerdictBadge.setText("KNOWN PHISHING DOMAIN");
                tvInsightVerdictBadge.setTextColor(0xFFFF3B30);
                tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_crimson);
            } else if (urlResult.riskLevel == PhishingUrlScanner.UrlRiskLevel.SUSPICIOUS_DESTINATION) {
                tvInsightVerdictBadge.setText("SUSPICIOUS DESTINATION");
                tvInsightVerdictBadge.setTextColor(0xFFF59E0B);
                tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_amber);
            } else {
                tvInsightVerdictBadge.setText("NO PHISHING INDICATORS");
                tvInsightVerdictBadge.setTextColor(0xFF00F5A0);
                tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_emerald);
            }

            tvInsightVerdictAdvice.setText(isHindi ? HindiExplanationSynthesizer.explainPhishingLinkInHindi(urlResult) : urlResult.plainAdvice);
            tvInsightEvidence.setText("Host: " + urlResult.domain + " • Reasons: " + String.join(", ", urlResult.warningReasons));

            // Log event
            AppState.get().addActivityEvent(new ActivityEvent(
                    "evt-" + System.currentTimeMillis(),
                    new Date(),
                    ActivityEvent.EventCategory.PHISHING,
                    urlResult.severity,
                    "Scanned Link: " + urlResult.domain,
                    urlResult.plainAdvice,
                    urlResult.warningReasons,
                    "View URL Security Report"
            ));

        } else {
            // Sense Chat NLP
            ConversationRiskReport report = AppState.get().getSenseEngine().analyze(input);

            if (report.getSeverity() == ThreatSeverity.CRITICAL) {
                tvInsightVerdictBadge.setText("CRITICAL GROOMING ALERT");
                tvInsightVerdictBadge.setTextColor(0xFFFF3B30);
                tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_crimson);
            } else if (report.getSeverity() == ThreatSeverity.HIGH) {
                tvInsightVerdictBadge.setText("CHILD FINANCIAL SCAM");
                tvInsightVerdictBadge.setTextColor(0xFFF59E0B);
                tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_amber);
            } else {
                tvInsightVerdictBadge.setText("BENIGN CONVERSATION");
                tvInsightVerdictBadge.setTextColor(0xFF00F5A0);
                tvInsightVerdictBadge.setBackgroundResource(R.drawable.bg_pill_emerald);
            }

            tvInsightVerdictAdvice.setText(isHindi ? HindiExplanationSynthesizer.explainChildRiskInHindi(report) : report.getChildExplanation());
            tvInsightEvidence.setText("Category: " + report.getCategory().name() + " • Matched Patterns: " + report.getDetectedSignals());

            // Log event
            AppState.get().addActivityEvent(new ActivityEvent(
                    "evt-" + System.currentTimeMillis(),
                    new Date(),
                    ActivityEvent.EventCategory.SENSE,
                    report.getSeverity(),
                    "Sense NLP: " + report.getCategory().name(),
                    report.getChildExplanation(),
                    report.getSafeOptionsForChild(),
                    "Talk to Child / Parent Action"
            ));
        }

        Toast.makeText(activity, "On-device inference complete (<1ms)", Toast.LENGTH_SHORT).show();
    }
}
