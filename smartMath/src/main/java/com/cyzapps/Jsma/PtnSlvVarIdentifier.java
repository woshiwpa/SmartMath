/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class PtnSlvVarIdentifier {
    // this class is for some very simple patterns. Assume these patterns have only
    // one expression, one variable and only one or two pseudo-constants.
    
   	public static final String FUNCTION_SINGLE_VAR_INVERTIBLE = "f_single_var_invertible";
   	public static final String FUNCTION_SINGLE_VAR_INVERTED = "f_single_var_inverted";

    static public boolean isSingleVarInvertibleFunc(String strFuncName) {
        String strFormattedFuncName = strFuncName.trim().toLowerCase(Locale.US);
        if (strFormattedFuncName.equals("acos") || strFormattedFuncName.equals("acosd") || strFormattedFuncName.equals("acosh")
                || strFormattedFuncName.equals("asin") || strFormattedFuncName.equals("asind") || strFormattedFuncName.equals("asinh")
                || strFormattedFuncName.equals("atan") || strFormattedFuncName.equals("atand") || strFormattedFuncName.equals("atanh")
                || strFormattedFuncName.equals("cos") || strFormattedFuncName.equals("cosd") || strFormattedFuncName.equals("cosh")
                || strFormattedFuncName.equals("sin") || strFormattedFuncName.equals("sind") || strFormattedFuncName.equals("sinh")
                || strFormattedFuncName.equals("tan") || strFormattedFuncName.equals("tand") || strFormattedFuncName.equals("tanh")
                || strFormattedFuncName.equals("exp") || strFormattedFuncName.equals("invert") || strFormattedFuncName.equals("left_recip")
                || strFormattedFuncName.equals("lg") || strFormattedFuncName.equals("ln") || strFormattedFuncName.equals("log")
                || strFormattedFuncName.equals("log10") || strFormattedFuncName.equals("log2") || strFormattedFuncName.equals("loge")
                || strFormattedFuncName.equals("recip") || strFormattedFuncName.equals("sqrt") || strFormattedFuncName.equals("todeg")
                 || strFormattedFuncName.equals("torad"))   {
            return true;
        }
        return false;
    }

    static public AbstractExpr getInvertedAExpr(String strFuncName, AbstractExpr aeParameter) throws JFCALCExpErrException, JSmartMathErrException {
        String strFormattedFuncName = strFuncName.trim().toLowerCase(Locale.US);
        AbstractExpr aeReturn = new AEFunction();
        LinkedList<AbstractExpr> listParameter = new LinkedList<AbstractExpr>();
        listParameter.add(aeParameter);
        if (strFormattedFuncName.equals("acos")) {
            aeReturn = new AEFunction("cos", listParameter);
        } else if (strFormattedFuncName.equals("acosd")) {
            aeReturn = new AEFunction("cosd", listParameter);
        } else if (strFormattedFuncName.equals("acosh")) {
            aeReturn = new AEFunction("cosh", listParameter);
        } else if (strFormattedFuncName.equals("asin")) {
            aeReturn = new AEFunction("sin", listParameter);
        } else if (strFormattedFuncName.equals("asind")) {
            aeReturn = new AEFunction("sind", listParameter);
        } else if (strFormattedFuncName.equals("asinh")) {
            aeReturn = new AEFunction("sinh", listParameter);
        } else if (strFormattedFuncName.equals("atan")) {
            aeReturn = new AEFunction("tan", listParameter);
        } else if (strFormattedFuncName.equals("atand")) {
            aeReturn = new AEFunction("tand", listParameter);
        } else if (strFormattedFuncName.equals("atanh")) {
            aeReturn = new AEFunction("tanh", listParameter);
        } else if (strFormattedFuncName.equals("cos")) {
            aeReturn = new AEFunction("acos", listParameter);
        } else if (strFormattedFuncName.equals("cosd")) {
            aeReturn = new AEFunction("acosd", listParameter);
        } else if (strFormattedFuncName.equals("cosh")) {
            aeReturn = new AEFunction("acosh", listParameter);
        } else if (strFormattedFuncName.equals("sin")) {
            aeReturn = new AEFunction("asin", listParameter);
        } else if (strFormattedFuncName.equals("sind")) {
            aeReturn = new AEFunction("asind", listParameter);
        } else if (strFormattedFuncName.equals("sinh")) {
            aeReturn = new AEFunction("asinh", listParameter);
        } else if (strFormattedFuncName.equals("tan")) {
            aeReturn = new AEFunction("atan", listParameter);
        } else if (strFormattedFuncName.equals("tand")) {
            aeReturn = new AEFunction("atand", listParameter);
        } else if (strFormattedFuncName.equals("tanh")) {
            aeReturn = new AEFunction("atanh", listParameter);
        } else if (strFormattedFuncName.equals("exp")) {
            aeReturn = new AEFunction("log", listParameter);
        } else if (strFormattedFuncName.equals("invert")) {
            aeReturn = new AEFunction("invert", listParameter);
        } else if (strFormattedFuncName.equals("left_recip")) {
            aeReturn = new AEFunction("recip", listParameter);
        } else if (strFormattedFuncName.equals("lg")) {
            aeReturn = new AEFunction("exp", listParameter);
        } else if (strFormattedFuncName.equals("ln")) {
            aeReturn = new AEFunction("exp", listParameter);
        } else if (strFormattedFuncName.equals("log")) {
            aeReturn = new AEFunction("exp", listParameter);
        } else if (strFormattedFuncName.equals("log10")) {
            AEConst aeTen = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TEN));
            aeReturn = new AEPowerOpt(aeTen, aeParameter);
        } else if (strFormattedFuncName.equals("log2")) {
            AEConst aeTwo = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO));
            aeReturn = new AEPowerOpt(aeTwo, aeParameter);
        } else if (strFormattedFuncName.equals("loge")) {
            aeReturn = new AEFunction("exp", listParameter);
        } else if (strFormattedFuncName.equals("recip")) {
            aeReturn = new AEFunction("left_recip", listParameter);
        } else if (strFormattedFuncName.equals("sqrt")) {
            AEConst aeTwo = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO));
            aeReturn = new AEPowerOpt(aeParameter, aeTwo);
        } else if (strFormattedFuncName.equals("todeg")) {
            aeReturn = new AEFunction("torad", listParameter);
        } else if (strFormattedFuncName.equals("torad"))   {
            aeReturn = new AEFunction("todeg", listParameter);
        }
        return aeReturn;
    }
		
	public ABSTRACTEXPRPATTERNS menumAEPType = ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN;
    public String mstrPattern = "";
    public String[] mstrarrayPseudoConsts = new String[0];
    public String[] mstrarrayPCRestricts = new String[0];    //restricts of pseudo constant, if not match, throw exception
    public String[] mstrarrayPCConditions = new String[0];    //conditions of pseudo constant, if not match, return false
    public String mstrToSolve = "";
    public String mstrSolveVarExpr = "";
    
    public AbstractExpr maePattern = AEInvalid.AEINVALID;
    public AEVar[] maearrayPseudoConsts = new AEVar[0];
    public AEVar maeToSolve = new AEVar();
    public AbstractExpr maeSolveVarExpr = AEInvalid.AEINVALID;
    
    public PtnSlvVarIdentifier() {
        
    }
    
    public PtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String strPattern, String[] strarrayPseudoConsts,
            String[] strarrayPCRestricts, String[] strarrayPCConditions, String strToSolve, String strSolveVarExpr)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        setPtnSlvVarIdentifier(enumAEPType, strPattern, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, strToSolve, strSolveVarExpr);
    }
    
    public void setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String strPattern, String[] strarrayPseudoConsts,
            String[] strarrayPCRestricts, String[] strarrayPCConditions, String strToSolve, String strSolveVarExpr)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        menumAEPType = enumAEPType;
        mstrPattern = strPattern;
        mstrarrayPseudoConsts = strarrayPseudoConsts;
        mstrarrayPCRestricts = strarrayPCRestricts;
        mstrarrayPCConditions = strarrayPCConditions;
        mstrToSolve = strToSolve;
        mstrSolveVarExpr = strSolveVarExpr;
        maearrayPseudoConsts = new AEVar[mstrarrayPseudoConsts.length];
        LinkedList<UnknownVariable> listUnknowns = new LinkedList<UnknownVariable>();
        LinkedList<Variable> listPseudoConsts = new LinkedList<Variable>();
        for (int idx = 0; idx < mstrarrayPseudoConsts.length; idx ++)   {
            maearrayPseudoConsts[idx] = (AEVar) ExprAnalyzer.analyseExpression(mstrarrayPseudoConsts[idx], new CurPos());
            UnknownVariable var = UnknownVarOperator.lookUpList(maearrayPseudoConsts[idx].mstrVariableName, listUnknowns);
            if (var != null)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_REDECLARED);
            }
            listUnknowns.add(new UnknownVariable(maearrayPseudoConsts[idx].mstrVariableName));
            listPseudoConsts.add(new Variable(maearrayPseudoConsts[idx].mstrVariableName));
        }
        maePattern = ExprAnalyzer.analyseExpression(mstrPattern, new CurPos(), listPseudoConsts);	
        maeToSolve = (AEVar) ExprAnalyzer.analyseExpression(mstrToSolve, new CurPos()); // it must be a variable, so no need to add listPseudoConsts.
        UnknownVariable var = UnknownVarOperator.lookUpList(maeToSolve.mstrVariableName, listUnknowns);
        if (var != null)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_REDECLARED);
        }
        listUnknowns.add(new UnknownVariable(maeToSolve.mstrVariableName));
        
        // simplify most so that can easily compare.
        maePattern = maePattern.simplifyAExprMost(listUnknowns, new LinkedList<LinkedList<Variable>>(), new SimplifyParams(false, true, false));
        
        maeSolveVarExpr = ExprAnalyzer.analyseExpression(mstrSolveVarExpr, new CurPos());
    }

	public boolean isPatternMatch(AbstractExpr aeOriginalExpr,  // need not to call replace pattern. Assume it has been simplified most.
								PatternExprUnitMap peuMap,	// this variable is used to return the map of unknown variable to pattern unit
                                DataClass datumValueOfUnknown,    // the data value which is used to return value of unknown variable.
                                LinkedList<UnknownVariable> listUnknown,    // listUnknown and lVarNameSpaces are original expression's unknown list and namespace, 
                                                                            // they are not solved result's unknown list and name space (which only include psconsts
                                                                            // defined patterns
								LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException
	{
        LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs = new LinkedList<PatternExprUnitMap>();
        LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts = new LinkedList<PatternExprUnitMap>();
        LinkedList<PatternExprUnitMap> listpeuMapUnknowns = new LinkedList<PatternExprUnitMap>();
        // do not allow conversion when matching pattern
        if (aeOriginalExpr.isPatternMatch(maePattern, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, false))   {
            // get the pattern! Assume there are at most one member in listpeuMapPseudoFuncs which is FUNCTION_SINGLE_VAR_INVERTIBLE
            // also assume there is only one unknown var in listpeuMapUnknowns.
            if (listpeuMapPseudoFuncs.size() > 1 || listpeuMapUnknowns.size() != 1)    {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN);
            }
            LinkedList<LinkedList<Variable>> lVarSpaces = new LinkedList<LinkedList<Variable>>();
            LinkedList<Variable> listKnownVars = new LinkedList<Variable>();
            lVarSpaces.add(listKnownVars);  // here name space and unknown list are psconst's name space and unknown list.
            LinkedList<UnknownVariable> listUnknownVars = new LinkedList<UnknownVariable>();
            for (int idx = 0; idx < listpeuMapPseudoConsts.size(); idx ++)  {
                DataClass datumValue = new DataClass();
                if (listpeuMapPseudoConsts.get(idx).maeExprUnit instanceof AEConst) {
                    datumValue = ((AEConst)listpeuMapPseudoConsts.get(idx).maeExprUnit).getDataClass(); // getDataClass will automatically determine when to deep copy.
                    Variable var = new Variable(((AEVar)listpeuMapPseudoConsts.get(idx).maePatternUnit).mstrVariableName,
                                                datumValue);
                    listKnownVars.add(var); // listpeuMapPseudoConsts does not include any duplicated pseudo-consts.
                } else  {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN);
                }
            }
            for (int idx = 0; idx < mstrarrayPCRestricts.length; idx ++)   {
                ExprEvaluator exprEvaluator = new ExprEvaluator(lVarSpaces);
                DataClass datumRestrictResult = exprEvaluator.evaluateExpression(mstrarrayPCRestricts[idx], new CurPos());
                if (datumRestrictResult.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                    AbstractExpr aexpr = datumRestrictResult.getAExpr();
                    aexpr = aexpr.simplifyAExprMost(listUnknown, lVarNameSpaces, new SimplifyParams(false, true, false));
                    if (aexpr instanceof AEConst) {
                        datumRestrictResult = ((AEConst)aexpr).getDataClassRef();
                    } else {
                        return false; // if result of restrict is an expression, we assume it cannot fit.
                    }
                }
                if (datumRestrictResult.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                    datumRestrictResult.changeDataType(DATATYPES.DATUM_BOOLEAN);
                    if (datumRestrictResult.getDataValue().isActuallyZero()) {   // restrict does not fit.
                        return false;
                    }
                } else {
                    return false; // if result of restrict is an expression, we assume it cannot fit.
                }
            }
            for (int idx = 0; idx < mstrarrayPCConditions.length; idx ++)   {
                ExprEvaluator exprEvaluator = new ExprEvaluator(lVarSpaces);
                DataClass datumConditionResult = exprEvaluator.evaluateExpression(mstrarrayPCConditions[idx], new CurPos());
                if (datumConditionResult.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                    AbstractExpr aexpr = datumConditionResult.getAExpr();
                    aexpr = aexpr.simplifyAExprMost(listUnknown, lVarNameSpaces, new SimplifyParams(false, true, false));
                    if (aexpr instanceof AEConst) {
                        datumConditionResult = ((AEConst)aexpr).getDataClassRef();
                    }
                }
                if (datumConditionResult.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                    datumConditionResult.changeDataType(DATATYPES.DATUM_BOOLEAN);
                    if (datumConditionResult.getDataValue().isActuallyZero()) {   // condition does not fit.
                        return false;
                    }
                }
            }
            peuMap.maeExprUnit = listpeuMapUnknowns.get(0).maeExprUnit;
            peuMap.maePatternUnit = listpeuMapUnknowns.get(0).maePatternUnit;
            AbstractExpr aeToSimplifyMost = maeSolveVarExpr;
            if (menumAEPType == ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCZERO
                    || menumAEPType == ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCADD
                    || menumAEPType == ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCSUB)   {
                if (aeToSimplifyMost instanceof AEFunction)  {
                    if (((AEFunction)aeToSimplifyMost).mlistChildren.size() != 1)   {
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_SIMPLE_PATTERN);
                    }
                    String strInvertibleFuncName = ((AEFunction)listpeuMapPseudoFuncs.get(0).maeExprUnit).mstrFuncName;
                    // strInvertibleFuncName has been validated in AEFunction.isPatternMatch and should must be one of
                    // the invertible functions.
                    aeToSimplifyMost = getInvertedAExpr(strInvertibleFuncName, ((AEFunction)aeToSimplifyMost).mlistChildren.get(0));
                } else  {
                    throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_SIMPLE_PATTERN);
                }
            } 
            // consider a situation aeToSimplifyMost = a where a is a psconst = something like a + 1 where a is an variable,
            // First a and second a are in different name space, have to simplify the expression step by step.
            aeToSimplifyMost = aeToSimplifyMost.simplifyAExprMost(listUnknownVars, lVarSpaces, new SimplifyParams(false, true, false));
            AbstractExpr aeReturn = aeToSimplifyMost.simplifyAExprMost(listUnknown, lVarNameSpaces, new SimplifyParams(false, false, false));
            if (aeReturn instanceof AEConst)    {
                datumValueOfUnknown.copyTypeValue(((AEConst)aeReturn).getDataClass());  // getDataClass will determine whether to deep copy or not.
                return true;
            } else  {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_UNSUPPORTED_SIMPLE_PATTERN);
            }
        }
        return false;
	}
}
