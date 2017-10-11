/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jsma;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.PatternManager.ABSTRACTEXPRPATTERNS;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

/**
 *
 * @author tonyc
 */
public class Ptn1VarIntegIdentifierMgr {
	public static Ptn1VarIntegIdentifier create1OverAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/(a+b*x)";
		strarrayPatterns[1] = "1/(a+x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "b!=0";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log(a+b*x)/b");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXOverAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x/(a+b*x)";
		strarrayPatterns[1] = "x/(a+x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "b!=0";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_OVER_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/b**2*(b*x-a*log(a+b*x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXSqrOverAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x**2/(a+b*x)";
		strarrayPatterns[1] = "x**2/(a+x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "b!=0";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_SQR_OVER_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/(2*b**3)*((a+b*x)**2-4*a*(a+b*x)+2*a**2*log(a+b*x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[4];
		strarrayPatterns[0] = "1/(a*x+b*x**2)";
		strarrayPatterns[1] = "1/(a*x+x**2)";
		strarrayPatterns[2] = "1/(x+b*x**2)";
		strarrayPatterns[3] = "1/(x+x**2)";
		String[] strarrayPatternConditions = new String[4];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		strarrayPatternConditions[2] = "a=1";
		strarrayPatternConditions[3] = "[a=1,b=1]";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-1/a*log((a+b*x)/x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[4];
		strarrayPatterns[0] = "1/(a*x**2+b*x**3)";
		strarrayPatterns[1] = "1/(a*x**2+x**3)";
		strarrayPatterns[2] = "1/(x**2+b*x**3)";
		strarrayPatterns[3] = "1/(x**2+x**3)";
		String[] strarrayPatternConditions = new String[4];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		strarrayPatternConditions[2] = "a=1";
		strarrayPatternConditions[3] = "[a=1,b=1]";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQR_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "b/a**2*log((a+b*x)/x)-1/a/x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "(a+b*x)**0.5";
		strarrayPatterns[1] = "(a+x)**0.5";
		/*strarrayPatterns[2] = "sqrt(a+b*x)";
		strarrayPatterns[3] = "sqrt(a+x)";*/
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		/*strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";*/
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "b!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "2/b/3*(a+b*x)**1.5");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXSqrtAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x*(a+b*x)**0.5";
		strarrayPatterns[1] = "x*(a+x)**0.5";
		/*strarrayPatterns[2] = "x*sqrt(a+b*x)";
		strarrayPatterns[3] = "x*sqrt(a+x)";*/
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		/*strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";*/
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "b!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_SQRT_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "2/15/b**2*(3*b*x-2*a)*(a+b*x)**1.5");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXToNSqrtAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x**n*(a+b*x)**0.5";
		strarrayPatterns[1] = "x**n*(a+x)**0.5";
		/*strarrayPatterns[2] = "x**n*sqrt(a+b*x)";
		strarrayPatterns[3] = "x**n*sqrt(a+x)";*/
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		/*strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";*/  
		String[] strarrayPseudoConsts = new String[3];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[2];
		strarrayPCConditions[0] = "b!=0";
		strarrayPCConditions[1] = "and(n>1,round(n)==n)";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_TO_N_SQRT_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "2/b/(2*n+3)*x**n*(a+b*x)**1.5-2*n*a/b/(2*n+3)*integrate(\"x**(n-1)*sqrt(a+b*x)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtAPlusBXOverXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "(a+b*x)**0.5/x";
		strarrayPatterns[1] = "(a+x)**0.5/x";
		/*strarrayPatterns[2] = "sqrt(a+b*x)/x";
		strarrayPatterns[3] = "sqrt(a+x)/x";*/
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		/*strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";*/
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[2];
		strarrayPCConditions[0] = "a!=0";
		strarrayPCConditions[1] = "b!=0";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_PLUS_B_X_OVER_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "2*sqrt(a+b*x)+(a**0.5)*log((sqrt(a+b*x)-(a**0.5))/(sqrt(a+b*x)+(a**0.5)))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtAPlusBXOverXToNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x**n*(a+b*x)**0.5";
		strarrayPatterns[1] = "x**n*(a+x)**0.5";
		/*strarrayPatterns[2] = "x**n*sqrt(a+b*x)";
		strarrayPatterns[3] = "x**n*sqrt(a+x)";*/
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		/*strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";*/
		String[] strarrayPseudoConsts = new String[3];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[2];
		strarrayPCConditions[0] = "a*b!=0";
		strarrayPCConditions[1] = "and(n<-1,round(n)==n)";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_PLUS_B_X_OVER_X_TO_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/a/(n+1)*(a+b*x)**1.5*x**(n+1)-(2*n+5)*b/2/a/(n+1)*integrate(\"x**(n+1)*sqrt(a+b*x)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverSqrtAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[4];
		strarrayPatterns[0] = "(a+b*x)**-0.5";
		strarrayPatterns[1] = "(a+x)**-0.5";
		strarrayPatterns[2] = "1/(a+b*x)**0.5";
		strarrayPatterns[3] = "1/(a+x)**0.5";
		/*strarrayPatterns[4] = "1/sqrt(a+b*x)";
		strarrayPatterns[5] = "1/sqrt(a+x)";*/
		String[] strarrayPatternConditions = new String[4];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";		
		/*strarrayPatternConditions[4] = "";
		strarrayPatternConditions[5] = "b=1";*/
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a*b!=0";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_SQRT_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/a*x*sqrt(a+b*x)-1.5*b/a*integrate(\"x*sqrt(a+b*x)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrtAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/x/(a+b*x)**0.5";
		strarrayPatterns[1] = "1/x/(a+x)**0.5";
		/*strarrayPatterns[2] = "1/x/sqrt(a+b*x)";
		strarrayPatterns[3] = "1/x/sqrt(a+x)";*/
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		/*strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "b=1";  */	  
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[2];
		strarrayPCConditions[0] = "a!=0";
		strarrayPCConditions[1] = "b!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQRT_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/(a**0.5)*log((sqrt(a+b*x)-(a**0.5))/(sqrt(a+b*x)+(a**0.5)))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXToNSqrtAPlusBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x**n/(a+b*x)**0.5";
		strarrayPatterns[1] = "x**n/(a+x)**0.5";
		//strarrayPatterns[2] = "x**n/sqrt(a+b*x)";
		//strarrayPatterns[3] = "x**n/sqrt(a+x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "b=1";
		//strarrayPatternConditions[2] = "";
		//strarrayPatternConditions[3] = "b=1";		
		String[] strarrayPseudoConsts = new String[3];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[2];
		strarrayPCConditions[0] = "a*b!=0";
		strarrayPCConditions[1] = "and(n<-1,round(n)==n)";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "-a/b";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_TO_N_SQRT_A_PLUS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/a/(n+1)*sqrt(a+b*x)*x**(n+1)-(2*n+3)*b/(2*a*(n+1))*integrate(\"(x**(n+1)*sqrt(a+b*x))\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrPlusASqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/(a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5*i";
		strarraySingularPnts[1] = "-a**0.5*i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQR_PLUS_A_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "atan(x/a**0.5)/a**0.5");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrMinusASqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/(-a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5";
		strarraySingularPnts[1] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQR_MINUS_A_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log((x-a**0.5)/(x+a**0.5))/2/a**0.5");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverMinusXSqrPlusASqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/(a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5";
		strarraySingularPnts[1] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_MINUS_X_SQR_PLUS_A_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log((x+a**0.5)/(-x+a**0.5))/2/a**0.5");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverMinusXSqrMinusASqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/(-a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5*i";
		strarraySingularPnts[1] = "-a**0.5*i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_MINUS_X_SQR_MINUS_A_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-atan(x/a**0.5)/a**0.5");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverAXSqrPlusBPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/(b+a*x**2)";
		strarrayPatterns[1] = "1/(b+x**2)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "a=1";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a*b!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "(-b/a)**0.5";
		strarraySingularPnts[1] = "-(-b/a)**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_A_X_SQR_PLUS_B,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/(a*b)**0.5*atan(a**0.5*x/b**0.5)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtASqrPlusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "(a+x**2)**0.5";
		//strarrayPatterns[1] = "sqrt(a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_SQR_PLUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.5*x*sqrt(x**2+a)+0.5*a*log(x+sqrt(x**2+a))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtXSqrSqrtASqrPlusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "x**2*(a+x**2)**0.5";
		//strarrayPatterns[1] = "x**2*sqrt(a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_X_SQR_SQRT_A_SQR_PLUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.125*x*(a+2*x**2)*sqrt(a+x**2)+0.125*a**2*log(x+sqrt(a+x**2))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtASqrPlusXSqrOverXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "(a+x**2)**0.5/x";
		//strarrayPatterns[1] = "sqrt(a+x**2)/x";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_SQR_PLUS_X_SQR_OVER_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "sqrt(a+x**2)-a*log((a**0.5+sqrt(a+x**2))/x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtASqrPlusXSqrOverXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "(a+x**2)**0.5/x**2";
		//strarrayPatterns[1] = "sqrt(a+x**2)/x**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_SQR_PLUS_X_SQR_OVER_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log(x+sqrt(a+x**2))-sqrt(a+x**2)/x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverSqrtASqrPlusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "(a+x**2)**-0.5";
		strarrayPatterns[1] = "1/(a+x**2)**0.5";
		//strarrayPatterns[2] = "1/sqrt(a+x**2)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		//strarrayPatternConditions[2] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5*i";
		strarraySingularPnts[1] = "-a**0.5*i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_SQRT_A_SQR_PLUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log(x+sqrt(a+x**2))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXSqrOverSqrtASqrPlusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "x**2/(a+x**2)**0.5";
		//strarrayPatterns[1] = "x**2/sqrt(a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5*i";
		strarraySingularPnts[1] = "-a**0.5*i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_SQR_OVER_SQRT_A_SQR_PLUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.5*x*sqrt(x**2+a)-0.5*a*log(x+sqrt(a+x**2))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrtASqrPlusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/x/(a+x**2)**0.5";
		//strarrayPatterns[1] = "1/x/sqrt(a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[3];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "a**0.5*i";
		strarraySingularPnts[2] = "-a**0.5*i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQRT_A_SQR_PLUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/a**0.5*log(x/(a**0.5+sqrt(x**2+a)))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrSqrtASqrPlusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/x**2/(a+x**2)**0.5";
		//strarrayPatterns[1] = "1/x**2/sqrt(a+x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[3];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "a**0.5*i";
		strarraySingularPnts[2] = "-a**0.5*i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQR_SQRT_A_SQR_PLUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-sqrt(x**2+a)/a/x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverSqrtXSqrMinusASqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "(-a+x**2)**-0.5";
		strarrayPatterns[1] = "1/(-a+x**2)**0.5";
		//strarrayPatterns[2] = "1/sqrt(-a+x**2)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		//strarrayPatternConditions[2] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5";
		strarraySingularPnts[1] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_SQRT_X_SQR_MINUS_A_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log(x+sqrt(x**2-a))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverSqrtASqrMinusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "(a-x**2)**-0.5";
		strarrayPatterns[1] = "1/(a-x**2)**0.5";
		//strarrayPatterns[2] = "1/sqrt(a-x**2)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		//strarrayPatternConditions[2] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5";
		strarraySingularPnts[1] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_SQRT_A_SQR_MINUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "asin(x/a**0.5)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtASqrMinusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "(a-x**2)**0.5";
		//strarrayPatterns[1] = "sqrt(a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_SQR_MINUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.5*x*sqrt(a-x**2)+a/2*asin(x/a**0.5)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXSqrSqrtASqrMinusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "x**2*(a-x**2)**0.5";
		//strarrayPatterns[1] = "x**2*sqrt(a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_SQR_SQRT_A_SQR_MINUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.125*x*(2*x**2-a)*sqrt(a-x**2)+0.125*a**2*asin(x/a**0.5)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtASqrMinusXSqrOverXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "(a-x**2)**0.5/x";
		//strarrayPatterns[1] = "sqrt(a-x**2)/x";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_SQR_MINUS_X_SQR_OVER_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "sqrt(a-x**2)-a**0.5*log((a+sqrt(a-x**2))/x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSqrtASqrMinusXSqrOverXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "(a-x**2)**0.5/x**2";
		//strarrayPatterns[1] = "sqrt(a-x**2)/x**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SQRT_A_SQR_MINUS_X_SQR_OVER_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-sqrt(a-x**2)/x-asin(x/a**0.5)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrtASqrMinusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/x/(a-x**2)**0.5";
		//strarrayPatterns[1] = "1/x/sqrt(a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[3];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "a**0.5";
		strarraySingularPnts[2] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQRT_A_SQR_MINUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-1/a**0.5*log((a**0.5+sqrt(a-x**2))/x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXSqrOverSqrtASqrMinusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "x**2/(a-x**2)**0.5";
		//strarrayPatterns[1] = "x**2/sqrt(a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "a**0.5";
		strarraySingularPnts[1] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_SQR_OVER_SQRT_A_SQR_MINUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-0.5*x*sqrt(a-x**2)+0.5*a*asin(x/a**0.5)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXSqrSqrtASqrMinusXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/x**2/(a-x**2)**0.5";
		//strarrayPatterns[1] = "1/x**2/sqrt(a-x**2)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[3];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "a**0.5";
		strarraySingularPnts[2] = "-a**0.5";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQR_SQRT_A_SQR_MINUS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-sqrt(a-x**2)/a/x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverSqrtAXSqrPlusBXPlusCPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[8];
		strarrayPatterns[0] = "1/(c+b*x+a*x**2)**0.5";
		strarrayPatterns[1] = "1/(c+b*x+x**2)**0.5";
		strarrayPatterns[2] = "1/(c+x+a*x**2)**0.5";
		strarrayPatterns[3] = "1/(c+x+x**2)**0.5";
		strarrayPatterns[4] = "(c+b*x+a*x**2)**-0.5";
		strarrayPatterns[5] = "(c+b*x+x**2)**-0.5";
		strarrayPatterns[6] = "(c+x+a*x**2)**-0.5";
		strarrayPatterns[7] = "(c+x+x**2)**-0.5";
		/*strarrayPatterns[8] = "1/sqrt(c+b*x+a*x**2)";
		strarrayPatterns[9] = "1/sqrt(c+b*x+x**2)";
		strarrayPatterns[10] = "1/sqrt(c+x+a*x**2)";
		strarrayPatterns[11] = "1/sqrt(c+x+x**2)";
		strarrayPatterns[12] = "sqrt(1/(c+b*x+a*x**2))";
		strarrayPatterns[13] = "sqrt(1/(c+b*x+x**2))";
		strarrayPatterns[14] = "sqrt(1/(c+x+a*x**2))";
		strarrayPatterns[15] = "sqrt(1/(c+x+x**2))";*/
		String[] strarrayPatternConditions = new String[8];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "a=1";
		strarrayPatternConditions[2] = "b=1";
		strarrayPatternConditions[3] = "[a=1,b=1]";
		strarrayPatternConditions[4] = "";
		strarrayPatternConditions[5] = "a=1";
		strarrayPatternConditions[6] = "b=1";
		strarrayPatternConditions[7] = "[a=1,b=1]";
		/*strarrayPatternConditions[8] = "";
		strarrayPatternConditions[9] = "a=1";
		strarrayPatternConditions[10] = "b=1";
		strarrayPatternConditions[11] = "[a=1,b=1]";
		strarrayPatternConditions[12] = "";
		strarrayPatternConditions[13] = "a=1";
		strarrayPatternConditions[14] = "b=1";
		strarrayPatternConditions[15] = "[a=1,b=1]";*/
		String[] strarrayPseudoConsts = new String[3];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "c";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "(-b+(b**2-4*a*c)**0.5)/2/a";
		strarraySingularPnts[1] = "(-b-(b**2-4*a*c)**0.5)/2/a";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/(a**0.5)*log(2*(a**0.5)*sqrt(a*x**2+b*x+c)+2*a*x+b)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXOverSqrtAXSqrPlusBXPlusCPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[4];
		strarrayPatterns[0] = "x/(c+b*x+a*x**2)**0.5";
		strarrayPatterns[1] = "x/(c+b*x+x**2)**0.5";
		strarrayPatterns[2] = "x/(c+x+a*x**2)**0.5";
		strarrayPatterns[3] = "x/(c+x+x**2)**0.5";
		/*strarrayPatterns[4] = "x/sqrt(c+b*x+a*x**2)";
		strarrayPatterns[5] = "x/sqrt(c+b*x+x**2)";
		strarrayPatterns[6] = "x/sqrt(c+x+a*x**2)";
		strarrayPatterns[7] = "x/sqrt(c+x+x**2)";*/
		String[] strarrayPatternConditions = new String[4];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "a=1";
		strarrayPatternConditions[2] = "b=1";
		strarrayPatternConditions[3] = "[a=1,b=1]";
		/*strarrayPatternConditions[4] = "";
		strarrayPatternConditions[5] = "a=1";
		strarrayPatternConditions[6] = "b=1";
		strarrayPatternConditions[7] = "[a=1,b=1]";*/
		String[] strarrayPseudoConsts = new String[3];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "c";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "(-b+(b**2-4*a*c)**0.5)/2/a";
		strarraySingularPnts[1] = "(-b-(b**2-4*a*c)**0.5)/2/a";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "sqrt(a*x**2+b*x+c)/a - b/2/a*(1/(a**0.5)*log(2*(a**0.5)*sqrt(a*x**2+b*x+c)+2*a*x+b))");
		return pi;
	}
	/*
	public static Ptn1VarIntegIdentifier create1OverSqrtAXSqrPlusBXPlusCToNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPseudoConsts = new String[4];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "c";
		strarrayPseudoConsts[3] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C_TO_N,
				"1/sqrt(a*x**2+b*x+c)**n", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXOverSqrtAXSqrPlusBXPlusCToNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPseudoConsts = new String[4];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "c";
		strarrayPseudoConsts[3] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_OVER_SQRT_A_X_SQR_PLUS_B_X_PLUS_C_TO_N,
				"x/sqrt(a*x**2+b*x+c)**n", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x");
		return pi;
	}*/
	public static Ptn1VarIntegIdentifier create1OverXSqrtAXSqrPlusBXPlusCPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[4];
		strarrayPatterns[0] = "1/x/(c+b*x+a*x**2)**0.5";
		strarrayPatterns[1] = "1/x/(c+b*x+x**2)**0.5";
		strarrayPatterns[2] = "1/x/(c+x+a*x**2)**0.5";
		strarrayPatterns[3] = "1/x/(c+x+x**2)**0.5";
		/*strarrayPatterns[4] = "1/x/sqrt(c+b*x+a*x**2)";
		strarrayPatterns[5] = "1/x/sqrt(c+b*x+x**2)";
		strarrayPatterns[6] = "1/x/sqrt(c+x+a*x**2)";
		strarrayPatterns[7] = "1/x/sqrt(c+x+x**2)";*/
		String[] strarrayPatternConditions = new String[4];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "a=1";
		strarrayPatternConditions[2] = "b=1";
		strarrayPatternConditions[3] = "[a=1,b=1]";
		/*strarrayPatternConditions[4] = "";
		strarrayPatternConditions[5] = "a=1";
		strarrayPatternConditions[6] = "b=1";
		strarrayPatternConditions[7] = "[a=1,b=1]";*/
		String[] strarrayPseudoConsts = new String[3];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		strarrayPseudoConsts[2] = "c";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "c!=0";
		String[] strarraySingularPnts = new String[3];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "(-b+(b**2-4*a*c)**0.5)/2/a";
		strarraySingularPnts[2] = "(-b-(b**2-4*a*c)**0.5)/2/a";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_SQRT_A_X_SQR_PLUS_B_X_PLUS_C,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-1/(c**0.5)*log((2*(c**0.5)*sqrt(a*x**2+b*x+c)+b*x+2*c)/x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSinXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "sin(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SIN_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-cos(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSinXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "sin(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SIN_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x/2-sin(2*x)/4");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSinXNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "sin(x)**n";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "and(n>2,round(n)==n)";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SIN_X_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "-1/n*sin(x)**(n-1)*cos(x)+(n-1)/n*integrate(\"sin(x)**(n-2)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCosXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "cos(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COS_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "sin(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCosXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "cos(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COS_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x/2+sin(2*x)/4");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCosXNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "cos(x)**n";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "and(n>2,round(n)==n)";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COS_X_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/n*cos(x)**(n-1)*sin(x)+(n-1)/n*integrate(\"cos(x)**(n-2)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createTanXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "tan(x)";
		strarrayPatterns[1] = "sin(x)/cos(x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];  // have to provide both singular points and excluded integration range. singular points are frequently experienced points.
		strarraySingularPnts[0] = "pi*1.5";
		strarraySingularPnts[1] = "pi*0.5";
		strarraySingularPnts[2] = "-pi*0.5";
		strarraySingularPnts[3] = "-pi*1.5";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_TAN_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-log(cos(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createTanXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "tan(x)**2";
		strarrayPatterns[1] = "sin(x)**2/cos(x)**2";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "1.5*pi";
		strarraySingularPnts[1] = "0.5*pi";
		strarraySingularPnts[2] = "-0.5*pi";
		strarraySingularPnts[3] = "-1.5*pi";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_TAN_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "tan(x)-x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createTanXNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "tan(x)**n";
		strarrayPatterns[1] = "sin(x)**n*cos(x)**minus_n";  // sin(x)**n/cos(x)**n will be simplified to sin(x)**n*cos(x)**(-n), but a negative value cannot be patterned to -n
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "minus_n=0"; // here cannot set minus_n = null or nan, if null, MFP will assume minus_N is not found,
													// if nan or null, sin(x)**n*cos(x)**nan (or null) will be matched. If 0, sin(x)**n*cos(x)**0
													// will be simplified to sin(x)**n which cannot match sin(x)**n*cos(x)**minus_n so is ok.
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "n";
		strarrayPseudoConsts[1] = "minus_n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "and(n>2,round(n)==n,or(minus_n==0,n==-minus_n))";	// minus_n == NULL means minus_n is not used.
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "1.5*pi";
		strarraySingularPnts[1] = "0.5*pi";
		strarraySingularPnts[2] = "-0.5*pi";
		strarraySingularPnts[3] = "-1.5*pi";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_TAN_X_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "1/(n-1)*tan(x)**(n-1)-integrate(\"tan(x)**(n-2)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCotXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[3];
		strarrayPatterns[0] = "1/tan(x)";
		strarrayPatterns[1] = "tan(x)**-1"; //tan(x)**-1 cannot be simplified to 1/tan(x)
		strarrayPatterns[2] = "cos(x)/sin(x)";
		String[] strarrayPatternConditions = new String[3];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		strarrayPatternConditions[2] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COT_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "log(sin(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCotXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/tan(x)**2";
		strarrayPatterns[1] = "cos(x)**2/sin(x)**2";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COT_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-1/tan(x)-x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCotXNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "tan(x)**n";  // a negative value cannot be patterned to -n, so have to use tan(x)**n (n<-2), and also cos(x)**n/sin(x)**n will be simplifed to cos(x)**n*sin(x)**-n so that have to use cos(x)**minus_n*sin(x)**n
		strarrayPatterns[1] = "cos(x)**minus_n*sin(x)**n";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "minus_n=0";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "n";
		strarrayPseudoConsts[1] = "minus_n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "and(n<-2,round(n)==n, or(minus_n == 0, n == -minus_n))";
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COT_X_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-1/(n+1)*tan(x)**(n+1)-integrate(\"tan(x)**(n+2)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCscXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/sin(x)";
		strarrayPatterns[1] = "sin(x)**-1";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_CSC_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "log(1/sin(x)-1/tan(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCscXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/sin(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_CSC_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-1/tan(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCscXNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "sin(x)**n";  // cannot use "1/sin(x)**n" because 1/sin(x)**3 will be simplified to sin(x)**-3 which cannot match sin(x)**-n
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "and(n<-2,round(n)==n)";
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_CSC_X_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "1/(n+1)*sin(x)**(n+2)/tan(x)+(n+2)/(n+1)*integrate(\"sin(x)**(n+2)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCscXCotXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[4];
		strarrayPatterns[0] = "1/sin(x)/tan(x)";
		strarrayPatterns[1] = "tan(x)**-1/sin(x)";
		strarrayPatterns[2] = "sin(x)**-1/tan(x)";
		strarrayPatterns[3] = "sin(x)**-1*tan(x)**-1";
		String[] strarrayPatternConditions = new String[4];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		strarrayPatternConditions[2] = "";
		strarrayPatternConditions[3] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi";
		strarraySingularPnts[1] = "-pi";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi";
		strarraySingularPnts[4] = "2*pi";
		String strExclIntegRanges = "sin(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_CSC_X_COT_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-1/sin(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSecXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/cos(x)";
		strarrayPatterns[1] = "cos(x)**-1";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "1.5*pi";
		strarraySingularPnts[1] = "0.5*pi";
		strarraySingularPnts[2] = "-0.5*pi";
		strarraySingularPnts[3] = "-1.5*pi";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SEC_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "log(1/cos(x)+tan(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSecXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/cos(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "1.5*pi";
		strarraySingularPnts[1] = "0.5*pi";
		strarraySingularPnts[2] = "-0.5*pi";
		strarraySingularPnts[3] = "-1.5*pi";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SEC_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "tan(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSecXNPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "cos(x)**n";  // cannot use "1/cos(x)**n (n > 0) because  1/cos(x)**3 will be simplified to cos(x)**-3 which cannot match cos(x)**-n
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "and(n<-2,round(n)==n)";
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "1.5*pi";
		strarraySingularPnts[1] = "0.5*pi";
		strarraySingularPnts[2] = "-0.5*pi";
		strarraySingularPnts[3] = "-1.5*pi";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SEC_X_N,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-1/(n+1)*cos(x)**(n+2)*tan(x)+(n+2)/(n+1)*integrate(\"cos(x)**(n+2)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSecXTanXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/cos(x)*tan(x)";
		strarrayPatterns[1] = "cos(x)**(-1)*tan(x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "1.5*pi";
		strarraySingularPnts[1] = "0.5*pi";
		strarraySingularPnts[2] = "-0.5*pi";
		strarraySingularPnts[3] = "-1.5*pi";
		String strExclIntegRanges = "cos(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SEC_X_TAN_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "1/cos(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createAsinXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "asin(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_ASIN_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x*asin(x)+sqrt(1-x**2)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createAcosXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "acos(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_ACOS_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x*acos(x)-sqrt(1-x**2)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createAtanXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "atan(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "i";
		strarraySingularPnts[1] = "-i";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_ATAN_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x*atan(x)-log(sqrt(1+x**2))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createEToXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "e**x";
		//strarrayPatterns[1] = "exp(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_E_TO_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "e**x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createAToXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "a**x";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_A_TO_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "a**x/log(a)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createEToAXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "e**(a*x**2)";
		//strarrayPatterns[1] = "exp(a*x**2)";
		strarrayPatterns[1] = "e**(x**2)";
		//strarrayPatterns[3] = "exp(x**2)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		strarrayPatternConditions[1] = "a=1";
		//strarrayPatternConditions[3] = "a=1";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_E_TO_A_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", true, "-inf", "inf", "sqrt(pi/-a)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createEToMinusAXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "e**(-a*x**2)";
		//strarrayPatterns[1] = "exp(-a*x**2)";
		strarrayPatterns[1] = "e**(-x**2)";
		//strarrayPatterns[3] = "exp(-x**2)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		strarrayPatternConditions[1] = "a=1";
		//strarrayPatternConditions[3] = "a=1";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_E_TO_MINUS_A_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", true, "-inf", "inf", "sqrt(pi/a)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXEToAXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[3];
		strarrayPatterns[0] = "x*e**(a*x)";
		//strarrayPatterns[1] = "x*exp(a*x)";
		strarrayPatterns[1] = "x*e**x";
		//strarrayPatterns[3] = "x*exp(x)";
		strarrayPatterns[2] = "x*e**-x";
		//strarrayPatterns[5] = "x*exp(-x)";
		String[] strarrayPatternConditions = new String[3];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		strarrayPatternConditions[1] = "a=1";
		//strarrayPatternConditions[3] = "a=1";
		strarrayPatternConditions[2] = "a=-1";
		//strarrayPatternConditions[5] = "a=-1";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "a";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "a!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_E_TO_A_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/a**2*(a*x-1)*e**(a*x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXToNEToAXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[3];
		strarrayPatterns[0] = "x**n*e**(a*x)";
		//strarrayPatterns[1] = "x**n*exp(a*x)";
		strarrayPatterns[1] = "x**n*e**(x)";
		//strarrayPatterns[3] = "x**n*exp(x)";
		strarrayPatterns[2] = "x**n*e**(-x)";
		//strarrayPatterns[5] = "x**n*exp(-x)";
		String[] strarrayPatternConditions = new String[3];
		strarrayPatternConditions[0] = "";
		//strarrayPatternConditions[1] = "";
		strarrayPatternConditions[1] = "a=1";
		//strarrayPatternConditions[3] = "a=1";
		strarrayPatternConditions[2] = "a=-1";
		//strarrayPatternConditions[5] = "a=-1";
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[2];
		strarrayPCConditions[0] = "a!=0";
		strarrayPCConditions[1] = "and(n>1,round(n)==n)";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_TO_N_E_TO_A_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "1/a*x**n*e**(a*x)-n/a*integrate(\"x**(n-1)*e**(a*x)\", \"x\")");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createEToAXSinBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[5];
		strarrayPatterns[0] = "e**(a*x)*sin(b*x)";
		strarrayPatterns[1] = "e**(x)*sin(x)";
		strarrayPatterns[2] = "e**(x)*sin(-x)";
		strarrayPatterns[3] = "e**(-x)*sin(x)";
		strarrayPatterns[4] = "e**(-x)*sin(-x)";
		/*strarrayPatterns[5] = "exp(a*x)*sin(b*x)";
		strarrayPatterns[6] = "exp(x)*sin(x)";
		strarrayPatterns[7] = "exp(x)*sin(-x)";
		strarrayPatterns[8] = "exp(-x)*sin(x)";
		strarrayPatterns[9] = "exp(-x)*sin(-x)";*/
		String[] strarrayPatternConditions = new String[5];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "[a=1,b=1]";
		strarrayPatternConditions[2] = "[a=1,b=-1]";
		strarrayPatternConditions[3] = "[a=-1,b=1]";
		strarrayPatternConditions[4] = "[a=-1,b=-1]";
		/*strarrayPatternConditions[5] = "";
		strarrayPatternConditions[6] = "[a=1,b=1]";
		strarrayPatternConditions[7] = "[a=1,b=-1]";
		strarrayPatternConditions[8] = "[a=-1,b=1]";
		strarrayPatternConditions[9] = "[a=-1,b=-1]";*/
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "(a**2+b**2)!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_E_TO_A_X_SIN_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "e**(a*x)/(a**2+b**2)*(a*sin(b*x)-b*cos(b*x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createEToAXCosBXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[5];
		strarrayPatterns[0] = "e**(a*x)*cos(b*x)";
		strarrayPatterns[1] = "e**(x)*cos(x)";
		strarrayPatterns[2] = "e**(x)*cos(-x)";
		strarrayPatterns[3] = "e**(-x)*cos(x)";
		strarrayPatterns[4] = "e**(-x)*cos(-x)";
		/*strarrayPatterns[5] = "cos(b*x)*exp(a*x)";
		strarrayPatterns[6] = "cos(x)*exp(x)";
		strarrayPatterns[7] = "cos(-x)*exp(x)";
		strarrayPatterns[8] = "cos(x)*exp(-x)";
		strarrayPatterns[9] = "cos(-x)*exp(-x)";*/
		String[] strarrayPatternConditions = new String[5];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "[a=1,b=1]";
		strarrayPatternConditions[2] = "[a=1,b=-1]";
		strarrayPatternConditions[3] = "[a=-1,b=1]";
		strarrayPatternConditions[4] = "[a=-1,b=-1]";
		/* strarrayPatternConditions[5] = "";
		strarrayPatternConditions[6] = "[a=1,b=1]";
		strarrayPatternConditions[7] = "[a=1,b=-1]";
		strarrayPatternConditions[8] = "[a=-1,b=1]";
		strarrayPatternConditions[9] = "[a=-1,b=-1]";*/
		String[] strarrayPseudoConsts = new String[2];
		strarrayPseudoConsts[0] = "a";
		strarrayPseudoConsts[1] = "b";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "(a**2+b**2)!=0";
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_E_TO_A_X_COS_B_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "e**(a*x)/(a**2+b**2)*(a*cos(b*x)+b*sin(b*x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createLogXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "log(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_LOG_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x*log(x)-x");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createXToNLogXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "x**n*log(x)";
		strarrayPatterns[1] = "x*log(x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "n=1";
		String[] strarrayPseudoConsts = new String[1];
		strarrayPseudoConsts[0] = "n";
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[1];
		strarrayPCConditions[0] = "n!=-1";
		String[] strarraySingularPnts = new String[1];
		strarraySingularPnts[0] = "0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_X_TO_N_LOG_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "x**(n+1)/(n+1)**2*((n+1)*log(x)-1)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier create1OverXLogXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/x/log(x)";
		strarrayPatterns[1] = "x**-1/log(x)";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[2];
		strarraySingularPnts[0] = "0";
		strarraySingularPnts[1] = "1";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_1_OVER_X_LOG_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "log(log(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSinhXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "sinh(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SINH_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "cosh(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCoshXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "cosh(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COSH_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "sinh(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createTanhXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "tanh(x)";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "-3*pi/2*i";
		strarraySingularPnts[1] = "-pi/2*i";
		strarraySingularPnts[2] = "pi/2*i";
		strarraySingularPnts[3] = "3*pi/2*i";
		String strExclIntegRanges = "cosh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_TANH_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "log(cosh(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSinhXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "sinh(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SINH_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.25*sinh(2*x)-x/2");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCoshXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "cosh(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[0];
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COSH_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, "", false, "", "", "0.25*sinh(2*x)+x/2");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createTanhXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "tanh(x)**2";
		strarrayPatterns[1] = "sinh(x)**2/cosh(x)**2";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "-3*pi/2*i";
		strarraySingularPnts[1] = "-pi/2*i";
		strarraySingularPnts[2] = "pi/2*i";
		strarraySingularPnts[3] = "3*pi/2*i";
		String strExclIntegRanges = "cosh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_TANH_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "x-tanh(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCschXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/sinh(x)";
		strarrayPatterns[1] = "sinh(x)**-1";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi*i";
		strarraySingularPnts[1] = "-pi*i";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi*i";
		strarraySingularPnts[4] = "2*pi*i";
		String strExclIntegRanges = "sinh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_CSCH_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "log(tanh(x/2))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSechXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/cosh(x)";
		strarrayPatterns[1] = "cosh(x)**-1";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "-3*pi/2*i";
		strarraySingularPnts[1] = "-pi/2*i";
		strarraySingularPnts[2] = "pi/2*i";
		strarraySingularPnts[3] = "3*pi/2*i";
		String strExclIntegRanges = "cosh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SECH_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "2*atan(exp(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCothXPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/tanh(x)";
		strarrayPatterns[1] = "tanh(x)**-1";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi*i";
		strarraySingularPnts[1] = "-pi*i";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi*i";
		strarraySingularPnts[4] = "2*pi*i";
		String strExclIntegRanges = "sinh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COTH_X,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "log(sinh(x))");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCschXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/sinh(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi*i";
		strarraySingularPnts[1] = "-pi*i";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi*i";
		strarraySingularPnts[4] = "2*pi*i";
		String strExclIntegRanges = "sinh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_CSCH_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-cosh(x)/sinh(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createSechXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[1];
		strarrayPatterns[0] = "1/cosh(x)**2";
		String[] strarrayPatternConditions = new String[1];
		strarrayPatternConditions[0] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[4];
		strarraySingularPnts[0] = "-3*pi/2*i";
		strarraySingularPnts[1] = "-pi/2*i";
		strarraySingularPnts[2] = "pi/2*i";
		strarraySingularPnts[3] = "3*pi/2*i";
		String strExclIntegRanges = "cosh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_SECH_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "tanh(x)");
		return pi;
	}
	public static Ptn1VarIntegIdentifier createCothXSqrPatternIdentifier() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException {
		Ptn1VarIntegIdentifier pi = new Ptn1VarIntegIdentifier();
		String[] strarrayPatterns = new String[2];
		strarrayPatterns[0] = "1/tanh(x)**2";
		strarrayPatterns[1] = "cosh(x)**2/sinh(x)**2";
		String[] strarrayPatternConditions = new String[2];
		strarrayPatternConditions[0] = "";
		strarrayPatternConditions[1] = "";
		String[] strarrayPseudoConsts = new String[0];
		String[] strarrayPCRestricts = new String[0];
		String[] strarrayPCConditions = new String[0];
		String[] strarraySingularPnts = new String[5];
		strarraySingularPnts[0] = "-2*pi*i";
		strarraySingularPnts[1] = "-pi*i";
		strarraySingularPnts[2] = "0";
		strarraySingularPnts[3] = "pi*i";
		strarraySingularPnts[4] = "2*pi*i";
		String strExclIntegRanges = "sinh(x)==0";
		pi.setPtn1VarIntegIdentifier(ABSTRACTEXPRPATTERNS.AEP_EXPR_COTH_X_SQR,
				strarrayPatterns, strarrayPatternConditions, strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions,
				"x", strarraySingularPnts, strExclIntegRanges, false, "", "", "-cosh(x)/sinh(x)+x");
		return pi;
	}
}
