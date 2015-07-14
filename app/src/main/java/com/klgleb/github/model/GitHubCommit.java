package com.klgleb.github.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Model that store repositories from GitHub (https://developer.github.com/v3/repos/)
 * This  is simple model. It doesn't contain some properties.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubCommit {


    public static final String TAG = "GitHubCommit MyTag";
    private final String mRepOwner;
    private final String mRepoName;
    private final String mSha;
    private final String mAuthorName;
    private final String mAuthorEmail;
    private final String mDate;
    private final String mMessage;

    public GitHubCommit(String mRepOwner, String mRepoName, String mSha, String mAuthorName,
                        String mAuthorEmail, String mDate, String mMessage) {
        this.mRepOwner = mRepOwner;
        this.mRepoName = mRepoName;

        this.mSha = mSha;
        this.mAuthorName = mAuthorName;
        this.mAuthorEmail = mAuthorEmail;
        this.mDate = mDate;
        this.mMessage = mMessage;
    }

    public GitHubCommit(String repoName, String repoOwner, JSONObject object) throws JSONException {


        this.mRepOwner = repoOwner;
        this.mRepoName = repoName;

        Log.d(TAG, String.format("Create GitHubCommit from JSONObject: %s/%s", mRepOwner, mRepoName));

        this.mSha = object.getString("sha");

        JSONObject commit = object.getJSONObject("commit");
        JSONObject author = commit.getJSONObject("author");

        this.mAuthorName = author.getString("name");
        this.mAuthorEmail = author.getString("email");
        this.mDate = author.getString("date");

        this.mMessage = commit.getString("message");
    }

    public GitHubCommit(Cursor cursor) {

        this.mRepOwner = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_REPO_OWNER));
        this.mRepoName = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_REPO));


        this.mSha = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_SHA));
        this.mAuthorName = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_AUTHOR_NAME));
        this.mAuthorEmail = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_AUTHOR_EMAIL));
        this.mDate = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLUMN_DATE));
        this.mMessage = cursor.getString(cursor.getColumnIndexOrThrow(GitHubSQLiteHelper.COLLUMN_MESSAGE));


        Log.d(TAG, String.format("Create GitHubCommit from Cursor: %s/%s", mRepOwner, mRepoName));
    }


    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();


        values.put(GitHubSQLiteHelper.COLUMN_REPO, getRepoName());
        values.put(GitHubSQLiteHelper.COLUMN_REPO_OWNER, getRepOwner());
        values.put(GitHubSQLiteHelper.COLUMN_SHA, getSha());
        values.put(GitHubSQLiteHelper.COLUMN_AUTHOR_NAME, getAuthorName());
        values.put(GitHubSQLiteHelper.COLUMN_AUTHOR_EMAIL, getAuthorEmail());
        values.put(GitHubSQLiteHelper.COLUMN_DATE, getDate());
        values.put(GitHubSQLiteHelper.COLLUMN_MESSAGE, getMessage());

        return values;
    }


    public String getRepOwner() {
        return mRepOwner;
    }

    public String getRepoName() {
        return mRepoName;
    }

    public String getSha() {
        return mSha;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public String getAuthorEmail() {
        return mAuthorEmail;
    }

    public String getDate() {
        return mDate;
    }

    public String getMessage() {
        return mMessage;
    }


    @Override
    public String toString() {

        String s = "This is commit. %s/%s, author %s (%s)\n " +
                "sha %s\n" +
                "date %s\n" +
                "message: %s";
        return String.format(s, getRepOwner(), getRepoName(), getAuthorName(),
                getAuthorEmail(), getSha(), getDate(), getMessage());
    }
}
