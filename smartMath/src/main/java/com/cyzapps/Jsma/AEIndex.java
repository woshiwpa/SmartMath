package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEIndex extends AbstractExpr {

    public AbstractExpr maeBase = AEInvalid.AEINVALID;
	public AbstractExpr maeIndex = AEInvalid.AEINVALID;
	
	public AEIndex() {
		initAbstractExpr();
	}
	
	public AEIndex(AbstractExpr aeBase, AbstractExpr aeIndex) throws JSmartMathErrException	{
		setAEIndex(aeBase, aeIndex);
	}

    public AEIndex(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException    {
        copy(aexprOrigin);
    }
    
	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX;
		maeBase = AEInvalid.AEINVALID;
		maeIndex = AEInvalid.AEINVALID;
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException {
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}
		if (maeIndex instanceof AEConst)	{
			if (((AEConst)maeIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
			}
		} else if (!(maeIndex instanceof AEDataRef))	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
		// test if indices are integers will be left in the simplify
	}
	
	private void setAEIndex(AbstractExpr aeBase, AbstractExpr aeIndex) throws JSmartMathErrException	{
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX;
		maeBase = aeBase;
		maeIndex = aeIndex;
		validateAbstractExpr();
	}
	
	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEIndex)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		
		maeBase = ((AEIndex)aexprOrigin).maeBase;
		maeIndex = ((AEIndex)aexprOrigin).maeIndex;
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEIndex)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		
		maeBase = ((AEIndex)aexprOrigin).maeBase.cloneSelf();
		maeIndex = ((AEIndex)aexprOrigin).maeIndex.cloneSelf();
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException,
			JSmartMathErrException {
		AbstractExpr aeReturn = new AEIndex();
		aeReturn.copyDeep(this);
		return aeReturn;
	}

	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();
		
		int[] nIndexArray = new int[0];
		if (maeIndex instanceof AEConst)	{
			// this dataclass has been validated, it must be a data reference
			DataClass[] datumIndexList = ((AEConst)maeIndex).getDataClassCopy().getDataList();
			nIndexArray = new int[datumIndexList.length];
			for (int idx = 0; idx < datumIndexList.length; idx ++)	{
				datumIndexList[idx].changeDataType(DATATYPES.DATUM_INTEGER);
				nIndexArray[idx] = (int)datumIndexList[idx].getDataValue().longValue();
			}
		} else	{
			// must be AEDATAREF
			nIndexArray = new int[((AEDataRef)maeIndex).mlistChildren.size()];
			for (int idx = 0; idx < nIndexArray.length; idx ++)	{
				if (!(((AEDataRef)maeIndex).mlistChildren.get(idx) instanceof AEConst))	{   // the index is not know
                    if (bUnknownAsSingle)   {
                        return new int[0];  // index is unknown, so we assume the indexed value is a single value if bUnknownAsSingle is flagged
                    } else {
                        throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
                    }
				} else	{
					// need not to getDatumValue cause it has to be changed to integer anyway.
					DataClass datumIdx = ((AEConst)((AEDataRef)maeIndex).mlistChildren.get(idx)).getDataClassCopy();
					datumIdx.changeDataType(DATATYPES.DATUM_INTEGER);
					nIndexArray[idx] = (int)datumIdx.getDataValue().longValue();
				}
			}
		}
		if (!(maeBase instanceof AEConst))	{
            if (bUnknownAsSingle)   {
                return new int[0];
            } else {
                throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
            }
		} else	{
			DataClass datumChild = ((AEConst)maeBase).getDataClassRef().getDataAtIndexByRef(nIndexArray);   // because it is a matrix, so use getDataClassRef() instead of getDataClass()
			return datumChild.recalcDataArraySize();
		}
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (maeBase.isEqual(((AEIndex)aexpr).maeBase) == false)	{
			return false;
		} else if (maeIndex.isEqual(((AEIndex)aexpr).maeIndex) == false)	{
			return false;
		} else {
			return true;
		}
	}

    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        /* do not call isPatternDegrade function because generally index expression cannot degrade-match a pattern.*/
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
        if (!(aePattern instanceof AEDataRef))  {
            return false;
        }
        if (maeBase.isPatternMatch(((AEIndex)aePattern).maeBase, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false)  {
            return false;
        }
        if (maeIndex.isPatternMatch(((AEIndex)aePattern).maeIndex, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false)  {
            return false;
        } 
        
        return true;
    }
    
	@Override
	public boolean isKnownValOrPseudo() {
		if (!maeBase.isKnownValOrPseudo())	{
			return false;
		}
		if (!maeIndex.isKnownValOrPseudo())	{
			return false;
		}
		return true;
	}
	
	@Override
	public boolean isVariable() {
		return maeBase.isVariable();
	}

	@Override
	public LinkedList<AbstractExpr> getListOfChildren() {
		LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
		listChildren.add(maeBase);
		listChildren.add(maeIndex);
		return listChildren;
	}
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);			
        }
        AEIndex aeReturn = new AEIndex();
        aeReturn.copy(this);
        aeReturn.maeBase = listChildren.getFirst();
        aeReturn.maeIndex = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
	
	@Override
	public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException	{
		AEIndex aeReturn = new AEIndex();
        aeReturn.copy(this);
        for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeBase.isEqual(aeFrom))	{
				aeReturn.maeBase = aeTo;
				listReplacedChildren.add(aeReturn.maeBase);
				break;
			}
		}
		
		for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeIndex.isEqual(aeFrom))	{
				aeReturn.maeIndex = aeTo;
				listReplacedChildren.add(aeReturn.maeIndex);
				break;
			}
		}
		return aeReturn;
	}

	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException,
			JSmartMathErrException {
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
        DataClass datumBase = maeBase.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
		DataClass datumIndex = maeIndex.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
        if (datumIndex.getDataType() != DATATYPES.DATUM_REF_DATA)	{
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
        }
        DataClass[] datumIndexList = datumIndex.getDataList();
        int[] nIndexArray = new int[datumIndexList.length];
        for (int idx = 0; idx < datumIndexList.length; idx ++)	{
            DataClass datumThisIdx = new DataClass();
            datumThisIdx.copyTypeValueDeep(datumIndexList[idx]);
            datumThisIdx.changeDataType(DATATYPES.DATUM_INTEGER);
            nIndexArray[idx] = (int)datumThisIdx.getDataValue().longValue();
        }
        return datumBase.getDataAtIndex(nIndexArray);
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeBase = maeBase.evaluateAExpr(lUnknownVars, lVarNameSpaces);
		AbstractExpr aeIndex = maeIndex.evaluateAExpr(lUnknownVars, lVarNameSpaces);
        if (aeBase instanceof AEConst && aeIndex instanceof AEConst) {
            DataClass datumBase = ((AEConst)aeBase).getDataClass();
            DataClass datumIndex = ((AEConst)aeIndex).getDataClass();
            if (datumIndex.getDataType() != DATATYPES.DATUM_REF_DATA)	{
                throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
            }
            DataClass[] datumIndexList = datumIndex.getDataList();
            int[] nIndexArray = new int[datumIndexList.length];
            for (int idx = 0; idx < datumIndexList.length; idx ++)	{
                DataClass datumThisIdx = new DataClass();
                datumThisIdx.copyTypeValueDeep(datumIndexList[idx]);
                datumThisIdx.changeDataType(DATATYPES.DATUM_INTEGER);
                nIndexArray[idx] = (int)datumThisIdx.getDataValue().longValue();
            }
            return new AEConst(datumBase.getDataAtIndex(nIndexArray));
        } else {
            return new AEIndex(aeBase, aeIndex);
        }
    }

	@Override
	public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();
		
		AbstractExpr aeBase = maeBase.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		AbstractExpr aeIndex = maeIndex.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		
		AbstractExpr aeReturn = this;
		try	{
			// different from merge power, need not use cloneSelf in mergeIndex
			// Although maeIndex's children may be converted to integer, this is
			// what we desire.
			aeReturn = mergeIndex(aeBase, aeIndex);
		} catch (JSmartMathErrException e)	{
			if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
				throw e;
			}
		}
		aeReturn = aeReturn.distributeAExpr(simplifyParams);
		return aeReturn;
	}

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if (enumAET == ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX && nLeftOrRight <= 0)   { // like [[1,2],[3,4]]([1,[0]][1])
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
			int nReturn = maeBase.compareAExpr(((AEIndex)aexpr).maeBase);
			if (nReturn != 0)	{
				nReturn = maeIndex.compareAExpr(((AEIndex)aexpr).maeIndex);
			}
			return nReturn;
		}
	}

	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException	{
		validateAbstractExpr();
		if (maeBase.isNegligible())	{
			return true;
		}
		return false;	// because this aexpr has been simplified most, but it is
						// still a AEIndex. This implies that maeIndex is also not
						// a constant as maeBase. This also means the value of this
						// aexpr is not known so far.
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
        boolean bBaseNeedBracketsWhenToStr = false;
        bBaseNeedBracketsWhenToStr = maeBase.needBracketsWhenToStr(menumAEType, 1);
        
		String strOutput = "";
        if (bBaseNeedBracketsWhenToStr) {
            strOutput += "(" + maeBase.output() + ")";
        } else  {
            strOutput += maeBase.output();
        }
        strOutput += maeIndex.output();
		return strOutput;
	}
	
	public static AbstractExpr mergeIndex(AbstractExpr aeBase, AbstractExpr aeIndex) throws JFCALCExpErrException, JSmartMathErrException	{
		if (aeBase instanceof AEConst && aeIndex instanceof AEConst)	{
			// this dataclass has been validated, it must be a data reference
			if (((AEConst)aeIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                    && (((AEConst)aeIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR
                        || !(((AEConst)aeIndex).getDataClassRef().getAExpr() instanceof AEDataRef)))	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
			}
            boolean bIsIdxAExpr = (((AEConst)aeIndex).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR);
            AbstractExpr aeNewIndex = aeIndex;
            if (bIsIdxAExpr) {
                aeNewIndex = ((AEConst)aeIndex).getDataClassRef().getAExpr();   // if index is an aexpr data.
            } /*
             * // aexpr data inside a data reference data is not allowed. Otherwise, evaluate2OperandCalc, evaluate1OperandCalc will fail. too complicated.
             * else if (((AEConst)aeIndex).getDataClassRef().getDataType() == DATATYPES.DATUM_REF_DATA) {
                DataClass[] arrayDataChildren = ((AEConst)aeIndex).getDataClassRef().getDataList();
                LinkedList<AbstractExpr> listAEChildren = new LinkedList<AbstractExpr>();
                for (int idx = 0; idx < arrayDataChildren.length; idx ++) {
                    if (arrayDataChildren[idx].getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                        listAEChildren.add(new AEConst(arrayDataChildren[idx]));
                    } else {
                        bIsIdxAExpr = true;
                        listAEChildren.add(arrayDataChildren[idx].getAExpr());
                    }
                }
                if (bIsIdxAExpr) {  //if index is a data array but includes aexpr children.
                    aeNewIndex = new AEDataRef(listAEChildren);
                }
            }*/
            
            if (((AEConst)aeBase).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR
                    || ((AEConst)aeIndex).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                // we assume aeIndex has been converted to an aexpr datum, i.e. situation like [datum, datum, aexpr datum] does not exist
                AbstractExpr aeNewBase = (((AEConst)aeBase).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR)?
                                            ((AEConst)aeBase).getDataClassRef().getAExpr():aeBase;
                AbstractExpr aeMerged = new AEIndex(aeNewBase, aeNewIndex);
                return new AEConst(new DataClass(aeMerged));
            } else {
                DataClass[] datumIndexList = ((AEConst)aeIndex).getDataClassCopy().getDataList();
                int[] nIndexArray = new int[datumIndexList.length];
                for (int idx = 0; idx < datumIndexList.length; idx ++)	{
                    datumIndexList[idx].changeDataType(DATATYPES.DATUM_INTEGER);
                    nIndexArray[idx] = (int)datumIndexList[idx].getDataValue().longValue();
                }
                DataClass datumBase = ((AEConst)aeBase).getDataClassRef();    //    // because it is a matrix, so use getDataClassRef() instead of getDataClass()
                return new AEConst(datumBase.getDataAtIndex(nIndexArray));  // data in AEConst is not immutable.
            }
		}
		AbstractExpr aeReturn = new AEIndex(aeBase, aeIndex);
		if (aeBase instanceof AEIndex)	{
			AbstractExpr aeNewBase = ((AEIndex)aeBase).maeBase;
			AbstractExpr aeLowerLevelIndex = ((AEIndex)aeBase).maeIndex;
			if (aeLowerLevelIndex instanceof AEConst && aeIndex instanceof AEConst)	{
				if (((AEConst)aeLowerLevelIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
						|| ((AEConst)aeIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)	{
					throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
				}
				DataClass[] datumLowerIndexList = ((AEConst)aeLowerLevelIndex).getDataClassCopy().getDataList();
				DataClass[] datumIndexList = ((AEConst)aeIndex).getDataClassCopy().getDataList();
				DataClass[] datumNewIndexList = new DataClass[datumIndexList.length + datumLowerIndexList.length];
				for (int idx = 0; idx < datumNewIndexList.length; idx ++)	{
					if (idx < datumLowerIndexList.length)	{
						datumNewIndexList[idx] = datumLowerIndexList[idx];
						datumNewIndexList[idx].changeDataType(DATATYPES.DATUM_INTEGER);
					} else	{
						datumNewIndexList[idx] = datumIndexList[idx - datumLowerIndexList.length];
						datumNewIndexList[idx].changeDataType(DATATYPES.DATUM_INTEGER);
					}
				}
				DataClass datumNewIndex = new DataClass(datumNewIndexList);
				AbstractExpr aeNewIndex = new AEConst(datumNewIndex);
				aeReturn = new AEIndex(aeNewBase, aeNewIndex);				
			} else if (aeLowerLevelIndex instanceof AEDataRef && aeIndex instanceof AEConst)	{
				if (((AEConst)aeIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)	{
					throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
				}
				LinkedList<AbstractExpr> listNewIndexChildren = new LinkedList<AbstractExpr>();
				listNewIndexChildren.addAll(((AEDataRef)aeLowerLevelIndex).mlistChildren);
				DataClass[] datumIndexList = ((AEConst)aeIndex).getDataClassCopy().getDataList();
				for (int idx = 0; idx < datumIndexList.length; idx ++)	{
					datumIndexList[idx].changeDataType(DATATYPES.DATUM_INTEGER);
					listNewIndexChildren.add(new AEConst(datumIndexList[idx]));
				}
				AbstractExpr aeNewIndex = new AEDataRef(listNewIndexChildren);
				aeReturn = new AEIndex(aeNewBase, aeNewIndex);				
			} else if (aeLowerLevelIndex instanceof AEConst && aeIndex instanceof AEDataRef)	{
				if (((AEConst)aeLowerLevelIndex).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA)	{
					throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
				}
				LinkedList<AbstractExpr> listNewIndexChildren = new LinkedList<AbstractExpr>();
				DataClass[] datumIndexList = ((AEConst)aeLowerLevelIndex).getDataClassCopy().getDataList();
				for (int idx = 0; idx < datumIndexList.length; idx ++)	{
					datumIndexList[idx].changeDataType(DATATYPES.DATUM_INTEGER);
					listNewIndexChildren.add(new AEConst(datumIndexList[idx]));
				}
				listNewIndexChildren.addAll(((AEDataRef)aeIndex).mlistChildren);
				AbstractExpr aeNewIndex = new AEDataRef(listNewIndexChildren);
				aeReturn = new AEIndex(aeNewBase, aeNewIndex);				
			} else if (aeLowerLevelIndex instanceof AEDataRef && aeIndex instanceof AEDataRef)	{
				LinkedList<AbstractExpr> listNewIndexChildren = new LinkedList<AbstractExpr>();
				listNewIndexChildren.addAll(((AEDataRef)aeLowerLevelIndex).mlistChildren);
				listNewIndexChildren.addAll(((AEDataRef)aeIndex).mlistChildren);
				AbstractExpr aeNewIndex = new AEDataRef(listNewIndexChildren);
				aeReturn = new AEIndex(aeNewBase, aeNewIndex);				
			} else	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
			}
			
			if (((AEIndex)aeReturn).maeBase instanceof AEIndex)	{
				// if the newly merged AExpr's base is still an AEIndex, merge again.
				aeReturn = mergeIndex(((AEIndex)aeReturn).maeBase, ((AEIndex)aeReturn).maeIndex);
			}
		}
		return aeReturn;	// do not throw cannot merge exception here.
	}
    
    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeBase = (maeBase instanceof AEConst)?maeBase:maeBase.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeIndex = (maeIndex instanceof AEConst)?maeIndex:maeIndex.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEIndex(aeBase, aeIndex);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeBase = maeBase, aeIndex = maeIndex;
        if (maeBase instanceof AEConst
                && ((AEConst)maeBase).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeBase = ((AEConst)maeBase).getDataClassRef().getAExpr();
        }
        if (maeIndex instanceof AEConst
                && ((AEConst)maeIndex).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeIndex = ((AEConst)maeIndex).getDataClassRef().getAExpr();
        }
        return new AEIndex(aeBase, aeIndex);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeBase.getVarAppearanceCnt(strVarName);
        nCnt += maeIndex.getVarAppearanceCnt(strVarName);
        return nCnt;
    }
}
