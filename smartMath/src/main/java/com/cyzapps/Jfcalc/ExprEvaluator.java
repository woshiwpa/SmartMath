package com.cyzapps.Jfcalc;

import java.util.*;

import com.cyzapps.Jfcalc.BaseData.BoundOperator;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ErrProcessor.*;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEAssign;
import com.cyzapps.Jsma.AEBitwiseOpt;
import com.cyzapps.Jsma.AECompare;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEIndex;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AELeftDivOpt;
import com.cyzapps.Jsma.AEMulDivOpt;
import com.cyzapps.Jsma.AEOnOffUnaryOpt;
import com.cyzapps.Jsma.AEPosNegOpt;
import com.cyzapps.Jsma.AEPowerOpt;
import com.cyzapps.Jsma.AEUnaryOpt;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExprEvaluator    {

    public LinkedList<LinkedList<Variable>> m_lVarNameSpaces;

    public ExprEvaluator() {
        m_lVarNameSpaces = new LinkedList<LinkedList<Variable>>();
    }

    public ExprEvaluator(LinkedList<LinkedList<Variable>> lVarNameSpaces) {
        m_lVarNameSpaces = lVarNameSpaces; // initialize m_lVarNameSpace
    }

    public static DataClass evaluateTwoOperandCell(DataClass datumFirstOperand,
            CalculateOperator COPTROperator, DataClass datumSecondOperand) throws JFCALCExpErrException {

        DataClass datumReturn = new DataClass();
        
        if (datumFirstOperand.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR
                && datumSecondOperand.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
            switch (COPTROperator.getOperatorType()) {
            case OPERATOR_ASSIGN:
                datumFirstOperand.copyTypeValue(datumSecondOperand);    // not deep copy
                datumReturn.copyTypeValue(datumSecondOperand);    // not deep copy
                break;
            case OPERATOR_EQ:
                if (datumFirstOperand.isEqual(datumSecondOperand)) {
                    datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                } else {
                    datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                }
                break;
            case OPERATOR_NEQ:
                if (datumFirstOperand.isEqual(datumSecondOperand)) {
                    datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                } else {
                    datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                }
                break;
            case OPERATOR_LARGER:
                DataClass datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                DataClass datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                } else  {            
                    datum1stOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    datum2ndOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    if (datum1stOperand.getDataValue().compareTo(datum2ndOperand.getDataValue()) > 0) {
                        datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                    } else {
                        datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                    }
                }
                break;
            case OPERATOR_SMALLER:
                datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                } else  {            
                    datum1stOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    datum2ndOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    if (datum1stOperand.getDataValue().compareTo(datum2ndOperand.getDataValue()) < 0) {
                        datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                    } else {
                        datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                    }
                }
                break;
            case OPERATOR_NOLARGER:
                datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                } else  {
                    datum1stOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    datum2ndOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    if (datum1stOperand.getDataValue().compareTo(datum2ndOperand.getDataValue()) <= 0) {
                        datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                    } else {
                        datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                    }
                }
                break;
            case OPERATOR_NOSMALLER:
                datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                if (datum1stOperand.isEqual(datum2ndOperand))   {
                    datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                } else  {
                    datum1stOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    datum2ndOperand.changeDataType(DATATYPES.DATUM_DOUBLE);
                    if (datum1stOperand.getDataValue().compareTo(datum2ndOperand.getDataValue()) >= 0) {
                        datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                    } else {
                        datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                    }
                }
                break;
            case OPERATOR_ADD:
                if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA
                    && datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    DataClass datum1stOperandCpy = new DataClass();
                    datum1stOperandCpy.copyTypeValueDeep(datumFirstOperand);    // need deep copy because populatedataarray change array elements
                    datumFirstOperand = datum1stOperandCpy;
                    datumFirstOperand.populateDataArray(narrayFirstDims, false);
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    DataClass datum2ndOperandCpy = new DataClass();
                    datum2ndOperandCpy.copyTypeValueDeep(datumSecondOperand);    // need deep copy because populatedataarray change array elements
                    datumSecondOperand = datum2ndOperandCpy;
                    datumSecondOperand.populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateAdding(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_SUBTRACT:
                if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA
                    && datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    DataClass datum1stOperandCpy = new DataClass();
                    datum1stOperandCpy.copyTypeValueDeep(datumFirstOperand);    // need deep copy because populatedataarray change array elements
                    datumFirstOperand = datum1stOperandCpy;
                    datumFirstOperand.populateDataArray(narrayFirstDims, false);
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    DataClass datum2ndOperandCpy = new DataClass();
                    datum2ndOperandCpy.copyTypeValueDeep(datumSecondOperand);    // need deep copy because populatedataarray change array elements
                    datumSecondOperand = datum2ndOperandCpy;
                    datumSecondOperand.populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateSubstraction(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_MULTIPLY:
                if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    DataClass datum1stOperandCpy = new DataClass();
                    datum1stOperandCpy.copyTypeValueDeep(datumFirstOperand);    // need deep copy because populatedataarray change array elements
                    datumFirstOperand = datum1stOperandCpy;
                    datumFirstOperand.populateDataArray(narrayFirstDims, false);
                }
                if (datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    DataClass datum2ndOperandCpy = new DataClass();
                    datum2ndOperandCpy.copyTypeValueDeep(datumSecondOperand);    // need deep copy because populatedataarray change array elements
                    datumSecondOperand = datum2ndOperandCpy;
                    datumSecondOperand.populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateMultiplication(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_DIVIDE:
                if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    DataClass datum1stOperandCpy = new DataClass();
                    datum1stOperandCpy.copyTypeValueDeep(datumFirstOperand);    // need deep copy because populatedataarray change array elements
                    datumFirstOperand = datum1stOperandCpy;
                    datumFirstOperand.populateDataArray(narrayFirstDims, false);
                }
                if (datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    DataClass datum2ndOperandCpy = new DataClass();
                    datum2ndOperandCpy.copyTypeValueDeep(datumSecondOperand);    // need deep copy because populatedataarray change array elements
                    datumSecondOperand = datum2ndOperandCpy;
                    datumSecondOperand.populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateDivision(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_LEFTDIVIDE:
                if (datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narrayFirstDims = datumFirstOperand.recalcDataArraySize();
                    DataClass datum1stOperandCpy = new DataClass();
                    datum1stOperandCpy.copyTypeValueDeep(datumFirstOperand);    // need deep copy because populatedataarray change array elements
                    datumFirstOperand = datum1stOperandCpy;
                    datumFirstOperand.populateDataArray(narrayFirstDims, false);
                }
                if (datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    int[] narraySecondDims = datumSecondOperand.recalcDataArraySize();
                    DataClass datum2ndOperandCpy = new DataClass();
                    datum2ndOperandCpy.copyTypeValueDeep(datumSecondOperand);    // need deep copy because populatedataarray change array elements
                    datumSecondOperand = datum2ndOperandCpy;
                    datumSecondOperand.populateDataArray(narraySecondDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateLeftDivision(datumFirstOperand, datumSecondOperand);
                break;
            case OPERATOR_AND:
                DATATYPES enumDataType = DATATYPES.DATUM_BOOLEAN;
                if (datumFirstOperand.getDataType() != DATATYPES.DATUM_BOOLEAN
                        || datumSecondOperand.getDataType() != DATATYPES.DATUM_BOOLEAN)    {
                    enumDataType = DATATYPES.DATUM_INTEGER;
                }

                datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                datum1stOperand.changeDataType(DATATYPES.DATUM_INTEGER);
                datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                datum2ndOperand.changeDataType(DATATYPES.DATUM_INTEGER);

                if (datum1stOperand.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                if (datum2ndOperand.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                datumReturn.setDataClass(enumDataType,  // we can use toBigInteger directly because 1st and 2nd operands have been converted to MFP_INTEGER_TYPE
                        new MFPNumeric(datum1stOperand.getDataValue().toBigInteger().and(datum2ndOperand.getDataValue().toBigInteger())),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                break;
            case OPERATOR_OR:
                enumDataType = DATATYPES.DATUM_BOOLEAN;
                if (datumFirstOperand.getDataType() != DATATYPES.DATUM_BOOLEAN
                        || datumSecondOperand.getDataType() != DATATYPES.DATUM_BOOLEAN)    {
                    enumDataType = DATATYPES.DATUM_INTEGER;
                }

                datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                datum1stOperand.changeDataType(DATATYPES.DATUM_INTEGER);
                datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                datum2ndOperand.changeDataType(DATATYPES.DATUM_INTEGER);

                if (datum1stOperand.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                if (datum2ndOperand.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                datumReturn.setDataClass(enumDataType,  // we can use toBigInteger directly because 1st and 2nd operands have been converted to MFP_INTEGER_TYPE
                        new MFPNumeric(datum1stOperand.getDataValue().toBigInteger().or(datum2ndOperand.getDataValue().toBigInteger())),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                break;
            case OPERATOR_XOR:
                enumDataType = DATATYPES.DATUM_BOOLEAN;
                if (datumFirstOperand.getDataType() != DATATYPES.DATUM_BOOLEAN
                        || datumSecondOperand.getDataType() != DATATYPES.DATUM_BOOLEAN)    {
                    enumDataType = DATATYPES.DATUM_INTEGER;
                }

                datum1stOperand = new DataClass();
                datum1stOperand.copyTypeValue(datumFirstOperand);
                datum1stOperand.changeDataType(DATATYPES.DATUM_INTEGER);
                datum2ndOperand = new DataClass();
                datum2ndOperand.copyTypeValue(datumSecondOperand);
                datum2ndOperand.changeDataType(DATATYPES.DATUM_INTEGER);

                if (datum1stOperand.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                if (datum2ndOperand.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                datumReturn.setDataClass(enumDataType,  // we can use toBigInteger directly because 1st and 2nd operands have been converted to MFP_INTEGER_TYPE
                        new MFPNumeric(datum1stOperand.getDataValue().toBigInteger().xor(datum2ndOperand.getDataValue().toBigInteger())),
                        "", "", new DataClass[0], AEInvalid.AEINVALID, "");
                break;
            case OPERATOR_POWER:
                // operator power only returns one root of the operand. To return all the roots, use pow function.
                // note that parameter values will not be changed in evaluatePower function so no copytypevalue
                // needed.
                datumReturn = BuiltinProcedures.evaluatePower(datumFirstOperand, datumSecondOperand, null);            
                break;
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_BINARY_OPERATOR);
            }
        } else {
            try {
                AbstractExpr aexpr1stOperand = (datumFirstOperand.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR)?
                                        datumFirstOperand.getAExpr()
                                        :new AEConst(datumFirstOperand.getDataType() == DATATYPES.DATUM_REF_DATA?
                                            datumFirstOperand:datumFirstOperand.cloneSelf());
                AbstractExpr aexpr2ndOperand = (datumSecondOperand.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR)?
                                        datumSecondOperand.getAExpr()
                                        :new AEConst(datumSecondOperand.getDataType() == DATATYPES.DATUM_REF_DATA?
                                            datumSecondOperand:datumSecondOperand.cloneSelf());
                switch (COPTROperator.getOperatorType()) {
                case OPERATOR_ASSIGN: {
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr1stOperand);
                    listAExprs.add(aexpr2ndOperand);
                    datumReturn.setAExpr(new AEAssign(listAExprs));
                    break;
                } case OPERATOR_EQ:
                case OPERATOR_NEQ:
                case OPERATOR_LARGER:
                case OPERATOR_SMALLER:
                case OPERATOR_NOLARGER:
                case OPERATOR_NOSMALLER: {
                    datumReturn.setAExpr(new AECompare(aexpr1stOperand, COPTROperator, aexpr2ndOperand));
                    break;
                } case OPERATOR_ADD:
                case OPERATOR_SUBTRACT: {
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
                    listOpts.add(COPTROperator);
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr1stOperand);
                    listAExprs.add(aexpr2ndOperand);
                    datumReturn.setAExpr(new AEPosNegOpt(listAExprs, listOpts));
                    break;
                } case OPERATOR_MULTIPLY:
                case OPERATOR_DIVIDE: {
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                    listOpts.add(COPTROperator);
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr1stOperand);
                    listAExprs.add(aexpr2ndOperand);
                    datumReturn.setAExpr(new AEMulDivOpt(listAExprs, listOpts));
                    break;
                } case OPERATOR_LEFTDIVIDE: {
                    datumReturn.setAExpr(new AELeftDivOpt(aexpr1stOperand, aexpr2ndOperand));
                    break;
                } case OPERATOR_AND:
                case OPERATOR_OR:
                case OPERATOR_XOR: {
                    datumReturn.setAExpr(new AEBitwiseOpt(aexpr1stOperand, COPTROperator, aexpr2ndOperand));
                    break;
                } case OPERATOR_POWER: {
                    datumReturn.setAExpr(new AEPowerOpt(aexpr1stOperand, aexpr2ndOperand));            
                    break;
                } default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_BINARY_OPERATOR);
                }
            } catch (JSmartMathErrException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
        }
        return datumReturn;
    }

    public static DataClass evaluateOneOperandCell(DataClass datumOperand,
            CalculateOperator COPTROperator) throws JFCALCExpErrException {
        OPERATORTYPES enumOperatorType = COPTROperator.getOperatorType();
        DataClass datumReturn = new DataClass();
        if (datumOperand.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
            switch (enumOperatorType) {
            case OPERATOR_PERCENT: {
                // % is always treated as divided by 100. Note that if it is matrix, it has to be fully populated first.
                DataClass datumOprndCpy = new DataClass();
                datumOprndCpy.copyTypeValueDeep(datumOperand);
                if (datumOprndCpy.getDataType() == DATATYPES.DATUM_REF_DATA) {
                    int[] narrayDims = datumOprndCpy.recalcDataArraySize();
                    datumOprndCpy.populateDataArray(narrayDims, false);
                }
                datumReturn = BuiltinProcedures.evaluateDivision(datumOprndCpy, new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(100)));
                break;
            }
            case OPERATOR_FACTORIAL: {
                if (datumOperand.getDataType() != DATATYPES.DATUM_DOUBLE
                        && datumOperand.getDataType() != DATATYPES.DATUM_INTEGER
                        && datumOperand.getDataType() != DATATYPES.DATUM_BOOLEAN)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_OPERAND_TYPE);
                }
                MFPNumeric mfpNumValue = datumOperand.getDataValue();
                if (mfpNumValue.isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_FACTORIAL_CAN_NOT_LESS_THAN_ZERO);
                }
                // need not to worry about too large factorial because MFPNumeric support any large value.
                datumOperand.changeDataType(DATATYPES.DATUM_INTEGER);
                if (datumOperand.getDataValue().isActuallyZero())
                    mfpNumValue = MFPNumeric.ONE;
                else {
                    MFPNumeric mfpNumMultiplyNum = mfpNumValue;
                    mfpNumValue = MFPNumeric.ONE;
                    for (MFPNumeric i = MFPNumeric.ONE; i.compareTo(mfpNumMultiplyNum) <= 0; i = i.add(MFPNumeric.ONE)) {
                        mfpNumValue = mfpNumValue.multiply(i);
                    }
                }
                datumReturn.setDataValue(mfpNumValue, DATATYPES.DATUM_INTEGER);    // need not to worry about integer overflow
                                                                                // now coz there will be auto-conversion
                                                                                // later on.
                break;
            }
            case OPERATOR_TRANSPOSE:    {
                int[] narrayDims = null;
                if (datumOperand.getDataType() == DATATYPES.DATUM_REF_DATA)    {
                    narrayDims = datumOperand.recalcDataArraySize();
                    DataClass datumOperandCpy = new DataClass();
                    datumOperandCpy.copyTypeValueDeep(datumOperand);    // need deep copy here coz PopulateDataArray will change array content
                    datumOperandCpy.populateDataArray(narrayDims, false);
                    datumOperand = datumOperandCpy;
                }
                datumReturn = BuiltinProcedures.evaluateTransposition(datumOperand, narrayDims);
                break;
            }
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
            }
        } else {
            try {
                // it is an abstractexpr
                AbstractExpr aexpr = datumOperand.getAExpr();
                switch (enumOperatorType) {
                case OPERATOR_PERCENT:
                case OPERATOR_FACTORIAL:    {
                    datumReturn.setAExpr(new AEUnaryOpt(COPTROperator, aexpr));
                    break;
                }
                case OPERATOR_TRANSPOSE:    {
                    datumReturn.setAExpr(new AEOnOffUnaryOpt(COPTROperator, aexpr, 1));
                    break;
                }
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
                }
            } catch (JSmartMathErrException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
        }
        return datumReturn;
    }

    public static DataClass evaluateOneOperandCell(CalculateOperator COPTROperator,
            DataClass datumOperand) throws JFCALCExpErrException {
        OPERATORTYPES enumOperatorType = COPTROperator.getOperatorType();
        DataClass datumReturn = new DataClass();

        if (datumOperand.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
            switch (enumOperatorType) {
            case OPERATOR_FALSE:
                datumReturn.copyTypeValue(datumOperand);
                datumReturn.changeDataType(DATATYPES.DATUM_BOOLEAN);
                if (datumReturn.getDataValue().isActuallyZero()) /* boolean : FALSE */
                    datumReturn.setDataValue(MFPNumeric.TRUE, DATATYPES.DATUM_BOOLEAN);
                else
                    datumReturn.setDataValue(MFPNumeric.FALSE, DATATYPES.DATUM_BOOLEAN);
                break;
            case OPERATOR_NOT:
                DATATYPES enumDataType = DATATYPES.DATUM_BOOLEAN;
                if (datumOperand.getDataType() != DATATYPES.DATUM_BOOLEAN)    {
                    enumDataType = DATATYPES.DATUM_INTEGER;
                }

                datumReturn.copyTypeValue(datumOperand);
                datumOperand.changeDataType(DATATYPES.DATUM_INTEGER);
                if (datumReturn.getDataValue().isActuallyNegative()) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO);
                }
                // datum return has been converted to MFP_INTEGER_TYPE so that we can use toBigInteger() directly.
                datumReturn.setDataValue(new MFPNumeric(datumReturn.getDataValue().toBigInteger().not()), enumDataType);
                break;
            case OPERATOR_POSSIGN:
                // if data type does not match, the following two statements
                // will throw exceptions.
                switch (datumOperand.getDataType()) {
                case DATUM_BOOLEAN:
                    datumReturn.copyTypeValue(datumOperand);
                    datumReturn.changeDataType(DATATYPES.DATUM_INTEGER);
                    break;
                case DATUM_INTEGER:
                case DATUM_DOUBLE:
                case DATUM_COMPLEX:
                    datumReturn.copyTypeValue(datumOperand);
                    break;
                case DATUM_REF_DATA:
                    if (!datumOperand.isNumericalData(true))    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
                    }
                    datumReturn.copyTypeValue(datumOperand);
                    break;
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
                }
                break;
            case OPERATOR_NEGSIGN:
                datumReturn = BuiltinProcedures.evaluateNegSign(datumOperand);
                break;
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
            }
        } else {
            try {
                // it is an abstractexpr
                AbstractExpr aexpr = datumOperand.getAExpr();
                switch (enumOperatorType) {
                case OPERATOR_FALSE:
                case OPERATOR_NOT:
                    datumReturn.setAExpr(new AEOnOffUnaryOpt(COPTROperator, aexpr, 1));
                    break;
                case OPERATOR_POSSIGN:
                    datumReturn = datumOperand.cloneSelf();
                    break;
                case OPERATOR_NEGSIGN:
                    LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                    listOpts.add(COPTROperator);
                    LinkedList<AbstractExpr> listAExprs = new LinkedList<AbstractExpr>();
                    listAExprs.add(aexpr);
                    AbstractExpr aexprReturn = new AEPosNegOpt(listAExprs, listOpts);
                    datumReturn = new DataClass(aexprReturn);
                    break;
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
                }
            } catch (JSmartMathErrException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
        }
        return datumReturn;
    }

    public static DataClass evaluateIndex(DataClass datumToBeIndex, DataClass datumIndex) throws JFCALCExpErrException    {
        
        if (datumToBeIndex.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR
                && datumIndex.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
            DataClass datumIdx = new DataClass();
            datumIdx.copyTypeValueDeep(datumIndex);
            int[] nIndices = new int[datumIdx.getDataListSize()];
            for (int idx = 0; idx < datumIdx.getDataListSize(); idx ++)    {
                datumIdx.getDataList()[idx].changeDataType(DATATYPES.DATUM_INTEGER);
                nIndices[idx] = (int)datumIdx.getDataList()[idx].getDataValue().longValue();    // index can be integer. don't worry its overflow
            }
            /*
             * We do not use datumReturnNum = getDataAtIndex(indexArray)
             * (which includes a deep copy of the returned data at index)
             * here because when we assign a new value to a data class,
             * we need the data class reference.
             */
            DataClass datumReturnNum = datumToBeIndex.getDataAtIndexByRef(nIndices);
            return datumReturnNum;
        } else {    // at least one of the parameters is AbstractExpr.
            AbstractExpr aeReturn = AEInvalid.AEINVALID;
            try {
                AbstractExpr aexpr2BeIndex = (datumToBeIndex.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR)?
                                        datumToBeIndex.getAExpr():new AEConst(datumToBeIndex);
                AbstractExpr aexprIndex = (datumIndex.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR)?
                                        datumIndex.getAExpr():new AEConst(datumIndex.cloneSelf());
                aeReturn = new AEIndex(aexpr2BeIndex, aexprIndex);
            } catch (JSmartMathErrException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_GET_RESULT);    // will not arrive here.
            }
            return new DataClass(aeReturn);
        }
    }
    
    public DataClass evaluateExpression(String strExpression, CurPos curpos) throws JFCALCExpErrException, InterruptedException {
        LinkedList<DataClass> listValues = new LinkedList<DataClass>();
        LinkedList<CalculateOperator> listCalcOpts = new LinkedList<CalculateOperator>();

        BoundOperator BOPTRCurboptr;
        CalculateOperator COPTRStackTopcoptr;
        CalculateOperator COPTRCurcoptr = new CalculateOperator();
        DataClass datumCurNum = new DataClass();

        int nLastPushedCellType = 0; /* 1 for number, 2 for calculation operator */
        int nRecursionStartPosition = curpos.m_nPos;

        while (curpos.m_nPos < strExpression.length()) {

            /* A bound char */
            if (ElemAnalyzer.isBoundOperatorChar(strExpression, curpos.m_nPos)) {
                BOPTRCurboptr = ElemAnalyzer.getBoundOperator(strExpression, curpos);
                // need not to worry about OPERATOR_NOTEXIST because it has been handled
                // in GetBoundOperator
                if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_LEFTPARENTHESE)) {
                    datumCurNum = evaluateExpression(strExpression, curpos);

                    if (nLastPushedCellType == 1) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                    }

                    listValues.addFirst(datumCurNum);
                    nLastPushedCellType = 1;
                }

                else if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_RIGHTPARENTHESE)) {
                    if (nRecursionStartPosition == 0) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_RIGHTPARENTHESE);
                    }

                    if ((listCalcOpts.size() == 0) && (listValues.size() == 0)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                    }

                    if (nLastPushedCellType != 1) {
                        throw new JFCALCExpErrException(
                                ERRORTYPES.ERROR_LACK_OPERAND);
                    }

                    datumCurNum = listValues.poll();
                    COPTRStackTopcoptr = listCalcOpts.poll();
                    while (COPTRStackTopcoptr != null) {
                        if (COPTRStackTopcoptr.getOperandNum() == 1) {
                            if (datumCurNum == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            datumCurNum = evaluateOneOperandCell(COPTRStackTopcoptr, datumCurNum);
                        } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                            DataClass datumFirstOperand;
                            datumFirstOperand = listValues.poll();
                            if (datumFirstOperand == null || datumCurNum == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            datumCurNum = evaluateTwoOperandCell(
                                    datumFirstOperand, COPTRStackTopcoptr,
                                    datumCurNum);
                        }
                        /* need not to worry about non exist data return now because an exception will be thrown
                        if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                            return datumCurNum;*/

                        COPTRStackTopcoptr = listCalcOpts.poll();
                    }
                    return datumCurNum;
                } else {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERATOR_NOT_EXIST);
                }

            }

            /* an operator char */
            else if (ElemAnalyzer.isCalcOperatorChar(strExpression,
                    curpos.m_nPos)) {
                while ((strExpression.length() > curpos.m_nPos)
                        && (ElemAnalyzer.isCalcOperatorChar(strExpression, curpos.m_nPos)
                                || ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))) {
                    if (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))
                        curpos.m_nPos++;
                    else {
                        COPTRCurcoptr = ElemAnalyzer.getCalcOperator(strExpression, curpos, nLastPushedCellType);
                        // need not worry about OPERATOR_NOTEXIST here because OPERATOR_NOTEXIST has been handled
                        // in elemAnalyzer.GetCalcOperator.
                        if (COPTRCurcoptr.getLabelPrefix()) {
                            if (COPTRCurcoptr.getOperandNum() == 2
                                    && nLastPushedCellType != 1) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            if (COPTRCurcoptr.getOperandNum() == 1
                                    && nLastPushedCellType == 1) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            break;
                        } else {
                            datumCurNum = listValues.poll();
                            if (nLastPushedCellType != 1 || datumCurNum == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            datumCurNum = evaluateOneOperandCell(datumCurNum, COPTRCurcoptr);
                            /* need not to worry about non-exist data because it is handled
                             * in EvaluateOneOperandCell
                            if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                                return datumCurNum;*/
                            listValues.addFirst(datumCurNum);
                            nLastPushedCellType = 1;
                        }
                    }
                }

                if (COPTRCurcoptr.getLabelPrefix()) {
                    if (COPTRCurcoptr.getOperandNum() == 2) {
                        boolean bHaveBOptHigherLevelLeft = false;
                        for (int idx = 0; idx < listCalcOpts.size(); idx ++) {
                            if (listCalcOpts.get(idx).getOperandNum() == 2) {
                                if (!ElemAnalyzer.is2ndOPTRHaveHighLevel(listCalcOpts.get(idx), COPTRCurcoptr)) {
                                    bHaveBOptHigherLevelLeft = true;
                                }
                                break;
                            }
                        }
                        datumCurNum = listValues.poll();
                        COPTRStackTopcoptr = listCalcOpts.poll();

                        while (COPTRStackTopcoptr != null
                                && ((bHaveBOptHigherLevelLeft && COPTRStackTopcoptr.getOperandNum() == 1)
                                    || !ElemAnalyzer.is2ndOPTRHaveHighLevel(COPTRStackTopcoptr, COPTRCurcoptr))) {
                            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                                if (datumCurNum == null)    {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                                }
                                datumCurNum = evaluateOneOperandCell(COPTRStackTopcoptr, datumCurNum);
                            } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                                DataClass datumFirstOperand = listValues.poll();
                                if (datumFirstOperand == null || datumCurNum == null) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                                }
                                datumCurNum = evaluateTwoOperandCell(datumFirstOperand, COPTRStackTopcoptr, datumCurNum);
                            }
                            /* need not to worry about non-exist data because it is handled in
                             * EvaluateOneOperandCell
                            if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                                return datumCurNum;*/

                            COPTRStackTopcoptr = listCalcOpts.poll();
                        }
                        if (COPTRStackTopcoptr != null) {
                            listCalcOpts.addFirst(COPTRStackTopcoptr);
                        }
                        listValues.addFirst(datumCurNum);
                    }

                    listCalcOpts.addFirst(COPTRCurcoptr);
                    nLastPushedCellType = 2;
                }
            }

            /* The new cell seems to be a number */
            else if (ElemAnalyzer.isStartNumberChar(strExpression, curpos.m_nPos)) {
                datumCurNum = ElemAnalyzer.getNumber(strExpression, curpos);
                /* we need not to worry about Not exist data here because error has been handled before
                 * if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return datumCurNum;*/
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }

                listValues.addFirst(datumCurNum);
                nLastPushedCellType = 1;
            }

            /* The new cell seems to be a data reference */
            else if (ElemAnalyzer.isDataRefChar(strExpression, curpos.m_nPos)) {
                datumCurNum = ElemAnalyzer.getDataRef(strExpression, curpos, m_lVarNameSpaces);

                if (nLastPushedCellType == 1)    {    // last time a value was pushed, not an operator, this data reference should be an index.
                    DataClass datumToBeIndexed = listValues.poll();
                    if (datumToBeIndexed == null || datumCurNum == null)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                    }
                    datumCurNum = evaluateIndex(datumToBeIndexed, datumCurNum);
                    
                }
                listValues.addFirst(datumCurNum);
                nLastPushedCellType = 1;
            }

            /*
             * The new cell seems to be the beginning of a variable or
             * function's name
             */
            else if (ElemAnalyzer.isNameChar(strExpression, curpos.m_nPos) == 1) {
                datumCurNum = ElemAnalyzer.getExprName(strExpression, curpos,
                        m_lVarNameSpaces);
                /* A function might return NULL point. A NULL point is DATATYPES.DATUM_NOTEXIST
                if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return datumCurNum;*/
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }

                listValues.addFirst(datumCurNum);
                nLastPushedCellType = 1;
            }

            /* A blank char */
            else if (ElemAnalyzer.isBlankChar(strExpression, curpos.m_nPos))
                curpos.m_nPos++;
            /* A "," */
            else if (strExpression.charAt(curpos.m_nPos) == ',') {
                /*
                 * we do not add curpos.m_nPos++; here because curpos stops at
                 * the first char which does not belong to the expression.
                 */
                break; /* ignore part of the expression after "," */
            }
            else if (ElemAnalyzer.isStringStartChar(strExpression, curpos.m_nPos))    {
                datumCurNum = ElemAnalyzer.getString(strExpression, curpos);
                listValues.addFirst(datumCurNum);
                nLastPushedCellType = 1;
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_IDENTIFIED_CHARACTER);
            }

        }
        /* arriving here means we are in the top level of expression */
        if (nRecursionStartPosition != 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
        }

        if ((listCalcOpts.size() == 0) && (listValues.size() == 0)) {
            if (curpos.m_nPos == 0)
                curpos.m_nPos = 1;
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
        }

        if (nLastPushedCellType != 1) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
        }

        datumCurNum = listValues.poll();
        COPTRStackTopcoptr = listCalcOpts.poll();
        while (COPTRStackTopcoptr != null) {
            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                if (datumCurNum == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                }
                datumCurNum = evaluateOneOperandCell(COPTRStackTopcoptr, datumCurNum);
            } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                DataClass datumFirstOperand;
                datumFirstOperand = listValues.poll();
                if (datumFirstOperand == null || datumCurNum == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                }
                datumCurNum = evaluateTwoOperandCell(datumFirstOperand, COPTRStackTopcoptr, datumCurNum);
            }
            /* need not to worry about non-exist data because it is handled in
             * EvaluateOneOperandCell
            if (datumCurNum.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                return datumCurNum;*/

            COPTRStackTopcoptr = listCalcOpts.poll();
        }

        return datumCurNum;
    }
}