package com.android.TestMode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.SeekBar;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class VerticalSeekBar extends SeekBar
{
	private String TAG = "VerticalSeekBar";
    private boolean mIsDragging;
    private float mTouchDownY;
    private int mScaledTouchSlop;
    private boolean isInScrollingContainer = false;
    
    private OnVerticalSeekBarChangeListener mOnSeekBarChangeListener;
    
    public interface OnVerticalSeekBarChangeListener {
    	void onProgressChanged(VerticalSeekBar bar, int progress, boolean fromUser);
    	void onStartTrackingTouch(VerticalSeekBar bar);
    	void onStopTrackingTouch(VerticalSeekBar bar);
    }
    
    public void setOnSeekBarChangeListener(OnVerticalSeekBarChangeListener l) {
    	Log.d(TAG, "setOnSeekBarChangeListener");
    	mOnSeekBarChangeListener = l;
    }

    public boolean isInScrollingContainer()
    {
        return isInScrollingContainer;
    }

    public void setInScrollingContainer(boolean isInScrollingContainer)
    {
        this.isInScrollingContainer = isInScrollingContainer;
    }

    /**
     * On touch, this offset plus the scaled value from the position of the
     * touch will form the progress value. Usually 0.
     */
    float mTouchProgressOffset;

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

    }

    public VerticalSeekBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public VerticalSeekBar(Context context)
    {
        super(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {

        super.onSizeChanged(h, w, oldh, oldw);

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec,
            int heightMeasureSpec)
    {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }

    @Override
    protected synchronized void onDraw(Canvas canvas)
    {
        canvas.rotate(-90);
        canvas.translate(-getHeight(), 0);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (!isEnabled())
        {
            return false;
        }

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if (isInScrollingContainer())
                {

                    mTouchDownY = event.getY();
                }
                else
                {
                    setPressed(true);

                    invalidate();
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    attemptClaimDrag();

                    onSizeChanged(getWidth(), getHeight(), 0, 0);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsDragging)
                {
                    trackTouchEvent(event);

                }
                else
                {
                    final float y = event.getY();
                    if (Math.abs(y - mTouchDownY) > mScaledTouchSlop)
                    {
                        setPressed(true);

                        invalidate();
                        onStartTrackingTouch();
                        trackTouchEvent(event);
                        attemptClaimDrag();

                    }
                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                break;

            case MotionEvent.ACTION_UP:
                if (mIsDragging)
                {
                    trackTouchEvent(event);
                    onStopTrackingTouch();
                    setPressed(false);

                }
                else
                {
                    // Touch up when we never crossed the touch slop threshold
                    // should
                    // be interpreted as a tap-seek to that location.
                    onStartTrackingTouch();
                    trackTouchEvent(event);
                    onStopTrackingTouch();

                }
                onSizeChanged(getWidth(), getHeight(), 0, 0);
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;
        }
        return true;

    }

    private void trackTouchEvent(MotionEvent event)
    {
        final int height = getHeight();
        final int top = getPaddingTop();
        final int bottom = getPaddingBottom();
        final int available = height - top - bottom;

        int y = (int) event.getY();

        float scale;
        float progress = 0;

        // 下面是最小值
        if (y > height - bottom)
        {
            scale = 0.0f;
        }
        else if (y < top)
        {
            scale = 1.0f;
        }
        else
        {
            scale = (float) (available - y + top) / (float) available;
            progress = mTouchProgressOffset;
        }

        final int max = getMax();
        progress += scale * max;

        setProgress((int) progress);

    }

    /**
     * This is called when the user has started touching this widget.
     */
    void onStartTrackingTouch()
    {
    	Log.d(TAG, "onStartTrackingTouch");
        mIsDragging = true;
        if (mOnSeekBarChangeListener != null) {
        	Log.d(TAG, "if");
        	mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    /**
     * This is called when the user either releases his touch or the touch is
     * canceled.
     */
    void onStopTrackingTouch()
    {
    	Log.d(TAG, "onStopTrackingTouch");
        mIsDragging = false;
        if (mOnSeekBarChangeListener != null) {
        	Log.d(TAG, "if");
        	mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    private void attemptClaimDrag()
    {
        ViewParent p = getParent();
        if (p != null)
        {
            p.requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public synchronized void setProgress(int progress)
    {

        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);

    }
    
	private void setThumbPos(int w, Drawable thumb, float scale, int gap)
	{
		int available = 0;
		try
		{

			int up = getPaddingTop();
			int bottom = getPaddingBottom();
			
			available = getHeight() - up - bottom;
	        int thumbWidth = thumb.getIntrinsicWidth();
	        int thumbHeight = thumb.getIntrinsicHeight();
	        available -= thumbWidth;
	        
	        //The extra space for the thumb to move on the track
	        available += getThumbOffset() * 2;
	        
	        int thumbPos = (int) (scale * available);

	        int topBound, bottomBound;
	        if (gap == Integer.MIN_VALUE) {
	            Rect oldBounds = thumb.getBounds();
	            topBound = oldBounds.top;
	            bottomBound = oldBounds.bottom;
	        } else {
	            topBound = gap;
	            bottomBound = gap + thumbHeight;
	        }
	        // Canvas will be translated, so 0,0 is where we start drawing
	        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}   
	void onProgressRefresh(float scale, boolean fromUser)
	{
		Drawable thumb = null;
		try
		{
			Field mThumb_f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mThumb");
			mThumb_f.setAccessible(true);
			thumb = (Drawable)mThumb_f.get(this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		//setThumbPos(getWidth(), thumb, scale, Integer.MIN_VALUE);
		
		invalidate();
		
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
        }
	}	

}