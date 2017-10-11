package com.cyzapps.GraphDemon;

import java.io.Serializable;

import com.cyzapps.SmartMath.R;
import com.cyzapps.GraphDemon.ActivityConfigXYZGraph.AdjOGLChartParams;
import com.cyzapps.PlotAdapter.OGLExprChartOperator;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ActivityConfig3DExprGraph extends Activity {

	public static class AdjOGLExprChartParams implements Serializable	{
		public double mdXFrom = -5;
		public double mdXTo = 5;
		public int mnXNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		public double mdYFrom = -5;
		public double mdYTo = 5;
		public int mnYNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		public double mdZFrom = -5;
		public double mdZTo = 5;
		public int mnZNumOfSteps = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS;
		
		public boolean mbNotShowAxisAndTitle = false;		
		public boolean isNoAdj(AdjOGLExprChartParams adjOriginal)	{
			return (mdXFrom == adjOriginal.mdXFrom) && (mdXTo == adjOriginal.mdXTo) && (mnXNumOfSteps == adjOriginal.mnXNumOfSteps)
					&& (mdYFrom == adjOriginal.mdYFrom) && (mdYTo == adjOriginal.mdYTo) && (mnYNumOfSteps == adjOriginal.mnYNumOfSteps)
					&& (mdZFrom == adjOriginal.mdZFrom) && (mdZTo == adjOriginal.mdZTo) && (mnZNumOfSteps == adjOriginal.mnZNumOfSteps)
					&& (mbNotShowAxisAndTitle == adjOriginal.mbNotShowAxisAndTitle);
		}

		public boolean isNoAdj(double dOriginalXFrom, double dOriginalXTo, int nOriginalXNumOfSteps,
				double dOriginalYFrom, double dOriginalYTo, int nOriginalYNumOfSteps,
				double dOriginalZFrom, double dOriginalZTo, int nOriginalZNumOfSteps,
				boolean bNotShowAxisAndTitle)	{
			return (mdXFrom == dOriginalXFrom) && (mdXTo == dOriginalXTo) && (mnXNumOfSteps == nOriginalXNumOfSteps)
					&& (mdYFrom == dOriginalYFrom) && (mdYTo == dOriginalYTo) && (mnYNumOfSteps == nOriginalYNumOfSteps)
					&& (mdZFrom == dOriginalZFrom) && (mdZTo == dOriginalZTo) && (mnZNumOfSteps == nOriginalZNumOfSteps)
					&& (mbNotShowAxisAndTitle == bNotShowAxisAndTitle);
		}

	}
	private EditText medtXFrom, medtXTo, medtXNumOfSteps, medtYFrom, medtYTo, medtYNumOfSteps, medtZFrom, medtZTo, medtZNumOfSteps;
	private CheckBox mchkBoxNotShowAxisTitle;
	
    public static final int NORMAL_BKGRND_COLOR = Color.WHITE;
    public static final int ERROR_BKGRND_COLOR = Color.YELLOW;

    public static final int MAX_NUM_OF_STEPS = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS * 5;
    public static final int MIN_NUM_OF_STEPS = OGLExprChartOperator.DEFAULT_NUM_OF_STEPS / 5;
    
    public static final double XFROM_INITIAL_VALUE = -5.0;
    public static final double XTO_INITIAL_VALUE = 5.0;
    public static final int XNUMOFSTEPS_INITIAL_VALUE = 10;
    public static final double YFROM_INITIAL_VALUE = -5.0;
    public static final double YTO_INITIAL_VALUE = 5.0;
    public static final int YNUMOFSTEPS_INITIAL_VALUE = 10;
    public static final double ZFROM_INITIAL_VALUE = -5.0;
    public static final double ZTO_INITIAL_VALUE = 5.0;
    public static final int ZNUMOFSTEPS_INITIAL_VALUE = 10;
    public static final boolean NOTSHOWAXISTITLE_INITIAL_VALUE = false;
	
	private double mdXFrom = XFROM_INITIAL_VALUE;
	private double mdXTo = XTO_INITIAL_VALUE;
	private int mnXNumOfSteps = XNUMOFSTEPS_INITIAL_VALUE;
	private double mdYFrom = YFROM_INITIAL_VALUE;
	private double mdYTo = YTO_INITIAL_VALUE;
	private int mnYNumOfSteps = YNUMOFSTEPS_INITIAL_VALUE;
	private double mdZFrom = ZFROM_INITIAL_VALUE;
	private double mdZTo = ZTO_INITIAL_VALUE;
	private int mnZNumOfSteps = ZNUMOFSTEPS_INITIAL_VALUE;
    private boolean mbNotShowAxisAndTitle = NOTSHOWAXISTITLE_INITIAL_VALUE;
	
	private String mstrLastChangedParam = "";
	private boolean mbValidXFrom = true;
	private boolean mbValidXTo = true;
	private boolean mbValidXNumOfSteps = true;
	private boolean mbValidYFrom = true;
	private boolean mbValidYTo = true;
	private boolean mbValidYNumOfSteps = true;
	private boolean mbValidZFrom = true;
	private boolean mbValidZTo = true;
	private boolean mbValidZNumOfSteps = true;

    public static Double validateToFromTextInput(EditText edtFrom, EditText edtTo, Double dDefaultFrom, Double dDefaultTo, boolean bReturnFrom)    {
        String strFrom = edtFrom.getText().toString();
        String strTo = edtTo.getText().toString();
        double dValueFrom = dDefaultFrom, dValueTo = dDefaultTo;
        Boolean bInputRight = true;
        try {
            dValueFrom = Double.parseDouble(strFrom);
            dValueTo = Double.parseDouble(strTo);
        } catch (NumberFormatException e)  {
            bInputRight = false;
        }
        if (bInputRight && dValueFrom >= dValueTo)  {
            bInputRight = false;
        }
        if (!bInputRight)    {
        	edtFrom.setBackgroundColor(ERROR_BKGRND_COLOR);
        	edtTo.setBackgroundColor(ERROR_BKGRND_COLOR);
        } else  {
        	edtFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
        	edtTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
        }
        if (bInputRight)    {
            return bReturnFrom?dValueFrom:dValueTo;
        } else  {
            return null;
        }
    }
    
    public static Integer validateInclusiveIntRange(EditText edtInput, Integer nDefault, Integer nMin, Integer nMax)    {
        String strText = edtInput.getText().toString();
        int nValue = nDefault;
        Boolean bInputRight = true;
        try {
            nValue = Integer.parseInt(strText);
        } catch (NumberFormatException e)  {
            bInputRight = false;
        }
        if (nMin != null && nValue < nMin)  {
            bInputRight = false;
        } else if (nMax != null && nValue > nMax)  {
            bInputRight = false;
        }
        if (!bInputRight)    {
        	edtInput.setBackgroundColor(ERROR_BKGRND_COLOR);
        } else  {
        	edtInput.setBackgroundColor(NORMAL_BKGRND_COLOR);
        }
        if (bInputRight)    {
            return nValue;
        } else  {
            return null;
        }
    }
    
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle("====== " + getString(R.string.config_3D_title) + " ======");	// title should not be too short, otherwise, dialog will be too narrow.
        setContentView(R.layout.config_3dexprgraph);

        String strXAxisName = null, strYAxisName = null, strZAxisName = null;
        AdjOGLExprChartParams adjParams = null;
        //First Extract the bundle from intent
        Intent intent = getIntent();
        if (intent != null)	{
        	//Next extract the values using the key as
        	strXAxisName = intent.getStringExtra("XAxisName");
        	strYAxisName = intent.getStringExtra("YAxisName");
         	strZAxisName = intent.getStringExtra("ZAxisName");
        	adjParams = (AdjOGLExprChartParams) intent.getSerializableExtra("AdjustParams");
        }

        if (strXAxisName == null)	{
    		strXAxisName = "x";
    	}
    	if (strYAxisName == null)	{
    		strYAxisName = "y";
    	}
    	if (strZAxisName == null)	{
    		strZAxisName = "z";
    	}
    	if (adjParams == null)	{
    		adjParams = new AdjOGLExprChartParams();
    	}
    	
     	TextView txtXFrom = (TextView) findViewById(R.id.x_from_prompt);
    	TextView txtYFrom = (TextView) findViewById(R.id.y_from_prompt);
    	TextView txtZFrom = (TextView) findViewById(R.id.z_from_prompt);
		txtXFrom.setText(strXAxisName + ": " + txtXFrom.getText());
		txtYFrom.setText(strYAxisName + ": " + txtYFrom.getText());
		txtZFrom.setText(strZAxisName + ": " + txtZFrom.getText());
       
    	TextView txtXNumOfSteps = (TextView) findViewById(R.id.x_number_of_steps_prompt);
    	TextView txtYNumOfSteps = (TextView) findViewById(R.id.y_number_of_steps_prompt);
    	TextView txtZNumOfSteps = (TextView) findViewById(R.id.z_number_of_steps_prompt);
    	txtXNumOfSteps.setText(txtXNumOfSteps.getText() + " (" + MIN_NUM_OF_STEPS + "-" +  MAX_NUM_OF_STEPS + ")");
    	txtYNumOfSteps.setText(txtYNumOfSteps.getText() + " (" + MIN_NUM_OF_STEPS + "-" +  MAX_NUM_OF_STEPS + ")");
    	txtZNumOfSteps.setText(txtZNumOfSteps.getText() + " (" + MIN_NUM_OF_STEPS + "-" +  MAX_NUM_OF_STEPS + ")");
    	
		medtXFrom = (EditText) findViewById(R.id.x_from_edit);
		medtXTo = (EditText) findViewById(R.id.x_to_edit);
		medtXNumOfSteps = (EditText) findViewById(R.id.x_number_of_steps_edit);
		medtYFrom = (EditText) findViewById(R.id.y_from_edit);
		medtYTo = (EditText) findViewById(R.id.y_to_edit);
		medtYNumOfSteps = (EditText) findViewById(R.id.y_number_of_steps_edit);
		medtZFrom = (EditText) findViewById(R.id.z_from_edit);
		medtZTo = (EditText) findViewById(R.id.z_to_edit);
		medtZNumOfSteps = (EditText) findViewById(R.id.z_number_of_steps_edit);

		mchkBoxNotShowAxisTitle = (CheckBox) findViewById(R.id.chkBoxNotShowAxisTitle);
		
		medtXFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtXTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtXNumOfSteps.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtYFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtYTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtYNumOfSteps.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtZFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtZTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtZNumOfSteps.setBackgroundColor(NORMAL_BKGRND_COLOR);
		
		medtXFrom.setText(String.valueOf(adjParams.mdXFrom));
		medtXTo.setText(String.valueOf(adjParams.mdXTo));
		medtXNumOfSteps.setText(String.valueOf(adjParams.mnXNumOfSteps));
		medtYFrom.setText(String.valueOf(adjParams.mdYFrom));
		medtYTo.setText(String.valueOf(adjParams.mdYTo));
		medtYNumOfSteps.setText(String.valueOf(adjParams.mnYNumOfSteps));
		medtZFrom.setText(String.valueOf(adjParams.mdZFrom));
		medtZTo.setText(String.valueOf(adjParams.mdZTo));
		medtZNumOfSteps.setText(String.valueOf(adjParams.mnZNumOfSteps));
		
		mchkBoxNotShowAxisTitle.setChecked(adjParams.mbNotShowAxisAndTitle);
		
		mdXFrom = adjParams.mdXFrom;
		mdXTo = adjParams.mdXTo;
		mnXNumOfSteps = adjParams.mnXNumOfSteps;
		mdYFrom = adjParams.mdYFrom;
		mdYTo = adjParams.mdYTo;
		mnYNumOfSteps = adjParams.mnYNumOfSteps;
		mdZFrom = adjParams.mdZFrom;
		mdZTo = adjParams.mdZTo;
		mnZNumOfSteps = adjParams.mnZNumOfSteps;

		medtXFrom.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateToFromTextInput(medtXFrom, medtXTo, XFROM_INITIAL_VALUE, XTO_INITIAL_VALUE, true); 
					if (dInput != null) {
						mdXFrom = dInput;
				        try {
				            mdXTo = Double.parseDouble(medtXTo.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidXFrom = true;
			            mbValidXTo = true;
			        } else	{
			        	mbValidXFrom = false;
			        	mbValidXTo = false;
					}
				}
			}
			
		});
		
		medtXFrom.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "XFrom";
			}
			
		});
		
		medtXTo.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateToFromTextInput(medtXFrom, medtXTo, XFROM_INITIAL_VALUE, XTO_INITIAL_VALUE, false); 
					if (dInput != null) {
						mdXTo = dInput;
				        try {
				            mdXFrom = Double.parseDouble(medtXFrom.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidXTo = true;
			            mbValidXFrom = true;
			        } else	{
			        	mbValidXTo = false;
			        	mbValidXFrom = false;
					}
				}
			}
			
		});
		
		medtXTo.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "XTo";
			}
			
		});
		
		medtXNumOfSteps.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Integer nInput = validateInclusiveIntRange(medtXNumOfSteps, XNUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
					if (nInput != null) {
						mnXNumOfSteps = nInput;
			            mbValidXNumOfSteps = true;
			        } else	{
			        	mbValidXNumOfSteps = false;
					}
				}
			}
			
		});
		
		medtXNumOfSteps.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "XNumOfSteps";
			}
			
		});

		medtYFrom.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateToFromTextInput(medtYFrom, medtYTo, YFROM_INITIAL_VALUE, YTO_INITIAL_VALUE, true); 
					if (dInput != null) {
						mdYFrom = dInput;
				        try {
				            mdYTo = Double.parseDouble(medtYTo.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidYFrom = true;
			            mbValidYTo = true;
			        } else	{
			        	mbValidYFrom = false;
			        	mbValidYTo = false;
					}
				}
			}
			
		});
		
		medtYFrom.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "YFrom";
			}
			
		});
		
		medtYTo.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateToFromTextInput(medtYFrom, medtYTo, YFROM_INITIAL_VALUE, YTO_INITIAL_VALUE, false); 
					if (dInput != null) {
						mdYTo = dInput;
				        try {
				            mdYFrom = Double.parseDouble(medtYFrom.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidYTo = true;
			            mbValidYFrom = true;
			        } else	{
			        	mbValidYTo = false;
			        	mbValidYFrom  = false;
					}
				}
			}
			
		});
		
		medtYTo.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "YTo";
			}
			
		});
		
		medtYNumOfSteps.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Integer nInput = validateInclusiveIntRange(medtYNumOfSteps, YNUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
					if (nInput != null) {
						mnYNumOfSteps = nInput;
			            mbValidYNumOfSteps = true;
			        } else	{
			        	mbValidYNumOfSteps = false;
					}
				}
			}
			
		});
		
		medtYNumOfSteps.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "YNumOfSteps";
			}
			
		});
		

		medtZFrom.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateToFromTextInput(medtZFrom, medtZTo, ZFROM_INITIAL_VALUE, ZTO_INITIAL_VALUE, true); 
					if (dInput != null) {
						mdZFrom = dInput;
				        try {
				            mdZTo = Double.parseDouble(medtZTo.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidZFrom = true;
			            mbValidZTo = true;
			        } else	{
			        	mbValidZFrom = false;
			        	mbValidZTo = false;
					}
				}
			}
			
		});
		
		medtZFrom.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "ZFrom";
			}
			
		});
		
		medtZTo.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateToFromTextInput(medtZFrom, medtZTo, ZFROM_INITIAL_VALUE, ZTO_INITIAL_VALUE, false);  
					if (dInput != null) {
						mdZTo = dInput;
				        try {
				            mdZFrom = Double.parseDouble(medtZFrom.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidZTo = true;
			            mbValidZFrom = true;
			        } else	{
			        	mbValidZTo = false;
			        	mbValidZFrom = false;
					}
				}
			}
			
		});
		
		medtZTo.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "ZTo";
			}
			
		});
		
		medtZNumOfSteps.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Integer nInput = validateInclusiveIntRange(medtZNumOfSteps, ZNUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
					if (nInput != null) {
						mnZNumOfSteps = nInput;
			            mbValidZNumOfSteps = true;
			        } else	{
			        	mbValidZNumOfSteps = false;
					}
				}
			}
			
		});
		
		medtZNumOfSteps.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mstrLastChangedParam = "ZNumOfSteps";
			}
			
		});

		
		mchkBoxNotShowAxisTitle.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
		        if (isChecked) {
		            mbNotShowAxisAndTitle = true;
		        } else {
		            mbNotShowAxisAndTitle = false;
		        }
			}
			
		});
		
        Button btnApply = (Button) findViewById(R.id.apply_button);
        btnApply.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				if (mstrLastChangedParam.equals("XFrom"))	{
					Double dInput = validateToFromTextInput(medtXFrom, medtXTo, XFROM_INITIAL_VALUE, XTO_INITIAL_VALUE, true); 
					if (dInput != null) {
						mdXFrom = dInput;
				        try {
				            mdXTo = Double.parseDouble(medtXTo.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidXFrom = true;
			            mbValidXTo = true;
			        } else	{
			        	mbValidXFrom = false;
			        	mbValidXTo = false;
					}
				} else if (mstrLastChangedParam.equals("XTo"))	{
					Double dInput = validateToFromTextInput(medtXFrom, medtXTo, XFROM_INITIAL_VALUE, XTO_INITIAL_VALUE, false); 
					if (dInput != null) {
						mdXTo = dInput;
				        try {
				            mdXFrom = Double.parseDouble(medtXFrom.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidXTo = true;
			            mbValidXFrom = true;
			        } else	{
			        	mbValidXTo = false;
			        	mbValidXFrom = false;
					}
				} else if (mstrLastChangedParam.equals("XNumOfSteps"))	{
					Integer nInput = validateInclusiveIntRange(medtXNumOfSteps, XNUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
					if (nInput != null) {
						mnXNumOfSteps = nInput;
			            mbValidXNumOfSteps = true;
			        } else	{
			        	mbValidXNumOfSteps = false;
					}
				} else if (mstrLastChangedParam.equals("YFrom"))	{
					Double dInput = validateToFromTextInput(medtYFrom, medtYTo, YFROM_INITIAL_VALUE, YTO_INITIAL_VALUE, true); 
					if (dInput != null) {
						mdYFrom = dInput;
				        try {
				            mdYTo = Double.parseDouble(medtYTo.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidYFrom = true;
			            mbValidYTo = true;
			        } else	{
			        	mbValidYFrom = false;
			        	mbValidYTo = false;
					}
				} else if (mstrLastChangedParam.equals("YTo"))	{
					Double dInput = validateToFromTextInput(medtYFrom, medtYTo, YFROM_INITIAL_VALUE, YTO_INITIAL_VALUE, false); 
					if (dInput != null) {
						mdYTo = dInput;
				        try {
				            mdYFrom = Double.parseDouble(medtYFrom.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidYTo = true;
			            mbValidYFrom = true;
			        } else	{
			        	mbValidYTo = false;
			        	mbValidYFrom  = false;
					}
				} else if (mstrLastChangedParam.equals("YNumOfSteps"))	{
					Integer nInput = validateInclusiveIntRange(medtYNumOfSteps, YNUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
					if (nInput != null) {
						mnYNumOfSteps = nInput;
			            mbValidYNumOfSteps = true;
			        } else	{
			        	mbValidYNumOfSteps = false;
					}
				} else if (mstrLastChangedParam.equals("ZFrom"))	{
					Double dInput = validateToFromTextInput(medtZFrom, medtZTo, ZFROM_INITIAL_VALUE, ZTO_INITIAL_VALUE, true); 
					if (dInput != null) {
						mdZFrom = dInput;
				        try {
				            mdZTo = Double.parseDouble(medtZTo.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidZFrom = true;
			            mbValidZTo = true;
			        } else	{
			        	mbValidZFrom = false;
			        	mbValidZTo = false;
					}
				} else if (mstrLastChangedParam.equals("ZTo"))	{
					Double dInput = validateToFromTextInput(medtZFrom, medtZTo, ZFROM_INITIAL_VALUE, ZTO_INITIAL_VALUE, false);  
					if (dInput != null) {
						mdZTo = dInput;
				        try {
				            mdZFrom = Double.parseDouble(medtZFrom.getText().toString());
				        } catch (NumberFormatException e)  {
				            // this will not happen coz the text has been validated in function validateToFromTextInput
				        }
			            mbValidZTo = true;
			            mbValidZFrom = true;
			        } else	{
			        	mbValidZTo = false;
			        	mbValidZFrom = false;
					}
				} else if (mstrLastChangedParam.equals("ZNumOfSteps"))	{
					Integer nInput = validateInclusiveIntRange(medtZNumOfSteps, ZNUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
					if (nInput != null) {
						mnZNumOfSteps = nInput;
			            mbValidZNumOfSteps = true;
			        } else	{
			        	mbValidZNumOfSteps = false;
					}
				}
				if (mbValidXFrom && mbValidYFrom && mbValidZFrom
						&& mbValidXTo && mbValidYTo && mbValidZTo
						&& mbValidXNumOfSteps && mbValidYNumOfSteps && mbValidZNumOfSteps)	{
						Intent intent = new Intent();
						if (intent != null) {
							Bundle b = new Bundle();
							if (b != null) {
								b.putSerializable("AdjustParams", getAdjParams());
								intent.putExtras(b);
							}
						}
						setResult(Activity.RESULT_OK, intent);
						finish();
					}
			}
        	
        });
	}
	
    public void resetParams()    {
    	mdXFrom = XFROM_INITIAL_VALUE;
    	mdXTo = XTO_INITIAL_VALUE;
    	mnXNumOfSteps = XNUMOFSTEPS_INITIAL_VALUE;
    	mdYFrom = YFROM_INITIAL_VALUE;
    	mdYTo = YTO_INITIAL_VALUE;
    	mnYNumOfSteps = YNUMOFSTEPS_INITIAL_VALUE;
    	mdZFrom = ZFROM_INITIAL_VALUE;
    	mdZTo = ZTO_INITIAL_VALUE;
    	mnZNumOfSteps = ZNUMOFSTEPS_INITIAL_VALUE;
    	mbNotShowAxisAndTitle = NOTSHOWAXISTITLE_INITIAL_VALUE;
    	
        medtXFrom.setText("" + mdXFrom);
        medtXTo.setText("" + mdXFrom);
        medtXNumOfSteps.setText("" + mnXNumOfSteps);
        medtYFrom.setText("" + mdYFrom);
        medtYTo.setText("" + mdYFrom);
        medtYNumOfSteps.setText("" + mnYNumOfSteps);
        medtZFrom.setText("" + mdZFrom);
        medtZTo.setText("" + mdZFrom);
        medtZNumOfSteps.setText("" + mnZNumOfSteps);
        mchkBoxNotShowAxisTitle.setChecked(mbNotShowAxisAndTitle);

    }

    public AdjOGLExprChartParams getAdjParams()	{
    	AdjOGLExprChartParams adjParams = new AdjOGLExprChartParams();
    	adjParams.mdXFrom = mdXFrom;
    	adjParams.mdXTo = mdXTo;
    	adjParams.mnXNumOfSteps = mnXNumOfSteps;
    	adjParams.mdYFrom = mdYFrom;
    	adjParams.mdYTo = mdYTo;
    	adjParams.mnYNumOfSteps = mnYNumOfSteps;
    	adjParams.mdZFrom = mdZFrom;
    	adjParams.mdZTo = mdZTo;
    	adjParams.mnZNumOfSteps = mnZNumOfSteps;
        adjParams.mbNotShowAxisAndTitle = mbNotShowAxisAndTitle;
    	return adjParams;
    }
}
