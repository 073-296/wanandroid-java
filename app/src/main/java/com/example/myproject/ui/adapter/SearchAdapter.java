package com.example.myproject.ui.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.entity.api.Article;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {
    private List<Article> searchList;
    private OnTitleClickListener onTitleClickListener;

    public SearchAdapter() {
    }

    public SearchAdapter(List<Article> searchList) {
        this.searchList = searchList;
    }

    public void setOnTitleClickListener(OnTitleClickListener onTitleClickListener) {
        this.onTitleClickListener = onTitleClickListener;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);
        return new SearchHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchHolder holder, int position) {
        holder.title.setText(searchList.get(position).getTitle());
        holder.title.setEllipsize(TextUtils.TruncateAt.END);
        holder.title.setHorizontallyScrolling(true);
        holder.title.setOnClickListener(v ->
                onTitleClickListener.onClick(holder.getAdapterPosition()));

    }

    @Override
    public int getItemCount() {
        return searchList.size();
    }

    public static class SearchHolder extends RecyclerView.ViewHolder {
        TextView title;

        public SearchHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.show_title_tv);
        }
    }

    public interface OnTitleClickListener {
        void onClick(int position);
    }

}
