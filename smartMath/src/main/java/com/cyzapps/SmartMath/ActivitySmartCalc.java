package com.cyzapps.SmartMath;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import com.cyzapps.SmartMath.InputPadMgrEx.InputKey;
import com.cyzapps.SmartMath.InputPadMgrEx.TableInputPad;
import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jfcalc.FuncEvaluator.GraphPlotter;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptInterrupter;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.AbstractExprInterrupter;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.VisualMFP.Color;
import com.cyzapps.adapter.AbstractExprConverter;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.MFPAdapter.FunctionEntry;
import com.cyzapps.adapter.MFPAdapter.InternalFuncInfo;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ActivitySmartCalc extends Activity	implements AMInputMethod.InputMethodCaller {

    //private static PatternManager mspm = null;	// use solveAnalyzer's patternmanager instead.

	private static final int ITEM0 = Menu.FIRST;
	private static final int ITEM1 = Menu.FIRST + 1;
	private static final int ITEM2 = Menu.FIRST + 2;
	private static final int ITEM3 = Menu.FIRST + 3;
	private static final int ITEM4 = Menu.FIRST + 4;
	private static final int ITEM5 = Menu.FIRST + 5;
	private static final int ITEM6 = Menu.FIRST + 6;
	private static final int ITEM7 = Menu.FIRST + 7;
	private static final int ITEM8 = Menu.FIRST + 8;

	public static final int HISTORICAL_LIST_ACTIVITY = 1;
	public static final int CFG_KEYPAD_ACTIVITY = 2;
	public static final int CALCULATOR_ASSISTANT_ACTIVITY = 3;
	public static final int GRAPH_DEMON_ACTIVITY = 4;
	public static final int CAPTURE_IMAGE_ACTIVITY = 5;
	public static final int FINGER_PAINT_ACTIVITY = 6;
	
	public static final int MAX_INPUT_ROWS = 6; //at this moment, JSma limit the linear equations solver to max 6 equations.
	
	public static final String SHOWN_PANEL = "shown_panel";
	public static final String SHIFT_PRESSED = "shift_pressed";
	public static final String INPUT_BUFFER = "input_buffer";
	public static final String LAST_EXPRESSION = "last_expression";
	public static final String LAST_ANSWER = "last_answer";
	public static final String LAST_TASK_TYPE = "last_task_type";
	public static final String LAST_OUTPUT = "last_output";
	public static final String LAST_INPUT_TYPE ="last_input_type";
	public static final String LAST_SOFTKEY_STATE = "last_softkey_state";
	public static final String LAST_BITMAP_USED = "last_bitmap_used";
	public static final String LAST_RECOGNIZED_RESULT = "last_recognized_result";
	
	public static final String VERSION_NUMBER = "SmartMath_version_number";
	public static final String WHATS_NEW_FILE_PATH = "whats_new";

	public boolean mbDblBack2ExitPressedOnce = false;

	protected AMInputMethod minputMethod = null;
	
	public static final String IMMUTABLE_INPUTPAD_CONFIG = "immutable_inputpad_sc.cfg";
	public static final int MY_PERMISSIONS_REQUEST = 0;

    private AsyncTaskManager masyncTask;
    private ProgressDialog mdlgTaskProgressing = null;
    public static final String OUTPUT_HEAD_STRING = "<!DOCTYPE html>"
    		+ "<html lang=\"en\" xmlns:m=\"http://www.w3.org/1998/Math/MathML\">"
    		+ "<head>" + "<meta charset=\"utf-8\">"
    		+ "<link rel=\"stylesheet\" href=\"http://fonts.googleapis.com/css?family=UnifrakturMaguntia\">"
    		+ "<link rel=\"stylesheet\" href=\"../mathscribe/jqmath-0.4.3.css\">"
    		+ "<script src=\"../mathscribe/jquery-3.5.0.min.js\"></script><script src=\"../mathscribe/jqmath-etc-0.4.6.min.js\"></script>"
    		+ "<title></title><style>p.quickhelp{width:100%;word-wrap:normal;}</style></head><body style=\"font-size:";
    public static final String OUTPUT_SIZE_UNIT_STRING = "pt;\">";
    public static final String OUTPUT_TAIL_STRING = "<p><br></p><p><br></p><p><br></p><p><br></p><p><br></p><p><br></p></body></html>";
 
    private String mstrFontSize = "30";
    private String[] mstrarrayTaskAndOutput = new String[] {"", ""};	// 0 is task type and 1 is task output
    private String mstrRecognized = "";
    
    public static final int ENABLE_SHOW_INPUTPAD = -1;
    public static final int ENABLE_HIDE_INPUTPAD = 0;
    public static final int ENABLE_HIDE_SOFTKEY = 1;
    public static final int ENABLE_SHOW_SOFTKEY = 2;
    
    private int mnSoftKeyState = ENABLE_SHOW_INPUTPAD;	// 0 means use but hide inputpad, -1 means use and show inputpad
									// 1 means use but hide softkeyboard, 2 means use and show soft keyboard.
   
	public static final String ACHART_URL_HEADER = "AChart://";
	public static final String SELECTED_STORAGE_PATH_UNIMATCH = "SELECTED_STORAGE_PATH_UNIMATCH";
	public static final String SCREEN_RELATIVE_DPI = "SCREENDPI";
	
	public static final String AOPER_URL_HEADER = "AOper://";
	public static final String EMAIL_UNSATISFACTORY_RECOG = "email_unsatisfactory_recog";
	
	public static final String HISTORICAL_RECORDS_FILE = "historical_records";
    public static class HistoricalRecordItem	{
    	public String mstrTaskType = "";
    	public String mstrFormattedInput = "";
    	public String mstrOutput = "";    	
    	public String mstrChartFileName = "";
    	
    	public HistoricalRecordItem()	{}
    	
    	public HistoricalRecordItem(String strFormattedInput, String strTaskType, String strOutput, String strChartFileName)	{
    		mstrFormattedInput = strFormattedInput;
    		mstrTaskType = strTaskType;
    		mstrOutput = strOutput;
    		mstrChartFileName = strChartFileName;
    	}
    	
    	public void setHistoricalRecordItem(String strFormattedInput, String strTaskType, String strOutput, String strChartFileName)	{
    		mstrFormattedInput = strFormattedInput;
    		mstrTaskType = strTaskType;
    		mstrOutput = strOutput;
    		mstrChartFileName = strChartFileName;
    	}
    	
    }
    
    public static class HistoricalRecordManager	{
    	protected String mstrSavedFileName = "";
    	protected LinkedList<HistoricalRecordItem> mlistAllRecords = new LinkedList<HistoricalRecordItem>();	// history record.
    	
    	HistoricalRecordManager()	{}
    	
    	HistoricalRecordManager(String strSavedFileName)	{
    		mstrSavedFileName = strSavedFileName;
    	}
    	
    	public boolean addRecord(String strFormattedInput, String strTaskType, String strOutput, String strChartFileName, int nMaxNumberofRecords)	{
    		boolean bReturn = true;
    		for (int idx = 0; idx < mlistAllRecords.size(); idx ++)	{
    			if (mlistAllRecords.get(idx).mstrFormattedInput.equals(strFormattedInput)
    					&& mlistAllRecords.get(idx).mstrTaskType.equals(strTaskType))	{
    				mlistAllRecords.remove(idx);
    				idx --;
    				bReturn = false;
    			}
    		}
    		mlistAllRecords.add(0, new HistoricalRecordItem(strFormattedInput, strTaskType, strOutput, strChartFileName));
    		if (nMaxNumberofRecords >= 0)	{
    			restrictRecordsLen(nMaxNumberofRecords);
    		}
    		return bReturn;	// true means the record is new
    	}
    	
    	public boolean addRecord(String strFormattedInput, String strTaskType, String strOutput, String strChartFileName)	{
    		return addRecord(strFormattedInput, strTaskType, strOutput, strChartFileName, -1);
    	}
    	
    	public boolean addRecord(HistoricalRecordItem item, int nMaxNumberofRecords)	{
    		return addRecord(item.mstrFormattedInput, item.mstrTaskType, item.mstrOutput, item.mstrChartFileName, nMaxNumberofRecords);
    	}
    	
    	public boolean addRecord(HistoricalRecordItem item)	{
    		return addRecord(item.mstrFormattedInput, item.mstrTaskType, item.mstrOutput, item.mstrChartFileName, -1);
    	}
    	
    	public LinkedList<HistoricalRecordItem> getAllRecords()	{
    		return mlistAllRecords;
    	}
    	
    	public HistoricalRecordItem getFirstRecord()	{
    		return (mlistAllRecords.size()>0)?mlistAllRecords.get(0):null;
    	}
    	
    	public int getRecordsLen()	{
    		return mlistAllRecords.size();
    	}
    	
    	public void clearRecords()	{
    		if (mlistAllRecords.size() > 0)	{
	    		mlistAllRecords.clear();
	    	}
    	}
    	
    	public void restrictRecordsLen(int nLen)	{
    		boolean bRecordsRemoved = false;
    		while (mlistAllRecords.size() > nLen)	{
    			mlistAllRecords.removeLast();
    			bRecordsRemoved = true;
    		}
    	}
    	
    	public String genXMLFromRecords(String strFontSize, String strNoRecordMsg)	{
    	   	String strAllRecords = "";
    	   	if (mlistAllRecords.size() == 0)	{
    	   		strAllRecords = OUTPUT_HEAD_STRING + strFontSize + OUTPUT_SIZE_UNIT_STRING
    	   						+ "<pre>" + strNoRecordMsg + "</pre>"
    	   						+ OUTPUT_TAIL_STRING;
    	   	} else	{
    	   		for (int idx = 0; idx < mlistAllRecords.size(); idx ++)	{
    	   			strAllRecords += mlistAllRecords.get(idx).mstrOutput;
    	   			if (idx != mlistAllRecords.size() - 1)	{
    	   				strAllRecords += "\n<hr>\n";
    	   			}
    	   		}
    	   		strAllRecords = OUTPUT_HEAD_STRING + strFontSize + OUTPUT_SIZE_UNIT_STRING
    				+ strAllRecords + OUTPUT_TAIL_STRING;
    	   	}
    		return strAllRecords;
    	}
    	
    	public boolean flush(Context context)	{
    		if (mstrSavedFileName == null || mstrSavedFileName.trim().length() == 0)	{
    			return false;
    		}
    		try	{
	    	    DataOutputStream out = new DataOutputStream(context.openFileOutput(mstrSavedFileName, Context.MODE_PRIVATE));
	    	    out.writeUTF(Integer.toString(mlistAllRecords.size()));	// number of records
	    	    for (int i = 0; i < mlistAllRecords.size(); i++) {
	    	    	out.writeUTF("FormattedInput");
	    	        out.writeUTF(mlistAllRecords.get(i).mstrFormattedInput);
	    	    	out.writeUTF("TaskType");
	    	        out.writeUTF(mlistAllRecords.get(i).mstrTaskType);
	    	    	out.writeUTF("Output");
	    	        out.writeUTF(mlistAllRecords.get(i).mstrOutput);
					out.writeUTF("ChartFileName");
					out.writeUTF(mlistAllRecords.get(i).mstrChartFileName);
	    	    	out.writeUTF("EndOfItem");
	    	    }
	    	    out.close();
	    	    return true;
    		} catch(IOException e)	{
    			return false;
    		}
    	}
    	
    	public boolean load(Context context)	{
    		if (mstrSavedFileName == null || mstrSavedFileName.trim().length() == 0)	{
    			return false;
    		}
    		boolean bReturn = true;
    		try	{
    			DataInputStream in = new DataInputStream(context.openFileInput(mstrSavedFileName));
		    	LinkedList<HistoricalRecordItem> listAllRecords = new LinkedList<HistoricalRecordItem>();
		    	HistoricalRecordItem itemCurrent = null;
		    	int nLen = 0;
			    try {
		        	String str = in.readUTF();
	        		try	{
	        			nLen = Integer.parseInt(str);
		        		if (nLen < 0)	{
		        			bReturn = false;
		        		}
	        		} catch(NumberFormatException e)	{
	        			bReturn = false;
	        		}
		        	if (bReturn)	{
		        		while (true)	{
		        			itemCurrent = new HistoricalRecordItem();
			        		while (true)	{
		        				String strName = in.readUTF();
		        				if (strName.equalsIgnoreCase("EndOfItem"))	{
		        					break;
		        				} else if (strName.equalsIgnoreCase("FormattedInput"))	{
		        					itemCurrent.mstrFormattedInput = in.readUTF();
		        				} else if (strName.equalsIgnoreCase("TaskType"))	{
		        					itemCurrent.mstrTaskType = in.readUTF();
								} else if (strName.equalsIgnoreCase("Output"))	{
									itemCurrent.mstrOutput = in.readUTF();
								} else if (strName.equalsIgnoreCase("ChartFileName"))	{
									itemCurrent.mstrChartFileName = in.readUTF();
								} else	{
									// unsupported type
									in.readUTF();
								}
			        		}
			        		listAllRecords.add(itemCurrent);
		        		}
		        	}
			    } catch (EOFException e) {
			        if (listAllRecords.size() != nLen)	{
			        	bReturn = false;
			        }
			    }
			    in.close();
			    if (bReturn == true)	{
			    	mlistAllRecords = listAllRecords;
			    }
	    	    return bReturn;
    		} catch(IOException e)	{
    			return false;
    		}
    	}
    }
    
    public static HistoricalRecordManager mshistoricalRecMgr = new HistoricalRecordManager();

    public static class SmartCalcFunctionInterrupter extends FunctionInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public static class SmartCalcScriptInterrupter extends ScriptInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }
	
    public static class SmartCalcAbstractExprInterrupter extends AbstractExprInterrupter	{

		@Override
		public boolean shouldInterrupt() {
			return Thread.currentThread().isInterrupted();
		}

		@Override
		public void interrupt() throws InterruptedException {
			throw new InterruptedException();
		}
    	
    }

	public static class PlotGraphPlotter extends GraphPlotter	{
		public PlotGraphPlotter()	{}
		public PlotGraphPlotter(Context context)	{
			mcontext = context;
		}
		
		public PlotGraphPlotter(Context context, long lGraphTriggerTime)	{
			mcontext = context;
			mlGraphTriggerTime = lGraphTriggerTime;
		}
		
		public boolean mbOK = false;
		public Context mcontext = null;
		public long mlGraphTriggerTime = 0L;
		
		@Override
		public boolean plotGraph(String strGraphInfo) {
			if (mcontext != null)	{
				Intent intent = new Intent(mcontext, ActivityChartDemon.class);
				intent.putExtra(ChartOperator.VMFPChart, strGraphInfo);
				intent.putExtra("GraphTriggerTime", mlGraphTriggerTime);
				((Activity) mcontext).startActivityForResult(intent, GRAPH_DEMON_ACTIVITY);
				mbOK = true;
			} else	{
				mbOK = false;
			}
			return mbOK;
		}
		
	}

    public static class AsyncTaskManager extends AsyncTask<String, Void, String>	{

    	private ActivitySmartCalc mactivity;
    	private boolean mbCompleted;
    	
    	public String mstrTaskType = "";
    	public String mstrFormattedInput = "";
    	public long mlTaskTriggerTime = 0L;
    	
    	public AsyncTaskManager(ActivitySmartCalc activity)	{
    		mactivity = activity;
    	}
        /**
         * Showing the processing dialog on the UI thread.
         **/
        @Override
        protected void onPreExecute() {
        	mbCompleted = false;
        	if (mactivity != null)	{
            	if (mstrTaskType.trim().equalsIgnoreCase("calculate")) {
	        		mactivity.mdlgTaskProgressing = ProgressDialog
	        										.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_result_cancelable), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("recognize-calculate"))	{
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_result_after_recognition), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("plot")) {
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_chart_data_cancelable), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("recognize-plot"))	{
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_chart_data_after_recognition), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("initialize"))	{
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.loading_libs_history), true);
	        	}
        	}
        }
        
    	@Override
    	protected String doInBackground(String... arg0) {
    		// ensure that all functions are static
    		Context context = mactivity;
    		if (context == null)	{
    			context = AppSmartMath.getContext();
    		}
    		mstrFormattedInput = "";
    		java.util.Date date= new java.util.Date();
    		mlTaskTriggerTime = date.getTime();
    		if (context != null)	{
	        	if (mstrTaskType.trim().equalsIgnoreCase("calculate"))	{
	        		mstrFormattedInput = formatInput(arg0[0]);
	        		return SmartCalcProcLib.calculate(context, arg0[0], true);
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("recognize-calculate"))	{
	        		mstrFormattedInput = formatInput(arg0[0]);
	        		String strOutput = SmartCalcProcLib.calculate(context, arg0[0], false);
	        		if (strOutput == null || strOutput.length() == 0) {
	        			strOutput = "<p>" + context.getString(R.string.no_valid_expr_recognized) + "</p>\n";
	        		}
	        		return strOutput;
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("plot"))	{
	        		mstrFormattedInput = formatInput(arg0[0]);
	        		return SmartCalcProcLib.plot(context, mactivity, arg0[0], mlTaskTriggerTime, true);
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("recognize-plot"))	{
	        		mstrFormattedInput = formatInput(arg0[0]);
	        		String strOutput = SmartCalcProcLib.plot(context, mactivity, arg0[0], mlTaskTriggerTime, false);
	        		if (strOutput == null || strOutput.length() == 0) {
	        			strOutput = "<p>" + context.getString(R.string.no_valid_expr_recognized) + "</p>\n";
	        		}
	        		return strOutput;
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("initialize"))	{
	        		// do not delete the following lines because I need to create smartmath folder and chart folder to store taken photos and graphs.
	        		boolean bFolderExist = true;
	                File dir = new File(MFPFileManagerActivity.getChartFolderFullPath());	// app folder is the parent of chart folder
	                if (!dir.exists())	{
	                	bFolderExist = dir.mkdirs();
	                } else if (!dir.isDirectory())	{
	                	bFolderExist = false;
	                }

	        		// load historical record manager. Have to do this as early as possible to ensure
	        		// get activity result later than load history (as they are in different thread).
	        		mshistoricalRecMgr = new HistoricalRecordManager(HISTORICAL_RECORDS_FILE);
	    			mshistoricalRecMgr.load(context); // if no file to load, then mhistoricalRecMgr is empty.
	    			mshistoricalRecMgr.restrictRecordsLen(ActivitySettings.msnNumberofRecords);
	        		// load libs if necessary
	        		if (MFPAdapter.isFuncSpaceEmpty())	{
	        			MFPAdapter.reloadAll(context, -1, null);	// using sync mode to reload all coz it has been in an async thread.
	        		}
	    			// do not load patterns here because take too much time.
	    			// delete obsolete charts.
	    			ActivitySmartCalc.removeObsoleteCharts(context);
	        		// do not delete the following lines because I need to create smartmath folder and chart folder to store taken photos and graphs.
	    			if (bFolderExist)	{
	    				return "";	// no error.
	    			} else	{
	    				return context.getString(R.string.cannot_create_app_folder);
	    			}
	        	}
    		}
    		return null;
    	}

    	/**
    	 * When the task is completed, notifiy the Activity.
    	 */
    	@Override
    	protected void onPostExecute(String strOutput) {
    		mbCompleted = true;
        	if (mactivity != null
        			&& (mstrTaskType.trim().equalsIgnoreCase("calculate")
        					|| mstrTaskType.trim().equalsIgnoreCase("recognize-calculate")
        					|| mstrTaskType.trim().equalsIgnoreCase("plot")
        					|| mstrTaskType.trim().equalsIgnoreCase("recognize-plot")))	{
        		mactivity.onCalcPlotCompleted(mstrFormattedInput, mlTaskTriggerTime, mstrTaskType, strOutput);
            	notifyTaskCompletedOrDetached(mactivity);
        	} else if (mstrTaskType.trim().equalsIgnoreCase("initialize"))	{
            	notifyTaskCompletedOrDetached(mactivity);
        		PackageInfo pinfo;
        		int nCurrentVersionNum = 0;
        		try {
        			pinfo = mactivity.getPackageManager().getPackageInfo(mactivity.getPackageName(), 0);
        			nCurrentVersionNum = pinfo.versionCode;
        		} catch (NameNotFoundException e1) {
        			// Do nothing
        		}
        		int nSavedVersionNum = 0;
        		SharedPreferences init_settings = mactivity.getApplication().getSharedPreferences(ActivitySettings.SETTINGS, 0);
        		if (init_settings != null) {
        			nSavedVersionNum = init_settings.getInt(VERSION_NUMBER, 0);
        			init_settings.edit().putInt(VERSION_NUMBER, nCurrentVersionNum).commit();
        		}
        		
            	if ((strOutput == null || strOutput.length() == 0) && nCurrentVersionNum > nSavedVersionNum)	{
            		mactivity.showWhatsNewBox();
            	} else if (strOutput != null && strOutput.length() > 0 && nCurrentVersionNum > nSavedVersionNum)	{
            		mactivity.showErrorMsgBox(strOutput, "WhatsNew");	// next step is whats new box
            	} else if (strOutput != null && strOutput.length() > 0) {
            		mactivity.showErrorMsgBox(strOutput, "");	// do not show whats new box after error message box.
        		}	// no error message nor whats new box, do nothing.
        	} else	{
            	notifyTaskCompletedOrDetached(mactivity);
        	}
        	if (mactivity != null)	{
        	}
    	}
     
        @Override
        protected void onCancelled()
        {
        	//no need to dismiss dialog coz dialog has been dismissed when user cancel it;
        	// if cancel dlg again here, we may canel dlg belonging to a different activity.
        }

        public void setActivity(ActivitySmartCalc activity) {
        	notifyTaskCompletedOrDetached(mactivity);	// notify old activity task detached.
    		mactivity = activity;
    		if (mactivity != null && mactivity.mdlgTaskProgressing == null && !mbCompleted)	{
    			// async task is still running, but the progressing dialog hasn't been initialized.
            	if (mstrTaskType.trim().equalsIgnoreCase("calculate") && !isCancelled())	{
            		mactivity.mdlgTaskProgressing = ProgressDialog
    								        		.show(mactivity, mactivity.getString(R.string.please_wait),
    								        				mactivity.getString(R.string.calculating_result_cancelable), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("recognize-calculate") && !isCancelled())	{
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_result_after_recognition), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("plot") && !isCancelled()) {
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_chart_data_cancelable), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
	        	} else if (mstrTaskType.trim().equalsIgnoreCase("recognize-plot") && !isCancelled())	{
	        		mactivity.mdlgTaskProgressing = ProgressDialog
									        		.show(mactivity, mactivity.getString(R.string.please_wait),
									        				mactivity.getString(R.string.calculating_chart_data_after_recognition), true, true,
									        				new DialogInterface.OnCancelListener() {

																@Override
																public void onCancel(DialogInterface dialog) {
																	// cancel AsyncTask
												                    cancel(true);
																}
									        			
									        				});
            	} else if (mstrTaskType.trim().equalsIgnoreCase("initialize"))	{
            		mactivity.mdlgTaskProgressing = ProgressDialog
    								        		.show(mactivity, mactivity.getString(R.string.please_wait),
    								        				mactivity.getString(R.string.loading_libs_history), true);
            	}
    		}
    		if ( mbCompleted ) {	// check mbCompleted again coz onPostExecute may be in a different thread (but it should be in main UI, maybe a different activity?)
    			notifyTaskCompletedOrDetached(mactivity);	// notify current activity task is done.
    		}
    	}
     
    	/**
    	 * Helper method to notify the activity that this task was completed.
    	 */
    	private static void notifyTaskCompletedOrDetached(ActivitySmartCalc activity) {
    		if ( activity != null ) {
    			activity.onDismissProgDlg();
    		}
    	}
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
    	Log.d("History", "onCreate Start");
    	//-----------------------------------------------------------
		super.onCreate(savedInstanceState);
        // requesting to turn the title OFF
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenOrientation = getResources().getConfiguration().orientation;
		if (nScreenOrientation != Configuration.ORIENTATION_LANDSCAPE)	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{        // making it full screen in portrait mode if small screen
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		} else	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
					|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{        // making it full screen in landscape mode if small or normal screen
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
		setContentView(R.layout.smart_calc);
    	Log.d("History", "After setContentView");

		//----------------------------------------------------------------
		// always do ansyc task as early as possible.
		Object oRetained = getLastNonConfigurationInstance();
		if ( oRetained instanceof AsyncTaskManager ) {
			Log.d("History", "Reclaiming previous background task.");
			masyncTask = (AsyncTaskManager) oRetained;
			masyncTask.setActivity(this);
		} else {
			//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);	// prevent orientation change during initialization.
			masyncTask = new AsyncTaskManager(this);
			Log.d("History", "Creating new background task.");
			masyncTask.mstrTaskType = "initialize";
			masyncTask.execute(new String[0]);	// cannot use progress dlg coz calculator will destroy and recreate activity at orientation switch.
		}
		Log.d("History", "After asyncTask.");
		//----------------------------------------------------------------
		// set initial working dir inside readSettings.
		ActivitySettings.readSettings();	// have to call readsettings again here because when reload, readsettings in main panel is not executed.

		//----------------------------------------------------------

		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		txtInput.setHorizontallyScrolling(true);	// disable word-wrapping while keep multiple lines.
		txtInput.setOnTouchListener(new EditText.OnTouchListener()	{

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				/*
				 * Use soft keyboard or inputpad depends on mnSoftKeyState
				 */
			   	if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
			   		// do not use soft keyboard, use input pad
			   		v.onTouchEvent(event);	// different from scripteditor, cannot reset inputtype here.
					setSoftKeyState((EditText)v, ENABLE_SHOW_INPUTPAD);
				    return true;	// intercept the onTouch event.
			   	} else {	// if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY || mnSoftKeyState == ENABLE_HIDE_SOFTKEY)	{
			   		// show the soft keyboard and disable input pad
			   		setSoftKeyState((EditText)v, ENABLE_SHOW_SOFTKEY);
			   		return false;
			   	}
			}
			
		});
		View btnCameraIme = findViewById(R.id.btnCameraIme);
		btnCameraIme.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent i = new Intent(ActivitySmartCalc.this, ActivityQuickRecog.class);
	            startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY);
			}
			
		});
		
		View btnLastFingerPaint = findViewById(R.id.btnLastFingerPaint);
		btnLastFingerPaint.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
	            Bundle bundle = new Bundle();
	            if (bundle != null)	{
	                bundle.putBoolean("AfterPhotoTaken", false);
	                Intent i = new Intent(ActivitySmartCalc.this, ActivityFingerPaint.class);
	                i.putExtras(bundle);
	                startActivityForResult(i, FINGER_PAINT_ACTIVITY);
	            }
			}
			
		});
		
		Camera camera = CameraPreview.getCamera(this);
		if (camera == null)	{
			// if no camera is available, we disable the taking picture function.
			btnCameraIme.setVisibility(View.GONE);
		} else {
			camera.release(); // we do not use camera in smart calc.
		}
				
		View btnStartCalc = findViewById(R.id.btnStartCalc);
		btnStartCalc.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				analyze("calculate");
				EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
				if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
					setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);
				} else	{
					setSoftKeyState(txtInput, ENABLE_HIDE_SOFTKEY);
				}
			}
			
		});
		View btnStartPlot = findViewById(R.id.btnStartPlot);
		btnStartPlot.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				analyze("plot");
				EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
				if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
					setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);
				} else	{
					setSoftKeyState(txtInput, ENABLE_HIDE_SOFTKEY);
				}
			}
			
		});
		
		Log.d("History", "After set events.");
		//----------------------------------------------------------------
		Resources resources = getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    float fSizeInDPI = txtInput.getTextSize() / (metrics.densityDpi / 160f);
	    int nFontSize = (int)(fSizeInDPI * 130.0 / 160.0);
	    mstrFontSize = String.valueOf(nFontSize);	// in html, a pt is 1/72 inch while in Android, it is 1/160 inch
		
		WebView wvOutput = (WebView) findViewById(R.id.webviewSmartMathOutput);
		wvOutput.setVerticalScrollBarEnabled(true);
		wvOutput.setHorizontalScrollBarEnabled(true);
		wvOutput.getSettings().setBuiltInZoomControls(true);
		wvOutput.getSettings().setJavaScriptEnabled(true);
		wvOutput.setWebViewClient(new WebViewClient(){
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (AbstractExprConverter.isQuotedUrlExprStr(url))	{
	                // copy the selected expression text into input
					String strExpr = AbstractExprConverter.convtQuotedUrl2PlainStr(url);
					insert2Input(strExpr);
				} else if (url.length() >= ACHART_URL_HEADER.length()
						&& url.substring(0, ACHART_URL_HEADER.length()).equalsIgnoreCase(ACHART_URL_HEADER))	{
					// this is a chart
					String strPath = url.substring(ACHART_URL_HEADER.length()).trim();
					try {
						strPath = URLDecoder.decode(strPath, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// Do nothing here;
					}
					String strErrorMsg = MFPFileManagerActivity.openChartFile(ActivitySmartCalc.this, strPath);
					if (strErrorMsg != null && strErrorMsg.trim().length() != 0)	{
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(ActivitySmartCalc.this, strErrorMsg, duration);
						toast.show();
					}
				} else if (url.length() >= AOPER_URL_HEADER.length()
						&& url.substring(0, AOPER_URL_HEADER.length()).equalsIgnoreCase(AOPER_URL_HEADER)) {
					// this is an operation command
					String strOperCmd = url.substring(AOPER_URL_HEADER.length()).trim();
					if (strOperCmd.equals(EMAIL_UNSATISFACTORY_RECOG)) {
						String[] strarrayEmailTexts = new String[3];
						strarrayEmailTexts[0] = mstrRecognized;
						EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
						strarrayEmailTexts[1] = txtInput.getText().toString();
						strarrayEmailTexts[2] = mstrarrayTaskAndOutput[1];
						sendEmail2Developer(strOperCmd, strarrayEmailTexts);
					}
				}
				return true;	// always intercept, otherwise may cause crash problem.
			}
		});
		mstrarrayTaskAndOutput[0] = "initialize";
	    mstrarrayTaskAndOutput[1] = "<p class=\"quickhelp\">" + SmartCalcProcLib.recalcWidthHeight4Help(this, getString(R.string.welcome_message4)).replace("\n", "</p><p>") + "</p>";
		// here we do not need to covert SELECTED_STORAGE_PATH_UNIMATCH to real path because it is just welcome message.
		wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
				OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING + mstrarrayTaskAndOutput[1] + OUTPUT_TAIL_STRING,
				"text/html", "utf-8", "");
		Log.d("History", "After set web view.");
		//-----------------------------------------------------------
		
        //--- Show input method
        LinearLayout layoutIMEHolder = (LinearLayout) findViewById(R.id.linearLayoutSMInputPad);
		minputMethod = new AMInputMethod(this);
		layoutIMEHolder.addView(minputMethod);

		minputMethod.initialize(this, txtInput);
		setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);	// use inputpad and hide soft keyboard
		Log.d("History", "After set input key pad.");
		
		//----------------------------------------------------------------
		// request permissions.
		process1stStartStuffWithPermissionRequest();
	}

	public void process1stStartStuffWithPermissionRequest() {
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.MODIFY_AUDIO_SETTINGS)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.WAKE_LOCK)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.VIBRATE)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED
                /*|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.FLASHLIGHT)    // flashlight is a normal permission so no need to request.
                    != PackageManager.PERMISSION_GRANTED*/
                /*|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED*/
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET)
				!= PackageManager.PERMISSION_GRANTED
                /*|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED*/
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_WIFI_STATE)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_WIFI_STATE)
				!= PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this, android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
				!= PackageManager.PERMISSION_GRANTED
		) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.VIBRATE)
					|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
					|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)
					//|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.FLASHLIGHT)  // flashlight a normal permission no need to request.
					//|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_PHONE_STATE)
					|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.INTERNET)
					//|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
					|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_NETWORK_STATE)
					|| ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_WIFI_STATE)
			) {
				// Show an expanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
				AlertDialog.Builder blder = new AlertDialog.Builder(this);
				blder.setIcon(R.drawable.icon);
				blder.setTitle(getString(R.string.help));
				blder.setMessage(getString(R.string.why_request_permissions));
				blder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// No explanation needed, we can request the permission.
						ActivityCompat.requestPermissions(ActivitySmartCalc.this, new String[]{
										android.Manifest.permission.VIBRATE,
										android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
										android.Manifest.permission.CAMERA,
										//android.Manifest.permission.FLASHLIGHT,   // normal permission no need to request
										//android.Manifest.permission.READ_PHONE_STATE,
										android.Manifest.permission.INTERNET,
										//android.Manifest.permission.ACCESS_COARSE_LOCATION,
										android.Manifest.permission.ACCESS_NETWORK_STATE,
										android.Manifest.permission.ACCESS_WIFI_STATE
								},
								MY_PERMISSIONS_REQUEST);
					}
				});
				blder.setCancelable(false);
				AlertDialog alertErrDlg = blder.create();
				alertErrDlg.show();
			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this, new String[]{
								android.Manifest.permission.VIBRATE,
								android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
								android.Manifest.permission.CAMERA,
								//android.Manifest.permission.FLASHLIGHT,   // normal permission no need to request.
								//android.Manifest.permission.READ_PHONE_STATE,
								android.Manifest.permission.INTERNET,
								//android.Manifest.permission.ACCESS_COARSE_LOCATION,
								android.Manifest.permission.ACCESS_NETWORK_STATE,
								android.Manifest.permission.ACCESS_WIFI_STATE
						},
						MY_PERMISSIONS_REQUEST);

				// MY_PERMISSIONS_REQUEST is an app-defined int constant. The callback method gets the
				// result of the request.
			}
		} else {
			// permission has been granted. Do the task you need to do
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length >= 6
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED
						&& grantResults[1] == PackageManager.PERMISSION_GRANTED
						&& grantResults[2] == PackageManager.PERMISSION_GRANTED
						&& grantResults[3] == PackageManager.PERMISSION_GRANTED
						&& grantResults[4] == PackageManager.PERMISSION_GRANTED
						&& grantResults[5] == PackageManager.PERMISSION_GRANTED
				) {

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
				} else {
					// permission denied, boo! Still start the functionality.
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState)	{
		outState.putInt(SHOWN_PANEL, minputMethod.getSelectedPadIndex());
		outState.putBoolean(SHIFT_PRESSED, minputMethod.mbShiftKeyPressed);
		outState.putString(INPUT_BUFFER, minputMethod.mstrBuffered);
		outState.putString(LAST_TASK_TYPE, mstrarrayTaskAndOutput[0]);
		outState.putString(LAST_OUTPUT, mstrarrayTaskAndOutput[1]);
		outState.putInt(LAST_SOFTKEY_STATE, mnSoftKeyState);
		outState.putString(LAST_RECOGNIZED_RESULT, mstrRecognized);
		// mshistoricalRecMgr.flush(this);	// shouldn't done here because onSaveInstanceState() is not called
											// when you simply walk out of the activity using the back button.
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle inState) {
		//Restore last state
		super.onRestoreInstanceState(inState);
		
		//mhistoricalRecMgr.load(); // done in onCreate
		mstrRecognized = inState.getString(LAST_RECOGNIZED_RESULT);
		mnSoftKeyState = inState.getInt(LAST_SOFTKEY_STATE);
   		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
   		setSoftKeyState(txtInput, mnSoftKeyState);
   		mstrarrayTaskAndOutput[0] = inState.getString(LAST_TASK_TYPE);
   		mstrarrayTaskAndOutput[1] = inState.getString(LAST_OUTPUT);
		String strOutputProcessed =  mstrarrayTaskAndOutput[1].replace(SELECTED_STORAGE_PATH_UNIMATCH,
				AndroidStorageOptions.getSelectedStoragePath());
		if (mstrarrayTaskAndOutput[0].equalsIgnoreCase("recognize")
				|| mstrarrayTaskAndOutput[0].equalsIgnoreCase("recognize-plot")
				|| mstrarrayTaskAndOutput[0].equalsIgnoreCase("recognize-calculate")) {
	        String strConfirm = "<p>" + getString(R.string.please_confirm_recognized_result_and_calculation_result)
	        					+ "<a href=\"" + ActivitySmartCalc.AOPER_URL_HEADER + ActivitySmartCalc.EMAIL_UNSATISFACTORY_RECOG + "\">"
	        					+ getString(R.string.here) + "</a>" + getString(R.string.stop_charater) + "</p>"; 
	        
	        strOutputProcessed = strConfirm + strOutputProcessed;
		}
		
		WebView wvOutput = (WebView) findViewById(R.id.webviewSmartMathOutput);
		wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
				OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING + strOutputProcessed + OUTPUT_TAIL_STRING,
				"text/html", "utf-8", "");
		
		int nSelectedPadIndex = inState.getInt(SHOWN_PANEL);
		minputMethod.mstrBuffered = inState.getString(INPUT_BUFFER);
		boolean bShiftKeyPressed = inState.getBoolean(SHIFT_PRESSED);
		if (minputMethod.mbShiftKeyPressed != bShiftKeyPressed)	{
			setShiftKeyState(bShiftKeyPressed);
		}
		minputMethod.refreshInputMethod();
		minputMethod.showInputMethod(nSelectedPadIndex, false);	// do not clear input buffer here.
	}

	/**
	 * After a screen orientation change, this method is invoked.
	 * As we're going to state save the task, we can no longer associate
	 * it with the Activity that is going to be destroyed here.
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		Log.d("History", "in onRetainNonConfigurationInstance");
		masyncTask.setActivity(null);
		return masyncTask;
	}

	/**
	 * When the aSyncTask has notified the activity that it has completed,
	 * we can refresh the list control, and attempt to dismiss the dialog.
	 */
	public void onDismissProgDlg() {
		Log.i("ProgressDlg", "Activity " + this + " has been notified " + masyncTask.mstrTaskType + " task is complete.");

		//Check added because dismissDialog throws an exception if the current
		//activity hasn't shown it. This Happens if task finishes early enough
		//before an orientation change that the dialog is already gone when
		//the previous activity bundles up the dialogs to reshow.
		if ( mdlgTaskProgressing != null ) {
			mdlgTaskProgressing.dismiss();
			mdlgTaskProgressing = null;
		}
	}
	
	public void onCalcPlotCompleted(String strFormattedInput, long lTaskTriggerTime, String strTaskType, String strOutput)	{
		mstrarrayTaskAndOutput[0] = strTaskType;
		mstrarrayTaskAndOutput[1] = strOutput;
		// chart file name is added later on.
		String strChartFileName = "";
		if (strTaskType.equalsIgnoreCase("plot") || strTaskType.equalsIgnoreCase("recognize-plot"))	{
			strChartFileName = ActivityChartDemon.getChartFileName(
					ChartOperator.addEscapes(getString(R.string.chart_name_default)).trim(),
					lTaskTriggerTime);
		}
		String strOutputProcessed = mstrarrayTaskAndOutput[1].replace(SELECTED_STORAGE_PATH_UNIMATCH,
									AndroidStorageOptions.getSelectedStoragePath());
		WebView wvOutput = (WebView) findViewById(R.id.webviewSmartMathOutput);

		if (strTaskType.equalsIgnoreCase("recognize-plot") || strTaskType.equalsIgnoreCase("recognize-calculate")) {
			if (mshistoricalRecMgr.getRecordsLen() > 0 && mshistoricalRecMgr.getFirstRecord().mstrTaskType.equals("recognize")) {
				// change record because last recognized record is a temporary record for the recognize step.
				mshistoricalRecMgr.getFirstRecord().setHistoricalRecordItem(strFormattedInput, strTaskType, strOutput, strChartFileName);
			} else {
				mshistoricalRecMgr.addRecord(strFormattedInput, strTaskType, strOutput, strChartFileName, ActivitySettings.msnNumberofRecords);
				mshistoricalRecMgr.flush(this);	// flush should be called here because onPause may be called before onCalcPlotCompleted so that flush in onPause may happen before addRecord.
			}
	        String strConfirm = "<p>" + getString(R.string.please_confirm_recognized_result_and_calculation_result)
	        		+ "<a href=\"" + ActivitySmartCalc.AOPER_URL_HEADER + ActivitySmartCalc.EMAIL_UNSATISFACTORY_RECOG + "\">"
	        		+ getString(R.string.here) + "</a>" + getString(R.string.stop_charater) + "</p>"; 
			wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
						OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING + strConfirm + strOutputProcessed + OUTPUT_TAIL_STRING,
						"text/html", "utf-8", "");
		} else {
			mshistoricalRecMgr.addRecord(strFormattedInput, strTaskType, strOutput, strChartFileName, ActivitySettings.msnNumberofRecords);
			mshistoricalRecMgr.flush(this);	// flush should be called here because onPause may be called before onCalcPlotCompleted so that flush in onPause may happen before addRecord.
			wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
						OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING + strOutputProcessed + OUTPUT_TAIL_STRING,
						"text/html", "utf-8", "");
		}
		/* do not clear the input text box for next calculation */
		// EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		// txtInput.setText("");
	}
	
	public void setSoftKeyState(EditText edt, int nState)
	{
		if (nState == ENABLE_SHOW_INPUTPAD)	{	// use inputpad and inputpad is visible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
	        if (minputMethod != null)	{	// show inputpad.
	        	minputMethod.setVisibility(View.VISIBLE);
	        }
		} else if (nState == ENABLE_HIDE_INPUTPAD)	{	// use inputpad and inputpad is invisible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        }
		} else if (nState == ENABLE_HIDE_SOFTKEY)	{	// use soft keyboard and soft keyboard is invisible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);	// hide soft keyboard
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        }
		} else {	// nstate == ENABLE_SHOW_SOFTKEY, use soft keyboard and soft keyboard is visible.
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.showSoftInput(edt, 0);
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        }
		}
        mnSoftKeyState = nState;
	}
	
	@Override
	public LinkedList<ImageButton> genConvenientBtns(int nBtnHeight)	{	// nBtnHeight < 0 means wrap content
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nIconSelector = 0;	// 0 means use normal icon size, 1 means large, 2 means xlarge
		if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
				|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
			// do nothing coz by default it is set as normal icon size.
			nIconSelector = 0;
		} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
			nIconSelector = 1;
		} else	{	//XLarge
			nIconSelector = 2;
		}
		LinkedList<ImageButton> listImageBtns = new LinkedList<ImageButton>();
		ImageButton imgBtn1 = new ImageButton(this);
		imgBtn1.setBackgroundResource(R.drawable.convebtn_background);
		if (nBtnHeight >= 0)	{
			imgBtn1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, nBtnHeight, 1.0f));
		} else	{
			imgBtn1.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
		if (nIconSelector == 0)	{
			imgBtn1.setImageResource(R.drawable.quickhelp);
		} else if (nIconSelector == 1)	{
			imgBtn1.setImageResource(R.drawable.quickhelp_large);
		} else	{
			imgBtn1.setImageResource(R.drawable.quickhelp_xlarge);
		}
		imgBtn1.setId(R.id.btnQuickHelp);
		imgBtn1.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn1.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn1);
		ImageButton imgBtn2 = new ImageButton(this);
		imgBtn2.setBackgroundResource(R.drawable.convebtn_background);
		if (nBtnHeight >= 0)	{
			imgBtn2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, nBtnHeight, 1.0f));
		} else	{
			imgBtn2.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
		if (nIconSelector == 0)	{
			imgBtn2.setImageResource(R.drawable.cursorleft);
		} else if (nIconSelector == 1)	{
			imgBtn2.setImageResource(R.drawable.cursorleft_large);
		} else	{
			imgBtn2.setImageResource(R.drawable.cursorleft_xlarge);
		}
		imgBtn2.setId(R.id.btnCursorLeft);
		imgBtn2.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn2.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn2);
		ImageButton imgBtn3 = new ImageButton(this);
		imgBtn3.setBackgroundResource(R.drawable.convebtn_background);
		if (nBtnHeight >= 0)	{
			imgBtn3.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, nBtnHeight, 1.0f));
		} else	{
			imgBtn3.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
		if (nIconSelector == 0)	{
			imgBtn3.setImageResource(R.drawable.cursorright);
		} else if (nIconSelector == 1)	{
			imgBtn3.setImageResource(R.drawable.cursorright_large);
		} else	{
			imgBtn3.setImageResource(R.drawable.cursorright_xlarge);
		}
		imgBtn3.setId(R.id.btnCursorRight);
		imgBtn3.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn3.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn3);
		ImageButton imgBtn4 = new ImageButton(this);
		imgBtn4.setBackgroundResource(R.drawable.convebtn_background);
		if (nBtnHeight >= 0)	{
			imgBtn4.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, nBtnHeight, 1.0f));
		} else	{
			imgBtn4.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
		if (nIconSelector == 0)	{
			imgBtn4.setImageResource(R.drawable.delete);
		} else if (nIconSelector == 1)	{
			imgBtn4.setImageResource(R.drawable.delete_large);
		} else	{
			imgBtn4.setImageResource(R.drawable.delete_xlarge);
		}
		imgBtn4.setId(R.id.btnDEL);
		imgBtn4.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn4.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn4);
		ImageButton imgBtn5 = new ImageButton(this);
		imgBtn5.setBackgroundResource(R.drawable.convebtn_background);
		if (nBtnHeight >= 0)	{
			imgBtn5.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, nBtnHeight, 1.0f));
		} else	{
			imgBtn5.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
		if (nIconSelector == 0)	{
			imgBtn5.setImageResource(R.drawable.enter);
		} else if (nIconSelector == 1)	{
			imgBtn5.setImageResource(R.drawable.enter_large);
		} else	{
			imgBtn5.setImageResource(R.drawable.enter_xlarge);
		}
		imgBtn5.setId(R.id.btnEnter);
		imgBtn5.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn5.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn5);
		ImageButton imgBtn6 = new ImageButton(this);
		imgBtn6.setBackgroundResource(R.drawable.convebtn_background);
		if (nBtnHeight >= 0)	{
			imgBtn6.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, nBtnHeight, 1.0f));
		} else	{
			imgBtn6.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1.0f));
		}
		if (nIconSelector == 0)	{
			imgBtn6.setImageResource(R.drawable.inputpadhide);
		} else if (nIconSelector == 1)	{
			imgBtn6.setImageResource(R.drawable.inputpadhide_large);
		} else	{
			imgBtn6.setImageResource(R.drawable.inputpadhide_xlarge);
		}
		imgBtn6.setId(R.id.btnHideInputPad);
		imgBtn6.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn6.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn6);
		
		final EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);

		//-------------- set actions for convenient buttons ---------------
		OnClickListener lConvenBtnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ActivitySettings.msbEnableBtnPressVibration)	{
					Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
					myVibrator.vibrate(ActivitySettings.VIBERATION_LENGTH);
				}
				int nSelectionStart = txtInput.getSelectionStart();
				int nSelectionEnd = txtInput.getSelectionEnd();
				if (nSelectionStart > nSelectionEnd) {
					int nSelectionSwap = nSelectionEnd;
					nSelectionStart = nSelectionEnd;
					nSelectionEnd = nSelectionSwap;
				}
				if (v.getId() == R.id.btnCursorLeft)	{
					if (nSelectionStart > 0)	{
						txtInput.setSelection(nSelectionStart - 1, nSelectionStart - 1);
					} else	{
						txtInput.setSelection(nSelectionStart, nSelectionStart);
					}
				} else if (v.getId() == R.id.btnCursorRight)	{
					if (nSelectionEnd < txtInput.length())	{
						txtInput.setSelection(nSelectionEnd + 1, nSelectionEnd + 1);
					} else	{
						txtInput.setSelection(nSelectionEnd, nSelectionEnd);
					}
				} else if (v.getId() == R.id.btnDEL)	{
					if (minputMethod.isInputBufferEmpty())	{
						if (nSelectionStart < nSelectionEnd)	{	// remove selection
							txtInput.getText().replace(nSelectionStart, nSelectionEnd, "");
							txtInput.setSelection(nSelectionStart);
						} else if (nSelectionStart > 0)	{	// remove one char
							txtInput.getText().replace(nSelectionStart - 1, nSelectionStart, "");
							txtInput.setSelection(nSelectionStart - 1);
						}
					} else	{
						minputMethod.typeDelete4InputBuffer();
					}
				} else if (v.getId() == R.id.btnEnter)	{
					if (minputMethod.isInputBufferEmpty())	{
						int nNumofEnters = 0;
						for (int idx = 0; idx < txtInput.getText().toString().length(); idx ++)	{
							if (txtInput.getText().toString().charAt(idx) == '\n')	{
								nNumofEnters ++;
							}
						}
						if (nNumofEnters < MAX_INPUT_ROWS - 1)	{	// now less than MAX_INPUT_ROWS lines.
							txtInput.getText().replace(nSelectionStart, nSelectionEnd, "\n");
							txtInput.setSelection(nSelectionStart + 1, nSelectionStart + 1);
						}
					} else {
						minputMethod.typeEnter4InputBuffer();
					}
				} else if (v.getId() == R.id.btnQuickHelp)	{
					if (minputMethod.isInputBufferEmpty())	{
						txtInput.setText("Help ");
						txtInput.setSelection(txtInput.length());
					} else	{
						minputMethod.typeHelp4InputBuffer();
					}
				} else	{	// inputpad hide
					setSoftKeyState(txtInput, ActivitySmartCalc.ENABLE_HIDE_INPUTPAD);
				}
			}
		};
		
		OnLongClickListener lConvenBtnLongClick = new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (ActivitySettings.msbEnableBtnPressVibration)	{
					Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
					myVibrator.vibrate(ActivitySettings.VIBERATION_LENGTH);
				}
				String strTextInBox = txtInput.getText().toString();
				if (v.getId() == R.id.btnCursorLeft) {
					int nNewSelectionStart = txtInput.getSelectionStart();
					if (nNewSelectionStart > 0 && strTextInBox.charAt(nNewSelectionStart - 1) == '\n')	{
						nNewSelectionStart --;
					}
					while (nNewSelectionStart > 0)	{
						if (strTextInBox.charAt(nNewSelectionStart - 1) != '\n')	{
							nNewSelectionStart --;
						} else	{
							break;
						}
					}
					txtInput.setSelection(nNewSelectionStart);
					return true;
				} else if (v.getId() == R.id.btnCursorRight) {
					int nNewSelectionEnd = txtInput.getSelectionEnd();
					if (nNewSelectionEnd < strTextInBox.length())	{
						if (strTextInBox.charAt(nNewSelectionEnd) == '\n')	{
							nNewSelectionEnd ++;
						}
						while (nNewSelectionEnd < strTextInBox.length())	{
							if (strTextInBox.charAt(nNewSelectionEnd) != '\n')	{
								nNewSelectionEnd ++;
							} else	{
								break;
							}
						}
					}
					txtInput.setSelection(nNewSelectionEnd);
					return true;
				} else if (v.getId() == R.id.btnDEL) {	// delete line by line
					if (minputMethod.isInputBufferEmpty())	{
						int nSelectionStart = txtInput.getSelectionStart();
						int nSelectionEnd = txtInput.getSelectionEnd();
						int nNewSelectionStart = nSelectionStart;
						int nNewSelectionEnd = nSelectionEnd;
						if (nNewSelectionStart == nSelectionEnd)	{
							if (nNewSelectionEnd < strTextInBox.length() && strTextInBox.charAt(nNewSelectionEnd) == '\n')	{
								if (nNewSelectionStart > 0)	{
									nNewSelectionStart --;
								} else	{
									nNewSelectionEnd ++;	// need not to check if nNewSelectionEnd < string length or not because it cannot be string end.
								}
							} else if (nNewSelectionEnd == strTextInBox.length())	{
								if (nNewSelectionStart > 0)	{
									nNewSelectionStart --;
								}
							}
						}
						while (nNewSelectionStart > 0 && nNewSelectionStart < strTextInBox.length() && strTextInBox.charAt(nNewSelectionStart) != '\n')	{
							nNewSelectionStart --;
						}
						while (nNewSelectionEnd < strTextInBox.length() && strTextInBox.charAt(nNewSelectionEnd) != '\n')	{
							nNewSelectionEnd ++;
						}
						txtInput.getText().replace(nNewSelectionStart, nNewSelectionEnd, "");
						txtInput.setSelection(nNewSelectionStart);
					} else	{
						minputMethod.clearInputBuffer();
					}
					return true;
				} else {	// hide input pad, start and enter does nothing
					return false;
				}
			}
		};

		for (ImageButton imgBtn : listImageBtns)	{
			imgBtn.setOnClickListener(lConvenBtnClick);
			imgBtn.setOnLongClickListener(lConvenBtnLongClick);
		}		
		
		return listImageBtns;
	}
	
	@Override
	public LinkedList<TableInputPad> getInputPads()
	{
        InputStream inputStreamPads = null;
		try	{
			inputStreamPads = getAssets().open(IMMUTABLE_INPUTPAD_CONFIG);
		} catch (IOException e) {
			// unsuccessful, load nothing.
		}
		LinkedList<TableInputPad> listSCInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
		
		try {
			// first try to read input pads config file in SD card
			inputStreamPads = new FileInputStream(MFPFileManagerActivity.getConfigFolderFullPath()
												+ MFPFileManagerActivity.STRING_PATH_DIV
												+ ActivityCfgKeyPad.INPUTPAD_SC_CONFIG);
		} catch (FileNotFoundException e0) {
			try	{
				// if unsuccessful, try to read input pads config file in assets
				inputStreamPads = getAssets().open(ActivityCfgKeyPad.INPUTPAD_SC_CONFIG);
			} catch (IOException e) {
				// still unsuccessful, load nothing.
			}
		}
		LinkedList<TableInputPad> listInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
		listSCInputPads.addAll(listInputPads);
		return listSCInputPads;
	}
	
	@Override
	public int calcIMEMaxHeight()	{
		return -1; // do not use screen height times 0.5, smart calculator should use max height should be match parent.
	}
	
	@Override
	public int calcInputKeyBtnHeight()	{
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenOrientation = getResources().getConfiguration().orientation;

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int nScreenShortSideInPx = Math.min(metrics.heightPixels, metrics.widthPixels);
		int nScreenLongSideInPx = Math.max(metrics.heightPixels, metrics.widthPixels);
		int nBtnHeight;
		if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
			nBtnHeight = nScreenShortSideInPx;
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{
				nBtnHeight /= 6;
			} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
				nBtnHeight /= 8;
			} else	{
				// large and xlarge
				nBtnHeight /= 12;	// because the ads are on top
			}
		} else	{
			nBtnHeight = nScreenLongSideInPx;
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{
				nBtnHeight /= 9;
			} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
				nBtnHeight /= 12;
			} else	{
				// large and xlarge
				nBtnHeight /= 18;
			}
		}
		return nBtnHeight;
	}
	
	@Override
	public float calcInputKeyTextSize()	{
		return (float)(calcInputKeyBtnHeight()/2);
	}
	
	@Override
	public void onClickSelectedWord(String str)	{
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		int nSelectionStart = txtInput.getSelectionStart();
		int nSelectionEnd = txtInput.getSelectionEnd();
		if (str.length() > 0)	{
			// ensure that some text will be changed
			String strInputLowerCase = txtInput.getText().toString().toLowerCase(Locale.US);
			if (strInputLowerCase.trim().equals("help")	// This should be first to avoid the case where string length = 0.
					&& strInputLowerCase.charAt(strInputLowerCase.length() - 1) == ' ') {
				// this is to input help.
				String strTxt2InputProc = str.trim();
				if (strTxt2InputProc.equals("(") == false)	{
					strTxt2InputProc = strTxt2InputProc.split("\\(")[0];
				}
				txtInput.append(strTxt2InputProc);
				txtInput.setSelection(txtInput.getText().length());
				minputMethod.clearInputBuffer();
			} else {
				txtInput.getText().replace(nSelectionStart, nSelectionEnd, str);
				txtInput.setSelection(nSelectionStart + str.length());
				minputMethod.clearInputBuffer();
				// may need to move to another input pad.
				String strTrimmed = str.trim();
				if (strTrimmed.length() > 0
						&& (strTrimmed.charAt(strTrimmed.length() - 1) == '(' || strTrimmed.charAt(strTrimmed.length() - 1) == '['))	{
					int idx = 0;
					for (TableInputPad inputPad : minputMethod.mlistShownInputPads)	{
						if (inputPad.mstrName.equals("numbers_operators"))	{
							minputMethod.showInputMethod(idx, true);
							break;
						}
						idx ++;
					}
				}
			}
		} else	{
			// not in the editable area or nothing to input.
			minputMethod.clearInputBuffer();
		}
	}
	
	@SuppressLint("DefaultLocale")
	@Override
	public void onClickKey(View v)	{
		if (ActivitySettings.msbEnableBtnPressVibration)	{
			Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
			myVibrator.vibrate(ActivitySettings.VIBERATION_LENGTH);
		}
		if (v.getTag() == null || !(v.getTag() instanceof InputKey))	{
			return;	// does not include any valid inputkey info.
		}
		InputKey inputKeyDef = (InputKey)(v.getTag());
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		int nSelectionStart = txtInput.getSelectionStart();
		int nSelectionEnd = txtInput.getSelectionEnd();
		if (inputKeyDef.isFunctionKey() == false)	{	// normal text key
			String strText2Input = minputMethod.type4InputBuffer(inputKeyDef.mstrKeyInput);
			int nCursorPlace = inputKeyDef.mnCursorPlace;
			
			if (strText2Input.length() > 0)	{
				// ensure that some text will be changed
				String strInputLowerCase = txtInput.getText().toString().toLowerCase(Locale.US);
				if (strInputLowerCase.trim().equals("help")	// This should be first to avoid the case where string length = 0.
						&& strInputLowerCase.charAt(strInputLowerCase.length() - 1) == ' ') {
					// this is to input help.
					String strTxt2InputProc = strText2Input.trim();
					if (strTxt2InputProc.equals("(") == false)	{
						strTxt2InputProc = strTxt2InputProc.split("\\(")[0];
					}
					txtInput.append(strTxt2InputProc);
					txtInput.setSelection(txtInput.getText().length());
				} else	{
					txtInput.getText().replace(nSelectionStart, nSelectionEnd, strText2Input);
					int nNewSelectionStart = nSelectionStart + strText2Input.length() + nCursorPlace;
					if (nNewSelectionStart < 0)	{
						nNewSelectionStart = 0;
					}
					if (nNewSelectionStart > txtInput.getText().length())	{
						nNewSelectionStart = txtInput.getText().length();
					}
					txtInput.setSelection(nNewSelectionStart);
					if (!inputKeyDef.mparent.mparent.mstrName.equals("abc_keyboard") && !inputKeyDef.mparent.mparent.mstrName.equals("numbers_operators"))	{
						// may need to move to another input pad.
						int idx = 0;
						for (TableInputPad inputPad : minputMethod.mlistShownInputPads)	{
							if (inputPad.mstrName.equals("numbers_operators"))	{
								minputMethod.showInputMethod(idx, true);
								break;
							}
							idx ++;
						}
					}
				}
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("SHIFT"))	{
			// InputKey.DEFAULT_FOREGROUND_COLOR is the color b4 press, so if it is DEFAULT_FOREGROUND_COLOR, means shift key is pressed
			boolean bShiftKeyPressed = inputKeyDef.mcolorForeground.isEqual(InputKey.DEFAULT_FOREGROUND_COLOR);
			setShiftKeyState(bShiftKeyPressed);
			minputMethod.refreshInputMethod();
			minputMethod.showInputMethod(minputMethod.getSelectedPadIndex(), false);
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("ENTER"))	{
			if (minputMethod.isInputBufferEmpty())	{
				int nNumofEnters = 0;
				for (int idx = 0; idx < txtInput.getText().toString().length(); idx ++)	{
					if (txtInput.getText().toString().charAt(idx) == '\n')	{
						nNumofEnters ++;
					}
				}
				if (nNumofEnters < MAX_INPUT_ROWS - 1)	{	// now less than MAX_INPUT_ROWS lines.
					txtInput.getText().replace(nSelectionStart, nSelectionEnd, "\n");
					txtInput.setSelection(nSelectionStart + 1, nSelectionStart + 1);
				}
			} else	{
				minputMethod.typeEnter4InputBuffer();
			}
			
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("HELP"))	{
			if (minputMethod.isInputBufferEmpty())	{
				txtInput.setText("Help ");
				txtInput.setSelection(txtInput.length());
			} else	{
				minputMethod.typeHelp4InputBuffer();
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("DELETE"))	{
			if (minputMethod.isInputBufferEmpty())	{
				if (nSelectionStart < nSelectionEnd)	{	// remove selection
					txtInput.getText().replace(nSelectionStart, nSelectionEnd, "");
					txtInput.setSelection(nSelectionStart);
				} else if (nSelectionStart > 0)	{	// remove one char
					txtInput.getText().replace(nSelectionStart - 1, nSelectionStart, "");
					txtInput.setSelection(nSelectionStart - 1);
				}
			} else	{
				minputMethod.typeDelete4InputBuffer();
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("HIDE"))	{
			setSoftKeyState(txtInput, ActivitySmartCalc.ENABLE_HIDE_INPUTPAD);
		}
	}
		
	@Override
	public LinkedList<String> matchFuncVars(String str)	{
		LinkedList<String> listMatched = new LinkedList<String>();
		if (str == null || str.length() == 0)	{
			return listMatched;
		}
		// first search internal functions:
		ListIterator<InternalFuncInfo> itrIFI = MFPAdapter.m_slInternalFuncInfo.listIterator();
		while (itrIFI.hasNext())	{
			InternalFuncInfo ifi = itrIFI.next();
			if (ifi.mstrFuncName.length() >= str.length() && ifi.mstrFuncName.substring(0, str.length()).equalsIgnoreCase(str))	{
				// find it.
				boolean bHasBeenInList = false;
				String strMatched = ifi.mstrFuncName + "(";
				int idx = 0;
				for (; idx < listMatched.size(); idx ++)	{
					if (strMatched.length() < listMatched.get(idx).length())	{
						break;
					} else if (strMatched.length() > listMatched.get(idx).length())	{
						continue;
					} else if (strMatched.compareToIgnoreCase(listMatched.get(idx)) < 0)	{
						break;
					} else if (strMatched.compareToIgnoreCase(listMatched.get(idx)) > 0)	{
						continue;
					} else	{	// it has been there.
						bHasBeenInList = true;
						break;
					}
				}
				if (bHasBeenInList == false)	{
					listMatched.add(idx, strMatched);
				}
			}
		}
		// then predefined functions:
		ListIterator<FunctionEntry> itrFE = MFPAdapter.m_slFunctionSpace.listIterator();
		while (itrFE.hasNext())	{
			FunctionEntry fe = itrFE.next();
			if (fe.m_sf.m_strFunctionName.length() >= str.length() && fe.m_sf.m_strFunctionName.substring(0, str.length()).equalsIgnoreCase(str))	{
				// find it.
				boolean bHasBeenInList = false;
				String strMatched = fe.m_sf.m_strFunctionName + "(";
				int idx = 0;
				for (; idx < listMatched.size(); idx ++)	{
					if (strMatched.length() < listMatched.get(idx).length())	{
						break;
					} else if (strMatched.length() > listMatched.get(idx).length())	{
						continue;
					} else if (strMatched.compareToIgnoreCase(listMatched.get(idx)) < 0)	{
						break;
					} else if (strMatched.compareToIgnoreCase(listMatched.get(idx)) > 0)	{
						continue;
					} else	{	// it has been there.
						bHasBeenInList = true;
						break;
					}
				}
				if (bHasBeenInList == false)	{
					listMatched.add(idx, strMatched);
				}
			}
		}

		// then constant variables
		LinkedList<String> listConstVarNames = new LinkedList<String>();
		listConstVarNames.add("null");
		listConstVarNames.add("true");
		listConstVarNames.add("false");
		listConstVarNames.add("pi");
		listConstVarNames.add("e");
		listConstVarNames.add("inf");
		listConstVarNames.add("infi");
		listConstVarNames.add("nan");
		listConstVarNames.add("nani");
		ListIterator<String> itrPreDef = listConstVarNames.listIterator();
		while (itrPreDef.hasNext())	{
			String strConstVarName = itrPreDef.next();
			if (strConstVarName.length() >= str.length() && strConstVarName.substring(0, str.length()).equalsIgnoreCase(str))	{
				// find it.
				boolean bHasBeenInList = false;
				String strMatched = strConstVarName;
				int idx = 0;
				for (; idx < listMatched.size(); idx ++)	{
					if (strMatched.length() < listMatched.get(idx).length())	{
						break;
					} else if (strMatched.length() > listMatched.get(idx).length())	{
						continue;
					} else if (strMatched.compareToIgnoreCase(listMatched.get(idx)) < 0)	{
						break;
					} else if (strMatched.compareToIgnoreCase(listMatched.get(idx)) > 0)	{
						continue;
					} else	{	// it has been there.
						bHasBeenInList = true;
						break;
					}
				}
				if (bHasBeenInList == false)	{
					listMatched.add(idx, strMatched);
				}
			}
		}
		
		listMatched.addFirst(str); 	// then add itself.
		return listMatched;
	}
	
	@Override
	public boolean isFlushBufferString(String str)	{
		boolean bIsFlushInput = true;
		for (char c : str.toCharArray())	{
			if (c != ',' && c != ' ' && c != '\n' && c != '\t' && c != '\r'
					&& c != '(' && c != ')' && c!= '[' && c != ']')	{
				bIsFlushInput = false;
				break;
			}
		}
		return bIsFlushInput;
	}
	
	@Override
	public void setShiftKeyState(boolean bShiftKeyPressed)	{
		int idx = 0;
		boolean bFoundAbcKeyboard = false;
		for (TableInputPad inputPad : minputMethod.mlistShownInputPads)	{
			if (inputPad.mstrName.equals("abc_keyboard"))	{
				bFoundAbcKeyboard = true;
				break;
			}
			idx ++;
		}
		if (!bFoundAbcKeyboard)	{
			return;	// cannot find abc_keyboard, return.
		}
		LinkedList<InputKey> listOfAllKeys = minputMethod.mlistShownInputPads.get(idx).getListOfKeys();
		listOfAllKeys.addAll(minputMethod.mlistShownInputPads.get(idx).getListOfKeysLand());
		minputMethod.mbShiftKeyPressed = bShiftKeyPressed;
		if (bShiftKeyPressed)	{
			for (InputKey inputKey : listOfAllKeys)	{
				if (inputKey.mstrKeyFunction.equalsIgnoreCase("SHIFT"))	{
					inputKey.mstrKeyShown = inputKey.mstrKeyShown.toUpperCase(Locale.US);
					inputKey.mcolorForeground = Color.BLUE;
				} else if (inputKey.mstrKeyName.length() == 19 && inputKey.mstrKeyName.substring(0, 5).equals("char_")
						&& inputKey.mstrKeyName.substring(6).equals("_abc_keyboard"))	{
					inputKey.mstrKeyShown = inputKey.mstrKeyShown.toUpperCase(Locale.US);
					inputKey.mstrKeyInput = inputKey.mstrKeyInput.toUpperCase(Locale.US);
				}
			}
		} else	{
			for (InputKey inputKey : listOfAllKeys)	{
				if (inputKey.mstrKeyFunction.equalsIgnoreCase("SHIFT"))	{
					inputKey.mstrKeyShown = inputKey.mstrKeyShown.toLowerCase(Locale.US);
					inputKey.mcolorForeground = InputKey.DEFAULT_FOREGROUND_COLOR;
				} else if (inputKey.mstrKeyName.length() == 19 && inputKey.mstrKeyName.substring(0, 5).equals("char_")
						&& inputKey.mstrKeyName.substring(6).equals("_abc_keyboard"))	{
					inputKey.mstrKeyShown = inputKey.mstrKeyShown.toLowerCase(Locale.US);
					inputKey.mstrKeyInput = inputKey.mstrKeyInput.toLowerCase(Locale.US);
				}
			}
		}
	}
	
	@Override
	public void hideInputMethod()	{
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		minputMethod.clearInputBuffer();	// flush buffer only used in shift to another inputpad.
		setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);
	}
	
    public static String formatInput(String strExpressions)
    {
		String[] strarrayExprs = strExpressions.split("\n");
		String strFormatted = "";
		for (int idx = 0; idx < strarrayExprs.length; idx ++)	{
			String strThisExpr = strarrayExprs[idx].trim();
			if (strThisExpr.length() == 0)	{
				continue;	// empty string
			} else if (strThisExpr.toLowerCase(Locale.US).equals("help"))	{
				strFormatted = "help";
			} else if (strThisExpr.length() >= 5 && strThisExpr.substring(0, 5).toLowerCase(Locale.US).equals("help "))	{
				strFormatted = "help" + " " + strThisExpr.substring(5).trim() + "\n";
			} else	{
				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				AbstractExpr aexpr = new AEInvalid();
				try	{
                    /* evaluate the expression */
                    aexpr = ExprAnalyzer.analyseExpression(strThisExpr, curpos);
                    strFormatted += aexpr.toString() + "\n";	// to String throw exceptions
				} catch (Exception e)	{
					strFormatted += strThisExpr + "\n";
				}
			}
		}
		return strFormatted;
    }
    
	public void analyze(String strTaskType)
	{
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		masyncTask = new AsyncTaskManager(this);
		masyncTask.mstrTaskType = strTaskType;
		String[] strlistArgs = new String[1];
		strlistArgs[0] = txtInput.getText().toString();
		masyncTask.execute(strlistArgs);	// do calculating asynchronously
	}
	
	@Override
	/*
	 * Create three menus: History, Settings and Help
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM0, 0, getString(R.string.menu_calcassist));
		menu.add(0, ITEM1, 0, getString(R.string.menu_chartmgr));
		menu.add(0, ITEM2, 0, getString(R.string.menu_plotcomplicatedgraph));
		menu.add(0, ITEM3, 0, getString(R.string.menu_integrate));
		menu.add(0, ITEM4, 0, getString(R.string.menu_history));
		menu.add(0, ITEM5, 0, getString(R.string.menu_alwayshidesoftkeyboard));			
		menu.add(0, ITEM6, 0, getString(R.string.menu_enablesoftkeyboard));			
		menu.add(0, ITEM7, 0, getString(R.string.menu_settings));
		menu.add(0, ITEM8, 0, getString(R.string.menu_help));

		return true;
	}
	
	//Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
		menu.add(0, ITEM0, 0, getString(R.string.menu_calcassist));
		menu.add(0, ITEM1, 0, getString(R.string.menu_chartmgr));
		menu.add(0, ITEM2, 0, getString(R.string.menu_plotcomplicatedgraph));
		menu.add(0, ITEM3, 0, getString(R.string.menu_integrate));
		menu.add(0, ITEM4, 0, getString(R.string.menu_history));
		if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
			menu.add(0, ITEM5, 0, getString(R.string.menu_alwayshidesoftkeyboard));			
		} else	{
			menu.add(0, ITEM6, 0, getString(R.string.menu_enablesoftkeyboard));			
		}
		menu.add(0, ITEM7, 0, getString(R.string.menu_settings));
		menu.add(0, ITEM8, 0, getString(R.string.menu_help));
        return super.onPrepareOptionsMenu(menu);

    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM0:
			actionClickMenuCalcAssist();
			break;
		case ITEM1: 
			actionClickMenuChartMgr();
			break;
		case ITEM2: 
			actionClickMenuPlotComplicatedGraph();
			break;
		case ITEM3: 
			actionClickMenuIntegrate();
			break;
		case ITEM4: 
			actionClickMenuHistory();
			break;
		case ITEM5: 
		case ITEM6: 
			actionClickMenuSoftKey();
			break;
		case ITEM7: 
			actionClickMenuSettings();
			break;
		case ITEM8: 
			actionClickMenuHelp();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Start calculation assistant (read constant values or convert units).
	 */
	private void actionClickMenuCalcAssist()	{
		Intent intentCalcAssistant = new Intent(this, ActivityCalcAssistant.class);
	   	Bundle bundle = new Bundle();
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
	   	bundle.putString("This_Input", txtInput.getText().toString());
	   	//Add this bundle to the intent
	   	intentCalcAssistant.putExtras(bundle);
		startActivityForResult(intentCalcAssistant,
							CALCULATOR_ASSISTANT_ACTIVITY);
	}
	
	/*
	 * Show record history list
	 */
	private void actionClickMenuHistory(){
	   	Intent intentRecordList = new Intent(this,ActivitySmCRecord.class);	
		
	   	String strAllRecords = mshistoricalRecMgr.genXMLFromRecords(mstrFontSize, getString(R.string.no_records));
	   	Bundle bundle = new Bundle();
	   	bundle.putString("HISTORY_CONTEXT", strAllRecords);
	   	intentRecordList.putExtras(bundle);
		startActivityForResult(intentRecordList, HISTORICAL_LIST_ACTIVITY);
	}

	/*
	 * Chart manager
	 */
	private void actionClickMenuChartMgr(){
	   	Intent intentScriptChartManager = new Intent(this, MFPFileManagerActivity.class);
	   	Bundle bundleChart = new Bundle();
        String strChartFileFolder = MFPFileManagerActivity.getChartFolderFullPath();
        bundleChart.putString("FILE_FOLDER", strChartFileFolder);
        bundleChart.putInt("MODE", MFPFileManagerActivity.START_FILE_MANAGER_BY_ITSELF);
        bundleChart.putString("FILE_SHOWN_FILTER", MFPFileManagerActivity.STRING_CHART_EXTENSION);
        intentScriptChartManager.putExtras(bundleChart);
	   	startActivity(intentScriptChartManager);
	}
	
	/*
	 * plot normal-coordinate graph
	 */
	private void actionClickMenuPlotComplicatedGraph(){
		LayoutInflater inflater = LayoutInflater.from(this);
		final View viewChoosePlotter = inflater.inflate(R.layout.graph_plotter_chooser, null);
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		AlertDialog alertDialog
			= blder.setIcon(R.drawable.icon)
				.setTitle(getString(R.string.choose_graph_plotter))
				.setView(viewChoosePlotter)
				.setPositiveButton(R.string.ok, 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							RadioGroup radioGrp = (RadioGroup) viewChoosePlotter.findViewById(R.id.radioGrp_plotter_type);
							if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_2d_graph_plotter) {
								startActivity(new Intent(ActivitySmartCalc.this, ActivityPlotXYGraph.class));
							} else if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_polar_graph_plotter) {
								startActivity(new Intent(ActivitySmartCalc.this, ActivityPlotPolarGraph.class));
							} else	{
								startActivity(new Intent(ActivitySmartCalc.this, ActivityPlotXYZGraph.class));
							}
						}
					})
				.setNegativeButton(R.string.cancel, 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							return;
						}
					})
				.create();
		alertDialog.show();
	}
	
	/*
	 * Show integrate activity
	 */
	private void actionClickMenuIntegrate(){
	   	Intent intentIntegrate = new Intent(this,ActivityIntegrate.class);	 
	   	startActivity(intentIntegrate);
	}
	
	/*
	 * Enable or hide soft keyboard
	 */
	private void actionClickMenuSoftKey(){
   		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
	   	if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
            // code to hide the soft keyboard
            setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);
	   	} else	{
	   		// code to enable the soft keyboard
	   		setSoftKeyState(txtInput, ENABLE_SHOW_SOFTKEY);
	   	}
	}
	
	/*
	 * Show settings
	 */
	private void actionClickMenuSettings(){
	   	Intent intentSettings = new Intent(this,ActivitySettings.class);	 
	   	startActivity(intentSettings);
	}
	
	/*
	 * Show help
	 */
	private void actionClickMenuHelp(){
	   	Intent intentHelp = new Intent(this,ActivityShowHelp.class);  
	   	Bundle bundle = new Bundle();
	   	bundle.putString("HELP_CONTENT", "main");
	   	//Add this bundle to the intent
	   	intentHelp.putExtras(bundle);
	   	startActivity(intentHelp);
	}
	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		switch(requestCode) { 
		case (HISTORICAL_LIST_ACTIVITY) : { 
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getExtras();
				if (bundle != null && bundle.getString("Record") != null) {
					/* we just come back from historical results */
					insert2Input(bundle.getString("Record"));
				}
			} 
			break; 
		} case (CFG_KEYPAD_ACTIVITY) :	{
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getBundleExtra("android.intent.extra.FunCalc");
				if (bundle != null && bundle.getBoolean("CfgFileChanged")) {
					EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
					int nLastSelectedPadIndex = minputMethod.getSelectedPadIndex();
					String strInputBuf = minputMethod.mstrBuffered;
					boolean bShiftKeyPressed = minputMethod.mbShiftKeyPressed;
			        LinearLayout layoutIMEHolder = (LinearLayout) findViewById(R.id.linearLayoutSMInputPad);
			        layoutIMEHolder.removeAllViews();
					minputMethod = new AMInputMethod(this);
					layoutIMEHolder.addView(minputMethod);
					minputMethod.initialize(this, txtInput);
					// restore last state.
					if (bShiftKeyPressed)	{
						setShiftKeyState(bShiftKeyPressed);
					}
					minputMethod.mstrBuffered = strInputBuf;
					minputMethod.refreshInputMethod();
					minputMethod.showInputMethod(nLastSelectedPadIndex, false);	// do not flush input buffer
				}
			}
			break;
		} case (CALCULATOR_ASSISTANT_ACTIVITY) :	{
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getBundleExtra("android.intent.extra.FunCalc");
				if (bundle != null)	{
					String strReturn = "";
					if (bundle.getString("ConstantValue") != null) {
						strReturn = bundle.getString("ConstantValue");
					} else if (bundle.getString("ConstantFunction") != null) {
						strReturn = bundle.getString("ConstantFunction");
					} else if (bundle.getString("ConvertedValue") != null) {
						strReturn = bundle.getString("ConvertedValue");
					} else if (bundle.getString("ConvertFunction") != null) {
						strReturn = bundle.getString("ConvertFunction");
					}
					if (strReturn.length() > 0)	{	// valid return
						/* we just come back from any one of calculator assistant results */
						insert2Input(strReturn);
					}
				}
			} 
			break; 
		} case (GRAPH_DEMON_ACTIVITY) :	{
			if (resultCode == Activity.RESULT_OK) { 
				Bundle bundle = data.getBundleExtra("android.intent.extra.FunCalc");
				if (bundle != null)	{
					/*int nChartWidth = bundle.getInt("ChartWidth");
					int nChartHeight = bundle.getInt("ChartHeight");
					long lPlotTS = bundle.getLong("PlotTimeStamp");*/
					if (mshistoricalRecMgr.getRecordsLen() > 0)	{
						HistoricalRecordItem hri = mshistoricalRecMgr.getFirstRecord();
						String strTaskType = hri.mstrTaskType;
						String strOutputProcessed = hri.mstrOutput.replace(SELECTED_STORAGE_PATH_UNIMATCH,
															AndroidStorageOptions.getSelectedStoragePath());
						if (strTaskType.equalsIgnoreCase("recognize")
								|| strTaskType.equalsIgnoreCase("recognize-plot")
								|| strTaskType.equalsIgnoreCase("recognize-calculate")) {
					        String strConfirm = "<p>" + getString(R.string.please_confirm_recognized_result_and_calculation_result)
					        					+ "<a href=\"" + AOPER_URL_HEADER + EMAIL_UNSATISFACTORY_RECOG + "\">"
					        					+ getString(R.string.here) + "</a>" + getString(R.string.stop_charater) + "</p>"; 
					        
					        strOutputProcessed = strConfirm + strOutputProcessed;
						}
						if (masyncTask.mactivity != null)	{
							masyncTask.mactivity.mstrarrayTaskAndOutput[0] = hri.mstrTaskType;
							masyncTask.mactivity.mstrarrayTaskAndOutput[1] = hri.mstrOutput;
							WebView wvOutput = (WebView) masyncTask.mactivity.findViewById(R.id.webviewSmartMathOutput);
							wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
										OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING
											+ strOutputProcessed + OUTPUT_TAIL_STRING,
										"text/html", "utf-8", "");
						} else	{
							mstrarrayTaskAndOutput[0] = hri.mstrTaskType;
							mstrarrayTaskAndOutput[1] = hri.mstrOutput;
							WebView wvOutput = (WebView) findViewById(R.id.webviewSmartMathOutput);
							wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
										OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING
											+ strOutputProcessed + OUTPUT_TAIL_STRING,
										"text/html", "utf-8", "");
						}
					}
				}
			} 
			break; 
		} case (CAPTURE_IMAGE_ACTIVITY) :	{
	        if (resultCode == RESULT_OK) {
	        	Bundle bundle = data.getExtras();
	        	int nActionAfterRecog = CameraPreview.READ_AFTER_TAKING_SNAPSHOT; 
	            if (bundle != null)	{
	            	mstrRecognized = bundle.getString("RecognizingResult");		 
	            	String strRecogError = bundle.getString("RecognizingError");
		            setRecognizedInput(mstrRecognized, strRecogError);	// show confirm recognized text.
	            	nActionAfterRecog = bundle.getInt("ActionAfterTakingSnapshot");
		            if (nActionAfterRecog == CameraPreview.CALC_AFTER_TAKING_SNAPSHOT) {
		        		analyze("recognize-calculate");
		            } else if (nActionAfterRecog == CameraPreview.PLOT_AFTER_TAKING_SNAPSHOT) {
		        		analyze("recognize-plot");
		            } // else nActionAfterRecog == CameraPreview.CALC_AFTER_TAKING_SNAPSHOT, do nothing but save recognized string.

		            /* need not to hide inputpad because this has been done in setRecognizedInput function.
		            EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
	        		if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
	        			setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);
	        		} else	{
	        			setSoftKeyState(txtInput, ENABLE_HIDE_SOFTKEY);
	        		}*/
	            }
	        }
	        break;
		} case (FINGER_PAINT_ACTIVITY) : {
	        if (resultCode == RESULT_OK) {
	        	Bundle bundle = data.getExtras();
	        	String strRecogError = null;
	            if (bundle != null)	{
		            mstrRecognized = bundle.getString("RecognizingResult");
	            	strRecogError = bundle.getString("RecognizingError");
		            setRecognizedInput(mstrRecognized, strRecogError);	// show confirm recognized text.
	            }
	            setRecognizedInput(mstrRecognized, strRecogError);
	        }
	        break;
		}
		}
	}
	
	public void setRecognizedInput(String strRecogResult, String strRecogErr) {
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		// if strRecogResult is null, we do not change input text.
    	if (strRecogResult != null)	{
			txtInput.setText(strRecogResult);	// clear input and set text.
    	} else {
    		txtInput.setText("");
    	}

        String strConfirm = "<p>" + getString(R.string.please_confirm_recognized_result_and_calculation_result)
				+ "<a href=\"" + AOPER_URL_HEADER + EMAIL_UNSATISFACTORY_RECOG + "\">"
				+ getString(R.string.here) + "</a>" + getString(R.string.stop_charater) + "</p>";
        String strRecogExprsOutput = "";
        if (strRecogErr == null || strRecogErr.length() == 0) {
        	if (strRecogResult == null || strRecogResult.length() == 0) {
        		// output is empty
        		strRecogExprsOutput = "<p>" + getString(R.string.no_valid_expr_recognized) + "</p>\n";
        	} else {
        		// output is not empty
                strRecogExprsOutput = "<p>" + getString(R.string.recognized_result) + "</p>"; 
        		String[] strarrayExprs = txtInput.getText().toString().split("\n");
        		String strRecognizedExprColor = "color:#008000;", strInvalidExprColor = "color:#800000;";
        		for (int idx = 0; idx < strarrayExprs.length; idx ++)	{
        			/* show the expression(s) */
        			CurPos curpos = new CurPos();
        			curpos.m_nPos = 0;
        			AbstractExpr aexpr = new AEInvalid();
                    try {
        				aexpr = ExprAnalyzer.analyseExpression(strarrayExprs[idx], curpos);
        				strRecogExprsOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
        							+ "\" style=\"text-decoration: none;" + strRecognizedExprColor + "\">$"
        							+ AbstractExprConverter.convtAExpr2JQMath(aexpr) + "$</a></p>\n";
                    } catch (Exception e)	{
        				strRecogExprsOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
        						+ "\" style=\"text-decoration: none;" + strInvalidExprColor + "\">$"
        						+ AbstractExprConverter.convtPlainStr2JQMathNoException(strarrayExprs[idx]) + "$</a></p>\n";
        				strRecogExprsOutput += "<p>&nbsp;<big>&rArr;</big>&nbsp" + getString(R.string.invalid_expr_to_analyse) + " ";
        				strRecogExprsOutput += getString(R.string.please_modify_the_input_expression) + "</p><p>"
        						+ TextUtils.htmlEncode(strarrayExprs[idx]) + "</p>\n"; 
                    }
        		}
        	}
        } else {
        	// recognition error.
        	strRecogExprsOutput = "<p>" + strRecogErr + "</p>\n";
        }
		mstrarrayTaskAndOutput[0] = "recognize";
		mstrarrayTaskAndOutput[1] = strRecogExprsOutput;	// save it to mstrOutput so that it can be reloaded after rotation.
		mshistoricalRecMgr.addRecord(strRecogResult, mstrarrayTaskAndOutput[0], mstrarrayTaskAndOutput[1], "", ActivitySettings.msnNumberofRecords);
		WebView wvOutput = (WebView) findViewById(R.id.webviewSmartMathOutput);
		wvOutput.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
					OUTPUT_HEAD_STRING + mstrFontSize + OUTPUT_SIZE_UNIT_STRING + strConfirm + strRecogExprsOutput + OUTPUT_TAIL_STRING,
					"text/html", "utf-8", "");
		
       	// hide the soft keyboard and inputpad so that recognizing result can be shown.   		
		if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
            setSoftKeyState(txtInput, ENABLE_HIDE_SOFTKEY);
		} else if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD)	{
            setSoftKeyState(txtInput, ENABLE_HIDE_INPUTPAD);
		}
	}

	public static void removeObsoleteCharts(Context context)	{
		long lOldestTS = 0L;
		for (int idx = mshistoricalRecMgr.getAllRecords().size() -1; idx >= 0; idx --)	{
			String strFileName = mshistoricalRecMgr.getAllRecords().get(idx).mstrChartFileName;
			if (strFileName != null && strFileName.trim().length() != 0)	{
				lOldestTS = ActivityChartDemon.getChartTimestamp(strFileName);
				if (lOldestTS != 0)	{
					break;
				}
			}
		}

        String strRootPath = AndroidStorageOptions.getSelectedStoragePath()
        					+ MFPFileManagerActivity.STRING_PATH_DIV
        					+ MFPFileManagerActivity.STRING_APP_FOLDER;
		String strSnapshotFileFolder = strRootPath
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ MFPFileManagerActivity.STRING_CHART_SNAPSHOT_FOLDER
				+ MFPFileManagerActivity.STRING_PATH_DIV;
		String strChartFileFolder = strRootPath
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ MFPFileManagerActivity.STRING_CHART_FOLDER
				+ MFPFileManagerActivity.STRING_PATH_DIV;
		String strSysGen = ChartOperator.addEscapes(context.getString(R.string.chart_name_default)).trim();
		File directorySnapshot = new File(strSnapshotFileFolder);
		String[] strSnapshotFiles = directorySnapshot.list();
		if (strSnapshotFiles == null)	{
			strSnapshotFiles = new String[0];
		}
		for (int index = 0; index < strSnapshotFiles.length; index ++)	{
			String strLeafPath = strSnapshotFileFolder + strSnapshotFiles[index];
			File leafFile = new File(strLeafPath);
			if (leafFile.isDirectory())	{
				// this is a Directory
				continue;
			} else if (strSnapshotFiles[index].length() >= strSysGen.length()
					&& strSnapshotFiles[index].substring(0, strSysGen.length()).equalsIgnoreCase(strSysGen)
					&& strSnapshotFiles[index].substring(strSnapshotFiles[index].length() - ".jpg".length())
								.toLowerCase(Locale.US).equals(".jpg"))	{
				// this is a snapshot file
				long lTS = ActivityChartDemon.getChartTimestamp(
						strSnapshotFiles[index].substring(0, strSnapshotFiles[index].length() - ".jpg".length()));
				if (lOldestTS == 0 || lTS < lOldestTS)	{
					leafFile.delete();	// delete obsolete file
				}
			}
		}
		
		File directoryChart = new File(strChartFileFolder);
		String[] strChartFiles = directoryChart.list();
		if (strChartFiles == null)	{
			strChartFiles = new String[0];
		}
		for (int index = 0; index < strChartFiles.length; index ++)	{
			String strLeafPath = strChartFileFolder + strChartFiles[index];
			File leafFile = new File(strLeafPath);
			if (leafFile.isDirectory())	{
				// this is a Directory
				continue;
			} else if (strChartFiles[index].length() >= strSysGen.length()
					&& strChartFiles[index].substring(0, strSysGen.length()).equalsIgnoreCase(strSysGen)
					&& strChartFiles[index].substring(strChartFiles[index].length() - MFPFileManagerActivity.STRING_CHART_EXTENSION.length())
								.toLowerCase(Locale.US).equals(MFPFileManagerActivity.STRING_CHART_EXTENSION))	{
				// this is a snapshot file
				long lTS = ActivityChartDemon.getChartTimestamp(
						strChartFiles[index].substring(0, strChartFiles[index].length() - MFPFileManagerActivity.STRING_CHART_EXTENSION.length()));
				if (lOldestTS == 0 || lTS < lOldestTS)	{
					leafFile.delete();	// delete obsolete file
				}
			}
		}
	}
	
	void insert2Input(String strToInsert)	{
		EditText txtInput = (EditText)findViewById(R.id.edtSmartMathInput);
		int nSelectionStart = txtInput.getSelectionStart();
		int nSelectionEnd = txtInput.getSelectionEnd();
		if (nSelectionStart < 0 || nSelectionEnd < 0
				|| nSelectionStart > txtInput.getText().length()
				|| nSelectionEnd > txtInput.getText().length()
				|| nSelectionStart > nSelectionEnd)	{	// invalid selection
			txtInput.setText(strToInsert);
			txtInput.setSelection(txtInput.getText().length());
		} else	{	// valid selection, insert the text
			txtInput.getText().replace(nSelectionStart, nSelectionEnd, strToInsert);
			txtInput.setSelection(nSelectionStart + strToInsert.length());
		}
	}
	
	public void showErrorMsgBox(String strErrMsg, final String strNextAction)	{
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		blder.setIcon(R.drawable.icon);
		blder.setTitle(getString(R.string.error));
		blder.setMessage(strErrMsg);
		blder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (strNextAction.equalsIgnoreCase("WhatsNew"))	{
					showWhatsNewBox();
				}
			}
		});
		blder.setCancelable(false);
		AlertDialog alertErrDlg = blder.create();
		alertErrDlg.show();
	}
	
	public void sendEmail2Developer(String strOperCmd, String[] strParams) {
		if (strOperCmd.equals(EMAIL_UNSATISFACTORY_RECOG)) {
			Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			emailIntent.setType("text/plain");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"cyzsoft@gmail.com"});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.unsatisfactory_recog));
			String strBodyText = getString(R.string.please_type_what_you_want_to_tell_us);
			strBodyText += "\n\n";
			strBodyText += getString(R.string.please_do_not_change_the_follows);
		    String strPackageName = getPackageName();
		    String strVersionName = "v";
		    try {
		    	PackageInfo pInfo = getPackageManager().getPackageInfo(strPackageName, PackageManager.GET_META_DATA);
		    	strVersionName += pInfo.versionName;
		    } catch (NameNotFoundException e1) {
		        Log.e(this.getClass().getSimpleName(), "Name not found", e1);
		    }

			strBodyText += "\n\n" + strPackageName + " " + strVersionName;
			strBodyText += "\n\n";
			strBodyText += getString(R.string.recognized_result);
			strBodyText += ":\n" + ((strParams[0] == null)?"":strParams[0]) + "\n\n";
			strBodyText += getString(R.string.input_text);
			strBodyText += ":\n" + ((strParams[1] == null)?"":strParams[1]) + "\n\n";
			strBodyText += getString(R.string.output_text);
			strBodyText += ":\n" + ((strParams[2] == null)?"":strParams[2]) + "\n\n";
			
			emailIntent.putExtra(Intent.EXTRA_TEXT, strBodyText);
			ArrayList<Uri> uris = new ArrayList<Uri>();
			String strPhotoFilePath = MFPFileManagerActivity.getAppFolderFullPath() + File.separator + CameraPreview.INITIAL_BMP_FILE_NAME;
			String strFinalFilePath = MFPFileManagerActivity.getAppFolderFullPath() + File.separator + CameraPreview.PROCESSED_IMAGE_SAVED_FILE_NAME;
            File filePhoto = new File(strPhotoFilePath);
			if (filePhoto.exists() && filePhoto.canRead()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //Cannot use Uri uri = Uri.fromFile(filePhoto); after Android N. Have to use file provider
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", filePhoto);
                    uris.add(uri);
                } else {
                    Uri uri = Uri.fromFile(filePhoto);
                    uris.add(uri);
                }
			}
			File fileFinal = new File(strFinalFilePath);
			if (fileFinal.exists() && fileFinal.canRead()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //Cannot use Uri uri = Uri.fromFile(fileFinal); after Android N. Have to use file provider
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", fileFinal);
                    uris.add(uri);
                } else {
                    Uri uri = Uri.fromFile(fileFinal);
                    uris.add(uri);
                }
			}
			emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
			emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_an_email_provider)));
		}
	}
	
	public void showWhatsNewBox()	{
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		blder.setIcon(R.drawable.icon);
		blder.setTitle(getString(R.string.whats_new));
        LayoutInflater factory = LayoutInflater.from(this);
        View vWhatsNew = factory.inflate(R.layout.whats_new, null);
        WebView webviewWhatsNew = (WebView)vWhatsNew.findViewById(R.id.webviewWhatsNew);
        webviewWhatsNew.setVerticalScrollBarEnabled(true);
        webviewWhatsNew.setHorizontalScrollBarEnabled(true);
	    webviewWhatsNew.getSettings().setBuiltInZoomControls(true);
        Locale l = Locale.getDefault();  
	    String strLanguage = String.format("%s-%s", l.getLanguage(), l.getCountry());
	    String strIndexAddr;
	    if (strLanguage.equals("zh-CN") || strLanguage.equals("zh-SG"))	{
	    	strIndexAddr = "file:///android_asset/zh-CN/" + WHATS_NEW_FILE_PATH + ".html";
	    } else if (strLanguage.equals("zh-TW") || strLanguage.equals("zh-HK"))	{
		    	strIndexAddr = "file:///android_asset/zh-TW/" + WHATS_NEW_FILE_PATH + ".html";
	    } else	{
	    	strIndexAddr = "file:///android_asset/en/" + WHATS_NEW_FILE_PATH + ".html";
	    	strLanguage = "en";
	    }
	    webviewWhatsNew.loadUrl(strIndexAddr);
		blder.setView(vWhatsNew);
		blder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		blder.setCancelable(false);
		AlertDialog alertWhatsnewDlg = blder.create();
		alertWhatsnewDlg.show();
	}

	public int getSoftKeyState()	{
		return mnSoftKeyState;
	}

	@Override
	public void onResume()	{
		super.onResume();
		if (masyncTask != null && masyncTask.mbCompleted)	{
		}
	}
	
	@Override
	public void onPause()	{
		mshistoricalRecMgr.flush(this);	// flush should be called here because onPause is always called when exit an activity.
		super.onPause();
	}
	@Override
    public void onBackPressed() {
        if (mbDblBack2ExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        mbDblBack2ExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.press_back_again_to_exit), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
            	mbDblBack2ExitPressedOnce = false;   

            }
        }, 2000);
    } 
	
	/** Called when the activity is finally destroyed or in portrait-landscape switch. */
    @Override
    public void onDestroy()	{
		// mhistoricalRecMgr.flush();	// shouldn't be done here because onDestroy is not called when switch to home screen and put smartmath background.

    	// release memory of wvOutput.
    	WebView wvOutput = (WebView) findViewById(R.id.webviewSmartMathOutput);
    	if (wvOutput != null)	{
			ViewGroup viewGroup = (ViewGroup) wvOutput.getParent();
			if (viewGroup != null)
			{
				viewGroup.removeView(wvOutput);
			}
			wvOutput.setFocusable(true);
			wvOutput.removeAllViews();
			wvOutput.clearHistory();
			wvOutput.destroy();
    	}
    	if (isFinishing())	{
    		MFPAdapter.clear();
    	}
    	try	{
    		super.onDestroy();
    	} catch(Exception e)	{
    		// have to add this for Amazon because at com.amazon.android.Kiwi.onDestroy(Unknown Source) it may throw exceptions.
    	}
    	if (isFinishing())	{
    		System.exit(0);	// clear memory so that if adView get problem, it can really restart.
    	}
    }
}
