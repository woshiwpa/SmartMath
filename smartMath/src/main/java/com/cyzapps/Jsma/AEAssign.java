package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEAssign extends AbstractExpr {

	public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>();

	public AEAssign() {
		initAbstractExpr();
	}
	
	public AEAssign(LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException	{
		setAEAssign(listChildren);
	}

	public AEAssign(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_ASSIGN;
		mlistChildren = new LinkedList<AbstractExpr>();
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException	{
		if (mlistChildren.size() < 2)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_OPERATOR_SHOULD_HAVE_AT_LEAST_TWO_OPERANDS);
		}
		for (int idx = 0; idx < mlistChildren.size() - 1; idx ++)	{
			if (mlistChildren.get(idx).isVariable() == false)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE);
			}
		}
	}
	
	private void setAEAssign(LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException	{
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_ASSIGN;
		mlistChildren = (listChildren == null)?new LinkedList<AbstractExpr>():listChildren;
		validateAbstractExpr();
	}
	
	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEAssign)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		mlistChildren = new LinkedList<AbstractExpr>();
		if (((AEAssign)aexprOrigin).mlistChildren != null)	{
            mlistChildren.addAll(((AEAssign)aexprOrigin).mlistChildren);
		}
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEAssign)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		mlistChildren = new LinkedList<AbstractExpr>();
		if (((AEAssign)aexprOrigin).mlistChildren != null)	{
			for (int idx = 0; idx < ((AEAssign)aexprOrigin).mlistChildren.size(); idx ++)	{
				AbstractExpr aexprChild = ((AEAssign)aexprOrigin).mlistChildren.get(idx).cloneSelf();
				mlistChildren.add(aexprChild);
			}
		}
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException	{
		AbstractExpr aeReturn = new AEAssign();
		aeReturn.copyDeep(this);
		return aeReturn;
	}
	
	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();

		return mlistChildren.getLast().recalcAExprDim(bUnknownAsSingle);
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (mlistChildren.size() != ((AEAssign)aexpr).mlistChildren.size())	{
			return false;
		} else {
			for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
				if (mlistChildren.get(idx).isEqual(((AEAssign)aexpr).mlistChildren.get(idx)) == false)	{
					return false;
				}
			}
			return true;
		}
	}
    
    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion)  throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
        /* do not call isPatternDegrade function because generally assign expression cannot degrade-match a pattern.*/
        if (aePattern.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE)   {
            // unknown variable
            for (int idx = 0; idx < listpeuMapUnknowns.size(); idx ++)  {
                if (listpeuMapUnknowns.get(idx).maePatternUnit.isEqual(aePattern))    {
                    if (isEqual(listpeuMapUnknowns.get(idx).maeExprUnit))   {
                        // this unknown variable has been mapped to an expression and the expression is the same as this
                        return true;
                    } else  {
                        // this unknown variable has been mapped to an expression but the expression is not the same as this
                        return false;
                    }
                }
            }
            // the aePattern is an unknown variable and it hasn't been mapped to some expressions before.
            PatternExprUnitMap peuMap = new PatternExprUnitMap(this, aePattern);
            listpeuMapUnknowns.add(peuMap);
            return true;
        }
        if (!(aePattern instanceof AEAssign))   {
            return false;
        }
        if (mlistChildren.size() != ((AEAssign)aePattern).mlistChildren.size()) {
            return false;
        }
        for (int idx = 0; idx < mlistChildren.size(); idx ++)   {
            if (mlistChildren.get(idx).isPatternMatch(((AEAssign)aePattern).mlistChildren.get(idx),
                                                    listpeuMapPseudoFuncs,
                                                    listpeuMapPseudoConsts,
                                                    listpeuMapUnknowns,
                                                    bAllowConversion) == false)   {
                return false;
            }
        }
        return true;
    }

	@Override
	public boolean isKnownValOrPseudo() {
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			if ( mlistChildren.get(idx).isKnownValOrPseudo() == false)	{
				return false;
			}
		}
		return true;
	}
	
	// note that the return list should not be changed.
	@Override
	public LinkedList<AbstractExpr> getListOfChildren()	{
		return mlistChildren;
	}
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        AEAssign aeReturn = new AEAssign();
        aeReturn.copy(this);
        aeReturn.mlistChildren = listChildren == null?new LinkedList<AbstractExpr>():listChildren;
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
	
	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
	public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException	{
		AEAssign aeReturn = new AEAssign();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)	{
			for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)	{
				if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit))	{
					aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);
					listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
					break;
				} else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit))	{
					aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);
					listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
					break;
				}
			}
		}
		return aeReturn;
	}
	
	// assignment cannot be distributed.
	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException	{
		validateAbstractExpr();
		return this;
	}

    // avoid to do any overhead work.
	@Override
	public DataClass evaluateAExprQuick(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumReturn = mlistChildren.getLast().evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
        for (int idx = mlistChildren.size() - 2; idx >= 0 ; idx --)	{	// have to go through from last to first consider the following case:
                                                                        // x[y] = y = 3. If y is not known, x[y] cannot be evaluated.
            // if child idx hasn't been simplified, child idx - 1 cannot be simplified considering the situation y = -1; x[y] = y = 3
            if (mlistChildren.get(idx).isVariable())	{
                if (mlistChildren.get(idx) instanceof AEVar)	{
                    String strVariableName = ((AEVar)mlistChildren.get(idx)).mstrVariableName;
                    Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                    // first lookup unknown variable list, then lookup known variable space.
                    if (var == null)	{
                        var = VariableOperator.lookUpSpaces(strVariableName, lVarNameSpaces);
                        if (var == null)	{
                            throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
                        }
                    }
                    var.setValue(datumReturn);  // datumReturn should be a deep copy of single type or a shallow copy of a matrix.
                } else	{	// AEIndex with base is a variable.
                    AbstractExpr aeBase = ((AEIndex)mlistChildren.get(idx)).maeBase;
                    AbstractExpr aeIndex = ((AEIndex)mlistChildren.get(idx)).maeIndex;
                    AbstractExpr aeMerged = mlistChildren.get(idx);
                    try	{
                        aeMerged = AEIndex.mergeIndex(aeBase, aeIndex);
                    } catch (JSmartMathErrException e)	{
                        if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
                            throw e;
                        }
                    }
                    // now aeMerged must be an AEIndex type, and it's base must be a variable.
                    AEVar aeNewBase = (AEVar)((AEIndex)aeMerged).maeBase;
                    DataClass datumNewIndex = ((AEIndex)aeMerged).evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
                    DataClass[] datumIndexList = datumNewIndex.getDataList();
                    int[] nIndexArray = new int[datumIndexList.length];
                    for (int idx1 = 0; idx1 < datumIndexList.length; idx1 ++)	{
                        DataClass datumThisIdx = new DataClass();
                        datumThisIdx.copyTypeValueDeep(datumIndexList[idx1]);
                        datumThisIdx.changeDataType(DATATYPES.DATUM_INTEGER);
                        nIndexArray[idx1] = (int)datumThisIdx.getDataValue().longValue();
                    }
                    String strVariableName = aeNewBase.mstrVariableName;
                    Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                    // first lookup unknown variable list, then lookup known variable space.
                    if (var == null)	{
                        var = VariableOperator.lookUpSpaces(strVariableName, lVarNameSpaces);
                        if (var == null)	{
                            throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
                        }
                    }
                    DataClass datumValue = new DataClass();
                    if ((var instanceof UnknownVariable && ((UnknownVariable)var).isValueAssigned())
                            || var instanceof Variable)	{
                        datumValue = var.getValue();
                    }
                    datumValue.createDataAtIndexByRef(nIndexArray, datumReturn, new DataClass());
                    var.setValue(datumValue);
                }
            } else	{
                throw new JSmartMathErrException(ERRORTYPES.ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE);
            }
        }
        return datumReturn;
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        AbstractExpr aeReturn = mlistChildren.getLast().evaluateAExpr(lUnknownVars, lVarNameSpaces);
        DataClass datumReturn = (aeReturn instanceof AEConst)?((AEConst)aeReturn).getDataClass():null;   // use data ref coz aeReturn may be a matrix.
        listNewChildren.addFirst(aeReturn);
        for (int idx = mlistChildren.size() - 2; idx >= 0 ; idx --)	{	// have to go through from last to first consider the following case:
                                                                        // x[y] = y = 3. If y is not known, x[y] cannot be evaluated.
            // if child idx hasn't been simplified, child idx - 1 cannot be simplified considering the situation y = -1; x[y] = y = 3
            if (mlistChildren.get(idx).isVariable())	{
                if (mlistChildren.get(idx) instanceof AEVar)	{
                    if (aeReturn instanceof AEConst) {
                        String strVariableName = ((AEVar)mlistChildren.get(idx)).mstrVariableName;
                        Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                        // first lookup unknown variable list, then lookup known variable space.
                        if (var == null)	{
                            var = VariableOperator.lookUpSpaces(strVariableName, lVarNameSpaces);
                            if (var == null)	{
                                throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
                            }
                        }
                        DataClass datumValue = new DataClass();
                        datumValue.copyTypeValue(datumReturn);  // do not deep copy considering a matrix (reference copy)
                        var.setValue(datumValue);
                    } else {
                        listNewChildren.addFirst(mlistChildren.get(idx));
                    }
                } else	{	// AEIndex with base is a variable.
                    AbstractExpr aeBase = ((AEIndex)mlistChildren.get(idx)).maeBase;
                    AbstractExpr aeIndex = ((AEIndex)mlistChildren.get(idx)).maeIndex;
                    AbstractExpr aeMerged = mlistChildren.get(idx);
                    try	{
                        aeMerged = AEIndex.mergeIndex(aeBase, aeIndex);
                    } catch (JSmartMathErrException e)	{
                        if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
                            throw e;
                        }
                    }
                    // now aeMerged must be an AEIndex type, and it's base must be a variable.
                    AEVar aeNewBase = (AEVar)((AEIndex)aeMerged).maeBase;
                    AbstractExpr aeNewIndex = ((AEIndex)aeMerged).maeIndex.evaluateAExpr(lUnknownVars, lVarNameSpaces);
                    if (aeReturn instanceof AEConst && aeNewIndex instanceof AEConst) {
                        DataClass datumNewIndex = ((AEConst)aeNewIndex).getDataClassRef();
                        DataClass[] datumIndexList = datumNewIndex.getDataList();
                        int[] nIndexArray = new int[datumIndexList.length];
                        for (int idx1 = 0; idx1 < datumIndexList.length; idx1 ++)	{
                            DataClass datumThisIdx = new DataClass();
                            datumThisIdx.copyTypeValueDeep(datumIndexList[idx1]);
                            datumThisIdx.changeDataType(DATATYPES.DATUM_INTEGER);
                            nIndexArray[idx1] = (int)datumThisIdx.getDataValue().longValue();
                        }
                        String strVariableName = aeNewBase.mstrVariableName;
                        Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
                        // first lookup unknown variable list, then lookup known variable space.
                        if (var == null)	{
                            var = VariableOperator.lookUpSpaces(strVariableName, lVarNameSpaces);
                            if (var == null)	{
                                throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
                            }
                        }
                        DataClass datumValue = new DataClass();
                        if ((var instanceof UnknownVariable && ((UnknownVariable)var).isValueAssigned())
                                || var instanceof Variable)	{
                            datumValue = var.getValue();
                        }
                        DataClass datumReturnCp = new DataClass();
                        datumReturnCp.copyTypeValue(datumReturn);  // do not deep copy considering a matrix (reference copy).
                        datumValue.createDataAtIndexByRef(nIndexArray, datumReturnCp, new DataClass());
                        var.setValue(datumValue);
                    } else {
                        AEIndex aeNewChild = new AEIndex(aeNewBase, aeNewIndex);
                        listNewChildren.addFirst(aeNewChild);
                    }
                }
            } else	{
                throw new JSmartMathErrException(ERRORTYPES.ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE);
            }
        }
        if (listNewChildren.size() == 1) {
            return aeReturn;
        } else {
            return new AEAssign(listNewChildren);
        }        
    }
	// exceptions like cannot merge or unknown variable should be handled internally in this function.
	// if an exception is thrown out, something is wrong.
	@Override
	public AbstractExpr simplifyAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr();

        // ensure that this will not be changed.
        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
        listChildren.addAll(mlistChildren);
        
		// only simplify the last one, other children must be variables and should
		// be simplified gradually.
		AbstractExpr aexpr = listChildren.getLast().simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
        listChildren.set(listChildren.size() - 1, aexpr);
		
		// merge children.
		for (int idx = listChildren.size() - 1; idx >= 1; idx --)	{
			try {
				AbstractExpr aeMerged = mergeAssign(listChildren.get(idx - 1), listChildren.get(idx));
				listChildren.remove(idx - 1);
				listChildren.remove(idx - 1);
				listChildren.add(idx - 1, aeMerged);
			} catch (JSmartMathErrException e)	{
				if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
					throw e;
				}
			}
		}
		
		if (listChildren.getLast() instanceof AEConst)	{
			DataClass datumReturn = ((AEConst)listChildren.getLast()).getDataClass();    // getDataClass will automatically determine whether to deepcopy or not.
			
			for (int idx = listChildren.size() - 2; idx >= 0 ; idx --)	{	// have to go through from last to first consider the following case:
																			// x[y] = y = 3. If y is not known, x[y] cannot be evaluated.
				// if child idx hasn't been simplified, child idx - 1 cannot be simplified considering the situation y = -1; x[y] = y = 3
				boolean bChildCanBeRemoved = true;
				if (listChildren.get(idx).isVariable())	{
					if (listChildren.get(idx) instanceof AEVar)	{
						String strVariableName = ((AEVar)listChildren.get(idx)).mstrVariableName;
						Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
						// first lookup unknown variable list, then lookup known variable space.
						if (var == null)	{
							var = VariableOperator.lookUpSpaces(strVariableName, lVarNameSpaces);
							if (var == null)	{
								throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
							}
						}
						DataClass datumValue = new DataClass();
						datumValue.copyTypeValue(datumReturn);  // do not deep copy considering a matrix (reference copy).
						var.setValue(datumValue);
					} else	{	// AEIndex with base is a variable.
						AbstractExpr aeBase = ((AEIndex)listChildren.get(idx)).maeBase;
						AbstractExpr aeIndex = ((AEIndex)listChildren.get(idx)).maeIndex;
						AbstractExpr aeMerged = listChildren.get(idx);
						try	{
							aeMerged = AEIndex.mergeIndex(aeBase, aeIndex);
						} catch (JSmartMathErrException e)	{
							if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
								throw e;
							}
						}
						// now aeMerged must be an AEIndex type, and it's base must be a variable.
						AEVar aeNewBase = (AEVar)((AEIndex)aeMerged).maeBase;
						AbstractExpr aeNewIndex = ((AEIndex)aeMerged).maeIndex.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
						if (!(aeNewIndex instanceof AEConst))	{
							// some indices are still unknown.
							listChildren.set(idx, aeMerged);
							bChildCanBeRemoved = false;
						} else	{
							DataClass[] datumIndexList = ((AEConst)aeNewIndex).getDataClassCopy().getDataList();
							int[] nIndexArray = new int[datumIndexList.length];
							for (int idx1 = 0; idx1 < datumIndexList.length; idx1 ++)	{
								datumIndexList[idx1].changeDataType(DATATYPES.DATUM_INTEGER);
								nIndexArray[idx1] = (int)datumIndexList[idx].getDataValue().longValue();
							}
							String strVariableName = aeNewBase.mstrVariableName;
							Variable var = UnknownVarOperator.lookUpList(strVariableName, lUnknownVars);
							// first lookup unknown variable list, then lookup known variable space.
							if (var == null)	{
								var = VariableOperator.lookUpSpaces(strVariableName, lVarNameSpaces);
								if (var == null)	{
									throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
								}
							}
							DataClass datumValue = new DataClass();
							if ((var instanceof UnknownVariable && ((UnknownVariable)var).isValueAssigned())
									|| var instanceof Variable)	{
								datumValue = var.getValue();
							}
							DataClass datumReturnCp = new DataClass();
							datumReturnCp.copyTypeValue(datumReturn);  // do not deep copy considering a matrix (reference copy).
							datumValue.createDataAtIndexByRef(nIndexArray, datumReturnCp, new DataClass());
                            datumValue.validateDataClass(); // prevent refer to itself
							var.setValue(datumValue);
						}
					}
				} else	{
					throw new JSmartMathErrException(ERRORTYPES.ERROR_ONLY_VARIABLE_CAN_BE_ASSIGNED_A_VALUE);
				}
				if (bChildCanBeRemoved)	{
					// the variable has been known, remove it from child list.
					// or if it is not a variable, remove it anyway.
					listChildren.remove(idx);
					idx --;
				}
			}
		}
		if (listChildren.size() == 1)	{
			return listChildren.get(0);	// if a variable aexpr, it has been converted to const in its simplifyexpr function.
		} else	{
			return new AEAssign(listChildren);
		}
	}
    
    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if (enumAET == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_ASSIGN
                && (nLeftOrRight >= 0)) {   // like (a = b) = c or d = (a = b) = c
            return true;
        } else if (enumAET.getValue() > ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())    {
            return true;
        }
        return false;
    }
    
	@Override
	public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
		if (menumAEType.getValue() < aexpr.menumAEType.getValue())	{
			return 1;
		} else if (menumAEType.getValue() > aexpr.menumAEType.getValue())	{
			return -1;
		} else	{
			int nChildrenListSize1 = mlistChildren.size();
			int nChildrenListSize2 = ((AEAssign)aexpr).mlistChildren.size();
            for (int idx = Math.min(nChildrenListSize1, nChildrenListSize2) - 1; idx >= 0; idx --)	{
                int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEAssign)aexpr).mlistChildren.get(idx));
                if (nCompareChildReturn != 0)	{
                    return nCompareChildReturn;
                }
            }
			if (nChildrenListSize1 > nChildrenListSize2)	{
				return 1;
			} else if (nChildrenListSize1 < nChildrenListSize2)	{
				return -1;
			} else	{
				return 0;
			}
		}
	}
	
	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException	{
		validateAbstractExpr();
		return mlistChildren.getLast().isNegligible();
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
		String strOutput = "";
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
            boolean bNeedBracketsWhenToStr = false;
            if (idx > 0 && idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 0);
            } else if (idx == 0 && idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 1);
            } else if (idx > 0 && idx == mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, -1);
            }
            if (bNeedBracketsWhenToStr) {
                strOutput += "(" + mlistChildren.get(idx).output() + ")";
            } else  {
                strOutput += mlistChildren.get(idx).output();
            }
			if (idx != mlistChildren.size() - 1)	{
				strOutput += "=";
			}
		}
		return strOutput;
	}
	
	// note that parameters of this function will be changed inside.
	public static AbstractExpr mergeAssign(AbstractExpr aeLeft, AbstractExpr aeRight) throws JFCALCExpErrException, JSmartMathErrException	{
		if (aeRight instanceof AEAssign)	{
            AEAssign aeReturn = new AEAssign();
            aeReturn.copy(aeRight);
			aeReturn.mlistChildren.addFirst(aeLeft);
			return aeReturn;
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}
	
	public AEAssign[] splitIntoExprs() throws JSmartMathErrException, JFCALCExpErrException {
		/*
		 * convert like y[x] = x = 5 to x = 5 and y[x] = x
		 */
		validateAbstractExpr();

		AEAssign[] aearrayExprs = new AEAssign[mlistChildren.size() - 1];
		for (int idx = mlistChildren.size() - 1, idx1 = 0; idx >= 1; idx --, idx1 ++)	{
			LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
			AbstractExpr child1 = mlistChildren.get(idx - 1);
			AbstractExpr child2 = mlistChildren.get(idx);
			listChildren.add(child1);
			listChildren.add(child2);
			aearrayExprs[idx1] = new AEAssign(listChildren);
		}
		return aearrayExprs;
	}

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        LinkedList<AbstractExpr> listChildrenCvted = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx) instanceof AEConst) {
                listChildrenCvted.add(mlistChildren.get(idx));
            } else {
                listChildrenCvted.add(mlistChildren.get(idx).convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars));
            }
        }
        return new AEAssign(listChildrenCvted);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        LinkedList<AbstractExpr> listChildrenCvted = new LinkedList<AbstractExpr>();
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            if (mlistChildren.get(idx) instanceof AEConst
                    && ((AEConst)mlistChildren.get(idx)).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                listChildrenCvted.add(((AEConst)mlistChildren.get(idx)).getDataClassRef().getAExpr());
            } else {
                listChildrenCvted.add(mlistChildren.get(idx));
            }
        }
        return new AEAssign(listChildrenCvted);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = 0;
        for (int idx = 0; idx < mlistChildren.size(); idx ++) {
            nCnt += mlistChildren.get(idx).getVarAppearanceCnt(strVarName);
        }
        return nCnt;
    }
}
