package com.cyzapps.GraphDemon;

import java.io.Serializable;

import com.cyzapps.SmartMath.R;
import com.cyzapps.GraphDemon.ActivityConfigXYZGraph.AdjOGLChartParams;
import com.cyzapps.PlotAdapter.OGLExprChartOperator;
import com.cyzapps.PlotAdapter.XYExprChartOperator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityConfigPolarExprGraph extends ActivityConfig2DExprGraph {

    public static final double XFROM_INITIAL_VALUE = 0;	// variable cannot be overridden, has to use function.
    public static final double XTO_INITIAL_VALUE = 10.0;
    public static final double YFROM_INITIAL_VALUE = -180;
    public static final double YTO_INITIAL_VALUE = 180;
	
    public static final String XAXIS_DEFAULT_NAME = "r";
    public static final String YAXIS_DEFAULT_NAME = "\u03b8";	// theta
    
    public String getXAxisDefaultName()	{return XAXIS_DEFAULT_NAME;}
    public String getYAxisDefaultName()	{return YAXIS_DEFAULT_NAME;}
    public double getXFromInitialValue()	{return XFROM_INITIAL_VALUE;}
    public double getXToInitialValue()	{return XTO_INITIAL_VALUE;}
    public double getYFromInitialValue()	{return YFROM_INITIAL_VALUE;}
    public double getYToInitialValue()	{return YTO_INITIAL_VALUE;}

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle("====== " + getString(R.string.config_polar_title) + " ======");	// title should not be too short, otherwise, dialog will be too narrow.
        EditText edtXFrom = (EditText)findViewById(R.id.x_from_edit);
        edtXFrom.setText("0");
        edtXFrom.setEnabled(false);
        EditText edtXTo = (EditText)findViewById(R.id.x_to_edit);
        edtXTo.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        TextView tvXInputNote = (TextView)findViewById(R.id.x_input_note);
        tvXInputNote.setText(getString(R.string.polar_chart_r_range_note));
        TextView tvYInputNote = (TextView)findViewById(R.id.y_input_note);
        tvYInputNote.setText(getString(R.string.polar_chart_angle_range_note) + " " + getString(R.string.degree) + ".");
	}
	
}
