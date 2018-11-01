package com.android.TestMode;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AudioRadioGroupFragment extends Fragment {
	
	private static String TAG = "AudioRadioGroupFragment";
	private AudioOnRadioButtonClickListener activityListener;
	//private static AudioContentFragment instance = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
	    return inflater.inflate(R.layout.audio_radiogroup_fragment, container, false);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");
		activityListener = (AudioOnRadioButtonClickListener) activity;
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		activityListener.onRadioButtonClick(AudioScenario.FRAGMENT_RADIOGROUP); // RadioGroup.onCheckedChanged --> set scenario --> AudioContentFragment.onResume()
	}
	
	// factory mode
	// static method <--> static fields
	public static AudioRadioGroupFragment getInstance() {
		return new AudioRadioGroupFragment();
	}

}
