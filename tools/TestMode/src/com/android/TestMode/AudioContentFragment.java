package com.android.TestMode;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AudioContentFragment extends Fragment {
	
	private static String TAG = "AudioContentFragment";
	private TextView contentText;
	private AudioOnRadioButtonClickListener activityListener;
	//private static AudioContentFragment instance = null;
	private AudioTunningActivity realActivity;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_PLAYBACK
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_PLAYBACK_SINGLE) // primary scenario
			return inflater.inflate(R.layout.audio_playback_single_fragment, container, false); 
		else if (AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_PLAYBACK // second scenario
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_PLAYBACK_DUAL)
			return inflater.inflate(R.layout.audio_playback_dual_fragment, container, false);
		
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_RECORD  //primary scenario
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_RECORD_COMMON)
			return inflater.inflate(R.layout.audio_record_common_fragment, container, false);
		
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_RECORD  //second scenario
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_RECORD_CALL)
			return inflater.inflate(R.layout.audio_record_call_fragment, container, false);
		
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_VOICE_CALL // primary scenario
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK) // second scenario, downlink content fragment is loaded as default
			return inflater.inflate(R.layout.audio_voice_downlink_fragment, container, false);
		else if (AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_VOICE_CALL
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_VOICE_CALL_UPLINK) 
			return inflater.inflate(R.layout.audio_voice_uplink_fragment, container, false);
		
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL // primary scenario
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE) // second sceanario
			return inflater.inflate(R.layout.audio_bt_phone_fragment, container, false);
		else if (AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP) // second scenario
			return inflater.inflate(R.layout.audio_bt_voip_fragment, container, false);
		
		else if(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_FM
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_FM_LISTEN)
			return inflater.inflate(R.layout.audio_fm_listen_fragment, container, false);
		else if (AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_FM
				&& AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_FM_RECORD)
			return inflater.inflate(R.layout.audio_fm_record_fragment, container, false);
		
		else // include SCENARIO_NONE
			return inflater.inflate(R.layout.audio_welcome_fragment, container, false); 
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach");
		realActivity = (AudioTunningActivity)activity;
		activityListener = (AudioOnRadioButtonClickListener) activity;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}
	
	private void stopAllTest() {
		// stop playback test
//		if (realActivity.playbackPage.singlePlaying || realActivity.playbackPage.dualPlaying) {
//			Log.d(TAG, "stop single and dual playing");
//			realActivity.playbackPage.singlePlaying = false;
//			realActivity.playbackPage.dualPlaying = false;
//			realActivity.playbackPage.mediaPlayer.stop();
//			realActivity.playbackPage.mediaPlayer.release();
//			realActivity.playbackPage.mediaPlayer = null;
//		}
//		// stop record test
//		if (realActivity.recordPage.recording) {
//			Log.d(TAG, "stop recording");
//			realActivity.recordPage.recording = false;
//			realActivity.recordPage.mediaRecorder.stop();
//			realActivity.recordPage.mediaRecorder.release();
//			realActivity.recordPage.mediaRecorder = null;	
//		}
		
		// refactor code here
		if (realActivity.isPlaybackTesting()) {
			Log.d(TAG, "stop playback test");
			realActivity.stopPlaybackTest();
		}
		
		if (realActivity.isRecordTesting()) {
			Log.d(TAG, "stop record test");
			realActivity.stopRecordTest();
		}
		
		// voice call and bluetooth call test is by dialing
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView, second scenario="+AudioScenario.querySecondScenario());
		switch (AudioScenario.queryPrimaryScenario()) {
		case AudioScenario.SCENARIO_PLAYBACK:
			stopAllTest();
			break;
		case AudioScenario.SCENARIO_RECORD:
			stopAllTest();
			break;
		case AudioScenario.SCENARIO_VOICE_CALL:
			stopAllTest();
			break;
		case AudioScenario.SCENARIO_BLUETOOTH_CALL:
			stopAllTest();
			break;
		case AudioScenario.SCENARIO_FM:
			stopAllTest();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		activityListener.onRadioButtonClick(AudioScenario.FRAGMENT_CONTENT); // RadioGroup.onCheckedChanged --> set scenario --> AudioContentFragment.onResume()
	}
	
	// factory mode
	// static method <--> static fields
	public static AudioContentFragment getInstance() {
		return new AudioContentFragment();
	}

}
