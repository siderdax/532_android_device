package com.android.TestMode;

import java.io.IOException;
import android.graphics.PixelFormat;
import android.app.Activity;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import java.util.*;



import android.app.AlertDialog;
import android.content.Intent;
import android.content.DialogInterface;



public class FrontCameraTestActivity extends Activity implements SurfaceHolder.Callback{
	
	static final int FINISH_PREVIEW = 1;
	private Camera mCamera01;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder = null;
    private boolean bIfPreview=false;
	private final String TAG="FrontCameraTestActivity<<<";
	
	public EngSqlite mEngSqlite;//[yeez_haojie add 12.28]
	
	private final Timer timer = new Timer();
    private TimerTask task;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
         // TODO Auto-generated method stub
         
         super.handleMessage(msg);

         if(msg.what == FINISH_PREVIEW)
         {
        	  Log.i(TAG,"FINISH_PREVIEW!!!!");
        	 
        	  new AlertDialog.Builder(FrontCameraTestActivity.this)
        	  .setTitle(R.string.FrontCamera)
        	  .setMessage(R.string.DialogMessage)        	 
        	  .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
  						final Intent intent = new Intent(FrontCameraTestActivity.this, TestModeActivity.class);
                        setResult(RESULT_OK);
                        finish();
        	  }

        	  })
        	  .setNegativeButton(R.string. Button_No, new DialogInterface.OnClickListener() {
        	  public void onClick(DialogInterface dialog, int whichButton) {
                  setResult(RESULT_FIRST_USER);
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

        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 12.28]
        
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_preview);
        mSurfaceHolder=mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
      //  mSurfaceHolder.setFixedSize(320, 480);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
           
        //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//[yeez_haojie modfiy 12.28]
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//[yeez_haojie add 12.28]
        setupBottom();//[yeez_haojie add 12.28]
        Log.i(TAG,"onCreate end");
      }

//    private synchronized void initCamera()
//    {
//      if(!bIfPreview)
//      {

//        mCamera01 = Camera.open();
//      }
//      
//      if (mCamera01 != null && !bIfPreview)
//      {
//        Log.i(TAG, "inside the camera");
//        

//        Camera.Parameters parameters = mCamera01.getParameters();
//        

//        parameters.setPictureFormat(PixelFormat.JPEG);
//        

//        parameters.setPreviewSize(640, 480);
//        

//        parameters.setPictureSize(320, 240);
//        

//        mCamera01.setParameters(parameters);
//        

//        try
//		{
//        mCamera01.setPreviewDisplay(mSurfaceHolder);
//        }catch(IOException e)
//		{
//			mCamera01.release();
//			mCamera01=null;
//			Log.i(TAG,e.toString());
//			e.printStackTrace();
//		}
//        

//        mCamera01.startPreview();
//        bIfPreview = true;
//      }
//    }
    private void closeCamera()
    {
      if (mCamera01 != null && bIfPreview)
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
    			int cameraCount;
    			Camera.CameraInfo cameraInfo = new Camera.CameraInfo();  
			cameraCount = Camera.getNumberOfCameras(); // get cameras number  
        
			for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {  
    				Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo  
    				if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {
    		 			mCamera01=Camera.open(camIdx);
					break;
    				}
			}
    		}catch(Exception e)
    		{
    			Log.e(TAG,e.getMessage());
    		}
    	}
    	
    	if(mCamera01 !=null && !bIfPreview)
    	{
    		try
    		{
    			Log.i(TAG,"inside the camera");
    			
    			//mCamera01.setDisplayOrientation(270);//[yeez_haojie add 12.29]
    			mCamera01.setDisplayOrientation(90);//[yeez_haojie add 3.7]
    			
    			mCamera01.setPreviewDisplay(mSurfaceHolder);
    			Camera.Parameters parameters=mCamera01.getParameters();
    			List<Camera.Size> s=parameters.getSupportedPreviewSizes();
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
    				Log.i(TAG,"startPreview");
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
    	
    	/*task = new TimerTask() {
    	    @Override
    	    public void run() {
    	     // TODO Auto-generated method stub
    	     Message message = new Message();
    	     message.what = FINISH_PREVIEW;
    	     handler.sendMessage(message);
    	    }
    	   };    	
    	timer.schedule(task, 5000);*/ //[yeez_haojie modify 12.28]
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	Log.i(TAG,"surface Created");
    	initCamera();

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.i(TAG,"surface Destroyed");
    }
    
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG,"onDestroy11");
		 closeCamera();
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
	protected void onPause() {
		closeCamera();
		super.onPause();
	}
	@Override
	protected void onResume() {
//		initCamera();
		super.onResume();
	}  

	/**
	 * [yeez_haojie add 12.28]
	 */
	private void setupBottom() {
        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               setResult(RESULT_OK);
               BaseActivity.NewstoreRusult(true, "FrontCamera test",mEngSqlite);
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "FrontCamera test",mEngSqlite);//[yeez_haojie add 12.3]
               finish();
            }
        });
     }
}

