package com.example.myproject.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.databinding.ActivityManageGroupBinding;
import com.example.myproject.db.MyDatabaseHelper;
import com.example.myproject.entity.db.CollectionGroup;
import com.example.myproject.repo.MyRepository;
import com.example.myproject.util.ToastUtil;
import com.example.myproject.ui.adapter.ManageGroupAdapter;

import java.util.List;

public class ManageGroupActivity extends AppCompatActivity {
    private ActivityManageGroupBinding binding;
    private List<CollectionGroup> collectionGroupList;
    private MyRepository repo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        long userId = intent.getLongExtra("userId", -1L);

        //toolbar 返回箭头：等价于系统返回（回到收藏页）
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
        repo = new MyRepository(ManageGroupActivity.this);
        repo.open();
        collectionGroupList = repo.getCollectionGroups(userId);
        ManageGroupAdapter adapter = new ManageGroupAdapter(collectionGroupList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ManageGroupActivity.this);
        binding.groupRv.setAdapter(adapter);
        binding.groupRv.setLayoutManager(layoutManager);

        //添加分组
        binding.addGroupIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = binding.addGroupEdit.getText().toString();

                if (!groupName.isEmpty()) {
                    long groupId = repo.addGroup(userId, groupName);
                    if (groupId != -1) {
                        adapter.addGroup(new CollectionGroup(userId, groupId, groupName));
                        binding.addGroupEdit.setText("");
                    } else {
                        ToastUtil.show(ManageGroupActivity.this, "该分组已存在");
                    }
                } else {
                    ToastUtil.show(ManageGroupActivity.this, "请输入内容");
                }
            }
        });

        //删除分组
        adapter.setGarbageOnClickListener(new ManageGroupAdapter.GarbageOnClickListener() {
            @Override
            public void onClick(int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ManageGroupActivity.this);
                builder
                        .setTitle("提示")
                        .setMessage("是否删除")
                        .setPositiveButton("确定", (dialog, which) -> {
                                    long id = adapter.getItem(position).getId();
                                    adapter.removeGroup(position);
                                    repo.deleteGroup(userId, id);
                                }
                        )
                        .setNegativeButton("取消", (dialog, which) -> {
                        })
                        .show();
            }
        });

        //确认
        adapter.setConfirmOnClickListener(new ManageGroupAdapter.ConfirmOnClickListener() {
            @Override
            public boolean onClick(int position,String editedGroupName) {
                long id = collectionGroupList.get(position).getId();
                long userId = collectionGroupList.get(position).getUserId();
                boolean exits = repo.updateGroupName(new CollectionGroup(id, userId, editedGroupName));
                if (exits) {
                    ToastUtil.show(ManageGroupActivity.this, "该分组已存在");
                }
                return exits;
            }
        });
    }
}