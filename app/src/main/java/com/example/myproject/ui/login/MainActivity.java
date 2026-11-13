package com.example.myproject.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myproject.R;
import com.example.myproject.data.repository.MyRepository;
import com.example.myproject.ui.home.HomeActivity;
import com.example.myproject.util.ToastUtil;

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
        registerBt.setOnClickListener(v -> {
            String username1 = usernameEt.getText().toString();
            String password1 = passwordEt.getText().toString();
            //保存进用户表中，返回
            long userId1 = repo.register(username1, password1);
            if (userId1 != -1) {
                //注册成功并进行跳转
                saveAndTurn(userId1, username1, password1);
            } else {
                ToastUtil.show(MainActivity.this, "该用户名已存在");
            }
        });

        //登录
        loginBt.setOnClickListener(v -> {
            String username2 = usernameEt.getText().toString();
            String password2 = passwordEt.getText().toString();
            long userId2 = repo.login(username2, password2);
            if (userId2 != -1) {
                saveAndTurn(userId2, username2, password2);
            }else {
                ToastUtil.show(MainActivity.this, "账号或密码错误");
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
