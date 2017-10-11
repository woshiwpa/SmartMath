package com.cyzapps.SmartMath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.imgmatrixproc.ImgMatrixConverter;
import com.cyzapps.imgmatrixproc.ImgNoiseFilter;
import com.cyzapps.imgmatrixproc.ImgThreshBiMgr;
import com.cyzapps.imgproc.ImageMgr;
import com.cyzapps.mathexprgen.SerMFPTranslator;
import com.cyzapps.mathexprgen.SerMFPTranslator.CurPos;
import com.cyzapps.mathexprgen.SerMFPTranslator.SerMFPTransFlags;
import com.cyzapps.mathrecog.CharLearningMgr;
import com.cyzapps.mathrecog.ExprFilter;
import com.cyzapps.mathrecog.ExprRecognizer;
import com.cyzapps.mathrecog.ImageChop;
import com.cyzapps.mathrecog.MisrecogWordMgr;
import com.cyzapps.mathrecog.StructExprRecog;
import com.cyzapps.mathrecog.UnitPrototypeMgr;
import com.cyzapps.mathrecog.UnitRecognizer;
import com.cyzapps.mathrecog.ExprRecognizer.ExprRecognizeException;
import com.cyzapps.uptloadermgr.UPTJavaLoaderMgr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered preview of the Camera
 * to the surface. We need to center the SurfaceView because not all devices have cameras that
 * support preview sizes at the same aspect ratio as the device's display.
 */
public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback, Camera.PreviewCallback {

	protected static final int PROCESSED_IMAGE_MAX_AREA = 100000;
	protected static final int PROCESSED_IMAGE_MIN_DOT_PER_INCH = 160;
	protected final String TAG = "CameraPreview";
	protected static final String RECOG_FILE_FOLDER = "mathrecog";
	public static final String INITIAL_BMP_FILE_NAME = "mr_initial.bmp";
	public static final String PROCESSED_IMAGE_SAVED_FILE_NAME = "mr_finallyproc.bmp";

    protected SurfaceView mSurfaceView;
    protected SurfaceHolder mHolder;
    protected Size mPreviewSize;
    protected List<Size> mSupportedPreviewSizes;
    protected Camera mCamera;
    
    protected static CharLearningMgr msCLM = new CharLearningMgr();
    protected static MisrecogWordMgr msMWM = new MisrecogWordMgr();

    public boolean mbTakeSnapshot = false;
    public Bitmap mbitmapSnapshot = null;
    
    public static final int VERIFY_AFTER_TAKING_SNAPSHOT = 0;
    public static final int READ_AFTER_TAKING_SNAPSHOT = 1;
    public static final int CALC_AFTER_TAKING_SNAPSHOT = 2;
    public static final int PLOT_AFTER_TAKING_SNAPSHOT = 3;
    
    public int mnActionAfterTakingSnapshot = READ_AFTER_TAKING_SNAPSHOT;
    
    // the following variables have to be static as they are set in main screen, i.e. smart calc
    // before a CameraPreview instance is initialized.
    public static int msnCameraId = 0;
    public static int msnFocusMode = 0;
    public static boolean msbSupportFlash = false;
    
    public Context mcontext = null;
    
    protected Thread mthreadRecognizing = null;

    CameraPreview(final Context context) {
        super(context);

    	mcontext = context;
        mSurfaceView = new SurfaceView(mcontext);
        addView(mSurfaceView);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void setCamera(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
	        Parameters parameters = mCamera.getParameters();
            mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
			List<Integer> formatsList = parameters.getSupportedPreviewFormats();	//get supported preview formats
			if(formatsList.contains(ImageFormat.NV21)) {	// formatsList is always not null.
				parameters.setPreviewFormat(ImageFormat.NV21);		//set preview format is NV21,default is NV21 (yuv420sp)
			}

	    	//  Set Focus mode depending on what is supported. MODE_AUTO is 
	    	//  preferred mode.
			// need not to test supported mode coz it has been tested in main activity.
	    	if (msnFocusMode == 2)	{
	    		parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_INFINITY );
	    	} else if (msnFocusMode == 1)	{
	    		parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_FIXED );
	    	} else	{
	    		// set to auto focus by default
	    		parameters.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO);
	    	}

	    	/*if ((parameters.getMaxExposureCompensation() != 0 || parameters.getMinExposureCompensation() != 0)
	        		&& ActivitySettings.msnPhotoTakenFrom == 1)	{	// screen mode.
	        	parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
	        } */
	    	parameters.setExposureCompensation(0);	// exposure is not adjusted. Seems that screen mode does not bring much benefit.
	        

			List<String> scenesList = parameters.getSupportedSceneModes();
			if (scenesList != null && scenesList.contains(Camera.Parameters.SCENE_MODE_STEADYPHOTO)) {
				parameters.setSceneMode(Camera.Parameters.SCENE_MODE_STEADYPHOTO);	// this may crash in some devices.
			}
			boolean bSuccessful = setCameraParams(mCamera, parameters);

	        requestLayout();
        } 
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetWidth = w;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.width - targetWidth) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.width - targetWidth);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.width - targetWidth) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.width - targetWidth);
                }
            }
        }
        return optimalSize;
    }

	public static void preload(Context context)	{
		AssetManager am = context.getAssets();
		if (UnitRecognizer.msUPTMgr == null)	{
			UnitRecognizer.msUPTMgr = new UnitPrototypeMgr();
		}
		if (UnitRecognizer.msUPTMgr.isEmpty())	{	// if it is not empty, it's must be assigned print or handwriting. so no need to load again.
			UPTJavaLoaderMgr.load(false, true, true);
		}

        InputStream is = null;
        if (msCLM.isEmpty())
        {
	        try {
				is = am.open(RECOG_FILE_FOLDER + File.separator + "clm.xml");
	            msCLM.readFromXML(is);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
        }

        if (msMWM.isEmpty()) {
	        try {
				is = am.open(RECOG_FILE_FOLDER + File.separator + "mwm.xml");
	            msMWM.readFromXML(is);
	        } catch (IOException e) {
	        	e.printStackTrace();
	        }
        }
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We purposely disregard child measurements because act as a
        // wrapper to a SurfaceView that centers the camera preview instead
        // of stretching it.
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed && getChildCount() > 0) {
            final View child = getChildAt(0);

            final int width = r - l;
            final int height = b - t;

            int previewWidth = width;
            int previewHeight = height;
            if (mPreviewSize != null) {
                previewWidth = mPreviewSize.width;
                previewHeight = mPreviewSize.height;
            }

            // keep the child SurfaceView in the top most of the parent and scale its width to fulfill the whole screen.
            final int scaledChildHeight = previewHeight * width / previewWidth;
            child.layout(0, 0, width, scaledChildHeight);
            
            /*if (width * previewHeight > height * previewWidth) {
                final int scaledChildWidth = previewWidth * height / previewHeight;
                child.layout((width - scaledChildWidth) / 2, 0,
                        (width + scaledChildWidth) / 2, height);
            } else {
                final int scaledChildHeight = previewHeight * width / previewWidth;
                child.layout(0, (height - scaledChildHeight) / 2,
                        width, (height + scaledChildHeight) / 2);
            }*/
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.

        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
            }
        } catch (IOException exception) {
            Log.e(TAG, "IOException caused by surfaceCreated()", exception);
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
	    	if (mCamera != null) {
		        // Now that the size is known, set up the camera parameters and begin
		        // the preview.
		        Camera.Parameters p = mCamera.getParameters();
	
		        p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		        requestLayout();
	
		        setCameraParams(mCamera, p);
		        startPreview();
	    	}
        } catch (Exception exception) {
            Log.e(TAG, "Exception caused by surfaceChanged()", exception);
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        try {
        	stopPreview();
	    } catch (Exception exception) {
	        Log.e(TAG, "Exception caused by surfaceChanged()", exception);
	    }
    }
    
    public void startPreview() {
    	if (mCamera != null) {
    		// set flash mode
	        if (msbSupportFlash) {
		        Camera.Parameters p = mCamera.getParameters();
		        if (((ActivityQuickRecog)getContext()).isTurnOnFlashChecked()) {
		        	p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		        } else {
		        	p.setFlashMode(Parameters.FLASH_MODE_OFF);
		        }
		        setCameraParams(mCamera, p);
	        }
	        // set call back.
	        mCamera.setPreviewCallback(this);
	        // start preview.
	        mCamera.startPreview();    
	        
    	}
        ((ActivityQuickRecog)getContext()).onStartPreview();
    }
    
    public void stopPreview() {
    	if (mCamera != null) {
        	// mCamera.cancelAutoFocus();	cannot cancel autofocus here because we may in an autofocus to get data. cancel autofocus may block the whole procedure.
	        mCamera.stopPreview();    
    		// turn off flash after stop preview, otherwise we will see a whole black.
	        if (msbSupportFlash) {
		        Camera.Parameters p = mCamera.getParameters();
		        p.setFlashMode(Parameters.FLASH_MODE_OFF);
		        setCameraParams(mCamera, p);
	        }
	        // unset call back.
	        mCamera.setPreviewCallback(null);
    	}
        ((ActivityQuickRecog)getContext()).onStopPreview();
    }

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// do not use try catch protection for camera operations in this function because too hard to protect.
		if (mbTakeSnapshot) {
		    mbTakeSnapshot = false;
		    stopPreview();
			Camera.Parameters parameters = camera.getParameters();
		    int width = parameters.getPreviewSize().width;
		    int height = parameters.getPreviewSize().height;
		    Rect selectedRect = ((ActivityQuickRecog)getContext()).getSelectRectView().getSelectedRect();
		    Rect clipRect = mSurfaceView.getHolder().getSurfaceFrame();
		    // when we select preview size, we try to find a a preview size matches width best, and we scale
		    // picture to fit width, so we have to use width scaling ratio to adjust select rectangle
		    int nSelectedLeft = selectedRect.left * width / clipRect.width();
		    int nSelectedTop = selectedRect.top * width / clipRect.width();
		    int nSelectedRight = selectedRect.right * width / clipRect.width();
		    int nSelectedBottom = selectedRect.bottom * width / clipRect.width();
		    if (nSelectedRight > width) {
		    	nSelectedRight = width;
		    }
		    if (nSelectedBottom > height) {
		    	nSelectedBottom = height;
		    }
		    DisplayMetrics dm = new DisplayMetrics();
		    ((Activity) mcontext).getWindowManager().getDefaultDisplay().getMetrics(dm);
		    double dotPerInch = width/(double)clipRect.width()*dm.xdpi;
		    final int[][] colorMatrix = decodeYUV420SP(data, width, height, nSelectedLeft, nSelectedTop, nSelectedRight, nSelectedBottom, dotPerInch);
		    final String folder_path = MFPFileManagerActivity.getAppFolderFullPath();
			((ActivityQuickRecog)mcontext).mdlgProgress = ProgressDialog.show(getContext(), getContext().getString(R.string.please_wait),
					getContext().getString(R.string.recognizing_math_expressions_press_back_2_cancel), true, true,
    				new DialogInterface.OnCancelListener() {

						@Override
						public void onCancel(DialogInterface dialog) {
							// cancel AsyncTask
		                    interruptRecogThread(true);
		                    startPreview();
						}
    			
    				});

			final Handler handler = new Handler();
			mthreadRecognizing = new Thread(new Runnable()	{

				@Override
				public void run() {
				    String strRecogResult = "", strErrorMsg = "";
				    int nErrorType = 0;
				    // whether read or calc or plot, we simply return recog result and error message.
			    	try {
					    byte[][] byteMatrix = preprocessImageSync(colorMatrix, folder_path);
			    		strRecogResult = recognize(byteMatrix);
					} catch(Exception e)	{
						Log.e(this.getClass().getSimpleName(), "Recognize exception", e);
						strRecogResult = "";
						if (e instanceof InterruptedException) {
							strErrorMsg = getContext().getString(R.string.recognition_interrupted);
							nErrorType = 1;
						} else if (e.getMessage() != null && e.getMessage().compareTo(ExprRecognizer.TOO_DEEP_CALL_STACK) == 0) {
							strErrorMsg = getContext().getString(R.string.insufficient_memory_to_recognize_complicated_expr);
							nErrorType = 2;
						} else {
							strErrorMsg = getContext().getString(R.string.recognizing_error);
							nErrorType = 3;
						}
			    	}
				    
				    final String strRecogOutput = strRecogResult;
				    final String strErrorMsgOutput = strErrorMsg;
				    final int nErrorTypeValue = nErrorType;
				    
				    handler.post(new Runnable() {

						@Override
						public void run() {
							mthreadRecognizing = null;	//need not to interrupt recog thread because it has finished . We only set recog thread to null.
							if (((ActivityQuickRecog)mcontext).mdlgProgress != null && ((ActivityQuickRecog)mcontext).mdlgProgress.isShowing()) {
								((ActivityQuickRecog)mcontext).mdlgProgress.dismiss();
								((ActivityQuickRecog)mcontext).mdlgProgress = null;
							} else {
								if (strErrorMsgOutput != null && strErrorMsgOutput.length() > 0) {
									Toast.makeText(getContext(), strErrorMsgOutput, Toast.LENGTH_SHORT).show();
								}
								return;	// if progress dialog is not showing, it implies that activity has stopped so do nothing further.
							}
							if (nErrorTypeValue != 1 && nErrorTypeValue != 2) {	// not out of memory error or interruption error.
			            		// need not to restart preview as the activity has finished.
								Intent intent = new Intent();
			            		Bundle b = new Bundle();
			            		if (nErrorTypeValue == 0) {
			            			if (strRecogOutput.length() > 0) {
				            			b.putString("RecognizingResult", strRecogOutput);
				            			b.putString("RecognizingError", strErrorMsgOutput);
					            		b.putInt("ActionAfterTakingSnapshot", mnActionAfterTakingSnapshot);
				            		} else {
				            			b.putString("RecognizingResult", strRecogOutput);
				            			b.putString("RecognizingError", strErrorMsgOutput);
					            		b.putInt("ActionAfterTakingSnapshot", READ_AFTER_TAKING_SNAPSHOT);
				            		}
			            		} else if (nErrorTypeValue != 0) {	// this implies that nErrorType is 3.
			            			b.putString("RecognizingResult", strRecogOutput);
			            			b.putString("RecognizingError", strErrorMsgOutput);
				            		b.putInt("ActionAfterTakingSnapshot", READ_AFTER_TAKING_SNAPSHOT);
			            		}
			            		intent.putExtras(b);
			            		((Activity) getContext()).setResult(Activity.RESULT_OK, intent);
			            		((Activity) getContext()).finish();
							} else {
				                Toast.makeText(getContext(), strErrorMsgOutput, Toast.LENGTH_SHORT).show();
				    	        startPreview();			
							}
						}
				    	
				    });
				    
				}
			});
			mthreadRecognizing.start();

		}
	}

	public boolean isRecogThreadAlive() {
		return (mthreadRecognizing != null && mthreadRecognizing.isAlive());
	}
	
	public void interruptRecogThread(boolean bSetToNull) {
		if (mthreadRecognizing != null && mthreadRecognizing.isAlive()) {
			mthreadRecognizing.interrupt();
		}
		if (bSetToNull) {
			mthreadRecognizing = null;
		}
	}
	
    // check to see if any camera available.
    public static Camera getCamera(Context context)	{
    	int nNumberOfCameras = Camera.getNumberOfCameras();
    	if (nNumberOfCameras == 0)	{
    		msnCameraId = -1;
    		return null;	// do not have a camera.
    	}
    	for (int idx = 0; idx < nNumberOfCameras; idx ++)	{
	    	CameraInfo cameraInfo = new CameraInfo();
	    	try {
	    		Camera.getCameraInfo(idx, cameraInfo);	// may cause crash in Glaxy Grand.
	    	} catch(Exception e) {
	    		continue;
	    	}
    		if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK)	{
    			Camera camera = null;
    			try {
    				camera = Camera.open(idx);
	    			if (camera != null)	{
	    				Camera.Parameters p = camera.getParameters();
	    				List<Integer> formatsList = p.getSupportedPreviewFormats();	//get supported preview formats
	    				if(formatsList.contains(ImageFormat.NV21)) {
	        		    	List<String> supportedFocusModes = p.getSupportedFocusModes();
	        		    	int nFocusMode = -1;
	        		    	if( supportedFocusModes!= null ) 
	        		    	{
	        		    		if( supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
	        		    			nFocusMode = 0;
	        		    		} else if( supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
	        		    			nFocusMode = 1;
	        		    		} else if( supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
	        		    			nFocusMode = 2;
	        		    		}
	        		    	}
	        		    	if (nFocusMode != -1)	{
	        		    		// ok, get it. this is the camera we want
	        		    		msnCameraId = idx;
	        		    		msnFocusMode = nFocusMode;
	        		    		// now determine if flash settings are supported.
	        		            List<String> supportedFlashModes = p.getSupportedFlashModes();
	        		            if (supportedFlashModes == null || supportedFlashModes.isEmpty()
	        		            		|| (supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF))) {
	        		            	msbSupportFlash = false;
	        		            } else if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
	        		            		&& supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)
	        		            		&& supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_OFF))	{
	        		            	msbSupportFlash = true;
	        		            } else {
	        		            	msbSupportFlash = false;
	        		            }
	        		    		return camera;
	        		    	} else	{
	        		    		camera.release();	// focus mode is not acceptable, release the camera and look for the next one.
	        		    		camera = null;
	        		    	}
	    				} else {
	    					camera.release(); // if camera does not support nv21 format, we cannot use it.
	    					camera = null;
	    				}
	    			}
    			} catch (Exception e){
    				// fail to open camera.
    				if (camera != null) {
    					camera.release();
    					camera = null;
    				}
    				
    			}
    		}
    	}
    	// unfortunately, no camera available, return false;
    	msnCameraId = -1;
    	return null;
    }

    public static int[][] decodeYUV420SP(byte[] yuv420sp, int width, int height,
			int left, int top, int right, int bottom, double dotPerInch) {
		int nArea = (right - left)*(bottom - top);
		int nFrameArea = width * height;
		int nStrideRatio = (int)(dotPerInch/PROCESSED_IMAGE_MIN_DOT_PER_INCH);
		if (nStrideRatio < 1) {
			nStrideRatio = 1;
		}
		if (nArea > PROCESSED_IMAGE_MAX_AREA) {
			int nStrideRatio1 = (int) Math.sqrt((double)nArea/PROCESSED_IMAGE_MAX_AREA);
			if (nStrideRatio1 > nStrideRatio) {
				nStrideRatio = nStrideRatio1;
			}
		}
		int idxYuv = 0, idxPixelW = 0, idxPixelH;
		int pixelsWidth = (int)Math.ceil((double)(right - left)/(double)nStrideRatio);
		int pixelsHeight = (int)Math.ceil((double)(bottom - top)/(double)nStrideRatio);
		int[][] pixels = new int[pixelsWidth][pixelsHeight];
		for (int j = top; j < bottom; j += nStrideRatio) {
			int uvp_base = nFrameArea + (j >> 1) * width;
			int u = 0, v = 0;
			idxPixelH = (j - top)/nStrideRatio;
			for (int i = left; i < right; i += nStrideRatio) {
				idxYuv = j * width + i;
				idxPixelW = (i - left)/nStrideRatio;
				int y = (0xff & ((int) yuv420sp[idxYuv])) - 16;
				if (y < 0)
					y = 0;
				int uvp_current = uvp_base + 2*(int)(i/2);
				v = (0xff & yuv420sp[uvp_current]) - 128;
				u = (0xff & yuv420sp[uvp_current + 1]) - 128;

				int y1192 = 1192 * y;
				int r = (y1192 + 1634 * v);
				int g = (y1192 - 833 * v - 400 * u);
				int b = (y1192 + 2066 * u);

				if (r < 0)
					r = 0;
				else if (r > 262143)
					r = 262143;
				if (g < 0)
					g = 0;
				else if (g > 262143)
					g = 262143;
				if (b < 0)
					b = 0;
				else if (b > 262143)
					b = 262143;

				// use interal buffer instead of pixels for UX reasons
				pixels[idxPixelW][idxPixelH] = ((r << 6) & 0xff0000)
						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
			}
		}
		return pixels;
	}	

	public byte[][] preprocessImageSync(int[][] colorMatrix, String strFilesFolderPath) throws InterruptedException	{
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    	int nPixelDiv = 100;
    	if (mbitmapSnapshot != null) {
    		mbitmapSnapshot.recycle();
    	}
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    	mbitmapSnapshot = ImageMgr.convertColorMatrix2Img(colorMatrix);
    	ImageMgr.saveImg(mbitmapSnapshot, strFilesFolderPath + File.separator + INITIAL_BMP_FILE_NAME);	
    	
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        int[][] grayMatrix = ImgMatrixConverter.convertColor2Gray(colorMatrix);
    	if (mbitmapSnapshot != null) {
    		mbitmapSnapshot.recycle();
    	}
    	
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        //mbitmapSnapshot = ImageMgr.convertGrayMatrix2Img(grayMatrix);
        //ImageMgr.saveImg(mbitmapSnapshot, strFilesFolderPath + File.separator + "mr_grayed.bmp");
        
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        grayMatrix = ImgNoiseFilter.filterNoiseNbAvg4Gray(grayMatrix, 1);
    	if (mbitmapSnapshot != null) {
    		mbitmapSnapshot.recycle();
    	}

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    	//mbitmapSnapshot = ImageMgr.convertGrayMatrix2Img(grayMatrix);
        //ImageMgr.saveImg(mbitmapSnapshot, strFilesFolderPath + File.separator + "mr_filtered.bmp");
        
        int nWHMax = Math.max(grayMatrix.length, grayMatrix[0].length);
        int nEstimatedStrokeWidth = (int)Math.ceil((double)nWHMax/(double)nPixelDiv);
        double dAvgRadius = (int)Math.max(3.0, nEstimatedStrokeWidth /2.0);
        byte[][] biMatrix = ImgThreshBiMgr.convertGray2Bi2ndD(grayMatrix, (int)dAvgRadius);
    	if (mbitmapSnapshot != null) {
    		mbitmapSnapshot.recycle();
    	}

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
    	//mbitmapSnapshot = ImageMgr.convertBiMatrix2Img(biMatrix);
        //ImageMgr.saveImg(mbitmapSnapshot, strFilesFolderPath + File.separator + "mr_bilized.bmp");
        
        ImageChop imgChop = new ImageChop();
        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
        double dAvgStrokeWidth = imgChop.calcAvgStrokeWidth();

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        int nFilterR = (int)Math.ceil((dAvgStrokeWidth/2.0 - 1)/2.0);
        biMatrix = ImgNoiseFilter.filterNoiseNbAvg4Bi(biMatrix, nFilterR, 1);
        biMatrix = ImgNoiseFilter.filterNoiseNbAvg4Bi(biMatrix, nFilterR, 2);
        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
    	if (mbitmapSnapshot != null) {
    		mbitmapSnapshot.recycle();
    	}
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        //mbitmapSnapshot = ImageMgr.convertBiMatrix2Img(imgChop.mbarrayImg);
        //ImageMgr.saveImg(mbitmapSnapshot, strFilesFolderPath + File.separator + "mr_smoothed1.bmp");
        
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        biMatrix = ImgNoiseFilter.filterNoisePoints4Bi(biMatrix, (int)dAvgStrokeWidth);
        imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
    	if (mbitmapSnapshot != null) {
    		mbitmapSnapshot.recycle();
    	}
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        mbitmapSnapshot = ImageMgr.convertBiMatrix2Img(imgChop.mbarrayImg);
        ImageMgr.saveImg(mbitmapSnapshot, strFilesFolderPath + File.separator + PROCESSED_IMAGE_SAVED_FILE_NAME); 	// was "mr_smoothed2.bmp"
        
        return biMatrix;
	}
	
	public String recognize(byte[][] biMatrix) throws ExprRecognizeException, InterruptedException	{
        if (biMatrix != null && biMatrix.length > 0 && biMatrix[0].length > 0)  {
            //ImageChop imgChop = rectifySelect(biMatrix);	// do not rectify because it leads to a lot of noise points.
            ImageChop imgChop = new ImageChop();
            imgChop.setImageChop(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length, ImageChop.TYPE_UNKNOWN);
            //----- now start recognization.
            imgChop = imgChop.convert2MinContainer();
    		Log.e(TAG, "Now start to recognize image:");
            long startTime = System.nanoTime();
            StructExprRecog ser = ExprRecognizer.recognize(imgChop, null, -1, 0, 0);
            ser = ExprFilter.filterRawSER(ser, null);
            if (ser == null) {
            	return "";
            }
            ser = ser.restruct();
            ser = ExprFilter.filterRestructedSER(ser, null, null);
            if (ser == null) {
            	return "";
            }
            ser.rectifyMisRecogChars1stRnd(msCLM);
            ser.rectifyMisRecogChars2ndRnd();
            ser.rectifyMisRecogWords(msMWM);
            SerMFPTransFlags smtFlags = new SerMFPTransFlags();
            smtFlags.mbConvertAssign2Eq = true;
            String strOutput = SerMFPTranslator.cvtSer2MFPExpr(ser, null, new CurPos(0), msMWM, smtFlags);
            long endTime = System.nanoTime();
            Log.e(TAG, String.format("recognizing takes %s", toString(endTime - startTime)));
            return strOutput;
        }
        return "";
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

    public static boolean setCameraParams(Camera camera, Parameters params) {
    	try {
	    	camera.setParameters(params);	// some parameters may not be supported which may cause crash.
	    	return true;
    	} catch(Exception e) {
    		return false;
    	}
    }
    
}
