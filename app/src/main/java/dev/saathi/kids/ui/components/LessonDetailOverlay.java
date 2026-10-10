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

import dev.saathi.kids.ui.model.Lesson;
import dev.saathi.kids.ui.state.KidsAppState;

import java.util.List;

/**
 * Interactive Lesson Detail Screen.
 * Provides scenario walkthrough, interactive multiple-choice question, supportive feedback,
 * and completion action.
 */
public class LessonDetailOverlay {

    public interface LessonCallback {
        void onClosed();
    }

    private final Context context;
    private final FrameLayout container;
    private final Lesson lesson;
    private final LessonCallback callback;
    private int selectedOption = -1;
    private boolean answerSubmitted = false;

    public LessonDetailOverlay(Context context, FrameLayout container, Lesson lesson, LessonCallback callback) {
        this.context = context;
        this.container = container;
        this.lesson = lesson;
        this.callback = callback;
    }

    public void show() {
        container.removeAllViews();
        container.setVisibility(View.VISIBLE);
        render();
    }

    private void render() {
        container.removeAllViews();

        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        scroll.setBackgroundColor(Color.parseColor("#F7F7F5"));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(36), dp(24), dp(40));
        scroll.addView(root);

        // Header Bar
        LinearLayout topBar = new LinearLayout(context);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvBadge = new TextView(context);
        tvBadge.setText(lesson.isCompleted() ? "COMPLETED" : "SAFETY LESSON");
        tvBadge.setTextSize(10f);
        tvBadge.setTypeface(Typeface.DEFAULT_BOLD);
        tvBadge.setTextColor(Color.parseColor(lesson.isCompleted() ? "#386B57" : "#555555"));
        tvBadge.setBackgroundColor(Color.parseColor(lesson.isCompleted() ? "#EAF3ED" : "#E5E5E2"));
        tvBadge.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        tvBadge.setLayoutParams(bLp);
        topBar.addView(tvBadge);

        Button btnClose = new Button(context);
        btnClose.setText("✕");
        btnClose.setTextColor(Color.parseColor("#666666"));
        btnClose.setBackgroundColor(Color.TRANSPARENT);
        btnClose.setTextSize(18f);
        btnClose.setOnClickListener(v -> dismiss());
        topBar.addView(btnClose);

        root.addView(topBar);

        // Title with Emoji
        TextView tvTitle = new TextView(context);
        tvTitle.setText(lesson.getIconEmoji() + "  " + lesson.getTitle());
        tvTitle.setTextSize(22f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        tvTitle.setPadding(0, dp(14), 0, dp(4));
        root.addView(tvTitle);

        TextView tvSubtitle = new TextView(context);
        tvSubtitle.setText(lesson.getSubtitle());
        tvSubtitle.setTextSize(14f);
        tvSubtitle.setTextColor(Color.parseColor("#666666"));
        tvSubtitle.setPadding(0, 0, 0, dp(18));
        root.addView(tvSubtitle);

        // Scenario Card
        LinearLayout scenarioCard = new LinearLayout(context);
        scenarioCard.setOrientation(LinearLayout.VERTICAL);
        scenarioCard.setBackgroundColor(Color.WHITE);
        scenarioCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, 0, 0, dp(16));
        scenarioCard.setLayoutParams(scLp);

        TextView tvScHeading = new TextView(context);
        tvScHeading.setText("THE SITUATION");
        tvScHeading.setTextSize(11f);
        tvScHeading.setTypeface(Typeface.DEFAULT_BOLD);
        tvScHeading.setTextColor(Color.parseColor("#888888"));
        scenarioCard.addView(tvScHeading);

        TextView tvScenario = new TextView(context);
        tvScenario.setText(lesson.getScenario());
        tvScenario.setTextSize(14f);
        tvScenario.setTextColor(Color.parseColor("#222222"));
        tvScenario.setLineSpacing(dp(2), 1.25f);
        tvScenario.setPadding(0, dp(8), 0, 0);
        scenarioCard.addView(tvScenario);

        root.addView(scenarioCard);

        // Warning Signs to Spot
        LinearLayout signsCard = new LinearLayout(context);
        signsCard.setOrientation(LinearLayout.VERTICAL);
        signsCard.setBackgroundColor(Color.parseColor("#FFF6E5"));
        signsCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        LinearLayout.LayoutParams snLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        snLp.setMargins(0, 0, 0, dp(20));
        signsCard.setLayoutParams(snLp);

        TextView tvSnHeading = new TextView(context);
        tvSnHeading.setText("WARNING SIGNS TO SPOT");
        tvSnHeading.setTextSize(11f);
        tvSnHeading.setTypeface(Typeface.DEFAULT_BOLD);
        tvSnHeading.setTextColor(Color.parseColor("#855A12"));
        signsCard.addView(tvSnHeading);

        for (String sign : lesson.getWarningSigns()) {
            TextView tvSign = new TextView(context);
            tvSign.setText("• " + sign);
            tvSign.setTextSize(13f);
            tvSign.setTextColor(Color.parseColor("#5A3C08"));
            tvSign.setPadding(0, dp(4), 0, 0);
            signsCard.addView(tvSign);
        }

        root.addView(signsCard);

        // Interactive Scenario Question
        TextView tvQHeading = new TextView(context);
        tvQHeading.setText("What would you do?");
        tvQHeading.setTextSize(16f);
        tvQHeading.setTypeface(Typeface.DEFAULT_BOLD);
        tvQHeading.setTextColor(Color.parseColor("#141414"));
        tvQHeading.setPadding(0, 0, 0, dp(12));
        root.addView(tvQHeading);

        List<String> options = lesson.getOptions();
        for (int i = 0; i < options.size(); i++) {
            final int index = i;
            Button btnOption = new Button(context);
            String prefix = (selectedOption == index) ? "●  " : "○  ";
            btnOption.setText(prefix + options.get(i));
            btnOption.setTextSize(13f);
            btnOption.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            btnOption.setPadding(dp(16), dp(12), dp(16), dp(12));

            if (answerSubmitted) {
                if (index == lesson.getSafeOptionIndex()) {
                    btnOption.setTextColor(Color.parseColor("#386B57"));
                    btnOption.setBackgroundColor(Color.parseColor("#EAF3ED"));
                } else if (index == selectedOption) {
                    btnOption.setTextColor(Color.parseColor("#666666"));
                    btnOption.setBackgroundColor(Color.parseColor("#F0F0EE"));
                } else {
                    btnOption.setTextColor(Color.parseColor("#888888"));
                    btnOption.setBackgroundColor(Color.WHITE);
                }
            } else {
                boolean isSelected = (selectedOption == index);
                btnOption.setTextColor(isSelected ? Color.WHITE : Color.parseColor("#141414"));
                btnOption.setBackgroundColor(isSelected ? Color.parseColor("#151515") : Color.WHITE);
            }

            LinearLayout.LayoutParams oLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            oLp.setMargins(0, 0, 0, dp(8));
            btnOption.setLayoutParams(oLp);

            btnOption.setOnClickListener(v -> {
                if (!answerSubmitted) {
                    selectedOption = index;
                    answerSubmitted = true;
                    render();
                }
            });

            root.addView(btnOption);
        }

        // Supportive Feedback Section if answered
        if (answerSubmitted) {
            LinearLayout feedbackCard = new LinearLayout(context);
            feedbackCard.setOrientation(LinearLayout.VERTICAL);
            boolean isSafe = (selectedOption == lesson.getSafeOptionIndex());
            feedbackCard.setBackgroundColor(Color.parseColor(isSafe ? "#EAF3ED" : "#F8F0DF"));
            feedbackCard.setPadding(dp(16), dp(16), dp(16), dp(16));
            LinearLayout.LayoutParams fbLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            fbLp.setMargins(0, dp(8), 0, dp(16));
            feedbackCard.setLayoutParams(fbLp);

            TextView tvFbTitle = new TextView(context);
            tvFbTitle.setText(isSafe ? "✅ Safe Choice!" : "💡 Helpful Tip:");
            tvFbTitle.setTextSize(14f);
            tvFbTitle.setTypeface(Typeface.DEFAULT_BOLD);
            tvFbTitle.setTextColor(Color.parseColor(isSafe ? "#386B57" : "#855A12"));
            feedbackCard.addView(tvFbTitle);

            TextView tvFbBody = new TextView(context);
            tvFbBody.setText(lesson.getFeedbackExplanation());
            tvFbBody.setTextSize(13f);
            tvFbBody.setTextColor(Color.parseColor(isSafe ? "#234B3B" : "#5A3C08"));
            tvFbBody.setLineSpacing(dp(2), 1.2f);
            tvFbBody.setPadding(0, dp(6), 0, 0);
            feedbackCard.addView(tvFbBody);

            root.addView(feedbackCard);

            // Practical Next Steps
            TextView tvStepsHeading = new TextView(context);
            tvStepsHeading.setText("PRACTICAL NEXT STEPS");
            tvStepsHeading.setTextSize(11f);
            tvStepsHeading.setTypeface(Typeface.DEFAULT_BOLD);
            tvStepsHeading.setTextColor(Color.parseColor("#888888"));
            tvStepsHeading.setPadding(0, dp(8), 0, dp(6));
            root.addView(tvStepsHeading);

            for (String step : lesson.getNextSteps()) {
                TextView tvStep = new TextView(context);
                tvStep.setText("• " + step);
                tvStep.setTextSize(13f);
                tvStep.setTextColor(Color.parseColor("#333333"));
                tvStep.setPadding(0, dp(4), 0, 0);
                root.addView(tvStep);
            }

            // Mark Complete Button
            Button btnComplete = new Button(context);
            btnComplete.setText(lesson.isCompleted() ? "LESSON COMPLETED ✓" : "MARK AS COMPLETED ✓");
            btnComplete.setTextColor(Color.WHITE);
            btnComplete.setBackgroundColor(Color.parseColor("#151515"));
            btnComplete.setTextSize(14f);
            btnComplete.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(52));
            cLp.setMargins(0, dp(24), 0, 0);
            btnComplete.setLayoutParams(cLp);

            btnComplete.setOnClickListener(v -> {
                KidsAppState.getInstance().markLessonCompleted(lesson.getId());
                dismiss();
            });
            root.addView(btnComplete);
        }

        container.addView(scroll);
    }

    private void dismiss() {
        container.setVisibility(View.GONE);
        container.removeAllViews();
        if (callback != null) callback.onClosed();
    }

    private int dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }
}
