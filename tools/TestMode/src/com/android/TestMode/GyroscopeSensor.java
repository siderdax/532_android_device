package com.android.TestMode;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GyroscopeSensor extends Activity{

	 /** Called when the activity is first created. */
	TextView myTextView1;
	TextView myTextView2;
	TextView myTextView3;
	SensorManager mySensorManager;
	public EngSqlite mEngSqlite;//[yeez_haojie add 11.27]
	boolean bGSensorResult = false;
	boolean bPSensorResult = false;
	protected SensorEvent mLastEvent;
	float[] mValues = new float[]{-1,-1,-1};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gyroscope_sensor);
        mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.27]
        myTextView1 = (TextView) findViewById(R.id.myTextView1);
        myTextView2 = (TextView) findViewById(R.id.myTextView2);
        myTextView3 = (TextView) findViewById(R.id.myTextView3);

      	myTextView1.setText("FAIL");
      	mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

	setupBottom();
    }
   
    private SensorEventListener myGSensorListener = new SensorEventListener(){

    	//public void onAccuracyChanged(int sensor,int accuracy){}
    	//public void onAccuracyChanged(int sensor,int accuracy){}
    	public void onAccuracyChanged(Sensor sensor,int value){}

    	//public void onSensorChanged(int sensor,float[] values){
    	//public void onSensorChanged(int sensor,float[] values){
    	public void onSensorChanged(SensorEvent event){
    		float[] values = event.values;
    		
			/*if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				myTextView1.setText("x: " + values[0]);
				myTextView2.setText("y: " + values[1]);
				myTextView3.setText("z: " + values[2]);
				bGSensorResult = true;
				if (mLastEvent != null && mValues[0] != values[0]
						&& mValues[1] != values[1] && mValues[2] != values[2]) {
					Log.d("leo", "onSensorChanged");
					Button b = (Button) findViewById(R.id.testpassed);
					b.setVisibility(View.VISIBLE);
				}
				mValues[0] = values[0];
				mValues[1] = values[1];
				mValues[2] = values[2];
				mLastEvent = event;
			}*/
    		 
			if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				myTextView1.setText("x: " + values[0]);
				myTextView2.setText("y: " + values[1]);
				myTextView3.setText("z: " + values[2]);
				bGSensorResult = true;
				if (mLastEvent != null && mValues[0] != values[0]
						&& mValues[1] != values[1] && mValues[2] != values[2]) {
					Log.v("GyroscopeSensor", "onSensorChanged");
					Button b = (Button) findViewById(R.id.testpassed);
					b.setVisibility(View.VISIBLE);
				}
				mValues[0] = values[0];
				mValues[1] = values[1];
				mValues[2] = values[2];
				mLastEvent = event;
			}
    		 
    	//	if(sensor == SensorManager.SENSOR_PROXIMITY)
    	//		myTextView4.setText("Distance: " + values[0]);
    	}   	
    };
    
    private SensorEventListener myPSensorEventListener = new SensorEventListener(){

    	//public void onAccuracyChanged(int sensor,int accuracy){}
    	public void onAccuracyChanged(Sensor sensor,int value){}

    	//public void onSensorChanged(int sensor,float[] values){
    	public void onSensorChanged(SensorEvent event){
    		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY)
    			{}
    			
    	}   	
    };
 
    @Override
    protected void onResume(){
    	mySensorManager.registerListener(
   			myPSensorEventListener,
    		mySensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY),
        	SensorManager.SENSOR_DELAY_UI);
    	
    	/*mySensorManager.registerListener(
    		myGSensorListener,
    		mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
    		SensorManager.SENSOR_DELAY_UI);*/
    	
    	mySensorManager.registerListener(
        		myGSensorListener,
        		mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
        		SensorManager.SENSOR_DELAY_UI);
    	
    	super.onResume();
    } 
    @Override
    protected void onPause(){
    	mySensorManager.unregisterListener(myGSensorListener);
    	mySensorManager.unregisterListener(myPSensorEventListener);
    	super.onPause();
     }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setupBottom() {
       Button b = (Button) findViewById(R.id.testpassed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //passed
              setResult(RESULT_OK);
              BaseActivity.NewstoreRusult(true, "Gyroscope test",mEngSqlite);//[yeez_haojie add 11.27]
              finish();
           }
       });
       
       b = (Button) findViewById(R.id.testfailed);
       b.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
              //failed
              setResult(RESULT_FIRST_USER);
              BaseActivity.NewstoreRusult(false, "Gyroscope test",mEngSqlite);//[yeez_haojie add 11.27]
              finish();
           }
       });
    }

}
