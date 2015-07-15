package com.klgleb.githubclient;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.klgleb.github.GitHub;
import com.klgleb.github.GitHubRequest;
import com.klgleb.github.GitHubResponse;
import com.klgleb.github.model.GitHubOwner;
import com.klgleb.github.model.GitHubRepo;
import com.klgleb.github.model.GitHubRepos;
import com.klgleb.github.model.GitHubSQLiteHelper;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MyTag MainActivity";
    public static final String LOGIN_KEY = "login";
    public static final String PASS_KEY = "pass";
    public static final String TWO_FA_KEY = "twofa";
    public static final String PREFERENCES = "com.klgleb.githubclient";
    private static boolean sTaskLoading = false;
    //    private ProgressDialog mProgressDialog;
    //private static ReposAdapter mAdapter;
    private ListView mListView;
    private LocalBroadcastManager mBoardcastManager;
    private BroadcastReceiver mReceiver;
    //private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiverError;
    private BroadcastReceiver mReceiverTaskComplete;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);

        setSWipeRefreching(sTaskLoading);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateList();
            }
        });

        //mSwipeRefreshLayout.setColorSchemeResources(Color.YELLOW, Color.BLUE, Color.GREEN);

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
                new IntentFilter(GitHubRepositoriesAsyncTask.ACTION_ON_GET_REPOSITORIES));


        mReceiverError = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadFromCache();
                Log.d(TAG, "Caching error");

                setSWipeRefreching(false);

                Toast.makeText(MainActivity.this, getString(R.string.caching_error), Toast.LENGTH_LONG).show();
            }
        };

        mBoardcastManager.registerReceiver(mReceiverError,
                new IntentFilter(GitHubRepositoriesAsyncTask.ACTION_ON_CACHING_ERROR));


        mReceiverTaskComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                setSWipeRefreching(false);
            }
        };


        mBoardcastManager.registerReceiver(mReceiverTaskComplete,
                new IntentFilter(GitHubRepositoriesAsyncTask.ACTION_TASK_COMPLETE));


        //IntentFilter mFilter = new IntentFilter(ACTION_BACK_PRESSED);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

               /* if (GitHub.getInstance().getOwner() != null) {

                    GitHubRepo repo = (GitHubRepo) mListView.getAdapter().getItem(position);

                    Intent intent = new Intent(MainActivity.this, CommitsActivity.class);

                    intent.putExtra("repo", repo.getName());
                    intent.putExtra("owner", GitHub.getInstance().getOwner().getLogin());

                    startActivity(intent);
                }*/

                GitHubRepo repo = (GitHubRepo) mListView.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, CommitsActivity.class);

                intent.putExtra("repo", repo.getName());
                intent.putExtra("owner", repo.getOwner().getLogin());

                startActivity(intent);
            }
        });


        if (!GitHub.getInstance().isLogin()) {

            SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

            String userLogin = prefs.getString(LOGIN_KEY, "");
            String userPass = prefs.getString(PASS_KEY, "");
            String twoKey = prefs.getString(TWO_FA_KEY, "");

            assert userLogin != null;
            assert userPass != null;
            assert twoKey != null;

            if (!userLogin.equals("") && !userPass.equals("")) {
                GitHub.getInstance().init(userLogin, userPass);
                GitHub.getInstance().setTwoFAKey(twoKey);


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

        if (mSwipeRefreshLayout != null) {
            setSWipeRefreching(true);
        }


        GitHubRepositoriesAsyncTask mTask = new GitHubRepositoriesAsyncTask(this);
        mTask.execute();
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

    public void onClick(View view) {
        updateList();

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
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void logout() {
        AsyncTask<Context, Void, Void> task = new AsyncTask<Context, Void, Void>() {


            @Override
            protected Void doInBackground(Context... contexts) {

                GitHubSQLiteHelper helper = new GitHubSQLiteHelper(contexts[0]);
                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_OWNERS);
                db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_REPOSITORIES);
                db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_COMMITS);
                db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_CURRENT_OWNER);

                return null;
            }

        };
        task.execute(this);


        SharedPreferences prefs = this.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        prefs.edit().putString(LOGIN_KEY, "")
                .putString(TWO_FA_KEY, "")
                .putString(PASS_KEY, "")
                .apply();

        GitHub.getInstance().logout();
        mListView.setAdapter(new ReposAdapter(new GitHubRepos()));
        showLoginDialog();
    }

    private void twoFactorAuth() {

        //TODO:реализовать двухфакторную авторизацию
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Двухфакторная авторизация не доделана");
       /* builder.setTitle(getString(R.string.two_fa_title));

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
         builder.setView(input);

        builder.setPositiveButton(getString(R.string.ok_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = input.getText().toString();

                SharedPreferences prefs = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

                prefs.edit().putString(TWO_FA_KEY, text).apply();


                GitHub.getInstance().setTwoFAKey(text);
                updateList();
//                TWO_FA_KEY
            }
        });*/

        builder.setNegativeButton(getString(R.string.cancel_msg), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private class GitHubRepositoriesAsyncTask extends AsyncTask<Void, Long, GitHubResponse> {

        public static final String ACTION_ON_GET_REPOSITORIES = "com.klgleb.githubclient.ongetrep";
        // public static final String ACTION_ON_PROGRESS = "com.klgleb.githubclient.onprogressrep";
        private static final String ACTION_ON_CACHING_ERROR = "com.klgleb.githubclient.oncachingerror";
        private static final String ACTION_TASK_COMPLETE = "com.klgleb.githubclient.taskcomplete";


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
            sTaskLoading = true;
            //Надо получить текущего пользователя, если его у нас еще нет.
/*
            if (GitHub.getInstance().getOwner() == null) {

                Log.d(TAG, "Start get current user");

                GitHubRequest request2 = new GitHubRequest("user", null);
                GitHubResponse resp = request2.execute();

                if (resp.getStatus() == GitHubResponse.COMPLETE) {
                    try {
                        GitHubOwner owner = new GitHubOwner(resp.getJSONObj());
                        GitHub.getInstance().setOwner(owner);


                    } catch (JSONException e) {
                        return resp;//Тоже пробросим. Там внизу json тоже не обработается и появится ошибка, что нам и надо.
                    }

                    Log.d(TAG, "Start cache current user");

                    //Здесь же закешируем.
                    try {
                        GitHubSQLiteHelper helper = new GitHubSQLiteHelper(mContext);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        db.execSQL("DELETE FROM " + GitHubSQLiteHelper.TABLE_CURRENT_OWNER);
                        ContentValues values = new ContentValues();
                        values.put(GitHubSQLiteHelper.COLUMN_AVATAR_URL, GitHub.getInstance().getOwner().getAvatarUrl());
                        values.put(GitHubSQLiteHelper.COLUMN_LOGIN, GitHub.getInstance().getOwner().getLogin());
                        db.insert(GitHubSQLiteHelper.TABLE_CURRENT_OWNER, null, values);
                        db.close();
                    } catch (Throwable throwable) {
                        //Ошибка? Да и фиг с ней.
                        //do nothing
                    }


                } else {
                    //Пробрасываем ошибку дальше.
                    return resp;
                }
            }*/



            HashMap<String, String> params = new HashMap<>();
            params.put("type", "all");
            params.put("sort", "created");
            params.put("direction", "desc");

            GitHubRequest request = new GitHubRequest("user/repos", params, null);

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
            sTaskLoading = false;

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
                case GitHubResponse.TWO_FACTOR_AUTH:
                    twoFactorAuth();
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


            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(mContext);

            Intent intent = new Intent(ACTION_TASK_COMPLETE);
            manager.sendBroadcast(intent);


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

                //Сначала загрузим из кэша текущего пользователя
                Log.d(TAG, "Start get user from cache");

                GitHubSQLiteHelper helper = new GitHubSQLiteHelper(mContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                Cursor cursor = db.rawQuery("SELECT * FROM " + GitHubSQLiteHelper.TABLE_CURRENT_OWNER, null);

                if (cursor.moveToFirst()) {
                    GitHubOwner owner = new GitHubOwner(cursor);
                    GitHub.getInstance().setOwner(owner);
                    Log.d(TAG, String.format("User got from cache: %s", owner.getLogin()));
                }


                //Затем загружаем репозитории
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
