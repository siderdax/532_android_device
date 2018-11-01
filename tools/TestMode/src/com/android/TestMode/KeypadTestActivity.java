package com.android.TestMode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.hardware.SensorManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

public class KeypadTestActivity extends Activity {
    /** Called when the activity is first created. */
    private static final String LOG_TAG = "KeypadTest";
    boolean bVolUpResult = false;
    boolean bVolDownResult = false;
    boolean bRayhovResult = false;
    boolean bHomeResult = false;
    boolean bMenuResult = false;
    boolean bBackResult = false;
    boolean bRearchResult = false;
    public EngSqlite mEngSqlite;//[yeez_haojie add 11.27]
    
    /* huanglongcheng 2014.1.16 add for navigation bar */
    boolean mHasNavigationBar = false;
    Context mContext;
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.keypad);
       mContext = this;
       mHasNavigationBar= mContext.getResources().getBoolean(
    		   com.android.internal.R.bool.config_showNavigationBar);
       if (mHasNavigationBar){
    	   Button mButton = (Button) findViewById(R.id.textHome); 
           mButton.setVisibility(View.INVISIBLE);
           mButton = (Button) findViewById(R.id.textMenu); 
           mButton.setVisibility(View.INVISIBLE);
           mButton = (Button) findViewById(R.id.textBack); 
           mButton.setVisibility(View.INVISIBLE);
       }    	   
       mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.27]	
       setupBottom();
    }

 /*   @Override
    public void onAttachedToWindow () {
        Log.d(LOG_TAG,"onAttachedToWindow");
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD); 
        super.onAttachedToWindow();
    }*/ //[yeez_haojie modify 11.15]

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(LOG_TAG,"onKeyDown:"+keyCode);
        if (keyCode == KeyEvent.KEYCODE_HOME) {
        	if (!mHasNavigationBar){
	            Button mButton = (Button) findViewById(R.id.textHome); 
	            mButton.setVisibility(View.INVISIBLE);
	            bHomeResult = true;
	            if (IsAllKeyTested())
	            {
	            	Button b = (Button) findViewById(R.id.testpassed);
	            	b.setVisibility(View.VISIBLE);
	            }
        	}
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
        	if (!mHasNavigationBar){
	            Button mButton = (Button) findViewById(R.id.textMenu); 
	            mButton.setVisibility(View.INVISIBLE);
	            bMenuResult = true;
	            if (IsAllKeyTested())
	            {
	            	Button b = (Button) findViewById(R.id.testpassed);
	            	b.setVisibility(View.VISIBLE);
	            }
        	}
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (!mHasNavigationBar){
	            Button mButton = (Button) findViewById(R.id.textBack); 
	            mButton.setVisibility(View.INVISIBLE);
	            bBackResult = true;
	            if (IsAllKeyTested())
	            {
	            	Button b = (Button) findViewById(R.id.testpassed);
	            	b.setVisibility(View.VISIBLE);
	            }
        	}
            return true;
        } /*else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            Button mButton = (Button) findViewById(R.id.textSearch); 
            mButton.setVisibility(View.INVISIBLE);
            bRearchResult = true;
            if (IsAllKeyTested())
            {
            	Button b = (Button) findViewById(R.id.testpassed);
            	b.setVisibility(View.VISIBLE);
            }
            return true;
        } */ //[yeez_haojie modify 11.15]
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Button mButton = (Button) findViewById(R.id.textVolUp); 
            mButton.setVisibility(View.INVISIBLE);
            bVolUpResult = true;
            if (IsAllKeyTested())
            {
            	Button b = (Button) findViewById(R.id.testpassed);
            	b.setVisibility(View.VISIBLE);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Button mButton = (Button) findViewById(R.id.textVolDown); 
            mButton.setVisibility(View.INVISIBLE);
            bVolDownResult = true;
            if (IsAllKeyTested())
            {
            	Button b = (Button) findViewById(R.id.testpassed);
            	b.setVisibility(View.VISIBLE);
            }
            return true;
        } /*else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            Button mButton = (Button) findViewById(R.id.textRayhov); 
            mButton.setVisibility(View.INVISIBLE);
            bRayhovResult = true;
            if (IsAllKeyTested())
            {
            	Button b = (Button) findViewById(R.id.testpassed);
            	b.setVisibility(View.VISIBLE);
            }
            return true;
        }*/ //[yeez_haojie modify 11.15]
        return super.onKeyDown(keyCode, event);
    }

    private boolean IsAllKeyTested() {
        return (boolean)( 
        	bVolUpResult && bVolDownResult &&
        	(mHasNavigationBar? true : (bMenuResult && bBackResult && bHomeResult)));
    }
	
	private void setupBottom() {
       Button b = (Button) findViewById(R.id.testpassed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //passed
              setResult(RESULT_OK);
              BaseActivity.NewstoreRusult(true, "Keyboard test",mEngSqlite);//[yeez_haojie add 11.27]
              finish();
           }
       });
       
       b = (Button) findViewById(R.id.testfailed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //failed
              setResult(RESULT_FIRST_USER);
              BaseActivity.NewstoreRusult(false, "Keyboard test",mEngSqlite);//[yeez_haojie add 11.27]
              finish();
           }
       });
    }

}
