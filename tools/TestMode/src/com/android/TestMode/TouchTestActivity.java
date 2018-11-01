package com.android.TestMode;

import android.app.Activity;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.TestMode.TouchTestView.SuccessListener;

public class TouchTestActivity extends Activity implements SuccessListener{
	/** Called when the activity is first created. */
	
	public EngSqlite mEngSqlite;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Display dm = getWindowManager().getDefaultDisplay();
		int width = dm.getWidth();
		int height = dm.getHeight();
		TouchTestView myView = new TouchTestView(this,width,height);
		setContentView(myView);
		mEngSqlite = EngSqlite.getInstance(this);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 0.75f;
		getWindow().setAttributes(lp);
		myView.setListener(this);
		Toast.makeText (this, R.string.touchToast, Toast.LENGTH_LONG).show();
	}
	
	private int backTime = 0;
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backTime ++;
			if (backTime == 1) {
				Toast.makeText(getApplicationContext(), getString(R.string.exit_single_touch_test), Toast.LENGTH_SHORT).show();
				return true;
			}
			setResult(RESULT_FIRST_USER);
            BaseActivity.NewstoreRusult(false, "Touch test",mEngSqlite);//[yeez_haojie add 11.23]
            finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onSuccess() {
		Toast.makeText(TouchTestActivity.this, "success",
				Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		BaseActivity.NewstoreRusult(true, "Touch test", mEngSqlite);
		finish();
		return false;
	}


	@Override
	public void onFail() {
		Toast.makeText(TouchTestActivity.this, "Fail",
				Toast.LENGTH_SHORT).show();
	}

}
