package com.android.TestMode;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class FocusManager {
	
	private ViewGroup mFocus;
	private List<Area> mFocusArea;
	private List<Area> mMeteringArea;
	private Matrix mMatrix;
	private View mPreviewFrame;
	private FocusIndicatorView mFocIndicatorView;
	
	public FocusManager (ViewGroup group,View previewFrame) {
		this.mFocus = group;
		mPreviewFrame = previewFrame;
		Matrix matrix = new Matrix();
		prepareMatrix(matrix, false, 90,
                previewFrame.getWidth(), previewFrame.getHeight());
		mMatrix = new Matrix();
		matrix.invert(mMatrix);
		mFocIndicatorView = (FocusIndicatorView) group.findViewById(R.id.focus);
	}
	
	public void showSuccess() {
		mFocIndicatorView.showSuccess();
	}
	
	public void showFail() {
		mFocIndicatorView.showFail();
	}
	
	public void showFocusing() {
		mFocIndicatorView.showStart();
	}
	
	public void onTouch(MotionEvent e) {
        int x = Math.round(e.getX());
        int y = Math.round(e.getY());
        
        int width = mFocus.getWidth();
        int height = mFocus.getHeight();
        
        int preWidth = mPreviewFrame.getWidth();
        int preHeight = mPreviewFrame.getHeight();
        
        if(mFocusArea == null) {
        	mMeteringArea = new ArrayList<Camera.Area>();
        	mFocusArea = new ArrayList<Camera.Area>();
        	mFocusArea.add(new Area(new Rect(), 1));
        	mMeteringArea.add(new Area(new Rect(), 1));
        }
        calculateTapArea(width, height, 1f, x, y, preWidth, preHeight, mFocusArea.get(0).rect);
        calculateTapArea(width, height, 1.5f, x, y, preWidth, preHeight, mMeteringArea.get(0).rect);
        // Use margin to set the focus indicator to the touched area.
        RelativeLayout.LayoutParams p =
                (RelativeLayout.LayoutParams) mFocus.getLayoutParams();
        int left = clamp(x - width / 2, 0, preWidth - width);
        int top = clamp(y - height / 2, 0, preHeight - height);
        p.setMargins(left, top, 0, 0);
        // Disable "center" rule because we no longer want to put it in the center.
        int[] rules = p.getRules();
        rules[RelativeLayout.CENTER_IN_PARENT] = 0;
        mFocus.requestLayout();
	}
	
	public List<Area> getFocusAreas() {
		return mFocusArea;
	}
	
	public List<Area> getMeteringArea() {
		return mMeteringArea;
	}
	
    public void calculateTapArea(int focusWidth, int focusHeight, float areaMultiple,
            int x, int y, int previewWidth, int previewHeight, Rect rect) {
        int areaWidth = (int)(focusWidth * areaMultiple);
        int areaHeight = (int)(focusHeight * areaMultiple);
        int left = clamp(x - areaWidth / 2, 0, previewWidth - areaWidth);
        int top = clamp(y - areaHeight / 2, 0, previewHeight - areaHeight);

        RectF rectF = new RectF(left, top, left + areaWidth, top + areaHeight);
        mMatrix.mapRect(rectF);
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
        
    }
    
    public int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }
    
    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
            int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }
}
