/**
 * Copyright (C) 2009 - 2012 SC 4ViewSoft SRL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyzapps.PlotAdapter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;

import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.GraphDemon.ActivityConfigXYZGraph.AdjOGLChartParams;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.VisualMFP.CoordAxis;
import com.cyzapps.VisualMFP.DataSeriesGridSurface;
import com.cyzapps.VisualMFP.LinearMapper;
import com.cyzapps.VisualMFP.MathLib;
import com.cyzapps.VisualMFP.Position3D;
import com.cyzapps.VisualMFP.MultiLinkedPoint;
import com.cyzapps.VisualMFP.SurfaceStyle.SURFACETYPE;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.util.Log;

/**
 * The XY chart rendering class.
 */
public class OGLChart extends MFPChart implements Renderer {
	
	// FROM coordinate: chart's coordinate
	// TO coordinate: Android's coordinate
	
	public static final float PROJECT_Y_OVERALL_ANGLE =28.0725f;
	public static final float PROJECT_Z_NEAR = 500f;
	public static final float PROJECT_Z_FAR = 1000f;
	
	public double mdVeryTinySize = 2;
	public double mdTinySize = 4;
	public double mdVerySmallSize = 8;
	public double mdSmallSize = 16;
	public double mdMediumSize = 32;
	public double mdLargeSize = 64;
	public double mdVeryLargeSize = 128;
	public double mdHugeSize = 256;
	public double mdVeryHugeSize = 512;

	/** The multiple series dataset. */
	public LinkedList<DataSeriesGridSurface> mDataSet = new LinkedList<DataSeriesGridSurface>();

    public double mdChartWindowWidth = 0;   //the width of the chart window. 3D chart has been mapped to 2D.
    public double mdChartWindowHeight = 0;   //the height of the chart window. 3D chart has been mapped to 2D.
    
    public boolean mbTakeSnapshot = false;	// generally it is false.
    public Bitmap mbitmapScreenSnapshot = null;
    
	public String mstrChartTitle = "A chart";
	
	public Position3D mp3OriginInTO = new Position3D();
	public double mdXAxisLenInTO = 0;
	public double mdYAxisLenInTO = 0;
	public double mdZAxisLenInTO = 0;

	protected LinearMapper mmapperP2PFROM2FROM = new LinearMapper();
	protected LinearMapper mmapperP2PFROM2TO = new LinearMapper();
	protected LinearMapper mmapperP2PTO2TO = new LinearMapper();
	protected LinearMapper mmapperP2P = new LinearMapper();

	protected CoordAxis mcaXAxis = new CoordAxis();
	protected CoordAxis mcaYAxis = new CoordAxis();
	protected CoordAxis mcaZAxis = new CoordAxis();
	
    protected boolean mbNotDrawAxisAndTitle = false;
    public boolean getNotDrawAxisAndTitle() {
        return mbNotDrawAxisAndTitle;
    }

	public String mstrXAxisName = "x";
	public double mdXMark1 = 0;
	public double mdXMark2 = 1;
	public String mstrYAxisName = "y";
	public double mdYMark1 = 0;
	public double mdYMark2 = 1;
	public String mstrZAxisName = "z";
	public double mdZMark1 = 0;
	public double mdZMark2 = 1;
	
	public Position3D mp3OriginInFROM = new Position3D();
	public double mdXAxisLenInFROM = 0;
	public double mdYAxisLenInFROM = 0;
	public double mdZAxisLenInFROM = 0;
	public double getScalingRatioX()	{
		return mdXAxisLenInTO / mdXAxisLenInFROM;
	}
	public void setScalingRatioX(double dScalingRatioX)	{
		mdXAxisLenInFROM = mdXAxisLenInTO / dScalingRatioX;
	}
	public double getScalingRatioY()	{
		return mdYAxisLenInTO / mdYAxisLenInFROM;
	}
	public void setScalingRatioY(double dScalingRatioY)	{
		mdYAxisLenInFROM = mdYAxisLenInTO / dScalingRatioY;
	}
	public double getScalingRatioZ()	{
		return mdZAxisLenInTO / mdZAxisLenInFROM;
	}
	public void setScalingRatioZ(double dScalingRatioZ)	{
		mdZAxisLenInFROM = mdZAxisLenInTO / dScalingRatioZ;
	}
	
	public Position3D mp3SavedOriginInFROM = new Position3D(mp3OriginInFROM);

	public double mdSavedXAxisLenInFROM = mdXAxisLenInFROM;
	public double getSavedScalingRatioX()	{	// scaling ratio = length of 1 unit in FROM / length of 1 unit in TO = TO total length / FROM total length
		return mdXAxisLenInTO / mdSavedXAxisLenInFROM;
	}
	public double mdSavedYAxisLenInFROM = mdYAxisLenInFROM;
	public double getSavedScalingRatioY()	{
		return mdYAxisLenInTO / mdSavedYAxisLenInFROM;
	}
	public double mdSavedZAxisLenInFROM = mdZAxisLenInFROM;
	public double getSavedScalingRatioZ()	{
		return mdZAxisLenInTO / mdSavedZAxisLenInFROM;
	}
	public double mdSavedXMark1 = mdXMark1;
	public double mdSavedXMark2 = mdXMark2;
	public double mdSavedYMark1 = mdYMark1;
	public double mdSavedYMark2 = mdYMark2;
	public double mdSavedZMark1 = mdZMark1;
	public double mdSavedZMark2 = mdZMark2;
	
	// background color is always black.
	public com.cyzapps.VisualMFP.Color mcolorBkGrnd = new com.cyzapps.VisualMFP.Color();
	
	// default foreground color
	public com.cyzapps.VisualMFP.Color mcolorForeGrnd = new com.cyzapps.VisualMFP.Color(255, 255, 255);
	
    public static final int NUMBER_OF_TEXTURES = 64;
    public int mnCurrentTextureId = 0;
    
    protected Rect mrectSettingsBtnPos = new Rect();
    protected Rect mrectZoom1BtnPos = new Rect();
    protected Rect mrectZoomFitBtnPos = new Rect();
    protected Rect mrectZoomInBtnPos = new Rect();
    protected Rect mrectZoomOutBtnPos = new Rect();
    
    protected int mnSettingsBtnTexture = -1;
    protected int mnZoom1BtnTexture = -1;
    protected int mnZoomFitBtnTexture = -1;
    protected int mnZoomInBtnTexture = -1;
    protected int mnZoomOutBtnTexture = -1;

    public Bitmap mbitmapSettingGear;
    public Bitmap mbitmapZoom1;
    public Bitmap mbitmapZoomFit;
    public Bitmap mbitmapZoomIn;
    public Bitmap mbitmapZoomOut;

	public boolean mbCfgWindowStarted = false;
	public void zoom(double dRatio)	{
		if (dRatio != 1)	{
			updateMapperFROM2FROM(0, 0, 0, 0, 0, 0, 1/dRatio, 1/dRatio, 1/dRatio);
			update();
		}
	}
	
	
	public void applyCfgChart(AdjOGLChartParams adjParams)	{
		if (!adjParams.isNoAdj())	{
	        // should be mdLastXScale /mdXScale because mdLastXScale = LastTo/LastFrom, mdXScale = ThisTo/ThisFrom, and we want ThisFrom/LastFrom
	        updateMapperFROM2FROM(0, 0, 0, 0, 0, 0, 1 / adjParams.mdXScale, 1 / adjParams.mdYScale, 1 / adjParams.mdZScale);
	        updateMapperFROM2FROM(adjParams.mdXShift, adjParams.mdYShift, adjParams.mdZShift,
	                0, 0, 0, 1, 1, 1);
	        updateMapperTO2TO(0, 0, 0,
	                (adjParams.mdXRotate)*Math.PI/180.0, (adjParams.mdYRotate)*Math.PI/180.0, (adjParams.mdZRotate)*Math.PI/180.0,
	                1, 1, 1);		
	        mbNotDrawAxisAndTitle = adjParams.mbShowAxisAndTitleChange?(!mbNotDrawAxisAndTitle):mbNotDrawAxisAndTitle;
	        update();
		}
	}
	
	public void clickCfgBtn()	{
		((ActivityChartDemon) mcontext).startCfgOGLChart();
	}
	
	public void clickZoom1Btn()	{
		adjustXYZ1To1();
	}
	
	public void clickZoomFitBtn()	{
		restoreSettings();
        mmapperP2PFROM2FROM = new LinearMapper();
        setMapperFROM2TO();
		update();
	}
	
	public void clickZoomInBtn()	{
        updateMapperFROM2FROM(0, 0, 0, 0, 0, 0, 1 / 2.0, 1 / 2.0, 1 / 2.0);
        update();		
	}
	
	public void clickZoomOutBtn()	{
       	updateMapperFROM2FROM(0, 0, 0, 0, 0, 0, 2.0, 2.0, 2.0);
    	update();
	}
	
	public void click(double dX, double dY)	{
        if (dX >= mrectSettingsBtnPos.left && dY <= mrectSettingsBtnPos.right
                && dY >= mrectSettingsBtnPos.top && dY <= mrectSettingsBtnPos.bottom)   {
     		clickCfgBtn();
        } else if (dX >= mrectZoom1BtnPos.left && dX <= mrectZoom1BtnPos.right
                && dY >= mrectZoom1BtnPos.top && dY <= mrectZoom1BtnPos.bottom)    {
        	clickZoom1Btn();
        } else if (dX >= mrectZoomFitBtnPos.left && dX <= mrectZoomFitBtnPos.right
                && dY >= mrectZoomFitBtnPos.top && dY <= mrectZoomFitBtnPos.bottom)    {
        	clickZoomFitBtn();
        } else if (dX >= mrectZoomInBtnPos.left && dX <= mrectZoomInBtnPos.right
                && dY >= mrectZoomInBtnPos.top && dY <= mrectZoomInBtnPos.bottom)    {
        	clickZoomInBtn();
        } else if (dX >= mrectZoomOutBtnPos.left && dX <= mrectZoomOutBtnPos.right
                && dY >= mrectZoomOutBtnPos.top && dY <= mrectZoomOutBtnPos.bottom)    {
        	clickZoomOutBtn();
        }
	}
	
	public void slide(double dOldX, double dOldY, double dNewX, double dNewY)	{
        double thetaY = Math.PI / 2 * ( (float)(dNewX - dOldX)/(float)mdChartWindowWidth);
        double thetaX = Math.PI / 2 * ( (float)(dNewY - dOldY)/(float)mdChartWindowHeight);

        updateMapperTO2TO(0, 0, 0, thetaX, thetaY, 0, 1, 1, 1);
		update();
	}
	
	public void initialize()	{
		// non-OpenGL initialize stuff.
	}
	
	public static class TextureMgr	{
    	
    	protected int mnTexturesNumber = 16;
	   
	    public static class TextureInfo	{
	    	public Bitmap mbitmap = null;
	    	public int mnState = 0;	// 0 means this texture is not initialized, 1 means initialized.
	    }
	    public int[] mtextures;
	    public TextureInfo[] mtextureInfos;
	 
	    public TextureMgr()	{
	    	resetTextureMgr();
	    }
	    
	    public void resetTextureMgr()	{
	    	mtextures = new int[mnTexturesNumber];
	    	mtextureInfos = new TextureInfo[mnTexturesNumber];
	    	for (int idx = 0; idx < mnTexturesNumber; idx ++)	{
	    		mtextureInfos[idx] = new TextureInfo();
	    	}
	    }
	    
	    public TextureMgr(int nTexturesNumber)	{
	    	mnTexturesNumber = nTexturesNumber;
	    	resetTextureMgr();
	    }
	    
	    public int getFirstUninitTextureId()	{
	    	for (int idx = 0; idx < mnTexturesNumber; idx ++)	{
	    		if (mtextureInfos[idx].mnState == 0)	{
	    			return idx;
	    		}
	    	}
	    	return -1;	// no more uninitialized texture.
	    }
	    
	    // return the previous state of texture id.
	    public int markTextureIdUsed(int id)	{
	    	if (id < 0 || id >= mnTexturesNumber)	{
	    		return -1;
	    	} else if (mtextureInfos[id].mnState == 0)	{
	    		mtextureInfos[id].mnState = 1;
	    		return 0;	// the texture id is originally unused.
	    	} else	{
	    		return mtextureInfos[id].mnState;	// the texture id is originally used.
	    	}
	    }
	    
	    // return the previous state of texture id.
	    public int markTextureIdUnused(int id)	{
	    	if (id < 0 || id >= mnTexturesNumber)	{
	    		return -1;
	    	} else if (mtextureInfos[id].mnState != 0)	{
	    		mtextureInfos[id].mnState = 0;
	    		return mtextureInfos[id].mnState;	// the texture id is originally used.
	    	} else	{
	    		return 0;	// the texture id is originally unused.
	    	}
	    }
    }
    
    public TextureMgr mtextureMgr = new TextureMgr(NUMBER_OF_TEXTURES);
    
    public OGLChart(Context context) {
    	super(context);
    	initialize();
    }
   
	@Override
	public void saveSettings()	{
		mp3SavedOriginInFROM = new Position3D(mp3OriginInFROM);
		mdSavedXAxisLenInFROM = mdXAxisLenInFROM;
		mdSavedYAxisLenInFROM = mdYAxisLenInFROM;
		mdSavedZAxisLenInFROM = mdZAxisLenInFROM;
		mdSavedXMark1 = mdXMark1;
		mdSavedXMark2 = mdXMark2;
		mdSavedYMark1 = mdYMark1;
		mdSavedYMark2 = mdYMark2;
		mdSavedZMark1 = mdZMark1;
		mdSavedZMark2 = mdZMark2;
	}

	@Override
	public void restoreSettings()	{
		mp3OriginInFROM = new Position3D(mp3SavedOriginInFROM);
		mdXAxisLenInFROM = mdSavedXAxisLenInFROM;
		mdYAxisLenInFROM = mdSavedYAxisLenInFROM;
		mdZAxisLenInFROM = mdSavedZAxisLenInFROM;
		mdXMark1 = mdSavedXMark1;
		mdXMark2 = mdSavedXMark2;
		mdYMark1 = mdSavedYMark1;
		mdYMark2 = mdSavedYMark2;
		mdZMark1 = mdSavedZMark1;
		mdZMark2 = mdSavedZMark2;
	}
	
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    	gl.glGenTextures(NUMBER_OF_TEXTURES, mtextureMgr.mtextures, 0);

        gl.glShadeModel(GL10.GL_SMOOTH);             //Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);     //Black Background
        gl.glClearDepthf(1.0f);                     //Depth Buffer Setup
        gl.glEnable(GL10.GL_DEPTH_TEST);             //Enables Depth Testing
        gl.glDepthFunc(GL10.GL_LEQUAL);             //The Type Of Depth Testing To Do

        //Really Nice Perspective Calculations
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // clear Screen and Depth Buffer
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);     //Select The Projection Matrix
        gl.glLoadIdentity();                     //Reset The Projection Matrix

        // Drawing
        /*gl.glTranslatef(0.0f, 0.0f, -10.0f);        // move 10 units INTO the screen
                                                // is the same as moving the camera 10 units away
                                                // now the visible z range is from -9.9 to 90, and view port
                                                // is still the 480 * 800 screen . The fvof angle and y/x
                                                // does not change.
         * 
         */
        draw(gl);
        if (mbTakeSnapshot == true)	{
        	mbitmapScreenSnapshot = takeScreenSnapshot(gl);
        }
    }

    protected Point calcScreenXY(Position3D p3OpenGL)    {
        double dFurtherZ = PROJECT_Z_FAR;
        double dRatio = dFurtherZ/-p3OpenGL.getZ();
        double dFarYMax = dFurtherZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360);
        double dFarXMax = dFarYMax * mdChartWindowWidth / mdChartWindowHeight;
        double dMappedX = (1 + (p3OpenGL.getX() * dRatio / dFarXMax)) * mdChartWindowWidth / 2.0;
        double dMappedY = (1 - (p3OpenGL.getY() * dRatio / dFarYMax)) * mdChartWindowHeight / 2.0;
        return new Point((int)dMappedX, (int)dMappedY);
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
Log.d("OGLChart", "onSurfaceChanged : start, chart title is " + this.mstrChartTitle);
        if(height == 0) {                         //Prevent A Divide By Zero By
            height = 1;                         //Making Height Equal One
        }

        mdChartWindowHeight = height;
        mdChartWindowWidth = width;
        
        gl.glViewport(0, 0, width, height);     //Reset The Current Viewport

        gl.glMatrixMode(GL10.GL_PROJECTION);     //Select The Projection Matrix
        gl.glLoadIdentity();                     //Reset The Projection Matrix
        //Calculate The Aspect Ratio Of The Window, tan(28.0725/2) = 0.25
        //GLU.gluPerspective(gl, PROJECT_Y_OVERALL_ANGLE, (float)width / (float)height, PROJECT_Z_NEAR - 1, PROJECT_Z_FAR + 1);
        GLU.gluPerspective(gl, PROJECT_Y_OVERALL_ANGLE, (float)width / (float)height, PROJECT_Z_NEAR, PROJECT_Z_FAR);

		double dFarXLength = PROJECT_Z_FAR * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * 2
				* width / height;
		double dFarYLength = PROJECT_Z_FAR * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * 2;
        mp3OriginInTO = new Position3D(0, 0, (-PROJECT_Z_NEAR - PROJECT_Z_FAR)/2.0);
        double dFarMinFarXYLen = Math.min(dFarXLength, dFarYLength);
        mdXAxisLenInTO = dFarMinFarXYLen * 3/4;    //* 5 / 9;
        mdYAxisLenInTO = dFarMinFarXYLen * 3/4;    //* 5 / 9;;
        mdZAxisLenInTO = dFarMinFarXYLen * 3/4;    //* 5 / 9;;
		
        setMapperFROM2TO();
		update();
Log.d("OGLChart", "onSurfaceChanged : end");
    }
  
    public void drawLine(GL10 gl, Position3D p3PrevPoint, Position3D p3NextPoint, com.cyzapps.VisualMFP.Color color) {
        // set the colour for the line
        gl.glColor4f(color.getF1R(), color.getF1G(), color.getF1B(), color.getF1Alpha());
        float vertices[] = {
                (float)p3PrevPoint.getX(), (float)p3PrevPoint.getY(), (float)p3PrevPoint.getZ(),
                (float)p3NextPoint.getX(), (float)p3NextPoint.getY(), (float)p3NextPoint.getZ(),

        };
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer;
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Set the face rotation
        //gl.glFrontFace(GL10.GL_CW);
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_LINES, 0, vertices.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
    
    public void drawRectangle(GL10 gl, Position3D p3LeftTop, Position3D p3RightBottom, com.cyzapps.VisualMFP.Color color) {
    	/** 3d chart ignore point shape and size. An alternative way is like
            gl.glColor3f(r, g, b);
            gl.glBegin(GL.GL_QUADS);
            gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
            gl.glVertex3f( halfFaceSize, -halfFaceSize, halfFaceSize);
            gl.glVertex3f( halfFaceSize,  halfFaceSize, halfFaceSize);
            gl.glVertex3f(-halfFaceSize,  halfFaceSize, halfFaceSize);
            gl.glEnd();
        */
        gl.glColor4f(color.getF1R(), color.getF1G(), color.getF1B(), color.getF1Alpha());
        float vertices[] = {
                (float)p3LeftTop.getX(), (float)p3RightBottom.getY(), (float)p3LeftTop.getZ(),
                (float)p3RightBottom.getX(), (float)p3RightBottom.getY(), (float)p3RightBottom.getZ(),
                (float)p3LeftTop.getX(), (float)p3LeftTop.getY(), (float)p3LeftTop.getZ(),
                (float)p3RightBottom.getX(), (float)p3LeftTop.getY(), (float)p3LeftTop.getZ()
                };
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer;
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Set the face rotation
        //gl.glFrontFace(GL10.GL_CW);
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
    
    public void drawPoint(GL10 gl, Position3D p3Point, com.cyzapps.VisualMFP.Color color) {
    	// 3d chart ignore point shape and size.
        gl.glColor4f(color.getF1R(), color.getF1G(), color.getF1B(), color.getF1Alpha());
        float vertices[] = {
                (float)p3Point.getX(), (float)p3Point.getY(), (float)p3Point.getZ()
                };
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer;
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(vertices);
        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Set the face rotation
        //gl.glFrontFace(GL10.GL_CW);
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_POINTS, 0, vertices.length / 3);

        // Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }
   
    public void draw(GL10 gl)	{
//Log.d("OGLChart", "draw : start draw, chart title is " + this.mstrChartTitle);
	    // draw surface has to be in front of draw axis coz text of axis will show black background on surface otherwise.
        drawDataGridSurfaces(gl);
        
//Log.d("OGLChart", "draw : after draw surfaces");
		com.cyzapps.VisualMFP.Color colorChartTitle = mcolorForeGrnd;
		double dTitleSize = mdSmallSize;
		double dLegendSize = mdSmallSize * 1.1;
        double dButtonSize = mdSmallSize * 1.5;
		double dAxisFontSize = mdSmallSize * 1.5;
		//Note that in Android the drawing order is always from furthest to nearest. Otherwise some transparency will be opaque.

        // assume that mp3OriginInTO is the same as mmapperP2P.mapFrom2To(mca*Axis.mp3From)
        com.cyzapps.VisualMFP.Color colorLine1 = mcolorForeGrnd;	//new com.cyzapps.VisualMFP.Color(255.0f, 255.0f, 255.0f);
        Position3D p3XAxisFrom = mmapperP2P.mapFrom2To(mcaXAxis.mp3From);
        Position3D p3XAxisTo = mmapperP2P.mapFrom2To(mcaXAxis.mp3To);
        if (!mbNotDrawAxisAndTitle) {
        	drawAxis(gl, mcaXAxis, p3XAxisFrom, p3XAxisTo, colorLine1, 0, 1, 0, dAxisFontSize);
        }
        
        com.cyzapps.VisualMFP.Color colorLine2 = mcolorForeGrnd;	//new com.cyzapps.VisualMFP.Color(255.0f, 255.0f, 255.0f);
        Position3D p3YAxisFrom = mmapperP2P.mapFrom2To(mcaYAxis.mp3From);
        Position3D p3YAxisTo = mmapperP2P.mapFrom2To(mcaYAxis.mp3To);
        if (!mbNotDrawAxisAndTitle) {
        	drawAxis(gl, mcaYAxis, p3YAxisFrom, p3YAxisTo, colorLine2, 0, -1, 0, dAxisFontSize);
        }

        com.cyzapps.VisualMFP.Color colorLine3 = mcolorForeGrnd;	//new com.cyzapps.VisualMFP.Color(255.0f, 255.0f, 255.0f);
        Position3D p3ZAxisFrom = mmapperP2P.mapFrom2To(mcaZAxis.mp3From);
        Position3D p3ZAxisTo = mmapperP2P.mapFrom2To(mcaZAxis.mp3To);
        if (!mbNotDrawAxisAndTitle) {
        	drawAxis(gl, mcaZAxis, p3ZAxisFrom, p3ZAxisTo, colorLine3, -1, 0, 0, dAxisFontSize);
        }
//Log.d("OGLChart", "draw : after draw axises");

		double dChartTitleX = 0;
		double dChartTitleZ = -PROJECT_Z_NEAR - 1;
		double dChartTitleY = -dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) - dTitleSize/2;
		Position3D pntChartTitle = new Position3D(dChartTitleX, dChartTitleY, dChartTitleZ);
        if (!mbNotDrawAxisAndTitle) {
        	drawText(gl, mstrChartTitle, pntChartTitle, dTitleSize, colorChartTitle);
        }
//Log.d("OGLChart", "draw : after draw title");

        Position3D pntLegendTopLeft;
        if (mdChartWindowWidth >= mdChartWindowHeight * 0.9)   {  // landscape mode
            pntLegendTopLeft = new Position3D(
            		dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                    pntChartTitle.getY() - dTitleSize, pntChartTitle.getZ());
            drawLegend(gl, pntLegendTopLeft, dLegendSize * 3, true); // dLegendSize * 3 is width 
            dButtonSize = mdSmallSize * 1.5;
        } else  {   // portrait mode
            pntLegendTopLeft = new Position3D(
            		dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
            		dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) + dLegendSize, pntChartTitle.getZ());
            drawLegend(gl, pntLegendTopLeft, dLegendSize, false);  // dLegendSize is height
            dButtonSize = mdSmallSize;
        }
//Log.d("OGLChart", "draw : after draw legend");
        
        Position3D pntSettings[] = new Position3D[4], pntZoom1[] = new Position3D[4], pntZoomFit[] = new Position3D[4], pntZoomIn[] = new Position3D[4], pntZoomOut[] = new Position3D[4];
        if (mnSettingsBtnTexture == -1)    {
        	mnSettingsBtnTexture = createBmpTexture(gl, mbitmapSettingGear);
        }
        if (mnZoom1BtnTexture == -1)   {
            mnZoom1BtnTexture = createBmpTexture(gl, mbitmapZoom1);
        }
        if (mnZoomFitBtnTexture == -1)   {
        	mnZoomFitBtnTexture = createBmpTexture(gl, mbitmapZoomFit);
        }
        if (mnZoomInBtnTexture == -1)   {
            mnZoomInBtnTexture = createBmpTexture(gl, mbitmapZoomIn);
        }
        if (mnZoomOutBtnTexture == -1)   {
        	mnZoomOutBtnTexture = createBmpTexture(gl, mbitmapZoomOut);
        }

        pntSettings[0] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize, pntChartTitle.getZ());
        pntSettings[1] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize, pntChartTitle.getZ());
        pntSettings[2] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize, pntChartTitle.getZ());
        pntSettings[3] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize, pntChartTitle.getZ());
        applyRectTexture(gl, pntSettings, mnSettingsBtnTexture);
        pntZoom1[0] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 5/2, pntChartTitle.getZ());
        pntZoom1[1] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 3/2, pntChartTitle.getZ());
        pntZoom1[2] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 5/2, pntChartTitle.getZ());
        pntZoom1[3] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 3/2, pntChartTitle.getZ());
        applyRectTexture(gl, pntZoom1, mnZoom1BtnTexture);
        pntZoomFit[0] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 4, pntChartTitle.getZ());
        pntZoomFit[1] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 3, pntChartTitle.getZ());
        pntZoomFit[2] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 4, pntChartTitle.getZ());
        pntZoomFit[3] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 3, pntChartTitle.getZ());
        applyRectTexture(gl, pntZoomFit, mnZoomFitBtnTexture);
        pntZoomIn[0] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 11/2, pntChartTitle.getZ());
        pntZoomIn[1] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 9/2, pntChartTitle.getZ());
        pntZoomIn[2] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 11/2, pntChartTitle.getZ());
        pntZoomIn[3] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 9/2, pntChartTitle.getZ());
        applyRectTexture(gl, pntZoomIn, mnZoomInBtnTexture);
        pntZoomOut[0] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 7, pntChartTitle.getZ());
        pntZoomOut[1] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight - dButtonSize,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 6, pntChartTitle.getZ());
        pntZoomOut[2] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 7, pntChartTitle.getZ());
        pntZoomOut[3] = new Position3D(
        		-dChartTitleZ * Math.tan(PROJECT_Y_OVERALL_ANGLE * Math.PI / 360) * mdChartWindowWidth / mdChartWindowHeight,
                pntChartTitle.getY() - dTitleSize - dButtonSize * 6, pntChartTitle.getZ());
        applyRectTexture(gl, pntZoomOut, mnZoomOutBtnTexture);
        
        Point pntLeftTop, pntRightBottom;
        Position3D pntSettingsTopLeft = pntSettings[1], pntZoom1TopLeft = pntZoom1[1], pntZoomFitTopLeft = pntZoomFit[1], pntZoomInTopLeft = pntZoomIn[1], pntZoomOutTopLeft = pntZoomOut[1];
        pntLeftTop = calcScreenXY(pntSettingsTopLeft);
        pntRightBottom = calcScreenXY(new Position3D(pntSettingsTopLeft.getX() + dButtonSize,
                                                    pntSettingsTopLeft.getY() - dButtonSize,
                                                    pntSettingsTopLeft.getZ()));
        mrectSettingsBtnPos = new Rect(pntLeftTop.x, pntLeftTop.y, pntRightBottom.x, pntRightBottom.y);
        pntLeftTop = calcScreenXY(pntZoom1TopLeft);
        pntRightBottom = calcScreenXY(new Position3D(pntZoom1TopLeft.getX() + dButtonSize,
                                                    pntZoom1TopLeft.getY() - dButtonSize,
                                                    pntZoom1TopLeft.getZ()));
        mrectZoom1BtnPos = new Rect(pntLeftTop.x, pntLeftTop.y, pntRightBottom.x, pntRightBottom.y);
        pntLeftTop = calcScreenXY(pntZoomFitTopLeft);
        pntRightBottom = calcScreenXY(new Position3D(pntZoomFitTopLeft.getX() + dButtonSize,
                                                    pntZoomFitTopLeft.getY() - dButtonSize,
                                                    pntZoomFitTopLeft.getZ()));
        mrectZoomFitBtnPos = new Rect(pntLeftTop.x, pntLeftTop.y, pntRightBottom.x, pntRightBottom.y);
        pntLeftTop = calcScreenXY(pntZoomInTopLeft);
        pntRightBottom = calcScreenXY(new Position3D(pntZoomInTopLeft.getX() + dButtonSize,
									        		pntZoomInTopLeft.getY() - dButtonSize,
									        		pntZoomInTopLeft.getZ()));
        mrectZoomInBtnPos = new Rect(pntLeftTop.x, pntLeftTop.y, pntRightBottom.x, pntRightBottom.y);
        pntLeftTop = calcScreenXY(pntZoomOutTopLeft);
        pntRightBottom = calcScreenXY(new Position3D(pntZoomOutTopLeft.getX() + dButtonSize,
                                                    pntZoomOutTopLeft.getY() - dButtonSize,
                                                    pntZoomOutTopLeft.getZ()));
        mrectZoomOutBtnPos = new Rect(pntLeftTop.x, pntLeftTop.y, pntRightBottom.x, pntRightBottom.y);
//Log.d("OGLChart", "draw : after draw buttons");
    }
    
    // note that bitmap should be a square and size is 2**n * 2**n
    public int createBmpTexture(GL10 gl, Bitmap bitmap)	{
        int nNextAvailableId = mtextureMgr.getFirstUninitTextureId();
        if (nNextAvailableId >= 0)	{	// -1 means no available texture id.
	        //...and bind it to our array
	        gl.glBindTexture(GL10.GL_TEXTURE_2D, mtextureMgr.mtextures[nNextAvailableId]);
	
	        //Create Nearest Filtered Texture
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	
	        //Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
	        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
	        //gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
	
	        //Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
	        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	        mtextureMgr.markTextureIdUsed(nNextAvailableId);
        }
        return nNextAvailableId;
    }
    
    /**
     * Create a bitmap text based texture.
     * @param text
     * @param color
     * @param dTextSize
     * @return texture id
     */
    public int createBmpStrTexture(GL10 gl, String text, com.cyzapps.VisualMFP.Color color, double dTextSize, Rect rectSize, Rect rectOrigTxt)	{
        // initialize textures
        Bitmap bitmapText = createTextBitmap(text, new Paint(), color, dTextSize, rectOrigTxt);
        if (rectSize != null)	{
        	rectSize.top = rectSize.left = 0;
        	rectSize.right = bitmapText.getWidth();
        	rectSize.bottom = bitmapText.getHeight();
        }

        int nTextureId = createBmpTexture(gl, bitmapText);
        bitmapText.recycle();
        return nTextureId;
    }

    // return the rectangle of the text boundary
    public Rect calcTextBounds(String text, Paint paint, double dTextSize)	{
    	return calcTextBounds(text, paint, dTextSize, dTextSize/5);
    }
    
    // return the rectangle of the text boundary
    public Rect calcTextBounds(String text, Paint paint, double dTextSize, double dSpaceBtwTxtLines)	{
        float fPaintOriginalTxtSize = paint.getTextSize();
        Align alignOriginal = paint.getTextAlign();
        Rect rectTxtBox = new Rect(); // rectTxtBox gives original text box and returns the actual text box
        if (text == null)	{
        	return rectTxtBox;	// null protection.
        }
        paint.setTextSize((float)dTextSize);
        paint.setTextAlign(Align.LEFT);
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; ++i) {
        	int nTxtWidth = (int)Math.ceil(paint.measureText(lines[i]));
            if (i == 0)    {
                rectTxtBox.left = 0;
                rectTxtBox.right = nTxtWidth;	//rect.right;
                rectTxtBox.top = 0;
                rectTxtBox.bottom = (int)Math.ceil(-paint.ascent() + paint.descent());//rect.height();
            } else    {
                rectTxtBox.right = Math.max(rectTxtBox.right, nTxtWidth);
                rectTxtBox.bottom = (int) (rectTxtBox.bottom + (int)Math.ceil(-paint.ascent() + paint.descent()) + dSpaceBtwTxtLines);
            }
        }
        paint.setTextAlign(alignOriginal);
        paint.setTextSize(fPaintOriginalTxtSize);
        return rectTxtBox;
    }
    
    protected String getLenLimitedString(GL10 gl, String text, double dHeight, double dLen) {
    	if (text != null)	{
	        for (int idx = text.length(); idx >= 0; idx --)  {
	            String strNewText = text.substring(0, idx);
	            if (idx == text.length() && calcTextBounds(text, new Paint(), dHeight).width() < dLen)  {
	                return strNewText;
	            } else if (calcTextBounds(strNewText + "...", new Paint(), dHeight).width() < dLen)    {
	                return strNewText + "...";
	            }
	        }
        }
        return "";
    }
	
    public void drawZAdjustedText(GL10 gl, String text,
            Position3D pntCentral, double dHeight,  // width is determined by the text
            com.cyzapps.VisualMFP.Color color)   {
        dHeight = dHeight  / PROJECT_Z_FAR * -pntCentral.getZ(); // adjust dHeight make the font z irrelevant.
        drawText(gl, text, pntCentral, dHeight, color);
    }
    
    public void drawText(GL10 gl, String text,
            Position3D pntCentral, double dHeight,  // width is determined by the text
            com.cyzapps.VisualMFP.Color color) {
    	if (text == null)	{
    		return;	// cannot draw null;
    	}
    	Rect rectSize = new Rect();
    	Rect rectOrigText = new Rect();
    	double dTextScalingRatio = 8;
		int nTextureId = createBmpStrTexture(gl, text, color, dTextScalingRatio * dHeight, rectSize, rectOrigText);
		Position3D[] pnts = new Position3D[4];
        pnts[0] = pntCentral.add(new Position3D((-rectOrigText.width()/2.0)/dTextScalingRatio, (rectOrigText.height()/2.0 - rectSize.height())/dTextScalingRatio, 0));
        pnts[1] = pntCentral.add(new Position3D((-rectOrigText.width()/2.0)/dTextScalingRatio, (rectOrigText.height()/2.0)/dTextScalingRatio, 0));
        pnts[2] = pntCentral.add(new Position3D((-rectOrigText.width()/2.0 + rectSize.width())/dTextScalingRatio, (rectOrigText.height()/2.0 - rectSize.height())/dTextScalingRatio, 0));
        pnts[3] = pntCentral.add(new Position3D((-rectOrigText.width()/2.0 + rectSize.width())/dTextScalingRatio, (rectOrigText.height()/2.0)/dTextScalingRatio, 0));
        applyRectTexture(gl, pnts, nTextureId);
        mtextureMgr.markTextureIdUnused(nTextureId);
    }
    
    /**
     * Draw a multiple lines text bitmap. The size of the bitmap is returned in rectTxtBox
     */
    public Bitmap createTextBitmap(String text, Paint paint, com.cyzapps.VisualMFP.Color color, double dTextSize, Rect rectOrigTxtBox) {
    	if (text == null)	{
    		text = "";	// null pointer protection.
    	}
        // First of all, calculate text bound
        int nPaintOriginalColor = paint.getColor();
        if (color != null)    {
            paint.setColor(color.getARGB());
        }    // otherwise, use paint's color.
        float fOriginalTxtSize = paint.getTextSize();
        paint.setTextSize((float) dTextSize);
        
        String[] lines = text.split("\n");
        double dSpaceBtwTxtLines = dTextSize/5.0;
        Rect rectTxtBox = calcTextBounds(text, paint, dTextSize, dSpaceBtwTxtLines);
        rectOrigTxtBox.set(rectTxtBox);
        Rect rectAdjustBox = new Rect();
        rectAdjustBox.left = 0;
        rectAdjustBox.top = 0;
        // need 2 ** integer power otherwise some devices cannot show.
        if (rectTxtBox.width() <= 0)	{
        	rectAdjustBox.right = 2;	// was 0, but createBitmap would fail if 0
        } else	{
        	rectAdjustBox.right = getNextHighestPO2(rectTxtBox.width());
        }
        if (rectTxtBox.height() <= 0)	{
        	rectAdjustBox.bottom = 2;	// was 0, but createBitmap would fail if 0
        } else	{
        	rectAdjustBox.bottom = getNextHighestPO2(rectTxtBox.height());
        }
       
        // Create an empty, mutable bitmap
        Bitmap bitmap = Bitmap.createBitmap(rectAdjustBox.width(), rectAdjustBox.height(), Bitmap.Config.ARGB_8888);
        // get a canvas to paint over the bitmap
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(0);
        // get a background image from resources
        // note the image format must match the bitmap format

        Drawable background = new ColorDrawable(0x00000000);
        background.setBounds(0, 0, rectAdjustBox.width(), rectAdjustBox.height());
        background.draw(canvas); // draw the background to our bitmap
       
        float fX = 0, fY = /*rectTxtBox.height();*/-(float)paint.ascent();    // drawText's y parameter is the baseline of the character. Note that paint.ascent() is negative.
        int yOff = 0;   
        for (int i = 0; i < lines.length; ++i) {
            canvas.drawText(lines[i], fX, fY + yOff, paint);
            yOff = (int) (yOff + (int)Math.ceil(-paint.ascent() + paint.descent()) + dSpaceBtwTxtLines); // space between lines
        }

        paint.setTextSize(fOriginalTxtSize);
        paint.setColor(nPaintOriginalColor);
       
        return bitmap;
    }
   
    /**
     * Calculates the next highest power of two for a given integer.
     *
     * @param n the number
     * @return a power of two equal to or higher than n
     */
    public static int getNextHighestPO2( int n ) {
        n -= 1;
        n = n | (n >> 1);
        n = n | (n >> 2);
        n = n | (n >> 4);
        n = n | (n >> 8);
        n = n | (n >> 16);
        n = n | (n >> 32);
        return n + 1;
    }
    
    /**
     * convert a rectangle bitmap texture and apply it to a rectangle,
     * note that order of pnts is top left -> top right -> bottom left -> bottom right
     */
    public void applyRectTexture(GL10 gl, Position3D[] pnts, int nTextureId) {    //a 4 point rectangle.

    	if (nTextureId < 0 || nTextureId >= NUMBER_OF_TEXTURES)	{
    		return;	// invalid texture
    	}
        // apply the texture
        FloatBuffer vertexBuffer;    // buffer holding the vertices
        float vertices[] = {
                (float)pnts[0].getX(), (float)pnts[0].getY(), (float)pnts[0].getZ(),    // bottom left
                (float)pnts[1].getX(), (float)pnts[1].getY(), (float)pnts[1].getZ(),    // top left
                (float)pnts[2].getX(), (float)pnts[2].getY(), (float)pnts[2].getZ(),    // bottom right
                (float)pnts[3].getX(), (float)pnts[3].getY(), (float)pnts[3].getZ(),    // top right
        };

        FloatBuffer textureBuffer;    // buffer holding the texture coordinates
        float texture[] = {           
                // Mapping coordinates for the vertices
                0.0f, 1.0f,        // top left
                0.0f, 0.0f,        // bottom left
                1.0f, 1.0f,        // top right
                1.0f, 0.0f        // bottom right
        };
        // a float has 4 bytes so we allocate for each coordinate 4 bytes
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
       
        // allocates the memory from the byte buffer
        vertexBuffer = byteBuffer.asFloatBuffer();
       
        // fill the vertexBuffer with the vertices
        vertexBuffer.put(vertices);
       
        // set the cursor position to the beginning of the buffer
        vertexBuffer.position(0);
       
        byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        textureBuffer = byteBuffer.asFloatBuffer();
        textureBuffer.put(texture);
        textureBuffer.position(0);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);
       
        // Point to our buffers
        gl.glEnable(GL10.GL_TEXTURE_2D);            //Enable Texture Mapping ( NEW )
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		//gl.glBlendFunc(GL10.GL_ZERO, GL10.GL_ONE);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);	// was GL10.GL_REPLACE
       
        //...and bind it to our array
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mtextureMgr.mtextures[nTextureId]);
        
        gl.glColor4f(0f, 0f, 0f, 0f);
       
        // Point to our vertex buffer
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
       
        // Draw the vertices as triangle strip
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);

        //Disable the client state before leaving
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL10.GL_BLEND);
        gl.glDisable(GL10.GL_TEXTURE_2D);            //Disable Texture Mapping ( NEW )
    }

	/**
	 * Draw an axis, assume at least one of dMarkOffsetX, Y, Z are not equal to 0
	 */
	public void drawAxis(GL10 gl, CoordAxis caAxis, Position3D pnt0, Position3D pnt1,
			com.cyzapps.VisualMFP.Color colorLine, double dMarkOffsetX,
			double dMarkOffsetY, double dMarkOffsetZ, double dAxisFontSize) {
        // do not draw angle for a 3-D chart
        drawLine(gl, pnt0, pnt1, colorLine);
		double dMarkOffsetLen = Math.sqrt(dMarkOffsetX * dMarkOffsetX + dMarkOffsetY * dMarkOffsetY + dMarkOffsetZ * dMarkOffsetZ);
		for (int idx = 0; idx < caAxis.marraydScaleMarks.length; idx++)	{
			double dX = 0, dY = 0, dZ = 0;
			if (caAxis.mdValueTo != caAxis.mdValueFrom)	{
				dX = (pnt1.getX() - pnt0.getX())/(caAxis.mdValueTo - caAxis.mdValueFrom) * (caAxis.marraydScaleMarks[idx] - caAxis.mdValueFrom) + pnt0.getX();
				dY = (pnt1.getY() - pnt0.getY())/(caAxis.mdValueTo - caAxis.mdValueFrom) * (caAxis.marraydScaleMarks[idx] - caAxis.mdValueFrom) + pnt0.getY();
				dZ = (pnt1.getZ() - pnt0.getZ())/(caAxis.mdValueTo - caAxis.mdValueFrom) * (caAxis.marraydScaleMarks[idx] - caAxis.mdValueFrom) + pnt0.getZ();
				Position3D pntMark0 = new Position3D(dX, dY, dZ),
						pntMark1 = new Position3D(dX + dMarkOffsetX/dMarkOffsetLen * mdTinySize,
												dY + dMarkOffsetY/dMarkOffsetLen * mdTinySize,
												dZ + dMarkOffsetZ/dMarkOffsetLen * mdTinySize);
				drawLine(gl, pntMark0, pntMark1, colorLine);
				
				Rect rectSize = calcTextBounds(caAxis.marraystrMarkTxts[idx], new Paint(), dAxisFontSize);
				double dShiftedZ = 0;
                double dAdjMarkTxtZ = dZ - dMarkOffsetZ/dMarkOffsetLen * mdVerySmallSize - dShiftedZ;
				double dShiftedX = (dMarkOffsetX >= 0)?rectSize.width()/2.0:(-rectSize.width()/2.0);
                dShiftedX = dShiftedX / PROJECT_Z_FAR * -dAdjMarkTxtZ;
				double dShiftedY = (dMarkOffsetY >= 0)?rectSize.height()/2.0:(-rectSize.height()/2.0);
                dShiftedY = dShiftedY / PROJECT_Z_FAR * -dAdjMarkTxtZ;
				Position3D pntMarkTxt = new Position3D(dX - dMarkOffsetX/dMarkOffsetLen * mdVerySmallSize / PROJECT_Z_FAR * -dAdjMarkTxtZ - dShiftedX,
						dY - dMarkOffsetY/dMarkOffsetLen * mdVerySmallSize / PROJECT_Z_FAR * -dAdjMarkTxtZ - dShiftedY, dAdjMarkTxtZ);
                drawZAdjustedText(gl, caAxis.marraystrMarkTxts[idx], pntMarkTxt, dAxisFontSize, colorLine);

			}
		}
		
		/*axis name string is placed on the other side to the mark texts, and always on the end of the axis.*/
		double dNameTxtX = pnt1.getX(),
				dNameTxtY = pnt1.getY(),
				dNameTxtZ = pnt1.getZ();
		Rect rectSize = calcTextBounds(caAxis.mstrAxisName, new Paint(), dAxisFontSize);
		double dShiftedZ = 0;
        double dAdjNameTxtZ = dNameTxtZ + dMarkOffsetZ/dMarkOffsetLen * mdVerySmallSize + dShiftedZ;
		double dShiftedX = (dMarkOffsetX >= 0)?rectSize.width()/2.0:(-rectSize.width()/2.0);
        dShiftedX = dShiftedX / PROJECT_Z_FAR * -dAdjNameTxtZ;
		double dShiftedY = (dMarkOffsetY >= 0)?rectSize.height()/2.0:(-rectSize.height()/2.0);
        dShiftedY = dShiftedY / PROJECT_Z_FAR * -dAdjNameTxtZ;
		Position3D pntNameTxt = new Position3D(dNameTxtX + dMarkOffsetX/dMarkOffsetLen * mdVerySmallSize / PROJECT_Z_FAR * -dAdjNameTxtZ + dShiftedX,
					dNameTxtY + dMarkOffsetY/dMarkOffsetLen * mdVerySmallSize / PROJECT_Z_FAR * -dAdjNameTxtZ + dShiftedY, dAdjNameTxtZ);
		
		drawZAdjustedText(gl, caAxis.mstrAxisName, pntNameTxt, dAxisFontSize, colorLine);
	}

    public void drawLegend(GL10 gl, Position3D pntLeftTop, double dWidthOrHeight, boolean bVerticalMode)	{
        int nDataWithLegendNum = mDataSet.size();
        for (int idx = 0; idx < mDataSet.size(); idx ++)	{
            if (mDataSet.get(idx).mstrName == null || mDataSet.get(idx).mstrName.length() == 0) {
                nDataWithLegendNum--;   // do not show empty legend.
            }
        }
        if (bVerticalMode)  {
            double dTextHeight = dWidthOrHeight/4;
            Position3D pntRightBottom = new Position3D(pntLeftTop.getX() + dWidthOrHeight,
                    pntLeftTop.getY() - dTextHeight*5.0/2.0 * nDataWithLegendNum,
                    pntLeftTop.getZ());
            drawRectangle(gl, pntLeftTop.subtract(new Position3D(0,0,1)), pntRightBottom.subtract(new Position3D(0,0,1)), new com.cyzapps.VisualMFP.Color(100, 60, 60, 60));
            double dCurrentY = pntLeftTop.getY();
            int nNumberofLines = 10;
            for (int idx = 0; idx < mDataSet.size(); idx ++)	{
                if (mDataSet.get(idx).mstrName == null || mDataSet.get(idx).mstrName.length() == 0) {
                    continue;   // do not show empty legend.
                }

                dCurrentY -= dTextHeight/2;
                com.cyzapps.VisualMFP.Color colorMin = mDataSet.get(idx).msurfaceStyle.mclrUpFaceMin;
                com.cyzapps.VisualMFP.Color colorMax = mDataSet.get(idx).msurfaceStyle.mclrUpFaceMax;
                for (int idx1 = 0; idx1 < nNumberofLines; idx1 ++)  {
                    Position3D pntFrom = new Position3D(pntLeftTop.getX() + (idx1 + 1) * dWidthOrHeight/(nNumberofLines + 2),
                            dCurrentY, pntLeftTop.getZ());
                    Position3D pntTo = new Position3D(pntLeftTop.getX() + (idx1 + 2) * dWidthOrHeight/(nNumberofLines + 2),
                            dCurrentY, pntLeftTop.getZ());
                    com.cyzapps.VisualMFP.Color colorCurrent = new com.cyzapps.VisualMFP.Color(
                            (int)(((double)(colorMax.mnAlpha - colorMin.mnAlpha))/nNumberofLines * idx1 + colorMin.mnAlpha),
                            (int)(((double)(colorMax.mnR - colorMin.mnR))/nNumberofLines * idx1 + colorMin.mnR),
                            (int)(((double)(colorMax.mnG - colorMin.mnG))/nNumberofLines * idx1 + colorMin.mnG),
                            (int)(((double)(colorMax.mnB - colorMin.mnB))/nNumberofLines * idx1 + colorMin.mnB));

                    drawLine(gl, pntFrom, pntTo, colorCurrent);
                }
                dCurrentY -= dTextHeight;
                Position3D pntTxt = new Position3D(pntLeftTop.getX() + dWidthOrHeight/2, dCurrentY, pntLeftTop.getZ());
                String strName = getLenLimitedString(gl, mDataSet.get(idx).mstrName, dTextHeight, dWidthOrHeight/(nNumberofLines + 2) * nNumberofLines);
                //drawZAdjustedText(gl, strName, pntTxt, mdSmallSize, new com.cyzapps.VisualMFP.Color(255, 255, 255, 255));  // white
                drawText(gl, strName, pntTxt, dTextHeight, new com.cyzapps.VisualMFP.Color(255, 255, 255, 255));  // white and 1/8 * dWidth
                dCurrentY -= dTextHeight;
            }
        } else  {
            double dTextHeight = dWidthOrHeight*2/5;
            double dWidth = dWidthOrHeight * 2;
            Position3D pntRightBottom = new Position3D(pntLeftTop.getX() + dWidth * nDataWithLegendNum,
                    pntLeftTop.getY() - dWidthOrHeight,
                    pntLeftTop.getZ());
            drawRectangle(gl, pntLeftTop.subtract(new Position3D(0,0,1)), pntRightBottom.subtract(new Position3D(0,0,1)), new com.cyzapps.VisualMFP.Color(100, 60, 60, 60));
            double dCurrentX = pntLeftTop.getX();
            int nNumberofLines = 10;
            for (int idx = 0; idx < mDataSet.size(); idx ++)	{
                if (mDataSet.get(idx).mstrName == null || mDataSet.get(idx).mstrName.length() == 0) {
                    continue;   // do not show empty legend.
                }

                com.cyzapps.VisualMFP.Color colorMin = mDataSet.get(idx).msurfaceStyle.mclrUpFaceMin;
                com.cyzapps.VisualMFP.Color colorMax = mDataSet.get(idx).msurfaceStyle.mclrUpFaceMax;
                for (int idx1 = 0; idx1 < nNumberofLines; idx1 ++)  {
                    Position3D pntFrom = new Position3D(dCurrentX + (idx1 + 1) * dWidth/(nNumberofLines + 2),
                            pntLeftTop.getY() - dTextHeight/2, pntLeftTop.getZ());
                    Position3D pntTo = new Position3D(dCurrentX + (idx1 + 2) * dWidth/(nNumberofLines + 2),
                            pntLeftTop.getY() - dTextHeight/2, pntLeftTop.getZ());
                    com.cyzapps.VisualMFP.Color colorCurrent = new com.cyzapps.VisualMFP.Color(
                            (int)(((double)(colorMax.mnAlpha - colorMin.mnAlpha))/nNumberofLines * idx1 + colorMin.mnAlpha),
                            (int)(((double)(colorMax.mnR - colorMin.mnR))/nNumberofLines * idx1 + colorMin.mnR),
                            (int)(((double)(colorMax.mnG - colorMin.mnG))/nNumberofLines * idx1 + colorMin.mnG),
                            (int)(((double)(colorMax.mnB - colorMin.mnB))/nNumberofLines * idx1 + colorMin.mnB));

                    drawLine(gl, pntFrom, pntTo, colorCurrent);
                }
                Position3D pntTxt = new Position3D(dCurrentX + dWidth/2, pntLeftTop.getY() - dTextHeight * 1.5, pntLeftTop.getZ());
                String strName = getLenLimitedString(gl, mDataSet.get(idx).mstrName, dTextHeight, dWidth/(nNumberofLines + 2) * nNumberofLines);
                //drawZAdjustedText(gl, strName, pntTxt, mdSmallSize, new com.cyzapps.VisualMFP.Color(255, 255, 255, 255));  // white
                drawText(gl, strName, pntTxt, dTextHeight, new com.cyzapps.VisualMFP.Color(255, 255, 255, 255));  // white and 1/8 * dWidth
                dCurrentX += dWidth;
            }
        }
    }
    
	public void drawDataGridSurface(GL10 gl, DataSeriesGridSurface dsg)	{
        int nMode = dsg.getMode();
        if (dsg.msurfaceStyle.menumSurfaceType == SURFACETYPE.SURFACETYPE_GRID)   {
            for (int idx = 0; idx < dsg.mlistData.size(); idx ++)	{
                if (!MathLib.isValidReal(dsg.mlistData.get(idx).getConvertedPoint().getX())
                        || !MathLib.isValidReal(dsg.mlistData.get(idx).getConvertedPoint().getY())
                        || !MathLib.isValidReal(dsg.mlistData.get(idx).getConvertedPoint().getZ()))	{
                    continue;
                }
                Position3D p3Mapped = mmapperP2P.mapFrom2To(dsg.mlistData.get(idx).getConvertedPoint());
                double dHorizontalValue = (nMode == 2)?dsg.mlistData.get(idx).getConvertedPoint().getY():(nMode == 1?dsg.mlistData.get(idx).getConvertedPoint().getX():dsg.mlistData.get(idx).getConvertedPoint().getZ());
                drawPoint(gl, p3Mapped, dsg.msurfaceStyle.getUpFaceColorAt(dHorizontalValue));

                for (MultiLinkedPoint itr : dsg.mlistData.get(idx).msetConnects)	{
                    // drawline only if counter-party's x is smaller than me.
                    if (MathLib.isValidReal(itr.getConvertedPoint().getX())
                            && MathLib.isValidReal(itr.getConvertedPoint().getY())
                            && MathLib.isValidReal(itr.getConvertedPoint().getZ())
                            && ((itr.getConvertedPoint().getX() < dsg.mlistData.get(idx).getConvertedPoint().getX())
                                || (itr.getConvertedPoint().getX() == dsg.mlistData.get(idx).getConvertedPoint().getX()
                                        && itr.getConvertedPoint().getY() < dsg.mlistData.get(idx).getConvertedPoint().getY())
                                || (itr.getConvertedPoint().getX() == dsg.mlistData.get(idx).getConvertedPoint().getX()
                                        && itr.getConvertedPoint().getY() == dsg.mlistData.get(idx).getConvertedPoint().getY()
                                        && itr.getConvertedPoint().getZ() < dsg.mlistData.get(idx).getConvertedPoint().getZ())))	{
                        Position3D p3MappedNextPnt = mmapperP2P.mapFrom2To(itr.getConvertedPoint());
                        double dHValueNxtPnt = (nMode == 2)?itr.getConvertedPoint().getY():(nMode == 1?itr.getConvertedPoint().getX():itr.getConvertedPoint().getZ());
                        drawLine(gl, p3Mapped, p3MappedNextPnt, dsg.msurfaceStyle.getUpFaceColorAt((dHorizontalValue + dHValueNxtPnt)/2.0));
                    }
                }
            }
        } else if (dsg.msurfaceStyle.menumSurfaceType == SURFACETYPE.SURFACETYPE_SURFACE)    {
            for (LinkedList<MultiLinkedPoint> itr : dsg.mlistSurfaceElements)   {
                // Setup color-array buffer. Colors in float. A float has 4 bytes (NEW)
                float[] colorsUpFace = new float[itr.size() * 4];
                float[] colorsDownFace = new float[itr.size() * 4];
                float vertices[] = new float[itr.size() * 3];
                byte[] indices = new byte[itr.size()]; // Indices to above vertices 
                for (int idxPnt = 0; idxPnt < itr.size(); idxPnt ++)   {
                    Position3D p3 = itr.get(idxPnt).getConvertedPoint();
                    Position3D p3Mapped = mmapperP2P.mapFrom2To(itr.get(idxPnt).getConvertedPoint());
                    double dHorizontalValue = (nMode == 2)?p3.getY():(nMode == 1?p3.getX():p3.getZ());
                    com.cyzapps.VisualMFP.Color colorAtPntUpFace = dsg.msurfaceStyle.getUpFaceColorAt(dHorizontalValue);
                    com.cyzapps.VisualMFP.Color colorAtPntDownFace = dsg.msurfaceStyle.getDownFaceColorAt(dHorizontalValue);
                    colorsUpFace[idxPnt * 4] = colorAtPntUpFace.getF1R();
                    colorsUpFace[idxPnt * 4 + 1] = colorAtPntUpFace.getF1G();
                    colorsUpFace[idxPnt * 4 + 2] = colorAtPntUpFace.getF1B();
                    colorsUpFace[idxPnt * 4 + 3] = colorAtPntUpFace.getF1Alpha();
                    colorsDownFace[idxPnt * 4] = colorAtPntDownFace.getF1R();   //0.0f;
                    colorsDownFace[idxPnt * 4 + 1] = colorAtPntDownFace.getF1G();   //0.0f;
                    colorsDownFace[idxPnt * 4 + 2] = colorAtPntDownFace.getF1B();   //0.0f;
                    colorsDownFace[idxPnt * 4 + 3] = colorAtPntDownFace.getF1Alpha();
                    vertices[idxPnt * 3] = (float)p3Mapped.getX();
                    vertices[idxPnt * 3 + 1] = (float)p3Mapped.getY();
                    vertices[idxPnt * 3 + 2] = (float)p3Mapped.getZ();
                    indices[idxPnt] = (byte)idxPnt;
                }
                
                ByteBuffer cbbUpFace = ByteBuffer.allocateDirect(colorsUpFace.length * 4);
                cbbUpFace.order(ByteOrder.nativeOrder()); // Use native byte order
                FloatBuffer colorBufferUpFace = cbbUpFace.asFloatBuffer();  // Convert byte buffer to float
                colorBufferUpFace.put(colorsUpFace);            // Copy data into buffer
                colorBufferUpFace.position(0);            // Rewind
                
                ByteBuffer cbbDownFace = ByteBuffer.allocateDirect(colorsDownFace.length * 4);
                cbbDownFace.order(ByteOrder.nativeOrder()); // Use native byte order
                FloatBuffer colorBufferDownFace = cbbDownFace.asFloatBuffer();  // Convert byte buffer to float
                colorBufferDownFace.put(colorsDownFace);            // Copy data into buffer
                colorBufferDownFace.position(0);            // Rewind
                
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
                byteBuffer.order(ByteOrder.nativeOrder());
                FloatBuffer vertexBuffer;
                vertexBuffer = byteBuffer.asFloatBuffer();
                vertexBuffer.put(vertices);
                // set the cursor position to the beginning of the buffer
                vertexBuffer.position(0);
                
                ByteBuffer indexBuffer;    // Buffer for index-array
                indexBuffer = ByteBuffer.allocateDirect(indices.length);
                indexBuffer.put(indices);
                indexBuffer.position(0);
                
                // Point to our vertex buffer
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);          // Enable color-array
                gl.glEnable(GL10.GL_CULL_FACE);
                // gl.glPolygonMode(GL10.GL_FRONT_AND_BACK, GL10.GL_FILL);	// Android doesn't support it.
                gl.glEnable( GL10.GL_POLYGON_OFFSET_FILL );      
                gl.glPolygonOffset( 1.0f, 1.0f );
                
                // Set the face rotation
                gl.glCullFace(GL10.GL_FRONT);
                // Point to our vertex buffer
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBufferDownFace);  // Define color-array buffer
                // Draw the vertices as triangle strip
                //gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
                gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);
                
                gl.glCullFace(GL10.GL_BACK);
                gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBufferUpFace);  // Define color-array buffer
                gl.glDrawElements(GL10.GL_TRIANGLE_STRIP, indices.length, GL10.GL_UNSIGNED_BYTE, indexBuffer);

                gl.glDisable( GL10.GL_POLYGON_OFFSET_FILL ); 
                gl.glDisable(GL10.GL_CULL_FACE);
                gl.glDisableClientState(GL10.GL_COLOR_ARRAY);   // Disable color-array (NEW)
                gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

            }
            for (int idx = 0; idx < dsg.mlistData.size(); idx ++)	{
                if (!MathLib.isValidReal(dsg.mlistData.get(idx).getConvertedPoint().getX())
                        || !MathLib.isValidReal(dsg.mlistData.get(idx).getConvertedPoint().getY())
                        || !MathLib.isValidReal(dsg.mlistData.get(idx).getConvertedPoint().getZ()))	{
                    continue;
                }
                Position3D p3Mapped = mmapperP2P.mapFrom2To(dsg.mlistData.get(idx).getConvertedPoint());
                double dHorizontalValue = (nMode == 2)?dsg.mlistData.get(idx).getConvertedPoint().getY():(nMode == 1?dsg.mlistData.get(idx).getConvertedPoint().getX():dsg.mlistData.get(idx).getConvertedPoint().getZ());
                drawPoint(gl, p3Mapped, dsg.msurfaceStyle.getUpFaceColorAt(dHorizontalValue));

                for (MultiLinkedPoint itr : dsg.mlistData.get(idx).msetConnects)	{
                    // drawline only if counter-party's x is smaller than me.
                    if (MathLib.isValidReal(itr.getConvertedPoint().getX())
                            && MathLib.isValidReal(itr.getConvertedPoint().getY())
                            && MathLib.isValidReal(itr.getConvertedPoint().getZ())
                            && ((itr.getConvertedPoint().getX() < dsg.mlistData.get(idx).getConvertedPoint().getX())
                                || (itr.getConvertedPoint().getX() == dsg.mlistData.get(idx).getConvertedPoint().getX()
                                        && itr.getConvertedPoint().getY() < dsg.mlistData.get(idx).getConvertedPoint().getY())
                                || (itr.getConvertedPoint().getX() == dsg.mlistData.get(idx).getConvertedPoint().getX()
                                        && itr.getConvertedPoint().getY() == dsg.mlistData.get(idx).getConvertedPoint().getY()
                                        && itr.getConvertedPoint().getZ() < dsg.mlistData.get(idx).getConvertedPoint().getZ())))	{
                        Position3D p3MappedNextPnt = mmapperP2P.mapFrom2To(itr.getConvertedPoint());
                        //double dHValueNxtPnt = (nMode == 2)?itr.getConvertedPoint().getY():(nMode == 1?itr.getConvertedPoint().getX():itr.getConvertedPoint().getZ());
                        //com.cyzapps.VisualMFP.Color lineColor = dsg.msurfaceStyle.getUpFaceColorAt((dHorizontalValue + dHValueNxtPnt)/2.0).getRGBInverseColor();
                        com.cyzapps.VisualMFP.Color lineColor = new com.cyzapps.VisualMFP.Color(127, 127, 127); // wireframe is gray.
                        drawLine(gl, p3Mapped, p3MappedNextPnt, lineColor);
                    }
                }
            }
        }
	}

	public void drawDataGridSurfaces(GL10 gl)	{
		for (int idx = 0; idx < mDataSet.size(); idx ++)	{
			drawDataGridSurface(gl, mDataSet.get(idx));
		}
	}

    public void setMapperFROM2TO()   {
        mmapperP2PFROM2TO.setLinearMapper(mp3SavedOriginInFROM.getX(), mp3SavedOriginInFROM.getY(), mp3SavedOriginInFROM.getZ(),
				-Math.PI/2.0, -Math.PI/4.0, 0, 
				getSavedScalingRatioX(), -getSavedScalingRatioY(), getSavedScalingRatioZ(),
				mp3OriginInTO.getX(), mp3OriginInTO.getY(), mp3OriginInTO.getZ());
        mmapperP2PFROM2TO.adjustLinearMapper4TOChange(mp3OriginInTO.getX(), mp3OriginInTO.getY(), mp3OriginInTO.getZ(),
                Math.atan(Math.sqrt(2)), 0, 0, 1, 1, 1,
                mp3OriginInTO.getX(), mp3OriginInTO.getY(), mp3OriginInTO.getZ());
    }
    
	public void updateMapperFROM2FROM(double dXShift, double dYShift, double dZShift,
            double dXRotationAngle, double dYRotationAngle, double dZRotationAngle,
            double dXScalingRatio, double dYScalingRatio, double dZScalingRatio)	{
		mmapperP2PFROM2FROM.adjustLinearMapper4FROMChange(mp3OriginInFROM.getX(), mp3OriginInFROM.getY(), mp3OriginInFROM.getZ(),
                dXRotationAngle, dYRotationAngle, dZRotationAngle, dXScalingRatio, dYScalingRatio, dZScalingRatio,
                mp3OriginInFROM.getX() + dXShift, mp3OriginInFROM.getY() + dYShift, mp3OriginInFROM.getZ() + dZShift);

        // adjust origin in from
        mp3OriginInFROM = new Position3D(mp3OriginInFROM.getX() + dXShift, mp3OriginInFROM.getY() + dYShift, mp3OriginInFROM.getZ() + dZShift);
        // adjust scaling ratio
        setScalingRatioX(getScalingRatioX() / dXScalingRatio);
        setScalingRatioY(getScalingRatioY() / dYScalingRatio);
        setScalingRatioZ(getScalingRatioZ() / dZScalingRatio);
    }
    
	public void updateMapperTO2TO(double dXShift, double dYShift, double dZShift,
            double dXRotationAngle, double dYRotationAngle, double dZRotationAngle,
            double dXScalingRatio, double dYScalingRatio, double dZScalingRatio)	{
		mmapperP2PTO2TO.adjustLinearMapper4TOChange(mp3OriginInTO.getX(), mp3OriginInTO.getY(), mp3OriginInTO.getZ(),
                dXRotationAngle, dYRotationAngle, dZRotationAngle, dXScalingRatio, dYScalingRatio, dZScalingRatio,
                mp3OriginInTO.getX() + dXShift, mp3OriginInTO.getY() + dYShift, mp3OriginInTO.getZ() + dZShift);

        // adjust origin in TO
        mp3OriginInTO = new Position3D(mp3OriginInTO.getX() + dXShift,
                                       mp3OriginInTO.getY() + dYShift,
                                       mp3OriginInTO.getZ() + dZShift);
        // adjust scaling ratio
        setScalingRatioX(getScalingRatioX() / dXScalingRatio);
        setScalingRatioY(getScalingRatioY() / dYScalingRatio);
        setScalingRatioZ(getScalingRatioZ() / dZScalingRatio);
    }
        
	public static class AxisMarks	{
		public double mdXMark1 = 0.0;
		public double mdXMark2 = 1.0;
		public double mdYMark1 = 0.0;
		public double mdYMark2 = 1.0;
		public double mdZMark1 = 0.0;
		public double mdZMark2 = 1.0;
	}
	
    public AxisMarks updateAxisMarks()   {
        // adjust marks along x axis
        double dOverAllZoomRatioX = getScalingRatioX()/getSavedScalingRatioX();
        double dTmp = Math.pow(10, (int)Math.log10(dOverAllZoomRatioX));
        double dTmp1 = dOverAllZoomRatioX/dTmp;	// always between (10, 0.1)
        if (dTmp1 >= 7.5)	{
            dOverAllZoomRatioX = dTmp * 10;
        } else if (dTmp1 >= 3.5)	{
            dOverAllZoomRatioX = dTmp * 5;
        } else if (dTmp1 >= 1.5)	{
            dOverAllZoomRatioX = dTmp * 2;
        } else if (dTmp1 >= 0.75)	{
            dOverAllZoomRatioX = dTmp;
        } else if (dTmp1 >= 0.35)	{
            dOverAllZoomRatioX = dTmp * 0.5;
        } else if (dTmp1 >= 0.15)	{
            dOverAllZoomRatioX = dTmp * 0.2;
        } else	{
            dOverAllZoomRatioX = dTmp * 0.1;
        }
        double dXMark1 = mdSavedXMark1;
        double dXMark2 = (mdSavedXMark2 - mdSavedXMark1) / dOverAllZoomRatioX + mdSavedXMark1;
        // adjust marks along y axis
        double dOverAllZoomRatioY = getScalingRatioY()/getSavedScalingRatioY();
        dTmp = Math.pow(10, (int)Math.log10(dOverAllZoomRatioY));
        dTmp1 = dOverAllZoomRatioY/dTmp;	// always between (10, 0.1)
        if (dTmp1 >= 7.5)	{
            dOverAllZoomRatioY = dTmp * 10;
        } else if (dTmp1 >= 3.5)	{
            dOverAllZoomRatioY = dTmp * 5;
        } else if (dTmp1 >= 1.5)	{
            dOverAllZoomRatioY = dTmp * 2;
        } else if (dTmp1 >= 0.75)	{
            dOverAllZoomRatioY = dTmp;
        } else if (dTmp1 >= 0.35)	{
            dOverAllZoomRatioY = dTmp * 0.5;
        } else if (dTmp1 >= 0.15)	{
            dOverAllZoomRatioY = dTmp * 0.2;
        } else	{
            dOverAllZoomRatioY = dTmp * 0.1;
        }
        double dYMark1 = mdSavedYMark1;
        double dYMark2 = (mdSavedYMark2 - mdSavedYMark1) / dOverAllZoomRatioY + mdSavedYMark1;
        // adjust marks along z axis
        double dOverAllZoomRatioZ = getScalingRatioZ()/getSavedScalingRatioZ();
        dTmp = Math.pow(10, (int)Math.log10(dOverAllZoomRatioZ));
        dTmp1 = dOverAllZoomRatioZ/dTmp;	// always between (10, 0.1)
        if (dTmp1 >= 7.5)	{
            dOverAllZoomRatioZ = dTmp * 10;
        } else if (dTmp1 >= 3.5)	{
            dOverAllZoomRatioZ = dTmp * 5;
        } else if (dTmp1 >= 1.5)	{
            dOverAllZoomRatioZ = dTmp * 2;
        } else if (dTmp1 >= 0.75)	{
            dOverAllZoomRatioZ = dTmp;
        } else if (dTmp1 >= 0.35)	{
            dOverAllZoomRatioZ = dTmp * 0.5;
        } else if (dTmp1 >= 0.15)	{
            dOverAllZoomRatioZ = dTmp * 0.2;
        } else	{
            dOverAllZoomRatioZ = dTmp * 0.1;
        }
        double dZMark1 = mdSavedZMark1;
        double dZMark2 = (mdSavedZMark2 - mdSavedZMark1) / dOverAllZoomRatioZ + mdSavedZMark1;
        
        AxisMarks axisMarks = new AxisMarks();
        axisMarks.mdXMark1 = dXMark1;
        axisMarks.mdXMark2 = dXMark2;
        axisMarks.mdYMark1 = dYMark1;
        axisMarks.mdYMark2 = dYMark2;
        axisMarks.mdZMark1 = dZMark1;
        axisMarks.mdZMark2 = dZMark2;
        return axisMarks;
    }
    
    public LinearMapper updateMapperP2P()   {
    	LinearMapper mapperP2P = mmapperP2PTO2TO.multiply(mmapperP2PFROM2TO.multiply(mmapperP2PFROM2FROM));	// this should be thread-safe coz mmapperP2P is changed atomically.
    	return mapperP2P;
    }
	
    public void update()	{
    	LinearMapper mapperP2P = null;
    	AxisMarks axisMarks = null;
    	AllCoordAxis allCoordAxis = null;
    	try	{
	    	mapperP2P = updateMapperP2P();
			axisMarks = updateAxisMarks();
			allCoordAxis = updateCoordAxis(axisMarks);
		} catch(Exception e)	{
    	} finally	{
            // To ensure thread safe, have to put all the writing update here.
    		// each of the operations are atomic. But still have potential problem because
    		// no lock is used (to ensure performance).
    		if (mapperP2P != null && axisMarks != null && allCoordAxis != null
    				&& allCoordAxis.mcaXAxis != null && allCoordAxis.mcaYAxis != null
    				&& allCoordAxis.mcaZAxis != null)	{
    			mmapperP2P = mapperP2P;
    			mdXMark1 = axisMarks.mdXMark1;
    			mdXMark2 = axisMarks.mdXMark2;
    			mdYMark1 = axisMarks.mdYMark1;
    			mdYMark2 = axisMarks.mdYMark2;
    			mdZMark1 = axisMarks.mdZMark1;
    			mdZMark2 = axisMarks.mdZMark2;
    			mcaXAxis = allCoordAxis.mcaXAxis;
    			mcaYAxis = allCoordAxis.mcaYAxis;
    			mcaZAxis = allCoordAxis.mcaZAxis;
    		}
    	}
    }
    
    public static class AllCoordAxis	{
    	public CoordAxis mcaXAxis = null;
    	public CoordAxis mcaYAxis = null;
    	public CoordAxis mcaZAxis = null;
    }
    
	public AllCoordAxis updateCoordAxis(AxisMarks axisMarks)	{
		// TO coordinate is canvas while FROM coordinate is the chart's coordinate. 
		CoordAxis caXAxis = new CoordAxis(mcaXAxis), caYAxis = new CoordAxis(mcaYAxis), caZAxis = new CoordAxis(mcaZAxis);
		caXAxis.mdValueFrom = mp3OriginInFROM.getX() - mdXAxisLenInFROM/2.0;
		caXAxis.mdValueTo = caXAxis.mdValueFrom + mdXAxisLenInFROM;
		caXAxis.mp3From = mp3OriginInFROM.subtract(new Position3D(mdXAxisLenInFROM/2.0, 0, 0));
		caXAxis.mp3To = mp3OriginInFROM.add(new Position3D(mdXAxisLenInFROM/2.0, 0, 0));
		caXAxis.mclr = this.mcolorForeGrnd;
		caXAxis.mstrAxisName = mstrXAxisName;
		caXAxis.mdMarkInterval = axisMarks.mdXMark2 - axisMarks.mdXMark1;
		// cannot directly convert to int because mdXMark1 - mcaXAxis.mdValueFrom may overflow int.
		MFPNumeric mfpNumIntTmp = new MFPNumeric((axisMarks.mdXMark1 - caXAxis.mdValueFrom)/caXAxis.mdMarkInterval).toIntOrNanInfMFPNum();
		double dXMarkStart = axisMarks.mdXMark1 - mfpNumIntTmp.multiply(new MFPNumeric(caXAxis.mdMarkInterval)).doubleValue();
		if (dXMarkStart < caXAxis.mdValueFrom)	{
			dXMarkStart += caXAxis.mdMarkInterval;
		}
		int nNumofMarks = 0;
		if (caXAxis.mdValueTo >= dXMarkStart)	{
			nNumofMarks = (int)((caXAxis.mdValueTo - dXMarkStart)/caXAxis.mdMarkInterval) + 1;
		}
		int nNumofSigDig = 32;
		if (caXAxis.mdValueTo > caXAxis.mdValueFrom)	{
			nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(caXAxis.mdValueFrom), Math.abs(caXAxis.mdValueTo))
						/(caXAxis.mdValueTo - caXAxis.mdValueFrom)*(nNumofMarks + 1))) + 1;
		}
		if (nNumofSigDig > 32)	{
			nNumofSigDig = 32;
		}
		caXAxis.marraydScaleMarks = new double[nNumofMarks];
		caXAxis.marraystrMarkTxts = new String[nNumofMarks];
		for (int idx = 0; idx < nNumofMarks; idx ++)	{
			caXAxis.marraydScaleMarks[idx] = dXMarkStart + idx * caXAxis.mdMarkInterval;
            // use bigdecimal instead of MFPNumeric so that toString can output something like 2.1e30
			caXAxis.marraystrMarkTxts[idx] = new BigDecimal(caXAxis.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
		}
		
		caYAxis.mdValueFrom = mp3OriginInFROM.getY() - mdYAxisLenInFROM/2.0;
		caYAxis.mdValueTo = caYAxis.mdValueFrom + mdYAxisLenInFROM;
		caYAxis.mp3From = mp3OriginInFROM.subtract(new Position3D(0, mdYAxisLenInFROM/2.0, 0));
		caYAxis.mp3To = mp3OriginInFROM.add(new Position3D(0, mdYAxisLenInFROM/2.0, 0));
		caYAxis.mclr = this.mcolorForeGrnd;
		caYAxis.mstrAxisName = mstrYAxisName;
		caYAxis.mdMarkInterval = axisMarks.mdYMark2 - axisMarks.mdYMark1;
		// cannot directly convert to int because mdYMark1 - caYAxis.mdValueFrom may overflow int.
		mfpNumIntTmp = new MFPNumeric((axisMarks.mdYMark1 - caYAxis.mdValueFrom)/caYAxis.mdMarkInterval).toIntOrNanInfMFPNum();
		double dYMarkStart = axisMarks.mdYMark1 - mfpNumIntTmp.multiply(new MFPNumeric(caYAxis.mdMarkInterval)).doubleValue();
		if (dYMarkStart < caYAxis.mdValueFrom)	{
			dYMarkStart += caYAxis.mdMarkInterval;
		}
		nNumofMarks = 0;
		if (caYAxis.mdValueTo >= dYMarkStart)	{
			nNumofMarks = (int)((caYAxis.mdValueTo - dYMarkStart)/caYAxis.mdMarkInterval) + 1;
		}
		nNumofSigDig = 32;
		if (caYAxis.mdValueTo > caYAxis.mdValueFrom)	{
			nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(caYAxis.mdValueFrom), Math.abs(caYAxis.mdValueTo))
					/(caYAxis.mdValueTo - caYAxis.mdValueFrom)*(nNumofMarks + 1))) + 1;
		}
		if (nNumofSigDig > 32)	{
			nNumofSigDig = 32;
		}
		caYAxis.marraydScaleMarks = new double[nNumofMarks];
		caYAxis.marraystrMarkTxts = new String[nNumofMarks];
		for (int idx = 0; idx < nNumofMarks; idx ++)	{
			caYAxis.marraydScaleMarks[idx] = dYMarkStart + idx * caYAxis.mdMarkInterval;
            // use bigdecimal instead of MFPNumeric so that toString can output something like 2.1e30
			caYAxis.marraystrMarkTxts[idx] = new BigDecimal(caYAxis.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
		}
		
		caZAxis.mdValueFrom = mp3OriginInFROM.getZ() - mdZAxisLenInFROM/2.0;
		caZAxis.mdValueTo = caZAxis.mdValueFrom + mdZAxisLenInFROM;
		caZAxis.mp3From = mp3OriginInFROM.subtract(new Position3D(0, 0, mdZAxisLenInFROM/2.0));
		caZAxis.mp3To = mp3OriginInFROM.add(new Position3D(0, 0, mdZAxisLenInFROM/2.0));
		caZAxis.mclr = this.mcolorForeGrnd;
		caZAxis.mstrAxisName = mstrZAxisName;
		caZAxis.mdMarkInterval = axisMarks.mdZMark2 - axisMarks.mdZMark1;
		// cannot directly convert to int because mdZMark1 - caZAxis.mdValueFrom may overflow int.
		mfpNumIntTmp = new MFPNumeric((axisMarks.mdZMark1 - caZAxis.mdValueFrom)/caZAxis.mdMarkInterval).toIntOrNanInfMFPNum();
		double dZMarkStart = axisMarks.mdZMark1 - mfpNumIntTmp.multiply(new MFPNumeric(caZAxis.mdMarkInterval)).doubleValue();
		if (dZMarkStart < caZAxis.mdValueFrom)	{
			dZMarkStart += caZAxis.mdMarkInterval;
		}
		nNumofMarks = 0;
		if (caZAxis.mdValueTo >= dZMarkStart)	{
			nNumofMarks = (int)((caZAxis.mdValueTo - dZMarkStart)/caZAxis.mdMarkInterval) + 1;
		}
		nNumofSigDig = 32;
		if (caZAxis.mdValueTo > caZAxis.mdValueFrom)	{
			nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(caZAxis.mdValueFrom), Math.abs(caZAxis.mdValueTo))
					/(caZAxis.mdValueTo - caZAxis.mdValueFrom)*(nNumofMarks + 1))) + 1;
		}
		if (nNumofSigDig > 32)	{
			nNumofSigDig = 32;
		}
		caZAxis.marraydScaleMarks = new double[nNumofMarks];
		caZAxis.marraystrMarkTxts = new String[nNumofMarks];
		for (int idx = 0; idx < nNumofMarks; idx ++)	{
			caZAxis.marraydScaleMarks[idx] = dZMarkStart + idx * caZAxis.mdMarkInterval;
            // use bigdecimal instead of MFPNumeric so that toString can output something like 2.1e30
			caZAxis.marraystrMarkTxts[idx] = new BigDecimal(caZAxis.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
		}
		
		AllCoordAxis allCoordAxis = new AllCoordAxis();
		allCoordAxis.mcaXAxis = caXAxis;
		allCoordAxis.mcaYAxis = caYAxis;
		allCoordAxis.mcaZAxis = caZAxis;
		return allCoordAxis;
	}
	
    public CoordAxis getOppositeAxis(CoordAxis axisFrom)    {
        CoordAxis axisTo = new CoordAxis();
		axisTo.mdValueFrom = axisFrom.mdValueFrom;
		axisTo.mdValueTo = 2 * axisFrom.mdValueFrom - axisFrom.mdValueTo;
		axisTo.mp3From = axisFrom.mp3From;
		axisTo.mp3To = axisFrom.mp3From.add(axisFrom.mp3From).subtract(axisFrom.mp3To);
		axisTo.mclr = axisFrom.mclr;
		axisTo.mstrAxisName = axisFrom.mstrAxisName;
		axisTo.mdMarkInterval = axisFrom.mdMarkInterval;
        int nNumofMarksInFrom = axisFrom.marraydScaleMarks.length;
        if (nNumofMarksInFrom == 0) {
            axisTo.marraydScaleMarks = new double[0];
            axisTo.marraystrMarkTxts = new String[0];
        } else  {

            // cannot directly convert to int because mdZMark1 - mcaZAxis.mdValueFrom may overflow int.
            MFPNumeric mfpNumIntTmp = new MFPNumeric((axisFrom.marraydScaleMarks[0] - axisTo.mdValueFrom)/axisTo.mdMarkInterval).toIntOrNanInfMFPNum();
            double dMarkStart = axisFrom.marraydScaleMarks[0] - mfpNumIntTmp.multiply(new MFPNumeric(axisTo.mdMarkInterval)).doubleValue();
            while (dMarkStart > axisTo.mdValueFrom)	{
                dMarkStart -= axisTo.mdMarkInterval;
            }
            int nNumofMarks = 0;
            if (axisTo.mdValueTo <= dMarkStart)	{
                nNumofMarks = (int)((dMarkStart - axisTo.mdValueTo)/axisTo.mdMarkInterval) + 1;
            }
            int nNumofSigDig = 32;
            if (axisTo.mdValueTo < axisTo.mdValueFrom)	{
                nNumofSigDig = (int) Math.ceil(Math.log10(Math.max(Math.abs(axisTo.mdValueFrom), Math.abs(axisTo.mdValueTo))
                        /(axisTo.mdValueFrom - axisTo.mdValueTo)*(nNumofMarks + 1))) + 1;
            }
            if (nNumofSigDig > 32)	{
                nNumofSigDig = 32;
            }
            axisTo.marraydScaleMarks = new double[nNumofMarks];
            axisTo.marraystrMarkTxts = new String[nNumofMarks];
            for (int idx = 0; idx < nNumofMarks; idx ++)	{
                axisTo.marraydScaleMarks[idx] = dMarkStart - idx * axisTo.mdMarkInterval;
                // use bigdecimal instead of MFPNumeric so that toString can output something like 2.1e30
                axisTo.marraystrMarkTxts[idx] = new BigDecimal(axisTo.marraydScaleMarks[idx], new MathContext(nNumofSigDig)).toString();
            }
        }
        return axisTo;
    }
    
	public void adjustXYZ1To1()	{
		// make sure X, Y and Z (which is Y axis to the user's view) scaling ratio equal.
        boolean bXHasMinScalingRatio = true, bYHasMinScalingRatio = false, bZHasMinScalingRatio = false;
        double dMinXYZScalingRatio = getScalingRatioX();
        if (getScalingRatioY() <= getScalingRatioZ()) {
            if (getScalingRatioY() <= getScalingRatioX())  {
                bXHasMinScalingRatio = false;
                bYHasMinScalingRatio = true;
                bZHasMinScalingRatio = false;
                dMinXYZScalingRatio = getScalingRatioY();
            }
        } else if (getScalingRatioZ() <= getScalingRatioX()) {
            bXHasMinScalingRatio = false;
            bYHasMinScalingRatio = false;
            bZHasMinScalingRatio = true;
            dMinXYZScalingRatio = getScalingRatioZ();
        }
        
        double dXFurtherScaling  = 1, dYFurtherScaling = 1, dZFurtherScaling = 1;
        double dXShift = 0, dYShift = 0, dZShift = 0;
		if (bXHasMinScalingRatio == false)	{
			double dNewXAxisLenInFROM = mdXAxisLenInTO/dMinXYZScalingRatio;
            dXFurtherScaling = getScalingRatioX()/dMinXYZScalingRatio;
            dXShift = (dNewXAxisLenInFROM - mdXAxisLenInFROM)/2.0;
		}
		if (bYHasMinScalingRatio == false)	{
			double dNewYAxisLenInFROM = mdYAxisLenInTO/dMinXYZScalingRatio;
            dYFurtherScaling = getScalingRatioY()/dMinXYZScalingRatio;
            dYShift = (dNewYAxisLenInFROM - mdYAxisLenInFROM)/2.0;
		}
        if (bZHasMinScalingRatio == false)	{
			double dNewZAxisLenInFROM = mdZAxisLenInTO/dMinXYZScalingRatio;
            dZFurtherScaling = getScalingRatioZ()/dMinXYZScalingRatio;
            dZShift = (dNewZAxisLenInFROM - mdZAxisLenInFROM)/2.0;
		}
        
        /* do not call 
         * updateMapperFROM2FROM(-dXShift, -dYShift, -dZShift, 0, 0, 0, dXFurtherScaling, dYFurtherScaling, dZFurtherScaling);
         * because now mp3OriginInFROM is in the middle of the axis.
         */
        updateMapperFROM2FROM(0, 0, 0, 0, 0, 0, dXFurtherScaling, dYFurtherScaling, dZFurtherScaling);
		update();
	}
	
	public Bitmap takeScreenSnapshot(GL10 gl)	{
		int width = (int) mdChartWindowWidth;
		int height = (int) mdChartWindowHeight;
	    int screenshotSize = width * height;
	    int pixelsBuffer[] = new int[screenshotSize];
	    try {
		    ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
		    bb.order(ByteOrder.nativeOrder());
		    // this statement does not work in the landscape mode of galaxy express. Seems that it cannot support so large height (lines).
		    //gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
		    gl.glReadPixels(0, 0, width, height/2, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
		    bb.asIntBuffer().get(pixelsBuffer);
		    gl.glReadPixels(0, height/2, width, height/2, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, bb);
		    int pixelsBufferBottom[] = new int[screenshotSize];
		    bb.asIntBuffer().get(pixelsBufferBottom);
		    for (int idx = screenshotSize/2; idx < screenshotSize; idx ++)	{
		    	pixelsBuffer[idx] = pixelsBufferBottom[idx - screenshotSize/2];
		    }
		    bb = null;
	    } catch (OutOfMemoryError  e)	{
	    	// ByteBuffer.allocateDirect(screenshotSize * 4); could be out of memory
	    	// in general, OutOfMemoryError should not be handled and developer should let JVM die.
	    	// however, in this case, we know what statement causes the out of memory error
	    	// and other threads are not affected. So it is ok to catch it.
	    }

	    for (int i = 0; i < screenshotSize; ++i) {
	        // The alpha and green channels' positions are preserved while the red and blue are swapped
	        pixelsBuffer[i] = ((pixelsBuffer[i] & 0xff00ff00)) | ((pixelsBuffer[i] & 0x000000ff) << 16) | ((pixelsBuffer[i] & 0x00ff0000) >> 16);
	    }

	    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    bitmap.setPixels(pixelsBuffer, screenshotSize-width, -width, 0, 0, width, height);
	    return bitmap;
	}
}