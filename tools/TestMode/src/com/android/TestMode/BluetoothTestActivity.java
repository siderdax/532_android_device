package com.android.TestMode;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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


public class BluetoothTestActivity extends Activity implements OnClickListener
{
    /** Called when the activity is first created. */
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2; 
    BluetoothAdapter mBluetoothAdapter;
    public TextView mText1; 
    public TextView mText2; 
    public Button mButton2;

    boolean bFindDevice = false;
    boolean bBTTestResult = false;
    boolean bShowPass = false;
    boolean bRun = false;
    public EngSqlite mEngSqlite;//[yeez_haojie add 11.26]
    
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.26]
        
	mText1 = (TextView) findViewById(R.id.BT_Address); 
	mText2 = (TextView) findViewById(R.id.BT_Prompt); 
	mButton2 = (Button) findViewById(R.id.btnBT);
	mButton2.setTextSize(25.0f);
	mButton2.setOnClickListener(this);
	setupBottom();
	
	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
	registerReceiver(mReceiver, filter);
	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
	registerReceiver(mReceiver, filter);

    }
    
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			switch (v.getId()) 
			{
				case R.id.btnBT:
				{
					if (!bRun)
					{
						mText2.setText(R.string.Test_ing);
						bRun = true;
						BT_Test();
					}
					break;
				}
			}
		}

	public Handler handler = new Handler(){
		public void handleMessage(Message message){
			switch(message.what){
					case 0x0001:
					{
						if (mBluetoothAdapter.isEnabled())
						{
							if (!mBluetoothAdapter.startDiscovery())
								break;
							if (mBluetoothAdapter.getAddress().equals( "06:05:04:03:02:01"))
							{
								mText1.setText(R.string.NoMAC);
							}
							else
							{
								bShowPass = true;
								mText1.setText(String.format(getString(R.string.MAC), mBluetoothAdapter.getAddress()));
							}
							bBTTestResult = true;
							handler.removeMessages(0x0001);
						}
						break;
					}
				}
				//super.handleMessage(message);
			}
		};
		
		public boolean BT_Test()
		{
  			if (mBluetoothAdapter == null) 
			{   
				 // Device does not support Bluetooth}
				  mText2.setText(R.string.Test_Fail);
				  bBTTestResult  = false;
				  TestResult(false);
				  return false;
			} 

			if (!mBluetoothAdapter.isEnabled()) 
			{
				mBluetoothAdapter.enable();
			}

			new getInfoThread().start();
			return true; 
		}

		
		public void TestResult(boolean bResult)
		{
			if (mBluetoothAdapter.isEnabled()) 
					mBluetoothAdapter.disable() ;
			if (bResult)
			{
				//final Intent intent = new Intent(WirelessTestActivity.this, CameraTestActivity.class);
				//startActivity(intent);
				setResult(RESULT_OK);
				BaseActivity.NewstoreRusult(true, "Bt test",mEngSqlite);//[yeez_haojie add 11.26]
				finish();
			}
			else
			{
				//Toast.makeText(this, "FAILED", Toast.LENGTH_SHORT).show();
				setResult(RESULT_FIRST_USER);
				BaseActivity.NewstoreRusult(false, "Bt test",mEngSqlite);//[yeez_haojie add 11.26]
				finish();
			}
		}
	
	
		@Override  
	    protected void onDestroy() 
	    {  
					if (mBluetoothAdapter.isEnabled()) 
						mBluetoothAdapter.disable() ;
	        super.onDestroy();  
	    }
		
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				moveTaskToBack(true);
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}

			BroadcastReceiver mReceiver = new BroadcastReceiver() 
			{
				public void onReceive(Context context, Intent intent) 
				{
					if (bRun)
					{
						String action = intent.getAction();
						//Find device
						if (BluetoothDevice.ACTION_FOUND.equals(action)) 
						{
							BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
							//if (device.getBondState() != BluetoothDevice.BOND_BONDED)
							if (null != device && !"null".equals(device.getName()))
							{
								mText2.setText(String.format(getString(R.string.FindDevice), device.getName()));
								//bFindDevice = true;
								if (bShowPass)
								{
									Button b = (Button) findViewById(R.id.testpassed);
									b.setVisibility(View.VISIBLE);
									bRun = false;
								}
							}
						}
						//Find over
						else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
						{
							if (!bFindDevice)
								mText2.setText(R.string.NoDevice);
							bRun = false;
							bBTTestResult = false;
						}
					}
				}
			};

	class getInfoThread extends Thread
	{
		@Override
		public void run()
		{
			while (!bBTTestResult)
			{
				handler.sendEmptyMessage(0x0001);
				try
				{
					Thread.sleep(1000);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	};

	private void setupBottom() {
       Button b = (Button) findViewById(R.id.testpassed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //passed
              unregisterReceiver(mReceiver);
              setResult(RESULT_OK);
              BaseActivity.NewstoreRusult(true, "Bt test",mEngSqlite);//[yeez_haojie add 11.26]
              finish();
           }
       });
       
       b = (Button) findViewById(R.id.testfailed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //failed
              unregisterReceiver(mReceiver);
              setResult(RESULT_FIRST_USER);
              BaseActivity.NewstoreRusult(false, "Bt test",mEngSqlite);//[yeez_haojie add 11.26]
              finish();
           }
       });
    }

}





