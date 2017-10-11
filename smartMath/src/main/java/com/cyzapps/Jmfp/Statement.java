package com.cyzapps.Jmfp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Locale;

import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;

public class Statement {
	public int m_nStartLineNo = 0;	// which line does this statement start? note that line no starts from 1.
	public int m_nEndLineNo = 0;	// which line does this statement end? note that line no starts from 1.
	public Exception m_eAnalyze = null; // the exception occurred in analyzing the statement when loading the file.
    private String m_strStatement = "";	// trimmed lower-case statement.
	private String m_strHelpTail = "";	// the help information at the tail.
	
	//goToStringEnd returns the position of character after " even if " is the end of strLine
	// nStringStart is the position of beginning char ".
	public int goToStringEnd(String strLine, int nStringStart)
	{
		if (strLine == null || nStringStart >= strLine.length())	{
			return -1;
		}
		// we always assume char[nStringStart] is '\'' or '"'.
		char cStartChar = strLine.charAt(nStringStart);
		char cEscapeChar = '\\';
		boolean bInEscapeMode = false;
		int i;
		String strValue = "";
		for (i = nStringStart + 1; i < strLine.length(); i++)
		{
			if (strLine.charAt(i) == cEscapeChar && !bInEscapeMode)	{
				bInEscapeMode = true;
			} else if (bInEscapeMode)	{
				switch (strLine.charAt(i))	{
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
					strValue += strLine.charAt(i);
				}
				bInEscapeMode = false;
			} else if (strLine.charAt(i) == cStartChar)	{
				break;
			} else	{
				strValue += strLine.charAt(i);
			}
		}
		if (i >= strLine.length())	{
			// no close quotation mark for the string
			return -1;
		}
		return i + 1;
	}

	public boolean isFinishedStatement()	{
		// identify if the statement finished or not. Assume m_strStatement has been trimmed.
		// if the last two characters of a statement is " _", the statement is not finished.
		if (m_strStatement.length() > 2
				&& m_strStatement.substring(m_strStatement.length() - 2).equals(" _"))	{
			return false;
		}
		return true;
	}
	
	public boolean concatenate(Statement sNext)	{
		if (isFinishedStatement())	{
			return false;	// this statement has finished so that can not be concatenate
		} else if(m_nEndLineNo >= sNext.m_nStartLineNo)	{
			return false;	// the two statements have overlapped lines, something wrong.
		} else {
			m_nEndLineNo = sNext.m_nEndLineNo;
			m_strStatement = m_strStatement.substring(0, m_strStatement.length() - 1) + sNext.m_strStatement;
			m_strHelpTail = m_strHelpTail + "\n" + sNext.m_strHelpTail;
			return true;
		}
	}
	
	public Statement(String strStatement, int nLineNo)	{
		m_nStartLineNo = m_nEndLineNo = nLineNo;
		setStatement(strStatement);
		// Log.e("statement", "original is " + strStatement + " statement is " + m_strStatement + " help tail is " + m_strHelpTail);
	}
	
	public final void setStatement(String strStatement) {
		if (strStatement == null)	{
			m_strStatement = "";
			m_strHelpTail = "";
		} else	{
			int index = 0;
			while(index < strStatement.length())	{
				if (strStatement.charAt(index) == '"')	{
					// should be the start of a string
					if ((index = goToStringEnd(strStatement, index)) == -1)	{	// index could be strStatement.length() (beyond the last char)
						// something wrong with the statement, string has no end.
						m_strStatement = strStatement.trim();
						m_strHelpTail = "";
						return;
					}					
				} else if (strStatement.charAt(index) == '/'
							&& index < (strStatement.length() - 1) && strStatement.charAt(index + 1) == '/')	{
					// comment starts.
					m_strStatement = strStatement.substring(0, index).trim();
					m_strHelpTail = strStatement.substring(index + 2);
					return;
				} else	{
					index ++;
				}
			}
			// no // help found.
			m_strStatement = strStatement.trim();
			m_strHelpTail = "";
		}
	}
	
	public String getStatement()	{
		return m_strStatement;
	}
	
	public String getHelpTail()	{
		return m_strHelpTail;
	}
	
	// note that m_statementType in the derived object should always be no-null.
	public StatementType m_statementType = null;	// this variable stores the type of the statement.
	
	public abstract class StatementType	{
		protected String m_strType = "";	// what is the statement
		
		public String getType()	{
			return m_strType;
		}
		
		/*
		 * we always assume that strStatement has been trimmed and uncapitalized.
		 * and the statement type has been correctly identified.
		 */
		protected abstract void analyze(String strStatement)
				throws ErrorProcessor.JMFPCompErrException;
		
	}
	
	public class Statement_function extends StatementType {
		Statement_function() {
			m_strType = "function";
		}
		public String m_strFunctionName;
		public String[] m_strParams = new String[0];	// parameter list
		public boolean m_bIncludeOptParam = false;	// include optional parameters?
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException	{
			String strFunctionDef = strStatement.substring("function".length()).trim();	// function name defined from char 8.
			m_strFunctionName = strFunctionDef.split("\\(")[0].trim().toLowerCase(Locale.US);    // trim to prevent declaration like mytest ()

            int nValidationResult = validateVarOrFuncName(m_strFunctionName);
            if (nValidationResult == 1) {
				ERRORTYPES e = ERRORTYPES.BAD_FUNCTION_NAME;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
            } else if (nValidationResult == 2) {
				ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
            }
			
			String strParamList = strFunctionDef.substring(m_strFunctionName.length()).trim();
			if (strParamList.length() < 2)	{
				// parameter list should at least be made up of '(' and ')'
				ERRORTYPES e = ERRORTYPES.NO_PARAMETER_DEFINITION_BORDER;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);				
			} else if (strParamList.charAt(0) != '(' || strParamList.charAt(strParamList.length() - 1) != ')')	{
				// parameter list should at least be made up of '(' and ')'
				ERRORTYPES e = ERRORTYPES.NO_PARAMETER_DEFINITION_BORDER;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);				
			}
			strParamList = strParamList.substring(1, strParamList.length()-1).trim();
			if (strParamList.equals("") == false)	{
				m_strParams = strParamList.split(",");
				for (int index = 0; index < m_strParams.length; index ++)	{
					m_strParams[index] = m_strParams[index].trim().toLowerCase(Locale.US);
                    if (m_strParams[index].equals("...") && (index == m_strParams.length - 1))	{
						m_strParams[index] = "opt_argv";
						m_bIncludeOptParam = true;
					} else  {
                        nValidationResult = validateVarOrFuncName(m_strParams[index]);
                        if (nValidationResult == 1) {
                            ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                            throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                        } else if (nValidationResult == 2) {
                            ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                            throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                        }
                    }
				}
			}
		}
	}
	
	public class Statement_endf extends StatementType {
		Statement_endf() {
			m_strType = "endf";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("endf") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_return extends StatementType {
		Statement_return() {
			m_strType = "return";
		}

		public String m_strReturnedExpr;	// we assume that a function should always return something.
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strReturnedExpr = strStatement.substring("return".length()).trim();
			/*
			 * function needs not necessarily return something.
			if (m_strReturnedExpr.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_RETURN_IN_FUNCTION;
				throw new JMFPCompErrException(m_nLineNo, e);
			}
			*/
		}
	}
	
	public class Statement_variable extends StatementType {
		Statement_variable() {
			m_strType = "variable";
		}

		public String[] m_strVariables = new String[0];	// variable list
		public String[] m_strVarValues = new String[0]; // variable initial values

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			String strVariableList = strStatement.substring("variable".length()).trim();
			if (strVariableList.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_VARIABLE;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			
			LinkedList<String> listVariableDefs = new LinkedList<String>();
			int nRoundBracketLevel = 0;
			int nSquareBracketLevel = 0;
			int nBraceBracketLevel = 0;
			int nLastComma = -1;
			for (int index = 0; index < strVariableList.length(); index ++)	{
				if (strVariableList.charAt(index) == '"')	{
					index = goToStringEnd(strVariableList, index);	//goToStringEnd returns the position of character after "
                    if (index == -1)    {
                        ERRORTYPES e = ERRORTYPES.CANNOT_FIND_CLOSE_QUATATION_MARK_FOR_STRING;
                        throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                    } else if (index >= strVariableList.length())   {
						listVariableDefs.add(strVariableList.substring(nLastComma + 1));
                        break;
                    }
                }
				if (strVariableList.charAt(index) == '(')	{
					nRoundBracketLevel ++;
				} else if (strVariableList.charAt(index) == ')')	{
					nRoundBracketLevel --;
				} else if (strVariableList.charAt(index) == '[')	{
					nSquareBracketLevel ++;
				} else if (strVariableList.charAt(index) == ']')	{
					nSquareBracketLevel --;
				} else if (strVariableList.charAt(index) == '{')	{
					nBraceBracketLevel ++;
				} else if (strVariableList.charAt(index) == '}')	{
					nBraceBracketLevel --;
				}
				if (nRoundBracketLevel < 0 || nSquareBracketLevel < 0 || nBraceBracketLevel < 0)	{
					ERRORTYPES e = ERRORTYPES.NO_OPEN_BRACKET;
					throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
				} else if (nRoundBracketLevel > 0 || nSquareBracketLevel > 0 || nBraceBracketLevel > 0)	{
					continue;
				} else	{	// not in any brackets.
					if (strVariableList.charAt(index) == ',')	{
						listVariableDefs.add(strVariableList.substring(nLastComma + 1, index));
						nLastComma = index;
					} else if (index == strVariableList.length() - 1)	{
						listVariableDefs.add(strVariableList.substring(nLastComma + 1));
					}
				}
			}
			if (nRoundBracketLevel > 0 || nSquareBracketLevel > 0 || nBraceBracketLevel > 0)	{
				ERRORTYPES e = ERRORTYPES.NO_CLOSE_BRACKET;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			if (listVariableDefs.size() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_VARIABLE;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			m_strVariables = new String[listVariableDefs.size()];
			m_strVarValues = new String[listVariableDefs.size()];
			for (int idx = 0; idx < listVariableDefs.size(); idx ++)	{
				String[] strlistVarDef = listVariableDefs.get(idx).split("=");
				if (strlistVarDef.length == 0)	{
					ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
					throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
				} else	{
					m_strVariables[idx] = strlistVarDef[0].trim().toLowerCase(Locale.US);
                    int nValidationResult = validateVarOrFuncName(m_strVariables[idx]);
                    if (nValidationResult == 1) {
                        ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                        throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                    } else if (nValidationResult == 2) {
                        ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                        throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                    }
					for (int idx1 = 0; idx1 < idx; idx1 ++)	{
						if (m_strVariables[idx1].equalsIgnoreCase(m_strVariables[idx]))	{
							ERRORTYPES e = ERRORTYPES.REDEFINED_VARIABLE;
							throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
						}
					}
					if (strlistVarDef[0].length() == listVariableDefs.get(idx).length())	{
						// no =
						m_strVarValues[idx] = "";
					} else {
						// character listVariableDefs.charAt(strlistVarDef[0].length()) must be =
						m_strVarValues[idx] = listVariableDefs.get(idx).substring(strlistVarDef[0].length() + 1).trim();
						if (m_strVarValues[idx].length() == 0)	{
							ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
							throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
						}
					}
				}
			}
		}
	}
	
	public class Statement_if extends StatementType {
		Statement_if() {
			m_strType = "if";
		}

		public String m_strCondition;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strCondition = strStatement.substring("if".length()).trim();
			if (m_strCondition.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_CONDITION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}			
		}
	}
	
	public class Statement_elseif extends StatementType {
		Statement_elseif() {
			m_strType = "elseif";
		}

		public String m_strCondition;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strCondition = strStatement.substring("elseif".length()).trim();
			if (m_strCondition.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_CONDITION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}			
		}
	}

	public class Statement_else extends StatementType {
		Statement_else() {
			m_strType = "else";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("else") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			
		}
	}

	public class Statement_endif extends StatementType {
		Statement_endif() {
			m_strType = "endif";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("endif") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			
		}
	}
	
	public class Statement_while extends StatementType {
		Statement_while() {
			m_strType = "while";
		}

		public String m_strCondition;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strCondition = strStatement.substring("while".length()).trim();
			if (m_strCondition.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_CONDITION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}			
		}
	}
	
	public class Statement_loop extends StatementType {
		Statement_loop() {
			m_strType = "loop";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("loop") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			
		}
	}
	
	public class Statement_do extends StatementType {
		Statement_do() {
			m_strType = "do";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("do") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			
		}
	}
	
	public class Statement_until extends StatementType {
		Statement_until() {
			m_strType = "until";
		}

		public String m_strCondition;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strCondition = strStatement.substring("until".length()).trim();
			if (m_strCondition.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NO_CONDITION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}			
		}
	}
	
	public class Statement_for extends StatementType {
		Statement_for() {
			m_strType = "for";
		}

		public String m_strIndexName;
		boolean m_bLocalDefIndex;
		public String m_strStart;
		public String m_strEnd;
		public String m_strStep;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			/*
			 * for statement should be something like
			 * for [variable ]index = expr1 to expr2[ step expr3]
			 * where [] means optional.
			 * Because there should not be "" enclosed string in a for statement,
			 * for strAfterFor is converted to lower case at the very beginning
			 */
			String strAfterFor = strStatement.substring("for".length()).trim().toLowerCase(Locale.US);
			String[] strDividedByTos = strAfterFor.split("[ \\t]to[ \\t]");
			if (strDividedByTos.length != 2)	{
				ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			m_bLocalDefIndex = false;	// locally defined index variable?
			if (strDividedByTos[0].length() >= "variable".length()
                    && strDividedByTos[0].substring(0, "variable".length()).equals("variable"))	{
				m_bLocalDefIndex = true;
				strDividedByTos[0] = strDividedByTos[0].substring("variable".length()).trim();
			}
			String[] strDividedByEqs = strDividedByTos[0].split("=");
			if (strDividedByEqs.length < 2)	{
				ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			m_strIndexName = strDividedByEqs[0].trim();
            int nValidationResult = validateVarOrFuncName(m_strIndexName);
            if (nValidationResult == 1) {
                ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
            } else if (nValidationResult == 2) {
                ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
            }
			/*
			 * In the following statement, substring(m_strIndexName.length()) removes the index name,
			 * substring(1) removes '='.
			 */
			m_strStart = strDividedByTos[0].trim().substring(m_strIndexName.length()).trim().substring(1);
			if (m_strStart.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
			
			String[] strDividedBySteps = strDividedByTos[1].split("[ \\t]step[ \\t]");
			if (strDividedBySteps.length == 1)	{
				// By default, step = 1.
				m_strEnd = strDividedBySteps[0].trim();
				m_strStep = "1";
				if (m_strEnd.length() == 0)	{
					ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
					throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
				}
			} else if (strDividedBySteps.length == 2)	{
				m_strEnd = strDividedBySteps[0].trim();
				m_strStep = strDividedBySteps[1].trim();
				if (m_strEnd.length() == 0 || m_strStep.length() == 0)	{
					ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
					throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
				}
			} else	{
				ERRORTYPES e = ERRORTYPES.INVALID_FOR_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_next extends StatementType {
		Statement_next() {
			m_strType = "next";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("next") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_break extends StatementType {
		Statement_break() {
			m_strType = "break";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("break") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_continue extends StatementType {
		Statement_continue() {
			m_strType = "continue";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("continue") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_select extends StatementType {
		Statement_select() {
			m_strType = "select";
		}
		
		public String m_strSelectedExpr;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strSelectedExpr = strStatement.substring("select".length()).trim();
			if (m_strSelectedExpr.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_case extends StatementType {
		Statement_case() {
			m_strType = "case";
		}

		public String m_strCaseExpr;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strCaseExpr = strStatement.substring("case".length()).trim();
			if (m_strCaseExpr.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_default extends StatementType {
		Statement_default() {
			m_strType = "default";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("default") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_ends extends StatementType {
		Statement_ends() {
			m_strType = "ends";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("ends") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_try extends StatementType	{
		Statement_try()	{
			m_strType = "try";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("try") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_throw extends StatementType {
		Statement_throw() {
			m_strType = "throw";
		}

		public String m_strThrownExpr;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strThrownExpr = strStatement.substring("throw".length()).trim();
			if (m_strThrownExpr.length() == 0)	{
				ERRORTYPES e = ERRORTYPES.NEED_EXPRESSION;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_catch extends StatementType	{
		Statement_catch()	{
			m_strType = "catch";
		}

		public String m_strFilter;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strFilter = strStatement.substring("catch".length()).trim();
			// No condition is allowed.
		}
	}
	
	public class Statement_endtry extends StatementType	{
		Statement_endtry()	{
			m_strType = "endtry";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("endtry") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_solve extends StatementType	{
		Statement_solve()	{
			m_strType = "solve";
		}

		public String[] m_strVariables = new String[0];	// variable list

        @Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
            
            String strVariableList = strStatement.toLowerCase(Locale.US).substring("solve".length()).trim();
            m_strVariables = strVariableList.split(",");
            for (int idx = 0; idx < m_strVariables.length; idx ++)   {
                m_strVariables[idx] = m_strVariables[idx].trim();
                int nValidationResult = validateVarOrFuncName(m_strVariables[idx]);
                if (nValidationResult == 1) {
                    ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                    throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                } else if (nValidationResult == 2) {
                    ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                    throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                }
                for (int idx1 = 0; idx1 < idx; idx1 ++)	{
                    if (m_strVariables[idx1].equalsIgnoreCase(m_strVariables[idx]))	{
                        ERRORTYPES e = ERRORTYPES.REDEFINED_VARIABLE;
                        throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                    }
                }
            }            
		}
	}
	
	public class Statement_slvreto extends StatementType	{
		Statement_slvreto()	{
			m_strType = "slvreto";
		}

		public String m_strReturnedVar = "";	// if it is an empty string, then ignore returned rootspace.
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.trim().substring(0, "slvreto".length()).toLowerCase(Locale.US).equals("slvreto") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
            m_strReturnedVar = strStatement.substring("slvreto".length()).trim().toLowerCase(Locale.US);
            if (m_strReturnedVar.length() != 0)   { // this means solve returns something
                int nValidationResult = validateVarOrFuncName(m_strReturnedVar);
                if (nValidationResult == 1) {
                    ERRORTYPES e = ERRORTYPES.BAD_VARIABLE_NAME;
                    throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                } else if (nValidationResult == 2) {
                    ERRORTYPES e = ERRORTYPES.IS_KEYWORD;
                    throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
                }
            }
		}
	}
	
	public class Statement_help extends StatementType {
		Statement_help() {
			m_strType = "help";
		}

		public String m_strHelpTitle;
		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			m_strHelpTitle = strStatement.substring("help".length()).trim();
		}
	}
	
	public class Statement_endh extends StatementType {
		Statement_endh() {
			m_strType = "endh";
		}

		@Override
		protected void analyze(String strStatement) throws JMFPCompErrException {
			if (strStatement.toLowerCase(Locale.US).equals("endh") == false)	{
				ERRORTYPES e = ERRORTYPES.UNRECOGNIZED_STATEMENT;
				throw new JMFPCompErrException(m_nStartLineNo, m_nEndLineNo, e);
			}
		}
	}
	
	public class Statement_expression extends StatementType	{
		Statement_expression()	{
			m_strType = "_expression_";
		}
		@Override
		protected void analyze(String strStatement)	{
			return;
		}
	}
	
	public void analyze() throws JMFPCompErrException	{
		String[] strSplits = m_strStatement.split("\\s+");
		String strLowerCaseStart = strSplits[0].toLowerCase(Locale.US);
		if (strLowerCaseStart.equals("function"))	{
			m_statementType = new Statement_function();
		} else if (strLowerCaseStart.equals("endf"))	{
			m_statementType = new Statement_endf();	// end of function
		} else if (strLowerCaseStart.equals("return"))	{
			m_statementType = new Statement_return();	// return value
		} else if (strLowerCaseStart.equals("variable"))	{
			m_statementType = new Statement_variable();	// define variables
		} else if (strLowerCaseStart.equals("if"))	{
			m_statementType = new Statement_if();
		} else if (strLowerCaseStart.equals("elseif"))	{
			m_statementType = new Statement_elseif();	// else if
		} else if (strLowerCaseStart.equals("else"))	{
			m_statementType = new Statement_else();
		} else if (strLowerCaseStart.equals("endif"))	{
			m_statementType = new Statement_endif();	// end of if block
		} else if (strLowerCaseStart.equals("while"))	{
			m_statementType = new Statement_while();
		} else if (strLowerCaseStart.equals("loop"))	{
			m_statementType = new Statement_loop();	// end of while ... loop block
		} else if (strLowerCaseStart.equals("do"))	{
			m_statementType = new Statement_do();
		} else if (strLowerCaseStart.equals("until"))	{
			m_statementType = new Statement_until();	// end of do ... until block
		} else if (strLowerCaseStart.equals("for"))	{
			m_statementType = new Statement_for();
		} else if (strLowerCaseStart.equals("next"))	{
			m_statementType = new Statement_next();	// end of for block
		} else if (strLowerCaseStart.equals("break"))	{
			m_statementType = new Statement_break();	// break from a loop
		} else if (strLowerCaseStart.equals("continue"))	{
			m_statementType = new Statement_continue();	// continue a loop without executing the following statements.
		} else if (strLowerCaseStart.equals("select"))	{
			m_statementType = new Statement_select();	// select ... case
		} else if (strLowerCaseStart.equals("case"))	{
			m_statementType = new Statement_case();
		} else if (strLowerCaseStart.equals("default"))	{
			m_statementType = new Statement_default();
		} else if (strLowerCaseStart.equals("ends"))	{
			m_statementType = new Statement_ends();	// end of select
		} else if (strLowerCaseStart.equals("try"))	{
			m_statementType = new Statement_try();
		} else if (strLowerCaseStart.equals("throw"))	{
			m_statementType = new Statement_throw();
		} else if (strLowerCaseStart.equals("catch"))	{
			m_statementType = new Statement_catch();
		} else if (strLowerCaseStart.equals("endtry"))	{
			m_statementType = new Statement_endtry();
		} else if (strLowerCaseStart.equals("solve"))  {
			m_statementType = new Statement_solve();
		} else if (strLowerCaseStart.equals("slvreto"))  {
			m_statementType = new Statement_slvreto();
		} else if (strLowerCaseStart.equals("help"))	{
			m_statementType = new Statement_help();	// help
		} else if (strLowerCaseStart.equals("endh"))	{
			m_statementType = new Statement_endh();	// end of help
		} else {
            // it is possible that some statement key words followed closely by ( or [ or ", so need to split them by these chars.
            strSplits = m_strStatement.split("[\\(\\[\"]");
            if (strSplits.length == 0) {    // for case ""
                m_statementType = new Statement_expression();	// a pure expression
            } else {
                strLowerCaseStart = strSplits[0].toLowerCase(Locale.US);
                if (strLowerCaseStart.equals("return"))	{
                    m_statementType = new Statement_return();
                } else if (strLowerCaseStart.equals("if"))	{
                    m_statementType = new Statement_if();
                } else if (strLowerCaseStart.equals("elseif"))	{
                    m_statementType = new Statement_elseif();
                } else if (strLowerCaseStart.equals("while"))	{
                    m_statementType = new Statement_while();
                } else if (strLowerCaseStart.equals("until"))	{
                    m_statementType = new Statement_until();
                } else if (strLowerCaseStart.equals("select"))	{
                    m_statementType = new Statement_select();
                } else if (strLowerCaseStart.equals("case"))	{
                    m_statementType = new Statement_case();
                } else if (strLowerCaseStart.equals("catch"))	{
                    m_statementType = new Statement_catch();
                } else if (strLowerCaseStart.equals("throw"))	{
                    m_statementType = new Statement_throw();
                } else  {
                    m_statementType = new Statement_expression();	// a pure expression
                }
            }
		}
		m_statementType.analyze(m_strStatement);
	}

    private static final String[] MFP_KEY_WORDS = new String[]{
										"function",
										"endf",
										"return",
										"variable",
										"if",
										"elseif",
										"else",
										"endif",
										"while",
										"loop",
										"do",
										"until",
										"for",
										"to",
										"step",
										"next",
										"break",
										"continue",
										"select",
										"case",
										"default",
										"ends",
										"help",
										"endh",
										"opt_argv",
										"opt_argc",
										"throw",
										"try",
										"catch",
										"endtry",
										"solve",
										"slvreto"
										};
	public static String[] getMFPKeyWords()	{
		// all the key words must be small letters.
		// because the key words are read only, copy and return.
		return MFP_KEY_WORDS.clone();
	}
    
	private static final String[] MFP_RESERVED_WORDS = new String[]{
										"argc",
										"argv",
										"internal_var*",
                                        /*"a_pcon",
                                        "b_pcon",
                                        "c_pcon",
                                        "d_pcon",
                                        "e_pcon",
                                        "f_pcon",
                                        "g_pcon",
                                        "h_pcon",
                                        "i_pcon",
                                        "j_pcon",
                                        "k_pcon",
                                        "l_pcon",
                                        "m_pcon",
                                        "n_pcon",
                                        "o_pcon",
                                        "p_pcon",
                                        "q_pcon",
                                        "r_pcon",
                                        "s_pcon",
                                        "t_pcon",
                                        "u_pcon",
                                        "v_pcon",
                                        "w_pcon",
                                        "x_pcon",
                                        "y_pcon",
                                        "z_pcon",*/
										"enum",
										"type",
										"typeof",
										"export",
										"import",
										"include",
										"class",
										"internal",
										"external",
										"end",
										"private",
										"protected",
										"public",
										"virtual",
										"extends",
										"finally",
										"declare",
										"define",
										"long",
										"int",
										"float",
										"double",
										"solver",
										"caught_expt",
                                        "true",
                                        "false",
                                        "i",
                                        "null",
                                        "inf",
                                        "infi",
                                        "nan",
                                        "nani"
										};
    
	public static String[] getMFPReservedWords()	{
		// all the reserved words must be small letters.
		// because the reserved words are read only, copy and return.
		return MFP_RESERVED_WORDS.clone();
	}
    
	public static boolean isKeyword(String strName)	{
		strName = strName.trim();
		for (String strKeyWords : MFP_KEY_WORDS)	{
			if (strName.equalsIgnoreCase(strKeyWords))	{
				return true;
			}
		}
		
		for (String strReservedWords : MFP_RESERVED_WORDS)	{
			if (strReservedWords.charAt(strReservedWords.length() - 1) != '*') {
				if (strName.equalsIgnoreCase(strReservedWords))	{
					return true;
				}
			} else {
				if (strName.length() >= strReservedWords.length() - 1
						&& strName.substring(0, strReservedWords.length() - 1).equalsIgnoreCase(strReservedWords.substring(0, strReservedWords.length() - 1))) {
					// wide card character match
					return true;
				}
			}
		}
		
		return false;
	}
	
    public static int validateVarOrFuncName(String strName)    {
        strName = strName.toLowerCase(Locale.US);
        if (strName.length() == 0)	{   // var name should not be 0.
            return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
        } else if (strName.charAt(0) > 'z' || strName.charAt(0) < 'a')	{
            // variable name should start from a letter.
            return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
        } else if (isKeyword(strName))	{
            return 2;   // ERRORTYPES.IS_KEYWORD
        } else {
            for (int index1 = 0; index1 < strName.length(); index1 ++)	{
                if ((strName.charAt(index1) > 'z' || strName.charAt(index1) < 'a')
                    && (strName.charAt(index1) > '9' || strName.charAt(index1) < '0')
                    && strName.charAt(index1) != '_')	{
                    // variable name should only include 'a' to 'z', '0' to '9' and '_'
                    return 1;   // ERRORTYPES.BAD_VARIABLE_NAME or BAD_FUNCTION_NAME
                }
            }
        }
        return 0;
    }
}

