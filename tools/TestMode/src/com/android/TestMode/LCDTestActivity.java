package com.android.TestMode;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class LCDTestActivity extends Activity {
//	private int clickTime;
	private TextView textView;
	//private static final int[] COLOR = {Color.BLUE,Color.RED,Color.YELLOW,Color.BLACK,Color.GREEN,Color.WHITE,Color.CYAN};
	private static final int[] COLOR = {Color.BLUE,Color.RED,Color.BLACK,Color.GREEN,Color.WHITE
		,Color.BLUE,Color.RED,Color.BLACK,Color.GREEN,Color.WHITE};//[yeez_haojie modfiy 3.18]

	public EngSqlite mEngSqlite;//[yeez_haojie add 11.26]
	
	int colorint = 0;//[yeez_haojie add 2013.3.5]
	
//	static final int FINISH_TEST = 1;

//    private final Timer timer = new Timer();
//              private TimerTask task;
//              Handler handler = new Handler() {
//                       public void handleMessage(Message msg) {
//                                super.handleMessage(msg);
//                                if(msg.what == FINISH_TEST)
//                                {
//                                          new AlertDialog.Builder(LCDTestActivity.this)
//                                          .setTitle(R.string.lcd_test)
//                                          .setMessage(R.string.DialogMessage)            
//                                          .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
//                                                   public void onClick(DialogInterface dialog, int whichButton) {
//                                                       setResult(RESULT_OK);
//                                                       onDestroy();
//                                                   }
//                                
//                                          })
//                                          .setNegativeButton(R.string.Button_No, new DialogInterface.OnClickListener() {
//                                                   public void onClick(DialogInterface dialog, int whichButton) {
//                                                       setResult(RESULT_FIRST_USER);
//                                                       onDestroy();
//                                                   }
//                                          })
//                                           .setOnKeyListener(new OnKeyListener() {
//
//						        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//						        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
//						        					
//						        					return true;
//						        		         }
//						        		         return false;
//						        			}
//						        		}).show();
//                                          
//                                }
//                                
//                       }
//              };

	
              /** Called when the activity is first created. */
          	@Override
          	public void onCreate(Bundle savedInstanceState) {
          		super.onCreate(savedInstanceState);
          		 mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.26]
          		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          				WindowManager.LayoutParams.FLAG_FULLSCREEN);
          		requestWindowFeature(Window.FEATURE_NO_TITLE);
          		setContentView(R.xml.lcd_test);
          		DisplayMetrics dm = new DisplayMetrics();
          		dm = getApplicationContext().getResources().getDisplayMetrics();
          		int screenWidth = dm.widthPixels;
          		int screenHeight = dm.heightPixels;
          		textView = (TextView) findViewById(R.id.LCDTestView);
          		textView.setWidth(screenWidth);
          		textView.setHeight(screenHeight);
          		textView.setBackgroundColor(COLOR[0]);
          		//mHandler.post(runnable);//[yeez_haojie mofiy 2013.3.15]
          		Toast.makeText (this, R.string.LCDToast, Toast.LENGTH_LONG).show();
          	}

          	int j = 0;
          	Runnable runnable = new Runnable() {
          		public void run() {
          			Message msg = mHandler.obtainMessage();
          			msg.arg1 = j;
          			mHandler.sendMessage(msg);
          			if (j != 0) {
          				try {
          					//Thread.sleep(2000);
          					Thread.sleep(1000);//[yeez_haojie mofiy 12.17]
          				} catch (InterruptedException e) {
          					e.printStackTrace();
          				}
          			}
          			j++;
          		}
          	};
          	private Handler mHandler = new Handler() {
          		public void handleMessage(Message msg) {
          			if (msg.arg1 >= COLOR.length) {
          				mHandler.removeCallbacks(runnable);
          				OpenOptionsDialog();
          			} else {
          				textView.setBackgroundColor(COLOR[msg.arg1]);
          				mHandler.post(runnable);
          			}
          		}
          	};

          	public void OpenOptionsDialog() {
          		new AlertDialog.Builder(this).setCancelable(false)//[yeez_haojie add 12.17]
          		.setTitle(R.string.lcd_test).setMessage(R.string.DialogMessage)
          				.setPositiveButton(R.string. Button_Yes,
          						new DialogInterface.OnClickListener() {

          							public void onClick(DialogInterface dialog,
          									int which) {
//          								j = 0;
//          								mHandler.post(runnable);
          								setResult(RESULT_OK);
          								BaseActivity.NewstoreRusult(true, "Lcd test",mEngSqlite);//[yeez_haojie add 11.26]
                                        onDestroy();
          							}
          						}).setNegativeButton(R.string. Button_No,
          						new DialogInterface.OnClickListener() {

          							public void onClick(DialogInterface dialog,
          									int which) {
          								setResult(RESULT_FIRST_USER);
          								BaseActivity.NewstoreRusult(false, "Lcd test",mEngSqlite);//[yeez_haojie add 11.26]
                                        onDestroy();
          							}
          						})
          						.setOnKeyListener(new OnKeyListener() {

						        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
						        					
						        					return true;
						        		         }
						        		         return false;
						        			}
          						}).show();
          	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}
	
//	private void changeTextViewBackGround() {
//		if (clickTime < TESTCOLOR.length - 1) {
//			textView.setBackgroundColor(TESTCOLOR[++clickTime]);
//		} 
////			clickTime = -1;
////			openOptionsDialog();
//		if (clickTime >= TESTCOLOR.length - 1) {
//			task = new TimerTask() {
//				@Override
//				public void run() {
//					Message message = new Message();
//					message.what = FINISH_TEST;
//					handler.sendMessage(message);
//				}
//			};
//			timer.schedule(task, 1);
//		}
//	}
	
	//private void openOptionsDialog() {
	//	new AlertDialog.Builder(this).setTitle(R.string.test_result)
	//			.setMessage(R.string.DialogMessage).setPositiveButton(
	//					R.string.Button_No,
	//					new DialogInterface.OnClickListener() {

	//						public void onClick(DialogInterface dialog,
	//								int which) {
	//							onDestroy();
	//						}
	//					}).setNegativeButton(R.string.Button_Yes,
	//					new DialogInterface.OnClickListener() {

	//						public void onClick(DialogInterface dialog,
	//								int which) {
	//							onDestroy();
//								Intent intent = new Intent();
//								intent.setClass(LCDTestActivity.this,LEDAndVibrationTestActivity.class);
//								startActivity(intent);
	//						}
	//					}).show();
	//}
	
	
	/**
	 * [yeez_haojie add 2013.3.15]
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//int colorint = 0;
	
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		
		if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
			if(colorint != 0)
			{
				Log.v("Lcd", "KEYCODE_VOLUME_DOWN");
				Log.v("Lcd", "KEYCODE_VOLUME_DOWN colorint :"+colorint);
				textView.setBackgroundColor(COLOR[colorint-1]);
				colorint = colorint-1;
				Log.v("Lcd", "KEYCODE_VOLUME_UP colorint :"+colorint);
			}

			//myScrollBy(200);

			return true;

			}else if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
				Log.v("Lcd", "KEYCODE_VOLUME_UP COLOR.length "+COLOR.length);
				if(colorint == COLOR.length-1)
				{
					OpenOptionsDialog();
				}
				else{
					Log.v("Lcd", "KEYCODE_VOLUME_UP");
					Log.v("Lcd", "KEYCODE_VOLUME_UP colorint :"+colorint);
					textView.setBackgroundColor(COLOR[colorint+1]);
					colorint = colorint+1;
					Log.v("Lcd", "KEYCODE_VOLUME_UP colorint+1 :"+colorint);
				}
			//myScrollBy(-200);

			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
