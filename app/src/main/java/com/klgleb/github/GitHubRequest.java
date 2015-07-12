package com.klgleb.github;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import org.apache.commons.codec.binary.Base64;

import java.util.HashMap;

/**
 * Simple request for GitHub API (using  OkHttp library)
 * <p/>
 * Created by klgleb on 10.07.15.
 */
public class GitHubRequest {


    public static final String URL = "https://api.github.com/";
    private final Request mRequest;


    public GitHubRequest(String method, HashMap<String, String> parametres) {

        FormEncodingBuilder formBuilder = new FormEncodingBuilder();

        for (String key : parametres.keySet()) {
            formBuilder.add(key, parametres.get(key));
        }

        RequestBody formBody = formBuilder.build();

        String name = GitHub.getInstance().getUserLogin();
        String password = GitHub.getInstance().getUserPassword();

        String authString = name + ":" + password;
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);


        mRequest = new Request.Builder()
                .url(GitHubRequest.URL + method)
                .header("User-Agent", "OkHttp Headers.java")
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .addHeader("Authorization", "Basic " + authStringEnc)
                        //.post(formBody)
                .build();


    }

    public GitHubResponse execute() {

        return new GitHubResponse(mRequest);

    }
}
