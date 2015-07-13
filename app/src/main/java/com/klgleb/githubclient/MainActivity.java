package com.klgleb.githubclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.klgleb.github.GitHub;
import com.klgleb.github.GitHubRequest;
import com.klgleb.github.GitHubResponse;
import com.klgleb.github.model.GitHubRepos;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MyTag MainActivity";
    public static final String LOGIN_KEY = "login";
    public static final String PASS_KEY = "pass";
    public static final String PREFERENCES = "com.klgleb.githubclient";

    //    private ProgressDialog mProgressDialog;
    //private static ReposAdapter mAdapter;
    private ListView mListView;
    private LocalBroadcastManager mBoardcastManager;
    private BroadcastReceiver mReceiver;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiverError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mProgressBar.setVisibility(View.INVISIBLE);

        mBoardcastManager = LocalBroadcastManager.getInstance(this);


        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadFromCache();
                Log.d(TAG, "onReceive and loading from SQLite.");

                mProgressBar.setVisibility(View.INVISIBLE);
            }
        };

        mBoardcastManager.registerReceiver(mReceiver,
                new IntentFilter(GitHubRepositoriesAsyncTask.ACTION_ON_GET_REPOSITORIES));


        mReceiverError = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadFromCache();
                Log.d(TAG, "Caching error");

                mProgressBar.setVisibility(View.INVISIBLE);

                Toast.makeText(MainActivity.this, getString(R.string.caching_error), Toast.LENGTH_LONG).show();
            }
        };
        mBoardcastManager.registerReceiver(mReceiverError,
                new IntentFilter(GitHubRepositoriesAsyncTask.ACTION_ON_CACHING_ERROR));


        //IntentFilter mFilter = new IntentFilter(ACTION_BACK_PRESSED);


        if (!GitHub.getInstance().isLogin()) {

            SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

            String userLogin = prefs.getString(LOGIN_KEY, "");
            String userPass = prefs.getString(PASS_KEY, "");

            assert userLogin != null;
            assert userPass != null;

            if (!userLogin.equals("") && !userPass.equals("")) {
                GitHub.getInstance().init(userLogin, userPass);

                loadFromCache();

                this.updateList();

            } else {
                this.showLoginDialog();
            }


        } else {

            if (mListView.getAdapter() == null) {
                loadFromCache();
            }


        }

        Log.d(TAG, "onCreate");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBoardcastManager.unregisterReceiver(mReceiver);
        mBoardcastManager.unregisterReceiver(mReceiverError);
    }

    private void loadFromCache() {
        assert mProgressBar != null;


        LoadFromCacheAsyncTask task = new LoadFromCacheAsyncTask(this);
        task.execute();
    }

    private void showLoginDialog() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void updateList() {

        mListView.setAdapter(new ReposAdapter(new GitHubRepos()));

        mProgressBar.setVisibility(View.VISIBLE);
        GitHubRepositoriesAsyncTask mTask = new GitHubRepositoriesAsyncTask(this);
        mTask.execute();
    }


    public void onClick(View view) {
        updateList();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class GitHubRepositoriesAsyncTask extends AsyncTask<Void, Long, GitHubResponse> {

        public static final String ACTION_ON_GET_REPOSITORIES = "com.klgleb.githubclient.ongetrep";
        // public static final String ACTION_ON_PROGRESS = "com.klgleb.githubclient.onprogressrep";
        private static final String ACTION_ON_CACHING_ERROR = "com.klgleb.githubclient.oncachingerror";


        private Context mContext;

        public GitHubRepositoriesAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected GitHubResponse doInBackground(Void... voids) {

            HashMap<String, String> params = new HashMap<>();
            params.put("type", "all");
            params.put("sort", "created");
            params.put("direction", "desc");

            GitHubRequest request = new GitHubRequest("user/repos", params);

            final GitHubResponse.ProgressListener progressListener = new GitHubResponse.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {
                    /*System.out.println(bytesRead);
                    System.out.println(contentLength);
                    System.out.println(done);
                    System.out.format("%d%% done\n", (100 * bytesRead) / contentLength);*/

                    GitHubRepositoriesAsyncTask.this.publishProgress(bytesRead, contentLength);
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

            switch (response.getStatus()) {
                case GitHubResponse.BAD_REQUEST:
                    Toast.makeText(mContext, getString(R.string.bad_request), Toast.LENGTH_LONG).show();
                    break;
                case GitHubResponse.CONNECTION_PROBLEM:
                    Toast.makeText(mContext, getString(R.string.connection_problem), Toast.LENGTH_LONG).show();
                    break;
                case GitHubResponse.JSON_PARSE_ERROR:
                    Toast.makeText(mContext, getString(R.string.json_error), Toast.LENGTH_LONG).show();
                    break;
                case GitHubResponse.UNAUTHORIZED:
                    Toast.makeText(mContext, getString(R.string.auth_error), Toast.LENGTH_LONG).show();
                    showLoginDialog();

                    break;
                case GitHubResponse.COMPLETE:

                    JSONArray jsonArr = response.getJSONArr();

                    try {
                        final GitHubRepos repos = new GitHubRepos(jsonArr);
                        //mListView.setAdapter(new ReposAdapter(repos));

                        //caching data
                        AsyncTask<GitHubRepos, Void, Void> task = new AsyncTask<GitHubRepos, Void, Void>() {

                            @Override
                            protected Void doInBackground(GitHubRepos... gitHubReposes) {
                                GitHubRepos repose = gitHubReposes[0];

                                try {
                                    repose.cache(MainActivity.this);
                                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);

                                    Intent intent = new Intent(ACTION_ON_GET_REPOSITORIES);
                                    manager.sendBroadcast(intent);

                                    Log.d(TAG, "Repositories cached -- count = " + String.valueOf(repos.size()));
                                } catch (Throwable throwable) {
                                    //throwable.printStackTrace();
                                    Log.w(TAG, "Problem during caching: ");
                                    Log.w(TAG, throwable);

                                    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);

                                    Intent intent = new Intent(ACTION_ON_CACHING_ERROR);
                                    manager.sendBroadcast(intent);

                                }

                                return null;
                            }
                        };

                        task.execute(repos);

                    } catch (JSONException e) {
                        e.printStackTrace();

                        Toast.makeText(mContext, getString(R.string.json_error), Toast.LENGTH_LONG).show();
                    }

                    break;
            }

            Log.d(TAG, "GitHubRepositoriesAsyncTask finished");
        }
    }


    private class LoadFromCacheAsyncTask extends AsyncTask<Void, Void, GitHubRepos> {
        private final Context mContext;

        public LoadFromCacheAsyncTask(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        protected GitHubRepos doInBackground(Void... params) {

            try {

                return new GitHubRepos(this.mContext);

            } catch (Throwable throwable) {
                Log.w(TAG, throwable);
                return new GitHubRepos();
            }


        }

        @Override
        protected void onPostExecute(GitHubRepos result) {
            if (result != null && result.size() > 0 && mListView != null) {

                Log.d(TAG, "Repositories got from SQLite: count of this is " + result.size());

                mListView.setAdapter(new ReposAdapter(result));

            }
        }


    }
}
