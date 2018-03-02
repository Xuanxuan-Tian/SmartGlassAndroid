package com.example.administrator.smartglass;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import static com.example.administrator.smartglass.WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA;

public class SettingsActivity extends AppCompatActivity implements View.OnTouchListener {


    private long firstClick;
    private long lastClick;
    // 计算点击的次数
    private int count;

    final String FILE_NAME = "/b.txt";

    TextView tvLeft;
    TextView tvRight;
    WifiManager wifiManager;
    public static Map<Integer, String> scan =  new HashMap<>();
    String user1,ssid1,password1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ImageButton button = (ImageButton) findViewById(R.id.manageButton);
        button.setOnTouchListener(this);

        tvLeft = (TextView) findViewById(R.id.tvLeft);
        tvLeft.setText("双击屏幕设置");
        tvRight = (TextView) findViewById(R.id.tvRight);
       // write(null);
        if(read()==null){
            Toast.makeText(this, "请扫描二维码进行相关设置", Toast.LENGTH_SHORT).show();
        }else{
        resultProcess(read());
            tvRight.setText("   当前设置："+"\n"+"\n"+"   用户名："+scan.get(1) +"\n"+
                    "   WiFi名："+scan.get(2) +"\n"+"   分辨率："+scan.get(4)+"P"+"\n"+"   码    率："+scan.get(5)+"kbps"+"\n"+"   服务器："+scan.get(6) +"\n");}
        System.out.println("chaxun"+scan.get(1));
        System.out.println("chaxun"+scan.get(2));
        System.out.println("chaxun"+scan.get(3));


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
                        // IntentIntegrator integrator = IntentIntegrator.forSupportFragment(SecondFragment.this);
                        //integrator.initiateScan();
                        IntentIntegrator integrator = new IntentIntegrator(SettingsActivity.this);
                        integrator.initiateScan();

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

    //扫描回调结果
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String result = scanResult.getContents();
            if (result != null) {

                write(result);
                resultProcess(result);
                //scan.get(user1);
                wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                WifiAutoConnectManager wifiAutoConnectManager = new WifiAutoConnectManager(wifiManager);
                wifiAutoConnectManager.connect(scan.get(2),scan.get(3), WIFICIPHER_WPA);


                //tvRight.setText("   当前设置："+"\n"+"\n"+"   用户："+scan.get(1) +"\n"+"   WiFi："+scan.get(2) +"\n");
                //WifiAutoConnectManager wifiAutoConnectManager = new WifiAutoConnectManager(wifiManager);
                //wifiAutoConnectManager.connect("sinet","bjtungirc",WIFICIPHER_WPA);
                tvRight.setText("   当前设置："+"\n"+"\n"+"   用户名："+scan.get(1) +"\n"+
                        "   WiFi名："+scan.get(2) +"\n"+"   分辨率："+scan.get(4)+"P"+"\n"+"   码    率："+scan.get(5)+"kbps"+"\n"+"   服务器："+scan.get(6) +"\n");


                Toast.makeText(this,"设置成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "设置失败，请重新扫描二维码", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void write(String content) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
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

    private String read() {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //获得SD卡对应的存储目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                //获取指定文件对应的输入流
                FileInputStream fis = new FileInputStream(sdCardDir.getCanonicalPath() + FILE_NAME);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder("");
                String line = null;
                //循环读取文件内容
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return sb.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void resultProcess(String result) {
        String str=result;
        int i=1;
        int b=0;
        int a = str.indexOf(" ");
        while (a!=-1){
            scan.put(i,str.substring(b,a));
            b=a+1;
            i++;
            a = str.indexOf(" ",a+1);
        }
        int last = str.lastIndexOf(" ");
        int length = str.length();
        scan.put(i,str.substring(last+1,length));

       /* int first = str.indexOf(" ");
        int second = str.indexOf(" ",first+1);
        int third = str.indexOf(" ",second+1);
        int fourth = str.indexOf(" ",third+1);
        //int fifth = str.indexOf(" ",fourth+1);
        //int last = str.lastIndexOf(" ");
        int length = str.length();
        String user = str.substring(0,first);
        String ssid = str.substring(first+1,second);
        String password = str.substring(second+1,third);
        String resolution = str.substring(third+1,fourth);
        String ipAdress = str.substring(fourth+1,length);
        scan.put(1, user);
        scan.put(2,ssid);
        scan.put(3,password);
        scan.put(4,resolution);
        scan.put(5,ipAdress);
        System.out.println("chaxun1"+user);
        System.out.println("chaxun1"+ssid);
        System.out.println("chaxun1"+password);*/
    }





    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Settings Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }



}
