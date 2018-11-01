package com.android.TestMode;

import java.util.ArrayList;

import com.android.TestMode.EngSqlite;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TestResultActivity extends Activity{
private Context mContext;
public TextView mTestResultView;
private EngSqlite mEngSqlite;
//private View viewRow[] = new View[28];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_result);
        mContext = this;
        mEngSqlite = EngSqlite.getInstance(mContext);
        ListView listView = (ListView)findViewById(R.id.listview_layout);
        ListAdapter listAdapter = new ListAdapter(mContext,mEngSqlite.queryData());
        listView.setAdapter(listAdapter);
        mTestResultView = (TextView)findViewById(R.id.test_result_text);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        int failCount = mEngSqlite.queryFailCount();
            if(failCount >= 1){
                mTestResultView.setText(getResources().getString(R.string.TestResultTitleStringFail));
                mTestResultView.setTextColor(Color.RED);
            }
        super.onResume();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }


    public final class ViewHolder {
        public TextView textID;
        public TextView textCase;
        public TextView textResult;
    }

    private class ListAdapter extends BaseAdapter{
        private Context mContext;
        private LayoutInflater mLayoutInflater;
        private ViewHolder mViewHolder;
        private ArrayList<GridViewItems> mItems;

        public ListAdapter(Context context,ArrayList<GridViewItems> items){
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mViewHolder = new ViewHolder();
            mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.table_row_layout, parent,false);
                mViewHolder.textID = (TextView)convertView.findViewById(R.id.id_text);
                mViewHolder.textCase = (TextView)convertView.findViewById(R.id.test_case);
                mViewHolder.textResult = (TextView)convertView.findViewById(R.id.test_result);
                mViewHolder.textID.setText(String.valueOf(position+1));
                mViewHolder.textCase.setText( mItems.get(position).testCase);
                if (mItems.get(position).testResult == Const.FAIL) {
                    mViewHolder.textResult.setText(getResources().getString(R.string.text_fail));
                    mViewHolder.textResult.setTextColor(Color.RED);
                    mTestResultView.setText(getResources().getString(R.string.TestResultTitleStringFail));
                    mTestResultView.setTextColor(Color.RED);
                }else if(mItems.get(position).testResult == Const.SUCCESS){
                    mViewHolder.textResult.setText(getResources().getString(R.string.text_pass));
                }
            return convertView;
        }
    }

}
