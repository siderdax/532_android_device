package com.android.TestMode;

public class GridViewItems {

    public int iconID;
    public String textTitle;
    public int itemID;

    public String testID;
    public String testCase;
    public int testResult;


    public GridViewItems(){

    }
    public GridViewItems(int id, String title, int itemID){
        this.iconID = id;
        this.textTitle = title;
        this.itemID = itemID;
    }

    public GridViewItems(String testID,String testCase,int testResult){
        this.testID = testID;
        this.testCase = testCase;
        this.testResult = testResult;
    }
    public void setTestResult(int testResult){
    	this.testResult = testResult;
    }
}
