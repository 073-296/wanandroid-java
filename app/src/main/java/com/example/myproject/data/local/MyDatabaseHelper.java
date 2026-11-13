package com.example.myproject.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 2;

    /**
     * 全局唯一实例。
     * 一个 SQLiteOpenHelper 对应一条到数据库文件的连接，构造函数每次都会新建一条。
     * 之前每个 Activity/Fragment 各 new 一个 MyRepository，等于同一个库文件上挂了
     * 好几条连接，并发写时可能互相锁住（SQLiteDatabaseLockedException）。
     * 改成单例后全 App 共用一条连接，也就不再需要手动 close()。
     */
    private static volatile MyDatabaseHelper instance;

    /**
     * 私有构造：只能通过 getInstance() 获取。
     * 必须用 ApplicationContext —— 单例是静态的、生命周期比任何界面都长，
     * 直接持有 Activity 的 Context 会导致整个界面无法回收。
     */
    private MyDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 双重检查锁获取单例（第一次调用时才真正实例化，之后直接返回缓存）。
     */
    public static MyDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (MyDatabaseHelper.class) {
                if (instance == null) {
                    instance = new MyDatabaseHelper(context);
                }
            }
        }
        return instance;
    }

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
