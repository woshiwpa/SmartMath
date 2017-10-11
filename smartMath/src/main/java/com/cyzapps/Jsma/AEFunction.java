package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEFunction extends AbstractExpr {

    public String mstrFuncName = "";
    public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>();

    public AEFunction() {
        initAbstractExpr();
    }
    
    public AEFunction(String strName, LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException    {
        setAEFunction(strName, listChildren);
    }

    public AEFunction(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }

    @Override
    protected void initAbstractExpr() {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION;
        mstrFuncName = "";
        mlistChildren = new LinkedList<AbstractExpr>();
    }

    @Override
    public void validateAbstractExpr() throws JSmartMathErrException    {
        if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
        }
    }
    
    private void setAEFunction(String strName, LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException    {
        menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_FUNCTION;
        mstrFuncName = strName;
        mlistChildren = (listChildren == null)?new LinkedList<AbstractExpr>():listChildren;
        validateAbstractExpr();
    }
    
    @Override
    protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
            JSmartMathErrException {
        ((AEFunction)aexprOrigin).validateAbstractExpr();
        super.copy(aexprOrigin);
        mstrFuncName = ((AEFunction)aexprOrigin).mstrFuncName;
        mlistChildren = new LinkedList<AbstractExpr>();
        if (((AEFunction)aexprOrigin).mlistChildren != null)    {
            for (int idx = 0; idx < ((AEFunction)aexprOrigin).mlistChildren.size(); idx ++)    {
                mlistChildren.add(((AEFunction)aexprOrigin).mlistChildren.get(idx));
            }
        }
    }

    @Override
    protected void copyDeep(AbstractExpr aexprOrigin)
            throws JFCALCExpErrException, JSmartMathErrException {
        ((AEFunction)aexprOrigin).validateAbstractExpr();
        super.copyDeep(aexprOrigin);
        mstrFuncName = ((AEFunction)aexprOrigin).mstrFuncName;
        mlistChildren = new LinkedList<AbstractExpr>();
        if (((AEFunction)aexprOrigin).mlistChildren != null)    {
            for (int idx = 0; idx < ((AEFunction)aexprOrigin).mlistChildren.size(); idx ++)    {
                AbstractExpr aexprChild = ((AEFunction)aexprOrigin).mlistChildren.get(idx).cloneSelf();
                mlistChildren.add(aexprChild);
            }
        }
    }

    @Override
    public AbstractExpr cloneSelf() throws JFCALCExpErrException,
            JSmartMathErrException {
        AbstractExpr aeReturn = new AEFunction();
        aeReturn.copyDeep(this);
        return aeReturn;
    }

    @Override
    public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
            JFCALCExpErrException {
        if (bUnknownAsSingle) {
            return new int[0];
        }
        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
    }

    @Override
    public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
        if (menumAEType != aexpr.menumAEType)    {
            return false;
        } else if (mstrFuncName.equalsIgnoreCase(((AEFunction)aexpr).mstrFuncName) == false)    {
            return false;
        } else if (mlistChildren.size() != ((AEFunction)aexpr).mlistChildren.size())    {
            return false;
        } else {
            for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
                if (mlistChildren.get(idx).isEqual(((AEFunction)aexpr).mlistChildren.get(idx)) == false)    {
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
        /* do not call isPatternDegrade function because generally function expression cannot degrade-match a pattern.*/
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
        if (!(aePattern instanceof AEFunction))   {
            return false;
        }
        if (mlistChildren.size() != ((AEFunction)aePattern).mlistChildren.size()) {
            return false;
        }
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx).isPatternMatch(((AEFunction)aePattern).mlistChildren.get(idx),
                    listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
                return false;
            }
        }
        if (mstrFuncName.equalsIgnoreCase(((AEFunction)aePattern).mstrFuncName))   {
            // this is not a pattern so that mstrFuncName should not be something like f_single_var_invertible
            return true;
        } else if (((AEFunction)aePattern).mstrFuncName.equalsIgnoreCase(PtnSlvVarIdentifier.FUNCTION_SINGLE_VAR_INVERTIBLE)
                && PtnSlvVarIdentifier.isSingleVarInvertibleFunc(mstrFuncName)
                && mlistChildren.size() == 1)    {
            // assume there is always only one invertible single var function in the pattern.
            for (int idx = 0; idx < listpeuMapPseudoFuncs.size(); idx ++)  {
                if (listpeuMapPseudoFuncs.get(idx).maePatternUnit.isEqual(aePattern))    {
                    if (isEqual(listpeuMapPseudoFuncs.get(idx).maeExprUnit))   {
                        // this pseudo function has been mapped to a function and the function is the same as this
                        return true;
                    } else  {
                        // this pseudo function has been mapped to a function but the function is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern hasn't been mapped to any function before.
            PatternExprUnitMap peuMap = new PatternExprUnitMap(this, aePattern);
            listpeuMapPseudoFuncs.add(peuMap);
            return true;
        } else  {
            return false;
        }
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
        AEFunction aeReturn = new AEFunction();
        aeReturn.copy(this);
        aeReturn.mlistChildren = listChildren == null?new LinkedList<AbstractExpr>():listChildren;
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }	
	
    // this function replaces children who equal aeFrom to aeTo and
    // returns the number of children that are replaced.
    @Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException    {
        AEFunction aeReturn = new AEFunction();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)    {
            for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)    {
                if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);	// actually need to clone because will be many aeTo copies. Otherwise parent cannot be identified. However, at this moment don't care.
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                } else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit))    {
                    aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);	// actually need to clone because will be many aeTo copies. Otherwise parent cannot be identified. However, at this moment don't care.
                    listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
                    break;
                }
            }
        }
        return aeReturn;
    }

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
		LinkedList<DataClass> listParams = new LinkedList<DataClass>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			DataClass datumParameter = mlistChildren.get(idx).evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
            listParams.addFirst(datumParameter);    // no need to deep copy. also note that last parameer is the first in the param list.
		}
        DataClass datumReturn = FuncEvaluator.evaluateFunction(mstrFuncName, listParams, lVarNameSpaces);
        if (datumReturn == null)    {
            // a function can return nothing, but this function is not supported by smart math.
            throw new JSmartMathErrException(ERRORTYPES.ERROR_FUNCTION_CANNOT_RETURN_NOTHING);
        }
		return datumReturn;
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
		LinkedList<DataClass> listParams = new LinkedList<DataClass>();
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        boolean bAllChildrenKnownValues = true;
        for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			AbstractExpr aeParameter = mlistChildren.get(idx).evaluateAExpr(lUnknownVars, lVarNameSpaces);
            if (aeParameter instanceof AEConst) {
                listParams.addFirst(((AEConst)aeParameter).getDataClass()); // let getDataClass decides when to deep copy
            } else {
                bAllChildrenKnownValues = false;
            }
            listNewChildren.add(aeParameter);
		}
        if (bAllChildrenKnownValues) {
            DataClass datumReturn = FuncEvaluator.evaluateFunction(mstrFuncName, listParams, lVarNameSpaces);
            if (datumReturn == null)    {
                // a function can return nothing, but this function is not supported by smart math.
                throw new JSmartMathErrException(ERRORTYPES.ERROR_FUNCTION_CANNOT_RETURN_NOTHING);
            }
            return new AEConst(datumReturn);
        } else {
            return new AEFunction(mstrFuncName, listNewChildren);
        }
    }

    @Override
    public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
            LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
            throws InterruptedException, JSmartMathErrException,
            JFCALCExpErrException {
        validateAbstractExpr();
        AEFunction aeReturn = new AEFunction();
        aeReturn.copy(this);
        boolean bAllParamSolved = true;
        LinkedList<DataClass> listParams = new LinkedList<DataClass>();
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)    {
            AbstractExpr aeChild = aeReturn.mlistChildren.get(idx).simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
            if (aeChild instanceof AEConst)    {
                // aeChild refers to same data value in this. So have to call getDatumValue() instead of getFinalDatumValue().
                listParams.addFirst(((AEConst) aeChild).getDataClass());   // have to be addFirst because listParams will be read from tail.
            } else    {
                listParams.addFirst(new DataClass(aeChild));
                bAllParamSolved = false;
            }
            aeReturn.mlistChildren.set(idx, aeChild);
        }
        if (bAllParamSolved)    {
            DataClass datumReturn = FuncEvaluator.evaluateFunction(aeReturn.mstrFuncName, listParams, lVarNameSpaces);
            if (datumReturn == null)    {
                // a function can return nothing, but this function is not supported by smart math.
                throw new JSmartMathErrException(ERRORTYPES.ERROR_FUNCTION_CANNOT_RETURN_NOTHING);
            }
            AEConst aexprReturn = new AEConst(datumReturn);
            return aexprReturn;
        } else if (aeReturn.mstrFuncName.equalsIgnoreCase("pow") && aeReturn.mlistChildren.size() == 2) { // pow(x,y) converted to x**y
            AEPowerOpt aexprReturn = new AEPowerOpt(aeReturn.mlistChildren.getFirst(), aeReturn.mlistChildren.getLast());
            return aexprReturn;
        } else if (aeReturn.mstrFuncName.equalsIgnoreCase("exp") && aeReturn.mlistChildren.size() == 1) { // exp(x) converted to e**x
            AEConst aexprE = new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.E));
            AEPowerOpt aexprReturn = new AEPowerOpt(aexprE, aeReturn.mlistChildren.getLast());
            return aexprReturn;
        } else if (aeReturn.mstrFuncName.equalsIgnoreCase("invert") && aeReturn.mlistChildren.size() == 1) { // invert(x) converted to 1/x
            AEConst aexprOne = new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE));
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
            LinkedList<AbstractExpr> listAEChildren = new LinkedList<AbstractExpr>();
            listAEChildren.add(aexprOne);
            listAEChildren.add(aeReturn.mlistChildren.getLast());
            AEMulDivOpt aexprReturn = new AEMulDivOpt(listAEChildren, listOpts);
            return aexprReturn;
        } else if (aeReturn.mstrFuncName.equalsIgnoreCase("sqrt") && aeReturn.mlistChildren.size() == 1) { // sqrt(x) converted to x**0.5
            AEConst aexprHalf = new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.HALF));
            AEPowerOpt aexprReturn = new AEPowerOpt(aeReturn.mlistChildren.getFirst(), aexprHalf);
            return aexprReturn;
        } else if (aeReturn.mstrFuncName.equalsIgnoreCase("ln") && aeReturn.mlistChildren.size() == 1) { // ln(x) converted to log(x)
            AEFunction aexprReturn = new AEFunction("log", aeReturn.mlistChildren);
            return aexprReturn;
        } else if (simplifyParams.mbAllowCvtFunc2MoreThan1Funcs
                && aeReturn.mstrFuncName.equalsIgnoreCase("tan") && aeReturn.mlistChildren.size() == 1) { // tan(x) converted to sin(x)/cos(x)
            AEFunction aexprSin = new AEFunction("sin", aeReturn.mlistChildren);
            AbstractExpr aexprX = aeReturn.mlistChildren.getFirst();
            LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
            listNewChildren.add(aexprX);
            AEFunction aexprCos = new AEFunction("cos", listNewChildren);
            LinkedList<AbstractExpr> listCnvtChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listCnvtChildren.add(aexprSin);
            listCnvtChildren.add(aexprCos);
            CalculateOperator coMultiply = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            CalculateOperator coDiv = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
            listOpts.add(coMultiply);
            listOpts.add(coDiv);
            AEMulDivOpt aexprReturn = new AEMulDivOpt(listCnvtChildren, listOpts);
            return aexprReturn;
        } else if (simplifyParams.mbAllowCvtFunc2MoreThan1Funcs
                && aeReturn.mstrFuncName.equalsIgnoreCase("tanh") && aeReturn.mlistChildren.size() == 1) { // tanh(x) converted to sinh(x)/cosh(x)
            AEFunction aexprSinh = new AEFunction("sinh", aeReturn.mlistChildren);
            AbstractExpr aexprX = aeReturn.mlistChildren.getFirst();
            LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
            listNewChildren.add(aexprX);
            AEFunction aexprCosh = new AEFunction("cosh", listNewChildren);
            LinkedList<AbstractExpr> listCnvtChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listCnvtChildren.add(aexprSinh);
            listCnvtChildren.add(aexprCosh);
            CalculateOperator coMultiply = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            CalculateOperator coDiv = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
            listOpts.add(coMultiply);
            listOpts.add(coDiv);
            AEMulDivOpt aexprReturn = new AEMulDivOpt(listCnvtChildren, listOpts);
            return aexprReturn;
        } else    {
            return aeReturn;
        }
    }

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        return false;
    }
    
    @Override
    public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
        if (menumAEType.getValue() < aexpr.menumAEType.getValue())    {
            return 1;
        } else if (menumAEType.getValue() > aexpr.menumAEType.getValue())    {
            return -1;
        } else    {
            int nReturn = mstrFuncName.compareTo(((AEFunction)aexpr).mstrFuncName);
            if (nReturn == 0)    {
                // when comparing children, function parameter is a bit different from posneg sign
                int nChildrenListSize1 = mlistChildren.size();
                int nChildrenListSize2 = ((AEFunction)aexpr).mlistChildren.size();
                if (nChildrenListSize1 > nChildrenListSize2)    {
                    return 1;
                } else if (nChildrenListSize1 < nChildrenListSize2)    {
                    return -1;
                } else    {
                    for (int idx = 0; idx < nChildrenListSize1; idx ++)    {
                        int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEFunction)aexpr).mlistChildren.get(idx));
                        if (nCompareChildReturn != 0)    {
                            return nCompareChildReturn;
                        }
                    }
                    return 0;
                }                
            } else    {
                return nReturn;
            }
        }
    }
    
    // identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
    @Override
    public boolean isNegligible() throws JSmartMathErrException    {
        validateAbstractExpr();
        return false;
    }
    
    // output the string based expression of any abstract expression type.
    @Override
    public String output()    throws JFCALCExpErrException, JSmartMathErrException {
        validateAbstractExpr();
        String strOutput = mstrFuncName + "(";
        for (int idx = 0; idx < mlistChildren.size(); idx ++)    {
            // we do not need to consider to use ()
            strOutput += mlistChildren.get(idx).output();
            if (idx < mlistChildren.size() - 1)    {
                strOutput += ",";
            } else    {
                strOutput += ")";
            }
        }
        return strOutput;
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
        return new AEFunction(mstrFuncName, listChildrenCvted);
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
        return new AEFunction(mstrFuncName, listChildrenCvted);
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
