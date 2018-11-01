package com.android.TestMode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


import com.android.TestMode.Const;
import com.android.TestMode.GridViewItems;

//import dalvik.system.VMRuntime;

public class EngSqlite {
    private static final String TAG = "EngSqlite";
    private Context mContext;
    private SQLiteDatabase mSqLiteDatabase = null;
//    private ArrayList<GridViewItems> mResultListItem;


    private static EngSqlite mEngSqlite;
    public static synchronized EngSqlite getInstance(Context context){
    	if(mEngSqlite == null){
    		mEngSqlite = new EngSqlite(context);
    	}
    	return mEngSqlite;
    }
    private  EngSqlite(Context context){
        mContext = context;
//        File file = new File(Const.ENG_ENGTEST_DB);
//        if (!file.canWrite()) {
//            Process p = null;
//            DataOutputStream os = null;
//            try {
////                p = Runtime.getRuntime().exec("su");
//
////                os.writeBytes("chmod 777 "+file.getAbsolutePath()+"\n");
////                os.writeBytes("exit\n");
////                os.flush();
//                p = Runtime.getRuntime().exec("chmod 777 unit");
////                Runtime.getRuntime().exec("chmod -r 777 /data/data/");
//                os = new DataOutputStream(p.getOutputStream());
//                BufferedInputStream err = new BufferedInputStream(p.getErrorStream());
//                BufferedReader br = new BufferedReader(new InputStreamReader(err));
//                Log.v("Vtools","os= "+br.readLine());
//                Runtime.getRuntime().exec("chmod 777 "+file.getAbsolutePath());
//                int status = p.waitFor();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }finally{
//                if (os != null) {
//                    try {
//                        os.close();
//                        p.destroy();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
        ValidationToolsDatabaseHelper databaseHelper = new ValidationToolsDatabaseHelper(mContext);
        mSqLiteDatabase = databaseHelper.getWritableDatabase();
       // mSqLiteDatabase = mContext.openOrCreateDatabase(Const.ENG_ENGTEST_DB, Context.MODE_PRIVATE, null);
    }

    public ArrayList<GridViewItems> queryData(){
        Cursor cur = mSqLiteDatabase.query(Const.ENG_STRING2INT_TABLE, new String[]{"name","value"},
                                           null,null,
                                           null, null, null);
        int count = cur.getCount();
        ArrayList<GridViewItems> resultListItem =new ArrayList<GridViewItems>(count);
        cur.moveToFirst();
        for (int i = 0; i < count; i++) {
           GridViewItems item = new GridViewItems();
           item.testID = String.valueOf(i+1);
           item.testCase = cur.getString(0);
           item.testResult = cur.getInt(1);
           if(item.testCase != null && item.testCase.trim().equals("") == false && item.testCase.trim().equals("result") == false)
        	   resultListItem.add(item);
           cur.moveToNext();
        }
        cur.close();
      // mSqLiteDatabase.close();
       return resultListItem;
    }

    public int  getTestGridItemStatus(int groupId){
		Cursor cursor = mSqLiteDatabase.query(Const.ENG_STRING2INT_TABLE,
				new String[] { "value" }, "groupId=" + groupId, null, null,
				null, null);
		Log.d(TAG,"value-groupId" + groupId);
		Log.d(TAG,"cursor.count" + cursor.getCount());
		try {
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					if (cursor.getInt(0) == Const.FAIL) {
						return Const.FAIL;
					}
				}
               return Const.SUCCESS;
			} else {
				Log.d(TAG,"cursor.count");
				return Const.DEFAULT;
			}
		} catch (Exception ex) {
			Log.d(TAG,"exception");
			return Const.DEFAULT;
		} finally {
			Log.d(TAG,"fianlly");
			if (cursor != null) {
				cursor.close();
			}
		}
    }


    public int  getTestListItemStatus(String  name){
		Cursor cursor = mSqLiteDatabase.query(Const.ENG_STRING2INT_TABLE,
				new String[] { "value" }, "name=" + "\'" + name + "\'", null, null,
				null, null);
		Log.d(TAG,"name" + name);
		Log.d(TAG,"cursor.count" + cursor.getCount());
		try {
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					if (cursor.getInt(0) == Const.FAIL) {
						return Const.FAIL;
					}
				}
               return Const.SUCCESS;
			} else {
				Log.d(TAG,"cursor.count");
				return Const.DEFAULT;
			}
		} catch (Exception ex) {
			Log.d(TAG,"exception");
			return Const.DEFAULT;
		} finally {
			Log.d(TAG,"fianlly");
			if (cursor != null) {
				cursor.close();
			}
		}
    }

    public void inSertData(String name,String value){
       ContentValues cv = new ContentValues();
       cv.put(Const.ENG_STRING2INT_NAME,name);
       cv.put(Const.ENG_STRING2INT_VALUE,value);
       long returnValue= mSqLiteDatabase.insert(Const.ENG_STRING2INT_TABLE, null, cv);
       if (returnValue == -1) {
        Log.e(TAG, "insert DB error!");
    }
       mSqLiteDatabase.close();
    }

    public void updateData(String name,int value){
        ContentValues cv = new ContentValues();
        cv.put(Const.ENG_STRING2INT_NAME,name);
        cv.put(Const.ENG_STRING2INT_VALUE,value);
        mSqLiteDatabase.update(Const.ENG_STRING2INT_TABLE, cv,
                Const.ENG_STRING2INT_NAME+"= \'"+name+"\'", null);
     //   mSqLiteDatabase.close();
    }


    public void updateDB(int groupId,String name,int value){
        if (queryDate(name)) {
            updateData(name,value);
        }else {
            inSertData(groupId,name, value);
        }
    }
    
    /**
     * [yeez_haojie add 11.22]
     * @param groupId
     * @param name
     * @param value
     */
    public void updateDB8012(String name,int value){
    	Log.v(TAG, "updateDB8012  name :"+name);
    	Log.v(TAG, "updateDB8012  value :"+value);
    	if (queryDate(name)) {
            updateData(name,value);
        }else {
        	inSertData8012(name, value);
        }
           
       
    }
    
    /**
     * [yeez_haojie add 11.22]
     * @param name
     * @param value
     */
    public void inSertData8012(String name,int  value){
        ContentValues cv = new ContentValues();
        cv.put(Const.ENG_STRING2INT_NAME,name);
        cv.put(Const.ENG_STRING2INT_VALUE,value);
        //cv.put(Const.ENG_GROUPID_VALUE, groupId);

        if(Const.DEBUG){
     	   Log.d(TAG,"name" + name + "value:" + value );
        }

        long returnValue= mSqLiteDatabase.insert(Const.ENG_STRING2INT_TABLE, null, cv);
        Log.e(TAG, "returnValue" + returnValue);
        if (returnValue == -1) {
         Log.e(TAG, "insert DB error!");
        }
        //mSqLiteDatabase.close();
     }
    
    public void inSertData(int groupId,String name,int  value){
       ContentValues cv = new ContentValues();
       cv.put(Const.ENG_STRING2INT_NAME,name);
       cv.put(Const.ENG_STRING2INT_VALUE,value);
       cv.put(Const.ENG_GROUPID_VALUE, groupId);

       if(Const.DEBUG){
    	   Log.d(TAG,"name" + name + "value:" + value + "groupId" + groupId);
       }

       long returnValue= mSqLiteDatabase.insert(Const.ENG_STRING2INT_TABLE, null, cv);
       Log.e(TAG, "returnValue" + returnValue);
       if (returnValue == -1) {
        Log.e(TAG, "insert DB error!");
       }
       //mSqLiteDatabase.close();
    }

    public boolean queryDate(String name){
        try {
            Cursor c = mSqLiteDatabase.query(Const.ENG_STRING2INT_TABLE,
                    new String[]{Const.ENG_STRING2INT_NAME,Const.ENG_STRING2INT_VALUE},
                    Const.ENG_STRING2INT_NAME+"= \'"+name+"\'",
                    null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                	c.close();
                    return true;
                }
            	c.close();
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    private static final int ERROR = 99;
    private static final int NO_TEST = 98;
    public int queryFailCount(){
    	Cursor cursor = mSqLiteDatabase.query(Const.ENG_STRING2INT_TABLE, null, null, null, null, null, null);
    	if(cursor.getCount() < testCount()){
    		cursor.close();
    		return NO_TEST;
    	}
        int bln = 0;
        if(mSqLiteDatabase == null)
            return ERROR;
        Cursor cur = mSqLiteDatabase.query(Const.ENG_STRING2INT_TABLE, new String[]{"name","value"},
                                           "value=?",new String[]{"0"},
                                           null, null, null);
        if(cur!=null && cur.getCount() >= 1 ){
            bln = cur.getCount();
        }
        cur.close();
        return bln;
    }
    
    private int testCount() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(TestModeActivity.CATEGORY_PCBATEST);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        if(list != null ) {
        	return list.size();
        }
        return 0;
    }

    private static class ValidationToolsDatabaseHelper extends SQLiteOpenHelper{

		public ValidationToolsDatabaseHelper(Context context) {
			super(context, Const.ENG_ENGTEST_DB, null, Const.ENG_ENGTEST_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + Const.ENG_STRING2INT_TABLE + ";");
			db.execSQL("CREATE TABLE " + Const.ENG_STRING2INT_TABLE + " (" + BaseColumns._ID
	                + " INTEGER PRIMARY KEY AUTOINCREMENT," + Const.ENG_GROUPID_VALUE + " INTEGER NOT NULL DEFAULT 0,"
	                + Const.ENG_STRING2INT_NAME + " TEXT," + Const.ENG_STRING2INT_VALUE + " INTEGER NOT NULL DEFAULT 0" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			if(newVersion > oldVersion){
				db.execSQL("DROP TABLE IF EXISTS " + Const.ENG_STRING2INT_TABLE + ";");
				onCreate(db);
			}
		}

    }
}
