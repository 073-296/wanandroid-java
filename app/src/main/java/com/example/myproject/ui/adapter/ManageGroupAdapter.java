package com.example.myproject.ui.adapter;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.entity.db.CollectionGroup;

import java.util.List;


public class ManageGroupAdapter extends RecyclerView.Adapter<ManageGroupAdapter.NewViewHolder> {

    private List<CollectionGroup> collectionGroupList;
    private GarbageOnClickListener garbageOnClickListener;
    private ConfirmOnClickListener confirmOnClickListener;

    public ManageGroupAdapter() {
    }

    public ManageGroupAdapter(List<CollectionGroup> collectionGroupList) {
        this.collectionGroupList = collectionGroupList;
    }

    public void setGarbageOnClickListener(GarbageOnClickListener garbageOnClickListener) {
        this.garbageOnClickListener = garbageOnClickListener;
    }

    public void setConfirmOnClickListener(ConfirmOnClickListener confirmOnClickListener) {
        this.confirmOnClickListener = confirmOnClickListener;
    }


    @NonNull
    @Override
    public ManageGroupAdapter.NewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.new_group_item, parent, false);
        return new NewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageGroupAdapter.NewViewHolder holder, int position) {
        holder.editText.setText(collectionGroupList.get(position).getGroupName());
        String originalGroupName = collectionGroupList.get(position).getGroupName();
        editTextEnable(false, holder.editText);
        if (garbageOnClickListener != null) {
            holder.garbage.setOnClickListener(v -> garbageOnClickListener.onClick(holder.getAdapterPosition()));
        }
        if (confirmOnClickListener != null) {
            holder.confirm.setOnClickListener(v -> {
                String editedGroupName = holder.editText.getText().toString();
                boolean noUpdate = confirmOnClickListener.onClick(holder.getAdapterPosition(), editedGroupName);
                if (noUpdate) {
                    holder.editText.setText(originalGroupName);
                }
                holder.confirm.setVisibility(View.GONE);
                editTextEnable(false, holder.editText);
            });
        }
        holder.editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextEnable(true, holder.editText);
                holder.confirm.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return collectionGroupList.size();
    }


    //新布局的ViewHolder
    public static class NewViewHolder extends RecyclerView.ViewHolder {
        ImageView editImage;
        EditText editText;
        ImageView garbage;
        ImageView confirm;

        public NewViewHolder(@NonNull View itemView) {
            super(itemView);
            editImage = itemView.findViewById(R.id.new_edit_iv);
            editText = itemView.findViewById(R.id.new_edit_et);
            garbage = itemView.findViewById(R.id.new_garbage_iv);
            confirm = itemView.findViewById(R.id.new_confirm_iv);
        }
    }

    public interface GarbageOnClickListener {
        void onClick(int position);
    }

    public interface ConfirmOnClickListener {
        boolean onClick(int position, String editedGroupName);
    }

    private void editTextEnable(boolean enable, EditText editText) {
        editText.setFocusable(enable);
        editText.setFocusableInTouchMode(enable);
        editText.setLongClickable(false);
        editText.setInputType(enable ? InputType.TYPE_CLASS_TEXT : InputType.TYPE_NULL);
        if (enable) {
            editText.setSelection(editText.getText().length());
        }
    }

    public void updateCollectionGroup(List<CollectionGroup> collectionGroupList) {
        this.collectionGroupList.clear();
        this.collectionGroupList.addAll(collectionGroupList);
        notifyDataSetChanged();
    }

    public void removeGroup(int position) {
        collectionGroupList.remove(position);
        notifyItemRemoved(position);
    }

    public void addGroup(CollectionGroup collectionGroup) {
        collectionGroupList.add(collectionGroup);
        notifyItemInserted(collectionGroupList.size());
    }

    public CollectionGroup getItem(int position) {
        return collectionGroupList.get(position);
    }
}
