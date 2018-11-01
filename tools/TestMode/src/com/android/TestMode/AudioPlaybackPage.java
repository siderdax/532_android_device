package com.android.TestMode;

import com.android.TestMode.R;
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
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
//import android.widget.VerticalSeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
//import android.os.IAudioTunningService;
import android.os.ServiceManager;
import android.os.RemoteException;
import com.android.TestMode.TabSwitcher;
import com.android.TestMode.TabSwitcher.OnItemClickListener;
import com.android.TestMode.VerticalSeekBar;
import com.android.TestMode.VerticalSeekBar.OnVerticalSeekBarChangeListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class AudioPlaybackPage {
	
	private String TAG = "AudioPlaybackPage";
	
	private static final int POSITION_PLAYBACK_SINGLE_DEVICE = 0;
	private static final int POSITION_PLAYBACK_DUAL_DEVICE = 1;
	public static int currentPosition = POSITION_PLAYBACK_SINGLE_DEVICE;
	
	// playback single scenario
	private TextView playbackSingleTitle;
	private TextView playbackSingleSpkVolText;
	private TextView playbackSingleHpVolText;
	private TextView playbackSingleEpVolText;
	private SeekBar playbackSingleSpkBar;
	private SeekBar playbackSingleHpBar;
	private SeekBar playbackSingleEpBar;
	private VerticalSeekBar playbackSingleEqBand1Bar;
	private VerticalSeekBar playbackSingleEqBand2Bar;
	private VerticalSeekBar playbackSingleEqBand3Bar;
	private VerticalSeekBar playbackSingleEqBand4Bar;
	private VerticalSeekBar playbackSingleEqBand5Bar;
	private Button playbackSingleImportButton;
	//private Button playbackSingleOneshotButton;
	private Button playbackSingleListenButton;
	private Button playbackSingleListenEpButton;
	private Button playbackSingleForeverButton;
	
	// playback dual scenario
	private TextView playbackDualTitle;
	private TextView playbackDualSpkVolText;
	private TextView playbackDualHpVolText;
	private TextView playbackDualEpVolText;
	private SeekBar playbackDualSpkBar;
	private SeekBar playbackDualHpBar;
	private SeekBar playbackDualEpBar;
	private VerticalSeekBar playbackDualEqBand1Bar;
	private VerticalSeekBar playbackDualEqBand2Bar;
	private VerticalSeekBar playbackDualEqBand3Bar;
	private VerticalSeekBar playbackDualEqBand4Bar;
	private VerticalSeekBar playbackDualEqBand5Bar;
	private Button playbackDualImportButton;
	//private Button playbackDualOneshotButton;
	private Button playbackDualListenButton;
	private Button playbackDualForeverButton;
	
	//public Vector<SeekBar> progressVector = new Vector<SeekBar>();
	
	private TabSwitcher playbackTabSwitcher;
	
	//private IAudioTunningService audiotunningManager;
	private AudioTunningActivity activity;
	private AudioManager audioManager;
	
	private int singleCount = 0;
	private int dualCount = 0;
	
	private MediaPlayer mediaPlayer;
	private boolean singlePlaying = false;
	private boolean epPlaying = false;
	private boolean dualPlaying = false;
	
	// for lazy code
	private Set<String> singleNameSet = new HashSet<String>();
	private Map<String,SeekBar> singleNameAddrMap = new HashMap<String,SeekBar>();
	
	private Set<String> dualNameSet = new HashSet<String>();
	private Map<String,SeekBar> dualNameAddrMap = new HashMap<String,SeekBar>();
	
	//public AudioPlaybackPage(AudioTunningActivity activity, IAudioTunningService audiotunningManager) {
	//public AudioPlaybackPage(AudioTunningActivity activity, IAudioTunningService audiotunningManager, AudioManager audioManager) {
	public AudioPlaybackPage(AudioTunningActivity activity, AudioManager audioManager) {	
		this.activity = activity;
		//this.audiotunningManager = audiotunningManager;
		this.audioManager = audioManager;
	}
	
	public boolean isTesting() {
		return singlePlaying || epPlaying || dualPlaying;
	}
	
	public void stopTest() {
		singlePlaying = false;
		epPlaying = false;
		dualPlaying = false;
		mediaPlayer.stop();
		mediaPlayer.release();
		mediaPlayer = null;
	}
	
	public static void clearSwitchFragmentCurrentPosition() {
		currentPosition = POSITION_PLAYBACK_SINGLE_DEVICE;
	}
	
	public void setPlaybackTabSwitcherFragmentClickListener() {
		
//		playbackSingleEpVolText = (TextView) activity.findViewById(R.id.playback_single_ep_vol); // for changing color pressing TabSwitcher
//		playbackSingleEpBar = (SeekBar) activity.findViewById(R.id.playback_single_ep_vol_bar);
		
		playbackTabSwitcher = (TabSwitcher)activity.findViewById(R.id.playback_tabswitcher);
		playbackTabSwitcher.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClickListener(View view, int position) {
				activity.changeLayoutBackground(AudioScenario.MODE_CONTENT_PIC);
				switch (position) {
				case POSITION_PLAYBACK_SINGLE_DEVICE:
					// media-speaker
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_PLAYBACK_SINGLE);
					// earpiece is useful
					//playbackSingleEpVolText.setTextColor(activity.getResources().getColor(R.color.black));
					//playbackSingleEpBar.setEnabled(true);
					ToastManager.showToast(activity.getApplicationContext(), "扬声器或耳机只有一个使用的场景", Toast.LENGTH_SHORT);
					//AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE);
					break;
				case POSITION_PLAYBACK_DUAL_DEVICE:
					// speaker-and-headphones
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_PLAYBACK_DUAL);
					// earpiece is not useful
					//playbackSingleEpVolText.setTextColor(activity.getResources().getColor(R.color.gray));
					//playbackSingleEpBar.setEnabled(false);
					ToastManager.showToast(activity.getApplicationContext(), "扬声器或耳机同时输出的场景：如铃声通知", Toast.LENGTH_SHORT);
					//AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL);
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
				// we must set seekbar saved value after fragment is loaded finally
				currentPosition = position;
			}
		});
	}
	
	private void resetSingleSeekBarValue() {
		int spkVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",name=volume"));
		singleNameAddrMap.get(AudioStorage.SPK_VOL).setProgress(spkVol);
		int hpVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+",name=volume"));
		singleNameAddrMap.get(AudioStorage.HP_VOL).setProgress(hpVol);
		int epVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_EARPIECE+",name=volume"));
		singleNameAddrMap.get(AudioStorage.EP_VOL).setProgress(epVol);
		int eq1 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",name=eq1"));
		singleNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq1);
		int eq2 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",name=eq2"));
		singleNameAddrMap.get(AudioStorage.EQ_COEFF2).setProgress(eq2);
		int eq3 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",name=eq3"));
		singleNameAddrMap.get(AudioStorage.EQ_COEFF3).setProgress(eq3);
		int eq4 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",name=eq4"));
		singleNameAddrMap.get(AudioStorage.EQ_COEFF4).setProgress(eq4);
		int eq5 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",name=eq5"));
		singleNameAddrMap.get(AudioStorage.EQ_COEFF5).setProgress(eq5);
		
	}
	

	// For SCENARIO_PLAYBACK_SINGLE
	public void setPlaybackSingleContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "扬声器或耳机只有一个使用的场景", Toast.LENGTH_SHORT);
		//playbackSingleTitle = (TextView) activity.findViewById(R.id.playback_single_scene_title); // fragment already loaded
		//playbackSingleTitle.setText(R.string.playback_single_scene_title);
		playbackSingleEpVolText = (TextView) activity.findViewById(R.id.playback_single_ep_vol); // for changing color pressing TabSwitcher
		// Volume Bar
		playbackSingleSpkBar = (SeekBar) activity.findViewById(R.id.playback_single_spk_vol_bar);
		playbackSingleHpBar = (SeekBar) activity.findViewById(R.id.playback_single_hp_vol_bar);
		playbackSingleEpBar = (SeekBar) activity.findViewById(R.id.playback_single_ep_vol_bar);
		// EQ Bar
		playbackSingleEqBand1Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_single_eq_band1_bar);
		playbackSingleEqBand2Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_single_eq_band2_bar);
		playbackSingleEqBand3Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_single_eq_band3_bar);
		playbackSingleEqBand4Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_single_eq_band4_bar);
		playbackSingleEqBand5Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_single_eq_band5_bar);
		// Round button
		playbackSingleImportButton = (Button) activity.findViewById(R.id.playback_single_import_filter_btn);
		//playbackSingleOneshotButton = (Button) activity.findViewById(R.id.playback_single_oneshot_btn);
		playbackSingleListenButton = (Button) activity.findViewById(R.id.playback_single_listen_btn);
		playbackSingleListenEpButton = (Button) activity.findViewById(R.id.playback_single_listen_ep_btn);
		playbackSingleForeverButton = (Button) activity.findViewById(R.id.playback_single_forever_btn);
		
		// put ui resources addr together to manage
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleSpkBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleHpBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleEpBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleEqBand1Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleEqBand2Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleEqBand3Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleEqBand4Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleEqBand5Bar);
		
		singleNameAddrMap.put(AudioStorage.SPK_VOL, playbackSingleSpkBar);
		singleNameAddrMap.put(AudioStorage.HP_VOL, playbackSingleHpBar);
		singleNameAddrMap.put(AudioStorage.EP_VOL, playbackSingleEpBar);
		singleNameAddrMap.put(AudioStorage.EQ_COEFF1, playbackSingleEqBand1Bar);
		singleNameAddrMap.put(AudioStorage.EQ_COEFF2, playbackSingleEqBand2Bar);
		singleNameAddrMap.put(AudioStorage.EQ_COEFF3, playbackSingleEqBand3Bar);
		singleNameAddrMap.put(AudioStorage.EQ_COEFF4, playbackSingleEqBand4Bar);
		singleNameAddrMap.put(AudioStorage.EQ_COEFF5, playbackSingleEqBand5Bar);
		
		singleNameSet = singleNameAddrMap.keySet();
		Iterator<String> iterator;
		
		// put ui resources string together
		iterator = singleNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, name);
		}
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.SPK_VOL);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.HP_VOL);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EP_VOL);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF1);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF2);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF3);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF4);
//		AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF5);
		
		// bind ui resources string and addr
		iterator = singleNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = singleNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, name, seekBar);
		}
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.SPK_VOL,
//				playbackSingleSpkBar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.HP_VOL,
//				playbackSingleHpBar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EP_VOL,
//				playbackSingleEpBar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF1,
//				playbackSingleEqBand1Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF2,
//				playbackSingleEqBand2Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF3,
//				playbackSingleEqBand3Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF4,
//				playbackSingleEqBand4Bar);
//		AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF5,
//				playbackSingleEqBand5Bar);
		

		
		// This code will leads value of seekbar can't be recovered
		// storage init value
		if (singleCount == 0) {
			//resetSingleSeekBarValue();
			iterator = singleNameSet.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = singleNameAddrMap.get(name);
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, name, seekBar.getProgress());
			}
		}
		singleCount++;
		
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.SPK_VOL, playbackSingleSpkBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.HP_VOL, playbackSingleHpBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EP_VOL, playbackSingleEpBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF1, 
//												playbackSingleEqBand1Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF2, 
//												playbackSingleEqBand2Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF3, 
//												playbackSingleEqBand3Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF4, 
//												playbackSingleEqBand4Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF5, 
//												playbackSingleEqBand5Bar.getProgress());
		
		//Log.d(TAG, "Wakaka"+AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.SPK_VOL));
		//Log.d(TAG, "Wakaka"+AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.HP_VOL));
		//Log.d(TAG, "Wakaka"+AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EP_VOL));
		//playbackTabSwitcher = (TabSwitcher)activity.findViewById(R.id.playback_tabswitcher);				

		playbackSingleSpkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, 
														AudioStorage.SPK_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的扬声器通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		playbackSingleHpBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, 
														AudioStorage.HP_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的耳机通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		playbackSingleEpBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, 
														AudioStorage.EP_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_EARPIECE+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的听筒通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});	
		
		playbackSingleEqBand1Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF1, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+
					//	   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将0-70Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});

		playbackSingleEqBand2Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				//AudioStorage.eqCoeff2 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF2, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+
					//	   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将70-270Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});	

		playbackSingleEqBand3Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				//AudioStorage.eqCoeff3 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF3, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+
					//	   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将0.27-1KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});	

		playbackSingleEqBand4Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				//AudioStorage.eqCoeff4 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF4, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+
					//	   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将1-3.7KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});
		playbackSingleEqBand5Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				//AudioStorage.eqCoeff5 = bar.getProgress();
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, AudioStorage.EQ_COEFF5, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+
					//	   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将3.7-14.5KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});
		
		playbackSingleImportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//ToastManager.showToast(activity.getApplicationContext(), "閫夋嫨鍖呭惈婊ゆ尝鍣ㄥ弬鏁扮殑鐩爣鏂囦欢", Toast.LENGTH_SHORT);
				/*try {
				audiotunningManager.importFilterFile(AudioScenario.SCENARIO_PLAYBACK, "HAHAHA");
				} catch (RemoteException e) {
				  Log.e(TAG, "Error in AudioTunningService");
				}*/
				ToastManager.showToast(activity.getApplicationContext(), "选择一个后缀名为flt的文件", Toast.LENGTH_SHORT);
				Intent intent = new Intent(activity.getApplicationContext(), SDFileExplorerActivity.class);
				intent.putExtra("total", ""+1); // only one path
				intent.putExtra("path1", ""+AudioScenario.PATH_MEDIA_SPEAKER);
				activity.startActivity(intent);
			}
		});
		playbackSingleListenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "playback single listen button pressed");
				if (!epPlaying) {
	//					audiotunningManager.setVolume(AudioScenario.PATH_MEDIA_SPEAKER, spkVolume);
	//					audiotunningManager.setVolume(AudioScenario.PATH_MEDIA_HEADPHONES, hpVolume);
	//					audiotunningManager.setVolume(AudioScenario.PATH_MEDIA_EARPIECE, epVolume);
	//					audiotunningManager.setEqCoeff(AudioScenario.PATH_MEDIA_SPEAKER, eqCoeff1, 
	//														eqCoeff2, eqCoeff3,
	//														eqCoeff4, eqCoeff5);
					//audioManager.setParameters("path=1,volume=100"); 	// Look out, no space in the string
					//audioManager.setParameters("path=1,eq1=100,eq2=100,eq3=100,eq4=100,eq5=100");
					//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_SPEAKER+",volume="+spkVolume);
					//try{ Thread.sleep(100); } catch (Exception e) {};
					//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_HEADPHONES+",volume="+hpVolume);
	//				//try{ Thread.sleep(100); } catch (Exception e) {};
					//audioManager.setParameters("path="+AudioScenario.PATH_MEDIA_EARPIECE+",volume="+epVolume);
					if (singlePlaying == false) {
						mediaPlayer = new MediaPlayer();
						mediaPlayer = MediaPlayer.create(activity, R.raw.new_divide);
						mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								singlePlaying = false;
								mediaPlayer.release();
								playbackSingleListenButton.setText(R.string.playback_listen_btn);
							}
						});
						mediaPlayer.start();
						singlePlaying = true;
						playbackSingleListenButton.setText(R.string.playback_listen_btn_2);
						ToastManager.showToast(activity.getApplicationContext(), "从SPK/HP播放", Toast.LENGTH_SHORT);
					}
					else {
						mediaPlayer.stop();
						mediaPlayer.release();
						mediaPlayer = null;
						singlePlaying = false;
						playbackSingleListenButton.setText(R.string.playback_listen_btn);
					}
				}
				else {
					ToastManager.showToast(activity.getApplicationContext(), "请先结束听筒播放再测试扬声器或耳机播放", Toast.LENGTH_SHORT);
				}
			}
		});
		playbackSingleListenEpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "playback single listen ep button pressed");
				if (epPlaying == false) {
					if (!singlePlaying) {
						playbackSingleListenEpButton.setText(R.string.playback_listen_btn_2);
						audioManager.setSpeakerphoneOn(false);
						activity.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
						audioManager.setMode(AudioManager.MODE_IN_CALL);
						mediaPlayer = new MediaPlayer();
						mediaPlayer = MediaPlayer.create(activity, R.raw.liberation);
						mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								epPlaying = false;
								playbackSingleListenEpButton.setText(R.string.playback_listen_ep_btn);
								audioManager.setMode(AudioManager.MODE_NORMAL);
							}
						});
						//mediaPlayer.prepare();
						mediaPlayer.start();
						epPlaying = true;
						ToastManager.showToast(activity.getApplicationContext(), "从听筒播放", Toast.LENGTH_SHORT);
					}
					else {
						ToastManager.showToast(activity.getApplicationContext(), "请先结束扬声器或耳机播放再测试听筒播放", Toast.LENGTH_SHORT);
						playbackSingleListenEpButton.setText(R.string.playback_listen_ep_btn);
					}
				}
				else {
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer = null;
					audioManager.setMode(AudioManager.MODE_NORMAL);
					epPlaying = false;
					playbackSingleListenEpButton.setText(R.string.playback_listen_ep_btn);
				}
			}
		});		
		playbackSingleForeverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
				audioManager.setParameters("path=1999,tunning=end");
//				try {
//					ToastManager.showToast(activity.getApplicationContext(), "鎮ㄧ殑淇敼姘镐箙鐢熸晥", Toast.LENGTH_SHORT);
//					audiotunningManager.overwriteXML();
//				} catch (RemoteException e) {
//					  Log.e(TAG, "Error in AudioTunningService");
//				}				
			}
		});
		
	}
	
	private void resetDualSeekBarValue() {
		int spkVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=spk_volume"));
		dualNameAddrMap.get(AudioStorage.SPK_VOL).setProgress(spkVol);
		int hpVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=hp_volume"));
		dualNameAddrMap.get(AudioStorage.HP_VOL).setProgress(hpVol);
		int eq1 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=eq1"));
		dualNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq1);
		int eq2 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=eq2"));
		dualNameAddrMap.get(AudioStorage.EQ_COEFF2).setProgress(eq2);
		int eq3 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=eq3"));
		dualNameAddrMap.get(AudioStorage.EQ_COEFF3).setProgress(eq3);
		int eq4 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=eq4"));
		dualNameAddrMap.get(AudioStorage.EQ_COEFF4).setProgress(eq4);
		int eq5 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",name=eq5"));
		dualNameAddrMap.get(AudioStorage.EQ_COEFF5).setProgress(eq5);
	}
	
	// For SCENARIO_PLAYBACK_DUAL
	public void setPlaybackDualContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "扬声器或耳机同时输出的场景：如铃声通知", Toast.LENGTH_SHORT);
		//playbackDualTitle = (TextView) activity.findViewById(R.id.playback_dual_scene_title); // fragment already loaded
		//playbackDualTitle.setText(R.string.playback_dual_scene_title);
		// Volume Bar
		playbackDualSpkBar = (SeekBar) activity.findViewById(R.id.playback_dual_spk_vol_bar);
		playbackDualHpBar = (SeekBar) activity.findViewById(R.id.playback_dual_hp_vol_bar);
		// EQ Bar
		playbackDualEqBand1Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_dual_eq_band1_bar);
		playbackDualEqBand2Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_dual_eq_band2_bar);
		playbackDualEqBand3Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_dual_eq_band3_bar);
		playbackDualEqBand4Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_dual_eq_band4_bar);
		playbackDualEqBand5Bar = (VerticalSeekBar) activity.findViewById(R.id.playback_dual_eq_band5_bar);
		// Round button
		playbackDualImportButton = (Button) activity.findViewById(R.id.playback_dual_import_filter_btn);
		playbackDualListenButton = (Button) activity.findViewById(R.id.playback_dual_listen_btn);
		playbackDualForeverButton = (Button) activity.findViewById(R.id.playback_dual_forever_btn);
		
		//playbackTabSwitcher = (TabSwitcher)activity.findViewById(R.id.playback_tabswitcher);
		
		// NOT USEFUL, put ui resources together to manage
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualSpkBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualHpBar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualEqBand1Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualEqBand2Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualEqBand3Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualEqBand4Bar);
//		AudioStorage.putScenarioSeekBarResources(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualEqBand5Bar);
		
		dualNameAddrMap.put(AudioStorage.SPK_VOL, playbackDualSpkBar);
		dualNameAddrMap.put(AudioStorage.HP_VOL, playbackDualHpBar);
		dualNameAddrMap.put(AudioStorage.EQ_COEFF1, playbackDualEqBand1Bar);
		dualNameAddrMap.put(AudioStorage.EQ_COEFF2, playbackDualEqBand2Bar);
		dualNameAddrMap.put(AudioStorage.EQ_COEFF3, playbackDualEqBand3Bar);
		dualNameAddrMap.put(AudioStorage.EQ_COEFF4, playbackDualEqBand4Bar);
		dualNameAddrMap.put(AudioStorage.EQ_COEFF5, playbackDualEqBand5Bar);
		
		dualNameSet = dualNameAddrMap.keySet();
		
		Iterator<String> iterator;
		
		// put seekbar name string together
		iterator = dualNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_PLAYBACK_DUAL, name);
		}
		
		// bind seekbar name and object address
		iterator = dualNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = dualNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_PLAYBACK_DUAL, name, seekBar);
		}
		

		// This will leads value of seekbar can't be recovered
		// storage init value
		if (dualCount == 0) {
			//resetDualSeekBarValue();
			iterator = dualNameSet.iterator(); // reset iterator
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = dualNameAddrMap.get(name);
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, name, seekBar.getProgress());
			}
		}
		dualCount++;
		
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.SPK_VOL, playbackDualSpkBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.HP_VOL, playbackDualHpBar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF1, 
//												playbackDualEqBand1Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF2, 
//												playbackDualEqBand2Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF3, 
//												playbackDualEqBand3Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF4, 
//												playbackDualEqBand4Bar.getProgress());
//		AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF5, 
//												playbackDualEqBand5Bar.getProgress());

		playbackDualSpkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, 
								AudioStorage.SPK_VOL, bar.getProgress());
				int spkVolume = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.SPK_VOL);
				int hpVolume = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.HP_VOL);
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",volume="+spkVolume+",volume2="+hpVolume);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的扬声器通路音量设定为"+bar.getProgress()+"%", 
									 Toast.LENGTH_SHORT);				
			}
		});
		playbackDualHpBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, 
								AudioStorage.HP_VOL, bar.getProgress());
				int spkVolume = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.SPK_VOL);
				int hpVolume = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.HP_VOL);
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+",volume="+spkVolume+",volume2="+hpVolume);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的耳机通路音量设定为"+bar.getProgress()+"%", 
								Toast.LENGTH_SHORT);
			}
		});
		
		playbackDualEqBand1Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF1, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band5 Coefficient");
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+
										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将0-70Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});
		playbackDualEqBand2Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF2, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band5 Coefficient");
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+
										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将70-270Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});	
		playbackDualEqBand3Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF3, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band5 Coefficient");
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+
										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将0.27-1KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});	
		playbackDualEqBand4Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF4, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band5 Coefficient");
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+
										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将1-3.7KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});
		playbackDualEqBand5Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, AudioStorage.EQ_COEFF5, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL, "EQ Band5 Coefficient");
				audioManager.setParameters("path="+AudioScenario.PATH_SPEAKER_AND_HEADPHONES+
										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将3.7-14.5KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
			
		});
		
		playbackDualImportButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "选择一个后缀名为flt的文件", Toast.LENGTH_SHORT);
//				try {
//				audiotunningManager.importFilterFile(AudioScenario.SCENARIO_PLAYBACK, "HAHAHA");
//				} catch (RemoteException e) {
//				  Log.e(TAG, "Error in AudioTunningService");
//				}
				Intent intent = new Intent(activity.getApplicationContext(), SDFileExplorerActivity.class);
				intent.putExtra("total", ""+1);
				intent.putExtra("path1", ""+AudioScenario.PATH_SPEAKER_AND_HEADPHONES);
				activity.startActivity(intent);
			}
		});
		playbackDualListenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dualPlaying == false) {
					mediaPlayer = new MediaPlayer();
					mediaPlayer = MediaPlayer.create(activity, R.raw.new_divide);
					mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							dualPlaying = false;
							mediaPlayer.release();
							playbackDualListenButton.setText(R.string.playback_listen_btn);
						}
					});
					//mediaPlayer.prepare();
					mediaPlayer.start();
					dualPlaying = true;
					playbackDualListenButton.setText(R.string.playback_listen_btn_2);
				}
				else  {
					mediaPlayer.stop();
					mediaPlayer.release();
					mediaPlayer = null;
					dualPlaying = false;
					playbackDualListenButton.setText(R.string.playback_listen_btn);
				}			
			}
		});
		playbackDualForeverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
				audioManager.setParameters("path=1999,tunning=end");
//				try {
//					ToastManager.showToast(activity.getApplicationContext(), "鎮ㄧ殑淇敼姘镐箙鐢熸晥", Toast.LENGTH_SHORT);
//					audiotunningManager.overwriteXML();
//				} catch (RemoteException e) {
//					  Log.e(TAG, "Error in AudioTunningService");
//				}				
			}
		});
		
	}
	
}
