package com.cyzapps.SmartMath;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptInterrupter;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.AbstractExprInterrupter;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.adapter.MFPAdapter;

public class ActivityIntegrate extends ActivityImeMultiEdtsOri	{
	public String mstrIntegration = "";
	public String mstrIntegResult = "";
	public String mstrErrMsg = "";
	public Thread mthreadIntegrate = null;
    // Need handler for callbacks to the UI thread
    public Handler mHandler = new Handler();
	
	public static final String IMMUTABLE_INPUTPAD_CONFIG = "immutable_inputpad_integ_plot.cfg";
	
	public static final int SELECT_INDEFINITE_INTEGRAL = 0;
	public static final int SELECT_1ST_LEVEL_INTEGRAL = 1;
	public static final int SELECT_2ND_LEVEL_INTEGRAL = 2;
	public static final int SELECT_3RD_LEVEL_INTEGRAL = 3;
	
	
    public class CalcIntegralFunctionInterrupter extends FunctionInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public class CalcIntegralScriptInterrupter extends ScriptInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public class CalcIntegralAbstractExprInterrupter extends AbstractExprInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }

	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		if (MFPAdapter.isFuncSpaceEmpty())	{
			MFPAdapter.reloadAll(this, 1, null);	// actually, to be save, should load ads only after this finish. But make things simple here.
													// assume the possiblity that ads and webview get problem here is very very small.
		}
		ActivitySettings.readSettings();	// have to call readsettings again here because when reload, readsettings in main panel is not executed.

        setFullScreenForSmallLandscape();
        
        setContentView(R.layout.calc_integ);

        Spinner spinnerMultipleIntegral = (Spinner) findViewById(R.id.multiple_integral_spinner);
        spinnerMultipleIntegral.setOnItemSelectedListener(new OnItemSelectedListener()	{

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				View vx = findViewById(R.id.dx_input);
				View vxFromToStep = findViewById(R.id.x_variable_from_to_step_frame);
				View vy = findViewById(R.id.dy_input);
				View vz = findViewById(R.id.dz_input);
				EditText etXSteps = (EditText)findViewById(R.id.x_variable_number_of_steps_edit);
				EditText etYSteps = (EditText)findViewById(R.id.y_variable_number_of_steps_edit);
				EditText etZSteps = (EditText)findViewById(R.id.z_variable_number_of_steps_edit);
				View vNote = findViewById(R.id.integrate_note_view);
				switch (arg2)	{
				case SELECT_1ST_LEVEL_INTEGRAL:
					vx.setVisibility(View.VISIBLE);
					vxFromToStep.setVisibility(View.VISIBLE);
					vy.setVisibility(View.GONE);
					vz.setVisibility(View.GONE);
					etXSteps.setText("200");	// first level integral, by default 200 steps
					vNote.setVisibility(View.VISIBLE);
					break;
				case SELECT_2ND_LEVEL_INTEGRAL:
					vx.setVisibility(View.VISIBLE);
					vxFromToStep.setVisibility(View.VISIBLE);
					vy.setVisibility(View.VISIBLE);
					vz.setVisibility(View.GONE);
					etXSteps.setText("30");	// 2nd level integral, by default 30 steps
					etYSteps.setText("30");	// 2nd level integral, by default 30 steps
					vNote.setVisibility(View.GONE);
					break;
				case SELECT_3RD_LEVEL_INTEGRAL:
					vx.setVisibility(View.VISIBLE);
					vxFromToStep.setVisibility(View.VISIBLE);
					vy.setVisibility(View.VISIBLE);
					vz.setVisibility(View.VISIBLE);
					etXSteps.setText("10");	// 3rd level integral, by default 20 steps
					etYSteps.setText("10");	// 3rd level integral, by default 20 steps
					etZSteps.setText("10");	// 3rd level integral, by default 20 steps
					vNote.setVisibility(View.GONE);
					break;
				default:	//case 0: indefinite integral
					vx.setVisibility(View.VISIBLE);
					vxFromToStep.setVisibility(View.GONE);
					vy.setVisibility(View.GONE);
					vz.setVisibility(View.GONE);
					vNote.setVisibility(View.GONE);
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });
        
        adjustMargin();
        //spinnerMultipleIntegral.setSelection(1);
        // fill as much screen space as possible.
		//int nScreenOrientation = getResources().getConfiguration().orientation;
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL
				|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{
	        spinnerMultipleIntegral.setSelection(SELECT_INDEFINITE_INTEGRAL);	// show indefinite integral by default
		} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
			spinnerMultipleIntegral.setSelection(SELECT_2ND_LEVEL_INTEGRAL);
		} else	{	//xlarge
			spinnerMultipleIntegral.setSelection(SELECT_3RD_LEVEL_INTEGRAL);
		}

        Button btnCalc = (Button) findViewById(R.id.start_to_integrate_button);
        btnCalc.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				calcIntegration();
			}
        	
        });
               
        mlayoutBaseView = (LinearLayout)findViewById(R.id.calc_integ_base_view);
        mlayoutIMEHolder = (LinearLayout)findViewById(R.id.layoutICInputMethodHolder);
		EditText edtExpr = (EditText)findViewById(R.id.integrated_expr_edit);
		mstrImmutableInputPadConfig = IMMUTABLE_INPUTPAD_CONFIG;
		mstrUsrDefInputPadConfig = ActivityCfgKeyPad.INPUTPAD_INTEG_PLOT_CONFIG;
		initInputMethod(edtExpr);
	}

	public void calcIntegration()	{
		EditText edtIntegratedExpr = (EditText) findViewById(R.id.integrated_expr_edit);
		String strIntegratedExpr = edtIntegratedExpr.getText().toString();
		
		EditText edtXName = (EditText) findViewById(R.id.x_variable_name_edit);
		String strXName = edtXName.getText().toString();
		EditText edtXFrom = (EditText) findViewById(R.id.x_variable_from_edit);
		String strXFrom = edtXFrom.getText().toString();
		EditText edtXTo = (EditText) findViewById(R.id.x_variable_to_edit);
		String strXTo = edtXTo.getText().toString();
		EditText edtXNumofSteps = (EditText) findViewById(R.id.x_variable_number_of_steps_edit);
		String strXNumofSteps = edtXNumofSteps.getText().toString();
		if (strXNumofSteps.trim().length() == 0) {
			strXNumofSteps = getString(R.string.default_number_of_steps_integ);	// by default, number of steps is 0
			if (strXNumofSteps.trim().length() == 0) {
				strXNumofSteps = "0";
			}
		}
        
		EditText edtYName = (EditText) findViewById(R.id.y_variable_name_edit);
		String strYName = edtYName.getText().toString();
		EditText edtYFrom = (EditText) findViewById(R.id.y_variable_from_edit);
		String strYFrom = edtYFrom.getText().toString();
		EditText edtYTo = (EditText) findViewById(R.id.y_variable_to_edit);
		String strYTo = edtYTo.getText().toString();
		EditText edtYNumofSteps = (EditText) findViewById(R.id.y_variable_number_of_steps_edit);
		String strYNumofSteps = edtYNumofSteps.getText().toString();
		if (strYNumofSteps.trim().length() == 0) {
			strYNumofSteps = getString(R.string.default_number_of_steps_integ);	// by default, number of steps is 0
			if (strYNumofSteps.trim().length() == 0) {
				strYNumofSteps = "0";
			}
		}
        
		EditText edtZName = (EditText) findViewById(R.id.z_variable_name_edit);
		String strZName = edtZName.getText().toString();
		EditText edtZFrom = (EditText) findViewById(R.id.z_variable_from_edit);
		String strZFrom = edtZFrom.getText().toString();
		EditText edtZTo = (EditText) findViewById(R.id.z_variable_to_edit);
		String strZTo = edtZTo.getText().toString();
		EditText edtZNumofSteps = (EditText) findViewById(R.id.z_variable_number_of_steps_edit);
		String strZNumofSteps = edtZNumofSteps.getText().toString();
		if (strZNumofSteps.trim().length() == 0) {
			strZNumofSteps = getString(R.string.default_number_of_steps_integ);	// by default, number of steps is 0
			if (strZNumofSteps.trim().length() == 0) {
				strZNumofSteps = "0";
			}
		}
		
/*		strIntegratedExpr = "a-b+c";
		strXName = "a";
		strXFrom = "41 + 7*i";
		strXTo = "8 - 22 *i";
		strXNumofSteps = "3";
		strYName = "b";
		strYFrom = "28";
		strYTo = "91";
		strYNumofSteps = "6";
		strZName = "c";
		strZFrom = "53*i";
		strZTo = "137 *i";
		strZNumofSteps = "8";*/
		
		String strTmp = "";
        Spinner spinnerMultipleIntegral = (Spinner) findViewById(R.id.multiple_integral_spinner);       
		switch (spinnerMultipleIntegral.getSelectedItemPosition())	{
		case SELECT_INDEFINITE_INTEGRAL:
			strTmp = "Integrate(\"" + strIntegratedExpr + "\", \"" + strXName + "\")";
			break;
		case SELECT_1ST_LEVEL_INTEGRAL:
			strTmp = "Integrate(\"" + strIntegratedExpr + "\", \"" + strXName + "\", " + strXFrom + ", " + strXTo + ", " + strXNumofSteps + ")";
			break;
		case SELECT_2ND_LEVEL_INTEGRAL:
			strTmp = "Integrate(\\\"" + strIntegratedExpr + "\\\", \\\"" + strXName + "\\\", " + strXFrom + ", " + strXTo + ", " + strXNumofSteps + ")";
			strTmp = "Integrate(\"" + strTmp + "\", \"" + strYName + "\", " + strYFrom + ", " + strYTo + ", " + strYNumofSteps + ")";
			break;
		case SELECT_3RD_LEVEL_INTEGRAL:
			strTmp = "Integrate(\\\\\\\"" + strIntegratedExpr + "\\\\\\\", \\\\\\\"" + strXName + "\\\\\\\", " + strXFrom + ", " + strXTo + ", " + strXNumofSteps + ")";
			strTmp = "Integrate(\\\"" + strTmp + "\\\", \\\"" + strYName + "\\\", " + strYFrom + ", " + strYTo + ", " + strYNumofSteps + ")";
			strTmp = "Integrate(\"" + strTmp + "\", \"" + strZName + "\", " + strZFrom + ", " + strZTo + ", " + strZNumofSteps + ")";
			break;
		}
		
		mstrIntegration = strTmp;
		final ProgressDialog dlgIntegProgress = ProgressDialog.show(this, getString(R.string.please_wait),
				getString(R.string.calculating_integrating_result), true);

		mthreadIntegrate = new Thread(new Runnable(){

			@Override
			public void run() {
				/* evaluate the expression */
				ExprEvaluator exprEvaluator = new ExprEvaluator();
				// clear variable namespaces
				exprEvaluator.m_lVarNameSpaces = new LinkedList<LinkedList<Variable>>();

				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				// integrate screen does not output log or chart or read/write files. But should
				// be interrupted.
				FuncEvaluator.msfunctionInterrupter = new CalcIntegralFunctionInterrupter();
				ScriptAnalyzer.msscriptInterrupter = new CalcIntegralScriptInterrupter();
				AbstractExpr.msaexprInterrupter = new CalcIntegralAbstractExprInterrupter();
				FuncEvaluator.msstreamConsoleInput = null;
				FuncEvaluator.msstreamLogOutput = null;
				FuncEvaluator.msfileOperator = null;
				FuncEvaluator.msgraphPlotter = null;
				FuncEvaluator.msgraphPlotter3D = null;
                if (FuncEvaluator.mspm == null) {
                    FuncEvaluator.mspm = new PatternManager();
                    try {
						FuncEvaluator.mspm.loadPatterns(2);	// load pattern is a very time consuming work. So only do this if needed.
					} catch (Exception e) {
						 // load all integration patterns. Assume load patterns will not throw any exceptions.
					}
                 }

				DataClass datumReturn = null; 
				try	{
					datumReturn = exprEvaluator.evaluateExpression(mstrIntegration, curpos);
					if (datumReturn != null)	{
						mstrIntegResult = MFPAdapter.outputDatum(datumReturn)[1];
					} else	{
						mstrIntegResult = "";
					}
				}
				catch (Exception e)
				{
					mstrErrMsg = MFPAdapter.outputException(e);
					mHandler.post(new Runnable()	{

						@Override
						public void run() {
							/* use message to tell the main thread that I finish.
							 * Do not set mthreadCmd directly coz there might be some
							 * delayed output message which changes GUI after directly
							 * set mthreadCmd to null.
							 */
							dlgIntegProgress.dismiss();
							mthreadIntegrate = null;
							AlertDialog alertDialog;
							LayoutInflater inflater = LayoutInflater.from(ActivityIntegrate.this);
							View vErrMsg = inflater.inflate(R.layout.scroll_message, null);
							TextView txtErrMsg = (TextView) vErrMsg.findViewById(R.id.text_message);
							txtErrMsg.setText((mstrErrMsg == null)?"":mstrErrMsg);
							alertDialog = new AlertDialog.Builder(ActivityIntegrate.this)
										.setView(vErrMsg)
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
											}
											
										}).setTitle(getString(R.string.error))
										.setMessage(getString(R.string.integ_settings_wrong))
										.create();
							alertDialog.show();
						}
						
					});
					return;
				}
				mHandler.post(new Runnable()	{

					@Override
					public void run() {
						/* use message to tell the main thread that I finish.
						 * Do not set mthreadCmd directly coz there might be some
						 * delayed output message which changes GUI after directly
						 * set mthreadCmd to null.
						 */
						dlgIntegProgress.dismiss();
						mthreadIntegrate = null;
						// show result
						AlertDialog alertDialog;
						alertDialog = new AlertDialog.Builder(ActivityIntegrate.this)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
							}
							
						}).setTitle(getString(R.string.integrating_result))
						.setMessage(mstrIntegration + "\n= " + mstrIntegResult)
						.create();
						alertDialog.show();
					}
					
				});
				
			}
			
		});
		mthreadIntegrate.start();
	}
	
	/** Called when the activity is finally destroyed or in portrait-landscape switch. */
    @Override
    public void onDestroy()	{
    	if (isFinishing())	{
			if (mthreadIntegrate != null)	{
				if (mthreadIntegrate.isAlive())	{
					mthreadIntegrate.interrupt();
				}
				mthreadIntegrate = null;
			}
    	}
    	super.onDestroy();
    }
    
	@Override
	/*
	 * Create only one help menu.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, getString(R.string.menu_fill_example));
		menu.add(0, Menu.FIRST + 1, 0, getString(R.string.pop_up_system_soft_key));			
		menu.add(0, Menu.FIRST + 2, 0, getString(R.string.menu_help));
		return true;
	}

	//Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
		menu.add(0, Menu.FIRST, 0, getString(R.string.menu_fill_example));
		if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
			menu.add(0, Menu.FIRST + 1, 0, getString(R.string.hide_system_soft_key));			
		} else	{
			menu.add(0, Menu.FIRST + 1, 0, getString(R.string.pop_up_system_soft_key));			
		}
		menu.add(0, Menu.FIRST + 2, 0, getString(R.string.menu_help));
    	return super.onPrepareOptionsMenu(menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			EditText edtIntegExpr = (EditText) findViewById(R.id.integrated_expr_edit);
			edtIntegExpr.setText("sin(x)*y");
			
			EditText edtXName = (EditText) findViewById(R.id.x_variable_name_edit);
			edtXName.setText("x");
			EditText edtXFrom = (EditText) findViewById(R.id.x_variable_from_edit);
			edtXFrom.setText("3 + 4*i");
			EditText edtXTo = (EditText) findViewById(R.id.x_variable_to_edit);
			edtXTo.setText("13 - 32.33*i");
			EditText edtXNumberOfSteps = (EditText) findViewById(R.id.x_variable_number_of_steps_edit);
			edtXNumberOfSteps.setText("20");
			
			EditText edtYName = (EditText) findViewById(R.id.y_variable_name_edit);
			edtYName.setText("y");
			EditText edtYFrom = (EditText) findViewById(R.id.y_variable_from_edit);
			edtYFrom.setText("18");
			EditText edtYTo = (EditText) findViewById(R.id.y_variable_to_edit);
			edtYTo.setText("64");
			EditText edtYNumberOfSteps = (EditText) findViewById(R.id.y_variable_number_of_steps_edit);
			edtYNumberOfSteps.setText("15");
			
	        Spinner spinnerMultipleIntegral = (Spinner) findViewById(R.id.multiple_integral_spinner);
	        spinnerMultipleIntegral.setSelection(SELECT_2ND_LEVEL_INTEGRAL);	// level 2 integration
	        break;
		case Menu.FIRST + 1:
		   	if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
	            // code to hide the soft keyboard
	            setSoftKeyState(minputMethod.medtInput, ENABLE_HIDE_INPUTPAD);
		   	} else	{
		   		// code to enable the soft keyboard
		   		setSoftKeyState(minputMethod.medtInput, ENABLE_SHOW_SOFTKEY);
		   	}
			break;
		case Menu.FIRST + 2:
		   	Intent intentHelp = new Intent(this, ActivityShowHelp.class);
		   	Bundle bundle = new Bundle();
		   	bundle.putString("HELP_CONTENT", "integration");
		   	//Add this bundle to the intent
		   	intentHelp.putExtras(bundle);
		   	startActivity(intentHelp);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void adjustMargin()	{
		LinearLayout.LayoutParams llayoutParams  = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		final float fScale = getResources().getDisplayMetrics().density;
		int nScreenOrientation = getResources().getConfiguration().orientation;
		int nMargin = 0;
		if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
					|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
				nMargin = 4;
			} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
				nMargin = 8;
			} else	{
				// xlarge
				nMargin = 16;
			}
		} else	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
					|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
				nMargin = 4;
			} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
				nMargin = 12;
			} else	{
				// xlarge
				nMargin = 32;
			}
		}

		LinearLayout linearlayoutLevelInput = (LinearLayout)findViewById(R.id.integral_level_input);
		LinearLayout linearlayoutExprInput = (LinearLayout)findViewById(R.id.integrated_expr_input);
		LinearLayout linearlayoutDxInput =  (LinearLayout)findViewById(R.id.dx_input);
		LinearLayout linearlayoutDyInput =  (LinearLayout)findViewById(R.id.dy_input);
		LinearLayout linearlayoutDzInput =  (LinearLayout)findViewById(R.id.dz_input);
		LinearLayout linearlayoutCalculate =  (LinearLayout)findViewById(R.id.integral_calculate_layout);
		
		llayoutParams.setMargins((int)(nMargin * fScale + 0.5f),
								(int)(nMargin * fScale + 0.5f),
								(int)(nMargin * fScale + 0.5f),
								0);
		linearlayoutLevelInput.setLayoutParams(llayoutParams);
		linearlayoutExprInput.setLayoutParams(llayoutParams);
		linearlayoutDxInput.setLayoutParams(llayoutParams);
		linearlayoutDyInput.setLayoutParams(llayoutParams);
		linearlayoutDzInput.setLayoutParams(llayoutParams);

		llayoutParams.setMargins((int)(nMargin * fScale + 0.5f),
								(int)(nMargin * fScale + 0.5f),
								(int)(nMargin * fScale + 0.5f),
								(int)(nMargin * fScale + 0.5f));
		linearlayoutCalculate.setLayoutParams(llayoutParams);
		
	}
	
	@Override
	public Boolean isAsyncTaskRunning()	{
		return (mthreadIntegrate != null);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		adjustMargin();
	}
	
	/* Comment Adwo
	@Override
	public void onFailedToReceiveAd(AdwoAdView arg0, ErrorCode arg1) {
		Log.e("Adwo@ActivityIntegrate", "onFailedToReceiveAd");
	}

	@Override
	public void onReceiveAd(AdwoAdView arg0) {
		Log.e("Adwo@ActivityIntegrate", "onReceiveAd");
	}*/
	
}
