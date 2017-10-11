package com.cyzapps.SmartMath;

import java.util.Locale;

import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.adapter.MFPAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ActivityCvtUnit extends Activity {
	
	public String mstrCommand = "";
	public String mstrResult = "";
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert_unit);
        
        // do not reload MFPAdapter function libs here coz it is a dialog box, ActivitySmartCalc OnCreate will be called
        // when this dialog box is restored. And MFPAdapter function libs will be reload in ActivitySmartCalc OnCreate
        //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();
        String strInitialInput = "";
        if (bundle != null)	{
        	strInitialInput = bundle.getString("Initial_Input");
        	if (strInitialInput == null)	{
        		strInitialInput = "";
        	}
        }

        CheckBox chkBoxUseSciNotation = (CheckBox)findViewById(R.id.chkBoxUseSciNotation);
        EditText edtTxtFromUnit = (EditText)findViewById(R.id.edtTextFromUnit);
        EditText edtTxtFromUnitE = (EditText)findViewById(R.id.edtTextFromUnitE);
    	// set initial value
        if (strInitialInput.length() > 0)	{
        	strInitialInput.toUpperCase(Locale.US);
        	int nEIndex = strInitialInput.indexOf("E");
        	if (nEIndex == -1)	{	// no scientific notation
        		chkBoxUseSciNotation.setChecked(false);
        		edtTxtFromUnit.setText(strInitialInput);
        	} else	{	// scientific notation
        		chkBoxUseSciNotation.setChecked(true);
        		edtTxtFromUnit.setText(strInitialInput.substring(0, nEIndex));
        		edtTxtFromUnitE.setVisibility(View.VISIBLE);
        		edtTxtFromUnitE.setText(strInitialInput.substring(nEIndex + 1));
        	}
        }

        chkBoxUseSciNotation.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
		        TextView txtViewSciNotationE = (TextView)findViewById(R.id.txtViewSciNoteE);
		        EditText edtTxtFromUnitE = (EditText)findViewById(R.id.edtTextFromUnitE);
				if (isChecked)	{
					txtViewSciNotationE.setVisibility(View.VISIBLE);
					edtTxtFromUnitE.setText("0");
					edtTxtFromUnitE.setVisibility(View.VISIBLE);
				} else	{
					txtViewSciNotationE.setVisibility(View.INVISIBLE);
					edtTxtFromUnitE.setText("0");
					edtTxtFromUnitE.setVisibility(View.INVISIBLE);
				}
			}
        	
        });
        
        edtTxtFromUnit.addTextChangedListener(new TextWatcher(){

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
				convertUnit();
			}
        	
        });
        
        edtTxtFromUnitE.addTextChangedListener(new TextWatcher(){

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
				convertUnit();
			}
        	
        });
        
        Button btnClearInput = (Button)findViewById(R.id.btnClearInput);
        btnClearInput.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View arg0) {
		        EditText edtTxtFromUnit = (EditText)findViewById(R.id.edtTextFromUnit);
		        edtTxtFromUnit.setText("");
		        EditText edtTxtFromUnitE = (EditText)findViewById(R.id.edtTextFromUnitE);
				edtTxtFromUnitE.setText("0");
			}
        	
        });
        
        Spinner spinnerUnitType = (Spinner)findViewById(R.id.spinnerUnitType);
        spinnerUnitType.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String strSelection = arg0.getItemAtPosition(arg2).toString();
		        Spinner spinnerFromUnit = (Spinner)findViewById(R.id.spinnerFromUnit);
				String[] strarrayFrom = new String[0];
				
		        Spinner spinnerToUnit = (Spinner)findViewById(R.id.spinnerToUnit);
				String[] strarrayTo = new String[0];
				int nUnitTypeArrayId = R.array.length_unit_type_array;
				if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.length_unit_type)))	{
					nUnitTypeArrayId = R.array.length_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.area_unit_type)))	{
					nUnitTypeArrayId = R.array.area_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.volume_unit_type)))	{
					nUnitTypeArrayId = R.array.volume_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.mass_unit_type)))	{
					nUnitTypeArrayId = R.array.mass_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.speed_unit_type)))	{
					nUnitTypeArrayId = R.array.speed_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.time_unit_type)))	{
					nUnitTypeArrayId = R.array.time_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.force_unit_type)))	{
					nUnitTypeArrayId = R.array.force_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.pressure_unit_type)))	{
					nUnitTypeArrayId = R.array.pressure_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.energy_unit_type)))	{
					nUnitTypeArrayId = R.array.energy_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.power_unit_type)))	{
					nUnitTypeArrayId = R.array.power_unit_type_array;
				} else if (strSelection.equalsIgnoreCase(arg1.getResources().getString(R.string.temperature_unit_type)))	{
					nUnitTypeArrayId = R.array.temperature_unit_type_array;
				}
				strarrayFrom = getResources().getStringArray(nUnitTypeArrayId);
				ArrayAdapter<String> arrayAdapterFrom = new ArrayAdapter<String>(ActivityCvtUnit.this,
																				android.R.layout.simple_spinner_item,
																				strarrayFrom);
				arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinnerFromUnit.setAdapter(arrayAdapterFrom);
				spinnerFromUnit.setSelection(0);
				strarrayTo = getResources().getStringArray(nUnitTypeArrayId);
				ArrayAdapter<String> arrayAdapterTo = new ArrayAdapter<String>(ActivityCvtUnit.this,
																				android.R.layout.simple_spinner_item,
																				strarrayTo);
				arrayAdapterTo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinnerToUnit.setAdapter(arrayAdapterTo);
				spinnerToUnit.setSelection(0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        });

        Spinner spinnerFromUnit = (Spinner)findViewById(R.id.spinnerFromUnit);
        spinnerFromUnit.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				convertUnit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				convertUnit();
			}
        });
		
        Spinner spinnerToUnit = (Spinner)findViewById(R.id.spinnerToUnit);
        spinnerToUnit.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				convertUnit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				convertUnit();
			}
        });

    	Button btnCpyCvtValue = (Button)findViewById(R.id.copy_converted_value_btn);
    	btnCpyCvtValue.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if (intent != null) {
					Bundle b = new Bundle();
					if (b != null) {
						b.putString("ConvertedValue", mstrResult);
						intent.putExtra("android.intent.extra.FunCalc", b);
					}
				}
				getParent().setResult(Activity.RESULT_OK, intent);
			   	finish();
			}
    	});
    	Button btnCpyCvtFunc = (Button)findViewById(R.id.copy_function_btn);
    	btnCpyCvtFunc.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				if (intent != null) {
					Bundle b = new Bundle();
					if (b != null) {
						b.putString("ConvertFunction", mstrCommand);
						intent.putExtra("android.intent.extra.FunCalc", b);
					}
				}
				getParent().setResult(Activity.RESULT_OK, intent);
			   	finish();
			}
    	});
    	Button btnClose = (Button)findViewById(R.id.close_convert_unit_btn);
    	btnClose.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
			   	finish();
			}
    	});
    	// will trigger convertUnit function.
        spinnerUnitType.setSelection(0);
    }
    
    /* we cannot delete these two functions although they don't do anything 
     * because we don't want the unit convert dialog show the text and selection
     * since last session.
     * (non-Javadoc)
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		// super.onRestoreInstanceState(bundle); do not restore the text in the command line box.
		
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		// super.onSaveInstanceState(bundle);
	}
    public void convertUnit()	{
    	boolean bIsAbleToPrice = true;
        Spinner spinnerFromUnit = (Spinner)findViewById(R.id.spinnerFromUnit);
        String strFromUnitFullName = spinnerFromUnit.getSelectedItem().toString();
        String strFromUnitAbbr = getUnitAbbrFromFullName(strFromUnitFullName);
        if (strFromUnitAbbr.trim().length() == 0)	{
        	bIsAbleToPrice = false;
        }
        Spinner spinnerToUnit = (Spinner)findViewById(R.id.spinnerToUnit);
        String strToUnitFullName = spinnerToUnit.getSelectedItem().toString();
        String strToUnitAbbr = getUnitAbbrFromFullName(strToUnitFullName);
        if (strToUnitAbbr.trim().length() == 0)	{
        	bIsAbleToPrice = false;
        }
        EditText edtTxtFromUnit = (EditText)findViewById(R.id.edtTextFromUnit);
        EditText edtTxtFromUnitE = (EditText)findViewById(R.id.edtTextFromUnitE);
        String strFromValue = edtTxtFromUnit.getText().toString().trim();
        String strFromEValue = edtTxtFromUnitE.getText().toString().trim();
        double dFromUnitValue = 0;
		if (strFromValue.length() == 0)	{
			bIsAbleToPrice = false;
		} else if (strFromEValue.length() == 0)	{
			strFromEValue = "0";
		}
		if (bIsAbleToPrice)	{
	    	try {
	    		dFromUnitValue = Double.parseDouble(strFromValue + "E" + strFromEValue);
	    	} catch	(NumberFormatException e){
	    		bIsAbleToPrice = false;
	    	}
		}

		TextView txtViewToResult = (TextView)findViewById(R.id.txtViewEqual);
		TextView txtViewUnitCvtFunc = (TextView)findViewById(R.id.txtViewFunction);
    	Button btnCpyCvtValue = (Button)findViewById(R.id.copy_converted_value_btn);
    	Button btnCpyCvtFunc = (Button)findViewById(R.id.copy_function_btn);
        if (bIsAbleToPrice == false)	{
        	mstrCommand = "";
        	mstrResult = "";
        	txtViewToResult.setText(getString(R.string.invalid_input));
        	txtViewUnitCvtFunc.setText("");
        	btnCpyCvtValue.setEnabled(false);
        	btnCpyCvtFunc.setEnabled(false);
        } else	{
        	// assume unit conversion is very fast so that we need not to use a seperate thread.
        	mstrCommand = "convert_unit(" + dFromUnitValue + ", \"" + strFromUnitAbbr
        						+ "\", \"" + strToUnitAbbr + "\")";
			ExprEvaluator exprEvaluator = new ExprEvaluator();

			/* evaluate the expression */
			CurPos curpos = new CurPos();
			curpos.m_nPos = 0;
			DataClass datumReturn = new DataClass();
			try	{
				datumReturn = exprEvaluator.evaluateExpression(mstrCommand, curpos);
				String[] strarrayResult = MFPAdapter.outputDatum(datumReturn);
				mstrResult = strarrayResult[0];
				txtViewToResult.setText(getString(R.string.is_equal_to_prompt) + " "
										+ strarrayResult[1]);
				txtViewUnitCvtFunc.setText(getString(R.string.underlying_function_is) + " " + mstrCommand);
	        	btnCpyCvtValue.setEnabled(true);
	        	btnCpyCvtFunc.setEnabled(true);
			} catch (Exception e)	{
	        	mstrCommand = "";
	        	mstrResult = "";
	        	txtViewToResult.setText(getString(R.string.cannot_convert_unit));
	        	txtViewUnitCvtFunc.setText("");
	        	btnCpyCvtValue.setEnabled(false);
	        	btnCpyCvtFunc.setEnabled(false);
			}
        }
    }
    
    public String getUnitAbbrFromFullName(String strUnitFullName)	{
    	String strUnitAbbreviation = "";
    	if (getString(R.string.micron_unit).equalsIgnoreCase(strUnitFullName)) {    // Microns
        	strUnitAbbreviation = "um";
        } else if (getString(R.string.millimetre_unit).equalsIgnoreCase(strUnitFullName)) {    // Millimetres
        	strUnitAbbreviation = "mm";
        } else if (getString(R.string.centimetre_unit).equalsIgnoreCase(strUnitFullName)) {    // Centimetres
        	strUnitAbbreviation = "cm";
        } else if (getString(R.string.metre_unit).equalsIgnoreCase(strUnitFullName)) {    // Metres
        	strUnitAbbreviation = "m";
        } else if (getString(R.string.kilometre_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilometres
        	strUnitAbbreviation = "km";
        } else if (getString(R.string.inch_unit).equalsIgnoreCase(strUnitFullName)) {    // Inches
        	strUnitAbbreviation = "in";
        } else if (getString(R.string.foot_unit).equalsIgnoreCase(strUnitFullName)) {    // Feet
        	strUnitAbbreviation = "ft";
        } else if (getString(R.string.yard_unit).equalsIgnoreCase(strUnitFullName)) {    // Yards
        	strUnitAbbreviation = "yd";
        } else if (getString(R.string.mile_unit).equalsIgnoreCase(strUnitFullName)) {    // Miles
        	strUnitAbbreviation = "mi";
        } else if (getString(R.string.nautical_mile_unit).equalsIgnoreCase(strUnitFullName)) {    // Nautical miles
        	strUnitAbbreviation = "nmi";
        } else if (getString(R.string.astronomical_unit_unit).equalsIgnoreCase(strUnitFullName)) {    // Astronomical units
        	strUnitAbbreviation = "AU";
        } else if (getString(R.string.light_year_unit).equalsIgnoreCase(strUnitFullName)) {    // Light years
        	strUnitAbbreviation = "ly";
        } else if (getString(R.string.parsec_unit).equalsIgnoreCase(strUnitFullName)) {    // Parsecs
        	strUnitAbbreviation = "pc";
        } else if (getString(R.string.mm2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square mms
        	strUnitAbbreviation = "mm2";
        } else if (getString(R.string.cm2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square cms
        	strUnitAbbreviation = "cm2";
        } else if (getString(R.string.metre2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square metres
        	strUnitAbbreviation = "m2";
        } else if (getString(R.string.hectare_unit).equalsIgnoreCase(strUnitFullName)) {    // Hectares
        	strUnitAbbreviation = "ha";
        } else if (getString(R.string.km2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square kms
        	strUnitAbbreviation = "km2";
        } else if (getString(R.string.inch2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square inches
        	strUnitAbbreviation = "sq in";
        } else if (getString(R.string.foot2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square feet
        	strUnitAbbreviation = "sq ft";
        } else if (getString(R.string.yard2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square Yards
        	strUnitAbbreviation = "sq yd";
        } else if (getString(R.string.acre_unit).equalsIgnoreCase(strUnitFullName)) {    // Acres
        	strUnitAbbreviation = "ac";
        } else if (getString(R.string.mile2_unit).equalsIgnoreCase(strUnitFullName)) {    // Square miles
        	strUnitAbbreviation = "sq mi";
        } else if (getString(R.string.ml_unit).equalsIgnoreCase(strUnitFullName)) {    // Millilitres (cc)
        	strUnitAbbreviation = "mL";
        } else if (getString(R.string.litre_unit).equalsIgnoreCase(strUnitFullName)) {    // Litres
        	strUnitAbbreviation = "L";
        } else if (getString(R.string.metre3_unit).equalsIgnoreCase(strUnitFullName)) {    // Cubic metres
        	strUnitAbbreviation = "m3";
        } else if (getString(R.string.inch3_unit).equalsIgnoreCase(strUnitFullName)) {    // Cubic inches
        	strUnitAbbreviation = "cu in";
        } else if (getString(R.string.foot3_unit).equalsIgnoreCase(strUnitFullName)) {    // Cubic feet
        	strUnitAbbreviation = "cu ft";
        } else if (getString(R.string.yard3_unit).equalsIgnoreCase(strUnitFullName)) {    // Cubic yards
        	strUnitAbbreviation = "cu yd";
        } else if (getString(R.string.km3_unit).equalsIgnoreCase(strUnitFullName)) {    // Cubic kms
        	strUnitAbbreviation = "km3";
        } else if (getString(R.string.founce_imp_unit).equalsIgnoreCase(strUnitFullName)) {    // Fluid ounce (Imp)
        	strUnitAbbreviation = "fl oz(Imp)";
        } else if (getString(R.string.pint_imp_unit).equalsIgnoreCase(strUnitFullName)) {    // Pint (Imp)
        	strUnitAbbreviation = "pt(Imp)";
        } else if (getString(R.string.gallon_imp_unit).equalsIgnoreCase(strUnitFullName)) {    // Gallon (Imp)
        	strUnitAbbreviation = "gal(Imp)";
        } else if (getString(R.string.founce_us_unit).equalsIgnoreCase(strUnitFullName)) {    // Fluid ounce (US)
        	strUnitAbbreviation = "fl oz(US)";
        } else if (getString(R.string.pint_us_unit).equalsIgnoreCase(strUnitFullName)) {    // Pint (US)
        	strUnitAbbreviation = "pt(US)";
        } else if (getString(R.string.gallon_us_unit).equalsIgnoreCase(strUnitFullName)) {    // Gallon (US)
        	strUnitAbbreviation = "gal(US)";
        } else if (getString(R.string.microgram_unit).equalsIgnoreCase(strUnitFullName)) {    // Micrograms
        	strUnitAbbreviation = "ug";
        } else if (getString(R.string.milligram_unit).equalsIgnoreCase(strUnitFullName)) {    // Milligrams
        	strUnitAbbreviation = "mg";
        } else if (getString(R.string.gram_unit).equalsIgnoreCase(strUnitFullName)) {    // Grams
        	strUnitAbbreviation = "g";
        } else if (getString(R.string.kg_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilograms
        	strUnitAbbreviation = "kg";
        } else if (getString(R.string.ton_unit).equalsIgnoreCase(strUnitFullName)) {    // Tonnes
        	strUnitAbbreviation = "t";
        } else if (getString(R.string.ounce_unit).equalsIgnoreCase(strUnitFullName)) {    // Ounces
        	strUnitAbbreviation = "oz";
        } else if (getString(R.string.lb_unit).equalsIgnoreCase(strUnitFullName)) {    // Pounds
        	strUnitAbbreviation = "lb";
        } else if (getString(R.string.market_catty_unit).equalsIgnoreCase(strUnitFullName)) {    // Market catties
        	strUnitAbbreviation = "jin";
        } else if (getString(R.string.hk_catty_unit).equalsIgnoreCase(strUnitFullName)) {    // Catties (HK)
        	strUnitAbbreviation = "jin(HK)";
        } else if (getString(R.string.tw_catty_unit).equalsIgnoreCase(strUnitFullName)) {    // Catties (TW)
        	strUnitAbbreviation = "jin(TW)";
        } else if (getString(R.string.metre_s_unit).equalsIgnoreCase(strUnitFullName)) {    // Metres per second
        	strUnitAbbreviation = "m/s";
        } else if (getString(R.string.km_h_unit).equalsIgnoreCase(strUnitFullName)) {    // Kms per hour
        	strUnitAbbreviation = "km/h";
        } else if (getString(R.string.foot_s_unit).equalsIgnoreCase(strUnitFullName)) {    // Feet per second
        	strUnitAbbreviation = "ft/s";
        } else if (getString(R.string.mile_h_unit).equalsIgnoreCase(strUnitFullName)) {    // Miles per hour
        	strUnitAbbreviation = "mph";
        } else if (getString(R.string.knot_unit).equalsIgnoreCase(strUnitFullName)) {    // Knots
        	strUnitAbbreviation = "knot";
        } else if (getString(R.string.nanosecond_unit).equalsIgnoreCase(strUnitFullName)) {    // Nanoseconds
        	strUnitAbbreviation = "ns";
        } else if (getString(R.string.microsecond_unit).equalsIgnoreCase(strUnitFullName)) {    // Microseconds
        	strUnitAbbreviation = "us";
        } else if (getString(R.string.millisecond_unit).equalsIgnoreCase(strUnitFullName)) {    // Milliseconds
        	strUnitAbbreviation = "ms";
        } else if (getString(R.string.second_unit).equalsIgnoreCase(strUnitFullName)) {    // Seconds
        	strUnitAbbreviation = "s";
        } else if (getString(R.string.minute_unit).equalsIgnoreCase(strUnitFullName)) {    // Minutes
        	strUnitAbbreviation = "min";
        } else if (getString(R.string.hour_unit).equalsIgnoreCase(strUnitFullName)) {    // Hours
        	strUnitAbbreviation = "h";
        } else if (getString(R.string.day_unit).equalsIgnoreCase(strUnitFullName)) {    // Days
        	strUnitAbbreviation = "d";
        } else if (getString(R.string.week_unit).equalsIgnoreCase(strUnitFullName)) {    // Weeks
        	strUnitAbbreviation = "wk";
        } else if (getString(R.string.year_unit).equalsIgnoreCase(strUnitFullName)) {    // Years
        	strUnitAbbreviation = "yr";
        } else if (getString(R.string.newton_unit).equalsIgnoreCase(strUnitFullName)) {    // Newtons
        	strUnitAbbreviation = "N";
        } else if (getString(R.string.kgf_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilogram-force
        	strUnitAbbreviation = "kgf";
        } else if (getString(R.string.lbf_unit).equalsIgnoreCase(strUnitFullName)) {    // Pound-force
        	strUnitAbbreviation = "lbF";
        } else if (getString(R.string.pascal_unit).equalsIgnoreCase(strUnitFullName)) {    // Pascal
        	strUnitAbbreviation = "Pa";
        } else if (getString(R.string.hectopascal_unit).equalsIgnoreCase(strUnitFullName)) {    // Hectopascal
        	strUnitAbbreviation = "hPa";
        } else if (getString(R.string.kilopascal_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilopascal
        	strUnitAbbreviation = "kPa";
        } else if (getString(R.string.megapascal_unit).equalsIgnoreCase(strUnitFullName)) {    // Megapascal
        	strUnitAbbreviation = "MPa";
        } else if (getString(R.string.atmosphere ).equalsIgnoreCase(strUnitFullName)) {    // Atomsphere
        	strUnitAbbreviation = "atm";
        } else if (getString(R.string.pound_square_inche_unit).equalsIgnoreCase(strUnitFullName)) {    // Pounds per square inch
        	strUnitAbbreviation = "psi";
        } else if (getString(R.string.torr_unit).equalsIgnoreCase(strUnitFullName)) {    // Torrs (millimetres of mercury)
        	strUnitAbbreviation = "Torr";
        } else if (getString(R.string.joule_unit).equalsIgnoreCase(strUnitFullName)) {    // Joules
        	strUnitAbbreviation = "J";
        } else if (getString(R.string.kilojoule_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilojoules
        	strUnitAbbreviation = "kJ";
        } else if (getString(R.string.megajoule_unit).equalsIgnoreCase(strUnitFullName)) {    // Megajoules
        	strUnitAbbreviation = "MJ";
        } else if (getString(R.string.kwh_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilowatt-Hours
        	strUnitAbbreviation = "kWh";
        } else if (getString(R.string.calorie_unit).equalsIgnoreCase(strUnitFullName)) {    // Calories
        	strUnitAbbreviation = "cal";
        } else if (getString(R.string.kilocalorie_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilocalories
        	strUnitAbbreviation = "kcal";
        } else if (getString(R.string.btu_unit).equalsIgnoreCase(strUnitFullName)) {    // British Thermal Units
        	strUnitAbbreviation = "BTU";
        } else if (getString(R.string.watt_unit).equalsIgnoreCase(strUnitFullName)) {    // Watts
        	strUnitAbbreviation = "W";
        } else if (getString(R.string.kilowatt_unit).equalsIgnoreCase(strUnitFullName)) {    // Kilowatts
        	strUnitAbbreviation = "kW";
        } else if (getString(R.string.megawatt_unit).equalsIgnoreCase(strUnitFullName)) {    // Megawatts
        	strUnitAbbreviation = "MW";
        } else if (getString(R.string.calorie_second_unit).equalsIgnoreCase(strUnitFullName)) {    // Calories per second
        	strUnitAbbreviation = "cal/s";
        } else if (getString(R.string.btu_hour_unit).equalsIgnoreCase(strUnitFullName)) {    // BTUs per hour
        	strUnitAbbreviation = "BTU/h";
        } else if (getString(R.string.horsepower_unit).equalsIgnoreCase(strUnitFullName)) {    // Horsepower
        	strUnitAbbreviation = "hp";
        } else if (getString(R.string.celsius_unit).equalsIgnoreCase(strUnitFullName)) {    // Celsius
        	strUnitAbbreviation = "0C";
        } else if (getString(R.string.fahrenheit_unit).equalsIgnoreCase(strUnitFullName)) {    // Fahrenheit
        	strUnitAbbreviation = "0F";
        } else if (getString(R.string.kelvin_unit).equalsIgnoreCase(strUnitFullName)) {    // Kelvin
        	strUnitAbbreviation = "K";
        } else {
        	strUnitAbbreviation = "";
    	}
    	return strUnitAbbreviation;
    }
}
