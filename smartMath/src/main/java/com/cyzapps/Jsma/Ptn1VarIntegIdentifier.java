/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class Ptn1VarIntegIdentifier {
    // this class is for normal expression patterns. Assume these patterns have only
    // one expression, one variable and limited number of pseudo-constants.
    public ABSTRACTEXPRPATTERNS menumAEPType = ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN;
    public String[] mstrarrayPatterns = new String[0]; // if there are several different way to express, then use alternative patterns, like sqrt(x) can be written as x**0.5, a+b*x can be written as a+x when b = 1
    public String[] mstrarrayPatternConditions = new String[0];
    public String[] mstrarrayPseudoConsts = new String[0];
    public String[] mstrarrayPCRestricts = new String[0];    //restricts of pseudo constant, if not match, throw exception
    public String[] mstrarrayPCConditions = new String[0];    //conditions of pseudo constant, if not match, return false
    public String mstrVariable = "";
    
    public String[] mstrarraySingularPnts = new String[0];  // limited number of singular points. A singular point should be a pseudo consts based expression
                                                            // (e.g. a/b) or a simple constant (e.g. 0) or a simple constants based expression (e.g. 1/2*pi).
    public String mstrExclIntegRanges = "";  // This should be an x based equation, like tan(x)==0. This implies more than one singular points
    public boolean mbIsDefiniteInteg = false;
    // mstrDefIntegEnd1 and 2 must be valid expression (ie cannot be "" or null) coz even if it is inf, it is still a range end.
    public String mstrDefIntegEnd1 = "";  // integration is compulsorily from or to. Only valid is mbIsDefiniteInteg is true.
    public String mstrDefIntegEnd2 = "";   // integration is compulsorily to or from. Only valid is mbIsDefiniteInteg is true.
    
    public String mstrIntegrated = "";
    
    public AbstractExpr[] maearrayPatterns = new AbstractExpr[0];
    public AEVar[] maearrayPseudoConsts = new AEVar[0];
    public AEVar maeVariable = new AEVar();
    
    public Ptn1VarIntegIdentifier() {
        
    }
    
    public Ptn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String[] strarrayPatterns, String[] strarrayPatternConditions, String[] strarrayPseudoConsts,
            String[] strarrayPCRestricts, String[] strarrayPCConditions, String strVariable, String[] strarraySingularPnts, String strExclIntegRanges,
            boolean bIsDefiniteInteg, String strDefIntegEnd1, String strDefIntegEnd2, String strIntegrated)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        setPtn1VarIntegIdentifier(enumAEPType, strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, strVariable,
                strarraySingularPnts, strExclIntegRanges, bIsDefiniteInteg, strDefIntegEnd1, strDefIntegEnd2, strIntegrated);
    }
    
    public final void setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS enumAEPType, String[] strarrayPatterns, String[] strarrayPatternConditions, String[] strarrayPseudoConsts,
            String[] strarrayPCRestricts, String[] strarrayPCConditions, String strVariable, String[] strarraySingularPnts, String strExclIntegRanges,
            boolean bIsDefiniteInteg, String strDefIntegEnd1, String strDefIntegEnd2, String strIntegrated)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        menumAEPType = enumAEPType;
        mstrarrayPatterns = strarrayPatterns;
        mstrarrayPatternConditions = strarrayPatternConditions;
        mstrarrayPseudoConsts = strarrayPseudoConsts;
        mstrarrayPCRestricts = strarrayPCRestricts;
        mstrarrayPCConditions = strarrayPCConditions;
        mstrVariable = strVariable;
        mstrarraySingularPnts = strarraySingularPnts;
        mstrExclIntegRanges = strExclIntegRanges;
        mbIsDefiniteInteg = bIsDefiniteInteg;
        mstrDefIntegEnd1 = strDefIntegEnd1;
        mstrDefIntegEnd2 = strDefIntegEnd2;
        mstrIntegrated = strIntegrated;
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
        maearrayPatterns = new AbstractExpr[mstrarrayPatterns.length];
        for (int idx = 0; idx < maearrayPatterns.length; idx ++) {
            maearrayPatterns[idx] = ExprAnalyzer.analyseExpression(mstrarrayPatterns[idx], new CurPos(), listPseudoConsts);
        }
        maeVariable = (AEVar) ExprAnalyzer.analyseExpression(mstrVariable, new CurPos()); // it must be a variable, so no need to add listPseudoConsts.
        UnknownVariable var = UnknownVarOperator.lookUpList(maeVariable.mstrVariableName, listUnknowns);
        if (var != null)    {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_REDECLARED);
        }
        listUnknowns.add(new UnknownVariable(maeVariable.mstrVariableName));
        
        // simplify most so that can easily compare.
        for (int idx = 0; idx < maearrayPatterns.length; idx ++) {
            maearrayPatterns[idx] = maearrayPatterns[idx].simplifyAExprMost(listUnknowns, new LinkedList<LinkedList<Variable>>(), new SimplifyParams(true, true, true));  // treat all unknowns as single value
        }
    }
}
