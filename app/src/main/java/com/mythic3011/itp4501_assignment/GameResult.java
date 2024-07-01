package com.mythic3011.itp4501_assignment;

public class GameResult {
    private int id;
    private String playDate;
    private String playTime;
    private int duration;
    private int correctCount;
    private boolean synced;
    private String name;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getPlayDate() {
        return playDate;
    }
    public void setPlayDate(String playDate) {
        this.playDate = playDate;
    }
    public String getPlayTime() {
        return playTime;
    }
    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }
    public int getCorrectCount() {
        return correctCount;
    }
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }
    public boolean isSynced() {
        return synced;
    }
    public void setSynced(boolean synced) {
        this.synced = synced;
    }


    public void setName(String string) {
        this.name = string;
    }

    public String getName() {
        return name;
    }
}
