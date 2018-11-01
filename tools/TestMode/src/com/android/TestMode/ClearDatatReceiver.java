package com.android.TestMode;

import static com.android.TestMode.ClearDataBroadcastReceiver.SECRET_CODE_ACTION;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ClearDatatReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(SECRET_CODE_ACTION)) {
            Intent i = new Intent();
            i.setClass(context, ClearDataActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
	}

}
