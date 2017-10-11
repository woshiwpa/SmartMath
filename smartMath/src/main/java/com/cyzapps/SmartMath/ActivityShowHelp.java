package com.cyzapps.SmartMath;


import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Scanner;

import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.adapter.AbstractExprConverter;
import com.cyzapps.adapter.AndroidStorageOptions;
import com.cyzapps.adapter.MFPAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.MailTo;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ActivityShowHelp extends Activity {
	/** Called when the activity is first created. */
	String mstrLanguage = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.calculator_help));
		setContentView(R.layout.help_info);

        //First Extract the bundle from intent
        Bundle bundle = getIntent().getExtras();
        String strHelpContent = "main";
        if (bundle != null)	{
        	//Next extract the values using the key as
        	strHelpContent = bundle.getString("HELP_CONTENT");
        }

        String strHelpFileStart = "index";
        if (strHelpContent.equalsIgnoreCase("language_quick_start"))	{
        	strHelpFileStart = "language_quick_start";
        } else if (strHelpContent.equalsIgnoreCase("integration"))	{
        	strHelpFileStart = "HowtoInfo/integration_chart";
        } else if (strHelpContent.equalsIgnoreCase("plot_graph"))	{
        	strHelpFileStart = "HowtoInfo/integration_chart";
        } else if (strHelpContent.equalsIgnoreCase("calculator_gui"))	{
        	strHelpFileStart = "HowtoInfo/calculator_GUI";
        } else if (strHelpContent.equalsIgnoreCase("smartcalc_gui"))	{
        	strHelpFileStart = "HowtoInfo/smartcalc_GUI";
        } else if (strHelpContent.equalsIgnoreCase("config_inputpads"))	{
        	strHelpFileStart = "HowtoInfo/config_inputpads";
        } else if (strHelpContent.equalsIgnoreCase("cmdline"))	{
        	strHelpFileStart = "HowtoInfo/cmdline";
        } else if (strHelpContent.equalsIgnoreCase("settings"))	{
        	strHelpFileStart = "HowtoInfo/settings";
        } else if (strHelpContent.equalsIgnoreCase("file_management"))	{
        	strHelpFileStart = "HowtoInfo/file_management";
        } else if (strHelpContent.equalsIgnoreCase("math_recognition"))	{
        	strHelpFileStart = "HowtoInfo/math_recognition";
        } else if (strHelpContent.equalsIgnoreCase("handwriting_recognition"))	{
        	strHelpFileStart = "HowtoInfo/handwriting_recog";
        } else if (strHelpContent.equalsIgnoreCase("photo_taken_method"))	{
        	strHelpFileStart = "HowtoInfo/take_photo_right_wrong";
        } else if (strHelpContent.equalsIgnoreCase("recog_print"))	{
        	strHelpFileStart = "HowtoInfo/recog_print";
        } else if (strHelpContent.equalsIgnoreCase("main"))	{
        	strHelpFileStart = "index";
       }
        
        WebView webviewHelp = (WebView)findViewById(R.id.webview_help);
        webviewHelp.setVerticalScrollBarEnabled(true);
        webviewHelp.setHorizontalScrollBarEnabled(true);
        webviewHelp.getSettings().setBuiltInZoomControls(true);
        webviewHelp.setWebViewClient(new WebViewClient(){
			@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if( url.startsWith("mailto:") )
				{
					try	{
						// use try...catch block to quench the famous android.util.AndroidRuntimeException: Calling
						// startActivity() from outside of an Activity context requires the FLAG_ACTIVITY_NEW_TASK flag.
						// Is this really what you want? exception.
						Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				        emailIntent.setType("plain/text");
				        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{url.substring(7)});
				        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
				        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
			            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        startActivity(Intent.createChooser(emailIntent, ""));
					} catch(Exception e)	{
						
					}
			        return true;	//intercept it.
				}
		        /*if (url.startsWith("mailto:")) {
		            MailTo mt = MailTo.parse(url);
		            Intent intent = new Intent();
		            intent.setType("text/html");
		            intent.putExtra(Intent.EXTRA_EMAIL, new String[] {mt.getTo()});
		            intent.putExtra(Intent.EXTRA_SUBJECT, mt.getSubject());
		            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            startActivity(Intent.createChooser(intent, "Email ..."));
		            return true;
		        }*/
				return false;
            }
		});
	    Locale l = Locale.getDefault();  
	    mstrLanguage = String.format("%s-%s", l.getLanguage(), l.getCountry());
	    String strIndexAddr;
	    if (mstrLanguage.equals("zh-CN") || mstrLanguage.equals("zh-SG"))	{
	    	strIndexAddr = "file:///android_asset/zh-CN/" + strHelpFileStart + ".html";
	    } else if (mstrLanguage.equals("zh-TW") || mstrLanguage.equals("zh-HK"))	{
		    	strIndexAddr = "file:///android_asset/zh-TW/" + strHelpFileStart + ".html";
	    } else	{
	    	strIndexAddr = "file:///android_asset/en/" + strHelpFileStart + ".html";
	    	mstrLanguage = "en";
	    }
		
		//processFunctionHelp(getAssets());
		//processLanguageHelp(getAssets());
		
		webviewHelp.loadUrl(strIndexAddr);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(keyCode)
			{
			case KeyEvent.KEYCODE_BACK:
				WebView webviewHelp = (WebView)findViewById(R.id.webview_help);
				if(webviewHelp.canGoBack() == true){
					webviewHelp.goBack();
				}else{
					finish();
				}
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	
	/*********************************************\
	 * the following codes are for help adapter. *
	\*********************************************/
	public class FunctionInfo	{
		public String mstrFunctionName = "";
		public String mstrFunctionType = "";
		public String mstrFunctionCategory = "";
		public String mstrFunctionHelp ="";
	}
	
	public LinkedList<FunctionInfo> mlistfunctionInfos = new LinkedList<FunctionInfo>();
	
	public boolean loadFunctionInfo(AssetManager am)	{
		InputStream inputStream = null;
		try {
			inputStream = am.open("en/FunctionInfo/builtin_predefined_functions.txt");
		} catch(IOException e)	{
			// cannot open the file.
			return false;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		Scanner scanner = new Scanner(br);
		try {
			while (scanner.hasNextLine()){
				String strLine = scanner.nextLine();
				String[] strs = strLine.split("\\s+");
				if (strs == null || strs.length < 4)	{
					continue;	// this line is broken.
				}
				FunctionInfo finfo = new FunctionInfo();
				finfo.mstrFunctionName = strs[1];
				finfo.mstrFunctionType = strs[2];
				finfo.mstrFunctionCategory = strs[3];
				int index;
				for (index = 0; index < mlistfunctionInfos.size(); index ++)	{
					if (mlistfunctionInfos.get(index).mstrFunctionName
							.compareToIgnoreCase(finfo.mstrFunctionName) <= 0)	{
						continue;
					} else	{
						break;
					}
				}
				mlistfunctionInfos.add(index, finfo);
			}
		} catch (IllegalStateException e)	{
			return false;
		}
		return true;
	}
	
	public boolean matchSelCondition(FunctionInfo finfo, String strField, String strValue)	{
		if (strField == null)	{
			return true;
		} else if (strField.equalsIgnoreCase("type"))	{
			if (strValue.equalsIgnoreCase(finfo.mstrFunctionType))	{
				return true;
			} else	{
				return false;
			}
		} else if (strField.equalsIgnoreCase("category"))	{
			if (strValue.equalsIgnoreCase(finfo.mstrFunctionCategory))	{
				return true;
			} else	{
				return false;
			}
		} else	{
			return false;
		}
	}
	
	public void outputFunctionInfo(String strFolder, String strFunClass, String strFileName, String strSelCondition)	{
		String[] strsCondition = strSelCondition.split("=");
		String strField = null;
		String strValue = null;
		if (strsCondition.length == 2)	{
			strField = strsCondition[0];
			strValue = strsCondition[1];
		}
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(strFolder
												+ MFPFileManagerActivity.STRING_PATH_DIV
												+ strFileName));
			out.print("<html>\n<head>\n<title>" + getString(R.string.app_name) + "帮助</title>\n");
			out.print("<meta http-equiv=Content-Type content=\"text/html; charset=UTF-8\"></head>\n");
			out.print("<body style=\"background-color:Black;\">\n");
			out.print("<h2 style=\"color:blue\">" + getString(R.string.app_name) + "帮助："
					+ strFunClass + "</h2>\n");
			out.print("<table border=\"1\" style=\"font-family:times;color:green;\">\n");
			out.print("<tr>\n");
			out.print("<th>"+ getString(R.string.function_name) + "</th>\n");
			out.print("<th>"+ getString(R.string.function_info) + "</th>\n");
			out.print("</tr>\n");
			for (int index = 0; index < mlistfunctionInfos.size(); index ++)	{
				FunctionInfo finfo = mlistfunctionInfos.get(index);
				if (matchSelCondition(finfo, strField, strValue) == false)	{
					continue;
				}
				out.print("<tr>\n<td><center>"); 
				out.print(finfo.mstrFunctionName);
				out.print("</center></td>\n<td>");
				String[] strs = finfo.mstrFunctionHelp.split("\\n");
				for (int index1 = 0; index1 < strs.length; index1 ++)	{
					out.print("<p>" + strs[index1] + "</p>\n");
				}
				out.print("</td>\n</tr>\n");
			}
			out.print("</table>\n</body>\n</html>");
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}
	
    public String getLocalLanguage()
    {
	    Locale l = Locale.getDefault();  
	    String strLanguage = l.getLanguage();
	    if (strLanguage.equals("en"))	{
	    	return "English";
	    } else if (strLanguage.equals("fr"))	{
	    	return "French";
	    } else if (strLanguage.equals("de"))	{
	    	return "German";
	    } else if (strLanguage.equals("it"))	{
	    	return "Itanian";
	    } else if (strLanguage.equals("ja"))	{
	    	return "Japanese";
	    } else if (strLanguage.equals("ko"))	{
	    	return "Korean";
	    } else if (strLanguage.equals("zh"))	{
	    	if (l.getCountry().equals("TW") || l.getCountry().equals("HK"))	{
	    		return "Traditional_Chinese";
	    	} else	{
	    		return "Simplified_Chinese";
	    	}
	    } else {
	    	return "";	// unknown language
	    }
    }

    public void processFunctionHelp(AssetManager am)	{
		loadFunctionInfo(am);
		String strLang = getLocalLanguage();
		for (int index = 0; index < mlistfunctionInfos.size(); index ++)	{
			mlistfunctionInfos.get(index).mstrFunctionHelp
				= MFPAdapter.getFunctionHelp(mlistfunctionInfos.get(index).mstrFunctionName, strLang);
		}
		
		String strFolder = AndroidStorageOptions.getSelectedStoragePath()
						+ MFPFileManagerActivity.STRING_PATH_DIV
						+ MFPFileManagerActivity.STRING_APP_FOLDER
						+ MFPFileManagerActivity.STRING_PATH_DIV + "funchelp";
		outputFunctionInfo(strFolder, getString(R.string.all_functions), "all.html", "");
		outputFunctionInfo(strFolder, getString(R.string.builtin_functions), "builtin.html", "type=built-in");
		outputFunctionInfo(strFolder, getString(R.string.predefined_functions), "predefined.html", "type=predefined");
		outputFunctionInfo(strFolder, getString(R.string.integer_operation_functions), "integer_operation.html", "category=integer_operation");
		outputFunctionInfo(strFolder, getString(R.string.logic_functions), "logic.html", "category=logic");
		outputFunctionInfo(strFolder, getString(R.string.statistic_and_stochastic_functions), "statistic_and_stochastic.html", "category=statistic_and_stochastic");
		outputFunctionInfo(strFolder, getString(R.string.trigononmetric_functions), "trigononmetric.html", "category=trigononmetric");
		outputFunctionInfo(strFolder, getString(R.string.exponential_and_logarithmic_functions), "exponential_and_logarithmic.html", "category=exponential_and_logarithmic");
		outputFunctionInfo(strFolder, getString(R.string.complex_number_functions), "complex_number.html", "category=complex_number");
		outputFunctionInfo(strFolder, getString(R.string.system_functions), "system.html", "category=system");
		outputFunctionInfo(strFolder, getString(R.string.array_or_matrix_functions), "array_or_matrix.html", "category=array_or_matrix");
		outputFunctionInfo(strFolder, getString(R.string.graphic_functions), "graphic.html", "category=graphic");
		outputFunctionInfo(strFolder, getString(R.string.expression_and_integration_functions), "expression_and_integration.html", "category=expression_and_integration");
		outputFunctionInfo(strFolder, getString(R.string.string_functions), "string.html", "category=string");
		outputFunctionInfo(strFolder, getString(R.string.hyperbolic_trigononmetric_functions), "hyperbolic_trigononmetric.html", "category=hyperbolic_trigononmetric");
		outputFunctionInfo(strFolder, getString(R.string.sorting_functions), "sorting.html", "category=sorting");
		outputFunctionInfo(strFolder, getString(R.string.others_functions), "others.html", "category=others");
	}

    public boolean cvtTxt2Html(AssetManager am, String strTxtFileName, String strOutputFolder)	{
		InputStream inputStream = null;
		String strLangCodeSet = "UTF8";
		try {
			inputStream = am.open(mstrLanguage + "/LanguageInfo/" + strTxtFileName + ".txt");
		} catch(IOException e)	{
			// cannot open the file.
			return false;
		}
		
		PrintWriter out;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, strLangCodeSet));
			Scanner scanner = new Scanner(br);
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(strOutputFolder
												+ MFPFileManagerActivity.STRING_PATH_DIV
												+ strTxtFileName + ".html"), strLangCodeSet));
			out.print("<html>\n<head>\n<title>" + getString(R.string.app_name) + "帮助</title>\n");
			out.print("<meta http-equiv=Content-Type content=\"text/html; charset=UTF-8\">\n</head>\n");
			out.print("<body style=\"background-color:Black;\">\n");
			int index = 0;
			while (scanner.hasNextLine()){
				String strLine = scanner.nextLine();
				if (index == 0)	{
					out.print("<h2 style=\"color:blue\">");
					out.print(strLine);
					out.print("</h2>\n");
				} else	{
					out.print("<p style=\"font-family:verdana;color:red;\">\n");
					strLine = strLine.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
					out.print(strLine);
					out.print("</p>\n");
				}
				index ++;
			}
			out.print("</body>\n</html>");
			out.close();
		} catch (IOException e)	{
			return false;
		}
		return true;
    }
    
    public void processLanguageHelp(AssetManager am)	{
		String strFolder = AndroidStorageOptions.getSelectedStoragePath()
						+ MFPFileManagerActivity.STRING_PATH_DIV
						+ MFPFileManagerActivity.STRING_APP_FOLDER
						+ MFPFileManagerActivity.STRING_PATH_DIV + "langhelp";
		cvtTxt2Html(am, "array", strFolder);
		cvtTxt2Html(am, "break_continue", strFolder);
		cvtTxt2Html(am, "complex_number", strFolder);
		cvtTxt2Html(am, "function_return_endf", strFolder);
		cvtTxt2Html(am, "help_endh", strFolder);
		cvtTxt2Html(am, "if_elseif_else_endif", strFolder);
		cvtTxt2Html(am, "select_case_default_ends", strFolder);
		cvtTxt2Html(am, "string", strFolder);
		cvtTxt2Html(am, "throw", strFolder);
		cvtTxt2Html(am, "variable", strFolder);
		cvtTxt2Html(am, "while_loop_do_until_for_next", strFolder);
    }
}
