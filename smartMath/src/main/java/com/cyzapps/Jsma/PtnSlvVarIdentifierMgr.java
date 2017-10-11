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
public class PtnSlvVarIdentifierMgr {
    public static PtnSlvVarIdentifier createZeroPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[0];
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEZERO, "x", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEADD, "x+a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "-a");
        return pi;
    }

    public static PtnSlvVarIdentifier createSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLESUB, "x-a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a");
        return pi;
    }

    public static PtnSlvVarIdentifier createMuledPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEMULEDZERO, "x*a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0/a"); // in matrix multiplication x*a maynot be simplifed to a*x
        return pi;
    }

    public static PtnSlvVarIdentifier createMuledAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEMULEDADD, "x*a+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "(-b)/a"); // in matrix multiplication x*a maynot be simplifed to a*x
        return pi;
    }

    public static PtnSlvVarIdentifier createMuledSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEMULEDSUB, "x*a-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "b/a"); // in matrix multiplication x*a maynot be simplifed to a*x
        return pi;
    }

    public static PtnSlvVarIdentifier createMulPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEMULZERO, "a*x", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a\\0");
        return pi;
    }

    public static PtnSlvVarIdentifier createMulAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEMULADD, "a*x+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a\\(-b)");
        return pi;
    }

    public static PtnSlvVarIdentifier createMulSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEMULSUB, "a*x-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a\\b");
        return pi;
    }

    public static PtnSlvVarIdentifier createDivedPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "!is_zeros(a, true)";
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEDIVEDZERO, "x/a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createDivedAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "!is_zeros(a, true)";
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEDIVEDADD, "x/a+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "(-b)*a");
        return pi;
    }

    public static PtnSlvVarIdentifier createDivedSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "!is_zeros(a, true)";
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEDIVEDSUB, "x/a-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "b*a");
        return pi;
    }

    public static PtnSlvVarIdentifier createDivPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEDIVZERO, "a/x", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0\\a");
        return pi;
    }

    public static PtnSlvVarIdentifier createDivAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEDIVADD, "a/x+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "(-b)\\a");
        return pi;
    }

    public static PtnSlvVarIdentifier createDivSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEDIVSUB, "a/x-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "b\\a");
        return pi;
    }

    public static PtnSlvVarIdentifier createLeftDivedPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "!is_zeros(a, true)";
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLELEFTDIVEDZERO, "a\\x", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createLeftDivedAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "!is_zeros(a, true)";
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLELEFTDIVEDADD, "a\\x+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a*(-b)");
        return pi;
    }

    public static PtnSlvVarIdentifier createLeftDivedSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "!is_zeros(a, true)";
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLELEFTDIVEDSUB, "a\\x-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a*b");
        return pi;
    }

    public static PtnSlvVarIdentifier createLeftDivPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLELEFTDIVZERO, "x\\a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a/0");
        return pi;
    }

    public static PtnSlvVarIdentifier createLeftDivAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLELEFTDIVADD, "x\\a+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a/(-b)");
        return pi;
    }

    public static PtnSlvVarIdentifier createLeftDivSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLELEFTDIVSUB, "x\\a-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a/b");
        return pi;
    }

    public static PtnSlvVarIdentifier createPoweredPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPOWEREDZERO, "x**a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createPoweredAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "round(real(a)) != a"; // a is not integer
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPOWEREDADD, "x**a+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "pow(-b, 1/a)"); // only return one root here considering that like x**0.783 has infinite roots
        return pi;
    }

    public static PtnSlvVarIdentifier createPoweredSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[1];
        strarrayPCConditions[0] = "round(real(a)) != a"; // a is not integer
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPOWEREDSUB, "x**a-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "pow(b, 1/a)");
        return pi;
    }

    public static PtnSlvVarIdentifier createPowerPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPOWERZERO, "a**x", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "log(0)/log(a)");
        return pi;
    }

    public static PtnSlvVarIdentifier createPowerAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPOWERADD, "a**x+b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "log(-b)/log(a)");
        return pi;
    }

    public static PtnSlvVarIdentifier createPowerSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[2];
        strarrayPseudoConsts[0] = "a";
        strarrayPseudoConsts[1] = "b";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPOWERSUB, "a**x-b", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "log(b)/log(a)");
        return pi;
    }

    // need not to consider positive sign case because +x will be simplifed to x.
    public static PtnSlvVarIdentifier createNegPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[0];
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLENEGZERO, "-x", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createNegAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLENEGADD, "-x+a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a");
        return pi;
    }

    public static PtnSlvVarIdentifier createNegSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLENEGSUB, "-x-a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "-a");
        return pi;
    }
    
    public static PtnSlvVarIdentifier createTransposePatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[0];
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLETRANSPOSEZERO, "x'", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createTransposeAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLETRANSPOSEADD, "x'+a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "(-a)'");
        return pi;
    }

    public static PtnSlvVarIdentifier createTransposeSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLETRANSPOSESUB, "x'-a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a'");
        return pi;
    }
    
    public static PtnSlvVarIdentifier createPercentPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[0];
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPERCENTZERO, "x%", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "0");
        return pi;
    }

    public static PtnSlvVarIdentifier createPercentAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPERCENTADD, "x%+a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "-a*100");
        return pi;
    }

    public static PtnSlvVarIdentifier createPercentSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEPERCENTSUB, "x%-a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "a*100");
        return pi;
    }
    
    public static PtnSlvVarIdentifier createInvFuncPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[0];
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCZERO, "f_single_var_invertible(x)", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "f_single_var_inverted(0)");
        return pi;
    }

    public static PtnSlvVarIdentifier createInvFuncAddPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCADD, "f_single_var_invertible(x)+a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "f_single_var_inverted(-a)");
        return pi;
    }

    public static PtnSlvVarIdentifier createInvFuncSubPatternIdentifer() throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        PtnSlvVarIdentifier pi = new PtnSlvVarIdentifier();
        String[] strarrayPseudoConsts = new String[1];
        strarrayPseudoConsts[0] = "a";
        String[] strarrayPCRestricts = new String[0];
        String[] strarrayPCConditions = new String[0];
        pi.setPtnSlvVarIdentifier(ABSTRACTEXPRPATTERNS.AEP_SIMPLEINVFUNCSUB, "f_single_var_invertible(x)-a", strarrayPseudoConsts, strarrayPCRestricts, strarrayPCConditions, "x", "f_single_var_inverted(a)");
        return pi;
    }

}
