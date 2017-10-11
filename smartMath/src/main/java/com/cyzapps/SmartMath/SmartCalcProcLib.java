package com.cyzapps.SmartMath;

import java.util.LinkedList;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.cyzapps.SmartMath.ActivitySmartCalc.PlotGraphPlotter;
import com.cyzapps.SmartMath.ActivitySmartCalc.SmartCalcAbstractExprInterrupter;
import com.cyzapps.SmartMath.ActivitySmartCalc.SmartCalcFunctionInterrupter;
import com.cyzapps.SmartMath.ActivitySmartCalc.SmartCalcScriptInterrupter;
import com.cyzapps.GraphDemon.ActivityChartDemon;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jmfp.ScriptAnalyzer;
import com.cyzapps.Jmfp.SolveAnalyzer;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEAssign;
import com.cyzapps.Jsma.AECompare;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEFunction;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AEPosNegOpt;
import com.cyzapps.Jsma.AEVar;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.Jsma.PtnSlvMultiVarsIdentifier;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Jsma.UnknownVarOperator;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import com.cyzapps.MFPFileManager.MFPFileManagerActivity;
import com.cyzapps.PlotAdapter.ChartOperator;
import com.cyzapps.adapter.AbstractExprConverter;
import com.cyzapps.adapter.MFPAdapter;

public class SmartCalcProcLib {
    public static String quickhelp(Context context, String strExpression)
	{
		String strOutput = "";
		strExpression = strExpression.trim();
		if ((strExpression.length() >= 5 && strExpression.substring(0, 5).toLowerCase(Locale.US).equals("help ") == false)
			|| (strExpression.length() == 4 && strExpression.substring(0, 4).toLowerCase(Locale.US).equals("help") == false)
			|| (strExpression.length() < 4))	{
			strOutput = context.getString(R.string.error_in_help_requirement) + "!";
		} else if (strExpression.toLowerCase(Locale.US).trim().equals("help"))	{
			strOutput = recalcWidthHeight4Help(context, context.getString(R.string.welcome_message4));
		} else	{
			// the length of the string must be larger than 4 now.
			String strHelpReq = strExpression.substring(4).trim().toLowerCase(Locale.US);
			
			if (strHelpReq.equals("")) {
				strOutput = recalcWidthHeight4Help(context, context.getString(R.string.welcome_message4));
			} else if (strHelpReq.equals("x")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.x_help_info);
			} else if (strHelpReq.equals("y")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.y_help_info);
			} else if (strHelpReq.equals("z")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.z_help_info);
			} else if (strHelpReq.equals("u")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.u_help_info);
			} else if (strHelpReq.equals("v")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.v_help_info);
			} else if (strHelpReq.equals("t")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.t_help_info);
			} else if (strHelpReq.equals("r")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.r_help_info);
			} else if (strHelpReq.equals("\u03B8")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.theta_help_info);
			} else if (strHelpReq.equals("=")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.assign_help_info);
			} else if (strHelpReq.equals("==")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.equal_help_info);
			} else if (strHelpReq.equals("(")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.parenthesis_help_info);
			} else if (strHelpReq.equals(")")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.closeparenthesis_help_info);
			} else if (strHelpReq.equals("[")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.squarebracket_help_info);
			} else if (strHelpReq.equals("]")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.closesquarebracket_help_info);
			} else if (strHelpReq.equals(",")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.comma_help_info);
			} else if (strHelpReq.equals("+")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.plus_help_info);
			} else if (strHelpReq.equals("-")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.minus_help_info);
			} else if (strHelpReq.equals("*")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.multiplication_help_info);
			} else if (strHelpReq.equals("/")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.division_help_info);
			} else if (strHelpReq.equals("\\")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.leftdivision_help_info);
			} else if (strHelpReq.equals("**")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.power_help_info);
			} else if (strHelpReq.equals("'")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.transpose_help_info);
			} else if (strHelpReq.equals("\"")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.doublequote_help_info);
			} else if (strHelpReq.equals("!")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.exclaimation_help_info);
			} else if (strHelpReq.equals("%")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.percentage_help_info);
			} else if (strHelpReq.equals("&")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.bit_and_help_info);
			} else if (strHelpReq.equals("|")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.bit_or_help_info);
			} else if (strHelpReq.equals("^")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.bit_xor_help_info);
			} else if (strHelpReq.equals("~")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.bit_not_help_info);
			} else if (strHelpReq.equals("i")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.image_i_help_info);
			} else if (strHelpReq.equals("pi")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.pi_constant_help_info);
			} else if (strHelpReq.equals("e")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.e_constant_help_info);
			} else if (strHelpReq.equals("null")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.null_constant_help_info);
			} else if (strHelpReq.equals("true")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.true_constant_help_info);
			} else if (strHelpReq.equals("false")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.false_constant_help_info);
			} else if (strHelpReq.equals("inf")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.inf_constant_help_info);
			} else if (strHelpReq.equals("infi")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.infi_constant_help_info);
			} else if (strHelpReq.equals("nan")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.nan_constant_help_info);
			} else if (strHelpReq.equals("nani")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.nani_constant_help_info);
			} else if (strHelpReq.equals("0x")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.hex_oct_bin_initial_help_info);
			} else if (strHelpReq.equals("00")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.hex_oct_bin_initial_help_info);
			} else if (strHelpReq.equals("0b")) {
				strOutput = strHelpReq + " : " + context.getString(R.string.hex_oct_bin_initial_help_info);
			} else if ((strOutput = MFPAdapter.getMFPKeyWordHelp(strHelpReq, getLocalLanguage())) != null)	{
				// is key word.
			} else	{
				strOutput = "";
				String[] strHelpReqParts = strHelpReq.split("\\(");
				if (strHelpReqParts.length == 2)	{
					boolean bRightFormat = true;
					String strFuncName = strHelpReqParts[0].trim();
					String strNumofParams = "";
					int nNumofParams = 0;
					boolean bIncludeOptionParam = false;
					if (strHelpReqParts[1].trim().matches("[0-9]*\\s*\\.\\.\\.\\s*\\)"))	{
						strNumofParams = strHelpReqParts[1].split("\\.\\.\\.")[0].trim();
						bIncludeOptionParam = true;
					} else if (strHelpReqParts[1].trim().matches("[0-9]*\\s*\\)"))	{
						String[] strParts = strHelpReqParts[1].split("\\)");
                        if (strParts.length > 0)    {   // if it is ")", then strParts.length = 0
                            strNumofParams = strParts[0].trim();
                        } else  {
                            strNumofParams = "";
                        }
					} else	{
						bRightFormat = false;
					}
					if (strNumofParams.trim().equals(""))	{
						nNumofParams = 0;
					} else {
						try {
							nNumofParams = Integer.parseInt(strNumofParams.trim());
						} catch (NumberFormatException e) {
							bRightFormat = false;
						}
					}
					if (bRightFormat)	{
						strOutput = MFPAdapter.getFunctionHelp(strFuncName,
																nNumofParams,
																bIncludeOptionParam,
																getLocalLanguage());
					} 
					if (strOutput == null || strOutput.trim().equals(""))	{
						strOutput = context.getString(R.string.no_quick_help_info) + " " + strHelpReq;
					}
				} else if (strHelpReqParts.length == 1) {
					strOutput = MFPAdapter.getFunctionHelp(strHelpReq, getLocalLanguage());
					if (strOutput.equals(""))	{
						strOutput = context.getString(R.string.no_quick_help_info) + " " + strHelpReq;
					}
				}
			}
		}
		return "<p class=\"quickhelp\">" + strOutput.replace("\n", "</p><p>") + "</p>";
	}

    public static String getLocalLanguage()
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
	
	public static String recalcWidthHeight4Help(Context context, String strHelp) {
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    float fRelativeDPI = metrics.densityDpi / 160f;
	    int nShortSide = (int) Math.min(metrics.widthPixels/fRelativeDPI, metrics.heightPixels/fRelativeDPI);
	    int nOldWidth, nOldHeight, nNewWidth, nNewHeight;
	    nNewWidth = (int)((metrics.widthPixels + metrics.heightPixels)/4/fRelativeDPI);
	    // 400, 240
	    nOldWidth = 400;
	    nOldHeight = 240;
	    nNewHeight = (nOldHeight * nNewWidth)/nOldWidth;
	    String strToBeReplaced400_240 = "WITH_HEIGHT_TO_RECALC(400,240)";
	    String strRelpaced400_240 = "width=\"" + nNewWidth + "\"; height=\"" + nNewHeight + "\";";
	    // 800, 480
	    nOldWidth = 800;
	    nOldHeight = 480;
	    nNewHeight = (nOldHeight * nNewWidth)/nOldWidth;
	    String strToBeReplaced800_480 = "WITH_HEIGHT_TO_RECALC(800,480)";
	    String strRelpaced800_480 = "width=\"" + nNewWidth + "\"; height=\"" + nNewHeight + "\";";
	    return strHelp.replace(strToBeReplaced400_240, strRelpaced400_240).replace(strToBeReplaced800_480, strRelpaced800_480);
	}
	
	public static String calculate(Context context, String strExpressions, boolean bShowInValidAExpr)
	{
		/*
		 * make sure that we do not output any log or show any chart in calculator screen
		 * or be interrupted.
		 * but calculator is still able to save files in disk.
		 */
		FuncEvaluator.msstreamConsoleInput = null;
		FuncEvaluator.msstreamLogOutput = null;
		FuncEvaluator.msfunctionInterrupter = new SmartCalcFunctionInterrupter();
		FuncEvaluator.msfileOperator = new MFPFileManagerActivity.MFPFileOperator();
		FuncEvaluator.msgraphPlotter = null;
		FuncEvaluator.msgraphPlotter3D = null;
        if (FuncEvaluator.mspm == null) {
            FuncEvaluator.mspm = new PatternManager();
            try {
				FuncEvaluator.mspm.loadPatterns(2);	// load pattern is a very time consuming work. So only do this if needed.
			} catch (Exception e) {
				 // load all integration patterns. Assume load patterns will not throw any exceptions.
			}
         }
		ScriptAnalyzer.msscriptInterrupter = new SmartCalcScriptInterrupter();
		AbstractExpr.msaexprInterrupter = new SmartCalcAbstractExprInterrupter();
		
		ExprEvaluator exprEvaluator = new ExprEvaluator();
		// clear variable namespaces
		exprEvaluator.m_lVarNameSpaces = new LinkedList<LinkedList<Variable>>();

		String[] strarrayExprs = strExpressions.split("\n");
		String strOutput = "";
		String strOriginalExprColor = "color:#008000;", strExprColor = "color:#0000ff;", strResultColor = "color:#FFA500;",
				strVarValueColor = "color:#ff0000;", strVarNameColor = "color:#800080;";
		LinkedList<AbstractExpr> listaeInputExprs = new LinkedList<AbstractExpr>();
        LinkedList<UnknownVariable> listVarUnknown = new LinkedList<UnknownVariable>();	// The unknown variable list
        LinkedList<LinkedList<Variable>> lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
		for (int idx = 0; idx < strarrayExprs.length; idx ++)	{
			if (strarrayExprs[idx].trim().length() == 0)	{
				continue;	// empty string
			} else if ((strarrayExprs[idx].trim().length() >= 5 && strarrayExprs[idx].trim().substring(0, 5).toLowerCase(Locale.US).equals("help "))
				|| strarrayExprs[idx].trim().toLowerCase(Locale.US).equals("help"))	{
				strOutput += quickhelp(context, strarrayExprs[idx].trim()) + "\n";	// help information
			} else	{
				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				String strarrayAnswer[] = new String[2];
				AbstractExpr aexpr = new AEInvalid();
				try	{
                    /* evaluate the expression */
                    aexpr = ExprAnalyzer.analyseExpression(strarrayExprs[idx], curpos);
			        LinkedList<AbstractExpr> listAEVars = new LinkedList<AbstractExpr>();
			        LinkedList<AbstractExpr> listAERootVars = new LinkedList<AbstractExpr>();
			        AbstractExpr[] arrayAEs = new AbstractExpr[1];
			        arrayAEs[0] = aexpr;
			        PtnSlvMultiVarsIdentifier.lookupToSolveVarsInExprs(arrayAEs, listAEVars, listAERootVars);
					
			        LinkedList<Variable> listVarThisSpace = new LinkedList<Variable>();
					lVarNameSpaces.add(listVarThisSpace);
			        for (int idx1 = 0; idx1 < listAEVars.size(); idx1 ++)	{
			        	if (listAEVars.get(idx1) instanceof AEVar)	{
			        		String strName = ((AEVar)listAEVars.get(idx1)).mstrVariableName;
			        		if (VariableOperator.lookUpPreDefined(strName) == null)	{	// this variable is not a predefined var nor does it exist
				        		UnknownVariable varUnknown = new UnknownVariable(strName);
				        		// if this variable hasn't been added, add it.
				        		if (UnknownVarOperator.lookUpList(strName, listVarUnknown) == null)	{
				        			listVarUnknown.add(varUnknown);
				        		}
				        		if (VariableOperator.lookUpList(strName, listVarThisSpace) == null)	{
				        			listVarThisSpace.add(varUnknown);
				        		}
			        		}
			        	}
			        }
			        AbstractExpr aeSimplified = new AEInvalid();
			        // shouldn't use exprEvaluator.evaluateExpression coz this function cannot set unknown var assigned.
 			        aeSimplified = aexpr.simplifyAExprMost(listVarUnknown, lVarNameSpaces, new SimplifyParams(false, false, false));
                    if (aeSimplified instanceof AEConst)	{ // ok, we get the value!
                    	String strResultOutput = MFPAdapter.outputDatum(((AEConst) aeSimplified).getDataClassRef())[1];
                    	strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
    							+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
    							// + AbstractExprConverter.convtAExpr2JQMath(aexpr) + "$</a>&nbsp;<big>&rArr;</big>&nbsp;<a href=\""	// do not use convtAExprJQMath here because it cannot properly show hex or binary values.
    							+ AbstractExprConverter.convtPlainStr2JQMathNoException(strarrayExprs[idx]) + "$</a>&nbsp;<big>&rArr;</big>&nbsp;<a href=\""
								+ AbstractExprConverter.convtPlainStr2QuotedUrl(strResultOutput)
								+ "\" style=\"text-decoration: none;" + strResultColor + "\">$";
								//+ AbstractExprConverter.convtAExpr2JQMath(aeSimplified) + "$</a></p>\n";
                    	boolean bConvertOutput2Expr = false;
                    	if (aexpr instanceof AEFunction) {
                    		String strFuncName = ((AEFunction)aexpr).mstrFuncName.trim();
                    		if (strFuncName.equalsIgnoreCase("integrate") && ((AEFunction)aexpr).mlistChildren.size() == 2
                    				&& ((AEConst) aeSimplified).getDataClassRef().getDataType() == DATATYPES.DATUM_STRING) {
                    			bConvertOutput2Expr = true;	// indefinite integration result is a string based expression. 
                    		}
                    	}
                    	if (bConvertOutput2Expr) {
                    		try {
                    			// convert to expression and then output.
                    			AbstractExpr aeIndefIntegResult = ExprAnalyzer.analyseExpression(((AEConst) aeSimplified).getDataClassRef().getStringValue(), new CurPos());
                    			strOutput += AbstractExprConverter.convtAExpr2JQMath(aeIndefIntegResult) + "$</a></p>\n";
                    		} catch (Exception e) {
                    			// cannot convert anyway.
                    			strOutput += AbstractExprConverter.convtAExpr2JQMath(aeSimplified) + "$</a></p>\n";
                    		}
                    	} else {
                    		strOutput += AbstractExprConverter.convtAExpr2JQMath(aeSimplified) + "$</a></p>\n";
                    	}
                    } else {	// we just simplify it, still need solver
                    	strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
    							+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
    							// + AbstractExprConverter.convtAExpr2JQMath(aexpr) + "$</a></p>";	// do not use convtAExprJQMath here because it cannot properly show hex or binary values.
    							+ AbstractExprConverter.convtPlainStr2JQMathNoException(strarrayExprs[idx]) + "$</a></p>";
                    	listaeInputExprs.add(aeSimplified);
                    }
				} catch (Exception e)	{
					if (bShowInValidAExpr) {	// camera calculation preview will not show any invalid expression.
						strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
								+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
								+ TextUtils.htmlEncode(strarrayExprs[idx]) + "</a></p>\n";
						strarrayAnswer[0] = "Error";
						strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_solve) + " : ";
						if (e instanceof JSmartMathErrException
								&& ((JSmartMathErrException)e).m_se.m_enumErrorType
									== SMErrProcessor.ERRORTYPES.ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE)	{
							strarrayAnswer[1] += context.getString(R.string.did_you_use_assign) + "</p>";
						} else	{
							strarrayAnswer[1] += MFPAdapter.outputException(e) + ".</p>";
						}
						strOutput += strarrayAnswer[1];
					}
				}
			}
		}

		boolean bNeedArrow = true;
        for (int idx1 = 0; idx1 < listVarUnknown.size(); idx1 ++)	{
        	if (listVarUnknown.get(idx1).isValueAssigned())	{
        		// this means value is assigned in the expression.
        		String strVarName = listVarUnknown.get(idx1).getName();
        		String strVarValue;
				try {
					if (bNeedArrow)	{
						strOutput += "<p> <big>&rarr;</big> </p>";
						bNeedArrow = false;
					}
					strVarValue = MFPAdapter.outputDatum(listVarUnknown.get(idx1).getSolvedValue())[1];
	                strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strVarName)
							+ "\" style=\"text-decoration: none;" + strVarNameColor + "\">$"
							+ AbstractExprConverter.convtPlainStr2JQMath(strVarName, false) + "$</a> = <a href=\""
							+ AbstractExprConverter.convtPlainStr2QuotedUrl(strVarValue)
							+ "\" style=\"text-decoration: none;" + strVarValueColor + "\">$"
							+ AbstractExprConverter.convtAExpr2JQMath(new AEConst(listVarUnknown.get(idx1).getSolvedValue()))
							+ "$</a></p>\n";
				} catch (Exception e) {
					// do not print variable and its value if there is any exception.
				}
        		
        	}
        }
        
		boolean bContinue2Solve = true;
		if (listaeInputExprs.size() != 0)	{
			// now construct a solver.
			if (bNeedArrow)	{
				strOutput += "<p> <big>&rarr;</big> </p>";
				bNeedArrow = false;
			}
			LinkedList<AbstractExpr> listaeOriginals = new LinkedList<AbstractExpr>();
			String strExprs4Solver = new String();
			for (int idx = 0; idx <listaeInputExprs.size(); idx ++)	{
				try	{
		            AbstractExpr aeReturn = listaeInputExprs.get(idx);
		            //aeReturn = aeReturn.simplifyAExprMost(listVarUnknown, lVarNameSpaces);    // do not simplify most here.
		            if ( aeReturn instanceof AECompare && ((AECompare) aeReturn).moptType == OPERATORTYPES.OPERATOR_EQ)	{
		                // move the part right to == to left and constract a pos-neg opt aexpr.
		                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
		                listChildren.add(((AECompare) aeReturn).maeLeft.cloneSelf());
		                listChildren.add(((AECompare) aeReturn).maeRight.cloneSelf());
		                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
		                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
		                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
		                listaeOriginals.add(new AEPosNegOpt(listChildren, listOpts));
		                strExprs4Solver += "<p><a href=\"" + AbstractExprConverter.convtAExpr2QuotedUrl(listaeInputExprs.get(idx))
										+ "\" style=\"text-decoration: none;" + strExprColor + "\">$"
										+ AbstractExprConverter.convtAExpr2JQMath(listaeInputExprs.get(idx)) + "$</a></p>";
		            } else  {
		                strOutput += "<p><a href=\"" + AbstractExprConverter.convtAExpr2QuotedUrl(listaeInputExprs.get(idx))
								+ "\" style=\"text-decoration: none;" + strExprColor + "\">$"
								+ AbstractExprConverter.convtAExpr2JQMath(listaeInputExprs.get(idx))
		                		+ "$</a> <big>&rarr;</big> " + context.getString(R.string.invalid_expr_type_to_solve) + "!</p>";		
		                bContinue2Solve = false;
		                break;
		            }
				} catch (Exception e)	{
					try {
		                strOutput += "<p><a href=\"" + AbstractExprConverter.convtAExpr2QuotedUrl(listaeInputExprs.get(idx))
										+ "\" style=\"text-decoration: none;" + strExprColor + "\">$"
										+ AbstractExprConverter.convtAExpr2JQMath(listaeInputExprs.get(idx))
										+ "$</a> <big>&rarr;</big> " + context.getString(R.string.invalid_expr_to_solve) + "!\n";		
					} catch (Exception e1)	{
						strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(listaeInputExprs.get(idx).toString())
								+ "\" style=\"text-decoration: none;" + strExprColor + "\">"
								+ TextUtils.htmlEncode(listaeInputExprs.get(idx).toString()) + "</a> <big>&rarr;</big> "
								+ context.getString(R.string.invalid_expr_to_solve) + "!\n";		
					}
					bContinue2Solve = false;
	                break;
				}
			}
			
			if (bContinue2Solve)	{
				try	{
			        AbstractExpr[] aeOriginalExprs = new AbstractExpr[listaeOriginals.size()];
			        for (int index = 0; index < listaeOriginals.size(); index ++)	{
			            aeOriginalExprs[index] = listaeOriginals.get(index);
			        }

			        // aeOriginalExprs.length must be non-zero.
					// first of all, output the expressions.
					// strOutput += strExprs4Solver + "<p> <big>&rarr;</big> </p>"; // not want too many middle level output.
	                LinkedList<UnknownVariable> listAlreadyPrinted = UnknownVarOperator.cloneUnknownVarList(listVarUnknown);
	                AbstractExpr[] aeOriginalExprsOld = aeOriginalExprs;
	                // load patterns only if we need it.
	    			if (SolveAnalyzer.mspm == null)	{
	    				SolveAnalyzer.mspm = new PatternManager();
	    				try {
	    					SolveAnalyzer.mspm.loadPatterns(13);
	    				} catch (Exception e) {
	    					// TODO Do something if load pattern failed;
	    				}
	    			}
		            aeOriginalExprs = SolveAnalyzer.mspm.simplifyByPtnSlvVarIdentifier(aeOriginalExprsOld, listVarUnknown, lVarNameSpaces);
		            for (int idx = 0; idx < listVarUnknown.size(); idx ++)  {
		                if (listVarUnknown.get(idx).isValueAssigned()
		                		&& (!listAlreadyPrinted.get(idx).isValueAssigned()
		                				|| !listAlreadyPrinted.get(idx).getSolvedValue().isEqual(listVarUnknown.get(idx).getSolvedValue())))  {
		                	// value is changed during Pattern identifying
		                    String strVarName = listVarUnknown.get(idx).getName();
		                    Variable var = VariableOperator.lookUpSpaces(strVarName, lVarNameSpaces);
		                    if (var != null)    {
		                        var.setValue(listVarUnknown.get(idx).getSolvedValue());
		                    }
		                    // need to simplify MFPAdapter.outputDatum(listVarUnknown.get(idx).getSolvedValue())[1] because
		                    // the outputDatum might be 3 * i, and cannot be initialized as AEConst, so converted to 3 * 1 * i
		                    String strValueOutput = MFPAdapter.outputDatum(listVarUnknown.get(idx).getSolvedValue())[1];
		                    strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strVarName)
										+ "\" style=\"text-decoration: none;" + strVarNameColor + "\">$"
										+ AbstractExprConverter.convtPlainStr2JQMath(strVarName, false) + "$</a> = <a href=\""
										+ AbstractExprConverter.convtPlainStr2QuotedUrl(strValueOutput)
										+ "\" style=\"text-decoration: none;" + strVarValueColor + "\">$"
										+ AbstractExprConverter.convtAExpr2JQMath(new AEConst(listVarUnknown.get(idx).getSolvedValue()))
										+ "$</a></p>\n";
		                }
		            }

		            if (aeOriginalExprs.length != 0)    {
		                // then try to use pattern analyzer
		            	// first, print simplified expression(s)
		            	String strExprs4PatternAnalyser = "";
		            	for (int idx = 0; idx < aeOriginalExprs.length; idx ++)	{
		            		AECompare aeEqualZero = new AECompare(aeOriginalExprs[idx],
		            											OPERATORTYPES.OPERATOR_EQ,
		            											new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)));
		            		
		            		strExprs4PatternAnalyser += "<p><a href=\"" + AbstractExprConverter.convtAExpr2QuotedUrl(aeEqualZero)
										+ "\" style=\"text-decoration: none;" + strExprColor + "\">$"
										+ AbstractExprConverter.convtAExpr2JQMath(aeEqualZero) + "$</a></p>";
		            	}
		            	if (AbstractExpr.isExprsEqual(aeOriginalExprs, aeOriginalExprsOld) == false)	{
		            		strOutput += strExprs4PatternAnalyser + "<p> <big>&rarr;</big> </p>";
		            	}
		                listAlreadyPrinted = UnknownVarOperator.cloneUnknownVarList(listVarUnknown);
		                // only run SolveAnalyzer.solveExprVars once to save time.
	                    LinkedList<LinkedList<UnknownVariable>> listAllResultSets = SolveAnalyzer.solveExprVars(SolveAnalyzer.mspm, aeOriginalExprs, listVarUnknown, lVarNameSpaces);
		                if (listAllResultSets.size() > 0)   {
		                	strOutput += "<pre>";
		                    // TODO: at this moment only store all the roots in an array. In the future should have more choices.
		                	for (int idx = 0; idx < listAllResultSets.get(0).size(); idx ++)  {
		                		String strVarName = listAllResultSets.get(0).get(idx).getName();
		                		UnknownVariable varSolved = UnknownVarOperator.lookUpList(strVarName, listAlreadyPrinted);
		                		if (varSolved != null && varSolved.isValueAssigned())	{
		                			continue;	// this value must be printed before.
		                		}
		                		String strOneVarOutput = new String();
		                		int nLineStartPos = 0;
		                		strOneVarOutput += "<a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strVarName)
								+ "\" style=\"text-decoration: none;" + strVarNameColor + "\">$"
								+ AbstractExprConverter.convtPlainStr2JQMath(strVarName, false) + "$</a> = ";
			                	if (listAllResultSets.size() > 1)	{
			                		strOneVarOutput += "{ ";
			                	}
		                        DataClass[] datumList = new DataClass[listAllResultSets.size()];
		                        int  nAssignedValue = 0;
		                        for (int idx1 = 0; idx1 < listAllResultSets.size(); idx1 ++)    {
		                            datumList[idx1] = new DataClass();
		                            if (listAllResultSets.get(idx1).get(idx).isValueAssigned())	{
		                            	datumList[idx1] = listAllResultSets.get(idx1).get(idx).getSolvedValue();
		                            	nAssignedValue ++;
		                            }
		                            String strOneRoot = MFPAdapter.outputDatum(datumList[idx1])[1];
		    						// need to simplify most b4 convert plain string to JQMath because like 3*i cannot be initialized to an AEConst
		                            strOneVarOutput += "<a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strOneRoot)
										+ "\" style=\"text-decoration: none;" + strVarValueColor + "\">$"
										+ AbstractExprConverter.convtAExpr2JQMath(new AEConst(datumList[idx1])) + "$</a>";
		                            if (idx1 != listAllResultSets.size() - 1)	{
		                            	strOneVarOutput += ", ";
		                            	if (strOneVarOutput.length() - nLineStartPos >= 20)	{
		                            		// the result is very long so that it takes whole line. next result should be placed in next line.
		                            		strOneVarOutput += "\n";
		                            		nLineStartPos = strOneVarOutput.length();
		                            	}
		                            }
		                        }
			                	if (listAllResultSets.size() > 1)	{
			                		strOneVarOutput += " }";
			                	}
			                	if (nAssignedValue != 0)	{
			                		strOutput += strOneVarOutput + "\n";
			                        DataClass datumValue = new DataClass();
			                        datumValue.setDataList(datumList);
			                        Variable var = VariableOperator.lookUpSpaces(listAllResultSets.get(0).get(idx).getName(), lVarNameSpaces);
			                        // var should exist in lVarNameSpaces
			                        var.setValue(datumValue);
			                	}
		                    }
		                	strOutput += "</pre>";
		                }
		            }
				} catch (Exception e)	{
					if (e instanceof JSmartMathErrException
							&& ((JSmartMathErrException)e).m_se.m_enumErrorType
									== SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN)	{
						strOutput += "<p>" + context.getString(R.string.expr_cannot_solve) + "!</p>";

					} else 	{
						strOutput += "<p>" + context.getString(R.string.expr_cannot_solve) + "!</p>";	// some other exception may not be invalid.
					}
				}
			}
		}
		return strOutput;
	}
	
	public static String plot(Context context, Context contextPlot, String strExpressions, long lPlotTriggerTime, boolean bStopWhenInValidAExpr)	{
		String strOutput = "";
		String strOriginalExprColor = "color:#008000;", strExprColor = "color:#0000ff;", strResultColor = "color:#FFA500;",
				strVarValueColor = "color:#ff0000;", strVarNameColor = "color:#800080;";
		String[] strarrayExprs = strExpressions.split("\n");
		LinkedList<AbstractExpr> listaeInputExprs = new LinkedList<AbstractExpr>();
		LinkedList<String> liststrOriginalExprs = new LinkedList<String>();
		LinkedList<AbstractExpr> listaeOriginalExprs = new LinkedList<AbstractExpr>();
		LinkedList<AEVar> listaeInputEqualVars = new LinkedList<AEVar>();
		LinkedList<LinkedList<UnknownVariable>> listlVarUnknownExprsAll = new LinkedList<LinkedList<UnknownVariable>>();
        LinkedList<UnknownVariable> listVarUnknown = new LinkedList<UnknownVariable>();	// The unknown variable list
        LinkedList<int[]> listOriginal2Solved = new LinkedList<int[]>();
        LinkedList<Integer> listSolved2Original = new LinkedList<Integer>();
		boolean bCanPlot = true;
		for (int idx = 0; idx < strarrayExprs.length; idx ++)	{
			String strarrayAnswer[] = new String[2];
			if (strarrayExprs[idx].trim().length() == 0)	{
				continue;	// empty string
			} else if ((strarrayExprs[idx].trim().length() >= 5 && strarrayExprs[idx].trim().substring(0, 5).toLowerCase(Locale.US).equals("help "))
					|| strarrayExprs[idx].trim().toLowerCase(Locale.US).equals("help"))	{
            	strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
							+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
							+ strarrayExprs[idx] + "</a></p>\n";
				strarrayAnswer[0] = "Error";
				strarrayAnswer[1] = "<p>" + context.getString(R.string.help_statement_cannot_be_plotted) + "</p>";
				strOutput += strarrayAnswer[1];
				if (bStopWhenInValidAExpr) {
					bCanPlot = false;	// help information, cannot be plotted.
				}
			} else	{
				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				AbstractExpr aexpr = AEInvalid.AEINVALID;
                try {
					aexpr = ExprAnalyzer.analyseExpression(strarrayExprs[idx], curpos);
					strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
								+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
								+ AbstractExprConverter.convtPlainStr2JQMathNoException(strarrayExprs[idx]) + "$</a></p>\n";
					try {
						LinkedList<AbstractExpr> listAEVars = new LinkedList<AbstractExpr>();
				        LinkedList<UnknownVariable> listVarUnknownSingleExpr = new LinkedList<UnknownVariable>();
				        LinkedList<AbstractExpr> listAERootVars = new LinkedList<AbstractExpr>();
				        AbstractExpr[] arrayAEs = new AbstractExpr[1];
				        arrayAEs[0] = aexpr;
				        PtnSlvMultiVarsIdentifier.lookupToSolveVarsInExprs(arrayAEs, listAEVars, listAERootVars);					
	           	
						int nNumOfToSolves = 0;
						for (int idx1 = 0; idx1 < listAEVars.size(); idx1 ++)	{
				        	if (listAEVars.get(idx1) instanceof AEVar)	{
				        		String strName = ((AEVar)listAEVars.get(idx1)).mstrVariableName;
				        		if (VariableOperator.lookUpPreDefined(strName) == null)	{	// this variable is not a predefined var nor does it exist
				        			nNumOfToSolves ++;
				        		}
				        	}
						}
						// if do not stop if invalid aexpr, we need to check the number of vars for this expr, if too many, skip this one.
						// too many variables to solve. This is not a valid expression.
						
				        if (nNumOfToSolves == 0)	{
							strarrayAnswer[0] = "Error";
							strarrayAnswer[1] = "<p>" + context.getString(R.string.no_variable_to_plot) + "</p>";
							strOutput += strarrayAnswer[1];
				        	if (bStopWhenInValidAExpr) {
					        	bCanPlot = false;	// no variable, cannot be plotted.
				        	}
				        	continue;
				        } else {
				        	boolean bTooManyVar2Solve = false;
							if (nNumOfToSolves > 3 && (aexpr instanceof AEAssign || aexpr instanceof AECompare)) {
								bTooManyVar2Solve = true;
							} else if (nNumOfToSolves > 2 && !(aexpr instanceof AEAssign || aexpr instanceof AECompare)) {
								bTooManyVar2Solve = true;
							}
							
							if (bTooManyVar2Solve) 	{
								strarrayAnswer[0] = "Error";
								strarrayAnswer[1] = "<p>" + context.getString(R.string.cannot_plot_more_than_3d_chart) + "</p>";
								strOutput += strarrayAnswer[1];
								if (bStopWhenInValidAExpr) {
									bCanPlot = false;	// help information, cannot be plotted.
								}
								continue;
							}
				        }
						
				        // since we have ensured that number of variables is valid, we add them in variable list.
				        for (int idx1 = 0; idx1 < listAEVars.size(); idx1 ++)	{
				        	if (listAEVars.get(idx1) instanceof AEVar)	{
				        		String strName = ((AEVar)listAEVars.get(idx1)).mstrVariableName;
				        		if (VariableOperator.lookUpPreDefined(strName) == null)	{	// this variable is not a predefined var nor does it exist
				        			UnknownVariable varUnknown = new UnknownVariable(strName);
					        		// if this variable hasn't been added, add it.
					        		if (UnknownVarOperator.lookUpList(strName, listVarUnknown) == null)	{
					        			listVarUnknown.addFirst(varUnknown);	// not use add last coz y=x*sin(x), hope to see y is the second, not the first variable.
					        		}
					        		if (UnknownVarOperator.lookUpList(strName, listVarUnknownSingleExpr) == null)	{
					        			listVarUnknownSingleExpr.add(varUnknown);
					        		}
				        		}
				        	}
				        }
				        
				        if (aexpr instanceof AEAssign)	{
				        	if (aexpr.getListOfChildren().size() != 2)	{
								strarrayAnswer[0] = "Error";
								strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + " " + context.getString(R.string.did_you_use_assign) + "</p>";
								strOutput += strarrayAnswer[1];
								if (bStopWhenInValidAExpr) {
									bCanPlot = false;	// invalid expression, cannot be plotted. Print error message here so that user can see.
								}
				        	} else if (!(aexpr.getListOfChildren().getFirst() instanceof AEVar)) {
								strarrayAnswer[0] = "Error";
								strarrayAnswer[1] = "<p>" + context.getString(R.string.cannot_assign_value_to_anything_except_variable_use_equal_instead) + " " + context.getString(R.string.did_you_use_assign) + "</p>";
								strOutput += strarrayAnswer[1];
								if (bStopWhenInValidAExpr) {
									bCanPlot = false;	// invalid expression, cannot be plotted. Print error message here so that user can see.
								}
				        	} else	{
								listaeInputExprs.add(aexpr.getListOfChildren().getLast());
								listaeInputEqualVars.add((AEVar)aexpr.getListOfChildren().getFirst());
								liststrOriginalExprs.add(strarrayExprs[idx]);
								listaeOriginalExprs.add(aexpr);
								int[] narrayMap = new int[1];
								narrayMap[0] = listaeInputExprs.size() - 1;
								listOriginal2Solved.add(narrayMap);
								listSolved2Original.add(listaeOriginalExprs.size() - 1);
					        	listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
				        	}
						} else if (aexpr instanceof AECompare)	{
							if (((AECompare) aexpr).moptType == OPERATORTYPES.OPERATOR_EQ)	{
								LinkedList<AbstractExpr> listaeSolvedResults = null;
								if (aexpr.getListOfChildren().size() != 2)	{
									strarrayAnswer[0] = "Error";
									strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>";
									strOutput += strarrayAnswer[1];
									if (bStopWhenInValidAExpr) {
										bCanPlot = false;	// no variable in either side of the equation, cannot be plotted. Print error message here so that user can see.
									}
								} else if ((aexpr.getListOfChildren().getFirst() instanceof AEVar) || (aexpr.getListOfChildren().getLast() instanceof AEVar))	{
									boolean bNoValidUnknownInFirst = true, bNoValidUnknownInLast = true;
									if (aexpr.getListOfChildren().getFirst() instanceof AEVar)	{
										String strVarName = ((AEVar)(aexpr.getListOfChildren().getFirst())).mstrVariableName;
										if (UnknownVarOperator.lookUpList(strVarName, listVarUnknownSingleExpr) != null)	{
											// not a valid unknown var, might be predefined var.
											bNoValidUnknownInFirst = false;
										}
									}
									if (bNoValidUnknownInFirst && aexpr.getListOfChildren().getLast() instanceof AEVar)	{
										String strVarName = ((AEVar)(aexpr.getListOfChildren().getLast())).mstrVariableName;
										if (UnknownVarOperator.lookUpList(strVarName, listVarUnknownSingleExpr) != null)	{
											// not a valid unknown var, might be predefined var.
											bNoValidUnknownInLast = false;
										}
									}
									if (bNoValidUnknownInFirst && bNoValidUnknownInLast)	{
										strarrayAnswer[0] = "Error";
										strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>";
										strOutput += strarrayAnswer[1];
										if (bStopWhenInValidAExpr) {
											bCanPlot = false;	// no variable in either side of the equation, cannot be plotted. Print error message here so that user can see.
										}
									} else	{
										if (bNoValidUnknownInFirst)	{
											listaeInputExprs.add(aexpr.getListOfChildren().getFirst());
											listaeInputEqualVars.add((AEVar)aexpr.getListOfChildren().getLast());
										} else	{
											listaeInputExprs.add(aexpr.getListOfChildren().getLast());
											listaeInputEqualVars.add((AEVar)aexpr.getListOfChildren().getFirst());
										}
										liststrOriginalExprs.add(strarrayExprs[idx]);
										listaeOriginalExprs.add(aexpr);
										int[] narrayMap = new int[1];
										narrayMap[0] = listaeInputExprs.size() - 1;
										listOriginal2Solved.add(narrayMap);
										listSolved2Original.add(listaeOriginalExprs.size() - 1);
							        	listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
									}
								} else if (listVarUnknownSingleExpr.size() == 2) {
									// ok, we have to solve the equation.
									// select which variable to solve.
									LinkedList<LinkedList<Variable>> lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
									LinkedList<Variable> listVars = new LinkedList<Variable>();
									listVars.addAll(listVarUnknownSingleExpr);
									lVarNameSpaces.add(listVars);
									int nMinAppearanceIdx = 0, nMaxAppearanceIdx = 1;
									if (aexpr.getVarAppearanceCnt(listVars.get(nMinAppearanceIdx).getName())
											> aexpr.getVarAppearanceCnt(listVars.get(nMaxAppearanceIdx).getName())) {
										nMinAppearanceIdx = 1;
										nMaxAppearanceIdx = 0;
									}
									String strEqualVarName = listVars.get(nMinAppearanceIdx).getName();
									AEVar aeEqualVarName = new AEVar(strEqualVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
									SolveAnalyzer solveAnalyzer = new SolveAnalyzer();  // need to initialize SolveAnalyzer even if solveVarInSingleExpr is static coz mspm needs initialization.
									listaeSolvedResults = new LinkedList<AbstractExpr>();
									try {
										listaeSolvedResults = SolveAnalyzer.solveVarInSingleExpr(aexpr, strEqualVarName, listVarUnknownSingleExpr, lVarNameSpaces, true, 0);
									} catch (Exception e) {
										
									}
									if (listaeSolvedResults.size() == 0) {
										// if cannot use one var to solve, then try to use another var.
										for (int idxUnknownVar = 0; idxUnknownVar < listVarUnknownSingleExpr.size(); idxUnknownVar ++) {
											listVarUnknownSingleExpr.get(idxUnknownVar).setValueAssigned(false); // solveVarInSingleExpr may assign value to unknown var
										}
										strEqualVarName = listVars.get(nMaxAppearanceIdx).getName();
										aeEqualVarName = new AEVar(strEqualVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
										listaeSolvedResults = SolveAnalyzer.solveVarInSingleExpr(aexpr, strEqualVarName, listVarUnknownSingleExpr, lVarNameSpaces, true, 0);
									}
									int[] narrayMap = new int[Math.min(4, listaeSolvedResults.size())];
									for (int idxResult = 0; idxResult < narrayMap.length; idxResult ++) {
										// show at most four result.
										listaeInputExprs.add(listaeSolvedResults.get(idxResult));
										listaeInputEqualVars.add(aeEqualVarName);
										narrayMap[idxResult] = listaeInputEqualVars.size() - 1;
										listSolved2Original.add(listaeOriginalExprs.size());
									}
									liststrOriginalExprs.add(strarrayExprs[idx]);
									listaeOriginalExprs.add(aexpr);
									listOriginal2Solved.add(narrayMap);
									listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
								} else {    // listVarUnknownSingleExpr.size() == 3
									// ok, we have to solve the equation.
									// for 3D charts, we solve all the variables.
									LinkedList<LinkedList<Variable>> lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
									LinkedList<Variable> listVars = new LinkedList<Variable>();
									listVars.addAll(listVarUnknownSingleExpr);
									lVarNameSpaces.add(listVars);
									LinkedList<AbstractExpr> listaeAllSolvedResults = new LinkedList<AbstractExpr>();
									LinkedList<AEVar> listaeAllEqualVarNames = new LinkedList<AEVar>();
									for (int idxVar = 0; idxVar < listVars.size(); idxVar ++) {
										if (idxVar > 0) {
											for (int idxUnknownVar = 0; idxUnknownVar < listVarUnknownSingleExpr.size(); idxUnknownVar ++) {
												listVarUnknownSingleExpr.get(idxUnknownVar).setValueAssigned(false); // solveVarInSingleExpr may assign value to unknown var
											}
										}
										String strEqualVarName = listVars.get(idxVar).getName();
										AEVar aeEqualVarName = new AEVar(strEqualVarName, ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE);
										SolveAnalyzer solveAnalyzer = new SolveAnalyzer();  // need to initialize SolveAnalyzer even if solveVarInSingleExpr is static coz mspm needs initialization.
										listaeSolvedResults = new LinkedList<AbstractExpr>();
										try {
											listaeSolvedResults = SolveAnalyzer.solveVarInSingleExpr(aexpr, strEqualVarName, listVarUnknownSingleExpr, lVarNameSpaces, true, 0);
										} catch (Exception e) {

										}
										if (listaeSolvedResults.size() == 1) {
											// ok, only one solution. Good, use it as we need not to consider other solutions based on other vars
											listaeAllSolvedResults.clear();
											listaeAllEqualVarNames.clear();
											listaeAllSolvedResults.add(listaeSolvedResults.getFirst());
											listaeAllEqualVarNames.add(aeEqualVarName);
											break;
										}
										for (int idxResult = 0; idxResult < Math.min(2, listaeSolvedResults.size()); idxResult ++) {
											listaeAllSolvedResults.add(listaeSolvedResults.get(idxResult));
											listaeAllEqualVarNames.add(aeEqualVarName);
										}
									}
									int[] narrayMap = new int[listaeAllSolvedResults.size()];
									for (int idxResult = 0; idxResult < narrayMap.length; idxResult ++) {
										// show at most 6 result.
										listaeInputExprs.add(listaeAllSolvedResults.get(idxResult));
										listaeInputEqualVars.add(listaeAllEqualVarNames.get(idxResult));
										narrayMap[idxResult] = listaeInputEqualVars.size() - 1;
										listSolved2Original.add(listaeOriginalExprs.size());
									}
									liststrOriginalExprs.add(strarrayExprs[idx]);
									listaeOriginalExprs.add(aexpr);
									listOriginal2Solved.add(narrayMap);
									listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
								}
								if (listaeSolvedResults != null && listaeSolvedResults.size() == 0) {
									// listaeSolvedResults != null means we need to solve implicit function
									// size() == 0 means we cannot solve.
									strarrayAnswer[0] = "Error";
									strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>";
									strOutput += strarrayAnswer[1];
									if (bStopWhenInValidAExpr) {
										bCanPlot = false;	// < > <= >= cannot be plotted. Print error message here so that user can see.
									}
								}
							} else	{
								strarrayAnswer[0] = "Error";
								strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>";
								strOutput += strarrayAnswer[1];
								if (bStopWhenInValidAExpr) {
									bCanPlot = false;	// < > <= >= cannot be plotted. Print error message here so that user can see.
								}
							}
						} else	{
				        	listaeInputExprs.add(aexpr);
				        	listaeInputEqualVars.add(new AEVar());	// invalid AEVar
				        	listlVarUnknownExprsAll.add(listVarUnknownSingleExpr);
				        	liststrOriginalExprs.add(strarrayExprs[idx]);
				        	listaeOriginalExprs.add(aexpr);
							int[] narrayMap = new int[1];
							narrayMap[0] = listaeInputExprs.size() - 1;
							listOriginal2Solved.add(narrayMap);
							listSolved2Original.add(liststrOriginalExprs.size() - 1);
				        }
					} catch (Exception e) {
						strarrayAnswer[0] = "Error";
						strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>";
						strOutput += strarrayAnswer[1];
						if (bStopWhenInValidAExpr) {
				        	bCanPlot = false;	// invalid expression, cannot be plotted.
						}
					}
				} catch (Exception e)	{
				    strOutput += "<p><a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(strarrayExprs[idx])
							+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
							+ strarrayExprs[idx] + "</a></p>\n";
					strarrayAnswer[0] = "Error";
					strarrayAnswer[1] = "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>";
					strOutput += strarrayAnswer[1];
					if (bStopWhenInValidAExpr) {
			        	bCanPlot = false;	// invalid expression, cannot be plotted.
					}
				}
			}
		}

		if (bCanPlot == false)	{
			return strOutput;
		} else if (listaeInputExprs.size() == 0)	{
			// no expression to plot, report no error message.
			return strOutput;
		} else if (listVarUnknown.size() > 3)	{
			strOutput += "<p> <big>&rarr;</big> </p><p>" + context.getString(R.string.cannot_plot_more_than_3d_chart) + "</p>";
			return strOutput;
		} else	{
			strOutput += "<p> <big>&rarr;</big> </p>";
			boolean bPlot3DChart = false;
			if (listVarUnknown.size() == 3) {
				bPlot3DChart = true;
			} else if (listVarUnknown.size() == 2)	{
				boolean bAllOmitted2Vars = true;
				for (int idx = 0; idx < listaeOriginalExprs.size(); idx ++)	{
					if ((listaeOriginalExprs.get(idx) instanceof AECompare)
						|| (listaeOriginalExprs.get(idx) instanceof AEAssign)
							|| listlVarUnknownExprsAll.get(idx).size() != 2)	{
						bAllOmitted2Vars = false;
					}
				}
				if (bAllOmitted2Vars)	{	// all the expressions are like x*y, y+z, etc.
					bPlot3DChart = true;
					listVarUnknown.add(new UnknownVariable(listVarUnknown.get(0).getName() + "_" + listVarUnknown.get(1).getName()));
				} //else some of the exprs are like sin(x) == y, bPlot3DChart is false
			} else if (listVarUnknown.size() == 1)	{
				listVarUnknown.add(new UnknownVariable("f_" + listVarUnknown.get(0).getName()));
			}
			if (bPlot3DChart)	{
				// three D chart
				if (listaeInputExprs.size() > 8)	{
					strOutput += "<p>" + context.getString(R.string.cannot_plot_too_many_curves) + "</p>";
					return strOutput;
				}
				
				String strPlotCmdLine = "plot_3d_expressions(\""
						+ ActivityChartDemon.getChartFileName(
								ChartOperator.addEscapes(context.getString(R.string.chart_name_default)).trim(),
								lPlotTriggerTime)
						+ "\",\""
						+ ActivityPlotXYZGraph.addEscapes(context.getString(R.string.chart_title_default)).trim() + "\",\""
						+ ActivityPlotXYZGraph.addEscapes(listVarUnknown.get(0).getName()).trim() + "\"," + ActivitySettings.msfSmCPlotVarFrom + "," + ActivitySettings.msfSmCPlotVarTo + ",\""
						+ ActivityPlotXYZGraph.addEscapes(listVarUnknown.get(1).getName()).trim() + "\"," + ActivitySettings.msfSmCPlotVarFrom + "," + ActivitySettings.msfSmCPlotVarTo + ",\""
						+ ActivityPlotXYZGraph.addEscapes(listVarUnknown.get(2).getName()).trim() + "\"," + ActivitySettings.msfSmCPlotVarFrom + "," + ActivitySettings.msfSmCPlotVarTo;
				for (int idx = 0; idx < listaeInputExprs.size(); idx ++)	{
					String strXExpr = "", strYExpr = "", strZExpr = "";
					AbstractExpr aexpr = listaeInputExprs.get(idx);
                    int idxOriginal = listSolved2Original.get(idx);
                    AbstractExpr aexprOriginal = listaeOriginalExprs.get(idxOriginal);
					if (!(aexprOriginal instanceof AECompare) && !(aexprOriginal instanceof AEAssign))	{
						if (listlVarUnknownExprsAll.get(idxOriginal).size() != listVarUnknown.size() - 1)	{
							try {
								strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
										+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
										// + AbstractExprConverter.convtAExpr2JQMath(listaeOriginalExprs.get(idx)) + "$</a></p>"; // binary or hex values cannot be shown properly by convtAExpr2JQMath
										+ AbstractExprConverter.convtPlainStr2JQMathNoException(liststrOriginalExprs.get(idxOriginal)) + "$</a></p>";
							} catch (Exception e) {
								strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
										+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
										+ TextUtils.htmlEncode(liststrOriginalExprs.get(idxOriginal)) + "</a></p>";
							}
							return strOutput;
						} else	{
							for (int idx2 = 0; idx2 < listVarUnknown.size(); idx2 ++)	{
								if (UnknownVarOperator.lookUpList(listVarUnknown.get(idx2).getName(), listlVarUnknownExprsAll.get(idxOriginal)) == null)	{
									try {
										listaeInputEqualVars.set(idx, new AEVar(listVarUnknown.get(idx2).getName(), ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE));
									} catch (JSmartMathErrException e) {
										try	{
											strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
													+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
													// + AbstractExprConverter.convtAExpr2JQMath(listaeOriginalExprs.get(idx)) + "$</a></p>"; // binary or hex values cannot be shown properly by convtAExpr2JQMath
													+ AbstractExprConverter.convtPlainStr2JQMathNoException(liststrOriginalExprs.get(idxOriginal)) + "$</a></p>";
										} catch(Exception e1)	{
											strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
													+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
													+ TextUtils.htmlEncode(liststrOriginalExprs.get(idxOriginal)) + "</a></p>";
										}
										return strOutput;
									}
								}
							}
						}
					}
					int nFunctionVar = 2;
					for (int idx1 = 0; idx1 < listVarUnknown.size(); idx1 ++)	{
						if (listVarUnknown.get(idx1).getName().compareToIgnoreCase(listaeInputEqualVars.get(idx).mstrVariableName) == 0)	{
							// in x + y case this is z
							nFunctionVar = idx1;
							try {
								if (idx1 == 0)	{
									strXExpr = aexpr.output();
								} else if (idx1 == 1)	{
									strYExpr = aexpr.output();
								} else	{
									strZExpr = aexpr.output();
								}
							} catch (Exception e) {
								try	{
									strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
											+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
											// + AbstractExprConverter.convtAExpr2JQMath(listaeOriginalExprs.get(idx)) + "$</a></p>"; // binary or hex values cannot be shown properly by convtAExpr2JQMath
											+ AbstractExprConverter.convtPlainStr2JQMathNoException(liststrOriginalExprs.get(idxOriginal)) + "$</a></p>";
								} catch(Exception e1)	{
									strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
											+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
											+ TextUtils.htmlEncode(liststrOriginalExprs.get(idxOriginal)) + "</a></p>";
								}
								return strOutput;
							}
						} else	{
							String strExpr = listVarUnknown.get(idx1).getName();
							if (idx1 == 0)	{
								strXExpr = strExpr;
							} else if (idx1 == 1)	{
								strYExpr = strExpr;
							} else	{
								strZExpr = strExpr;
							}
						}
					}
                    int nNumOfSolvedExprsFromOriginal = listOriginal2Solved.get(idxOriginal).length;
					strPlotCmdLine += ",\"" + ActivityPlotXYZGraph.addEscapes(liststrOriginalExprs.get(idxOriginal))
		                    +  "\",false,\"" + ((nNumOfSolvedExprsFromOriginal <= 1)?
												((idxOriginal == 0)?"cyan":((idxOriginal == 1)?"magenta":((idxOriginal == 2)?"white":"gray")))
												:((idxOriginal == 0)?"red":((idxOriginal == 1)?"green":((idxOriginal == 2)?"blue":"yellow"))))
		                    +  "\",\"" + ((nNumOfSolvedExprsFromOriginal <= 1)?
												((idxOriginal == 0)?"cyan":((idxOriginal == 1)?"magenta":((idxOriginal == 2)?"white":"gray")))
												:((idxOriginal == 0)?"red":((idxOriginal == 1)?"green":((idxOriginal == 2)?"blue":"yellow"))))
		                    +  "\",\"" + ((idxOriginal == 0)?"red":((idxOriginal == 1)?"green":((idxOriginal == 2)?"blue":"yellow")))
		                    +  "\",\"" + ((idxOriginal == 0)?"red":((idxOriginal == 1)?"green":((idxOriginal == 2)?"blue":"yellow")))
							+  "\"," + nFunctionVar
				            +  ",\"" + ActivityPlotXYZGraph.addEscapes(strXExpr).trim() + "\",0"
				            +  ",\"" + ActivityPlotXYZGraph.addEscapes(strYExpr).trim() + "\",0"
				            +  ",\"" + ActivityPlotXYZGraph.addEscapes(strZExpr).trim() + "\",0";
                    liststrOriginalExprs.set(idxOriginal, "");  //strOriginalExpr no longer used
					
				}
				strPlotCmdLine += ")";
	
				/* evaluate the expression */
				ExprEvaluator exprEvaluator = new ExprEvaluator();
				// clear variable namespaces
				exprEvaluator.m_lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
				
				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				// should be interrupted, should save file and plot graphs. No input or log output.
				FuncEvaluator.msfunctionInterrupter = new SmartCalcFunctionInterrupter();
				ScriptAnalyzer.msscriptInterrupter = new SmartCalcScriptInterrupter();
				AbstractExpr.msaexprInterrupter = new SmartCalcAbstractExprInterrupter();
				FuncEvaluator.msstreamConsoleInput = null;
				FuncEvaluator.msstreamLogOutput = null;
				FuncEvaluator.msfileOperator = new MFPFileManagerActivity.MFPFileOperator();
				FuncEvaluator.msgraphPlotter = null;
				FuncEvaluator.msgraphPlotter3D = new PlotGraphPlotter(contextPlot, lPlotTriggerTime);
                if (FuncEvaluator.mspm == null) {
                    FuncEvaluator.mspm = new PatternManager();	// even if plot we still need mspm because we may calculate integrate.
                    try {
						FuncEvaluator.mspm.loadPatterns(2);	// load pattern is a very time consuming work. So only do this if needed.
					} catch (Exception e) {
						 // load all integration patterns. Assume load patterns will not throw any exceptions.
					}
                 }
				
				try	{
					exprEvaluator.evaluateExpression(strPlotCmdLine, curpos);
					strOutput += "<p>" + context.getString(R.string.graph_is_plotted) + "</p>";
					strOutput += getSnapshotOutput(context, lPlotTriggerTime, -1, -1);
				} catch (Exception e)	{
					String strErrMsg = MFPAdapter.outputException(e);
					if (((PlotGraphPlotter)(FuncEvaluator.msgraphPlotter3D)).mbOK)	{
						// graph is plotted without problem, but we cannot save the file.
						// cannot use Toast here seems because we are in main thread
						strOutput += "<p>" + context.getString(R.string.graph_is_plotted_but_graph_file_cannot_be_saved) + "</p>";
					} else	{
						strOutput += "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>" + "<pre>" + strErrMsg + "</pre>";
					}
				}
				return strOutput;
			} else	{
				// two D chart, we still have at most 4 curves coz color resource is limited.
				if (listaeInputExprs.size() > 4)	{
					strOutput += "<p>" + context.getString(R.string.cannot_plot_too_many_curves) + "</p>";
					return strOutput;
				}
				String strPlotFunction = "plot_2d_expressions";
				float fFirstVarPlotFrom = ActivitySettings.msfSmCPlotVarFrom;
				float fFirstVarPlotTo = ActivitySettings.msfSmCPlotVarTo;
				float fSecondVarPlotFrom = ActivitySettings.msfSmCPlotVarFrom;
				float fSecondVarPlotTo = ActivitySettings.msfSmCPlotVarTo;
				if (listVarUnknown.get(0).getName().trim().equals("\u03B1")	// alpha
						|| listVarUnknown.get(0).getName().trim().equals("\u03B2")	// beta
						|| listVarUnknown.get(0).getName().trim().equals("\u03B3")	// gamma
						|| listVarUnknown.get(0).getName().trim().equals("\u03B8"))		{// theta
					// because the first variable is angle, needs to swap first and second variables.
					strPlotFunction = "plot_polar_expressions";
					UnknownVariable varTemp = listVarUnknown.get(1);
					listVarUnknown.set(1, listVarUnknown.get(0));
					listVarUnknown.set(0, varTemp);
					fFirstVarPlotFrom = 0;
					fFirstVarPlotTo = Math.max(Math.abs(ActivitySettings.msfSmCPlotVarFrom),
							Math.abs(ActivitySettings.msfSmCPlotVarTo));
					fSecondVarPlotFrom = (float) 0;
					fSecondVarPlotTo = (float) (2 * Math.PI);
				} else if (listVarUnknown.get(1).getName().trim().equals("\u03B1")	// alpha
						|| listVarUnknown.get(1).getName().trim().equals("\u03B2")	// beta
						|| listVarUnknown.get(1).getName().trim().equals("\u03B3")	// gamma
						|| listVarUnknown.get(1).getName().trim().equals("\u03B8"))		{// theta
					strPlotFunction = "plot_polar_expressions";
					fFirstVarPlotFrom = 0;
					fFirstVarPlotTo = Math.max(Math.abs(ActivitySettings.msfSmCPlotVarFrom),
							Math.abs(ActivitySettings.msfSmCPlotVarTo));
					fSecondVarPlotFrom = (float) 0;
					fSecondVarPlotTo = (float) (2 * Math.PI);
				}
				String strPlotCmdLine = strPlotFunction + "(\""
						+ ActivityChartDemon.getChartFileName(
								ChartOperator.addEscapes(context.getString(R.string.chart_name_default)).trim(),
								lPlotTriggerTime)
						+ "\",\""
						+ ActivityPlotXYGraph.addEscapes(context.getString(R.string.chart_title_default)).trim() + "\",\""
						+ ActivityPlotXYGraph.addEscapes(listVarUnknown.get(0).getName()).trim() + "\"," + fFirstVarPlotFrom + "," + fFirstVarPlotTo + ",\""
						+ ActivityPlotXYGraph.addEscapes(listVarUnknown.get(1).getName()).trim() + "\"," + fSecondVarPlotFrom + "," + fSecondVarPlotTo + ",\"black\",\"true\"";
				for (int idx = 0; idx < listaeInputExprs.size(); idx ++)	{
					String strXExpr = "", strYExpr = "";
					AbstractExpr aexpr = listaeInputExprs.get(idx);
                    int idxOriginal = listSolved2Original.get(idx);
                    AbstractExpr aexprOriginal = listaeOriginalExprs.get(idxOriginal);
					if (!(aexprOriginal instanceof AECompare) && !(aexprOriginal instanceof AEAssign))	{
						if (listlVarUnknownExprsAll.get(idxOriginal).size() != listVarUnknown.size() - 1)	{
							try {
								strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
										+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
										// + AbstractExprConverter.convtAExpr2JQMath(listaeOriginalExprs.get(idxOriginal)) + "$</a></p>"; // binary or hex values cannot be shown properly by convtAExpr2JQMath
										+ AbstractExprConverter.convtPlainStr2JQMathNoException(liststrOriginalExprs.get(idxOriginal)) + "$</a></p>";
							} catch(Exception e)	{
								strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
										+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
										+ TextUtils.htmlEncode(liststrOriginalExprs.get(idxOriginal)) + "</a></p>";
							}
							return strOutput;
						} else	{
							for (int idx2 = 0; idx2 < listVarUnknown.size(); idx2 ++)	{
								if (UnknownVarOperator.lookUpList(listVarUnknown.get(idx2).getName(), listlVarUnknownExprsAll.get(idxOriginal)) == null)	{
									try {
										listaeInputEqualVars.set(idx, new AEVar(listVarUnknown.get(idx2).getName(), ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE));
									} catch (JSmartMathErrException e) {
										try	{
											strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
													+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
													// + AbstractExprConverter.convtAExpr2JQMath(listaeOriginalExprs.get(idxOriginal)) + "$</a></p>"; // binary or hex values cannot be shown properly by convtAExpr2JQMath
													+ AbstractExprConverter.convtPlainStr2JQMathNoException(liststrOriginalExprs.get(idxOriginal)) + "$</a></p>";
										} catch(Exception e1)	{
											strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
													+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
													+ TextUtils.htmlEncode(liststrOriginalExprs.get(idxOriginal)) + "</a></p>";
										}
										return strOutput;
									}
								}
							}
						}
					}
					int nFunctionVar = 1;
					for (int idx1 = 0; idx1 < listVarUnknown.size(); idx1 ++)	{
						if (listVarUnknown.get(idx1).getName().compareToIgnoreCase(listaeInputEqualVars.get(idx).mstrVariableName) == 0)	{
							// in sin(x) case this is y
							nFunctionVar = idx1;
							try {
								if (idx1 == 0)	{
									strXExpr = aexpr.output();
								} else	{
									strYExpr = aexpr.output();
								}
							} catch (Exception e) {
								try	{
									strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
											+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">$"
											// + AbstractExprConverter.convtAExpr2JQMath(listaeOriginalExprs.get(idxOriginal)) + "$</a></p>"; // binary or hex values cannot be shown properly by convtAExpr2JQMath
											+ AbstractExprConverter.convtPlainStr2JQMathNoException(liststrOriginalExprs.get(idxOriginal)) + "$</a></p>";
								} catch(Exception e1)	{
									strOutput += "<p>" + context.getString(R.string.cannot_plot) + " <a href=\"" + AbstractExprConverter.convtPlainStr2QuotedUrl(liststrOriginalExprs.get(idxOriginal))
											+ "\" style=\"text-decoration: none;" + strOriginalExprColor + "\">"
											+ TextUtils.htmlEncode(liststrOriginalExprs.get(idxOriginal)) + "</a></p>";
								}
								return strOutput;
							}
						} else	{
							String strExpr = listVarUnknown.get(idx1).getName();
							if (idx1 == 0)	{
								strXExpr = strExpr;
							} else	{
								strYExpr = strExpr;
							}
						}
					}
					// TODO
					strPlotCmdLine += ",\"" + ActivityPlotXYGraph.addEscapes(liststrOriginalExprs.get(idxOriginal))
							+ "\",\"" + ((idxOriginal == 0)?"red":((idxOriginal == 1)?"green":((idxOriginal == 2)?"blue":"yellow")))
		                    + "\",\"dot\",1,\"" + ((idxOriginal == 0)?"red":((idxOriginal == 1)?"green":((idxOriginal == 2)?"blue":"yellow")))
							+ "\",\"solid\",1," + nFunctionVar
				            + ",\"" + ActivityPlotXYGraph.addEscapes(strXExpr).trim() + "\",\""
							+ ActivityPlotXYGraph.addEscapes(strYExpr).trim() + "\",0";
                    liststrOriginalExprs.set(idxOriginal, "");  //strOriginalExpr no longer used
				}
				strPlotCmdLine += ")";
	
				/* evaluate the expression */
				ExprEvaluator exprEvaluator = new ExprEvaluator();
				// clear variable namespaces
				exprEvaluator.m_lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
				
				/* evaluate the expression */
				CurPos curpos = new CurPos();
				curpos.m_nPos = 0;
				// should be interrupted, should save file and plot graphs. No log output.
				FuncEvaluator.msfunctionInterrupter = new SmartCalcFunctionInterrupter();
				ScriptAnalyzer.msscriptInterrupter = new SmartCalcScriptInterrupter();
				AbstractExpr.msaexprInterrupter = new SmartCalcAbstractExprInterrupter();
				FuncEvaluator.msstreamConsoleInput = null;
				FuncEvaluator.msstreamLogOutput = null;
				FuncEvaluator.msfileOperator = new MFPFileManagerActivity.MFPFileOperator();
				FuncEvaluator.msgraphPlotter = new PlotGraphPlotter(contextPlot, lPlotTriggerTime);
				FuncEvaluator.msgraphPlotter3D = null;
                if (FuncEvaluator.mspm == null) {
                    FuncEvaluator.mspm = new PatternManager();	// even if plot we still need mspm because we may calculate integrate.
                    try {
						FuncEvaluator.mspm.loadPatterns(2);	// load pattern is a very time consuming work. So only do this if needed.
					} catch (Exception e) {
						 // load all integration patterns. Assume load patterns will not throw any exceptions.
					}
                 }
				
				try	{
					exprEvaluator.evaluateExpression(strPlotCmdLine, curpos);
					strOutput += "<p>" + context.getString(R.string.graph_is_plotted) + "</p>";
					strOutput += getSnapshotOutput(context, lPlotTriggerTime, -1, -1);
				} catch (Exception e)	{
					String strErrMsg = MFPAdapter.outputException(e);
					if (((PlotGraphPlotter)(FuncEvaluator.msgraphPlotter)).mbOK)	{
						// graph is plotted without problem, but we cannot save the file.
						// cannot use Toast here seems because we are in main thread
						strOutput += "<p>" + context.getString(R.string.graph_is_plotted_but_graph_file_cannot_be_saved) + "</p>";
					} else	{
						strOutput += "<p>" + context.getString(R.string.invalid_expr_to_plot) + "</p>" + "<pre>" + strErrMsg + "</pre>";
					}
				}
				return strOutput;
			}
		}
	}
	
	public static String getSnapshotOutput(Context context, long lPlottingTime, int nChartWidth, int nChartHeight)	{
		String strSnapshotFileNameNoExt = ActivityChartDemon.getChartFileName(
				ChartOperator.addEscapes(context.getString(R.string.chart_name_default)).trim(),
				lPlottingTime);
		String strSnapshotFileName = strSnapshotFileNameNoExt + ".jpg";
		String strChartFileName = strSnapshotFileNameNoExt + MFPFileManagerActivity.STRING_CHART_EXTENSION;
		String strRootPath = ActivitySmartCalc.SELECTED_STORAGE_PATH_UNIMATCH // here do not use AndroidStorageOptions.getSelectedStoragePath() so that can be dynamically determined by history record.
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ MFPFileManagerActivity.STRING_APP_FOLDER;
		String strSnapshotFilePath = strRootPath
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ MFPFileManagerActivity.STRING_CHART_SNAPSHOT_FOLDER
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ strSnapshotFileName;
		String strChartFilePath = strRootPath
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ MFPFileManagerActivity.STRING_CHART_FOLDER
				+ MFPFileManagerActivity.STRING_PATH_DIV
				+ strChartFileName;
	    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    float fRelativeDPI = metrics.densityDpi / 160f;
	    int nShortSide = (int) Math.min(metrics.widthPixels/fRelativeDPI, metrics.heightPixels/fRelativeDPI);
	    String strOutput = "";
	    if (nChartWidth > 0 && nChartHeight > 0)	{	// given chart width and height.
	    	strOutput = "<p><a href=\"" + ActivitySmartCalc.ACHART_URL_HEADER + strChartFilePath + "\"><img src=\"" + strSnapshotFilePath + "\" alt=\"" + strChartFileName
					+ "\" width=\"" + nChartWidth/(2 * fRelativeDPI) + "\" height=\"" + nChartHeight/(2 * fRelativeDPI) + "\"></a></p>";
	    } else	{
		    strOutput = "<p><a href=\"" + ActivitySmartCalc.ACHART_URL_HEADER + strChartFilePath + "\"><img src=\"" + strSnapshotFilePath + "\" alt=\"" + strChartFileName
					+ "\" onload=\"if(this.width<this.height){this.width=" + nShortSide/2 + ";}else{this.height=" + nShortSide/2 + ";}\"></a></p>";
	    }
	    return strOutput;
	}
	
}
