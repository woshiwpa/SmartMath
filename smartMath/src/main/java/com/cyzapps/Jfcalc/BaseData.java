package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.ErrProcessor.*;
import com.cyzapps.Jsma.AEConst;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.Jsma.AbstractExpr;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

public class BaseData {
    
    /*     cursor position class. cursor means where we are currently analyzing
     *  This class is used to wrap an int position variable so that we can use
     *  reference parameter. */
    public static class CurPos 
    {
        public int m_nPos;
    }
    
    public static final MFPNumeric THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION = new MFPNumeric(5.0e-24);
    
    public static final int MAX_REPEATING_VALIDATION_CNT_ALLOWED = 32;  // if a data reference is validated more than 32 times, it implies a recursive validation error.

    public static final MFPNumeric THE_NULL_DATA_VALUE = MFPNumeric.ZERO;

    public static enum DATATYPES
    {
        DATUM_NULL,
        DATUM_DOUBLE,   // nan and inf are not independent types because double can be nan or inf.
        DATUM_INTEGER,  // nan and inf are not independent types because int can be nan or inf.
        DATUM_BOOLEAN,  // boolean can be inf too.
        DATUM_STRING,    // a string
        DATUM_REF_DATA,    // a reference to a list of data
        DATUM_COMPLEX,    // complex number
        DATUM_REF_FUNC,    // a reference to a function
        DATUM_ABSTRACT_EXPR,    // an abstract expr
    }

    public static class DCUncopibleFeatures
    {
        public int mnValidatedCnt = 0;
    }
    
    public static class DataClass
    {        
        private DATATYPES menumDataType = DATATYPES.DATUM_NULL;   
        private MFPNumeric mmfpNumDataValue = THE_NULL_DATA_VALUE;   // this is for non-exist, double, integer and boolean, nan, inf
        private String mstrValue = "";    // this is for string type
        private String mstrFunctionName = "";    // this is for ref_func
        private DataClass[] mdataList = new DataClass[0];    // this is for ref_data
        private String mstrUsrDefType = "";    // this is for user to define their own type
        private AbstractExpr maexpr = AEInvalid.AEINVALID;  // this is for expression type data class.
        
        public DCUncopibleFeatures mdcUncopibleFeature = new DCUncopibleFeatures();
        
        public DataClass()    {
        }    

        public DataClass(DataClass datumSrc) throws JFCALCExpErrException {
            // initial data class by deep copy it from another dataclass
            copyTypeValueDeep(datumSrc);
        }
        
        public DataClass(DATATYPES enumDataType, MFPNumeric mfpNumDataValue) throws JFCALCExpErrException    {
            // initialize numerical data class
            if (enumDataType != DATATYPES.DATUM_NULL && enumDataType != DATATYPES.DATUM_BOOLEAN
                    && enumDataType != DATATYPES.DATUM_INTEGER && enumDataType != DATATYPES.DATUM_DOUBLE)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            }
            setDataClass(enumDataType, mfpNumDataValue, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
        }

        public DataClass(DATATYPES enumDataType, String str) throws JFCALCExpErrException    {
            // initialize function or string data class
            if (enumDataType != DATATYPES.DATUM_STRING && enumDataType != DATATYPES.DATUM_REF_FUNC)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            }
            setDataClass(enumDataType, null, str, "", new DataClass[0], AEInvalid.AEINVALID, "");
        }

        public DataClass(DataClass[] dataList) throws JFCALCExpErrException    {
            // initialize function or string data class
            if (dataList == null)    {
                dataList = new DataClass[0];
            }
            setDataClass(DATATYPES.DATUM_REF_DATA, null, "", "", dataList, AEInvalid.AEINVALID, "");
        }

        public DataClass(AbstractExpr aexpr) throws JFCALCExpErrException {
            setDataClass(DATATYPES.DATUM_ABSTRACT_EXPR, null, "", "", new DataClass[0], aexpr, "");
        }
        
        public final void setDataClass(DATATYPES enumDataType, MFPNumeric mfpNumDataValue, String strValue, 
                  String strFuncName, DataClass[] dataList, AbstractExpr aexpr, String strUsrDefType) throws JFCALCExpErrException    {
            DATATYPES enumDataTypeOld = menumDataType;
            MFPNumeric mfpNumDataValueOld = mmfpNumDataValue;
            String strValueOld = mstrValue;
            String strFunctionNameOld = mstrFunctionName;
            DataClass[] dataListOld = mdataList;
            AbstractExpr aexprOld = maexpr;
            String strUsrDefTypeOld = mstrUsrDefType;            
            menumDataType = enumDataType;
            mmfpNumDataValue = (mfpNumDataValue == null)?new MFPNumeric(0):mfpNumDataValue;
            mstrValue = strValue;
            mstrFunctionName = strFuncName;
            mdataList = dataList;
            maexpr = aexpr;
            mstrUsrDefType = strUsrDefType;
            //basic validation work done here, try to avoid any call of ChangeDataType
            try {
                validateDataClass();
            } catch (JFCALCExpErrException e) {
                // if fail to set, restore, otherwise, incorrect dataclass will cause trouble later on.
                // only restore when set data list cause recursive referring may cause stack overflow.
                menumDataType = enumDataTypeOld;
                mmfpNumDataValue = mfpNumDataValueOld;
                mstrValue = strValueOld;
                mstrFunctionName = strFunctionNameOld;
                mdataList = dataListOld;
                maexpr = aexprOld;
                mstrUsrDefType = strUsrDefTypeOld;    
                validateDataClass_Step2();  // reset nvalidatedcnt.
                throw e;
            }
        }
        
        public boolean isSingleBoolean()    {
            if ((menumDataType == DATATYPES.DATUM_BOOLEAN
                    || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE)
                && (mmfpNumDataValue.isEqual(MFPNumeric.ONE)
                    || mmfpNumDataValue.isEqual(MFPNumeric.ZERO))) {
                return true;
            }
            return false;
        }
        
        public boolean isSingleInteger()    {
            if ((menumDataType == DATATYPES.DATUM_BOOLEAN || menumDataType == DATATYPES.DATUM_INTEGER || menumDataType == DATATYPES.DATUM_DOUBLE)
                    && mmfpNumDataValue.isActuallyInteger())    {
                return true;
            }
            return false;
        }
        public boolean isSingleDouble()    {
            if (menumDataType == DATATYPES.DATUM_BOOLEAN
                    || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE)    {
                return true;
            }
            return false;
        }
        public boolean isNumericalData(boolean bLookOnNullAsZero) {
            if (menumDataType == DATATYPES.DATUM_NULL) {
                return bLookOnNullAsZero;
            } else if (menumDataType == DATATYPES.DATUM_BOOLEAN
                    || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE
                    || menumDataType == DATATYPES.DATUM_COMPLEX)   {
                return true;
            } else if (menumDataType != DATATYPES.DATUM_REF_DATA) {
                return false;
            } else  {
                // data_ref type
                if (mdataList == null) {
                    // empty array.
                    mdataList = new DataClass[0];
                    return true;
                }
                for (int idx = 0; idx < mdataList.length; idx ++)  {
                    if (mdataList[idx] == null)    {
                        mdataList[idx] = new DataClass();  // if we find any null, we convert it first
                    }
                    DATATYPES dataTypeChild = mdataList[idx].getDataType();
                    if (dataTypeChild == DATATYPES.DATUM_NULL)  {
                        if (!bLookOnNullAsZero) {
                            return false;
                        }
                    } else if (dataTypeChild != DATATYPES.DATUM_BOOLEAN && dataTypeChild != DATATYPES.DATUM_INTEGER
                            && dataTypeChild != DATATYPES.DATUM_DOUBLE && dataTypeChild != DATATYPES.DATUM_COMPLEX
                            && !(mdataList[idx].isNumericalData(bLookOnNullAsZero)))    {
                        return false;
                    }
                }
                return true;
            }
        }
        
        public void validateDataClass_Step1() throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_NULL)    {
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                mdataList = new DataClass[0];
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_BOOLEAN)    {
                if (mmfpNumDataValue.isActuallyTrue())    {
                    mmfpNumDataValue = MFPNumeric.TRUE;
                } else if (mmfpNumDataValue.isActuallyFalse())    {
                    mmfpNumDataValue = MFPNumeric.FALSE;
                } else {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
                }
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                mdataList = new DataClass[0];
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_INTEGER)    {
                mmfpNumDataValue = mmfpNumDataValue.toIntOrNanInfMFPNum();
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                mdataList = new DataClass[0];
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_DOUBLE)    {
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                mdataList = new DataClass[0];
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_COMPLEX)    {
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                if (mdataList != null && mdataList.length > 0)    {
                    if (mdataList.length > 2)    {
                        DataClass[] dataList = new DataClass[2];
                        dataList[0] = mdataList[0];
                        dataList[1] = mdataList[1];
                        mdataList = dataList;
                    }
                    mdataList[0].validateDataClass_Step1();
                    if (mdataList.length > 1 && mdataList[1] != null)    {
                        mdataList[1].validateDataClass_Step1();    // recursive calling
                    }
                }
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_REF_DATA)    {
                if (mdcUncopibleFeature.mnValidatedCnt > MAX_REPEATING_VALIDATION_CNT_ALLOWED) {
                    //it has been validated many times. This implies a recursive reference. So throw an error.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_MATRIX_CANNOT_RECURSIVELY_REFERRED);
                }
                mdcUncopibleFeature.mnValidatedCnt ++;
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                if (mdataList != null)    {
                    for (int i = 0; i < mdataList.length; i++)    {
                        if (mdataList[i] != null)    {
                            mdataList[i].validateDataClass_Step1();    // recursive calling
                        }
                    }
                } else    {
                    mdataList = new DataClass[0];
                }
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_REF_FUNC)    {
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrValue = mstrUsrDefType = "";
                mdataList = new DataClass[0];
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_STRING)    {
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrFunctionName = mstrUsrDefType = "";
                mdataList = new DataClass[0];
                maexpr = AEInvalid.AEINVALID;
            } else if (menumDataType == DATATYPES.DATUM_ABSTRACT_EXPR)    {
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrValue = mstrFunctionName = mstrUsrDefType = "";
                mdataList = new DataClass[0];
            }
        }
        public void validateDataClass_Step2()    {
            if (menumDataType == DATATYPES.DATUM_REF_DATA)    {
                mdcUncopibleFeature.mnValidatedCnt = 0;
                if (mdataList != null)    {
                    for (int i = 0; i < mdataList.length; i++)    {
                        if (mdataList[i] != null && mdataList[i].menumDataType == DATATYPES.DATUM_REF_DATA)    {
                            mdataList[i].validateDataClass_Step2();    // recursive calling
                        }
                    }
                }
            }
        }
        
        public void validateDataClass() throws JFCALCExpErrException {
            validateDataClass_Step1();
            validateDataClass_Step2();
        }
        
        public DATATYPES getDataType()    {
            return menumDataType;
        }
        public MFPNumeric getDataValue() throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_BOOLEAN)    {
                /* make sure boolean value is 0 or 1 */
                if (!mmfpNumDataValue.isEqual(MFPNumeric.ZERO))    {
                    mmfpNumDataValue = MFPNumeric.TRUE;
                } else    {
                    mmfpNumDataValue = MFPNumeric.FALSE;
                }
            } else if (menumDataType == DATATYPES.DATUM_NULL || menumDataType == DATATYPES.DATUM_STRING
                    || menumDataType == DATATYPES.DATUM_REF_DATA || menumDataType == DATATYPES.DATUM_COMPLEX
                    || menumDataType == DATATYPES.DATUM_ABSTRACT_EXPR || menumDataType == DATATYPES.DATUM_REF_FUNC)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            }
            return mmfpNumDataValue;
        }
        public String getStringValue()    {
            if (menumDataType != DATATYPES.DATUM_STRING)    {
                mstrValue = "";
            }
            return mstrValue;
        }
        public String getFunctionName()    {
            if (menumDataType != DATATYPES.DATUM_REF_FUNC)    {
                mstrFunctionName = "";
            }
            return mstrFunctionName;
        }
        public DataClass[] getDataList()    {
            if (menumDataType != DATATYPES.DATUM_REF_DATA && menumDataType != DATATYPES.DATUM_COMPLEX){
                mdataList = new DataClass[0];
            }
            return mdataList;
        }
        public String getUsrDefType()    {
            if (mstrUsrDefType == null){
                mstrUsrDefType = "";
            }
            return mstrUsrDefType;            
        }
        public AbstractExpr getAExpr() {
            if (menumDataType != DATATYPES.DATUM_ABSTRACT_EXPR) {
                return AEInvalid.AEINVALID;
            }
            return maexpr;
        }
            
        public void setDataValue(MFPNumeric mfpNumDataValue, DATATYPES enumDataType) throws JFCALCExpErrException    {
            mmfpNumDataValue = mfpNumDataValue;
            menumDataType = enumDataType;
            validateDataClass();    
        }
        public void setDataValue(MFPNumeric mfpNumDataValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = mfpNumDataValue;
            menumDataType = DATATYPES.DATUM_DOUBLE;
            validateDataClass();    
        }
        public void setDataValue(boolean bValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = bValue?MFPNumeric.TRUE:MFPNumeric.FALSE;
            menumDataType = DATATYPES.DATUM_BOOLEAN;
            validateDataClass();    
        }
        public void setDataValue(byte nValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = new MFPNumeric((long)nValue);
            menumDataType = DATATYPES.DATUM_INTEGER;
            validateDataClass();    
        }
        public void setDataValue(short nValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = new MFPNumeric((long)nValue);
            menumDataType = DATATYPES.DATUM_INTEGER;
            validateDataClass();    
        }
        public void setDataValue(int nValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = new MFPNumeric((long)nValue);
            menumDataType = DATATYPES.DATUM_INTEGER;
            validateDataClass();    
        }
        public void setDataValue(long nValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = new MFPNumeric(nValue);
            menumDataType = DATATYPES.DATUM_INTEGER;
            validateDataClass();    
        }
        public void setDataValue(float fValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = new MFPNumeric((double)fValue);
            menumDataType = DATATYPES.DATUM_DOUBLE;
            validateDataClass();    
        }
        public void setDataValue(double dValue) throws JFCALCExpErrException    {
            mmfpNumDataValue = new MFPNumeric(dValue);
            menumDataType = DATATYPES.DATUM_DOUBLE;
            validateDataClass();    
        }
        
        public void setStringValue(String strValue) throws JFCALCExpErrException    {
            mstrValue = strValue;
            menumDataType = DATATYPES.DATUM_STRING;
            validateDataClass();    
        }
        public void setFunctionName(String strFunctionName) throws JFCALCExpErrException    {
            mstrFunctionName = strFunctionName;
            menumDataType = DATATYPES.DATUM_REF_FUNC;
            validateDataClass();    
        }
        public void setDataList(DataClass[] dataList, DATATYPES enumDataType) throws JFCALCExpErrException    {
            DATATYPES enumDataTypeOld = menumDataType;
            MFPNumeric mfpNumDataValueOld = mmfpNumDataValue;
            String strValueOld = mstrValue;
            String strFunctionNameOld = mstrFunctionName;
            DataClass[] dataListOld = mdataList;
            AbstractExpr aexprOld = maexpr;
            String strUsrDefTypeOld = mstrUsrDefType;            
            if (dataList == null)    {
                dataList = new DataClass[0];
            }
            mdataList = dataList;
            menumDataType = enumDataType;
            try {
                validateDataClass();
            } catch (JFCALCExpErrException e) {
                // if fail to set, restore, otherwise, incorrect dataclass will cause trouble later on.
                // only restore when set data list cause recursive referring may cause stack overflow.
                menumDataType = enumDataTypeOld;
                mmfpNumDataValue = mfpNumDataValueOld;
                mstrValue = strValueOld;
                mstrFunctionName = strFunctionNameOld;
                mdataList = dataListOld;
                maexpr = aexprOld;
                mstrUsrDefType = strUsrDefTypeOld;    
                validateDataClass_Step2();  // reset nvalidatedcnt.
                throw e;
            }
        }
        public void setDataList(DataClass[] dataList) throws JFCALCExpErrException    {
            DATATYPES enumDataTypeOld = menumDataType;
            MFPNumeric mfpNumDataValueOld = mmfpNumDataValue;
            String strValueOld = mstrValue;
            String strFunctionNameOld = mstrFunctionName;
            DataClass[] dataListOld = mdataList;
            AbstractExpr aexprOld = maexpr;
            String strUsrDefTypeOld = mstrUsrDefType;            
            if (dataList == null)    {
                dataList = new DataClass[0];
            }
            
            mdataList = dataList;
            menumDataType = DATATYPES.DATUM_REF_DATA;
            try {
                validateDataClass();
            } catch (JFCALCExpErrException e) {
                // if fail to set, restore, otherwise, incorrect dataclass will cause trouble later on.
                // only restore when set data list cause recursive referring may cause stack overflow.
                menumDataType = enumDataTypeOld;
                mmfpNumDataValue = mfpNumDataValueOld;
                mstrValue = strValueOld;
                mstrFunctionName = strFunctionNameOld;
                mdataList = dataListOld;
                maexpr = aexprOld;
                mstrUsrDefType = strUsrDefTypeOld;    
                validateDataClass_Step2();  // reset nvalidatedcnt.
                throw e;
            }
        }
        public void setAExpr(AbstractExpr aexpr) throws JFCALCExpErrException {
            maexpr = aexpr;
            menumDataType = DATATYPES.DATUM_ABSTRACT_EXPR;
            validateDataClass();    
        }
        public void setUsrDefType(String strUsrDefType)    {
            mstrUsrDefType = strUsrDefType;
        }
        
        // functions for complex
        public void setComplex(DataClass datumComplex) throws JFCALCExpErrException    {
            if (datumComplex == null)    {
                setComplex(MFPNumeric.ZERO, MFPNumeric.ZERO);
            } else    {
                copyTypeValue(datumComplex);
                // we need not to explicitely change menumDataType to DATUM_COMPLEX
                // because datumComplex may not be a complex number. In other words,
                // this setting may not be successful.
                changeDataType(DATATYPES.DATUM_COMPLEX);
            }
        }
        public void setComplex(DataClass datumReal, DataClass datumImage) throws JFCALCExpErrException    {
            menumDataType = DATATYPES.DATUM_COMPLEX;
            mdataList = new DataClass[2];
            if (datumReal != null)    {
                mdataList[0] = new DataClass();
                mdataList[0].copyTypeValue(datumReal);
            } else    {
                mdataList[0] = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
            }
            if (datumImage != null)    {
                mdataList[1] = new DataClass();
                mdataList[1].copyTypeValue(datumImage);
            } else    {
                mdataList[1] = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
            }
            validateDataClass();    
        }
        public void setComplex(MFPNumeric mfpNumReal, MFPNumeric mfpNumImage) throws JFCALCExpErrException    {
            menumDataType = DATATYPES.DATUM_COMPLEX;
            mdataList = new DataClass[2];
            mdataList[0] = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumReal);
            mdataList[1] = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumImage);
            validateDataClass();    
        }
        public void setComplexRadAngle(MFPNumeric mfpNumRad, MFPNumeric mfpNumAngle) throws JFCALCExpErrException    {
            menumDataType = DATATYPES.DATUM_COMPLEX;
            MFPNumeric mfpNumReal = mfpNumRad.multiply(MFPNumeric.cos(mfpNumAngle));
            MFPNumeric mfpNumImage = mfpNumRad.multiply(MFPNumeric.sin(mfpNumAngle));
            if (!mfpNumImage.isActuallyZero()
                    && mfpNumReal.divide(mfpNumImage).abs().compareTo(THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0) {
                // comparing to image, real is too small so that it should be set to zero.
                mfpNumReal = MFPNumeric.ZERO;
            } else if (!mfpNumReal.isActuallyZero()
                    && mfpNumImage.divide(mfpNumReal).abs().compareTo(THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0) {
                // comparing to real, image is too small so that it should be set to zero.
                mfpNumImage = MFPNumeric.ZERO;
            }

            mdataList = new DataClass[2];
            mdataList[0] = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumReal);
            mdataList[1] = new DataClass(DATATYPES.DATUM_DOUBLE, mfpNumImage);
            validateDataClass();    
        }
        public void setReal(DataClass datumReal) throws JFCALCExpErrException    {
            setComplex(datumReal, getImageDataClass());
        }
        public void setReal(MFPNumeric mfpNumReal) throws JFCALCExpErrException    {
            setComplex(mfpNumReal, getImage());
        }
        public void setImage(DataClass datumImage) throws JFCALCExpErrException    {
            setComplex(getRealDataClass(), datumImage);
        }
        public void setImage(MFPNumeric mfpNumImage) throws JFCALCExpErrException    {
            setComplex(getReal(), mfpNumImage);
        }
        public MFPNumeric[] getComplex() throws JFCALCExpErrException    {
            MFPNumeric[] mfpNumListRealImage = new MFPNumeric[2];
            mfpNumListRealImage[0] = getReal();
            mfpNumListRealImage[1] = getImage();
            return mfpNumListRealImage;
        }
        public MFPNumeric[] getComplexRadAngle() throws JFCALCExpErrException    {
            MFPNumeric[] mfpNumListRadAngle = new MFPNumeric[2];
            MFPNumeric mfpNumReal = getReal();
            MFPNumeric mfpNumImage = getImage();
            mfpNumListRadAngle[0] = MFPNumeric.hypot(mfpNumReal, mfpNumImage);
            if (!mfpNumImage.isActuallyZero() && !mfpNumImage.isNanOrInf()
                    && mfpNumReal.divide(mfpNumImage).abs().compareTo(THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0)    {
                // comparing to image, real is too small so that it should be set to zero.
                mfpNumReal =MFPNumeric.ZERO;
            } else if (!mfpNumReal.isActuallyZero() && !mfpNumReal.isNanOrInf()
                    && mfpNumImage.divide(mfpNumReal).abs().compareTo(THE_MAX_RELATIVE_ERROR_OF_TRIGONOMETRIC_CALCULATION) < 0)    {
                // comparing to real, image is too small so that it should be set to zero.
                mfpNumImage =MFPNumeric.ZERO;
            }
            mfpNumListRadAngle[1] = MFPNumeric.atan2(mfpNumImage, mfpNumReal);
            return mfpNumListRadAngle;
        }
        public MFPNumeric getReal() throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_BOOLEAN)    {
                return mmfpNumDataValue;
            } else if (menumDataType == DATATYPES.DATUM_DOUBLE
                    || menumDataType == DATATYPES.DATUM_INTEGER)    {
                return mmfpNumDataValue;
            } else if (menumDataType != DATATYPES.DATUM_COMPLEX)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList == null || mdataList.length == 0
                    || mdataList[0] == null || mdataList[0].menumDataType == DATATYPES.DATUM_NULL)    {
                return MFPNumeric.ZERO;
            } else if (mdataList[0].menumDataType != DATATYPES.DATUM_NULL
                    && mdataList[0].menumDataType != DATATYPES.DATUM_BOOLEAN
                    && mdataList[0].menumDataType != DATATYPES.DATUM_INTEGER
                    && mdataList[0].menumDataType != DATATYPES.DATUM_DOUBLE)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList[0].menumDataType == DATATYPES.DATUM_DOUBLE) {
                return mdataList[0].mmfpNumDataValue;
            } else    {
                return mdataList[0].mmfpNumDataValue;
            }
        }
        public DataClass getRealDataClass() throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_BOOLEAN)    {
                DataClass datum = new DataClass(DATATYPES.DATUM_INTEGER, mmfpNumDataValue);
                return datum;
            } else if (menumDataType == DATATYPES.DATUM_DOUBLE
                    || menumDataType == DATATYPES.DATUM_INTEGER)    {
                DataClass datum = new DataClass(menumDataType, mmfpNumDataValue);
                return datum;
            } else if (menumDataType != DATATYPES.DATUM_COMPLEX)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList == null || mdataList.length == 0
                    || mdataList[0] == null || mdataList[0].menumDataType == DATATYPES.DATUM_NULL)    {
                return new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
            } else if (mdataList[0].menumDataType != DATATYPES.DATUM_NULL
                    && mdataList[0].menumDataType != DATATYPES.DATUM_BOOLEAN
                    && mdataList[0].menumDataType != DATATYPES.DATUM_INTEGER
                    && mdataList[0].menumDataType != DATATYPES.DATUM_DOUBLE)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList[0].menumDataType == DATATYPES.DATUM_DOUBLE) {
                return new DataClass(DATATYPES.DATUM_DOUBLE, mdataList[0].mmfpNumDataValue);
            } else    {
                return new DataClass(DATATYPES.DATUM_INTEGER, mdataList[0].mmfpNumDataValue);
            }
        }
        public MFPNumeric getImage() throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_BOOLEAN
                    || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE)    {
                return MFPNumeric.ZERO;
            } else if (menumDataType != DATATYPES.DATUM_COMPLEX)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList == null || mdataList.length < 2
                    || mdataList[1] == null || mdataList[1].menumDataType == DATATYPES.DATUM_NULL)    {
                return MFPNumeric.ZERO;
            } else if (mdataList[1].menumDataType != DATATYPES.DATUM_NULL
                    && mdataList[1].menumDataType != DATATYPES.DATUM_BOOLEAN
                    && mdataList[1].menumDataType != DATATYPES.DATUM_INTEGER
                    && mdataList[1].menumDataType != DATATYPES.DATUM_DOUBLE)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList[1].menumDataType == DATATYPES.DATUM_DOUBLE) {
                return mdataList[1].mmfpNumDataValue;
            } else    {
                return mdataList[1].mmfpNumDataValue;
            }
        }
        public DataClass getImageDataClass() throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_BOOLEAN
                    || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE)    {
                DataClass datum = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
                return datum;
            } else if (menumDataType != DATATYPES.DATUM_COMPLEX)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList == null || mdataList.length < 2
                    || mdataList[1] == null || mdataList[1].menumDataType == DATATYPES.DATUM_NULL)    {
                return new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
            } else if (mdataList[1].menumDataType != DATATYPES.DATUM_NULL
                    && mdataList[1].menumDataType != DATATYPES.DATUM_BOOLEAN
                    && mdataList[1].menumDataType != DATATYPES.DATUM_INTEGER
                    && mdataList[1].menumDataType != DATATYPES.DATUM_DOUBLE)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            } else if (mdataList[1].menumDataType == DATATYPES.DATUM_DOUBLE) {
                return new DataClass(DATATYPES.DATUM_DOUBLE, mdataList[1].mmfpNumDataValue);
            } else    {
                return new DataClass(DATATYPES.DATUM_INTEGER, mdataList[1].mmfpNumDataValue);
            }
        }
        
        // functions for data reference

        /* 
         * AllocDataArray resets this dataclass and allocates a new data array for this.
         * Everything is initialized to null 
         */
        public void allocDataArray(int[] nListArraySize, DataClass datumDefault) throws JFCALCExpErrException    {
            if (nListArraySize == null || nListArraySize.length == 0 || nListArraySize[0] < 0)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else    {
                menumDataType = DATATYPES.DATUM_REF_DATA;
                mmfpNumDataValue = THE_NULL_DATA_VALUE;
                mstrValue = "";
                mstrFunctionName = "";
                mdataList = new DataClass[nListArraySize[0]];
                maexpr = AEInvalid.AEINVALID;
                mstrUsrDefType = "";
                if (nListArraySize.length == 1)    {
                    for (int index = 0; index < nListArraySize[0]; index ++)    {
                        mdataList[index] = new DataClass();
                        mdataList[index].copyTypeValueDeep(datumDefault);
                    }
                } else {
                    for (int index = 0; index < nListArraySize[0]; index ++)    {
                        mdataList[index] = new DataClass();
                        int[] nListSubArraySize = new int[nListArraySize.length-1];
                        for (int index1 = 1; index1 < nListArraySize.length; index1 ++)    {
                            nListSubArraySize[index1 - 1] = nListArraySize[index1];
                        }
                        mdataList[index].allocDataArray(nListSubArraySize, datumDefault);
                    }
                }
            }
        }
        public int getDataListSize() throws JFCALCExpErrException    {
            if (menumDataType != DATATYPES.DATUM_REF_DATA)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_DATATYPE);
            }
            if (mdataList == null)    {
                return 0;
            } else    {
                return mdataList.length;
            }
        }
        
        public int[] recalcDataArraySize() throws JFCALCExpErrException    {
            if (menumDataType != DATATYPES.DATUM_REF_DATA)    {
                // if it is not an array, it must be a single element, we arrive at the end.
                // abstract expr data type does not support array size calculation. So we always assume it is single data type (size is [])
                int[] narrayDims = new int[0];
                return narrayDims;
            }
            int nVectorLength = (mdataList == null)?0:mdataList.length;
            int[][] vectorLens = new int[nVectorLength][];
            int nMaxDim = 0;
            for (int index = 0; index < nVectorLength; index ++)    {
                if (mdataList[index] != null)    {
                    vectorLens[index] = mdataList[index].recalcDataArraySize();
                } else {
                    vectorLens[index] = new int[0];
                }
                if (nMaxDim < vectorLens[index].length)    {
                    nMaxDim = vectorLens[index].length;
                }
            }
            int[] nArraySize = new int[nMaxDim + 1];
            nArraySize[0] = nVectorLength;
            for (int index = 1; index < nMaxDim + 1; index++)    {
                int nMaxVectorLen = 0;
                for (int index1 = 0; index1 < nVectorLength; index1 ++)    {
                    if (vectorLens[index1].length >= index)    {
                        if (nMaxVectorLen < vectorLens[index1][index - 1])    {
                            nMaxVectorLen = vectorLens[index1][index - 1];
                        }
                    }
                }
                nArraySize[index] = nMaxVectorLen;
            }
            return nArraySize;
        }
        
        public void populateDataArray(int[] narrayDims, boolean bCvtNull2Zero) throws JFCALCExpErrException    {
            // populate a data array. If no value or null value in a cell, put 0 in it if bCvtNull2Zero is true.
            // assume the parameter narrayDims is not null.
            if (narrayDims == null)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            } else if (narrayDims.length == 0)    {
                if (menumDataType == DATATYPES.DATUM_REF_DATA)    {    // if it is an array, it cannot be populated to single number
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else {    // if it is a single value, and we want it be populated to a single value, just return.
                    return;
                }
            }
            
            int nThisDimSize = 0;
            if (menumDataType == DATATYPES.DATUM_REF_DATA)    {
                nThisDimSize = getDataListSize();
            } else    {
                nThisDimSize = 1;
            }
            if (nThisDimSize > narrayDims[0])    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
            }
            
            int[] narrayDimsNext = null;
            if (narrayDims.length > 1)    {
                narrayDimsNext = new int[narrayDims.length - 1];
                for (int index = 0; index < narrayDims.length - 1; index ++)    {
                    narrayDimsNext[index] = narrayDims[index + 1];
                }
            }
            
            DataClass[] dataList = new DataClass[narrayDims[0]];
            if (menumDataType != DATATYPES.DATUM_REF_DATA)    {
                DataClass datumNew = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
                if ((!bCvtNull2Zero) || (datumNew.menumDataType != DATATYPES.DATUM_NULL))    {
                    datumNew.copyTypeValue(this);
                }
                if (narrayDimsNext != null)    {
                    datumNew.populateDataArray(narrayDimsNext, bCvtNull2Zero);
                }
                dataList[0] = datumNew;
                for (int index = 1; index < narrayDims[0]; index ++)    {
                    DataClass datumNewElem = new DataClass();
                    datumNewElem.setDataValue(MFPNumeric.ZERO);
                    if (narrayDimsNext != null)    {
                        datumNewElem.populateDataArray(narrayDimsNext, bCvtNull2Zero);
                    }
                    dataList[index] = datumNewElem;
                }
            } else    {                
                for (int index = 0; index < narrayDims[0]; index ++)    {
                    dataList[index] = new DataClass(DATATYPES.DATUM_DOUBLE, MFPNumeric.ZERO);
                    if (index < nThisDimSize)    {
                        if ((!bCvtNull2Zero) || (mdataList[index].menumDataType != DATATYPES.DATUM_NULL))    {
                            dataList[index] = mdataList[index];
                        }
                    }
                    if (narrayDimsNext != null)    {
                        dataList[index].populateDataArray(narrayDimsNext, bCvtNull2Zero);
                    }
                }
            }
            setDataList(dataList);
        }
        
        // return DataClass reference
        public DataClass getDataAtIndexByRef(int[] indexList) throws JFCALCExpErrException    {
            DataClass datumCurrent = this;
            for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
                if (indexList[nIndex] < 0)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else if (datumCurrent.menumDataType != DATATYPES.DATUM_REF_DATA)    {
                    // a non-array data cannot be converted to x[0,0,0,0,0...] implicitly.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else    {
                    if (indexList[nIndex] >= datumCurrent.mdataList.length)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                    } else    {
                        datumCurrent = datumCurrent.mdataList[indexList[nIndex]];
                    }
                }
            }
            return datumCurrent;
        }
        // return DataClass value
        public DataClass getDataAtIndex(int[] indexList) throws JFCALCExpErrException    {
            DataClass datumReturn = new DataClass();
            datumReturn.copyTypeValueDeep(getDataAtIndexByRef(indexList));
            return datumReturn;
        }
        
        // assign data class reference, not data value.
        public void setDataAtIndexByRef(int[] indexList, DataClass datum) throws JFCALCExpErrException    {
            DataClass datumCurrent = this;
            for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
                if (indexList[nIndex] < 0)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else if (datumCurrent.menumDataType != DATATYPES.DATUM_REF_DATA)    {
                    // a non-array data cannot be converted to x[0,0,0,0,0...] implicitly.
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                } else    {
                    if (indexList[nIndex] >= datumCurrent.mdataList.length)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                    } else if (nIndex < indexList.length - 1)    {
                        datumCurrent = datumCurrent.mdataList[indexList[nIndex]];
                    } else    {
                        // assign data reference
                        datumCurrent.mdataList[indexList[nIndex]] = datum;
                    }
                }
            }
            // validateDataClass(); // do not validate because this function is mainly used for internal calculation like swap rows or columns
                                    // will throw error during swapping.
        }
        // assign data class by value
        public void setDataAtIndex(int[] indexList, DataClass datum) throws JFCALCExpErrException    {
            DataClass datumParam = new DataClass();
            datumParam.copyTypeValue(datum);
            setDataAtIndexByRef(indexList, datumParam);
        }
        // different from GetDataAtIndexByRef, this function will create and return an datumDefault
        // data if the array index is beyond its range.
        public DataClass obtainDataAtIndexByRef(int[] indexList, DataClass datumDefault) throws JFCALCExpErrException    {
            for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
                if (indexList[nIndex] < 0)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                }
            }
            DataClass datumCurrent = this;
            for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
                if (datumCurrent.menumDataType != DATATYPES.DATUM_REF_DATA)    {
                    try    {
                        datumCurrent.changeDataType(DATATYPES.DATUM_REF_DATA);
                    } catch (JFCALCExpErrException e)    {
                        // I cannot change datumCurrent to a ref_data type, force it to be.
                        // DATUM_NULL cannot be converted to an array automatically, it will be mannually cvted to an empty array here.
                        datumCurrent.setDataList(new DataClass[0]);
                    }
                }
                if (datumCurrent.mdataList != null && datumCurrent.mdataList.length > indexList[nIndex]){
                    datumCurrent = datumCurrent.mdataList[indexList[nIndex]];
                    if (datumCurrent == null)    {
                        datumCurrent = new DataClass(new DataClass[0]);
                    }
                } else    {
                    if (datumCurrent.mdataList == null)    {
                        datumCurrent.mdataList = new DataClass[0];
                    }
                    DataClass[] dataList = new DataClass[indexList[nIndex] + 1];
                    for (int nIndex2 = 0; nIndex2 <= indexList[nIndex]; nIndex2 ++)    {
                        if (nIndex2 < datumCurrent.mdataList.length)    {
                            dataList[nIndex2] = datumCurrent.mdataList[nIndex2];
                        } else if (nIndex2 < indexList[nIndex])    {
                            dataList[nIndex2] = new DataClass();
                            dataList[nIndex2].copyTypeValueDeep(datumDefault);
                        } else    {
                            dataList[nIndex2] = new DataClass(new DataClass[0]);
                        }
                    }
                    datumCurrent.mdataList = dataList;
                    datumCurrent = datumCurrent.mdataList[indexList[nIndex]];
                }
            }
            return datumCurrent;
        }
        // different from GetDataAtIndex, this function will create and return an empty
        // data if the array index is beyond its range.
        public DataClass obtainDataAtIndex(int[] indexList, DataClass datumDefault) throws JFCALCExpErrException    {
            DataClass datumReturn = new DataClass();
            datumReturn.copyTypeValueDeep(obtainDataAtIndexByRef(indexList, datumDefault));
            return datumReturn;
        }
        // different from SetDataAtIndexByRef, this function will create data
        // if the array index is beyond its range.
        public void createDataAtIndexByRef(int[] indexList, DataClass datum, DataClass datumNewByDefault) throws JFCALCExpErrException    {
            for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
                if (indexList[nIndex] < 0)    {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_WRONG_INDEX);
                }
            }
            DataClass datumCurrent = this;
            for (int nIndex = 0; nIndex < indexList.length; nIndex ++)    {
                if (datumCurrent.menumDataType != DATATYPES.DATUM_REF_DATA)    {
                    try    {
                        datumCurrent.changeDataType(DATATYPES.DATUM_REF_DATA);
                    } catch (JFCALCExpErrException e)    {
                        // I cannot change datumCurrent to a ref_data type, force it to be.
                        // DATUM_NULL cannot be converted to an array automatically, it will be mannually cvted to an empty array here.
                        datumCurrent.setDataList(new DataClass[0]);
                    }
                }
                if (datumCurrent.mdataList != null && datumCurrent.mdataList.length > indexList[nIndex]){
                    if (nIndex < indexList.length - 1)    {
                        datumCurrent = datumCurrent.mdataList[indexList[nIndex]];
                        if (datumCurrent == null)    {
                            datumCurrent = new DataClass(new DataClass[0]);
                        }
                    } else    {
                        datumCurrent.mdataList[indexList[nIndex]] = datum;
                    }
                } else    {
                    if (datumCurrent.mdataList == null)    {
                        datumCurrent.mdataList = new DataClass[0];
                    }
                    DataClass[] dataList = new DataClass[indexList[nIndex] + 1];
                    for (int nIndex2 = 0; nIndex2 <= indexList[nIndex]; nIndex2 ++)    {
                        if (nIndex2 < datumCurrent.mdataList.length)    {
                            dataList[nIndex2] = datumCurrent.mdataList[nIndex2];
                        } else if (nIndex2 < indexList[nIndex])    {
                            dataList[nIndex2] = new DataClass();
                            dataList[nIndex2].copyTypeValueDeep(datumNewByDefault);// Have to initialize the data to datumNewByDefault, otherwise, null will leads to problem in PopulateDataArray.
                        } else    {
                            dataList[nIndex2] = new DataClass(new DataClass[0]);
                        }
                    }
                    datumCurrent.mdataList = dataList;
                    if (nIndex < indexList.length - 1)    {
                        datumCurrent = datumCurrent.mdataList[indexList[nIndex]];
                    } else    {
                        datumCurrent.mdataList[indexList[nIndex]] = datum;
                    }
                }
            }
        }
        // different from SetDataAtIndex, this function will create data
        // if the array index is beyond its range.
        public void createDataAtIndex(int[] indexList, DataClass datum, DataClass datumNewByDefault) throws JFCALCExpErrException    {
            DataClass datumParam = new DataClass();
            datumParam.copyTypeValue(datum);
            createDataAtIndexByRef(indexList, datumParam, datumNewByDefault);
        }
        
        public boolean changeDataType2AExpr() throws JFCALCExpErrException {
            DataClass datumCy = new DataClass();
            datumCy.copyTypeValue(this);
            menumDataType = DATATYPES.DATUM_ABSTRACT_EXPR;
            mdataList = new DataClass[0];
            mmfpNumDataValue = THE_NULL_DATA_VALUE;
            mstrValue = "";
            mstrFunctionName = "";
            try {
                maexpr = new AEConst(datumCy);
            } catch (Exception e) {
                // will not arrive here.
            }
            mstrUsrDefType = "";
            return true;
        }
        
        // change data type
        public boolean changeDataType(DATATYPES enumNewDataType) throws JFCALCExpErrException
        {
            switch(menumDataType)
            {
            case DATUM_NULL:
                switch(enumNewDataType)
                {
                case DATUM_NULL:
                    validateDataClass();
                    return true;
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_A_NULL_DATUM_TO_ANY_OTHER_DATATYPE);
                }
            case DATUM_INTEGER:
                switch(enumNewDataType)
                {
                case DATUM_NULL:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
                case DATUM_COMPLEX:
                case DATUM_REF_DATA:
                    // need a data list
                    menumDataType = enumNewDataType;
                    mdataList = new DataClass[1];
                    mdataList[0] = new DataClass();
                    // shouldn't call SetDataClass here because it will call ChangeDataType again.
                    //mdataList[1].SetDataClass(DATATYPES.DATUM_INTEGER, mbigdecDataValue, "", "", new DataClass[0], "");
                    mdataList[0].menumDataType = DATATYPES.DATUM_INTEGER;
                    mdataList[0].mmfpNumDataValue = mmfpNumDataValue;
                    mdataList[0].mstrValue = "";
                    mdataList[0].mstrFunctionName = "";
                    mdataList[0].mdataList = new DataClass[0];
                    mdataList[0].mstrUsrDefType = "";
                    mmfpNumDataValue = THE_NULL_DATA_VALUE;
                    mstrValue = "";
                    mstrFunctionName = "";
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_INTEGER:
                    validateDataClass();
                    return true;
                case DATUM_DOUBLE:
                    menumDataType = enumNewDataType;
                    mstrValue = "";
                    mstrFunctionName = "";
                    mdataList = new DataClass[0];
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_BOOLEAN:
                    menumDataType = enumNewDataType;
                    try {
                        mmfpNumDataValue = mmfpNumDataValue.toBoolMFPNum();
                    } catch (ArithmeticException e) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
                    }
                    mstrValue = "";
                    mstrFunctionName = "";
                    mdataList = new DataClass[0];
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_INTEGER);
                }
            case DATUM_DOUBLE:
                switch(enumNewDataType)
                {
                case DATUM_NULL:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
                case DATUM_REF_DATA:
                case DATUM_COMPLEX:
                    // need a data list
                    menumDataType = enumNewDataType;
                    mdataList = new DataClass[1];
                    mdataList[0] = new DataClass();
                    // shouldn't call SetDataClass here because it will call ChangeDataType again.
                    //mdataList[0].SetDataClass(DATATYPES.DATUM_DOUBLE, mbigdecDataValue, "", "", new DataClass[0], "");
                    mdataList[0].menumDataType = DATATYPES.DATUM_DOUBLE;
                    mdataList[0].mmfpNumDataValue = mmfpNumDataValue;
                    mdataList[0].mstrValue = "";
                    mdataList[0].mstrFunctionName = "";
                    mdataList[0].mdataList = new DataClass[0];
                    mdataList[0].mstrUsrDefType = "";
                    mmfpNumDataValue = THE_NULL_DATA_VALUE;
                    mstrValue = "";
                    mstrFunctionName = "";
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_INTEGER:
                    menumDataType = enumNewDataType;
                    mmfpNumDataValue = mmfpNumDataValue.toIntOrNanInfMFPNum();
                    mstrValue = "";
                    mstrFunctionName = "";
                    mdataList = new DataClass[0];
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_DOUBLE:
                    validateDataClass();
                    return true;
                case DATUM_BOOLEAN:
                    menumDataType = enumNewDataType;
                    try {
                        mmfpNumDataValue = mmfpNumDataValue.toBoolMFPNum();
                    } catch (ArithmeticException e) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
                    }
                    mstrValue = "";
                    mstrFunctionName = "";
                    mdataList = new DataClass[0];
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_DOUBLE);
                }
            case DATUM_BOOLEAN:
                switch(enumNewDataType)
                {
                case DATUM_NULL:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
                case DATUM_REF_DATA:
                case DATUM_COMPLEX:
                    // need a data list
                    menumDataType = enumNewDataType;
                    mdataList = new DataClass[1];
                    mdataList[0] = new DataClass();
                    // shouldn't call SetDataClass here because it will call ChangeDataType again.
                    //mdataList[1].SetDataClass(DATATYPES.DATUM_BOOLEAN, mbigdecDataValue, "", "", new DataClass[0], "");
                    if (enumNewDataType == DATATYPES.DATUM_COMPLEX)    {
                        mdataList[0].menumDataType = DATATYPES.DATUM_INTEGER;
                    } else    {
                        mdataList[0].menumDataType = DATATYPES.DATUM_BOOLEAN;
                    }
                    mdataList[0].mmfpNumDataValue = mmfpNumDataValue.toIntOrNanInfMFPNum();
                    mdataList[0].mstrValue = "";
                    mdataList[0].mstrFunctionName = "";
                    mdataList[0].mdataList = new DataClass[0];
                    mdataList[0].mstrUsrDefType = "";
                    mmfpNumDataValue = THE_NULL_DATA_VALUE;
                    mstrValue = "";
                    mstrFunctionName = "";
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_BOOLEAN:
                    validateDataClass();
                    return true;
                case DATUM_INTEGER:
                    menumDataType = enumNewDataType;
                    mmfpNumDataValue = mmfpNumDataValue.toIntOrNanInfMFPNum();
                    mstrValue = "";
                    mstrFunctionName = "";
                    mdataList = new DataClass[0];
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_DOUBLE:
                    menumDataType = enumNewDataType;
                    mmfpNumDataValue = mmfpNumDataValue.toDblOrNanInfMFPNum();
                    mstrValue = "";
                    mstrFunctionName = "";
                    mdataList = new DataClass[0];
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_BOOLEAN);
                }
            case DATUM_COMPLEX:
                switch(enumNewDataType)
                {
                case DATUM_NULL:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
                case DATUM_BOOLEAN:
                case DATUM_INTEGER:
                case DATUM_DOUBLE:
                    // we shouldnt use getImage.isZero() because GetImage may return a very very very
                    // small double which is the result of something like 1/3 - 1/4 - 1/12.
                    if (getImage().isActuallyZero())    {
                        setDataValue(getReal(), enumNewDataType);
                    } else    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX);
                    }
                    return true;
                case DATUM_COMPLEX:
                    validateDataClass();
                    return true;
                case DATUM_REF_DATA:
                    DataClass datumComplex = new DataClass();
                    datumComplex.copyTypeValue(this);
                    menumDataType = enumNewDataType;
                    mdataList = new DataClass[1];
                    mdataList[0] = datumComplex;
                    mmfpNumDataValue = THE_NULL_DATA_VALUE;
                    mstrValue = "";
                    mstrFunctionName = "";
                    maexpr = AEInvalid.AEINVALID;
                    mstrUsrDefType = "";
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_COMPLEX);
                }
            case DATUM_REF_DATA:
                switch(enumNewDataType)
                {
                case DATUM_NULL:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CHANGE_ANY_OTHER_DATATYPE_TO_NONEXIST);
                case DATUM_BOOLEAN:
                case DATUM_INTEGER:
                case DATUM_DOUBLE:
                case DATUM_COMPLEX:    /* complex should not be looked on as a 2-element data reference. complex number is a single number */
                    if (mdataList == null || mdataList.length != 1)    {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE);
                    }
                    // there are some tricks here. if original dataclass is a dataref and we want to convert it to a single number like
                    // double or complex, we should use deepcopy because there might be other dataclasses reference the dataref. The convesion
                    // should not affect other dataclasses.
                    menumDataType = mdataList[0].menumDataType;
                    mstrValue = mdataList[0].mstrValue;
                    mstrFunctionName = mdataList[0].mstrFunctionName;
                    maexpr = mdataList[0].maexpr;
                    mstrUsrDefType = mdataList[0].mstrUsrDefType;
                    if (mdataList[0].getDataType() == DATATYPES.DATUM_COMPLEX)    {
                        // if the child of original dataclass is a complex, mdataList of the child stores real and image
                        // because convert from original dataref dataclass to a single number should not change the original
                        // dataref, doing copy here.
                        mmfpNumDataValue = THE_NULL_DATA_VALUE;
                        DataClass datumReal = mdataList[0].getRealDataClass();
                        DataClass datumImage = mdataList[0].getImageDataClass();
                        mdataList = new DataClass[2];
                        mdataList[0] = datumReal;
                        mdataList[1] = datumImage;
                    } else if (mdataList[0].getDataType() == DATATYPES.DATUM_BOOLEAN
                            || mdataList[0].getDataType() == DATATYPES.DATUM_INTEGER
                            || mdataList[0].getDataType() == DATATYPES.DATUM_DOUBLE)    {
                        // if the child of original dataclass is a boolean, integer or double, mdataList[0].mdataList is empty
                        // value is stored in mdataList[0].mbigdecDataValue
                        mmfpNumDataValue = mdataList[0].mmfpNumDataValue;    // MFP Numeric is immutable, so assign the reference, need not to copy.
                        mdataList = new DataClass[0];
                    } else if (mdataList[0].getDataType() == DATATYPES.DATUM_REF_DATA)    {    //  child of original dataclass is a data reference
                        mmfpNumDataValue = THE_NULL_DATA_VALUE;
                        mdataList = mdataList[0].mdataList;
                    } else    {    // string, function name, etc.
                        mmfpNumDataValue = THE_NULL_DATA_VALUE;
                        mdataList = new DataClass[0];
                    }
                    changeDataType(enumNewDataType);    // recursive calling.
                    return true;
                case DATUM_REF_DATA:
                    validateDataClass();
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_DATA_REFERENCE);
                }
            case DATUM_STRING:
                switch(enumNewDataType)
                {
                case DATUM_STRING:
                    validateDataClass();
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_STRING);
                }
            case DATUM_REF_FUNC:
                switch(enumNewDataType)
                {
                case DATUM_REF_FUNC:
                    validateDataClass();
                    return true;
                case DATUM_ABSTRACT_EXPR:
                    return changeDataType2AExpr();
                default:
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_FUNCTION_REFERENCE);
                }
            case DATUM_ABSTRACT_EXPR:
                switch(enumNewDataType)
                {
                case DATUM_ABSTRACT_EXPR:
                    validateDataClass();
                    return true;
                default:
                    if (maexpr instanceof AEConst) {
                        DataClass datum = ((AEConst)maexpr).getDataClassRef();  // just use reference, do not use deep copy.
                        datum.changeDataType(enumNewDataType);
                        maexpr = AEInvalid.AEINVALID;
                        this.copyTypeValue(datum);
                    } else {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_CHANGE_DATATYPE_FROM_ABSTRACT_EXPR);
                    }
                }
            default:
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_DATATYPE_IS_NOT_DEFINED);
            }
        }    
 
        public boolean isEqual(DataClass datum, double errorScale) throws JFCALCExpErrException
        {
            if (errorScale < 0)    {
                errorScale = 0;    // ensure that error scale is always non-negative
            }
            if (menumDataType == DATATYPES.DATUM_NULL)    {
                if (datum.menumDataType == DATATYPES.DATUM_NULL)    {
                    return true;
                }
            } else if (menumDataType == DATATYPES.DATUM_BOOLEAN || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE)    {
                if ((datum.menumDataType == DATATYPES.DATUM_BOOLEAN || datum.menumDataType == DATATYPES.DATUM_INTEGER || datum.menumDataType == DATATYPES.DATUM_DOUBLE)
                        && MFPNumeric.isEqual(mmfpNumDataValue, datum.mmfpNumDataValue, errorScale))    {
                    return true;
                } else if (datum.menumDataType == DATATYPES.DATUM_COMPLEX
                        && MFPNumeric.isEqual(datum.getImage(), MFPNumeric.ZERO, errorScale)
                        && MFPNumeric.isEqual(datum.getReal(), mmfpNumDataValue, errorScale))    {
                    return true;
                }
                
            } else if (menumDataType == DATATYPES.DATUM_COMPLEX)    {
                if ((datum.menumDataType == DATATYPES.DATUM_BOOLEAN || datum.menumDataType == DATATYPES.DATUM_INTEGER || datum.menumDataType == DATATYPES.DATUM_DOUBLE)
                        && MFPNumeric.isEqual(getReal(), datum.mmfpNumDataValue, errorScale)
                        && MFPNumeric.isEqual(getImage(), MFPNumeric.ZERO, errorScale))    {
                    return true;
                } else if (datum.menumDataType == DATATYPES.DATUM_COMPLEX
                        && MFPNumeric.isEqual(datum.getReal(), getReal(), errorScale) && MFPNumeric.isEqual(datum.getImage(), getImage(), errorScale))    {
                    return true;
                }
            } else if (menumDataType == DATATYPES.DATUM_REF_DATA)    {
                if (datum.menumDataType == DATATYPES.DATUM_REF_DATA && datum.getDataListSize() == getDataListSize())    {
                    if (getDataListSize() == 0)    {
                        return true;    // both of the data lists are empty.
                    }
                    boolean bEqualDataList = true;
                    for (int index = 0; index < mdataList.length; index++)    {
                        // recursive compare
                        if (mdataList[index].isEqual(datum.mdataList[index], errorScale) == false)    {
                            bEqualDataList = false;
                        }
                    }
                    return bEqualDataList;
                }
            } else if (menumDataType == DATATYPES.DATUM_STRING)    {
                if (datum.menumDataType == DATATYPES.DATUM_STRING && datum.mstrValue.equals(mstrValue))    {
                    return true;
                }
            } else if (menumDataType == DATATYPES.DATUM_REF_FUNC)    {
                if (datum.menumDataType == DATATYPES.DATUM_REF_FUNC && datum.mstrFunctionName.equals(mstrFunctionName))    {
                    return true;
                }
            } else if (menumDataType == DATATYPES.DATUM_ABSTRACT_EXPR) {
                if (datum.menumDataType == DATATYPES.DATUM_ABSTRACT_EXPR && maexpr.isEqual(datum.maexpr)) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean isEqual(DataClass datum) throws JFCALCExpErrException
        {
            return isEqual(datum, 1);
        }
         // This function identify if a data value is zero (false is zero) or a data array is full-of-zero array
        public boolean isZeros(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
            if (menumDataType == DATATYPES.DATUM_NULL)    {
                if (bExplicitNullIsZero)    {
                    return true;
                } else    {
                    return false;
                }
            } else if (menumDataType == DATATYPES.DATUM_BOOLEAN || menumDataType == DATATYPES.DATUM_INTEGER
                    || menumDataType == DATATYPES.DATUM_DOUBLE
                    || menumDataType == DATATYPES.DATUM_COMPLEX)    {
                DataClass datum = new DataClass();
                datum.setDataValue(MFPNumeric.ZERO);
                if (this.isEqual(datum))    {
                    return true;
                } else    {
                    return false;
                }
            } else if (menumDataType == DATATYPES.DATUM_REF_DATA)    {
                if (mdataList == null)    {
                    mdataList = new DataClass[0];
                }
                for (int idx = 0; idx < mdataList.length; idx ++)    {
                    if (mdataList[idx] == null)    {
                        continue;
                    } else if (mdataList[idx].isZeros(bExplicitNullIsZero) == false)    {
                        return false;
                    }
                }
                return true;
            } else    {
                return false;
            }
        }
        
        public void setAllLeafChildren(DataClass datumValue) throws JFCALCExpErrException    {
            if (menumDataType != DATATYPES.DATUM_REF_DATA)    {
                copyTypeValueDeep(datumValue);
            } else if (mdataList != null)    {
                for (int idx = 0; idx < mdataList.length; idx ++)    {
                    if (mdataList[idx] != null)    {
                        mdataList[idx].setAllLeafChildren(datumValue);
                    } else    {
                        mdataList[idx] = new DataClass();
                        mdataList[idx].setAllLeafChildren(datumValue);
                    }
                }
            }
        }
        
        // This function identify if a data value is I or 1 or [1].
        public boolean isEye(boolean bExplicitNullIsZero) throws JFCALCExpErrException    {
            DataClass datumOne = new DataClass();
            datumOne.setDataValue(MFPNumeric.ONE);
            int[] narrayDims = recalcDataArraySize();
            if (narrayDims.length == 0)    {
                if (isEqual(datumOne))    {
                    return true;
                } else    {
                    return false;
                }
            } else     {
                for (int idx = 0; idx < narrayDims.length; idx ++)    {
                    if (narrayDims[idx] != narrayDims[0])    {
                        return false;
                    }
                }
                
                // check if all the should be one elements are one.
                DataClass datumCopy = new DataClass();
                datumCopy.copyTypeValueDeep(this);
                datumCopy.populateDataArray(narrayDims, bExplicitNullIsZero);
                for (int idxSize = 0; idxSize < narrayDims[0]; idxSize ++)   {
                    DataClass datumTmp = datumCopy;
                    for (int idxDim = 0; idxDim < narrayDims.length; idxDim ++)  {
                        if (datumTmp.getDataList() == null || datumTmp.getDataList().length <= idxSize)    {
                            return false;    // does not include the element.
                        }
                        datumTmp = datumTmp.getDataList()[idxSize];
                    }
                    if (datumTmp.isEqual(datumOne) == false)    {
                        return false;
                    } else    {
                        datumTmp.setDataValue(MFPNumeric.ZERO);    // convert 1 to zero
                    }
                }
                // after convert all one to zero, it should be a zero array
                return datumCopy.isZeros(bExplicitNullIsZero);
            }
        }
        
        // This is not deep copy. array is copied as a reference. array elements will not be copied.
        // however, complex number will be copied.
        public void copyTypeValue(DataClass datumSrc) throws JFCALCExpErrException
        {
            if (datumSrc.getDataType() != DATATYPES.DATUM_COMPLEX)    {
                setDataClass(datumSrc.menumDataType, datumSrc.mmfpNumDataValue, datumSrc.mstrValue, datumSrc.mstrFunctionName, datumSrc.mdataList, datumSrc.maexpr, datumSrc.mstrUsrDefType);
            } else    {
                setComplex(datumSrc.getReal(), datumSrc.getImage());
            }
        }
        
        // This is deep copy. Each array elemenet is copied.
        public final void copyTypeValueDeep(DataClass datumSrc) throws JFCALCExpErrException
        {
            if (datumSrc.getDataType() != DATATYPES.DATUM_COMPLEX && datumSrc.getDataType() != DATATYPES.DATUM_REF_DATA)    {
                // do not clone maexpr because maexpr is immutable (even for AEConst, its dataclass can be changed but its dataclass reference cannot be changed).
                setDataClass(datumSrc.menumDataType, datumSrc.mmfpNumDataValue, datumSrc.mstrValue, datumSrc.mstrFunctionName, datumSrc.mdataList, datumSrc.maexpr, datumSrc.mstrUsrDefType);
            } else if (datumSrc.getDataType() == DATATYPES.DATUM_COMPLEX)    {
                setComplex(datumSrc.getReal(), datumSrc.getImage());
            } else    {    //DATUM_REF_DATA
                if (datumSrc.getDataList() == null)    {
                    setDataList(new DataClass[0]);
                } else {
                    DataClass[] dataList = new DataClass[datumSrc.getDataListSize()];
                    for (int index0 = 0; index0 < dataList.length; index0 ++)    {
                        dataList[index0] = new DataClass();
                        if (datumSrc.getDataList()[index0] != null)    {
                            dataList[index0].copyTypeValueDeep(datumSrc.getDataList()[index0]);
                        }
                    }
                    setDataList(dataList);
                }
            }
        }
        
        // clone itself.
        public DataClass cloneSelf() throws JFCALCExpErrException    {
            DataClass datumReturn = new DataClass();
            datumReturn.copyTypeValueDeep(this);
            return datumReturn;
        }
        
        @Override
        public String toString()    {
            String strReturn = "";
            try {
                strReturn = output();
            } catch (JFCALCExpErrException e) {
                // TODO Auto-generated catch block
                strReturn = e.toString();
                e.printStackTrace();
            }
            return strReturn;
        }
        
        public String output() throws JFCALCExpErrException    {
            String strOutput = "";
            if (menumDataType == DATATYPES.DATUM_NULL)
            {
                strOutput = "NULL";
            }
            else if (menumDataType == DATATYPES.DATUM_BOOLEAN)
            {
                /* If the answer is a boolean */
                if (mmfpNumDataValue.mType == MFPNumeric.Type.MFP_NAN_VALUE) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CAN_NOT_CONVERT_NAN_VALUE_TO_BOOLEAN);
                } else if (MFPNumeric.isEqual(mmfpNumDataValue, MFPNumeric.ZERO)) {
                    strOutput = "FALSE";
                } else {
                    strOutput = "TRUE";
                }
            }
            else if (menumDataType == DATATYPES.DATUM_DOUBLE)
            {
                /* If the answer is an double */
                strOutput = mmfpNumDataValue.toString();
            }
            else if (menumDataType == DATATYPES.DATUM_INTEGER)
            {
                /* If the answer is an integer */
                strOutput = mmfpNumDataValue.toIntOrNanInfMFPNum().toString();
            }
            else if (menumDataType == DATATYPES.DATUM_STRING)
            {
                /* If the answer is a string */
                strOutput = "\"" + mstrValue + "\"";
            }
            else if (menumDataType == DATATYPES.DATUM_REF_FUNC)
            {
                /* If the answer is a function name */
                strOutput = String.format("Function Name: %s", mstrFunctionName);
            }
            else if (menumDataType == DATATYPES.DATUM_COMPLEX)
            {
                MFPNumeric mfpNumReal = getReal(), mfpNumImage = getImage();
                String strOutputReal = getRealDataClass().output();
                DataClass datumImage = getImageDataClass(); // datumImage has been deepcopied so that it can be changed later.
                String strRealImageConn = "+";
                if (mfpNumImage.isActuallyNegative())    {
                    datumImage.setDataValue((MFPNumeric.MINUS_ONE).multiply(datumImage.getDataValue()),
                            datumImage.getDataType());
                    strRealImageConn = "-";
                }
                String strOutputImage = datumImage.output();
                if (mfpNumReal.isActuallyZero() && mfpNumImage.isActuallyZero())    {
                    strOutput = "0";
                } else if (mfpNumReal.isActuallyZero())    {
                    if (strOutputImage.equals("1"))    {
                        strOutput = "i";
                    } else    {    // nani and infi are supported
                        strOutput = strOutputImage + "i";
                    }
                    if (mfpNumImage.isActuallyNegative())    {
                        strOutput = strRealImageConn + strOutput;
                    }
                } else if (mfpNumImage.isActuallyZero())    {
                    strOutput = strOutputReal;
                } else    {
                    if (MFPNumeric.isEqual(datumImage.getDataValue(), MFPNumeric.ONE))    {
                        strOutput = strOutputReal + " " + strRealImageConn + " " + "i";
                    } else {    // nani and infi are supported.
                        strOutput = strOutputReal + " " + strRealImageConn + " " + strOutputImage + "i";
                    }
                }
            }
            else if (menumDataType == DATATYPES.DATUM_REF_DATA)
            {
                strOutput = "[";
                for (int index = 0; index < getDataListSize(); index ++)
                {
                    if (index == (getDataListSize() - 1))
                    {
                        strOutput += (mdataList[index] == null)?
                                (new DataClass()).output():mdataList[index].output();
                    }
                    else
                    {
                        strOutput += ((mdataList[index] == null)?
                                (new DataClass()).output():mdataList[index].output()) + ", ";
                    }
                }
                strOutput += "]";
            }
            else if (menumDataType == DATATYPES.DATUM_ABSTRACT_EXPR)
            {
                try {
                    strOutput = maexpr.output();
                } catch (JSmartMathErrException e) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_DATA_ABSTRACT_EXPR);
                }
            }
            return strOutput;
        }
    };

    public static enum OPERATORTYPES
    {
        /* invalid operator */
        OPERATOR_NOTEXIST(0),  

        /* assign a value to a variable */
        OPERATOR_ASSIGN(1),
        
        /* judge if two operands are equal */
        OPERATOR_EQ(2),
        /* judge if two operands are not equal */
        OPERATOR_NEQ(3),
        /* judge if operand a is larger than b */
        OPERATOR_LARGER(4),
        /* judge if operand a is smaller than b */
        OPERATOR_SMALLER(5),
        /* judge if operand a is no larger than b */
        OPERATOR_NOLARGER(6),
        /* judge if operand a is no smaller than b */
        OPERATOR_NOSMALLER(7),

        /* bit AND */
        OPERATOR_AND(8),  
        /* bit OR */
        OPERATOR_OR(9),    
        /* bit XOR */
        OPERATOR_XOR(10),   

        /* + */
        OPERATOR_ADD(11),
        /* - */
        OPERATOR_SUBTRACT(12),
        /* positive number, only one operand */
        OPERATOR_POSSIGN(13),   
        /* negative number, only one operand */
        OPERATOR_NEGSIGN(14),   


        /* * */
        OPERATOR_MULTIPLY(15),  
        /* / */
        OPERATOR_DIVIDE(16),    
        /* \ (left division for matrix) */
        OPERATOR_LEFTDIVIDE(17),
        
        
        /* **: power */
        OPERATOR_POWER(18), 

        /* equal to FALSE or not, only one operand */
        OPERATOR_FALSE(19),   
        /* bit NOT, only one operand */
        OPERATOR_NOT(20),   
        
        /* factorial */
        OPERATOR_FACTORIAL(21), 
        /* %: percentage */
        OPERATOR_PERCENT(22),
        /* ': transpose of an at most 2-D matrix */
        OPERATOR_TRANSPOSE(23),

        /* ( */
        OPERATOR_LEFTPARENTHESE(24),    
        /* ) */
        OPERATOR_RIGHTPARENTHESE(25),   
        OPERATOR_STARTEND(26);
        
        private int value; 

        private OPERATORTYPES(int i) { 
            value = i; 
        } 

        public int getValue() { 
            return value; 
        } 
        
        public String output() throws JFCALCExpErrException {
            if (value == OPERATOR_NOTEXIST.getValue())    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERATOR_NOT_EXIST);
            } else if (value == OPERATOR_ASSIGN.getValue())    {
                return "=";
            } else if (value == OPERATOR_EQ.getValue())    {
                return "==";
            } else if (value == OPERATOR_NEQ.getValue())    {
                return "!=";
            } else if (value == OPERATOR_LARGER.getValue())    {
                return ">";
            } else if (value == OPERATOR_SMALLER.getValue())    {
                return "<";
            } else if (value == OPERATOR_NOLARGER.getValue())    {
                return "<=";
            } else if (value == OPERATOR_NOSMALLER.getValue())    {
                return ">=";
            } else if (value == OPERATOR_ADD.getValue())    {
                return "+";
            } else if (value == OPERATOR_SUBTRACT.getValue())    {
                return "-";
            } else if (value == OPERATOR_MULTIPLY.getValue())    {
                return "*";
            } else if (value == OPERATOR_DIVIDE.getValue())    {
                return "/";
            } else if (value == OPERATOR_LEFTDIVIDE.getValue())    {
                return "\\";
            } else if (value == OPERATOR_AND.getValue())    {
                return "&";
            } else if (value == OPERATOR_OR.getValue())    {
                return "|";
            } else if (value == OPERATOR_XOR.getValue())    {
                return "^";
            } else if (value == OPERATOR_POWER.getValue())    {
                return "**";
            } else if (value == OPERATOR_POSSIGN.getValue())    {
                return "+";
            } else if (value == OPERATOR_NEGSIGN.getValue())    {
                return "-";
            } else if (value == OPERATOR_FALSE.getValue())    {
                return "!";
            } else if (value == OPERATOR_NOT.getValue())    {
                return "~";
            } else if (value == OPERATOR_FACTORIAL.getValue())    {
                return "!";
            } else if (value == OPERATOR_PERCENT.getValue())    {
                return "%";
            } else if (value == OPERATOR_TRANSPOSE.getValue())    {
                return "'";
            } else if (value == OPERATOR_LEFTPARENTHESE.getValue())    {
                return "(";
            } else if (value == OPERATOR_RIGHTPARENTHESE.getValue())    {
                return ")";
            } else if (value == OPERATOR_STARTEND.getValue())    {
                return ";";
            } else    {   // highest priority
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_OPERATOR_NOT_EXIST);
            }
        }
    };  

    public static class BoundOperator
    {
        private OPERATORTYPES m_OPTRBoundOperator;  
        private int m_nOperatorLevel;  
        public BoundOperator() 
        {
            m_OPTRBoundOperator = OPERATORTYPES.OPERATOR_NOTEXIST;
            m_nOperatorLevel = 0;
        }
        public BoundOperator(int nStartPosition,  
                           int nEndPosition,    
                           OPERATORTYPES OPTRBoundOperator, 
                           int nOperatorLevel)   
        {
            setBoundOperator(nStartPosition, nEndPosition, OPTRBoundOperator, nOperatorLevel);
        }    
            
        private void setBoundOperator(int nStartPosition,  
                                   int nEndPosition,    
                                   OPERATORTYPES OPTRBoundOperator, 
                                   int nOperatorLevel)  
        {
            m_OPTRBoundOperator = OPTRBoundOperator;
            m_nOperatorLevel = nOperatorLevel;
        }
        public boolean isEqual(OPERATORTYPES OPTRBoundOperator) 
        {
            if(m_OPTRBoundOperator == OPTRBoundOperator)
                return true;
            return false;
        }    
        public boolean isEqual(int nOperatorLevel)  
        {
            if(m_nOperatorLevel == nOperatorLevel)
                return true;
            return false;
        }    
        public OPERATORTYPES getOperatorType()
        {
            return m_OPTRBoundOperator;
        }
        public int getOperatorLevel()
        {
            return m_nOperatorLevel;
        }
    }

    public static class CalculateOperator   // this class should be immutable.
    {
        private OPERATORTYPES m_OPTRCalcOperator;   
        private int m_nOperandNum;  
        private boolean m_boolLabelPrefix; 
        public CalculateOperator()
        {
            m_OPTRCalcOperator = OPERATORTYPES.OPERATOR_NOTEXIST;
            m_nOperandNum = -1;
            m_boolLabelPrefix = true;
        }
        public CalculateOperator(OPERATORTYPES OPTRCalcOperator,  
                                int nOperandNum, 
                                boolean boolLabelPrefix)  
        {
            setCalculateOperator(OPTRCalcOperator, nOperandNum, boolLabelPrefix);
        }    
        public CalculateOperator(OPERATORTYPES OPTRCalcOperator,  
                                int nOperandNum)  
        {
            setCalculateOperator(OPTRCalcOperator, nOperandNum, true);
        }    
            
        private void setCalculateOperatorValue(OPERATORTYPES OPTRCalcOperator,
                                            int nOperandNum,
                                            boolean boolLabelPrefix)
        {
            m_OPTRCalcOperator = OPTRCalcOperator;
            m_nOperandNum = nOperandNum;
            m_boolLabelPrefix = boolLabelPrefix;
        }    
        private void setCalculateOperatorValue(OPERATORTYPES OPTRCalcOperator,
                                            int nOperandNum)
        {
            m_OPTRCalcOperator = OPTRCalcOperator;
            m_nOperandNum = nOperandNum;
            m_boolLabelPrefix = true;
        }

        private void setCalculateOperator(OPERATORTYPES OPTRCalcOperator,
                                        int nOperandNum,
                                        boolean boolLabelPrefix)
        {
            setCalculateOperatorValue(OPTRCalcOperator, nOperandNum, boolLabelPrefix);
        }    
            
        private void setCalculateOperator(OPERATORTYPES OPTRCalcOperator,  
                                       int nOperandNum)
        {
            setCalculateOperatorValue(OPTRCalcOperator, nOperandNum, true);
            
        }
            
        public boolean isEqual(OPERATORTYPES OPTRCalcOperator)  
        {
            if(m_OPTRCalcOperator == OPTRCalcOperator)
                return true;
            return false;
        }
        
        public OPERATORTYPES getOperatorType()
        {
            return m_OPTRCalcOperator;
        }
        public int getOperandNum()
        {
            return m_nOperandNum;
        }   
        public boolean getLabelPrefix()
        {
            return m_boolLabelPrefix;
        }
    }
    
}
