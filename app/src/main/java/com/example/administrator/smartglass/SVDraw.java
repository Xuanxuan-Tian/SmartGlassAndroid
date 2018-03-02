

package com.example.administrator.smartglass;

/**
 * Created by Administrator on 2017/5/26.
 */


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/*定义一个画矩形框的类*/
public class SVDraw extends SurfaceView implements SurfaceHolder.Callback{

    protected SurfaceHolder sh;
    private int mWidth;
    private int mHeight;

    public SVDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        sh = getHolder();
        sh.addCallback(this);
        sh.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        System.out.println("huatu2");
    }
    //surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h)
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int w, int h) {
        System.out.println("huatu3");
        // TODO Auto-generated method stub
        mWidth = w;
        mHeight = h;
    }

    public void surfaceCreated(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
        // TODO Auto-generated method stub

    }
    void clearDraw()
    {
        Canvas canvas = sh.lockCanvas();
        if(canvas!=null){
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(paint);
            //canvas.drawColor(Color.BLUE);
        }
        sh.unlockCanvasAndPost(canvas);
    }
    public void drawLine(int a,int b,int c,int d)
    {System.out.println("huatu");
        clearDraw();
        Canvas canvas = sh.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);
        Paint p = new Paint();

        p.setAntiAlias(true);
        p.setColor(Color.WHITE);
        p.setStyle(Style.STROKE);
        p.setStrokeWidth(3.5f);
        //canvas.drawPoint(100.0f, 100.0f, p);
        System.out.println("huatu");
       //canvas.drawLine(50,110, 100, 110, p);
        //canvas.drawCircle(110, 110, 10.0f, p);
        canvas.drawRect(a,b,c,d,p);
        sh.unlockCanvasAndPost(canvas);


    }
    void clearCanvas(){
        Canvas canvas = sh.lockCanvas();
        Paint p = new Paint();
        //清屏
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        System.out.println("qingping");
    }

}
