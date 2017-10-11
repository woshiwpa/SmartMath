package com.cyzapps.SmartMath;

import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jfcalc.FuncEvaluator.GraphPlotter;
import com.cyzapps.Jfcalc.PlotLib;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptInterrupter;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.AbstractExprInterrupter;
import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.adapter.MFPAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityPlotXYGraph extends ActivityImeMultiEdtsOri	{
	
	public static final int MAX_NUM_OF_CURVES = PlotLib.MAX_NUMBER_OF_2D_CURVES_TO_PLOT;
	
	public String mstrChartName = "a chart";
	public String mstrChartTitle = "chart1";
	public String mstrXTitle = "X";
	public String mstrYTitle = "Y";
	public String mstrChartBKColor = "black";	// this variable is not used yet
	public String mstrShowGrid = "true";
	public CurveSettings[] mlistCurveSettings = new CurveSettings[0];
	public LinearLayout mlayoutCurveListViewHolder;
	
	public static final String PLOT_FUNCTION_NAME = "plot_2d_curves";	// variable cannot be overridden, has to use function.
	public String getPlotFunctionName()	{return PLOT_FUNCTION_NAME;}
	public static final String CURVE_XT_PROMPT = "X(t) = ";
	public String getCurveXtPrompt()	{return CURVE_XT_PROMPT;}
	public static final String CURVE_YT_PROMPT = "Y(t) = ";
	public String getCurveYtPrompt()	{return CURVE_YT_PROMPT;}
	
	public String mstrPlotCmdLine = "";
	public String mstrErrMsg = "";
	public Thread mthreadPlotCmd = null;
    // Need handler for callbacks to the UI thread
    public Handler mHandler = new Handler();

	public static final String IMMUTABLE_INPUTPAD_CONFIG = "immutable_inputpad_integ_plot.cfg";

	public class CurveSettings	{
		public String mstrCurveTitle = "";
		public String mstrCurveColor = "white";
		public String mstrCurvePntColor = "white";	// not used here
		public String mstrCurvePntStyle = "point";
		public int mnCurvePntSize = 1;	// not used here
		public String mstrCurveLnColor = "white";	// not used here
		public String mstrCurveLnStyle = "solid";	// not used here
		public int mnCurveLnSize = 1;	// only 0 or 1 supported now
		public String mstrTFrom = "";
		public String mstrTTo = "";
		public String mstrTStep = "";
		public String mstrXExpr = "";
		public String mstrYExpr = "";
		
		public void copy(CurveSettings curveSettings)	{
			mstrCurveTitle = curveSettings.mstrCurveTitle;
			mstrCurveColor = curveSettings.mstrCurveColor;
			mstrCurvePntColor = curveSettings.mstrCurvePntColor;
			mstrCurvePntStyle = curveSettings.mstrCurvePntStyle;
			mnCurvePntSize = curveSettings.mnCurvePntSize;
			mstrCurveLnColor = curveSettings.mstrCurveLnColor;
			mstrCurveLnStyle = curveSettings.mstrCurveLnStyle;
			mnCurveLnSize = curveSettings.mnCurveLnSize;
			mstrTFrom = curveSettings.mstrTFrom;
			mstrTTo = curveSettings.mstrTTo;
			mstrTStep = curveSettings.mstrTStep;
			mstrXExpr = curveSettings.mstrXExpr;
			mstrYExpr = curveSettings.mstrYExpr;			
		}
	}
	
	public class CurveDefViewHolder {
		int mnPosition;
		EditText medtTitle;
		Spinner mspnrColor;
		Spinner mspnrPntStyle;
		CheckBox mchkboxShowLn;
		EditText medtTFrom;
		EditText medtTTo;
		EditText medtTStep;
		EditText medtXExpr;
		EditText medtYExpr;
		Button mbtnDelete;
		Button mbtnClear;
	}

    public class PlotGraphFunctionInterrupter extends FunctionInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public class PlotGraphScriptInterrupter extends ScriptInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public class PlotGraphAbstractExprInterrupter extends AbstractExprInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }

	public class PlotGraphPlotter extends GraphPlotter	{
		public PlotGraphPlotter()	{}
		public PlotGraphPlotter(Context context)	{
			mcontext = context;
		}
		
		public boolean mbOK = false;
		public Context mcontext = null;
		
		@Override
		public boolean plotGraph(String strGraphInfo) {
			Intent intent = new Intent(mcontext, ActivityChartDemon.class);
			intent.putExtra(ChartOperator.VMFPChart, strGraphInfo);
			mcontext.startActivity(intent);
			mbOK = true;
			return true;
		}
		
	}

	//add Escapes in ActivityPlotXYGraph works differently from same name function in ChartOperator.
	public static String addEscapes(String strInput)	{
		String strOutput = "";
		if (strInput != null)	{
			for (int i = 0; i < strInput.length(); i++)	{
				char cCurrent = strInput.charAt(i);
				if (cCurrent == '\"')	{
					strOutput += "\\\"";
				} else if (cCurrent == '\\')	{
					strOutput += "\\\\";
				} else	{
					strOutput += cCurrent;
				}
			}
		}
		return strOutput;
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
        
 		setTitle(getString(R.string.app_name) + ": " + getString(R.string.plot_2d_title));
        setContentView(R.layout.plot_xygraph);

		mlayoutCurveListViewHolder = (LinearLayout)findViewById(R.id.curve_def_list);
        EditText edtChartName = (EditText)findViewById(R.id.graph_name_edit);
        edtChartName.setText(getString(R.string.graph_name_hint));
        EditText edtChartTitle = (EditText)findViewById(R.id.graph_title_edit);
        edtChartTitle.setText(getString(R.string.graph_title_hint));
        EditText edtXTitle = (EditText)findViewById(R.id.graph_Xtitle_edit);
        edtXTitle.setText(getString(R.string.graph_Xtitle_hint));
        EditText edtYTitle = (EditText)findViewById(R.id.graph_Ytitle_edit);
        edtYTitle.setText(getString(R.string.graph_Ytitle_hint));
        
        Button btnAddCurve = (Button) findViewById(R.id.add_curve_btn);
        btnAddCurve.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View arg0) {
				actionAddNewCurve((Button)arg0);
			}
        	
        });
        if (mlistCurveSettings.length == 0)	{
        	setExample();	// show example.
        }
        
        Button btnClearAll = (Button) findViewById(R.id.clear_plot_settings_btn);
        btnClearAll.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				EditText edtChartName = (EditText) findViewById(R.id.graph_name_edit);
				EditText edtChartTitle = (EditText) findViewById(R.id.graph_title_edit);
				EditText edtXTitle = (EditText) findViewById(R.id.graph_Xtitle_edit);
				EditText edtYTitle = (EditText) findViewById(R.id.graph_Ytitle_edit);
				edtChartName.setText("");
				edtChartTitle.setText("");
				edtXTitle.setText("");
				edtYTitle.setText("");
				mlistCurveSettings = new CurveSettings[0];
				refreshCurveDefViewList();
				Button btnAddCurve = (Button) findViewById(R.id.add_curve_btn);
				if (btnAddCurve.isEnabled() == false)	{
					btnAddCurve.setEnabled(true);
				}
			}
        	
        });
        
        Button btnPlotGraph = (Button) findViewById(R.id.generate_chart_btn);
        btnPlotGraph.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				plotGraph();
			}
        	
        });
        
		//--- Show input method
        mlayoutBaseView = (LinearLayout)findViewById(R.id.plot_graph_base_view);
        mlayoutIMEHolder = (LinearLayout)findViewById(R.id.layoutICInputMethodHolder);
		EditText edtGraphName = (EditText)findViewById(R.id.graph_name_edit);
		mstrImmutableInputPadConfig = IMMUTABLE_INPUTPAD_CONFIG;
		mstrUsrDefInputPadConfig = ActivityCfgKeyPad.INPUTPAD_INTEG_PLOT_CONFIG;
		initInputMethod(edtGraphName);
    }

	public void actionAddNewCurve(Button btnAdd)	{
		if (mlistCurveSettings.length >= MAX_NUM_OF_CURVES)	{
			btnAdd.setEnabled(false);
		} else	{
			CurveSettings newCurveSettings = new CurveSettings();
			appendCurveDefView(newCurveSettings);
			if (mlistCurveSettings.length >= MAX_NUM_OF_CURVES)	{
				btnAdd.setEnabled(false);
			}
		}
	}
	
	public void appendCurveDefView(CurveSettings curveSettings)	{
		CurveSettings[] listCurveSettings = new CurveSettings[mlistCurveSettings.length + 1];
		for (int idx = 0; idx < mlistCurveSettings.length; idx ++)	{
			listCurveSettings[idx] = mlistCurveSettings[idx];
		}
		listCurveSettings[listCurveSettings.length - 1] = curveSettings;
		mlistCurveSettings = listCurveSettings;
		
		View newCurveDefView = genCurveDefView(listCurveSettings.length - 1);
		mlayoutCurveListViewHolder.addView(newCurveDefView);
	}
	
	public boolean deleteCurveDefView(int nPosition)	{
		if (nPosition >= mlistCurveSettings.length || nPosition < 0)	{
			return false;	// do not exist.
		}
		
		CurveSettings[] listCurveSettings
			= new CurveSettings[mlistCurveSettings.length - 1];
		for (int i = 0; i < mlistCurveSettings.length; i++)	{
			if (i < nPosition)	{
				listCurveSettings[i] = mlistCurveSettings[i];
			} else if (i > nPosition)	{
				listCurveSettings[i - 1] = mlistCurveSettings[i];
			}
		}
		mlistCurveSettings = listCurveSettings;
		
		int nIdxToBeDeleted = -1;
		for (int i = 0; i < mlayoutCurveListViewHolder.getChildCount(); i++)	{
			View vChild = mlayoutCurveListViewHolder.getChildAt(i);
			if (vChild.getTag() != null && vChild.getTag() instanceof CurveDefViewHolder)	{
				if (((CurveDefViewHolder)(vChild.getTag())).mnPosition == nPosition)	{
					nIdxToBeDeleted = i;
					View vFocusOnToBeDeleted = vChild.findFocus();
					if (vFocusOnToBeDeleted != null && vFocusOnToBeDeleted == minputMethod.medtInput)	{
						// ok, we have to change focus, otherwise input may crash
						EditText edtGraphName = (EditText)findViewById(R.id.graph_name_edit);
						edtGraphName.requestFocus();	// if not set new focus, the input from ime may crash.
					}
				} else if (((CurveDefViewHolder)(vChild.getTag())).mnPosition > nPosition)	{
					((CurveDefViewHolder)(vChild.getTag())).mnPosition --;
				}
			}
		}
		if (nIdxToBeDeleted >= 0)	{
			mlayoutCurveListViewHolder.removeViewAt(nIdxToBeDeleted);
		}
		return true;
	}
	
	public boolean changeCurveDefView(int nPosition, CurveSettings newCurveSettings)	{
		if (nPosition >= mlistCurveSettings.length || nPosition < 0)	{
			return false;	// do not exist.
		}
		
		mlistCurveSettings[nPosition].copy(newCurveSettings);
		for (int i = 0; i < mlayoutCurveListViewHolder.getChildCount(); i++)	{
			View vChild = mlayoutCurveListViewHolder.getChildAt(i);
			if (vChild.getTag() != null && vChild.getTag() instanceof CurveDefViewHolder)	{
				if (((CurveDefViewHolder)(vChild.getTag())).mnPosition == nPosition)	{
					CurveDefViewHolder holder = (CurveDefViewHolder)(vChild.getTag());
					setCurveDefViewHolder(holder, mlistCurveSettings[nPosition]);
				}
			}
		}
		return true;
	}
	
	public void setCurveDefViewHolder(CurveDefViewHolder holder, CurveSettings curveSettings)	{
		holder.medtTitle.setText(curveSettings.mstrCurveTitle);
		String strCurveColor = curveSettings.mstrCurvePntColor;
		// first check point color, then line color then curve color
		if (strCurveColor.trim().equals(""))	{
			strCurveColor = curveSettings.mstrCurveLnColor;
			if (strCurveColor.trim().equals(""))	{
				strCurveColor = curveSettings.mstrCurveColor;
			}
		}
		if (strCurveColor.trim().toLowerCase(Locale.US).equals("blue"))	{
			holder.mspnrColor.setSelection(1);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("cyan"))	{
			holder.mspnrColor.setSelection(2);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("dkgray"))	{
			holder.mspnrColor.setSelection(3);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("gray"))	{
			holder.mspnrColor.setSelection(4);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("green"))	{
			holder.mspnrColor.setSelection(5);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("ltgray"))	{
			holder.mspnrColor.setSelection(6);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("magenta"))	{
			holder.mspnrColor.setSelection(7);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("red"))	{
			holder.mspnrColor.setSelection(8);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("transparent"))	{
			holder.mspnrColor.setSelection(9);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("white"))	{
			holder.mspnrColor.setSelection(10);
		} else if (strCurveColor.trim().toLowerCase(Locale.US).equals("yellow"))	{
			holder.mspnrColor.setSelection(11);
		} else	{
			holder.mspnrColor.setSelection(0);
		}
		
		if (curveSettings.mstrCurvePntStyle.trim().toLowerCase(Locale.US).equals("circle"))	{
			holder.mspnrPntStyle.setSelection(0);
		} else if (curveSettings.mstrCurvePntStyle.trim().toLowerCase(Locale.US).equals("triangle"))	{
			holder.mspnrPntStyle.setSelection(1);
		} else if (curveSettings.mstrCurvePntStyle.trim().toLowerCase(Locale.US).equals("square"))	{
			holder.mspnrPntStyle.setSelection(2);
		} else if (curveSettings.mstrCurvePntStyle.trim().toLowerCase(Locale.US).equals("diamond"))	{
			holder.mspnrPntStyle.setSelection(3);
		} else if (curveSettings.mstrCurvePntStyle.trim().toLowerCase(Locale.US).equals("x"))	{
			holder.mspnrPntStyle.setSelection(4);
		} else	{	// point
			holder.mspnrPntStyle.setSelection(5);
		}
		
		holder.mchkboxShowLn.setChecked((curveSettings.mnCurveLnSize == 0)?false:true);
		
		holder.medtTFrom.setText(curveSettings.mstrTFrom);
		holder.medtTTo.setText(curveSettings.mstrTTo);
		holder.medtTStep.setText((curveSettings.mstrTStep == null)?"":curveSettings.mstrTStep.trim());
		holder.medtXExpr.setText(curveSettings.mstrXExpr);				
		holder.medtYExpr.setText(curveSettings.mstrYExpr);						
	}
	
	public void refreshCurveDefViewList()	{
		mlayoutCurveListViewHolder.removeAllViews();
		for (int idx = 0; idx < mlistCurveSettings.length; idx ++)	{
			View newCurveDefView = genCurveDefView(idx);
			mlayoutCurveListViewHolder.addView(newCurveDefView);
		}
		EditText edtGraphName = (EditText)findViewById(R.id.graph_name_edit);
		edtGraphName.requestFocus();	// if not set new focus, the input from ime may crash.
	}
	
	public View genCurveDefView(int position) {
		
		LayoutInflater inflater = LayoutInflater.from(this);
		View convertView = inflater.inflate(R.layout.xycurve_def, null);
		
		TextView tvXtPrompt = (TextView) convertView.findViewById(R.id.x_t_prompt);
		tvXtPrompt.setText(getCurveXtPrompt());
		TextView tvYtPrompt = (TextView) convertView.findViewById(R.id.y_t_prompt);
		tvYtPrompt.setText(getCurveYtPrompt());
	 
		/*
		 *  Creates a ViewHolder and store references to the children views
		 *  which we want to bind data to. 
		 */
		final CurveDefViewHolder holder = new CurveDefViewHolder();
		holder.mnPosition = position;
		holder.medtTitle = (EditText)convertView.findViewById(R.id.curve_name_edit);
		holder.mspnrColor = (Spinner)convertView.findViewById(R.id.curve_color_spinner);
		holder.mspnrPntStyle = (Spinner)convertView.findViewById(R.id.curve_point_style_spinner);
		holder.mchkboxShowLn = (CheckBox)convertView.findViewById(R.id.curve_show_line_chkbox);
		holder.medtTFrom = (EditText)convertView.findViewById(R.id.t_from_edit);
		holder.medtTTo = (EditText)convertView.findViewById(R.id.t_to_edit);
		holder.medtTStep = (EditText)convertView.findViewById(R.id.t_step_edit);
		holder.medtXExpr = (EditText)convertView.findViewById(R.id.X_expression_edit);
		holder.medtYExpr = (EditText)convertView.findViewById(R.id.Y_expression_edit);
		holder.mbtnDelete = (Button)convertView.findViewById(R.id.delete_curve_btn);
		holder.mbtnClear = (Button)convertView.findViewById(R.id.clear_curve_btn);

		holder.medtTitle.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mlistCurveSettings[holder.mnPosition].mstrCurveTitle = s.toString();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		setImeForEditor(holder.medtTitle);
		
		holder.mspnrColor.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String strSelection = arg0.getItemAtPosition(arg2).toString();
				String strColor = "white";
				if (strSelection.equalsIgnoreCase(getResources().getString(R.string.black_color)))	{
					strColor = "black";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.blue_color)))	{
					strColor = "blue";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.cyan_color)))	{
					strColor = "cyan";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.dkgray_color)))	{
					strColor = "dkgray";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.gray_color)))	{
					strColor = "gray";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.green_color)))	{
					strColor = "green";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.ltgray_color)))	{
					strColor = "ltgray";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.magenta_color)))	{
					strColor = "magenta";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.red_color)))	{
					strColor = "red";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.transparent_color)))	{
					strColor = "transparent";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.white_color)))	{
					strColor = "white";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.yellow_color)))	{
					strColor = "yellow";
				}
				// doesn't support pnt color and line color now so assign them to the same value as curve color.
				mlistCurveSettings[holder.mnPosition].mstrCurveColor
					= mlistCurveSettings[holder.mnPosition].mstrCurvePntColor
					= mlistCurveSettings[holder.mnPosition].mstrCurveLnColor
					= strColor;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		holder.mspnrPntStyle.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String strSelection = arg0.getItemAtPosition(arg2).toString();
				String strPntStyle = "point";
				if (strSelection.equalsIgnoreCase(getResources().getString(R.string.circle_point_style)))	{
					strPntStyle = "circle";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.triangle_point_style)))	{
					strPntStyle = "triangle";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.square_point_style)))	{
					strPntStyle = "square";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.diamond_point_style)))	{
					strPntStyle = "diamond";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.x_point_style)))	{
					strPntStyle = "x";
				} else if (strSelection.equalsIgnoreCase(getResources().getString(R.string.point_point_style)))	{
					strPntStyle = "point";
				}
				mlistCurveSettings[holder.mnPosition].mstrCurvePntStyle = strPntStyle;
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		holder.mchkboxShowLn.setOnCheckedChangeListener(new OnCheckedChangeListener()	{

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mlistCurveSettings[holder.mnPosition].mnCurveLnSize = isChecked?1:0;
			}
			
		});
		
		holder.medtTFrom.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().trim().length() == 0)	{
					mlistCurveSettings[holder.mnPosition].mstrTFrom = "0";
				} else	{
					mlistCurveSettings[holder.mnPosition].mstrTFrom = s.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		setImeForEditor(holder.medtTFrom);
		
		holder.medtTTo.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().trim().length() == 0)	{
					mlistCurveSettings[holder.mnPosition].mstrTTo = "0";
				} else	{
					mlistCurveSettings[holder.mnPosition].mstrTTo = s.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		setImeForEditor(holder.medtTTo);
		
		holder.medtTStep.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().trim().length() == 0)	{
					mlistCurveSettings[holder.mnPosition].mstrTStep = "0";	// means auto-step
				} else	{
					mlistCurveSettings[holder.mnPosition].mstrTStep = s.toString();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		setImeForEditor(holder.medtTStep);
		
		holder.medtXExpr.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mlistCurveSettings[holder.mnPosition].mstrXExpr = s.toString();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		setImeForEditor(holder.medtXExpr);
		
		holder.medtYExpr.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				mlistCurveSettings[holder.mnPosition].mstrYExpr = s.toString();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start,
					int count, int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start,
					int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
		});
		setImeForEditor(holder.medtYExpr);
		
		holder.mbtnDelete.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				deleteCurveDefView(holder.mnPosition);
				Button btnAddCurve = (Button)findViewById(R.id.add_curve_btn);
				if (btnAddCurve.isEnabled() == false)	{
					btnAddCurve.setEnabled(true);
				}
			}
			
		});
		
		holder.mbtnClear.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				mlistCurveSettings[holder.mnPosition].mstrCurveTitle = "";
				mlistCurveSettings[holder.mnPosition].mstrCurveColor = "white";
				mlistCurveSettings[holder.mnPosition].mstrTFrom = "0";
				mlistCurveSettings[holder.mnPosition].mstrTTo = "0";
				mlistCurveSettings[holder.mnPosition].mstrTStep = "0";
				mlistCurveSettings[holder.mnPosition].mstrXExpr = "";
				mlistCurveSettings[holder.mnPosition].mstrYExpr = "";
				changeCurveDefView(holder.mnPosition, mlistCurveSettings[holder.mnPosition]);
				}
				
			});
			
		holder.mspnrColor.setSelection(0);
		holder.mspnrPntStyle.setSelection(0);
		holder.mchkboxShowLn.setChecked(true);
		
		convertView.setTag(holder);
 
		// synchronize the values in the with with the values in the list.
		setCurveDefViewHolder(holder, mlistCurveSettings[position]);				

		return convertView;
	}
	
	public void plotGraph()	{
		EditText edtChartName = (EditText) findViewById(R.id.graph_name_edit);
		EditText edtChartTitle = (EditText) findViewById(R.id.graph_title_edit);
		EditText edtXTitle = (EditText) findViewById(R.id.graph_Xtitle_edit);
		EditText edtYTitle = (EditText) findViewById(R.id.graph_Ytitle_edit);
		CheckBox chkboxShowGrid = (CheckBox) findViewById(R.id.graph_show_grid_chkbox);
		mstrChartName = edtChartName.getText().toString();
		mstrChartTitle = edtChartTitle.getText().toString();
		mstrXTitle = edtXTitle.getText().toString();
		mstrYTitle = edtYTitle.getText().toString();
		mstrChartBKColor = "black";	// this variable is not used yet.
		mstrShowGrid = chkboxShowGrid.isChecked()?"true":"false";
		mstrPlotCmdLine = getPlotFunctionName() +"(\"" + addEscapes(mstrChartName).trim() + "\",\""
						+ addEscapes(mstrChartTitle).trim() + "\",\""
						+ addEscapes(mstrXTitle).trim() + "\",\""
						+ addEscapes(mstrYTitle).trim() + "\",\""
						+ addEscapes(mstrChartBKColor).trim() + "\",\""
						+ addEscapes(mstrShowGrid).trim() + "\"";
		
		int nTotalNumberOfPnts = 0;
		for (int i = 0; i < mlistCurveSettings.length; i++)	{
			String strStep = mlistCurveSettings[i].mstrTStep;
			if (strStep == null || strStep.trim().length() == 0) {	// auto step, 100 points
				strStep = "0";	//(mlistCurveSettings[i].mdblTTo - mlistCurveSettings[i].mdblTFrom) / 100;
			}
			mstrPlotCmdLine += ",\"" + addEscapes(mlistCurveSettings[i].mstrCurveTitle).trim()
							+  "\",\"" + addEscapes(mlistCurveSettings[i].mstrCurvePntColor).trim()
							+  "\",\"" + addEscapes(mlistCurveSettings[i].mstrCurvePntStyle).trim()
							+  "\"," + mlistCurveSettings[i].mnCurvePntSize
							+  ",\"" + addEscapes(mlistCurveSettings[i].mstrCurveLnColor).trim()
							+  "\",\"" + addEscapes(mlistCurveSettings[i].mstrCurveLnStyle).trim()
							+  "\"," + mlistCurveSettings[i].mnCurveLnSize
							+  ",\"t\"," + mlistCurveSettings[i].mstrTFrom
							+  "," + mlistCurveSettings[i].mstrTTo
							+  "," + strStep
							+  ",\"" + addEscapes(mlistCurveSettings[i].mstrXExpr).trim()
							+  "\",\"" + addEscapes(mlistCurveSettings[i].mstrYExpr).trim() + "\"";
			int nNumOfPnts = 256;	// if nstep == 0, num of points is 256
			try {
				double dTFrom = Double.parseDouble(mlistCurveSettings[i].mstrTFrom);
				double dTTo = Double.parseDouble(mlistCurveSettings[i].mstrTTo);
				double dTStep = Double.parseDouble(mlistCurveSettings[i].mstrTStep);
				if (dTStep != 0)	{
					nNumOfPnts = (int) Math.ceil((dTFrom - dTTo)/dTStep);					
				}
			} catch(Exception e)	{
			} finally	{
			}
			nTotalNumberOfPnts += (nNumOfPnts >= 0)?nNumOfPnts:0;
		}
		mstrPlotCmdLine += ")";
		
		final ProgressDialog dlgPlotProgress = ProgressDialog.show(this, getString(R.string.please_wait),
																getString(R.string.calculating_chart_data), true);
		mthreadPlotCmd = new Thread(new Runnable(){

			@Override
			public void run() {
				/* evaluate the expression */
				ExprEvaluator exprEvaluator = new ExprEvaluator();
				// clear variable namespaces
				exprEvaluator.m_lVarNameSpaces = new LinkedList<LinkedList<Variable>>();

				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				// should be interrupted, should save file and plot graphs. No log output.
				FuncEvaluator.msfunctionInterrupter = new PlotGraphFunctionInterrupter();
				ScriptAnalyzer.msscriptInterrupter = new PlotGraphScriptInterrupter();
				AbstractExpr.msaexprInterrupter = new PlotGraphAbstractExprInterrupter();
				FuncEvaluator.msstreamConsoleInput = null;
				FuncEvaluator.msstreamLogOutput = null;
				FuncEvaluator.msfileOperator = new MFPFileManagerActivity.MFPFileOperator();
				FuncEvaluator.msgraphPlotter = new PlotGraphPlotter(ActivityPlotXYGraph.this);
				FuncEvaluator.msgraphPlotter3D = null;
                if (FuncEvaluator.mspm == null) {
                    FuncEvaluator.mspm = new PatternManager();	// even if plot we still need mspm because we may calculate integrate.
                    try {
						FuncEvaluator.mspm.loadPatterns(2);	// load pattern is a very time consuming work. So only do this if needed.
					} catch (Exception e) {
						 // load all integration patterns. Assume load patterns will not throw any exceptions.
					}
                 }
				
				try	{
					exprEvaluator.evaluateExpression(mstrPlotCmdLine, curpos);
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
							dlgPlotProgress.dismiss();
							mthreadPlotCmd = null;
							AlertDialog alertDialog;
							if (((PlotGraphPlotter)(FuncEvaluator.msgraphPlotter)).mbOK)	{
								// graph is plotted without problem, but we cannot save the file.
								Context context = getApplicationContext();
								CharSequence text = getString(R.string.graph_file_cannot_be_saved);
								int duration = Toast.LENGTH_SHORT;
								Toast toast = Toast.makeText(context, text, duration);
								toast.show();								
							} else	{
								LayoutInflater inflater = LayoutInflater.from(ActivityPlotXYGraph.this);
								View vErrMsg = inflater.inflate(R.layout.scroll_message, null);
								TextView txtErrMsg = (TextView) vErrMsg.findViewById(R.id.text_message);
								txtErrMsg.setText((mstrErrMsg == null)?"":mstrErrMsg);
								alertDialog = new AlertDialog.Builder(ActivityPlotXYGraph.this)
											.setView(vErrMsg)
											.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
												}
												
											}).setTitle(getString(R.string.error))
											.setMessage(getString(R.string.graph_settings_wrong))
											.create();
								alertDialog.show();
							}
						}
						
					});
					return;
				}
				mHandler.post(new Runnable()	{

					@Override
					public void run() {
						/* use message to tell the main thread that I finish.
						 */
						dlgPlotProgress.dismiss();
						mthreadPlotCmd = null;
					}
					
				});
			}
			
		});
		
		if (nTotalNumberOfPnts > 1024)	{	// too many points
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(this)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								// continue to plot graph
								mthreadPlotCmd.start();
							}
							
						}).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
								// cancel.
								dlgPlotProgress.dismiss();
								mthreadPlotCmd = null;
							}
							
						}).setTitle(getString(R.string.warning))
						.setMessage(getString(R.string.too_many_points_in_graph))
						.setCancelable(false)
						.create();
			alertDialog.show();			
		} else	{
			mthreadPlotCmd.start();
		}
	}
	
	/** Called when the activity is finally destroyed or in portrait-landscape switch. */
    @Override
    public void onDestroy()	{
    	if (isFinishing())	{
			if (mthreadPlotCmd != null)	{
				if (mthreadPlotCmd.isAlive())	{
					mthreadPlotCmd.interrupt();
				}
				mthreadPlotCmd = null;
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
			// initial a one-curve example
	        EditText edtChartName = (EditText)findViewById(R.id.graph_name_edit);
	        edtChartName.setText(getString(R.string.graph_name_hint));
	        EditText edtChartTitle = (EditText)findViewById(R.id.graph_title_edit);
	        edtChartTitle.setText(getString(R.string.graph_title_hint));
	        EditText edtXTitle = (EditText)findViewById(R.id.graph_Xtitle_edit);
	        edtXTitle.setText(getString(R.string.graph_Xtitle_hint));
	        EditText edtYTitle = (EditText)findViewById(R.id.graph_Ytitle_edit);
	        edtYTitle.setText(getString(R.string.graph_Ytitle_hint));
	        setExample();
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
		   	bundle.putString("HELP_CONTENT", "plot_graph");
		   	//Add this bundle to the intent
		   	intentHelp.putExtras(bundle);
		   	startActivity(intentHelp);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Boolean isAsyncTaskRunning()	{
		return (mthreadPlotCmd != null);
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
		mlistCurveSettings = new CurveSettings[nNumofCurves];
		switch (nNumofCurves)	{
		case 3:
	        mlistCurveSettings[2] = new CurveSettings();
			mlistCurveSettings[2].mstrCurveTitle = getString(R.string.curve_title_hint2);
			mlistCurveSettings[2].mstrCurveColor = "green";
			mlistCurveSettings[2].mstrCurvePntColor = "green";
			mlistCurveSettings[2].mstrCurvePntStyle = "diamond";
			mlistCurveSettings[2].mnCurvePntSize = 1;
			mlistCurveSettings[2].mstrCurveLnColor = "green";
			mlistCurveSettings[2].mstrCurveLnStyle = "solid";
			mlistCurveSettings[2].mnCurveLnSize = 1;
			mlistCurveSettings[2].mstrTFrom = "4";
			mlistCurveSettings[2].mstrTTo = "-6";
			mlistCurveSettings[2].mstrTStep = "-0.1";
			mlistCurveSettings[2].mstrXExpr = "t**2/5";
			mlistCurveSettings[2].mstrYExpr = "log2(abs(t)+2)";
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
			mlistCurveSettings[1].mstrTFrom = "-5";
			mlistCurveSettings[1].mstrTTo = "5";
			mlistCurveSettings[1].mstrTStep = "0.1";	//"0";	// auto step.
			mlistCurveSettings[1].mstrXExpr = "2*(" + getString(R.string.x_t_hint) + ")+3";
			mlistCurveSettings[1].mstrYExpr = "32*(" + getString(R.string.y_t_hint) + ")";	// make the shape close to x:y == 1:1
		case 1:
	        mlistCurveSettings[0] = new CurveSettings();
			mlistCurveSettings[0].mstrCurveTitle = getString(R.string.curve_title_hint1);
			mlistCurveSettings[0].mstrCurveColor = "red";
			mlistCurveSettings[0].mstrCurvePntColor = "red";
			mlistCurveSettings[0].mstrCurvePntStyle = "point";
			mlistCurveSettings[0].mnCurvePntSize = 1;
			mlistCurveSettings[0].mstrCurveLnColor = "red";
			mlistCurveSettings[0].mstrCurveLnStyle = "solid";
			mlistCurveSettings[0].mnCurveLnSize = 1;
			mlistCurveSettings[0].mstrTFrom = "0";
			mlistCurveSettings[0].mstrTTo = "6";
			mlistCurveSettings[0].mstrTStep = " ";	//Auto Step;
			mlistCurveSettings[0].mstrXExpr = "t";	//"10/(t**2 - 10 * t + 10)";
			mlistCurveSettings[0].mstrYExpr = "tan(t)";
		default:	// 0
		}
		refreshCurveDefViewList();
	}
}
