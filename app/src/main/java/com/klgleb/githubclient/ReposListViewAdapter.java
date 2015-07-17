package com.klgleb.githubclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
public class ReposListViewAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<GitHubRepo> mRepos;

    public ReposListViewAdapter(Context context, ArrayList<GitHubRepo> repos) {
        mContext = context;
        mRepos = repos;
    }

    @Override
    public int getCount() {
        return mRepos.size();
    }

    @Override
    public GitHubRepo getItem(int i) {
        return mRepos.get(i);
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


            view = inflater.inflate(R.layout.repositories_list_item, viewGroup, false);
        }


        GitHubRepo repo = mRepos.get(position);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        TextView userNameText = ((TextView) view.findViewById(R.id.userNameText));
        TextView repoNameTxt = ((TextView) view.findViewById(R.id.repoNameTxt));
        TextView descrTxt = ((TextView) view.findViewById(R.id.descrTxt));
        TextView forksTxt = ((TextView) view.findViewById(R.id.forksTxt));
        TextView watchesTxt = ((TextView) view.findViewById(R.id.watchesTxt));


        userNameText.setText(repo.getOwner().getLogin());

        repoNameTxt.setText(repo.getName());
        descrTxt.setText(repo.getDescription());
        forksTxt.setText(String.valueOf(repo.getForksCount()));
        watchesTxt.setText(String.valueOf(repo.getWatchersCount()));

        if (repo.getDescription().equals("")) {
            descrTxt.setVisibility(View.GONE);
        } else {
            descrTxt.setVisibility(View.VISIBLE);
        }


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


        /*Product p = getProduct(position);

        // заполняем View в пункте списка данными из товаров: наименование, цена
        // и картинка
        ((TextView) view.findViewById(R.id.tvDescr)).setText(p.name);
        ((TextView) view.findViewById(R.id.tvPrice)).setText(p.price + "");
        ((ImageView) view.findViewById(R.id.ivImage)).setImageResource(p.image);

        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.cbBox);
        // присваиваем чекбоксу обработчик
        cbBuy.setOnCheckedChangeListener(myCheckChangList);
        // пишем позицию
        cbBuy.setTag(position);
        // заполняем данными из товаров: в корзине или нет
        cbBuy.setChecked(p.box);
        return view;*/

        return view;
    }
}
