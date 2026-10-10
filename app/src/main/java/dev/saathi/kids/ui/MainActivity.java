package dev.saathi.kids.ui;

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

import dev.saathi.kids.R;
import dev.saathi.kids.ui.components.KidsOnboardingOverlay;
import dev.saathi.kids.ui.components.LessonDetailOverlay;
import dev.saathi.kids.ui.components.SosFlowOverlay;
import dev.saathi.kids.ui.model.Lesson;
import dev.saathi.kids.ui.model.NavigationTab;
import dev.saathi.kids.ui.screens.CheckScreenController;
import dev.saathi.kids.ui.screens.HomeScreenController;
import dev.saathi.kids.ui.screens.LearnScreenController;
import dev.saathi.kids.ui.screens.SettingsScreenController;
import dev.saathi.kids.ui.state.KidsAppState;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Main Activity for Saathi Kids.
 * Manages 4-destination navigation: Home, Check, Learn, Settings.
 * Hosts fullscreen overlays for Onboarding, SOS Help Flow, and Lesson Scenarios.
 * Optimized specifically for iQOO 15 (Snapdragon 8 Elite, 144Hz LTPO display).
 */
public class MainActivity extends Activity implements
        HomeScreenController.HomeNavigationHost,
        CheckScreenController.CheckNavigationHost,
        LearnScreenController.LearnNavigationHost {

    private static final int REQ_PICK_FILE = 1001;

    private FrameLayout screenContainer;
    private FrameLayout overlayContainer;

    private LinearLayout navItemHome;
    private LinearLayout navItemCheck;
    private LinearLayout navItemLearn;
    private LinearLayout navItemSettings;

    private ImageView ivNavHome;
    private ImageView ivNavCheck;
    private ImageView ivNavLearn;
    private ImageView ivNavSettings;

    private TextView tvNavHome;
    private TextView tvNavCheck;
    private TextView tvNavLearn;
    private TextView tvNavSettings;

    private HomeScreenController homeController;
    private CheckScreenController checkController;
    private LearnScreenController learnController;
    private SettingsScreenController settingsController;

    private NavigationTab currentTab = NavigationTab.HOME;
    private final Deque<NavigationTab> navigationBackStack = new ArrayDeque<>();
    private Vibrator vibrator;

    private boolean isOverlayActive = false;

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

        // Initialize state
        KidsAppState.getInstance().initContext(this);

        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        initViews();
        setupScreenControllers();
        setupNavigation();

        // Check if first-launch onboarding is needed
        if (!KidsAppState.getInstance().isOnboardingCompleted()) {
            showOnboarding();
        } else {
            switchTab(NavigationTab.HOME, false);
        }

        // Handle incoming shared intent after initial screen setup
        handleIncomingIntent(getIntent());
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
        overlayContainer = findViewById(R.id.overlay_container);

        navItemHome = findViewById(R.id.nav_item_home);
        navItemCheck = findViewById(R.id.nav_item_check);
        navItemLearn = findViewById(R.id.nav_item_learn);
        navItemSettings = findViewById(R.id.nav_item_settings);

        ivNavHome = findViewById(R.id.iv_nav_home);
        ivNavCheck = findViewById(R.id.iv_nav_check);
        ivNavLearn = findViewById(R.id.iv_nav_learn);
        ivNavSettings = findViewById(R.id.iv_nav_settings);

        tvNavHome = findViewById(R.id.tv_nav_home);
        tvNavCheck = findViewById(R.id.tv_nav_check);
        tvNavLearn = findViewById(R.id.tv_nav_learn);
        tvNavSettings = findViewById(R.id.tv_nav_settings);
    }

    private void setupScreenControllers() {
        homeController = new HomeScreenController(this, this);
        checkController = new CheckScreenController(this, this);
        learnController = new LearnScreenController(this, this);
        settingsController = new SettingsScreenController(this);

        // Add 4 screens to the container
        screenContainer.addView(homeController.getView());
        screenContainer.addView(checkController.getView());
        screenContainer.addView(learnController.getView());
        screenContainer.addView(settingsController.getView());
    }

    private void setupNavigation() {
        navItemHome.setOnClickListener(v -> switchTab(NavigationTab.HOME, true));
        navItemCheck.setOnClickListener(v -> switchTab(NavigationTab.CHECK, true));
        navItemLearn.setOnClickListener(v -> switchTab(NavigationTab.LEARN, true));
        navItemSettings.setOnClickListener(v -> switchTab(NavigationTab.SETTINGS, true));
    }

    @Override
    public void switchTab(NavigationTab targetTab, boolean addToStack) {
        if (addToStack && currentTab != targetTab) {
            navigationBackStack.push(currentTab);
        }

        currentTab = targetTab;
        performHaptic();

        // Update Visibility of Screens
        homeController.getView().setVisibility(targetTab == NavigationTab.HOME ? View.VISIBLE : View.GONE);
        checkController.getView().setVisibility(targetTab == NavigationTab.CHECK ? View.VISIBLE : View.GONE);
        learnController.getView().setVisibility(targetTab == NavigationTab.LEARN ? View.VISIBLE : View.GONE);
        settingsController.getView().setVisibility(targetTab == NavigationTab.SETTINGS ? View.VISIBLE : View.GONE);

        // Refresh views
        if (targetTab == NavigationTab.HOME) homeController.render();
        else if (targetTab == NavigationTab.LEARN) learnController.render();
        else if (targetTab == NavigationTab.SETTINGS) settingsController.render();

        // Update Bottom Nav Highlighting
        updateNavHighlight(targetTab);
    }

    private void updateNavHighlight(NavigationTab tab) {
        int activeColor = 0xFF141414; // Solid Monochrome Black
        int inactiveColor = 0xFF888888; // Muted Neutral Grey

        ivNavHome.setColorFilter(tab == NavigationTab.HOME ? activeColor : inactiveColor);
        tvNavHome.setTextColor(tab == NavigationTab.HOME ? activeColor : inactiveColor);
        tvNavHome.setTypeface(null, tab == NavigationTab.HOME ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavCheck.setColorFilter(tab == NavigationTab.CHECK ? activeColor : inactiveColor);
        tvNavCheck.setTextColor(tab == NavigationTab.CHECK ? activeColor : inactiveColor);
        tvNavCheck.setTypeface(null, tab == NavigationTab.CHECK ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavLearn.setColorFilter(tab == NavigationTab.LEARN ? activeColor : inactiveColor);
        tvNavLearn.setTextColor(tab == NavigationTab.LEARN ? activeColor : inactiveColor);
        tvNavLearn.setTypeface(null, tab == NavigationTab.LEARN ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);

        ivNavSettings.setColorFilter(tab == NavigationTab.SETTINGS ? activeColor : inactiveColor);
        tvNavSettings.setTextColor(tab == NavigationTab.SETTINGS ? activeColor : inactiveColor);
        tvNavSettings.setTypeface(null, tab == NavigationTab.SETTINGS ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
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

    // --- Overlays Implementation ---

    public void showOnboarding() {
        isOverlayActive = true;
        KidsOnboardingOverlay onboarding = new KidsOnboardingOverlay(this, overlayContainer, () -> {
            isOverlayActive = false;
            overlayContainer.setVisibility(View.GONE);
            overlayContainer.removeAllViews();
            switchTab(NavigationTab.HOME, false);
        });
        onboarding.show();
    }

    @Override
    public void showSosFlow() {
        isOverlayActive = true;
        SosFlowOverlay sos = new SosFlowOverlay(this, overlayContainer, () -> {
            isOverlayActive = false;
            overlayContainer.setVisibility(View.GONE);
            overlayContainer.removeAllViews();
        });
        sos.show();
    }

    @Override
    public void showLessonDetail(Lesson lesson) {
        isOverlayActive = true;
        LessonDetailOverlay detail = new LessonDetailOverlay(this, overlayContainer, lesson, () -> {
            isOverlayActive = false;
            overlayContainer.setVisibility(View.GONE);
            overlayContainer.removeAllViews();
            // Refresh views
            if (currentTab == NavigationTab.HOME) homeController.render();
            else if (currentTab == NavigationTab.LEARN) learnController.render();
        });
        detail.show();
    }

    @Override
    public void openCheckMode(int modeIndex) {
        switchTab(NavigationTab.CHECK, true);
        checkController.setMode(modeIndex);
    }

    @Override
    public void requestFilePick() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, REQ_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = uri.getLastPathSegment();
                if (path == null) path = uri.toString();
                switchTab(NavigationTab.CHECK, true);
                checkController.setMode(2);
                checkController.runCheck(path);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action) && intent.getType() != null) {
            if ("text/plain".equals(intent.getType())) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null && !sharedText.trim().isEmpty()) {
                    switchTab(NavigationTab.CHECK, true);
                    if (sharedText.startsWith("http://") || sharedText.startsWith("https://")) {
                        checkController.setPastedUrl(sharedText);
                    } else {
                        checkController.setPastedText(sharedText);
                    }
                }
            } else {
                Uri streamUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (streamUri != null) {
                    switchTab(NavigationTab.CHECK, true);
                    String name = streamUri.getLastPathSegment();
                    if (name == null) name = streamUri.toString();
                    checkController.setFileToCheck(name);
                }
            }
        } else if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null) {
            Uri dataUri = intent.getData();
            String name = dataUri.getLastPathSegment();
            if (name == null) name = dataUri.toString();
            switchTab(NavigationTab.CHECK, true);
            checkController.setFileToCheck(name);
        }
    }

    @Override
    public void onBackPressed() {
        // If an overlay is active, dismiss it first
        if (isOverlayActive && overlayContainer != null && overlayContainer.getVisibility() == View.VISIBLE) {
            isOverlayActive = false;
            overlayContainer.setVisibility(View.GONE);
            overlayContainer.removeAllViews();
            return;
        }

        // Pop navigation stack
        if (!navigationBackStack.isEmpty()) {
            NavigationTab previous = navigationBackStack.pop();
            switchTab(previous, false);
            return;
        }

        // If on non-home tab, go to Home
        if (currentTab != NavigationTab.HOME) {
            switchTab(NavigationTab.HOME, false);
            return;
        }

        super.onBackPressed();
    }
}
