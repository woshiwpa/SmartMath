package com.cyzapps.MFPFileManager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Scanner;

import com.cyzapps.SmartMath.AMInputMethod;
import com.cyzapps.SmartMath.ActivityCfgKeyPad;
import com.cyzapps.SmartMath.ActivitySettings;
import com.cyzapps.SmartMath.ActivityShowHelp;
import com.cyzapps.SmartMath.ActivitySmartCalc;
import com.cyzapps.SmartMath.InputPadMgrEx;
import com.cyzapps.SmartMath.R;
import com.cyzapps.SmartMath.InputPadMgrEx.InputKey;
import com.cyzapps.SmartMath.InputPadMgrEx.TableInputPad;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.MFPAdapter.FunctionEntry;
import com.cyzapps.adapter.MFPAdapter.InternalFuncInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class ScriptEditorActivity extends Activity implements AMInputMethod.InputMethodCaller {
	private static final int DIALOG_FILE_CANNOT_OPEN = 1;
	private static final int DIALOG_FILE_NOT_FOUND = 2;
	private static final int DIALOG_IO_ERROR = 3;
	private static final int DIALOG_FILE_CHANGED = 4;
	private static final int DIALOG_GOTO_LINE = 5;

	private static final int ITEM0 = Menu.FIRST;
	private static final int ITEM1 = Menu.FIRST + 1;
	private static final int ITEM2 = Menu.FIRST + 2;
	private static final int ITEM3 = Menu.FIRST + 3;
	private static final int ITEM4 = Menu.FIRST + 4;
	private static final int ITEM5 = Menu.FIRST + 5;
	private static final int ITEM6 = Menu.FIRST + 6;
	
	public static final int SCRIPT_FILE_MANAGER_ACTIVITY = 1;

	public static final int START_FILE_EDITOR_BY_ITSELF = 0;
	public static final int START_FILE_EDITOR_TO_EDIT = 1;
	private int mnMode = START_FILE_EDITOR_BY_ITSELF;		//0 means main activity; 1 means opened by script editor.
	private boolean mbIsFileDirty = false;
	private String mstrFilePath = "";
	private String mstrSelectedFilePath = "";
	
	private String mstrNextTask = "";
	
	public EditText medtScriptEdtBox = null;
    public static final int ENABLE_SHOW_INPUTPAD = -1;
    public static final int ENABLE_HIDE_INPUTPAD = 0;
    public static final int ENABLE_HIDE_SOFTKEY = 1;
    public static final int ENABLE_SHOW_SOFTKEY = 2;

	public static final String IMMUTABLE_INPUTPAD_CONFIG = "immutable_inputpad_se.cfg";

	private int mnSoftKeyState = ENABLE_SHOW_INPUTPAD;	// 0 means use but hide inputpad, -1 means use and show inputpad
												// 1 means use but hide softkeyboard, 2 means use and show soft keyboard.

	protected AMInputMethod minputMethod = null;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.script_editor_title));
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
        setContentView(R.layout.edit_script);
        
		if (MFPAdapter.isFuncSpaceEmpty())	{
			MFPAdapter.reloadAll(this, 1, null);
		}
		
        //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();

        if (bundle != null)	{
        	//Next extract the values using the key as
        	mstrFilePath = bundle.getString("FILE_PATH");
        	mnMode = bundle.getInt("MODE");
        }
        medtScriptEdtBox = (EditText)findViewById(R.id.edittxtScript);
        medtScriptEdtBox.addTextChangedListener(new TextWatcher(){

        	@Override
			public void afterTextChanged(Editable s) {
        		mbIsFileDirty = true;
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				return;
				
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				return;
			}
        });
        
        medtScriptEdtBox.setOnTouchListener(new OnTouchListener()	{
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				/*
				 * Use soft keyboard or inputpad depends on mnSoftKeyState
				 */
			   	if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
			   		// hide system input only. show inputpad will be later on handled by onClickListener.
					v.onTouchEvent(event);	// different from scripteditor, cannot reset inputtype here.
			        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			        if (imm != null)	{
			        	imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			        }
				    return true;	// intercept the onTouch event.
			   	} else {	// if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY || mnSoftKeyState == ENABLE_HIDE_SOFTKEY)	{
			   		return false;
			   	}
			}
        });
        
        medtScriptEdtBox.setOnClickListener(new OnClickListener()	{

			@Override
			public void onClick(View v) {
				/*
				 * Use soft keyboard or inputpad depends on mnSoftKeyState
				 */
			   	if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
			   		// do not use soft keyboard, use input pad
					setSoftKeyState((EditText)v, ENABLE_SHOW_INPUTPAD);
			   	} else {	// if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY || mnSoftKeyState == ENABLE_HIDE_SOFTKEY)	{
			   		// show the soft keyboard and disable input pad
			   		setSoftKeyState((EditText)v, ENABLE_SHOW_SOFTKEY);
			   	}
			}
			
		});
        
        medtScriptEdtBox.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				/*
				 * Use soft keyboard or inputpad depends on mnSoftKeyState
				 */
			   	if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
			   		// do not use soft keyboard, use input pad
					setSoftKeyState((EditText)v, ENABLE_SHOW_INPUTPAD);
			   	} else {	// if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY || mnSoftKeyState == ENABLE_HIDE_SOFTKEY)	{
			   		// show the soft keyboard and disable input pad
			   		setSoftKeyState((EditText)v, ENABLE_SHOW_SOFTKEY);
			   	}
			   	return false;	// enable select text so return false;
			}
        	
        });

        if (mstrFilePath.equals(""))	{
        	openNewFile();
        } else	{
        	openFile(mstrFilePath);
        }

        //--- Show input method
        LinearLayout layoutIMEHolder = (LinearLayout) findViewById(R.id.layoutSEInputMethodHolder);
		minputMethod = new AMInputMethod(this);
		layoutIMEHolder.addView(minputMethod);

		minputMethod.initialize(this, medtScriptEdtBox);
		setSoftKeyState(medtScriptEdtBox, ENABLE_SHOW_INPUTPAD);	// use inputpad and hide soft keyboard

    }

    private String[] getFilePathNameExt(String strFileFullPath)	{
    	String[] strarrayReturn = new String[3];
    	strarrayReturn[0] = strarrayReturn[1] = strarrayReturn[2] = "";
    	if (strFileFullPath == null
    			|| strFileFullPath.length()
    				<= MFPFileManagerActivity.STRING_SCRIPT_EXTENSION.length() )	{
    		return strarrayReturn;
    	}
        String[] strarrayPath = strFileFullPath.split(MFPFileManagerActivity.STRING_PATH_DIV);
        String strNameWithNoExt = strarrayPath[strarrayPath.length - 1];
        strNameWithNoExt = strNameWithNoExt
        				.substring(0, strNameWithNoExt.length()
        						- MFPFileManagerActivity.STRING_SCRIPT_EXTENSION.length());
        String strPathWithNoName = strFileFullPath
        				.substring(0, strFileFullPath.length() - 1 - strNameWithNoExt.length()
        						- MFPFileManagerActivity.STRING_SCRIPT_EXTENSION.length());
        strarrayReturn[0] = strPathWithNoName;
        strarrayReturn[1] = strNameWithNoExt;
        strarrayReturn[2] = MFPFileManagerActivity.STRING_SCRIPT_EXTENSION;
        return strarrayReturn;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
		LayoutInflater inflater = LayoutInflater.from(this);
    	switch(id)	{
    	case DIALOG_FILE_CANNOT_OPEN:
	    	return new AlertDialog.Builder(ScriptEditorActivity.this)
	    				.setTitle(getString(R.string.error))
	    				.setMessage(getString(R.string.file_editor_file_open_fail))
	    				.create();
    	case DIALOG_FILE_NOT_FOUND:
	    	return new AlertDialog.Builder(ScriptEditorActivity.this)
	    				.setTitle(getString(R.string.error))
	    				.setMessage(getString(R.string.file_not_found))
	    				.create();
    	case DIALOG_IO_ERROR:
	    	return new AlertDialog.Builder(ScriptEditorActivity.this)
	    				.setTitle(getString(R.string.error))
	    				.setMessage(getString(R.string.file_editor_file_io_error))
	    				.create();
    	case DIALOG_FILE_CHANGED:
    		return new AlertDialog.Builder(ScriptEditorActivity.this)
    					.setTitle(getString(R.string.warning))
    					.setMessage(getString(R.string.file_editor_file_changed))
    					.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
    						
    						@Override
    						public void onClick(DialogInterface arg0, int arg1) {
								if (mstrNextTask.equals("openNewFile"))	{
									openNewFile();
									mstrNextTask = "";
								} else if (mstrNextTask.equals("finish"))	{
									finish();
									// no need to set mstrNextTask = "";
								}
    							return;	// do nothing but open a new file if press no.
    						}
    					}).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
    						
    						@Override
    						public void onClick(DialogInterface arg0, int arg1) {
    							if (mstrFilePath.equals(""))	{
    								actionClickMenuSaveFileAs();
    							} else	{
    								actionClickMenuSaveFile();
    								if (mstrNextTask.equals("openNewFile"))	{
    									openNewFile();
    									mstrNextTask = "";
    								} else if (mstrNextTask.equals("finish"))	{
    									finish();
    									// no need to set mstrNextTask = "";
    								}
    							}
    							return;	// save file if press yes
    						}
    					}).create();
    	case DIALOG_GOTO_LINE:
    		String strText = medtScriptEdtBox.getText().toString();
    		int nNumofLines = 1;
    		for (int index = 0; index < strText.length(); index ++)	{
    			if (strText.charAt(index) == '\n')	{
    				nNumofLines ++;
    			}
    		}
			View viewGotoLine = inflater.inflate(R.layout.goto_line, null);
			TextView txtView = (TextView)viewGotoLine.findViewById(R.id.txt_enter_goto_line_prompt);
			txtView.setText(getText(R.string.file_editor_goto_line)
							+ " (1" + " to " + String.valueOf(nNumofLines) + ") ");
			final EditText txtGotoLineInput = (EditText)viewGotoLine.findViewById(R.id.edt_txt_goto_line_input);
			txtGotoLineInput.setText("");
			txtGotoLineInput.setTextColor(Color.BLACK);
			AlertDialog.Builder blder = new AlertDialog.Builder(ScriptEditorActivity.this);
			// blder.setTitle(getString(R.string.file_editor_menu_goto_line)); //title shows the same text as prompt, no need.
    		blder.setView(viewGotoLine);
    		blder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener()	{

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (txtGotoLineInput.getText().toString().trim().equals(""))	{
						removeDialog(DIALOG_GOTO_LINE);
						return;	// do nothing.
					}
					if (txtGotoLineInput.getCurrentTextColor() == Color.BLACK)	{
						// if the line number input is valid
						int nLineNum = 1;
						try	{
							nLineNum = Integer.parseInt(txtGotoLineInput.getText().toString());
						} catch(NumberFormatException e)	{
							removeDialog(DIALOG_GOTO_LINE);
							return; // wrong line number, go to first line.
						}
			    		String strFileText = medtScriptEdtBox.getText().toString();
			    		int nLineCount = 1;
			    		for (int index = 0; index <= strFileText.length(); index++)	{
			    			if (nLineCount == nLineNum)	{
			    				medtScriptEdtBox.setSelection(index);
			    				break;
			    			}
			    			if (index < strFileText.length() && strFileText.charAt(index) == '\n')	{
			    				nLineCount ++;
			    			}
			    		}
					}
					removeDialog(DIALOG_GOTO_LINE);
				}
    			
    		});
    		final AlertDialog alertDlgGotoLine = blder.create();
			txtGotoLineInput.addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable s) {
					if (s.toString().trim().equals(""))	{
						alertDlgGotoLine.getButton(AlertDialog.BUTTON_POSITIVE).setText(getString(R.string.close));
						alertDlgGotoLine.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
						return;
					} else	{
						alertDlgGotoLine.getButton(AlertDialog.BUTTON_POSITIVE).setText(getString(R.string.ok));
					}
		    		int nNumofLines = medtScriptEdtBox.getLineCount();
					boolean bIsValidLineNum = true;
					try
					{
						int nLineNum = Integer.parseInt(s.toString());
						if (nLineNum > 0 && nLineNum <= nNumofLines)	{
							bIsValidLineNum = true;
						} else	{
							bIsValidLineNum = false;
						}
					}
					catch(NumberFormatException nfe)
					{
						bIsValidLineNum = false;
					}
					if (bIsValidLineNum)	{
						txtGotoLineInput.setTextColor(Color.BLACK);
						alertDlgGotoLine.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
					} else	{
						txtGotoLineInput.setTextColor(Color.RED);
						alertDlgGotoLine.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					
				}
				
			});
    		return alertDlgGotoLine;
    	default:
    		return null;
    	}
    }

    public boolean openNewFile()	{
    	medtScriptEdtBox.setText("");
		mbIsFileDirty = false;
		mstrFilePath = "";
		TextView tvStatus = (TextView)findViewById(R.id.txtviewStatus);
		tvStatus.setText(getString(R.string.file_editor_new_file));
		return true;
    }
    
    public boolean openFile(String strFilePath)	{
    	if (false)	{
	    	/* ---- too slow for big files.*/
	 		FileReader fr;
			try {
		    	File file = new File(strFilePath);
				fr = new FileReader (file);
			} catch(FileNotFoundException e)	{
				// cannot open the file.
				showDialog(DIALOG_FILE_CANNOT_OPEN);
				finish();
				return false;
			}
			BufferedReader br = new BufferedReader(fr);
			Scanner scanner = new Scanner(br);
			String strAllText = "";
			while (scanner.hasNextLine()){
				String strLine = scanner.nextLine();
				strAllText += strLine +"\n";
				//ev.append(strLine + "\n"); // operate on ev directly may significantly slow down system.
			}
			medtScriptEdtBox.setText(strAllText);
    	} else	{
			try {
		    	File file = new File(strFilePath);
				FileInputStream fin = new FileInputStream(file);
				byte[] buffer = new byte[(int) file.length()];
			    new DataInputStream(fin).readFully(buffer);
			    fin.close();
			    String s = new String(buffer, "UTF-8");
			    medtScriptEdtBox.setText(s);
			} catch(FileNotFoundException e)	{
				// cannot open the file.
				showDialog(DIALOG_FILE_CANNOT_OPEN);
				finish();
				return false;
			} catch(IOException e)	{
				showDialog(DIALOG_FILE_CANNOT_OPEN);
				finish();
				return false;
			}
    	}
		
		mstrFilePath = strFilePath;
		mbIsFileDirty = false;
        TextView tvStatus = (TextView)findViewById(R.id.txtviewStatus);
        String[] strarrayFileAddr = getFilePathNameExt(mstrFilePath);
        tvStatus.setText(getString(R.string.file_editor_file) + " " + strarrayFileAddr[1]
        		+ " " + getString(R.string.file_editor_inside) + " " + strarrayFileAddr[0] );
		return true;
    }
    
    public boolean saveFile(String strFilePath)	{
		//minputMethod.flushBufferedString();
		
    	try {
	    	File file = new File(strFilePath);
    		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
    		osw.write(medtScriptEdtBox.getText().toString());
     		osw.flush();
    		osw.close();
    	} catch (FileNotFoundException e) {
			showDialog(DIALOG_FILE_NOT_FOUND);
	    	return false;
		} catch (IOException e) {
			showDialog(DIALOG_IO_ERROR);
	    	return false;
		}

		// now reload the lib files.
		MFPAdapter.unloadFileOrFolder(strFilePath);
		MFPAdapter.loadFile(strFilePath, null);
		
		Toast.makeText(ScriptEditorActivity.this, R.string.script_file_saved, Toast.LENGTH_LONG).show();
		mstrFilePath = strFilePath;
		mbIsFileDirty = false;
        TextView tvStatus = (TextView)findViewById(R.id.txtviewStatus);
        String[] strarrayFileAddr = getFilePathNameExt(mstrFilePath);
        tvStatus.setText(getString(R.string.file_editor_file) + " " + strarrayFileAddr[1]
        		+ " " + getString(R.string.file_editor_inside) + " " + strarrayFileAddr[0] );
		return true;
    }

	@Override
	/*
	 * Create three menus: History, Settings and Help
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM0, 0, getString(R.string.file_editor_menu_new));
		menu.add(0, ITEM1, 0, getString(R.string.file_editor_menu_open));
		menu.add(0, ITEM2, 0, getString(R.string.file_editor_menu_save));			
		menu.add(0, ITEM3, 0, getString(R.string.file_editor_menu_save_as));
		menu.add(0, ITEM4, 0, getString(R.string.file_editor_menu_goto_line));
		menu.add(0, ITEM5, 0, getString(R.string.hide_system_soft_key));			
		menu.add(0, ITEM6, 0, getString(R.string.menu_help));
		return true;
	}

	//Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
        if (mnMode == START_FILE_EDITOR_BY_ITSELF)	{
        	menu.add(0, ITEM0, 0, getString(R.string.file_editor_menu_new));
        }
        if (mnMode == START_FILE_EDITOR_BY_ITSELF)	{
    		menu.add(0, ITEM1, 0, getString(R.string.file_editor_menu_open));
        }
        if (mbIsFileDirty && mstrFilePath.equals("") == false)	{
        	menu.add(0, ITEM2, 0, getString(R.string.file_editor_menu_save));
    	}
        if (mnMode == START_FILE_EDITOR_BY_ITSELF)	{
    		menu.add(0, ITEM3, 0, getString(R.string.file_editor_menu_save_as));
        }
    	menu.add(0, ITEM4, 0, getString(R.string.file_editor_menu_goto_line));
		if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
			menu.add(0, ITEM5, 0, getString(R.string.hide_system_soft_key));			
		} else	{
			menu.add(0, ITEM5, 0, getString(R.string.pop_up_system_soft_key));			
		}
		menu.add(0, ITEM6, 0, getString(R.string.menu_help));

        return super.onPrepareOptionsMenu(menu);
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM0: 
			actionClickMenuNewFile();
			break;
		case ITEM1: 
			actionClickMenuOpenFile();
			break;
		case ITEM2: 
			actionClickMenuSaveFile();
			break;
		case ITEM3: 
			actionClickMenuSaveFileAs();
			break;
		case ITEM4: 
			actionClickMenuGotoLine();
			break;
		case ITEM5:
			actionClickMenuSystemSoftKey();
			break;
		case ITEM6:
			actionClickMenuHelp();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Create a new file in the editor
	 */
	private void actionClickMenuNewFile()	{
		if (mbIsFileDirty)	{
			// showDialog always create a moduless (unblocking) dialog.
			mstrNextTask = "openNewFile";
			showDialog(DIALOG_FILE_CHANGED);
		} else	{
	        openNewFile();
		}
	}

	/*
	 * Open the file in the editor
	 */
	private void actionClickMenuOpenFile()	{
		mstrSelectedFilePath = "";
		startScriptFileManager(MFPFileManagerActivity.START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR);
	}

	/*
	 * Save the content of the editor
	 */
	private void actionClickMenuSaveFile()	{
		saveFile(mstrFilePath);
	}

	/*
	 * Save the file as
	 */
	private void actionClickMenuSaveFileAs()	{
		mstrSelectedFilePath = "";
		startScriptFileManager(MFPFileManagerActivity.START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR);
	}

	/*
	 * Go to a new line
	 */
	private void actionClickMenuGotoLine()	{
		showDialog(DIALOG_GOTO_LINE);
	}
	
	/*
	 * Show or hide system soft key board.
	 */
	private void actionClickMenuSystemSoftKey()	{
	   	if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY)	{
            // code to hide the soft keyboard
            setSoftKeyState(medtScriptEdtBox, ENABLE_HIDE_INPUTPAD);
	   	} else	{
	   		// code to enable the soft keyboard
	   		setSoftKeyState(medtScriptEdtBox, ENABLE_SHOW_SOFTKEY);
	   	}
	}
	/*
	 * Show help
	 */
	private void actionClickMenuHelp(){
	   	Intent intentHelp = new Intent(this, ActivityShowHelp.class);
	   	Bundle bundle = new Bundle();
	   	bundle.putString("HELP_CONTENT", "language_quick_start");
	   	//Add this bundle to the intent
	   	intentHelp.putExtras(bundle);
	   	startActivity(intentHelp);
	}
	
	/*
	 * Show script editor
	 */
	private void startScriptFileManager(int nMode) {
	   	Intent intentScriptFileManager = new Intent(this, MFPFileManagerActivity.class);
	   	Bundle bundle = new Bundle();
	   	if (mstrFilePath != null && mstrFilePath.trim().equals("") == false)	{
		   	String[] strarrayFileAddr = getFilePathNameExt(mstrFilePath);
		   	bundle.putString("FILE_FOLDER", strarrayFileAddr[0]);
		   	bundle.putString("FILE_NAME_WITH_Ext", strarrayFileAddr[1] + strarrayFileAddr[2]);
		   	bundle.putString("FILE_NAME_WITHOUT_Ext", strarrayFileAddr[1]);
		   	bundle.putString("FILE_FULL_PATH", mstrFilePath);
		   	bundle.putInt("MODE", nMode);
		   	bundle.putString("FILE_SHOWN_FILTER", strarrayFileAddr[2]);
	   	} else	{
	        String strScriptFileFolder = AndroidStorageOptions.getSelectedStoragePath()
	        			+ MFPFileManagerActivity.STRING_PATH_DIV
	        			+ MFPFileManagerActivity.STRING_APP_FOLDER
	        			+ MFPFileManagerActivity.STRING_PATH_DIV
	        			+ MFPFileManagerActivity.STRING_SCRIPT_FOLDER;
		   	bundle.putString("FILE_FOLDER", strScriptFileFolder);
		   	bundle.putInt("MODE", nMode);
		   	bundle.putString("FILE_SHOWN_FILTER", MFPFileManagerActivity.STRING_SCRIPT_EXTENSION);
	   	}
	   	//Add this bundle to the intent
	   	intentScriptFileManager.putExtras(bundle);
	   	startActivityForResult(intentScriptFileManager, SCRIPT_FILE_MANAGER_ACTIVITY);
	}

	
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
		super.onActivityResult(requestCode, resultCode, data); 
		int nMode = MFPFileManagerActivity.START_FILE_MANAGER_BY_ITSELF;
		switch(requestCode) { 
			case (SCRIPT_FILE_MANAGER_ACTIVITY) : { 
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = data.getBundleExtra("android.intent.extra.SelectedFile");
					if (bundle != null && bundle.getString("SelectedFilePath") != null) {
						mstrSelectedFilePath = bundle.getString("SelectedFilePath");
						nMode = bundle.getInt("FileManagerOpenMode");
					}
				} else 	{
					return;	// do nothing
				}
				break;
			} 
		}
		if (nMode == MFPFileManagerActivity.START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR)	{
			if (mstrSelectedFilePath.equals("") == false)	{
				openFile(mstrSelectedFilePath);
			}			
		} else if (nMode == MFPFileManagerActivity.START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR)	{
			if (mstrSelectedFilePath.equals("") == false)	{
				saveFile(mstrSelectedFilePath);
			}			
		}
		if (mstrNextTask.equals("openNewFile"))	{
			openNewFile();
			mstrNextTask = "";
		} else if (mstrNextTask.equals("finish"))	{
			finish();
			// need not to set mstrNextTask = "";
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenOrientation = getResources().getConfiguration().orientation;
		if (nScreenOrientation != Configuration.ORIENTATION_LANDSCAPE)	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{        // making it full screen in portrait mode if small screen
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else	{
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		} else	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
					|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{        // making it full screen in landscape mode if small or normal screen
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
			} else	{
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
		}
		
		if (minputMethod != null)	{
			// this means minputMethod has been initialized.
			minputMethod.resetMetrics();	// reset the heights and font size.
			minputMethod.refreshInputMethod();
			minputMethod.showInputMethod(minputMethod.getSelectedPadIndex(), false);
		}		
		
		setSoftKeyState(medtScriptEdtBox, mnSoftKeyState);	// call this function coz we may need ads hidden.
	}

    // After BACK key pressed, prompt to save file.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mbIsFileDirty)	{
        	mstrNextTask = "finish";
            showDialog(DIALOG_FILE_CHANGED);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
	public void setSoftKeyState(EditText edt, int nState)
	{
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		int nScreenOrientation = getResources().getConfiguration().orientation;
		TextView tvScriptState = (TextView)findViewById(R.id.txtviewStatus);
		boolean bInSmallNormalLand = false;
		if (nScreenOrientation == Configuration.ORIENTATION_LANDSCAPE)	{
			if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
					|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
				bInSmallNormalLand = true;
			}
		}
		
		ScrollView svScriptEdtHolder = (ScrollView)findViewById(R.id.scrollViewScriptEdt);
		View vEdtOrEdtHolder = svScriptEdtHolder;
		//View vEdtOrEdtHolder = medtScriptEdtBox;
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT); 
		param.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		if (nState == ENABLE_SHOW_INPUTPAD)	{	// use inputpad and inputpad is visible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
	        }
	        if (minputMethod != null)	{	// show inputpad.
	        	minputMethod.setVisibility(View.VISIBLE);
	        	if (bInSmallNormalLand)	{
	        		tvScriptState.setVisibility(View.GONE);
	        	}
	        }
			param.addRule(RelativeLayout.ABOVE, R.id.layoutSEInputMethodHolder);
			vEdtOrEdtHolder.setLayoutParams(param);
		} else if (nState == ENABLE_HIDE_INPUTPAD)	{	// use inputpad and inputpad is invisible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
	        }
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        	tvScriptState.setVisibility(View.VISIBLE);
	        }
			param.addRule(RelativeLayout.ABOVE, R.id.txtviewStatus);
			vEdtOrEdtHolder.setLayoutParams(param);
		} else if (nState == ENABLE_HIDE_SOFTKEY)	{	// use soft keyboard and soft keyboard is invisible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);	// hide soft keyboard
	        }
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        	tvScriptState.setVisibility(View.VISIBLE);
	        }
			param.addRule(RelativeLayout.ABOVE, R.id.txtviewStatus);
			vEdtOrEdtHolder.setLayoutParams(param);
		} else {	// nstate == ENABLE_SHOW_SOFTKEY, use soft keyboard and soft keyboard is visible.
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.showSoftInput(edt, 0);
	        }
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        	tvScriptState.setVisibility(View.VISIBLE);
	        }
			param.addRule(RelativeLayout.ABOVE, R.id.txtviewStatus);
			vEdtOrEdtHolder.setLayoutParams(param);
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
			imgBtn1.setImageResource(R.drawable.cursorleft);
		} else if (nIconSelector == 1)	{
			imgBtn1.setImageResource(R.drawable.cursorleft_large);
		} else	{
			imgBtn1.setImageResource(R.drawable.cursorleft_xlarge);
		}
		imgBtn1.setId(R.id.btnCursorLeft);
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
			imgBtn2.setImageResource(R.drawable.cursorright);
		} else if (nIconSelector == 1)	{
			imgBtn2.setImageResource(R.drawable.cursorright_large);
		} else	{
			imgBtn2.setImageResource(R.drawable.cursorright_xlarge);
		}
		imgBtn2.setId(R.id.btnCursorRight);
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
			imgBtn3.setImageResource(R.drawable.delete);
		} else if (nIconSelector == 1)	{
			imgBtn3.setImageResource(R.drawable.delete_large);
		} else	{
			imgBtn3.setImageResource(R.drawable.delete_xlarge);
		}
		imgBtn3.setId(R.id.btnDEL);
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
			imgBtn4.setImageResource(R.drawable.enter);
		} else if (nIconSelector == 1)	{
			imgBtn4.setImageResource(R.drawable.enter_large);
		} else	{
			imgBtn4.setImageResource(R.drawable.enter_xlarge);
		}
		imgBtn4.setId(R.id.btnEnter);
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
			imgBtn5.setImageResource(R.drawable.save);
		} else if (nIconSelector == 1)	{
			imgBtn5.setImageResource(R.drawable.save_large);
		} else	{
			imgBtn5.setImageResource(R.drawable.save_xlarge);
		}
		imgBtn5.setId(R.id.btnSave);
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

		//-------------- set actions for convenient buttons ---------------
		OnClickListener lConvenBtnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ActivitySettings.msbEnableBtnPressVibration)	{
					Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
					myVibrator.vibrate(ActivitySettings.VIBERATION_LENGTH);
				}
				int nSelectionStart = medtScriptEdtBox.getSelectionStart();
				int nSelectionEnd = medtScriptEdtBox.getSelectionEnd();
				if (nSelectionStart > nSelectionEnd) {
					int nSelectionSwap = nSelectionEnd;
					nSelectionStart = nSelectionEnd;
					nSelectionEnd = nSelectionSwap;
				}
				if (v.getId() == R.id.btnCursorLeft)	{
					if (nSelectionStart > 0)	{
						medtScriptEdtBox.setSelection(nSelectionStart - 1, nSelectionStart - 1);
					} else	{
						medtScriptEdtBox.setSelection(nSelectionStart, nSelectionStart);
					}
				} else if (v.getId() == R.id.btnCursorRight)	{
					if (nSelectionEnd < medtScriptEdtBox.length())	{
						medtScriptEdtBox.setSelection(nSelectionEnd + 1, nSelectionEnd + 1);
					} else	{
						medtScriptEdtBox.setSelection(nSelectionEnd, nSelectionEnd);
					}
				} else if (v.getId() == R.id.btnDEL)	{
					if (minputMethod.isInputBufferEmpty())	{
						if (nSelectionStart < nSelectionEnd)	{
							medtScriptEdtBox.getText().replace(nSelectionStart, nSelectionEnd, "");
							medtScriptEdtBox.setSelection(nSelectionStart, nSelectionStart);
						} else if (nSelectionStart > 0)	{
							medtScriptEdtBox.getText().replace(nSelectionStart - 1, nSelectionStart, "");
							medtScriptEdtBox.setSelection(nSelectionStart - 1, nSelectionStart - 1);
						}
					} else	{
						minputMethod.typeDelete4InputBuffer();
					}
				} else if (v.getId() == R.id.btnEnter)	{
					if (minputMethod.isInputBufferEmpty())	{
						medtScriptEdtBox.getText().replace(nSelectionStart, nSelectionEnd, "\n");
						medtScriptEdtBox.setSelection(nSelectionStart + 1, nSelectionStart + 1);
					} else	{
						minputMethod.typeEnter4InputBuffer();
					}
				} else if (v.getId() == R.id.btnSave)	{
					if (mbIsFileDirty && mstrFilePath.equals("") == false)	{
						saveFile(mstrFilePath);
					} else if (mnMode == START_FILE_EDITOR_BY_ITSELF)	{
						//minputMethod.flushBufferedString();
						mstrSelectedFilePath = "";
						startScriptFileManager(MFPFileManagerActivity.START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR);
					}
				} else	{	// inputpad hide
					setSoftKeyState(medtScriptEdtBox, ActivitySmartCalc.ENABLE_HIDE_INPUTPAD);
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
				String strTextInBox = medtScriptEdtBox.getText().toString();
				if (v.getId() == R.id.btnCursorLeft) {
					int nNewSelectionStart = medtScriptEdtBox.getSelectionStart();
					if (nNewSelectionStart > 0)	{
						// if selection is in the editable area
						if (strTextInBox.charAt(nNewSelectionStart - 1) == '\n')	{
							nNewSelectionStart --;
						}
						while (nNewSelectionStart > 0)	{
							if (strTextInBox.charAt(nNewSelectionStart - 1) != '\n')	{
								nNewSelectionStart --;
							} else	{
								break;
							}
						}
					}
					medtScriptEdtBox.setSelection(nNewSelectionStart);
					return true;
				} else if (v.getId() == R.id.btnCursorRight) {
					int nNewSelectionEnd = medtScriptEdtBox.getSelectionEnd();
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
					medtScriptEdtBox.setSelection(nNewSelectionEnd);
					return true;
				} else if (v.getId() == R.id.btnDEL) {
					if (minputMethod.isInputBufferEmpty())	{
						int nNewSelectionStart = medtScriptEdtBox.getSelectionStart();
						if (nNewSelectionStart > 0)	{
							// if selection is in the editable area
							if (strTextInBox.charAt(nNewSelectionStart - 1) == '\n')	{
								nNewSelectionStart --;
							}
							while (nNewSelectionStart > 0)	{
								if (strTextInBox.charAt(nNewSelectionStart - 1) != '\n')	{
									nNewSelectionStart --;
								} else	{
									break;
								}
							}
							medtScriptEdtBox.setText(strTextInBox.substring(0, nNewSelectionStart)
									+ strTextInBox.substring(medtScriptEdtBox.getSelectionEnd()));
							medtScriptEdtBox.setSelection(nNewSelectionStart);
						}
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
			// if unsuccessful, try to read input pads config file in assets
			inputStreamPads = getAssets().open(IMMUTABLE_INPUTPAD_CONFIG);
		} catch (IOException e) {
			// still unsuccessful, load nothing.
		}
		LinkedList<TableInputPad> listInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
		
		try {
			// first try to read input pads config file in SD card
			inputStreamPads = new FileInputStream(MFPFileManagerActivity.getConfigFolderFullPath()
												+ MFPFileManagerActivity.STRING_PATH_DIV
												+ ActivityCfgKeyPad.INPUTPAD_SE_CONFIG);
		} catch (FileNotFoundException e0) {
			try	{
				// if unsuccessful, try to read input pads config file in assets
				inputStreamPads = getAssets().open(ActivityCfgKeyPad.INPUTPAD_SE_CONFIG);
			} catch (IOException e) {
				// still unsuccessful, load nothing.
			}
		}
		LinkedList<TableInputPad> listSEInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
		listInputPads.addAll(listSEInputPads);
		return listInputPads;
	}
	
	@Override
	public int calcIMEMaxHeight()	{
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return (int)(metrics.heightPixels *0.5);
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
		int nSelectionStart = medtScriptEdtBox.getSelectionStart();
		int nSelectionEnd = medtScriptEdtBox.getSelectionEnd();
		if (str.length() > 0)	{
			// ensure that some text will be changed
			int nNewSelectionStart = nSelectionStart;
			medtScriptEdtBox.getText().replace(nNewSelectionStart, nSelectionEnd, str);
			nNewSelectionStart = nNewSelectionStart + str.length();
			if (nNewSelectionStart < 0)	{
				nNewSelectionStart = 0;
			}
			if (nNewSelectionStart > medtScriptEdtBox.getText().length())	{
				nNewSelectionStart = medtScriptEdtBox.getText().length();
			}
			medtScriptEdtBox.setSelection(nNewSelectionStart);
			minputMethod.clearInputBuffer();
			// may need to move to another input pad.
			String strTrimmed = str.trim();
			if (strTrimmed.length() > 0	// if it is a function, do not shift to number operators input pad.
					&& (/*strTrimmed.charAt(strTrimmed.length() - 1) == '(' || */strTrimmed.charAt(strTrimmed.length() - 1) == '['))	{
				int idx = 0;
				for (TableInputPad inputPad : minputMethod.mlistShownInputPads)	{
					if (inputPad.mstrName.equals("numbers_operators"))	{
						minputMethod.showInputMethod(idx, true);
						break;
					}
					idx ++;
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
		int nSelectionStart = medtScriptEdtBox.getSelectionStart();
		int nSelectionEnd = medtScriptEdtBox.getSelectionEnd();
		if (inputKeyDef.isFunctionKey() == false)	{	// normal text key
			String strText2Input = minputMethod.type4InputBuffer(inputKeyDef.mstrKeyInput);
			int nCursorPlace = inputKeyDef.mnCursorPlace;
			
			if (strText2Input.length() > 0)	{
				// ensure that some text will be changed
				int nNewSelectionStart = nSelectionStart;
				medtScriptEdtBox.getText().replace(nNewSelectionStart, nSelectionEnd, strText2Input);
				nNewSelectionStart = nNewSelectionStart + strText2Input.length() + nCursorPlace;
				if (nNewSelectionStart < 0)	{
					nNewSelectionStart = 0;
				}
				if (nNewSelectionStart > medtScriptEdtBox.getText().length())	{
					nNewSelectionStart = medtScriptEdtBox.getText().length();
				}
				medtScriptEdtBox.setSelection(nNewSelectionStart);
				if (inputKeyDef.mparent.mparent.mstrName.equals("MFP_key_words"))	{
					// may need to move to another input pad.
					String strKeyName = inputKeyDef.mstrKeyName;
					if (strKeyName.equals("variable_MFP_key_word") || strKeyName.equals("if_MFP_key_word") || strKeyName.equals("elseif_MFP_key_word")
							|| strKeyName.equals("while_MFP_key_word") || strKeyName.equals("until_MFP_key_word") || strKeyName.equals("for_MFP_key_word")
							|| strKeyName.equals("select_MFP_key_word") || strKeyName.equals("throw_MFP_key_word") || strKeyName.equals("solve_MFP_key_word")
							|| strKeyName.equals("slvret_MFP_key_word") || strKeyName.equals("function_MFP_key_word") || strKeyName.equals("return_MFP_key_word")
							|| strKeyName.equals("throw_MFP_key_word"))	{
						int idx = 0;
						for (TableInputPad inputPad : minputMethod.mlistShownInputPads)	{
							if (inputPad.mstrName.equals("abc_keyboard"))	{
								minputMethod.showInputMethod(idx, true);
								break;
							}
							idx ++;
						}
					} else if (strKeyName.equals("to_MFP_key_word") || strKeyName.equals("step_MFP_key_word") || strKeyName.equals("case_MFP_key_word")
							|| strKeyName.equals("print_MFP_function"))	{
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
				medtScriptEdtBox.getText().replace(nSelectionStart, nSelectionEnd, "\n");
				medtScriptEdtBox.setSelection(nSelectionStart + 1, nSelectionStart + 1);
			} else	{
				minputMethod.typeEnter4InputBuffer();
			}
			
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("DELETE"))	{
			if (minputMethod.isInputBufferEmpty())	{
				if (nSelectionStart < nSelectionEnd)	{
					medtScriptEdtBox.getText().replace(nSelectionStart, nSelectionEnd, "");
					medtScriptEdtBox.setSelection(nSelectionStart, nSelectionStart);
				} else if (nSelectionStart > 0)	{
					medtScriptEdtBox.getText().replace(nSelectionStart - 1, nSelectionStart, "");
					medtScriptEdtBox.setSelection(nSelectionStart - 1, nSelectionStart - 1);
				}
			} else	{
				minputMethod.typeDelete4InputBuffer();
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("SAVE"))	{
			if (mbIsFileDirty && mstrFilePath.equals("") == false)	{
				saveFile(mstrFilePath);
			} else if (mnMode == START_FILE_EDITOR_BY_ITSELF)	{
				//minputMethod.flushBufferedString();
				mstrSelectedFilePath = "";
				startScriptFileManager(MFPFileManagerActivity.START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR);
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("HIDE"))	{
			setSoftKeyState(medtScriptEdtBox, ActivitySmartCalc.ENABLE_HIDE_INPUTPAD);
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
		//  then defined variables.
		// doesn't support script defined variables at this moment.

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
		
		// then keywords
		String[] arrayKeyWords = Statement.getMFPKeyWords();
		for (String strKeyWord : arrayKeyWords)	{
			if (strKeyWord.length() >= str.length() && strKeyWord.substring(0, str.length()).equalsIgnoreCase(str))	{
				// find it.
				boolean bHasBeenInList = false;
				String strMatched = strKeyWord;
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
					inputKey.mcolorForeground = com.cyzapps.VisualMFP.Color.BLUE;
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
		minputMethod.clearInputBuffer();	// flush buffer only used in shift to another inputpad.
		setSoftKeyState(medtScriptEdtBox, ENABLE_HIDE_INPUTPAD);
	}
}
