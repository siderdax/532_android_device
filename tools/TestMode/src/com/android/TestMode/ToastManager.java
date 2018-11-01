package com.android.TestMode;

import android.widget.Toast;
import android.content.Context;
import android.view.Gravity;

public class ToastManager {
	//private static Toast spkToast;
	//private static Toast hpToast;
	//private static Toast epToast;
	private static Toast mToast;
	
	public static void showToast(Context context, String msg, int duration) {
		
		if (AudioScenario.barChangeInfo) { // the flag is modified in AudioStorage.recoverScenarioSeekBarValue
			if (mToast == null) {
				mToast = Toast.makeText(context, msg, duration);
			} else {
				mToast.setText(msg);
			}
			mToast.setGravity(Gravity.TOP, 0, 0);
			mToast.show();
		}
	}
};