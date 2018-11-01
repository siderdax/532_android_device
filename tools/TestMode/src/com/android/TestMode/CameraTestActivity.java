package com.android.TestMode;

import java.io.IOException;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.app.Activity;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import java.util.*;

import android.view.ViewGroup;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;



public class CameraTestActivity extends Activity implements SurfaceHolder.Callback,View.OnTouchListener{
	
	static final int FINISH_PREVIEW = 1;
	private Camera mCamera01;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;
    private boolean bIfPreview=false;
	private final String TAG="robert_camera_engineer<<<";
	
	private final Timer timer = new Timer();
    private TimerTask task;
    
    public EngSqlite mEngSqlite;//[yeez_haojie add 12.3]
    
    private FocusManager mFocusManager;  // ++ [yeez_liuwei]
    private boolean mIsSucces = false;
    private boolean mAutoFocusHandled = true;
    private int mWidth;
    private int mHeight;
    
    String focus = "";
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
         // TODO Auto-generated method stub
         
         super.handleMessage(msg);

         if(msg.what == FINISH_PREVIEW)
         {
        	  Log.v(TAG,"FINISH_PREVIEW!!!!");
        	  //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//[yeez_haojie add 12.17]
        	  new AlertDialog.Builder(CameraTestActivity.this).setCancelable(false)//[yeez_haojie add 12.17]
        	  .setTitle(R.string.Camera)
        	  .setMessage(R.string.DialogMessage)        	 
        	  .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
  						final Intent intent = new Intent(CameraTestActivity.this, TestModeActivity.class);
                        setResult(RESULT_OK);
                        BaseActivity.NewstoreRusult(true, "Camera test",mEngSqlite);//[yeez_haojie add 12.3]
                        finish();
        	  }

        	  })
        	  .setNegativeButton(R.string. Button_No, new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
                  setResult(RESULT_FIRST_USER);
                  BaseActivity.NewstoreRusult(false, "Camera test",mEngSqlite);//[yeez_haojie add 12.3]

                  finish();
        	  }
        	  })
        	  .setOnKeyListener(new OnKeyListener() {

        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {		
        					Log.d(TAG,"onKeyDown !______!!!!keyCode:"+keyCode);
        					return true;
        		         }
        		         return false;
        			}
        		})
        	  .show();

        	  }

         }
        };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
				getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN,WindowManager.LayoutParams. FLAG_FULLSCREEN); 
        setContentView(R.layout.camera);
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 12.3]
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Display display = getWindowManager().getDefaultDisplay();
        mWidth = display.getWidth();
        mHeight = display.getHeight();
        
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mFocusManager = new FocusManager((ViewGroup)findViewById(R.id.relative), mSurfaceView);
        mSurfaceHolder=mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
      //  mSurfaceHolder.setFixedSize(320, 480);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
           
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//[yeez_haojie add 12.17]
        setupBottom();//[yeez_haojie add 12.17]
        Log.v(TAG,"onCreate end");
      }

    private void closeCamera()
    {
      if (mCamera01 != null )
      {
        mCamera01.stopPreview();
        mCamera01.release();
        mCamera01 = null;
        bIfPreview = false;
      }
    }
    
  private synchronized void initCamera()
    {
    	if (!bIfPreview)
    	{
    		try
    		{
    		 mCamera01=Camera.open();
    		}catch(Exception e)
    		{
    			Log.e(TAG,e.getMessage());
    		}
    	}
    	
    	if(mCamera01 !=null && !bIfPreview)
    	{
    		try
    		{
    			Log.v(TAG,"inside the camera");
    			mCamera01.setDisplayOrientation(90);//[yeez_haojie add 12.17]
    			mCamera01.setPreviewDisplay(mSurfaceHolder);
    			Camera.Parameters parameters=mCamera01.getParameters();
    			List<Camera.Size> s=parameters.getSupportedPreviewSizes();
    			
    			 focus =  parameters.getFocusMode();//[yeez_haojie add 2013.3.7]

    			mSurfaceView.setOnTouchListener(this);
    			try
    			{
    				if (s != null)
    				{
    					for(int i=0;i<s.size();i++)
    					{
    						Log.i(TAG," "+(((Camera.Size)s.get(i)).height)+"/"+(((Camera.Size)s.get(i)).width));
    					}
    				}
    			parameters.setPreviewSize(640, 480);
    			try
    			{
    				mCamera01.setParameters(parameters);
    				mCamera01.setPreviewDisplay(mSurfaceHolder);
    				mCamera01.startPreview();
    				bIfPreview=true;
    				Log.v(TAG,"startPreview");
    			}
    			catch(Exception e)
    			{
    				Log.e(TAG,e.getMessage());
    				e.printStackTrace();
    			}
    		}catch(Exception e)
			{	Toast.makeText(getApplicationContext(), "initCamera error.",Toast.LENGTH_LONG).show();
				Log.e(TAG,e.getMessage());
				e.printStackTrace();
			}
    		}catch(IOException e)
    		{
    			mCamera01.release();
    			mCamera01=null;
    			Log.i(TAG,e.toString());
    			e.printStackTrace();
    		}
    	}
    }
   
    
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Log.i(TAG,"Surface changed");
    	initCamera();
    	/*task = new TimerTask() {
    	    @Override
    	    public void run() {
    	     // TODO Auto-generated method stub
    	     Message message = new Message();
    	     message.what = FINISH_PREVIEW;
    	     handler.sendMessage(message);
    	    }
    	   };    	
    	timer.schedule(task, 5000);*/ //[yeez_haojie modify 12.17]
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	Log.i(TAG,"surface Created");


    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.i(TAG,"surface Destroyed");
    }
    
    
    private AutoFocusCallback mAutoFocusCallBack = new AutoFocusCallback() {  
    	  
        @Override  
        public void onAutoFocus(boolean success, Camera camera) {  
        	//Log.v(TAG,"onAutoFocus ");  
        	int result = 0;
            if (success) {  
            	result = R.string.success;
            	mIsSucces = true;
            	
               // mInProgress = true;  
            	
            	//Log.v(TAG,"onAutoFocus success");

            	
                // set parameters of camera  
               /* Camera.Parameters Parameters = mCamera.getParameters();  
                Parameters.setPreviewSize(IMG_PREVIEW_WIDTH, IMG_PREVIEW_HEIGHT);  
                Parameters.setPictureSize(IMG_WIDTH, IMG_HEIGHT);  
                mCamera.setParameters(Parameters);  
      
                mCamera.takePicture(mShutterListener, null, mImageCaptureCallback); */ 
            } else {
            	result = R.string.fail; 
            	mIsSucces = false;
            }
            
        	Toast.makeText(CameraTestActivity.this,
      	          result , Toast.LENGTH_SHORT).show();
        	mAutoFocusHandled = true;
        }  
    };  
    
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG,"onDestroy11");
		 closeCamera();
	}
	
	@Override
	public void finish() {
		closeCamera();
		super.finish();
	}
    
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG,"onKeyDown keyCode:"+keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			Log.d(TAG,"onKeyDown !!!!!!!!!!keyCode:"+keyCode);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
//		initCamera();
		super.onResume();
	}  

	private void setupBottom() {
        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if(!mIsSucces) {
            		Toast.makeText(CameraTestActivity.this, R.string.ensure_after_success, Toast.LENGTH_SHORT).show();
            		return;
            	}
               //passed
               setResult(RESULT_OK);
               BaseActivity.NewstoreRusult(true, "Camera test",mEngSqlite);
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "Camera test",mEngSqlite);//[yeez_haojie add 12.3]
               finish();
            }
        });
     }
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		Log.v(TAG,"onTouch focus :"+focus);
		int action = event.getAction();
		
		if(action == MotionEvent.ACTION_DOWN && mAutoFocusHandled) {
			mAutoFocusHandled = false;
			mFocusManager.onTouch(event);
			Parameters parameters = mCamera01.getParameters();
//			parameters.setFocusAreas(mFocusManager.getFocusAreas());
			float x =  event.getX();
			float y =  event.getY();
			int width = (int) (x/mWidth *2000 -1000);
			int height = (int) (y/mHeight*2000 - 1000);
			if(Math.abs(width) < 200)
				width = 300;
			else if(Math.abs(width) > 800)
				width = 750;
			if(Math.abs(height) < 200)
				height = 300;
			else if(Math.abs(height) >800)
				height = 750;
			Rect rect = new Rect(-200+width, -200+height, 200+width, 200+height);
			Area area = new Area(rect, 900);
			List<Area>focusAreas = new ArrayList<Area>();
			focusAreas.add(area);
			parameters.setFocusAreas(focusAreas);
			mCamera01.setParameters(parameters);
			mCamera01.autoFocus(mAutoFocusCallBack);
		}
		return false;
	}
	
}














