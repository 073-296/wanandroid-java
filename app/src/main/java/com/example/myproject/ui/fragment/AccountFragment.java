package com.example.myproject.ui.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.myproject.R;
import com.example.myproject.databinding.FragmentAccountBinding;
import com.example.myproject.repo.MyRepository;
import com.example.myproject.ui.activity.ChangeAccountActivity;
import com.example.myproject.ui.activity.MainActivity;
import com.example.myproject.util.ToastUtil;


public class AccountFragment extends Fragment {
    FragmentAccountBinding binding;
    private long userId;
    private String username;
    private String password;
    private MyRepository repo;
    private SharedPreferences spf;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(getLayoutInflater());

        repo = new MyRepository(requireActivity());
        repo.open();

        spf = requireActivity().getSharedPreferences("spRecord", MODE_PRIVATE);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            userId = bundle.getLong("userId");
            username = bundle.getString("username");
            password = bundle.getString("password");
        }

        //欢迎您：用户名
        binding.accountWelcomeText.setText(getString(R.string.welcome, username));

        //更换账号密码
        binding.changePasswordBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = new EditText(requireActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder
                        .setTitle("更改密码")
                        .setMessage("请输入你的新密码")
                        .setView(input)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newPassword = input.getText().toString();
                                if (newPassword.isEmpty()) {
                                    ToastUtil.show(requireActivity(), "密码不可为空");
                                } else {
                                    repo.updatePassword(userId, newPassword);
                                }
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

            }
        });

        //切换账号
        binding.changeAccountBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新的Activity
                //注册，切换账号
                Intent intent = new Intent(requireActivity(), ChangeAccountActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("username", username);
                intent.putExtra("password", password);
                startActivity(intent);
            }
        });

        //注销账号
        binding.logoutAccountBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder
                        .setTitle("警告")
                        .setMessage("该过程不可逆，是否删除？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //删除该用户信息
                                repo.deleteUser(userId);
                                //修改SharedPreferences
                                SharedPreferences.Editor editor = spf.edit();
                                editor.putLong("userId", -1L);
                                editor.putString("username", "");
                                editor.putString("password", "");
                                editor.apply();
                                //返回登录界面
                                backToLogin();
                            }
                        })
                        .setNegativeButton("取消", (DialogInterface dialog, int which) -> {
                                }
                        )
                        .show();
            }
        });

        //退出账号
        binding.exitAccountBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
                builder
                        .setTitle("提示")
                        .setMessage("是否退出账户？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = spf.edit();
                                editor.putLong("userId", -1L);
                                editor.putString("username", "");
                                editor.putString("password", "");
                                editor.apply();
                                backToLogin();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
        return binding.getRoot();
    }

    /**
     * 注销 / 退出账号后回到登录页。
     * 一定要把整个任务栈清掉，否则按返回键还能退回已经"退出"的首页，
     * 而且首页里还留着上一个账号的数据。
     */
    private void backToLogin() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}