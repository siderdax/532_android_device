package com.android.TestMode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class AudioFileManager implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String TAG = "AudioFileManager";
	
	public String appDirPath;
	public String playbackSingleDirPath;
	public String playbackDualDirPath;
	public String recordCommonDirPath;
	public String recordCallDirPath;
	public String voiceDownDirPath;
	public String voiceUpDirPath;
	public String btPhoneDirPath;
	public String btVoipDirPath;
	public String fmListenDirPath;
	public String fmRecordDirPath;
	public String recordFilePath;
	
	private XmlPullParserFactory factory;
	private XmlSerializer serializer;
	private AudioTunningActivity activity;
	
	private Map<Integer,String> scenarioDirMap = new HashMap<Integer,String>();
	private Map<Integer,String> scenarioPrefixMap = new HashMap<Integer,String>();
	
	public AudioFileManager(AudioTunningActivity activity) {
		this.activity = activity;
		try {
			factory = XmlPullParserFactory.newInstance();
			serializer = factory.newSerializer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createAppDirs() {
		Log.d(TAG, "createAppDir");
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			appDirPath = sdcardDir.getPath()+"/TestMode";
			playbackSingleDirPath = appDirPath+"/playbackSingleScenario";
			playbackDualDirPath = appDirPath+"/playbackDualScenario";
			recordCommonDirPath = appDirPath+"/recordCommonScenario";
			recordCallDirPath = appDirPath+"/recordCallScenario";
			voiceDownDirPath = appDirPath+"/voiceDownScenario";
			voiceUpDirPath = appDirPath+"/voiceUpScenario";
			btPhoneDirPath = appDirPath+"/btPhoneScenario";
			btVoipDirPath = appDirPath+"/btVoipScenario";
			fmListenDirPath = appDirPath+"/fmListenScenario";
			fmRecordDirPath = appDirPath+"/fmRecordScenario";
			recordFilePath = appDirPath+"record.amr";
			
			scenarioDirMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, playbackSingleDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_PLAYBACK_DUAL, playbackDualDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_RECORD_COMMON, recordCommonDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_RECORD_CALL, recordCallDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, voiceDownDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, voiceUpDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, btPhoneDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, btVoipDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_FM_LISTEN, fmListenDirPath);
			scenarioDirMap.put(AudioScenario.SCENARIO_FM_RECORD, fmRecordDirPath);
			
			scenarioPrefixMap.put(AudioScenario.SCENARIO_PLAYBACK_SINGLE, "scene1_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_PLAYBACK_DUAL, "scene2_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_RECORD_COMMON, "scene3_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_RECORD_CALL, "scene4_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK, "scene5_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_VOICE_CALL_UPLINK, "scene6_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE, "scene7_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP, "scene8_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_FM_LISTEN, "scene9_");
			scenarioPrefixMap.put(AudioScenario.SCENARIO_FM_RECORD, "scene10_");
			
			File rootDir = new File(appDirPath);
			if (!rootDir.exists()) {
				rootDir.mkdirs();
			}
			
			
			File playbackSingleDir = new File(playbackSingleDirPath);
			if (!playbackSingleDir.exists()) {
				playbackSingleDir.mkdirs();
			}
			File playbackDualDir = new File(playbackDualDirPath);
			if (!playbackDualDir.exists()) {
				playbackDualDir.mkdirs();
			}
			File recordCommonDir = new File(recordCommonDirPath);
			if (!recordCommonDir.exists()) {
				recordCommonDir.mkdirs();
			}
			File recordCallDir = new File(recordCallDirPath);
			if (!recordCallDir.exists()) {
				recordCallDir.mkdirs();
			}
			File voiceDownDir = new File(voiceDownDirPath);
			if (!voiceDownDir.exists()) {
				voiceDownDir.mkdirs();
			}
			File voiceUpDir = new File(voiceUpDirPath);
			if (!voiceUpDir.exists()) {
				voiceUpDir.mkdirs();
			}
			File btPhoneDir = new File(btPhoneDirPath);
			if (!btPhoneDir.exists()) {
				btPhoneDir.mkdirs();
			}
			File btVoipDir = new File(btVoipDirPath);
			if (!btVoipDir.exists()) {
				btVoipDir.mkdirs();
			}		
			File fmListenDir = new File(fmListenDirPath);
			if (!fmListenDir.exists()) {
				fmListenDir.mkdirs();
			}
			File fmRecordDir = new File(fmRecordDirPath);
			if (!fmRecordDir.exists()) {
				fmRecordDir.mkdirs();
			}
		}
	}
	
	private String getSystemTime() {
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		int second = c.get(Calendar.SECOND);
		Log.d(TAG, "year"+year);
		Log.d(TAG, "month"+month);
		Log.d(TAG, "day"+day);
		Log.d(TAG, "hour"+hour);
		Log.d(TAG, "minute"+minute);
		Log.d(TAG, "second"+second);
		return ""+year+"-"+month+"-"+day+"-"+hour+"-"+minute+"-"+second;
	}
	
	/*private boolean checkRepeatedFileName(String dirPath, String newname) {
		File file = new File(dirPath);
		File[] subFile = file.listFiles();
		for (int i=0; i<subFile.length; i++) {
			if (!subFile[i].isDirectory()) {
				String filename = subFile[i].getName();
				if (filename.equals(newname)) return true;
			}
		}
		return false;
	} */
	private String getNewFilePath(int scenario) {
		String newname;
		String newpath;
		newname = scenarioPrefixMap.get(scenario)+getSystemTime()+".xml";
		newpath = scenarioDirMap.get(scenario)+"/"+newname;
		ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
		return newpath;
//		switch (scenario) {
//		case AudioScenario.SCENARIO_PLAYBACK_SINGLE:
//			newname = "scene1_"+getSystemTime()+".xml";
//			newpath = playbackSingleDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_PLAYBACK_DUAL:
//			newname = "scene2_"+getSystemTime()+".xml";
//			newpath = playbackDualDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_RECORD_COMMON:
//			newname = "scene3_"+getSystemTime()+".xml";
//			newpath = recordCommonDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_RECORD_CALL:
//			newname = "scene4_"+getSystemTime()+".xml";
//			newpath = recordCallDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK:
//			newname = "scene5_"+getSystemTime()+".xml";
//			newpath = voiceDownDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_VOICE_CALL_UPLINK:
//			newname = "scene6_"+getSystemTime()+".xml";
//			newpath = voiceUpDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE:
//			newname = "scene7_"+getSystemTime()+".xml";
//			newpath = btPhoneDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP:
//			newname = "scene8_"+getSystemTime()+".xml";
//			newpath = btVoipDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_FM_LISTEN:
//			newname = "scene9_"+getSystemTime()+".xml";
//			newpath = fmListenDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		case AudioScenario.SCENARIO_FM_RECORD:
//			newname = "scene10_"+getSystemTime()+".xml";
//			newpath = fmRecordDirPath+"/"+newname;
//			ToastManager.showToast(activity.getApplicationContext(), "已保存至"+newpath, Toast.LENGTH_SHORT);
//			return newpath;
//		default:
//			return "";
//		}
	}
	
	public List<String> getScenarioXmlPathList(int scenario) {
		List<String> pathList = new ArrayList<String>();
		String dirPath = scenarioDirMap.get(scenario);
		File file = new File(dirPath);
		File[] subFiles = file.listFiles();
		for (int i=0; i<subFiles.length; i++) {
			if (!subFiles[i].isDirectory()) {
				String filename = subFiles[i].getName();
				pathList.add(scenarioDirMap.get(scenario)+"/"+filename);
			}
		}
		return pathList;
	}
	
	// parse filePathList to fileNameList
	public List<String> parseXmlNameList(List<String> pathList) {
		List<String> nameList = new ArrayList<String>();
		for (int i=0; i<pathList.size(); i++) {
			String path = pathList.get(i);
			String name = "";
			if ((path != null) && (path.length() > 0)) {
				int dot = path.lastIndexOf("/");
				if ((dot > -1) && (dot < (path.length() - 1))) {
					name = path.substring(dot+1);
				}
			}
			nameList.add(name);
		}
		Log.d(TAG, "pathList.size()="+pathList.size()+",nameList.size()="+nameList.size());
		return nameList;
	}
	
	// write scenario data from AudioStorage into xml
	public void writeScenarioXml(int scenario) {
		Log.d(TAG, "writeScenarioXml"+"  "+getSystemTime());
		// check parameter, need second scenario only
		if (scenario != AudioScenario.SCENARIO_PLAYBACK_SINGLE
				&& scenario != AudioScenario.SCENARIO_PLAYBACK_DUAL
				&& scenario != AudioScenario.SCENARIO_RECORD_COMMON
				//&& scenario != AudioScenario.SCENARIO_RECORD_CALL  // unsupported
				&& scenario != AudioScenario.SCENARIO_VOICE_CALL_DOWNLINK
				&& scenario != AudioScenario.SCENARIO_VOICE_CALL_UPLINK
				&& scenario != AudioScenario.SCENARIO_BLUETOOTH_CALL_PHONE
				&& scenario != AudioScenario.SCENARIO_BLUETOOTH_CALL_VOIP
				&& scenario != AudioScenario.SCENARIO_FM_LISTEN
				&& scenario != AudioScenario.SCENARIO_FM_RECORD)
			return;
		StringWriter stringWriter = new StringWriter();
		try {
			serializer.setOutput(stringWriter);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "SeekBar");
			serializer.startTag("", "scenario");
			serializer.attribute("", "name", AudioScenario.scenarioName[scenario]);
			Map<String,Integer> map = AudioStorage.getScenarioSeekBarNameValueMap(scenario);
			Set<String> set = map.keySet();
			Iterator<String> iterator = set.iterator();
			while (iterator.hasNext()) {
				String name = iterator.next();
				Log.d(TAG, "SeekBar name="+name);
				serializer.startTag("", "para");
				serializer.attribute("", "name", name);
				serializer.attribute("", "value", ""+map.get(name));
				serializer.endTag("", "para");
			}
			serializer.endTag("", "scenario");
			serializer.endTag("", "SeekBar");
			serializer.endDocument();
			
			String newpath = getNewFilePath(scenario);
			Log.d(TAG, "newpath="+newpath);
			File file = new File(newpath);
			FileWriter fileWriter = new FileWriter(file, false);
			BufferedWriter bufWriter = new BufferedWriter(fileWriter);
			bufWriter.write(stringWriter.toString());
			bufWriter.newLine();
			bufWriter.close();
			fileWriter.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}
	
	

}
