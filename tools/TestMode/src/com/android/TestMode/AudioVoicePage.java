package com.android.TestMode;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
//import android.os.IAudioTunningService;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.TestMode.TabSwitcher;
import com.android.TestMode.TabSwitcher.OnItemClickListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.AudioManager;
import com.android.TestMode.VerticalSeekBar;
import com.android.TestMode.VerticalSeekBar.OnVerticalSeekBarChangeListener;
import android.net.Uri;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;


public class AudioVoicePage {
	
	private String TAG = "AudioVoicePage";
	
	//private IAudioTunningService audiotunningManager;
	private AudioTunningActivity activity;
	private AudioManager audioManager;
	
	private static final int POSITION_VOICE_CALL_DOWNLINK = 0;
	private static final int POSITION_VOICE_CALL_UPLINK = 1;
	
	private TabSwitcher voiceTabSwitcher;
	public static int currentPosition = POSITION_VOICE_CALL_DOWNLINK;
	
	// VOICE DOWNLINK SCENARIO
	private SeekBar voiceDownSpkBar;
	private SeekBar voiceDownHpBar;
	private SeekBar voiceDownEpBar;
	private VerticalSeekBar voiceDownEqBand1Bar;
	private VerticalSeekBar voiceDownEqBand2Bar;
	private VerticalSeekBar voiceDownEqBand3Bar;
	private VerticalSeekBar voiceDownEqBand4Bar;
	private VerticalSeekBar voiceDownEqBand5Bar;
	private Button voiceDownImportButton;
	private Button voiceDownTestButton;
	private Button voiceDownForeverButton;
	
	// VOICE UPLINK SCENARIO
	private SeekBar voiceUpMainMicBar;
	private SeekBar voiceUpHeadsetMicBar;
	private Button voiceUpImportButton;
	private Button voiceUpTestButton;
	private Button voiceUpForeverButton;
	
	private int downCount = 0;
	private int upCount = 0;
	
	// for write lazy code
	private Set<String> downNameSet = new HashSet<String>();
	private Map<String,SeekBar> downNameAddrMap = new HashMap<String,SeekBar>();
	
	private Set<String> upNameSet = new HashSet<String>();
	private Map<String,SeekBar> upNameAddrMap = new HashMap<String,SeekBar>();
	
	public AudioVoicePage(AudioTunningActivity activity, AudioManager audioManager) {
		this.activity = activity;
		//this.audiotunningManager = audiotunningManager;
		this.audioManager = audioManager;
	}
	
	public static void clearSwitchFragmentCurrentPosition() {
		currentPosition = POSITION_VOICE_CALL_DOWNLINK;
	}
	
	public void setVoiceTabSwitcherFragmentClickListener() {
		voiceTabSwitcher = (TabSwitcher)activity.findViewById(R.id.voice_tabswitcher);
		voiceTabSwitcher.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClickListener(View view, int position) {
				
				activity.changeLayoutBackground(AudioScenario.MODE_CONTENT_PIC);
				switch (position) {
				case POSITION_VOICE_CALL_DOWNLINK:
					ToastManager.showToast(activity.getApplicationContext(), "手机电话下行", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK);
					break;
				case POSITION_VOICE_CALL_UPLINK:
					ToastManager.showToast(activity.getApplicationContext(), "手机电话上行", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_VOICE_CALL_UPLINK);
					break;
				default:
					break;
				}
				
				Log.d(TAG, "currentPosition="+currentPosition+",position="+position);
				
				if (currentPosition != position) {
					FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
					AudioContentFragment contentFragment = AudioContentFragment.getInstance();
					transaction.replace(R.id.content_layout, contentFragment);
					transaction.commit();
				}
				currentPosition = position;
			}
		});
	}
	
	private void resetDownSeekBarValue() {
		int spkVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",name=volume"));
		downNameAddrMap.get(AudioStorage.SPK_VOL).setProgress(spkVol);
		int hpVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+",name=volume"));
		downNameAddrMap.get(AudioStorage.HP_VOL).setProgress(spkVol);
		int epVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+",name=volume"));
		downNameAddrMap.get(AudioStorage.EP_VOL).setProgress(spkVol);
		int eq1 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",name=eq1"));
		downNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(spkVol);
		int eq2 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",name=eq2"));
		downNameAddrMap.get(AudioStorage.EQ_COEFF2).setProgress(spkVol);
		int eq3 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",name=eq3"));
		downNameAddrMap.get(AudioStorage.EQ_COEFF3).setProgress(spkVol);
		int eq4 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",name=eq4"));
		downNameAddrMap.get(AudioStorage.EQ_COEFF4).setProgress(spkVol);
		int eq5 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",name=eq5"));
		downNameAddrMap.get(AudioStorage.EQ_COEFF5).setProgress(spkVol);
	}
	
	public void setVoiceDownContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "手机电话下行", Toast.LENGTH_SHORT);
		voiceDownSpkBar = (SeekBar) activity.findViewById(R.id.voice_downlink_spk_vol_bar);
		voiceDownHpBar = (SeekBar) activity.findViewById(R.id.voice_downlink_hp_vol_bar);
		voiceDownEpBar = (SeekBar) activity.findViewById(R.id.voice_downlink_ep_vol_bar);
		voiceDownEqBand1Bar = (VerticalSeekBar) activity.findViewById(R.id.voice_downlink_eq_band1_bar);
		voiceDownEqBand2Bar = (VerticalSeekBar) activity.findViewById(R.id.voice_downlink_eq_band2_bar);
		voiceDownEqBand3Bar = (VerticalSeekBar) activity.findViewById(R.id.voice_downlink_eq_band3_bar);
		voiceDownEqBand4Bar = (VerticalSeekBar) activity.findViewById(R.id.voice_downlink_eq_band4_bar);
		voiceDownEqBand5Bar = (VerticalSeekBar) activity.findViewById(R.id.voice_downlink_eq_band5_bar);
		voiceDownImportButton = (Button) activity.findViewById(R.id.voice_downlink_import_filter_btn);
		voiceDownTestButton = (Button) activity.findViewById(R.id.voice_downlink_test_btn);
		voiceDownForeverButton = (Button) activity.findViewById(R.id.voice_downlink_forever_btn);
		
		downNameAddrMap.put(AudioStorage.SPK_VOL, voiceDownSpkBar);
		downNameAddrMap.put(AudioStorage.HP_VOL, voiceDownHpBar);
		downNameAddrMap.put(AudioStorage.EP_VOL, voiceDownEpBar);
		downNameAddrMap.put(AudioStorage.EQ_COEFF1, voiceDownEqBand1Bar);
		downNameAddrMap.put(AudioStorage.EQ_COEFF2, voiceDownEqBand2Bar);
		downNameAddrMap.put(AudioStorage.EQ_COEFF3, voiceDownEqBand3Bar);
		downNameAddrMap.put(AudioStorage.EQ_COEFF4, voiceDownEqBand4Bar);
		downNameAddrMap.put(AudioStorage.EQ_COEFF5, voiceDownEqBand5Bar);
		
//		nameSet.add(AudioStorage.SPK_VOL);
//		nameSet.add(AudioStorage.HP_VOL);
//		nameSet.add(AudioStorage.EP_VOL);
//		nameSet.add(AudioStorage.EQ_COEFF1);
//		nameSet.add(AudioStorage.EQ_COEFF2);
//		nameSet.add(AudioStorage.EQ_COEFF3);
//		nameSet.add(AudioStorage.EQ_COEFF4);
//		nameSet.add(AudioStorage.EQ_COEFF5);
		downNameSet = downNameAddrMap.keySet();

		Iterator<String> iterator;
		// manage seekbar name of the voice call downlink scenario
		iterator = downNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, name);
		}
//		for (int i=0; i<nameSet.size(); i++) {
//			String name = nameSet.get(i);
//			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, name);
//		}
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.SPK_VOL);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.HP_VOL);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EP_VOL);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF1);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF2);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF3);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF4);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF5);
		
		// bind string and addr of ui resources
		iterator = downNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = downNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, name, seekBar);
		}
//		for (int i=0; i<nameAddrMap.size(); i++) {
//			String name = nameSet.get(i);
//			SeekBar seekBar = nameAddrMap.get(name);
//			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, name, seekBar);
//		}
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.SPK_VOL,
//				playbackSingleSpkBar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.HP_VOL,
//				playbackSingleHpBar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EP_VOL,
//				playbackSingleEpBar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF1,
//				playbackSingleEqBand1Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF2,
//				playbackSingleEqBand2Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF3,
//				playbackSingleEqBand3Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF4,
//				playbackSingleEqBand4Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF5,
//				playbackSingleEqBand5Bar);
		

		// put init value in storage
		if (downCount == 0) {
			//resetDownSeekBarValue();
			iterator = downNameSet.iterator(); // reset iterator
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = downNameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, name, value);
			}
		}
		downCount++;
//		for (int i=0; i<nameSet.size(); i++) {
//			String name = nameSet.get(i);
//			SeekBar seekBar = nameAddrMap.get(name);
//			int value = seekBar.getProgress();
//			AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, name, value);
//		}
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.SPK_VOL, playbackSingleSpkBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.HP_VOL, playbackSingleHpBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EP_VOL, playbackSingleEpBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF1, 
//												playbackSingleEqBand1Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF2, 
//												playbackSingleEqBand2Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF3, 
//												playbackSingleEqBand3Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF4, 
//												playbackSingleEqBand4Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF5, 
//												playbackSingleEqBand5Bar.getProgress());
		
		voiceDownSpkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(),
												"扬声器音量："+progress+"%", 
												Toast.LENGTH_SHORT);				
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, 
														AudioStorage.SPK_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的扬声器通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownHpBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(),
												"耳机音量："+progress+"%", 
												Toast.LENGTH_SHORT);				
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, 
														AudioStorage.SPK_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+",volume="+bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的耳机通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownEpBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(),
												"听筒音量："+progress+"%", 
												Toast.LENGTH_SHORT);				
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, 
														AudioStorage.SPK_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的听筒通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownEqBand1Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
			@Override
			public void onProgressChanged(VerticalSeekBar bar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				ToastManager.showToast(activity.getApplicationContext(), 
						   "0-70Hz频段EQ增益："+progress+"%", 
						   Toast.LENGTH_SHORT);
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				//AudioStorage.eqCoeff1 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF1, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET+
//	   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);				
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+
//						   				   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将0-70Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
			
		});
		
		voiceDownEqBand2Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
			@Override
			public void onProgressChanged(VerticalSeekBar bar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				ToastManager.showToast(activity.getApplicationContext(), 
						   "70-270Hz频段EQ增益："+progress+"%", 
						   Toast.LENGTH_SHORT);
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				//AudioStorage.eqCoeff1 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF2, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET+
//	   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);				
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+
//						   				   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将70-270Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownEqBand3Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
			@Override
			public void onProgressChanged(VerticalSeekBar bar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				ToastManager.showToast(activity.getApplicationContext(), 
						   "0.27-1KHz频段EQ增益："+progress+"%", 
						   Toast.LENGTH_SHORT);
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				//AudioStorage.eqCoeff1 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF3, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET+
//	   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);				
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+
//						   				   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将0.27-1KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownEqBand4Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
			@Override
			public void onProgressChanged(VerticalSeekBar bar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				ToastManager.showToast(activity.getApplicationContext(), 
						   "1-3.7KHz频段EQ增益："+progress+"%", 
						   Toast.LENGTH_SHORT);
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				//AudioStorage.eqCoeff1 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF4, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET+
//	   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);				
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+
//						   				   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将1-3.7KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownEqBand5Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
			@Override
			public void onProgressChanged(VerticalSeekBar bar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				ToastManager.showToast(activity.getApplicationContext(), 
						   "3.7-14.5KHz频段EQ增益："+progress+"%", 
						   Toast.LENGTH_SHORT);
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				Log.d(TAG, "onStartTrackingTouch");
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar bar) {
				// TODO Auto-generated method stub
				//AudioStorage.eqCoeff1 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, AudioStorage.EQ_COEFF5, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADPHONES+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET+
//	   										",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);				
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_EARPIECE+
//						   				   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将3.7-14.5KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		voiceDownImportButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//ToastManager.showToast(activity.getApplicationContext(), "选择包含滤波器参数的目标文件", Toast.LENGTH_SHORT);	
				// should not use activity.this here since AudioPlaybackPage is not activity itself
				ToastManager.showToast(activity.getApplicationContext(), "选择一个后缀名为flt的文件", Toast.LENGTH_SHORT);
				Intent intent = new Intent(activity.getApplicationContext(), SDFileExplorerActivity.class);
				intent.putExtra("total", ""+4);
				intent.putExtra("path1", ""+AudioScenario.PATH_VOICE_CALL_SPEAKER);
				intent.putExtra("path2", ""+AudioScenario.PATH_VOICE_CALL_HEADPHONES);
				intent.putExtra("path3", ""+AudioScenario.PATH_VOICE_CALL_HEADSET);
				intent.putExtra("path4", ""+AudioScenario.PATH_VOICE_CALL_EARPIECE);
				activity.startActivity(intent);
			}
		});
		
		voiceDownTestButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Uri uri = Uri.parse("tel:112");
				Intent intent = new Intent(Intent.ACTION_DIAL, uri);
				activity.startActivity(intent);
			}
			
		});
		
		voiceDownForeverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		}); 
	}
	
	private void resetUpSeekBarValue() {
		int mainMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_MAIN_MIC+",name=volume"));
		upNameAddrMap.get(AudioStorage.MAIN_MIC_VOL).setProgress(mainMicVol);
		int hpMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET_MIC+",name=volume"));
		upNameAddrMap.get(AudioStorage.HEADSET_MIC_VOL).setProgress(hpMicVol);
	}
	
	public void setVoiceUpContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "手机电话上行", Toast.LENGTH_SHORT);
		voiceUpMainMicBar = (SeekBar) activity.findViewById(R.id.voice_uplink_main_mic_bar);
		voiceUpHeadsetMicBar = (SeekBar) activity.findViewById(R.id.voice_uplink_headset_mic_bar);
		voiceUpTestButton = (Button) activity.findViewById(R.id.voice_uplink_test_btn);
		voiceUpForeverButton = (Button) activity.findViewById(R.id.voice_uplink_forever_btn);
		
		upNameAddrMap.put(AudioStorage.MAIN_MIC_VOL, voiceUpMainMicBar);
		upNameAddrMap.put(AudioStorage.HEADSET_MIC_VOL, voiceUpHeadsetMicBar);
		
		upNameSet = upNameAddrMap.keySet();
		Iterator<String> iterator;
		
		// put seekbar name string together
		iterator = upNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, name);
		}
		
		// bind seekbar name and addr
		iterator = upNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = upNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, name, seekBar);
		}
		

		// init value
		if (upCount == 0) {
			//resetUpSeekBarValue();
			iterator = upNameSet.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = upNameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, name, value);
			}
		}
		upCount++;
		
		voiceUpMainMicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"手机麦克音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, 
													AudioStorage.MAIN_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_MAIN_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的手机麦克通路音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		voiceUpHeadsetMicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"耳机麦克音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, 
													AudioStorage.HEADSET_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_CALL_HEADSET_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的耳机麦克通路音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		voiceUpTestButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Uri uri = Uri.parse("tel:112");
				Intent intent = new Intent(Intent.ACTION_DIAL, uri);
				activity.startActivity(intent);
			}
			
		});
		
		voiceUpForeverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		}); 
		
	}
	
}
