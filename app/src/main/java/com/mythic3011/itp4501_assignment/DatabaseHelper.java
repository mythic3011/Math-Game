package com.mythic3011.itp4501_assignment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MathGameDB";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_GAMES = "games";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PLAY_DATE = "play_date";
    private static final String COLUMN_PLAY_TIME = "play_time";
    private static final String COLUMN_DURATION = "duration";
    private static final String COLUMN_CORRECT_COUNT = "correct_count";
    private static final String COLUMN_SYNCED = "synced";
    private static final String COLUMN_NAME = "name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_GAMES + " ADD COLUMN " + COLUMN_NAME + " TEXT");
        }
    }

    public long addGameResult(String playDate, String playTime, int duration, int correctCount, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PLAY_DATE, playDate);
        values.put(COLUMN_PLAY_TIME, playTime);
        values.put(COLUMN_DURATION, duration);
        values.put(COLUMN_CORRECT_COUNT, correctCount);
        values.put(COLUMN_NAME, name);
        long id = db.insert(TABLE_GAMES, null, values);
        db.close();
        return id;
    }

    public List<GameResult> getAllGameResults() {
        List<GameResult> gameResults = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GAMES + " ORDER BY " + COLUMN_ID + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int playDateIndex = cursor.getColumnIndex(COLUMN_PLAY_DATE);
        int playTimeIndex = cursor.getColumnIndex(COLUMN_PLAY_TIME);
        int durationIndex = cursor.getColumnIndex(COLUMN_DURATION);
        int correctCountIndex = cursor.getColumnIndex(COLUMN_CORRECT_COUNT);
        int syncedIndex = cursor.getColumnIndex(COLUMN_SYNCED);
        int nameIndex = cursor.getColumnIndex(COLUMN_NAME);

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

    public void markAsSynced(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SYNCED, 1);
        db.update(TABLE_GAMES, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllGameResults() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GAMES, null, null);
        db.close();
    }
}