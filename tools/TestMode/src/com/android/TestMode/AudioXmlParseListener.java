package com.android.TestMode;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class AudioXmlParseListener extends DefaultHandler {
	
	private String TAG = "AudioXmlParseListener";
	private int scenario = 0; // current scenario
	
	public AudioXmlParseListener() {
		
	}
	
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		Log.d(TAG, "startDocument");
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		Log.d(TAG, "endDocument");
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, 
							 Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		//Log.d(TAG, "startElement: localName="+localName+",qName="+qName+
		//		   ",attr.length="+attributes.getLength()+",attr.name="+attributes.getLocalName(0)+",attr.value="+attributes.getValue(0));
		if (localName.equals("SeekBar")) {};
		if (localName.equals("scenario")) {
			String scenarioName = attributes.getValue(0);
			scenario = AudioScenario.covertScenarioNameToIndex(scenarioName);
			Log.d(TAG, "scenarioName="+scenarioName+",scenario="+scenario);
		}
		if (localName.equals("para")) {
			String barName = attributes.getValue(0);
			int barValue = Integer.parseInt(attributes.getValue(1));
			Log.d(TAG, "barName="+barName+",barValue="+barValue);
			AudioStorage.saveScenarioSeekBarValue(scenario, barName, barValue);
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		super.endElement(uri, localName, qName);
		//Log.d(TAG, "endElement: localName="+localName+",qName="+qName);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		super.characters(ch, start, length);
		Log.d(TAG, "characters: ch="+new String(ch)+",start="+start+",length="+length);
		//String str = new String(ch, start, length);
	}

}
