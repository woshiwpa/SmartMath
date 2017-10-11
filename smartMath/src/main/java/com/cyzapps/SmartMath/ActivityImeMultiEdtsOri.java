package com.cyzapps.SmartMath;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.SmartMath.InputPadMgrEx.InputKey;
import com.cyzapps.SmartMath.InputPadMgrEx.TableInputPad;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.MFPAdapter.FunctionEntry;
import com.cyzapps.adapter.MFPAdapter.InternalFuncInfo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;

import com.cyzapps.VisualMFP.Color;

import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ActivityImeMultiEdtsOri extends Activity implements AMInputMethod.InputMethodCaller	{

	protected View mvAds = null;
	
    public static final int ENABLE_SHOW_INPUTPAD = -1;
    public static final int ENABLE_HIDE_INPUTPAD = 0;
    public static final int ENABLE_HIDE_SOFTKEY = 1;
    public static final int ENABLE_SHOW_SOFTKEY = 2;

	public String mstrImmutableInputPadConfig = "immutable_inputpad.cfg";
	public String mstrUsrDefInputPadConfig = "inputpad.cfg";

	protected int mnSoftKeyState = ENABLE_SHOW_INPUTPAD;	// 0 means use but hide inputpad, -1 means use and show inputpad
												// 1 means use but hide softkeyboard, 2 means use and show soft keyboard.

	protected AMInputMethod minputMethod = null;
	
	protected LinearLayout mlayoutBaseView;
	protected LinearLayout mlayoutIMEHolder;
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mlayoutBaseView = new LinearLayout(this);
        mlayoutIMEHolder = new LinearLayout(this);
	}
	
	protected void setFullScreenForSmallLandscape()	{
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
		
	}
	
	protected void initInputMethod(EditText edtFirstFocus)	{
		List<View> listAllChildren = getAllChildrenInclGrand(mlayoutBaseView);
		for (int idx = 0; idx < listAllChildren.size(); idx ++)	{
			View vChild = listAllChildren.get(idx);
			if (vChild instanceof EditText)	{
				setImeForEditor((EditText)vChild);
			}
		}

		//--- Show input method
		minputMethod = new AMInputMethod(this);
		mlayoutIMEHolder.addView(minputMethod);

		edtFirstFocus.requestFocus();
		minputMethod.initialize(this, edtFirstFocus);
		setSoftKeyState(edtFirstFocus, ENABLE_HIDE_INPUTPAD);	// use inputpad and hide it to show more space

	}

	protected void setImeForEditor(EditText edt)	{
		edt.setOnTouchListener(new OnTouchListener()	{
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
        
		edt.setOnClickListener(new OnClickListener(){

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
		
		edt.setOnLongClickListener(new OnLongClickListener(){

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
			   	return true;	// disable text selection because not needed.
			}
			
		});
		
		edt.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)	{
					// this is necessary because the soft key state may only set for the previous edit text.
				   	if (mnSoftKeyState == ENABLE_SHOW_INPUTPAD || mnSoftKeyState == ENABLE_HIDE_INPUTPAD)	{
				   		// do not use soft keyboard, use input pad
						setSoftKeyState((EditText)v, ENABLE_SHOW_INPUTPAD);
				   	} else {	// if (mnSoftKeyState == ENABLE_SHOW_SOFTKEY || mnSoftKeyState == ENABLE_HIDE_SOFTKEY)	{
				   		// show the soft keyboard and disable input pad
				   		setSoftKeyState((EditText)v, ENABLE_SHOW_SOFTKEY);
				   	}
				   	
					if ((minputMethod.medtInput.getInputType() & InputType.TYPE_CLASS_NUMBER)
							== InputType.TYPE_CLASS_NUMBER)	{
						minputMethod.showInputMethod(1, true); 	// show number and operators
					} else	{
						minputMethod.showInputMethod(0, true); 	// show characters
					}
				}
			}
			
		});
	}

	public Boolean isAsyncTaskRunning()	{
		return false;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		setFullScreenForSmallLandscape();
		
		if (!isAsyncTaskRunning())	{

		}
		
		if (minputMethod != null)	{
			// this means minputMethod has been initialized.
			minputMethod.resetMetrics();	// reset the heights and font size.
			minputMethod.refreshInputMethod();
			minputMethod.showInputMethod(minputMethod.getSelectedPadIndex(), false);
		}		
		
		setSoftKeyState(minputMethod.medtInput, mnSoftKeyState);	// call this function coz we may need ads hidden.
	}

	public void setSoftKeyState(EditText edt, int nState)
	{
		if (minputMethod.medtInput != edt)	{	// do not use getId because some ids might be defined the same.
			minputMethod.medtInput = edt;
		}
		if (nState == ENABLE_SHOW_INPUTPAD)	{	// use inputpad and inputpad is visible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
	        }
	        if (minputMethod != null)	{	// show inputpad.
	        	minputMethod.setVisibility(View.VISIBLE);
	        }
		} else if (nState == ENABLE_HIDE_INPUTPAD)	{	// use inputpad and inputpad is invisible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);
	        }
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        }
		} else if (nState == ENABLE_HIDE_SOFTKEY)	{	// use soft keyboard and soft keyboard is invisible
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.hideSoftInputFromWindow(edt.getWindowToken(), 0);	// hide soft keyboard
	        }
	        if (minputMethod != null)	{	// hide inputpad.
	        	minputMethod.setVisibility(View.GONE);
	        }
		} else {	// nstate == ENABLE_SHOW_SOFTKEY, use soft keyboard and soft keyboard is visible.
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if (imm != null)	{
	        	imm.showSoftInput(edt, 0);
	        }
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
			imgBtn4.setImageResource(R.drawable.forward);
		} else if (nIconSelector == 1)	{
			imgBtn4.setImageResource(R.drawable.forward_large);
		} else	{
			imgBtn4.setImageResource(R.drawable.forward_xlarge);
		}
		imgBtn4.setId(R.id.btnNext);
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
			imgBtn5.setImageResource(R.drawable.inputpadhide);
		} else if (nIconSelector == 1)	{
			imgBtn5.setImageResource(R.drawable.inputpadhide_large);
		} else	{
			imgBtn5.setImageResource(R.drawable.inputpadhide_xlarge);
		}
		imgBtn5.setId(R.id.btnHideInputPad);
		imgBtn5.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imgBtn5.setAdjustViewBounds(true);
		listImageBtns.add(imgBtn5);

		//-------------- set actions for convenient buttons ---------------
		OnClickListener lConvenBtnClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ActivitySettings.msbEnableBtnPressVibration)	{
					Vibrator myVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
					myVibrator.vibrate(ActivitySettings.VIBERATION_LENGTH);
				}
				int nSelectionStart = minputMethod.medtInput.getSelectionStart();
				int nSelectionEnd = minputMethod.medtInput.getSelectionEnd();
				if (nSelectionStart > nSelectionEnd) {
					int nSelectionSwap = nSelectionEnd;
					nSelectionStart = nSelectionEnd;
					nSelectionEnd = nSelectionSwap;
				}
				if (v.getId() == R.id.btnCursorLeft)	{
					if (nSelectionStart > 0)	{
						minputMethod.medtInput.setSelection(nSelectionStart - 1, nSelectionStart - 1);
					} else	{
						minputMethod.medtInput.setSelection(nSelectionStart, nSelectionStart);
					}
				} else if (v.getId() == R.id.btnCursorRight)	{
					if (nSelectionEnd < minputMethod.medtInput.length())	{
						minputMethod.medtInput.setSelection(nSelectionEnd + 1, nSelectionEnd + 1);
					} else	{
						minputMethod.medtInput.setSelection(nSelectionEnd, nSelectionEnd);
					}
				} else if (v.getId() == R.id.btnDEL)	{
					if (minputMethod.isInputBufferEmpty())	{
						if (nSelectionStart < nSelectionEnd)	{
							minputMethod.medtInput.getText().replace(nSelectionStart, nSelectionEnd, "");
							minputMethod.medtInput.setSelection(nSelectionStart, nSelectionStart);
						} else if (nSelectionStart > 0)	{
							minputMethod.medtInput.getText().replace(nSelectionStart - 1, nSelectionStart, "");
							minputMethod.medtInput.setSelection(nSelectionStart - 1, nSelectionStart - 1);
						}
					} else	{
						minputMethod.typeDelete4InputBuffer();
					}
				} else if (v.getId() == R.id.btnNext)	{
					boolean bStartNavigation = false;
					List<View> listAllChildren = getAllChildrenInclGrand(mlayoutBaseView);
					for (int idx = 0; idx < listAllChildren.size(); idx ++)	{
						View vChild = listAllChildren.get(idx);
						if (vChild == minputMethod.medtInput)	{
							bStartNavigation = true;
						}
						if ((vChild instanceof EditText) && bStartNavigation && vChild != minputMethod.medtInput)	{
							View vThis = vChild;
							boolean bIsVisible = true;
							while(vThis != mlayoutBaseView && vThis != null)	{
								if (vThis.getVisibility() != View.VISIBLE)	{
									bIsVisible = false;
									break;
								} else if (!(vThis.getParent() instanceof View))	{
									// parent is not a view, so on the top now.
									break;
								} else	{
									vThis = (View) vThis.getParent();
								}
							}
							if (bIsVisible)	{
								((EditText)vChild).requestFocus();	// request Focus will assign minputMethod.medt
								minputMethod.clearInputBuffer();	// flush buffer only used in shift to another inputpad.
								minputMethod.refreshInputMethod();
								setSoftKeyState(((EditText)vChild), mnSoftKeyState);	// this is needed because in Android 3.x request focus may not apply softkeystate on time.
								break;
							}
						}
					}
				} else	{	// inputpad hide
					setSoftKeyState(minputMethod.medtInput, ActivitySmartCalc.ENABLE_HIDE_INPUTPAD);
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
				if (v.getId() == R.id.btnCursorLeft) {
					minputMethod.medtInput.setSelection(0);
					return true;
				} else if (v.getId() == R.id.btnCursorRight) {
					int nTextLen = minputMethod.medtInput.getText().length();
					minputMethod.medtInput.setSelection(nTextLen);
					return true;
				} else if (v.getId() == R.id.btnDEL) {
					if (minputMethod.isInputBufferEmpty())	{
						minputMethod.medtInput.setText("");
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
			inputStreamPads = getAssets().open(mstrImmutableInputPadConfig);
		} catch (IOException e) {
			// still unsuccessful, load nothing.
		}
		LinkedList<TableInputPad> listInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
		
		try {
			// first try to read input pads config file in SD card
			inputStreamPads = new FileInputStream(MFPFileManagerActivity.getConfigFolderFullPath()
												+ MFPFileManagerActivity.STRING_PATH_DIV
												+ mstrUsrDefInputPadConfig);
		} catch (FileNotFoundException e0) {
			try	{
				// if unsuccessful, try to read input pads config file in assets
				inputStreamPads = getAssets().open(mstrUsrDefInputPadConfig);
			} catch (IOException e) {
				// still unsuccessful, load nothing.
			}
		}
		LinkedList<TableInputPad> listIntegPlotInputPads = InputPadMgrEx.readInputPadsFromXML(inputStreamPads);
		listInputPads.addAll(listIntegPlotInputPads);
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
		int nSelectionStart = minputMethod.medtInput.getSelectionStart();
		int nSelectionEnd = minputMethod.medtInput.getSelectionEnd();
		if (str.length() > 0)	{
			// ensure that some text will be changed
			int nNewSelectionStart = nSelectionStart;
			minputMethod.medtInput.getText().replace(nNewSelectionStart, nSelectionEnd, str);
			nNewSelectionStart = nNewSelectionStart + str.length();
			if (nNewSelectionStart < 0)	{
				nNewSelectionStart = 0;
			}
			if (nNewSelectionStart > minputMethod.medtInput.getText().length())	{
				nNewSelectionStart = minputMethod.medtInput.getText().length();
			}
			minputMethod.medtInput.setSelection(nNewSelectionStart);
			minputMethod.clearInputBuffer();
			// may need to move to another input pad.
			String strTrimmed = str.trim();
			if (strTrimmed.length() > 0	// do not move to number opt input pad if we are not in command line.
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
		int nSelectionStart = minputMethod.medtInput.getSelectionStart();
		int nSelectionEnd = minputMethod.medtInput.getSelectionEnd();
		if (inputKeyDef.isFunctionKey() == false)	{	// normal text key
			String strText2Input = minputMethod.type4InputBuffer(inputKeyDef.mstrKeyInput);
			int nCursorPlace = inputKeyDef.mnCursorPlace;
			
			if (strText2Input.length() > 0)	{
				// ensure that some text will be changed
				int nNewSelectionStart = nSelectionStart;
				minputMethod.medtInput.getText().replace(nNewSelectionStart, nSelectionEnd, strText2Input);
				nNewSelectionStart = nNewSelectionStart + strText2Input.length() + nCursorPlace;
				if (nNewSelectionStart < 0)	{
					nNewSelectionStart = 0;
				}
				if (nNewSelectionStart > minputMethod.medtInput.getText().length())	{
					nNewSelectionStart = minputMethod.medtInput.getText().length();
				}
				minputMethod.medtInput.setSelection(nNewSelectionStart);
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("SHIFT"))	{
			// InputKey.DEFAULT_FOREGROUND_COLOR is the color b4 press, so if it is DEFAULT_FOREGROUND_COLOR, means shift key is pressed
			boolean bShiftKeyPressed = inputKeyDef.mcolorForeground.isEqual(InputKey.DEFAULT_FOREGROUND_COLOR);
			setShiftKeyState(bShiftKeyPressed);
			minputMethod.refreshInputMethod();
			minputMethod.showInputMethod(minputMethod.getSelectedPadIndex(), false);
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("DELETE"))	{
			if (minputMethod.isInputBufferEmpty())	{
				if (nSelectionStart < nSelectionEnd)	{
					minputMethod.medtInput.getText().replace(nSelectionStart, nSelectionEnd, "");
					minputMethod.medtInput.setSelection(nSelectionStart, nSelectionStart);
				} else if (nSelectionStart > 0)	{
					minputMethod.medtInput.getText().replace(nSelectionStart - 1, nSelectionStart, "");
					minputMethod.medtInput.setSelection(nSelectionStart - 1, nSelectionStart - 1);
				}
			} else	{
				minputMethod.typeDelete4InputBuffer();
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("NEXT"))	{
			boolean bStartNavigation = false;
			List<View> listAllChildren = getAllChildrenInclGrand(mlayoutBaseView);
			for (int idx = 0; idx < listAllChildren.size(); idx ++)	{
				View vChild = listAllChildren.get(idx);
				if (vChild == minputMethod.medtInput)	{
					bStartNavigation = true;
				}
				if ((vChild instanceof EditText) && bStartNavigation && vChild != minputMethod.medtInput)	{
					View vThis = vChild;
					boolean bIsVisible = true;
					while(vThis != mlayoutBaseView && vThis != null)	{
						if (vThis.getVisibility() != View.VISIBLE)	{
							bIsVisible = false;
							break;
						} else if (!(vThis.getParent() instanceof View))	{
							// parent is not a view, so on the top now.
							break;
						} else	{
							vThis = (View) vThis.getParent();
						}
					}
					if (bIsVisible)	{
						((EditText)vChild).requestFocus();	// request Focus will assign minputMethod.medt
						minputMethod.clearInputBuffer();	// flush buffer only used in shift to another inputpad.
						minputMethod.refreshInputMethod();
						setSoftKeyState(((EditText)vChild), mnSoftKeyState);	// this is needed because in Android 3.x 
						break;
					}
				}
			}
		} else if (inputKeyDef.mstrKeyFunction.equalsIgnoreCase("HIDE"))	{
			setSoftKeyState(minputMethod.medtInput, ActivitySmartCalc.ENABLE_HIDE_INPUTPAD);
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
		minputMethod.clearInputBuffer();	// flush buffer only used in shift to another inputpad.
		setSoftKeyState(minputMethod.medtInput, ENABLE_HIDE_INPUTPAD);
	}
	
	public static List<View> getAllChildrenInclGrand(View v) {
	    List<View> visited = new ArrayList<View>();
	    List<View> unvisited = new ArrayList<View>();
	    unvisited.add(v);

	    while (!unvisited.isEmpty()) {
	        View child = unvisited.remove(0);
	        visited.add(child);
	        if (!(child instanceof ViewGroup)) continue;
	        ViewGroup group = (ViewGroup) child;
	        final int childCount = group.getChildCount();
	        for (int i=0; i<childCount; i++) 	{
	        	unvisited.add(group.getChildAt(i));
	        }
	    }

	    return visited;
	}
}
