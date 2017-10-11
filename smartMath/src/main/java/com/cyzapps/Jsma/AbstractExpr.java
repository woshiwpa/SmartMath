package com.cyzapps.Jsma;

import java.util.LinkedList;

import com.cyzapps.Jfcalc.BaseData.CalculateOperator;
import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.BaseData.OPERATORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ExprEvaluator;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.PatternManager.PatternExprUnitMap;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;
import com.cyzapps.Jsma.UnknownVarOperator.UnknownVariable;

public abstract class AbstractExpr {    // AbstractExpr should try to be immutable. This means it should not be changed after initialization. parent should
                                        // be set in a upper structure. The only exception is AEConst, its mdatumValue should not be immutable considering that 
                                        // mdatumValue could be a matrix and it only supports reference copy and its content may change.
	
	public static abstract class AbstractExprInterrupter	{
		public abstract boolean shouldInterrupt();
		public abstract void interrupt() throws InterruptedException;		
	}
	public static AbstractExprInterrupter msaexprInterrupter = null;

	static public enum ABSTRACTEXPRTYPES
	{
		ABSTRACTEXPR_INVALID(0),
		ABSTRACTEXPR_DATAREFVALUE(1),	//  a data reference which does not include unknown part
		ABSTRACTEXPR_VALUE(2),	// including (complex number) and string
		ABSTRACTEXPR_KNOWNVAR(3),	// known variable (variable value obtained from mfps' upper namespace
		ABSTRACTEXPR_PSEUDOCONST(4),	// like a1 (a1 >= 10 etc.) Note that a1 cannot be a data array
		ABSTRACTEXPR_DATAREF(5),	// a data reference which may include unknown part like [[5], [x], [7]] or [3, sin(4)], (here ,sin(4) is not a value but a function).
		//ABSTRACTEXPR_DATAREFVAR(6),	// variable which is a data reference.
		ABSTRACTEXPR_VARIABLE(7),	// variable (can be single or data reference)
		
		// binary operators:
		ABSTRACTEXPR_BOPT_ASSIGN(10),	// =
		ABSTRACTEXPR_BOPT_COMPARE(11),	// >, <, ==, !=, >=, <=
		ABSTRACTEXPR_BOPT_BITWISE(12),	// bitwise and, or and xor operators
		ABSTRACTEXPR_BOPT_POSNEG(13),	// + and -, this is very special binary opt and can be either Add/Sub or Positive/negative
		ABSTRACTEXPR_BOPT_MULTIPLYDIV(14),	// * and / (can be matrix) 
		ABSTRACTEXPR_BOPT_LEFTDIV(15),	// \, swap in limited cases 
		ABSTRACTEXPR_BOPT_POWER(16),	// power operator
		
		// unary operators:
		ABSTRACTEXPR_UOPT_FALSE(51),	// equal to FALSE or not, only one operand
		ABSTRACTEXPR_UOPT_NOT(52),	// bit NOT, only one operand
		ABSTRACTEXPR_UOPT_FACTORIAL(53),	// factorial
		ABSTRACTEXPR_UOPT_PERCENT(54),	// percentage
		ABSTRACTEXPR_UOPT_TRANSPOSE(55),	// ': transpose of an at most 2-D matrix
		
		// indexing:
		ABSTRACTEXPR_INDEX(60),	// indexing
		
		ABSTRACTEXPR_FUNCTION(100),
		ABSTRACTEXPR_OTHERS(1000);	// we have type invalid, so type others is no longer needed.
		
		
		private int value; 

		private ABSTRACTEXPRTYPES(int i) { 
			value = i; 
		} 

		public int getValue() { 
			return value; 
		}
		
		public boolean isBinaryOpt()	{
			if (value >= 10 && value < 50)	{
				return true;
			}
			return false;
		}
		
		public boolean isUnaryOpt()	{
			if (value >= 50 && value < 100)	{
				return true;
			}
			return false;
		}
		
		public static ABSTRACTEXPRTYPES getAETypeFromOpt(OPERATORTYPES ot)	{
			switch(ot)	{
				case OPERATOR_ASSIGN:
					return ABSTRACTEXPR_BOPT_ASSIGN;
				case OPERATOR_EQ:
				case OPERATOR_NEQ:
				case OPERATOR_LARGER:
				case OPERATOR_SMALLER:
				case OPERATOR_NOLARGER:
				case OPERATOR_NOSMALLER:
					return ABSTRACTEXPR_BOPT_COMPARE;
				case OPERATOR_ADD:
				case OPERATOR_SUBTRACT:
					return ABSTRACTEXPR_BOPT_POSNEG;
				case OPERATOR_MULTIPLY:
				case OPERATOR_DIVIDE:
				case OPERATOR_LEFTDIVIDE:
					return ABSTRACTEXPR_BOPT_MULTIPLYDIV;
				case OPERATOR_AND:
				case OPERATOR_OR:
				case OPERATOR_XOR:
					return ABSTRACTEXPR_BOPT_BITWISE;
				case OPERATOR_POWER:
					return ABSTRACTEXPR_BOPT_POWER;
				case OPERATOR_POSSIGN:
				case OPERATOR_NEGSIGN:
					return ABSTRACTEXPR_BOPT_POSNEG;
				case OPERATOR_FALSE:
					return ABSTRACTEXPR_UOPT_FALSE;
				case OPERATOR_NOT:
					return ABSTRACTEXPR_UOPT_NOT;
				case OPERATOR_FACTORIAL:
					return ABSTRACTEXPR_UOPT_FACTORIAL;
				case OPERATOR_PERCENT:
					return ABSTRACTEXPR_UOPT_PERCENT;
				case OPERATOR_TRANSPOSE:
					return ABSTRACTEXPR_UOPT_TRANSPOSE;
				default:
					return ABSTRACTEXPR_OTHERS;				
			}
		}

	};
    
    public static class SimplifyParams {
        public final boolean mbIgnoreMatrixDim;
        public final boolean mbNotSimplifyAExprDatum;
        public final boolean mbAllowCvtFunc2MoreThan1Funcs;
        public SimplifyParams(boolean bIgnoreMatrixDim, boolean bNotSimplifyAExprDatum, boolean bAllowCvtFunc2MoreThan1Funcs) {
            mbIgnoreMatrixDim = bIgnoreMatrixDim;
            mbNotSimplifyAExprDatum = bNotSimplifyAExprDatum;
            mbAllowCvtFunc2MoreThan1Funcs = bAllowCvtFunc2MoreThan1Funcs;
        }
    }

	/* 
	 * An expression unit is something like 4-7i (a constant number), "abc efg" (a string),
	 * [x+4, 5^y] (a matrix), x^3, 3*x, x*y, x^y, sin(4+x) (where 4 + x is an expression group),
	 * funname(x*y, 4-5y)... . In other words, an expression unit cannot be splitted into two
	 * or more expression units by plus or minus.
	 */
	public ABSTRACTEXPRTYPES menumAEType = ABSTRACTEXPRTYPES.ABSTRACTEXPR_OTHERS;
	
	protected abstract void initAbstractExpr();
	
	public abstract void validateAbstractExpr() throws JSmartMathErrException;

    // This function cannot be public coz we want to make AbstractExpr immutable
	protected void copy(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		menumAEType = aexprOrigin.menumAEType;
	}
	
	/*
	 * The difference between copy and deep copy is that copy does not really copy the
	 * child abstract expressions. Instead, the new aexpr still refer to the old aexpr's
	 * child abstract expressions.
     * This function cannot be public coz we want to make AbstractExpr immutable
	 */
	protected void copyDeep(AbstractExpr aexprOrigin) throws JFCALCExpErrException, JSmartMathErrException	{
		menumAEType = aexprOrigin.menumAEType;
	}
	
	// clone a new instance of itself.
	public abstract AbstractExpr cloneSelf() throws JFCALCExpErrException, JSmartMathErrException;
	
	public abstract int[] recalcAExprDim(boolean bUnknownAsSingle) throws JSmartMathErrException, JFCALCExpErrException;

	public abstract boolean isEqual(AbstractExpr aexpr) throws JFCALCExpErrException;
	
	public abstract boolean isPatternMatch (AbstractExpr aePattern,
											LinkedList<PatternExprUnitMap> listpeuMapPseudoFuncs,
											LinkedList<PatternExprUnitMap> listpeuMapPseudoConsts,
											LinkedList<PatternExprUnitMap> listpeuMapUnknowns,
											boolean bAllowConversion)  throws JFCALCExpErrException, JSmartMathErrException, InterruptedException;
	
	public abstract boolean isKnownValOrPseudo();
	
	public boolean isVariable()	{
		return false;
	}
	
	public abstract LinkedList<AbstractExpr> getListOfChildren();
    
    public abstract AbstractExpr copySetListOfChildren(LinkedList<AbstractExpr> listChildren)   throws JFCALCExpErrException, JSmartMathErrException;
	
	// this function replaces children who equal aeFrom to aeTo and returns the number of children that are replaced.
	public abstract AbstractExpr replaceChildren(LinkedList<PatternExprUnitMap> listFromToMap, boolean bExpr2Pattern, LinkedList<AbstractExpr> listReplacedChildren) throws JFCALCExpErrException, JSmartMathErrException;
	
	public abstract AbstractExpr distributeAExpr(SimplifyParams simplifyParams) throws JFCALCExpErrException, JSmartMathErrException, InterruptedException;

	/*
	 * This function simplify an abstract expression. Note that it will try to simpify as many children as possible.
	 * And it still throws exceptions. But it only throw exceptions when there is a real error, i.e. an error that
	 * cannot be recovered. For example, unknown variable y, x[y] = 3, y = 2 may lead to an exception because y by default is
	 * null. But it is not a real error because y will be finally assigned 2.
	 * lVarNameSpaces includes both known variables and unknown variables. The unknown variables are also
	 * included in lUnknownVars.
	 */
	public abstract AbstractExpr simplifyAExpr(LinkedList<UnknownVariable> lUnknownVars,
												LinkedList<LinkedList<Variable>> lVarNameSpaces,
												SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException;
	
	public abstract boolean needBracketsWhenToStr(ABSTRACTEXPRTYPES enumAET, int nLeftOrRight) throws JFCALCExpErrException;
	
    // evaluate an expression. If cannot get data value, throws an exception. The part or all of the returned data may be used by aexpr so may need deep copy after return.
    public abstract DataClass evaluateAExprQuick(LinkedList<UnknownVariable> lUnknownVars,
                                                LinkedList<LinkedList<Variable>> lVarNameSpaces)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException;
	
    // evaluate an expression. If cannot get data value, returns the most possibly evaluated aexpr. The part or all of the returned data may be used by aexpr so may need deep copy after return.
    public abstract AbstractExpr evaluateAExpr(LinkedList<UnknownVariable> lUnknownVars,
                                                LinkedList<LinkedList<Variable>> lVarNameSpaces)
            throws InterruptedException, JSmartMathErrException, JFCALCExpErrException;
    
    public abstract AbstractExpr convertAEVar2AExprDatum(LinkedList<String> listVars, boolean bNotConvertVar, LinkedList<String> listCvtedVars)
            throws JSmartMathErrException, JFCALCExpErrException;
    
    public abstract AbstractExpr convertAExprDatum2AExpr()
            throws JSmartMathErrException;

    public abstract int getVarAppearanceCnt(String strVarName);
    
	// this member function does not change this.
	public AbstractExpr simplifyAExprMost(LinkedList<UnknownVariable> lUnknownVars, LinkedList<LinkedList<Variable>> lVarNameSpaces, SimplifyParams simplifyParams)
			throws InterruptedException, JSmartMathErrException, JFCALCExpErrException	{
		if (msaexprInterrupter != null) {
			// for debug or killing a background thread.
			if (msaexprInterrupter.shouldInterrupt())    {
				msaexprInterrupter.interrupt();
			}
		}
		LinkedList<AbstractExpr> listSimplified = new LinkedList<AbstractExpr>();
		listSimplified.add(this);
		int idx = 0;
		while (true)	{
			AbstractExpr aeAfter = listSimplified.get(idx).simplifyAExpr(lUnknownVars, lVarNameSpaces, simplifyParams);
            if (aeAfter instanceof AEConst && ((AEConst)aeAfter).getDataClassRef().getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                return aeAfter; // a normal constant which is not an aexpr cannot be further simplified.
            }
			for (int idx1 = idx; idx1 >= 0; idx1 --)	{
				if (aeAfter.isEqual(listSimplified.get(idx1)))	{
					return aeAfter;
				}
			}
			listSimplified.add(aeAfter);
			idx ++;
			if (idx > 24)	{	// we have simplify 24 times, the expression is too complicated, exit.
				return aeAfter;
			}
		}
	}
		
	// 1 if this should be left of aexpr, -1 if this should be right of aexpr, 0 if this and aexpr should have
	// the same position.
	public abstract int compareAExpr(AbstractExpr aexpr) throws JSmartMathErrException, JFCALCExpErrException;

	// identify if it is very, very close to 0 or zero array. Assume the expression has been simplified most
	public abstract boolean isNegligible() throws JSmartMathErrException, JFCALCExpErrException;
	
	// output the string based expression of the abstract expression.
	public abstract String output() throws JFCALCExpErrException, JSmartMathErrException;
	
	public String toString()	{
		String strReturn = "";
		try {
			strReturn = output();
		} catch (JSmartMathErrException e) {
			// TODO Auto-generated catch block
			strReturn = e.toString();
			e.printStackTrace();
		} catch (JFCALCExpErrException e) {
			// TODO Auto-generated catch block
			strReturn = e.toString();
			e.printStackTrace();
		}
		return strReturn;

	}
	/********************************* static member functions *********************************/
	
	/*
	 * order two abstract expressions, return 1 if aexpr1 should be the left of
	 * aexpr2, return -1 if vice versa, return 0 if aexpr1 is the same as aexpr2.
	 */
	public static int compareTwoAExprs(AbstractExpr aexpr1, AbstractExpr aexpr2) throws JSmartMathErrException, JFCALCExpErrException	{
		return aexpr1.compareAExpr(aexpr2);
	}

	public static boolean isCompareOpt(CalculateOperator co)	{
		OPERATORTYPES ot = co.getOperatorType();
		if (ot == OPERATORTYPES.OPERATOR_LARGER || ot == OPERATORTYPES.OPERATOR_NOSMALLER
				|| ot == OPERATORTYPES.OPERATOR_EQ
				|| ot == OPERATORTYPES.OPERATOR_NOLARGER || ot == OPERATORTYPES.OPERATOR_SMALLER)	{
			return true;
		}
		return false;
	}

	public static boolean isExprsEqual(AbstractExpr[] aexprs1, AbstractExpr[] aexprs2) throws JFCALCExpErrException	{
		if (aexprs1.length != aexprs2.length)   {
			return false;
		}
		for (int idx = 0; idx < aexprs1.length; idx ++) {
			boolean bIsExprEqual = false;
			for (int idx1 = 0; idx1 < aexprs2.length; idx1 ++)  {
				if (aexprs1[idx].isEqual(aexprs2[idx1]))	{
					bIsExprEqual = true;
					break;
				}
			}
			if (bIsExprEqual == false)  {
				return false;
			}
		}
		return true;
	}
    
    // Identify if the two expressions must be negative of each other or positive of each other
    // If must be negative, returns -1, must be positive, returns 1, otherwise returns 0. Remember
    // that return 0 does not mean they cannot be negative or positive to each other.
    public static int mustBeNegativeOrPositive(AbstractExpr aeInput1, AbstractExpr aeInput2) throws JFCALCExpErrException {
        if (aeInput1.isEqual(aeInput2)) {
            return 1;
        } else if (aeInput1 instanceof AEPosNegOpt && ((AEPosNegOpt)aeInput1).mlistChildren.size() == 1 && ((AEPosNegOpt)aeInput1).mlistChildren.getFirst().isEqual(aeInput2)) {
            if (((AEPosNegOpt)aeInput1).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                    || ((AEPosNegOpt)aeInput1).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN) {
                return 1;   // pos sign
            } else {
                return -1;  // neg sign
            }
        } else if (aeInput2 instanceof AEPosNegOpt && ((AEPosNegOpt)aeInput2).mlistChildren.size() == 1 && ((AEPosNegOpt)aeInput2).mlistChildren.getFirst().isEqual(aeInput1)) {
            if (((AEPosNegOpt)aeInput2).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                    || ((AEPosNegOpt)aeInput2).mlistOpts.getFirst().getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN) {
                return 1;   // pos sign
            } else {
                return -1;  // neg sign
            }
        } else if (aeInput1 instanceof AEPosNegOpt && aeInput2 instanceof AEPosNegOpt && aeInput1.getListOfChildren().size() == aeInput2.getListOfChildren().size()) {
            int nPosNeg = 0;
            for (int idx = 0; idx < aeInput1.getListOfChildren().size(); idx ++) {
                int nThisPosNeg = mustBeNegativeOrPositive(aeInput1.getListOfChildren().get(idx), aeInput2.getListOfChildren().get(idx));
                if (nThisPosNeg != 0) {
                    if ((((AEPosNegOpt)aeInput1).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                            || ((AEPosNegOpt)aeInput1).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)
                            && (((AEPosNegOpt)aeInput2).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT
                            || ((AEPosNegOpt)aeInput2).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)){
                        nThisPosNeg *= -1;
                    } else if ((((AEPosNegOpt)aeInput2).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_ADD
                            || ((AEPosNegOpt)aeInput2).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_POSSIGN)
                            && (((AEPosNegOpt)aeInput1).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_SUBTRACT
                            || ((AEPosNegOpt)aeInput1).mlistOpts.get(idx).getOperatorType() == OPERATORTYPES.OPERATOR_NEGSIGN)) {
                        nThisPosNeg *= -1;
                    }
                } else {
                    nPosNeg = 0;
                    break;
                }
                if (idx == 0) {
                    nPosNeg = nThisPosNeg;
                } else if (nPosNeg != nThisPosNeg) {
                    nPosNeg = 0;
                    break;
                }
            }
            return nPosNeg;
        } else if (aeInput1 instanceof AEMulDivOpt && aeInput2 instanceof AEMulDivOpt && aeInput1.getListOfChildren().size() == aeInput2.getListOfChildren().size()) {
            int nPosNeg = 1;
            for (int idx = 0; idx < aeInput1.getListOfChildren().size(); idx ++) {
                if (((AEMulDivOpt)aeInput1).mlistOpts.get(idx).getOperatorType() != ((AEMulDivOpt)aeInput2).mlistOpts.get(idx).getOperatorType()) {
                    nPosNeg = 0;
                }
            }
            if (nPosNeg != 0) {
                for (int idx = 0; idx < aeInput1.getListOfChildren().size(); idx ++) {
                    int nThisPosNeg = mustBeNegativeOrPositive(aeInput1.getListOfChildren().get(idx), aeInput2.getListOfChildren().get(idx));
                    if (nThisPosNeg != 0) {
                        nPosNeg *= nThisPosNeg;
                    } else {
                        nPosNeg = 0;
                        break;
                    }
                }
            }
            return nPosNeg;
        } else if (aeInput1 instanceof AELeftDivOpt && aeInput2 instanceof AELeftDivOpt) {
            AbstractExpr aeLeft1 = ((AELeftDivOpt)aeInput1).maeLeft;
            AbstractExpr aeRight1 = ((AELeftDivOpt)aeInput1).maeRight;
            AbstractExpr aeLeft2 = ((AELeftDivOpt)aeInput2).maeLeft;
            AbstractExpr aeRight2 = ((AELeftDivOpt)aeInput2).maeRight;
            int nPosNeg = mustBeNegativeOrPositive(aeLeft1, aeRight1);
            if (nPosNeg != 0) {
                nPosNeg *= mustBeNegativeOrPositive(aeLeft2, aeRight2);
            }
            return nPosNeg;
        } else if (aeInput1 instanceof AEDataRef && aeInput2 instanceof AEDataRef && aeInput1.getListOfChildren().size() == aeInput2.getListOfChildren().size()) {
            int nPosNeg = 0;
            for (int idx = 0; idx < aeInput1.getListOfChildren().size(); idx ++) {
                int nThisPosNeg = mustBeNegativeOrPositive(aeInput1.getListOfChildren().get(idx), aeInput2.getListOfChildren().get(idx));
                if (nThisPosNeg ==0) {
                    nPosNeg = 0;
                    break;
                }
                if (idx == 0) {
                    nPosNeg = nThisPosNeg;
                } else if (nPosNeg != nThisPosNeg) {
                    nPosNeg = 0;
                    break;
                }
            }
            return nPosNeg;
        } else if (aeInput1 instanceof AEIndex && aeInput2 instanceof AEIndex) {
            AbstractExpr aeInput1Base = ((AEIndex)aeInput1).maeBase;
            AbstractExpr aeInput1Idx = ((AEIndex)aeInput1).maeIndex;
            AbstractExpr aeInput2Base = ((AEIndex)aeInput2).maeBase;
            AbstractExpr aeInput2Idx = ((AEIndex)aeInput2).maeIndex;
            if (aeInput1Idx.isEqual(aeInput2Idx)) {
                return mustBeNegativeOrPositive(aeInput1Base, aeInput2Base);
            }
        } else if (aeInput1 instanceof AEConst && aeInput2 instanceof AEConst) {
            DataClass datum1 = ((AEConst)aeInput1).getDataClassRef();
            DataClass datum2 = ((AEConst)aeInput2).getDataClassRef();
            if (datum1.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR && datum2.getDataType() == DATATYPES.DATUM_ABSTRACT_EXPR) {
                return mustBeNegativeOrPositive(datum1.getAExpr(), datum2.getAExpr());
            } else if (datum1.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR && datum2.getDataType() != DATATYPES.DATUM_ABSTRACT_EXPR) {
                if (datum1.isEqual(datum2)) {
                    return 1;
                } else {
                    DataClass datum1Neg = ExprEvaluator.evaluateOneOperandCell(new CalculateOperator(OPERATORTYPES.OPERATOR_NEGSIGN, 1, true), datum1);
                    if (datum1Neg.isEqual(datum2)) {
                        return -1;
                    }
                }
            }
        }
        return 0;
    }
}
