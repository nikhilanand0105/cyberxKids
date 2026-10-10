package dev.saathi.kids.ui.model;

import java.util.List;

/**
 * Standardized child-friendly result model for message, link, and file checks.
 */
public class CheckResult {
    private final CheckVerdict verdict;
    private final String summary;
    private final List<String> warningSigns;
    private final String whyItMatters;
    private final List<String> nextSteps;
    private final boolean isDemo;
    private final String limitationsNote;

    public CheckResult(CheckVerdict verdict, String summary, List<String> warningSigns,
                       String whyItMatters, List<String> nextSteps, boolean isDemo,
                       String limitationsNote) {
        this.verdict = verdict;
        this.summary = summary;
        this.warningSigns = warningSigns;
        this.whyItMatters = whyItMatters;
        this.nextSteps = nextSteps;
        this.isDemo = isDemo;
        this.limitationsNote = limitationsNote;
    }

    public CheckVerdict getVerdict() { return verdict; }
    public String getSummary() { return summary; }
    public List<String> getWarningSigns() { return warningSigns; }
    public String getWhyItMatters() { return whyItMatters; }
    public List<String> getNextSteps() { return nextSteps; }
    public boolean isDemo() { return isDemo; }
    public String getLimitationsNote() { return limitationsNote; }
}
