package dev.saathi.kids.ui.model;

/**
 * Model representing a configured trusted adult for the child.
 */
public class TrustedAdult {
    private final String id;
    private final String name;
    private final String relation;
    private final String contactInfo;
    private final boolean isPrimary;

    public TrustedAdult(String id, String name, String relation, String contactInfo, boolean isPrimary) {
        this.id = id;
        this.name = name;
        this.relation = relation;
        this.contactInfo = contactInfo;
        this.isPrimary = isPrimary;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRelation() {
        return relation;
    }

    public String getRelationship() {
        return relation;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public String getPhoneNumber() {
        return contactInfo;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
