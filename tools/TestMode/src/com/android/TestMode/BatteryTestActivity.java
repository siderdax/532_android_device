package com.android.TestMode;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.view.KeyEvent;
import android.view.View;

import com.android.internal.app.IBatteryStats;

public class BatteryTestActivity extends Activity {
	private TextView mStatus;
	private ListView mlist;
	private IBatteryStats mBatteryStats;
    private IPowerManager mScreenStats;
    private SimpleAdapter mbatterySimple;
    private static final int EVENT_TICK = 1;
    private static final int BATTERY_CAPACITY = 2900;
    
    private File file;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private DataInputStream dis;
    private FileOutputStream fos;
    private DataOutputStream dos;
    
    private Timer mTimer = new Timer();
    private TimerTask mTimerTask;
    private String mStr;
    private String mStr1;
    public EngSqlite mEngSqlite;//[yeez_haojie add 11.27]
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_TICK:
                    sendEmptyMessageDelayed(EVENT_TICK, 1000);
                    
                    break;
            }
        }
    };
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.xml.battery_test);
        mStatus = (TextView)findViewById(R.id.status);
		mlist = (ListView) findViewById (R.id.BatteyInfoView);
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.27]	
     // create the IntentFilter that will be used to listen
        // to battery status broadcasts
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        setupBottom();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// Get awake time plugged in and on battery
        mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batteryinfo"));
        mScreenStats = IPowerManager.Stub.asInterface(ServiceManager.getService(POWER_SERVICE));
        mHandler.sendEmptyMessageDelayed(EVENT_TICK, 1000);
        
        registerReceiver(mIntentReceiver, mIntentFilter);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeMessages(EVENT_TICK);
        
        // we are no longer on the screen stop the observers
        unregisterReceiver(mIntentReceiver);
    }
    
    private IntentFilter   mIntentFilter;
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				int plugType = intent.getIntExtra("plugged", 0);
				int status = intent.getIntExtra("status",
						BatteryManager.BATTERY_STATUS_UNKNOWN);
				int health = intent.getIntExtra("health", 0);
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 0);
				int temperature = intent.getIntExtra("temperature", 0);
				int voltage = intent.getIntExtra("voltage", 0);
				String technology = intent.getStringExtra("technology");
				
				String statusString;
				if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
					mStatus.setTextColor(Color.GREEN);
					statusString = getString(R.string.battery_info_status_charging);
					if (plugType > 0) {
						statusString = statusString
								+ " "
								+ getString((plugType == BatteryManager.BATTERY_PLUGGED_AC) ? R.string.battery_info_status_charging_ac
										: R.string.battery_info_status_charging_usb);
					}
					if (plugType == BatteryManager.BATTERY_PLUGGED_USB) {
						Button b = (Button) findViewById(R.id.testpassed);
						b.setVisibility(View.VISIBLE);
					}
				} else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
					mStatus.setTextColor(Color.RED);
					statusString = getString(R.string.battery_info_status_discharging);
					Button b = (Button) findViewById(R.id.testpassed);
					b.setVisibility(View.GONE);
				} else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
					mStatus.setTextColor(Color.GREEN);
					statusString = getString(R.string.battery_info_status_not_charging);
				} else if (status == BatteryManager.BATTERY_STATUS_FULL) {
					mStatus.setTextColor(Color.GREEN);
					statusString = getString(R.string.battery_info_status_full);
				} else {
					mStatus.setTextColor(Color.GREEN);
					statusString = getString(R.string.battery_info_status_unknown);
				}
				mStatus.setTextSize(25.0f);
				mStatus.setText(statusString);
				if (statusString
						.equals(getString(R.string.battery_info_status_charging)
								+ " "
								+ getString(R.string.battery_info_status_charging_ac))) {
					try {
			        	file = new File("/sys/devices/platform/pxa2xx-i2c.0/i2c-0/0-0034/88pm860x-battery.0/power_supply/battery/voltage_now");
			        	//fos = new FileOutputStream(file);
			        	//dos = new DataOutputStream(fos);
			        	//dos.writeChars("2748");
			        	//dos.close();
			        	fis = new FileInputStream(file);
			        	bis = new BufferedInputStream(fis);
			        	dis = new DataInputStream(bis);
			        	mStr = dis.readLine();
			        	dis.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
//					mStatus.append("\n" + mStr.substring(0, mStr.length() - 3) + "mV"); //[yeez_haojie modify 11.14]
					mTimerTask = new TimerTask() {

						@Override
						public void run() {
							try {
								//fos = new FileOutputStream(file);
								//dos = new DataOutputStream(fos);
								//dos.writeChars("2748");
								fis = new FileInputStream(file);
								bis = new BufferedInputStream(fis);
								dis = new DataInputStream(bis);
								mStr1 = dis.readLine();
								dis.close();
								mHandler.post(new Runnable() {
									
									public void run() {
										mStatus.append("\n" + mStr1.substring(0, mStr.length() - 3) + "mV");
										/*try {
											dos.writeChars("3258");
											dos.close();
										} catch (Exception e) {
											e.printStackTrace();
										}*/
										Button b = (Button) findViewById(R.id.testpassed);
										b.setVisibility(View.VISIBLE);
									}
								});
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					};
					mTimer.schedule(mTimerTask, 1000 * 5);
				}
				
			String healthString = "";
			
			switch (health) {
			case BatteryManager.BATTERY_HEALTH_UNKNOWN:
				healthString = getString(R.string.battery_info_health_unknown);
			    break;
			case BatteryManager.BATTERY_HEALTH_GOOD:
				healthString = getString(R.string.battery_info_health_good);
				break;
			case BatteryManager.BATTERY_HEALTH_OVERHEAT:
				healthString = getString(R.string.battery_info_health_overheat);
				break;
			case BatteryManager.BATTERY_HEALTH_DEAD:
				healthString = getString(R.string.battery_info_health_dead);
				break;
			case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
				healthString = getString(R.string.battery_info_health_voltage);
				break;
			case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
				healthString = getString(R.string.battery_info_health_unspecified_failure);
				break;
			case BatteryManager.BATTERY_HEALTH_COLD:
				healthString = getString(R.string.battery_info_health_cold);
				break;
			}
			
			String greenstr = healthString;
			String greenstr2 = String.valueOf(scale * BATTERY_CAPACITY / 100) + "mAh";
			String greenstr3 = String.valueOf(level * BATTERY_CAPACITY / 100) + "mAh";
			String greenstr4 = String.valueOf((float)temperature / 10);
			String greenstr5 = String.valueOf((float)voltage / 1000) + "V";
			String greenstr6 = technology;
			
			String[] names = new String[]	{ 
					getString(R.string.battery_info_health), 
					getString(R.string.battery_info_maximum_battery_level), 
					getString(R.string.battery_info_current_battery_level),
					getString(R.string.battery_info_battery_temperature), 
					getString(R.string.battery_info_battery_voltage),
					getString(R.string.battery_info_battery_technology)
					};
			
			String[] descs = new String[] {
					greenstr, greenstr2, greenstr3, greenstr4, greenstr5, greenstr6
			};
			
			List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
			for (int i = 0; i < names.length; i++)
			{
				Map<String, Object> listItem = new HashMap<String, Object>();
				listItem.put ("names", names[i]);
				listItem.put ("desc", descs[i]);
				listItems.add (listItem);
			}
			mbatterySimple = new SimpleAdapter (BatteryTestActivity.this, listItems, R.xml.battery_item,
					new String [] {"names", "desc"}, new int [] {R.id.battery_name, R.id.battery_desc});
			mlist.setAdapter (mbatterySimple);
			
			}
		}
	};
    
    private void setupBottom() {
        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               setResult(RESULT_OK);
               BaseActivity.NewstoreRusult(true, "Charge test",mEngSqlite);//[yeez_haojie add 11.27]
				try {
					dis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "Charge test",mEngSqlite);//[yeez_haojie add 11.27]
               try {
					dis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
               finish();
            }
        });
     }
}
