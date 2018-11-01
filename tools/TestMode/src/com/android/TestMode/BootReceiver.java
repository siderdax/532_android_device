
package com.android.TestMode;

import com.android.TestMode.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.android.TestMode.EngSqlite;

// ++ by [yeez_liuwei] 2012.11.20 show a notification about test fail when reboot
public class BootReceiver extends BroadcastReceiver {
    private int sockid = 0;
   // private engfetch mEf;
    private String str = null;
    //private EventHandler mHandler;
    private boolean isAdcCalibrateinfoPass;
    private Context mContext;
    
    private static final int TEST_OK = 0;
    private static final int TEST_FAIL = 1;
    private static final int NO_TEST = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.v("BootReceiver", "onReceive");
        mContext = context;
        if(intent.getAction().equals(intent.ACTION_LOCALE_CHANGED)) {
        	showNotification(mContext, true);
        }
        else {
            initial();
        }
    }

    private void showNotification(Context context, boolean notified) {
//        Boolean adccalibrateinfopass = isAdcCalibrateinfoPass(context);
//        Log.v("BootReceiver", "showNotification adccalibrateinfopass :" + adccalibrateinfopass);
    	int allpass = isAllPass(context);
    	Log.v("BootReceiver", "showNotification allpass :" + allpass);
//        if (adccalibrateinfopass && allpass)
    	if (allpass == TEST_OK)
            return;

    	/* Add pendingintent by huanglongcheng 2014.1.14*/
    	Intent i = new Intent(Intent.ACTION_MAIN);
    	i.setClass(context, TestModeActivity.class); 
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
    	i.putExtra(TestModeActivity.INTENT_CAT_NAME, TestModeActivity.CATEGORY_TEST);
    	
//        Intent bootintent = new Intent(context, TestModeActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
    	
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.icon = R.drawable.icon_sensor;
        if (!notified) {
        	notification.defaults = Notification.DEFAULT_ALL;
        }
        notification.flags = Notification.FLAG_NO_CLEAR;
        if (allpass == TEST_FAIL){
        	notification.setLatestEventInfo(context, context.getString(R.string.test_result_title),
                    context.getString(R.string.test_not_pass), pi);
        }
        else{
        	notification.setLatestEventInfo(context, context.getString(R.string.test_result_title),
                    context.getString(R.string.test_fail), pi);
        }
        if (notified) {
        	nm.cancel(999);
        }
        nm.notify(999, notification);
    }

/*    private boolean isAllPass(Context context) {
        EngSqlite sql = EngSqlite.getInstance(context);
        
        int failCount = sql.queryFailCount();
        Log.v("BootReceiver", "isAllPass failCount :"+failCount);
        if(failCount >= 1){
        	return false;
        }*/
       /* int len = 8;
        for (int i = 0; i <= len; i++) {
            if (sql.getTestGridItemStatus(i) != 1)
                return false;
        }*/
/*        return true;
    }*/
    
    /* change by huanglongcheng 2014.1.15 */
    private int isAllPass(Context context) {
        EngSqlite sql = EngSqlite.getInstance(context);
        
        int failCount = sql.queryFailCount();
        Log.v("BootReceiver", "isAllPass failCount :"+failCount);
        if (failCount == 98){
        	return NO_TEST;
        }
        if (failCount >= 1){
        	return TEST_FAIL;
        }
        
       /* int len = 8;
        for (int i = 0; i <= len; i++) {
            if (sql.getTestGridItemStatus(i) != 1)
                return false;
        }*/
        return TEST_OK;
    }

    private boolean isAdcCalibrateinfoPass(Context context) {
        String calibrationresult = SystemProperties.get("gsm.result.calibration", "Result:Fail");
        Log.v("BootReceiver", "calibrationresult = " + calibrationresult);
        if (calibrationresult.equalsIgnoreCase("Result:Success")) {
            return true;
        } else {
            return false;
        }
    }

    private void initial() {
    	
    	showNotification(mContext, false);
        //mEf = new engfetch();
        //sockid = mEf.engopen();
       /* Looper looper;
        looper = Looper.myLooper();
        mHandler = new EventHandler(looper);
        mHandler.removeMessages(0);
        Message msg = mHandler.obtainMessage(engconstents.ENG_AT_SGMR, 0, 0, 0);
        mHandler.sendMessage(msg);*/
    }

   /* private class EventHandler extends Handler
    {
        public EventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what)
            {
                case engconstents.ENG_AT_SGMR:
                    ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
                    DataOutputStream outputBufferStream = new DataOutputStream(outputBuffer);

                    str = String.format("%d,%d,%d,%d,%d", msg.what, 3, 0, 0, 3);
                    try {
                        outputBufferStream.writeBytes(str);
                    } catch (IOException e) {
                        return;
                    }
                    mEf.engwrite(sockid, outputBuffer.toByteArray(),
                            outputBuffer.toByteArray().length);

                    int dataSize = 512;
                    byte[] inputBytes = new byte[dataSize];
                    int showlen = mEf.engread(sockid, inputBytes, dataSize);
                    String str = new String(inputBytes, 0, showlen);
                    String[] strings = str.split("\n");
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < strings.length; i++) {
                        if (i == 3 || i == 4 || i == 5 || i == 6 || i == 9 || i == 10 || i == 15
                                || i == 16 || i == 17) {
                            sb.append(strings[i]);
                        }
                    }
                    if (sb != null) {
                        if (sb.toString().contains("Fail") || sb.toString().contains("Not Test")) {
                            isAdcCalibrateinfoPass = false;
                        } else {
                            isAdcCalibrateinfoPass = true;
                        }
                    }
                    showNotification(mContext);
                    break;
            }
        }
    }*/
}
