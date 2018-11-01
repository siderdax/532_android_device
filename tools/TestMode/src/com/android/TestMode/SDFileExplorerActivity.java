package com.android.TestMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.media.AudioManager;
import android.content.Intent;
import android.content.Context;

import org.apache.http.util.EncodingUtils;


public class SDFileExplorerActivity extends Activity {
	
	private static final String TAG = "SDFileExplorerActivity";
	
	private static final String lcdDstPath = "/data/lcd_debug.txt";
	
	private ListView listView;
	private TextView textView;
	private File currentParentDir; 
	private File[] currentFiles;
	
	private AudioManager audioManager;
	
	private String getExtensionName(String filename) {
		if ((filename != null) && (filename.length() > 0)) {
			int dot = filename.lastIndexOf(".");
			if ((dot > -1) && (dot < (filename.length() -1))) {
				return filename.substring(dot + 1);
			}
		}
		return filename;
	}
	
	private boolean copyFile(String fileFrom, String fileTo) {
		try {
			FileInputStream in = new FileInputStream(fileFrom);
			FileOutputStream out = new FileOutputStream(fileTo);
			byte[] buf = new byte[1024];
			int count;
			while ((count = in.read(buf)) > 0) {
				out.write(buf, 0, count);
			}
			in.close();
			out.close();
			ToastManager.showToast(SDFileExplorerActivity.this, "The txt file is copied into "+lcdDstPath, Toast.LENGTH_SHORT);
			return true;
		} catch (IOException e) {
			ToastManager.showToast(SDFileExplorerActivity.this, "An error occurs while copying file", Toast.LENGTH_SHORT);
			return false;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_explorer);
		listView = (ListView)findViewById(R.id.list);
		textView = (TextView)findViewById(R.id.path);
		
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				
		File root = new File("/mnt/sdcard/");
		if (root.exists()) {
			Log.d(TAG, "/mnt/sdcard exists");
			currentParentDir = root;
			currentFiles = root.listFiles();
			// make it happen
			inflateListView(currentFiles);
		}
		else {
			Log.d(TAG, "ERROR: /mnt/sdcard NOT exists!");
		}
		
		Intent intent = getIntent();
		String extra = intent.getStringExtra("total");
		Log.d(TAG, "extra="+extra);
		// lcd tunning
		if (extra != null && extra.equals("lcd")) {
			Log.d(TAG, "from lcd tunning");
			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					File clickedFile = currentFiles[arg2];
					if (clickedFile.isFile()) {
						Log.d(TAG, "filename="+clickedFile.getName()+",extname="+getExtensionName(clickedFile.getName()));
						if (!getExtensionName(clickedFile.getName()).equals("txt")) {
							ToastManager.showToast(SDFileExplorerActivity.this, "Not txt file, please choose again!", Toast.LENGTH_SHORT);
						}
						else {
							//ToastManager.showToast(SDFileExplorerActivity.this, "The txt file is copied as /data/lcd_debug.txt", Toast.LENGTH_SHORT);
							File lcdDstFile = new File(lcdDstPath);
							if (!lcdDstFile.exists()) {
								try {
									lcdDstFile.createNewFile();
								} catch (IOException e) {
									ToastManager.showToast(SDFileExplorerActivity.this, "An error occurs while creating "+lcdDstPath, Toast.LENGTH_SHORT);
								}
							}
							else {
								String fileFrom = "";
								try {
									fileFrom = clickedFile.getCanonicalPath();
								} catch (IOException e) {
									ToastManager.showToast(SDFileExplorerActivity.this, "An error occurs while getting src file name", Toast.LENGTH_SHORT);
								}
								if (fileFrom.length() != 0)
									copyFile(fileFrom, lcdDstPath);
								else {
									ToastManager.showToast(SDFileExplorerActivity.this, "An error occurs that the copied file name is empty", Toast.LENGTH_SHORT);
								}
							}
						}
					}
					// it's Direcotry
					else {
						// dir clicked
						File[] listedFiles = clickedFile.listFiles();
		//				if (tmp == null || tmp.length == 0) {
		//					ToastManager.showToast(SDFileExplorerActivity.this, "当前路径不可访问或该路径下没有文件", Toast.LENGTH_SHORT);
		//				}
						if (listedFiles == null) {
							ToastManager.showToast(SDFileExplorerActivity.this, "当前路径不可访问", Toast.LENGTH_SHORT);
						}
						else if (listedFiles.length == 0) {
							currentParentDir = clickedFile;
							currentFiles = listedFiles;
							inflateListView(currentFiles);
							ToastManager.showToast(SDFileExplorerActivity.this, "该目录下没有文件", Toast.LENGTH_SHORT);
						}
						else {
							currentParentDir = clickedFile;
							currentFiles = listedFiles;
							inflateListView(currentFiles);
						}
					}
				}
			});
			
			Button goParent = (Button)findViewById(R.id.go_parent);
			goParent.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						if (!currentParentDir.getCanonicalPath().equals("/mnt/sdcard")) {
							currentParentDir = currentParentDir.getParentFile();
							currentFiles = currentParentDir.listFiles();
							inflateListView(currentFiles);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		// audio tunning
		else {
			
			listView.setOnItemClickListener(new OnItemClickListener() {
				// arg2: position
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					int path1, path2, path3, path4;
					// it's file
					File clickedFile = currentFiles[arg2];
					if (clickedFile.isFile()) {
						Log.d(TAG, "filename="+clickedFile.getName()+",extname="+getExtensionName(clickedFile.getName()));
						if (!getExtensionName(clickedFile.getName()).equals("flt")) {
							ToastManager.showToast(SDFileExplorerActivity.this, "所选文件的后缀名不是flt，请重命名或者选择其它文件", Toast.LENGTH_SHORT);
						}
						else {
							ToastManager.showToast(SDFileExplorerActivity.this, "所选文件后缀名为flt，已读取并设置了滤波器参数", Toast.LENGTH_SHORT);
							try {
								FileInputStream fin = new FileInputStream(clickedFile.getCanonicalPath());
								int length = fin.available();
								byte[] buffer = new byte[length];
								fin.read(buffer);
								String res = EncodingUtils.getString(buffer, "UTF-8");
								fin.close();
								Log.d(TAG, "*.flt file content="+res);
								Intent intent = getIntent();
								int total = Integer.parseInt(intent.getStringExtra("total"));
								switch (total) {
								// must be in playback scenario or fm scenario
								case 1:
									path1 = Integer.parseInt(intent.getStringExtra("path1"));
									audioManager.setParameters("path="+path1+","+res);
									break;
								case 4: // must be in voice call scenario
									path1 = Integer.parseInt(intent.getStringExtra("path1"));
									path2 = Integer.parseInt(intent.getStringExtra("path2"));
									path3 = Integer.parseInt(intent.getStringExtra("path3"));
									path4 = Integer.parseInt(intent.getStringExtra("path4"));
									audioManager.setParameters("path="+path1+","+res);
									audioManager.setParameters("path="+path2+","+res);
									audioManager.setParameters("path="+path3+","+res);
									audioManager.setParameters("path="+path4+""+res);
									break;
								default:
									Log.e(TAG, "total="+total+" ,not supported, only support 1 or 4");
									return;
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
							// close file explorer
							finish();
						}
					}
					// it's Direcotry
					else {
						// dir clicked
						File[] listedFiles = clickedFile.listFiles();
		//				if (tmp == null || tmp.length == 0) {
		//					ToastManager.showToast(SDFileExplorerActivity.this, "当前路径不可访问或该路径下没有文件", Toast.LENGTH_SHORT);
		//				}
						if (listedFiles == null) {
							ToastManager.showToast(SDFileExplorerActivity.this, "当前路径不可访问", Toast.LENGTH_SHORT);
						}
						else if (listedFiles.length == 0) {
							currentParentDir = clickedFile;
							currentFiles = listedFiles;
							inflateListView(currentFiles);
							ToastManager.showToast(SDFileExplorerActivity.this, "该目录下没有文件", Toast.LENGTH_SHORT);
						}
						else {
							currentParentDir = clickedFile;
							currentFiles = listedFiles;
							inflateListView(currentFiles);
						}
					}
				}
			});
			
			Button goParent = (Button)findViewById(R.id.go_parent);
			goParent.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					try {
						if (!currentParentDir.getCanonicalPath().equals("/mnt/sdcard")) {
							currentParentDir = currentParentDir.getParentFile();
							currentFiles = currentParentDir.listFiles();
							inflateListView(currentFiles);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
	
	private void inflateListView(File[] files) {
		List<Map<String,Object>> listItems = new ArrayList<Map<String,Object>>();
		for (int i=0; i<files.length; i++) {
			Map<String,Object> listItem = new HashMap<String,Object>();
			if (files[i].isDirectory()) {
				listItem.put("icon", R.drawable.folder);
			}
			else {
				listItem.put("icon", R.drawable.file);
			}
			listItem.put("fileName", files[i].getName());
			listItems.add(listItem);
		}
		
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listItems, R.layout.line, new String[]{"icon", "fileName"}, new int[]{R.id.icon, R.id.file_name});
		listView.setAdapter(simpleAdapter);
		try {
			textView.setText("当前路径为："+currentParentDir.getCanonicalPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
