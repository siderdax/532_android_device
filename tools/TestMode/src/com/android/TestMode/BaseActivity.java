package com.android.TestMode;

import android.graphics.Color;
import com.android.TestMode.EngSqlite;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

//public class BaseActivity extends Activity implements OnClickListener{
public class BaseActivity extends Activity {
	private static final String TAG = "BaseActivity";
    protected Button mPassButton;
    protected Button mFailButton;
    protected WindowManager mWindowManager;
    protected String mTestCaseName = null;
    protected int isFullTest = 0;
    protected int fullTestActivityId;
    protected int mGroupId;

    private static final int TEXT_SIZE = 30;


    public String getmTestCaseName() {
        return mTestCaseName;
    }

    public int getTestCaseGroupId(){
    	return mGroupId;
    }

    public void setTestCaseGroupId(int groupId){
    	this.mGroupId = groupId;
    }

    public void setIsFullTest(int isFullTest, int fullTestActivityId){
    	this.isFullTest = isFullTest;
    	this.fullTestActivityId = fullTestActivityId;
    }
    public void setmTestCaseName(String mTestCaseName) {
        this.mTestCaseName = mTestCaseName;
    }
    public static EngSqlite mEngSqlite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      mWindowManager = (WindowManager)getSystemService("window");
        mWindowManager = getWindowManager();
        //int groupId = this.getIntent().getIntExtra("groupId", 0);
        String testName = this.getIntent().getStringExtra("testname");
      /*  if(Const.DEBUG){
        	Log.d(TAG,"groupId" + groupId);
        }*/

        //this.setTestCaseGroupId(groupId);
        setmTestCaseName(testName);
        //createButton(true);
        //createButton(false);
        mEngSqlite = EngSqlite.getInstance(this);


    }

   /* private void createButton(boolean isPassButton) {
        int buttonSize = getResources().getDimensionPixelSize(R.dimen.pass_fail_button_size);
        if(isPassButton) {
            mPassButton = new Button(this);
            mPassButton.setText(R.string.text_pass);
            mPassButton.setTextColor(Color.WHITE);
            mPassButton.setTextSize(TEXT_SIZE);
            mPassButton.setBackgroundColor(Color.GREEN);
            mPassButton.setOnClickListener(this);
        }else {
            mFailButton = new Button(this);
            mFailButton.setText(R.string.text_fail);
            mFailButton.setTextColor(Color.WHITE);
            mFailButton.setTextSize(TEXT_SIZE);
            mFailButton.setBackgroundColor(Color.RED);
            mFailButton.setOnClickListener(this);
        }


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
//                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT);
        lp.gravity = isPassButton?Gravity.LEFT | Gravity.BOTTOM:Gravity.RIGHT | Gravity.BOTTOM;
        lp.flags =LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width=buttonSize;
        lp.height=buttonSize;
        mWindowManager.addView(isPassButton?mPassButton:mFailButton, lp);
    }
*/
    protected void removeButton() {
        try{
            mWindowManager.removeView(mPassButton);
            mWindowManager.removeView(mFailButton);
        }catch(Exception e){
            //TODO
        }
    }
    public void onClick(View v) {
        if(v == mPassButton) {
            Log.d("onclick", "pass.."+this);
            storeRusult(true);
        }else if(v == mFailButton) {
            storeRusult(false);
        }
        finish();
    }

  /* @Override
   public void finish() {
	   if(isFullTest == 1){
		   Intent intent = new Intent(this, Const.FULL_TEST_ACTIVITY_ARRAY[fullTestActivityId]);
		   intent.putExtra("isFullTest", 1);
		   intent.putExtra("fullTestActivityId", fullTestActivityId);
		   intent.putExtra("testname",Const.TEST_CASE_ARRAY[fullTestActivityId]);
		   intent.putExtra("groupId", Const.TEST_GROUP_ID[fullTestActivityId]);

		   isFullTest = 0;
		   this.startActivity(intent);
	   }
	   removeButton();
	   super.finish();
   }*/
   
    /**
     * [yeez_haojie add 11.22]
     * @param mEngSqlite2 
     */
    public  static void NewstoreRusult(boolean isSuccess,String testname, EngSqlite mEngSqlite2) {
    	Log.v(TAG, "NewstoreRusult  testname :"+testname);
    	Log.v(TAG, "NewstoreRusult  isSuccess :"+isSuccess);
    	mEngSqlite2.updateDB8012(testname, isSuccess ? 1 : 0);
    }
   
    public  void storeRusult(boolean isSuccess) {
    	Log.v(TAG, "storeRusult  getmTestCaseName().."+getmTestCaseName());
        mEngSqlite.updateDB(mGroupId,getmTestCaseName(), isSuccess ? 1 : 0);
    }
}