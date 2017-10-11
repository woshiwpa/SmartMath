package com.cyzapps.PlotAdapter;

import com.cyzapps.SmartMath.R;
import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.GraphDemon.ActivityConfig2DExprGraph.AdjXYExprChartParams;
import com.cyzapps.Jfcalc.PlotLib;
import com.cyzapps.Jfcalc.PlotLib.TwoDExprCurve;
import com.cyzapps.Jfcalc.TwoDExprDataCache;
import com.cyzapps.adapter.MFPAdapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;

public class XYExprChart extends XYChart {

	public TwoDExprCurve[] m2DExprCurves = new TwoDExprCurve[0]; 

	public Handler mhandler = new Handler();;
	public ProgressDialog mdlgDataRecalcing;	// handler is not needed here because updateCoordAxis() is not done in main thread.

	protected double mdXFrom = 0, mdYFrom = 0, mdXTo = 0, mdYTo = 0;
	protected boolean mbCfgChangeApplied = false;
	
	protected TwoDExprDataCache[] m2DExprDataCaches = new TwoDExprDataCache[0];
	public XYExprChart(Context context) {
		super(context);
	}
	
	public AdjXYExprChartParams getChartDataParams()	{
		AdjXYExprChartParams adjParam = new AdjXYExprChartParams();
		adjParam.mdXFrom = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 - (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdXTo = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 + (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdYFrom = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 - (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
		adjParam.mdYTo = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 + (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
		for (int idx = 0; idx < m2DExprCurves.length; idx ++)	{
			if (idx == 0)	{
				adjParam.mnNumOfSteps = m2DExprCurves[idx].mnNumOfSteps;
				adjParam.mbAutoStep = m2DExprCurves[idx].mbAutoStep;
			} else	{
				if (adjParam.mnNumOfSteps > m2DExprCurves[idx].mnNumOfSteps)	{
					adjParam.mnNumOfSteps = m2DExprCurves[idx].mnNumOfSteps;
					if (adjParam.mbAutoStep == false)	{
						adjParam.mbAutoStep = m2DExprCurves[idx].mbAutoStep;
					}
				}
			}
		}
		if (adjParam.mnNumOfSteps <= 0)	{
			adjParam.mnNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		}
		return adjParam;
	}
	
	public void updateUponDataRange(double dXMin, double dXMax, double dYMin, double dYMax)	{
		double dXMaxShouldBe = 0, dXMinShouldBe = 0, dYMaxShouldBe = 0, dYMinShouldBe = 0;
   		dXMinShouldBe = (dXMax + dXMin)/2.0 - (dXMax - dXMin)/2.0 / PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		dXMaxShouldBe = (dXMax + dXMin)/2.0 + (dXMax - dXMin)/2.0 / PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMinShouldBe = (dYMax + dYMin)/2.0 - (dYMax - dYMin)/2.0 / PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMaxShouldBe = (dYMax + dYMin)/2.0 + (dYMax - dYMin)/2.0 / PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		
   		double dXZoom = 1;
   		if (dXMaxShouldBe != dXMinShouldBe && mcaXAxis.mdValueTo != mcaXAxis.mdValueFrom)	{
   			dXZoom = (dXMaxShouldBe - dXMinShouldBe)/(mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom);
   		}
   		double dYZoom = 1;
   		if (dYMaxShouldBe != dYMinShouldBe && mcaYAxis.mdValueTo != mcaYAxis.mdValueFrom)	{
   			dYZoom = (dYMaxShouldBe - dYMinShouldBe)/(mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom);
   		}
   		double dOldX = ((mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 - (dXMaxShouldBe - dXMinShouldBe)/2.0),
   				dOldY = ((mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 - (dYMaxShouldBe - dYMinShouldBe)/2.0);
   		double dNewX = dXMinShouldBe, dNewY = dYMinShouldBe;
   		
        super.zoom(1/dXZoom, 1/dYZoom);
        super.slide(dNewX * getScalingRatioX(), dNewY * getScalingRatioY(), dOldX * getScalingRatioX(), dOldY * getScalingRatioY());	// mind that slide is - (new - old) not + (new - old)
	}

	public void applyCfgChart(AdjXYExprChartParams adjParams)	{
		double dXMax = 0, dXMin = 0, dYMax = 0, dYMin = 0;
   		dXMin = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 - (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		dXMax = (mcaXAxis.mdValueTo + mcaXAxis.mdValueFrom)/2.0 + (mcaXAxis.mdValueTo - mcaXAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMin = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 - (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
   		dYMax = (mcaYAxis.mdValueTo + mcaYAxis.mdValueFrom)/2.0 + (mcaYAxis.mdValueTo - mcaYAxis.mdValueFrom)/2.0 * PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO;
		int nNumOfSteps = -1;
		boolean bAutoStep = true, bSameAutoStep = true;
		for (int idx = 0; idx < m2DExprCurves.length; idx ++)	{
			if (idx == 0)	{
				bAutoStep = m2DExprCurves[idx].mbAutoStep;
			} else if (bAutoStep != m2DExprCurves[idx].mbAutoStep)	{
				bSameAutoStep = false;
				break;
			}
			int nThisNumOfSteps = m2DExprCurves[idx].mnNumOfSteps;
			if (bAutoStep)	{
				nThisNumOfSteps = XYExprChartOperator.DEFAULT_NUM_OF_STEPS;
			}
			if (nNumOfSteps == -1 || nNumOfSteps == nThisNumOfSteps)	{
				nNumOfSteps = nThisNumOfSteps;
			} else	{
				nNumOfSteps = -2;	// not a single nXNumOfSteps.
				break;
			}
		}
   		if (nNumOfSteps == -2 || bSameAutoStep == false || !adjParams.isNoAdj(dXMin, dXMax, dYMin, dYMax, nNumOfSteps, bAutoStep))	{
   			if (mdlgDataRecalcing == null)	{
				mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
						mcontext.getString(R.string.calculating_chart_data), true);
				for (int idx = 0; idx < m2DExprCurves.length; idx ++)	{
					m2DExprCurves[idx].mnNumOfSteps = adjParams.mnNumOfSteps;
					m2DExprCurves[idx].mbAutoStep = adjParams.mbAutoStep;
				}
				updateUponDataRange(adjParams.mdXFrom, adjParams.mdXTo, adjParams.mdYFrom, adjParams.mdYTo);
				mbCfgChangeApplied = true;
			}
   		}
		
	}
	
	@Override
	public void config()	{
		((ActivityChartDemon) mcontext).startCfg2DExprChart();
	}
	
	@Override
	public void zoom(double dXRatio, double dYRatio)	{
		if (dXRatio != 1 || dYRatio != 1)	{
			if (mdlgDataRecalcing == null)	{
				mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
    											mcontext.getString(R.string.calculating_chart_data), true);
				super.zoom(dXRatio, dYRatio);
			}
		}
	}
	
	@Override
	public void zoomReset()	{
		if (mdlgDataRecalcing == null)	{
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
    											mcontext.getString(R.string.calculating_chart_data), true);
			super.zoomReset();
		}
	}
	
	@Override
	public void slide(double dOldX, double dOldY, double dNewX, double dNewY)	{
		if (dOldX != dNewX && dOldY != dNewY)	{
			if (mdlgDataRecalcing == null)	{
				mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
    											mcontext.getString(R.string.calculating_chart_data), true);
				super.slide(dOldX, dOldY, dNewX, dNewY);
			}
		}
	}

	@Override
	public void initialize()	{
		// resize must be down whether there is progressing dialog or not
		super.initialize();
		if (mdlgDataRecalcing == null)	{	// because it is initializing, mdlgDataRecalcing is always null.
			mdlgDataRecalcing = ProgressDialog.show(mcontext, mcontext.getString(R.string.please_wait),
					mcontext.getString(R.string.calculating_chart_data), true);
		}
	}
	
	@Override
	public void draw(Canvas canvas, double x, double y, double width, double height, Paint paint)	{
		if (mdXFrom != mcaXAxis.mdValueFrom || mdXTo != mcaXAxis.mdValueTo
				|| mdYFrom != mcaYAxis.mdValueFrom || mdYTo != mcaYAxis.mdValueTo
				|| mbCfgChangeApplied)	{	// cfg change applied may means number of steps change.)
			if (MFPAdapter.isFuncSpaceEmpty())	{	// before recalc, we may need reload libs.
				// only 3DExpr and 2DExpr charts need to reload the functions
				MFPAdapter.reloadAll(mcontext, 2, null);	// use sync mode here
			}
			// update dataset.
			mDataSet = PlotLib.recalc2DExprDataSet(mcaXAxis.mdValueFrom, mcaXAxis.mdValueTo, mcaYAxis.mdValueFrom, mcaYAxis.mdValueTo, PlotLib.TWOD_EXPR_CHART_AXIS_FILL_RATIO, m2DExprCurves, m2DExprDataCaches);
			
			mdXFrom = mcaXAxis.mdValueFrom;
			mdXTo = mcaXAxis.mdValueTo;
			mdYFrom = mcaYAxis.mdValueFrom;
			mdYTo = mcaYAxis.mdValueTo;
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
		
		super.draw(canvas, x, y, width, height, paint);
	}
}
