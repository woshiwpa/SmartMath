package com.cyzapps.SmartMath;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ActivityPlotPolarGraph extends ActivityPlotXYGraph	{
	
	public String mstrXTitle = "r";
	public String mstrYTitle = "\u03b8";

	public static final String PLOT_FUNCTION_NAME = "plot_polar_curves";	// variable cannot be overridden, has to use function.
	public String getPlotFunctionName()	{return PLOT_FUNCTION_NAME;}
	public static final String CURVE_XT_PROMPT = "r(t) = ";
	public String getCurveXtPrompt()	{return CURVE_XT_PROMPT;}
	public static final String CURVE_YT_PROMPT = "\u03B8(t) = ";
	public String getCurveYtPrompt()	{return CURVE_YT_PROMPT;}

	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
 		setTitle(getString(R.string.app_name) + ": " + getString(R.string.plot_polar_title));
 		TextView tvXTitle = (TextView)findViewById(R.id.textview_Xtitle_prompt);
 		tvXTitle.setText(getString(R.string.graph_Rtitle_prompt));
 		TextView tvYTitle = (TextView)findViewById(R.id.textview_Ytitle_prompt);
 		tvYTitle.setText(getString(R.string.graph_Angletitle_prompt));
        EditText edtXTitle = (EditText)findViewById(R.id.graph_Xtitle_edit);
        edtXTitle.setText(getString(R.string.graph_Rtitle_hint));
        EditText edtYTitle = (EditText)findViewById(R.id.graph_Ytitle_edit);
        edtYTitle.setText(getString(R.string.graph_Angletitle_hint));
 		LinearLayout layoutYTitleInput = (LinearLayout)findViewById(R.id.Y_title_input);
 		layoutYTitleInput.setVisibility(LinearLayout.GONE);
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemID = item.getItemId();
		boolean bReturnVal = super.onOptionsItemSelected(item);
		switch (itemID) {
		case Menu.FIRST:
			// initial a one-curve example
	        EditText edtXTitle = (EditText)findViewById(R.id.graph_Xtitle_edit);
	        edtXTitle.setText(getString(R.string.graph_Rtitle_hint));
	        EditText edtYTitle = (EditText)findViewById(R.id.graph_Ytitle_edit);
	        edtYTitle.setText(getString(R.string.graph_Angletitle_hint));
			break;
		}
		return bReturnVal;
	}
	
	// use super class's onConfigurationChange	
	public void setExample()	{
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nNumofCurves = 1;
		if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
				|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
			nNumofCurves = 2;
		} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
			nNumofCurves = 2;
		} else	{	// ex-large
			nNumofCurves = 3;
		}
		if (nNumofCurves <= 0)	{
			nNumofCurves = 0;
		} else if (nNumofCurves >= 3)	{
			nNumofCurves = 3;
		}
		nNumofCurves = 3;
		mlistCurveSettings = new CurveSettings[nNumofCurves];
		switch (nNumofCurves)	{
		case 3:
	        mlistCurveSettings[2] = new CurveSettings();
			mlistCurveSettings[2].mstrCurveTitle = getString(R.string.curve_title_hint2);
			mlistCurveSettings[2].mstrCurveColor = "magenta";
			mlistCurveSettings[2].mstrCurvePntColor = "magenta";
			mlistCurveSettings[2].mstrCurvePntStyle = "point";
			mlistCurveSettings[2].mnCurvePntSize = 1;
			mlistCurveSettings[2].mstrCurveLnColor = "green";
			mlistCurveSettings[2].mstrCurveLnStyle = "solid";
			mlistCurveSettings[2].mnCurveLnSize = 1;
			mlistCurveSettings[2].mstrTFrom = "-1.5 * pi";
			mlistCurveSettings[2].mstrTTo = "1.5 * pi";
			mlistCurveSettings[2].mstrTStep = "0.0";
			mlistCurveSettings[2].mstrXExpr = "t";
			mlistCurveSettings[2].mstrYExpr = "t";
		case 2:
	        mlistCurveSettings[1] = new CurveSettings();
			mlistCurveSettings[1].mstrCurveTitle = getString(R.string.curve_title_hint);
			mlistCurveSettings[1].mstrCurveColor = "blue";
			mlistCurveSettings[1].mstrCurvePntColor = "blue";
			mlistCurveSettings[1].mstrCurvePntStyle = "point";
			mlistCurveSettings[1].mnCurvePntSize = 1;
			mlistCurveSettings[1].mstrCurveLnColor = "blue";
			mlistCurveSettings[1].mstrCurveLnStyle = "solid";
			mlistCurveSettings[1].mnCurveLnSize = 1;
			mlistCurveSettings[1].mstrTFrom = "2 * pi";
			mlistCurveSettings[1].mstrTTo = "-2 * pi";
			mlistCurveSettings[1].mstrTStep = "";	// auto step.
			mlistCurveSettings[1].mstrXExpr = "2 * sin (4 * t)";
			mlistCurveSettings[1].mstrYExpr = "t";
		case 1:
	        mlistCurveSettings[0] = new CurveSettings();
			mlistCurveSettings[0].mstrCurveTitle = getString(R.string.curve_title_hint1);
			mlistCurveSettings[0].mstrCurveColor = "green";
			mlistCurveSettings[0].mstrCurvePntColor = "green";
			mlistCurveSettings[0].mstrCurvePntStyle = "point";
			mlistCurveSettings[0].mnCurvePntSize = 1;
			mlistCurveSettings[0].mstrCurveLnColor = "red";
			mlistCurveSettings[0].mstrCurveLnStyle = "solid";
			mlistCurveSettings[0].mnCurveLnSize = 1;
			mlistCurveSettings[0].mstrTFrom = "0";
			mlistCurveSettings[0].mstrTTo = "PI * 2";
			mlistCurveSettings[0].mstrTStep = "   ";	//0.2;
			mlistCurveSettings[0].mstrXExpr = "cos(t)";	//"10/(t**2 - 10 * t + 10)";
			mlistCurveSettings[0].mstrYExpr = "t";
		default:	// 0
		}
		refreshCurveDefViewList();
	}
}
