package com.klgleb.githubclient;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.klgleb.github.model.GitHubCommit;

import java.util.ArrayList;

/**
 * Adapter for repositories ListView.
 * <p/>
 * Created by klgleb on 11.07.15.
 */
public class CommitsAdapter extends BaseAdapter {

    private final ArrayList<GitHubCommit> mCommits;

    public CommitsAdapter(ArrayList<GitHubCommit> repos) {
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        View view = convertView;

        if (view == null) {
            //view = lInflater.inflate(R.layout.item, parent, false);
            view = new TextView(viewGroup.getContext());
        }


        ((TextView) view).setText(getItem(i).getRepOwner() + "/" + getItem(i).getRepoName());

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
