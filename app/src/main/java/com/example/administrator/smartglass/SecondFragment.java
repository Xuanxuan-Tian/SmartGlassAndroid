package com.example.administrator.smartglass;

/**
 * Created by Administrator on 2017/6/22.
 */

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.os.EnvironmentCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
//import com.uuzuche.lib_zxing.activity.CaptureActivity;
//import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import static android.content.Context.WIFI_SERVICE;
import static com.example.administrator.smartglass.WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA;

public class SecondFragment extends Fragment implements View.OnTouchListener {

    public static final int REQUEST_CODE = 111;

    public static SecondFragment newInstance() {
        return new SecondFragment();
    }
    //Context out;
    final String FILE_NAME = "/a.txt";

    private long firstClick;
    private long lastClick;
    // 计算点击的次数
    private int count;

    //WifiManager wifiManager;
    //WifiManager wifiManager = (WifiManager)Context.getSystemService(Service.WIFI_SERVICE);
    //WifiManager wifiManager= (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        return inflater.inflate(R.layout.fragment_second, container, false);



    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        ImageButton button = (ImageButton) getActivity().findViewById(R.id.manageButton);
        button.setOnTouchListener(this);


        /*button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(),CaptureActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
                startActivityForResult(intent,REQUEST_CODE);//第二个界面会返回给第一个界面值
            }
        });*/
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult =IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (scanResult !=null){
            String result=scanResult.getContents();
            if(result!=null) {
                write(result);
                //WifiAutoConnectManager wifiAutoConnectManager = new WifiAutoConnectManager(wifiManager);
                //wifiAutoConnectManager.connect("sinet","bjtungirc",WIFICIPHER_WPA);


                Toast.makeText(getActivity(), "用户名：" + result + " 设置成功", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "设置失败，请重新扫描二维码", Toast.LENGTH_SHORT).show();
            }
        }
    }




   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * 处理二维码扫描结果
         */
     /*   if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    write(result);
                    Toast.makeText(getActivity(),"用户名："+result+" 设置成功",Toast.LENGTH_SHORT).show();
                    //用默认浏览器打开扫描得到的地址
                    //Intent intent = new Intent();
                    //intent.setAction("android.intent.action.VIEW");
                    //tv.setText(result);
                    //Uri content_url = Uri.parse(result.toString());
                    //intent.setData(content_url);
                    //startActivity(intent);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                }
            }
        }
    }*/


    private void write(String content){
        try {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获取SD卡的目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                File targetFile = new File(sdCardDir.getCanonicalPath() + FILE_NAME);
                //以指定文件创建RandomAccessFile对象
                targetFile.delete();
                targetFile.createNewFile();
                RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
                //将文件记录指针移动到最后
                //输出文件内容
                raf.write(content.getBytes());
                raf.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                       //Intent intent = new Intent(getActivity(),ScanActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
                       // startActivityForResult(intent,REQUEST_CODE);//第二个界面会返回给第一个界面值
                       //IntentIntegrator integrator = IntentIntegrator.forSupportFragment(SecondFragment.this);
                       // integrator.initiateScan();
                        Intent intent = new Intent(getActivity(),SettingsActivity.class);//Fragment中使用intent，第一个参数应该是getActivity
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


}

