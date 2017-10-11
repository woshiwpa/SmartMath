package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jsma.AEInvalid;

public class BuiltinProcedures {
	// this class includes many built-in mathematic procedures
		
    public static DataClass evaluateNegSign(DataClass datumOperand) throws JFCALCExpErrException    {
        // note that if operand is an array, it should NOT be fully populated.
        // inside this function datumOperand should not be changed.
        DataClass datumReturn = new DataClass();
        switch (datumOperand.getDataType()) {
        case DATUM_BOOLEAN: {
            try {
                MFPNumeric mfpReturn = datumOperand.getDataValue().toBoolMFPNum().negate();
                datumReturn.setDataValue(mfpReturn, DATATYPES.DATUM_INTEGER);
            } catch (ArithmeticException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
            }
            break;
        } case DATUM_INTEGER: {
            MFPNumeric mfpReturn = datumOperand.getDataValue().toIntOrNanInfMFPNum().negate();
            datumReturn.setDataValue(mfpReturn, DATATYPES.DATUM_INTEGER);
            break;
        } case DATUM_DOUBLE: {
            MFPNumeric mfpReturn = datumOperand.getDataValue().toDblOrNanInfMFPNum().negate();
            datumReturn.setDataValue(mfpReturn, DATATYPES.DATUM_DOUBLE);
            break;
        } case DATUM_COMPLEX:
			DataClass datumReal = datumOperand.getRealDataClass();  // not use deep copy here coz GetRealDataClass generates a new class 
            datumReal = evaluateNegSign(datumReal);
			DataClass datumImage = datumOperand.getImageDataClass();    // not use deep copy here coz GetImageDataClass generates a new class 
            datumImage = evaluateNegSign(datumImage);
			if (datumImage.getDataValue().isActuallyZero())	{
				datumReturn = datumReal;
			} else	{
				datumReturn.setComplex(datumReal, datumImage);
			}
			break;
        case DATUM_REF_DATA:
            datumReturn = new DataClass();
            // data_ref type
            if (datumOperand.getDataList() == null) {
                // empty array.
                datumReturn.setDataList(new DataClass[0]);
            } else  {
                DataClass[] dataListOprd = datumOperand.getDataList();
                DataClass[] dataListRet = new DataClass[dataListOprd.length];
                for (int idx = 0; idx < dataListOprd.length; idx ++)  {
                    if (dataListOprd[idx] == null)    {
                        dataListRet[idx] = new DataClass();  // if we find any null, we convert it first
                    }
                    DATATYPES dataTypeChild = dataListOprd[idx].getDataType();
                    if (dataTypeChild != DATATYPES.DATUM_NULL && dataTypeChild != DATATYPES.DATUM_BOOLEAN && dataTypeChild != DATATYPES.DATUM_INTEGER
                            && dataTypeChild != DATATYPES.DATUM_DOUBLE && dataTypeChild != DATATYPES.DATUM_COMPLEX
                            && dataTypeChild != DATATYPES.DATUM_REF_DATA)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
                    } else if (dataTypeChild == DATATYPES.DATUM_NULL)   {
                        // data_null is treated as a numerical (0) in data_reference (matrix)
                        dataListRet[idx] = new DataClass();
                    } else  {
                        dataListRet[idx] = evaluateNegSign(dataListOprd[idx]);
                    }
                }
                datumReturn.setDataList(dataListRet);
            }
            break;
        default:
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
        }
        return datumReturn;
    }
    
	public static DataClass evaluateAdding(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException	{
		// note that if operand(s) are array, they should have been fully populated.
		// inside this function datumFirstOperand and datumSecondOperand should not be changed
		DataClass datumReturn = new DataClass();
		if (datumFirstOperand.getDataType() == DATATYPES.DATUM_STRING
						|| datumSecondOperand.getDataType() == DATATYPES.DATUM_STRING)	{
			// any one of the parameter is string, so output a string.
			String str1st = new String(), str2nd = new String();
			if (datumFirstOperand.getDataType() == DATATYPES.DATUM_STRING)	{
				str1st = datumFirstOperand.getStringValue();
			} else	{
				str1st = datumFirstOperand.toString();
			}
			if (datumSecondOperand.getDataType() == DATATYPES.DATUM_STRING)	{
				str2nd = datumSecondOperand.getStringValue();
			} else	{
				str2nd = datumSecondOperand.toString();
			}
			
			datumReturn.setStringValue(str1st + str2nd);	
		} else if ((datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				&& datumFirstOperand.isZeros(false))
				|| (datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				&& datumSecondOperand.isZeros(false)))	{
			// if one of the operand is zero (not data reference zero).
			if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				&& datumFirstOperand.isZeros(false))	{
				// first operand is zero. we don't care second operand's dimension.
				datumReturn.copyTypeValueDeep(datumSecondOperand);
			} else	{
				// second operand is zero. we don't care first operand's dimension.
				datumReturn.copyTypeValueDeep(datumFirstOperand);
			}			
		} else if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
						|| datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			//at least one of the operand is not array.
			// if cannot be converted to complex, exception
			DataClass datum1stReal = datumFirstOperand.getRealDataClass();
			DataClass datum1stImage = datumFirstOperand.getImageDataClass();
			DataClass datum2ndReal = datumSecondOperand.getRealDataClass();
			DataClass datum2ndImage = datumSecondOperand.getImageDataClass();
			DATATYPES enumRealType = DATATYPES.DATUM_INTEGER;
			if (datum1stReal.getDataType() == DATATYPES.DATUM_DOUBLE
					|| datum2ndReal.getDataType() == DATATYPES.DATUM_DOUBLE)	{
				enumRealType = DATATYPES.DATUM_DOUBLE;
			}
			datum1stReal.setDataClass(DATATYPES.DATUM_DOUBLE, datum1stReal.getDataValue().add(datum2ndReal.getDataValue()),
					"", "", new DataClass[0], AEInvalid.AEINVALID, "");
			if (enumRealType == DATATYPES.DATUM_INTEGER && datum1stReal.isSingleInteger())	{
				datum1stReal.changeDataType(enumRealType);
			}
			DATATYPES enumImageType = DATATYPES.DATUM_INTEGER;
			if (datum1stImage.getDataType() == DATATYPES.DATUM_DOUBLE
					|| datum2ndImage.getDataType() == DATATYPES.DATUM_DOUBLE)	{
				enumImageType = DATATYPES.DATUM_DOUBLE;
			}
			datum1stImage.setDataClass(DATATYPES.DATUM_DOUBLE, datum1stImage.getDataValue().add(datum2ndImage.getDataValue()),
					"", "", new DataClass[0], AEInvalid.AEINVALID, "");
			if (enumImageType == DATATYPES.DATUM_INTEGER && datum1stImage.isSingleInteger())	{
				datum1stImage.changeDataType(enumImageType);
			}
			datumReturn.setComplex(datum1stReal, datum1stImage);
			if (datum1stImage.getDataValue().isActuallyZero())	{
				datumReturn = datumReturn.getRealDataClass();
			}			
		} else	{
			int nListLen1 = datumFirstOperand.getDataListSize();
			int nListLen2 = datumSecondOperand.getDataListSize();
			if (nListLen1 != nListLen2)	{
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
			}
			// both of the operands are arrays
			DataClass[] dataList = new DataClass[nListLen1];
			int[] narrayIndex = new int[1];
			for (int index = 0; index < nListLen1; index ++)	{
				narrayIndex[0] = index;
				dataList[index] = evaluateAdding(datumFirstOperand.getDataAtIndexByRef(narrayIndex),
											datumSecondOperand.getDataAtIndexByRef(narrayIndex));
			}
			datumReturn.setDataList(dataList);
		}
		return datumReturn;
	}
	
	public static DataClass evaluateSubstraction(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException	{
		// note that if operand(s) are array, they should have been fully populated.
		// Moreover, the parameters datumFirstOperand and datumSecondOperand will not be modified inside
		// the function.
		DataClass datumReturn = new DataClass();
		if ((datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
                    && datumFirstOperand.isZeros(false))
				|| (datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA
                    && datumSecondOperand.isZeros(false)))	{
			// if one of the operand is zero (not data reference zero).
			if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
                    && datumFirstOperand.isZeros(false))	{
				datumReturn = evaluateNegSign(datumSecondOperand);
			} else	{
				// second operand is zero. we don't care first operand's dimension.
				datumReturn.copyTypeValueDeep(datumFirstOperand);
			}			
		} else if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
						|| datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			//at least one of the operand is not array.
			// if cannot be converted to complex, exception
			DataClass datum1stReal = datumFirstOperand.getRealDataClass();
			DataClass datum1stImage = datumFirstOperand.getImageDataClass();
			DataClass datum2ndReal = datumSecondOperand.getRealDataClass();
			DataClass datum2ndImage = datumSecondOperand.getImageDataClass();
			DATATYPES enumRealType = DATATYPES.DATUM_INTEGER;
			if (datum1stReal.getDataType() == DATATYPES.DATUM_DOUBLE
					|| datum2ndReal.getDataType() == DATATYPES.DATUM_DOUBLE)	{
				enumRealType = DATATYPES.DATUM_DOUBLE;
			}
			datum1stReal.setDataClass(DATATYPES.DATUM_DOUBLE, datum1stReal.getDataValue().subtract(datum2ndReal.getDataValue()),
					"", "", new DataClass[0], AEInvalid.AEINVALID, "");
			if (enumRealType == DATATYPES.DATUM_INTEGER && datum1stReal.isSingleInteger())	{
				datum1stReal.changeDataType(enumRealType);
			}
			DATATYPES enumImageType = DATATYPES.DATUM_INTEGER;
			if (datum1stImage.getDataType() == DATATYPES.DATUM_DOUBLE
					|| datum2ndImage.getDataType() == DATATYPES.DATUM_DOUBLE)	{
				enumImageType = DATATYPES.DATUM_DOUBLE;
			}
			datum1stImage.setDataClass(DATATYPES.DATUM_DOUBLE, datum1stImage.getDataValue().subtract(datum2ndImage.getDataValue()),
					"", "", new DataClass[0], AEInvalid.AEINVALID, "");
			if (enumImageType == DATATYPES.DATUM_INTEGER && datum1stImage.isSingleInteger())	{
				datum1stImage.changeDataType(enumImageType);
			}
			datumReturn.setComplex(datum1stReal, datum1stImage);
            if (datumReturn.getImage().isActuallyZero())    {
				datumReturn = datumReturn.getRealDataClass();
			}
		} else	{
			int nListLen1 = datumFirstOperand.getDataListSize();
			int nListLen2 = datumSecondOperand.getDataListSize();
			if (nListLen1 != nListLen2)	{
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
			}
			// both of the operands are arrays
			DataClass[] dataList = new DataClass[nListLen1];
			int[] narrayIndex = new int[1];
			for (int index = 0; index < nListLen1; index ++)	{
				narrayIndex[0] = index;
				dataList[index] = evaluateSubstraction(datumFirstOperand.getDataAtIndexByRef(narrayIndex),
											datumSecondOperand.getDataAtIndexByRef(narrayIndex));
			}
			datumReturn.setDataList(dataList);
		}
		return datumReturn;
	}
	
	public static DataClass evaluateMultiplication(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException	{
		// note that if operand(s) are array, they should have been fully populated.
		// the parameters datumFirstOperand and datumSecondOperand will not be modified inside
		// the function.
		DataClass datumReturn = new DataClass();
		if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				&& datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			// if data1 and data2 are not arrays
            if (datumFirstOperand.getImage().isActuallyZero() && datumSecondOperand.getImage().isActuallyZero()) {
                // if both data1 and data2 are real values. Need not to worry about string type because getImage in that case throws exceptions
                // ok, now the data types are ok
                MFPNumeric mfpNumResult = datumFirstOperand.getReal().multiply(datumSecondOperand.getReal());
                if (mfpNumResult.isActuallyInteger())   {
                    datumReturn.setDataValue(mfpNumResult, DATATYPES.DATUM_INTEGER);
                } else {
                    datumReturn.setDataValue(mfpNumResult, DATATYPES.DATUM_DOUBLE);
                }
            } else {
                // one of the data is complex.
                DataClass datum1stReal = datumFirstOperand.getRealDataClass();
                DataClass datum1stImage = datumFirstOperand.getImageDataClass();
                DataClass datum2ndReal = datumSecondOperand.getRealDataClass();
                DataClass datum2ndImage = datumSecondOperand.getImageDataClass();
                DATATYPES enumRealType = DATATYPES.DATUM_INTEGER;
                if (datum1stReal.getDataType() == DATATYPES.DATUM_DOUBLE
                        || datum2ndReal.getDataType() == DATATYPES.DATUM_DOUBLE
                        || datum1stImage.getDataType() == DATATYPES.DATUM_DOUBLE
                        || datum2ndImage.getDataType() == DATATYPES.DATUM_DOUBLE)	{
                    enumRealType = DATATYPES.DATUM_DOUBLE;
                }
                DataClass datumResultReal = new DataClass(); 
                datumResultReal.setDataClass(DATATYPES.DATUM_DOUBLE, datum1stReal.getDataValue().multiply(datum2ndReal.getDataValue())
                        .subtract(datum1stImage.getDataValue().multiply(datum2ndImage.getDataValue())),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                if (enumRealType == DATATYPES.DATUM_INTEGER && datumResultReal.isSingleInteger())	{
                    datumResultReal.changeDataType(enumRealType);
                }
                DATATYPES enumImageType = enumRealType;
                DataClass datumResultImage = new DataClass(); 
                datumResultImage.setDataClass(DATATYPES.DATUM_DOUBLE, datum1stReal.getDataValue().multiply(datum2ndImage.getDataValue())
                        .add(datum1stImage.getDataValue().multiply(datum2ndReal.getDataValue())),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                if (enumImageType == DATATYPES.DATUM_INTEGER && datumResultImage.isSingleInteger())	{
                    datumResultImage.changeDataType(enumImageType);
                }
                datumReturn.setComplex(datumResultReal, datumResultImage);
                if (datumReturn.getImage().isActuallyZero())    {
                    datumReturn = datumReturn.getRealDataClass();
                }
            }
		} else if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				|| datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			// if one of the data is not array
			DataClass datumArray = new DataClass(), datumNumber = new DataClass();
			boolean bNumber1st = true;
			if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
				datumArray.copyTypeValue(datumSecondOperand);
				datumNumber.copyTypeValue(datumFirstOperand);
			} else	{
				datumArray.copyTypeValue(datumFirstOperand);
				datumNumber.copyTypeValue(datumSecondOperand);
				bNumber1st = false;
			}
			DataClass[] dataList = new DataClass[datumArray.getDataListSize()];
			int[] narrayIndex = new int[1];
			for (int index = 0; index < datumArray.getDataListSize(); index ++)	{
				narrayIndex[0] = index;
				if (bNumber1st)	{
					dataList[index] = evaluateMultiplication(datumNumber, datumArray.getDataAtIndexByRef(narrayIndex));
				} else	{
					dataList[index] = evaluateMultiplication(datumArray.getDataAtIndexByRef(narrayIndex), datumNumber);
				}
			}
			datumReturn.setDataList(dataList);
		} else	{
			// if both of the operands are arrays.
			int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
			if (narrayFirstDims.length == 0
					|| narrayFirstDims[narrayFirstDims.length - 1] != datumSecondOperand.getDataListSize())	{
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
			}
			
			if (datumFirstOperand.getDataListSize() <= 0)	{
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
			} else if (datumFirstOperand.getDataList()[0].getDataType() != DATATYPES.DATUM_REF_DATA)	{
				// because the array has been fully populated, we must have arrived at the last dimension
				int nFirstOperandDataListSize = datumFirstOperand.getDataListSize();
				if (nFirstOperandDataListSize != datumSecondOperand.getDataListSize())	{
					throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
				}
				for (int index = 0; index < nFirstOperandDataListSize; index ++)	{
					if (datumFirstOperand.getDataList()[index].getDataType() == DATATYPES.DATUM_REF_DATA)	{
						throw new JFCALCExpErrException(ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
					}
					DataClass dataResultElem = evaluateMultiplication(datumFirstOperand.getDataList()[index],
																datumSecondOperand.getDataList()[index]);
					if (index == 0)	{
						datumReturn = dataResultElem;
					} else	{
						datumReturn = evaluateAdding(datumReturn, dataResultElem);
					}
				}
			} else	{
				int nFirstOperandDataListSize = datumFirstOperand.getDataListSize();
				DataClass[] dataList = new DataClass[nFirstOperandDataListSize];
				// because the array has been fully populated, recursion can be used.
				for (int index = 0; index < nFirstOperandDataListSize; index ++)	{
					// we need not to worry about null return because only if 
					// datumFirstOperand.GetDataListSize() > 0 we can enter this loop.
					DataClass datumResultElem = evaluateMultiplication(datumFirstOperand.getDataList()[index], datumSecondOperand);
					dataList[index] = datumResultElem;
				}
				datumReturn.setDataList(dataList);
			}
		}
		return datumReturn;
	}
	
	public static DataClass divideByNumber(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException	{
		// in this function, the denominator should not be an array.
		if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA && datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			// if data1 and data2 are not arrays
            // should not directly compare datumSecondOperand.getDataValue() or its abs value with 0 coz it is more close to zero than
            // real and image.
            /* // no longer need divide by zero exception.
             * if (datumSecondOperand.isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)))	{
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_DIVISOR_CAN_NOT_BE_ZERO);	
            }*/
            if (datumFirstOperand.getImage().isActuallyZero() && datumSecondOperand.getImage().isActuallyZero()) {
                // if both data1 and data2 are real values. need not to worry about string type because getImage will throw exceptions if so.
                // ok, now the data types are ok
                MFPNumeric mfpNum1stOperand = datumFirstOperand.getReal();
                MFPNumeric mfpNum2ndOperand = datumSecondOperand.getReal();
                MFPNumeric mfpNumResult = MFPNumeric.divide(mfpNum1stOperand, mfpNum2ndOperand);
                DataClass datumReturn = new DataClass();
                if (mfpNumResult.isActuallyInteger())   {
                    datumReturn.setDataValue(mfpNumResult, DATATYPES.DATUM_INTEGER);
                } else {
                    datumReturn.setDataValue(mfpNumResult, DATATYPES.DATUM_DOUBLE);
                }
                return datumReturn;
            } else {
                DataClass datum1stReal = datumFirstOperand.getRealDataClass();
                DataClass datum1stImage = datumFirstOperand.getImageDataClass();
                DataClass datum2ndReal = datumSecondOperand.getRealDataClass();
                DataClass datum2ndImage = datumSecondOperand.getImageDataClass();
                DATATYPES enumRealType = DATATYPES.DATUM_DOUBLE;
                DATATYPES enumImageType = DATATYPES.DATUM_DOUBLE;
                MFPNumeric mfpNumTmp = datum2ndReal.getDataValue().multiply(datum2ndReal.getDataValue())
                        .add(datum2ndImage.getDataValue().multiply(datum2ndImage.getDataValue()));
                DataClass datumResultReal = new DataClass();
                datumResultReal.setDataClass(enumRealType, MFPNumeric.divide(datum1stReal.getDataValue().multiply(datum2ndReal.getDataValue())
                        .add(datum1stImage.getDataValue().multiply(datum2ndImage.getDataValue())), mfpNumTmp),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                DataClass datumResultImage = new DataClass();
                datumResultImage.setDataClass(enumImageType, MFPNumeric.divide(datum1stReal.getDataValue().negate()
                        .multiply(datum2ndImage.getDataValue()).add(datum1stImage.getDataValue()
                        .multiply(datum2ndReal.getDataValue())), mfpNumTmp),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                DataClass datumReturn = new DataClass();
                datumReturn.setComplex(datumResultReal, datumResultImage);
                if (datumReturn.getImage().isActuallyZero())    {
                    datumReturn = datumReturn.getRealDataClass();
                }
                return datumReturn;
            }
		} else if (datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			DataClass[] dataList = new DataClass[datumFirstOperand.getDataListSize()];
			for (int index = 0; index < datumFirstOperand.getDataListSize(); index ++)	{
				dataList[index] = divideByNumber(datumFirstOperand.getDataList()[index],
													datumSecondOperand);
			}
			DataClass datumReturn = new DataClass();
			datumReturn.setDataList(dataList);
			return datumReturn;
		} else	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_TYPE);	
		}
		
	}

	private static DataClass cvtMatrix2Vector(DataClass datumMatrix, int nCvtLevel) throws JFCALCExpErrException	{
		// assume that datumMatrix is a fully populated matrix and nCvtlevel is a positive integer.
		// this function will refer to each element of the parameter datumMatrix, but will not change its value.
		int[] narraySize = datumMatrix.recalcDataArraySize();
		if (narraySize.length <= nCvtLevel + 1)	{
			return datumMatrix;
		}
		DataClass datumReturn = new DataClass();
		DataClass[] dataList = new DataClass[0];
		for (int index = 0; index < datumMatrix.getDataListSize(); index ++)	{
			DataClass datumTmp = cvtMatrix2Vector(datumMatrix.getDataList()[index], nCvtLevel);
			DataClass[] dataList1 = new DataClass[dataList.length + datumTmp.getDataListSize()];
			for (int index1 = 0; index1 < dataList.length + datumTmp.getDataListSize(); index1 ++)	{
				if (index1 < dataList.length)	{
					dataList1[index1] = dataList[index1];
				} else	{
					dataList1[index1] = datumTmp.getDataList()[index1 - dataList.length];
				}
			}
			dataList = dataList1;
		}
		datumReturn.setDataList(dataList);
		return datumReturn;
	}
	
	public static DataClass evaluateDivision(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException	{
		// note that if operand(s) are array, they should have been fully populated.
		// the parameters datumFirstOperand and datumSecondOperand will not be modified inside
		// the function.
		DataClass datumReturn = new DataClass();
		if (datumFirstOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				&& datumFirstOperand.isEye(false))	{
			// first operand is 1.
			datumReturn = evaluateReciprocal(datumSecondOperand);		
		} else if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA
				&& datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)	{
			int[] narrayFirstSize = datumFirstOperand.recalcDataArraySize();
			int[] narraySecondSize = datumSecondOperand.recalcDataArraySize();
			if (narrayFirstSize.length < 2 || narraySecondSize.length < 2)	{
				// one dim matrix division is not well defined. For example, [1, 2]/[2, 4] can
				// result in a 2 * 2 matrix, but can also returns 0.5.
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
			}
			for (int index = 1; index < narraySecondSize.length; index ++)	{
                int nIndexGap = narrayFirstSize.length - narraySecondSize.length;
				if (narrayFirstSize[index + nIndexGap] != narraySecondSize[index])	{
					throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
				}
			}
            DataClass datumT1stOprnd = evaluateTransposition(datumFirstOperand);
            DataClass datumT2ndOprnd = evaluateTransposition(datumSecondOperand);
			DataClass datumLinearizedTy = cvtMatrix2Vector(datumT1stOprnd, narrayFirstSize.length + 1 - narraySecondSize.length);
			DataClass datumLinearizedTA = cvtMatrix2Vector(datumT2ndOprnd, 1);
			datumReturn = leftDivideBy2DMatrix(datumLinearizedTy, datumLinearizedTA);
            datumReturn = evaluateTransposition(datumReturn);
		} else	{
			datumReturn = divideByNumber(datumFirstOperand, datumSecondOperand);
		}
		
		return datumReturn;
	}
	
	public static DataClass evaluateReciprocal(DataClass datum) throws JFCALCExpErrException	{
		// note that if operand is an array, it should have been fully populated.
		// the parameter datum will not be modified in this function.
		DataClass datumReturn = new DataClass();
		if (datum.getDataType() == DATATYPES.DATUM_REF_DATA)	{
			int[] narraySize = datum.recalcDataArraySize();
			if (narraySize.length != 2)	{
				// at this moment only support reciprocal of number of 2D matrix
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
			}
			DataClass datumEye = createEyeMatrix(narraySize[1], 2);
			datumReturn = evaluateDivision(datumEye, datum);
		} else	{	// it is just an number.
			datumReturn = divideByNumber(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE), datum);
		}
		return datumReturn;
	}
	
	public static DataClass evaluateLeftDivision(DataClass datumFirstOperand, DataClass datumSecondOperand) throws JFCALCExpErrException	{
		// note that if operand(s) are array, they should have been fully populated.
		// the parameters datumFirstOperand and datumSecondOperand will not be modified inside
		// the function.
        // also note that left division the first operator is divisor
		DataClass datumReturn = new DataClass();
		if (datumSecondOperand.getDataType() != DATATYPES.DATUM_REF_DATA
				&& datumSecondOperand.isEye(false))	{
			// second operand is 1.
			datumReturn = evaluateLeftReciprocal(datumFirstOperand);		
		} else if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA
				&& datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)	{
			int[] narrayFirstSize = datumFirstOperand.recalcDataArraySize();
			int[] narraySecondSize = datumSecondOperand.recalcDataArraySize();
			if (narrayFirstSize.length < 2 || narraySecondSize.length < 2)	{
				// one dim matrix left-division is not well defined.
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
			}
			for (int index = 0; index < narrayFirstSize.length - 1; index ++)	{
				if (narrayFirstSize[index] != narraySecondSize[index])	{
					throw new JFCALCExpErrException(ERRORTYPES.ERROR_PARAMETER_NOT_MATCH);
				}
			}
			DataClass datumLinearizedy = cvtMatrix2Vector(datumSecondOperand, narraySecondSize.length + 1 - narrayFirstSize.length);
			DataClass datumLinearizedA = cvtMatrix2Vector(datumFirstOperand, 1);
			datumReturn = leftDivideBy2DMatrix(datumLinearizedy, datumLinearizedA);
		} else	{
			datumReturn = divideByNumber(datumSecondOperand, datumFirstOperand);
		}
		
		return datumReturn;
	}
	
	public static DataClass evaluateLeftReciprocal(DataClass datum) throws JFCALCExpErrException	{
		// note that if operand is an array, it should have been fully populated.
		// the parameter datum will not be modified in this function.
		DataClass datumReturn = new DataClass();
		if (datum.getDataType() == DATATYPES.DATUM_REF_DATA)	{
			int[] narraySize = datum.recalcDataArraySize();
			if (narraySize.length != 2)	{
				// at this moment only support reciprocal of number of 2D matrix
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
			}
			DataClass datumEye = createEyeMatrix(narraySize[0], 2);
			datumReturn = evaluateLeftDivision(datum, datumEye);
		} else	{	// it is just an number.
			datumReturn = divideByNumber(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE), datum);
		}
		return datumReturn;
	}
	
	public static DataClass evaluateTransposition(DataClass datumOperand) throws JFCALCExpErrException	{
		return evaluateTransposition(datumOperand, datumOperand.recalcDataArraySize());
	}
	
	public static DataClass evaluateTransposition(DataClass datumOperand, int[] narrayDims) throws JFCALCExpErrException	{
		// assume this array (datumOperand) has been fully populated if the operand is an array
		// and narrayDims is the dim of the array datumOperand will not be changed in this function
		// and the return value will not refer to any part of datumOperand.
		if (datumOperand.getDataType() != DATATYPES.DATUM_REF_DATA)	{
			// if it is not an array.
			DataClass datumReturn = new DataClass();
			datumReturn.copyTypeValue(datumOperand);
			return datumReturn;
		} else if (narrayDims == null || narrayDims.length == 0)	{
			return datumOperand;    // do not transpose anything
		} else if (narrayDims.length == 1)	{
			// if this is a 1-D array, convert to x * 1 array
			DataClass datumReturn = new DataClass();
			DataClass[] dataList = new DataClass[narrayDims[0]];
			for (int index1 = 0; index1 < narrayDims[0]; index1 ++)	{
				DataClass datum = new DataClass();
				datum.copyTypeValue(datumOperand.getDataList()[index1]);
				DataClass[] dataList1 = new DataClass[1];
				dataList1[0] = datum;
				dataList[index1] = new DataClass();
				dataList[index1].setDataList(dataList1);
			}
			datumReturn.setDataList(dataList);
			return datumReturn;
		} else if (narrayDims.length == 2)	{
            // if narrayDims.length == 2, use a fast way
			DataClass datumReturn = new DataClass();
			DataClass[] dataList1 = new DataClass[narrayDims[1]];
			for (int index = 0; index < narrayDims[1]; index ++)	{
				dataList1[index] = new DataClass();
				DataClass[] dataList0 = new DataClass[narrayDims[0]];
				for (int index1 = 0; index1 < narrayDims[0]; index1 ++)	{
					dataList0[index1] = new DataClass();
					dataList0[index1].copyTypeValue(datumOperand.getDataList()[index1].getDataList()[index]);
				}
				dataList1[index].setDataList(dataList0);
			}
			datumReturn.setDataList(dataList1);
			return datumReturn;
		} else  {   // narrayDims.length > 2
            DataClass datumReturn = new DataClass();
            int[] narrayNewDims = new int[narrayDims.length];
            for (int idx = 0; idx < narrayDims.length; idx ++)  {
                narrayNewDims[idx] = narrayDims[narrayDims.length - 1 -idx];
            }
            DataClass datumDefault = new DataClass();
            datumDefault.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
            datumReturn.allocDataArray(narrayNewDims, datumDefault); // populate an empty array
            DataClass datumVectorizedOprnd = cvtMatrix2Vector(datumOperand, 0); // cvt the matrix to a list to transvert it
            for (int idx = 0; idx < datumVectorizedOprnd.getDataListSize(); idx ++) {
                int[] narrayIdx = new int[narrayDims.length];
                int nTmp = idx;
                for (int idx1 = 0; idx1 < narrayDims.length; idx1 ++)   {
                    narrayIdx[idx1] = nTmp % narrayDims[narrayDims.length - 1 - idx1];
                    nTmp /= narrayDims[narrayDims.length - 1 - idx1];
                }
                datumReturn.setDataAtIndex(narrayIdx, datumVectorizedOprnd.getDataList()[idx]);
            }
            return datumReturn;
        }
	}
	
    public static DataClass evaluateDeterminant(DataClass datum2DSqrMatrixOprd) throws JFCALCExpErrException   {
        // this function is used to calculate determiinant of a 2D square matrix. Assume datum2DMatrix have been fully populated.
        // Note that the parameter will not be modified in this function.
        DataClass datum2DSqrMatrix = new DataClass();
        datum2DSqrMatrix.copyTypeValueDeep(datum2DSqrMatrixOprd);

        int[] narraySize2DSqrMatrix = datum2DSqrMatrix.recalcDataArraySize();
		if (narraySize2DSqrMatrix.length != 2 || narraySize2DSqrMatrix[0] == 0 || narraySize2DSqrMatrix[1] == 0
                || narraySize2DSqrMatrix[0] != narraySize2DSqrMatrix[1])	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
		}
		
        int nNumofLnSwaps = 0;
        for (int idxLn = 0; idxLn < narraySize2DSqrMatrix[0]; idxLn ++)	{
            int nMaxAbsLnIdx = idxLn;
            DataClass datumTmp = datum2DSqrMatrix.getDataList()[idxLn].getDataList()[idxLn];
            MFPNumeric mfpNumMaxAbsSqr = MFPNumeric.hypot(datumTmp.getReal(), datumTmp.getImage());
            for (int idxLn1 = idxLn + 1; idxLn1 < narraySize2DSqrMatrix[0]; idxLn1 ++)	{
                datumTmp = datum2DSqrMatrix.getDataList()[idxLn1].getDataList()[idxLn];
                MFPNumeric mfpNumAbsSqr = MFPNumeric.hypot(datumTmp.getReal(), datumTmp.getImage());
                if (mfpNumAbsSqr.compareTo(mfpNumMaxAbsSqr) > 0)    {
                    nMaxAbsLnIdx = idxLn1;
                    mfpNumMaxAbsSqr = mfpNumAbsSqr;
                }
            }
            if (nMaxAbsLnIdx != idxLn)  {
                // swap the line to ensure the driagonal element always have the max abs value compared to others in the same
                // column but lower lines
                DataClass[] dataListTmp = datum2DSqrMatrix.getDataList()[idxLn].getDataList();
                datum2DSqrMatrix.getDataList()[idxLn].setDataList(datum2DSqrMatrix.getDataList()[nMaxAbsLnIdx].getDataList());
                datum2DSqrMatrix.getDataList()[nMaxAbsLnIdx].setDataList(dataListTmp);
                nNumofLnSwaps ++;
            }
            datumTmp = new DataClass();
            datumTmp.setDataValue(MFPNumeric.ZERO);
            if (datum2DSqrMatrix.getDataList()[idxLn].getDataList()[idxLn].isEqual(datumTmp))   {
                // determinant is zero
                return datumTmp;
            }
            for (int idxLn1 = idxLn + 1; idxLn1 < narraySize2DSqrMatrix[0]; idxLn1 ++)	{
                DataClass datumRowEliminateRatio
                    = divideByNumber(datum2DSqrMatrix.getDataList()[idxLn1].getDataList()[idxLn],
                                    datum2DSqrMatrix.getDataList()[idxLn].getDataList()[idxLn]);
                datum2DSqrMatrix.getDataList()[idxLn1]
                    = evaluateSubstraction(
                            datum2DSqrMatrix.getDataList()[idxLn1],
                            evaluateMultiplication(datumRowEliminateRatio, datum2DSqrMatrix.getDataList()[idxLn]));
            }
        }
        DataClass datumReturn = datum2DSqrMatrix.getDataList()[0].getDataList()[0];
        for (int idx = 1; idx < narraySize2DSqrMatrix[0]; idx ++)   {
            datumReturn = evaluateMultiplication(datumReturn, datum2DSqrMatrix.getDataList()[idx].getDataList()[idx]);
        }
        if (nNumofLnSwaps % 2 == 1) {
            DataClass datumTmp = new DataClass();
            datumTmp.setDataValue(MFPNumeric.MINUS_ONE);
            datumReturn = evaluateMultiplication(datumReturn, datumTmp);
        }
        return datumReturn;
    }
    
	public static DataClass leftDivideBy2DMatrix(DataClass datumNumeratorOperand, DataClass datum2DMatrixOperand) throws JFCALCExpErrException	{
		// this function is used to calculate x value in Ax = y where A is datum2DMatrixOperand, y is datumNumeratorOperand
		// assume A and y are both fully populated. Note that parameters will not be modified in this function and return
		// value will not refer to any part of the parameters.
		DataClass datumNumerator = new DataClass();
		datumNumerator.copyTypeValueDeep(datumNumeratorOperand);
		DataClass datum2DMatrix = new DataClass();
		datum2DMatrix.copyTypeValueDeep(datum2DMatrixOperand);
		
		int[] narraySizeNumerator = datumNumerator.recalcDataArraySize();
		int[] narraySize2DMatrix = datum2DMatrix.recalcDataArraySize();
		
		DataClass datumFormatedNumerator = datumNumerator;
		if (narraySizeNumerator.length == 1)	{
			if (narraySizeNumerator[0] == 0)	{
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
			} else	{
				// this is 1D vector, in the transpose, it is turned to x * 1 vector
				datumFormatedNumerator = evaluateTransposition(datumNumerator);
			}
		} else	{
			for (int index = 0; index < narraySizeNumerator.length; index ++)	{
				if (narraySizeNumerator[index] == 0)	{
					throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
				}
			}
		}
		narraySizeNumerator = new int[1];
		narraySizeNumerator[0] = datumFormatedNumerator.getDataListSize();
		
		if (narraySize2DMatrix.length != 2 || narraySize2DMatrix[0] == 0 || narraySize2DMatrix[1] == 0)	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
		}
		
		if (narraySize2DMatrix[0] < narraySize2DMatrix[1])	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INDEFINITE_RESULT);
		}
		
		if (narraySizeNumerator[0] != narraySize2DMatrix[0])	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
		}
		
		for (int index0 = 0; index0 < narraySize2DMatrix[1]; index0 ++)	{   // for every column
			for (int index1 = 0; index1 < narraySize2DMatrix[0]; index1 ++)	{   // for every line
				if (index0 == index1)	{
					continue;
				} else	{
					if (datum2DMatrix.getDataList()[index0].getDataList()[index0]
					    .isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)))	{
						// if the diagonal element is 0.
						int index2 = index0 + 1;
						for (; index2 < narraySize2DMatrix[0]; index2 ++)	{
							if (datum2DMatrix.getDataList()[index2].getDataList()[index0]
							    .isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)) == false)	{
								datum2DMatrix.getDataList()[index0]
									= evaluateAdding(datum2DMatrix.getDataList()[index0], datum2DMatrix.getDataList()[index2]);
								datumFormatedNumerator.getDataList()[index0]
									= evaluateAdding(datumFormatedNumerator.getDataList()[index0], datumFormatedNumerator.getDataList()[index2]);
								break;
							}
						}
						if (index2 == narraySize2DMatrix[0])	{
							throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_ANSWER_FOR_MATRIX_DIVISION);
						}
					}
					DataClass datumRowEliminateRatio
						= divideByNumber(datum2DMatrix.getDataList()[index1].getDataList()[index0],
										datum2DMatrix.getDataList()[index0].getDataList()[index0]);
					datum2DMatrix.getDataList()[index1]
					    = evaluateSubstraction(
					    		datum2DMatrix.getDataList()[index1],
								evaluateMultiplication(datumRowEliminateRatio, datum2DMatrix.getDataList()[index0]));
					datumFormatedNumerator.getDataList()[index1]
					    = evaluateSubstraction(
					    		datumFormatedNumerator.getDataList()[index1],
								evaluateMultiplication(datumRowEliminateRatio, datumFormatedNumerator.getDataList()[index0]));
				}
			}
		}
		for (int index0 = narraySize2DMatrix[1]; index0 < narraySize2DMatrix[0]; index0 ++)	{
			DataClass datumTmp = cvtMatrix2Vector(datumFormatedNumerator.getDataList()[index0], 0);
			for (int index1 = 0; index1 < datumTmp.getDataListSize(); index1 ++)	{
				if (datumTmp.getDataList()[index1]
                        .isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO),
                                narraySize2DMatrix[1] * narraySize2DMatrix[1]) == false)	{   // the error tolerance scale is narraySize2DMatrix[1] * narraySize2DMatrix[1]
					throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_ANSWER_FOR_MATRIX_DIVISION);
				}
			}
		}
		DataClass[] dataList = new DataClass[narraySize2DMatrix[1]];
		for (int index0 = 0; index0 < narraySize2DMatrix[1]; index0 ++)	{
			DataClass datum = divideByNumber(datumFormatedNumerator.getDataList()[index0],
											datum2DMatrix.getDataList()[index0].getDataList()[index0]);		
			dataList[index0] = datum;
		}
		DataClass datumResult = new DataClass();
		datumResult.setDataList(dataList);
		return datumResult;
	}
	
	public static DataClass invert2DSquare(DataClass datum2DSquareOperand) throws JFCALCExpErrException	{
		// assume that datum2DSquare is always a 2D square matrix and it has been fully populated.
		// Moreover, the parameter datum2DSquareOperand will be not be modified inside the function.
		// nor the return value will refer to any part of the parameter.

		DataClass datum2DSquare = new DataClass();
		datum2DSquare.copyTypeValueDeep(datum2DSquareOperand);
		int[] narraySquareSize = datum2DSquare.recalcDataArraySize();
		if (narraySquareSize.length != 2
				|| narraySquareSize[0] != narraySquareSize[1]
				|| narraySquareSize[0] == 0)	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_MATRIX_SIZE);
		}
		// assume Matrix has been fully populated
		//datum2DSquare.PopulateDataArray(narraySquareSize, true);
		
		DataClass datum2DInv = new DataClass();
        DataClass datumDefault = new DataClass();
        datumDefault.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
		datum2DInv.allocDataArray(narraySquareSize, datumDefault);
		
		//initialize I
		for (int index0 = 0; index0 < narraySquareSize[0]; index0 ++)	{
			for (int index1 = 0; index1 < narraySquareSize[1]; index1 ++)	{
				// because datum2DInv is newly created and call AllocDataArray,
				// it should have been fully populated.
				if (index0 == index1)	{
					datum2DInv.getDataList()[index0].getDataList()[index1].setDataValue(MFPNumeric.ONE);
				} else	{
					datum2DInv.getDataList()[index0].getDataList()[index1].setDataValue(MFPNumeric.ZERO);
				}
			}
		}
		
		for (int index0 = 0; index0 < narraySquareSize[0]; index0 ++)	{
			for (int index1 = 0; index1 < narraySquareSize[0]; index1 ++)	{
				if (index0 == index1)	{
					continue;
				} else	{
                    DataClass datumLargestAbs = new DataClass();
                    MFPNumeric[] mfpNumRadAng = datum2DSquare.getDataList()[index0].getDataList()[index0].getComplexRadAngle();
                    datumLargestAbs.setDataValue(mfpNumRadAng[0]);
					int nLargestAbsIdx = index0;
					for (int idx = index0 + 1; idx < narraySquareSize[0]; idx ++)	{
						mfpNumRadAng = datum2DSquare.getDataList()[idx].getDataList()[index0].getComplexRadAngle();
                        DataClass datumThisAbs = new DataClass();
                        datumThisAbs.setDataValue(mfpNumRadAng[0]);
                        // we can do direct value compare here because both datumLargestAbs and datumThisAbs are real.
						if (datumLargestAbs.getDataValue().compareTo(datumThisAbs.getDataValue()) < 0)	{
							datumLargestAbs = datumThisAbs;
							nLargestAbsIdx = idx;
						}
					}
					if (datum2DSquare.getDataList()[nLargestAbsIdx].getDataList()[index0]
					     .isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)))	{
						// even the largest abs value is zero.
						throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_CANNOT_BE_INVERTED);
					}
					if (nLargestAbsIdx != index0)	{
						// swap the rows
						DataClass datumTmp = datum2DSquare.getDataList()[index0];
						datum2DSquare.getDataList()[index0] = datum2DSquare.getDataList()[nLargestAbsIdx];
						datum2DSquare.getDataList()[nLargestAbsIdx] = datumTmp;
						datumTmp = datum2DInv.getDataList()[index0];
						datum2DInv.getDataList()[index0] = datum2DInv.getDataList()[nLargestAbsIdx];
						datum2DInv.getDataList()[nLargestAbsIdx] = datumTmp;
					}
					/*
					 * the following code cannot guarantee to use maximum abs value at [index0][index0]
					if (datum2DSquare.getDataList()[index0].getDataList()[index0]
					    .isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)))	{
						// if the diagonal element is 0.
						int index2 = index0 + 1;
						for (; index2 < narraySquareSize[0]; index2 ++)	{
							if (datum2DSquare.getDataList()[index2].getDataList()[index0]
							    .isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO)) == false)	{
								datum2DSquare.getDataList()[index0]
													        = evaluateAdding(datum2DSquare.getDataList()[index0], datum2DSquare.getDataList()[index2]);
								datum2DInv.getDataList()[index0]
													        = evaluateAdding(datum2DInv.getDataList()[index0], datum2DInv.getDataList()[index2]);
								break;
							}
						}
						if (index2 == narraySquareSize[0])	{
							throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_CANNOT_BE_INVERTED);
						}
					}*/
					DataClass datumRowEliminateRatio
						= divideByNumber(
								datum2DSquare.getDataList()[index1].getDataList()[index0],
								datum2DSquare.getDataList()[index0].getDataList()[index0]);
					datum2DSquare.getDataList()[index1]
					    = evaluateSubstraction(
					    		datum2DSquare.getDataList()[index1],
								evaluateMultiplication(datumRowEliminateRatio, datum2DSquare.getDataList()[index0]));
					datum2DInv.getDataList()[index1]
					    = evaluateSubstraction(
					    		datum2DInv.getDataList()[index1],
								evaluateMultiplication(datumRowEliminateRatio, datum2DInv.getDataList()[index0]));
				}
			}
		}
		for (int index0 = 0; index0 < narraySquareSize[0]; index0 ++)	{
			for (int index1 = 0; index1 < narraySquareSize[0]; index1 ++)	{
				datum2DInv.getDataList()[index0].getDataList()[index1]
				    = divideByNumber(
						datum2DInv.getDataList()[index0].getDataList()[index1],
						datum2DSquare.getDataList()[index0].getDataList()[index0]);
			}
			
		}
		return datum2DInv;
	}
    
    public static DataClass evaluateExp(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumCoeff = MFPNumeric.exp(datum.getReal());
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        datumReturnNum.setComplex(mfpNumCoeff.multiply(MFPNumeric.cos(mfpNumImagePart)), mfpNumCoeff.multiply(MFPNumeric.sin(mfpNumImagePart)));
        return datumReturnNum;
    }
    
    public static DataClass evaluateLog(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        /* log 0 can be handled now.
         * if (mfpNumRealPart.isActuallyZero() && mfpNumImagePart.isActuallyZero())   {
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
        }*/
        MFPNumeric mfpNumReturnReal = MFPNumeric.log(MFPNumeric.hypot(mfpNumRealPart, mfpNumImagePart));
        MFPNumeric mfpNumReturnImage = MFPNumeric.atan2(mfpNumImagePart, mfpNumRealPart);
        DataClass datumReturnNum = new DataClass();
        datumReturnNum.setComplex(mfpNumReturnReal, mfpNumReturnImage);
        return datumReturnNum;
    }
    
    public static DataClass evaluateSin(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum.setDataValue(MFPNumeric.sin(mfpNumRealPart));
        } else  {
            // this is a complex number
            MFPNumeric mfpNumExpImage = MFPNumeric.exp(mfpNumImagePart);
            MFPNumeric mfpNumExpMinusImage = MFPNumeric.exp(mfpNumImagePart.negate());
            MFPNumeric mfpNumReturnReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumReturnImage = mfpNumExpImage.subtract(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            datumReturnNum.setComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateCos(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum.setDataValue(MFPNumeric.cos(mfpNumRealPart));
        } else  {
            // this is a complex number
            MFPNumeric mfpNumExpImage = MFPNumeric.exp(mfpNumImagePart);
            MFPNumeric mfpNumExpMinusImage = MFPNumeric.exp(mfpNumImagePart.negate());
            MFPNumeric mfpNumReturnReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumReturnImage = mfpNumExpMinusImage.subtract(mfpNumExpImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            datumReturnNum.setComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateTan(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum.setDataValue(MFPNumeric.tan(mfpNumRealPart));
        } else  {
            // this is a complex number
            MFPNumeric mfpNumExpImage = MFPNumeric.exp(mfpNumImagePart);
            MFPNumeric mfpNumExpMinusImage = MFPNumeric.exp(mfpNumImagePart.negate());
            MFPNumeric mfpNumSinReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumSinImage = mfpNumExpImage.subtract(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumCosReal = mfpNumExpImage.add(mfpNumExpMinusImage).multiply(MFPNumeric.cos(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            MFPNumeric mfpNumCosImage = mfpNumExpMinusImage.subtract(mfpNumExpImage).multiply(MFPNumeric.sin(mfpNumRealPart)).multiply(MFPNumeric.HALF);
            /* // no longer need divide by zero exception.
             * if (mfpNumCosReal.isActuallyZero() && mfpNumCosImage.isActuallyZero())  {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_DIVISOR_CAN_NOT_BE_ZERO);
            }*/
            MFPNumeric mfpNumDivisor = mfpNumCosReal.multiply(mfpNumCosReal).add(mfpNumCosImage.multiply(mfpNumCosImage));
            MFPNumeric mfpNumReturnReal = mfpNumSinReal.multiply(mfpNumCosReal).add(mfpNumSinImage.multiply(mfpNumCosImage)).divide(mfpNumDivisor);
            MFPNumeric mfpNumReturnImage = mfpNumSinImage.multiply(mfpNumCosReal).subtract(mfpNumSinReal.multiply(mfpNumCosImage)).divide(mfpNumDivisor);
            datumReturnNum.setComplex(mfpNumReturnReal, mfpNumReturnImage);
        }
        return datumReturnNum;
    }

    public static DataClass evaluateASin(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        if (mfpNumImagePart.isActuallyZero() && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.MINUS_ONE) >= 0
                && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.ONE) <= 0)   {
            // this is a real number between [-1, 1].
            datumReturnNum.setDataValue(MFPNumeric.asin(mfpNumRealPart));
        } else  {
            // the result will be a complex value.
            DataClass datumI = new DataClass();
            datumI.setComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
            DataClass datumOne = new DataClass();
            datumOne.setDataValue(MFPNumeric.ONE);
            DataClass datumZeroPntFive = new DataClass();
            datumZeroPntFive.setDataValue(MFPNumeric.HALF);
            
            DataClass datumTmp = evaluateMultiplication(datumOperand, datumOperand);
            datumTmp = evaluateSubstraction(datumOne, datumTmp);
            datumTmp = evaluatePower(datumTmp, datumZeroPntFive, null);
            DataClass datumTmp1 = evaluateMultiplication(datumOperand, datumI);
            datumTmp = evaluateAdding(datumTmp1, datumTmp);
            datumTmp = evaluateLog(datumTmp);
            datumReturnNum = evaluateDivision(datumTmp, datumI);
        }
        return datumReturnNum;
    }

    public static DataClass evaluateAbs(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        MFPNumeric[] mfpNumRadAng = datumOperand.getComplexRadAngle();
        DataClass datumReturnNum = new DataClass();
        datumReturnNum.setDataValue(mfpNumRadAng[0]);
        if (datumReturnNum.isSingleInteger())    {
            datumReturnNum.changeDataType(DATATYPES.DATUM_INTEGER);
        }
        return datumReturnNum;
    }
    
    public static DataClass evaluateACos(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        if (mfpNumImagePart.isActuallyZero() && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.MINUS_ONE) >= 0
                && MFPNumeric.compareTo(mfpNumRealPart, MFPNumeric.ONE) <= 0)    {
            // this is a real number between [-1, 1].
            datumReturnNum.setDataValue(MFPNumeric.acos(mfpNumRealPart));
        } else  {
            // the result will be a complex value.
            DataClass datumI = new DataClass();
            datumI.setComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
            DataClass datumOne = new DataClass();
            datumOne.setDataValue(MFPNumeric.ONE);
            DataClass datumZeroPntFive = new DataClass();
            datumZeroPntFive.setDataValue(MFPNumeric.HALF);
            
            DataClass datumTmp = evaluateMultiplication(datumOperand, datumOperand);
            datumTmp = evaluateSubstraction(datumTmp, datumOne);
            datumTmp = evaluatePower(datumTmp, datumZeroPntFive, null);
            datumTmp = evaluateAdding(datumOperand, datumTmp);
            datumTmp = evaluateLog(datumTmp);
            datumReturnNum = evaluateDivision(datumTmp, datumI);
        }
        return datumReturnNum;
    }
    

    public static DataClass evaluateATan(DataClass datumOperand) throws JFCALCExpErrException    {
        // parameter will not be modified in this function
        DataClass datum = new DataClass();
        datum.copyTypeValue(datumOperand);
        datum.changeDataType(DATATYPES.DATUM_COMPLEX);
        MFPNumeric mfpNumRealPart = datum.getReal();
        MFPNumeric mfpNumImagePart = datum.getImage();
        DataClass datumReturnNum = new DataClass();
        if (mfpNumImagePart.isActuallyZero())    {
            // this is a real number.
            datumReturnNum.setDataValue(MFPNumeric.atan(mfpNumRealPart));
        } else  {
            // the result will be a complex value.
            DataClass datumI = new DataClass();
            datumI.setComplex(MFPNumeric.ZERO, MFPNumeric.ONE);
            DataClass datumOne = new DataClass();
            datumOne.setDataValue(MFPNumeric.ONE);
            DataClass datumTwoI = new DataClass();
            datumTwoI.setComplex(MFPNumeric.ZERO, MFPNumeric.TWO);
            
            // ToDo if x = i or -i atan(x) has not result.
            DataClass datumTmp = evaluateMultiplication(datumOperand, datumI);
            DataClass datumTmp1 = evaluateAdding(datumOne, datumTmp);
            DataClass datumTmp2 = evaluateSubstraction(datumOne, datumTmp);
            datumTmp = evaluateDivision(datumTmp1, datumTmp2);
            datumTmp = evaluateLog(datumTmp);
            datumReturnNum = evaluateDivision(datumTmp, datumTwoI);
        }
        return datumReturnNum;
    }

    public static DataClass evaluatePower(DataClass datumBaseOperand, DataClass datumPowerOperand, DataClass datumNumOfRootsOperand) throws JFCALCExpErrException    {
		DataClass datumReturnNum = new DataClass();
		
		DataClass datumNumOfRoots = new DataClass();
		boolean bNotReturnList = false;
		if (datumNumOfRootsOperand == null)	{
			bNotReturnList = true;
			datumNumOfRoots.setDataValue(MFPNumeric.ONE);
		} else	{
			datumNumOfRoots.copyTypeValue(datumNumOfRootsOperand);
		}
		
		int nNumofReturnedRoots = 1;
		datumNumOfRoots.changeDataType(DATATYPES.DATUM_INTEGER);
		if (datumNumOfRoots.getDataValue().compareTo(MFPNumeric.ONE) < 0)	{
			throw new JFCALCExpErrException(ERRORTYPES.ERROR_POWER_SHOULD_RETURN_AT_LEAST_ONE_ROOT);
		} else if (datumNumOfRoots.getDataValue().longValue() > 0 && datumNumOfRoots.getDataValue().longValue() != Long.MAX_VALUE)	{
			nNumofReturnedRoots = (int)datumNumOfRoots.getDataValue().longValue();
		} else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);  // number of roots is invalid.
        }

		DataClass datumPower = new DataClass();
		datumPower.copyTypeValue(datumPowerOperand);

		boolean bImagePowerMode = false;
		datumPower.changeDataType(DATATYPES.DATUM_COMPLEX);
		if (!datumPower.getImage().isActuallyZero())	{
			// image power
			bImagePowerMode = true;
			if (nNumofReturnedRoots != 1)	{
				// if image power, only return one result.
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
			}
		} else {
			datumPower.changeDataType(DATATYPES.DATUM_DOUBLE);
		}

        if (datumBaseOperand.getDataType() == DATATYPES.DATUM_REF_DATA) {
            // integer power of a matrix (only support integer now)
            if (bImagePowerMode)    {
				throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW);
            } else  {
                DataClass datumPowerInt = new DataClass();
                datumPowerInt.copyTypeValue(datumPower);
                datumPowerInt.changeDataType(DATATYPES.DATUM_INTEGER);
                if (datumPower.isEqual(datumPowerInt) == false) {
    				throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW);               
                }
                int[] narraySize = datumBaseOperand.recalcDataArraySize();
                if (narraySize.length != 2 || (narraySize.length == 2 && narraySize[0] != narraySize[1]))    {
    				throw new JFCALCExpErrException(ERRORTYPES.ERROR_ONLY_SQUARE_MATRIX_SUPPORTED_BY_POWER);               
                }
                DataClass datumBase = new DataClass();
                datumBase.copyTypeValueDeep(datumBaseOperand);  // deep copy because it is a reference
                datumBase.populateDataArray(narraySize, false);
                datumReturnNum = createEyeMatrix(narraySize[0], 2);
                if (datumPowerInt.getDataValue().compareTo(MFPNumeric.ZERO) > 0)    {
                    DataClass datumIdx = new DataClass();
                    datumIdx.setDataValue(MFPNumeric.ZERO);
                    // have to identify if they are equal or not because 0.9999999999999999999 should be equal to 1
                    while (!MFPNumeric.isEqual(datumIdx.getDataValue(), datumPowerInt.getDataValue())
                            && datumIdx.getDataValue().compareTo(datumPowerInt.getDataValue()) < 0) {
                        datumReturnNum = evaluateMultiplication(datumReturnNum, datumBase);
                        datumIdx.setDataValue(datumIdx.getDataValue().add(MFPNumeric.ONE));
                    }
                } else  {
                    DataClass datumIdx = new DataClass();
                    datumIdx.setDataValue(MFPNumeric.ZERO);
                    // have to identify if they are equal or not because 0.9999999999999999999 should be equal to 1
                    while (!MFPNumeric.isEqual(datumIdx.getDataValue(), datumPowerInt.getDataValue())
                            && datumIdx.getDataValue().compareTo(datumPowerInt.getDataValue()) > 0) {
                        datumReturnNum = evaluateDivision(datumReturnNum, datumBase);
                        datumIdx.setDataValue(datumIdx.getDataValue().subtract(MFPNumeric.ONE));
                    }
                }
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClass();
                    datumReturnNum.setDataList(arrayRoots);
                }
            }
        } else  {
            // power of a complex number
            DataClass datumBase = new DataClass();
            datumBase.copyTypeValue(datumBaseOperand);
            datumBase.changeDataType(DATATYPES.DATUM_COMPLEX);
            MFPNumeric mfpNumRealBase = datumBase.getReal();
            MFPNumeric mfpNumImageBase = datumBase.getImage();
            if (datumPower.isEqual(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO)))	{
            	// x**0 is always 1.
                datumReturnNum.setDataValue(MFPNumeric.ONE);
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClass();
                    datumReturnNum.setDataList(arrayRoots);
                }
            } else if (mfpNumRealBase.isActuallyZero() && mfpNumImageBase.isActuallyZero())	{
                // if base is zero
                if (!datumPower.getImage().isActuallyZero()) {
                    datumReturnNum.setComplex(MFPNumeric.NAN, MFPNumeric.NAN);
                } else if (datumPower.getReal().isActuallyNegative()) {
                    datumReturnNum.setDataValue(MFPNumeric.INF);    // zero to a negative value should be infinite.
                } else {
                    datumReturnNum.setDataValue(MFPNumeric.ZERO);
                }
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClass();
                    datumReturnNum.setDataList(arrayRoots);
                }
            } else if (bImagePowerMode)	{
                MFPNumeric mfpNumRealLogBase = MFPNumeric.log(MFPNumeric.hypot(mfpNumRealBase, mfpNumImageBase));
                MFPNumeric mfpNumImageLogBase = MFPNumeric.atan2(mfpNumImageBase, mfpNumRealBase);
                MFPNumeric mfpNumRealPower = datumPower.getReal();
                MFPNumeric mfpNumImagePower = datumPower.getImage();
                MFPNumeric mfpNumRealPart = mfpNumRealLogBase.multiply(mfpNumRealPower).subtract(mfpNumImageLogBase.multiply(mfpNumImagePower));
                MFPNumeric mfpNumImagePart = mfpNumRealLogBase.multiply(mfpNumImagePower).add(mfpNumImageLogBase.multiply(mfpNumRealPower));
                MFPNumeric mfpNumCoeff = MFPNumeric.exp(mfpNumRealPart);
                datumReturnNum.setComplex(mfpNumCoeff.multiply(MFPNumeric.cos(mfpNumImagePart)), mfpNumCoeff.multiply(MFPNumeric.sin(mfpNumImagePart)));			
                if (bNotReturnList == false)    {   //number of returned root must be one..
                    DataClass[] arrayRoots = new DataClass[1];
                    arrayRoots[0] = datumReturnNum;
                    datumReturnNum = new DataClass();
                    datumReturnNum.setDataList(arrayRoots);
                }
            } else	{
                MFPNumeric mfpNum2ndParam = datumPower.getDataValue();
                MFPNumeric[] mfpNumListRadAngle = datumBase.getComplexRadAngle();
                if (mfpNumListRadAngle[0].isActuallyZero() && mfpNum2ndParam.isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_ANSWER_OF_POWER_FUNCTION_OPERANDS);
                }
                for (int index = 0; index < nNumofReturnedRoots; index++)	{
                    if ((new MFPNumeric(index)).multiply(mfpNum2ndParam).isActuallyInteger() && index != 0)	{
                        nNumofReturnedRoots = index;
                        break;	// have found all the roots
                    }
                    MFPNumeric[] mfpNumListRadAngleRoot = new MFPNumeric[2];
                    mfpNumListRadAngleRoot[0] = MFPNumeric.pow(mfpNumListRadAngle[0], mfpNum2ndParam);
                    if (mfpNumListRadAngle[1].isActuallyZero() && index == 0)   {
                        // base is a positive real number, and we calculate its first root
                        // this avoid to multiply INF and get NAN if mfpNum2ndParam is inf.
                        mfpNumListRadAngleRoot[1] = MFPNumeric.ZERO;
                    } else {
                        mfpNumListRadAngleRoot[1] = mfpNumListRadAngle[1].add(new MFPNumeric(2 * index).multiply(MFPNumeric.PI)).multiply(mfpNum2ndParam);
                    }
                    
                    boolean bImageIsZero = false;
                    boolean bRealIsZero = false;
                    MFPNumeric mfpNumOverPI = MFPNumeric.divide(mfpNumListRadAngleRoot[1], MFPNumeric.PI);
                    if (mfpNumOverPI.isActuallyInteger())    {
                        bImageIsZero = true;
                    }
                    
                    MFPNumeric mfpNumOverHalfPI = MFPNumeric.divide(mfpNumListRadAngleRoot[1], MFPNumeric.PI_OVER_TWO);
                    if (mfpNumOverHalfPI.isActuallyInteger() && !bImageIsZero)    { // if it is integer times of 1/2 PI but not integer times of PI
                        bRealIsZero = true;
                    }
                    
                    if (nNumofReturnedRoots == 1 && bNotReturnList)	{	//return a single root
                        datumReturnNum.setComplexRadAngle(mfpNumListRadAngleRoot[0], mfpNumListRadAngleRoot[1]);
                        if (bImageIsZero)	{
                            datumReturnNum.setImage(MFPNumeric.ZERO);
                        } else if (bRealIsZero) {
                            datumReturnNum.setReal(MFPNumeric.ZERO);
                        } else	{
                            if (MFPNumeric.isEqual(datumReturnNum.getImage(), MFPNumeric.ZERO))	{
                                datumReturnNum.changeDataType(DATATYPES.DATUM_DOUBLE);
                            }
                        }
                        break;
                    } else	{	// three parameters, return a root list
                        DataClass datumRoot = new DataClass();
                        datumRoot.setComplexRadAngle(mfpNumListRadAngleRoot[0], mfpNumListRadAngleRoot[1]);
                        if (bImageIsZero)	{
                            datumRoot.setImage(MFPNumeric.ZERO);
                        } else if (bRealIsZero) {
                            datumRoot.setReal(MFPNumeric.ZERO);
                        } else	{
                            if (datumRoot.getImage().isActuallyZero())	{
                                datumRoot.changeDataType(DATATYPES.DATUM_DOUBLE);
                            }
                        }
                        int[] nRootIndices = new int[1];
                        nRootIndices[0] = index;
                        DataClass datumZero = new DataClass();
                        datumZero.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                        datumReturnNum.createDataAtIndex(nRootIndices, datumRoot, datumZero);
                    }
                }
            }
        }
		return datumReturnNum;
    }
    
    public static DataClass createEyeMatrix(int nSize, int nDim) throws JFCALCExpErrException {
        DataClass datumReturn = new DataClass();
        if (nSize == 0)	{
        	// if size is 0, whatever nDim is, return 1
        	datumReturn.setDataValue(MFPNumeric.ONE, DATATYPES.DATUM_DOUBLE);
        } else	{
	        int[] narrayDims = new int[nDim];
	        for (int idx = 0; idx < nDim; idx ++)   {
	            narrayDims[idx] = nSize;
	        }
	        datumReturn.populateDataArray(narrayDims, false);
	        for (int idxSize = 0; idxSize < nSize; idxSize ++)   {
	            DataClass datumTmp = datumReturn;
	            for (int idxDim = 0; idxDim < nDim; idxDim ++)  {
	                datumTmp = datumTmp.getDataList()[idxSize];
	            }
	            datumTmp.setDataValue(MFPNumeric.ONE, DATATYPES.DATUM_DOUBLE);
	        }
        }
        return datumReturn;
    }

    public static DataClass createUniValueMatrix(int[] nlistSizes, DataClass datumUniValue) throws JFCALCExpErrException {
        // assume all nlistSizes members are positive integer.
        DataClass datumReturn = new DataClass();
    	if (nlistSizes == null || nlistSizes.length == 0)	{
    		// return a single value
    		datumReturn.copyTypeValueDeep(datumUniValue);
    	} else	{
	    	// here we have checked the size of return value and it is an array.
	        datumReturn.populateDataArray(nlistSizes, false);
	        int[] nlistSizesChild = new int[nlistSizes.length - 1];
	        for (int idx = 0; idx < nlistSizes.length - 1; idx ++)  {
	            nlistSizesChild[idx] = nlistSizes[idx + 1];
	        }
	        for (int idxSize = 0; idxSize < nlistSizes[0]; idxSize ++)   {
	            if (nlistSizes.length > 1)   {
	                datumReturn.getDataList()[idxSize] = createUniValueMatrix(nlistSizesChild, datumUniValue);
	            } else  {
	                DataClass datumRetValue = new DataClass();
	                datumRetValue.copyTypeValueDeep(datumUniValue);
	                datumReturn.getDataList()[idxSize] = datumRetValue;
	            }
	        }
    	}
        return datumReturn;
    }
    
    public static boolean includesAbnormalValues(DataClass datum, int nSearchMode) throws JFCALCExpErrException {
        // nSearchMode == 1 means search for NULL
        // nSearchMode == 2 means search for NAN
        // nSearchMode == 4 means search for +INF
        // nSearchMode == 8 means search for -INF
        boolean bSearchNULL = (nSearchMode & 1) == 1;
        boolean bSearchNan = (nSearchMode & 2) == 2;
        boolean bSearchPosInf = (nSearchMode & 4) == 4;
        boolean bSearchNegInf = (nSearchMode & 8) == 8;
        
        if (!(bSearchNULL || bSearchNan || bSearchPosInf || bSearchNegInf)) {
            // nothing to look for
            return true;
        }
        boolean bReturn = false;
        if (datum.getDataType() == DATATYPES.DATUM_COMPLEX || datum.getDataType() == DATATYPES.DATUM_REF_DATA) {
            DataClass[] datumList = datum.getDataList();
            if (datumList != null)    {
                for (int idx = 0; idx < datumList.length; idx ++)    {
                    bReturn |= includesAbnormalValues(datumList[idx], nSearchMode);
                    if (bReturn)    {
                        break;  // find!
                    }
                }
            }
        } else if (datum.getDataType() == DATATYPES.DATUM_DOUBLE || datum.getDataType() == DATATYPES.DATUM_INTEGER) {
            if (bSearchNan && datum.getDataValue().isNan()) {
                bReturn = true;
            }
            if (bSearchPosInf && datum.getDataValue().isPosInf()) {
                bReturn = true;
            }
            if (bSearchNegInf && datum.getDataValue().isNegInf()) {
                bReturn = true;
            }
        } else if (datum.getDataType() == DATATYPES.DATUM_NULL && bSearchNULL) {
            bReturn = true;
        }
        return bReturn;
    }
}
    
