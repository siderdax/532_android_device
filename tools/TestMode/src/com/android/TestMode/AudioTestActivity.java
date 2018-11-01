package com.android.TestMode;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;  
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.util.*;

import java.util.*;


public class AudioTestActivity extends Activity {

	public static final int UPDATE = 0; 
	private TextView SpkText;
	private MediaRecorder recorder;
	private MediaPlayer player;
	private String path = "";
	private int duration = 0;
	private int state = 0;
	private static final int IDLE = 0;
	private static final int RECORDING = 1;
	
	boolean isRecording; //currently not used  
	private AudioManager amEar;
	private MediaPlayer speakerPlayer;
	
  boolean bRecTestResult = false;
  boolean bSpkTestResult = false;
  boolean bPlay = false;
    
  public EngSqlite mEngSqlite;//[yeez_haojie add 11.26]
  
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio);
        
        SpkText = (TextView) findViewById(R.id.Speak_Prompt); 
    
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.26]

		//amEar.setMicrophoneMute(true);

 		//speakerPlayer = new MediaPlayer(); 
		//speakerPlayer = MediaPlayer.create(EarpieceTestActivity.this, R.raw.test);
        Button btn = (Button)findViewById(R.id.btnSpeak);
        btn.setTextSize(25.0f);
        btn.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Log.i("-----","button click-----");
        		play_music();
              }
        });
	setupBottom();
    }
	
    private void play_music() { 
    SpkText.setText(R.string.playing);
    
		speakerPlayer = new MediaPlayer(); 
		speakerPlayer = MediaPlayer.create(AudioTestActivity.this, R.raw.liberation);
		
		//if(!speakerPlayer.isPlaying()){
			bPlay = true;
			speakerPlayer.start();
		//}
	 
		Button b = (Button) findViewById(R.id.testpassed);
		b.setVisibility(View.VISIBLE);
	}  
    

	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        if (recorder != null) {  
            recorder.release();  
            recorder = null;  
        }  
        if (player != null) {  
            player.release();  
            player = null;  
        }
       if (speakerPlayer != null){
        	speakerPlayer.release();
        	speakerPlayer = null;
        }
        finish();
    }  
	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setupBottom() {
       Button b = (Button) findViewById(R.id.testpassed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //passed
		if(bPlay)
	              speakerPlayer.stop();
              setResult(RESULT_OK);;
              BaseActivity.NewstoreRusult(true, "Speak test",mEngSqlite);//[yeez_haojie add 11.26]
              finish();
           }
       });
       
       b = (Button) findViewById(R.id.testfailed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //failed
		if(bPlay)
	              speakerPlayer.stop();
              setResult(RESULT_FIRST_USER);
              BaseActivity.NewstoreRusult(false, "Speak test",mEngSqlite);//[yeez_haojie add 11.26]
              finish();
           }
       });
    }

}
