package com.android.TestMode;

import java.util.ArrayList;
import java.util.List;



import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
//import com.android.internal.telephony.PhoneFactory;

public class SIMCardTestActivity extends Activity{

  private TextView txtWifiResult0 = null;
//  private TextView txtWifiResult1 = null;     //   modify by [yeez_chaomin]  20121020

  private TelephonyManager telMgr;
  private int mSimReadyCount = 0;
  public Handler mHandler = new Handler();
  public EngSqlite mEngSqlite;//[yeez_haojie add 11.27]
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
     /* int isFullTest = getIntent().getIntExtra("isFullTest", 0);
      int fullTestActivityId = getIntent().getIntExtra("fullTestActivityId", 0);
      setIsFullTest(isFullTest, ++fullTestActivityId);*/
      //setmTestCaseName("SIM test");
      setContentView(R.layout.sim_card_test);
      mEngSqlite = EngSqlite.getInstance(this);//[yeez_haojie add 11.27]
     // setTitle(R.string.sim_card_test_tittle);
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
      txtWifiResult0 = (TextView) findViewById(R.id.sim_test_list_0);
//      txtWifiResult1 = (TextView) findViewById(R.id.sim_test_list_1);    //   modify by [yeez_chaomin]  20121020
      //setTitle(R.string.sim_card_test_tittle);
      showDevice();
      
//      int phoneCount = PhoneFactory.getPhoneCount();
//     if((phoneCount == 2 && mSimReadyCount == 2) || (phoneCount == 1 && mSimReadyCount == 1)) {
		    /*if(isFullTest == 1) {
			    mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						onClick(mPassButton);
					}
				}, 1000);
		    }*/
//     }
      
      setupBottom();
      
  }

  private List<String> getResultList(int simId){
	  List<String> resultList = new ArrayList<String>();
//  ++ modify by [yeez_chaomin] 20121020
//	  telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE+simId);
	  
	 /* if (PhoneFactory.getPhoneCount() == 1) {
	      telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);   
	  } else {
	      telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE + simId);   
	  } */ 
	  
	  telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);   //  -- modify by [yeez_haojie] 20121114
	  
	  if(telMgr == null)
		  return null;
	  if(telMgr.getSimState()==TelephonyManager.SIM_STATE_READY) {
		  
		  resultList.add("fine");
		  mSimReadyCount++;
		  Button b = (Button) findViewById(R.id.testpassed);
		  b.setVisibility(View.VISIBLE);
	  }else if(telMgr.getSimState()==TelephonyManager.SIM_STATE_ABSENT) {
		  resultList.add("no SIM card");
	  }else{
		  resultList.add("locked/unknow");
	  }

	  /*
	  if(telMgr.getSimCountryIso().equals("")) {
		  resultList.add("can not get country");
	  }else{
		  resultList.add(telMgr.getSimCountryIso());
	  }

	  if(telMgr.getSimOperator().equals("")) {
		  resultList.add("can not get operator");
	  }else{
		  resultList.add(telMgr.getSimOperator());
	  }

	  if(telMgr.getSimOperatorName().equals("")) {
		  resultList.add("can not get operator name");
	  } else {
		  resultList.add(telMgr.getSimOperatorName());
	  }

	  if(telMgr.getSimSerialNumber() != null) {
		  resultList.add(telMgr.getSimSerialNumber());
	  }else{
		  resultList.add("can not get serial number");
	  }

	  if(telMgr.getSubscriberId() != null){
		  resultList.add(telMgr.getSubscriberId());
	  }else{
		  resultList.add("can not get subscriber id");
	  }

	  if(telMgr.getDeviceId() != null){
		  resultList.add(telMgr.getDeviceId());
	  }else{
		  resultList.add("can not get device id");
	  }

	  if(telMgr.getLine1Number() != null){
		  resultList.add(telMgr.getLine1Number());
	  }else{
		  resultList.add("can not get phone number");
	  }

	  if(telMgr.getPhoneType() == 0){
		  resultList.add("NONE");
	  }else if(telMgr.getPhoneType() == 1){
		  resultList.add("GSM");
	  }else if(telMgr.getPhoneType() == 2){
		  resultList.add("CDMA");
	  }else if(telMgr.getPhoneType() == 3){
		  resultList.add("SIP");
	  }

	  if(telMgr.getDataState() == 0){
		  resultList.add("disconnected");
	  }else if(telMgr.getDataState() == 1){
		  resultList.add("connecting");
	  }else if(telMgr.getDataState() == 2){
		  resultList.add("connected");
	  }else if(telMgr.getDataState() == 3){
		  resultList.add("suspended");
	  }

	  if(telMgr.getDataActivity() == 0){
		  resultList.add("none");
	  }else if(telMgr.getDataActivity() == 1){
		  resultList.add("in");
	  }else if(telMgr.getDataActivity() == 2){
		  resultList.add("out");
	  }else if(telMgr.getDataActivity() == 3){
		  resultList.add("in/out");
	  }else if(telMgr.getDataActivity() == 4){
		  resultList.add("dormant");
	  }

	  if(!telMgr.getNetworkCountryIso().equals("")){
		  resultList.add(telMgr.getNetworkCountryIso());
	  }else{
		  resultList.add("can not get network country");
	  }

	  if(!telMgr.getNetworkOperator().equals("")){
		  resultList.add(telMgr.getNetworkOperator());
	  }else{
		  resultList.add("can not get network operator");
	  }

	  if(telMgr.getNetworkType() == 0){
		  resultList.add("unknow");
	  }else if(telMgr.getNetworkType() == 1){
		  resultList.add("gprs");
	  }else if(telMgr.getNetworkType() == 2){
		  resultList.add("edge");
	  }else if(telMgr.getNetworkType() == 3){
		  resultList.add("umts");
	  }else if(telMgr.getNetworkType() == 4){
		  resultList.add("hsdpa");
	  }else if(telMgr.getNetworkType() == 5){
		  resultList.add("hsupa");
	  }else if(telMgr.getNetworkType() == 6){
		  resultList.add("hspa");
	  }else if(telMgr.getNetworkType() == 7){
		  resultList.add("cdma");
	  }else if(telMgr.getNetworkType() == 8){
		  resultList.add("evdo 0");
	  }else if(telMgr.getNetworkType() == 9){
		  resultList.add("evdo a");
	  }else if(telMgr.getNetworkType() == 10){
		  resultList.add("evdo b");
	  }else if(telMgr.getNetworkType() == 11){
		  resultList.add("1xrtt");
	  }else if(telMgr.getNetworkType() == 12){
		  resultList.add("iden");
	  }else if(telMgr.getNetworkType() == 13){
		  resultList.add("lte");
	  }else if(telMgr.getNetworkType() == 14){
		  resultList.add("ehrpd");
	  }else if(telMgr.getNetworkType() == 15){
		  resultList.add("hspap");
	  }*/ //[yeez_haojie modify 12.17]
	  
	  return resultList;
  }
    private List<String> getKeyList(){
  	    List<String> keyList = new ArrayList<String>();
  	    keyList.add("SIM State:  ");
  	   /* keyList.add("Sim Country:  ");
  	    keyList.add("Sim Operator:  ");
  	    keyList.add("Sim Operator Name:  ");
  	    keyList.add("Sim Serial Number:  ");
  	    keyList.add("Subscriber Id:  ");
  	    keyList.add("Device Id:  ");
  	    keyList.add("Line 1 Number:  ");
  	    keyList.add("Phone Type:  ");
  	    keyList.add("Data State:  ");
  	    keyList.add("Data Activity:  ");
  	    keyList.add("Network Country:  ");
  	    keyList.add("Network Operator:  ");
  	    keyList.add("Network Type:  ");*/ //[yeez_haojie modify 12.17]
  	    return keyList;
    }

    private void showDevice() {

      //TextView txtList0 = (TextView) findViewById(R.id.sim_test_list_0);//[yeez_haojie modify 12.17]
      //TextView txtList1 = (TextView) findViewById(R.id.sim_test_list_1);   //   modify by [yeez_chaomin]  20121020
      //txtList0.setText("");//[yeez_haojie modify 12.17]
      txtWifiResult0.setText("");//[yeez_haojie add 12.17]
      //txtList1.setText("");   //   modify by [yeez_chaomin]  20121020
      List<String> keyList = getKeyList();
      List<String> resultList0= getResultList(0);
      //List<String> resultList1= getResultList(1);   //   modify by [yeez_chaomin]  20121020
      
      String resulttext = resultList0.get(0);
      int lenth = resulttext.length();
      Log.v("SIMCardTestActivity", "resulttext :"+resulttext+" lenth :"+lenth);
      SpannableStringBuilder style = new SpannableStringBuilder(resulttext);//[yeez_haojie add 12.17]
      style.setSpan(new ForegroundColorSpan(Color.GREEN), 0, lenth, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
       if(resultList0 != null && keyList.size()==1)//[yeez_haojie add 12.17]
	  {
    	   txtWifiResult0.append(keyList.get(0)+style+"\n");
	  }
      
     /* for (int i = 0; i < 14; i++) {   	      	  
    	  
    	  if(resultList0 != null)
    		  txtList0.append(keyList.get(i)+resultList0.get(i)+"\n");    	  
    	  else
    		  txtList0.append(keyList.get(i)+"\n");
      }*/  //[yeez_haojie modfiy 12.17]
      
      
      
 //   ++ modify by [yeez_chaomin]  20121020
      //for (int i = 0; i < 14; i++) {
    	//  if(resultList1 != null)
    	//	  txtList1.append(keyList.get(i)+resultList1.get(i)+"\n");
    	//  else
    	//	  txtList1.append(keyList.get(i)+"\n");
      //}
 //   -- modify by [yeez_chaomin]  20121020
  }


    private void setupBottom() {
        Button b = (Button) findViewById(R.id.testpassed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //passed
               setResult(RESULT_OK);
               BaseActivity.NewstoreRusult(true, "Sim Card test",mEngSqlite);//[yeez_haojie add 11.27]
               finish();
            }
        });
       
        b = (Button) findViewById(R.id.testfailed);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               //failed
               setResult(RESULT_FIRST_USER);
               BaseActivity.NewstoreRusult(false, "Sim Card test",mEngSqlite);//[yeez_haojie modify 12.21]
               finish();
            }
        });
     }
}