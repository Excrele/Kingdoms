package com.excrele.kingdoms.model;

public enum MemberRole {
    KING("King", 100), // Highest permissions
    ADVISOR("Advisor", 80), // Can invite, set flags, claim/unclaim
    GUARD("Guard", 60), // Can claim, set plot flags
    BUILDER("Builder", 40), // Can claim, set plot types
    MEMBER("Member", 20); // Basic member, can only contribute

    private final String displayName;
    private final int permissionLevel;

    MemberRole(String displayName, int permissionLevel) {
        this.displayName = displayName;
        this.permissionLevel = permissionLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    public boolean canInvite() {
        return this == KING || this == ADVISOR;
    }

    public boolean canClaim() {
        return this != MEMBER;
    }

    public boolean canUnclaim() {
        return this == KING || this == ADVISOR;
    }

    public boolean canSetFlags() {
        return this == KING || this == ADVISOR;
    }

    public boolean canSetPlotFlags() {
        return this != MEMBER;
    }

    public boolean canSetPlotType() {
        return this != MEMBER;
    }

    public boolean canLevelUp() {
        return this == KING;
    }

    public boolean canKick() {
        return this == KING || this == ADVISOR;
    }

    public boolean canPromote() {
        return this == KING;
    }
}

