package com.klgleb.githubclient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.klgleb.github.model.GitHubCommit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Adapter for repositories ListView.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class CommitsAdapter extends BaseAdapter {

    public static final String TAG = "MyTag ComitsAdapter";
    private final Context mContext;
    private final ArrayList<GitHubCommit> mCommits;

    public CommitsAdapter(Context context, ArrayList<GitHubCommit> repos) {
        mContext = context;
        mCommits = repos;
    }

    @Override
    public int getCount() {
        return mCommits.size();
    }

    @Override
    public GitHubCommit getItem(int i) {
        return mCommits.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {


        View view = convertView;


        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            view = inflater.inflate(R.layout.commits_list_item, viewGroup, false);
        }


        GitHubCommit commit = getItem(position);


        TextView userNameText = ((TextView) view.findViewById(R.id.userNameText));
        TextView commitMessageTxt = ((TextView) view.findViewById(R.id.commitMessageTxt));
        TextView hashTxt = ((TextView) view.findViewById(R.id.hashTxt));

        TextView dateTxt = ((TextView) view.findViewById(R.id.dateTxt));

        String oldstring = commit.getDate();
        try {
            //2015-04-15T12:11:57Z
            if (oldstring.length() == 20) {
                oldstring = oldstring.substring(0, 19) + "+00:00";
            }

            //2014-02-27T15:05:06+01:00
            oldstring = oldstring.substring(0, 22) + oldstring.substring(23);

//           Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse(oldstring);
            //2014-02-27T15:05:06+01:00


            Log.d(TAG, oldstring);

            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(oldstring);


            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext.getApplicationContext());

            dateTxt.setText(dateFormat.format(date));


        } catch (ParseException e) {
            e.printStackTrace();

            dateTxt.setText(commit.getDate());
        }


        userNameText.setText(commit.getAuthorName());

        if (commit.getMessage() != null) {

            commitMessageTxt.setText(commit.getMessage());
        } else {

            commitMessageTxt.setText("");
        }
        hashTxt.setText(commit.getSha());

        return view;
    }
}
