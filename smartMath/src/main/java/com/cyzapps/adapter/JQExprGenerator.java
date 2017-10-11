package com.cyzapps.adapter;

import com.cyzapps.Jfcalc.BaseData.BoundOperator;
import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.*;
import java.util.LinkedList;

public class JQExprGenerator	{
    
	public static String convertTwoOperandCell(String strFirstOperand,
			CalculateOperator COPTROperator, String strSecondOperand) {

		String strOpt = "";
		
		switch (COPTROperator.getOperatorType()) {
		case OPERATOR_ASSIGN:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("=");
			break;
		case OPERATOR_EQ:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("=");   // EQ is also output as =
			break;
		case OPERATOR_NEQ:
			strOpt = "\u2260"; 
			break;
		case OPERATOR_LARGER:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr(">");
			break;
		case OPERATOR_SMALLER:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("<");
			break;
		case OPERATOR_NOLARGER:
			strOpt = "\u2264";
			break;
		case OPERATOR_NOSMALLER:
			strOpt = "\u2265";
			break;
		case OPERATOR_ADD:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("+");
			break;
		case OPERATOR_SUBTRACT:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("-");
			break;
		case OPERATOR_MULTIPLY:
			strOpt = "\u00D7";
			break;
		case OPERATOR_DIVIDE:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("/");
			break;
		case OPERATOR_LEFTDIVIDE:
            strOpt = "^T";
			break;
		case OPERATOR_AND:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("&");
            break;
		case OPERATOR_OR:
			strOpt = "\u2223";
			break;
		case OPERATOR_XOR:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("^");
			break;
		case OPERATOR_POWER:
            strOpt = "^";
			break;
		default:
			strOpt = " ";
		}
		return "{" + strFirstOperand + "}" + strOpt + "{" + strSecondOperand + "}";
	}

	public static String convertOneOperandCell(String strOperand, CalculateOperator COPTROperator) {
		OPERATORTYPES enumOperatorType = COPTROperator.getOperatorType();
		String strOpt = "";
		switch (enumOperatorType) {
		case OPERATOR_PERCENT: {
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("%");
			break;
		}
		case OPERATOR_FACTORIAL: {
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("!");
			break;
		}
		case OPERATOR_TRANSPOSE:	{
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("'");
			break;
		}
		default:
			strOpt = " ";
		}
		return "{" + strOperand + "}" + strOpt; // need not to add {} around strOpt because strOpt must be like [...]
	}

	public static String convertOneOperandCell(CalculateOperator COPTROperator, String strOperand) {
		OPERATORTYPES enumOperatorType = COPTROperator.getOperatorType();
		String strOpt = "";

		switch (enumOperatorType) {
		case OPERATOR_FALSE:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("!");
			break;
		case OPERATOR_NOT:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("~");
			break;
		case OPERATOR_POSSIGN:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("+");
			break;
		case OPERATOR_NEGSIGN:
			strOpt = JQElemAnalyzer.convt2JQEscapedStr("-");
			break;
		default:
			strOpt = " ";
		}

		return strOpt + "{" + strOperand + "}";
	}

	public static String convertIndex(String strToBeIndex, String strIndex)	{
		return "{" + strToBeIndex + "}" + strIndex;
	}
	
    public static String cvtExpr2JQMath(String strExpression, CurPos curpos) {
        LinkedList<String> listValues = new LinkedList<String>();
		LinkedList<CalculateOperator> listCalcOpts = new LinkedList<CalculateOperator>();
        
		BoundOperator BOPTRCurboptr;
		CalculateOperator COPTRStackTopcoptr;
		CalculateOperator COPTRCurcoptr = new CalculateOperator();
		String str = "";

		int nLastPushedCellType = 0; /* 1 for number, 2 for calculation operator */
		int nRecursionStartPosition = curpos.m_nPos;

		while (curpos.m_nPos < strExpression.length()) {

			/* A bound char */
			if (ElemAnalyzer.isBoundOperatorChar(strExpression, curpos.m_nPos)) {
				BOPTRCurboptr = JQElemAnalyzer.getBoundOperator(strExpression, curpos);
				// need not to worry about OPERATOR_NOTEXIST because it has been handled
				// in GetBoundOperator
				if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_LEFTPARENTHESE)) {
					str = "(" + cvtExpr2JQMath(strExpression, curpos);

					listValues.addFirst(str);
					nLastPushedCellType = 1;
				}

				else if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_RIGHTPARENTHESE)) {
					if (listValues.size() == 0) {
                        str = "";
                    } else {
                        str = listValues.poll();
                    }
                    if (listCalcOpts.size() == 0) {
                        COPTRStackTopcoptr = null;
                    } else {
                        COPTRStackTopcoptr = listCalcOpts.poll(); 
                    }

					while (COPTRStackTopcoptr != null) {
						if (COPTRStackTopcoptr.getOperandNum() == 1) {
	                        str = convertOneOperandCell(COPTRStackTopcoptr, str);
						} else {	// COPTRStackTopcoptr.getOperandNum() == 2
							String strFirstOperand;
							strFirstOperand = (listValues.size() == 0)?"":listValues.poll();
							str = convertTwoOperandCell(
									strFirstOperand, COPTRStackTopcoptr,
									str);
						}

						COPTRStackTopcoptr = listCalcOpts.poll();
					}
					return str + ")";
				}

			}

			/* an operator char */
			else if (ElemAnalyzer.isCalcOperatorChar(strExpression,
					curpos.m_nPos)) {
				while ((strExpression.length() > curpos.m_nPos)
						&& (ElemAnalyzer.isCalcOperatorChar(strExpression, curpos.m_nPos)
								|| ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))) {
					if (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))
						curpos.m_nPos++;
					else {
						COPTRCurcoptr = JQElemAnalyzer.getCalcOperator(strExpression, curpos, nLastPushedCellType);
						// need not worry about OPERATOR_NOTEXIST here because OPERATOR_NOTEXIST has been handled
						// in elemAnalyzer.GetCalcOperator.
						if (COPTRCurcoptr.getLabelPrefix()) {
							break;
						} else {
							str = (listValues.size() == 0)?"":listValues.poll();
							str = convertOneOperandCell(str, COPTRCurcoptr);
							
							listValues.addFirst(str);
							nLastPushedCellType = 1;
						}
					}
				}

				if (COPTRCurcoptr.getLabelPrefix()) {
					if (COPTRCurcoptr.getOperandNum() == 2) {
                        boolean bHaveBOptHigherLevelLeft = false;
                        for (int idx = 0; idx < listCalcOpts.size(); idx ++) {
                            if (listCalcOpts.get(idx).getOperandNum() == 2) {
                                if (!ElemAnalyzer.is2ndOPTRHaveHighLevel(listCalcOpts.get(idx), COPTRCurcoptr)) {
                                    bHaveBOptHigherLevelLeft = true;
                                }
                                break;
                            }
                        }
						str = (listValues.size() == 0)?"":listValues.poll();
						COPTRStackTopcoptr = listCalcOpts.poll();
                        while (COPTRStackTopcoptr != null
                                && ((bHaveBOptHigherLevelLeft && COPTRStackTopcoptr.getOperandNum() == 1)
                                    || !ElemAnalyzer.is2ndOPTRHaveHighLevel(COPTRStackTopcoptr, COPTRCurcoptr))) {
                            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                            	str = convertOneOperandCell(COPTRStackTopcoptr, str);
                            } else {	// COPTRStackTopcoptr.getOperandNum() == 2
								String strFirstOperand = (listValues.size() == 0)?"":listValues.poll();
								str = convertTwoOperandCell(
										strFirstOperand, COPTRStackTopcoptr,
										str);
								
							}
							COPTRStackTopcoptr = listCalcOpts.poll();
                        }
						if (COPTRStackTopcoptr != null) {
							listCalcOpts.addFirst(COPTRStackTopcoptr);
						}
						listValues.addFirst(str);
					}

					listCalcOpts.addFirst(COPTRCurcoptr);
					nLastPushedCellType = 2;
				}
			}

			/* The new cell seems to be a number */
			else if (ElemAnalyzer.isStartNumberChar(strExpression, curpos.m_nPos)) {
				str = JQElemAnalyzer.getNumberJQStr(strExpression, curpos);

				listValues.addFirst(str);
				nLastPushedCellType = 1;
			}

			/* The new cell seems to be a data reference */
			else if (ElemAnalyzer.isDataRefChar(strExpression, curpos.m_nPos)) {
				str = JQElemAnalyzer.getDataRefJQStr(strExpression, curpos, (nLastPushedCellType == 1));

                if (nLastPushedCellType == 1)	{	// last time a value was pushed, not an operator, this data reference should be an index.
					String strToBeIndexed = (listValues.size() == 0)?"":listValues.poll();
					str = convertIndex(strToBeIndexed, str);
					
				}
				listValues.addFirst(str);
				nLastPushedCellType = 1;
			}

			/*
			 * The new cell seems to be the beginning of a variable or
			 * function's name
			 */
			else if (ElemAnalyzer.isNameChar(strExpression, curpos.m_nPos) == 1) {
				str = JQElemAnalyzer.getExprNameJQStr(strExpression, curpos);
				
				listValues.addFirst(str);
				nLastPushedCellType = 1;
			}

			/* A blank char */
			else if (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))
				curpos.m_nPos++;
			/* A "," */
			else if (strExpression.charAt(curpos.m_nPos) == ',') {
				/*
				 * we do not add curpos.m_nPos++; here because curpos stops at
				 * the first char which does not belong to the expression.
				 */
				break; /* ignore part of the expression after "," */
			}
			else if (ElemAnalyzer.isStringStartChar(strExpression, curpos.m_nPos))	{
				str = JQElemAnalyzer.getStringJQStr(strExpression, curpos);
				listValues.addFirst(str);
				nLastPushedCellType = 1;
			} else {
				// the character is not a valid char, discard it, not throw any exception.
				str = strExpression.substring(curpos.m_nPos, curpos.m_nPos + 1);
                //listValues.addFirst(str);
				curpos.m_nPos++;
			}

		}
		/* arriving here means we are in the top level of expression */
		if (listValues.size() == 0) {
            str = "";
        } else {
            str = listValues.poll();
        }
		COPTRStackTopcoptr = listCalcOpts.poll();
		while (COPTRStackTopcoptr != null) {
			if (COPTRStackTopcoptr.getOperandNum() == 1) {
				str = convertOneOperandCell(COPTRStackTopcoptr, str);
			} else {	// COPTRStackTopcoptr.getOperandNum() == 2
				String strFirstOperand;
                if (listValues.size() == 0) {
                    strFirstOperand = "";
                } else {
                    strFirstOperand = listValues.poll();
                }
				str = convertTwoOperandCell(strFirstOperand, COPTRStackTopcoptr, str);
			}

            COPTRStackTopcoptr = listCalcOpts.poll();
		}
        
        while (listValues.size() > 0) {
            str = listValues.poll() + str;  // add stacked strings.
        }

		return str;
    }
}