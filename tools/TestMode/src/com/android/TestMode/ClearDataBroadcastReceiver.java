package com.android.TestMode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * @author haojie.shi
 * add 11.28
 */
public class ClearDataBroadcastReceiver extends BroadcastReceiver{
	
	public static final String SECRET_CODE_ACTION = "android.provider.Telephony.SECRET_CODE";
	private static final String LOG_TAG = "ClearDataBroadcastReceiver";

    
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.d(LOG_TAG, ">>>>>>>>>>>>>>>onReceive ");
        if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Intent i = new Intent(Intent.ACTION_MAIN);
	    // i.setClass(context, FdnList.class);
	    i.setClass(context, ClearDataActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            
            i.putExtra(ClearDataActivity.INTENT_CAT_NAME, ClearDataActivity.CATEGORY_PCBATEST);
            context.startActivity(i);
        }
    }

}
