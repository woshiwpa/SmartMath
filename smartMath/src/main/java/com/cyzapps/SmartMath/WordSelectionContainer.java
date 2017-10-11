package com.cyzapps.SmartMath;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class WordSelectionContainer extends LinearLayout	{

	public WordSelectionContainer(Context context) {
		super(context);
	}
	
	public WordSelectionContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AMInputMethod minputMethod = null;
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		minputMethod.handleSwipe(ev, false, true);
		return super.dispatchTouchEvent(ev);
    }		
}

