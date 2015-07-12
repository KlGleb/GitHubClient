package com.klgleb.github.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The database helper for caching data from GitHub
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubSQLiteHelper extends SQLiteOpenHelper {

    public static final String COLUMN_ID = "_id"; // primary key for all
    //MY REPOSITORIES
    public static final String TABLE_REPOSITORIES = "repositories";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_FORKS_COUNT = "forks_count";
    public static final String COLUMN_WATCHERS_COUNT = "watchers_count";
    public static final String COLUMN_OWNER_ID = "owner";
    //OWNERS
    public static final String TABLE_OWNERS = "owners";
    public static final String COLUMN_AVATAR_URL = "avatar_url";
    public static final String COLUMN_LOGIN = "login";
    private static final String DATABASE_NAME = "github.db";
    private static final int DATABASE_VERSION = 1;


    public GitHubSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s TEXT, %s TEXT,  %s INTEGER, %s INTEGER, %s INTEGER)",

                TABLE_REPOSITORIES, COLUMN_ID, COLUMN_NAME, COLUMN_DESCRIPTION,
                COLUMN_FORKS_COUNT, COLUMN_WATCHERS_COUNT, COLUMN_OWNER_ID));

        database.execSQL(String.format("CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "%s TEXT, %s TEXT)",

                TABLE_OWNERS, COLUMN_ID, COLUMN_AVATAR_URL, COLUMN_LOGIN));

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(GitHubSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        if (oldVersion == 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_OWNERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPOSITORIES);
            onCreate(db);
        }


    }

}