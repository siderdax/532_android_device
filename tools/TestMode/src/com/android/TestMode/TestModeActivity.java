/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.TestMode;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.internal.telephony.PhoneFactory;
import com.android.TestMode.EngSqlite;//[yeez_haojie add 11.22]


public class TestModeActivity extends ListActivity {
		BluetoothAdapter mBluetoothAdapter;
	   private enum TestState {
	        NotTested,
	        TestFailed,
	        TestScuessed
	    }
	//private View mCurrentTestView;
	   static final String ALL_PASSED = "99";
	   static final String INTENT_CAT_NAME = "category";
	   static final String CATEGORY_TEST = "android.intent.category.TEST_MODE";
	   static final String CATEGORY_PCBATEST = "android.intent.category.PCBATEST_MODE";
	   
	private int mStep;
	private static Map<Integer, TestState> mTestStateItems = new HashMap<Integer, TestState>();
	private List<Map<String, Object>> mTestList;
	private String mCategaryName;
	private String mAppName;
	private static final String TAG = "TestModeActivity";
	private boolean mGpsEnabled;
	private WifiManager wifiManager;
	private EngSqlite mEngSqlite;//[yeez_haojie add 11.22]
	Intent gpsintent;//[yeez_haojie add 11.29]
	  private static class MySimpleAdapter extends SimpleAdapter {

			public MySimpleAdapter(Context context,
					List<? extends Map<String, ?>> data, int resource,
					String[] from, int[] to) {
				super(context, data, resource, from, to);
				
			}
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				Log.d(TAG,"getView position:"+" state:"+mTestStateItems.get(position)+position+" view:"+v);
				if((TestState.TestScuessed == mTestStateItems.get(position))){
					v.setBackgroundColor(Color.GREEN);
				}else if((TestState.TestFailed == mTestStateItems.get(position))){
					v.setBackgroundColor(Color.RED);
				}else {
					v.setBackgroundColor(Color.BLACK);
				}
//				ViewItems.put(position,v);
				return v;
			}
		
	  }
	  
	// add by zhai 
	private String[] testModeToUse;
	private boolean testMode=true;// true means---> test all ,false means--->test single
	private AlertDialog.Builder builder;
	//ended by zhai
	
	private void setListView(List l) {
		setListAdapter(new MySimpleAdapter(this, l,
				android.R.layout.simple_list_item_1, new String[] { "title" },
				new int[] { android.R.id.text1 }));
	}
	  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.22]
        
        testModeToUse=new String[]{this.getString(R.string.test_all_lable),this.getString(R.string.test_single_lable),
        		this.getString(R.string.test_result_lable)};//[yeez_haojie add 11.26]
        resolveIntent();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Intent intent = getIntent();
        String path = intent.getStringExtra("com.example.android.apis.Path");
        
        if (path == null) {
            path = "";
        }
        Log.d(TAG, "path="+path);
        mTestList = getData(path);

        for (int i = 0; i < mTestList.size(); i++) {
        	mTestStateItems.put(i, TestState.NotTested);
        }
        mStep=0;
        Log.d(TAG,"onListItemClick mStep:"+mStep);
        
        intent = (Intent) mTestList.get(mStep).get("intent");
//        startActivity(intent);
		mGpsEnabled = Settings.Secure.isLocationProviderEnabled(getContentResolver(),
                LocationManager.GPS_PROVIDER);
        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                LocationManager.GPS_PROVIDER, true);
        Log.d("leo","onCreate mGpsEnabled:"+mGpsEnabled);
        //[yeez_yuxiang.zhang added 2013.5.2 start. start locating at first]
        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = lm.getBestProvider(criteria, true);
//        android.location.Location location = lm.getLastKnownLocation(provider); 
    	lm.requestLocationUpdates(provider, 1*1000, 100, new LocationListener() {
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onProviderDisabled(String provider) {}
			@Override
			public void onLocationChanged(Location location) {}
		});  
    	//[yeez_yuxiang.zhang added 2013.5.2 end. start locating at first]
    	
        wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
		wifiManager.setWifiEnabled(true);
		mBluetoothAdapter.enable();
//        startActivityForResult(intent, 0);// unused by zhai
//        onListItemClick(null,null,0,0);
        showSelectOptions(intent);
    }
    
    //add by zhai
    private void showSelectOptions(Intent intent){
    	final Intent mIntent=intent;
    	builder=new AlertDialog.Builder(this).setCancelable(true);//[yeez_haojie add 2.7];
    	builder.setTitle(R.string.app_testmode);
    	builder.setItems(testModeToUse, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
					if(which==0){
						startTestAllMode(mIntent);
					}
					else if(which==1)
					{
						startTestSingleMode(mIntent);
					}
					else if(which==2)
					{
						startTestResult(mIntent);//[yeez_haojie add 11.26]
					}
					else
					{
						startClearData(mIntent);//[yeez_haojie add 11.27]
					}
			}
		});
    	builder.show();
    }
    
    private void startTestAllMode(Intent intent){
	   	 mStep=0;
	   	 testMode=true;
	   	 intent = (Intent) mTestList.get(mStep).get("intent");
	   	 startActivityForResult(intent, 0);
   }
    
    /**
     * yeez_haojie add 11.26
     * @param intent
     */
    private void startTestResult(Intent intent){   	
    	 Intent mIntent=new Intent();
    	 mIntent.setClass(TestModeActivity.this, com.android.TestMode.TestResultActivity.class);
    	 //startActivity(mIntent);
    	 //startActivityForResult(mIntent, 1);//[yeez_haojie modify 2013.2.20]
    	 startActivity(mIntent);//[yeez_haojie add 2013.2.20]
    	 TestModeActivity.this.finish();//[yeez_haojie add 2013.2.20]
    }
    
    /**
     * yeez_haojie add 11.29
     * @param intent
     */
    private void startClearData(Intent intent){   	
    	 Intent mIntent=new Intent();
    	 mIntent.setClass(TestModeActivity.this, com.android.TestMode.ClearDataActivity.class);
    	 //startActivity(mIntent);
    	 startActivityForResult(mIntent, 2);
    	 
    }
    
    private void startTestSingleMode(Intent intent){
    	// show ListView
    	setListView(mTestList);
    	testMode=false;
    	getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent mIntent=new Intent();
				mStep=position;
				mIntent = (Intent) mTestList.get(position).get("intent");
				
				Log.v(TAG, "mIntent: "+mIntent.toString());
				Log.v(TAG, "mIntent.getComponent().getClassName(): "+mIntent.getComponent().getClassName());
				//Log.v(TAG, "mIntent.getClass(): "+mIntent.getClass());
				
				/*if(mIntent.getComponent().equals("com.android.TestMode.TestResultActivity"))
				{
					startActivity(mIntent);
				}*/
				if(mIntent.getComponent().getClassName().equals("com.android.TestMode.TestResultActivity"))
				{
					startActivity(mIntent);
				}
				else if(mIntent.getComponent().getClassName().equals("com.android.TestMode.ClearDataActivity"))
				{
					startActivity(mIntent);
				}
				//  cmp=com.chartcross.gpstest/.GPSTest} from pid 1780
				// com.android.TestMode.GpsTestActivity
				
				//[yeez_haojie add 11.28 st]
				else if(mIntent.getComponent().getClassName().equals("com.android.TestMode.GpsTestActivity"))
				{
					/* Intent intent = new Intent();
			  		  intent.setClassName("com.chartcross.gpstest", "com.chartcross.gpstest.GPSTest");
					//startActivity(intent);
			  		gpsintent = mIntent;
					//startActivityForResult(intent, 3);*/ //[yeez_haojie modify 12.19 end]
					startActivityForResult(mIntent, 0); //[yeez_haojie modify 12.19 end]
					//[yeez_haojie add 11.28 end]
				}
				else
				{
					startActivityForResult(mIntent, 0);
				}
			}
		});
    }//ended by zhai
    
    private void resolveIntent() {
        Intent intent = getIntent();
        mCategaryName=intent.getStringExtra(INTENT_CAT_NAME);
        if(CATEGORY_TEST.equals(mCategaryName)){
     	   mAppName =getListView().getContext().getString(R.string.app_testmode);   
        }else{
        	mAppName =getListView().getContext().getString(R.string.app_pcbatestmode);
        }
        Log.d(TAG,"resolveIntent: mAppName:"+mCategaryName);
        setTitle(mAppName);

    }
    protected List getData(String prefix) {
        List<Map> myData = new ArrayList<Map>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(mCategaryName);
        Log.v(TAG,"getData: mAppName:"+mCategaryName);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);

        if (null == list)
            return myData;

        String[] prefixPath;
        
        if (prefix.equals("")) {
            prefixPath = null;
        } else {
            prefixPath = prefix.split("/");
        }
        Log.d(TAG, "prefix="+prefix);
        Log.d(TAG, "prefixPath="+prefixPath);
        
        int len = list.size();
        Log.d(TAG, "size of List<ResolveInfo> list="+len);
        
        Map<String, Boolean> entries = new HashMap<String, Boolean>();

        for (int i = 0; i < len; i++) {
            ResolveInfo info = list.get(i);
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null
                    ? labelSeq.toString()
                    : info.activityInfo.name;
                    Log.d(TAG,"getData i="+i+" :"+label);
            if (prefix.length() == 0 || label.startsWith(prefix)) {
//            	if (label.startsWith("0") || label.startsWith("1")) {
	                String[] labelPath = label.split("/");
	                Log.d(TAG, "labelPath.length="+labelPath.length);
	                Log.d(TAG, "labelPath[0]="+labelPath[0]);
	                Log.d(TAG, "label="+label);
	
	                String nextLabel = prefixPath == null ? labelPath[0] : labelPath[prefixPath.length];
	                Log.d(TAG, "nextLabel="+nextLabel);
	
	                if ((prefixPath != null ? prefixPath.length : 0) == labelPath.length - 1) {
	                	Log.d(TAG, "If --- addItem");
	                    addItem(myData, nextLabel, activityIntent(
	                            info.activityInfo.applicationInfo.packageName,
	                            info.activityInfo.name));
	                } else {
	                	Log.d(TAG, "Else --- addItem");
	                    if (entries.get(nextLabel) == null) {
	                        addItem(myData, nextLabel, browseIntent(prefix.equals("") ? nextLabel : prefix + "/" + nextLabel));
	                        entries.put(nextLabel, true);
	                    }
	                }
	                
//            	}
            }
        }

        Collections.sort(myData, sDisplayNameComparator);
        
        return myData;
    }

    private final static Comparator<Map> sDisplayNameComparator = new Comparator<Map>() {
        private final Collator   collator = Collator.getInstance();

        public int compare(Map map1, Map map2) {
            return collator.compare(map1.get("title"), map2.get("title"));
        }
    };


	    
	
	protected Intent activityIntent(String pkg, String componentName) {
        Intent result = new Intent();
        result.setClassName(pkg, componentName);
        Log.d(TAG, "pkg="+pkg+" componentName="+componentName);
        return result;
    }
    
    protected Intent browseIntent(String path) {
        Intent result = new Intent();
        result.setClass(this, TestModeActivity.class);
        result.putExtra("com.example.android.apis.Path", path);
        return result;
    }

    protected void addItem(List<Map> data, String name, Intent intent) {
        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("title", name);
        temp.put("intent", intent);
        data.add(temp);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//        Map map = (Map) l.getItemAtPosition(position);
////        mCurrentTestView = v;
//        mStep=position;
//        Log.d(TAG,"onListItemClick mStep:"+mStep);
//        Intent intent = (Intent) map.get("intent");
////        startActivity(intent);
//        startActivityForResult(intent, 0);
    }
    
    protected View getViewForIntent(Intent i){
    	return null;
    	
    }
	protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
		Log.d(TAG,"onActivityResult +++++++++++mStep:"+mStep+" result:"+resultCode);
		//1,getlabel
		//2,find item in listview
		//3, set background color

		Log.v(TAG,"onActivityResult requestCode:"+requestCode);
		//[yeez_haojie add 11.26 start]
		if(requestCode == 1)
		{
			showSelectOptions(intent);
			Log.v(TAG,"showSelectOptions");
		}
		//[yeez_haojie add 11.26 end]
		
		
		if(requestCode == 2)
		{
			startTestResult(intent);//[yeez_haojie add 2013.2.19]
			Log.v(TAG,"startTestResult end");
		}
		
		
		//[yeez_haojie add 11.29 start]
	/*	else if(requestCode == 3)
		{
			Bundle bundle = new Bundle();
			bundle.putString("gps sign", "gps sign");			
			gpsintent.putExtras(bundle);
			startActivityForResult(gpsintent, 0);
			
			Log.v(TAG,"startActivityForResult gpsintent");
		}*/ //[yeez_haojie modfiy 12.21]
		//[yeez_haojie add 11.29 end]
		
		//if (resultCode == RESULT_OK && testMode){
		else if (resultCode == RESULT_OK && testMode && requestCode != 1){//[yeez_haojie modfiy 11.26]
			mTestStateItems.put(mStep, TestState.TestScuessed);
		
        	mStep = mStep+1;    		
    		
    		Log.v(TAG, "mStep :"+mStep);
    		Log.v(TAG, "mTestList.size() :"+mTestList.size());
	        if(mStep <mTestList.size()){
	        	//next test
//	        	Map map = (Map) getListView().getItemAtPosition(mStep);
	        	Intent newintent = (Intent) mTestList.get(mStep).get("intent");
	        	
	        	//[yeez_haojie add 12.3 st]
	    		if(newintent.getComponent().getClassName().equals("com.android.TestMode.GpsTestActivity") && requestCode == 0)
	    		{
	    			/* Intent intentg = new Intent();
	    			 intentg.setClassName("com.chartcross.gpstest", "com.chartcross.gpstest.GPSTest");
	    			//startActivity(intent);
	    	  		gpsintent = newintent;
	    			startActivityForResult(intentg, 3);	*/		//[yeez_haojie modfiy 12.21]
	    			startActivityForResult(newintent,0);	//[yeez_haojie modfiy 12.21]
	    		}
	    		//[yeez_haojie add 12.3 end]
	    		else
	    		{
	    			startActivityForResult(newintent,0);
	    		}
	        	
	        }
	        
	        else if(mStep == mTestList.size())
	        {
	        	startClearData(intent);//[yeez_haojie add 2013.2.19]
	        }
	        
	        else{
	        		        	if(CATEGORY_TEST.equals(mCategaryName)){
					String[] ats = { "MOBILE_TEST_RESULT", ALL_PASSED };
					//PhoneFactory.getDefaultPhone().invokeOemRilRequestStrings(ats, null); //[yeez_haojie modify 12.21]
				}else{
					String[] ats = { "PCBA_TEST_RESULT", ALL_PASSED};
					//PhoneFactory.getDefaultPhone().invokeOemRilRequestStrings(ats, null);//[yeez_haojie modify 12.21]
				}	
        		
        		
        		
	        	setListView(mTestList.subList(0, mStep));
	        	new AlertDialog.Builder(TestModeActivity.this)
				.setTitle(mAppName).setMessage(
						R.string.Test_Pass).setPositiveButton(
						R.string.Button_Yes,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialog,
									int whichButton) {
								finish();
							}
						}).show();
	
	        	
	        }
		}else if(resultCode == RESULT_OK && !testMode){//add by zhai
        		mTestStateItems.put(mStep, TestState.TestScuessed);	
        		setListView(mTestList);//FIXME: change when released
            	new AlertDialog.Builder(TestModeActivity.this)
    			.setTitle(mAppName).setMessage(
    					" "+getListView().getContext().getString(R.string.Test_Pass)).setPositiveButton(
    					R.string.Button_Yes,
    					new DialogInterface.OnClickListener() {
    						public void onClick(
    								DialogInterface dialog,
    								int whichButton) {
    						}
    					}).show();//ended by zhai
		}else{
			//test failed
			/*if(CATEGORY_TEST.equals(mCategaryName)){
				String[] ats = { "MOBILE_TEST_RESULT", Integer.toString(mStep+1) };
				PhoneFactory.getDefaultPhone().invokeOemRilRequestStrings(ats, null);
			}else{
				String[] ats = { "PCBA_TEST_RESULT", Integer.toString(mStep+1) };
				PhoneFactory.getDefaultPhone().invokeOemRilRequestStrings(ats, null);
			}*/
    		
    		if(testMode){
    			

        		Log.v(TAG, "mStep :"+mStep);
        		Log.v(TAG, "mTestList.size() :"+mTestList.size());
        		
    			if(mStep == mTestList.size()){
    				Log.v(TAG, "mStep == mTestList.size()");
    				TestModeActivity.this.finish();
    			}//[yeez_haojie add 2013.2.19]
    			
			mTestStateItems.put(mStep, TestState.TestFailed);			
			String failedItem = (String) mTestList.get(mStep).get("title");			
			setListView(mTestList.subList(0, mStep+1));//FIXME: change when released
        	new AlertDialog.Builder(TestModeActivity.this)
			.setTitle(mAppName).setMessage(
					failedItem.substring(4)+" "+getListView().getContext().getString(R.string.Test_Fail)).setPositiveButton(
					R.string.Button_Yes,
					new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog,
								int whichButton) {
							//finish();
							//							mStep = mStep+1;  
//							if(mStep <mTestList.size()){
//					        	//next test
////					        	Map map = (Map) getListView().getItemAtPosition(mStep);
//					        	Intent newintent = (Intent) mTestList.get(mStep).get("intent");
//					        	startActivityForResult(newintent,0);
//					        	
//					        }
						}
					}).show();
        	}else{//add by zhai
        		mTestStateItems.put(mStep, TestState.TestFailed);	
        		setListView(mTestList);//FIXME: change when released
            	new AlertDialog.Builder(TestModeActivity.this)
    			.setTitle(mAppName).setMessage(
    					" "+getListView().getContext().getString(R.string.Test_Fail)).setPositiveButton(
    					R.string.Button_Yes,
    					new DialogInterface.OnClickListener() {
    						public void onClick(
    								DialogInterface dialog,
    								int whichButton) {
    						}
    					}).show();
        	}//ended by zhai
		}
		//((MySimpleAdapter) getListView().getAdapter()).notifyDataSetChanged();
		
	}

	@Override
	protected void onDestroy() {
		Settings.Secure.setLocationProviderEnabled(getContentResolver(),
				LocationManager.GPS_PROVIDER, mGpsEnabled);
		if (mBluetoothAdapter.isEnabled())
			mBluetoothAdapter.disable();
		super.onDestroy();
	}
	
	/**
	 * [yeez_haojie add 2013.2.19]
	 */
	/*public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}*/
}
