package com.cyzapps.adapter;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.cyzapps.SmartMath.ActivitySettings;
import com.cyzapps.SmartMath.R;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.Statement;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.ScriptAnalyzer.ScriptStatementException;
import com.cyzapps.Jmfp.Statement.Statement_function;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;

public class MFPAdapter {
	public static LinkedList<MFPKeyWordInfo> m_slMFPKeyWordInfo = new LinkedList<MFPKeyWordInfo>();
	public static LinkedList<InternalFuncInfo> m_slInternalFuncInfo = new LinkedList<InternalFuncInfo>();
	public static LinkedList<FunctionEntry> m_slFunctionSpace = new LinkedList<FunctionEntry>();
	public static LinkedList<String> m_slFailedFilePaths = new LinkedList<String>();
	public static LinkedList<Statement_function> m_slRedefinedFunction = new LinkedList<Statement_function>();
	
	public static final String STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION = "_lib";
	public static final String STRING_ASSET_SCRIPT_LIB_FOLDER = "predef_lib";
	public static final String STRING_ASSET_SCRIPT_MATH_LIB_FILE = "math.mfps";
	public static final String STRING_ASSET_SCRIPT_MISC_LIB_FILE = "misc.mfps";
	public static final String STRING_ASSET_SCRIPT_SIG_PROC_LIB_FILE = "sig_proc.mfps";
	public static final String STRING_ASSET_CHARTS_FOLDER_EXTENSION = "_lib";
	public static final String STRING_ASSET_CHARTS_FOLDER = "charts_lib";
	public static final String STRING_ASSET_CHART_EXAMPLE1_FILE = "chart_example1.mfpc";
	public static final String STRING_ASSET_CHART_EXAMPLE2_FILE = "chart_example2.mfpc";
	public static final String STRING_ASSET_ZIP_FILE = "assets.zip";
	public static final String STRING_ASSET_JMATHCMD_JAR_FILE = "JMathCmd.jar";
	public static final String STRING_ASSET_INTERNAL_FUNC_INFO_FILE = "InternalFuncInfo.txt";
	public static final String STRING_ASSET_ENGLISH_LANG_FOLDER = "en"; 
	public static final String STRING_ASSET_SCHINESE_LANG_FOLDER = "zh-CN"; 
	public static final String STRING_ASSET_TCHINESE_LANG_FOLDER = "zh-TW";
	public static final String STRING_ASSET_LANGUAGEINFO_FOLDER = "LanguageInfo";
	public static final String STRING_ASSET_MFP_KEY_WORDS_INFO_FILE = "MFPKeyWordsInfo.txt";

	public static final int INT_ASSET_PATH_MAX_CHILD_LEVEL = 32;	// assume asset path cannot be as deep as 32 level.
	
    public static final String STRING_PATH_DIVISOR = System.getProperty("file.separator");
	public static final int MAX_NUMBER_OF_OPEN_FILES = 2048;
	public static final long FD_EXPIRY_TIME = 3600000;

    public static MFPNumeric mmfpNumBigThresh = new MFPNumeric(100000000);
    public static MFPNumeric mmfpNumSmallThresh = new MFPNumeric("0.00000001"); //0.00000001 has to be accurately represented. So use string

	public static byte[] m_sbyteBuffer = new byte[32768];	//make it static so that save realloc time.
	
	public static void clear()
	{
		m_slMFPKeyWordInfo.clear();
		m_slInternalFuncInfo.clear();
		m_slFunctionSpace.clear();
		m_slFailedFilePaths.clear();
		m_slRedefinedFunction.clear();
	}
	
	public static boolean isEmpty()
	{
		if (m_slMFPKeyWordInfo.size() == 0 && m_slInternalFuncInfo.size() == 0 && m_slFunctionSpace.size() == 0
				&& m_slFailedFilePaths.size() == 0 && m_slRedefinedFunction.size() == 0)
			return true;
		return false;
	}
	
	public static boolean isFuncSpaceEmpty()
	{
		if (m_slFunctionSpace.size() == 0)
			return true;
		return false;
	}
	
	public static class FunctionEntry	{
		public String[] m_strarrayNameSpace = new String[0];
		public Statement_function m_sf = null;
		public int m_nStartStatementPos = -1;
		public int[] m_nlistHelpBlock = null;
		public String[] m_strLines = new String[0];
		public Statement[] m_sLines = new Statement[0];

		public boolean isSameFunction(Statement_function sf)	{
			if (m_sf.m_strFunctionName.equals(sf.m_strFunctionName))	{
				if (m_sf.m_strParams.length == sf.m_strParams.length)	{
					if (sf.m_strParams.length == 0)	{
						return true;
					} else if (sf.m_strParams.length > 0
							&& m_sf.m_strParams[m_sf.m_strParams.length - 1].equals("opt_argv")
							&& sf.m_strParams[sf.m_strParams.length - 1].equals("opt_argv"))	{
						return true;
					} else if (sf.m_strParams.length > 0
							&& !m_sf.m_strParams[m_sf.m_strParams.length - 1].equals("opt_argv")
							&& !sf.m_strParams[sf.m_strParams.length - 1].equals("opt_argv"))	{
						return true;
					}
				}
			}
			return false;
		}
		public boolean matchFunction(String strFunctionName, int nNumofParams)	{
			if (m_sf.m_strFunctionName.equals(strFunctionName))	{
				if (m_sf.m_strParams.length == nNumofParams && !(m_sf.m_bIncludeOptParam))	{
					// make sure function name and the number of parameters match.
					return true;
				} else if (m_sf.m_strParams.length - 1 <= nNumofParams && m_sf.m_bIncludeOptParam)	{
					// if there are optional parameters.
					return true;
				}
			}
			return false;
		}
		
	}
	
	public static boolean isSameExternalFile(String strPath1, String strPath2)	{
		String[] strarrayNameSpace1 = cvtPath2NameSpace(strPath1);
		String[] strarrayNameSpace2 = cvtPath2NameSpace(strPath2);
		return isSameExternalFile(strarrayNameSpace1, strarrayNameSpace2);
	}
	public static boolean isSameExternalFile(String[] strarrayNameSpace1, String[] strarrayNameSpace2)	{
		if (strarrayNameSpace1 == null)	{
			return false;
		}
		if (strarrayNameSpace2 == null)	{
			return false;
		}
		if (strarrayNameSpace1.length != strarrayNameSpace2.length)	{
			return false;
		}
		for (int i = 0; i < strarrayNameSpace1.length; i++)	{
			if (strarrayNameSpace1[i].equals(strarrayNameSpace2[i]) == false)	{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isExternalFileOrSubFile(String strPathParent, String strPathChild)	{
		String[] strarrayNameSpaceParent = cvtPath2NameSpace(strPathParent);
		String[] strarrayNameSpaceChild = cvtPath2NameSpace(strPathChild);
		return isExternalFileOrSubFile(strarrayNameSpaceParent, strarrayNameSpaceChild);
	}
	public static boolean isExternalFileOrSubFile(String[] strarrayNameSpaceParent, String[] strarrayNameSpaceChild)	{
		if (strarrayNameSpaceParent == null)	{
			return false;
		}
		if (strarrayNameSpaceChild == null)	{
			return false;
		}
		if (strarrayNameSpaceChild.length < strarrayNameSpaceParent.length)	{
			return false;
		}
		for (int i = 0; i < strarrayNameSpaceParent.length; i++)	{
			if (strarrayNameSpaceChild[i].equals(strarrayNameSpaceParent[i]) == false)	{
				return false;
			}
		}
		return true;
	}
	
	// note that this function is different from CvtPath2NameSpace in MFPFileManagerActivity
	// bcoz it has a m_sstrRootPath.
	public static String[] cvtPath2NameSpace(String strPath)	{
		String strRootPath = MFPFileManagerActivity.getScriptFolderFullPath();
		if (strRootPath == null)	{
			strRootPath = "";
		}

		if (strPath.length() < strRootPath.length())	{
			return null;
		}
		// assume m_sstrRootPath always not ended by "/"
		if (strPath.substring(0, strRootPath.length()).equals(strRootPath))	{
			strPath = strPath.substring(strRootPath.length());
			int i;
			for (i = 0; i < strPath.length(); i ++)	{
				if (strPath.substring(i, i + 1).equals(MFPFileManagerActivity.STRING_PATH_DIV) == false)	{
					break;
				}
			}
			strPath = strPath.substring(i);
			String[] strarray = strPath.split(MFPFileManagerActivity.STRING_PATH_DIV + "+");
			LinkedList<String> listNameSpace = new LinkedList<String>();
			for (i = 0; i < strarray.length; i ++)	{
				if (strarray[i].equals(".."))	{
					if (listNameSpace.isEmpty())	{
						// this means it is not a valid path inside root folder.
						return null;
					} else	{
						listNameSpace.removeLast();
					}
				} else if (strarray[i].equals(".") == false)	{
					listNameSpace.addLast(strarray[i]);
				}
			}
			String[] strarrayNameSpace = new String[listNameSpace.size()];
			int nSize = listNameSpace.size();
			for (i = 0; i < nSize; i ++)	{
				strarrayNameSpace[i] = listNameSpace.remove();
			}
			return strarrayNameSpace;
		}
		return null;	// this means it is not a valid path inside root folder.
	}
	
	// note that this function is different from CvtNameSpace2Path in MFPFileManagerActivity
	// bcoz it has a m_sstrRootPath.
	public static String cvtNameSpace2Path(String[] strarrayNameSpace)	{
		String strRootPath = MFPFileManagerActivity.getScriptFolderFullPath();
		if (strRootPath == null)	{
			strRootPath = "";
		}
		// assume m_sstrRootPath always not ended by "/"
		String strPath = strRootPath;
		for (int i = 0; i < strarrayNameSpace.length; i ++)	{
			strPath += MFPFileManagerActivity.STRING_PATH_DIV + strarrayNameSpace[i];
		}
		return strPath;
	}

	// this function returns file extension from file name.
	public static String getFileExtFromName(String strFileName)	{
		// always assume strFileName is not null and assume this is a file not folder
		int nExtInitIndex = strFileName.lastIndexOf(MFPFileManagerActivity.STRING_EXTENSION_INITIAL);
		int nDivLastIndex = strFileName.lastIndexOf(MFPFileManagerActivity.STRING_PATH_DIV);
		if (nExtInitIndex == -1 || nExtInitIndex <= nDivLastIndex)	{
			// no extension
			return "";
		} else {
			return strFileName.substring(nExtInitIndex);
		}
	}
	
	public static boolean copyAssetScripts2SD(AssetManager am, String strSrcPath, String strDestPath) {
	    String strAssetFiles[] = null;
	    boolean bReturnValue = true;
	    try {
	    	String strScriptExt = MFPFileManagerActivity.STRING_SCRIPT_EXTENSION;
	        if (strSrcPath.substring(strSrcPath.length() - strScriptExt.length())
	        		.toLowerCase(Locale.US).equals(strScriptExt)) {
	        	// this is a script.
	            if (copyAssetFile2SD(am, strSrcPath, strDestPath) == false)	{
					return false;
				}
	        } else if (strSrcPath.substring(strSrcPath.length() - STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION.length())
					.toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION))	{
	            File dir = new File(strDestPath);
	            if (!dir.exists())	{
	                if (!dir.mkdirs())	{
	                    return false;	// cannot create destination folder
					}
				}
		        strAssetFiles = am.list(strSrcPath);
	            for (int i = 0; i < strAssetFiles.length; ++i) {
	            	boolean bThisCpyReturn = copyAssetScripts2SD(am, strSrcPath + MFPFileManagerActivity.STRING_PATH_DIV + strAssetFiles[i],
								strDestPath + MFPFileManagerActivity.STRING_PATH_DIV + strAssetFiles[i]);
	            	if (!bThisCpyReturn) {
	            		bReturnValue = false;
	            	}
	            }
	        }
	    } catch (IOException ex) {
	        return false;
	    }
		return bReturnValue;
	}

	public static boolean copyAssetCharts2SD(AssetManager am, String strSrcPath, String strDestPath) {
	    String strAssetFiles[] = null;
	    boolean bReturnValue = true;
	    try {
	    	String strScriptExt = MFPFileManagerActivity.STRING_CHART_EXTENSION;
	        if (strSrcPath.substring(strSrcPath.length() - strScriptExt.length())
	        		.toLowerCase(Locale.US).equals(strScriptExt)
	        	&& (strSrcPath.equals(STRING_ASSET_CHARTS_FOLDER
	        						+ MFPFileManagerActivity.STRING_PATH_DIV
	        						+ STRING_ASSET_CHART_EXAMPLE1_FILE)
	        		|| strSrcPath.equals(STRING_ASSET_CHARTS_FOLDER
    						+ MFPFileManagerActivity.STRING_PATH_DIV
    						+ STRING_ASSET_CHART_EXAMPLE2_FILE))) {
	        	// this is a chart.
	            if (copyAssetFile2SD(am, strSrcPath, strDestPath) == false)	{
					return false;
				}
	        } else if (strSrcPath.substring(strSrcPath.length() - STRING_ASSET_CHARTS_FOLDER_EXTENSION.length())
					.toLowerCase(Locale.US).equals(STRING_ASSET_CHARTS_FOLDER_EXTENSION))	{
	            File dir = new File(strDestPath);
	            if (!dir.exists())	{
	                if (!dir.mkdirs())	{
	                    return false;	// cannot create destination folder
					}
				}
		        strAssetFiles = am.list(strSrcPath);
	            for (int i = 0; i < strAssetFiles.length; ++i) {
	            	boolean bThisCpyReturn = copyAssetCharts2SD(am, strSrcPath + MFPFileManagerActivity.STRING_PATH_DIV + strAssetFiles[i],
								strDestPath + MFPFileManagerActivity.STRING_PATH_DIV + strAssetFiles[i]);
	            	if (!bThisCpyReturn) {
	            		bReturnValue = false;
	            	}
	            }
	        }
	    } catch (IOException ex) {
	        return false;
	    }
		return bReturnValue;
	}

	public static boolean copyAsset2SD(AssetManager am, String strSrcPath, String strDestPath, int nIterLevel) {
		nIterLevel++;
		if (nIterLevel > INT_ASSET_PATH_MAX_CHILD_LEVEL)	{
			// prevent stack overflow exception here
			return false;
		}
	    String strAssetFiles[] = null;
	    boolean bReturnValue = true;
	    try {
	    	strAssetFiles = am.list(strSrcPath);
	    	if (strAssetFiles.length == 0) {
	        	// this is a file but not the .jar binary file.
	    		if (strSrcPath.equalsIgnoreCase(STRING_ASSET_JMATHCMD_JAR_FILE) == false)	{
		            if (copyAssetFile2SD(am, strSrcPath, strDestPath) == false)	{
						return false;
					}
	    		}
	        } else 	{
	        	// this is a folder
	            File dir = new File(strDestPath);
	            if (!dir.exists())	{
	                if (!dir.mkdirs())	{
	                    return false;	// cannot create destination folder
					}
				}
	            for (int i = 0; i < strAssetFiles.length; ++i) {
	            	String strChildSrcPath = strAssetFiles[i];
	            	if (strSrcPath.equals("") == false)	{
	            		strChildSrcPath = strSrcPath + MFPFileManagerActivity.STRING_PATH_DIV + strAssetFiles[i];
	            	} else	{
	            		if (strChildSrcPath.trim().equals(""))	{
	            			// this situation might occur since user does report stack overflow.
	            			// but this might be a feature of new Android OS.
	            			return false;
	            		}
	            	}
	            	boolean bThisCpyReturn = copyAsset2SD(am, strChildSrcPath,
								strDestPath + MFPFileManagerActivity.STRING_PATH_DIV + strAssetFiles[i],
								nIterLevel);
	            	if (!bThisCpyReturn) {
	            		bReturnValue = false;
	            	}
	            }
	        }
	    } catch (IOException ex) {
	        return false;
	    }
		return bReturnValue;
	}

	public static boolean copyAssetFile2SD(AssetManager am, String strSrcPath, String strDestPath) {

	    InputStream in = null;
	    OutputStream out = null;
	    try {
	        in = am.open(strSrcPath);
	        out = new FileOutputStream(strDestPath);

	        int read;
	        while ((read = in.read(m_sbyteBuffer)) != -1) {
	            out.write(m_sbyteBuffer, 0, read);
	        }
	        in.close();
	        in = null;
	        out.flush();
	        out.close();
	        out = null;
			return true;
	    } catch (Exception e) {
	        return false;
	    }

	}
	
	public static void getPreDefLibFiles(AssetManager am, String strPath, LinkedList<String> listFilePathLib)
	{
		String[] strSubFolderOrFiles; 
		try	{
			strSubFolderOrFiles = am.list(strPath);
		} catch(IOException e)	{
			strSubFolderOrFiles = new String[0];
		}
		for (int index = 0; index < strSubFolderOrFiles.length; index ++)	{
			String strThisChild = strSubFolderOrFiles[index];
			String strLeafPath = strPath + MFPFileManagerActivity.STRING_PATH_DIV + strThisChild;
			if (strThisChild.length() >= STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION.length()
					&& strThisChild.substring(strThisChild.length() - STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION.length())
						.toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_LIB_FOLDER_EXTENSION))	{
				// this is a lib
				getPreDefLibFiles(am, strLeafPath, listFilePathLib);
			} else if (strThisChild.toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_MATH_LIB_FILE)
	        		|| strThisChild.toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_MISC_LIB_FILE)
	        		|| strThisChild.toLowerCase(Locale.US).equals(STRING_ASSET_SCRIPT_SIG_PROC_LIB_FILE))	{
				// this is a script file, and it is math.mfps or misc.mfps or sig_proc.mfps
        		listFilePathLib.addLast(strLeafPath);
			}
		}
	}
	
	public static void getUsrDefLibFiles(String strPath, LinkedList<String> listFilePathLib)
	{
		String[] strSubFolderOrFiles;
		File directory = new File(strPath);
		strSubFolderOrFiles = directory.list();
		if (strSubFolderOrFiles == null)	{
			strSubFolderOrFiles = new String[0];
		}
		for (int index = 0; index < strSubFolderOrFiles.length; index ++)	{
			if (strSubFolderOrFiles[index].equals(MFPFileManagerActivity.STRING_SCRIPT_EXAMPLE_FOLDER))	{
				continue;	//do not load example folder.
			}
			String strLeafPath = strPath + MFPFileManagerActivity.STRING_PATH_DIV
								+ strSubFolderOrFiles[index];
			File leafFile = new File(strLeafPath);
			if (leafFile.isDirectory())	{
				// this is a Directory
				getUsrDefLibFiles(strLeafPath, listFilePathLib);
			} else if (strLeafPath.substring(strLeafPath.length() - MFPFileManagerActivity.STRING_SCRIPT_EXTENSION.length())
								.toLowerCase(Locale.US).equals(MFPFileManagerActivity.STRING_SCRIPT_EXTENSION))	{
				// this is a script file
				listFilePathLib.addLast(strLeafPath);
			}
		}
	}
	
	
	public static void loadLib(LinkedList<String> listFilePathLib, AssetManager am)	{
		ListIterator<String> itrFilePath = listFilePathLib.listIterator();
		while (itrFilePath.hasNext())	{
			String strFilePath = itrFilePath.next();
			loadFile(strFilePath, am);
		}
	}

	public static int unloadFileOrFolder(String strPath)	{
		// unloaded file should not be internal lib file (predefined lib).
		// return the number of functions unloaded.
		String[] strarrayNameSpace = cvtPath2NameSpace(strPath);
		int nNumofUnloadedFunc = 0;
		for (int i = 0; i < m_slFunctionSpace.size(); i ++)	{
			FunctionEntry fe = m_slFunctionSpace.get(i);
			// internal file name-space saved in function entry is null, so that isExternalFileOrSubFile is always return false.
			if (isExternalFileOrSubFile(strarrayNameSpace, fe.m_strarrayNameSpace))	{
				m_slFunctionSpace.remove(i);
				i--;
				nNumofUnloadedFunc ++;
			}
		}
		return nNumofUnloadedFunc;
	}
	
	public static int loadFileOrFolder(String strPath, AssetManager am)	{
		int nNumofFailedLoading = 0;
	
		File f = new File(strPath);
		if (f.isDirectory())	{
            String[] strarrayChildren = f.list();
            for (int i = 0; i < strarrayChildren.length; i ++)	{
            	nNumofFailedLoading += loadFileOrFolder(strPath
            											+ MFPFileManagerActivity.STRING_PATH_DIV
														+ strarrayChildren[i], am);
            }
		} else	{
			if (loadFile(strPath, am) == false)	{
				nNumofFailedLoading ++;
			}
		}
		return nNumofFailedLoading;
	}
	
	public static boolean loadFile(String strFilePath, AssetManager am)	{
		String[] strarrayNameSpace = cvtPath2NameSpace(strFilePath);
		if (strarrayNameSpace != null && strarrayNameSpace.length > 0
				&& strarrayNameSpace[0].equals(MFPFileManagerActivity.STRING_SCRIPT_EXAMPLE_FOLDER))	{
			// this file is in example folder so that should not be loaded.
			return false;
		} else if (getFileExtFromName(strFilePath).toLowerCase(Locale.US)
				.equals(MFPFileManagerActivity.STRING_SCRIPT_EXTENSION) == false)	{
			// this file is not a script file.
			return false;
		}

		InputStream inputStream = null;
		if (am == null)	{
			// normal file
			try	{
				inputStream = new BufferedInputStream(new FileInputStream(strFilePath));
			} catch (FileNotFoundException e)	{
				// cannot find the file
				return false;
			}
		} else	{
			// asset file
			try {
				inputStream = am.open(strFilePath);
			} catch(IOException e)	{
				// cannot open the file.
				return false;
			}
		}
		
		// using this way instead of BufferedReader, accelerate reading speed.
		String strFileContent = "";
		ByteArrayOutputStream baos = null;
		try	{
			baos = new ByteArrayOutputStream();
		    int length = 0;
		    while ((length = inputStream.read(m_sbyteBuffer)) != -1) {
		        baos.write(m_sbyteBuffer, 0, length);
		    }
		    byte[] byteArray = baos.toByteArray();
		    strFileContent = new String(byteArray, "UTF-8");
		} catch (Exception  e)	{
			// System.out.println("Cannot read file " + f.getPath());
			m_slFailedFilePaths.addLast(strFilePath);
			return false;
		} finally {
			try	{
			    if (baos != null)	{
			    	baos.close();
			    }
			    inputStream.close();
			} catch (IOException e)	{
				
			}
		}
		
		String[] strSrcLines = strFileContent.split("\n");
		LinkedList<Statement> lAllStatements = new LinkedList<Statement>();
		LinkedList<Statement> lStatements = new LinkedList<Statement>();
		LinkedList<Integer> lStatementPos = new LinkedList<Integer>();
		LinkedList<int[]> lHelpBlocks = new LinkedList<int[]>();
		boolean bInHelpBlock = false;
		int nHelpBlockStart = 0;
		int nHelpBlockEnd = 0;
		int nLineNo = 1;
		Statement sCurrent = null;
		for (String strLine : strSrcLines){
			Statement sLine = new Statement(strLine, nLineNo);
			if (bInHelpBlock == false && sCurrent != null)	{
				// this statement needs to be appended to last one (only if we are not in help block)
				sCurrent.concatenate(sLine);
			} else	{
				sCurrent = sLine;
			}
			if (bInHelpBlock == false && sCurrent.isFinishedStatement() == false)	{
				nLineNo ++;
				continue;
			}
			try	{
				sCurrent.analyze();
			} catch(JMFPCompErrException e)	{
				sCurrent.m_eAnalyze = e;
			} catch (Exception e)   {
				sCurrent.m_eAnalyze = e;
            }
            if (bInHelpBlock == false)	{
                lAllStatements.add(sCurrent);
                if (sCurrent.m_statementType != null
                        && sCurrent.m_statementType.getType().equals("function"))	{
                    // the function defined in this file
                    lStatements.addLast(sCurrent);
                    lStatementPos.addLast(lAllStatements.size() - 1);
                    int[] nlistHelpBlock = null;
                    if (nHelpBlockStart > 0 && nHelpBlockEnd > 0)	{
                        nlistHelpBlock = new int[2];
                        nlistHelpBlock[0] = nHelpBlockStart;
                        nlistHelpBlock[1] = nHelpBlockEnd;
                        nHelpBlockStart = nHelpBlockEnd = 0;
                    }
                    lHelpBlocks.addLast(nlistHelpBlock);
                } else if (sCurrent.m_statementType != null
                        && sCurrent.m_statementType.getType().equals("help"))	{
                    bInHelpBlock = true;
                    nHelpBlockStart = nLineNo;
                    nHelpBlockEnd = 0;
                }
            } else if (sCurrent.m_statementType != null
                        && sCurrent.m_statementType.getType().equals("endh"))	{
                lAllStatements.add(sCurrent);
                bInHelpBlock = false;
                nHelpBlockEnd = nLineNo;
            }
			sCurrent = null;
			nLineNo ++;
		}

		Statement[] sSrcLines = lAllStatements.toArray(new Statement[0]);
		ListIterator<Statement> itrStatement = lStatements.listIterator();
		ListIterator<Integer> itrStatementPos = lStatementPos.listIterator();
		ListIterator<int[]> itrHelpBlock = lHelpBlocks.listIterator();
		while (itrStatement.hasNext())	{
			Statement s = itrStatement.next();
			int nStatementPos = itrStatementPos.next();
			int[] hb = itrHelpBlock.next();
			ListIterator<FunctionEntry> itrFE = m_slFunctionSpace.listIterator();
			boolean bDefined = false;
			/* should not check function redefinition considering the following case:
			 * file a has function f1() and file b has function f1() as well. Now if a
			 * is deleted, f1() can still be called because b's f1 is valid now. 
			 */
			/*
			while (itrFE.hasNext())	{
				if (itrFE.next().isSameFunction((Statement_function)(s.m_statementType)))	{
					//System.out.println("Redefined function " + ((Statement_function)(s.m_statementType)).m_strFunctionName
					//		+ " with " + ((Statement_function)(s.m_statementType)).m_strParams.length + " parameters.");
					m_slRedefinedFunction.addLast((Statement_function)(s.m_statementType));
					bDefined = true;
				}
			}*/
			FunctionEntry functionEntry = new FunctionEntry();
			if (am == null)	{
				functionEntry.m_strarrayNameSpace = cvtPath2NameSpace(strFilePath);
			} else	{
				functionEntry.m_strarrayNameSpace = null;
			}
			functionEntry.m_sf = (Statement_function)(s.m_statementType);
			functionEntry.m_nStartStatementPos = nStatementPos;
			functionEntry.m_strLines = strSrcLines;
			functionEntry.m_sLines = sSrcLines;
			functionEntry.m_nlistHelpBlock = hb;
			m_slFunctionSpace.addLast(functionEntry);
		}
		return true;	// means successful
	}
	
    public static FunctionEntry loadSession(String[] strlistSession) throws JMFPCompErrException    {
		LinkedList<Statement> lAllStatements = new LinkedList<Statement>();
        Statement sSessionStart = new Statement("function session_function()", 0);
        sSessionStart.analyze();
        lAllStatements.add(sSessionStart);
        boolean bInHelpBlock = false;
		Statement sCurrent = null;
        for (int idx = 0; idx < strlistSession.length; idx ++)  {
            Statement sLine = new Statement(strlistSession[idx], idx + 1);
            if (bInHelpBlock == false && sCurrent != null)	{
                // this statement needs to be appended to last one (only if not in a help block)
                sCurrent.concatenate(sLine);
            } else	{
                sCurrent = sLine;
            }
            if (bInHelpBlock == false && sCurrent.isFinishedStatement() == false)	{
                continue;
            }
            try	{
                sCurrent.analyze();
            } catch(JMFPCompErrException e)	{
                // analyzing might trigger some exceptions which should be ignored.
                sCurrent.m_eAnalyze = e;
            } catch (Exception e)   {
                // analyzing might trigger some exceptions which should be ignored.
                sCurrent.m_eAnalyze = e;
            }
            if (bInHelpBlock == false)	{
                lAllStatements.add(sCurrent);
                if (sCurrent.m_statementType != null
                        && sCurrent.m_statementType.getType().equals("help"))	{
                    bInHelpBlock = true;
                }
            } else if (sCurrent.m_statementType != null
                        && sCurrent.m_statementType.getType().equals("endh"))	{
                lAllStatements.add(sCurrent);
                bInHelpBlock = false;
            }
            sCurrent = null;
        }
        Statement sSessionEnd = new Statement("endf", strlistSession.length + 1);
        sSessionEnd.analyze();
        lAllStatements.add(sSessionEnd);

        FunctionEntry functionEntry = new FunctionEntry();
        functionEntry.m_strarrayNameSpace = new String[0];
        functionEntry.m_sf = (Statement_function)(sSessionStart.m_statementType);;
        functionEntry.m_nStartStatementPos = 0;
        functionEntry.m_strLines = strlistSession;
        functionEntry.m_sLines = lAllStatements.toArray(new Statement[0]);;
        functionEntry.m_nlistHelpBlock = new int[0];
        return functionEntry;
    }
    
	public static class InternalFuncInfo {

		public String mstrFuncName = "";
		public int mnLeastNumofParams = 0;
		public boolean mbOptParam = false;
		public String[] mstrlistHelpInfo = new String[0];
	}

	public static void loadInternalFuncInfo(AssetManager am, String strFilePath)	{
		InputStream inputStream = null;
		try {
			inputStream = am.open(strFilePath);
		} catch(IOException e)	{
			// cannot open the file.
			return;
		}
		// using this way instead of BufferedReader, accelerate reading speed.
		String strFileContent = "";
		ByteArrayOutputStream baos = null;
		try	{
			baos = new ByteArrayOutputStream();
		    int length = 0;
		    while ((length = inputStream.read(m_sbyteBuffer)) != -1) {
		        baos.write(m_sbyteBuffer, 0, length);
		    }
		    byte[] byteArray = baos.toByteArray();
		    strFileContent = new String(byteArray, "UTF-8");
		} catch (Exception  e)	{
			// System.out.println("Cannot read file " + f.getPath());
			return;
		} finally {
			try	{
			    if (baos != null)	{
			    	baos.close();
			    }
			    inputStream.close();
			} catch (IOException e)	{
				
			}
		}
		
		String[] strAllLines = strFileContent.split("\n");
		int nCurrentIdx = 0;
        while(nCurrentIdx < strAllLines.length && strAllLines[nCurrentIdx].trim().length() == 0) {
            nCurrentIdx ++; // remove all the blank lines in the very beginning.
        }
		InternalFuncInfo internalFuncInfoNew = null;
		while (nCurrentIdx < strAllLines.length){
			String strLine = strAllLines[nCurrentIdx];
			nCurrentIdx ++;
			String[] strlistFuncInfo = strLine.trim().split("\\s+");
			int nNumofParams = 0;
			if (strlistFuncInfo.length != 3 || strlistFuncInfo[0].equals(""))	{
				return;
			} else if (strlistFuncInfo[2].equalsIgnoreCase("true") == false
					&& strlistFuncInfo[2].equalsIgnoreCase("false") == false)	{
				return;
			} else	{
				try {
					nNumofParams = Integer.parseInt(strlistFuncInfo[1]);
				} catch (NumberFormatException e)	{
					return;
				}
				if (nNumofParams < 0)	{
					return;
				}
			}
			internalFuncInfoNew = new InternalFuncInfo();
			internalFuncInfoNew.mstrFuncName = strlistFuncInfo[0];
			internalFuncInfoNew.mnLeastNumofParams = nNumofParams;
			internalFuncInfoNew.mbOptParam = Boolean.parseBoolean(strlistFuncInfo[2]);
			
			boolean bInHelpBlock = false;
			String strHelpBlockTotal = "";
			while (nCurrentIdx < strAllLines.length)	{
				String strFollowingLine = strAllLines[nCurrentIdx];
				nCurrentIdx ++;
				if (strFollowingLine.trim().equalsIgnoreCase("help"))	{
					bInHelpBlock = true;
				} else if (strFollowingLine.trim().equalsIgnoreCase("endh"))	{
					strHelpBlockTotal += "\n" + strFollowingLine;
					internalFuncInfoNew.mstrlistHelpInfo = strHelpBlockTotal.split("\\n");
					break;
				}
				if (bInHelpBlock)	{
					if (strHelpBlockTotal.equals(""))	{
						strHelpBlockTotal = strFollowingLine;
					} else {
						strHelpBlockTotal += "\n" + strFollowingLine;
					}
				}
			}
			m_slInternalFuncInfo.add(internalFuncInfoNew);
		}
	}
	
	public static class MFPKeyWordInfo	{
		public String mstrKeyWordName = "";
		public Map<String, String> mmapHelpInfo = new HashMap<String, String>();
		
		public String extractHelpFromMFPKeyWordInfo(String strLang)	{
			String strLangLowerCase = strLang.trim().toLowerCase(Locale.US);
			String strHelp = mmapHelpInfo.get(strLangLowerCase);
			if (strHelp == null)	{
				strHelp = mmapHelpInfo.get("default");
				if (strHelp == null)	{
					strHelp = "";
				}
			}
			return strHelp;
		}
	}

	public static void loadMFPKeyWordsInfo(AssetManager am)	{
		InputStream inputStream = null;
		ByteArrayOutputStream baos = null;
	    String strAllInfo = "";
		byte[] byteArray = null;
		try	{
			inputStream = am.open(STRING_ASSET_MFP_KEY_WORDS_INFO_FILE);
			baos = new ByteArrayOutputStream();
		    int length = 0;
		    while ((length = inputStream.read(m_sbyteBuffer)) != -1) {
		        baos.write(m_sbyteBuffer, 0, length);
		    }
		    byteArray = baos.toByteArray();
		    strAllInfo = new String(byteArray, "UTF-8");
		} catch	(Exception e)	{
			
		} finally	{
			try	{
				if (baos != null)	{
					baos.close();
				}
				if (inputStream != null)	{
					inputStream.close();
				}
			} catch (IOException e)	{
				
			}
		}
	    
		String[] strItemInfos = strAllInfo.split("\n\u25C0\u25C0\u25C0\u25C0");
		for (String str: strItemInfos)	{
			String strThisItem = str.trim();
			if (strThisItem.length() == 0)	{
				continue;
			}
			String[] strThisItemInfos = strThisItem.split("\u25B6\u25B6\u25B6\u25B6\n");
			if (strThisItemInfos.length != 2)	{
				continue;
			}
			String strThisItemTitle = strThisItemInfos[0].trim();
			String[] strThisItemContents = strThisItemInfos[1].trim().split("\u25C1\u25C1\u25C1\u25C1");
			Map<String, String> mapAllLangsInfo = new HashMap<String, String>();
			String strDefault = "";
			for (int idx = 0; idx < strThisItemContents.length; idx ++)	{
				String strItr = strThisItemContents[idx];
				String[] strarrayTmp = strItr.split("\u25B7\u25B7\u25B7\u25B7\n");
				if (strarrayTmp.length != 2)	{
					continue;
				}
				String strLang = strarrayTmp[0].trim().toLowerCase(Locale.US);
				mapAllLangsInfo.put(strLang, strarrayTmp[1]);
				if (strDefault.length() == 0 || strLang.compareToIgnoreCase("English") == 0)	{
					strDefault = strarrayTmp[1];
				}
			}
			mapAllLangsInfo.put("default", strDefault);

			String[] strarrayKeyWords = strThisItemTitle.split("_");
			for (String strItr : strarrayKeyWords)	{
				String strKeyWord = strItr.trim();
				if (strKeyWord.length() != 0)	{
					MFPKeyWordInfo mfpKeyWordInfo = new MFPKeyWordInfo();
					mfpKeyWordInfo.mstrKeyWordName = strKeyWord;
					mfpKeyWordInfo.mmapHelpInfo = mapAllLangsInfo;
					m_slMFPKeyWordInfo.add(mfpKeyWordInfo);
				}
			}
			
		}
	}
	
	// get help information for all the functions having the same name.
	public static String getMFPKeyWordHelp(String strMFPKeyWord, String strLang)	{
		for (MFPKeyWordInfo itr : m_slMFPKeyWordInfo)	{
			if (itr.mstrKeyWordName.equalsIgnoreCase(strMFPKeyWord))	{
				return itr.extractHelpFromMFPKeyWordInfo(strLang);
			}
		}
		return null;	// this is not a keyword with help info.
	}
	
	public static boolean msbIsReloadingAll = false;

	public static boolean canReloadAll()	{
		return (msbIsReloadingAll == false);
	}
	
	// the following functions is reloading libs or at activity reloading
    public static void reloadAll(final Context context, int nReloadingMode, final String strToastInfo)	{
		
		// functions haven't been loaded
		if (nReloadingMode == 1)	{	// asynchronous mode with progress dialog shown.
			final ProgressDialog dlgInitProgress = ProgressDialog.show(context, context.getString(R.string.please_wait),
																	context.getString(R.string.loading_user_defined_libs), true);
			final Handler handler = new Handler();
			
			Thread threadReload = new Thread(new Runnable()	{

				@Override
				public void run() {
					msbIsReloadingAll = true;
					
					MFPAdapter.clear();

					AssetManager am = context.getAssets();
									
			        LinkedList<String> listLibFilePaths = new LinkedList<String>();
					MFPAdapter.getUsrDefLibFiles(MFPFileManagerActivity.getScriptFolderFullPath(), listLibFilePaths);
					MFPAdapter.loadLib(listLibFilePaths, null);	// load user defined lib.
					handler.post(new Runnable()	{
						@Override
						public void run() {
							dlgInitProgress.setMessage(context.getString(R.string.loading_pre_defined_libs));
						}
					});
					
					listLibFilePaths.clear();
					MFPAdapter.getPreDefLibFiles(am, MFPAdapter.STRING_ASSET_SCRIPT_LIB_FOLDER, listLibFilePaths);
					MFPAdapter.loadLib(listLibFilePaths, am);	// load developer defined lib.
					MFPAdapter.loadInternalFuncInfo(am, MFPAdapter.STRING_ASSET_INTERNAL_FUNC_INFO_FILE);
					MFPAdapter.loadMFPKeyWordsInfo(am);
					handler.post(new Runnable()	{
						@Override
						public void run() {
							dlgInitProgress.dismiss();
							if (strToastInfo != null && strToastInfo.trim().equals("") == false)	{
								int duration = Toast.LENGTH_SHORT;
								Toast toast = Toast.makeText(context, strToastInfo, duration);
								toast.show();
							}
						}
					});
					
					msbIsReloadingAll = false;
				}
			});
			threadReload.start();
		} else if (nReloadingMode == 0)	{	// asynchronous mode without progress dialog shown
			final Handler handler = new Handler();
			Thread threadReload = new Thread(new Runnable()	{
				
				@Override
				public void run() {
					msbIsReloadingAll = true;

					MFPAdapter.clear();

					AssetManager am = context.getAssets();
									
			        LinkedList<String> listLibFilePaths = new LinkedList<String>();
					MFPAdapter.getUsrDefLibFiles(MFPFileManagerActivity.getScriptFolderFullPath(), listLibFilePaths);
					MFPAdapter.loadLib(listLibFilePaths, null);	// load user defined lib.
					
					listLibFilePaths.clear();
					MFPAdapter.getPreDefLibFiles(am, MFPAdapter.STRING_ASSET_SCRIPT_LIB_FOLDER, listLibFilePaths);
					MFPAdapter.loadLib(listLibFilePaths, am);	// load developer defined lib.
					MFPAdapter.loadInternalFuncInfo(am, MFPAdapter.STRING_ASSET_INTERNAL_FUNC_INFO_FILE);
					MFPAdapter.loadMFPKeyWordsInfo(am);
					handler.post(new Runnable()	{
						@Override
						public void run() {
							if (strToastInfo != null && strToastInfo.trim().equals("") == false)	{
								int duration = Toast.LENGTH_SHORT;
								Toast toast = Toast.makeText(context, strToastInfo, duration);
								toast.show();
							}
						}
					});
					
					msbIsReloadingAll = false;
				}
			});
			threadReload.start();
		} else	{// synchronous mode
			msbIsReloadingAll = true;

			MFPAdapter.clear();
			
			AssetManager am = context.getAssets();
							
	        LinkedList<String> listLibFilePaths = new LinkedList<String>();
			MFPAdapter.getUsrDefLibFiles(MFPFileManagerActivity.getScriptFolderFullPath(), listLibFilePaths);
			MFPAdapter.loadLib(listLibFilePaths, null);	// load user defined lib.
			
			listLibFilePaths.clear();
			MFPAdapter.getPreDefLibFiles(am, MFPAdapter.STRING_ASSET_SCRIPT_LIB_FOLDER, listLibFilePaths);
			MFPAdapter.loadLib(listLibFilePaths, am);	// load developer defined lib.
			MFPAdapter.loadInternalFuncInfo(am, MFPAdapter.STRING_ASSET_INTERNAL_FUNC_INFO_FILE);
			MFPAdapter.loadMFPKeyWordsInfo(am);
			if (strToastInfo != null && strToastInfo.trim().equals("") == false)	{
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, strToastInfo, duration);
				toast.show();
			}
			msbIsReloadingAll = false;
		}
	}

	// get help information from a specific help block.
	public static String extractHelpFromBlock(String[] strLines, int nStartLine, int nEndLine, String strLang)	{
		String strReturn = "";
		String strDefault = "";
		boolean bLanguageFound = false;
		if (nStartLine > 0 && nStartLine < strLines.length
				&& nEndLine > 0 && nEndLine <= strLines.length
				&& nStartLine < nEndLine)	{
			boolean bInSubBlock = false;
			boolean bInProperSubBlock = false;
			boolean bInDefaultSubBlock = false;
			for (int index = nStartLine; index < nEndLine - 1; index++)	{
				// means this part of block is for all the language
				String strLine = strLines[index];
				String strTrimLine = strLine.trim();
				if (strTrimLine.length() > 0 && strTrimLine.charAt(0) == '@')	{
					if (strTrimLine.compareToIgnoreCase("@end") == 0)	{
						if (bInDefaultSubBlock == true)	{
							bInDefaultSubBlock = false;
						}
						if (bInProperSubBlock == true)	{
							bInProperSubBlock = false;
						}
						if (bInSubBlock == true)	{
							bInSubBlock = false;
						}
					} else if (strTrimLine.length() >= "@language:".length())	{
						if (strTrimLine.compareToIgnoreCase("@language:") == 0)	{
							bInDefaultSubBlock = true;
						}
						if (strTrimLine.compareToIgnoreCase("@language:" + strLang) == 0)	{
							bLanguageFound = true;
							bInProperSubBlock = true;
						}
						bInSubBlock = true;
					}
				} else {
					if (bInSubBlock == false)	{
						strReturn += strLines[index] + "\n";
						strDefault += strLines[index] + "\n";
					}
					if (bInProperSubBlock)	{
						strReturn += strLines[index] + "\n";
					}
					if (bInDefaultSubBlock)	{
						strDefault += strLines[index] + "\n";
					}
				}
			}
		}
		
		if (bLanguageFound == false)	{
			return strDefault;
		} else	{
			return strReturn;
		}
	}
	
	// get help information for the function.
	public static String getFunctionHelp(String strFuncName, int nNumofParam, boolean bIncludeOpt, String strLang)	{
		ListIterator<FunctionEntry> itrFE = m_slFunctionSpace.listIterator();
		String strReturn = "";
		boolean bFunctionFound = false;
		while (itrFE.hasNext())	{
			FunctionEntry fe = itrFE.next();
			if (fe.m_sf.m_strFunctionName.equalsIgnoreCase(strFuncName)
					&& fe.m_sf.m_bIncludeOptParam == bIncludeOpt
					&& (bIncludeOpt?((fe.m_sf.m_strParams.length - 1) == nNumofParam)
							:(fe.m_sf.m_strParams.length == nNumofParam)))	{
				bFunctionFound = true;
				if (fe.m_nlistHelpBlock != null)	{
					strReturn = extractHelpFromBlock(fe.m_strLines,
													fe.m_nlistHelpBlock[0],
													fe.m_nlistHelpBlock[1],
													strLang);
					break;
				}
			}
		}
		if (bFunctionFound)	{
			return strReturn;
		}
		// function not found, search predefined function info.
		ListIterator<InternalFuncInfo> itrIFI = m_slInternalFuncInfo.listIterator();
		strReturn = "";
		while (itrIFI.hasNext())	{
			InternalFuncInfo ifi = itrIFI.next();
			if (ifi.mstrFuncName.equalsIgnoreCase(strFuncName)
					&& ifi.mbOptParam == bIncludeOpt
					&& ifi.mnLeastNumofParams == nNumofParam)	{
				bFunctionFound = true;
				strReturn = extractHelpFromBlock(ifi.mstrlistHelpInfo,
												1,
												ifi.mstrlistHelpInfo.length,
												strLang);
				break;
			}
		}
		if (bFunctionFound)	{
			return strReturn;
		} else {
			return null;
		}
	}
	
	// get help information for all the functions having the same name.
	public static String getFunctionHelp(String strFuncName, String strLang)	{
		ListIterator<FunctionEntry> itrFE = m_slFunctionSpace.listIterator();
		String strFuncDeclares = "";
		String strReturn = "";
		while (itrFE.hasNext())	{
			FunctionEntry fe = itrFE.next();
			if (fe.m_sf.m_strFunctionName.compareToIgnoreCase(strFuncName) == 0)	{
				String strFuncDeclare = "";
				if (fe.m_sf.m_bIncludeOptParam)	{
					strFuncDeclare += fe.m_sf.m_strFunctionName + "("
								+ (fe.m_sf.m_strParams.length - 1) + "...)";
				} else	{
					strFuncDeclare += fe.m_sf.m_strFunctionName + "("
							+ fe.m_sf.m_strParams.length + ")";						
				}
				if (strFuncDeclares.indexOf(strFuncDeclare) >= 0)	{
					// this function has been defined before, should not be included.
					continue;
				}
				strFuncDeclares += ":" + strFuncDeclare;
				strReturn += strFuncDeclare + " :\n";
				if (fe.m_nlistHelpBlock != null)	{
					strReturn += extractHelpFromBlock(fe.m_strLines,
												fe.m_nlistHelpBlock[0],
												fe.m_nlistHelpBlock[1],
												strLang);						
				}
			}
		}

		ListIterator<InternalFuncInfo> itrIFI = m_slInternalFuncInfo.listIterator();
		while (itrIFI.hasNext())	{
			InternalFuncInfo ifi = itrIFI.next();
			if (ifi.mstrFuncName.equalsIgnoreCase(strFuncName))	{
				String strFuncDeclare = ifi.mstrFuncName + "(" + ifi.mnLeastNumofParams;
				if (ifi.mbOptParam)	{
					strFuncDeclare += "...)";
				} else	{
					strFuncDeclare += ")";						
				}
				if (strFuncDeclares.indexOf(strFuncDeclare) >= 0)	{
					// this function has been defined before, should not be included.
					continue;
				}
				strFuncDeclares += ":" + strFuncDeclare;
				strReturn += strFuncDeclare + " :\n";
				strReturn += extractHelpFromBlock(ifi.mstrlistHelpInfo,
												1,
												ifi.mstrlistHelpInfo.length,
												strLang);
			}
		}
		return strReturn;
	}
	
	// return data recorded (string[0]) and data shown (string[1])
	public static String[] outputDatum(DataClass datumAnswer) throws JFCALCExpErrException	{
		datumAnswer.validateDataClass(); // prevent refer to itself
		
		String[] strarrayReturn = new String[2];
		String strAnswerRecorded = new String();
		String strAnswerShown = new String();
		
        /* try to convert a double value to integer if we can */
        if (datumAnswer.getDataType() == DATATYPES.DATUM_DOUBLE && datumAnswer.getDataValue().isActuallyInteger()) {
            datumAnswer.setDataValue(datumAnswer.getDataValue().toIntOrNanInfMFPNum(), DATATYPES.DATUM_INTEGER);
        }
		
		if (datumAnswer.getDataType() == DATATYPES.DATUM_NULL)
		{
			strAnswerShown = strAnswerRecorded = "NULL";
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_BOOLEAN)
		{
			/* If the answer is a boolean */
            if (datumAnswer.getDataValue().mType == MFPNumeric.Type.MFP_NAN_VALUE)  {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
            }
            else if (datumAnswer.getDataValue().isActuallyZero())
			{
				strAnswerShown = strAnswerRecorded = "FALSE";
			}
			else
			{
				strAnswerShown = strAnswerRecorded = "TRUE";
			}
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_INTEGER)
		{
			/* If the answer is an integer, assume integer is always in a range of long */
            if (datumAnswer.getDataValue().isNanOrInf()) {
                strAnswerShown = strAnswerRecorded = datumAnswer.getDataValue().toString();
            } else if (isVeryBigorSmallValue(datumAnswer.getDataValue())
                    && !datumAnswer.getDataValue().isActuallyZero())  { // 0 is also a very small value but should not use scientific notation
                Format format = null;
                if (ActivitySettings.msnBitsofPrecision != -1)   {
                    String strTmp = "";
                    for (int idx = 0; idx < ActivitySettings.msnBitsofPrecision; idx ++) {
                        strTmp += "#";
                    }
                    if (ActivitySettings.msnBitsofPrecision == 0)    {
                        format = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));	// otherwise, 0.5 may be output as 0,5 in spanish
                    } else  {
                        format = new DecimalFormat("0." + strTmp + "E0", new DecimalFormatSymbols(Locale.US));
                    }
                } else  {
                    format = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.US));
                }
                MFPNumeric mfpNumIntValue = datumAnswer.getDataValue().toIntOrNanInfMFPNum();
				strAnswerRecorded = strAnswerShown = format.format(mfpNumIntValue.toBigInteger());
			} else {
				strAnswerRecorded = strAnswerShown = datumAnswer.getDataValue().toIntOrNanInfMFPNum().toString();				
			}
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_DOUBLE)
		{
            Format format = null;
			if (datumAnswer.getDataValue().isNanOrInf()) {
                strAnswerShown = strAnswerRecorded = datumAnswer.getDataValue().toString();
            } else if (isVeryBigorSmallValue(datumAnswer.getDataValue())) {
                String strTmp = "";
                for (int idx = 0; idx < ActivitySettings.msnBitsofPrecision; idx ++) {
                    strTmp += "#";
                }
                if (ActivitySettings.msnBitsofPrecision != -1)   {
                    if (ActivitySettings.msnBitsofPrecision == 0)    {
                        format = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
                    } else  {
                        format = new DecimalFormat("0." + strTmp + "E0", new DecimalFormatSymbols(Locale.US));
                    }
                } else  {
                    format = new DecimalFormat("0.0E0", new DecimalFormatSymbols(Locale.US));
                }
				strAnswerRecorded = strAnswerShown = format.format(datumAnswer.getDataValue().toBigDecimal());
            }
            else
            {
                String strTmp = "";
                for (int idx = 0; idx < ActivitySettings.msnBitsofPrecision; idx ++) {
                    strTmp += "#";
                }
                if (ActivitySettings.msnBitsofPrecision != -1)   {
                    if (ActivitySettings.msnBitsofPrecision == 0)    {
                        format = new DecimalFormat("0", new DecimalFormatSymbols(Locale.US));
                    } else  {
                        String strValueString = datumAnswer.getDataValue().toString();
                        int decimalIndex = strValueString.indexOf( '.' );
                        int nNumofZerosB4SigDig = 0;
                        if (decimalIndex != -1)	{	// this means no decimal point, it is an integer
                        	int idx = 0;
                        	for (idx = 0; idx < strValueString.length(); idx ++)	{
                        		if (strValueString.charAt(idx) >= '1' && strValueString.charAt(idx) <= '9')	{
                        			break;	// siginificant digit start from here.
                        		}
                        	}
                        	if (idx > decimalIndex)	{
                        		nNumofZerosB4SigDig = idx - decimalIndex - 1;
                        	}
                        }
                        String strTmpZeroB4SigDig = "";
                        for (int idx = 0; idx < nNumofZerosB4SigDig; idx ++) {
                        	strTmpZeroB4SigDig += "#";
                        }
                        format = new DecimalFormat("0." + strTmpZeroB4SigDig + strTmp, new DecimalFormatSymbols(Locale.US));
                    }
                } else  {
                    format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
                }
				strAnswerRecorded = strAnswerShown = format.format(datumAnswer.getDataValue().doubleValue());
            }
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_COMPLEX)
		{
			boolean bImageNegative = false;
			if (datumAnswer.getImage().isActuallyNegative())	{	// this means NAN is always positive.
				bImageNegative = true;
			}
			String[] strOutputReal = outputDatum(datumAnswer.getRealDataClass());
			DataClass datumImage = datumAnswer.getImageDataClass();
			String strRealImageConn = "+";
			if (bImageNegative)	{
				datumImage.setDataValue(datumImage.getDataValue().negate(), datumImage.getDataType());
				strRealImageConn = "-";
			}
			String[] strOutputImage = outputDatum(datumImage);
			if (datumAnswer.getReal().isActuallyZero() && datumAnswer.getImage().isActuallyZero())	{
				strAnswerShown = strAnswerRecorded = "0";
			} else if (datumAnswer.getReal().isActuallyZero())	{
				if (strOutputImage[1].equals("1"))	{
					strAnswerRecorded = strAnswerShown = "i";
				} else if (datumAnswer.getImage().isNanOrInf()) {
                    // inf*i is not infi but nan + infi. So have to use infi here. Same as nani.
					strAnswerRecorded = strOutputImage[0] + "i";
					strAnswerShown = strOutputImage[1] + "i";
				} else	{
					strAnswerRecorded = strOutputImage[0] + " * i";
					strAnswerShown = strOutputImage[1] + " * i";
				}
				if (bImageNegative)	{
					strAnswerRecorded = strRealImageConn + strAnswerRecorded;
					strAnswerShown = strRealImageConn + strAnswerShown;
				}
			} else if (datumAnswer.getImage().isActuallyZero())	{
				strAnswerRecorded = strOutputReal[0];
				strAnswerShown = strOutputReal[1];
			} else if (datumAnswer.getImage().isNanOrInf()) {
                // inf*i is not infi but nan + infi. So have to use infi here. Same as nani.
				// for nani, need not to worry about strRealImageConn because it should be positive.
				strAnswerRecorded = strOutputReal[0] + " " + strRealImageConn + " " + strOutputImage[0] + "i";
				strAnswerShown = strOutputReal[1] + " " + strRealImageConn + " " + strOutputImage[1] + "i";
			} else	{
				strAnswerRecorded = strOutputReal[0] + " " + strRealImageConn + " " + strOutputImage[0] + " * i";
				strAnswerShown = strOutputReal[1] + " " + strRealImageConn + " " + strOutputImage[1] + " * i";
			}
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_REF_DATA)
		{
			strAnswerRecorded = strAnswerShown = "[";
			for (int index = 0; index < datumAnswer.getDataListSize(); index ++)
			{
				if (index == (datumAnswer.getDataListSize() - 1))
				{
					strAnswerRecorded += (datumAnswer.getDataList()[index] == null)?
							outputDatum(new DataClass())[0]
							:outputDatum(datumAnswer.getDataList()[index])[0];
					strAnswerShown += (datumAnswer.getDataList()[index] == null)?
							outputDatum(new DataClass())[1]
							:outputDatum(datumAnswer.getDataList()[index])[1];
				}
				else
				{
					strAnswerRecorded += (datumAnswer.getDataList()[index] == null)?
							outputDatum(new DataClass())[0]
							:outputDatum(datumAnswer.getDataList()[index])[0] + ", ";
					strAnswerShown += (datumAnswer.getDataList()[index] == null)?
							outputDatum(new DataClass())[1]
							:outputDatum(datumAnswer.getDataList()[index])[1] + ", ";
				}
			}
			strAnswerRecorded = strAnswerShown += "]";
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_STRING)
		{
			strAnswerShown = strAnswerRecorded = "\"" + datumAnswer.getStringValue() + "\"";
		}
		else if (datumAnswer.getDataType() == DATATYPES.DATUM_REF_FUNC)
		{
			strAnswerRecorded = datumAnswer.getFunctionName() + "()";
			strAnswerShown = "Function name: " + datumAnswer.getFunctionName();
		}
        else if (datumAnswer.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR)
        {
            strAnswerShown = strAnswerRecorded = datumAnswer.output();  // actually calls getAExpr().output().
        }
		strarrayReturn[0] = strAnswerRecorded;
		strarrayReturn[1] = strAnswerShown;
		return strarrayReturn;
	}
	
	public static String outputException(Exception e)	{
		String strOutput = new String();
		/* If there is an error */
		if (e instanceof JFCALCExpErrException)	{
			JFCALCExpErrException eExp = (JFCALCExpErrException)e;
			String strError = eExp.m_se.getErrorInfo();
			strOutput = String.format("%s\n", strError);
			if (eExp.m_strBlockName != null)	{
				String strTmp1 = new String();
				strTmp1 = String.format("In function %s :\n", eExp.m_strBlockName);
				String strTmp2 = outputException(eExp.m_exceptionLowerLevel);
				strOutput += strTmp1 + strTmp2;
			}
		} else if(e instanceof JMFPCompErrException)	{
			JMFPCompErrException eMFP = (JMFPCompErrException)e;
			String strTmp1 = new String();
			if (eMFP.m_se.m_nStartLineNo == eMFP.m_se.m_nEndLineNo)	{
				strTmp1 = String.format("\tLine %d : ", eMFP.m_se.m_nStartLineNo);
			} else	{
				strTmp1 = String.format("\tLines %d-%d : ", eMFP.m_se.m_nStartLineNo, eMFP.m_se.m_nEndLineNo);
			}
			String strError = eMFP.m_se.getErrorInfo();
			String strTmp2 = new String();
			strTmp2 = String.format("%s\n", strError);
			strOutput = outputException(eMFP.m_exceptionLowerLevel);
			strOutput = strTmp1 + strTmp2 + strOutput;
		} else if (e instanceof ScriptStatementException){
			ScriptStatementException eSSE = (ScriptStatementException)e;
			if (eSSE.m_statement.m_nStartLineNo == eSSE.m_statement.m_nEndLineNo)	{
				strOutput = String.format("\tLine %d : %s\n", eSSE.m_statement.m_nStartLineNo, e.toString());
			} else	{	
				strOutput = String.format("\tLines %d-%d : %s\n", eSSE.m_statement.m_nStartLineNo,
																eSSE.m_statement.m_nEndLineNo,
																e.toString());
			}
		} else if (e instanceof JSmartMathErrException) {
			JSmartMathErrException eSM = (JSmartMathErrException)e;
			String strError = eSM.m_se.getErrorInfo();
			strOutput = String.format("%s\n", strError);
			if (eSM.m_strBlockName != null)	{
				String strTmp1 = new String();
				strTmp1 = String.format("In function %s :\n", eSM.m_strBlockName);
				String strTmp2 = outputException(eSM.m_exceptionLowerLevel);
				strOutput += strTmp1 + strTmp2;
			}
		} else if (e != null)	{
			strOutput = String.format("%s\n", e.toString());
		}
		return strOutput;
	}

	public static boolean isVeryBigorSmallValue(MFPNumeric mfpNumValue)
	{
		if (ActivitySettings.msnBigSmallThresh < 0)
			return false;
		if ((mfpNumValue.compareTo(mmfpNumBigThresh) >= 0)
				|| (mfpNumValue.compareTo(mmfpNumBigThresh.negate()) <= 0)
				|| ((mfpNumValue.compareTo(mmfpNumSmallThresh)) <= 0)
	                && (mfpNumValue.compareTo(mmfpNumSmallThresh.negate()) >= 0)) {
			return true;
		} else {
			return false;
		}
	}
	
    
    public static double getPlotChartVariableFrom()
    {
        return ActivitySettings.msfSmCPlotVarFrom;
    }
    
    public static double getPlotChartVariableTo()
    {
        return ActivitySettings.msfSmCPlotVarTo;
    }
}
