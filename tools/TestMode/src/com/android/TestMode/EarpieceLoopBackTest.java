package com.android.TestMode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * @author haojie.shi
 * [yeez_haojie add 11.30]
 */
public class EarpieceLoopBackTest extends Activity{

	public byte mPLBTestFlag[] = new byte[1];
    public EngSqlite mEngSqlite;//[yeez_haojie add 11.23]
    private File file = null;
    private FileOutputStream fos;
    private DataOutputStream dos;
    private FileDescriptor fd;
    private AudioManager audioManager;

    private static String TAG = "EarpieceLoopBackTest";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.earpiece_loopback_test);
        //setTitle(R.string.earpiece_audioloop_test);

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
        audioManager.setParameters("looptest=headset");
    }
    
    private void startLoophardbackAudio() {
        Log.v(TAG, "startLoophardbackAudio()");
        audioManager.setParameters("looptest=0");
        audioManager.setParameters("routing=4");
    }

    private void setupBottom() {
        Log.v(TAG, "setupBottom()");

        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startLoophardbackAudio();
               //passed
               setResult(RESULT_OK);

               BaseActivity.NewstoreRusult(true, "EarpieceAudioloop test",mEngSqlite);
               
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	startLoophardbackAudio();
               //failed
               setResult(RESULT_FIRST_USER);
               
               BaseActivity.NewstoreRusult(false, "EarpieceAudioloop test",mEngSqlite);
               
               finish();
            }
        });
     }
    
	
}
