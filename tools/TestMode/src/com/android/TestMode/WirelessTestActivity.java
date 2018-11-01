package com.android.TestMode;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.Thread;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class WirelessTestActivity extends Activity implements OnClickListener
{
    /** Called when the activity is first created. */
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2; 
//    BluetoothAdapter mBluetoothAdapter;
    private TextView mText1; 
//    private TextView mText2; 
    private Button mButton1;
//    private Button mButton2;
    
    boolean bWifiTestResult = false;
//    boolean bBTTestResult = false;
    private final Timer timer = new Timer();
    private TimerTask task;
    private WifiManager wifiManager;
    private List<ScanResult> list;
    public EngSqlite mEngSqlite;//[yeez_haojie add 11.26]
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wireless);
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.26]		
        
				mText1 = (TextView) findViewById(R.id.Wifi_Prompt); 
//				mText2 = (TextView) findViewById(R.id.BT_Prompt); 
				mButton1 = (Button) findViewById(R.id.btnWifi);
				mButton1.setTextSize(25.0f);
//				mButton2 = (Button) findViewById(R.id.btnBT);
				mButton1.setOnClickListener(this);
//				mButton2.setOnClickListener(this);
				
//				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				setupBottom();
    }
    
    private void showResult() {
    	
    	
    	list = wifiManager.getScanResults();
    	
    	/*WifiInfo info = wifiManager.getConnectionInfo();
    	int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);*/
    	
    	if (list != null) {
    		if (list.size() == 0 ) {
    			mText1.setText(R.string.end);
    			mText1.append("\n" + getString(R.string.NoWlan));
    			bWifiTestResult = false;
    		} else {
    			mText1.setText(R.string.end);
    			mText1.append("\n" + getString(R.string.FindWlan));
    			for (int i = 0; i < list.size(); i++) {
    				mText1.append("\n" + list.get(i).SSID);
    				mText1.append(" level:" + list.get(i).level);//[yeez_haojie add 12.26]
    				Log.v("WirelessTestActivity", "level"+i+":"+list.get(i).level);
    				
    			}
    		 	mText1.append("\n" + getString(R.string.Test_Pass));
    		  bWifiTestResult = true;
    		}
    		wifiManager.setWifiEnabled(false);
    		if (bWifiTestResult)
    		{
    			TestResult(true);
    			Button b = (Button) findViewById(R.id.testpassed);
    	        b.setVisibility(View.VISIBLE);
    		}
    		handler.removeMessages(0x0001);
    	}
	}
    
    public Handler handler = new Handler(){
		public void handleMessage(Message message){
			switch(message.what){
					case 0x0001:
					{
						showResult();
					}
			}
		}
    };
    
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			switch (v.getId()) 
			{
				case R.id.btnWifi:
				{
					wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
					wifiManager.setWifiEnabled(true);
					if (WifiManager.WIFI_STATE_UNKNOWN != wifiManager.getWifiState()) {
						
						/*WifiInfo info = wifiManager.getConnectionInfo();
				    	int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);
				    	Log.v("WirelessTestActivity", "strength :"+strength);*/
						
//						wifiManager.startScan();
						mText1.setText(R.string.Test_ing);
//						list = wifiManager.getScanResults();
						task = new TimerTask() {
							@Override
							public void run() {
								handler.sendEmptyMessage(0x0001);
							}
						};
						timer.schedule(task, 8000);
					} else {
						bWifiTestResult = false;
						mText1.setText(R.string.Test_Fail);
//						mText1.append(getString(R.string.Test_Fail));
						TestResult(false);
						wifiManager.setWifiEnabled(false);
						if (bWifiTestResult)
						{
							TestResult(true);
						}
					}
				}
//				case R.id.btnBT:
//				{
//					mText2.setText(R.string.Test_ing);
//					threadBT();
//					break;
//				}
			}
		}
	 
//	public void threadBT()
//	{
//		new Thread(){
//		public void run(){
//			Message msg_listData = new Message();
//			msg_listData.what = 0x0001;
//			handler.sendMessage(msg_listData);
//			}
//		}.start();
//	}
	
//	private Handler handler = new Handler(){
//		public void handleMessage(Message message){
//			switch(message.what){
//				case 0x0001:
//					BT_Test();
//					break;
//				}
//				//super.handleMessage(message);
//			}
//		};
		
//		public boolean BT_Test()
//		{
//  		if (mBluetoothAdapter == null) 
//			{   
//				 // Device does not support Bluetooth}
//				  mText2.setText(R.string.Test_Fail);
//				  bBTTestResult  = false;
//				  TestResult(false);
//				  return false;
//			} 
//			
//			if (mBluetoothAdapter.isEnabled()) 
//			{
//					bBTTestResult  = true;
//					mText2.setText(R.string.Test_Pass);
//					if ((bWifiTestResult)&&(bBTTestResult))
//					{
//						TestResult(true);
//					}
//					return true;
//			}
			
/*
			if (mBluetoothAdapter.isEnabled()) 
			{
				mBluetoothAdapter.disable();
				while (!mBluetoothAdapter.isEnabled())
				{
					try {
						Thread.sleep(1000);
						} 
						catch (InterruptedException e) {
						}
				}
			}
*/			
//		Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//		startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//			if (mBluetoothAdapter.enable())
//			{
//					bBTTestResult  = true;
//					mText2.setText(R.string.Test_Pass);
//					if ((bWifiTestResult)&&(bBTTestResult))
//					{
//						TestResult(true);
//					}
//					return true;
//			}
//			else
//			{
//				bBTTestResult  = false;
//				mText2.setText(R.string.Test_Fail);
//				TestResult(false);
//			}
			/*for (int i=0; i<100; i++)
			{
				if (mBluetoothAdapter.isEnabled())  
				{
					bBTTestResult  = true;
					mText2.setText(R.string.Test_Pass);
					if ((bWifiTestResult)&&(bBTTestResult))
					{
						TestResult(true);
					}
					return true;
				}
				try {
				Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
				}

			}
			if (!bBTTestResult)
			{
				mText2.setText(R.string.Test_Fail);
				TestResult(false);
			} 
			*/
//			return false; 
//		}
		
		public void TestResult(boolean bResult)
		{
//			if (mBluetoothAdapter.isEnabled()) 
//					mBluetoothAdapter.disable() ;
			if (bResult)
			{
				//final Intent intent = new Intent(WirelessTestActivity.this, CameraTestActivity.class);
				//startActivity(intent);
//				setResult(RESULT_OK);
//				finish();
			}
			else
			{
				//Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
//				setResult(RESULT_FIRST_USER);
//				finish();
			}
		}
	
	
		@Override  
    protected void onDestroy() 
    {  
//				if (mBluetoothAdapter.isEnabled()) 
//					mBluetoothAdapter.disable() ;
        super.onDestroy();  
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
	               setResult(RESULT_OK);
	               BaseActivity.NewstoreRusult(true, "Wifi test",mEngSqlite);//[yeez_haojie add 11.26]
	               finish();
	            }
	        });
	       
	        b = (Button) findViewById(R.id.testfailed);
	        b.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	               //failed
	               setResult(RESULT_FIRST_USER);
	               BaseActivity.NewstoreRusult(false, "Wifi test",mEngSqlite);//[yeez_haojie add 11.26]
	               finish();
	            }
	        });
	     }
}