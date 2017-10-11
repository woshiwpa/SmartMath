/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jmfp;

import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.Statement.Statement_slvreto;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AECompare;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AEPosNegOpt;
import com.cyzapps.Jsma.AEVar;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.AbstractExpr.SimplifyParams;
import com.cyzapps.Jsma.ExprAnalyzer;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.PatternManager;
import com.cyzapps.Jsma.PtnSlvMultiVarsIdentifier;
import com.cyzapps.Jsma.PtnSlvVarMultiRootsIdentifier;
import com.cyzapps.Jsma.SMErrProcessor;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class SolveAnalyzer {
	
    static public PatternManager mspm = null;
    
    public SolveAnalyzer() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException    {
        if (mspm == null)   {
            mspm = new PatternManager();
            mspm.loadPatterns(13);
        }
    }
            
	public int analyzeSolve(Statement[] sarrayLines,	// statements of this script
			int nDeclarationPosition,	// The position of block declaration in sarrayLines 
			LinkedList<UnknownVariable> listVarUnknown,	// The unknown variable list
			LinkedList<LinkedList<Variable>> lVarNameSpaces)	// variable name space
			throws
			InterruptedException,	// sleep function may throw Interrupted Exception
            JMFPCompErrException,
            JFCALCExpErrException,
            JSmartMathErrException	{
        Statement sLine = sarrayLines[nDeclarationPosition];
        if (sLine.m_statementType.m_strType.equals("solve") == false)  {
            ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
            throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);												
        }
        
        int nReturnLineIndex = nDeclarationPosition;
        Variable varSlvReToVar = null;
        LinkedList<LinkedList<UnknownVariable>> listAllResultSets = new LinkedList<LinkedList<UnknownVariable>>();
        listAllResultSets.add(listVarUnknown);
        LinkedList<LinkedList<LinkedList<Variable>>> listAllVarSpacesSets = new LinkedList<LinkedList<LinkedList<Variable>>>();
        listAllVarSpacesSets.add(lVarNameSpaces);
        LinkedList<LinkedList<AbstractExpr>> listAllAExprs = new LinkedList<LinkedList<AbstractExpr>>();
        listAllAExprs.add(new LinkedList<AbstractExpr>());   
        boolean bInHelpBlock = false;
        for (int idx = nDeclarationPosition + 1; idx < sarrayLines.length; idx ++)  {
            sLine = sarrayLines[idx];
            if (sLine.m_statementType.m_strType.equals("help")) {
                bInHelpBlock = true;
            } else if (sLine.m_statementType.m_strType.equals("endh"))  {
                if (bInHelpBlock == false)  {
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);
                } else  {
                    bInHelpBlock = false;
                }
            }
            if (bInHelpBlock == false)  {
                if (sLine.m_statementType.m_strType.equals("slvreto"))   {
                    String strSlvReToVarName = ((Statement.Statement_slvreto)(sLine.m_statementType)).m_strReturnedVar;
                    if (strSlvReToVarName.length() > 0)    {
                        varSlvReToVar = VariableOperator.lookUpSpaces(strSlvReToVarName, lVarNameSpaces);
                        if (varSlvReToVar == null)    {
                            ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
                            throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);
                        }                        
                    }
                    nReturnLineIndex = idx;
                    break;
                } else if (sLine.getStatement().equals("")) {
                    continue;   // empty statement
                } else if (sLine.m_statementType.m_strType.equals("_expression_"))  {
                    LinkedList<LinkedList<UnknownVariable>> listNewAllResultSets = new LinkedList<LinkedList<UnknownVariable>>();
                    LinkedList<LinkedList<LinkedList<Variable>>> listNewAllVarSpacesSets = new LinkedList<LinkedList<LinkedList<Variable>>>();
                    LinkedList<LinkedList<AbstractExpr>> listNewAllAExprs = new LinkedList<LinkedList<AbstractExpr>>();
                    for (int idx1 = 0; idx1 < listAllResultSets.size(); idx1 ++)    {
                        /* evaluate the expression */
                        CurPos curpos = new CurPos();
                        curpos.m_nPos = 0;
                        AbstractExpr aexpr = ExprAnalyzer.analyseExpression(sLine.getStatement(), curpos);
                        try {
                            // shouldn't use exprEvaluator.evaluateExpression coz this function cannot set unknown var assigned.
                            aexpr = aexpr.simplifyAExprMost(listAllResultSets.get(idx1), listAllVarSpacesSets.get(idx1), new SimplifyParams(false, true, false));
                        } catch (JFCALCExpErrException e)   {   // divided by zero exception etc can be allowed.
                            if (e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_DIVISOR_CAN_NOT_BE_ZERO
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_INDEFINITE_RESULT
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_CHANGE_FROM_DOUBLE_TO_INTEGER_OVERFLOW
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_DOUBLE_OVERFLOW
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_INTEGER_OVERFLOW
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_PARAMETER_NOT_MATCH
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE
                                    || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_WRONG_INDEX) {
                                // this means the expression can not be simplified. It should be excluded.
                                continue;
                            } else  {
                                throw e;
                            }
                        }

                        if (!(aexpr instanceof AECompare) || ((AECompare)aexpr).moptType != OPERATORTYPES.OPERATOR_EQ)    {
                            // this means we need not to use solver
                            listNewAllResultSets.add(listAllResultSets.get(idx1));
                            listNewAllVarSpacesSets.add(listAllVarSpacesSets.get(idx1));
                            listNewAllAExprs.add(listAllAExprs.get(idx1));
                            continue;
                        }
                        // move the part right to == to left and constract a pos-neg opt aexpr.
                        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                        listChildren.add(((AECompare)aexpr).maeLeft);
                        listChildren.add(((AECompare)aexpr).maeRight);
                        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                        aexpr = new AEPosNegOpt(listChildren, listOpts);

                        UnknownVariable varSolved = new UnknownVariable();
                        // apply pattern identifiers to individual expressions
                        aexpr = mspm.solveVarByPtnSlvVarIdentifier(aexpr, varSolved, listAllResultSets.get(idx1), listAllVarSpacesSets.get(idx1));
                        if (!varSolved.isValueAssigned())    {  // unknown variable is not solved
                            try {
                                AbstractExpr[] aexprs = new AbstractExpr[1];
                                aexprs[0] = aexpr;
                                LinkedList<LinkedList<Variable>> lVarSpacesCpy = VariableOperator.cloneVarSpaces(listAllVarSpacesSets.get(idx1));
                                LinkedList<LinkedList<UnknownVariable>> listThisResultSets
                                        = solveExprVars(mspm, aexprs, listAllResultSets.get(idx1), lVarSpacesCpy);
                                listNewAllResultSets.addAll(listThisResultSets);
                                LinkedList<AbstractExpr> listAEs = listAllAExprs.get(idx1);
                                for (int idx2 = 0; idx2 < listThisResultSets.size(); idx2 ++)   {
                                    listNewAllAExprs.add(new LinkedList<AbstractExpr>());
                                    listNewAllAExprs.getLast().addAll(listAEs);
                                    lVarSpacesCpy = VariableOperator.cloneVarSpaces(listAllVarSpacesSets.get(idx1));
                                    UnknownVarOperator.mergeUnknowns2VarSpaces(listThisResultSets.get(idx2), lVarSpacesCpy);
                                    listNewAllVarSpacesSets.add(lVarSpacesCpy);
                                }
                           } catch (JSmartMathErrException e)  {
                                if (e.m_se.m_enumErrorType == SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN) {
                                    listNewAllResultSets.add(listAllResultSets.get(idx1));
                                    listAllAExprs.get(idx1).add(aexpr);
                                    listNewAllAExprs.add(listAllAExprs.get(idx1));
                                    listNewAllVarSpacesSets.add(listAllVarSpacesSets.get(idx1));
                                } else  {
                                    throw e;
                                }
                            }
                        } else  {   // unknown variable is solved
                            listNewAllResultSets.add(listAllResultSets.get(idx1));
                            listNewAllVarSpacesSets.add(listAllVarSpacesSets.get(idx1));
                            listNewAllAExprs.add(listAllAExprs.get(idx1));
                        }
                    }
                    listAllResultSets = listNewAllResultSets;
                    listAllAExprs = listNewAllAExprs;
                    listAllVarSpacesSets = listNewAllVarSpacesSets;
                }
            }
        }
        LinkedList<LinkedList<UnknownVariable>> listSlvReToResultSets = new LinkedList<LinkedList<UnknownVariable>>();
        for (int idx0 = 0; idx0 < listAllResultSets.size(); idx0 ++)    {
            // apply pattern identifiers to individual group of expressions
            AbstractExpr[] aeOriginalExprs = new AbstractExpr[0];
            if (listAllAExprs.get(idx0).size() == 0)    {
                listSlvReToResultSets.add(listAllResultSets.get(idx0));
            } else	{   // there are some expressions need to solve
                LinkedList<LinkedList<UnknownVariable>> listGroupResultSets = new LinkedList<LinkedList<UnknownVariable>>();
                listGroupResultSets.add(listAllResultSets.get(idx0));
                LinkedList<LinkedList<AbstractExpr>> listGroupAExprs = new LinkedList<LinkedList<AbstractExpr>>();
                listGroupAExprs.add(new LinkedList<AbstractExpr>());   
                // apply pattern analyzers to a group of expressions here
                aeOriginalExprs = mspm.simplifyByPtnSlvVarIdentifier(listAllAExprs.get(idx0).toArray(new AbstractExpr[0]), listGroupResultSets.get(0), listAllVarSpacesSets.get(idx0));
                if (aeOriginalExprs.length != 0)    {
                    for (int idx = 0; idx < aeOriginalExprs.length; idx ++) {
                        LinkedList<LinkedList<UnknownVariable>> listNewGroupResultSets = new LinkedList<LinkedList<UnknownVariable>>();
                        LinkedList<LinkedList<AbstractExpr>> listNewGroupAExprs = new LinkedList<LinkedList<AbstractExpr>>();
                        for (int idx1 = 0; idx1 < listGroupResultSets.size(); idx1 ++)    {
                            try {
                                // have to clone listAllResultSets.get(idx1) because if solve unsuccessful, we need to save the unknown list
                                // in listNewAllResultSets. However, solveExprVars may change this variable.
                                LinkedList<UnknownVariable> listUnknownCpy = UnknownVarOperator.cloneUnknownVarList(listGroupResultSets.get(idx1));
                                LinkedList<LinkedList<Variable>> lVarSpacesCpy = VariableOperator.cloneVarSpaces(listAllVarSpacesSets.get(idx0));
                                UnknownVarOperator.mergeUnknowns2VarSpaces(listUnknownCpy, lVarSpacesCpy);
                                AbstractExpr[] arrayaeThisExpr = new AbstractExpr[1];
                                arrayaeThisExpr[0] = aeOriginalExprs[idx];
                                LinkedList<LinkedList<UnknownVariable>> listThisResultSets =
                                        solveExprVars(mspm, arrayaeThisExpr, listUnknownCpy, lVarSpacesCpy);
                                listNewGroupResultSets.addAll(listThisResultSets);
                                for (int idx2 = 0; idx2 < listThisResultSets.size(); idx2 ++)   {
                                    listNewGroupAExprs.add(new LinkedList<AbstractExpr>());
                                    listNewGroupAExprs.getLast().addAll(listGroupAExprs.get(idx1));
                                }
                            } catch (JSmartMathErrException e)  {
                                if (e.m_se.m_enumErrorType != SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN) {
                                    throw e;
                                } else  {
                                    listNewGroupResultSets.add(listGroupResultSets.get(idx1));
                                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                                    listAExprs.addAll(listGroupAExprs.get(idx1));
                                    listAExprs.add(aeOriginalExprs[idx]);
                                    listNewGroupAExprs.add(listAExprs);
                                }
                            }
                        }
                        listGroupResultSets = listNewGroupResultSets;
                        listGroupAExprs = listNewGroupAExprs;
                    }
                    LinkedList<LinkedList<UnknownVariable>> listLastGroupResultSets = listGroupResultSets;
                    listGroupResultSets = new LinkedList<LinkedList<UnknownVariable>>();
                    for (int idx = 0; idx < listLastGroupResultSets.size(); idx ++)    {
                        if (listGroupAExprs.get(idx).size() == 0) {
                            listGroupResultSets.add(listLastGroupResultSets.get(idx));
                            continue;   // no need to solve
                        }
                        try {
                            LinkedList<UnknownVariable> listUnknownCpy = listLastGroupResultSets.get(idx);    // no need to clone because of final use
                            // need to clone coz one varnamespaces in listAllVarSpacesSets corresponds to a number of solveExprVars.
                            LinkedList<LinkedList<Variable>> lVarSpacesCpy = VariableOperator.cloneVarSpaces(listAllVarSpacesSets.get(idx0));
                            UnknownVarOperator.mergeUnknowns2VarSpaces(listUnknownCpy, lVarSpacesCpy);
                            LinkedList<AbstractExpr> listGroupAETmp = new LinkedList<AbstractExpr>();
                            listGroupAETmp.addAll(listGroupAExprs.get(idx));
                            listGroupAExprs.get(idx).clear();   // first clear it, then add aexprs back one by one.
                            for (int idx1 = 0; idx1 < listGroupAETmp.size(); idx1 ++) {
                                try {
                                    // shouldn't use exprEvaluator.evaluateExpression coz this function cannot set unknown var assigned.
                                    AbstractExpr aexpr = listGroupAETmp.get(idx1).simplifyAExprMost(listUnknownCpy, lVarSpacesCpy, new SimplifyParams(false, true, false));
                                    if (!(aexpr instanceof AEConst))   {    // if its not a constant, we need to solve the expression.
                                        listGroupAExprs.get(idx).add(aexpr);
                                    }
                                } catch (JFCALCExpErrException e)   {   // divided by zero exception etc can be allowed.
                                    if (e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_DIVISOR_CAN_NOT_BE_ZERO
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_INDEFINITE_RESULT
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_CHANGE_FROM_DOUBLE_TO_INTEGER_OVERFLOW
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_DOUBLE_OVERFLOW
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_INTEGER_OVERFLOW
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_PARAMETER_NOT_MATCH
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE
                                            || e.m_se.m_enumErrorType == ErrProcessor.ERRORTYPES.ERROR_WRONG_INDEX) {
                                        // this means the expression can not be simplified. It should NOT be excluded so that we can see NULL roots.
                                        listGroupAExprs.get(idx).add(listGroupAETmp.get(idx1));
                                    } else  {
                                        throw e;
                                    }
                                }
                            }
                            LinkedList<LinkedList<UnknownVariable>> listNewGroupResultSets
                                    = solveExprVars(mspm, listGroupAExprs.get(idx).toArray(new AbstractExpr[0]), listUnknownCpy, lVarSpacesCpy);
                            listGroupResultSets.addAll(listNewGroupResultSets);
                        } catch (JSmartMathErrException e)  {
                            if (e.m_se.m_enumErrorType != SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN) {
                                throw e;
                            } else  {
                                // fail to solve
                                listGroupResultSets.add(listLastGroupResultSets.get(idx));
                            }
                        }
                    }

                }
                for (int idx = 0; idx < listGroupResultSets.size(); idx ++)  {
                    boolean bHasSolvedVar = false;
                    for (int idx1 = 0; idx1 < listGroupResultSets.get(idx).size(); idx1 ++)   {
                        if (listGroupResultSets.get(idx).get(idx1).isValueAssigned()) {
                            bHasSolvedVar = true;
                            break;
                        }
                    }
                    if (bHasSolvedVar != false) {
                        listSlvReToResultSets.add(listGroupResultSets.get(idx));
                    }
                }
            }
        }
            
        if (nReturnLineIndex == nDeclarationPosition)   {
            ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
            throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);
        }
        
        if (listSlvReToResultSets.size() != 0)  {   /* do not worry about unsolved pattern, return to value will tell user */
            for (int idx = 0; idx < listSlvReToResultSets.get(0).size(); idx ++)  {
                if (listSlvReToResultSets.get(0).get(idx).isValueAssigned())    {
                    // values in lVarNameSpaces will be automatically changed.
                    listVarUnknown.get(idx).setValue(listSlvReToResultSets.get(0).get(idx).getSolvedValue());
                }
            }
        }

        DataClass datumSlvReTo = new DataClass();
        DataClass[] arrayDataList = new DataClass[listSlvReToResultSets.size()];
        for (int idx = 0; idx < listSlvReToResultSets.size(); idx ++)    {
            DataClass datumOneSolvedVars = new DataClass();
            DataClass[] arraySolvedValues = new DataClass[listSlvReToResultSets.get(idx).size()];
            for (int idx1 = 0; idx1 < listSlvReToResultSets.get(idx).size(); idx1 ++)    {
                if (listSlvReToResultSets.get(idx).get(idx1).isValueAssigned())  {
                    arraySolvedValues[idx1] = listSlvReToResultSets.get(idx).get(idx1).getSolvedValue().cloneSelf();
                } else  {
                    arraySolvedValues[idx1] = new DataClass();  // NULL means value is not solved.
                }
            }
            datumOneSolvedVars.setDataList(arraySolvedValues);
            arrayDataList[idx] = datumOneSolvedVars;
        }
        datumSlvReTo.setDataList(arrayDataList);
        
        Statement sSlvReToLine = sarrayLines[nReturnLineIndex];
        String strReturnedVarName = ((Statement_slvreto)(sSlvReToLine.m_statementType)).m_strReturnedVar;
        Variable varSlvReTo = VariableOperator.lookUpSpaces(strReturnedVarName, lVarNameSpaces);
        if (varSlvReTo != null)  {
            varSlvReTo.setValue(datumSlvReTo);
        }
        return nReturnLineIndex;
    }
		
    public static DataClass[] solveExprVarMultiRoots(PatternManager pm, AbstractExpr aeOriginalExpr,
                                                                LinkedList<PatternExprUnitMap> listPEUMap,
                                                                LinkedList<UnknownVariable> listUnknowns,
                                                                LinkedList<LinkedList<Variable>> lVarNameSpaces)
                                                                throws InterruptedException,
                                                                JMFPCompErrException,
                                                                JFCALCExpErrException,
                                                                JSmartMathErrException	{
        
		// apply patterns here
		boolean bIsAllVarsAEVar = true;
		// the list of pseudo const vars in the pattern identifier.
        LinkedList<UnknownVariable> listPseudoConstVars = new LinkedList<UnknownVariable>();
        // need not to deep copy listUnknowns and lVarNameSpaces because if a valid pi is returned,
        // changes of listUnknowns and lVarNameSpaces are desired.
        PtnSlvVarMultiRootsIdentifier pa = pm.findPtnSlvVarMultiRootsIdentifierToMatch(aeOriginalExpr,
                                                    listPEUMap,
                                                    listPseudoConstVars,
                                                    listUnknowns,
                                                    lVarNameSpaces);
        if (pa.menumAEPType == ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN
                || listPEUMap.size() != 1)	{
            return new DataClass[0];    // cannot recognize.
        }
        DataClass[] datumarrayResults = pa.solveOriginalExprUnit(listPEUMap.getFirst(), listPseudoConstVars);
        return datumarrayResults;
    }
    
    public static LinkedList<LinkedList<UnknownVariable>> solveExprVars(PatternManager pm, AbstractExpr[] aeOriginalExprs,
                                                                LinkedList<UnknownVariable> listUnknowns,
                                                                LinkedList<LinkedList<Variable>> lVarNameSpaces)
                                                                throws InterruptedException,
                                                                JMFPCompErrException,
                                                                JFCALCExpErrException,
                                                                JSmartMathErrException	{
		// apply patterns here
		boolean bIsAllVarsAEVar = true;
		LinkedList<PatternExprUnitMap> listPEUMap = new LinkedList<PatternExprUnitMap>();
        // the list of pseudo const vars in the pattern identifier.
        LinkedList<UnknownVariable> listPseudoConstVars = new LinkedList<UnknownVariable>();
        // need not to deep copy listUnknowns and lVarNameSpaces because if a valid pi is returned,
        // changes of listUnknowns and lVarNameSpaces are desired.
        PtnSlvMultiVarsIdentifier pa = pm.findPtnSlvMultiVarsIdentifierToMatch(aeOriginalExprs,
                                                    listPEUMap,
                                                    listPseudoConstVars,
                                                    listUnknowns,
                                                    lVarNameSpaces);
        if (pa.menumAEPType == ABSTRACTEXPRPATTERNS.AEP_UNRECOGNIZEDPATTERN)	{
            throw new JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN);
        }
        listPseudoConstVars.addAll(listUnknowns);
        LinkedList<DataClass[]> listResults = pa.solveOriginalExprUnits(listPEUMap, listPseudoConstVars);
        LinkedList<DataClass[]> listAllPossibleResultSets = new LinkedList<DataClass[]>();
        if (listResults.size() != listPEUMap.size())	{
            throw new JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_VARIABLE_CANNOT_BE_SOLVED);
        } else if (listPEUMap.size() != 0)	{
            DataClass[] datumResultList = new DataClass[listPEUMap.size()];
            listAllPossibleResultSets.add(datumResultList);
            for (int idx = 0; idx < listPEUMap.size(); idx ++)	{
                LinkedList<DataClass[]> listOneVarPossibleResultSets = new LinkedList<DataClass[]>();
                for (int idx2 = 0; idx2 < listAllPossibleResultSets.size(); idx2 ++)	{
                    DataClass[] datum1VarList = listResults.get(idx);
                    for (int idx4 = 0; idx4 < datum1VarList.length; idx4 ++)	{
                        if (listAllPossibleResultSets.get(idx2)[idx] == null)	{
                            listAllPossibleResultSets.get(idx2)[idx] = new DataClass();
                            listAllPossibleResultSets.get(idx2)[idx].copyTypeValueDeep(datum1VarList[idx4]);
                        } else	{
                            DataClass[] dataResultListNewCpy = new DataClass[listPEUMap.size()];
                            for (int idx3 = 0; idx3 < listPEUMap.size(); idx3 ++)	{
                                if (listAllPossibleResultSets.get(idx2)[idx3] != null && idx3 != idx)	{
                                    dataResultListNewCpy[idx3] = new DataClass();
                                    dataResultListNewCpy[idx3].copyTypeValueDeep(listAllPossibleResultSets.get(idx2)[idx3]);
                                }
                            }
                            dataResultListNewCpy[idx] = new DataClass();
                            dataResultListNewCpy[idx].copyTypeValueDeep(datum1VarList[idx4]);
                            listOneVarPossibleResultSets.add(dataResultListNewCpy);
                        }
                    }

                }
                listAllPossibleResultSets.addAll(listOneVarPossibleResultSets);
            }
            /*
            for (int idx = 0; idx < listAllPossibleResultSets.size(); idx ++)	{
                for (int idx1 = 0; idx1 < listPEUMap.size(); idx1 ++)	{
                    AbstractExpr aeUnit = listPEUMap.get(idx).maeExprUnit;
                    String strSingleValueOutput = mMFPAdapter.outputDatum(listAllPossibleResultSets.get(idx)[idx1])[1];
                    strOutput += aeUnit.output() + " == " + strSingleValueOutput + "\n";
                }
            }*/
        }

        bIsAllVarsAEVar = true;
        for (int idx = 0; idx < listPEUMap.size(); idx ++)	{
            if (!(listPEUMap.get(idx).maeExprUnit instanceof AEVar))	{
                bIsAllVarsAEVar = false;
                break;
            }
        }
        
        LinkedList<LinkedList<UnknownVariable>> listAllResultSets = new LinkedList<LinkedList<UnknownVariable>>();
        if (!bIsAllVarsAEVar)	{
            AbstractExpr[] aeOriginalExprsNew = new AbstractExpr[listPEUMap.size()];
            LinkedList<AbstractExpr[]> listAllPossibleGroups = new LinkedList<AbstractExpr[]>();
            listAllPossibleGroups.add(aeOriginalExprsNew);
            for (int idx = 0; idx < listPEUMap.size(); idx ++)	{
                LinkedList<AbstractExpr[]> listOneVarPossibleGroups = new LinkedList<AbstractExpr[]>();
                for (int idx3 = 0; idx3 < listAllPossibleGroups.size(); idx3 ++)	{
                    LinkedList<DataClass> listIdenticalResults = new LinkedList<DataClass>();
                    for (int idx1 = 0; idx1 < listResults.get(idx).length; idx1 ++)	{
                        boolean bIsRedundant = false;
                        for (int idx2 =idx1 + 1; idx2 < listResults.get(idx).length; idx2 ++)	{
                            if (listResults.get(idx)[idx1].isEqual(listResults.get(idx)[idx2]))	{
                                bIsRedundant = true;
                                break;
                            }
                        }
                        if (!bIsRedundant)	{
                            listIdenticalResults.add(listResults.get(idx)[idx1]);
                        }
                    }
                    for (int idx1 = 0; idx1 < listIdenticalResults.size(); idx1 ++)	{
                        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                        listChildren.add(listPEUMap.get(idx).maeExprUnit);
                        listChildren.add(new AEConst(listIdenticalResults.get(idx1)));
                        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
                        if (listAllPossibleGroups.get(idx3)[idx] == null)	{
                            listAllPossibleGroups.get(idx3)[idx] = new AEPosNegOpt(listChildren, listOpts);
                        } else	{
                            AbstractExpr[] aeOriginalExprsNewCpy = new AbstractExpr[listPEUMap.size()];
                            for (int idx2 = 0; idx2 < listPEUMap.size(); idx2 ++)	{
                                if (listAllPossibleGroups.get(idx3)[idx2] != null && idx2 != idx)	{
                                    aeOriginalExprsNewCpy[idx2] = listAllPossibleGroups.get(idx3)[idx2];
                                }
                            }
                            aeOriginalExprsNewCpy[idx] = new AEPosNegOpt(listChildren, listOpts);
                            listOneVarPossibleGroups.add(aeOriginalExprsNewCpy);
                        }
                    }
                }
                listAllPossibleGroups.addAll(listOneVarPossibleGroups);
            }
            for (int idx = 0; idx < listAllPossibleGroups.size(); idx ++)	{
                // avoid interaction between different roots, we have to clone variable list and namespaces.
                LinkedList<UnknownVariable> listUnknownsCpy = UnknownVarOperator.cloneUnknownVarList(listUnknowns);
                LinkedList<LinkedList<Variable>> lVarNameSpacesCpy = VariableOperator.cloneVarSpaces(lVarNameSpaces);
                LinkedList<Variable> listVarLocal = new LinkedList<Variable>();
                if (lVarNameSpacesCpy.size() > 0)  {
                    listVarLocal = lVarNameSpacesCpy.get(0);
                } else  {
                    lVarNameSpacesCpy.add(listVarLocal);
                }
                // first of all, move all the solved var from list unknown to var name space
                for (int idx1 = 0; idx1 < listUnknownsCpy.size(); idx1 ++)  {
                    if (listUnknownsCpy.get(idx1).isValueAssigned())  {
                        String strVarName = listUnknownsCpy.get(idx1).getName();
                        Variable var = VariableOperator.lookUpSpaces(strVarName, lVarNameSpacesCpy);
                        if (var == null)    {
                            var = new Variable(listUnknownsCpy.get(idx1).getName(), listUnknownsCpy.get(idx1).getSolvedValue());
                            listVarLocal.add(var);
                            listUnknownsCpy.remove(idx1);
                            idx1 --;
                        }
                    }
                }
                // then try to identify the pattern
                aeOriginalExprsNew = pm.simplifyByPtnSlvVarIdentifier(listAllPossibleGroups.get(idx), listUnknownsCpy, lVarNameSpacesCpy);
                if (aeOriginalExprsNew.length == 0)    {
                    // need not to analyze the pattern.
                    listAllResultSets.add(listUnknownsCpy);
                } else if (AbstractExpr.isExprsEqual(aeOriginalExprs, aeOriginalExprsNew))  {
                    // listAllPossibleGroups.get(idx) is the same as aeOriginalExprs, this means
                    // infinite loop.
                    // throw new JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN);
                    listAllResultSets.add(listUnknownsCpy);
                } else  {
                    // then try to analyze the pattern
                    LinkedList<LinkedList<UnknownVariable>> listThisResultSets
                            = solveExprVars(pm, aeOriginalExprsNew, listUnknownsCpy, lVarNameSpacesCpy);
                    listAllResultSets.addAll(listThisResultSets);
                }
            }
        } else if (listAllPossibleResultSets.size() > 0) {
            for (int idx = 0; idx < listAllPossibleResultSets.size(); idx ++)   {
                LinkedList<UnknownVariable> listUnknownsTmp = new LinkedList<UnknownVariable>();
                for (int idx1 = 0; idx1 < listUnknowns.size(); idx1 ++) {
                    UnknownVariable var = new UnknownVariable(listUnknowns.get(idx1).getName(), listUnknowns.get(idx1).getValue());
                    var.setValueAssigned(listUnknowns.get(idx1).isValueAssigned());
                    for (int idx2 = 0; idx2 < listPEUMap.size(); idx2 ++)   {
                        if (((AEVar)listPEUMap.get(idx2).maeExprUnit).mstrVariableName
                                .equalsIgnoreCase(var.getName()))	{
                            var.setValue( listAllPossibleResultSets.get(idx)[idx2]);
                        }
                    }
                    listUnknownsTmp.add(var);
                }
                listAllResultSets.add(listUnknownsTmp);
            }
        }
        return listAllResultSets;
	}


    public static LinkedList<AbstractExpr> solveVarInSingleExpr(AbstractExpr aexpr, String strVariable,
            LinkedList<UnknownVariable> listUnknownVars, LinkedList<LinkedList<Variable>> llistNameSpace, boolean bIgnoreMatrixDim, int nStackLen)
            throws JSmartMathErrException, JFCALCExpErrException, InterruptedException, JMFPCompErrException {
        if (!(aexpr instanceof AECompare) || ((AECompare)aexpr).moptType != OPERATORTYPES.OPERATOR_EQ || nStackLen > 24) {
            throw new JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_CANNOT_SOLVE_CALCULATION); // will exit if not equation or stack len > 24.
        }
        if (((AECompare)aexpr).maeLeft instanceof AEConst
                && ((AEConst)((AECompare)aexpr).maeLeft).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                && ((AEConst)((AECompare)aexpr).maeLeft).getDataClassRef().isZeros(false)) {
            aexpr = ((AECompare)aexpr).maeRight;
        } else if (((AECompare)aexpr).maeRight instanceof AEConst
                && ((AEConst)((AECompare)aexpr).maeRight).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                && ((AEConst)((AECompare)aexpr).maeRight).getDataClassRef().isZeros(false)) {
            aexpr = ((AECompare)aexpr).maeLeft;
        } else {
            // move the part right to == to left and constract a pos-neg opt aexpr.
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(((AECompare)aexpr).maeLeft);
            listChildren.add(((AECompare)aexpr).maeRight);
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
            aexpr = new AEPosNegOpt(listChildren, listOpts);
        }

        LinkedList<String> listNotConvert = new LinkedList<String>();
        listNotConvert.add(strVariable);
        LinkedList<String> listConverted = new LinkedList<String>();
        aexpr = aexpr.convertAEVar2AExprDatum(listNotConvert, true, listConverted);
                
        aexpr = aexpr.simplifyAExprMost(listUnknownVars, llistNameSpace, new SimplifyParams(bIgnoreMatrixDim, true, false));

        // apply pattern identifiers to individual expressions
        UnknownVariable varSolved = new UnknownVariable();
        try {
            aexpr = mspm.solveVarByPtnSlvVarIdentifier(aexpr, varSolved, listUnknownVars, llistNameSpace);
        } catch (JSmartMathErrException e) {
            // if there is an exception, continue.
        }
        
        LinkedList<AbstractExpr> listResults = new LinkedList<AbstractExpr>();
        if (!varSolved.isValueAssigned())    {  // unknown variable is not solved
            DataClass[] datumarrayResults = new DataClass[0];
            try {
                LinkedList<PatternExprUnitMap> listPEUMap = new LinkedList<PatternExprUnitMap>();
                datumarrayResults = solveExprVarMultiRoots(mspm, aexpr, listPEUMap, listUnknownVars, llistNameSpace);
                if (datumarrayResults.length == 0 || listPEUMap.size() != 1) {
                    return new LinkedList<AbstractExpr>();
                } else if (listPEUMap.getFirst().maeExprUnit instanceof AEVar) {
                    if (!((AEVar)listPEUMap.getFirst().maeExprUnit).mstrVariableName.equalsIgnoreCase(strVariable)) {
                        throw new JSmartMathErrException(SMErrProcessor.ERRORTYPES.ERROR_INVALID_RESULT);
                    }
                    // the to be solved variable is a variable, so it is solved.
                    for (int idx2 = 0; idx2 < datumarrayResults.length; idx2 ++) {
                        DataClass datum = datumarrayResults[idx2];
                        if (datum.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                            AbstractExpr aexprResult = datum.getAExpr().simplifyAExprMost(listUnknownVars, llistNameSpace, new SimplifyParams(bIgnoreMatrixDim, true, false));
                            listResults.add(aexprResult);
                        } else {
                            listResults.add(new AEConst(datum));
                        }
                    }
                    return listResults;
                } else {
                    // the solved expression is not for the variable but for the variable's parent.
                    for (int idx = 0; idx < datumarrayResults.length; idx ++) {
                        LinkedList<UnknownVariable> listUnknownsCpy = UnknownVarOperator.cloneUnknownVarList(listUnknownVars);
                        LinkedList<LinkedList<Variable>> lVarNameSpacesCpy = VariableOperator.cloneVarSpaces(llistNameSpace);
                        try {
                            AbstractExpr aexprResult = AEInvalid.AEINVALID;
                            if (datumarrayResults[idx].getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                                aexprResult = datumarrayResults[idx].getAExpr();
                            } else {
                                aexprResult = new AEConst(datumarrayResults[idx]);
                            }
                            AECompare aeNewExpr = new AECompare(listPEUMap.getFirst().maeExprUnit, OPERATORTYPES.OPERATOR_EQ, aexprResult);
                            LinkedList<AbstractExpr> listThisResults = solveVarInSingleExpr(aeNewExpr, strVariable, listUnknownsCpy, lVarNameSpacesCpy, bIgnoreMatrixDim, (++nStackLen));
                            listResults.addAll(listThisResults);
                        } catch (JSmartMathErrException e)  {
                            if (e.m_se.m_enumErrorType == SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN) {
                                // do nothing if is just unrecognized pattern.
                            } else  {
                                throw e;
                            }
                        }
                    }
                    return listResults;
                }
            } catch (JSmartMathErrException e)  {
				if (e.m_se.m_enumErrorType == SMErrProcessor.ERRORTYPES.ERROR_UNRECOGNIZED_PATTERN) {
					// do nothing if is just unrecognized pattern.
				} else  {
					throw e;
				}
			}
        } else  {   // unknown variable is solved
            DataClass datum = varSolved.getSolvedValue();
            if (datum.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                AbstractExpr aexprResult = datum.getAExpr().simplifyAExprMost(listUnknownVars, llistNameSpace, new SimplifyParams(bIgnoreMatrixDim, true, false));
                listResults.add(aexprResult);
            } else {
                listResults.add(new AEConst(datum));
            }
        }

        return listResults;
    }
}
