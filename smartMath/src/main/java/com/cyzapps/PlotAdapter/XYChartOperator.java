package com.cyzapps.PlotAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Color;

import com.cyzapps.VisualMFP.DataSeriesCurve;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;
import com.cyzapps.VisualMFP.PointStyle.POINTSHAPE;
import com.cyzapps.adapter.MFPAdapter;

public class XYChartOperator extends ChartOperator {
	public String mstrChartType = "multiXY";
	public String mstrChartTitle = "";
	public String mstrXTitle = "";
	public double mdblXMin = 0;
	public double mdblXMax = 1;
	public int mnXLabels = 1;
	public String mstrYTitle = "";
	public double mdblYMin = 0;
	public double mdblYMax = 1;
	public int mnYLabels = 1;
	public String mstrAxesColor = "gray";
	public String mstrLabelsColor = "ltgray";
	public String mstrChartBKColor = "black";
	public boolean mbShowGrid = true;
	public int mnNumofCurves = 0;
	public XYCurve[] mXYCurves = new XYCurve[0];
	
	public XYChartOperator()	{
	}
			
	public class XYCurve	{
		public double[] mdbllistX = new double[0];
		public double[] mdbllistY = new double[0];
		public String mstrCurveLabel = "";
		public String mstrColor = "";
		public String mstrPntColor = "";
		public String mstrPointStyle = "";
		public int mnPntSize = 1;
		public String mstrLnColor = "";
		public String mstrLineStyle = "";
		public int mnLnSize = 1;
	}
	
	public void getChartSettings(String strSettings)	{
		String[] strlistFileSettings = strSettings.split("(?<!\\\\);");
		for (int nIndex = 0; nIndex < strlistFileSettings.length; nIndex ++)	{
			String[] strlistSetting = strlistFileSettings[nIndex].split("(?<!\\\\):");
			if (strlistSetting.length != 2)	{
				continue;
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("chart_type"))	{
				mstrChartType = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("chart_title"))	{
				mstrChartTitle = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_title"))	{
				mstrXTitle = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_min"))	{
				try	{
					mdblXMin = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mdblXMin = 0;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_max"))	{
				try	{
					mdblXMax = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mdblXMax = 1;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_labels"))	{
				try	{
					mnXLabels = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mnXLabels = 1;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_title"))	{
				mstrYTitle = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_min"))	{
				try	{
					mdblYMin = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mdblYMin = 0;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_max"))	{
				try	{
					mdblYMax = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mdblYMax = 1;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_labels"))	{
				try	{
					mnYLabels = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mnYLabels = 1;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("axes_color"))	{
				mstrAxesColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("labels_color"))	{
				mstrLabelsColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("background_color"))	{
				mstrChartBKColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("show_grid"))	{
				mbShowGrid = removeEscapes(strlistSetting[1]).trim().equalsIgnoreCase("true")?true:false;
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("number_of_curves"))	{
				try {
					mnNumofCurves = Integer.parseInt(removeEscapes(strlistSetting[1]));
					if (mnNumofCurves < 0)	{
						mnNumofCurves = 0;
					}
				} catch (NumberFormatException e)	{
					mnNumofCurves = 0;
				}
			}
		}
	}
	
	public void getCurveSettings(String strSettings, XYCurve xyCurve)	{
		String[] strlistCurveSettings = strSettings.split("(?<!\\\\);");
		for (int nIndex = 0; nIndex < strlistCurveSettings.length; nIndex ++)	{
			String[] strlistSetting = strlistCurveSettings[nIndex].split("(?<!\\\\):");
			if (strlistSetting.length != 2)	{
				continue;
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("curve_label"))	{
				xyCurve.mstrCurveLabel = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("color"))	{
				xyCurve.mstrColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_color"))	{
				xyCurve.mstrPntColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_style"))	{
				xyCurve.mstrPointStyle = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("point_size"))	{
				try	{
					xyCurve.mnPntSize = Integer.parseInt(removeEscapes(strlistSetting[1]));
					if (xyCurve.mnPntSize < 0)	{
						xyCurve.mnPntSize = 0;
					}
				} catch(NumberFormatException e)	{
					xyCurve.mnPntSize = 1;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_color"))	{
				xyCurve.mstrLnColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_style"))	{
				xyCurve.mstrLineStyle = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("line_size"))	{
				try	{
					xyCurve.mnLnSize = Integer.parseInt(removeEscapes(strlistSetting[1]));
					if (xyCurve.mnLnSize < 0)	{
						xyCurve.mnLnSize = 0;
					}
				} catch(NumberFormatException e)	{
					xyCurve.mnLnSize = 1;
				}
			}
		}
	}
	
	public double[] getValueList(String strValueList)	{
		String[] strlistValues = strValueList.split(";");
		double[] dbllistValues = new double[strlistValues.length];
		for (int nIndex = 0; nIndex < strlistValues.length; nIndex ++)	{
			try {
				dbllistValues[nIndex] = Double.parseDouble(strlistValues[nIndex]);
			} catch(NumberFormatException e)	{
				dbllistValues[nIndex] = Double.NaN;
			}
		}
		return dbllistValues;
	}
	
	public static POINTSHAPE cvtStr2PntShape(String str)	{
		if (str.trim().toLowerCase(Locale.US).equals("circle"))	{
			return POINTSHAPE.POINTSHAPE_CIRCLE;
		} else if (str.trim().toLowerCase(Locale.US).equals("triangle"))	{
			return POINTSHAPE.POINTSHAPE_UPTRIANGLE;
		} else if (str.trim().toLowerCase(Locale.US).equals("square"))	{
			return POINTSHAPE.POINTSHAPE_SQUARE;
		} else if (str.trim().toLowerCase(Locale.US).equals("diamond"))	{
			return POINTSHAPE.POINTSHAPE_DIAMOND;
		} else if (str.trim().toLowerCase(Locale.US).equals("x"))	{
			return POINTSHAPE.POINTSHAPE_X;
		} else	{
			return POINTSHAPE.POINTSHAPE_DOT;
		}
	}
	
	public static int cvtStr2Color(String str)	{
		if (str.trim().toLowerCase(Locale.US).equals("blue"))	{
			return Color.BLUE; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("cyan"))	{
			return Color.CYAN; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("dkgray"))	{
			return Color.DKGRAY; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("gray"))	{
			return Color.GRAY; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("green"))	{
			return Color.GREEN; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("ltgray"))	{
			return Color.LTGRAY; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("magenta"))	{
			return Color.MAGENTA; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("red"))	{
			return Color.RED; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("transparent"))	{
			return Color.TRANSPARENT; 	
		} else if (str.trim().toLowerCase(Locale.US).equals("white"))	{
			return Color.WHITE;
		} else if (str.trim().toLowerCase(Locale.US).equals("yellow"))	{
			return Color.YELLOW;
		} else	{
			return Color.BLACK;
		}
	}
	
	public static com.cyzapps.VisualMFP.Color cvtStr2VMFPColor(String str)	{
		int nColor = cvtStr2Color(str);
		int nAlpha= (nColor >> 24) & 0xFF;
		int nRed=   (nColor >> 16) & 0xFF;
		int nGreen= (nColor >> 8) & 0xFF;
		int nBlue=  (nColor >> 0) & 0xFF;
		return new com.cyzapps.VisualMFP.Color(nAlpha, nRed, nGreen, nBlue);
	}
	
	@Override
	public boolean loadFromFile(String strFilePath)    {
		FileInputStream fis = null;
		BufferedReader br = null;
		try {
			fis = new FileInputStream(strFilePath);
			br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			int nNumofLines = 0;
			// use BufferedReader.readLine instead of scanner because scanner in Samsung Galaxy Pad A
			// long line will block at scanner.hasNextLine(). OGLChart and XYChart generally have very long line.
			String strLine = br.readLine();
			while (strLine != null){
				nNumofLines ++;
				if (nNumofLines == 1)    {
					getChartSettings(strLine);
					double dDefaultHalfRange = (MFPAdapter.getPlotChartVariableTo() - MFPAdapter.getPlotChartVariableFrom())/2.0;
					if (mdblXMin == mdblXMax
							|| (this instanceof PolarChartOperator
							&& Math.abs(mdblXMax - mdblXMin) < 0.00001)) {
						// polar chart is a bit special. For some reason, if xmin = xmax = like 2.67,
						// mdblXMax and mdblXMin are not exactly the same, their difference is so small
						// that R step length will be too small and there will be so many points to draw
						// that final results in outofmemory error.
						mdblXMin -= dDefaultHalfRange;
						mdblXMax += dDefaultHalfRange;
					}
					if (mdblYMin == mdblYMax) {
						mdblYMin -= dDefaultHalfRange;
						mdblYMax += dDefaultHalfRange;
					}
					if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax
							|| mnXLabels <= 0 || mnYLabels <= 0)    {
						// something wrong with file format
						return false;
					}
					if (mnNumofCurves <= 0)    {
						return false;
					}
					mXYCurves = new XYCurve[mnNumofCurves];
					for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
						mXYCurves[nIndex] = new XYCurve();
					}
				} else {
					int nCurveId = (int)Math.floor((double)(nNumofLines - 2)/3.0);
					if (nCurveId >= mnNumofCurves)    {
						return false;
					}
					if (nNumofLines % 3 == 2)    {
						// X
						mXYCurves[nCurveId].mdbllistX = getValueList(strLine);
					} else if (nNumofLines % 3 == 0)    {
						// Y
						mXYCurves[nCurveId].mdbllistY = getValueList(strLine);
						if (mXYCurves[nCurveId].mdbllistX.length != mXYCurves[nCurveId].mdbllistY.length)    {
							// something wrong
							return false;
						}
					} else    {
						// curve settings
						getCurveSettings(strLine, mXYCurves[nCurveId]);
					}
				}
				strLine = br.readLine();
			}
		} catch(FileNotFoundException ex)    {
			// cannot open the file.
			Logger.getLogger(XYChartOperator.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(XYChartOperator.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
				Logger.getLogger(XYChartOperator.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return true;
	}
	
	@Override
	public boolean loadFromString(String strSettings)	{
		if (strSettings == null)	{
			return false;
		}
		String[] strlistLines = strSettings.split("\n");
		int nNumofLines = 0;
		for (int idxLn = 0; idxLn < strlistLines.length; idxLn ++){
			String strLine = strlistLines[idxLn];
			nNumofLines ++;
			if (nNumofLines == 1)	{
				getChartSettings(strLine);
				if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax
						|| mnXLabels <= 0 || mnYLabels <= 0)	{
					// something wrong with file format
					return false;
				}
				if (mnNumofCurves <= 0)	{
					return false;
				}
				if (strlistLines.length != (1 + 3 * mnNumofCurves))	{
					// something wrong
					return false;
				}
				mXYCurves = new XYCurve[mnNumofCurves];
				for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
					mXYCurves[nIndex] = new XYCurve();
				}
			} else {
				int nCurveId = (int)Math.floor((double)(nNumofLines - 2)/3.0);
				if (nNumofLines % 3 == 2)	{
					// X
					mXYCurves[nCurveId].mdbllistX = getValueList(strLine);
				} else if (nNumofLines % 3 == 0)	{
					// Y
					mXYCurves[nCurveId].mdbllistY = getValueList(strLine);
					if (mXYCurves[nCurveId].mdbllistX.length != mXYCurves[nCurveId].mdbllistY.length)	{
						// something wrong
						return false;
					}
				} else	{
					// curve settings
					getCurveSettings(strLine, mXYCurves[nCurveId]);
				}
			}
		}
		return true;
	}
	
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        FlatChart flatChart = new XYChart(context);
        flatChart.mstrChartTitle = mstrChartTitle;
        flatChart.mcolorBkGrnd = new com.cyzapps.VisualMFP.Color(255, 0, 0, 0);
        flatChart.mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 200, 200, 200);
        ((XYChart)flatChart).mstrXAxisName = mstrXTitle;
        ((XYChart)flatChart).mstrYAxisName = mstrYTitle;
        ((XYChart)flatChart).mbShowGrid = mbShowGrid;
        
        double dXMarkInterval = (mdblXMax - mdblXMin)/ccpParam1.mnRecommendedNumOfMarksPerAxis;
        double dTmp1 = Math.pow(10, Math.floor(Math.log10(dXMarkInterval)));
        double dTmp = dXMarkInterval/dTmp1;
        if (dTmp >= 7.5)	{
        	dXMarkInterval = dTmp1 * 10;
        } else if (dTmp >= 3.5)	{
        	dXMarkInterval = dTmp1 * 5;
        } else if (dTmp >= 1.5)	{
        	dXMarkInterval = dTmp1 * 2;
        } else	{
        	dXMarkInterval = dTmp1;
        }
        
        double dYMarkInterval = (mdblYMax - mdblYMin)/ccpParam1.mnRecommendedNumOfMarksPerAxis;
        dTmp1 = Math.pow(10, Math.floor(Math.log10(dYMarkInterval)));
        dTmp = dYMarkInterval/dTmp1;
        if (dTmp >= 7.5)	{
        	dYMarkInterval = dTmp1 * 10;
        } else if (dTmp >= 3.5)	{
        	dYMarkInterval = dTmp1 * 5;
        } else if (dTmp >= 1.5)	{
        	dYMarkInterval = dTmp1 * 2;
        } else	{
        	dYMarkInterval = dTmp1;
        }        
        
        ((XYChart)flatChart).mdXMark1 = 0;
        ((XYChart)flatChart).mdXMark2 = dXMarkInterval;
        ((XYChart)flatChart).mdYMark1 = 0;
        ((XYChart)flatChart).mdYMark2 = dYMarkInterval;
        
        ((XYChart)flatChart).mdXAxisLenInFROM = (mdblXMax - mdblXMin);
        ((XYChart)flatChart).mdYAxisLenInFROM = (mdblYMax - mdblYMin);
		
        ((XYChart)flatChart).mp3CoordLeftBottomInFROM = new Position3D(mdblXMin, mdblYMin);
        
        flatChart.mbUseInit2CalibrateZoom = true;
        flatChart.saveSettings();
        
        for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
    		DataSeriesCurve dsv = new DataSeriesCurve();
    		dsv.mstrName = mXYCurves[nIndex].mstrCurveLabel;
    		dsv.mpointStyle = new PointStyle();
    		dsv.mpointStyle.mclr = cvtStr2VMFPColor(mXYCurves[nIndex].mstrPntColor.trim().equals("")?
					(mXYCurves[nIndex].mstrLnColor.trim().equals("")?mXYCurves[nIndex].mstrColor:mXYCurves[nIndex].mstrLnColor)
					:mXYCurves[nIndex].mstrPntColor);
    		dsv.mpointStyle.menumPointShape =  cvtStr2PntShape(mXYCurves[nIndex].mstrPointStyle);
    		dsv.mpointStyle.mdSize = flatChart.mdVerySmallSize;
    		dsv.mlineStyle = new LineStyle();
    		dsv.mlineStyle.mclr = cvtStr2VMFPColor(mXYCurves[nIndex].mstrLnColor.trim().equals("")?
    				mXYCurves[nIndex].mstrColor:mXYCurves[nIndex].mstrLnColor);
    		dsv.mlineStyle.menumLinePattern = (mXYCurves[nIndex].mnLnSize <= 0)?LINEPATTERN.LINEPATTERN_NON:LINEPATTERN.LINEPATTERN_SOLID;
    		dsv.mlineStyle.mdLineWidth = (mXYCurves[nIndex].mnLnSize < 0)?0:mXYCurves[nIndex].mnLnSize;
    		
    		for (int idx = 0; idx < mXYCurves[nIndex].mdbllistX.length; idx ++)	{
    			dsv.add(new Position3D(mXYCurves[nIndex].mdbllistX[idx], mXYCurves[nIndex].mdbllistY[idx]));
    		}
    		((XYChart)flatChart).mDataSet.add(dsv);
        }
        
        return flatChart;
	}

}
