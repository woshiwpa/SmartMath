package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.ElemAnalyzer;
import com.cyzapps.Jfcalc.BaseData.*;
import com.cyzapps.Jfcalc.ElemAnalyzer.NUMBERCHARTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

public class ExprAnalyzer {
    
    /*
     * Note that here variable name space is not used because variable name will be looked up when we further-analyze
     * the abstract expression.
     */
    
    public ExprAnalyzer() {
    }

    public static AbstractExpr processTwoOperandCell(AbstractExpr aeFirstOperand,
            CalculateOperator COPTROperator, AbstractExpr aeSecondOperand) throws JFCALCExpErrException, JSmartMathErrException {

        AbstractExpr aeReturn = AEInvalid.AEINVALID;
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        
        switch (COPTROperator.getOperatorType()) {
        case OPERATOR_ASSIGN:
            listChildren.add(aeFirstOperand);
            listChildren.add(aeSecondOperand);
            aeReturn = new AEAssign(listChildren);
            break;
        case OPERATOR_EQ:
        case OPERATOR_NEQ:
        case OPERATOR_LARGER:
        case OPERATOR_SMALLER:
        case OPERATOR_NOLARGER:
        case OPERATOR_NOSMALLER:
            aeReturn = new AECompare(aeFirstOperand, COPTROperator.getOperatorType(), aeSecondOperand);
            break;
        case OPERATOR_ADD:
        case OPERATOR_SUBTRACT:
            listChildren.add(aeFirstOperand);
            listChildren.add(aeSecondOperand);
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
            listOpts.add(new CalculateOperator(COPTROperator.getOperatorType(), 2));
            aeReturn = new AEPosNegOpt(listChildren, listOpts);
            break;
        case OPERATOR_MULTIPLY:
        case OPERATOR_DIVIDE:
            listChildren.add(aeFirstOperand);
            listChildren.add(aeSecondOperand);
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
            listOpts.add(new CalculateOperator(COPTROperator.getOperatorType(), 2));
            aeReturn = new AEMulDivOpt(listChildren, listOpts);
            break;
        case OPERATOR_LEFTDIVIDE:
            aeReturn = new AELeftDivOpt(aeFirstOperand, aeSecondOperand);
            break;
        case OPERATOR_AND:
        case OPERATOR_OR:
        case OPERATOR_XOR:
            aeReturn = new AEBitwiseOpt(aeFirstOperand, COPTROperator.getOperatorType(), aeSecondOperand);
            break;
        case OPERATOR_POWER:
            aeReturn = new AEPowerOpt(aeFirstOperand, aeSecondOperand);
            break;
        default:
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_BINARY_OPERATOR);
        }
        return aeReturn;
    }

    public static AbstractExpr processOneOperandCell(AbstractExpr aeOperand,
            CalculateOperator COPTROperator) throws JFCALCExpErrException, JSmartMathErrException {
        AbstractExpr aeReturn = AEInvalid.AEINVALID;
        
        switch (COPTROperator.getOperatorType()) {
        case OPERATOR_TRANSPOSE:
            aeReturn = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_TRANSPOSE, aeOperand, 1);
            break;
        case OPERATOR_FACTORIAL:
            aeReturn = new AEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FACTORIAL, aeOperand);
            break;
        case OPERATOR_PERCENT:
            aeReturn = new AEUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_PERCENT, aeOperand);
            break;
        default:
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
        }

        return aeReturn;
    }

    public static AbstractExpr processOneOperandCell(CalculateOperator COPTROperator,
            AbstractExpr aeOperand) throws JFCALCExpErrException, JSmartMathErrException {
        AbstractExpr aeReturn = AEInvalid.AEINVALID;
        switch (COPTROperator.getOperatorType()) {
        case OPERATOR_FALSE:
            aeReturn = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_FALSE, aeOperand, 1);
            break;
        case OPERATOR_NOT:
            aeReturn = new AEOnOffUnaryOpt(ABSTRACTEXPRTYPES.ABSTRACTEXPR_UOPT_NOT, aeOperand, 1);
            break;
        case OPERATOR_POSSIGN:
        case OPERATOR_NEGSIGN:
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listChildren.add(aeOperand);
            listOpts.add(new CalculateOperator(COPTROperator.getOperatorType(), 1, true));
            aeReturn = new AEPosNegOpt(listChildren, listOpts);
            break;
        default:
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_MONADIC_OPERATOR);
        }
        return aeReturn;
    }
    
    public static AbstractExpr processIndex(AbstractExpr aeToBeIndex, AbstractExpr aeIndex) throws JFCALCExpErrException, JSmartMathErrException    {
        return new AEIndex(aeToBeIndex, aeIndex);
    }
    
    public static AbstractExpr analyseExpression(String strExpression, CurPos curpos)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        return analyseExpression(strExpression, curpos, new LinkedList<Variable>());
    }
    
    public static AbstractExpr analyseExpression(String strExpression,
            CurPos curpos, LinkedList<Variable> listPseudoConsts)
            throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        LinkedList<AbstractExpr> tsAbstractExpr = new LinkedList<AbstractExpr>();
        LinkedList<CalculateOperator> tsCalculateOperators = new LinkedList<CalculateOperator>();

        BoundOperator BOPTRCurboptr;
        CalculateOperator COPTRStackTopcoptr;
        CalculateOperator COPTRCurcoptr = new CalculateOperator();
        AbstractExpr aeCurExpr = AEInvalid.AEINVALID;

        SMElemAnalyzer smelemAnalyzer = new SMElemAnalyzer();

        int nLastPushedCellType = 0; /* 1 for number, 2 for calculation operator */
        int nRecursionStartPosition = curpos.m_nPos;

        while (curpos.m_nPos < strExpression.length()) {

            /* A bound char */
            if (ElemAnalyzer.isBoundOperatorChar(strExpression, curpos.m_nPos)) {
                BOPTRCurboptr = ElemAnalyzer.getBoundOperator(strExpression, curpos);
                // need not to worry about OPERATOR_NOTEXIST because it has been handled
                // in GetBoundOperator
                if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_LEFTPARENTHESE)) {
                    aeCurExpr = analyseExpression(strExpression, curpos, listPseudoConsts);

                    if (nLastPushedCellType == 1) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                    }

                    tsAbstractExpr.addFirst(aeCurExpr);
                    nLastPushedCellType = 1;
                }

                else if (BOPTRCurboptr.isEqual(OPERATORTYPES.OPERATOR_RIGHTPARENTHESE)) {
                    if (nRecursionStartPosition == 0) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_RIGHTPARENTHESE);
                    }

                    if ((tsCalculateOperators.size() == 0) && (tsAbstractExpr.size() == 0)) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                    }

                    if (nLastPushedCellType != 1) {
                        throw new JFCALCExpErrException(
                                ERRORTYPES.ERROR_LACK_OPERAND);
                    }

                    aeCurExpr = tsAbstractExpr.poll();
                    COPTRStackTopcoptr = tsCalculateOperators.poll();
                    while (COPTRStackTopcoptr != null) {
                        if (COPTRStackTopcoptr.getOperandNum() == 1) {
                            if (aeCurExpr == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            aeCurExpr = processOneOperandCell(COPTRStackTopcoptr, aeCurExpr);
                        } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                            AbstractExpr aeFirstOperand;
                            aeFirstOperand = tsAbstractExpr.poll();
                            if (aeFirstOperand == null || aeCurExpr == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            aeCurExpr = processTwoOperandCell(
                                    aeFirstOperand, COPTRStackTopcoptr,
                                    aeCurExpr);
                        }
                        /* need not to worry about non exist data return now because an exception will be thrown
                        if (aeCurExpr.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                            return aeCurExpr;*/

                        COPTRStackTopcoptr = tsCalculateOperators.poll();
                    }
                    return aeCurExpr;
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
                            aeCurExpr = tsAbstractExpr.poll();
                            if (nLastPushedCellType != 1 || aeCurExpr == null) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                            }
                            aeCurExpr = processOneOperandCell(aeCurExpr, COPTRCurcoptr);
                            /* need not to worry about non-exist data because it is handled
                             * in EvaluateOneOperandCell
                            if (aeCurExpr.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                                return aeCurExpr;*/
                            tsAbstractExpr.addFirst(aeCurExpr);
                            nLastPushedCellType = 1;
                        }
                    }
                }

                if (COPTRCurcoptr.getLabelPrefix()) {
                    if (COPTRCurcoptr.getOperandNum() == 2) {
                        boolean bHaveBOptHigherLevelLeft = false;
                        for (int idx = 0; idx < tsCalculateOperators.size(); idx ++) {
                            if (tsCalculateOperators.get(idx).getOperandNum() == 2) {
                                if (!ElemAnalyzer.is2ndOPTRHaveHighLevel(tsCalculateOperators.get(idx), COPTRCurcoptr)) {
                                    bHaveBOptHigherLevelLeft = true;
                                }
                                break;
                            }
                        }
                        aeCurExpr = tsAbstractExpr.poll();
                        COPTRStackTopcoptr = tsCalculateOperators.poll();
                        while (COPTRStackTopcoptr != null
                                && ((bHaveBOptHigherLevelLeft && COPTRStackTopcoptr.getOperandNum() == 1)
                                    || !ElemAnalyzer.is2ndOPTRHaveHighLevel(COPTRStackTopcoptr, COPTRCurcoptr))) {
                            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                                if (aeCurExpr == null)    {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                                }
                                aeCurExpr = processOneOperandCell(COPTRStackTopcoptr, aeCurExpr);
                            } else {    // COPTRStackTopcoptr.getOperandNum() == 2
                                AbstractExpr aeFirstOperand = tsAbstractExpr.poll();
                                if (aeFirstOperand == null || aeCurExpr == null) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                                }
                                aeCurExpr = processTwoOperandCell(aeFirstOperand, COPTRStackTopcoptr, aeCurExpr);
                            }
                            /* need not to worry about non-exist data because it is handled in
                             * EvaluateOneOperandCell
                            if (aeCurExpr.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                                return aeCurExpr;*/

                            COPTRStackTopcoptr = tsCalculateOperators.poll();
                        }
                        if (COPTRStackTopcoptr != null) {
                            tsCalculateOperators.addFirst(COPTRStackTopcoptr);
                        }
                        tsAbstractExpr.addFirst(aeCurExpr);
                    }

                    tsCalculateOperators.addFirst(COPTRCurcoptr);
                    nLastPushedCellType = 2;
                }
            }

            /* The new cell seems to be a number */
            else if (ElemAnalyzer.isStartNumberChar(strExpression, curpos.m_nPos)) {
                aeCurExpr = smelemAnalyzer.getNumberAExpr(strExpression, curpos);
                /* we need not to worry about Not exist data here because error has been handled before
                 * if (aeCurExpr.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return aeCurExpr;*/
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }

                tsAbstractExpr.addFirst(aeCurExpr);
                nLastPushedCellType = 1;
            }

            /* The new cell seems to be a data reference */
            else if (ElemAnalyzer.isDataRefChar(strExpression, curpos.m_nPos)) {
                aeCurExpr = smelemAnalyzer.getDataRefAExpr(strExpression, curpos, listPseudoConsts);
                if (nLastPushedCellType == 1) {
                    AbstractExpr aeToBeIndexed = tsAbstractExpr.poll();
                    if (aeToBeIndexed == null || aeCurExpr == null)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                    }
                    aeCurExpr = processIndex(aeToBeIndexed, aeCurExpr);
                }

                tsAbstractExpr.addFirst(aeCurExpr);
                nLastPushedCellType = 1;
            }

            /*
             * The new cell seems to be the beginning of a variable or
             * function's name
             */
            else if (ElemAnalyzer.isNameChar(strExpression, curpos.m_nPos) == 1) {
                aeCurExpr = smelemAnalyzer.getExprNameAExpr(strExpression, curpos, listPseudoConsts);
                /* A function might return NULL point. A NULL point is DATATYPES.DATUM_NOTEXIST
                if (aeCurExpr.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                    return aeCurExpr;*/
                if (nLastPushedCellType == 1) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
                }

                tsAbstractExpr.addFirst(aeCurExpr);
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
                aeCurExpr = smelemAnalyzer.getStringAExpr(strExpression, curpos);
                tsAbstractExpr.addFirst(aeCurExpr);
                nLastPushedCellType = 1;
            } else {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_IDENTIFIED_CHARACTER);
            }

        }
        /* arriving here means we are in the top level of expression */
        if (nRecursionStartPosition != 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
        }

        if ((tsCalculateOperators.size() == 0) && (tsAbstractExpr.size() == 0)) {
            if (curpos.m_nPos == 0)
                curpos.m_nPos = 1;
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
        }

        if (nLastPushedCellType != 1) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
        }

        aeCurExpr = tsAbstractExpr.poll();
        COPTRStackTopcoptr = tsCalculateOperators.poll();
        while (COPTRStackTopcoptr != null) {
            if (COPTRStackTopcoptr.getOperandNum() == 1) {
                if (aeCurExpr == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                }
                aeCurExpr = processOneOperandCell(COPTRStackTopcoptr, aeCurExpr);
            } else {
                AbstractExpr aeFirstOperand;
                aeFirstOperand = tsAbstractExpr.poll();
                if (aeFirstOperand == null || aeCurExpr == null) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERAND);
                }
                aeCurExpr = processTwoOperandCell(aeFirstOperand, COPTRStackTopcoptr, aeCurExpr);
            }
            /* need not to worry about non-exist data because it is handled in
             * EvaluateOneOperandCell
            if (aeCurExpr.GetDataType() == DATATYPES.DATUM_NOTEXIST)
                return aeCurExpr;*/

            COPTRStackTopcoptr = tsCalculateOperators.poll();
        }

        return aeCurExpr;
    }
}
