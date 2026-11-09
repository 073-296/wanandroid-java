package com.example.myproject.entity.api;

import java.util.List;

public class ArticleList {
    public ArticleListData data;
    public int errorCode;
    public String errorMsg;

    public static class ArticleListData{
        public List<Article> datas;
        public int curPage;
        //总页数
        public int pageCount;
        //当前页的文章个数
        public int size;
        //总文章个数
        public int total;
    }
}
