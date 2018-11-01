package com.android.TestMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

public class TmuService extends Service {
	
	private String tag = "TmuService";
	private Timer timer;
	private Handler handler = new Handler();
	private WindowManager windowManager;
	private MyApp app;
	private FloatDashedWindowView floatWindow;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(tag, "onStartCommand");
		if (timer == null) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
		}
		app = (MyApp) getApplication();
		windowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
		timer = null;
	}
	
	public boolean isWindowShowing() {
		boolean bool = (app.getFloatWindow() != null);
		Log.e(tag, "isWindowShowing()="+bool);
		return bool;
	}
	
	class RefreshTask extends TimerTask {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (isHome() && isWindowShowing()) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						//windowManager.removeView(app.getFloatWindow());
						//app.setFloatWindow(null);
					}
				});
			}

		}

	}
	
	private boolean isHome() {
		ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
		return getHomes().contains(rti.get(0).topActivity.getPackageName());
	}
	
	private List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		PackageManager packageManager = this.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, 
					PackageManager.MATCH_DEFAULT_ONLY);
		for (ResolveInfo ri : resolveInfo) {
			names.add(ri.activityInfo.packageName);
		}
		return names;
	}

}
