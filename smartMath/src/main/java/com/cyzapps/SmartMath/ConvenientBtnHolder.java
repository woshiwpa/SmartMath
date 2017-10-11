package com.cyzapps.SmartMath;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class ConvenientBtnHolder extends LinearLayout	{

	public ConvenientBtnHolder(Context context) {
		super(context);
	}
	
	public ConvenientBtnHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AMInputMethod minputMethod = null;
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
		minputMethod.handleSwipe(ev, true, true);
		return super.dispatchTouchEvent(ev);
    }		
}
