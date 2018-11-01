package com.android.TestMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import com.android.TestMode.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PsensorTestActivity extends Activity {
	/** the value of change color */
	private static final float VALUE_OF_CHANGE_COLOR = 1.5f;

	/** the default value */
	private static final int DEFAULT_VALUE = 0;

	private static final String VALUE_FAR = "Distant";

	private static final String VALUE_CLOSE = "Closer";

	private static final String VALUE_DEFAULT = "0";

	/** sensor manager object */
	private SensorManager pManager = null;

	/** sensor object */
	private Sensor pSensor = null;

	/** sensor listener object */
	private SensorEventListener pListener = null;

	/** the status of p-sensor */
	private TextView psensorTextView;

	/** the textview object */
	private TextView txtData, txtValue;

	private TextView dizaotitle, dizaoValue;

	private static final float MAXIMUM_BACKLIGHT = 1.0f;

	/** Screen backlight when the value of the darkest */
	private static final float MINIMUM_BACKLIGHT = 0.1f;

	/** sensor manager object */
	private SensorManager lManager = null;

	/** sensor object */
	private Sensor lSensor = null;

	/** sensor listener object */
	private SensorEventListener lListener = null;

	/** the progressBar object */
	private ProgressBar lsensorProgressBar;

	/** the textview object */
	private TextView valueIllumination;

	/** the max value of progressBar */
	private static final int MAX_VALUE_PROGRESSBAR = 300;

	/** System backlight value */
	private int mCurrentValue;

	/** Integer into a floating-point type */
	private float mBrightnessValue;

	/** Brightness current value */
	private static final int BRIGHTNESS_CURRENT_VALUE = 180;

	/** Brightness max value */
	private static final float BRIGHTNESS_MAX_VALUE = 255.0f;

	private Context mContext;

	public EngSqlite mEngSqlite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;
		/*
		 * int isFullTest = getIntent().getIntExtra("isFullTest", 0); int
		 * fullTestActivityId = getIntent().getIntExtra("fullTestActivityId",
		 * 0); setIsFullTest(isFullTest, ++fullTestActivityId);
		 */
		// setmTestCaseName("Proximity test");
		setContentView(R.layout.sensor_proximity);

		mEngSqlite = EngSqlite.getInstance(this);
		// setTitle(R.string.proximity_sensor_test);
		psensorTextView = (TextView) findViewById(R.id.txt_psensor);
		txtData = (TextView) findViewById(R.id.txt_data_psensor);
		txtValue = (TextView) findViewById(R.id.txt_value_psensor);

		dizaotitle = (TextView) findViewById(R.id.txt_title_dizhao);

		initSensor();
		readfile();

		pManager.registerListener(pListener, pSensor,
				SensorManager.SENSOR_DELAY_UI);
		getString(DEFAULT_VALUE, VALUE_DEFAULT);

		try {
			mCurrentValue = Settings.System.getInt(getContentResolver(),
					Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			mCurrentValue = BRIGHTNESS_CURRENT_VALUE;
		}
		mBrightnessValue = mCurrentValue / BRIGHTNESS_MAX_VALUE;

		valueIllumination = (TextView) findViewById(R.id.txt_value_lsensor);

		lsensorProgressBar = (ProgressBar) findViewById(R.id.progressbar_lsensor);
		lsensorProgressBar.setMax(MAX_VALUE_PROGRESSBAR);

		lManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);
		lSensor = lManager.getDefaultSensor(Sensor.TYPE_LIGHT);

		lListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor s, int accuracy) {
				readfile();
			}

			public void onSensorChanged(SensorEvent event) {
				float x = event.values[SensorManager.DATA_X];
				showStatus(x);
				readfile();
			}
		};

		readfile();

		setupBottom();
	}

	protected boolean readfile() {
		boolean readsuccess = false;
		File a = new File("/sys/class/input/input0/raw_adc");
		if (a.exists()) {
			try {
				FileInputStream fi = new FileInputStream(a);
				InputStreamReader isr = new InputStreamReader(fi, "GBk");
				BufferedReader bfin = new BufferedReader(isr);
				String rLine = "";
				while ((rLine = bfin.readLine()) != null) {
					Log.v("readfile", "rLine :" + rLine);

					if (rLine.equalsIgnoreCase("0")) {
						// readfile();
						a = new File("/sys/class/input/input0/raw_adc");
						fi = new FileInputStream(a);
						isr = new InputStreamReader(fi, "GBk");
						bfin = new BufferedReader(isr);
						while ((rLine = bfin.readLine()) != null) {
							Log.v("readfile", "newrLine :" + rLine);
							dizaotitle
									.setText(getString(R.string.psensor_dizhao_title)
											+ rLine);
						}

						// dizaotitle.setText(getString(R.string.psensor_dizhao_wait));
					} else {
						dizaotitle
								.setText(getString(R.string.psensor_dizhao_title)
										+ rLine);
					}

					readsuccess = true;
					return true;
				}
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}

		} else {
			readsuccess = false;
			return false;
		}
		return readsuccess;
	}

	@Override
	protected void onResume() {
		super.onResume();
		getString(DEFAULT_VALUE, VALUE_DEFAULT);
		psensorTextView.setBackgroundColor(Color.WHITE);

		valueIllumination.setText(" " + DEFAULT_VALUE);
		lsensorProgressBar.setProgress(DEFAULT_VALUE);
		lManager.registerListener(lListener, lSensor,
				SensorManager.SENSOR_DELAY_UI);

		readfile();
	}

	@Override
	protected void onPause() {
		lManager.unregisterListener(lListener);
		setBrightness(mBrightnessValue);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (pManager != null) {
			pManager.unregisterListener(pListener);
		}
		super.onDestroy();
	}

	private void showStatus(float x) {
		// The numerical value display light
		valueIllumination.setText(" " + x);

		// The ProgressBar display light
		int valueProgress = (int) x;
		lsensorProgressBar.setProgress(valueProgress);

		// The screen display light
		float mCurrentBrightnessValue = x / BRIGHTNESS_MAX_VALUE;
		if (mCurrentBrightnessValue > MAXIMUM_BACKLIGHT) {
			setBrightness(MAXIMUM_BACKLIGHT);
		} else if (mCurrentBrightnessValue < MINIMUM_BACKLIGHT) {
			setBrightness(MINIMUM_BACKLIGHT);
		} else {
			setBrightness(mCurrentBrightnessValue);
		}
	}

	private void setBrightness(float brightness) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = brightness;
		getWindow().setAttributes(lp);
	}

	private void getString(float data, String value) {
		txtData.setText("");
		if (pSensor != null)
			txtData.append("chip id: " + pSensor.getName() + "\n");

		String tdata = "";
		if (data == 0.0) {
			tdata = getString(R.string.P_data_j);
		} else {
			tdata = getString(R.string.P_data_y);
		}
		txtData.append(getString(R.string.psensor_msg_data) + " " + tdata);

		// txtData.append(getString(R.string.psensor_msg_data) + " " + data);
		// txtData.setText(getString(R.string.psensor_msg_data) + " " + data);
		txtValue.setText(getString(R.string.psensor_msg_value) + " " + value);
	}

	private void initSensor() {
		pManager = (SensorManager) this
				.getSystemService(Context.SENSOR_SERVICE);
		pSensor = pManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		pListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor s, int accuracy) {
			}

			public void onSensorChanged(SensorEvent event) {
				float x = event.values[SensorManager.DATA_X];
				if (x <= VALUE_OF_CHANGE_COLOR) {
					getString(x, VALUE_CLOSE);
					psensorTextView.setBackgroundColor(Color.RED);
					Toast.makeText(mContext, "Success", Toast.LENGTH_SHORT)
							.show();
					readfile();
					/*
					 * if(isFullTest==1){ finish(); }
					 */
				} else {
					getString(x, VALUE_FAR);
					psensorTextView.setBackgroundColor(Color.WHITE);
					readfile();
				}
			}
		};
	}

	// /**
	// * @param view view
	// */
	// public void onPSensor(View view) {
	// manager.registerListener(listener, sensor,
	// SensorManager.SENSOR_DELAY_UI);
	// getString(STATUS_ON, DEFAULT_VALUE, VALUE_DEFAULT);
	// }
	//
	// /**
	// * @param view view
	// */
	// public void offPSensor(View view) {
	// manager.unregisterListener(listener);
	// getString(STATUS_OFF, DEFAULT_VALUE, VALUE_DEFAULT);
	// }

	private void setupBottom() {
		Button b = (Button) findViewById(R.id.testpassed);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// passed
				setResult(RESULT_OK);
				BaseActivity
						.NewstoreRusult(true, "P L Sensor test", mEngSqlite);
				finish();
			}
		});

		b = (Button) findViewById(R.id.testfailed);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// failed
				setResult(RESULT_FIRST_USER);
				BaseActivity.NewstoreRusult(false, "P L Sensor test",
						mEngSqlite);
				finish();
			}
		});
	}
}
