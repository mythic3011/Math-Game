package com.mythic3011.itp4501_assignment;

/**
 * Represents the result of a game, including details such as the game's ID, play date and time,
 * duration, correct answer count, synchronization status, and player name.
 */
public class GameResult {
    private int id; // Unique identifier for the game result
    private String playDate; // The date when the game was played
    private String playTime; // The time when the game was played
    private int duration; // The duration of the game in seconds
    private int correctCount; // The number of correct answers
    private boolean synced; // Whether the game result has been synchronized with a remote database
    private String name; // The name of the player

    /**
     * Gets the game result's unique identifier.
     * @return The unique identifier for the game result.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the game result's unique identifier.
     * @param id The unique identifier for the game result.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the date when the game was played.
     * @return The play date in a String format.
     */
    public String getPlayDate() {
        return playDate;
    }

    /**
     * Sets the date when the game was played.
     * @param playDate The play date in a String format.
     */
    public void setPlayDate(String playDate) {
        this.playDate = playDate;
    }

    /**
     * Gets the time when the game was played.
     * @return The play time in a String format.
     */
    public String getPlayTime() {
        return playTime;
    }

    /**
     * Sets the time when the game was played.
     * @param playTime The play time in a String format.
     */
    public void setPlayTime(String playTime) {
        this.playTime = playTime;
    }

    /**
     * Gets the duration of the game.
     * @return The duration of the game in seconds.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the game.
     * @param duration The duration of the game in seconds.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * Gets the number of correct answers.
     * @return The number of correct answers.
     */
    public int getCorrectCount() {
        return correctCount;
    }

    /**
     * Sets the number of correct answers.
     * @param correctCount The number of correct answers.
     */
    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    /**
     * Checks if the game result has been synchronized with a remote database.
     * @return True if the game result is synchronized, false otherwise.
     */
    public boolean isSynced() {
        return synced;
    }

    /**
     * Sets the synchronization status of the game result.
     * @param synced True if the game result is synchronized, false otherwise.
     */
    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    /**
     * Gets the name of the player.
     * @return The name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     * @param name The name of the player.
     */
    public void setName(String name) {
        this.name = name;
    }
}