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
import java.math.MathContext;

import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.VisualMFP.CoordAxis;
import com.cyzapps.VisualMFP.PolarXYMapper;
import com.cyzapps.VisualMFP.Position3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.util.Log;

/**
 * The Polar chart rendering class.
 */
public class PolarChart extends XYChart {
	
	public static final double MAX_SCALING_RATIO = 256;
	
	// FROM coordinate: chart's coordinate, in from coordinate, x is r, y is theta.
	// TO coordinate: Android's coordinate
	protected boolean mbIsInitialization = true;
	protected double mdXAxisLenInTO = 0;
	protected double mdSavedXAxisLenInTO = mdXAxisLenInTO;	// this means it hasn't been initialized yet.
	
	// x axis is R axis and y axis is angle axis.
	
	//mp3CoordOriginInFROM is not needed coz it is always (0,0).
	//protected Position3D mp3CoordOriginInFROM = new Position3D();	// FROM's coordinate of origin point of FROM's coordinate
	protected Position3D mp3CoordOriginInTO = new Position3D();	// TO's coordinate of origin point of FROM's coordinate
	
	protected PolarXYMapper mmapperPolarXY = new PolarXYMapper();
		
	public double getScalingRatio()	{
		return mdXAxisLenInTO / mdXAxisLenInFROM;
	}
	public void setScalingRatio(double dScalingRatio)	{
		mdXAxisLenInFROM = mdXAxisLenInTO / dScalingRatio;
	}
	
	public String mstrXAxisName = "r";
	
	public String mstrYAxisName = "angle";
	
	public Position3D mp3SavedCoordOriginInTO = mp3CoordOriginInTO;	// original coordinate of FROM's origin point in TO

	public double getSavedScalingRatio()	{	// scaling ratio = length of 1 unit in FROM / length of 1 unit in TO = TO total length / FROM total length
		return mdSavedXAxisLenInTO / mdSavedXAxisLenInFROM;
	}

	public PolarChart(Context context) {
		super(context);

	}
	
	@Override
	public void saveSettings()	{
		mp3SavedCoordOriginInTO = mp3CoordOriginInTO;
		mdSavedXAxisLenInFROM = mdXAxisLenInFROM;
		mdSavedXAxisLenInTO = mdXAxisLenInTO;
		mdSavedXMark1 = mdXMark1;
		mdSavedXMark2 = mdXMark2;
	}

	@Override
	public void restoreSettings()	{
		mp3CoordOriginInTO = mp3SavedCoordOriginInTO;
		mdXAxisLenInFROM = mdSavedXAxisLenInFROM;
		mdXAxisLenInTO = mdSavedXAxisLenInTO;
		mdXMark1 = mdSavedXMark1;
		mdXMark2 = mdSavedXMark2;
	}

	@Override
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
				Position3D p3ScaleMarkInFROM = new Position3D(caAxis.marraydScaleMarks[idx], 0);
				Position3D p3ScaleMarkInTO = mmapperPolarXY.mapFrom2To(p3ScaleMarkInFROM);
				dX = p3ScaleMarkInTO.getX();
				dY = p3ScaleMarkInTO.getY();
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
	
    @Override
    public void drawButtons(Canvas canvas, double left, double top, double width, double height, Paint paint)  {
		int nOriginalPaintColor = paint.getColor();
		Style styleOriginal = paint.getStyle();
		paint.setColor(mnCfgZoomBtnsBkcolor);
		paint.setStyle(Style.STROKE);
		
		double dZoomButtonSize = mdLargeSize;
		int nNumOfButtons = 3;
		if (this instanceof PolarExprChart)	{
			nNumOfButtons = 4;
		}
		// mfMediumSize and mfLargeSize should be determined by Screen size.
		// dZoomButtonSize = Math.max(dZoomButtonSize, Math.min(width, height) / 7);
		if (width <= height)	{	// Portrait
			mrectZoomR.set((float)(left + width - dZoomButtonSize * nNumOfButtons),
					(float)(top + height - dZoomButtonSize * 0.7),
					(float)(left + width),
					(float)(top + height));
		} else	{	// Landscape
			mrectZoomR.set((float)(left /*+ width - dZoomButtonSize * 0.7*/),   // polar chart is a bit different from xy chart, buttons on right if landscape
					(float)(top + height / 2 - dZoomButtonSize * nNumOfButtons / 2.0),
					(float)(left /*+ width */+ dZoomButtonSize * 0.7),
					(float)(top + height / 2 + dZoomButtonSize * nNumOfButtons / 2.0));
		}
		canvas.drawRoundRect(mrectZoomR, (float)(dZoomButtonSize / 3), (float)(dZoomButtonSize / 3), paint);
		Bitmap cfgImage, zoomInImage, zoomOutImage, fitZoomImage;
		// height of the bitmap is determined by device density and original bitmap height
		// device density / original bitmap stored density * original bitmap height
		double dIconSize = mzoomInImage_24.getHeight();
		if (dZoomButtonSize <= mzoomInImage_24.getHeight() * 3)	{	// was 24 * 3
			dIconSize = mzoomInImage_24.getHeight();
			cfgImage = mcfgImage_24;
			zoomInImage = mzoomInImage_24;
			zoomOutImage = mzoomOutImage_24;
			fitZoomImage = mfitZoomImage_24;
		} else if (dZoomButtonSize <= mzoomInImage_32.getHeight() * 3)	{	// was 32 * 3
			dIconSize = mzoomInImage_32.getHeight();
			cfgImage = mcfgImage_32;
			zoomInImage = mzoomInImage_32;
			zoomOutImage = mzoomOutImage_32;
			fitZoomImage = mfitZoomImage_32;
		} else if (dZoomButtonSize <= mzoomInImage_48.getHeight() * 3)	{	// was 48 * 3
			dIconSize = mzoomInImage_48.getHeight();
			cfgImage = mcfgImage_48;
			zoomInImage = mzoomInImage_48;
			zoomOutImage = mzoomOutImage_48;
			fitZoomImage = mfitZoomImage_48;
		} else	{
			dIconSize = mzoomInImage_64.getHeight();
			cfgImage = mcfgImage_64;
			zoomInImage = mzoomInImage_64;
			zoomOutImage = mzoomOutImage_64;
			fitZoomImage = mfitZoomImage_64;
		}
		if (width <= height)	{	// Portrait
			mbLandScapeMode = false;
			double dButtonY = mrectZoomR.top + (dZoomButtonSize * 0.7 - dIconSize)/2.0;
			double dHShiftRatio = 0.25;
			if (this instanceof PolarExprChart)	{
				canvas.drawBitmap(cfgImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
				dHShiftRatio ++;
			}
			canvas.drawBitmap(zoomInImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
			canvas.drawBitmap(zoomOutImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
			canvas.drawBitmap(fitZoomImage, (float)(mrectZoomR.left + dZoomButtonSize * dHShiftRatio), (float)dButtonY, null);
			dHShiftRatio ++;
		} else	{	// landscape
			mbLandScapeMode = true;
			double dButtonX = mrectZoomR.left + (dZoomButtonSize * 0.7 - dIconSize)/2.0;
			double dVShiftRatio = 0.25;
			if (this instanceof PolarExprChart)	{
				canvas.drawBitmap(cfgImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
				dVShiftRatio ++;
			}
			canvas.drawBitmap(zoomInImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
			canvas.drawBitmap(zoomOutImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
			canvas.drawBitmap(fitZoomImage, (float)dButtonX, (float)(mrectZoomR.top + dZoomButtonSize * dVShiftRatio), null);
			dVShiftRatio ++;
		}
    	
		paint.setColor(nOriginalPaintColor);
		paint.setStyle(styleOriginal);
    }
    	
	@Override
	public void draw(Canvas canvas, double x, double y, double width, double height, Paint paint) {
		Log.e(LOG_TAG, "in PolarChart.draw");
		
		//calcCoordArea(x, y, width, height);
		//update();

		int nPaintOriginalColor = paint.getColor();
		// draw background
		paint.setColor(mcolorBkGrnd.getARGB());
		drawBackground(canvas, x, y, width, height, paint, null);
		
		paint.setColor(mcolorForeGrnd.getARGB());
		// draw title
		draw1LineTextAnchorMid(canvas, mstrChartTitle, width / 2, y + mdSmallSize/2, paint, null, mdSmallSize, 0);
		
		// draw r-axis only if it is visible.
		if (mp3CoordOriginInTO.getX() <= mp3CoordLeftBottomInTO.getX() + mdCoordWidth)	{
			drawAxis(canvas, mcaXAxis, mp3CoordOriginInTO.getX(), mp3CoordOriginInTO.getY(),
					mp3CoordLeftBottomInTO.getX() + mdCoordWidth, mp3CoordOriginInTO.getY(),
					paint, mcolorForeGrnd, false);
		}
		
		// draw a rectangle for drawing area
		float fCoordLeft = (float)mp3CoordLeftBottomInTO.getX(),
				fCoordTop = (float)(mp3CoordLeftBottomInTO.getY() - mdCoordHeight),
				fCoordRight = (float)(mp3CoordLeftBottomInTO.getX() + mdCoordWidth),
				fCoordBottom = (float)mp3CoordLeftBottomInTO.getY();
		
		Rect rectClip = canvas.getClipBounds();
		canvas.clipRect(fCoordLeft, fCoordTop, fCoordRight, fCoordBottom, Op.REPLACE);
		if (mbShowGrid)	{
			drawGrid(canvas, paint);
		}
		
		drawDataCurves(canvas, mmapperPolarXY, mDataSet, fCoordLeft, fCoordTop, mdCoordWidth, mdCoordHeight, paint);
		canvas.clipRect(rectClip, Op.REPLACE);
		
		drawLegends(canvas, mdCoordWidth <= mdCoordHeight, paint);
		
        drawButtons(canvas, x, y, width, height, paint);

		paint.setColor(nPaintOriginalColor);
		
		Log.e(LOG_TAG, "out of PolarChart.draw");
	}
	
	public void updateMapper()	{
		mmapperPolarXY.setPolarXYMapper(mp3CoordOriginInTO, getScalingRatio());;
	}
	
	public double calcRMaxLenInTO(Position3D p3OriginInTO)	{
		double dRMaxLenInTO = 0;
		double dLeftBottomToOriginInTO = p3OriginInTO.getDistance(mp3CoordLeftBottomInTO);
		if (dRMaxLenInTO < dLeftBottomToOriginInTO)	{
			dRMaxLenInTO = dLeftBottomToOriginInTO;
		}
		Position3D p3CoordLeftTopInIO = new Position3D(mp3CoordLeftBottomInTO.getX(),
													mp3CoordLeftBottomInTO.getY() - mdCoordHeight);
		double dLeftTopToOriginInTO = p3OriginInTO.getDistance(p3CoordLeftTopInIO);
		if (dRMaxLenInTO < dLeftTopToOriginInTO)	{
			dRMaxLenInTO = dLeftTopToOriginInTO;
		}
		Position3D p3CoordRightBottomInIO = new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth,
													mp3CoordLeftBottomInTO.getY());
		double dRightBottomToOriginInTO = p3OriginInTO.getDistance(p3CoordRightBottomInIO);
		if (dRMaxLenInTO < dRightBottomToOriginInTO)	{
			dRMaxLenInTO = dRightBottomToOriginInTO;
		}
		Position3D p3CoordRightTopInIO = new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth,
													mp3CoordLeftBottomInTO.getY() - mdCoordHeight);
		double dRightTopToOriginInTO = p3OriginInTO.getDistance(p3CoordRightTopInIO);
		if (dRMaxLenInTO < dRightTopToOriginInTO)	{
			dRMaxLenInTO = dRightTopToOriginInTO;
		}
		return dRMaxLenInTO;
	}
	
	public void updateRMaxLenInTO()	{
		// update mdXAxisLenInTO
		mdXAxisLenInTO = calcRMaxLenInTO(mp3CoordOriginInTO);
	}
	
	public void updateRMaxLenInFROM(double dScalingRatio)	{
		// updatemdXAxisLenInFROM
		setScalingRatio(dScalingRatio);
	}
	
	public void updateCoordAxis()	{
		// TO coordinate is canvas while FROM coordinate is the chart's coordinate. 
		/*// do not need to calculate dVisibleRRangeTo coz it is mdXAxisLenInFROM.
		Position3D p3CoordLeftBottomInFROM = mmapperPolarXY.mapTo2From(mp3CoordLeftBottomInTO);
		dVisibleRRangeTo = (dVisibleRRangeTo < p3CoordLeftBottomInFROM.getX())?p3CoordLeftBottomInFROM.getX():dVisibleRRangeTo;
		Position3D p3CoordLeftTopInFROM = mmapperPolarXY.mapTo2From(
				new Position3D(mp3CoordLeftBottomInTO.getX(),
				mp3CoordLeftBottomInTO.getY() - mdCoordHeight));
		dVisibleRRangeTo = (dVisibleRRangeTo < p3CoordLeftTopInFROM.getX())?p3CoordLeftTopInFROM.getX():dVisibleRRangeTo;
		Position3D p3CoordRightBottomInFROM = mmapperPolarXY.mapTo2From(
				new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth,
				mp3CoordLeftBottomInTO.getY()));
		dVisibleRRangeTo = (dVisibleRRangeTo < p3CoordRightBottomInFROM.getX())?p3CoordRightBottomInFROM.getX():dVisibleRRangeTo;
		Position3D p3CoordRightTopInFROM = mmapperPolarXY.mapTo2From(
				new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth,
				mp3CoordLeftBottomInTO.getY() - mdCoordHeight));
		dVisibleRRangeTo = (dVisibleRRangeTo < p3CoordRightTopInFROM.getX())?p3CoordRightTopInFROM.getX():dVisibleRRangeTo;
		*/
		
		mcaXAxis.mdValueFrom = 0;	// force it to be 0.
		mcaXAxis.mdValueTo = mdXAxisLenInFROM;
		mcaXAxis.mp3From = mmapperPolarXY.mapFrom2To(new Position3D(mcaXAxis.mdValueFrom, 0));
		mcaXAxis.mp3To = mmapperPolarXY.mapFrom2To(new Position3D(mcaXAxis.mdValueTo, 0));
		mcaXAxis.mclr = this.mcolorForeGrnd;
		mcaXAxis.mstrAxisName = mstrXAxisName;
		double dRMarkInterval = mdXMark2 - mdXMark1;
		// cannot directly convert to int because mdXMark1 - mcaXAxis.mdValueFrom may overflow int.
		MFPNumeric mfpNumIntTmp = new MFPNumeric((mdXMark1 - mcaXAxis.mdValueFrom)/dRMarkInterval).toIntOrNanInfMFPNum();
		double dRMarkStart = mdXMark1 - mfpNumIntTmp.multiply(new MFPNumeric(dRMarkInterval)).doubleValue();
		if (dRMarkStart < mcaXAxis.mdValueFrom)	{
			dRMarkStart += dRMarkInterval;
		}
		int nNumofMarks = 0;
		if (mcaXAxis.mdValueTo >= dRMarkStart)	{
			nNumofMarks = (int)Math.ceil((mcaXAxis.mdValueTo - dRMarkStart)/dRMarkInterval) + 1;
		}
		int nNumofSigDig = 32;
		if (mcaXAxis.mdValueTo > mcaXAxis.mdValueFrom)	{
			nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(mcaXAxis.mdValueFrom), Math.abs(mcaXAxis.mdValueTo))
						/(mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)*(nNumofMarks + 1))) + 1;
		}
		if (nNumofSigDig > 32)	{
			nNumofSigDig = 32;
		}
		
		double dDiagonalInFrom = Math.sqrt(mdCoordWidth * mdCoordWidth + mdCoordHeight * mdCoordHeight)/getScalingRatio();
		int nMaxNumofMarksAlongDiagonal = (int)Math.ceil(dDiagonalInFrom/dRMarkInterval) + 1;
		int nShrinkedNumofMarks = (nNumofMarks < nMaxNumofMarksAlongDiagonal)?nNumofMarks:nMaxNumofMarksAlongDiagonal;
		mcaXAxis.marraydScaleMarks = new double[nShrinkedNumofMarks];
		mcaXAxis.marraystrMarkTxts = new String[nShrinkedNumofMarks];
		for (int idx = nShrinkedNumofMarks - 1; idx >= 0; idx --)	{
			mcaXAxis.marraydScaleMarks[idx] = dRMarkStart + (idx + nNumofMarks - nShrinkedNumofMarks) * dRMarkInterval;
            // use BigDecimal not MFPNumeric here so that toString is able to output like 1.1e3
			mcaXAxis.marraystrMarkTxts[idx] = new BigDecimal(mcaXAxis.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
		}

		// do not dynamically update Y Axis (angle axis)
	}
	
	public boolean canUpdate(Position3D p3NewOrigin)	{
		double dOriginToLeftTopLen = p3NewOrigin.getDistance(new Position3D(0,0));
		if (dOriginToLeftTopLen > (mdCoordWidth + mdCoordHeight) * MAX_SCALING_RATIO)	{
			return false;
		}
		return true;
	}
	
	@Override
	public void update()	{
		updateMapper();
		double dOriginalScalingRatio = getScalingRatio();
		updateRMaxLenInTO();
		updateRMaxLenInFROM(dOriginalScalingRatio);
		updateCoordAxis();
	}
	
	// calculate coordinate area from screen area
	@Override
	public void calcCoordArea(double x, double y, double width, double height)	{
		mp3CoordLeftBottomInTO = new Position3D(x, y + height);
		mdCoordWidth = width;
		mdCoordHeight = height;
		
		if (mbIsInitialization)	{
			mp3CoordOriginInTO = new Position3D(mp3CoordLeftBottomInTO.getX() + mdCoordWidth/2.0,
														mp3CoordLeftBottomInTO.getY() - mdCoordHeight/2.0);
		
			updateRMaxLenInTO();
			// mdSavedXAxisLenInTO is very speical here. It actually should be initialized in createChart
			// function, not here because here, i.e. in resize function, is called at orientation change
			// while mdSavedXAxisLenInTO should not be initialized twice. However, polar chart cannot use
			// mdCoordWidth/mdXAisLenInFROM to obtain ratio, as such hwe have to use mdXAxisLenInTO, but
			// mdXAxisLenInTO cannot be valued in createChart function, so has to initialize mdSavedXAxisLenInTO
			// here. To guarantee only one initialization, use mbIsInitialization as a flag. This situation
			// is the same as mp3CoordOriginInTO.
			saveSettings();
			mbIsInitialization = false;
		}
	}

	public void drawGrid(Canvas canvas, Paint paint)	{
		int nPaintOriginalColor = paint.getColor();
		if (mcolorHint != null)	{
			paint.setColor(mcolorHint.getARGB());
		}	// else use paint's original color.
				
		Style styleOriginal = paint.getStyle();
		paint.setStyle(Style.STROKE);
		for (int idx = 0; idx < mcaXAxis.marraydScaleMarks.length; idx ++)	{
			double dOvalR = Math.abs(mcaXAxis.marraydScaleMarks[idx]) * getScalingRatio();
			double dX0 = mp3CoordOriginInTO.getX();
			double dY0 = mp3CoordOriginInTO.getY();
			if (dOvalR <= Math.min(mdCoordHeight, mdCoordWidth))	{
				float fX0 = (float)dX0;
				float fY0 = (float)dY0;
				float fOvalR = (float)dOvalR;
				canvas.drawCircle(fX0, fY0, fOvalR, paint);
			} else	{
				int nDrawArcStep = 2;
				for (int idx1 = 0; idx1 < 360; idx1 = idx1 + nDrawArcStep)	{
					
					Position3D p31 = new Position3D(dOvalR * Math.cos(idx1 * Math.PI/180.0) + dX0,
							dOvalR * Math.sin(idx1 * Math.PI/180.0) + dY0);
					Position3D p32 = new Position3D(dOvalR * Math.cos((idx1 + nDrawArcStep) * Math.PI/180.0) + dX0,
							dOvalR * Math.sin((idx1 + nDrawArcStep) * Math.PI/180.0) + dY0);
					
					Position3D[] p3sInRange = findInRangeLine(0, mdCoordWidth, 0, mdCoordHeight, p31, p32);
					if (p3sInRange.length == 2)	{
						float fArcXStart = (float) (p3sInRange[0].getX());
						float fArcYStart = (float) (p3sInRange[0].getY());
						float fArcXEnd = (float) (p3sInRange[1].getX());
						float fArcYEnd = (float) (p3sInRange[1].getY());
						canvas.drawLine(fArcXStart, fArcYStart, fArcXEnd, fArcYEnd, paint);
					}
					/*
					float fArcXStart = (float) (p31.getX());
					float fArcYStart = (float) (p31.getY());
					float fArcXEnd = (float) (p32.getX());
					float fArcYEnd = (float) (p32.getY());
					canvas.drawLine(fArcXStart, fArcYStart, fArcXEnd, fArcYEnd, paint);
					*/
				}
			}
		}
		paint.setStyle(styleOriginal);
		
		for (int idx = 0; idx < 360; idx = idx + 30)	{
			double dAngle = idx * Math.PI/180.0;
			double dRNear = mcaXAxis.mdValueFrom;
			double dRFar = mcaXAxis.mdValueTo;
			Position3D p3NearPnt = mmapperPolarXY.mapFrom2To(new Position3D(dRNear, dAngle));
			Position3D p3FarPnt = mmapperPolarXY.mapFrom2To(new Position3D(dRFar, dAngle));
			canvas.drawLine((float)p3NearPnt.getX(), (float)p3NearPnt.getY(), (float)p3FarPnt.getX(), (float)p3FarPnt.getY(), paint);
		}

		paint.setColor(nPaintOriginalColor);
	}
	
	@Override
	public void zoom(double dXRatio, double dYRatio)	{
		if (dXRatio <= 0)	{
			dXRatio = 1;
		}
		if (dYRatio <= 0)	{
			dYRatio = 1;
		}
		double dRatio = Math.sqrt(dXRatio * dYRatio);

		Position3D p3CoordCentreInTO = new Position3D(
				mp3CoordLeftBottomInTO.getX() + mdCoordWidth/2,
				mp3CoordLeftBottomInTO.getY() - mdCoordHeight/2);
		Position3D p3CoordCentreInFROM = mmapperPolarXY.mapTo2From(p3CoordCentreInTO);

		Position3D p3NewOriginInFROMB4Zoom = new Position3D(p3CoordCentreInFROM.getX() * (1 - dRatio), p3CoordCentreInFROM.getY());
		Position3D p3NewCoordOriginInTO = mmapperPolarXY.mapFrom2To(p3NewOriginInFROMB4Zoom);
		
		if (!canUpdate(p3NewCoordOriginInTO))	{
			return;
		}
		mp3CoordOriginInTO = p3NewCoordOriginInTO;
	
		setScalingRatio(getScalingRatio() * dRatio);
		
		updateMapper();
		
		if (mbUseInit2CalibrateZoom)	{	// otherwise, do not change mdRMark1, mdRMark2, mdAlphaMark1, mdAlphaMark2
			double dOverAllZoomRatio = getScalingRatio()/getSavedScalingRatio();
			double dTmp = Math.pow(10, (int)Math.log10(dOverAllZoomRatio));
			double dTmp1 = dOverAllZoomRatio/dTmp;	// always between (10, 0.1)
			if (dTmp1 >= 7.5)	{
				dOverAllZoomRatio = dTmp * 10;
			} else if (dTmp1 >= 3.5)	{
				dOverAllZoomRatio = dTmp * 5;
			} else if (dTmp1 >= 1.5)	{
				dOverAllZoomRatio = dTmp * 2;
			} else if (dTmp1 >= 0.75)	{
				dOverAllZoomRatio = dTmp;
			} else if (dTmp1 >= 0.35)	{
				dOverAllZoomRatio = dTmp * 0.5;
			} else if (dTmp1 >= 0.15)	{
				dOverAllZoomRatio = dTmp * 0.2;
			} else	{
				dOverAllZoomRatio = dTmp * 0.1;
			}
			mdXMark1 = mdSavedXMark1;
			mdXMark2 = (mdSavedXMark2 - mdSavedXMark1) / dOverAllZoomRatio + mdSavedXMark1;
			
			// angle marks do not change.
		}
		
		double dScalingRatio = getScalingRatio();
		updateRMaxLenInTO();
		updateRMaxLenInFROM(dScalingRatio);

		updateCoordAxis();
	}

	@Override
	public void slide(double dOldX, double dOldY, double dNewX, double dNewY)	{
		Position3D p3NewCoordOriginInTO = new Position3D(mp3CoordOriginInTO.getX() + (dNewX - dOldX),
				mp3CoordOriginInTO.getY() + (dNewY - dOldY));
		if (canUpdate(p3NewCoordOriginInTO))	{
			mp3CoordOriginInTO = p3NewCoordOriginInTO;
			update();
		}
	}
}
