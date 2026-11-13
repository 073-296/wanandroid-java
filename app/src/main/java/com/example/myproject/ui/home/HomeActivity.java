package com.example.myproject.ui.home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myproject.R;
import com.example.myproject.databinding.ActivityHomeBinding;
import com.example.myproject.ui.account.AccountFragment;
import com.example.myproject.ui.favourite.FavouriteFragment;
/**
 * 首页容器：底部三个 Tab 对应三个 Fragment。
 *
 * 关键点（也是原来 bug 的根源）：
 * 1. Fragment 只创建一次，之后用 show/hide 切换，而不是每点一次就 new 一个新的；
 * 2. Activity 被系统重建（旋转屏幕、深色模式切换、进程被杀后恢复）时，
 *    FragmentManager 会自动把之前的 Fragment 恢复出来，此时绝不能再 add 一遍，
 *    否则同一个容器里会叠着两份界面、网络请求也会发两遍。
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG_HOME = "tab_home";
    private static final String TAG_FAVOURITE = "tab_favourite";
    private static final String TAG_ACCOUNT = "tab_account";

    private ActivityHomeBinding binding;
    /** 传给三个 Fragment 的用户信息，只构建一次 */
    private Bundle args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //获取用户名以及密码
        Intent intent = getIntent();
        long userId = intent.getLongExtra("userId", -1L);
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");

        args = new Bundle();
        args.putLong("userId", userId);
        args.putString("username", username);
        args.putString("password", password);

        //只有在"一个 Fragment 都没有"的时候才添加，避免重建时重复添加
        if (findAnyTabFragment() == null) {
            switchTab(savedInstanceState == null
                    ? R.id.nav_find
                    : binding.mainBottomNav.getSelectedItemId());
        }

        binding.mainBottomNav.setOnItemSelectedListener(item -> {
            switchTab(item.getItemId());
            return true;
        });
    }

    private Fragment findAnyTabFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(TAG_HOME);
        if (fragment == null) {
            fragment = fm.findFragmentByTag(TAG_FAVOURITE);
        }
        if (fragment == null) {
            fragment = fm.findFragmentByTag(TAG_ACCOUNT);
        }
        return fragment;
    }

    /**
     * 切换到指定 Tab：没有就 add 一个，有就 show 出来，其余的 hide。
     * 因为 Fragment 实例被复用，来回切换不会重复走 onCreateView、不会重复发请求，
     * 列表的滚动位置也能保留。
     */
    private void switchTab(int itemId) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment target = fm.findFragmentByTag(tagOf(itemId));
        FragmentTransaction transaction = fm.beginTransaction().setReorderingAllowed(true);

        if (target == null) {
            target = createFragment(itemId);
            if (target == null) {
                return;
            }
            target.setArguments(args);
            transaction.add(R.id.fragmentContainer, target, tagOf(itemId));
        }

        //同一个容器里只能有一个可见的 Fragment
        for (Fragment fragment : fm.getFragments()) {
            if (fragment != target && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        if (target.isHidden()) {
            transaction.show(target);
        }
        //用 commitNow 让显示/隐藏立刻生效，快速连点 Tab 也不会出现两个同时可见
        transaction.commitNow();
    }

    private Fragment createFragment(int itemId) {
        if (itemId == R.id.nav_find) {
            return new HomeFragment();
        } else if (itemId == R.id.nav_collect) {
            return new FavouriteFragment();
        } else if (itemId == R.id.nav_account) {
            return new AccountFragment();
        }
        return null;
    }

    private String tagOf(int itemId) {
        if (itemId == R.id.nav_find) {
            return TAG_HOME;
        } else if (itemId == R.id.nav_collect) {
            return TAG_FAVOURITE;
        } else {
            return TAG_ACCOUNT;
        }
    }
}
