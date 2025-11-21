package com.excrele.kingdoms.model;

public enum TrustPermission {
    BUILD("build", "Place and break blocks"),
    INTERACT("interact", "Interact with blocks (doors, buttons, etc.)"),
    USE("use", "Use items and blocks"),
    REDSTONE("redstone", "Activate redstone"),
    CONTAINER("container", "Access containers (chests, furnaces, etc.)"),
    TELEPORT("teleport", "Teleport into claim"),
    FLY("fly", "Fly in claim"),
    ENDERPEARL("enderpearl", "Use ender pearls"),
    CHORUS_FRUIT("chorus_fruit", "Use chorus fruit"),
    PISTON("piston", "Use pistons"),
    ANIMAL_BREED("animal_breed", "Breed animals"),
    CROP_TRAMPLE("crop_trample", "Trample crops"),
    ALL("all", "All permissions");

    private final String key;
    private final String description;

    TrustPermission(String key, String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public static TrustPermission fromKey(String key) {
        for (TrustPermission perm : values()) {
            if (perm.key.equalsIgnoreCase(key)) {
                return perm;
            }
        }
        return null;
    }
}

