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

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class TestVideoActivity extends Activity {

	/**
	 * TODO: Set the path variable to a streaming video URL or a local media
	 * file path.
	 */
	private String path = "/sdcard/720p.rmvb";// FIXME:R.raw.test
	private VideoView mVideoView;
	private static final int POLL_STATUS_INTERVAL_MSECS = 3000;
	private static final int SETP_1 = 8;
	static final int FINISH_TEST = 1;

	private final Timer timer = new Timer();
	private TimerTask task;

	// private Handler mHandler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// AsyncResult ar=(AsyncResult) msg.obj;;
	// boolean isScuessed;
	// String result =((String[]) ar.result)[0];
	// switch (msg.what) {
	// case SETP_1:
	//                    
	//
	//                    
	//                    
	// break;
	// }
	//
	// return;
	// }
	// };
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == FINISH_TEST) {
				AlertDialog d =new AlertDialog.Builder(TestVideoActivity.this).setMessage(R.string.DialogMessage)
						.setPositiveButton(R.string.Button_Yes,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										mVideoView.stopPlayback();
                                        setResult(RESULT_OK);
                                        finish();
									}

								}).setNegativeButton(R.string.Button_No,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
                                        setResult(RESULT_FIRST_USER);
                                        finish();
									}
								}).setOnKeyListener(new OnKeyListener() {

				        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
				        					
				        					return true;
				        		         }
				        		         return false;
				        			}
				        		}).create();
				d.getWindow().getAttributes().y = 120;
				d.show();

			}

		}
	};

	// private MediaPlayer.OnCompletionListener mCompletionListener =
	// new MediaPlayer.OnCompletionListener() {
	// public void onCompletion(MediaPlayer mp) {
	//
	// }
	// };
	private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {

		public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			Toast.makeText(TestVideoActivity.this, path+mVideoView.getContext().getString(R.string.MissingFile),
					Toast.LENGTH_LONG).show();
			return false;
		}
	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);  
			getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN,WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setTitle(R.string.VideoTest);
		setContentView(R.layout.videoview);
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		mVideoView.setOnErrorListener(mErrorListener);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		if (path == "") {
			// Tell the user to provide a media file URL/path.
			Toast.makeText(
					TestVideoActivity.this,
					"Please edit VideoViewDemo Activity, and set path"
							+ " variable to your media file URL/path",
					Toast.LENGTH_LONG).show();

		} else {

			/*
			 * Alternatively,for streaming media you can use
			 * mVideoView.setVideoURI(Uri.parse(URLstring));
			 */
			//mVideoView.setVideoURI(Uri.parse("res/raw/720.mp4"));  
			mVideoView.setVideoPath(path);
			mVideoView.setMediaController(new MediaController(this));
			mVideoView.requestFocus();
			mVideoView.start();
			task = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Message message = new Message();
					message.what = FINISH_TEST;
					handler.sendMessage(message);
				}
			};
			timer.schedule(task, 5000);
			// mHandler.sendEmptyMessageDelayed(SETP_1,
			// POLL_STATUS_INTERVAL_MSECS);
		}
		// mVideoView.setOnCompletionListener(mCompletionListener);

		// final Intent intent = new Intent("Phone.intent.action.Launch");
		// startActivity(intent);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
