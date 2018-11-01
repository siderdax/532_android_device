package com.android.TestMode;

import android.app.Application;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class MyApp extends Application {
	
	private static String TAG = "MyApp";
	private AudioFileManager fileManager;
	private TmuBrain tmuBrain;
	private FloatDashedWindowView floatWindow;
	private TmuSliderView sliderView;
	
	private WindowManager windowManager;
	private LayoutParams floatWindowParams;
	
	public MyApp() {
		Log.e(TAG, "constructor");
	}
	
	public void setAudioFileManager(AudioFileManager fileManager) {
		this.fileManager = fileManager;
	}
	
	public AudioFileManager getAudioFileManager() {
		return fileManager;
	}
	
	public void setTmuBrain(TmuBrain tmuBrain) {
		this.tmuBrain = tmuBrain;
	}
	
	public TmuBrain getTmuBrain() {
		return tmuBrain;
	}
	
	public FloatDashedWindowView getFloatWindow() {
		return floatWindow;
	}
	
	public void setFloatWindow(FloatDashedWindowView floatWindow) {
		this.floatWindow = floatWindow;
	}
	
	public TmuSliderView getSliderView() {
		return sliderView;
	}

	public void setSliderView(TmuSliderView sliderView) {
		this.sliderView = sliderView;
	}
}
