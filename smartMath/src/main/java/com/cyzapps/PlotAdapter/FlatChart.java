package com.cyzapps.PlotAdapter;

import java.io.Serializable;
import java.util.LinkedList;

import com.cyzapps.SmartMath.R;
import com.cyzapps.VisualMFP.CoordAxis;
import com.cyzapps.VisualMFP.DataSeriesCurve;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.MathLib;
import com.cyzapps.VisualMFP.PointMapper;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.PointStyle.POINTSHAPE;
import com.cyzapps.VisualMFP.Position3D;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.Log;

public class FlatChart extends MFPChart {
	public static final String LOG_TAG = "com.apps.cyzsoft.PlotAdapter.FlatChart";
	
	public String mstrChartTitle = "";
	
	public final double MAX_SCALING_RATIO = 1.0e32;
	public final double MIN_SCALING_RATIO = 1.0e-32;
	
	public double mdVeryTinySize = 2;
	public double mdTinySize = 4;
	public double mdVerySmallSize = 8;
	public double mdSmallSize = 16;
	public double mdMediumSize = 32;
	public double mdLargeSize = 64;
	public double mdVeryLargeSize = 128;
	public double mdHugeSize = 256;
	public double mdVeryHugeSize = 512;
	
	public double mdArrowAngle = Math.PI/3;
	public double mdSpaceBtwTxtLines = 5;
	
	public com.cyzapps.VisualMFP.Color mcolorBkGrnd = new com.cyzapps.VisualMFP.Color();
	
	// default foreground color
	public com.cyzapps.VisualMFP.Color mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 255, 255);
	
	public boolean mbUseInit2CalibrateZoom = false;
	
	public boolean mbCfgWindowStarted = false;
	
	/** The config icon. */
	public Bitmap mcfgImage_24, mcfgImage_32, mcfgImage_48, mcfgImage_64;
	/** The zoom in icon. */
	public Bitmap mzoomInImage_24, mzoomInImage_32, mzoomInImage_48, mzoomInImage_64;
	/** The zoom out icon. */
	public Bitmap mzoomOutImage_24, mzoomOutImage_32, mzoomOutImage_48, mzoomOutImage_64;
	/** The fit zoom icon. */
	public Bitmap mfitZoomImage_24, mfitZoomImage_32, mfitZoomImage_48, mfitZoomImage_64;
	/** The xy1To1 zoom icon. */
	public Bitmap mxy1To1ZoomImage_24, mxy1To1ZoomImage_32, mxy1To1ZoomImage_48, mxy1To1ZoomImage_64;
	/** The zoom buttons background color. */
	public int mnCfgZoomBtnsBkcolor = Color.argb(175, 150, 150, 150);
	/** The zoom buttons rectangle. */
	public RectF mrectZoomR = new RectF();

	public boolean mbLandScapeMode = false;
	  
	public FlatChart(Context context)	{
		super(context);
		initialize();
	}	
	
	/**
	 * Calculates the best text to fit into the available space.
	 */
	public static String getFitText(String text, double width, Paint paint) {
		String newText = text;
		int length = text.length();
		int diff = 0;
		while (paint.measureText(newText) > width && diff < length) {
			diff++;
			newText = text.substring(0, length - diff) + "...";
		}
		if (diff == length) {
			newText = "...";
		}
		return newText;
	}

	/**
	 * Calculates How many characters can be placed in the width
	 */
	public static int getNumOfCharsInWidth(double width, Paint paint)	{
		if (width <= 0)	{
			return 0;	// width cannot be negative.
		}
		double dWWidth = paint.measureText("W");
		return (int)(width/dWWidth);
	}
	
	/**
	 * Draw a multiple lines text.
	 */
	public void drawText(Canvas canvas, String text, double x, double y,
			Paint paint, com.cyzapps.VisualMFP.Color color, double dTextSize, double dRotateDegree) {
		
		float fRotateDegree = (float)dRotateDegree, fX = (float)x, fY = (float)y;
		if (dRotateDegree != 0)	{
			canvas.rotate(fRotateDegree, fX, fY);
		}
		int nPaintOriginalColor = paint.getColor();
		float fPaintOriginalTxtSize = paint.getTextSize();
		if (color != null)	{
			paint.setColor(color.getARGB());
		}	// otherwise, use paint's color.
		paint.setTextSize((float)dTextSize);
		String[] lines = text.split("\n");
		Rect rect = new Rect();
		int yOff = 0;
		for (int i = 0; i < lines.length; ++i) {
			canvas.drawText(lines[i], fX, fY + paint.getFontMetrics().leading + paint.ascent() + yOff, paint);
			paint.getTextBounds(lines[i], 0, lines[i].length(), rect);
			yOff = (int) (yOff + rect.height() + mdSpaceBtwTxtLines); // space between lines
		}
		paint.setTextSize(fPaintOriginalTxtSize);
		paint.setColor(nPaintOriginalColor);
		if (dRotateDegree != 0) {
			canvas.rotate(-fRotateDegree, fX, fY);
		}
	}
	
	/**
	 * Draw a single line text, x, y is in the middle of the line.
	 */
	public void draw1LineTextAnchorMid(Canvas canvas, String text, double x, double y,
			Paint paint, com.cyzapps.VisualMFP.Color color, double dTextSize, double dRotateDegree) {
		
		float fRotateDegree = (float)dRotateDegree, fX = (float)x, fY = (float)y;
		if (dRotateDegree != 0)	{
			canvas.rotate(fRotateDegree, fX, fY);
		}
		int nPaintOriginalColor = paint.getColor();
		float fPaintOriginalTxtSize = paint.getTextSize();
		if (color != null)	{
			paint.setColor(color.getARGB());
		}	// otherwise, use paint's color.
		paint.setTextSize((float)dTextSize);
		float fWidth = paint.measureText(text);
		canvas.drawText(text, fX - fWidth/2, fY + (float)dTextSize/2 - paint.descent(), paint);
		paint.setTextSize(fPaintOriginalTxtSize);
		paint.setColor(nPaintOriginalColor);
		if (dRotateDegree != 0) {
			canvas.rotate(-fRotateDegree, fX, fY);
		}
	}
	
	/**
	 * Draws the chart background.
	 */
	public void drawBackground(Canvas canvas, double x, double y, double width, double height,
								Paint paint, com.cyzapps.VisualMFP.Color newColor) {
		int nPaintOriginalColor = paint.getColor();
		Style stylePaintOriginalStyle = paint.getStyle();
		if (newColor != null) {	// a valid color
			paint.setColor(newColor.getARGB());
		} else if (mcolorBkGrnd != null)	{
			paint.setColor(mcolorBkGrnd.getARGB());
		}
		paint.setStyle(Style.FILL);
		float fLeft = (float)x, fTop = (float)y, fRight = (float)(x + width), fBottom = (float)(y + height);
		canvas.drawRect(fLeft, fTop, fRight, fBottom, paint);
		// restore paint attribute.
		paint.setStyle(stylePaintOriginalStyle);
		paint.setColor(nPaintOriginalColor);
	}
	
	public void drawPoint(Canvas canvas, Position3D point, Paint paint, PointStyle pointStyle)	{
		int nOriginalColor = paint.getColor();
		Style styleOriginal = paint.getStyle();
		float fOriginalStrokeWidth = paint.getStrokeWidth();
		PathEffect pathEffectOriginal = paint.getPathEffect();
		boolean bAntiAlias = paint.isAntiAlias();
		
		if (pointStyle.mclr != null)	{
			paint.setColor(pointStyle.mclr.getARGB());
		}
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setPathEffect(new DashPathEffect(new float[]{(float)mdMediumSize, 0}, 0));
		double dSize = pointStyle.mdSize;
		switch (pointStyle.menumPointShape)	{
		case POINTSHAPE_CIRCLE:
		{
			float fX = (float)point.getX();
			float fY = (float)point.getY();
			float fRadius = (float)(dSize/2);
			canvas.drawCircle(fX, fY, fRadius, paint);
			break;
		}
		case POINTSHAPE_DIAMOND:
		{
			float fX = (float) point.getX();
			float fY = (float) point.getY();
			float fX1 = (float) (point.getX() - dSize/2), fX2 = (float) (point.getX() + dSize/2);
			float fY1 = (float) (point.getY() - dSize/2), fY2 = (float) (point.getY() + dSize/2);
			canvas.drawLine(fX1, fY, fX, fY1, paint);
			canvas.drawLine(fX1, fY, fX, fY2, paint);
			canvas.drawLine(fX2, fY, fX, fY1, paint);
			canvas.drawLine(fX2, fY, fX, fY2, paint);
			break;
		}
		case POINTSHAPE_CROSS:
		{
			float fX = (float) point.getX();
			float fY = (float) point.getY();
			float fX1 = (float) (point.getX() - dSize/2), fX2 = (float) (point.getX() + dSize/2);
			float fY1 = (float) (point.getY() - dSize/2), fY2 = (float) (point.getY() + dSize/2);
			canvas.drawLine(fX1, fY, fX2, fY, paint);
			canvas.drawLine(fX, fY1, fX, fY2, paint);
			break;
		}
		case POINTSHAPE_X:
		{
			float fX1 = (float) (point.getX() - dSize/2), fX2 = (float) (point.getX() + dSize/2);
			float fY1 = (float) (point.getY() - dSize/2), fY2 = (float) (point.getY() + dSize/2);
			canvas.drawLine(fX1, fY1, fX2, fY2, paint);
			canvas.drawLine(fX1, fY2, fX2, fY1, paint);
			break;
		}
		case POINTSHAPE_DOWNTRIANGLE:
		{
			float fX = (float) point.getX();
			float fX1 = (float) (point.getX() - dSize*0.433), fX2 = (float) (point.getX() + dSize*0.433);
			float fY1 = (float) (point.getY() + dSize/2), fY2 = (float) (point.getY() - dSize/4);
			canvas.drawLine(fX, fY1, fX1, fY2, paint);
			canvas.drawLine(fX, fY1, fX2, fY2, paint);
			canvas.drawLine(fX1, fY2, fX2, fY2, paint);
			break;
		}
		case POINTSHAPE_UPTRIANGLE:
		{
			float fX = (float) point.getX();
			float fX1 = (float) (point.getX() - dSize*0.433), fX2 = (float) (point.getX() + dSize*0.433);
			float fY1 = (float) (point.getY() - dSize/2), fY2 = (float) (point.getY() + dSize/4);
			canvas.drawLine(fX, fY1, fX1, fY2, paint);
			canvas.drawLine(fX, fY1, fX2, fY2, paint);
			canvas.drawLine(fX1, fY2, fX2, fY2, paint);
			break;
		}
		case POINTSHAPE_SQUARE:
		{
			float fX1 = (float)(point.getX() - dSize/2);
			float fX2 = (float)(point.getX() + dSize/2);
			float fY1 = (float)(point.getY() - dSize/2);
			float fY2 = (float)(point.getY() + dSize/2);
			canvas.drawRect(fX1, fY1, fX2, fY2, paint);
			break;
		}
		default:	// dot.
			canvas.drawPoint((float)point.getX(), (float)point.getY(), paint);
		}
		
		paint.setAntiAlias(bAntiAlias);
		paint.setColor(nOriginalColor);
		paint.setStyle(styleOriginal);
		paint.setStrokeWidth(fOriginalStrokeWidth);
		paint.setPathEffect(pathEffectOriginal);
	}
	
	public void drawLine(Canvas canvas, Position3D p3PrevPoint, Position3D p3NextPoint, Paint paint, LineStyle lineStyle)	{
		if (lineStyle.menumLinePattern == LINEPATTERN.LINEPATTERN_NON)	{
			return;
		}
		int nOriginalColor = paint.getColor();
		Style styleOriginal = paint.getStyle();
		float fOriginalStrokeWidth = paint.getStrokeWidth();
		PathEffect pathEffectOriginal = paint.getPathEffect();

		if (lineStyle.mclr != null)	{
			paint.setColor(lineStyle.mclr.getARGB());
		}
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth((float)lineStyle.mdLineWidth);
		switch (lineStyle.menumLinePattern)	{
		case LINEPATTERN_DASH:
		{
			paint.setPathEffect(new DashPathEffect(new float[]{(float)mdVerySmallSize, (float)mdTinySize}, 0));
			break;
		}
		case LINEPATTERN_DOT:
		{
			paint.setPathEffect(new DashPathEffect(new float[]{(float)mdVeryTinySize, (float)mdTinySize}, 0));
			break;
		}
		case LINEPATTERN_DASH_DOT:
		{
			paint.setPathEffect(new DashPathEffect(new float[]{(float)mdVerySmallSize, (float)mdTinySize, (float)mdVeryTinySize, (float)mdTinySize}, 0));
			break;
		}
		/* case LINEPATTERN_NON:	// need not to consider LINEPATTERN_NON
		{
			paint.setPathEffect(new DashPathEffect(new float[]{0, (float)mdMediumSize}, 0));
			break;
		} */
		default:	// solid
			paint.setPathEffect(new PathEffect());
		}
		
		float fX0 = (float)p3PrevPoint.getX(), fY0 = (float)p3PrevPoint.getY(),
				fX1 = (float)p3NextPoint.getX(), fY1 = (float)p3NextPoint.getY();
		canvas.drawLine(fX0, fY0, fX1, fY1, paint);
		
		paint.setColor(nOriginalColor);
		paint.setStyle(styleOriginal);
		paint.setStrokeWidth(fOriginalStrokeWidth);
		paint.setPathEffect(pathEffectOriginal);
	}
	
    public void drawButtons(Canvas canvas, double left, double top, double width, double height, Paint paint)  {
    }

    public void draw(Canvas canvas, double x, double y, double width, double height, Paint paint) {
		drawBackground(canvas, x, y, width, height, paint, null);
		float fPaintOriginalTxtSize = paint.getTextSize();
		paint.setTextSize((float)(width/2));
		draw1LineTextAnchorMid(canvas, mstrChartTitle, x / 2, y + mdSmallSize, paint, null, mdSmallSize, 0);
        drawButtons(canvas, x, y, width, height, paint);
		paint.setTextSize(fPaintOriginalTxtSize);
	}


	public void calcCoordArea(double x, double y, double width, double height)	{
	}
	
	public void update()	{
		
	}
	
	public boolean isZoomInEnabled()	{
		return true;
	}
	
	public boolean isZoomOutEnabled()	{
		return true;
	}
		
	public void config()	{
		
	}
	
	public void initialize()	{
		// mcontext should be always not null.
		mcfgImage_24 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.setting_gear_24);
		mcfgImage_32 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.setting_gear_32);
		mcfgImage_48 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.setting_gear_48);
		mcfgImage_64 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.setting_gear_64);
		mzoomInImage_24 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_in_24);
		mzoomInImage_32 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_in_32);
		mzoomInImage_48 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_in_48);
		mzoomInImage_64 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_in_64);
		mzoomOutImage_24 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_out_24);
		mzoomOutImage_32 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_out_32);
		mzoomOutImage_48 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_out_48);
		mzoomOutImage_64 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_out_64);
		mxy1To1ZoomImage_24 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_1_24);
		mxy1To1ZoomImage_32 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_1_32);
		mxy1To1ZoomImage_48 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_1_48);
		mxy1To1ZoomImage_64 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_1_64);
		mfitZoomImage_24 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_fit_24);
		mfitZoomImage_32 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_fit_32);
		mfitZoomImage_48 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_fit_48);
		mfitZoomImage_64 = BitmapFactory.decodeResource(mcontext.getResources(), R.drawable.zoom_fit_64);
	}
	
	public void zoom(double dXRatio, double dYRatio)	{
		
	}
	
	public void slide(double dOldX, double dOldY, double dNewX, double dNewY)	{
		
	}
	
	public void resize(double dWidth, double dHeight, double dOldWidth, double dOldHeight)	{
		if (dWidth != dOldWidth || dHeight != dOldHeight)	{
			calcCoordArea(0, 0, dWidth, dHeight);
			update();
		}
	}
	
	public void zoom1To1()	{
		
	}
	
	public void zoomReset()	{
		
	}
	
	public void resetAllSizesBasedOnMedium()	{
		mdVeryTinySize = mdMediumSize/16;
		mdTinySize = mdMediumSize/8;
		mdVerySmallSize = mdMediumSize/4;
		mdSmallSize = mdMediumSize/2;
		mdLargeSize = mdMediumSize*2;
		mdVeryLargeSize = mdMediumSize*4;
		mdHugeSize = mdMediumSize*8;
		mdVeryHugeSize = mdMediumSize*16;
	}
	
	public static double solve1OrderLinearEq(Position3D p31, Position3D p32, double input, boolean bSolveY)	{
		if (bSolveY)	{
			if (p31.getX() == p32.getX())	{
				if (input == p31.getX())	{
					return Double.NaN;
				} else	{
					return Double.POSITIVE_INFINITY;
				}
			} else	{
				double deltaX = p32.getX() - p31.getX();
				return (p32.getY() - p31.getY())/deltaX * input + (p32.getX()*p31.getY() - p31.getX()*p32.getY())/deltaX;
			}
		} else	{
			if (p31.getY() == p32.getY())	{
				if (input == p31.getY())	{
					return Double.NaN;
				} else	{
					return Double.POSITIVE_INFINITY;
				}
			} else	{
				double deltaY = p32.getY() - p31.getY();
				return (p32.getX() - p31.getX())/deltaY * input + (p32.getY()*p31.getX() - p31.getY()*p32.getX())/deltaY;
			}
		}
	}
	
	public static Position3D[] findInRangeLine(double x1, double x2, double y1, double y2, Position3D p31, Position3D p32)	{
		// assume p31 and p32 are not null.
		
		double dXMax = Math.max(x1, x2), dXMin = Math.min(x1, x2), dYMax = Math.max(y1, y2), dYMin = Math.min(y1, y2);		
		if (p31.getX() >= dXMin && p31.getX() <= dXMax && p31.getY() >= dYMin && p31.getY() <= dYMax)	{
			if (p32.getX() >= dXMin && p32.getX() <= dXMax && p32.getY() >= dYMin && p32.getY() <= dYMax)	{
				Position3D[] points = new Position3D[2];
				points[0] = p31;
				points[1] = p32;
				return points;
			} else	{
				// p31 in range while p32 is not
				Position3D p3End2 = null;
				double dXMinY = Double.NaN, dXMaxY = Double.NaN, dYMinX = Double.NaN, dYMaxX = Double.NaN;
				if (p32.getX() < dXMin)	{
					dXMinY = solve1OrderLinearEq(p31, p32, dXMin, true);
					if (dXMinY >= dYMin && dXMinY <= dYMax)	{
						p3End2 = new Position3D(dXMin, dXMinY);
					}
				} else if (p32.getX() > dXMax)	{
					dXMaxY = solve1OrderLinearEq(p31, p32, dXMax, true);
					if (dXMaxY >= dYMin && dXMaxY <= dYMax)	{
						p3End2 = new Position3D(dXMax, dXMaxY);
					}
				}
				if (p32.getY() < dYMin && p3End2 == null)	{
					dYMinX = solve1OrderLinearEq(p31, p32, dYMin, false);
					if (dYMinX >= dXMin && dYMinX <= dXMax)	{
						p3End2 = new Position3D(dYMinX, dYMin);
					}
				} else if (p32.getY() > dYMax && p3End2 == null)	{
					dYMaxX = solve1OrderLinearEq(p31, p32, dYMax, false);				
					if (dYMaxX >= dXMin && dYMaxX <= dXMax)	{
						p3End2 = new Position3D(dYMaxX, dYMax);
					}
				}
				Position3D[] points;
				if (p3End2 == null)	{
					points = new Position3D[1];
					points[0] = p31;
				} else	{
					points = new Position3D[2];
					points[0] = p31;
					points[1] = p3End2;
				}
				return points;
			}
		} else if (p32.getX() >= dXMin && p32.getX() <= dXMax && p32.getY() >= dYMin && p32.getY() <= dYMax)	{
			// p32 in range while p31 is not
			Position3D p3End1 = null;
			double dXMinY = Double.NaN, dXMaxY = Double.NaN, dYMinX = Double.NaN, dYMaxX = Double.NaN;
			if (p31.getX() < dXMin)	{
				dXMinY = solve1OrderLinearEq(p31, p32, dXMin, true);
				if (dXMinY >= dYMin && dXMinY <= dYMax)	{
					p3End1 = new Position3D(dXMin, dXMinY);
				}
			} else if (p31.getX() > dXMax)	{
				dXMaxY = solve1OrderLinearEq(p31, p32, dXMax, true);
				if (dXMaxY >= dYMin && dXMaxY <= dYMax)	{
					p3End1 = new Position3D(dXMax, dXMaxY);
				}
			}
			if (p31.getY() < dYMin && p3End1 == null)	{
				dYMinX = solve1OrderLinearEq(p31, p32, dYMin, false);
				if (dYMinX >= dXMin && dYMinX <= dXMax)	{
					p3End1 = new Position3D(dYMinX, dYMin);
				}
			} else if (p31.getY() > dYMax && p3End1 == null)	{
				dYMaxX = solve1OrderLinearEq(p31, p32, dYMax, false);				
				if (dYMaxX >= dXMin && dYMaxX <= dXMax)	{
					p3End1 = new Position3D(dYMaxX, dYMax);
				}
			}
			Position3D[] points;
			if (p3End1 == null)	{
				points = new Position3D[1];
				points[0] = p32;
			} else	{
				points = new Position3D[2];
				points[0] = p3End1;
				points[1] = p32;
			}
			return points;
		} else	{
			double dXMinY = Double.NaN, dXMaxY = Double.NaN, dYMinX = Double.NaN, dYMaxX = Double.NaN;
			Position3D p3Ends[] = new Position3D[4];
			Position3D p3End1 = null, p3End2 = null;
			int nNumofEnds = 0;
			if ((dXMin - p31.getX()) * (dXMin - p32.getX()) <= 0)	{
				dXMinY = solve1OrderLinearEq(p31, p32, dXMin, true);
				if (Double.isInfinite(dXMinY) == false && Double.isNaN(dXMinY) == false
						&& (dXMinY - dYMin) * (dXMinY - dYMax) <= 0)	{
					p3Ends[0] = new Position3D(dXMin, dXMinY);
					p3End1 = p3Ends[0];
					nNumofEnds ++;
				}
			}
			if ((dXMax - p31.getX()) * (dXMax - p32.getX()) <= 0)	{
				dXMaxY = solve1OrderLinearEq(p31, p32, dXMax, true);
				if (Double.isInfinite(dXMaxY) == false && Double.isNaN(dXMaxY) == false
						&& (dXMaxY - dYMin) * (dXMaxY - dYMax) <= 0)	{
					p3Ends[1] = new Position3D(dXMax, dXMaxY);
					if (nNumofEnds == 0)	{
						p3End1 = p3Ends[1];
					} else	{	// nNumofEnds == 1
						p3End2 = p3Ends[1];
					}
					nNumofEnds ++;
				}
			}
			Position3D p3End2Candidate = null;
			if ((dYMin - p31.getY()) * (dYMin - p32.getY()) <= 0 && nNumofEnds < 2)	{
				dYMinX = solve1OrderLinearEq(p31, p32, dYMin, false);
				if (Double.isInfinite(dYMinX) == false && Double.isNaN(dYMinX) == false
						&& (dYMinX - dXMin) * (dYMinX - dXMax) <= 0)	{
					p3Ends[2] = new Position3D(dYMinX, dYMin);
					if (nNumofEnds == 0)	{
						p3End1 = p3Ends[2];
						nNumofEnds ++;
					} else if (nNumofEnds == 1)	{
						p3End2Candidate = p3Ends[2];
					}
				}
			}
			if ((dYMax - p31.getY()) * (dYMax - p32.getY()) <= 0 && nNumofEnds < 2)	{
				dYMaxX = solve1OrderLinearEq(p31, p32, dYMax, false);
				if (Double.isInfinite(dYMaxX) == false && Double.isNaN(dYMaxX) == false
						&& (dYMaxX - dXMin) * (dYMaxX - dXMax) <= 0)	{
					p3Ends[3] = new Position3D(dYMaxX, dYMax);
					if (nNumofEnds == 0)	{
						p3End1 = p3Ends[3];
						nNumofEnds ++;
					} else if (nNumofEnds == 1)	{
						if (p3End2Candidate == null)	{
							p3End2 = p3Ends[3];
						} else	{
							p3End1 = p3End2Candidate;
							p3End2 = p3Ends[3];
						}
						nNumofEnds ++;
					}
				}
			}
			
			if (nNumofEnds == 1 && p3End2Candidate != null)	{
				p3End2 = p3End2Candidate;
				nNumofEnds ++;
			}
			
			Position3D[] points;
			if (nNumofEnds == 0)	{
				points = new Position3D[0];
			} else if (nNumofEnds == 1)	{
				points = new Position3D[1];
				points[0] = p3End1;
			} else	{	// nNumofEnds == 2
				points = new Position3D[2];
				points[0] = p3End1;
				points[1] = p3End2;
			}
			return points;
		}
	}
}
