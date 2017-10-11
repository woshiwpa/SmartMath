package com.cyzapps.PlotAdapter;

import android.content.Context;
import com.cyzapps.VisualMFP.DataSeriesCurve;
import com.cyzapps.VisualMFP.LineStyle;
import com.cyzapps.VisualMFP.PointStyle;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.LineStyle.LINEPATTERN;

public class PolarChartOperator extends XYChartOperator {
	public String mstrChartType = "multiRangle";
	
	public PolarChartOperator()	{
		super();
		mdblYMin = -Math.PI;
		mdblYMax = Math.PI;
	}
			
	/* create chart */
	@Override
	public MFPChart createChart(ChartCreationParam ccpParam1, Context context) {
		
        FlatChart flatChart = new PolarChart(context);
        flatChart.mstrChartTitle = mstrChartTitle;
        flatChart.mcolorBkGrnd = new com.cyzapps.VisualMFP.Color(255, 0, 0, 0);
        flatChart.mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 200, 200, 200);
        ((PolarChart)flatChart).mstrXAxisName = mstrXTitle;
        ((PolarChart)flatChart).mstrYAxisName = mstrYTitle;
        ((PolarChart)flatChart).mbShowGrid = mbShowGrid;
        
        
        ((PolarChart)flatChart).mdXAxisLenInFROM = Math.max(Math.abs(mdblXMax), Math.abs(mdblXMin));	// support negative value.
        double dXAxisShownRange = ((mdblXMax * mdblXMin) >= 0)?Math.abs(mdblXMax - mdblXMin)
        		:((PolarChart)flatChart).mdXAxisLenInFROM;
        double dXMarkInterval = dXAxisShownRange/ccpParam1.mnRecommendedNumOfMarksPerAxis;
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
        
        double dYMarkInterval = (mdblYMax - mdblYMin)/8;

        ((PolarChart)flatChart).mdXMark1 = 0;
        ((PolarChart)flatChart).mdXMark2 = dXMarkInterval;
        ((PolarChart)flatChart).mdYMark1 = 0;
        ((PolarChart)flatChart).mdYMark2 = dYMarkInterval;

        flatChart.mbUseInit2CalibrateZoom = true;
        //flatChart.saveSettings(); not call save settings here coz coordOrigin and axis len haven't been caclulated.
        
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
    		((PolarChart)flatChart).mDataSet.add(dsv);
        }
        
        return flatChart;
	}

}
