package com.android.TestMode;

import java.text.SimpleDateFormat;
import java.util.Date;



import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.TextView;

public class RtcNewTest extends Activity{

	private static final String TAG = "RTCTest";
    TextView mContent;
    public static final String timeFormat = "hh:mm:ss";
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat(timeFormat);
    public long mTime;
    public EngSqlite mEngSqlite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setmTestCaseName("RTC test");
        mEngSqlite = EngSqlite.getInstance(this);
        
        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        setContentView(mContent);
        setTitle(R.string.rtc_test);
        mTime = System.currentTimeMillis();
        setTimeText();
    }

    private void setTimeText() {
        mContent.postDelayed(new Runnable() {

            public void run() {
                mContent.setText(getResources().getText(R.string.rtc_tag)+getTime());
                mContent.setTextSize(35);
                
                /*if(isFullTest == 1 && System.currentTimeMillis() - mTime > 3000) {
					onClick(mPassButton);
                }*/
                if( System.currentTimeMillis() - mTime > 3000) {
					
					setResult(RESULT_OK);
		               BaseActivity.NewstoreRusult(true, "RTC test",mEngSqlite);
		               finish();
                }
                else {
                	setTimeText();
                }
            }
        }, 100);
    }

    private String getTime() {
        return TIME_FORMAT.format(new Date());
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
