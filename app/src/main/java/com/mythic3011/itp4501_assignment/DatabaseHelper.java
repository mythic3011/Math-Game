package com.mythic3011.itp4501_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing the application database. It handles the creation, upgrade, and basic data operations for the game results.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version and Name constants.
    private static final String DATABASE_NAME = "MathGameDB";
    private static final int DATABASE_VERSION = 2;

    // Table and Columns names constants.
    private static final String TABLE_GAMES = "games";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PLAY_DATE = "play_date";
    private static final String COLUMN_PLAY_TIME = "play_time";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_CORRECT_COUNT = "correct_count";
    private static final String COLUMN_SYNCED = "synced";
    private static final String COLUMN_NAME = "name";

    /**
     * Constructor for DatabaseHelper.
     *
     * @param context The context of the caller.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_GAMES_TABLE = "CREATE TABLE " + TABLE_GAMES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PLAY_DATE + " TEXT,"
                + COLUMN_PLAY_TIME + " TEXT,"
                + COLUMN_DURATION + " INTEGER,"
                + COLUMN_CORRECT_COUNT + " INTEGER,"
                + COLUMN_SYNCED + " INTEGER DEFAULT 0,"
                + COLUMN_NAME + " TEXT"
                + ")";
        db.execSQL(CREATE_GAMES_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. This method will only be called if a database already exists on disk with the same DATABASE_NAME,
     * but the DATABASE_VERSION is different than the version of the database that exists on disk.
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_GAMES + " ADD COLUMN " + COLUMN_NAME + " TEXT");
        }
    }

    /**
     * Retrieves all game results from the database.
     * This method queries the database for all entries in the games table, orders them by their ID in descending order,
     * and then constructs a list of GameResult objects from the query results. Each GameResult object represents a single game's results,
     * including the game's ID, play date, play time, duration, correct answer count, sync status, and player name.
     *
     * @return A list of {@link GameResult} objects, each representing the results of a single game. If no games are found, returns an empty list.
     */
    public List<GameResult> getAllGameResults() {
        List<GameResult> gameResults = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GAMES + " ORDER BY " + COLUMN_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Column indexes
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int playDateIndex = cursor.getColumnIndex(COLUMN_PLAY_DATE);
        int playTimeIndex = cursor.getColumnIndex(COLUMN_PLAY_TIME);
        int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
        int correctCountIndex = cursor.getColumnIndex(COLUMN_CORRECT_COUNT);
        int syncedIndex = cursor.getColumnIndex(COLUMN_SYNCED);
        int nameIndex = cursor.getColumnIndex(COLUMN_NAME);

        // Iterating through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GameResult gameResult = new GameResult();
                gameResult.setId(idIndex != -1 ? cursor.getInt(idIndex) : -1);
                gameResult.setPlayDate(playDateIndex != -1 ? cursor.getString(playDateIndex) : "");
                gameResult.setPlayTime(playTimeIndex != -1 ? cursor.getString(playTimeIndex) : "");
                gameResult.setDuration(durationIndex != -1 ? cursor.getInt(durationIndex) : 0);
                gameResult.setCorrectCount(correctCountIndex != -1 ? cursor.getInt(correctCountIndex) : 0);
                gameResult.setSynced(syncedIndex != -1 && cursor.getInt(syncedIndex) == 1);
                gameResult.setName(nameIndex != -1 ? cursor.getString(nameIndex) : "");
                gameResults.add(gameResult);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return gameResults;
    }

    /**
     * Retrieves a list of game results that have not been synchronized with a remote database.
     * This method queries the local SQLite database for all game results where the synced status is 0 (false),
     * indicating that they have not yet been synchronized. It constructs a list of GameResult objects from the query results,
     * each representing a single game's results including the game's ID, play date, play time, duration, correct answer count,
     * and player name. This list can be used to synchronize local results with a remote database.
     *
     * @return A list of {@link GameResult} objects representing unsynced game results. Returns an empty list if no unsynced results are found.
     */
    public List<GameResult> getUnsyncedGameResults() {
        List<GameResult> gameResults = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GAMES + " WHERE " + COLUMN_SYNCED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int playDateIndex = cursor.getColumnIndex(COLUMN_PLAY_DATE);
        int playTimeIndex = cursor.getColumnIndex(COLUMN_PLAY_TIME);
        int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
        int correctCountIndex = cursor.getColumnIndex(COLUMN_CORRECT_COUNT);
        int nameIndex = cursor.getColumnIndex(COLUMN_NAME);

        if (cursor.moveToFirst()) {
            do {
                GameResult gameResult = new GameResult();
                gameResult.setId(idIndex != -1 ? cursor.getInt(idIndex) : -1);
                gameResult.setPlayDate(playDateIndex != -1 ? cursor.getString(playDateIndex) : "");
                gameResult.setPlayTime(playTimeIndex != -1 ? cursor.getString(playTimeIndex) : "");
                gameResult.setDuration(durationIndex != -1 ? cursor.getInt(durationIndex) : 0);
                gameResult.setCorrectCount(correctCountIndex != -1 ? cursor.getInt(correctCountIndex) : 0);
                gameResult.setName(nameIndex != -1 ? cursor.getString(nameIndex) : "");
                gameResults.add(gameResult);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return gameResults;
    }

    /**
     * Inserts a new game result into the database.
     * This method adds a new entry to the games table with the provided game result details including the player's name,
     * the correct answer count, the game duration, and the play date. It returns the ID of the newly inserted row,
     * which can be used for further operations such as updating or deleting the specific game result.
     *
     * @param playerName   The name of the player.
     * @param correctCount The number of correct answers.
     * @param duration     The duration of the game in milliseconds.
     * @param date         The date when the game was played.
     * @return The row ID of the newly inserted game result, or -1 if an error occurred.
     */
    public long insertResult(String playerName, int correctCount, long duration, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAY_DATE, date);
        values.put(COLUMN_CORRECT_COUNT, correctCount);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_NAME, playerName);
        long id = db.insert(TABLE_GAMES, null, values);
        db.close();
        return id;
    }
}