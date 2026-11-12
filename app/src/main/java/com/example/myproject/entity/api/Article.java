package com.example.myproject.entity.api;

/**
 * 文章条目 DTO。字段由 Gson 反射填充，不提供 setter。
 */
public class Article {
    private String title;
    private String author;
    private String link;
    //分享者：接口约定 author 为空字符串时，分享类文章的作者放在 shareUser 里
    private String shareUser;

    public Article() {
    }

    public Article(String title, String author, String link) {
        this.title = title;
        this.author = author;
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getLink() {
        return link;
    }

    public String getShareUser() {
        return shareUser;
    }

    /**
     * 展示用的作者名：author 有值用 author，为空则用 shareUser，
     * 两者都为空时返回空字符串，由调用方决定兜底文案（如“佚名”）。
     */
    public String getDisplayAuthor() {
        if (author != null && !author.isEmpty()) {
            return author;
        }
        if (shareUser != null && !shareUser.isEmpty()) {
            return shareUser;
        }
        return "";
    }
}
