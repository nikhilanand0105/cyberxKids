package dev.saathi.personal.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.ai.HindiExplanationSynthesizer;
import dev.saathi.engine.ApkInstallHandoffHelper;
import dev.saathi.engine.SaathiApkIntelligence;
import dev.saathi.model.ApkAnalysisReport;
import dev.saathi.model.ScanVerdict;
import dev.saathi.model.ThreatIndicator;
import dev.saathi.personal.R;
import dev.saathi.sense.ConversationRiskReport;
import dev.saathi.sense.SaathiSenseEngine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Main Activity for Saathi Personal on Android 16 (iQOO 15).
 * Optimized for Qualcomm Snapdragon 8 Elite, 144Hz LTPO display, and Funtouch OS power limits.
 */
public class MainActivity extends Activity {

    private boolean isSeniorMode = false;
    private boolean isHindi = false;

    private SaathiApkIntelligence apkEngine;
    private SaathiSenseEngine senseEngine;

    private TextView tvDevicePill;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private Button btnToggleSenior;
    private Button btnToggleLang;
    private Button btnCall1930;
    private TextView tvHelplineTitle;
    private TextView tvHelplineDesc;
    private TextView tvVerdictBadge;
    private TextView tvVerdictAdvice;
    private TextView tvVerdictEvidence;
    private Button btnInstallAction;
    private TextView tvSenseStatus;

    private ApkAnalysisReport lastReport = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hardware Optimization: Request 144Hz refresh rate on iQOO 15 display
        optimizeDisplayForIqoo15();

        setContentView(R.layout.activity_main);

        apkEngine = new SaathiApkIntelligence();
        senseEngine = new SaathiSenseEngine();

        initViews();
        setupListeners();

        // Check if launched via file share / ACTION_VIEW (e.g. downloaded APK from Chrome)
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
                // Handle top camera hole cutout (1440x3168 display)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                }
                getWindow().setAttributes(params);
            } catch (Exception ignored) {
            }
        }
    }

    private void initViews() {
        tvDevicePill = findViewById(R.id.tv_device_pill);
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        btnToggleSenior = findViewById(R.id.btn_toggle_senior);
        btnToggleLang = findViewById(R.id.btn_toggle_lang);
        btnCall1930 = findViewById(R.id.btn_call_1930);
        tvHelplineTitle = findViewById(R.id.tv_helpline_title);
        tvHelplineDesc = findViewById(R.id.tv_helpline_desc);
        tvVerdictBadge = findViewById(R.id.tv_verdict_badge);
        tvVerdictAdvice = findViewById(R.id.tv_verdict_advice);
        tvVerdictEvidence = findViewById(R.id.tv_verdict_evidence);
        btnInstallAction = findViewById(R.id.btn_install_action);
        tvSenseStatus = findViewById(R.id.tv_sense_status);

        // Display detected SoC and hardware
        String socInfo = Build.HARDWARE + " (" + Build.SOC_MODEL + ")";
        tvDevicePill.setText("⚡ iQOO 15 • Snapdragon 8 Elite (" + Build.SOC_MODEL + ") • 144Hz Active");
    }

    private void setupListeners() {
        btnToggleSenior.setOnClickListener(v -> toggleSeniorMode());
        btnToggleLang.setOnClickListener(v -> toggleLanguage());

        btnCall1930.setOnClickListener(v -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:1930"));
            startActivity(callIntent);
        });

        findViewById(R.id.btn_simulate_banking_trojan).setOnClickListener(v -> runSimulation(true));
        findViewById(R.id.btn_simulate_clean_apk).setOnClickListener(v -> runSimulation(false));

        btnInstallAction.setOnClickListener(v -> {
            if (lastReport != null && lastReport.canSafelyProceedWithInstall()) {
                var intentDesc = ApkInstallHandoffHelper.createSystemInstallIntent("content://dev.saathi.personal.fileprovider/scans/sample.apk");
                Toast.makeText(this, "Handoff to System PackageInstaller: " + intentDesc.action, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Installation blocked: High threat severity detected.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleSeniorMode() {
        isSeniorMode = !isSeniorMode;
        float density = getResources().getDisplayMetrics().density;
        if (isSeniorMode) {
            btnToggleSenior.setText(isHindi ? "सीनियर मोड: चालू" : "Senior Mode: ON");
            tvTitle.setTextSize(26);
            tvVerdictAdvice.setTextSize(16);
            btnCall1930.setTextSize(16);
            btnCall1930.getLayoutParams().height = (int) (64 * density);
            btnCall1930.requestLayout();
        } else {
            btnToggleSenior.setText(isHindi ? "सीनियर मोड: बंद" : "Senior Mode: OFF");
            tvTitle.setTextSize(22);
            tvVerdictAdvice.setTextSize(13);
            btnCall1930.setTextSize(14);
            btnCall1930.getLayoutParams().height = (int) (52 * density);
            btnCall1930.requestLayout();
        }
        if (lastReport != null) {
            renderReport(lastReport);
        }
    }

    private void toggleLanguage() {
        isHindi = !isHindi;
        btnToggleLang.setText(isHindi ? "भाषा: हिन्दी" : "Language: English");
        btnToggleSenior.setText(isSeniorMode ? (isHindi ? "सीनियर मोड: चालू" : "Senior Mode: ON") : (isHindi ? "सीनियर मोड: बंद" : "Senior Mode: OFF"));
        
        if (isHindi) {
            tvSubtitle.setText("डिजिटल सुरक्षा के लिए आपका AI साथी");
            tvHelplineTitle.setText("📞 राष्ट्रीय साइबर हेल्पलाइन (1930)");
            tvHelplineDesc.setText("यदि किसी अनजान व्यक्ति ने आपको पैसे भेजने या OTP साझा करने को कहा है, तो तुरंत 1930 पर कॉल करें।");
            btnCall1930.setText("तुरंत 1930 पर कॉल करें");
        } else {
            tvSubtitle.setText("Your AI Companion for Digital Safety");
            tvHelplineTitle.setText("📞 National Cyber Crime Helpline (India)");
            tvHelplineDesc.setText("If an unknown caller persuaded you to send money or share an OTP, dial 1930 immediately to freeze fraudulent bank transactions.");
            btnCall1930.setText("Call 1930 Immediately");
        }

        if (lastReport != null) {
            renderReport(lastReport);
        }
    }

    private void runSimulation(boolean isTrojan) {
        byte[] apkBytes;
        String name;

        if (isTrojan) {
            name = "QuickBank_Update_Trojan.apk";
            apkBytes = createSyntheticApk(
                    "com.fake.quickbank",
                    "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.fake.quickbank\">\n" +
                    "    <uses-permission android:name=\"android.permission.SYSTEM_ALERT_WINDOW\" />\n" +
                    "    <uses-permission android:name=\"android.permission.INTERNET\" />\n" +
                    "    <application>\n" +
                    "        <service android:name=\".ATS\" android:permission=\"android.permission.BIND_ACCESSIBILITY_SERVICE\" />\n" +
                    "    </application>\n" +
                    "</manifest>",
                    true
            );
        } else {
            name = "CleanCalculator.apk";
            apkBytes = createSyntheticApk(
                    "com.simple.calculator",
                    "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" package=\"com.simple.calculator\">\n" +
                    "    <application>\n" +
                    "        <activity android:name=\".Main\">\n" +
                    "            <intent-filter>\n" +
                    "                <action android:name=\"android.intent.action.MAIN\" />\n" +
                    "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
                    "            </intent-filter>\n" +
                    "        </activity>\n" +
                    "    </application>\n" +
                    "</manifest>",
                    false
            );
        }

        lastReport = apkEngine.analyzeApk(name, new ByteArrayInputStream(apkBytes), apkBytes.length);
        renderReport(lastReport);
    }

    private void renderReport(ApkAnalysisReport report) {
        if (report.getVerdict() == ScanVerdict.HIGH_RISK || report.getVerdict() == ScanVerdict.KNOWN_MALICIOUS) {
            tvVerdictBadge.setBackgroundColor(0xFFB91C1C); // Red
            tvVerdictBadge.setText(isHindi ? "ख़तरा: अत्यधिक जोखिम" : "VERDICT: HIGH RISK");
            tvVerdictAdvice.setText(isHindi ? HindiExplanationSynthesizer.explainApkInHindi(report) : report.getPlainLanguageExplanation());
            btnInstallAction.setEnabled(false);
            btnInstallAction.setAlpha(0.4f);
            btnInstallAction.setText(isHindi ? "इंस्टॉलेशन अस्वीकृत (ख़तरा)" : "Installation Blocked (Threat)");
        } else {
            tvVerdictBadge.setBackgroundColor(0xFF047857); // Green
            tvVerdictBadge.setText(isHindi ? "जांच: कोई ख़तरा नहीं मिला" : "VERDICT: NO KNOWN THREAT");
            tvVerdictAdvice.setText(isHindi ? HindiExplanationSynthesizer.explainApkInHindi(report) : report.getPlainLanguageExplanation());
            btnInstallAction.setEnabled(true);
            btnInstallAction.setAlpha(1.0f);
            btnInstallAction.setText(isHindi ? "सिस्टम इंस्टॉलर के जरिए आगे बढ़ें" : "Proceed via System Installer");
        }

        StringBuilder ev = new StringBuilder("Evidence: ");
        if (report.getIndicators().isEmpty()) {
            ev.append("None (Clean static baseline)");
        } else {
            for (ThreatIndicator ti : report.getIndicators()) {
                ev.append("• ").append(ti.getTitle()).append(" ");
            }
        }
        tvVerdictEvidence.setText(ev.toString());
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            try {
                InputStream is = getContentResolver().openInputStream(data);
                if (is != null) {
                    lastReport = apkEngine.analyzeApk("downloaded_package.apk", is, 0);
                    renderReport(lastReport);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Could not open APK stream: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private byte[] createSyntheticApk(String pkg, String xml, boolean trojan) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("AndroidManifest.xml"));
            zos.write(xml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("classes.dex"));
            byte[] dex = new byte[256];
            dex[0] = 'd'; dex[1] = 'e'; dex[2] = 'x'; dex[3] = '\n';
            dex[4] = '0'; dex[5] = '3'; dex[6] = '5'; dex[7] = 0;
            if (trojan) {
                byte[] sig = "findAccessibilityNodeInfosByViewId".getBytes(StandardCharsets.UTF_8);
                System.arraycopy(sig, 0, dex, 120, sig.length);
            }
            zos.write(dex);
            zos.closeEntry();
            zos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
