package com.android.TestMode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class PhoneLoopBackTest extends Activity {
    public byte mPLBTestFlag[] = new byte[1];
    public EngSqlite mEngSqlite;
    private File file = null;
    private FileOutputStream fos;
    private DataOutputStream dos;
    private FileDescriptor fd;
    private AudioManager audioManager;

    private static String TAG = "PhoneLoopBackTest";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.v(TAG, "onCreate()");
	
        super.onCreate(savedInstanceState);
        /*int isFullTest = getIntent().getIntExtra("isFullTest", 0);
        int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId",
                0);
        setIsFullTest(isFullTest, ++fullTestActivityId);*/
        //setmTestCaseName("Phone loopback test");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.micphone_test);
        //setTitle(R.string.phone_loopback_test);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
        mEngSqlite = EngSqlite.getInstance(this);
        
        setupBottom();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume()");
        super.onResume();
        startLoophardAudio();
    }

    @Override
    protected void onDestroy() {
    	Log.v(TAG, "onDestroy()");
    	super.onDestroy();
    	startLoophardbackAudio();	
    }

    private void startLoophardAudio() {
        Log.v(TAG, "startLoophardAudio()");
        audioManager.setParameters("looptest=handset");
    }
    
    private void startLoophardbackAudio() {
        Log.v(TAG, "startLoophardbackAudio()");
        audioManager.setParameters("looptest=0");
        audioManager.setParameters("routing=2");
    }

    private void setupBottom() {
        Log.v(TAG, "setupBottom()");

        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startLoophardbackAudio();
               //passed
               setResult(RESULT_OK);

               BaseActivity.NewstoreRusult(true, "Audioloop test",mEngSqlite);//[yeez_haojie add 11.22]
               
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startLoophardbackAudio();
               //failed
               setResult(RESULT_FIRST_USER);
               
               BaseActivity.NewstoreRusult(false, "Audioloop test",mEngSqlite);//[yeez_haojie add 11.22]
               
               finish();
            }
        });
     }
}
