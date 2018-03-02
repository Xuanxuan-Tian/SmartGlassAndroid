package com.example.administrator.smartglass;

/**
 * Created by Administrator on 2017/6/22.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

//import com.uuzuche.lib_zxing.activity.CaptureActivity;


public class FirstFragment extends Fragment implements View.OnTouchListener {

    private long firstClick;
    private long lastClick;
    // 计算点击的次数
    private int count;

    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState ) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

       ImageButton button = (ImageButton) getActivity().findViewById(R.id.liveButton);
        button.setOnTouchListener(this);
        /*    button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),NewPeerVideoActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
                startActivity(intent);
            }
        });*/

        IntentFilter filter = new IntentFilter();
        filter.addAction("reStartNewPeerVideoActivity");
        getActivity().registerReceiver(mFinishReceiver, filter);

    }

    @Override
    public boolean onTouch(View arg0, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 如果第二次点击 距离第一次点击时间过长 那么将第二次点击看为第一次点击
                if (firstClick != 0 && System.currentTimeMillis() - firstClick > 300) {
                    count = 0;
                }
                count++;
                if (count == 1) {
                    firstClick = System.currentTimeMillis();
                } else if (count == 2) {
                    lastClick = System.currentTimeMillis();
                    // 两次点击小于300ms 也就是连续点击
                    if (lastClick - firstClick < 300) {// 判断是否是执行了双击事件
                        System.out.println(">>>>>>>>执行了双击事件");
                        Intent intent = new Intent(getActivity(),NewPeerVideoActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
                        startActivity(intent);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("reStartNewPeerVideoActivity".equals(intent.getAction())) {
                // Log.e("#########", "I am " + getLocalClassName()
                //   + ",now finishing myself...");
                try{
                    Thread.currentThread().sleep(700);
                }catch (Exception e){}
                try {
                    intent = new Intent(getActivity(),NewPeerVideoActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
                    startActivity(intent);
                    System.out.println("shoudaole");
                } catch (Exception e) {
                }
            }
        }
    };




}

