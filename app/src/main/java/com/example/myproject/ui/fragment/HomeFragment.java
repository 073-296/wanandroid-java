package com.example.myproject.ui.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.myproject.R;
import com.example.myproject.db.MyDatabaseHelper;
import com.example.myproject.entity.api.BannerImage;
import com.example.myproject.entity.db.CollectionItem;
import com.example.myproject.repo.MyRepository;
import com.example.myproject.ui.adapter.ImageSliderAdapter;
import com.example.myproject.ui.adapter.MyAdapter;
import com.example.myproject.databinding.FragmentHomeBinding;
import com.example.myproject.entity.api.Article;
import com.example.myproject.entity.api.ArticleList;
import com.example.myproject.ui.activity.WebViewActivity;
import com.example.myproject.util.HttpUtil;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    /** 轮播指示器圆点的直径（dp） */
    private static final float DOT_SIZE_DP = 8f;
    /** 轮播指示器圆点的左右外边距（dp） */
    private static final float DOT_MARGIN_DP = 4f;

    private FragmentHomeBinding binding;
    private String responseData;
    private List<Article> articles;
    private MyAdapter myAdapter;
    private MyRepository repo;
    private ViewPager2 viewPager2;
    private ImageSliderAdapter imageSliderAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Gson gson;
    private int pageCount = 0;
    private int bannerCount = 0;
    /** 是否正在加载下一页，防止滚到底部时重复请求同一页 */
    private boolean isLoadingMore = false;

    //轮播只保留一个 Handler + Runnable，视图销毁时必须移除，否则每切换一次 Tab 就多一个循环在后台跑
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private final ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {//position是当前页面在集合中的索引
            //更新小圆点
            //(position - 1 + length) % length -->图片在集合的位置(不是索引)
            if (bannerCount > 0 && binding != null) {
                updateIndicator((position - 1 + bannerCount) % bannerCount);
            }
        }

        //用于实现图片轮播的无限循环效果
        @Override
        public void onPageScrollStateChanged(int state) {
            //当页面滑动状态变为 SCROLL_STATE_IDLE（即滑动停止时）
            if (state != ViewPager2.SCROLL_STATE_IDLE || bannerCount == 0 || binding == null) {
                return;
            }
            //获取当前页面索引 position
            int position = binding.viewPager2.getCurrentItem();
            //默认从左往右播放
            if (position == 0) {
                //图3    图1  图2  图3     图1
                // 0      1   2    3      4
                //如果当前是第 0 页（即滑到最左边），若继续向左划一次，则设置为第2页（length），无动画跳转，实现循环
                binding.viewPager2.setCurrentItem(bannerCount, false);
            } else if (position == bannerCount + 1) {
                //如果当前是最后一页的下一个（length + 1，即滑到最右边），则无动画跳转到第 1 页，同样实现循环
                binding.viewPager2.setCurrentItem(1, false);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        gson = new Gson();

        //创建Article集合
        articles = new ArrayList<>();

        repo = new MyRepository(requireActivity());
        repo.open();

        //获取适配器对象
        myAdapter = new MyAdapter(articles);

        //创建布局管理器对象
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireActivity());

        //设置适配器
        binding.firstRecyclerView.setAdapter(myAdapter);

        //设置布局管理器
        binding.firstRecyclerView.setLayoutManager(layoutManager);

        binding.refreshBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initial();
            }
        });

        //下拉刷新
        swipeRefreshLayout = binding.swiperRefreshLayout;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        //分页加载
        binding.firstRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();//当前屏幕可见的 item 数量
                int totalItemCount = layoutManager.getItemCount();//RecyclerView 总 item 数量
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();//当前屏幕第一个可见 item 的位置
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount //最后一个 item 已经可见
                        && firstVisibleItemPosition >= 0) {//firstVisibleItemPosition >= 0 确保列表中有 item（防止空列表时出错）
                    //滚到底部会连续触发很多次 onScrolled，用 isLoadingMore 挡住，
                    //否则会同时发出好几个请求，同一页被重复加载好几次
                    if (!isLoadingMore) {
                        isLoadingMore = true;
                        loadNextPage(++pageCount);
                    }
                }
            }
        });


        viewPager2 = binding.viewPager2;
        //只在创建视图时注册一次，避免每次刷新/重新请求都多注册一个回调
        viewPager2.registerOnPageChangeCallback(pageChangeCallback);

        initial();

        //点击后传递网址给WebViewActivity并在其中打开
        myAdapter.setOnItemClickListener(position -> {
                    //获取被点击条目的网址
                    String link = myAdapter.getArticle(position).getLink();
                    startActivity(new Intent(requireActivity(), WebViewActivity.class).putExtra("Link", link));
                }
        );


        //点击收藏按钮后将所需信息存入表中
        myAdapter.setOnItemButtonClickListener(position -> {
            //获取按钮被点击的时间
            long collectTime = System.currentTimeMillis();

            //获取将要收藏的文章对象
            Article article = articles.get(position);
            CollectionItem item = new CollectionItem();
            if (getArguments() != null) {
                Bundle arguments = getArguments();
                long userId = arguments.getLong("userId");
                item.setUserId(userId);
            }
            item.setTitle(article.getTitle());
            item.setAuthor(article.getAuthor());
            item.setLink(article.getLink());
            item.setCollectTime(collectTime);
            //判断是否已经添加过该文章
            if (repo.judgeExits(MyDatabaseHelper.TABLE_COLLECTIONS,
                    "user_id =? and link=?",
                    new String[]{String.valueOf(item.getUserId()), item.getLink()})) {
                Toast.makeText(requireActivity(), "已经收藏过了", Toast.LENGTH_SHORT).show();
                return;
            }
            //添加进收藏表中
            long num = repo.addCollectionItem(item);
            if (num != -1) {
                Toast.makeText(requireActivity(), "收藏成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireActivity(), "未知错误", Toast.LENGTH_SHORT).show();
            }
        });

        return binding.getRoot();
    }

    public void initial() {
        //网络请求
        HttpUtil.sendOkHttpRequest("https://www.wanandroid.com/banner/json", new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                responseData = response.body().string();
                if (!response.isSuccessful()) {
                    runOnUiIfAlive(() -> {
                        stopBannerAutoScroll();
                        imageSliderAdapter = null;
                    });
                    return;
                }
                runOnUiIfAlive(() -> {
                    BannerImage bannerImage = gson.fromJson(responseData, BannerImage.class);
                    //获取到的图片的个数
                    int length = bannerImage.data.size();
                    bannerCount = length;
                    //初始化小圆点
                    setupIndicator(length);
                    updateIndicator(0);
                    imageSliderAdapter = new ImageSliderAdapter(requireActivity(), bannerImage.data);
                    binding.viewPager2.setAdapter(imageSliderAdapter);
                    binding.viewPager2.setCurrentItem(1, false);//默认显示第一张
                    //启动自动轮播（内部会先清掉上一次的定时任务）
                    startBannerAutoScroll();
                });
            }
        });

        getDataRequest();
    }

    private void setupIndicator(int count) {//参数count表示图片的个数
        binding.indicator.removeAllViews();
        //直接写 20 / 10 是裸像素，在不同 dpi 的屏幕上物理大小不一致（420dpi 上只有约 7.6dp），
        //这里统一换算成 dp，保证任何设备上圆点大小和间距都一样
        float density = getResources().getDisplayMetrics().density;
        int dotSize = (int) (DOT_SIZE_DP * density + 0.5f);
        int dotMargin = (int) (DOT_MARGIN_DP * density + 0.5f);
        for (int i = 0; i < count; i++) {
            View dot = new View(requireActivity());
            dot.setBackgroundResource(i == 0 ? R.drawable.indicator_dot_selected : R.drawable.indicator_dot_unselected);
            //设置dot的布局参数
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);
            binding.indicator.addView(dot);
        }
    }

    private void updateIndicator(int position) {
        int count = binding.indicator.getChildCount();//获取小圆点的个数
        for (int i = 0; i < count; i++) {//遍历每一个小圆点并绘制颜色
            View dot = binding.indicator.getChildAt(i);
            dot.setBackgroundResource(i == position ? R.drawable.indicator_dot_selected : R.drawable.indicator_dot_unselected);
        }
    }

    /**
     * 开始自动轮播。先移除上一次的定时任务，保证不管 initial() 被调用多少次，
     * 后台都只有一个轮播循环在跑。
     */
    private void startBannerAutoScroll() {
        stopBannerAutoScroll();
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || binding == null || imageSliderAdapter == null) {
                    return;
                }
                int itemCount = imageSliderAdapter.getItemCount();
                if (itemCount <= 1) {
                    return;
                }
                int currentPosition = binding.viewPager2.getCurrentItem();
                if (currentPosition == itemCount - 1) {
                    currentPosition = -1;
                }
                binding.viewPager2.setCurrentItem(currentPosition + 1, true);
                bannerHandler.postDelayed(this, 3000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void stopBannerAutoScroll() {
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
            bannerRunnable = null;
        }
    }

    /**
     * 视图销毁时把还在排队的东西全部清掉：
     * 轮播的 Runnable 会一直持有 binding，不清掉就会内存泄漏，
     * 而且会在界面已经销毁后继续操作 View。
     */
    @Override
    public void onDestroyView() {
        stopBannerAutoScroll();
        if (viewPager2 != null) {
            viewPager2.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        //清掉对视图的引用，避免 Fragment 已经销毁了还抱着整棵 View 树
        imageSliderAdapter = null;
        binding = null;
        super.onDestroyView();
    }

    private void getDataRequest() {
        //重新拉第一页时，分页计数要归零，否则下拉刷新后会从第 N 页接着加载，中间的文章就丢了
        pageCount = 0;
        HttpUtil.sendOkHttpRequest("https://www.wanandroid.com/article/list/0/json", new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiIfAlive(HomeFragment.this::showError);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                responseData = response.body().string();
                runOnUiIfAlive(() -> {
                    binding.errorMessage.setVisibility(View.GONE);
                    binding.refreshBt.setVisibility(View.GONE);
                    //用gson反序列化获得对象，填充集合
                    ArticleList articleList = gson.fromJson(responseData, ArticleList.class);
                    articles = articleList.data.datas;
                    isLoadingMore = false;
                    myAdapter.updateArticles(articles);
                    showFinish();
                });
            }
        });
    }

    /**
     * 网络回调可能在界面已经销毁之后才回来（比如切换账号时整个 Activity 被清掉），
     * 这时再碰 binding 或者 requireActivity() 就会崩，所以统一在这里判断一下。
     */
    private void runOnUiIfAlive(Runnable action) {
        if (!isAdded() || binding == null || getActivity() == null) {
            return;
        }
        requireActivity().runOnUiThread(() -> {
            if (isAdded() && binding != null) {
                action.run();
            }
        });
    }


    public void showErrorMessageAndButton() {
        binding.errorMessage.setVisibility(View.VISIBLE);
        binding.refreshBt.setVisibility(View.VISIBLE);
        binding.refreshBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initial();
            }
        });
    }

    public void refreshData() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }
            showLoading();
            getDataRequest();
            //停止刷新动画
            swipeRefreshLayout.setRefreshing(false);
        }, 2000);
    }

    public void showFinish() {
        binding.loadingLayout.setVisibility(View.GONE);
        binding.swiperRefreshLayout.setVisibility(View.VISIBLE);
        binding.errorLayout.setVisibility(View.GONE);
    }

    public void showLoading() {
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.swiperRefreshLayout.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.GONE);
    }

    public void showError() {
        binding.loadingLayout.setVisibility(View.GONE);
        binding.swiperRefreshLayout.setVisibility(View.GONE);
        binding.errorLayout.setVisibility(View.VISIBLE);
    }

    private void loadNextPage(int page) {
        HttpUtil.sendOkHttpRequest("https://www.wanandroid.com/article/list/" + page + "/json", new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiIfAlive(() -> {
                    isLoadingMore = false;
                    showError();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                responseData = response.body().string();
                runOnUiIfAlive(() -> {
                    binding.errorMessage.setVisibility(View.GONE);
                    binding.refreshBt.setVisibility(View.GONE);
                    //用gson反序列化获得对象，追加到列表尾部
                    //注意：不能写成 articles = articleList.data.datas，那样 articles 就只剩新的一页，
                    //点击条目时 position 会和适配器里的真实列表对不上
                    ArticleList articleList = gson.fromJson(responseData, ArticleList.class);
                    myAdapter.loadMore(articleList.data.datas);
                    isLoadingMore = false;
                    showFinish();
                });
            }
        });
    }
}