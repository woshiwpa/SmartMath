package com.cyzapps.GraphDemon;

import java.io.Serializable;

import com.cyzapps.SmartMath.R;
import com.cyzapps.PlotAdapter.OGLChart;

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

public class ActivityConfigXYZGraph extends Activity	{
	
    private void setChartZoomingRatio()   {
        if (mdXScale == mdYScale && mdYScale == mdZScale
                && mdXScale >= Math.pow(10.0, ZOOMING_MIN)
                && mdXScale <= Math.pow(10.0, ZOOMING_MAX))   {
            medtZoomWholeChart.setEnabled(true);
            medtZoomWholeChart.setText("" + mdXScale);
            mbValidWholeChartScale = true;
        } else  {
        	medtZoomWholeChart.setEnabled(false);
        }
    }
    
    public static Double validateNumericTextInput(EditText edtInput, Double dDefault, Double dMin, Double dMax)    {
        String strText = edtInput.getText().toString();
        double dValue = dDefault;
        Boolean bInputRight = true;
        try {
            dValue = Double.parseDouble(strText);
        } catch (NumberFormatException e)  {
            bInputRight = false;
        }
        if (dMin != null && dValue < dMin)  {
            bInputRight = false;
        } else if (dMax != null && dValue > dMax)  {
            bInputRight = false;
        }
        if (!bInputRight)    {
        	edtInput.setBackgroundColor(ERROR_BKGRND_COLOR);
        } else  {
        	edtInput.setBackgroundColor(NORMAL_BKGRND_COLOR);
        }
        if (bInputRight)    {
            return dValue;
        } else  {
            return null;
        }
    }
    
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setTitle("====== " + getString(R.string.config_3D_title) + " ======");	// title should not be too short, otherwise, dialog will be too narrow.
        setContentView(R.layout.config_xyzgraph);
 
        Intent intent = getIntent();
        if (intent != null)	{
        	//Next extract the values using the key as
        	mbChartNotShowAxisAndTitle = intent.getBooleanExtra("NotShowAxisTitle", NOTSHOWAXISTITLE_INITIAL_VALUE);
        }
		medtZoomWholeChart = (EditText) findViewById(R.id.zoom_whole_chart_edit);
		medtZoomX = (EditText) findViewById(R.id.zoom_x_edit);
		medtZoomY = (EditText) findViewById(R.id.zoom_y_edit);
		medtZoomZ = (EditText) findViewById(R.id.zoom_z_edit);
		medtShiftX = (EditText) findViewById(R.id.shift_x_edit);
		medtShiftY = (EditText) findViewById(R.id.shift_y_edit);
		medtShiftZ = (EditText) findViewById(R.id.shift_z_edit);
		medtRotateX = (EditText) findViewById(R.id.rotate_x_edit);
		medtRotateY = (EditText) findViewById(R.id.rotate_y_edit);
		medtRotateZ = (EditText) findViewById(R.id.rotate_z_edit);
		
		mchkBoxNotShowAxisTitle = (CheckBox) findViewById(R.id.chkBoxNotShowAxisTitle);
		
		medtZoomWholeChart.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtZoomX.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtZoomY.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtZoomZ.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtShiftX.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtShiftY.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtShiftZ.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtRotateX.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtRotateY.setBackgroundColor(NORMAL_BKGRND_COLOR);
		medtRotateZ.setBackgroundColor(NORMAL_BKGRND_COLOR);
		
		resetParams();
		
		medtZoomWholeChart.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
			        Double dInput = validateNumericTextInput(medtZoomWholeChart, CHARTSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
			        if (dInput != null) {
			            mdXScale = mdYScale = mdZScale = dInput;
			            medtZoomX.setText("" + mdXScale);
			            medtZoomX.setBackgroundColor(NORMAL_BKGRND_COLOR);
			            medtZoomY.setText("" + mdYScale);
			            medtZoomY.setBackgroundColor(NORMAL_BKGRND_COLOR);
			            medtZoomZ.setText("" + mdZScale);
			            medtZoomZ.setBackgroundColor(NORMAL_BKGRND_COLOR);
			            mbValidWholeChartScale = mbValidXScale = mbValidYScale = mbValidZScale = true;
			        } else	{
			        	mbValidWholeChartScale = false;
			        }
				}
			}
			
		});
		medtZoomWholeChart.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ZoomWholeChart";
			}
			
		});
		
		
		medtZoomX.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
			        Double dInput = validateNumericTextInput(medtZoomX, XSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
			        if (dInput != null) {
			            mdXScale = dInput;
			            setChartZoomingRatio();
			            mbValidXScale = true;
			        } else	{
			        	mbValidXScale = false;
			        }
				}
			}
			
		});
		medtZoomX.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ZoomX";
			}
			
		});
		
		
		medtZoomY.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
			        Double dInput = validateNumericTextInput(medtZoomY, YSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
			        if (dInput != null) {
			            mdYScale = dInput;
			            setChartZoomingRatio();
			            mbValidYScale = true;
			        } else	{
			        	mbValidYScale = false;
			        }
				}
			}
			
		});
		medtZoomY.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ZoomY";
			}
			
		});
		
		
		medtZoomZ.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
			        Double dInput = validateNumericTextInput(medtZoomZ, ZSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
			        if (dInput != null) {
			            mdZScale = dInput;
			            setChartZoomingRatio();
			            mbValidZScale = true;
			        } else	{
			        	mbValidZScale = false;
			        }
				}
			}
			
		});
		medtZoomZ.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ZoomZ";
			}
			
		});
		
		
		medtShiftX.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateNumericTextInput(medtShiftX, XSHIFT_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdXShift = dInput;
			            mbValidXShift = true;
			        } else	{
			        	mbValidXShift = false;
					}
				}
			}
			
		});
		medtShiftX.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ShiftX";
			}
			
		});
		
		
		medtShiftY.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateNumericTextInput(medtShiftY, YSHIFT_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdYShift = dInput;
			            mbValidYShift = true;
			        } else	{
			        	mbValidYShift = false;
					}
				}
			}
			
		});
		medtShiftY.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ShiftY";
			}
			
		});
		
		medtShiftZ.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateNumericTextInput(medtShiftZ, ZSHIFT_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdZShift = dInput;
			            mbValidZShift = true;
			        } else	{
			        	mbValidZShift = false;
					}
				}
			}
			
		});
		medtShiftZ.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "ShiftZ";
			}
			
		});
		
		medtRotateX.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateNumericTextInput(medtRotateX, XROTATE_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdXRotate = dInput;
			            mbValidXRotate = true;
			        } else	{
			        	mbValidXRotate = false;
					}
				}
			}
			
		});
		medtRotateX.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "RotateX";
			}
			
		});
		
		medtRotateY.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateNumericTextInput(medtRotateY, YROTATE_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdYRotate = dInput;
			            mbValidYRotate = true;
			        } else	{
			        	mbValidYRotate = false;
					}
				}
			}
			
		});
		medtRotateY.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "RotateY";
			}
			
		});
		
		medtRotateZ.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)	{
					Double dInput = validateNumericTextInput(medtRotateZ, ZROTATE_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdZRotate = dInput;
			            mbValidZRotate = true;
			        } else	{
			        	mbValidZRotate = false;
					}
				}
			}
			
		});
		medtRotateZ.addTextChangedListener(new TextWatcher(){

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
				// TODO Auto-generated method stub
				mstrLastChangedParam = "RotateZ";
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
			public void onClick(View arg0) {
				Double dInput;
				if (mstrLastChangedParam.equals("ZoomWholeChart"))	{
					dInput = validateNumericTextInput(medtZoomWholeChart, CHARTSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
					if (dInput != null) {
						mdXScale = mdYScale = mdZScale = dInput;
						medtZoomX.setText("" + mdXScale);
						medtZoomX.setBackgroundColor(NORMAL_BKGRND_COLOR);
						medtZoomY.setText("" + mdYScale);
						medtZoomY.setBackgroundColor(NORMAL_BKGRND_COLOR);
						medtZoomZ.setText("" + mdZScale);
						medtZoomZ.setBackgroundColor(NORMAL_BKGRND_COLOR);
			            mbValidWholeChartScale = mbValidXScale = mbValidYScale = mbValidZScale = true;
			        } else	{
			        	mbValidWholeChartScale = false;
					}
				} else if (mstrLastChangedParam.equals("ZoomX"))	{
					dInput = validateNumericTextInput(medtZoomX, XSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
					if (dInput != null) {
						mdXScale = dInput;
						setChartZoomingRatio();
			            mbValidXScale = true;
			        } else	{
			        	mbValidXScale = false;
					}
				} else if (mstrLastChangedParam.equals("ZoomY"))	{
					dInput = validateNumericTextInput(medtZoomY, YSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
					if (dInput != null) {
						mdYScale = dInput;
						setChartZoomingRatio();
			            mbValidYScale = true;
			        } else	{
			        	mbValidYScale = false;
					}
				} else if (mstrLastChangedParam.equals("ZoomZ"))	{
					dInput = validateNumericTextInput(medtZoomZ, ZSCALE_INITIAL_VALUE, Math.pow(10.0, ZOOMING_MIN), Math.pow(10.0, ZOOMING_MAX)); 
					if (dInput != null) {
						mdZScale = dInput;
						setChartZoomingRatio();
			            mbValidZScale = true;
			        } else	{
			        	mbValidZScale = false;
					}
				} else if (mstrLastChangedParam.equals("ShiftX"))	{
					dInput = validateNumericTextInput(medtShiftX, XSHIFT_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdXShift = dInput;
			            mbValidXShift = true;
			        } else	{
			        	mbValidXShift = false;
					}
				} else if (mstrLastChangedParam.equals("ShiftY"))	{
					dInput = validateNumericTextInput(medtShiftY, YSHIFT_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdYShift = dInput;
			            mbValidYShift = true;
			        } else	{
			        	mbValidYShift = false;
					}
				} else if (mstrLastChangedParam.equals("ShiftZ"))	{
					dInput = validateNumericTextInput(medtShiftZ, ZSHIFT_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdZShift = dInput;
			            mbValidZShift = true;
			        } else	{
			        	mbValidZShift = false;
					}
				} else if (mstrLastChangedParam.equals("RotateX"))	{
					dInput = validateNumericTextInput(medtRotateX, XROTATE_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdXRotate = dInput;
			            mbValidXRotate = true;
			        } else	{
			        	mbValidXRotate = false;
					}
				} else if (mstrLastChangedParam.equals("RotateY"))	{
					dInput = validateNumericTextInput(medtRotateY, YROTATE_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdYRotate = dInput;
			            mbValidYRotate = true;
			        } else	{
			        	mbValidYRotate = false;
					}
				} else if (mstrLastChangedParam.equals("RotateZ"))	{
					dInput = validateNumericTextInput(medtRotateZ, ZROTATE_INITIAL_VALUE, null, null); 
					if (dInput != null) {
						mdZRotate = dInput;
			            mbValidZRotate = true;
			        } else	{
			        	mbValidZRotate = false;
					}
				}
				if (mbValidXScale && mbValidYScale && mbValidZScale	// do not consider mbValidWholeChartScale
					&& mbValidXShift && mbValidYShift && mbValidZShift
					&& mbValidXRotate && mbValidYRotate && mbValidZRotate)	{
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
	
	public static class AdjOGLChartParams implements Serializable	{
		public double mdXScale = 1;
		public double mdYScale = 1;
		public double mdZScale = 1;
		public double mdXShift = 0;
		public double mdYShift = 0;
		public double mdZShift = 0;
		public double mdXRotate = 0;
		public double mdYRotate = 0;
		public double mdZRotate = 0;
        public boolean mbShowAxisAndTitleChange = false;
		
		public boolean isNoAdj()	{
			return (mdXScale == 1) && (mdYScale == 1) && (mdZScale == 1)
					&& (mdXShift == 0) && (mdYShift == 0) && (mdZShift == 0)
					&& (mdXRotate == 0) && (mdYRotate == 0) && (mdZRotate == 0)
            		&& (!mbShowAxisAndTitleChange);
		}
	}
	
	private EditText medtZoomWholeChart, medtZoomX, medtZoomY, medtZoomZ,
					medtShiftX, medtShiftY, medtShiftZ,
					medtRotateX, medtRotateY, medtRotateZ;
	
	private CheckBox mchkBoxNotShowAxisTitle;
	
    public static final int NORMAL_BKGRND_COLOR = Color.WHITE;
    public static final int ERROR_BKGRND_COLOR = Color.YELLOW;

    public static final int ZOOMING_MAX = 2; // 10 ** 2
    public static final int ZOOMING_MIN = -2;    // 10 ** -2
    public static final double CHARTSCALE_INITIAL_VALUE = 1.0;
    public static final double XSCALE_INITIAL_VALUE = 1.0;
    public static final double YSCALE_INITIAL_VALUE = 1.0;
    public static final double ZSCALE_INITIAL_VALUE = 1.0;
    public static final double XSHIFT_INITIAL_VALUE = 0.0;
    public static final double YSHIFT_INITIAL_VALUE = 0.0;
    public static final double ZSHIFT_INITIAL_VALUE = 0.0;
    public static final double XROTATE_INITIAL_VALUE = 0.0;
    public static final double YROTATE_INITIAL_VALUE = 0.0;
    public static final double ZROTATE_INITIAL_VALUE = 0.0;
    public static final boolean NOTSHOWAXISTITLE_INITIAL_VALUE = false;
    
    private double mdXScale = XSCALE_INITIAL_VALUE;
    private double mdYScale = YSCALE_INITIAL_VALUE;
    private double mdZScale = ZSCALE_INITIAL_VALUE;
    private double mdXShift = XSHIFT_INITIAL_VALUE;
    private double mdYShift = YSHIFT_INITIAL_VALUE;
    private double mdZShift = ZSHIFT_INITIAL_VALUE;
    private double mdXRotate = XROTATE_INITIAL_VALUE;
    private double mdYRotate = YROTATE_INITIAL_VALUE;
    private double mdZRotate = ZROTATE_INITIAL_VALUE;
    private boolean mbChartNotShowAxisAndTitle = NOTSHOWAXISTITLE_INITIAL_VALUE;
    private boolean mbNotShowAxisAndTitle = NOTSHOWAXISTITLE_INITIAL_VALUE;
    
    private String mstrLastChangedParam = "";
    private boolean mbValidWholeChartScale = true;
    private boolean mbValidXScale = true;
    private boolean mbValidYScale = true;
    private boolean mbValidZScale = true;
    private boolean mbValidXShift = true;
    private boolean mbValidYShift = true;
    private boolean mbValidZShift = true;
    private boolean mbValidXRotate = true;
    private boolean mbValidYRotate = true;
    private boolean mbValidZRotate = true;
    
   
    public void resetParams()    {
		mdXScale = XSCALE_INITIAL_VALUE;
		mdYScale = YSCALE_INITIAL_VALUE;
		mdZScale = ZSCALE_INITIAL_VALUE;
		mdXShift = XSHIFT_INITIAL_VALUE;
		mdYShift = YSHIFT_INITIAL_VALUE;
		mdZShift = ZSHIFT_INITIAL_VALUE;
		mdXRotate = XROTATE_INITIAL_VALUE;
		mdYRotate = YROTATE_INITIAL_VALUE;
		mdZRotate = ZROTATE_INITIAL_VALUE;
        mbNotShowAxisAndTitle = mbChartNotShowAxisAndTitle;
		
        medtZoomX.setText("" + mdXScale);
        medtZoomY.setText("" + mdYScale);
        medtZoomZ.setText("" + mdZScale);
        medtShiftX.setText("" + mdXShift);
        medtShiftY.setText("" + mdYShift);
        medtShiftZ.setText("" + mdZShift);
        medtRotateX.setText("" + mdXRotate);
        medtRotateY.setText("" + mdYRotate);
        medtRotateZ.setText("" + mdZRotate);
        setChartZoomingRatio();
        mchkBoxNotShowAxisTitle.setChecked(mbNotShowAxisAndTitle);
    }
    
    public AdjOGLChartParams getAdjParams()	{
    	AdjOGLChartParams adjParams = new AdjOGLChartParams();
    	adjParams.mdXScale = mdXScale;
		adjParams.mdYScale = mdYScale;
		adjParams.mdZScale = mdZScale;
		adjParams.mdXShift = mdXShift;
		adjParams.mdYShift = mdYShift;
		adjParams.mdZShift = mdZShift;
		adjParams.mdXRotate = mdXRotate;
		adjParams.mdYRotate = mdYRotate;
		adjParams.mdZRotate = mdZRotate;
        adjParams.mbShowAxisAndTitleChange = (mbNotShowAxisAndTitle != mbChartNotShowAxisAndTitle);
		return adjParams;
    }
}
