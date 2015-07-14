package com.klgleb.github.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * ArrayList of commits, that also create itself from JSONArray
 * and load itself from SQLite.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubCommits extends ArrayList<GitHubCommit> {

    public static final String TAG = "GitHubCommits MyTag";

    private final String mOwner;
    private final String mRepo;

    public GitHubCommits(String author, String name) {
        super();
        this.mOwner = author.toLowerCase();
        this.mRepo = name.toLowerCase();
    }

    public GitHubCommits(JSONArray commits, String author, String name) throws JSONException {
        super();
        this.mOwner = author.toLowerCase();
        this.mRepo = name.toLowerCase();

        for (int i = 0; i < commits.length(); i++) {


            GitHubCommit commit = new GitHubCommit(mRepo, mOwner, commits.getJSONObject(i));

            this.add(commit);
        }
    }


    /**
     * Creating array from cache.
     *
     * @param context a context that need to working  SQLite.
     */
    public GitHubCommits(Context context, String author, String name) throws Throwable {

        this.mOwner = author.toLowerCase();
        this.mRepo = name.toLowerCase();

        Log.d(TAG, "Start loading commits from cache");

        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(context);

        SQLiteDatabase db = helper.getWritableDatabase();


        String whereClause = String.format("(%s LIKE '%s') AND (%s LIKE  '%s')",
                GitHubSQLiteHelper.COLUMN_REPO, mRepo,
                GitHubSQLiteHelper.COLUMN_REPO_OWNER, mOwner);

        String[] whereArgs = new String[]{
                mRepo, mOwner
        };



        /*Cursor c = db.query(GitHubSQLiteHelper.TABLE_COMMITS, null, whereClause, null,
                null, null, null);

        */


        Cursor c = db.rawQuery("SELECT * FROM " + GitHubSQLiteHelper.TABLE_COMMITS + " WHERE " +
                        "(" + GitHubSQLiteHelper.COLUMN_REPO + " LIKE ?) AND ("
                        + GitHubSQLiteHelper.COLUMN_REPO_OWNER + " LIKE  ?)",

                whereArgs);

     /*   Cursor c = db.rawQuery(
                MessageFormat.format("SELECT * FROM {0} WHERE ({1} LIKE ''{2}'') ",
                        GitHubSQLiteHelper.TABLE_COMMITS,
                        GitHubSQLiteHelper.COLUMN_REPO, mRepo
                ), null
        );*/

        //Cursor c = db.rawQuery("SELECT * FROM " + GitHubSQLiteHelper.TABLE_COMMITS, null);

        if (c.moveToFirst()) {
            do {

                GitHubCommit commit = new GitHubCommit(c);
                Log.d(TAG, "For example " + commit.getMessage());

                this.add(commit);


            } while (c.moveToNext());

            Log.d(TAG, "The end of loading from cache. Count =  " + this.size());
        }

        db.close();

    }

    public void cache(Context context) throws Throwable {
        //viewCache(context);

        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(context);

        SQLiteDatabase db = helper.getWritableDatabase();
        //Список коммитов в репозитории в табличном виде: hash, короткий commit message, автор, дата

        db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_COMMITS +
                String.format(" WHERE (%s LIKE '%s') AND (%s LIKE  '%s') ",
                        GitHubSQLiteHelper.COLUMN_REPO_OWNER, mOwner,
                        GitHubSQLiteHelper.COLUMN_REPO, mRepo
                ));

        for (int i = 0; i < this.size(); i++) {
            GitHubCommit commit = this.get(i);

            try {
                db.insertOrThrow(GitHubSQLiteHelper.TABLE_COMMITS, null, commit.getContentValues());
            } catch (SQLiteConstraintException exception) {
                exception.printStackTrace();
            }

        }

        db.close();

    }

    private void viewCache(Context context) {
        /*
        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(context);

        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor c = db.rawQuery(
                MessageFormat.format("SELECT * FROM {0} ",
                        GitHubSQLiteHelper.TABLE_COMMITS
                ), null
        );

        if (c.moveToFirst()) {
            do {

                GitHubCommit commit = new GitHubCommit(c);

                Log.d(TAG, commit.toString());


            } while (c.moveToNext());

        }

        db.close();*/
    }


}
