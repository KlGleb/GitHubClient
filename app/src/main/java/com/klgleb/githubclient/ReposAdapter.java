package com.klgleb.githubclient;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.klgleb.github.model.GitHubRepo;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Adapter for repositories ListView.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class ReposAdapter extends RecyclerView.Adapter<ReposAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<GitHubRepo> mRepos;

    public ReposAdapter(Context context, ArrayList<GitHubRepo> repos) {
        mContext = context;
        mRepos = repos;
    }


    @Override
    public ReposAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.repositories_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReposAdapter.ViewHolder holder, int position) {
        GitHubRepo repo = mRepos.get(position);


        holder.userNameText.setText(repo.getOwner().getLogin());

        holder.repoNameTxt.setText(repo.getName());
        holder.descrTxt.setText(repo.getDescription());
        holder.forksTxt.setText(String.valueOf(repo.getForksCount()));
        holder.watchesTxt.setText(String.valueOf(repo.getWatchersCount()));

        if (repo.getDescription().equals("")) {
            holder.descrTxt.setVisibility(View.GONE);
        } else {
            holder.descrTxt.setVisibility(View.VISIBLE);
        }


        ImageView imageView = holder.imageView;

        imageView.setImageResource(R.drawable.gitcat);

        DisplayImageOptions options = new DisplayImageOptions.Builder()

                .showStubImage(R.drawable.gravatar_icon)
                        // .showImageForEmptyUrl(R.drawable.image_for_empty_url)
                .resetViewBeforeLoading()
                .cacheInMemory()
                .cacheOnDisc()
                        //.decodingType(ImageScaleType.EXACT)
                .build();


        ImageLoader.getInstance().displayImage(repo.getOwner().getAvatarUrl(), imageView, options);
    }

    public GitHubRepo getItem(int position) {
        return mRepos.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemCount() {
        return mRepos.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView userNameText;
        private final TextView repoNameTxt;
        private final TextView descrTxt;
        private final TextView forksTxt;
        private final TextView watchesTxt;
        private final ImageView imageView;

        public ViewHolder(View holderView) {
            super(holderView);

            imageView = (ImageView) holderView.findViewById(R.id.imageView);

            userNameText = ((TextView) holderView.findViewById(R.id.userNameText));
            repoNameTxt = ((TextView) holderView.findViewById(R.id.repoNameTxt));
            descrTxt = ((TextView) holderView.findViewById(R.id.descrTxt));
            forksTxt = ((TextView) holderView.findViewById(R.id.forksTxt));
            watchesTxt = ((TextView) holderView.findViewById(R.id.watchesTxt));

        }
    }

}
