package com.android.TestMode;

import android.app.Activity;
import android.app.FragmentTransaction;
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
import android.media.AudioManager;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class AudioRecordPage {
	
	private String TAG = "AudioRecordPage";
	
	// record scenario
	private TabSwitcher recordTabSwitcher;
	
	private SeekBar recordMainMicBar;
	private SeekBar recordHeadsetMicBar;
	private SeekBar recordDigitMicBar;
	private Button recordListenButton;
	private Button recordForeverButton;
	
	//private IAudioTunningService audiotunningManager;
	private AudioTunningActivity activity;
	private AudioManager audioManager;
	
	private MediaRecorder mediaRecorder;
	private MediaPlayer mediaPlayer;
	
	private String recordPath;
	private int recordCount = 0;
	
	private final static int POSITION_RECORD_COMMON = 0;
	private final static int POSITION_RECORD_CALL = 1;
	public static int currentPosition = POSITION_RECORD_COMMON;
	
	private boolean recording = false;
	private boolean listening = false; // playing the recorded file
	
	private Set<String> nameSet = new HashSet<String>();
	private Map<String,SeekBar> nameAddrMap = new HashMap<String,SeekBar>();
	
	public AudioRecordPage(AudioTunningActivity activity,  AudioManager audioManager) {
		this.activity = activity;
		//this.audiotunningManager = audiotunningManager;
		this.audioManager = audioManager;
	}
	
	public boolean isTesting() {
		return recording || listening;
	}
	
	public void stopTest() {
		Log.d(TAG, "stopTest,recording="+recording+",listening="+listening);
		if (recording) {
			Log.d(TAG, "stop recording");
			mediaRecorder.stop();
			mediaRecorder.release();
			mediaRecorder = null;	
			recording = false;
		}
		if (listening) {
			Log.d(TAG, "stop listening");
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
			listening = false;
		}
	}
	
	public static void clearSwitchFragmentCurrentPosition() {
		currentPosition = POSITION_RECORD_COMMON;
	}
	
	public void setRecordTabSwitcherFragmentClickListener() {
		recordTabSwitcher = (TabSwitcher) activity.findViewById(R.id.record_tabswitcher);
		recordTabSwitcher.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClickListener(View view, int position) {
				activity.changeLayoutBackground(AudioScenario.MODE_CONTENT_PIC);
				switch (position) {
				case POSITION_RECORD_COMMON:
					ToastManager.showToast(activity.getApplicationContext(), "非电话录音", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_RECORD_COMMON);
					break;
				case POSITION_RECORD_CALL:
					ToastManager.showToast(activity.getApplicationContext(), "电话录音，该场景未定义，敬请关注", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_RECORD_CALL);
					break;
				default:
					break;
				}
				
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
	
	private void resetSeekBarValue() {
		//Log.d(TAG, "resetSeekBarValue");
		int mainMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_MAIN_MIC+",name=volume"));
		nameAddrMap.get(AudioStorage.MAIN_MIC_VOL).setProgress(mainMicVol);
		int hpMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_HEADSET_MIC+",name=volume"));
		nameAddrMap.get(AudioStorage.HEADSET_MIC_VOL).setProgress(hpMicVol);
		int digitMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_MAIN_DIGITAL_MIC+",name=volume"));
		nameAddrMap.get(AudioStorage.DIGIT_MIC_VOL).setProgress(mainMicVol);
	}

	public void setRecordContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "非电话录音", Toast.LENGTH_SHORT);
		Log.d(TAG, "setRecordContentFragmentClickListener");
		
		if (recordPath == null && activity.fileManager.appDirPath != null) {
			recordPath = activity.fileManager.appDirPath + "/record.amr";
		}
		
		recordMainMicBar = (SeekBar) activity.findViewById(R.id.record_main_mic_bar);
		recordHeadsetMicBar = (SeekBar) activity.findViewById(R.id.record_headset_mic_bar);
		recordDigitMicBar = (SeekBar) activity.findViewById(R.id.record_digit_mic_bar);
		recordListenButton = (Button) activity.findViewById(R.id.record_listen_btn);
		recordForeverButton = (Button) activity.findViewById(R.id.record_forever_btn);
		
		nameAddrMap.put(AudioStorage.MAIN_MIC_VOL, recordMainMicBar);
		nameAddrMap.put(AudioStorage.HEADSET_MIC_VOL, recordHeadsetMicBar);
		nameAddrMap.put(AudioStorage.DIGIT_MIC_VOL, recordDigitMicBar);
		
		nameSet = nameAddrMap.keySet();
		Iterator<String> iterator;
		
		// put seekbar name string together
		iterator = nameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_RECORD_COMMON, name);
		}
		
		// bind seekbar name and object addr
		iterator = nameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = nameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_RECORD_COMMON, name, seekBar);
		}
		
		// put ui resources together to manage
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_RECORD, recordMainMicBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_RECORD, recordHeadsetMicBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_RECORD, recordDigitMicBar);
		

		// put init value in the storage
		Log.d(TAG, "recordCount="+recordCount);
		if (recordCount == 0) {
			//resetSeekBarValue();
			iterator = nameSet.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = nameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD_COMMON, name, value);
			}
		}
		recordCount++;
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD, 
//											AudioStorage.MAIN_MIC_VOL, recordMainMicBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD, 
//				AudioStorage.HEADSET_MIC_VOL, recordHeadsetMicBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD, 
//				AudioStorage.DIGIT_MIC_VOL, recordDigitMicBar.getProgress());
		
		recordMainMicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD_COMMON, 
													AudioStorage.MAIN_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_MAIN_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的手机麦克通路音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		recordHeadsetMicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD_COMMON, 
													AudioStorage.HEADSET_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADSET_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的耳机麦克通路音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		recordDigitMicBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"数字麦克音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD_COMMON, 
													AudioStorage.DIGIT_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_MAIN_DIGITAL_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的数字麦克通路音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		recordListenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (recording == false) {
					try {
						mediaRecorder = new MediaRecorder();
						mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
						mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
						mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
						mediaRecorder.setOutputFile(recordPath);
						mediaRecorder.prepare();
						mediaRecorder.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
					recording = true;
					listening = false;
					recordListenButton.setText(R.string.record_listen_btn);
					ToastManager.showToast(activity.getApplicationContext(), "正在录音，请说话", Toast.LENGTH_SHORT);
				}
				else {
					mediaRecorder.stop();
					mediaRecorder.release();
					mediaRecorder = null;
					if (recordPath != null) {
						try {
							mediaPlayer = new MediaPlayer();
							mediaPlayer.setDataSource(recordPath);
							mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
								@Override
								public void onCompletion(MediaPlayer mp) {
									listening = false;
									//recordListenButton.setText(R.string.record_say_btn);
								}
							});
							mediaPlayer.prepare();
							mediaPlayer.start();
							listening = true; // now the recorded file is playing
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					recording = false;
					recordListenButton.setText(R.string.record_say_btn);
					ToastManager.showToast(activity.getApplicationContext(), "正在回放刚才的录音", Toast.LENGTH_SHORT);
				}
			}
		});
		
		recordForeverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		});
	}
	
}
