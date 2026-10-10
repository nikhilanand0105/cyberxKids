package dev.saathi.kids.ui.screens;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import dev.saathi.kids.ui.model.Lesson;
import dev.saathi.kids.ui.state.KidsAppState;

import java.util.List;

/**
 * Saathi Kids Learn Screen.
 * Displays all 8 digital safety scenario quests with completion badges and progress tracking.
 * Tapping any scenario opens LessonDetailOverlay.
 */
public class LearnScreenController {

    public interface LearnNavigationHost {
        void showLessonDetail(Lesson lesson);
    }

    private final Activity activity;
    private final LearnNavigationHost host;
    private final float density;

    private ScrollView rootView;
    private LinearLayout containerLessonsList;
    private TextView tvProgressStats;
    private ProgressBar progressBar;

    public LearnScreenController(Activity activity, LearnNavigationHost host) {
        this.activity = activity;
        this.host = host;
        this.density = activity.getResources().getDisplayMetrics().density;
        createView();
        KidsAppState.getInstance().addListener(this::render);
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
        rootView.setBackgroundColor(Color.parseColor("#F7F7F5"));

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(20), dp(20), dp(60));

        // 1. Header
        layout.addView(buildHeader());

        // 2. Progress Overview Card
        layout.addView(buildProgressCard());

        // 3. Lessons Section Title
        TextView tvSectionTitle = new TextView(activity);
        tvSectionTitle.setText("All Safety Quests (8)");
        tvSectionTitle.setTextSize(14f);
        tvSectionTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvSectionTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        stLp.bottomMargin = dp(12);
        layout.addView(tvSectionTitle, stLp);

        // 4. Lessons List Container
        containerLessonsList = new LinearLayout(activity);
        containerLessonsList.setOrientation(LinearLayout.VERTICAL);
        layout.addView(containerLessonsList);

        rootView.addView(layout);
        render();
    }

    private View buildHeader() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(4), 0, dp(14));

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Safety Quests 🛡️");
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        tvTitle.setTextColor(Color.parseColor("#141414"));
        box.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText("Short, real-world interactive situations. Learn how to spot scam tricks and stay in control.");
        tvSub.setTextSize(13.5f);
        tvSub.setTextColor(Color.parseColor("#555555"));
        tvSub.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(4);
        box.addView(tvSub, subLp);

        return box;
    }

    private View buildProgressCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(20);
        card.setLayoutParams(lp);

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Your Quest Progress");
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        topRow.addView(tvTitle, titleLp);

        tvProgressStats = new TextView(activity);
        tvProgressStats.setTextSize(13f);
        tvProgressStats.setTypeface(Typeface.DEFAULT_BOLD);
        tvProgressStats.setTextColor(Color.parseColor("#386B57"));
        topRow.addView(tvProgressStats);

        card.addView(topRow);

        progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(8);
        progressBar.setProgress(0);
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(8));
        pbLp.topMargin = dp(10);
        pbLp.bottomMargin = dp(8);
        card.addView(progressBar, pbLp);

        TextView tvDesc = new TextView(activity);
        tvDesc.setText("Each quest takes about 2 minutes and teaches one powerful safety habit.");
        tvDesc.setTextSize(12f);
        tvDesc.setTextColor(Color.parseColor("#777777"));
        card.addView(tvDesc);

        return card;
    }

    public void render() {
        KidsAppState state = KidsAppState.getInstance();
        List<Lesson> lessons = state.getAllLessons();

        int completed = state.getCompletedLessonCount();
        int total = lessons.size();

        if (tvProgressStats != null) {
            tvProgressStats.setText(completed + " / " + total + " Finished");
        }
        if (progressBar != null) {
            progressBar.setMax(total);
            progressBar.setProgress(completed);
        }

        if (containerLessonsList != null) {
            containerLessonsList.removeAllViews();
            for (Lesson lesson : lessons) {
                containerLessonsList.addView(buildLessonItemView(lesson));
            }
        }
    }

    private View buildLessonItemView(Lesson lesson) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(14)));
        card.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);

        // Emoji badge
        TextView tvEmoji = new TextView(activity);
        tvEmoji.setText(lesson.getEmoji());
        tvEmoji.setTextSize(26f);
        tvEmoji.setGravity(Gravity.CENTER);
        tvEmoji.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#EAEAE6"), dp(12)));
        tvEmoji.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout.LayoutParams emojiLp = new LinearLayout.LayoutParams(
                dp(48), dp(48));
        emojiLp.rightMargin = dp(14);
        card.addView(tvEmoji, emojiLp);

        // Middle Text Info
        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textColLp.rightMargin = dp(10);
        textCol.setLayoutParams(textColLp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText(lesson.getTitle());
        tvTitle.setTextSize(14.5f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText(lesson.getSubtitle());
        tvSub.setTextSize(12f);
        tvSub.setTextColor(Color.parseColor("#666666"));
        tvSub.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(3);
        subLp.bottomMargin = dp(6);
        textCol.addView(tvSub, subLp);

        // Status badge
        TextView tvStatus = new TextView(activity);
        if (lesson.isCompleted()) {
            tvStatus.setText("COMPLETED ✅");
            tvStatus.setTextColor(Color.parseColor("#386B57"));
            tvStatus.setBackground(createBadgeDrawable(Color.parseColor("#EAF3ED"), dp(8)));
        } else {
            tvStatus.setText("READY TO PLAY 🚀");
            tvStatus.setTextColor(Color.parseColor("#855A12"));
            tvStatus.setBackground(createBadgeDrawable(Color.parseColor("#F8F0DF"), dp(8)));
        }
        tvStatus.setTextSize(10f);
        tvStatus.setTypeface(Typeface.DEFAULT_BOLD);
        tvStatus.setPadding(dp(8), dp(3), dp(8), dp(3));
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textCol.addView(tvStatus, statusLp);

        card.addView(textCol);

        // Arrow icon
        TextView tvArrow = new TextView(activity);
        tvArrow.setText("→");
        tvArrow.setTextSize(18f);
        tvArrow.setTextColor(Color.parseColor("#999999"));
        card.addView(tvArrow);

        card.setOnClickListener(v -> {
            if (host != null) host.showLessonDetail(lesson);
        });

        return card;
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

    private GradientDrawable createBadgeDrawable(int bgColor, int radiusPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(radiusPx);
        return gd;
    }
}
