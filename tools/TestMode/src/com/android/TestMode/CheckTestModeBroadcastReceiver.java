package com.android.TestMode;

import android.provider.Telephony;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.util.Config;
import android.util.Log;
import android.view.KeyEvent;
import static com.android.TestMode.ClearDataBroadcastReceiver.SECRET_CODE_ACTION;


public class CheckTestModeBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = "TestModeBroadcastReceiver";
    public CheckTestModeBroadcastReceiver() {
    	Log.d(LOG_TAG, "<<<<<<<<<<<<<<<<<<<<<<<<<<<<TestModeBroadcastReceiver :");
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(LOG_TAG, ">>>>>>>>>>>>>>>onReceive ");
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Intent i = new Intent(Intent.ACTION_MAIN);
	    // i.setClass(context, FdnList.class);
	    i.setClass(context, CheckTestModeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            
            i.putExtra(TestModeActivity.INTENT_CAT_NAME, TestModeActivity.CATEGORY_PCBATEST);
            context.startActivity(i);
        }
    }
}
