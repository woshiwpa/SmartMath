package com.cyzapps.PlotAdapter;


import java.lang.reflect.Array;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class FlatChartView extends View {
	public static class LimitedSizeStack {
		private int mnBufferSize = 1;
		private Float[] marrayFloats = new Float[1];

		private int mnSize = 0;
		private int mnStartIdx = 0;
		
		public LimitedSizeStack(int nBufferSize) {
			// buffer size has to be positive
			mnBufferSize = nBufferSize;
			marrayFloats = new Float[mnBufferSize];
			mnSize = 0;
			mnStartIdx = 0;
		}
		
		public int size() {return mnSize;}
		
		public int getSizeLimit() {return mnBufferSize;}
		
		public void addFirst(Float f) {
			mnStartIdx --;
			if (mnStartIdx < 0)	{
				mnStartIdx = mnBufferSize - 1;
			}
			marrayFloats[mnStartIdx] = f;
			if (mnSize + 1 > mnBufferSize) {
				mnSize = mnBufferSize;
			} else {
				mnSize ++;
			}
		}
		
		public Float getFirst() {
			if (mnSize == 0)	{
				return null;
			}
			return marrayFloats[mnStartIdx];
		}

		public Float getLast() {
			if (mnSize == 0)	{
				return null;
			}
			return marrayFloats[(mnStartIdx + mnSize - 1)%mnBufferSize];
		}

		public Float removeFirst() {
			if (mnSize == 0)	{
				return null;
			}
			int nOldStartIdx = mnStartIdx;
			mnStartIdx = (mnStartIdx + 1)%mnBufferSize;
			mnSize --;
			return marrayFloats[nOldStartIdx];
		}
		
		public Float removeLast() {
			if (mnSize == 0)	{
				return null;
			}
			mnSize --;
			return marrayFloats[(mnStartIdx + mnSize)%mnBufferSize];
		}
		
		public void clear() {
			mnStartIdx = 0;
			mnSize = 0;
		}
	}

    private Handler mHandler = new Handler();

	/** the chart to be drawn */
	public FlatChart mflatChart = null;
	
	/** The paint to be used when drawing the chart. */
	private Paint mPaint = new Paint();
	
	/** The view bounds. */
	private Rect mRect = new Rect();

	/** The old x coordinates. Use float here coz they definitely are Android's coordinate. The length should be no grate than 16.*/
	private LimitedSizeStack mlistfOldX = new LimitedSizeStack(16);
	/** The old y coordinates. The length should be no grate than 16.*/
	private LimitedSizeStack mlistfOldY = new LimitedSizeStack(16);
	/** The old x1 coordinates. Use float here coz they definitely are Android's coordinate*/
	
	private float mfOldX1 = -1;
	/** The old y1 coordinate. */
	private float mfOldY1 = -1;
	/** The old x2 coordinate. */
	private float mfOldX2 = -1;
	/** The old y2 coordinate. */
	private float mfOldY2 = -1;
	
	public FlatChartView(Context context) {
		super(context);

	}
	
	public FlatChartView(Context context, FlatChart flatChart) {
		super(context);
		mflatChart = flatChart;
	}
	
    
	@Override
	protected void onSizeChanged(int w, int h, int wOld, int hOld) {
		super.onSizeChanged(w, h, wOld, hOld);

		mflatChart.resize(w, h, wOld, hOld);
	}
    
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.getClipBounds(mRect);
		double top = mRect.top;
		double left = mRect.left;
		double width = mRect.width();
		double height = mRect.height();
	    
		mflatChart.draw(canvas, left, top, width, height, mPaint);
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
				if (mlistfOldX.size() > 0 && mlistfOldY.size() > 0)	{	// valid last position.
					if (mflatChart instanceof XYExprChart || mflatChart instanceof PolarExprChart)	{
						/* no longer move chart at each moving event in Android because continuous
						 * move in Android is very sluggish. Move once all all moving event finishes.
						// multiple continues slides are supported. slide only if movement is big enough.
						float fXMove = newX - mlistfOldX.getLast(), fYMove = newY - mlistfOldY.getLast();
						float fMinMoveThresh = Math.min(160, (getWidth() + getHeight())/8.0f);
						if (Math.abs(fXMove) >= fMinMoveThresh || Math.abs(fYMove) >= fMinMoveThresh)	{
							float fAdjustXMove, fAdjustYMove;
							if (Math.abs(fXMove) > Math.abs(fYMove))	{
								fAdjustXMove = (float) (fXMove / Math.abs(fXMove) * 0.375 * (getWidth() + getHeight())/2.0);
								fAdjustYMove = fAdjustXMove * fYMove/fXMove;
							} else	{
								fAdjustYMove = (float) (fYMove / Math.abs(fYMove) * 0.375 * (getWidth() + getHeight())/2.0);
								fAdjustXMove = fAdjustYMove * fXMove/fYMove;
							}
							mflatChart.slide(0, 0, fAdjustXMove, fAdjustYMove);
							mlistfOldX.clear();
							mlistfOldY.clear();
						}*/
						Float fXLast = mlistfOldX.getLast(), fYLast = mlistfOldY.getLast();
						mlistfOldX.clear();
						mlistfOldY.clear();
						mlistfOldX.addFirst(fXLast);
						mlistfOldY.addFirst(fYLast);
					} else	{
						mflatChart.slide(mlistfOldX.getFirst(), mlistfOldY.getFirst(), newX, newY);
						repaint();
					}
				}
				mfOldX2 = -1;
				mfOldY2 = -1;
				mfOldX1 = newX;
				mfOldY1 = newY;
				mlistfOldX.addFirst(newX);
				mlistfOldY.addFirst(newY);
			} else if (event.getPointerCount() == 2)	{	// pinch
				mlistfOldX.clear();	// clear old xy lists. this prevent miss recognize pinch to move
				mlistfOldY.clear();
				if (!(mflatChart instanceof XYExprChart) && !(mflatChart instanceof PolarExprChart))	{
					// note that instanceof XYExprChart is also instance of XYChart, so cannot use
					// if (mflatChart instance XYChart ...
					pinch2Zoom(event);	// data chart pinch to zoom
				}
			}
		} else if (action == MotionEvent.ACTION_DOWN) {
			mfOldX1 = event.getX(0);
			mfOldY1 = event.getY(0);
			mlistfOldX.clear();
			mlistfOldX.addFirst(mfOldX1);
			mlistfOldY.clear();
			mlistfOldY.addFirst(mfOldY1);
			
			if (mflatChart.mrectZoomR.contains(mfOldX1, mfOldY1)) {
				int nNumOfButtons = 4;
				int nCfgBtnPosition = 0, nZoomInBtnPosition = 0, nZoomOutBtnPosition = 0, nZoom1BtnPosition = 0, nZoomFitBtnPosition = 0;
				// keep in mind the order, has to be polar first then xy, expr first than non-expr
				// because a mflatChart can be instance of both polar and xy.
				if (mflatChart instanceof PolarExprChart || mflatChart instanceof PolarChart)	{
					if (mflatChart instanceof PolarExprChart)	{
						nNumOfButtons = 4;
						nCfgBtnPosition = 1;
					} else if (mflatChart instanceof PolarChart)	{
						nNumOfButtons = 3;
						nCfgBtnPosition = 0;
					}
					nZoomInBtnPosition = nCfgBtnPosition + 1;
					nZoomOutBtnPosition = nCfgBtnPosition + 2;
					nZoomFitBtnPosition = nCfgBtnPosition + 3;
				} else if (mflatChart instanceof XYExprChart || mflatChart instanceof XYChart)	{
					if (mflatChart instanceof XYExprChart)	{
						nNumOfButtons = 5;
						nCfgBtnPosition = 1;
					} else if (mflatChart instanceof XYChart)	{
						nNumOfButtons = 4;
						nCfgBtnPosition = 0;
					}
					nZoomInBtnPosition = nCfgBtnPosition + 1;
					nZoomOutBtnPosition = nCfgBtnPosition + 2;
					nZoom1BtnPosition = nCfgBtnPosition + 3;
					nZoomFitBtnPosition = nCfgBtnPosition + 4;
				}
				if (!mflatChart.mbLandScapeMode)	{
					if (mfOldX1 < mflatChart.mrectZoomR.left + mflatChart.mrectZoomR.width() * nCfgBtnPosition / nNumOfButtons) {
						mflatChart.config();
					} else if (mfOldX1 < mflatChart.mrectZoomR.left + mflatChart.mrectZoomR.width() * nZoomInBtnPosition / nNumOfButtons) {
						mflatChart.zoom(2.0, 2.0);
					} else if (mfOldX1 < mflatChart.mrectZoomR.left + mflatChart.mrectZoomR.width() * nZoomOutBtnPosition / nNumOfButtons) {
						mflatChart.zoom(0.5, 0.5);
					} else if (mfOldX1 < mflatChart.mrectZoomR.left + mflatChart.mrectZoomR.width() * nZoom1BtnPosition / nNumOfButtons) {
						mflatChart.zoom1To1();
					} else {	// zoom fit
						mflatChart.zoomReset();
					}
				} else {
					if (mfOldY1 < mflatChart.mrectZoomR.top + mflatChart.mrectZoomR.height() * nCfgBtnPosition / nNumOfButtons) {
						mflatChart.config();
					} else if (mfOldY1 < mflatChart.mrectZoomR.top + mflatChart.mrectZoomR.height() * nZoomInBtnPosition / nNumOfButtons) {
						mflatChart.zoom(2.0, 2.0);
					} else if (mfOldY1 < mflatChart.mrectZoomR.top + mflatChart.mrectZoomR.height() * nZoomOutBtnPosition / nNumOfButtons) {
						mflatChart.zoom(0.5, 0.5);
					} else if (mfOldY1 < mflatChart.mrectZoomR.top + mflatChart.mrectZoomR.height() * nZoom1BtnPosition / nNumOfButtons) {
						mflatChart.zoom1To1();
					} else {	// zoom fit
						mflatChart.zoomReset();
					}					
				}
				repaint();
				// clear old x y list to prevent unwanted movement.
				mlistfOldX.clear();
				mlistfOldY.clear();
			}
		} else if (action == MotionEvent.ACTION_POINTER_DOWN) {
			mlistfOldX.clear();
			mlistfOldY.clear();
			if (event.getPointerCount() == 2)	{
				mfOldX1 = event.getX(0);
				mfOldY1 = event.getY(0);
				mfOldX2 = event.getX(1);
				mfOldY2 = event.getY(1);
			} else if (event.getPointerCount() > 2)	{
				// if more than two pointers, exit pinch to zoom mode.
				mfOldX1 = -1;
				mfOldY1 = -1;
				mfOldX2 = -1;
				mfOldY2 = -1;
			}
		} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
			// a pointer down implies that movement track is cleared.
			if (action == MotionEvent.ACTION_UP && (mflatChart instanceof XYExprChart || mflatChart instanceof PolarExprChart)
					&& mlistfOldX.size() >= 2 && mlistfOldY.size() >= 2 && mlistfOldX.size() == mlistfOldY.size()) {
				// we just finished a series moving event.
				mflatChart.slide(mlistfOldX.getLast(), mlistfOldY.getLast(), mlistfOldX.getFirst(), mlistfOldY.getFirst());
				repaint();
			}
			if (event.getPointerCount() == 2 && (mflatChart instanceof XYExprChart || mflatChart instanceof PolarExprChart)) {
				pinch2Zoom(event);
			}
			mlistfOldX.clear();
			mlistfOldY.clear();
			mfOldX1 = -1;
			mfOldY1 = -1;
			mfOldX2 = -1;
			mfOldY2 = -1;
		}
		//TODO: enable or disable click.
		return true;	//!mRenderer.isClickEnabled();
	}

	private void pinch2Zoom (MotionEvent event) {	// repaint has been called inside the function
		float newX = event.getX(0);
		float newY = event.getY(0);
		float newX2 = event.getX(1);
		float newY2 = event.getY(1);
		if (mfOldX1 >= 0 && mfOldY1 >= 0 && mfOldX2 >= 0 && mfOldY2 >= 0)	{	// valid last positions
			float newDeltaX = Math.abs(newX - newX2);
			float newDeltaY = Math.abs(newY - newY2);
			float oldDeltaX = Math.abs(mfOldX1 - mfOldX2);
			float oldDeltaY = Math.abs(mfOldY1 - mfOldY2);
			float zoomRate = 1;

			// 1000.0 means a very large tan value.
			float fXMove = Math.abs((newX2 - newX) - (mfOldX2 - mfOldX1));
			float fYMove = Math.abs((newY2 - newY) - (mfOldY2 - mfOldY1));
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
					if (mflatChart instanceof XYExprChart || mflatChart instanceof PolarExprChart)	{
						// multiple continues zooms are not supported.
						if (zoomRate > 1)	{
							zoomRate = 1.6f;
						} else if (zoomRate < 1)	{
							zoomRate = 0.625f;
						}
					} else if (zoomRate < 0.625) {
						zoomRate = 0.625f;
					} else if (zoomRate > 1.6f)	{
						zoomRate = 1.6f;
					}
					mflatChart.zoom(zoomRate, 1);
				} else if (tan >= 1.732) {
					// 1.732 is the approximate value of tan(Pi/3)
					zoomRate = (float) Math.sqrt(
							(newDeltaX * newDeltaX + newDeltaY * newDeltaY)
							/ (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
					if (mflatChart instanceof XYExprChart || mflatChart instanceof PolarExprChart)	{
						// multiple continues zooms are not supported.
						if (zoomRate > 1)	{
							zoomRate = 1.6f;
						} else if (zoomRate < 1)	{
							zoomRate = 0.625f;
						}
					} else if (zoomRate < 0.625) {
						zoomRate = 0.625f;
					} else if (zoomRate > 1.6f)	{
						zoomRate = 1.6f;
					}
					mflatChart.zoom(1, zoomRate);
				} else {
					// pinch zoom diagonally
					zoomRate = (float) Math.sqrt(
							(newDeltaX * newDeltaX + newDeltaY * newDeltaY)
							/ (oldDeltaX * oldDeltaX + oldDeltaY * oldDeltaY));
					if (mflatChart instanceof XYExprChart || mflatChart instanceof PolarExprChart)	{
						// multiple continues zooms are not supported.
						if (zoomRate > 1)	{
							zoomRate = 1.6f;
						} else if (zoomRate < 1)	{
							zoomRate = 0.625f;
						}
					} else if (zoomRate < 0.625) {
						zoomRate = 0.625f;
					} else if (zoomRate > 1.6f)	{
						zoomRate = 1.6f;
					}
					mflatChart.zoom(zoomRate, zoomRate);
				}
			}
			if (bPinched)	{
				repaint();
			}
		}
		mfOldX1 = newX;
		mfOldY1 = newY;
		mfOldX2 = newX2;
		mfOldY2 = newY2;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
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

}
