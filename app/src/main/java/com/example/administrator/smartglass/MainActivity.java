package com.example.administrator.smartglass;

/**
 * Created by Administrator on 2017/6/22.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.Map;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
               // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        final Map<Integer, Fragment> data = new TreeMap<>();
        data.put(0, FirstFragment.newInstance());
        data.put(1, SecondFragment.newInstance());

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new FragmentStatePagerAdapter(
                getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return data.get(position);
            }

            @Override
            public int getCount() {
                return data.size();
            }
        });

        //IntentFilter filter = new IntentFilter();
       // filter.addAction("reStartNewPeerVideoActivity");
       // registerReceiver(mFinishReceiver, filter);

       /* registerReceiver(mFinishReceiver, filter);*/
    }

    /*private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("finish".equals(intent.getAction())) {
                Log.e("#########", "I am " + getLocalClassName()
                        + ",now finishing myself...");
                try {
                    finish();
                } catch (Exception e) {
                }
            }
        }
    };*/


    /*private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("reStartNewPeerVideoActivity".equals(intent.getAction())) {
                // Log.e("#########", "I am " + getLocalClassName()
                //   + ",now finishing myself...");
                try{
                    Thread.currentThread().sleep(15000);
                }catch (Exception e){}
                try {
                    //intent = new Intent(MainActivity.this,NewPeerVideoActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
                    //startActivity(intent);
                    System.out.println("shoudaole");
                } catch (Exception e) {
                }
            }
        }
    };*/

}

