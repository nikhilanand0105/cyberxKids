package dev.saathi.kids.ui.model;

import java.util.List;

/**
 * Model representing a scenario-based digital safety lesson.
 */
public class Lesson {
    private final String id;
    private final String title;
    private final String subtitle;
    private final String iconEmoji;
    private final String scenario;
    private final List<String> warningSigns;
    private final String question;
    private final List<String> options;
    private final int safeOptionIndex;
    private final String feedbackExplanation;
    private final List<String> nextSteps;
    private boolean completed;

    public Lesson(String id, String title, String subtitle, String iconEmoji,
                  String scenario, List<String> warningSigns, String question,
                  List<String> options, int safeOptionIndex, String feedbackExplanation,
                  List<String> nextSteps, boolean completed) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.iconEmoji = iconEmoji;
        this.scenario = scenario;
        this.warningSigns = warningSigns;
        this.question = question;
        this.options = options;
        this.safeOptionIndex = safeOptionIndex;
        this.feedbackExplanation = feedbackExplanation;
        this.nextSteps = nextSteps;
        this.completed = completed;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getIconEmoji() { return iconEmoji; }
    public String getEmoji() { return iconEmoji; }
    public String getScenario() { return scenario; }
    public List<String> getWarningSigns() { return warningSigns; }
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public int getSafeOptionIndex() { return safeOptionIndex; }
    public String getFeedbackExplanation() { return feedbackExplanation; }
    public List<String> getNextSteps() { return nextSteps; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
