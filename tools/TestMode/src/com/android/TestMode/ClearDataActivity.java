package com.android.TestMode;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.content.pm.IPackageDataObserver;
import com.android.TestMode.EngSqlite;

public class ClearDataActivity extends Activity implements FileFilter{
	private static final String TAG = "ClearDataActivity";
	private static final int DIALOG_MULTIPLE_CHOICE_DELETE = 1;
	
	static final String INTENT_CAT_NAME = "category";//[yeez_haojie add 11.28]
	   static final String CATEGORY_TEST = "android.intent.category.TEST_MODE";//[yeez_haojie add 11.28]
	   static final String CATEGORY_PCBATEST = "android.intent.category.PCBATEST_MODE";//[yeez_haojie add 11.28]
	
	private static final int CLEAR_DATA = 0;
	private static final int CLOSE_MODULE = 1;
	
	private ClearUserDataObserver mClearDataObserver = null;
	private boolean clearData = true;
	private boolean closeModule = true;
	
	private ActivityManager am;
	private WifiManager wifiManager;
	private BluetoothAdapter adapter;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_MULTIPLE_CHOICE_DELETE:
			return new AlertDialog.Builder(ClearDataActivity.this)
            .setIcon(R.drawable.ic_launcher)
            .setTitle(R.string.clear_status)
            .setMultiChoiceItems(R.array.clear_data,
                    new boolean[]{true, true},
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton,
                                boolean isChecked) {
                        	switch(whichButton){
                        	case CLEAR_DATA:
                        		clearData = isChecked;
                        		break;
                        	case CLOSE_MODULE:
                        		closeModule = isChecked;
                        		break;
                        	}
                        }
                    })
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    /* User clicked Yes so do some stuff */
                	if(clearData){
                		//clear Contacts
                		
                        am.clearApplicationUserData("com.android.providers.contacts", mClearDataObserver);
                        
                        // SMS,Mms
                        am.clearApplicationUserData("com.android.providers.telephony", mClearDataObserver);

                        // Media
                        am.clearApplicationUserData("com.android.providers.media", mClearDataObserver);
                        
                        // Settings
                        am.clearApplicationUserData("com.android.settings", mClearDataObserver);
                        
                        // Calendar
                        am.clearApplicationUserData("com.android.providers.calendar", mClearDataObserver);
                        
                        // Application
                        am.clearApplicationUserData("com.android.providers.applications", mClearDataObserver);
                        
                        // Download
                        am.clearApplicationUserData("com.android.providers.downloads", mClearDataObserver);
                        
                        //++ by [yeez_liuwei] 2012.11.10
                        //FM
                    	File file = new File("/sdcard/FmChannels.txt");
                    	if(file.exists())
                    		file.delete();
                    	
                    	//TV
                    	cleanTvData();
                    	// -- by [yeez_liuwei] 2012.11.10
                	}
                	
                	if(closeModule){
                		// clear wifi saveconfig
                		List<WifiConfiguration> config = (List<WifiConfiguration>) wifiManager.getConfiguredNetworks();
                		for(int i = 0; i < config.size(); i++){
                			wifiManager.removeNetwork(config.get(i).networkId);
                		}
                		wifiManager.saveConfiguration();
                		
                		// close wifi
                		if ((wifiManager != null) && (wifiManager.isWifiEnabled())) {  
                			wifiManager.setWifiEnabled(false);  
                		}
                		
                		// close wifiap
                		WifiConfiguration wifiap = wifiManager.getWifiApConfiguration();
                		wifiap.SSID = getString(com.android.internal.R.string.wifi_tether_configure_ssid_default);
                		wifiManager.setWifiApConfiguration(wifiap);
                		wifiManager.saveConfiguration();

                		if(wifiManager.WIFI_AP_STATE_ENABLED == wifiManager.getWifiApState()){
                			wifiManager.setWifiApEnabled(wifiap, false);  
                		}

                		// close bluetooth
                		if((adapter != null) && (!(adapter.getState() == BluetoothAdapter.STATE_OFF))){
                			adapter.disable();
                		}
                		
                		//close GPS
                		Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                                LocationManager.GPS_PROVIDER, false);
                		//finish();
                	}
                	ClearDataActivity.this.finish();
                }
            })
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked No so do some stuff */
                	ClearDataActivity.this.finish();
                }
            })
           .create();
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);  
		adapter = BluetoothAdapter.getDefaultAdapter();
		 
		//showDialog(DIALOG_MULTIPLE_CHOICE_DELETE); //[yeez_haojie modify 2013.3.14]
		//clearDatacloseModule();//[yeez_haojie add 2013.3.14]
		if (mClearDataObserver == null) {
            mClearDataObserver = new ClearUserDataObserver();
        }
		
		clearDatacloseModule();//[yeez_haojie add 2013.3.18]
		
		init();
        int isFullTest = getIntent().getIntExtra("isFullTest", 0);
        int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId", 0);
        setIsFullTest(isFullTest, ++fullTestActivityId);
		
	}

	class ClearUserDataObserver extends IPackageDataObserver.Stub {
       public void onRemoveCompleted(final String packageName, final boolean succeeded) {
    	   Log.v(TAG, "[yeez_jinwei]succeeded = " + succeeded);
        }
    }
    // ++ by [yee_liuwei] 2012.11.9
    public void cleanTvData(){
    	File tvFile = new File("data/data/com.maxscend.player");
    	deleteAll(tvFile);
    }
    public void deleteAll(File file){
    	File[] files = file.listFiles(this);
    	if(files==null)
    		return;
    	for(int i=0;i<files.length;i++){
    		files[i].delete();
    	}
    }
	@Override
	public boolean accept(File pathname) {
		if(pathname.isFile())
			return true;
		else
			deleteAll(pathname);
		return false;
	}
	// -- by [yeez_liuwei] 2012.11.9
	
	//-----from  baseActivity--------------add by [yeez_liuwei] 2012.11.7----------------------
    protected String mTestCaseName = null;
    protected int isFullTest = 0;
    protected int fullTestActivityId;
    protected int mGroupId;


    public String getmTestCaseName() {
        return mTestCaseName;
    }

    public int getTestCaseGroupId(){
    	return mGroupId;
    }

    public void setTestCaseGroupId(int groupId){
    	this.mGroupId = groupId;
    }

    public void setIsFullTest(int isFullTest, int fullTestActivityId){
    	this.isFullTest = isFullTest;
    	this.fullTestActivityId = fullTestActivityId;
    }
    public void setmTestCaseName(String mTestCaseName) {
        this.mTestCaseName = mTestCaseName;
    }
    public EngSqlite mEngSqlite;
   
    protected void init() {
//      mWindowManager = (WindowManager)getSystemService("window");
        int groupId = this.getIntent().getIntExtra("groupId", 0);
        String testName = this.getIntent().getStringExtra("testname");

        this.setTestCaseGroupId(groupId);
        setmTestCaseName(testName);
        mEngSqlite = EngSqlite.getInstance(this);


    }

   @Override
   public void finish() {
	   if(isFullTest == 1){
		   Intent intent = new Intent(this, Const.FULL_TEST_ACTIVITY_ARRAY[fullTestActivityId]);
		   intent.putExtra("isFullTest", 1);
		   intent.putExtra("fullTestActivityId", fullTestActivityId);
		   intent.putExtra("testname",Const.TEST_CASE_ARRAY[fullTestActivityId]);
		   intent.putExtra("groupId", Const.TEST_GROUP_ID[fullTestActivityId]);

		   isFullTest = 0;
		   this.startActivity(intent);
	   }
	   super.finish();
   }
   // -- by [yeez_liuwei] 2012.11.7
   
   
   /**
    * [yeez_haojie add 2013.3.14]
    */
   
   private void deleteTargetFiles(File target) {
	   Log.d("ClearDataActivity", "deleteTargetFiles(), target is " + target.getAbsolutePath());
	   if(target.exists()) {
		   if(target.isDirectory()) {
			   File[] files = target.listFiles();
			   for(File f : files) {
				   deleteTargetFiles(f);
			   }
		   }
		   target.delete();
	   }
   }
   
   public void clearDatacloseModule(){
	 //clear Contacts
		
       am.clearApplicationUserData("com.android.providers.contacts", mClearDataObserver);
       
       // SMS,Mms
//     am.clearApplicationUserData("com.android.providers.telephony", mClearDataObserver);   // the phone database cannot be deleted [yeez_liuwei]

       // Media
       am.clearApplicationUserData("com.android.providers.media", mClearDataObserver);
       
       // Settings
       am.clearApplicationUserData("com.android.settings", mClearDataObserver);
       
       // Calendar
       am.clearApplicationUserData("com.android.providers.calendar", mClearDataObserver);
       
       // Application
       am.clearApplicationUserData("com.android.providers.applications", mClearDataObserver);
       
       // Download
       am.clearApplicationUserData("com.android.providers.downloads", mClearDataObserver);
       
       //++ by [yeez_liuwei] 2012.11.10
       //FM
   	File file = new File("/sdcard/FmChannels.txt");
   	if(file.exists())
   		file.delete();
   	
   	//TV
   	cleanTvData();
   	
   	//clean camera and video files
   	deleteTargetFiles(new File("/sdcard/DCIM"));//[yeez_yuxiang.zhang added 2013.5.23]
   	
   	
 // clear wifi saveconfig
	List<WifiConfiguration> config = (List<WifiConfiguration>) wifiManager.getConfiguredNetworks();
	if(config != null) {
	for(int i = 0; i < config.size(); i++){
		wifiManager.removeNetwork(config.get(i).networkId);
	}
	wifiManager.saveConfiguration();
	
	// close wifi
	if ((wifiManager != null) && (wifiManager.isWifiEnabled())) {  
		wifiManager.setWifiEnabled(false);  
	}
	}
	// close wifiap
	WifiConfiguration wifiap = wifiManager.getWifiApConfiguration();
	wifiap.SSID = getString(com.android.internal.R.string.wifi_tether_configure_ssid_default);
	wifiManager.setWifiApConfiguration(wifiap);
	wifiManager.saveConfiguration();

	if(wifiManager.WIFI_AP_STATE_ENABLED == wifiManager.getWifiApState()){
		wifiManager.setWifiApEnabled(wifiap, false);  
	}

	// close bluetooth
	if((adapter != null) && (!(adapter.getState() == BluetoothAdapter.STATE_OFF))){
		adapter.disable();
	}
	
	//close GPS
	Settings.Secure.setLocationProviderEnabled(getContentResolver(),
            LocationManager.GPS_PROVIDER, false);
   	
	ClearDataActivity.this.finish();
	
   }
}
