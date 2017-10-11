package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.BuiltinProcedures;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEPosNegOpt extends AbstractExpr {

    // AEPosNegOpt can be either plus/minus operator or positive/negative sign
    public LinkedList<CalculateOperator> mlistOpts = new LinkedList<CalculateOperator>();
    public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>();
    
    public AEPosNegOpt() {
        initAbstractExpr();
    }
    
    public AEPosNegOpt(LinkedList<AbstractExpr> listChildren, LinkedList<CalculateOperator> listOpts) throws JSmartMathErrException    {
        setAEPosNegOpt(listChildren, listOpts);
    }

    public AEPosNegOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    protected void initAbstractExpr() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG;
        mlistChildren =  new LinkedList<AbstractExpr>();
        mlistOpts = new LinkedList<CalculateOperator>();
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
        
        for (int idx = 0; idx < mlistOpts.size(); idx ++)    {
            if (mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_ADD
                    && mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_SUBTRACT
                    && mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_POSSIGN
                    && mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_NEGSIGN)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
            }
        }
        if (mlistChildren.size() < 1 || mlistOpts.size() != mlistChildren.size())    { // like - 7 is a valid AEPosNegOpt
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
    }

    private void setAEPosNegOpt(LinkedList<AbstractExpr> listChildren, LinkedList<CalculateOperator> listOpts) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG;
        mlistChildren = listChildren;
        mlistOpts = listOpts;
        validateAbstractExpr();
    }

    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEPosNegOpt)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        mlistChildren = new LinkedList<AbstractExpr>();
        mlistChildren.addAll(((AEPosNegOpt)aexprOrigin).mlistChildren);
        mlistOpts = new LinkedList<CalculateOperator>();
        mlistOpts.addAll(((AEPosNegOpt)aexprOrigin).mlistOpts);
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEPosNegOpt)aexprOrigin).validateAbstractExpr();

        super.copyDeep(aexprOrigin);
        mlistChildren = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < ((AEPosNegOpt)aexprOrigin).mlistChildren.size(); idx ++)    {
            mlistChildren.add(((AEPosNegOpt)aexprOrigin).mlistChildren.get(idx).cloneSelf());
        }
        mlistOpts = new LinkedList<CalculateOperator>();
        for (int idx = 0; idx < ((AEPosNegOpt)aexprOrigin).mlistOpts.size(); idx ++)    {
            mlistOpts.add(new CalculateOperator(((AEPosNegOpt)aexprOrigin).mlistOpts.get(idx).getOperatorType(),
                    ((AEPosNegOpt)aexprOrigin).mlistOpts.get(idx).getOperandNum(), true));    // Note that binary opt's prefix flag is always true.
        }
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEPosNegOpt();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();

        int[] narrayDim = new int[0];
        for (int idx = 0; idx < mlistChildren.size() - 1; idx ++)    {
            try {
                narrayDim = mlistChildren.get(idx).recalcAExprDim(false);   // try to get accurate dim first
                return narrayDim;
            } catch (JSmartMathErrException e)    {
                if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)    {
                    continue;
                }
                throw e;
            }
        }
        // if no way to get accurate dim, then look on unknown as single if bUnknownAsSingle is flagged.
        return mlistChildren.get(mlistChildren.size() - 1).recalcAExprDim(bUnknownAsSingle); 
    }

    @Override
    public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (mlistChildren.size() != ((AEPosNegOpt)aexpr).mlistChildren.size())    {
            return false;
        } else if (mlistOpts.size() != ((AEPosNegOpt)aexpr).mlistOpts.size())    {
            return false;
        } else    {
            for (int idx = 0; idx < mlistOpts.size(); idx ++)    {
                boolean bHostAdd = false, bGuestAdd = false;
                if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                    bHostAdd = true;
                }
                if (((AEPosNegOpt)aexpr).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || ((AEPosNegOpt)aexpr).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                    bGuestAdd = true;
                }
                
                if (bHostAdd != bGuestAdd)    {
                    return false;
                }
            }
            for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
                if (mlistChildren.get(idx).isEqual(((AEPosNegOpt)aexpr).mlistChildren.get(idx)) == false)    {
                    return false;
                }
            }
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
        if (!(aePattern instanceof AEPosNegOpt))   {
            return false;
        }
        if (mlistOpts.size() != ((AEPosNegOpt)aePattern).mlistOpts.size())  {
            return false;
        }
        if (mlistChildren.size() != ((AEPosNegOpt)aePattern).mlistChildren.size())  {
            return false;
        }
        LinkedList<AbstractExpr> listConvertedChildren = new LinkedList<AbstractExpr>();
        LinkedList<Boolean> listNeged = new LinkedList<Boolean>();
        for (int idx = 0; idx < mlistOpts.size(); idx ++)   {
            OPERATORTYPES enumExprOptType = mlistOpts.get(idx).getOperatorType();
            Boolean bExprAdd = (enumExprOptType == OPERATORTYPES.OPERATOR_ADD || enumExprOptType == OPERATORTYPES.OPERATOR_POSSIGN);
            OPERATORTYPES enumPatternOptType = ((AEPosNegOpt)aePattern).mlistOpts.get(idx).getOperatorType();
            Boolean bPatternAdd = (enumPatternOptType == OPERATORTYPES.OPERATOR_ADD || enumPatternOptType == OPERATORTYPES.OPERATOR_POSSIGN);
            if (bExprAdd == bPatternAdd) {  // both are + or -
                listConvertedChildren.add(mlistChildren.get(idx));
                listNeged.add(false);
            } else if (bAllowConversion) {    // one is + the other is -, but we allow conversion here.
                // this function is able to handle like x+3 matches pattern x-a. But it cannot handle degraded expression case (e.g. x+1 match a*x + b).
                AbstractExpr aeThisChild = mlistChildren.get(idx);  // no need to clone here because may do it later.
                AEConst aeMinus1 = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.MINUS_ONE));
                if (aeThisChild instanceof AEMulDivOpt) {
                    aeThisChild = new AEMulDivOpt();
                    aeThisChild.copy(mlistChildren.get(idx));
                    boolean bMultiplyMinus1ToConst = false;
                    for (int idx1 = 0; idx1 < ((AEMulDivOpt)aeThisChild).mlistChildren.size(); idx1 ++) {
                        AbstractExpr aeThisGrandChild = ((AEMulDivOpt)aeThisChild).mlistChildren.get(idx1);
                        if (aeThisGrandChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                            || aeThisGrandChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE) {
                            bMultiplyMinus1ToConst = true;
                            DataClass datum = ((AEConst) aeThisGrandChild).getDataClassRef();
                            try {
                                datum = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true), datum);
                                            //BuiltinProcedures.evaluateNegSign(datum); // cannot use this because it does not handle aexpr datum
                            } catch (JFCALCExpErrException e) {
                                // cannot calculate like -"ABC"
                                return false;
                            }
                            ((AEMulDivOpt)aeThisChild).mlistChildren.set(idx1, new AEConst(datum));
                            break;
                        }
                    }
                    if (!bMultiplyMinus1ToConst) {
                        // need not to reorder or simplify here because const is always first.
                        ((AEMulDivOpt)aeThisChild).mlistOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        ((AEMulDivOpt)aeThisChild).mlistChildren.addFirst(aeMinus1);
                    }
                } else if (aeThisChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                        || aeThisChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE) {
                    DataClass datum = ((AEConst) aeThisChild).getDataClassRef();
                    try {
                        datum = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true), datum);
                                            //BuiltinProcedures.evaluateNegSign(datum); // cannot use this because it does not handle aexpr datum
                    } catch (JFCALCExpErrException e) {
                        // cannot calculate like -"ABC"
                        return false;
                    }
                    aeThisChild = new AEConst(datum);
                } else {
                    //need not to simplify most because muldiv or const has been considered above, we cannot simply others.
                    LinkedList<AbstractExpr> listAEs = new LinkedList<AbstractExpr>();
                    listAEs.add(aeMinus1);
                    listAEs.add(aeThisChild);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    aeThisChild = new AEMulDivOpt(listAEs, listOpts);
                }
                listConvertedChildren.add(aeThisChild);
                listNeged.add(true);
            } else {    // one is + the other is -, and we do not allow conversion here.
                return false;
            }
        }
        for (int idx = 0; idx < listConvertedChildren.size(); idx ++)   {
            if (listConvertedChildren.get(idx).isPatternMatch(((AEPosNegOpt)aePattern).mlistChildren.get(idx),
                listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false)  {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isKnownValOrPseudo() {
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            if (!mlistChildren.get(idx).isKnownValOrPseudo())    {
                return false;
            }
        }
        return true;
    }
    
    // note that the return list should not be changed.
    @Override
    public LinkedList<AbstractExpr> getListOfChildren()    {
        return mlistChildren;
    }
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        AEPosNegOpt aeReturn = new AEPosNegOpt();
        aeReturn.copy(this);
        aeReturn.mlistChildren = listChildren == null?new LinkedList<AbstractExpr>():listChildren;
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }

    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException    {
        AEPosNegOpt aeReturn = new AEPosNegOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)    {
            for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)    {
                if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);    // need to clone because will be many aeTo copies. However, to save time dont clone.
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                } else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);    // need to clone because will be many aeTo copies. However, to save time dont clone.
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                }
            }
        }
        return aeReturn;
    }

    // + and - cannot be distributed.
    @Override
    public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException    {
        validateAbstractExpr();
        return this;
    }

    // avoid to do any overhead work.
	@Override
	public DataClass evaluateAExprQuick(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumLast = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
        for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			DataClass datum = mlistChildren.get(idx).evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
            if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                    || mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN) {
                datumLast = ExprEvaluator.evaluateTwoOperandCell(datumLast, new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2), datum);
            } else {
                datumLast = ExprEvaluator.evaluateTwoOperandCell(datumLast, new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2), datum);
            }
		}

        return datumLast;        
    }
    
    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        LinkedList<CalculateOperator> listNewOpts = new LinkedList<CalculateOperator>();
        DataClass datumZero = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
        for (int idx0 = 0; idx0 < mlistChildren.size(); idx0 ++)	{
            DataClass datumLast = datumZero;
            int idx = idx0;
            AbstractExpr aexpr2Add = null;
            for (; idx < mlistChildren.size(); idx ++)	{
                AbstractExpr aexpr = mlistChildren.get(idx).evaluateAExpr(lUnknownVars, lVarNameSpaces);
                if (aexpr instanceof AEConst) {
                    if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                            || mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN) {
                        datumLast = ExprEvaluator.evaluateTwoOperandCell(datumLast, new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2), ((AEConst)aexpr).getDataClassRef());
                    } else {
                        datumLast = ExprEvaluator.evaluateTwoOperandCell(datumLast, new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2), ((AEConst)aexpr).getDataClassRef());
                    }
                } else {
                    aexpr2Add = aexpr;
                    break;
                }
            }
            if (!datumLast.isEqual(datumZero)) {
                listNewOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
                listNewChildren.add(new AEConst(datumLast));
            }
            if (aexpr2Add != null) {
                if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                            || mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN) {
                    listNewOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
                } else {
                    listNewOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                }
                listNewChildren.add(aexpr2Add);
            }
            idx0 = idx;
        }
        if (listNewChildren.size() == 0) {
            // this means returns zero
            return new AEConst(datumZero);
        }
        if (listNewOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                || listNewOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN) {
            listNewOpts.set(0, new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
        } else {
            listNewOpts.set(0, new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true));
        }
        if (listNewChildren.size() == 1) {
            if (listNewOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN) {
                // if there is only one child and it is negsign, the child cannot be aeConst.
                return new AEPosNegOpt(listNewChildren, listNewOpts);
            } else {
                return listNewChildren.getFirst();
            }
        } else {
            return new AEPosNegOpt(listNewChildren, listNewOpts);
        }
    }
    
    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
            LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();

        AEPosNegOpt aeCopy = new AEPosNegOpt();
        aeCopy.copy(this);
        for (int idx = 0; idx < aeCopy.mlistChildren.size(); idx ++)    {
            AbstractExpr aexpr = aeCopy.mlistChildren.get(idx).simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
            aeCopy.mlistChildren.set(idx, aexpr);
        }
        
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        
        // sort the children.
        // TODO: may not properly handle string, e.g. int10To16(x) + i is sorted to i + int10To16(x)
        // however, this might be acceptable since x is still unknown.
        boolean bB4String = true;
        for (int idx = 0; idx < aeCopy.mlistChildren.size(); idx ++)    {
            AbstractExpr aeCurrent = aeCopy.mlistChildren.get(idx);
            if (aeCurrent instanceof AEConst
                    && ((AEConst)aeCurrent).getDataClassRef().getDataType() == DATATYPES.DATUM_STRING)    {
                bB4String = false;
            }
            CalculateOperator calcOpt = aeCopy.mlistOpts.get(idx);
            int idx1 = listChildren.size();
            if (bB4String)  {   // we can reorder only if the children are before string
                for (idx1 = 0; idx1 < listChildren.size(); idx1 ++)    {
                    if (aeCurrent.compareAExpr(listChildren.get(idx1)) > 0)    {    // means aeCurrent should be left of listChildren.get(idx1)
                        // insert
                        listChildren.add(idx1, aeCurrent);
                        if (idx1 == 0)    {
                            if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_ADD)    {
                                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true);
                            } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT)    {
                                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true);
                            }
                        } else    {
                            if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                            } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)    {
                                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                            }
                        }
                        listOpts.add(idx1, calcOpt);
                        break;
                    }
                }
            }
            if (idx1 == listChildren.size())    {
                // append
                listChildren.add(aeCurrent);
                if (idx1 == 0)    {
                    if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_ADD)    {
                        calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true);
                    } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT)    {
                        calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true);
                    }
                } else    {
                    if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                        calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                    } else if (calcOpt.getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)    {
                        calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                    }
                }
                listOpts.add(calcOpt);
            }
        }
        
        for (int idx = 0; idx < listChildren.size() - 1; idx ++)    {
            // different from multiply division, a + b +c can jump off b and merge a and c
            // consider a situation 3*x**2 + 4*x**3 + x**2. The children have been sorted.
            // However, x**2 must be after 4*x**3 because x**2 is AEPowerOpt while 4 * x**3
            // is AEMulDivOpt. And 3*x**2 must be before 4*x**3. Thus, we have to skip 4*x**3
            // and merge 3*x**2 and x**2. This is different from multiply division because
            // children of AEMulDivOpt cannot be easily sorted.
            for (int idx1 = idx + 1; idx1 < listChildren.size(); idx1 ++)    {
                boolean bIsAdd = true;
                boolean bIsLeftAdd = false;
                if (listOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || listOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                    bIsLeftAdd = true;
                }
                boolean bIsRightAdd = false;
                if (listOpts.get(idx1).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || listOpts.get(idx1).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                    bIsRightAdd = true;
                }

                if (bIsLeftAdd != bIsRightAdd )    {
                    bIsAdd = false;
                }
                try    {
                    // we need cloned children as parameters for mergeAddSub because function 
                    // mergeAddSub may change content of its parameter. If mergeAddSub fails in the middle
                    // (throw cannot merge exception), then we have to revert the change back which is
                    // very difficult. So use copied parameters here.
                    AbstractExpr aexpr = mergeAddSub(listChildren.get(idx), listChildren.get(idx1), bIsAdd, simplifyParams.mbIgnoreMatrixDim);
                    listChildren.remove(idx1);
                    listOpts.remove(idx1);
                    listChildren.set(idx, aexpr);
                    idx --;
                    break;
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                        // cannot merge two children at this moment, so continue.
                        continue;
                    }
                }
            }
        }
        
        if (listChildren.size() == 1
                && (listOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || listOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN))    {
            // expression is +value
            return listChildren.get(0).distributeAExpr(simplifyParams);
        } else if (listChildren.size() == 1 
                && (listOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT
                        || listOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN))    {
            // expression is -value
            AbstractExpr aexprReturn = mergeNegSignIntoSingleChild(listChildren.get(0));
            return aexprReturn.distributeAExpr(simplifyParams);
        } else    {
            AEPosNegOpt aexprReturn = new AEPosNegOpt(listChildren, listOpts);
            return aexprReturn.distributeAExpr(simplifyParams);
        }
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
            int nChildrenListSize1 = mlistChildren.size();
            int nChildrenListSize2 = ((AEPosNegOpt)aexpr).mlistChildren.size();
            for (int idx = Math.min(nChildrenListSize1, nChildrenListSize2) - 1; idx >= 0; idx --)    {
                int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEPosNegOpt)aexpr).mlistChildren.get(idx));
                if (nCompareChildReturn != 0)    {
                    return nCompareChildReturn;
                }
            }
            if (nChildrenListSize1 > nChildrenListSize2)    {
                return 1;
            } else if (nChildrenListSize1 < nChildrenListSize2)    {
                return -1;
            } else    {
                return 0;
            }
        }
    }

    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException    {
        validateAbstractExpr();
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            if (mlistChildren.get(idx).isNegligible() == false)    {
                return false;
            }
        }
        return true;
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = "";
        if (mlistChildren.size() == 1 && (mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                || mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN))    {
            // expression is +value
            strOutput = mlistChildren.get(0).output();
        } else    {
            for (int idx = 0; idx < mlistOpts.size(); idx ++)    {
                boolean bLeftHasOpt = false, bRightHasOpt = false;
                if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                        || mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
                    if (idx != 0)    {
                        strOutput += "+";
                        bLeftHasOpt = true;
                    }
                } else    {
                    strOutput += "-";
                    bLeftHasOpt = true;
                }
                if (idx < mlistOpts.size() - 1) {
                    bRightHasOpt = true;
                }
                
                boolean bNeedBracketsWhenToStr = false;
                if (bLeftHasOpt && bRightHasOpt)  {
                    bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 0);
                } else if (bLeftHasOpt && !bRightHasOpt)    {
                    bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, -1);
                } else if (!bLeftHasOpt && bRightHasOpt)    {
                    bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 1);
                }
                if (bNeedBracketsWhenToStr) {
                    strOutput += "(" + mlistChildren.get(idx).output() + ")";
                } else  {
                    strOutput += mlistChildren.get(idx).output();
                }
            }
        }
        return strOutput;
    }
    
    // this function returns operator for the idxth child. If it is the first child, return add.
    public boolean isPosOpt(int idx) throws JSmartMathErrException    {
        if (idx > mlistOpts.size())    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
        } else if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                || mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)    {
            return true;
        } else    {    // SUB or NEGSIGN
            return false;
        }
    }
    
    //=============== The following functions only support 2D matrix calculation
    // moreover, content of aeInput1 and aeInput2 might be changed so that they should be deeply copied
    // before transfer the parameter.
    
    // this function will be called when merging two pos-neg abstract expressions.
    private static AbstractExpr mergePosNegAddSubPosNeg(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd) throws JSmartMathErrException, JFCALCExpErrException    {
        if (aeInput1 instanceof AEPosNegOpt && aeInput2 instanceof AEPosNegOpt)    {
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.addAll(((AEPosNegOpt)aeInput1).mlistChildren); // cloneSelf only needed in case like a*(B+C), a needs to be distributed to B and C.
            listChildren.addAll(((AEPosNegOpt)aeInput2).mlistChildren);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.addAll(((AEPosNegOpt)aeInput1).mlistOpts);
            if (bIsAdd)    {
                listOpts.addAll(((AEPosNegOpt)aeInput2).mlistOpts);
            } else    {
                for (int idx = 0; idx < ((AEPosNegOpt)aeInput2).mlistOpts.size(); idx ++)    {
                    switch(((AEPosNegOpt)aeInput2).mlistOpts.get(idx).getOperatorType())    {
                    case OPERATOR_ADD:
                    case OPERATOR_POSSIGN:
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                        break;
                    case OPERATOR_SUBTRACT:
                    case OPERATOR_NEGSIGN:
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
                        break;
                    default:
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
                    } 
                }
            }
            return new AEPosNegOpt(listChildren, listOpts);
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    // this function will be called when merging pos-neg and another type of abstract expressions.
    private static AbstractExpr mergePosNegAddSubOther(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd) throws JSmartMathErrException, JFCALCExpErrException    {
        if (aeInput1 instanceof AEPosNegOpt && !(aeInput2 instanceof AEPosNegOpt))    {
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.addAll(((AEPosNegOpt)aeInput1).mlistChildren); // cloneSelf only needed in case like a*(B+C), a needs to be distributed to B and C.
            listChildren.add(aeInput2);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.addAll(((AEPosNegOpt)aeInput1).mlistOpts);
            if (bIsAdd)    {
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
            } else    {
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
            }
            return new AEPosNegOpt(listChildren, listOpts);
        } else if (!(aeInput1 instanceof AEPosNegOpt) && aeInput2 instanceof AEPosNegOpt)    {
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeInput1);
            listChildren.addAll(((AEPosNegOpt)aeInput2).mlistChildren); // cloneSelf only needed in case like a*(B+C), a needs to be distributed to B and C.
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
            if (bIsAdd)    {
                listOpts.addAll(((AEPosNegOpt)aeInput2).mlistOpts);
            } else    {
                for (int idx = 0; idx < ((AEPosNegOpt)aeInput2).mlistOpts.size(); idx ++)    {
                    switch(((AEPosNegOpt)aeInput2).mlistOpts.get(idx).getOperatorType())    {
                    case OPERATOR_ADD:
                    case OPERATOR_POSSIGN:
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                        break;
                    case OPERATOR_SUBTRACT:
                    case OPERATOR_NEGSIGN:
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
                        break;
                    default:
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
                    } 
                }
            }
            return new AEPosNegOpt(listChildren, listOpts);
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    // this function will merge a value zero with another abstract expression.
    private static AbstractExpr mergeZeroAddSubOther(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd,
            boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aeInput1 instanceof AEConst && ((AEConst)aeInput1).getDataClassRef().isZeros(false))    {
            if (aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE)    {
                // aeInput1 is a zero value or ignore matrix dim.
                if (bIsAdd)    {
                    return aeInput2;
                } else    {
                    LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                    listChildren.add(aeInput2);
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true));
                    return new AEPosNegOpt(listChildren, listOpts);
                }
            } else if (!(aeInput2 instanceof AEConst && ((AEConst)aeInput2).getDataClassRef().isZeros(false)
                 && aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE))    {
                // aeInput1 is a zero array while aeInput2 is not a zero value
                try    {
                    int[] narraySize1 = aeInput1.recalcAExprDim(bIgnoreMatrixDim);
                    int[] narraySize2 = aeInput2.recalcAExprDim(bIgnoreMatrixDim);
                    if (narraySize1.length != narraySize2.length)    {
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                    } else {
                        for (int idx = 0; idx < narraySize1.length; idx ++)    {
                            if (narraySize1[idx] != narraySize2[idx])    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                            }
                        }
                    }
                    // now the dimensions match
                    if (bIsAdd)    {
                        return aeInput2;
                    } else    {
                        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                        listChildren.add(aeInput2);
                        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true));
                        return new AEPosNegOpt(listChildren, listOpts);
                    }
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)    {
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    } else    {
                        throw e;
                    }
                }
            } else    {
                // aeInput2 is a zero value
                return aeInput1;
            }
        } else if (aeInput2 instanceof AEConst && ((AEConst)aeInput2).getDataClassRef().isZeros(false))    {
            if (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE)    {
                // aeInput2 is a zero value or ignore matrix dim.
                return aeInput1;
            } else    {
                // aeInput2 is a zero array while aeInput1 is definitely not a zero value
                try    {
                    int[] narraySize1 = aeInput1.recalcAExprDim(bIgnoreMatrixDim);
                    int[] narraySize2 = aeInput2.recalcAExprDim(bIgnoreMatrixDim);
                    if (narraySize1.length != narraySize2.length)    {
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                    } else {
                        for (int idx = 0; idx < narraySize1.length; idx ++)    {
                            if (narraySize1[idx] != narraySize2[idx])    {
                                throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                            }
                        }
                    }
                    // now the dimensions match
                    return aeInput1;
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)    {
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    } else    {
                        throw e;
                    }
                }
            }
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    // this function will be called when merging two Add-sub children and both of them are constant.
    private static AbstractExpr mergeConstAddSubConst(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aeInput1 instanceof AEConst && aeInput2 instanceof AEConst)    {
            // both aeInput1 and aeInput2 are constants
            DataClass datum1 = ((AEConst)aeInput1).getDataClassRef(),
                    datum2 = ((AEConst)aeInput2).getDataClassRef();

            CalculateOperator calcOpt = bIsAdd?(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2))
                                            :(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datum1, calcOpt, datum2);
            AbstractExpr aexprReturn = new AEConst(datum);
            return aexprReturn;        
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    // this function will be called when merging two add-sub children and one of them is a mul-div aexpr.
    private static AbstractExpr mergeMulDivAddSubOther(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd,
            boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        boolean bAE2NotMultiDiv = true;
        AbstractExpr aexprNotMultiDiv = null;
        AbstractExpr aexprMultiDiv = null;
        if (aeInput1.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV
                && aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)    {
            // AEinput2 is multiply-divide but aeInput1 is not.
            bAE2NotMultiDiv = false;
            aexprNotMultiDiv = aeInput1;
            aexprMultiDiv = aeInput2;
        } else if (aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV
                && aeInput2.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)    {
            // AEinput1 is mulitply or divide but aeInput2 is not.
            aexprNotMultiDiv = aeInput2;
            aexprMultiDiv = aeInput1;
        } else    {
            // this function does not handle other cases
            throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
        }
        
        if (((AEMulDivOpt)aexprMultiDiv).mlistChildren.size() == 2)    {
            // AE multiply divide has a two children and one of them is a constant value.
            // note that the two children cannot be both constants because this function is called after simplification and
            // evaluation.
            if (((AEMulDivOpt)aexprMultiDiv).mlistChildren.get(0).menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                    && ((AEMulDivOpt)aexprMultiDiv).mlistChildren.get(1).isEqual(aexprNotMultiDiv))    {
                // first child is a constant and second child equals to aexprNotMultiDiv.
                AEConst aeConstChild = (AEConst)((AEMulDivOpt)aexprMultiDiv).mlistChildren.get(0);
                int[] narrayConstDim = aeConstChild.recalcAExprDim(bIgnoreMatrixDim);
                boolean bHasEye = true;
                if (narrayConstDim.length != 2 && narrayConstDim.length != 0)    {
                    // only support number or 2D matrix.
                    bHasEye = false;
                } else {
                    for (int idx = 0; idx < narrayConstDim.length; idx ++)    {
                        if (narrayConstDim[idx] != narrayConstDim[0])    {
                            bHasEye = false;
                        }
                    }
                }
                if (bHasEye)    {    // has a corresponding eye matrix.
                    DataClass datumEye = new DataClass();
                    if (narrayConstDim.length == 0)    {    // if it is not a matrix.
                        datumEye.setDataValue(MFPNumeric.ONE);
                    } else    {
                        datumEye = BuiltinProcedures.createEyeMatrix(narrayConstDim[0], narrayConstDim.length);
                    }
                    DataClass datumConst = aeConstChild.getDataClassRef();
                    CalculateOperator calcOpt;
                    DataClass datum;
                    AEConst aeConst;
                    AbstractExpr aexprReturn = AEInvalid.AEINVALID;
                    LinkedList<AbstractExpr> listAENew = new LinkedList<AbstractExpr>();
                    LinkedList<CalculateOperator> listCalcOptNew = new LinkedList<CalculateOperator>();
                    switch (((AEMulDivOpt)aexprMultiDiv).mlistOpts.get(1).getOperatorType())    {
                    case OPERATOR_MULTIPLY:
                        // const * a
                        if (bIsAdd)    {
                            calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                        } else    {
                            calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                        }
                        if (((AEMulDivOpt)aexprMultiDiv).mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE)   {
                            datumConst = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst);
                                    //BuiltinProcedures.evaluateReciprocal(datumConst); // cannot use this because it does not handle aexpr datum
                        }
                        if (bAE2NotMultiDiv)    {
                            datum = ExprEvaluator.evaluateTwoOperandCell(datumConst, calcOpt, datumEye);
                        } else    {
                            datum = ExprEvaluator.evaluateTwoOperandCell(datumEye, calcOpt, datumConst);
                        }
                        aeConst = new AEConst(datum);
                        listAENew.add(aeConst);
                        listAENew.add(aexprNotMultiDiv);
                        listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        aexprReturn = new AEMulDivOpt(listAENew, listCalcOptNew);
                        return aexprReturn;
                    case OPERATOR_DIVIDE:
                        // const / a, we cannot merge it with a
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    }
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                }
            } else if (((AEMulDivOpt)aexprMultiDiv).mlistChildren.get(1).menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                    && ((AEMulDivOpt)aexprMultiDiv).mlistChildren.get(0).isEqual(aexprNotMultiDiv)
                    && ((AEMulDivOpt)aexprMultiDiv).mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)    {
                // second child is a constant and first child equals to aexprNotMultiDiv.
                AEConst aeConstChild = (AEConst)((AEMulDivOpt)aexprMultiDiv).mlistChildren.get(1);
                int[] narrayConstDim = aeConstChild.recalcAExprDim(bIgnoreMatrixDim);
                boolean bHasEye = true;
                if (narrayConstDim.length != 2 && narrayConstDim.length != 0)    {
                    bHasEye = false;
                } else {
                    for (int idx = 0; idx < narrayConstDim.length; idx ++)    {
                        if (narrayConstDim[idx] != narrayConstDim[0])    {
                            bHasEye = false;
                        }
                    }
                }
                if (bHasEye)    {    // has a corresponding eye matrix.
                    DataClass datumEye = new DataClass();
                    if (narrayConstDim.length == 0)    {    // if it is not a matrix.
                        datumEye.setDataValue(MFPNumeric.ONE);
                    } else    {
                        datumEye = BuiltinProcedures.createEyeMatrix(narrayConstDim[0], narrayConstDim.length);
                    }
                    DataClass datumConst = aeConstChild.getDataClassRef();
                    CalculateOperator calcOpt;
                    DataClass datum;
                    AEConst aeConst;
                    AbstractExpr aexprReturn = AEInvalid.AEINVALID;
                    LinkedList<AbstractExpr> listAENew = new LinkedList<AbstractExpr>();
                    LinkedList<CalculateOperator> listCalcOptNew = new LinkedList<CalculateOperator>();
                    switch (((AEMulDivOpt)aexprMultiDiv).mlistOpts.get(1).getOperatorType())    {
                    case OPERATOR_MULTIPLY:
                        // a * const
                        if (bIsAdd)    {
                            calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                        } else    {
                            calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                        }
                        if (bAE2NotMultiDiv)    {
                            datum = ExprEvaluator.evaluateTwoOperandCell(datumConst, calcOpt, datumEye);
                        } else    {
                            datum = ExprEvaluator.evaluateTwoOperandCell(datumEye, calcOpt, datumConst);
                        }
                        // mlistOpts.get(0) has been verified that it is multiply
                        aeConst = new AEConst(datum);
                        listAENew.add(aexprNotMultiDiv);
                        listAENew.add(aeConst);
                        listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        aexprReturn = new AEMulDivOpt(listAENew, listCalcOptNew);
                        return aexprReturn;
                    case OPERATOR_DIVIDE:
                        // a / const
                        datum = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst);
                                //BuiltinProcedures.evaluateReciprocal(datumConst); // cannot use this because it does not handle aexpr datum
                        if (bIsAdd)    {
                            calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                        } else    {
                            calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                        }
                        if (bAE2NotMultiDiv)    {
                            datum = ExprEvaluator.evaluateTwoOperandCell(datum, calcOpt, datumEye);
                        } else    {
                            datum = ExprEvaluator.evaluateTwoOperandCell(datumEye, calcOpt, datum);
                        }
                        aeConst = new AEConst(datum);
                        listAENew.add(aexprNotMultiDiv);
                        listAENew.add(aeConst);
                        listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        aexprReturn = new AEMulDivOpt(listAENew, listCalcOptNew);
                        return aexprReturn;
                    }
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                }
            }
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }

    // this function will be called when merging two add-sub children and one of them is a left-div aexpr.
    private static AbstractExpr mergeLeftDivAddSubOther(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        boolean bAE2NotLeftDiv = true;
        AbstractExpr aexprNotLeftDiv = null;
        AbstractExpr aexprLeftDiv = null;
        if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV)
                || (aeInput2.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV))    {
            aexprNotLeftDiv = aeInput2;
            aexprLeftDiv = aeInput1;            
        } else if ((aeInput1.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV)
                || (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV))    {
            bAE2NotLeftDiv = false;
            aexprNotLeftDiv = aeInput1;
            aexprLeftDiv = aeInput2;            
        } else    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
        }
        
        if (((AELeftDivOpt)aexprLeftDiv).maeLeft.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                && ((AELeftDivOpt)aexprLeftDiv).maeRight.isEqual(aexprNotLeftDiv))    {
            // first child is a constant and second child equals to aexprNotLeftDiv.
            // i.e. const \ a (not that a \ const cannot be merged with a).
            AEConst aeConstChild = (AEConst)((AELeftDivOpt)aexprLeftDiv).maeLeft;
            int[] narrayConstDim = aeConstChild.recalcAExprDim(bIgnoreMatrixDim);
            boolean bHasEye = true;
            if (narrayConstDim.length != 2 && narrayConstDim.length != 0)    {
                // only support number or 2D matrix.
                bHasEye = false;
            } else {
                for (int idx = 0; idx < narrayConstDim.length; idx ++)    {
                    if (narrayConstDim[idx] != narrayConstDim[0])    {
                        bHasEye = false;
                    }
                }
            }
            if (bHasEye)    {    // has a corresponding eye matrix.
                DataClass datumEye = new DataClass();
                if (narrayConstDim.length == 0)    {    // if it is not a matrix.
                    datumEye.setDataValue(MFPNumeric.ONE);
                } else    {
                    datumEye = BuiltinProcedures.createEyeMatrix(narrayConstDim[0], narrayConstDim.length);
                }
                DataClass datumConst = aeConstChild.getDataClassRef();
                CalculateOperator calcOpt;
                DataClass datum;
                AEConst aeConst;
                AbstractExpr aexprReturn = AEInvalid.AEINVALID;
                datum = ExprEvaluator.evaluateTwoOperandCell(datumConst, new CalculateOperator(OPERATORTYPES.OPERATOR_LEFTDIVIDE, 2), new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE));
                        //BuiltinProcedures.evaluateLeftReciprocal(datumConst);
                if (bIsAdd)    {
                    calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                } else    {
                    calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                }
                if (bAE2NotLeftDiv)    {
                    datum = ExprEvaluator.evaluateTwoOperandCell(datum, calcOpt, datumEye);
                } else    {
                    datum = ExprEvaluator.evaluateTwoOperandCell(datumEye, calcOpt, datum);
                }
                aeConst = new AEConst(datum);
                LinkedList<AbstractExpr> listAENew = new LinkedList<AbstractExpr>();
                LinkedList<CalculateOperator> listCalcOptNew = new LinkedList<CalculateOperator>();
                listAENew.add(aeConst);
                listAENew.add(aexprNotLeftDiv);
                listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                aexprReturn = new AEMulDivOpt(listAENew, listCalcOptNew);
                return aexprReturn;
            }
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    // this function will be called when merging two add-sub children and both them are left-div aexprs.
    private static AbstractExpr mergeLeftDivAddSubLeftDiv(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        if (((AELeftDivOpt)aeInput1).maeLeft.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                && ((AELeftDivOpt)aeInput2).maeLeft.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                && ((AELeftDivOpt)aeInput1).maeRight.isEqual(((AELeftDivOpt)aeInput2).maeRight))    {
            // const1 \ a and const2 \ a
            AEConst aeConstChild1 = (AEConst)((AELeftDivOpt)aeInput1).maeLeft;
            AEConst aeConstChild2 = (AEConst)((AELeftDivOpt)aeInput2).maeLeft;
            DataClass datum1 =  ExprEvaluator.evaluateTwoOperandCell(aeConstChild1.getDataClassRef(), new CalculateOperator(OPERATORTYPES.OPERATOR_LEFTDIVIDE, 2), new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE));
                        //BuiltinProcedures.evaluateLeftReciprocal(aeConstChild1.getDataClassRef());
            DataClass datum2 = ExprEvaluator.evaluateTwoOperandCell(aeConstChild2.getDataClassRef(), new CalculateOperator(OPERATORTYPES.OPERATOR_LEFTDIVIDE, 2), new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE));
                        //BuiltinProcedures.evaluateLeftReciprocal(aeConstChild2.getDataClassRef());
            CalculateOperator calcOpt;
            DataClass datum;
            AEConst aeConst;
            if (bIsAdd)    {
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
            } else    {
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
            }
            datum = ExprEvaluator.evaluateTwoOperandCell(datum1, calcOpt, datum2);
            aeConst = new AEConst(datum);
            LinkedList<AbstractExpr> listAENew = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listCalcOptNew = new LinkedList<CalculateOperator>();
            listAENew.add(aeConst);
            listAENew.add(((AELeftDivOpt)aeInput1).maeRight);
            listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listCalcOptNew.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            AbstractExpr aexprReturn = new AEMulDivOpt(listAENew, listCalcOptNew);
            return aexprReturn;
        } else if (((AELeftDivOpt)aeInput1).maeLeft.isEqual(((AELeftDivOpt)aeInput2).maeLeft))    {
            // same a \ * and same a \ *
            AbstractExpr aeChild1 = ((AELeftDivOpt)aeInput1).maeRight;
            AbstractExpr aeChild2 = ((AELeftDivOpt)aeInput2).maeRight;
            // if unsuccessful, will throw an ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS exception.
            AbstractExpr aexpr = mergeAddSub(aeChild1, aeChild2, bIsAdd, bIgnoreMatrixDim);
            AbstractExpr aexprReturn = new AELeftDivOpt((AEConst)((AELeftDivOpt)aeInput1).maeLeft, aexpr);
            return aexprReturn;
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }

    // this function will be called when merging two add-sub children and both them are mul-div aexprs.
    // consider the following cases:
    // aeInput1 is const * a * ..., aeInput2 is a * ...
    // aeInput1 is a * ... * const, aeInput2 is a * ...
    // aeInput1 is a * ..., aeInput2 is const * a * ...
    // aeInput1 is a * ..., aeInput2 is a * ... * const
    // aeInput1 is const1 * a * ..., aeInput2 is const2 * a * ...
    // aeInput1 is a * ... * const1, aeInput2 is a * ... * const2
    // aeInput1 is const1 * a * ..., aeInput2 is const1 * a * ...
    // aeInput1 is a * ... * const1, aeInput2 is a * ... * const1
    // aeInput1 is a * ..., aeInput2 is a * ...
    private static AbstractExpr mergeMulDivAddSubMulDiv(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd) throws JFCALCExpErrException, JSmartMathErrException    {
        if (aeInput1.isEqual(aeInput2))    {
            // aeInput1 and aeInput2 are the same
            if (bIsAdd == false)    {
                // aeInput1 - aeInput2, return 0
                DataClass datumReturn = new DataClass();
                datumReturn.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                AbstractExpr aexprReturn = new AEConst(datumReturn);
                return aexprReturn;
            } else    {
                AEMulDivOpt aexprReturn = new AEMulDivOpt();
                aexprReturn.copy(aeInput1);
                for (int idx = 0; idx < ((AEMulDivOpt)aeInput1).mlistChildren.size(); idx ++)    {
                    if (((AEMulDivOpt)aeInput1).mlistChildren.get(idx) instanceof AEConst)    {
                        // if this child is a constant.
                        AEConst aeConstChild = (AEConst)((AEMulDivOpt)aeInput1).mlistChildren.get(idx);
                        DataClass datumConst = aeConstChild.getDataClassRef();
                        if (((AEMulDivOpt)aeInput1).mlistOpts.get(idx).getOperatorType()
                                    == OPERATORTYPES.OPERATOR_MULTIPLY)    {
                            // multiply child
                            datumConst = ExprEvaluator.evaluateTwoOperandCell(datumConst,
                                    new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2),
                                    new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO));
                        } else    {
                            // divided by child
                            datumConst = ExprEvaluator.evaluateTwoOperandCell(datumConst,
                                    new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2),
                                    new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO));
                        }
                        aeConstChild = new AEConst(datumConst);
                        aexprReturn.mlistChildren.set(idx, aeConstChild);
                        return aexprReturn;
                    }
                }
                
                // ok, there is no constant child
                DataClass datumConst = new DataClass();
                datumConst.setDataValue(MFPNumeric.TWO, DATATYPES.DATUM_INTEGER);
                aexprReturn.mlistChildren.add(new AEConst(datumConst));
                aexprReturn.mlistOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));    // order: constant is first
                return aexprReturn;
            }
        } else if (((AEMulDivOpt)aeInput1).mlistChildren.size() == ((AEMulDivOpt)aeInput2).mlistChildren.size())    {
            // if aeInput1 and aeInput2 can be merged and they have same number
            // of children, their pattern must be
            // aeInput1 is const1 * a * ..., aeInput2 is const2 * a * ...
            // aeInput1 is a * ... * const1, aeInput2 is a * ... * const2
            // aeInput1 is a * ... * const1 * ..., aeInput2 is a * ... * const2 * ...,
            // note that all the constants have to be simplified as multiply.
            int nNotEqualChild = -1;
            for (int idx = 0; idx < ((AEMulDivOpt)aeInput1).mlistChildren.size(); idx ++)    {
                if (((AEMulDivOpt)aeInput1).mlistChildren.get(idx)
                        .isEqual(((AEMulDivOpt)aeInput2).mlistChildren.get(idx)) == false
                        || ((AEMulDivOpt)aeInput1).mlistOpts.get(idx).getOperatorType() != ((AEMulDivOpt)aeInput2).mlistOpts.get(idx).getOperatorType())    {
                    if (nNotEqualChild != -1)    {
                        // more than one children are different
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    }
                    nNotEqualChild = idx;
                    if (!(((AEMulDivOpt)aeInput1).mlistChildren.get(idx) instanceof AEConst)
                            || !(((AEMulDivOpt)aeInput2).mlistChildren.get(idx) instanceof AEConst)) {
                        // the different child must be constant
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    }
                }
            }
            boolean bIsMultiplyChild1 = true, bIsMultiplyChild2 = true;
            if (((AEMulDivOpt)aeInput1).mlistOpts.get(nNotEqualChild).getOperatorType()
                        == OPERATORTYPES.OPERATOR_DIVIDE)    {
                bIsMultiplyChild1 = false;
            }
            if (((AEMulDivOpt)aeInput2).mlistOpts.get(nNotEqualChild).getOperatorType()
                        == OPERATORTYPES.OPERATOR_DIVIDE)    {
                bIsMultiplyChild2 = false;
            }
            AEConst aeChildConst1 = (AEConst)((AEMulDivOpt)aeInput1).mlistChildren.get(nNotEqualChild);
            DataClass datumConst1 = aeChildConst1.getDataClassRef();
            AEConst aeChildConst2 = (AEConst)((AEMulDivOpt)aeInput2).mlistChildren.get(nNotEqualChild);        
            DataClass datumConst2 = aeChildConst2.getDataClassRef();
            DataClass datum = new DataClass();
            CalculateOperator calcOptMerge = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
            if (bIsAdd == false)    {
                calcOptMerge = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
            }
            if (bIsMultiplyChild1 == bIsMultiplyChild2 && bIsMultiplyChild1)    {    // ... * const1 ... and ... * const2 ...
                datum = ExprEvaluator.evaluateTwoOperandCell(datumConst1,
                                calcOptMerge,
                                datumConst2);
                AEConst aeMergedChild = new AEConst(datum);
                AEMulDivOpt aexprReturn = new AEMulDivOpt();
                aexprReturn.copy(aeInput1);
                aexprReturn.mlistChildren.set(nNotEqualChild, aeMergedChild);
                return aexprReturn;
            } else if (bIsMultiplyChild1 == bIsMultiplyChild2 && !bIsMultiplyChild1) {    // ... / const1 ... and ... / const2 ...
                DataClass datum1 = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst1);
                                    //BuiltinProcedures.evaluateReciprocal(datumConst1); // cannot use this because it does not handle aexpr datum
                DataClass datum2 = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst2);
                                    //BuiltinProcedures.evaluateReciprocal(datumConst2); // cannot use this because it does not handle aexpr datum
                datum = ExprEvaluator.evaluateTwoOperandCell(datum1,
                                                calcOptMerge,
                                                datum2);
                AEConst aeMergedChild = new AEConst(datum);
                AEMulDivOpt aexprReturn = new AEMulDivOpt();
                aexprReturn.copy(aeInput1);
                aexprReturn.mlistOpts.set(nNotEqualChild, new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                aexprReturn.mlistChildren.set(nNotEqualChild, aeMergedChild);
                return aexprReturn;
            } else if (bIsMultiplyChild1)    {    // ... * const1 * ... and ... / const2 * ...
                datum = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst2);
                        //BuiltinProcedures.evaluateReciprocal(datumConst); // cannot use this because it does not handle aexpr datum
                datum = ExprEvaluator.evaluateTwoOperandCell(datumConst1,
                                                            calcOptMerge,
                                                            datum);
                AEConst aeMergedChild = new AEConst(datum);
                AEMulDivOpt aexprReturn = new AEMulDivOpt();
                aexprReturn.copy(aeInput1);
                aexprReturn.mlistChildren.set(nNotEqualChild, aeMergedChild);
                return aexprReturn;
            } else    {    // ... / const1 * ... and ... * const2 * ...
                datum = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst1);
                        //BuiltinProcedures.evaluateReciprocal(datumConst1); // cannot use this because it does not handle aexpr datum
                datum = ExprEvaluator.evaluateTwoOperandCell(datum,
                                                            calcOptMerge,
                                                            datumConst2);
                AEConst aeMergedChild = new AEConst(datum);
                AEMulDivOpt aexprReturn = new AEMulDivOpt();
                aexprReturn.copy(aeInput2);
                aexprReturn.mlistChildren.set(nNotEqualChild, aeMergedChild);
                return aexprReturn;
            }
        } else    {
            // aeInput1.mlistChildren.size() != aeInput2.mlistChildren.size()
            if (Math.abs(((AEMulDivOpt)aeInput1).mlistChildren.size() - ((AEMulDivOpt)aeInput2).mlistChildren.size()) != 1)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
            }
            boolean b1LongerThan2 = true;
            AEMulDivOpt aexprLong, aexprShort;
            if (((AEMulDivOpt)aeInput1).mlistChildren.size() > ((AEMulDivOpt)aeInput2).mlistChildren.size())    {
                aexprLong = (AEMulDivOpt) aeInput1;
                aexprShort = (AEMulDivOpt) aeInput2;
                b1LongerThan2 = true;
            } else    {
                aexprLong = (AEMulDivOpt) aeInput2;
                aexprShort = (AEMulDivOpt) aeInput1;
                b1LongerThan2 = false;
            }
            int idxChildShort = 0;
            int idxChildLong = 0;
            int idxUnequalLong = -1;
            for (; (idxChildLong < aexprLong.mlistChildren.size()) && (idxChildShort < aexprShort.mlistChildren.size());
                idxChildLong ++, idxChildShort ++)    {
                if (aexprLong.mlistChildren.get(idxChildLong).isEqual(aexprShort.mlistChildren.get(idxChildShort)) == false
                        || aexprLong.mlistOpts.get(idxChildLong).getOperatorType() != aexprShort.mlistOpts.get(idxChildShort).getOperatorType())    {
                    if (idxUnequalLong != -1)    {    // more than one children difference
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    }
                    if (!(aexprLong.mlistChildren.get(idxChildLong) instanceof AEConst))    {
                        // different part is not a constant.
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
                    }
                    idxUnequalLong = idxChildLong;
                    idxChildShort --;    // compare this child of aexprShort to next child of aexprLong
                }
            }
            if (idxUnequalLong == -1)    {    // the last child is different.
                idxUnequalLong = aexprLong.mlistChildren.size() - 1;
            }
            if (!(aexprLong.mlistChildren.get(idxUnequalLong) instanceof AEConst)) {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
            }
            DataClass datumConst = ((AEConst)(aexprLong.mlistChildren.get(idxUnequalLong))).getDataClassRef();
            if (aexprLong.mlistOpts.get(idxUnequalLong).getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE)    {
                // if operator is divide
                datumConst = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datumConst);
                             //BuiltinProcedures.evaluateReciprocal(datumConst); // cannot use this because it does not handle aexpr datum
            }
            int[] narrayConstDim = datumConst.recalcDataArraySize();
            boolean bHasEye = true;
            if (narrayConstDim.length != 2 && narrayConstDim.length != 0)    {
                // only support number or 2D matrix.
                bHasEye = false;
            } else {
                for (int idx = 0; idx < narrayConstDim.length; idx ++)    {
                    if (narrayConstDim[idx] != narrayConstDim[0])    {
                        bHasEye = false;
                    }
                }
            }
            if (bHasEye)    {
                DataClass datumEye = new DataClass();
                if (narrayConstDim.length == 0)    {    // a non-array value
                    datumEye.setDataValue(MFPNumeric.ONE, DATATYPES.DATUM_INTEGER);
                } else    {    // only support 2D array.
                    datumEye = BuiltinProcedures.createEyeMatrix(narrayConstDim[0], narrayConstDim.length);
                }
                if (bIsAdd)    {    // aexprInput1 + aexprInput2
                    datumConst = ExprEvaluator.evaluateTwoOperandCell(datumEye, new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2), datumConst);
                } else if (b1LongerThan2)    {    // aexprInput1 - aexprInput2 and aexprInput1 has the datumConst
                    datumConst = ExprEvaluator.evaluateTwoOperandCell(datumConst, new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2), datumEye);
                } else    {    // aexprInput1 - aexprInput2 and aexprInput2 has the datumConst
                    datumConst = ExprEvaluator.evaluateTwoOperandCell(datumEye, new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2), datumConst);
                }
                AEMulDivOpt aexprReturn = new AEMulDivOpt();
                aexprReturn.copy(aexprLong);
                aexprReturn.mlistChildren.set(idxUnequalLong, new AEConst(datumConst));
                // operator is always multiply
                aexprReturn.mlistOpts.set(idxUnequalLong,
                                        new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                return aexprReturn;
            }
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    // merge if aeInput1 and aeInput2 are the same.
    private static AbstractExpr mergeAddSubSame(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        if (bIsAdd)    {
            DataClass datumTwo = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO);
            AEConst aeTwo = new AEConst(datumTwo);
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(aeTwo);
            listChildren.add(aeInput1);    // do not use cloneSelf because believe aeInput1 will not be used any more.
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            // simplify after this function returns.
            return new AEMulDivOpt(listChildren, listOpts);
        } else {    // subtract
            int[] narrayDim = new int[0];
            try {
                narrayDim = aeInput1.recalcAExprDim(bIgnoreMatrixDim);
                DataClass datumReturn = BuiltinProcedures.createUniValueMatrix(narrayDim,
                                            new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO));
                return new AEConst(datumReturn);
            } catch (JSmartMathErrException e)    {
                if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)    {
                    // 0 is treated as a special value in add/subtract.
                    return new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO));
                }
                // other exceptions means error which should be thrown to higher level.
                throw e;
            }
        }
    }
    // merge if aeInput1 and aeInput2 are the postive or negative of each other.
    private static AbstractExpr merge2PosNegs(AbstractExpr aeInput1, AbstractExpr aeInput2, int nPosNeg, boolean bIsAdd, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException {
        if (((nPosNeg == 1) && !bIsAdd) || ((nPosNeg == -1) && bIsAdd)) {
            return mergeAddSubSame(aeInput1, aeInput2, false, bIgnoreMatrixDim);
        } else {
            return mergeAddSubSame(aeInput1, aeInput2, true, bIgnoreMatrixDim);
        }
    }
    /*
     * This function try to merge two abstract expressions connected by + or -.
     * Assume that the two abstract expression parameters have to sorted and simplified (simplified means all the * 1 and * I have been
     * eliminated and all the constants are multiplied ).
     */
    public static AbstractExpr mergeAddSub(AbstractExpr aeInput1, AbstractExpr aeInput2, boolean bIsAdd, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException    {
        int nPosNegFor1And2 = 0;
        if (aeInput1 instanceof AEConst && aeInput2 instanceof AEConst)    {
            // both aeInput1 and aeInput2 are constants. Needs to do this first otherwise we get 0+0->2*0
            return mergeConstAddSubConst(aeInput1, aeInput2, bIsAdd);
        } else if (aeInput1.isEqual(aeInput2) && (!(aeInput1 instanceof AEConst) || !(aeInput2 instanceof AEConst)))    {
            // aeInput1 equals aeInput2 and at least one of them is not const.
            return mergeAddSubSame(aeInput1, aeInput2, bIsAdd, bIgnoreMatrixDim);
        } else if ((nPosNegFor1And2 = mustBeNegativeOrPositive(aeInput1, aeInput2)) != 0) {
            // if aeInput1 == - aeInput2 or aeInput1 == aeInput2,
            return merge2PosNegs(aeInput1, aeInput2, nPosNegFor1And2, bIsAdd, bIgnoreMatrixDim);
        } else if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV)
                && (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV))    {
            // both of aeInput1 and aeInput2 are left division.
            return mergeLeftDivAddSubLeftDiv(aeInput1, aeInput2, bIsAdd, bIgnoreMatrixDim);
        } else if((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)
                && (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV))    {
            // both of aeInput1 and aeInput2 are multiply division.
            return mergeMulDivAddSubMulDiv(aeInput1, aeInput2, bIsAdd);
        } else if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG)
                && (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG))    {
            // both of aeInput1 and aeInput2 are pos neg.
            return mergePosNegAddSubPosNeg(aeInput1, aeInput2, bIsAdd);
        } else {
            // the following tests are for cases where one is a determined type but the other is
            // not a determined type. So we have to try every possiblity.
            if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)
                    || (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV))    {
                // one of aeInput1 and aeInput2 is multiply division.
                try    {
                    return mergeMulDivAddSubOther(aeInput1, aeInput2, bIsAdd, bIgnoreMatrixDim);
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                        ;    // do nothing but handle the exception.
                    }
                }
            }
            if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV)
                    || (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV))    {
                // one of aeInput1 and aeInput2 is left division.
                try    {
                    return mergeLeftDivAddSubOther(aeInput1, aeInput2, bIsAdd, bIgnoreMatrixDim);
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                        ;    // do nothing but handle the exception.
                    }
                }
            }
            if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG)
                    || (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG))    {
                // one of aeInput1 and aeInput2 is pos neg.
                try    {
                    return mergePosNegAddSubOther(aeInput1, aeInput2, bIsAdd);
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                        ;    // do nothing but handle the exception.
                    }
                }
            }
            if ((aeInput1 instanceof AEConst && ((AEConst)aeInput1).getDataClassRef().isZeros(false))
                || (aeInput2 instanceof AEConst && ((AEConst)aeInput2).getDataClassRef().isZeros(false))){
                // either aeInput1 or aeInput2 or both are zero(s).
                try    {
                    return mergeZeroAddSubOther(aeInput1, aeInput2, bIsAdd, bIgnoreMatrixDim);
                } catch (JSmartMathErrException e)    {
                    if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)    {
                        ;    // do nothing but handle the exception.
                    }
                }
            }
        } 
        // cannot merge.
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
    public static AbstractExpr mergeNegSignIntoSingleChild(AbstractExpr aeInput) throws JFCALCExpErrException, JSmartMathErrException   {
        if (aeInput instanceof AEConst) {
            DataClass datumChild = ((AEConst)aeInput).getDataClassRef();
            DataClass datumNewValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1), datumChild);
            AEConst aeReturn = new AEConst(datumNewValue);
            return aeReturn;
        } else if (aeInput instanceof AEPosNegOpt)  {
            AEPosNegOpt aeReturn = new AEPosNegOpt();
            aeReturn.copy(aeInput);  // have to copy because aeReturn will change.
            for (int idx = 0; idx < aeReturn.mlistOpts.size(); idx ++)    {
                CalculateOperator co = aeReturn.mlistOpts.get(idx);
                if (co.getOperatorType() == OPERATORTYPES.OPERATOR_ADD) {
                    aeReturn.mlistOpts.set(idx, new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT) {
                    aeReturn.mlistOpts.set(idx, new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)  {
                    aeReturn.mlistOpts.set(idx, new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true));
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)  {
                    aeReturn.mlistOpts.set(idx, new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
                }
            }
            return aeReturn;
        } else if (aeInput instanceof AEMulDivOpt)  {
            AEMulDivOpt aeReturn = new AEMulDivOpt();
            aeReturn.copy(aeInput);  // have to copy because aeReturn will change.
            for (int idx = 0; idx < ((AEMulDivOpt)aeReturn).mlistChildren.size(); idx ++)    {
                AbstractExpr aeChild = ((AEMulDivOpt)aeReturn).mlistChildren.get(idx);
                if (aeChild instanceof AEConst) {
                    DataClass datumChild = ((AEConst)aeChild).getDataClassRef();
                    DataClass datumNewValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1), datumChild);
                    ((AEMulDivOpt)aeReturn).mlistChildren.set(idx, new AEConst(datumNewValue));
                    return aeReturn;
                }
            }
            // none of the child is constant. add -1 * in the front.
            AEConst aeMinus1 = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.MINUS_ONE));
            CalculateOperator co = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            ((AEMulDivOpt)aeReturn).mlistChildren.addFirst(aeMinus1);
            ((AEMulDivOpt)aeReturn).mlistOpts.addFirst(co);
            return aeReturn;
        } else if (aeInput instanceof AELeftDivOpt 
                && (((AELeftDivOpt)aeInput).maeLeft instanceof AEConst || ((AELeftDivOpt)aeInput).maeRight instanceof AEConst))    {
            AEConst aeConstChild = new AEConst();
            if (((AELeftDivOpt)aeInput).maeLeft instanceof AEConst) {
                aeConstChild = (AEConst) ((AELeftDivOpt)aeInput).maeLeft;
            } else  {
                aeConstChild = (AEConst) ((AELeftDivOpt)aeInput).maeRight;
            }
            DataClass datumChild = ((AEConst)aeConstChild).getDataClassRef();
            DataClass datumNewValue = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1), datumChild);
            if (((AELeftDivOpt)aeInput).maeLeft instanceof AEConst) {
                AELeftDivOpt aeReturn = new AELeftDivOpt(new AEConst(datumNewValue), ((AELeftDivOpt)aeInput).maeRight);
                return aeReturn;
            } else  {
                AELeftDivOpt aeReturn = new AELeftDivOpt(((AELeftDivOpt)aeInput).maeLeft, new AEConst(datumNewValue));
                return aeReturn;
            }
        } else  {
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listChildren.add(aeInput);
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true));
            AbstractExpr aeReturn = new AEPosNegOpt(listChildren, listOpts);
            return aeReturn;
        }
    }

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        LinkedList<AbstractExpr> listChildrenCvted = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx) instanceof AEConst) {
                listChildrenCvted.add(mlistChildren.get(idx));
            } else {
                listChildrenCvted.add(mlistChildren.get(idx).convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars));
            }
        }
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        listOpts.addAll(mlistOpts);
        return new AEPosNegOpt(listChildrenCvted, listOpts);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        LinkedList<AbstractExpr> listChildrenCvted = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx) instanceof AEConst
                    && ((AEConst)mlistChildren.get(idx)).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                listChildrenCvted.add(((AEConst)mlistChildren.get(idx)).getDataClassRef().getAExpr());
            } else {
                listChildrenCvted.add(mlistChildren.get(idx));
            }
        }
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        listOpts.addAll(mlistOpts);
        return new AEPosNegOpt(listChildrenCvted, listOpts);
    }
    
    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = 0;
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            nCnt += mlistChildren.get(idx).getVarAppearanceCnt(strVarName);
        }
        return nCnt;
    }
}
