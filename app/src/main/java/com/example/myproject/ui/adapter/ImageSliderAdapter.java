package com.example.myproject.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myproject.R;
import com.example.myproject.entity.api.BannerImage;
import com.example.myproject.entity.api.ImageMessage;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.SliderViewHolder> {
    private Context context;
    private List<ImageMessage> imageList;

    public ImageSliderAdapter() {
    }

    public ImageSliderAdapter(Context context, List<ImageMessage> imageList) {
        this.context = context;
        this.imageList = new ArrayList<>();//初始化集合
        this.imageList.add(imageList.get(imageList.size() - 1));//最后一张放到最前面
        this.imageList.addAll(imageList);
        this.imageList.add(imageList.get(0));//第一张放到最后面
    }

    @NonNull
    @Override
    public ImageSliderAdapter.SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageSliderAdapter.SliderViewHolder holder, int position) {
        //使用Glide加载图片
        Glide
                .with(context)//初始化glide请求，创建一个图片加载的上下文环境
                .load(imageList.get(position).getImagePath())
                //.load(imageList.get(position % imageList.size()).getImagePath())
                .into(holder.imageView);//指定图片加载的目标ImageView

    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.banner_image_view);
        }
    }
}
