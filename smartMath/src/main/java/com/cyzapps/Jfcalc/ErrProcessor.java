package com.cyzapps.Jfcalc;

public class ErrProcessor {
    /* Definition of error types. */
    public static enum ERRORTYPES
    {
        /* No error */
        NO_ERROR_STATE,
        /* No expression */
        ERROR_NO_EXPRESSION,
        /* Multiple expressions */
        ERROR_MULTIPLE_EXPRESSIONS,
        /* Lack of operator between two operands */
        ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS,
        /* Two decimal points in one number */
        ERROR_NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT,
        /* Scientific notation format is wrong */
        ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG,
        /* Does not match with number writing standard, not necessarily decimal */
        ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD,
        /* No-exist operator */
        ERROR_OPERATOR_NOT_EXIST,
        /* Unmatched right parenthese */
        ERROR_UNMATCHED_RIGHTPARENTHESE,
        /*Unmatched left parenthese */
        ERROR_UNMATCHED_LEFTPARENTHESE,
        /* Incorrect operand type */
        ERROR_WRONG_OPERAND_TYPE,
        /* Power function should return at least one root */
        ERROR_POWER_SHOULD_RETURN_AT_LEAST_ONE_ROOT,
        /* Incorrect answer of power function operand */
        ERROR_INCORRECT_ANSWER_OF_POWER_FUNCTION_OPERANDS,
        /* Incorrect binary operator */
        ERROR_INCORRECT_BINARY_OPERATOR,
        /* Incorrect monadic operator */
        ERROR_INCORRECT_MONADIC_OPERATOR,
        /* Lack of operand */
        ERROR_LACK_OPERAND,
        /* Can not identify character */
        ERROR_CAN_NOT_IDENTIFIED_CHARACTER,
        /* Undefined data type */
        ERROR_DATATYPE_IS_NOT_DEFINED,
        /* Can not change any other data type to a non-exist DATUM */
        ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST,
        /* Can not convert a nan value to boolean */
        ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN,
        /* Can not change a null DATUM to any other data type */
        ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE,
        /* Can not change data type from boolean */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_BOOLEAN,
        /* Can not change data type from integer */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_INTEGER,
        /* Can not change data type from double */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_DOUBLE,
        /* Can not change data type from complex */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX,
        /* Can not change data type from data reference */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE,
        /* Can not change data type from string */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_STRING,
        /* Can not change data type from function reference */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_FUNCTION_REFERENCE,
        /* Can not change data type from abstract expr */
        ERROR_CANNOT_CHANGE_DATATYPE_FROM_ABSTRACT_EXPR,
        /* Cannot calculate data array size */
        ERROR_CANNOT_CALCULATE_DATA_ARRAY_SIZE,
        /* Invalid data reference */
        ERROR_INVALID_DATA_REFERENCE,
        /* Invalid data abstract expr */
        ERROR_INVALID_DATA_ABSTRACT_EXPR,
        /* Matrix cannot recursively referred */
        ERROR_MATRIX_CANNOT_RECURSIVELY_REFERRED,
        /* Void data */
        ERROR_VOID_DATA,
        /* Wrong datatype */
        ERROR_WRONG_DATATYPE,
        /* Undefined new data type */
        ERROR_NEW_DATATYPE_IS_NOT_DEFINED,
        /* Invalid complex number */
        ERROR_INVALID_COMPLEX_NUMBER,
        /* Wrong index */
        ERROR_WRONG_INDEX,
        /* Invalid matrix size */
        ERROR_INVALID_MATRIX_SIZE,
        /* Matrix cannot be inverted */
        ERROR_MATRIX_CANNOT_BE_INVERTED,
        /* No answer for matrix division */
        ERROR_NO_ANSWER_FOR_MATRIX_DIVISION,
        /* Integer overflow */
        ERROR_INTEGER_OVERFLOW,
        /* Double overflow */
        ERROR_DOUBLE_OVERFLOW,
        /* Overflow when convert double to integer */
        ERROR_CHANGE_FROM_DOUBLE_TO_INTEGER_OVERFLOW,
        /* Too large operand of factorial */
        ERROR_OPERAND_OF_FACTORIAL_MAY_BE_TOO_LARGE,
        /* Zero division */
        ERROR_DIVISOR_CAN_NOT_BE_ZERO,
        /* Negative operand of factorial */
        ERROR_OPERAND_OF_FACTORIAL_CAN_NOT_LESS_THAN_ZERO,
        /* No-positive operand of bit operation */
        ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO,

        /* Incorrect number of parameters in a function */
        ERROR_INCORRECT_NUM_OF_PARAMETER,
        /* At least one of the parameters is out of the valid range.  */
        ERROR_INVALID_PARAMETER_RANGE,
        /* At least one of the parameters does not have right format.  */
        ERROR_INVALID_PARAMETER_FORMAT,
        /* At least one of the parameters is incorrect.  */
        ERROR_INVALID_PARAMETER,
        /* parameters do not match each other. */
        ERROR_PARAMETER_NOT_MATCH,
        /* The type of a parameter is invalid.  */
        ERROR_INVALID_PARAMETER_TYPE,
        /* The function is used but undefined.  */
        ERROR_UNDEFINED_FUNCTION,
        /* The function cannot be properly evaluated. */
        ERROR_FUNCTION_EVALUATION,
        /* The function cannot be processed. */
        ERROR_FUNCTION_PROCESSION,
        /* The function should return something but actually returns nothing. */
        ERROR_FUNCTION_RETURNS_NOTHING,
        
        /* The variable is used but undefined.  */
        ERROR_UNDEFINED_VARIABLE,
        /* cannot assign value to anything except variable, use equal instead */
        ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD,
        /* lack of index */
        ERROR_LACK_OF_INDEX,
        /* array dimension does not match */
        ERROR_ARRAY_DIM_DOES_NOT_MATCH,
        /* only able to transpose  1D or 2D matrix */
        ERROR_ONLY_SUPPORT_1D_2D_MATRIX_TRANSPOSE,
        /* only support 2D square matrix power */
        ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW,
        /* only support power of 2d square matrix */
        ERROR_ONLY_SQUARE_MATRIX_SUPPORTED_BY_POWER,
        /* not a string */
        ERROR_NOT_A_STRING,
        /* cannot find close quatation mark for string */
        ERROR_CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING,
        
        /* the result of an expression is indefinite */
        ERROR_INDEFINITE_RESULT,
        /* cannot get result of the expression */
        ERROR_CANNOT_GET_RESULT,
        /* calculation cannot converge */
        ERROR_CALCULATION_CANNOT_CONVERGE,
        
        /* cannot plot ogl chart lack system support files */
        ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES,
        /* cannot plot chart */
        ERROR_CANNOT_PLOT_CHART,
        /* cannot plot chart too many curves*/
        ERROR_CANNOT_PLOT_CHART_TOO_MANY_CURVES,
        /* curve should have at least one point */
        ERROR_CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT,
        /* too many points to plot in a curve */
        ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE,
        
        /* invalid file open mode */
        ERROR_INVALID_FILE_OPEN_MODE,
        /* illegal input format */
        ERROR_ILLEGAL_INPUT_FORMAT,
        /* invalid number format */
        ERROR_INVALID_NUMBER_FORMAT,
        /* illegal output format */
        ERROR_ILLEGAL_OUTPUT_FORMAT,
        /* too many open files */
        ERROR_TOO_MANY_OPEN_FILES,
        /* file not found */
        ERROR_FILE_NOT_FOUND,
        /* invalid file */
        ERROR_INVALID_FILE,
        /* cannot open file */
        ERROR_CANNOT_OPEN_FILE,
        /* cannot read file */
        ERROR_CANNOT_READ_FILE,
        /* cannot write file */
        ERROR_CANNOT_WRITE_FILE,
        /* cannot access file */
        ERROR_CANNOT_ACCESS_FILE,
        
        /* runtime error */
        ERROR_RUNTIME_ERROR,
        
        
        /* user defined error */
        ERROR_USER_DEFINED,
    }
    
    /* Definition of the error structure. */
    public static class StructError
    {
        public ERRORTYPES m_enumErrorType;
        public String m_strUserDefMsg;
        
        public String getErrorType()    {
            String strErrorType = "NO_EXCEPTION";
            switch(m_enumErrorType)    /* Find the corresponding error type. */
            {
            case ERROR_NO_EXPRESSION:
                strErrorType = "NO_EXPRESSION_EXCEPTION";
                break;
            case ERROR_MULTIPLE_EXPRESSIONS:
                strErrorType = "MULTIPLE_EXPRESSIONS_EXCEPTION";
                break;
            case ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS:
                strErrorType = "LACK_OPERATOR_BETWEEN_TWO_OPERANDS_EXCEPTION";
                break;
            case ERROR_NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT:
                strErrorType = "NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT_EXCEPTION";
                break;
            case ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG:
                strErrorType = "SCIENTIFIC_NOTATION_FORMAT_WRONG_EXCEPTION";
                break;
            case ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD:
                strErrorType = "NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD_EXCEPTION";
                break;
            case ERROR_OPERATOR_NOT_EXIST:    
                strErrorType = "OPERATOR_NOT_EXIST_EXCEPTION";
                break;
            case ERROR_UNMATCHED_RIGHTPARENTHESE:    
                strErrorType = "UNMATCHED_RIGHTPARENTHESE_EXCEPTION";
                break;
            case ERROR_UNMATCHED_LEFTPARENTHESE:
                strErrorType = "UNMATCHED_LEFTPARENTHESE_EXCEPTION";
                break;
            case ERROR_WRONG_OPERAND_TYPE:
                strErrorType = "WRONG_OPERAND_TYPE_EXCEPTION";
                break;
            case ERROR_POWER_SHOULD_RETURN_AT_LEAST_ONE_ROOT:
                strErrorType = "POWER_SHOULD_RETURN_AT_LEAST_ONE_ROOT_EXCEPTION";
                break;
            case ERROR_INCORRECT_ANSWER_OF_POWER_FUNCTION_OPERANDS:
                strErrorType = "INCORRECT_ANSWER_OF_POWER_FUNCTION_OPERANDS_EXCEPTION";
                break;
            case ERROR_INCORRECT_BINARY_OPERATOR:
                strErrorType = "INCORRECT_BINARY_OPERATOR_EXCEPTION";
                break;
            case ERROR_INCORRECT_MONADIC_OPERATOR:
                strErrorType = "INCORRECT_MONADIC_OPERATOR_EXCEPTION";
                break;
            case ERROR_LACK_OPERAND:
                strErrorType = "LACK_OPERAND_EXCEPTION";
                break;
            case ERROR_CAN_NOT_IDENTIFIED_CHARACTER:
                strErrorType = "CAN_NOT_IDENTIFIED_CHARACTER_EXCEPTION";
                break;
            case ERROR_DATATYPE_IS_NOT_DEFINED:
                strErrorType = "DATATYPE_IS_NOT_DEFINED_EXCEPTION";
                break;
            case ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST:
                strErrorType = "CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST_EXCEPTION";
                break;
            case ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN:
                strErrorType = "CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN_EXCEPTION";
                break;
            case ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE:
                strErrorType = "CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_BOOLEAN:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_BOOLEAN_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_INTEGER:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_INTEGER_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_DOUBLE:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_DOUBLE_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_COMPLEX_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_STRING:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_STRING_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_FUNCTION_REFERENCE:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_FUNCTION_REFERENCE_EXCEPTION";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_ABSTRACT_EXPR:
                strErrorType = "CANNOT_CHANGE_DATATYPE_FROM_ABSTRACT_EXPR_EXCEPTION";
                break;
            case ERROR_CANNOT_CALCULATE_DATA_ARRAY_SIZE:
                strErrorType = "CANNOT_CALCULATE_DATA_ARRAY_SIZE_EXCEPTION";
                break;
            case ERROR_INVALID_DATA_REFERENCE:
                strErrorType = "INVALID_DATA_REFERENCE_EXCEPTION";
                break;
            case ERROR_INVALID_DATA_ABSTRACT_EXPR:
                strErrorType = "INVALID_DATA_ABSTRACT_EXPR_EXCEPTION";
                break;
            case ERROR_MATRIX_CANNOT_RECURSIVELY_REFERRED:
                strErrorType = "ERROR_MATRIX_CANNOT_RECURSIVELY_REFERRED_EXCEPTION";
                break;
            case ERROR_VOID_DATA:
                strErrorType = "VOID_DATA_EXCEPTION";
                break;
            case ERROR_WRONG_DATATYPE:
                strErrorType = "WRONG_DATATYPE_EXCEPTION";
                break;
            case ERROR_NEW_DATATYPE_IS_NOT_DEFINED:
                strErrorType = "NEW_DATATYPE_IS_NOT_DEFINED_EXCEPTION";
                break;
            case ERROR_INVALID_COMPLEX_NUMBER:
                strErrorType = "INVALID_COMPLEX_NUMBER_EXCEPTION";
                break;
            case ERROR_WRONG_INDEX:
                strErrorType = "WRONG_INDEX_EXCEPTION";
                break;
            case ERROR_INVALID_MATRIX_SIZE:
                strErrorType = "INVALID_MATRIX_SIZE_EXCEPTION";
                break;
            case ERROR_MATRIX_CANNOT_BE_INVERTED:
                strErrorType = "MATRIX_CANNOT_BE_INVERTED_EXCEPTION";
                break;
            case ERROR_NO_ANSWER_FOR_MATRIX_DIVISION:
                strErrorType = "NO_ANSWER_FOR_MATRIX_DIVISION_EXCEPTION";
                break;
            case ERROR_INTEGER_OVERFLOW:
                strErrorType = "INTEGER_OVERFLOW_EXCEPTION";
                break;
            case ERROR_DOUBLE_OVERFLOW:
                strErrorType = "DOUBLE_OVERFLOW_EXCEPTION";
                break;
            case ERROR_CHANGE_FROM_DOUBLE_TO_INTEGER_OVERFLOW:
                strErrorType = "CHANGE_FROM_DOUBLE_TO_INTEGER_OVERFLOW_EXCEPTION";
                break;
            case ERROR_OPERAND_OF_FACTORIAL_MAY_BE_TOO_LARGE:
                strErrorType = "OPERAND_OF_FACTORIAL_MAY_BE_TOO_LARGE_EXCEPTION";
                break;
            case ERROR_DIVISOR_CAN_NOT_BE_ZERO:
                strErrorType = "DIVISOR_CAN_NOT_BE_ZERO_EXCEPTION";
                break;
            case ERROR_OPERAND_OF_FACTORIAL_CAN_NOT_LESS_THAN_ZERO:
                strErrorType = "OPERAND_OF_FACTORIAL_CAN_NOT_LESS_THAN_ZERO_EXCEPTION";
                break;
            case ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO:
                strErrorType = "OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO_EXCEPTION";
                break;
            case ERROR_INCORRECT_NUM_OF_PARAMETER:
                strErrorType = "INCORRECT_NUM_OF_PARAMETER_EXCEPTION";
                break;
            case ERROR_INVALID_PARAMETER_RANGE:
                strErrorType = "INVALID_PARAMETER_RANGE_EXCEPTION";
                break;
            case ERROR_INVALID_PARAMETER_FORMAT:
                strErrorType = "INVALID_PARAMETER_FORMAT_EXCEPTION";
                break;
            case ERROR_INVALID_PARAMETER:
                strErrorType = "INVALID_PARAMETER_EXCEPTION";
                break;
            case ERROR_PARAMETER_NOT_MATCH:
                strErrorType = "PARAMETER_NOT_MATCH_EXCEPTION";
                break;
            case ERROR_INVALID_PARAMETER_TYPE:
                strErrorType = "INVALID_PARAMETER_TYPE_EXCEPTION";
                break;
            case ERROR_UNDEFINED_FUNCTION:
                strErrorType = "UNDEFINED_FUNCTION_EXCEPTION";
                break;
            case ERROR_FUNCTION_EVALUATION:
                strErrorType = "FUNCTION_EVALUATION_EXCEPTION";
                break;
            case ERROR_FUNCTION_PROCESSION:
                strErrorType = "FUNCTION_PROCESSION_EXCEPTION";
                break;
            case ERROR_FUNCTION_RETURNS_NOTHING:
                strErrorType = "FUNCTION_RETURNS_NOTHING_EXCEPTION";
                break;
            case ERROR_UNDEFINED_VARIABLE:
                strErrorType = "UNDEFINED_VARIABLE_EXCEPTION";
                break;
            case ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD:
                strErrorType = "CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD_EXCEPTION";
                break;
            case ERROR_LACK_OF_INDEX:
                strErrorType = "LACK_OF_INDEX_EXCEPTION";
                break;
            case ERROR_ARRAY_DIM_DOES_NOT_MATCH:
                strErrorType = "ARRAY_DIM_DOES_NOT_MATCH_EXCEPTION";
                break;
            case ERROR_ONLY_SUPPORT_1D_2D_MATRIX_TRANSPOSE:
                strErrorType = "ONLY_SUPPORT_1D_2D_MATRIX_TRANSPOSE_EXCEPTION";
                break;
            case ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW:
                strErrorType = "MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW_EXCEPTION";
                break;
            case ERROR_ONLY_SQUARE_MATRIX_SUPPORTED_BY_POWER:
                strErrorType = "ONLY_SQUARE_MATRIX_SUPPORTED_BY_POWER_EXCEPTION";
                break;
            case ERROR_NOT_A_STRING:
                strErrorType = "NOT_A_STRING_EXCEPTION";
                break;
            case ERROR_CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING:
                strErrorType = "CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING_EXCEPTION";
                break;
            case ERROR_INDEFINITE_RESULT:
                strErrorType = "INDEFINITE_RESULT_EXCEPTION";
                break;
            case ERROR_CANNOT_GET_RESULT:
                strErrorType = "CANNOT_GET_RESULT_EXCEPTION";
                break;
            case ERROR_CALCULATION_CANNOT_CONVERGE:
                strErrorType = "CALCULATION_CANNOT_CONVERGE_EXCEPTION";
                break;
            case ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES:
                strErrorType = "ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES_EXCEPTION";
                break;
            case ERROR_CANNOT_PLOT_CHART:
                strErrorType = "CANNOT_PLOT_CHART_EXCEPTION";
                break;
            case ERROR_CANNOT_PLOT_CHART_TOO_MANY_CURVES:
                strErrorType = "CANNOT_PLOT_CHART_TOO_MANY_CURVES_EXCEPTION";
                break;
            case ERROR_CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT:
                strErrorType = "CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT_EXCEPTION";
                break;
            case ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE:
                strErrorType = "TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE_EXCEPTION";
                break;
            case ERROR_INVALID_FILE_OPEN_MODE:
                strErrorType = "INVALID_FILE_OPEN_MODE_EXCEPTION";
                break;
            case ERROR_ILLEGAL_INPUT_FORMAT:
                strErrorType = "ILLEGAL_INPUT_FORMAT_EXCEPTION";
                break;
            case ERROR_INVALID_NUMBER_FORMAT:
                strErrorType = "INVALID_NUMBER_FORMAT_EXCEPTION";
                break;
            case ERROR_ILLEGAL_OUTPUT_FORMAT:
                strErrorType = "ILLEGAL_OUTPUT_FORMAT_EXCEPTION";
                break;
            case ERROR_TOO_MANY_OPEN_FILES:
                strErrorType = "TOO_MANY_OPEN_FILES_EXCEPTION";
                break;
            case ERROR_FILE_NOT_FOUND:
                strErrorType = "FILE_NOT_FOUND_EXCEPTION";
                break;
            case ERROR_INVALID_FILE:
                strErrorType = "INVALID_FILE_EXCEPTION";
                break;
            case ERROR_CANNOT_OPEN_FILE:
                strErrorType = "CANNOT_OPEN_FILE_EXCEPTION";
                break;
            case ERROR_CANNOT_READ_FILE:
                strErrorType = "CANNOT_READ_FILE_EXCEPTION";
                break;
            case ERROR_CANNOT_WRITE_FILE:
                strErrorType = "CANNOT_WRITE_FILE_EXCEPTION";
                break;
            case ERROR_CANNOT_ACCESS_FILE:
                strErrorType = "CANNOT_ACCESS_FILE_EXCEPTION";
                break;
            case ERROR_RUNTIME_ERROR:
                strErrorType = "RUNTIME_ERROR_EXCEPTION";
                break;
            case ERROR_USER_DEFINED:
                strErrorType = "USER_DEFINED_EXCEPTION";
                break;
            default:
                ;    /*NO_ERROR_STATE returns "NO_EXCEPTION"*/
            }
        
            return strErrorType;            
        }
        
        public String getErrorInfo()    {
            /* Handle error. */
            String strErrorMsg = "";    /* The string to save error
                                                    information. */
        
            switch(m_enumErrorType)    /* Find the corresponding error type. */
            {
            case ERROR_NO_EXPRESSION:
                strErrorMsg = "No expression!";
                break;
            case ERROR_MULTIPLE_EXPRESSIONS:
                strErrorMsg = "Multiple expressions!";
                break;
            case ERROR_LACK_OPERATOR_BETWEEN_TWO_OPERANDS:
                strErrorMsg = "Lack of operator between two operands!";
                break;
            case ERROR_NUM_CAN_NOT_HAVE_TWO_DECIMAL_POINT:
                strErrorMsg = "Two decimal points in one number!";
                break;
            case ERROR_SCIENTIFIC_NOTATION_FORMAT_WRONG:
                strErrorMsg = "Scientific notation format is wrong!";
                break;
            case ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD:
                strErrorMsg = "Does not match number writing standard!";
                break;
            case ERROR_OPERATOR_NOT_EXIST:    
                strErrorMsg = "No-exist operator!";
                break;
            case ERROR_UNMATCHED_RIGHTPARENTHESE:    
                strErrorMsg = "Cannot find a left parnthese to match the right parenthese!";
                break;
            case ERROR_UNMATCHED_LEFTPARENTHESE:
                strErrorMsg = "Cannot find a right parnthese to match the left parenthese!";
                break;
            case ERROR_WRONG_OPERAND_TYPE:
                strErrorMsg = "Incorrect operand type!";
                break;
            case ERROR_POWER_SHOULD_RETURN_AT_LEAST_ONE_ROOT:
                strErrorMsg = "Power function should return at least one root!";
                break;
            case ERROR_INCORRECT_ANSWER_OF_POWER_FUNCTION_OPERANDS:
                strErrorMsg = "Incorrect answer of power function operand!";
                break;
            case ERROR_INCORRECT_BINARY_OPERATOR:
                strErrorMsg = "Incorrect binary operator!";
                break;
            case ERROR_INCORRECT_MONADIC_OPERATOR:
                strErrorMsg = "Incorrect monadic operator!";
                break;
            case ERROR_LACK_OPERAND:
                strErrorMsg = "Lack of operand!";
                break;
            case ERROR_CAN_NOT_IDENTIFIED_CHARACTER:
                strErrorMsg = "Can not identify character!";
                break;
            case ERROR_DATATYPE_IS_NOT_DEFINED:
                strErrorMsg = "Undefined data type!";
                break;
            case ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST:
                strErrorMsg = "Can not change to any other data type a non-exist datum!";
                break;
            case ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN:
                strErrorMsg = "Can not convert nan value to boolean!";
                break;
            case ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE:
                strErrorMsg = "Can not change a null DATUM to any other data type!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_BOOLEAN:
                strErrorMsg = "Can not change data type from boolean!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_INTEGER:
                strErrorMsg = "Can not change data type from integer!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_DOUBLE:
                strErrorMsg = "Can not change data type from double!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX:
                strErrorMsg = "Can not change data type from complex!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE:
                strErrorMsg = "Can not change data type from data reference!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_STRING:
                strErrorMsg = "Can not change data type from string!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_FUNCTION_REFERENCE:
                strErrorMsg = "Can not change data type from function reference!";
                break;
            case ERROR_CANNOT_CHANGE_DATATYPE_FROM_ABSTRACT_EXPR:
                strErrorMsg = "Can not change data type from abstract expr!";
                break;
            case ERROR_CANNOT_CALCULATE_DATA_ARRAY_SIZE:
                strErrorMsg = "Can not calculate data array size!";
                break;
            case ERROR_INVALID_DATA_REFERENCE:
                strErrorMsg = "Invalid data reference!";
                break;
            case ERROR_INVALID_DATA_ABSTRACT_EXPR:
                strErrorMsg = "Invalid abstract expr data!";
                break;
            case ERROR_MATRIX_CANNOT_RECURSIVELY_REFERRED:
                strErrorMsg = "Matrix cannot recursively referred!";
                break;
            case ERROR_VOID_DATA:
                strErrorMsg = "No data!";
                break;
            case ERROR_WRONG_DATATYPE:
                strErrorMsg = "Wrong data type!";
                break;
            case ERROR_NEW_DATATYPE_IS_NOT_DEFINED:
                strErrorMsg = "Undefined new data type!";
                break;
            case ERROR_INVALID_COMPLEX_NUMBER:
                strErrorMsg = "Invalid complex number!";
                break;
            case ERROR_WRONG_INDEX:
                strErrorMsg = "Wrong index!";
                break;
            case ERROR_INVALID_MATRIX_SIZE:
                strErrorMsg = "Invalid matrix size!";
                break;
            case ERROR_MATRIX_CANNOT_BE_INVERTED:
                strErrorMsg = "Matrix cannot be inverted!";
                break;
            case ERROR_NO_ANSWER_FOR_MATRIX_DIVISION:
                strErrorMsg = "No answer for matrix division!";
                break;
            case ERROR_INTEGER_OVERFLOW:
                strErrorMsg = "Integer overflow!";
                break;
            case ERROR_DOUBLE_OVERFLOW:
                strErrorMsg = "Double overflow!";
                break;
            case ERROR_CHANGE_FROM_DOUBLE_TO_INTEGER_OVERFLOW:
                strErrorMsg = "Overflow when convert double to integer!";
                break;
            case ERROR_OPERAND_OF_FACTORIAL_MAY_BE_TOO_LARGE:
                strErrorMsg = "Too large operand of factorial!";
                break;
            case ERROR_DIVISOR_CAN_NOT_BE_ZERO:
                strErrorMsg = "Zero division!";
                break;
            case ERROR_OPERAND_OF_FACTORIAL_CAN_NOT_LESS_THAN_ZERO:
                strErrorMsg = "Negative operand of factorial!";
                break;
            case ERROR_OPERAND_OF_BIT_OPERATION_MUST_BE_GREATER_THAN_ZERO:
                strErrorMsg = "No-positive operand of bit operation!";
                break;
            case ERROR_INCORRECT_NUM_OF_PARAMETER:
                strErrorMsg = "Incorrect number of parameters!";
                break;
            case ERROR_INVALID_PARAMETER_RANGE:
                strErrorMsg = "Invalid parameter range!";
                break;
            case ERROR_INVALID_PARAMETER_FORMAT:
                strErrorMsg = "Invalid parameter format!";
                break;
            case ERROR_INVALID_PARAMETER:
                strErrorMsg = "Invalid parameter!";
                break;
            case ERROR_PARAMETER_NOT_MATCH:
                strErrorMsg = "Parameters do not match each other.";
                break;
            case ERROR_INVALID_PARAMETER_TYPE:
                strErrorMsg = "Invalid parameter type!";
                break;
            case ERROR_UNDEFINED_FUNCTION:
                strErrorMsg = "Undefined function!";
                break;
            case ERROR_FUNCTION_EVALUATION:
                strErrorMsg = "Function cannot be properly be evaluated!";
                break;
            case ERROR_FUNCTION_PROCESSION:
                strErrorMsg = "Function cannot be be processed!";
                break;
            case ERROR_FUNCTION_RETURNS_NOTHING:
                strErrorMsg = "Function should return something but actually returns nothing!";
                break;
            case ERROR_UNDEFINED_VARIABLE:
                strErrorMsg = "Undefined variable!";
                break;
            case ERROR_CANNOT_ASSIGN_VALUE_TO_ANYTHING_EXCEPT_VARIALBE_USE_EQUAL_INSTEAD:
                strErrorMsg = "Cannot assign value to anything except variable. Please use equal instead!";
            case ERROR_LACK_OF_INDEX:
                strErrorMsg = "Lack of index!";
                break;
            case ERROR_ARRAY_DIM_DOES_NOT_MATCH:
                strErrorMsg = "Array dimension does not match!";
                break;
            case ERROR_ONLY_SUPPORT_1D_2D_MATRIX_TRANSPOSE:
                strErrorMsg = "Only able to transpose 1-D or 2-D matrix!";
                break;
            case ERROR_MATRIX_POWER_ONLY_SUPPORT_INTEGER_NOW:
                strErrorMsg = "Only support integer power of matrix at this stage!";
                break;
            case ERROR_ONLY_SQUARE_MATRIX_SUPPORTED_BY_POWER:
                strErrorMsg = "Only support power of 2-D square matrix!";
                break;
            case ERROR_NOT_A_STRING:
                strErrorMsg = "Not a string!";
                break;
            case ERROR_CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING:
                strErrorMsg = "Cannot find close quatation mark for string!";
                break;
            case ERROR_INDEFINITE_RESULT:
                strErrorMsg = "Result of the expression is indefinite!";
                break;
            case ERROR_CANNOT_GET_RESULT:
                strErrorMsg = "Cannot get result of the expression!";
                break;
            case ERROR_CALCULATION_CANNOT_CONVERGE:
                strErrorMsg = "Calculation cannot converge!";
                break;
            case ERROR_CANNOT_PLOT_OGL_CHART_LACK_SYSTEM_SUPPORT_FILES:
                strErrorMsg = "Cannot plot openGL chart because lack of platform related libraries! Please contact developer for the libraries.";
                break;
            case ERROR_CANNOT_PLOT_CHART:
                strErrorMsg = "Cannot plot chart!";
                break;
            case ERROR_CANNOT_PLOT_CHART_TOO_MANY_CURVES:
                strErrorMsg = "Cannot plot chart too many curves!";
                break;
            case ERROR_CURVE_SHOULD_HAVE_AT_LEAST_ONE_POINT:
                strErrorMsg = "Curve should have at least one point!";
                break;
            case ERROR_TOO_MANY_POINTS_TO_PLOT_IN_A_CURVE:
                strErrorMsg = "Too many points to plot in a curve!";
                break;
            case ERROR_INVALID_FILE_OPEN_MODE:
                strErrorMsg = "Invalid file open mode!";
                break;
            case ERROR_ILLEGAL_INPUT_FORMAT:
                strErrorMsg = "Illegal input format!";
                break;
            case ERROR_INVALID_NUMBER_FORMAT:
                strErrorMsg = "Invalid number format!";
                break;
            case ERROR_ILLEGAL_OUTPUT_FORMAT:
                strErrorMsg = "Illegal output format!";
                break;
            case ERROR_TOO_MANY_OPEN_FILES:
                strErrorMsg = "Too many open files!";
                break;
            case ERROR_FILE_NOT_FOUND:
                strErrorMsg = "File not found!";
                break;
            case ERROR_INVALID_FILE:
                strErrorMsg = "Invalid file!";
                break;
            case ERROR_CANNOT_OPEN_FILE:
                strErrorMsg = "Cannot open file!";
                break;
            case ERROR_CANNOT_READ_FILE:
                strErrorMsg = "Cannot read file!";
                break;
            case ERROR_CANNOT_WRITE_FILE:
                strErrorMsg = "Cannot write file!";
                break;
            case ERROR_CANNOT_ACCESS_FILE:
                strErrorMsg = "Cannot access file!";
                break;
            case ERROR_RUNTIME_ERROR:
                strErrorMsg = "Runtime error!";
                break;
            case ERROR_USER_DEFINED:
                strErrorMsg = m_strUserDefMsg;
                break;
            default:
                ;    /*NO_ERROR_STATE returns null*/
            };
        
            return strErrorMsg;
        }
    }
    
    public static class JFCALCExpErrException extends Exception    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        public StructError m_se = new StructError();
        public String m_strBlockName = null;
        public Exception m_exceptionLowerLevel = null;
        public JFCALCExpErrException()    {
            m_se.m_enumErrorType = ERRORTYPES.NO_ERROR_STATE;
            m_se.m_strUserDefMsg = "";
            m_strBlockName = "";
            m_exceptionLowerLevel = null;
        }
        public JFCALCExpErrException(ERRORTYPES e)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = "";
            m_strBlockName = null;
            m_exceptionLowerLevel = null;
        }
        public JFCALCExpErrException(ERRORTYPES e, String strBlockName, Exception exceptionLowerLevel)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg ="";
            m_strBlockName = strBlockName;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        public JFCALCExpErrException(ERRORTYPES e, String strUserDefMsg)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg = strUserDefMsg;
            m_strBlockName = null;
            m_exceptionLowerLevel = null;
        }
        public JFCALCExpErrException(ERRORTYPES e, String strUserDefMsg,
                String strBlockName, Exception exceptionLowerLevel)    {
            m_se.m_enumErrorType = e;
            m_se.m_strUserDefMsg =  strUserDefMsg;
            m_strBlockName = strBlockName;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
        public JFCALCExpErrException(StructError se)    {
            m_se.m_enumErrorType = se.m_enumErrorType;
            m_se.m_strUserDefMsg = se.m_strUserDefMsg;
            m_strBlockName = "";
            m_exceptionLowerLevel = null;
        }
        public JFCALCExpErrException(StructError se,
                String strBlockName, Exception exceptionLowerLevel)    {
            m_se.m_enumErrorType = se.m_enumErrorType;
            m_se.m_strUserDefMsg = se.m_strUserDefMsg;
            m_strBlockName = strBlockName;
            m_exceptionLowerLevel = exceptionLowerLevel;
        }
    }
}
