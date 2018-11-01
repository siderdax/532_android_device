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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import android.net.Uri;
import android.content.Intent;

public class AudioBluetoothPage {
	
	private String TAG = "AudioBluetoothPage";
	
	// record scenario
	
	private TabSwitcher bluetoothTabSwitcher;
	
	private final static int POSITION_BLUETOOTH_CALL_PHONE = 0;
	private final static int POSITION_BLUETOOTH_CALL_VOIP = 1;
	
	public static int currentPosition = POSITION_BLUETOOTH_CALL_PHONE;
	
	//private IAudioTunningService audiotunningManager;
	private AudioTunningActivity activity;
	private AudioManager audioManager;
	
	// BLUETOOTH PHONE CALL SCENARIO
	private SeekBar btPhoneDownlinkBar;
	private SeekBar btPhoneUplinkBar;
	private Button btPhoneTestButton;
	private Button btPhoneForeverButton;
	
	// BLUETOOTH VOIP CALL SCENARIO
	private SeekBar btVoipDownlinkBar;
	private SeekBar btVoipUplinkBar;
	private Button btVoipTestButton;
	private Button btVoipForeverButton;
	
	private int phoneCount = 0;
	private int voipCount = 0;
	
	// for write lazy code
	private Set<String> phoneNameSet = new HashSet<String>();
	private Map<String,SeekBar> phoneNameAddrMap = new HashMap<String,SeekBar>();
	
	private Set<String> voipNameSet = new HashSet<String>();
	private Map<String,SeekBar> voipNameAddrMap = new HashMap<String,SeekBar>();
	
	//public AudioBluetoothPage(AudioTunningActivity activity, IAudioTunningService audiotunningManager, AudioManager audioManager) {
	public AudioBluetoothPage(AudioTunningActivity activity, AudioManager audioManager) {		
		this.activity = activity;
		//this.audiotunningManager = audiotunningManager;
		this.audioManager = audioManager;
	}
	
	public static void clearSwitchFragmentCurrentPosition() {
		currentPosition = POSITION_BLUETOOTH_CALL_PHONE;
	}
	
	public void setBluetoothTabSwitcherFragmentClickListener() {
		bluetoothTabSwitcher = (TabSwitcher)activity.findViewById(R.id.bt_tabswitcher);
		bluetoothTabSwitcher.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClickListener(View view, int position) {
				
				activity.changeLayoutBackground(AudioScenario.MODE_CONTENT_PIC);
				switch (position) {
				case POSITION_BLUETOOTH_CALL_PHONE:
					ToastManager.showToast(activity.getApplicationContext(), "蓝牙耳机电话", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE);
					break;
				case POSITION_BLUETOOTH_CALL_VOIP:
					ToastManager.showToast(activity.getApplicationContext(), "蓝牙耳机VOIP", Toast.LENGTH_SHORT);
					AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP);
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
	
	
	private void resetPhoneSeekBarValue() {
		int hpVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_BT_SCO_MIC+",name=volume"));
		phoneNameAddrMap.get(AudioStorage.HP_VOL).setProgress(hpVol);
		int hpMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_VOICE_BT_SCO_MIC+",name=volume"));
		phoneNameAddrMap.get(AudioStorage.HEADSET_MIC_VOL).setProgress(hpMicVol);
	}

	public void setBluetoothPhoneContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "蓝牙耳机电话", Toast.LENGTH_SHORT);
		btPhoneDownlinkBar = (SeekBar) activity.findViewById(R.id.bt_phone_downlink_bar);
		btPhoneUplinkBar = (SeekBar) activity.findViewById(R.id.bt_phone_uplink_bar);
		btPhoneTestButton = (Button) activity.findViewById(R.id.bt_phone_test_btn);
		btPhoneForeverButton = (Button) activity.findViewById(R.id.bt_phone_forever_btn);
		
		phoneNameAddrMap.put(AudioStorage.HP_VOL, btPhoneDownlinkBar);
		phoneNameAddrMap.put(AudioStorage.HEADSET_MIC_VOL, btPhoneUplinkBar);
		phoneNameSet = phoneNameAddrMap.keySet();
		
		Iterator<String> iterator;
		
		iterator = phoneNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, name);
		}
		
		iterator = phoneNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = phoneNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, name, seekBar);
		}
		
		if (phoneCount == 0) {
			//resetPhoneSeekBarValue();
			iterator = phoneNameSet.iterator(); // reset iterator
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = phoneNameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, name, value);
			}			
		}
		phoneCount++;
		
		btPhoneDownlinkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"蓝牙耳机电话下行音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, 
													AudioStorage.HP_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_BT_SCO_HEADSET+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的蓝牙电话下行音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		btPhoneUplinkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"蓝牙耳机电话上行音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, 
													AudioStorage.HEADSET_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_VOICE_BT_SCO_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的蓝牙电话上行音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		btPhoneTestButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("tel:112");
				Intent intent = new Intent(Intent.ACTION_DIAL, uri);
				activity.startActivity(intent);
			}
		});
		
		btPhoneForeverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		});
	}

	
	private void resetVoipSeekBarValue() {
		int hpVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_BT_SCO_MIC+",name=volume"));
		voipNameAddrMap.get(AudioStorage.HP_VOL).setProgress(hpVol);
		int hpMicVol = Integer.parseInt(audioManager.getParameters("path="+AudioScenario.PATH_BT_SCO_MIC+",name=volume"));
		voipNameAddrMap.get(AudioStorage.HEADSET_MIC_VOL).setProgress(hpMicVol);
	}

	public void setBluetoothVoipContentFragmentClickListener() {
		ToastManager.showToast(activity.getApplicationContext(), "蓝牙耳机VOIP", Toast.LENGTH_SHORT);
		btVoipDownlinkBar = (SeekBar) activity.findViewById(R.id.bt_voip_downlink_bar);
		btVoipUplinkBar = (SeekBar) activity.findViewById(R.id.bt_voip_uplink_bar);
		btVoipTestButton = (Button) activity.findViewById(R.id.bt_voip_test_btn);
		btVoipForeverButton = (Button) activity.findViewById(R.id.bt_voip_forever_btn);
		
		voipNameAddrMap.put(AudioStorage.HP_VOL, btVoipDownlinkBar);
		voipNameAddrMap.put(AudioStorage.HEADSET_MIC_VOL, btVoipUplinkBar);
		voipNameSet = voipNameAddrMap.keySet();
		
		Iterator<String> iterator;
		
		iterator = voipNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			AudioStorage.putScenarioSeekBarName(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, name);
		}
		
		iterator = voipNameSet.iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			SeekBar seekBar = voipNameAddrMap.get(name);
			AudioStorage.bindScenarioSeekBarNameAddr(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, name, seekBar);
		}
		
		if (voipCount == 0) {
			//resetVoipSeekBarValue();
			iterator = voipNameSet.iterator(); // reset iterator
			while (iterator.hasNext()) {
				String name = iterator.next();
				SeekBar seekBar = voipNameAddrMap.get(name);
				int value = seekBar.getProgress();
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, name, value);
			}			
		}
		voipCount++;		
		
		btVoipDownlinkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"蓝牙耳机VOIP通话下行音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, 
													AudioStorage.HP_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_BT_SCO_HEADSET+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的蓝牙VOIP通路下行音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		btVoipUplinkBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
				ToastManager.showToast(activity.getApplicationContext(), 
						"蓝牙耳机VOIP通话上行音量："+progress+"%", Toast.LENGTH_SHORT);
			}
			@Override
			public void onStartTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStartTrackingTouch");
			}
			@Override
			public void onStopTrackingTouch(SeekBar bar) {
				Log.d(TAG, "onStopTrackingTouch");
				AudioStorage.saveScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, 
													AudioStorage.HEADSET_MIC_VOL, bar.getProgress());
//				audioManager.setParameters("path="+AudioScenario.PATH_BT_SCO_MIC+",volume="+bar.getProgress());
				ToastManager.showToast(activity.getApplicationContext(), 
						"暂时将Codec的蓝牙VOIP通路上行音量设定为"+bar.getProgress()+"%", Toast.LENGTH_SHORT);
			}
		});
		
		btVoipTestButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
			}
		});
		
		btVoipForeverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastManager.showToast(activity.getApplicationContext(), "您的修改永久生效", Toast.LENGTH_SHORT);
//				audioManager.setParameters("path=1999,tunning=end");
			}
		});		
	}
	
}
