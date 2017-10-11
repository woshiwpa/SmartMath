package com.cyzapps.SmartMath;

import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.imgproc.ImageMgr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class PaintImageView extends View {
	
    protected static final float TOUCH_TOLERANCE = 4;
    
	protected static final String BITMAP_X0 = "Bitmap_X0";
	protected static final String BITMAP_Y0 = "Bitmap_Y0";
	protected static final String ZOOM_RATIO = "Zoom_Ratio";
	protected static final String BITMAP_ACTUAL = "Bitmap_Actual";
	protected static final String BITMAP_PREV = "Bitmap_Prev";
	protected static final String BITMAP_NEXT = "Bitmap_Next";
	protected static final String TOUCHING_MODE = "Touching_Mode";
	protected static final String SELECTED_LEFT = "Selected_Left";
	protected static final String SELECTED_RIGHT = "Selected_Right";
	protected static final String SELECTED_TOP = "Selected_Top";
	protected static final String SELECTED_BOTTOM = "Selected_Bottom";

	public int mnViewWidth = 0;
    public int mnViewHeight = 0;
    
    public double mdBitmapX0 = 0.0;
    public double mdBitmapY0 = 0.0;
    public double mdZoomRatio = 1.0;
    
    public Context mcontext;
    public Bitmap mbitmapActual = null;
    public Bitmap mbitmapPrev = null, mbitmapNext = null;
    public Bitmap mbitmapShown = null;
    public Paint mpaint;
    public Paint mpaintSelect;
    public int mnTouchingMode = 1;	// 0 means erazer, 1 means pen, 2 means crop select, 3 means move the chart.
    protected float mfX0 = -1f, mfY0 = -1f;
    protected float mfX, mfY;
    public int mnSelectedLeft = -1, mnSelectedRight = -1, mnSelectedTop = -1, mnSelectedBottom = -1;	// select right and bottom are actually rightP1 and bottomP1 (this works better in zoomed in cases).
    
    public PaintImageView(Context c, AttributeSet attrs) {
        super(c, attrs);
        mcontext = c;
        // now get screen size.
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((ActivityFingerPaint)mcontext).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        //int width = (int)(displaymetrics.widthPixels / displaymetrics.density);
        //int height = (int)(displaymetrics.heightPixels / displaymetrics.density);
        // do not adjust density here
        int width = (int)(displaymetrics.widthPixels * 0.6667);
        int height = (int)(displaymetrics.heightPixels * 0.6667);
        
    	// if bitmap hasn't been initialized, make the initial bitmap size same as screen size
    	if (mbitmapActual != null)	{
    		mbitmapActual.recycle();
    	}
    	mbitmapActual = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
    
        Canvas canvas = new Canvas(mbitmapActual);
        canvas.drawColor(0xFFFFFFFF);

        mpaintSelect = new Paint();
        mpaintSelect.setAntiAlias(false);
        mpaintSelect.setDither(false);
        mpaintSelect.setFilterBitmap(false);
        mpaintSelect.setColor(0xFF0000AA);
        mpaintSelect.setStyle(Paint.Style.STROKE);
        mpaintSelect.setStrokeJoin(Paint.Join.ROUND);
        mpaintSelect.setStrokeCap(Paint.Cap.ROUND);
        mpaintSelect.setStrokeWidth(2);
    }
    
    public void createShownBitmap()	{
    	Position3D pntShownLeftTop = mapView2Bitmap(new Position3D(0, 0));
    	Position3D pntShownRightBottom = mapView2Bitmap(new Position3D(mnViewWidth, mnViewHeight));
    	int nShownLeft = (int)Math.max(pntShownLeftTop.getX(), 0);
    	int nShownTop = (int)Math.max(pntShownLeftTop.getY(), 0);
    	int nShownRight = (int)Math.min(pntShownRightBottom.getX(), mbitmapActual.getWidth());
    	int nShownBottom = (int)Math.min(pntShownRightBottom.getY(), mbitmapActual.getHeight());
    	int nWidth = nShownRight - nShownLeft;
    	int nHeight = nShownBottom - nShownTop;
    	if (nWidth <= 0 || nHeight <= 0)	{
    		if (mbitmapShown != null)	{
    			mbitmapShown.recycle();
	    		mbitmapShown = null;	// nothing to show
    		}
    	} else	{
	    	Bitmap bitmapShownTmp = Bitmap.createBitmap(mbitmapActual, nShownLeft, nShownTop, nWidth, nHeight);
    		if (mbitmapShown != null)	{
    			mbitmapShown.recycle();
    		}
	    	mbitmapShown = Bitmap.createScaledBitmap(bitmapShownTmp, (int)(nWidth * mdZoomRatio), (int)(nHeight * mdZoomRatio), false);
	    	if (bitmapShownTmp != null)	{
	    		bitmapShownTmp.recycle();
	    	}
	    	
	    	if (mnSelectedLeft >= 0 && mnSelectedRight >= 0 && mnSelectedTop >= 0 && mnSelectedBottom >= 0
	    			&& mnSelectedRight > mnSelectedLeft && mnSelectedBottom > mnSelectedTop)	{
	    		// we do select something
	    		Position3D pntSelectLeftTop = mapBitmap2View(new Position3D(mnSelectedLeft - 1, mnSelectedTop - 1));	// use left -1 and top -1 so that the left top edge can be selected.
	    		Position3D pntSelectRightBottom = mapBitmap2View(new Position3D(mnSelectedRight, mnSelectedBottom));
	        	Canvas canvas = new Canvas(mbitmapShown);
	        	canvas.drawLine((float)pntSelectLeftTop.getX(), (float)pntSelectLeftTop.getY(),
	        			(float)pntSelectLeftTop.getX(), (float)pntSelectRightBottom.getY(), mpaintSelect);
	        	canvas.drawLine((float)pntSelectLeftTop.getX(), (float)pntSelectLeftTop.getY(),
	        			(float)pntSelectRightBottom.getX(), (float)pntSelectLeftTop.getY(), mpaintSelect);
	        	canvas.drawLine((float)pntSelectLeftTop.getX(), (float)pntSelectRightBottom.getY(),
	        			(float)pntSelectRightBottom.getX(), (float)pntSelectRightBottom.getY(), mpaintSelect);
	        	canvas.drawLine((float)pntSelectRightBottom.getX(), (float)pntSelectLeftTop.getY(),
	        			(float)pntSelectRightBottom.getX(), (float)pntSelectRightBottom.getY(), mpaintSelect);
	    	}
    	}
    }
    
    public byte[][] getBiMatrix()	{
    	if (mbitmapActual == null)	{
    		return null;
    	}else if (mnSelectedLeft < 0 || mnSelectedRight < 0 || mnSelectedTop < 0 || mnSelectedBottom < 0
    			|| mnSelectedRight <= mnSelectedLeft || mnSelectedBottom <= mnSelectedTop)	{
    		return ImageMgr.convertImg2BiMatrix(mbitmapActual);
    	} else	{
    		byte[][] biAllMatrix = ImageMgr.convertImg2BiMatrix(mbitmapActual);
    		int nWidth = mnSelectedRight - mnSelectedLeft;
    		int nHeight = mnSelectedBottom - mnSelectedTop;
    		byte[][] biMatrix = new byte[nWidth][nHeight];
    		for (int idx = 0; idx < nWidth; idx ++)	{
    			System.arraycopy(biAllMatrix[idx + mnSelectedLeft], mnSelectedTop, biMatrix[idx], 0, nHeight);
    		}
    		return biMatrix;
    	}
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	createShownBitmap();
    	if (mbitmapShown != null)	{
    		canvas.drawBitmap(mbitmapShown, 0, 0, null);	// shown map is always shown on left top.
    		// cannot delete the mbitmapShown here because rotate image may use it.
    		//mbitmapShown.recycle();
    		//mbitmapShown = null;
    	}
    }
    
    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        mnViewWidth = xNew;
        mnViewHeight = yNew;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y, event);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }
    
    private void touch_start(float x, float y) {
        mfX0 = mfX = x;
        mfY0 = mfY = y;
		Position3D pnt = mapView2Bitmap(new Position3D(x, y));
    	if (mnTouchingMode == 0 || mnTouchingMode == 1)	{	// draw mode, which will change the chart
    		if (mbitmapPrev != null)	{
    			mbitmapPrev.recycle();
    		}
    		mbitmapPrev = mbitmapActual.copy(Bitmap.Config.RGB_565, true);		// to save memory, do not use ALPHA_8888
	        ((ActivityFingerPaint)mcontext).mstrCurrentPnt = "(" + (int)(pnt.getX()) + "," + (int)(pnt.getY()) + ")";
	        ((ActivityFingerPaint)mcontext).mstrWidthHeight = "";
	        ((ActivityFingerPaint)mcontext).setImageInfoText();
    	} else if (mnTouchingMode == 2)	{	// select mode
	        ((ActivityFingerPaint)mcontext).mstrCurrentPnt = "(" + (int)(pnt.getX()) + "," + (int)(pnt.getY()) + ")";
	        ((ActivityFingerPaint)mcontext).mstrWidthHeight = "0×0";
	        ((ActivityFingerPaint)mcontext).setImageInfoText();
    	}	// move mode need not to do anything.
		
    }
    
    private void touch_move(float x, float y, MotionEvent event) {
        float dx = x - mfX;
        float dy = y - mfY;
        if (Math.abs(dx) >= TOUCH_TOLERANCE || Math.abs(dy) >= TOUCH_TOLERANCE) {
        	if (mnTouchingMode == 0 || mnTouchingMode == 1)	{	// draw mode
            	Canvas canvas = new Canvas(mbitmapActual);
        		Position3D pnt = mapView2Bitmap(new Position3D(x, y));
            	if (mfX == x && mfY == y)	{
            		canvas.drawPoint((float)pnt.getX(), (float)pnt.getY(), mpaint);
            	} else	{
            		Position3D pnt1 = mapView2Bitmap(new Position3D(mfX, mfY));
            		float xLast = (float)pnt1.getX(), yLast = (float)pnt1.getY();
            		for (int idx = 0; idx < event.getHistorySize(); idx ++) {
            			Position3D pntThis = mapView2Bitmap(new Position3D(event.getHistoricalX(idx), event.getHistoricalY(idx)));
            			float xThis = (float)pntThis.getX();
            			float yThis = (float)pntThis.getY();
            			canvas.drawLine(xLast, yLast, xThis, yThis, mpaint);
            			xLast = xThis;
            			yLast = yThis;
            		}
            		canvas.drawLine(xLast, yLast, (float)pnt.getX(), (float)pnt.getY(), mpaint);
            	}
    	        ((ActivityFingerPaint)mcontext).mstrCurrentPnt = "(" + (int)(pnt.getX()) + "," + (int)(pnt.getY()) + ")";
    	        ((ActivityFingerPaint)mcontext).mstrWidthHeight = "";
    	        ((ActivityFingerPaint)mcontext).setImageInfoText();
        	} else if (mnTouchingMode == 2)	{	// select mode
        		calcCroppedArea(mfX0, mfY0, x, y);
    	        ((ActivityFingerPaint)mcontext).mstrWidthHeight = (mnSelectedRight - mnSelectedLeft) + "×" + (mnSelectedBottom - mnSelectedTop);
    	        ((ActivityFingerPaint)mcontext).setImageInfoText();
        	} else if (mnTouchingMode == 3)	{	// move mode
        		double dBmpActualLeft = mdBitmapX0, dBmpActualTop = mdBitmapY0;
        		double dBmpActualRight = mdBitmapX0 + mbitmapActual.getWidth() * mdZoomRatio;
        		double dBmpActualBottom  = mdBitmapY0 + mbitmapActual.getHeight() * mdZoomRatio;
        		double dMax2LeftMove = Math.max(0, dBmpActualRight - mnViewWidth);
        		double dMax2RightMove = Math.max(0, - dBmpActualLeft);
        		double dMaxUpMove = Math.max(0, dBmpActualBottom - mnViewHeight);
        		double dMaxDownMove = Math.max(0, - dBmpActualTop);
        		
        		if (dx < 0)	{	// move to left
        			dx = (float)Math.max(dx, - dMax2LeftMove);
        		} else if (dx > 0)	{	// move to right
        			dx = (float)Math.min(dx, dMax2RightMove);
        		}

        		if (dy < 0)	{	// move up
        			dy = (float)Math.max(dy, - dMaxUpMove);
        		} else if (dy > 0)	{	// move down
        			dy = (float)Math.min(dy, dMaxDownMove);
        		}
        		
        		mdBitmapX0 += dx / mdZoomRatio;
        		mdBitmapY0 += dy / mdZoomRatio;
        	}
            mfX = x;
            mfY = y;
        }
    }
    
    private void touch_up() {
        if (mnTouchingMode == 0 || mnTouchingMode == 1)	{
        	Canvas canvas = new Canvas(mbitmapActual);
        	Position3D pnt = mapView2Bitmap(new Position3D(mfX, mfY));
    		canvas.drawPoint((float)pnt.getX(), (float)pnt.getY(), mpaint);
    		((ActivityFingerPaint)mcontext).onImageChanged();
    	}
        mfX0 = mfY0 = -1;
    }
    
    protected void calcCroppedArea(float fX0, float fY0, float fX1, float fY1)	{
		int nSelectedLeft = (int)Math.min(fX0, fX1);
		int nSelectedRight = (int)Math.max(fX0, fX1);
		int nSelectedTop = (int)Math.min(fY0, fY1);
		int nSelectedBottom = (int)Math.max(fY0, fY1);
		Position3D pntLeftTop = mapView2Bitmap(new Position3D(nSelectedLeft, nSelectedTop));
		Position3D pntRightBottom = mapView2Bitmap(new Position3D(nSelectedRight, nSelectedBottom));
		mnSelectedLeft = Math.max(0, (int)pntLeftTop.getX());
		mnSelectedTop = Math.max(0, (int)pntLeftTop.getY());
		mnSelectedRight = Math.min(mbitmapActual.getWidth(), (int)pntRightBottom.getX());
		mnSelectedBottom = Math.min(mbitmapActual.getHeight(), (int)pntRightBottom.getY());
    }

    public void setBitmap(Bitmap bitmap)	{
    	if (mbitmapActual != null)	{
    		mbitmapActual.recycle();
    	}
    	mbitmapActual = bitmap;
    	if (mbitmapPrev != null)	{
    		mbitmapPrev.recycle();
    	}
    	mbitmapPrev = null;
    	if (mbitmapNext != null)	{
    		mbitmapNext.recycle();
    	}
    	mbitmapNext = null;
    	mdZoomRatio = ((ActivityFingerPaint)mcontext).setImageViewZoomRatio(Math.max((double)mnViewWidth/bitmap.getWidth(),
    																				mnViewHeight/bitmap.getHeight()));
    	mdBitmapX0 = 0.0;
    	mdBitmapY0 = 0.0;
    	
    	mnSelectedLeft = mnSelectedRight = mnSelectedTop = mnSelectedBottom = -1;
    	createShownBitmap();
    	invalidate();
    }
    
    public int getActualBitmapWidth() {
    	return (mbitmapActual == null)? 0:mbitmapActual.getWidth();
    }
    
    public int getActualBitmapHeight() {
    	return (mbitmapActual == null)? 0:mbitmapActual.getHeight();
    }
    
    public void resizeBitmap(int nNewWidth, int nNewHeight) {
    	if (nNewWidth <= 0 || nNewHeight <= 0) {
    		return; // invalid size
    	} else if (mbitmapActual.getWidth() == nNewWidth && mbitmapActual.getHeight() == nNewHeight) {
    		return;	// no change
    	}
    	if (mbitmapPrev != null)	{
    		mbitmapPrev.recycle();
    	}
		mbitmapPrev = mbitmapActual;
		int nOverlappedWidth = Math.min(mbitmapActual.getWidth(), nNewWidth);
		int nOverlappedHeight = Math.min(mbitmapActual.getHeight(), nNewHeight);
		// copy overlapped area first
		Bitmap bitmapOverlapped = Bitmap.createBitmap(mbitmapActual, 0, 0, nOverlappedWidth, nOverlappedHeight);
    	// shouldn't recycle mbitmapActual now because it is mbitmapPrev.
		mbitmapActual = Bitmap.createBitmap(nNewWidth, nNewHeight, Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
        Canvas canvas = new Canvas(mbitmapActual);
        canvas.drawColor(0xFFFFFFFF);
		canvas.drawBitmap(bitmapOverlapped, 0, 0, null);

    	if (mbitmapNext != null)	{
    		mbitmapNext.recycle();
    	}
		mbitmapNext = null;
		setTouchMode(mnTouchingMode);
		((ActivityFingerPaint)mcontext).onImageChanged();
    }

    public void deleteCroppedArea()	{
    	if (mbitmapActual == null)	{
    		return;
    	} else if (mnSelectedLeft < 0 || mnSelectedRight < 0 || mnSelectedTop < 0 || mnSelectedBottom < 0
    			|| mnSelectedRight <= mnSelectedLeft || mnSelectedBottom <= mnSelectedTop)	{
    		// the whole picture is deleted.
        	if (mbitmapPrev != null)	{
        		mbitmapPrev.recycle();
        	}
    		mbitmapPrev = mbitmapActual;
        	// shouldn't recycle mbitmapActual now because it is mbitmapPrev.
    		mbitmapActual = Bitmap.createBitmap(mbitmapActual.getWidth(), mbitmapActual.getHeight(), Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
            Canvas canvas = new Canvas(mbitmapActual);
            canvas.drawColor(0xFFFFFFFF);

        	if (mbitmapNext != null)	{
        		mbitmapNext.recycle();
        	}
    		mbitmapNext = null;
    		setTouchMode(mnTouchingMode);
    		((ActivityFingerPaint)mcontext).onImageChanged();
    	} else	{
        	if (mbitmapPrev != null)	{
        		mbitmapPrev.recycle();
        	}
    		mbitmapPrev = mbitmapActual.copy(Bitmap.Config.RGB_565, true);		// to save memory, do not use ALPHA_8888
        	Canvas canvas = new Canvas(mbitmapActual);
            Paint paintDelete = new Paint();
            paintDelete.setAntiAlias(false);
            paintDelete.setDither(false);
            paintDelete.setFilterBitmap(false);
            paintDelete.setColor(0xFFFFFFFF);
            paintDelete.setStyle(Paint.Style.FILL_AND_STROKE);
            paintDelete.setStrokeJoin(Paint.Join.ROUND);
            paintDelete.setStrokeCap(Paint.Cap.ROUND);
            paintDelete.setStrokeWidth(1);
        	canvas.drawRect(mnSelectedLeft, mnSelectedTop, mnSelectedRight - 1, mnSelectedBottom - 1, paintDelete);
        	if (mbitmapNext != null)	{
        		mbitmapNext.recycle();
        	}
        	mbitmapNext = null;
    		setTouchMode(mnTouchingMode);
    		((ActivityFingerPaint)mcontext).onImageChanged();
        }
    }
    
    public void rotateImage(double dAngle)	{
    	if (mbitmapActual == null || dAngle == 0)	{
    		return;
    	} 
    	
    	if (mnSelectedLeft < 0 || mnSelectedRight < 0 || mnSelectedTop < 0 || mnSelectedBottom < 0
    			|| mnSelectedRight <= mnSelectedLeft || mnSelectedBottom <= mnSelectedTop)	{
    		// the whole picture is rotated.
    		
        	if (mbitmapPrev != null)	{
        		mbitmapPrev.recycle();
        	}
    		mbitmapPrev = mbitmapActual;
    		// clear canvas first
        	// shouldn't recycle mbitmapActual now because it is mbitmapPrev.
    		mbitmapActual = Bitmap.createBitmap(mbitmapActual.getWidth(), mbitmapActual.getHeight(), Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
            Canvas canvas = new Canvas(mbitmapActual);
            canvas.drawColor(0xFFFFFFFF);
            // calculate pivot point
			Position3D pntCentralShownBitmap = new Position3D(mbitmapShown.getWidth()/2.0, mbitmapShown.getHeight()/2.0);
			Position3D pntRotatePnt = mapView2Bitmap(pntCentralShownBitmap);
			// rotate canvas
			canvas.rotate((float)-dAngle, (float)pntRotatePnt.getX(), (float)pntRotatePnt.getY());
			// draw rotated bitmap.
			canvas.drawBitmap(mbitmapPrev, 0, 0, null);

        	if (mbitmapNext != null)	{
        		mbitmapNext.recycle();
        	}
			mbitmapNext = null;
    		setTouchMode(mnTouchingMode);
    		((ActivityFingerPaint)mcontext).onImageChanged();
    	} else	{
    		// selected area is rotated.
    		
        	if (mbitmapPrev != null)	{
        		mbitmapPrev.recycle();
        	}
    		mbitmapPrev = mbitmapActual.copy(Bitmap.Config.RGB_565, true);		// to save memory, do not use ALPHA_8888
    		// copy selected area first
    		Bitmap bitmapRotated = Bitmap.createBitmap(mbitmapActual, mnSelectedLeft, mnSelectedTop,
					mnSelectedRight - mnSelectedLeft, mnSelectedBottom - mnSelectedTop);
    		// clear selected area
    		Canvas canvas = new Canvas(mbitmapActual);
            Paint paintDelete = new Paint();
            paintDelete.setAntiAlias(false);
            paintDelete.setDither(false);
            paintDelete.setFilterBitmap(false);
            paintDelete.setColor(0xFFFFFFFF);
            paintDelete.setStyle(Paint.Style.FILL_AND_STROKE);
            paintDelete.setStrokeJoin(Paint.Join.ROUND);
            paintDelete.setStrokeCap(Paint.Cap.ROUND);
            paintDelete.setStrokeWidth(1);
        	canvas.drawRect(mnSelectedLeft, mnSelectedTop, mnSelectedRight - 1, mnSelectedBottom - 1, paintDelete);
            // calculate pivot point
			Position3D pntCentralShownBitmap = new Position3D((mnSelectedLeft + mnSelectedRight)/2.0, (mnSelectedTop + mnSelectedBottom)/2.0);
			Position3D pntRotatePnt = mapView2Bitmap(pntCentralShownBitmap);
			// rotate canvas
			canvas.rotate((float)-dAngle, (float)pntRotatePnt.getX(), (float)pntRotatePnt.getY());
			// draw rotated selection.
			canvas.drawBitmap(bitmapRotated, mnSelectedLeft, mnSelectedTop, null);

        	if (mbitmapNext != null)	{
        		mbitmapNext.recycle();
        	}
        	mbitmapNext = null;
    		setTouchMode(mnTouchingMode);
    		((ActivityFingerPaint)mcontext).onImageChanged();
        }
    }
    
    public boolean unDo()	{
    	if (mbitmapPrev == null)	{
    		return false;
    	} else	{
        	if (mbitmapNext != null)	{
        		mbitmapNext.recycle();
        	}
    		mbitmapNext = mbitmapActual;
        	// should not recycle mbitmapActual now because it is mbitmapNext.
    		mbitmapActual = mbitmapPrev;
        	// should not recycle mbitmapPrev now because it is mbitmapActual.
    		mbitmapPrev = null;
    		setTouchMode(mnTouchingMode);
            return true;
    	}
    }
    
    public boolean reDo()	{
    	if (mbitmapNext == null)	{
    		return false;
    	} else	{
        	if (mbitmapPrev != null)	{
        		mbitmapPrev.recycle();
        	}
    		mbitmapPrev = mbitmapActual;
    		// should not recycle mbitmapActual now because it is mbitmapPrev.
    		mbitmapActual = mbitmapNext;
    		// should not recycle mbitmapNext now because it is mbitmapActual.
    		mbitmapNext = null;
    		setTouchMode(mnTouchingMode);
            return true;
    	}
    }
    
    public void setTouchMode(int nTouchingMode)	{
		mnTouchingMode = nTouchingMode;
		mnSelectedLeft = mnSelectedRight = mnSelectedTop = mnSelectedBottom = -1;
        invalidate();
    }
    
    public void zoom(double dNewRatio)	{
    	double dViewCentralX = mnViewWidth / 2.0;
    	double dViewCentralY = mnViewHeight / 2.0;
    	Position3D pntCentralXY = mapView2Bitmap(new Position3D(dViewCentralX, dViewCentralY));
    	mdBitmapX0 = (mdBitmapX0 - pntCentralXY.getX()) * dNewRatio / mdZoomRatio + pntCentralXY.getX();
    	mdBitmapY0 = (mdBitmapY0 - pntCentralXY.getY()) * dNewRatio / mdZoomRatio + pntCentralXY.getY();
    	mdZoomRatio = dNewRatio;
    	// ok, now ensure that shown image is as large as possible and left top point is always included in image.
		double dBmpActualLeft = mdBitmapX0, dBmpActualTop = mdBitmapY0;
		double dBmpActualRight = mdBitmapX0 + mbitmapActual.getWidth() * mdZoomRatio;
		double dBmpActualBottom  = mdBitmapY0 + mbitmapActual.getHeight() * mdZoomRatio;
		double dLeftEdge = - dBmpActualLeft;
		double dRightEdge = dBmpActualRight - mnViewWidth;
		double dTopEdge = - dBmpActualTop;
		double dBottomEdge = dBmpActualBottom - mnViewHeight;
		if (dLeftEdge + dRightEdge < 0)	{
			mdBitmapX0 = 0;	// align bitmap left with view left.
		} else if (dRightEdge < 0)	{
			mdBitmapX0 -= dRightEdge;	// shift right as left edge must be positive
		} else if (dLeftEdge < 0)	{
			mdBitmapX0 += dLeftEdge;	// shift left as right edge must be positive
		}
		
		if (dTopEdge + dBottomEdge < 0)	{
			mdBitmapY0 = 0;	// align bitmap top with view top.
		} else if (dBottomEdge < 0)	{
			mdBitmapY0 -= dBottomEdge;	// shift down as top edge must be positive
		} else if (dTopEdge < 0)	{
			mdBitmapY0 += dTopEdge;	// shift up as bottom edge must be positive
		}

    	invalidate();
    }
    
    public void zoomFit()	{
    	mdZoomRatio = ((ActivityFingerPaint)mcontext).setImageViewZoomRatio(Math.max((double)mnViewWidth/mbitmapActual.getWidth(),
																			(double)mnViewHeight/mbitmapActual.getHeight()));
    	mdBitmapX0 = 0.0;
    	mdBitmapY0 = 0.0;
    	invalidate();
    }
    
    
    // map point functions
    public Position3D mapBitmap2View(Position3D pntBitmap)	{
    	double dX = pntBitmap.getX() * mdZoomRatio + mdBitmapX0;
    	double dY = pntBitmap.getY() * mdZoomRatio + mdBitmapY0;
    	return new Position3D(dX, dY);
    }
    
    public Position3D mapView2Bitmap(Position3D pntView)	{
    	double dX = (pntView.getX() - mdBitmapX0) / mdZoomRatio;
    	double dY = (pntView.getY() - mdBitmapY0) / mdZoomRatio;
    	return new Position3D(dX, dY);
    }
    
	public void onSaveInstanceState(Bundle outState)	{
		outState.putDouble(BITMAP_X0, mdBitmapX0);
		outState.putDouble(BITMAP_Y0, mdBitmapY0);
		outState.putDouble(ZOOM_RATIO, mdZoomRatio);
		outState.putParcelable(BITMAP_ACTUAL, mbitmapActual);
		outState.putParcelable(BITMAP_PREV, mbitmapPrev);
		outState.putParcelable(BITMAP_NEXT, mbitmapNext);
		outState.putInt(TOUCHING_MODE, mnTouchingMode);
		outState.putInt(SELECTED_LEFT, mnSelectedLeft);
		outState.putInt(SELECTED_TOP, mnSelectedTop);
		outState.putInt(SELECTED_RIGHT, mnSelectedRight);
		outState.putInt(SELECTED_BOTTOM, mnSelectedBottom);
	}

	protected void onRestoreInstanceState (Bundle inState) {
		mdBitmapX0 = inState.getDouble(BITMAP_X0);
		mdBitmapY0 = inState.getDouble(BITMAP_Y0);
		mdZoomRatio = inState.getDouble(ZOOM_RATIO);
    	if (mbitmapActual != null)	{
    		mbitmapActual.recycle();
    	}
		mbitmapActual = inState.getParcelable(BITMAP_ACTUAL);
    	if (mbitmapPrev != null)	{
    		mbitmapPrev.recycle();
    	}
		mbitmapPrev = inState.getParcelable(BITMAP_PREV);
    	if (mbitmapNext != null)	{
    		mbitmapNext.recycle();
    	}
		mbitmapNext = inState.getParcelable(BITMAP_NEXT);
		mnTouchingMode = inState.getInt(TOUCHING_MODE);
		mnSelectedLeft = inState.getInt(SELECTED_LEFT);
		mnSelectedTop = inState.getInt(SELECTED_TOP);
		mnSelectedRight = inState.getInt(SELECTED_RIGHT);
		mnSelectedBottom = inState.getInt(SELECTED_BOTTOM);
		
		((ActivityFingerPaint)mcontext).mimgBtnUndo.setEnabled(mbitmapPrev != null);
		((ActivityFingerPaint)mcontext).mimgBtnRedo.setEnabled(mbitmapNext != null);
		mdZoomRatio = ((ActivityFingerPaint)mcontext).setImageViewZoomRatio(mdZoomRatio);
		((ActivityFingerPaint)mcontext).selectTouchMoveBtn(mnTouchingMode, false);
		
		invalidate();
	}
}
