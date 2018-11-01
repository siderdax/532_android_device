package com.android.TestMode;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

public class SDCardTestActivity extends Activity {
	  static final int FINISH_TEST = 1;
	  
	  private String PATH_MEMORY_DIR;       
//	  private String PATH_SDCARD_DIR;     
	  
	  private StorageManager mStorageManager = null;
	  public EngSqlite mEngSqlite;
      private final Timer timer = new Timer();
                private TimerTask task;
                Handler handler = new Handler() {
                         public void handleMessage(Message msg) {
                                  super.handleMessage(msg);
                                  if(msg.what == FINISH_TEST)
                                  {
                                            new AlertDialog.Builder(SDCardTestActivity.this)
                                            .setTitle(R.string.sdcard_test)
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
							        				if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {	
							        					
							        					return true;
							        		         }
							        		         return false;
							        			}
							        		}).show();
                                            
                                  }
                                  
                         }
                };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.xml.sdcard);
		
		 mEngSqlite = EngSqlite.getInstance(this);
		TextView sdcardView = (TextView) findViewById(R.id.SDCardTestView);
		
//		TextView sdcard8012View = (TextView) findViewById(R.id.SDCard2TestView);
		
		if (mStorageManager == null) {
            mStorageManager = (StorageManager) getSystemService(SDCardTestActivity.this.STORAGE_SERVICE);
            //mStorageManager.registerListener(mStorageListener);
        }
		StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
		 int length = storageVolumes.length;
		 Log.v("sd_test", "length :"+length);
		 boolean Storage1 = storageVolumes[0].allowMassStorage();
//		 boolean Storage2 = storageVolumes[1].allowMassStorage();
//		 boolean Storage3 = storageVolumes[2].allowMassStorage();
		// boolean Storage4 = storageVolumes[3].allowMassStorage();
//		String newsdcard2 =  "/sys/devices/platform/s3c-sdhci.2/mmc_host/mmc1/mmc1:0001";//[yeez_haojie add 11.16]
		 
		String sDStateString = Environment.getExternalStorageState();
		if(sDStateString == null)
		{
			sdcardView.append(getString(R.string.sdcard_size) 
					+ "0 MB");
		}
		else
		{
		
		String sd8012 = "/mnt/sdcard2";//8012/mnt/sdcard2  //[yeez_haojie add 11.13]
		
		if (Environment.MEDIA_MOUNTED.equals(sDStateString)) {
			File pathFile = Environment.getExternalStorageDirectory();
			StatFs statfs = new StatFs(pathFile.getPath());
			long totalBlocks = statfs.getBlockCount();
			long blockSize = statfs.getBlockSize();
			long SDCardTotalSize = totalBlocks * blockSize / 1024 / 1024;
			sdcardView.append(getString(R.string.sdcard_size) + SDCardTotalSize
					+ " MB");
			sdcardView.append("\n");
			Button b = (Button) findViewById(R.id.testpassed);
			b.setVisibility(View.VISIBLE);
		} 
		
		/* String sD2StateString = Environment.getExternalStorageStateSd();
		
		if (Environment.MEDIA_MOUNTED.equals(sD2StateString)) {
			File pathFile = Environment.getExternalStorageDirectorySd();
			StatFs statfs = new StatFs(pathFile.getPath());
			long totalBlocks = statfs.getBlockCount();
			long blockSize = statfs.getBlockSize();
			long SDCardTotalSize = totalBlocks * blockSize / 1024 / 1024;
			sdcardView.append(getString(R.string.sdcard2_size) + SDCardTotalSize
					+ " MB");
			Button b = (Button) findViewById(R.id.testpassed);
			b.setVisibility(View.VISIBLE);
			
			sdcard8012View.append(getString(R.string.sdcard2_exit));
		} 
		else {
			sdcard8012View.append(getString(R.string.no_sdcard2));
		} */
		}

		setupBottom();
		init();
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
               BaseActivity.NewstoreRusult(true, "Sd and Memory test",mEngSqlite);
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "Sd and Memory test",mEngSqlite);
               finish();
            }
        });
     }
	  
	  private void init(){  
	        PATH_MEMORY_DIR = getFilesDir().getAbsolutePath() + File.separator ;
	     
//	        PATH_SDCARD_DIR = Environment.getExternalStorageDirectorySd().getAbsolutePath();     
	    }  
}
