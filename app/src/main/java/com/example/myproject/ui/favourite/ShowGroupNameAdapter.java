package com.example.myproject.ui.favourite;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.data.model.db.CollectionGroup;

import java.util.List;

public class ShowGroupNameAdapter extends RecyclerView.Adapter<ShowGroupNameAdapter.ShowGroupNameViewHolder> {

    private List<CollectionGroup> groupNameList;
    private GroupNameClickListener groupNameClickListener;

    public ShowGroupNameAdapter() {
    }

    public ShowGroupNameAdapter(List<CollectionGroup> groupNameList) {
        this.groupNameList = groupNameList;
    }

    public void setGroupNameClickListener(GroupNameClickListener groupNameClickListener) {
        this.groupNameClickListener = groupNameClickListener;
    }

    @NonNull
    @Override
    public ShowGroupNameAdapter.ShowGroupNameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.show_group_name, parent, false);
        return new ShowGroupNameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShowGroupNameAdapter.ShowGroupNameViewHolder holder, int position) {
        holder.groupNameText.setText(groupNameList.get(position).getGroupName());
        if (groupNameClickListener != null) {
            holder.groupNameText.setOnClickListener(v -> groupNameClickListener.onClick(position));
        }
    }

    @Override
    public int getItemCount() {
        return groupNameList.size();
    }

    public static class ShowGroupNameViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameText;

        public ShowGroupNameViewHolder(@NonNull View itemView) {
            super(itemView);
            groupNameText = itemView.findViewById(R.id.show_group_name_tv);
        }
    }

    public interface GroupNameClickListener {
        void onClick(int position);
    }

    public void updateGroupNameList(List<CollectionGroup> groupNameList) {
        this.groupNameList = groupNameList;
        notifyDataSetChanged();
    }
}
