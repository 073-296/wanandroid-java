package com.example.myproject.ui.favourite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.data.model.api.Article;
import com.example.myproject.data.model.db.CollectionItem;

import java.util.List;

public class FavouriteAdapter extends RecyclerView.Adapter<FavouriteAdapter.FavouriteHolder> {
    private List<CollectionItem> collectionItemList;
    private onItemClickListener itemClickListener;
    private onItemButtonClickListener buttonClickListener;
    private LongClickListener longClickListener;

    public FavouriteAdapter() {
    }

    public FavouriteAdapter(List<CollectionItem> collectionItemList) {
        this.collectionItemList = collectionItemList;
    }

    public void setItemClickListener(onItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setButtonClickListener(onItemButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    @NonNull
    @Override
    public FavouriteAdapter.FavouriteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item, parent, false);
        return new FavouriteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteAdapter.FavouriteHolder holder, int position) {
        holder.title.setText(collectionItemList.get(position).getTitle());
        holder.collectButton.setText("取消收藏");
        //收藏表里存的是收藏时的展示作者，空串（含历史数据）时兜底显示“佚名”
        String author = collectionItemList.get(position).getAuthor();
        if (author == null || author.isEmpty()) {
            holder.subTitle.setText("佚名");
        } else {
            holder.subTitle.setText(author);
        }

        if (itemClickListener != null) {
            holder.itemView.setOnClickListener(v ->
                    itemClickListener.onClick(position));
        }
        if (buttonClickListener != null) {
            holder.collectButton.setOnClickListener(v ->
                    buttonClickListener.onClick(position));
        }
        holder.itemView.setOnLongClickListener( v-> {
                if(longClickListener!=null){
                    longClickListener.onClick(position);
                    return true;
                }
                return false;
            }
        );

    }

    @Override
    public int getItemCount() {
        return collectionItemList.size();
    }

    public static class FavouriteHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView subTitle;
        Button collectButton;

        public FavouriteHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title_text);
            subTitle = itemView.findViewById(R.id.subTitle_text);
            collectButton = itemView.findViewById(R.id.collect_button);
        }
    }

    public interface onItemClickListener {
        void onClick(int position);
    }

    public interface onItemButtonClickListener {
        void onClick(int position);
    }

    public interface LongClickListener{
        void onClick(int itemPosition);
    }

    public void updateArticles(List<CollectionItem> collectionItemList) {
        this.collectionItemList = collectionItemList;
        notifyDataSetChanged();
    }
}
