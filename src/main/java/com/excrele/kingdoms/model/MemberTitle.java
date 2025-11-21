package com.excrele.kingdoms.model;

public class MemberTitle {
    private String title;
    private String color;
    private long setAt;
    private String setBy;

    public MemberTitle(String title, String color, String setBy) {
        this.title = title;
        this.color = color != null ? color : "§7"; // Default gray
        this.setAt = System.currentTimeMillis() / 1000;
        this.setBy = setBy;
    }

    public MemberTitle(String title, String color, long setAt, String setBy) {
        this.title = title;
        this.color = color != null ? color : "§7";
        this.setAt = setAt;
        this.setBy = setBy;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color != null ? color : "§7";
    }

    public long getSetAt() {
        return setAt;
    }

    public void setSetAt(long setAt) {
        this.setAt = setAt;
    }

    public String getSetBy() {
        return setBy;
    }

    public void setSetBy(String setBy) {
        this.setBy = setBy;
    }

    public String getFormattedTitle() {
        return color + title;
    }
}

