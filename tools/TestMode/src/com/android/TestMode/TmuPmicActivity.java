package com.android.TestMode;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;


public class TmuPmicActivity extends Activity implements OnTouchListener, OnGestureListener {
	
	private String TAG = "TmuPmicActivity";
	private int screenWidth;
	private int screenHeight;
	private WindowManager windowManager;
	private GestureDetector gestureDetector;
	private int FLING_MIN_DISTANCE = 50;
	private int FLING_MIN_VELOCITY = 0;
	
	private TmuSliderView myView;
	private FloatDashedWindowView floatWindow;
	private ScrollView tmuPmicScrollView;
	private TableLayout tmuPmicLayout;
	private LayoutInflater inflater;
	private ArrayList<View> viewList = new ArrayList<View>();
	private Spinner spinnerTz;
	private Spinner spinnerTrip;
	private Button rebootButton;
	
	private TmuBrain tmuBrain;
	private MyApp app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.tmu_pmic);

		spinnerTz = (Spinner) findViewById(R.id.spinner_tz);
		spinnerTrip = (Spinner) findViewById(R.id.spinner_trip);
		String[] itemsTz = getResources().getStringArray(R.array.spinner_tz);
		String[] itemsTrip = getResources().getStringArray(R.array.spinner_trip);
		ArrayAdapter<String> adapterTz = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsTz);
		ArrayAdapter<String> adapterTrip = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itemsTrip);
		spinnerTz.setAdapter(adapterTz);
		spinnerTrip.setAdapter(adapterTrip);
		spinnerTrip.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String str = parent.getItemAtPosition(position).toString();
//				View v1 = inflater.inflate(R.layout.tmu_trip_point, null);
//				tmuCpuLayout.addView(v1);
//				//tmuCpuScrollView.addView(v1);
//				View v2 = inflater.inflate(R.layout.tmu_trip_point, null);
//				tmuCpuLayout.addView(v2);
//				//tmuCpuScrollView.addView(v2);
//				View v3 = inflater.inflate(R.layout.tmu_trip_point, null);
//				tmuCpuLayout.addView(v3);
//				//tmuCpuScrollView.addView(v3);
				
				// set spinner text color
				TextView textView = (TextView) view;
				if (view != null)
				textView.setTextColor(Color.RED);
				
				int need = Integer.valueOf(str);
				int size = viewList.size();
				Log.d(TAG, "need="+need+",size="+size);
				if (need < size) {
					for (int i=size-1; i>=need; i--) {
						View v = viewList.get(i);
						tmuPmicLayout.removeView(v);
						viewList.remove(i);
					}
				}
				else if (need > size) {
					for (int i=size+1; i<=need; i++) {
						View v = inflater.inflate(R.layout.tmu_trip_point, null);
						viewList.add(v);
						tmuPmicLayout.addView(v);
					}
				}

				for (int i=0; i<viewList.size(); i++) {
					// add number for text and set red color
					View v = viewList.get(i);
					//TableLayout tbl = (TableLayout) v.findViewById(R.id.tmu_trip_point_layout);
					//TableRow row = (TableRow) tbl.getChildAt(0);
					TextView tv = (TextView) v.findViewById(R.id.trip_point_txt);
					tv.setTextColor(Color.RED);
					String s = getResources().getString(R.string.trip_point) + " "+ (i+1);
					tv.setText(s);
					
					//add contents for spinner
					Spinner spinnerCoolerName = (Spinner) v.findViewById(R.id.spinner_cooler_name);
					String[] itemsCoolerName = getResources().getStringArray(R.array.spinner_cooler_name);
					ArrayAdapter<String> adapterCoolerName = new ArrayAdapter<String>(getApplicationContext(), 
								android.R.layout.simple_spinner_item, itemsCoolerName);
					
					spinnerCoolerName.setAdapter(adapterCoolerName);
				}
				
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		rebootButton = (Button) findViewById(R.id.tmu_reboot);
		rebootButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(TmuPmicActivity.this)
				.setTitle("Hint")
				.setMessage("Sure to reboot?")
				.setPositiveButton("Reboot", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
						pm.reboot("reboot");
						
					}
				})
				.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				}).show();
			}
		});
		
		windowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		screenWidth = windowManager.getDefaultDisplay().getWidth();
		screenHeight = windowManager.getDefaultDisplay().getHeight();
		
		gestureDetector = new GestureDetector((OnGestureListener) this);
//		TableLayout tmuLayout = (TableLayout) findViewById(R.id.tmu_pmic_layout);
//		tmuLayout.setOnTouchListener(this);
//		tmuLayout.setClickable(true);
		tmuPmicScrollView = (ScrollView) findViewById(R.id.tmu_pmic_scroll_view);
		tmuPmicLayout = (TableLayout) findViewById(R.id.tmu_pmic_layout);
		tmuPmicScrollView.setOnTouchListener(this);
		tmuPmicScrollView.setClickable(true);
		inflater = LayoutInflater.from(this);
		
		// singleton, all activity share one brain
		app = (MyApp) getApplication();
		tmuBrain = app.getTmuBrain();
		if (tmuBrain == null) {		
			tmuBrain = new TmuBrain();
			app.setTmuBrain(tmuBrain); // must before createFloatWindow
		}
		
		
		createFloatWindow();
		
		// should be called after createFloatWindow
		tmuBrain.setContext(this);
		
		Log.e(TAG, "onCreate");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
		//myView = (TmuSliderView) findViewById(R.id.myView);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		//windowManager.removeView(floatWindow);
		Log.e(TAG, "onDestroy");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.e(TAG, "go back");
			//windowManager.removeView(app.getFloatWindow());
			//app.setFloatWindow(null);
			Log.e(TAG, "onFling, to RIGHT");
			tmuBrain.updateIndexByGesture(TmuBrain.FLING_RIGHT); // The logic strategy is extracted here
			tmuBrain.updateSliderView();
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void createFloatWindow() {
		floatWindow = app.getFloatWindow();
		if (floatWindow == null) {
			Log.e(TAG, "create FloatDashedWindowView");
			floatWindow = new FloatDashedWindowView(this);
			LayoutParams floatWindowParams = new LayoutParams();
			floatWindowParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
			floatWindowParams.format = PixelFormat.RGBA_8888;
			floatWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
			floatWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
			floatWindowParams.width = floatWindow.viewWidth;
			floatWindowParams.height = floatWindow.viewHeight;
			floatWindowParams.x = 0;
			floatWindowParams.y = screenHeight*3/4;
			windowManager.addView(floatWindow, floatWindowParams);
		
			app.setFloatWindow(floatWindow);
		} else {
			Log.e(TAG, "draw FloatDashedWindowView");
			tmuBrain.updateSliderView();
		}
		
	}


	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(arg1); // forward to GestureDetector
		//return true;
	}
	
//	private void sendGesutreMsg(int direction) {
//		
//		Intent intent = new Intent("com.android.TestMode.TmuTunningActivity");
//		
//		if (direction == TmuBrain.FLING_LEFT) {
//			intent.putExtra("direction", "left");
//		} else {
//			intent.putExtra("direction", "right");
//		}
//		sendBroadcast(intent);
//	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			Log.e(TAG, "onFling, to LEFT");
			//myView.notifyGesture(TmuBrain.FLING_LEFT);
//			Intent intent = new Intent("com.android.TestMode.TmuTunningActivity");
//			intent.putExtra("direction", "left");
//			sendBroadcast(intent);
			//sendGesutreMsg(TmuBrain.FLING_LEFT);
			//setContentView(R.layout.tmu_battery);	
			//tmuBrain.setContext(this);
			tmuBrain.updateIndexByGesture(TmuBrain.FLING_LEFT); // The logic strategy is extracted here
			tmuBrain.updateSliderView();

			tmuBrain.startActivity();
		}
		else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			Log.e(TAG, "onFling, to RIGHT");
			//myView.notifyGesture(TmuBrain.FLING_RIGHT);
//			Intent intent = new Intent("com.android.TestMode.TmuTunningActivity");
//			intent.putExtra("direction", "right");
//			sendBroadcast(intent);
			//sendGesutreMsg(TmuBrain.FLING_RIGHT);
			//setContentView(R.layout.tmu_bb);		
			//tmuBrain.setContext(this);
			tmuBrain.updateIndexByGesture(TmuBrain.FLING_RIGHT); // The logic strategy is extracted here
			tmuBrain.updateSliderView();

			tmuBrain.startActivity();
		}
		
		return false;
		
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}
