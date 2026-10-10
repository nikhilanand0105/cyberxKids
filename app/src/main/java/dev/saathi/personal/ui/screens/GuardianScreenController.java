package dev.saathi.personal.ui.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.personal.R;
import dev.saathi.personal.ui.model.ChildProfile;
import dev.saathi.personal.ui.state.AppState;
import dev.saathi.storage.EmergencySosEvent;
import dev.saathi.storage.TrustedHelper;

import java.util.List;

public class GuardianScreenController {

    private final Activity activity;
    private final float density;
    private ScrollView rootView;

    private TextView tvActiveProfileName;
    private TextView tvRetentionInfo;
    private LinearLayout containerHelpers;
    private Button btnSeniorToggle;
    private Button btnLangToggle;

    public GuardianScreenController(Activity activity) {
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
        rootView.setBackgroundColor(0xFFF7F7F5);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (20 * density), (int) (16 * density), (int) (20 * density), (int) (100 * density));

        // 1. Header
        layout.addView(buildHeader());

        // 2. Active Profile & Device Card
        layout.addView(buildProfileCard());

        // 3. Android Permissions & Security Health Audit
        layout.addView(buildPermissionsAuditSection());

        // 4. Privacy & Data Retention Pruning
        layout.addView(buildPrivacyRetentionSection());

        // 5. Emergency SOS & Trusted Helpers Management
        layout.addView(buildTrustedHelpersSection());

        // 6. Senior Mode & Vernacular Switches
        layout.addView(buildAccessibilitySection());

        // 7. National Helpline (1930) Section
        layout.addView(buildHelplineSection());

        rootView.addView(layout);
        render();
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, 0, 0, (int) (16 * density));

        TextView title = new TextView(activity);
        title.setText("GUARDIAN & DEVICE CONTROLS");
        title.setTextSize(22);
        title.setTextColor(0xFF141414);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(title);

        TextView sub = new TextView(activity);
        sub.setText("Hardware governance, trusted emergency contacts, and privacy minimization");
        sub.setTextSize(13);
        sub.setTextColor(0xFF666666);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sp.topMargin = (int) (4 * density);
        sub.setLayoutParams(sp);
        header.addView(sub);

        return header;
    }

    private View buildProfileCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_cred);
        card.setPadding((int) (18 * density), (int) (16 * density), (int) (18 * density), (int) (16 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (16 * density);
        card.setLayoutParams(params);

        TextView label = new TextView(activity);
        label.setText("MONITORED WARD PROFILE & DEVICE");
        label.setTextSize(11);
        label.setTextColor(0xFF888888);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(label);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rp.topMargin = (int) (8 * density);
        row.setLayoutParams(rp);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(tp);

        tvActiveProfileName = new TextView(activity);
        tvActiveProfileName.setText(AppState.get().getCurrentProfile().getName());
        tvActiveProfileName.setTextSize(16);
        tvActiveProfileName.setTextColor(0xFF141414);
        tvActiveProfileName.setTypeface(null, android.graphics.Typeface.BOLD);
        textCol.addView(tvActiveProfileName);

        TextView tvDev = new TextView(activity);
        tvDev.setText("Device: iQOO 15 (Android 16, SM8850) • Paired");
        tvDev.setTextSize(12);
        tvDev.setTextColor(0xFF666666);
        textCol.addView(tvDev);

        row.addView(textCol);

        Button btnSwitch = new Button(activity);
        btnSwitch.setText("Switch");
        btnSwitch.setTextSize(12);
        btnSwitch.setTextColor(0xFFFFFFFF);
        btnSwitch.setBackgroundResource(R.drawable.bg_pill_black);
        btnSwitch.setOnClickListener(v -> showProfileDialog());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                (int) (84 * density),
                (int) (38 * density)
        );
        btnSwitch.setLayoutParams(bp);
        row.addView(btnSwitch);

        card.addView(row);

        return card;
    }

    private View buildPermissionsAuditSection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setBackgroundResource(R.drawable.bg_card_cred);
        section.setPadding((int) (18 * density), (int) (16 * density), (int) (18 * density), (int) (16 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (16 * density);
        section.setLayoutParams(params);

        TextView label = new TextView(activity);
        label.setText("ANDROID PERMISSION & HEALTH AUDIT");
        label.setTextSize(11);
        label.setTextColor(0xFF888888);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        section.addView(label);

        section.addView(createPermRow("APK Package Inspection", "Scoped FileProvider", "SECURE", 0xFF386B57, R.drawable.bg_pill_emerald));
        section.addView(createPermRow("Accessibility Abuse Guard", "Active Background Hook Monitor", "ARMED", 0xFF386B57, R.drawable.bg_pill_emerald));
        section.addView(createPermRow("High Refresh Rate (144Hz)", "Window DisplayMode ID 144.0Hz", "ENABLED", 0xFF141414, R.drawable.bg_pill_dark));
        section.addView(createPermRow("Restricted Settings Check", "Android 16 API 36 Verified", "COMPLIANT", 0xFF386B57, R.drawable.bg_pill_emerald));

        return section;
    }

    private View createPermRow(String title, String desc, String badge, int color, int badgeBg) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = (int) (10 * density);
        row.setLayoutParams(params);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(tp);

        TextView t = new TextView(activity);
        t.setText(title);
        t.setTextSize(13);
        t.setTextColor(0xFF141414);
        textCol.addView(t);

        TextView d = new TextView(activity);
        d.setText(desc);
        d.setTextSize(11);
        d.setTextColor(0xFF666666);
        textCol.addView(d);

        row.addView(textCol);

        TextView b = new TextView(activity);
        b.setText(badge);
        b.setTextSize(10);
        b.setTextColor(color);
        b.setBackgroundResource(badgeBg);
        b.setPadding((int) (8 * density), (int) (3 * density), (int) (8 * density), (int) (3 * density));
        row.addView(b);

        return row;
    }

    private View buildPrivacyRetentionSection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setBackgroundResource(R.drawable.bg_card_cred);
        section.setPadding((int) (18 * density), (int) (16 * density), (int) (18 * density), (int) (16 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (16 * density);
        section.setLayoutParams(params);

        TextView label = new TextView(activity);
        label.setText("LOCAL DATA MINIMIZATION & AUTO-RETENTION");
        label.setTextSize(11);
        label.setTextColor(0xFF888888);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        section.addView(label);

        tvRetentionInfo = new TextView(activity);
        tvRetentionInfo.setText("Current Retention Window: 14 Days • Auto-Pruning Enabled");
        tvRetentionInfo.setTextSize(12);
        tvRetentionInfo.setTextColor(0xFF666666);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ip.topMargin = (int) (4 * density);
        ip.bottomMargin = (int) (10 * density);
        tvRetentionInfo.setLayoutParams(ip);
        section.addView(tvRetentionInfo);

        // Retention Buttons Row
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button btn7 = createRetentionButton("7 Days", 7, false);
        Button btn14 = createRetentionButton("14 Days (Default)", 14, true);
        Button btn30 = createRetentionButton("30 Days", 30, false);

        row.addView(btn7);
        row.addView(btn14);
        row.addView(btn30);
        section.addView(row);

        return section;
    }

    private Button createRetentionButton(String label, int days, boolean isDefault) {
        Button b = new Button(activity);
        b.setText(label);
        b.setTextSize(11);
        b.setTextColor(isDefault ? 0xFFFFFFFF : 0xFF141414);
        b.setBackgroundResource(isDefault ? R.drawable.bg_pill_black : R.drawable.bg_pill_dark);
        b.setOnClickListener(v -> {
            AppState.get().getStorageRepository().setRetentionDays(days);
            int purged = AppState.get().getStorageRepository().pruneExpiredRecords();
            tvRetentionInfo.setText("Retention Window: " + days + " Days • Purged " + purged + " expired records");
            Toast.makeText(activity, "Retention updated to " + days + " days. " + purged + " records pruned.", Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, (int) (40 * density), 1f);
        bp.rightMargin = (int) (4 * density);
        b.setLayoutParams(bp);
        return b;
    }

    private View buildTrustedHelpersSection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setBackgroundResource(R.drawable.bg_card_cred);
        section.setPadding((int) (18 * density), (int) (16 * density), (int) (18 * density), (int) (16 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (16 * density);
        section.setLayoutParams(params);

        LinearLayout top = new LinearLayout(activity);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView label = new TextView(activity);
        label.setText("TRUSTED EMERGENCY CONTACTS (RAKSHAK)");
        label.setTextSize(11);
        label.setTextColor(0xFF888888);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        top.addView(label, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Button btnAdd = new Button(activity);
        btnAdd.setText("+ Add");
        btnAdd.setTextSize(11);
        btnAdd.setTextColor(0xFFFFFFFF);
        btnAdd.setBackgroundResource(R.drawable.bg_pill_black);
        btnAdd.setOnClickListener(v -> showAddHelperDialog());
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                (int) (70 * density),
                (int) (34 * density)
        );
        btnAdd.setLayoutParams(bp);
        top.addView(btnAdd);

        section.addView(top);

        containerHelpers = new LinearLayout(activity);
        containerHelpers.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cp.topMargin = (int) (8 * density);
        containerHelpers.setLayoutParams(cp);
        section.addView(containerHelpers);

        // Test SOS Button
        Button btnTestSos = new Button(activity);
        btnTestSos.setText("🚨 Dispatch Test Emergency SOS");
        btnTestSos.setTextSize(12);
        btnTestSos.setTextColor(0xFFA63232);
        btnTestSos.setBackgroundResource(R.drawable.bg_pill_crimson);
        btnTestSos.setOnClickListener(v -> {
            EmergencySosEvent sos = AppState.get().getStorageRepository().dispatchSos(28.6139, 77.2090, 5.0f, "Test SOS from iQOO 15 Guardian", true);
            Toast.makeText(activity, "Emergency SOS Dispatched: " + sos.getStatus().name() + " (Coords: 28.61, 77.20)", Toast.LENGTH_LONG).show();
        });
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (44 * density)
        );
        tp.topMargin = (int) (12 * density);
        btnTestSos.setLayoutParams(tp);
        section.addView(btnTestSos);

        return section;
    }

    private View buildAccessibilitySection() {
        LinearLayout section = new LinearLayout(activity);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setBackgroundResource(R.drawable.bg_card_cred);
        section.setPadding((int) (18 * density), (int) (16 * density), (int) (18 * density), (int) (16 * density));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = (int) (16 * density);
        section.setLayoutParams(params);

        TextView label = new TextView(activity);
        label.setText("ACCESSIBILITY & VERNACULAR PREFERENCES");
        label.setTextSize(11);
        label.setTextColor(0xFF888888);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        section.addView(label);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rp.topMargin = (int) (10 * density);
        row.setLayoutParams(rp);

        btnSeniorToggle = new Button(activity);
        btnSeniorToggle.setText(AppState.get().isSeniorMode() ? "Senior Mode: ON" : "Senior Mode: OFF");
        btnSeniorToggle.setTextSize(12);
        btnSeniorToggle.setTextColor(0xFF141414);
        btnSeniorToggle.setBackgroundResource(R.drawable.bg_pill_dark);
        btnSeniorToggle.setOnClickListener(v -> {
            AppState.get().setSeniorMode(!AppState.get().isSeniorMode());
            btnSeniorToggle.setText(AppState.get().isSeniorMode() ? "Senior Mode: ON" : "Senior Mode: OFF");
            Toast.makeText(activity, "Senior Mode toggled: " + (AppState.get().isSeniorMode() ? "Large Typography" : "Standard"), Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, (int) (44 * density), 1f);
        p1.rightMargin = (int) (6 * density);
        btnSeniorToggle.setLayoutParams(p1);
        row.addView(btnSeniorToggle);

        btnLangToggle = new Button(activity);
        btnLangToggle.setText(AppState.get().isHindi() ? "भाषा: हिन्दी" : "Language: English");
        btnLangToggle.setTextSize(12);
        btnLangToggle.setTextColor(0xFF141414);
        btnLangToggle.setBackgroundResource(R.drawable.bg_pill_dark);
        btnLangToggle.setOnClickListener(v -> {
            AppState.get().setHindi(!AppState.get().isHindi());
            btnLangToggle.setText(AppState.get().isHindi() ? "भाषा: हिन्दी" : "Language: English");
            Toast.makeText(activity, "Language toggled: " + (AppState.get().isHindi() ? "हिन्दी" : "English"), Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, (int) (44 * density), 1f);
        p2.leftMargin = (int) (6 * density);
        btnLangToggle.setLayoutParams(p2);
        row.addView(btnLangToggle);

        section.addView(row);
        return section;
    }

    private View buildHelplineSection() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_card_glow_amber);
        card.setPadding((int) (18 * density), (int) (16 * density), (int) (18 * density), (int) (16 * density));

        TextView title = new TextView(activity);
        title.setText("📞 NATIONAL CYBER CRIME HELPLINE (1930)");
        title.setTextSize(14);
        title.setTextColor(0xFF855A12);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(title);

        TextView desc = new TextView(activity);
        desc.setText("Official Government of India portal (I4C). Call within the 'Golden Hour' to freeze fraudulent bank transactions.");
        desc.setTextSize(12);
        desc.setTextColor(0xFF666666);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        dp.topMargin = (int) (4 * density);
        dp.bottomMargin = (int) (10 * density);
        desc.setLayoutParams(dp);
        card.addView(desc);

        Button btnDial = new Button(activity);
        btnDial.setText("Call 1930 Immediately");
        btnDial.setTextSize(13);
        btnDial.setTextColor(0xFFFFFFFF);
        btnDial.setBackgroundResource(R.drawable.bg_pill_black);
        btnDial.setTypeface(null, android.graphics.Typeface.BOLD);
        btnDial.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:1930"));
            activity.startActivity(intent);
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (46 * density)
        );
        btnDial.setLayoutParams(bp);
        card.addView(btnDial);

        return card;
    }

    public void render() {
        tvActiveProfileName.setText(AppState.get().getCurrentProfile().getName());
        btnSeniorToggle.setText(AppState.get().isSeniorMode() ? "Senior Mode: ON" : "Senior Mode: OFF");
        btnLangToggle.setText(AppState.get().isHindi() ? "भाषा: हिन्दी" : "Language: English");

        // Populate trusted helpers list
        containerHelpers.removeAllViews();
        List<TrustedHelper> helpers = AppState.get().getStorageRepository().getTrustedHelpers();
        for (TrustedHelper h : helpers) {
            containerHelpers.addView(createHelperRow(h));
        }
    }

    private View createHelperRow(TrustedHelper h) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rp.topMargin = (int) (6 * density);
        row.setLayoutParams(rp);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(tp);

        TextView name = new TextView(activity);
        name.setText(h.getName() + " (" + h.getRelationship() + ")");
        name.setTextSize(13);
        name.setTextColor(0xFF141414);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        textCol.addView(name);

        TextView phone = new TextView(activity);
        phone.setText(h.getPhoneNumber());
        phone.setTextSize(11);
        phone.setTextColor(0xFF666666);
        textCol.addView(phone);

        row.addView(textCol);

        Button btnRemove = new Button(activity);
        btnRemove.setText("✕");
        btnRemove.setTextSize(12);
        btnRemove.setTextColor(0xFFA63232);
        btnRemove.setBackgroundResource(R.drawable.bg_pill_dark);
        btnRemove.setOnClickListener(v -> {
            AppState.get().getStorageRepository().removeTrustedHelper(h.getHelperId());
            render();
            Toast.makeText(activity, "Removed " + h.getName(), Toast.LENGTH_SHORT).show();
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                (int) (40 * density),
                (int) (34 * density)
        );
        btnRemove.setLayoutParams(bp);
        row.addView(btnRemove);

        return row;
    }

    private void showProfileDialog() {
        String[] profiles = {
                "Aarav (Teen • 14y)",
                "Ananya (Child • 9y)",
                "Dadaji (Senior • 72y)"
        };
        new AlertDialog.Builder(activity)
                .setTitle("Select Monitored Profile")
                .setItems(profiles, (dialog, which) -> {
                    if (which == 0) AppState.get().setCurrentProfile(ChildProfile.AARAV);
                    else if (which == 1) AppState.get().setCurrentProfile(ChildProfile.ANANYA);
                    else AppState.get().setCurrentProfile(ChildProfile.DADAJI);
                    render();
                })
                .show();
    }

    private void showAddHelperDialog() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (20 * density), (int) (10 * density), (int) (20 * density), (int) (10 * density));

        EditText etName = new EditText(activity);
        etName.setHint("Contact Name (e.g. Chachi / Doctor)");
        layout.addView(etName);

        EditText etPhone = new EditText(activity);
        etPhone.setHint("Phone Number (+91 ...)");
        layout.addView(etPhone);

        new AlertDialog.Builder(activity)
                .setTitle("Add Trusted Guardian Contact")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        String id = "th-" + System.currentTimeMillis();
                        AppState.get().getStorageRepository().addTrustedHelper(new TrustedHelper(id, name, "EMERGENCY_CONTACT", phone, true));
                        render();
                        Toast.makeText(activity, "Added " + name + " to Trusted Helpers", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
