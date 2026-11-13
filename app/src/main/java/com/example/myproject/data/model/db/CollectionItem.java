package com.example.myproject.data.model.db;

public class CollectionItem {
    private long userId;
    private long groupId;
    private String groupName;
    private String title;
    private String author;
    private String link;
    private long collectTime;

    public CollectionItem() {
    }

    public CollectionItem(long userId, long groupId, String groupName, String title, String author, String link, long collectTime) {
        this.userId = userId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.title = title;
        this.author = author;
        this.link = link;
        this.collectTime = collectTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }
}
