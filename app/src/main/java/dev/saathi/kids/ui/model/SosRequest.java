package dev.saathi.kids.ui.model;

/**
 * Model representing a child's help request.
 */
public class SosRequest {
    private final String id;
    private final String category;
    private final String trustedAdultName;
    private final String trustedAdultContact;
    private final String previewMessage;
    private final long timestamp;
    private final boolean isDemo;

    public SosRequest(String id, String category, String trustedAdultName,
                      String trustedAdultContact, String previewMessage,
                      long timestamp, boolean isDemo) {
        this.id = id;
        this.category = category;
        this.trustedAdultName = trustedAdultName;
        this.trustedAdultContact = trustedAdultContact;
        this.previewMessage = previewMessage;
        this.timestamp = timestamp;
        this.isDemo = isDemo;
    }

    public String getId() { return id; }
    public String getCategory() { return category; }
    public String getTrustedAdultName() { return trustedAdultName; }
    public String getTrustedAdultContact() { return trustedAdultContact; }
    public String getPreviewMessage() { return previewMessage; }
    public long getTimestamp() { return timestamp; }
    public boolean isDemo() { return isDemo; }
}
