/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.PtnSlvVarMultiRootsIdentifier.PatternSetByString;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

/**
 *
 * @author tonyc
 */
public class PtnSlvVarMultiRootsIdentifierMgr {   // have to be thread safe.

    public static PtnSlvVarMultiRootsIdentifier createPosIntPowPtnSlvVarMultiRootsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
        // does not support matrix yet.
        PtnSlvVarMultiRootsIdentifier paIntPow = new PtnSlvVarMultiRootsIdentifier();
		String strVariable = "x";
		String strSolveVarExpr = "pow(-internal_var0\\internal_var2, 1/internal_var1, 6)";  // return at most 6 roots.
		String strRootNumber = "iff(get_boolean_aexpr_false(internal_var2 == 0), 1, internal_var1)";
		PatternSetByString pss = new PatternSetByString();
		pss.mstrPattern = "internal_var0*x**internal_var1 + internal_var2";
		pss.mstrarrayPseudoConsts = new String[3];
		pss.mstrarrayPseudoConsts[0] = "internal_var0";
		pss.mstrarrayPseudoConsts[1] = "internal_var1";
		pss.mstrarrayPseudoConsts[2] = "internal_var2";
		pss.mstrarrayPConExprs = new String[3];
		pss.mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1) - f_aexpr_to_analyze(0)";
        // use real because log2(...) can be a complex value, use round so that ensure that internal_var1 is an integer. Otherwise,
        // in validation phase it will fail.
		pss.mstrarrayPConExprs[1] = "round(real(log2((f_aexpr_to_analyze(4) - f_aexpr_to_analyze(2))/(f_aexpr_to_analyze(2) - f_aexpr_to_analyze(1)))))";
		pss.mstrarrayPConExprs[2] = "f_aexpr_to_analyze(0)";
		pss.mstrarrayPConRestricts = new String[1];
        pss.mstrarrayPConRestricts[0] = "and(internal_var1 > 1, round(real(internal_var1)) == internal_var1)";
        pss.mstrarrayPConConditions = new String[2];
        pss.mstrarrayPConConditions[0] = "internal_var0 != 0";
        pss.mstrarrayPConConditions[1] = "internal_var2 != 0";
        
		paIntPow.setPtnSlvVarMultiRootsIdentifier(ABSTRACTEXPRPATTERNS.AEP_SINGLEVARPOSINTPOW, pss, strVariable, strSolveVarExpr, strRootNumber);
		return paIntPow;
    }
    
    public static PtnSlvVarMultiRootsIdentifier createNegIntPowPtnSlvVarMultiRootsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
        // does not support matrix yet.
        PtnSlvVarMultiRootsIdentifier paIntPow = new PtnSlvVarMultiRootsIdentifier();
		String strVariable = "x";
		String strSolveVarExpr = "pow(-internal_var0\\internal_var2, 1/internal_var1, 6)";  // return at most 6 roots.
		String strRootNumber = "iff(get_boolean_aexpr_false(internal_var2 == 0), 1, -internal_var1)";
		PatternSetByString pss = new PatternSetByString();
		pss.mstrPattern = "internal_var0*x**internal_var1 + internal_var2";
		pss.mstrarrayPseudoConsts = new String[3];
		pss.mstrarrayPseudoConsts[0] = "internal_var0";
		pss.mstrarrayPseudoConsts[1] = "internal_var1";
		pss.mstrarrayPseudoConsts[2] = "internal_var2";
		pss.mstrarrayPConExprs = new String[3];
		pss.mstrarrayPConExprs[0] = "f_aexpr_to_analyze(1) - f_aexpr_to_analyze(INF)";
        // use real because log2(...) can be a complex value, use round so that ensure that internal_var1 is an integer. Otherwise,
        // in validation phase it will fail.
		pss.mstrarrayPConExprs[1] = "round(real(log2((f_aexpr_to_analyze(4) - f_aexpr_to_analyze(2))/(f_aexpr_to_analyze(2) - f_aexpr_to_analyze(1)))))";
		pss.mstrarrayPConExprs[2] = "f_aexpr_to_analyze(INF)";
		pss.mstrarrayPConRestricts = new String[1];
        pss.mstrarrayPConRestricts[0] = "and(internal_var1 < -1, round(real(internal_var1)) == internal_var1)";
        pss.mstrarrayPConConditions = new String[2];
        pss.mstrarrayPConConditions[0] = "internal_var0 != 0";
        pss.mstrarrayPConConditions[1] = "internal_var2 != 0";
        
		paIntPow.setPtnSlvVarMultiRootsIdentifier(ABSTRACTEXPRPATTERNS.AEP_SINGLEVARNEGINTPOW, pss, strVariable, strSolveVarExpr, strRootNumber);
		return paIntPow;
    }
    
	public static PtnSlvVarMultiRootsIdentifier create3OrderPolynomialPtnSlvVarMultiRootsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvVarMultiRootsIdentifier paPolynomial = new PtnSlvVarMultiRootsIdentifier();
		String strVariable = "x";
		String strSolveVarExpr = "roots_internal(internal_var0, internal_var1, internal_var2, internal_var3)";
		String strRootNumber = "iff(get_boolean_aexpr_true(internal_var0 != 0), 3, "
                + "get_boolean_aexpr_true(internal_var1 != 0), 2, "
                + "get_boolean_aexpr_true(internal_var2 != 0), 1, 0)";
                
		PatternSetByString pss= new PatternSetByString();
		pss.mstrPattern = "internal_var0*x**3 + internal_var1*x**2 + internal_var2*x + internal_var3";
		pss.mstrarrayPseudoConsts = new String[4];
		pss.mstrarrayPseudoConsts[0] = "internal_var0";
		pss.mstrarrayPseudoConsts[1] = "internal_var1";
		pss.mstrarrayPseudoConsts[2] = "internal_var2";
		pss.mstrarrayPseudoConsts[3] = "internal_var3";
		pss.mstrarrayPConExprs = new String[4];
		pss.mstrarrayPConExprs[0] = 
			"1/6 * f_aexpr_to_analyze(2) - 1/2 * f_aexpr_to_analyze(1) + 1/2 * f_aexpr_to_analyze(0) - 1/6 * f_aexpr_to_analyze(-1)";
		pss.mstrarrayPConExprs[1] = 
			"1/2 * f_aexpr_to_analyze(1) - f_aexpr_to_analyze(0) + 1/2 * f_aexpr_to_analyze(-1)";
		pss.mstrarrayPConExprs[2] = 
			"-1/6 * f_aexpr_to_analyze(2) + f_aexpr_to_analyze(1) - 1/2 * f_aexpr_to_analyze(0) - 1/3 * f_aexpr_to_analyze(-1)";
		pss.mstrarrayPConExprs[3] = 
			"f_aexpr_to_analyze(0)";
		pss.mstrarrayPConRestricts = new String[2];
        pss.mstrarrayPConRestricts[0] = "and(get_boolean_aexpr_false(internal_var0 == 0), "
                                        + "get_boolean_aexpr_false(internal_var1 == 0)) == false";  // note that internal_var2 != 0 while interval_var0 and 1 == 0 is unacceptable, this leads to infinite solving.
        pss.mstrarrayPConRestricts[1] = "and(get_boolean_aexpr_true(image(internal_var0) == 0), "
                                        + "get_boolean_aexpr_true(image(internal_var1) == 0), "
                                        + "get_boolean_aexpr_true(image(internal_var2) == 0), "
                                        + "get_boolean_aexpr_true(image(internal_var3) == 0)) == true"; // additional condition to plot charts.
        pss.mstrarrayPConConditions = new String[0];
		
		paPolynomial.setPtnSlvVarMultiRootsIdentifier(ABSTRACTEXPRPATTERNS.AEP_SINGLEVAR3ORDERPOLYNORMIAL, pss, strVariable, strSolveVarExpr, strRootNumber);
		return paPolynomial;
	}
    
	public static PtnSlvVarMultiRootsIdentifier create6OrderPolynomialPtnSlvVarMultiRootsIdentifier() throws JSmartMathErrException, JFCALCExpErrException, InterruptedException	{
		PtnSlvVarMultiRootsIdentifier paPolynomial = new PtnSlvVarMultiRootsIdentifier();
		String strVariable = "x";
		String strSolveVarExpr = "roots_internal(internal_var0, internal_var1, internal_var2, internal_var3, internal_var4, internal_var5, internal_var6)";
		String strRootNumber = "iff(get_boolean_aexpr_true(internal_var0 != 0), 6, "
                + "get_boolean_aexpr_true(internal_var1 != 0), 5, "
                + "get_boolean_aexpr_true(internal_var2 != 0), 4, "
                + "get_boolean_aexpr_true(internal_var3 != 0), 3, "
                + "get_boolean_aexpr_true(internal_var4 != 0), 2, "
                + "get_boolean_aexpr_true(internal_var5 != 0), 1, 0)";
                
		PatternSetByString pss= new PatternSetByString();
		pss.mstrPattern = "internal_var0*x**6 + internal_var1*x**5 + internal_var2*x**4 + internal_var3*x**3 + internal_var4*x**2 + internal_var5*x + internal_var6";
		pss.mstrarrayPseudoConsts = new String[7];
		pss.mstrarrayPseudoConsts[0] = "internal_var0";
		pss.mstrarrayPseudoConsts[1] = "internal_var1";
		pss.mstrarrayPseudoConsts[2] = "internal_var2";
		pss.mstrarrayPseudoConsts[3] = "internal_var3";
		pss.mstrarrayPseudoConsts[4] = "internal_var4";
		pss.mstrarrayPseudoConsts[5] = "internal_var5";
		pss.mstrarrayPseudoConsts[6] = "internal_var6";
		pss.mstrarrayPConExprs = new String[7];
		pss.mstrarrayPConExprs[0] = 
			"1/4 * f_aexpr_to_analyze(0) - 1/12 * f_aexpr_to_analyze(1) - 1/12 * f_aexpr_to_analyze(-1) - 1/20 * f_aexpr_to_analyze(i) - 1/20 * f_aexpr_to_analyze(-i) + 1/120 * f_aexpr_to_analyze(2) + 1/120 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConExprs[1] = 
			"0 * f_aexpr_to_analyze(0) - 1/12 * f_aexpr_to_analyze(1) + 1/12 * f_aexpr_to_analyze(-1) - 1/20 * i * f_aexpr_to_analyze(i) + 1/20 * i * f_aexpr_to_analyze(-i) + 1/60 * f_aexpr_to_analyze(2) - 1/60 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConExprs[2] = 
			"-1 * f_aexpr_to_analyze(0) + 1/4 * f_aexpr_to_analyze(1) + 1/4 * f_aexpr_to_analyze(-1) + 1/4 * f_aexpr_to_analyze(i) + 1/4 * f_aexpr_to_analyze(-i) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConExprs[3] = 
			"0 * f_aexpr_to_analyze(0) + 1/4 * f_aexpr_to_analyze(1) - 1/4 * f_aexpr_to_analyze(-1) + 1/4 * i * f_aexpr_to_analyze(i) - 1/4 * i * f_aexpr_to_analyze(-i) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConExprs[4] = 
			"-1/4 * f_aexpr_to_analyze(0) + 1/3 * f_aexpr_to_analyze(1) + 1/3 * f_aexpr_to_analyze(-1) - 1/5 * f_aexpr_to_analyze(i) - 1/5 * f_aexpr_to_analyze(-i) - 1/120 * f_aexpr_to_analyze(2) - 1/120 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConExprs[5] = 
			"0 * f_aexpr_to_analyze(0) + 1/3 * f_aexpr_to_analyze(1) - 1/3 * f_aexpr_to_analyze(-1) - 1/5 * i * f_aexpr_to_analyze(i) + 1/5 * i * f_aexpr_to_analyze(-i) - 1/60 * f_aexpr_to_analyze(2) + 1/60 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConExprs[6] = 
			"1 * f_aexpr_to_analyze(0) + 0 * f_aexpr_to_analyze(1) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(i) + 0 * f_aexpr_to_analyze(-i) + 0 * f_aexpr_to_analyze(2) + 0 * f_aexpr_to_analyze(-2)";
		pss.mstrarrayPConRestricts = new String[1];
        pss.mstrarrayPConRestricts[0] = "and(get_boolean_aexpr_false(internal_var0 == 0), "
                                        + "get_boolean_aexpr_false(internal_var1 == 0), "
                                        + "get_boolean_aexpr_false(internal_var2 == 0), "
                                        + "get_boolean_aexpr_false(internal_var3 == 0), "
                                        + "get_boolean_aexpr_false(internal_var4 == 0), "
                                        + "get_boolean_aexpr_false(internal_var5 == 0)) == false";
        pss.mstrarrayPConConditions = new String[0];
		
		paPolynomial.setPtnSlvVarMultiRootsIdentifier(ABSTRACTEXPRPATTERNS.AEP_SINGLEVAR6ORDERPOLYNORMIAL, pss, strVariable, strSolveVarExpr, strRootNumber);
		return paPolynomial;
	}
}
