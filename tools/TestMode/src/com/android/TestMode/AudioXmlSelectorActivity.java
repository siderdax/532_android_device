package com.android.TestMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AudioXmlSelectorActivity extends Activity {
	
	private static final String TAG = "AudioXmlSelectorActivity";
	private AudioFileManager fileManager;
	private ListView listView;
	private TextView textView;
	private List<String> filePathList;
	private List<String> fileNameList;
	
	private SAXParserFactory factory;
	private SAXParser parser;
	private XMLReader reader;
	private AudioXmlParseListener listener;
	
	private String convertInputStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line+"\n");
			}
		} catch (IOException e) {
				e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Log.d(TAG, "xml file string="+sb.toString());
		return sb.toString();
		
	}
	
	private void inflateListView(List<String> nameList) {
		
		Log.d(TAG, "inflateListView size()="+nameList.size());
		List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
		for (int i=0; i<nameList.size(); i++) {
			Map<String,Object> listItem = new HashMap<String,Object>();
			listItem.put("icon", R.drawable.file);
			listItem.put("fileName", nameList.get(i));
			listItems.add(listItem);
		}
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.line,
				new String[]{"icon", "fileName"},
				new int[]{R.id.icon, R.id.file_name});
		listView.setAdapter(simpleAdapter);
		textView.setText("选择一个存档");
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String path = filePathList.get(arg2);
				//ToastManager.showToast(getApplicationContext(), ""+path, Toast.LENGTH_SHORT);
				try {
					InputStream inputStream = new FileInputStream(path);
					String content = convertInputStreamToString(inputStream);
					StringReader stringReader = new StringReader(content);
					InputSource is = new InputSource(stringReader);
					reader.parse(is);
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// now xml is handled
				finish();
			}
		});
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		setContentView(R.layout.audio_xml_list);
		listView = (ListView)findViewById(R.id.xml_list);
		textView = (TextView)findViewById(R.id.hint_txt);
		Log.d(TAG, "onCreate");
		
		//fileManager = (AudioFileManager) getIntent().getSerializableExtra("class");
		MyApp myApp = (MyApp) getApplication();
		fileManager = myApp.getAudioFileManager();
		filePathList = fileManager.getScenarioXmlPathList(AudioScenario.querySecondScenario());
		fileNameList = fileManager.parseXmlNameList(filePathList);
		
		inflateListView(fileNameList);
		
		// xml parser
		try {
			factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
			reader = parser.getXMLReader();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		listener = new AudioXmlParseListener();
		reader.setContentHandler(listener);
		
	}

}
