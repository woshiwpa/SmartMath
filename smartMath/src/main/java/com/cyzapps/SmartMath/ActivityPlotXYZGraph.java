package com.cyzapps.SmartMath;

import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.IOLib;
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
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.adapter.MFPAdapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityPlotXYZGraph extends ActivityImeMultiEdtsOri	{
	
	public static final int MAX_NUM_OF_CURVES = PlotLib.MAX_NUMBER_OF_3D_SURFACES_TO_PLOT;
	
	public String mstrChartName = "a 3dchart";
	public String mstrChartTitle = "chart2";
	public String mstrXTitle = "X";
	public String mstrYTitle = "Y";
	public String mstrZTitle = "Z";
	public SurfaceSettings[] mlistSurfaceSettings = new SurfaceSettings[0];
	public LinearLayout mlayoutSurfaceListViewHolder;
	
	public String mstrPlotCmdLine = "";
	public String mstrErrMsg = "";
	public Thread mthreadPlotCmd = null;
    // Need handler for callbacks to the UI thread
    public Handler mHandler = new Handler();

	public static final String IMMUTABLE_INPUTPAD_CONFIG = "immutable_inputpad_integ_plot.cfg";

	public class SurfaceSettings	{
		public String mstrCurveTitle = "";
		Boolean mbIsGrid = false;
		String mstrMinColor = "white";
		String mstrMinColor1 = "white";
        Double mdMinColorValue = null;  // null means min z is corresponding to min color
		String mstrMaxColor = "white";
		String mstrMaxColor1 = "white";
        Double mdMaxColorValue = null;  // null means max z is corresponding to max color
		public String mstrUFrom = "0";
		public String mstrUTo = "0";
		public String mstrUStep = "0";
		public String mstrVFrom = "0";
		public String mstrVTo = "0";
		public String mstrVStep = "0";
		public String mstrXExpr = "";
		public String mstrYExpr = "";
		public String mstrZExpr = "";
		public void copy(SurfaceSettings surfaceSettings)	{
			mstrCurveTitle = surfaceSettings.mstrCurveTitle;
			mbIsGrid = surfaceSettings.mbIsGrid;
			mstrMinColor = surfaceSettings.mstrMinColor;
			mstrMinColor1 = surfaceSettings.mstrMinColor1;
			mdMinColorValue = surfaceSettings.mdMinColorValue;
			mstrMaxColor = surfaceSettings.mstrMaxColor;
			mstrMaxColor1 = surfaceSettings.mstrMaxColor1;
			mdMaxColorValue = surfaceSettings.mdMaxColorValue;			
			mstrUFrom = surfaceSettings.mstrUFrom;
			mstrUTo = surfaceSettings.mstrUTo;
			mstrUStep = surfaceSettings.mstrUStep;
			mstrVFrom = surfaceSettings.mstrVFrom;
			mstrVTo = surfaceSettings.mstrVTo;
			mstrVStep = surfaceSettings.mstrVStep;
			mstrXExpr = surfaceSettings.mstrXExpr;
			mstrYExpr = surfaceSettings.mstrYExpr;
			mstrZExpr = surfaceSettings.mstrZExpr;
		}
	}
	
	static class SurfaceDefViewHolder {
		int mnPosition;
		EditText medtTitle;
		CheckBox mchkIsGrid;
		Spinner mspnrMaxColor;
		Spinner mspnrMaxColor1;
		EditText medtMaxColorValue;
		Spinner mspnrMinColor;
		Spinner mspnrMinColor1;
		EditText medtMinColorValue;
		EditText medtUFrom;
		EditText medtUTo;
		EditText medtUStep;
		EditText medtVFrom;
		EditText medtVTo;
		EditText medtVStep;
		EditText medtXExpr;
		EditText medtYExpr;
		EditText medtZExpr;
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

	//add Escapes in ActivityPlotXYGraph works differently from same name function in ChartViewer.
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
        
        setTitle(getString(R.string.app_name) + ": " + getString(R.string.plot_3d_title));
        setContentView(R.layout.plot_xyzgraph);

        mlayoutSurfaceListViewHolder = (LinearLayout)findViewById(R.id.curve_def_list);
        EditText edtChartName = (EditText)findViewById(R.id.graph_name_edit);
        edtChartName.setText(getString(R.string.graph_name_hint));
        EditText edtChartTitle = (EditText)findViewById(R.id.graph_title_edit);
        edtChartTitle.setText(getString(R.string.graph_title_hint));
        EditText edtXTitle = (EditText)findViewById(R.id.graph_Xtitle_edit);
        edtXTitle.setText(getString(R.string.graph_Xtitle_hint));
        EditText edtYTitle = (EditText)findViewById(R.id.graph_Ytitle_edit);
        edtYTitle.setText(getString(R.string.graph_Ytitle_hint));
        EditText edtZTitle = (EditText)findViewById(R.id.graph_Ztitle_edit);
        edtZTitle.setText(getString(R.string.graph_Ztitle_hint));
        
        Button btnAddCurve = (Button) findViewById(R.id.add_curve_btn);
        btnAddCurve.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View arg0) {
				actionAddNewCurve((Button)arg0);
			}
        	
        });
        if (mlistSurfaceSettings.length == 0)	{
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
				EditText edtZTitle = (EditText) findViewById(R.id.graph_Ztitle_edit);
				edtChartName.setText("");
				edtChartTitle.setText("");
				edtXTitle.setText("");
				edtYTitle.setText("");
				edtZTitle.setText("");
				mlistSurfaceSettings = new SurfaceSettings[0];
				refreshSurfaceDefViewList();
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
		if (mlistSurfaceSettings.length >= MAX_NUM_OF_CURVES)	{
			btnAdd.setEnabled(false);
		} else	{
			SurfaceSettings newSurfaceSettings = new SurfaceSettings();
			appendSurfaceDefView(newSurfaceSettings);
			if (mlistSurfaceSettings.length >= MAX_NUM_OF_CURVES)	{
				btnAdd.setEnabled(false);
			}
		}
	}
	
	public void appendSurfaceDefView(SurfaceSettings surfaceSettings)	{
		SurfaceSettings[] listSurfaceSettings = new SurfaceSettings[mlistSurfaceSettings.length + 1];
		for (int idx = 0; idx < mlistSurfaceSettings.length; idx ++)	{
			listSurfaceSettings[idx] = mlistSurfaceSettings[idx];
		}
		listSurfaceSettings[listSurfaceSettings.length - 1] = surfaceSettings;
		mlistSurfaceSettings = listSurfaceSettings;
		
		View newSurfaceDefView = genSurfaceDefView(listSurfaceSettings.length - 1);
		mlayoutSurfaceListViewHolder.addView(newSurfaceDefView);
	}
	
	public boolean deleteSurfaceDefView(int nPosition)	{
		if (nPosition >= mlistSurfaceSettings.length || nPosition < 0)	{
			return false;	// do not exist.
		}
		
		SurfaceSettings[] listSurfaceSettings
			= new SurfaceSettings[mlistSurfaceSettings.length - 1];
		for (int i = 0; i < mlistSurfaceSettings.length; i++)	{
			if (i < nPosition)	{
				listSurfaceSettings[i] = mlistSurfaceSettings[i];
			} else if (i > nPosition)	{
				listSurfaceSettings[i - 1] = mlistSurfaceSettings[i];
			}
		}
		mlistSurfaceSettings = listSurfaceSettings;
		
		int nIdxToBeDeleted = -1;
		for (int i = 0; i < mlayoutSurfaceListViewHolder.getChildCount(); i++)	{
			View vChild = mlayoutSurfaceListViewHolder.getChildAt(i);
			if (vChild.getTag() != null && vChild.getTag() instanceof SurfaceDefViewHolder)	{
				if (((SurfaceDefViewHolder)(vChild.getTag())).mnPosition == nPosition)	{
					nIdxToBeDeleted = i;
					View vFocusOnToBeDeleted = vChild.findFocus();
					if (vFocusOnToBeDeleted != null && vFocusOnToBeDeleted == minputMethod.medtInput)	{
						// ok, we have to change focus, otherwise input may crash
						EditText edtGraphName = (EditText)findViewById(R.id.graph_name_edit);
						edtGraphName.requestFocus();	// if not set new focus, the input from ime may crash.
					}
				} else if (((SurfaceDefViewHolder)(vChild.getTag())).mnPosition > nPosition)	{
					((SurfaceDefViewHolder)(vChild.getTag())).mnPosition --;
				}
			}
		}
		if (nIdxToBeDeleted >= 0)	{
			mlayoutSurfaceListViewHolder.removeViewAt(nIdxToBeDeleted);
		}
		return true;
	}
	
	public boolean changeSurfaceDefView(int nPosition, SurfaceSettings newSurfaceSettings)	{
		if (nPosition >= mlistSurfaceSettings.length || nPosition < 0)	{
			return false;	// do not exist.
		}
		
		mlistSurfaceSettings[nPosition].copy(newSurfaceSettings);
		for (int i = 0; i < mlayoutSurfaceListViewHolder.getChildCount(); i++)	{
			View vChild = mlayoutSurfaceListViewHolder.getChildAt(i);
			if (vChild.getTag() != null && vChild.getTag() instanceof SurfaceDefViewHolder)	{
				if (((SurfaceDefViewHolder)(vChild.getTag())).mnPosition == nPosition)	{
					SurfaceDefViewHolder holder = (SurfaceDefViewHolder)(vChild.getTag());
					setSurfaceDefViewHolder(holder, mlistSurfaceSettings[nPosition]);
				}
			}
		}
		return true;
	}
	
	public void setSurfaceDefViewHolder(SurfaceDefViewHolder holder, SurfaceSettings surfaceSettings)	{
		holder.medtTitle.setText(surfaceSettings.mstrCurveTitle);
		
		holder.mchkIsGrid.setChecked(surfaceSettings.mbIsGrid);
		
		String strMaxColor = surfaceSettings.mstrMaxColor;
		if (strMaxColor.trim().toLowerCase(Locale.US).equals("blue"))	{
			holder.mspnrMaxColor.setSelection(1);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("cyan"))	{
			holder.mspnrMaxColor.setSelection(2);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("dkgray"))	{
			holder.mspnrMaxColor.setSelection(3);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("gray"))	{
			holder.mspnrMaxColor.setSelection(4);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("green"))	{
			holder.mspnrMaxColor.setSelection(5);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("ltgray"))	{
			holder.mspnrMaxColor.setSelection(6);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("magenta"))	{
			holder.mspnrMaxColor.setSelection(7);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("red"))	{
			holder.mspnrMaxColor.setSelection(8);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("transparent"))	{
			holder.mspnrMaxColor.setSelection(9);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("white"))	{
			holder.mspnrMaxColor.setSelection(10);
		} else if (strMaxColor.trim().toLowerCase(Locale.US).equals("yellow"))	{
			holder.mspnrMaxColor.setSelection(11);
		} else	{
			holder.mspnrMaxColor.setSelection(0);
		}
		String strMaxColor1 = surfaceSettings.mstrMaxColor1;
		if (strMaxColor1.trim().toLowerCase(Locale.US).equals("blue"))	{
			holder.mspnrMaxColor1.setSelection(1);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("cyan"))	{
			holder.mspnrMaxColor1.setSelection(2);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("dkgray"))	{
			holder.mspnrMaxColor1.setSelection(3);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("gray"))	{
			holder.mspnrMaxColor1.setSelection(4);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("green"))	{
			holder.mspnrMaxColor1.setSelection(5);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("ltgray"))	{
			holder.mspnrMaxColor1.setSelection(6);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("magenta"))	{
			holder.mspnrMaxColor1.setSelection(7);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("red"))	{
			holder.mspnrMaxColor1.setSelection(8);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("transparent"))	{
			holder.mspnrMaxColor1.setSelection(9);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("white"))	{
			holder.mspnrMaxColor1.setSelection(10);
		} else if (strMaxColor1.trim().toLowerCase(Locale.US).equals("yellow"))	{
			holder.mspnrMaxColor1.setSelection(11);
		} else	{
			holder.mspnrMaxColor1.setSelection(0);
		}
		Double dMaxColorValue = surfaceSettings.mdMaxColorValue;
		if (dMaxColorValue == null)	{
			holder.medtMaxColorValue.setText("");
		} else	{
			holder.medtMaxColorValue.setText(dMaxColorValue.toString());
		}
		String strMinColor = surfaceSettings.mstrMinColor;
		if (strMinColor.trim().toLowerCase(Locale.US).equals("blue"))	{
			holder.mspnrMinColor.setSelection(1);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("cyan"))	{
			holder.mspnrMinColor.setSelection(2);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("dkgray"))	{
			holder.mspnrMinColor.setSelection(3);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("gray"))	{
			holder.mspnrMinColor.setSelection(4);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("green"))	{
			holder.mspnrMinColor.setSelection(5);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("ltgray"))	{
			holder.mspnrMinColor.setSelection(6);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("magenta"))	{
			holder.mspnrMinColor.setSelection(7);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("red"))	{
			holder.mspnrMinColor.setSelection(8);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("transparent"))	{
			holder.mspnrMinColor.setSelection(9);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("white"))	{
			holder.mspnrMinColor.setSelection(10);
		} else if (strMinColor.trim().toLowerCase(Locale.US).equals("yellow"))	{
			holder.mspnrMinColor.setSelection(11);
		} else	{
			holder.mspnrMinColor.setSelection(0);
		}
		String strMinColor1 = surfaceSettings.mstrMinColor1;
		if (strMinColor1.trim().toLowerCase(Locale.US).equals("blue"))	{
			holder.mspnrMinColor1.setSelection(1);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("cyan"))	{
			holder.mspnrMinColor1.setSelection(2);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("dkgray"))	{
			holder.mspnrMinColor1.setSelection(3);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("gray"))	{
			holder.mspnrMinColor1.setSelection(4);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("green"))	{
			holder.mspnrMinColor1.setSelection(5);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("ltgray"))	{
			holder.mspnrMinColor1.setSelection(6);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("magenta"))	{
			holder.mspnrMinColor1.setSelection(7);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("red"))	{
			holder.mspnrMinColor1.setSelection(8);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("transparent"))	{
			holder.mspnrMinColor1.setSelection(9);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("white"))	{
			holder.mspnrMinColor1.setSelection(10);
		} else if (strMinColor1.trim().toLowerCase(Locale.US).equals("yellow"))	{
			holder.mspnrMinColor1.setSelection(11);
		} else	{
			holder.mspnrMinColor1.setSelection(0);
		}
		Double dMinColorValue = surfaceSettings.mdMinColorValue;
		if (dMinColorValue == null)	{
			holder.medtMinColorValue.setText("");
		} else	{
			holder.medtMinColorValue.setText(dMinColorValue.toString());
		}
		
		holder.medtUFrom.setText(surfaceSettings.mstrUFrom);
		holder.medtUTo.setText(surfaceSettings.mstrUTo);
		holder.medtUStep.setText((surfaceSettings.mstrUStep == null)?"":surfaceSettings.mstrUStep.trim());
		holder.medtVFrom.setText(surfaceSettings.mstrVFrom);
		holder.medtVTo.setText(surfaceSettings.mstrVTo);
		holder.medtVStep.setText((surfaceSettings.mstrVStep == null)?"":surfaceSettings.mstrVStep.trim());
		
		holder.medtXExpr.setText(surfaceSettings.mstrXExpr);				
		holder.medtYExpr.setText(surfaceSettings.mstrYExpr);				
		holder.medtZExpr.setText(surfaceSettings.mstrZExpr);				
	}
	
	public void refreshSurfaceDefViewList()	{
		mlayoutSurfaceListViewHolder.removeAllViews();
		for (int idx = 0; idx < mlistSurfaceSettings.length; idx ++)	{
			View newCurveDefView = genSurfaceDefView(idx);
			mlayoutSurfaceListViewHolder.addView(newCurveDefView);
		}
		EditText edtGraphName = (EditText)findViewById(R.id.graph_name_edit);
		edtGraphName.requestFocus();	// if not set new focus, the input from ime may crash.
	}
	
	public View genSurfaceDefView(int position) {
			if (position >= mlistSurfaceSettings.length || position < 0)	{
				return null;	// do not exist.
			}
			
			LayoutInflater inflater = LayoutInflater.from(this);
			View convertView = inflater.inflate(R.layout.xyzcurve_def, null);
		 
			/*
			 *  Creates a ViewHolder and store references to the children views
			 *  which we want to bind data to. 
			 */
			final SurfaceDefViewHolder holder = new SurfaceDefViewHolder();
			holder.mnPosition = position;
			holder.medtTitle = (EditText)convertView.findViewById(R.id.curve_name_edit);
			holder.mchkIsGrid = (CheckBox)convertView.findViewById(R.id.surface_is_grid_chkbox);
			holder.mspnrMaxColor = (Spinner)convertView.findViewById(R.id.max_color_spinner);
			holder.mspnrMaxColor1 = (Spinner)convertView.findViewById(R.id.max_color_1_spinner);
			holder.medtMaxColorValue = (EditText)convertView.findViewById(R.id.max_color_value_edit);
			holder.mspnrMinColor = (Spinner)convertView.findViewById(R.id.min_color_spinner);
			holder.mspnrMinColor1 = (Spinner)convertView.findViewById(R.id.min_color_1_spinner);
			holder.medtMinColorValue = (EditText)convertView.findViewById(R.id.min_color_value_edit);
			holder.medtUFrom = (EditText)convertView.findViewById(R.id.u_from_edit);
			holder.medtUTo = (EditText)convertView.findViewById(R.id.u_to_edit);
			holder.medtUStep = (EditText)convertView.findViewById(R.id.u_step_edit);
			holder.medtVFrom = (EditText)convertView.findViewById(R.id.v_from_edit);
			holder.medtVTo = (EditText)convertView.findViewById(R.id.v_to_edit);
			holder.medtVStep = (EditText)convertView.findViewById(R.id.v_step_edit);
			holder.medtXExpr = (EditText)convertView.findViewById(R.id.X_expression_edit);
			holder.medtYExpr = (EditText)convertView.findViewById(R.id.Y_expression_edit);
			holder.medtZExpr = (EditText)convertView.findViewById(R.id.Z_expression_edit);
			holder.mbtnDelete = (Button)convertView.findViewById(R.id.delete_curve_btn);
			holder.mbtnClear = (Button)convertView.findViewById(R.id.clear_curve_btn);

			holder.medtTitle.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					mlistSurfaceSettings[holder.mnPosition].mstrCurveTitle = s.toString();
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
			
			holder.mchkIsGrid.setOnCheckedChangeListener(new OnCheckedChangeListener()	{

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					mlistSurfaceSettings[holder.mnPosition].mbIsGrid = isChecked;
				}
				
			});
			
			holder.mspnrMaxColor.setOnItemSelectedListener(new OnItemSelectedListener(){

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
					mlistSurfaceSettings[holder.mnPosition].mstrMaxColor
						= strColor;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			holder.mspnrMaxColor1.setOnItemSelectedListener(new OnItemSelectedListener(){

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
					mlistSurfaceSettings[holder.mnPosition].mstrMaxColor1
						= strColor;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});
			
			holder.medtMaxColorValue.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					String strInput = s.toString().trim();
					if (strInput.equalsIgnoreCase("null") || strInput.equalsIgnoreCase(""))	{
						mlistSurfaceSettings[holder.mnPosition].mdMaxColorValue = null;
					} else	{
						try {
							mlistSurfaceSettings[holder.mnPosition].mdMaxColorValue = Double.parseDouble(s.toString());
						} catch (NumberFormatException e)	{
							mlistSurfaceSettings[holder.mnPosition].mdMaxColorValue = null;
						}
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
			setImeForEditor(holder.medtMaxColorValue);
			
			holder.mspnrMinColor.setOnItemSelectedListener(new OnItemSelectedListener(){

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
					mlistSurfaceSettings[holder.mnPosition].mstrMinColor
						= strColor;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});				
			
			holder.mspnrMinColor1.setOnItemSelectedListener(new OnItemSelectedListener(){

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
					mlistSurfaceSettings[holder.mnPosition].mstrMinColor1
						= strColor;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub
					
				}
				
			});				
			
			holder.medtMinColorValue.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					String strInput = s.toString().trim();
					if (strInput.equalsIgnoreCase("null") || strInput.equalsIgnoreCase(""))	{
						mlistSurfaceSettings[holder.mnPosition].mdMinColorValue = null;
					} else	{
						try {
							mlistSurfaceSettings[holder.mnPosition].mdMinColorValue = Double.parseDouble(s.toString());
						} catch (NumberFormatException e)	{
							mlistSurfaceSettings[holder.mnPosition].mdMinColorValue = null;
						}
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
			setImeForEditor(holder.medtMinColorValue);
			
			holder.medtUFrom.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					try {
						mlistSurfaceSettings[holder.mnPosition].mstrUFrom = s.toString();
					} catch(NumberFormatException e)	{
						mlistSurfaceSettings[holder.mnPosition].mstrUFrom = "0";
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
			setImeForEditor(holder.medtUFrom);
			
			holder.medtUTo.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					try {
						mlistSurfaceSettings[holder.mnPosition].mstrUTo = s.toString();
					} catch(NumberFormatException e)	{
						mlistSurfaceSettings[holder.mnPosition].mstrUTo = "100";
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
			setImeForEditor(holder.medtUTo);
			
			holder.medtUStep.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					try {
						mlistSurfaceSettings[holder.mnPosition].mstrUStep = s.toString().trim();
					} catch(NumberFormatException e)	{
						mlistSurfaceSettings[holder.mnPosition].mstrUStep = "0";	// means auto-step
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
			setImeForEditor(holder.medtUStep);
			
			holder.medtVFrom.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					try {
						mlistSurfaceSettings[holder.mnPosition].mstrVFrom = s.toString();
					} catch(NumberFormatException e)	{
						mlistSurfaceSettings[holder.mnPosition].mstrVFrom = "0";
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
			setImeForEditor(holder.medtVFrom);
			
			holder.medtVTo.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					try {
						mlistSurfaceSettings[holder.mnPosition].mstrVTo = s.toString();
					} catch(NumberFormatException e)	{
						mlistSurfaceSettings[holder.mnPosition].mstrVTo = "100";
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
			setImeForEditor(holder.medtVTo);
			
			holder.medtVStep.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					try {
						mlistSurfaceSettings[holder.mnPosition].mstrVStep = s.toString().trim();
					} catch(NumberFormatException e)	{
						mlistSurfaceSettings[holder.mnPosition].mstrVStep = "0";	// means auto-step
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
			setImeForEditor(holder.medtVStep);
			
			holder.medtXExpr.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					mlistSurfaceSettings[holder.mnPosition].mstrXExpr = s.toString();
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
					mlistSurfaceSettings[holder.mnPosition].mstrYExpr = s.toString();
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
			
			holder.medtZExpr.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					mlistSurfaceSettings[holder.mnPosition].mstrZExpr = s.toString();
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
			setImeForEditor(holder.medtZExpr);
			
			holder.mbtnDelete.setOnClickListener(new OnClickListener()	{

				@Override
				public void onClick(View v) {
					deleteSurfaceDefView(holder.mnPosition);
					Button btnAddCurve = (Button)findViewById(R.id.add_curve_btn);
					if (btnAddCurve.isEnabled() == false)	{
						btnAddCurve.setEnabled(true);
					}
				}
				
			});
			holder.mbtnClear.setOnClickListener(new OnClickListener()	{

				@Override
				public void onClick(View v) {
					mlistSurfaceSettings[holder.mnPosition].mstrCurveTitle = "";
					mlistSurfaceSettings[holder.mnPosition].mstrMinColor = "white";
					mlistSurfaceSettings[holder.mnPosition].mstrMinColor1 = "white";
					mlistSurfaceSettings[holder.mnPosition].mdMinColorValue = null;  // null means min z is corresponding to min color
					mlistSurfaceSettings[holder.mnPosition].mstrMaxColor = "white";
					mlistSurfaceSettings[holder.mnPosition].mstrMaxColor1 = "white";
					mlistSurfaceSettings[holder.mnPosition].mdMaxColorValue = null;  // null means max z is corresponding to max color
					mlistSurfaceSettings[holder.mnPosition].mstrUFrom = "0";
					mlistSurfaceSettings[holder.mnPosition].mstrUTo = "0";
					mlistSurfaceSettings[holder.mnPosition].mstrUStep = "0";
					mlistSurfaceSettings[holder.mnPosition].mstrVFrom = "0";
					mlistSurfaceSettings[holder.mnPosition].mstrVTo = "0";
					mlistSurfaceSettings[holder.mnPosition].mstrVStep = "0";
					mlistSurfaceSettings[holder.mnPosition].mstrXExpr = "";
					mlistSurfaceSettings[holder.mnPosition].mstrYExpr = "";
					mlistSurfaceSettings[holder.mnPosition].mstrZExpr = "";
					changeSurfaceDefView(holder.mnPosition, mlistSurfaceSettings[holder.mnPosition]);
				}
				
			});
			
			holder.mspnrMaxColor.setSelection(0);
			holder.mspnrMaxColor1.setSelection(0);
			holder.mspnrMinColor.setSelection(0);
			holder.mspnrMinColor1.setSelection(0);
			
			convertView.setTag(holder);

			// synchronize the values in the with with the values in the list.
			setSurfaceDefViewHolder(holder, mlistSurfaceSettings[position]);				

			return convertView;
	}

	public void plotGraph()	{
		EditText edtChartName = (EditText) findViewById(R.id.graph_name_edit);
		EditText edtChartTitle = (EditText) findViewById(R.id.graph_title_edit);
		EditText edtXTitle = (EditText) findViewById(R.id.graph_Xtitle_edit);
		EditText edtYTitle = (EditText) findViewById(R.id.graph_Ytitle_edit);
		EditText edtZTitle = (EditText) findViewById(R.id.graph_Ztitle_edit);
		mstrChartName = edtChartName.getText().toString();
		mstrChartTitle = edtChartTitle.getText().toString();
		mstrXTitle = edtXTitle.getText().toString();
		mstrYTitle = edtYTitle.getText().toString();
		mstrZTitle = edtZTitle.getText().toString();
		mstrPlotCmdLine = "plot_3d_surfaces(\"" + addEscapes(mstrChartName).trim() + "\",\""
						+ addEscapes(mstrChartTitle).trim() + "\",\""
						+ addEscapes(mstrXTitle).trim() + "\",\""
						+ addEscapes(mstrYTitle).trim() + "\",\""
						+ addEscapes(mstrZTitle).trim() + "\"";
		
		int nTotalNumberOfPnts = 0;
		for (int i = 0; i < mlistSurfaceSettings.length; i++)	{
            String strMaxColorValue = "null";
            if (mlistSurfaceSettings[i].mdMaxColorValue != null)    {
                strMaxColorValue = mlistSurfaceSettings[i].mdMaxColorValue.toString();
            }
            String strMinColorValue = "null";
            if (mlistSurfaceSettings[i].mdMinColorValue != null)    {
                strMinColorValue = mlistSurfaceSettings[i].mdMinColorValue.toString();
            }
			String strUStep = mlistSurfaceSettings[i].mstrUStep;
			if (strUStep == null || strUStep.trim().length() == 0) {
				strUStep = "0";
			}
			String strVStep = mlistSurfaceSettings[i].mstrVStep;
			if (strVStep == null || strVStep.trim().length() == 0) {
				strVStep = "0";
			}
			mstrPlotCmdLine += ",\"" + addEscapes(mlistSurfaceSettings[i].mstrCurveTitle).trim()
                            +  "\"," + mlistSurfaceSettings[i].mbIsGrid
                            +  ",\"" + addEscapes(mlistSurfaceSettings[i].mstrMinColor).trim()
                            +  "\",\"" + addEscapes(mlistSurfaceSettings[i].mstrMinColor1).trim()
							+  "\"," + strMinColorValue
							+  ",\"" + addEscapes(mlistSurfaceSettings[i].mstrMaxColor).trim()
							+  "\",\"" + addEscapes(mlistSurfaceSettings[i].mstrMaxColor1).trim()
							+  "\"," + strMaxColorValue
				            +  ",\"u\"," + mlistSurfaceSettings[i].mstrUFrom
				            +  "," + mlistSurfaceSettings[i].mstrUTo
				            +  "," + strUStep
				            +  ",\"v\"," + mlistSurfaceSettings[i].mstrVFrom
				            +  "," + mlistSurfaceSettings[i].mstrVTo
				            +  "," + strVStep
				            +  ",\"" + addEscapes(mlistSurfaceSettings[i].mstrXExpr).trim()
				            +  "\",\"" + addEscapes(mlistSurfaceSettings[i].mstrYExpr).trim()
				            +  "\",\"" + addEscapes(mlistSurfaceSettings[i].mstrZExpr).trim() + "\"";							

			int nNumOfPntsAlongUAxis = 16;	// if nstep == 0, num of points along this axis is 16
			try {
				double dUFrom = Double.parseDouble(mlistSurfaceSettings[i].mstrUFrom);
				double dUTo = Double.parseDouble(mlistSurfaceSettings[i].mstrUTo);
				double dUStep = Double.parseDouble(mlistSurfaceSettings[i].mstrUStep);
				if (dUStep != 0)	{
					nNumOfPntsAlongUAxis = (int) Math.ceil((dUFrom - dUTo)/dUStep);					
				}
			} catch(Exception e)	{
			} finally	{
			}
			int nNumOfPntsAlongVAxis = 16;	// if nstep == 0, num of points along this axis is 16
			try {
				double dVFrom = Double.parseDouble(mlistSurfaceSettings[i].mstrVFrom);
				double dVTo = Double.parseDouble(mlistSurfaceSettings[i].mstrVTo);
				double dVStep = Double.parseDouble(mlistSurfaceSettings[i].mstrVStep);
				if (dVStep != 0)	{
					nNumOfPntsAlongVAxis = (int) Math.ceil((dVFrom - dVTo)/dVStep);					
				}
			} catch(Exception e)	{
			} finally	{
			}
			int nNumOfPnts = nNumOfPntsAlongUAxis * nNumOfPntsAlongVAxis;
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
				// should be interrupted, should save file and plot graphs. No input or log output.
				FuncEvaluator.msfunctionInterrupter = new PlotGraphFunctionInterrupter();
				ScriptAnalyzer.msscriptInterrupter = new PlotGraphScriptInterrupter();
				AbstractExpr.msaexprInterrupter = new PlotGraphAbstractExprInterrupter();
				FuncEvaluator.msstreamConsoleInput = null;
				FuncEvaluator.msstreamLogOutput = null;
				FuncEvaluator.msfileOperator = new MFPFileManagerActivity.MFPFileOperator();
				FuncEvaluator.msgraphPlotter = null;
				FuncEvaluator.msgraphPlotter3D = new PlotGraphPlotter(ActivityPlotXYZGraph.this);
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
							if (((PlotGraphPlotter)(FuncEvaluator.msgraphPlotter3D)).mbOK)	{
								// graph is plotted without problem, but we cannot save the file.
								Context context = getApplicationContext();
								CharSequence text = getString(R.string.graph_file_cannot_be_saved);
								int duration = Toast.LENGTH_SHORT;
								Toast toast = Toast.makeText(context, text, duration);
								toast.show();								
							} else	{
								LayoutInflater inflater = LayoutInflater.from(ActivityPlotXYZGraph.this);
								View vErrMsg = inflater.inflate(R.layout.scroll_message, null);
								TextView txtErrMsg = (TextView) vErrMsg.findViewById(R.id.text_message);
								txtErrMsg.setText((mstrErrMsg == null)?"":mstrErrMsg);
								alertDialog = new AlertDialog.Builder(ActivityPlotXYZGraph.this)
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
	        EditText edtZTitle = (EditText)findViewById(R.id.graph_Ztitle_edit);
	        edtZTitle.setText(getString(R.string.graph_Ztitle_hint));
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
		int nNumofCurves = 3;
		/*int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
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
		}*/
		mlistSurfaceSettings = new SurfaceSettings[nNumofCurves];
		switch (nNumofCurves)	{
		case 3:
	        mlistSurfaceSettings[2] = new SurfaceSettings();
			mlistSurfaceSettings[2].mstrCurveTitle = getString(R.string.curve_title_hint2);
			mlistSurfaceSettings[2].mbIsGrid = false;
			mlistSurfaceSettings[2].mstrMaxColor = "blue";
			mlistSurfaceSettings[2].mstrMaxColor1 = "green";
			mlistSurfaceSettings[2].mdMaxColorValue = -0.5;
			mlistSurfaceSettings[2].mstrMinColor = "red";
			mlistSurfaceSettings[2].mstrMinColor1 = "yellow";
			mlistSurfaceSettings[2].mdMinColorValue = null;
			mlistSurfaceSettings[2].mstrUFrom = "4";
			mlistSurfaceSettings[2].mstrUTo = "-6";
			mlistSurfaceSettings[2].mstrUStep = "-1";
			mlistSurfaceSettings[2].mstrVFrom = "-3";
			mlistSurfaceSettings[2].mstrVTo = "3";
			mlistSurfaceSettings[2].mstrVStep = "0.5";
			mlistSurfaceSettings[2].mstrXExpr = "u";
			mlistSurfaceSettings[2].mstrYExpr = "v";
			mlistSurfaceSettings[2].mstrZExpr = "-2 - 4/(u**2 + v**2 + 1)";
		case 2:
	        mlistSurfaceSettings[1] = new SurfaceSettings();
			mlistSurfaceSettings[1].mstrCurveTitle = getString(R.string.curve_title_hint);
			mlistSurfaceSettings[1].mbIsGrid = true;
			mlistSurfaceSettings[1].mstrMaxColor = "white";
			mlistSurfaceSettings[1].mstrMaxColor1 = "white";
			mlistSurfaceSettings[1].mdMaxColorValue = null;
			mlistSurfaceSettings[1].mstrMinColor = "green";
			mlistSurfaceSettings[1].mstrMinColor1 = "green";
			mlistSurfaceSettings[1].mdMinColorValue = null;
			mlistSurfaceSettings[1].mstrUFrom = "0";
			mlistSurfaceSettings[1].mstrUTo = "PI";
			mlistSurfaceSettings[1].mstrUStep = "PI/16.0";	//Double.NaN;	// auto step.
			mlistSurfaceSettings[1].mstrVFrom = "-PI/2.0";
			mlistSurfaceSettings[1].mstrVTo = "PI/2.0";
			mlistSurfaceSettings[1].mstrVStep = "PI/8.0";	//Double.NaN;	// auto step.
			mlistSurfaceSettings[1].mstrXExpr = "2*cos(u)*sin(v)";
			mlistSurfaceSettings[1].mstrYExpr = "2*cos(u)*cos(v)";
			mlistSurfaceSettings[1].mstrZExpr = "2*sin(u)";
		case 1:
	        mlistSurfaceSettings[0] = new SurfaceSettings();
			mlistSurfaceSettings[0].mstrCurveTitle = getString(R.string.curve_title_hint1);
			mlistSurfaceSettings[0].mbIsGrid = false;
			mlistSurfaceSettings[0].mstrMaxColor = "green";
			mlistSurfaceSettings[0].mstrMaxColor1 = "cyan";
			mlistSurfaceSettings[0].mdMaxColorValue = 0.0;
			mlistSurfaceSettings[0].mstrMinColor = "white";
			mlistSurfaceSettings[0].mstrMinColor1 = "magenta";
			mlistSurfaceSettings[0].mdMinColorValue = -1.0;
			mlistSurfaceSettings[0].mstrUFrom = "0";
			mlistSurfaceSettings[0].mstrUTo = "PI";
			mlistSurfaceSettings[0].mstrUStep = "PI/15.0";	//Double.NaN;	// auto step.
			mlistSurfaceSettings[0].mstrVFrom = "-PI/2.0";
			mlistSurfaceSettings[0].mstrVTo = "PI/2.0";
			mlistSurfaceSettings[0].mstrVStep = "PI/8.0";	//Double.NaN;	// auto step.
			mlistSurfaceSettings[0].mstrXExpr = "2*cos(u)*sin(v)";
			mlistSurfaceSettings[0].mstrYExpr = "2*cos(u)*cos(v)";
			mlistSurfaceSettings[0].mstrZExpr = "-2*sin(u)";
		default:	// 0
		}
		refreshSurfaceDefViewList();
	}
}
