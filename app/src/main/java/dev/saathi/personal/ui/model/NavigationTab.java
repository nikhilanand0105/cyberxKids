package dev.saathi.personal.ui.model;

public enum NavigationTab {
    HOME("Home"),
    INSIGHTS("Insights"),
    ACTIVITY("Activity"),
    ASSISTANT("Assistant"),
    GUARDIAN("Guardian");

    private final String label;

    NavigationTab(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
