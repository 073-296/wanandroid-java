package com.example.myproject.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myproject.R;
import com.example.myproject.databinding.ActivityChangeAccountBinding;
import com.example.myproject.entity.db.User;
import com.example.myproject.repo.MyRepository;

public class ChangeAccountActivity extends AppCompatActivity {
    private ActivityChangeAccountBinding binding;
    private MyRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangeAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        long userId = intent.getLongExtra("userId", -1L);
        String username = intent.getStringExtra("username");
        String password = intent.getStringExtra("password");
        //Log.d("iii", "onCreate: "+userId);
        //Log.d("iii", "onCreate: "+username);
        //Log.d("iii", "onCreate: "+password);

        repo = new MyRepository(ChangeAccountActivity.this);
        repo.open();

        //toolbar 返回箭头：等价于系统返回（取消本次切换，回"用户"页，登录态不变）
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());

        //切换账号(不能登录当前账号的登录)
        binding.changeAccountBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iUsername = binding.usernameEt.getText().toString();
                String iPassword = binding.passwordEt.getText().toString();
                if (iUsername.isEmpty() || iPassword.isEmpty()) {//账号或密码为空
                    Toast.makeText(ChangeAccountActivity.this, "账号和密码不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    if (!iUsername.equals(username)) {//不是当前账号

                        User user = repo.getUserByUsername(iUsername);
                        if (user != null) {//该账号存在
                            String oUsername = user.getUsername();
                            String oPassword = user.getPassword();
                            Log.d("aaa", "onClick: "+oUsername+"and"+oPassword);
                            //判断密码是否一致
                            if (iUsername.equals(oUsername) && oPassword.equals(iPassword)) {
                                //一致
                                long oUserId = user.getId();
                                saveAndTurn(oUserId, oUsername, oPassword);
                            } else {
                                //密码有误
                                Toast.makeText(ChangeAccountActivity.this, "账号或密码错误(密码有误)", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //账号不存在
                            Toast.makeText(ChangeAccountActivity.this, "账号或密码错误(账号不存在)", Toast.LENGTH_SHORT).show();
                        }



                    } else {
                        //是当前账号
                        Toast.makeText(ChangeAccountActivity.this, "该账号为当前登录账号", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //注册
        binding.registerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String iUsername = binding.usernameEt.getText().toString();
                String iPassword = binding.passwordEt.getText().toString();
                User user = repo.getUserByUsername(iUsername);
                if (user == null) {
                    //用户不存在，进行注册
                    long newUserId = repo.register(iUsername, iPassword);
                    saveAndTurn(newUserId, iUsername, iPassword);
                }
            }
        });
    }

    private void saveAndTurn(long userId, String username, String password) {
        //一开始登录页面的spf中的值要进行改变
        SharedPreferences spf=getSharedPreferences("spRecord",MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putLong("userId", userId);
        editor.putString("username", username);
        editor.putString("password", password);
        editor.apply();
        //跳转到主页
        Intent intent = new Intent(ChangeAccountActivity.this, HomeActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        //清空旧的任务栈：之前那个（属于旧账号的）HomeActivity、切换账号页都会被销毁，
        //否则按返回键会一层层退回旧账号的首页，看起来就是多个重复界面
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}