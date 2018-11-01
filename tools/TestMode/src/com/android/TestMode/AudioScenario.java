package com.android.TestMode;

public class AudioScenario {

	public static boolean barChangeInfo = true;
	
	// Primary scenario: The scenario is only for different TAB
	public static final int SCENARIO_NONE = 0; // for welcome UI
	public static final int SCENARIO_PLAYBACK = 1; // primary scenario
	public static final int SCENARIO_PLAYBACK_SINGLE = 2; 	// Second scenario
	public static final int SCENARIO_PLAYBACK_DUAL = 3;		// Second scenario
	public static final int SCENARIO_RECORD = 4;	// primary scenario
	public static final int SCENARIO_RECORD_COMMON = 5; //primary scenario
	public static final int SCENARIO_RECORD_CALL = 6; //second scenario
	public static final int SCENARIO_VOICE_CALL = 7;	// primary scenario
	public static final int SCENARIO_VOICE_CALL_DOWNLINK = 8; // Second Scenario
	public static final int SCENARIO_VOICE_CALL_UPLINK = 9;		// Second scenario
	public static final int SCENARIO_BLUETOOTH_CALL = 10;	// primary scenario
	public static final int SCENARIO_BLUETOOTH_CALL_PHONE = 11; // Second scenario
	public static final int SCENARIO_BLUETOOTH_CALL_VOIP = 12;	// Second scenario
	public static final int SCENARIO_FM = 13;	// primary scenario
	public static final int SCENARIO_FM_LISTEN = 14;
	public static final int SCENARIO_FM_RECORD = 15;
	
	public static final String[] scenarioName = {"Scenario None",
											     "Scenario Playback",
											     "Scenario Playback Single Device",
											     "Sceanrio Playback Dual Device",
											     "Scenario Record",
											     "Scenario Record Common",
											     "Scenario Record Call",
											     "Scenario Voice Call",
											     "Scenario Voice Call Downlink",
											     "Scenario Voice Call Uplink",
											     "Scenario Bluetooth Call",
											     "Scenario Bluetooth Call Phone",
											     "Scenario Bluetooth Call VOIP",
											     "Scenario FM",
											     "Scenario FM Listener",
											     "Scenario FM Record",
												};
	public static final int TOTAL_SCENARIO = SCENARIO_FM_RECORD + 1;
	
	// TAB or RadioButton
	public static final int TAB_NONE = 0; // for welcome UI
	public static final int TAB_PLAYBACK = 1;
	public static final int TAB_RECORD = 2;
	public static final int TAB_VOICE_CALL = 3;
	public static final int TAB_BLUETOOTH_CALL = 4;
	public static final int TAB_FM = 5;
	
	// Fragment Number
	// not so good to put fragment number here, it's better to change AudioScenario.java to AudioConstants.java
	public static final int FRAGMENT_TABSWITCHER = 0;
	public static final int FRAGMENT_CONTENT = 1;
	public static final int FRAGMENT_RADIOGROUP = 2;
	// for change background picture
	public static final int MODE_WELCOME_PIC = 0;
	public static final int MODE_CONTENT_PIC = 1;
	
	// make you confuse, may move these to a new class named AudioDevice probably, forgive my lazy
//	public static final int DEVICE_NONE = 0;
//	public static final int DEVICE_SPEAKER = 1;
//	public static final int DEVICE_HEADPHONE = 2;
//	public static final int DEVICE_EARPIECE = 3;
	
	// JNI api is oneshot in default
	//public static final int LIFE_OHESHOT = 0;
	//public static final int LIFE_FOREVER = 1;
	
	// Look out: should be the same to path name of codec XML file
	public static final int PATH_VOICE_CALL_SPEAKER = 0;
	public static final int PATH_VOICE_CALL_HEADPHONES = 1;
	public static final int PATH_VOICE_CALL_HEADSET = 2;
	public static final int PATH_VOICE_CALL_EARPIECE = 3;
	public static final int PATH_VOICE_BT_SCO_HEADSET = 4;
	public static final int PATH_VOICE_BT_SCO_MIC = 5;
	public static final int PATH_VOICE_CALL_MAIN_MIC = 6;
	public static final int PATH_VOICE_CALL_HEADSET_MIC = 7;
	public static final int PATH_MEDIA_SPEAKER = 8;
	public static final int PATH_MEDIA_HEADPHONES = 9;
	public static final int PATH_MEDIA_EARPIECE = 10;
	public static final int PATH_SPEAKER_AND_HEADPHONES = 11;
	public static final int PATH_BT_SCO_HEADSET = 12;
	public static final int PATH_MEDIA_MAIN_DIGITAL_MIC = 13;
	public static final int PATH_MEDIA_MAIN_MIC = 14;
	public static final int PATH_MEDIA_HEADSET_MIC = 15;
	public static final int PATH_BT_SCO_MIC = 16;
	public static final int PATH_FMRADIO_SPK = 17;
	public static final int PATH_FMRADIO_HP = 18;
	public static final int PATH_FMRADIO_RECORD = 19;
	public static final int PATH_DEFAULT = 20;
	
	// button id, to distinguish which scenario
	private static int playbackTabId;
	private static int recordTabId;
	private static int voiceTabId;
	private static int btTabId;
	private static int fmTabId;
	
	private static int primaryScenario = SCENARIO_NONE; // at the moment
	private static int secondScenario; // at the moment
	
	// translate layer to make code common
	public static void setPrimaryScenarioFromTab(int tabId) {
		if (tabId == playbackTabId) {
			primaryScenario = SCENARIO_PLAYBACK;
		}
		else if (tabId == recordTabId) {
			primaryScenario = SCENARIO_RECORD;
		}
		else if (tabId == voiceTabId) {
			primaryScenario = SCENARIO_VOICE_CALL;
		}
		else if (tabId == btTabId) {
			primaryScenario = SCENARIO_BLUETOOTH_CALL;
		}
		else if (tabId == fmTabId) {
			primaryScenario = SCENARIO_FM;
		}
		else
			primaryScenario = SCENARIO_NONE;
	}
	
	public static void setPrimaryScenarioDebug(int currentScenario) {
		primaryScenario = currentScenario;
	}
	
	public static void setSecondScenarioFromTabSwitcher(int scenario) {
		secondScenario = scenario;
	}
	
	public static int queryPrimaryScenario() {
		return primaryScenario;
	}
	
	public static int querySecondScenario() {
		return secondScenario;
	}
	
	// must be called after setTabButtonId
	public static int getButtonId(int tab) {
		switch (tab) {
		case TAB_PLAYBACK:
			return playbackTabId;
		case TAB_RECORD:
			return recordTabId;
		case TAB_VOICE_CALL:
			return voiceTabId;
		case TAB_BLUETOOTH_CALL:
			return btTabId;
		case TAB_FM:
			return fmTabId;
		case TAB_NONE:
			return 0;
		default:
			return -1;
		}
	}
	
	public static void setTabButtonId(int tab, int buttonId) {
		switch (tab) {
		case TAB_PLAYBACK:
			playbackTabId = buttonId;
			break;
		case TAB_RECORD:
			recordTabId = buttonId;
			break;
		case TAB_VOICE_CALL:
			voiceTabId = buttonId;
			break;
		case TAB_BLUETOOTH_CALL:
			btTabId = buttonId;
			break;
		case TAB_FM:
			fmTabId = buttonId;
			break;
		default:
			break;
		}
	}
	
	public static int covertScenarioNameToIndex(String name) {
		for (int i=0; i<TOTAL_SCENARIO; i++) {
			if (scenarioName[i].equals(name))
				return i;
		}
		return 0;
	}
	
}
