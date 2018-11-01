/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.TestMode.BluetoothTestActivity.getInfoThread;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class CheckTestModeActivity extends PreferenceActivity {
    private static final int BUSY_READING_DIALOG = 100;
	private static final int SETP_1 = 100;
	private static final int SETP_2 = 200;
	private static final int SETP_3 = 300;
	private static final int SETP_4 = 400;
	private static final int SETP_5 = 500;
	private static final int SETP_6 = 600;
	private static final int SETP_7 = 700;
	private static final int SETP_8 = 800;
	private static final int SETP_9 = 900;
	private static final int SETP_10 = 1000;
	private static final int SETP_11 = 1001;
	private static final int SETP_VERSION = 1002;
	private static final String LOG_TAG = "CheckTestModeActivity";
	private ArrayList<Preference> mPreferences = new ArrayList<Preference> ();	
	/**
	 * TODO: Set the path variable to a streaming video URL or a local media
	 * file path.
	 */
	private String path = "";
	private VideoView mVideoView;
	private Phone mPhone;
	private String mFailMessage;
	TextView myTextView1;
	private Context mContext;
	private List<String> mPCBATestItems;
	private List<String> mTestItems;
	private String mVersion;
	private boolean mIsLensVersionMatch;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AsyncResult ar = (AsyncResult) msg.obj;
			String result = null;
			Log.d(LOG_TAG, "handleMessage msg.what:" + msg.what);
			boolean isScuessed;
			if ((ar == null) || (ar.result == null)) {
				isScuessed = false;
			} else {
//				String[] s = (String[]) ar.result;
				result = ((String[]) ar.result)[0];
//				 Log.d(LOG_TAG, "handleMessage strings[]:" +s+" 0:"+s[0]);
				if (ar.exception != null) {
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: ar.exception="
									+ ar.exception);
					isScuessed = false;

				} else {
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"
									+ result);
//					if ("1".equals(result)) {
//						isScuessed = true;
//					} else {
//						isScuessed = false;
//					}
					isScuessed = true;
				}
			}
			Message callback;
			switch (msg.what) {
			case SETP_1:
				callback = Message.obtain(mHandler, SETP_3);

				String[] at1 = { "ADC_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(at1, callback);
				 
				if (isScuessed) {
					mPreferences.get(0).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
					mPreferences.get(0).setSummary(mContext.getString(R.string.battery_info_status_unknown));
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;
			case SETP_3:
				callback = Message.obtain(mHandler, SETP_4);

				String[] at3 = { "GSM900_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(at3, callback);
				setCalibrationTestResult(mPreferences.get(2),result);				 
				if (isScuessed) {
//					mPreferences.get(2).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(2).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;

			case SETP_4:
				callback = Message.obtain(mHandler, SETP_5);

				String[] at4 = { "DCS1800_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(at4, callback);
				setCalibrationTestResult(mPreferences.get(3),result);
				if (isScuessed) {
//					mPreferences.get(3).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(3).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;
			case SETP_5:
				callback = Message.obtain(mHandler, SETP_6);

				String[] at5 = { "PCS1900_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(at5, callback);
				setCalibrationTestResult(mPreferences.get(4),result);
				if (isScuessed) {
//					mPreferences.get(4).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(4).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;
			case SETP_6:
				callback = Message.obtain(mHandler, SETP_7);

				String[] at6 = { "TD1880_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(at6, callback);
				setCalibrationTestResult(mPreferences.get(5),result);
				if (isScuessed) {
//					mPreferences.get(5).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(5).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;
			case SETP_7:
				callback = Message.obtain(mHandler, SETP_8);

				String[] at7 = { "TD_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(at7, callback);
				setCalibrationTestResult(mPreferences.get(6),result);
				if (isScuessed) {
//					mPreferences.get(6).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(6).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;

			case SETP_8:
				callback = Message.obtain(mHandler, SETP_9);

				String[] at8 = { "PCBA_TEST_RESULT", "READ" };
				mPhone.invokeOemRilRequestStrings(at8, callback);
				setCalibrationTestResult(mPreferences.get(7),result);
				if (isScuessed) {
//					mPreferences.get(7).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(7).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;
			case SETP_9:
				callback = Message.obtain(mHandler, SETP_10);

				String[] at9 = { "MOBILE_TEST_RESULT", "READ" };
				mPhone.invokeOemRilRequestStrings(at9, callback);
				//setCalibrationTestResult(mPreferences.get(8),result);
				if("0".equals(result)){

					mPreferences.get(8).setSummary(mContext.getString(R.string.Test_NT));
				}else if(TestModeActivity.ALL_PASSED.equals(result)){

					mPreferences.get(8).setSummary(mContext.getString(R.string.Test_Pass));
				}else{

				    if (result==null){
				        mPreferences.get(8).setSummary(mContext.getString(R.string.battery_info_power_unknown));
				    }else{
				        mPreferences.get(8).setSummary(mContext.getString(R.string.Test_Fail) + mPCBATestItems.get(Integer.parseInt(result)-1));
				    }
				}
				if (isScuessed) {
//					mPreferences.get(8).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(8).setSummary(R.string.Test_Fail);
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;
			case SETP_10:

				if("0".equals(result)){

					mPreferences.get(9).setSummary(mContext.getString(R.string.Test_NT));
				}else if(TestModeActivity.ALL_PASSED.equals(result)){

					mPreferences.get(9).setSummary(mContext.getString(R.string.Test_Pass));
				}else{

                    if (result==null){
                        mPreferences.get(9).setSummary(mContext.getString(R.string.battery_info_power_unknown));
                    }else{
                        mPreferences.get(9).setSummary(mContext.getString(R.string.Test_Fail) + mPCBATestItems.get(Integer.parseInt(result)-1));
                    }
					
				}
				Log.d(LOG_TAG,"mIsLensVersionMatch:"+mIsLensVersionMatch);
                if (mIsLensVersionMatch){

                    callback = Message.obtain(mHandler, SETP_11);                    
                    String[] at10 = { "TOUCH_TEST_RESULT", "READ" };
                    mPhone.invokeOemRilRequestStrings(at10, callback);
                }else{

                    mPreferences.get(11).setSummary(mContext.getString(R.string.Test_Fail));
                }
                
				if (isScuessed) {
//					mPreferences.get(9).setSummary(result);
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

				} else {
//					mPreferences.get(9).setSummary(mContext.getString(R.string.Test_Fail));
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
									+ ar.exception);
				}
				break;

			case SETP_11:
               if("0".equals(result)){

                   mPreferences.get(11).setSummary(mContext.getString(R.string.Test_NT));
               }else if(TestModeActivity.ALL_PASSED.equals(result)){
                
                    mPreferences.get(11).setSummary(mContext.getString(R.string.Test_Pass));
                   
               }else{

                   if (result==null){
                       mPreferences.get(11).setSummary(mContext.getString(R.string.battery_info_power_unknown));
                   }else{
                       mPreferences.get(11).setSummary(mContext.getString(R.string.Test_Fail));
                   }
                   
               }
               
               if (isScuessed) {
//                 mPreferences.get(9).setSummary(result);
                   Log
                           .d(LOG_TAG,
                                   "handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result);

               } else {
//                 mPreferences.get(9).setSummary(mContext.getString(R.string.Test_Fail));
                   Log.d(LOG_TAG,
                           "handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"+result+"ar.exception="
                                   + ar.exception);
               }
               break;
//            case SETP_VERSION:
//                
//                mPreferences.get(10).setSummary(mVersion);
//                dismissDialog(BUSY_READING_DIALOG);
//                Log.d(LOG_TAG,"mIsLensVersionMatch:"+mIsLensVersionMatch);
//                if (mIsLensVersionMatch){

//                    callback = Message.obtain(mHandler, SETP_11);                    
//                    String[] at10 = { "TOUCH_TEST_RESULT", "READ" };
//                    mPhone.invokeOemRilRequestStrings(at10, callback);
//                }else{

//                    mPreferences.get(11).setSummary(mContext.getString(R.string.Test_Fail));
//                }
//                    
//                break;
			}

			return;
		}
	};
	void setCalibrationTestResult(Preference p,String result){
		if("1".equals(result)){
			p.setSummary(mContext.getString(R.string.Calibrationed));
		}else{
			p.setSummary(mContext.getString(R.string.NotCalibrationed));
		}
	}
	
	void setTestModeTestResult(Preference p,String result){
		if("0".equals(result)){

			p.setSummary(mContext.getString(R.string.Calibrationed));
		}else if("100".equals(result)){

			p.setSummary(mContext.getString(R.string.NotCalibrationed));
		}else{

			p.setSummary(mContext.getString(R.string.NotCalibrationed));
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// mPhone = PhoneFactory.getDefaultPhone();
		setTitle(R.string.app_check_testmode);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mContext = this;
//		setContentView(R.layout.adc);
		
//		myTextView1 = (TextView) findViewById(R.id.myTextView1);
		
		// Root
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
//        PreferenceCategory inlinePrefCat = new PreferenceCategory(this);
//        inlinePrefCat.setTitle("test");
//        root.addPreference(inlinePrefCat);
        
        // Toggle preference
//        CheckBoxPreference togglePref = new CheckBoxPreference(this);
//        togglePref.setKey("toggle_preference");
//        togglePref.setTitle(R.string.title_toggle_preference);
//        togglePref.setSummary(R.string.summary_toggle_preference);
//        inlinePrefCat.addPreference(togglePref);
//                
//        // Dialog based preferences
//        PreferenceCategory dialogBasedPrefCat = new PreferenceCategory(this);
//        dialogBasedPrefCat.setTitle(R.string.dialog_based_preferences);
//        root.addPreference(dialogBasedPrefCat);
        
        
        Preference checkPref = new Preference(this);
//        checkPref.setKey("toggle_preference");
       
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check1));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check2));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check3));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check4));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check5));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check6));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check7));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check8));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check9));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check10));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check11));
        mPreferences.add(checkPref);
        
        checkPref = new Preference(this);
        checkPref.setTitle(mContext.getString(R.string.testmode_check12));
        mPreferences.add(checkPref);
        
        
        Log.d(LOG_TAG,"onCreate dialog");
        
        for(int i=0;i < mPreferences.size();i++ ){
        	root.addPreference(mPreferences.get(i));
        }
        
//        new GetVersionThread().start();
//        showDialog(BUSY_READING_DIALOG);
        
        setPreferenceScreen(root);
		mPhone = PhoneFactory.getDefaultPhone();
		mPreferences.get(1).setSummary(mPhone.getDeviceId());
				
		File devfile = new File("/sys/kernel/ctp_sysfs/ctp_vendor");
		mPreferences.get(10).setSummary(readCDC(devfile));
		
		// mVideoView = (VideoView) findViewById(R.id.surface_view);
		//
		// if (path == "") {
		// // Tell the user to provide a media file URL/path.
		// Toast.makeText(
		// TestADCCalibrationActivity.this,
		// "Please edit VideoViewDemo Activity, and set path"
		// + " variable to your media file URL/path",
		// Toast.LENGTH_LONG).show();
		//
		// } else {
		//
		// /*
		// * Alternatively,for streaming media you can use
		// * mVideoView.setVideoURI(Uri.parse(URLstring));
		// */
		// mVideoView.setVideoPath(path);
		// mVideoView.setMediaController(new MediaController(this));
		// mVideoView.requestFocus();
		//
		// }
		Message callback = Message.obtain(mHandler, SETP_1);
		String[] ats = { "MOBILE_SN_NUMBER", "READ" };
		mPhone.invokeOemRilRequestStrings(ats, callback);
		mTestItems=getData("",TestModeActivity.CATEGORY_TEST);
		mPCBATestItems=getData("",TestModeActivity.CATEGORY_PCBATEST);
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
	    Log.d(LOG_TAG,"onCreateDialog id:"+id);
        if (id == BUSY_READING_DIALOG ) {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setTitle(getText(R.string.updating_title));
            dialog.setIndeterminate(true);


                    dialog.setCancelable(false);
//                    dialog.setOnCancelListener(this);
                    dialog.setMessage(getText(R.string.reading_settings));
                    return dialog;
                

        }
        return null;
	}

//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			//moveTaskToBack(true);
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
	protected List<String> getData(String prefix,String categaryname) {
        List<String> myData = new ArrayList<String>();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(categaryname);
        Log.d(LOG_TAG,"getData: mAppName:"+categaryname);
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
        
        int len = list.size();
        
        ArrayList<String> entries = new ArrayList<String>();

        for (int i = 0; i < len; i++) {
            ResolveInfo info = list.get(i);
            CharSequence labelSeq = info.loadLabel(pm);
            String label = labelSeq != null
                    ? labelSeq.toString()
                    : info.activityInfo.name;
                    Log.d(LOG_TAG,"getData i="+i+" :"+label);
            if (prefix.length() == 0 || label.startsWith(prefix)) {
//            	if (label.startsWith("0") || label.startsWith("1")) {
	                String[] labelPath = label.split("/");
	
	                String nextLabel = prefixPath == null ? labelPath[0] : labelPath[prefixPath.length];
	
	                if ((prefixPath != null ? prefixPath.length : 0) == labelPath.length - 1) {
//	                    addItem(myData, nextLabel, activityIntent(
//	                            info.activityInfo.applicationInfo.packageName,
//	                            info.activityInfo.name));
	                    myData.add(nextLabel);
	                } else {
//	                    if (entries.get(nextLabel) == null) {
////	                        addItem(myData, nextLabel, browseIntent(prefix.equals("") ? nextLabel : prefix + "/" + nextLabel));
////	                        entries.put(nextLabel, true);
//	                    }
	                }
//            	}
            }
        }

        Collections.sort(myData);
        for (int i =0;i<myData.size();i++){
            Log.d(LOG_TAG,"getData i="+i+" :"+myData.get(i));
        }
        return myData;
    }
//	   class GetVersionThread extends Thread
//	    {
//	        @Override
//	        public void run()
//	        {
	    public String readCDC(File devfile)
	    {
//	            File devfile = new File("/sys/kernel/ctp_sysfs/ctp_vendor");
	                try
	                {
	                    byte[] buffer = new byte[90];
	                    int[] buffer_show = new int[45];
	                    String version;
	                    FileInputStream fin;
	                    Log.e("Robert", "readCDC file=" + devfile);
	                    try {
	                        fin = new FileInputStream(devfile);
	                        fin.read(buffer, 0, 90);
	                        fin.close();
	                    } catch (Exception e) {
	                        Log.e("Robert", ">>>>>>>>>>>>>>>>>error=" + e.getMessage());
	                    }
	                    version = mContext.getString(R.string.lense_factory_version);
	                    for (int i = 0; i < 3; i++) {
	                        int c = (int) (buffer[i] & 0xff);
	                        version = version + new String(new byte[] {buffer[i]});
	                        Log.i("Robert", "buffer[" + i + "] = [" + Integer.toHexString(c) + "]\n");
	                    }

	                    version = version +"  " + mContext.getString(R.string.lense_firmware_version);
	                    for (int i = 3; i < 3 + 9; i++) {
	                        int c = (int) (buffer[i] & 0xff);
	                        version = version + Integer.toHexString(c);
	                        Log.i("Robert", "buffer[" + i + "] = [" + Integer.toHexString(c) + "]\n");
	                    }

	                    version = version+"  " + mContext.getString(R.string.lense_config_version);
	                    for (int i = 3 + 9; i < 3 + 9 + 5; i++) {
	                        int c = (int) (buffer[i] & 0xff);
	                        version = version + Integer.toHexString(c);
	                        Log.i("Robert", "buffer[" + i + "] = [" + Integer.toHexString(c) + "]\n");
	                    }
	                    mVersion = version;
	                    File devfile_test = new File("/sys/kernel/ctp_sysfs/ctp_calibration");
	                    
	                    if (devfile_test.exists()) {

//	                        if (buffer[3]!=9 || buffer[4]!=0 || buffer[5]!=0 || buffer[6]!=0 || buffer[7]!=6 || buffer[8]!=1 || buffer[9]!=1 || buffer[10]!=14 || buffer[11]!=20 || buffer[12]!=7 || buffer[13]!=14 || buffer[14]!=4 || buffer[15]!=0 ||buffer[16]!=13 ){
	                        if (buffer[3]!=9 || buffer[4]!=0 || buffer[5]!=0 || buffer[6]!=0 || buffer[7]!=6 || buffer[8]!=1 || buffer[9]!=1 || buffer[10]!=14 || buffer[11]!=21 || buffer[12]!=7 || buffer[13]!=14 || buffer[14]!=4 || buffer[15]!=0 ||buffer[16]!=13){
	                            mIsLensVersionMatch = false;
	                        }else{
	                            mIsLensVersionMatch = true;
	                        }
	                        Log.d("Robert", "mIsLensVersionMatch     adfafdasf111:"+mIsLensVersionMatch);
	                    }else{

	                        mIsLensVersionMatch = true;
	                    }
	                    

	                    
	                    mHandler.sendEmptyMessage(SETP_VERSION);

	                }
	                catch(Exception e)
	                {
	                    e.printStackTrace();
	                }
	                return mVersion;
	        }
//	    };
//    public String readCDC(File devfile)
// {
//        byte[] buffer = new byte[90];
//        int[] buffer_show = new int[45];
//        String version;
//        FileInputStream fin;
//        Log.e("Robert", "readCDC file=" + devfile);
//        try {
//            fin = new FileInputStream(devfile);
//            fin.read(buffer, 0, 90);
//            fin.close();
//        } catch (Exception e) {
//            Log.e("Robert", ">>>>>>>>>>>>>>>>>error=" + e.getMessage());
//        }
//        version = mContext.getString(R.string.lense_factory_version);
//        for (int i = 0; i < 3; i++) {
//            int c = (int) (buffer[i] & 0xff);
//            version = version + Integer.toHexString(c);
//            Log.i("Robert", "buffer[" + i + "] = [" + Integer.toHexString(c) + "]\n");
//        }
//
//        version = version +"  " + mContext.getString(R.string.lense_firmware_version);
//        for (int i = 3; i < 3 + 9; i++) {
//            int c = (int) (buffer[i] & 0xff);
//            version = version + Integer.toHexString(c);
//            Log.i("Robert", "buffer[" + i + "] = [" + Integer.toHexString(c) + "]\n");
//        }
//
//        version = version+"  " + mContext.getString(R.string.lense_config_version);
//        for (int i = 3 + 9; i < 3 + 9 + 7; i++) {
//            int c = (int) (buffer[i] & 0xff);
//            version = version + Integer.toHexString(c);
//            Log.i("Robert", "buffer[" + i + "] = [" + Integer.toHexString(c) + "]\n");
//        }
//
//        return version;
//        // for (int i=0;i<45;i++)
//        // {
//        // // buffer_show[i] =(buffer[2*i] & 0xff)+(buffer[2*i+1] & 0xff)<<8;
//        // int c = (int)(buffer[2*i] & 0xff);
//        // int d = (int)((buffer[2*i+1] & 0xff)<<8);
//        // buffer_show[i]=c+d;
//        // // Log.i("Robert",
//        // "buffer["+(2*i)+"] = ["+Integer.toHexString(c)+"]\n");
//        // // Log.i("Robert",
//        // "buffer["+(2*i+1)+"] = ["+Integer.toHexString(d)+"]\n");
//        // // Log.i("Robert",
//        // "buffer["+"result"+"] = ["+Integer.toHexString(c+d)+"]\n");
//        // // Log.i("Robert",
//        // "buffer["+"actual resut"+"] = ["+Integer.toHexString(buffer_show[i])+"]\n");
//        // }
//        //         
//        // // for (int i=0;i<45;i++)
//        // // {int c = buffer_show[i];
//        // // Log.i("Robert",
//        // "buffer_show["+i+"]="+Integer.toHexString(c)+" \r\n" );
//        //            
//        // // }
//        //         
//        //         
//        // //if(SysReadCDC(45, buffer) == 0)
//        // {
//        // for(int i = 0; i < channelNum; i++)
//        // {
//        // cdc[fifo][i] = buffer_show[i];
//        // }
//        // fifo = (fifo + 1) % maxFifo;
//        // }
//        //        
//
//    }
}
