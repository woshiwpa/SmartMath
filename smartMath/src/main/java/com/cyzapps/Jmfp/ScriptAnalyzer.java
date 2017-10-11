//:CodeAnalyzer.java analyzes the mfp codes
package com.cyzapps.Jmfp;

import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import java.util.LinkedList;
import com.cyzapps.Jfcalc.BaseData.CurPos;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.BaseData.*;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.ErrorProcessor.ERRORTYPES;
import com.cyzapps.Jmfp.ErrorProcessor.JMFPCompErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.UnknownVarOperator;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class ScriptAnalyzer{
	
	public static ScriptInterrupter msscriptInterrupter = null;
	public static abstract class ScriptInterrupter	{
		public abstract boolean shouldInterrupt();
		public abstract void interrupt() throws InterruptedException;		
	}
	
	public class ScriptStatementException extends Exception	{
		private static final long serialVersionUID = 1L;
		public Statement m_statement;
		public ScriptStatementException()	{
			m_statement = null;
		}
		public ScriptStatementException(Statement statement)	{
			m_statement = statement;
		}
	}
	
	public class FuncRetException extends ScriptStatementException	{
		private static final long serialVersionUID = 1L;
		public DataClass m_datumReturn;
		public FuncRetException()	{
			super();
			m_datumReturn = null;
		}
		public FuncRetException(Statement sFuncRet, DataClass datumReturn)
				throws JFCALCExpErrException	{
			super(sFuncRet);
			m_datumReturn = new DataClass();
			m_datumReturn.copyTypeValue(datumReturn);
		}
	}
	
	public class BreakException extends ScriptStatementException	{
		private static final long serialVersionUID = 1L;
		public BreakException()	{
			super();
		}
		public BreakException(Statement sBreak)	{
			super(sBreak);
		}
	}
	
	public class ContinueException extends ScriptStatementException	{
		private static final long serialVersionUID = 1L;
		public ContinueException()	{
			super();
		}
		public ContinueException(Statement sContinue)	{
			super(sContinue);
		}
	}
	
	public class UntilException extends ScriptStatementException	{
		private static final long serialVersionUID = 1L;
		public int m_nStatementPosition = -1;
		public UntilException()	{
			super();
		}
		public UntilException(Statement sUntil)	{
			super(sUntil);
		}
		public UntilException(Statement sUntil, int nStatementPosition)	{
			super(sUntil);
			m_nStatementPosition = nStatementPosition;
		}
	}
	
	public class NextStatementFilter {
		protected Statement m_sThisStatement = null;
		protected String[] m_strCandidates = null;	// null means all the statements can be candidates except the ones in shouldnots
		protected boolean m_bEndofBlock = false;
		protected String[] m_strShouldNots = new String[0];	//new String[0] means no statement should not be candidate except the ones in candidates.
		public class BorderPair {
			public String[] m_strBorders = new String[2];
			public int m_nEmbeddedLevel = 0;
		}
		public BorderPair[] m_borderPairs = new BorderPair[0];
		
		public void clear()	{
			m_sThisStatement = null;
			m_strCandidates = null;
			m_bEndofBlock = false;
			m_strShouldNots = new String[0];
		}
		
		public void set(Statement sThisStatement, String[] strCandidates, boolean bEndofBlock, String[] strShouldNots)	{
			/*
			 * we do not check if there is any confliction, e.g. one statement type is both in
			 * candidates set and in shouldnots set. Programmer should guarantee this.
			 */
			m_sThisStatement = sThisStatement;
			m_strCandidates = strCandidates;
			m_bEndofBlock = bEndofBlock;
			m_strShouldNots = strShouldNots;
		}
		
		NextStatementFilter()	{
			m_borderPairs = new BorderPair[7];
			m_borderPairs[0] = new BorderPair();
			m_borderPairs[0].m_strBorders[0] = "function";
			m_borderPairs[0].m_strBorders[1] = "endf";
			m_borderPairs[1] = new BorderPair();
			m_borderPairs[1].m_strBorders[0] = "if";
			m_borderPairs[1].m_strBorders[1] = "endif";
			m_borderPairs[2] = new BorderPair();
			m_borderPairs[2].m_strBorders[0] = "for";
			m_borderPairs[2].m_strBorders[1] = "next";
			m_borderPairs[3] = new BorderPair();
			m_borderPairs[3].m_strBorders[0] = "while";
			m_borderPairs[3].m_strBorders[1] = "loop";
			m_borderPairs[4] = new BorderPair();
			m_borderPairs[4].m_strBorders[0] = "do";
			m_borderPairs[4].m_strBorders[1] = "until";
			m_borderPairs[5] = new BorderPair();
			m_borderPairs[5].m_strBorders[0] = "select";
			m_borderPairs[5].m_strBorders[1] = "ends";			
			m_borderPairs[6] = new BorderPair();
			m_borderPairs[6].m_strBorders[0] = "try";
			m_borderPairs[6].m_strBorders[1] = "endtry";			
		}
		
		public boolean isNextStatement(Statement sStatement) throws JMFPCompErrException	{
			/*
			 * we do not check if there is any confliction, e.g. one statement type is both in
			 * candidates set and in shouldnots set. Programmer should guarantee this.
			 */
			if (m_strShouldNots != null)	{
				for (int index = 0; index < m_strShouldNots.length; index ++)	{
					if (m_strShouldNots[index].equals(sStatement.m_statementType.m_strType))	{
						ERRORTYPES e = ERRORTYPES.SHOULD_NOT_AFTER_PREVIOUS_STATEMENT;
						throw new JMFPCompErrException(sStatement.m_nStartLineNo, sStatement.m_nEndLineNo, e);	
					}
				}
			}

			if (m_strCandidates == null)	{
				// this implies that every statement can be candidate.
				return true;
			} else	{
				for (int index1 = 0; index1 < m_borderPairs.length; index1 ++)	{
					if (sStatement.m_statementType.getType().equals(m_borderPairs[index1].m_strBorders[0]))	{
						m_borderPairs[index1].m_nEmbeddedLevel ++;
					} else if (sStatement.m_statementType.getType().equals(m_borderPairs[index1].m_strBorders[1]))	{
						int nMatchedCandidate = 0;
						for (nMatchedCandidate = 0; nMatchedCandidate < m_strCandidates.length; nMatchedCandidate ++)	{
							if (m_strCandidates[nMatchedCandidate].equals(m_borderPairs[index1].m_strBorders[1]))	{
								break;
							}
						}
						if (nMatchedCandidate >= m_strCandidates.length)	{
							// not next statement candidate
							m_borderPairs[index1].m_nEmbeddedLevel --;
							if (m_borderPairs[index1].m_nEmbeddedLevel < 0)	{
								ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
								throw new JMFPCompErrException(sStatement.m_nStartLineNo, sStatement.m_nEndLineNo, e);	
							}
						} else	{
							if (m_borderPairs[index1].m_nEmbeddedLevel > 0)	{
								// next statement candidate, but in a sub-block
								m_borderPairs[index1].m_nEmbeddedLevel --;
								return false;
							} else	{
								return true;	// we are not in a sub-block of pair index1, match.
							}
						}
					}
				}
				for (int index1 = 0; index1 < m_borderPairs.length; index1 ++)	{
					if (m_borderPairs[index1].m_nEmbeddedLevel > 0)	{
						return false;	// we are in a sub-block.
					}
				}
				for (int index = 0; index < m_strCandidates.length; index ++)	{
					if (m_strCandidates[index].equals(sStatement.m_statementType.m_strType))	{
						return true;	// we are not in a sub-block and we find a match here
					}
				}
				return false;
			}
		}
		
		public boolean isEndofBlock()	{
			return m_bEndofBlock;
		}
	}
	
	public int analyzeBlock(Statement[] sarrayLines,	// statements of this script
			int nDeclarationPosition,	// The position of block declaration in sarrayLines 
			LinkedList<Variable> lParams,	// parameter value list used by function or for block, empty if other blocks
			LinkedList<LinkedList<Variable>> lVarNameSpaces)	// variable name space
			throws
			ErrorProcessor.JMFPCompErrException,	// compiling exception
			FuncRetException,	// function return by throwing FuncRetException
			BreakException,		// break statement throws break exception
			ContinueException,		// continue statement throws continue exception
			UntilException, JFCALCExpErrException,	// until statement throws until exception
			InterruptedException	{	// sleep function may throw Interrupted Exception
		
		// in this function we make an assumption that every element in sarrayLines cannot be null.
		// and all the statements have been analyzed.
		
		if (sarrayLines.length == 0)	{
			ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
			throw new JMFPCompErrException(1, 1, e);
		} else if (nDeclarationPosition >= sarrayLines.length || nDeclarationPosition < 0)	{
			ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
			Statement sLast = sarrayLines[sarrayLines.length - 1];
			throw new JMFPCompErrException(1, sLast.m_nEndLineNo, e);
		} else if (nDeclarationPosition >= sarrayLines.length - 1)	{
			ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
			Statement sLast = sarrayLines[sarrayLines.length - 1];
			throw new JMFPCompErrException(sLast.m_nEndLineNo, sLast.m_nEndLineNo, e);
		}
		Statement sDeclaration = sarrayLines[nDeclarationPosition];
        if (sDeclaration.m_eAnalyze != null)    {
            if (sDeclaration.m_eAnalyze instanceof JMFPCompErrException) {
                throw new JMFPCompErrException(sDeclaration.m_nStartLineNo, sDeclaration.m_nEndLineNo,
                        ((JMFPCompErrException)sDeclaration.m_eAnalyze).m_se.m_enumErrorType);
            } else  {
                throw new JMFPCompErrException(sDeclaration.m_nStartLineNo, sDeclaration.m_nEndLineNo,
                        ERRORTYPES.INVALID_EXPRESSION, sDeclaration.m_eAnalyze);
            }
        }
		
		JMFPCompErrException jmfpeCaught = null; 
		
		if (sDeclaration.m_statementType.m_strType.equals("function"))	{
			Statement.Statement_function sf = (Statement.Statement_function)(sDeclaration.m_statementType);
			boolean bOptParams = false;
			if (sf.m_strParams.length != 0 && sf.m_strParams[sf.m_strParams.length - 1].equals("opt_argv"))	{
				bOptParams = true;
			}
			if ((bOptParams == false && sf.m_strParams.length != lParams.size())
					|| (bOptParams == true && sf.m_strParams.length > (lParams.size() + 1)))	{
				// check if the number of parameters match definition.
				ERRORTYPES e = ERRORTYPES.INCORRECT_NUMBER_OF_PARAMETERS;
				throw new JMFPCompErrException(sDeclaration.m_nStartLineNo, sDeclaration.m_nEndLineNo, e);
			} else if (bOptParams == true)	{
				// optional parameters
				Variable vOptArgC = new Variable();
				vOptArgC.setName("opt_argc");
				DataClass datumOptArgC = new DataClass();
				int nOptArgC = lParams.getLast().getValue().getDataListSize();
				datumOptArgC.setDataValue(new MFPNumeric(nOptArgC), DATATYPES.DATUM_INTEGER);
				vOptArgC.setValue(datumOptArgC);
				lParams.addLast(vOptArgC);
			}
		}
		
		LinkedList<Variable> lLocalVars = lParams;	// initialize local name space
		lVarNameSpaces.addFirst(lLocalVars);	// push local variable list into name space.
		if (msscriptInterrupter != null)	{
			if (msscriptInterrupter.shouldInterrupt())	{
				msscriptInterrupter.interrupt();
			}
		}

		int index;
		NextStatementFilter nsf = new NextStatementFilter();	// the next statement to find and execute
		for (index = nDeclarationPosition + 1; index < sarrayLines.length; index ++)	{

			Statement sLine = sarrayLines[index];
			if (nsf.isNextStatement(sLine) == false)	{
				continue;	// not next statement candidate
			}
            if (sLine.m_eAnalyze != null)    {
                if (sLine.m_eAnalyze instanceof JMFPCompErrException) {
                    throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo,
                            ((JMFPCompErrException)sLine.m_eAnalyze).m_se.m_enumErrorType);
                } else  {
                    throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo,
                            ERRORTYPES.INVALID_EXPRESSION, sLine.m_eAnalyze);
                }
            }
			
			// debug output
			//System.out.printf("%d:%s\n", sLine.m_nLineNo - 1, strLines[sLine.m_nLineNo - 1]);
			
			
			if (sLine.m_statementType.m_strType.equals("function"))	{
				// multiple function keywords.
				ERRORTYPES e = ERRORTYPES.EMBEDDED_FUNCTION_DEFINITION;
				throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);
			} else if (sLine.m_statementType.m_strType.equals("endf"))	{
				if (sDeclaration.m_statementType.m_strType.equals("function"))	{
					/*
					 *  arriving at endf means the function does not return anything, throw an exception
					 *  which includes a null datumReturn
					 */
					throw new FuncRetException();
				} else {
					// incomplete block
					ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);
				}
			} else if (sLine.m_statementType.m_strType.equals("return"))	{
				Statement.Statement_return sr = (Statement.Statement_return)(sLine.m_statementType);
				if (sr.m_strReturnedExpr.length() > 0)	{
					// do return something.
					ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
					CurPos c = new CurPos();
					c.m_nPos = 0;
					DataClass datumReturn;
					try	{
						datumReturn = exprEvaluator.evaluateExpression(sr.m_strReturnedExpr, c);
					} catch(JFCALCExpErrException e)	{
                        ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
					}
					lVarNameSpaces.poll();	// pop out name space
					throw new FuncRetException(sLine, datumReturn);
				} else {
					lVarNameSpaces.poll();	// pop out name space
					throw new FuncRetException();
				}
			} else if (sLine.m_statementType.m_strType.equals("variable"))	{
                LinkedList<Variable> listVar = new LinkedList<Variable>();
				Statement.Statement_variable sv = (Statement.Statement_variable)(sLine.m_statementType);
				for (int index1 = 0; index1 < sv.m_strVariables.length; index1 ++)	{
					String strVarName = sv.m_strVariables[index1];
					if (VariableOperator.lookUpList(strVarName, lLocalVars) != null)	{
						ERRORTYPES e = ERRORTYPES.REDEFINED_VARIABLE;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);												
					}
					Variable var = new Variable(sv.m_strVariables[index1], new DataClass());
					listVar.add(var);
				}
                // after verify all the variables, then assign initial values and add them into namespace.
				ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
				for (int index1 = 0; index1 < listVar.size(); index1 ++)    {
					if (sv.m_strVarValues[index1].length() != 0)	{
						CurPos c = new CurPos();
						c.m_nPos = 0;
						DataClass datumReturn;
						try	{
							datumReturn = exprEvaluator.evaluateExpression(sv.m_strVarValues[index1], c);
                            if (datumReturn == null)    {
                                ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
                                throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
                            }
							ExprEvaluator.evaluateTwoOperandCell(listVar.get(index1).getValue(),
																new CalculateOperator(OPERATORTYPES.OPERATOR_ASSIGN, 2),
																datumReturn);
						} catch(JFCALCExpErrException e)	{
							ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
						}
					}
                    lLocalVars.addFirst(listVar.get(index1));
                }
				nsf.clear();	// no filter applied
			} else if (sLine.m_statementType.m_strType.equals("if"))	{
				Statement.Statement_if sif = (Statement.Statement_if)(sLine.m_statementType);
				ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
				CurPos c = new CurPos();
				c.m_nPos = 0;
				DataClass datumCondition;
				try	{
					datumCondition = exprEvaluator.evaluateExpression(sif.m_strCondition, c);
					if (datumCondition == null)	{
						ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
					}
					datumCondition.changeDataType(DATATYPES.DATUM_BOOLEAN);
				} catch(JFCALCExpErrException e)	{
					ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
				}
				String[] strCandidates;
				if (datumCondition.getDataType() == DATATYPES.DATUM_BOOLEAN && datumCondition.getDataValue().isActuallyTrue())	{
					/*
					 * condition is true
					 */
					// empty parameter list
					LinkedList<Variable> l = new LinkedList<Variable>();
					/*
					 * index is the next line of elseif, else or endif.
					 */
					// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
					// (excluding declaration) starting from 0.
					index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces); 
					nsf.clear();	// jump out of analyze block when arrive at endif. after for loop increase
									// index by one, the next statement should be the statement below endif.
									// Thus, nsf should be cleared.
				} else {
					/*
					 * condition is false
					 */
					strCandidates = new String[3];
					strCandidates[0] = "elseif";
					strCandidates[1] = "else";
					strCandidates[2] = "endif";
					// this means all statements can be after if but the next step is to jump to elseif, else or endif.
					String[] strShouldNots = new String[0];
					nsf.set(sLine, strCandidates, false, strShouldNots);
				}
				
			} else if (sLine.m_statementType.m_strType.equals("elseif"))	{
				if (nsf.m_sThisStatement != null
						&& (nsf.m_sThisStatement.m_statementType.m_strType.equals("if")
						|| nsf.m_sThisStatement.m_statementType.m_strType.equals("elseif")))	{
					// previous if / elseif conditions are false
					// else if should not be the end of if/elseif/else/endif block anyway.
					Statement.Statement_elseif selseif = (Statement.Statement_elseif)(sLine.m_statementType);
					ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
					CurPos c = new CurPos();
					c.m_nPos = 0;
					DataClass datumCondition;
					try	{
						datumCondition = exprEvaluator.evaluateExpression(selseif.m_strCondition, c);
						if (datumCondition == null)	{
							ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
						}
						datumCondition.changeDataType(DATATYPES.DATUM_BOOLEAN);
					} catch(JFCALCExpErrException e)	{
						ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
					}
					String[] strCandidates;
					if (datumCondition.getDataType() == DATATYPES.DATUM_BOOLEAN && datumCondition.getDataValue().isActuallyTrue())	{
						/*
						 * condition is true
						 */
						// empty parameter list
						LinkedList<Variable> l = new LinkedList<Variable>();
						/*
						 * index is the next line of elseif, else or endif.
						 */
						// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
						// (excluding declaration) starting from 0.
						index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces);
						nsf.clear();	// jump out of analyze block when arrive at endif. after for loop increase
										// index by one, the next statement should be the statement below endif.
										// Thus, nsf should be cleared.
					} else {
						/*
						 * condition is false
						 */
						strCandidates = new String[3];
						strCandidates[0] = "elseif";
						strCandidates[1] = "else";
						strCandidates[2] = "endif";
						// all statements can be after this statement but the next step is to jump to one of the candidates
						String[] strShouldNots = new String[0];
						nsf.set(sLine, strCandidates, false, strShouldNots);
					}
				} else if (sDeclaration.m_statementType.m_strType.equals("if")
						|| sDeclaration.m_statementType.m_strType.equals("elseif"))	{
					/*
					 * the end of if or else branch block. Exit the analyzeBlock function. However,
					 * index should not be added because index will be automatically added by one
					 * later on.
					 */
					String[] strCandidates = new String[1];
					strCandidates[0] = "endif";
					// this means all statements can be after if but the next step is to jump to elseif, else or endif.
					String[] strShouldNots = new String[0];
					nsf.set(sLine, strCandidates, true, strShouldNots);
				} else {
					// it is not in a if/elseif block and there is no if/elseif before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
				
			} else if (sLine.m_statementType.m_strType.equals("else"))	{
				if (nsf.m_sThisStatement != null
						&& (nsf.m_sThisStatement.m_statementType.m_strType.equals("if")
						|| nsf.m_sThisStatement.m_statementType.m_strType.equals("elseif")))	{
					// previous if / elseif conditions are false
					/*
					 * if else is not the end of a if/elseif branch, it should be the beginning of 
					 * else branch
					 */
					// empty parameter list
					LinkedList<Variable> l = new LinkedList<Variable>();
					/*
					 * index is the next line of elseif, else or endif.
					 */
					// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
					// (excluding declaration) starting from 0.
					index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces);
					nsf.clear();	// jump out of analyze block when arrive at endif. after for loop increase
									// index by one, the next statement should be the statement below endif.
									// Thus, nsf should be cleared.
				} else if (sDeclaration.m_statementType.m_strType.equals("if")
						|| sDeclaration.m_statementType.m_strType.equals("elseif"))	{
					/*
					 * the end of if or else branch block. Exit the analyzeBlock function. However,
					 * index should not be added because index will be automatically added by one
					 * later on.
					 */
					//index ++;
					String[] strCandidates = new String[1];
					strCandidates[0] = "endif";
					// this means all statements can be after if but the next step is to jump to elseif, else or endif.
					String[] strShouldNots = new String[0];
					nsf.set(sLine, strCandidates, true, strShouldNots);
				} else {
					// it is not in a if/elseif block and there is no if/elseif before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("endif"))	{
				if (nsf.m_sThisStatement != null
						&& (nsf.m_sThisStatement.m_statementType.m_strType.equals("if")
						|| nsf.m_sThisStatement.m_statementType.m_strType.equals("elseif")
						|| nsf.m_sThisStatement.m_statementType.m_strType.equals("else")))	{
					// if there is if/elseif/else before it, clear next statement filter structure.
					if (nsf.isEndofBlock())	{
						break;
					} else {
						nsf.clear();					
					}
				} else if (sDeclaration.m_statementType.m_strType.equals("if")
						|| sDeclaration.m_statementType.m_strType.equals("elseif")
						|| sDeclaration.m_statementType.m_strType.equals("else"))	{
					/*
					 * the end of if or else branch block and exit the analyzeBlock function. However
					 * we should not go to the next line because index will be automatically added by
					 * one later on.
					 */
					//index ++;
					break;
				} else	{
					// it is not in a if/elseif/else block and there is no if/elseif/else before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("while"))	{
                int nNumofNameSpaceLevels = 0;  // this variable is used when continue or break exist loop
                                                // coz these two statements may exist from several block levels
				try	{
					int nWhileStatementPos = index;
					while(true)	{
						Statement.Statement_while swhile = (Statement.Statement_while)(sLine.m_statementType);
						ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
						CurPos c = new CurPos();
						c.m_nPos = 0;
						DataClass datumCondition;
						try	{
							datumCondition = exprEvaluator.evaluateExpression(swhile.m_strCondition, c);
							if (datumCondition == null)	{
								ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
								throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
							}
							datumCondition.changeDataType(DATATYPES.DATUM_BOOLEAN);
						} catch(JFCALCExpErrException e)	{
							ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
						}
						if (datumCondition.getDataType() == DATATYPES.DATUM_BOOLEAN && datumCondition.getDataValue().isActuallyTrue())	{
							/*
							 * condition is true
							 */
							// empty parameter list
							LinkedList<Variable> l = new LinkedList<Variable>();
							/*
							 * index is the next line of loop.
							 */
							try	{
                                nNumofNameSpaceLevels = lVarNameSpaces.size();
								// m_nLineNo starts from 1, and AnalyzeBlock needs first body line number
								// (excluding declaration) starting from 0.
								index = analyzeBlock(sarrayLines, nWhileStatementPos, l, lVarNameSpaces);
							} catch(ContinueException e)	{
                                while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                                    lVarNameSpaces.poll();  // pop up stacks.
                                }
								continue;
							}
						} else	{
							String[] strCandidates = new String[1];
							strCandidates[0] = "loop";
							// all statements can be after this statement but the next step is to jump to one of the
							// candidate.
							String[] strShouldNots = new String[0];
							// in this case we are not in AnlyzeBlock caused by while. As such isendofblock should be false.
							nsf.set(sLine, strCandidates, false, strShouldNots);
							break;	// while condition is false, jump out.
						}
					}
				} catch(BreakException e)	{
					// break exception
                    while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                        lVarNameSpaces.poll();  // pop up stacks.
                    }
					String[] strCandidates = new String[1];
					strCandidates[0] = "loop";
					// all statements can be after this statement but the next step is to jump to one of the candidates
					String[] strShouldNots = new String[0];
					// "while" AnalyzeBlock returns the line of while statement so that loop here is not end of block.
					nsf.set(e.m_statement, strCandidates, false, strShouldNots);
				}
			} else if (sLine.m_statementType.m_strType.equals("loop"))	{
				if (nsf.m_sThisStatement != null
						&& nsf.m_sThisStatement.m_statementType.m_strType.equals("while"))	{
					// just finish a while-loop block
					// we always need not to identify isendofblock because while block's exit point is while, not loop.
					nsf.clear();
					continue;
				} else if (nsf.m_sThisStatement != null
						&& nsf.m_sThisStatement.m_statementType.m_strType.equals("break"))	{
					// previously exited a while-loop block by break
					// we always need not to identify isendofblock because while block's normal exit point is while, not break.
					nsf.clear();
					continue;
				} else if (sDeclaration.m_statementType.m_strType.equals("while"))	{
					/*
					 * This is the end of while-loop block function. Exit this analyzeBlock
					 * function here but should not go to next line because index should go
					 * to the beginning of the loop. If this loop is never entered, we will
					 * jump from the beginning of the loop to the end of the loop.
					 */
					index = nDeclarationPosition;
					break;
				} else	{
					// something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("do"))	{
                int nNumofNameSpaceLevels = 0;  // this variable is used when continue or break exist loop
                                                // coz these two statements may exist from several block levels
				try	{
					int nDoStatementPos = index;
					while(true)	{
						LinkedList<Variable> l = new LinkedList<Variable>();
						/*
						 * index is the next line of until.
						 */
						try {
                            nNumofNameSpaceLevels = lVarNameSpaces.size();
							// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
							// (excluding declaration) starting from 0.
							index = analyzeBlock(sarrayLines, nDoStatementPos, l, lVarNameSpaces);
						} catch(ContinueException e)	{
                            while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                                lVarNameSpaces.poll();  // pop up stacks.
                            }
							continue;
						}
					}
				} catch(BreakException e)	{
					// break exception
                    while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                        lVarNameSpaces.poll();  // pop up stacks.
                    }
					String[] strCandidates = new String[1];
					strCandidates[0] = "until";
					// all statements can be after this statement but the next step is to jump to one of the candidates
					String[] strShouldNots = new String[0];
					//break here is not the end of "do" AnalyzeBlock because AnalyzeBlock returns to the line of do statment.
					nsf.set(e.m_statement, strCandidates, false, strShouldNots);
				} catch(UntilException e)	{
					// set the index to the until statement line so that the next step
					// we can go the statement after until.
					index = e.m_nStatementPosition;
				}
			} else if (sLine.m_statementType.m_strType.equals("until"))	{
				if (nsf.m_sThisStatement != null
						&& nsf.m_sThisStatement.m_statementType.m_strType.equals("break"))	{
					// previously exited a do-until block by break
					nsf.clear();
					// we always need not to identify isendofblock because do block's normal exit point is do, not break.
					continue;
				} else if (sDeclaration.m_statementType.m_strType.equals("do"))	{
					// this is the end of do-until block function
					Statement.Statement_until suntil = (Statement.Statement_until)(sLine.m_statementType);
					ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
					CurPos c = new CurPos();
					c.m_nPos = 0;
					DataClass datumCondition;
					try	{
						datumCondition = exprEvaluator.evaluateExpression(suntil.m_strCondition, c);
						if (datumCondition == null)	{
							ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
						}
						datumCondition.changeDataType(DATATYPES.DATUM_BOOLEAN);
					} catch(JFCALCExpErrException e)	{
						ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
					}
					if (datumCondition.getDataType() == DATATYPES.DATUM_BOOLEAN && datumCondition.getDataValue().isActuallyTrue())	{
						lVarNameSpaces.poll();	// pop local variable list out from name space
						throw new UntilException(sLine, index);
					}
					/*
					 * This is the end of do-until block function. Exit this analyzeBlock
					 * function here but should not go to next line because index should go
					 * to the beginning of the loop. If this loop is never entered, we will
					 * jump from the beginning of the loop to the end of the loop.
					 */
					index = nDeclarationPosition;
					nsf.clear();
					break;
				} else	{
					// something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("for"))	{
				Statement.Statement_for sfor = (Statement.Statement_for)(sLine.m_statementType);
				// calculate start, end and step value
				ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
				CurPos c = new CurPos();
				c.m_nPos = 0;
				DataClass datumIndex;
				try	{
					DataClass datumStart = exprEvaluator.evaluateExpression(sfor.m_strStart, c);
					if (datumStart == null)	{
						ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
					}
					datumIndex = new DataClass();
					exprEvaluator.evaluateTwoOperandCell(datumIndex, new CalculateOperator(OPERATORTYPES.OPERATOR_ASSIGN, 2), datumStart);
					if (datumIndex.isSingleInteger())	{
						datumIndex.changeDataType(DATATYPES.DATUM_INTEGER);
					} else	{
						datumIndex.changeDataType(DATATYPES.DATUM_DOUBLE);
					}
				} catch(JFCALCExpErrException e)	{
					ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
				}
				c.m_nPos = 0;
				DataClass datumStep;
				try	{
					datumStep = exprEvaluator.evaluateExpression(sfor.m_strStep, c);
					if (datumStep == null)	{
						ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
					}
				} catch(JFCALCExpErrException e)	{
					ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
				}
				DataClass datumEnd = null;
                int nNumofNameSpaceLevels = 0;  // this variable is used when continue or break exist loop
                                                // coz these two statements may exist from several block levels
				try	{
					int nForStatementPos = index;
					while(true)	{
						c.m_nPos = 0;
						try	{
							datumEnd = exprEvaluator.evaluateExpression(sfor.m_strEnd, c);
							if (datumEnd == null)	{
								ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
								throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
							}
							if (datumIndex.isSingleInteger())	{
								datumIndex.changeDataType(DATATYPES.DATUM_INTEGER);
							} else	{
								datumIndex.changeDataType(DATATYPES.DATUM_DOUBLE);
							}
							if (datumStep.isSingleInteger())	{
								datumStep.changeDataType(DATATYPES.DATUM_INTEGER);
							} else	{
								datumStep.changeDataType(DATATYPES.DATUM_DOUBLE);
							}
							if (datumEnd.isSingleInteger())	{
								datumEnd.changeDataType(DATATYPES.DATUM_INTEGER);
							} else	{
								datumEnd.changeDataType(DATATYPES.DATUM_DOUBLE);
							}
						} catch(JFCALCExpErrException e)	{
							ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
						}
						if ((datumStep.getDataValue().isActuallyPositive()
									&& datumIndex.getDataValue().compareTo(datumEnd.getDataValue()) <= 0)
								|| (datumStep.getDataValue().isActuallyNegative()
									&& datumIndex.getDataValue().compareTo(datumEnd.getDataValue()) >= 0)
								|| (datumStep.getDataValue().isActuallyZero()
									&& datumIndex.getDataValue().isEqual(datumEnd.getDataValue())))	{
							/*
							 * index matches the range.
							 */
							try	{
								Variable var = null;
								// empty parameter list
								LinkedList<Variable> l = new LinkedList<Variable>();
								if (sfor.m_bLocalDefIndex)	{
									var = new Variable(sfor.m_strIndexName, datumIndex);
									l.addFirst(var);
								} else	{
									if (VariableOperator.setValueInSpaces(lVarNameSpaces, sfor.m_strIndexName, datumIndex) == null)	{
										ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
										throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);												
									}
								}
                                nNumofNameSpaceLevels = lVarNameSpaces.size();
								// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
								// (excluding declaration) starting from 0.
								index = analyzeBlock(sarrayLines, nForStatementPos, l, lVarNameSpaces);
								c.m_nPos = 0;
								try	{
									datumStep = exprEvaluator.evaluateExpression(sfor.m_strStep, c);
									if (datumStep == null)	{
										ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
										throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
									}
									datumIndex.setDataValue(datumIndex.getDataValue().add(datumStep.getDataValue()));
									if (datumIndex.isSingleInteger())	{
										datumIndex.changeDataType(DATATYPES.DATUM_INTEGER);
									}
								} catch(JFCALCExpErrException eExpression)	{
									ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
									throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, eExpression);												
								}
							} catch(ContinueException e)	{
                                while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                                    lVarNameSpaces.poll();  // pop up stacks.
                                }
								c.m_nPos = 0;
								try {
									datumStep = exprEvaluator.evaluateExpression(sfor.m_strStep, c);
									if (datumStep == null)	{
										ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
										throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
									}
									datumIndex.setDataValue(datumIndex.getDataValue().add(datumStep.getDataValue()));
									if (datumIndex.isSingleInteger())	{
										datumIndex.changeDataType(DATATYPES.DATUM_INTEGER);
									}
								} catch(JFCALCExpErrException eExpression)	{
									ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
									throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, eExpression);												
								}
								continue;
							}
						} else	{
							String[] strCandidates = new String[1];
							strCandidates[0] = "next";
							// all statements can be after this statement but the next step is to jump to one of the
							// candidate.
							String[] strShouldNots = new String[0];
							// next here is not end of "for" AnalyzeBlock coz "for" AnalyzeBlock returns to for statement line.
							nsf.set(sLine, strCandidates, false, strShouldNots);
							break;	// finish for - next loop in normal.
						}
					}
				} catch(BreakException e)	{
					// break exception
                    while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                        lVarNameSpaces.poll();  // pop up stacks.
                    }
					String[] strCandidates = new String[1];
					strCandidates[0] = "next";
					// all statements can be after this statement but the next step is to jump to one of the candidates
					String[] strShouldNots = new String[0];
					// break here is not end of "for" AnalyzeBlock coz "for" AnalyzeBlock returns to for statement line.
					nsf.set(e.m_statement, strCandidates, false, strShouldNots);
				}
			} else if (sLine.m_statementType.m_strType.equals("next"))	{
				if (nsf.m_sThisStatement != null
						&& nsf.m_sThisStatement.m_statementType.m_strType.equals("for"))	{
					// just finish a for-next loop block
					nsf.clear();
					// we always need not to identify isendofblock because for block's exit point is for, not next.
					continue;
				} else if (nsf.m_sThisStatement != null
						&& nsf.m_sThisStatement.m_statementType.m_strType.equals("break"))	{
					// previously exited a for-next block by break
					nsf.clear();
					// we always need not to identify isendofblock because for block's normal exit point is for, not break.
					continue;
				} else if (sDeclaration.m_statementType.m_strType.equals("for"))	{
					/*
					 * This is the end of for-next block function. Exit this analyzeBlock
					 * function here but should not go to next line because index should go
					 * to the beginning of the loop. If this loop is never entered, we will
					 * jump from the beginning of the loop to the end of the loop.
					 */
					index = nDeclarationPosition;
					break;
				} else	{
					// something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("break"))	{
				// lVarNameSpaces.poll();	// pop local variable list out from name space
                                            // but should not do it here coz we don't know how many
                                            // stack levels we are gonna exit.
				throw new BreakException(sLine);
			} else if (sLine.m_statementType.m_strType.equals("continue"))	{
				// lVarNameSpaces.poll();	// pop local variable list out from name space
                                            // but should not do it here coz we don't know how many
                                            // stack levels we are gonna exit.
				throw new ContinueException(sLine);
			} else if (sLine.m_statementType.m_strType.equals("select"))	{
				String[] strCandidates = new String[3];
				strCandidates[0] = "case";
				strCandidates[1] = "default";
				strCandidates[2] = "ends";
				String[] strShouldNots = null;	//all strings should not be after select except case or default.
				nsf.set(sLine, strCandidates, false, strShouldNots);
			} else if (sLine.m_statementType.m_strType.equals("case"))	{
				if (nsf.m_sThisStatement != null && nsf.m_sThisStatement.m_statementType.m_strType.equals("select"))	{
					// this is the case statement following a select or previous unhit case
					// case should not be the normal exit point of select block.
					Statement.Statement_select sselect = (Statement.Statement_select)(nsf.m_sThisStatement.m_statementType);
					ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
					CurPos c = new CurPos();
					c.m_nPos = 0;
					DataClass datumSelect;
					try	{
						datumSelect = exprEvaluator.evaluateExpression(sselect.m_strSelectedExpr, c);
						if (datumSelect == null)	{
							ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
						}		
					} catch(JFCALCExpErrException e)	{
						ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
						throw new JMFPCompErrException(nsf.m_sThisStatement.m_nStartLineNo,
																		nsf.m_sThisStatement.m_nEndLineNo, errType, e);												
					}
					Statement.Statement_case scase = (Statement.Statement_case)(sLine.m_statementType);
					c.m_nPos = 0;
					DataClass datumCase;
					try	{
						datumCase = exprEvaluator.evaluateExpression(scase.m_strCaseExpr, c);
						if (datumCase == null)	{
							ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
							throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);
						}
					} catch(JFCALCExpErrException e)	{
						ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
					}
					if (datumSelect.isEqual(datumCase))	{	// so that we can compare string, array, complex
						// this is the case!
						// empty parameter list
						LinkedList<Variable> l = new LinkedList<Variable>();
                        int nNumofNameSpaceLevels = 0;  // this variable is used when continue or break exist loop
                                                        // coz these two statements may exist from several block levels
						try	{
							/*
							 * index is the next line of ends.
							 */
                            nNumofNameSpaceLevels = lVarNameSpaces.size();
							// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
							// (excluding declaration) starting from 0.
							index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces);
							// we have arrived at ends so that clear nsf here.
							nsf.clear();
						} catch (BreakException e)	{
							// break exception
                            while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                                lVarNameSpaces.poll();  // pop up stacks.
                            }
							String[] strCandidates = new String[1];
							strCandidates[0] = "ends";
							// all strings can be after this statement but the next step is jump to ends.
							String[] strShouldNots = new String[0];
							// we have already been out of "case" analyzeblock 
							nsf.set(e.m_statement, strCandidates, false, strShouldNots);
						}
					} else {
						// this is not the case
						continue;
					}
				} else if (sDeclaration.m_statementType.m_strType.equals("case"))	{
					// this is another case. If no break before, ignore it.
					continue;
				} else 	{
					ERRORTYPES e = ERRORTYPES.SHOULD_NOT_AFTER_PREVIOUS_STATEMENT;
					throw new JMFPCompErrException(nsf.m_sThisStatement.m_nStartLineNo,
																	nsf.m_sThisStatement.m_nEndLineNo, e);												
				}
			} else if (sLine.m_statementType.m_strType.equals("default"))	{
				if (sDeclaration.m_statementType.m_strType.equals("case"))	{
					// this is default case. If no break before, ignore it.
					continue;
				} else	{
					// this is the default statement following a select or previous unhit case
					// this is the case!
					// empty parameter list
					LinkedList<Variable> l = new LinkedList<Variable>();
                    int nNumofNameSpaceLevels = 0;  // this variable is used when continue or break exist loop
                                                    // coz these two statements may exist from several block levels
					try	{
						/*
						 * index is the next line of ends.
						 */
                        nNumofNameSpaceLevels = lVarNameSpaces.size();
						// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
						// (excluding declaration) starting from 0.
						index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces);
						// we have arrived at ends so that we should reset next statement filter here.
						nsf.clear();
					} catch (BreakException e)	{
						// break exception
                        while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                            lVarNameSpaces.poll();  // pop up stacks.
                        }
						String[] strCandidates = new String[1];
						strCandidates[0] = "ends";
						// all strings can be after this statement but the next step is jumping to ends
						String[] strShouldNots = new String[0];
						// we have already been out of "default" analyzeblock.
						nsf.set(e.m_statement, strCandidates, false, strShouldNots);
					}
				}
			} else if (sLine.m_statementType.m_strType.equals("ends"))	{
				if (nsf.m_sThisStatement != null
						&& (nsf.m_sThisStatement.m_statementType.m_strType.equals("break")
								|| nsf.m_sThisStatement.m_statementType.m_strType.equals("select")))	{
					// exited a select-case blog by break previously or no case nor default is hit so exit anyway.
					nsf.clear();
					// we have previously exited case or default analyzeBlock so need not exit again.
					continue;
				} else if (sDeclaration.m_statementType.m_strType.equals("select")
						|| sDeclaration.m_statementType.m_strType.equals("case")
						|| sDeclaration.m_statementType.m_strType.equals("default"))	{
					/*
					 * this is the end of select-case blog function, exit this analyzeBlock function
					 * index should not be added by one because it will be automatically added later
					 * on
					 */
					//index ++;
					break;
				} else	{
					// something wrong here. It is not in a select/case/default/ends block and there is no case/default before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("try"))	{
				// empty parameter list
				LinkedList<Variable> l = new LinkedList<Variable>();
				/*
				 * index is the next line of try.
				 */
				// m_nLineNo starts from 1, and AnalyzeLock needs first body line number
				// (excluding declaration) starting from 0.
                int nNumofNameSpaceLevels = 0;
				try	{
					jmfpeCaught = null;
                    nNumofNameSpaceLevels = lVarNameSpaces.size();
					index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces);
				} catch (JMFPCompErrException e)	{
					while (lVarNameSpaces.size() > nNumofNameSpaceLevels)  {
                        lVarNameSpaces.poll();  // pop up stacks.
                    }
                    // only JMFPCompErrException is captured.
					jmfpeCaught = e;
					
					String[] strCandidates = new String[2];
					strCandidates[0] = "catch";
					strCandidates[1] = "endtry";
					// this means all statements can be after try but the next step is to jump to catch or endtry.
					String[] strShouldNots = new String[0];
					nsf.set(sLine, strCandidates, false, strShouldNots);
					continue;
				}
				nsf.clear();	// jump out of analyze block when arrive at endtry. after for loop increase
								// index by one, the next statement should be the statement below endtry.
								// Thus, nsf should be cleared.
			} else if (sLine.m_statementType.m_strType.equals("throw"))	{
				Statement.Statement_throw sthrow = (Statement.Statement_throw)sLine.m_statementType;
				ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
				CurPos c = new CurPos();
				c.m_nPos = 0;
				DataClass datum;
				try {
					datum = exprEvaluator.evaluateExpression(sthrow.m_strThrownExpr, c);
				} catch(JFCALCExpErrException e)	{
					ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
				}
				if (datum == null)	{
					ERRORTYPES errType = ERRORTYPES.NO_VALUE_OBTAINED_FROM_EXPRESSION;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);												
				} 
				try {
					datum.changeDataType(DATATYPES.DATUM_STRING);		
				} catch(JFCALCExpErrException e)	{
					ERRORTYPES errType = ERRORTYPES.WRONG_VARIABLE_TYPE;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType);												
				}
				ERRORTYPES errType = ERRORTYPES.USER_DEFINED_EXCEPTION;
				throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, datum.getStringValue());					
			} else if (sLine.m_statementType.m_strType.equals("catch"))	{
				if (jmfpeCaught == null)	{
					// this means the exception has been processed or no exception was thrown in try block.
					String[] strCandidates = new String[1];
					strCandidates[0] = "endtry";
					// this means all statements can be after catch but the next step is to jump to endtry.
					String[] strShouldNots = new String[0];
					nsf.set(sLine, strCandidates, false, strShouldNots);
				} else	{
                    // assume all the exceptions (including lower level exceptions) caught here are eighter JFCALCExpErrException or JMFPCompErrException
					String strExceptionLevel = "EXPRESSION";
                    String strExceptionType = "UNKNOWN_EXCEPTION";
                    String strExceptionInfo = "Unknown exception";
                    Exception eThisLevel = jmfpeCaught;
                    while (eThisLevel != null)    {
                        if (eThisLevel instanceof JMFPCompErrException) {
                            JMFPCompErrException jmfpeThisLevel = (JMFPCompErrException)eThisLevel;
                            if (jmfpeThisLevel.m_se.m_enumErrorType != ErrorProcessor.ERRORTYPES.INVALID_EXPRESSION
                                    || jmfpeThisLevel.m_exceptionLowerLevel == null)   {
                                strExceptionLevel = "LANGUAGE";
                                strExceptionType = jmfpeThisLevel.m_se.getErrorType();
                                strExceptionInfo = jmfpeThisLevel.m_se.getErrorInfo();
                                break;  // definitely not "EXPRESSION" level.
                            } else  {
                                eThisLevel = jmfpeThisLevel.m_exceptionLowerLevel;
                            }
                        } else if (eThisLevel instanceof JFCALCExpErrException) {
                            JFCALCExpErrException jfcalceThisLevel = (JFCALCExpErrException)eThisLevel;
                            if (jfcalceThisLevel.m_se.m_enumErrorType != ErrProcessor.ERRORTYPES.ERROR_FUNCTION_EVALUATION
                                    || jfcalceThisLevel.m_exceptionLowerLevel == null)  {
                                strExceptionLevel = "EXPRESSION";
                                strExceptionType = jfcalceThisLevel.m_se.getErrorType();
                                strExceptionInfo = jfcalceThisLevel.m_se.getErrorInfo();
                                break;
                            } else  {
                                // function evaluation exception, should have deeper exceptions
                                eThisLevel = jfcalceThisLevel.m_exceptionLowerLevel;
                            }
                        } else  {
                            // unknown exception
                            strExceptionLevel = "EXPRESSION";
                            strExceptionType = "UNKNOWN_EXCEPTION";
                            strExceptionInfo = "Unknown exception";
                            break;
                        }                        
                    }
					DataClass datumExceptionLevel = new DataClass();
					datumExceptionLevel.setStringValue(strExceptionLevel);
		
					DataClass datumExceptionType = new DataClass();
					datumExceptionType.setStringValue(strExceptionType);
					
					DataClass datumExceptionInfo = new DataClass();
					datumExceptionInfo.setStringValue(strExceptionInfo);

					LinkedList<Variable> lCatchFilterVars = new LinkedList<Variable>();
					Variable vCatchFilterArgLevel = new Variable();
					vCatchFilterArgLevel.setName("level");
					vCatchFilterArgLevel.setValue(datumExceptionLevel);
					lCatchFilterVars.addLast(vCatchFilterArgLevel);
					Variable vCatchFilterArgType = new Variable();
					vCatchFilterArgType.setName("type");
					vCatchFilterArgType.setValue(datumExceptionType);
					lCatchFilterVars.addLast(vCatchFilterArgType);
					Variable vCatchFilterArgInfo = new Variable();
					vCatchFilterArgInfo.setName("info");
					vCatchFilterArgInfo.setValue(datumExceptionInfo);
					lCatchFilterVars.addLast(vCatchFilterArgInfo);
					
					lVarNameSpaces.addFirst(lCatchFilterVars);
					ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
					CurPos c = new CurPos();
					c.m_nPos = 0;
					DataClass datum = new DataClass();
					Statement.Statement_catch scatch = (Statement.Statement_catch)sLine.m_statementType;
					try {
						if (scatch.m_strFilter.trim().equals(""))	{
							datum.setDataValue(MFPNumeric.TRUE);	// catch any exception
						} else	{
							datum = exprEvaluator.evaluateExpression(scatch.m_strFilter, c);
						}
						datum.changeDataType(DATATYPES.DATUM_BOOLEAN);
					} catch(JFCALCExpErrException e)	{
						if (lVarNameSpaces.isEmpty() == false)	{
							lVarNameSpaces.removeFirst();
						}
						ERRORTYPES errType = ERRORTYPES.INVALID_CATCH_FILTER;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);												
					}
					if (lVarNameSpaces.isEmpty() == false)	{
						lVarNameSpaces.removeFirst();
					}
                    // datum has been converted to boolean, MFP type must be MFP_BOOLEAN_TYPE so that we can
                    // use isFalse() instead of isActuallyFalse()
					if (datum.getDataValue().isFalse())	{
						// false, go to next catch
						String[] strCandidates = new String[2];
						strCandidates[0] = "catch";
						strCandidates[1] = "endtry";
						// this means all statements can be after try but the next step is to jump to catch or endtry.
						String[] strShouldNots = new String[0];
						nsf.set(sLine, strCandidates, false, strShouldNots);
					} else	{
						// true, go to next statement in this catch block
						jmfpeCaught = null;	// so that next catch knows that no exception needs to catch.
						// empty parameter list
						LinkedList<Variable> l = new LinkedList<Variable>();
						index = analyzeBlock(sarrayLines, index, l, lVarNameSpaces);
						nsf.clear();
					}
				}
			} else if (sLine.m_statementType.m_strType.equals("endtry"))	{
				if (jmfpeCaught != null
						&& (nsf.m_sThisStatement.m_statementType.m_strType.equals("try")
						|| nsf.m_sThisStatement.m_statementType.m_strType.equals("catch")))	{
					// the exception in try block is not handled. throw the exception out.
					throw jmfpeCaught;					
				} else if (jmfpeCaught == null
						&& (sDeclaration.m_statementType.m_strType.equals("try")	// no exception thrown
								|| sDeclaration.m_statementType.m_strType.equals("catch")))	{	// the exception is handled.
					/*
					 * the end of try/catch branch block and exit the analyzeBlock function. However
					 * we should not go to the next line because index will be automatically added by
					 * one later on.
					 */
					//index ++;
					break;
				} else	{
					// it is not in a try/catch/endtry block and there is no try or catch before it.
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
			} else if (sLine.m_statementType.m_strType.equals("solve"))	{
				LinkedList<UnknownVariable> listVarUnknown = new LinkedList<UnknownVariable>();
				Statement.Statement_solve ss = (Statement.Statement_solve)(sLine.m_statementType);
				for (int index1 = 0; index1 < ss.m_strVariables.length; index1 ++)	{
					String strVarName = ss.m_strVariables[index1];
                    Variable varFromOutside;
					if ((varFromOutside = VariableOperator.lookUpSpaces(strVarName, lVarNameSpaces)) == null)	{
						ERRORTYPES e = ERRORTYPES.UNDEFINED_VARIABLE;
						throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);												
					}
					UnknownVariable varUnknown = new UnknownVariable(ss.m_strVariables[index1]);
                    varUnknown.setValue(varFromOutside.getValue()); // do not deep copy the value coz it might be referred by something
                    // although unknown variable var has been assigned a value, the state of it is still unassigned.
                    varUnknown.setValueAssigned(false);
					listVarUnknown.add(varUnknown);
				}
                // ensure that all the vars in unknown list are actually in lVarNameSpaces
                UnknownVarOperator.mergeUnknowns2VarSpaces(listVarUnknown, lVarNameSpaces);
                try {
                    SolveAnalyzer solveAnalyzer = new SolveAnalyzer();
                    index = solveAnalyzer.analyzeSolve(sarrayLines, index, listVarUnknown, lVarNameSpaces);
                } catch (JSmartMathErrException e1) {
                    ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
                    throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e, e1);												
                } catch (JFCALCExpErrException e1) {
                    ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
                    throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e, e1);
                } catch (JMFPCompErrException e1)   {
                    ERRORTYPES e = ERRORTYPES.INVALID_SOLVER;
                    throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e, e1);
                }
                nsf.clear();	// no filter applied
			} else if (sLine.m_statementType.m_strType.equals("help"))	{
				String[] strCandidates = new String[1];
				strCandidates[0] = "endh";
				// all statements can be after help but the next step is to jump to endh.
				String[] strShouldNots = new String[0];
				nsf.set(sLine, strCandidates, false, strShouldNots);
			} else if (sLine.m_statementType.m_strType.equals("endh"))	{
				if (nsf.m_sThisStatement == null
						|| nsf.m_sThisStatement.m_statementType.m_strType.equals("help") == false)	{
					ERRORTYPES e = ERRORTYPES.CANNOT_FIND_BEGINNING_OF_BLOCK;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, e);					
				}
				nsf.clear();
			} else if (sLine.getStatement().compareTo("") == 0)	{
				// empty statement. just //...
				continue;
			} else	{
				// this should be a single expression
				ExprEvaluator exprEvaluator = new ExprEvaluator(lVarNameSpaces);
				CurPos c = new CurPos();
				c.m_nPos = 0;
				try {
					exprEvaluator.evaluateExpression(sLine.getStatement(), c);
				} catch(JFCALCExpErrException e)	{
					ERRORTYPES errType = ERRORTYPES.INVALID_EXPRESSION;
					throw new JMFPCompErrException(sLine.m_nStartLineNo, sLine.m_nEndLineNo, errType, e);										
				}
			}
		}

		if (index >= sarrayLines.length)	{
			// we've arrived at the bottom of the script but no return nor endf found.
			ERRORTYPES e = ERRORTYPES.INCOMPLETE_BLOCK;
			/*
			 *  remember that the line seen by user starts from 1 so that the line no
			 *  should be strLines.length not strLines.length - 1.
			 */
			Statement sLast = sarrayLines[sarrayLines.length - 1];
			throw new JMFPCompErrException(sLast.m_nEndLineNo, sLast.m_nEndLineNo, e);
		}
		
		lVarNameSpaces.poll();	// pop local variable list out from name space
		return index;	// return the line No that to be processed
	}
}
