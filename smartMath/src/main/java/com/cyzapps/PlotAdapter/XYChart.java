/**
 * Copyright (C) 2009 - 2012 SC 4ViewSoft SRL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyzapps.PlotAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.LinkedList;

import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.VisualMFP.CoordAxis;
import com.cyzapps.VisualMFP.DataSeriesCurve;
import com.cyzapps.VisualMFP.LinearMapper;
import com.cyzapps.VisualMFP.MathLib;
import com.cyzapps.VisualMFP.PointMapper;
import com.cyzapps.VisualMFP.Position3D;






import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.Log;

/**
 * The XY chart rendering class.
 */
public class XYChart extends FlatChart {
	
	// FROM coordinate: chart's coordinate
	// TO coordinate: Android's coordinate
	
	/** The multiple series dataset. */
	public LinkedList<DataSeriesCurve> mDataSet = new LinkedList<DataSeriesCurve>();
	
	protected CoordAxis mcaXAxis = new CoordAxis();
	protected CoordAxis mcaYAxis = new CoordAxis();
	
	public Position3D mp3CoordLeftBottomInFROM = new Position3D();	// FROM's coordinate of left bottom point of FROM's coordinate
	protected Position3D mp3CoordLeftBottomInTO = new Position3D();	// TO's coordinate of left bottom point of FROM's coordinate
	
	protected double mdCoordWidth = 0;
	protected double mdCoordHeight = 0;
	
	protected LinearMapper mmapperP2P = new LinearMapper();
	
	public double mdXAxisLenInFROM = 0;
	public double mdYAxisLenInFROM = 0;
	public double getScalingRatioX()	{
		return mdCoordWidth / mdXAxisLenInFROM;
	}
	public void setScalingRatioX(double dScalingRatioX)	{
		mdXAxisLenInFROM = mdCoordWidth / dScalingRatioX;
	}
	public double getScalingRatioY()	{
		return -mdCoordHeight / mdYAxisLenInFROM;
	}
	public void setScalingRatioY(double dScalingRatioY)	{
		mdYAxisLenInFROM = -mdCoordHeight / dScalingRatioY;
	}
	
	public String mstrXAxisName = "x";
	public double mdXMark1 = 0;
	public double mdXMark2 = 1;
	public String mstrYAxisName = "y";
	public double mdYMark1 = 0;
	public double mdYMark2 = 1;
	
	public Position3D mp3SavedCoordLeftBottomInFROM = mp3CoordLeftBottomInFROM;	// original coordinate of FROM's zero point in TO
	public double mdSavedXAxisLenInFROM = mdXAxisLenInFROM;
	public double getSavedScalingRatioX()	{	// scaling ratio = length of 1 unit in FROM / length of 1 unit in TO = TO total length / FROM total length
		return mdCoordWidth / mdSavedXAxisLenInFROM;
	}
	public double mdSavedYAxisLenInFROM = mdYAxisLenInFROM;
	public double getSavedScalingRatioY()	{
		return -mdCoordHeight / mdSavedYAxisLenInFROM;
	}
	public double mdSavedXMark1 = mdXMark1;
	public double mdSavedXMark2 = mdXMark2;
	public double mdSavedYMark1 = mdYMark1;
	public double mdSavedYMark2 = mdYMark2;
	
	public boolean mbShowGrid = true;
	// default foreground color
	public com.cyzapps.VisualMFP.Color mcolorHint = new com.cyzapps.VisualMFP.Color(128, 128, 128, 128);
	
	public boolean mbShowZoomBtns = true;
		
	public XYChart(Context context) {
		super(context);
	}
	
	@Override
	public void saveSettings()	{
		mp3SavedCoordLeftBottomInFROM = mp3CoordLeftBottomInFROM;
		mdSavedXAxisLenInFROM = mdXAxisLenInFROM;
		mdSavedYAxisLenInFROM = mdYAxisLenInFROM;
		mdSavedXMark1 = mdXMark1;
		mdSavedXMark2 = mdXMark2;
		mdSavedYMark1 = mdYMark1;
		mdSavedYMark2 = mdYMark2;
	}

	@Override
	public void restoreSettings()	{
		mp3CoordLeftBottomInFROM = mp3SavedCoordLeftBottomInFROM;
		mdXAxisLenInFROM = mdSavedXAxisLenInFROM;
		mdYAxisLenInFROM = mdSavedYAxisLenInFROM;
		mdXMark1 = mdSavedXMark1;
		mdXMark2 = mdSavedXMark2;
		mdYMark1 = mdSavedYMark1;
		mdYMark2 = mdSavedYMark2;
	}

	public void drawAxis(Canvas canvas, CoordAxis caAxis, double x0, double y0, double x1, double y1,
			Paint paint, com.cyzapps.VisualMFP.Color newColor, boolean bTxtPlacedClockWise) {
		float fX0 = (float)x0, fY0 = (float)y0, fX1 = (float)x1, fY1 = (float)y1;
		int nPaintOriginalColor = paint.getColor();
		if (newColor != null)	{
			paint.setColor(newColor.getARGB());
		} else if (caAxis.mclr != null)	 {
			paint.setColor(caAxis.mclr.getARGB());
		} else if (mcolorForeGrnd != null)	{
			paint.setColor(mcolorForeGrnd.getARGB());
		}	// otherwise, use paint's color
		canvas.drawLine(fX0, fY0, fX1, fY1, paint);
		double dAngle0To1 = Math.atan2(y1 - y0, x1 - x0);
		// arrow angle, 
		double dAngleArrowLn1 = -Math.PI - mdArrowAngle / 2 + dAngle0To1;
		double dAngleArrowLn2 = -Math.PI + mdArrowAngle / 2 + dAngle0To1;
		float fArrowLn1PntX = (float) (Math.cos(dAngleArrowLn1) * mdSmallSize + x1);
		float fArrowLn1PntY = (float) (Math.sin(dAngleArrowLn1) * mdSmallSize + y1);
		float fArrowLn2PntX = (float) (Math.cos(dAngleArrowLn2) * mdSmallSize + x1);
		float fArrowLn2PntY = (float) (Math.sin(dAngleArrowLn2) * mdSmallSize + y1);
		canvas.drawLine(fX1, fY1, fArrowLn1PntX, fArrowLn1PntY, paint);
		canvas.drawLine(fX1, fY1, fArrowLn2PntX, fArrowLn2PntY, paint);
		
		double dAngleLn2Txt = Math.PI/2 + dAngle0To1;
		if (bTxtPlacedClockWise)	{
			dAngleLn2Txt = -Math.PI/2 + dAngle0To1;
		}
		for (int idx = 0; idx < caAxis.marraydScaleMarks.length; idx++)	{
			double dX = 0, dY = 0;
			if (caAxis.mdValueTo != caAxis.mdValueFrom)	{
				dX = (x1 - x0)/(caAxis.mdValueTo - caAxis.mdValueFrom) * (caAxis.marraydScaleMarks[idx] - caAxis.mdValueFrom) + x0;
				dY = (y1 - y0)/(caAxis.mdValueTo - caAxis.mdValueFrom) * (caAxis.marraydScaleMarks[idx] - caAxis.mdValueFrom) + y0;
				float fX = (float)dX, fY = (float)dY, fTinySize = (float)mdTinySize;
				canvas.drawCircle(fX, fY, fTinySize, paint);
				double dMarkTxtX = dX, dMarkTxtY = dY;
				dMarkTxtX = Math.cos(dAngleLn2Txt) * mdSmallSize + dX;
				dMarkTxtY = Math.sin(dAngleLn2Txt) * mdSmallSize + dY;
				draw1LineTextAnchorMid(canvas, caAxis.marraystrMarkTxts[idx],
						dMarkTxtX, dMarkTxtY, paint, null, 
						mdSmallSize, dAngle0To1 * 180 / Math.PI);
			}
		}
		
		double dNameTxtX = (x0 + x1)/2, dNameTxtY = (y0 + y1)/2;
		dNameTxtX = Math.cos(dAngleLn2Txt) * mdSmallSize * 2 + dNameTxtX;
		dNameTxtY = Math.sin(dAngleLn2Txt) * mdSmallSize * 2 + dNameTxtY;
		// write title of the axis
		draw1LineTextAnchorMid(canvas, caAxis.mstrAxisName, dNameTxtX, dNameTxtY, paint, null,
				mdSmallSize, dAngle0To1 * 180 / Math.PI);
		paint.setColor(nPaintOriginalColor);
	}
	
	public void drawDataCurve(Canvas canvas, PointMapper mapper, DataSeriesCurve dsv, double x, double y, double width, double height, Paint paint)	{
		int nPaintOriginalColor = paint.getColor();
		Paint.Style paintStyleOriginal = paint.getStyle();
		float fPaintStrokeWidthOriginal = paint.getStrokeWidth();
		PathEffect peOriginal = paint.getPathEffect();
		
		boolean bPrevPntExists = false, bPrevPntInRange = false, bShouldDrawLine = true;
		Position3D p3MappedPrev = null;
		for (int idx = 0; idx < dsv.mlistData.size(); idx ++)	{
			if (!MathLib.isValidReal(dsv.mlistData.get(idx).getConvertedPoint().getX())
					|| !MathLib.isValidReal(dsv.mlistData.get(idx).getConvertedPoint().getY()))	{
				bPrevPntExists = false;
				bPrevPntInRange = false;
				continue;
			}
			Position3D p3Mapped = mapper.mapFrom2To(dsv.mlistData.get(idx).getConvertedPoint());
			Position3D p3LnPnt1 = p3Mapped, p3LnPnt0 = p3MappedPrev;
			
			Log.e(LOG_TAG, "in FlatChart.findInRangeLine, x1 = " + x + " X2 = " + (x + width) + " y1 = " + y + " y2 = " + (y + height)
					+ " point = " + p3Mapped + " idx = " + idx + " dsv.listData.size() = " + dsv.mlistData.size());
			if (p3Mapped.getX() >= x && p3Mapped.getX() <= (x + width)
					&& p3Mapped.getY() >= y && p3Mapped.getY() <= (y + height))	{
				// p3Mapped is in the rectangle of coordinate screen.
				drawPoint(canvas, p3Mapped, paint, dsv.mpointStyle);
				if (bPrevPntExists && bPrevPntInRange)	{
					bShouldDrawLine = true;
					p3LnPnt1 = p3Mapped;
					p3LnPnt0 = p3MappedPrev;
				} else if (bPrevPntExists)	{	//bPrevPntInRange == false
					Position3D[] points = findInRangeLine(x, x+width, y, y+height, p3MappedPrev, p3Mapped);
					if (points.length == 0)	{
						bShouldDrawLine = false;
					} else if (points.length == 1)	{
						bShouldDrawLine = true;
						p3LnPnt0 = points[0];
						p3LnPnt1 = points[0];
					} else	{
						bShouldDrawLine = true;
						p3LnPnt0 = points[0];
						p3LnPnt1 = points[1];
					}
				} else	{
					bShouldDrawLine = false;
				}
				bPrevPntInRange = true;
			} else	{
				// p3Mapped is out of the rectangle of coordinate screen.
				if (bPrevPntExists)	{
					Position3D[] points = findInRangeLine(x, x+width, y, y+height, p3MappedPrev, p3Mapped);
					if (points.length == 0)	{
						bShouldDrawLine = false;
					} else if (points.length == 1)	{
						bShouldDrawLine = true;
						p3LnPnt0 = points[0];
						p3LnPnt1 = points[0];
					} else	{
						bShouldDrawLine = true;
						p3LnPnt0 = points[0];
						p3LnPnt1 = points[1];
					}
				} else	{
					bShouldDrawLine = false;
				}
				bPrevPntInRange = false;
			}
			bPrevPntExists = true;
			
			if (bShouldDrawLine)	{
				drawLine(canvas, p3LnPnt0, p3LnPnt1, paint, dsv.mlineStyle);
			}
			
			p3MappedPrev = p3Mapped;
		}
		
		paint.setStyle(paintStyleOriginal);
		paint.setStrokeWidth(fPaintStrokeWidthOriginal);
		paint.setPathEffect(peOriginal);
		paint.setColor(nPaintOriginalColor);
	}
	
	public void drawDataCurves(Canvas canvas, PointMapper mapper, LinkedList<DataSeriesCurve> dataSet, double x, double y, double width, double height, Paint paint)	{
		for (int idx = 0; idx < dataSet.size(); idx ++)	{
			drawDataCurve(canvas, mapper, dataSet.get(idx), x, y, width, height, paint);
		}
	}
		
    @Override
    public void drawButtons(Canvas canvas, double left, double top, double width, double height, Paint paint)  {
		int nOriginalPaintColor = paint.getColor();
		Style styleOriginal = paint.getStyle();
		paint.setColor(mnCfgZoomBtnsBkcolor);
		paint.setStyle(Style.STROKE);
		
		double dZoomButtonSize = mdLargeSize;
		int nNumOfButtons = 4;
		if (this instanceof XYExprChart)	{
			nNumOfButtons = 5;
		}
		// mfMediumSize and mfLargeSize should be determined by Screen size.
		// dZoomButtonSize = Math.max(dZoomButtonSize, Math.min(width, height) / 7);
		if (width <= height)	{	// Portrait
			mrectZoomR.set((float)(left + width - dZoomButtonSize * nNumOfButtons),
					(float)(top + height - dZoomButtonSize * 0.7),
					(float)(left + width),
					(float)(top + height));
		} else	{	// Landscape
			mrectZoomR.set((float)(left + width - dZoomButtonSize * 0.7),
					(float)(top + height / 2 - dZoomButtonSize * nNumOfButtons / 2.0),
					(float)(left + width),
					(float)(top + height / 2 + dZoomButtonSize * nNumOfButtons / 2.0));
		}
		canvas.drawRoundRect(mrectZoomR, (float)(dZoomButtonSize / 3), (float)(dZoomButtonSize / 3), paint);
		Bitmap cfgImage, zoomInImage, zoomOutImage, xy1To1ZoomImage, fitZoomImage;
		// height of the bitmap is determined by device density and original bitmap height
		// device density / original bitmap stored density * original bitmap height
		double dIconSize = mzoomInImage_24.getHeight();
		if (dZoomButtonSize <= mzoomInImage_24.getHeight() * 3)	{	// was 24 * 3
			dIconSize = mzoomInImage_24.getHeight();
			cfgImage = mcfgImage_24;
			zoomInImage = mzoomInImage_24;
			zoomOutImage = mzoomOutImage_24;
			xy1To1ZoomImage = mxy1To1ZoomImage_24;
			fitZoomImage = mfitZoomImage_24;
		} else if (dZoomButtonSize <= mzoomInImage_32.getHeight() * 3)	{	// was 32 * 3
			dIconSize = mzoomInImage_32.getHeight();
			cfgImage = mcfgImage_32;
			zoomInImage = mzoomInImage_32;
			zoomOutImage = mzoomOutImage_32;
			xy1To1ZoomImage = mxy1To1ZoomImage_32;
			fitZoomImage = mfitZoomImage_32;
		} else if (dZoomButtonSize <= mzoomInImage_48.getHeight() * 3)	{	// was 48 * 3
			dIconSize = mzoomInImage_48.getHeight();
			cfgImage = mcfgImage_48;
			zoomInImage = mzoomInImage_48;
			zoomOutImage = mzoomOutImage_48;
			xy1To1ZoomImage = mxy1To1ZoomImage_48;
			fitZoomImage = mfitZoomImage_48;
		} else	{
			dIconSize = mzoomInImage_64.getHeight();
			cfgImage = mcfgImage_64;
			zoomInImage = mzoomInImage_64;
			zoomOutImage = mzoomOutImage_64;
			xy1To1ZoomImage = mxy1To1ZoomImage_64;
			fitZoomImage = mfitZoomImage_64;
		}
		if (width <= height)	{	// Portrait
			mbLandScapeMode = false;
			double dButtonY = mrectZoomR.top + (dZoomButtonSize * 0.7 - dIconSize)/2.0;
			double dHShiftRatio = 0.25;
			if (this instanceof XYExprChart)	{
				canvas.drawBitmap(cfgImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
				dHShiftRatio ++;
			}
			canvas.drawBitmap(zoomInImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
			canvas.drawBitmap(zoomOutImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
			canvas.drawBitmap(xy1To1ZoomImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
			canvas.drawBitmap(fitZoomImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
		} else	{	// landscape
			mbLandScapeMode = true;
			double dButtonX = mrectZoomR.left + (dZoomButtonSize * 0.7 - dIconSize)/2.0;
			double dVShiftRatio = 0.25;
			if (this instanceof XYExprChart)	{
				canvas.drawBitmap(cfgImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
				dVShiftRatio ++;
			}
			canvas.drawBitmap(zoomInImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
			canvas.drawBitmap(zoomOutImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
			canvas.drawBitmap(xy1To1ZoomImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
			canvas.drawBitmap(fitZoomImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
		}
    	
		paint.setColor(nOriginalPaintColor);
		paint.setStyle(styleOriginal);
    }
    
	@Override
	public void draw(Canvas canvas, double x, double y, double width, double height, Paint paint) {
		Log.e(LOG_TAG, "in XYChart.draw");
		
		//calcCoordArea(x, y, width, height);
		//update();

		int nPaintOriginalColor = paint.getColor();
		// draw background
		paint.setColor(mcolorBkGrnd.getARGB());
		drawBackground(canvas, x, y, width, height, paint, null);
		
		paint.setColor(mcolorForeGrnd.getARGB());
		// draw title
		draw1LineTextAnchorMid(canvas, mstrChartTitle, width / 2, y + mdSmallSize/2, paint, null, mdSmallSize, 0);
		
		
		// draw x-axis
		drawAxis(canvas, mcaXAxis, mp3CoordLeftBottomInTO.getX(), mp3CoordLeftBottomInTO.getY(),
				mp3CoordLeftBottomInTO.getX() + mdCoordWidth, mp3CoordLeftBottomInTO.getY(),
				paint, mcolorForeGrnd, false);
		// draw y-axis
		drawAxis(canvas, mcaYAxis, mp3CoordLeftBottomInTO.getX(), mp3CoordLeftBottomInTO.getY(),
				mp3CoordLeftBottomInTO.getX(), mp3CoordLeftBottomInTO.getY() - mdCoordHeight,
				paint, mcolorForeGrnd, true);
		
		paint.setColor(mcolorHint.getARGB());
		// draw a rectangle for drawing area
		float fCoordLeft = (float)mp3CoordLeftBottomInTO.getX(),
				fCoordTop = (float)(mp3CoordLeftBottomInTO.getY() - mdCoordHeight),
				fCoordRight = (float)(mp3CoordLeftBottomInTO.getX() + mdCoordWidth),
				fCoordBottom = (float)mp3CoordLeftBottomInTO.getY();
		canvas.drawLine(fCoordLeft, fCoordTop, fCoordRight, fCoordTop, paint);
		canvas.drawLine(fCoordRight, fCoordTop, fCoordRight, fCoordBottom, paint);
		
		if (mbShowGrid)	{
			drawGrid(canvas, paint);
		}

		Rect rectClip = canvas.getClipBounds();
		canvas.save();	// IMPORTANT: save current state of clip and matrix (i.e. unclipped state) (let's say it's state #1)
		canvas.clipRect(fCoordLeft, fCoordTop, fCoordRight, fCoordBottom/*, Op.REPLACE*/);	// Op.REPLACE is no longer supported from sdk 28
		drawDataCurves(canvas, mmapperP2P, mDataSet, mp3CoordLeftBottomInTO.getX(), mp3CoordLeftBottomInTO.getY() - mdCoordHeight,
				mdCoordWidth, mdCoordHeight, paint);
		canvas.restore();     // IMPORTANT: get back to previously saved (unclipped) state of the canvas (restores state #1)

		canvas.save(); // now save again the current state of canvas (clip and matrix) (it's state #2)
		canvas.clipRect(rectClip/*, Op.REPLACE*/);	// Op.REPLACE is no longer supported from sdk 28

		drawLegends(canvas, mdCoordWidth <= mdCoordHeight, paint);

		drawButtons(canvas, x, y, width, height, paint);
		canvas.restore(); // get back go previously saved state (to state #2)

		paint.setColor(nPaintOriginalColor);
		
		Log.e(LOG_TAG, "out of XYChart.draw");
	}
	
	public void updateMapper()	{
		Position3D p3Coord0 = new Position3D(	// coordinate FROM's 0 in TO's coordinate
				mp3CoordLeftBottomInTO.getX() - mp3CoordLeftBottomInFROM.getX() * getScalingRatioX(),
				mp3CoordLeftBottomInTO.getY() - mp3CoordLeftBottomInFROM.getY() * getScalingRatioY()
				);
		mmapperP2P.setLinearMapper(p3Coord0.getX(), p3Coord0.getY(), 0,
								0, 0, 0, 1/getScalingRatioX(), 1/getScalingRatioY(), 1, false);
	}
	
	public void updateCoordAxis()	{
		// TO coordinate is canvas while FROM coordinate is the chart's coordinate. 
		Position3D p3LeftBottomInFROM = mmapperP2P.mapTo2From(mp3CoordLeftBottomInTO);
		Position3D p3RightBottomInTO = new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth,
				mp3CoordLeftBottomInTO.getY());
		Position3D p3RightBottomInFROM = mmapperP2P.mapTo2From(p3RightBottomInTO);	// need not to worry about Z.
		Position3D p3LeftTopInTO = new Position3D(mp3CoordLeftBottomInTO.getX(),
				mp3CoordLeftBottomInTO.getY() - mdCoordHeight);
		Position3D p3LeftTopInFROM = mmapperP2P.mapTo2From(p3LeftTopInTO);	// need not to worry about Z.

		mcaXAxis.mdValueFrom = p3LeftBottomInFROM.getX();
		mcaXAxis.mdValueTo = p3RightBottomInFROM.getX();
		mcaXAxis.mp3From = mp3CoordLeftBottomInTO;
		mcaXAxis.mp3To = p3RightBottomInTO;
		mcaXAxis.mclr = this.mcolorForeGrnd;
		mcaXAxis.mstrAxisName = mstrXAxisName;
		double dXMarkInterval = mdXMark2 - mdXMark1;
		// cannot directly convert to int because mdXMark1 - mcaXAxis.mdValueFrom may overflow int.
		MFPNumeric mfpNumIntTmp = new MFPNumeric((mdXMark1 - mcaXAxis.mdValueFrom)/dXMarkInterval).toIntOrNanInfMFPNum();
		double dXMarkStart = mdXMark1 - mfpNumIntTmp.multiply(new MFPNumeric(dXMarkInterval)).doubleValue();
		if (dXMarkStart < mcaXAxis.mdValueFrom)	{
			dXMarkStart += dXMarkInterval;
		}
		int nNumofMarks = 0;
		if (mcaXAxis.mdValueTo >= dXMarkStart)	{
			nNumofMarks = (int)((mcaXAxis.mdValueTo - dXMarkStart)/dXMarkInterval) + 1;
		}
		int nNumofSigDig = 32;
		if (mcaXAxis.mdValueTo > mcaXAxis.mdValueFrom)	{
			nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(mcaXAxis.mdValueFrom), Math.abs(mcaXAxis.mdValueTo))
						/(mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)*(nNumofMarks + 1))) + 1;
		}
		if (nNumofSigDig > 32)	{
			nNumofSigDig = 32;
		}
		mcaXAxis.marraydScaleMarks = new double[nNumofMarks];
		mcaXAxis.marraystrMarkTxts = new String[nNumofMarks];
		for (int idx = 0; idx < nNumofMarks; idx ++)	{
			mcaXAxis.marraydScaleMarks[idx] = dXMarkStart + idx * dXMarkInterval;
            // use bigDecimal instead of MFPNumeric here so that toString can return like 1.1e30
			mcaXAxis.marraystrMarkTxts[idx] = new BigDecimal(mcaXAxis.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
		}
		
		mcaYAxis.mdValueFrom = p3LeftBottomInFROM.getY();
		mcaYAxis.mdValueTo = p3LeftTopInFROM.getY();
		mcaYAxis.mp3From = mp3CoordLeftBottomInTO;
		mcaYAxis.mp3To = p3LeftTopInTO;
		mcaYAxis.mclr = this.mcolorForeGrnd;
		mcaYAxis.mstrAxisName = mstrYAxisName;
		double dYMarkInterval = mdYMark2 - mdYMark1;
		// cannot directly convert to int because mdYMark1 - mcaYAxis.mdValueFrom may overflow int.
		mfpNumIntTmp = new MFPNumeric((mdYMark1 - mcaYAxis.mdValueFrom)/dYMarkInterval).toIntOrNanInfMFPNum();
		double dYMarkStart = mdYMark1 - mfpNumIntTmp.multiply(new MFPNumeric(dYMarkInterval)).doubleValue();
		if (dYMarkStart < mcaYAxis.mdValueFrom)	{
			dYMarkStart += dYMarkInterval;
		}
		nNumofMarks = 0;
		if (mcaYAxis.mdValueTo >= dYMarkStart)	{
			nNumofMarks = (int)((mcaYAxis.mdValueTo - dYMarkStart)/dYMarkInterval) + 1;
		}
		nNumofSigDig = 32;
		if (mcaYAxis.mdValueTo > mcaYAxis.mdValueFrom)	{
			nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(mcaYAxis.mdValueFrom), Math.abs(mcaYAxis.mdValueTo))
					/(mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)*(nNumofMarks + 1))) + 1;
		}
		if (nNumofSigDig > 32)	{
			nNumofSigDig = 32;
		}
		mcaYAxis.marraydScaleMarks = new double[nNumofMarks];
		mcaYAxis.marraystrMarkTxts = new String[nNumofMarks];
		for (int idx = 0; idx < nNumofMarks; idx ++)	{
			mcaYAxis.marraydScaleMarks[idx] = dYMarkStart + idx * dYMarkInterval;
            // use bigDecimal instead of MFPNumeric here so that toString can return like 1.1e30
			mcaYAxis.marraystrMarkTxts[idx] = new BigDecimal(mcaYAxis.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
		}
		
	}
	
	// calculate coordinate area from screen area
	@Override
	public void calcCoordArea(double x, double y, double width, double height)	{
		double dChartTitleHeight = mdMediumSize;	// although actual character size is mdSmallSize
		double dMarkNameHeight = mdSmallSize;
		double dAxisNameHeight = mdSmallSize;
		double dZoomBtnsHeight = mdLargeSize * 0.85;
		if (width <= height)	{	// Portrait
			mp3CoordLeftBottomInTO = new Position3D(x + dMarkNameHeight * 1.5 + dAxisNameHeight,
												y + height - dZoomBtnsHeight - dMarkNameHeight * 1.5 - dAxisNameHeight, 0);
			mdCoordWidth = width - 0.5 * mdSmallSize - dMarkNameHeight * 1.5 - dAxisNameHeight;
			mdCoordHeight = height - dChartTitleHeight - dZoomBtnsHeight - dMarkNameHeight * 1.5 - dAxisNameHeight;
		} else	{	// landscape
			mp3CoordLeftBottomInTO = new Position3D(x + dMarkNameHeight * 1.5 + dAxisNameHeight,
												y + height - dMarkNameHeight * 1.5 - dAxisNameHeight, 0);
			mdCoordWidth = width - dZoomBtnsHeight - 0.5 * mdSmallSize - dMarkNameHeight * 1.5 - dAxisNameHeight;
			mdCoordHeight = height - dChartTitleHeight - dMarkNameHeight * 1.5 - dAxisNameHeight;
		}
	}
	
	@Override
	public void update()	{
		updateMapper();
		updateCoordAxis();
	}
	
	public void drawGrid(Canvas canvas, Paint paint)	{
		int nPaintOriginalColor = paint.getColor();
		if (mcolorHint != null)	{
			paint.setColor(mcolorHint.getARGB());
		}	// else use paint's original color.
		Position3D[] arrayp3XMarks = new Position3D[mcaXAxis.marraydScaleMarks.length];
		Position3D[] arrayp3XMarksUp = new Position3D[mcaXAxis.marraydScaleMarks.length];
		Position3D[] arrayp3XMarksInTO = new Position3D[mcaXAxis.marraydScaleMarks.length];
		Position3D[] arrayp3XMarksUpInTO = new Position3D[mcaXAxis.marraydScaleMarks.length];
		for (int idx = 0; idx < mcaXAxis.marraydScaleMarks.length; idx ++)	{
			arrayp3XMarks[idx] = new Position3D(mcaXAxis.marraydScaleMarks[idx], mcaYAxis.mdValueFrom);
			arrayp3XMarksUp[idx] = new Position3D(mcaXAxis.marraydScaleMarks[idx], mcaYAxis.mdValueTo);
			arrayp3XMarksInTO[idx] = mmapperP2P.mapFrom2To(arrayp3XMarks[idx]);
			arrayp3XMarksUpInTO[idx] = mmapperP2P.mapFrom2To(arrayp3XMarksUp[idx]);
			float fXMarksInTOX = (float)arrayp3XMarksInTO[idx].getX(),
					fXMarksInTOY = (float)arrayp3XMarksInTO[idx].getY(),
					fXMarksUpInTOX = (float)arrayp3XMarksUpInTO[idx].getX(),
					fXMarksUpInTOY = (float)arrayp3XMarksUpInTO[idx].getY();
			canvas.drawLine(fXMarksInTOX, fXMarksInTOY, fXMarksUpInTOX, fXMarksUpInTOY, paint);
		}
		Position3D[] arrayp3YMarks = new Position3D[mcaYAxis.marraydScaleMarks.length];
		Position3D[] arrayp3YMarksRight = new Position3D[mcaYAxis.marraydScaleMarks.length];
		Position3D[] arrayp3YMarksInTO = new Position3D[mcaYAxis.marraydScaleMarks.length];
		Position3D[] arrayp3YMarksRightInTO = new Position3D[mcaYAxis.marraydScaleMarks.length];
		for (int idx = 0; idx < mcaYAxis.marraydScaleMarks.length; idx ++)	{
			arrayp3YMarks[idx] = new Position3D(mcaXAxis.mdValueFrom, mcaYAxis.marraydScaleMarks[idx]);
			arrayp3YMarksRight[idx] = new Position3D(mcaXAxis.mdValueTo, mcaYAxis.marraydScaleMarks[idx]);
			arrayp3YMarksInTO[idx] = mmapperP2P.mapFrom2To(arrayp3YMarks[idx]);
			arrayp3YMarksRightInTO[idx] = mmapperP2P.mapFrom2To(arrayp3YMarksRight[idx]);
			float fYMarksInTOX = (float)arrayp3YMarksInTO[idx].getX(),
					fYMarksInTOY = (float)arrayp3YMarksInTO[idx].getY(),
					fYMarksRightInTOX = (float)arrayp3YMarksRightInTO[idx].getX(),
					fYMarksRightInTOY = (float)arrayp3YMarksRightInTO[idx].getY();
			canvas.drawLine(fYMarksInTOX, fYMarksInTOY, fYMarksRightInTOX, fYMarksRightInTOY, paint);
		}
		paint.setColor(nPaintOriginalColor);
	}
		
	public void drawLegends(Canvas canvas, boolean bPortrait, Paint paint)	{
		Position3D mp3LegendBoxLeftTop = new Position3D();	// left top point in TO's coordinate
		double dLegendBoxWidth = 0;
		double dLegendBoxHeight = 0;
		double dCvExampleWidth = 0;
		
		int nOriginalPaintColor = paint.getColor();
		Style styleOriginal = paint.getStyle();
		float fPaintOriginalTxtSize = paint.getTextSize();
		
		paint.setColor(mcolorForeGrnd.getARGB());
		paint.setStyle(Style.FILL);
		double dTextSize = mdSmallSize;
		paint.setTextSize((float)dTextSize);
		double dMarginSize = mdVerySmallSize;
		
		if (bPortrait)	{	// Portrait
			dCvExampleWidth = dLegendBoxWidth = 4 * dTextSize;
			mp3LegendBoxLeftTop = new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth - dLegendBoxWidth,
					mp3CoordLeftBottomInTO.getY() - mdCoordHeight);
		} else	{	// Landscape
			dLegendBoxWidth = 8 * dTextSize;
			dCvExampleWidth = 4 * dTextSize;
			mp3LegendBoxLeftTop = new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth - dLegendBoxWidth,
					mp3CoordLeftBottomInTO.getY() - mdCoordHeight);
		}
		
		double dCurrentLegendStart = mp3LegendBoxLeftTop.getY() + dMarginSize;
		double dLastLegendEnd = dCurrentLegendStart;
		for (int idx = 0; idx < mDataSet.size(); idx ++)	{
            if (mDataSet.get(idx).mstrName == null || mDataSet.get(idx).mstrName.length() == 0) {
                continue;   // do not show empty legend.
            }
			double dSize = mDataSet.get(idx).mpointStyle.mdSize;
			Position3D p3Point = new Position3D(mp3LegendBoxLeftTop.getX() + dCvExampleWidth/2,
					dCurrentLegendStart + Math.max(dSize, dTextSize)/2);
			Position3D p3LineEnd1 = new Position3D(mp3LegendBoxLeftTop.getX() + dMarginSize,
					dCurrentLegendStart + Math.max(dSize, dTextSize)/2);
			Position3D p3LineEnd2 = new Position3D(mp3LegendBoxLeftTop.getX() + dCvExampleWidth - dMarginSize,
					dCurrentLegendStart + Math.max(dSize, dTextSize)/2);
			drawPoint(canvas, p3Point, paint, mDataSet.get(idx).mpointStyle);
			drawLine(canvas, p3LineEnd1, p3LineEnd2, paint, mDataSet.get(idx).mlineStyle);
			if (bPortrait)	{	// Portrait
				double dCvNameWidth = dLegendBoxWidth - 2 * dMarginSize;
				String strFittedName = getFitText(mDataSet.get(idx).mstrName, dCvNameWidth, paint);
				draw1LineTextAnchorMid(canvas, strFittedName,
						mp3LegendBoxLeftTop.getX() + dMarginSize + dCvNameWidth / 2,
						dCurrentLegendStart + Math.max(dSize, dTextSize) + dTextSize/2,
						paint, null, dTextSize, 0);
				dLastLegendEnd = dCurrentLegendStart + Math.max(dSize, dTextSize) + dTextSize;
				dCurrentLegendStart = dLastLegendEnd + dMarginSize;
			} else	{
				double dCvNameWidth = dLegendBoxWidth - dCvExampleWidth - dMarginSize;
				String strFittedName = getFitText(mDataSet.get(idx).mstrName, dCvNameWidth, paint);
				draw1LineTextAnchorMid(canvas, strFittedName,
						mp3LegendBoxLeftTop.getX() + dCvExampleWidth + dCvNameWidth / 2,
						dCurrentLegendStart + dTextSize/2,
						paint, null, dTextSize, 0);
				dLastLegendEnd = dCurrentLegendStart + Math.max(dSize, dTextSize);
				dCurrentLegendStart = dLastLegendEnd + dMarginSize;
			}
			if (dCurrentLegendStart >= (mp3LegendBoxLeftTop.getY() + mdCoordHeight*2/3 - dMarginSize)
					&& idx <= (mDataSet.size() - 2))	{
				// there are some more curves but no space to show their legend
				draw1LineTextAnchorMid(canvas, "... ...",
						mp3LegendBoxLeftTop.getX() + dLegendBoxWidth / 2,
						dCurrentLegendStart + dTextSize/2,
						paint, null, dTextSize, 0);
				dLastLegendEnd = dCurrentLegendStart + dTextSize;
				break;
			}
		}
		
		dLegendBoxHeight = dLastLegendEnd + dMarginSize - mp3LegendBoxLeftTop.getY();;
		paint.setColor(new com.cyzapps.VisualMFP.Color(64,	// transparent background color
				mcolorForeGrnd.mnR, mcolorForeGrnd.mnG, mcolorForeGrnd.mnB).getARGB());
		float fLegendBoxLeft = (float)mp3LegendBoxLeftTop.getX(), fLegendBoxTop = (float)mp3LegendBoxLeftTop.getY(),
				fLegendBoxRight = (float)(mp3LegendBoxLeftTop.getX() + dLegendBoxWidth),
				fLegendBoxBottom = (float)(mp3LegendBoxLeftTop.getY() + dLegendBoxHeight);
		canvas.drawRect(fLegendBoxLeft, fLegendBoxTop, fLegendBoxRight, fLegendBoxBottom, paint);

		paint.setTextSize(fPaintOriginalTxtSize);
		paint.setColor(nOriginalPaintColor);
		paint.setStyle(styleOriginal);
	}
	
	@Override
	public boolean isZoomInEnabled()	{
		if (Math.abs(getScalingRatioX()) >= MAX_SCALING_RATIO || Math.abs(getScalingRatioY()) >= MAX_SCALING_RATIO)	{
			return false;
		} else	{
			return true;
		}
	}
	
	@Override
	public boolean isZoomOutEnabled()	{
		if (Math.abs(getScalingRatioX()) <= MIN_SCALING_RATIO || Math.abs(getScalingRatioY()) <= MIN_SCALING_RATIO)	{
			return false;
		} else	{
			return true;
		}
	}
	
	public boolean canZoom(double dXRatio, double dYRatio)	{
		if (Math.abs(getScalingRatioX() * dXRatio) >= MAX_SCALING_RATIO || Math.abs(getScalingRatioX() * dXRatio) <= MIN_SCALING_RATIO
				|| Math.abs(getScalingRatioY() * dYRatio) >= MAX_SCALING_RATIO || Math.abs(getScalingRatioY() * dYRatio) <= MIN_SCALING_RATIO)	{
			return false;
		} else	{
			return true;
		}
	}
		
	@Override
	public void zoom(double dXRatio, double dYRatio)	{
		if (canZoom(dXRatio, dYRatio) == false)	{
			return;
		}
		
		Position3D p3CoordCentreInTO = new Position3D(
				mp3CoordLeftBottomInTO.getX() + mdCoordWidth/2,
				mp3CoordLeftBottomInTO.getY() - mdCoordHeight/2);
		Position3D p3CoordCentreInFROM = mmapperP2P.mapTo2From(p3CoordCentreInTO);
		
		
		setScalingRatioX(getScalingRatioX() * dXRatio);
		setScalingRatioY(getScalingRatioY() * dYRatio);
		mp3CoordLeftBottomInFROM = new Position3D(
				p3CoordCentreInFROM.getX() - mdCoordWidth/2/Math.abs(getScalingRatioX()),
				p3CoordCentreInFROM.getY() - mdCoordHeight/2/Math.abs(getScalingRatioY()));
		
		updateMapper();
		
		if (mbUseInit2CalibrateZoom)	{	// otherwise, do not change mdXMark1, mdXMark2, mdYMark1, mdYMark2
			double dOverAllZoomRatioX = getScalingRatioX()/getSavedScalingRatioX();
			double dTmp = Math.pow(10, (int)Math.log10(dOverAllZoomRatioX));
			double dTmp1 = dOverAllZoomRatioX/dTmp;	// always between (10, 0.1)
			if (dTmp1 >= 7.5)	{
				dOverAllZoomRatioX = dTmp * 10;
			} else if (dTmp1 >= 3.5)	{
				dOverAllZoomRatioX = dTmp * 5;
			} else if (dTmp1 >= 1.5)	{
				dOverAllZoomRatioX = dTmp * 2;
			} else if (dTmp1 >= 0.75)	{
				dOverAllZoomRatioX = dTmp;
			} else if (dTmp1 >= 0.35)	{
				dOverAllZoomRatioX = dTmp * 0.5;
			} else if (dTmp1 >= 0.15)	{
				dOverAllZoomRatioX = dTmp * 0.2;
			} else	{
				dOverAllZoomRatioX = dTmp * 0.1;
			}
			mdXMark1 = mdSavedXMark1;
			mdXMark2 = (mdSavedXMark2 - mdSavedXMark1) / dOverAllZoomRatioX + mdSavedXMark1;
			
			double dOverAllZoomRatioY = getScalingRatioY()/getSavedScalingRatioY();
			dTmp = Math.pow(10, (int)Math.log10(dOverAllZoomRatioY));
			dTmp1 = dOverAllZoomRatioY/dTmp;	// always between (10, 0.1)
			if (dTmp1 >= 7.5)	{
				dOverAllZoomRatioY = dTmp * 10;
			} else if (dTmp1 >= 3.5)	{
				dOverAllZoomRatioY = dTmp * 5;
			} else if (dTmp1 >= 1.5)	{
				dOverAllZoomRatioY = dTmp * 2;
			} else if (dTmp1 > 0.75)	{
				dOverAllZoomRatioY = dTmp;
			} else if (dTmp1 > 0.35)	{
				dOverAllZoomRatioY = dTmp * 0.5;
			} else if (dTmp1 > 0.15)	{
				dOverAllZoomRatioY = dTmp * 0.2;
			} else	{
				dOverAllZoomRatioY = dTmp * 0.1;
			}
			mdYMark1 = mdSavedYMark1;
			mdYMark2 = (mdSavedYMark2 - mdSavedYMark1) / dOverAllZoomRatioY + mdSavedYMark1;
		}
		updateCoordAxis();
	}

	@Override
	public void zoom1To1()	{
		if (Math.abs(getScalingRatioX()) < Math.abs(getScalingRatioY()))	{
			zoom(1, Math.abs(getScalingRatioX()/getScalingRatioY()));
		} else if (Math.abs(getScalingRatioX()) > Math.abs(getScalingRatioY()))	{
			zoom(Math.abs(getScalingRatioY()/getScalingRatioX()), 1);
		}	// if equal, do nothing.
	}
	
	@Override
	public void zoomReset()	{
		if (mbUseInit2CalibrateZoom)	{
			restoreSettings();
			update();
		}
	}
	
	@Override
	public void slide(double dOldX, double dOldY, double dNewX, double dNewY)	{
		mp3CoordLeftBottomInFROM = new Position3D(mp3CoordLeftBottomInFROM.getX() - (dNewX - dOldX) / getScalingRatioX(),
				mp3CoordLeftBottomInFROM.getY() - (dNewY - dOldY) / getScalingRatioY());
		update();
	}
	
	@Override
	public void initialize()	{
		super.initialize();
	}
}
