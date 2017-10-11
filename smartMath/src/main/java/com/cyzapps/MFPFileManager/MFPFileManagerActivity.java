package com.cyzapps.MFPFileManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.cyzapps.SmartMath.ActivityShowHelp;
import com.cyzapps.SmartMath.R;
import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.Jfcalc.FuncEvaluator.FileOperator;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.PlotAdapter.FlatChart;
import com.cyzapps.PlotAdapter.FlatChartView;
import com.cyzapps.PlotAdapter.MFPChart;
import com.cyzapps.PlotAdapter.OGLChart;
import com.cyzapps.PlotAdapter.OGLChartOperator;
import com.cyzapps.PlotAdapter.OGLExprChartOperator;
import com.cyzapps.PlotAdapter.PolarChartOperator;
import com.cyzapps.PlotAdapter.PolarExprChartOperator;
import com.cyzapps.PlotAdapter.XYChartOperator;
import com.cyzapps.PlotAdapter.XYExprChartOperator;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MFPFileManagerActivity extends Activity {
	private static final int DIALOG_FILE_GENERATION = 1;
	private static final int DIALOG_FILE_RENAME = 2;
	private static final int DIALOG_FILE_DELETE = 3;

	private static final int ITEM0 = Menu.FIRST;
	private static final int ITEM1 = Menu.FIRST + 1;
	private static final int ITEM2 = Menu.FIRST + 2;
	private static final int ITEM3 = Menu.FIRST + 3;
	private static final int ITEM4 = Menu.FIRST + 4;
	private static final int ITEM5 = Menu.FIRST + 5;
	private static final int ITEM6 = Menu.FIRST + 6;
	private static final int ITEM7 = Menu.FIRST + 7;
	private static final int ITEM8 = Menu.FIRST + 8;


	public static final String CURRENT_PATH = "current_path";
	public static final String SELECTED_POSITION = "selected_position";
	
	public static final int INT_INVALID_POSITION = -1;
	
	public static final int COLOR_HIGHLIGHTED_BACKGND = 0xFFCC6666;

	public static final String STRING_PATH_DIV = System.getProperty("file.separator");
	public static final String STRING_EXTENSION_INITIAL = ".";
	public static final String STRING_APP_FOLDER = "SmartMath";
	public static final String STRING_CONFIG_FOLDER = "config";
	public static final String STRING_ASSETS_FOLDER = "assets";
	public static final String STRING_SCRIPT_FOLDER = "scripts";
	public static final String STRING_SCRIPT_EXAMPLE_FOLDER = "examples";
	public static final String STRING_CHART_FOLDER = "charts";
	public static final String STRING_CHART_SNAPSHOT_FOLDER = "chart_snaps";
	public static final String STRING_JMATHCMD_JAR_FILE = MFPAdapter.STRING_ASSET_JMATHCMD_JAR_FILE;
	public static final String STRING_ASSET_ZIP_FILE = MFPAdapter.STRING_ASSET_ZIP_FILE;
	public static final String STRING_FOLDER = "folder";
	public static final String STRING_UPPER_FOLDER = "upper_folder";
	public static final String STRING_SCRIPT_EXTENSION = ".mfps";
	public static final String STRING_CHART_EXTENSION = ".mfpc";
	public static final String STRING_BMP_EXTENSION = ".bmp";
	public static final String STRING_UNKNOWN_FILE = "unknown_file";
	
	private GridView mgridView = null;
	private String mstrCurrentPath = "";
	private String mstrRootPath = "";
	
	public static final int START_FILE_MANAGER_BY_ITSELF = 0;
	public static final int START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR = 1;
	public static final int START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR = 2;
	
	private int mnMode = START_FILE_MANAGER_BY_ITSELF;	//0 means main activity; 1 means open file (started by script editor);
							//2 means save file (started by script editor).
	private int mnSelectedFilePosition = INT_INVALID_POSITION;	//-1 means nothing selected.
	private MFPFileType mmfpClipboardFileType = null;	// null means no file cut or copied
	private String mstrClipBoardPath = "";
	private boolean mbClipBoardCut = false;	// we will cut the file or not.
	private MFPFileType mmfparrayFiles[] = null;
	private String[] mstrlistFileFilter = null;	// null means show all the file, 
	
	private AlertDialog malertNewFileDlg = null, malertFileRenameDlg = null, malertFileDeleteDlg = null; 
	public static class MFPFileOperator extends FileOperator	{

		@Override
		public boolean outputGraphFile(String strFileName, String strFileContent) throws IOException {
	        String strRootPath = AndroidStorageOptions.getSelectedStoragePath()
	        					+ MFPFileManagerActivity.STRING_PATH_DIV
	        					+ MFPFileManagerActivity.STRING_APP_FOLDER;
	        int nLastDivIdx = strFileName.lastIndexOf(MFPFileManagerActivity.STRING_PATH_DIV);
			File folder;
	        if (nLastDivIdx > 0)	{
		        folder = new File(strRootPath + MFPFileManagerActivity.STRING_PATH_DIV
									+ MFPFileManagerActivity.STRING_CHART_FOLDER
									+ MFPFileManagerActivity.STRING_PATH_DIV
									+ strFileName.substring(0, nLastDivIdx));
			} else	{
		        folder = new File(strRootPath + MFPFileManagerActivity.STRING_PATH_DIV
									+ MFPFileManagerActivity.STRING_CHART_FOLDER
									+ MFPFileManagerActivity.STRING_PATH_DIV);
			}
	    	File file = new File(strRootPath
	    					+ MFPFileManagerActivity.STRING_PATH_DIV
	    					+ MFPFileManagerActivity.STRING_CHART_FOLDER
	    					+ MFPFileManagerActivity.STRING_PATH_DIV
	    					+ strFileName
	    					+ MFPFileManagerActivity.STRING_CHART_EXTENSION);
			folder.mkdirs();
    		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
			osw.write(strFileContent);
			osw.flush();
    		osw.close();
			return true;
		}
		
	}
	
	public static String getAppFolderFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER;
	}
	
	public static String getConfigFolderFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_CONFIG_FOLDER;
	}
	
	public static String getAssetsFolderFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_ASSETS_FOLDER;
	}
	
	public static String getScriptFolderFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_SCRIPT_FOLDER;
	}
	
	public static String getChartFolderFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_CHART_FOLDER;
	}
	
	public static String getScriptExampleFolderFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_SCRIPT_FOLDER
		+ STRING_PATH_DIV + STRING_SCRIPT_EXAMPLE_FOLDER;
	}
	
	public static String getJMathCmdFileFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_JMATHCMD_JAR_FILE;
	}
	
	public static String getAssetZipFileFullPath()	{
		return AndroidStorageOptions.getSelectedStoragePath()
		+ STRING_PATH_DIV + STRING_APP_FOLDER + STRING_PATH_DIV + STRING_ASSET_ZIP_FILE;
	}
	
	public static boolean isSameFile(String strPath1, String strPath2)	{
		String[] strarrayNameSpace1 = cvtPath2NameSpace(strPath1);
		String[] strarrayNameSpace2 = cvtPath2NameSpace(strPath2);
		return isSameFile(strarrayNameSpace1, strarrayNameSpace2);
	}
	public static boolean isSameFile(String[] strarrayNameSpace1, String[] strarrayNameSpace2)	{
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
	
	public static boolean isParentChild(String strPathParent, String strPathChild)	{
		String[] strarrayNameSpaceParent = cvtPath2NameSpace(strPathParent);
		String[] strarrayNameSpaceChild = cvtPath2NameSpace(strPathChild);
		return isParentChild(strarrayNameSpaceParent, strarrayNameSpaceChild);
	}
	public static boolean isParentChild(String[] strarrayNameSpaceParent, String[] strarrayNameSpaceChild)	{
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
	
	// note that this function is different from CvtPath2NameSpace in MFPAdapter
	// bcoz it does not include a m_sstrRootPath.
	public static String[] cvtPath2NameSpace(String strPath)	{
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
	
	// note that this function is different from CvtNameSpace2Path in MFPAdapter
	// bcoz it does not include a m_sstrRootPath.
	public static String cvtNameSpace2Path(String[] strarrayNameSpace)	{
		String strPath = "";
		for (int i = 0; i < strarrayNameSpace.length; i ++)	{
			strPath += STRING_PATH_DIV + strarrayNameSpace[i];
		}
		return strPath;
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager);
        //setTitle(R.string.script_file_manager_title); // title should be different depends on script or chart file.
        
        mstrRootPath = AndroidStorageOptions.getSelectedStoragePath()
        			+ STRING_PATH_DIV + STRING_APP_FOLDER;

        //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();

        if (bundle != null)	{
        	//Next extract the values using the key as
        	mstrCurrentPath = bundle.getString("FILE_FOLDER");
        	mnMode = bundle.getInt("MODE");
        	mstrlistFileFilter = (bundle.getString("FILE_SHOWN_FILTER") == null)?null
        						:bundle.getString("FILE_SHOWN_FILTER").split(";");
        	if (mstrlistFileFilter != null && mstrlistFileFilter.length == 1)	{
        		if (isShownFileType(STRING_SCRIPT_EXTENSION))	{
        			// only script files are shown
        			mstrRootPath += STRING_PATH_DIV + STRING_SCRIPT_FOLDER;
        			setTitle(R.string.script_file_manager_title);
        		} else if (isShownFileType(STRING_CHART_EXTENSION))	{
        			// only chart files are shown
        			mstrRootPath += STRING_PATH_DIV + STRING_CHART_FOLDER;
        			setTitle(R.string.chart_file_manager_title);
        		} else	{
        			setTitle(R.string.file_manager_title);
        		}
        	}
        	
        } else	{
        	mstrCurrentPath = mstrRootPath;
        	mnMode = START_FILE_MANAGER_BY_ITSELF;
        	mstrlistFileFilter = null;
        }
        if (mstrCurrentPath == null || mstrCurrentPath.length() < mstrRootPath.length()
        		||  mstrCurrentPath.substring(0, mstrRootPath.length()).equals(mstrRootPath) == false)	{
        	// current path should always be root folder or a sub-folder in root.
        	mstrCurrentPath = mstrRootPath;
        }
        
        File directory = new File(mstrCurrentPath);
        Boolean bValidPath = true;
        if (!directory.exists())	{
        	bValidPath = false;
        	bValidPath = directory.mkdirs();
        } else if (directory.isDirectory() == false)	{
        	bValidPath = false;
        }
        if (bValidPath == false)	{
        	showErrorMsg(getString(R.string.file_manager_invalid_path) + " (" + mstrCurrentPath + ")");
        }

        mgridView = (GridView) findViewById(R.id.file_grid_view);
        mgridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				setAdapter(position, false);	// do not recreate adapter.
			}
        });
        
        mgridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
				MFPFileType mfpFileType = (MFPFileType) parent.getAdapter().getItem(position);
				if (mfpFileType == null)	{
					return false;
				}
				if (mfpFileType.mstrFileType.equals(STRING_FOLDER))	{	// folder
					mstrCurrentPath += STRING_PATH_DIV + mfpFileType.mstrFileName;
					((MFPFileManagerActivity)parent.getContext()).setAdapter(INT_INVALID_POSITION);
					if (isSameFile(mstrCurrentPath, getScriptExampleFolderFullPath()))	{
						int duration = Toast.LENGTH_SHORT;
						Toast toast = Toast.makeText(MFPFileManagerActivity.this, getString(R.string.example_scripts_not_loaded), duration);
						toast.show();
					}
				} else if (mfpFileType.mstrFileType.equals(STRING_UPPER_FOLDER))	{	// upper folder
					/*
					 * two cases considered: xxxx/xxxx/xxx or xxxx/xxxx/xxx/
					 */
					for (int index = mstrCurrentPath.length() - 2; index >= 0; index --)	{
						if (mstrCurrentPath.charAt(index) == '/')	{
							mstrCurrentPath = mstrCurrentPath.substring(0, index);
							break;
						}
					}
					((MFPFileManagerActivity)parent.getContext()).setAdapter(INT_INVALID_POSITION);
				} else if (mfpFileType.mstrFileType.equals(STRING_SCRIPT_EXTENSION))	{	// mfps file
					if (mnMode == START_FILE_MANAGER_BY_ITSELF)	{
						((MFPFileManagerActivity)parent.getContext()).startScriptEditor(
								mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName);
					} else if (mnMode == START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR || mnMode == START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR)	{	// open or save file
						Intent intentData = new Intent(MFPFileManagerActivity.this, ScriptEditorActivity.class);
						if (intentData != null) {
							Bundle b = new Bundle();
							if (b != null) {
								b.putString("SelectedFilePath", mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName);
								b.putInt("FileManagerOpenMode", mnMode);
								intentData.putExtra("android.intent.extra.SelectedFile", b);
							}
						}
						setResult(Activity.RESULT_OK, intentData);
						finish();
					} else	{
						setAdapter( position );
					}
				} else if (mfpFileType.mstrFileType.equals(STRING_CHART_EXTENSION))	{	// mfpc file
					if (mnMode == START_FILE_MANAGER_BY_ITSELF)	{
						String strPath = mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName;
		        		String strErrorMsg = MFPFileManagerActivity.openChartFile(MFPFileManagerActivity.this, strPath);
		        		if (strErrorMsg != null && strErrorMsg.trim().length() != 0)	{
		        			showErrorMsg(strErrorMsg);
		        		}
					}
				}
				return false;
			}
        	
        });
        
        setAdapter(INT_INVALID_POSITION);
        if (mmfparrayFiles.length == 0)	{
 			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(MFPFileManagerActivity.this)
						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
						        File dir = new File(MFPFileManagerActivity.getAppFolderFullPath());
						        String strErrMsg = "";
						        if (!dir.exists())	{
						            if (!dir.mkdirs())	{
						                // cannot create app folder.
						            	strErrMsg = getString(R.string.cannot_create_app_folder);
										// have to return because we cannot copy files.
									}
								}
						        if (strErrMsg.equals(""))	{
							        try	{
							        	// copy script examples and chart example(s)
										boolean bCopyResult1 = MFPAdapter.copyAssetScripts2SD(getAssets(), MFPAdapter.STRING_ASSET_SCRIPT_LIB_FOLDER,
												MFPFileManagerActivity.getScriptExampleFolderFullPath());
										boolean bCopyResult2 = MFPAdapter.copyAssetCharts2SD(getAssets(), MFPAdapter.STRING_ASSET_CHARTS_FOLDER,
												MFPFileManagerActivity.getChartFolderFullPath());
										if (!bCopyResult1 || !bCopyResult2)	{
							        		strErrMsg = getString(R.string.error_in_copying_example_files);
										}
						        	} catch (Exception e)	{
						        		// error in copying
						        		strErrMsg = getString(R.string.error_in_copying_example_files);
						        	}
					        	}
						        if (strErrMsg.equals("") == false)	{
						        	showErrorMsg(strErrMsg);
						        } else	{
						        	// copy successfully, show the copied files.
						            setAdapter(INT_INVALID_POSITION);
						        }
							}
							
						}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(
									DialogInterface dialog,
									int which) {
							}
							
						}).setTitle(getString(R.string.cpexample_name))
						.setMessage(getString(R.string.file_manager_copy_examples_confirm))
						// .setCancelable(false)	// can be cancelable
						.create();
			alertDialog.show();							
        }
    }

    public void setAdapter(int nSelectedFilePosition)	{
    	setAdapter(nSelectedFilePosition, true);
    }
    
    public void setAdapter(int nSelectedFilePosition, boolean bRecreateAdapter)	{
		boolean bIsRoot = false;
		if (mstrCurrentPath.charAt(mstrCurrentPath.length() - 1) == '/')	{
			// remove the last '/'
			mstrCurrentPath = mstrCurrentPath.substring(0, mstrCurrentPath.length() - 1);
		}
		if (mstrRootPath.charAt(mstrRootPath.length() - 1) == '/')	{
			// remove the last '/'
			mstrRootPath = mstrRootPath.substring(0, mstrRootPath.length() - 1);
		}
		if (mstrRootPath.equals(mstrCurrentPath))	{
			bIsRoot = true;
		}
		
        mnSelectedFilePosition = nSelectedFilePosition;
        if (mgridView.getAdapter() == null || bRecreateAdapter) {
            // mviewSelectedChild is set in getView function which is called back
            // when getAllFiles function is called.
            FileGridAdapter fga = new FileGridAdapter(this,
            							getAllFiles(mstrCurrentPath, bIsRoot));
            mmfparrayFiles = fga.mmfpArrayFiles;
            mgridView.setAdapter(fga);
        } else {
            ((FileGridAdapter)mgridView.getAdapter()).refill(mmfparrayFiles);
        }
        
    }
    
	/*
	 * Show script editor
	 */
	private void startScriptEditor(String strFilePath) {
	   	Intent intentScriptEditor = new Intent(this, ScriptEditorActivity.class);
	   	Bundle bundle = new Bundle();
	   	bundle.putString("FILE_PATH", strFilePath);
	   	bundle.putInt("MODE", ScriptEditorActivity.START_FILE_EDITOR_TO_EDIT);
	   	//Add this bundle to the intent
	   	intentScriptEditor.putExtras(bundle);
	   	startActivity(intentScriptEditor);
	}

	private class FileGridAdapter extends BaseAdapter {
        private Bitmap mIconScriptFile;
        private Bitmap mIconChartFile;
        private Bitmap mIconBMPFile;
        private Bitmap mIconUnknownFile;
        private Bitmap mIconUpperFolder;
        private Bitmap mIconFolder;

        private MFPFileType[] mmfpArrayFiles = null; 
        public FileGridAdapter(Context context, MFPFileType[] mfpFileTypes) {
            LayoutInflater.from(context);

            // Icons bound to the rows.
            mIconScriptFile = BitmapFactory.decodeResource(context.getResources(), R.drawable.script_file_icon);
            mIconChartFile = BitmapFactory.decodeResource(context.getResources(), R.drawable.chart_file_icon);
            mIconBMPFile = BitmapFactory.decodeResource(context.getResources(), R.drawable.bmp_file_icon);
            mIconUnknownFile = BitmapFactory.decodeResource(context.getResources(), R.drawable.unknown_file_icon);
            mIconUpperFolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.upper_folder_icon);
            mIconFolder = BitmapFactory.decodeResource(context.getResources(), R.drawable.folder_icon);
            
            setData(mfpFileTypes);
        }
        
        public void setData(MFPFileType[] mfpArrayFiles) {
            mmfpArrayFiles = (mfpArrayFiles == null)?(new MFPFileType[0]):mfpArrayFiles;        	
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         *
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return mmfpArrayFiles.length;
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         *
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return (position < 0 || position >= mmfpArrayFiles.length) ? null : mmfpArrayFiles[position];
        }

        /**
         * Use the array index as a unique id.
         *
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view to hold each row.
         *
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // A ViewHolder keeps references to children views to avoid unneccessary calls
            // to findViewById() on each row.
            ViewHolder holder;

            // When convertView is not null, we can reuse it directly, there is no need
            // to reinflate it. We only inflate a new View when the convertView supplied
            // by ListView is null.
            if (convertView == null) {
            	final LayoutInflater factory = getLayoutInflater();
            	convertView = factory.inflate(R.layout.image_text, null);

                // Creates a ViewHolder and store references to the two children views
                // we want to bind data to.
                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.child_TextView);
                holder.icon = (ImageView) convertView.findViewById(R.id.child_ImageView);

                convertView.setTag(holder);
            } else {
                // Get the ViewHolder back to get fast access to the TextView
                // and the ImageView.
                holder = (ViewHolder) convertView.getTag();
            }

            // Bind the data efficiently with the holder.
            holder.text.setText(mmfpArrayFiles[position].getNameWithNoExtension());
            if (mmfpArrayFiles[position].mstrFileType.equals(STRING_UPPER_FOLDER))	{
            	holder.icon.setImageBitmap(mIconUpperFolder);
            } else if (mmfpArrayFiles[position].mstrFileType.equals(STRING_FOLDER))	{
            	holder.icon.setImageBitmap(mIconFolder);
            } else if (mmfpArrayFiles[position].mstrFileType.equals(STRING_SCRIPT_EXTENSION))	{
            	holder.icon.setImageBitmap(mIconScriptFile);            	
            } else if (mmfpArrayFiles[position].mstrFileType.equals(STRING_CHART_EXTENSION))	{
            	holder.icon.setImageBitmap(mIconChartFile);
            } else if (mmfpArrayFiles[position].mstrFileType.equals(STRING_BMP_EXTENSION))	{
            	holder.icon.setImageBitmap(mIconBMPFile);
            } else	{
            	holder.icon.setImageBitmap(mIconUnknownFile);
            }

            if (position == mnSelectedFilePosition)	{
            	// set select selected view.
            	convertView.setBackgroundColor(COLOR_HIGHLIGHTED_BACKGND);
            } else	{
            	convertView.setBackgroundColor(0);
            }
            return convertView;
        }

        public void refill(MFPFileType[] mfpArrayFiles) {
        	mmfpArrayFiles = mfpArrayFiles;
            notifyDataSetChanged();
        }
        
        public class ViewHolder {
            TextView text;
            ImageView icon;
        }
    }

    public class MFPFileType	{
    	public String mstrFileName = new String();
    	public String mstrFileType = STRING_SCRIPT_EXTENSION;	// "upper_folder" means .., "folder" means folder, ...
    	public String getNameWithNoExtension()	{
    		if ((!mstrFileType.equals(STRING_UPPER_FOLDER)) && (!mstrFileType.equals(STRING_FOLDER))
    				&& (!mstrFileType.equals(STRING_UNKNOWN_FILE)))	{
    			if (mstrFileName == null
    					|| mstrFileName.length() <= mstrFileType.length())	{
    				return "";
    			} else	{
    				return mstrFileName.substring(0,
    						mstrFileName.length() - mstrFileType.length());
    			}
    		} else	{
    			return mstrFileName;
    		}
    	}
    }
    
    public MFPFileType[] getAllFiles(String strDirPath, boolean bIsRoot)	{
    	MFPFileType[] mfpFileTypes = new MFPFileType[0];
		File directory = new File(strDirPath);
		if (directory != null && directory.isDirectory())	{
			String[] strarrayMiscFileNames = directory.list();
			mfpFileTypes = new MFPFileType[strarrayMiscFileNames.length];
			int nNumofShownItems = (bIsRoot)?0:1;
			for (int index = 0; index < strarrayMiscFileNames.length; index ++)	{
				mfpFileTypes[index] = new MFPFileType();
				mfpFileTypes[index].mstrFileName = strarrayMiscFileNames[index];
				File fileChild = new File(strDirPath + STRING_PATH_DIV + strarrayMiscFileNames[index]);
				if (fileChild.isDirectory())	{
					mfpFileTypes[index].mstrFileType = STRING_FOLDER;
					nNumofShownItems ++;
				} else if (MFPAdapter.getFileExtFromName(strarrayMiscFileNames[index]).toLowerCase(Locale.US).equals(STRING_SCRIPT_EXTENSION))	{
					mfpFileTypes[index].mstrFileType = STRING_SCRIPT_EXTENSION;
					if (isShownFileType(mfpFileTypes[index].mstrFileType))	{
						nNumofShownItems ++;
					}
				} else if (MFPAdapter.getFileExtFromName(strarrayMiscFileNames[index]).toLowerCase(Locale.US).equals(STRING_CHART_EXTENSION))	{
					mfpFileTypes[index].mstrFileType = STRING_CHART_EXTENSION;
					if (isShownFileType(mfpFileTypes[index].mstrFileType))	{
						nNumofShownItems ++;
					}
				} else if (MFPAdapter.getFileExtFromName(strarrayMiscFileNames[index]).toLowerCase(Locale.US).equals(STRING_BMP_EXTENSION))	{
					mfpFileTypes[index].mstrFileType = STRING_BMP_EXTENSION;
					if (isShownFileType(mfpFileTypes[index].mstrFileType))	{
						nNumofShownItems ++;
					}
				} else	{
					mfpFileTypes[index].mstrFileType = STRING_UNKNOWN_FILE;
					if (mstrlistFileFilter == null)	{
						nNumofShownItems ++;
					}
				}
			}
			MFPFileType[] mfpSelected = new MFPFileType[nNumofShownItems];
			int index1 = 0;
			if (bIsRoot == false)	{
				mfpSelected[0] = new MFPFileType();
				mfpSelected[0].mstrFileName = "..";
				mfpSelected[0].mstrFileType = STRING_UPPER_FOLDER;	// upper folder
				index1 ++;
			}
			for (int index = 0; index < strarrayMiscFileNames.length; index ++)	{
				if (mfpFileTypes[index].mstrFileType.equals(STRING_FOLDER)
						|| (mstrlistFileFilter == null)
						|| isShownFileType(mfpFileTypes[index].mstrFileType))	{
					mfpSelected[index1] = mfpFileTypes[index];
					index1 ++;
				}
			}
			mfpFileTypes = mfpSelected;
		}
		return mfpFileTypes;
    }
    
    boolean isShownFileType(String strFileType)	{
    	if (strFileType == null)	{
    		return false;
    	} else if (mstrlistFileFilter == null)	{
    		return true;
    	} else	{
    		for (int nIndex = 0; nIndex < mstrlistFileFilter.length; nIndex++)	{
    			if (mstrlistFileFilter[nIndex] != null
    					&& mstrlistFileFilter[nIndex].toLowerCase(Locale.US).equals(strFileType.toLowerCase(Locale.US)))	{
    				return true;
    			}
    		}
    	}
    	return false;    	
    }

	@Override
	/*
	 * Create three menus: History, Settings and Help
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ITEM0, 0, getString(R.string.file_manager_menu_new));
		menu.add(0, ITEM1, 0, getString(R.string.file_manager_menu_open));
		menu.add(0, ITEM2, 0, getString(R.string.file_manager_menu_save));			
		menu.add(0, ITEM3, 0, getString(R.string.file_manager_menu_rename));
		menu.add(0, ITEM4, 0, getString(R.string.file_manager_menu_delete));
		menu.add(0, ITEM5, 0, getString(R.string.menu_help));
		return true;
	}

	//Dynamically create context Menu
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear(); //Clear view of previous menu
        if (isShownFileType(STRING_SCRIPT_EXTENSION))	{
        	// we can add new file only if the shown file type includes script file.
        	menu.add(0, ITEM0, 0, getString(R.string.file_manager_menu_new));
        }
		if (mnSelectedFilePosition >= 0
				&& (mmfparrayFiles[mnSelectedFilePosition].mstrFileType.equals(STRING_FOLDER)
				|| mmfparrayFiles[mnSelectedFilePosition].mstrFileType.equals(STRING_UPPER_FOLDER)
				|| (mmfparrayFiles[mnSelectedFilePosition].mstrFileType.equals(STRING_SCRIPT_EXTENSION)
						&& mnMode == START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR)
				|| ((!mmfparrayFiles[mnSelectedFilePosition].mstrFileType.equals(STRING_UNKNOWN_FILE))
						&& mnMode == START_FILE_MANAGER_BY_ITSELF )))	{
			menu.add(0, ITEM1, 0, getString(R.string.file_manager_menu_open));
		}
		if (mnSelectedFilePosition >= 0
				&& (mmfparrayFiles[mnSelectedFilePosition].mstrFileType.equals(STRING_SCRIPT_EXTENSION)
						&& mnMode == START_FILE_MANAGER_TO_SAVE_BY_SCRIPT_EDITOR))	{
			menu.add(0, ITEM2, 0, getString(R.string.file_manager_menu_save));			
		}
		if (mnSelectedFilePosition >= 0)	{
			MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
			if (mfpFileType != null && mfpFileType.mstrFileType.equals(STRING_UPPER_FOLDER) == false)	{	// not upper folder
				menu.add(0, ITEM3, 0, getString(R.string.file_manager_menu_cut));
				menu.add(0, ITEM4, 0, getString(R.string.file_manager_menu_copy));
			}
		}
		if (mmfpClipboardFileType != null)	{
			menu.add(0, ITEM5, 0, getString(R.string.file_manager_menu_paste));
		}
		if (mnSelectedFilePosition >= 0)	{
			MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
			if (mfpFileType != null && mfpFileType.mstrFileType.equals(STRING_UPPER_FOLDER) == false)	{	// not upper folder
				menu.add(0, ITEM6, 0, getString(R.string.file_manager_menu_rename));
				menu.add(0, ITEM7, 0, getString(R.string.file_manager_menu_delete));
			}
		}
		menu.add(0, ITEM8, 0, getString(R.string.menu_help));		
        return super.onPrepareOptionsMenu(menu);
    }
    
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case ITEM0: 
			actionClickMenuNewFile();
			break;
		case ITEM1: 
			actionClickMenuOpenFile();
			break;
		case ITEM2: 
			actionClickMenuSaveFile();
			break;
		case ITEM3: 
			actionClickMenuCutFile();
			break;
		case ITEM4: 
			actionClickMenuCopyFile();
			break;
		case ITEM5:
			actionClickMenuPasteFile();
			break;
		case ITEM6: 
			actionClickMenuRenameFile();
			break;
		case ITEM7: 
			actionClickMenuDeleteFile();
			break;
		case ITEM8:
			actionClickMenuHelp();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * Create a new folder or file
	 */
	private void actionClickMenuNewFile()	{
	   	showDialog(DIALOG_FILE_GENERATION);
	}
	
	/*
	 * Open file or folder
	 */
	private void actionClickMenuOpenFile()	{
		if (mgridView.getAdapter().getItem(mnSelectedFilePosition) == null)	// including < 0 or > max idx cases
			return;	// no item is selected.
		
		MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
		if (mfpFileType.mstrFileType.equals(STRING_FOLDER))	{	// folder
			mstrCurrentPath += STRING_PATH_DIV + mfpFileType.mstrFileName;
			setAdapter(INT_INVALID_POSITION);
			if (isSameFile(mstrCurrentPath, getScriptExampleFolderFullPath()))	{
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(MFPFileManagerActivity.this, getString(R.string.example_scripts_not_loaded), duration);
				toast.show();
			}
		} else if (mfpFileType.mstrFileType.equals(STRING_UPPER_FOLDER))	{	// upper folder
			/*
			 * two cases considered: xxxx/xxxx/xxx or xxxx/xxxx/xxx/
			 */
			for (int index = mstrCurrentPath.length() - 2; index >= 0; index --)	{
				if (mstrCurrentPath.charAt(index) == '/')	{
					mstrCurrentPath = mstrCurrentPath.substring(0, index);
					break;
				}
			}
			setAdapter(INT_INVALID_POSITION);
		} else if (mfpFileType.mstrFileType.equals(STRING_SCRIPT_EXTENSION))	{	// mfps file
			if (mnMode == START_FILE_MANAGER_BY_ITSELF)	{
				startScriptEditor(mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName);
			} else if (mnMode == START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR){
				// Select a file to edit.
				Intent intentData = new Intent(this, ScriptEditorActivity.class);
				if (intentData != null) {
					Bundle b = new Bundle();
					if (b != null) {
						b.putString("SelectedFilePath", mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName);
						b.putInt("FileManagerOpenMode", mnMode);
						intentData.putExtra("android.intent.extra.SelectedFile", b);
					}
				}
				setResult(Activity.RESULT_OK, intentData);
			   	finish();
			}
		} else if (mfpFileType.mstrFileType.equals(STRING_CHART_EXTENSION))	{	//mfpc file
			if (mnMode == START_FILE_MANAGER_BY_ITSELF)	{
				String strPath = mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName;
        		String strErrorMsg = openChartFile(this, strPath);
        		if (strErrorMsg != null && strErrorMsg.trim().length() != 0)	{
        			showErrorMsg(strErrorMsg);
        		}
			}
		}
	}
	
	/*
	 * Save file
	 */
	private void actionClickMenuSaveFile()	{
		if (mgridView.getAdapter().getItem(mnSelectedFilePosition) == null)	{	// including < 0 or > max idx cases
			return;	// no item is selected.
		}
		
		MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
		if (mfpFileType.mstrFileType.equals(STRING_FOLDER) || mfpFileType.mstrFileType.equals(STRING_UPPER_FOLDER))	{	// folder
			return;
		}
		
		if (mnMode == START_FILE_MANAGER_BY_ITSELF || mnMode == START_FILE_MANAGER_TO_OPEN_BY_SCRIPT_EDITOR)	{
			return;	// should not be in self-starting mode or started-to-open mode.
		}
		
		// Select a file to save.
		Intent intentData = new Intent(this, ScriptEditorActivity.class);
		if (intentData != null) {
			Bundle b = new Bundle();
			if (b != null) {
				b.putString("SelectedFilePath", mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName);
				b.putInt("FileManagerOpenMode", mnMode);
				intentData.putExtra("android.intent.extra.SelectedFile", b);
			}
		}
		setResult(Activity.RESULT_OK, intentData);
	   	finish();
	}
	
	/*
	 * cut file or folder
	 */
	private void actionClickMenuCutFile()	{
		if (mgridView.getAdapter().getItem(mnSelectedFilePosition) == null)	// including < 0 or > max idx cases
			return;	// no item is selected.
		
		mmfpClipboardFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
		mstrClipBoardPath = mstrCurrentPath;
		mbClipBoardCut = true;
	}
	
	/*
	 * copy file or folder
	 */
	private void actionClickMenuCopyFile()	{
		if (mgridView.getAdapter().getItem(mnSelectedFilePosition) == null)	// including < 0 or > max idx cases
			return;	// no item is selected.
		
		mmfpClipboardFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
		mstrClipBoardPath = mstrCurrentPath;
		mbClipBoardCut = false;
	}
	
	/*
	 * paste file or folder
	 */
	private void actionClickMenuPasteFile()	{
	   	if (mmfpClipboardFileType != null)	{
	   		if (mbClipBoardCut)	{
				File fOld = new File(mstrClipBoardPath + STRING_PATH_DIV + mmfpClipboardFileType.mstrFileName);
				File fNew = new File(mstrCurrentPath + STRING_PATH_DIV + mmfpClipboardFileType.mstrFileName);
				performValidFileTransfer(fOld, fNew, "cut");
	   		} else	{	// copy
				File fSrc = new File(mstrClipBoardPath + STRING_PATH_DIV + mmfpClipboardFileType.mstrFileName);
				String strNewFileName = mmfpClipboardFileType.mstrFileName;
				File fDest = new File(mstrCurrentPath + STRING_PATH_DIV + strNewFileName);
				performValidFileTransfer(fSrc, fDest, "copy");
	   		}
	   	}
	}
	
	/*
	 * Rename file or folder
	 */
	private void actionClickMenuRenameFile()	{
	   	showDialog(DIALOG_FILE_RENAME);
	}
	
	/*
	 * Delete file or folder
	 */
	private void actionClickMenuDeleteFile()	{
		// Android may reuse the dialog box so we need to change the text before showDialog is called.
		if (malertFileDeleteDlg != null)	{
			MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
			if (mfpFileType != null)	{
				malertFileDeleteDlg.setMessage(getString(R.string.file_manager_file_delete_confirm)
						+ " " + mfpFileType.getNameWithNoExtension() + " ?");
				showDialog(DIALOG_FILE_DELETE);
			}
		} else	{
			showDialog(DIALOG_FILE_DELETE);
		}
	}
	
	/*
	 * Show help
	 */
	private void actionClickMenuHelp(){
	   	Intent intentHelp = new Intent(this, ActivityShowHelp.class);
	   	Bundle bundle = new Bundle();
	   	bundle.putString("HELP_CONTENT", "file_management");
	   	//Add this bundle to the intent
	   	intentHelp.putExtras(bundle);
	   	startActivity(intentHelp);
	}
		
	/*
	 * select expression or answer as the record to return
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater inflater = LayoutInflater.from(this);
		AlertDialog.Builder blder = new AlertDialog.Builder(this);
		switch(id)	{
		case DIALOG_FILE_GENERATION:
			final View viewNewFileGeneration = inflater.inflate(R.layout.new_file_generator, null);
			EditText txtFileName = ((EditText)viewNewFileGeneration.findViewById(R.id.edttxt_enter_file_name));
			txtFileName.setText("");
			blder.setIcon(R.drawable.icon);
			blder.setTitle(getString(R.string.file_manager_new_file_title));
			blder.setView(viewNewFileGeneration);
			blder.setPositiveButton(R.string.ok, 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						boolean bFileGenerationResult = false;
						RadioGroup radioGrp = (RadioGroup) viewNewFileGeneration.findViewById(R.id.radioGrp_new_file_type);
						EditText txtFileName = (EditText)(viewNewFileGeneration.findViewById(R.id.edttxt_enter_file_name));
						String strFileName = txtFileName.getText().toString().trim();
						if (strFileName.equals("") == false)	{
							strFileName = mstrCurrentPath + STRING_PATH_DIV + strFileName;
							if( radioGrp.getCheckedRadioButtonId() == R.id.radioBtn_new_script) {
								strFileName += STRING_SCRIPT_EXTENSION;
								File f = new File(strFileName);
								try {
									bFileGenerationResult = f.createNewFile();
								} catch(IOException e)	{
									bFileGenerationResult = false;
								}
							} else	{
								File f = new File(strFileName);
								bFileGenerationResult = f.mkdir();
							}
						}
						if (bFileGenerationResult == false)	{
							showErrorMsg(getString(R.string.file_manager_new_file_failed_msg));
						} else	{
							setAdapter(INT_INVALID_POSITION);
						}
						((EditText)viewNewFileGeneration.findViewById(R.id.edttxt_enter_file_name)).setText("");
					}
			});
			malertNewFileDlg = blder.create();
			return malertNewFileDlg;
		case DIALOG_FILE_RENAME:
			final View viewFileRename = inflater.inflate(R.layout.file_rename, null);
			EditText txtFileNewName = ((EditText)viewFileRename.findViewById(R.id.edttxt_enter_file_new_name));
			txtFileNewName.setText("");
			blder.setIcon(R.drawable.icon);
			blder.setTitle(getString(R.string.file_manager_file_rename_title));
			blder.setView(viewFileRename);
			blder.setPositiveButton(R.string.ok, 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
						if (mfpFileType != null)	{
							String strFileOldName = mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName;
							EditText txtFileName = (EditText)(viewFileRename.findViewById(R.id.edttxt_enter_file_new_name));
							String strFileNewName = txtFileName.getText().toString().trim();
							if (strFileNewName.equals("") == false)	{
								strFileNewName = mstrCurrentPath + STRING_PATH_DIV + strFileNewName;
								if (mfpFileType.mstrFileType.equals(STRING_SCRIPT_EXTENSION))	{
									strFileNewName += STRING_SCRIPT_EXTENSION;
								} else if (mfpFileType.mstrFileType.equals(STRING_CHART_EXTENSION))	{
									strFileNewName += STRING_CHART_EXTENSION;
								} else if (mfpFileType.mstrFileType.equals(STRING_BMP_EXTENSION))	{
									strFileNewName += STRING_BMP_EXTENSION;
								}
								File fOld = new File(strFileOldName);
								File fNew = new File(strFileNewName);
								performValidFileTransfer(fOld, fNew, "rename");
							} else	{
								showErrorMsg(getString(R.string.file_manager_renamed_file_name_cannot_be_empty));
							}
							((EditText)viewFileRename.findViewById(R.id.edttxt_enter_file_new_name)).setText("");
						}
					}
			});
			malertFileRenameDlg = blder.create();
			return malertFileRenameDlg;
		case DIALOG_FILE_DELETE:
			MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
			if (mfpFileType == null)	{
				return null;
			}
			blder.setIcon(R.drawable.icon);
			blder.setTitle(getString(R.string.file_manager_file_delete_title));
			blder.setMessage(getString(R.string.file_manager_file_delete_confirm)
					+ " " + mfpFileType.getNameWithNoExtension() + " ?");
			blder.setPositiveButton(R.string.ok, 
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						MFPFileType mfpFileType = (MFPFileType) mgridView.getAdapter().getItem(mnSelectedFilePosition);
						if (mfpFileType != null)	{
							String strFileName = mstrCurrentPath + STRING_PATH_DIV + mfpFileType.mstrFileName;
							boolean bFileDeleteResult = false;
							File f = new File(strFileName);
							bFileDeleteResult = deleteFileOrFolder(f);
							if (bFileDeleteResult == false)	{
								showErrorMsg(getString(R.string.file_manager_file_delete_failed_msg));
							}
							setAdapter(INT_INVALID_POSITION);	// we may partially delete
						}
					}
			});
			blder.setNegativeButton(R.string.cancel, 
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						return;
					}
			});
			malertFileDeleteDlg = blder.create();
			return malertFileDeleteDlg;
		default:
			return null;
		}
	}

	public void performValidFileTransfer(File fSrc, File fDest, String strAction)	{
		if (fSrc == null || fDest == null)	{
			showErrorMsg(getString(R.string.file_manager_file_io_error));
			mmfpClipboardFileType = null;
			return;	// invalid file
		} else if (!fSrc.exists())	{	// source file does not exist, deleted?
			showErrorMsg(fSrc.getName() + " " + getString(R.string.file_manager_source_file_does_not_exist));
			mmfpClipboardFileType = null;
			return;
		} else if (strAction.trim().compareToIgnoreCase("cut") != 0
				&& strAction.trim().compareToIgnoreCase("copy") != 0
				&& strAction.trim().compareToIgnoreCase("rename") != 0)	{
			showErrorMsg(getString(R.string.file_manager_invalid_operation));
			mmfpClipboardFileType = null;
			return;	// invalid action
		}
		
		String strCoverFileErrorMsg = "";
		
		if (isSameFile(fSrc.getAbsolutePath(), fDest.getAbsolutePath()))	{
			if ( strAction.trim().compareToIgnoreCase("rename") == 0 )	{
				strCoverFileErrorMsg = getString(R.string.file_manager_file_cannot_be_renamed_to_the_same_file);
				showErrorMsg(strCoverFileErrorMsg);
			} else if ( strAction.trim().compareToIgnoreCase("cut") == 0 )	{
				// do nothing
			} else {	// copy
				String strSrcFullName = fSrc.getAbsolutePath();
				File fDestNew;
				if (fSrc.isDirectory())	{
					fDestNew = new File(strSrcFullName + "_copy");
				} else	{
					String strExt = MFPAdapter.getFileExtFromName(strSrcFullName);
					fDestNew = new File(strSrcFullName.substring(0, strSrcFullName.length() - strExt.length()) + "_copy" + strExt);
				}
				if (fDestNew.exists())	{
					performFileTransfer2Exist(fSrc, fDestNew, strAction);
				} else	{
					boolean bResult = performFileTransfer(fSrc, fDestNew, strAction);
					if (!bResult)	{
						strCoverFileErrorMsg = getString(R.string.cannot) + " "
											+ getString(R.string.file_copy_action) + " "
											+ fSrc.getName() + " "
											+ getString(R.string.to_path) + " "
											+ mstrCurrentPath;
						showErrorMsg(strCoverFileErrorMsg);
					}
				}
			}
		} else if (isParentChild(fSrc.getAbsolutePath(), fDest.getAbsolutePath()))	{	// if destination is the child of source
			strCoverFileErrorMsg = getString(R.string.file_manager_folder_cannot_be_copied_moved_or_renamed_to_its_child);
			showErrorMsg(strCoverFileErrorMsg);			
		} else if ((fSrc.isDirectory() && fDest.exists() && !fDest.isDirectory())
				|| (!fSrc.isDirectory() && fDest.exists() && fDest.isDirectory()))	{
			strCoverFileErrorMsg = getString(R.string.file_manager_file_cannot_be_copied_moved_or_renamed_to_folder_or_vice_versa);
			showErrorMsg(strCoverFileErrorMsg);
		} else if (fDest.exists())	{
			performFileTransfer2Exist(fSrc, fDest, strAction);
		} else	{
			boolean bResult = performFileTransfer(fSrc, fDest, strAction);
			if (!bResult)	{
				if (strAction.trim().compareToIgnoreCase("cut") == 0)	{
					strCoverFileErrorMsg = getString(R.string.cannot) + " "
										+ getString(R.string.file_move_action) + " "
										+ fSrc.getName() + " "
										+ getString(R.string.to_path) + " "
										+ mstrCurrentPath;
				} else if (strAction.trim().compareToIgnoreCase("copy") == 0)	{
					strCoverFileErrorMsg = getString(R.string.cannot) + " "
										+ getString(R.string.file_copy_action) + " "
										+ fSrc.getName() + " "
										+ getString(R.string.to_path) + " "
										+ mstrCurrentPath;
				} else	{	// rename
					strCoverFileErrorMsg = getString(R.string.cannot) + " "
										+ getString(R.string.file_rename_action) + " "
										+ fSrc.getName() + " "
										+ getString(R.string.to_path) + " "
										+ fDest.getName();
				}
				showErrorMsg(strCoverFileErrorMsg);
			}
		}
	}
	
	public void performFileTransfer2Exist(final File fSrc, final File fDest, final String strAction)	{
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(MFPFileManagerActivity.this)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							String strCoverFileErrorMsg = "";
							boolean bResult = performFileTransfer(fSrc, fDest, strAction);
							if (!bResult)	{
								if (strAction.trim().compareToIgnoreCase("cut") == 0)	{
									strCoverFileErrorMsg = getString(R.string.cannot) + " "
														+ getString(R.string.file_move_action) + " "
														+ fSrc.getName() + " "
														+ getString(R.string.to_path) + " "
														+ mstrCurrentPath;
								} else if (strAction.trim().compareToIgnoreCase("copy") == 0)	{
									strCoverFileErrorMsg = getString(R.string.cannot) + " "
														+ getString(R.string.file_copy_action) + " "
														+ fSrc.getName() + " "
														+ getString(R.string.to_path) + " "
														+ mstrCurrentPath;
								} else	{	// rename
									strCoverFileErrorMsg = getString(R.string.cannot) + " "
														+ getString(R.string.file_rename_action) + " "
														+ fSrc.getName() + " "
														+ getString(R.string.to_path) + " "
														+ fDest.getName();
								}
								showErrorMsg(strCoverFileErrorMsg);
							}
						}
						
					}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
							// do nothing. cancel.
						}
						
					}).setTitle(getString(R.string.warning))
					.setMessage(getString(R.string.file_manager_to_cover_existing_file))
					.create();
		alertDialog.show();		
	}
	
	public boolean performFileTransfer(File fSrc, File fDest, String strAction)	{
		// here, always assume that fSrc and fDest are valid files.
		boolean bFileOperationResult = false;
		if (strAction.trim().compareToIgnoreCase("cut") == 0)	{
			// cut - paste:
			boolean bFileOperationResult1 = false, bFileOperationResult2 = false;
			bFileOperationResult1 = copyFileOrFolder(fSrc, fDest);
			if (bFileOperationResult1)	{	// only if copy is successful, we try to delete.
				bFileOperationResult2 = deleteFileOrFolder(fSrc);
				// only if delete is successful, we prevent original file from being copied.
				if (bFileOperationResult2)	{
					mmfpClipboardFileType = null;	// after a successful cut-paste, the original file no longer exists
					bFileOperationResult = true;
				}
			}
		} else if (strAction.trim().compareToIgnoreCase("copy") == 0)	{
			// copy - paste
			bFileOperationResult = copyFileOrFolder(fSrc, fDest);
   		} else if (strAction.trim().compareToIgnoreCase("rename") == 0)	{
   			// rename:
    		/*
    		 * note that if destination is a non-empty folder, rename will
    		 * fail (return false). In other words, destination must be either
    		 * a file or an empty folder. If destination has some defined
    		 * functions, these functions will be removed from memory and source's
    		 * function will replace them. And the original source's functions
    		 * will be removed as well.
    		 */
   			if (fDest.exists())	{
	    		MFPAdapter.unloadFileOrFolder(fDest.getAbsolutePath());
   			}
   			bFileOperationResult = fSrc.renameTo(fDest);
			if (bFileOperationResult)	{
	    		MFPAdapter.unloadFileOrFolder(fSrc.getAbsolutePath());
			}
			if (fDest.exists())	{
				// whether successful or not, we have to load dest coz we unload it b4 rename.
				MFPAdapter.loadFileOrFolder(fDest.getAbsolutePath(), null);
			}
   		} else	{
   			bFileOperationResult = false;	// invalid action
   		}
		setAdapter(INT_INVALID_POSITION);
		return bFileOperationResult;
	}
	
	public void showErrorMsg(String strMsg)	{
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(MFPFileManagerActivity.this)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(
								DialogInterface dialog,
								int which) {
						}
						
					}).setTitle(getString(R.string.error))
					.setMessage(strMsg)
					.create();
		alertDialog.show();
	}
	
	public static final boolean copyFileOrFolder( File source, File destination ) {
		if( source.isDirectory() ) {
			if (!destination.exists())	{
				destination.mkdirs();
			} else if (!destination.isDirectory())	{
				return false;	// destination is a file, not folder
			}
			
			File[] files = source.listFiles();

			boolean bReturnVal = true;
			for( File file : files ) {
				bReturnVal &= copyFileOrFolder( file, new File( destination, file.getName() ) );
			}
			return bReturnVal;
		} else if (source.isFile()) {
			if (destination.exists() && !destination.isFile())	{
				return false;	// destination is a not a file
			}
			FileInputStream fisSrc = null;
			FileOutputStream fosDest = null;
			boolean bReturnVal = true;
			boolean bDestExistB4Copy = destination.exists();
			if (bDestExistB4Copy)	{	// if destination file exists before copy, need to unload it first.
		       	if (MFPAdapter.getFileExtFromName(destination.getAbsolutePath())
	        			.toLowerCase(Locale.US).equals(STRING_SCRIPT_EXTENSION))	{
	        		MFPAdapter.unloadFileOrFolder(destination.getAbsolutePath());
	        	}
			}
			try	{
				fisSrc = new FileInputStream(source);
				fosDest = new FileOutputStream(destination);
				FileChannel sourceChannel = fisSrc.getChannel();
				FileChannel targetChannel = fosDest.getChannel();
				sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
				sourceChannel.close();
				targetChannel.close();
			} catch(Exception e)	{
				bReturnVal = false;
			} finally	{
				try	{
					if (fosDest != null)
						fosDest.close();
				}
				catch (IOException e)
				{
				}
				try	{
					if (fisSrc != null)
						fisSrc.close();
				}
				catch (IOException e)
				{
				}
			}
			if (bReturnVal)		{	// copy successfully
		       	if (MFPAdapter.getFileExtFromName(destination.getAbsolutePath())
	        			.toLowerCase(Locale.US).equals(STRING_SCRIPT_EXTENSION))	{
	        		MFPAdapter.loadFile(destination.getAbsolutePath(), null);
	        	}
			} else if (bDestExistB4Copy)	{	// copy unsuccessfully, but destination file existed b4 copy
		       	if (MFPAdapter.getFileExtFromName(destination.getAbsolutePath())
	        			.toLowerCase(Locale.US).equals(STRING_SCRIPT_EXTENSION)
	        			&& destination.exists())	{	// destination file still exists.
	        		MFPAdapter.loadFile(destination.getAbsolutePath(), null);
	        	}
				
			}
			return bReturnVal;
		} else	{
			return false;	// not file or directory
		}
	}

	public static boolean deleteFileOrFolder(File fFileOrFolder)	{
	    // Deletes all files and sub-directories under fFileOrFolder if it is a folder.
		// Or delete the file if fFileOrFolder is a file.
	    // Returns true if all deletions were successful.
	    // If a deletion fails, the method stops attempting to delete and returns false.
        if (fFileOrFolder.isDirectory()) {
            String[] strarrayChildren = fFileOrFolder.list();
            for (int i=0; i<strarrayChildren.length; i++) {
                boolean bSuccess = deleteFileOrFolder(new File(fFileOrFolder, strarrayChildren[i]));
                if (!bSuccess) {
                    return false;
                }
            }
        } else	{
    		// now unload the lib files.
        	if (MFPAdapter.getFileExtFromName(fFileOrFolder.getAbsolutePath())
        			.toLowerCase(Locale.US).equals(STRING_SCRIPT_EXTENSION))	{
        		MFPAdapter.unloadFileOrFolder(fFileOrFolder.getAbsolutePath());
        	}
        }
    
        // The directory is now empty so delete it
        return fFileOrFolder.delete();
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState)	{
		outState.putInt(SELECTED_POSITION, mnSelectedFilePosition);
		outState.putString(CURRENT_PATH, mstrCurrentPath);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState (Bundle inState) {
		//Restore last state
		super.onRestoreInstanceState(inState);
		
		mnSelectedFilePosition = inState.getInt(SELECTED_POSITION);
		mstrCurrentPath = inState.getString(CURRENT_PATH);
		setAdapter(mnSelectedFilePosition);
	}
	
	/** Called when the activity is finally destroyed or in portrait-landscape switch. */
    @Override
    public void onDestroy()	{
    	if (isFinishing())	{
    		setResult(Activity.RESULT_CANCELED, null);
    	}
    	super.onDestroy();
    }

    public static void loadChart(final Context context, final String strChartFilePath)	{
		final ProgressDialog dlgLoadingChartProgress = ProgressDialog.show(context, context.getString(R.string.please_wait),
				context.getString(R.string.loading_chart), true);
		final Handler handler = new Handler();
		
		Thread threadLoadChart = new Thread(new Runnable()	{
		
			@Override
			public void run() {
				final Intent intent = new Intent(context, ActivityChartDemon.class);
				intent.putExtra(ChartOperator.VMFPChartPath, strChartFilePath);
				handler.post(new Runnable()	{
					@Override
					public void run() {
						context.startActivity(intent);
						dlgLoadingChartProgress.dismiss();
					}
				});
			}
		});
		threadLoadChart.start();
    }
    
    public static String openChartFile(Context context, String strPath)	{
    	String strChartType = ChartOperator.getChartTypeFromFile(strPath);
		if (strChartType.compareToIgnoreCase("multiXY") == 0)	{
			XYChartOperator xyChartOperator = new XYChartOperator();
			if (xyChartOperator.loadFromFile(strPath))	{
				loadChart(context, strPath);
			} else	{
				return context.getString(R.string.graph_file_cannot_be_read);
			}
		} else if (strChartType.compareToIgnoreCase("2DExpr") == 0)	{
			XYExprChartOperator xyExprChartOperator = new XYExprChartOperator();
        	if (xyExprChartOperator.loadFromFile(strPath))	{
				loadChart(context, strPath);
			} else	{
				return context.getString(R.string.graph_file_cannot_be_read);
        	}
    	} else if (strChartType.compareToIgnoreCase("multiRangle") == 0)	{
    		PolarChartOperator polarChartOperator = new PolarChartOperator();
        	if (polarChartOperator.loadFromFile(strPath))	{
				loadChart(context, strPath);
			} else	{
				return context.getString(R.string.graph_file_cannot_be_read);
        	}
    	} else if (strChartType.compareToIgnoreCase("polarExpr") == 0)	{
    		PolarExprChartOperator polarExprChartOperator = new PolarExprChartOperator();
        	if (polarExprChartOperator.loadFromFile(strPath))	{
				loadChart(context, strPath);
			} else	{
				return context.getString(R.string.graph_file_cannot_be_read);
        	}
		} else if (strChartType.compareToIgnoreCase("multiXYZ") == 0)	{
			OGLChartOperator oglChartOperator = new OGLChartOperator();
        	if (oglChartOperator.loadFromFile(strPath))	{
				loadChart(context, strPath);
			} else	{
				return context.getString(R.string.graph_file_cannot_be_read);
        	}
		} else if (strChartType.compareToIgnoreCase("3DExpr") == 0)	{
			OGLExprChartOperator oglExprChartOperator = new OGLExprChartOperator();
        	if (oglExprChartOperator.loadFromFile(strPath))	{
				loadChart(context, strPath);
			} else	{
				return context.getString(R.string.graph_file_cannot_be_read);
        	}
    	} else	{
    		return context.getString(R.string.graph_settings_wrong);
    	}
		return "";
    }
}