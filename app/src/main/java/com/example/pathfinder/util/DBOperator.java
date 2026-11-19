package com.example.pathfinder.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.pathfinder.constant.DBConstant;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

// One shared connection to Pathfinder.db (singleton, as in Tutorial 3)
public class DBOperator {
    private static DBOperator instance;
    private final SQLiteDatabase db;

    private DBOperator(Context context) {
        File file = context.getDatabasePath(DBConstant.DATABASE_FILE);
        if (!file.exists() || versionOf(file) < DBConstant.DATABASE_VERSION) copyDB(context, file);
        db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
    }

    public static DBOperator getInstance(Context context) {
        if (instance == null) instance = new DBOperator(context.getApplicationContext());
        return instance;
    }

    public Cursor execQuery(String sql, String... args) {
        return db.rawQuery(sql, args);
    }

    public void execSQL(String sql, Object... args) {
        db.execSQL(sql, args);
    }

    private static int versionOf(File file) {
        try (SQLiteDatabase old = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY)) {
            return old.getVersion();
        }
    }

    // Copies Pathfinder.db from assets onto the phone and stamps it with DATABASE_VERSION
    private static void copyDB(Context context, File file) {
        context.deleteDatabase(DBConstant.DATABASE_FILE);
        try (InputStream in = context.getAssets().open(DBConstant.DATABASE_FILE)) {
            Files.createDirectories(file.getParentFile().toPath());
            Files.copy(in, file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not copy " + DBConstant.DATABASE_FILE, e);
        }
        try (SQLiteDatabase copy = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READWRITE)) {
            copy.setVersion(DBConstant.DATABASE_VERSION);
        }
    }
}
