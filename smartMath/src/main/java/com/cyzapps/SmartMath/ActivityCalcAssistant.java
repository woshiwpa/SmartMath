package com.cyzapps.SmartMath;

import java.math.BigDecimal;

import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.adapter.MFPAdapter;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.*;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
 
public class ActivityCalcAssistant extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.calc_assistant);
 
        //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();

        MFPNumeric mfpNumInitInput2UnitCvt = MFPNumeric.MINUS_ONE;	// means invalid initial input.
        if (bundle != null)	{
        	//Next extract the values using the key as
        	String strLastAnswer = bundle.getString("Last_Answer");
        	String strThisInput = bundle.getString("This_Input");
        	try	{
        		mfpNumInitInput2UnitCvt = new MFPNumeric(strLastAnswer);
    			if (mfpNumInitInput2UnitCvt.isActuallyNegative())	{
    				mfpNumInitInput2UnitCvt = mfpNumInitInput2UnitCvt.negate();
    			}
        	} catch(Exception e)	{	// can be either a null pointer exception or number format exception.
        		try {
        			mfpNumInitInput2UnitCvt = new MFPNumeric(strThisInput);
        			if (mfpNumInitInput2UnitCvt.isActuallyNegative())	{
        				mfpNumInitInput2UnitCvt = mfpNumInitInput2UnitCvt.negate();
        			}
            	} catch(Exception e1)	{	// can be either a null pointer exception or number format exception.
            		mfpNumInitInput2UnitCvt = MFPNumeric.MINUS_ONE;
        		}
        	}
        }
        
        TabHost tabHost = getTabHost();
        
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View viewReadConstIndicator = inflater.inflate(R.layout.tab_indicator_view, null);
        final TextView tvReadConstText = (TextView)(viewReadConstIndicator.findViewById(R.id.textViewText));
        tvReadConstText.setTextColor(Color.MAGENTA);	// by default, it is selected, so the color is Magenta
        tvReadConstText.setText(Html.fromHtml("<large><b><u>" + getString(R.string.read_constants) + "</b></u></large>"));
        //viewReadConstIndicator.setBackgroundColor(Color.BLACK);
        final View viewCvtUnitsIndicator = inflater.inflate(R.layout.tab_indicator_view, null);
        final TextView tvCvtUnitsText = (TextView)(viewCvtUnitsIndicator.findViewById(R.id.textViewText));
        tvCvtUnitsText.setTextColor(Color.CYAN);
        tvCvtUnitsText.setText(Html.fromHtml("<small>" + getString(R.string.convert_units) + "</small>"));
        //viewCvtUnitsIndicator.setBackgroundColor(Color.DKGRAY);

        // Tab for constants
        TabSpec tspecReadConst = tabHost.newTabSpec("Constant values");
        // setting Title and Icon for the Tab
        tspecReadConst.setIndicator(viewReadConstIndicator);
        Intent intentReadConst = new Intent(this, ActivityReadConstants.class);
        tspecReadConst.setContent(intentReadConst);
 
        // Tab for units
        TabSpec tspecCvtUnits = tabHost.newTabSpec("Convert units");
        // setting Title and Icon for the Tab
        tspecCvtUnits.setIndicator(viewCvtUnitsIndicator);
        Intent intentCvtUnit = new Intent(this, ActivityCvtUnit.class);
        if (mfpNumInitInput2UnitCvt.isActuallyNonNegative())	{
		   	Bundle bundleCvtUnit = new Bundle();
		   	String strInitInput2CvtUnit = "";
		   	try	{
		   		strInitInput2CvtUnit = MFPAdapter.outputDatum(new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumInitInput2UnitCvt))[0];
		   	} catch (Exception e)	{
		   	}
		   	bundleCvtUnit.putString("Initial_Input", strInitInput2CvtUnit);
		   	intentCvtUnit.putExtras(bundleCvtUnit);
        }
        tspecCvtUnits.setContent(intentCvtUnit);
 
        // Adding all TabSpec to TabHost
        tabHost.addTab(tspecReadConst); // Adding constant tab
        tabHost.addTab(tspecCvtUnits); // Adding unit tab
        tabHost.setCurrentTab(0); 
        
        tabHost.setOnTabChangedListener(new OnTabChangeListener()	{

			@Override
			public void onTabChanged(String strTabId) {
				// TODO Auto-generated method stub
				if (strTabId.compareToIgnoreCase("Constant values") == 0)	{
					//viewReadConstIndicator.setBackgroundColor(Color.BLACK);
			        tvReadConstText.setText(Html.fromHtml("<large><b><u>" + getString(R.string.read_constants) + "</b></u></large>"));
			        tvReadConstText.setTextColor(Color.MAGENTA);
					//viewCvtUnitsIndicator.setBackgroundColor(Color.DKGRAY);
			        tvCvtUnitsText.setText(Html.fromHtml("<small>" + getString(R.string.convert_units) + "</small>"));
			        tvCvtUnitsText.setTextColor(Color.CYAN);
				} else if (strTabId.compareToIgnoreCase("Convert units") == 0)	{
					//viewReadConstIndicator.setBackgroundColor(Color.DKGRAY);
			        tvReadConstText.setText(Html.fromHtml("<small>" + getString(R.string.read_constants) + "</small>"));
			        tvReadConstText.setTextColor(Color.CYAN);
					//viewCvtUnitsIndicator.setBackgroundColor(Color.BLACK);
			        tvCvtUnitsText.setText(Html.fromHtml("<large><b><u>" + getString(R.string.convert_units) + "</b></u></large>"));
			        tvCvtUnitsText.setTextColor(Color.MAGENTA);
				} else	{
					//viewReadConstIndicator.setBackgroundColor(Color.DKGRAY);
			        tvReadConstText.setText(Html.fromHtml("<small>" + getString(R.string.read_constants) + "</small>"));
			        tvReadConstText.setTextColor(Color.CYAN);
					//viewCvtUnitsIndicator.setBackgroundColor(Color.DKGRAY);
			        tvCvtUnitsText.setText(Html.fromHtml("<small>" + getString(R.string.convert_units) + "</small>"));
			        tvCvtUnitsText.setTextColor(Color.CYAN);
				}
			}
        	
        });

    }
}