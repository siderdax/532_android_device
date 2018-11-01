package com.android.TestMode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;
import android.text.TextUtils;

import android.media.AudioManager;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.text.InputType;

public class FMTestActivity extends Activity{
    static final int FINISH_TEST = 1;
    private static final String LOG_TAG = "FMTest";

    private TextView mFreq = null;
    private EditText mEditValue = null;
    private Button mBtnStart = null;
    private Button mBtnFail = null;
    private Button mBtnSuccess = null;

    private final BroadcastReceiver mReceiver = new FMTestServiceBroadcastReceiver();

    private static final int DIALOG_MSG_HEADSET = 4;

    Handler handler = new Handler() {
           public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(msg.what == FINISH_TEST)
                    {
                              new AlertDialog.Builder(FMTestActivity.this)
                              .setTitle(R.string.FM_test)
                              .setMessage(R.string.DialogMessage)            
                              .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                           setResult(RESULT_OK);
                                           finish();
                                       }
                    
                              })
                              .setNegativeButton(R.string.Button_No, new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog, int whichButton) {
                                           setResult(RESULT_FIRST_USER);
                                           finish();
                                       }
                              })
                              .setOnKeyListener(new OnKeyListener() {

    			        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
    //							        				if (keyCode == KeyEvent.KEYCODE_BACK ) {	
    //							        					
    //							        					return true;
    //							        		         }
    			        				Log.d(LOG_TAG,"key:"+keyCode);
    			        		         return true;
    			        			}
    			        		}).show();
                              
                    }
                    
           }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context mContext = this.getApplicationContext();
        
        setContentView(R.xml.fm);
        
        mEditValue = (EditText)findViewById(R.id.edit_fm_value);
        mBtnStart = (Button) findViewById(R.id.begin_fm_test);

        mEditValue.setInputType(InputType.TYPE_CLASS_PHONE);
        mEditValue.setText(R.string.preinstall_value);
        mEditValue.selectAll();

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mEditValue.selectAll();
                int testfreq = 0;
                try {
                    testfreq = Integer.parseInt(mEditValue.getText().toString());
                } catch(Exception e) {
                    Log.d(LOG_TAG, "sth error");
                }
                Log.d(LOG_TAG, "send TestFreq="+testfreq);
                Intent intent = new Intent();
                intent.setClassName("com.marvell.fmradio", "com.marvell.fmradio.MainActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("IsTestMode", true);
                intent.putExtra("TestFreq", testfreq);
                startActivity(intent);
            }
        });

        IntentFilter intentFilter =
        new IntentFilter("android.testmode.action.FM");

        mContext.registerReceiver(mReceiver, intentFilter);

        mBtnSuccess= (Button) findViewById(R.id.testpassed);
        mBtnFail = (Button) findViewById(R.id.testfailed);
        setupBottom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        mBtnSuccess.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               setResult(RESULT_OK);;
               finish();
            }
        });

        
        mBtnFail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               finish();
            }
        });
    }

    /**
     * Receiver for headset intent broadcasts the FM app cares about.
     */
    private class FMTestServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("android.testmode.action.FM")) {
                boolean result = intent.getBooleanExtra("FMresult", false);

                if(result == true) {
                    //mBtnSuccess.setVisibility(View.VISIBLE);
                    
                    mBtnSuccess.performClick();
                } else {
                    //mBtnFail.setVisibility(View.VISIBLE);
                   
                    mBtnFail.performClick();
                }
            }
        }
    }
}
