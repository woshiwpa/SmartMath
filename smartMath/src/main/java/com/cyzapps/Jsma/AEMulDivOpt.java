package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jfcalc.MFPNumeric;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEMulDivOpt extends AbstractExpr {

	public LinkedList<CalculateOperator> mlistOpts = new LinkedList<CalculateOperator>();
	public LinkedList<AbstractExpr> mlistChildren = new LinkedList<AbstractExpr>();
	
	public AEMulDivOpt() {
		initAbstractExpr();
	}
	
	public AEMulDivOpt(LinkedList<AbstractExpr> listChildren, LinkedList<CalculateOperator> listOpts) throws JSmartMathErrException	{
		setAEMulDivOpt(listChildren, listOpts);
	}

	public AEMulDivOpt(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV;
		mlistChildren =  new LinkedList<AbstractExpr>();
		mlistOpts = new LinkedList<CalculateOperator>();
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException {
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}
		
		for (int idx = 0; idx < mlistOpts.size(); idx ++)	{
			if (mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_MULTIPLY
					&& mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_DIVIDE)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
			}
		}
		if (mlistChildren.size() < 2 || mlistOpts.size() != mlistChildren.size())	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
	}

	private void setAEMulDivOpt(LinkedList<AbstractExpr> listChildren, LinkedList<CalculateOperator> listOpts) throws JSmartMathErrException	{
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV;
		mlistChildren = listChildren;
		mlistOpts = listOpts;
		validateAbstractExpr();
	}

	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEMulDivOpt)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		mlistChildren = new LinkedList<AbstractExpr>();
        mlistChildren.addAll(((AEMulDivOpt)aexprOrigin).mlistChildren);
		mlistOpts = new LinkedList<CalculateOperator>();
        mlistOpts.addAll(((AEMulDivOpt)aexprOrigin).mlistOpts);
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEMulDivOpt)aexprOrigin).validateAbstractExpr();

		super.copyDeep(aexprOrigin);
		mlistChildren = new LinkedList<AbstractExpr>();
		for (int idx = 0; idx < ((AEMulDivOpt)aexprOrigin).mlistChildren.size(); idx ++)	{
			mlistChildren.add(((AEMulDivOpt)aexprOrigin).mlistChildren.get(idx).cloneSelf());
		}
		mlistOpts = new LinkedList<CalculateOperator>();
        mlistOpts.addAll(((AEMulDivOpt)aexprOrigin).mlistOpts);    // calculateOperators are immutable.
		/*for (int idx = 0; idx < ((AEMulDivOpt)aexprOrigin).mlistOpts.size(); idx ++)	{
			mlistOpts.add(new CalculateOperator(((AEMulDivOpt)aexprOrigin).mlistOpts.get(idx).getOperatorType(),
					((AEMulDivOpt)aexprOrigin).mlistOpts.get(idx).getOperandNum(), true));	// Note that binary opt's prefix flag is always true.
		}*/
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException,
			JSmartMathErrException {
		AbstractExpr aeReturn = new AEMulDivOpt();
		aeReturn.copyDeep(this);
		return aeReturn;
	}

	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();

		int[] narrayDimReturn = mlistChildren.get(0).recalcAExprDim(bUnknownAsSingle);
		for (int idx = 1; idx < mlistChildren.size(); idx ++)	{
			int[] narrayOprd = mlistChildren.get(idx).recalcAExprDim(bUnknownAsSingle);
			CalculateOperator calcOpt = mlistOpts.get(idx);
			switch (calcOpt.getOperatorType())	{
			case OPERATOR_MULTIPLY:
				if (narrayOprd.length == 0)	{	// right-operand is a number, not matrix
					// do nothing because narrayDimReturn will not change.
				} else if (narrayDimReturn.length == 0)	{	// left-operand is a number, not matrix
					narrayDimReturn = narrayOprd;
				} else	{
					if (narrayDimReturn[narrayDimReturn.length - 1] != narrayOprd[0])	{
						throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
					}
					int[] narrayLeftOprd = narrayDimReturn;
					narrayDimReturn = new int[narrayDimReturn.length + narrayOprd.length - 2];
					for (int idx1 = 0; idx1 < narrayDimReturn.length; idx1 ++)	{
						if (idx1 < narrayLeftOprd.length - 1)	{
							narrayDimReturn[idx1] = narrayLeftOprd[idx1];
						} else	{
							narrayDimReturn[idx1] = narrayOprd[idx1 - narrayLeftOprd.length + 1];
						}
					}
				}
				break;
			case OPERATOR_DIVIDE:
				if (narrayOprd.length == 0)	{	// right-operand is a number, not a matrix
					// do nothing because narrayDimReturn will not change
                } else if (narrayDimReturn.length == 0  // left-operand is constant 1
                        && mlistChildren.get(0) instanceof AEConst
                        && ((AEConst)mlistChildren.get(0)).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE))) {
                    if (narrayOprd.length != 2)	{
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                    }
                    if (narrayOprd[1] != narrayOprd[0])  {
                        throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
                    }
                    narrayDimReturn = new int[2];
                    narrayDimReturn[0] = narrayDimReturn[1] = narrayOprd[0];
				} else	{
					if (narrayOprd.length - 1 > narrayDimReturn.length)	{
						throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
					}
					for (int idx1 = 1; idx1 < narrayOprd.length; idx1 ++)	{
						if (narrayOprd[idx1] != narrayDimReturn[idx1 - narrayOprd.length + narrayDimReturn.length])	{
							throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
						}
					}
					int[] narrayLeftOprd = narrayDimReturn;
					narrayDimReturn = new int[narrayLeftOprd.length - narrayOprd.length + 2];
                    System.arraycopy(narrayLeftOprd, 0, narrayDimReturn, 0, narrayLeftOprd.length - narrayOprd.length + 1);
					narrayDimReturn[narrayLeftOprd.length - narrayOprd.length + 1] = narrayOprd[0];
				}
				break;
			default:
				throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
			}
		}
		return narrayDimReturn;
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (mlistChildren.size() != ((AEMulDivOpt)aexpr).mlistChildren.size())	{
			return false;
		} else if (mlistOpts.size() != ((AEMulDivOpt)aexpr).mlistOpts.size())	{
			return false;
		} else	{
			for (int idx = 0; idx < mlistOpts.size(); idx ++)	{
				if (mlistOpts.get(idx).getOperatorType() != ((AEMulDivOpt)aexpr).mlistOpts.get(idx).getOperatorType())	{
					return false;
				}
			}
			for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
				if (mlistChildren.get(idx).isEqual(((AEMulDivOpt)aexpr).mlistChildren.get(idx)) == false)	{
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
        if (!(aePattern instanceof AEMulDivOpt))   {
            return false;
        }
        if (mlistOpts.size() != ((AEMulDivOpt)aePattern).mlistOpts.size())  {
            return false;
        }
        if (mlistChildren.size() != ((AEMulDivOpt)aePattern).mlistChildren.size())  {
            return false;
        }
        LinkedList<AbstractExpr> listConvertedChildren = new LinkedList<AbstractExpr>();
        LinkedList<Boolean> listInverted = new LinkedList<Boolean>();
        for (int idx = 0; idx < mlistOpts.size(); idx ++)   {
            if (mlistOpts.get(idx).getOperatorType() == ((AEMulDivOpt)aePattern).mlistOpts.get(idx).getOperatorType())  {
                listConvertedChildren.add(mlistChildren.get(idx));
                listInverted.add(false);
            } else if (bAllowConversion) {    // one is * the other is /, but we allow conversion here.
                // one is multiply, the other is divide
                // this function is able to handle like x+3 matches pattern x-a. But it cannot handle degraded expression case (e.g. x+1 match a*x + b).
                AbstractExpr aeThisChild = mlistChildren.get(idx);
                AEConst aeMinus1 = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.MINUS_ONE));
                // assume this abstractexpr has been fully simplified, so that aeThisGrandChild cannot be muldivopt
                if (aeThisChild instanceof AEPowerOpt) {
                    AbstractExpr aePowerTo = ((AEPowerOpt)aeThisChild).maeRight;
                    if (aePowerTo instanceof AEMulDivOpt) {
                        boolean bMultiplyMinus1ToConst = false;
                        LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                        listChildren.addAll(((AEMulDivOpt)aePowerTo).mlistChildren);
                        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                        listOpts.addAll(((AEMulDivOpt)aePowerTo).mlistOpts);
                        for (int idx1 = 0; idx1 < listChildren.size(); idx1 ++) {
                            AbstractExpr aeThisGrandChild = listChildren.get(idx1);
                            if (aeThisGrandChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                                || aeThisGrandChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE) {
                                bMultiplyMinus1ToConst = true;
                                DataClass datum = ((AEConst) aeThisGrandChild).getDataClassRef();
                                try {
                                    datum = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true), datum);
                                            //BuiltinProcedures.evaluateNegSign(datum); // cannot use this because it does not handle aexpr datum
                                } catch (JFCALCExpErrException e) {
                                    // cannot calculate like -"ABC"
                                    return false;
                                }
                                listChildren.set(idx1, new AEConst(datum));
                                break;
                            }
                        }
                        if (!bMultiplyMinus1ToConst) {
                            // need not to reorder or simplify here because const is always first.
                            listOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                            listChildren.addFirst(aeMinus1);
                        }
                        aeThisChild = new AEPowerOpt(((AEPowerOpt)aeThisChild).maeLeft, new AEMulDivOpt(listChildren, listOpts));
                    } else if (aePowerTo.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                            || aePowerTo.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE) {
                        DataClass datum = ((AEConst) aePowerTo).getDataClassRef();
                        try {
                            datum = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true), datum);
                                    //BuiltinProcedures.evaluateNegSign(datum); // cannot use this because it does not handle aexpr datum
                        } catch (JFCALCExpErrException e) {
                            // cannot calculate like -"ABC"
                            return false;
                        }
                        aeThisChild = new AEPowerOpt(((AEPowerOpt)aeThisChild).maeLeft, new AEConst(datum));
                    } else {
                        //need not to simplify most because muldiv or const has been considered above, we cannot simply others.
                        LinkedList<AbstractExpr> listAEs = new LinkedList<AbstractExpr>();
                        listAEs.add(aeMinus1);
                        listAEs.add(aePowerTo);
                        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                        aeThisChild = new AEPowerOpt(((AEPowerOpt)aeThisChild).maeLeft, new AEMulDivOpt(listAEs, listOpts));
                    }
                } else if (aeThisChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE
                        || aeThisChild.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_DATAREFVALUE) {
                    DataClass datum = ((AEConst)aeThisChild).getDataClassRef();
                    try {
                        datum = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE), new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2), datum);
                                //BuiltinProcedures.evaluateReciprocal(datum); // cannot use this because it does not handle aexpr datum
                    } catch (JFCALCExpErrException e) {
                        // cannot calculate 1/datum
                        return false;
                    }
                    aeThisChild = new AEConst(datum);
                } else {
                    aeThisChild = new AEPowerOpt(aeThisChild, aeMinus1);    // do not consider reorder children (because too complicated) here although should reorder.
                }
                listConvertedChildren.add(aeThisChild);
                listInverted.add(true);
            } else {    // one is * the other is /, and we do not allow conversion here.
                return false;
            }
        }
        for (int idx = 0; idx < listConvertedChildren.size(); idx ++)   {
            if (listConvertedChildren.get(idx).isPatternMatch(((AEMulDivOpt)aePattern).mlistChildren.get(idx),
                    listpeuMapPseudoFuncs, listpeuMapPseudoConsts, listpeuMapUnknowns, bAllowConversion) == false)  {
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
        AEMulDivOpt aeReturn = new AEMulDivOpt();
        aeReturn.copy(this);
        aeReturn.mlistChildren = listChildren == null?new LinkedList<AbstractExpr>():listChildren;
        aeReturn.validateAbstractExpr();
        return aeReturn;
    }
	
	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
	public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException	{
		AEMulDivOpt aeReturn = new AEMulDivOpt();
        aeReturn.copy(this);
        for (int idx = 0; idx < aeReturn.mlistChildren.size(); idx ++)	{
			for (int idx1 = 0; idx1 < listFromToMap.size(); idx1 ++)	{
				if (bExpr2Pattern && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maeExprUnit))	{
					aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maePatternUnit);	// need to clone because will be many aeTo copies. To make it simple, not clone.
					listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
					break;
				} else if ((!bExpr2Pattern) && aeReturn.mlistChildren.get(idx).isEqual(listFromToMap.get(idx1).maePatternUnit))	{
					aeReturn.mlistChildren.set(idx, listFromToMap.get(idx1).maeExprUnit);	// need to clone because will be many aeTo copies. To make it simple, not clone.
					listReplacedChildren.add(aeReturn.mlistChildren.get(idx));
					break;
				}
			}
		}
		return aeReturn;
	}

	@Override
	public AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		validateAbstractExpr();
		int nNeedDistrIdx = -1;
		AEPosNegOpt aeAddSub = new AEPosNegOpt();
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			if (mlistChildren.get(idx).menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG
					&& ((AEPosNegOpt)mlistChildren.get(idx)).mlistChildren.size() > 1
					&& mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)	{
				// need distribution
				nNeedDistrIdx = idx;
				aeAddSub = (AEPosNegOpt)mlistChildren.get(idx);
				break;
			}
			if (mlistChildren.get(idx).menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POSNEG
					&& ((AEPosNegOpt)mlistChildren.get(idx)).mlistChildren.size() == 1)	{
				// positive sign or negative sign, need distribution
				nNeedDistrIdx = idx;
				aeAddSub = (AEPosNegOpt)mlistChildren.get(idx);
				break;
			}
		}
		
		if (nNeedDistrIdx == -1)	{
			return this;
		} else if (aeAddSub.mlistChildren.size() == 1)	{
			AEMulDivOpt aeReturn = new AEMulDivOpt();
			aeReturn.mlistChildren.addAll(mlistChildren);
			aeReturn.mlistOpts.addAll(mlistOpts);
			if (aeAddSub.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
					|| aeAddSub.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)	{
				// positive sign
				aeReturn.mlistChildren.set(nNeedDistrIdx, aeAddSub.mlistChildren.get(0));
			} else	{
				// negative sign
				aeReturn.mlistChildren.set(nNeedDistrIdx, new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE)));
				aeReturn.mlistOpts.add(nNeedDistrIdx + 1, aeReturn.mlistOpts.get(nNeedDistrIdx));   // calculateOperator is immutable.
				aeReturn.mlistChildren.add(nNeedDistrIdx + 1, aeAddSub.mlistChildren.get(0));
			}
			return aeReturn;
		} else	{
			LinkedList<CalculateOperator> listDistributedCalcOpts = new LinkedList<CalculateOperator>();
			for (int idx = 0; idx < aeAddSub.mlistOpts.size(); idx ++)	{
				CalculateOperator calcOpt = new CalculateOperator(aeAddSub.mlistOpts.get(idx).getOperatorType(), 2);    //+- can have operand= 1 or 2, so cannot simply set mlistOpts.get(idx1) into it.
				listDistributedCalcOpts.add(calcOpt);
			}
			LinkedList<AbstractExpr> listDistributedChildren = new LinkedList<AbstractExpr>();
			for (int idx = 0; idx < aeAddSub.mlistChildren.size(); idx ++)	{
				LinkedList<CalculateOperator> listCalcOptsSub = new LinkedList<CalculateOperator>();
				for (int idx1 = 0; idx1 < mlistOpts.size(); idx1 ++)	{
					CalculateOperator calcOpt = new CalculateOperator(mlistOpts.get(idx1).getOperatorType(), 2);    //+- can have operand= 1 or 2, so cannot simply set mlistOpts.get(idx1) into it.
					listCalcOptsSub.add(calcOpt);
				}
				LinkedList<AbstractExpr> listChildrenSub = new LinkedList<AbstractExpr>();
				for (int idx1 = 0; idx1 < mlistChildren.size(); idx1 ++)	{
					if (idx1 != nNeedDistrIdx)	{
						listChildrenSub.add(mlistChildren.get(idx1));
					} else	{
						listChildrenSub.add(aeAddSub.mlistChildren.get(idx));
					}
				}
				AbstractExpr aexprDistributedChild = new AEMulDivOpt(listChildrenSub, listCalcOptsSub);
				// distribute multiply-division for the child
				aexprDistributedChild = aexprDistributedChild.distributeAExpr(simplifyParams);
				listDistributedChildren.add(aexprDistributedChild);
			}
			AbstractExpr aeReturn = (AbstractExpr) new AEPosNegOpt(listDistributedChildren, listDistributedCalcOpts);
			return aeReturn;	// needs sort&merge after this function returns.
		}
	}

    // avoid to do any overhead work.
	@Override
	public DataClass evaluateAExprQuick(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        DataClass datumLast = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE);
        for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			DataClass datum = mlistChildren.get(idx).evaluateAExprQuick(lUnknownVars, lVarNameSpaces);
            datumLast = ExprEvaluator.evaluateTwoOperandCell(datumLast, mlistOpts.get(idx), datum);
		}

        return datumLast;
    }
    
    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
        LinkedList<AbstractExpr> listNewChildren = new LinkedList<AbstractExpr>();
        LinkedList<CalculateOperator> listNewOpts = new LinkedList<CalculateOperator>();
        DataClass datumOne = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE);
        for (int idx0 = 0; idx0 < mlistChildren.size(); idx0 ++)	{
            DataClass datumLast = datumOne;
            int idx = idx0;
            AbstractExpr aexpr2Add = null;
            for (; idx < mlistChildren.size(); idx ++)	{
                AbstractExpr aexpr = mlistChildren.get(idx).evaluateAExpr(lUnknownVars, lVarNameSpaces);
                if (aexpr instanceof AEConst) {
                    datumLast = ExprEvaluator.evaluateTwoOperandCell(datumLast, mlistOpts.get(idx), ((AEConst)aexpr).getDataClassRef());
                } else {
                    aexpr2Add = aexpr;
                    break;
                }
            }
            if (!datumLast.isEqual(datumOne)) {
                listNewOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listNewChildren.add(new AEConst(datumLast));
            }
            if (aexpr2Add != null) {
                listNewOpts.add( mlistOpts.get(idx));
                listNewChildren.add(aexpr2Add);
            }
            idx0 = idx;
        }
        if (listNewChildren.size() == 0) {
            // this means returns 1
            return new AEConst(datumOne);
        }
        if (listNewChildren.size() == 1) {
            if (listNewOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE) {
                // if there is only one child and it is divide, the child cannot be aeConst.
                listNewOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listNewChildren.add(new AEConst(datumOne));
                return new AEMulDivOpt(listNewChildren, listNewOpts);
            } else {
                return listNewChildren.getFirst();
            }
        } else {
            return new AEMulDivOpt(listNewChildren, listNewOpts);
        }
    }
    
    @Override
	public AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces, SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException,
			JFCALCExpErrException {
		validateAbstractExpr();

        AEMulDivOpt aeCopy = new AEMulDivOpt();
        aeCopy.copy(this);
        for (int idx = 0; idx < aeCopy.mlistChildren.size(); idx ++)	{
			AbstractExpr aexpr = aeCopy.mlistChildren.get(idx).simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
			aeCopy.mlistChildren.set(idx, aexpr);
		}
		
		// sort the children.
		int nTotalChildrenNumber = aeCopy.mlistChildren.size();
        int nLastSorted = nTotalChildrenNumber - 1;
		while (nLastSorted > 0)	{
			boolean bBubbledUp = false;
			for (int idx = 0; idx < nLastSorted; idx ++)	{
				AbstractExpr aeCurrent = aeCopy.mlistChildren.get(idx);
				AbstractExpr aeNext = aeCopy.mlistChildren.get(idx + 1);
				if (aeCurrent.compareAExpr(aeNext) < 0)	{	// means aeCurrent should be right of aeNext
                    int[] nDimCurrent = null, nDimNext = null;
                    try {
                        nDimCurrent = aeCurrent.recalcAExprDim(simplifyParams.mbIgnoreMatrixDim);
                    } catch (JSmartMathErrException e)	{
                        if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)	{
                            throw e;
                        }
                        // at this moment we cannot calculate size of the aeCurrent.
                    }
                    try {
                        nDimNext = aeNext.recalcAExprDim(simplifyParams.mbIgnoreMatrixDim);
                    } catch (JSmartMathErrException e)	{
                        if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)	{
                            throw e;
                        }
                        // at this moment we cannot calculate size of the aeNext.
                    }
                    if ((nDimCurrent != null && nDimCurrent.length == 0)
                            || (nDimNext != null && nDimNext.length == 0))	{
                        // if ignore matrix dim, or one of them is a pure value, not data reference. We can reorder them.
                        aeCopy.mlistChildren.set(idx, aeNext);
                        aeCopy.mlistChildren.set(idx + 1, aeCurrent);
                        CalculateOperator calcOpt = aeCopy.mlistOpts.get(idx);
                        aeCopy.mlistOpts.set(idx, aeCopy.mlistOpts.get(idx + 1));
                        aeCopy.mlistOpts.set(idx + 1, calcOpt);
                        bBubbledUp = true;
                        continue;
                    }
				}
				if (bBubbledUp)	{
					nLastSorted = idx;
				}
			}
            if (bBubbledUp == false) {
                break;  // cannot reorder any more.
            }
		}
		
		for (int idx = 0; idx < aeCopy.mlistChildren.size() - 1; idx ++)	{
			boolean bIsMultiply1 = true, bIsMultiply2 = true;
			if (aeCopy.mlistOpts.get(idx).getOperatorType() != OPERATORTYPES.OPERATOR_MULTIPLY)	{
				bIsMultiply1 = false;
			}
			if (aeCopy.mlistOpts.get(idx + 1).getOperatorType() != OPERATORTYPES.OPERATOR_MULTIPLY)	{
				bIsMultiply2 = false;
			}
			try	{
                // ensure that mergeMultiplyDiv does not change content of input.
                CalcOptrWrap cow = new CalcOptrWrap();
                cow.mCalcOptr =  aeCopy.mlistOpts.get(idx);
				AbstractExpr aexpr = mergeMultiplyDiv(aeCopy.mlistChildren.get(idx), aeCopy.mlistChildren.get(idx + 1),
                        bIsMultiply1, bIsMultiply2, cow, simplifyParams);
                aeCopy.mlistOpts.set(idx, cow.mCalcOptr);
				aeCopy.mlistChildren.remove(idx + 1);
				aeCopy.mlistOpts.remove(idx + 1);
				aeCopy.mlistChildren.set(idx, aexpr);
				idx --;
			} catch (JSmartMathErrException e)	{
				if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
					// cannot merge two children at this moment, so continue.
					continue;
				}
			}
		}
		
        for (int idx = 0; idx < aeCopy.mlistChildren.size(); idx ++)   {
            // try to convert single child to simplest format (manipulate single child)
            // this may change the operator as well.
            CalcOptrWrap cow = new CalcOptrWrap();
            cow.mCalcOptr =  aeCopy.mlistOpts.get(idx);
            aeCopy.mlistChildren.set(idx, convertSingleChild(aeCopy.mlistChildren.get(idx), cow));
            aeCopy.mlistOpts.set(idx, cow.mCalcOptr);
        }
        
		if (aeCopy.mlistChildren.size() == 1)  {
            if (aeCopy.mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)	{
                return aeCopy.mlistChildren.get(0).distributeAExpr(simplifyParams);	// distribute may not clone itself.
            } else {    // divide
                aeCopy.mlistChildren.addFirst(new AEConst(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE)));
                aeCopy.mlistOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                AbstractExpr aexprReturn = aeCopy.distributeAExpr(simplifyParams);
                return aexprReturn;
            }
		} else	{
			AbstractExpr aexprReturn = aeCopy.distributeAExpr(simplifyParams);
			return aexprReturn;
		}
	}

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        if ((enumAET.getValue() >= ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER.getValue()
                    && enumAET.getValue() <= ABSTRACTEXPRTYPES.ABSTRACTEXPR_INDEX.getValue())
                || ((enumAET.getValue() ==  ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV.getValue()
                    || enumAET.getValue() ==  ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_LEFTDIV.getValue())
                && nLeftOrRight <= 0))    {
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
			int nChildrenListSize2 = ((AEMulDivOpt)aexpr).mlistChildren.size();
            for (int idx = Math.min(nChildrenListSize1, nChildrenListSize2) - 1; idx >= 0; idx --)	{
                int nCompareChildReturn = mlistChildren.get(idx).compareAExpr(((AEMulDivOpt)aexpr).mlistChildren.get(idx));
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
		for (int idx = 0; idx < mlistChildren.size(); idx ++)	{
			if (mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)	{
				if (mlistChildren.get(idx).isNegligible())	{
					return true;
				}
			}
		}
		return false;
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
        String strOutput = "";
        if (mlistOpts.get(0).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY)  {
            boolean bNeedBracketsWhenToStr = false;
            if (mlistChildren.size() > 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(0).needBracketsWhenToStr(menumAEType, 1);
            }
            if (bNeedBracketsWhenToStr) {
                strOutput = "(" + mlistChildren.get(0).output() + ")";
            } else  {
                strOutput = mlistChildren.get(0).output();
            }
        } else  {   // divide
            boolean bNeedBracketsWhenToStr = false;
            if (mlistChildren.size() > 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(0).needBracketsWhenToStr(menumAEType, 0);
            } else  {
                bNeedBracketsWhenToStr = mlistChildren.get(0).needBracketsWhenToStr(menumAEType, -1);
            }
            if (bNeedBracketsWhenToStr) {
                strOutput = "1/(" + mlistChildren.get(0).output() + ")";
            } else  {
                strOutput = "1/" + mlistChildren.get(0).output();
            }
        }
        for (int idx = 1; idx < mlistOpts.size(); idx ++)	{
            boolean bNeedBracketsWhenToStr = false;
            if (idx < mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, 0);
            } else if (idx == mlistChildren.size() - 1)  {
                bNeedBracketsWhenToStr = mlistChildren.get(idx).needBracketsWhenToStr(menumAEType, -1);
            }
            strOutput += mlistOpts.get(idx).getOperatorType().output();
            if (bNeedBracketsWhenToStr) {
                strOutput += "(" + mlistChildren.get(idx).output() + ")";
            } else  {
                strOutput += mlistChildren.get(idx).output();
            }
        }
		return strOutput;
	}
	
    public static class CalcOptrWrap {
        CalculateOperator mCalcOptr = new CalculateOperator();
    }
	//=============== The following functions only support 2D matrix calculation, i.e. all eye matrix
	// would be calculated as 2D Eye matrix.
	// moreover, content of aeInput1 and aeInput2 should not be changed.
	
	// this function will be called when merging two mul-div abstract expressions.
	private static AbstractExpr mergeMDMultiplyDivMD(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged) throws JSmartMathErrException, JFCALCExpErrException	{
		if (aeInput1 instanceof AEMulDivOpt && aeInput2 instanceof AEMulDivOpt)	{
			LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
			LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
			if (bIsMultiply1 && bIsMultiply2)	{   // both are multiply
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
                listChildren.addAll(((AEMulDivOpt)aeInput2).mlistChildren);
                listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
				listOpts.addAll(((AEMulDivOpt)aeInput2).mlistOpts);
            } else if (!bIsMultiply1 && !bIsMultiply2)	{   // both are divide
                listChildren.addAll(((AEMulDivOpt)aeInput2).mlistChildren);
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
				listOpts.addAll(((AEMulDivOpt)aeInput2).mlistOpts);
                listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
			} else if (bIsMultiply1 && !bIsMultiply2)	{   // first is * and second is /
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
				listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
				for (int idx = ((AEMulDivOpt)aeInput2).mlistOpts.size() - 1; idx >= 0; idx --)	{
                    listChildren.add(((AEMulDivOpt)aeInput2).mlistChildren.get(idx));
					switch(((AEMulDivOpt)aeInput2).mlistOpts.get(idx).getOperatorType())	{
					case OPERATOR_MULTIPLY:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
						break;
					case OPERATOR_DIVIDE:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
						break;
					default:
						throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
					} 
				}
			} else  {   // first is / and second is *
				for (int idx = ((AEMulDivOpt)aeInput2).mlistOpts.size() - 1; idx >= 0; idx --)	{
                    listChildren.add(((AEMulDivOpt)aeInput2).mlistChildren.get(idx));
					switch(((AEMulDivOpt)aeInput2).mlistOpts.get(idx).getOperatorType())	{
					case OPERATOR_MULTIPLY:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
						break;
					case OPERATOR_DIVIDE:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
						break;
					default:
						throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
					} 
				}
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
				listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
            }
			return new AEMulDivOpt(listChildren, listOpts);
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}

	// this function will be called when merging multiply-divide and another type of abstract expressions.
	private static AbstractExpr mergeMDMultiplyDivOther(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged) throws JSmartMathErrException, JFCALCExpErrException	{
		if (aeInput1 instanceof AEMulDivOpt && !(aeInput2 instanceof AEMulDivOpt))	{
			LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
			LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
			if (bIsMultiply1 && bIsMultiply2)	{
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
                listChildren.add(aeInput2);
                listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
				listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
			} else if (!bIsMultiply1 && !bIsMultiply2)	{
                listChildren.add(aeInput2);
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
				listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
			} else if (bIsMultiply1 && !bIsMultiply2)	{
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
                listChildren.add(aeInput2);
                listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
				listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
            } else  {   // !bIsMultiply1 && bIsMultiply2
                listChildren.add(aeInput2);
                listChildren.addAll(((AEMulDivOpt)aeInput1).mlistChildren); // cloneSelf only needed in distribution.
				listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                listOpts.addAll(((AEMulDivOpt)aeInput1).mlistOpts);
            }
			return new AEMulDivOpt(listChildren, listOpts);
		} else if (!(aeInput1 instanceof AEMulDivOpt) && (aeInput2 instanceof AEMulDivOpt))	{
			LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
			LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
			if (bIsMultiply1 && bIsMultiply2)	{
                listChildren.add(aeInput1);
                listChildren.addAll(((AEMulDivOpt)aeInput2).mlistChildren); // cloneSelf only needed in distribution.
				listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
				listOpts.addAll(((AEMulDivOpt)aeInput2).mlistOpts);
            } else if (!bIsMultiply1 && !bIsMultiply2)	{
                listChildren.addAll(((AEMulDivOpt)aeInput2).mlistChildren); // cloneSelf only needed in distribution.
                listChildren.add(aeInput1);
				listOpts.addAll(((AEMulDivOpt)aeInput2).mlistOpts);
				listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
			} else if (bIsMultiply1 && !bIsMultiply2)	{
                listChildren.add(aeInput1);
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
				for (int idx = ((AEMulDivOpt)aeInput2).mlistOpts.size() - 1; idx >= 0; idx --)	{
                    listChildren.add(((AEMulDivOpt)aeInput2).mlistChildren.get(idx));
					switch(((AEMulDivOpt)aeInput2).mlistOpts.get(idx).getOperatorType())	{
					case OPERATOR_MULTIPLY:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
						break;
					case OPERATOR_DIVIDE:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
						break;
					default:
						throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
					} 
				}
            } else {    // !bIsMultiply1 && bIsMultiply2
				for (int idx = ((AEMulDivOpt)aeInput2).mlistOpts.size() - 1; idx >= 0; idx --)	{
                    listChildren.add(((AEMulDivOpt)aeInput2).mlistChildren.get(idx));
					switch(((AEMulDivOpt)aeInput2).mlistOpts.get(idx).getOperatorType())	{
					case OPERATOR_MULTIPLY:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
						break;
					case OPERATOR_DIVIDE:
						listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
						break;
					default:
						throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_OPERATOR);
					} 
				}
                listChildren.add(aeInput1);
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
			}
			return new AEMulDivOpt(listChildren, listOpts);
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}

    	// this function will merge a value eye with another abstract expression.
	private static AbstractExpr mergeZeroMultiplyDivOther(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged) throws JFCALCExpErrException, JSmartMathErrException	{
        // mergeZeroMultiplyDivOther does not handle nan or inf, we handle the cases in mergeConstMultiplyDivConst. make sure
        // mergeConstMultiplyDivConst is called before mergeZeroMultiplyDivOther.
        // only handle ...*0 or 0*..., zero matrix is not included.
        if (aeInput1 instanceof AEConst
                && ((AEConst)aeInput1).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                && ((AEConst)aeInput1).getDataClassRef().isZeros(false)
                && bIsMultiply1 == true)    {
            return aeInput1;
        } else if (aeInput2 instanceof AEConst
                && ((AEConst)aeInput2).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                && ((AEConst)aeInput2).getDataClassRef().isZeros(false))  {
            if (bIsMultiply2 == true && bIsMultiply1 == false)    {
                cowMerged.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                return aeInput2;
            } else if (bIsMultiply1 == bIsMultiply2)    {
                return aeInput2;
            }   // bIsMultiply1 == true while bIsMultiply2 == false, do nothing.
        }
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
    
	// this function will merge a value eye with another abstract expression.
	private static AbstractExpr mergeEyeMultiplyDivOther(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged, boolean bIgnoreMatrixDim) throws JFCALCExpErrException, JSmartMathErrException	{
		boolean bIsMultiply = (bIsMultiply1 == bIsMultiply2);
        if (aeInput1 instanceof AEConst	&& ((AEConst)aeInput1).getDataClassRef().isEye(false))	{
			if (aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE)	{
				// aeInput1 is a 1 value or we ignore matrix dim.
                if (bIsMultiply2) {
                    cowMerged.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                } else {
                    cowMerged.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
                }
                return aeInput2;
			} else if (!(aeInput2 instanceof AEConst && ((AEConst)aeInput2).getDataClassRef().isEye(false)
				&& aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE))	{
				// aeInput1 is an Eye array while aeInput2 is not a 1 value
				try	{
					int[] narraySize1 = aeInput1.recalcAExprDim(bIgnoreMatrixDim);
					int[] narraySize2 = aeInput2.recalcAExprDim(bIgnoreMatrixDim);
					if (narraySize2.length != 0)	{	// == 0 means a value and can be calculated.
						if (bIsMultiply)	{
							if (narraySize1[narraySize1.length - 1] != narraySize2[0])	{
								throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
							}
						} else	{	// division.
							if (narraySize1.length < narraySize2.length)	{
								throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
							} else	{
								for (int idx = narraySize2.length - 1; idx > 0; idx --)	{
									if (narraySize1[narraySize1.length - narraySize2.length + idx]
									                != narraySize2[idx])	{
										throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
									}
								}
							}
						}
					}
					// now the dimensions match
					if (bIsMultiply)	{
						if (aeInput2 instanceof AEConst)	{
							DataClass datum = ExprEvaluator.evaluateTwoOperandCell(
															((AEConst)aeInput1).getDataClassRef(),
															new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2),
															((AEConst)aeInput2).getDataClassRef());
							return new AEConst(datum);
						} else if (narraySize2.length != 0)	{
							return aeInput2;
						} else	{
							// an unknown value
							throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
						}
					} else	{
						if (aeInput2 instanceof AEConst)	{
							DataClass datum = ExprEvaluator.evaluateTwoOperandCell(
															((AEConst)aeInput1).getDataClassRef(),
															new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2),
															((AEConst)aeInput2).getDataClassRef());
							return new AEConst(datum);
						} else	{
							// an unknown value
							throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
						}
					}
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)	{
						throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
					} else	{
						throw e;
					}
				}
			} else	{
				// aeInput2 is a 1 value
				return aeInput1;
			}
		} else if (aeInput2 instanceof AEConst && ((AEConst)aeInput2).getDataClassRef().isEye(false))	{
			if (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_VALUE)	{
				// aeInput2 is a 1 value or we ignore matrix dim
				return aeInput1;
			} else	{
				// aeInput2 is an Eye array while aeInput1 is definitely not a 1 value or Eye array
				try	{
					int[] narraySize1 = aeInput1.recalcAExprDim(bIgnoreMatrixDim);
					int[] narraySize2 = aeInput2.recalcAExprDim(bIgnoreMatrixDim);
					if (bIsMultiply)	{
						if (narraySize1[narraySize1.length - 1] != narraySize2[0])	{
							throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
						}
					} else	{	// division.
						if (narraySize1.length < narraySize2.length)	{
							throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
						} else	{
							for (int idx = narraySize2.length - 1; idx > 0; idx --)	{
								if (narraySize1[narraySize1.length - narraySize2.length + idx]
								                != narraySize2[idx])	{
									throw new JFCALCExpErrException(ErrProcessor.ERRORTYPES.ERROR_ARRAY_DIM_DOES_NOT_MATCH);
								}
							}
						}
					}
					// now the dimensions match
					return aeInput1;
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION)	{
						throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
					} else	{
						throw e;
					}
				}
			}
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}
		
	// this function will be called when merging two add-sub children and one of them is a mul-div aexpr.
	private static AbstractExpr mergePowerMultiplyDivOther(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged, SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		boolean bIsMultiply = (bIsMultiply1 == bIsMultiply2);
        boolean bAE2NotPower = true;
		AbstractExpr aexprNotPower = null;
		AbstractExpr aexprPower = null;
		if (aeInput1.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER
				&& aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER)	{
			// AEinput2 is power opt but aeInput1 is not.
			bAE2NotPower = false;
			aexprNotPower = aeInput1;
			aexprPower = aeInput2;
		} else if (aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER
				&& aeInput2.menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER)	{
			// AEinput1 is mulitply or divide but aeInput2 is not.
			aexprNotPower = aeInput2;
			aexprPower = aeInput1;
		} else	{
			// this function does not handle other cases
			throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
		}
		
		if (((AEPowerOpt)aexprPower).maeLeft.isEqual(aexprNotPower)
                && ((AEPowerOpt)aexprPower).maeRight instanceof AEConst)	{
			LinkedList<AbstractExpr> listPowerToChildren = new LinkedList<AbstractExpr>();
			LinkedList<CalculateOperator> listPowerToOpts = new LinkedList<CalculateOperator>();
			if (bIsMultiply)	{
				listPowerToChildren.add(((AEPowerOpt)aexprPower).maeRight);
				listPowerToChildren.add(new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE)));
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
			} else if (bAE2NotPower)	{
				listPowerToChildren.add(((AEPowerOpt)aexprPower).maeRight);
				listPowerToChildren.add(new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE)));
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
			} else	{
				listPowerToChildren.add(new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE)));
				listPowerToChildren.add(((AEPowerOpt)aexprPower).maeRight);
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
			}
            // we can use new LinkedList<UnknownVariable>(), new LinkedList<LinkedList<Variable>>() as parameters
            // of simplifyAExprMost because we know elements of listPowerToChildren are constants
			AbstractExpr aePowerTo = new AEPosNegOpt(listPowerToChildren, listPowerToOpts)
                    .simplifyAExprMost(new LinkedList<UnknownVariable>(), new LinkedList<LinkedList<Variable>>(), simplifyParams);
			return new AEPowerOpt(aexprNotPower, aePowerTo);
		} else	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
		}		
	}
	
	// this function will be called when merging two add-sub children and one of them is a mul-div aexpr.
	private static AbstractExpr mergePowerMultiplyDivPower(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged, SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		boolean bIsMultiply = (bIsMultiply1 == bIsMultiply2);
        if (((AEPowerOpt)aeInput1).maeLeft.isEqual(((AEPowerOpt)aeInput2).maeLeft)
                && ((AEPowerOpt)aeInput1).maeRight instanceof AEConst
                && ((AEPowerOpt)aeInput2).maeRight instanceof AEConst)	{	// base equals to base.
			LinkedList<AbstractExpr> listPowerToChildren = new LinkedList<AbstractExpr>();
			LinkedList<CalculateOperator> listPowerToOpts = new LinkedList<CalculateOperator>();
			listPowerToChildren.add(((AEPowerOpt)aeInput1).maeRight);
			listPowerToChildren.add(((AEPowerOpt)aeInput2).maeRight);
			listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true));
			if (bIsMultiply)	{
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2));
			} else	{
				listPowerToOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2));
			}
			AbstractExpr aePowerTo = new AEPosNegOpt(listPowerToChildren, listPowerToOpts)
                    .simplifyAExprMost(new LinkedList<UnknownVariable>(), new LinkedList<LinkedList<Variable>>(), simplifyParams);
			return new AEPowerOpt(((AEPowerOpt)aeInput1).maeLeft, aePowerTo);
        } else if (((AEPowerOpt)aeInput1).maeRight.isEqual(((AEPowerOpt)aeInput2).maeRight)
                && ((AEPowerOpt)aeInput1).maeLeft instanceof AEConst
                && ((AEPowerOpt)aeInput2).maeLeft instanceof AEConst)	{	// powerto equals to powerto.
			LinkedList<AbstractExpr> listBaseChildren = new LinkedList<AbstractExpr>();
			LinkedList<CalculateOperator> listBaseOpts = new LinkedList<CalculateOperator>();
			listBaseChildren.add(((AEPowerOpt)aeInput1).maeLeft);
			listBaseChildren.add(((AEPowerOpt)aeInput2).maeLeft);
			listBaseOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
			if (bIsMultiply)	{
				listBaseOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
			} else	{
				listBaseOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
			}
			AbstractExpr aeBase = new AEMulDivOpt(listBaseChildren, listBaseOpts)
                    .simplifyAExprMost(new LinkedList<UnknownVariable>(), new LinkedList<LinkedList<Variable>>(), simplifyParams);
			return new AEPowerOpt(aeBase, ((AEPowerOpt)aeInput1).maeRight);
		} else	{
            // cannot merge like x**3*y**3 to (x*y)**3 because x and y might be matrix.
			throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
		}		
	}
	
	// this function will be called when merging two Add-sub children and both of them are constant.
	private static AbstractExpr mergeConstMultiplyDivConst(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged) throws JFCALCExpErrException, JSmartMathErrException	{
		if (aeInput1 instanceof AEConst && aeInput2 instanceof AEConst)	{
			// both aeInput1 and aeInput2 are constants
			DataClass datum1 = ((AEConst)aeInput1).getDataClassRef(),
					datum2 = ((AEConst)aeInput2).getDataClassRef();

			CalculateOperator calcOpt;
            DataClass datum = new DataClass();
            if (bIsMultiply1 && bIsMultiply2)   {
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                datum = ExprEvaluator.evaluateTwoOperandCell(datum1, calcOpt, datum2);
            } else if (!bIsMultiply1 && !bIsMultiply2)   {
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                datum = ExprEvaluator.evaluateTwoOperandCell(datum2, calcOpt, datum1);
            } else if (bIsMultiply1 && !bIsMultiply2)   {
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
                datum = ExprEvaluator.evaluateTwoOperandCell(datum1, calcOpt, datum2);
            } else  {   //!bIsMultiply1 && bIsMultiply2
                // cannot simply use left division considering 1/[[1,2],[3,4]]*[4,5] is valid while [[1,2],[3,4]]\[4,5] is invalid
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
                datum = ExprEvaluator.evaluateTwoOperandCell(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ONE), calcOpt, datum1);
                calcOpt = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
                datum = ExprEvaluator.evaluateTwoOperandCell(datum, calcOpt, datum2);   
                cowMerged.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            }
			AEConst aexprReturn = new AEConst(datum);
			return aexprReturn;
		}
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}
	
	// merge if aeInput1 and aeInput2 are the same.
    private static AbstractExpr mergeMultiplyDivSame(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged) throws JFCALCExpErrException, JSmartMathErrException	{
		if (aeInput1.isEqual(aeInput2))     {
            if (bIsMultiply1 == bIsMultiply2)	{
                DataClass datumPower = new DataClass();
                datumPower.setDataValue(MFPNumeric.TWO, DATATYPES.DATUM_INTEGER);
                AEPowerOpt aexprReturn = new AEPowerOpt(aeInput1, new AEConst(datumPower));
                return aexprReturn;				
            } else	{	// divide
                DataClass datumOne = new DataClass();
                datumOne.setDataValue(MFPNumeric.ONE);
                AEConst aexprReturn = new AEConst(datumOne);
                return aexprReturn;
            }
        } else  {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
        }
	}
    
    private static AbstractExpr mergeMinus1MultiplyDivPosNeg(AbstractExpr aeInput1, AbstractExpr aeInput2,
            boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged) throws JFCALCExpErrException, JSmartMathErrException {
        if (aeInput1 instanceof AEPosNegOpt && aeInput2 instanceof AEConst
                && ((AEConst)aeInput2).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE)))   {
            AEPosNegOpt aeReturn = new AEPosNegOpt();
            aeReturn.copy(aeInput1);
            for (int idx = 0; idx < aeReturn.mlistOpts.size(); idx ++)    {
                CalculateOperator co = aeReturn.mlistOpts.get(idx);
                if (co.getOperatorType() == OPERATORTYPES.OPERATOR_ADD) {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                    aeReturn.mlistOpts.set(idx, coNew);
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT) {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                    aeReturn.mlistOpts.set(idx, coNew);
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)  {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true);
                    aeReturn.mlistOpts.set(idx, coNew);
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)  {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true);
                    aeReturn.mlistOpts.set(idx, coNew);
                }
            }
            return aeReturn;
        } else if (aeInput2 instanceof AEPosNegOpt && aeInput1 instanceof AEConst
                && ((AEConst)aeInput1).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE)))  {
            AEPosNegOpt aeCopy = new AEPosNegOpt();
            aeCopy.copy(aeInput2);
            for (int idx = 0; idx < aeCopy.mlistOpts.size(); idx ++)    {
                CalculateOperator co = aeCopy.mlistOpts.get(idx);
                if (co.getOperatorType() == OPERATORTYPES.OPERATOR_ADD) {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_SUBTRACT, 2);
                    aeCopy.mlistOpts.set(idx, coNew);
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT) {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_ADD, 2);
                    aeCopy.mlistOpts.set(idx, coNew);
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)  {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true);
                    aeCopy.mlistOpts.set(idx, coNew);
                } else if (co.getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)  {
                    CalculateOperator coNew = new CalculateOperator(OPERATORTYPES.OPERATOR_POSSIGN, 1, true);
                    aeCopy.mlistOpts.set(idx, coNew);
                }
            }
            if (bIsMultiply1 == bIsMultiply2)   {
                return aeCopy;
            } else  {
                AEConst aeOne = new AEConst(new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ONE));
                LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
                listChildren.add(aeOne);
                listChildren.add(aeCopy);
                LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                AbstractExpr aeReturn = new AEMulDivOpt(listChildren, listOpts);
                return aeReturn;
            }
        }
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
    }
	
	/*
	 * This function try to merge two abstract expressions connected by * or /.
	 * Assume that the two abstract expression parameters have to sorted and simplified (simplified means all the * 1 and * I have been
	 * eliminated and all the constants are multiplied ). Also assume that the two parameters have already distributed multiply & divisions
	 */
	public static AbstractExpr mergeMultiplyDiv(AbstractExpr aeInput1, AbstractExpr aeInput2,
                                                boolean bIsMultiply1, boolean bIsMultiply2, CalcOptrWrap cowMerged,
                                                SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException	{
		// cowMerged, by default, will be bIsMultiply1. But it may change inside the function
		if (aeInput1 instanceof AEConst && aeInput2 instanceof AEConst)	{
			// both aeInput1 and aeInput2 are constants. need to do this first because if aeInput1 == aeInput2 == 0, we get wrong result if isEqual first.
            // also, mergeZeroMultiplyDivOther does not handle nan or inf, so we handle the cases here.
			return mergeConstMultiplyDivConst(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged);
        } else if (aeInput1.isEqual(aeInput2))	{
			// aeInput1 equals aeInput2
			return mergeMultiplyDivSame(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged);
        } else if ((aeInput1 instanceof AEPosNegOpt && aeInput2 instanceof AEConst
            && ((AEConst)aeInput2).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE)))
                || (aeInput2 instanceof AEPosNegOpt && aeInput1 instanceof AEConst
            && ((AEConst)aeInput1).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE)))) {
            return mergeMinus1MultiplyDivPosNeg(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged);
		} else if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER)
				&& (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER))	{
			// both of aeInput1 and aeInput2 are left division.
			return mergePowerMultiplyDivPower(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged, simplifyParams);
		} else if((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)
				&& (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV))	{
			// both of aeInput1 and aeInput2 are multiply division.
			return mergeMDMultiplyDivMD(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged);
		} else {
			// the following tests are for cases where one is a determined type but the other is
			// not a determined type. So we have to try every possiblity.
			if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER)
					|| (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_POWER))	{
				// one of aeInput1 and aeInput2 is power.
				try	{
					return mergePowerMultiplyDivOther(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged, simplifyParams);
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
						;	// do nothing but handle the exception.
					}
				}
			}
            if ((aeInput1 instanceof AEConst && ((AEConst)aeInput1).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                && ((AEConst)aeInput1).getDataClassRef().isZeros(false))
				|| (aeInput2 instanceof AEConst	&& ((AEConst)aeInput2).getDataClassRef().getDataType() != DATATYPES.DATUM_REF_DATA
                && ((AEConst)aeInput2).getDataClassRef().isZeros(false))){
				// either aeInput1 or aeInput2 or both are number (not matrix) 0.
				try	{
					return mergeZeroMultiplyDivOther(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged);
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
						;	// do nothing but handle the exception.
					}
				}
            }
			if ((aeInput1 instanceof AEConst && ((AEConst)aeInput1).getDataClassRef().isEye(false))
				|| (aeInput2 instanceof AEConst	&& ((AEConst)aeInput2).getDataClassRef().isEye(false))){
				// either aeInput1 or aeInput2 or both are eye.
				try	{
					return mergeEyeMultiplyDivOther(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged, simplifyParams.mbIgnoreMatrixDim);
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
						;	// do nothing but handle the exception.
					}
				}
			}
            // the case that one of aeInput1 and aeInput2 is multiply division should be the last try because
            // it simply create another ae mul div opt. It does not do any real merge. However, it is still
            // useful in the case like (x*y)*z. It can simplify (x*y)*z to x*y*z so that later on we can try
            // to merge y and z.
			if ((aeInput1.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV)
					|| (aeInput2.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_BOPT_MULTIPLYDIV))	{
				// one of aeInput1 and aeInput2 is multiply division.
				try	{
					return mergeMDMultiplyDivOther(aeInput1, aeInput2, bIsMultiply1, bIsMultiply2, cowMerged);
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType == ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS)	{
						;	// do nothing but handle the exception.
					}
				}
			}
		} 
		// cannot merge.
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_MERGE_TWO_ABSTRACTEXPRS);
	}
    
    // this function adjust aeChild based on its calculate operator co. In the adjusting, co may be changed.
    private static AbstractExpr convertSingleChild(AbstractExpr aeChild, CalcOptrWrap cow) throws JSmartMathErrException, JFCALCExpErrException {
        if (cow.mCalcOptr.getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE && aeChild instanceof AEMulDivOpt) {
            // if is / (a*b) or / (a/b), convert to /b/a or *b /a
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            for (int idx = 0; idx < ((AEMulDivOpt)aeChild).mlistChildren.size(); idx ++)    {
                if (((AEMulDivOpt)aeChild).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY) {
                    listOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2));
                } else  {
                    listOpts.addFirst(new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2));
                }
                listChildren.addFirst(((AEMulDivOpt)aeChild).mlistChildren.get(idx));
            }
            cow.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            AbstractExpr aeReturn = new AEMulDivOpt(listChildren, listOpts);
            return aeReturn;
        } else if (cow.mCalcOptr.getOperatorType() == OPERATORTYPES.OPERATOR_DIVIDE && aeChild instanceof AEPowerOpt) {
            // if is /a**b, convert to * a**-b
            AbstractExpr aeBase = ((AEPowerOpt)aeChild).maeLeft;
            LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
            listOpts.add(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true));
            LinkedList<AbstractExpr> listChildren = new LinkedList<AbstractExpr>();
            listChildren.add(((AEPowerOpt)aeChild).maeRight);
            AbstractExpr aePowerTo = new AEPosNegOpt(listChildren, listOpts);
            AbstractExpr aeReturn = new AEPowerOpt(aeBase, aePowerTo);
            cow.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_MULTIPLY, 2);
            return aeReturn;
        } else if (cow.mCalcOptr.getOperatorType() == OPERATORTYPES.OPERATOR_MULTIPLY
                && aeChild instanceof AEPowerOpt
                && ((AEPowerOpt)aeChild).maeRight instanceof AEConst
                && ((AEConst)((AEPowerOpt)aeChild).maeRight).getDataClassRef().isEqual(new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.MINUS_ONE))) {
            // if is * a**-1, convert to / a
            cow.mCalcOptr = new CalculateOperator(OPERATORTYPES.OPERATOR_DIVIDE, 2);
            return ((AEPowerOpt)aeChild).maeLeft;
        }
        return aeChild;
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
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        listOpts.addAll(mlistOpts);
        return new AEMulDivOpt(listChildrenCvted, listOpts);
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
        LinkedList<CalculateOperator> listOpts = new LinkedList<CalculateOperator>();
        listOpts.addAll(mlistOpts);
        return new AEMulDivOpt(listChildrenCvted, listOpts);
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
