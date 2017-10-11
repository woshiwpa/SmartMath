/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class MathLib {
    public static LinkedList<DataClass> solvePolynomial(LinkedList<DataClass> listParams, FunctionInterrupter functionInterrupter) throws JFCALCExpErrException, InterruptedException    {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        if (listParams.size() < 2)   {
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        int nParamNumber = listParams.size(), nNonZeroStart = 0;
        DataClass datumZero = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
        DataClass datumHalf = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.HALF);
        DataClass datumOne = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE);
        DataClass datumMinus1 = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE);
        DataClass datumTwo = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.TWO);
        DataClass datumThree = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(3));
        DataClass datum1Over3 = BuiltinProcedures.evaluateDivision(datumOne, datumThree);
        DataClass datumi = new DataClass();
        datumi.setComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
        
        for (int idx = 0; idx < nParamNumber; idx ++)   {
            if (listParams.get(idx).isEqual(datumZero)) {
                nNonZeroStart = idx + 1;
            } else  {
                break;
            }
        }
        if (nNonZeroStart >= nParamNumber - 1)  {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }
        
        LinkedList<DataClass> listRoots = new LinkedList<DataClass>();
        switch (nParamNumber - 1 - nNonZeroStart)   {
            case 1:
            {
                // ax + b == 0
                DataClass datumParam1 = listParams.get(nParamNumber - 2);
                DataClass datumParam2 = listParams.get(nParamNumber - 1);
                DataClass datumRoot = BuiltinProcedures.evaluateDivision(datumParam2, datumParam1);
                datumRoot = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumRoot);
                listRoots.add(datumRoot);
                break;
            }
            case 2:
            {
                // ax**2 + bx + c = 0
                DataClass a = new DataClass(), b = new DataClass(), c = new DataClass(), sqrt_b2_4ac = new DataClass();
                a = listParams.get(nParamNumber - 3);
                b = listParams.get(nParamNumber - 2);
                c = listParams.get(nParamNumber - 1);
                DataClass bsqr = BuiltinProcedures.evaluateMultiplication(b, b);
                DataClass fourac = BuiltinProcedures.evaluateMultiplication(a, c);
                fourac = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(4)), fourac);
                if (c.isEqual(datumZero))   {
                    listRoots.add(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO));
                    DataClass datumRoot2 = BuiltinProcedures.evaluateDivision(b, a);
                    datumRoot2 = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumRoot2);
                    listRoots.add(datumRoot2);
                } else if (BuiltinProcedures.evaluateSubstraction(bsqr, fourac).isEqual(datumZero)) {   // b**2-4ac == 0
                    DataClass datumRoot = BuiltinProcedures.evaluateDivision(b, a);
                    datumRoot = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumRoot);
                    datumRoot = BuiltinProcedures.evaluateDivision(datumRoot, datumTwo);
                    listRoots.add(datumRoot);
                    listRoots.add(datumRoot.cloneSelf());
                } else  {
                    sqrt_b2_4ac = BuiltinProcedures.evaluatePower(
                            BuiltinProcedures.evaluateSubstraction(bsqr, fourac), datumHalf,
                            null);
                    DataClass minusb = BuiltinProcedures.evaluateMultiplication(datumMinus1, b);
                    DataClass twoa = BuiltinProcedures.evaluateMultiplication(datumTwo, a);
                    DataClass datumRoot1 = BuiltinProcedures.evaluateAdding(minusb, sqrt_b2_4ac);
                    DataClass datumRoot2 = BuiltinProcedures.evaluateSubstraction(minusb, sqrt_b2_4ac);
                    listRoots.add(BuiltinProcedures.evaluateDivision(datumRoot1, twoa));
                    listRoots.add(BuiltinProcedures.evaluateDivision(datumRoot2, twoa));
                }
                break;
            }
            case 3:
            {
                // ax**3 + bx**2 + cx + d = 0
                DataClass a = listParams.get(nParamNumber - 4),
                        b = listParams.get(nParamNumber - 3),
                        c = listParams.get(nParamNumber - 2),
                        d = listParams.get(nParamNumber - 1),
                        delta = new DataClass(), big_A = new DataClass(), big_B = new DataClass(), big_C = new DataClass();
                if (d.isEqual(datumZero))   {
                    // now we get one root which is 0
                    DataClass x_value = datumZero.cloneSelf();
                    listRoots.addFirst(x_value);
                    LinkedList<DataClass> listNewParams = new LinkedList<DataClass>();
                    listNewParams.add(a);
                    listNewParams.add(b);
                    listNewParams.add(c);
                    LinkedList<DataClass> listNewRoots = solvePolynomial(listNewParams, functionInterrupter);
                    for (int idx = 0; idx < listNewRoots.size(); idx ++)    {
                        listRoots.add(listNewRoots.get(idx));
                    }
                } else  {
                    DataClass bsqr = BuiltinProcedures.evaluateMultiplication(b, b);
                    DataClass threeac = BuiltinProcedures.evaluateMultiplication(a, c);
                    threeac = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(3)), threeac);
                    big_A = BuiltinProcedures.evaluateSubstraction(bsqr, threeac);
                    DataClass bc = BuiltinProcedures.evaluateMultiplication(b, c);
                    DataClass ninead = BuiltinProcedures.evaluateMultiplication(a, d);
                    ninead = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(9)), ninead);
                    big_B = BuiltinProcedures.evaluateSubstraction(bc, ninead);
                    DataClass csqr = BuiltinProcedures.evaluateMultiplication(c, c);
                    DataClass threebd = BuiltinProcedures.evaluateMultiplication(b, d);
                    threebd = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(3)), threebd);
                    big_C = BuiltinProcedures.evaluateSubstraction(csqr, threebd);
                    DataClass big_Bsqr = BuiltinProcedures.evaluateMultiplication(big_B, big_B);
                    DataClass fourbig_Abig_C = BuiltinProcedures.evaluateMultiplication(big_A, big_C);
                    fourbig_Abig_C = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(4)), fourbig_Abig_C);
                    delta = BuiltinProcedures.evaluateSubstraction(big_Bsqr, fourbig_Abig_C);
                    if (big_A.isEqual(big_B) && big_A.isEqual(datumZero))   {   // big_B == big_A == 0
                        // three same roots
                        DataClass minusbover3overa = BuiltinProcedures.evaluateDivision(b, new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(3)));
                        minusbover3overa = BuiltinProcedures.evaluateDivision(minusbover3overa, a);
                        minusbover3overa = BuiltinProcedures.evaluateMultiplication(datumMinus1, minusbover3overa);
                        listRoots.add(minusbover3overa);
                        DataClass root2 = new DataClass(), root3 = new DataClass();
                        root2.copyTypeValueDeep(minusbover3overa);
                        root3.copyTypeValueDeep(minusbover3overa);
                        listRoots.add(root2);
                        listRoots.add(root3);               
                    } else if (delta.isEqual(datumZero))    {
                        // two same roots
                        DataClass minusbovera = BuiltinProcedures.evaluateDivision(b, a);
                        minusbovera = BuiltinProcedures.evaluateMultiplication(datumMinus1, minusbovera);
                        DataClass big_Boverbig_A = BuiltinProcedures.evaluateDivision(big_B, big_A);
                        DataClass root1 = BuiltinProcedures.evaluateAdding(minusbovera, big_Boverbig_A);
                        DataClass root2 = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.HALF.negate()),
                                                            big_Boverbig_A);
                        DataClass root3 = BuiltinProcedures.evaluateMultiplication(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.HALF.negate()),
                                                            big_Boverbig_A);
                        listRoots.add(root1);
                        listRoots.add(root2);
                        listRoots.add(root3);
                    } else if (a.getImage().isActuallyZero() && b.getImage().isActuallyZero()
                            && c.getImage().isActuallyZero() && d.getImage().isActuallyZero()) {
                        //all parameters are real value
                        DataClass Y1 = new DataClass(), Y2 = new DataClass(), X1 = new DataClass(), X2 = new DataClass(), X3 = new DataClass();
                        MFPNumeric mfpSqrt3 = MFPNumeric.sqrt(new MFPNumeric(3.0));
                        DataClass big_Ab = BuiltinProcedures.evaluateMultiplication(big_A, b);
                        DataClass threeaover2 = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                        threeaover2 = BuiltinProcedures.evaluateDivision(threeaover2, datumTwo);
                        DataClass sqrtdelta = BuiltinProcedures.evaluatePower(delta, datumHalf, null);
                        Y1 = BuiltinProcedures.evaluateSubstraction(sqrtdelta, big_B);
                        Y1 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y1);
                        Y1 = BuiltinProcedures.evaluateAdding(big_Ab, Y1);
                        if (Y1.getImage().isActuallyZero()) {
                            MFPNumeric mfpY1 = (Y1.getDataType() == DATATYPES.DATUM_COMPLEX)?Y1.getReal():Y1.getDataValue();
                            if (mfpY1.isActuallyNegative()) {
                                MFPNumeric mfpY1Real = MFPNumeric.pow(mfpY1.negate(), datum1Over3.getDataValue()).divide(MFPNumeric.TWO);
                                MFPNumeric mfpY1Img = mfpY1Real.multiply(mfpSqrt3);
                                Y1 = new DataClass();
                                Y1.setComplex(mfpY1Real, mfpY1Img);
                            } else {
                                mfpY1 = MFPNumeric.pow(mfpY1, datum1Over3.getDataValue());
                                Y1 = new DataClass(DATATYPES.DATUM_DOUBLE, mfpY1);  // ensure that we alsways get real Y1.
                            }
                        } else {
                            DataClass datumY1s = BuiltinProcedures.evaluatePower(Y1, datum1Over3, datumThree);
                            datumY1s.changeDataType(DATATYPES.DATUM_REF_DATA);
                            if (datumY1s.getDataListSize() != 3) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_REFERENCE); 
                            }
                            MFPNumeric mfpLargestReal = datumY1s.getDataList()[0].getReal();
                            int nLargestRealIdx = 0;
                            for (int idx = 1; idx < 3; idx ++) {
                                if (datumY1s.getDataList()[idx].getReal().compareTo(mfpLargestReal) > 0) {
                                    mfpLargestReal = datumY1s.getDataList()[idx].getReal();
                                    nLargestRealIdx = idx;
                                }
                            }
                            Y1 = datumY1s.getDataList()[nLargestRealIdx];   // return Y1 with largest real.
                        }
                        Y2 = BuiltinProcedures.evaluateAdding(sqrtdelta, big_B);
                        Y2 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y2);
                        Y2 = BuiltinProcedures.evaluateSubstraction(big_Ab, Y2);
                        if (Y1.getImage().isActuallyZero()) {   // note: here use Y1 not Y2 to identify if we should return real value in **1/3
                            MFPNumeric mfpY2 = (Y2.getDataType() == DATATYPES.DATUM_COMPLEX)?Y2.getReal():Y2.getDataValue();
                            if (mfpY2.isActuallyNegative()) {
                                mfpY2 = MFPNumeric.pow(mfpY2.negate(), datum1Over3.getDataValue()).negate();
                            } else {
                                mfpY2 = MFPNumeric.pow(mfpY2, datum1Over3.getDataValue());
                            }
                            Y2 = new DataClass(DATATYPES.DATUM_DOUBLE, mfpY2);  // ensure that we alsways get real Y1.
                        } else if (Y2.getImage().isActuallyZero()) {    // Y1 ** 1/3 does not return real
                            MFPNumeric mfpY2 = (Y2.getDataType() == DATATYPES.DATUM_COMPLEX)?Y2.getReal():Y2.getDataValue();
                            if (mfpY2.isActuallyNegative()) {   // if Y2 is negative same as Y1, select the conjugate direction.
                                MFPNumeric mfpY2Real = MFPNumeric.pow(mfpY2.negate(), datum1Over3.getDataValue()).divide(MFPNumeric.TWO);
                                MFPNumeric mfpY2Img = mfpY2Real.multiply(mfpSqrt3).negate();
                                Y2 = new DataClass();
                                Y2.setComplex(mfpY2Real, mfpY2Img);
                            } else {   // if Y2 is positive different from Y1, select the real-conjugate direction.
                                MFPNumeric mfpY2Real = MFPNumeric.pow(mfpY2, datum1Over3.getDataValue()).divide(MFPNumeric.TWO).negate();
                                MFPNumeric mfpY2Img = mfpY2Real.multiply(mfpSqrt3).negate();
                                Y2 = new DataClass();
                                Y2.setComplex(mfpY2Real, mfpY2Img);
                            }
                        } else {
                            Y2 = new DataClass();
                            Y2.setComplex(Y1.getReal(), Y1.getImage().negate());    // Y1 and Y2 must be conjugate.
                        }
                        DataClass tmpDatum = BuiltinProcedures.evaluatePower(datumThree, datumHalf, null);
                        tmpDatum = BuiltinProcedures.evaluateMultiplication(tmpDatum, datumHalf);
                        DataClass sqrt3Halfi = new DataClass();
                        sqrt3Halfi.setComplex(MFPNumeric.ZERO, tmpDatum.getDataValue());
                        DataClass Threea = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                        DataClass Y1plusY2 = BuiltinProcedures.evaluateAdding(Y1, Y2);
                        DataClass Y1minusY2 = BuiltinProcedures.evaluateSubstraction(Y1, Y2);
                        X1 = BuiltinProcedures.evaluateAdding(b, Y1plusY2);
                        X1 = BuiltinProcedures.evaluateMultiplication(datumMinus1, X1);
                        X1 = BuiltinProcedures.evaluateDivision(X1, Threea);
                        DataClass MinusbplusHalfY1Y2 = BuiltinProcedures.evaluateMultiplication(datumHalf, Y1plusY2);
                        MinusbplusHalfY1Y2 = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1Y2, b);
                        tmpDatum = BuiltinProcedures.evaluateMultiplication(sqrt3Halfi, Y1minusY2);
                        X2 = BuiltinProcedures.evaluateAdding(MinusbplusHalfY1Y2, tmpDatum);
                        X2 = BuiltinProcedures.evaluateDivision(X2, Threea);
                        X3 = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1Y2, tmpDatum);
                        X3 = BuiltinProcedures.evaluateDivision(X3, Threea);
                        listRoots.add(X1);
                        listRoots.add(X2);
                        listRoots.add(X3);
                    } else  {
                        DataClass Y1 = new DataClass(), Y2 = new DataClass(), X1 = new DataClass(), X2 = new DataClass(), X3 = new DataClass();
                        DataClass big_Ab = BuiltinProcedures.evaluateMultiplication(big_A, b);
                        DataClass threeaover2 = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                        threeaover2 = BuiltinProcedures.evaluateDivision(threeaover2, datumTwo);
                        DataClass sqrtdelta = BuiltinProcedures.evaluatePower(delta, datumHalf, null);
                        Y1 = BuiltinProcedures.evaluateSubstraction(sqrtdelta, big_B);
                        Y1 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y1);
                        Y1 = BuiltinProcedures.evaluateAdding(big_Ab, Y1);
                        if (Y1.getImage().isActuallyZero()) {
                            MFPNumeric mfpY1 = (Y1.getDataType() == DATATYPES.DATUM_COMPLEX)?Y1.getReal():Y1.getDataValue();
                            if (mfpY1.isActuallyNegative()) {
                                mfpY1 = MFPNumeric.pow(mfpY1.negate(), datum1Over3.getDataValue()).negate();
                            } else {
                                mfpY1 = MFPNumeric.pow(mfpY1, datum1Over3.getDataValue());
                            }
                            Y1 = new DataClass(DATATYPES.DATUM_DOUBLE, mfpY1);  // ensure that we alsways get real Y1.
                        } else {
                            Y1 = BuiltinProcedures.evaluatePower(Y1, datum1Over3, null);
                        }
                        Y2 = BuiltinProcedures.evaluateAdding(sqrtdelta, big_B);
                        Y2 = BuiltinProcedures.evaluateMultiplication(threeaover2, Y2);
                        Y2 = BuiltinProcedures.evaluateSubstraction(big_Ab, Y2);
                        if (Y2.getImage().isActuallyZero()) {
                            MFPNumeric mfpY2 = (Y2.getDataType() == DATATYPES.DATUM_COMPLEX)?Y2.getReal():Y2.getDataValue();
                            if (mfpY2.isActuallyNegative()) {
                                mfpY2 = MFPNumeric.pow(mfpY2.negate(), datum1Over3.getDataValue()).negate();
                            } else {
                                mfpY2 = MFPNumeric.pow(mfpY2, datum1Over3.getDataValue());
                            }
                            Y2 = new DataClass(DATATYPES.DATUM_DOUBLE, mfpY2);  // ensure that we alsways get real Y1.
                        } else {
                            Y2 = BuiltinProcedures.evaluatePower(Y2, datum1Over3, null);
                        }
                        
                        DataClass selected_X1 = new DataClass(), selected_X2 = new DataClass(), selected_X3 = new DataClass();
                        MFPNumeric mfpNumMinabs_YX = MFPNumeric.MINUS_ONE;
                        DataClass tmpDatum = BuiltinProcedures.evaluatePower(datumThree, datumHalf, null);
                        tmpDatum = BuiltinProcedures.evaluateMultiplication(tmpDatum, datumHalf);
                        DataClass sqrt3Halfi = new DataClass();
                        sqrt3Halfi.setComplex(MFPNumeric.ZERO, tmpDatum.getDataValue());
                        // if do not consider the 9 rotating cases, we may not get right roots.
                        for (int idxY1 = 0; idxY1 <= 2; idxY1 ++)    {
                            for (int idxY2 = 0; idxY2 <= 2; idxY2 ++)   {
                                DataClass Y1_rotated = new DataClass(), Y2_rotated = new DataClass(),
                                        YX1 = new DataClass(), YX2 = new DataClass(), YX3 = new DataClass(),
                                        abs_YX = new DataClass();
                                if (idxY1 == 0) {
                                    Y1_rotated.copyTypeValueDeep(Y1);
                                } else if (idxY1 == 1)  {
                                    Y1_rotated = BuiltinProcedures.evaluateSubstraction(sqrt3Halfi, datumHalf);
                                    Y1_rotated = BuiltinProcedures.evaluateMultiplication(Y1, Y1_rotated);
                                } else  {
                                    Y1_rotated = BuiltinProcedures.evaluateAdding(sqrt3Halfi, datumHalf);
                                    Y1_rotated = BuiltinProcedures.evaluateMultiplication(Y1, Y1_rotated);
                                    Y1_rotated = BuiltinProcedures.evaluateMultiplication(datumMinus1, Y1_rotated);
                                }
                                if (idxY2 == 0) {
                                    Y2_rotated.copyTypeValueDeep(Y2);
                                } else if (idxY2 == 1)  {
                                    Y2_rotated = BuiltinProcedures.evaluateSubstraction(sqrt3Halfi, datumHalf);
                                    Y2_rotated = BuiltinProcedures.evaluateMultiplication(Y2, Y2_rotated);
                                } else  {
                                    Y2_rotated = BuiltinProcedures.evaluateAdding(sqrt3Halfi, datumHalf);
                                    Y2_rotated = BuiltinProcedures.evaluateMultiplication(Y2, Y2_rotated);
                                    Y2_rotated = BuiltinProcedures.evaluateMultiplication(datumMinus1, Y2_rotated);
                                }
                                DataClass Threea = BuiltinProcedures.evaluateMultiplication(datumThree, a);
                                DataClass Y1_rplusY2_r = BuiltinProcedures.evaluateAdding(Y1_rotated, Y2_rotated);
                                DataClass Y1_rminusY2_r = BuiltinProcedures.evaluateSubstraction(Y1_rotated, Y2_rotated);
                                X1 = BuiltinProcedures.evaluateAdding(b, Y1_rplusY2_r);
                                X1 = BuiltinProcedures.evaluateMultiplication(datumMinus1, X1);
                                X1 = BuiltinProcedures.evaluateDivision(X1, Threea);
                                DataClass MinusbplusHalfY1rY2r = BuiltinProcedures.evaluateMultiplication(datumHalf, Y1_rplusY2_r);
                                MinusbplusHalfY1rY2r = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1rY2r, b);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(sqrt3Halfi, Y1_rminusY2_r);
                                X2 = BuiltinProcedures.evaluateAdding(MinusbplusHalfY1rY2r, tmpDatum);
                                X2 = BuiltinProcedures.evaluateDivision(X2, Threea);
                                X3 = BuiltinProcedures.evaluateSubstraction(MinusbplusHalfY1rY2r, tmpDatum);
                                X3 = BuiltinProcedures.evaluateDivision(X3, Threea);

                                tmpDatum = BuiltinProcedures.evaluatePower(X1, datumThree, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(a, tmpDatum);
                                YX1.copyTypeValueDeep(tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluatePower(X1, datumTwo, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(b, tmpDatum);
                                YX1 = BuiltinProcedures.evaluateAdding(YX1, tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(c, X1);
                                YX1 = BuiltinProcedures.evaluateAdding(YX1, tmpDatum);
                                YX1 = BuiltinProcedures.evaluateAdding(YX1, d);
                                tmpDatum = BuiltinProcedures.evaluatePower(X2, datumThree, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(a, tmpDatum);
                                YX2.copyTypeValueDeep(tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluatePower(X2, datumTwo, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(b, tmpDatum);
                                YX2 = BuiltinProcedures.evaluateAdding(YX2, tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(c, X2);
                                YX2 = BuiltinProcedures.evaluateAdding(YX2, tmpDatum);
                                YX2 = BuiltinProcedures.evaluateAdding(YX2, d);
                                tmpDatum = BuiltinProcedures.evaluatePower(X3, datumThree, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(a, tmpDatum);
                                YX3.copyTypeValueDeep(tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluatePower(X3, datumTwo, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(b, tmpDatum);
                                YX3 = BuiltinProcedures.evaluateAdding(YX3, tmpDatum);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(c, X3);
                                YX3 = BuiltinProcedures.evaluateAdding(YX3, tmpDatum);
                                YX3 = BuiltinProcedures.evaluateAdding(YX3, d);
                                MFPNumeric mfpNumabs_YX = YX1.getComplexRadAngle()[0].add(YX2.getComplexRadAngle()[0]).add(YX3.getComplexRadAngle()[0]);
                                if (mfpNumMinabs_YX.isActuallyNegative()
                                        || mfpNumabs_YX.compareTo(mfpNumMinabs_YX) < 0) {
                                    mfpNumMinabs_YX = mfpNumabs_YX;
                                    selected_X1 = X1;
                                    selected_X2 = X2;
                                    selected_X3 = X3;
                                }
                            }
                        }
                        listRoots.add(selected_X1);
                        listRoots.add(selected_X2);
                        listRoots.add(selected_X3);
                    }
                }
                break;
            }
            default:
            {
                DataClass x_real = new DataClass(), x_image = new DataClass(), y_value = new DataClass(), y_last_value = new DataClass(),
                        stop_threshold = new DataClass(), root_avg = new DataClass(), root_abs_avg = new DataClass();
                int num_tried_pnts, max_starting_pnts_try, max_iteration_steps;
                boolean root_found = false;
                if (listParams.getLast().isEqual(datumZero))    {
                    x_real.copyTypeValueDeep(datumZero);
                    x_image.copyTypeValueDeep(datumZero);
                } else  {
                    y_last_value.copyTypeValueDeep(datumZero);
                    max_starting_pnts_try = 8;
                    max_iteration_steps = 100;
                    stop_threshold = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.00000001"));
                    root_found = false;
                    root_avg = BuiltinProcedures.evaluateDivision(listParams.get(nNonZeroStart + 1), listParams.get(nNonZeroStart));
                    root_avg = BuiltinProcedures.evaluateDivision(root_avg, new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(nParamNumber - 1)));
                    root_abs_avg = BuiltinProcedures.evaluateDivision(listParams.get(nParamNumber - 1), listParams.get(nNonZeroStart));
                    if ((nParamNumber % 2 == 0) && (nParamNumber > 2)   // root_abs_avg ** 1/odd value.
                            && (root_abs_avg.isSingleDouble() || root_abs_avg.isSingleInteger() || root_abs_avg.isSingleBoolean()
                                || (root_abs_avg.getDataType() == DATATYPES.DATUM_COMPLEX && root_abs_avg.getImage().isActuallyZero()))) {
                        MFPNumeric mfproot_abs_avg = (root_abs_avg.getDataType() == DATATYPES.DATUM_COMPLEX)?
                                                    root_abs_avg.getReal():root_abs_avg.getDataValue();
                        if (mfproot_abs_avg.isActuallyNegative()) {
                            mfproot_abs_avg = MFPNumeric.pow(mfproot_abs_avg.negate(), new MFPNumeric(1.0/(nParamNumber - 1.0))).negate();
                        } else {
                            mfproot_abs_avg = MFPNumeric.pow(mfproot_abs_avg, new MFPNumeric(1.0/(nParamNumber - 1.0)));
                        }
                        root_abs_avg = new DataClass(DATATYPES.DATUM_DOUBLE, mfproot_abs_avg);  // ensure that we alsways get real Y1.
                    } else {
                        root_abs_avg = BuiltinProcedures.evaluatePower(root_abs_avg,
                            new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(1.0/(nParamNumber - 1.0))),
                            null);
                    }
                    MFPNumeric mfpNumRootAbsAvg = root_abs_avg.getComplexRadAngle()[0],
                            mfpNumRootAvg = root_avg.getComplexRadAngle()[0];
                    if (mfpNumRootAbsAvg.compareTo(mfpNumRootAvg) > 0)  {
                        root_abs_avg = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumRootAbsAvg);
                    } else  {
                        root_abs_avg = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumRootAvg);
                    }
                    x_real = root_avg.getRealDataClass();
                    x_image = root_avg.getImageDataClass();
                    num_tried_pnts = 0;
                    do {
                        // try to use Newton Raphson method to find roots
                        for (int idx = 0; idx <= max_iteration_steps; idx ++)   {
                            y_value.copyTypeValueDeep(datumZero);
                            for (int idx1 = nNonZeroStart; idx1 < nParamNumber; idx1 ++)    {
                                DataClass x_real_image = new DataClass();
                                x_real_image.copyTypeValueDeep(x_image);
                                x_real_image = BuiltinProcedures.evaluateMultiplication(datumi, x_real_image);
                                x_real_image = BuiltinProcedures.evaluateAdding(x_real, x_real_image);
                                DataClass tmpDatum = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(nParamNumber - 1 - idx1));
                                tmpDatum = BuiltinProcedures.evaluatePower(x_real_image, tmpDatum, null);
                                tmpDatum = BuiltinProcedures.evaluateMultiplication(listParams.get(idx1), tmpDatum);
                                y_value = BuiltinProcedures.evaluateAdding(y_value, tmpDatum);
                            }
                            if (y_value.getComplexRadAngle()[0].compareTo(stop_threshold.getDataValue()) < 0)   {   // find root
                                root_found = true;
                                break;
                            } else  {
                                DataClass y_dash_value = new DataClass();
                                y_dash_value.copyTypeValueDeep(datumZero);
                                for (int idx1 = nNonZeroStart; idx1 < nParamNumber - 1; idx1 ++)    {
                                    DataClass x_real_image = new DataClass();
                                    x_real_image.copyTypeValueDeep(x_image);
                                    x_real_image = BuiltinProcedures.evaluateMultiplication(datumi, x_real_image);
                                    x_real_image = BuiltinProcedures.evaluateAdding(x_real, x_real_image);
                                    DataClass tmpDatum = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(nParamNumber - 2 - idx1));
                                    tmpDatum = BuiltinProcedures.evaluatePower(x_real_image, tmpDatum, null);
                                    tmpDatum = BuiltinProcedures
                                                .evaluateMultiplication(
                                                                new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(nParamNumber - 1 - idx1)),
                                                                tmpDatum);
                                    tmpDatum = BuiltinProcedures.evaluateMultiplication(listParams.get(idx1), tmpDatum);
                                    y_dash_value = BuiltinProcedures.evaluateAdding(y_dash_value, tmpDatum);
                                }
                                if (y_dash_value.isEqual(datumZero) == false)  {
                                    DataClass tmpDatum = new DataClass();
                                    tmpDatum = BuiltinProcedures.evaluateDivision(y_value, y_dash_value);
                                    if (idx % 2 == 0)   {
                                        x_real = BuiltinProcedures.evaluateSubstraction(x_real, tmpDatum);
                                    } else  {
                                        x_image = BuiltinProcedures.evaluateAdding(x_image, tmpDatum);
                                    }
                                } else  {
                                    break;  // have to change to another starting point
                                }
                                DataClass new_x = new DataClass();
                                new_x.copyTypeValueDeep(x_image);
                                new_x = BuiltinProcedures.evaluateMultiplication(datumi, new_x);
                                new_x = BuiltinProcedures.evaluateAdding(x_real, new_x);
                                x_real = new_x.getRealDataClass();
                                x_image = new_x.getImageDataClass();
                            }
                        }
                        if (root_found) {
                            break;
                        }
                        DataClass datumRand1 = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(Math.random())),
                                datumRand2 = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(Math.random()));
                        DataClass radius_4_new_x = BuiltinProcedures.evaluateMultiplication(datumRand1, root_abs_avg);
                        DataClass degree_4_new_x = BuiltinProcedures.evaluateMultiplication(datumRand2,
                                                                        new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.PI));
                        degree_4_new_x = BuiltinProcedures.evaluateMultiplication(datumTwo, degree_4_new_x);
                        DataClass tmpDatum = BuiltinProcedures.evaluateMultiplication(radius_4_new_x,
                                                    BuiltinProcedures.evaluateCos(degree_4_new_x));
                        x_real = BuiltinProcedures.evaluateAdding(root_avg.getRealDataClass(), tmpDatum);
                        x_image = BuiltinProcedures.evaluateAdding(root_avg.getImageDataClass(), tmpDatum);
                        num_tried_pnts = num_tried_pnts + 1;
                    } while (num_tried_pnts < max_starting_pnts_try);
                    if (!root_found)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                }
                // now we get one root which is x_value
                DataClass x_value = new DataClass(), last_param = new DataClass();
                LinkedList<DataClass> listNewParams = new LinkedList<DataClass>();
                x_value.copyTypeValueDeep(x_image);
                x_value = BuiltinProcedures.evaluateMultiplication(datumi, x_value);
                x_value = BuiltinProcedures.evaluateAdding(x_real, x_value);
                last_param.copyTypeValueDeep(datumZero);
                for (int idx = nNonZeroStart; idx < nParamNumber - 1; idx ++)   {
                    DataClass datumNewParam = BuiltinProcedures.evaluateMultiplication(x_value, last_param);
                    datumNewParam = BuiltinProcedures.evaluateAdding(listParams.get(idx), datumNewParam);
                    listNewParams.add(datumNewParam);
                    last_param = listNewParams.get(idx - nNonZeroStart);
                }
                listRoots.addFirst(x_value);
                LinkedList<DataClass> listNewRoots = solvePolynomial(listNewParams, functionInterrupter);
                for (int idx = 0; idx < listNewRoots.size(); idx ++)    {
                    listRoots.add(listNewRoots.get(idx));
                }
                break;
            }
        }
        return listRoots;
    }
 
    public static int[][] getPermutations(int n, LinkedList<Long> listReversedPairCnts, FunctionInterrupter functionInterrupter) throws JFCALCExpErrException, InterruptedException   {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        if (n == 1)  {
            listReversedPairCnts.clear();
            listReversedPairCnts.add(0l);
            int[][] narrayReturn = new int[1][1];
            narrayReturn[0][0] = 0;
            return narrayReturn;
        } else if (n > 12 || n <= 0)    {
            //13! > 2**32, overflow
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        } else {
            int[][] narrayLastReturn = getPermutations(n - 1, listReversedPairCnts, functionInterrupter);
            int[][] narrayReturn = new int[narrayLastReturn.length * n][n];
            LinkedList<Long> listReversedPairCntsNew = new LinkedList<Long>();
            int idxNew0 = 0;
            for (int idx0 = 0; idx0 < narrayLastReturn.length; idx0 ++) {
                for (int idx = 0; idx < n; idx ++)  {
                    int[] narrayThisList = new int[n];
                    long nThisReversedCnt = listReversedPairCnts.get(idx0);
                    // need not to worry that narrayLastReturn[idx0][idx1] > n if idx1 < idx because n should be the largest one
                    if (idx > 0)    {
                        System.arraycopy(narrayLastReturn[idx0], 0, narrayThisList, 0, idx); // copy the first idx elements
                    }
                    narrayThisList[idx] = n - 1;
                    if (idx < n - 1)    {
                        nThisReversedCnt += n - 1 - idx;
                        System.arraycopy(narrayLastReturn[idx0], idx, narrayThisList, idx + 1, n - 1 - idx); // copy the last n - 1 - idx elements
                    }
                    idxNew0 = idx0 * n + idx;
                    narrayReturn[idxNew0] = narrayThisList;
                    listReversedPairCntsNew.add(nThisReversedCnt);
                }
            }
            listReversedPairCnts.clear();
            listReversedPairCnts.addAll(listReversedPairCntsNew);
            return narrayReturn;
        }
    }
    
    public static DataClass[] multiplyPolynomial(DataClass[] datumParams1, DataClass[] datumParams2, FunctionInterrupter functionInterrupter)  throws JFCALCExpErrException, InterruptedException   {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        // this function will not change the contents in datumParams1 and datumParams2.
        if (datumParams1.length == 0 || datumParams2.length == 0) {
            return new DataClass[0];
        }
        DataClass datumParamsRet[] = new DataClass[datumParams1.length + datumParams2.length - 1];
        for (int idx = 0; idx < datumParamsRet.length; idx ++) {
            datumParamsRet[idx] = new DataClass();
            datumParamsRet[idx].setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
        }
        for (int idx1 = 0; idx1 < datumParams1.length; idx1 ++)   {
            for (int idx2 = 0; idx2 < datumParams2.length; idx2 ++)   {
                datumParamsRet[idx1 + idx2] = BuiltinProcedures.evaluateAdding(datumParamsRet[idx1 + idx2],
                                                    BuiltinProcedures.evaluateMultiplication(datumParams1[idx1], datumParams2[idx2]));
            }
        }
        // remove initial zeros
        int idx = datumParamsRet.length - 1;
        for (; idx >= 1; idx --)    {
            if (!datumParamsRet[idx].isZeros(false))  {
                break;
            }
        }
        DataClass datumParamsRetShrinked[] = new DataClass[idx + 1];
        for (idx = 0; idx < datumParamsRetShrinked.length; idx ++) {
            datumParamsRetShrinked[idx] = datumParamsRet[idx];
        }
        return datumParamsRetShrinked;
    }
 
    public static DataClass[] addSubPolynomial(DataClass[] datumParams1, DataClass[] datumParams2, boolean bIsAdd, FunctionInterrupter functionInterrupter)  throws JFCALCExpErrException, InterruptedException   {
        if (functionInterrupter != null)    {
            // for debug or killing a background thread. Every heavy functions need to have this check.
            if (functionInterrupter.shouldInterrupt())    {
                functionInterrupter.interrupt();
            }
        }
        // this function will not change the contents in datumParams1 and datumParams2.
        DataClass datumParamsRet[] = new DataClass[Math.max(datumParams1.length, datumParams2.length)];
        for (int idx = 0; idx < datumParamsRet.length; idx ++)   {
            DataClass datumParam1, datumParam2;
            if (idx >= datumParams1.length) {
                datumParam1 = new DataClass();
                datumParam1.setDataValue(0);
            } else {
                datumParam1 = datumParams1[idx];
            }
            if (idx >= datumParams2.length) {
                datumParam2 = new DataClass();
                datumParam2.setDataValue(0);
            } else {
                datumParam2 = datumParams2[idx];
            }
            if (bIsAdd) {
                datumParamsRet[idx] = BuiltinProcedures.evaluateAdding(datumParam1, datumParam2);
            } else  {
                datumParamsRet[idx] = BuiltinProcedures.evaluateSubstraction(datumParam1, datumParam2);
            }
        }
        // remove initial zeros
        int idx = datumParamsRet.length - 1;
        for (; idx >= 1; idx --)    {
            if (!datumParamsRet[idx].isZeros(false))  {
                break;
            }
        }
        DataClass datumParamsRetShrinked[] = new DataClass[idx + 1];
        for (idx = 0; idx < datumParamsRetShrinked.length; idx ++) {
            datumParamsRetShrinked[idx] = datumParamsRet[idx];
        }
        return datumParamsRetShrinked;
    }
    
    public static DataClass getLimValue(DataClass[] datumArrayXs, DataClass[] datumArrayF_Xs, DataClass datumX0, DataClass datumF_X0) throws JFCALCExpErrException   {
        // datumArrayXs are the x values before lim to x0, datumArrayF_Xs are the f(x) values before lim to x0
        // datumX0 are the x0 value and datumF_X0 are the f(x0) value.
        // parameters in this function will not be changed. returned value is not parameter (will deep copy if needed).
        if (datumArrayXs.length != datumArrayF_Xs.length)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
        } else if (datumArrayXs.length <= 1)    {
            DataClass datumReturn = datumF_X0.cloneSelf();
            return datumReturn;
        }
        
        // at this monent, only support slope which is a value (complex or simple value)
        MFPNumeric[] mfpNumArrayPowers = new MFPNumeric[datumArrayXs.length - 1];
        MFPNumeric mfpNumFinalPower = MFPNumeric.ONE;
        for (int idx = 1; idx < datumArrayXs.length; idx ++)    {
            // calculate the power of each interval.
            MFPNumeric mfpNumAbsDelta = BuiltinProcedures.evaluateSubstraction(datumArrayF_Xs[idx], datumArrayF_Xs[idx - 1]).getComplexRadAngle()[0];
            MFPNumeric mfpNumAbsStep = BuiltinProcedures.evaluateSubstraction(datumArrayXs[idx], datumArrayXs[idx - 1]).getComplexRadAngle()[0];
            mfpNumArrayPowers[idx - 1] = MFPNumeric.divide(MFPNumeric.log(mfpNumAbsDelta), MFPNumeric.log(mfpNumAbsStep));
        }
        if (mfpNumArrayPowers.length >= 2)    {
            mfpNumFinalPower = mfpNumArrayPowers[mfpNumArrayPowers.length - 1].multiply(MFPNumeric.TWO)
                    .subtract(mfpNumArrayPowers[mfpNumArrayPowers.length - 2]);
        } else if (mfpNumArrayPowers.length == 1)   {
            mfpNumFinalPower = mfpNumArrayPowers[0];
        }

        MFPNumeric mfpNumAbsFinalStep = BuiltinProcedures.evaluateSubstraction(datumX0, datumArrayXs[datumArrayXs.length - 1]).getComplexRadAngle()[0];
        MFPNumeric mfpNumAbsFinalDelta = MFPNumeric.pow(mfpNumAbsFinalStep, mfpNumFinalPower);
        MFPNumeric mfpNumLastDeltaAngle = BuiltinProcedures.evaluateSubstraction(datumArrayF_Xs[datumArrayF_Xs.length - 1],
                                                                    datumArrayF_Xs[datumArrayF_Xs.length - 2]).getComplexRadAngle()[1];
        DataClass datumFinalDelta = new DataClass();
        datumFinalDelta.setComplexRadAngle(mfpNumAbsFinalDelta, mfpNumLastDeltaAngle);
        DataClass datumFinalValue = BuiltinProcedures.evaluateAdding(datumArrayF_Xs[datumArrayF_Xs.length - 1], datumFinalDelta);

        MFPNumeric mfpNumFinalReal = datumFinalValue.getReal();
        MFPNumeric mfpNumFinalImage = datumFinalValue.getImage();
        MFPNumeric mfpNumF_X0Rad = datumF_X0.getComplexRadAngle()[0];
        MFPNumeric mfpNumF_X0Real = datumF_X0.getReal();
        MFPNumeric mfpNumF_X0Image = datumF_X0.getImage();
        if (!mfpNumF_X0Rad.isNanOrInf())  {
            mfpNumFinalReal = mfpNumF_X0Real;
            mfpNumFinalImage = mfpNumF_X0Image;
        } else if (mfpNumF_X0Real.isInf() && !mfpNumF_X0Image.isActuallyZero()) {
            // real is infinite and image is not zero. image can be independent from real.
            if (mfpNumFinalReal.isActuallyPositive())    {
                mfpNumFinalReal = MFPNumeric.INF;
            } else if (mfpNumFinalReal.isActuallyNegative()) {
                mfpNumFinalReal = MFPNumeric.NEG_INF;
            } else {
                mfpNumFinalReal = MFPNumeric.ZERO;
            }
            mfpNumFinalImage = mfpNumF_X0Image;
        } else if (mfpNumF_X0Image.isInf() && !mfpNumF_X0Real.isActuallyZero()) {
            // image is infinite and real is not zero. real can be indpendent from image.
            if (mfpNumFinalImage.isActuallyPositive())    {
                mfpNumFinalImage = MFPNumeric.INF;
            } else if (mfpNumFinalImage.isActuallyNegative()) {
                mfpNumFinalImage = MFPNumeric.NEG_INF;
            } else {
                mfpNumFinalImage = MFPNumeric.ZERO;
            }
            mfpNumFinalReal = mfpNumF_X0Real;
        } else {
            if (mfpNumFinalReal.doubleValue() >= MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX) {
                mfpNumFinalReal = MFPNumeric.INF;
            } else if (mfpNumFinalReal.doubleValue() <= -MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX) {
                mfpNumFinalReal = MFPNumeric.NEG_INF;
            } else if (mfpNumFinalReal.doubleValue() <= MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN
                    && mfpNumFinalReal.doubleValue() >= -MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN) {
                mfpNumFinalReal = MFPNumeric.ZERO;
            } else if (mfpNumF_X0Rad.isInf())   {
                if (mfpNumFinalReal.isActuallyPositive())    {
                    mfpNumFinalReal = MFPNumeric.INF;
                } else if (mfpNumFinalReal.isActuallyNegative()) {
                    mfpNumFinalReal = MFPNumeric.NEG_INF;
                } else {
                    mfpNumFinalReal = MFPNumeric.ZERO;
                }
            }
            if (mfpNumFinalImage.doubleValue() >= MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX) {
                mfpNumFinalImage = MFPNumeric.INF;
            } else if (mfpNumFinalImage.doubleValue() <= -MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX) {
                mfpNumFinalImage = MFPNumeric.NEG_INF;
            } else if (mfpNumFinalImage.doubleValue() <= MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN
                    && mfpNumFinalImage.doubleValue() >= -MFPNumeric.PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN) {
                mfpNumFinalImage = MFPNumeric.ZERO;
            } else if (mfpNumF_X0Rad.isInf()) {
                if (mfpNumFinalImage.isActuallyPositive())    {
                    mfpNumFinalImage = MFPNumeric.INF;
                } else if (mfpNumFinalImage.isActuallyNegative()) {
                    mfpNumFinalImage = MFPNumeric.NEG_INF;
                } else {
                    mfpNumFinalImage = MFPNumeric.ZERO;
                }
            }
        }
        
        datumFinalValue.setComplex(mfpNumFinalReal, mfpNumFinalImage);
        return datumFinalValue;
    }
    
    // initialize some constants matrices for integration.
    static final DataClass msdarrayAbsCissa[] = initGaussKronrodMatrices(0);
    static final DataClass msdarrayWeights7[] = initGaussKronrodMatrices(1);
    static final DataClass msdarrayWeights15[] = initGaussKronrodMatrices(2);

    public static DataClass[] integByGaussKronrod(AbstractExpr aeStrExpr/*String strIntegExpr*/, String strVariable, DataClass datumFrom, DataClass datumTo,
            int nMaxSteps, boolean bCheckConverge, boolean bExceptNotEnoughSteps, boolean bCheckFinalResult,
            LinkedList<LinkedList<Variable>> lVarNameSpaces, FunctionInterrupter functionInterrupter) throws JFCALCExpErrException, InterruptedException {

        // first of all, need to validate parameters.
        // from and to must be complex.
        datumFrom.changeDataType(DATATYPES.DATUM_COMPLEX);
        datumTo.changeDataType(DATATYPES.DATUM_COMPLEX);
        
        MFPNumeric mfpFromReal = datumFrom.getReal();
        MFPNumeric mfpFromImage = datumFrom.getImage();
        MFPNumeric mfpToReal = datumTo.getReal();
        MFPNumeric mfpToImage = datumTo.getImage();
        
        if (mfpFromReal.isNan() || mfpFromImage.isNan() || mfpToReal.isNan() || mfpToImage.isNan()) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if ((mfpFromReal.isInf() && !mfpFromImage.isActuallyZero()) || (mfpToReal.isInf() && !mfpToImage.isActuallyZero())
                || (!mfpFromReal.isActuallyZero() && mfpFromImage.isInf()) || (!mfpToReal.isActuallyZero() && mfpToImage.isInf())) {
            // only support integrate from -inf to inf, any real value to inf, -inf to any real value or
            // -inf * i to inf * i, any image value to inf * i and -inf * i to any image value.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if ((mfpFromReal.isInf() && !mfpToImage.isActuallyZero()) || (mfpFromImage.isInf() && !mfpToReal.isActuallyZero())
                || (mfpToReal.isInf() && !mfpFromImage.isActuallyZero()) || (mfpToImage.isInf() && !mfpFromReal.isActuallyZero())) {
            // if one is inf, the other 's real (if inf image) or image (if inf real) must be 0.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        } else if (mfpFromReal.isEqual(mfpToReal) && mfpFromImage.isEqual(mfpToImage)) {
            // from to are equal, the integrated result is always zero.
            return new DataClass[] {
                new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO),
                new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO)
            };
        }
        
        // now integrate:
        boolean bNeed2MultiplyMinus1 = false;
        // initialize some constants
        DataClass datumTwo = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO);
        double abstol = 1e-10;
        double reltol = 1e-5;
        DataClass[] darraySubs = new DataClass[11];
        int nDefaultMaxCalcIntervals = 256;  // matlab use 650 as default nMaxSteps. I select 256.
        int nNumOfErrIncreases = 0;
        int nMaxNumOfErrIncreases = 6;
        if (nMaxSteps <= 0) {
            nMaxSteps = nDefaultMaxCalcIntervals;
        } else if (nMaxSteps > nDefaultMaxCalcIntervals) {
            nMaxSteps = nDefaultMaxCalcIntervals; 
        }
        boolean bNotConvergeExit = false, bNotEnoughStepsExit = false;
        
        DataClass datumH = new DataClass();
        //String strCvtVarName = "internal_var0"; // cannot simply use strVariable + "1" because it may be another variable's name.
        //String strTransFunc = "";
        //String strCvtIntegExpr = strIntegExpr;
        int nIntegMode = -4; // 1 means -inf to inf, -1 means -infi to infi, 2 means -inf to something, -2 means -infi to something,
                            // 3 means something to inf, -3 means something to -infi, 4 means real to real, -4 means complex to complex.

        if (!mfpFromImage.isActuallyZero() || !mfpToImage.isActuallyZero()) { // from or to is a complex value.
            if ((mfpFromImage.isInf() || mfpToImage.isInf()) && mfpFromImage.compareTo(mfpToImage) > 0) {
                // Swap from and to if to and from are simple image value and to image < from image.
                // Note that only support simple image to from if one of them is (negative) inf i.
                bNeed2MultiplyMinus1 = true;
                MFPNumeric mfpSwapTmp = mfpFromImage;
                mfpFromImage = mfpToImage;
                mfpToImage = mfpSwapTmp;
                DataClass datumSwapTmp = datumFrom;
                datumFrom = datumTo;
                datumTo = datumSwapTmp;
            }
            if (mfpFromImage.isInf() && mfpToImage.isInf()) {    // both from and to are inf
                initGaussKronrodSubRanges(-1.0, 1.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO);
                //strTransFunc = "i*" + strCvtVarName + "/(1-" + strCvtVarName + "**2)";
                //strCvtIntegExpr = "i*(" + strIntegExpr + ")*(1+" + strCvtVarName + "**2)/((1-" + strCvtVarName + "**2)**2)";
                nIntegMode = -1;
            } else if (mfpFromImage.isInf()) {   // only from is inf
                initGaussKronrodSubRanges(-1.0, 0.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE);
                //strTransFunc = datumTo.output() + "-i*(" + strCvtVarName + "/(1+" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "-2i*" + strCvtVarName + "*(" + strIntegExpr + ")/(1+" + strCvtVarName + ")**3"; 
                nIntegMode = -2;
            } else if (mfpToImage.isInf()) { // only to is inf
                initGaussKronrodSubRanges(0.0, 1.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE);
                //strTransFunc = datumFrom.output() + "+i*(" + strCvtVarName + "/(1-" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "2i*" + strCvtVarName + "*(" + strIntegExpr + ")/(1-" + strCvtVarName + ")**3"; 
                nIntegMode = -3;
            } else {    // from and to are not inf
                darraySubs = new DataClass[2];
                darraySubs[0] = datumFrom;
                darraySubs[1] = datumTo;
                datumH = BuiltinProcedures.evaluateSubstraction(datumFrom, datumTo);
                datumH = new DataClass(DATATYPES.DATUM_DOUBLE, datumH.getComplexRadAngle()[0]); // datumH has to be a real value.
                //strTransFunc = strCvtVarName;
                //strCvtIntegExpr = strIntegExpr;
                nIntegMode = -4;
            }
        } else {    // integrate from and to are both real values
            if (mfpFromReal.compareTo(mfpToReal) > 0) {
                // swap from and to.
                bNeed2MultiplyMinus1 = true;
                MFPNumeric mfpSwapTmp = mfpFromReal;
                mfpFromReal = mfpToReal;
                mfpToReal = mfpSwapTmp;
                DataClass datumSwapTmp = datumFrom;
                datumFrom = datumTo;
                datumTo = datumSwapTmp;
            }
            if (mfpFromReal.isInf()&& mfpToReal.isInf()) {    // both from and to are inf
                initGaussKronrodSubRanges(-1.0, 1.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO);
                //strTransFunc = strCvtVarName + "/(1-" + strCvtVarName + "**2)";
                //strCvtIntegExpr = "(" + strIntegExpr + ")*(1+" + strCvtVarName + "**2)/((1-" + strCvtVarName + "**2)**2)";
                nIntegMode = 1;
            } else if (mfpFromReal.isInf()) {   // only from is inf
                initGaussKronrodSubRanges(-1.0, 0.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE);
                //strTransFunc = datumTo.output() + "-(" + strCvtVarName + "/(1+" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "-2*" + strCvtVarName + "*(" + strIntegExpr + ")/(1+" + strCvtVarName + ")**3"; 
                nIntegMode = 2;
            } else if (mfpToReal.isInf()) { // only to is inf
                initGaussKronrodSubRanges(0.0, 1.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE);
                //strTransFunc = datumFrom.output() + "+(" + strCvtVarName + "/(1-" + strCvtVarName + "))**2";
                //strCvtIntegExpr = "2*" + strCvtVarName + "*(" + strIntegExpr + ")/(1-" + strCvtVarName + ")**3"; 
                nIntegMode = 3;
            } else {    // from and to are not inf
                initGaussKronrodSubRanges(-1.0, 1.0, darraySubs);
                datumH = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.TWO);
                //strTransFunc = "((" + datumTo.output() + "-" + datumFrom.output() + ")/4)*" + strCvtVarName + "*(3-" + strCvtVarName + "**2)+(" + datumTo.output() + "+" + datumFrom.output() + ")/2";
                //strCvtIntegExpr = "(" + strIntegExpr + ")*3*(" + datumTo.output() + "-" + datumFrom.output() + ")/4*(1-" + strCvtVarName + "**2)";
                nIntegMode = 4;
            }
        }

        // Split interval into at least 10 sub-interval with a 15 point
        // Gauss-Kronrod rule giving a minimum of 150 function evaluations
        while (darraySubs.length < 11) {
            DataClass[] darraySubsNew = new DataClass[darraySubs.length * 2 - 1];
            darraySubsNew[0] = darraySubs[0];
            for (int idx = 1; idx < darraySubs.length; idx ++) {
                darraySubsNew[idx*2 - 1] = BuiltinProcedures.evaluateAdding(darraySubs[idx], darraySubs[idx - 1]);
                darraySubsNew[idx*2 - 1] = BuiltinProcedures.evaluateDivision(darraySubsNew[idx*2 - 1], datumTwo);
                darraySubsNew[idx*2] = darraySubs[idx]; // dont think we need to clone.
            }
            darraySubs = darraySubsNew;
        }
        DataClass[][] darraySubIntervals = new DataClass[darraySubs.length - 1][2];
        for (int idx = 1; idx < darraySubs.length; idx ++) {
            darraySubIntervals[idx - 1][0] = darraySubs[idx - 1];
            darraySubIntervals[idx - 1][1] = darraySubs[idx];
        }
	
        DataClass[][] darrayResult = integByGaussKronrodCore(aeStrExpr/*strIntegExpr*/, strVariable, datumFrom, datumTo, nIntegMode,
                                                    darraySubIntervals, msdarrayAbsCissa, msdarrayWeights15, msdarrayWeights7,
                                                    lVarNameSpaces, functionInterrupter);
        DataClass datumQ0 = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
        DataClass datumErr0 = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
        DataClass datumErr0Last = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.INF);
        for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
            datumQ0 = BuiltinProcedures.evaluateAdding(datumQ0, darrayResult[0][idx]);
            datumErr0 = BuiltinProcedures.evaluateAdding(datumErr0, darrayResult[1][idx]);
        }
        
        DataClass datumQ = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
        DataClass datumErr = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
        
        // now start to do the calculation.
    	double dULP = Math.ulp(1.0);
        MFPNumeric mfpIntervalThresh = new MFPNumeric(dULP*100);
        int nTotalCalcSubIntervals = darraySubIntervals.length;
        while (true) {
            // Check for sub-intervals that are too small. Test must be
            // performed in untransformed sub-intervals. What is a good
            // value for this test. Shampine suggests 100*eps
            // Check for infinite sub-interval integration result. If there
            // is, exit.
            DataClass datumFrom2To = BuiltinProcedures.evaluateSubstraction(datumTo, datumFrom);
            boolean bFoundSmallInterval = false;
            boolean bFoundInfQSub = false;
            for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
                DataClass datumItvlEnd1 = integGKTransF(darraySubIntervals[idx][1], datumFrom, datumTo, nIntegMode);
                DataClass datumItvlEnd0 = integGKTransF(darraySubIntervals[idx][0], datumFrom, datumTo, nIntegMode);
                DataClass datumRelInterval = BuiltinProcedures.evaluateSubstraction(datumItvlEnd1, datumItvlEnd0);
                datumRelInterval = BuiltinProcedures.evaluateDivision(datumRelInterval, datumFrom2To);
                if (!(mfpFromReal.isInf() || mfpToReal.isInf() || mfpFromImage.isInf() || mfpToImage.isInf())   // if to or from is inf do not check small interval
                        && datumRelInterval.getComplexRadAngle()[0].compareTo(mfpIntervalThresh) <= 0) {
                    bFoundSmallInterval = true;
                    break;
                }
                if (darrayResult[0][idx].getReal().isInf() || darrayResult[0][idx].getImage().isInf()) {
                    bFoundInfQSub = true;
                    break;
                }
            }
            if (bFoundSmallInterval || bFoundInfQSub) {
                datumQ = datumQ0;
                datumErr = datumErr0;
                /*if ((mfpFromReal.isInf() || mfpToReal.isInf() || mfpFromImage.isInf() || mfpToImage.isInf())
                        && bFoundSmallInterval) {
                    // do not check converge if one of to and from is infinite.
                    bCheckConverge = false;
                }*/
                break;
            }
            
            // If the global error estimate is meet exit
            MFPNumeric mfpTolerance = datumQ0.getComplexRadAngle()[0].multiply(new MFPNumeric(reltol));
            if (mfpTolerance.compareTo(new MFPNumeric(abstol)) < 0) {
                mfpTolerance  = new MFPNumeric(abstol);
            }
            
            if (datumErr0.getDataValue().compareTo(mfpTolerance) < 0) {
                datumQ = datumQ0;
                datumErr = datumErr0;
                break;
            }
		
            datumQ0 = datumQ;
            datumErr0Last = datumErr0;
            datumErr0 = datumErr;
            LinkedList<Integer> listRemainingSubs = new LinkedList<Integer>();
            // accept the sub-intervals that meet convergence criteria
            for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
                DataClass datumTmp = BuiltinProcedures.evaluateSubstraction(darraySubIntervals[idx][1], darraySubIntervals[idx][0]);
                datumTmp = BuiltinProcedures.evaluateDivision(datumTmp, datumH);
                datumTmp = new DataClass(DATATYPES.DATUM_DOUBLE, mfpTolerance.multiply(datumTmp.getComplexRadAngle()[0]));
                if (darrayResult[1][idx].getDataValue().compareTo(datumTmp.getDataValue()) < 0) {
                    datumQ = BuiltinProcedures.evaluateAdding(datumQ, darrayResult[0][idx]);
                    datumErr = BuiltinProcedures.evaluateAdding(datumErr, darrayResult[1][idx]);
                } else {
                    listRemainingSubs.add(idx);
                }
                datumQ0 = BuiltinProcedures.evaluateAdding(datumQ0, darrayResult[0][idx]);
                datumErr0 = BuiltinProcedures.evaluateAdding(datumErr0, darrayResult[1][idx]);
            }

            // if no remaining sub-intervals, exit
            if (listRemainingSubs.size() == 0) {
                break;
            }
            
            // If the maximum sub-interval count is met accept remaining sub-interval and exit
            if (nTotalCalcSubIntervals > nMaxSteps) {
                datumQ = datumQ0;
                datumErr = datumErr0;
                bNotEnoughStepsExit = true;
                break;
            }
            nTotalCalcSubIntervals += listRemainingSubs.size() * 2;
            
            // If error increase more than max error increase number, exit
            if (datumErr0Last.getDataValue().compareTo(datumErr0.getDataValue()) < 0) {
                nNumOfErrIncreases ++;
                if (nNumOfErrIncreases > nMaxNumOfErrIncreases) {
                    datumQ = datumQ0;
                    datumErr = datumErr0;
                    bNotConvergeExit = true;
                    break;
                }
            }
            
            // now split the remaining sub-intervals into two
            DataClass[][] darrayNewSubIntervals = new DataClass[listRemainingSubs.size()*2][2];
            int idx1 = 0;
            for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
                if (listRemainingSubs.getFirst() == idx) {
                    listRemainingSubs.removeFirst();
                    darrayNewSubIntervals[idx1][0] = darraySubIntervals[idx][0];
                    darrayNewSubIntervals[idx1][1] = BuiltinProcedures.evaluateAdding(darraySubIntervals[idx][0], darraySubIntervals[idx][1]);
                    darrayNewSubIntervals[idx1][1] = BuiltinProcedures.evaluateDivision(darrayNewSubIntervals[idx1][1], datumTwo);
                    idx1 ++;
                    darrayNewSubIntervals[idx1][0] = darrayNewSubIntervals[idx1 - 1][1];
                    darrayNewSubIntervals[idx1][1] = darraySubIntervals[idx][1];
                    idx1 ++;
                }
                if (listRemainingSubs.size() == 0)  {
                    break;
                }
            }
            darraySubIntervals = darrayNewSubIntervals;
            
            // Evaluation of the integrand on the remaining sub-intervals
            darrayResult = integByGaussKronrodCore(aeStrExpr/*strIntegExpr*/, strVariable, datumFrom, datumTo, nIntegMode,
                                                    darraySubIntervals, msdarrayAbsCissa, msdarrayWeights15, msdarrayWeights7,
                                                    lVarNameSpaces, functionInterrupter);
        }
        
        MFPNumeric mfpQAbs = datumQ.getComplexRadAngle()[0];
        MFPNumeric mfpTolerance = mfpQAbs.multiply(new MFPNumeric(reltol));
        if (mfpTolerance.compareTo(new MFPNumeric(abstol)) < 0) {
            mfpTolerance  = new MFPNumeric(abstol);
        }

        if (bCheckConverge && bNotConvergeExit) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALCULATION_CANNOT_CONVERGE); // not converge like 1/x from -inf to inf
        }
        if (bExceptNotEnoughSteps && bNotEnoughStepsExit) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALCULATION_CANNOT_CONVERGE); // the calculation steps are not enough to see a converged result.
        }
        if (bCheckFinalResult && (mfpQAbs.isNan() || datumErr.getDataValue().isNan()  //we can still get Nan when cvtvar is very close to 1.
                    || datumErr.getDataValue().compareTo(mfpTolerance) > 0)) {
            // Error tolerance not met. Estimated error
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CALCULATION_CANNOT_CONVERGE);
        }
        DataClass[] darrayQAndErr = new DataClass[2];
        if (bNeed2MultiplyMinus1) {
            DataClass datumMinus1 = new DataClass();
            datumMinus1.setDataValue(MFPNumeric.MINUS_ONE, DATATYPES.DATUM_INTEGER);
            darrayQAndErr[0] = BuiltinProcedures.evaluateMultiplication(datumMinus1, datumQ);
        } else {
            darrayQAndErr[0] = datumQ;
        }
        darrayQAndErr[1] = datumErr;
        return darrayQAndErr;
    }

    protected static DataClass[] initGaussKronrodMatrices(int nReturnMode) {
         /* nReturnMode is 0 for darrayAbsCissa[], 1 for darrayWeights7[], 2 for darrayWeights15[] */
        try {
        switch (nReturnMode) {
            case (1): {
                DataClass[] darrayWeights7 = new DataClass[7];
                darrayWeights7[0] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1294849661688697"));
                darrayWeights7[1] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2797053914892767"));
                darrayWeights7[2] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.3818300505051889"));
                darrayWeights7[3] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.4179591836734694"));
                darrayWeights7[4] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.3818300505051889"));
                darrayWeights7[5] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2797053914892767"));
                darrayWeights7[6] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1294849661688697"));
                return darrayWeights7;
            } case (2): {
                DataClass[] darrayWeights15 = new DataClass[15];
                darrayWeights15[0] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2293532201052922e-01"));
                darrayWeights15[1] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.6309209262997855e-01"));
                darrayWeights15[2] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1047900103222502"));
                darrayWeights15[3] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1406532597155259"));
                darrayWeights15[4] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1690047266392679"));
                darrayWeights15[5] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1903505780647854"));
                darrayWeights15[6] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2044329400752989"));
                darrayWeights15[7] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2094821410847278"));
                darrayWeights15[8] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2044329400752989"));
                darrayWeights15[9] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1903505780647854"));
                darrayWeights15[10] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1690047266392679"));
                darrayWeights15[11] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1406532597155259"));
                darrayWeights15[12] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.1047900103222502"));
                darrayWeights15[13] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.6309209262997855e-01"));
                darrayWeights15[14] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2293532201052922e-01"));
                return darrayWeights15;
            } default: {
                DataClass[] darrayAbsCissa = new DataClass[15];
                darrayAbsCissa[0] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.9914553711208126"));
                darrayAbsCissa[1] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.9491079123427585"));
                darrayAbsCissa[2] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.8648644233597691"));
                darrayAbsCissa[3] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.7415311855993944"));
                darrayAbsCissa[4] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.5860872354676911"));
                darrayAbsCissa[5] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.4058451513773972"));
                darrayAbsCissa[6] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("-0.2077849550078985"));
                darrayAbsCissa[7] = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
                darrayAbsCissa[8] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.2077849550078985"));
                darrayAbsCissa[9] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.4058451513773972"));
                darrayAbsCissa[10] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.5860872354676911"));
                darrayAbsCissa[11] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.7415311855993944"));
                darrayAbsCissa[12] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.8648644233597691"));
                darrayAbsCissa[13] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.9491079123427585"));
                darrayAbsCissa[14] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric("0.9914553711208126"));
                return darrayAbsCissa;
            }
        }
        } catch (JFCALCExpErrException e) {
            throw new Error(e); //shouldn't arrive here.
        }
    }
    
    protected static void initGaussKronrodSubRanges(double dFrom, double dTo, DataClass[] darraySubs) throws JFCALCExpErrException   {
        double dAllRange = dTo - dFrom;
        darraySubs[0] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom));
        darraySubs[1] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.1));
        darraySubs[2] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.2));
        darraySubs[3] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.3));
        darraySubs[4] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.4));
        darraySubs[5] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.5));
        darraySubs[6] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.6));
        darraySubs[7] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.7));
        darraySubs[8] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.8));
        darraySubs[9] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dFrom + dAllRange * 0.9));
        darraySubs[10] = new DataClass(DATATYPES.DATUM_DOUBLE, new MFPNumeric(dTo));
    }
    
    protected static DataClass[][] integByGaussKronrodCore(AbstractExpr aeStrExpr/*String strIntegExpr*/,
                                                        String strVariable,
                                                        DataClass datumFrom,
                                                        DataClass datumTo,
                                                        int nIntegMode,
                                                        DataClass[][] darraySubIntervals,
                                                        DataClass[] darrayAbsCissa,
                                                        DataClass[] darrayWeights15,
                                                        DataClass[] darrayWeights7,
                                                        LinkedList<LinkedList<Variable>> lVarNameSpaces,
                                                        FunctionInterrupter functionInterrupter
                                                        ) throws JFCALCExpErrException, InterruptedException {
        DataClass[] darrayHalfWidth = new DataClass[darraySubIntervals.length];
        DataClass[] darrayCenter = new DataClass[darraySubIntervals.length];
        DataClass datumTwo = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.TWO);
        for (int idx = 0; idx < darraySubIntervals.length; idx ++) {
            if (functionInterrupter != null)    {
                // for debug or killing a background thread. Every heavy functions need to have this check.
                if (functionInterrupter.shouldInterrupt())    {
                    functionInterrupter.interrupt();
                }
            }
            darrayHalfWidth[idx] = BuiltinProcedures.evaluateSubstraction(darraySubIntervals[idx][1], darraySubIntervals[idx][0]);
            darrayHalfWidth[idx] = BuiltinProcedures.evaluateDivision(darrayHalfWidth[idx], datumTwo);
            darrayCenter[idx] = BuiltinProcedures.evaluateAdding(darraySubIntervals[idx][1], darraySubIntervals[idx][0]);
            darrayCenter[idx] = BuiltinProcedures.evaluateDivision(darrayCenter[idx], datumTwo);
        }
        
        DataClass[][] datumX = new DataClass[darrayHalfWidth.length][darrayAbsCissa.length];
        DataClass[][] datumY = new DataClass[darrayHalfWidth.length][darrayAbsCissa.length];
        Variable varVar = new Variable(strVariable);
		LinkedList<Variable> l = new LinkedList<Variable>();
        l.addFirst(varVar);
        lVarNameSpaces.addFirst(l);
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        for (int idx0 = 0; idx0 < darrayHalfWidth.length; idx0 ++) {
            for (int idx1 = 0; idx1 < darrayAbsCissa.length; idx1 ++)   {
                if (functionInterrupter != null)    {
                    // for debug or killing a background thread. Every heavy functions need to have this check.
                    if (functionInterrupter.shouldInterrupt())    {
                        functionInterrupter.interrupt();
                    }
                }
                datumX[idx0][idx1] = BuiltinProcedures.evaluateMultiplication(darrayHalfWidth[idx0], darrayAbsCissa[idx1]);
                datumX[idx0][idx1] = BuiltinProcedures.evaluateAdding(datumX[idx0][idx1], darrayCenter[idx0]);
                DataClass datumTrans = integGKTransF(datumX[idx0][idx1], datumFrom, datumTo, nIntegMode);
                varVar.setValue(datumTrans);
                DataClass datumInteg = null;
                if (aeStrExpr != null) {
                    try {
                        datumInteg = aeStrExpr.evaluateAExprQuick(new LinkedList<UnknownVariable>(), lVarNameSpaces);    // do not use lUnknown coz lVarNameSpaces has included var.
                    } catch (Exception ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);
                    }
                }
                //DataClass datumInteg = exprEvaluator.evaluateExpression(strIntegExpr, new CurPos());
                datumY[idx0][idx1] = integGKExprF(datumX[idx0][idx1], datumInteg, datumFrom, datumTo, nIntegMode);
            }
        }
        lVarNameSpaces.pollFirst();
        
        DataClass[] darrayQs = new DataClass[darrayHalfWidth.length];
        for (int idx0 = 0; idx0 < darrayHalfWidth.length; idx0 ++) {
            DataClass datumQElem = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
            for (int idx1 = 0; idx1 < darrayAbsCissa.length; idx1 ++)   {
                if (functionInterrupter != null)    {
                    // for debug or killing a background thread. Every heavy functions need to have this check.
                    if (functionInterrupter.shouldInterrupt())    {
                        functionInterrupter.interrupt();
                    }
                }
                DataClass datumTmp = BuiltinProcedures.evaluateMultiplication(datumY[idx0][idx1], darrayWeights15[idx1]);
                datumQElem = BuiltinProcedures.evaluateAdding(datumQElem, datumTmp);
            }
            darrayQs[idx0] = BuiltinProcedures.evaluateMultiplication(datumQElem, darrayHalfWidth[idx0]);
        }
        DataClass[] darrayErrs = new DataClass[darrayHalfWidth.length];
        for (int idx0 = 0; idx0 < darrayHalfWidth.length; idx0 ++) {
            DataClass datumErrElem = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
            for (int idx1 = 0; idx1 < darrayWeights7.length; idx1 ++)   {
                if (functionInterrupter != null)    {
                    // for debug or killing a background thread. Every heavy functions need to have this check.
                    if (functionInterrupter.shouldInterrupt())    {
                        functionInterrupter.interrupt();
                    }
                }
                DataClass datumTmp = BuiltinProcedures.evaluateMultiplication(datumY[idx0][idx1*2+1], darrayWeights7[idx1]);
                datumErrElem = BuiltinProcedures.evaluateAdding(datumErrElem, datumTmp);
            }
            datumErrElem = BuiltinProcedures.evaluateMultiplication(datumErrElem, darrayHalfWidth[idx0]);
            datumErrElem = BuiltinProcedures.evaluateSubstraction(datumErrElem, darrayQs[idx0]);
            darrayErrs[idx0] = new DataClass(DATATYPES.DATUM_DOUBLE, datumErrElem.getComplexRadAngle()[0]);
        }
        DataClass[][] darrayReturn = new DataClass[2][];
        darrayReturn[0] = darrayQs;
        darrayReturn[1] = darrayErrs;
        return darrayReturn;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFInf2Inf(DataClass datumCvtVar, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then i * cvtvar / (1 - cvtvar**2)
        // else cvtvar / (1 - cvtvar**2), here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
        MFPNumeric mfpReturn = mfpNumCvtVar.divide(MFPNumeric.ONE.subtract(mfpNumCvtVar.multiply(mfpNumCvtVar)));
        DataClass datum = new DataClass();
        if (bImage) {
            datum.setComplex(MFPNumeric.ZERO, mfpReturn);
        } else {
            datum.setDataValue(mfpReturn);
        }
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFInf2Inf(DataClass datumCvtVar, DataClass datumInteg, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then i * integ * (1 + cvtvar ** 2)/((1 - cvtvar ** 2)**2)
        // else integ * (1 + cvtvar ** 2)/((1 - cvtvar ** 2)**2), here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
        MFPNumeric mfpNumCvtVarSqr = mfpNumCvtVar.multiply(mfpNumCvtVar);
        MFPNumeric mfpNum1MinusSqr = MFPNumeric.ONE.subtract(mfpNumCvtVarSqr);
        MFPNumeric mfpNumExceptInteg = MFPNumeric.ONE.add(mfpNumCvtVarSqr).divide(mfpNum1MinusSqr.multiply(mfpNum1MinusSqr));
        
        MFPNumeric mfpNumReal = datumInteg.getReal();
        mfpNumReal = mfpNumReal.multiply(mfpNumExceptInteg);
        MFPNumeric mfpNumImage = datumInteg.getImage();
        mfpNumImage = mfpNumImage.multiply(mfpNumExceptInteg);
        
        DataClass datum = new DataClass();
        if (bImage) {
            datum.setComplex(mfpNumImage.negate(), mfpNumReal);
        } else {
            datum.setComplex(mfpNumReal, mfpNumImage);
        }
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFFromInf(DataClass datumCvtVar, DataClass datumTo, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then to - i*(cvtvar/(1+cvtvar))**2
        // else to - (cvtvar/(1+cvtvar))**2, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
        MFPNumeric mfpNumCvtVarOver1PlusCvtVar = mfpNumCvtVar.divide(MFPNumeric.ONE.add(mfpNumCvtVar));
        MFPNumeric mfpNumExceptI = mfpNumCvtVarOver1PlusCvtVar.multiply(mfpNumCvtVarOver1PlusCvtVar);
        
        MFPNumeric mfpNumReal = datumTo.getReal();
        MFPNumeric mfpNumImage = datumTo.getImage();
        
        DataClass datum = new DataClass();
        if (bImage) {
            mfpNumImage = mfpNumImage.subtract(mfpNumExceptI);
        } else {
            mfpNumReal = mfpNumReal.subtract(mfpNumExceptI);
        }
        datum.setComplex(mfpNumReal, mfpNumImage);
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFFromInf(DataClass datumCvtVar, DataClass datumInteg, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then -2i * cvtvar * integ / (1 + cvtvar)**3 ----------> i * integ * (-2) * cvtvar/ (1 + cvtvar)**3
        // else -2 * cvtvar * integ / (1 + cvtvar)**3 ------------> integ * (-2) * cvtvar/ (1 + cvtvar)**3, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
        MFPNumeric mfpNum1PlusCvtVar = MFPNumeric.ONE.add(mfpNumCvtVar);
        MFPNumeric mfpNumExceptInteg = mfpNumCvtVar.divide(mfpNum1PlusCvtVar.multiply(mfpNum1PlusCvtVar).multiply(mfpNum1PlusCvtVar)).multiply(MFPNumeric.TWO.negate());
        
        MFPNumeric mfpNumReal = datumInteg.getReal();
        mfpNumReal = mfpNumReal.multiply(mfpNumExceptInteg);
        MFPNumeric mfpNumImage = datumInteg.getImage();
        mfpNumImage = mfpNumImage.multiply(mfpNumExceptInteg);
        
        DataClass datum = new DataClass();
        if (bImage) {
            datum.setComplex(mfpNumImage.negate(), mfpNumReal);
        } else {
            datum.setComplex(mfpNumReal, mfpNumImage);
        }
        return datum;
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFToInf(DataClass datumCvtVar, DataClass datumFrom, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then from + i*(cvtvar/(1-cvtvar))**2
        // else from + (cvtvar/(1-cvtvar))**2, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
        MFPNumeric mfpNumCvtVarOver1MinusCvtVar = mfpNumCvtVar.divide(MFPNumeric.ONE.subtract(mfpNumCvtVar));
        MFPNumeric mfpNumExceptI = mfpNumCvtVarOver1MinusCvtVar.multiply(mfpNumCvtVarOver1MinusCvtVar);
        
        MFPNumeric mfpNumReal = datumFrom.getReal();
        MFPNumeric mfpNumImage = datumFrom.getImage();
        
        DataClass datum = new DataClass();
        if (bImage) {
            mfpNumImage = mfpNumImage.add(mfpNumExceptI);
        } else {
            mfpNumReal = mfpNumReal.add(mfpNumExceptI);
        }
        datum.setComplex(mfpNumReal, mfpNumImage);
        return datum;       
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFToInf(DataClass datumCvtVar, DataClass datumInteg, boolean bImage) throws JFCALCExpErrException {
        // if bImage is true, then 2i * cvtvar * integ / (1 - cvtvar)**3 ----------> i * integ * 2 * cvtvar/ (1 - cvtvar)**3
        // else 2 * cvtvar * integ / (1 - cvtvar)**3 ------------> integ * 2 * cvtvar/ (1 - cvtvar)**3, here cvtvar must be a real value.
        MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
        MFPNumeric mfpNum1MinusCvtVar = MFPNumeric.ONE.subtract(mfpNumCvtVar);
        MFPNumeric mfpNumExceptInteg = mfpNumCvtVar.divide(mfpNum1MinusCvtVar.multiply(mfpNum1MinusCvtVar).multiply(mfpNum1MinusCvtVar)).multiply(MFPNumeric.TWO);
        
        MFPNumeric mfpNumReal = datumInteg.getReal();
        mfpNumReal = mfpNumReal.multiply(mfpNumExceptInteg);
        MFPNumeric mfpNumImage = datumInteg.getImage();
        mfpNumImage = mfpNumImage.multiply(mfpNumExceptInteg);
        
        DataClass datum = new DataClass();
        if (bImage) {
            datum.setComplex(mfpNumImage.negate(), mfpNumReal);
        } else {
            datum.setComplex(mfpNumReal, mfpNumImage);
        }
        return datum;        
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKTransFGeneral(DataClass datumCvtVar, DataClass datumFrom, DataClass datumTo, boolean bImage) throws JFCALCExpErrException {
        if (bImage) {
            return datumCvtVar.cloneSelf();
        } else {
            // transf = ((to - from)/4)*cvtvar*(3-cvtvar**2)+(to+from)/2 where cvtvar, to and from are reals
            MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
            MFPNumeric mfpNumThree = new MFPNumeric(3);
            MFPNumeric mfpNumExceptI = mfpNumCvtVar.multiply(mfpNumThree.subtract(mfpNumCvtVar.multiply(mfpNumCvtVar)));
            
            MFPNumeric mfpNumToValue = datumTo.getReal();
            MFPNumeric mfpNumFromValue = datumFrom.getReal();
            
            MFPNumeric mfpNumFour = new MFPNumeric(4);
            
            MFPNumeric mfpNumValue = mfpNumToValue.subtract(mfpNumFromValue).divide(mfpNumFour).multiply(mfpNumExceptI);
            mfpNumValue = mfpNumValue.add(mfpNumToValue.add(mfpNumFromValue).divide(MFPNumeric.TWO));
            /* to and from are both real. */
            
            DataClass datum = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumValue);
            return datum;
        }
    }
    
    // this function will not change its parameters and its returned value does not refer to any part of its parameters
    protected static DataClass integGKExprFGeneral(DataClass datumCvtVar, DataClass datumInteg, DataClass datumFrom, DataClass datumTo, boolean bImage) throws JFCALCExpErrException {
        if (bImage) {
            return datumInteg.cloneSelf();
        } else {
            // integ * 3*(to - from)/4*(1-cvtvar**2) where cvtvar, to and from are reals --------> integ * (to - from) * 0.75 * (1-cvtvar**2)
            MFPNumeric mfpNumCvtVar = datumCvtVar.getReal();
            MFPNumeric mfpNum0Pnt75 = new MFPNumeric(0.75);
            MFPNumeric mfpNumExceptI = mfpNum0Pnt75.multiply(MFPNumeric.ONE.subtract(mfpNumCvtVar.multiply(mfpNumCvtVar)));
            
            MFPNumeric mfpNumToMinusFrom = datumTo.getReal().subtract(datumFrom.getReal());
            mfpNumExceptI = mfpNumExceptI.multiply(mfpNumToMinusFrom);
            MFPNumeric mfpNumReal = datumInteg.getReal().multiply(mfpNumExceptI);
            MFPNumeric mfpNumImage = datumInteg.getImage().multiply(mfpNumExceptI);
            
            DataClass datum = new DataClass();
            datum.setComplex(mfpNumReal, mfpNumImage);
            return datum;
        }
    }
    
    protected static DataClass integGKTransF(DataClass datumCvtVar, DataClass datumFrom, DataClass datumTo, int nIntegMode) throws JFCALCExpErrException {
        switch(nIntegMode) {
            case 1:
                return integGKTransFInf2Inf(datumCvtVar, false);
            case -1:
                return integGKTransFInf2Inf(datumCvtVar, true);
            case 2:
                return integGKTransFFromInf(datumCvtVar, datumTo, false);
            case -2:
                return integGKTransFFromInf(datumCvtVar, datumTo, true);
            case 3:
                return integGKTransFToInf(datumCvtVar, datumFrom, false);
            case -3:
                return integGKTransFToInf(datumCvtVar, datumFrom, true);
            case 4:
                return integGKTransFGeneral(datumCvtVar, datumFrom, datumTo, false);
            default:    // -4
                return integGKTransFGeneral(datumCvtVar, datumFrom, datumTo, true);
        }
    }
    
    protected static DataClass integGKExprF(DataClass datumCvtVar, DataClass datumInteg, DataClass datumFrom, DataClass datumTo, int nIntegMode) throws JFCALCExpErrException {
        switch(nIntegMode) {
            case 1:
                return integGKExprFInf2Inf(datumCvtVar, datumInteg, false);
            case -1:
                return integGKExprFInf2Inf(datumCvtVar, datumInteg, true);
            case 2:
                return integGKExprFFromInf(datumCvtVar, datumInteg, false);
            case -2:
                return integGKExprFFromInf(datumCvtVar, datumInteg, true);
            case 3:
                return integGKExprFToInf(datumCvtVar, datumInteg, false);
            case -3:
                return integGKExprFToInf(datumCvtVar, datumInteg, true);
            case 4:
                return integGKExprFGeneral(datumCvtVar, datumInteg, datumFrom, datumTo, false);
            default:    // -4
                return integGKExprFGeneral(datumCvtVar, datumInteg, datumFrom, datumTo, true);
        }
    }    
}
