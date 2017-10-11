package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.BuiltinProcedures;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEMulDivOpt.CalcOptrWrap;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEPowerOpt extends AbstractExpr {

    // here mlistChildren and mlistOpts are not used because it leads to difficulty in merging.
    public AbstractExpr maeLeft = AEInvalid.AEINVALID, maeRight = AEInvalid.AEINVALID;
    
    public AEPowerOpt() {
        initAbstractExpr();
    }
    
    public AEPowerOpt(AbstractExpr aeLeft, AbstractExpr aeRight) throws JSmartMathErrException    {
        setAEPowerOpt(aeLeft, aeRight);
    }

    public AEPowerOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }
    
    @Override
    protected void initAbstractExpr() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER;
        maeLeft = AEInvalid.AEINVALID;
        maeRight = AEInvalid.AEINVALID;
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
    }

    private void setAEPowerOpt(AbstractExpr aeLeft, AbstractExpr aeRight) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER;
        maeLeft = aeLeft;
        maeRight = aeRight;
        validateAbstractExpr();
    }

    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEPowerOpt)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        maeLeft = ((AEPowerOpt)aexprOrigin).maeLeft;
        maeRight = ((AEPowerOpt)aexprOrigin).maeRight;
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEPowerOpt)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        maeLeft = ((AEPowerOpt)aexprOrigin).maeLeft.cloneSelf();
        maeRight = ((AEPowerOpt)aexprOrigin).maeRight.cloneSelf();
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEPowerOpt();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        // TODO: so far we haven't been able to support x**A where A is a matrix, so the dim of result is only determined by power base.
        return maeLeft.recalcAExprDim(bUnknownAsSingle); 
    }

    @Override
    public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (maeLeft.isEqual(((AEPowerOpt)aexpr).maeLeft) == false)    {
            return false;
        } else if (maeRight.isEqual(((AEPowerOpt)aexpr).maeRight) == false)    {
            return false;
        } else    {
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
        if (!(aePattern instanceof AEPowerOpt))   {
            return false;
        }
        if (maeLeft.isPatternMatch(((AEPowerOpt)aePattern).maeLeft,
                listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        if (maeRight.isPatternMatch(((AEPowerOpt)aePattern).maeRight,
                listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean isKnownValOrPseudo() {
        if (!maeLeft.isKnownValOrPseudo())    {
            return false;
        }
        if (!maeRight.isKnownValOrPseudo())    {
            return false;
        }
        return true;
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.add(maeLeft);
        listChildren.add(maeRight);
        return listChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);			
        }
        AEPowerOpt aeReturn = new AEPowerOpt();
        aeReturn.copy(this);
        aeReturn.maeLeft = listChildren.getFirst();
        aeReturn.maeRight = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
	
    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException    {
        AEPowerOpt aeReturn = new AEPowerOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeLeft.isEqual(aeFrom))    {
                aeReturn.maeLeft = aeTo;
                listReplacedChildren.add(aeReturn.maeLeft);
                break;
            }
        }
        
        for (int idx = 0; idx < listFromToMap.size(); idx ++)    {
            AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
            AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
            if (aeReturn.maeRight.isEqual(aeFrom))    {
                aeReturn.maeRight = aeTo;
                listReplacedChildren.add(aeReturn.maeRight);
                break;
            }
        }
        return aeReturn;
    }

    // assignment cannot be distributed.
    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        validateAbstractExpr();
        if (maeRight instanceof AEPosNegOpt && ((AEPosNegOpt)maeRight).mlistChildren.size() > 1)    {
            AEPosNegOpt aeAddSub = (AEPosNegOpt)maeRight;
            LinkedList<CalculateOperator> listDistributedCalcOpts = new LinkedList<CalculateOperator>();
            for (int idx = 0; idx < aeAddSub.mlistOpts.size(); idx ++)    {
                CalculateOperator calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                listDistributedCalcOpts.add(calcOpt);
            }
            LinkedList<AbstractExpr> listDistributedChildren = new LinkedList<AbstractExpr>();
            for (int idx = 0; idx < aeAddSub.mlistChildren.size(); idx ++)    {
                if (aeAddSub.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || aeAddSub.mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {    // +
                    // here aeInput.maeLeft needs clone itself while aeAddSub.mlistChildren.get(idx) needs
                    // not because aeInput.maeLeft will be used many times while aeAddSub.mlistChildren.get(idx)
                    // is only used once.
                    AEPowerOpt aeChild = new AEPowerOpt(maeLeft, aeAddSub.mlistChildren.get(idx));
                    listDistributedChildren.add(aeChild);
                } else    {    // -
                    // here aeInput.maeLeft needs clone itself while aeAddSub.mlistChildren.get(idx) needs
                    // not because aeInput.maeLeft will be used many times while aeAddSub.mlistChildren.get(idx)
                    // is only used once.
                    LinkedList<AbstractExpr> listChildrenSub = new LinkedList<AbstractExpr>();
                    listChildrenSub.add(aeAddSub.mlistChildren.get(idx));
                    LinkedList<CalculateOperator> listCalcOptsSub = new LinkedList<CalculateOperator>();
                    listCalcOptsSub.add(aeAddSub.mlistOpts.get(idx));
                    AEPosNegOpt aeChildPowerTo = new AEPosNegOpt(listChildrenSub, listCalcOptsSub);
                    AEPowerOpt aeChild = new AEPowerOpt(maeLeft, aeChildPowerTo);
                    listDistributedChildren.add(aeChild);
                }
            }
            AbstractExpr aeReturn = (AbstractExpr) new AEMulDivOpt(listDistributedChildren, listDistributedCalcOpts);
            return aeReturn;    // needs sort&merge after this function returns.            
        } else if (maeLeft instanceof AEMulDivOpt && maeRight instanceof AEConst
                && ((AEConst)maeRight).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)    {
            AEMulDivOpt aeMulDivBase = new AEMulDivOpt();
            aeMulDivBase.copy(maeLeft);
            // because maeLeft has been simplifiedmost before, so there should be at most one const on left and 
            // at most one const on right.
            boolean bLeftMostSimpleConst = false, bRightMostSimpleConst = false;
            if (aeMulDivBase.mlistChildren.getFirst() instanceof AEConst
                    && ((AEConst)aeMulDivBase.mlistChildren.getFirst()).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)    {
                // the left most is a constant but not an array.
                bLeftMostSimpleConst = true;
            } 
            if (aeMulDivBase.mlistChildren.size() > 1 && aeMulDivBase.mlistChildren.getLast() instanceof AEConst
                    && ((AEConst)aeMulDivBase.mlistChildren.getLast()).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)    {
                // the right most is a constant but not an array.
                bRightMostSimpleConst = true;
            }
            
            AbstractExpr aeNewLeftMost = AEInvalid.AEINVALID, aeNewRightMost = AEInvalid.AEINVALID, aeNewMiddle = AEInvalid.AEINVALID;
            if (bLeftMostSimpleConst)    {
                // we can call mergePower without worrying to throw exception because base and power to are both constant
                aeNewLeftMost = mergePower(aeMulDivBase.mlistChildren.getFirst(), maeRight, simplifyParams.mbIgnoreMatrixDim);
            }
            if (bRightMostSimpleConst)    {
                // we can call mergePower without worrying to throw exception because base and power to are both constant
                aeNewRightMost = mergePower(aeMulDivBase.mlistChildren.getLast(), maeRight, simplifyParams.mbIgnoreMatrixDim);
            }
            AEConst aeOne = new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE));
            if (bLeftMostSimpleConst == false && bRightMostSimpleConst == false)    {
                return this;
            } else {
                AbstractExpr aeConstPart;
                CalculateOperator co;
                if (bLeftMostSimpleConst && bRightMostSimpleConst == false)    {
                    co = aeMulDivBase.mlistOpts.removeFirst();
                    aeMulDivBase.mlistChildren.removeFirst();
                    aeConstPart = aeNewLeftMost;
                } else if (bLeftMostSimpleConst == false && bRightMostSimpleConst)    {
                    co = aeMulDivBase.mlistOpts.removeLast();
                    aeMulDivBase.mlistChildren.removeLast();
                    aeConstPart = aeNewRightMost;
                } else    {
                    CalculateOperator coFirst = aeMulDivBase.mlistOpts.removeFirst();
                    CalculateOperator coLast = aeMulDivBase.mlistOpts.removeLast();
                    aeMulDivBase.mlistChildren.removeFirst();
                    aeMulDivBase.mlistChildren.removeLast();
                    CalcOptrWrap cow = new CalcOptrWrap();
                    cow.mCalcOptr = coFirst;
                    aeConstPart = AEMulDivOpt.mergeMultiplyDiv(aeNewLeftMost, aeNewRightMost,
                            coFirst.getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY,
                            coLast.getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY, cow, simplifyParams);
                    co = cow.mCalcOptr;
                }
                if (aeMulDivBase.mlistChildren.size() == 0)    {
                    if (co.getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)    {
                        return aeConstPart;
                    } else    {    // divide
                        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                        listChildren.add(aeOne);
                        listChildren.add(aeConstPart);
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                        return new AEMulDivOpt(listChildren, listOpts);
                    }
                } else if (aeMulDivBase.mlistChildren.size() == 1)    {
                    aeNewMiddle = new AEPowerOpt(aeMulDivBase.mlistChildren.getFirst(), maeRight);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(co);
                    listOpts.add(aeMulDivBase.mlistOpts.getFirst());
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeConstPart);    // coz aeConstPart is a simple 
                    listChildren.add(aeNewMiddle);
                    return new AEMulDivOpt(listChildren, listOpts);
                } else    {    //aeMulDivBase.mlistChildren.size() > 1
                    aeNewMiddle = new AEPowerOpt(aeMulDivBase, maeRight);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(co);
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeConstPart);
                    listChildren.add(aeNewMiddle);
                    return new AEMulDivOpt(listChildren, listOpts);
                }
            }
        } else if (maeLeft instanceof AEPosNegOpt && ((AEPosNegOpt)maeLeft).mlistChildren.size() == 1
                && maeRight instanceof AEConst)    {
            if (((AEPosNegOpt)maeLeft).mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                    || ((AEPosNegOpt)maeLeft).mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                return new AEPowerOpt(((AEPosNegOpt)maeLeft).mlistChildren.get(0), maeRight);
            } else    {    // negative or minus
                AEConst aeMinusOne = new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE));
                AbstractExpr aeConstPart = new AEPowerOpt(aeMinusOne, maeRight);
                AbstractExpr aeRemainPart = new AEPowerOpt(((AEPosNegOpt)maeLeft).mlistChildren.get(0), maeRight);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeConstPart);
                listChildren.add(aeRemainPart);
                return new AEMulDivOpt(listChildren, listOpts);
            }
        }
        return this;    // cannot distribute.
    }

    // avoid to do any overhead work.
	@Override
	public DataClass evaluateAExprQuick(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumLeft = maeLeft.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
		DataClass datumRight = maeRight.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
        DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, new CalculateOperator(OPERATORTYPES.OPERATOR_POWER, 2), datumRight);
        return datum;        
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeLeft = maeLeft.evaluateAExpr(lUnknownVars, lVarNameSpaces);
		AbstractExpr aeRight = maeRight.evaluateAExpr(lUnknownVars, lVarNameSpaces);
        if (aeLeft instanceof AEConst && aeRight instanceof AEConst) {
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(((AEConst)aeLeft).getDataClassRef(), new CalculateOperator(OPERATORTYPES.OPERATOR_POWER, 2), ((AEConst)aeRight).getDataClassRef());
            return new AEConst(datum);        
        } else {
            return new AEPowerOpt(aeLeft, aeRight);
        }
    }

    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
            LinkedList<LinkedList<Variable>> lVarNameSpaces, SimplifyParams simplifyParams)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        
        AbstractExpr aeLeft = maeLeft.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
        AbstractExpr aeRight = maeRight.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
        
        AbstractExpr aeReturn = new AEPowerOpt(aeLeft, aeRight);
        try    {
            // have to clone the parameters coz they may be changed inside.
            aeReturn = mergePower(aeLeft, aeRight, simplifyParams.mbIgnoreMatrixDim);
        } catch (JSmartMathErrException e)    {
            if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                throw e;
            }
        }
        
        aeReturn = aeReturn.distributeAExpr(simplifyParams);
        return aeReturn;
    }

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if ((enumAET.getValue() > menumAEType.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                || (enumAET.getValue() == menumAEType.getValue() && nLeftOrRight <= 0))    {
            return true;
        }
        return false;
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {
            int nReturn = maeLeft.compareAExpr(((AEPowerOpt)aexpr).maeLeft);
            if (nReturn == 0)    {
                nReturn = maeRight.compareAExpr(((AEPowerOpt)aexpr).maeRight);
            }
            return nReturn;
        }
    }
    
    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        return maeLeft.isNegligible();
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
        bLeftNeedBracketsWhenToStr = maeLeft.needBracketsWhenToStr(menumAEType, 1);
        bRightNeedBracketsWhenToStr = maeRight.needBracketsWhenToStr(menumAEType, -1);
        
        String strOutput = "";
        if (bLeftNeedBracketsWhenToStr) {
            strOutput += "(" + maeLeft.output() + ")";
        } else  {
            strOutput += maeLeft.output();
        }
        strOutput += "**";
        if (bRightNeedBracketsWhenToStr)    {
            strOutput += "(" + maeRight.output() + ")";
        } else  {
            strOutput += maeRight.output();
        }
        return strOutput;
    }
        
    // note that parameters of this function will be changed inside.
    public static AbstractExpr mergePower(AbstractExpr aeBase, AbstractExpr aePowerTo, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aePowerTo instanceof AEConst && ((AEConst)aePowerTo).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE)))    {
            // power to 1. At this moment, we assume power base is valid to accelerate calculation.
            return aeBase;
        } else if (aePowerTo instanceof AEConst && ((AEConst)aePowerTo).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO))) {
            // anything power to 0 is 1.
            // have to validate power base dim here because this determines return is Eye or 1
            int[] ndimarray = new int[0];
            try {
                ndimarray = aeBase.recalcAExprDim(bIgnoreMatrixDim);    // if it is a variable, then the dim size cannot be calculated.
            } catch (JSmartMathErrException e)    {
                if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)    {
                    throw e;
                }
            }
            boolean bIsValidPowerBase = true;
            if (ndimarray.length != 0) {
                for (int idx = 0; idx < ndimarray.length; idx ++)   {
                    if (ndimarray[idx] != ndimarray[0]) {
                        bIsValidPowerBase = false;
                        break;
                    }
                }
            }
            DataClass datum = null;
            if (!bIsValidPowerBase) {
                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
            } else if (ndimarray.length == 0 || (ndimarray.length == 1 && ndimarray[0] == 0))  {
                datum = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE);
            } else {
                datum = BuiltinProcedures.createEyeMatrix(ndimarray[0], ndimarray.length);
            }
            AEConst aexprReturn = new AEConst(datum);
            return aexprReturn;
        } else if (aeBase instanceof AEConst && aePowerTo instanceof AEConst)    {
            // both aeInput1 and aeInput2 are constants.
            DataClass datumBase = ((AEConst)aeBase).getDataClassRef(),
            datumPowerTo = ((AEConst)aePowerTo).getDataClassRef();

            CalculateOperator calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_POWER, 2);
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumBase, calcOpt, datumPowerTo);
            AEConst aexprReturn = new AEConst(datum);
            return aexprReturn;
        } else if (aeBase instanceof AEPowerOpt && aePowerTo instanceof AEConst)    {
            // convert a ** b ** c to a ** (b * c) (which is an express degrading)
            if (((AEPowerOpt)aeBase).maeRight instanceof AEMulDivOpt)    {
                AEMulDivOpt aeNewPowerTo = (AEMulDivOpt) ((AEPowerOpt)aeBase).maeRight;
                aeNewPowerTo.mlistChildren.add(aePowerTo);
                aeNewPowerTo.mlistOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                return new AEPowerOpt(((AEPowerOpt)aeBase).maeLeft, aeNewPowerTo);
            } else    {
                LinkedList<AbstractExpr> listPowerToChildren = new LinkedList<AbstractExpr>();
                LinkedList<CalculateOperator> listPowerToOpts = new LinkedList<CalculateOperator>();
                listPowerToChildren.add(((AEPowerOpt)aeBase).maeRight);
                listPowerToChildren.add(aePowerTo);
                listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                return new AEPowerOpt(((AEPowerOpt)aeBase).maeLeft, new AEMulDivOpt(listPowerToChildren, listPowerToOpts));
            }
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeLeft = (maeLeft instanceof AEConst)?maeLeft:maeLeft.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeRight = (maeRight instanceof AEConst)?maeRight:maeRight.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEPowerOpt(aeLeft, aeRight);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeLeft = maeLeft, aeRight = maeLeft;
        if (maeLeft instanceof AEConst
                && ((AEConst)maeLeft).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeLeft = ((AEConst)maeLeft).getDataClassRef().getAExpr();
        }
        if (maeRight instanceof AEConst
                && ((AEConst)maeRight).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeRight = ((AEConst)maeRight).getDataClassRef().getAExpr();
        }
        return new AEPowerOpt(aeLeft, aeRight);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeLeft.getVarAppearanceCnt(strVarName);
        nCnt += maeRight.getVarAppearanceCnt(strVarName);
        return nCnt;
    }
}
