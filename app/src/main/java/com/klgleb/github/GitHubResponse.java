package com.klgleb.github;

import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Class for processing the query and result.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class GitHubResponse {

    public static final String TAG = "GirResult MyTag";


    public static final int COMPLETE = 0;
    public static final int UNAUTHORIZED = 1;
    public static final int BAD_REQUEST = 2;
    public static final int CONNECTION_PROBLEM = 3;
    public static final int JSON_PARSE_ERROR = 5;


    private String mErrorMessage;

    private int mStatus = CONNECTION_PROBLEM;

    private Object mJsonResult;

    public GitHubResponse(Request request) {

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Response response = null;

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(15, TimeUnit.SECONDS);

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();

            mStatus = CONNECTION_PROBLEM;
        }
        int code = 0;

        try {
            assert response != null;
            code = response.code();
        } catch (Throwable throwable) {
            mStatus = CONNECTION_PROBLEM;
        }


        Log.d(TAG, "Code: " + String.valueOf(code));

        switch (code) {

            case 404:
            case 403:
            case 400:
                mStatus = BAD_REQUEST;
                break;
            case 401:
                mStatus = UNAUTHORIZED;
                break;
            case 200:
                mStatus = COMPLETE;
                break;
            case 0:
            default:
                mStatus = CONNECTION_PROBLEM;
                break;

        }

        String body = null;

        try {
            assert response != null;
            body = response.body().string();
            Log.d(TAG, body);

        } catch (Throwable throwable) {
            mStatus = CONNECTION_PROBLEM;
            throwable.printStackTrace();

        }


        if (mStatus != CONNECTION_PROBLEM && body != null) {
            try {
                if (body.startsWith("[")) {

                    mJsonResult = new JSONArray(body);

                } else if (body.startsWith("{")) {

                    mJsonResult = new JSONObject(body);

                } else {
                    mStatus = JSON_PARSE_ERROR;
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


            if (mStatus != COMPLETE) {
                try {

                    if (mJsonResult instanceof JSONObject) {
                        mErrorMessage = ((JSONObject) mJsonResult).getString("message");
                    }

                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }

        }


    }

    public Object getJsonResult() {
        return mJsonResult;
    }

    public int getStatus() {
        return mStatus;
    }

    public String getErrorMessage() {
        return mErrorMessage;
    }

    public JSONArray getJSONArr() {
        return (JSONArray) mJsonResult;
    }

    public JSONObject getJSONObj() {
        return (JSONObject) mJsonResult;
    }
}
