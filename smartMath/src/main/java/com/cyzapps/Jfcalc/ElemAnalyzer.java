package com.cyzapps.Jfcalc;

import java.util.*;

import com.cyzapps.Jfcalc.BaseData.BoundOperator;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.*;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AEInvalid;

public class ElemAnalyzer    {

    /***************************************************************************\
     isBlankChar:
       This function is used to justify if a character is a ' ' or a TAB.
       Input:
         The expression string and the char position.
       Output:
         TRUE if the character is , FALSE otherwise.
    \***************************************************************************/
    public static boolean isBlankChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if (cCurChar == ' ' || cCurChar == '\t')
            return true;
        return false;
    }

    /***************************************************************************\
     isBoundOperatorChar:
       This function is used to justify if a character is a bound operator.
       Input:
         The expression string and the char position.
       Output:
         TRUE if the character is a part of a bound operator, FALSE otherwise.
    \***************************************************************************/
    public static boolean isBoundOperatorChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if (cCurChar == '(' || cCurChar == ')')
            return true;
        return false;
    }

    /***************************************************************************\
     getBoundOperator:
       This function is used to get a bound operator.
       Input:
         char *: The expression string.
         unsigned int &: The initial position of the operator in the expression.
       Output:
         The bound operator.
    \
     * @throws JFCALCExpErrException ***************************************************************************/
    public static BoundOperator getBoundOperator(String strExpression, CurPos curpos) throws JFCALCExpErrException
    {
        BoundOperator BOPTRReturnValue = new BoundOperator();
        if ( (strExpression.length() > curpos.m_nPos) && (strExpression.charAt(curpos.m_nPos) == '(') )
            BOPTRReturnValue = new BoundOperator(curpos.m_nPos,
                                            curpos.m_nPos,
                                            OPERATORTYPES.OPERATOR_LEFTPARENTHESE,
                                            1);
        else if ((strExpression.length() > curpos.m_nPos) && (strExpression.charAt(curpos.m_nPos) == ')'))
            BOPTRReturnValue = new BoundOperator(curpos.m_nPos,
                                                curpos.m_nPos,
                                                OPERATORTYPES.OPERATOR_RIGHTPARENTHESE,
                                                1);
    
        /* Return an error if a bound operator cannot be found. */
        if (BOPTRReturnValue.isEqual(OPERATORTYPES.OPERATOR_NOTEXIST))
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERATOR_NOT_EXIST);
    
        curpos.m_nPos++;
        return BOPTRReturnValue;
    }
    
    /***************************************************************************\
     isCalcOperatorChar:
        This function is used to justify if a character is a calculation
        operator.
       Input:
         The expression string and the char position.
       Output:
         TRUE if the character is a part of a calculation operator, FALSE
         otherwise.
    \***************************************************************************/
    public static boolean isCalcOperatorChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if ((cCurChar == '=') ||
            (cCurChar == '>') ||
            (cCurChar == '<') ||
            (cCurChar == '+') ||
            (cCurChar == '-') ||
            (cCurChar == '*') ||
            (cCurChar == '/') ||
            (cCurChar == '\\') ||
            (cCurChar == '%') ||
            (cCurChar == '&') ||
            (cCurChar == '|') ||
            (cCurChar == '^') ||
            (cCurChar == '~') ||
            (cCurChar == '!') ||
            (cCurChar == '\''))
                return true;
        return false;
    }

    /***************************************************************************\
     getCalcOperator:
       This function is used to get a calculation operator.
       Input:
         char *: The expression string.
         unsigned int &: The initial position of the operator in the expression.
         int: justify what is the type of the previous cell pushed in the stack.
       Output:
         The calculation operator.
    \
     * @throws JFCALCExpErrException ***************************************************************************/
    public static CalculateOperator getCalcOperator(String strExpression,
                                      CurPos curpos,
                                      int nLastPushedCellType) throws JFCALCExpErrException
    {
        /* The position of current character */
        int nCurCharPos = curpos.m_nPos;
        /* The starting point of the operator */
        String strStart = strExpression.substring(nCurCharPos);
        
        /* It is necessary to set the calculation operator to be illegal.
        Otherwise, if it is something like "addor", which should be an
        invalid operator, it is hard for the program to identify. */
        CalculateOperator COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_NOTEXIST, -1);
        
        switch(strStart.charAt(0))
        {
        case '+': /* If it is '+'. */
            /* if we didn't push anything or push an operator last time */
            if (nLastPushedCellType == 0 || nLastPushedCellType == 2)
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1);
            else if(nLastPushedCellType == 1) /* if we push a number */
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
            break;
        case '-': /* If it is '-'. */
            if (nLastPushedCellType == 0 || nLastPushedCellType == 2)
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1);
            else if(nLastPushedCellType == 1)
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
            break;
        case '*': /*If it is '*', two possibilities. */
            /* If it is '**'. */
            if (strStart.length() > 1 && strStart.charAt(1) == '*')
            {
                nCurCharPos++;
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_POWER, 2);
            }
            else /* If it is '*'. */
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            break;
        case '/': /* If it is '/'. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
            break;
        case '\\': /* If it is '\'. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_LEFTDIVIDE, 2);
            break;
        case '&':  /* If it is AND. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_AND, 2);
            break;
        case '|':  /* If it is OR. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_OR, 2);
            break;
        case '^':  /* If it is XOR. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_XOR, 2);
            break;
        case '~':  /* If it is NOT. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_NOT, 1);
            break;
        case '!':  /* If it is '!'. */
            if (strStart.length() > 1 && strStart.charAt(1) == '=')
            {
                /* if it is power followed by '=='*/
                if (strStart.length() > 2 && strStart.charAt(2) == '=')
                    COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL,
                                                        1, false);
                else /* if it is NEQ */
                {
                    nCurCharPos++;
                    COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_NEQ, 2);
                }
            }
            else /* If it is FALSE or power. */
            {
                /* if we didn't push anything or push an operator last time */
                if (nLastPushedCellType == 0 || nLastPushedCellType == 2)
                    COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_FALSE, 1);
                else if(nLastPushedCellType == 1) /* if we push a number */
                    COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_FACTORIAL,
                                                          1, false);
            }
            break;
        case '%': /* If it is '%'. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_PERCENT, 1, false);
            break;
        case '\'': /* If it is '\''. */
            COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_TRANSPOSE, 1, false);
            break;
        case '=': /*If it is '='. */
            if (strStart.length() == 1 || (strStart.length() > 1 && strStart.charAt(1) != '='))
            {
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_ASSIGN, 2);
            }
            else if (strStart.length() > 1 && strStart.charAt(1) == '=') /* If it is EQ. */
            {
                nCurCharPos++;
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_EQ, 2);
            }
            break;
        case '>': /*If it is '>', two possibilities. */
            if (strStart.length() > 1 && strStart.charAt(1) == '=')/* If it is '>='. */
            {
                nCurCharPos++;
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_NOSMALLER, 2);
            }
            else /* If it is '>'. */
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_LARGER, 2);
            break;
        case '<': /*If it is '<', two possibilities. */
            if (strStart.length() > 1 && strStart.charAt(1) == '=')/* If it is '<='. */
            {
                nCurCharPos++;
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_NOLARGER, 2);
            }
            else /* If it is '<'. */
                COPTRReturnValue = new CalculateOperator(OPERATORTYPES.OPERATOR_SMALLER, 2);
            break;
        default:
            break;
        }
    
        if (COPTRReturnValue.isEqual(OPERATORTYPES.OPERATOR_NOTEXIST))  /* If we cannot find an
                                                    operator, error! */
        {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERATOR_NOT_EXIST);
        }
    
        curpos.m_nPos = nCurCharPos + 1;
        return COPTRReturnValue;
    }
    
    /***************************************************************************\
     is2ndOPTRHaveHighLevel:
       This function is used to compare the priority of 2 operators.
       Input:
         CALCULATEOPERATOR: The first operator.
         CALCULATEOPERATOR: The second operator.
       Output:
         TRUE if the second operator has a higher priority, FALSE otherwise.
    \***************************************************************************/
    public static boolean is2ndOPTRHaveHighLevel(CalculateOperator COPTR1st,
                                CalculateOperator COPTR2nd)
    {
        if (COPTR2nd.getOperandNum() == 1)
            return true;
        else if (COPTR2nd.getOperatorType().getValue() > COPTR1st.getOperatorType().getValue())
        {
            if ((COPTR2nd.getOperatorType().getValue() <= OPERATORTYPES.OPERATOR_NOSMALLER.getValue()
                && COPTR1st.getOperatorType().getValue() >= OPERATORTYPES.OPERATOR_EQ.getValue()) ||
               (COPTR2nd.getOperatorType().getValue() <= OPERATORTYPES.OPERATOR_SUBTRACT.getValue()
                && COPTR1st.getOperatorType().getValue() >= OPERATORTYPES.OPERATOR_ADD.getValue()) ||
               (COPTR2nd.getOperatorType().getValue() <= OPERATORTYPES.OPERATOR_LEFTDIVIDE.getValue()
                && COPTR1st.getOperatorType().getValue() >= OPERATORTYPES.OPERATOR_MULTIPLY.getValue()) ||
               (COPTR2nd.getOperatorType().getValue() <= OPERATORTYPES.OPERATOR_XOR.getValue()
                && COPTR1st.getOperatorType().getValue() >= OPERATORTYPES.OPERATOR_AND.getValue()))
                return false;
            return true;
        }
        else if ((COPTR1st.getOperatorType() == COPTR2nd.getOperatorType())
            && (COPTR1st.getOperatorType() == OPERATORTYPES.OPERATOR_ASSIGN))
        {
            // for assign operator, things are different, a = b = 5 is calculated by b=5 than a = b
            return true;
        }
        else  return false;
    }
    
    /***************************************************************************\
     isNumberChar:
        This function is used to justify if a character is a digit or not.
       Input:
         The expression string and the char position.
       Output:
         5 if the character is a number;
         4 if the character is a decimal point;
         3 if the character is E or e for scientific notation;
         2 if the character is + or - after E( or e);
         1 if the character is i;
         0 if otherwise.
    \***************************************************************************/
    public static enum NUMBERCHARTYPES {
        NUMBERCHAR_DIGIT,
        NUMBERCHAR_DECIMALPNT,
        NUMBERCHAR_E,
        NUMBERCHAR_POSNEGSIGNAFTERE,
        NUMBERCHAR_I,
        NUMBERCHAR_UNRECOGNIZED,
    };
    
    public static boolean isStartNumberChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if ((cCurChar >= '0' && cCurChar <= '9') || cCurChar == '.')    {
            return true;
        } else if ((cCurChar == 'i' || cCurChar == 'I')
            && ((strExpression.length() <= nCurPos + 1) // this is the last char
                    || strExpression.charAt(nCurPos + 1) == ',' // it is before ,
                    || isBlankChar(strExpression, nCurPos + 1)  // it is before a blank char
                    || isCalcOperatorChar(strExpression, nCurPos + 1)   // it is before an operator
                    || isBoundOperatorChar(strExpression, nCurPos + 1)))    {   // it is before ( or )
            return true;            
        } else  {
            return false;
        }
    }
    
    public static boolean isDigitChar(String strExpression, int nCurPos, int nPN)   {
        char cThis = strExpression.charAt(nCurPos);
        return isDigitChar(cThis, nPN);
    }
    
    public static boolean isDigitChar(char cThis, int nPN) {
        if (nPN == 2 && (cThis == '0' || cThis == '1')) {
            return true;
        } else if (nPN == 8 && (cThis >= '0' && cThis <= '7'))  {
            return true;
        } else if (nPN == 16 && ((cThis >= '0' && cThis <= '9') || (cThis >= 'a' && cThis <= 'f') || (cThis >= 'A' && cThis <= 'F')))   {
            return true;
        } else if (nPN == 10 && cThis >= '0' && cThis <= '9')   {
            return true;
        } else  {
            return false;
        }        
    }
    
    public static NUMBERCHARTYPES getDecimalCharType(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if (cCurChar >= '0' && cCurChar <= '9')    {
            return NUMBERCHARTYPES.NUMBERCHAR_DIGIT;
        } else if (cCurChar == '.')    {
            return NUMBERCHARTYPES.NUMBERCHAR_DECIMALPNT;
        } else if ((cCurChar == 'e' || cCurChar == 'E')
            && nCurPos >= 1
            && getDecimalCharType(strExpression, nCurPos - 1) == NUMBERCHARTYPES.NUMBERCHAR_DIGIT
            && strExpression.length() > nCurPos + 1
            && (strExpression.charAt(nCurPos + 1) == '-'
                || strExpression.charAt(nCurPos + 1) == '+'
                || (strExpression.charAt(nCurPos + 1) >= '0' && strExpression.charAt(nCurPos + 1) <= '9'))) {
            return NUMBERCHARTYPES.NUMBERCHAR_E;
        } else if ((cCurChar == '+' || cCurChar == '-')
            && nCurPos >= 1 && getDecimalCharType(strExpression, nCurPos - 1) == NUMBERCHARTYPES.NUMBERCHAR_E)   {
            return NUMBERCHARTYPES.NUMBERCHAR_POSNEGSIGNAFTERE;
        } else if ((cCurChar == 'i' || cCurChar == 'I')
            && ((strExpression.length() <= nCurPos + 1)
                    || strExpression.charAt(nCurPos + 1) == ','
                    || isBlankChar(strExpression, nCurPos + 1)
                    || isCalcOperatorChar(strExpression, nCurPos + 1)
                    || isBoundOperatorChar(strExpression, nCurPos + 1)))    {
            return NUMBERCHARTYPES.NUMBERCHAR_I;
        } else    {
            return NUMBERCHARTYPES.NUMBERCHAR_UNRECOGNIZED;
        }
    }
    
    /***************************************************************************\
     getNumber:
       This function is used to get a number.
       Input:
         char *: The expression string.
         unsigned int &: The initial position of the number in the expression.
       Output:
         The number.
    \
     * @throws JFCALCExpErrException ***************************************************************************/
    public static DataClass getNumber(String strExpression, CurPos curpos) throws JFCALCExpErrException
    {
        // first of all, find positional notation
        int nPN = 10;   // can be 2, 8, 10 or 16, other value is treated as 10.
        if (strExpression.charAt(curpos.m_nPos) == '0' && strExpression.length() > (curpos.m_nPos + 1))   {
            if (strExpression.charAt(curpos.m_nPos + 1) == 'b' || strExpression.charAt(curpos.m_nPos + 1) == 'B')  {
                nPN = 2;
            } else if (strExpression.charAt(curpos.m_nPos + 1) == 'x' || strExpression.charAt(curpos.m_nPos + 1) == 'X')    {
                nPN = 16;
            } else if (strExpression.charAt(curpos.m_nPos + 1) >= '0' && strExpression.charAt(curpos.m_nPos + 1) <= '7')    {
                nPN = 8;
            }
        }
        
        DataClass datumReturnNum = new DataClass();
        if (nPN == 2 || nPN == 8 || nPN == 16)   {  // these does not support scientific notation like 0.11e11
            MFPNumeric mfpNumCurNum = MFPNumeric.ZERO;
            int nStartPosition = (nPN == 8)?(curpos.m_nPos + 1):(curpos.m_nPos + 2);
            curpos.m_nPos = nStartPosition;
            int nPosAfterDecimalPnt = -1;
            boolean boolIsComplexImage = false;
            while (strExpression.length() > curpos.m_nPos && boolIsComplexImage == false) {
                char cThis = strExpression.charAt(curpos.m_nPos);
                if (isDigitChar(strExpression, curpos.m_nPos, nPN)) {
                    int nThisDigVal = cThis - '0';
                    if (nPN == 16)  {
                        if (cThis >= 'A' && cThis <= 'F')   {
                            nThisDigVal = cThis - 'A' + 10;
                        } else if (cThis >= 'a' && cThis <= 'f')    {
                            nThisDigVal = cThis - 'a' + 10;
                        }
                    }
                    if (nPosAfterDecimalPnt < 0)   {
                        mfpNumCurNum = mfpNumCurNum.multiply(new MFPNumeric(nPN)).add(new MFPNumeric(nThisDigVal));
                    } else  {
                        mfpNumCurNum = mfpNumCurNum.add(new MFPNumeric(nThisDigVal).divide(MFPNumeric.pow(new MFPNumeric(nPN), new MFPNumeric(nPosAfterDecimalPnt))));
                    }
                } else if (cThis == '.')   {
                    if (nPosAfterDecimalPnt == -1)  {
                        nPosAfterDecimalPnt = 0;
                        if (strExpression.length() <= (curpos.m_nPos + 1) || isDigitChar(strExpression, curpos.m_nPos + 1, nPN) == false) {
                            /* Based on tradition, there should be some digits after the radix point. */
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
                        }
                    } else {
                        /* If there are two radix points, error. */
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT);
                    }
                } else if (cThis == 'i' || cThis == 'I')    {/* otherwise it is i or I */
                    if (nStartPosition == curpos.m_nPos)    /* this is the first character */
                    {
                        // 0xi is not supported.
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
                    }
                    boolIsComplexImage = true;
                } else  {
                    break;  // we may arrive at the end.
                }
                curpos.m_nPos++; 
                if (nPosAfterDecimalPnt >= 0)   {
                    nPosAfterDecimalPnt ++;
                }
            }

            if (nStartPosition == curpos.m_nPos)    {
                // there is no digit following 0x or 0b.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }
            else if (nPosAfterDecimalPnt < 0) /* If there is no radix piont, it is an integer. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_INTEGER, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            else    /* If there is a radix point, it is a flow number. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_DOUBLE, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            if (boolIsComplexImage)    {
                // this is a complex number
                DataClass datumReturnComplex = new DataClass();
                DataClass datumReturnReal = new DataClass();
                datumReturnReal.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                datumReturnComplex.setComplex(datumReturnReal, datumReturnNum);
                datumReturnNum = datumReturnComplex;
            }
        } else  {   // nPN == 10
            MFPNumeric mfpNumCurNum = MFPNumeric.ZERO;
            int nStartPosition = curpos.m_nPos;
            if (strExpression.length() > (curpos.m_nPos + 1)
                && strExpression.charAt(curpos.m_nPos) == '0'
                && getDecimalCharType(strExpression, curpos.m_nPos + 1) == NUMBERCHARTYPES.NUMBERCHAR_DIGIT)    // 0 to 9
            {
                /* if 0 is the initial digit, it must appear exactly before the radix point. */
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }

            boolean boolBeforeDecimalPnt = true;
            boolean boolBeforeE = true;
            boolean boolIsComplexImage = false;
            String strNumberB4I = "";
            while (((strExpression.length() > curpos.m_nPos)
                    && getDecimalCharType(strExpression, curpos.m_nPos) != NUMBERCHARTYPES.NUMBERCHAR_UNRECOGNIZED)
                    && boolIsComplexImage == false)
            {
                if (strExpression.charAt(curpos.m_nPos) != '.'
                    && strExpression.charAt(curpos.m_nPos) != 'i' && strExpression.charAt(curpos.m_nPos) != 'I'
                    && strExpression.charAt(curpos.m_nPos) != 'e' && strExpression.charAt(curpos.m_nPos) != 'E')
                {
                    strNumberB4I += strExpression.charAt(curpos.m_nPos);
                }
                else if (strExpression.charAt(curpos.m_nPos) == '.')    /* If it is a radix point. */
                {
                    if (boolBeforeE == false)  {
                        // after E, there should not be decimal point
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG);
                    } else if (boolBeforeDecimalPnt)
                    {
                        boolBeforeDecimalPnt = false;
                        if (strExpression.length() <= (curpos.m_nPos + 1)
                            || strExpression.charAt(curpos.m_nPos + 1) > '9'
                            || strExpression.charAt(curpos.m_nPos + 1) < '0')
                        {
                            /* Based on tradition, there should be some digits after the radix point. */
                            throw new JFCALCExpErrException(ERRORTYPES
                                                    .ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
                        }
                    }
                    else
                    {
                        /* If there are two radix points, error. */
                        throw new JFCALCExpErrException(ERRORTYPES
                                                .ERROR_NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT);
                    }
                    strNumberB4I += strExpression.charAt(curpos.m_nPos);
                }
                else if (strExpression.charAt(curpos.m_nPos) == 'E' || strExpression.charAt(curpos.m_nPos) == 'e')    /* If it is a scientific notation e or E. */
                {
                    if (boolBeforeE)
                    {
                        boolBeforeE = false;
                        if (strExpression.length() <= (curpos.m_nPos + 1)) {
                            // should be something after 'E'
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG);
                        } else if (strExpression.charAt(curpos.m_nPos + 1) == '+' || strExpression.charAt(curpos.m_nPos + 1) == '-')    {
                            if (strExpression.length() <= (curpos.m_nPos + 2)
                                    || strExpression.charAt(curpos.m_nPos + 2) > '9'
                                    || strExpression.charAt(curpos.m_nPos + 2) < '0')   {
                                // should be E+... or E-... (... means digits).
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG);
                            }
                        } else if (strExpression.charAt(curpos.m_nPos + 1) > '9' || strExpression.charAt(curpos.m_nPos + 1) < '0')  {
                            /* should be E... (... means digits). */
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG);
                        }
                    }
                    else
                    {
                        /* Should not be two 'E's. */
                        throw new JFCALCExpErrException(ERRORTYPES
                                                .ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG);
                    }
                    strNumberB4I += strExpression.charAt(curpos.m_nPos);
                }
                else    /* otherwise it is i or I */
                {
                    if (nStartPosition == curpos.m_nPos)    /* this is the first character */
                    {
                        strNumberB4I = "1";
                    }
                    boolIsComplexImage = true;
                }
                curpos.m_nPos++; 
            }
            if (strNumberB4I.length() == 0) {   // no digit in the data string.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }
            try {
                mfpNumCurNum = new MFPNumeric(strNumberB4I);
            } catch(NumberFormatException e)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }
            if (boolBeforeDecimalPnt && boolBeforeE) /* If there is no radix piont or Scientific notation, it is an integer. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_INTEGER, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            else    /* If there is a radix point, it is a flow number. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_DOUBLE, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            if (boolIsComplexImage)    {
                // this is a complex number
                DataClass datumReturnComplex = new DataClass();
                DataClass datumReturnReal = new DataClass();
                datumReturnReal.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                datumReturnComplex.setComplex(datumReturnReal, datumReturnNum);
                datumReturnNum = datumReturnComplex;
            }
        }
        return datumReturnNum;
    }

    /***************************************************************************\
     isDataRefChar:
        This function is used to justify if a character is a data reference (array).
       Input:
         The expression string and the char position.
       Output:
         true if it is;
         false if it is not.
    \***************************************************************************/
    public static boolean isDataRefChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if (cCurChar == '[')    {
            return true;
        } else    {
            return false;
        }
    }
    /***************************************************************************\
     getDataRef:
       This function is used to get the data reference.
       Input:
         strExpression: The expression string.
         CurPos: The initial position of the name in the expression.
         lVarNameSpaces: Parameters namespace.
       Output:
         The data reference data.
    \
     * @throws JFCALCExpErrException 
     * @throws InterruptedException ***************************************************************************/
    public static DataClass getDataRef(String strExpression, CurPos curpos, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException, InterruptedException
    {
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
        int nEndofVariablePos = curpos.m_nPos;
        if (strExpression.charAt(nEndofVariablePos) == '[')    {
            int nBracketLevel = 1;
            int i;
            /* find out the close bracket position */
            for (i = nEndofVariablePos + 1; i < strExpression.length(); i++)
            {
                if (strExpression.charAt(i) == ']')
                    nBracketLevel --;
                else if (strExpression.charAt(i) == '[')
                    nBracketLevel ++;
                if (nBracketLevel == 0)
                {
                    nEndofVariablePos = i;
                    break;
                }
            }
    
            if (nBracketLevel != 0)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
            }
        }
        else
        {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_REFERENCE);
        }

        LinkedList<DataClass> tsDataRef = new LinkedList<DataClass>();  /* data reference list */
        /* get a string of index list */
        int nIndexStart = curpos.m_nPos + 1;
        boolean bNoIndex = true;  /* if there is no Index */
        for (int j = nIndexStart; j < nEndofVariablePos; j++)
        {
            if (!isBlankChar(strExpression, j))
            {
                bNoIndex = false;
                break;
            }
        }
        if (bNoIndex == true)
        {
            //throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_REFERENCE); // do not throw exception as [] should be allowed
        }
        else
        {
            while(nIndexStart < nEndofVariablePos)
            {
                String strDataList = strExpression.substring(nIndexStart, nEndofVariablePos);
                CurPos curposSubExpr = new CurPos();
                curposSubExpr.m_nPos = 0;
                DataClass datumIndex = exprEvaluator.evaluateExpression(strDataList, curposSubExpr);
                if (datumIndex == null)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                }
                /* if the parameter expression's value is invalid, error will be generated. */
                tsDataRef.addLast(datumIndex);
                nIndexStart += curposSubExpr.m_nPos + 1;
                if ((strDataList.length() == curposSubExpr.m_nPos + 1)
                    && (strDataList.charAt(curposSubExpr.m_nPos) == ','))
                {
                    /* this is to handle a situation like [-1,], where there is a , unnecessary */
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                }                            
            }
        }
        
        DataClass[] dataList = new DataClass[tsDataRef.size()];
        for (int indxData = 0; indxData < tsDataRef.size(); indxData ++)
        {
            dataList[indxData] = tsDataRef.get(indxData);
        }
        DataClass datumReturnNum = new DataClass(dataList);
        curpos.m_nPos = nEndofVariablePos + 1;
        return datumReturnNum;        
    }

    /***************************************************************************\
     isNameChar:
        This function is used to justify if a character belongs to a name of a
        function or a variable.
       Input:
         The expression string and the char position.
       Output:
         0 if the character is not a part of a name, 1 if the character is
         possibly the first charactor of a name, >=2 if the character can only be
         the non-first character of a name.
    \***************************************************************************/
    public static int isNameChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if (cCurChar == '.' ||(cCurChar >= '0' && cCurChar <= '9'))
            return 2;
        if ((cCurChar >= 'a' && cCurChar <= 'z') ||
            (cCurChar >= 'A' && cCurChar <= 'Z') ||
            (cCurChar >= '\u0391' && cCurChar <= '\u03A9') ||    // support limited greek letters for polar charts.
            (cCurChar >= '\u03B1' && cCurChar <= '\u03C9') ||    // support limited greek letters for polar charts.
            cCurChar == '_')
            return 1;
        return 0;
    }
    
    /***************************************************************************\
     getExprName:
       This function is used to get the name of a variable or a function.
       Input:
         char *: The expression string.
         unsigned int &: The initial position of the name in the expression.
       Output:
         The value of the name.
    \
     * @throws JFCALCExpErrException 
     * @throws InterruptedException ***************************************************************************/
    public static DataClass getExprName(String strExpression, CurPos curpos, LinkedList<LinkedList<Variable>> lVarNameSpaces) throws JFCALCExpErrException, InterruptedException
    {
        DataClass datumReturnNum = new DataClass();
        int nCurNameCharPos = curpos.m_nPos;
        ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
    
        /* get the function or variable name */
        String strName = "";
        while ((strExpression.length() > nCurNameCharPos) && (isNameChar(strExpression, nCurNameCharPos) >= 1))
        {
            strName += strExpression.charAt(nCurNameCharPos);
            nCurNameCharPos++;
        }
    
        /* identify if it is function or variable */
        int nCurFurtherCharPos = nCurNameCharPos;
        while ((strExpression.length() > nCurFurtherCharPos) && isBlankChar(strExpression, nCurFurtherCharPos))
            nCurFurtherCharPos ++;
        
    
        if ((strExpression.length() > nCurFurtherCharPos) && (strExpression.charAt(nCurFurtherCharPos) == '(')) /* function */
        {
            int nBracketLevel = 1;
            int nCloseBracketPos = nCurFurtherCharPos;
            int i;
            /* find out the close bracket position */
            for (i = nCurFurtherCharPos + 1; i < strExpression.length(); i++)
            {
                if (isStringStartChar(strExpression, i))
                {
                    CurPos curpos1 = new CurPos();
                    curpos1.m_nPos = i;
                    getString(strExpression, curpos1);
                    i = curpos1.m_nPos;
                }
                
                if (i >= strExpression.length())    {
                    // ok, we have arrived at the end of the expression, but we cannot find close bracket.
                    // jump out and throw exception
                    break;
                } else if (strExpression.charAt(i) == ')')    {
                    nBracketLevel --;
                } else if (strExpression.charAt(i) == '(')    {
                    nBracketLevel ++;
                }
                
                if (nBracketLevel == 0)
                {
                    nCloseBracketPos = i;
                    break;
                }
            }
    
            if (nBracketLevel != 0)
            {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNMATCHED_LEFTPARENTHESE);
            }
    
            LinkedList<DataClass> tsParameter = new LinkedList<DataClass>();  /* parameter stack */
            /* get a string of parameter list */
            int nParameterStart = nCurFurtherCharPos + 1;
            boolean bNoParameter = true;  /* if there is no parameter */
            for (int j = nParameterStart; j < nCloseBracketPos; j++)
            {
                if (!isBlankChar(strExpression, j))
                {
                    bNoParameter = false;
                }
            }
            if (bNoParameter == false)
            {
                while(nParameterStart < nCloseBracketPos)
                {
                    String strParameter = strExpression.substring(nParameterStart, nCloseBracketPos);
                    CurPos curposSubExpr = new CurPos();
                    curposSubExpr.m_nPos = 0;
                    datumReturnNum = exprEvaluator.evaluateExpression(strParameter, curposSubExpr);
                    if (datumReturnNum == null)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_VOID_DATA);
                    }
                    
                    /* push the parameter */
                    tsParameter.addFirst(datumReturnNum);
                    nParameterStart += curposSubExpr.m_nPos + 1;
                    if ((strParameter.length() == curposSubExpr.m_nPos + 1)
                        && (strParameter.charAt(curposSubExpr.m_nPos) == ','))
                    {
                        /* this is to handle a situation like abs(-1,), where there is a , unecessary */
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NO_EXPRESSION);
                    }
                }
            }
            datumReturnNum = FuncEvaluator.evaluateFunction(strName, tsParameter, lVarNameSpaces);
            if (datumReturnNum == null)
            {
                // Now allow return nothing.
            }
            /* tell the EveluateExpression function to identify next character. */
            curpos.m_nPos = nCloseBracketPos + 1;
            return datumReturnNum;
        }
        else if (strExpression.length() == nCurFurtherCharPos
                || (strExpression.length() > nCurFurtherCharPos
                        && (strExpression.charAt(nCurFurtherCharPos) == ',' ||
                        strExpression.charAt(nCurFurtherCharPos) == '[' ||
                        strExpression.charAt(nCurFurtherCharPos) == ')' ||
                        isCalcOperatorChar(strExpression, nCurFurtherCharPos))))
        {
            /*
             * str length <= further char pos means we are at the end of string, this means it
             * is a variable. It may also be a function name. JMAnalyzer will first look up
             * variable name space then look up function name space for it.
             * if str length > further char pos and the further char is ',', it is a parameter
             * which could be either a variable or a function name.
             * if str length > further char pos and the further char is '[', the follows are a
             * data index.
             */
            
            String strVarName = strExpression.substring(curpos.m_nPos, nCurNameCharPos);
            Variable var = VariableOperator.lookUpSpaces(strVarName, lVarNameSpaces);
            if (var != null){
                /*
                 * We do not use datumReturnNum = CopyTypeValue(var.GetValue())
                 * here because when we assign a new value to a data class,
                 * we need the data class reference.
                 */
                //datumReturnNum.CopyTypeValue(var.GetValue());
                datumReturnNum = var.getValue();
            } else    {
                /* we cannot find the variable. */
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_UNDEFINED_VARIABLE);
            }
            curpos.m_nPos = nCurFurtherCharPos;
            return datumReturnNum;
        }
        else /* neither function nor variable */
        {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS);
        }
    }
    
    /***************************************************************************\
     isStringStartChar:
        This function is used to justify if a character is the beginning of a string.
       Input:
         The expression string and the char position.
       Output:
         true if the character is the beginning of a string, otherwise false.
    \***************************************************************************/
    public static boolean isStringStartChar(String strExpression, int nCurPos)
    {
        char cCurChar = strExpression.charAt(nCurPos);
        if (cCurChar == '"')
            return true;
        return false;
    }
    
    /***************************************************************************\
     getString:
       This function is used to get a string constant from expression.
       Input:
         String: The expression string.
         CurPos: The initial position of the name in the expression.
       Output:
         A dataclass which is the string constant.
    \***************************************************************************/
    public static DataClass getString(String strExpression, CurPos curpos) throws JFCALCExpErrException
    {
        if (isStringStartChar(strExpression, curpos.m_nPos) == false)    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_NOT_A_STRING);
        }
        char cStartChar = strExpression.charAt(curpos.m_nPos);
        char cEscapeChar = '\\';
        boolean bInEscapeMode = false;
        int i;
        String strValue = "";
        for (i = curpos.m_nPos + 1; i < strExpression.length(); i++)
        {
            if (strExpression.charAt(i) == cEscapeChar && !bInEscapeMode)    {
                bInEscapeMode = true;
            } else if (bInEscapeMode)    {
                switch (strExpression.charAt(i))    {
                case 'b':
                    strValue += '\b';
                    break;
                case 'f':
                    strValue += '\f';
                    break;
                case 'n':
                    strValue += '\n';
                    break;
                case 'r':
                    strValue += '\r';
                    break;
                case 't':
                    strValue += '\t';
                    break;
                case '\\':
                    strValue += '\\';
                    break;
                case '\'':
                    strValue += '\'';
                    break;
                case '\"':
                    strValue += '\"';
                    break;
                default:
                    strValue += strExpression.charAt(i);
                }
                bInEscapeMode = false;
            } else if (strExpression.charAt(i) == cStartChar)    {
                break;
            } else    {
                strValue += strExpression.charAt(i);
            }
        }
        if (i >= strExpression.length())    {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING);
        }
        DataClass datumString = new DataClass(DATATYPES.DATUM_STRING, strValue);
        curpos.m_nPos = i + 1;
        return datumString;
    }
}
