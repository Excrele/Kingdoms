package com.excrele.kingdoms.model;

import java.util.HashSet;
import java.util.Set;

public class MemberPermission {
    private Set<String> allowedPermissions;
    private Set<String> deniedPermissions;
    private long lastModified;
    private String modifiedBy;

    public MemberPermission() {
        this.allowedPermissions = new HashSet<>();
        this.deniedPermissions = new HashSet<>();
        this.lastModified = System.currentTimeMillis() / 1000;
    }

    public MemberPermission(Set<String> allowedPermissions, Set<String> deniedPermissions, long lastModified, String modifiedBy) {
        this.allowedPermissions = allowedPermissions != null ? allowedPermissions : new HashSet<>();
        this.deniedPermissions = deniedPermissions != null ? deniedPermissions : new HashSet<>();
        this.lastModified = lastModified;
        this.modifiedBy = modifiedBy;
    }

    public Set<String> getAllowedPermissions() {
        return allowedPermissions;
    }

    public void setAllowedPermissions(Set<String> allowedPermissions) {
        this.allowedPermissions = allowedPermissions != null ? allowedPermissions : new HashSet<>();
    }

    public Set<String> getDeniedPermissions() {
        return deniedPermissions;
    }

    public void setDeniedPermissions(Set<String> deniedPermissions) {
        this.deniedPermissions = deniedPermissions != null ? deniedPermissions : new HashSet<>();
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public void allowPermission(String permission) {
        allowedPermissions.add(permission.toLowerCase());
        deniedPermissions.remove(permission.toLowerCase());
        lastModified = System.currentTimeMillis() / 1000;
    }

    public void denyPermission(String permission) {
        deniedPermissions.add(permission.toLowerCase());
        allowedPermissions.remove(permission.toLowerCase());
        lastModified = System.currentTimeMillis() / 1000;
    }

    public void removePermission(String permission) {
        String perm = permission.toLowerCase();
        allowedPermissions.remove(perm);
        deniedPermissions.remove(perm);
        lastModified = System.currentTimeMillis() / 1000;
    }

    public boolean hasPermission(String permission) {
        String perm = permission.toLowerCase();
        if (deniedPermissions.contains(perm)) {
            return false;
        }
        return allowedPermissions.contains(perm);
    }

    public boolean hasOverride(String permission) {
        String perm = permission.toLowerCase();
        return allowedPermissions.contains(perm) || deniedPermissions.contains(perm);
    }
}

