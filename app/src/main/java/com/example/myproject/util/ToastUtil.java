package com.example.myproject.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast;
    private static Toast getInstance(Context context){
        if (toast==null){
            //静态 Toast 会被类一直持有，必须用 ApplicationContext 创建，
            //否则会长时间引用第一次调用时的 Activity，造成泄漏
            toast=Toast.makeText(context.getApplicationContext(),"",Toast.LENGTH_SHORT);
        }
        return toast;
    }
    public static void show(Context context,String content){
        Toast t=getInstance(context);
        t.setText(content);
        t.show();
    }

    public static void setDurationAndShow(Context context,String content,int duration){
        Toast t=getInstance(context);
        t.setText(content);
        t.setDuration(duration);
        t.show();
    }
}
