package com.cyzapps.GraphDemon;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

import com.cyzapps.SmartMath.R;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.PlotAdapter.ChartOperator.ChartCreationParam;
import com.cyzapps.PlotAdapter.FlatChartView;
import com.cyzapps.PlotAdapter.FlatChart;
import com.cyzapps.PlotAdapter.MFPChart;
import com.cyzapps.PlotAdapter.OGLChart;
import com.cyzapps.PlotAdapter.OGLChartOperator;
import com.cyzapps.PlotAdapter.OGLChartView;
import com.cyzapps.PlotAdapter.OGLExprChart;
import com.cyzapps.PlotAdapter.OGLExprChartOperator;
import com.cyzapps.PlotAdapter.PolarChartOperator;
import com.cyzapps.PlotAdapter.PolarExprChart;
import com.cyzapps.PlotAdapter.PolarExprChartOperator;
import com.cyzapps.PlotAdapter.XYChartOperator;
import com.cyzapps.PlotAdapter.XYExprChart;
import com.cyzapps.PlotAdapter.XYExprChartOperator;
import com.cyzapps.adapter.AndroidStorageOptions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;

public class ActivityChartDemon extends Activity {
	/** The encapsulated graphical view. */
	public FlatChartView mflatChartView = null;
	public OGLChartView moglChartView = null;
	/** The chart to be drawn. */
	public MFPChart mmfpChart = null;

    public static final int CONFIG_XYEXPRCHART_ACTIVITY = 1;
    public static final int CONFIG_POLAREXPRCHART_ACTIVITY = 2;
    public static final int CONFIG_OGLCHART_ACTIVITY = 3;
    public static final int CONFIG_OGLEXPRCHART_ACTIVITY = 4;
    
    public static final String PLOTTING_TS = "plotting_timestamp";
    public long mlPlottingTime = 0L;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String strChart = extras.getString(ChartOperator.VMFPChart);
        mlPlottingTime = extras.getLong("GraphTriggerTime");	// get plotting time if set timestamp since 1970.1.1
        
        int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int nRecommendedNumOfMarksPerAxis = 10;
        if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{
        	nRecommendedNumOfMarksPerAxis = 3;
        } else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
        	nRecommendedNumOfMarksPerAxis = 4;
        } else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
        	nRecommendedNumOfMarksPerAxis = 5;
        } else	{
        	nRecommendedNumOfMarksPerAxis = 6;
        }
        XYChartOperator xyChartOperator = new XYChartOperator();
        XYExprChartOperator xyExprChartOperator = new XYExprChartOperator();
        PolarChartOperator polarChartOperator = new PolarChartOperator();
        PolarExprChartOperator polarExprChartOperator = new PolarExprChartOperator();
        OGLChartOperator oglChartOperator = new OGLChartOperator();
        OGLExprChartOperator oglExprChartOperator = new OGLExprChartOperator();
        ChartCreationParam ccp = new ChartCreationParam();
        ccp.mnRecommendedNumOfMarksPerAxis = nRecommendedNumOfMarksPerAxis;
        String strChartType = "Invalid";
        if (strChart == null)	{
        	strChart = extras.getString(ChartOperator.VMFPChartPath);
        	if (strChart == null)	{
        		showErrorMsg(getString(R.string.no_graph_input), true);
        	} else	{
        		strChartType = ChartOperator.getChartTypeFromFile(strChart);
        		if (strChartType.compareToIgnoreCase("multiXY") == 0)	{
		        	if (xyChartOperator.loadFromFile(strChart) == false)	{
		    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
		    		} else	{
		    	        mmfpChart = (MFPChart) xyChartOperator.createChart(ccp, this);
		    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
		        	}
	        	} else if (strChartType.compareToIgnoreCase("2DExpr") == 0)	{
		        	if (xyExprChartOperator.loadFromFile(strChart) == false)	{
		    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
		    		} else	{
		    			// load function libs should be when initialize the chart.
		    	        mmfpChart = (MFPChart) xyExprChartOperator.createChart(ccp, this);
		    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
		        	}
	        	} else if (strChartType.compareToIgnoreCase("multiRangle") == 0)	{
		        	if (polarChartOperator.loadFromFile(strChart) == false)	{
		    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
		    		} else	{
		    	        mmfpChart = (MFPChart) polarChartOperator.createChart(ccp, this);
		    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
		        	}
	        	} else if (strChartType.compareToIgnoreCase("polarExpr") == 0)	{
		        	if (polarExprChartOperator.loadFromFile(strChart) == false)	{
		    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
		    		} else	{
		    			// load function libs should be when initialize the chart.
		    	        mmfpChart = (MFPChart) polarExprChartOperator.createChart(ccp, this);
		    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
		        	}
	        	} else if (strChartType.compareToIgnoreCase("multiXYZ") == 0)	{
		        	if (oglChartOperator.loadFromFile(strChart) == false)	{
		    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
		    		} else	{
		    	        mmfpChart = (MFPChart) oglChartOperator.createChart(ccp, this);
		    	        moglChartView = new OGLChartView(this, (OGLChart)mmfpChart);
		        	}
	        	} else if (strChartType.compareToIgnoreCase("3DExpr") == 0)	{
		        	if (oglExprChartOperator.loadFromFile(strChart) == false)	{
		    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
		    		} else	{
		    			// load function libs should be when initialize the chart.
		    	        mmfpChart = (MFPChart) oglExprChartOperator.createChart(ccp, this);
		    	        moglChartView = new OGLChartView(this, (OGLChart)mmfpChart);
		        	}
	        	} else	{
	        		showErrorMsg(getString(R.string.graph_settings_wrong), true);
	        	}
        	}
        } else	{
        	strChartType = ChartOperator.getChartTypeFromString(strChart);
    		if (strChartType.compareToIgnoreCase("multiXY") == 0)	{
	        	if (xyChartOperator.loadFromString(strChart) == false)	{
	    			showErrorMsg(getString(R.string.graph_settings_wrong), true);
	    		} else	{
	    	        mmfpChart = (MFPChart) xyChartOperator.createChart(ccp, this);
	    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
	        	}
        	} else if (strChartType.compareToIgnoreCase("2DExpr") == 0)	{
	        	if (xyExprChartOperator.loadFromString(strChart) == false)	{
	    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
	    		} else	{
	    			// load function libs should be when initialize the chart.
	    	        mmfpChart = (MFPChart) xyExprChartOperator.createChart(ccp, this);
	    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
	        	}
        	} else if (strChartType.compareToIgnoreCase("multiRangle") == 0)	{
	        	if (polarChartOperator.loadFromString(strChart) == false)	{
	    			showErrorMsg(getString(R.string.graph_settings_wrong), true);
	    		} else	{
	    	        mmfpChart = (MFPChart) polarChartOperator.createChart(ccp, this);
	    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
	        	}
        	} else if (strChartType.compareToIgnoreCase("polarExpr") == 0)	{
	        	if (polarExprChartOperator.loadFromString(strChart) == false)	{
	    			showErrorMsg(getString(R.string.graph_file_cannot_be_read), true);
	    		} else	{
	    			// load function libs should be when initialize the chart.
	    	        mmfpChart = (MFPChart) polarExprChartOperator.createChart(ccp, this);
	    	        mflatChartView = new FlatChartView(this, (FlatChart)mmfpChart);
	        	}
        	} else if (strChartType.compareToIgnoreCase("multiXYZ") == 0)	{
	        	if (oglChartOperator.loadFromString(strChart) == false)	{
	    			showErrorMsg(getString(R.string.graph_settings_wrong), true);
	    		} else	{
	    	        mmfpChart = (MFPChart) oglChartOperator.createChart(ccp, this);
	    	        moglChartView = new OGLChartView(this, (OGLChart)mmfpChart);
	        	}
        	} else if (strChartType.compareToIgnoreCase("3DExpr") == 0)	{
	        	if (oglExprChartOperator.loadFromString(strChart) == false)	{
	    			showErrorMsg(getString(R.string.graph_settings_wrong), true);
	    		} else	{
	    			// load function libs should be when initialize the chart.
	    	        mmfpChart = (MFPChart) oglExprChartOperator.createChart(ccp, this);
	    	        moglChartView = new OGLChartView(this, (OGLChart)mmfpChart);
	        	}
        	} else	{
        		showErrorMsg(getString(R.string.graph_settings_wrong), true);
        	}
		}
        
        if (mmfpChart != null)	{	// create successfully.
	        if (strChartType.compareToIgnoreCase("multiXY") == 0 || strChartType.compareToIgnoreCase("2DExpr") == 0
	        		|| strChartType.compareToIgnoreCase("multiRangle") == 0 || strChartType.compareToIgnoreCase("polarExpr") == 0)	{
				Display d = getWindowManager().getDefaultDisplay();
				DisplayMetrics metrics = new DisplayMetrics();
				d.getMetrics(metrics);
				int nScreenWidthPixels = d.getWidth(); 
				int nScreenHeightPixels = d.getHeight();
				int nScreenShortDimSize = Math.min(nScreenWidthPixels, nScreenHeightPixels);
				double dOnePixelToCm = 1.0f/160.0f/metrics.density * 2.54f;	// 1 inche = 2.54cm
				((FlatChart)mmfpChart).mdMediumSize = Math.max(0.5/dOnePixelToCm, nScreenShortDimSize/16.0);	// medium size should be at least 0.5cm
				((FlatChart)mmfpChart).resetAllSizesBasedOnMedium();
				
				requestWindowFeature(Window.FEATURE_NO_TITLE);
	    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	    				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
				setContentView(mflatChartView);
	        } else if(strChartType.compareToIgnoreCase("multiXYZ") == 0 || strChartType.compareToIgnoreCase("3DExpr") == 0)	{
	        	((OGLChart)mmfpChart).mbitmapSettingGear = createBitmapFromRes(R.drawable.setting_gear_64, this, true);
	           	((OGLChart)mmfpChart).mbitmapZoom1 = createBitmapFromRes(R.drawable.zoom_1_64, this, true);
	        	((OGLChart)mmfpChart).mbitmapZoomFit = createBitmapFromRes(R.drawable.zoom_fit_64, this, true);
	           	((OGLChart)mmfpChart).mbitmapZoomIn = createBitmapFromRes(R.drawable.zoom_in_64, this, true);
	        	((OGLChart)mmfpChart).mbitmapZoomOut = createBitmapFromRes(R.drawable.zoom_out_64, this, true);
	        	if (mlPlottingTime != 0L)	{
	        		((OGLChart)mmfpChart).mbTakeSnapshot = true;
	        	}
	        	
	            // requesting to turn the title OFF
	            requestWindowFeature(Window.FEATURE_NO_TITLE);
	            // making it full screen
	    		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	    				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	
	    		moglChartView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
	    		setContentView(moglChartView);
	        }
	    }
    }
    
    public static Bitmap createBitmapFromRes( int nResID, Context context, boolean bScaleToPO2 ) {

        // pull in the resource
        Bitmap bitmap = null;

        Drawable image = context.getResources().getDrawable( nResID );
        float density = context.getResources().getDisplayMetrics().density;

        int originalWidth = (int)(image.getIntrinsicWidth() / density);
        int originalHeight = (int)(image.getIntrinsicHeight() / density);

        int powWidth = OGLChart.getNextHighestPO2( originalWidth );
        int powHeight = OGLChart.getNextHighestPO2( originalHeight );

        if ( bScaleToPO2 ) {
            image.setBounds( 0, 0, powWidth, powHeight );
        } else {
            image.setBounds( 0, 0, originalWidth, originalHeight );
        }

        // Create an empty, mutable bitmap
        bitmap = Bitmap.createBitmap( powWidth, powHeight, Bitmap.Config.ARGB_8888 );
        // get a canvas to paint over the bitmap
        Canvas canvas = new Canvas( bitmap );
        bitmap.eraseColor(0);

        image.draw( canvas ); // draw the image onto our bitmap
        
        return bitmap;
    }
    
    public void showErrorMsg(String strMsg, final boolean bExitActivity)	{
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(ActivityChartDemon.this)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							if (bExitActivity)	{
								finish();	// exit
							}
						}
						
					}).setTitle(getString(R.string.error))
					.setMessage(strMsg)
					.create();
		alertDialog.show();
	}
    
    public void startCfg2DExprChart()	{
    	if (((XYExprChart)mmfpChart).mbCfgWindowStarted == false)	{
			((XYExprChart)mmfpChart).mbCfgWindowStarted = true;
	   		Intent intentConfig2DExprGraph = new Intent(this, ActivityConfig2DExprGraph.class);
	   		intentConfig2DExprGraph.putExtra("XAxisName", ((XYExprChart)mmfpChart).mstrXAxisName);
	   		intentConfig2DExprGraph.putExtra("YAxisName", ((XYExprChart)mmfpChart).mstrYAxisName);
	   		intentConfig2DExprGraph.putExtra("AdjustParams", ((XYExprChart)mmfpChart).getChartDataParams());
			startActivityForResult(intentConfig2DExprGraph, CONFIG_XYEXPRCHART_ACTIVITY);
    	}
    }
	
    public void startCfgPolarExprChart()	{
    	if (((PolarExprChart)mmfpChart).mbCfgWindowStarted == false)	{
			((PolarExprChart)mmfpChart).mbCfgWindowStarted = true;
	   		Intent intentConfigPolarExprGraph = new Intent(this, ActivityConfigPolarExprGraph.class);
	   		intentConfigPolarExprGraph.putExtra("XAxisName", ((PolarExprChart)mmfpChart).mstrXAxisName);
	   		intentConfigPolarExprGraph.putExtra("YAxisName", ((PolarExprChart)mmfpChart).mstrYAxisName);
	   		intentConfigPolarExprGraph.putExtra("AdjustParams", ((PolarExprChart)mmfpChart).getChartDataParams());
			startActivityForResult(intentConfigPolarExprGraph, CONFIG_POLAREXPRCHART_ACTIVITY);
    	}
    }
	
    public void startCfgOGLChart()	{
    	if (((OGLChart)mmfpChart).mbCfgWindowStarted == false)	{
			((OGLChart)mmfpChart).mbCfgWindowStarted = true;
    		Intent intentConfigXYZGraph = new Intent(this, ActivityConfigXYZGraph.class);
    		intentConfigXYZGraph.putExtra("NotShowAxisTitle", ((OGLChart)mmfpChart).getNotDrawAxisAndTitle());
    		startActivityForResult(intentConfigXYZGraph, CONFIG_OGLCHART_ACTIVITY);
    	}
    }
	
    public void startCfgOGLExprChart()	{
    	if (((OGLExprChart)mmfpChart).mbCfgWindowStarted == false)	{
			((OGLExprChart)mmfpChart).mbCfgWindowStarted = true;
	   		Intent intentConfig3DExprGraph = new Intent(this, ActivityConfig3DExprGraph.class);
	   		intentConfig3DExprGraph.putExtra("XAxisName", ((OGLExprChart)mmfpChart).mstrXAxisName);
	   		intentConfig3DExprGraph.putExtra("YAxisName", ((OGLExprChart)mmfpChart).mstrYAxisName);
	   		intentConfig3DExprGraph.putExtra("ZAxisName", ((OGLExprChart)mmfpChart).mstrZAxisName);
	   		intentConfig3DExprGraph.putExtra("AdjustParams", ((OGLExprChart)mmfpChart).getChartDataParams());
			startActivityForResult(intentConfig3DExprGraph, CONFIG_OGLEXPRCHART_ACTIVITY);
    	}
    }
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
		case (CONFIG_XYEXPRCHART_ACTIVITY) : { 
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getExtras();
				ActivityConfig2DExprGraph.AdjXYExprChartParams adjParams = null;
				if (bundle != null
						&& (adjParams = (ActivityConfig2DExprGraph.AdjXYExprChartParams)bundle.getSerializable("AdjustParams")) != null) {
					/* we just come back from historical results */
					((XYExprChart)mmfpChart).applyCfgChart(adjParams);
					mflatChartView.repaint();	// has to repaint.
				}
			} 
			((XYExprChart)mmfpChart).mbCfgWindowStarted = false;
			break; 
		} case (CONFIG_POLAREXPRCHART_ACTIVITY) : { 
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getExtras();
				ActivityConfigPolarExprGraph.AdjXYExprChartParams adjParams = null;
				if (bundle != null
						&& (adjParams = (ActivityConfigPolarExprGraph.AdjXYExprChartParams)bundle.getSerializable("AdjustParams")) != null) {
					/* we just come back from historical results */
					((PolarExprChart)mmfpChart).applyCfgChart(adjParams);
					mflatChartView.repaint();	// has to repaint.
				}
			} 
			((PolarExprChart)mmfpChart).mbCfgWindowStarted = false;
			break; 
		} case (CONFIG_OGLCHART_ACTIVITY) : { 
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getExtras();
				ActivityConfigXYZGraph.AdjOGLChartParams adjParams = null;
				if (bundle != null
						&& (adjParams = (ActivityConfigXYZGraph.AdjOGLChartParams)bundle.getSerializable("AdjustParams")) != null) {
					/* we just come back from historical results */
					((OGLChart)mmfpChart).applyCfgChart(adjParams);
				}
			} 
			((OGLChart)mmfpChart).mbCfgWindowStarted = false;
			break; 
		} case (CONFIG_OGLEXPRCHART_ACTIVITY) :	{
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getExtras();
				ActivityConfig3DExprGraph.AdjOGLExprChartParams adjParams = null;
				if (bundle != null
						&& (adjParams = (ActivityConfig3DExprGraph.AdjOGLExprChartParams)bundle.getSerializable("AdjustParams")) != null) {
					/* we just come back from historical results */
					((OGLExprChart)mmfpChart).applyCfgChart(adjParams);
				}
			} 
			((OGLExprChart)mmfpChart).mbCfgWindowStarted = false;
			break; 
		}
		}
	}

    @Override
    public void finish()	{
		Intent intent = new Intent();
    	if (mlPlottingTime != 0)	{
    		Bitmap bmp = null;
	    	if (mflatChartView != null)	{
	    		mflatChartView.setDrawingCacheEnabled(true);
	    		bmp = Bitmap.createBitmap(mflatChartView.getDrawingCache());
	    		mflatChartView.setDrawingCacheEnabled(false);
	    	} else if (moglChartView != null)	{
	    		if ((bmp = moglChartView.getSnapshot()) == null)	{	// may crash when take snapshot.
	    			moglChartView.setDrawingCacheEnabled(true);
	    			bmp = Bitmap.createBitmap(moglChartView.getDrawingCache());
	    			moglChartView.setDrawingCacheEnabled(false);
	    		}
    		}
	    	if (bmp != null)	{
	    		String strSnapshotFileNameNoExt = getChartFileName(
	    										ChartOperator.addEscapes(getString(R.string.chart_name_default)).trim(),
	    										mlPlottingTime);
	    		String strSnapshotFileName = strSnapshotFileNameNoExt + ".jpg";
		        String strRootPath = AndroidStorageOptions.getSelectedStoragePath()
		        					+ MFPFileManagerActivity.STRING_PATH_DIV
		        					+ MFPFileManagerActivity.STRING_APP_FOLDER;
				File folder = new File(strRootPath + MFPFileManagerActivity.STRING_PATH_DIV
										+ MFPFileManagerActivity.STRING_CHART_SNAPSHOT_FOLDER
										+ MFPFileManagerActivity.STRING_PATH_DIV);
				String strSnapshotFilePath = strRootPath
						+ MFPFileManagerActivity.STRING_PATH_DIV
						+ MFPFileManagerActivity.STRING_CHART_SNAPSHOT_FOLDER
						+ MFPFileManagerActivity.STRING_PATH_DIV
						+ strSnapshotFileName;
				File file = new File(strSnapshotFilePath);
				folder.mkdirs();
	    		try {
					bmp.compress(CompressFormat.JPEG, 95, new FileOutputStream(file));
					if (intent != null) {
						Bundle b = new Bundle();
						if (b != null) {
							b.putInt("ChartWidth", bmp.getWidth());
							b.putInt("ChartHeight", bmp.getHeight());
							b.putLong("PlotTimeStamp", mlPlottingTime);
							intent.putExtra("android.intent.extra.FunCalc", b);
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
    	}
		setResult(Activity.RESULT_OK, intent);
    	super.finish();
    }
    
    public static String getChartFileName(String strChartNameByDefault, long lPlottingTime)	{
    	Calendar cal = Calendar.getInstance();
    	cal.setTimeInMillis(lPlottingTime);
    	String strYear = String.valueOf(cal.get(Calendar.YEAR));
    	String strMonth = ((cal.get(Calendar.MONTH) + 1) < 10)?("0" + String.valueOf(cal.get(Calendar.MONTH) + 1)):(String.valueOf(cal.get(Calendar.MONTH) + 1));
    	String strDate = (cal.get(Calendar.DATE) < 10)?("0" + String.valueOf(cal.get(Calendar.DATE))):(String.valueOf(cal.get(Calendar.DATE)));
    	String strHour = (cal.get(Calendar.HOUR_OF_DAY) < 10)?("0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY))):(String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
    	String strMinute = (cal.get(Calendar.MINUTE) < 10)?("0" + String.valueOf(cal.get(Calendar.MINUTE))):(String.valueOf(cal.get(Calendar.MINUTE)));
    	String strSecond = (cal.get(Calendar.SECOND) < 10)?("0" + String.valueOf(cal.get(Calendar.SECOND))):(String.valueOf(cal.get(Calendar.SECOND)));
    	String strTimeStamp = String.valueOf(lPlottingTime);	// timezone calculation is very complicated. And we have to consider special characters like / .so do not use it.
    	String strFileName = strChartNameByDefault
    					+ ((lPlottingTime != 0L)?("_" + strYear + strMonth + strDate + "_"
    							+ strHour + strMinute + strSecond + "_" + strTimeStamp):"");
    	return strFileName;
    }
    
    public static long getChartTimestamp(String strFileName)	{	// strFileName does not include .jpg or .mfpc
    	String strarrayTemps[] = strFileName.split("_");
    	if (strarrayTemps.length < 4)	{
    		return 0L;
    	} else	{
    		long lPlottingTime = 0L;
    		try	{
    			lPlottingTime  = Long.parseLong(strarrayTemps[3]);
    		} catch(NumberFormatException e)	{
    			// cannot parse the string, lPlottingTime by default is 0.
			}
    		return lPlottingTime;
    	}
	}
    
	@Override
	protected void onSaveInstanceState(Bundle outState)	{
		outState.putLong(PLOTTING_TS, mlPlottingTime);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle inState) {
		//Restore last state
		super.onRestoreInstanceState(inState);
		
		mlPlottingTime = inState.getLong(PLOTTING_TS);
		if  (moglChartView != null)	{
			if (mlPlottingTime != 0L)	{
				((OGLChart)mmfpChart).mbTakeSnapshot = true;
			} else	{
				((OGLChart)mmfpChart).mbTakeSnapshot = false;
			}
		}
	}
}