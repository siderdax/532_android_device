package com.android.TestMode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MutiTouchTest extends Activity{
    private MuiltImageView imgView;
    private DisplayMetrics mDisplayMetrics;
    private MainHandler mHandler;
    private Context mContext;
    public EngSqlite mEngSqlite;
    private static final int MULTI_TOUCH_COUNT = 2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	//setmTestCaseName("Multi-TP test");
    	/*int isFullTest = getIntent().getIntExtra("isFullTest", 0);
    	int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId", 0);
        setIsFullTest(isFullTest, ++fullTestActivityId);*/
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mContext = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getMetrics(mDisplayMetrics);
        mHandler = new MainHandler();
        imgView = new MuiltImageView(this, mDisplayMetrics.widthPixels,
                mDisplayMetrics.heightPixels,mHandler);
        
        
        /*LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imgView = (MuiltImageView) inflater.inflate(R.layout.mutitouchtest, null, true);*/
        
        //setTitle(R.string.muti_touchpoint_test);
        setContentView(imgView);
        super.onCreate(savedInstanceState);
        mEngSqlite = EngSqlite.getInstance(this);
        Toast.makeText (this, R.string.mutitouchToast, Toast.LENGTH_LONG).show();
    }
 
    private class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                BaseActivity.NewstoreRusult(true, "Multi point touch test",mEngSqlite);
                finish();
            }
        }

    }

    private class MuiltImageView extends View{
        private static final float RADIUS = 75f;
        private PointF pointf = new PointF();
        private PointF points = new PointF();
  
        private PointF pointa = new PointF();
        private PointF pointd = new PointF();
	    private PointF pointg = new PointF();
        
        private Handler mHandler;
        private boolean mPass = false;
        private int mWidth, mHeight;


        public MuiltImageView(Context context, int width, int height,Handler handler) {
            super(context);
            mWidth = width;
            mHeight = height;
            mHandler = handler;
            initData();
            
           /* LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            imgView = (MuiltImageView) inflater.inflate(R.layout.mutitouchtest, null, true);
            
            //RelativeLayout relate = (RelativeLayout).inflater(R.layout.mutitouchtest, null);
            //LinearLayout relate = (LinearLayout)inflater.inflate(R.layout.mutitouchtest, null);
            RelativeLayout relate = (RelativeLayout)inflater.inflate(R.layout.mutitouchtest, null);*/
           
        }



        private void initData() {
            pointf.set(mWidth - RADIUS, RADIUS);
            points.set(RADIUS, mHeight-RADIUS-150);
            
            if (MULTI_TOUCH_COUNT == 5) {
		            pointa.set((float)Math.random()*mWidth, (float)Math.random()*mHeight);
		            pointd.set((float)Math.random()*mWidth, (float)Math.random()*mHeight);
		            pointg.set((float)Math.random()*mWidth, (float)Math.random()*mHeight);
            }
            
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(pointf.x, pointf.y, RADIUS, paint);
            canvas.drawCircle(points.x, points.y, RADIUS, paint);
            if (MULTI_TOUCH_COUNT == 5) {
		            canvas.drawCircle(pointa.x, pointa.y, RADIUS, paint);
		            canvas.drawCircle(pointd.x, pointd.y, RADIUS, paint);
		            canvas.drawCircle(pointg.x, pointg.y, RADIUS, paint);
            }
            
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
           // if (event.getPointerCount() == 2) {
        	 if (event.getPointerCount() == MULTI_TOUCH_COUNT) {
                pointf.set(event.getX(0), event.getY(0));
                points.set(event.getX(1), event.getY(1));
                // [yeez_haojie add st 2013.2.18]
                if (MULTI_TOUCH_COUNT == 5) {
		                pointa.set(event.getX(2), event.getY(2));
		                pointd.set(event.getX(3), event.getY(3));
		                pointg.set(event.getX(4), event.getY(4));
                }
                
                    double distance = Math.sqrt((pointf.x - points.x) * (pointf.x - points.x)
                            + (pointf.y - points.y) * (pointf.y - points.y));
                    if (distance < mWidth/3 || distance > mWidth/3*2) {
                        mPass = true;

                    }
//              if (arround(event, 0, pointf)) {
//                  pointf.set(event.getX(0), event.getY(0));
//              } else if (arround(event, 0, points)) {
//                  points.set(event.getX(0), event.getY(0));
//              }
//
//              if (arround(event, 1, pointf)) {
//                  pointf.set(event.getX(1), event.getY(1));
//              } else if (arround(event, 1, points)) {
//                  points.set(event.getX(1), event.getY(1));
//              }
            }
            if (event.getAction() == MotionEvent.ACTION_UP && mPass) {
                mHandler.sendEmptyMessage(1);
            }
            invalidate();
            return true;
        }


    //  public boolean arround(MotionEvent ev, int pointIndex, PointF pf) {
//          PointF pointc = new PointF(ev.getX(pointIndex), ev.getY(pointIndex));
//          double distance = Math.sqrt((pointc.x - pf.x) * (pointc.x - pf.x)
//                  + (pointc.y - pf.y) * (pointc.y - pf.y));
//          if (distance <= RADIUS) {
//              return true;
//          }
//          return false;
    //  }
    }
}
