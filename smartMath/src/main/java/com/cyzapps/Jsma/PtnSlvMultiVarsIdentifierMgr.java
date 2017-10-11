/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.PtnSlvMultiVarsIdentifier.PatternSetByString;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

/**
 *
 * @author tonyc
 */
public class PtnSlvMultiVarsIdentifierMgr {   // have to be thread safe.

    public static PtnSlvMultiVarsIdentifier createIntPowPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
        // does not support matrix yet.
        PtnSlvMultiVarsIdentifier paIntPow = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[1];
		strarrayVars[0] = "x";
		int[] narrayVarOrders = new int[1];
		narrayVarOrders[0] = 0;
		String[] strarraySolveVarExprs = new String[1];
		strarraySolveVarExprs[0] = "pow(-internal_var0\\internal_var2, 1/internal_var1, 6)";  // return at most 6 roots.
		boolean[] barrayRootsList = new boolean[1];
		barrayRootsList[0] = true;
		PatternSetByString[] pssArray = new PatternSetByString[1];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "internal_var0*x**internal_var1 + internal_var2";
		pssArray[0].mstrarrayPseudoConsts = new String[3];
		pssArray[0].mstrarrayPseudoConsts[0] = "internal_var0";
		pssArray[0].mstrarrayPseudoConsts[1] = "internal_var1";
		pssArray[0].mstrarrayPseudoConsts[2] = "internal_var2";
		pssArray[0].mstrarrayPConExprs = new String[3];
		pssArray[0].mstrarrayPConExprs[0] = 
			"f_aexpr_to_analyze(1) - iff(or(is_inf(real(f_aexpr_to_analyze(0))), is_inf(image(f_aexpr_to_analyze(0)))), f_aexpr_to_analyze(inf), f_aexpr_to_analyze(0))";
        // use real because log2(...) can be a complex value, use round so that ensure that internal_var1 is an integer. Otherwise,
        // in validation phase it will fail.
		pssArray[0].mstrarrayPConExprs[1] = 
			"round(real(log2((f_aexpr_to_analyze(4) - f_aexpr_to_analyze(2))/(f_aexpr_to_analyze(2) - f_aexpr_to_analyze(1)))))";
		pssArray[0].mstrarrayPConExprs[2] = "iff(or(is_inf(real(f_aexpr_to_analyze(0))), is_inf(image(f_aexpr_to_analyze(0)))), f_aexpr_to_analyze(inf), f_aexpr_to_analyze(0))";
		
		paIntPow.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_SINGLEVARINTPOW,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return paIntPow;
    }
    
	public static PtnSlvMultiVarsIdentifier createPolynomialPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvMultiVarsIdentifier paPolynomial = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[1];
		strarrayVars[0] = "x";
		int[] narrayVarOrders = new int[1];
		narrayVarOrders[0] = 0;
		String[] strarraySolveVarExprs = new String[1];
		strarraySolveVarExprs[0] = "roots_internal(internal_var0, internal_var1, internal_var2, internal_var3, internal_var4, internal_var5, internal_var6)";
		boolean[] barrayRootsList = new boolean[1];
		barrayRootsList[0] = true;
		PatternSetByString[] pssArray = new PatternSetByString[1];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "internal_var0*x**6 + internal_var1*x**5 + internal_var2*x**4 + internal_var3*x**3 + internal_var4*x**2 + internal_var5*x + internal_var6";
		pssArray[0].mstrarrayPseudoConsts = new String[7];
		pssArray[0].mstrarrayPseudoConsts[0] = "internal_var0";
		pssArray[0].mstrarrayPseudoConsts[1] = "internal_var1";
		pssArray[0].mstrarrayPseudoConsts[2] = "internal_var2";
		pssArray[0].mstrarrayPseudoConsts[3] = "internal_var3";
		pssArray[0].mstrarrayPseudoConsts[4] = "internal_var4";
		pssArray[0].mstrarrayPseudoConsts[5] = "internal_var5";
		pssArray[0].mstrarrayPseudoConsts[6] = "internal_var6";
		pssArray[0].mstrarrayPConExprs = new String[7];
		pssArray[0].mstrarrayPConExprs[0] = 
			"1/4 * f_aexpr_to_analyze(0) - 1/12 * f_aexpr_to_analyze(1) - 1/12 * f_aexpr_to_analyze(-1) - 1/20 * f_aexpr_to_analyze(i) - 1/20 * f_aexpr_to_analyze(-i) + 1/120 * f_aexpr_to_analyze(2) + 1/120 * f_aexpr_to_analyze(-2)";
		pssArray[0].mstrarrayPConExprs[1] = 
			"0 * f_aexpr_to_analyze(0) - 1/12 * f_aexpr_to_analyze(1) + 1/12 * f_aexpr_to_analyze(-1) - 1/20 * i * f_aexpr_to_analyze(i) + 1/20 * i * f_aexpr_to_analyze(-i) + 1/60 * f_aexpr_to_analyze(2) - 1/60 * f_aexpr_to_analyze(-2)";
		pssArray[0].mstrarrayPConExprs[2] = 
			"-1 * f_aexpr_to_analyze(0) + 1/4 * f_aexpr_to_analyze(1) + 1/4 * f_aexpr_to_analyze(-1) + 1/4 * f_aexpr_to_analyze(i) + 1/4 * f_aexpr_to_analyze(-i) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(-2)";
		pssArray[0].mstrarrayPConExprs[3] = 
			"0 * f_aexpr_to_analyze(0) + 1/4 * f_aexpr_to_analyze(1) - 1/4 * f_aexpr_to_analyze(-1) + 1/4 * i * f_aexpr_to_analyze(i) - 1/4 * i * f_aexpr_to_analyze(-i) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(-2)";
		pssArray[0].mstrarrayPConExprs[4] = 
			"-1/4 * f_aexpr_to_analyze(0) + 1/3 * f_aexpr_to_analyze(1) + 1/3 * f_aexpr_to_analyze(-1) - 1/5 * f_aexpr_to_analyze(i) - 1/5 * f_aexpr_to_analyze(-i) - 1/120 * f_aexpr_to_analyze(2) - 1/120 * f_aexpr_to_analyze(-2)";
		pssArray[0].mstrarrayPConExprs[5] = 
			"0 * f_aexpr_to_analyze(0) + 1/3 * f_aexpr_to_analyze(1) - 1/3 * f_aexpr_to_analyze(-1) - 1/5 * i * f_aexpr_to_analyze(i) + 1/5 * i * f_aexpr_to_analyze(-i) - 1/60 * f_aexpr_to_analyze(2) + 1/60 * f_aexpr_to_analyze(-2)";
		pssArray[0].mstrarrayPConExprs[6] = 
			"1 * f_aexpr_to_analyze(0) + 0 * f_aexpr_to_analyze(1) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(i) + 0 * f_aexpr_to_analyze(-i) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(-2)";
		
		paPolynomial.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_SINGLEVARPOLYNORMIAL,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return paPolynomial;
	}
	
	public static PtnSlvMultiVarsIdentifier create1stOrderEqs2VarsPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvMultiVarsIdentifier pa1stOrderEqs2Vars = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[2];
		strarrayVars[0] = "x";
		strarrayVars[1] = "y";
		int[] narrayVarOrders = new int[2];
		narrayVarOrders[0] = 0;
		narrayVarOrders[1] = 0;
		String[] strarraySolveVarExprs = new String[2];
		String strList = "(1/[[a0,b0],[a1,b1]])*[-c0,-c1]";
		strarraySolveVarExprs[0] = "(" + strList + ")[0]";
		strarraySolveVarExprs[1] = "(" + strList + ")[1]";
		boolean[] barrayRootsList = new boolean[2];
		barrayRootsList[0] = false;
		barrayRootsList[1] = false;
		PatternSetByString[] pssArray = new PatternSetByString[2];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "a0*x + b0*y + c0";
		pssArray[0].mstrarrayPseudoConsts = new String[3];
		pssArray[0].mstrarrayPseudoConsts[0] = "a0";
		pssArray[0].mstrarrayPseudoConsts[1] = "b0";
		pssArray[0].mstrarrayPseudoConsts[2] = "c0";
		pssArray[0].mstrarrayPConExprs = new String[3];
		pssArray[0].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0) - f_aexpr_to_analyze(0,0)";
		pssArray[0].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1) - f_aexpr_to_analyze(0,0)";
		pssArray[0].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0)";
		pssArray[1] = new PatternSetByString();
		pssArray[1].mstrPattern = "a1*x + b1*y + c1";
		pssArray[1].mstrarrayPseudoConsts = new String[3];
		pssArray[1].mstrarrayPseudoConsts[0] = "a1";
		pssArray[1].mstrarrayPseudoConsts[1] = "b1";
		pssArray[1].mstrarrayPseudoConsts[2] = "c1";
		pssArray[1].mstrarrayPConExprs = new String[3];
		pssArray[1].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0) - f_aexpr_to_analyze(0,0)";
		pssArray[1].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1) - f_aexpr_to_analyze(0,0)";
		pssArray[1].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0)";
		
		pa1stOrderEqs2Vars.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_FIRSTORDER2VAREQUATIONS,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return pa1stOrderEqs2Vars;
	}

	public static PtnSlvMultiVarsIdentifier create1stOrderEqs3VarsPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvMultiVarsIdentifier pa1stOrderEqs3Vars = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[3];
		strarrayVars[0] = "x";
		strarrayVars[1] = "y";
		strarrayVars[2] = "z";
		int[] narrayVarOrders = new int[3];
		narrayVarOrders[0] = 0;
		narrayVarOrders[1] = 0;
		narrayVarOrders[2] = 0;
		String[] strarraySolveVarExprs = new String[3];
		String strList = "(1/[[a0,b0,c0],[a1,b1,c1],[a2,b2,c2]])*[-d0,-d1,-d2]";
		strarraySolveVarExprs[0] = "(" + strList + ")[0]";
		strarraySolveVarExprs[1] = "(" + strList + ")[1]";
		strarraySolveVarExprs[2] = "(" + strList + ")[2]";
		boolean[] barrayRootsList = new boolean[3];
		barrayRootsList[0] = false;
		barrayRootsList[1] = false;
		barrayRootsList[2] = false;		
		PatternSetByString[] pssArray = new PatternSetByString[3];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "a0*x + b0*y + c0*z + d0";
		pssArray[0].mstrarrayPseudoConsts = new String[4];
		pssArray[0].mstrarrayPseudoConsts[0] = "a0";
		pssArray[0].mstrarrayPseudoConsts[1] = "b0";
		pssArray[0].mstrarrayPseudoConsts[2] = "c0";
		pssArray[0].mstrarrayPseudoConsts[3] = "d0";
		pssArray[0].mstrarrayPConExprs = new String[4];
		pssArray[0].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0) - f_aexpr_to_analyze(0,0,0)";
		pssArray[0].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0) - f_aexpr_to_analyze(0,0,0)";
		pssArray[0].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1) - f_aexpr_to_analyze(0,0,0)";
		pssArray[0].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0)";
		pssArray[1] = new PatternSetByString();
		pssArray[1].mstrPattern = "a1*x + b1*y + c1*z + d1";
		pssArray[1].mstrarrayPseudoConsts = new String[4];
		pssArray[1].mstrarrayPseudoConsts[0] = "a1";
		pssArray[1].mstrarrayPseudoConsts[1] = "b1";
		pssArray[1].mstrarrayPseudoConsts[2] = "c1";
		pssArray[1].mstrarrayPseudoConsts[3] = "d1";
		pssArray[1].mstrarrayPConExprs = new String[4];
		pssArray[1].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0) - f_aexpr_to_analyze(0,0,0)";
		pssArray[1].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0) - f_aexpr_to_analyze(0,0,0)";
		pssArray[1].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1) - f_aexpr_to_analyze(0,0,0)";
		pssArray[1].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0)";
		pssArray[2] = new PatternSetByString();
		pssArray[2].mstrPattern = "a2*x + b2*y + c2*z + d2";
		pssArray[2].mstrarrayPseudoConsts = new String[4];
		pssArray[2].mstrarrayPseudoConsts[0] = "a2";
		pssArray[2].mstrarrayPseudoConsts[1] = "b2";
		pssArray[2].mstrarrayPseudoConsts[2] = "c2";
		pssArray[2].mstrarrayPseudoConsts[3] = "d2";
		pssArray[2].mstrarrayPConExprs = new String[4];
		pssArray[2].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0) - f_aexpr_to_analyze(0,0,0)";
		pssArray[2].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0) - f_aexpr_to_analyze(0,0,0)";
		pssArray[2].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1) - f_aexpr_to_analyze(0,0,0)";
		pssArray[2].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0)";
		
		pa1stOrderEqs3Vars.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_FIRSTORDER3VAREQUATIONS,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return pa1stOrderEqs3Vars;
	}

	public static PtnSlvMultiVarsIdentifier create1stOrderEqs4VarsPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvMultiVarsIdentifier pa1stOrderEqs4Vars = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[4];
		strarrayVars[0] = "x";
		strarrayVars[1] = "y";
		strarrayVars[2] = "z";
		strarrayVars[3] = "u";
		int[] narrayVarOrders = new int[4];
		narrayVarOrders[0] = 0;
		narrayVarOrders[1] = 0;
		narrayVarOrders[2] = 0;
		narrayVarOrders[3] = 0;
		String[] strarraySolveVarExprs = new String[4];
		String strList = "(1/[[a0,b0,c0,d0],[a1,b1,c1,d1],[a2,b2,c2,d2],[a3,b3,c3,d3]])*[-e0,-e1,-e2,-e3]";
		strarraySolveVarExprs[0] = "(" + strList + ")[0]";
		strarraySolveVarExprs[1] = "(" + strList + ")[1]";
		strarraySolveVarExprs[2] = "(" + strList + ")[2]";
		strarraySolveVarExprs[3] = "(" + strList + ")[3]";
		boolean[] barrayRootsList = new boolean[4];
		barrayRootsList[0] = false;
		barrayRootsList[1] = false;
		barrayRootsList[2] = false;	
		barrayRootsList[3] = false;			
		PatternSetByString[] pssArray = new PatternSetByString[4];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "a0*x + b0*y + c0*z + d0*u + e0";
		pssArray[0].mstrarrayPseudoConsts = new String[5];
		pssArray[0].mstrarrayPseudoConsts[0] = "a0";
		pssArray[0].mstrarrayPseudoConsts[1] = "b0";
		pssArray[0].mstrarrayPseudoConsts[2] = "c0";
		pssArray[0].mstrarrayPseudoConsts[3] = "d0";
		pssArray[0].mstrarrayPseudoConsts[4] = "e0";
		pssArray[0].mstrarrayPConExprs = new String[5];
		pssArray[0].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0)";
		pssArray[1] = new PatternSetByString();
		pssArray[1].mstrPattern = "a1*x + b1*y + c1*z + d1*u + e1";
		pssArray[1].mstrarrayPseudoConsts = new String[5];
		pssArray[1].mstrarrayPseudoConsts[0] = "a1";
		pssArray[1].mstrarrayPseudoConsts[1] = "b1";
		pssArray[1].mstrarrayPseudoConsts[2] = "c1";
		pssArray[1].mstrarrayPseudoConsts[3] = "d1";
		pssArray[1].mstrarrayPseudoConsts[4] = "e1";
		pssArray[1].mstrarrayPConExprs = new String[5];
		pssArray[1].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0)";
		pssArray[2] = new PatternSetByString();
		pssArray[2].mstrPattern = "a2*x + b2*y + c2*z + d2*u + e2";
		pssArray[2].mstrarrayPseudoConsts = new String[5];
		pssArray[2].mstrarrayPseudoConsts[0] = "a2";
		pssArray[2].mstrarrayPseudoConsts[1] = "b2";
		pssArray[2].mstrarrayPseudoConsts[2] = "c2";
		pssArray[2].mstrarrayPseudoConsts[3] = "d2";
		pssArray[2].mstrarrayPseudoConsts[4] = "e2";
		pssArray[2].mstrarrayPConExprs = new String[5];
		pssArray[2].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0)";
		pssArray[3] = new PatternSetByString();
		pssArray[3].mstrPattern = "a3*x + b3*y + c3*z + d3*u + e3";
		pssArray[3].mstrarrayPseudoConsts = new String[5];
		pssArray[3].mstrarrayPseudoConsts[0] = "a3";
		pssArray[3].mstrarrayPseudoConsts[1] = "b3";
		pssArray[3].mstrarrayPseudoConsts[2] = "c3";
		pssArray[3].mstrarrayPseudoConsts[3] = "d3";
		pssArray[3].mstrarrayPseudoConsts[4] = "e3";
		pssArray[3].mstrarrayPConExprs = new String[5];
		pssArray[3].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1) - f_aexpr_to_analyze(0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0)";
		
		pa1stOrderEqs4Vars.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_FIRSTORDER4VAREQUATIONS,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return pa1stOrderEqs4Vars;
	}
	
	public static PtnSlvMultiVarsIdentifier create1stOrderEqs5VarsPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvMultiVarsIdentifier pa1stOrderEqs5Vars = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[5];
		strarrayVars[0] = "x";
		strarrayVars[1] = "y";
		strarrayVars[2] = "z";
		strarrayVars[3] = "u";
		strarrayVars[4] = "v";
		int[] narrayVarOrders = new int[5];
		narrayVarOrders[0] = 0;
		narrayVarOrders[1] = 0;
		narrayVarOrders[2] = 0;
		narrayVarOrders[3] = 0;
		narrayVarOrders[4] = 0;
		String[] strarraySolveVarExprs = new String[5];
		String strList = "(1/[[a0,b0,c0,d0,e0],[a1,b1,c1,d1,e1],[a2,b2,c2,d2,e2],[a3,b3,c3,d3,e3],[a4,b4,c4,d4,e4]])*[-f0,-f1,-f2,-f3,-f4]";
		strarraySolveVarExprs[0] = "(" + strList + ")[0]";
		strarraySolveVarExprs[1] = "(" + strList + ")[1]";
		strarraySolveVarExprs[2] = "(" + strList + ")[2]";
		strarraySolveVarExprs[3] = "(" + strList + ")[3]";
		strarraySolveVarExprs[4] = "(" + strList + ")[4]";
		boolean[] barrayRootsList = new boolean[5];
		barrayRootsList[0] = false;
		barrayRootsList[1] = false;
		barrayRootsList[2] = false;	
		barrayRootsList[3] = false;			
		barrayRootsList[4] = false;			
		PatternSetByString[] pssArray = new PatternSetByString[5];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "a0*x + b0*y + c0*z + d0*u + e0*v + f0";
		pssArray[0].mstrarrayPseudoConsts = new String[6];
		pssArray[0].mstrarrayPseudoConsts[0] = "a0";
		pssArray[0].mstrarrayPseudoConsts[1] = "b0";
		pssArray[0].mstrarrayPseudoConsts[2] = "c0";
		pssArray[0].mstrarrayPseudoConsts[3] = "d0";
		pssArray[0].mstrarrayPseudoConsts[4] = "e0";
		pssArray[0].mstrarrayPseudoConsts[5] = "f0";
		pssArray[0].mstrarrayPConExprs = new String[6];
		pssArray[0].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[1] = new PatternSetByString();
		pssArray[1].mstrPattern = "a1*x + b1*y + c1*z + d1*u + e1*v + f1";
		pssArray[1].mstrarrayPseudoConsts = new String[6];
		pssArray[1].mstrarrayPseudoConsts[0] = "a1";
		pssArray[1].mstrarrayPseudoConsts[1] = "b1";
		pssArray[1].mstrarrayPseudoConsts[2] = "c1";
		pssArray[1].mstrarrayPseudoConsts[3] = "d1";
		pssArray[1].mstrarrayPseudoConsts[4] = "e1";
		pssArray[1].mstrarrayPseudoConsts[5] = "f1";
		pssArray[1].mstrarrayPConExprs = new String[6];
		pssArray[1].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[2] = new PatternSetByString();
		pssArray[2].mstrPattern = "a2*x + b2*y + c2*z + d2*u + e2*v + f2";
		pssArray[2].mstrarrayPseudoConsts = new String[6];
		pssArray[2].mstrarrayPseudoConsts[0] = "a2";
		pssArray[2].mstrarrayPseudoConsts[1] = "b2";
		pssArray[2].mstrarrayPseudoConsts[2] = "c2";
		pssArray[2].mstrarrayPseudoConsts[3] = "d2";
		pssArray[2].mstrarrayPseudoConsts[4] = "e2";
		pssArray[2].mstrarrayPseudoConsts[5] = "f2";
		pssArray[2].mstrarrayPConExprs = new String[6];
		pssArray[2].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[3] = new PatternSetByString();
		pssArray[3].mstrPattern = "a3*x + b3*y + c3*z + d3*u + e3*v + f3";
		pssArray[3].mstrarrayPseudoConsts = new String[6];
		pssArray[3].mstrarrayPseudoConsts[0] = "a3";
		pssArray[3].mstrarrayPseudoConsts[1] = "b3";
		pssArray[3].mstrarrayPseudoConsts[2] = "c3";
		pssArray[3].mstrarrayPseudoConsts[3] = "d3";
		pssArray[3].mstrarrayPseudoConsts[4] = "e3";
		pssArray[3].mstrarrayPseudoConsts[5] = "f3";
		pssArray[3].mstrarrayPConExprs = new String[6];
		pssArray[3].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[4] = new PatternSetByString();
		pssArray[4].mstrPattern = "a4*x + b4*y + c4*z + d4*u + e4*v + f4";
		pssArray[4].mstrarrayPseudoConsts = new String[6];
		pssArray[4].mstrarrayPseudoConsts[0] = "a4";
		pssArray[4].mstrarrayPseudoConsts[1] = "b4";
		pssArray[4].mstrarrayPseudoConsts[2] = "c4";
		pssArray[4].mstrarrayPseudoConsts[3] = "d4";
		pssArray[4].mstrarrayPseudoConsts[4] = "e4";
		pssArray[4].mstrarrayPseudoConsts[5] = "f4";
		pssArray[4].mstrarrayPConExprs = new String[6];
		pssArray[4].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0)";
		
		pa1stOrderEqs5Vars.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_FIRSTORDER5VAREQUATIONS,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return pa1stOrderEqs5Vars;
	}
	
	public static PtnSlvMultiVarsIdentifier create1stOrderEqs6VarsPtnSlvMultiVarsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvMultiVarsIdentifier pa1stOrderEqs6Vars = new PtnSlvMultiVarsIdentifier();
		String[] strarrayVars = new String[6];
		strarrayVars[0] = "x";
		strarrayVars[1] = "y";
		strarrayVars[2] = "z";
		strarrayVars[3] = "u";
		strarrayVars[4] = "v";
		strarrayVars[5] = "w";
		int[] narrayVarOrders = new int[6];
		narrayVarOrders[0] = 0;
		narrayVarOrders[1] = 0;
		narrayVarOrders[2] = 0;
		narrayVarOrders[3] = 0;
		narrayVarOrders[4] = 0;
		narrayVarOrders[5] = 0;
		String[] strarraySolveVarExprs = new String[6];
		String strList = "(1/[[a0,b0,c0,d0,e0,f0],[a1,b1,c1,d1,e1,f1],[a2,b2,c2,d2,e2,f2],[a3,b3,c3,d3,e3,f3],[a4,b4,c4,d4,e4,f4],[a5,b5,c5,d5,e5,f5]])*[-g0,-g1,-g2,-g3,-g4,-g5]";
		strarraySolveVarExprs[0] = "(" + strList + ")[0]";
		strarraySolveVarExprs[1] = "(" + strList + ")[1]";
		strarraySolveVarExprs[2] = "(" + strList + ")[2]";
		strarraySolveVarExprs[3] = "(" + strList + ")[3]";
		strarraySolveVarExprs[4] = "(" + strList + ")[4]";
		strarraySolveVarExprs[5] = "(" + strList + ")[5]";
		boolean[] barrayRootsList = new boolean[6];
		barrayRootsList[0] = false;
		barrayRootsList[1] = false;
		barrayRootsList[2] = false;	
		barrayRootsList[3] = false;			
		barrayRootsList[4] = false;			
		barrayRootsList[5] = false;			
		PatternSetByString[] pssArray = new PatternSetByString[6];
		pssArray[0] = new PatternSetByString();
		pssArray[0].mstrPattern = "a0*x + b0*y + c0*z + d0*u + e0*v + f0*w + g0";
		pssArray[0].mstrarrayPseudoConsts = new String[7];
		pssArray[0].mstrarrayPseudoConsts[0] = "a0";
		pssArray[0].mstrarrayPseudoConsts[1] = "b0";
		pssArray[0].mstrarrayPseudoConsts[2] = "c0";
		pssArray[0].mstrarrayPseudoConsts[3] = "d0";
		pssArray[0].mstrarrayPseudoConsts[4] = "e0";
		pssArray[0].mstrarrayPseudoConsts[5] = "f0";
		pssArray[0].mstrarrayPseudoConsts[6] = "g0";
		pssArray[0].mstrarrayPConExprs = new String[7];
		pssArray[0].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[0].mstrarrayPConExprs[6] = "f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1] = new PatternSetByString();
		pssArray[1].mstrPattern = "a1*x + b1*y + c1*z + d1*u + e1*v + f1*w + g1";
		pssArray[1].mstrarrayPseudoConsts = new String[7];
		pssArray[1].mstrarrayPseudoConsts[0] = "a1";
		pssArray[1].mstrarrayPseudoConsts[1] = "b1";
		pssArray[1].mstrarrayPseudoConsts[2] = "c1";
		pssArray[1].mstrarrayPseudoConsts[3] = "d1";
		pssArray[1].mstrarrayPseudoConsts[4] = "e1";
		pssArray[1].mstrarrayPseudoConsts[5] = "f1";
		pssArray[1].mstrarrayPseudoConsts[6] = "g1";
		pssArray[1].mstrarrayPConExprs = new String[7];
		pssArray[1].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[1].mstrarrayPConExprs[6] = "f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2] = new PatternSetByString();
		pssArray[2].mstrPattern = "a2*x + b2*y + c2*z + d2*u + e2*v + f2*w + g2";
		pssArray[2].mstrarrayPseudoConsts = new String[7];
		pssArray[2].mstrarrayPseudoConsts[0] = "a2";
		pssArray[2].mstrarrayPseudoConsts[1] = "b2";
		pssArray[2].mstrarrayPseudoConsts[2] = "c2";
		pssArray[2].mstrarrayPseudoConsts[3] = "d2";
		pssArray[2].mstrarrayPseudoConsts[4] = "e2";
		pssArray[2].mstrarrayPseudoConsts[5] = "f2";
		pssArray[2].mstrarrayPseudoConsts[6] = "g2";
		pssArray[2].mstrarrayPConExprs = new String[7];
		pssArray[2].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[2].mstrarrayPConExprs[6] = "f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3] = new PatternSetByString();
		pssArray[3].mstrPattern = "a3*x + b3*y + c3*z + d3*u + e3*v + f3*w + g3";
		pssArray[3].mstrarrayPseudoConsts = new String[7];
		pssArray[3].mstrarrayPseudoConsts[0] = "a3";
		pssArray[3].mstrarrayPseudoConsts[1] = "b3";
		pssArray[3].mstrarrayPseudoConsts[2] = "c3";
		pssArray[3].mstrarrayPseudoConsts[3] = "d3";
		pssArray[3].mstrarrayPseudoConsts[4] = "e3";
		pssArray[3].mstrarrayPseudoConsts[5] = "f3";
		pssArray[3].mstrarrayPseudoConsts[6] = "g3";
		pssArray[3].mstrarrayPConExprs = new String[7];
		pssArray[3].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[3].mstrarrayPConExprs[6] = "f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4] = new PatternSetByString();
		pssArray[4].mstrPattern = "a4*x + b4*y + c4*z + d4*u + e4*v + f4*w + g4";
		pssArray[4].mstrarrayPseudoConsts = new String[7];
		pssArray[4].mstrarrayPseudoConsts[0] = "a4";
		pssArray[4].mstrarrayPseudoConsts[1] = "b4";
		pssArray[4].mstrarrayPseudoConsts[2] = "c4";
		pssArray[4].mstrarrayPseudoConsts[3] = "d4";
		pssArray[4].mstrarrayPseudoConsts[4] = "e4";
		pssArray[4].mstrarrayPseudoConsts[5] = "f4";
		pssArray[4].mstrarrayPseudoConsts[6] = "g4";
		pssArray[4].mstrarrayPConExprs = new String[7];
		pssArray[4].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[4].mstrarrayPConExprs[6] = "f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5] = new PatternSetByString();
		pssArray[5].mstrPattern = "a5*x + b5*y + c5*z + d5*u + e5*v + f5*w + g5";
		pssArray[5].mstrarrayPseudoConsts = new String[7];
		pssArray[5].mstrarrayPseudoConsts[0] = "a5";
		pssArray[5].mstrarrayPseudoConsts[1] = "b5";
		pssArray[5].mstrarrayPseudoConsts[2] = "c5";
		pssArray[5].mstrarrayPseudoConsts[3] = "d5";
		pssArray[5].mstrarrayPseudoConsts[4] = "e5";
		pssArray[5].mstrarrayPseudoConsts[5] = "f5";
		pssArray[5].mstrarrayPseudoConsts[6] = "g5";
		pssArray[5].mstrarrayPConExprs = new String[7];
		pssArray[5].mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1,0,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5].mstrarrayPConExprs[1] = "f_aexpr_to_analyze(0,1,0,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5].mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0,0,1,0,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5].mstrarrayPConExprs[3] = "f_aexpr_to_analyze(0,0,0,1,0,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5].mstrarrayPConExprs[4] = "f_aexpr_to_analyze(0,0,0,0,1,0) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5].mstrarrayPConExprs[5] = "f_aexpr_to_analyze(0,0,0,0,0,1) - f_aexpr_to_analyze(0,0,0,0,0,0)";
		pssArray[5].mstrarrayPConExprs[6] = "f_aexpr_to_analyze(0,0,0,0,0,0)";
		
		pa1stOrderEqs6Vars.setPtnSlvMultiVarsIdentifier(ABSTRACTEXPRPATTERNS.AEP_FIRSTORDER6VAREQUATIONS,
				pssArray, strarrayVars, narrayVarOrders, strarraySolveVarExprs, barrayRootsList);
		return pa1stOrderEqs6Vars;
	}

}
