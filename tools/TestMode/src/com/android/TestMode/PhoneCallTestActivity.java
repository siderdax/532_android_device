package com.android.TestMode;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;


import com.android.TestMode.R;

public class PhoneCallTestActivity extends Activity{

	TextView mContent;
	TextView phone_tv;
	Button bok; 
	Button bfail;
	public EngSqlite mEngSqlite;//[yeez_haojie add 11.27]
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.27]
		/*int isFullTest = getIntent().getIntExtra("isFullTest", 0);
		int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId", 0);
        setIsFullTest(isFullTest, ++fullTestActivityId);*/
		//setmTestCaseName("PhoneCall test");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*mFailButton.setVisibility(View.INVISIBLE);
        mPassButton.setVisibility(View.INVISIBLE);*/
		

		
		mContent = new TextView(this);
		mContent.setGravity(Gravity.CENTER);
		setContentView(mContent);
//		setTitle(R.string.phone_call_test);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		mContent.setText(getResources().getText(R.string.phone_call_test));
		mContent.setTextSize(35);
		Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED",Uri.parse("tel:112"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("factory_mode", true);
		startActivity(intent);
	}

	
	
	private void findviewbyid() {
		// TODO Auto-generated method stub
		bok = (Button) findViewById(R.id.testpassed);
		bfail = (Button) findViewById(R.id.testfailed);
		phone_tv = (TextView) findViewById(R.id.phone_test_tv);
		phone_tv.setGravity(Gravity.CENTER);
		phone_tv.setText(getResources().getText(R.string.phone_call_test));
		phone_tv.setTextSize(35);
		
	}



	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
        /*mFailButton.setVisibility(View.VISIBLE);
        mPassButton.setVisibility(View.VISIBLE);*/
		setContentView(R.layout.phone_call_test);
		findviewbyid();
		setupBottom();
        
		super.onRestart();
	}
	
	private void setupBottom() {
        
		bok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               setResult(RESULT_OK);
               BaseActivity.NewstoreRusult(true, "Telephone test",mEngSqlite);//[yeez_haojie add 11.27]
               finish();
            }
        });
       
       
        bfail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "Telephone test",mEngSqlite);//[yeez_haojie add 12.29]
               finish();
            }
        });
     }
	
}
