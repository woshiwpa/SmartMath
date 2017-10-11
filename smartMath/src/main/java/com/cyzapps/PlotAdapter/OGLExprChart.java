package com.cyzapps.PlotAdapter;

import javax.microedition.khronos.opengles.GL10;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.cyzapps.SmartMath.R;
import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.GraphDemon.ActivityConfig3DExprGraph.AdjOGLExprChartParams;
import com.cyzapps.Jfcalc.PlotLib;
import com.cyzapps.Jfcalc.PlotLib.ThreeDExprSurface;
import com.cyzapps.adapter.MFPAdapter;

public class OGLExprChart extends OGLChart {
	public ThreeDExprSurface[] m3DExprSurfaces = new ThreeDExprSurface[0]; 
	
	public Handler mhandler = new Handler();
	public ProgressDialog mdlgDataRecalcing;
	
	protected double mdXFrom = 0, mdYFrom = 0, mdZFrom = 0, mdXTo = 0, mdYTo = 0, mdZTo = 0;
	protected boolean mbCfgChangeApplied = false;
		
	public OGLExprChart(Context context) {
		super(context);
	}

	public AdjOGLExprChartParams getChartDataParams()	{
		AdjOGLExprChartParams adjParam = new AdjOGLExprChartParams();
		adjParam.mdXFrom = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 - (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdXTo = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 + (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdYFrom = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 - (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdYTo = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 + (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdZFrom = (mcaZAxis.mdValueTo + mcaZAxis.mdValueFrom)/2.0 - (mcaZAxis.mdValueTo - mcaZAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdZTo = (mcaZAxis.mdValueTo + mcaZAxis.mdValueFrom)/2.0 + (mcaZAxis.mdValueTo - mcaZAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		for (int idx = 0; idx < m3DExprSurfaces.length; idx ++)	{
			if (idx == 0)	{
				adjParam.mnXNumOfSteps = m3DExprSurfaces[idx].mnXNumOfSteps;
				adjParam.mnYNumOfSteps = m3DExprSurfaces[idx].mnYNumOfSteps;
				adjParam.mnZNumOfSteps = m3DExprSurfaces[idx].mnZNumOfSteps;
			} else	{
				if (adjParam.mnXNumOfSteps > m3DExprSurfaces[idx].mnXNumOfSteps)	{
					adjParam.mnXNumOfSteps = m3DExprSurfaces[idx].mnXNumOfSteps;
				}
				if (adjParam.mnYNumOfSteps > m3DExprSurfaces[idx].mnYNumOfSteps)	{
					adjParam.mnYNumOfSteps = m3DExprSurfaces[idx].mnYNumOfSteps;
				}
				if (adjParam.mnZNumOfSteps > m3DExprSurfaces[idx].mnZNumOfSteps)	{
					adjParam.mnZNumOfSteps = m3DExprSurfaces[idx].mnZNumOfSteps;
				}
			}
		}
		if (adjParam.mnXNumOfSteps <= 0)	{
			adjParam.mnXNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		}
		if (adjParam.mnYNumOfSteps <= 0)	{
			adjParam.mnYNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		}
		if (adjParam.mnZNumOfSteps <= 0)	{
			adjParam.mnZNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		}
        adjParam.mbNotShowAxisAndTitle = mbNotDrawAxisAndTitle;
		return adjParam;
	}
	
	public void updateUponDataRange(double dXMin, double dXMax, double dYMin, double dYMax, double dZMin, double dZMax)	{
		double dXMaxShouldBe = 0, dXMinShouldBe = 0, dYMaxShouldBe = 0, dYMinShouldBe = 0, dZMaxShouldBe = 0, dZMinShouldBe = 0;
   		dXMinShouldBe = (dXMax + dXMin)/2.0 - (dXMax - dXMin)/2.0 / PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dXMaxShouldBe = (dXMax + dXMin)/2.0 + (dXMax - dXMin)/2.0 / PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMinShouldBe = (dYMax + dYMin)/2.0 - (dYMax - dYMin)/2.0 / PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMaxShouldBe = (dYMax + dYMin)/2.0 + (dYMax - dYMin)/2.0 / PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dZMinShouldBe = (dZMax + dZMin)/2.0 - (dZMax - dZMin)/2.0 / PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dZMaxShouldBe = (dZMax + dZMin)/2.0 + (dZMax - dZMin)/2.0 / PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		double dXShift = (dXMaxShouldBe + dXMinShouldBe)/2.0 - (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0;
   		double dYShift = (dYMaxShouldBe + dYMinShouldBe)/2.0 - (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0;
   		double dZShift = (dZMaxShouldBe + dZMinShouldBe)/2.0 - (mcaZAxis.mdValueTo + mcaZAxis.mdValueFrom)/2.0;
   		
   		
   		double dXZoom = 1;
   		if (dXMaxShouldBe != dXMinShouldBe && mcaXAxis.mdValueTo != mcaXAxis.mdValueFrom)	{
   			dXZoom = (dXMaxShouldBe - dXMinShouldBe)/(mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom);
   		}
   		double dYZoom = 1;
   		if (dYMaxShouldBe != dYMinShouldBe && mcaYAxis.mdValueTo != mcaYAxis.mdValueFrom)	{
   			dYZoom = (dYMaxShouldBe - dYMinShouldBe)/(mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom);
   		}
   		double dZZoom = 1;
   		if (dZMaxShouldBe != dZMinShouldBe && mcaZAxis.mdValueTo != mcaZAxis.mdValueFrom)	{
   			dZZoom = (dZMaxShouldBe - dZMinShouldBe)/(mcaZAxis.mdValueTo - mcaZAxis.mdValueFrom);
   		}
   		
        updateMapperFROM2FROM(dXShift, dYShift, dZShift, 0, 0, 0, 1, 1, 1);
        updateMapperFROM2FROM(0, 0, 0, 0, 0, 0, dXZoom, dYZoom, dZZoom);
        update();
	}
	
	@Override
	public synchronized void zoom(double dRatio)	{
		if (dRatio != 1 && mdlgDataRecalcing == null)	{
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
    											mcontext.getString(R.string.calculating_chart_data), true);
			super.zoom(dRatio);
		}
	}
	
	public synchronized void applyCfgChart(AdjOGLExprChartParams adjParams)	{
		double dXMax = 0, dXMin = 0, dYMax = 0, dYMin = 0, dZMax = 0, dZMin = 0;
   		dXMin = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 - (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dXMax = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 + (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMin = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 - (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMax = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 + (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dZMin = (mcaZAxis.mdValueTo + mcaZAxis.mdValueFrom)/2.0 - (mcaZAxis.mdValueTo - mcaZAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
   		dZMax = (mcaZAxis.mdValueTo + mcaZAxis.mdValueFrom)/2.0 + (mcaZAxis.mdValueTo - mcaZAxis.mdValueFrom)/2.0 * PlotLib.THREED_EXPR_CHART_AXIS_FILL_RATIO;
		int nXNumOfSteps = -1, nYNumOfSteps = -1, nZNumOfSteps = -1;
		for (int idx = 0; idx < m3DExprSurfaces.length; idx ++)	{
			if (nXNumOfSteps == -1 || nXNumOfSteps == m3DExprSurfaces[idx].mnXNumOfSteps)	{
				nXNumOfSteps = m3DExprSurfaces[idx].mnXNumOfSteps;
			} else	{
				nXNumOfSteps = -2;	// not a single nXNumOfSteps.
				break;
			}
			if (nYNumOfSteps == -1 || nYNumOfSteps == m3DExprSurfaces[idx].mnYNumOfSteps)	{
				nYNumOfSteps = m3DExprSurfaces[idx].mnYNumOfSteps;
			} else	{
				nYNumOfSteps = -2;	// not a single nYNumOfSteps.
				break;
			}
			if (nZNumOfSteps == -1 || nZNumOfSteps == m3DExprSurfaces[idx].mnZNumOfSteps)	{
				nZNumOfSteps = m3DExprSurfaces[idx].mnZNumOfSteps;
			} else	{
				nZNumOfSteps = -2;	// not a single nZNumOfSteps.
				break;
			}
		}
   		if (!adjParams.isNoAdj(dXMin, dXMax, nXNumOfSteps, dYMin, dYMax, nYNumOfSteps, dZMin, dZMax, nZNumOfSteps, mbNotDrawAxisAndTitle) && mdlgDataRecalcing == null)	{
			if (mdlgDataRecalcing == null)	{
	   			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
						mcontext.getString(R.string.calculating_chart_data), true);
			}
			for (int idx = 0; idx < m3DExprSurfaces.length; idx ++)	{
				m3DExprSurfaces[idx].mnXNumOfSteps = adjParams.mnXNumOfSteps;
				m3DExprSurfaces[idx].mnYNumOfSteps = adjParams.mnYNumOfSteps;
				m3DExprSurfaces[idx].mnZNumOfSteps = adjParams.mnZNumOfSteps;
			}
            mbNotDrawAxisAndTitle = adjParams.mbNotShowAxisAndTitle;
			updateUponDataRange(adjParams.mdXFrom, adjParams.mdXTo, adjParams.mdYFrom, adjParams.mdYTo, adjParams.mdZFrom, adjParams.mdZTo);
			mbCfgChangeApplied = true;
   		}
	}
	
	@Override
	public void clickCfgBtn()	{
		((ActivityChartDemon) mcontext).startCfgOGLExprChart();
	}
	
	@Override
	public synchronized void clickZoom1Btn()	{
		if (mdlgDataRecalcing == null)	{
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
					mcontext.getString(R.string.calculating_chart_data), true);
			super.clickZoom1Btn();
		}
	}
	
	@Override
	public synchronized void clickZoomFitBtn()	{
		if (mdlgDataRecalcing == null)	{
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
					mcontext.getString(R.string.calculating_chart_data), true);
			double dXMax = 0, dXMin = 0, dYMax = 0, dYMin = 0, dZMax = 0, dZMin = 0;
			for (int idx = 0; idx < mDataSet.size(); idx ++)	{
				if (idx == 0)	{
					dXMax = mDataSet.get(idx).getMaxCvtedX();
					dXMin = mDataSet.get(idx).getMinCvtedX();
					dYMax = mDataSet.get(idx).getMaxCvtedY();
					dYMin = mDataSet.get(idx).getMinCvtedY();
					dZMax = mDataSet.get(idx).getMaxCvtedZ();
					dZMin = mDataSet.get(idx).getMinCvtedZ();
				} else	{
					dXMax = (dXMax < mDataSet.get(idx).getMaxCvtedX())?mDataSet.get(idx).getMaxCvtedX():dXMax;
					dXMin = (dXMin > mDataSet.get(idx).getMinCvtedX())?mDataSet.get(idx).getMinCvtedX():dXMin;
					dYMax = (dYMax < mDataSet.get(idx).getMaxCvtedY())?mDataSet.get(idx).getMaxCvtedY():dYMax;
					dYMin = (dYMin > mDataSet.get(idx).getMinCvtedY())?mDataSet.get(idx).getMinCvtedY():dYMin;
					dZMax = (dZMax < mDataSet.get(idx).getMaxCvtedZ())?mDataSet.get(idx).getMaxCvtedZ():dZMax;
					dZMin = (dZMin > mDataSet.get(idx).getMinCvtedZ())?mDataSet.get(idx).getMinCvtedZ():dZMin;
				}
			}
	   		/* disable the following statements which may cause negative axis length.
	   		if (dXMinShouldBe < mcaXAxis.mdValueFrom)	{
	   			dXMinShouldBe = mcaXAxis.mdValueFrom;
	   		}
	   		if (dXMaxShouldBe > mcaXAxis.mdValueTo)	{
	   			dXMaxShouldBe = mcaXAxis.mdValueTo;
	   		}
	   		if (dYMinShouldBe < mcaYAxis.mdValueFrom)	{
	   			dYMinShouldBe = mcaYAxis.mdValueFrom;
	   		}
	   		if (dYMaxShouldBe > mcaYAxis.mdValueTo)	{
	   			dYMaxShouldBe = mcaYAxis.mdValueTo;
	   		}
	   		if (dZMinShouldBe < mcaZAxis.mdValueFrom)	{
	   			dZMinShouldBe = mcaZAxis.mdValueFrom;
	   		}
	   		if (dZMaxShouldBe > mcaZAxis.mdValueTo)	{
	   			dZMaxShouldBe = mcaZAxis.mdValueTo;
	   		}*/
	   		
			updateUponDataRange(dXMin, dXMax, dYMin, dYMax, dZMin, dZMax);
		}
	}
	
	@Override
	public synchronized void clickZoomInBtn()	{
		if (mdlgDataRecalcing == null)	{
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
					mcontext.getString(R.string.calculating_chart_data), true);
			super.clickZoomInBtn();
		}
	}
	
	@Override
	public synchronized void clickZoomOutBtn()	{
		if (mdlgDataRecalcing == null)	{
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
					mcontext.getString(R.string.calculating_chart_data), true);
			super.clickZoomOutBtn();
		}
	}
	
	@Override
	public synchronized void initialize()	{
		super.initialize();
		if (mdlgDataRecalcing == null)	{	// because it is initializing, mdlgDataRecalcing is always null.
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
					mcontext.getString(R.string.calculating_chart_data), true);
		}
	}
	
	// update data here
	@Override
	public void draw(GL10 gl)	{
		if (mdXFrom != mcaXAxis.mdValueFrom || mdXTo != mcaXAxis.mdValueTo
				|| mdYFrom != mcaYAxis.mdValueFrom || mdYTo != mcaYAxis.mdValueTo
				|| mdZFrom != mcaZAxis.mdValueFrom || mdZTo != mcaZAxis.mdValueTo
				|| mbCfgChangeApplied)	{	// cfg change applied may means number of steps change.
			if (MFPAdapter.isFuncSpaceEmpty())	{	// before recalc, we may need reload libs.
				// only 3DExpr and 2DExpr charts need to reload the functions
				MFPAdapter.reloadAll(mcontext, 2, null);	// use sync mode here
			}
				// update dataset.
			mDataSet = PlotLib.recalc3DExprDataSet(mcaXAxis.mdValueFrom, mcaXAxis.mdValueTo, mcaYAxis.mdValueFrom, mcaYAxis.mdValueTo, 
					mcaZAxis.mdValueFrom, mcaZAxis.mdValueTo, m3DExprSurfaces);
			mdXFrom = mcaXAxis.mdValueFrom;
			mdXTo = mcaXAxis.mdValueTo;
			mdYFrom = mcaYAxis.mdValueFrom;
			mdYTo = mcaYAxis.mdValueTo;
			mdZFrom = mcaZAxis.mdValueFrom;
			mdZTo = mcaZAxis.mdValueTo;
			mbCfgChangeApplied = false;
		}
    	// everything is done successfully.
		mhandler.post(new Runnable()	{
			@Override
			public void run() {
				if (mdlgDataRecalcing != null)	{
					mdlgDataRecalcing.dismiss();
					mdlgDataRecalcing = null;
				}
			}
		});
		
		super.draw(gl);
	}
}
