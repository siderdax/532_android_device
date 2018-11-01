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

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import java.io.File;//cys
import java.io.FileInputStream;//cys
import java.io.FileOutputStream;//cys

public class TestADCCalibrationActivity extends Activity {

	private static final int SETP_0 = 101;
	private static final int SETP_1 = 100;
	private static final int SETP_2 = 200;
	private static final int SETP_3 = 300;
	private static final int SETP_4 = 400;
	private static final String LOG_TAG = "TestADCCalibrationActivity";
	/**
	 * TODO: Set the path variable to a streaming video URL or a local media
	 * file path.
	 */
	private String path = "";
	private String nvm_file_name = "/data/Linux/Marvell/NVM/BatteryCalData.nvm";//cys
	private String adc_calibrate_file_name= "/sys/devices/platform/pxa2xx-i2c.0/i2c-0/0-0034/88pm860x-battery.0/calibration";//cys
	private VideoView mVideoView;
	private Phone mPhone;
	private String mFailMessage;
	TextView myTextView1;
	private Context mContext;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AsyncResult ar = (AsyncResult) msg.obj;
			;
			Log.d(LOG_TAG, "handleMessage msg.what:" + msg.what);
			boolean isScuessed;
			if ((ar == null) || (ar.result == null)) {
				isScuessed = false;
			} else {
				
				String result = ((String[]) ar.result)[0];

				if (ar.exception != null) {
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: ar.exception="
									+ ar.exception);
					isScuessed = false;

				} else {
					Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: result:"
									+ result);
					if ("1".equals(result)) {
						isScuessed = true;
					} else {
						isScuessed = false;
					}
				}
			}

			switch (msg.what) {
			case SETP_0:
				Message callback = Message.obtain(mHandler, SETP_1);
				String[] ats = { "ADC_CALI_CHECK", "READ" };
				mPhone.invokeOemRilRequestStrings(ats, callback);
				break;
			case SETP_1:
//				Message callback = Message.obtain(mHandler, SETP_2);
//
//				String[] ats = { "TD_CALI_CHECK", "READ" };
//				mPhone.invokeOemRilRequestStrings(ats, callback);
//cys
				File file=new File(nvm_file_name);  
				if(!file.exists())  
				{  
					isScuessed =false;
				}else{

				//File file1=new File(adc_calibrate_file_name);  
				//if(!file1.exists())  
				//{  
				//	isScuessed =false;
				//}else{
					isScuessed =true;
				//}
				}
//cys
				if (isScuessed) {
					Log
							.d(LOG_TAG,
									"handleMessage EVENT_UPLMN_SCAN_COMPLETED: ");

				} else {
					mFailMessage = "ADC "+mContext.getString(R.string.CalibrationFailed)
							+ System.getProperty("line.separator");
					/*Log.d(LOG_TAG,
							"handleMessage EVENT_UPLMN_SCAN_COMPLETED: ar.exception="
									+ ar.exception);*/
				}
				if (mFailMessage != null && mFailMessage.length() > 0) {
					//Failed
					myTextView1.setText(mFailMessage);
					new AlertDialog.Builder(TestADCCalibrationActivity.this)
							.setTitle(R.string.ADCCalibrationTest).setNegativeButton(
									R.string.Button_No,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
			                                setResult(RESULT_FIRST_USER);
			                                mHandler.post(
			                                        new Runnable() {
			                                            public void run() {
			                                                finish();
			                                            }
			                                        });
										}
									}).setOnKeyListener(new OnKeyListener() {

					        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
					        					
					        					return true;
					        		         }
					        		         return false;
					        			}
					        		}).show();
				} else {
					myTextView1.setText("ADC "+mContext.getString(R.string.CalibrationPassed));
					// myTextView1.setText(mFailMessage);
					new AlertDialog.Builder(TestADCCalibrationActivity.this)
							.setTitle(R.string.ADCCalibrationTest).setPositiveButton(
									R.string.Button_Yes,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
			                                setResult(RESULT_OK);
			                                mHandler.post(
			                                        new Runnable() {
			                                            public void run() {
			                                                finish();
			                                            }
			                                        });
										}
									}).setOnKeyListener(new OnKeyListener() {

					        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
					        					
					        					return true;
					        		         }
					        		         return false;
					        			}
					        		}).show();

				}
				break;

			}

			return;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		// mPhone = PhoneFactory.getDefaultPhone();
		setTitle(R.string.ADCCalibrationTest);
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mContext = this;
		setContentView(R.layout.adc);
		
		myTextView1 = (TextView) findViewById(R.id.myTextView1);
		myTextView1.setTextSize(25.0f);
		mPhone = PhoneFactory.getDefaultPhone();
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

		Message callback = Message.obtain(mHandler, SETP_1);//cys  SETP_0-->SETP_1
		mHandler.sendMessageDelayed(callback, 1000);
		

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
