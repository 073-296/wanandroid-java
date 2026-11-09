package com.example.myproject.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast toast;
    public static Toast getInstance(Context context){
        if (toast==null){
            toast=Toast.makeText(context,"",Toast.LENGTH_SHORT);
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
