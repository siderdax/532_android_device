package com.android.TestMode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TmuBrain {
	public static final String TAG = "TmuBrain";
	private int index = 1; // current page
	/* index=1: cpu
	 * index=2: pmic
	 * index=3: battery
	 * index=4: pa
	 * index=5: bb
	 */
	private Context context;
//	private MyApp app;
//	private TmuSliderView sliderView;
	private final int N = 5; // number of page
	public static final int FLING_LEFT = 0;
	public static final int FLING_RIGHT = 1;

	
	public int getSum() {
		return N;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public void updateIndexByGesture(int direction) {
		Log.e(TAG, "updateIndexByGesture, before update, index="+index);
		if (direction == FLING_LEFT) {
			if (index < N) index++;
		} else {
			if (index > 1) index--;
		}
		Log.e(TAG, "updateIndexByGesture, after update, index="+index);
	}
	
	public void setContext(Context context) {
		this.context = context;
		
//		if (sliderView== null) {
//			Activity activity = (Activity) context;
//			app = (MyApp) activity.getApplication();
//			sliderView = app.getSliderView();
//		}
	}
	
	public void setSliderView(TmuSliderView sliderView) {
		
	}

	// should be called after updateIndexByGesture, setContext
	public void startActivity() {
		Log.e(TAG, "startActivity");
		Intent intent;
		switch (index) {
		case 1:
			//return "cpu";
			intent = new Intent(context, TmuCpuActivity.class);
			break;
		case 2:
			//return "pmic";
			intent = new Intent(context, TmuPmicActivity.class);
			break;
		case 3:
			//return "battery";
			intent = new Intent(context, TmuBatteryActivity.class);
			break;
		case 4:
			//return "pa";
			intent = new Intent(context, TmuPaActivity.class);
			break;
		case 5:
			//return "bb";
			intent = new Intent(context, TmuBbActivity.class);
			break;
		default:
			return;
		}
		if (context != null)
			context.startActivity(intent);
	}
	
	public void updateSliderView() {	
		Log.e(TAG, "updateSliderView");
		Intent intent = new Intent("com.android.TestMode.TmuTunningActivity");
		intent.putExtra("cmd", "draw");
		if (context != null)
			context.sendBroadcast(intent);
//		if (sliderView != null) {
//			sliderView.updateSliderView();
//		}
	}
}
