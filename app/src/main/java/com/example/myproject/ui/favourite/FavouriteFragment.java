package com.example.myproject.ui.favourite;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myproject.R;
import com.example.myproject.data.model.db.CollectionGroup;
import com.example.myproject.data.model.db.CollectionItem;
import com.example.myproject.data.repository.MyRepository;
import com.example.myproject.databinding.AddToOtherGroupBinding;
import com.example.myproject.databinding.BottomSheetSortDialogBinding;
import com.example.myproject.databinding.FragmentFavouriteBinding;
import com.example.myproject.databinding.ShowGroupBinding;
import com.example.myproject.ui.group.ManageGroupActivity;
import com.example.myproject.ui.web.WebViewActivity;
import com.example.myproject.util.ToastUtil;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

public class FavouriteFragment extends Fragment {
    private FragmentFavouriteBinding binding;
    private MyRepository repo;
    private long userId;
    private long groupId;
    private List<CollectionItem> list;
    private Toolbar toolbar;
    List<CollectionGroup> groupList;
    private FavouriteAdapter favouriteAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFavouriteBinding.inflate(getLayoutInflater());
        //使toolbar可用
        toolbar = binding.favouriteToolbar;

        repo = new MyRepository(requireActivity());
        repo.open();

        Bundle arguments = getArguments();
        userId = 0;
        groupId = 0;
        if (arguments != null) {
            userId = arguments.getLong("userId");
        }

        //加上groupId作为参数二
        list = repo.getCollectionItemList(userId, groupId);

        favouriteAdapter = new FavouriteAdapter(list);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity());

        binding.favouriteRecyclerView.setAdapter(favouriteAdapter);
        binding.favouriteRecyclerView.setLayoutManager(layoutManager);

        favouriteAdapter.setItemClickListener(position -> {
            //点击后在程序中打开链接
            String link = list.get(position).getLink();
            startActivity(new Intent(requireActivity(), WebViewActivity.class).putExtra("Link", link));
        });

        favouriteAdapter.setButtonClickListener(position -> {
            //再加上groupId参数，在MyRepository的方法中判断groupId是从总收藏中取消还是分组中取消收藏的
            //点击后取消(总，分组)收藏
            String link = list.get(position).getLink();
            groupId = list.get(position).getGroupId();
            list = repo.deleteCollectionItem(userId, groupId, link);
            favouriteAdapter.updateArticles(list);
        });

        //长按某个(总,分)组中的条目
        favouriteAdapter.setLongClickListener(itemPosition -> {
            AddToOtherGroupBinding binding = AddToOtherGroupBinding.inflate(getLayoutInflater());
            BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
            dialog.setContentView(binding.getRoot());

            groupList = repo.getCollectionGroups(userId);
            ShowGroupNameAdapter adapter = new ShowGroupNameAdapter(groupList);
            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(requireActivity());
            binding.addToGroupRv.setAdapter(adapter);
            binding.addToGroupRv.setLayoutManager(layoutManager1);

            adapter.setGroupNameClickListener(position -> {
                //itemPosition:被长按的收藏条目
                //position:分组的编号

                //获取被长按的收藏条目
                CollectionItem collectionItem = list.get(itemPosition);
                //获取被点击的分组
                CollectionGroup collectionGroup = groupList.get(position);
                //添加到数据库
                collectionItem.setGroupId(collectionGroup.getId());
                collectionItem.setGroupName(collectionGroup.getGroupName());
                repo.addItemInGroup(collectionItem);
                ToastUtil.show(requireActivity(), "已被添加到分组" + collectionItem.getGroupName());
                dialog.dismiss();
            });

            binding.cancelTv.setOnClickListener(v -> dialog.dismiss());

            dialog.show();
        });


        toolbar.setOnMenuItemClickListener(item -> {
            int choose = item.getItemId();
            if (choose == R.id.favourites_in_group) {
                //弹出分组(切换)
                ShowGroupBinding showGroupBinding = ShowGroupBinding.inflate(getLayoutInflater());
                BottomSheetDialog dialog = new BottomSheetDialog(requireActivity());
                dialog.setContentView(showGroupBinding.getRoot());

                groupList = repo.getCollectionGroups(userId);
                ShowGroupNameAdapter adapter = new ShowGroupNameAdapter(groupList);
                RecyclerView.LayoutManager layoutManager2 = new LinearLayoutManager(requireActivity());
                showGroupBinding.showGroupRv.setAdapter(adapter);
                showGroupBinding.showGroupRv.setLayoutManager(layoutManager2);

                //点击后展示对应分组下的条目
                adapter.setGroupNameClickListener(position -> {
                    groupId = groupList.get(position).getId();
                    String groupName=groupList.get(position).getGroupName();
                    toolbar.setTitle(groupName);
                    list = repo.getItemsFromGroup(userId, groupId);
                    favouriteAdapter.updateArticles(list);
                    dialog.dismiss();
                });

                //所有收藏
                showGroupBinding.allCollectionsTv.setOnClickListener(v -> {
                    list = repo.getCollectionItemList(userId, 0);
                    favouriteAdapter.updateArticles(list);
                    groupId = 0;
                    toolbar.setTitle("所有收藏");
                    dialog.dismiss();
                });

                //取消
                showGroupBinding.cancelText.setOnClickListener(v -> dialog.dismiss());

                dialog.show();

            } else if (choose == R.id.edit_groups) {
                //跳转到编辑分组的activity
                Intent intent = new Intent(requireActivity(), ManageGroupActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else if (choose == R.id.sort) {
                //弹出时间排序
                BottomSheetSortDialogBinding dialogBinding = BottomSheetSortDialogBinding.inflate(getLayoutInflater());
                BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
                dialog.setContentView(dialogBinding.getRoot());


                //升序
                dialogBinding.mixText.setOnClickListener(v -> {
                    list = repo.sortByCollectionTime(userId, groupId, MyRepository.SORT_ASC);
                    favouriteAdapter.updateArticles(list);
                    dialog.dismiss();
                });

                //降序
                dialogBinding.orderText.setOnClickListener(v -> {
                    list = repo.sortByCollectionTime(userId, groupId, MyRepository.SORT_DESC);
                    favouriteAdapter.updateArticles(list);
                    dialog.dismiss();
                });

                //取消
                dialogBinding.cancelText.setOnClickListener(v -> dialog.dismiss());

                dialog.show();
            }
            return true;
        });

        return binding.getRoot();
    }

    /**
     * 首页的 Tab 用的是 show/hide 切换，本 Fragment 不会重新走 onCreateView，
     * 所以在首页新收藏了文章之后切回来，要自己再查一次数据库刷新列表。
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && favouriteAdapter != null) {
            list = repo.getCollectionItemList(userId, groupId);
            favouriteAdapter.updateArticles(list);
        }
    }

}