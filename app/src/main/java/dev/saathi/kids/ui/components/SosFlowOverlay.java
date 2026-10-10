package dev.saathi.kids.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import dev.saathi.kids.ui.model.SosRequest;
import dev.saathi.kids.ui.model.TrustedAdult;
import dev.saathi.kids.ui.state.KidsAppState;

import java.util.List;

/**
 * 4-Step transparent, child-safe Help / SOS Flow.
 * Allows a child to categorize their worry, select a trusted adult, preview the message,
 * and view truthful confirmation stating that demo mode does not silently message anyone.
 */
public class SosFlowOverlay {

    public interface SosCallback {
        void onDismissed();
    }

    private final Context context;
    private final FrameLayout container;
    private final SosCallback callback;

    private int step = 0;
    private String selectedCategory = "I feel uncomfortable or unsafe";
    private TrustedAdult selectedAdult = null;

    private final String[] categories = new String[]{
            "Someone is being mean to me",
            "Someone is pressuring me",
            "I clicked or shared something by mistake",
            "I feel uncomfortable or unsafe",
            "Something else confusing"
    };

    public SosFlowOverlay(Context context, FrameLayout container, SosCallback callback) {
        this.context = context;
        this.container = container;
        this.callback = callback;
    }

    public void show() {
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);
        step = 0;
        List<TrustedAdult> adults = KidsAppState.getInstance().getTrustedAdults();
        selectedAdult = adults.isEmpty() ? null : adults.get(0);
        renderCurrentStep();
    }

    private void renderCurrentStep() {
        container.removeAllViews();

        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        scroll.setBackgroundColor(Color.parseColor("#F7F7F5"));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(36), dp(24), dp(36));
        scroll.addView(root);

        // Header with Close / Cancel
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("Ask for Help 🤝");
        tvTitle.setTextSize(22f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tvTitle.setLayoutParams(titleLp);
        topBar.addView(tvTitle);

        Button btnClose = new Button(context);
        btnClose.setText("✕");
        btnClose.setTextColor(Color.parseColor("#666666"));
        btnClose.setBackgroundColor(Color.TRANSPARENT);
        btnClose.setTextSize(18f);
        btnClose.setOnClickListener(v -> dismiss());
        topBar.addView(btnClose);

        root.addView(topBar);

        TextView tvReassure = new TextView(context);
        tvReassure.setText("You are never in trouble for asking for help. We are here to support you.");
        tvReassure.setTextSize(14f);
        tvReassure.setTextColor(Color.parseColor("#386B57"));
        tvReassure.setBackgroundColor(Color.parseColor("#EAF3ED"));
        tvReassure.setPadding(dp(12), dp(8), dp(12), dp(8));
        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rLp.setMargins(0, dp(12), 0, dp(20));
        tvReassure.setLayoutParams(rLp);
        root.addView(tvReassure);

        if (step == 0) {
            renderStepCategory(root);
        } else if (step == 1) {
            renderStepAdult(root);
        } else if (step == 2) {
            renderStepPreview(root);
        } else {
            renderStepComplete(root);
        }

        container.addView(scroll);
    }

    private void renderStepCategory(LinearLayout root) {
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText("Step 1 of 3: What happened?");
        tvPrompt.setTextSize(16f);
        tvPrompt.setTypeface(Typeface.DEFAULT_BOLD);
        tvPrompt.setTextColor(Color.parseColor("#141414"));
        tvPrompt.setPadding(0, 0, 0, dp(12));
        root.addView(tvPrompt);

        for (String cat : categories) {
            boolean isSelected = cat.equals(selectedCategory);
            Button btnCat = new Button(context);
            btnCat.setText((isSelected ? "●  " : "○  ") + cat);
            btnCat.setTextSize(14f);
            btnCat.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btnCat.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#141414"));
            btnCat.setBackgroundColor(isSelected ? Color.parseColor("#151515") : Color.WHITE);
            btnCat.setPadding(dp(16), dp(14), dp(16), dp(14));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(8));
            btnCat.setLayoutParams(lp);
            btnCat.setOnClickListener(v -> {
                selectedCategory = cat;
                renderCurrentStep();
            });
            root.addView(btnCat);
        }

        Button btnNext = new Button(context);
        btnNext.setText("NEXT: CHOOSE ADULT →");
        btnNext.setTextColor(Color.WHITE);
        btnNext.setTextSize(14f);
        btnNext.setTypeface(Typeface.DEFAULT_BOLD);
        btnNext.setBackgroundColor(Color.parseColor("#151515"));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        btnLp.setMargins(0, dp(24), 0, 0);
        btnNext.setLayoutParams(btnLp);
        btnNext.setOnClickListener(v -> {
            step = 1;
            renderCurrentStep();
        });
        root.addView(btnNext);
    }

    private void renderStepAdult(LinearLayout root) {
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText("Step 2 of 3: Who do you want to tell?");
        tvPrompt.setTextSize(16f);
        tvPrompt.setTypeface(Typeface.DEFAULT_BOLD);
        tvPrompt.setTextColor(Color.parseColor("#141414"));
        tvPrompt.setPadding(0, 0, 0, dp(12));
        root.addView(tvPrompt);

        List<TrustedAdult> adults = KidsAppState.getInstance().getTrustedAdults();
        for (TrustedAdult adult : adults) {
            boolean isSelected = selectedAdult != null && selectedAdult.getId().equals(adult.getId());
            Button btnAdult = new Button(context);
            btnAdult.setText((isSelected ? "●  " : "○  ") + adult.getName() + " (" + adult.getRelation() + ")");
            btnAdult.setTextSize(14f);
            btnAdult.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btnAdult.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#141414"));
            btnAdult.setBackgroundColor(isSelected ? Color.parseColor("#151515") : Color.WHITE);
            btnAdult.setPadding(dp(16), dp(14), dp(16), dp(14));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(8));
            btnAdult.setLayoutParams(lp);
            btnAdult.setOnClickListener(v -> {
                selectedAdult = adult;
                renderCurrentStep();
            });
            root.addView(btnAdult);
        }

        LinearLayout navRow = new LinearLayout(context);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        rowLp.setMargins(0, dp(24), 0, 0);
        navRow.setLayoutParams(rowLp);

        Button btnBack = new Button(context);
        btnBack.setText("← BACK");
        btnBack.setTextColor(Color.parseColor("#141414"));
        btnBack.setBackgroundColor(Color.WHITE);
        btnBack.setTextSize(14f);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        bLp.setMargins(0, 0, dp(8), 0);
        btnBack.setLayoutParams(bLp);
        btnBack.setOnClickListener(v -> {
            step = 0;
            renderCurrentStep();
        });
        navRow.addView(btnBack);

        Button btnNext = new Button(context);
        btnNext.setText("PREVIEW MESSAGE →");
        btnNext.setTextColor(Color.WHITE);
        btnNext.setBackgroundColor(Color.parseColor("#151515"));
        btnNext.setTextSize(14f);
        btnNext.setTypeface(Typeface.DEFAULT_BOLD);
        btnNext.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.5f));
        btnNext.setOnClickListener(v -> {
            step = 2;
            renderCurrentStep();
        });
        navRow.addView(btnNext);

        root.addView(navRow);
    }

    private void renderStepPreview(LinearLayout root) {
        TextView tvPrompt = new TextView(context);
        tvPrompt.setText("Step 3 of 3: Preview what you will send");
        tvPrompt.setTextSize(16f);
        tvPrompt.setTypeface(Typeface.DEFAULT_BOLD);
        tvPrompt.setTextColor(Color.parseColor("#141414"));
        tvPrompt.setPadding(0, 0, 0, dp(12));
        root.addView(tvPrompt);

        // Preview Message Card
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.WHITE);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        TextView tvTo = new TextView(context);
        String adultName = selectedAdult != null ? selectedAdult.getName() : "Trusted Adult";
        tvTo.setText("To: " + adultName);
        tvTo.setTextSize(14f);
        tvTo.setTypeface(Typeface.DEFAULT_BOLD);
        tvTo.setTextColor(Color.parseColor("#141414"));
        card.addView(tvTo);

        TextView tvMsg = new TextView(context);
        String messageBody = "“Hi " + adultName + ", something happened on my phone (" + selectedCategory + ") and I feel uncomfortable. Can you please sit with me and take a look?”";
        tvMsg.setText(messageBody);
        tvMsg.setTextSize(14f);
        tvMsg.setTextColor(Color.parseColor("#444444"));
        tvMsg.setLineSpacing(dp(2), 1.2f);
        tvMsg.setPadding(0, dp(12), 0, dp(8));
        card.addView(tvMsg);

        root.addView(card);

        // Note
        TextView tvNote = new TextView(context);
        tvNote.setText("Note: In this preview demo, no actual SMS or network call will be triggered.");
        tvNote.setTextSize(12f);
        tvNote.setTextColor(Color.parseColor("#888888"));
        tvNote.setPadding(0, dp(12), 0, dp(20));
        root.addView(tvNote);

        LinearLayout navRow = new LinearLayout(context);
        navRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        navRow.setLayoutParams(rowLp);

        Button btnBack = new Button(context);
        btnBack.setText("← EDIT");
        btnBack.setTextColor(Color.parseColor("#141414"));
        btnBack.setBackgroundColor(Color.WHITE);
        btnBack.setTextSize(14f);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        bLp.setMargins(0, 0, dp(8), 0);
        btnBack.setLayoutParams(bLp);
        btnBack.setOnClickListener(v -> {
            step = 1;
            renderCurrentStep();
        });
        navRow.addView(btnBack);

        Button btnSend = new Button(context);
        btnSend.setText("CONFIRM HELP REQUEST");
        btnSend.setTextColor(Color.WHITE);
        btnSend.setBackgroundColor(Color.parseColor("#386B57"));
        btnSend.setTextSize(14f);
        btnSend.setTypeface(Typeface.DEFAULT_BOLD);
        btnSend.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 2.0f));
        btnSend.setOnClickListener(v -> {
            // Record local SOS demo event
            SosRequest req = new SosRequest(
                    "sos_" + System.currentTimeMillis(),
                    selectedCategory,
                    selectedAdult != null ? selectedAdult.getName() : "Trusted Adult",
                    selectedAdult != null ? selectedAdult.getContactInfo() : "None",
                    messageBody,
                    System.currentTimeMillis(),
                    true
            );
            KidsAppState.getInstance().recordSosRequest(req);
            step = 3;
            renderCurrentStep();
        });
        navRow.addView(btnSend);

        root.addView(navRow);
    }

    private void renderStepComplete(LinearLayout root) {
        TextView tvBadge = new TextView(context);
        tvBadge.setText("REQUEST LOGGED LOCALLY");
        tvBadge.setTextSize(11f);
        tvBadge.setTypeface(Typeface.DEFAULT_BOLD);
        tvBadge.setTextColor(Color.parseColor("#386B57"));
        tvBadge.setBackgroundColor(Color.parseColor("#EAF3ED"));
        tvBadge.setPadding(dp(12), dp(4), dp(12), dp(4));
        root.addView(tvBadge);

        TextView tvDone = new TextView(context);
        tvDone.setText("You took the right step! 🌟");
        tvDone.setTextSize(22f);
        tvDone.setTypeface(Typeface.DEFAULT_BOLD);
        tvDone.setTextColor(Color.parseColor("#141414"));
        tvDone.setPadding(0, dp(12), 0, dp(8));
        root.addView(tvDone);

        // Truthful Demo Notice Card
        LinearLayout noticeCard = new LinearLayout(context);
        noticeCard.setOrientation(LinearLayout.VERTICAL);
        noticeCard.setBackgroundColor(Color.parseColor("#F8F0DF"));
        noticeCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        LinearLayout.LayoutParams nLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        nLp.setMargins(0, dp(8), 0, dp(20));
        noticeCard.setLayoutParams(nLp);

        TextView tvNoticeTitle = new TextView(context);
        tvNoticeTitle.setText("⚠️ Truthful Demo Notice");
        tvNoticeTitle.setTextSize(14f);
        tvNoticeTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvNoticeTitle.setTextColor(Color.parseColor("#855A12"));
        noticeCard.addView(tvNoticeTitle);

        TextView tvNoticeBody = new TextView(context);
        String adultName = selectedAdult != null ? selectedAdult.getName() : "your trusted adult";
        tvNoticeBody.setText("This was a demo request. No one was contacted outside this app.\n\nBecause this is an on-device demonstration, please speak with " + adultName + " directly in person or show them your phone right now.");
        tvNoticeBody.setTextSize(13f);
        tvNoticeBody.setTextColor(Color.parseColor("#654810"));
        tvNoticeBody.setPadding(0, dp(8), 0, 0);
        noticeCard.addView(tvNoticeBody);

        root.addView(noticeCard);

        // Action: Close
        Button btnFinish = new Button(context);
        btnFinish.setText("RETURN TO HOME");
        btnFinish.setTextColor(Color.WHITE);
        btnFinish.setBackgroundColor(Color.parseColor("#151515"));
        btnFinish.setTextSize(14f);
        btnFinish.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
        btnFinish.setLayoutParams(fLp);
        btnFinish.setOnClickListener(v -> dismiss());
        root.addView(btnFinish);
    }

    private void dismiss() {
        container.setVisibility(View.GONE);
        container.removeAllViews();
        if (callback != null) callback.onDismissed();
    }

    private int dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }
}
