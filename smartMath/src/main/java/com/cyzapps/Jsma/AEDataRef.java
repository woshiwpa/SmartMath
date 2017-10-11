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
import com.cyzapps.adapter.MFPAdapter.FunctionEntry;

public class AEDataRef extends AbstractExpr {
	public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>();

	public AEDataRef() {
		initAbstractExpr();
	}
	
	public AEDataRef(LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException	{
		setAEDataRef(listChildren);
	}

	public AEDataRef(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREF;
		mlistChildren = new LinkedList<AbstractExpr>();
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException	{
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREF)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}		
	}
	
	private void setAEDataRef(LinkedList<AbstractExpr> listChildren) throws JSmartMathErrException	{
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREF;
		mlistChildren = (listChildren == null)?new LinkedList<AbstractExpr>():listChildren;
		validateAbstractExpr();
	}
	
	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEDataRef)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		mlistChildren = new LinkedList<AbstractExpr>();
		if (((AEDataRef)aexprOrigin).mlistChildren != null)	{
			for (int idx = 0; idx < ((AEDataRef)aexprOrigin).mlistChildren.size(); idx ++)	{
				mlistChildren.add(((AEDataRef)aexprOrigin).mlistChildren.get(idx));
			}
		}
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEDataRef)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		mlistChildren = new LinkedList<AbstractExpr>();
		if (((AEDataRef)aexprOrigin).mlistChildren != null)	{
			for (int idx = 0; idx < ((AEDataRef)aexprOrigin).mlistChildren.size(); idx ++)	{
				AbstractExpr aexprChild = ((AEDataRef)aexprOrigin).mlistChildren.get(idx).cloneSelf();
				mlistChildren.add(aexprChild);
			}
		}
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException	{
		AbstractExpr aeReturn = new AEDataRef();
		aeReturn.copyDeep(this);
		return aeReturn;
	}
	
	// the dimension cannot be accurately evaluated.
	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();
		
		int nFirstDimLen = mlistChildren.size();
		int[] narrayFollowingDims = new int[0];
		for (int idx = 0; idx < nFirstDimLen; idx ++)	{
			int[] narrayChildDim = mlistChildren.get(idx).recalcAExprDim(bUnknownAsSingle);
			if (narrayFollowingDims.length < narrayChildDim.length)	{
				int[] narrayNewDim = new int[narrayChildDim.length];
				for (int idx1 = 0; idx1 < narrayChildDim.length; idx1 ++)	{
					if (idx1 < narrayFollowingDims.length)	{
						narrayNewDim[idx1] = Math.max(narrayFollowingDims[idx1], narrayChildDim[idx1]);
					} else	{
						narrayNewDim[idx1] = narrayChildDim[idx1];
					}
				}
				narrayFollowingDims = narrayNewDim;
			} else	{
				for (int idx1 = 0; idx1 < narrayChildDim.length; idx1 ++)	{
					narrayFollowingDims[idx1] = Math.max(narrayFollowingDims[idx1], narrayChildDim[idx1]);
				}
			}
		}
		int[] narrayDim = new int[narrayFollowingDims.length + 1];
		narrayDim[0] = nFirstDimLen;
		for (int idx = 0; idx < narrayFollowingDims.length; idx ++)	{
			narrayDim[idx + 1] = narrayFollowingDims[idx];
		}
		return narrayDim;
	}

	// the minimum dimension of AEDataRef.
	public int[] calcAExprMinDim() throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
		
		int nFirstDimLen = mlistChildren.size();
		int[] narrayFollowingDims = new int[0];
		for (int idx = 0; idx < nFirstDimLen; idx ++)	{
			int[] narrayChildDim = new int[0];
			if (mlistChildren.get(idx) instanceof AEDataRef)	{
				narrayChildDim = ((AEDataRef)mlistChildren.get(idx)).calcAExprMinDim();
			} else if (mlistChildren.get(idx) instanceof AEConst)	{
				narrayChildDim = ((AEConst)mlistChildren.get(idx)).recalcAExprDim(true);
			}
			
			if (narrayFollowingDims.length < narrayChildDim.length)	{
				int[] narrayNewDim = new int[narrayChildDim.length];
				for (int idx1 = 0; idx1 < narrayChildDim.length; idx1 ++)	{
					if (idx1 < narrayFollowingDims.length)	{
						narrayNewDim[idx1] = Math.max(narrayFollowingDims[idx1], narrayChildDim[idx1]);
					} else	{
						narrayNewDim[idx1] = narrayChildDim[idx1];
					}
				}
				narrayFollowingDims = narrayNewDim;
			} else	{
				for (int idx1 = 0; idx1 < narrayChildDim.length; idx1 ++)	{
					narrayFollowingDims[idx1] = Math.max(narrayFollowingDims[idx1], narrayChildDim[idx1]);
				}
			}
		}
		int[] narrayDim = new int[narrayFollowingDims.length + 1];
		narrayDim[0] = nFirstDimLen;
		for (int idx = 0; idx < narrayFollowingDims.length; idx ++)	{
			narrayDim[idx + 1] = narrayFollowingDims[idx];
		}
		return narrayDim;
	}

    @Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (mlistChildren.size() != ((AEDataRef)aexpr).mlistChildren.size())	{
			return false;
		} else {
			for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
				if (mlistChildren.get(idx).isEqual(((AEDataRef)aexpr).mlistChildren.get(idx)) == false)	{
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
                                boolean bAllowConversion) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException  {
        /* do not call isPatternDegrade function because dataref expression cannot degrade-match a pattern.*/
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
        if (mlistChildren.size() != ((AEDataRef)aePattern).mlistChildren.size())    {
            return false;
        }
        for (int idx = 0; idx < mlistChildren.size(); idx ++)   {
            if (mlistChildren.get(idx).isPatternMatch(((AEDataRef)aePattern).mlistChildren.get(idx),
                    listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false)   {
                return false;
            }
        }
        
        return true;
    }
    
	@Override
	public boolean isKnownValOrPseudo() {
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			if (!mlistChildren.get(idx).isKnownValOrPseudo())	{
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
        AEDataRef aeReturn = new AEDataRef();
        aeReturn.copy(this);
        aeReturn.mlistChildren = listChildren == null?new LinkedList<AbstractExpr>():listChildren;
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }	
	
	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
	public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException	{
		AEDataRef aeReturn = new AEDataRef();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)	{
			for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)	{
				if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit))	{
					aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);	// actually need to clone because will be many aeTo copies. Otherwise parent cannot be identified. However, at this moment don't care.
					listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
					break;
				} else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit))	{
					aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);	// actually need to clone because will be many aeTo copies. Otherwise parent cannot be identified. However, at this moment don't care.
					listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
					break;
				}
			}
		}
		return aeReturn;
	}

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
		DataClass[] datumList = new DataClass[mlistChildren.size()];
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			DataClass datumChild = mlistChildren.get(idx).evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
            datumList[idx] = datumChild;    // no need to deep copy
		}
        DataClass datumReturn = new DataClass();
		datumReturn.setDataList(datumList);
		return datumReturn;
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
		DataClass[] datumList = new DataClass[mlistChildren.size()];
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        boolean bAllChildrenKnownValues = true;
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			AbstractExpr aeChild = mlistChildren.get(idx).evaluateAExpr(lUnknownVars, lVarNameSpaces);
            if (aeChild instanceof AEConst) {
                datumList[idx] = ((AEConst)aeChild).getDataClass(); // let getDataClass determine deep copy or not.
            } else {
                bAllChildrenKnownValues = false;
            }
            listNewChildren.add(aeChild);
		}
        if (bAllChildrenKnownValues) {
            DataClass datumReturn = new DataClass();
            datumReturn.setDataList(datumList);
            return new AEConst(datumReturn);
        } else {
            return new AEDataRef(listNewChildren);
        }
    }

    @Override
	public AbstractExpr simplifyAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr();
		boolean bIsConst = true;
        AEDataRef aeReturn = new AEDataRef();
        aeReturn.copy(this);
		DataClass[] datumList = new DataClass[aeReturn.mlistChildren.size()];
		for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)	{
			AbstractExpr aeChild = aeReturn.mlistChildren.get(idx).simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
			if (aeChild instanceof AEConst)	{
				datumList[idx] = ((AEConst) aeChild).getDataClass();    //getDataClass will automatically determine when to deep copy when not to deep copy.
			} else	{
				bIsConst = false;
			}
			aeReturn.mlistChildren.set(idx, aeChild);
		}
		if (bIsConst)	{
			DataClass datumReturn = new DataClass();
			datumReturn.setDataList(datumList);
			return new AEConst(datumReturn);
		} else	{
			return aeReturn;
		}
	}

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        return false;
    }
    
	@Override
	public int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException {
		if (menumAEType.getValue() < aexpr.menumAEType.getValue())	{
			return 1;
		} else if (menumAEType.getValue() > aexpr.menumAEType.getValue())	{
			return -1;
		} else	{
            // when comparing children, data reference is a bit different from posneg sign
			int nChildrenListSize1 = mlistChildren.size();
			int nChildrenListSize2 = ((AEDataRef)aexpr).mlistChildren.size();
			if (nChildrenListSize1 > nChildrenListSize2)	{
				return 1;
			} else if (nChildrenListSize1 < nChildrenListSize2)	{
				return -1;
			} else	{
				for (int idx = 0; idx < nChildrenListSize1; idx ++)	{
					int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEDataRef)aexpr).mlistChildren.get(idx));
					if (nCompareChildReturn != 0)	{
						return nCompareChildReturn;
					}
				}
				return 0;
			}
		}
	}
	
	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException	{
		validateAbstractExpr();
		boolean bIsNegligible = true;
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			bIsNegligible &= mlistChildren.get(idx).isNegligible();
		}
		return bIsNegligible;
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
		String strOutput = "[";
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
            // we do not need to consider use ().
			strOutput += mlistChildren.get(idx).output();
			if (idx < mlistChildren.size() - 1)	{
				strOutput += ",";
			} else	{
				strOutput += "]";
			}
		}
		return strOutput;
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
        return new AEDataRef(listChildrenCvted);
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
        return new AEDataRef(listChildrenCvted);
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
