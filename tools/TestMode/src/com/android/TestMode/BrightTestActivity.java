package com.android.TestMode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

public class BrightTestActivity extends Activity {
	private TextView mStatus;
	//private File file = new File("/sys/kernel/bl_sysfs/bl_setvalue");
	//private File file = new File("sys/class/backlight/backlight-0/brightness");
	private File file = new File("/sys/class/backlight/pwm-backlight.0/brightness");
	
	private FileOutputStream fos;
	private DataOutputStream dos;
	private FileDescriptor fd;
	private int brightness;  
	private Button b1;
	private Button b2;
	private int j;
	public EngSqlite mEngSqlite;//[yeez_haojie add 11.30]
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
  				WindowManager.LayoutParams.FLAG_FULLSCREEN);
  		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.xml.brightness_test);
        
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.30]
        
        DisplayMetrics dm = new DisplayMetrics();
  		dm = getApplicationContext().getResources().getDisplayMetrics();
  		int screenWidth = dm.widthPixels;
  		int screenHeight = dm.heightPixels;
        mStatus = (TextView)findViewById(R.id.brightness_test);
        mStatus.setBackgroundColor(Color.WHITE);
        mStatus.setWidth(screenWidth);
        mStatus.setHeight(screenHeight);
        mStatus.setText(R.string.brightness_test);
        mStatus.setGravity(Gravity.CENTER);
        mStatus.setVisibility(View.GONE);
        try {
			brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
			fos = new FileOutputStream(file);
			fd = fos.getFD();
			dos = new DataOutputStream(fos);
		} catch (Exception e) {
			e.printStackTrace();
		}
		final Button button = (Button) findViewById(R.id.brightness_testButton);
		button.setText(R.string.test_begin);
		button.setTextSize(25.0f);
		button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				button.setVisibility(View.GONE);
				mStatus.setVisibility(View.VISIBLE);
				b1.setVisibility(View.GONE);
				b2.setVisibility(View.GONE);
				j = 0;
				mHandler.post(runnable);
			}
		});
        setupBottom();
    }
    
  	Runnable runnable = new Runnable() {
  		public void run() {
  			Message msg = mHandler.obtainMessage();
  			msg.arg1 = j;
  			mHandler.sendMessage(msg);
  			if (j != 0) {
  				try {
  					Thread.sleep(1000);
  				} catch (InterruptedException e) {
  					e.printStackTrace();
  				}
  			}
  			j++;
  		}
  	};
  	private Handler mHandler = new Handler() {
  		public void handleMessage(Message msg) {
  			try {
  				Button button = (Button) findViewById(R.id.brightness_testButton);
  				Log.v("@@@@@@@@@@@@@", msg.arg1 + "");
  				//if (msg.arg1 > 6) {
  				if (msg.arg1 > 2) {//[yeez_haojie modfiy 12.17]
  					Log.v("#################", msg.arg1 + "");
  					mHandler.post(new Runnable() {
						
						public void run() {
							try {
							//	dos.writeUTF(brightness + "");
							//	dos.writeBytes(brightness + "");	//tsy
								dos.writeBytes("30");
								//fd.sync();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
  					mHandler.removeCallbacks(runnable);
  					button.setVisibility(View.GONE);
  					b1.setVisibility(View.VISIBLE);
  					b2.setVisibility(View.VISIBLE);
  					mStatus.setVisibility(View.VISIBLE);
  				} else if (msg.arg1 % 3 == 0) {
  				//	dos.writeUTF("255");
					dos.writeBytes("255");  		//tsy
  					Log.v("#################1", msg.arg1 + "  ,write value 255");
  					//fd.sync();
  					mStatus.setVisibility(View.VISIBLE);
  					button.setVisibility(View.GONE);
  					b1.setVisibility(View.GONE);
  					b2.setVisibility(View.GONE);
  					mHandler.post(runnable);
  				} else if (msg.arg1 % 3 == 1) {
  				//	dos.writeUTF("120");
					dos.writeBytes("120");  		//tsy
  					Log.v("#################1", msg.arg1 + "  ,write value 120");
  					//fd.sync();
  					mStatus.setVisibility(View.VISIBLE);
  					button.setVisibility(View.GONE);
  					b1.setVisibility(View.GONE);
  					b2.setVisibility(View.GONE);
  					mHandler.post(runnable);
  				} else {
  				//	dos.writeUTF("0");
					dos.writeBytes("0");  		//tsy
  					Log.v("#################1", msg.arg1 + "  ,write value 0");
  					//fd.sync();
  					mStatus.setVisibility(View.VISIBLE);
  					button.setVisibility(View.GONE);
  					b1.setVisibility(View.GONE);
  					b2.setVisibility(View.GONE);
  					mHandler.post(runnable);
  				}
			} catch (Exception e) {
				e.printStackTrace();
			}
  		}
  	};
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
    }
    
    private void setupBottom() {
        b1 = (Button) findViewById(R.id.testpassed);
        b1.setVisibility(View.GONE);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               mHandler.post(new Runnable() {
				
				public void run() {
					setResult(RESULT_OK);
					 BaseActivity.NewstoreRusult(true, "bright test",mEngSqlite);//[yeez_haojie add 11.30]
					try {
						dos.writeBytes(brightness + "");
						//fd.sync();
						fos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					finish();
				}
			});
            }
        });
       
        b2 = (Button) findViewById(R.id.testfailed);
        b2.setVisibility(View.VISIBLE);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               mHandler.post(new Runnable() {
   				
   				public void run() {
   					setResult(RESULT_FIRST_USER);
   					BaseActivity.NewstoreRusult(false, "bright test",mEngSqlite);//[yeez_haojie add 11.30]
   					try {
   						dos.writeBytes(brightness + "");
						//fd.sync();
						fos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
   					finish();
   				}
   			});
            }
        });
     }
}