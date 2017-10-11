package com.cyzapps.SmartMath;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Set;

import com.cyzapps.SmartMath.InputPadMgrEx.InputKey;
import com.cyzapps.SmartMath.InputPadMgrEx.InputKeyRow;
import com.cyzapps.SmartMath.InputPadMgrEx.TableInputPad;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ActivityCfgKeyPad extends Activity {
	private static final int ITEM0 = Menu.FIRST;
	private static final int ITEM1 = Menu.FIRST + 1;
	private static final int ITEM2 = Menu.FIRST + 2;
	private static final int ITEM3 = Menu.FIRST + 3;
	private static final int ITEM4 = Menu.FIRST + 4;
	private static final int ITEM5 = Menu.FIRST + 5;

	private static final int KEY_CONTEXT_MENU_CUT = Menu.FIRST + 6;
	private static final int KEY_CONTEXT_MENU_COPY = Menu.FIRST + 7;
	private static final int KEY_CONTEXT_MENU_PASTE_BEFORE = Menu.FIRST + 8;
	private static final int KEY_CONTEXT_MENU_PASTE_ON = Menu.FIRST + 9;
	private static final int KEY_CONTEXT_MENU_PASTE_AFTER = Menu.FIRST + 10;;
	private static final int KEY_CONTEXT_MENU_CREATE_BEFORE = Menu.FIRST + 11; 
	private static final int KEY_CONTEXT_MENU_CREATE_AFTER = Menu.FIRST + 12;
	private static final int KEY_CONTEXT_MENU_DELETE = Menu.FIRST + 13;
	private static final int KEY_CONTEXT_MENU_EDIT = Menu.FIRST + 14;
	
	private static final Integer[] TEXT_COLOR_LIST = new Integer[]{Color.BLACK, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.RED, Color.YELLOW, Color.WHITE};

    public ColorStateList mcslDefaultTextColors = null;
    public static final int ERROR_TEXT_COLOR = Color.RED;
    
	EditText medtTxtInputPadName;
	EditText medtTxtInputPadLongName;
	EditText medtTxtInputPadShortName;
	EditText medtTxtInputPadWrappedName1;
	EditText medtTxtInputPadWrappedName2;
	EditText medtTxtKeyHeight;
	Spinner mspinnerNumofCols;
	int mnMaxNumofCols = 4;
	int mnBtnStandardHeight = 10;
	CheckBox mchkBoxVisible;

	public static final String INPUTPAD_SC_CONFIG = "inputpad.cfg";
	public static final String INPUTPAD_CL_CONFIG = "inputpad_cl.cfg";
	public static final String INPUTPAD_SE_CONFIG = "inputpad_se.cfg";
	public static final String INPUTPAD_INTEG_PLOT_CONFIG = "inputpad_integ_plot.cfg";
	public String mstrInputPadConfig = INPUTPAD_SC_CONFIG;
	public int mnFile2Config = 0;	// 0 means INPUTPAD_SC_CONFIG, 1 means INPUTPAD_CL_CONFIG, 2 means INPUTPAD_SE_CONFIG, 3 means INPUTPAD_INTEG_PLOT_CONFIG, default is 0.
	public float mfInputKeyTextSize = (float) 18.0;
	public String mstrOriginallyLoadedInputPadsCfg = "";
	public LinkedList<TableInputPad> mlistInputPads = new LinkedList<TableInputPad>();
	public LinkedList<View> mlistInputPadViews = new LinkedList<View>();
	public LinkedList<View> mlistInputPadViewsLand = new LinkedList<View>();
	public int mnSelectedInputPadIdx = -1;
	public int mnDialogState = 0;	// no dialog
	public String mstrCannotSaveError = "";
	public boolean mbCfgFileChanged = false;
	public LinearLayout mlLayoutholderInputPadCfg = null;
	
	public class InputKeyReference	{
		public int mnInputPadIdx = -1;
		public boolean mbIsLand = false;
		public int mnInputKeyIdx = -1;
		
		private InputKeyReference()	{}
		
		public boolean isEmpty()	{
			if ((mnInputPadIdx == -1) && (!mbIsLand) && (mnInputKeyIdx == -1))	{
				return true;
			} else	{
				return false;
			}
		}
		
		public boolean isValid()	{
			if ((mnInputPadIdx < 0) || (mnInputPadIdx >= mlistInputPads.size()))	{
				return false;
			}
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else {
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			if ((mnInputKeyIdx < 0) || (mnInputKeyIdx >= listInputKeys.size()))	{
				return false;
			}
			return true;
		}
		
		public boolean isSame(InputKeyReference inputKeyRef)	{
			if (inputKeyRef != null)	{
				if (mnInputPadIdx == inputKeyRef.mnInputPadIdx && mbIsLand == inputKeyRef.mbIsLand
						&& mnInputKeyIdx == inputKeyRef.mnInputKeyIdx)	{
					return true;
				}
			}
			return false;
		}
		
		public void copy(InputKeyReference from)	{
			if (from == null)	{
				// if from is null, copy nothing
				return;
			} else	{
				mnInputPadIdx = from.mnInputPadIdx;
				mbIsLand = from.mbIsLand;
				mnInputKeyIdx = from.mnInputKeyIdx;
			}
		}
		
		public void clear()	{
			mnInputPadIdx = -1;
			mbIsLand = false;
			mnInputKeyIdx = -1;
		}
		
		public InputKey getInputKey()	{
			InputKey inputKey = null;
			if (!isValid())	{
				inputKey = null;
			} else	{
				if (!mbIsLand)	{
					inputKey = mlistInputPads.get(mnInputPadIdx).getListOfKeys().get(mnInputKeyIdx);
				} else {
					inputKey = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand().get(mnInputKeyIdx);
				}
			}
			return inputKey;
		}
		
		public void createInputKeyToLeft()	{
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeys.add(mnInputKeyIdx, new InputKey());
			updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
		}
		
		public boolean insertInputKeyToLeft(InputKeyReference inputKeyRefSrc)	{
			if (!isValid() || inputKeyRefSrc == null || !inputKeyRefSrc.isValid())	{
				return false;
			}
			InputKey inputKeySrc = new InputKey();
			inputKeySrc.copy(inputKeyRefSrc.getInputKey());	// has to use copy instead of reference otherwise may change two keys together.
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeys.add(mnInputKeyIdx, inputKeySrc);
			updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
			return true;
		}

		public boolean moveInputKeyToLeft(InputKeyReference inputKeyRefSrc)	{
			if (!isValid() || inputKeyRefSrc == null || !inputKeyRefSrc.isValid())	{
				return false;
			} else if (isSame(inputKeyRefSrc))	{
				return true;	// move itself to its left, do nothing.
			}
			int nToDeletedKeyIdx = inputKeyRefSrc.mnInputKeyIdx;
			if (mnInputPadIdx == inputKeyRefSrc.mnInputPadIdx && mbIsLand == inputKeyRefSrc.mbIsLand
					&& mnInputKeyIdx < inputKeyRefSrc.mnInputKeyIdx)	{
				// source key and dest key are in the same pad and source key is after dest key
				nToDeletedKeyIdx ++;
			}
			InputKey inputKeySrc = new InputKey();
			inputKeySrc.copy(inputKeyRefSrc.getInputKey());
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeys.add(mnInputKeyIdx, inputKeySrc);
			updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
			LinkedList<InputKey> listInputKeysSrc;
			if (!inputKeyRefSrc.mbIsLand)	{
				listInputKeysSrc = mlistInputPads.get(inputKeyRefSrc.mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeysSrc = mlistInputPads.get(inputKeyRefSrc.mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeysSrc.remove(nToDeletedKeyIdx);
			updateInputPadKeys(inputKeyRefSrc.mnInputPadIdx, listInputKeysSrc, inputKeyRefSrc.mbIsLand);
			return true;
		}

		public void createInputKeyToRight()	{
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeys.add(mnInputKeyIdx + 1, new InputKey());
			updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
		}
		
		public boolean insertInputKeyToRight(InputKeyReference inputKeyRefSrc)	{
			if (!isValid() || inputKeyRefSrc == null || !inputKeyRefSrc.isValid())	{
				return false;
			}
			InputKey inputKeySrc = new InputKey();
			inputKeySrc.copy(inputKeyRefSrc.getInputKey());
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeys.add(mnInputKeyIdx + 1, inputKeySrc);
			updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
			return true;
		}

		public boolean moveInputKeyToRight(InputKeyReference inputKeyRefSrc)	{
			if (!isValid() || inputKeyRefSrc == null || !inputKeyRefSrc.isValid())	{
				return false;
			} else if (isSame(inputKeyRefSrc))	{
				return true;	// move itself to its right, do nothing.
			}
			int nToDeletedKeyIdx = inputKeyRefSrc.mnInputKeyIdx;
			if (mnInputPadIdx == inputKeyRefSrc.mnInputPadIdx && mbIsLand == inputKeyRefSrc.mbIsLand
					&& mnInputKeyIdx < inputKeyRefSrc.mnInputKeyIdx)	{
				// source key and dest key are in the same pad and source key is after dest key
				nToDeletedKeyIdx ++;
			}
			InputKey inputKeySrc = new InputKey();
			inputKeySrc.copy(inputKeyRefSrc.getInputKey());
			LinkedList<InputKey> listInputKeys;
			if (!mbIsLand)	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeys.add(mnInputKeyIdx + 1, inputKeySrc);
			updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
			LinkedList<InputKey> listInputKeysSrc;
			if (!inputKeyRefSrc.mbIsLand)	{
				listInputKeysSrc = mlistInputPads.get(inputKeyRefSrc.mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeysSrc = mlistInputPads.get(inputKeyRefSrc.mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeysSrc.remove(nToDeletedKeyIdx);
			updateInputPadKeys(inputKeyRefSrc.mnInputPadIdx, listInputKeysSrc, inputKeyRefSrc.mbIsLand);
			return true;
		}

		public boolean replaceInputKey(InputKeyReference inputKeyRefSrc)	{
			// copy and replace
			if (!isValid() || inputKeyRefSrc == null || !inputKeyRefSrc.isValid())	{
				return false;
			} else if (isSame(inputKeyRefSrc))	{
				return true;	// replace itself, do nothing.
			}
			InputKey inputKeySrc = inputKeyRefSrc.getInputKey();
			InputKey inputKeyDest = getInputKey();
			inputKeyDest.copy(inputKeySrc);
			return true;
		}
		
		public boolean moveInputKeyToReplace(InputKeyReference inputKeyRefSrc)	{
			if (!isValid() || inputKeyRefSrc == null || !inputKeyRefSrc.isValid())	{
				return false;
			} else if (isSame(inputKeyRefSrc))	{
				return true;	// move itself to replace itself, do nothing.
			}
			InputKey inputKeySrc = inputKeyRefSrc.getInputKey();
			InputKey inputKeyDest = getInputKey();
			inputKeyDest.copy(inputKeySrc);
			LinkedList<InputKey> listInputKeysSrc;
			if (!inputKeyRefSrc.mbIsLand)	{
				listInputKeysSrc = mlistInputPads.get(inputKeyRefSrc.mnInputPadIdx).getListOfKeys();
			} else	{
				listInputKeysSrc = mlistInputPads.get(inputKeyRefSrc.mnInputPadIdx).getListOfKeysLand();
			}
			listInputKeysSrc.remove(inputKeyRefSrc.mnInputKeyIdx);
			updateInputPadKeys(inputKeyRefSrc.mnInputPadIdx, listInputKeysSrc, inputKeyRefSrc.mbIsLand);
			return true;
		}

		public boolean deleteInputKey()	{
			if (isValid())	{
				LinkedList<InputKey> listInputKeys;
				if (!mbIsLand)	{
					listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeys();
				} else	{
					listInputKeys = mlistInputPads.get(mnInputPadIdx).getListOfKeysLand();
				}
				listInputKeys.remove(mnInputKeyIdx);
				updateInputPadKeys(mnInputPadIdx, listInputKeys, mbIsLand);
				return true;
			}
			return false;
		}
	}

	public class SpinnerAdapter extends ArrayAdapter<Integer> {
		Context context;
		Integer[] items = new Integer[] {};
		private int textSize=20; //initial default textsize

        public SpinnerAdapter(final Context context, final int textViewResourceId, final Integer[] objects) {
            super(context, textViewResourceId, objects);
            this.items = objects;
            this.context = context;

        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position].toString());
            //tv.setTextColor(Color.BLUE);
            tv.setTextSize(textSize);
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            // android.R.id.text1 is default text view in resource of the android.
            // android.R.layout.simple_spinner_item is default layout in resources of android.

            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(items[position].toString());
            //tv.setTextColor(Color.BLUE);
            tv.setTextSize(textSize);
            return convertView;
        }

        //set the textsize
        public void setSpinnerTextSize(int size){
            textSize= size;
        }

        //return the textsize
        public int getSpinnerTextSize(){
            return textSize;
        }
	}

	private String mstrClipboardMode = "";
	public InputKeyReference minputKeyRefClipboard = new InputKeyReference();	// input key reference in clipboard
	public InputKeyReference minputKeyRefUnderOpt = new InputKeyReference();	// input key reference under operation
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.inputpad_config);
		setContentView(R.layout.inputpad_cfg);
        //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null)	{
        	//Next extract the values using the key as
        	mnFile2Config = bundle.getInt("File_2_Config");
        	if (mnFile2Config == 0)	{
        		mstrInputPadConfig = INPUTPAD_SC_CONFIG;
        	} else if (mnFile2Config == 1) {
           		mstrInputPadConfig = INPUTPAD_CL_CONFIG;       		
        	} else if (mnFile2Config == 2) {
           		mstrInputPadConfig = INPUTPAD_SE_CONFIG;        		
        	} else if (mnFile2Config == 3) {
           		mstrInputPadConfig = INPUTPAD_INTEG_PLOT_CONFIG;       		
        	} else {
           		mstrInputPadConfig = INPUTPAD_SC_CONFIG;        		
        	}
        }
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenOrientation = getResources().getConfiguration().orientation;
		mfInputKeyTextSize = getKeyTextSize(nScreenSizeCategory, nScreenOrientation);
		setBtnIcons(nScreenSizeCategory, nScreenOrientation);
		mnBtnStandardHeight = calcInputKeyBtnHeight();
		
		if (savedInstanceState == null || !restoreInstanceState(savedInstanceState))	{	// onCreate is called at very beginning
	        InputStream inputStreamPads = null;
			try {
				// first try to read input pads config file in SD card
				inputStreamPads = new FileInputStream(MFPFileManagerActivity.getConfigFolderFullPath()
													+ MFPFileManagerActivity.STRING_PATH_DIV
													+ mstrInputPadConfig);
			} catch (FileNotFoundException e0) {
				try	{
					// if unsuccessful, try to read input pads config file in assets
					inputStreamPads = getAssets().open(mstrInputPadConfig);
				} catch (IOException e) {
					// still unsuccessful, load nothing.
				}
			}
			mlistInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
			mstrOriginallyLoadedInputPadsCfg = InputPadMgrEx.writeInputPadsToXML(mlistInputPads);
			if (mlistInputPads.size() == 0)	{
				mnSelectedInputPadIdx = -1;
			} else	{
				mnSelectedInputPadIdx = 0;
			}
			minputKeyRefClipboard = new InputKeyReference();
			mstrClipboardMode = "";
		} else	{	// onCreate is called after restore
			// reload from savedInstance. Use reference, not copy for all the elements.
			if (mlistInputPads.size() != 0)	{
				// has inputpad(s)
				if (mnSelectedInputPadIdx < 0)	{
					mnSelectedInputPadIdx = 0;
				} else if (mnSelectedInputPadIdx >= mlistInputPads.size())	{
					mnSelectedInputPadIdx = mlistInputPads.size() - 1;
				}
			} else	{
				mnSelectedInputPadIdx = -1;
			}
		}
		
		populateInputPads(mlistInputPads);
		
		medtTxtInputPadName = (EditText)findViewById(R.id.edtTxtInputPadName);
		medtTxtInputPadLongName = (EditText)findViewById(R.id.edtTxtInputPadLongName);
		medtTxtInputPadShortName = (EditText)findViewById(R.id.edtTxtInputPadShortName);
		medtTxtInputPadWrappedName1 = (EditText)findViewById(R.id.edtTxtInputPadWrappedName1);
		medtTxtInputPadWrappedName2 = (EditText)findViewById(R.id.edtTxtInputPadWrappedName2);
		
		/*
		 *  use different id for number of columns spinner and key height edit text at portrait & landscape coz
		 *  Android automatically restore the content of components after orientation change. This is fine for
		 *  all the names but the number of columns and key height in portrait could be different from landscape.
		 */
		if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
			mspinnerNumofCols = (Spinner)findViewById(R.id.spinnerNumofColsLand);
			medtTxtKeyHeight = (EditText)findViewById(R.id.edtTxtTimesStandardHeightLand);
		} else	{
			mspinnerNumofCols = (Spinner)findViewById(R.id.spinnerNumofCols);
			medtTxtKeyHeight = (EditText)findViewById(R.id.edtTxtTimesStandardHeight);
		}
		mnMaxNumofCols = getMaxNumofCols(nScreenSizeCategory, nScreenOrientation);
		Integer[] array_spinner = new Integer[mnMaxNumofCols];
		for (int indx = 0; indx < mnMaxNumofCols; indx ++)	{
			array_spinner[indx] = Integer.valueOf(indx + 1);
		}
		SpinnerAdapter adapterArray = new SpinnerAdapter(this, android.R.layout.simple_spinner_item, array_spinner);
		mspinnerNumofCols.setAdapter(adapterArray);
		
		mchkBoxVisible = (CheckBox)findViewById(R.id.chkboxVisibility);
		mlLayoutholderInputPadCfg = (LinearLayout)findViewById(R.id.holderInputPadCfg);
		
		setInputPadCfgFont(mfInputKeyTextSize);
		updateInputPadCfgWindow();
		
		medtTxtInputPadName.addTextChangedListener(new TextWatcher()	{

			@Override
			public void afterTextChanged(Editable arg0) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
		    		TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		inputPad.mstrName = arg0.toString().trim();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing
			}
			
		});
		
		medtTxtInputPadLongName.addTextChangedListener(new TextWatcher()	{
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
					TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		inputPad.mstrLongName = arg0.toString().trim();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing
			}
			
		});
		
		medtTxtInputPadWrappedName1.addTextChangedListener(new TextWatcher()	{
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
					TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		inputPad.mstrWrappedName1 = arg0.toString().trim();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing
			}
			
		});
		
		medtTxtInputPadWrappedName2.addTextChangedListener(new TextWatcher()	{
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
		    		TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		inputPad.mstrWrappedName2 = arg0.toString().trim();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing
			}
			
		});
		
		medtTxtInputPadShortName.addTextChangedListener(new TextWatcher()	{
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
		    		TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		inputPad.mstrShortName = arg0.toString().trim();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// Do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing
			}
			
		});
		
		mspinnerNumofCols.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				int nNumofCols = arg2 + 1;
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
		    		TableInputPad tableInputPad = (TableInputPad)mlistInputPads.get(mnSelectedInputPadIdx);
			    	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)	{
			    		if (tableInputPad.mnNumofColumnsLand != nNumofCols)	{	// do not need to update if number of columns not change
				    		tableInputPad.mnNumofColumnsLand = nNumofCols;
				    		updateInputPadKeys(mnSelectedInputPadIdx, tableInputPad.getListOfKeysLand(), true);
				    		updateInputPad(mnSelectedInputPadIdx, true, true);
			    		}
			    	} else	{
			    		if (tableInputPad.mnNumofColumns != nNumofCols)	{	// do not need to update if number of columns not change
				    		tableInputPad.mnNumofColumns = nNumofCols;
				    		updateInputPadKeys(mnSelectedInputPadIdx, tableInputPad.getListOfKeys(), false);
				    		updateInputPad(mnSelectedInputPadIdx, false, false);
			    		}
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
			}
			
		});
		
		mcslDefaultTextColors = medtTxtKeyHeight.getTextColors();
		medtTxtKeyHeight.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
		    		TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		double dKeyHeight = 0.0;
					try {
						dKeyHeight = Double.parseDouble(s.toString());
						if (dKeyHeight < 0.25 || dKeyHeight > 4)	{
							dKeyHeight = 0;
						}
					} catch(NumberFormatException e)	{
						
					}
					if (dKeyHeight == 0)	{
						// input is not valid
						medtTxtKeyHeight.setTextColor(ERROR_TEXT_COLOR);
					} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)	{
						if (inputPad.mdKeyHeightLand != dKeyHeight) {
							inputPad.mdKeyHeightLand = dKeyHeight;
							updateInputPad(mnSelectedInputPadIdx, true, true);
						}
						medtTxtKeyHeight.setTextColor(mcslDefaultTextColors);
					} else { // portrait.
						if (inputPad.mdKeyHeight != dKeyHeight) {
							inputPad.mdKeyHeight = dKeyHeight;
							updateInputPad(mnSelectedInputPadIdx, false, false);
						}
						medtTxtKeyHeight.setTextColor(mcslDefaultTextColors);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				
			}
			
		});
		
		mchkBoxVisible.setOnCheckedChangeListener(new OnCheckedChangeListener()	{

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (mnSelectedInputPadIdx >= 0 && mnSelectedInputPadIdx < mlistInputPads.size())	{
		    		TableInputPad inputPad = mlistInputPads.get(mnSelectedInputPadIdx);
		    		inputPad.mbVisible = arg1;
				}
			}
			
		});
		
		Button btnPrevInputPad = (Button)findViewById(R.id.btnPrevInputPad);
		btnPrevInputPad.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				if (mnSelectedInputPadIdx >= 1)	{
					mnSelectedInputPadIdx --;
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)	{
						loadInputPadDescription(mlistInputPads.get(mnSelectedInputPadIdx), true);
						updateLayoutHolder(mlistInputPadViewsLand.get(mnSelectedInputPadIdx), true);
					} else	{
						loadInputPadDescription(mlistInputPads.get(mnSelectedInputPadIdx), false);
						updateLayoutHolder(mlistInputPadViews.get(mnSelectedInputPadIdx), true);
					}
					if (mnSelectedInputPadIdx == 0)	{
						manipulateCompsByConditions("leftmost");
					} else {
						manipulateCompsByConditions("normal");
					}
				}
			}
			
		});
		
		Button btnNextInputPad = (Button)findViewById(R.id.btnNextInputPad);
		btnNextInputPad.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				if (mnSelectedInputPadIdx < mlistInputPads.size() - 1)	{
					mnSelectedInputPadIdx ++;
					if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)	{
						loadInputPadDescription(mlistInputPads.get(mnSelectedInputPadIdx), true);
						updateLayoutHolder(mlistInputPadViewsLand.get(mnSelectedInputPadIdx), true);
					} else	{
						loadInputPadDescription(mlistInputPads.get(mnSelectedInputPadIdx), false);
						updateLayoutHolder(mlistInputPadViews.get(mnSelectedInputPadIdx), true);
					}
					if (mnSelectedInputPadIdx == mlistInputPads.size() - 1)	{
						manipulateCompsByConditions("rightmost");
					} else {
						manipulateCompsByConditions("normal");
					}
				}
			}
			
		});
		
		if (mnDialogState == 1)	{
			showSaveOrNotDlg();
		} else if (mnDialogState == 2)	{
			showCannotSaveDlg(mstrCannotSaveError, true);	// exit
		} else if (mnDialogState == 3)	{
			showCannotSaveDlg(mstrCannotSaveError, false);	// do not exit
		}
	}
	
	public static float getKeyTextSize(int nScreenSizeCategory, int nScreenOrientation)	{
		float fInputKeyTextSize;
		if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL) {     
	    	fInputKeyTextSize = 18;
	    } else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE) {
	    	fInputKeyTextSize = 24;
	    } else if (nScreenSizeCategory >= Configuration.SCREENLAYOUT_SIZE_LARGE + 1)	{	//	xlarge size
	    	// Configuration.SCREENLAYOUT_SIZE_XLARGE is not supported until Android 9, this is to ensure
	    	// compatibility with Android 7.
	    	if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
	    		fInputKeyTextSize = 40;
	    	} else	{
	    		fInputKeyTextSize = 36;
	    	}

	    } else	{	// normal size or undefined size
	    	fInputKeyTextSize = 18;
	    }
		return fInputKeyTextSize;

	}

	public static int getMaxNumofCols(int nScreenSizeCategory, int nScreenOrientation)	{
		int nMaxNumofCols;
		if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL) {
	    	if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
	    		nMaxNumofCols = 8;
	    	} else	{
	    		nMaxNumofCols = 6;
	    	}
	    } else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE) {
	    	if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
	    		nMaxNumofCols = 12;
	    	} else	{
	    		nMaxNumofCols = 8;
	    	}
	    } else if (nScreenSizeCategory >= Configuration.SCREENLAYOUT_SIZE_LARGE + 1)	{	//	xlarge size
	    	// Configuration.SCREENLAYOUT_SIZE_XLARGE is not supported until Android 9, this is to ensure
	    	// compatibility with Android 7.
	    	if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
	    		nMaxNumofCols = 16;
	    	} else	{
	    		nMaxNumofCols = 12;
	    	}

	    } else	{	// normal size or undefined size
	    	if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
	    		nMaxNumofCols = 8;
	    	} else	{
	    		nMaxNumofCols = 6;
	    	}
	    }
		return nMaxNumofCols;
	}
	
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
	
	public void setBtnIcons(int nScreenSizeCategory, int nScreenOrientation)	{
    	Button btnPrevInputPad = (Button)findViewById(R.id.btnPrevInputPad);
    	Button btnNextInputPad = (Button)findViewById(R.id.btnNextInputPad);
		if (nScreenSizeCategory >= Configuration.SCREENLAYOUT_SIZE_LARGE + 1)	{	//	xlarge size
	    	// Configuration.SCREENLAYOUT_SIZE_XLARGE is not supported until Android 9, this is to ensure
	    	// compatibility with Android 7.
	    	btnPrevInputPad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.toleft_land_large, 0, 0, 0);
	    	btnNextInputPad.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.toright_land_large, 0);
	    } else	{	// normal size or undefined size
	    	btnPrevInputPad.setCompoundDrawablesWithIntrinsicBounds(R.drawable.toleft_land, 0, 0, 0);
	    	btnNextInputPad.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.toright_land, 0);
	    }
	}
	
	public void setInputPadCfgFont(float nFontSize)	{
		TextView txtViewInputPadName = (TextView)findViewById(R.id.txtViewInputPadName);
		txtViewInputPadName.setTextSize(nFontSize);
		TextView txtViewInputPadLongName = (TextView)findViewById(R.id.txtViewInputPadLongName);
		txtViewInputPadLongName.setTextSize(nFontSize);
		TextView txtViewInputPadShortName = (TextView)findViewById(R.id.txtViewInputPadShortName);
		txtViewInputPadShortName.setTextSize(nFontSize);
		TextView txtViewInputPadWrappedName = (TextView)findViewById(R.id.txtViewInputPadWrappedName);
		txtViewInputPadWrappedName.setTextSize(nFontSize);
		TextView txtViewBtnKeyHeight = (TextView)findViewById(R.id.txtViewBtnKeyHeight);
		txtViewBtnKeyHeight.setTextSize(nFontSize);
		TextView txtViewTimesStandardHeight = (TextView)findViewById(R.id.txtViewTimesStandardHeight);
		txtViewTimesStandardHeight.setTextSize(nFontSize);
		TextView txtViewPrevInputPad = (TextView)findViewById(R.id.txtViewPrevInputPad);
		txtViewPrevInputPad.setTextSize(nFontSize);
		TextView txtViewNextInputPad = (TextView)findViewById(R.id.txtViewNextInputPad);
		txtViewNextInputPad.setTextSize(nFontSize);
		TextView txtViewNumofCols = (TextView)findViewById(R.id.txtViewNumofCols);
		txtViewNumofCols.setTextSize(nFontSize);
		TextView txtViewVisibility = (TextView)findViewById(R.id.txtViewVisibility);
		txtViewVisibility.setTextSize(nFontSize);
		TextView txtViewNoInputPad = (TextView)findViewById(R.id.txtViewNoInputPad);
		txtViewNoInputPad.setTextSize(nFontSize);
		medtTxtInputPadName.setTextSize(nFontSize);
		medtTxtInputPadLongName.setTextSize(nFontSize);
		medtTxtInputPadShortName.setTextSize(nFontSize);
		medtTxtInputPadWrappedName1.setTextSize(nFontSize);
		medtTxtInputPadWrappedName2.setTextSize(nFontSize);
		medtTxtKeyHeight.setTextSize(nFontSize);
		((SpinnerAdapter)mspinnerNumofCols.getAdapter()).setSpinnerTextSize((int) nFontSize);
		mchkBoxVisible.setTextSize(nFontSize);
		
	}

	public void manipulateCompsByConditions(String strCondition)	{
		//show/hide, enable/disable the components at different conditions
		View vInputPadCfg = findViewById(R.id.lLayoutNormalInputPadCfg);
		View vNoInputPad = findViewById(R.id.txtViewNoInputPad);
		Button btnPrevInputPad = (Button)findViewById(R.id.btnPrevInputPad);
		Button btnNextInputPad = (Button)findViewById(R.id.btnNextInputPad);
		if (strCondition.equalsIgnoreCase("none") == true)	{
			// no inputpad
			vInputPadCfg.setVisibility(View.GONE);
			vNoInputPad.setVisibility(View.VISIBLE);
		} else if (strCondition.equalsIgnoreCase("single") == true)	{
			// only one inputpad
			vInputPadCfg.setVisibility(View.VISIBLE);
			vNoInputPad.setVisibility(View.GONE);
			btnPrevInputPad.setEnabled(false);
			btnNextInputPad.setEnabled(false);
		} else if (strCondition.equalsIgnoreCase("leftmost") == true)	{
			vInputPadCfg.setVisibility(View.VISIBLE);
			vNoInputPad.setVisibility(View.GONE);
			btnPrevInputPad.setEnabled(false);
			btnNextInputPad.setEnabled(true);
		} else if (strCondition.equalsIgnoreCase("rightmost") == true)	{
			vInputPadCfg.setVisibility(View.VISIBLE);
			vNoInputPad.setVisibility(View.GONE);
			btnPrevInputPad.setEnabled(true);
			btnNextInputPad.setEnabled(false);
		} else	{
			vInputPadCfg.setVisibility(View.VISIBLE);
			vNoInputPad.setVisibility(View.GONE);
			btnPrevInputPad.setEnabled(true);
			btnNextInputPad.setEnabled(true);
		}
	}
	
	public View populateInputPad(TableInputPad inputPad, int nIndex, boolean bIsLand)	{
		// shown hidden inputpads as well
		TableLayout inputPadView = new TableLayout(this);
		inputPadView.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
		int nNumofColumns = 1;
		LinkedList<InputKeyRow> listInputKeyRows = null;
		if (bIsLand)	{
			nNumofColumns = inputPad.mnNumofColumnsLand;
			listInputKeyRows = inputPad.mlistKeyRowsLand;
		} else	{
			nNumofColumns = inputPad.mnNumofColumns;
			listInputKeyRows = inputPad.mlistKeyRows;
		}
		int index = 0;	// index of all input keys
		TableRow trLast = null;
		LinkedList<InputKey> listKeysInLastRow = new LinkedList<InputKey>();
		int nActualBtnHeight = (int) (mnBtnStandardHeight * (bIsLand?inputPad.mdKeyHeightLand:inputPad.mdKeyHeight));
		for (int idx = 0; idx < listInputKeyRows.size(); idx ++)	{
			TableRow tr = new TableRow(this);
			tr.setLayoutParams(new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
			trLast = tr;
			LinkedList<InputKey> listInputKeys = listInputKeyRows.get(idx).mlistInputKeys;
			listKeysInLastRow = listInputKeys;
			for (int idx1 = 0; idx1 < listInputKeys.size(); idx1 ++)	{
				Button btn = new Button(this);
				InputKey inputKey = listInputKeys.get(idx1);
				btn.setText(inputKey.mstrKeyShown);
				int nTextColor = Color.argb(inputKey.mcolorForeground.mnAlpha, inputKey.mcolorForeground.mnR,
						inputKey.mcolorForeground.mnG, inputKey.mcolorForeground.mnB);
				btn.setTextColor(nTextColor);
				btn.setTextSize(mfInputKeyTextSize);
				btn.setBackgroundResource(R.drawable.btn_background);
				InputKeyReference inputKeyReference = new InputKeyReference();
				inputKeyReference.mnInputPadIdx = nIndex;
				inputKeyReference.mbIsLand = bIsLand;
				inputKeyReference.mnInputKeyIdx = index;
				btn.setTag(inputKeyReference);
				registerForContextMenu(btn);
				btn.setOnClickListener(new OnClickListener()	{

					@Override
					public void onClick(View arg0) {
						// should not use assign, i.e. minputKeyRefUnderOpt = v.getTag()
						// cause minputKeyRefUnderOpt and tag may change in different scenarios
						minputKeyRefUnderOpt.copy((InputKeyReference)arg0.getTag());
						editInputKey();
					}
					
				});
				btn.setLayoutParams(new LayoutParams( 0, nActualBtnHeight));
				tr.addView(btn);
				index ++;
			}
			inputPadView.addView(tr);
		}
		
		if (trLast == null || listKeysInLastRow.size() >= nNumofColumns)	{
			// we need a new row to place the add button.
			trLast = new TableRow(this);
			inputPadView.addView(trLast);
		}
		
		// now add the "add" button.
		ImageButton imgBtn = new ImageButton(this);
		imgBtn.setBackgroundResource(R.drawable.btn_background);
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if (nScreenSizeCategory >= Configuration.SCREENLAYOUT_SIZE_LARGE + 1)	{
			imgBtn.setImageDrawable(getResources().getDrawable(R.drawable.plus_large));
		} else	{
			imgBtn.setImageDrawable(getResources().getDrawable(R.drawable.plus));
		}
		InputKeyReference inputKeyReference = new InputKeyReference();
		inputKeyReference.mnInputPadIdx = nIndex;
		inputKeyReference.mbIsLand = bIsLand;
		inputKeyReference.mnInputKeyIdx = -1;
		imgBtn.setTag(inputKeyReference);	// so that next time we can identify this button.
		imgBtn.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View arg0) {
				addInputKeys(((InputKeyReference)arg0.getTag()).mnInputPadIdx,
						((InputKeyReference)arg0.getTag()).mbIsLand);
			}
			
		});
		imgBtn.setOnLongClickListener(new OnLongClickListener()	{

			@Override
			public boolean onLongClick(View arg0) {
				addInputKeys(((InputKeyReference)arg0.getTag()).mnInputPadIdx,
						((InputKeyReference)arg0.getTag()).mbIsLand);
				return false;	// does not consume the long click.
			}
			
		});
		imgBtn.setLayoutParams(new LayoutParams( 0, nActualBtnHeight));
		trLast.addView(imgBtn);
		return inputPadView;
	}
	
	public void populateInputPads(LinkedList<TableInputPad> listInputPads)	{
		// load all the input pads.
		mlistInputPadViews.clear();
		mlistInputPadViewsLand.clear();
		for (int index = 0; index < listInputPads.size(); index ++)	{
			TableInputPad inputPad = listInputPads.get(index);
			View vInputPad = populateInputPad(inputPad, index, false);
			View vInputPadLand = populateInputPad(inputPad, index, true);
			if (vInputPad != null && vInputPadLand != null)	{
				mlistInputPadViews.add(vInputPad);
				mlistInputPadViewsLand.add(vInputPadLand);
			}
		}
	}
	
	public void updateInputPadKeys(int nInputPadIdx, LinkedList<InputKey> listKeysNew, boolean bIsLand)	{
		if (nInputPadIdx >= mlistInputPads.size() || nInputPadIdx < 0)	{
			return;
		}
		TableInputPad inputPad = mlistInputPads.get(nInputPadIdx);
		LinkedList<InputKeyRow> listKeyRows = new LinkedList<InputKeyRow>();
		InputKeyRow inputKeyRow = new InputKeyRow();
		int nNumofColumns = bIsLand?inputPad.mnNumofColumnsLand:inputPad.mnNumofColumns;
		inputKeyRow.mlistInputKeys = new LinkedList<InputKey>();
		inputKeyRow.mparent = inputPad;
		inputKeyRow.mbInLandScape = bIsLand;
		listKeyRows.add(inputKeyRow);
		for (InputKey inputKey: listKeysNew)	{
			if (inputKeyRow.mlistInputKeys.size() >= nNumofColumns)	{
				inputKeyRow = new InputKeyRow();
				inputKeyRow.mlistInputKeys = new LinkedList<InputKey>();
				inputKeyRow.mparent = inputPad;
				inputKeyRow.mbInLandScape = bIsLand;
				listKeyRows.add(inputKeyRow);
			}
			inputKeyRow.mlistInputKeys.add(inputKey);
		}
		if (bIsLand)	{
			inputPad.mlistKeyRowsLand = listKeyRows;
		} else	{
			inputPad.mlistKeyRows = listKeyRows;
		}
	}
	
	public void updateInputPad(int nInputPadIdx, boolean bIsLand, boolean bCurrentStateLand)	{
		if (nInputPadIdx >= mlistInputPads.size() || nInputPadIdx < 0)	{
			return;
		}
		View v = populateInputPad(mlistInputPads.get(nInputPadIdx), nInputPadIdx, bIsLand);
		if (bIsLand)	{
			mlistInputPadViewsLand.add(nInputPadIdx, v);
			mlistInputPadViewsLand.remove(nInputPadIdx + 1);
		} else	{
			mlistInputPadViews.add(nInputPadIdx, v);
			mlistInputPadViews.remove(nInputPadIdx + 1);
		}
		if (mnSelectedInputPadIdx == nInputPadIdx && bCurrentStateLand == bIsLand)	{	// update interface
			updateLayoutHolder(v, true);
		}
		
	}
	
	public void loadInputPadDescription(TableInputPad inputPad, boolean bIsLand)	{
		if (inputPad == null)	{
			medtTxtInputPadName.setText("");
			medtTxtInputPadLongName.setText("");
			medtTxtInputPadShortName.setText("");
			medtTxtInputPadWrappedName1.setText("");
			medtTxtInputPadWrappedName2.setText("");
			medtTxtKeyHeight.setText("1");
			mspinnerNumofCols.setSelection(0);
			mchkBoxVisible.setChecked(true);
		} else {
			medtTxtInputPadName.setText(inputPad.mstrName);
			medtTxtInputPadLongName.setText(inputPad.mstrLongName);
			medtTxtInputPadShortName.setText(inputPad.mstrShortName);
			medtTxtInputPadWrappedName1.setText(inputPad.mstrWrappedName1);
			medtTxtInputPadWrappedName2.setText(inputPad.mstrWrappedName2);
			if (inputPad instanceof TableInputPad)	{
				double dKeyHeight = bIsLand?((TableInputPad)inputPad).mdKeyHeightLand:((TableInputPad)inputPad).mdKeyHeight;
				if (dKeyHeight < 0)	{
					dKeyHeight = -dKeyHeight;
				}
				if (dKeyHeight < 0.25)	{
					dKeyHeight = 0.25;
				} else if (dKeyHeight > 4)	{
					dKeyHeight = 4;
				}
				medtTxtKeyHeight.setText(Double.toString(dKeyHeight));
				int nNumofCols = bIsLand?((TableInputPad)inputPad).mnNumofColumnsLand:((TableInputPad)inputPad).mnNumofColumns;
				if (nNumofCols < 1)	{
					nNumofCols = 1;
				} else if (nNumofCols > mnMaxNumofCols)	{
					nNumofCols = mnMaxNumofCols;
				}
				mspinnerNumofCols.setSelection(nNumofCols - 1, true);
			}
			mchkBoxVisible.setChecked(inputPad.mbVisible);
		}
	}
	
	public void updateLayoutHolder(View vToBeAdded, boolean bRemoveAllFirst)	{
		if (bRemoveAllFirst)	{
			mlLayoutholderInputPadCfg.removeAllViews();
		}
		mlLayoutholderInputPadCfg.addView(vToBeAdded);
		vToBeAdded.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		if (vToBeAdded instanceof TableLayout)	{
			int nNumofRows = ((TableLayout)vToBeAdded).getChildCount();
			for (int idx = 0; idx < nNumofRows; idx ++)	{
				TableRow tr = (TableRow)((TableLayout)vToBeAdded).getChildAt(idx);
				tr.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				int nNumofBtns = tr.getChildCount();
				for (int idx1 = 0; idx1 < nNumofBtns; idx1 ++)	{
					View vBtn = tr.getChildAt(idx1);
					// height has been set before.
					vBtn.setLayoutParams(new TableRow.LayoutParams(0, vBtn.getLayoutParams().height, 1));
				}
			}
		}
	}
	
	public void updateInputPadCfgWindow()	{
		if (mlistInputPads.size() == 0)	{
			mnSelectedInputPadIdx = -1;
			manipulateCompsByConditions("none");	// no inputpad.
		} else	{
			if (mnSelectedInputPadIdx < 0)	{
				mnSelectedInputPadIdx = 0;
			}
			if (mnSelectedInputPadIdx >= mlistInputPads.size())	{
				mnSelectedInputPadIdx = mlistInputPads.size() - 1;
			}
			
			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)	{
				loadInputPadDescription(mlistInputPads.get(mnSelectedInputPadIdx), true);
				updateLayoutHolder(mlistInputPadViewsLand.get(mnSelectedInputPadIdx), true);
			} else	{
				loadInputPadDescription(mlistInputPads.get(mnSelectedInputPadIdx), false);
				updateLayoutHolder(mlistInputPadViews.get(mnSelectedInputPadIdx), true);
			}
			
			if (mlistInputPads.size() == 1)	{
				manipulateCompsByConditions("single");	// only one inputpad.
			} else if (mnSelectedInputPadIdx == 0)	{
				manipulateCompsByConditions("leftmost");	// leftmost inputpad.
			} else if (mnSelectedInputPadIdx == mlistInputPads.size() - 1)	{
				manipulateCompsByConditions("rightmost");	// rightmost inputpad.
			} else	{
				manipulateCompsByConditions("normal");	// rightmost inputpad.
			}
		}
	}
	
	public String validateInputPadCfg()	{
		for (int idx = 0; idx < mlistInputPads.size(); idx ++)	{
			for (int idx1 = 0; idx1 < mlistInputPads.size(); idx1 ++)	{
				if (idx != idx1 && mlistInputPads.get(idx).mstrName.trim()
						.equalsIgnoreCase(mlistInputPads.get(idx1).mstrName.trim()))	{
					// two input pad share same name
					return getString(R.string.error_two_inputpads_with_same_name) + ": " + mlistInputPads.get(idx).mstrName.trim();
				}
			}
		}
		return null;	// no error
	}
	
	public void editInputKey()	{
		AlertDialog alertDialogSetKey;
		LayoutInflater factory = LayoutInflater.from(this);
        View v = factory.inflate( R.layout.set_inputkey, null);
        final CheckBox chkBoxCpy2Input = (CheckBox)v.findViewById(R.id.chkBoxCopy2Input);
		final EditText edtTxtShown = (EditText)v.findViewById(R.id.edtTxtShown);
		final Spinner spinnerTxtShownColor = (Spinner)v.findViewById(R.id.spinnerTxtShownColor);
		final EditText edtTxtInput = (EditText)v.findViewById(R.id.edtTxtInput);
		final EditText edtTxtCursorPlace = (EditText)v.findViewById(R.id.edtTxtCursorPlace);
		final Integer[] text_color_list;
		if (!minputKeyRefUnderOpt.isEmpty())	{
			InputKey inputKey = minputKeyRefUnderOpt.getInputKey();
			edtTxtShown.setText(inputKey.mstrKeyShown);
			int nTextColor = Color.argb(inputKey.mcolorForeground.mnAlpha, inputKey.mcolorForeground.mnR,
					inputKey.mcolorForeground.mnG, inputKey.mcolorForeground.mnB);
			int idx = TEXT_COLOR_LIST.length;
			for (idx = 0; idx < TEXT_COLOR_LIST.length; idx ++)	{
				if (nTextColor == TEXT_COLOR_LIST[idx]) {
					break;	// find the color.
				}
			}
			if (idx < TEXT_COLOR_LIST.length) {
				text_color_list = TEXT_COLOR_LIST;
			} else {
				text_color_list = new Integer[TEXT_COLOR_LIST.length + 1];
				text_color_list[0] = nTextColor;
				System.arraycopy(TEXT_COLOR_LIST, 0, text_color_list, 1, TEXT_COLOR_LIST.length);
				idx = 0;
			}
			ColorSpinnerAdapter spinnerArrayAdapter = new ColorSpinnerAdapter(this, text_color_list);
			spinnerTxtShownColor.setAdapter(spinnerArrayAdapter);
			spinnerTxtShownColor.setSelection(idx);
			
			edtTxtInput.setText(inputKey.mstrKeyInput);
			int nCursorPlace = (inputKey.mnCursorPlace < 0)?(inputKey.mnCursorPlace * -1):0;
			if (nCursorPlace > inputKey.mstrKeyInput.length())	{
				nCursorPlace = inputKey.mstrKeyInput.length();
			}
			String strCursorPlace = Integer.valueOf(nCursorPlace).toString();
			edtTxtCursorPlace.setText(strCursorPlace);
		} else {
			text_color_list = null;
		}
        chkBoxCpy2Input.setOnCheckedChangeListener(new OnCheckedChangeListener()	{

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean bIsChecked) {
				if (bIsChecked)	{
					edtTxtInput.setEnabled(false);
					edtTxtInput.setText(edtTxtShown.getText());
				} else	{
					edtTxtInput.setEnabled(true);
				}
			}
        	
        });
        edtTxtShown.addTextChangedListener(new TextWatcher()	{

			@Override
			public void afterTextChanged(Editable arg0) {
				if (chkBoxCpy2Input.isChecked())	{
					edtTxtInput.setText(arg0.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// Do nothing
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// Do nothing
			}
        	
        });
        
		alertDialogSetKey = new AlertDialog.Builder(this)
					.setTitle(R.string.inputkey_config)
					.setView(v)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (!minputKeyRefUnderOpt.isEmpty())	{
								InputKey inputKey = minputKeyRefUnderOpt.getInputKey();
								inputKey.mstrKeyShown = edtTxtShown.getText().toString();
								int nTextColor = Color.BLACK;
								if (text_color_list != null && spinnerTxtShownColor.getSelectedItemPosition() >= 0
										&& spinnerTxtShownColor.getSelectedItemPosition() < text_color_list.length) {
									nTextColor = text_color_list[spinnerTxtShownColor.getSelectedItemPosition()];
								}
								inputKey.mcolorForeground.mnAlpha = Color.alpha(nTextColor);
								inputKey.mcolorForeground.mnR = Color.red(nTextColor);
								inputKey.mcolorForeground.mnG = Color.green(nTextColor);
								inputKey.mcolorForeground.mnB = Color.blue(nTextColor);
								inputKey.mstrKeyInput = edtTxtInput.getText().toString();
								try {
									inputKey.mnCursorPlace = Integer.parseInt(edtTxtCursorPlace.getText().toString()) * -1;
								} catch (NumberFormatException e)	{
									inputKey.mnCursorPlace = 0;
								}
								// update under operation inputpad.
								updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx,
												minputKeyRefUnderOpt.mbIsLand,
												minputKeyRefUnderOpt.mbIsLand);
							}
						}
						
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// Do nothing if press cancel.							
						}
					}).create();
		alertDialogSetKey.show();					
		
	}
	
	public void addInputKeys(final int nInputPadIdx, final boolean bIsLand)	{
		LayoutInflater factory = LayoutInflater.from(this);
        View v = factory.inflate( R.layout.add_inputkeys, null);
		final EditText edtTxtHowManyKeys = (EditText)v.findViewById(R.id.edt_txt_how_many_inputkeys_to_add);
		edtTxtHowManyKeys.setText("1");	// by default, add one inputkey.
		final AlertDialog alertDlgAddkeys = new AlertDialog.Builder(this)
				.setTitle(R.string.inputkey_config)
				.setView(v)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int nNumofKeys = 0;
						try	{
							nNumofKeys = Integer.parseInt(edtTxtHowManyKeys.getText().toString());
						} catch (NumberFormatException e)	{
							nNumofKeys = 0;
						}
						if (nNumofKeys > 0)	{
							// update under operation inputpad.
							LinkedList<InputKey> listInputKeys;
							if (!bIsLand)	{
								listInputKeys = mlistInputPads.get(nInputPadIdx).getListOfKeys();
							} else	{
								listInputKeys = mlistInputPads.get(nInputPadIdx).getListOfKeysLand();
							}
							for (int idx = 0; idx < nNumofKeys; idx ++)	{
								listInputKeys.addLast(new InputKey());
							}
							updateInputPadKeys(nInputPadIdx, listInputKeys, bIsLand);
							updateInputPad(nInputPadIdx, bIsLand, bIsLand);
						}
					}
					
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing if press cancel.							
					}
				}).create();
		
		// cannot call the following statement coz positive button returns null.
		// alertDlgAddkeys.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);	// positive button is enabled.

		edtTxtHowManyKeys.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				int nNumofKeys = 0;
				try	{
					nNumofKeys = Integer.parseInt(s.toString());
				} catch (NumberFormatException e)	{
					nNumofKeys = 0;
				}
				if (nNumofKeys < 1)	{
					alertDlgAddkeys.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				} else	{
					alertDlgAddkeys.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
		});
		alertDlgAddkeys.show();					
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {  
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getTag() instanceof InputKeyReference)	{
			menu.setHeaderTitle(R.string.what_do_you_want);
			menu.add(0, KEY_CONTEXT_MENU_CUT, 0, R.string.cut_key);  
			menu.add(0, KEY_CONTEXT_MENU_COPY, 0, R.string.copy_key);  
			if (minputKeyRefClipboard.isEmpty() == false)	{
				menu.add(0, KEY_CONTEXT_MENU_PASTE_BEFORE, 0, R.string.paste_before_key);  
				menu.add(0, KEY_CONTEXT_MENU_PASTE_ON, 0, R.string.paste_on_key);
				menu.add(0, KEY_CONTEXT_MENU_PASTE_AFTER, 0, R.string.paste_after_key);
			}
			menu.add(0, KEY_CONTEXT_MENU_CREATE_BEFORE, 0, R.string.create_before_key);  
			menu.add(0, KEY_CONTEXT_MENU_CREATE_AFTER, 0, R.string.create_after_key);  
			menu.add(0, KEY_CONTEXT_MENU_DELETE, 0, R.string.delete_key);  
			menu.add(0, KEY_CONTEXT_MENU_EDIT, 0, R.string.edit_key); 
			// should not use assign, i.e. minputKeyRefUnderOpt = v.getTag()
			// cause minputKeyRefUnderOpt and tag may change in different scenarios
			minputKeyRefUnderOpt.copy((InputKeyReference) v.getTag());
		}
	}  
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {  
		switch (item.getItemId()) {  
		case KEY_CONTEXT_MENU_CUT:  
			// should not use assign, i.e. minputKeyRefClipboard = minputKeyRefUnderOpt
			minputKeyRefClipboard.copy(minputKeyRefUnderOpt);
			mstrClipboardMode = "cut";
			break;
		case KEY_CONTEXT_MENU_COPY:  
			// should not use assign, i.e. minputKeyRefClipboard = minputKeyRefUnderOpt
			minputKeyRefClipboard.copy(minputKeyRefUnderOpt);
			mstrClipboardMode = "copy";
			break;
		case KEY_CONTEXT_MENU_PASTE_BEFORE:  
			if (!minputKeyRefClipboard.isEmpty())	{
				if (mstrClipboardMode.trim().equalsIgnoreCase("cut"))	{
					minputKeyRefUnderOpt.moveInputKeyToLeft(minputKeyRefClipboard);
					// update under operation inputpad.
					updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					// update clipboard inputpad if needed.
					if (minputKeyRefUnderOpt.mnInputPadIdx != minputKeyRefClipboard.mnInputPadIdx
							|| minputKeyRefUnderOpt.mbIsLand != minputKeyRefClipboard.mbIsLand)	{
						updateInputPad(minputKeyRefClipboard.mnInputPadIdx, minputKeyRefClipboard.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					}
					minputKeyRefClipboard.clear();
				} else if (mstrClipboardMode.trim().equalsIgnoreCase("copy"))	{
					minputKeyRefUnderOpt.insertInputKeyToLeft(minputKeyRefClipboard);
					// update under operation inputpad.
					updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					// do not need to clear clipboard, but may need to recalculate minputKeyRefClipboard
					if (minputKeyRefUnderOpt.mnInputPadIdx == minputKeyRefClipboard.mnInputPadIdx
							&& minputKeyRefUnderOpt.mbIsLand == minputKeyRefClipboard.mbIsLand
							&& minputKeyRefUnderOpt.mnInputKeyIdx <= minputKeyRefClipboard.mnInputKeyIdx)	{
						minputKeyRefClipboard.mnInputKeyIdx ++;
					}
				}
			}
			break;
		case KEY_CONTEXT_MENU_PASTE_ON:  
			if (!minputKeyRefClipboard.isEmpty() && !minputKeyRefClipboard.isSame(minputKeyRefUnderOpt))	{
				if (mstrClipboardMode.trim().equalsIgnoreCase("cut"))	{
					minputKeyRefUnderOpt.moveInputKeyToReplace(minputKeyRefClipboard);
					// update under operation inputpad.
					updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					// update clipboard inputpad if needed.
					if (minputKeyRefUnderOpt.mnInputPadIdx != minputKeyRefClipboard.mnInputPadIdx
							|| minputKeyRefUnderOpt.mbIsLand != minputKeyRefClipboard.mbIsLand)	{
						updateInputPad(minputKeyRefClipboard.mnInputPadIdx, minputKeyRefClipboard.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					}
					minputKeyRefClipboard.clear();
				} else if (mstrClipboardMode.trim().equalsIgnoreCase("copy"))	{
					minputKeyRefUnderOpt.replaceInputKey(minputKeyRefClipboard);
					// update under operation inputpad.
					updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					// do not need to clear clipboard
				}
			}
			break;
		case KEY_CONTEXT_MENU_PASTE_AFTER:  
			if (!minputKeyRefClipboard.isEmpty())	{
				if (mstrClipboardMode.trim().equalsIgnoreCase("cut"))	{
					minputKeyRefUnderOpt.moveInputKeyToRight(minputKeyRefClipboard);
					// update under operation inputpad.
					updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					// update clipboard inputpad if needed.
					if (minputKeyRefUnderOpt.mnInputPadIdx != minputKeyRefClipboard.mnInputPadIdx
							|| minputKeyRefUnderOpt.mbIsLand != minputKeyRefClipboard.mbIsLand)	{
						updateInputPad(minputKeyRefClipboard.mnInputPadIdx, minputKeyRefClipboard.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					}
					minputKeyRefClipboard.clear();
				} else if (mstrClipboardMode.trim().equalsIgnoreCase("copy"))	{
					minputKeyRefUnderOpt.insertInputKeyToRight(minputKeyRefClipboard);
					// update under operation inputpad.
					updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
					// do not need to clear clipboard, but may need to recalculate minputKeyRefClipboard
					if (minputKeyRefUnderOpt.mnInputPadIdx == minputKeyRefClipboard.mnInputPadIdx
							&& minputKeyRefUnderOpt.mbIsLand == minputKeyRefClipboard.mbIsLand
							&& minputKeyRefUnderOpt.mnInputKeyIdx < minputKeyRefClipboard.mnInputKeyIdx)	{
						minputKeyRefClipboard.mnInputKeyIdx ++;
					}
				}
			}
			break;
		case KEY_CONTEXT_MENU_CREATE_BEFORE:  
			minputKeyRefUnderOpt.createInputKeyToLeft();
			// update under operation inputpad.
			updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
			// do not need to clear clipboard, but may need to recalculate minputKeyRefClipboard
			if (minputKeyRefUnderOpt.mnInputPadIdx == minputKeyRefClipboard.mnInputPadIdx
					&& minputKeyRefUnderOpt.mbIsLand == minputKeyRefClipboard.mbIsLand
					&& minputKeyRefUnderOpt.mnInputKeyIdx <= minputKeyRefClipboard.mnInputKeyIdx)	{
				minputKeyRefClipboard.mnInputKeyIdx ++;
			}
			break;
		case KEY_CONTEXT_MENU_CREATE_AFTER:  
			minputKeyRefUnderOpt.createInputKeyToRight();
			// update under operation inputpad.
			updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
			// do not need to clear clipboard, but may need to recalculate minputKeyRefClipboard
			if (minputKeyRefUnderOpt.mnInputPadIdx == minputKeyRefClipboard.mnInputPadIdx
					&& minputKeyRefUnderOpt.mbIsLand == minputKeyRefClipboard.mbIsLand
					&& minputKeyRefUnderOpt.mnInputKeyIdx < minputKeyRefClipboard.mnInputKeyIdx)	{
				minputKeyRefClipboard.mnInputKeyIdx ++;
			}
			break;
		case KEY_CONTEXT_MENU_DELETE:  
			minputKeyRefUnderOpt.deleteInputKey();
			// update under operation inputpad.
			updateInputPad(minputKeyRefUnderOpt.mnInputPadIdx, minputKeyRefUnderOpt.mbIsLand, minputKeyRefUnderOpt.mbIsLand);
			// do not necessarily need to clear clipboard, but may need to recalculate minputKeyRefClipboard
			if (minputKeyRefUnderOpt.mnInputPadIdx == minputKeyRefClipboard.mnInputPadIdx
					&& minputKeyRefUnderOpt.mbIsLand == minputKeyRefClipboard.mbIsLand)	{
				if ( minputKeyRefUnderOpt.mnInputKeyIdx < minputKeyRefClipboard.mnInputKeyIdx)	{
					minputKeyRefClipboard.mnInputKeyIdx --;
				} else if (minputKeyRefUnderOpt.mnInputKeyIdx == minputKeyRefClipboard.mnInputKeyIdx)	{
					// delete cut or copied key
					minputKeyRefClipboard.clear();
				}
			}
			break;
		case KEY_CONTEXT_MENU_EDIT:  
			editInputKey();
		default:  
		}  
		return super.onContextItemSelected(item);  
	}
	
	@Override
	/*
	 * Create menus
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM0, 0, getString(R.string.menu_add));
		menu.add(0, ITEM1, 0, getString(R.string.menu_addleft));
		menu.add(0, ITEM2, 0, getString(R.string.menu_addright));
		menu.add(0, ITEM3, 0, getString(R.string.menu_delete));
		menu.add(0, ITEM4, 0, getString(R.string.menu_save));
		menu.add(0, ITEM5, 0, getString(R.string.menu_help));
		return true;
	}
	
	@Override
	/*
	 * Dynamically create menus
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mlistInputPads.size() == 0)	{
			menu.add(0, ITEM0, 0, getString(R.string.menu_add));
		} else	{
			menu.add(0, ITEM1, 0, getString(R.string.menu_addleft));
			menu.add(0, ITEM2, 0, getString(R.string.menu_addright));
			menu.add(0, ITEM3, 0, getString(R.string.menu_delete));
			menu.add(0, ITEM4, 0, getString(R.string.menu_save));
		}
		menu.add(0, ITEM5, 0, getString(R.string.menu_help));

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM0:
		{
			// add the first inputPad
			TableInputPad inputPad = new TableInputPad();
			mlistInputPads.add(inputPad);
			mnSelectedInputPadIdx = 0;
			populateInputPads(mlistInputPads);
			updateInputPadCfgWindow();
			break;
		}
		case ITEM1:
		{
			// add an inputPad to the left of current inputPad
			TableInputPad inputPad = new TableInputPad();
			mlistInputPads.add(mnSelectedInputPadIdx, inputPad);
			if (minputKeyRefClipboard.isEmpty() == false)	{
				if (minputKeyRefClipboard.mnInputPadIdx >= mnSelectedInputPadIdx)	{
					minputKeyRefClipboard.mnInputPadIdx ++;
				}
			}
			populateInputPads(mlistInputPads);
			updateInputPadCfgWindow();
			break;
		}
		case ITEM2: 
		{
			// add an inputPad to the right of current inputPad
			TableInputPad inputPad = new TableInputPad();
			mlistInputPads.add(mnSelectedInputPadIdx + 1, inputPad);
			if (minputKeyRefClipboard.isEmpty() == false)	{
				if (minputKeyRefClipboard.mnInputPadIdx > mnSelectedInputPadIdx)	{
					minputKeyRefClipboard.mnInputPadIdx ++;
				}
			}
			mnSelectedInputPadIdx ++;
			populateInputPads(mlistInputPads);
			updateInputPadCfgWindow();
			break;
		}
		case ITEM3: 
		{
			// delete an inputpad
			mlistInputPads.remove(mnSelectedInputPadIdx);
			if (minputKeyRefClipboard.isEmpty() == false)	{
				if (minputKeyRefClipboard.mnInputPadIdx == mnSelectedInputPadIdx)	{
					minputKeyRefClipboard.clear();
				} else if (minputKeyRefClipboard.mnInputPadIdx > mnSelectedInputPadIdx)	{
					minputKeyRefClipboard.mnInputPadIdx --;
				}
			}
			if (mnSelectedInputPadIdx == mlistInputPads.size())	{
				// rightmost before becomes rightmost and single becomes invalid position
				mnSelectedInputPadIdx = mlistInputPads.size() - 1;
			}	// else selectedinputpadidx does not change.
			populateInputPads(mlistInputPads);
			updateInputPadCfgWindow();
			break;
		}
		case ITEM4:
		{
			String strError = validateInputPadCfg();
			if (strError == null)	{
				strError = saveInputPadsCfg();
			}
			if (strError != null)	{
				AlertDialog alertDialog;
				alertDialog = new AlertDialog.Builder(this)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
								}
								
							}).setTitle(getString(R.string.error))
							.setMessage(strError)
							.create();
				alertDialog.show();					
			} else	{
				// saved successfully so that we update mstrOriginallyLoadedInputPadsCfg
				mstrOriginallyLoadedInputPadsCfg = InputPadMgrEx.writeInputPadsToXML(mlistInputPads);
			}
			break;
		}
		case ITEM5: 
		   	Intent intentHelp = new Intent(this,ActivityShowHelp.class);  
		   	Bundle bundle = new Bundle();
		   	bundle.putString("HELP_CONTENT", "config_inputpads");
		   	//Add this bundle to the intent
		   	intentHelp.putExtras(bundle);
		   	startActivity(intentHelp);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public String saveInputPadsCfg()	{
		boolean bSaveSuccessfully = true;
		String strError = null;
		String strXML = InputPadMgrEx.writeInputPadsToXML(mlistInputPads);
		try {
			File folder = new File(MFPFileManagerActivity.getConfigFolderFullPath());
			if (!folder.exists())	{
				if (!folder.mkdirs())	{
					bSaveSuccessfully = false;
					strError = getString(R.string.error_cannot_save_to_file);
				}
			}
			if (bSaveSuccessfully)	{
				BufferedWriter out = new BufferedWriter(new FileWriter(
													MFPFileManagerActivity.getConfigFolderFullPath()
													+ MFPFileManagerActivity.STRING_PATH_DIV
													+ mstrInputPadConfig));
				out.write(strXML);
				out.close();
				mbCfgFileChanged = true;
			}
		} catch (IOException e)	{
			bSaveSuccessfully = false;
			strError = getString(R.string.error_cannot_save_to_file);
		}
		return strError;
	}
	
	public boolean restoreInstanceState(Bundle bundle)	{
		if (bundle == null)	{
			return false;
		}
		Set<String> setKeys = bundle.keySet();
		if (setKeys.contains("InputKeyRefClipboard.InputPadIdx"))	{
			minputKeyRefClipboard.mnInputPadIdx = bundle.getInt("InputKeyRefClipboard.InputPadIdx");
		}
		if (setKeys.contains("InputKeyRefClipboard.IsLand"))	{
			minputKeyRefClipboard.mbIsLand = bundle.getBoolean("InputKeyRefClipboard.IsLand");
		}
		if (setKeys.contains("InputKeyRefClipboard.InputKeyIdx"))	{
			minputKeyRefClipboard.mnInputKeyIdx = bundle.getInt("InputKeyRefClipboard.InputKeyIdx");
		}
		if (setKeys.contains("ClipboardMode"))	{
			mstrClipboardMode = bundle.getString("ClipboardMode");
		}
		if (setKeys.contains("SelectedInputPadIdx"))	{
			mnSelectedInputPadIdx = bundle.getInt("SelectedInputPadIdx");
		}
		if (setKeys.contains("OriginallyLoadedInputPadsCfg"))	{
			mstrOriginallyLoadedInputPadsCfg = bundle.getString("OriginallyLoadedInputPadsCfg");
		}
		if (setKeys.contains("DialogState"))	{
			mnDialogState = bundle.getInt("DialogState");
		}
		if (setKeys.contains("CannotSaveError"))	{
			mstrCannotSaveError = bundle.getString("CannotSaveError");
		}
		if (setKeys.contains("CfgFileChanged"))	{
			mbCfgFileChanged = bundle.getBoolean("CfgFileChanged");
		}
		if (setKeys.contains("InputPads"))	{
			String strXML = bundle.getString("InputPads");
			InputStream is = new ByteArrayInputStream(strXML.getBytes());
			mlistInputPads = InputPadMgrEx.readInputPadsFromXML(is);
			return true;
		}
		return false;
	}
	
	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		// super.onRestoreInstanceState(bundle); do not restore the edit txt views' states.
		
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		bundle.putInt("InputKeyRefClipboard.InputPadIdx", minputKeyRefClipboard.mnInputPadIdx);
		bundle.putBoolean("InputKeyRefClipboard.IsLand", minputKeyRefClipboard.mbIsLand);
		bundle.putInt("InputKeyRefClipboard.InputKeyIdx", minputKeyRefClipboard.mnInputKeyIdx);
		bundle.putString("ClipboardMode", mstrClipboardMode);
		bundle.putInt("SelectedInputPadIdx", mnSelectedInputPadIdx);
		bundle.putString("OriginallyLoadedInputPadsCfg", mstrOriginallyLoadedInputPadsCfg);
		bundle.putInt("DialogState", mnDialogState);
		bundle.putString("CannotSaveError", mstrCannotSaveError);
		bundle.putBoolean("CfgFileChanged", mbCfgFileChanged);
		String strXML = InputPadMgrEx.writeInputPadsToXML(mlistInputPads);
		bundle.putString("InputPads", strXML);
		// super.onSaveInstanceState(bundle);
	}
	
	public void showSaveOrNotDlg()	{
		// config has been changed, save it or not?	
		AlertDialog alertDialog = new AlertDialog.Builder(this)
			.setTitle(getString(R.string.warning))
			.setMessage(getString(R.string.inputpad_cfg_changed))
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					finish();
					return;	// do nothing and exit.
				}
			}).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					String strInvalid = validateInputPadCfg();
					if (strInvalid != null)	{
						showCannotSaveDlg(strInvalid, false);	// do not exit
					} else	{
						String strError = saveInputPadsCfg();
						if (strError != null)	{
							showCannotSaveDlg(strError, true);	// exit
						} else	{
							finish();
						}
					}
				}
			}).setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					mnDialogState = 0;
				}
				
			}).create();
		alertDialog.show();
		mnDialogState = 1;
	}
	
	public void showCannotSaveDlg(String strError, boolean bExit)	{
		AlertDialog alertDialog1;
		alertDialog1 = new AlertDialog.Builder(ActivityCfgKeyPad.this)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							if (mnDialogState == 2)	{
								ActivityCfgKeyPad.this.finish();
							} else	{
								mnDialogState = 0;	// dismiss the dialog only
							}
						}
						
					}).setTitle(getString(R.string.error))
					.setMessage(strError)
					.setCancelable(false)
					.create();
		alertDialog1.show();
		mstrCannotSaveError = strError;
		if (bExit)	{
			mnDialogState = 2;	// exit mode
		} else	{
			mnDialogState = 3;	// not exit mode;
		}
	}
	
    // After BACK key pressed, prompt to save file.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)	{
        	String strInputPadsCfg = InputPadMgrEx.writeInputPadsToXML(mlistInputPads);
        	if (mstrOriginallyLoadedInputPadsCfg == null
        			|| strInputPadsCfg.equals(mstrOriginallyLoadedInputPadsCfg) == false)	{
        		showSaveOrNotDlg();
        	} else	{
        		finish();
        	}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void finish()	{
		Intent intentCfgFileChanged = new Intent();
		if (intentCfgFileChanged != null) {
			Bundle b = new Bundle();
			if (b != null) {
				b.putBoolean("CfgFileChanged", mbCfgFileChanged);
				intentCfgFileChanged.putExtra("android.intent.extra.FunCalc", b);
			}
		}
		setResult(Activity.RESULT_OK, intentCfgFileChanged);
    	super.finish();
    }
}
