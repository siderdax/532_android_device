package com.android.TestMode;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.view.MotionEvent;
import android.view.View;
// add by [yeez_liuwei] 2013.7.18
public class TouchTestView extends View {
	public Bitmap mBitmap;
	public Paint mRoadPaint,mLinePaint,mMovePaint,mCirclePaint,mSuccessPaint;
	private Canvas mCanvas;
	private float mX,mY;
	private static final int ROAD_WIDTH = 80; 
	private static final int HAFT_ROAD = ROAD_WIDTH/2;
	private static final int STROKE = 10;
	private static final int HAFT_STROKE = STROKE/2;
	private static final float RADIUS = 40;
	private int mStartPoint;
	private List<Integer> mSuccess;
	private SuccessListener mListener;
	private int mWidth = 0;
	private int mHeight = 0;
	
	public TouchTestView(Context context, int width, int height) {
		super(context);
		mWidth = width;
		mHeight = height;
		prepareCanvans();
		mRoadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRoadPaint.setColor(Color.BLACK);
		mRoadPaint.setStyle(Style.FILL_AND_STROKE);
		mRoadPaint.setAntiAlias(true);
		
		mLinePaint = new Paint();
		mLinePaint.setColor(Color.YELLOW);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStrokeWidth(STROKE);
		
		mMovePaint = new Paint();
		mMovePaint.setColor(Color.BLUE);
		mMovePaint.setStrokeWidth(10);
		mMovePaint.setStyle(Style.FILL);
		mMovePaint.setStrokeCap(Paint.Cap.ROUND);
		mMovePaint.setAntiAlias(true);
		
		mCirclePaint = new Paint();
		mCirclePaint.setColor(Color.YELLOW);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setStyle(Style.FILL);
		
		mSuccess = new ArrayList<Integer>(); 
		mSuccessPaint = new Paint();
		mSuccessPaint.setAntiAlias(true);
		mSuccessPaint.setColor(Color.GREEN);
		mSuccessPaint.setStrokeWidth(10);
		
		prepare();
		
	}
	
	public void setListener(SuccessListener listener) {
		mListener = listener;
	}

	private void prepareCanvans() {
		mBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mCanvas.drawColor(Color.RED);
	}
	
	public void prepare() {
		drawBorder();
		drawCenter();
		drawLine();
		drawCornerPoint();
	}
	
	public void drawPath(Path path) {
		mCanvas.drawPath(path, mLinePaint);
		invalidate();
	}

	public void drawRect(Rect rect) {
		mCanvas.drawRect(rect, mRoadPaint);
		invalidate();
	}
	
	public void drawLine(Point start,Point end,int width) {
		Paint p = new Paint(mRoadPaint);
		p.setStrokeWidth(width);
		mCanvas.drawLine(start.x, start.y, end.x, end.y, p);
		invalidate();
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}
	
	boolean down;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		if(x < 0 || x >= mWidth || y < 0 || y >= mHeight)
			return false;
		if(mBitmap.getPixel((int)x, (int)y) == Color.RED) {
			reset();
			if(mListener != null)
				mListener.onFail();
			return false;
		}
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mCanvas.drawPoint(x, y, mMovePaint);
			down = true;
			break;
		case MotionEvent.ACTION_UP:
			mStartPoint = 0;
			down = false;
			break;
		case MotionEvent.ACTION_MOVE:
			if(mBitmap.getPixel((int)(mX+x)/2, (int)(mY+y)/2) == Color.RED) {
				reset();
				if(mListener != null)
					mListener.onFail();
				return false;
			}
			if(mX != 0 && mY != 0)
				mCanvas.drawLine(mX, mY, x, y, mMovePaint);
			break;
		default:
			break;
		}
		mX = x;
		mY = y;
		invalidate();
		int in = isInCircle(x, y);
		if(in != 0 && mStartPoint == 0 && down) {
			mStartPoint = in;
		}else if(in !=0 && in != mStartPoint && mStartPoint != 0) {
			int value = Math.abs(in - mStartPoint);
			if(!mSuccess.contains(value)) {
				mSuccess.add(value);
				mStartPoint = in;
				drawSuccessLine(value);
			}
		}
		if(mSuccess.size() == 6) {
			mListener.onSuccess();
			return false;
		}
		return true;
	}

	private void drawSuccessLine(int value) {
		switch (value) {
		case 2:
			mCanvas.drawLine(HAFT_ROAD, HAFT_ROAD, mWidth - HAFT_ROAD,HAFT_ROAD, mSuccessPaint);
			break;
		case 3:
			mCanvas.drawLine(mWidth - HAFT_ROAD,  HAFT_ROAD, mWidth - HAFT_ROAD,  mHeight - HAFT_ROAD, mSuccessPaint);
			break;
		case 4:
			mCanvas.drawLine(mWidth - HAFT_ROAD, mHeight - HAFT_ROAD, HAFT_ROAD, mHeight - HAFT_ROAD, mSuccessPaint);
			break;
		case 5:
			mCanvas.drawLine(mWidth - HAFT_ROAD, mHeight - HAFT_ROAD, HAFT_ROAD, HAFT_ROAD, mSuccessPaint);
			break;
		case 7:
			mCanvas.drawLine(HAFT_ROAD, mHeight - HAFT_ROAD, mWidth - HAFT_ROAD, HAFT_ROAD, mSuccessPaint);
			break;
		case 9:
			mCanvas.drawLine(HAFT_ROAD, mHeight - HAFT_ROAD, HAFT_ROAD, HAFT_ROAD, mSuccessPaint);
			break;
		default:
			break;
		}
		invalidate();
	}

	public void reset() {
		prepareCanvans();
		prepare();
		mX = 0;
		mY = 0;
		mSuccess.clear();
		down = false;
	}
	
	private int isInCircle(float x,float y) {
	
		int in = 0;
		if(y < mHeight/2) {
			if(x < mWidth/2)
				in = (Math.pow(x - HAFT_ROAD, 2) + Math.pow(y - HAFT_ROAD, 2)) <= RADIUS*RADIUS?1:0;
			else
				in = (Math.pow(x - mWidth + HAFT_ROAD, 2) + Math.pow(y - HAFT_ROAD, 2)) <= RADIUS*RADIUS?3:0;
		} else {
			if(x < mWidth/2) {
				in = (Math.pow(x - HAFT_ROAD, 2) + Math.pow(y - mHeight + HAFT_ROAD, 2)) <= RADIUS*RADIUS?10:0;
			} else
				in = (Math.pow(x - mWidth + HAFT_ROAD, 2) + Math.pow(y - mHeight + HAFT_ROAD, 2)) <= RADIUS*RADIUS?6:0;
		}
		return in;
	}
	
	private void drawCornerPoint() {
		mCanvas.drawCircle(HAFT_ROAD, HAFT_ROAD, RADIUS, mCirclePaint);
		mCanvas.drawCircle(mWidth - HAFT_ROAD, mHeight - HAFT_ROAD, RADIUS, mCirclePaint);
		mCanvas.drawCircle(mWidth - HAFT_ROAD, HAFT_ROAD, RADIUS, mCirclePaint);
		mCanvas.drawCircle(HAFT_ROAD, mHeight - HAFT_ROAD, RADIUS, mCirclePaint);
	}
	
	private void drawLine() {
		Path path = new Path();
		RectF rect = new RectF(HAFT_ROAD,HAFT_ROAD, mWidth - HAFT_ROAD,mHeight - HAFT_ROAD);
		path.addRect(rect, Direction.CCW);
		drawPath(path);
		path = new Path();
		path.moveTo(HAFT_ROAD +HAFT_STROKE,HAFT_ROAD+HAFT_STROKE);
		path.lineTo(mWidth - HAFT_ROAD-HAFT_STROKE,mHeight - HAFT_ROAD-HAFT_STROKE);
		
		path.moveTo(mWidth - HAFT_ROAD-HAFT_STROKE,HAFT_ROAD+HAFT_STROKE);
		path.lineTo(HAFT_ROAD+HAFT_STROKE,mHeight - HAFT_ROAD-HAFT_STROKE);
		drawPath(path);
	}
	

	private void drawCenter() {
		Point start = new Point(HAFT_ROAD+HAFT_STROKE, HAFT_ROAD+HAFT_STROKE);
		Point end = new Point(mWidth -HAFT_ROAD-HAFT_STROKE, mHeight-HAFT_ROAD-HAFT_STROKE);
		drawLine(start, end, ROAD_WIDTH);
		
		start = new Point(mWidth -HAFT_ROAD-HAFT_STROKE, HAFT_ROAD + HAFT_STROKE);
		end = new Point(HAFT_ROAD+HAFT_STROKE, mHeight-HAFT_ROAD-HAFT_STROKE);
		drawLine(start, end, ROAD_WIDTH);
	}


	private void drawBorder() {
		Rect rect = new Rect(0, 0, ROAD_WIDTH, mHeight);
		drawRect(rect);
		rect = new Rect(0, 0, mWidth, ROAD_WIDTH);
		drawRect(rect);
		rect = new Rect(mWidth - ROAD_WIDTH, 0, mWidth, mHeight);
		drawRect(rect);
		rect = new Rect(0, mHeight - ROAD_WIDTH, mWidth, mHeight);
		drawRect(rect);
	}
	
	public interface SuccessListener {
		public boolean onSuccess();
		public void onFail();
	}
}
