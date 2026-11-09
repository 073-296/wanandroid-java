package com.example.myproject.entity.api;

public class Article {
    String title;
    private String author;
    private String link;

    public Article() {
    }

    public Article(String title, String author, String link) {
        this.title = title;
        this.author = author;
        this.link = link;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return author;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

}
