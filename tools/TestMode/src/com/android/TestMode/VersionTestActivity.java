package com.android.TestMode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.android.TestMode.EngSqlite;//[yeez_haojie add 11.22]

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class VersionTestActivity extends Activity{
	static final int FINISH_TEST = 1;
	 public EngSqlite mEngSqlite;//[yeez_haojie add 11.22]
    private final Timer timer = new Timer();
              private TimerTask task;
              Handler handler = new Handler() {
                       public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                if(msg.what == FINISH_TEST)
                                {
                                          new AlertDialog.Builder(VersionTestActivity.this)
                                          .setTitle(R.string.test_version)
                                          .setMessage(R.string.DialogMessage)            
                                          .setPositiveButton(R.string. Button_Yes, new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int whichButton) {
                                                       setResult(RESULT_OK);
                                                       finish();
                                                   }
                                
                                          })
                                          .setNegativeButton(R.string.Button_No, new DialogInterface.OnClickListener() {
                                                   public void onClick(DialogInterface dialog, int whichButton) {
                                                       setResult(RESULT_FIRST_USER);
                                                       finish();
                                                   }
                                          })
                                          .setOnKeyListener(new OnKeyListener() {

							        			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//							        				if (keyCode == KeyEvent.KEYCODE_BACK ) {	
//							        					
//							        					return true;
//							        		         }
							        				Log.d("VersionTestActivity","key:"+keyCode);
							        		         return true;
							        			}
							        		}).show();
                                          
                                }
                                
                       }
              };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.xml.version);
		
		mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.22]
		
/*		TextView versionView = (TextView) findViewById(R.id.versionTestView);
		
		//[yeez_haojie add 12.20 for greenlength]
		String greenstr = SystemProperties.get("ro.build.version.release");
		int lengthgreen = greenstr.length();
		//[yeez_haojie modfiy 12.17 for style]
		SpannableStringBuilder style = new SpannableStringBuilder(getString(R.string.firmware_version)
				+ SystemProperties.get("ro.build.version.release"));
        style.setSpan(new ForegroundColorSpan(Color.GREEN), 6, 6+lengthgreen, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        versionView.append(style);
        versionView.append("\n");
		
		String greenstr2 = SystemProperties.get("ro.product.hardware","H01");
		int lengthgreen2 = greenstr2.length();
		SpannableStringBuilder style2 = new SpannableStringBuilder(getString(R.string.hardware_version)
				+ SystemProperties.get("ro.product.hardware","H01"));
		style2.setSpan(new ForegroundColorSpan(Color.GREEN), 6, 6+lengthgreen2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//12
        versionView.append(style2);
		versionView.append("\n");
		
		String greenstr3 = SystemProperties.get("gsm.version.baseband");
		int lengthgreen3 = greenstr3.length();
		SpannableStringBuilder style3 = new SpannableStringBuilder(getString(R.string.baseband_version)
				+ SystemProperties.get("gsm.version.baseband"));
		style3.setSpan(new ForegroundColorSpan(Color.GREEN), 6, 6+lengthgreen3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//66
        versionView.append(style3);
		versionView.append("\n");
		
		String greenstr4 = getFormattedKernelVersion();
		int lengthgreen4 = greenstr4.length();
		SpannableStringBuilder style4 = new SpannableStringBuilder(getString(R.string.kernal_version)
				+ getFormattedKernelVersion());
		style4.setSpan(new ForegroundColorSpan(Color.GREEN), 6, 6+lengthgreen4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//12
        versionView.append(style4);
		versionView.append("\n");
		
		String greenstr5 = SystemProperties.get("ro.build.display.id");
		int lengthgreen5 = greenstr5.length();
		SpannableStringBuilder style5 = new SpannableStringBuilder(getString(R.string.software_version)
				+ SystemProperties.get("ro.build.display.id"));
		style5.setSpan(new ForegroundColorSpan(Color.GREEN), 6, 6+lengthgreen5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);//66
        versionView.append(style5);*/
		
		//[SSCR_huanglongcheng change 2014.1.21]
		String greenstr = SystemProperties.get("ro.build.version.release");
		String greenstr2 = SystemProperties.get("ro.product.hardware","H01");
		String greenstr3 = SystemProperties.get("gsm.version.baseband");
		String greenstr4 = getFormattedKernelVersion();
		String greenstr5 = SystemProperties.get("ro.build.display.id");
		
		String[] names = new String[]	{ 
				getString(R.string.firmware_version), 
				getString(R.string.hardware_version), 
				getString(R.string.baseband_version),
				getString(R.string.kernal_version), 
				getString(R.string.software_version)
				};
		
		String[] descs = new String[] {
				greenstr, greenstr2, greenstr3, greenstr4, greenstr5
		};
		
		List<Map<String, Object>> listItems = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < names.length; i++)
		{
			Map<String, Object> listItem = new HashMap<String, Object>();
			listItem.put ("names", names[i]);
			listItem.put ("desc", descs[i]);
			listItems.add (listItem);
		}
		SimpleAdapter versionSimple = new SimpleAdapter (this, listItems, R.xml.version_item,
				new String [] {"names", "desc"}, new int [] {R.id.version_name, R.id.version_desc});
		ListView list = (ListView) findViewById (R.id.versionTestView);
		list.setAdapter (versionSimple);
		setupBottom();
	}

	private String getFormattedKernelVersion() {
        String procVersionStr;
        boolean isCtaMode = false;
        
        isCtaMode = SystemProperties.getBoolean("ro.product.cta.mode", false) || SystemProperties.getBoolean("ro.product.cmcc.mode", false);
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/proc/version"), 256);
            try {
                procVersionStr = reader.readLine();
            } finally {
                reader.close();
            }

            final String PROC_VERSION_REGEX =
                "\\w+\\s+" + /* ignore: Linux */
                "\\w+\\s+" + /* ignore: version */
                "([^\\s]+)\\s+" + /* group 1: 2.6.22-omap1 */
                "\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
                "\\(.*?(?:\\(.*?\\)).*?\\)\\s+" + /* ignore: (gcc ..) */
                "([^\\s]+)\\s+" + /* group 3: #26 */
                "(?:PREEMPT\\s+)?" + /* ignore: PREEMPT (optional) */
                "(.+)"; /* group 4: date */


            //final String PROC_VERSION_REGEX =
            //"\\w+\\s+" + /* ignore: Linux */
            //"\\w+\\s+" + /* ignore: version */
            //"(\\d+\\.\\d+\\.\\d+)\\s" + /* group 1: 2.6.25 */
            //"\\(([^\\s@]+(?:@[^\\s.]+)?)[^)]*\\)\\s+" + /* group 2: (xxxxxx@xxxxx.constant) */
            //"\\((\\w+\\s+\\w+\\-?\\w+\\s+\\d+\\.\\d+\\.\\d+)\\)\\s+" + /* group 3: gcc version 4.2.1 */
            //"(\\#\\d+)\\s" + /* group 4: #26 */
            //"(.+)"; /* group 5: date */
						
            Pattern p = Pattern.compile(PROC_VERSION_REGEX);
            Matcher m = p.matcher(procVersionStr);
            String s,tag;
            if (!m.matches()) {
                return "Unavailable";
            } else if (m.groupCount() < 4) {
                return "Unavailable";             
            } else {
            	try{            		
            		s = m.group(4);
                	tag=s.substring(s.lastIndexOf("W", s.length()), s.length());
                	s = s.substring(0, s.lastIndexOf("W",s.length()));                	
                	
//                	if( !isCtaMode ){
                	return (new StringBuilder(m.group(1)).toString().replace('+', '-'));                   	 
//                	}else{
//                		return (new StringBuilder(m.group(1)).toString());	
//                	}
                	

            	}catch (Exception e){
            		return (new StringBuilder(m.group(1).substring(0, 6)).toString());
            	}
            	
            }
        } catch (IOException e) {  
            return "Unavailable";
        }
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		finish();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setupBottom() {
        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               setResult(RESULT_OK);;
               
               BaseActivity.NewstoreRusult(true, "Version",mEngSqlite);//[yeez_haojie add 11.22]
               
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               
               BaseActivity.NewstoreRusult(false, "Version",mEngSqlite);//[yeez_haojie add 11.22]
               
               finish();
            }
        });
     }
}
