package com.example.myproject.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myproject.R;
import com.example.myproject.repo.MyRepository;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences spf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //第二次登录直接跳到首页
        spf = getSharedPreferences("spRecord", MODE_PRIVATE);
        long userId = spf.getLong("userId", -1L);
        String username = spf.getString("username", "");
        String password = spf.getString("password", "");
        if (userId != -1 && !username.isEmpty() && !password.isEmpty()) {
            //已经登录过，直接进首页，并且不再初始化登录界面
            gotoHome(userId, username, password);
            return;
        }

        setContentView(R.layout.activity_main);


        //获取编辑文本以及按钮
        EditText usernameEt = findViewById(R.id.username_et);
        EditText passwordEt = findViewById(R.id.password_et);
        Button loginBt = findViewById(R.id.login_bt);
        Button registerBt = findViewById(R.id.register_bt);

        //创建对象
        MyRepository repo = new MyRepository(MainActivity.this);
        //读写数据库
        repo.open();

        //注册
        registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEt.getText().toString();
                String password = passwordEt.getText().toString();
                //保存进用户表中，返回
                long userId = repo.register(username, password);
                Log.e("MainActivity_userId", String.valueOf(userId) );
                if (userId != -1) {
                    //注册成功并进行跳转
                    saveAndTurn(userId, username, password);
                } else {
                    Toast.makeText(MainActivity.this, "该用户名已存在", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //登录
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEt.getText().toString();
                String password = passwordEt.getText().toString();
                long userId = repo.login(username, password);
                if (userId != -1) {
                    saveAndTurn(userId, username, password);
                }else {
                    Toast.makeText(MainActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveAndTurn(long userId, String username, String password) {
        //存储用户信息，下次打开程序直接跳到HomeActivity
        SharedPreferences.Editor editor = spf.edit();
        editor.putLong("userId", userId);

        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();

        gotoHome(userId, username, password);
    }

    /**
     * 跳到首页。
     * 用 NEW_TASK + CLEAR_TASK 把登录页从任务栈里清掉：
     * 1. 登录成功后按返回键不会再退回登录页；
     * 2. 从"切换账号"等页面再次进首页时，栈里不会堆积多个 HomeActivity。
     */
    private void gotoHome(long userId, String username, String password) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        //传递用户信息
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


/*//创建表
        MyDatabaseHelper UserDbHelper = new MyDatabaseHelper(this, "UserMessage.db", null, 2);
        MyDatabaseHelper ArticleDbHelper = new MyDatabaseHelper(this, "ArticleMessage.db", null, 2);
        MyDatabaseHelper GroupDbHelper=new MyDatabaseHelper(this,"Group.db",null,2);
        //创建库
        SQLiteDatabase UserDb = UserDbHelper.getWritableDatabase();
        SQLiteDatabase ArticleDb = ArticleDbHelper.getWritableDatabase();
        SQLiteDatabase GroupDb=GroupDbHelper.getWritableDatabase();


        //登录
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iUsername = usernameEt.getText().toString();
                String iPassword = passwordEt.getText().toString();
                boolean loginSuccess = false;

                //和表中的数据进行比对是否一致
                Cursor cursor = UserDb.query("users_table", null, null, null, null, null, null);
                while (cursor.moveToNext()) {
                    int index = cursor.getColumnIndex("username");
                    String username = cursor.getString(index);
                    index = cursor.getColumnIndex("password");
                    String password = cursor.getString(index);
                    if (username.equals(iUsername) && password.equals(iPassword)) {
                        //关闭光标
                        cursor.close();
                        loginSuccess=true;
                        //保留这次的账号，下次直接登录该账号SharedPreferences
                        SharedPreferences.Editor edit = spf.edit();
                        edit.putString("username", iUsername);
                        edit.putString("password", iPassword);
                        edit.apply();
                        //登录成功，跳转到首页(传递用户名以及密码)
                        Intent intent = new Intent(MainActivity.this, FirstActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        //销毁当前活动
                        finish();
                        //跳出循环
                        break;
                    }
                }
                //关闭光标
                cursor.close();
                //登录失败，进行提示
                if (!loginSuccess) {
                    Toast.makeText(MainActivity.this, "账号或密码错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //注册
        registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iUsername = usernameEt.getText().toString();
                String iPassword = passwordEt.getText().toString();
                boolean usernameExists=false;

                //和表中的用户名比对是否重复
                Cursor cursor = UserDb.query("users_table", null, null, null, null, null, null);

                while (cursor.moveToNext()) {
                    int index = cursor.getColumnIndex("username");
                    String username = cursor.getString(index);
                    if (username.equals(iUsername)) {
                        usernameExists=true;
                        break;
                    }
                }
                //关闭光标
                cursor.close();

                if(usernameExists){
                    //用户名已存在，进行提示
                    Toast.makeText(MainActivity.this, "该用户名已存在", Toast.LENGTH_SHORT).show();
                }else {
                    //添加用户名
                    ContentValues values = new ContentValues();
                    values.put("username", iUsername);
                    values.put("password", iPassword);
                    UserDb.insert("users_table", null, values);
                    //提示注册成功
                    Toast.makeText(MainActivity.this, "注册成功!", Toast.LENGTH_SHORT).show();
                    //登录成功，跳转到首页(传递用户名以及密码)
                    Intent intent = new Intent(MainActivity.this, FirstActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("password", password);
                    startActivity(intent);
                    //销毁当前活动
                    finish();
                }
            }
        });*/