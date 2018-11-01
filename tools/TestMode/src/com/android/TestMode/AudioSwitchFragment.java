package com.android.TestMode;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AudioSwitchFragment extends Fragment {
	
	private static String TAG = "AudioSwitchFragment";
	private TextView contentText;
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
		
		if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_PLAYBACK)
			return inflater.inflate(R.layout.audio_playback_switch_fragment, container, false);
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_RECORD)
			return inflater.inflate(R.layout.audio_record_switch_fragment, container, false);
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_VOICE_CALL)
			return inflater.inflate(R.layout.audio_voice_switch_fragment, container, false); //downlink is choosed as default, so downlink fragment is loaded as default
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL)
			return inflater.inflate(R.layout.audio_bt_switch_fragment, container, false);
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_FM)
			return inflater.inflate(R.layout.audio_fm_switch_fragment, container, false);
		else // include SCENARIO_NONE
			return inflater.inflate(R.layout.audio_welcome_fragment, container, false); 
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
		activityListener.onRadioButtonClick(AudioScenario.FRAGMENT_TABSWITCHER); // RadioGroup.onCheckedChanged --> set scenario --> AudioContentFragment.onResume()
	}
	
	// factory mode
	// static method <--> static fields
	public static AudioSwitchFragment getInstance() {
		return new AudioSwitchFragment();
	}

}
