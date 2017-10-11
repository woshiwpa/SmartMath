/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 *
 * @author tonyc
 * 
 * MFPNumeric is a read-only class. In other words, value of it cannot be changed after it is assigned.
 * because MFPNumeric is read only, no need for MFPNumeric.copy, MFPNumeric.clone and MFPNumeric(MFPNumeric) construction.
 */
public class MFPNumeric {
	// note that the following ranges are always inclusive.
    // if two integers are in the following range, we use primitive long add/sub to save computing time. this range ensures accuracy and no overflow.
    public static final long PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MAX = Long.MAX_VALUE / 2;
    public static final long PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MIN = Long.MIN_VALUE / 2;
    
    public static final long PRIMITIVE_LONG_MULTIPLY_INPUT_RANGE_MAX = (long)Math.sqrt(Long.MAX_VALUE);
    public static final long PRIMITIVE_LONG_MULTIPLY_INPUT_RANGE_MIN = -(long)Math.sqrt(Long.MAX_VALUE);
    
    // if overflows, double operation returns inf, inf is out of the range, so we use result range.
    public static final double PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX = Math.pow(2, 32);    // 4.2 * 10**9
    public static final double PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN = Math.pow(2, -32);
    
    public static final double POWER_EXPONENT_BIG_DECIMAL_CALCULATION_STEP = 256;    // was Math.pow(2, 6);
    public static final double POWER_EXPONENT_REASONABLE_ACCU_POS_RANGE_MAX = 8;    // so that the error is less than 10**-15;
    public static final double EXP_TO_VALUE_RESULT_ZERO_THRESH = -128;    // exp(-128) is too small and it is calculated as zero;
    public static final double POWER_BASE_TO_POSTIVE_ZERO_RESULT_THRESHOLD = 0.92;  // 0.92**2048 cannot be accrately calculated and it is returned as zero.
    public static final double POWER_BASE_TO_NEGATIVE_ZERO_RESULT_THRESHOLD = 1.08;  // 1.08**2048 cannot be accrately calculated and it is returned as zero.
    public static final double POWER_BASE_TO_VALUE_ZERO_RESULT_THRESHOLD = 2048;
    
    public static final int THE_MAX_ROUNDING_SCALE = 64;   // was 64;
    public static final double BIG_DECIMAL_DEFAULT_ROUNDING_ERROR = Math.pow(0.1, THE_MAX_ROUNDING_SCALE);
    public static final double THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC = 5.0e-48;
    public static final BigDecimal BIG_DEC_THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC = BigDecimal.valueOf(THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC);
	public static final double THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC = 5.0e-48;
    public static final BigDecimal BIG_DEC_THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC = BigDecimal.valueOf(THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC);

    public static int ROUND_CEILING = BigDecimal.ROUND_CEILING;
    public static int ROUND_DOWN = BigDecimal.ROUND_DOWN;
    public static int ROUND_FLOOR = BigDecimal.ROUND_FLOOR;
    public static int ROUND_HALF_DOWN = BigDecimal.ROUND_HALF_DOWN;
    public static int ROUND_HALF_EVEN = BigDecimal.ROUND_HALF_EVEN;
    public static int ROUND_HALF_UP = BigDecimal.ROUND_HALF_UP;
    public static int ROUND_UNNECESSARY = BigDecimal.ROUND_UNNECESSARY;
    public static int ROUND_UP = BigDecimal.ROUND_UP;
    
    public static final MFPNumeric ONE = new MFPNumeric(1);
    public static final MFPNumeric MINUS_ONE = new MFPNumeric(-1);
    public static final MFPNumeric TWO = new MFPNumeric(2);
    public static final MFPNumeric TEN = new MFPNumeric(10);
    public static final MFPNumeric HALF = new MFPNumeric("0.5");
    public static final MFPNumeric ONE_TENTH = new MFPNumeric("0.1");
    public static final MFPNumeric ZERO = new MFPNumeric(0);
    // do not need pi and e's string values.
	public static final String PI_STRING100 = "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679";	// 100 bits
	public static final String PI_STRING64 = "3.1415926535897932384626433832795028841971693993751058209749445923";	// 64 bits, will be much faster if use it.
    public static final MFPNumeric PI = new MFPNumeric(PI_STRING64); // 64 bits
    public static final MFPNumeric TWO_PI = new MFPNumeric(MFPNumeric.PI.mbigDecimalValue.multiply(BigDecimal.valueOf(2)));
    public static final MFPNumeric PI_OVER_TWO = new MFPNumeric(MFPNumeric.PI.mbigDecimalValue.multiply(BigDecimal.valueOf(0.5)));
    public static final MFPNumeric PI_OVER_THREE = new MFPNumeric(divide(MFPNumeric.PI.mbigDecimalValue, BigDecimal.valueOf(3)));
    public static final MFPNumeric PI_OVER_FOUR = new MFPNumeric(MFPNumeric.PI.mbigDecimalValue.multiply(BigDecimal.valueOf(0.25)));
    public static final MFPNumeric PI_OVER_SIX = new MFPNumeric(divide(MFPNumeric.PI.mbigDecimalValue, BigDecimal.valueOf(6)));
    public static final String E_STRING100 = "2.7182818284590452353602874713526624977572470936999595749669676277240766303535475945713821785251664274";
    public static final String E_STRING64 = "2.7182818284590452353602874713526624977572470936999595749669676277";
    public static final MFPNumeric E = new MFPNumeric(E_STRING64);  // 64 bits
    public static final MFPNumeric TRUE = new MFPNumeric("true");
    public static final MFPNumeric FALSE = new MFPNumeric("false");
    public static final MFPNumeric NAN = new MFPNumeric("nan");
    public static final MFPNumeric POS_INF = new MFPNumeric("inf");
    public static final MFPNumeric NEG_INF = new MFPNumeric("-inf");
    public static final MFPNumeric INF = POS_INF;

    // note that if mlValue is converted from mdValue or bigDecimalValue, it's possible that mlValue == 0 
    // while mdValue == 0.999999999999999999999999999999999999... So never trust the values which are not
    // used by the MFPNumeric type.
    protected long mlValue = 0;
    protected double mdValue = 0.0;
    protected BigInteger mbigIntegerValue = BigInteger.ZERO;
    protected BigDecimal mbigDecimalValue = BigDecimal.ZERO;
    
    protected boolean mbIsActuallyInteger = false;
    protected double mdActualValueCompZero = 0;
    
    public enum Type {
        MFP_BOOLEAN_VALUE,
        MFP_INTEGER_VALUE,
        MFP_DOUBLE_VALUE,
        MFP_POSITIVE_INF,
        MFP_NEGATIVE_INF,
        MFP_NAN_VALUE
    }
    public Type mType = Type.MFP_DOUBLE_VALUE;  // by default, it is double value.
    
    public MFPNumeric() {
        // do nothing here because the members have been initialized.
    }
    
    public MFPNumeric(BigDecimal bigDecValue, double dValue, BigInteger bigIntegerValue, long lValue, Type typeInfo) {
        mbigDecimalValue = bigDecValue;
        mdValue = dValue;
        mbigIntegerValue = bigIntegerValue;
        mlValue = lValue;
        mType = typeInfo;
        
        // now let's check if this MFPNumeric is an integer and compare it with 0.
        // we cannot use double value to check because double value's accuracy is not
        // high enough. Consider a decimal value 1.00000000000000000019. Its double
        // value will be 1 so that if we use double to check, we believe it is integer
        // however, the rounding error is 19*10-21 >> the max rounding error defined.
        // As such, we have to use bigDecimal. However, for zero check it is ok because
        // when compared to 0, error of a double is itself. Even double value is inf
        // it is fine.
        mdActualValueCompZero = compareToNoRelative(mdValue, 0, 1);   // use double to compare with zero so that it is fast. This is the only special case we can use compareTo(double, double).
        BigDecimal bigDecRoundValue = mbigDecimalValue.setScale(0, RoundingMode.HALF_UP);
        // use big decimal to compare with integer so that it is accurate.
        mbIsActuallyInteger = (compareToNoRelative(mbigDecimalValue, bigDecRoundValue, 1) == 0);
    }
    
    public MFPNumeric(BigDecimal bigDecValue, double dValue, BigInteger bigIntegerValue, long lValue, Type typeInfo,
            double dActualValueCompZero, boolean bIsActuallyInteger) {
        mbigDecimalValue = bigDecValue;
        mdValue = dValue;
        mbigIntegerValue = bigIntegerValue;
        mlValue = lValue;
        mType = typeInfo;
        mdActualValueCompZero = dActualValueCompZero;
        mbIsActuallyInteger = bIsActuallyInteger;
    }
    
    public MFPNumeric(BigDecimal bigDecValue)   {
        mbigDecimalValue = bigDecValue;//.stripTrailingZeros(); // seems useless.
        mbigIntegerValue = mbigDecimalValue.toBigInteger();
        mlValue = mbigIntegerValue.longValue();
        int nComparelValue = mbigIntegerValue.compareTo(BigInteger.valueOf(mlValue));
        if (nComparelValue > 0)    {
            mlValue = Long.MAX_VALUE;   // means long overflowed.
        } else if (nComparelValue < 0)    {
            mlValue = Long.MIN_VALUE;   // means long overflowed.
        }
        mdValue = mbigDecimalValue.doubleValue();
        mType = Type.MFP_DOUBLE_VALUE;
        
        // now let's check if this MFPNumeric is an integer and compare it with 0.
        // we cannot use double value to check because double value's accuracy is not
        // high enough. Consider a decimal value 1.00000000000000000019. Its double
        // value will be 1 so that if we use double to check, we believe it is integer
        // however, the rounding error is 19*10-21 >> the max rounding error defined.
        // As such, we have to use bigDecimal. However, for zero check it is ok because
        // when compared to 0, error of a double is itself. Even double value is inf
        // it is fine.
        mdActualValueCompZero = compareToNoRelative(mdValue, 0, 1);   // use double to compare with zero so that it is fast. This is the only special case we can use compareTo(double, double).
        BigDecimal bigDecRoundValue = mbigDecimalValue.setScale(0, RoundingMode.HALF_UP);
        // use big decimal to compare with integer so that it is accurate.
        mbIsActuallyInteger = (compareToNoRelative(mbigDecimalValue, bigDecRoundValue, 1) == 0);
    }
    
    public MFPNumeric(BigInteger bigIntValue)   {
        mlValue = bigIntValue.longValue();
        int nComparelValue = bigIntValue.compareTo(BigInteger.valueOf(mlValue));
        if (nComparelValue > 0)    {
            mlValue = Long.MAX_VALUE;   // means long overflowed.
        } else if (nComparelValue < 0)    {
            mlValue = Long.MIN_VALUE;   // means long overflowed.
        }
        mdValue = bigIntValue.doubleValue();
        mbigIntegerValue = bigIntValue;
        mbigDecimalValue = new BigDecimal(bigIntValue);
        mType = Type.MFP_INTEGER_VALUE;
        
        // now let's check if this MFPNumeric is an integer and compare it with 0.
        mbIsActuallyInteger = true;
        mdActualValueCompZero = bigIntValue.compareTo(BigInteger.ZERO);
    }
    
    public MFPNumeric(double dValue)   {
        // if nan, big decimal, big integer and long are 0, double is nan.
        // else, if inf, big decimal and big integer are 0, double is inf and long is max/min value.
        // otherwise, assign the double value
        if (Double.isNaN(dValue)) {
            mdValue = dValue;
            mType = Type.MFP_NAN_VALUE;
            mbIsActuallyInteger = false;
            mdActualValueCompZero = Double.NaN;
        } else if (dValue == Double.POSITIVE_INFINITY) {
            mlValue = Long.MAX_VALUE;
            mdValue = dValue;
            mType = Type.MFP_POSITIVE_INF;
            mbIsActuallyInteger = false;
            mdActualValueCompZero = 1;
        } else if (dValue == Double.NEGATIVE_INFINITY) {
            mlValue = Long.MIN_VALUE;
            mdValue = dValue;
            mType = Type.MFP_NEGATIVE_INF;
            mbIsActuallyInteger = false;
            mdActualValueCompZero = -1;
        } else  {
            mlValue = (long)dValue;
            BigDecimal bigDecValue = new BigDecimal(dValue); // new BigDecimal(dValue) may not be accurate. but this is what we want. And we need not to strip tailing zeros.
            int nComparelValue = bigDecValue.toBigInteger().compareTo(BigInteger.valueOf(mlValue));
            if (nComparelValue > 0)    {
                mlValue = Long.MAX_VALUE;   // means long overflowed.
            } else if (nComparelValue < 0)    {
                mlValue = Long.MIN_VALUE;   // means long overflowed.
            }
            mdValue = dValue;
            mbigDecimalValue = bigDecValue; 
            mbigIntegerValue = mbigDecimalValue.toBigInteger(); // cannot get bigIntegerValue from mlValue because mlValue may be overflowed value, i.e. dValue > Long.MAX_VALUE
            mType = Type.MFP_DOUBLE_VALUE;
            
            // because this constructor's parameter is double, so we need not to use bigDecimal
            // to compare with 0 and integer value.
            mdActualValueCompZero = compareToNoRelative(mdValue, 0, 1);
            long lRoundValue = Math.round(mdValue);
            mbIsActuallyInteger = (compareToNoRelative(mdValue, lRoundValue, 1) == 0);
        }
    }
    
    public MFPNumeric(long lValue)   {
        mlValue = lValue;
        mdValue = lValue;
        mbigIntegerValue = BigInteger.valueOf(lValue);
        mbigDecimalValue = new BigDecimal(lValue);
        mType = Type.MFP_INTEGER_VALUE;
        
        // now let's check if this MFPNumeric is an integer and compare it with 0.
        mbIsActuallyInteger = true;
        mdActualValueCompZero = (lValue > 0)?1:(lValue < 0)?-1:0;
    }
    
    public MFPNumeric(boolean bValue)   {
        mlValue = bValue?1:0;
        mdValue = bValue?1.0:0.0;
        mbigIntegerValue = bValue?BigInteger.ONE:BigInteger.ZERO;
        mbigDecimalValue = bValue?BigDecimal.ONE:BigDecimal.ZERO;
        mType = Type.MFP_BOOLEAN_VALUE;
        // now let's check if this MFPNumeric is an integer and compare it with 0.
        mbIsActuallyInteger = true;
        mdActualValueCompZero = bValue?1:0;
    }
    
    public MFPNumeric(String strValue)  {
        // like BigDecimal, do not trim strValue here.
        if (strValue.equalsIgnoreCase("nan"))   {
            //Nan
            mdValue = Double.NaN;
            mType = Type.MFP_NAN_VALUE;
            mbIsActuallyInteger = false;
            mdActualValueCompZero = Double.NaN;
        } else if (strValue.equalsIgnoreCase("true"))   {
            mlValue = 1;
            mdValue = 1.0;
            mbigIntegerValue = BigInteger.ONE;
            mbigDecimalValue = BigDecimal.ONE;
            mType = Type.MFP_BOOLEAN_VALUE;
            mbIsActuallyInteger = true;
            mdActualValueCompZero = 1;
        } else if (strValue.equalsIgnoreCase("false"))   {
            mlValue = 0;
            mdValue = 0.0;
            mbigIntegerValue = BigInteger.ZERO;
            mbigDecimalValue = BigDecimal.ZERO;
            mType = Type.MFP_BOOLEAN_VALUE;
            mbIsActuallyInteger = true;
            mdActualValueCompZero = 0;
        } else if (strValue.equalsIgnoreCase("-inf"))   {
            mlValue = Long.MIN_VALUE;
            mdValue = Double.NEGATIVE_INFINITY;
            mType = Type.MFP_NEGATIVE_INF;
            mbIsActuallyInteger = false;
            mdActualValueCompZero = -1;
        } else if (strValue.equalsIgnoreCase("inf") || strValue.equalsIgnoreCase("+inf"))   {
            mlValue = Long.MAX_VALUE;
            mdValue = Double.POSITIVE_INFINITY;
            mType = Type.MFP_POSITIVE_INF;
            mbIsActuallyInteger = false;
            mdActualValueCompZero = 1;
        } else  {
            // throw NumberFormatException here, but doesn't matter because BigDecimal also throw NumberFormatException.
            mbigDecimalValue = new BigDecimal(strValue).stripTrailingZeros();
            mlValue = mbigDecimalValue.longValue();
            int nComparelValue = mbigDecimalValue.toBigInteger().compareTo(BigInteger.valueOf(mlValue));
            if (nComparelValue > 0)    {
                mlValue = Long.MAX_VALUE;   // means long overflowed.
            } else if (nComparelValue < 0)    {
                mlValue = Long.MIN_VALUE;   // means long overflowed.
            }
            mdValue = mbigDecimalValue.doubleValue();   // cannot use mlValue to get mdValue because mlValue may be overflowed value (bigger than Long.MAX_VALUE)
            mbigIntegerValue = mbigDecimalValue.toBigInteger();   // cannot use mlValue to get BigInteger because mlValue may be overflowed value (bigger than Long.MAX_VALUE)
            if (mbigDecimalValue.compareTo(new BigDecimal(mbigIntegerValue)) == 0)   {  // ok, (int)this == this so this should be an integer.
                mType = Type.MFP_INTEGER_VALUE;
                // now let's check if it is an integer and compare it to 0.
                mbIsActuallyInteger = true;
                mdActualValueCompZero = mbigIntegerValue.compareTo(BigInteger.ZERO);
            } else {
                mType = Type.MFP_DOUBLE_VALUE;
                // now let's check if this MFPNumeric is an integer and compare it with 0.
                // we cannot use double value to check because double value's accuracy is not
                // high enough. Consider a decimal value 1.00000000000000000019. Its double
                // value will be 1 so that if we use double to check, we believe it is integer
                // however, the rounding error is 19*10-21 >> the max rounding error defined.
                // As such, we have to use bigDecimal. However, for zero check it is ok because
                // when compared to 0, error of a double is itself. Even double value is inf
                // it is fine.
                mdActualValueCompZero = compareToNoRelative(mdValue, 0, 1);   // use double to compare with zero so that it is fast.
                BigDecimal bigDecRoundValue = mbigDecimalValue.setScale(0, RoundingMode.HALF_UP);
                // use big decimal to compare with integer so that it is accurate.
                mbIsActuallyInteger = (compareToNoRelative(mbigDecimalValue, bigDecRoundValue, 1) == 0);
            }
        }
    }
    
    public static MFPNumeric valueOf(BigDecimal bigDecValue)    {
        return new MFPNumeric(bigDecValue);
    }
    
    public static MFPNumeric valueOf(BigInteger bigIntValue)    {
        return new MFPNumeric(bigIntValue);
    }
    
    public static MFPNumeric valueOf(double dValue) {
        return new MFPNumeric(Double.toString(dValue));
    }
    
    public static MFPNumeric valueOf(long lValue)   {
        return new MFPNumeric(lValue);
    }
    
    public static MFPNumeric valueOf(boolean bValue)    {
        return new MFPNumeric(bValue);
    }
    
    public static MFPNumeric valueOf(String strValue)   {
        return new MFPNumeric(strValue);
    }
    
    public BigDecimal toBigDecimal()    {
        // even if it is nan or inf, still return mbigDecimalValue because bigDecimal doesn't support nan or inf.
        return mbigDecimalValue;
    }
    
    public BigInteger toBigInteger()    {
        // even if it is nan or inf, still return mbigDecimalValue because bigDecimal doesn't support nan or inf.
        return mbigIntegerValue;
    }
    
    public double doubleValue() {
        return mdValue;
    }
    public float floatValue() {
        return (float)mdValue;
    }
    
    public long longValue() {
        // how to handle NAN?
        return mlValue;
    }
    public int intValue() {
        return (int)mlValue;
    }
    public int shortValue() {
        return (short)mlValue;
    }
    public byte byteValue() {
        return (byte)mlValue;
    }
    
    public boolean booleanValue()   {
        // how to handle NAN?
        if (mType == Type.MFP_BOOLEAN_VALUE || mType == Type.MFP_INTEGER_VALUE) {
            return (mlValue == 0)?false:true;
        } else if (mType == Type.MFP_DOUBLE_VALUE)  {
            return mbigDecimalValue.compareTo(BigDecimal.ZERO) != 0;
        } else if (mType == Type.MFP_POSITIVE_INF || mType == Type.MFP_NEGATIVE_INF)    {
            return true;
        } else {
            return false;   // nan is false. Not sure.
        }
    }
    
    @Override
    public String toString()    {
        if (mType == Type.MFP_DOUBLE_VALUE) {
            if (isActuallyInteger())    {
                return mbigDecimalValue.setScale(0, RoundingMode.HALF_UP).toBigInteger().toString();    // if it is actually integer, print integer value.
            } else {
                return mbigDecimalValue.stripTrailingZeros().toPlainString();   // to prevent too many extra 0s.
            }
        } else if (mType == Type.MFP_INTEGER_VALUE)    {
            return mbigIntegerValue.toString();
        } else if (mType == Type.MFP_BOOLEAN_VALUE)    {
            return (mlValue == 0)?"FALSE":"TRUE";
        } else if (mType == Type.MFP_POSITIVE_INF)  {
            return "INF";
        } else if (mType == Type.MFP_NEGATIVE_INF)  {
            return "-INF";
        } else {// if (mType == Type.MFP_NAN_VALUE) {
            return "NAN";
        }
    }
    
    public MFPNumeric toDblOrNanInfMFPNum() {
        // convert to a double MFP Numeric. If NAN or Inf, returned MFPNumeric type is still NAN or Inf
        if (mType == Type.MFP_INTEGER_VALUE || mType == Type.MFP_BOOLEAN_VALUE)  {
            return new MFPNumeric(mbigDecimalValue, mdValue, mbigIntegerValue, mlValue, Type.MFP_DOUBLE_VALUE, mdActualValueCompZero, mbIsActuallyInteger);
        } else if (mType == Type.MFP_DOUBLE_VALUE) {
            return this;
        } else  {
            return this;    // can convert NAN or Inf to int MFPNumeric, in this case it is itself.
        }
    }
    
    public MFPNumeric toIntOrNanInfMFPNum() {
        // convert to a int MFP Numeric. If NAN or Inf, returned MFPNumeric type is still NAN or Inf
        if (mType == Type.MFP_BOOLEAN_VALUE)    {
            return new MFPNumeric(mlValue);
        } else if (mType == Type.MFP_INTEGER_VALUE)  {
            return this;
        } else if (mType == Type.MFP_DOUBLE_VALUE)  {
            if (isActuallyInteger())  {
                // if this double value is actually an integer
                MFPNumeric mfpNumRounded = new MFPNumeric(mbigDecimalValue.setScale(0, RoundingMode.HALF_UP).toBigInteger());
                return mfpNumRounded;
            } else  {
                return new MFPNumeric(mbigIntegerValue);
            }
        } else  {
            return this;    // can convert NAN or Inf to int MFPNumeric, in this case it is itself.
        }
    }
    
    public MFPNumeric toBoolMFPNum() throws ArithmeticException {
        // convert to a boolean MFP Numeric. If NAN, returned MFPNumeric type is still NAN
        if (mType == Type.MFP_BOOLEAN_VALUE)    {
            return this;
        } else if (mType == Type.MFP_INTEGER_VALUE)  {
            return new MFPNumeric((mlValue == 0)?false:true);
        } else if (mType == Type.MFP_DOUBLE_VALUE || isInf())  {
            return new MFPNumeric((mdValue == 0)?false:true);
        } else {
            throw new ArithmeticException();    // cannot convert NAN to boolean MFPNumeric because boolean range does not cover NaN.
        }
    }
    
    public boolean isNan()  {
        return mType == Type.MFP_NAN_VALUE;
    }
    
    public boolean isInf() {
        return mType == Type.MFP_POSITIVE_INF || mType == Type.MFP_NEGATIVE_INF;
    }
    
    public boolean isPosInf()   {
        return mType == Type.MFP_POSITIVE_INF;
    }
    
    public boolean isNegInf()   {
        return mType == Type.MFP_NEGATIVE_INF;
    }
    
    public boolean isNanOrInf() {
        return mType == Type.MFP_NAN_VALUE || mType == Type.MFP_POSITIVE_INF || mType == Type.MFP_NEGATIVE_INF;
    }
    
    public boolean isBoolean()  {
        return mType == Type.MFP_BOOLEAN_VALUE;
    }
    
    public boolean isInteger()  {
        return mType == Type.MFP_INTEGER_VALUE;
    }
    
    public boolean isActuallyInteger()    {
        return mbIsActuallyInteger;
    }
    
    public boolean isDouble()   {
        return mType == Type.MFP_DOUBLE_VALUE;
    }
    
    public boolean isZero() {
        if (((mType == Type.MFP_BOOLEAN_VALUE || mType == Type.MFP_INTEGER_VALUE) && mlValue == 0)
                || (mType == Type.MFP_DOUBLE_VALUE && mdValue == 0)) {
            // even if there is an overflow, mlValue should be zero if it is boolean false, or
            // integer 0 and mdValue should be 0 if it is double 0.
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isActuallyTrue() {
        return (mType != Type.MFP_NAN_VALUE && mdActualValueCompZero != 0);
    }
    
    public boolean isTrue() {   // is exactly true.
        return (mType == Type.MFP_BOOLEAN_VALUE && mlValue != 0);
    }
    
    public boolean isActuallyFalse()    {
        return (mType != Type.MFP_NAN_VALUE && mdActualValueCompZero == 0);
    }
    
    public boolean isFalse()    {   // is exactly false.
        return (mType == Type.MFP_BOOLEAN_VALUE && mlValue == 0);
    }
    
    public boolean isActuallyZero() {
        return mdActualValueCompZero == 0;
    }
    
    public boolean isPositive() {
        return (mdValue > 0);   // whether it is boolean, integer, double or positive inf, mdValue > 0
    }
    
    public boolean isActuallyPositive() {
        return mdActualValueCompZero > 0;
    }
    
    public boolean isNegative() {
        return (mdValue < 0);   // whether it is boolean, integer, double or negative inf, mdValue < 0
    }
    
    public boolean isActuallyNegative() {
        return mdActualValueCompZero < 0;
    }
    
    public boolean isNonPositive()  {
        return (mdValue <= 0);
    }
    
    public boolean isActuallyNonPositive()  {
        return mdActualValueCompZero <= 0;
    }
    
    public boolean isNonNegative()  {
        return (mdValue >= 0);
    }
    
    public boolean isActuallyNonNegative()  {
        return mdActualValueCompZero >= 0;
    }

    protected static double compareToNoRelative(double dA, double dB, double errorScale)  {
        // do not use divide to compare. just compare value - value.
        // not check if dA or dB is NaN or Inf, not check if error scale is positive or negative or 0. Assume it is valid when use.
        if (dA == dB)   {   // if dA == dB, definitely return 0.
            return 0;
        }
        double dAbsoluteErr = (errorScale == 1)?THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC
                :((errorScale == 0)?0:THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC* errorScale);
        double dAbsAMinusB = Math.abs(dA - dB);
        if (dAbsAMinusB <= dAbsoluteErr)  {
            // this is to compare very small values (values close to zero).
            return 0;
        } else {
            return (dA > dB)?1.0:-1.0;
        }
    }
    
    protected static double compareTo(double dA, double dB, double errorScale)  {
        // not check if dA or dB is NaN or Inf, not check if error scale is positive or negative or 0. Assume it is valid when use.
        if (dA == dB)   {   // if dA == dB, definitely return 0.
            return 0;
        }
        double dAbsoluteErr = (errorScale == 1)?THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC
                :((errorScale == 0)?0:THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC* errorScale);
        double dRelativeErr = (errorScale == 1)?THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC
                :((errorScale == 0)?0:THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC* errorScale);
        double dAbsAMinusB = Math.abs(dA - dB), dAbsA = Math.abs(dA), dAbsB = Math.abs(dB);
        if (dAbsAMinusB <= dAbsoluteErr && dAbsA <= dAbsoluteErr && dAbsB <= dAbsoluteErr)  {
            // this is to compare very small values (values close to zero).
            return 0;
        } else {
            double dDivBy = Math.max(dAbsA, dAbsB);
            if (dAbsAMinusB/dDivBy <= dRelativeErr)   { // need not to worry about dDivBy == 0 because this means dA == dB
                return 0;
            } else if (dA > dB) {
                return 1;
            } else {    // dA < dB
                return -1;
            }
        }
    }
    
    protected static double compareToNoRelative(BigDecimal bigDecA, BigDecimal bigDecB, double errorScale)  {
        // do not use divide to compare. just compare value - value.
        // not check if errorScale is positive or negative or 0, assume it is valid when use.
        // we cannot compare primitive values, we have to use big decimal to compare
        int nStrictlyCompare = bigDecA.compareTo(bigDecB);
        if (nStrictlyCompare == 0)    {
            return 0;   // A == B (strictly).
        } else {
            BigDecimal bigDecAbsoluteErr = (errorScale == 1)?BIG_DEC_THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC
                :((errorScale == 0)?BigDecimal.ZERO:BigDecimal.valueOf(THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC * errorScale));
            BigDecimal bigDecAbsAMinusB = bigDecA.subtract(bigDecB).abs();
            if (bigDecAbsAMinusB.compareTo(bigDecAbsoluteErr) <= 0) {
                // this is to compare very small values (values close to zero).
                return 0;
            } else {
                return Math.signum(nStrictlyCompare);
            }
        }
    }
    
    protected static double compareTo(BigDecimal bigDecA, BigDecimal bigDecB, double errorScale)    {
        // not check if errorScale is positive or negative or 0, assume it is valid when use.
        // we cannot compare primitive values, we have to use big decimal to compare
        if (bigDecA.compareTo(bigDecB) == 0)    {
            return 0;   // A == B (strictly).
        }
        
        BigDecimal bigDecAbsoluteErr = (errorScale == 1)?BIG_DEC_THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC
                :((errorScale == 0)?BigDecimal.ZERO:BigDecimal.valueOf(THE_MAX_ABSOLUTE_ERROR_OF_MFPNUMERIC * errorScale));
        BigDecimal bigDecRelativeErr = (errorScale == 1)?BIG_DEC_THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC
                :((errorScale == 0)?BigDecimal.ZERO:BigDecimal.valueOf(THE_MAX_RELATIVE_ERROR_OF_MFPNUMERIC * errorScale));
        
        BigDecimal bigDecAbsAMinusB = bigDecA.subtract(bigDecB).abs();
        BigDecimal bigDecAbsA = bigDecA.abs(), bigDecAbsB = bigDecB.abs();
        if (bigDecAbsAMinusB.compareTo(bigDecAbsoluteErr) <= 0
                && bigDecAbsA.compareTo(bigDecAbsoluteErr) <= 0
                && bigDecAbsB.compareTo(bigDecAbsoluteErr) <= 0) {
            // this is to compare numbers very close to zero
            return 0;
        } else  {
            BigDecimal bigdecDivBy;
            if (bigDecAbsA.compareTo(bigDecAbsB) > 0)	{
                bigdecDivBy = bigDecAbsA;
            } else	{
                bigdecDivBy = bigDecAbsB;
            }
            // need not to worry about bigdecDivBy is 0 because this means A==B==0. A==B has been covered above.
            if (bigdecDivBy.multiply(bigDecRelativeErr).compareTo(bigDecAbsAMinusB) >= 0) {
                return 0;
            } else {
                return bigDecA.compareTo(bigDecB);
            }
        }
    }
    
    // compareTo, return -1 (a < b), 0 (a == b), 1 (a > b) or NaN (one and only one of a and b is NaN)
    public static double compareTo(MFPNumeric a, MFPNumeric b, double errorScale) {
        if (a.mType == MFPNumeric.Type.MFP_NAN_VALUE && b.mType == MFPNumeric.Type.MFP_NAN_VALUE)   {
            return 0;
        } else if (a.mType == MFPNumeric.Type.MFP_NAN_VALUE || b.mType == MFPNumeric.Type.MFP_NAN_VALUE)    {
            return Double.NaN;
        }
        
        if (a.mType == MFPNumeric.Type.MFP_POSITIVE_INF && b.mType == MFPNumeric.Type.MFP_POSITIVE_INF)   {
            return 0;
        } else if (a.mType == MFPNumeric.Type.MFP_POSITIVE_INF) {
            return 1;
        } else if (b.mType == MFPNumeric.Type.MFP_POSITIVE_INF)    {
            return -1;
        }
        
        if (a.mType == MFPNumeric.Type.MFP_NEGATIVE_INF && b.mType == MFPNumeric.Type.MFP_NEGATIVE_INF)   {
            return 0;
        } else if (a.mType == MFPNumeric.Type.MFP_NEGATIVE_INF)    {
            return -1;
        } else if (b.mType == MFPNumeric.Type.MFP_NEGATIVE_INF)    {
            return 1;
        }

        if (errorScale < 0)    {   // error scale is a simple input, need not to use isActuallyNonNegative.
            errorScale = 0;    // ensure that error scale is always non-negative.
        } else if (errorScale == Double.POSITIVE_INFINITY)  {
            errorScale = Double.MAX_VALUE;  // error scale shouldn't be unreasonably large.
        }
                
        // always use big decimal instead of double value to compare, otherwise, 1.000000000000000000001 will be equal to 1.
        return compareTo(a.mbigDecimalValue, b.mbigDecimalValue, errorScale);
    }
    public static double compareTo(MFPNumeric a, MFPNumeric b) {
        return compareTo(a, b, 1);
    }
    public double compareTo(MFPNumeric mfpNumeric)    {
        return compareTo(this, mfpNumeric, 1);
    }
    
    // isequal?
    public static boolean isEqual(MFPNumeric a, MFPNumeric b, double errorScale) {
        return compareTo(a, b, errorScale) == 0;
    }
    public static boolean isEqual(MFPNumeric a, MFPNumeric b) {
        return compareTo(a, b, 1) == 0;
    }
    public boolean isEqual(MFPNumeric mfpNumeric)    {
        return compareTo(this, mfpNumeric, 1) == 0;
    }
    
    // abs
    public static MFPNumeric abs(MFPNumeric mfpNumeric)    {
        switch (mfpNumeric.mType)   {
            case MFP_NAN_VALUE: {
                return MFPNumeric.NAN;
            } case MFP_NEGATIVE_INF:    {
                return MFPNumeric.INF;
            } case MFP_POSITIVE_INF:    {
                return MFPNumeric.POS_INF;
            } case MFP_BOOLEAN_VALUE:   {
                // abs function always return a double or integer, as such boolean is converted to int.
                return (mfpNumeric.mlValue == 0)?MFPNumeric.ZERO:MFPNumeric.ONE;
            } case MFP_INTEGER_VALUE:   {
                return new MFPNumeric(mfpNumeric.mbigIntegerValue.abs());
            } default: { // double
                return new MFPNumeric(mfpNumeric.mbigDecimalValue.abs(), Math.abs(mfpNumeric.mdValue), mfpNumeric.mbigIntegerValue.abs(), Math.abs(mfpNumeric.mlValue),
                        mfpNumeric.mType, Math.abs(mfpNumeric.mdActualValueCompZero), mfpNumeric.mbIsActuallyInteger);
            }
        }
    }
    public MFPNumeric abs() {
        return abs(this);
    }
    
    // -this
    public static MFPNumeric negate(MFPNumeric mfpNumeric)  {
        switch (mfpNumeric.mType)   {
            case MFP_NAN_VALUE: {
                return MFPNumeric.NAN;
            } case MFP_NEGATIVE_INF:    {
                return MFPNumeric.INF;
            } case MFP_POSITIVE_INF:    {
                return MFPNumeric.NEG_INF;
            } case MFP_BOOLEAN_VALUE:   {
                // abs function always return a double or integer, as such boolean is converted to int.
                return (mfpNumeric.mlValue == 0)?MFPNumeric.ZERO:MFPNumeric.MINUS_ONE;
            } case MFP_INTEGER_VALUE:   {
                return new MFPNumeric(mfpNumeric.mbigIntegerValue.negate());
            } default: { // double
                return new MFPNumeric(mfpNumeric.mbigDecimalValue.negate(), -mfpNumeric.mdValue, mfpNumeric.mbigIntegerValue.negate(), -mfpNumeric.mlValue,
                        mfpNumeric.mType, -mfpNumeric.mdActualValueCompZero, mfpNumeric.mbIsActuallyInteger);
            }
        }
    }
    public MFPNumeric negate()  {
        return negate(this);
    }
    
    // signum
    public static MFPNumeric signum(MFPNumeric mfpNumeric)  {
        // do not use isActuallyZero here because zero can be + zero or - zero.
        if (mfpNumeric.mType == Type.MFP_NAN_VALUE) {
            return MFPNumeric.NAN;
        } else if (mfpNumeric.isPositive()) {
            return MFPNumeric.ONE;
        } else if (mfpNumeric.isNegative()) {
            return MFPNumeric.MINUS_ONE;
        } else  {
            return MFPNumeric.ZERO;
        }
    }
    public MFPNumeric signum()  {
        return signum(this);
    }
    
    // ceil
    public static MFPNumeric ceil(MFPNumeric mfpNumeric)    {
        if (mfpNumeric.mType == Type.MFP_DOUBLE_VALUE)  {
            BigDecimal bigDecFromInt = new BigDecimal(mfpNumeric.mbigIntegerValue);
            if (mfpNumeric.mbigDecimalValue.compareTo(bigDecFromInt) > 0) {
                return new MFPNumeric(mfpNumeric.mbigIntegerValue.add(BigInteger.ONE));
            } else {    //if (mfpNumeric.mbigDecimalValue.compareTo(bigDecFromInt) <= 0) 
                return new MFPNumeric(mfpNumeric.mbigIntegerValue);
            }
        } else {
            return mfpNumeric;  // ceil of nan or inf or integer value is itself.
        }
        
    }
    public MFPNumeric ceil()    {
        return ceil(this);
    }
    
    // floor
    public static MFPNumeric floor(MFPNumeric mfpNumeric)    {
        if (mfpNumeric.mType == Type.MFP_DOUBLE_VALUE)  {
            BigDecimal bigDecFromInt = new BigDecimal(mfpNumeric.mbigIntegerValue);
            if (mfpNumeric.mbigDecimalValue.compareTo(bigDecFromInt) < 0) {
                return new MFPNumeric(mfpNumeric.mbigIntegerValue.subtract(BigInteger.ONE));
            } else {    //if (mfpNumeric.mbigDecimalValue.compareTo(bigDecFromInt) >= 0) 
                return new MFPNumeric(mfpNumeric.mbigIntegerValue);
            }
        } else {
            return mfpNumeric;  // floor of nan or inf or integer value is itself.
        }
        
    }
    public MFPNumeric floor()    {
        return floor(this);
    }
    
    // add, subtract, multiply and divide: we use bigdecimal calculation. We never use primitive types in calculation.
    // This is because, although primitive types are quick, the accuracy is not good. double type's initialtive error
    // is 10**-16 of this value. After calculation, error will be even bigger. This cause problem when calculate matrix
    // division. Although we can add a new error field in MFPNumeric and use the error amount to identify if two values
    // are equal or not, the expense will be large and the benefit, compared to using primitive types, may not be so
    // significant. In the long run, we still need an error amount for compareTo function.
    // For power, log, exp, etc., we still have to use primitive types.
    
    // add
    public static MFPNumeric add(MFPNumeric mfpNumeric1, MFPNumeric mfpNumeric2)    {
        if (mfpNumeric1.mType == Type.MFP_NAN_VALUE || mfpNumeric2.mType == Type.MFP_NAN_VALUE
                || (mfpNumeric1.mType == Type.MFP_POSITIVE_INF && mfpNumeric2.mType == Type.MFP_NEGATIVE_INF)
                || (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF && mfpNumeric2.mType == Type.MFP_POSITIVE_INF)) {
            // nan adds anything is nan. inf + (-inf) is nan
            return MFPNumeric.NAN;
        } else if (mfpNumeric1.mType == Type.MFP_POSITIVE_INF || mfpNumeric2.mType == Type.MFP_POSITIVE_INF)    {
            // positive infinite adds anything is positive infinite except nan and negative infinite.
            return MFPNumeric.POS_INF;
        } else if (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF || mfpNumeric2.mType == Type.MFP_NEGATIVE_INF)    {
            // negative infinite adds anything is negative infinite except nan and positive infinite.
            return MFPNumeric.NEG_INF;
        } else if (mfpNumeric1.mType != Type.MFP_DOUBLE_VALUE && mfpNumeric2.mType != Type.MFP_DOUBLE_VALUE)  {
            // value 1 and value 2 are integer or boolean. need not to worry about +-inf or nan because above cases have included them
			if (mfpNumeric1.mlValue >= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MIN
				&& mfpNumeric1.mlValue <= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MAX
				&& mfpNumeric2.mlValue >= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MIN
				&& mfpNumeric2.mlValue <= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MAX)	{
				// inputs' absolute values are small enough, use primitive types to accelerate calculating.
				long lAdded = mfpNumeric1.mlValue + mfpNumeric2.mlValue;
				return new MFPNumeric(lAdded);
			} else	{
                BigInteger bigIntAdded = mfpNumeric1.mbigIntegerValue.add(mfpNumeric2.mbigIntegerValue);
                return new MFPNumeric(bigIntAdded);
            }
        } else  {
            // one of the parameter is double.
            BigDecimal bigDecAdded = mfpNumeric1.mbigDecimalValue.add(mfpNumeric2.mbigDecimalValue);
            return new MFPNumeric(bigDecAdded);
        }
    }
    public MFPNumeric add(MFPNumeric mfpNumeric)    {
        return add(this, mfpNumeric);
    }
    
    // subtract
    public static MFPNumeric subtract(MFPNumeric mfpNumeric1, MFPNumeric mfpNumeric2)    {
        if (mfpNumeric1.mType == Type.MFP_NAN_VALUE || mfpNumeric2.mType == Type.MFP_NAN_VALUE
                || (mfpNumeric1.mType == Type.MFP_POSITIVE_INF && mfpNumeric2.mType == Type.MFP_POSITIVE_INF)
                || (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF && mfpNumeric2.mType == Type.MFP_NEGATIVE_INF)) {
            // nan adds anything is nan. inf - inf is nan
            return MFPNumeric.NAN;
        } else if (mfpNumeric1.mType == Type.MFP_POSITIVE_INF || mfpNumeric2.mType == Type.MFP_NEGATIVE_INF)    {
            // positive infinite subtracts anything is positive infinite except nan and positive infinite.
            // anything except nan and negative infinite substracts negative infinite is positive infinite
            return MFPNumeric.POS_INF;
        } else if (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF || mfpNumeric2.mType == Type.MFP_POSITIVE_INF)    {
            // negative infinite subtracts anything is negative infinite except nan and negative infinite.
            // anything except nan and positive infinite subtracts positive infinite is negative infinite.
            return MFPNumeric.NEG_INF;
        } else if (mfpNumeric1.mType != Type.MFP_DOUBLE_VALUE && mfpNumeric2.mType != Type.MFP_DOUBLE_VALUE)  {
            // value 1 and value 2 are integer or boolean. need not to worry about +-inf or nan because above cases have included them
			if (mfpNumeric1.mlValue >= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MIN
				&& mfpNumeric1.mlValue <= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MAX
				&& mfpNumeric2.mlValue >= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MIN
				&& mfpNumeric2.mlValue <= PRIMITIVE_LONG_ADD_SUB_INPUT_RANGE_MAX)	{
				// inputs' absolute values are small enough, use primitive types to accelerate calculating.
				long lSubtracted = mfpNumeric1.mlValue - mfpNumeric2.mlValue;
				return new MFPNumeric(lSubtracted);
			} else	{
				BigInteger bigIntSubtracted = mfpNumeric1.mbigIntegerValue.subtract(mfpNumeric2.mbigIntegerValue);
				return new MFPNumeric(bigIntSubtracted);
			}
        } else  {
            // one of the value is double.
            BigDecimal bigDecSubtracted = mfpNumeric1.mbigDecimalValue.subtract(mfpNumeric2.mbigDecimalValue);
            return new MFPNumeric(bigDecSubtracted);
        }
    }
    public MFPNumeric subtract(MFPNumeric mfpNumeric)    {
        return subtract(this, mfpNumeric);
    }
    
    // multiply
    public static MFPNumeric multiply(MFPNumeric mfpNumeric1, MFPNumeric mfpNumeric2)    {
        if (mfpNumeric1.mType == Type.MFP_NAN_VALUE || mfpNumeric2.mType == Type.MFP_NAN_VALUE
                || (mfpNumeric1.isInf() && mfpNumeric2.isActuallyZero())
                || (mfpNumeric1.isActuallyZero() && mfpNumeric2.isInf()))   {
            // nan * anything == nan, inf * 0 == nan.
            return MFPNumeric.NAN;
        } else if ((mfpNumeric1.mType == Type.MFP_POSITIVE_INF && mfpNumeric2.isActuallyPositive())
                || (mfpNumeric1.isActuallyPositive() && mfpNumeric2.mType == Type.MFP_POSITIVE_INF)
                || (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF && mfpNumeric2.isActuallyNegative())
                || (mfpNumeric1.isActuallyNegative() && mfpNumeric2.mType == Type.MFP_NEGATIVE_INF))    {
            //inf * any positive value is INF. -inf * any negative value is INF.
            return MFPNumeric.INF;
        } else if ((mfpNumeric1.mType == Type.MFP_POSITIVE_INF && mfpNumeric2.isActuallyNegative())
                || (mfpNumeric1.isActuallyNegative() && mfpNumeric2.mType == Type.MFP_POSITIVE_INF)
                || (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF && mfpNumeric2.isActuallyPositive())
                || (mfpNumeric1.isActuallyPositive() && mfpNumeric2.mType == Type.MFP_NEGATIVE_INF))    {
            // -inf * any positive value is -inf. inf * any negative value is -inf.
            return MFPNumeric.NEG_INF;
        } else if (mfpNumeric1.mType != Type.MFP_DOUBLE_VALUE && mfpNumeric2.mType != Type.MFP_DOUBLE_VALUE)  {
            // value 1 and value 2 are integer or boolean. need not to worry about +-inf or nan because above cases have included them.
			if (mfpNumeric1.mlValue >= PRIMITIVE_LONG_MULTIPLY_INPUT_RANGE_MIN
				&& mfpNumeric1.mlValue <= PRIMITIVE_LONG_MULTIPLY_INPUT_RANGE_MAX
				&& mfpNumeric2.mlValue >= PRIMITIVE_LONG_MULTIPLY_INPUT_RANGE_MIN
				&& mfpNumeric2.mlValue <= PRIMITIVE_LONG_MULTIPLY_INPUT_RANGE_MAX)	{
				// inputs' absolute values are small enough, use primitive types to accelerate calculating.
				long lMultiplied = mfpNumeric1.mlValue * mfpNumeric2.mlValue;
				return new MFPNumeric(lMultiplied);
			} else	{
				BigInteger bigIntMultiplied = mfpNumeric1.mbigIntegerValue.multiply(mfpNumeric2.mbigIntegerValue);
				return new MFPNumeric(bigIntMultiplied);
			}
        } else  {
            // one of the value is double.
            BigDecimal bigDecMultiplied = mfpNumeric1.mbigDecimalValue.multiply(mfpNumeric2.mbigDecimalValue);
            bigDecMultiplied = bigDecMultiplied.setScale(THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP); // round, so that save cpu time in calculation.
            return new MFPNumeric(bigDecMultiplied);
        }
    }
    public MFPNumeric multiply(MFPNumeric mfpNumeric)    {
        return multiply(this, mfpNumeric);
    }
    
    // divide
    // use this function to avoid any rounding error or ArithmeticException:
    // “Non-terminating decimal expansion; no exact representable decimal result”.
    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        // do not use try/catch to save CPU time.
        BigDecimal bigdecReturn = a.divide(b, THE_MAX_ROUNDING_SCALE, BigDecimal.ROUND_HALF_UP);
        return bigdecReturn;
    }
    
    public static MFPNumeric divide(MFPNumeric mfpNumeric1, MFPNumeric mfpNumeric2)    {
        if (mfpNumeric1.mType == Type.MFP_NAN_VALUE || mfpNumeric2.mType == Type.MFP_NAN_VALUE
                || (mfpNumeric1.isActuallyZero() && mfpNumeric2.isActuallyZero())
                || (mfpNumeric1.isInf() && mfpNumeric2.isInf()))   {
            // nan / anything == nan, anything / nan == nan, inf / inf == nan, 0/0 == nan.
            return MFPNumeric.NAN;
        } else if ((mfpNumeric1.mType == Type.MFP_POSITIVE_INF && mfpNumeric2.isActuallyNonNegative())
                || (mfpNumeric1.isActuallyPositive() && mfpNumeric2.isActuallyZero())
                || (mfpNumeric1.mType == Type.MFP_NEGATIVE_INF && mfpNumeric2.isActuallyNegative()))    {
            //inf / any non-negative value is INF. -inf / any negative value is INF. any positive value / 0 is inf
            return MFPNumeric.INF;
        } else if ((mfpNumeric1.mType == Type.MFP_NEGATIVE_INF && mfpNumeric2.isActuallyNonNegative())
                || (mfpNumeric1.isActuallyNegative() && mfpNumeric2.isActuallyZero())
                || (mfpNumeric1.mType == Type.MFP_POSITIVE_INF && mfpNumeric2.isActuallyNegative()))    {
            //-inf / any non-negative value is -INF. inf / any negative value is -INF. any negative value / 0 is -inf
            return MFPNumeric.NEG_INF;
        } else if (mfpNumeric2.isInf()) {
            // anything except nan and inf divided by inf is 0.
            return MFPNumeric.ZERO;
        } else  {
            if (mfpNumeric1.mType != Type.MFP_DOUBLE_VALUE && mfpNumeric2.mType != Type.MFP_DOUBLE_VALUE
                    && mfpNumeric1.mlValue < Long.MAX_VALUE && mfpNumeric1.mlValue > Long.MIN_VALUE
                    && mfpNumeric2.mlValue < Long.MAX_VALUE && mfpNumeric2.mlValue > Long.MIN_VALUE)	{
				// value 1 and value 2 are integer or boolean. need not to worry about inf and NAN and divided by 0 cases because above cases
                // have included them. inputs' absolute long values are not overflowed value, use primitive types to accelerate calculating.
                // nuermic 2 cannot be zero because divided by 0 cases have been covered above.
				long lDivided = mfpNumeric1.mlValue / mfpNumeric2.mlValue;
                if (lDivided * mfpNumeric2.mlValue == mfpNumeric1.mlValue)  {
                    // ok, the result lDivided is what we should get.
                    return new MFPNumeric(lDivided);
                }
			}
            // We need not to worry about divided by 0 or INF or NaN because the above cases have coverted these situations.
            BigDecimal bigDecDivided = divide(mfpNumeric1.mbigDecimalValue, mfpNumeric2.mbigDecimalValue);
            return new MFPNumeric(bigDecDivided);
       }
    }
    public MFPNumeric divide(MFPNumeric mfpNumeric)    {
        return divide(this, mfpNumeric);
    }
    
    // power
    public static MFPNumeric pow(MFPNumeric a, MFPNumeric b)  {
        if (b.isActuallyZero()) {
            // any value ** 0 is 1.
            return MFPNumeric.ONE;
        } else if (a.mType == Type.MFP_NAN_VALUE || b.mType == Type.MFP_NAN_VALUE)  {
            //nan ** any value is nan except nan ** 0 == 1, but b==0 has been covered so no worries here.
            //any value ** nan is nan.
            return MFPNumeric.NAN;
        } else if (a.mType == Type.MFP_POSITIVE_INF)    {
            if (b.mType == Type.MFP_NAN_VALUE)  {
                return MFPNumeric.NAN;
            } else if (b.isActuallyPositive())  {
                return MFPNumeric.INF;
            } else {    //b is negative, b == 0 has been covered above.
                return MFPNumeric.ZERO;
            }
        } else if (a.mType == Type.MFP_NEGATIVE_INF)    {
             if (b.mType == Type.MFP_NAN_VALUE)  {
                return MFPNumeric.NAN;
            } else if (b.isActuallyPositive())  {
                if (b.isActuallyInteger())    {
                    BigInteger bigIntRounded = b.mbigDecimalValue.setScale(0, RoundingMode.HALF_UP).toBigInteger();
                    if (bigIntRounded.mod(new BigInteger("2")).compareTo(BigInteger.ONE) == 0)  {
                        // (-inf)**an odd integer is -inf
                        return MFPNumeric.NEG_INF;
                    } else {
                        // (-inf)**an even integer is inf
                        return MFPNumeric.INF;
                    }
                } else {
                    // (-inf)**a float value is nan.
                    return MFPNumeric.NAN;
                }
            } else {    //b is negative, b == 0 has been covered above.
                if (b.isActuallyInteger())    {
                    return MFPNumeric.ZERO;
                } else { // is not integer or is minus inf
                    return MFPNumeric.NAN;
                }
            }
        } else if (a.isActuallyNegative() && !b.isActuallyInteger())    {
            // a is negative and b is not integer, return nan
            return MFPNumeric.NAN;
        } else if (a.isActuallyZero()) {
            if (b.isActuallyPositive()) {
                return MFPNumeric.ZERO;
            } else if (b.isActuallyNegative()) {
                return MFPNumeric.INF;
            }
        }
        
        //now calculate if b == inf or -inf what's the value. b == nan has been covered above.
        //a must be normal value here because a == nan or a is infinite has been covered above.
        if (b.mType == Type.MFP_POSITIVE_INF)   {
            double dCompareAWith1 = compareTo(a, MFPNumeric.ONE);
            if (a.isActuallyNegative())  {
                return MFPNumeric.NAN;
            } else if (dCompareAWith1 < 0)    {
                return MFPNumeric.ZERO;
            } else if (dCompareAWith1 == 0) {
                return MFPNumeric.ONE;
            } else {
                return MFPNumeric.INF;
            }
        } else if (b.mType == Type.MFP_NEGATIVE_INF)   {
            double dCompareAWith1 = compareTo(a, MFPNumeric.ONE);
            if (a.isActuallyNegative())  {
                return MFPNumeric.NAN;
            } else if (dCompareAWith1 < 0)    {
                return MFPNumeric.INF;
            } else if (dCompareAWith1 == 0) {
                return MFPNumeric.ONE;
            } else {
                return MFPNumeric.ZERO;
            }
        }
        
        // now calculate normal value power.
        if (a.isActuallyInteger() && Math.abs(a.mdValue) == 1.0)  { // avoid to use compareTo(BigDecimal, BigDecimal to save CPU time.
            // if abs(a) == 1
            if (a.isPositive()) {
                // 1**any value is 1.
                return MFPNumeric.ONE;
            } else if (b.isActuallyInteger())   {
                BigInteger bigIntRounded = b.mbigDecimalValue.setScale(0, RoundingMode.HALF_UP).toBigInteger();
                if (bigIntRounded.mod(BigInteger.valueOf(2)).compareTo(BigInteger.ZERO) == 0)    {
                    // b is an even number
                    return MFPNumeric.ONE;
                } else {
                    return MFPNumeric.MINUS_ONE;
                }
            } else  {   // if b is not an integer.
                return MFPNumeric.NAN;
            }
        } else if ((a.mdValue <= POWER_BASE_TO_POSTIVE_ZERO_RESULT_THRESHOLD && b.mdValue >= POWER_BASE_TO_VALUE_ZERO_RESULT_THRESHOLD)
                || (a.mdValue >= POWER_BASE_TO_NEGATIVE_ZERO_RESULT_THRESHOLD && b.mdValue <= -POWER_BASE_TO_VALUE_ZERO_RESULT_THRESHOLD)) {
            // power to value is too large, while we believe the result should be so close to zero that cannot be accurately represented, so return zero directly
            return MFPNumeric.ZERO;
        } else {
            // first, we use primitive values to calculate abs(pow exponent) <= 8. This guarentee the error is less than 10**-15.
            double dAbsAValue = Math.abs(a.mdValue), dAbsBValue = Math.abs(b.mdValue);
            if (dAbsAValue <= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX && dAbsAValue >= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN
                    && dAbsBValue <= POWER_EXPONENT_REASONABLE_ACCU_POS_RANGE_MAX)   {
                // we use double to calculate power if exponent is less than 8 and base is between reasonable accuracy range
                // this guarantees that the calculated error is less than 10**-15.
                double dResult = Math.pow(a.mdValue, b.mdValue);
                double dAbsResult = Math.abs(dResult);
                if (dAbsResult <= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX && dAbsResult >= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN)   {
                    // the calculated result is accurate enough
                    return new MFPNumeric(dResult);
                }
            }
            
            // have to convert to a**(b_1+b_2+...b_m), if b_m is not integer, then calculate (a_1 * a_2 * ... a_n)**(int)b_m
            // and (a_1 * a_2 * ... a_n)**(remainder)b_m. a step should be PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX
            BigDecimal bigDecResult = BigDecimal.ONE;
            BigDecimal bigDecCurrentB = b.mbigDecimalValue;
            int nBStep = (int)POWER_EXPONENT_BIG_DECIMAL_CALCULATION_STEP;
            // first of all, calculate a**(b_1+b_2+...(int)b_m)
            while (bigDecCurrentB.abs().compareTo(BigDecimal.ONE) >= 0) {
                int nIntExponent = nBStep;
                if (bigDecCurrentB.abs().compareTo(new BigDecimal(nBStep)) < 0)   {
                    nIntExponent = (int)Math.abs(bigDecCurrentB.longValue());
                }
                BigDecimal bigDecComponent = a.mbigDecimalValue.pow(nIntExponent);
                if (b.isActuallyPositive()) {
                    bigDecResult = bigDecResult.multiply(bigDecComponent);
                    bigDecCurrentB = bigDecCurrentB.subtract(new BigDecimal(nIntExponent));
                } else {    // b is negative. b is actually zero has been covered above.
                    bigDecResult = divide(bigDecResult, bigDecComponent);
                    bigDecCurrentB = bigDecCurrentB.add(new BigDecimal(nIntExponent));
                }
            }
            // now bigDecCurrentB is in (-1, 1)
            if (compareTo(bigDecCurrentB, BigDecimal.ZERO, 1) != 0)    {   // bigDecCurrentB is not actually 0
                long nAStep = (long)PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX;
                // calculate (a_1 * a_2 * ... a_n)**(remainder)b_m. Need not to worry about negative a because
                // if a is negative, b must be integer which has been covered above
                BigDecimal bigDecCurrentA = a.mbigDecimalValue;
                double dCurrentB = bigDecCurrentB.doubleValue();
                while (true)   { // new BigDecimal(1.0/nAStep) may not be accurate.
                    if (bigDecCurrentA.compareTo(new BigDecimal(nAStep)) >= 0)   {
                        double dAStepToCurrentB = Math.pow((double)nAStep, dCurrentB);
                        bigDecResult = bigDecResult.multiply(new BigDecimal(dAStepToCurrentB)); // do not use valueOf(double) so that more accurate
                        bigDecCurrentA = divide(bigDecCurrentA, new BigDecimal(nAStep));
                    } else if (bigDecCurrentA.compareTo(new BigDecimal(1.0/nAStep)) <= 0) {
                        double dAStepToCurrentB = Math.pow((double)nAStep, dCurrentB);
                        bigDecResult = divide(bigDecResult, new BigDecimal(dAStepToCurrentB));
                        bigDecCurrentA = bigDecCurrentA.multiply(new BigDecimal(nAStep));
                    } else {    // bigDecCurrentA is between nAStep and 1.0/nAStep
                        double dCurrentAToCurrentB = Math.pow(bigDecCurrentA.doubleValue(), dCurrentB);
                        bigDecResult = bigDecResult.multiply(new BigDecimal(dCurrentAToCurrentB));
                        break;
                    }
                }
            }
            return new MFPNumeric(bigDecResult);
        }
    }
    
    // sqrt
    public static MFPNumeric sqrt(MFPNumeric a) {
        if (a.mType == Type.MFP_NAN_VALUE || a.isActuallyNegative())    {
            return MFPNumeric.NAN;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.ZERO;
        } else if (a.mType == Type.MFP_POSITIVE_INF)    {
            return MFPNumeric.INF;
        } else if (a.mdValue <= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX && a.mdValue >= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN) {
            // use double to calculate, same accuracy, higher speed.
            return new MFPNumeric(Math.sqrt(a.mdValue));
        } else  {
            // calculate big decimal. Do not use double to calculate because sqrt(1.0000000000000000000019) may not be accurate.
            BigDecimal bigDecResult = BigDecimal.ONE;
            BigDecimal bigDecCurrentA = a.mbigDecimalValue;
            long nAStep = (long)PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX;
            while (true)    {
                if (bigDecCurrentA.compareTo(new BigDecimal(nAStep)) >= 0)   {
                    double dSqrtAStep = Math.sqrt(nAStep);
                    bigDecResult = bigDecResult.multiply(new BigDecimal(dSqrtAStep));
                    bigDecCurrentA = divide(bigDecCurrentA, new BigDecimal(nAStep));
                } else if (bigDecCurrentA.compareTo(new BigDecimal(1.0/nAStep)) <= 0)   {
                    double dSqrtAStep = Math.sqrt(nAStep);
                    bigDecResult = divide(bigDecResult, new BigDecimal(dSqrtAStep));
                    bigDecCurrentA = bigDecCurrentA.multiply(new BigDecimal(nAStep));
                } else {    // bigDecCurrentA is between (1.0/nAStep, nAStep)
                    double dSqrtCurrentA = Math.sqrt(bigDecCurrentA.doubleValue());
                    bigDecResult = bigDecResult.multiply(new BigDecimal(dSqrtCurrentA));
                    break;
                }
            }
            return new MFPNumeric(bigDecResult);
        }
    }
    
    // hypot
    public static MFPNumeric hypot(MFPNumeric x, MFPNumeric y) {
        if (x.mType == Type.MFP_NAN_VALUE || y.mType == Type.MFP_NAN_VALUE)    {
            return MFPNumeric.NAN;
        } else if (x.isInf() || y.isInf())  {
            return MFPNumeric.INF;
        } else if (x.isActuallyZero() && y.isActuallyZero())  {
            return MFPNumeric.ZERO;
        } else if (x.isActuallyZero())  {
            return abs(y);
        } else if (y.isActuallyZero())  {
            return abs(x);
        } else  {   // now x, y are both non-zero, non-nan and non-inf
            MFPNumeric mfpNumAbsX = abs(x), mfpNumAbsY = abs(y);
            BigDecimal bigDecXYRatio = BigDecimal.ZERO;
            BigDecimal bigDecXYRatioSqr = BigDecimal.ZERO;
            BigDecimal bigDecBase = BigDecimal.ONE;
            if (mfpNumAbsX.compareTo(mfpNumAbsY) < 0) {
                bigDecXYRatio = divide(mfpNumAbsX.mbigDecimalValue, mfpNumAbsY.mbigDecimalValue);
                bigDecBase = mfpNumAbsY.mbigDecimalValue;
            } else {
                bigDecXYRatio = divide(mfpNumAbsY.mbigDecimalValue, mfpNumAbsX.mbigDecimalValue);
                bigDecBase = mfpNumAbsX.mbigDecimalValue;
            }
            bigDecXYRatioSqr = bigDecXYRatio.multiply(bigDecXYRatio);
            
            // now bigDecXYRatioSqr should be a value <= 1.
            if (bigDecXYRatioSqr.compareTo(new BigDecimal(PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN)) < 0) {
                // bigDecXYRatioSqr is too small, so return abs(a)*(1+(x/y)**2/2), this is more accurate than calculate double
                // Math.sqrt(1 + (x/y)**2)
                BigDecimal bigDecReturn = bigDecBase.multiply(BigDecimal.ONE.add(divide(bigDecXYRatioSqr, new BigDecimal("2"))));
                return new MFPNumeric(bigDecReturn);
            } else {
                BigDecimal bigDecReturn = bigDecBase.multiply(new BigDecimal(Math.sqrt(1.0 + bigDecXYRatioSqr.doubleValue())));
                return new MFPNumeric(bigDecReturn);
            }
        }
    }
    
    // cbrt
    public static MFPNumeric cbrt(MFPNumeric a) {
        if (a.mType == Type.MFP_NAN_VALUE)    {
            return MFPNumeric.NAN;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.ZERO;
        } else if (a.isInf())    {
            return a;   // cube root of inf is inf, cube root of -inf is -inf
        } else if (Math.abs(a.mdValue) <= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX
                && Math.abs(a.mdValue) >= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN) {
            // use double to calculate, same accuracy, higher speed.
            return new MFPNumeric(Math.cbrt(a.mdValue));
        } else  {
            // calculate big decimal. Do not use double to calculate because cbrt(1.0000000000000000000019) may not be accurate.
            BigDecimal bigDecSig = a.isActuallyNegative()?BigDecimal.valueOf(-1):BigDecimal.ONE;
            BigDecimal bigDecAbsA = a.mbigDecimalValue.abs();
            BigDecimal bigDecResult = BigDecimal.ONE;
            BigDecimal bigDecCurrentAbsA = bigDecAbsA;
            long nAStep = (long)PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX;
            while (true)    {
                if (bigDecCurrentAbsA.compareTo(new BigDecimal(nAStep)) >= 0)   {
                    double dCbrtAStep = Math.cbrt(nAStep);
                    bigDecResult = bigDecResult.multiply(new BigDecimal(dCbrtAStep));
                    bigDecCurrentAbsA = divide(bigDecCurrentAbsA, new BigDecimal(nAStep));
                } else if (bigDecCurrentAbsA.compareTo(new BigDecimal(1.0/nAStep)) <= 0)   {
                    double dCbrtAStep = Math.cbrt(nAStep);
                    bigDecResult = divide(bigDecResult, new BigDecimal(dCbrtAStep));
                    bigDecCurrentAbsA = bigDecCurrentAbsA.multiply(new BigDecimal(nAStep));
                } else {    // bigDecCurrentAbsA is between (1.0/nAStep, nAStep)
                    double dCbrtCurrentAbsA = Math.cbrt(bigDecCurrentAbsA.doubleValue());
                    bigDecResult = bigDecResult.multiply(new BigDecimal(dCbrtCurrentAbsA));
                    break;
                }
            }
            return new MFPNumeric(bigDecSig.multiply(bigDecResult));
        }
    }
        
    // exp
    public static MFPNumeric exp(MFPNumeric a) {
        if (a.mType == Type.MFP_NAN_VALUE)    {
            return MFPNumeric.NAN;
        } else if (a.mType == Type.MFP_POSITIVE_INF)    {
            return MFPNumeric.INF;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.ONE;
        } else if (a.mType == Type.MFP_NEGATIVE_INF)    {
            return MFPNumeric.ZERO;
        } else if (Math.abs(a.mdValue) <= POWER_EXPONENT_REASONABLE_ACCU_POS_RANGE_MAX) {
            // use double to calculate, much quicker, and error should be less than 10**-15
            return new MFPNumeric(Math.exp(a.mdValue));
        } else if (a.mdValue <= EXP_TO_VALUE_RESULT_ZERO_THRESH) {
            // a value is so small that result will be calculated as zero.
            return MFPNumeric.ZERO;
        } else {
            // use bigDecimal.
            BigDecimal bigDecResult = BigDecimal.ONE;
            BigDecimal bigDecCurrentA = a.mbigDecimalValue;
            int nAStep = (int)POWER_EXPONENT_BIG_DECIMAL_CALCULATION_STEP;
            // first of all, calculate a**(b_1+b_2+...(int)b_m)
            while (bigDecCurrentA.abs().compareTo(BigDecimal.ONE) >= 0) {
                int nIntExponent = nAStep;
                if (bigDecCurrentA.abs().compareTo(new BigDecimal(nAStep)) < 0)   {
                    nIntExponent = (int)Math.abs(bigDecCurrentA.longValue());
                }
                BigDecimal bigDecComponent = MFPNumeric.E.mbigDecimalValue.pow(nIntExponent);
                if (a.isActuallyPositive()) {
                    bigDecResult = bigDecResult.multiply(bigDecComponent);
                    bigDecCurrentA = bigDecCurrentA.subtract(new BigDecimal(nIntExponent));
                } else {    // a is negative. a is actually zero has been covered above.
                    bigDecResult = divide(bigDecResult, bigDecComponent);
                    bigDecCurrentA = bigDecCurrentA.add(new BigDecimal(nIntExponent));
                }
            }
            
            // current A is between (-1, 1) and is not zero.
            if (compareTo(bigDecCurrentA, BigDecimal.ZERO, 1) != 0)    {   // bigDecCurrentA is not actually 0
                bigDecResult = bigDecResult.multiply(new BigDecimal(Math.exp(bigDecCurrentA.doubleValue())));
            }
            return new MFPNumeric(bigDecResult);
        }    
    }
    
    // log
    public static MFPNumeric log(MFPNumeric a)  {
        if (a.mType == Type.MFP_NAN_VALUE || a.isActuallyNegative())    {
            return MFPNumeric.NAN;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.NEG_INF;
        } else if (a.mType == Type.MFP_POSITIVE_INF)    {
            return MFPNumeric.INF;
        } else if (a.mdValue >= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN && a.mdValue <= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX)  {
            // a is normal positive value. using primitive calculation.
            // still use primitive types to do calculation here. Otherwise, it is too complicated.
            return new MFPNumeric(Math.log(a.mdValue)); // do not use logp1 seems log is more accurate.
        } else {
            // have to use big decimal.
            BigDecimal bigDecResult = BigDecimal.ZERO;
            BigDecimal bigDecCurrentA = a.mbigDecimalValue;
            int nALogStep = (int)(Math.log(PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX)/2.0);
            BigDecimal bigDecAStep = MFPNumeric.E.mbigDecimalValue.pow(nALogStep);
            BigDecimal bigDecOneOverAStep = divide(BigDecimal.ONE, bigDecAStep);
            while (true)    {
                if (bigDecCurrentA.compareTo(bigDecAStep) >= 0)   {
                    bigDecResult = bigDecResult.add(new BigDecimal(nALogStep));
                    bigDecCurrentA = divide(bigDecCurrentA, bigDecAStep);
                } else if (bigDecCurrentA.compareTo(bigDecOneOverAStep) <= 0)   {
                    bigDecResult = bigDecResult.subtract(new BigDecimal(nALogStep));
                    bigDecCurrentA = bigDecCurrentA.multiply(bigDecAStep);
                } else {
                    bigDecResult = bigDecResult.add(new BigDecimal(Math.log(bigDecCurrentA.doubleValue())));
                    break;
                }
            }
            return new MFPNumeric(bigDecResult);
        } 
    }
    
    // log10
    public static MFPNumeric log10(MFPNumeric a)  {
        if (a.mType == Type.MFP_NAN_VALUE || a.isActuallyNegative())    {
            return MFPNumeric.NAN;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.NEG_INF;
        } else if (a.mType == Type.MFP_POSITIVE_INF)    {
            return MFPNumeric.INF;
        } else if (a.mdValue >= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN && a.mdValue <= PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX)  {
            // a is normal positive value. using primitive calculation.
            // still use primitive types to do calculation here. Otherwise, it is too complicated.
            return new MFPNumeric(Math.log10(a.mdValue));
        } else {
            // have to use big decimal.
            BigDecimal bigDecResult = BigDecimal.ZERO;
            BigDecimal bigDecCurrentA = a.mbigDecimalValue;
            int nALogStep = (int)(Math.log10(PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MAX)/2.0);
            BigDecimal bigDecAStep = MFPNumeric.TEN.mbigDecimalValue.pow(nALogStep);
            BigDecimal bigDecOneOverAStep = divide(BigDecimal.ONE, bigDecAStep);
            while (true)    {
                if (bigDecCurrentA.compareTo(bigDecAStep) >= 0)   {
                    bigDecResult = bigDecResult.add(new BigDecimal(nALogStep));
                    bigDecCurrentA = divide(bigDecCurrentA, bigDecAStep);
                } else if (bigDecCurrentA.compareTo(bigDecOneOverAStep) <= 0)   {
                    bigDecResult = bigDecResult.subtract(new BigDecimal(nALogStep));
                    bigDecCurrentA = bigDecCurrentA.multiply(bigDecAStep);
                } else {
                    bigDecResult = bigDecResult.add(new BigDecimal(Math.log10(bigDecCurrentA.doubleValue())));
                    break;
                }
            }
            return new MFPNumeric(bigDecResult);
        } 
    }

    // sin
    public static MFPNumeric sin(MFPNumeric a)  {
        if (a.isNanOrInf()) {
            return MFPNumeric.NAN;
        } else if (Math.abs(a.mdValue) <= MFPNumeric.TWO_PI.mdValue)    {  
            // between -2pi and 2pi we use double to calculate. We have to use double tan anyway because bigdecimal does not have cos function
            double dAbsA = Math.abs(a.mdValue);
            double dASigNum = Math.signum(a.mdValue);
            if (dAbsA == MFPNumeric.TWO_PI.mdValue || dAbsA == MFPNumeric.PI.mdValue || a.isActuallyZero())    {
                // 2pi, pi or 0
                return MFPNumeric.ZERO;
            } else if (dAbsA == MFPNumeric.PI_OVER_TWO.mdValue)  {
                // pi/2
                return (dASigNum > 0)?MFPNumeric.ONE:MFPNumeric.MINUS_ONE;
            } else if (dAbsA == (MFPNumeric.PI_OVER_TWO.mdValue + MFPNumeric.PI.mdValue))   {
                // pi * 1.5
                return (dASigNum < 0)?MFPNumeric.ONE:MFPNumeric.MINUS_ONE;
            } else if (dAbsA == MFPNumeric.PI_OVER_SIX.mdValue || dAbsA == (MFPNumeric.PI.mdValue - MFPNumeric.PI_OVER_SIX.mdValue))    {
                // pi or 5*pi/6
                return new MFPNumeric(dASigNum * 0.5);
            } else if (dAbsA == (MFPNumeric.PI.mdValue + MFPNumeric.PI_OVER_SIX.mdValue)
                    || dAbsA == (MFPNumeric.TWO_PI.mdValue - MFPNumeric.PI_OVER_SIX.mdValue))   {
                // 7*pi/6 or 11*pi/6
                return new MFPNumeric(dASigNum * (-0.5));
            } else {
                return new MFPNumeric(Math.sin(a.mdValue));
            }
        } else {
            // use bigDecimal, do not consider special angle like pi, pi/2 to save computing time.
            BigDecimal[] bigDecArray = a.mbigDecimalValue.divideAndRemainder(MFPNumeric.TWO_PI.mbigDecimalValue);
            BigDecimal bigDecAngle = bigDecArray[1];
            return new MFPNumeric(Math.sin(bigDecAngle.doubleValue()));
        }
    }

    // cos
    public static MFPNumeric cos(MFPNumeric a)  {
        if (a.isNanOrInf()) {
            return MFPNumeric.NAN;
        } else if (Math.abs(a.mdValue) <= MFPNumeric.TWO_PI.mdValue)    {  
            // between -2pi and 2pi we use double to calculate. We have to use double cos anyway because bigdecimal does not have cos function
            double dAbsA = Math.abs(a.mdValue);
            if (dAbsA == MFPNumeric.TWO_PI.mdValue || a.isActuallyZero())    {
                // 2pi or 0
                return MFPNumeric.ONE;
            } else if (dAbsA == MFPNumeric.PI.mdValue)  {
                // pi
                return MFPNumeric.MINUS_ONE;
            } else if (dAbsA == MFPNumeric.PI_OVER_TWO.mdValue || dAbsA == (MFPNumeric.PI_OVER_TWO.mdValue + MFPNumeric.PI.mdValue))  {
                // pi/2 or pi * 1.5
                return MFPNumeric.ZERO;
            } else if (dAbsA == MFPNumeric.PI_OVER_THREE.mdValue || dAbsA == (MFPNumeric.TWO_PI.mdValue - MFPNumeric.PI_OVER_THREE.mdValue))    {
                // pi/3 or 5*pi/3
                return MFPNumeric.HALF;
            } else if (dAbsA == (MFPNumeric.PI.mdValue - MFPNumeric.PI_OVER_THREE.mdValue)
                    || dAbsA == (MFPNumeric.PI.mdValue + MFPNumeric.PI_OVER_THREE.mdValue))   {
                // 2*pi/3 or 4*pi/3
                return MFPNumeric.HALF.negate();
            } else {
                return new MFPNumeric(Math.cos(a.mdValue));
            }
        } else {
            // use bigDecimal, do not consider special angle like pi, pi/2 to save computing time.
            BigDecimal[] bigDecArray = a.mbigDecimalValue.divideAndRemainder(MFPNumeric.TWO_PI.mbigDecimalValue);
            BigDecimal bigDecAngle = bigDecArray[1];
            return new MFPNumeric(Math.cos(bigDecAngle.doubleValue()));
        }
    }

    // tan
    public static MFPNumeric tan(MFPNumeric a)  {
        if (a.isNanOrInf()) {
            return MFPNumeric.NAN;
        } else if (Math.abs(a.mdValue) <= MFPNumeric.TWO_PI.mdValue)    {  
            // between -2pi and 2pi we use double to calculate. We have to use double tan anyway because bigdecimal does not have tan function
            double dAbsA = Math.abs(a.mdValue);
            double dASigNum = Math.signum(a.mdValue);
            if (dAbsA == MFPNumeric.TWO_PI.mdValue || dAbsA == MFPNumeric.PI.mdValue || a.isActuallyZero())    {
                // 2pi, pi or 0
                return MFPNumeric.ZERO;
            } else if (dAbsA == MFPNumeric.PI_OVER_TWO.mdValue)  {
                // pi/2
                return (dASigNum > 0)?MFPNumeric.INF:MFPNumeric.NEG_INF;
            } else if (dAbsA == (MFPNumeric.PI_OVER_TWO.mdValue + MFPNumeric.PI.mdValue))   {
                // pi * 1.5
                return (dASigNum < 0)?MFPNumeric.INF:MFPNumeric.NEG_INF;
            } else if (dAbsA == MFPNumeric.PI_OVER_FOUR.mdValue || dAbsA == (MFPNumeric.PI.mdValue + MFPNumeric.PI_OVER_FOUR.mdValue))    {
                // pi/4 or 5*pi/4
                return (dASigNum > 0)?MFPNumeric.ONE:MFPNumeric.MINUS_ONE;
            } else if (dAbsA == (MFPNumeric.PI.mdValue - MFPNumeric.PI_OVER_FOUR.mdValue)
                    || dAbsA == (MFPNumeric.TWO_PI.mdValue - MFPNumeric.PI_OVER_FOUR.mdValue))   {
                // 3*pi/4 or 7*pi/4
                return (dASigNum < 0)?MFPNumeric.ONE:MFPNumeric.MINUS_ONE;
            } else {
                return new MFPNumeric(Math.tan(a.mdValue));
            }
        } else {
            // use bigDecimal, do not consider special angle like pi, pi/2 to save computing time.
            BigDecimal[] bigDecArray = a.mbigDecimalValue.divideAndRemainder(MFPNumeric.TWO_PI.mbigDecimalValue);
            BigDecimal bigDecAngle = bigDecArray[1];
            if (bigDecAngle.compareTo(BigDecimal.ZERO) < 0) {
                bigDecAngle = bigDecAngle.add(MFPNumeric.TWO_PI.mbigDecimalValue);
            }
            // now angle is from 0 to 2pi
            if (compareTo(bigDecAngle, MFPNumeric.PI_OVER_TWO.mbigDecimalValue, 1) == 0)    {
                // tan(pi/2) = inf
                return MFPNumeric.INF;
            } else if (compareTo(bigDecAngle, add(MFPNumeric.PI, MFPNumeric.PI_OVER_TWO).mbigDecimalValue, 1) == 0)    {
                // tan(pi/2 * 3) = -inf
                return MFPNumeric.NEG_INF;
            } else {
                return new MFPNumeric(Math.tan(bigDecAngle.doubleValue()));
            }
        }
    }
    
    // arcsin
    public static MFPNumeric asin(MFPNumeric a) {
        double dCompare1 = compareTo(a, MFPNumeric.ONE), dCompareMinus1 = compareTo(a, MFPNumeric.MINUS_ONE);
        if (dCompare1 < 0 && dCompareMinus1 > 0) {
            return new MFPNumeric(Math.asin(a.mdValue));
        } else if (dCompare1 == 0)  {
            return MFPNumeric.PI_OVER_TWO;
        } else if (dCompareMinus1 == 0) {
            return MFPNumeric.PI_OVER_TWO.negate();
        } else {
            return MFPNumeric.NAN;
        }
    }
    
    // arccos
    public static MFPNumeric acos(MFPNumeric a) {
        double dCompare1 = compareTo(a, MFPNumeric.ONE), dCompareMinus1 = compareTo(a, MFPNumeric.MINUS_ONE);
        if (dCompare1 < 0 && dCompareMinus1 > 0) {
            return new MFPNumeric(Math.acos(a.mdValue));
        } else if (dCompare1 == 0)  {
            return MFPNumeric.ZERO;
        } else if (dCompareMinus1 == 0) {
            return MFPNumeric.PI;
        } else {
            return MFPNumeric.NAN;
        }
    }
    
    // arctan
    public static MFPNumeric atan(MFPNumeric a) {
        if (a.mType == Type.MFP_NAN_VALUE) {
            return MFPNumeric.NAN;
        } else if (a.mType == Type.MFP_POSITIVE_INF)    {
            return MFPNumeric.PI_OVER_TWO;
        } else if (a.mType == Type.MFP_NEGATIVE_INF)    {
            return MFPNumeric.PI_OVER_TWO.negate();
        } else if (a.isActuallyZero())  {
            return MFPNumeric.ZERO;
        } else if (a.mdValue < PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN && a.mdValue > -PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN) {
            // if range of a is very small, calculate by taylor series, i.e. a - a**3/3 + a**5/5 + O(x**6)
            // use big decimal here.
            BigDecimal bigDecResult = a.mbigDecimalValue;
            BigDecimal bigDecATo2 = a.mbigDecimalValue.multiply(a.mbigDecimalValue);
            BigDecimal bigDecATo3 = a.mbigDecimalValue.multiply(bigDecATo2);
            BigDecimal bigDecATo5 = bigDecATo3.multiply(bigDecATo2);
            bigDecResult = bigDecResult.subtract(divide(bigDecATo3, BigDecimal.valueOf(3))).add(divide(bigDecATo5, BigDecimal.valueOf(5)));
            return new MFPNumeric(bigDecResult);
        } else {
            return new MFPNumeric(Math.atan(a.mdValue));    // don't worry about very big value because atan(a) in that case should be pi/2 or -pi/2
        }
    }
    
    // atan(y,x)
    public static MFPNumeric atan2(MFPNumeric y, MFPNumeric x)  {
        if (y.mType == Type.MFP_NAN_VALUE || x.mType == Type.MFP_NAN_VALUE) {
            return MFPNumeric.NAN;
        } else if (y.isActuallyZero() && x.isActuallyZero()) {
            return MFPNumeric.ZERO;
        } else if ((y.isActuallyZero() && y.isNonNegative() && x.isPositive()) // x cannot be actually zero.
                || (y.isNonNegative() && y.mType != Type.MFP_POSITIVE_INF && x.mType == Type.MFP_POSITIVE_INF))   {
            return MFPNumeric.ZERO;
        } else if ((y.isActuallyZero() && y.isNegative() && x.isPositive()) // x cannot be actually zero.
                || (y.isNegative() && y.mType != Type.MFP_NEGATIVE_INF && x.mType == Type.MFP_POSITIVE_INF))   {
            return MFPNumeric.ZERO;
        } else if ((y.isActuallyZero() && y.isNonNegative() && x.isNegative()) // x cannot be actually zero.
                || (y.isNonNegative() && y.mType != Type.MFP_POSITIVE_INF && x.mType == Type.MFP_NEGATIVE_INF))   {
            return MFPNumeric.PI;
        } else if ((y.isActuallyZero() && y.isNegative() && x.isNegative()) // x cannot be actually zero.
                || (y.isNegative() && y.mType != Type.MFP_NEGATIVE_INF && x.mType == Type.MFP_NEGATIVE_INF))   {
            return MFPNumeric.PI.negate();
        } else if ((y.isActuallyPositive() && x.isActuallyZero()) || (y.mType== Type.MFP_POSITIVE_INF && !x.isInf()))   {
            return MFPNumeric.PI_OVER_TWO;
        } else if ((y.isActuallyNegative() && x.isActuallyZero()) || (y.mType== Type.MFP_NEGATIVE_INF && !x.isInf()))   {
            return MFPNumeric.PI_OVER_TWO.negate();
        } else if (y.mType == Type.MFP_POSITIVE_INF && x.mType == Type.MFP_POSITIVE_INF)    {
            return MFPNumeric.PI_OVER_FOUR;
        } else if (y.mType == Type.MFP_POSITIVE_INF && x.mType == Type.MFP_NEGATIVE_INF)    {
            return multiply(MFPNumeric.PI, new MFPNumeric(0.75));
        } else if (y.mType == Type.MFP_NEGATIVE_INF && x.mType == Type.MFP_POSITIVE_INF)    {
            return multiply(MFPNumeric.PI, new MFPNumeric(-0.25));
        } else if (y.mType == Type.MFP_NEGATIVE_INF && x.mType == Type.MFP_NEGATIVE_INF)    {
            return multiply(MFPNumeric.PI, new MFPNumeric(-0.75));
        } else {
            BigDecimal bigDecYOverX = divide(y.mbigDecimalValue, x.mbigDecimalValue);
            double dYOverX = bigDecYOverX.doubleValue();
            MFPNumeric mfpNumAtanResult;
            if (dYOverX < PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN && dYOverX > -PRIMITIVE_DOUBLE_REASONABLE_ACCU_POS_RANGE_MIN) {
                // if range of y/x is very small, calculate by taylor series, i.e. (y/x) - (y/x)**3/3 + (y/x)**5/5 + O((y/x)**6)
                // use big decimal here.
                BigDecimal bigDecResult = bigDecYOverX;
                BigDecimal bigDecYOverXTo2 = bigDecYOverX.multiply(bigDecYOverX);
                BigDecimal bigDecYOverXTo3 = bigDecYOverX.multiply(bigDecYOverXTo2);
                BigDecimal bigDecYOverXTo5 = bigDecYOverXTo3.multiply(bigDecYOverXTo2);
                bigDecResult = bigDecResult.subtract(divide(bigDecYOverXTo3, BigDecimal.valueOf(3))).add(divide(bigDecYOverXTo5, BigDecimal.valueOf(5)));
                mfpNumAtanResult = new MFPNumeric(bigDecResult);
            } else {
                mfpNumAtanResult = new MFPNumeric(Math.atan(dYOverX));    // don't worry about very big value because atan(y/x) in that case should be pi/2 or -pi/2
            }
            if (x.isPositive()) {
                return mfpNumAtanResult;
            } else if (y.isPositive()) {    // x must be negative, need not to worry about 0 because it has been covered above.
                return add(mfpNumAtanResult, MFPNumeric.PI);
            } else {    // x is negative and y is negative, need not to worry about 0.
                return subtract(mfpNumAtanResult, MFPNumeric.PI);
            }
        }
    }
    
    //toRadians
    public static MFPNumeric toRadians(MFPNumeric a)   {
        if (a.mType == Type.MFP_NAN_VALUE || a.isInf()) {
            return a;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.ZERO;
        } else  {
            // always use big decimal otherwise rounding error will be too large
            // to think that double value 1.00000000000000000000001 is actually 1.
            BigDecimal bigDecResult = divide(a.mbigDecimalValue.multiply(MFPNumeric.PI.mbigDecimalValue), new BigDecimal("180"));
            return new MFPNumeric(bigDecResult);
        }
    }
    
    //toDegrees
    public static MFPNumeric toDegrees(MFPNumeric a)   {
        if (a.mType == Type.MFP_NAN_VALUE || a.isInf()) {
            return a;
        } else if (a.isActuallyZero())  {
            return MFPNumeric.ZERO;
        } else  {
            // always use big decimal otherwise rounding error will be too large
            // to think that double value 1.00000000000000000000001 is actually 1.
            BigDecimal bigDecResult = divide(a.mbigDecimalValue.multiply(new BigDecimal("180")), MFPNumeric.PI.mbigDecimalValue);
            return new MFPNumeric(bigDecResult);
        }
    }
    
    public MFPNumeric setScale(int nNewScale, int roundingMode) {
        if (mType != Type.MFP_DOUBLE_VALUE) {
            return this;    // if nan, inf, integer or boolean, need no rounding.
        }
        if (nNewScale < 0)  {
            nNewScale = 0;
        }
        BigDecimal bigDecRounded = mbigDecimalValue.setScale(nNewScale, roundingMode);
        return new MFPNumeric(bigDecRounded);
    }
}
