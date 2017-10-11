package com.cyzapps.SmartMath;


import java.io.File;
import java.util.ArrayList;

import com.cyzapps.Jfcalc.IOLib;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class ActivitySettings extends Activity {

	public static final String SETTINGS = "SmartMath_settings";
	public static final String BITS_OF_PRECISION = "bits_of_precision";
	public static final String BIG_SMALL_THRESH = "big_small_thresh";
	public static final String SMC_PLOT_VAR_FROM = "smc_plot_var_from";
	public static final String SMC_PLOT_VAR_TO = "smc_plot_var_to";
	public static final String NUMBER_OF_RECORDS = "number_of_records";	
	public static final String ENABLE_BUTTON_PRESS_VIBRATION = "enable_btn_press_vibration";

	public static int msnBitsofPrecision = 4;
	public static int msnNumberofRecords = 20;
	public static int msnBigSmallThresh = 10;
	public static float msfSmCPlotVarFrom = -5.0f;
	public static float msfSmCPlotVarTo = 5.0f;
	public static boolean msbEnableBtnPressVibration = false;

	public static final int VIBERATION_LENGTH = 50;
	
    public static final int NORMAL_BKGRND_COLOR = Color.WHITE;
    public static final int ERROR_BKGRND_COLOR = Color.YELLOW;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.calculator_settings));
		setContentView(R.layout.settings);
		
		setStyleOfSettingsCtrls();

		//Read preferences
		readSettings();
	
		Spinner spnBitsofPrec = (Spinner)findViewById(R.id.setting_number_format);
		ArrayList<String> arraylistBitsofPrec = new ArrayList<String>();
		int nSelectedBitsofPrecPos = -1;
		for (int i = 0; i <= 5; i++) {
			if (i < 5) {
				Integer nChoice = i * 2;
				if (nChoice == msnBitsofPrecision)
					nSelectedBitsofPrecPos = i;
				arraylistBitsofPrec.add(nChoice.toString() + getString(R.string.digits_shown));
			} else {
				if (msnBitsofPrecision == -1)
					nSelectedBitsofPrecPos = i;
				arraylistBitsofPrec.add(getString(R.string.let_calculator_decide));
			}
		}
		ArrayAdapter<String> aspnBitsofPrec = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, arraylistBitsofPrec);
		aspnBitsofPrec
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnBitsofPrec.setAdapter(aspnBitsofPrec);
		spnBitsofPrec.setSelection(nSelectedBitsofPrecPos);
		
		Spinner spnExtremeValue = (Spinner)findViewById(R.id.setting_extreme_value);
		ArrayList<String> arraylistExtremeValue = new ArrayList<String>();
		int nSelectedExtremeValue = -1;
		for (int i = -5; i <= 20; i = i + 5) {
			if (msnBigSmallThresh == i)
			{
				nSelectedExtremeValue = i / 5 + 1;
			}
			if (i == -5) {
				arraylistExtremeValue.add(getString(R.string.never_sci_notation));
			} else if (i == 0){
				arraylistExtremeValue.add(getString(R.string.always_sci_notation));
			} else {
				arraylistExtremeValue.add(getString(R.string.if_log10_abs_result) + i);
			}
		}
		ArrayAdapter<String> aspnExtremeValue = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, arraylistExtremeValue);
		aspnExtremeValue
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnExtremeValue.setAdapter(aspnExtremeValue);
		spnExtremeValue.setSelection(nSelectedExtremeValue);		
		
		Spinner spnNumofRec = (Spinner)findViewById(R.id.setting_record_length);
		ArrayList<String> arraylistNumofRec = new ArrayList<String>();
		int nSelectedNumofRecPos = 0;
		for (int i = 0; i < 4; i++) {
			Integer nChoice = (i + 1) * 10;
			if (nChoice == msnNumberofRecords)
				nSelectedNumofRecPos = i;
			arraylistNumofRec.add(nChoice.toString() + getString(R.string.records_shown));
		}
		ArrayAdapter<String> aspnNumofRec = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, arraylistNumofRec);
		aspnNumofRec
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnNumofRec.setAdapter(aspnNumofRec);
		spnNumofRec.setSelection(nSelectedNumofRecPos);
		
		final EditText edtSmcPlotVarFrom = (EditText)findViewById(R.id.variable_from);
		final EditText edtSmcPlotVarTo = (EditText)findViewById(R.id.variable_to);
		edtSmcPlotVarFrom.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try	{
					msfSmCPlotVarFrom = Float.parseFloat(edtSmcPlotVarFrom.getText().toString());
					msfSmCPlotVarTo = Float.parseFloat(edtSmcPlotVarTo.getText().toString());
					if (!(msfSmCPlotVarFrom < msfSmCPlotVarTo))	{	// this can handle Nan or Inf
						edtSmcPlotVarFrom.setBackgroundColor(ERROR_BKGRND_COLOR);
						edtSmcPlotVarTo.setBackgroundColor(ERROR_BKGRND_COLOR);
					} else	{
						edtSmcPlotVarFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
						edtSmcPlotVarTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
					}
				} catch (NumberFormatException e)	{
					edtSmcPlotVarFrom.setBackgroundColor(ERROR_BKGRND_COLOR);
					edtSmcPlotVarTo.setBackgroundColor(ERROR_BKGRND_COLOR);
				}
			}
			
		});
		edtSmcPlotVarTo.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				try	{
					msfSmCPlotVarFrom = Float.parseFloat(edtSmcPlotVarFrom.getText().toString());
					msfSmCPlotVarTo = Float.parseFloat(edtSmcPlotVarTo.getText().toString());
					if (!(msfSmCPlotVarFrom < msfSmCPlotVarTo))	{	// this can handle Nan or Inf
						edtSmcPlotVarFrom.setBackgroundColor(ERROR_BKGRND_COLOR);
						edtSmcPlotVarTo.setBackgroundColor(ERROR_BKGRND_COLOR);
					} else	{
						edtSmcPlotVarFrom.setBackgroundColor(NORMAL_BKGRND_COLOR);
						edtSmcPlotVarTo.setBackgroundColor(NORMAL_BKGRND_COLOR);
					}
				} catch (NumberFormatException e)	{
					edtSmcPlotVarFrom.setBackgroundColor(ERROR_BKGRND_COLOR);
					edtSmcPlotVarTo.setBackgroundColor(ERROR_BKGRND_COLOR);
				}
			}
			
		});
		if (!(msfSmCPlotVarFrom < msfSmCPlotVarTo))	{	// this can handle Nan or Inf
			edtSmcPlotVarFrom.setText(String.valueOf("-5.0"));
			edtSmcPlotVarTo.setText(String.valueOf("5.0"));
			msfSmCPlotVarFrom = -5.0f;
			msfSmCPlotVarTo = 5.0f;
		} else	{
			edtSmcPlotVarFrom.setText(String.valueOf(msfSmCPlotVarFrom));
			edtSmcPlotVarTo.setText(String.valueOf(msfSmCPlotVarTo));
		}
				
		CheckBox chkboxEnableVibration = (CheckBox)findViewById(R.id.enable_btn_press_vibration);
		chkboxEnableVibration.setChecked(msbEnableBtnPressVibration);
		
		AndroidStorageOptions.determineStorageOptions();
		Spinner spnSelectedAppDataStorage = (Spinner)findViewById(R.id.setting_appdata_storage_path);
		spnSelectedAppDataStorage.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				
				EditText et = (EditText) findViewById(R.id.external_storage_mnt_folder_edit);
				if (arg2 == 0)	{
					et.setText(AndroidStorageOptions.getDefaultStoragePath());
				} else	{
					et.setText(AndroidStorageOptions.paths[arg2 - 1]);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				EditText et = (EditText) findViewById(R.id.external_storage_mnt_folder_edit);
				et.setText(AndroidStorageOptions.getDefaultStoragePath());
			}
			
		});
		ArrayList<String> arraylistAppDataStorage = new ArrayList<String>();
		int nSelectedAppDataStorage = 0;
		arraylistAppDataStorage.add(AppSmartMath.getContext().getString(R.string.default_storage));
		for (int i = 0; i < AndroidStorageOptions.count; i++) {
			arraylistAppDataStorage.add(AndroidStorageOptions.labels[i]);
			if (AndroidStorageOptions.getSelectedStoragePath().equals(AndroidStorageOptions.paths[i]))	{
				nSelectedAppDataStorage = i + 1;
			}
		}
		ArrayAdapter<String> aspnSelectedAppDataStorage = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, arraylistAppDataStorage);
		aspnSelectedAppDataStorage
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnSelectedAppDataStorage.setAdapter(aspnSelectedAppDataStorage);
		spnSelectedAppDataStorage.setSelection(nSelectedAppDataStorage);
		
		// read-only settings are created in onCreate
		EditText et = (EditText) findViewById(R.id.external_storage_mnt_folder_edit);
		et.setText(AndroidStorageOptions.getSelectedStoragePath());
		et = (EditText) findViewById(R.id.system_folder_edit);
		et.setText(MFPFileManagerActivity.STRING_APP_FOLDER);
		
		Button btnOk = (Button)findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				onOK();
			}
			
		});
		
		Button btnCancel = (Button)findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				ActivitySettings.this.finish();
			}
			
		});
	}
	
	public static boolean readSettings()	{
		//Read preferences
		SharedPreferences settings = AppSmartMath.getContext().getSharedPreferences(SETTINGS, 0);
		boolean bReturn = false;
		if (settings != null) {
			/*
			 * Note: we cannot write like
			 * settings.edit().putInt(BITS_OF_PRECISION, nBitsofPrecision);
			 * settings.edit().putInt(NUMBER_OF_RECORDS, nNumberofRecords);
			 * settings.edit().commit();
			 * because settings.edit() returns different editor each time.
			 */
			msnBitsofPrecision = settings.getInt(BITS_OF_PRECISION, msnBitsofPrecision);
			msnBigSmallThresh = settings.getInt(BIG_SMALL_THRESH, msnBigSmallThresh);
			MFPAdapter.mmfpNumBigThresh = MFPNumeric.pow(MFPNumeric.TEN, new MFPNumeric(msnBigSmallThresh));
			MFPAdapter.mmfpNumSmallThresh = MFPNumeric.pow(MFPNumeric.ONE_TENTH, new MFPNumeric(msnBigSmallThresh));
			msnNumberofRecords = settings.getInt(NUMBER_OF_RECORDS, msnNumberofRecords);
			msfSmCPlotVarFrom = settings.getFloat(SMC_PLOT_VAR_FROM, msfSmCPlotVarFrom);
			msfSmCPlotVarTo = settings.getFloat(SMC_PLOT_VAR_TO, msfSmCPlotVarTo);
			msbEnableBtnPressVibration = settings.getBoolean(ENABLE_BUTTON_PRESS_VIBRATION, msbEnableBtnPressVibration);
			AndroidStorageOptions.setSelectedStoragePath(
					settings.getString(AndroidStorageOptions.SELECTED_STORAGE_PATH,
							AndroidStorageOptions.getSelectedStoragePath()));
			
			IOLib.msstrWorkingDir = MFPFileManagerActivity.getAppFolderFullPath();	// set the initial working directory.		
			bReturn = true;
		} else	{
			bReturn = false;
		}
	
		IOLib.msstrWorkingDir = MFPFileManagerActivity.getAppFolderFullPath();	// set the initial working directory.		
		return bReturn;
	}
	
	public static boolean saveSettings()	{
		//Save preferences
		SharedPreferences settings = AppSmartMath.getContext().getSharedPreferences(SETTINGS, 0);
		if (settings != null) {
			/*
			 * Note: we cannot write like
			 * settings.edit().putInt(BITS_OF_PRECISION, nBitsofPrecision);
			 * settings.edit().putInt(NUMBER_OF_RECORDS, nNumberofRecords);
			 * settings.edit().commit();
			 * because settings.edit() returns different editor each time.
			 */
			settings.edit().putInt(BITS_OF_PRECISION, msnBitsofPrecision)
						.putInt(BIG_SMALL_THRESH, msnBigSmallThresh)
						.putInt(NUMBER_OF_RECORDS, msnNumberofRecords)
						.putFloat(SMC_PLOT_VAR_FROM, msfSmCPlotVarFrom)
						.putFloat(SMC_PLOT_VAR_TO, msfSmCPlotVarTo)
						.putBoolean(ENABLE_BUTTON_PRESS_VIBRATION, msbEnableBtnPressVibration)
						.putString(AndroidStorageOptions.SELECTED_STORAGE_PATH, AndroidStorageOptions.getSelectedStoragePath())
						.commit();
			
			IOLib.msstrWorkingDir = MFPFileManagerActivity.getAppFolderFullPath();	// set the initial working directory.		

			CharSequence text = AppSmartMath.getContext().getString(R.string.settings_saved);
			int duration = Toast.LENGTH_SHORT;
	
			Toast toast = Toast.makeText(AppSmartMath.getContext(), text, duration);
			toast.show();
			return true;
		}
		return false;
	}

	protected void onOK() {
		Spinner spnBitsofPrec = (Spinner)findViewById(R.id.setting_number_format);
		Spinner spnExtremeValue = (Spinner)findViewById(R.id.setting_extreme_value);
		Spinner spnNumofRec = (Spinner)findViewById(R.id.setting_record_length);
		EditText edtSmcPlotVarFrom = (EditText)findViewById(R.id.variable_from);
		EditText edtSmcPlotVarTo = (EditText)findViewById(R.id.variable_to);
		CheckBox chkboxEnableVibration = (CheckBox)findViewById(R.id.enable_btn_press_vibration);
		Spinner spnSelectedAppDataStorage = (Spinner)findViewById(R.id.setting_appdata_storage_path);
		if (spnBitsofPrec.getSelectedItemPosition() == 5)
			msnBitsofPrecision = -1;
		else
			msnBitsofPrecision = spnBitsofPrec.getSelectedItemPosition() * 2;
		msnBigSmallThresh = (spnExtremeValue.getSelectedItemPosition() - 1) * 5;
		MFPAdapter.mmfpNumBigThresh = MFPNumeric.pow(MFPNumeric.TEN, new MFPNumeric(msnBigSmallThresh));
		MFPAdapter.mmfpNumSmallThresh = MFPNumeric.pow(MFPNumeric.ONE_TENTH, new MFPNumeric(msnBigSmallThresh));
		msnNumberofRecords = (spnNumofRec.getSelectedItemPosition() + 1) * 10;
		try	{
			msfSmCPlotVarFrom = Float.parseFloat(edtSmcPlotVarFrom.getText().toString());
			msfSmCPlotVarTo = Float.parseFloat(edtSmcPlotVarTo.getText().toString());
			if (!(msfSmCPlotVarFrom < msfSmCPlotVarTo))	{	// this can handle Nan or Inf
				msfSmCPlotVarFrom = -5f;
				msfSmCPlotVarTo = 5f;
			}
		} catch (NumberFormatException e)	{
			msfSmCPlotVarFrom = -5f;
			msfSmCPlotVarTo = 5f;
		}
		msbEnableBtnPressVibration = chkboxEnableVibration.isChecked();
		
		int nSelectedPosition = spnSelectedAppDataStorage.getSelectedItemPosition();
		String strSelectedStoragePath = AndroidStorageOptions.getDefaultStoragePath();
		if (nSelectedPosition > 0 && nSelectedPosition <= AndroidStorageOptions.count)	{
			strSelectedStoragePath
				= AndroidStorageOptions.paths[nSelectedPosition - 1];
		}
		if (!strSelectedStoragePath.equals(AndroidStorageOptions.getSelectedStoragePath()))	{
			// we change the place to store application data.
	        File dir = new File(MFPFileManagerActivity.getAppFolderFullPath());
	        if (!dir.exists())	{
	            if (!dir.mkdirs())	{
	                // cannot create app folder.
					showErrorMsgBox(getString(R.string.cannot_create_app_folder));	// next step is whats new box
					// have to return because we cannot copy files.
					return;
				}
			}

        	// change settings only if succeed.
    		final String strOriginalAppDataFullPath = MFPFileManagerActivity.getAppFolderFullPath();
    		AndroidStorageOptions.setSelectedStoragePath(strSelectedStoragePath);
			final ProgressDialog dlgMovingAppDataProgress = ProgressDialog.show(this,
																				getString(R.string.please_wait),
																				getString(R.string.copying_data),
																				true);

			final Handler handler = new Handler();
			Thread thread = new Thread(new Runnable()	{
			
				@Override
				public void run() {
					boolean bCopySuccessfully = true;
		        	try	{
		        		
		        		File fileOriginalAppDataFolder = new File(strOriginalAppDataFullPath);
		        		File fileNewAppDataFolder = new File(MFPFileManagerActivity.getAppFolderFullPath());
		        		// copy old SmartMath folder to new place.
		        		MFPFileManagerActivity.copyFileOrFolder(fileOriginalAppDataFolder, fileNewAppDataFolder);
		        		
						bCopySuccessfully = true;
		        	} catch (Exception e)	{
		        		// error in copying
						AndroidStorageOptions.setSelectedStoragePath(strOriginalAppDataFullPath);	// restore original settings.
						bCopySuccessfully = false;
		        	} finally	{
		        		if (bCopySuccessfully)	{
							handler.post(new Runnable()	{
								@Override
								public void run() {
									dlgMovingAppDataProgress.dismiss();
									saveSettings();
									ActivitySettings.this.finish();
								}
							});
		        		} else	{
							handler.post(new Runnable()	{
								@Override
								public void run() {
									dlgMovingAppDataProgress.dismiss();
									showErrorMsgBox(getString(R.string.error_in_copying_data_files));
								}
							});
		        		}
		        	}
				}
			});
			thread.start();
        } else	{
        	saveSettings();
        	finish();
        }
	}
	
	@Override
	/*
	 * Create only one help menu.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, Menu.FIRST, 0, getString(R.string.menu_help));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST: 
		   	Intent intentHelp = new Intent(this, ActivityShowHelp.class);
		   	Bundle bundle = new Bundle();
		   	bundle.putString("HELP_CONTENT", "settings");
		   	//Add this bundle to the intent
		   	intentHelp.putExtras(bundle);
		   	startActivity(intentHelp);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	//setContentView(R.layout.settings);	// should not reset content view, otherwise, the whole layout is reset and content in controls are empty.
    											// Have to do reinitialization. This is different from ActivityMainPanel
    	
    	setStyleOfSettingsCtrls();
    }
    
    public void setStyleOfSettingsCtrls()	{
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenWidthPixels;
		Display d = getWindowManager().getDefaultDisplay(); 
		nScreenWidthPixels = d.getWidth();
		LinearLayout linearlayoutWidthCtrl = (LinearLayout)findViewById(R.id.width_control);
		if (nScreenSizeCategory != Configuration.SCREENLAYOUT_SIZE_NORMAL
				&& nScreenSizeCategory != Configuration.SCREENLAYOUT_SIZE_SMALL
				&& nScreenSizeCategory != Configuration.SCREENLAYOUT_SIZE_LARGE)	{	// xlarge
			linearlayoutWidthCtrl.setLayoutParams(new LinearLayout.LayoutParams(nScreenWidthPixels/2, 0));
		} else	{
			linearlayoutWidthCtrl.setLayoutParams(new LinearLayout.LayoutParams(nScreenWidthPixels, 0));
		}
    }
    
	public void showErrorMsgBox(String strErrMsg)	{
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		blder.setIcon(R.drawable.icon);
		blder.setTitle(getString(R.string.error));
		blder.setMessage(strErrMsg);
		blder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				saveSettings();
				ActivitySettings.this.finish();
			}
		});
		blder.setCancelable(false);
		AlertDialog alertErrDlg = blder.create();
		alertErrDlg.show();
	}

}
