/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Locale;

/**
 *
 * @author tonyc
 */
public class MFPDateTime {
    public static DataClass getNowTS(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 0)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        long lTS = System.currentTimeMillis();
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(lTS));
    }
    
    public static DataClass getTS(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() > 7)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        String strTime = "1970-01-01 00:00:00.0";
        if (listParams.size() == 1 && listParams.getFirst().getDataType() == DATATYPES.DATUM_STRING) {
            DataClass datum = listParams.removeLast();
            strTime = datum.getStringValue();
        } else {
            int nYear = 1970, nMonth = 1, nDay = 1, nHour = 0, nMinute = 0, nSecond = 0, nMilliSecond = 0;
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nYear = datum.getDataValue().intValue();
            }
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nMonth = datum.getDataValue().intValue();
            }
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nDay = datum.getDataValue().intValue();
            }
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nHour = datum.getDataValue().intValue();
            }
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nMinute = datum.getDataValue().intValue();
            }
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nSecond = datum.getDataValue().intValue();
            }
            if (listParams.size() > 0) {
                DataClass datum = listParams.removeLast();
                datum.changeDataType(DATATYPES.DATUM_INTEGER);
                nMilliSecond = datum.getDataValue().intValue();
            }
            try {
                StringBuilder sb = new StringBuilder();
                Formatter formatter = new Formatter(sb, Locale.US);
                String strFormat = "%04d-%02d-%02d %02d:%02d:%02d.%03d";
                formatter.format(strFormat, nYear, nMonth, nDay, nHour, nMinute, nSecond, nMilliSecond);
                strTime = sb.toString();
            } catch(Exception e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER_RANGE);
            }
        }
        try {
            Timestamp ts = Timestamp.valueOf(strTime);
            long lTS = ts.getTime();
            return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(lTS));
        } catch(IllegalArgumentException e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
    }
    
    public static DataClass getYear(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nYear = cal.get(Calendar.YEAR);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nYear));
    }
    
    public static DataClass getMonth(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.MONTH);
        int nMonth = 1;
        switch (nReturn) {
            case (Calendar.JANUARY): {
                nMonth = 1;
                break;
            } case (Calendar.FEBRUARY): {
                nMonth = 2;
                break;
            } case (Calendar.MARCH): {
                nMonth = 3;
                break;
            } case (Calendar.APRIL): {
                nMonth = 4;
                break;
            } case (Calendar.MAY): {
                nMonth = 5;
                break;
            } case (Calendar.JUNE): {
                nMonth = 6;
                break;
            } case (Calendar.JULY): {
                nMonth = 7;
                break;
            } case (Calendar.AUGUST): {
                nMonth = 8;
                break;
            } case (Calendar.SEPTEMBER): {
                nMonth = 9;
                break;
            } case (Calendar.OCTOBER): {
                nMonth = 10;
                break;
            } case (Calendar.NOVEMBER): {
                nMonth = 11;
                break;
            } case (Calendar.DECEMBER): {
                nMonth = 12;
                break;
            }
        }
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nMonth));
    }
    
    public static DataClass getDayOfYear(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.DAY_OF_YEAR);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
    
    public static DataClass getDayOfMonth(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.DATE);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
    
    public static DataClass getDayOfWeek(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.DAY_OF_WEEK);
        int nDayOfWeek = 0;
        switch (nReturn) {
            case (Calendar.SUNDAY): {
                nDayOfWeek = 0;
                break;
            } case (Calendar.MONDAY): {
                nDayOfWeek = 1;
                break;
            } case (Calendar.TUESDAY): {
                nDayOfWeek = 2;
                break;
            } case (Calendar.WEDNESDAY): {
                nDayOfWeek = 3;
                break;
            } case (Calendar.THURSDAY): {
                nDayOfWeek = 4;
                break;
            } case (Calendar.FRIDAY): {
                nDayOfWeek = 5;
                break;
            } case (Calendar.SATURDAY): {
                nDayOfWeek = 6;
                break;
            }
        }
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nDayOfWeek));
    }
    
    public static DataClass getHour(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.HOUR_OF_DAY);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
    
    public static DataClass getMinute(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.MINUTE);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
    
    public static DataClass getSecond(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.SECOND);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
    
    public static DataClass getMilliSecond(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumTS = new DataClass();
        datumTS.copyTypeValueDeep(listParams.removeLast());
        datumTS.changeDataType(DATATYPES.DATUM_INTEGER);
        long lTS = datumTS.getDataValue().longValue();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lTS);
        int nReturn = cal.get(Calendar.MILLISECOND);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
}
