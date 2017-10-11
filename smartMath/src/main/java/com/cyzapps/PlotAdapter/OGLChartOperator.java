/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.PlotAdapter;

import com.cyzapps.VisualMFP.DataSeriesGridSurface;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.SurfaceStyle;
import com.cyzapps.VisualMFP.SurfaceStyle.SURFACETYPE;
import com.cyzapps.adapter.MFPAdapter;

import android.content.Context;
import android.graphics.Color;

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

/**
 *
 * @author tonyc
 */
public class OGLChartOperator extends ChartOperator{
	public String mstrChartType = "multiXYZ";
	public String mstrChartTitle = "";
	public String mstrXTitle = "";
	public double mdblXMin = 0;
	public double mdblXMax = 1;
	public int mnXLabels = 1;
	public String mstrYTitle = "";
	public double mdblYMin = 0;
	public double mdblYMax = 1;
	public int mnYLabels = 1;
	public String mstrZTitle = "";
	public double mdblZMin = 0;
	public double mdblZMax = 1;
	public int mnZLabels = 1;
	public int mnNumofCurves = 0;
	public ThreeDSurface[] m3DSurfaces = new ThreeDSurface[0];
	
	public OGLChartOperator()	{
	}
			
	public class ThreeDSurface	{
		public double[][] mdblarrayX = new double[0][0];
		public double[][] mdblarrayY = new double[0][0];
		public double[][] mdblarrayZ = new double[0][0];
		public String mstrCurveLabel = "";
        boolean mbIsGrid = false;    // if is grid, then is not filled
		String mstrMinColor = "white";
		String mstrMinColor1 = "white";
        Double mdMinColorValue = null;  // null means min z is corresponding to min color
		String mstrMaxColor = "white";
		String mstrMaxColor1 = "white";
        Double mdMaxColorValue = null;  // null means max z is corresponding to max color
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
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("z_title"))	{
				mstrZTitle = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("z_min"))	{
				try	{
					mdblZMin = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mdblZMin = 0;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("z_max"))	{
				try	{
					mdblZMax = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mdblZMax = 1;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("z_labels"))	{
				try	{
					mnZLabels = Integer.parseInt(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					mnZLabels = 1;
				}
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
	
	public void getCurveSettings(String strSettings, ThreeDSurface threeDSurface)	{
		String[] strlistCurveSettings = strSettings.split("(?<!\\\\);");
		for (int nIndex = 0; nIndex < strlistCurveSettings.length; nIndex ++)	{
			String[] strlistSetting = strlistCurveSettings[nIndex].split("(?<!\\\\):");
			if (strlistSetting.length != 2)	{
				continue;
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("curve_label"))	{
				threeDSurface.mstrCurveLabel = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("is_grid"))	{
                try {
                    threeDSurface.mbIsGrid = Boolean.parseBoolean(removeEscapes(strlistSetting[1]));
                } catch(NumberFormatException e)	{
					threeDSurface.mbIsGrid = false;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("min_color"))	{
				threeDSurface.mstrMinColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("min_color_1"))	{
				threeDSurface.mstrMinColor1 = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("min_color_value"))	{
				try	{
					threeDSurface.mdMinColorValue = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					threeDSurface.mdMinColorValue = null;
				}
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("max_color"))	{
				threeDSurface.mstrMaxColor = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("max_color_1"))	{
				threeDSurface.mstrMaxColor1 = removeEscapes(strlistSetting[1]);
			} else if (strlistSetting[0].trim().toLowerCase(Locale.US).equals("max_color_value"))	{
				try	{
					threeDSurface.mdMaxColorValue = Double.parseDouble(removeEscapes(strlistSetting[1]));
				} catch(NumberFormatException e)	{
					threeDSurface.mdMaxColorValue = null;
				}
			}
		}
	}
	
	public double[][] getValueArray(String strValueList)	{
		String[] strlistValueLists = strValueList.split(";");
		double[][] dblarrayValues = new double[0][0];
		for (int nIndex = 0; nIndex < strlistValueLists.length; nIndex ++)	{
            String[] strlistValues = strlistValueLists[nIndex].split(",");
            if (nIndex == 0)    {
                dblarrayValues = new double[strlistValueLists.length][strlistValues.length];
            }
            for (int nIndex1 = 0; nIndex1 < dblarrayValues[0].length; nIndex1 ++)   {
                try {
                    dblarrayValues[nIndex][nIndex1] = Double.parseDouble(strlistValues[nIndex1]);
                } catch(NumberFormatException e)	{
                    dblarrayValues[nIndex][nIndex1] = Double.NaN;
                }
            }
		}
		return dblarrayValues;
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
		/*} else if (str.trim().toLowerCase(Locale.US).equals("orange"))	{
			return Color.ORANGE; 	// Pink and orange are not predefined colors in Android
		} else if (str.trim().toLowerCase(Locale.US).equals("pink"))	{
			return Color.PINK; 	*/
		} else if (str.trim().toLowerCase(Locale.US).equals("red"))	{
			return Color.RED; 	
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
			// long line will block at scanner.hasNextLine(). OGLChart and XYChart generally have very lone line.
			String strLine = br.readLine();
			while (strLine != null){
				nNumofLines ++;
				if (nNumofLines == 1)    {
					getChartSettings(strLine);
					double dDefaultHalfRange = (MFPAdapter.getPlotChartVariableTo() - MFPAdapter.getPlotChartVariableFrom())/2.0;
					if (mdblXMin == mdblXMax) {
						mdblXMin -= dDefaultHalfRange;
						mdblXMax += dDefaultHalfRange;
					}
					if (mdblYMin == mdblYMax) {
						mdblYMin -= dDefaultHalfRange;
						mdblYMax += dDefaultHalfRange;
					}
					if (mdblZMin == mdblZMax) {
						mdblZMin -= dDefaultHalfRange;
						mdblZMax += dDefaultHalfRange;
					}
					if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax || mdblZMin >= mdblZMax
							|| mnXLabels <= 0 || mnYLabels <= 0 || mnZLabels <= 0)    {
						// something wrong with file format
						return false;
					}
					if (mnNumofCurves <= 0)    {
						return false;
					}
					m3DSurfaces = new ThreeDSurface[mnNumofCurves];
					for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)    {
						m3DSurfaces[nIndex] = new ThreeDSurface();
					}
				} else {
					int nCurveId = (int)Math.floor((double)(nNumofLines - 2)/4.0);
					if (nCurveId >= mnNumofCurves)    {
						// something wrong here.
						return false;
					}
					if (nNumofLines % 4 == 2)    {
						// X
						m3DSurfaces[nCurveId].mdblarrayX = getValueArray(strLine);
						if (m3DSurfaces[nCurveId].mdblarrayX.length == 0 || m3DSurfaces[nCurveId].mdblarrayX[0].length == 0)    {
							// something wrong
							return false;
						}
					} else if (nNumofLines % 4 == 3)    {
						// Y
						m3DSurfaces[nCurveId].mdblarrayY = getValueArray(strLine);
						if (m3DSurfaces[nCurveId].mdblarrayY.length != m3DSurfaces[nCurveId].mdblarrayX.length
								|| m3DSurfaces[nCurveId].mdblarrayY[0].length != m3DSurfaces[nCurveId].mdblarrayX[0].length)    {
							// something wrong
							return false;
						}
					} else if (nNumofLines % 4 == 0)    {
						// Z
						m3DSurfaces[nCurveId].mdblarrayZ = getValueArray(strLine);
						if (m3DSurfaces[nCurveId].mdblarrayZ.length != m3DSurfaces[nCurveId].mdblarrayX.length
								|| m3DSurfaces[nCurveId].mdblarrayZ[0].length != m3DSurfaces[nCurveId].mdblarrayX[0].length)    {
							// something wrong
							return false;
						}
					} else    {
						// curve settings
						getCurveSettings(strLine, m3DSurfaces[nCurveId]);
					}
				}
				strLine = br.readLine();
			}
		} catch(FileNotFoundException ex)    {
			// cannot open the file.
			Logger.getLogger(OGLChartOperator.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(OGLChartOperator.class.getName()).log(Level.SEVERE, null, ex);
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
				Logger.getLogger(OGLChartOperator.class.getName()).log(Level.SEVERE, null, ex);
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
				if (mdblXMin >= mdblXMax || mdblYMin >= mdblYMax || mdblZMin >= mdblZMax
						|| mnXLabels <= 0 || mnYLabels <= 0 || mnZLabels <= 0)	{
					// something wrong with file format
					return false;
				}
				if (mnNumofCurves <= 0)	{
					return false;
				}
				if (strlistLines.length != (1 + 4 * mnNumofCurves))	{
					// something wrong
					return false;
				}
				m3DSurfaces = new ThreeDSurface[mnNumofCurves];
				for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
					m3DSurfaces[nIndex] = new ThreeDSurface();
				}
			} else {
				int nCurveId = (int)Math.floor((double)(nNumofLines - 2)/4.0);
				if (nNumofLines % 4 == 2)	{
					// X
					m3DSurfaces[nCurveId].mdblarrayX = getValueArray(strLine);
					if (m3DSurfaces[nCurveId].mdblarrayX.length == 0 || m3DSurfaces[nCurveId].mdblarrayX[0].length == 0)	{
						// something wrong
						return false;
					}
				} else if (nNumofLines % 4 == 3)	{
					// Y
					m3DSurfaces[nCurveId].mdblarrayY = getValueArray(strLine);
					if (m3DSurfaces[nCurveId].mdblarrayY.length != m3DSurfaces[nCurveId].mdblarrayX.length
                            || m3DSurfaces[nCurveId].mdblarrayY[0].length != m3DSurfaces[nCurveId].mdblarrayX[0].length)	{
						// something wrong
						return false;
					}
				} else if (nNumofLines % 4 == 0)	{
					// Z
					m3DSurfaces[nCurveId].mdblarrayZ = getValueArray(strLine);
					if (m3DSurfaces[nCurveId].mdblarrayZ.length != m3DSurfaces[nCurveId].mdblarrayX.length
                            || m3DSurfaces[nCurveId].mdblarrayZ[0].length != m3DSurfaces[nCurveId].mdblarrayX[0].length)	{
						// something wrong
						return false;
					}
				} else	{
					// curve settings
					getCurveSettings(strLine, m3DSurfaces[nCurveId]);
				}
			}
		}
		return true;
	}
	
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        OGLChart oglChart = new OGLChart(context);
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
        
        for (int nIndex = 0; nIndex < mnNumofCurves; nIndex ++)	{
    		DataSeriesGridSurface dsgs = new DataSeriesGridSurface("xy");
            int nDim1 = m3DSurfaces[nIndex].mdblarrayX.length;
            int nDim2 = m3DSurfaces[nIndex].mdblarrayX[0].length;
            Position3D[][] arrayCvtPnts = new Position3D[nDim1][nDim2];
            for (int idx1 = 0; idx1 < nDim1; idx1 ++)   {
                for (int idx2 = 0; idx2 < nDim2; idx2 ++)   {
                    arrayCvtPnts[idx1][idx2] = new Position3D(m3DSurfaces[nIndex].mdblarrayX[idx1][idx2],
                            m3DSurfaces[nIndex].mdblarrayY[idx1][idx2], m3DSurfaces[nIndex].mdblarrayZ[idx1][idx2]);
                }
            }
            dsgs.mstrName = m3DSurfaces[nIndex].mstrCurveLabel;
            dsgs.setByMatrix(arrayCvtPnts);
            dsgs.msurfaceStyle = new SurfaceStyle();
            dsgs.msurfaceStyle.menumSurfaceType = m3DSurfaces[nIndex].mbIsGrid?SURFACETYPE.SURFACETYPE_GRID:SURFACETYPE.SURFACETYPE_SURFACE;
            dsgs.msurfaceStyle.mclrUpFaceMin = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMinColor);
            dsgs.msurfaceStyle.mclrDownFaceMin = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMinColor1);
            if (m3DSurfaces[nIndex].mdMinColorValue == null)    {
                dsgs.msurfaceStyle.mdUpFaceMinValue = dsgs.msurfaceStyle.mdDownFaceMinValue
                    = ((dsgs.getMode() == 2)?dsgs.getMinCvtedY():((dsgs.getMode()==1)?dsgs.getMinCvtedX():dsgs.getMinCvtedZ()));
            } else  {
                dsgs.msurfaceStyle.mdUpFaceMinValue = dsgs.msurfaceStyle.mdDownFaceMinValue = m3DSurfaces[nIndex].mdMinColorValue;
            }
            dsgs.msurfaceStyle.mclrUpFaceMax = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMaxColor);
            dsgs.msurfaceStyle.mclrDownFaceMax = cvtStr2VMFPColor(m3DSurfaces[nIndex].mstrMaxColor1);
            if (m3DSurfaces[nIndex].mdMaxColorValue == null)    {
                dsgs.msurfaceStyle.mdUpFaceMaxValue = dsgs.msurfaceStyle.mdDownFaceMaxValue
                    = ((dsgs.getMode() == 2)?dsgs.getMaxCvtedY():((dsgs.getMode()==1)?dsgs.getMaxCvtedX():dsgs.getMaxCvtedZ()));
            } else  {
                dsgs.msurfaceStyle.mdUpFaceMaxValue = dsgs.msurfaceStyle.mdDownFaceMaxValue = m3DSurfaces[nIndex].mdMaxColorValue;
            }
        

    		oglChart.mDataSet.add(dsgs);
        }
        
        return oglChart;
	}

}
