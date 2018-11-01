package com.android.TestMode;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.apache.http.message.BufferedHeader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LcdTunningActivity extends Activity {
	
	private static final String TAG = "LcdTunningActivity";
	
	private EditText pre_divider;
	private TextWatcher textWatcher1;
	private StringBuilder sb1;
	
	private EditText main_divider;
	private TextWatcher textWatcher2;
	private StringBuilder sb2;
	
	private EditText scaler;
	private TextWatcher textWatcher3;
	private StringBuilder sb3;
	
	private EditText smies_sae_on;
	private TextWatcher textWatcher4;
	private StringBuilder sb4;
	
	private EditText smies_sae_gain;
	private TextWatcher textWatcher5;
	private StringBuilder sb5;
	
	private Button set_para;
	private Button select_txt;
	
	//private String cmd = "/bin/lcd_panel_test";
	private String lcdTool = "lcd_tunning";
	
	private boolean isNumeric1(String str) {
		for (int i=str.length();--i>=0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isNumeric2(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}
	
	private boolean isNumeric3(String str) {
		for (int i=str.length();--i>=0;) {
			int ch = str.charAt(i);
			if (ch<48 || ch>57) {
				return false;
			}
		}
		return true;
	}
	
	private void handleInput(CharSequence cs, EditText edit, StringBuilder sb) {
		if (!isNumeric1(cs.toString())) {
			ToastManager.showToast(getApplicationContext(), "Invalid parameter!", Toast.LENGTH_SHORT);
			edit.setText("");
		}
		else {
			sb.replace(0, sb.length(), ""); // clear StringBuilder
			sb.append(cs.toString());
		//Log.d(TAG, "pre_divider onTextChanged: "+sb1.toString()+"	length="+sb1.toString().length());
		}
	}
	
	private boolean checkBeforeSet() {
		if (sb1.toString().length() != 0 && sb2.toString().length() != 0 && sb3.toString().length() != 0)
			return true;
		else
			return false;
	}
	
	private boolean callShell(String cmd) {
		try {
			Process process = Runtime.getRuntime().exec(cmd);
			int exitVal = process.waitFor();
			if (exitVal != 0) {
				ToastManager.showToast(getApplicationContext(), "Process exit code is "+exitVal, Toast.LENGTH_SHORT);
				return false;
			}
			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = input.readLine();
			input.close();
			Log.d(TAG, "callShell returns="+line);
			return true;
		} catch (Throwable e) {
			ToastManager.showToast(getApplicationContext(), "An error occurs executing shell command", Toast.LENGTH_SHORT);
			return false;
		}
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lcd_tunning);
		
		pre_divider = (EditText) findViewById(R.id.lcd_pre_divider);
		main_divider = (EditText) findViewById(R.id.lcd_main_divider);
		scaler = (EditText) findViewById(R.id.lcd_scaler);
		smies_sae_on = (EditText) findViewById(R.id.lcd_smies_sae_on);
		smies_sae_gain = (EditText) findViewById(R.id.lcd_smies_sae_gain);
		set_para = (Button) findViewById(R.id.lcd_set_para);
		select_txt = (Button) findViewById(R.id.lcd_select_txt);
		
		sb1 = new StringBuilder();
		sb2 = new StringBuilder();
		sb3 = new StringBuilder();
		sb4 = new StringBuilder();
		sb5 = new StringBuilder();
		
		textWatcher1 = new TextWatcher() {
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.d(TAG, "pre_divider onTextChanged: "+cs.toString()+"	length="+cs.toString().length());
				handleInput(cs, pre_divider, sb1);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "pre_divider afterTextChanged");
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.d(TAG, "pre_divider beforeTextChanged: "+arg0.toString());
			}
		};
		
		textWatcher2 = new TextWatcher() {
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.d(TAG, "main_divider onTextChanged: "+cs.toString());
				handleInput(cs, main_divider, sb2);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "main_divider afterTextChanged");
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.d(TAG, "main_divider beforeTextChanged: "+arg0.toString());
			}
			
			
			
		};
		
		textWatcher3 = new TextWatcher() {
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.d(TAG, "scaler onTextChanged: "+cs.toString());
				handleInput(cs, scaler, sb3);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "scaler afterTextChanged");
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.d(TAG, "scaler beforeTextChanged: "+arg0.toString());
			}
		};
		
		textWatcher4 = new TextWatcher() {
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.d(TAG, "smies_sae_on onTextChanged: "+cs.toString());
				handleInput(cs, smies_sae_on, sb4);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "smies_sae_on afterTextChanged");
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.d(TAG, "smies_sae_on beforeTextChanged: "+arg0.toString());
			}
		};
		
		textWatcher5 = new TextWatcher() {
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				// TODO Auto-generated method stub
				Log.d(TAG, "smies_sae_gain onTextChanged: "+cs.toString());
				handleInput(cs, smies_sae_gain, sb5);
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				Log.d(TAG, "smies_sae_gain afterTextChanged");
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
				Log.d(TAG, "smies_sae_gain beforeTextChanged: "+arg0.toString());
			}
		};
		
		pre_divider.addTextChangedListener(textWatcher1);
		main_divider.addTextChangedListener(textWatcher2);
		scaler.addTextChangedListener(textWatcher3);
		smies_sae_on.addTextChangedListener(textWatcher4);
		smies_sae_gain.addTextChangedListener(textWatcher5);
		
		set_para.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (checkBeforeSet()) {
					// call shell
					String lcdCmd = lcdTool+" "+sb1.toString()+" "+sb2.toString()+" "+sb3.toString()+" "+sb4.toString()+" "+sb5.toString();
					if(callShell(lcdCmd))
						ToastManager.showToast(getApplicationContext(), "Executed "+"\""+lcdCmd+"\"", Toast.LENGTH_SHORT);
				}
				else {
					ToastManager.showToast(getApplicationContext(), "Please Input valid parameter first!", Toast.LENGTH_SHORT);
				}
			}
			
		});

		select_txt.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ToastManager.showToast(getApplicationContext(), "Select *.txt file includes lcd parameter", Toast.LENGTH_SHORT);
				Intent intent = new Intent(getApplicationContext(), SDFileExplorerActivity.class);
				intent.putExtra("total", "lcd");
				startActivity(intent);
			}
			
		});
	}
	
	
	@Override
	protected  void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}
	


}
