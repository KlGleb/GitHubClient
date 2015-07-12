package com.klgleb.github.model;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple model for github users.
 * This  is simple model. It doesn't contain some properties.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubOwner {

    private final String mLogin;
    private final String mAvatarUrl;
    private final int mOwnerId;

    public GitHubOwner(JSONObject object) throws JSONException {
        mAvatarUrl = object.getString("avatar_url");
        mLogin = object.getString("login");
        mOwnerId = object.getInt("id");
    }

    public GitHubOwner(Cursor cursor) throws Throwable {

        mOwnerId = cursor.getInt(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_ID));
        mAvatarUrl = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_AVATAR_URL));
        mLogin = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_LOGIN));


    }

    public String getLogin() {
        return mLogin;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public int getOwnerId() {
        return mOwnerId;
    }

    public ContentValues getContentValues() {

        ContentValues values = new ContentValues();

        values.put(GitHubSQLiteHelper.COLUMN_ID, getOwnerId());
        values.put(GitHubSQLiteHelper.COLUMN_AVATAR_URL, getAvatarUrl());
        values.put(GitHubSQLiteHelper.COLUMN_LOGIN, getLogin());

        return values;
    }
}
