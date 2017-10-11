package com.cyzapps.SmartMath;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.adapter.MFPAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ActivityReadConstants extends Activity {
	
	public class ConstantDefinition	{
		public String mstrInternalName;
		public String mstrExternalName; // support latin character
		public String mstrComment;
		public MFPNumeric mmfpNumValue;
		
		ConstantDefinition()	{
			mstrInternalName = "";
			mstrExternalName = ""; // support latin character
			mstrComment = "";
			mmfpNumValue = MFPNumeric.ZERO;
		}
		
		ConstantDefinition(String strInternalName, String strExternalName, String strComment, MFPNumeric mfpNumValue)	{
			mstrInternalName = strInternalName;
			mstrExternalName = strExternalName; // support latin character
			mstrComment = strComment;
			mmfpNumValue = mfpNumValue;
		}
		
		public void setConstantDefinition(String strInternalName, String strExternalName, String strComment, MFPNumeric mfpNumValue)	{
			mstrInternalName = strInternalName;
			mstrExternalName = strExternalName; // support latin character
			mstrComment = strComment;
			mmfpNumValue = mfpNumValue;
		}
		
		public String outputConstText()	{
			if (mstrComment.trim().length() == 0)	{
				return mstrExternalName;
			} else	{
				return mstrExternalName + " (" + mstrComment + ")";
			}
		}
		
		public String outputFunction(int nBitsAfterDecimal)	{
			if (nBitsAfterDecimal < 0)	{
				return "get_constant(\"" + mstrInternalName + "\")";
			} else	{
				return "get_constant(\"" + mstrInternalName + "\", " + Integer.toString(nBitsAfterDecimal) + ")";
			}
		}
		
		public String outputValueString(int nBitsAfterDecimal)	{
			if (nBitsAfterDecimal < 0)	{	// no bit limit after decimal point
				return mmfpNumValue.toString();
			} else	{
	            Format format = null;
				if (MFPAdapter.isVeryBigorSmallValue(mmfpNumValue))
				{
	                String strTmp = "";
	                for (int idx = 0; idx < nBitsAfterDecimal; idx ++) {
	                    strTmp += "#";
	                }
                    if (nBitsAfterDecimal == 0)    {
                        format = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
                    } else  {
                        format = new DecimalFormat("0." + strTmp + "E0", new DecimalFormatSymbols(Locale.US));
                    }
	            }
	            else
	            {
	                String strTmp = "";
	                for (int idx = 0; idx < nBitsAfterDecimal; idx ++) {
	                    strTmp += "#";
	                }
                    if (nBitsAfterDecimal == 0)    {
                        format = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
                    } else  {
                        String strValueString = mmfpNumValue.toString();
                        int decimalIndex = strValueString.indexOf( '.' );
                        int nNumofZerosB4SigDig = 0;
                        if (decimalIndex != -1)	{	// this means no decimal point, it is an integer
                        	int idx = 0;
                        	for (idx = 0; idx < strValueString.length(); idx ++)	{
                        		if (strValueString.charAt(idx) >= '1' && strValueString.charAt(idx) <= '9')	{
                        			break;	// siginificant digit start from here.
                        		}
                        	}
                        	if (idx > decimalIndex)	{
                        		nNumofZerosB4SigDig = idx - decimalIndex - 1;
                        	}
                        }
                        String strTmpZeroB4SigDig = "";
                        for (int idx = 0; idx < nNumofZerosB4SigDig; idx ++) {
                        	strTmpZeroB4SigDig += "#";
                        }
                        format = new DecimalFormat("0." + strTmpZeroB4SigDig + strTmp, new DecimalFormatSymbols(Locale.US));
                    }
	            }
				return format.format(mmfpNumValue.toBigDecimal());
			}			
		}
	}
	
	public class ConstantArrayAdapter extends ArrayAdapter<String> {
		private final Activity mcontext;
		private final String[] mstrarrayConsts;
		private final String[] mstrarrayFunctions;
		private final String[] mstrarrayValues;

		public class ViewHolder {
			public TextView txtViewConst;
			public TextView txtViewValue;
		}

		public ConstantArrayAdapter(Activity context, String[] strarrayConsts, String[] strarrayFunctions, String[] strarrayValues) {
			super(context, R.layout.const_value_child, strarrayConsts);
			this.mcontext = context;
			this.mstrarrayConsts = strarrayConsts;
			this.mstrarrayFunctions = strarrayFunctions;
			this.mstrarrayValues = strarrayValues;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = mcontext.getLayoutInflater();
				rowView = inflater.inflate(R.layout.const_value_child, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.txtViewConst = (TextView) rowView.findViewById(R.id.txtViewConst);
				viewHolder.txtViewValue = (TextView) rowView.findViewById(R.id.txtViewValue);
				rowView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) rowView.getTag();
			String strConst = mstrarrayConsts[position];
			holder.txtViewConst.setText(Html.fromHtml(strConst));
			String strFunction = mstrarrayFunctions[position];
			String strValue = mstrarrayValues[position];
			holder.txtViewValue.setText(strFunction + " = " + strValue);

            if (position == mnSelectedConstPos)	{
            	// set select selected view.
            	rowView.setBackgroundColor(COLOR_HIGHLIGHTED_BACKGND);
            } else	{
            	rowView.setBackgroundColor(0);
            }
			return rowView;
		}
	} 
	
	public LinkedList<ConstantDefinition> mlistConstants = new LinkedList<ConstantDefinition>();
	
	public ListView mlistViewConstants = null; 
	
	public int mnBitsAfterDecimal = -1;
	
	public static final int COLOR_HIGHLIGHTED_BACKGND = 0xFFCC6666;
	public static final int INT_INVALID_POSITION = -1;
	public int mnSelectedConstPos = INT_INVALID_POSITION;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_constants);
        
        // do not reload MFPAdapter function libs here coz it is a dialog box, ActivitySmartCalc OnCreate will be called
        // when this dialog box is restored. And MFPAdapter function libs will be reload in ActivitySmartCalc OnCreate
        EditText edtTxtNumOfBits = (EditText)findViewById(R.id.edtTextNumOfBits);
    	edtTxtNumOfBits.addTextChangedListener(new TextWatcher()	{

			@Override
			public void afterTextChanged(Editable arg0) {
				if (arg0.toString().trim().length() == 0)	{
					mnBitsAfterDecimal = -1;
				} else	{
			    	try {
						mnBitsAfterDecimal = Integer.parseInt(arg0.toString());
			    	} catch	(NumberFormatException e){
			    		mnBitsAfterDecimal = -1;
			    	}
				}
				setAdapter(mnSelectedConstPos);				
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
				
			}
    		
    	});
    	
    	String strUnit = "";
        addConstant("pi", "\u03C0", getString(R.string.pi_comment) + strUnit, MFPNumeric.PI);
        strUnit = "";
        addConstant("e", "e", getString(R.string.e_comment) + strUnit, MFPNumeric.E);
        strUnit = " [m/s]";
        addConstant("light_speed_in_vacuum", "c", getString(R.string.light_speed_in_vacuum_comment) + strUnit, new MFPNumeric("299792458"));
        strUnit = " [m<small><sup>3</sup></small>*kg<small><sup>-1</sup></small>*s<small><sup>-2</sup></small>]";
        addConstant("gravitational_constant", "G", getString(R.string.gravitational_constant_comment) + strUnit, new MFPNumeric("6.67428E-11"));
        strUnit = " [J*s]";
        addConstant("planck_constant", "h", getString(R.string.planck_constant_comment) + strUnit, new MFPNumeric("6.62606896E-34"));
        strUnit = " [N*A<small><sup>-2</sup></small>]";
        addConstant("magnetic_constant", "\u03BC0", getString(R.string.magnetic_constant_comment) + strUnit, new MFPNumeric("1.256637061E-6"));
        strUnit = " [F*m<small><sup>-1</sup></small>]";
        addConstant("electric_constant", "\u04040", getString(R.string.electric_constant_comment) + strUnit, new MFPNumeric("8.854187817E-12"));
        strUnit = " [c]";
        addConstant("elementary_charge_constant", "e", getString(R.string.elementary_charge_constant_comment) + strUnit, new MFPNumeric("1.602176487E-19"));
        strUnit = " [mol<small><sup>-1</sup></small>]";
        addConstant("avogadro_constant", "N<small><sub>A</sub></small>", getString(R.string.avogadro_constant_comment) + strUnit, new MFPNumeric("6.02214179E23"));
        strUnit = " [C*mol<small><sup>-1</sup></small>]";
        addConstant("faraday_constant", "F", getString(R.string.faraday_constant_comment) + strUnit, new MFPNumeric("96485.3399"));
        strUnit = " [J*mol<small><sup>-1</sup></small>*K<small><sup>-1</sup></small>]";
        addConstant("molar_gas_constant", "R", getString(R.string.molar_gas_constant_comment) + strUnit, new MFPNumeric("8.314472"));
        strUnit = " [J*K<small><sup>-1</sup></small>]";
        addConstant("boltzman_constant", "k", getString(R.string.boltzman_constant_comment) + strUnit, new MFPNumeric("1.3806504E-23"));
        strUnit = " [m*s<small><sup>-2</sup></small>]";
        addConstant("standard_gravity", "g", getString(R.string.standard_gravity_comment) + strUnit, new MFPNumeric("9.80665"));
    	
    	mlistViewConstants = (ListView) findViewById(R.id.listViewConstantValues);
    	mlistViewConstants.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				int nPrevSelectedConstPos = mnSelectedConstPos;
				mnSelectedConstPos =position;
		        if (mnSelectedConstPos >= mlistConstants.size())	{
		        	mnSelectedConstPos = INT_INVALID_POSITION;
		        }
		        if (mnSelectedConstPos != INT_INVALID_POSITION)	{
	            	v.setBackgroundColor(COLOR_HIGHLIGHTED_BACKGND);
	            	// if the new clicked item is not previously selected item.
	            	if (nPrevSelectedConstPos != INT_INVALID_POSITION && mnSelectedConstPos != nPrevSelectedConstPos
	            			&& parent.getChildAt(nPrevSelectedConstPos - parent.getFirstVisiblePosition()) != null)	{
	            		// set the last select not hightlighted.
	            		parent.getChildAt(nPrevSelectedConstPos - parent.getFirstVisiblePosition()).setBackgroundColor(0);
	            	}
		        }
		    	Button btnCpyConstValue = (Button)findViewById(R.id.copy_constant_value_btn);
		    	Button btnCpyFunction = (Button)findViewById(R.id.copy_function_btn);
		    	if (mnSelectedConstPos == INT_INVALID_POSITION)	{
		    		btnCpyConstValue.setEnabled(false);
		    		btnCpyFunction.setEnabled(false);
		    	} else	{
		    		btnCpyConstValue.setEnabled(true);
		    		btnCpyFunction.setEnabled(true);
		    	}
			}

        });
    	setAdapter(mnSelectedConstPos); 
    	
    	Button btnCpyConstValue = (Button)findViewById(R.id.copy_constant_value_btn);
    	btnCpyConstValue.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				if (mnSelectedConstPos != INT_INVALID_POSITION)	{
					Intent intent = new Intent();
					if (intent != null) {
						Bundle b = new Bundle();
						if (b != null) {
							b.putString("ConstantValue",
									mlistConstants.get(mnSelectedConstPos).outputValueString(mnBitsAfterDecimal));
							intent.putExtra("android.intent.extra.FunCalc", b);
						}
					}
					getParent().setResult(Activity.RESULT_OK, intent);
				}
			   	finish();
			}
    		
    	});
    	Button btnCpyFunction = (Button)findViewById(R.id.copy_function_btn);
    	btnCpyFunction.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				if (mnSelectedConstPos != INT_INVALID_POSITION)	{
					Intent intent = new Intent();
					if (intent != null) {
						Bundle b = new Bundle();
						if (b != null) {
							b.putString("ConstantFunction",
									mlistConstants.get(mnSelectedConstPos).outputFunction(mnBitsAfterDecimal));
							intent.putExtra("android.intent.extra.FunCalc", b);
						}
					}
					getParent().setResult(Activity.RESULT_OK, intent);
				}
			   	finish();
			}
    		
    	});
    	if (mnSelectedConstPos == INT_INVALID_POSITION)	{
    		btnCpyConstValue.setEnabled(false);
    		btnCpyFunction.setEnabled(false);
    	} else	{
    		btnCpyConstValue.setEnabled(true);
    		btnCpyFunction.setEnabled(true);
    	}
    	
    	Button btnClose = (Button)findViewById(R.id.close_read_contants_btn);
    	btnClose.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
			   	finish();
			}
    		
    	});
    	
     }
    
    public void addConstant(String strInternalName, String strExternalName, String strComment, MFPNumeric mfpNumValue)	{
    	boolean bUpdateConstant = false;
    	
    	for (int idx = 0; idx < mlistConstants.size(); idx ++)	{
    		if (mlistConstants.get(idx).mstrInternalName.equalsIgnoreCase(strInternalName))	{
    			// replace the existing one
    			bUpdateConstant = true;
    			mlistConstants.get(idx).setConstantDefinition(strInternalName, strExternalName, strComment, mfpNumValue);
    		}
    	}
    	if (bUpdateConstant == false)	{
    		ConstantDefinition constDefinition = new ConstantDefinition(strInternalName,
    																	strExternalName,
    																	strComment,
    																	mfpNumValue);
    		mlistConstants.addLast(constDefinition);
    	}
    	// setAdapter(mnSelectedConstPos);	// do not update view coz waste of resource.
    }
    
    public void setAdapter(int nSelectedPosition)	{
        // mviewSelectedChild is set in getView function which is called back
        // when getAllFiles function is called.
        mnSelectedConstPos = nSelectedPosition;
        if (mnSelectedConstPos >= mlistConstants.size())	{
        	mnSelectedConstPos = INT_INVALID_POSITION;
        }
		String[] strarrayConsts = new String[mlistConstants.size()];
		String[] strarrayFunctions = new String[mlistConstants.size()];
		String[] strarrayValues = new String[mlistConstants.size()];
		for (int idx = 0; idx < mlistConstants.size(); idx ++)	{
			strarrayConsts[idx] = mlistConstants.get(idx).outputConstText();
			strarrayFunctions[idx] = mlistConstants.get(idx).outputFunction(mnBitsAfterDecimal);
			strarrayValues[idx] = mlistConstants.get(idx).outputValueString(mnBitsAfterDecimal);
		}
		ConstantArrayAdapter caa = new ConstantArrayAdapter(this, strarrayConsts, strarrayFunctions, strarrayValues);
        mlistViewConstants.setAdapter(caa);
    }
}
