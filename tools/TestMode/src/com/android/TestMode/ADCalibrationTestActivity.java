package com.android.TestMode;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.SystemProperties;//[yeez_haojie add 12.4]

public class ADCalibrationTestActivity extends Activity {
	private TextView textView1;
	private TextView textView2;
	private Button button;
	private Timer timer = new Timer();
	private TimerTask task;
	
	public EngSqlite mEngSqlite;//[yeez_haojie add 12.4]
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0x0001:
				{
					readFile(textView2,time2);
					showDialog();
				}
				break;

			default:
				break;
			}
		};
	};
	private int[] time1 = new int[3];
	private int[] time2 = new int[3];
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.xml.rtc_test);
        textView1 = (TextView) findViewById(R.id.t1);
        //textView1.append(getText(R.string.alarm_test_warn));
        textView2 = (TextView) findViewById(R.id.t2);
        
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 12.4]
        
        Intent intent = new Intent();
        intent.setClassName("com.yeezone.android.eng", "com.yeezone.android.eng.adcCalibrateInfo");
        startActivityForResult(intent, 99);
        if(true)
        	return;
        List<String> resultList = new ArrayList<String>();
        resultList.add(SystemProperties.get("gsm.info_0.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_1.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_2.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_3.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_4.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_5.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_6.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_7.calibration")+"\n");
        resultList.add(SystemProperties.get("gsm.info_8.calibration")+"\n");
        
        for (int i = 0; i < 9; i++) {//[yeez_haojie modfiy 12.24]
        	 String resulttext = resultList.get(i);
             int lenth = resulttext.length();
             Log.v("bit", "resulttext :"+resulttext+" lenth :"+lenth);
             SpannableStringBuilder style = new SpannableStringBuilder(resulttext);//[yeez_haojie add 12.17]
             style.setSpan(new ForegroundColorSpan(Color.GREEN), 5, lenth, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             textView1.append(style);
        }
       
        /*
        TextView versionView = (TextView) findViewById(R.id.t1);
		versionView.append(SystemProperties.get("gsm.info_0.calibration")+"\n"+
				SystemProperties.get("gsm.info_1.calibration")+"\n"+
				SystemProperties.get("gsm.info_2.calibration")+"\n"+
				SystemProperties.get("gsm.info_3.calibration")+"\n"+
				SystemProperties.get("gsm.info_4.calibration")+"\n"+
				SystemProperties.get("gsm.info_5.calibration")+"\n"+
				SystemProperties.get("gsm.info_6.calibration")+"\n"+
				SystemProperties.get("gsm.info_7.calibration")+"\n"+
				SystemProperties.get("gsm.info_8.calibration"));
		versionView.append("\n");
        String proinfo_0 = SystemProperties.get("gsm.info_0.calibration");
		Log.v("RTCTestActivity", "proinfo_0 :"+proinfo_0 );*/
		
		
		
		TextView versionView2 = (TextView) findViewById(R.id.t2);
		/*versionView2.append(SystemProperties.get("gsm.result.calibration"));
		versionView2.append("\n");*/
		String proRESULT = SystemProperties.get("gsm.result.calibration");
		Log.v("RTCTestActivity", "proRESULT :"+proRESULT );
		
		//[yeez_haojie add 12.24 st]
		if(proRESULT.equalsIgnoreCase("Result:Success"))
		{
			textView1.append("GSM FT PASS"+"\n");
			textView1.append("DCS FT PASS"+"\n");
			textView1.append("TD FT PASS"+"\n");
			
			versionView2.append(SystemProperties.get("gsm.result.calibration"));//[yeez_haojie add 2.7]
			versionView2.append("\n");//[yeez_haojie add 2.7]
			versionView2.setTextSize(50);//[yeez_haojie add 2.7]
		}
		//[yeez_haojie add 12.24 end]
		
		setupBottom();
		
       /* button = (Button) findViewById(R.id.nButton);
        button.setText(getText(R.string.alarm_test));
        button.setTextSize(25.0f);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				button.setEnabled(false);
				readFile(textView1,time1);
				task = new TimerTask() {
					
					@Override
					public void run() {
						mHandler.sendEmptyMessage(0x0001);
					}
				};
				timer.schedule(task, 5000);
			}
		});*/
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode == 99 && resultCode == 100) {
    		boolean pass = data.getBooleanExtra("result", false);
    		if(pass) {
                setResult(RESULT_OK);
                BaseActivity.NewstoreRusult(true, "Calibration test",mEngSqlite);
    		} else {
                setResult(RESULT_FIRST_USER);
                BaseActivity.NewstoreRusult(false, "Calibration test",mEngSqlite);
    		}
    		finish();
    	}
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void readFile(TextView textView,int[] i) {
    	try {
			FileInputStream fis = new FileInputStream("/sys/class/rtc/rtc0/time");
			//echo "AT+SPCALIBRATE=2\r" >  /dev/CHNPTY1
			DataInputStream dis = new DataInputStream(fis);
			String str = dis.readLine();
//			Integer.parseInt(str);
//			textView.append(str);
//			textView.append("\n" );
			i[0] = Integer.parseInt(str.substring(0, 2));
			i[1] = Integer.parseInt(str.substring(3, 5));
			i[2] = Integer.parseInt(str.substring(6, 8));
//			textView.append("\n" + i[0]);
//			textView.append("\n" + i[1]);
//			textView.append("\n" + i[2]);
//			textView.append("\n" + dis.readLine());
//			textView.append("\n" + dis.readLine());
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private void showDialog() {
    	if ((time2[2] == time1[2] + 5) || (time2[2] == time1[2] + 6)) {
    		creatPassDialog();
    	} else if (((time2[1] == time1[1] + 1) && (time2[2] + 60 == time1[2] + 5)) || ((time2[1] == time1[1] + 1) && (time2[2] + 60 == time1[2] + 6)) ) {
    		creatPassDialog();
    	} else if (((time2[0] == time1[0] + 1) && (time2[1] + 60 == time1[1] + 1) && (time2[2] + 60 == time1[2] + 5)) || ((time2[0] == time1[0] + 1) && (time2[1] + 60 == time1[1] + 1) && (time2[2] + 60 == time1[2] + 6))) {
    		creatPassDialog();
    	} else if (((time2[0] + 24 == time1[0] + 1) && (time2[1] + 60 == time1[1] + 1) && (time2[2] + 60 == time1[2] + 5)) || ((time2[0] + 24 == time1[0] + 1) && (time2[1] + 60 == time1[1] + 1) && (time2[2] + 60 == time1[2] + 6))) {
    		creatPassDialog();
    	} else {
    		creatFailDialog();
    	}
    	button.setEnabled(true);
	}
    
    private void creatFailDialog() {
    	new AlertDialog.Builder(this)
		.setTitle(getText(R.string.alarm_test))
		.setMessage(getText(R.string.alarm_test_fail))
		.setPositiveButton(getText(R.string.Test_Fail), new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				//failed
              setResult(RESULT_FIRST_USER);
              finish();
			}
		})
		.create().show();
	}

	private void creatPassDialog() {
    	new AlertDialog.Builder(this)
    		.setTitle(getText(R.string.alarm_test))
    		.setMessage(getText(R.string.time_to))
    		.setPositiveButton(getText(R.string.Test_Pass), new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					//passed
	               setResult(RESULT_OK);;
	               finish();
				}
			})
			.create().show();
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
               setResult(RESULT_OK);
               BaseActivity.NewstoreRusult(true, "Calibration test",mEngSqlite);//[yeez_haojie add 12.4]
              
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "Calibration test",mEngSqlite);//[yeez_haojie add 12.4]
              
               finish();
            }
        });
     }
}




//import java.util.Calendar;
//
//import android.app.Activity;
//import android.app.AlarmManager;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.app.PendingIntent;
//import android.app.TimePickerDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.DialogInterface.OnKeyListener;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.KeyEvent;
//import android.view.View;
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TimePicker;
//import android.widget.Toast;
//
//public class RTCTestActivity extends Activity{
//	private Button btn=null;
//    private AlarmManager alarmManager=null;
//    Calendar cal=Calendar.getInstance();
//    final int DIALOG_TIME = 0;    //设置对话框id
//    private AlarmReceiver alarmReceiver;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.xml.rtc_test);
//        
//        alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);
//        btn=(Button)findViewById(R.id.btn);
//        btn.setText(R.string.alarm_set);
//        btn.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View view) {
//                showDialog(DIALOG_TIME);////            }
//        });
//        setupBottom();
//    }
//    
//    @Override
//    protected void onStart() {
//    	super.onStart();
//    	alarmReceiver = new AlarmReceiver(this);
//    	alarmReceiver.registerAction("alarm");
//    }
//    
//    @Override
//    protected void onDestroy() {
//    	super.onDestroy();
//    	unregisterReceiver(alarmReceiver);
//    }
//    
//    @Override
//    protected Dialog onCreateDialog(int id) {
//        Dialog dialog=null;
//        switch (id) {
//        case DIALOG_TIME:
//            dialog=new TimePickerDialog(
//                    this, 
//                    new TimePickerDialog.OnTimeSetListener(){
//                        public void onTimeSet(TimePicker timePicker, int hourOfDay,int minute) {
//                            Calendar c=Calendar.getInstance();//    
//                            long time = System.currentTimeMillis();
//                            Log.e("##################", "time1=" + time);
//                            c.setTimeInMillis(time);        //
//                            c.set(Calendar.HOUR, hourOfDay);        /                            c.set(Calendar.MINUTE, minute);          
//                            c.set(Calendar.SECOND, 0);                                         c.set(Calendar.MILLISECOND, 0);           
//                            Intent intent = new Intent();   
//                            intent.setAction("alarm");
//                            PendingIntent pi = PendingIntent.getBroadcast(RTCTestActivity.this, 0, intent, 0);
//                            long alarmTimeUTC = c.getTimeInMillis() + c.get(Calendar.ZONE_OFFSET);
//                            Log.e("##################", "time2=" + (c.getTime().getTime()-43200000));
//                            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTime().getTime()-43200000, pi);     
////                            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);      
//                            Toast.makeText(RTCTestActivity.this, getString(R.string.set_success), Toast.LENGTH_LONG).show();//
//                        }
//                    }, 
//                    cal.get(Calendar.HOUR_OF_DAY), 
//                    cal.get(Calendar.MINUTE),
//                    true);
//            break;
//        }
//        return dialog;
//    }
//    
//    private void showOptionsDialog() {
//    	new AlertDialog.Builder(RTCTestActivity.this).setTitle(
//					R.string.alarm_test).
//					setMessage(R.string.time_to)//
//					.setPositiveButton(R.string.Test_Pass,
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int whichButton) {
//									setResult(RESULT_OK);
//									RTCTestActivity.this.finish();/
//								}
//
//							})
////							.setNegativeButton(R.string.Button_No,
////							new DialogInterface.OnClickListener() {
////								public void onClick(DialogInterface dialog,
////										int whichButton) {
////									setResult(RESULT_FIRST_USER);
////									RTCTestActivity.this.finish();//Activity
////								}
////							})
////							.setOnKeyListener(new OnKeyListener() {
////
////		        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
////		        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
////		        					
////		        					return true;
////		        		         }
////		        		         return false;
////		        			}
////		        		})
//					.create()
//					.show();
//	}
//    
//    public class AlarmReceiver extends BroadcastReceiver {
//    	
//    	private Context mContext;
//    	
//    	private AlarmReceiver alarmReceiver;
//    	
//    	public AlarmReceiver (Context c) {
//    		mContext = c;
//    		alarmReceiver = this;
//    	}
//    	
//    	public void registerAction(String action) {
//            final IntentFilter intentFilter = new IntentFilter();
//            intentFilter.addAction(action);
//            mContext.registerReceiver(alarmReceiver, intentFilter);
//        }
//    	
//    	@Override
//        public void onReceive(Context context, Intent intent) {
//    		if ("alarm".equals(intent.getAction())){
////    			Toast.makeText(context, "", Toast.LENGTH_LONG).show();
//    			showOptionsDialog();
//    		}
//        }
//    }
//    
//	
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			//moveTaskToBack(true);
//			return true;
//		}
//		return super.onKeyDown(keyCode, event);
//	}
//	
//	private void setupBottom() {
////        Button b = (Button) findViewById(R.id.testpassed);
////        b.setOnClickListener(new View.OnClickListener() {
////            public void onClick(View v) {
////               //passed
////               setResult(RESULT_OK);;
////               finish();
////            }
////        });
//       
//		Button b = (Button) findViewById(R.id.testfailed);
////		b.setGravity(Gravity.CENTER_HORIZONTAL);
//        b.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//               //failed
//               setResult(RESULT_FIRST_USER);
//               finish();
//            }
//        });
//     }
//}


