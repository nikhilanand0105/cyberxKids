package dev.saathi.personal.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.model.ApkAnalysisReport;
import dev.saathi.model.ScanVerdict;
import dev.saathi.model.ThreatSeverity;
import dev.saathi.personal.R;
import dev.saathi.personal.ui.components.DetailSheetDialog;
import dev.saathi.personal.ui.components.OnboardingOverlay;
import dev.saathi.personal.ui.model.ActivityEvent;
import dev.saathi.personal.ui.model.NavigationTab;
import dev.saathi.personal.ui.screens.ActivityScreenController;
import dev.saathi.personal.ui.screens.AssistantScreenController;
import dev.saathi.personal.ui.screens.GuardianScreenController;
import dev.saathi.personal.ui.screens.HomeScreenController;
import dev.saathi.personal.ui.screens.InsightsScreenController;
import dev.saathi.personal.ui.state.AppState;

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;

public class MainActivity extends Activity {

    private FrameLayout screenContainer;
    private FrameLayout onboardingContainer;

    private LinearLayout navItemHome;
    private LinearLayout navItemInsights;
    private LinearLayout navItemActivity;
    private LinearLayout navItemAssistant;
    private LinearLayout navItemGuardian;

    private ImageView ivNavHome;
    private ImageView ivNavInsights;
    private ImageView ivNavActivity;
    private ImageView ivNavAssistant;
    private ImageView ivNavGuardian;

    private TextView tvNavHome;
    private TextView tvNavInsights;
    private TextView tvNavActivity;
    private TextView tvNavAssistant;
    private TextView tvNavGuardian;

    private HomeScreenController homeController;
    private InsightsScreenController insightsController;
    private ActivityScreenController activityController;
    private AssistantScreenController assistantController;
    private GuardianScreenController guardianController;

    private final Deque<NavigationTab> navigationBackStack = new ArrayDeque<>();
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hardware Optimization: 144Hz LTPO AMOLED sync for iQOO 15 (Snapdragon 8 Elite)
        optimizeDisplayForIqoo15();

        // Enforce Light Status Bar and Light Navigation Bar (Dark Icons)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            int flags = decor.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            decor.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(0xFFF7F7F5);
            getWindow().setNavigationBarColor(0xFFFFFFFF);
        }

        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initViews();
        setupScreenControllers();
        setupNavigation();

        // Listen for centralized state changes (e.g. tab switches from other screens)
        AppState.get().addListener(this::onAppStateChanged);

        // Check if incoming intent (APK shared from Chrome, WhatsApp, Files)
        handleIncomingIntent(getIntent());

        // Check initial tab
        switchTab(NavigationTab.HOME, false);
    }

    private void optimizeDisplayForIqoo15() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                Display display = getDisplay();
                if (display != null) {
                    Display.Mode[] modes = display.getSupportedModes();
                    for (Display.Mode mode : modes) {
                        if (Math.round(mode.getRefreshRate()) == 144) {
                            params.preferredDisplayModeId = mode.getModeId();
                            break;
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                }
                getWindow().setAttributes(params);
            } catch (Exception ignored) {}
        }
    }

    private void initViews() {
        screenContainer = findViewById(R.id.screen_container);
        onboardingContainer = findViewById(R.id.onboarding_container);

        navItemHome = findViewById(R.id.nav_item_home);
        navItemInsights = findViewById(R.id.nav_item_insights);
        navItemActivity = findViewById(R.id.nav_item_activity);
        navItemAssistant = findViewById(R.id.nav_item_assistant);
        navItemGuardian = findViewById(R.id.nav_item_guardian);

        ivNavHome = findViewById(R.id.iv_nav_home);
        ivNavInsights = findViewById(R.id.iv_nav_insights);
        ivNavActivity = findViewById(R.id.iv_nav_activity);
        ivNavAssistant = findViewById(R.id.iv_nav_assistant);
        ivNavGuardian = findViewById(R.id.iv_nav_guardian);

        tvNavHome = findViewById(R.id.tv_nav_home);
        tvNavInsights = findViewById(R.id.tv_nav_insights);
        tvNavActivity = findViewById(R.id.tv_nav_activity);
        tvNavAssistant = findViewById(R.id.tv_nav_assistant);
        tvNavGuardian = findViewById(R.id.tv_nav_guardian);
    }

    private void setupScreenControllers() {
        homeController = new HomeScreenController(this);
        insightsController = new InsightsScreenController(this);
        activityController = new ActivityScreenController(this);
        assistantController = new AssistantScreenController(this);
        guardianController = new GuardianScreenController(this);

        // Add all 5 screens to the container
        screenContainer.addView(homeController.getView());
        screenContainer.addView(insightsController.getView());
        screenContainer.addView(activityController.getView());
        screenContainer.addView(assistantController.getView());
        screenContainer.addView(guardianController.getView());
    }

    private void setupNavigation() {
        navItemHome.setOnClickListener(v -> switchTab(NavigationTab.HOME, true));
        navItemInsights.setOnClickListener(v -> switchTab(NavigationTab.INSIGHTS, true));
        navItemActivity.setOnClickListener(v -> switchTab(NavigationTab.ACTIVITY, true));
        navItemAssistant.setOnClickListener(v -> switchTab(NavigationTab.ASSISTANT, true));
        navItemGuardian.setOnClickListener(v -> switchTab(NavigationTab.GUARDIAN, true));
    }

    public void switchTab(NavigationTab targetTab, boolean addToStack) {
        NavigationTab currentTab = AppState.get().getCurrentTab();
        if (addToStack && currentTab != targetTab) {
            navigationBackStack.push(currentTab);
        }

        AppState.get().setCurrentTab(targetTab);

        // Trigger subtle tactile haptic feedback
        performHaptic();

        // Update Visibility of Screens
        homeController.getView().setVisibility(targetTab == NavigationTab.HOME ? View.VISIBLE : View.GONE);
        insightsController.getView().setVisibility(targetTab == NavigationTab.INSIGHTS ? View.VISIBLE : View.GONE);
        activityController.getView().setVisibility(targetTab == NavigationTab.ACTIVITY ? View.VISIBLE : View.GONE);
        assistantController.getView().setVisibility(targetTab == NavigationTab.ASSISTANT ? View.VISIBLE : View.GONE);
        guardianController.getView().setVisibility(targetTab == NavigationTab.GUARDIAN ? View.VISIBLE : View.GONE);

        // Refresh views
        if (targetTab == NavigationTab.HOME) homeController.render();
        else if (targetTab == NavigationTab.ACTIVITY) activityController.render();
        else if (targetTab == NavigationTab.ASSISTANT) assistantController.render();
        else if (targetTab == NavigationTab.GUARDIAN) guardianController.render();

        // Update Bottom Nav Highlighting
        updateNavHighlight(targetTab);
    }

    private void updateNavHighlight(NavigationTab tab) {
        int activeColor = 0xFF141414; // Solid Monochrome Black
        int inactiveColor = 0xFF888888; // Muted Neutral Grey

        ivNavHome.setColorFilter(tab == NavigationTab.HOME ? activeColor : inactiveColor);
        tvNavHome.setTextColor(tab == NavigationTab.HOME ? activeColor : inactiveColor);
        tvNavHome.setTypeface(null, tab == NavigationTab.HOME ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavInsights.setColorFilter(tab == NavigationTab.INSIGHTS ? activeColor : inactiveColor);
        tvNavInsights.setTextColor(tab == NavigationTab.INSIGHTS ? activeColor : inactiveColor);
        tvNavInsights.setTypeface(null, tab == NavigationTab.INSIGHTS ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavActivity.setColorFilter(tab == NavigationTab.ACTIVITY ? activeColor : inactiveColor);
        tvNavActivity.setTextColor(tab == NavigationTab.ACTIVITY ? activeColor : inactiveColor);
        tvNavActivity.setTypeface(null, tab == NavigationTab.ACTIVITY ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavAssistant.setColorFilter(tab == NavigationTab.ASSISTANT ? activeColor : inactiveColor);
        tvNavAssistant.setTextColor(tab == NavigationTab.ASSISTANT ? activeColor : inactiveColor);
        tvNavAssistant.setTypeface(null, tab == NavigationTab.ASSISTANT ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavGuardian.setColorFilter(tab == NavigationTab.GUARDIAN ? activeColor : inactiveColor);
        tvNavGuardian.setTextColor(tab == NavigationTab.GUARDIAN ? activeColor : inactiveColor);
        tvNavGuardian.setTypeface(null, tab == NavigationTab.GUARDIAN ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void performHaptic() {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(12);
                }
            }
        } catch (Exception ignored) {}
    }

    private void onAppStateChanged() {
        NavigationTab tab = AppState.get().getCurrentTab();
        // Check if tab changed externally
        if (screenContainer != null) {
            homeController.getView().setVisibility(tab == NavigationTab.HOME ? View.VISIBLE : View.GONE);
            insightsController.getView().setVisibility(tab == NavigationTab.INSIGHTS ? View.VISIBLE : View.GONE);
            activityController.getView().setVisibility(tab == NavigationTab.ACTIVITY ? View.VISIBLE : View.GONE);
            assistantController.getView().setVisibility(tab == NavigationTab.ASSISTANT ? View.VISIBLE : View.GONE);
            guardianController.getView().setVisibility(tab == NavigationTab.GUARDIAN ? View.VISIBLE : View.GONE);
            updateNavHighlight(tab);
        }
    }

    @Override
    public void onBackPressed() {
        if (!navigationBackStack.isEmpty()) {
            NavigationTab previous = navigationBackStack.pop();
            switchTab(previous, false);
        } else {
            super.onBackPressed();
        }
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            try {
                InputStream is = getContentResolver().openInputStream(data);
                if (is != null) {
                    ApkAnalysisReport report = AppState.get().getApkEngine().analyzeApk("downloaded_package.apk", is, 0);

                    ActivityEvent event = new ActivityEvent(
                            "evt-" + System.currentTimeMillis(),
                            new Date(),
                            ActivityEvent.EventCategory.APK,
                            report.getVerdict() == ScanVerdict.KNOWN_MALICIOUS ? ThreatSeverity.CRITICAL : ThreatSeverity.INFO,
                            (report.getVerdict() == ScanVerdict.KNOWN_MALICIOUS ? "🚨 Intercepted Trojan APK: " : "✅ Clean APK Verified: ") + report.getFileName(),
                            report.getPlainLanguageExplanation(),
                            List.of("Verdict: " + report.getVerdict().getDisplayName(), "SHA256: " + report.getSha256Checksum()),
                            report.canSafelyProceedWithInstall() ? "Proceed with Install" : "Quarantine & Delete"
                    );

                    AppState.get().addActivityEvent(event);
                    DetailSheetDialog.showEventDetails(this, event);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Could not open APK: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
