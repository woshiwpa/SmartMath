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

import com.cyzapps.Jfcalc.PlotLib.ThreeDExprSurface;
import com.cyzapps.VisualMFP.Position3D;

public class OGLExprChartOperator extends OGLChartOperator {
	public ThreeDExprSurface[] m3DExprSurfaces = new ThreeDExprSurface[0];
	
	public OGLExprChartOperator()	{
	}
	
	public static final int DEFAULT_NUM_OF_STEPS = 10;
	public void getCurveSettings(String strSettings, ThreeDExprSurface threeDExprSurface)	{
		String[] strlistCurveSettings = strSettings.split("(?<!\\\\);");
		for (int nIndex = 0; nIndex < strlistCurveSettings.length; nIndex ++)	{
			String[] strlistSetting = strlistCurveSettings[nIndex].split("(?<!\\\\):");
			if (strlistSetting.length != 2)	{
				continue;
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("curve_label"))	{
				threeDExprSurface.mstrCurveLabel = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("is_grid"))	{
                try {
                	threeDExprSurface.mbIsGrid = Boolean.parseBoolean(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)	{
                	threeDExprSurface.mbIsGrid = false;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("min_color"))	{
				threeDExprSurface.mstrMinColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("min_color_1"))	{
				threeDExprSurface.mstrMinColor1 = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("max_color"))	{
				threeDExprSurface.mstrMaxColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("max_color_1"))	{
				threeDExprSurface.mstrMaxColor1 = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("function_variable"))	{
				try	{
					threeDExprSurface.mnFunctionVar = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					threeDExprSurface.mnFunctionVar = 2;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_expr"))	{
				threeDExprSurface.mstrXExpr = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("x_steps"))	{
				try	{
					threeDExprSurface.mnXNumOfSteps = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					threeDExprSurface.mnXNumOfSteps = DEFAULT_NUM_OF_STEPS;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_expr"))	{
				threeDExprSurface.mstrYExpr = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("y_steps"))	{
				try	{
					threeDExprSurface.mnYNumOfSteps = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					threeDExprSurface.mnYNumOfSteps = DEFAULT_NUM_OF_STEPS;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("z_expr"))	{
				threeDExprSurface.mstrZExpr = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("z_steps"))	{
				try	{
					threeDExprSurface.mnZNumOfSteps = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					threeDExprSurface.mnZNumOfSteps = DEFAULT_NUM_OF_STEPS;
				}
			}
		}
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
					if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax || mdblZMin >= mdblZMax
							|| mnXLabels <= 0 || mnYLabels <= 0 || mnZLabels <= 0)    {
						// something wrong with file format
						return false;
					}
					if (mnNumofCurves <= 0)    {
						return false;
					}
					m3DExprSurfaces = new ThreeDExprSurface[mnNumofCurves];
					for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
						m3DExprSurfaces[nIndex] = new ThreeDExprSurface();
					}
				} else {
					int nCurveId = nNumofLines - 2;
					if (nCurveId >= mnNumofCurves)    {
						return false;
					}
					// curve settings
					getCurveSettings(strLine, m3DExprSurfaces[nCurveId]);
				}
				strLine = br.readLine();
			}
		} catch(FileNotFoundException ex)    {
			// cannot open the file.
			Logger.getLogger(OGLExprChartOperator.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(OGLExprChartOperator.class.getName()).log(Level.SEVERE, null, ex);
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
				Logger.getLogger(OGLExprChartOperator.class.getName()).log(Level.SEVERE, null, ex);
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
		for (int idxLn = 0; idxLn < strlistLines.length; idxLn ++){
			String strLine = strlistLines[idxLn];
			if (idxLn == 0)	{
				getChartSettings(strLine);
				if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax || mdblZMin >= mdblZMax
						|| mnXLabels <= 0 || mnYLabels <= 0 || mnZLabels <= 0)	{
					// something wrong with file format
					return false;
				}
				if (mnNumofCurves <= 0)	{
					return false;
				}
				if (strlistLines.length != mnNumofCurves + 1)	{
					return false;
				}
				m3DExprSurfaces = new ThreeDExprSurface[mnNumofCurves];
				for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
					m3DExprSurfaces[nIndex] = new ThreeDExprSurface();
				}
			} else {
				int nCurveId = idxLn - 1;
				// curve settings
				getCurveSettings(strLine, m3DExprSurfaces[nCurveId]);
			}
		}
		return true;
	}
	
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        OGLExprChart oglChart = new OGLExprChart(context);
        oglChart.mstrChartTitle = mstrChartTitle;
        oglChart.mstrXAxisName = mstrXTitle;
        oglChart.mstrYAxisName = mstrYTitle;
        oglChart.mstrZAxisName = mstrZTitle;
        
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
               
        double dZMarkInterval = (mdblZMax - mdblZMin)/ccpParam1.mnRecommendedNumOfMarksPerAxis;
        dTmp1 = Math.pow(10, Math.floor(Math.log10(dZMarkInterval)));
        dTmp = dZMarkInterval/dTmp1;
        if (dTmp >= 7.5)	{
        	dZMarkInterval = dTmp1 * 10;
        } else if (dTmp >= 3.5)	{
        	dZMarkInterval = dTmp1 * 5;
        } else if (dTmp >= 1.5)	{
        	dZMarkInterval = dTmp1 * 2;
        } else	{
        	dZMarkInterval = dTmp1;
        }
        
        oglChart.mdXMark1 = 0;
        oglChart.mdXMark2 = dXMarkInterval;
        oglChart.mdYMark1 = 0;
        oglChart.mdYMark2 = dYMarkInterval;
        oglChart.mdZMark1 = 0;
        oglChart.mdZMark2 = dZMarkInterval;
        
        oglChart.mdXAxisLenInFROM = (mdblXMax - mdblXMin);
        oglChart.mdYAxisLenInFROM = (mdblYMax - mdblYMin);
        oglChart.mdZAxisLenInFROM = (mdblZMax - mdblZMin);
		
        oglChart.mp3OriginInFROM = new Position3D((mdblXMax + mdblXMin)/2.0, (mdblYMax + mdblYMin)/2.0, (mdblZMax + mdblZMin)/2.0);
        
        // save settings here if needed.
        oglChart.saveSettings();
        
        oglChart.m3DExprSurfaces = m3DExprSurfaces;
        
        return oglChart;
	}
}
