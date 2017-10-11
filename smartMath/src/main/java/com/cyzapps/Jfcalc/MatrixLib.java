/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator.FunctionInterrupter;
import java.util.LinkedList;

/**
 *
 * @author tonyc
 */
public class MatrixLib {
    public static LinkedList<DataClass> calculateEigenValues(DataClass datum2DSqrA, DataClass datum2DSqrB, FunctionInterrupter functionInterrupter)  throws JFCALCExpErrException, InterruptedException {
		// assume that datum2DSquare is always a 2D square matrix and it has been fully populated.
		// Moreover, the parameter datum2DSqrA and datum2DSqrB will be not be modified inside the function.
		// nor the return value will refer to any part of the parameter.

		// do not deep copy input because unecessary (we do not generate another matrix).
		int[] narraySquareSize = datum2DSqrA.recalcDataArraySize();
		if (narraySquareSize.length != 2
				|| narraySquareSize[0] != narraySquareSize[1]
				|| narraySquareSize[0] == 0)	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
		}
		// assume Matrix has been fully populated
		//datum2DSquare.PopulateDataArray(narraySquareSize, true);
        
        LinkedList<Long> listReversedPairCnts = new LinkedList<Long>();
        int[][] narrayPrLists = MathLib.getPermutations(narraySquareSize[0], listReversedPairCnts, functionInterrupter);
        DataClass[] datumArrayAddSub = new DataClass[1];
        datumArrayAddSub[0] = new DataClass();
        datumArrayAddSub[0].setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
        for (int idx = 0; idx < narrayPrLists.length; idx ++)    {
            DataClass[] datumArrayMultip = new DataClass[1];
            datumArrayMultip[0] = new DataClass();
            datumArrayMultip[0].setDataValue(MFPNumeric.ONE, DATATYPES.DATUM_INTEGER);
            for (int index0 = 0; index0 < narrayPrLists[idx].length; index0 ++) {
                int index1 = narrayPrLists[idx][index0];
                DataClass datumArrayThis[] = new DataClass[2];
                datumArrayThis[0] = datum2DSqrA.getDataList()[index0].getDataList()[index1];
                datumArrayThis[1] = BuiltinProcedures.evaluateNegSign(datum2DSqrB.getDataList()[index0].getDataList()[index1]);
                datumArrayMultip = MathLib.multiplyPolynomial(datumArrayMultip, datumArrayThis, functionInterrupter);
            }
            datumArrayAddSub = MathLib.addSubPolynomial(datumArrayAddSub, datumArrayMultip, (listReversedPairCnts.get(idx) % 2 == 0), functionInterrupter);
        }
        
        // calculate roots of polynomial to get the eigen values.
        LinkedList<DataClass> listParams = new LinkedList<DataClass>();
        for (int idx = 0; idx < datumArrayAddSub.length; idx ++)    {
            listParams.addFirst(datumArrayAddSub[idx]);
        }
        return MathLib.solvePolynomial(listParams, functionInterrupter);        
    }

    public static LinkedList<DataClass> calculateZeroVector(DataClass datum2DSquare)   throws JFCALCExpErrException   {
        // this function calculate a vector v which satisfy input A * v = 0 and v is not all zero vector.
        // note that function will not change input datum2DSquare.
        
		// do not deep copy input because unecessary (we do not generate another matrix).
		int[] narraySquareSize = datum2DSquare.recalcDataArraySize();
		if (narraySquareSize.length != 2
				|| narraySquareSize[0] != narraySquareSize[1]
				|| narraySquareSize[0] <= 1)	{   // if narraySquareSize[0] == 1, sub 2d matrix will be empty which cannot be handled.
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
		}
		// assume Matrix has been fully populated
		//datum2DSquare.PopulateDataArray(narraySquareSize, true);
        
        // step 1, find out the child matrix whose abs(det) is smallest. If it's zero, then look for the next one.
        DataClass datumZero = new DataClass();
        datumZero.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
        
        DataClass[] datumArrayAddSub = new DataClass[1];
        datumArrayAddSub[0] = new DataClass();
        datumArrayAddSub[0].setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);

        DataClass[] datumVector = new DataClass[narraySquareSize[0]];
        int nMinAbsDetIdx = -1;
        DataClass datumMinAbsDet = null;
        for (int index = 0; index < narraySquareSize[0]; index ++)    {
            // now reconstruct a sub matrix and calculate abs(det(sub matrix))
            DataClass datumSub2DMatrix = new DataClass();
            DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx0 = 0; idx0 < datumList.length; idx0++) {
                datumList[idx0] = new DataClass();
                DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                for (int idx1 = 0; idx1 < datumListChildren.length; idx1 ++)    {
                    if (idx0 < index && idx1 < index)   {
                        datumListChildren[idx1] = datum2DSquare.getDataList()[idx0].getDataList()[idx1];
                    } else if (idx0 >= index && idx1 < index)   {
                        datumListChildren[idx1] = datum2DSquare.getDataList()[idx0 + 1].getDataList()[idx1];
                    } else if (idx0 < index && idx1 >= index)   {
                        datumListChildren[idx1] = datum2DSquare.getDataList()[idx0].getDataList()[idx1 + 1];
                    } else {
                        datumListChildren[idx1] = datum2DSquare.getDataList()[idx0 + 1].getDataList()[idx1 + 1];
                    }
                }
                datumList[idx0].setDataList(datumListChildren);
            }
            datumSub2DMatrix.setDataList(datumList);
            DataClass datumAbsDet = BuiltinProcedures.evaluateDeterminant(datumSub2DMatrix);
            MFPNumeric[] mfpNumRadAngle = datumAbsDet.getComplexRadAngle();
			datumAbsDet.setDataValue(mfpNumRadAngle[0]);
            if (datumAbsDet.isEqual(datumZero)) {
                continue;   // abs(det) of child matrix is 0, we go to next one.
            } else if (datumMinAbsDet == null || MFPNumeric.compareTo(datumAbsDet.getDataValue(), datumMinAbsDet.getDataValue()) < 0) {
                nMinAbsDetIdx = index;
                datumMinAbsDet = datumAbsDet;
            }
        }
        
        if (nMinAbsDetIdx == -1)    {
            // all the Abs dets are zeros
            LinkedList<DataClass> listReturn = new LinkedList<DataClass>();
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                DataClass datumZeroElem = new DataClass();
                datumZeroElem.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                listReturn.add(datumZeroElem);
            }
            return listReturn;
        } else  {
            // now reconstruct a sub child matrix and calculate the sub zero vector
            DataClass datumSubMatrix = new DataClass();
            DataClass[] datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx0 = 0; idx0 < narraySquareSize[0]; idx0 ++)   {
                int idxNew0 = idx0;
                if (idx0 == nMinAbsDetIdx)  {
                    continue;
                } else if (idx0 > nMinAbsDetIdx)    {
                    idxNew0 = idx0 - 1;
                }
                DataClass[] datumListOrigChildren = datum2DSquare.getDataList()[idx0].getDataList();
                DataClass[] datumListChildren = new DataClass[narraySquareSize[1] - 1];
                for (int idx1 = 0; idx1 < narraySquareSize[1]; idx1 ++) {
                    int idxNew1 = idx1;
                    if (idx1 == nMinAbsDetIdx)  {
                        continue;
                    } else if (idx1 > nMinAbsDetIdx)    {
                        idxNew1 = idx1 - 1;
                    }
                    datumListChildren[idxNew1] = datumListOrigChildren[idx1];
                }
                datumList[idxNew0] = new DataClass();
                datumList[idxNew0].setDataList(datumListChildren);
            }
            datumSubMatrix.setDataList(datumList);
            // now sub matrix is ready, calculate its inverted matrix
            DataClass datumSubMatrixInv = BuiltinProcedures.evaluateReciprocal(datumSubMatrix);
            DataClass datum2MultiplyVector = new DataClass();
            datumList = new DataClass[narraySquareSize[0] - 1];
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                int idxNew = idx;
                if (idx == nMinAbsDetIdx) {
                    continue;
                } else if (idxNew > nMinAbsDetIdx)  {
                    idxNew --;
                }
                datumList[idxNew] = new DataClass();
                DataClass[] datumListChildren = new DataClass[1];
                datumListChildren[0] = BuiltinProcedures.evaluateNegSign(datum2DSquare.getDataList()[idx].getDataList()[nMinAbsDetIdx]);
                datumList[idxNew].setDataList(datumListChildren);
            }
            datum2MultiplyVector.setDataList(datumList);
            DataClass datumSubZeroVector = BuiltinProcedures.evaluateMultiplication(datumSubMatrixInv, datum2MultiplyVector);
            LinkedList<DataClass> listReturn = new LinkedList<DataClass>();
            for (int idx = 0; idx < narraySquareSize[0]; idx ++)    {
                if (idx < nMinAbsDetIdx)    {
                    listReturn.add(datumSubZeroVector.getDataList()[idx].getDataList()[0]);
                } else if (idx == nMinAbsDetIdx)    {
                    DataClass datumOne = new DataClass();
                    datumOne.setDataValue(MFPNumeric.ONE, DATATYPES.DATUM_INTEGER);
                    listReturn.add(datumOne);
                } else {    // idx > nMinAbsDetIdx
                    listReturn.add(datumSubZeroVector.getDataList()[idx - 1].getDataList()[0]);
                }
            }
            return listReturn;
        }
    }
    
    // this function will not change the value of datumInput and will assume this array (datumOperand) has been fully populated.
    public static DataClass calculateUpperTriangularMatrix(DataClass datumInput)   throws JFCALCExpErrException   {
        int[] narrayLen = new int[0];
        if (datumInput.getDataType() != DATATYPES.DATUM_REF_DATA) {
            // it is not a matrix, return a copy of itself.
            return datumInput.cloneSelf();
        }
        
        narrayLen = datumInput.recalcDataArraySize();
        if (narrayLen.length != 2 || narrayLen[0] == 0 || narrayLen[1] == 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
                
        // now it is a 2D matrix.
        DataClass datumReturn = new DataClass();
        datumReturn.copyTypeValueDeep(datumInput);
        int nShorterSize = Math.min(narrayLen[0], narrayLen[1]);
        for (int idx1 = 0; idx1 < nShorterSize; idx1 ++) {
            MFPNumeric mfpMaxAbs = MFPNumeric.ZERO;
            int nMaxAbsIdx = 0;
            for (int idx0 = idx1; idx0 < narrayLen[0]; idx0 ++) {
                int[] narrayIdx = new int[2];
                narrayIdx[0] = idx0;
                narrayIdx[1] = idx1;
                DataClass datumThisElem = datumReturn.getDataAtIndex(narrayIdx); // deep copy here
                DataClass datumAbs = BuiltinProcedures.evaluateAbs(datumThisElem);
                MFPNumeric mfpAbs = datumAbs.getDataValue();
                if (mfpAbs.compareTo(mfpMaxAbs) > 0) {
                    mfpMaxAbs = mfpAbs;
                    nMaxAbsIdx = idx0;
                }
            }
            if (!mfpMaxAbs.isActuallyZero()) {
                for (int idx11 = idx1; idx11 < narrayLen[0]; idx11 ++) {
                    if (idx11 != nMaxAbsIdx) {
                        int[] narrayIdx = new int[2];
                        narrayIdx[0] = nMaxAbsIdx;
                        narrayIdx[1] = idx1;
                        DataClass datumMaxAbs = datumReturn.getDataAtIndexByRef(narrayIdx);
                        narrayIdx[0] = idx11;
                        DataClass datum2Divided = datumReturn.getDataAtIndexByRef(narrayIdx);
                        DataClass datumCoeff = BuiltinProcedures.evaluateDivision(datum2Divided, datumMaxAbs);
                        DataClass datumMinus = BuiltinProcedures.evaluateSubstraction(datumReturn.getDataList()[idx11],
                                                BuiltinProcedures.evaluateMultiplication(datumCoeff, datumReturn.getDataList()[nMaxAbsIdx]));
                        for (int idxTmp = 0; idxTmp <= idx1; idxTmp++) {
                            narrayIdx = new int[1];
                            narrayIdx[0] = idxTmp;
                            datumMinus.setDataAtIndexByRef(narrayIdx, new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO));
                        }
                        narrayIdx = new int[1];
                        narrayIdx[0] = idx11;
                        datumReturn.setDataAtIndexByRef(narrayIdx, datumMinus);
                    }
                }
                if (nMaxAbsIdx != idx1) {
                    // swap nMaxAbsIdx and idx1. We need not to worry about invalid index here.
                    int[] narrayIdx = new int[1];
                    narrayIdx[0] = nMaxAbsIdx;
                    DataClass datumMaxAbsIdx = datumReturn.getDataAtIndexByRef(narrayIdx);
                    narrayIdx[0] = idx1;
                    DataClass datumIdx1 = datumReturn.getDataAtIndexByRef(narrayIdx);
                    datumReturn.setDataAtIndexByRef(narrayIdx, datumMaxAbsIdx);
                    narrayIdx[0] = nMaxAbsIdx;
                    datumReturn.setDataAtIndexByRef(narrayIdx, datumIdx1);
                }
            }
        }
        return datumReturn;
    }
    
    public static DataClass calculateMatrixRank(DataClass datumInput)   throws JFCALCExpErrException   {
        int[] narrayLen = new int[0];
        if (datumInput.getDataType() != DATATYPES.DATUM_REF_DATA) {
            // it is not a matrix, return a copy of itself.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);
        }
        
        narrayLen = datumInput.recalcDataArraySize();
        if (narrayLen.length != 2) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
        }
        
        if (narrayLen[0] == 0 || narrayLen[1] == 0) {
            return new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
        }
           
        DataClass datumUpperTriangularMatrix;
        if (narrayLen[0] < narrayLen[1]) {
            // this is necessary otherwise, rank may be lower estimated.
            datumUpperTriangularMatrix = calculateUpperTriangularMatrix(BuiltinProcedures.evaluateTransposition(datumInput));
        } else {
            datumUpperTriangularMatrix = calculateUpperTriangularMatrix(datumInput);
        }
        int nShorterSize = Math.min(narrayLen[0], narrayLen[1]);
        int nRank = nShorterSize;
        for (int idx0 = 0; idx0 < nShorterSize; idx0 ++) {
            int[] narrayIdx = new int[2];
            narrayIdx[0] = narrayIdx[1] = idx0;
            DataClass datumDiagonal = datumUpperTriangularMatrix.getDataAtIndexByRef(narrayIdx);
            MFPNumeric mfpAbs = BuiltinProcedures.evaluateAbs(datumDiagonal).getDataValue();
            if (mfpAbs.isActuallyZero()) {
                nRank --;
            }
        }
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nRank));
    }
}
