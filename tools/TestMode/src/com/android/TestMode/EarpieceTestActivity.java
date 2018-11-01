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
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.util.*;

import java.util.*;


public class EarpieceTestActivity extends Activity {
       private static final String LOG_TAG = "EarpieceTest";
	public static final int UPDATE = 0; 
	private TextView RecText;
	private TextView SpkText;
	private TextView KeyText;
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

  public EngSqlite mEngSqlite;//[yeez_haojie add 11.27]
	boolean bPlaying = false;
	private final BroadcastReceiver mReceiver = new HeadsetPlugReceiver();
    	private final MediaButtonReceiver mediaButtonReceiver = new MediaButtonReceiver();  	
	static final int FINISH_REC = 1;
	
	private final Timer timer = new Timer();
	private TimerTask task;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == FINISH_REC)
			{
					stop();
					play();
			}
		
		}
	};


    public class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Log.v(LOG_TAG,"HeadsetPlugReceiver");

            
            KeyEvent event2 = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT); 
             if(event2 != null)
             {
            	    int action = event2.getAction(); 
                    if(action == event2.ACTION_DOWN)
                    {
                    	Log.v(LOG_TAG,"getAction ACTION_DOWN");
                    	Log.v(LOG_TAG, "Action ---->"+action + "  event2----->"+event2.toString());  

                    }
             }
 
            String intentAction = intent.getAction() ;  
  
            if(intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)){ 
            	Log.v(LOG_TAG,"getAction ACTION_HEADSET_PLUG");
 
                KeyEvent keyEvent = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);  
                  
                if(keyEvent != null)
                {
                
                	Log.v(LOG_TAG, "Action ---->"+intentAction + "  KeyEvent----->"+keyEvent.toString());  
                }
                if(Intent.ACTION_MEDIA_BUTTON.equals(intentAction)){  

                    int keyCode = keyEvent.getKeyCode() ;  

                    int keyAction = keyEvent.getAction() ;  

                    long downtime = keyEvent.getEventTime();  
                      
 
                    StringBuilder sb = new StringBuilder();  

                    if(KeyEvent.KEYCODE_MEDIA_NEXT == keyCode){  
                        sb.append("KEYCODE_MEDIA_NEXT");  
                    }  

                    if(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ==keyCode){  
                        sb.append("KEYCODE_MEDIA_PLAY_PAUSE");  
                    }  
                    if(KeyEvent.KEYCODE_HEADSETHOOK == keyCode){  
                        sb.append("KEYCODE_HEADSETHOOK");  
                    }  
                    if(KeyEvent.KEYCODE_MEDIA_PREVIOUS ==keyCode){  
                        sb.append("KEYCODE_MEDIA_PREVIOUS");  
                    }  
                    if(KeyEvent.KEYCODE_MEDIA_STOP ==keyCode){  
                        sb.append("KEYCODE_MEDIA_STOP");  
                    }  

                    Log.v(LOG_TAG, sb.toString());  
                      
                }  
            	
            	
            	
            	
            	  KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                  Log.v(LOG_TAG,"HeadsetPlugReceiver:EXTRA_KEY_EVENT");

                  if ((event != null)&& (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
                	  Log.v(LOG_TAG,"HeadsetPlugReceiver:EXTRA_KEY_EVENT down");
                     Toast.makeText(EarpieceTestActivity.this, "KEYCODE_HEADSETHOOK", Toast.LENGTH_SHORT).show();
                  } 
            	
                //headphone plugged
                if(intent.getIntExtra("state", 0) == 1){
                    //do something
                    RecText.setText(String.format(getString(R.string.EP_INSERT), " Pass"));
                    play_music();
                    //headphone unplugged
                }else{
                    Log.i(LOG_TAG,"you headset is unplugin...+++++++++++++"+intent.getAction());
                    if(speakerPlayer != null)
                    {
                        speakerPlayer.stop();
                        SpkText.setText(R.string.end);
                    }
                }
            }                     
        } 
    }
    
    public class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            Log.v(LOG_TAG,"MediaButtonReceiver:intentAction="+intentAction+",intentAction="+intentAction);
            KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if(keyEvent==null) return; 
            
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.v(LOG_TAG,"MediaButtonReceiver:EXTRA_KEY_EVENT");

            if ((event != null)&& (event.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
               Toast.makeText(EarpieceTestActivity.this, "KEYCODE_HEADSETHOOK", Toast.LENGTH_SHORT).show();
            } 
            
            if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                int keyCode = keyEvent.getKeyCode();  
                int keyAction = keyEvent.getAction();
                
                Log.v(LOG_TAG,"MediaButtonReceiver:keyCode="+keyCode+",keyAction="+keyAction);

                if(keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
                    if(keyAction == KeyEvent.ACTION_UP) {
                        KeyText.setText(String.format(getString(R.string.EP_KEY), " Pass"));
                        Button b = (Button) findViewById(R.id.testpassed);
                        b.setVisibility(View.VISIBLE); //[yeez_haojie modify 11.13
                    }

                    //abort this broadcast to disable media player
                    abortBroadcast();
                    return;
                }
            }
        }
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earpiece);
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.27]	
        RecText = (TextView) findViewById(R.id.Earpiece_Prompt); 
        SpkText = (TextView) findViewById(R.id.Speak_Prompt); 
	 KeyText = (TextView) findViewById(R.id.Key_Prompt); 
        IntentFilter intentFilter =
            new IntentFilter(Intent.ACTION_HEADSET_PLUG);
       // intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        intentFilter.setPriority(1000);  //1001
        registerReceiver(mReceiver, intentFilter);
        


    IntentFilter intentFiltermediabutton = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);  
    intentFiltermediabutton.setPriority(1000);  //1001
    registerReceiver(mediaButtonReceiver, intentFiltermediabutton);  
    
    

 	

		//amEar.setMicrophoneMute(true);

 		//speakerPlayer = new MediaPlayer(); 
		//speakerPlayer = MediaPlayer.create(EarpieceTestActivity.this, R.raw.test);

		setupBottom();
    }
	
    private void play_music() {
        SpkText.setText(R.string.playing);

        speakerPlayer = new MediaPlayer(); 
        speakerPlayer = MediaPlayer.create(EarpieceTestActivity.this, R.raw.liberation);

        //if(!speakerPlayer.isPlaying()){
        speakerPlayer.start();
        //}
    } 
    
 	private void play() { 
		//if ("".equals(path) || state == RECORDING)  
		//	return;
		Log.v(LOG_TAG, "play");
		if ("".equals(path))  
			return;
		RecText.setText(R.string.playing);
		MediaPlayer m_mpEarPlayer;

		amEar = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//amEar.setMicrophoneMute(true);
		amEar.setSpeakerphoneOn(false);
		//amEar.setLoopHandOn(true);
		amEar.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);  

		setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

 		amEar.setMode(AudioManager.MODE_IN_CALL);

 		m_mpEarPlayer = new MediaPlayer();  
		m_mpEarPlayer.reset();
		try{
			m_mpEarPlayer.setDataSource(path);

			m_mpEarPlayer.prepare();
		}catch(IOException e){
			Log.i(LOG_TAG,"play prepare error!!!!\n");
		};
		
		m_mpEarPlayer.start();

		File file = new File(path);
		boolean deleted = file.delete(); 

		RecText.setText(R.string.end);
		bRecTestResult = true;
		if (bSpkTestResult)
			ShowDialog();
		
	}  

    private void record() {  
        task = new TimerTask() {
            @Override
            public void run() {
                 // TODO Auto-generated method stub
                 Message message = new Message();
                 message.what = FINISH_REC;
                 handler.sendMessage(message);
            }
        };       
        timer.schedule(task, 4000);
        RecText.setText(R.string.recording);
        try {  
            if (recorder == null)  
                recorder = new MediaRecorder();  
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);  
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  
            path = "/sdcard/" + System.currentTimeMillis() + ".3ga";  
            recorder.setOutputFile(path);  
            recorder.prepare();  
            recorder.start();  
            state = RECORDING;  
            handler.sendEmptyMessage(UPDATE);  
        } catch (IllegalStateException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
    
    private void stop() {  
         if (recorder != null) {  
             recorder.stop();  
             recorder.release();  
        }  
        recorder = null;  
        handler.removeMessages(UPDATE);  
        state = IDLE;  
        duration = 0;  
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
	
	private String timeToString() {  
        if (duration >= 60) {  
            int min = duration / 60;  
            String m = min > 9 ? min + "" : "0" + min;  
            int sec = duration % 60;  
            String s = sec > 9 ? sec + "" : "0" + sec;  
            return m + ":" + s;  
        } else {  
            return "00:" + (duration > 9 ? duration + "" : "0" + duration);  
        }  
    } 

	private void ShowDialog() 
	{ 
			new AlertDialog.Builder(EarpieceTestActivity.this)
       .setTitle(R.string.Audio)
       .setMessage(R.string.DialogMessage)            
       .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                		//final Intent intent = new Intent(EarpieceTestActivity.this, WirelessTestActivity.class);
                        setResult(RESULT_OK);
                        onDestroy();
                }

       })
       .setNegativeButton(R.string.Button_No, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    setResult(RESULT_FIRST_USER);
                    onDestroy();
                }
       })
       .setOnKeyListener(new OnKeyListener() {

        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
        					
        					return true;
        		         }
        		         return false;
        			}
        		})

       .show();
	}
	
	
	

	
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
             Log.v(LOG_TAG,"onKeyDown:keyCode="+keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		
		if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
			Log.v(LOG_TAG,"KEYCODE_HEADSETHOOK onKeyDown:keyCode="+keyCode);//[yeez_haojie add 2013.2.28]
			
            Toast.makeText(EarpieceTestActivity.this, "Success", Toast.LENGTH_SHORT).show();

			//moveTaskToBack(true);
			//RecText.setText(R.string.earkey);
			//SpkText.setText(R.string.end);
			KeyText.setText(String.format(getString(R.string.EP_KEY), " Pass"));
			Button b = (Button) findViewById(R.id.testpassed);
			b.setVisibility(View.VISIBLE);
			return true;
		}
		
		
		if(KeyEvent.KEYCODE_MEDIA_NEXT == keyCode){  
            //sb.append("KEYCODE_MEDIA_NEXT"); 
            Log.v(LOG_TAG,"KEYCODE_MEDIA_NEXT="+keyCode);
            return true;
        }  

        if(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE ==keyCode){  
            //sb.append("KEYCODE_MEDIA_PLAY_PAUSE");  
            Log.v(LOG_TAG,"KEYCODE_MEDIA_PLAY_PAUSE="+keyCode);
            return true;
        }  
        if(KeyEvent.KEYCODE_HEADSETHOOK == keyCode){  
            //sb.append("KEYCODE_HEADSETHOOK");  
            Log.v(LOG_TAG,"KEYCODE_HEADSETHOOK="+keyCode);
            return true;
        }  
        if(KeyEvent.KEYCODE_MEDIA_PREVIOUS ==keyCode){  
            //sb.append("KEYCODE_MEDIA_PREVIOUS");  
            Log.v(LOG_TAG,"KEYCODE_MEDIA_PREVIOUS="+keyCode);
            return true;
        }  
        if(KeyEvent.KEYCODE_MEDIA_STOP ==keyCode){  
            //sb.append("KEYCODE_MEDIA_STOP");  
            Log.v(LOG_TAG,"KEYCODE_MEDIA_STOP="+keyCode);
            return true;
        }  
		
        if(KeyEvent.ACTION_DOWN ==keyCode){  
            //sb.append("KEYCODE_MEDIA_STOP");  
            Log.v(LOG_TAG,"ACTION_DOWN="+keyCode);
            return true;
        }  
		
/*cys		
		if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
			//moveTaskToBack(true);
			//RecText.setText(R.string.earkey);
			//SpkText.setText(R.string.end);
			KeyText.setText(String.format(getString(R.string.EP_KEY), " Pass"));
			Button b = (Button) findViewById(R.id.testpassed);
			b.setVisibility(View.VISIBLE);
			return true;
		}
*/		
		return super.onKeyDown(keyCode, event);
	}

	private void setupBottom() {
       Button b = (Button) findViewById(R.id.testpassed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //passed
              unregisterReceiver(mReceiver);
		unregisterReceiver(mediaButtonReceiver); //cys
		if (bPlaying)
              	speakerPlayer.stop();
              setResult(RESULT_OK);
              BaseActivity.NewstoreRusult(true, "Earphone test",mEngSqlite);//[yeez_haojie add 11.27]
              finish();
           }
       });
       
       b = (Button) findViewById(R.id.testfailed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //failed
              unregisterReceiver(mReceiver);
		unregisterReceiver(mediaButtonReceiver); //cys
		if (bPlaying)
	              speakerPlayer.stop();
              setResult(RESULT_FIRST_USER);
              BaseActivity.NewstoreRusult(false, "Earphone test",mEngSqlite);//[yeez_haojie add 11.27]
              finish();
           }
       });
    }

}
