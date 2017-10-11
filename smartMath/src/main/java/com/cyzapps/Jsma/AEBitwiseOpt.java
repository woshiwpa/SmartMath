package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEBitwiseOpt extends AbstractExpr {

	// here mlistChildren and mlistOpts are not used because it leads to difficulty in merging.
	public AbstractExpr maeLeft = AEInvalid.AEINVALID, maeRight = AEInvalid.AEINVALID;
	
	public OPERATORTYPES moptType = OPERATORTYPES.OPERATOR_AND;	// by default it is and.
	
	public AEBitwiseOpt() {
		initAbstractExpr();
	}
	
	public AEBitwiseOpt(AbstractExpr aeLeft, OPERATORTYPES optType, AbstractExpr aeRight) throws JSmartMathErrException	{
		setAEBitwiseOpt(aeLeft, optType, aeRight);
	}

	public AEBitwiseOpt(AbstractExpr aeLeft, CalculateOperator opt, AbstractExpr aeRight) throws JSmartMathErrException	{
		setAEBitwiseOpt(aeLeft, opt.getOperatorType(), aeRight);
	}

	public AEBitwiseOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE;
		maeLeft = AEInvalid.AEINVALID;
		maeRight = AEInvalid.AEINVALID;
		moptType = OPERATORTYPES.OPERATOR_AND;
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException {
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}
		
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE)	{
			if (moptType != OPERATORTYPES.OPERATOR_AND && moptType != OPERATORTYPES.OPERATOR_OR
					&& moptType != OPERATORTYPES.OPERATOR_XOR)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);				
			}
		}
	}

	private void setAEBitwiseOpt(AbstractExpr aeLeft, OPERATORTYPES optType, AbstractExpr aeRight) throws JSmartMathErrException	{
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_BITWISE;
		maeLeft = aeLeft;
		moptType = optType;
		maeRight = aeRight;
		validateAbstractExpr();
	}

	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEBitwiseOpt)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		maeLeft = ((AEBitwiseOpt)aexprOrigin).maeLeft;
		maeRight = ((AEBitwiseOpt)aexprOrigin).maeRight;
		moptType = ((AEBitwiseOpt)aexprOrigin).moptType;
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEBitwiseOpt)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		maeLeft = ((AEBitwiseOpt)aexprOrigin).maeLeft.cloneSelf();
		maeRight = ((AEBitwiseOpt)aexprOrigin).maeRight.cloneSelf();
		moptType = ((AEBitwiseOpt)aexprOrigin).moptType;
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException,
			JSmartMathErrException {
		AbstractExpr aeReturn = new AEBitwiseOpt();
		aeReturn.copyDeep(this);
		return aeReturn;
	}

	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();

		return new int[0];	// bitwise operator always return a single value.
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (moptType != ((AEBitwiseOpt)aexpr).moptType)	{
			return false;
		} else if (!maeLeft.isEqual(((AEBitwiseOpt)aexpr).maeLeft))	{
			return false;
		} else if (!maeRight.isEqual(((AEBitwiseOpt)aexpr).maeRight))	{
			return false;
		} else	{
			return true;
		}
	}
   
    @Override
    public boolean isPatternMatch(AbstractExpr aePattern,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
                                LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
                                LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
                                boolean bAllowConversion)  throws JFCALCExpErrException, JSmartMathErrException, InterruptedException    {
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
        if (!(aePattern instanceof AEBitwiseOpt))   {
            return false;
        }
        if (moptType != ((AEBitwiseOpt)aePattern).moptType) {
            return false;
        }
        if (maeLeft.isPatternMatch(((AEBitwiseOpt)aePattern).maeLeft, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        if (maeRight.isPatternMatch(((AEBitwiseOpt)aePattern).maeRight, listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false) {
            return false;
        }
        
        return true;
    }
    
	@Override
	public boolean isKnownValOrPseudo() {
		if (!maeLeft.isKnownValOrPseudo())	{
			return false;
		}
		if (!maeRight.isKnownValOrPseudo())	{
			return false;
		}
		return true;
	}
	
	// note that the return list should not be changed.
	@Override
	public LinkedList<AbstractExpr> getListOfChildren()	{
		LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
		listChildren.add(maeLeft);
		listChildren.add(maeRight);
		return listChildren;
	}
    
    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren == null || listChildren.size() != 2) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);			
        }
        AEBitwiseOpt aeReturn = new AEBitwiseOpt();
        aeReturn.copy(this);
        aeReturn.maeLeft = listChildren.getFirst();
        aeReturn.maeRight = listChildren.getLast();
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
	
	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
	public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException	{
		AEBitwiseOpt aeReturn = new AEBitwiseOpt();
        aeReturn.copy(this);
		for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeLeft.isEqual(aeFrom))	{
				aeReturn.maeLeft = aeTo;    // no need to cloneSelf coz aeTo will not be changed.
				listReplacedChildren.add(aeReturn.maeLeft);
				break;
			}
		}
		
		for (int idx = 0; idx < listFromToMap.size(); idx ++)	{
			AbstractExpr aeFrom = bExpr2Pattern?listFromToMap.get(idx).maeExprUnit:listFromToMap.get(idx).maePatternUnit;
			AbstractExpr aeTo = bExpr2Pattern?listFromToMap.get(idx).maePatternUnit:listFromToMap.get(idx).maeExprUnit;
			if (aeReturn.maeRight.isEqual(aeFrom))	{
				aeReturn.maeRight = aeTo;    // no need to cloneSelf coz aeTo will not be changed.
				listReplacedChildren.add(aeReturn.maeRight);
				break;
			}
		}
		return aeReturn;
	}

	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException,
			JSmartMathErrException {
		// Distribute can be degraded to the same level of aexpr, but not higher level.
		if ((maeLeft instanceof AEBitwiseOpt || maeRight instanceof AEBitwiseOpt)
				&& !(maeLeft instanceof AEBitwiseOpt && maeRight instanceof AEBitwiseOpt))	{
			AEBitwiseOpt aeBitwiseChild = (AEBitwiseOpt) ((maeLeft instanceof AEBitwiseOpt)?maeLeft:maeRight);
			AbstractExpr aeNonBitwiseChild = (maeLeft instanceof AEBitwiseOpt)?maeRight:maeLeft;
			if (moptType != OPERATORTYPES.OPERATOR_XOR
					&& aeBitwiseChild.moptType != OPERATORTYPES.OPERATOR_XOR)	{
				AEBitwiseOpt aeNewLeft = new AEBitwiseOpt(aeNonBitwiseChild,    // no need to clone self
						moptType, aeBitwiseChild.maeLeft);
				AEBitwiseOpt aeNewRight = new AEBitwiseOpt(aeNonBitwiseChild,    // no need to clone self
						moptType, aeBitwiseChild.maeRight);
				return new AEBitwiseOpt(aeNewLeft, aeBitwiseChild.moptType, aeNewRight);
			}
		}
		return this;
	}
    
    // avoid to do any overhead work.
	@Override
	public DataClass evaluateAExprQuick(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumLeft = maeLeft.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
		DataClass datumRight = maeRight.evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
        DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datumLeft, new CalculateOperator(moptType, 2), datumRight);
        return datum;        
    }

    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        AbstractExpr aeLeft = maeLeft.evaluateAExpr(lUnknownVars, lVarNameSpaces);
		AbstractExpr aeRight = maeRight.evaluateAExpr(lUnknownVars, lVarNameSpaces);
        if (aeLeft instanceof AEConst && aeRight instanceof AEConst) {
            DataClass datum = ExprEvaluator.evaluateTwoOperandCell(((AEConst)aeLeft).getDataClassRef(), new CalculateOperator(moptType, 2), ((AEConst)aeRight).getDataClassRef());
            return new AEConst(datum);
        } else {
            return new AEBitwiseOpt(aeLeft, moptType, aeRight);
        }
    }

	@Override
	public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();
		
		AbstractExpr aeLeft = maeLeft.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		AbstractExpr aeRight = maeRight.simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
		
		AbstractExpr aeReturn = new AEBitwiseOpt(aeLeft, moptType, aeRight);
		try	{
			// have to clone the parameters coz they may be changed inside.
			aeReturn = mergeBitwise(aeLeft, aeRight, moptType);
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
        if ((enumAET.getValue() > menumAEType.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                || (enumAET.getValue() == menumAEType.getValue() && nLeftOrRight <= 0))    {
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
			int nReturn = maeLeft.compareAExpr(((AEBitwiseOpt)aexpr).maeLeft);
			if (nReturn != 0)	{
				nReturn = maeRight.compareAExpr(((AEBitwiseOpt)aexpr).maeRight);
			}
			return nReturn;
		}
	}

	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException	{
		validateAbstractExpr();
		if (moptType == OPERATORTYPES.OPERATOR_AND)	{
			return maeLeft.isNegligible() && maeRight.isNegligible();
		} else if (moptType == OPERATORTYPES.OPERATOR_OR)	{
			return maeLeft.isNegligible() || maeRight.isNegligible();
		} else	{	//moptType == OPERATORTYPES.OPERATOR_XOR
			if (maeLeft.isNegligible() && maeRight.isNegligible())	{
				return true;
			} else if (maeLeft.isEqual(maeRight))	{
				return true;
			} else	{
				return false;
			}
		}
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
        boolean bLeftNeedBracketsWhenToStr = false, bRightNeedBracketsWhenToStr = false;
        bLeftNeedBracketsWhenToStr = maeLeft.needBracketsWhenToStr(menumAEType, 1);
        bRightNeedBracketsWhenToStr = maeRight.needBracketsWhenToStr(menumAEType, -1);
        
		String strOutput = "";
        if (bLeftNeedBracketsWhenToStr) {
            strOutput += "(" + maeLeft.output() + ")";
        } else  {
            strOutput += maeLeft.output();
        }
        strOutput += moptType.output();
        if (bRightNeedBracketsWhenToStr)    {
            strOutput += "(" + maeRight.output() + ")";
        } else  {
            strOutput += maeRight.output();
        }
		return strOutput;
	}
	
	// note that parameters of this function will be changed inside.
	public static AbstractExpr mergeBitwise(AbstractExpr aeLeft, AbstractExpr aeRight, OPERATORTYPES optType) throws JFCALCExpErrException, JSmartMathErrException	{
		if (aeLeft instanceof AEConst && aeRight instanceof AEConst)	{
			// both aeInput1 and aeInput2 are constants.
			DataClass datum1 = ((AEConst)aeLeft).getDataClassRef(),
			datum2 = ((AEConst)aeRight).getDataClassRef();

			CalculateOperator calcOpt = new CalculateOperator(optType, 2);
			DataClass datum = ExprEvaluator.evaluateTwoOperandCell(datum1, calcOpt, datum2); // evaluateTwoOperandCell will not change datum1 or datum2 so use getFinalDatumValue
			AEConst aexprReturn = new AEConst(datum);
			return aexprReturn;
		} else if (aeLeft.isEqual(aeRight))	{
			// left and right are the same thing.
			if (optType == OPERATORTYPES.OPERATOR_AND || optType == OPERATORTYPES.OPERATOR_OR)	{
				return aeLeft;
			} else if (optType == OPERATORTYPES.OPERATOR_XOR)	{
				return new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO));
			}
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
		} else if (aeLeft instanceof AEBitwiseOpt && aeRight instanceof AEBitwiseOpt)	{
			if (((AEBitwiseOpt)aeLeft).moptType == ((AEBitwiseOpt)aeRight).moptType
					&& ((AEBitwiseOpt)aeLeft).moptType != OPERATORTYPES.OPERATOR_XOR
					&& optType != OPERATORTYPES.OPERATOR_XOR)	{
				if (((AEBitwiseOpt)aeLeft).maeLeft.isEqual(((AEBitwiseOpt)aeRight).maeLeft))	{
					AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
															optType, ((AEBitwiseOpt)aeRight).maeRight);
					return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
											((AEBitwiseOpt)aeLeft).moptType,
											aeNewChild);
				} else if (((AEBitwiseOpt)aeLeft).maeLeft.isEqual(((AEBitwiseOpt)aeRight).maeRight))	{
					AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
															optType, ((AEBitwiseOpt)aeRight).maeLeft);
					return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
								((AEBitwiseOpt)aeLeft).moptType,
								aeNewChild);
				} else if (((AEBitwiseOpt)aeLeft).maeRight.isEqual(((AEBitwiseOpt)aeRight).maeLeft))	{
					AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
															optType, ((AEBitwiseOpt)aeRight).maeRight);
					return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
											((AEBitwiseOpt)aeLeft).moptType,
											aeNewChild);
				} else if (((AEBitwiseOpt)aeLeft).maeRight.isEqual(((AEBitwiseOpt)aeRight).maeRight))	{
					AEBitwiseOpt aeNewChild = new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeLeft,
															optType, ((AEBitwiseOpt)aeRight).maeLeft);
					return new AEBitwiseOpt(((AEBitwiseOpt)aeLeft).maeRight,
											((AEBitwiseOpt)aeLeft).moptType,
											aeNewChild);
				}
			}
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
		} else if (aeLeft instanceof AEBitwiseOpt || aeRight instanceof AEBitwiseOpt)	{
			// one of aeLeft and aeRight is not AEBitwiseOpt.
			AEBitwiseOpt aeBitwiseChild = (AEBitwiseOpt) ((aeLeft instanceof AEBitwiseOpt)?aeLeft:aeRight);
			AbstractExpr aeNonBitwiseChild = (aeLeft instanceof AEBitwiseOpt)?aeRight:aeLeft;
			if (optType == aeBitwiseChild.moptType)	{
				if (aeBitwiseChild.maeLeft.isEqual(aeNonBitwiseChild))	{
					if (optType == OPERATORTYPES.OPERATOR_XOR)	{
						return aeBitwiseChild.maeRight;
					} else	{
						return aeBitwiseChild;
					}
				} else if (aeBitwiseChild.maeRight.isEqual(aeNonBitwiseChild))	{
					if (optType == OPERATORTYPES.OPERATOR_XOR)	{
						return aeBitwiseChild.maeLeft;
					} else	{
						return aeBitwiseChild;
					}
				}				
			}
			throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        AbstractExpr aeLeft = (maeLeft instanceof AEConst)?maeLeft:maeLeft.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        AbstractExpr aeRight = (maeRight instanceof AEConst)?maeRight:maeRight.convertAEVar2AExprDatum(listVars, bNotConvertVar, listCvtedVars);
        return new AEBitwiseOpt(aeLeft, moptType, aeRight);
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        AbstractExpr aeLeft = maeLeft, aeRight = maeRight;
        if (maeLeft instanceof AEConst
                && ((AEConst)maeLeft).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeLeft = ((AEConst)maeLeft).getDataClassRef().getAExpr();
        }
        if (maeRight instanceof AEConst
                && ((AEConst)maeRight).getDataClassRef().getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
            aeRight = ((AEConst)maeRight).getDataClassRef().getAExpr();
        }
        return new AEBitwiseOpt(aeLeft, moptType, aeRight);
    }

    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = maeLeft.getVarAppearanceCnt(strVarName);
        nCnt += maeRight.getVarAppearanceCnt(strVarName);
        return nCnt;
    }
}
