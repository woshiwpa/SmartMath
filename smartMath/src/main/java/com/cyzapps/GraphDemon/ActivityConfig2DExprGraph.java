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

public class ActivityConfig2DExprGraph extends Activity {

	public static class AdjXYExprChartParams implements Serializable	{
		public double mdXFrom = -5;
		public double mdXTo = 5;
		public double mdYFrom = -5;
		public double mdYTo = 5;
		public int mnNumOfSteps = XYExprChartOperator.DEFAULT_NUM_OF_STEPS;
		public boolean mbAutoStep = true;
		
		public boolean isNoAdj(AdjXYExprChartParams adjOriginal)	{
			return (mdXFrom == adjOriginal.mdXFrom) && (mdXTo == adjOriginal.mdXTo)
					&& (mdYFrom == adjOriginal.mdYFrom) && (mdYTo == adjOriginal.mdYTo)
					&& (mnNumOfSteps == adjOriginal.mnNumOfSteps)
					&& (mbAutoStep == adjOriginal.mbAutoStep);
		}

		public boolean isNoAdj(double dOriginalXFrom, double dOriginalXTo,double dOriginalYFrom, double dOriginalYTo,
				int nOriginalNumOfSteps, boolean bOriginalAutoStep)	{
			return (mdXFrom == dOriginalXFrom) && (mdXTo == dOriginalXTo)
					&& (mdYFrom == dOriginalYFrom) && (mdYTo == dOriginalYTo)
					&& (mnNumOfSteps == nOriginalNumOfSteps)
					&& (mbAutoStep == bOriginalAutoStep);
		}

	}
	protected EditText medtXFrom, medtXTo, medtYFrom, medtYTo, medtNumOfSteps;
	protected CheckBox mchkDetectSing;
	
    public static final int NORMAL_BKGRND_COLOR = Color.WHITE;
    public static final int ERROR_BKGRND_COLOR = Color.YELLOW;

    public static final int MAX_NUM_OF_STEPS = XYExprChartOperator.DEFAULT_NUM_OF_STEPS * 5;
    public static final int MIN_NUM_OF_STEPS = XYExprChartOperator.DEFAULT_NUM_OF_STEPS / 5;
    
    public static final String XAXIS_DEFAULT_NAME = "x";	// variable cannot be overridden, has to use function.
    public static final String YAXIS_DEFAULT_NAME = "y";
    
    public static final double XFROM_INITIAL_VALUE = -5.0;
    public static final double XTO_INITIAL_VALUE = 5.0;
    public static final double YFROM_INITIAL_VALUE = -5.0;
    public static final double YTO_INITIAL_VALUE = 5.0;
    
    public String getXAxisDefaultName()	{return XAXIS_DEFAULT_NAME;}
    public String getYAxisDefaultName()	{return YAXIS_DEFAULT_NAME;}
    public double getXFromInitialValue()	{return XFROM_INITIAL_VALUE;}
    public double getXToInitialValue()	{return XTO_INITIAL_VALUE;}
    public double getYFromInitialValue()	{return YFROM_INITIAL_VALUE;}
    public double getYToInitialValue()	{return YTO_INITIAL_VALUE;}
    
    public static final int NUMOFSTEPS_INITIAL_VALUE = XYExprChartOperator.DEFAULT_NUM_OF_STEPS;
    public static final boolean AUTOSTEP_INITIAL_VALUE = true;
	
    protected double mdXFrom = getXFromInitialValue();
    protected double mdXTo = getXToInitialValue();
    protected double mdYFrom = getYFromInitialValue();
    protected double mdYTo = getYToInitialValue();
    protected int mnNumOfSteps = NUMOFSTEPS_INITIAL_VALUE;
    protected boolean mbAutoStep = AUTOSTEP_INITIAL_VALUE;
	
    protected String mstrLastChangedParam = "";
    protected boolean mbValidXFrom = true;
    protected boolean mbValidXTo = true;
    protected boolean mbValidYFrom = true;
    protected boolean mbValidYTo = true;
    protected boolean mbValidNumOfSteps = true;

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
        setTitle("====== " + getString(R.string.config_2D_title) + " ======");	// title should not be too short, otherwise, dialog will be too narrow.
        setContentView(R.layout.config_2dexprgraph);

        String strXAxisName = null, strYAxisName = null, strZAxisName = null;
        AdjXYExprChartParams adjParams = null;
        //First Extract the bundle from intent
        Intent intent = getIntent();
        if (intent != null)	{
        	//Next extract the values using the key as
        	strXAxisName = intent.getStringExtra("XAxisName");
        	strYAxisName = intent.getStringExtra("YAxisName");
        	adjParams = (AdjXYExprChartParams) intent.getSerializableExtra("AdjustParams");
        }

        if (strXAxisName == null)	{
    		strXAxisName = getXAxisDefaultName();
    	}
    	if (strYAxisName == null)	{
    		strYAxisName = getYAxisDefaultName();
    	}
     	if (adjParams == null)	{
    		adjParams = new AdjXYExprChartParams();
    		// these variables (adjParams.mdXFrom etc)'s original value may be different in a derived class
    		adjParams.mdXFrom = getXFromInitialValue();
    		adjParams.mdXTo = getXToInitialValue();
    		adjParams.mdYFrom = getYFromInitialValue();
    		adjParams.mdYTo = getYToInitialValue();
    	}
    	
     	TextView txtXFrom = (TextView) findViewById(R.id.x_from_prompt);
    	TextView txtYFrom = (TextView) findViewById(R.id.y_from_prompt);
		txtXFrom.setText(strXAxisName + ": " + txtXFrom.getText());
		txtYFrom.setText(strYAxisName + ": " + txtYFrom.getText());
       
    	TextView txtNumOfSteps = (TextView) findViewById(R.id.number_of_steps_prompt);
     	txtNumOfSteps.setText(txtNumOfSteps.getText() + " (" + MIN_NUM_OF_STEPS + "-" +  MAX_NUM_OF_STEPS + ")");
    	
		medtXFrom = (EditText) findViewById(R.id.x_from_edit);
		medtXTo = (EditText) findViewById(R.id.x_to_edit);
		medtYFrom = (EditText) findViewById(R.id.y_from_edit);
		medtYTo = (EditText) findViewById(R.id.y_to_edit);
		medtNumOfSteps = (EditText) findViewById(R.id.number_of_steps_edit);
		mchkDetectSing = (CheckBox) findViewById(R.id.detect_singular_point);

		medtXFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtXTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtYFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtYTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtNumOfSteps.setBackgroundColor(NORMAL_BKGRND_COLOR);
		
		medtXFrom.setText(String.valueOf(adjParams.mdXFrom));
		medtXTo.setText(String.valueOf(adjParams.mdXTo));
		medtYFrom.setText(String.valueOf(adjParams.mdYFrom));
		medtYTo.setText(String.valueOf(adjParams.mdYTo));
		medtNumOfSteps.setText(String.valueOf(adjParams.mnNumOfSteps));
		mchkDetectSing.setChecked(adjParams.mbAutoStep);
		if (adjParams.mbAutoStep)	{
			medtNumOfSteps.setEnabled(false);
		} else	{
			medtNumOfSteps.setEnabled(true);
		}
		
		mdXFrom = adjParams.mdXFrom;
		mdXTo = adjParams.mdXTo;
		mdYFrom = adjParams.mdYFrom;
		mdYTo = adjParams.mdYTo;
		mnNumOfSteps = adjParams.mnNumOfSteps;
		mbAutoStep = adjParams.mbAutoStep;

		medtXFrom.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					validateXFrom();
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
					validateXTo();
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
		
		medtYFrom.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					validateYFrom();
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
					validateYTo();
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
		
		medtNumOfSteps.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					validateNumOfSteps();
				}
			}
			
		});
		
		medtNumOfSteps.addTextChangedListener(new TextWatcher(){

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
				mstrLastChangedParam = "NumOfSteps";
			}
			
		});
		
		mchkDetectSing.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// needs not to do anything.
			}
			
		});
		
		mchkDetectSing.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked)	{
					String strStoredLCP = mstrLastChangedParam;
					medtNumOfSteps.setText("" + NUMOFSTEPS_INITIAL_VALUE);
					medtNumOfSteps.setBackgroundColor(NORMAL_BKGRND_COLOR);
					mnNumOfSteps = NUMOFSTEPS_INITIAL_VALUE;
					mbValidNumOfSteps = true;
					medtNumOfSteps.setEnabled(false);
					mstrLastChangedParam = strStoredLCP;	// restore last changed param.
				} else	{
					medtNumOfSteps.setEnabled(true);
				}
				mbAutoStep = isChecked;
			}
			
		});

        Button btnApply = (Button) findViewById(R.id.apply_button);
        btnApply.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				if (mstrLastChangedParam.equals("XFrom"))	{
					validateXFrom();
				} else if (mstrLastChangedParam.equals("XTo"))	{
					validateXTo();
				} else if (mstrLastChangedParam.equals("YFrom"))	{
					validateYFrom();
				} else if (mstrLastChangedParam.equals("YTo"))	{
					validateYTo();
				} else if (mstrLastChangedParam.equals("NumOfSteps"))	{
					validateNumOfSteps();
				}
				if (mbValidXFrom && mbValidYFrom && mbValidXTo && mbValidYTo && mbValidNumOfSteps)	{
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
	
	protected void validateXFrom()	{
		Double dInput = validateToFromTextInput(medtXFrom, medtXTo, getXFromInitialValue(), getXToInitialValue(), true); 
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
	
	protected void validateXTo()	{
		Double dInput = validateToFromTextInput(medtXFrom, medtXTo, getXFromInitialValue(), getXToInitialValue(), false); 
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
	
	protected void validateYFrom()	{
		Double dInput = validateToFromTextInput(medtYFrom, medtYTo, getYFromInitialValue(), getYToInitialValue(), true); 
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
	
	protected void validateYTo()	{
		Double dInput = validateToFromTextInput(medtYFrom, medtYTo, getYFromInitialValue(), getYToInitialValue(), false); 
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
	
	protected void validateNumOfSteps()	{
		Integer nInput = validateInclusiveIntRange(medtNumOfSteps, NUMOFSTEPS_INITIAL_VALUE, MIN_NUM_OF_STEPS, MAX_NUM_OF_STEPS); 
		if (nInput != null) {
			mnNumOfSteps = nInput;
            mbValidNumOfSteps = true;
        } else	{
        	mbValidNumOfSteps = false;
		}
	}
	
    public void resetParams()    {
    	mdXFrom = getXFromInitialValue();
    	mdXTo = getXToInitialValue();
    	mdYFrom = getYFromInitialValue();
    	mdYTo = getYToInitialValue();
    	mnNumOfSteps = NUMOFSTEPS_INITIAL_VALUE;
    	mbAutoStep = AUTOSTEP_INITIAL_VALUE;
    	
        medtXFrom.setText("" + mdXFrom);
        medtXTo.setText("" + mdXFrom);
        medtYFrom.setText("" + mdYFrom);
        medtYTo.setText("" + mdYFrom);
        medtNumOfSteps.setText("" + mnNumOfSteps);
        mchkDetectSing.setChecked(mbAutoStep);	// medtNumOfSteps should be automatically enabled or disabled.
    }

    public AdjXYExprChartParams getAdjParams()	{
    	AdjXYExprChartParams adjParams = new AdjXYExprChartParams();
    	adjParams.mdXFrom = mdXFrom;
    	adjParams.mdXTo = mdXTo;
    	adjParams.mdYFrom = mdYFrom;
    	adjParams.mdYTo = mdYTo;
    	adjParams.mnNumOfSteps = mnNumOfSteps;
    	adjParams.mbAutoStep = mbAutoStep;
    	return adjParams;
    }
}
