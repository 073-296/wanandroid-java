package com.example.myproject.data.model.db;

public class CollectionGroup {
    private long id;
    private long userId;
    private String groupName;

    public CollectionGroup() {
    }

    public CollectionGroup(long id, long userId, String groupName) {
        this.id = id;
        this.userId = userId;
        this.groupName = groupName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}

//viewbinding
//bottomnavigationview + fragment
//回调的详解