package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEUnaryOpt extends AbstractExpr {

	public CalculateOperator getOpt() throws JSmartMathErrException	{
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)	{
			return new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false);
		} else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)	{
			return new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false);
		} else	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
	}
	
	public OPERATORTYPES getOptType() throws JSmartMathErrException	{
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)	{
			return OPERATORTYPES.OPERATOR_FACTORIAL;
		} else if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)	{
			return OPERATORTYPES.OPERATOR_PERCENT;
		} else	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
	}
	
	public AbstractExpr maexprChild = AEInvalid.AEINVALID;

	public AEUnaryOpt() {
		initAbstractExpr();
	}
	
	public AEUnaryOpt(ABSTRACTEXPRTYPES aeType, AbstractExpr aexprChild) throws JSmartMathErrException	{
		setAEUnaryOpt(aeType, aexprChild);
	}

	public AEUnaryOpt(CalculateOperator calcOpt, AbstractExpr aexprChild) throws JSmartMathErrException	{
		if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_FACTORIAL)	{
			setAEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL, aexprChild);
		} else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_PERCENT)	{
			setAEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aexprChild);
		} else	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
	}

	public AEUnaryOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL;
		maexprChild = AEInvalid.AEINVALID;
	}

	private void setAEUnaryOpt(ABSTRACTEXPRTYPES aeType, AbstractExpr aexprChild) throws JSmartMathErrException	{
		menumAEType = aeType;
		maexprChild = aexprChild;
		validateAbstractExpr();
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException	{
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL
				&& menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}
	}
	
	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEUnaryOpt)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		maexprChild = ((AEUnaryOpt)aexprOrigin).maexprChild;
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEUnaryOpt)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		maexprChild = ((AEUnaryOpt)aexprOrigin).maexprChild.cloneSelf();
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException,
			JSmartMathErrException {
		AbstractExpr aeReturn = new AEUnaryOpt();
		aeReturn.copyDeep(this);
		return aeReturn;
	}

	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();
		return new int[0];
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (maexprChild.isEqual(((AEUnaryOpt)aexpr).maexprChild) == false)	{
			return false;
		} else	{
			return true;
		}
	}

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        if (aePattern.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE)   {
            // unknown variable
            for (int idx = 0; idx < listpeuMapUnknowns.size(); idx ++)  {
                if (listpeuMapUnknowns.get(idx).maePatternUnit.isEqual(aePattern))    {
                    if (isEqual(listpeuMapUnknowns.get(idx).maeExprUnit))   {
                        // this unknown variable has been mapped to an expression and the expression is the same as this
                        return true;
                    } else  {
                        // this unknown variable has been mapped to an expression but the expression is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern is an unknown variable and it hasn't been mapped to some expressions before.
            PatternExprUnitMap peuMap = new PatternExprUnitMap(this, aePattern);
            listpeuMapUnknowns.add(peuMap);
            return true;
        }
        if (!(aePattern instanceof AEUnaryOpt))   {
            return false;
        }
        if (getOptType() != ((AEUnaryOpt)aePattern).getOptType())  {
            return false;
        }
        if (maexprChild.isPatternMatch(((AEUnaryOpt)aePattern).maexprChild,
                listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        
        return true;
    }
    
	@Override
	public boolean isKnownValOrPseudo() {
		return maexprChild.isKnownValOrPseudo();
	}
	
	// note that the return list should not be changed.
	@Override
	public LinkedList<AbstractExpr> getListOfChildren()	{
		LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
		listChildren.add(maexprChild);
		return listChildren;
	}
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 1) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);			
        }
        AEUnaryOpt aeReturn = new AEUnaryOpt();
        aeReturn.copy(this);
        aeReturn.maexprChild = listChildren.getFirst();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }

	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException    {
        AEUnaryOpt aeReturn = new AEUnaryOpt();
        aeReturn.copy(this);
		for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maexprChild.isEqual(aeFrom))	{
				aeReturn.maexprChild = aeTo;	// need to clone because will be many aeTo copies. But to keep performance, dont clone.
				listReplacedChildren.add(aeReturn.maexprChild);
				break;
			}
		}
		return aeReturn;
	}

	// assignment cannot be distributed.
	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException	{
		validateAbstractExpr();
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT)	{
            AEUnaryOpt aeReturn = this;
			if (aeReturn.maexprChild instanceof AEPosNegOpt)	{
                AEPosNegOpt aeReturnChild = new AEPosNegOpt();
                aeReturnChild.copy((AEPosNegOpt)aeReturn.maexprChild);
				for (int idx = 0; idx < aeReturnChild.mlistChildren.size(); idx ++)	{
					AbstractExpr aeSubChild = aeReturnChild.mlistChildren.get(idx);
					aeReturnChild.mlistChildren.set(idx, new AEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aeSubChild));
				}
                aeReturn = new AEUnaryOpt(aeReturnChild);
			} else if (aeReturn.maexprChild instanceof AEMulDivOpt)	{
                AEMulDivOpt aeReturnChild = new AEMulDivOpt();
                aeReturnChild.copy((AEMulDivOpt)aeReturn.maexprChild);
				aeReturnChild.mlistOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                aeReturnChild.mlistChildren.addFirst(new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.01"))));
                aeReturn = new AEUnaryOpt(aeReturnChild);
			} else if (maexprChild instanceof AELeftDivOpt)	{
                AELeftDivOpt aeReturnChild = new AELeftDivOpt();
                aeReturnChild.copy((AELeftDivOpt)aeReturn.maexprChild);
				AbstractExpr aeRightChild = aeReturnChild.maeRight;
				aeReturnChild.maeRight = new AEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aeRightChild);
                aeReturn = new AEUnaryOpt(aeReturnChild);
			}
            return aeReturn;
		}
		return this;
	}

    // avoid to do any overhead work.
	@Override
	public DataClass evaluateAExprQuick(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumChild = maexprChild.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
        DataClass datumValue = new DataClass();
        datumValue.copyTypeValueDeep(datumChild);
        if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)	{
            datumValue.changeDataType(DATATYPES.DATUM_INTEGER);
            datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false));
        } else {	// percentage
            datumValue.changeDataType(DATATYPES.DATUM_DOUBLE);
            datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false));
        }

        return datumValue;        
    }
    
    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeChild = maexprChild.evaluateAExpr(lUnknownVars, lVarNameSpaces);
        if (aeChild instanceof AEConst) {
            DataClass datumValue = new DataClass();
            datumValue.copyTypeValueDeep(((AEConst)aeChild).getDataClassRef());
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)	{
                datumValue.changeDataType(DATATYPES.DATUM_INTEGER);
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false));
            } else {	// percentage
                datumValue.changeDataType(DATATYPES.DATUM_DOUBLE);
                datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false));
            }

            return new AEConst(datumValue);
        } else {
            return new AEUnaryOpt(menumAEType, aeChild);
        }
    }
    
	@Override
	public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces, SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();
		AbstractExpr aexprChild = maexprChild.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		// we dont care the datavalue changed because it will be changed anyway.
		// if unsuccessful, it is an error.
		if (aexprChild instanceof AEConst)	{
			DataClass datumValue = ((AEConst)aexprChild).getDataClassCopy();
			if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)	{
				datumValue.changeDataType(DATATYPES.DATUM_INTEGER);
				datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL, 1, false));
			} else {	// percentage
				datumValue.changeDataType(DATATYPES.DATUM_DOUBLE);
				datumValue = ExprEvaluator.evaluateOneOperandCell(datumValue, new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false));
			}
			return new AEConst(datumValue);
		} else	{
            if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT) {
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listChildren.add(aexprChild);
                listChildren.add(new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(100))));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                AbstractExpr aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn.distributeAExpr(simplifyParams);
            } else  {
                return distributeAExpr(simplifyParams);
            }
		}
	}
	
    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        return false;
    }
    
	@Override
	public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
		if (menumAEType.getValue() < aexpr.menumAEType.getValue())	{
			return 1;
		} else if (menumAEType.getValue() > aexpr.menumAEType.getValue())	{
			return -1;
		} else	{
			return maexprChild.compareAExpr(((AEUnaryOpt)aexpr).maexprChild);
		}
	}

	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException	{
		validateAbstractExpr();
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL)	{
			return maexprChild.isNegligible();
		} else	{	// menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT
			return maexprChild.isNegligible();
		}
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
        boolean bNeedBracketsWhenToStr = false;
        String strOutput = "";
		if (getOpt().getLabelPrefix())	{
            bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, -1);
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.output() + ")";
            } else  {
                strOutput = maexprChild.output();
            }
			strOutput = getOptType().output() + strOutput;
		} else	{
            bNeedBracketsWhenToStr = maexprChild.needBracketsWhenToStr(menumAEType, 1);
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + maexprChild.output() + ")";
            } else  {
                strOutput = maexprChild.output();
            }
			strOutput += getOptType().output();
		}
		return strOutput;
	}

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeChild = (maexprChild instanceof AEConst)?maexprChild:maexprChild.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEUnaryOpt(menumAEType, aeChild);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeChild = maexprChild;
        if (maexprChild instanceof AEConst
                && ((AEConst)maexprChild).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeChild = ((AEConst)maexprChild).getDataClassRef().getAExpr();
        }
        return new AEUnaryOpt(menumAEType, aeChild);
    }
    
    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maexprChild.getVarAppearanceCnt(strVarName);
        return nCnt;
    }
}
