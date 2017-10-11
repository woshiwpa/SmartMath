package com.cyzapps.adapter;

import com.cyzapps.Jfcalc.BaseData.BoundOperator;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import java.util.*;

import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.BaseData.CurPos;

public class JQElemAnalyzer extends ElemAnalyzer	{

	public static String convt2JQEscapedStr(String strInput)	{
		String strOutput = "";
		if (strInput != null)	{
			for (int idx = 0; idx < strInput.length(); idx ++)	{
				if (strInput.charAt(idx) == '\\')	{
					strOutput += "\\\\";
				} else if (strInput.charAt(idx) == '&')	{
					strOutput += "\\&";
				} else if (strInput.charAt(idx) == '^')	{
					strOutput += "\\^";
				} else if (strInput.charAt(idx) == '{')	{
					strOutput += "\\{";
				} else if (strInput.charAt(idx) == '}')	{
					strOutput += "\\}";
				} else if (strInput.charAt(idx) == '_')	{
					strOutput += "\\_";
				} else	{
					strOutput += strInput.charAt(idx);
				}
			}
		}
		return strOutput;
	}
	
    public static BoundOperator getBoundOperator(String strExpression, CurPos curpos) {
        try {
            return ElemAnalyzer.getBoundOperator(strExpression, curpos);
        } catch (JFCALCExpErrException e) {
            return new BoundOperator();
        }
    }
    
    public static CalculateOperator getCalcOperator(String strExpression, CurPos curpos, int nLastPushedCellType) {
        try {
            return ElemAnalyzer.getCalcOperator(strExpression, curpos, nLastPushedCellType);
        } catch (JFCALCExpErrException e) {
            return new CalculateOperator();
        }

    }
    
	public static String getBndOptrJQStr(String strExpression, CurPos curpos)
	{
		String str = "";
		if (strExpression.length() > curpos.m_nPos)	{
			str = strExpression.substring(curpos.m_nPos, curpos.m_nPos + 1);	// even if it is not "(" or ")", we still set str to be the char.
		}
		curpos.m_nPos++;
		str = convt2JQEscapedStr(str);	// some characters which are not bound operator like & and ^ have to be escaped.
		return str;
	}
	
	public static String getCalcOptrJQStr(String strExpression, CurPos curpos)
	{
		String str = "";
		
		/* The position of current character */
		int nCurCharPos = curpos.m_nPos;
		/* The starting point of the operator */
		if (nCurCharPos < strExpression.length())	{
			String strStart = strExpression.substring(nCurCharPos);
		
			switch(strStart.charAt(0))
			{
			case '*': /*If it is '*', two possibilities. */
				/* If it is '**'. */
				if (strStart.length() > 1 && strStart.charAt(1) == '*') {
					nCurCharPos++;
					str += "^";
				} else {	/* If it is '*'. */
					str += "\u00D7";
				}
				break;
			case '!':  /* If it is '!'. */
				if (strStart.length() > 1 && strStart.charAt(1) == '=') {
					/* if it is power followed by '=='*/
					if (strStart.length() > 2 && strStart.charAt(2) == '=')	{
						str += "!";
					} else { /* if it is NEQ */
						nCurCharPos++;
						str += "\u2260";
					}
				} else { /* If it is FALSE or factorial. */
					str += "!";
				}
				break;
			case '=': /*If it is '='. */
				if (strStart.length() == 1 || (strStart.length() > 1 && strStart.charAt(1) != '=')) {
					str += "=";
				} else if (strStart.length() > 1 && strStart.charAt(1) == '=')  { /* If it is EQ. */
					nCurCharPos++;
					str += "=";
				}
				break;
			case '>': /*If it is '>', two possibilities. */
				if (strStart.length() > 1 && strStart.charAt(1) == '=') { /* If it is '>='. */
					nCurCharPos++;
					str += "\u2265";
				} else {/* If it is '>'. */
					str += ">";
				}
				break;
			case '<': /*If it is '<', two possibilities. */
				if (strStart.length() > 1 && strStart.charAt(1) == '=') { /* If it is '<='. */
					nCurCharPos++;
					str += "\u2264";
				} else { /* If it is '<'. */
					str += "<";
				}
				break;
			case '|': /*If it is '|', convert to unicode 2223 because '|' is too tall. */
				str += "\u2223";
				break;
			case '\'': /*If it is '\'', convert to ^T. */
				str += "^T";
				break;
			default:
				str += strStart.charAt(0);
				str = convt2JQEscapedStr(str);	// some characters which are not bound operator like & and ^ have to be escaped.
				break;
			}
			
			curpos.m_nPos = nCurCharPos + 1;
		}
		return str;
	}
	
	public static String getNumberJQStr(String strExpression, CurPos curpos)
	{
		String str = "";
        // first of all, find positional notation
        int nPN = 10;   // can be 2, 8, 10 or 16, other value is treated as 10.
        if (strExpression.charAt(curpos.m_nPos) == '0' && strExpression.length() > (curpos.m_nPos + 1))   {
            if (strExpression.charAt(curpos.m_nPos + 1) == 'b' || strExpression.charAt(curpos.m_nPos + 1) == 'B')  {
                nPN = 2;
                str += strExpression.substring(curpos.m_nPos, curpos.m_nPos + 2);
            } else if (strExpression.charAt(curpos.m_nPos + 1) == 'x' || strExpression.charAt(curpos.m_nPos + 1) == 'X')    {
                nPN = 16;
                str += strExpression.substring(curpos.m_nPos, curpos.m_nPos + 2);
            } else if (strExpression.charAt(curpos.m_nPos + 1) >= '0' && strExpression.charAt(curpos.m_nPos + 1) <= '7')    {
                nPN = 8;
                str += strExpression.substring(curpos.m_nPos, curpos.m_nPos + 1);
            }
        }
        
        if (nPN == 2 || nPN == 8 || nPN == 16)   {  // these does not support scientific notation like 0.11e11
            int nStartPosition = (nPN == 8)?(curpos.m_nPos + 1):(curpos.m_nPos + 2);
            curpos.m_nPos = nStartPosition;
            boolean boolIsComplexImage = false;
            while (strExpression.length() > curpos.m_nPos && boolIsComplexImage == false) {
                char cThis = strExpression.charAt(curpos.m_nPos);
                if (isDigitChar(strExpression, curpos.m_nPos, nPN) || cThis == '.') {	// don't worry about two decimal points.
                    str += cThis;
                } else if (cThis == 'i' || cThis == 'I')	{/* otherwise it is i or I */
                    boolIsComplexImage = true;
                } else  {
                    break;  // we may arrive at the end.
                }
                curpos.m_nPos++; 
            }

            str = "\\text\"" + str + "\"";  // to avoid italian characters.
            if (boolIsComplexImage)	{
                // this is a complex number
                if (str.equals("\\text\"1\"")) {
                    str = "i";
                } else {
                    str += "\u00D7" + "i";
                }
                str = "{" + str + "}";  // this line is to handle like 1/2i, should be 1/{2*i} not 1/2*i
            }
        } else  {   // nPN == 10
            int nStartPosition = curpos.m_nPos;

            boolean boolIsComplexImage = false;
            while (((strExpression.length() > curpos.m_nPos)
                    && getDecimalCharType(strExpression, curpos.m_nPos) != NUMBERCHARTYPES.NUMBERCHAR_UNRECOGNIZED)
                    && boolIsComplexImage == false)
            {
                if (strExpression.charAt(curpos.m_nPos) != 'i' && strExpression.charAt(curpos.m_nPos) != 'I')  {
                    str += strExpression.charAt(curpos.m_nPos);
                } else {	/* otherwise it is i or I */
                    if (nStartPosition == curpos.m_nPos)	/* this is the first character */
                    {
                        str = "1";
                    }
                    boolIsComplexImage = true;
                }
                curpos.m_nPos++; 
            }
            str = "\\text\"" + str + "\"";  // to avoid italian characters.
            if (boolIsComplexImage)	{
            	// this is a complex number
                if (str.equals("\\text\"1\"")) {
                    str = "i";
                } else {
                    str += "\u00D7" + "i";
                }
                str = "{" + str + "}";  // this line is to handle like 1/2i, should be 1/{2*i} not 1/2*i
            }
        }
		return str;
	}
	
	public static class DataRefJQStr {
		public int mnDimension = 0;
		public String mstrJQElem = "";
		public LinkedList<DataRefJQStr> mlistChildElems = new LinkedList<DataRefJQStr>();
		public String getJQStr()	{
			String str = "";
			int nMod2 = mnDimension % 2;
			if (mnDimension == 0)	{
				str = mstrJQElem;
			} else if (nMod2 == 1)	{
				str += "(\\table ";
				for (int idx = 0; idx < mlistChildElems.size(); idx ++)	{
					if (idx > 0)	{
						str += ", ";
					}
					str += mlistChildElems.get(idx).getJQStr();
				}
				str += ")";
			} else	{	// nMode2 == 0 and nMode2 > 0
				str += "(\\table ";
				for (int idx = 0; idx < mlistChildElems.size(); idx ++)	{
					if (idx > 0)	{
						str += "; ";
					}
					if (mlistChildElems.get(idx).mnDimension > 0)	{
						for (int idx1 = 0; idx1 < mlistChildElems.get(idx).mlistChildElems.size(); idx1 ++)	{
							if (idx1 > 0)	{
								str += ", ";
							}
							str += mlistChildElems.get(idx).mlistChildElems.get(idx1).getJQStr();
						}
					} else	{
						str += mlistChildElems.get(idx).getJQStr();
					}
				}
				str += ")";
			}
			return str;
		}
	}
	
	public static DataRefJQStr getDataRefJQType(String strExpression, CurPos curpos)	{
		// assume here strExpression must include a valid data reference.
		int nEndofVariablePos = curpos.m_nPos;
		int nBracketLevel = 1;
		int i;
		/* find out the close bracket position. Assume the initial character must be [ */
		for (i = nEndofVariablePos + 1; i < strExpression.length(); i++) {
			if (strExpression.charAt(i) == ']')	{
				nBracketLevel --;
			} else if (strExpression.charAt(i) == '[') {
				nBracketLevel ++;
			}
			if (nBracketLevel == 0) {
				nEndofVariablePos = i;
				break;
			}
		}

		if (nBracketLevel != 0) {
			nEndofVariablePos = strExpression.length();
		}
		DataRefJQStr dRJQ = new DataRefJQStr();
		int nMaxChildDimension = 0;
		int nIndexStart = curpos.m_nPos + 1;
		DataRefJQStr elemDRJQ = new DataRefJQStr();
		dRJQ.mlistChildElems.add(elemDRJQ);
		while(nIndexStart < nEndofVariablePos) {
			for (int j = nIndexStart; j < nEndofVariablePos; j++) {
				if (!isBlankChar(strExpression, j)) {
					nIndexStart = j;
					break;
				}
			}
			CurPos curposSubExpr = new CurPos();
			curposSubExpr.m_nPos = nIndexStart;
			if (strExpression.charAt(nIndexStart) == '[')	{
				// this element is a data array
				DataRefJQStr dRJQTmp = getDataRefJQType(strExpression, curposSubExpr);
				elemDRJQ.mnDimension = dRJQTmp.mnDimension;
				elemDRJQ.mstrJQElem = dRJQTmp.mstrJQElem;
				elemDRJQ.mlistChildElems = dRJQTmp.mlistChildElems;
				if (elemDRJQ.mnDimension > nMaxChildDimension)	{
					nMaxChildDimension = elemDRJQ.mnDimension;
				}
			} else	{
                // this element is not a data array. so we have to create a substring from nIndexStart to close ]
                // because ] cannot be the end of expression
                String strUntilEnd = strExpression.substring(nIndexStart, nEndofVariablePos);
                CurPos curposNew = new CurPos();
                String elemStr = JQExprGenerator.cvtExpr2JQMath(strUntilEnd, curposNew);
                curposSubExpr.m_nPos += curposNew.m_nPos;
                elemDRJQ.mstrJQElem = elemStr;
			}
            nIndexStart = curposSubExpr.m_nPos;
            for (int j = nIndexStart; j < nEndofVariablePos; j++) {
                if (!isBlankChar(strExpression, j)) {
                    nIndexStart = j;
                    break;
                }
            }
            // the next non-blank char is ,
            if (nIndexStart < strExpression.length() && strExpression.charAt(nIndexStart) == ',') {
                nIndexStart ++;
            }							
			if (nEndofVariablePos >= curposSubExpr.m_nPos + 1) {
				/* there is something left for further analysis */
				elemDRJQ = new DataRefJQStr();
				dRJQ.mlistChildElems.add(elemDRJQ);
			}							
		}
		dRJQ.mnDimension = nMaxChildDimension + 1;
		curpos.m_nPos = nEndofVariablePos + 1;
		return dRJQ;
	}
	
	public static String getDataRefJQStr(String strExpression, CurPos curpos, boolean bIsIndex)
	{
		boolean bValidDataRef = true;
		int nEndofVariablePos = curpos.m_nPos;
		int nBracketLevel = 1;
		int i;
		/* find out the close bracket position. Assume the initial character must be [ */
		for (i = nEndofVariablePos + 1; i < strExpression.length(); i++) {
			if (strExpression.charAt(i) == ']')	{
				nBracketLevel --;
			} else if (strExpression.charAt(i) == '[') {
				nBracketLevel ++;
			}
			if (nBracketLevel == 0) {
				nEndofVariablePos = i;
				break;
			}
		}

		if (nBracketLevel != 0) {
			nEndofVariablePos = strExpression.length();
			bValidDataRef = false;
		}

		/* get a string of index list */
		String str = "";
		if (bValidDataRef && !bIsIndex) {	// no error in the strExpression and the data reference is not a data index.
			DataRefJQStr dRJQ = getDataRefJQType(strExpression, curpos);
			str = dRJQ.getJQStr();
		} else {
            int nIndexStart = curpos.m_nPos + 1;
			str = "[";
			while(nIndexStart < nEndofVariablePos) {
				for (int j = nIndexStart; j < nEndofVariablePos; j++) {
					if (!isBlankChar(strExpression, j)) {
						nIndexStart = j;
						break;
					}
				}
				CurPos curposSubExpr = new CurPos();
				curposSubExpr.m_nPos = nIndexStart;
				if (strExpression.charAt(nIndexStart) == '[')	{
					// this element is a data array
					str += getDataRefJQStr(strExpression, curposSubExpr, bIsIndex || !bValidDataRef);
				} else	{
					// this element is not a data array. so we have to create a substring from nIndexStart to close ]
                    // because ] cannot be the end of expression
                    String strUntilEnd = strExpression.substring(nIndexStart, nEndofVariablePos);
                    CurPos curposNew = new CurPos();
					str += JQExprGenerator.cvtExpr2JQMath(strUntilEnd, curposNew);
                    curposSubExpr.m_nPos += curposNew.m_nPos;
				}
				nIndexStart = curposSubExpr.m_nPos;
				for (int j = nIndexStart; j < nEndofVariablePos; j++) {
					if (!isBlankChar(strExpression, j)) {
						nIndexStart = j;
						break;
					}
				}
                // the next non-blank char is ,
				if (nIndexStart < strExpression.length() && strExpression.charAt(nIndexStart) == ',') {
					str += ", ";
                    nIndexStart ++;
				}							
			}
			if (nEndofVariablePos < strExpression.length())	{
				str += strExpression.charAt(nEndofVariablePos);
			}
            curpos.m_nPos = nEndofVariablePos + 1;
		}
		
		return str;		
	}

	public static String getExprNameJQStr(String strExpression, CurPos curpos)
	{
		String str = "";
		int nCurNameCharPos = curpos.m_nPos;
	
		/* get the function or variable name */
		String strName = "";
		while ((strExpression.length() > nCurNameCharPos) && (isNameChar(strExpression, nCurNameCharPos) >= 1)) {
			strName += strExpression.charAt(nCurNameCharPos);
			nCurNameCharPos++;
		}
		
		/* identify if it is function or variable */
		int nCurFurtherCharPos = nCurNameCharPos;
		while ((strExpression.length() > nCurFurtherCharPos) && isBlankChar(strExpression, nCurFurtherCharPos)) {
			nCurFurtherCharPos ++;
		}	
	
		if ((strExpression.length() > nCurFurtherCharPos) && (strExpression.charAt(nCurFurtherCharPos) == '(')) { /* function */
			int nBracketLevel = 1;
			int nCloseBracketPos = nCurFurtherCharPos;
			int i;
			/* find out the close bracket position */
			for (i = nCurFurtherCharPos + 1; i < strExpression.length(); i++) {
				if (isStringStartChar(strExpression, i)) {
					CurPos curpos1 = new CurPos();
					curpos1.m_nPos = i;
					getStringJQStr(strExpression, curpos1);
					i = curpos1.m_nPos;
				}
				
				if (i >= strExpression.length())	{
					// ok, we have arrived at the end of the expression, but we cannot find close bracket.
					// do not jump out and throw exception
					break;
				} else if (strExpression.charAt(i) == ')')	{
					nBracketLevel --;
				} else if (strExpression.charAt(i) == '(')	{
					nBracketLevel ++;
				}
				
				if (nBracketLevel == 0)
				{
					nCloseBracketPos = i;
					break;
				}
			}
	
			if (nBracketLevel != 0) {	
				nCloseBracketPos = Math.min(strExpression.length(), i);	// do not throw exception, instead, set close bracket position to be the last char + 1.
			}
	
			LinkedList<String> liststrParamJQStrs = new LinkedList<String>();
			LinkedList<String> liststrParams = new LinkedList<String>();
			int nParameterStart = nCurFurtherCharPos + 1;
			boolean bNoParameter = true;  /* if there is no parameter */
			for (int j = nParameterStart; j < nCloseBracketPos; j++) {
    			if (!isBlankChar(strExpression, j)) {
					bNoParameter = false;
				}
			}
			if (bNoParameter == false) {
				while(nParameterStart < nCloseBracketPos) {
					String strParamList = strExpression.substring(nParameterStart, nCloseBracketPos);
					CurPos curposSubExpr = new CurPos();
					curposSubExpr.m_nPos = 0;
					String strParamJQStr = JQExprGenerator.cvtExpr2JQMath(strParamList, curposSubExpr);
					String strParam = strParamList.substring(0, Math.min(strParamList.length(), curposSubExpr.m_nPos));
					liststrParamJQStrs.add(strParamJQStr);
					liststrParams.add(strParam.trim());
					// need not to worry about null data
					if (curposSubExpr.m_nPos < strParamList.length() && strParamList.charAt(curposSubExpr.m_nPos) != ')')	{
						strParamList += strParamList.charAt(curposSubExpr.m_nPos);
					}
					
					nParameterStart += curposSubExpr.m_nPos + 1;	// + 1 is to move nParameterStart to the character after ,
					// do not throw exception even for the situation like abs(-1,)
				}
			}
			String strParams = "";
			for (int idx = 0; idx < liststrParamJQStrs.size(); idx ++)	{
				strParams += liststrParamJQStrs.get(idx);
				if (idx < liststrParamJQStrs.size() - 1)	{
					strParams += ",";
				}
			}
			if (strName.equalsIgnoreCase("sqrt"))	{
				str = "\u221A" + "{" + strParams + "}";
			} else if (strName.equalsIgnoreCase("exp"))	{
				str = "e^{" + strParams + "}";
			} else if (strName.equalsIgnoreCase("abs"))	{
				str = "|" + strParams + "|";
			} else if (strName.equalsIgnoreCase("integrate")
					&& (liststrParams.size() == 2 || liststrParams.size() == 4)
					&& liststrParams.get(0).length() > 0 && liststrParams.get(0).charAt(0) == '"'
					&& liststrParams.get(1).length() > 0 && liststrParams.get(1).charAt(0) == '"')	{
				// if it is function integrate and this function has 4 parameters and the first and second parameters are strings.
				
				String strExprIntegrated = liststrParams.get(0).substring(1);
				if (strExprIntegrated.length() > 0 && strExprIntegrated.charAt(strExprIntegrated.length() - 1) == '"')	{
					strExprIntegrated = strExprIntegrated.substring(0, strExprIntegrated.length() - 1);
				}
				strExprIntegrated = JQExprGenerator.cvtExpr2JQMath(strExprIntegrated, new CurPos());
				String strVarName = liststrParams.get(1).substring(1);
				if (strVarName.length() > 0 && strVarName.charAt(strVarName.length() - 1) == '"')	{
					strVarName = strVarName.substring(0, strVarName.length() - 1);
				}
				strVarName = JQExprGenerator.cvtExpr2JQMath(strVarName, new CurPos());
				if (liststrParams.size() == 2) {
					str = "\u222B" + strExprIntegrated + "d" + strVarName;
				} else { // liststrParams.size() == 4
					String strFrom = liststrParamJQStrs.get(2);
					if (liststrParams.get(2).length() > 0 && liststrParams.get(2).charAt(0) == '"')	{
						strFrom = liststrParams.get(2).substring(1);
						if (strFrom.length() > 0 && strFrom.charAt(strFrom.length() - 1) == '"')	{
							strFrom = strFrom.substring(0, strFrom.length() - 1);
						}
						strFrom = JQExprGenerator.cvtExpr2JQMath(strFrom, new CurPos());
					}
					String strTo = liststrParamJQStrs.get(3);
					if (liststrParams.get(3).length() > 0 && liststrParams.get(3).charAt(0) == '"')	{
						strTo = liststrParams.get(3).substring(1);
						if (strTo.length() > 0 && strTo.charAt(strTo.length() - 1) == '"')	{
							strTo = strTo.substring(0, strTo.length() - 1);
						}
						strTo = JQExprGenerator.cvtExpr2JQMath(strTo, new CurPos());
					}
					str = "\u222B_{" + strFrom + "}^{" + strTo + "}" + strExprIntegrated + "d" + strVarName;
				}
			} else if ((strName.equalsIgnoreCase("sum_over") || strName.equalsIgnoreCase("product_over")) && liststrParams.size() == 3
					&& liststrParams.get(0).length() > 0 && liststrParams.get(0).charAt(0) == '"'
					&& liststrParams.get(1).length() > 0 && liststrParams.get(1).charAt(0) == '"'
					&& liststrParams.get(2).length() > 0 && liststrParams.get(2).charAt(0) == '"')	{
				// if it is function sum_over or product_over and this function has 3 parameters and all parameters are strings.
				String strFuncChar = strName.equalsIgnoreCase("sum_over")?"\u03A3":"\u03A0";
				String strExprProcessed = liststrParams.get(0).substring(1);
				if (strExprProcessed.length() > 0 && strExprProcessed.charAt(strExprProcessed.length() - 1) == '"')	{
					strExprProcessed = strExprProcessed.substring(0, strExprProcessed.length() - 1);
				}
				strExprProcessed = JQExprGenerator.cvtExpr2JQMath(strExprProcessed, new CurPos());
				String strFrom = liststrParams.get(1).substring(1);
				if (strFrom.length() > 0 && strFrom.charAt(strFrom.length() - 1) == '"')	{
					strFrom = strFrom.substring(0, strFrom.length() - 1);
				}
				strFrom = JQExprGenerator.cvtExpr2JQMath(strFrom, new CurPos());
				String strTo = liststrParams.get(2).substring(1);
				if (strTo.length() > 0 && strTo.charAt(strTo.length() - 1) == '"')	{
					strTo = strTo.substring(0, strTo.length() - 1);
				}
				strTo = JQExprGenerator.cvtExpr2JQMath(strTo, new CurPos());
				str = strFuncChar + "\u2199{" + strFrom + "}\u2196{" + strTo + "}(" + strExprProcessed + ")"; 
			} else if ((strName.equalsIgnoreCase("lim")) && liststrParams.size() == 3
					&& liststrParams.get(0).length() > 0 && liststrParams.get(0).charAt(0) == '"'
					&& liststrParams.get(1).length() > 0 && liststrParams.get(1).charAt(0) == '"')	{
				// if it is function lim and this function has 3 parameters and first two parameters are strings.
				String strFuncChar = "\\lim";
				String strExprProcessed = liststrParams.get(0).substring(1);
				if (strExprProcessed.length() > 0 && strExprProcessed.charAt(strExprProcessed.length() - 1) == '"')	{
					strExprProcessed = strExprProcessed.substring(0, strExprProcessed.length() - 1);
				}
				strExprProcessed = JQExprGenerator.cvtExpr2JQMath(strExprProcessed, new CurPos());
				String strVariableName = liststrParams.get(1).substring(1);
				if (strVariableName.length() > 0 && strVariableName.charAt(strVariableName.length() - 1) == '"')	{
					strVariableName = strVariableName.substring(0, strVariableName.length() - 1);
				}
				String strVariableDest = liststrParamJQStrs.get(2);
				if (liststrParams.get(2).length() > 0 && liststrParams.get(2).charAt(0) == '"')	{
					strVariableDest = liststrParams.get(2).substring(1);
					if (strVariableDest.length() > 0 && strVariableDest.charAt(strVariableDest.length() - 1) == '"')	{
						strVariableDest = strVariableDest.substring(0, strVariableDest.length() - 1);
					}
					strVariableDest = JQExprGenerator.cvtExpr2JQMath(strVariableDest, new CurPos());
				}
				str = strFuncChar + "\u2199{" + strVariableName + "\u2192" + strVariableDest + "}(" + strExprProcessed + ")"; 
			} else	{
				str = "\\text\"" + strName + "\"(" + strParams;
				if (nCloseBracketPos < strExpression.length())	{
					str += ")";
				}
			}
			/* tell the EveluateExpression function to identify next character. */
			curpos.m_nPos = nCloseBracketPos + 1;
			return str;
		} else if (strExpression.length() == nCurFurtherCharPos
				|| (strExpression.length() > nCurFurtherCharPos
						&& (strExpression.charAt(nCurFurtherCharPos) == ',' ||
						strExpression.charAt(nCurFurtherCharPos) == '[' ||
						strExpression.charAt(nCurFurtherCharPos) == ')' ||
						isCalcOperatorChar(strExpression, nCurFurtherCharPos)))) {
			/*
			 * str length <= further char pos means we are at the end of string, this means it
			 * is a variable. It may also be a function name. JMAnalyzer will first look up
			 * variable name space then look up function name space for it.
			 * if str length > further char pos and the further char is ',', it is a parameter
			 * which could be either a variable or a function name.
			 * if str length > further char pos and the further char is '[', the follows are a
			 * data index.
			 */
			if (strName.equalsIgnoreCase("pi"))	{
				strName = "\u03C0";
			} else if (strName.equalsIgnoreCase("inf"))	{
                strName = "\u221E";
			} else if (strName.equalsIgnoreCase("infi"))	{
                strName = "\u221E i";
            }
			str = "\\text\"" + strName + "\"";  //nani is supported here.
			curpos.m_nPos = nCurFurtherCharPos;
			return str;
		} else { /* neither function nor variable */
			// this should means an error, i.e. lack operator between two operands. But we don't throw an error, so simply ignore.
			curpos.m_nPos = nCurFurtherCharPos;
			return str + "\\text\"" + strName + " \"";
		}
	}
	
	public static String getStringJQStr(String strExpression, CurPos curpos)
	{
		String strStringStart = "\"";
		if (isStringStartChar(strExpression, curpos.m_nPos) == false)	{
			strStringStart = convt2JQEscapedStr(strExpression.substring(curpos.m_nPos, curpos.m_nPos + 1));
		}
		char cStartChar = strExpression.charAt(curpos.m_nPos);
		char cEscapeChar = '\\';
		boolean bInEscapeMode = false;
		int i;
		String str = "";
		for (i = curpos.m_nPos + 1; i < strExpression.length(); i++)
		{
			if (strExpression.charAt(i) == cEscapeChar && !bInEscapeMode)	{
				bInEscapeMode = true;
			} else if (bInEscapeMode)	{
				switch (strExpression.charAt(i))	{
				case 'b':
					str += '\b';
					break;
				case 'f':
					str += '\f';
					break;
				case 'n':
					str += '\n';
					break;
				case 'r':
					str += '\r';
					break;
				case 't':
					str += '\t';
					break;
				case '\\':
					str += '\\';
					break;
				case '\'':
					str += '\'';
					break;
				case '\"':
					str += '\"';
					break;
				default:
					str += strExpression.charAt(i);
				}
				bInEscapeMode = false;
			} else if (strExpression.charAt(i) == cStartChar)	{
				break;
			} else	{
				str += strExpression.charAt(i);
			}
		}
		// don't throw any exception even if no close " found.
		boolean bCannotFindCloseQuatation = false;
		if (i >= strExpression.length())	{
			bCannotFindCloseQuatation = true;
		}
		curpos.m_nPos = i + 1;
		str = str.replace("`", "``");
		str = str.replace("\"", "`\"");
        strStringStart = strStringStart.replace("`", "``");
        strStringStart = strStringStart.replace("\"", "`\"");
        String strEnd = (bCannotFindCloseQuatation?"":"\"");
        strEnd = strEnd.replace("\"", "`\"");
		str = "\\text\"" + strStringStart + str + strEnd + "\"";
		return str;
	}
}
