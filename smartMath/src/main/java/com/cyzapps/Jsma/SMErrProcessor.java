package com.cyzapps.Jsma;

public class SMErrProcessor {
	/* Definition of error types. */
	public static enum ERRORTYPES
	{
		/* No error */
		NO_ERROR_STATE,
		/* Invalid AbstractExpr */
		ERROR_INVALID_ABSTRACTEXPR,
        /* Invalid Result */
        ERROR_INVALID_RESULT,
		/* Can only assign value to variable */
		ERROR_CAN_ONLY_ASSIGN_VALUE_TO_VARIABLE,
		/* Variable undeclared */
		ERROR_VARIABLE_UNDECLARED,
        /* Variable redeclared */
        ERROR_VARIABLE_REDECLARED,
		/* Variable value not known */
		ERROR_VARIABLE_VALUE_NOT_KNOWN,
		/* Function in Abstract expression should return a value */
		ERROR_FUNCTION_IN_AEXPR_SHOULD_RETURN_A_VALUE,
		/* Not a constant abstract expression type */
		ERROR_NOT_CONSTANT_ABSTRACTEXPR,
		/* Incorrect abstract expression type */
		ERROR_INCORRECT_ABSTRACTEXPR_TYPE,
		/* Cannot merge two abstract expressions */
		ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS,
		/* Operator should have at least two operands */
		ERROR_OPERATOR_SHOULD_HAVE_AT_LEAST_TWO_OPERANDS,
		/* Only variable can be assigned a value */
		ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE,
		/* Pseudo-constant cannot be evaluated */
		ERROR_PSEUDO_CONST_CANNOT_BE_EVALUATED,
		/* Function undefined */
		ERROR_FUNCTION_UNDEFINED,
		/* Function cannot return nothing */
		ERROR_FUNCTION_CANNOT_RETURN_NOTHING,
		/* Only support number or 2D matrix calculation */
		ERROR_ONLY_SUPPORT_NUMBER_OR_2D_MATRIX_CALCULATION,
		/* Invalid operator */
		ERROR_INVALID_OPERATOR,
		/* Cannot calculate dimension */
		ERROR_CANNOT_CALCULATE_DIMENSION,
		/* Number of variables not match */
		ERROR_NUMBER_OF_VARIABLES_NOT_MATCH,
		/* Invalid abstract expr pattern */
		ERROR_INVALID_ABSTRACTEXPRPATTERN,
		/* Invalid variable or pseudoconst dimension */
		ERROR_INVALID_VARIABLE_OR_PSEUDOCONST_DIMENSION,
		/* Invalid pattern variable order */
		ERROR_INVALID_PATTERN_VARIABLE_ORDER,
		/* Invalid pattern unknown unit */
		ERROR_INVALID_PATTERN_UNKNOWN_UNIT,
		/* Redefined variable or pseudoconst */
		ERROR_REDEFINED_VARIABLE_OR_PSEUDOCONST,
		/* Variable cannot be solved */
		ERROR_VARIABLE_CANNOT_BE_SOLVED,
        /* Unsupported simple pattern */
        ERROR_UNSUPPORTED_SIMPLE_PATTERN,
        /* Invalid simple pattern */
        ERROR_INVALID_SIMPLE_PATTERN,
		/* Unrecognized pattern */
		ERROR_UNRECOGNIZED_PATTERN,
        /* Cannot fit restrict */
        ERROR_CANNOT_FIT_RESTRICT,
        /* Invalid integration */
        ERROR_INVALID_INTEGRATION,
        /* Invalid integration type */
        ERROR_INVALID_INTEGRATION_TYPE,
        /* Unsupported integration type */
        ERROR_UNSUPPORTED_INTEGRATION_TYPE,
        /* Invalid integration range */
        ERROR_INVALID_INTEGRATION_RANGE,
        /* Cannot solve calculation */
        ERROR_CANNOT_SOLVE_CALCULATION,
	}

	/* Definition of the error structure. */
	public static class StructError
	{
		public ERRORTYPES m_enumErrorType;
		public String m_strUserDefMsg;
		
		public String getErrorType()	{
			String strErrorType = "NO_EXCEPTION";
			switch(m_enumErrorType)	/* Find the corresponding error type. */
			{
			case ERROR_INVALID_ABSTRACTEXPR:
				strErrorType = "INVALID_ABSTRACT_EXPR_TYPE_EXCEPTION";
				break;
            case ERROR_INVALID_RESULT:
                strErrorType = "INVALID_RESULT";
				break;
			case ERROR_CAN_ONLY_ASSIGN_VALUE_TO_VARIABLE:
				strErrorType = "CAN_ONLY_ASSIGN_VALUE_TO_VARIABLE";
				break;
			case ERROR_VARIABLE_UNDECLARED:
				strErrorType = "VARIABLE_UNDECLARED";
				break;
			case ERROR_VARIABLE_REDECLARED:
				strErrorType = "VARIABLE_REDECLARED";
				break;
			case ERROR_VARIABLE_VALUE_NOT_KNOWN:
				strErrorType = "VARIABLE_VALUE_NOT_KNOWN";
				break;
			case ERROR_FUNCTION_IN_AEXPR_SHOULD_RETURN_A_VALUE:
				strErrorType = "FUNCTION_IN_AEXPR_SHOULD_RETURN_A_VALUE";
				break;
			case ERROR_NOT_CONSTANT_ABSTRACTEXPR:
				strErrorType = "NOT_CONSTANT_ABSTRACTEXPR";
				break;
			case ERROR_INCORRECT_ABSTRACTEXPR_TYPE:
				strErrorType = "INCORRECT_ABSTRACTEXPR_TYPE";
				break;
			case ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS:
				strErrorType = "CANNOT_MERGE_TWO_ABSTRACTEXPRS";
				break;
			case ERROR_OPERATOR_SHOULD_HAVE_AT_LEAST_TWO_OPERANDS:
				strErrorType = "OPERATOR_SHOULD_HAVE_AT_LEAST_TWO_OPERANDS";
				break;
			case ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE:
				strErrorType = "ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE";
				break;
			case ERROR_PSEUDO_CONST_CANNOT_BE_EVALUATED:
				strErrorType = "PSEUDO_CONST_CANNOT_BE_EVALUATED";
				break;
			case ERROR_FUNCTION_UNDEFINED:
				strErrorType = "FUNCTION_UNDEFINED";
				break;
			case ERROR_FUNCTION_CANNOT_RETURN_NOTHING:
				strErrorType = "FUNCTION_CANNOT_RETURN_NOTHING";
				break;
			case ERROR_ONLY_SUPPORT_NUMBER_OR_2D_MATRIX_CALCULATION:
				strErrorType= "ONLY_SUPPORT_NUMBER_OR_2D_MATRIX_CALCULATION";
				break;
			case ERROR_INVALID_OPERATOR:
				strErrorType = "INVALID_OPERATOR";
				break;
			case ERROR_CANNOT_CALCULATE_DIMENSION:
				strErrorType = "CANNOT_CALCULATE_DIMENSION";
				break;
			case ERROR_NUMBER_OF_VARIABLES_NOT_MATCH:
				strErrorType = "NUMBER_OF_VARIABLES_NOT_MATCH";
				break;
			case ERROR_INVALID_ABSTRACTEXPRPATTERN:
				strErrorType = "INVALID_ABSTRACTEXPRPATTERN";
				break;
			case ERROR_INVALID_VARIABLE_OR_PSEUDOCONST_DIMENSION:
				strErrorType = "INVALID_VARIABLE_OR_PSEUDOCONST_DIMENSION";
				break;
			case ERROR_INVALID_PATTERN_VARIABLE_ORDER:
				strErrorType = "INVALID_PATTERN_VARIABLE_ORDER";
				break;
			case ERROR_INVALID_PATTERN_UNKNOWN_UNIT:
				strErrorType = "INVALID_PATTERN_UNKNOWN_UNIT";
				break;
			case ERROR_REDEFINED_VARIABLE_OR_PSEUDOCONST:
				strErrorType = "REDEFINED_VARIABLE_OR_PSEUDOCONST";
				break;
			case ERROR_VARIABLE_CANNOT_BE_SOLVED:
				strErrorType = "VARIABLE_CANNOT_BE_SOLVED";
				break;
			case ERROR_UNSUPPORTED_SIMPLE_PATTERN:
				strErrorType = "UNSUPPORTED_SIMPLE_PATTERN";
				break;
			case ERROR_INVALID_SIMPLE_PATTERN:
				strErrorType = "INVALID_SIMPLE_PATTERN";
				break;
			case ERROR_UNRECOGNIZED_PATTERN:
				strErrorType = "UNRECOGNIZED_PATTERN";
				break;
			case ERROR_CANNOT_FIT_RESTRICT:
				strErrorType = "CANNOT_FIT_RESTRICT";
				break;
			case ERROR_INVALID_INTEGRATION:
				strErrorType = "INVALID_INTEGRATION";
				break;
			case ERROR_INVALID_INTEGRATION_TYPE:
				strErrorType = "INVALID_INTEGRATION_TYPE";
				break;
			case ERROR_UNSUPPORTED_INTEGRATION_TYPE:
				strErrorType = "UNSUPPORTED_INTEGRATION_TYPE";
				break;
			case ERROR_INVALID_INTEGRATION_RANGE:
				strErrorType = "INVALID_INTEGRATION_RANGE";
				break;
			case ERROR_CANNOT_SOLVE_CALCULATION:
				strErrorType = "CANNOT_SOLVE_CALCULATION";
				break;
			default:
				;	/*NO_ERROR_STATE returns "NO_EXCEPTION"*/
			}
		
			return strErrorType;			
		}
		
		public String getErrorInfo()	{
			/* Handle error. */
			String strErrorMsg = "";	/* The string to save error
													information. */
		
			switch(m_enumErrorType)	/* Find the corresponding error type. */
			{
			case ERROR_INVALID_ABSTRACTEXPR:
				strErrorMsg = "Invalid abstract expression type!";
				break;
			case ERROR_INVALID_RESULT:
				strErrorMsg = "Invalid result!";
				break;
			case ERROR_CAN_ONLY_ASSIGN_VALUE_TO_VARIABLE:
				strErrorMsg = "Can only assign value to variable!";
				break;
			case ERROR_VARIABLE_UNDECLARED:
				strErrorMsg = "Variable undeclared!";
				break;
			case ERROR_VARIABLE_REDECLARED:
				strErrorMsg = "Variable redeclared!";
				break;
			case ERROR_VARIABLE_VALUE_NOT_KNOWN:
				strErrorMsg = "Variable value not known!";
				break;
			case ERROR_FUNCTION_IN_AEXPR_SHOULD_RETURN_A_VALUE:
				strErrorMsg = "Function in abstract expression should return a value!";
				break;
			case ERROR_NOT_CONSTANT_ABSTRACTEXPR:
				strErrorMsg = "Not a constant abstract expression type!";
				break;
			case ERROR_INCORRECT_ABSTRACTEXPR_TYPE:
				strErrorMsg = "Incorrect abstract expression type!";
				break;
			case ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS:
				strErrorMsg = "Cannot merge two abstract expressions!";
				break;
			case ERROR_OPERATOR_SHOULD_HAVE_AT_LEAST_TWO_OPERANDS:
				strErrorMsg = "Operator should have at least two operands!";
				break;
			case ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE:
				strErrorMsg = "Only variable can be assigned a value!";
				break;
			case ERROR_PSEUDO_CONST_CANNOT_BE_EVALUATED:
				strErrorMsg = "Pseudo-constant cannot be evaluated!";
				break;
			case ERROR_FUNCTION_UNDEFINED:
				strErrorMsg = "Function undefined!";
				break;
			case ERROR_FUNCTION_CANNOT_RETURN_NOTHING:
				strErrorMsg = "Function cannot return nothing!";
				break;
			case ERROR_ONLY_SUPPORT_NUMBER_OR_2D_MATRIX_CALCULATION:
				strErrorMsg= "Only support number or 2D matrix calculation!";
				break;
			case ERROR_INVALID_OPERATOR:
				strErrorMsg = "Invalid operator!";
				break;
			case ERROR_CANNOT_CALCULATE_DIMENSION:
				strErrorMsg = "Cannot calculate dimension!";
				break;
			case ERROR_NUMBER_OF_VARIABLES_NOT_MATCH:
				strErrorMsg = "Number of variables not match!";
				break;
			case ERROR_INVALID_ABSTRACTEXPRPATTERN:
				strErrorMsg = "Invalid abstract expression type!";
				break;
			case ERROR_INVALID_VARIABLE_OR_PSEUDOCONST_DIMENSION:
				strErrorMsg = "Invalid variable or pseudoconst dimension!";
				break;
			case ERROR_INVALID_PATTERN_VARIABLE_ORDER:
				strErrorMsg = "Invalid pattern variable order!";
				break;
			case ERROR_INVALID_PATTERN_UNKNOWN_UNIT:
				strErrorMsg = "Invalid pattern unknown unit!";
				break;
			case ERROR_REDEFINED_VARIABLE_OR_PSEUDOCONST:
				strErrorMsg = "Redefined variable or pseudo constant!";
				break;
			case ERROR_VARIABLE_CANNOT_BE_SOLVED:
				strErrorMsg = "Variable cannot be solved!";
				break;
			case ERROR_UNSUPPORTED_SIMPLE_PATTERN:
				strErrorMsg = "Unsupported simple pattern!";
				break;
			case ERROR_INVALID_SIMPLE_PATTERN:
				strErrorMsg = "Invalid simple pattern!";
				break;
			case ERROR_UNRECOGNIZED_PATTERN:
				strErrorMsg = "Unrecognized pattern!";
				break;
			case ERROR_CANNOT_FIT_RESTRICT:
				strErrorMsg = "Cannot fit restrict!";
				break;
			case ERROR_INVALID_INTEGRATION:
				strErrorMsg = "Invalid integration!";
				break;
			case ERROR_INVALID_INTEGRATION_TYPE:
				strErrorMsg = "Invalid integration type!";
				break;
			case ERROR_UNSUPPORTED_INTEGRATION_TYPE:
				strErrorMsg = "Unsupported integration type!";
				break;
			case ERROR_INVALID_INTEGRATION_RANGE:
				strErrorMsg = "Invalid integration range!";
				break;
			case ERROR_CANNOT_SOLVE_CALCULATION:
				strErrorMsg = "Cannot solve calculation!";
				break;
			default:
				;	/*NO_ERROR_STATE returns null*/
			};
		
			return strErrorMsg;
		}
	}
	
	public static class JSmartMathErrException extends Exception	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public StructError m_se = new StructError();
		public String m_strBlockName = null;
		public Exception m_exceptionLowerLevel = null;
		public JSmartMathErrException()	{
			m_se.m_enumErrorType = ERRORTYPES.NO_ERROR_STATE;
			m_se.m_strUserDefMsg = "";
			m_strBlockName = "";
			m_exceptionLowerLevel = null;
		}
		public JSmartMathErrException(ERRORTYPES e)	{
			m_se.m_enumErrorType = e;
			m_se.m_strUserDefMsg = "";
			m_strBlockName = null;
			m_exceptionLowerLevel = null;
		}
		public JSmartMathErrException(ERRORTYPES e, String strBlockName, Exception exceptionLowerLevel)	{
			m_se.m_enumErrorType = e;
			m_se.m_strUserDefMsg ="";
			m_strBlockName = strBlockName;
			m_exceptionLowerLevel = exceptionLowerLevel;
		}
		public JSmartMathErrException(ERRORTYPES e, String strUserDefMsg)	{
			m_se.m_enumErrorType = e;
			m_se.m_strUserDefMsg = strUserDefMsg;
			m_strBlockName = null;
			m_exceptionLowerLevel = null;
		}
		public JSmartMathErrException(ERRORTYPES e, String strUserDefMsg,
				String strBlockName, Exception exceptionLowerLevel)	{
			m_se.m_enumErrorType = e;
			m_se.m_strUserDefMsg =  strUserDefMsg;
			m_strBlockName = strBlockName;
			m_exceptionLowerLevel = exceptionLowerLevel;
		}
		public JSmartMathErrException(StructError se)	{
			m_se.m_enumErrorType = se.m_enumErrorType;
			m_se.m_strUserDefMsg = se.m_strUserDefMsg;
			m_strBlockName = "";
			m_exceptionLowerLevel = null;
		}
		public JSmartMathErrException(StructError se,
				String strBlockName, Exception exceptionLowerLevel)	{
			m_se.m_enumErrorType = se.m_enumErrorType;
			m_se.m_strUserDefMsg = se.m_strUserDefMsg;
			m_strBlockName = strBlockName;
			m_exceptionLowerLevel = exceptionLowerLevel;
		}
	}
}
