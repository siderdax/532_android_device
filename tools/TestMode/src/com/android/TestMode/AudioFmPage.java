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
import android.media.AudioManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;


import com.android.TestMode.VerticalSeekBar;
import com.android.TestMode.VerticalSeekBar.OnVerticalSeekBarChangeListener;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;



public class AudioFmPage {
	
	private String TAG = "AudioFmPage";
	
	// Fm scenario
	
	private TabSwitcher fmTabSwitcher;
	
	//private IAudioTunningService audiotunningManager;
	private AudioTunningActivity activity;
	private AudioManager audioManager;
	
	// SCENARIO_FM_LISTEN
	private SeekBar fmSpkBar;
	private SeekBar fmHpBar;
	private VerticalSeekBar fmEqBand1Bar;
	private VerticalSeekBar fmEqBand2Bar;
	private VerticalSeekBar fmEqBand3Bar;
	private VerticalSeekBar fmEqBand4Bar;
	private VerticalSeekBar fmEqBand5Bar;
	private Button fmImportButton;
	private Button fmListenButton;
	private Button fmForeverButton;
	
	// SCENARIO_FM_RECORD
	private SeekBar fmRecordBar;
	private Button fmRecordListenButton;
	private Button fmRecordForeverButton;
	
	private static final int POSITION_FM_LISTEN = 0;
	private static final int POSITION_FM_RECORD = 1;
	
	private int fmCount = 0;
	private int fmRecordCount = 0;
	public static int currentPosition = POSITION_FM_LISTEN;
	
	private Set<String> fmNameSet = new HashSet<String>();
	private Map<String,SeekBar> fmNameAddrMap = new HashMap<String,SeekBar>();
	
	private Set<String> fmRecordNameSet = new HashSet<String>();
	private Map<String,SeekBar> fmRecordNameAddrMap = new HashMap<String,SeekBar>();
	
//	public AudioFmPage(AudioTunningActivity activity, IAudioTunningService audiotunningManager, AudioManager audioManager) {
	public AudioFmPage(AudioTunningActivity activity, AudioManager audioManager) {
	    this.activity = activity;
		//this.audiotunningManager = audiotunningManager;
		this.audioManager = audioManager;
	}
	
	public static void clearSwitchFragmentCurrentPosition() {
		currentPosition = POSITION_FM_LISTEN;
	}
	
	public void setFmTabSwitcherFragmentClickListener() {
		fmTabSwitcher = (TabSwitcher)activity.findViewById(R.id.fm_tabswitcher);
		fmTabSwitcher.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClickListener(View view, int position) {
				activity.changeLayoutBackground(AudioScenario.MODE_CONTENT_PIC);
				switch (position) {
				case POSITION_FM_LISTEN:
					ToastManager.showToast(activity.getApplicationContext(), "FM收听", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_FM_LISTEN);
					break;
				case POSITION_FM_RECORD:
					ToastManager.showToast(activity.getApplicationContext(), "FM录音", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_FM_RECORD);
					break;
				default:
					break;
				}
				
				//Log.d(TAG, "currentPosition="+currentPosition+",position="+position);
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
	
	private void resetListenSeekBarValue() {
		int spkVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",name=volume"));
		fmNameAddrMap.get(AudioStorage.SPK_VOL).setProgress(spkVol);
		int hpVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_HP+",name=volume"));
		fmNameAddrMap.get(AudioStorage.HP_VOL).setProgress(hpVol);
		int eq1 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",name=eq1"));
		fmNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq1);
		int eq2 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",name=eq2"));
		fmNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq2);
		int eq3 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",name=eq3"));
		fmNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq3);
		int eq4 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",name=eq4"));
		fmNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq4);
		int eq5 = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",name=eq5"));
		fmNameAddrMap.get(AudioStorage.EQ_COEFF1).setProgress(eq5);
	}

	public void setFmContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "FM收听", Toast.LENGTH_SHORT);
		// ensure UI is loaded before
		fmSpkBar = (SeekBar) activity.findViewById(R.id.fm_spk_vol_bar);
		fmHpBar = (SeekBar) activity.findViewById(R.id.fm_hp_vol_bar);
		fmEqBand1Bar = (VerticalSeekBar) activity.findViewById(R.id.fm_eq_band1_bar);
		fmEqBand2Bar = (VerticalSeekBar) activity.findViewById(R.id.fm_eq_band2_bar);
		fmEqBand3Bar = (VerticalSeekBar) activity.findViewById(R.id.fm_eq_band3_bar);
		fmEqBand4Bar = (VerticalSeekBar) activity.findViewById(R.id.fm_eq_band4_bar);
		fmEqBand5Bar = (VerticalSeekBar) activity.findViewById(R.id.fm_eq_band5_bar);
		fmImportButton = (Button) activity.findViewById(R.id.fm_import_filter_btn);
		fmListenButton = (Button) activity.findViewById(R.id.fm_listen_btn);
		fmForeverButton = (Button) activity.findViewById(R.id.fm_forever_btn);
		
		fmNameAddrMap.put(AudioStorage.SPK_VOL, fmSpkBar);
		fmNameAddrMap.put(AudioStorage.HP_VOL, fmHpBar);
		fmNameAddrMap.put(AudioStorage.EQ_COEFF1, fmEqBand1Bar);
		fmNameAddrMap.put(AudioStorage.EQ_COEFF2, fmEqBand2Bar);
		fmNameAddrMap.put(AudioStorage.EQ_COEFF3, fmEqBand3Bar);
		fmNameAddrMap.put(AudioStorage.EQ_COEFF4, fmEqBand4Bar);
		fmNameAddrMap.put(AudioStorage.EQ_COEFF5, fmEqBand5Bar);
		
		fmNameSet = fmNameAddrMap.keySet();
		
		Iterator<String> iterator;
		iterator = fmNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_FM_LISTEN, name);
		}
		
		// bind string and addr of ui resources
		iterator = fmNameSet.iterator(); // reset iterator
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = fmNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_FM_LISTEN, name, seekBar);
		}
		
		if (fmCount == 0) {
			//resetListenSeekBarValue();
			iterator = fmNameSet.iterator(); // reset iterator
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = fmNameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, name, value);
			}
		}
		fmCount++;
		
		fmSpkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, 
														AudioStorage.SPK_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_SPK+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的扬声器通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		
		fmHpBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, 
														AudioStorage.HP_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_HP+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的耳机通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		
		fmEqBand1Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, AudioStorage.EQ_COEFF1, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_SPK+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_HP+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将0-70Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
			
		});
		
		fmEqBand2Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, AudioStorage.EQ_COEFF2, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_SPK+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_HP+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将70-270Hz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		fmEqBand3Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, AudioStorage.EQ_COEFF3, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_SPK+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_HP+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将0.27-1KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		fmEqBand4Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, AudioStorage.EQ_COEFF4, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_SPK+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_HP+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将1-3.7KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		fmEqBand5Bar.setOnSeekBarChangeListener(new OnVerticalSeekBarChangeListener() {
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
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, AudioStorage.EQ_COEFF5, 
						bar.getProgress());
				int eq1 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band1 Coefficient");
				int eq2 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band2 Coefficient");
				int eq3 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band3 Coefficient");
				int eq4 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band4 Coefficient");
				int eq5 = AudioStorage.getScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN, "EQ Band5 Coefficient");
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_SPK+
//										   ",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_HP+
//						   					",eq1="+eq1+",eq2="+eq2+",eq3="+eq3+",eq4="+eq4+",eq5="+eq5);
				ToastManager.showToast(activity.getApplicationContext(),
											"暂时将3.7-14.5KHz频段Codec的EQ增益设定为"+bar.getProgress()+"%", 
											Toast.LENGTH_SHORT);
			}
		});
		
		fmImportButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "选择一个后缀名为flt的文件", Toast.LENGTH_SHORT);
				Intent intent = new Intent(activity.getApplicationContext(), SDFileExplorerActivity.class);
				intent.putExtra("total", ""+1);
				intent.putExtra("path1", ""+AudioScenario.PATH_FMRADIO_SPK);
				activity.startActivity(intent);
			}
		});
		
		fmListenButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PackageManager pm = activity.getApplicationContext().getPackageManager();
				Intent intent = new Intent();
				intent = pm.getLaunchIntentForPackage("com.mediatek.FMRadio");
				if (intent != null)
					activity.getApplicationContext().startActivity(intent);
				else {
					ToastManager.showToast(activity.getApplicationContext(),
							"找不到FM应用程序，请先安装SSCR提供的FM应用", 
							Toast.LENGTH_SHORT);
					Log.d(TAG, "FMRadio APP not found!");
				}
			}
			
		});
		
		fmForeverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		}); 

	}
	
	private void resetRecordSeekBarValue() {
		int recVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_FMRADIO_RECORD+",name=volume"));
		fmRecordNameAddrMap.get(AudioStorage.FM_RECORD_VOL).setProgress(recVol);
	}
	
	public void setFmRecordContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "FM录音", Toast.LENGTH_SHORT);
		fmRecordBar = (SeekBar) activity.findViewById(R.id.fm_record_vol_bar);
		fmRecordListenButton = (Button) activity.findViewById(R.id.fm_record_listen_btn);
		fmRecordForeverButton = (Button) activity.findViewById(R.id.fm_record_forever_btn);
		
		fmRecordNameAddrMap.put(AudioStorage.FM_RECORD_VOL, fmRecordBar);
		fmRecordNameSet = fmRecordNameAddrMap.keySet();
		
		Iterator<String> iterator;
		iterator = fmRecordNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_FM_RECORD, name);
		}
		
		// bind string and addr of ui resources
		iterator = fmRecordNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = fmRecordNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_FM_RECORD, name, seekBar);
		}
		
		if (fmRecordCount == 0) {
			//resetRecordSeekBarValue();
			iterator = fmRecordNameSet.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = fmRecordNameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_RECORD, name, value);
			}
		}
		fmRecordCount++;
		
		
		fmRecordBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(),
												"FM录音音量："+progress+"%", 
												Toast.LENGTH_SHORT);				
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_FM_RECORD, 
														AudioStorage.FM_RECORD_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_FMRADIO_RECORD+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(),
						"暂时将Codec的FM录音通路音量设定为"+bar.getProgress()+"%", 
						Toast.LENGTH_SHORT);
			}
		});
		
		fmRecordListenButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PackageManager pm = activity.getApplicationContext().getPackageManager();
//				Intent intent = new Intent(Intent.ACTION_MAIN, null);
//				intent.addCategory(Intent.CATEGORY_LAUNCHER);
//				List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//				Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));
//				for (ResolveInfo everyone : resolveInfos) {
//					String activityName = everyone.activityInfo.name;
//					String pkgName = everyone.activityInfo.packageName;
//					String appLabel = (String) everyone.loadLabel(pm);
//					Log.d(TAG, "activity="+activityName+",pkgName="+pkgName+",label="+appLabel);
//				}
				Intent intent = new Intent();
				intent = pm.getLaunchIntentForPackage("com.mediatek.FMRadio");
				if (intent != null)
					activity.getApplicationContext().startActivity(intent);
				else {
					ToastManager.showToast(activity.getApplicationContext(),
							"找不到FM应用程序，请先安装SSCR提供的FM应用", 
							Toast.LENGTH_SHORT);
					Log.d(TAG, "FMRadio APP not found!");
				}
			}
			
		});
		
		fmRecordForeverButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		}); 
		
	}
	
}
