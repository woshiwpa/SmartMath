/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class TwoDExprDataCache {
    public static class CacheNode {
        public MFPNumeric mmfpVarValue = MFPNumeric.ZERO;
        public int mnLog2Gap = 0;
        public LinkedList<CacheNode> mlistChildren = new LinkedList<CacheNode>();
        public boolean mbFuncValueGot = false;
        public DataClass mdatumFuncValue = null;
    }
    
    public LinkedList<CacheNode> mlistPositives = new LinkedList<CacheNode>();
    public LinkedList<CacheNode> mlistNegatives = new LinkedList<CacheNode>();
    
    public static final MFPNumeric[] msarrayMFP2ToPosInts =  {
        MFPNumeric.ONE,
        MFPNumeric.TWO,
        new MFPNumeric("4"),
        new MFPNumeric("8"),
        new MFPNumeric("16"),
        new MFPNumeric("32"),
        new MFPNumeric("64"),
        new MFPNumeric("128"),
        new MFPNumeric("256"),
        new MFPNumeric("512"),
        new MFPNumeric("1024"),
        new MFPNumeric("2048"),
        new MFPNumeric("4096"),
        new MFPNumeric("8192"),
        new MFPNumeric("16384"),
        new MFPNumeric("32768"),
        new MFPNumeric("65536"),
        new MFPNumeric("131072"),
        new MFPNumeric("262144"),
        new MFPNumeric("524288"),
        new MFPNumeric("1048576"),
        new MFPNumeric("2097152"),
        new MFPNumeric("4194304"),
        new MFPNumeric("8388608"),
        new MFPNumeric("16777216"),
        new MFPNumeric("33554432"),
        new MFPNumeric("67108864"),
        new MFPNumeric("134217728"),
        new MFPNumeric("268435456"),
        new MFPNumeric("536870912"),
        new MFPNumeric("1073741824"),
        new MFPNumeric("2147483648"),
        new MFPNumeric("4294967296"),
    };
    
    public static final MFPNumeric[] msarrayMFP2ToNegInts =  {
        MFPNumeric.ONE,
        MFPNumeric.HALF,
        new MFPNumeric("0.25"),
        new MFPNumeric("0.125"),
        new MFPNumeric("0.0625"),
        new MFPNumeric("0.03125"),
        new MFPNumeric("0.015625"),
        new MFPNumeric("0.0078125"),
        new MFPNumeric("0.00390625"),
        new MFPNumeric("0.001953125"),
        new MFPNumeric("0.0009765625"),
        new MFPNumeric("0.00048828125"),
        new MFPNumeric("0.000244140625"),
        new MFPNumeric("0.0001220703125"),
        new MFPNumeric("0.00006103515625"),
        new MFPNumeric("0.000030517578125"),
        new MFPNumeric("0.0000152587890625"),
        new MFPNumeric("0.00000762939453125"),
        new MFPNumeric("0.000003814697265625"),
        new MFPNumeric("0.0000019073486328125"),
        new MFPNumeric("0.00000095367431640625"),
        new MFPNumeric("0.000000476837158203125"),
        new MFPNumeric("0.0000002384185791015625"),
        new MFPNumeric("0.00000011920928955078125"),
        new MFPNumeric("0.000000059604644775390625"),
        new MFPNumeric("0.0000000298023223876953125"),
        new MFPNumeric("0.00000001490116119384765625"),
        new MFPNumeric("0.000000007450580596923828125"),
        new MFPNumeric("0.0000000037252902984619140625"),
        new MFPNumeric("0.00000000186264514923095703125"),
        new MFPNumeric("0.000000000931322574615478515625"),
        new MFPNumeric("0.0000000004656612873077392578125"),
        new MFPNumeric("0.00000000023283064365386962890625"),
    };
    
    public TwoDExprDataCache() {
        CacheNode cnodeZeroPos = new CacheNode();
        CacheNode cnodeZeroNeg = new CacheNode();
        mlistPositives.add(cnodeZeroPos);
        mlistNegatives.add(cnodeZeroNeg);
    }
    
    protected int calcLog2Floor(MFPNumeric mfpValue) {
        // assume mfpValue must be positive, otherwise cannot be right.
        if (mfpValue.isActuallyNonPositive()) {
            return Integer.MIN_VALUE;   // invalid.
        }
        double dValue = mfpValue.doubleValue();
        if (dValue >= MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN
                && dValue <= MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX) {
            // 2**n n from -1023 to 1024 can be accurately presented by double
            double dCompared = 1;
            if (dValue > 1)  {
                int nReturn = 0;
                if (dValue < 2.0) {
                    nReturn = 0;
                } else if (dValue < 4.0) {
                    nReturn = 1;
                } else if (dValue < 8.0) {
                    nReturn = 2;
                } else if (dValue < 16.0) {
                    nReturn = 3;
                } else if (dValue < 32.0) {
                    nReturn = 4;
                } else if (dValue < 64.0) {
                    nReturn = 5;
                } else if (dValue < 128.0) {
                    nReturn = 6;
                } else if (dValue < 256.0) {
                    nReturn = 7;
                } else if (dValue < 512.0) {
                    nReturn = 8;
                } else if (dValue < 1024.0) {
                    nReturn = 9;
                } else if (dValue < 2048.0) {
                    nReturn = 10;
                } else if (dValue < 4096.0) {
                    nReturn = 11;
                } else if (dValue < 8192.0) {
                    nReturn = 12;
                } else if (dValue < 16384.0) {
                    nReturn = 13;
                } else if (dValue < 32768.0) {
                    nReturn = 14;
                } else if (dValue < 65536.0) {
                    nReturn = 15;
                } else if (dValue < 131072.0) {
                    nReturn = 16;
                } else if (dValue < 262144.0) {
                    nReturn = 17;
                } else if (dValue < 524288.0) {
                    nReturn = 18;
                } else if (dValue < 1048576.0) {
                    nReturn = 19;
                } else if (dValue < 2097152.0) {
                    nReturn = 20;
                } else if (dValue < 4194304.0) {
                    nReturn = 21;
                } else if (dValue < 8388608.0) {
                    nReturn = 22;
                } else if (dValue < 16777216.0) {
                    nReturn = 23;
                } else if (dValue < 33554432.0) {
                    nReturn = 24;
                } else if (dValue < 67108864.0) {
                    nReturn = 25;
                } else if (dValue < 134217728.0) {
                    nReturn = 26;
                } else if (dValue < 268435456.0) {
                    nReturn = 27;
                } else if (dValue < 536870912.0) {
                    nReturn = 28;
                } else if (dValue < 1073741824.0) {
                    nReturn = 29;
                } else if (dValue < 2147483648.0) {
                    nReturn = 30;
                } else if (dValue < 4294967296.0) {
                    nReturn = 31;
                } else {    // dValue >= 4294967296.0
                    nReturn = 31;
                    dCompared = 4294967296.0;
                    while (dCompared <= dValue) {
                        dCompared *= 2;
                        nReturn ++;
                    }
                }
                return nReturn;
                /*
                int nReturn = 0;
                while (dCompared <= dValue) {
                    dCompared *= 2;
                    nReturn ++;
                }
                return nReturn - 1;*/
            } else if (dValue < 1) {
                int nReturn = -1;
                if (dValue >= 0.5) {
                    nReturn = -1;
                } else if (dValue >= 0.25 ) {
                    nReturn = -2;
                } else if (dValue >= 0.125 ) {
                    nReturn = -3;
                } else if (dValue >= 0.0625 ) {
                    nReturn = -4;
                } else if (dValue >= 0.03125 ) {
                    nReturn = -5;
                } else if (dValue >= 0.015625 ) {
                    nReturn = -6;
                } else if (dValue >= 0.0078125 ) {
                    nReturn = -7;
                } else if (dValue >= 0.00390625 ) {
                    nReturn = -8;
                } else if (dValue >= 0.001953125 ) {
                    nReturn = -9;
                } else if (dValue >= 0.0009765625 ) {
                    nReturn = -10;
                } else if (dValue >= 0.00048828125 ) {
                    nReturn = -11;
                } else if (dValue >= 0.000244140625 ) {
                    nReturn = -12;
                } else if (dValue >= 0.0001220703125 ) {
                    nReturn = -13;
                } else if (dValue >= 0.00006103515625 ) {
                    nReturn = -14;
                } else if (dValue >= 0.000030517578125 ) {
                    nReturn = -15;
                } else if (dValue >= 0.0000152587890625 ) {
                    nReturn = -16;
                } else if (dValue >= 0.00000762939453125 ) {
                    nReturn = -17;
                } else if (dValue >= 0.000003814697265625 ) {
                    nReturn = -18;
                } else if (dValue >= 0.0000019073486328125 ) {
                    nReturn = -19;
                } else if (dValue >= 0.00000095367431640625 ) {
                    nReturn = -20;
                } else if (dValue >= 0.000000476837158203125 ) {
                    nReturn = -21;
                } else if (dValue >= 0.0000002384185791015625 ) {
                    nReturn = -22;
                } else if (dValue >= 0.00000011920928955078125 ) {
                    nReturn = -23;
                } else if (dValue >= 0.000000059604644775390625 ) {
                    nReturn = -24;
                } else if (dValue >= 0.0000000298023223876953125 ) {
                    nReturn = -25;
                } else if (dValue >= 0.00000001490116119384765625 ) {
                    nReturn = -26;
                } else if (dValue >= 0.000000007450580596923828125 ) {
                    nReturn = -27;
                } else if (dValue >= 0.0000000037252902984619140625 ) {
                    nReturn = -28;
                } else if (dValue >= 0.00000000186264514923095703125 ) {
                    nReturn = -29;
                } else if (dValue >= 0.000000000931322574615478515625 ) {
                    nReturn = -30;
                } else if (dValue >= 0.0000000004656612873077392578125 ) {
                    nReturn = -31;
                } else if (dValue >= 0.00000000023283064365386962890625 ) {
                    nReturn = -32;
                } else  {
                    nReturn = -32;
                    dCompared = 0.00000000023283064365386962890625;
                    while (dCompared > dValue) {
                        dCompared /= 2;
                        nReturn --;
                    }
                }
                /*
                int nReturn = 0;
                while (dCompared > dValue) {
                    dCompared /= 2;
                    nReturn --;
                }*/
                return nReturn;
            } else {    // dValue == 1
                return 0;
            }
        } else {
            double dCompare1 = mfpValue.compareTo(MFPNumeric.ONE);
            MFPNumeric mfpCompared = MFPNumeric.ONE;
            if (dCompare1 > 0) {
                int nReturn = 0;
                while (mfpCompared.compareTo(mfpValue) <= 0) {
                    mfpCompared = mfpCompared.multiply(MFPNumeric.TWO);
                    nReturn ++;
                }
                return nReturn - 1;
            } else if (dCompare1 < 0) {
                int nReturn = 0;
                while (mfpCompared.compareTo(mfpValue) > 0) {
                    mfpCompared = mfpCompared.divide(MFPNumeric.TWO);
                    nReturn --;
                }
                return nReturn;
            } else {    // is 1.
                return 0;
            }
        }
    }
    
    protected MFPNumeric calcIntPower2(int nPower) {
        if (nPower <= 32 && nPower >= - 32) {
            // double value can accurately represent 2**n n is from - 1023 to 1024
            if (nPower >=  0) {
                return msarrayMFP2ToPosInts[nPower];
            } else {
                return msarrayMFP2ToNegInts[-nPower];
            }
        } else {
            return MFPNumeric.pow(MFPNumeric.TWO, new MFPNumeric(nPower));
        }
    }
    
    public CacheNode calcFuncValue(MFPNumeric mfpVarValue, int nStepLog2, AbstractExpr aexpr, Variable var, LinkedList<LinkedList<Variable>> lVarNameSpaces)
    {
        if (mfpVarValue.isActuallyZero()) {
            // zero.
            if (mlistPositives.size() > 0 && mlistPositives.getFirst().mbFuncValueGot) {
                // ok, get it
                return mlistPositives.getFirst();
            } else {
                if (mlistPositives.size() == 0) {
                    mlistPositives.add(new CacheNode());
                }
                try {
                    //no need to set value here coz var has been set as mfpVarValue.
                    //var.setValue(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO));
                    mlistPositives.getFirst().mdatumFuncValue = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                    mlistPositives.getFirst().mbFuncValueGot = true;
                } catch (Exception e) {
                    
                }
                if (mlistNegatives.size() == 0) {
                    mlistNegatives.add(new CacheNode());
                }
                mlistNegatives.getFirst().mdatumFuncValue = mlistPositives.getFirst().mdatumFuncValue;
                mlistNegatives.getFirst().mbFuncValueGot = mlistPositives.getFirst().mbFuncValueGot;
                return mlistPositives.getFirst();
            }
        } else {
            MFPNumeric mfpVarAbsValue = mfpVarValue;
            LinkedList<CacheNode> listNodes = mlistPositives;
            if (mfpVarValue.isActuallyNegative()) {
                mfpVarAbsValue = mfpVarValue.negate();
                listNodes = mlistNegatives;
            }
            int nValueLog2 = calcLog2Floor(mfpVarAbsValue);
            MFPNumeric mfpVarPow2Value = calcIntPower2(nValueLog2);
            if (nValueLog2 >= 0) {    // 1 or larger
                int nNodeIdx = nValueLog2 + 1;
                for (int idx = listNodes.size(); idx <= nNodeIdx; idx ++) {
                    listNodes.add(null);
                }
                if (nNodeIdx < 0 || nNodeIdx >= listNodes.size()) {
                	// this may happen when calculate like log2(1/32) = -5.00000000000000000000142, then floor(log2(1/32)) == -6 not -5
                	CacheNode cNodeReturn = new CacheNode();
                	cNodeReturn.mmfpVarValue = mfpVarValue;
                    try {
                        cNodeReturn.mdatumFuncValue = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                        cNodeReturn.mbFuncValueGot = true;
                    } catch(Exception e) {
                    }
                	return cNodeReturn;
                }
                if (listNodes.get(nNodeIdx) == null) {
                    CacheNode cNode = new CacheNode();
                    listNodes.set(nNodeIdx, cNode);
                    cNode.mmfpVarValue = mfpVarPow2Value;
                    if (mfpVarValue.isActuallyNegative()) {
                        cNode.mmfpVarValue = cNode.mmfpVarValue.negate();
                    }
                    cNode.mnLog2Gap = nValueLog2;
                }
                MFPNumeric mfpVarAbsRemain = mfpVarAbsValue;
                MFPNumeric mfpRemainPow2Value = mfpVarPow2Value;
                while (true) {
                    mfpVarAbsRemain = mfpVarAbsRemain.subtract(mfpRemainPow2Value);
                    if (mfpVarAbsRemain.isActuallyZero()) {
                        break;
                    }
                    int nLastValueLog2 = nValueLog2;
                    nValueLog2 = calcLog2Floor(mfpVarAbsRemain);
                    if (nValueLog2 < nStepLog2) {
                        break;
                    }
                    CacheNode cNodeParent = listNodes.get(nNodeIdx);
                    listNodes = cNodeParent.mlistChildren;
                    mfpRemainPow2Value = calcIntPower2(nValueLog2);
                    nNodeIdx = nLastValueLog2 - 1 - nValueLog2;
                    for (int idx = listNodes.size(); idx <= nNodeIdx; idx ++) {
                        listNodes.add(null);
                    }
                    if (nNodeIdx < 0 || nNodeIdx >= listNodes.size()) {
                    	// this may happen when calculate like log2(1/32) = -5.00000000000000000000142, then floor(log2(1/32)) == -6 not -5
                    	CacheNode cNodeReturn = new CacheNode();
                    	cNodeReturn.mmfpVarValue = mfpVarValue;
                    	try {
                            cNodeReturn.mdatumFuncValue = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                            cNodeReturn.mbFuncValueGot = true;
                        } catch(Exception e) {
                        }
                        return cNodeReturn;
                    }
                    if (listNodes.get(nNodeIdx) == null) {
                        CacheNode cNode = new CacheNode();
                        listNodes.set(nNodeIdx, cNode);
                        cNode.mmfpVarValue = mfpRemainPow2Value;
                        if (mfpVarValue.isActuallyNegative()) {
                            cNode.mmfpVarValue = cNode.mmfpVarValue.negate();
                        }
                        cNode.mmfpVarValue = cNode.mmfpVarValue.add(cNodeParent.mmfpVarValue);
                        cNode.mnLog2Gap = nValueLog2;
                    }
                }
                if (!listNodes.get(nNodeIdx).mbFuncValueGot) {
                    try {
                        var.setValue(new DataClass(DATATYPES.DATUM_DOUBLE, listNodes.get(nNodeIdx).mmfpVarValue));
                        listNodes.get(nNodeIdx).mdatumFuncValue = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                        listNodes.get(nNodeIdx).mbFuncValueGot = true;
                    } catch(Exception e) {
                    }
                }
                return listNodes.get(nNodeIdx);
            } else { // < 1
                if (mlistPositives.size() == 0) {
                    mlistPositives.add(new CacheNode());
                }
                if (mlistNegatives.size() == 0) {
                    mlistNegatives.add(new CacheNode());
                }
                CacheNode cNodeParent = listNodes.getFirst();
                MFPNumeric mfpVarAbsRemain = mfpVarAbsValue;
                MFPNumeric mfpRemainPow2Value = mfpVarPow2Value;
                int nNodeIdx = -nValueLog2 - 1;
                while (true) {
                    listNodes = cNodeParent.mlistChildren;
                    for (int idx = listNodes.size(); idx <= nNodeIdx; idx ++) {
                        listNodes.add(null);
                    }
                    if (nNodeIdx < 0 || nNodeIdx >= listNodes.size()) {
                    	// this may happen when calculate like log2(1/32) = -5.00000000000000000000142, then floor(log2(1/32)) == -6 not -5
                    	CacheNode cNodeReturn = new CacheNode();
                    	cNodeReturn.mmfpVarValue = mfpVarValue;
                        try {
                            cNodeReturn.mdatumFuncValue = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                            cNodeReturn.mbFuncValueGot = true;
                        } catch(Exception e) {
                        }
                    	return cNodeReturn;
                    }
                    if (listNodes.get(nNodeIdx) == null) {
                        CacheNode cNode = new CacheNode();
                        listNodes.set(nNodeIdx, cNode);
                        cNode.mmfpVarValue = mfpRemainPow2Value;
                        if (mfpVarValue.isActuallyNegative()) {
                            cNode.mmfpVarValue = cNode.mmfpVarValue.negate();
                        }
                        cNode.mmfpVarValue = cNode.mmfpVarValue.add(cNodeParent.mmfpVarValue);
                        cNode.mnLog2Gap = nValueLog2;
                    }
                    mfpVarAbsRemain = mfpVarAbsRemain.subtract(mfpRemainPow2Value);
                    if (mfpVarAbsRemain.isActuallyZero()) {
                        break;
                    }
                    int nLastValueLog2 = nValueLog2;
                    nValueLog2 = calcLog2Floor(mfpVarAbsRemain);
                    if (nValueLog2 < nStepLog2) {
                        break;
                    }
                    cNodeParent = listNodes.get(nNodeIdx);
                    nNodeIdx = nLastValueLog2 - 1 - nValueLog2;
                    mfpRemainPow2Value = calcIntPower2(nValueLog2);
                }
                
                if (!listNodes.get(nNodeIdx).mbFuncValueGot) {
                    try {
                        var.setValue(new DataClass(DATATYPES.DATUM_DOUBLE, listNodes.get(nNodeIdx).mmfpVarValue));
                        listNodes.get(nNodeIdx).mdatumFuncValue = aexpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);
                        listNodes.get(nNodeIdx).mbFuncValueGot = true;
                    } catch (Exception e) {
                        
                    }
                }
                return listNodes.get(nNodeIdx);                
            }
            
        }        
    }
}
