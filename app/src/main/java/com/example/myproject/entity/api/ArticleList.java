package com.example.myproject.entity.api;

import java.util.List;

public class ArticleList {
    private ArticleListData data;
    private int errorCode;
    private String errorMsg;

    public ArticleListData getData() {
        return data;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static class ArticleListData {
        private List<Article> datas;
        private int curPage;
        //总页数
        private int pageCount;
        //当前页的文章个数
        private int size;
        //总文章个数
        private int total;

        public List<Article> getDatas() {
            return datas;
        }

        public int getCurPage() {
            return curPage;
        }

        public int getPageCount() {
            return pageCount;
        }

        public int getSize() {
            return size;
        }

        public int getTotal() {
            return total;
        }
    }
}
