package com.excrele.kingdoms.model;

public class PlayerChallengeData {
    private int timesCompleted;
    private long lastCompleted;
    private int progress;

    public int getTimesCompleted() { return timesCompleted; }
    public void setTimesCompleted(int timesCompleted) { this.timesCompleted = timesCompleted; }
    public long getLastCompleted() { return lastCompleted; }
    public void setLastCompleted(long lastCompleted) { this.lastCompleted = lastCompleted; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public void incrementProgress(int amount) { this.progress += amount; }
}