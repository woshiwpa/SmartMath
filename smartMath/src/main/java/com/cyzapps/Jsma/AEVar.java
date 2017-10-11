package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.AbstractExpr.ABSTRACTEXPRTYPES;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public class AEVar extends AbstractExpr {

	// this class is defined for variable and pseudo const.
	public LinkedList<AbstractExpr> mlistConditions = new LinkedList<AbstractExpr>();
	public String mstrVariableName = "";	// here variable is unknown variable which must be solved or pseudo const name

	public AEVar() {
		initAbstractExpr();
	}
	
	public AEVar(String strName, ABSTRACTEXPRTYPES typeExpr) throws JSmartMathErrException	{
		setAEVar(strName, typeExpr);
	}
	public AEVar(String strName, LinkedList<AbstractExpr> listConditions) throws JSmartMathErrException	{
		setAEVar(strName, listConditions);
	}

	public AEVar(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		copy(aexprOrigin);
	}

	@Override
	protected void initAbstractExpr() {
		mlistConditions = new LinkedList<AbstractExpr>();
		mstrVariableName = "";
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE;
	}

	@Override
	public void validateAbstractExpr() throws JSmartMathErrException	{
		if (mstrVariableName == null || mstrVariableName.trim().length() == 0)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);
		}
		if (menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE && menumAEType != ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST)	{
			throw new JSmartMathErrException(ERRORTYPES.ERROR_INCORRECT_ABSTRACTEXPR_TYPE);
		}

	}
	
	private void setAEVar(String strName, LinkedList<AbstractExpr> listConditions) throws JSmartMathErrException	{
		menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_VARIABLE;
		mstrVariableName = (strName == null)?"":strName;
		mlistConditions = (listConditions == null)?new LinkedList<AbstractExpr>():mlistConditions;
		validateAbstractExpr();
	}
	
	private void setAEVar(String strName, ABSTRACTEXPRTYPES typeExpr) throws JSmartMathErrException	{
		mstrVariableName = (strName == null)?"":strName;
		mlistConditions = new LinkedList<AbstractExpr>();
		menumAEType = typeExpr;
		validateAbstractExpr();
	}
	
	@Override
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException,
			JSmartMathErrException {
		((AEVar)aexprOrigin).validateAbstractExpr();
		super.copy(aexprOrigin);
		mstrVariableName = (((AEVar)aexprOrigin).mstrVariableName == null)?"":((AEVar)aexprOrigin).mstrVariableName;
		
		if (((AEVar)aexprOrigin).mlistConditions == null)	{
			mlistConditions = new LinkedList<AbstractExpr>();
		} else	{
            mlistConditions.addAll(((AEVar)aexprOrigin).mlistConditions);
		}
	}

	@Override
	protected void copyDeep(AbstractExpr aexprOrigin)
			throws JFCALCExpErrException, JSmartMathErrException {
		((AEVar)aexprOrigin).validateAbstractExpr();
		super.copyDeep(aexprOrigin);
		mstrVariableName = (((AEVar)aexprOrigin).mstrVariableName == null)?"":((AEVar)aexprOrigin).mstrVariableName;
		
		if (((AEVar)aexprOrigin).mlistConditions == null)	{
			mlistConditions = new LinkedList<AbstractExpr>();
		} else	{
			for (int idx = 0; idx < ((AEVar)aexprOrigin).mlistConditions.size(); idx ++)	{
				AbstractExpr aexprCond = ((AEVar)aexprOrigin).mlistConditions.get(idx).cloneSelf();
				mlistConditions.add(aexprCond);
			}
		}
	}

	@Override
	public AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException	{
		AbstractExpr aeReturn = new AEVar();
		aeReturn.copyDeep(this);
		return aeReturn;
	}
	
	@Override
	public int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException,
			JFCALCExpErrException {
        if (bUnknownAsSingle) {
            return new int[0];
        }
		throw new JSmartMathErrException(ERRORTYPES.ERROR_CANNOT_CALCULATE_DIMENSION);
	}

	@Override
	public boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType != aexpr.menumAEType)	{
			return false;
		} else if (mstrVariableName.trim().compareToIgnoreCase(((AEVar)aexpr).mstrVariableName) != 0)	{
			return false;
		} else if (mlistConditions.size() != ((AEVar)aexpr).mlistConditions.size())	{
			return false;
		} else	{
			for (int idx = 0; idx < ((AEVar)aexpr).mlistConditions.size(); idx ++)	{
				if (mlistConditions.get(idx).isEqual(((AEVar)aexpr).mlistConditions.get(idx)) == false)	{
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
        } else if (isEqual(aePattern)) {
            // if this is the same as aePattern
            return true;            
        }
        return false;
    }
    
	@Override
	public boolean isKnownValOrPseudo() {
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST)	{
			return true;
		} else	{
			return false;			
		}
	}
	
	@Override
	public boolean isVariable() {
		return true;
	}
	
	// note that the return list should not be changed.
	@Override
	public LinkedList<AbstractExpr> getListOfChildren()	{
		return new LinkedList<AbstractExpr>();
	}

    @Override
    public AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)  throws JFCALCExpErrException, JSmartMathErrException {
        if (listChildren != null && listChildren.size() != 0) {
            throw new JSmartMathErrException(ERRORTYPES.ERROR_INVALID_ABSTRACTEXPR);			
        }
        return this;    // AEVar does not have any child.
    }

	// this function replaces children who equal aeFrom to aeTo and
	// returns the number of children that are replaced.
	@Override
    public AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException    {
		return this;
	}

	// variable cannot be distributed.
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
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST)	{
			// pseudo-const is always saved in unknown variable space because it may be simplifed before the value is solved.
			UnknownVariable varUnknown = UnknownVarOperator.lookUpList(mstrVariableName, lUnknownVars);
			if (varUnknown == null)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
			}
			return varUnknown.getSolvedValue();
		} else	{
			Variable varKnown = VariableOperator.lookUpSpaces(mstrVariableName, lVarNameSpaces);
			UnknownVariable varUnknown = UnknownVarOperator.lookUpList(mstrVariableName, lUnknownVars);
			// unknown variable overrides same-name known variable.
			if (varUnknown == null && varKnown == null)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
			}
			if (varUnknown != null)	{
                return varUnknown.getSolvedValue();
            } else if (varKnown instanceof UnknownVariable) {	// varKnown != null and varKnown is actually an unknown variable
                return ((UnknownVariable)varKnown).getSolvedValue();
			} else	{	// varKnown != null
				return varKnown.getValue();
			}
		}
    }
    
    // avoid to do any overhead work.
	@Override
	public AbstractExpr evaluateAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr(); // still needs to do some basic validation.
		if (menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST)	{
			// pseudo-const is always saved in unknown variable space because it may be simplifed before the value is solved.
			UnknownVariable varUnknown = UnknownVarOperator.lookUpList(mstrVariableName, lUnknownVars);
			if (varUnknown == null)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
			}
            if (varUnknown.isValueAssigned()) {
                return new AEConst(varUnknown.getSolvedValue());
            } else {
                return this;
            }
		} else	{
			Variable varKnown = VariableOperator.lookUpSpaces(mstrVariableName, lVarNameSpaces);
			UnknownVariable varUnknown = UnknownVarOperator.lookUpList(mstrVariableName, lUnknownVars);
			// unknown variable overrides same-name known variable.
			if (varUnknown == null && varKnown == null)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
			}
			if (varUnknown != null)	{
                if (varUnknown.isValueAssigned()) {
                    return new AEConst(varUnknown.getSolvedValue());
                } else {
                    return this;
                }
            } else if (varKnown instanceof UnknownVariable) {	// varKnown != null and varKnown is actually an unknown variable
                if (((UnknownVariable)varKnown).isValueAssigned()) {
                    return new AEConst(((UnknownVariable)varKnown).getSolvedValue());
                } else {
                    return this;
                }
			} else	{	// varKnown != null and varKnown is known
				return new AEConst(varKnown.getValue());
			}
		}
    }
    
	@Override
	public AbstractExpr simplifyAExpr(
			LinkedList<UnknownVariable> lUnknownVars,
			LinkedList<LinkedList<Variable>> lVarNameSpaces,
            SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException {
		validateAbstractExpr();
		
        AEVar aeCopy = new AEVar();
        aeCopy.copy(this);
        
		LinkedList<AbstractExpr> listConditions = new LinkedList<AbstractExpr>();
		for (int idx = 0; idx < aeCopy.mlistConditions.size(); idx ++)	{
			listConditions.add(aeCopy.mlistConditions.get(idx).simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams));
		}
		aeCopy.mlistConditions = listConditions;
		
		if (aeCopy.menumAEType == ABSTRACTEXPRTYPES.ABSTRACTEXPR_PSEUDOCONST)	{
			// pseudo-const is always saved in unknown variable space because it may be simplifed before the value is solved.
			UnknownVariable varUnknown = UnknownVarOperator.lookUpList(aeCopy.mstrVariableName, lUnknownVars);
			if (varUnknown == null)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
			}
			try	{
				DataClass datumValue = varUnknown.getSolvedValue();
				DataClass datumTmp = new DataClass();
				datumTmp.copyTypeValueDeep(datumValue);
				return new AEConst(datumTmp);
			} catch (JSmartMathErrException e)	{
				if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_VARIABLE_VALUE_NOT_KNOWN)	{
					throw e;
				}
			}
		} else	{
			Variable varKnown = VariableOperator.lookUpSpaces(aeCopy.mstrVariableName, lVarNameSpaces);
			UnknownVariable varUnknown = UnknownVarOperator.lookUpList(aeCopy.mstrVariableName, lUnknownVars);
			// unknown variable overrides same-name known variable.
			if (varUnknown == null && varKnown == null)	{
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_UNDECLARED);
			}
			if (varUnknown != null)	{
				try	{
					DataClass datumValue = varUnknown.getSolvedValue();
					DataClass datumTmp = new DataClass();
					datumTmp.copyTypeValue(datumValue); // do not use deep copy because if a matrix, then needs to refer to it. Need copy because dataclass may change.
					return new AEConst(datumTmp);
				} catch (JSmartMathErrException e)	{
					if (e.m_se.m_enumErrorType != ERRORTYPES.ERROR_VARIABLE_VALUE_NOT_KNOWN)	{
						throw e;
					}
				}
            } else if (varKnown instanceof UnknownVariable) {	// varKnown != null and varKnown is actually an unknown variable
                if (((UnknownVariable)varKnown).isValueAssigned()) {
                    DataClass datumValue = ((UnknownVariable)varKnown).getSolvedValue();
					DataClass datumTmp = new DataClass();
					datumTmp.copyTypeValue(datumValue); // do not use deep copy because if a matrix, then needs to refer to it. Need copy because dataclass may change.
					return new AEConst(datumTmp);
                }
			} else	{	// varKnown != null and varKnown is known
				DataClass datumValue = varKnown.getValue();
				DataClass datumTmp = new DataClass();
				datumTmp.copyTypeValue(datumValue); // do not use deep copy because if a matrix, then needs to refer to it. Need copy because dataclass may change.
				return new AEConst(datumTmp);
			}
		}
		return aeCopy.distributeAExpr(simplifyParams);
	}

    @Override
    public boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight)  {    
        // null means no opt, nLeftOrRight == -1 means on left, == 0 means on both, == 1 means on right
        return false;
    }
    
	@Override
	public int compareAExpr(AbstractExpr aexpr) throws JFCALCExpErrException {
		if (menumAEType.getValue() < aexpr.menumAEType.getValue())	{
			return 1;
		} else if (menumAEType.getValue() > aexpr.menumAEType.getValue())	{
			return -1;
		} else	{
			return mstrVariableName.compareTo(((AEVar)aexpr).mstrVariableName);
		}
	}
	
	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	@Override
	public boolean isNegligible() throws JSmartMathErrException	{
		validateAbstractExpr();
		return false;
	}
	
	// output the string based expression of any abstract expression type.
	@Override
	public String output()	throws JFCALCExpErrException, JSmartMathErrException {
		validateAbstractExpr();
		String strOutput =  mstrVariableName;	// condition omitted.
		return strOutput;
	}

    @Override
    public AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars) throws JSmartMathErrException, JFCALCExpErrException {
        String strCvtedName = null;
        if (bNotConvertVar) {
            strCvtedName = mstrVariableName;
            for (int idx = 0; idx < listVars.size(); idx ++) {
                if (listVars.get(idx).equalsIgnoreCase(mstrVariableName)) {
                    strCvtedName = null;
                    break;  // do not convert.
                }
            }
        } else {
            strCvtedName = null;
            for (int idx = 0; idx < listVars.size(); idx ++) {
                if (listVars.get(idx).equalsIgnoreCase(mstrVariableName)) {
                    strCvtedName = mstrVariableName;
                    break;  // do convert.
                }
            }
        }
        
        if (strCvtedName == null) {
            return this;
        } else {
            if (listCvtedVars != null) {
                int idx = 0;
                for (; idx < listCvtedVars.size(); idx ++) {
                    if (listCvtedVars.get(idx).equalsIgnoreCase(strCvtedName)) {
                        break;  // it has been in the listCvtedVars, do not add again..
                    }
                }
                if (idx == listCvtedVars.size()) {  // it hasn't been in the listCvtedVars, add it.
                    listCvtedVars.add(strCvtedName);
                }
            }
            return new AEConst(new DataClass(this));
        }
    }

    @Override
    public AbstractExpr convertAExprDatum2AExpr() throws JSmartMathErrException {
        return this;
    }
    
    @Override
    public int getVarAppearanceCnt(String strVarName) {
        int nCnt = mstrVariableName.equalsIgnoreCase(strVarName)?1:0;
        return nCnt;
    }
}
