package com.cyzapps.SmartMath;

import java.util.LinkedList;
import java.util.ListIterator;

import com.cyzapps.SmartMath.InputPadMgrEx.InputKey;
import com.cyzapps.SmartMath.InputPadMgrEx.InputKeyRow;
import com.cyzapps.SmartMath.InputPadMgrEx.TableInputPad;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.adapter.MFPAdapter.InternalFuncInfo;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AMInputMethod extends LinearLayout {

	public static interface InputMethodCaller{
		public abstract LinkedList<ImageButton> genConvenientBtns(int nBtnHeight);
		public abstract LinkedList<TableInputPad> getInputPads();
		public abstract int calcInputKeyBtnHeight();
		public abstract float calcInputKeyTextSize();
		public abstract int calcIMEMaxHeight();
		public abstract void onClickKey(View v);
		public abstract void onClickSelectedWord(String str);
		public abstract LinkedList<String> matchFuncVars(String str);
		public abstract boolean isFlushBufferString(String str);
		public abstract void setShiftKeyState(boolean bShiftKeyPressed);
		public abstract void hideInputMethod();
	}
	
	static final int MIN_DISTANCE = 100;
	private float mfDownX, mfUpX, mfDownY, mfUpY;


	public InputMethodCaller minputMethodCaller = null;
	public EditText medtInput = null;
	protected WordSelectionContainer mwordSelectionContainer = null;
	protected LinearLayout mlLayoutWordSelectionBtnHolder = null;
	protected ConvenientBtnHolder mconvenientBtnHolder = null;
	protected LinkedList<ImageButton> mlistConvenientBtns = new LinkedList<ImageButton>();
	public LinearLayout mlLayoutInputPadHolder = null;
	
	public int mnInputBtnHeight = 10;
	public int mnMaxIMEHeight = 100;
	public float mfTextSize = 10.0f;
	
	protected int mnSelectedPadIndex = -1;	// -1 means no pad is selected at first.
	public int getSelectedPadIndex()	{ return mnSelectedPadIndex;}
	public LinkedList<TableInputPad> mlistShownInputPads = new LinkedList<TableInputPad>();
	public LinkedList<View> mlistShownViews = new LinkedList<View>();
	public LinkedList<View> mlistShownViewsLand = new LinkedList<View>();

	protected boolean mbEnableInputBuffer = false;
	public String mstrBuffered = "";
	public boolean mbShiftKeyPressed = false;
	
	public AMInputMethod(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.am_input_method, this, true);
	}
	
	public AMInputMethod(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.am_input_method, this, true);
	}

 	public void initialize(InputMethodCaller parent, EditText edtInput)	{
 		minputMethodCaller = parent;
 		medtInput = edtInput;

 		resetMetrics();	// reset the heights and font size.
 		
 		mwordSelectionContainer = (WordSelectionContainer)findViewById(R.id.layoutWordSelectionContainer);
 		mwordSelectionContainer.minputMethod = this;
 		mwordSelectionContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mnInputBtnHeight));		
 		mlLayoutWordSelectionBtnHolder = (LinearLayout)findViewById(R.id.layoutWordSelectionBtnHolder);
		
 		mconvenientBtnHolder = (ConvenientBtnHolder)findViewById(R.id.layoutConvenientBtnHolder);
 		mconvenientBtnHolder.minputMethod = this;
 		
 		mlistConvenientBtns = minputMethodCaller.genConvenientBtns(mnInputBtnHeight);
 		populateConvenientBtns(View.VISIBLE);
 		
 		InputPadContainer inputPadContainer = (InputPadContainer)findViewById(R.id.layoutInputPadContainer);
 		inputPadContainer.minputMethod = this;
        mlLayoutInputPadHolder = (LinearLayout)findViewById(R.id.layoutInputPadHolder);

		LinkedList<TableInputPad> listInputPads = minputMethodCaller.getInputPads();
		
		// load all the input pads.
		mlistShownInputPads = populateInputPads(listInputPads);		

		mnSelectedPadIndex = -1;	// no pad is selected at first.
		showInputMethod(0, true);
 	}

 	public void resetMetrics()	{
		mnInputBtnHeight = minputMethodCaller.calcInputKeyBtnHeight();
		mfTextSize = minputMethodCaller.calcInputKeyTextSize();
		mnMaxIMEHeight = minputMethodCaller.calcIMEMaxHeight();
 	}
 	
 	public void refreshWordSelector()	{
		LinkedList<String> listMatched = minputMethodCaller.matchFuncVars(mstrBuffered);
 		mwordSelectionContainer.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, mnInputBtnHeight));		
		mlLayoutWordSelectionBtnHolder.removeAllViews();
		for (int idx = 0 ; idx < listMatched.size(); idx ++)	{
			String str = listMatched.get(idx);
			Button btn = new Button(getContext());
			btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, mnInputBtnHeight));
			btn.setTag(str);
			btn.setText(str);
			btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mfTextSize);
			int nTextColor = Color.RED;
			if (idx > 0)	{
				nTextColor = Color.GREEN;
			}
			btn.setTextColor(nTextColor);
			btn.setBackgroundResource(R.drawable.btn_background);
			btn.setOnClickListener(new OnClickListener()	{

				@Override
				public void onClick(View v) {
					minputMethodCaller.onClickSelectedWord((String)v.getTag());
				}
				
			});
			mlLayoutWordSelectionBtnHolder.addView(btn);
		}	
 	}
 	
 	public void refreshInputMethod()	{
 		refreshWordSelector();
 		
 		mlistConvenientBtns = minputMethodCaller.genConvenientBtns(mnInputBtnHeight);	// regenerate the convienent buttons
 		
		mlistShownViews.clear();
		mlistShownViewsLand.clear();
		LinkedList<TableInputPad> listNewShownInputPads = new LinkedList<TableInputPad>();
		for (int index = 0; index < mlistShownInputPads.size(); index ++)	{
			TableInputPad inputPad = mlistShownInputPads.get(index);
			View vInputPad = populateInputPad(inputPad, false);
			View vInputPadLand = populateInputPad(inputPad, true);
			if (vInputPad != null && vInputPadLand != null)	{
				listNewShownInputPads.add(inputPad);
				vInputPad.setTag(inputPad);
				vInputPadLand.setTag(inputPad);
				mlistShownViews.add(vInputPad);
				mlistShownViewsLand.add(vInputPadLand);
			}
		}
		mlistShownInputPads = listNewShownInputPads;
 	}
 	
	public void showInputMethod(int nSelectedPadIndex, boolean bClearInputBuffer)	{
		if (nSelectedPadIndex < 0 || nSelectedPadIndex >= mlistShownInputPads.size()){
			return;	// invalid index
		} else if (mnSelectedPadIndex < 0 || mnSelectedPadIndex >= mlistShownInputPads.size()){
			// invalid original index
			mnSelectedPadIndex = nSelectedPadIndex;
			if (mlistShownInputPads.get(mnSelectedPadIndex).mbHaveDictionary)	{
				if (bClearInputBuffer)	{
					mlLayoutWordSelectionBtnHolder.removeAllViews();
					mstrBuffered = "";
				}
 				mbEnableInputBuffer = true;
 				mwordSelectionContainer.setVisibility(View.VISIBLE);
 				mconvenientBtnHolder.setVisibility(View.GONE);				
			} else	{
				mbEnableInputBuffer = false;
 				mwordSelectionContainer.setVisibility(View.GONE);
 				mconvenientBtnHolder.setVisibility(View.VISIBLE);
			}
		} else if (mnSelectedPadIndex != nSelectedPadIndex)	{
 			// ok, we switched inputPad.
 			int nfromPadIndex = mnSelectedPadIndex;
 			mnSelectedPadIndex = nSelectedPadIndex;
 			if (mlistShownInputPads.get(nfromPadIndex).mbHaveDictionary == false
 					&& mlistShownInputPads.get(mnSelectedPadIndex).mbHaveDictionary)	{
 				if (bClearInputBuffer)	{
 					mlLayoutWordSelectionBtnHolder.removeAllViews();
 					mstrBuffered = "";
 				}
 				mbEnableInputBuffer = true;
 				mwordSelectionContainer.setVisibility(View.VISIBLE);
 				mconvenientBtnHolder.setVisibility(View.GONE);
 			} else if (mlistShownInputPads.get(nfromPadIndex).mbHaveDictionary
 					&& mlistShownInputPads.get(mnSelectedPadIndex).mbHaveDictionary == false)	{
 				flushBufferedString();
 				mbEnableInputBuffer = false;
 				mwordSelectionContainer.setVisibility(View.GONE);
 				mconvenientBtnHolder.setVisibility(View.VISIBLE);
 			} else if (mlistShownInputPads.get(nfromPadIndex).mbHaveDictionary
 					&& mlistShownInputPads.get(mnSelectedPadIndex).mbHaveDictionary)	{
 				mbEnableInputBuffer = true;
 				mwordSelectionContainer.setVisibility(View.VISIBLE);
 				mconvenientBtnHolder.setVisibility(View.GONE);
 			} else	{
 				mbEnableInputBuffer = false;
 				mwordSelectionContainer.setVisibility(View.GONE);
 				mconvenientBtnHolder.setVisibility(View.VISIBLE);
 			}
 		}
		boolean bIsLand = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
		if (mnSelectedPadIndex >= 0 && mnSelectedPadIndex < mlistShownViews.size())	{
			// hide numbers & operators pad and show a function pad.
			mlLayoutInputPadHolder.removeAllViews();
			View vToBeAdded = null;
			int nNumberOfInputKeyRows = 0;
			if (bIsLand)	{
				vToBeAdded = mlistShownViewsLand.get(mnSelectedPadIndex);
				TableInputPad inputPad = (TableInputPad)vToBeAdded.getTag();
				nNumberOfInputKeyRows = inputPad.mlistKeyRowsLand.size();
			} else	{
				vToBeAdded = mlistShownViews.get(mnSelectedPadIndex);
				TableInputPad inputPad = (TableInputPad)vToBeAdded.getTag();
				nNumberOfInputKeyRows = inputPad.mlistKeyRows.size();
			}
			mlLayoutInputPadHolder.addView(vToBeAdded);
			vToBeAdded.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					(mnMaxIMEHeight < 0)?LayoutParams.MATCH_PARENT	// match parent.
							:Math.min(mnMaxIMEHeight, (nNumberOfInputKeyRows + 1) * mnInputBtnHeight)));	// + 1 is for conv btns
		}
 	}
	
 	public void populateConvenientBtns(int nVisibility)	{
 		mconvenientBtnHolder.removeAllViews();
 		for (ImageButton imgBtn: mlistConvenientBtns)	{
 			mconvenientBtnHolder.addView(imgBtn);
 		}
 		mconvenientBtnHolder.setVisibility(nVisibility);
 	}
 	
 	public Drawable getDrawableFromString(String strDrawableId)	{
 		if (strDrawableId == null || strDrawableId.trim().length() == 0)	{
 			return null;
 		} else if (strDrawableId.trim().equals("enter_key"))	{
 			return getResources().getDrawable(R.drawable.enter_128);
 		} else if (strDrawableId.trim().equals("delete_key"))	{
 			return getResources().getDrawable(R.drawable.delete_128);
 		} else if (strDrawableId.trim().equals("go_key"))	{
 			return getResources().getDrawable(R.drawable.black_start_128);
 		} else if (strDrawableId.trim().equals("hide_key"))	{
 			return getResources().getDrawable(R.drawable.hide_keypad_128);
 		} else if (strDrawableId.trim().equals("save_key"))	{
 			return getResources().getDrawable(R.drawable.save_128);
 		} else if (strDrawableId.trim().equals("next_key"))	{
 			return getResources().getDrawable(R.drawable.next_128);
 		} else	{
 			return null;
 		}
 	}
 	
 	public int getDrawableIdFromString(String strDrawableId)	{
 		if (strDrawableId == null || strDrawableId.trim().length() == 0)	{
 			return 0;
 		} else if (strDrawableId.trim().equals("enter_key"))	{
 			return R.drawable.enter_128;
 		} else if (strDrawableId.trim().equals("delete_key"))	{
 			return R.drawable.delete_128;
 		} else if (strDrawableId.trim().equals("go_key"))	{
 			return R.drawable.black_start_128;
 		} else if (strDrawableId.trim().equals("hide_key"))	{
 			return R.drawable.hide_keypad_128;
 		} else if (strDrawableId.trim().equals("save_key"))	{
 			return R.drawable.save_128;
 		} else if (strDrawableId.trim().equals("next_key"))	{
 			return R.drawable.next_128;
 		} else	{
 			return 0;
 		}
 	}
 	
	public View populateInputPad(TableInputPad inputPad, boolean bIsLand)	{
		// shown hidden inputpads as well
		TableInputPad inputPadTable = inputPad;
		LinkedList<InputKeyRow> listInputKeyRows = null;
		LinearLayout inputPadView = new LinearLayout(getContext());
		inputPadView.setOrientation(LinearLayout.VERTICAL);
		int nNumofColumns = 1;
		if (bIsLand)	{
			nNumofColumns = inputPadTable.mnNumofColumnsLand;
			listInputKeyRows = inputPadTable.mlistKeyRowsLand;
		} else	{
			nNumofColumns = inputPadTable.mnNumofColumns;
			listInputKeyRows = inputPadTable.mlistKeyRows;
		}
		if (nNumofColumns < 1)	{
			nNumofColumns = 1;
		}
		int nActualBtnHeight = (int) (mnInputBtnHeight * (bIsLand?inputPad.mdKeyHeightLand:inputPad.mdKeyHeight));
		for (InputKeyRow inputKeyRow : listInputKeyRows)	{
			LinearLayout keyRow = new LinearLayout(getContext());
			keyRow.setLayoutParams(new LayoutParams( LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
			keyRow.setOrientation(LinearLayout.HORIZONTAL);
			LinkedList<InputKey> listInputKeys = inputKeyRow.mlistInputKeys;
			double dAutoSpanActualValue = inputKeyRow.getAutoSpanValue(nNumofColumns);
			double dTotalSpan = inputKeyRow.getTotalSpan(nNumofColumns);
			for (InputKey inputKey : listInputKeys)	{
				if (inputKey.mdGap2Prev > 0)	{
					TextView vGap = new TextView(getContext());
					vGap.setText("");
					vGap.setBackgroundColor(Color.TRANSPARENT);
					vGap.setLayoutParams(new LayoutParams(0,nActualBtnHeight, (float)inputKey.mdGap2Prev));
					keyRow.addView(vGap);
				}
				
				if (inputKey.mstrDrawable == null || inputKey.mstrDrawable.equals(InputKey.DEFAULT_DRAWABLE))	{
					Button btn = new Button(getContext());
					if (inputKey.mdSpan == InputKey.AUTO_SPAN)	{
						btn.setLayoutParams(new LayoutParams( 0,nActualBtnHeight, (float)dAutoSpanActualValue));
					} else	{
						btn.setLayoutParams(new LayoutParams( 0,nActualBtnHeight, (float)inputKey.mdSpan));
					}
					btn.setSingleLine(true); 	// single line text
	
					btn.setText(inputKey.mstrKeyShown);
					int nTextColor = Color.argb(inputKey.mcolorForeground.mnAlpha, inputKey.mcolorForeground.mnR,
							inputKey.mcolorForeground.mnG, inputKey.mcolorForeground.mnB);
					btn.setTextColor(nTextColor);
					btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mfTextSize);
					btn.setBackgroundResource(R.drawable.btn_background);
					btn.setTag(inputKey);
					btn.setOnClickListener(new OnClickListener()	{
	
						@Override
						public void onClick(View v) {
							minputMethodCaller.onClickKey(v);
						}
						
					});
					keyRow.addView(btn);
				} else	{
					ImageButton btn = new ImageButton(getContext());
					if (inputKey.mdSpan == InputKey.AUTO_SPAN)	{
						btn.setLayoutParams(new LayoutParams( 0,nActualBtnHeight, (float)dAutoSpanActualValue));
					} else	{
						btn.setLayoutParams(new LayoutParams( 0,nActualBtnHeight, (float)inputKey.mdSpan));
					}
					int nDrawableId = getDrawableIdFromString(inputKey.mstrDrawable);
					btn.setImageResource(nDrawableId);
					btn.setScaleType(ScaleType.FIT_CENTER);
					btn.setBackgroundResource(R.drawable.btn_background);
					btn.setTag(inputKey);
					btn.setOnClickListener(new OnClickListener()	{
	
						@Override
						public void onClick(View v) {
							minputMethodCaller.onClickKey(v);
						}
						
					});
					keyRow.addView(btn);
				}
			}
			if (dTotalSpan < nNumofColumns)	{
				// fill the gap at the end of row
				TextView vGap = new TextView(getContext());
				vGap.setText("");
				vGap.setBackgroundColor(Color.TRANSPARENT);
				vGap.setLayoutParams(new LayoutParams(0,LayoutParams.WRAP_CONTENT, (float)(nNumofColumns - dTotalSpan)));
				keyRow.addView(vGap);
				
			}
			inputPadView.addView(keyRow);
		}
		return inputPadView;
	}
	
	public LinkedList<TableInputPad> populateInputPads(LinkedList<TableInputPad> listInputPads)	{
		// load all the input pads.
		LinkedList<TableInputPad> listShownInputPads = new LinkedList<TableInputPad>();
		mlistShownViews.clear();
		mlistShownViewsLand.clear();
		for (int index = 0; index < listInputPads.size(); index ++)	{
			TableInputPad inputPad = listInputPads.get(index);
			if (inputPad.mbVisible)	{
				View vInputPad = populateInputPad(inputPad, false);
				View vInputPadLand = populateInputPad(inputPad, true);
				if (vInputPad != null && vInputPadLand != null)	{
					listShownInputPads.add(inputPad);
					vInputPad.setTag(inputPad);
					vInputPadLand.setTag(inputPad);
					mlistShownViews.add(vInputPad);
					mlistShownViewsLand.add(vInputPadLand);
				}
			}
		}
		return listShownInputPads;
	}
	
	public boolean isInputBufferEmpty()	{
		if (mbEnableInputBuffer && mstrBuffered != null && mstrBuffered.length() > 0)	{
			return false;
		}
		return true;
	}
	
	public void clearInputBuffer()	{
		mstrBuffered = "";
		refreshWordSelector();
	}
	
	public void typeDelete4InputBuffer()	{
		if (mbEnableInputBuffer && mstrBuffered.length() > 0)	{
			mstrBuffered = mstrBuffered.substring(0, mstrBuffered.length() - 1);
			refreshWordSelector();
		}
	}
	
	public void typeEnter4InputBuffer()	{
		flushBufferedString();
	}
	
	public void typeHelp4InputBuffer()	{
		mstrBuffered = "help " + mstrBuffered;
		refreshWordSelector();
	}
	
	public String type4InputBuffer(String strOriginalInput)	{
		if (mbEnableInputBuffer)	{
			boolean bIsFlushInput = minputMethodCaller.isFlushBufferString(strOriginalInput);
			if (bIsFlushInput)	{
				String strReturn = mstrBuffered + strOriginalInput;
				mstrBuffered = "";
				refreshWordSelector();
				return strReturn;
			} else	{
				mstrBuffered += strOriginalInput;
				refreshWordSelector();
				return "";
			}
		} else	{
			return strOriginalInput;
		}
	}
	
	public void flushBufferedString()	{
		if (mbEnableInputBuffer && mstrBuffered != null && mstrBuffered.length() > 0)	{
			int nSelectionStart = medtInput.getSelectionStart();
			int nSelectionEnd = medtInput.getSelectionEnd();
			
			medtInput.getEditableText().replace(nSelectionStart, nSelectionEnd, mstrBuffered);
			mstrBuffered = "";
			refreshWordSelector();
		}
	}
	
    public void onRightToLeftSwipe(){
		// move to the right inputpad
    	int nNewSelectedPadIdx = mnSelectedPadIndex;
		if (mnSelectedPadIndex >= mlistShownInputPads.size() - 1)	{
			nNewSelectedPadIdx = 0;
		} else	{
			nNewSelectedPadIdx = mnSelectedPadIndex + 1;
		}
		showInputMethod(nNewSelectedPadIdx, true);
	}

	public void onLeftToRightSwipe(){
		// move to the left inputpad
    	int nNewSelectedPadIdx = mnSelectedPadIndex;
		if (mnSelectedPadIndex <= 0)	{
			nNewSelectedPadIdx = mlistShownInputPads.size() - 1;
		} else	{
			nNewSelectedPadIdx = mnSelectedPadIndex - 1;
		}
		showInputMethod(nNewSelectedPadIdx, true);
	}


	public boolean handleSwipe(MotionEvent event, boolean bEnableHSwipe, boolean bEnableVSwipe) {
		
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN: {
				mfDownX = event.getX();
				mfDownY = event.getY();
				return true;
			}
			case MotionEvent.ACTION_UP: {
				mfUpX = event.getX();
				mfUpY = event.getY();

				float fDeltaX = mfDownX - mfUpX;
				float fDeltaY = mfDownY - mfUpY;

				
				if(Math.abs(fDeltaX) >= Math.abs(fDeltaY) && Math.abs(fDeltaX) > MIN_DISTANCE && bEnableHSwipe){
					// swipe horizontal?
					// left or right
					if(fDeltaX < 0) {
						this.onLeftToRightSwipe();
						return true;
					} else if(fDeltaX > 0) {
						this.onRightToLeftSwipe();
						return true;
					}
				} else if(Math.abs(fDeltaY) >= Math.abs(fDeltaX) && Math.abs(fDeltaY) > AMInputMethod.MIN_DISTANCE && bEnableVSwipe){
					// swipe vertical?
					// up or down
					if(fDeltaY < 0) {
						minputMethodCaller.hideInputMethod();
						return true;
					}
				} else {
					return false; // We don't consume the event because the swiping distance is too short or it is swipe up and down.
				}

				return true;
			}
		}
		return false;
	}
}
