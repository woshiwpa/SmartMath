package com.cyzapps.SmartMath;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.adapter.AbstractExprConverter;
import com.cyzapps.adapter.AndroidStorageOptions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class ActivitySmCRecord extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(getString(R.string.select_record_title));
		setContentView(R.layout.smc_record_list);
		WebView wvSmCRecord = (WebView) findViewById(R.id.wvSmCRecord);
		wvSmCRecord.setVerticalScrollBarEnabled(true);
		wvSmCRecord.setHorizontalScrollBarEnabled(true);
		wvSmCRecord.getSettings().setBuiltInZoomControls(true);
		wvSmCRecord.getSettings().setJavaScriptEnabled(true);
		wvSmCRecord.setWebViewClient(new WebViewClient(){
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (AbstractExprConverter.isQuotedUrlExprStr(url))	{
	                // copy the selected expression text into input
					String strSelectedExpr = AbstractExprConverter.convtQuotedUrl2PlainStr(url);
					Intent intentRecordList = new Intent(ActivitySmCRecord.this, ActivitySmartCalc.class);
					if (intentRecordList != null) {
						Bundle b = new Bundle();
						if (b != null) {
							b.putString("Record", strSelectedExpr);
							intentRecordList.putExtras(b);
						}
					}
					setResult(Activity.RESULT_OK, intentRecordList);
				   	finish();
				} else if (url.length() >= ActivitySmartCalc.ACHART_URL_HEADER.length()
						&& url.substring(0, ActivitySmartCalc.ACHART_URL_HEADER.length()).equalsIgnoreCase(ActivitySmartCalc.ACHART_URL_HEADER))	{
					// this is a chart
					String strPath = url.substring(ActivitySmartCalc.ACHART_URL_HEADER.length()).trim();
					try {
						strPath = URLDecoder.decode(strPath, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// Do nothing here;
					}
					String strErrorMsg = MFPFileManagerActivity.openChartFile(ActivitySmCRecord.this, strPath);
					if (strErrorMsg != null && strErrorMsg.trim().length() != 0)	{
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(ActivitySmCRecord.this, strErrorMsg, duration);
						toast.show();
					}
				}
				return true;	// always intercept, otherwise may cause crash problem.
            }
		});
	       //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();
        String strHistory = "";
        if (bundle != null)	{
        	//Next extract the values using the key as
        	strHistory = bundle.getString("HISTORY_CONTEXT");
        	if (strHistory == null)	{
        		strHistory = getString(R.string.no_records);
        	}
        }
        String strHistoryStorageIdentified = strHistory.replace(ActivitySmartCalc.SELECTED_STORAGE_PATH_UNIMATCH,
        														AndroidStorageOptions.getSelectedStoragePath()); 
        wvSmCRecord.loadDataWithBaseURL("file:///android_asset/mathscribe/index.html",
        		strHistoryStorageIdentified, "text/html", "utf-8", "");

	}
}
