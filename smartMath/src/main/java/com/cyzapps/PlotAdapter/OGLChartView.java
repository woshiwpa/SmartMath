package com.cyzapps.PlotAdapter;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class OGLChartView extends GLSurfaceView {

	/** the chart to be drawn */
	public OGLChart moglChart = null;
	/** The old x coordinates. Use float here coz they definitely are Android's coordinate*/
	private float mfOldX = -1;
	/** The old y coordinate. */
	private float mfOldY = -1;
	/** The old x2 coordinate. */
	private float mfOldX2 = -1;
	/** The old y2 coordinate. */
	private float mfOldY2 = -1;

	private boolean mbLandScapeMode = false;
	
	public OGLChartView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public OGLChartView(Context context, OGLChart oglChart) {
		super(context);
		moglChart = oglChart;
		setRenderer(oglChart);
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
					moglChart.slide(mfOldX, mfOldY, newX, newY);
				}
				mfOldX2 = -1;
				mfOldY2 = -1;
				mfOldX = newX;
				mfOldY = newY;
			} else if (event.getPointerCount() == 2)	{	// pinch
				if (!(moglChart instanceof OGLExprChart))	{
					// note that OGLExprChart is also instance of OGLChart so cannot use
					// if (moglChart instanceof OGLChart)...
					pinch2Zoom(event);	// data chart pinch to zoom
				}
			}
		} else if (action == MotionEvent.ACTION_DOWN) {
			mfOldX = event.getX(0);
			mfOldY = event.getY(0);
			moglChart.click(mfOldX, mfOldY);
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
			if (event.getPointerCount() == 2 && moglChart instanceof OGLExprChart) {
				pinch2Zoom(event);
			}
			mfOldX = -1;
			mfOldY = -1;
			mfOldX2 = -1;
			mfOldY2 = -1;
		}
		//TODO: enable or disable click.
		return true;	//!mRenderer.isClickEnabled();
	}

	private void pinch2Zoom (MotionEvent event) {
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
				} else if (mfOldX2 == mfOldX && mfOldY2 == mfOldY)	{
					zoomRate = 1.6f;
				} else if (newX2 == newX && newY2 == newY)	{
					zoomRate = 0.625f;
				} else	{
					zoomRate = (float) (Math.sqrt(newDeltaX*newDeltaX + newDeltaY*newDeltaY)
							/ Math.sqrt(oldDeltaX*oldDeltaX + oldDeltaY*oldDeltaY));
					//zoomRate = (float) Math.pow(zoomRate, 4);	// do not magnify coz sometimes it can be large enough
					if (zoomRate > 1.6f)	{
						zoomRate = 1.6f;
					} else if (zoomRate < 0.625f)	{
						zoomRate = 0.625f;
					}
/*Log.d("OGLChartView", "Zoom : from (" + mfOldX + ", " + mfOldY + ") (" + mfOldX2 + ", " + mfOldY2 + ") to ("
					+ newX + ", " + newY + ") (" + newX2 + ", " + newY2 + "), distanceOld is "
					+ Math.sqrt(oldDeltaX*oldDeltaX + oldDeltaY*oldDeltaY) + " , distanceNew is "
					+ Math.sqrt(newDeltaX*newDeltaX + newDeltaY*newDeltaY) + " , zoomRate is " + zoomRate);*/
					if (moglChart instanceof OGLExprChart)	{	// a sequence of zoom events, only 1st can be handled.
						if (zoomRate > 1)	{
							moglChart.zoom(1.6f);
						} else if (zoomRate < 1)	{
							moglChart.zoom(0.625f);
						} else	{
							moglChart.zoom(zoomRate);
						}
					} else	{
						moglChart.zoom(zoomRate);
					}
				}
			}
		}
		mfOldX = newX;
		mfOldY = newY;
		mfOldX2 = newX2;
		mfOldY2 = newY2;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// save the x and y so they can be used in the click and long press listeners
			mfOldX = event.getX();
			mfOldY = event.getY();
		}
		return handleTouch(event);
	}
	
	public Bitmap getSnapshot()	{
		return moglChart.mbitmapScreenSnapshot;
	}
}
