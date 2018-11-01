package com.android.TestMode;

import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import android.widget.SeekBar;
import android.util.Log;

public class AudioStorage {
	public static int spkVolume;
	public static int hpVolume;
	public static int epVolume;
	public static int eqCoeff1;
	public static int eqCoeff2;
	public static int eqCoeff3;
	public static int eqCoeff4;
	public static int eqCoeff5;
	public static String fileContent;
	
	private static String TAG = "AudioStorage";
	
	// SeekBar name
	public static final String SPK_VOL = "SPK Volume";
	public static final String HP_VOL= "HP Volume";  //in bt scenario, use SPK_VOL to indicate bt headset play volume
	public static final String EP_VOL = "EP Volume";
	public static final String EQ_COEFF1 = "EQ Band1 Coefficient";
	public static final String EQ_COEFF2 = "EQ Band2 Coefficient";
	public static final String EQ_COEFF3 = "EQ Band3 Coefficient";
	public static final String EQ_COEFF4 = "EQ Band4 Coefficient";
	public static final String EQ_COEFF5 = "EQ Band5 Coefficient";
	public static final String MAIN_MIC_VOL = "Main Mic Volume"; // may indicate record volume and voice call volume int each scenario 
	public static final String HEADSET_MIC_VOL = "Headset Mic Volume"; //in bt scenario, use HP_VOL to indicate bt headset mic volume
	public static final String DIGIT_MIC_VOL = "Digit Mic Volume";
	public static final String FM_RECORD_VOL = "FM Record Volume";
	
	// key: scenario      value: Map<seekbar name, seekbar value>
	private static Map<Integer,Map<String,Integer>> allScenarioSeekBarNameValueMap = new HashMap<Integer,Map<String,Integer>>();
	private static Map<String,Integer> playbackSingleSeekBarValuesMap = new HashMap<String,Integer>(); // playback has second scenario
	private static Map<String,Integer> playbackDualSeekBarValuesMap = new HashMap<String,Integer>(); 	
	private static Map<String,Integer> recordCommonSeekBarValuesMap = new HashMap<String,Integer>();
	private static Map<String,Integer> recordCallSeekBarValuesMap = new HashMap<String,Integer>();
	private static Map<String,Integer> voiceDownSeekBarValuesMap = new HashMap<String,Integer>(); // voice has second scenario
	private static Map<String,Integer> voiceUpSeekBarValuesMap = new HashMap<String,Integer>();	
	private static Map<String,Integer> btPhoneSeekBarValuesMap = new HashMap<String,Integer>(); // bt has second scenario
	private static Map<String,Integer> btVoipSeekBarValuesMap = new HashMap<String,Integer>();	
	private static Map<String,Integer> fmSeekBarValuesMap = new HashMap<String,Integer>();
	private static Map<String,Integer> fmRecordSeekBarValuesMap = new HashMap<String,Integer>();
	
	// NOT USEFUL
//	private static Map<Integer,Vector<SeekBar>> allSeekBarResourcesMap = new HashMap<Integer,Vector<SeekBar>>();
//	private static Vector<SeekBar> playbackSingleSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> playbackDualSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> recordSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> voiceDownSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> voiceUpSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> btPhoneSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> btVoipSeekBarResourcesVector = new Vector<SeekBar>();
//	private static Vector<SeekBar> fmSeekBarResourcesVector = new Vector<SeekBar>();
	
	// key: scenario	value: Map<seekbar name, seekbar object address>
	// Bind the name with SeekBar address, so we can know which SeekBar uses which name
	private static Map<Integer,Map<String,SeekBar>> allScenarioSeekBarNameAddrMap = new HashMap<Integer,Map<String,SeekBar>>();
	private static Map<String,SeekBar> playbackSingleSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> playbackDualSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> recordCommonSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> recordCallSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> voiceDownSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> voiceUpSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> btPhoneSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> btVoipSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> fmSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	private static Map<String,SeekBar> fmRecordSeekBarNameAddrMap = new HashMap<String,SeekBar>();
	
	// key scenario      value: Set<seekbar name>
	// ui resources string name in specific scenario
	private static Map<Integer,Set<String>> allScenarioSeekBarNameMap = new HashMap<Integer,Set<String>>();
	private static Set<String> playbackSingleSeekBarNameSet = new HashSet<String>();
	private static Set<String> playbackDualSeekBarNameSet = new HashSet<String>();
	private static Set<String> recordCommonSeekBarNameSet = new HashSet<String>();
	private static Set<String> recordCallSeekBarNameSet = new HashSet<String>();
	private static Set<String> voiceDownSeekBarNameSet = new HashSet<String>();
	private static Set<String> voiceUpSeekBarNameSet = new HashSet<String>();
	private static Set<String> btPhoneSeekBarNameSet = new HashSet<String>();
	private static Set<String> btVoipSeekBarNameSet = new HashSet<String>();
	private static Set<String> fmSeekBarNameSet = new HashSet<String>();
	private static Set<String> fmRecordSeekBarNameSet = new HashSet<String>();
	
	static {
		// otherwise null pointer error occurs
		
		// put Map<String,Integer> into Map<Integer,Map<String,Integer>>
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_RECORD_COMMON, recordCommonSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_RECORD_CALL, recordCallSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, voiceDownSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, voiceUpSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, btPhoneSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, btVoipSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_FM_LISTEN, fmSeekBarValuesMap);
		allScenarioSeekBarNameValueMap.put(AudioScenario.SCENARIO_FM_RECORD, fmRecordSeekBarValuesMap);
		
		// NOT USEFUL
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleSeekBarResourcesVector);
//		// FUCK! should use SCENARIO_PLAYBACK_DUAL, this error takes me 1h to fix it
//		//allSeekBarResourcesMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackSingleSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_RECORD, recordSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, voiceDownSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, voiceUpSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, btPhoneSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, btVoipSeekBarResourcesVector);
//		allSeekBarResourcesMap.put(AudioScenario.SCENARIO_FM, fmSeekBarResourcesVector);
		
		// put Map<String,SeekBar> into Map<Integer,Map<String,SeekBar>>
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_RECORD_COMMON, recordCommonSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_RECORD_CALL, recordCallSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, voiceDownSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, voiceUpSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, btPhoneSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, btVoipSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_FM_LISTEN, fmSeekBarNameAddrMap);
		allScenarioSeekBarNameAddrMap.put(AudioScenario.SCENARIO_FM_RECORD, fmRecordSeekBarNameAddrMap);
		
		// put Set<String> into Map<Integer,Set<String>>
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_RECORD_COMMON, recordCommonSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_RECORD_CALL, recordCallSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, voiceDownSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, voiceUpSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, btPhoneSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, btVoipSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_FM_LISTEN, fmSeekBarNameSet);
		allScenarioSeekBarNameMap.put(AudioScenario.SCENARIO_FM_RECORD, fmRecordSeekBarNameSet);
	}
	
	public static void saveScenarioSeekBarValue(int scenario, String seekBarName, int percent) {
		Log.d(TAG, "saveScenarioSeekBarValue, scenario="+scenario+",seekBarName="+seekBarName+",percent="+percent);
		Map<String,Integer> map = allScenarioSeekBarNameValueMap.get(scenario);
		map.put(seekBarName, percent);
		Log.d(TAG, "getScenarioSeekBarValue returns "+getScenarioSeekBarValue(scenario,seekBarName));
	}
	
	public static int getScenarioSeekBarValue(int scenario, String seekBarName) {
		Map<String,Integer> map = allScenarioSeekBarNameValueMap.get(scenario);
		return map.get(seekBarName);
	}
	
	// NOT USEFUL
//	public static void putScenarioSeekBarResources(int scenario, SeekBar seekBar) {
//		Vector<SeekBar> vector = allSeekBarResourcesMap.get(scenario);
//		vector.add(seekBar);
//	}
	
	public static void putScenarioSeekBarName(int scenario, String seekBarName) {
		Set<String> set = allScenarioSeekBarNameMap.get(scenario);
		set.add(seekBarName);
	}
	
	public static void bindScenarioSeekBarNameAddr(int scenario, String seekBarName, SeekBar seekBar) {
		Map<String,SeekBar> map = allScenarioSeekBarNameAddrMap.get(scenario);
		map.put(seekBarName, seekBar);
	}
	
	// when go back to one page, the value saved from the page setting then recovers on UI
	public static void recoverScenarioSeekBarValue(int scenario) {
		
		AudioScenario.barChangeInfo = false;
		
		Log.d(TAG, "recoverScenarioSeekBarValue, scenario="+scenario);
		Set<String> seekBarNameSet = allScenarioSeekBarNameMap.get(scenario);
		Log.d(TAG, "seekBarNameSet size="+seekBarNameSet.size());
		
		Map<String,SeekBar> seekBarNameAddrMap = allScenarioSeekBarNameAddrMap.get(scenario);
		Log.d(TAG, "seekBarNameAddrMap size="+seekBarNameAddrMap.size());
		
		Map<String,Integer> seekBarNameValueMap = allScenarioSeekBarNameValueMap.get(scenario);
		Log.d(TAG, "seekBarNameValueMap size="+seekBarNameValueMap.size());
		if ((seekBarNameSet.size() > 0) && (seekBarNameAddrMap.size() > 0) && (seekBarNameValueMap.size() > 0)) {
//			for (int i=0; i<seekBarNameSet.size(); i++) {
//				String name = seekBarNameSet.get(i);
//				int value = seekBarNameValueMap.get(name);
//				SeekBar seekBar = seekBarNameAddrMap.get(name);
//				seekBar.setProgress(value);
//				Log.d(TAG, "SeekBar name="+name+", value="+seekBar.getProgress());
//			}
			Iterator<String> iterator = seekBarNameSet.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				Log.d(TAG, "SeekBar name="+name);
				SeekBar seekBar = seekBarNameAddrMap.get(name);
				int value = seekBarNameValueMap.get(name);
				seekBar.setProgress(value);
				Log.d(TAG, "SeekBar value="+seekBar.getProgress());
			}
		}
		
		AudioScenario.barChangeInfo = true;
	}
	
	// used in write xml
	public static Map<String,Integer> getScenarioSeekBarNameValueMap(int scenario) {
		return allScenarioSeekBarNameValueMap.get(scenario);
	}
}
