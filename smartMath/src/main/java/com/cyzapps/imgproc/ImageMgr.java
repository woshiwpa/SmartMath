package com.cyzapps.imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import com.cyzapps.imgmatrixproc.ImgMatrixConverter;
import com.cyzapps.mathrecog.ImageChop;
import com.cyzapps.mathrecog.UnitPrototypeMgr;
import com.cyzapps.mathrecog.UnitPrototypeMgr.UnitProtoType;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class ImageMgr {

	public static int[][] convertImg2ColorMatrix(Bitmap image) {
		int width = image.getWidth();
		int height = image.getHeight();
		int[] pix = new int[width * height];
		image.getPixels(pix, 0, width, 0, 0, width, height);
		int[][] result = new int[width][height];
	
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
                // do not use image.getRGB(x, y) directly because it includes alpha.
                int[] rgb = ImgMatrixConverter.convertInt2RGB(pix[row * width + col]);
                result[col][row] = ImgMatrixConverter.convertRGB2Int(rgb[0], rgb[1], rgb[2]);
			}
		}
	
		return result;
	}
	
	public static byte[][] convertImg2BiMatrix(Bitmap image)	{
		int[][] narrayColorMatrix = convertImg2ColorMatrix(image);
		return ImgMatrixConverter.convertColor2Bi(narrayColorMatrix);
	}
	
	public static Bitmap convertBiMatrix2Img(byte[][] biMatrix)	{
		if (biMatrix == null || biMatrix.length == 0 || biMatrix[0].length == 0)	{
			return null;	// we cannot create a zero width zero height bitmap.
		}
		return convertBiMatrix2Img(biMatrix, 0, 0, biMatrix.length, biMatrix[0].length);
	}
	
	public static Bitmap convertBiMatrix2Img(byte[][] biMatrix, int nLeft, int nTop, int nWidth, int nHeight)	{
		if (biMatrix == null || biMatrix.length == 0 || biMatrix[0].length == 0)	{
			return null;	// we cannot create a zero width zero height bitmap.
		}
		int width = Math.min(nWidth, biMatrix.length - nLeft);
		int height = Math.min(nHeight, biMatrix[0].length - nTop);
		int[] colorMatrix = new int[width * height];
		for (int idx = 0; idx < height; idx ++)	{
			for (int idx1 = 0; idx1 < width; idx1 ++)	{
				if (biMatrix[idx1 + nLeft][idx + nTop] == 0)	{
					colorMatrix[idx * width + idx1] = Color.WHITE;
				} else	{
					colorMatrix[idx * width + idx1] = Color.BLACK;
				}
			}
		}

		// Create bitmap
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
		// Set the pixels
		bitmap.setPixels(colorMatrix, 0, width, 0, 0, width, height);
		
		return bitmap;
	}
    
	public static Bitmap convertGrayMatrix2Img(int[][] grayMatrix)	{
		if (grayMatrix == null || grayMatrix.length == 0 || grayMatrix[0].length == 0)	{
			return null;	// we cannot create a zero width zero height bitmap.
		}
		return convertGrayMatrix2Img(grayMatrix, 0, 0, grayMatrix.length, grayMatrix[0].length);
	}
	
	public static Bitmap convertGrayMatrix2Img(int[][] grayMatrix, int nLeft, int nTop, int nWidth, int nHeight)	{
		if (grayMatrix == null || grayMatrix.length == 0 || grayMatrix[0].length == 0)	{
			return null;	// we cannot create a zero width zero height bitmap.
		}
		int width = Math.min(nWidth, grayMatrix.length - nLeft);
		int height = Math.min(nHeight, grayMatrix[0].length - nTop);
		int[] colorMatrix = new int[width * height];
		for (int idx = 0; idx < height; idx ++)	{
			for (int idx1 = 0; idx1 < width; idx1 ++)	{
				if (grayMatrix[idx1 + nLeft][idx + nTop] < 0)	{
                    grayMatrix[idx1 + nLeft][idx + nTop] = 0;
				} else if (grayMatrix[idx1 + nLeft][idx + nTop] > 255)	{
					grayMatrix[idx1 + nLeft][idx + nTop] = 255;
				}
				colorMatrix[idx * width + idx1] = 255*256*65536 + grayMatrix[idx1 + nLeft][idx + nTop] * 65793; // dont forget alpha
			}
		}

		// Create bitmap
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
		// Set the pixels
		bitmap.setPixels(colorMatrix, 0, width, 0, 0, width, height);
		
		return bitmap;
	}
	
	public static Bitmap convertColorMatrix2Img(int[][] colorMatrix)	{
		if (colorMatrix == null || colorMatrix.length == 0 || colorMatrix[0].length == 0)	{
			return null;	// we cannot create a zero width zero height bitmap.
		}
		return convertColorMatrix2Img(colorMatrix, 0, 0, colorMatrix.length, colorMatrix[0].length);
	}
	
	public static Bitmap convertColorMatrix2Img(int[][] colorMatrix, int nLeft, int nTop, int nWidth, int nHeight)	{
		if (colorMatrix == null || colorMatrix.length == 0 || colorMatrix[0].length == 0)	{
			return null;	// we cannot create a zero width zero height bitmap.
		}
		int width = Math.min(nWidth, colorMatrix.length - nLeft);
		int height = Math.min(nHeight, colorMatrix[0].length - nTop);
		int[] colorList = new int[width * height];
		for (int idx = 0; idx < height; idx ++)	{
			for (int idx1 = 0; idx1 < width; idx1 ++)	{
				colorList[idx * width + idx1] = 255*256*65536 + colorMatrix[idx1 + nLeft][idx + nTop]; // dont forget alpha
			}
		}

		// Create bitmap
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);		// to save memory, do not use ALPHA_8888
		// Set the pixels
		bitmap.setPixels(colorList, 0, width, 0, 0, width, height);
		
		return bitmap;
	}
	
	public static void saveImageChop(ImageChop chop, String strFileName)   {
		Bitmap img = ImageMgr.convertBiMatrix2Img(
	            chop.mbarrayImg,
	            chop.mnLeft,
	            chop.mnTop,
	            chop.mnWidth,
	            chop.mnHeight);
	    ImageMgr.saveImg(img, strFileName);
	
	}
	
	public static void saveImg(Bitmap bitmap, String strFilePathName)	{
		try {
		       FileOutputStream out = new FileOutputStream(strFilePathName);
		       bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
		       out.close();
		} catch (Exception e) {
		       e.printStackTrace();
		}
	}

    public static Bitmap readImg(AssetManager am, String strFilePathName) {
        BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;		// to save memory, do not use ALPHA_8888
		Bitmap bitmap = null;
		if (am == null)	{
			// read from external storage.
			bitmap = BitmapFactory.decodeFile(strFilePathName, options);
		} else	{
			// read from asset.
			InputStream is;
			try {
				is = am.open(strFilePathName);
				bitmap = BitmapFactory.decodeStream(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (bitmap != null)	{
			bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);	// an alternative way is set options.inMutable to true, but only for api >= 11.
		}
		return bitmap;
    }

    
    public static String getFont(String strProtoTypeFileName) {
        String strFileExtension = ".bmp";            
        String strFontPart = "";
        if (strProtoTypeFileName.length() > 4
                && strProtoTypeFileName
                    .substring(strProtoTypeFileName.length() - strFileExtension.length())
                    .equalsIgnoreCase(strFileExtension))  {
            strFontPart = strProtoTypeFileName.substring(0, strProtoTypeFileName.length() - strFileExtension.length());
        }
        return strFontPart;
    }
    
    public static String cvtProtoTypeFont2Folder(String strProtoTypeFont) {
        return "prototype_" + strProtoTypeFont + "_thinned";
    }    
    
}
