package com.klgleb.github.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.klgleb.github.GitHub;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * ArrayList of repisitories, that also create itself from JSONArray
 * and load itself from SQLite.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubRepos extends ArrayList<GitHubRepo> {

    public static final String TAG = "GitHubRepos MyTag";

    public GitHubRepos() {
        super();
    }

    public GitHubRepos(JSONArray repositories) throws JSONException {
        super();


        for (int i = 0; i < repositories.length(); i++) {


            GitHubRepo repo = new GitHubRepo(repositories.getJSONObject(i));

            this.add(repo);
        }
    }


    /**
     * Creating array from cache.
     *
     * @param context a context that need to working  SQLite.
     */
    public GitHubRepos(Context context) throws Throwable {

        Log.d(TAG, "Start loading from cache");

        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(context);

        SQLiteDatabase db = helper.getWritableDatabase();

        @SuppressLint("Recycle") Cursor c = db.query(GitHubSQLiteHelper.TABLE_REPOSITORIES, null, null, null, null, null, null);

        if (c.moveToFirst()) {
            do {


                String whereClause = String.format("%s = ?", GitHubSQLiteHelper.COLUMN_ID);

                String[] whereArgs = new String[]{
                        c.getString(c.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_OWNER_ID))
                };


                @SuppressLint("Recycle") Cursor c2 = db.query(GitHubSQLiteHelper.TABLE_OWNERS, null, whereClause, whereArgs,
                        null, null, null);

                c2.moveToFirst();


                GitHubOwner owner = new GitHubOwner(c2);
                GitHubRepo repo = new GitHubRepo(c, owner);

                this.add(repo);


            } while (c.moveToNext());

            Log.d(TAG, "The end of loading from cache. Count =  " + this.size());
        }

    }

    public void cache(Context context) throws Throwable {

        Log.d(TAG, "==============================================================");
        Log.d(TAG, "before delete: ");
        viewCache(context);

        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(context);

        SQLiteDatabase db = helper.getWritableDatabase();


        db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_OWNERS);
        db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_REPOSITORIES);


        Log.d(TAG, "After  delete: ");
        viewCache(context);





        for (int i = 0; i < this.size(); i++) {
            GitHubRepo rep = this.get(i);

            try {
                db.insertOrThrow(GitHubSQLiteHelper.TABLE_REPOSITORIES, null, rep.getContentValues());
            } catch (SQLiteConstraintException exception) {
                exception.printStackTrace();
            }

            try {
                db.insertOrThrow(GitHubSQLiteHelper.TABLE_OWNERS, null, rep.getOwner().getContentValues());
            } catch (SQLiteConstraintException exception) {
                exception.printStackTrace();
                //do nonhing
            }
        }

        Log.d(TAG, "After  insert: ");
        viewCache(context);


        Log.d(TAG, "==============================================================");
        db.close();

    }

    private void viewCache(Context context) {
        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(context);

        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor c = db.rawQuery(
                MessageFormat.format("SELECT * FROM {0} ",
                        GitHubSQLiteHelper.TABLE_REPOSITORIES
                ), null
        );

        if (c.moveToFirst()) {
            do {

                GitHubRepo repo = new GitHubRepo(c, GitHub.getInstance().getOwner());

                Log.d(TAG, repo.getName() + " ( " + repo.getDescription() + " )");


            } while (c.moveToNext());

        }

        db.close();
    }

}
