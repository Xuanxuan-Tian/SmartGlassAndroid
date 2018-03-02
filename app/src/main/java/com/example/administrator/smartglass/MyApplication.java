package com.example.administrator.smartglass;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/6/8.
 * 这个类只是为了KeepAlive中Intent mStartActivity = new Intent(MyApplication.getContextObject(), MainActivity.class);执行
 * 重启时生成app的上下文使用，没有这个类软件在重启时会崩溃。
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        //获取Context
        context = getApplicationContext();
    }

    //返回
    public static Context getContextObject(){
        return context;
    }
}
