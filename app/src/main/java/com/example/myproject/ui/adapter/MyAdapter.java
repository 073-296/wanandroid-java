package com.example.myproject.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.entity.api.Article;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_FOOTER = 1;
    List<Article> articles;

    public MyAdapter() {
    }

    public MyAdapter(List<Article> articles) {
        this.articles = articles;
    }

    private OnItemClickListener onItemClickListener;
    private OnItemButtonClickListener onItemButtonClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemButtonClickListener(OnItemButtonClickListener onItemButtonClickListener) {
        this.onItemButtonClickListener = onItemButtonClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        //因为 getItemCount() 返回的是 articles.size() + 1，所以最后一个位置（即 position == articles.size()）就是 footer 的位置
        if (position == articles.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_NORMAL;
    }

    @NonNull
    @Override
    //获得装子控件的容器
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
            return new MyHolder(view);
        }
        //R.layout.footer_item只有一个文本，显示“加载更多...”
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_item, parent, false);
        return new FooterHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyHolder) {
            MyHolder myHolder=(MyHolder) holder;
            myHolder.title.setText(articles.get(position).getTitle());
            //判断author是否为空
            String author = articles.get(position).getAuthor();
            if (author.isEmpty()) {
                myHolder.subTitle.setText("佚名");
            } else {
                myHolder.subTitle.setText(author);
            }
            //接口变量没有初始化，可能会出现空指针异常
            if (onItemClickListener != null) {
                myHolder.itemView.setOnClickListener((View v) ->
                        onItemClickListener.onClick(position)
                );
            }
            if (onItemButtonClickListener != null) {
                myHolder.collectButton.setOnClickListener(v ->
                        onItemButtonClickListener.onClick(position)
                );
            }
        }
    }

    @Override
    public int getItemCount() {
        return articles.size()+1;
    }

    public static class MyHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subTitle;
        Button collectButton;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_text);
            subTitle = itemView.findViewById(R.id.subTitle_text);
            collectButton = itemView.findViewById(R.id.collect_button);
        }
    }

    public static class FooterHolder extends RecyclerView.ViewHolder {
        public FooterHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public interface OnItemButtonClickListener {
        void onClick(int position);
    }

    public void updateArticles(List<Article> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    public void loadMore(List<Article> articles) {
        int oldSize = this.articles.size();
        this.articles.addAll(articles);
        //通知 RecyclerView，从 oldSize 位置开始，有 articles.size() 个新项被插入。这样界面会高效地只刷新新增的那一段数据，而不是全部刷新，提高性能和用户体验
        notifyItemRangeInserted(oldSize, articles.size());
    }

    public Article getArticle(int position) {
        return articles.get(position);
    }
}
