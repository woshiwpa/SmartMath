package com.cyzapps.SmartMath;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class InputPadContainer extends LinearLayout	{

	public InputPadContainer(Context context) {
		super(context);
	}
	
	public InputPadContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AMInputMethod minputMethod = null;
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		minputMethod.handleSwipe(ev, true, false);
		return super.dispatchTouchEvent(ev);
    }		
}

