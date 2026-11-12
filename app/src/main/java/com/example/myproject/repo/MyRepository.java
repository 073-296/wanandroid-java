package com.example.myproject.repo;

import android.content.ContentValues;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myproject.db.MyDatabaseHelper;
import com.example.myproject.entity.db.CollectionGroup;
import com.example.myproject.entity.db.CollectionItem;
import com.example.myproject.entity.db.User;

import java.util.ArrayList;
import java.util.List;

public class MyRepository {
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";
    private MyDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    public MyRepository(Context context) {
        //数据库名和版本号已收进 MyDatabaseHelper，这里取的是全局唯一实例，
        //重复 new MyRepository 也只是复用同一条连接，不会新开
        dbHelper = MyDatabaseHelper.getInstance(context);
    }

    /**
     * 建立/复用连接。getWritableDatabase() 内部会缓存已打开的连接，
     * 重复调用是幂等的，所以各页面照旧在 onCreate 里调用即可。
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    //说明：close() 已移除。现在是全 App 共享一条连接，任何一处关掉都会让别的页面
    //拿到"已关闭的数据库"而崩溃；连接由进程统一持有，进程退出时系统自动回收。

    //用户
    //注册
    //成功后将返回值放到Intent中给后面的活动
    public long register(String username, String password) {
        //判断该用户名是否存在
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_USERS,
                    new String[]{MyDatabaseHelper.COLUMN_USERNAME},
                    MyDatabaseHelper.COLUMN_USERNAME + "=?",
                    new String[]{username},
                    null, null, null, null);
            //存在，返回-1
            if (cursor.getCount() > 0) {
                return -1;
            }
            //用户名不存在，可以注册
            ContentValues values = new ContentValues();
            values.put(MyDatabaseHelper.COLUMN_USERNAME, username);
            values.put(MyDatabaseHelper.COLUMN_PASSWORD, password);
            return database.insert(MyDatabaseHelper.TABLE_USERS, null, values);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //登录
    public long login(String username, String password) {
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_USERS, null, null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_USERNAME);
                    String usernameStr = cursor.getString(index);
                    index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PASSWORD);
                    String passwordStr = cursor.getString(index);
                    if (username.equals(usernameStr) && password.equals(passwordStr)) {
                        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_USER_ID);
                        long userId = cursor.getLong(index);
                        cursor.close();
                        return userId;
                    }
                } while (cursor.moveToNext());
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //获取用户
    public User getUserById(long userId) {
        Cursor cursor = null;
        try {
            cursor = database.query(
                    MyDatabaseHelper.TABLE_USERS,
                    null,
                    MyDatabaseHelper.COLUMN_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                User user = cursorToUser(cursor);
                //给用户实例写入id
                user.setId(userId);
                return user;
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_USERNAME);
        user.setUsername(cursor.getString(index));
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PASSWORD);
        user.setPassword(cursor.getString(index));
        return user;
    }

    public User getUserByUsername(String username) {
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_USERS,
                    null,
                    MyDatabaseHelper.COLUMN_USERNAME + "=?",
                    new String[]{username},
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                User user = new User();
                int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_USER_ID);
                user.setId(cursor.getLong(index));
                user.setUsername(username);
                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PASSWORD);
                user.setPassword(cursor.getString(index));
                return user;
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    //更改用户密码
    public void updatePassword(long userId, String password) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_PASSWORD, password);
        database.update(MyDatabaseHelper.TABLE_USERS,
                values,
                MyDatabaseHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    //注销账户
    public void deleteUser(long userId) {
        //删除在用户表的信息
        database.delete(MyDatabaseHelper.TABLE_USERS, MyDatabaseHelper.COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        //删除在分组表的信息
        database.delete(MyDatabaseHelper.TABLE_GROUPS, MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=?", new String[]{String.valueOf(userId)});
        //删除在收藏表的信息
        database.delete(MyDatabaseHelper.TABLE_COLLECTIONS, MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }


    //分组
    //创建分组
    public long addGroup(long userId, String groupName) {
        //判断在该账户下有无重复的分组名
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_GROUPS,
                    new String[]{MyDatabaseHelper.COLUMN_GROUP_USER_ID, MyDatabaseHelper.COLUMN_GROUP_NAME},
                    MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_GROUP_NAME + "=?",
                    new String[]{String.valueOf(userId), groupName},
                    null, null, null, null);

            //不存在，写入表中
            if (cursor.getCount() == 0) {
                ContentValues values = new ContentValues();
                values.put(MyDatabaseHelper.COLUMN_GROUP_USER_ID, userId);
                values.put(MyDatabaseHelper.COLUMN_GROUP_NAME, groupName);
                database.insert(MyDatabaseHelper.TABLE_GROUPS, "", values);
                return getGroupId(userId, groupName);
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public long getGroupId(long userId, String groupName) {
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_GROUPS,
                    new String[]{MyDatabaseHelper.COLUMN_GROUP_ID},
                    MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_GROUP_NAME + "=?",
                    new String[]{String.valueOf(userId), groupName},
                    null, null, null, null);
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_GROUP_ID);
                return cursor.getLong(index);
            }
            return -1;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //删除分组
    public void deleteGroup(long userId, long groupId) {
        //收藏表
        database.delete(MyDatabaseHelper.TABLE_COLLECTIONS,
                MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(groupId)});

        //分组表
        database.delete(MyDatabaseHelper.TABLE_GROUPS,
                MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_GROUP_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(groupId)});

        //return getCollectionGroups(userId);
    }

    //修改分组名
    public boolean updateGroupName(CollectionGroup group) {
        Cursor cursor = null;
        boolean exits = judgeExits(MyDatabaseHelper.TABLE_GROUPS,
                MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_GROUP_NAME + "=?",
                new String[]{String.valueOf(group.getUserId()), group.getGroupName()});
        try {
            if (!exits) {
                //在同一个账号下没有相同的分组名
                ContentValues values1 = new ContentValues();
                values1.put(MyDatabaseHelper.COLUMN_GROUP_NAME, group.getGroupName());
                ContentValues values2 = new ContentValues();
                values2.put(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_NAME, group.getGroupName());
                //分组表
                database.update(MyDatabaseHelper.TABLE_GROUPS,
                        values1,
                        MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_GROUP_ID + "=?",
                        new String[]{String.valueOf(group.getUserId()), String.valueOf(group.getId())});
                //收藏表
                database.update(MyDatabaseHelper.TABLE_COLLECTIONS,
                        values2,
                        MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=?",
                        new String[]{String.valueOf(group.getUserId()), String.valueOf(group.getId())});
            }
            return exits;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    //获得该账号下的分组集合
    public List<CollectionGroup> getCollectionGroups(long userId) {
        List<CollectionGroup> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_GROUPS,
                    null,
                    MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    null, null, null, null);

            while (cursor.moveToNext()) {
                CollectionGroup group = new CollectionGroup();
                int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_GROUP_USER_ID);
                group.setUserId(cursor.getLong(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_GROUP_ID);
                group.setId(cursor.getLong(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_GROUP_NAME);
                group.setGroupName(cursor.getString(index));

                list.add(group);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }


    //从分组中获取收藏的条目，以集合的形式返回
    public List<CollectionItem> getItemsFromGroup(long userId, long groupId) {
        List<CollectionItem> collectionItems = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_COLLECTIONS,
                    null,
                    MyDatabaseHelper.COLUMN_GROUP_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(groupId)},
                    null, null, null, null);

            while (cursor.moveToNext()) {
                CollectionItem item = new CollectionItem();
                int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECTION_USER_ID);
                item.setUserId(cursor.getLong(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID);
                item.setGroupId(cursor.getLong(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_NAME);
                item.setGroupName(cursor.getString(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TITLE);
                item.setTitle(cursor.getString(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_AUTHOR);
                item.setAuthor(cursor.getString(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LINK);
                item.setLink(cursor.getString(index));

                index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECT_TIME);
                item.setCollectTime(cursor.getLong(index));

                collectionItems.add(item);
            }

            return collectionItems;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    //添加条目到分组中
    //判断条目是否收藏在分组中
    //userId,groupId,link
    //返回新的集合
    public List<CollectionItem> addItemInGroup(CollectionItem item) {//item的groupId是想要添加到的分组的id
        Cursor cursor = null;
        try {
            //select * from table_collections where user_id = ? and groups_id = ? and link=?;
            cursor = database.query(MyDatabaseHelper.TABLE_COLLECTIONS,
                    null,
                    MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=? and " + MyDatabaseHelper.COLUMN_LINK + "=?",
                    new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getGroupId()), item.getLink()},
                    null, null, null, null);

            if (cursor.getCount() == 0) {
                //还没被收藏
                ContentValues values = getItemValue(item);
                database.insert(MyDatabaseHelper.TABLE_COLLECTIONS, "", values);
            }

            return getItemsFromGroup(item.getUserId(), item.getGroupId());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    //删除分组中单个条目(通过userId,groupId,link)
    public List<CollectionItem> deleteItemInGroup(CollectionItem item) {
        database.delete(MyDatabaseHelper.TABLE_COLLECTIONS,
                MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=? and " + MyDatabaseHelper.COLUMN_LINK + "=?",
                new String[]{String.valueOf(item.getUserId()), String.valueOf(item.getGroupId()), item.getLink()});

        return getItemsFromGroup(item.getUserId(), item.getGroupId());
    }


    //收藏
    //添加收藏的条目
    public long addCollectionItem(CollectionItem item) {
        //判断是否已收藏(userId,link)
        Cursor cursor = null;
        long result = -1;
        try {
            if (
                    !judgeExits(MyDatabaseHelper.TABLE_COLLECTIONS,
                            MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_LINK + "=?",
                            new String[]{String.valueOf(item.getUserId()), item.getLink()})) {
                //还没收藏，进行添加
                result = database.insert(MyDatabaseHelper.TABLE_COLLECTIONS, "", getItemValue(item));
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public boolean judgeExits(String tableName, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        cursor = database.query(tableName,
                new String[]{"count(*) as count"},
                selection, selectionArgs, null, null, null, null);
        int count = 0;

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex("count");
            count = cursor.getInt(index);
        }
        Log.d("ppp", "judgeExits: " + count);
        cursor.close();
        if (count != 0) {
            //有，返回true
            return true;
        }
        //没有，返回false
        return false;
    }

    private ContentValues getItemValue(CollectionItem item) {
        ContentValues values = new ContentValues();
        values.put(MyDatabaseHelper.COLUMN_COLLECTION_USER_ID, item.getUserId());
        values.put(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID, item.getGroupId());
        values.put(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_NAME, item.getGroupName());
        values.put(MyDatabaseHelper.COLUMN_TITLE, item.getTitle());
        values.put(MyDatabaseHelper.COLUMN_AUTHOR, item.getAuthor());
        values.put(MyDatabaseHelper.COLUMN_LINK, item.getLink());
        values.put(MyDatabaseHelper.COLUMN_COLLECT_TIME, item.getCollectTime());
        return values;
    }

    //取消收藏的条目(userId,link)
    public List<CollectionItem> deleteCollectionItem(long userId, long groupId, String link) {
        if (groupId == 0) {//从总收藏中删除
            database.delete(MyDatabaseHelper.TABLE_COLLECTIONS,
                    MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_LINK + "=?",
                    new String[]{String.valueOf(userId), link});
        } else {//从分组中删除
            database.delete(MyDatabaseHelper.TABLE_COLLECTIONS,
                    MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=? and " + MyDatabaseHelper.COLUMN_LINK + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(groupId), link});
        }

        //获取新的条目的集合
        return getCollectionItemList(userId, groupId);
    }

    //获取该账号下收藏的所有条目集合
    public List<CollectionItem> getCollectionItemList(Long userId, long groupId) {
        List<CollectionItem> items = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_COLLECTIONS,
                    null,
                    MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(groupId)},
                    null, null, null, null);

            while (cursor.moveToNext()) {
                items.add(cursorToCollectionItem(cursor));
            }
            return items;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    //获取收藏条目的对象
    private CollectionItem cursorToCollectionItem(Cursor cursor) {
        CollectionItem collectionItem = new CollectionItem();

        //userId
        int index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECTION_USER_ID);
        collectionItem.setUserId(cursor.getLong(index));

        //groupId
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID);
        collectionItem.setGroupId(cursor.getLong(index));

        //groupName
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECTION_GROUP_NAME);
        collectionItem.setGroupName(cursor.getString(index));

        //title
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TITLE);
        collectionItem.setTitle(cursor.getString(index));

        //author
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_AUTHOR);
        collectionItem.setAuthor(cursor.getString(index));

        //link
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_LINK);
        collectionItem.setLink(cursor.getString(index));

        //collectTime
        index = cursor.getColumnIndex(MyDatabaseHelper.COLUMN_COLLECT_TIME);
        collectionItem.setCollectTime(cursor.getLong(index));

        return collectionItem;
    }

    //按照时间排序
    public List<CollectionItem> sortByCollectionTime(long userId, long groupId, String orderBy) {
        //select * from table_collections order by collectTime desc;
        //select * from table_collections order by collectTime asc;
        Cursor cursor = null;
        List<CollectionItem> itemList = new ArrayList<>();
        try {
            cursor = database.query(MyDatabaseHelper.TABLE_COLLECTIONS,
                    null,
                    MyDatabaseHelper.COLUMN_COLLECTION_USER_ID + "=? and " + MyDatabaseHelper.COLUMN_COLLECTION_GROUP_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(groupId)},
                    null, null, MyDatabaseHelper.COLUMN_COLLECT_TIME + " " + orderBy, null);

            while (cursor.moveToNext()) {
                itemList.add(cursorToCollectionItem(cursor));
            }
            return itemList;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
