package com.cyzapps.SmartMath;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;

public class ActivityTutorialBox extends Activity {

	public String mstrWhichTutorial = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        String strTutorialFileName = "",
        		strTutorialTitle = getString(R.string.what_you_need_to_know_before_start_to_use);
        if (bundle != null)	{
        	strTutorialFileName = bundle.getString("Tutorial_File_Name");
        	if (strTutorialFileName == null)	{
        		strTutorialFileName = "";
        	}
           	
        	strTutorialTitle = bundle.getString("Tutorial_Title");
        	if (strTutorialTitle == null)	{
        		strTutorialTitle = "";
        	}
           	
        	mstrWhichTutorial = bundle.getString("Which_Tutorial");
        	if (mstrWhichTutorial == null)	{
        		mstrWhichTutorial = "";
        	}
        }
		setTitle(strTutorialTitle);
		setContentView(R.layout.tutorial_box);
		
        WebView webviewTutorial = (WebView)findViewById(R.id.webviewTutorial);
        webviewTutorial.setVerticalScrollBarEnabled(true);
        webviewTutorial.setHorizontalScrollBarEnabled(true);
        webviewTutorial.getSettings().setBuiltInZoomControls(true);
	    webviewTutorial.loadUrl(strTutorialFileName);

	    Button btnOKIKnow = (Button)findViewById(R.id.btnOKIHaveKnown);
	    btnOKIKnow.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				finish();
			}
	    	
	    });
	}
	
    @Override
    public void onDestroy()	{
	    CheckBox chkNewShowNextTime = (CheckBox)findViewById(R.id.never_show_it_again);
		if (chkNewShowNextTime.isChecked()) {
    		SharedPreferences app_settings = getApplication().getSharedPreferences(ActivitySettings.SETTINGS, 0);
    		if (app_settings != null) {
    			app_settings.edit().putBoolean("Tutorial_" + mstrWhichTutorial + "_not_show_again", true).commit();
    		}
		}
    	super.onDestroy();
    }
}
