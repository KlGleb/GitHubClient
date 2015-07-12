package com.klgleb.github;

/**
 * Singleton that stores the authorization data.
 * <p/>
 * Created by klgleb on 10.07.15.
 */
public class GitHub {
    private static GitHub ourInstance = new GitHub();

    private String mUserLogin;
    private String mUserPassword;
    private boolean mIsInit = false;

    private GitHub() {
    }

    public static GitHub getInstance() {
        return ourInstance;
    }

    public void init(String login, String pass) {
        mUserLogin = login;
        mUserPassword = pass;

        mIsInit = true;
    }

    public String getUserLogin() {
        return mUserLogin;
    }

    public String getUserPassword() {
        return mUserPassword;
    }

    public boolean isLogin() {
        return mIsInit;
    }

}
