package com.excrele.kingdoms.model;

public class MemberNote {
    private String note;
    private String author;
    private long createdAt;
    private long lastModified;
    private String modifiedBy;

    public MemberNote(String note, String author) {
        this.note = note;
        this.author = author;
        this.createdAt = System.currentTimeMillis() / 1000;
        this.lastModified = this.createdAt;
        this.modifiedBy = author;
    }

    public MemberNote(String note, String author, long createdAt, long lastModified, String modifiedBy) {
        this.note = note;
        this.author = author;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.modifiedBy = modifiedBy;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
        this.lastModified = System.currentTimeMillis() / 1000;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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

    public void updateNote(String newNote, String modifier) {
        this.note = newNote;
        this.lastModified = System.currentTimeMillis() / 1000;
        this.modifiedBy = modifier;
    }
}

