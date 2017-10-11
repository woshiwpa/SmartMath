package com.cyzapps.adapter;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;

import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEAssign;
import com.cyzapps.Jsma.AEBitwiseOpt;
import com.cyzapps.Jsma.AECompare;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEDataRef;
import com.cyzapps.Jsma.AEFunction;
import com.cyzapps.Jsma.AEIndex;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AELeftDivOpt;
import com.cyzapps.Jsma.AEMulDivOpt;
import com.cyzapps.Jsma.AEOnOffUnaryOpt;
import com.cyzapps.Jsma.AEPosNegOpt;
import com.cyzapps.Jsma.AEPowerOpt;
import com.cyzapps.Jsma.AEUnaryOpt;
import com.cyzapps.Jsma.AEVar;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AbstractExprConverter {
	public static String convtAExpr2QuotedUrl(AbstractExpr aexpr, boolean bUseAE2Str) throws JFCALCExpErrException, JSmartMathErrException	{
		if (bUseAE2Str)	{
			return convtPlainStr2QuotedUrl(aexpr.toString());
		} else	{
			return convtPlainStr2QuotedUrl(convtAExpr2PlainString(aexpr));
		}
	}
	
	public static String convtAExpr2QuotedUrl(AbstractExpr aexpr) throws JFCALCExpErrException, JSmartMathErrException {
		// by default, use function convtAExpr2PlainString
		return convtPlainStr2QuotedUrl(convtAExpr2PlainString(aexpr));
	}
	
	public static String convtAExpr2PlainString(AbstractExpr aexpr) throws JFCALCExpErrException, JSmartMathErrException	{
		String strOutput = "";
		if (aexpr instanceof AEAssign)	{
			AEAssign ae = (AEAssign)aexpr;
			for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
	            boolean bNeedBracketsWhenToStr = false;
	            if (idx > 0 && idx < ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 0);
	            } else if (idx == 0 && idx < ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 1);
	            } else if (idx > 0 && idx == ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, -1);
	            }
	            if (bNeedBracketsWhenToStr) {
	                strOutput += "(" + convtAExpr2PlainString(ae.mlistChildren.get(idx)) + ")";
	            } else  {
	                strOutput += convtAExpr2PlainString(ae.mlistChildren.get(idx));
	            }
				if (idx != ae.mlistChildren.size() - 1)	{
					strOutput += "=";
				}
			}
		} else if (aexpr instanceof AEBitwiseOpt)	{
			AEBitwiseOpt ae = (AEBitwiseOpt)aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeLeft);
	        }
	        strOutput += ae.moptType.output();
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeRight);
	        }
		} else if (aexpr instanceof AECompare)	{
			AECompare ae = (AECompare)aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeLeft);
	        }
	        strOutput += ae.moptType.output();
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeRight);
	        }
		} else if (aexpr instanceof AEConst)	{
			// use function MFPAdapter.outputDatum instead of mdatumValue.output
			// because user may not want to see like 0.6666666666666666666666666666666666666666666666667
			// we have to round the digits.
			strOutput = MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef())[1];
		} else if (aexpr instanceof AEDataRef)	{
			AEDataRef ae = (AEDataRef)aexpr;
			strOutput = "[";
			for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
	            // we do not need to consider use ().
				strOutput += convtAExpr2PlainString(ae.mlistChildren.get(idx));
				if (idx < ae.mlistChildren.size() - 1)	{
					strOutput += ",";
				} else	{
					strOutput += "]";
				}
			}
		} else if (aexpr instanceof AEFunction)	{
			AEFunction ae = (AEFunction)aexpr;
			strOutput = ae.mstrFuncName + "(";
			for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
	            // we do not need to consider to use ()
				strOutput += convtAExpr2PlainString(ae.mlistChildren.get(idx));
				if (idx < ae.mlistChildren.size() - 1)	{
					strOutput += ",";
				} else	{
					strOutput += ")";
				}
			}
		} else if (aexpr instanceof AEIndex)	{
			AEIndex ae = (AEIndex)aexpr;
			boolean bBaseNeedBracketsWhenToStr = false;
	        bBaseNeedBracketsWhenToStr = ae.maeBase.needBracketsWhenToStr(ae.menumAEType, 1);
	        
	        if (bBaseNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeBase) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeBase);
	        }
	        strOutput += convtAExpr2PlainString(ae.maeIndex);
		} else if (aexpr instanceof AEInvalid)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		} else if (aexpr instanceof AELeftDivOpt)	{
			AELeftDivOpt ae = (AELeftDivOpt)aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeLeft);
	        }
	        strOutput += "\\";
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeRight);
	        }
		} else if (aexpr instanceof AEMulDivOpt)	{
			AEMulDivOpt ae = (AEMulDivOpt)aexpr;
	        if (ae.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)  {
	            boolean bNeedBracketsWhenToStr = false;
	            if (ae.mlistChildren.size() > 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(0).needBracketsWhenToStr(ae.menumAEType, 1);
	            }
	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2PlainString(ae.mlistChildren.get(0)) + ")";
	            } else  {
	                strOutput = convtAExpr2PlainString(ae.mlistChildren.get(0));
	            }
	        } else  {   // divide
	            boolean bNeedBracketsWhenToStr = false;
	            if (ae.mlistChildren.size() > 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(0).needBracketsWhenToStr(ae.menumAEType, 0);
	            } else  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(0).needBracketsWhenToStr(ae.menumAEType, -1);
	            }
	            if (bNeedBracketsWhenToStr) {
	                strOutput = "1/(" + convtAExpr2PlainString(ae.mlistChildren.get(0)) + ")";
	            } else  {
	                strOutput = "1/" + convtAExpr2PlainString(ae.mlistChildren.get(0));
	            }
	        }
	        for (int idx = 1; idx < ae.mlistOpts.size(); idx ++)	{
	            boolean bNeedBracketsWhenToStr = false;
	            if (idx < ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 0);
	            } else if (idx == ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, -1);
	            }
	            strOutput += ae.mlistOpts.get(idx).getOperatorType().output();
	            if (bNeedBracketsWhenToStr) {
	                strOutput += "(" + convtAExpr2PlainString(ae.mlistChildren.get(idx)) + ")";
	            } else  {
	                strOutput += convtAExpr2PlainString(ae.mlistChildren.get(idx));
	            }
	        }
		} else if (aexpr instanceof AEOnOffUnaryOpt)	{
			AEOnOffUnaryOpt ae = (AEOnOffUnaryOpt) aexpr;
			if (ae.mnNumofOpts == 0)	{
				strOutput = convtAExpr2PlainString(ae.maexprChild);
			} else	{
	            boolean bNeedBracketsWhenToStr = false;
	            if (ae.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)   {
	                bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, 1);
	            } else  {   // FALSE or NOT
	                bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, -1);
	            }

	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2PlainString(ae.maexprChild) + ")";
	            } else  {
	                strOutput = convtAExpr2PlainString(ae.maexprChild);
	            }
			}
			for (int idx = 0; idx < ae.mnNumofOpts; idx ++)	{
				if (ae.getOpt().getLabelPrefix() == false)	{
					strOutput += ae.getOptType().output();
				} else	{
					strOutput = ae.getOptType().output() + strOutput;
				}
			}
		} else if (aexpr instanceof AEPosNegOpt)	{
			AEPosNegOpt ae = (AEPosNegOpt) aexpr;
			if (ae.mlistChildren.size() == 1 && (ae.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
					|| ae.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN))	{
				// expression is +value
				strOutput = convtAExpr2PlainString(ae.mlistChildren.get(0));
			} else	{
				for (int idx = 0; idx < ae.mlistOpts.size(); idx ++)	{
	                boolean bLeftHasOpt = false, bRightHasOpt = false;
					if (ae.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
							|| ae.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)	{
						if (idx != 0)	{
							strOutput += "+";
	                        bLeftHasOpt = true;
						}
					} else	{
						strOutput += "-";
	                    bLeftHasOpt = true;
					}
	                if (idx < ae.mlistOpts.size() - 1) {
	                    bRightHasOpt = true;
	                }
	                
	                boolean bNeedBracketsWhenToStr = false;
	                if (bLeftHasOpt && bRightHasOpt)  {
	                    bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 0);
	                } else if (bLeftHasOpt && !bRightHasOpt)    {
	                    bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, -1);
	                } else if (!bLeftHasOpt && bRightHasOpt)    {
	                    bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 1);
	                }
	                if (bNeedBracketsWhenToStr) {
	                    strOutput += "(" + convtAExpr2PlainString(ae.mlistChildren.get(idx)) + ")";
	                } else  {
	                    strOutput += convtAExpr2PlainString(ae.mlistChildren.get(idx));
	                }
				}
			}
		} else if (aexpr instanceof AEPowerOpt)	{
			// Note that because base and power to are in different lines, output of aePowerOpt is a bit different from the follows.
			AEPowerOpt ae = (AEPowerOpt) aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeLeft);
	        }
	        strOutput += "**";
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2PlainString(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2PlainString(ae.maeRight);
	        }
		} else if (aexpr instanceof AEUnaryOpt)	{
			AEUnaryOpt ae = (AEUnaryOpt) aexpr;
	        boolean bNeedBracketsWhenToStr = false;
			if (ae.getOpt().getLabelPrefix())	{
	            bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, -1);
	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2PlainString(ae.maexprChild) + ")";
	            } else  {
	                strOutput = convtAExpr2PlainString(ae.maexprChild);
	            }
				strOutput = ae.getOptType().output() + strOutput;
			} else	{
	            bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, 1);
	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2PlainString(ae.maexprChild) + ")";
	            } else  {
	                strOutput = convtAExpr2PlainString(ae.maexprChild);
	            }
				strOutput += ae.getOptType();
			}
		} else if (aexpr instanceof AEVar)	{
			strOutput = ((AEVar)aexpr).mstrVariableName;
		}
		return strOutput;
	}
	
	// convert aexpr to JqMath expression
	public static String convtAExpr2JQMath(AbstractExpr aexpr) throws JFCALCExpErrException, JSmartMathErrException {
		String strOutput = "";
		if (aexpr instanceof AEAssign)	{
			AEAssign ae = (AEAssign)aexpr;
			for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
				if (idx > 0 )	{
					strOutput += "=";
				}
	            boolean bNeedBracketsWhenToStr = false;
	            if (idx > 0 && idx < ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 0);
	            } else if (idx == 0 && idx < ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 1);
	            } else if (idx > 0 && idx == ae.mlistChildren.size() - 1)  {
	                bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, -1);
	            }
	            if (bNeedBracketsWhenToStr) {
	                strOutput += "(" + convtAExpr2JQMath(ae.mlistChildren.get(idx)) + ")";
	            } else  {
	                strOutput += convtAExpr2JQMath(ae.mlistChildren.get(idx));
	            }
			}
		} else if (aexpr instanceof AEBitwiseOpt)	{
			AEBitwiseOpt ae = (AEBitwiseOpt)aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeLeft);
	        }
			if (ae.moptType == OPERATORTYPES.OPERATOR_AND)	{
				strOutput += "&";
			} else if (ae.moptType == OPERATORTYPES.OPERATOR_OR)	{
				strOutput += "\u2223";
			} else	{	// XOR
				strOutput += "\\^";
			}
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeRight);
	        }
		} else if (aexpr instanceof AECompare)	{
			AECompare ae = (AECompare)aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeLeft);
	        }
			if (ae.moptType == OPERATORTYPES.OPERATOR_LARGER)	{
				strOutput += ">";
			} else if (ae.moptType == OPERATORTYPES.OPERATOR_NOLARGER)	{
				strOutput += "\u2264";	// <=
			} else if (ae.moptType == OPERATORTYPES.OPERATOR_SMALLER)	{
				strOutput += "<";
			} else if (ae.moptType == OPERATORTYPES.OPERATOR_NOSMALLER)	{
				strOutput += "\u2265";	// >=
			} else if (ae.moptType == OPERATORTYPES.OPERATOR_NEQ)	{
				strOutput += "\u2260";
			} else	{	// =
				strOutput += "=";
			}
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeRight);
	        }
		} else if (aexpr instanceof AEConst)	{
			// use function MFPAdapter.outputDatum instead of mdatumValue.output
			// because user may not want to see like 0.6666666666666666666666666666666666666666666666667
			// we have to round the digits.
			if (((AEConst)aexpr).getDataClassRef().getDataType() == DATATYPES.DATUM_COMPLEX)	{
				String strReal = "", strImage = "", strRealImageOpt = "";
				if (!((AEConst)aexpr).getDataClassRef().getReal().isActuallyZero())	{
					if (((AEConst)aexpr).getDataClassRef().getReal().isPosInf())	{
						strReal = "\u221E";
					} else if (((AEConst)aexpr).getDataClassRef().getReal().isNegInf()) {
						strReal = "-\u221E";
					} else {
						strReal = "\\text\"" + MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef().getRealDataClass())[1] + "\"";	// prevent italian characters
					}
				}
				if (((AEConst)aexpr).getDataClassRef().getImage().isNan())	{
					strImage = "";
					if (!((AEConst)aexpr).getDataClassRef().getReal().isActuallyZero())	{
						strImage += "+";
					}
					strImage += "\\text\"" + MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef().getImageDataClass())[1] + "\"i";	// do not need multiply
				} else if (((AEConst)aexpr).getDataClassRef().getImage().isActuallyPositive())	{
					strRealImageOpt = (strReal.length() == 0)?"":"+";
					if (((AEConst)aexpr).getDataClassRef().getImage().isEqual(MFPNumeric.ONE))	{
						strImage = "i";
					} else if (((AEConst)aexpr).getDataClassRef().getImage().isInf()) {
						strImage = "\u221E i"; // do not need multiply
					} else	{
						strImage = "\\text\"" + MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef().getImageDataClass())[1] + "\"\u00D7" + "i";	// prevent italian characters
					}
				} else if (((AEConst)aexpr).getDataClassRef().getImage().isActuallyNegative())	{
					//strRealImageOpt = "-"; should not add another minus because image value has include minus
					if (((AEConst)aexpr).getDataClassRef().getImage().isEqual(MFPNumeric.MINUS_ONE))	{
						strImage = "-i";
					} else if (((AEConst)aexpr).getDataClassRef().getImage().isInf()) {
						strImage = "-\u221E i"; // do not need multiply
					} else {
						strImage = "\\text\"" + MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef().getImageDataClass())[1] + "\"\u00D7" + "i";	// prevent italian characters
					}
				}// image is zero, do nothing.
				
				strOutput += strReal + strRealImageOpt + strImage;
				if (strOutput.length() == 0)	{
					strOutput = "0";
				}
			} else if (((AEConst)aexpr).getDataClassRef().getDataType() == DATATYPES.DATUM_REF_DATA)	{
				int[] narrayDim = ((AEConst)aexpr).getDataClassRef().recalcDataArraySize();
				int nMod2 = narrayDim.length % 2;
				if (nMod2 == 1)	{
					strOutput += "(\\table ";
					for (int idx = 0; idx < ((AEConst)aexpr).getDataClassRef().getDataListSize(); idx ++)	{
						if (idx > 0)	{
							strOutput += ", ";
						}
						strOutput += convtAExpr2JQMath(new AEConst(((AEConst)aexpr).getDataClassRef().getDataList()[idx]));
					}
					strOutput += ")";
				} else	{	// nMode2 == 0
					strOutput += "(\\table ";
					for (int idx = 0; idx < ((AEConst)aexpr).getDataClassRef().getDataListSize(); idx ++)	{
						if (idx > 0)	{
							strOutput += "; ";
						}
						if (((AEConst)aexpr).getDataClassRef().getDataList()[idx].getDataType() == DATATYPES.DATUM_REF_DATA)	{
							DataClass datumChild = ((AEConst)aexpr).getDataClassRef().getDataList()[idx];
							for (int idx1 = 0; idx1 < datumChild.getDataListSize(); idx1 ++)	{
								if (idx1 > 0)	{
									strOutput += ", ";
								}
								DataClass datumGrandChild = datumChild.getDataList()[idx1];
								AEConst aeGrandChild = new AEConst(datumGrandChild);
								strOutput += convtAExpr2JQMath(aeGrandChild);
							}
						} else	{
							strOutput += convtAExpr2JQMath(new AEConst(((AEConst)aexpr).getDataClassRef().getDataList()[idx]));
						}
					}
					strOutput += ")";
				}
			} else if (((AEConst)aexpr).getDataClassRef().getDataType() == DATATYPES.DATUM_STRING)	{
				strOutput = MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef())[1];	// after this statement, strOutput would be like ["abc " de"]
				strOutput = strOutput.replace("`", "``");
				strOutput = strOutput.replace("\"", "`\"");
				strOutput = "\\text\"" + strOutput + "\"";
			} else if ((((AEConst)aexpr).getDataClassRef().getDataType() == DATATYPES.DATUM_DOUBLE || ((AEConst)aexpr).getDataClassRef().getDataType() == DATATYPES.DATUM_INTEGER)
					&& ((AEConst)aexpr).getDataClassRef().getDataValue().isInf())	{
				if (((AEConst)aexpr).getDataClassRef().getDataValue().isNegative()) {
					strOutput = "-\u221E";
				} else {
					strOutput = "\u221E";
				}
			} else {
				strOutput = "\\text\"" + MFPAdapter.outputDatum(((AEConst)aexpr).getDataClassRef())[1] + "\"";	// prevent italian characters
			}
		} else if (aexpr instanceof AEDataRef)	{
			AEDataRef ae = (AEDataRef)aexpr;
			int[] narrayDim = ae.calcAExprMinDim();
			int nMod2 = narrayDim.length % 2;
			if (nMod2 == 1)	{
				strOutput += "(\\table ";
				for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
					if (idx > 0)	{
						strOutput += ", ";
					}
					strOutput += convtAExpr2JQMath(ae.mlistChildren.get(idx));
				}
				strOutput += ")";
			} else	{	// nMode2 == 0
				strOutput += "(\\table ";
				for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
					if (idx > 0)	{
						strOutput += "; ";
					}
					if (ae.mlistChildren.get(idx) instanceof AEDataRef)	{
						for (int idx1 = 0; idx1 < ((AEDataRef) ae.mlistChildren.get(idx)).mlistChildren.size(); idx1 ++)	{
							if (idx1 > 0)	{
								strOutput += ", ";
							}
							strOutput += convtAExpr2JQMath(((AEDataRef) ae.mlistChildren.get(idx)).mlistChildren.get(idx1));
						}
					} else if (ae.mlistChildren.get(idx) instanceof AEConst
							&& ((AEConst) ae.mlistChildren.get(idx)).getDataClassRef().getDataType() == DATATYPES.DATUM_REF_DATA)	{
						DataClass datumChild = ((AEConst) ae.mlistChildren.get(idx)).getDataClassRef();
						for (int idx1 = 0; idx1 < datumChild.getDataListSize(); idx1 ++)	{
							if (idx1 > 0)	{
								strOutput += ", ";
							}
							DataClass datumGrandChild = datumChild.getDataList()[idx1];
							AEConst aeGrandChild = new AEConst(datumGrandChild);
							strOutput += convtAExpr2JQMath(aeGrandChild);
						}
					} else	{
						strOutput += convtAExpr2JQMath(ae.mlistChildren.get(idx));
					}
				}
				strOutput += ")";
			}
		} else if (aexpr instanceof AEFunction)	{
			AEFunction ae = (AEFunction)aexpr;
			String[] strarrayParams = new String[ae.mlistChildren.size()];
			String[] strarrayParamsNoStrTxt = new String[ae.mlistChildren.size()];
			String[] strarrayParamsNoStrExpr = new String[ae.mlistChildren.size()];
			boolean[] barrayParamIsString = new boolean[ae.mlistChildren.size()];
			for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
				if (idx > 0)	{
					strOutput += ", ";
				}
				String strParam = convtAExpr2JQMath(ae.mlistChildren.get(idx));
				strOutput += strParam;
				strarrayParamsNoStrExpr[idx] = strarrayParamsNoStrTxt[idx] = strarrayParams[idx] = strParam;
				if (ae.mlistChildren.get(idx) instanceof AEConst
						&& ((AEConst)(ae.mlistChildren.get(idx))).getDataClassRef().getDataType() == DATATYPES.DATUM_STRING)	{
					barrayParamIsString[idx] = true;
					strarrayParamsNoStrExpr[idx] = strarrayParamsNoStrTxt[idx] = ((AEConst)(ae.mlistChildren.get(idx))).getDataClassRef().getStringValue();
					strarrayParamsNoStrTxt[idx] = strarrayParamsNoStrTxt[idx].replace("`", "``");
					strarrayParamsNoStrTxt[idx] = strarrayParamsNoStrTxt[idx].replace("\"", "`\"");
					strarrayParamsNoStrTxt[idx] = "\\text\"" + strarrayParamsNoStrTxt[idx] + "\"";
				}
			}
			if (ae.mstrFuncName.equalsIgnoreCase("sqrt"))	{
				strOutput = "\u221A" + "{" + strOutput + "}";
			} else if (ae.mstrFuncName.equalsIgnoreCase("exp"))	{
				strOutput = "e^{" + strOutput + "}";
			} else if (ae.mstrFuncName.equalsIgnoreCase("abs"))	{
				strOutput = "|" + strOutput + "|";
			} else if (ae.mstrFuncName.equalsIgnoreCase("integrate")
					&& (strarrayParams.length == 2 || strarrayParams.length == 4)
					&& barrayParamIsString[0] && barrayParamIsString[1])	{
				String strExpression = strarrayParamsNoStrExpr[0];
				try {
					strExpression = convtPlainStr2JQMath(strarrayParamsNoStrExpr[0], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strVarName = strarrayParamsNoStrExpr[1];
				try {
					strVarName = convtPlainStr2JQMath(strarrayParamsNoStrExpr[1], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (strarrayParams.length == 2) {
					strOutput = "\u222B" + strExpression + "d" + strVarName;
				} else {	// 4 parameters
					String strFrom = strarrayParams[2];
					if (barrayParamIsString[2])	{
						try {
							strFrom = convtPlainStr2JQMath(strarrayParamsNoStrExpr[2], false);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					String strTo = strarrayParams[3];
					if (barrayParamIsString[3])	{
						try {
							strTo = convtPlainStr2JQMath(strarrayParamsNoStrExpr[3], false);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					strOutput = "\u222B_{" + strFrom + "}^{" + strTo + "}" + strExpression + "d" + strVarName;
				}
			} else if ((ae.mstrFuncName.equalsIgnoreCase("sum_over") || ae.mstrFuncName.equalsIgnoreCase("product_over"))
					&& strarrayParams.length == 3 && barrayParamIsString[0] && barrayParamIsString[1] && barrayParamIsString[2])	{
				String strFuncChar = ae.mstrFuncName.equalsIgnoreCase("sum_over")?"\u03A3":"\u03A0";
				String strExpression = strarrayParamsNoStrExpr[0];
				try {
					strExpression = convtPlainStr2JQMath(strarrayParamsNoStrExpr[0], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strFrom = strarrayParamsNoStrExpr[1];
				try {
					strFrom = convtPlainStr2JQMath(strarrayParamsNoStrExpr[1], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strTo = strarrayParamsNoStrExpr[2];
				try {
					strTo = convtPlainStr2JQMath(strarrayParamsNoStrExpr[2], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				strOutput = strFuncChar + "\u2199{" + strFrom + "}\u2196{" + strTo + "}(" + strExpression + ")"; 
			} else if ((ae.mstrFuncName.equalsIgnoreCase("lim")) && strarrayParams.length == 3
					&& barrayParamIsString[0] && barrayParamIsString[1])	{
				String strFuncChar = "\\lim";
				String strExpression = strarrayParamsNoStrExpr[0];
				try {
					strExpression = convtPlainStr2JQMath(strarrayParamsNoStrExpr[0], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strVariableName = strarrayParamsNoStrExpr[1];
				try {
					strVariableName = convtPlainStr2JQMath(strarrayParamsNoStrExpr[1], false);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strVariableDest = strarrayParams[2];
				if (barrayParamIsString[2])	{
					try {
						strVariableDest = convtPlainStr2JQMath(strarrayParamsNoStrExpr[2], false);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				strOutput = strFuncChar + "\u2199{" + strVariableName + "\u2192" + strVariableDest + "}(" + strExpression + ")"; 
			} else	{
				//strOutput = "\\" + ae.mstrFuncName.replace("_", "\\_") + "(" + strOutput + ")";	// do not use this because the fond after _ will be italian.
				strOutput = "\\text\"" + ae.mstrFuncName + "\"(" + strOutput + ")";	// don't worry about the special chars like ", ` because they cannot be a part of funcname.
			}
		} else if (aexpr instanceof AEIndex)	{
			AEIndex ae = (AEIndex)aexpr;
	        boolean bBaseNeedBracketsWhenToStr = false;
	        bBaseNeedBracketsWhenToStr = ae.maeBase.needBracketsWhenToStr(ae.menumAEType, 1);
	        
	        if (bBaseNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeBase) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeBase);
	        }
			strOutput += "[";
			if (ae.maeIndex instanceof AEConst)	{
				for (int idx = 0; idx < ((AEConst)ae.maeIndex).getDataClassRef().getDataListSize(); idx ++)	{
					DataClass datumChild = ((AEConst)ae.maeIndex).getDataClassRef().getDataList()[idx];
					AEConst aeChild = new AEConst(datumChild);
					if (idx != 0)	{
						strOutput += ", ";
					}
					strOutput += convtAExpr2JQMath(aeChild);
				}
			} else if (ae.maeIndex instanceof AEDataRef)	{
				for (int idx = 0; idx < ((AEDataRef)ae.maeIndex).mlistChildren.size(); idx ++)	{
					if (idx != 0)	{
						strOutput += ", ";
					}
					strOutput += convtAExpr2JQMath(((AEDataRef)ae.maeIndex).mlistChildren.get(idx));
				}
			} else	{ // should only be AEConst or AEDataRef
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
			}
			strOutput += "]";
		} else if (aexpr instanceof AEInvalid)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		} else if (aexpr instanceof AELeftDivOpt)	{
			AELeftDivOpt ae = (AELeftDivOpt)aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        bRightNeedBracketsWhenToStr = ae.maeRight.needBracketsWhenToStr(ae.menumAEType, -1);
	        
	        if (bLeftNeedBracketsWhenToStr) {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeLeft);
	        }
	        strOutput += "\\\\";
	        if (bRightNeedBracketsWhenToStr)    {
	            strOutput += "(" + convtAExpr2JQMath(ae.maeRight) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeRight);
	        }
		} else if (aexpr instanceof AEMulDivOpt)	{
			AEMulDivOpt ae = (AEMulDivOpt)aexpr;
			LinkedList<AbstractExpr> listMultiplyChildren = new LinkedList<AbstractExpr>();
			LinkedList<AbstractExpr> listDenominatorChildren = new LinkedList<AbstractExpr>();
			for (int idx = 0; idx < ae.mlistChildren.size(); idx ++)	{
				if (idx == 0)	{
					if (ae.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE)	{
						strOutput += "1/";
						listDenominatorChildren.add(ae.mlistChildren.get(idx));
					} else	{
						listMultiplyChildren.add(ae.mlistChildren.get(idx));
					}
				} else	{
					if (ae.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)	{
						if (ae.mlistOpts.get(idx - 1).getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE)	{	// from divide change to multiply
							strOutput += "{";
							for (int idx1 = 0; idx1 < listDenominatorChildren.size(); idx1 ++)	{
								boolean bNeedBrackets = false;
								if (idx1 > 0 && idx1 < listDenominatorChildren.size() - 1)	{
									bNeedBrackets = listDenominatorChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, 0);
								} else if (idx1 > 0 && idx1 == listDenominatorChildren.size() - 1)	{
									bNeedBrackets = listDenominatorChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, -1);
								} else if (idx1 == 0 && idx1 < listDenominatorChildren.size() - 1)	{
									bNeedBrackets = listDenominatorChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, 1);
								}
								if (bNeedBrackets)	{
									strOutput += "(" + convtAExpr2JQMath(listDenominatorChildren.get(idx1)) + ")";
								} else	{
									strOutput += convtAExpr2JQMath(listDenominatorChildren.get(idx1));
								}
								if (idx1 < listDenominatorChildren.size() - 1)	{
									strOutput += "\u00D7";
								}
							}
							strOutput += "}" + "\u00D7";
							listDenominatorChildren.clear();
						}
						listMultiplyChildren.add(ae.mlistChildren.get(idx));
					} else {
						if (ae.mlistOpts.get(idx - 1).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)	{
							// last is multiply while this is divide
							for (int idx1 = 0; idx1 < listMultiplyChildren.size(); idx1 ++)	{
								boolean bNeedBrackets = false;
								if (idx1 > 0 && idx1 < listMultiplyChildren.size() - 1)	{
									bNeedBrackets = listMultiplyChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, 0);
								} else if (idx1 > 0 && idx1 == listMultiplyChildren.size() - 1)	{
									bNeedBrackets = listMultiplyChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, -1);
								} else if (idx1 == 0 && idx1 < listMultiplyChildren.size() - 1)	{
									bNeedBrackets = listMultiplyChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, 1);
								}
								if (bNeedBrackets && idx1 < listMultiplyChildren.size() - 1)	{	// need brackets and not the last multiply
									strOutput += "(" + convtAExpr2JQMath(listMultiplyChildren.get(idx1)) + ")";
								} else if (idx1 == listMultiplyChildren.size() - 1)	{	// last multiply, in different line, always add {}
									strOutput += "{" + convtAExpr2JQMath(listMultiplyChildren.get(idx1)) + "}";
								} else	{	// do not need () or {}
									strOutput += convtAExpr2JQMath(listMultiplyChildren.get(idx1));
								}
								if (idx1 < listMultiplyChildren.size() - 1)	{
									strOutput += "\u00D7";
								}
							}
							strOutput += "/";
							listMultiplyChildren.clear();
						}
						listDenominatorChildren.add(ae.mlistChildren.get(idx));
					}
				}
			}
			
			LinkedList<AbstractExpr> listRemainingChildren = new LinkedList<AbstractExpr>();
			boolean bRemainingDenominatorChildren = false;
			if (listMultiplyChildren.size() != 0)	{
				listRemainingChildren = listMultiplyChildren;
			} else	{
				listRemainingChildren = listDenominatorChildren;
				bRemainingDenominatorChildren = true;
			}
			if (bRemainingDenominatorChildren)	{
				strOutput += "{";
			}
			for (int idx1 = 0; idx1 < listRemainingChildren.size(); idx1 ++)	{
				boolean bNeedBrackets = false;
				if (idx1 > 0 && idx1 < listRemainingChildren.size() - 1)	{
					bNeedBrackets = listRemainingChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, 0);
				} else if (idx1 > 0 && idx1 == listRemainingChildren.size() - 1)	{
					bNeedBrackets = listRemainingChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, -1);
				} else if (idx1 == 0 && idx1 < listRemainingChildren.size() - 1)	{
					bNeedBrackets = listRemainingChildren.get(idx1).needBracketsWhenToStr(ae.menumAEType, 1);
				}
				if (bNeedBrackets)	{
					strOutput += "(" + convtAExpr2JQMath(listRemainingChildren.get(idx1)) + ")";
				} else	{
					strOutput += convtAExpr2JQMath(listRemainingChildren.get(idx1));
				}
				if (idx1 < listRemainingChildren.size() - 1)	{
					strOutput += "\u00D7";
				}
			}
			if (bRemainingDenominatorChildren)	{
				strOutput += "}";
			}
		} else if (aexpr instanceof AEOnOffUnaryOpt)	{
			AEOnOffUnaryOpt ae = (AEOnOffUnaryOpt) aexpr;
			if (ae.mnNumofOpts == 0)	{
				strOutput = ae.maexprChild.output();
			} else	{
	            boolean bNeedBracketsWhenToStr = false;
	            if (ae.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE)   {
	                bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, 1);
	            } else  {   // FALSE or NOT
	                bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, -1);
	            }

	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2JQMath(ae.maexprChild) + ")";
	            } else  {
	                strOutput = convtAExpr2JQMath(ae.maexprChild);
	            }
			}
			for (int idx = 0; idx < ae.mnNumofOpts; idx ++)	{
				if (ae.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE)	{	// non-bitwise not
					strOutput = "!" + strOutput;
				} else if (ae.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT)	{	// bitwise not
					strOutput = "~" + strOutput;
				} else	{	// transpose
					strOutput += "^T";
				}
			}
		} else if (aexpr instanceof AEPosNegOpt)	{
			AEPosNegOpt ae = (AEPosNegOpt) aexpr;
			if (ae.mlistChildren.size() == 1 && (ae.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
					|| ae.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN))	{
				// expression is +value
				strOutput = convtAExpr2JQMath(ae.mlistChildren.get(0));
			} else	{
				for (int idx = 0; idx < ae.mlistOpts.size(); idx ++)	{
	                boolean bLeftHasOpt = false, bRightHasOpt = false;
					if (ae.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
							|| ae.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)	{
						if (idx != 0)	{
							strOutput += "+";
	                        bLeftHasOpt = true;
						}
					} else	{
						strOutput += "-";
	                    bLeftHasOpt = true;
					}
	                if (idx < ae.mlistOpts.size() - 1) {
	                    bRightHasOpt = true;
	                }
	                
	                boolean bNeedBracketsWhenToStr = false;
	                if (bLeftHasOpt && bRightHasOpt)  {
	                    bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 0);
	                } else if (bLeftHasOpt && !bRightHasOpt)    {
	                    bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, -1);
	                } else if (!bLeftHasOpt && bRightHasOpt)    {
	                    bNeedBracketsWhenToStr = ae.mlistChildren.get(idx).needBracketsWhenToStr(ae.menumAEType, 1);
	                }
	                if (bNeedBracketsWhenToStr) {
	                    strOutput += "(" + convtAExpr2JQMath(ae.mlistChildren.get(idx)) + ")";
	                } else  {
	                    strOutput += convtAExpr2JQMath(ae.mlistChildren.get(idx));
	                }
				}
			}
		} else if (aexpr instanceof AEPowerOpt)	{
			// Note that because base and power to are in different lines, output of aePowerOpt is a bit different from the follows.
			AEPowerOpt ae = (AEPowerOpt) aexpr;
	        boolean bLeftNeedBracketsWhenToStr = false;
	        bLeftNeedBracketsWhenToStr = ae.maeLeft.needBracketsWhenToStr(ae.menumAEType, 1);
	        
	        if (bLeftNeedBracketsWhenToStr
	        		|| ae.maeLeft.menumAEType.getValue() > ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER.getValue()) {	// base and power_to are in different lines, have to do this. 
	            strOutput += "(" + convtAExpr2JQMath(ae.maeLeft) + ")";
	        } else  {
	            strOutput += convtAExpr2JQMath(ae.maeLeft);
	        }
	        strOutput += "^{" + convtAExpr2JQMath(ae.maeRight) + "}";
		} else if (aexpr instanceof AEUnaryOpt)	{
			AEUnaryOpt ae = (AEUnaryOpt) aexpr;
	        boolean bNeedBracketsWhenToStr = false;
			if (ae.getOpt().getLabelPrefix())	{
	            bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, -1);
	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2JQMath(ae.maexprChild) + ")";
	            } else  {
	                strOutput = convtAExpr2JQMath(ae.maexprChild);
	            }
				strOutput = (ae.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)?
						"!":"%" + strOutput;
			} else	{
	            bNeedBracketsWhenToStr = ae.maexprChild.needBracketsWhenToStr(ae.menumAEType, 1);
	            if (bNeedBracketsWhenToStr) {
	                strOutput = "(" + convtAExpr2JQMath(ae.maexprChild) + ")";
	            } else  {
	                strOutput = convtAExpr2JQMath(ae.maexprChild);
	            }
				strOutput += (ae.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)?
						"!":"%";
			}
		} else if (aexpr instanceof AEVar)	{
			if (((AEVar)aexpr).mstrVariableName.equalsIgnoreCase("pi"))	{
				strOutput = "\u03C0";
			} else if (((AEVar)aexpr).mstrVariableName.equalsIgnoreCase("inf")) {
				strOutput = "\u221E";
			} else if (((AEVar)aexpr).mstrVariableName.equalsIgnoreCase("infi")) {
				strOutput = "\u221E i";
			} else	{
				//strOutput = ((AEVar)aexpr).mstrVariableName.replace("_", "\\_");	// do not use this because the fond after _ will be italian.
				strOutput = "\\text\"" + ((AEVar)aexpr).mstrVariableName + "\"";	// don't worry about the special chars like ", ` because they cannot be a part of funcname.
			}
		}
		return strOutput;
	}
	
	/*
	 * Cannot handle strings with double quote like "hello " world", have to be written as "hello \" world"
	 */
	public static String convtPlainStr2JQMath(String str, boolean bSimplify) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		if (str.trim().length() == 0)	{
			return "";
		}
		AbstractExpr aexpr = ExprAnalyzer.analyseExpression(str, new CurPos());
		if (bSimplify)	{
			aexpr = aexpr.simplifyAExprMost(new LinkedList<UnknownVariable>(), new LinkedList<LinkedList<Variable>>(), new SimplifyParams(false, false, false));
		}
		return convtAExpr2JQMath(aexpr);
	}
	
	/*
	 * Cannot handle strings with double quote like "hello " world", have to be written as "hello \" world"
	 */
	public static String convtPlainStr2JQMathNoException(String str) 	{
		if (str.trim().length() == 0)	{
			return "";
		}
		return JQExprGenerator.cvtExpr2JQMath(str, new CurPos());
	}
	
	public static final String AEXPR_URL_HEADER = "AExpr://";
	
	public static String convtPlainStr2QuotedUrl(String str)	{
		// convert a string based expression to a quoted url to help user to copy the string into input
		String strReturn = AEXPR_URL_HEADER;
		try {
			strReturn += URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Cannot convert, do nothing
		}
		return strReturn;
	}
	
	public static String convtQuotedUrl2PlainStr(String str)	{
		// convert a quoted url to a string based expression to help user to copy the string into input
		String strReturn = str;
		if (str.length() >= AEXPR_URL_HEADER.length()
				&& str.substring(0, AEXPR_URL_HEADER.length()).equalsIgnoreCase(AEXPR_URL_HEADER))	{
			strReturn = str.substring(AEXPR_URL_HEADER.length());
			try {
				strReturn = URLDecoder.decode(strReturn, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// Cannot convert, do nothing
			}
		}
		
		return strReturn;
	}
	
	public static boolean isQuotedUrlExprStr(String str)	{
		if (str.length() >= AEXPR_URL_HEADER.length()
				&& str.substring(0, AEXPR_URL_HEADER.length()).equalsIgnoreCase(AEXPR_URL_HEADER))	{
			return true;
		} else	{
			return false;
		}
	}
}
