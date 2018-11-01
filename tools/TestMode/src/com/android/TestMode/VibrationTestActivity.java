package com.android.TestMode;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

public class VibrationTestActivity extends Activity {
	private boolean isVibrate;
	static final int FINISH_TEST = 1;
	public EngSqlite mEngSqlite;//[yeez_haojie add 11.26]
	
    private final Timer timer = new Timer();
              private TimerTask task;
              Handler handler = new Handler() {
                       public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                if(msg.what == FINISH_TEST)
                                {
                                	isVibrate = false;
                                          new AlertDialog.Builder(VibrationTestActivity.this)
                                          .setTitle(R.string.vibrate_test)
                                          .setMessage(R.string.DialogMessage)            
                                          .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int whichButton) {
                                                	   isVibrate = false;
                                                       setResult(RESULT_OK);
                                                       finish();
                                                   }
                                
                                          })
                                          .setNegativeButton(R.string.Button_No, new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int whichButton) {
                                                	   isVibrate = false;
                                                       setResult(RESULT_FIRST_USER);
                                                       finish();
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
                                
                       }
              };

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.xml.vibration_test);
			 mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.26]
//			Button ledTestButton = (Button) findViewById(R.id.ledTestButton);
//			ledTestButton.setText(R.string.led_test);
			final Button vibrationTestButton = (Button) findViewById(R.id.vibrationTestButton);
			vibrationTestButton.setText(R.string.test_begin);
//			ledTestButton.setOnClickListener(new View.OnClickListener() {
//				
//				public void onClick(View v) {
//					showNotification(R.drawable.icon, "", "", "");
//				}
//			});
			vibrationTestButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					vibrationTestButton.setText(R.string.test_continue);
					vibrateTest();
//					 task = new TimerTask() {
//                         @Override
//                         public void run() {
//                                  Message message = new Message();
//                                  message.what = FINISH_TEST;
//                                  handler.sendMessage(message);
//                         }
//                };       
//                timer.schedule(task, 3000);
				}
			});
			setupBottom();
		}
		
//		private void showNotification(int icon, String tickertext, String title,
//				String content) {
//			int notification_id = 19172439;
//			NotificationManager nm =  (NotificationManager)getSystemService(NOTIFICATION_SERVICE); 
//
//			// Notification
//			Notification notification = new Notification(icon, tickertext, System
//					.currentTimeMillis());
//			notification.ledARGB = 0xffff0000;
//			notification.ledOnMS = 50000;
//	        notification.ledOffMS = 20000; 
//	        notification.defaults |= Notification.DEFAULT_LIGHTS;
//		    notification.flags |= Notification.FLAG_SHOW_LIGHTS;
//			
//			PendingIntent pt = PendingIntent.getActivity(this, 0, new Intent(this,
//					VibrationTestActivity.class), 0);
//			notification.setLatestEventInfo(this, title, content, pt);
//			nm.notify(notification_id, notification);
//			nm.cancel(notification_id);
//		}
		
		private void vibrateTest() {
			isVibrate = true;
	    	new Thread() { 

	            @Override 
	            public void run() {
	                Vibrator vb=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
	                
	                while(isVibrate)
	                {
	                	int b=(int)(Math.random()*300);           	
	                	vb.vibrate(b); 
	        	        try 
	        	        { 
	        	            Thread.sleep(800); 
	        	        }catch(Exception e){}
	                } 
//	                vb.cancel();
	            }
	    	}.start();
	    	Button b = (Button) findViewById(R.id.testpassed);
	    	b.setVisibility(View.VISIBLE);
	    }
		
		//private void openOptionsDialog() {
		//	new AlertDialog.Builder(this).setTitle(R.string.test_result)
		//			.setMessage("Vibration Test Result").setPositiveButton(
		//					R.string.Button_No,
		//					new DialogInterface.OnClickListener() {

		//						public void onClick(DialogInterface dialog,
		//								int which) {
		//							isVibrate = false;
		//						}
		//					}).setNegativeButton(R.string.Button_Yes,
		//					new DialogInterface.OnClickListener() {

		//						public void onClick(DialogInterface dialog,
		//								int which) {
		//							isVibrate = false;
		//						}
		//					}).show();
		//}
		
		@Override
		protected void onDestroy() {
			super.onDestroy();
			isVibrate = false;
			finish();
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
	            	isVibrate = false;
	               setResult(RESULT_OK);
	               BaseActivity.NewstoreRusult(true, "Vibration test",mEngSqlite);//[yeez_haojie add 11.26]
	               finish();
	            }
	        });
	       
	        b = (Button) findViewById(R.id.testfailed);
	        b.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	               //failed
	            	isVibrate = false;
	               setResult(RESULT_FIRST_USER);
	               BaseActivity.NewstoreRusult(false, "Vibration test",mEngSqlite);//[yeez_haojie add 11.26]
	               finish();
	            }
	        });
	     }
}
