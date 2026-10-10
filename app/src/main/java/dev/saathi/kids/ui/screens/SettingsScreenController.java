package dev.saathi.kids.ui.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.kids.ui.model.TrustedAdult;
import dev.saathi.kids.ui.state.KidsAppState;

import java.util.List;

/**
 * Saathi Kids Settings & Privacy Screen.
 * Contains:
 * 1. Transparent Privacy & Trust Manifesto
 * 2. Family Connection & Pairing Code (SAATHI-KIDS-4291)
 * 3. Trusted Adults Management (view, add, remove contacts)
 * 4. Language & Readability Preferences
 * 5. App Info & Child-Safety Principles
 */
public class SettingsScreenController {

    private final Activity activity;
    private final float density;

    private ScrollView rootView;
    private LinearLayout containerAdultsList;
    private TextView tvLanguageLabel;
    private Switch switchLargeText;
    private Switch switchHighContrast;

    public SettingsScreenController(Activity activity) {
        this.activity = activity;
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

        // 2. Privacy & Trust Manifesto Card
        layout.addView(buildManifestoCard());

        // 3. Family Connection Card
        layout.addView(buildFamilyConnectionCard());

        // 4. Trusted Adults Management Card
        layout.addView(buildTrustedAdultsCard());

        // 5. Language & Display Preferences Card
        layout.addView(buildPreferencesCard());

        // 6. App Info Footer
        layout.addView(buildAppInfoFooter());

        rootView.addView(layout);
        render();
    }

    private View buildHeader() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(4), 0, dp(14));

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Settings & Privacy ⚙️");
        tvTitle.setTextSize(24f);
        tvTitle.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        tvTitle.setTextColor(Color.parseColor("#141414"));
        box.addView(tvTitle);

        TextView tvSub = new TextView(activity);
        tvSub.setText("Everything about your privacy boundaries, trusted contacts, and family connection.");
        tvSub.setTextSize(13.5f);
        tvSub.setTextColor(Color.parseColor("#555555"));
        tvSub.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(4);
        box.addView(tvSub, subLp);

        return box;
    }

    private View buildManifestoCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Our Privacy Promise to You 🔒");
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        card.addView(tvTitle);

        TextView tvDesc = new TextView(activity);
        tvDesc.setText("Saathi Kids is designed to support you, not spy on you. Here is exactly how your data is handled:");
        tvDesc.setTextSize(12.5f);
        tvDesc.setTextColor(Color.parseColor("#666666"));
        tvDesc.setLineSpacing(dp(2), 1.15f);
        LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.topMargin = dp(4);
        descLp.bottomMargin = dp(12);
        card.addView(tvDesc, descLp);

        // Good items
        card.addView(createManifestoItem("✅", "Checks stay on your device", "When you test a message or link, our engine checks it locally. Nothing is uploaded to advertising servers."));
        card.addView(createManifestoItem("✅", "You choose what to check", "We do not run background keyloggers or silently read your private conversations. You paste what you want to inspect."));
        card.addView(createManifestoItem("🛡️", "No GPS surveillance", "We do not track where you walk, bike, or go after school."));
        card.addView(createManifestoItem("🛡️", "No ads or data selling", "Your activity is never used for commercial marketing or social media tracking."));

        return card;
    }

    private View createManifestoItem(String icon, String title, String explanation) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        TextView tvIcon = new TextView(activity);
        tvIcon.setText(icon);
        tvIcon.setTextSize(14f);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        iconLp.rightMargin = dp(8);
        row.addView(tvIcon, iconLp);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvT = new TextView(activity);
        tvT.setText(title);
        tvT.setTextSize(13f);
        tvT.setTypeface(Typeface.DEFAULT_BOLD);
        tvT.setTextColor(Color.parseColor("#222222"));
        textCol.addView(tvT);

        TextView tvE = new TextView(activity);
        tvE.setText(explanation);
        tvE.setTextSize(12f);
        tvE.setTextColor(Color.parseColor("#666666"));
        tvE.setLineSpacing(dp(1), 1.15f);
        textCol.addView(tvE);

        row.addView(textCol);
        return row;
    }

    private View buildFamilyConnectionCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Family Connection 👨‍👩‍👧");
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        card.addView(tvTitle);

        TextView tvStatus = new TextView(activity);
        tvStatus.setText("● Connected with Saathi Family");
        tvStatus.setTextSize(12.5f);
        tvStatus.setTypeface(Typeface.DEFAULT_BOLD);
        tvStatus.setTextColor(Color.parseColor("#386B57"));
        LinearLayout.LayoutParams statusLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusLp.topMargin = dp(4);
        statusLp.bottomMargin = dp(10);
        card.addView(tvStatus, statusLp);

        // Pairing Code Box
        LinearLayout codeBox = new LinearLayout(activity);
        codeBox.setOrientation(LinearLayout.HORIZONTAL);
        codeBox.setGravity(Gravity.CENTER_VERTICAL);
        codeBox.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#DCDCD8"), dp(10)));
        codeBox.setPadding(dp(14), dp(10), dp(14), dp(10));

        LinearLayout.LayoutParams codeBoxLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        codeBoxLp.bottomMargin = dp(10);

        TextView tvCode = new TextView(activity);
        tvCode.setText(KidsAppState.getInstance().getFamilyPairingCode());
        tvCode.setTextSize(15f);
        tvCode.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        tvCode.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams codeLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        codeBox.addView(tvCode, codeLp);

        Button btnCopy = new Button(activity);
        btnCopy.setText("Copy Code");
        btnCopy.setTextSize(11f);
        btnCopy.setTypeface(Typeface.DEFAULT_BOLD);
        btnCopy.setTextColor(Color.parseColor("#141414"));
        btnCopy.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#D0D0CC"), dp(6)));
        btnCopy.setPadding(dp(10), dp(4), dp(10), dp(4));
        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText("Pairing Code", KidsAppState.getInstance().getFamilyPairingCode()));
                Toast.makeText(activity, "Family pairing code copied!", Toast.LENGTH_SHORT).show();
            }
        });
        codeBox.addView(btnCopy);

        card.addView(codeBox, codeBoxLp);

        TextView tvExplain = new TextView(activity);
        tvExplain.setText("Your family can receive help requests if you send them and review completed quests. They cannot read your personal chats.");
        tvExplain.setTextSize(12f);
        tvExplain.setTextColor(Color.parseColor("#666666"));
        tvExplain.setLineSpacing(dp(1), 1.15f);
        card.addView(tvExplain);

        return card;
    }

    private View buildTrustedAdultsCard() {
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
        tvTitle.setText("Trusted Adults Contacts 👥");
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        topRow.addView(tvTitle, titleLp);

        Button btnAdd = new Button(activity);
        btnAdd.setText("+ Add");
        btnAdd.setTextSize(11f);
        btnAdd.setTypeface(Typeface.DEFAULT_BOLD);
        btnAdd.setTextColor(Color.parseColor("#141414"));
        btnAdd.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#D0D0CC"), dp(6)));
        btnAdd.setPadding(dp(10), dp(4), dp(10), dp(4));
        btnAdd.setOnClickListener(v -> showAddAdultDialog());
        topRow.addView(btnAdd);

        card.addView(topRow);

        TextView tvSub = new TextView(activity);
        tvSub.setText("People who will appear on your Help screen if you ever feel uncomfortable.");
        tvSub.setTextSize(12f);
        tvSub.setTextColor(Color.parseColor("#666666"));
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subLp.topMargin = dp(4);
        subLp.bottomMargin = dp(12);
        card.addView(tvSub, subLp);

        containerAdultsList = new LinearLayout(activity);
        containerAdultsList.setOrientation(LinearLayout.VERTICAL);
        card.addView(containerAdultsList);

        return card;
    }

    private void showAddAdultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add Trusted Adult");

        LinearLayout form = new LinearLayout(activity);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(10), dp(20), dp(10));

        final EditText etName = new EditText(activity);
        etName.setHint("Name (e.g. Uncle Rohan)");
        form.addView(etName);

        final EditText etRelation = new EditText(activity);
        etRelation.setHint("Relationship (e.g. Uncle / Teacher)");
        form.addView(etRelation);

        final EditText etPhone = new EditText(activity);
        etPhone.setHint("Phone Number");
        form.addView(etPhone);

        builder.setView(form);
        builder.setPositiveButton("Add Contact", (dialog, which) -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String relation = etRelation.getText() != null ? etRelation.getText().toString().trim() : "";
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
            if (!name.isEmpty()) {
                KidsAppState.getInstance().addTrustedAdult(name, relation, phone);
                Toast.makeText(activity, name + " added to trusted adults", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private View buildPreferencesCard() {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(createCardDrawable(Color.WHITE, Color.parseColor("#E6E6E2"), dp(16)));
        card.setPadding(dp(20), dp(18), dp(20), dp(18));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(16);
        card.setLayoutParams(lp);

        TextView tvTitle = new TextView(activity);
        tvTitle.setText("Display & Language Preferences 🎨");
        tvTitle.setTextSize(16f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setTextColor(Color.parseColor("#141414"));
        card.addView(tvTitle);

        // Language toggle row
        LinearLayout langRow = new LinearLayout(activity);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        langRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams langRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        langRowLp.topMargin = dp(12);
        langRowLp.bottomMargin = dp(12);

        tvLanguageLabel = new TextView(activity);
        tvLanguageLabel.setText("Language: English");
        tvLanguageLabel.setTextSize(13.5f);
        tvLanguageLabel.setTextColor(Color.parseColor("#222222"));
        LinearLayout.LayoutParams llLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        langRow.addView(tvLanguageLabel, llLp);

        Button btnToggleLang = new Button(activity);
        btnToggleLang.setText("Switch to Hinglish");
        btnToggleLang.setTextSize(11f);
        btnToggleLang.setTypeface(Typeface.DEFAULT_BOLD);
        btnToggleLang.setTextColor(Color.parseColor("#141414"));
        btnToggleLang.setBackground(createCardDrawable(Color.parseColor("#F5F5F3"), Color.parseColor("#D0D0CC"), dp(6)));
        btnToggleLang.setPadding(dp(10), dp(4), dp(10), dp(4));
        btnToggleLang.setOnClickListener(v -> {
            KidsAppState state = KidsAppState.getInstance();
            if (state.isHinglish()) {
                state.setLanguage("en");
                btnToggleLang.setText("Switch to Hinglish");
                tvLanguageLabel.setText("Language: English");
            } else {
                state.setLanguage("hi");
                btnToggleLang.setText("Switch to English");
                tvLanguageLabel.setText("Language: Hinglish");
            }
            Toast.makeText(activity, "Language updated", Toast.LENGTH_SHORT).show();
        });
        langRow.addView(btnToggleLang);

        card.addView(langRow, langRowLp);

        // Large text switch
        LinearLayout ltRow = new LinearLayout(activity);
        ltRow.setOrientation(LinearLayout.HORIZONTAL);
        ltRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams ltRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ltRowLp.bottomMargin = dp(8);

        TextView tvLt = new TextView(activity);
        tvLt.setText("Large Text & Bigger Buttons");
        tvLt.setTextSize(13.5f);
        tvLt.setTextColor(Color.parseColor("#222222"));
        LinearLayout.LayoutParams ltLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        ltRow.addView(tvLt, ltLp);

        switchLargeText = new Switch(activity);
        switchLargeText.setChecked(KidsAppState.getInstance().isLargeTypography());
        switchLargeText.setOnCheckedChangeListener((buttonView, isChecked) -> {
            KidsAppState.getInstance().setLargeTypography(isChecked);
        });
        ltRow.addView(switchLargeText);
        card.addView(ltRow, ltRowLp);

        // High contrast switch
        LinearLayout hcRow = new LinearLayout(activity);
        hcRow.setOrientation(LinearLayout.HORIZONTAL);
        hcRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvHc = new TextView(activity);
        tvHc.setText("High Contrast Mode");
        tvHc.setTextSize(13.5f);
        tvHc.setTextColor(Color.parseColor("#222222"));
        LinearLayout.LayoutParams hcLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        hcRow.addView(tvHc, hcLp);

        switchHighContrast = new Switch(activity);
        switchHighContrast.setChecked(KidsAppState.getInstance().isHighContrast());
        switchHighContrast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            KidsAppState.getInstance().setHighContrast(isChecked);
        });
        hcRow.addView(switchHighContrast);
        card.addView(hcRow);

        return card;
    }

    private View buildAppInfoFooter() {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_HORIZONTAL);
        box.setPadding(dp(10), dp(10), dp(10), dp(20));

        TextView tvApp = new TextView(activity);
        tvApp.setText("Saathi Kids v1.0.0-kids\nYour Companion for Digital Safety\nDesigned with child-first safety principles.");
        tvApp.setTextSize(11.5f);
        tvApp.setTextColor(Color.parseColor("#888888"));
        tvApp.setGravity(Gravity.CENTER);
        tvApp.setLineSpacing(dp(2), 1.2f);
        box.addView(tvApp);

        return box;
    }

    public void render() {
        KidsAppState state = KidsAppState.getInstance();

        if (containerAdultsList != null) {
            containerAdultsList.removeAllViews();
            List<TrustedAdult> adults = state.getTrustedAdults();
            for (TrustedAdult adult : adults) {
                containerAdultsList.addView(buildAdultItemView(adult));
            }
        }

        if (tvLanguageLabel != null) {
            tvLanguageLabel.setText(state.isHinglish() ? "Language: Hinglish" : "Language: English");
        }
    }

    private View buildAdultItemView(TrustedAdult adult) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(createCardDrawable(Color.parseColor("#F9F9F8"), Color.parseColor("#EAEAE6"), dp(10)));
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        row.setLayoutParams(lp);

        LinearLayout textCol = new LinearLayout(activity);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textColLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        row.addView(textCol, textColLp);

        TextView tvName = new TextView(activity);
        tvName.setText(adult.getName() + " (" + adult.getRelationship() + ")");
        tvName.setTextSize(13.5f);
        tvName.setTypeface(Typeface.DEFAULT_BOLD);
        tvName.setTextColor(Color.parseColor("#141414"));
        textCol.addView(tvName);

        TextView tvPhone = new TextView(activity);
        tvPhone.setText(adult.getPhoneNumber());
        tvPhone.setTextSize(12f);
        tvPhone.setTextColor(Color.parseColor("#666666"));
        textCol.addView(tvPhone);

        Button btnRemove = new Button(activity);
        btnRemove.setText("✕");
        btnRemove.setTextSize(12f);
        btnRemove.setTextColor(Color.parseColor("#888888"));
        btnRemove.setBackground(null);
        btnRemove.setOnClickListener(v -> {
            KidsAppState.getInstance().removeTrustedAdult(adult.getId());
            Toast.makeText(activity, "Removed " + adult.getName(), Toast.LENGTH_SHORT).show();
        });
        row.addView(btnRemove);

        return row;
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
}
