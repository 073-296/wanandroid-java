package com.example.myproject.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    //表的内容
    //用户表
    public static final String TABLE_USERS = "table_users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String CREATE_TABLE_USERS = "create table " + TABLE_USERS + "(" +
            COLUMN_USER_ID + " integer primary key autoincrement," +
            COLUMN_USERNAME + " text," +
            COLUMN_PASSWORD + " text)";


    //收藏表
    public static final String TABLE_COLLECTIONS = "table_collections";
    public static final String COLUMN_COLLECTION_USER_ID = "user_id";
    public static final String COLUMN_COLLECTION_GROUP_ID = "group_id";
    public static final String COLUMN_COLLECTION_GROUP_NAME = "groupName";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_LINK = "link";
    public static final String COLUMN_COLLECT_TIME = "collectTime";
    public static final String CREATE_TABLE_COLLECTIONS = "create table " + TABLE_COLLECTIONS + "(" +
            COLUMN_COLLECTION_USER_ID + " integer," +
            COLUMN_COLLECTION_GROUP_ID + " integer," +
            COLUMN_COLLECTION_GROUP_NAME + " text," +
            COLUMN_TITLE + " text," +
            COLUMN_AUTHOR + " text," +
            COLUMN_LINK + " text," +
            COLUMN_COLLECT_TIME + " integer)";

    //分组表
    public static final String TABLE_GROUPS = "table_groups";
    public static final String COLUMN_GROUP_ID = "groups_id";
    public static final String COLUMN_GROUP_USER_ID = "user_id";
    public static final String COLUMN_GROUP_NAME = "groupName";
    public static final String CREATE_TABLE_GROUPS = "create table " + TABLE_GROUPS + "(" +
            COLUMN_GROUP_ID + " integer primary key autoincrement," +
            COLUMN_GROUP_USER_ID + " integer," +
            COLUMN_GROUP_NAME + " text" +
            ")";

    public MyDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //建表
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_COLLECTIONS);
        db.execSQL(CREATE_TABLE_GROUPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
