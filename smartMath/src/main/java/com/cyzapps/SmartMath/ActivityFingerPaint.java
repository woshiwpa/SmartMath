package com.cyzapps.SmartMath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.SmartMath.InputPadMgrEx.InputKey;
import com.cyzapps.imgmatrixproc.ImgMatrixConverter;
import com.cyzapps.imgmatrixproc.ImgNoiseFilter;
import com.cyzapps.imgmatrixproc.ImgRectifier;
import com.cyzapps.imgmatrixproc.ImgThreshBiMgr;
import com.cyzapps.imgproc.ImageMgr;
import com.cyzapps.mathexprgen.SerMFPTranslator;
import com.cyzapps.mathexprgen.SerMFPTranslator.CurPos;
import com.cyzapps.mathexprgen.SerMFPTranslator.SerMFPTransFlags;
import com.cyzapps.mathrecog.CharLearningMgr;
import com.cyzapps.mathrecog.ExprRecognizer;
import com.cyzapps.mathrecog.ExprRecognizer.ExprRecognizeException;
import com.cyzapps.mathrecog.ImageChop;
import com.cyzapps.mathrecog.MisrecogWordMgr;
import com.cyzapps.mathrecog.StrokeFinder;
import com.cyzapps.mathrecog.StructExprRecog;
import com.cyzapps.mathrecog.UnitPrototypeMgr;
import com.cyzapps.mathrecog.UnitRecognizer;
import com.cyzapps.uptloadermgr.UPTJavaLoaderMgr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.MotionEvent;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ImageView.ScaleType;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityFingerPaint extends Activity {
    
	protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	protected static final String TAG = "RecogTest";
	protected static final String RECOG_FILE_FOLDER = "mathrecog";
	
	protected static final String SELECTED_TOUCH_MOVE_BUTTON = "Selected_Touch_Move_Button";
	
	protected static final long PROCESSED_IMAGE_MAX_AREA = 800 * 480;
	
	public static final String PROCESSED_IMAGE_SAVED_FILE_NAME = "mr_finallyproc.bmp";

	protected PaintImageView mpaintImgView;
    protected Paint mpaintPen;
    protected Paint mpaintErazer;
    protected ImageButton mimgBtnStart; 
    protected ImageButton mimgBtnCrop;
    protected ImageButton mimgBtnResize;
    protected ImageButton mimgBtnDelete; 
    protected ImageButton mimgBtnErazer;
    protected ImageButton mimgBtnPen;
    
    protected ImageButton mimgBtnRotateCW;	// rotate clock-wise
    protected ImageButton mimgBtnRotateACW;	// rotate anti-clock-wise
    protected ImageButton mimgBtnRedo;
    protected ImageButton mimgBtnUndo;
    protected ImageButton mimgBtnMove;
    protected ImageButton mimgBtnZoomFit;
    
    protected SeekBar mseekBarZoomRatio;
    protected TextView mtxtImageInfo;
    
    public String mstrZoomRatio = "100%", mstrCurrentPnt = "",/*"(0,0)",*/ mstrWidthHeight = "";//"0?0";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		//Remove title bar
        super.onCreate(savedInstanceState);
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        setContentView(R.layout.finger_paint);	// if we want to set full screen, setContentView should be after setting full screen calls.

        ImageButton imgBtnHelp = (ImageButton)findViewById(R.id.image_editor_help);
        imgBtnHelp.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
			   	Intent intentHelp = new Intent(ActivityFingerPaint.this, ActivityShowHelp.class);  
			   	Bundle bundle = new Bundle();
			   	bundle.putString("HELP_CONTENT", "handwriting_recognition");
			   	//Add this bundle to the intent
			   	intentHelp.putExtras(bundle);
			   	startActivity(intentHelp);
			}
        	
        });
        
        setButtons();
		mtxtImageInfo = (TextView)findViewById(R.id.image_state_info);
        
        mseekBarZoomRatio = (SeekBar)findViewById(R.id.image_zoom_ratio);
        mseekBarZoomRatio.setOnSeekBarChangeListener(new OnSeekBarChangeListener()	{

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				double dZoomRatio = (progress == 0)?0.5:progress;
				int nStrokeWidth = (int)(mseekBarZoomRatio.getMax()/dZoomRatio);
				if (nStrokeWidth < 1)	{
					nStrokeWidth = 1;
				}
				mpaintPen.setStrokeWidth(nStrokeWidth);
				mpaintErazer.setStrokeWidth(nStrokeWidth);
				if (fromUser)	{
					mpaintImgView.zoom(dZoomRatio);
				}
				mstrZoomRatio = (int)(dZoomRatio * 100) + "%";
				setImageInfoText();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        mpaintPen = new Paint();
        mpaintPen.setAntiAlias(false);
        mpaintPen.setDither(false);
        mpaintPen.setFilterBitmap(false);
        mpaintPen.setColor(0xFF000000);
        mpaintPen.setStyle(Paint.Style.STROKE);
        mpaintPen.setStrokeJoin(Paint.Join.ROUND);
        mpaintPen.setStrokeCap(Paint.Cap.ROUND);       
        mpaintPen.setStrokeWidth(mseekBarZoomRatio.getMax());
        
        mpaintErazer = new Paint();
        mpaintErazer.setAntiAlias(false);
        mpaintErazer.setDither(false);
        mpaintErazer.setFilterBitmap(false);
        mpaintErazer.setColor(0xFFFFFFFF);
        mpaintErazer.setStyle(Paint.Style.STROKE);
        mpaintErazer.setStrokeJoin(Paint.Join.ROUND);
        mpaintErazer.setStrokeCap(Paint.Cap.ROUND);
        mpaintErazer.setStrokeWidth(mseekBarZoomRatio.getMax());
        
        // initialize mpaintImgView last.
        mpaintImgView = (PaintImageView)findViewById(R.id.view_image);
		mpaintImgView.setOnTouchListener(new OnTouchListener(){

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mpaintImgView.onTouchEvent(event);
				return false;
			}
			
		});
		mpaintImgView.mdZoomRatio = setImageViewZoomRatio(1);	// initial mdZoomRatio must be 1 because initial bitmapActual is 2/3 whole screen size.
		
		// TODO: this cannot handle a situation where rotation is allowed.
		final ProgressDialog dlgProgress = ProgressDialog.show(this, getString(R.string.please_wait),
				getString(R.string.loading_img_proc_libs), true);
		final Handler handler = new Handler();
		Thread threadLoad = new Thread(new Runnable()	{
			@Override
			public void run() {
				Bitmap image_smoothed = null;
				Bundle bundle = getIntent().getExtras();
				if (bundle != null)	{
					// read saved image only if we click the finger paint button in smart calculator
					// do not use bAfterPhotoTaken directly here.
					image_smoothed = ImageMgr.readImg(null, MFPFileManagerActivity.getAppFolderFullPath()
															+ File.separator + PROCESSED_IMAGE_SAVED_FILE_NAME);
				}
				final Bitmap image_smoothed_ref = image_smoothed;
		        preload();
		        handler.post(new Runnable() {
					@Override
					public void run() {
						dlgProgress.dismiss();
						mpaintImgView.mnTouchingMode = 2;
						selectTouchMoveBtn(2, false);
						if (image_smoothed_ref != null)	{
							// everything is done successfully.
							mpaintImgView.setBitmap(image_smoothed_ref);
							performUndoRedo(0);
					        Toast.makeText(ActivityFingerPaint.this, getString(R.string.select_expr_to_recognize), Toast.LENGTH_SHORT).show();
						}
						String strWarningMsg, strWarningMsgBoxId;
						if (ExprRecognizer.getRecognitionMode() == 0) {
							strWarningMsg = getString(R.string.print_recog_notice);
							strWarningMsgBoxId = "Print_Recog_Notice";
						} else {
							strWarningMsg = getString(R.string.handwriting_recog_notice);
							strWarningMsgBoxId = "Handwriting_Recog_Notice";
						}
						showMsgBoxMaybeOnce(strWarningMsg, getString(R.string.warning), strWarningMsgBoxId, true);
					}
		        });
			}
		});
		threadLoad.start();
		// set recognition mode has to be in onCreate function because the frame is loaded not only by clicking button.
		ExprRecognizer.setRecognitionMode(ExprRecognizer.RECOG_SHANDWRITING_MODE);	// handwriting mode.
    }
    
    public void setButtons()	{
    	mimgBtnStart = new ImageButton(this);
    	mimgBtnStart.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnStart.setAdjustViewBounds(true);
    	
    	mimgBtnResize = new ImageButton(this);
    	mimgBtnResize.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnResize.setAdjustViewBounds(true);
    	
    	mimgBtnCrop = new ImageButton(this);
    	mimgBtnCrop.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnCrop.setAdjustViewBounds(true);
    	
    	mimgBtnDelete = new ImageButton(this);
    	mimgBtnDelete.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnDelete.setAdjustViewBounds(true);
    	
    	mimgBtnErazer = new ImageButton(this);
    	mimgBtnErazer.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnErazer.setAdjustViewBounds(true);
    	
    	mimgBtnPen = new ImageButton(this);
    	mimgBtnPen.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnPen.setAdjustViewBounds(true);
    	

    	mimgBtnRotateCW = new ImageButton(this);
    	mimgBtnRotateCW.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnRotateCW.setAdjustViewBounds(true);
    	
    	mimgBtnRotateACW = new ImageButton(this);
    	mimgBtnRotateACW.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnRotateACW.setAdjustViewBounds(true);
    	
    	mimgBtnRedo = new ImageButton(this);
    	mimgBtnRedo.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnRedo.setAdjustViewBounds(true);
    	
    	mimgBtnUndo = new ImageButton(this);
    	mimgBtnUndo.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnUndo.setAdjustViewBounds(true);
    	
    	mimgBtnMove = new ImageButton(this);
    	mimgBtnMove.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnMove.setAdjustViewBounds(true);
    	
    	mimgBtnZoomFit = new ImageButton(this);
    	mimgBtnZoomFit.setScaleType(ScaleType.FIT_CENTER);
    	mimgBtnZoomFit.setAdjustViewBounds(true);
    	    	
		Display display = getWindowManager().getDefaultDisplay(); 
    	int width = display.getWidth();  // deprecated
    	int height = display.getHeight();  // deprecated
    	DisplayMetrics metrics = new DisplayMetrics();
    	display.getMetrics(metrics);
    	double density = metrics.density;
    	int nNumOfBtnsIn1Column = 12;
    	int nScreenSizeCategory = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
    	if (nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_SMALL
    			|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_NORMAL
    			|| nScreenSizeCategory == Configuration.SCREENLAYOUT_SIZE_LARGE)	{
    		nNumOfBtnsIn1Column = 6;
    	}
    	double dOneBtnHeight = Math.min(width, height) / (double)nNumOfBtnsIn1Column;
    	// have to select different icon sizes because the width of the button is determined by icon size.
    	// not 64 / density. It seems that, when a image (64 * 64) is shown in android, its real size in screen
    	// is (64 * density) * (64*density)
		if (dOneBtnHeight <= 64 * density)	{
			mimgBtnStart.setImageResource(R.drawable.mr_start_32);
			mimgBtnResize.setImageResource(R.drawable.mr_resize_32);
			mimgBtnCrop.setImageResource(R.drawable.mr_crop_select_32);
			mimgBtnDelete.setImageResource(R.drawable.mr_delete_32);
			mimgBtnErazer.setImageResource(R.drawable.mr_erazer_32);
			mimgBtnPen.setImageResource(R.drawable.mr_pen_32);
			mimgBtnRotateCW.setImageResource(R.drawable.mr_rotate_cw_32);
			mimgBtnRotateACW.setImageResource(R.drawable.mr_rotate_anticw_32);
			mimgBtnRedo.setImageResource(R.drawable.mr_redo_32);
			mimgBtnUndo.setImageResource(R.drawable.mr_undo_32);
			mimgBtnMove.setImageResource(R.drawable.mr_move_32);
			mimgBtnZoomFit.setImageResource(R.drawable.mr_zoom_fit_32);
		} else if (dOneBtnHeight <= 96 * density)	{
			mimgBtnStart.setImageResource(R.drawable.mr_start_48);
			mimgBtnResize.setImageResource(R.drawable.mr_resize_48);
			mimgBtnCrop.setImageResource(R.drawable.mr_crop_select_48);
			mimgBtnDelete.setImageResource(R.drawable.mr_delete_48);
			mimgBtnErazer.setImageResource(R.drawable.mr_erazer_48);
			mimgBtnPen.setImageResource(R.drawable.mr_pen_48);
			mimgBtnRotateCW.setImageResource(R.drawable.mr_rotate_cw_48);
			mimgBtnRotateACW.setImageResource(R.drawable.mr_rotate_anticw_48);
			mimgBtnRedo.setImageResource(R.drawable.mr_redo_48);
			mimgBtnUndo.setImageResource(R.drawable.mr_undo_48);
			mimgBtnMove.setImageResource(R.drawable.mr_move_48);
			mimgBtnZoomFit.setImageResource(R.drawable.mr_zoom_fit_48);
		} else if (dOneBtnHeight <= 128 * density)	{
			mimgBtnStart.setImageResource(R.drawable.mr_start_64);
			mimgBtnCrop.setImageResource(R.drawable.mr_crop_select_64);
			mimgBtnResize.setImageResource(R.drawable.mr_resize_64);
			mimgBtnDelete.setImageResource(R.drawable.mr_delete_64);
			mimgBtnErazer.setImageResource(R.drawable.mr_erazer_64);
			mimgBtnPen.setImageResource(R.drawable.mr_pen_64);
			mimgBtnRotateCW.setImageResource(R.drawable.mr_rotate_cw_64);
			mimgBtnRotateACW.setImageResource(R.drawable.mr_rotate_anticw_64);
			mimgBtnRedo.setImageResource(R.drawable.mr_redo_64);
			mimgBtnUndo.setImageResource(R.drawable.mr_undo_64);
			mimgBtnMove.setImageResource(R.drawable.mr_move_64);
			mimgBtnZoomFit.setImageResource(R.drawable.mr_zoom_fit_64);
		} else if (dOneBtnHeight <= 192 * density)	{
			mimgBtnStart.setImageResource(R.drawable.mr_start_96);
			mimgBtnCrop.setImageResource(R.drawable.mr_crop_select_96);
			mimgBtnResize.setImageResource(R.drawable.mr_resize_96);
			mimgBtnDelete.setImageResource(R.drawable.mr_delete_96);
			mimgBtnErazer.setImageResource(R.drawable.mr_erazer_96);
			mimgBtnPen.setImageResource(R.drawable.mr_pen_96);
			mimgBtnRotateCW.setImageResource(R.drawable.mr_rotate_cw_96);
			mimgBtnRotateACW.setImageResource(R.drawable.mr_rotate_anticw_96);
			mimgBtnRedo.setImageResource(R.drawable.mr_redo_96);
			mimgBtnUndo.setImageResource(R.drawable.mr_undo_96);
			mimgBtnMove.setImageResource(R.drawable.mr_move_96);
			mimgBtnZoomFit.setImageResource(R.drawable.mr_zoom_fit_96);
		} else	{
			mimgBtnStart.setImageResource(R.drawable.mr_start_128);
			mimgBtnCrop.setImageResource(R.drawable.mr_crop_select_128);
			mimgBtnResize.setImageResource(R.drawable.mr_resize_128);
			mimgBtnDelete.setImageResource(R.drawable.mr_delete_128);
			mimgBtnErazer.setImageResource(R.drawable.mr_erazer_128);
			mimgBtnPen.setImageResource(R.drawable.mr_pen_128);
			mimgBtnRotateCW.setImageResource(R.drawable.mr_rotate_cw_128);
			mimgBtnRotateACW.setImageResource(R.drawable.mr_rotate_anticw_128);
			mimgBtnRedo.setImageResource(R.drawable.mr_redo_128);
			mimgBtnUndo.setImageResource(R.drawable.mr_undo_128);
			mimgBtnMove.setImageResource(R.drawable.mr_move_128);
			mimgBtnZoomFit.setImageResource(R.drawable.mr_zoom_fit_128);
		}
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        mimgBtnStart.setLayoutParams(lp);
        mimgBtnResize.setLayoutParams(lp);
        mimgBtnCrop.setLayoutParams(lp);
        mimgBtnDelete.setLayoutParams(lp);
        mimgBtnErazer.setLayoutParams(lp);
        mimgBtnPen.setLayoutParams(lp);
        mimgBtnRotateCW.setLayoutParams(lp);
        mimgBtnRotateACW.setLayoutParams(lp);
        mimgBtnRedo.setLayoutParams(lp);
        mimgBtnUndo.setLayoutParams(lp);
        mimgBtnMove.setLayoutParams(lp);
        mimgBtnZoomFit.setLayoutParams(lp);
        
		
		LinearLayout layoutBtn1 = (LinearLayout) findViewById(R.id.buttons_layout1);
		layoutBtn1.removeAllViews();
		LinearLayout layoutBtn2 = (LinearLayout)((nNumOfBtnsIn1Column < 12)?findViewById(R.id.buttons_layout2):findViewById(R.id.buttons_layout1));
		layoutBtn2.removeAllViews();
        layoutBtn2.addView(mimgBtnStart);
		layoutBtn1.addView(mimgBtnResize);
        layoutBtn2.addView(mimgBtnDelete);
        layoutBtn1.addView(mimgBtnCrop);
        layoutBtn2.addView(mimgBtnErazer);
        layoutBtn1.addView(mimgBtnPen);
        layoutBtn2.addView(mimgBtnRotateACW);
        layoutBtn1.addView(mimgBtnRotateCW);
        layoutBtn2.addView(mimgBtnRedo);
        layoutBtn1.addView(mimgBtnUndo);
        layoutBtn2.addView(mimgBtnZoomFit);
        layoutBtn1.addView(mimgBtnMove);
        
        mimgBtnStart.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				//... start to recog here ...
				final ProgressDialog dlgProgress = ProgressDialog.show(ActivityFingerPaint.this, getString(R.string.please_wait),
																		getString(R.string.recognizing_math_expressions), true);

				final Handler handler = new Handler();
				Thread threadRecognize = new Thread(new Runnable()	{

					@Override
					public void run() {
						String strResult = "";
						String strExceptionReason = "";
						try	{
							strResult = recognize();
						} catch(Exception e)	{
							strExceptionReason = e.getMessage(); // not that e.getMessage can return null.
							if (strExceptionReason == null) {
								strExceptionReason = "";
							}
							strResult = "\\exception";
						}					
						if (strResult == null || strResult.length() == 0 || strResult.equals("\\unknown") || strResult.equals("\\empty"))	{
				        	// error occurs
							handler.post(new Runnable()	{
								@Override
								public void run() {
									dlgProgress.dismiss();
									showMsgBox(getString(R.string.expression_might_not_valid), getString(R.string.error));
								}
							});
						} else if (strResult.equals("\\exception"))	{
				        	// error occurs
							final String strErrorMsg = (strExceptionReason.compareTo(ExprRecognizer.TOO_DEEP_CALL_STACK) == 0)
														?getString(R.string.insufficient_memory_to_recognize_complicated_expr)
																:getString(R.string.recognizing_error);
							handler.post(new Runnable()	{
								@Override
								public void run() {
									dlgProgress.dismiss();
									showMsgBox(strErrorMsg, getString(R.string.error));
								}
							});
						} else	{
							final String strResultRef = strResult;
							handler.post(new Runnable()	{
								@Override
								public void run() {
				            		if (mpaintImgView.mbitmapActual != null)	{
				            			// now recognition is done successfully, save the file
				            			ImageMgr.saveImg(mpaintImgView.mbitmapActual,
				            							MFPFileManagerActivity.getAppFolderFullPath()
				            							+ File.separator + PROCESSED_IMAGE_SAVED_FILE_NAME);
				            		}
									dlgProgress.dismiss();
				            		Bundle b = new Bundle();
				            		b.putString("RecognizingResult", strResultRef);
				            		Intent intent = new Intent();
				            		intent.putExtras(b);
				            		setResult(Activity.RESULT_OK, intent);
				            		finish();
								}
							});
						}
					}
				});
				threadRecognize.start();
			}
        });
        
        mimgBtnResize.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				LayoutInflater factory = LayoutInflater.from(ActivityFingerPaint.this);
		        View vSetPaintSize = factory.inflate( R.layout.set_paint_size, null);
				final EditText edtWidth = (EditText)vSetPaintSize.findViewById(R.id.edtTxtImageWidth);
				final EditText edtHeight = (EditText)vSetPaintSize.findViewById(R.id.edtTxtImageHeight);
				final TextView tvErrorMsg = (TextView)vSetPaintSize.findViewById(R.id.txtViewErrorMessage);
				
				final AlertDialog alertDialogResize = new AlertDialog.Builder(ActivityFingerPaint.this)
							.setTitle(R.string.reset_image_size)
							.setView(vSetPaintSize)
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									int nCurrentWidth = mpaintImgView.getActualBitmapWidth();
									int nCurrentHeight = mpaintImgView.getActualBitmapHeight();
									int nNewWidth = nCurrentWidth, nNewHeight = nCurrentHeight;
									try	{
										nNewWidth = Integer.parseInt(edtWidth.getText().toString());
									} catch (NumberFormatException e)	{
										nNewWidth = nCurrentWidth;
									}
									try	{
										nNewHeight = Integer.parseInt(edtHeight.getText().toString());
									} catch (NumberFormatException e)	{
										nNewHeight = nCurrentHeight;
									}
									mpaintImgView.resizeBitmap(nNewWidth, nNewHeight);
								}
								
							}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// Do nothing if press cancel.							
								}
							}).create();
				
				edtWidth.addTextChangedListener(new TextWatcher(){

					@Override
					public void afterTextChanged(Editable s) {
						int nCurrentWidth = mpaintImgView.getActualBitmapWidth();
						int nCurrentHeight = mpaintImgView.getActualBitmapHeight();
						int nNewWidth = nCurrentWidth, nNewHeight = nCurrentHeight;
						try	{
							nNewWidth = Integer.parseInt(s.toString());
						} catch (NumberFormatException e)	{
							nNewWidth = nCurrentWidth;
						}
						try	{
							nNewHeight = Integer.parseInt(edtHeight.getText().toString());
						} catch (NumberFormatException e)	{
							nNewHeight = nCurrentHeight;
						}
						
						if ((nNewWidth * nNewHeight) <= 0) {
							alertDialogResize.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							tvErrorMsg.setText(getString(R.string.image_size_cannot_be_zero));
						} else if ((nNewWidth * nNewHeight) > (CameraPreview.PROCESSED_IMAGE_MAX_AREA * 4))	{
							alertDialogResize.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							tvErrorMsg.setText(getString(R.string.image_is_too_large));
						} else	{
							alertDialogResize.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
							tvErrorMsg.setText("");
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
				edtHeight.addTextChangedListener(new TextWatcher(){

					@Override
					public void afterTextChanged(Editable s) {
						int nCurrentWidth = mpaintImgView.getActualBitmapWidth();
						int nCurrentHeight = mpaintImgView.getActualBitmapHeight();
						int nNewWidth = nCurrentWidth, nNewHeight = nCurrentHeight;
						try	{
							nNewWidth = Integer.parseInt(edtHeight.getText().toString());
						} catch (NumberFormatException e)	{
							nNewWidth = nCurrentWidth;
						}
						try	{
							nNewHeight = Integer.parseInt(s.toString());
						} catch (NumberFormatException e)	{
							nNewHeight = nCurrentHeight;
						}
						
						if ((nNewWidth * nNewHeight) <= 0) {
							alertDialogResize.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							tvErrorMsg.setText(getString(R.string.image_size_cannot_be_zero));
						} else if ((nNewWidth * nNewHeight) > (CameraPreview.PROCESSED_IMAGE_MAX_AREA * 4))	{
							alertDialogResize.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
							tvErrorMsg.setText(getString(R.string.image_is_too_large));
						} else	{
							alertDialogResize.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
							tvErrorMsg.setText("");
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
				alertDialogResize.show();				
				// text change has to be after dialog shown otherwise positive button is null.
				edtWidth.setText(Integer.toString(mpaintImgView.getActualBitmapWidth()));
				edtHeight.setText(Integer.toString(mpaintImgView.getActualBitmapHeight()));
			}
        	
        });
        
        
        mimgBtnCrop.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				selectTouchMoveBtn(2, true);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnDelete.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mpaintImgView.deleteCroppedArea();
				clearImageInfoPositionText();
				
			}
        	
        });
        
        mimgBtnPen.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				selectTouchMoveBtn(1, true);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnErazer.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				selectTouchMoveBtn(0, true);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnRotateCW.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mpaintImgView.rotateImage(-90);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnRotateCW.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(ActivityFingerPaint.this);
				final View viewRotateDegree = inflater.inflate(R.layout.rotate_setting, null);
				RadioGroup radioGrp = (RadioGroup) viewRotateDegree.findViewById(R.id.radioGrp_rotate_degrees);
				radioGrp.setOnCheckedChangeListener(new OnCheckedChangeListener()	{

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						LinearLayout layoutRotateDegrees = (LinearLayout) viewRotateDegree.findViewById(R.id.layout_enter_rotate_degrees);
						if (group.getCheckedRadioButtonId() == R.id.radioBtn_rotate_any_degrees)	{
							layoutRotateDegrees.setVisibility(View.VISIBLE);
						} else	{
							layoutRotateDegrees.setVisibility(View.GONE);
						}
					}
					
				});
				
				AlertDialog.Builder blder = new AlertDialog.Builder(ActivityFingerPaint.this);
				AlertDialog alertDialog
					= blder.setIcon(R.drawable.icon)
						.setTitle(getString(R.string.rotate_clockwise))
						.setView(viewRotateDegree)
						.setPositiveButton(R.string.ok, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									RadioGroup radioGrp = (RadioGroup) viewRotateDegree.findViewById(R.id.radioGrp_rotate_degrees);
									if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_rotate_90_degrees) {
										mpaintImgView.rotateImage(-90);
										clearImageInfoPositionText();
									} else if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_rotate_180_degrees) {
										mpaintImgView.rotateImage(-180);
										clearImageInfoPositionText();
									} else if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_rotate_270_degrees) {
										mpaintImgView.rotateImage(-270);
										clearImageInfoPositionText();
									} else	{
										EditText edtRotateDegree = (EditText) viewRotateDegree.findViewById(R.id.edttxt_rotate_degrees);
										String strRotateDegree = edtRotateDegree.getText().toString();
										int nRotateDegree = 0;
										try	{
											nRotateDegree = Integer.parseInt(strRotateDegree);
										} catch (NumberFormatException e)	{
											
										}
										if (nRotateDegree != 0)	{
											mpaintImgView.rotateImage(-nRotateDegree);
											clearImageInfoPositionText();
										}
									}
								}
							})
						.setNegativeButton(R.string.cancel, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									return;
								}
							})
						.create();
				alertDialog.show();			

				return false;
			}
        	
        });
        
        mimgBtnRotateACW.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mpaintImgView.rotateImage(90);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnRotateACW.setOnLongClickListener(new OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				LayoutInflater inflater = LayoutInflater.from(ActivityFingerPaint.this);
				final View viewRotateDegree = inflater.inflate(R.layout.rotate_setting, null);
				RadioGroup radioGrp = (RadioGroup) viewRotateDegree.findViewById(R.id.radioGrp_rotate_degrees);
				radioGrp.setOnCheckedChangeListener(new OnCheckedChangeListener()	{

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						LinearLayout layoutRotateDegrees = (LinearLayout) viewRotateDegree.findViewById(R.id.layout_enter_rotate_degrees);
						if (group.getCheckedRadioButtonId() == R.id.radioBtn_rotate_any_degrees)	{
							layoutRotateDegrees.setVisibility(View.VISIBLE);
						} else	{
							layoutRotateDegrees.setVisibility(View.GONE);
						}
					}
					
				});

				AlertDialog.Builder blder = new AlertDialog.Builder(ActivityFingerPaint.this);
				AlertDialog alertDialog
					= blder.setIcon(R.drawable.icon)
						.setTitle(getString(R.string.rotate_anti_clockwise))
						.setView(viewRotateDegree)
						.setPositiveButton(R.string.ok, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									RadioGroup radioGrp = (RadioGroup) viewRotateDegree.findViewById(R.id.radioGrp_rotate_degrees);
									if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_rotate_90_degrees) {
										mpaintImgView.rotateImage(90);
										clearImageInfoPositionText();
									} else if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_rotate_180_degrees) {
										mpaintImgView.rotateImage(180);
										clearImageInfoPositionText();
									} else if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_rotate_270_degrees) {
										mpaintImgView.rotateImage(270);
										clearImageInfoPositionText();
									} else	{
										EditText edtRotateDegree = (EditText) viewRotateDegree.findViewById(R.id.edttxt_rotate_degrees);
										String strRotateDegree = edtRotateDegree.getText().toString();
										int nRotateDegree = 0;
										try	{
											nRotateDegree = Integer.parseInt(strRotateDegree);
										} catch (NumberFormatException e)	{
											
										}
										if (nRotateDegree != 0)	{
											mpaintImgView.rotateImage(nRotateDegree);
											clearImageInfoPositionText();
										}
									}
								}
							})
						.setNegativeButton(R.string.cancel, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									return;
								}
							})
						.create();
				alertDialog.show();			

				return false;
			}
        	
        });
        
        mimgBtnUndo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				performUndoRedo(1);
				clearImageInfoPositionText();
			}
        	
        });

        mimgBtnRedo.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				performUndoRedo(2);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnMove.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				selectTouchMoveBtn(3, true);
				clearImageInfoPositionText();
			}
        	
        });
        
        mimgBtnZoomFit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				mpaintImgView.zoomFit();
			}
        	
        });
    }
    
    public void performUndoRedo(int nAction)	{
    	// nAction is 0 means do nothing, at this moment no paint action taken so undo/redo buttons should be disabled.
    	// nAction is 1 means undo, and redo button is enabled while undo button is disabled
    	// nAction is 2 means redo, and redo button is disabled while undo button is enabled
    	if (nAction == 0)	{
    		mimgBtnUndo.setEnabled(false);
			mimgBtnRedo.setEnabled(false);
    	} else if (nAction == 1)	{
			mpaintImgView.unDo();
			mimgBtnUndo.setEnabled(false);
			mimgBtnRedo.setEnabled(true);
    	} else if (nAction == 2)	{
			mpaintImgView.reDo();
			mimgBtnUndo.setEnabled(true);
			mimgBtnRedo.setEnabled(false);
    	}
    }
    
    // set seekBar and returns the actual zooming ratio.
    public double setImageViewZoomRatio(double dOriginalSetting)	{
    	double dReturn = dOriginalSetting;
    	if (dReturn > mseekBarZoomRatio.getMax())	{
    		dReturn = mseekBarZoomRatio.getMax();
    		mseekBarZoomRatio.setProgress(mseekBarZoomRatio.getMax());
    	} else if (dReturn < 0.75)	{
    		dReturn = 0.5;
    		mseekBarZoomRatio.setProgress(0);
    	} else {
    		dReturn = Math.round(dReturn);
    		mseekBarZoomRatio.setProgress((int)dReturn);
    	}
    	return dReturn;
    }
    
    public void onImageChanged()	{
		mimgBtnUndo.setEnabled(true);
		mimgBtnRedo.setEnabled(false);
    }

    public void selectTouchMoveBtn(int nTouchMoveBtn, boolean bFromUser)	{
    	// nPaint is 0 means erazor, 1 means pen and 2 means crop select
    	if (nTouchMoveBtn == 0)	{
    		mpaintImgView.mpaint = mpaintErazer;
        	if (bFromUser)	{
        		mpaintImgView.setTouchMode(nTouchMoveBtn);
        	}
    		mimgBtnErazer.setEnabled(false);
    		mimgBtnPen.setEnabled(true);
    		mimgBtnCrop.setEnabled(true);
    		mimgBtnMove.setEnabled(true);
    	} else if (nTouchMoveBtn == 1)	{
    		mpaintImgView.mpaint = mpaintPen;
        	if (bFromUser)	{
        		mpaintImgView.setTouchMode(nTouchMoveBtn);
        	}
    		mimgBtnErazer.setEnabled(true);
    		mimgBtnPen.setEnabled(false);
    		mimgBtnCrop.setEnabled(true);
    		mimgBtnMove.setEnabled(true);
    	} else if (nTouchMoveBtn == 2)	{
        	if (bFromUser)	{
        		mpaintImgView.setTouchMode(nTouchMoveBtn);
        	}
    		mimgBtnErazer.setEnabled(true);
    		mimgBtnPen.setEnabled(true);
    		mimgBtnCrop.setEnabled(false);
    		mimgBtnMove.setEnabled(true);
    	} else if (nTouchMoveBtn == 3)	{
        	if (bFromUser)	{
        		mpaintImgView.setTouchMode(nTouchMoveBtn);
        	}
    		mimgBtnErazer.setEnabled(true);
    		mimgBtnPen.setEnabled(true);
    		mimgBtnCrop.setEnabled(true);
    		mimgBtnMove.setEnabled(false);
    	}
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			boolean bImgCaptured = false;
	        if (resultCode == RESULT_OK) {
	        	//Intent Works

	 	       //First Extract the bundle from intent
	            Bundle bundle = intent.getExtras();
	            if (bundle != null && bundle.getInt("ImageDataLength") > 0)	{
		        	int nImageDataLength = bundle.getInt("ImageDataLength");
	            	preprocessImageAsync("MathImg.dat", nImageDataLength);
	            	bImgCaptured = true;
	            }
			}
		    if (bImgCaptured == false)	{
		    	showMsgBox(getString(R.string.fail_to_take_picture), getString(R.string.error));
		    }
	    }
	
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState)	{
		mpaintImgView.onSaveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle inState) {
		//Restore last state
		super.onRestoreInstanceState(inState);
		
		mpaintImgView.onRestoreInstanceState(inState);
	}
	
	public void setImageInfoText()	{
		// too slow if set frequently.
		mtxtImageInfo.setText(" " + mstrZoomRatio + " " + mstrCurrentPnt + " " + mstrWidthHeight);
	}
	
	public void clearImageInfoPositionText()	{
        mstrCurrentPnt = "";
        mstrWidthHeight = "";
        setImageInfoText();
	}
	
	public void preload()	{
		AssetManager am = getAssets();
		if (UnitRecognizer.msUPTMgr == null)	{
			UnitRecognizer.msUPTMgr = new UnitPrototypeMgr();
		}
		if (UnitRecognizer.msUPTMgr.isEmpty())	{	// if it is not empty, it's must be assigned print or handwriting. so no need to load again.
			UPTJavaLoaderMgr.load(false, true, true);
		}

        InputStream is = null;
        if (CameraPreview.msCLM.isEmpty()) {
	        try {
				is = am.open(RECOG_FILE_FOLDER + File.separator + "clm.xml");
	            CameraPreview.msCLM.readFromXML(is);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
        }

        if (CameraPreview.msMWM.isEmpty()) {
	        try {
				is = am.open(RECOG_FILE_FOLDER + File.separator + "mwm.xml");
				CameraPreview.msMWM.readFromXML(is);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
        }
	}
	
	public Bitmap preprocessImageSync(String strImgDataFile, int nImgDataLength)  throws InterruptedException	{
		byte[] buffer = null;
		boolean bSuccessful = true;
		try {
			FileInputStream inputStream = openFileInput(strImgDataFile);
			buffer = new byte[nImgDataLength];
			inputStream.read(buffer);
			inputStream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			bSuccessful = false;
		} catch (IOException e2)	{
			e2.printStackTrace();
			bSuccessful = false;
		}
		
		if (bSuccessful)	{
	    	BitmapFactory.Options opts = new BitmapFactory.Options();
	    	int nPixelDiv = 100;
	    	
	    	opts.inPurgeable = true; // inPurgeable is used to free up memory while required
	    	Bitmap image_initial= BitmapFactory.decodeByteArray(buffer, 0, buffer.length,opts);
	    	long nImageOriginalWidth = image_initial.getWidth();
	    	long nImageOriginalHeight = image_initial.getHeight();
	    	double dScalingRatio = Math.sqrt((double)(nImageOriginalWidth * nImageOriginalHeight)
	    									/ (double)(PROCESSED_IMAGE_MAX_AREA));
	    	int nImageWidth = (int)Math.floor(nImageOriginalWidth / dScalingRatio);
	    	int nImageHeight = (int)Math.floor(nImageOriginalHeight / dScalingRatio);
	    	image_initial = Bitmap.createScaledBitmap(image_initial, nImageWidth, nImageHeight, false);
	    	ImageMgr.saveImg(image_initial, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + CameraPreview.INITIAL_BMP_FILE_NAME);	
	    	
	        int[][] colorMatrix = ImageMgr.convertImg2ColorMatrix(image_initial);
	        int[][] grayMatrix = ImgMatrixConverter.convertColor2Gray(colorMatrix);
	        Bitmap image_grayed = ImageMgr.convertGrayMatrix2Img(grayMatrix);
	        ImageMgr.saveImg(image_grayed, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + "mr_grayed.bmp");
	        
	        grayMatrix = ImgNoiseFilter.filterNoiseNbAvg4Gray(grayMatrix, 1);
	        Bitmap image_filtered = ImageMgr.convertGrayMatrix2Img(grayMatrix);
	        ImageMgr.saveImg(image_filtered, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + "mr_filtered.bmp");
	        
	        int nWHMax = Math.max(grayMatrix.length, grayMatrix[0].length);
	        int nEstimatedStrokeWidth = (int)Math.ceil((double)nWHMax/(double)nPixelDiv);
	        double dAvgRadius = (int)Math.max(3.0, nEstimatedStrokeWidth /2.0);
	        byte[][] biMatrix = ImgThreshBiMgr.convertGray2Bi2ndD(grayMatrix, (int)dAvgRadius);
	        Bitmap image_bilized = ImageMgr.convertBiMatrix2Img(biMatrix);
	        ImageMgr.saveImg(image_bilized, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + "mr_bilized.bmp");
	        
	        ImageChop imgChop = new ImageChop();
	        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
	        double dAvgStrokeWidth = imgChop.calcAvgStrokeWidth();
	        int nFilterR = (int)Math.ceil((dAvgStrokeWidth/2.0 - 1)/2.0);
	        biMatrix = ImgNoiseFilter.filterNoiseNbAvg4Bi(biMatrix, nFilterR, 1);
	        biMatrix = ImgNoiseFilter.filterNoiseNbAvg4Bi(biMatrix, nFilterR, 2);
	        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
	        Bitmap image_smoothed1 = ImageMgr.convertBiMatrix2Img(imgChop.mbarrayImg);
	        ImageMgr.saveImg(image_smoothed1, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + "mr_smoothed1.bmp");
	        
	        biMatrix = ImgNoiseFilter.filterNoisePoints4Bi(biMatrix, (int)dAvgStrokeWidth);
	        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
	        Bitmap image_smoothed2 = ImageMgr.convertBiMatrix2Img(imgChop.mbarrayImg);
	        ImageMgr.saveImg(image_smoothed2, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + "mr_smoothed2.bmp");
	        
	        image_initial.recycle();
	        image_grayed.recycle();
	        image_filtered.recycle();
	        image_bilized.recycle();
	        image_smoothed1.recycle();
	        //image_smoothed2.recycle();
	        return image_smoothed2;
		} else	{
			return null;
		}
	}
	
	public void preprocessImageAsync(final String strImgDataFile, final int nImgDataLength)	{
		final ProgressDialog dlgProgress = ProgressDialog.show(this, getString(R.string.please_wait),
				getString(R.string.preprocessing_image), true);

		final Handler handler = new Handler();
		Thread threadPreprocImg = new Thread(new Runnable()	{

			@Override
			public void run() {
				try {
					final Bitmap image_smoothed2 = preprocessImageSync(strImgDataFile, nImgDataLength);
					handler.post(new Runnable()	{
						@Override
						public void run() {
							dlgProgress.dismiss();
					        
							if (image_smoothed2 != null)	{
								// everything is done successfully.
								mpaintImgView.setBitmap(image_smoothed2);
								performUndoRedo(0);
						        Toast.makeText(ActivityFingerPaint.this, getString(R.string.select_expr_to_recognize), Toast.LENGTH_SHORT).show();
							} else	{
								showMsgBox(getString(R.string.fail_to_take_picture), getString(R.string.error));
							}
						}
					});		
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		threadPreprocImg.start();
	}
	
	public ImageChop rectifySelect(byte[][] biMatrix) throws InterruptedException	{
		ImageChop imgChop = new ImageChop();
        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
        ImageChop imgChopThinned = StrokeFinder.thinImageChop(imgChop, true);
        double dAngle = ImgRectifier.calcRectifyAngleHough(imgChopThinned.mbarrayImg);
        biMatrix = ImgRectifier.adjustMatrixByAngle(biMatrix, -dAngle, new double[2]);
        biMatrix = StrokeFinder.smoothStroke(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, 19);
        Bitmap image_rectified = ImageMgr.convertBiMatrix2Img(biMatrix);
        ImageMgr.saveImg(image_rectified, MFPFileManagerActivity.getAppFolderFullPath() + File.separator + "mr_rectified.bmp");
   
        image_rectified.recycle();
        
        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
        return imgChop;
	}
		
	public String recognize() throws ExprRecognizeException, InterruptedException	{
		byte[][] biMatrix = mpaintImgView.getBiMatrix();
        if (biMatrix != null && biMatrix.length > 0 && biMatrix[0].length > 0)  {
            //ImageChop imgChop = rectifySelect(biMatrix);	// do not rectify because it leads to a lot of noise points.
            ImageChop imgChop = new ImageChop();
            imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
            //----- now start recognization.
            imgChop = imgChop.convert2MinContainer();
    		Log.e(TAG, "Now start to recognize image:");
            long startTime = System.nanoTime();
            StructExprRecog ser = ExprRecognizer.recognize(imgChop, null, -1, 0, 0);
            ser = ser.restruct();
            ser.rectifyMisRecogChars1stRnd(CameraPreview.msCLM);
            ser.rectifyMisRecogChars2ndRnd();
            ser.rectifyMisRecogWords(CameraPreview.msMWM);
            SerMFPTransFlags smtFlags = new SerMFPTransFlags();
            smtFlags.mbConvertAssign2Eq = true;
            String strOutput = SerMFPTranslator.cvtSer2MFPExpr(ser, null, new CurPos(0), CameraPreview.msMWM, smtFlags);
           long endTime = System.nanoTime();
            Log.e(TAG, String.format("recognizing takes %s", toString(endTime - startTime)));
            return strOutput;
        }
        return "";
	}

    public static String[] getPrototypeFolders()    {
        String[] strFolders = new String[113];
        strFolders[0] = "add";
        strFolders[1] = "backward_slash";
        strFolders[2] = "big_A";
        strFolders[3] = "big_B";
        strFolders[4] = "big_C";
        strFolders[5] = "big_D";
        strFolders[6] = "big_DELTA";
        strFolders[7] = "big_E";
        strFolders[8] = "big_F";
        strFolders[9] = "big_G";
        strFolders[10] = "big_H";
        strFolders[11] = "big_I";
        strFolders[12] = "big_J";
        strFolders[13] = "big_K";
        strFolders[14] = "big_L";
        strFolders[15] = "big_M";
        strFolders[16] = "big_N";
        strFolders[17] = "big_O";
        strFolders[18] = "big_OMEGA";
        strFolders[19] = "big_P";
        strFolders[20] = "big_PHI";
        strFolders[21] = "big_PI";
        strFolders[22] = "big_Q";
        strFolders[23] = "big_R";
        strFolders[24] = "big_S";
        strFolders[25] = "big_SIGMA";
        strFolders[26] = "big_T";
        strFolders[27] = "big_THETA";
        strFolders[28] = "big_U";
        strFolders[29] = "big_V";
        strFolders[30] = "big_W";
        strFolders[31] = "big_X";
        strFolders[32] = "big_Y";
        strFolders[33] = "big_Z";
        strFolders[34] = "brace";
        strFolders[35] = "close_brace";
        strFolders[36] = "close_round_bracket";
        strFolders[37] = "close_square_bracket";
        strFolders[38] = "dollar";
        strFolders[39] = "dot";
        strFolders[40] = "eight";
        strFolders[41] = "euro";
        strFolders[42] = "five";
        strFolders[43] = "forward_slash";
        strFolders[44] = "four";
        strFolders[45] = "infinite";
        strFolders[46] = "integrate";
        strFolders[47] = "integrate_circle";
        strFolders[48] = "larger";
        strFolders[49] = "left_arrow";
        strFolders[50] = "multiply";
        strFolders[51] = "nine";
        strFolders[52] = "one";
        strFolders[53] = "pound";
        strFolders[54] = "right_arrow";
        strFolders[55] = "round_bracket";
        strFolders[56] = "seven";
        strFolders[57] = "six";
        strFolders[58] = "smaller";
        strFolders[59] = "small_a";
        strFolders[60] = "small_alpha";
        strFolders[61] = "small_b";
        strFolders[62] = "small_beta";
        strFolders[63] = "small_c";
        strFolders[64] = "small_d";
        strFolders[65] = "small_delta";
        strFolders[66] = "small_e";
        strFolders[67] = "small_epsilon";
        strFolders[68] = "small_eta";
        strFolders[69] = "small_f";
        strFolders[70] = "small_g";
        strFolders[71] = "small_gamma";
        strFolders[72] = "small_h";
        strFolders[73] = "small_k";
        strFolders[74] = "small_lambda";
        strFolders[75] = "small_m";
        strFolders[76] = "small_mu";
        strFolders[77] = "small_n";
        strFolders[78] = "small_o";
        strFolders[79] = "small_omega";
        strFolders[80] = "small_p";
        strFolders[81] = "small_phi";
        strFolders[82] = "small_pi";
        strFolders[83] = "small_psi";
        strFolders[84] = "small_q";
        strFolders[85] = "small_r";
        strFolders[86] = "small_rho";
        strFolders[87] = "small_s";
        strFolders[88] = "small_sigma";
        strFolders[89] = "small_t";
        strFolders[90] = "small_tau";
        strFolders[91] = "small_theta";
        strFolders[92] = "small_u";
        strFolders[93] = "small_v";
        strFolders[94] = "small_w";
        strFolders[95] = "small_x";
        strFolders[96] = "small_xi";
        strFolders[97] = "small_y";
        strFolders[98] = "small_z";
        strFolders[99] = "small_zeta";
        strFolders[100] = "sqrt_long";
        strFolders[101] = "sqrt_medium";
        strFolders[102] = "sqrt_short";
        strFolders[103] = "sqrt_tall";
        strFolders[104] = "sqrt_very_tall";
        strFolders[105] = "square_bracket";
        strFolders[106] = "star";
        strFolders[107] = "subtract";
        strFolders[108] = "three";
        strFolders[109] = "two";
        strFolders[110] = "vertical_line";
        strFolders[111] = "yuan";
        strFolders[112] = "zero";
        return strFolders;
    }

    public static String toString(long nanoSecs) {
		int minutes    = (int) (nanoSecs / 60000000000.0);
		int seconds    = (int) (nanoSecs / 1000000000.0)  - (minutes * 60);
		int millisecs  = (int) ( ((nanoSecs / 1000000000.0) - (seconds + minutes * 60)) * 1000);


		if (minutes == 0 && seconds == 0)	{
			return millisecs + "ms";
		} else if (minutes == 0 && millisecs == 0)	{
			return seconds + "s";
		} else if (seconds == 0 && millisecs == 0)	{
			return minutes + "min";
		} else if (minutes == 0)	{
			return seconds + "s " + millisecs + "ms";
		} else if (seconds == 0)	{
			return minutes + "min " + millisecs + "ms";
		} else if (millisecs == 0)	{
			return minutes + "min " + seconds + "s";
		}
		return minutes + "min " + seconds + "s " + millisecs + "ms";
	}

	public void showMsgBox(String strMsg, String strTitle)	{
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		blder.setIcon(R.drawable.icon);
		blder.setTitle(strTitle);
		blder.setMessage(strMsg);
		blder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		blder.setCancelable(false);
		AlertDialog alertDlg = blder.create();
		alertDlg.show();
	}

	public void showMsgBoxMaybeOnce(String strMsg, String strTitle, final String strBoxId, boolean bShowOnce)	{
		boolean bNotShown = false;
		SharedPreferences app_settings = getApplication().getSharedPreferences(ActivitySettings.SETTINGS, 0);
		if (app_settings != null) {
			bNotShown = app_settings.getBoolean("Message_box_" + strBoxId + "_not_show_again", false);
		}
		if (bNotShown) {
			return;
		}
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		blder.setIcon(R.drawable.icon);
		blder.setTitle(strTitle);
        LayoutInflater factory = LayoutInflater.from(this);
        View vMsgBoxBody = factory.inflate(R.layout.msg_box_show_once, null);
        TextView txtMsg = (TextView)vMsgBoxBody.findViewById(R.id.txtViewMsg);
        txtMsg.setText(strMsg);
        final CheckBox chkBoxShowOnce = (CheckBox)vMsgBoxBody.findViewById(R.id.chkBoxShowOnce);
        if (!bShowOnce) {
        	chkBoxShowOnce.setVisibility(View.GONE);
        }
		blder.setView(vMsgBoxBody);
		blder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
        		SharedPreferences app_settings = getApplication().getSharedPreferences(ActivitySettings.SETTINGS, 0);
				if (chkBoxShowOnce.isChecked()) {
	        		if (app_settings != null) {
	        			app_settings.edit().putBoolean("Message_box_" + strBoxId + "_not_show_again", true).commit();
	        		}
				} else {
	        		if (app_settings != null) {
	        			app_settings.edit().putBoolean("Message_box_" + strBoxId + "_not_show_again", false).commit();
	        		}
				}
			}
		});
		blder.setCancelable(false);
		AlertDialog alertMsgBox = blder.create();
		alertMsgBox.show();
	}

}
