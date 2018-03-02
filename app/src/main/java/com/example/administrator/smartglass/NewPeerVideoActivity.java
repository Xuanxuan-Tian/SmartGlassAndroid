package com.example.administrator.smartglass;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.smartglass.util.Constants;

//import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import fi.vtt.nubomedia.kurentoroomclientandroid.KurentoRoomAPI;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomError;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomListener;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomNotification;
import fi.vtt.nubomedia.kurentoroomclientandroid.RoomResponse;
import fi.vtt.nubomedia.utilitiesandroid.LooperExecutor;
import fi.vtt.nubomedia.webrtcpeerandroid.NBMMediaConfiguration;
import fi.vtt.nubomedia.webrtcpeerandroid.NBMPeerConnection;
import fi.vtt.nubomedia.webrtcpeerandroid.NBMWebRTCPeer;

import static org.webrtc.SessionDescription.Type.OFFER;

//import net.sf.json.JSONObject;

/**
 * Created by Administrator on 2017/6/23.
 */

public class NewPeerVideoActivity extends Activity implements  NBMWebRTCPeer.Observer,RoomListener{
    private static final String TAG = "NewPeerVideoActivity";
    private static KurentoRoomAPI kurentoRoomAPI;
    private LooperExecutor executor;
    private int roomId=0;
    private String userName,roomName,wsUri;
    public static Map<String, Boolean> userPublishList = new HashMap<>();
    int zhixing=0;

    public SurfaceViewRenderer localView;
    public TextView mCallStatus;
    private TextView mTextMessageTV;
    public Map<Integer, String> videoRequestUserMapping;
    public NBMWebRTCPeer nbmWebRTCPeer;
    public CallState callState;
    public int publishVideoRequestId;
    public Handler mHandler;
    public Handler m1Handler;


    private boolean backPressed = false;
    private Thread  backPressedThread = null;

    private long lastTotalTxBytes = 0;//显示网速
    private long lastTimeStamp = 0;
    Handler m2Handler;
    private TextView wangsu;
    private TextView yonghuming;

    private SVDraw  mSVDraw = null;//画图

    final String FILE_NAME = "/b.txt";

    boolean keepalivestart=true;
    KeepAlive  ping1;

    public Map<Integer, String> scan =  new HashMap<>();
    String user1,ssid1,password1;

    String[] settings;

    int width,height,bandWidth;

    boolean reStart = false;
    boolean startDetection = true;

    public int getX,getY;

    private enum CallState {
        IDLE, PUBLISHING, PUBLISHED, WAITING_REMOTE_USER, RECEIVING_REMOTE_USER,JOINROOM
    }

    private String publicDescrition;

    private boolean initB = false;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction("finish");
        registerReceiver(mFinishReceiver, filter);

        setContentView(R.layout.activity_peervideo);

        executor = new LooperExecutor();
        executor.requestStart();
        String ip= getIpAdress();

        wsUri = "wss://"+ip+":8443/room";
        kurentoRoomAPI = new KurentoRoomAPI(executor, wsUri, this);

        /*证书vv
        * 从assets文件夹中的kurento_room_base64.cer加载证书*/
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getAssets().open("kurento_room_base64.cer"));
            Certificate ca = cf.generateCertificate(caInput);
            kurentoRoomAPI.addTrustedCertificate("ca", ca);
        } catch (CertificateException |IOException e) {
            e.printStackTrace();
        }
        kurentoRoomAPI.useSelfSignedCertificate(true);

        /*连接WebSocket*/
        if(!kurentoRoomAPI.isWebSocketConnected()){
            kurentoRoomAPI.connectWebSocket();
            System.out.println( "Connecting to room at " + wsUri);
            // System.out.println( kurentoRoomAPI.isWebSocketConnected());

        }
        System.out.println("zhixingonstart"+zhixing++);
        System.out.println(kurentoRoomAPI.isWebSocketConnected()+"zhixing2");

        System.out.println( kurentoRoomAPI.isWebSocketConnected());

////////////////////////////////////////////////////////////
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
        localView = (SurfaceViewRenderer) findViewById(com.example.administrator.smartglass.R.id.gl_surface_local);
        this.mCallStatus = (TextView) findViewById(com.example.administrator.smartglass.R.id.call_status);//最上方的呼叫状态控件的声明
        this.mTextMessageTV = (TextView) findViewById(com.example.administrator.smartglass.R.id.message_textview1);//这里是用来显示接收的或者自己发送的消息!!!!添加
        this.mTextMessageTV.setText("");
        wangsu = (TextView)findViewById(com.example.administrator.smartglass.R.id.wangsu);
        mSVDraw = (com.example.administrator.smartglass.SVDraw)findViewById(com.example.administrator.smartglass.R.id.mDraw);//画图
        mSVDraw.setVisibility(View.VISIBLE);

        this.yonghuming = (TextView) findViewById(com.example.administrator.smartglass.R.id.yonghuming);




        callState = CallState.IDLE;//枚举类型
        kurentoRoomAPI.addObserver(this);

        /*显示用户名*/
        m1Handler = new Handler(){
            public void handleMessage(android.os.Message msg){
                switch (msg.what) {
                    // case 0 : textView.setText("登出失败");     break;
                    case 1 :
                      yonghuming.setText(userName);
                }
            }
        };

        /*显示网速*/
        m2Handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    // case 0 : textView.setText("登出失败");     break;
                    case 100:
                        wangsu.setText(msg.obj.toString());
                }
            }
        };



    }

    @Override
    public void onStart(){
        super.onStart();

        if(initB){
            return;
        }
        EglBase rootEglBase = EglBase.create();
        localView.init(rootEglBase.getEglBaseContext(), null);
        localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);//设置预览屏幕适应方式

        setResolution(getResolution());
        setBandWidth(getBandWidth());
        NBMMediaConfiguration peerConnectionParameters = new NBMMediaConfiguration(
                NBMMediaConfiguration.NBMRendererType.OPENGLES,
                NBMMediaConfiguration.NBMAudioCodec.OPUS, 15,
                NBMMediaConfiguration.NBMVideoCodec.VP8, 15,

                new NBMMediaConfiguration.NBMVideoFormat(width, height, PixelFormat.RGB_888, 22, bandWidth),
                NBMMediaConfiguration.NBMCameraPosition.BACK);
        videoRequestUserMapping = new HashMap<>();

        nbmWebRTCPeer = new NBMWebRTCPeer(peerConnectionParameters, this, localView, this);
        nbmWebRTCPeer.initialize();
        callState = CallState.PUBLISHING;
        mCallStatus.setText(" 初始化...");//"Publishing..."

        /*显示网速*/
        lastTotalTxBytes = getTotalTxBytes();
        lastTimeStamp = System.currentTimeMillis();
        new Timer().schedule(task, 1000, 2000);

    }

    @Override
    protected void onStop() {
        //endCall();
        super.onStop();
    }

    private void endCall() {
        callState = CallState.IDLE;
        try {
            if (nbmWebRTCPeer != null) {
                nbmWebRTCPeer.close();
                nbmWebRTCPeer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
       // nbmWebRTCPeer.stopLocalMedia();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!initB) {
            nbmWebRTCPeer.startLocalMedia();
        }
        initB = true;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
       /* if (kurentoRoomAPI.isWebSocketConnected()) {
            kurentoRoomAPI.sendLeaveRoom(roomId);
            kurentoRoomAPI.disconnectWebSocket();
        }
        executor.requestStop();*/
        super.onDestroy();
        if(reStart){
        getApplicationContext().sendBroadcast(new Intent("reStartNewPeerVideoActivity"));}
    }



    public void onBackPressed(){
        /*if (!this.backPressed){
            this.backPressed = true;
            Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
            this.backPressedThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        backPressed = false;
                    } catch (InterruptedException e){ Log.d("VCA-oBP","Successfully interrupted"); }
                }
            });
            this.backPressedThread.start();
        }
        // If button pressed the second time then call super back pressed
        // (eventually calls onDestroy)
        else {
            if (this.backPressedThread != null)
               // ping1.running=false;
                mSVDraw.clearDraw();
            if (kurentoRoomAPI.isWebSocketConnected()) {
                kurentoRoomAPI.sendLeaveRoom(roomId);
                kurentoRoomAPI.disconnectWebSocket();
            }
            executor.requestStop();
                this.backPressedThread.interrupt();
            super.onBackPressed();
        }*/
        /*if (ping1.isAlive()){
        ping1.running=false;}*/
        //endCall();
        nbmWebRTCPeer.stopLocalMedia();
        mSVDraw.clearDraw();
        if (kurentoRoomAPI.isWebSocketConnected()) {
            task.cancel();
            ping.cancel();
            ping1.running=false;
            kurentoRoomAPI.sendLeaveRoom(roomId);
            kurentoRoomAPI.disconnectWebSocket();
        }

        try{
            Thread.currentThread().sleep(300);
        }catch (Exception e){}

        finish();

    }


    public void hangup(View view) {
        /*if(ping1.isAlive()){
        ping1.running=false;}*/
        mSVDraw.clearDraw();
        if (kurentoRoomAPI.isWebSocketConnected()) {
            task.cancel();
            ping.cancel();
            ping1.running=false;
            kurentoRoomAPI.sendLeaveRoom(roomId);
            kurentoRoomAPI.disconnectWebSocket();
        }

        try{
            Thread.currentThread().sleep(300);
        }catch (Exception e){}

        finish();
    }

    public void onInitialize() {
        nbmWebRTCPeer.generateOffer("local", true);
    }

    @Override
    public void onLocalSdpOfferGenerated(final SessionDescription sessionDescriptiona1, final NBMPeerConnection nbmPeerConnection){
            try{
                Thread.currentThread().sleep(3200);
            }catch (Exception e){}
        if(startDetection){
        kurentoRoomAPI.sendTestPing(true,Integer.parseInt(userName.substring(userName.length()-1)),486586386);//Integer.parseInt(userName.substring(userName.length()-1))
        try{
            Thread.currentThread().sleep(300);
        }catch (Exception e){}
        new Timer().scheduleAtFixedRate(ping,1000,3000);
            startDetection=false;}
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED){
                    publishVideoRequestId = ++Constants.id;
                     kurentoRoomAPI.sendPublishVideo(sessionDescriptiona1.description, false, publishVideoRequestId);//发送视频

           // publishVideoRequestId = ++Constants.id;
           // kurentoRoomAPI.sendPublishVideo(sessionDescriptiona1.description, false, publishVideoRequestId);//发送视频
            System.out.println(sessionDescriptiona1.description + "txsdp");
            //mCallStatus.setText(" 用户："+userName);
            //System.out.println("sendPublishVideo" + jishu++);
        }else{
                publishVideoRequestId = ++Constants.id;
                    String username = nbmPeerConnection.getConnectionId();//"admin";//nbmPeerConnection.getConnectionId();//nbmConnection是一个NBMPeerConnection实例化的对象。
                    System.out.println("tiam"+username);
                    publicDescrition = sessionDescriptiona1.description;
                    videoRequestUserMapping.put(publishVideoRequestId, username);//一把钥匙一把锁，这里publishVideoRequestId相当于key,username相当于value
                    kurentoRoomAPI.sendReceiveVideoFrom(username, "webcam", sessionDescriptiona1.description, publishVideoRequestId);

            //publishVideoRequestId = ++Constants.id;
            //videoRequestUserMapping.put(publishVideoRequestId, "Glass05");
            //kurentoRoomAPI.sendReceiveVideoFrom("Glass05", "webcam", sessionDescriptiona1.description, publishVideoRequestId);

        }
    }

    public void onLocalSdpAnswerGenerated(SessionDescription sessionDescription, NBMPeerConnection nbmPeerConnection) {
        //System.out.println("txxonLocalSdpAnswerGenerated" + jishu++);

        videoRequestUserMapping.put(publishVideoRequestId, "Glass2");//一把钥匙一把锁，这里publishVideoRequestId相当于key,username相当于value
        kurentoRoomAPI.sendReceiveVideoFrom("Glass2", "webcam", sessionDescription.description, publishVideoRequestId);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate, NBMPeerConnection nbmPeerConnection) {
        int sendIceCandidateRequestId = ++Constants.id;
        System.out.println("jintian517");
        if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {

            kurentoRoomAPI.sendOnIceCandidate(userName, iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
            Log.i(TAG, iceCandidate.sdp);
            Log.i(TAG, iceCandidate.sdpMid);
            Log.i(TAG, Integer.toString(iceCandidate.sdpMLineIndex));
            Log.i(TAG, Integer.toString(sendIceCandidateRequestId));
            Log.i(TAG, "tianxuan");

        } else {
            kurentoRoomAPI.sendOnIceCandidate(nbmPeerConnection.getConnectionId(), iceCandidate.sdp,
                    iceCandidate.sdpMid, Integer.toString(iceCandidate.sdpMLineIndex), sendIceCandidateRequestId);
            Log.i(TAG, iceCandidate.sdp);
            Log.i(TAG, iceCandidate.sdpMid);
            Log.i(TAG, Integer.toString(iceCandidate.sdpMLineIndex));
            Log.i(TAG, Integer.toString(sendIceCandidateRequestId));
            Log.i(TAG, "tianxuan1");
        }
        //System.out.println("txxonIceCandidate" + jishu++);
    }

    @Override
    public void onIceStatusChanged(PeerConnection.IceConnectionState iceConnectionState, NBMPeerConnection nbmPeerConnection) {

    }

    @Override
    public void onRemoteStreamAdded(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamAdded");


        ///publishVideoRequestId = ++Constants.id;
        //videoRequestUserMapping.put(publishVideoRequestId, "Glass05");
        //kurentoRoomAPI.sendReceiveVideoFrom("Glass05", "webcam",  publicDescrition, publishVideoRequestId);
        System.out.println("wansui"+publicDescrition);
       nbmWebRTCPeer.setActiveMasterStream(mediaStream);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallStatus.setText("");
            }
        });
       // System.out.println("txxonRemoteStreamAdded" + jishu++);
    }

    @Override
    public void onRemoteStreamRemoved(MediaStream mediaStream, NBMPeerConnection nbmPeerConnection) {
        Log.i(TAG, "onRemoteStreamRemoved");
    }

    @Override
    public void onPeerConnectionError(String s) {
        Log.e(TAG, "onPeerConnectionError:" + s);
    }

    @Override
    public void onDataChannel(DataChannel dataChannel, NBMPeerConnection connection) {
        Log.i(TAG, "[datachannel] Peer opened data channel");
    }

    @Override
    public void onBufferedAmountChange(long l, NBMPeerConnection connection, DataChannel channel) {

    }

    public void sendHelloMessage(DataChannel channel) {
        byte[] rawMessage = "Hello Peer!".getBytes(Charset.forName("UTF-8"));
        ByteBuffer directData = ByteBuffer.allocateDirect(rawMessage.length);
        directData.put(rawMessage);
        directData.flip();
        DataChannel.Buffer data = new DataChannel.Buffer(directData, false);
        channel.send(data);
        //System.out.println("txxsendHelloMessage" + jishu++);
    }

    @Override
    public void onStateChange(NBMPeerConnection connection, DataChannel channel) {
        Log.i(TAG, "[datachannel] DataChannel onStateChange: " + channel.state());
        if (channel.state() == DataChannel.State.OPEN) {
            sendHelloMessage(channel);
            Log.i(TAG, "[datachannel] Datachannel open, sending first hello");
        }
        //System.out.println("txxonStateChange()" + jishu++);
    }

    @Override
    public void onMessage(DataChannel.Buffer buffer, NBMPeerConnection connection, DataChannel channel) {
        Log.i(TAG, "[datachannel] Message received: " + buffer.toString());
        sendHelloMessage(channel);
       // System.out.println("txxonMessage()" + jishu++);
    }

    private Runnable offerWhenReady = new Runnable() {
        @Override
        public void run() {
            // Generate offers to receive video from all peers in the room
            for (Map.Entry<String, Boolean> entry : userPublishList.entrySet()) {
                if (entry.getValue()) {
                    GenerateOfferForRemote(entry.getKey());
                    Log.i(TAG, "I'm " + userName + " DERP: Generating offer for peer " + entry.getKey());
                    // Set value to false so that if this function is called again we won't
                    // generate another offer for this user
                    entry.setValue(false);
                }
            }
        }

    };
    private void GenerateOfferForRemote(String remote_name) {
        nbmWebRTCPeer.generateOffer(remote_name, false);
        callState = CallState.WAITING_REMOTE_USER;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallStatus.setText("");//com.example.administrator.smartglass.R.string.waiting_remote_stream
            }
        });
    }

    @Override
    public void onRoomResponse(RoomResponse response){
        if (response.getMethod()==KurentoRoomAPI.Method.JOIN_ROOM) {
            userPublishList = new HashMap<>(response.getUsers());
        }

        Log.d(TAG, "OnRoomResponse:" + response);
        int requestId = response.getId();
        if (requestId == publishVideoRequestId) {

            SessionDescription sd = new SessionDescription(SessionDescription.Type.ANSWER,
                    response.getValue("sdpAnswer").get(0));

            // Check if we are waiting for publication of our own video
            if (callState == CallState.PUBLISHING) {
                callState = CallState.PUBLISHED;
                nbmWebRTCPeer.processAnswer(sd, "local");
                mHandler.postDelayed(offerWhenReady, 2000);

                // Check if we are waiting for the video publication of the other peer
            } else if (callState == CallState.WAITING_REMOTE_USER) {
                //String user_name = Integer.toString(publishVideoRequestId);
                callState = CallState.RECEIVING_REMOTE_USER;
                String connectionId = videoRequestUserMapping.get(publishVideoRequestId);
                nbmWebRTCPeer.processAnswer(sd, connectionId);
            }
        }

        long lastSendTime;
        lastSendTime = System.currentTimeMillis();
        if(keepalivestart){
            ping1= new KeepAlive(lastSendTime);
            ping1.start();
            keepalivestart=false;
        }
        if(requestId==386486586){//Integer.parseInt(userName.substring(userName.length()-1))
            ping1.setLastsentime(lastSendTime);
            System.out.println(TAG+"txxOnRoomResponse()" +response);
        }
        System.out.println("bjtubjtu"+"Galss10".substring(5,6));

    }

    public void onRoomError(RoomError error){
        if(error.getCode() == 104) {
        }
    }


    @Override
    public void onRoomNotification(RoomNotification notification) {

        Log.i(TAG, "OnRoomNotification (state=" + callState.toString() + "):" + notification);
        Map<String, Object> map = notification.getParams();

        Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, Object> result = it.next();
            String key = result.getKey();
            Object value = result.getValue();
            System.out.print("jiashuo"+key+"("+value+")");}

        if (notification.getMethod().equals(RoomListener.METHOD_ICE_CANDIDATE)) {
            String sdpMid = map.get("sdpMid").toString();
            int sdpMLineIndex = Integer.valueOf(map.get("sdpMLineIndex").toString());
            String sdp = map.get("candidate").toString();
            IceCandidate ic = new IceCandidate(sdpMid, sdpMLineIndex, sdp);

            if (callState == CallState.PUBLISHING || callState == CallState.PUBLISHED) {
                nbmWebRTCPeer.addRemoteIceCandidate(ic, "local");

            } else {
                nbmWebRTCPeer.addRemoteIceCandidate(ic, notification.getParam("endpointName").toString());
            }
        }

        // Somebody in the room published their video
        else if (notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_PUBLISHED)) {
            mHandler.postDelayed(offerWhenReady, 2000);
        } else if (notification.getMethod().equals(RoomListener.METHOD_SEND_MESSAGE)) {
            final String user = map.get("user").toString();
            final String message = map.get("message").toString();

            //mHandler=new Handler();
            NewPeerVideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextMessageTV.setText(getString(com.example.administrator.smartglass.R.string.room_text_message, user, message));
                    mHandler.removeCallbacks(clearMessageView);
                    mHandler.postDelayed(clearMessageView, 3000);


                }
            });

            /*配合网页端实现标记*/

            /*mSVDraw.drawLine(187, 149, 274, 330);
            try
            {
                Thread.currentThread().sleep(7000);//毫秒
            }
            catch(Exception e){}
            mSVDraw.clearDraw();*/


           // kurentoRoomAPI.sendReceiveVideoFrom("Glass05", "webcam",  publicDescrition, publishVideoRequestId);
           // publishVideoRequestId = ++Constants.id;
           // videoRequestUserMapping.put(publishVideoRequestId, "Glass05");
            //kurentoRoomAPI.sendReceiveVideoFrom("Glass05", "webcam",  publicDescrition, publishVideoRequestId);



        }else if(notification.getMethod().equals(RoomListener.METHOD_MARK)){
            //if(!initB)
                //return;

            getDisplayInformation();

            System.out.println("nihao"+getX+""+getY);
           // final JSONObject message = map.get("markMessage")
           //final JSONObject markmessage= JSONObject.fromObject(map.get("markMessage"));
            final String left = map.get("left").toString();
            final String top = map.get("top").toString();
            final String right = map.get("right").toString();
            final String down = map.get("down").toString();
            int left1=Integer.parseInt(left);
            int top1=Integer.parseInt(top);
            int right1=Integer.parseInt(right);
            int down1=Integer.parseInt(down);
            //float left2=(float)left;
            int left2=(int)(left1*1.39f*getX/1280);
            int top2=(int)(top1*1.39f*getY/720);
            int right2=(int)(right1*1.39f*getX/1280);
            int down2=(int)(down1*1.39f*getY/720);



            System.out.println("huatu"+left1+" "+top1+ " "+right1+" "+down1);

            try
            {
                Thread.currentThread().sleep(300);//毫秒
            }
            catch(Exception e){}

            if(left1==-1 & top1==-1 & right1==-1 & down1==-1){
                mSVDraw.clearDraw();
            }else {
                mSVDraw.clearDraw();
                mSVDraw.drawLine(left2, top2, right2, down2);
            }
        } else if(notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_LEFT)){
            //Someone left room
        }else if(notification.getMethod().equals(RoomListener.METHOD_PARTICIPANT_JOINED)){
            //Someone joined room
        }
    /*获取屏幕分辨率*/
    }
    private void getDisplayInformation(){
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        getX = point.x;
        getY = point.y;
    }


    private Runnable clearMessageView = new Runnable() {//清除消息显示
        @Override
        public void run() {
            NewPeerVideoActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextMessageTV.setText("");
                }
            });
        }
    };



    @Override
    public void onRoomConnected(){
        System.out.println("zhixingonstart"+zhixing++);
        if(kurentoRoomAPI.isWebSocketConnected()){
            joinRoom();
            /*try{
                Thread.currentThread().sleep(300);
            }catch (Exception e){}
            kurentoRoomAPI.sendTestPing(true,486586386);
            try{
                Thread.currentThread().sleep(300);
            }catch (Exception e){}
           new Timer().scheduleAtFixedRate(ping,1000,3000);*/
            System.out.println("jiaru");
            System.out.println( kurentoRoomAPI.isWebSocketConnected());

            Message message = new Message();
            message.what = 1;
            m1Handler.sendMessage(message);



        }
    }

    TimerTask ping = new TimerTask() {
        @Override
        public void run() {
            kurentoRoomAPI.sendTestPing(false,Integer.parseInt(userName.substring(userName.length()-1)),386486586);//Integer.parseInt(userName.substring(userName.length()-1))
            System.out.println("sendpingtxx");
        }
    };



    public void joinRoom(){
        //userName="Glass2";
        userName=getUserName1();
        roomName="iplab";
        Constants.id++;
        roomId = Constants.id;
       // Log.i(TAG, "Joinroom: User: "+this.username+", Room: "+this.roomname+" id:"+roomId);
        if(kurentoRoomAPI.isWebSocketConnected()){
            kurentoRoomAPI.sendJoinRoom(userName,roomName,true,roomId);
        }
    }

    public String getUserName1() {
        if (read() == null) {
            Toast.makeText(this, "请扫描二维码进行相关设置", Toast.LENGTH_SHORT).show();
        } else {
            resultProcess(read());
            System.out.println("chaxun" + read());
            //if(scan.get(1)==null){//csan由方法resultHandle处理得到
            //  Toast.makeText(this,"请扫描二维码设置用户名",Toast.LENGTH_SHORT).show();
            //}
        }
        return scan.get(1);
    }

    public String getIpAdress(){
        if(read() == null){
            Toast.makeText(this, "请扫描二维码进行相关设置", Toast.LENGTH_SHORT).show();
        }else {
            resultProcess(read());
        }

        return scan.get(6);
    }
    public String getResolution(){
        if(read() == null){
            Toast.makeText(this, "请扫描二维码进行相关设置", Toast.LENGTH_SHORT).show();
        }else {
            resultProcess(read());
        }

        return scan.get(4);
    }

    public void setResolution(String str){
        str = getResolution();
        if( str.equals("720")){
            width=1280;height=720;
        }else if(str.equals("240")){
            width=320;height=240;
        }else if(str.equals("640")){
            width=640;height=480;
        }else if(str.equals("1080")){
            width=1920;height=1080;
        }
    }

    public String getBandWidth(){
        if(read() == null){
            Toast.makeText(this, "请扫描二维码进行相关设置", Toast.LENGTH_SHORT).show();
        }else {
            resultProcess(read());
        }

        return scan.get(5);
    }

    public void setBandWidth(String str){
        str = getResolution();
        if( str.equals("500")){
            bandWidth=500;
        }else if(str.equals("1000")){
            bandWidth=1000;
        }else if(str.equals("1500")){
            bandWidth=1500;
        }else if(str.equals("2000")){
            bandWidth=2000;
        }
    }



   /* public void resultProcess(String result) {
        String str;
        str = result;
        int first = str.indexOf(" ");
        int last = str.lastIndexOf(" ");
        int length = str.length();
        String user = str.substring(0,first);
        String ssid = str.substring(first+1,last);
        String password = str.substring(last+1,length);
        scan.put(1, user);
        scan.put(2,ssid);
        scan.put(3,password);

        System.out.println("chaxun2"+user);
        System.out.println("chaxun2"+ssid);
        System.out.println("chaxun2"+password);

        System.out.println("chaxun"+scan.get(1));
        System.out.println("chaxun"+scan.get(2));
        System.out.println("chaxun"+scan.get(3));
    }*/

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
    }

    @Override
    public void onRoomDisconnected(){
        Toast.makeText(this, "网络连接断开", Toast.LENGTH_SHORT).show();

    }

    private long getTotalTxBytes() {
        return TrafficStats.getUidTxBytes(getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalTxBytes() / 1024);//转为KB
    }
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            showNetSpeed();
        }
    };
    private void showNetSpeed() {

        long nowTotalTxBytes = getTotalTxBytes();
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalTxBytes - lastTotalTxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        System.out.println("wangsu" + speed);
        lastTimeStamp = nowTimeStamp;
        lastTotalTxBytes = nowTotalTxBytes;

        Message msg = m2Handler.obtainMessage();
        msg.what = 100;
        msg.obj = String.valueOf(speed) + " KB/s";

        m2Handler.sendMessage(msg);//更新界面
    }


    private String read(){
        try {
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                //获得SD卡对应的存储目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                //获取指定文件对应的输入流
                FileInputStream fis = new FileInputStream(sdCardDir.getCanonicalPath() + FILE_NAME);
                //将指定输入流包装成BufferReader
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder("");
                String line = null;
                //循环读取文件内容
                while((line = br.readLine()) != null){
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

    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("finish".equals(intent.getAction())) {
                Log.e("#########", "I am " + getLocalClassName()
                        + ",now finishing myself...");
                try {
                    reload();
                   // System.out.println("shoudaole");
                } catch (Exception e) {
                }
            }
        }
    };
    /**断网重连方法*/
    public void reload() {
        mSVDraw.clearDraw();
        if (kurentoRoomAPI.isWebSocketConnected()) {
            task.cancel();
            ping.cancel();
            ping1.running=false;
        }
        System.out.println("zhixingreload");
        reStart = !reStart;
        finish();
    }

}
