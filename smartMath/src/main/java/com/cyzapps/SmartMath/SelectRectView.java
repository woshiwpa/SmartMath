package com.cyzapps.SmartMath;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Region.Op;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SelectRectView extends View {
	
	public static final int MINIMUM_SELECTION_SIZE = 10;

    private Handler mHandler = new Handler();
	
	/** The paint to be used when drawing the chart. */
	private Paint mPaint = new Paint();
	
	/** The selection bounds. */
	private Rect mSelectedRect = new Rect(0, 0, 0, 0);
	private Rect mClipRect = new Rect();

	/** The old x coordinates. Use float here coz they definitely are Android's coordinate*/
	private float mfOldX = -1;
	/** The old y coordinate. */
	private float mfOldY = -1;
	/** The old x2 coordinate. */
	private float mfOldX2 = -1;
	/** The old y2 coordinate. */
	private float mfOldY2 = -1;
	
	public SelectRectView(Context context) {
		super(context);

	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int nPaintOriginalColor = mPaint.getColor();
		float fPaintOriginalStrokeWidth = mPaint.getStrokeWidth();
		Paint.Style stylePaintOriginalStyle = mPaint.getStyle();
		canvas.getClipBounds(mClipRect);	
		
		if (mSelectedRect.top == 0 && mSelectedRect.left == 0 && mSelectedRect.right == 0 && mSelectedRect.bottom == 0) {
			// mSelectedRect hasn't been initialized yet.
			mSelectedRect.top = (int) (mClipRect.top + mClipRect.height()/3.0);
			mSelectedRect.bottom = (int) (mClipRect.bottom - mClipRect.height()/3.0);
			mSelectedRect.left = (int) (mClipRect.left + mClipRect.width()/6.0);
			mSelectedRect.right = (int) (mClipRect.right - mClipRect.width()/6.0);
		} else {
			mSelectedRect.top = Math.max(mSelectedRect.top, mClipRect.top);
			mSelectedRect.bottom = Math.min(mSelectedRect.bottom, mClipRect.bottom);
			if (mSelectedRect.height() < MINIMUM_SELECTION_SIZE) {
				int nEnlarge = MINIMUM_SELECTION_SIZE - mSelectedRect.height();
				if (nEnlarge / 2 > mSelectedRect.top - mClipRect.top) {
					mSelectedRect.top = mClipRect.top;
					mSelectedRect.bottom = mSelectedRect.top + MINIMUM_SELECTION_SIZE;
				} else if (nEnlarge / 2 > mClipRect.bottom - mSelectedRect.bottom) {
					mSelectedRect.bottom = mClipRect.bottom;
					mSelectedRect.top = mSelectedRect.bottom - MINIMUM_SELECTION_SIZE;
				} else {
					mSelectedRect.top -= nEnlarge / 2;
					mSelectedRect.bottom += nEnlarge / 2;
				}
			}
			mSelectedRect.left = Math.max(mSelectedRect.left, mClipRect.left);
			mSelectedRect.right = Math.min(mSelectedRect.right, mClipRect.right);
			if (mSelectedRect.width() < MINIMUM_SELECTION_SIZE) {
				int nEnlarge = MINIMUM_SELECTION_SIZE - mSelectedRect.width();
				if (nEnlarge / 2 > mSelectedRect.left - mClipRect.left) {
					mSelectedRect.left = mClipRect.left;
					mSelectedRect.right = mSelectedRect.left + MINIMUM_SELECTION_SIZE;
				} else if (nEnlarge / 2 > mClipRect.right - mSelectedRect.right) {
					mSelectedRect.right = mClipRect.right;
					mSelectedRect.left = mSelectedRect.right - MINIMUM_SELECTION_SIZE;
				} else {
					mSelectedRect.left -= nEnlarge / 2;
					mSelectedRect.right += nEnlarge / 2;
				}
			}
		}

		// draw selection rectangle
		mPaint.setColor(Color.GREEN);
		mPaint.setStrokeWidth(3);        
		mPaint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(mSelectedRect, mPaint);

		mPaint.setColor(nPaintOriginalColor);
		mPaint.setStrokeWidth(fPaintOriginalStrokeWidth);
		mPaint.setStyle(stylePaintOriginalStyle);
	}

	/**
	 * Handles the touch event.
	 * 
	 * @param event
	 *            the touch event
	 */
	public boolean handleTouch(MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;	// same as getActionMask
		if (action == MotionEvent.ACTION_MOVE) {
			if (event.getPointerCount() == 1)	{	// slide
				float newX = event.getX(0);
				float newY = event.getY(0);
				if (mfOldX >= 0 && mfOldY >= 0)	{	// valid last position.
					// slide from (mfOldX, mfOldY) to (newX, newY)
					mSelectedRect.top += newY - mfOldY;
					mSelectedRect.bottom += newY - mfOldY;
					mSelectedRect.left += newX - mfOldX;
					mSelectedRect.right += newX - mfOldX;
					repaint();
				}
				mfOldX2 = -1;
				mfOldY2 = -1;
				mfOldX = newX;
				mfOldY = newY;
			} else if (event.getPointerCount() == 2)	{	// pinch
				float newX = event.getX(0);
				float newY = event.getY(0);
				float newX2 = event.getX(1);
				float newY2 = event.getY(1);
				if (mfOldX >= 0 && mfOldY >= 0 && mfOldX2 >= 0 && mfOldY2 >= 0)	{	// valid last positions
					float newDeltaX = Math.abs(newX - newX2);
					float newDeltaY = Math.abs(newY - newY2);
					float oldDeltaX = Math.abs(mfOldX - mfOldX2);
					float oldDeltaY = Math.abs(mfOldY - mfOldY2);
					float zoomRate = 1;

					// 1000.0 means a very large tan value.
					float fXMove = Math.abs((newX2 - newX) - (mfOldX2 - mfOldX));
					float fYMove = Math.abs((newY2 - newY) - (mfOldY2 - mfOldY));
					boolean bPinched = (oldDeltaX != 0 || oldDeltaY != 0);
					if (bPinched)	{
						float tan = 0;
						if (fXMove == 0 && fYMove == 0)	{
							bPinched = false;	// no need to pinche
						} else	{
							tan = fYMove / fXMove;
						}
						if (tan <= 0.577) {
							// 0.577 is the approximate value of tan(Pi/6)
							zoomRate = (float) Math.sqrt(
									(newDeltaX * newDeltaX + newDeltaY * newDeltaY)
									/ (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
							if (zoomRate < 0.625) {
								zoomRate = 0.625f;
							} else if (zoomRate > 1.6f)	{
								zoomRate = 1.6f;
							}
							// zoom zoomRate along x-axis.
							if (zoomRate > 1 || mSelectedRect.width() * zoomRate >= MINIMUM_SELECTION_SIZE) {
								// if enlarge or the size is still larger than minimum selection size, we zoom.
								mSelectedRect.left = (int) ((mSelectedRect.right + mSelectedRect.left)/2.0
										- mSelectedRect.width()/2.0 * zoomRate);
								mSelectedRect.right = (int) ((mSelectedRect.right + mSelectedRect.left)/2.0
										+ mSelectedRect.width()/2.0 * zoomRate);
							}
						} else if (tan >= 1.732) {
							// 1.732 is the approximate value of tan(Pi/3)
							zoomRate = (float) Math.sqrt(
									(newDeltaX * newDeltaX + newDeltaY * newDeltaY)
									/ (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
							if (zoomRate < 0.625) {
								zoomRate = 0.625f;
							} else if (zoomRate > 1.6f)	{
								zoomRate = 1.6f;
							}
							// zoom zoomRate along y-axis.
							if (zoomRate > 1 || mSelectedRect.height() * zoomRate >= MINIMUM_SELECTION_SIZE) {
								// if enlarge or the size is still larger than minimum selection size, we zoom.
								mSelectedRect.top = (int) ((mSelectedRect.bottom + mSelectedRect.top)/2.0
										- mSelectedRect.height()/2.0 * zoomRate);
								mSelectedRect.bottom = (int) ((mSelectedRect.bottom + mSelectedRect.top)/2.0
										+ mSelectedRect.height()/2.0 * zoomRate);
							}
						} else {
							// pinch zoom diagonally
							zoomRate = (float) Math.sqrt(
									(newDeltaX * newDeltaX + newDeltaY * newDeltaY)
									/ (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
							if (zoomRate < 0.625) {
								zoomRate = 0.625f;
							} else if (zoomRate > 1.6f)	{
								zoomRate = 1.6f;
							}
							if (zoomRate > 1 || mSelectedRect.width() * zoomRate >= MINIMUM_SELECTION_SIZE) {
								// if enlarge or the size is still larger than minimum selection size, we zoom.
								mSelectedRect.left = (int) ((mSelectedRect.right + mSelectedRect.left)/2.0
										- mSelectedRect.width()/2.0 * zoomRate);
								mSelectedRect.right = (int) ((mSelectedRect.right + mSelectedRect.left)/2.0
										+ mSelectedRect.width()/2.0 * zoomRate);
							}
							if (zoomRate > 1 || mSelectedRect.height() * zoomRate >= MINIMUM_SELECTION_SIZE) {
								// if enlarge or the size is still larger than minimum selection size, we zoom.
								mSelectedRect.top = (int) ((mSelectedRect.bottom + mSelectedRect.top)/2.0
										- mSelectedRect.height()/2.0 * zoomRate);
								mSelectedRect.bottom = (int) ((mSelectedRect.bottom + mSelectedRect.top)/2.0
										+ mSelectedRect.height()/2.0 * zoomRate);
							}
						}
					}
					if (bPinched)	{
						repaint();
					}
				}
				mfOldX = newX;
				mfOldY = newY;
				mfOldX2 = newX2;
				mfOldY2 = newY2;
			}
		} else if (action == MotionEvent.ACTION_POINTER_DOWN) {
			if (event.getPointerCount() == 2)	{
				mfOldX = event.getX(0);
				mfOldY = event.getY(0);
				mfOldX2 = event.getX(1);
				mfOldY2 = event.getY(1);
			} else if (event.getPointerCount() > 2)	{
				// if more than two pointers, exit pinch to zoom mode.
				mfOldX = -1;
				mfOldY = -1;
				mfOldX2 = -1;
				mfOldY2 = -1;
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
			mfOldX = -1;
			mfOldY = -1;
			mfOldX2 = -1;
			mfOldY2 = -1;
		}
		//TODO: enable or disable click.
		return true;	//!mRenderer.isClickEnabled();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// save the x and y so they can be used in the click and long press listeners
			mfOldX = event.getX();
			mfOldY = event.getY();
		}
		return handleTouch(event);
	}

	/**
	 * Schedule a view content repaint.
	 */
	public void repaint() {
		mHandler.post(new Runnable() {
			public void run() {
				invalidate();
			}
		});
	}

	/**
	 * Schedule a view content repaint, in the specified rectangle area.
	 */
	public void repaint(final int left, final int top, final int right,
			final int bottom) {
		mHandler.post(new Runnable() {
			public void run() {
				invalidate(left, top, right, bottom);
			}
		});
	}
	
	public Rect getSelectedRect() {
		return mSelectedRect;
	}

	public Rect getClipRect() {
		return mClipRect;
	}
}
