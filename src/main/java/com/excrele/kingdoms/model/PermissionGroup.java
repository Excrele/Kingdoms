package com.excrele.kingdoms.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a permission group/template that can be applied to members
 */
public class PermissionGroup {
    private final String name;
    private final Set<String> permissions;
    private final String description;
    private final boolean isDefault;
    
    public PermissionGroup(String name, Set<String> permissions, String description, boolean isDefault) {
        this.name = name;
        this.permissions = permissions != null ? permissions : new HashSet<>();
        this.description = description;
        this.isDefault = isDefault;
    }
    
    public String getName() {
        return name;
    }
    
    public Set<String> getPermissions() {
        return permissions;
    }
    
    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
    }
    
    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission.toLowerCase());
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isDefault() {
        return isDefault;
    }
}

