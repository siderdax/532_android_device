package com.android.TestMode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class TmuSliderView extends View {
	
	private String TAG = "TmuSliderView";
//	public static final int FLING_LEFT = 0;
//	public static final int FLING_RIGHT = 1;
	private Paint paint;
	private int viewWidth;
	private int viewHeight;
	private int index; // current page, [1, N]
	private int N; // number of pages
	private int radius;
	private int distance;
	private MyReceiver receiver;
	private Context context;
	private Canvas canvas;
	private MyApp app;
	private TmuBrain tmuBrain;
	
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.e(TAG, "MyReceiver: onReceive");
			// TODO Auto-generated method stub
//			String msg = intent.getStringExtra("direction");
//			Log.e(TAG, "msg of BroadcastReceiver is "+msg);
//			if (msg.equals("left")) {
//				handleGesture(TmuBrain.FLING_LEFT);
//			} else if (msg.equals("right")) {
//				handleGesture(TmuBrain.FLING_RIGHT);
//			}
			String msg = intent.getStringExtra("cmd");
			Log.e(TAG, "msg of MyReceiver is "+msg);
			if (msg.equals("draw")) {
				updateSliderView();
			}
		}
	}

	public TmuSliderView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public TmuSliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.e(TAG, "TmuSliderView");
		this.context = context;
		paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAlpha(50); //0-ff
		paint.setAntiAlias(true);
		Activity activity = (Activity)context;
		MyApp app = (MyApp) activity.getApplication();
		tmuBrain = app.getTmuBrain();
		N = tmuBrain.getSum(); // must give odd number
		index = tmuBrain.getIndex(); // default page is the first page
	}
	
	@Override
	protected void onAttachedToWindow() {
		Log.e(TAG, "onAttacheToWindow");
		// singleton
		if (receiver == null) {
			receiver = new MyReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("com.android.TestMode.TmuTunningActivity"); // sent from whom
			context.registerReceiver(receiver, filter);
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		Log.e(TAG, "onDetachedFromWindow");
		if (receiver != null)
			context.unregisterReceiver(receiver);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.e(TAG, "onMeasure");
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);		
		Log.e(TAG, "onLayout");

	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);	
		Log.e(TAG, "onDraw");
		
		viewWidth = getMeasuredWidth();
		viewHeight= getMeasuredHeight();
		//canvas.drawColor(Color.WHITE);
		index = tmuBrain.getIndex();
		drawCircles(N, index, canvas);
	}

	//index: [1,N]
	private void drawCircles(int N, int index, Canvas canvas) {
		Log.e(TAG, "drawCircles, N="+N+", index="+index);
		//ToastManager.showToast(context, "index="+index, Toast.LENGTH_SHORT);
		this.canvas = canvas;
		
		if (N % 2 == 0) {
			Log.e(TAG, "Slider not support even number");
			return;
		}
		radius = viewHeight / 4;
		distance = radius * 6;
		
		// The X,Y is to the origin of View itself
		int centerX = viewWidth / 2;
		int centerY = viewHeight / 2;
		
		//gerneal draw
		for (int i=0; i<=(N-1)/2; i++) {
			// draw N circles
			if (i == 0) {
				canvas.drawCircle(centerX, centerY, radius, paint);
			}
			else {
				canvas.drawCircle(centerX + i*distance, centerY, radius, paint);
				canvas.drawCircle(centerX - i*distance, centerY, radius, paint);
			}
		}
		
		// special draw
		// make the its color deeper than others
		/* X of circle:
		 * index=1 + (N-1)/2, X=centerX=centerX + 0*distance
		 * index=(N-1)/2, X=centerX - distance=centerX + (-1)*distance
		 * index=2 + (N-1)/2, X=centerX + distance=centerX + 1*distance
		 * we can refer that: if index=1, X=centerX + (-(N-1)/2)*distance
		 * we can refer that: if index=index, X=centerX + (index-1-(N-1)/2)*distance
		 * The rule always is: index - X = 1 + (N-1)/2
		 */
		int alpha = paint.getAlpha();
		paint.setAlpha(125);
		//paint.setColor(Color.BLUE);
		canvas.drawCircle(centerX + (index - 1 - (N-1)/2)*distance, centerY, radius, paint);
		paint.setAlpha(50);
		//paint.setColor(Color.WHITE);
		
	}
	
	//private void handleGesture(int direction) { // the strategy should be extracted out
		//Log.e(TAG, "handleGesture, direction="+direction);
	public void updateSliderView() {
		Log.e(TAG, "updateSliderView");
		//tmuBrain.updateIndexByGesture(direction);
		index = tmuBrain.getIndex();
		//drawCircles(N, index, canvas);
		invalidate();
//		switch (direction) {
//		case FLING_LEFT:
//			if (index > 1) {
//				drawCircles(N, --index, canvas);
//				invalidate();
//			}
//			break;
//		case FLING_RIGHT:
//			if (index < N) {
//				drawCircles(N, ++index, canvas);
//				invalidate();
//			}
//			break;
//		default:
//			break;
//		}
	}

}
