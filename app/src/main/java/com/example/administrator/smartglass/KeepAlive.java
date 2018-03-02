package com.example.administrator.smartglass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import static com.example.administrator.smartglass.Ping.ping;

/**
 * Created by Administrator on 2017/6/7.
 * 这是监听ping包的主进程
 */
class KeepAlive extends Thread {
    boolean running = true;
    boolean unsuccess=true;
    public long lastsentime;

    int count=1;
    Context context;
    KeepAlive(long lastsentime) {
        this.lastsentime = lastsentime;
    }
    void setLastsentime(long lastsentime) {
        this.lastsentime = lastsentime;
    }
    public void run() {
        while (running) {
           // System.out.println("panduan"+count++);
                long currenttime = System.currentTimeMillis();
                if (currenttime - lastsentime >12*1000) {
                    System.out.println("chongqiAPPduanwang");
                    running = false;

                    while(unsuccess){
                        boolean success=ping();
                        if (success){
                            unsuccess=!unsuccess;
                        }
                    }

                    //NewPeerVideoActivity.getKurentoRoomAPIInstance().


                    /*在重新启动之前杀死之前打开的MainActivity和PeerVideoActivity*/
                    try {
                        System.out.println("fasongguangbo");
                        MyApplication.getContextObject().sendBroadcast(new Intent("finish"));
                        System.out.println("fasongguangbo");
                    } catch (Exception e) {
                    }
                    //执行重启任务，目前这种重启的缺点是用户会重新看到加入房间的界面，不够友好，但是功能上是实现了断网检测，来网重连
                    /*System.out.println("chongqiAPPchongqi");
                    Intent mStartActivity = new Intent(MyApplication.getContextObject(),NewPeerVideoActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(MyApplication.getContextObject(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)MyApplication.getContextObject().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);*/
                    return;
                }

            }
        }


}



