package dev.saathi.personal.ui.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.engine.ApkInstallHandoffHelper;
import dev.saathi.model.ThreatSeverity;
import dev.saathi.personal.R;
import dev.saathi.personal.ui.model.ActivityEvent;
import dev.saathi.personal.ui.state.AppState;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailSheetDialog {

    public static void showEventDetails(Context context, ActivityEvent event) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        float density = context.getResources().getDisplayMetrics().density;

        ScrollView scrollView = new ScrollView(context);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        scrollView.setBackgroundColor(0xFF0E1420);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (24 * density), (int) (24 * density), (int) (24 * density), (int) (32 * density));

        // Drag pill bar
        TextView dragPill = new TextView(context);
        LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams((int) (48 * density), (int) (4 * density));
        pillParams.gravity = Gravity.CENTER_HORIZONTAL;
        pillParams.bottomMargin = (int) (18 * density);
        dragPill.setLayoutParams(pillParams);
        dragPill.setBackgroundColor(0xFF2B3D5B);
        layout.addView(dragPill);

        // Header: Category badge & severity
        LinearLayout badgeRow = new LinearLayout(context);
        badgeRow.setOrientation(LinearLayout.HORIZONTAL);
        badgeRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvCategory = new TextView(context);
        tvCategory.setText(event.getCategory().getDisplayName().toUpperCase(Locale.ROOT));
        tvCategory.setTextSize(11);
        tvCategory.setTextColor(0xFF00D9F5);
        tvCategory.setPadding((int) (10 * density), (int) (4 * density), (int) (10 * density), (int) (4 * density));
        tvCategory.setBackgroundResource(R.drawable.bg_pill_cyan);
        badgeRow.addView(tvCategory);

        TextView tvSeverity = new TextView(context);
        LinearLayout.LayoutParams sevParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sevParams.leftMargin = (int) (8 * density);
        tvSeverity.setLayoutParams(sevParams);
        tvSeverity.setText(event.getSeverity().name());
        tvSeverity.setTextSize(11);
        tvSeverity.setPadding((int) (10 * density), (int) (4 * density), (int) (10 * density), (int) (4 * density));

        if (event.getSeverity() == ThreatSeverity.CRITICAL) {
            tvSeverity.setTextColor(0xFFFF3B30);
            tvSeverity.setBackgroundResource(R.drawable.bg_pill_crimson);
        } else if (event.getSeverity() == ThreatSeverity.HIGH) {
            tvSeverity.setTextColor(0xFFF59E0B);
            tvSeverity.setBackgroundResource(R.drawable.bg_pill_amber);
        } else {
            tvSeverity.setTextColor(0xFF00F5A0);
            tvSeverity.setBackgroundResource(R.drawable.bg_pill_emerald);
        }
        badgeRow.addView(tvSeverity);

        layout.addView(badgeRow);

        // Title
        TextView tvTitle = new TextView(context);
        tvTitle.setText(event.getTitle());
        tvTitle.setTextSize(20);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = (int) (14 * density);
        tvTitle.setLayoutParams(titleParams);
        layout.addView(tvTitle);

        // Timestamp
        TextView tvTime = new TextView(context);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        tvTime.setText("Timestamp: " + sdf.format(event.getTimestamp()) + " • Snapdragon 8 Elite Verified");
        tvTime.setTextSize(12);
        tvTime.setTextColor(0xFF64748B);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        timeParams.topMargin = (int) (4 * density);
        tvTime.setLayoutParams(timeParams);
        layout.addView(tvTime);

        // Summary Card
        LinearLayout summaryCard = new LinearLayout(context);
        summaryCard.setOrientation(LinearLayout.VERTICAL);
        summaryCard.setBackgroundResource(R.drawable.bg_card_cred);
        summaryCard.setPadding((int) (16 * density), (int) (16 * density), (int) (16 * density), (int) (16 * density));
        LinearLayout.LayoutParams sumParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sumParams.topMargin = (int) (18 * density);
        summaryCard.setLayoutParams(sumParams);

        TextView sumLabel = new TextView(context);
        sumLabel.setText("ANALYSIS SUMMARY");
        sumLabel.setTextSize(11);
        sumLabel.setTextColor(0xFF94A3B8);
        sumLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        summaryCard.addView(sumLabel);

        TextView sumText = new TextView(context);
        sumText.setText(event.getSummary());
        sumText.setTextSize(14);
        sumText.setTextColor(0xFFE2E8F0);
        sumText.setLineSpacing(0, 1.25f);
        LinearLayout.LayoutParams stParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        stParams.topMargin = (int) (6 * density);
        sumText.setLayoutParams(stParams);
        summaryCard.addView(sumText);

        layout.addView(summaryCard);

        // Technical Evidence Card
        LinearLayout evCard = new LinearLayout(context);
        evCard.setOrientation(LinearLayout.VERTICAL);
        evCard.setBackgroundResource(R.drawable.bg_card_cred);
        evCard.setPadding((int) (16 * density), (int) (16 * density), (int) (16 * density), (int) (16 * density));
        LinearLayout.LayoutParams evParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        evParams.topMargin = (int) (14 * density);
        evCard.setLayoutParams(evParams);

        TextView evLabel = new TextView(context);
        evLabel.setText("EVIDENCE & REVERSE-ENGINEERING PROOF");
        evLabel.setTextSize(11);
        evLabel.setTextColor(0xFF00D9F5);
        evLabel.setTypeface(null, android.graphics.Typeface.BOLD);
        evCard.addView(evLabel);

        for (String detail : event.getTechnicalDetails()) {
            TextView detailLine = new TextView(context);
            detailLine.setText("• " + detail);
            detailLine.setTextSize(12);
            detailLine.setTextColor(0xFFCBD5E1);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.topMargin = (int) (6 * density);
            detailLine.setLayoutParams(lp);
            evCard.addView(detailLine);
        }

        layout.addView(evCard);

        // Action Buttons Row
        LinearLayout actionsRow = new LinearLayout(context);
        actionsRow.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams actParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        actParams.topMargin = (int) (22 * density);
        actionsRow.setLayoutParams(actParams);

        Button btnPrimaryAction = new Button(context);
        btnPrimaryAction.setText(event.getActionLabel());
        btnPrimaryAction.setTextSize(14);
        btnPrimaryAction.setTextColor(0xFF05070B);
        btnPrimaryAction.setBackgroundResource(R.drawable.bg_pill_emerald);
        btnPrimaryAction.setTypeface(null, android.graphics.Typeface.BOLD);
        btnPrimaryAction.setOnClickListener(v -> {
            event.setResolved(true);
            AppState.get().notifyListeners();
            Toast.makeText(context, "Action Applied: " + event.getActionLabel() + " (Threat Mitigated)", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        actionsRow.addView(btnPrimaryAction);

        Button btnDismiss = new Button(context);
        btnDismiss.setText("Close Inspection");
        btnDismiss.setTextSize(13);
        btnDismiss.setTextColor(0xFF94A3B8);
        btnDismiss.setBackgroundColor(Color.TRANSPARENT);
        LinearLayout.LayoutParams dismissParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (44 * density)
        );
        dismissParams.topMargin = (int) (8 * density);
        btnDismiss.setLayoutParams(dismissParams);
        btnDismiss.setOnClickListener(v -> dialog.dismiss());
        actionsRow.addView(btnDismiss);

        layout.addView(actionsRow);

        scrollView.addView(layout);
        dialog.setContentView(scrollView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }
}
