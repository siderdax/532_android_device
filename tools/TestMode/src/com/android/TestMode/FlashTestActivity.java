package com.android.TestMode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.PowerManager;
import android.os.ServiceManager;
import android.os.Vibrator;
//import android.renderscript.Light;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class FlashTestActivity extends Activity implements SurfaceHolder.Callback{
	static final int FINISH_TEST = 1;
	private int j;
	//private File file1 = new File("/sys/class/leds/led1-green/brightness");
	private File file2 = new File("/sys/class/leds/led1-blue/brightness");
	private File file1 = new File ("/sys/class/leds/keyboard-backlight/brightness");//[yeez_haojie add 12.3]
	private FileOutputStream fos1;
	private FileOutputStream fos2;
	private DataOutputStream dos1;
	private DataOutputStream dos2;
	private FileDescriptor fd1;
	private FileDescriptor fd2;
	public EngSqlite mEngSqlite;//[yeez_haojie add 11.26]
	private Camera mCamera;
	
	
	String flashMode = "off";
	
	
	//[yeez_haojie add 2013.2.27]
	int jf = 0;
  	Runnable runnablef = new Runnable() {
  		public void run() {
  			Message msg = mHandlerf.obtainMessage();
  			msg.arg1 = jf;
  			Log.v("LEDTestActivity", "jf :"+jf);
  			mHandlerf.sendMessage(msg);
  			if (jf != 0 && flashMode.equalsIgnoreCase("on")) {
  				try {
  					
  					Thread.sleep(1000);
  				} catch (InterruptedException e) {
  					e.printStackTrace();
  				}
  				//jf++;
  			}
  			jf++;
  		}
  	};
  	private Handler mHandlerf = new Handler() {
  		public void handleMessage(Message msg) {
  			if (msg.arg1 >= 3) {
  				
  				Log.v("FlashTestActivity", "msg.arg1 :"+msg.arg1);
  				/*if(mHandlerf != null){
  					mHandlerf.removeCallbacks(runnablef);
  				}
  				*/
				if(flashMode.equalsIgnoreCase("on"))
				{
//					writeFile("off");
					openOrCloseFlashLight(false);
					flashMode = "off";
					jf = 0;
					if(mHandlerf != null){
	  					mHandlerf.removeCallbacks(runnablef);
	  					mHandlerf = null; 
	  				}				
				}
				
  			} else {
  				//textView.setBackgroundColor(COLOR[msg.arg1]);
  				 if(mHandlerf != null){
  					mHandlerf.post(runnablef);
  				 }
  				
  			}
  		}
  	};
	//[yeez_haojie add 2013.2.27]
	
	
	Runnable runnable = new Runnable() {
  		public void run() {
  			Message msg = mHandler.obtainMessage();
  			msg.arg1 = j;
  			mHandler.sendMessage(msg);
  			if (j != 0) {
  				try {
  					Thread.sleep(1000);
  				} catch (InterruptedException e) {
  					e.printStackTrace();
  				}
  			}
  			j++;
  		}
  	};
  	
  	private Handler mHandler = new Handler() {
  		public void handleMessage(Message msg) {
  			if (msg.arg1 > 6) {
  				try {
  					dos1.writeBytes("0");
					//fd1.sync();
					/*dos2.writeBytes("0");
					fd2.sync();*/
//            		mPowerManagerService.setLEDLightBrightness(0x00000004, 0);
            		mHandler.removeCallbacks(runnable);
            		j = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
  			} else if (msg.arg1 % 2 == 0) {
  				try {
  					dos1.writeBytes("255");
					//fd1.sync();
					/*dos2.writeBytes("255");
					fd2.sync();*/
//            		mPowerManagerService.setLEDLightBrightness(0x00000004, 1);
            		mHandler.post(runnable);
				} catch (Exception e) {
					e.printStackTrace();
				}
  			} else if (msg.arg1 % 2 == 1) {
  				try {
  					dos1.writeBytes("0");
					//fd1.sync();
					/*dos2.writeBytes("0");
					fd2.sync();*/
//            		mPowerManagerService.setLEDLightBrightness(0x00000004, 0);
            		mHandler.post(runnable);
				} catch (Exception e) {
					e.printStackTrace();
				}
  			}
  		};
  	};
	
//	private IPowerManager mPowerManagerService;

    private final Timer timer = new Timer();
              private TimerTask task1;
              private TimerTask task;
              Handler handler = new Handler() {
                       public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                if(msg.what == FINISH_TEST)
                                {
                                          new AlertDialog.Builder(FlashTestActivity.this)
                                          .setTitle(R.string.led_test)
                                          .setMessage(R.string.DialogMessage)            
                                          .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int whichButton) {
                                                       setResult(RESULT_OK);
                                                       BaseActivity.NewstoreRusult(true, "flash test",mEngSqlite);//[yeez_haojie add 11.26]
                                                       finish();
                                                   }
                                
                                          })
                                          .setNegativeButton(R.string.Button_No, new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int whichButton) {
                                                       setResult(RESULT_FIRST_USER);
                                                       BaseActivity.NewstoreRusult(false, "flash test",mEngSqlite);//[yeez_haojie add 11.26]
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
        SurfaceHolder mHolder;
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

//			mPowerManagerService = IPowerManager.Stub.asInterface(
//                    ServiceManager.getService("power"));
			 mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.26]

		}
		
		Runnable run = new Runnable() {
			
			@Override
			public void run() {
					setContentView(R.xml.led_test);
					init();
			}
		};
		private void init() {
			try {
				fos1 = new FileOutputStream(file1);
				fd1 = fos1.getFD();
				dos1 = new DataOutputStream(fos1);
				/*fos2 = new FileOutputStream(file2);
				fd2 = fos2.getFD();
				dos2 = new DataOutputStream(fos2);*/
			} catch (Exception e) {
				e.printStackTrace();
			}
			Button ledTestButton = (Button) findViewById(R.id.ledTestButton);
			ledTestButton.setTextSize(25.0f);
			
			ledTestButton.setText(R.string.flash_test);//[yeez_haojie add 2013.2.27]
			
			ledTestButton.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					Button b = (Button) findViewById(R.id.testpassed);
					b.setVisibility(View.VISIBLE);
					
					if(flashMode.equalsIgnoreCase("on"))
					{
//						writeFile("off");
						openOrCloseFlashLight(false);
						flashMode = "off";
						jf = 0;//[yeez_haojie add 2013.2.27
						 if(mHandlerf != null)
		            	   {
		            		   mHandlerf.removeCallbacks(runnablef);//[yeez_haojie modfiy 2013.2.27
		            		   mHandlerf = null;
		            		  
		            	   }
					}
					else
					{
//						writeFile("on");
						openOrCloseFlashLight(true);
						flashMode = "on";
						jf = 0;//[yeez_haojie add 2013.2.27
					
					j = 0;

				}
			}
		});
			setupBottom();
	}
		@Override
		protected void onDestroy() {
			super.onDestroy();
			finish();
		}
		
		@Override
		protected void onPause() {
			openOrCloseFlashLight(false);
			mCamera.release();
			flashMode = "off";
			run.run();
			super.onPause();
		}
		
		@Override
		protected void onResume() {
			SurfaceView sv = new SurfaceView(this);
			setContentView(sv);
			mHolder = sv.getHolder();
			mHolder.addCallback(this);
			handler.postDelayed(run, 200);
			super.onResume();
		}
		
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				return true;
			}
			return super.onKeyDown(keyCode, event);
		}
		
		 public static void writeFile(String mode){
		    	File file = new File("/sys/devices/platform/flash-led/leds/flash-led/brightness");
		    	try {
					FileOutputStream fos = new FileOutputStream(file);
					if(mode.equals("on"))
						fos.write("1".getBytes());
					else
						fos.write("0".getBytes());
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		
		private void setupBottom() {
	        Button b = (Button) findViewById(R.id.testpassed);
	        b.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	               setResult(RESULT_OK);
	              // BaseActivity.NewstoreRusult(true, "Led test",mEngSqlite);//[yeez_haojie add 11.26]
	               
	               BaseActivity.NewstoreRusult(true, "flashMode test",mEngSqlite);//[yeez_haojie add 2013.2.27]
	               
	               try {
	            	  // mHandler.removeCallbacks(runnable);//[yeez_haojie modfiy 2013.2.27
	            	   if(mHandlerf != null)
	            	   {
	            		   mHandlerf.removeCallbacks(runnablef);//[yeez_haojie modfiy 2013.2.27
	            	   }
	            	   
						fos1.close();
						dos1.close();
						

					
//							writeFile("off");//[yeez_haojie add 2013.2.27
						openOrCloseFlashLight(false);
							flashMode = "off";
												
						
						/*fos2.close();
						dos2.close();*/
					} catch (Exception e) {
						e.printStackTrace();
					}
	               finish();
	            }
	        });
	       
	        b = (Button) findViewById(R.id.testfailed);
	        b.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	               setResult(RESULT_FIRST_USER);
	              // BaseActivity.NewstoreRusult(false, "Led test",mEngSqlite);//[yeez_haojie add 11.26]
	               
	               BaseActivity.NewstoreRusult(false, "flashMode test",mEngSqlite);//[yeez_haojie add 2013.2.27]

	               
	               try {
	            	  // mHandler.removeCallbacks(runnable);//[yeez_haojie modfiy 2013.2.27
	            	   if(mHandlerf != null)
	            	   {
	            		   mHandlerf.removeCallbacks(runnablef);//[yeez_haojie modfiy 2013.2.27
	            	   }
	            	   
						fos1.close();
						dos1.close();
						

//						writeFile("off");//[yeez_haojie add 2013.2.27
						openOrCloseFlashLight(false);
						flashMode = "off";
						
						
						/*fos2.close();
						dos2.close();*/
					} catch (Exception e) {
						e.printStackTrace();
					}
	               finish();
	            }
	        });
	     }

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mCamera = Camera.open();
			try{
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();
			}catch (Exception e) {
				// TODO: handle exception
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
		private void openOrCloseFlashLight(boolean open) {
			Parameters parameters = mCamera.getParameters();
			if(open)
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
			else
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(parameters);
		}
		
}
