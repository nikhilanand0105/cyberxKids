package dev.saathi.kids.ui.screens;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import dev.saathi.kids.ui.model.Lesson;
import dev.saathi.kids.ui.model.NavigationTab;
import dev.saathi.kids.ui.state.KidsAppState;

/**
 * Saathi Kids Home Screen.
 * Calm, welcoming dashboard with quick check entry points, featured safety quest,
 * learning progress, and reassuring SOS help button.
 */
public class HomeScreenController {

    public interface HomeNavigationHost {
        void switchTab(NavigationTab tab, boolean addToStack);
        void showSosFlow();
        void showLessonDetail(Lesson lesson);
        void openCheckMode(int modeIndex); // 0 = message, 1 = link, 2 = file
    }

    private final Activity activity;
    private final HomeNavigationHost host;
    private final float density;

    private ScrollView rootView;
    private TextView tvGreeting;
    private TextView tvProgressStats;
    private ProgressBar progressBar;
    private TextView tvFeaturedTitle;
    private TextView tvFeaturedSubtitle;
    private TextView tvFeaturedStatus;
    private Button btnFeaturedAction;

    public HomeScreenController(Activity activity, HomeNavigationHost host) {
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
        layout.setPadding(dp(20), dp(20), dp(20), dp(40));

        // 1. Top Header
        layout.addView(buildHeader());

        // 2. Primary "Check Something" Hero Card
        layout.addView(buildHeroCheckCard());

        // 3. Prominent Help Card (SOS)
        layout.addView(buildHelpCard());

        // 4. Featured Safety Quest Card
        layout.addView(buildFeaturedQuestCard());

        // 5. Learning Progress Card
        layout.addView(buildLearningProgressCard());

        // 6. Honest Trust & Privacy Indicator
        layout.addView(buildPrivacyFooter());

        rootView.addView(layout);
        render();
    }

    private View buildHeader() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(8), 0, dp(16));

        // Status badge
        TextView tvBadge = new TextView(activity);
        tvBadge.setText("🛡️  ON-DEVICE COMPANION  •  PRIVATE");
        tvBadge.setTextSize(10f);
        tvBadge.setTypeface(Typeface.DEFAULT_BOLD);
        tvBadge.setTextColor(Color.parseColor("#555555"));
        tvBadge.setLetterSpacing(0.08f);
        tvBadge.setBackground(createBadgeDrawable(Color.parseColor("#ECECE8"), dp(12)));
        tvBadge.setPadding(dp(10), dp(4), dp(10), dp(4));
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.bottomMargin = dp(10);
        box.addView(tvBadge, badgeLp);

        // Greeting
        tvGreeting = new TextView(activity);
        tvGreeting.setText("Hi there! 👋");
        tvGreeting.setTextSize(26f);
        tvGreeting.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        tvGreeting.setTextColor(Color.parseColor("#141414"));
        box.addView(tvGreeting);

        // Subtitle
        TextView tvSub = new TextView(activity);
        tvSub.setText("Check something. Understand it. Know what to do next.");
        tvSub.setTextSize(14f);
        tvSub.setTextColor(Color.parseColor("#555555"));
        tvSub.setLineSpacing(dp(2), 1.1f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(4);
        box.addView(tvSub, subLp);

        return box;
    }

    private View buildHeroCheckCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(20), dp(20), dp(20));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Check Something Suspicious 🔍");
        tvTitle.setTextSize(18f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        card.addView(tvTitle);

        TextView tvDesc = new TextView(activity);
        tvDesc.setText("Got an odd message, game reward link, or strange downloaded file? Test it safely right here.");
        tvDesc.setTextSize(13f);
        tvDesc.setTextColor(Color.parseColor("#666666"));
        tvDesc.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = dp(6);
        descLp.bottomMargin = dp(14);
        card.addView(tvDesc, descLp);

        // 3 Quick Check Mode Pills
        LinearLayout pillsRow = new LinearLayout(activity);
        pillsRow.setOrientation(LinearLayout.HORIZONTAL);
        pillsRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams pillsLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pillsLp.bottomMargin = dp(14);

        View pillMsg = createQuickPill("💬  Message", 0);
        View pillLink = createQuickPill("🔗  Link", 1);
        View pillFile = createQuickPill("📁  File", 2);

        pillsRow.addView(pillMsg);
        pillsRow.addView(pillLink);
        pillsRow.addView(pillFile);
        card.addView(pillsRow, pillsLp);

        // Primary Action Button
        Button btnCheck = new Button(activity);
        btnCheck.setText("Start a Check Now  →");
        btnCheck.setTextColor(Color.WHITE);
        btnCheck.setTextSize(14f);
        btnCheck.setTypeface(Typeface.DEFAULT_BOLD);
        btnCheck.setBackground(createSolidDrawable(Color.parseColor("#141414"), dp(10)));
        btnCheck.setOnClickListener(v -> {
            if (host != null) host.switchTab(NavigationTab.CHECK, true);
        });
        card.addView(btnCheck);

        return card;
    }

    private View createQuickPill(String title, int mode) {
        TextView pill = new TextView(activity);
        pill.setText(title);
        pill.setTextSize(12f);
        pill.setTypeface(Typeface.DEFAULT_BOLD);
        pill.setTextColor(Color.parseColor("#141414"));
        pill.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#E0E0DC"), dp(20)));
        pill.setPadding(dp(12), dp(8), dp(12), dp(8));
        pill.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        lp.rightMargin = mode < 2 ? dp(8) : 0;
        pill.setLayoutParams(lp);

        pill.setOnClickListener(v -> {
            if (host != null) host.openCheckMode(mode);
        });

        return pill;
    }

    private View buildHelpCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        // Soft crimson-tinted CRED card
        card.setBackground(createCardDrawable(Color.parseColor("#FFF8F8"), Color.parseColor("#F0D6D6"), dp(16)));
        card.setPadding(dp(18), dp(18), dp(18), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvSosEmoji = new TextView(activity);
        tvSosEmoji.setText("🚨");
        tvSosEmoji.setTextSize(22f);
        LinearLayout.LayoutParams emojiLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        emojiLp.rightMargin = dp(10);
        topRow.addView(tvSosEmoji, emojiLp);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Need Help or Feeling Unsafe?");
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#A63232"));
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText("No trouble, no blame. Reach someone you trust.");
        tvSub.setTextSize(12f);
        tvSub.setTextColor(Color.parseColor("#774444"));
        textCol.addView(tvSub);

        topRow.addView(textCol);
        card.addView(topRow);

        Button btnSos = new Button(activity);
        btnSos.setText("I Need Help Right Now");
        btnSos.setTextColor(Color.WHITE);
        btnSos.setTextSize(14f);
        btnSos.setTypeface(Typeface.DEFAULT_BOLD);
        btnSos.setBackground(createSolidDrawable(Color.parseColor("#A63232"), dp(10)));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.topMargin = dp(12);
        btnSos.setLayoutParams(btnLp);

        btnSos.setOnClickListener(v -> {
            if (host != null) host.showSosFlow();
        });

        card.addView(btnSos);

        return card;
    }

    private View buildFeaturedQuestCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        TextView tvTag = new TextView(activity);
        tvTag.setText("DAILY SAFETY QUEST 🎯");
        tvTag.setTextSize(10f);
        tvTag.setTypeface(Typeface.DEFAULT_BOLD);
        tvTag.setTextColor(Color.parseColor("#386B57"));
        tvTag.setLetterSpacing(0.06f);
        card.addView(tvTag);

        tvFeaturedTitle = new TextView(activity);
        tvFeaturedTitle.setTextSize(16f);
        tvFeaturedTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvFeaturedTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.topMargin = dp(4);
        card.addView(tvFeaturedTitle, titleLp);

        tvFeaturedSubtitle = new TextView(activity);
        tvFeaturedSubtitle.setTextSize(12.5f);
        tvFeaturedSubtitle.setTextColor(Color.parseColor("#666666"));
        tvFeaturedSubtitle.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(4);
        subLp.bottomMargin = dp(12);
        card.addView(tvFeaturedSubtitle, subLp);

        LinearLayout btmRow = new LinearLayout(activity);
        btmRow.setOrientation(LinearLayout.HORIZONTAL);
        btmRow.setGravity(Gravity.CENTER_VERTICAL);

        tvFeaturedStatus = new TextView(activity);
        tvFeaturedStatus.setTextSize(11.5f);
        tvFeaturedStatus.setTypeface(Typeface.DEFAULT_BOLD);
        tvFeaturedStatus.setTextColor(Color.parseColor("#888888"));
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        btmRow.addView(tvFeaturedStatus, statusLp);

        btnFeaturedAction = new Button(activity);
        btnFeaturedAction.setText("Start Quest →");
        btnFeaturedAction.setTextSize(12f);
        btnFeaturedAction.setTypeface(Typeface.DEFAULT_BOLD);
        btnFeaturedAction.setTextColor(Color.parseColor("#141414"));
        btnFeaturedAction.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#E0E0DC"), dp(8)));
        btnFeaturedAction.setPadding(dp(14), dp(6), dp(14), dp(6));
        btmRow.addView(btnFeaturedAction);

        card.addView(btmRow);

        card.setOnClickListener(v -> {
            Lesson featured = KidsAppState.getInstance().getFeaturedLesson();
            if (host != null && featured != null) host.showLessonDetail(featured);
        });

        btnFeaturedAction.setOnClickListener(v -> {
            Lesson featured = KidsAppState.getInstance().getFeaturedLesson();
            if (host != null && featured != null) host.showLessonDetail(featured);
        });

        return card;
    }

    private View buildLearningProgressCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Safety Skills Progress 📚");
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        topRow.addView(tvTitle, titleLp);

        tvProgressStats = new TextView(activity);
        tvProgressStats.setTextSize(12.5f);
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
        pbLp.bottomMargin = dp(10);
        card.addView(progressBar, pbLp);

        TextView tvDesc = new TextView(activity);
        tvDesc.setText("Explore real-world challenges about fake Robux, unknown links, bullying, and secrets.");
        tvDesc.setTextSize(12.5f);
        tvDesc.setTextColor(Color.parseColor("#666666"));
        tvDesc.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.bottomMargin = dp(12);
        card.addView(tvDesc, descLp);

        Button btnViewAll = new Button(activity);
        btnViewAll.setText("Explore All 8 Quests  →");
        btnViewAll.setTextSize(13f);
        btnViewAll.setTypeface(Typeface.DEFAULT_BOLD);
        btnViewAll.setTextColor(Color.parseColor("#141414"));
        btnViewAll.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#E0E0DC"), dp(8)));
        btnViewAll.setOnClickListener(v -> {
            if (host != null) host.switchTab(NavigationTab.LEARN, true);
        });
        card.addView(btnViewAll);

        return card;
    }

    private View buildPrivacyFooter() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(10), dp(10), dp(10), dp(20));

        TextView tvPrivacy = new TextView(activity);
        tvPrivacy.setText("🔒 Saathi Kids runs privately on this phone.\nWe do not read chats automatically or track where you go.");
        tvPrivacy.setTextSize(11.5f);
        tvPrivacy.setTextColor(Color.parseColor("#888888"));
        tvPrivacy.setGravity(Gravity.CENTER);
        tvPrivacy.setLineSpacing(dp(2), 1.2f);
        box.addView(tvPrivacy);

        return box;
    }

    public void render() {
        KidsAppState state = KidsAppState.getInstance();

        // 1. Progress stats
        int completed = state.getCompletedLessonCount();
        int total = state.getAllLessons().size();
        if (tvProgressStats != null) {
            tvProgressStats.setText(completed + " of " + total + " completed");
        }
        if (progressBar != null) {
            progressBar.setMax(total);
            progressBar.setProgress(completed);
        }

        // 2. Featured Quest
        Lesson featured = state.getFeaturedLesson();
        if (featured != null) {
            if (tvFeaturedTitle != null) {
                tvFeaturedTitle.setText(featured.getEmoji() + "  " + featured.getTitle());
            }
            if (tvFeaturedSubtitle != null) {
                tvFeaturedSubtitle.setText(featured.getSubtitle());
            }
            if (tvFeaturedStatus != null) {
                if (featured.isCompleted()) {
                    tvFeaturedStatus.setText("✅ Completed");
                    tvFeaturedStatus.setTextColor(Color.parseColor("#386B57"));
                    if (btnFeaturedAction != null) btnFeaturedAction.setText("Review Quest →");
                } else {
                    tvFeaturedStatus.setText("⏳ Ready to play");
                    tvFeaturedStatus.setTextColor(Color.parseColor("#855A12"));
                    if (btnFeaturedAction != null) btnFeaturedAction.setText("Start Quest →");
                }
            }
        }
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

    private GradientDrawable createSolidDrawable(int bgColor, int radiusPx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(radiusPx);
        return gd;
    }
}
