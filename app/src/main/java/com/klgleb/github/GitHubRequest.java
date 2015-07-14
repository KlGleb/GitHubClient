package com.klgleb.github;

import android.net.Uri;
import android.util.Log;

import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.Request;

import java.util.HashMap;

/**
 * Simple request for GitHub API (using  OkHttp library)
 * <p/>
 * Created by klgleb on 10.07.15.
 */
public class GitHubRequest {

    public static final String TAG = "MyTag GitHubRequest";
    public static final String API_DOMAIN = "api.github.com";
    private final Request mRequest;


    public GitHubRequest(String method, HashMap<String, String> parametres) {


        String name = GitHub.getInstance().getUserLogin();
        String password = GitHub.getInstance().getUserPassword();

        String credential = Credentials.basic(name, password);


        Uri.Builder builder = new Uri.Builder()
                .scheme("https")
                .authority(API_DOMAIN)
                .path(method);

        if (parametres != null) {
            for (String key : parametres.keySet()) {
                builder.appendQueryParameter(key, parametres.get(key));
            }
        }

        Uri uri = builder.build();

        Log.d(TAG, uri.toString());

        mRequest = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noCache().build())
                .url(uri.toString())
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", credential)
                        //.post(formBody)
                .build();


    }

    public GitHubResponse execute(GitHubResponse.ProgressListener progressListener) {

        return new GitHubResponse(mRequest, progressListener);

    }

    public GitHubResponse execute() {
        return execute(null);
    }
}
