package com.cyzapps.SmartMath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.adapter.AbstractExprConverter;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;
import com.cyzapps.imgproc.ImageMgr;
import com.cyzapps.mathrecog.ExprRecognizer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityQuickRecog extends Activity {
    protected CameraPreview mcameraPreview;
    protected SelectRectView mselectRectView;
    protected Camera mCamera;
    protected boolean mbHasBackFacingCamera = false;

	public static final int FINGER_PAINT_ACTIVITY = 1;
	private static final int ITEM0 = Menu.FIRST;
	
	protected CheckBox mchkTurnOnFlash;
	protected Button mbtnStartPreviewCalc;
	protected Button mbtnStartPreviewRecog;
	protected Button mbtnStartPreviewPlot;
	protected Button mbtnHelp;
	
	protected ProgressDialog mdlgProgress = null;
	protected Thread mthreadLoad = null;
	
	public static final String PHOTO_TAKEN_TUTORIAL_FILE_PATH = "HowtoInfo/take_photo_right_wrong";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
Log.e("ActivityQuickRecog", "onCreate");
        super.onCreate(savedInstanceState);

        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.quick_recog);

        // need not to verify if there are at least one back-facing camera because we have done this in smart calc
        // if no back-facing camera, then the quick recog button will not show.
        
        LinearLayout linearLayoutSelectionHolder = (LinearLayout)findViewById(R.id.linearLayoutSelectRectangle);
        mselectRectView = new SelectRectView(this);
        linearLayoutSelectionHolder.addView(mselectRectView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		
		mchkTurnOnFlash = (CheckBox)findViewById(R.id.turn_on_flash);

		mbtnStartPreviewCalc = (Button)findViewById(R.id.btnStartPreviewCalc);
        mbtnStartPreviewCalc.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// prevent multiple click
		    	enableButtons(false);
				takeAction(CameraPreview.CALC_AFTER_TAKING_SNAPSHOT);
			}
        	
        });

		mbtnStartPreviewRecog = (Button)findViewById(R.id.btnStartPreviewRecog);
		mbtnStartPreviewRecog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// prevent multiple click
		    	enableButtons(false);
				takeAction(CameraPreview.READ_AFTER_TAKING_SNAPSHOT);
			}
        	
        });

		mbtnStartPreviewPlot = (Button)findViewById(R.id.btnStartPreviewPlot);
		mbtnStartPreviewPlot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// prevent multiple click
				enableButtons(false);
				takeAction(CameraPreview.PLOT_AFTER_TAKING_SNAPSHOT);
			}
        	
        });
		
		// disable the three buttons wait until preview really started (surface is created) to enable them.
		enableButtons(false);		
    	
		mbtnHelp = (Button)findViewById(R.id.btnPreviewHelp);
		mbtnHelp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
			   	Intent intentHelp = new Intent(ActivityQuickRecog.this, ActivityShowHelp.class);  
			   	Bundle bundle = new Bundle();
			   	bundle.putString("HELP_CONTENT", "recog_print");
			   	//Add this bundle to the intent
			   	intentHelp.putExtras(bundle);
			   	startActivity(intentHelp);
			}
        	
        });
		
		// now set btn images, text and font size based on screen size.
		int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL)	{
			mbtnStartPreviewCalc.setText("");
			mbtnStartPreviewRecog.setText("");
			mbtnStartPreviewPlot.setText("");
			mbtnHelp.setText("");
			mchkTurnOnFlash.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewCalc.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewRecog.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewPlot.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnHelp.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewCalc.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start, 0, 0, 0);
			mbtnStartPreviewRecog.setCompoundDrawablesWithIntrinsicBounds( R.drawable.recognize, 0, 0, 0);
			mbtnStartPreviewPlot.setCompoundDrawablesWithIntrinsicBounds( R.drawable.startplot, 0, 0, 0);
			mbtnHelp.setCompoundDrawablesWithIntrinsicBounds( R.drawable.quickhelp, 0, 0, 0);
		} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL)	{
			mchkTurnOnFlash.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewCalc.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewRecog.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewPlot.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnHelp.setTextAppearance(this, android.R.attr.textAppearanceSmall);
			mbtnStartPreviewCalc.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start, 0, 0, 0);
			mbtnStartPreviewRecog.setCompoundDrawablesWithIntrinsicBounds( R.drawable.recognize, 0, 0, 0);
			mbtnStartPreviewPlot.setCompoundDrawablesWithIntrinsicBounds( R.drawable.startplot, 0, 0, 0);
			mbtnHelp.setCompoundDrawablesWithIntrinsicBounds( R.drawable.quickhelp, 0, 0, 0);
		} else if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
			mchkTurnOnFlash.setTextAppearance(this, android.R.attr.textAppearanceMedium);
			mbtnStartPreviewCalc.setTextAppearance(this, android.R.attr.textAppearanceMedium);
			mbtnStartPreviewRecog.setTextAppearance(this, android.R.attr.textAppearanceMedium);
			mbtnStartPreviewPlot.setTextAppearance(this, android.R.attr.textAppearanceMedium);
			mbtnHelp.setTextAppearance(this, android.R.attr.textAppearanceMedium);
			mbtnStartPreviewCalc.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start_large, 0, 0, 0);
			mbtnStartPreviewRecog.setCompoundDrawablesWithIntrinsicBounds( R.drawable.recognize_large, 0, 0, 0);
			mbtnStartPreviewPlot.setCompoundDrawablesWithIntrinsicBounds( R.drawable.startplot_large, 0, 0, 0);
			mbtnHelp.setCompoundDrawablesWithIntrinsicBounds( R.drawable.quickhelp_large, 0, 0, 0);
		} else {
			mchkTurnOnFlash.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			mbtnStartPreviewCalc.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			mbtnStartPreviewRecog.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			mbtnStartPreviewPlot.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			mbtnHelp.setTextAppearance(this, android.R.attr.textAppearanceLarge);
			mbtnStartPreviewCalc.setCompoundDrawablesWithIntrinsicBounds( R.drawable.start_xlarge, 0, 0, 0);
			mbtnStartPreviewRecog.setCompoundDrawablesWithIntrinsicBounds( R.drawable.recognize_xlarge, 0, 0, 0);
			mbtnStartPreviewPlot.setCompoundDrawablesWithIntrinsicBounds( R.drawable.startplot_xlarge, 0, 0, 0);
			mbtnHelp.setCompoundDrawablesWithIntrinsicBounds( R.drawable.quickhelp_xlarge, 0, 0, 0);
		}

		// TODO: this cannot handle a situation where rotation is allowed.
		mdlgProgress = ProgressDialog.show(this, getString(R.string.please_wait),
				getString(R.string.loading_img_proc_libs), true);
		final Handler handler = new Handler();
		mthreadLoad = new Thread(new Runnable()	{
			@Override
			public void run() {
				CameraPreview.preload(ActivityQuickRecog.this);
				// do not need to load libs coz it will be loaded in SmartCalc
		        handler.post(new Runnable() {
					@Override
					public void run() {
						mthreadLoad = null;
						if (mdlgProgress != null && mdlgProgress.isShowing()) {
							// after lock-unlock, dlgProgress may have disappeared, so check its visiblity first.
							mdlgProgress.dismiss();
Log.e("ActivityQuickRecog", "preload is done!");
						}
					}
		        });
			}
		});
		mthreadLoad.start();
		
		// set recognition mode has to be in onCreate function because the frame is loaded not only by clicking button.
        ExprRecognizer.setRecognitionMode(ExprRecognizer.RECOG_SPRINT_MODE);	// printed expressions mode.
		showTutorial(getString(R.string.photo_taken_tutorial), "photo_taken_tutorial");
    }

    @Override
    protected void onResume() {
Log.e("ActivityQuickRecog", "onResume");
        super.onResume();
        mCamera = CameraPreview.getCamera(this);
        if (mCamera == null) {
            Toast.makeText(getApplication(), getString(R.string.no_back_facing_camera), Toast.LENGTH_SHORT).show();
			setResult(Activity.RESULT_CANCELED);
            finish();
        	return;
        }

        // Open the default i.e. the first rear facing camera.
        try {
        	if (mcameraPreview == null) {
        		mcameraPreview = new CameraPreview(this);
        	}	// if mcameraPreview is not null, it implies Activity quick recog is waking up from onPause.
        	
        	mcameraPreview.setCamera(mCamera);
            // Create a RelativeLayout container that will hold a SurfaceView,
            // and set it as the content of our activity.
        	// have to do it in onStart because onCreate may not be called if phone is automatically
        	// locked and then unlocked by user.
            FrameLayout frameSurfaceView = (FrameLayout)findViewById(R.id.surfaceview_holder);
            frameSurfaceView.removeAllViews();
            frameSurfaceView.addView(mcameraPreview);
        } catch (Exception e){
            Toast.makeText(getApplication(), getString(R.string.fail_to_initialize_camera), Toast.LENGTH_SHORT).show();
			setResult(Activity.RESULT_CANCELED);
            finish();
        }

        // set flash
		if (!CameraPreview.msbSupportFlash) {
			mchkTurnOnFlash.setVisibility(View.GONE);
		} else {
			mchkTurnOnFlash.setChecked(false);
			mchkTurnOnFlash.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mCamera != null) {
						// turn on or turn off flash
						Parameters p = mCamera.getParameters();	// need not to test supported mode coz it has been tested in main activity.
						if (((CheckBox)v).isChecked()) {
							p.setFlashMode(Parameters.FLASH_MODE_TORCH);
						} else {
							p.setFlashMode(Parameters.FLASH_MODE_OFF);
						}
						CameraPreview.setCameraParams(mCamera, p);	// prevent any crash here.
					}
				}
				
			});
		}
    }

    @Override
    protected void onPause() {
Log.e("ActivityQuickRecog", "onPause");
        super.onPause();

        // Because the Camera object is a shared resource, it's very
        // important to release it when the activity is paused.
        try {
	        if (mCamera != null) {
	        	mcameraPreview.stopPreview();	// have to stop preview first.
	        	mcameraPreview.setCamera(null);
	            mCamera.release();
	            mCamera = null;
	        }
        } catch (Exception e){
        	mCamera = null;
        }
    }
    
    @Override
    protected void onStart() {
Log.e("ActivityQuickRecog", "OnStart");
    	super.onStart();
    }
    
    @Override
    protected void onStop() {
Log.e("ActivityQuickRecog", "OnStop");
		super.onStop();
    }
    
    @Override
    protected void onDestroy() {
Log.e("ActivityQuickRecog", "onDestroy");
		if (mthreadLoad != null) {
			if (mthreadLoad.isAlive())	{
				mthreadLoad.interrupt();
			}
			mthreadLoad = null;
		}
		if (mdlgProgress != null && mdlgProgress.isShowing()) {
			mdlgProgress.dismiss();
			mdlgProgress = null;
		}
		mcameraPreview.interruptRecogThread(true);
		
    	super.onDestroy();
    }
    
    // When an android device changes orientation usually the activity is destroyed and recreated with a new 
    // orientation layout. This method, along with a setting in the the manifest for this activity
    // tells the OS to let us handle it instead.
    //
    // This increases performance and gives us greater control over activity creation and destruction for simple 
    // activities. 
    // 
    // Must place this into the AndroidManifest.xml file for this activity in order for this to work properly 
    //   android:configChanges="keyboardHidden|orientation"
    //   optionally 
    //   android:screenOrientation="landscape"
    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);
    }
    
    public SelectRectView getSelectRectView() {
    	return mselectRectView;
    }

    public void takeAction(final int nAction) {
		if (CameraPreview.msnFocusMode != 0)	{	// if not support autofocus
			mcameraPreview.mbTakeSnapshot = true;
			mcameraPreview.mnActionAfterTakingSnapshot = nAction;
			Toast.makeText(ActivityQuickRecog.this, ActivityQuickRecog.this.getString(R.string.camera_preview_clicked), Toast.LENGTH_SHORT).show();
        } else if (mCamera != null) {
        	//mCamera.cancelAutoFocus();	// cancel existing auto focus. comment this line because cancel autofcus may hang
        	try {
		    	mCamera.autoFocus(new AutoFocusCallback() {
		            @Override
		            public void onAutoFocus(boolean success, Camera camera) {
		            	mcameraPreview.mbTakeSnapshot = true;
		    			mcameraPreview.mnActionAfterTakingSnapshot = nAction;
		                Toast.makeText(ActivityQuickRecog.this, ActivityQuickRecog.this.getString(R.string.camera_preview_clicked_and_autofocused),
		                        Toast.LENGTH_SHORT).show();
		            }
		        }); 
        	} catch(Exception e) {
                Toast.makeText(ActivityQuickRecog.this, ActivityQuickRecog.this.getString(R.string.camera_preview_fail_to_autofocus),
                        Toast.LENGTH_SHORT).show();
        	}
        }
    }
    
    public boolean isTurnOnFlashChecked() {
		if (mchkTurnOnFlash.getVisibility() == View.VISIBLE && mchkTurnOnFlash.isChecked()) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public void onStartPreview() {
    	if (mcameraPreview != null && mcameraPreview.isRecogThreadAlive() && mdlgProgress != null && mdlgProgress.isShowing()) {
        	mchkTurnOnFlash.setEnabled(false);
    		enableButtons(false);	// this means recognition thread is still running.
    	} else {
        	mchkTurnOnFlash.setEnabled(true);
    		enableButtons(true);	// recognition thread is not running. We can do some recognition, so enabl buttons.
    	}
    }
    
    public void onStopPreview() {
    	mchkTurnOnFlash.setEnabled(false);
    	enableButtons(false);
    }
    
    public void enableButtons(boolean bEnable) {
    	if (mbtnStartPreviewCalc != null) {
    		mbtnStartPreviewCalc.setEnabled(bEnable);
    	}
    	if (mbtnStartPreviewCalc != null) {
    		mbtnStartPreviewRecog.setEnabled(bEnable);
    	}
    	if (mbtnStartPreviewPlot != null) {
    		mbtnStartPreviewPlot.setEnabled(bEnable);
    	}
    	if (mbtnHelp != null) {
    		mbtnHelp.setEnabled(bEnable);
    	}
    }
    	
	@Override
	/*
	 * Create three menus: History, Settings and Help
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM0, 0, getString(R.string.menu_help));

		return true;
	}
	
	//Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
		menu.add(0, ITEM0, 0, getString(R.string.menu_help));
        return super.onPrepareOptionsMenu(menu);

    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM0:
		   	Intent intentHelp = new Intent(this, ActivityShowHelp.class);  
		   	Bundle bundle = new Bundle();
		   	bundle.putString("HELP_CONTENT", "recog_print");
		   	//Add this bundle to the intent
		   	intentHelp.putExtras(bundle);
		   	startActivity(intentHelp);
			break;

		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showTutorial(String strTitle, String strWhatTutorial)	{
		boolean bNotShown = false;
		SharedPreferences app_settings = getApplication().getSharedPreferences(ActivitySettings.SETTINGS, 0);
		if (app_settings != null) {
			bNotShown = app_settings.getBoolean("Tutorial_" + strWhatTutorial + "_not_show_again", false);
		}
		if (bNotShown) {
			return;
		}
	   	Intent intentTutorial = new Intent(this, ActivityTutorialBox.class);  
	   	Bundle bundle = new Bundle();
        Locale l = Locale.getDefault();  
	    String strLanguage = String.format("%s-%s", l.getLanguage(), l.getCountry());
	    String strIndexAddr;
	    if (strLanguage.equals("zh-CN") || strLanguage.equals("zh-SG"))	{
	    	strIndexAddr = "file:///android_asset/zh-CN/" + PHOTO_TAKEN_TUTORIAL_FILE_PATH + ".html";
	    } else if (strLanguage.equals("zh-TW") || strLanguage.equals("zh-HK"))	{
		    	strIndexAddr = "file:///android_asset/zh-TW/" + PHOTO_TAKEN_TUTORIAL_FILE_PATH + ".html";
	    } else	{
	    	strIndexAddr = "file:///android_asset/en/" + PHOTO_TAKEN_TUTORIAL_FILE_PATH + ".html";
	    	strLanguage = "en";
	    }
    	bundle.putString("Tutorial_File_Name", strIndexAddr);
    	bundle.putString("Tutorial_Title", getString(R.string.what_you_need_to_know_before_start_to_use));
    	bundle.putString("Which_Tutorial", strWhatTutorial);
	   	//Add this bundle to the intent
    	intentTutorial.putExtras(bundle);
	   	startActivity(intentTutorial);
	}

}
