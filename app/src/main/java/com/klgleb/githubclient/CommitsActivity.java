package com.klgleb.githubclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.klgleb.github.GitHub;
import com.klgleb.github.GitHubRequest;
import com.klgleb.github.GitHubResponse;
import com.klgleb.github.model.GitHubCommits;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;


public class CommitsActivity extends Activity {

    public static final String TAG = "MyTag CommitsActivity";
    public static final String LOGIN_KEY = "login";
    public static final String PASS_KEY = "pass";
    public static final String PREFERENCES = "com.klgleb.githubclient";
    private static boolean sTaskLoading = false;
    private static boolean mFlag = true;
    //    private ProgressDialog mProgressDialog;
    //private static ReposAdapter mAdapter;
    private ListView mListView;
    private LocalBroadcastManager mBoardcastManager;
    private BroadcastReceiver mReceiver;
    //private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiverError;
    private String mRepoName;
    private String mRepoOwner;
    private BroadcastReceiver mReceiverTaskComplete;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commits);


        Intent intent = getIntent();

        this.mRepoName = intent.getStringExtra("repo");
        this.mRepoOwner = intent.getStringExtra("owner");

        mListView = (ListView) findViewById(R.id.listView);


        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);

        setSWipeRefreching(sTaskLoading);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
            }
        });



        mBoardcastManager = LocalBroadcastManager.getInstance(this);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadFromCache();
                Log.d(TAG, "onReceive and loading from SQLite.");

                setSWipeRefreching(false);
            }
        };

        mBoardcastManager.registerReceiver(mReceiver,
                new IntentFilter(GitHubCommitsAsyncTask.ACTION_ON_GET_COMMITS));


        mReceiverError = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadFromCache();
                Log.d(TAG, "Caching error");
                setSWipeRefreching(false);

                Toast.makeText(CommitsActivity.this, getString(R.string.caching_error), Toast.LENGTH_LONG).show();
            }
        };
        mBoardcastManager.registerReceiver(mReceiverError,
                new IntentFilter(GitHubCommitsAsyncTask.ACTION_ON_CACHING_ERROR_COMMITS));

        mReceiverTaskComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setSWipeRefreching(false);
            }
        };


        mBoardcastManager.registerReceiver(mReceiverTaskComplete,
                new IntentFilter(GitHubCommitsAsyncTask.ACTION_TASK_COMPLETE));

        loadFromCache();

        mFlag = true;
        Log.d(TAG, "onCreate");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBoardcastManager.unregisterReceiver(mReceiver);
        mBoardcastManager.unregisterReceiver(mReceiverError);
        mBoardcastManager.unregisterReceiver(mReceiverTaskComplete);
    }

    private void loadFromCache() {

        assert mListView != null;


        LoadFromCacheAsyncTask task = new LoadFromCacheAsyncTask(this);
        if (Build.VERSION.SDK_INT >= 11) {
            //--post GB use serial executor by default --
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            //--GB uses ThreadPoolExecutor by default--
            task.execute();
        }
    }

    private void showLoginDialog() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void updateList() {

        //mListView.setAdapter(new ReposAdapter(new GitHubRepos()));
        if (mRepoOwner == null || mRepoName == null) {
            Log.w(TAG, "Owner and repo are null");
            super.onBackPressed();
            return;
        }


        setSWipeRefreching(true);

        GitHubCommitsAsyncTask mTask = new GitHubCommitsAsyncTask(this, mRepoOwner, mRepoName);
        mTask.execute();
    }


    public void onClick(View view) {
        updateList();

    }

    private void setSWipeRefreching(final boolean refreching) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(refreching);
            }
        }, 100);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            showLoginDialog();
            return;
        }


        String userLogin = data.getStringExtra(LOGIN_KEY);
        String userPass = data.getStringExtra(PASS_KEY);

        if (!userLogin.equals("") && !userPass.equals("")) {

            SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

            prefs.edit().putString(LOGIN_KEY, userLogin)
                    .putString(PASS_KEY, userPass).apply();


            GitHub.getInstance().init(userLogin, userPass);
            updateList();

        } else {
            showLoginDialog();
        }

    }

    private class GitHubCommitsAsyncTask extends AsyncTask<Void, Long, GitHubResponse> {

        public static final String ACTION_ON_GET_COMMITS = "com.klgleb.githubclient.ongecommits";
        // public static final String ACTION_ON_PROGRESS = "com.klgleb.githubclient.onprogressrep";
        private static final String ACTION_ON_CACHING_ERROR_COMMITS = "com.klgleb.githubclient.oncachingerrorcommits";
        private static final String ACTION_TASK_COMPLETE = "com.klgleb.githubclient.ontaskcompletecommits";

        private final String mPath;
        private final String mOwner;
        private final String mRepo;


        private Context mContext;

        public GitHubCommitsAsyncTask(Context context, String owner, String repo) {
            mPath = String.format("repos/%s/%s/commits", owner, repo);
            mContext = context;

            mOwner = owner;
            mRepo = repo;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected GitHubResponse doInBackground(Void... voids) {
            sTaskLoading = true;

            HashMap<String, String> params = new HashMap<>();

            GitHubRequest request = new GitHubRequest(mPath, params, null);

            final GitHubResponse.ProgressListener progressListener = new GitHubResponse.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    /*System.out.println(bytesRead);
                    System.out.println(contentLength);
                    System.out.println(done);
                    System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);*/

                    GitHubCommitsAsyncTask.this.publishProgress(bytesRead, contentLength);
                }
            };

            return request.execute(progressListener);

        }

        @Override
        protected void onProgressUpdate(Long... values) {
//            Long contentLength = values[0];
//            Long bytesRead = values[1];

            // Log.d(TAG, String.format("This is progress! 3-)  Bytes read %s, bytes total %s",bytesRead, contentLength));

            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(GitHubResponse response) {
            super.onPostExecute(response);
            sTaskLoading = false;

            switch (response.getStatus()) {
                case GitHubResponse.BAD_REQUEST:
                    Toast.makeText(mContext, getString(R.string.commit_dont_have), Toast.LENGTH_LONG).show();

                    if (mListView != null) {
                        CommitsActivity.super.onBackPressed();
                    }


                    break;
                case GitHubResponse.CONNECTION_PROBLEM:
                    Toast.makeText(mContext, getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                    break;
                case GitHubResponse.JSON_PARSE_ERROR:
                    Toast.makeText(mContext, getString(R.string.json_error), Toast.LENGTH_LONG).show();
                    break;
                case GitHubResponse.UNAUTHORIZED:
                case GitHubResponse.TWO_FACTOR_AUTH: //TODO:двухфакторная авторизация не реализована
                    Toast.makeText(mContext, getString(R.string.auth_error), Toast.LENGTH_LONG).show();
                    showLoginDialog();

                    break;
                case GitHubResponse.COMPLETE:

                    JSONArray jsonArr = response.getJSONArr();

                    try {
                        final GitHubCommits commit = new GitHubCommits(jsonArr, mOwner, mRepoName);
                        //mListView.setAdapter(new ReposAdapter(repos));

                        //caching data
                        AsyncTask<GitHubCommits, Void, Void> task = new AsyncTask<GitHubCommits, Void, Void>() {

                            @Override
                            protected Void doInBackground(GitHubCommits... gitHubReposes) {

                                GitHubCommits commits = gitHubReposes[0];

                                try {

                                    commits.cache(CommitsActivity.this);
                                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);

                                    Intent intent = new Intent(ACTION_ON_GET_COMMITS);
                                    manager.sendBroadcast(intent);

                                    Log.d(TAG, "Commits cached -- count = " + String.valueOf(commit.size()));
                                } catch (Throwable throwable) {
                                    //throwable.printStackTrace();
                                    Log.w(TAG, "Problem during caching commits: ");
                                    Log.w(TAG, throwable);

                                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);

                                    Intent intent = new Intent(ACTION_ON_CACHING_ERROR_COMMITS);
                                    manager.sendBroadcast(intent);

                                }

                                return null;
                            }
                        };

                        task.execute(commit);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(mContext, getString(R.string.json_error), Toast.LENGTH_LONG).show();
                    }

                    break;
            }


            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);

            Intent intent = new Intent(ACTION_TASK_COMPLETE);
            manager.sendBroadcast(intent);

            Log.d(TAG, "GitHubCommitsAsyncTask finished");
        }
    }


    private class LoadFromCacheAsyncTask extends AsyncTask<Void, Void, GitHubCommits> {
        private final Context mContext;

        public LoadFromCacheAsyncTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected GitHubCommits doInBackground(Void... params) {

            try {

                return new GitHubCommits(this.mContext, mRepoOwner, mRepoName);

            } catch (Throwable throwable) {
                Log.w(TAG, throwable);
                return new GitHubCommits(mRepoOwner, mRepoName);
            }


        }

        @Override
        protected void onPostExecute(GitHubCommits result) {
            if (result != null && result.size() > 0 && mListView != null) {

                Log.d(TAG, "Commits are got from SQLite: count of this is " + result.size());

                mListView.setAdapter(new CommitsAdapter(result));

            } else {
                if (mFlag) {
                    updateList();
                    mFlag = false; // для предотвращения зацикливания, когда 0 коммитов
                }

            }
        }


    }
}
