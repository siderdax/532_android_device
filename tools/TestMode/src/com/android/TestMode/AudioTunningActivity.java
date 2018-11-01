package com.android.TestMode;

import java.lang.reflect.Method;
import java.util.Random;

import com.android.TestMode.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
//import android.os.IAudioTunningService;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class AudioTunningActivity extends Activity implements AudioOnRadioButtonClickListener {

	private String TAG = "AudioTunningActivity";
	//private IAudioTunningService audiotunningManager;
	private AudioManager audioManager;
	
	private int NUM_WOOD_BACKGROUND = 19; // how many pictures to show randomly
	private int MAX_WELCOME_BACKGROUND = 1; // sort welcome pic and content pic
	private int currentWelcomeBackground = 0;
	
	//private HashSet<Integer> drawIdHashSet = new HashSet<Integer>();

	// Bottom Tab
	private RadioGroup radioGroup;
	private RadioButton playbackTab;
	private RadioButton recordTab;
	private RadioButton voiceTab;
	private RadioButton btTab;
	private RadioButton fmTab;
	
	private SoundPool soundPool;
	private int music; // sound id
	
	// playback scenario
	/*private TextView playbackTitle;
	private SeekBar playbackSpkBar;
	private SeekBar playbackHpBar;
	private SeekBar playbackEpBar;
	private VerticalSeekBar playbackEqBand1Bar;
	private VerticalSeekBar playbackEqBand2Bar;
	private VerticalSeekBar playbackEqBand3Bar;
	private VerticalSeekBar playbackEqBand4Bar;
	private VerticalSeekBar playbackEqBand5Bar;
	private Button playbackImportButton;
	private Button playbackOneshotButton;
	private Button playbackForeverButton;
	*/
	public AudioPlaybackPage playbackPage;
	public AudioRecordPage recordPage;
	private AudioVoicePage voicePage;
	private AudioBluetoothPage bluetoothPage;
	private AudioFmPage fmPage;
	
	private TextView welcomeTitle;
	public FragmentManager fragmentManager;
	
	public AudioFileManager fileManager;
	
	private static class Device {
		public static int NONE = 0;
		public static int SPEAKER = 1;
		public static int HEADPHONE = 2;
		public static int EARPIECE = 3;
	}
	
	private static class Storage {
		public static int spkVolume;
		public static int hpVolume;
		public static int epVolume;
		public static int eqCoeff1;
		public static int eqCoeff2;
		public static int eqCoeff3;
		public static int eqCoeff4;
		public static int eqCoeff5;
		public static String fileContent;
	}
	
	private static class ToastManager {
		//private static Toast spkToast;
		//private static Toast hpToast;
		//private static Toast epToast;
		private static Toast mToast;
		
		public static void showToast(Context context, String msg, int duration) {
			if (mToast == null) {
				mToast = Toast.makeText(context, msg, duration);
			} else {
				mToast.setText(msg);
			}
			mToast.show();
		}
	};
	
	@Override
	// only called first time press menu
	public boolean onCreateOptionsMenu(Menu menu) {
		try {
			Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
			Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
			m.setAccessible(true);
			m.invoke(menu, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_tunning, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_NONE) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case R.id.menu_welcome:
			loadRadioGroupFragment();
			break;
		case R.id.menu_current:
			ToastManager.showToast(getApplicationContext(), 
					   "正在开发...", 
					   Toast.LENGTH_SHORT);
			break;
		case R.id.menu_recent:
			Intent intent = new Intent(AudioTunningActivity.this, AudioXmlSelectorActivity.class);
//			Bundle bundle = new Bundle();
//			bundle.putSerializable("class", fileManager);
//			intent.putExtras(bundle);
			startActivity(intent);
			MyApp myApp = (MyApp) getApplication();
			myApp.setAudioFileManager(fileManager);
			break;			
		case R.id.menu_save:
			fileManager.writeScenarioXml(AudioScenario.querySecondScenario());
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void clearSwitchFragmentCurrentPosition() {
		AudioPlaybackPage.clearSwitchFragmentCurrentPosition();
		AudioRecordPage.clearSwitchFragmentCurrentPosition();
		AudioVoicePage.clearSwitchFragmentCurrentPosition();
		AudioBluetoothPage.clearSwitchFragmentCurrentPosition();
		AudioFmPage.clearSwitchFragmentCurrentPosition();
	}
	
	private void loadRadioGroupFragment() {
		// when redraw RadioGroup, must reset current scenario, so that onRadioButtonClick goes into correct case branch
		AudioScenario.setPrimaryScenarioDebug(AudioScenario.SCENARIO_NONE);
		
		FragmentTransaction transaction3 = fragmentManager.beginTransaction();
		AudioRadioGroupFragment radioGroupFragment = AudioRadioGroupFragment.getInstance();
		transaction3.replace(R.id.radiogroup_layout, radioGroupFragment);
		transaction3.commit();	
		
	}
	
	private void loadTop2FragmentsByPress(int checkedId) {
		
		Log.d(TAG, "loadTop2FragmentsByPress");
		 // set current primary scenario
		AudioScenario.setPrimaryScenarioFromTab(checkedId);
		// set current second scenario according to current primary scenario
		switch (AudioScenario.queryPrimaryScenario()) {
		case AudioScenario.SCENARIO_PLAYBACK: //has second scenario, we should show active content fragment
			AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_PLAYBACK_SINGLE);
			break;
		case AudioScenario.SCENARIO_VOICE_CALL: //has second scenario, we should show active content fragment
			AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK);
			break;
		case AudioScenario.SCENARIO_BLUETOOTH_CALL: //has second scenario, we should show active content fragment
			AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE);
			break;
		case AudioScenario.SCENARIO_RECORD: //has second scenario, we should show active content fragment
			AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_RECORD_COMMON);
			break;
		case AudioScenario.SCENARIO_FM: //has second scenario, we should show active content fragment
			AudioScenario.setSecondScenarioFromTabSwitcher(AudioScenario.SCENARIO_FM_LISTEN);
			break;
		default:
			break;
		}
		
		clearSwitchFragmentCurrentPosition();
		// if radiobutton value changed, clear currentPosition of two activity page
		// bad behaviour to put code here
//		AudioPlaybackPage.currentPosition = 0;
//		AudioVoicePage.currentPosition = 0;
//		AudioBluetoothPage.currentPosition = 0;
//		AudioFmPage.currentPosition = 0;
		
		// change whole layout background
		if (checkedId == AudioScenario.TAB_NONE) // welcome page
			changeLayoutBackground(AudioScenario.MODE_WELCOME_PIC);
		else
			changeLayoutBackground(AudioScenario.MODE_CONTENT_PIC);
		
		// load TabSwitcher fragment
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		AudioSwitchFragment switchFragment = AudioSwitchFragment.getInstance();
		transaction.replace(R.id.switch_layout, switchFragment);
		transaction.commit();
		
		// load content fragment
		FragmentTransaction transaction2 = fragmentManager.beginTransaction();
		AudioContentFragment contentFragment = AudioContentFragment.getInstance();				
		transaction2.replace(R.id.content_layout, contentFragment);
		transaction2.commit();

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.audio_tunning);
		Log.d(TAG, "onCreate");
		
		fragmentManager = getFragmentManager();
		//welcomeTitle = (TextView) findViewById(R.id.welcome_title); // could not find here since loaded in fragment later
		
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		music = soundPool.load(this, R.raw.keypressreturn, 1);
		
		// fragments are loaded in sequence, firstly load radiogroup fragment in the bottom
		loadRadioGroupFragment();
		//audiotunningManager = IAudioTunningService.Stub.asInterface(ServiceManager.getService("audiotunning"));
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
//		playbackPage = new AudioPlaybackPage(this, audiotunningManager, audioManager);
//		recordPage = new AudioRecordPage(this, audiotunningManager, audioManager);
//		voicePage = new AudioVoicePage(this, audiotunningManager, audioManager);
//		bluetoothPage = new AudioBluetoothPage(this, audiotunningManager, audioManager);
//		fmPage = new AudioFmPage(this, audiotunningManager, audioManager);
		
		playbackPage = new AudioPlaybackPage(this, audioManager);
		recordPage = new AudioRecordPage(this, audioManager);
		voicePage = new AudioVoicePage(this, audioManager);
		bluetoothPage = new AudioBluetoothPage(this, audioManager);
		fmPage = new AudioFmPage(this, audioManager);
		
		// merge xml manager
		fileManager = new AudioFileManager(this);
		fileManager.createAppDirs();
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		//playbackTitle = (TextView) findViewById(R.id.para_text);
		
	}
	
	private int produceRandomNumber(int min, int max) { // no repeate
		Random random = new Random();
		int n = random.nextInt(max)%(max-min+1) + min;
		return n;
	}
	
	public void changeLayoutBackground(int mode) {
		LinearLayout wholeLayout = (LinearLayout) findViewById(R.id.whole_layout);
		Resources res = getResources();
		Drawable drawable = null;
		
		if (mode == AudioScenario.MODE_WELCOME_PIC) {
			int randomNumber = produceRandomNumber(1, MAX_WELCOME_BACKGROUND+1)-1; // strange
			currentWelcomeBackground = randomNumber;
			Log.d(TAG, "mode="+mode+",randomNumber="+randomNumber);
			int resourceId = getResources().getIdentifier("wood_background_"+randomNumber, "drawable", getPackageName());
			drawable = res.getDrawable(resourceId);
			wholeLayout.setBackgroundDrawable(drawable);
			//welcomeTitle.setBackgroundResource(R.color.red);
			return;
		}
		
		int randomNumber = produceRandomNumber(MAX_WELCOME_BACKGROUND+1, NUM_WOOD_BACKGROUND);
		currentWelcomeBackground = randomNumber;
		Log.d(TAG, "mode="+mode+",randomNumber="+randomNumber);
		int resourceId = getResources().getIdentifier("wood_background_"+randomNumber, "drawable", getPackageName());
		
		drawable = res.getDrawable(resourceId);
		wholeLayout.setBackgroundDrawable(drawable);
		
		
		/*switch (AudioScenario.queryPrimaryScenario()) {
		case AudioScenario.SCENARIO_NONE:
			drawable = res.getDrawable(R.drawable.yellow_wall_background);
			wholeLayout.setBackgroundDrawable(drawable);
			break;
		case AudioScenario.SCENARIO_PLAYBACK:
			drawable = res.getDrawable(R.drawable.wood_raw_background);
			wholeLayout.setBackgroundDrawable(drawable);
			break;
		case AudioScenario.SCENARIO_RECORD:
			drawable = res.getDrawable(R.drawable.wood_ring_background);
			wholeLayout.setBackgroundDrawable(drawable);
			break;
		case AudioScenario.SCENARIO_VOICE_CALL:
			drawable = res.getDrawable(R.drawable.bamboo_mat_background);
			wholeLayout.setBackgroundDrawable(drawable);
			break;
		case AudioScenario.SCENARIO_BLUETOOTH_CALL:
			drawable = res.getDrawable(R.drawable.wood_grassgreen_background);
			wholeLayout.setBackgroundDrawable(drawable);
			break;
		case AudioScenario.SCENARIO_FM:
			drawable = res.getDrawable(R.drawable.wood_old_background);
			wholeLayout.setBackgroundDrawable(drawable);
			break;
		default:
			break;
		}*/
	}
	
//	private void showWelcome() {
//		Log.d(TAG, "showWelcome");
//		if (fragmentManager == null) return;
//		
//		AudioScenario.setPrimaryScenarioFromTab(AudioScenario.TAB_NONE);
//		
////		FragmentTransaction transaction = fragmentManager.beginTransaction();
////		AudioSwitchFragment switchFragment = AudioSwitchFragment.getInstance();
////		transaction.replace(R.id.switch_layout, switchFragment);
////		transaction.commit();
//		
//		FragmentTransaction transaction2 = fragmentManager.beginTransaction();
//		AudioContentFragment contentFragment = AudioContentFragment.getInstance();
//		transaction2.replace(R.id.content_layout, contentFragment);
//		transaction2.commit();
//		
//		// the above two fragment uses the same background png, it may look divided and ugly
//		// so I add this code to make the two fragment share one background png
//		changeLayoutBackground(AudioScenario.MODE_WELCOME_PIC);
//	}
	
	// common UI not related to scenario
	public void updateTabColor() {
		Log.d(TAG, "updateTabColor");
		// should not use API setBackgroundColor
		// Change RadioButton background color
		playbackTab.setBackgroundResource(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_PLAYBACK ? R.color.black: R.color.gray);
		recordTab.setBackgroundResource(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_RECORD ? R.color.black: R.color.gray);
		voiceTab.setBackgroundResource(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_VOICE_CALL ? R.color.black: R.color.gray);
		btTab.setBackgroundResource(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL ? R.color.black: R.color.gray);
		fmTab.setBackgroundResource(AudioScenario.queryPrimaryScenario() == AudioScenario.SCENARIO_FM ? R.color.black: R.color.gray);
	}
	
	// only calls when press back to homepage in menu options
	public void resetTabButtonDrawableAndText() {
		Log.d(TAG, "resetTabButtonDrawable");
//		Resources res = getResources();
//		playbackTab.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.playback_selector_background_default), null, null);
//		recordTab.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.record_selector_background_default), null, null);
//		voiceTab.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.voice_selector_background_default), null, null);
//		btTab.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.bt_selector_background_default), null, null);
//		fmTab.setCompoundDrawablesWithIntrinsicBounds(null, res.getDrawable(R.drawable.fm_selector_background_default), null, null);
//		playbackTab.setTextColor(R.color.white);
//		recordTab.setTextColor(R.color.white);
//		voiceTab.setTextColor(R.color.white);
//		btTab.setTextColor(R.color.white);
//		fmTab.setTextColor(R.color.white);
	}
	
	@Override
	// when user click RadioButton
	public void onRadioButtonClick(int whichFragmentLoaded) { // current scenario already set in onCheckedChanged of RadioGroup
		Log.d(TAG, "onRadioButtonClick, whichFragmentLoaded="+whichFragmentLoaded);
		
		// UI is consisted of 3 parts: TabSwitcher fragment + content fragment + RadioButton
		// fragment load sequence after press RadioButton : load TabSwitcher fragment --> load content fragment
		// complete scenario is primary scenario + second scenario
		switch (AudioScenario.queryPrimaryScenario()) {
		case AudioScenario.SCENARIO_PLAYBACK: // playback primary scenario
			if (whichFragmentLoaded == AudioScenario.FRAGMENT_TABSWITCHER)
				playbackPage.setPlaybackTabSwitcherFragmentClickListener();
			else if (whichFragmentLoaded == AudioScenario.FRAGMENT_CONTENT) {
				// playback second scenario
				if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_PLAYBACK_SINGLE)  {
					playbackPage.setPlaybackSingleContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_SINGLE);
				}
				else if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_PLAYBACK_DUAL) {
					playbackPage.setPlaybackDualContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_PLAYBACK_DUAL);
				}
			}
			break;
		case AudioScenario.SCENARIO_RECORD: // record tab
			if (whichFragmentLoaded == AudioScenario.FRAGMENT_TABSWITCHER)
				recordPage.setRecordTabSwitcherFragmentClickListener();
			else if (whichFragmentLoaded == AudioScenario.FRAGMENT_CONTENT) {
				if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_RECORD_COMMON) {
					recordPage.setRecordContentFragmentClickListener(); // find resource and set listener
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_RECORD_COMMON);
				}
				else if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_RECORD_CALL) {
					// this scenario is undefined
				}
			}
			break;
		case AudioScenario.SCENARIO_VOICE_CALL: // voice tab
			if (whichFragmentLoaded == AudioScenario.FRAGMENT_TABSWITCHER)
				voicePage.setVoiceTabSwitcherFragmentClickListener();
			else if (whichFragmentLoaded == AudioScenario.FRAGMENT_CONTENT) {
				if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK) {
					voicePage.setVoiceDownContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK);
				}
				else if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_VOICE_CALL_UPLINK) {
					voicePage.setVoiceUpContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_VOICE_CALL_UPLINK);
				}
			}
			break;
		case AudioScenario.SCENARIO_BLUETOOTH_CALL: // bluetooth tab
			if (whichFragmentLoaded == AudioScenario.FRAGMENT_TABSWITCHER)
				bluetoothPage.setBluetoothTabSwitcherFragmentClickListener();
			else if (whichFragmentLoaded == AudioScenario.FRAGMENT_CONTENT) {
				if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE) {
					bluetoothPage.setBluetoothPhoneContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE);
				}
				else if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP) {
					bluetoothPage.setBluetoothVoipContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP);
				}
			}
			break;
		case AudioScenario.SCENARIO_FM: // fm tab
			if (whichFragmentLoaded == AudioScenario.FRAGMENT_TABSWITCHER)
				fmPage.setFmTabSwitcherFragmentClickListener();
			else if (whichFragmentLoaded == AudioScenario.FRAGMENT_CONTENT) {
				if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_FM_LISTEN) {
					fmPage.setFmContentFragmentClickListener(); // find resource and set listener
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_FM_LISTEN);
				}
				else if (AudioScenario.querySecondScenario() == AudioScenario.SCENARIO_FM_RECORD) {
					fmPage.setFmRecordContentFragmentClickListener();
					AudioStorage.recoverScenarioSeekBarValue(AudioScenario.SCENARIO_FM_RECORD);
				}
			}
			break;
		case AudioScenario.SCENARIO_NONE: // welcome page
			Log.d(TAG, "currentWelcomeBackground="+currentWelcomeBackground);
			// for beauty reason: set text color to red or still keep black
			if (whichFragmentLoaded == AudioScenario.FRAGMENT_CONTENT && currentWelcomeBackground == 0) {
				welcomeTitle = (TextView) findViewById(R.id.welcome_title);
				welcomeTitle.setTextColor(getResources().getColor(R.color.red));
			}
			// RadioGroup is loaded
			else if (whichFragmentLoaded == AudioScenario.FRAGMENT_RADIOGROUP) {
				radioGroup = (RadioGroup) findViewById(R.id.radio_group);
				
				radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) { // to load fragment
						Log.d(TAG, "onCheckedChanged, checkedId="+checkedId);
						loadTop2FragmentsByPress(checkedId);
					}
				});
				
				playbackTab = (RadioButton)findViewById(R.id.playback_tab);
				recordTab = (RadioButton)findViewById(R.id.record_tab);
				voiceTab = (RadioButton)findViewById(R.id.voice_tab);
				btTab = (RadioButton)findViewById(R.id.bt_tab);
				fmTab = (RadioButton)findViewById(R.id.fm_tab);
				
				playbackTab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						soundPool.play(music, 1, 1, 0, 0, 1);
					}
				});
				recordTab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						soundPool.play(music, 1, 1, 0, 0, 1);
					}
				});
				voiceTab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						soundPool.play(music, 1, 1, 0, 0, 1);
					}
				});
				btTab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						soundPool.play(music, 1, 1, 0, 0, 1);
					}
				});
				fmTab.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						soundPool.play(music, 1, 1, 0, 0, 1);
					}
				});
				AudioScenario.setTabButtonId(AudioScenario.TAB_PLAYBACK, playbackTab.getId());
				AudioScenario.setTabButtonId(AudioScenario.TAB_RECORD, recordTab.getId());
				AudioScenario.setTabButtonId(AudioScenario.TAB_VOICE_CALL, voiceTab.getId());
				AudioScenario.setTabButtonId(AudioScenario.TAB_BLUETOOTH_CALL, btTab.getId());
				AudioScenario.setTabButtonId(AudioScenario.TAB_FM, fmTab.getId());
				
				loadTop2FragmentsByPress(AudioScenario.getButtonId(AudioScenario.TAB_NONE));
			}
			break;
		default:
			break;
		}
		updateTabColor();
		
	}
	
	public boolean isPlaybackTesting() {
		return playbackPage.isTesting();
	}
	
	public boolean isRecordTesting() {
		return recordPage.isTesting();
	}
	
	public void stopPlaybackTest() {
		playbackPage.stopTest();
	}
	
	public void stopRecordTest() {
		recordPage.stopTest();
	}
	
}
