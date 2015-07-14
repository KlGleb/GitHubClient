package com.klgleb.github.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model that store repositories from GitHub (https://developer.github.com/v3/repos/)
 * This  is simple model. It doesn't contain some properties.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubRepo {

    private final GitHubOwner mOwner;
    private final String mName;
    private final String mDescription;
    private final int mForksCount;
    private final int mWatchersCount;
    private final int mRepId;
    private final String mFullName;

    public GitHubRepo(JSONObject object) throws JSONException {

        mOwner = new GitHubOwner((JSONObject) object.get("owner"));
        mName = object.getString("name");
        mFullName = object.getString("full_name");
        mDescription = object.getString("description");
        mForksCount = object.getInt("forks_count");
        mWatchersCount = object.getInt("watchers_count");
        mRepId = object.getInt("id");

    }

    public GitHubRepo(Cursor cursor, GitHubOwner owner) {
        mOwner = owner;

        mForksCount = cursor.getInt(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_FORKS_COUNT));
        mWatchersCount = cursor.getInt(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_WATCHERS_COUNT));
        mRepId = cursor.getInt(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_WATCHERS_COUNT));
        mName = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_NAME));
        mFullName = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_FULL_NAME));
        mDescription = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_DESCRIPTION));
    }

    public GitHubOwner getOwner() {
        return mOwner;
    }

    public String getName() {
        return mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public int getForksCount() {
        return mForksCount;
    }

    public int getWatchersCount() {
        return mWatchersCount;
    }

    public int getRepId() {
        return mRepId;
    }

    public String getFullName() {
        return mFullName;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(GitHubSQLiteHelper.COLUMN_ID, getRepId());
        values.put(GitHubSQLiteHelper.COLUMN_NAME, getName());
        values.put(GitHubSQLiteHelper.COLUMN_DESCRIPTION, getDescription());
        values.put(GitHubSQLiteHelper.COLUMN_FORKS_COUNT, getForksCount());
        values.put(GitHubSQLiteHelper.COLUMN_WATCHERS_COUNT, getWatchersCount());
        values.put(GitHubSQLiteHelper.COLUMN_OWNER_ID, getOwner().getOwnerId());
        values.put(GitHubSQLiteHelper.COLUMN_FULL_NAME, getFullName());

        return values;
    }
}
