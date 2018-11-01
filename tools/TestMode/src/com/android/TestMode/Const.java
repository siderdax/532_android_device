package com.android.TestMode;

public class Const {

	public static boolean DEBUG = true;
	/*
	 * public static final int[] listIDs =
	 * {R.array.system_list_test,R.array.display_list_test,
	 * R.array.ring_list_test,R.array.phone_list_test,
	 * R.array.accessories_list_test,R.array.wireless_list_test,
	 * R.array.sensor_list_test,R.array.camera_list_test};
	 */

	public static final String ENG_ENGTEST_DB = "/data/data/com.android.TestMode/engtest.db";
	public static final String ENG_STRING2INT_TABLE = "str2int";
	public static final String ENG_STRING2INT_NAME = "name";
	public static final String ENG_STRING2INT_VALUE = "value";
	public static final String ENG_GROUPID_VALUE = "groupid";
	public static final int ENG_ENGTEST_VERSION = 1;

	public static final String TEST_CASE_ARRAY[] = {
			"Version",
			"Touch test",
			"RTC test",// [yeez_haojie modify 2013.2.18]
			// 1
			"Lcd test",
			// "Led test",
			"flashMode test",// [yeez_haojie modify 2013.2.27]
			"Speak test", "Vibration test",
			// 2
			"Sd and Memory test", "Bt test",
			// 3
			"Wifi test", "Audio test",
			"EarpieceAudioloop test",// [yeez_haojie add 11.30]
			"G Sensor test", "P L Sensor test",

			// 6
			"Keyboard test", "Earphone test", "Charge test",

			"Camera test",// [yeez_haojie add 12.3]
			"FrontCamera test",// [yeez_haojie add 12.28]
			// 7
			/*
			 * "Camera test", //8 "Key test", //4 "Charger test",
			 */
			"Gps test",
			// 5
			"Compass test", "Gyroscope test", "Multi point touch test",
			"Sim Card test", "Telephone Test", "Calibration Test",// [yeez_haojie
																	// add 12.4]
			// 9
			"cleardata", // add by [yeez_haojie] 2012.11.27
			"result" };

	public static final int[] TEST_GROUP_ID = { 0, 0, 0, 1, 1, 1, 1, 2, 2, 3,
			3, 3, 6, 6, 6, 7, 8, 4, 4, 5, 5, 5, 5, 5, 9, 9 }; // one 9 add by
																// [yeez_liuwei]
																// 2012.11.7

	public static final String MUTI_TOUCH_TEST = "Muti-TP test";
	public static final Class[] FULL_TEST_ACTIVITY_ARRAY = {
			VersionTestActivity.class,
			TouchTestActivity.class,// SingleTouchPointTest.class,//[yeez_yuxiang.zhang
									// modified 2013.4.25]
			LCDTestActivity.class,
			// 1
			FlashTestActivity.class,
			AudioTestActivity.class,
			/* SingleTouchPointTest.class, */
			VibrationTestActivity.class, // modify by [yeez_liuwei] 20121107
			SDCardTestActivity.class,
			/* CTPTest.class, */
			// 2
			PCBA_BluetoothTestActivity.class,
			WirelessTestActivity.class,
			// 3
			AudioTestActivity.class,
			EarpieceLoopBackTest.class,// [yeez_haojie add 11.30]
			GSensorTestActivity.class, PsensorTestActivity.class,
			KeypadTestActivity.class,

			// 6
			/* BTUTTest.class, */
			EarpieceTestActivity.class, BatteryTestActivity.class,
			GpsTestNew.class,
			// 7
			CompassTestActivity.class,
			// 8
			GyroscopeSensor.class,
			// 4
			MutiTouchTest.class, SIMCardTestActivity.class,
			// 5
			PhoneCallTestActivity.class,

			CameraTestActivity.class,// [yeez_haojie add 12.3]
			FrontCameraTestActivity.class,// [yeez_haojie add 12.28]
			ADCalibrationTestActivity.class,// [yeez_haojie add 12.4]

			RtcNewTest.class,// [yeez_haojie add 2013.2.18]

			TestResultActivity.class

	};

	// add status for test item
	public static final int FAIL = 0;
	public static final int SUCCESS = 1;
	public static final int DEFAULT = 2;
}
