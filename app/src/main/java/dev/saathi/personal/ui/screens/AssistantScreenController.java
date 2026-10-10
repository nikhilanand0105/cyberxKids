package dev.saathi.personal.ui.screens;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dev.saathi.personal.R;
import dev.saathi.personal.ui.model.ChatMessage;
import dev.saathi.personal.ui.state.AppState;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AssistantScreenController {

    private final Activity activity;
    private final float density;
    private ScrollView rootView;

    private LinearLayout containerMessages;
    private ScrollView svChat;
    private EditText etInput;

    public AssistantScreenController(Activity activity) {
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
        rootView.setBackgroundColor(0xFF05070B);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int) (20 * density), (int) (16 * density), (int) (20 * density), (int) (100 * density));

        // 1. Header & Privacy Badge
        layout.addView(buildHeader());

        // 2. Suggestion Prompt Pills
        layout.addView(buildSuggestionPills());

        // 3. Chat Messages Stream Container
        svChat = new ScrollView(activity);
        LinearLayout.LayoutParams scp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (380 * density)
        );
        scp.topMargin = (int) (8 * density);
        scp.bottomMargin = (int) (12 * density);
        svChat.setLayoutParams(scp);
        svChat.setBackgroundResource(R.drawable.bg_card_cred);
        svChat.setPadding((int) (12 * density), (int) (12 * density), (int) (12 * density), (int) (12 * density));

        containerMessages = new LinearLayout(activity);
        containerMessages.setOrientation(LinearLayout.VERTICAL);
        svChat.addView(containerMessages);
        layout.addView(svChat);

        // 4. Input Row
        layout.addView(buildInputRow());

        rootView.addView(layout);
        render();
    }

    private View buildHeader() {
        LinearLayout header = new LinearLayout(activity);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(0, 0, 0, (int) (10 * density));

        TextView title = new TextView(activity);
        title.setText("SAATHI AI GUARDIAN COPILOT");
        title.setTextSize(22);
        title.setTextColor(0xFFFFFFFF);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(title);

        TextView sub = new TextView(activity);
        sub.setText("Conversational security intelligence running privately on Snapdragon 8 Elite NPU");
        sub.setTextSize(13);
        sub.setTextColor(0xFF94A3B8);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sp.topMargin = (int) (4 * density);
        sub.setLayoutParams(sp);
        header.addView(sub);

        TextView tvPrivacy = new TextView(activity);
        tvPrivacy.setText("🔒 ZERO CLOUD TELEMETRY • NPU ISOLATED • 100% PRIVATE");
        tvPrivacy.setTextSize(10);
        tvPrivacy.setTextColor(0xFF00F5A0);
        tvPrivacy.setBackgroundResource(R.drawable.bg_pill_emerald);
        tvPrivacy.setPadding((int) (10 * density), (int) (5 * density), (int) (10 * density), (int) (5 * density));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        pp.topMargin = (int) (8 * density);
        tvPrivacy.setLayoutParams(pp);
        header.addView(tvPrivacy);

        return header;
    }

    private View buildSuggestionPills() {
        HorizontalScrollView hsv = new HorizontalScrollView(activity);
        hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        hp.topMargin = (int) (6 * density);
        hp.bottomMargin = (int) (6 * density);
        hsv.setLayoutParams(hp);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);

        row.addView(createSuggestionChip("What is a Trojan APK?", "What is a Trojan APK and why is it dangerous?"));
        row.addView(createSuggestionChip("Detect Child Grooming", "How does Saathi detect online grooming and secrecy cues?"));
        row.addView(createSuggestionChip("When to call 1930?", "What is the 1930 National Cyber Helpline and how quickly should I call?"));
        row.addView(createSuggestionChip("Is GB WhatsApp safe?", "Is modified WhatsApp or GB WhatsApp safe to install?"));
        row.addView(createSuggestionChip("साइबर सुरक्षा टिप्स", "कृपया मुझे ऑनलाइन फ्रॉड से बचने के ज़रूरी टिप्स बताइए।"));

        hsv.addView(row);
        return hsv;
    }

    private Button createSuggestionChip(String label, String prompt) {
        Button chip = new Button(activity);
        chip.setText(label);
        chip.setTextSize(11);
        chip.setTextColor(0xFF00D9F5);
        chip.setBackgroundResource(R.drawable.bg_pill_cyan);
        chip.setOnClickListener(v -> sendMessage(prompt));

        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                (int) (36 * density)
        );
        cp.rightMargin = (int) (8 * density);
        chip.setLayoutParams(cp);
        return chip;
    }

    private View buildInputRow() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        etInput = new EditText(activity);
        etInput.setHint("Ask Saathi anything about digital safety...");
        etInput.setHintTextColor(0xFF64748B);
        etInput.setTextColor(0xFFFFFFFF);
        etInput.setTextSize(13);
        etInput.setBackgroundResource(R.drawable.bg_input_field);
        etInput.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (12 * density));
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        ip.rightMargin = (int) (8 * density);
        etInput.setLayoutParams(ip);
        row.addView(etInput);

        Button btnSend = new Button(activity);
        btnSend.setText("Send");
        btnSend.setTextSize(13);
        btnSend.setTextColor(0xFF05070B);
        btnSend.setBackgroundResource(R.drawable.bg_pill_cyan);
        btnSend.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSend.setOnClickListener(v -> {
            String text = etInput.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etInput.setText("");
            }
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                (int) (80 * density),
                (int) (46 * density)
        );
        btnSend.setLayoutParams(bp);
        row.addView(btnSend);

        return row;
    }

    public void render() {
        containerMessages.removeAllViews();
        List<ChatMessage> list = AppState.get().getChatMessages();
        for (ChatMessage msg : list) {
            containerMessages.addView(createMessageBubble(msg));
        }

        // Scroll to bottom
        svChat.post(() -> svChat.fullScroll(View.FOCUS_DOWN));
    }

    private View createMessageBubble(ChatMessage msg) {
        LinearLayout bubbleContainer = new LinearLayout(activity);
        bubbleContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cp.bottomMargin = (int) (12 * density);
        bubbleContainer.setLayoutParams(cp);

        LinearLayout bubble = new LinearLayout(activity);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding((int) (14 * density), (int) (10 * density), (int) (14 * density), (int) (10 * density));

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        bp.gravity = msg.isUser ? Gravity.END : Gravity.START;
        bubble.setLayoutParams(bp);

        if (msg.isUser) {
            bubble.setBackgroundResource(R.drawable.bg_pill_cyan);
        } else {
            bubble.setBackgroundResource(R.drawable.bg_card_elevated);
        }

        TextView tvSender = new TextView(activity);
        tvSender.setText(msg.badge);
        tvSender.setTextSize(10);
        tvSender.setTextColor(msg.isUser ? 0xFF05070B : 0xFF00F5A0);
        tvSender.setTypeface(null, android.graphics.Typeface.BOLD);
        bubble.addView(tvSender);

        TextView tvText = new TextView(activity);
        tvText.setText(msg.text);
        tvText.setTextSize(13);
        tvText.setTextColor(msg.isUser ? 0xFF05070B : 0xFFFFFFFF);
        tvText.setLineSpacing(0, 1.25f);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tp.topMargin = (int) (4 * density);
        tvText.setLayoutParams(tp);
        bubble.addView(tvText);

        TextView tvTime = new TextView(activity);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        tvTime.setText(sdf.format(msg.timestamp));
        tvTime.setTextSize(9);
        tvTime.setTextColor(msg.isUser ? 0xFF1E293B : 0xFF64748B);
        LinearLayout.LayoutParams tmp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        tmp.topMargin = (int) (4 * density);
        tmp.gravity = Gravity.END;
        tvTime.setLayoutParams(tmp);
        bubble.addView(tvTime);

        bubbleContainer.addView(bubble);
        return bubbleContainer;
    }

    private void sendMessage(String prompt) {
        // Add User Message
        AppState.get().addChatMessage(new ChatMessage(true, prompt, "You"));
        render();

        // Simulate fast on-device inference response (< 300ms)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String answer = generateOnDeviceAnswer(prompt);
            AppState.get().addChatMessage(new ChatMessage(false, answer, "Saathi Guardian AI"));
            render();
        }, 250);
    }

    private String generateOnDeviceAnswer(String query) {
        String lower = query.toLowerCase(Locale.ROOT);

        if (lower.contains("trojan") || lower.contains("apk") || lower.contains("whatsapp")) {
            return "🛡️ Android Banking Trojans & Modded APKs:\n\n" +
                    "Fake or modified APKs (like GB WhatsApp or fake banking updates) often bundle malicious DEX payloads. " +
                    "They abuse Android's BIND_ACCESSIBILITY_SERVICE to read screen contents, inject invisible tap overlays (SYSTEM_ALERT_WINDOW), " +
                    "and intercept incoming SMS to steal OTPs.\n\n" +
                    "Saathi inspects these packages statically before installation on your iQOO 15 without ever executing untrusted code.";
        }

        if (lower.contains("grooming") || lower.contains("child") || lower.contains("secret")) {
            return "👁️ Child Online Safety & Grooming Detection:\n\n" +
                    "Groomers manipulate children using 3 consistent psychological cues:\n" +
                    "1. Isolation / Secrecy: 'Mummy-papa ko mat batana'.\n" +
                    "2. Platform Hopping: Moving from public gaming chats to private disappearing apps.\n" +
                    "3. Boundary Erosion: Asking to turn on video cameras or demanding private photos.\n\n" +
                    "Saathi Sense runs on-device Hinglish NLP to flag these cues immediately without uploading personal chats to the cloud.";
        }

        if (lower.contains("1930") || lower.contains("helpline") || lower.contains("fraud") || lower.contains("money")) {
            return "📞 National Cyber Crime Helpline (1930):\n\n" +
                    "If you or a family member have been tricked into transferring money or sharing an OTP, dial 1930 immediately.\n\n" +
                    "Under India's Citizen Financial Cyber Fraud Reporting System (CFCFRMS), calls made within the 'Golden Hour' (first 2 hours) allow law enforcement to freeze the fraudster's bank accounts and UPI nodes before funds are withdrawn at ATMs.";
        }

        if (lower.contains("हिंदी") || lower.contains("hindi") || lower.contains("टिप्स")) {
            return "🛡️ आवश्यक डिजिटल सुरक्षा सलाह (हिन्दी):\n\n" +
                    "1. कभी भी किसी अनजान कॉलर के कहने पर कोई ऐप (.apk) डाउनलोड न करें।\n" +
                    "2. बैंक या बिजली बिल के नाम पर आए अनजान लिंक (SMS) पर क्लिक न करें।\n" +
                    "3. यदि पैसे कट जाएं, तो तुरंत 1930 पर कॉल करें।\n" +
                    "4. बच्चों को सिखाएं कि इंटरनेट पर किसी अजनबी से गुप्त बातें या फ़ोटो कभी साझा न करें।";
        }

        return "Saathi AI Guardian is monitoring your device actively. " +
                "I analyze APK structures, conversation manipulation cues, and fraudulent bank links in real time. " +
                "Feel free to ask about any specific app, link, or security question!";
    }
}
