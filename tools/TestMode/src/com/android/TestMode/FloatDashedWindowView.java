package com.android.TestMode;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class FloatDashedWindowView extends LinearLayout {
	
	private String TAG = "FloatDashedWindowView";

	private WindowManager windowManager;
	private FrameLayout floatWindowLayout;
	
    public int viewWidth;
    public int viewHeight;
	
	public FloatDashedWindowView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater infalter = LayoutInflater.from(context);
		infalter.inflate(R.layout.float_dashed_window, this);
		floatWindowLayout = (FrameLayout) findViewById(R.id.float_window_layout);
		viewWidth = floatWindowLayout.getLayoutParams().width;
		viewHeight = floatWindowLayout.getLayoutParams().height;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.e(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.e(TAG, "onLayout");
		super.onLayout(changed, l, t, r, b);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Log.e(TAG, "onDraw");
		super.onDraw(canvas);
		
	}
}
