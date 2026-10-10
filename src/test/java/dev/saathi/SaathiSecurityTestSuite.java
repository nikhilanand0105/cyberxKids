package dev.saathi;

import dev.saathi.engine.SaathiApkIntelligence;
import dev.saathi.model.*;
import dev.saathi.parser.BinaryXmlDecoder;
import dev.saathi.parser.DexBytecodeScanner;
import dev.saathi.rules.BankingTrojanPatternRule;
import dev.saathi.rules.OtpStealerPatternRule;
import dev.saathi.rules.ThreatRuleEvaluator;
import dev.saathi.sense.ConversationRiskReport;
import dev.saathi.sense.SaathiSenseEngine;
import dev.saathi.sense.SafetyCategory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SaathiSecurityTestSuite {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("Running Saathi Security Core Verification Suite...");

        testBinaryXmlParser();
        testBankingTrojanRule();
        testOtpStealerRule();
        testVerdictDetermination();
        testHinglishSenseEngine();

        System.out.printf("\nTest Results: %d/%d tests PASSED.%n", testsPassed, testsRun);
        if (testsPassed != testsRun) {
            System.err.println("Some tests failed!");
            System.exit(1);
        }
    }

    private static void testBinaryXmlParser() {
        startTest("BinaryXmlDecoder plain text manifest parsing");
        String xml = "<manifest package=\"com.test.app\" xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                     "  <uses-permission android:name=\"android.permission.INTERNET\"/>\n" +
                     "  <uses-permission android:name=\"android.permission.READ_SMS\"/>\n" +
                     "  <application>\n" +
                     "    <activity android:name=\".MainActivity\">\n" +
                     "      <intent-filter>\n" +
                     "        <action android:name=\"android.intent.action.MAIN\"/>\n" +
                     "        <category android:name=\"android.intent.category.LAUNCHER\"/>\n" +
                     "      </intent-filter>\n" +
                     "    </activity>\n" +
                     "  </application>\n" +
                     "</manifest>";

        ManifestMetadata metadata = BinaryXmlDecoder.decode(xml.getBytes(StandardCharsets.UTF_8));
        assertEquals("com.test.app", metadata.getPackageName());
        assertTrue(metadata.hasPermission("android.permission.INTERNET"), "Has INTERNET permission");
        assertTrue(metadata.hasPermission("android.permission.READ_SMS"), "Has READ_SMS permission");
        assertTrue(metadata.hasLauncherActivity(), "Has Launcher Activity");
        pass();
    }

    private static void testBankingTrojanRule() {
        startTest("BankingTrojanPatternRule triggers on Overlay + Accessibility");
        BankingTrojanPatternRule rule = new BankingTrojanPatternRule();
        ManifestMetadata manifest = new ManifestMetadata(
                "com.evil.bank", "1.0", 1, 21, 33,
                Set.of("android.permission.SYSTEM_ALERT_WINDOW", "android.permission.BIND_ACCESSIBILITY_SERVICE"),
                List.of(), List.of("com.evil.bank.AccService"), List.of(),
                false, true, true
        );

        List<ThreatIndicator> indicators = rule.evaluate(manifest, null, null, null);
        assertFalse(indicators.isEmpty(), "Indicators should not be empty");
        assertEquals("ATS_CRITICAL_COMBO", indicators.get(0).getId());
        assertEquals(ThreatSeverity.CRITICAL, indicators.get(0).getSeverity());
        pass();
    }

    private static void testOtpStealerRule() {
        startTest("OtpStealerPatternRule triggers on SMS + Dynamic Dex loading");
        OtpStealerPatternRule rule = new OtpStealerPatternRule();
        ManifestMetadata manifest = new ManifestMetadata(
                "com.sms.stealer", "1.0", 1, 21, 33,
                Set.of("android.permission.READ_SMS"),
                List.of(), List.of(), List.of(),
                false, false, false
        );

        DexInspectionResult dex = new DexInspectionResult(
                1, 10, 20, Set.of(), List.of(), List.of(),
                true, false, false, false, false, false
        );

        List<ThreatIndicator> indicators = rule.evaluate(manifest, dex, null, null);
        assertFalse(indicators.isEmpty(), "Indicators should not be empty");
        assertEquals("SMS_DYNAMIC_CODE_STEALER", indicators.get(0).getId());
        assertEquals(ThreatSeverity.CRITICAL, indicators.get(0).getSeverity());
        pass();
    }

    private static void testVerdictDetermination() {
        startTest("ThreatRuleEvaluator maps critical indicators to HIGH_RISK / KNOWN_MALICIOUS");
        ThreatRuleEvaluator evaluator = new ThreatRuleEvaluator();

        // Critical indicator -> HIGH_RISK
        List<ThreatIndicator> criticalIndicators = List.of(
                new ThreatIndicator("C1", "Critical", "Test", ThreatSeverity.CRITICAL, "TEST")
        );
        ScanVerdict v1 = evaluator.determineVerdict("dummy_hash", criticalIndicators, true);
        assertEquals(ScanVerdict.HIGH_RISK, v1);

        // Empty indicators -> NO_KNOWN_THREAT_DETECTED
        ScanVerdict v2 = evaluator.determineVerdict("dummy_hash", Collections.emptyList(), true);
        assertEquals(ScanVerdict.NO_KNOWN_THREAT_DETECTED, v2);

        // Known hash -> KNOWN_MALICIOUS
        String knownHash = "3a019b88f8d2dfbb187b47b4d187bdf559ef14c1d686f0b4d45543c8d1720d29";
        ScanVerdict v3 = evaluator.determineVerdict(knownHash, Collections.emptyList(), true);
        assertEquals(ScanVerdict.KNOWN_MALICIOUS, v3);
        pass();
    }

    private static void testHinglishSenseEngine() {
        startTest("SaathiSenseEngine accurately separates benign birthday secret from predatory coercion");
        SaathiSenseEngine engine = new SaathiSenseEngine();

        // 1. Benign secret
        ConversationRiskReport r1 = engine.analyze("Kal Rahul ki surprise birthday party hai, kisi ko mat batana");
        assertEquals(SafetyCategory.BENIGN, r1.getCategory());
        assertFalse(r1.shouldPromptSos(), "Should not trigger SOS");

        // 2. Predatory coercion
        ConversationRiskReport r2 = engine.analyze("Camera on kar video call pe, kisi ko mat batana, gift dunga");
        assertEquals(SafetyCategory.GROOMING_MANIPULATION, r2.getCategory());
        assertEquals(ThreatSeverity.CRITICAL, r2.getSeverity());
        assertTrue(r2.shouldPromptSos(), "Should trigger SOS prompt");

        // 3. Gaming fraud
        ConversationRiskReport r3 = engine.analyze("Free diamonds ke liye UPI PIN daal de");
        assertEquals(SafetyCategory.CHILD_FINANCIAL_SCAM, r3.getCategory());
        pass();
    }

    private static void startTest(String name) {
        testsRun++;
        System.out.printf("Test %d: %s ... ", testsRun, name);
    }

    private static void pass() {
        testsPassed++;
        System.out.println("PASSED");
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("Expected [%s] but was [%s]", expected, actual));
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError("Condition failed: " + msg);
        }
    }

    private static void assertFalse(boolean condition, String msg) {
        if (condition) {
            throw new AssertionError("Condition should be false: " + msg);
        }
    }
}
