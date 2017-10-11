/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.BaseData.DATATYPES;
import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FileDescriptor.BufferedInputStream;
import com.cyzapps.Jfcalc.FileDescriptor.BufferedString;
import com.cyzapps.Jsma.AEInvalid;
import com.cyzapps.adapter.MFPAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tonyc
 */
public class IOLib {

    public static FileDescriptor[] msfileDescriptors = new FileDescriptor[MFPAdapter.MAX_NUMBER_OF_OPEN_FILES];
    public static int msnLastFDIdx = 0;
    
    public static String msstrWorkingDir = ".";
    
    // this class include basic file io functions including
    // fopen
    // fclose
    // fread
    // fwrite
    // fprintf
    // fscanf
    // feof
    
    public static DataClass fOpen(LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (listParams.size() != 2 && listParams.size() != 3)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumFileName = new DataClass();
        datumFileName.copyTypeValueDeep(listParams.removeLast());
        datumFileName.changeDataType(DATATYPES.DATUM_STRING);
        String strFileFullPath = datumFileName.getStringValue();
        
        DataClass datumFileOpenMode = new DataClass();
        datumFileOpenMode.copyTypeValueDeep(listParams.removeLast());
        datumFileOpenMode.changeDataType(DATATYPES.DATUM_STRING);
        String strFileOpenMode = datumFileOpenMode.getStringValue();
        
        String strFileEncoding = System.getProperty("file.encoding","UTF-8");
        if (listParams.size() > 0) {
            DataClass datumFileEncoding = new DataClass();
            datumFileEncoding.copyTypeValueDeep(listParams.removeLast());
            datumFileEncoding.changeDataType(DATATYPES.DATUM_STRING);
            strFileEncoding = datumFileEncoding.getStringValue();
        }
        
        int nFileId = fOpen(strFileFullPath, strFileOpenMode, strFileEncoding);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nFileId));
    }
    
    public static int fOpen(String strFileFullPath, String strFileOpenMode) throws JFCALCExpErrException {
        String strTextEncoding = System.getProperty("file.encoding","UTF-8");
        return fOpen(strFileFullPath, strFileOpenMode, strTextEncoding);
    }
    
    public static int fOpen(String strFileFullPath, String strFileOpenMode, String strTextEncoding) throws JFCALCExpErrException    {
        int nIdxPathDiv = strFileFullPath.lastIndexOf(MFPAdapter.STRING_PATH_DIVISOR);
        String strFileName = "", strFilePath = "";
        if (nIdxPathDiv == -1) {
            strFileName = strFileFullPath;
            strFilePath = "";
        } else {
            strFileName = strFileFullPath.substring(nIdxPathDiv + 1);
            strFilePath = strFileFullPath.substring(0, nIdxPathDiv);
        }
        
        int nOpenMode = -1;
        boolean bAppend = true;
        if (strFileOpenMode.equals("r") || strFileOpenMode.equals("r+")) {
            nOpenMode = 0;
            bAppend = false;
        } else if (strFileOpenMode.equals("w") || strFileOpenMode.equals("w+")) {
            nOpenMode = 1;
            bAppend = false;
        } else if (strFileOpenMode.equals("a") || strFileOpenMode.equals("a+")) {
            nOpenMode = 1;
            bAppend = true;
        } else if (strFileOpenMode.equals("rb") || strFileOpenMode.equals("r+b") || strFileOpenMode.equals("rb+") || strFileOpenMode.equals("br+")) {
            nOpenMode = 2;
            bAppend = false;            
        } else if (strFileOpenMode.equals("wb") || strFileOpenMode.equals("w+b") || strFileOpenMode.equals("wb+") || strFileOpenMode.equals("bw+")) {
            nOpenMode = 3;
            bAppend = false;
        } else if (strFileOpenMode.equals("ab") || strFileOpenMode.equals("a+b") || strFileOpenMode.equals("ab+") || strFileOpenMode.equals("ba+")) {
            nOpenMode = 3;
            bAppend = true;
        } else {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE_OPEN_MODE);
        }
        
        // FD always starts from 1, not 0.
        int nFileId = 0;
        int idx = 0;
        for (idx = 0; idx < msfileDescriptors.length; idx ++) {
            int nFDIdx = (idx + msnLastFDIdx)%msfileDescriptors.length;
            if (nFDIdx == 0) {
                continue;
            }
            if (msfileDescriptors[nFDIdx] != null && System.currentTimeMillis() - msfileDescriptors[nFDIdx].getLastAccessedTime() > MFPAdapter.FD_EXPIRY_TIME)  {   // fd expiry time is 1 hour
                if (msfileDescriptors[nFDIdx].mbufferedString != null) {
                    try {
                        msfileDescriptors[nFDIdx].mbufferedString.getBufferedReader().close();
                    } catch (IOException ex) {
                        Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (msfileDescriptors[nFDIdx].moutputStreamWriter != null) {
                    try {
                        msfileDescriptors[nFDIdx].moutputStreamWriter.close();
                    } catch (IOException ex) {
                        Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (msfileDescriptors[nFDIdx].mbufferedInputStream != null) {
                    try {
                        msfileDescriptors[nFDIdx].mbufferedInputStream.close();
                    } catch (JFCALCExpErrException ex) {    // do not throw exception here.
                        Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (msfileDescriptors[nFDIdx].mfileOutputStream != null) {
                    try {
                        msfileDescriptors[nFDIdx].mfileOutputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                msfileDescriptors[nFDIdx] = null;
            }
            if (msfileDescriptors[nFDIdx] == null) {
                msnLastFDIdx = nFileId = nFDIdx;
                break;
            }
        }
        if (nFileId == 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_TOO_MANY_OPEN_FILES);
        }
        
        // now start to open the file
        String strFileAbsPath = strFileFullPath;
        File fp = getFileFromCurrentDir(strFileFullPath);
        try {
            strFileAbsPath = fp.getAbsolutePath();
        } catch(Exception e) {
        }
        
        String strFileCanPath = strFileAbsPath;
        try {
        	strFileCanPath = fp.getCanonicalPath();
        } catch(Exception e) {
        }
        if (nOpenMode == 0) {   
            // read text file
            try {
                BufferedString bufferedString = new BufferedString(new BufferedReader(
                        new InputStreamReader(new FileInputStream(strFileAbsPath), strTextEncoding)));
                msfileDescriptors[nFileId] = new FileDescriptor(nFileId, strFileName, strFilePath, strFileFullPath, strFileAbsPath, strFileCanPath, bufferedString, null, null, null);
            } catch (FileNotFoundException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_FILE_NOT_FOUND);
            } catch (Exception ex1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
            }
        } else if (nOpenMode == 1) {
            // write text file
            try {
                if (fp.getParentFile() != null) {
                    fp.getParentFile().mkdirs();
                }
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                        new FileOutputStream(strFileAbsPath, bAppend), strTextEncoding);
                msfileDescriptors[nFileId] = new FileDescriptor(nFileId, strFileName, strFilePath, strFileFullPath, strFileAbsPath, strFileCanPath, null, outputStreamWriter, null, null);
            } catch (FileNotFoundException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_FILE_NOT_FOUND);
            } catch (IOException ex1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
            } catch (Exception ex2) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
            }
        }else if (nOpenMode == 2) {   
            // read binary file
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(strFileAbsPath));
                msfileDescriptors[nFileId] = new FileDescriptor(nFileId, strFileName, strFilePath, strFileFullPath, strFileAbsPath, strFileCanPath, null, null, bufferedInputStream, null);
            } catch (FileNotFoundException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_FILE_NOT_FOUND);
            } catch (Exception ex1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
            }
        } else if (nOpenMode == 3) {
            // write binary file
            try {
                if (fp.getParentFile() != null) {
                    fp.getParentFile().mkdirs();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(strFileAbsPath, bAppend);
                msfileDescriptors[nFileId] = new FileDescriptor(nFileId, strFileName, strFilePath, strFileFullPath, strFileAbsPath, strFileCanPath, null, null, null, fileOutputStream);
            } catch (FileNotFoundException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_FILE_NOT_FOUND);
            } catch (IOException ex1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
            } catch (Exception ex2) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_OPEN_FILE);
            }
        }
        return nFileId;
    }
    
    public static DataClass fClose(LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumFD = new DataClass();
        datumFD.copyTypeValueDeep(listParams.removeLast());
        datumFD.changeDataType(DATATYPES.DATUM_INTEGER);
        int idxFD = datumFD.getDataValue().intValue();
        int nReturn = fClose(idxFD);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }    
        
    public static int fClose(int idxFD) throws JFCALCExpErrException    {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        FileDescriptor fd = msfileDescriptors[idxFD];
        if (fd == null) {
            return -1;
        }
        //fd.setLastAccessedTime();   // need not to update last accessed time as we are gonna close it anyway.

        if (fd.mbufferedString != null) {
            try {
                fd.mbufferedString.getBufferedReader().close();
            } catch (IOException ex) {
                Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (fd.moutputStreamWriter != null) {
            try {
                fd.moutputStreamWriter.close();
            } catch (IOException ex) {
                Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (fd.mbufferedInputStream != null) {
            try {
                fd.mbufferedInputStream.close();
            } catch (JFCALCExpErrException ex) {    // do not throw exception here.
                Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (fd.mfileOutputStream != null) {
            try {
                fd.mfileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(IOLib.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        msfileDescriptors[idxFD] = null;
        return 0;
    }

    public static DataClass fEof(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumFD = new DataClass();
        datumFD.copyTypeValueDeep(listParams.removeLast());
        datumFD.changeDataType(DATATYPES.DATUM_INTEGER);
        int idxFD = datumFD.getDataValue().intValue();
        boolean bReturn = fEof(idxFD);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
        
    public static boolean fEof(int idxFD) throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        FileDescriptor fd = msfileDescriptors[idxFD];
        if (fd == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        fd.setLastAccessedTime();   // update last accessed time
        if (fd.moutputStreamWriter != null || fd.mfileOutputStream != null) {
            return false;   // if we write, we always not in the end of a file.
        } else if (fd.mbufferedString != null) {
            return fd.mbufferedString.isEnd(false);
        } else if (fd.mbufferedInputStream != null) {
            return fd.mbufferedInputStream.isEnd();
        }
        
        return false;
    }
    
    public static DataClass fRead(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1 && listParams.size() != 2 && listParams.size() != 4)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumFD = new DataClass();
        datumFD.copyTypeValueDeep(listParams.removeLast());
        datumFD.changeDataType(DATATYPES.DATUM_INTEGER);
        
        int idxFD = datumFD.getDataValue().intValue();
        DataClass dataBuffer = null;
        byte[] byteArray = null;
        int nOffset = -1, nCopyLen = -1;
        if (!listParams.isEmpty()) {
            // now need to store read bytes in the buffer
            dataBuffer = new DataClass();
            dataBuffer.copyTypeValue(listParams.removeLast());  // shouldn't copy deep here because we want to store something in buffer.
            if (dataBuffer.getDataType() != DATATYPES.DATUM_REF_DATA) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
            byteArray = new byte[dataBuffer.getDataListSize()];
            if (!listParams.isEmpty()) {
                DataClass datumOffset = new DataClass();
                datumOffset.copyTypeValueDeep(listParams.removeLast());
                datumOffset.changeDataType(DATATYPES.DATUM_INTEGER);
                nOffset = datumOffset.getDataValue().intValue();
                DataClass datumLen = new DataClass();
                datumLen.copyTypeValueDeep(listParams.removeLast());
                nCopyLen = datumLen.getDataValue().intValue();
                if (nCopyLen < 0 || nOffset < 0 || nCopyLen > byteArray.length - nOffset) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
                }
            }
        }
        if (byteArray == null) {
            int nReturn = fRead(idxFD);
            DataClass datumReturn = new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
            return datumReturn;
        } else if (nCopyLen < 0 || nOffset < 0) {
            int nReadBytes = fRead(idxFD, byteArray);
            for (int idx = 0; idx < nReadBytes; idx ++) {
                dataBuffer.getDataList()[idx].setDataValue(new MFPNumeric(byteArray[idx]), DATATYPES.DATUM_INTEGER);
            }
            DataClass datumReturn = new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReadBytes));
            return datumReturn;
        } else {
            int nReadBytes = fRead(idxFD, byteArray, nOffset, nCopyLen);
            for (int idx = nOffset; idx < nOffset + nCopyLen; idx ++) {
                dataBuffer.getDataList()[idx].setDataValue(new MFPNumeric(byteArray[idx]), DATATYPES.DATUM_INTEGER);
            }
            DataClass datumReturn = new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReadBytes));
            return datumReturn;
        }
    }
    
    public static int fRead(int idxFD) throws JFCALCExpErrException {
        return fRead(idxFD, null, -1, -1);
    }
    public static int fRead(int idxFD, byte[] byteArray) throws JFCALCExpErrException {
        return fRead(idxFD, byteArray, -1, -1);
    }
    public static int fRead(int idxFD, byte[] byteArray, int nOffset, int nCopyLen) throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }
        if (byteArray != null && nCopyLen > 0 && nOffset > 0 && nCopyLen > byteArray.length - nOffset) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        
        FileDescriptor fd = msfileDescriptors[idxFD];
        if (fd == null || fd.mbufferedInputStream == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        fd.setLastAccessedTime();   // update last accessed time
        
        if (byteArray == null) {
            // read only one byte.
            int content = -1;
            content = fd.mbufferedInputStream.read();
            return content;
        } else if (nOffset < 0 || nCopyLen < 0) {
            // copy to the whole buffer.
            int nReadBytes = -1;
            nReadBytes = fd.mbufferedInputStream.read(byteArray);
            return nReadBytes;
        } else {
            // copy nCopyLen bytes to buffer from nOffset
            int nReadBytes = -1;
            nReadBytes = fd.mbufferedInputStream.read(byteArray, nOffset, nCopyLen);
            return nReadBytes;
        }
    }
    
    public static void fWrite(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 2 && listParams.size() != 4)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumFD = new DataClass();
        datumFD.copyTypeValueDeep(listParams.removeLast());
        datumFD.changeDataType(DATATYPES.DATUM_INTEGER);
        int idxFD = datumFD.getDataValue().intValue();
        
        DataClass dataBuffer = new DataClass(), datumOutput = null;
        dataBuffer.copyTypeValue(listParams.removeLast());  // we can copy deep here but not necessary because we will not change any elements in dataBuffer.
        if (dataBuffer.getDataType() != DATATYPES.DATUM_REF_DATA) { // must be a single byte.
            datumOutput = new DataClass();
            datumOutput.copyTypeValueDeep(dataBuffer);
            dataBuffer = null;
        }
        
        int nOffset = -1, nCopyLen = -1;
        if (!listParams.isEmpty()) {
            DataClass datumOffset = new DataClass();
            datumOffset.copyTypeValueDeep(listParams.removeLast());
            datumOffset.changeDataType(DATATYPES.DATUM_INTEGER);
            nOffset = datumOffset.getDataValue().intValue();
            DataClass datumLen = new DataClass();
            datumLen.copyTypeValueDeep(listParams.removeLast());
            nCopyLen = datumLen.getDataValue().intValue();
            if (nOffset < 0 || nCopyLen < 0 || dataBuffer == null || nCopyLen > dataBuffer.getDataListSize() - nOffset) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            }
        }
        if (datumOutput != null) {
            fWrite(idxFD, datumOutput.getDataValue().byteValue());
        } else if (nOffset < 0 || nCopyLen < 0) {
            byte[] byteArray = new byte[dataBuffer.getDataListSize()];
            for (int idx = 0; idx < byteArray.length; idx ++) {
                byteArray[idx] = dataBuffer.getDataList()[idx].getDataValue().byteValue();
            }
            fWrite(idxFD, byteArray);
        } else {
            byte[] byteArray = new byte[dataBuffer.getDataListSize()];
            for (int idx = nOffset; idx < nOffset + nCopyLen; idx ++) {
                byteArray[idx] = dataBuffer.getDataList()[idx].getDataValue().byteValue();
            }
            fWrite(idxFD, byteArray, nOffset, nCopyLen);
        }
    }
    
    public static void fWrite(int idxFD, byte byteValue) throws JFCALCExpErrException {
        byte[] byteArray = new byte[1];
        byteArray[0] = byteValue;
        fWrite(idxFD, byteArray, -1, -1);
    }
    
    public static void fWrite(int idxFD, byte[] byteArray) throws JFCALCExpErrException {
        fWrite(idxFD, byteArray, -1, -1);
    }
    
    public static void fWrite(int idxFD, byte[] byteArray, int nOffset, int nCopyLen) throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }
        
        FileDescriptor fd = msfileDescriptors[idxFD];
        if (fd == null || fd.mfileOutputStream == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        fd.setLastAccessedTime();   // update last accessed time

        if (nCopyLen > 0 && nOffset > 0 && nCopyLen > byteArray.length - nOffset) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
        }
        if (nOffset < 0 || nCopyLen < 0) {
            // copy from the whole buffer.
            try {
                fd.mfileOutputStream.write(byteArray);
                return;
            } catch (IOException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_WRITE_FILE);
            }
        } else {
            // copy nCopyLen bytes from buffer from nOffset
            try {
                fd.mfileOutputStream.write(byteArray, nOffset, nCopyLen);
                return;
            } catch (IOException e) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_WRITE_FILE);
            }
        }
    }
    
    protected static void sIndividualOutputf(Formatter formatter, String strSubFormat, LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        DataClass datumThisOutput = new DataClass();
        String strArguIdx = "";
        String strFlag = "";
        String strWidth = "";
        char cConversion = 0;  // only conversion is used here.
        double dConversionType = -1;   // 0 means boolean, 1 means integer, 1.1 means h or H, 2 means double, 3 means character and 4 means string
        String strExtraConversion  = "";
        String strRemaining = "";
        if (strSubFormat.length() > 1 && strSubFormat.charAt(0) == '%' && strSubFormat.charAt(1) != '%') {
            for (int idx = 1; idx < strSubFormat.length(); idx ++) {
                if ((strSubFormat.charAt(idx) >= 'a' && strSubFormat.charAt(idx) <= 'z')
                        || (strSubFormat.charAt(idx) >= 'A' && strSubFormat.charAt(idx) <= 'Z')) {
                    cConversion = strSubFormat.charAt(idx);
                    switch (cConversion){
                        case 'b':
                        case 'B':
                            dConversionType = 0;
                            break;
                        case 'h':
                        case 'H':
                            dConversionType = 1.1;
                            break;
                        case 's':
                        case 'S':
                            dConversionType = 4;
                            break;
                        case 'c':
                        case 'C':
                            dConversionType = 3;
                            break;
                        case 'd':
                        case 'o':
                        case 'x':
                        case 'X':
                            dConversionType = 1;
                            break;
                        case 'e':
                        case 'E':
                        case 'f':
                        case 'g':
                        case 'G':
                        case 'a':
                        case 'A':
                            dConversionType = 2;
                            break;
                        default:
                            dConversionType = -1;
                            break;
                    }
                        
                    break;
                }
            }
        }
        try {
            datumThisOutput.copyTypeValueDeep(listParams.removeLast()); // if listParams is empty, throw an exception and will be caught later
            switch(datumThisOutput.getDataType()) {
                case DATUM_DOUBLE: 
                case DATUM_INTEGER: {
                    MFPNumeric mfpValue = datumThisOutput.getDataValue();
                    if ((int)dConversionType == 4) {    // output a string
                        formatter.format(strSubFormat, datumThisOutput.output());
                    } else if (mfpValue.isNan()) {
                        formatter.format(strSubFormat, Double.NaN); // nan can only be printed as double. it cannot be converted to boolean
                    } else if (mfpValue.isPosInf()) {
                        if ((int)dConversionType == 0) {
                            formatter.format(strSubFormat, true);   //inf is true if converted to double.
                        } else {
                            formatter.format(strSubFormat, Double.POSITIVE_INFINITY);   // throw exception if format is unsuccessful.
                        }
                    } else if (mfpValue.isNegInf()) {
                        if ((int)dConversionType == 0) {
                            formatter.format(strSubFormat, true);   //-inf is true if converted to double.
                        } else {
                            formatter.format(strSubFormat, Double.NEGATIVE_INFINITY);   // throw exception if format is unsuccessful.
                        }
                    } else if (datumThisOutput.getDataType() == DATATYPES.DATUM_DOUBLE) {
                        if ((int)dConversionType == 0) {
                            formatter.format(strSubFormat, mfpValue.isActuallyTrue());
                        } else if ((int)dConversionType == 1) { // 1 or 1.1
                            formatter.format(strSubFormat, mfpValue.toBigInteger());
                        } else {    // double or other cases.
                            formatter.format(strSubFormat, mfpValue.toBigDecimal());
                        }
                    } else {    // integer
                        if ((int)dConversionType == 0) {
                            formatter.format(strSubFormat, mfpValue.isActuallyTrue());
                        } else if ((int)dConversionType == 2) {
                            formatter.format(strSubFormat, mfpValue.toBigDecimal());
                        } else {    // integer or other cases.
                            formatter.format(strSubFormat, mfpValue.toBigInteger());
                        }
                    }
                    break;
                } case DATUM_BOOLEAN: {
                    MFPNumeric mfpValue = datumThisOutput.getDataValue();
                    if ((int)dConversionType == 4) {    // output a string
                        formatter.format(strSubFormat, datumThisOutput.output());
                    } else if (mfpValue.isNan()) {    //nan
                        formatter.format(strSubFormat, Double.NaN);
                    } else if ((int)dConversionType == 0) {
                        formatter.format(strSubFormat, mfpValue.isActuallyTrue());
                    } else if ((int)dConversionType == 1) { // 1 or 1.1
                        formatter.format(strSubFormat, mfpValue.isActuallyTrue()?1:0);
                    } else {    // double or other cases.
                        formatter.format(strSubFormat, mfpValue.isActuallyTrue()?1.0:0.0);
                    }
                    break;
                } case DATUM_STRING: {
                    if ((int)dConversionType == 3) {    // output a character
                        if (datumThisOutput.getStringValue().length() == 1) {
                            formatter.format(strSubFormat, datumThisOutput.getStringValue().charAt(0));
                        } else {
                            throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_OUTPUT_FORMAT);
                        }
                    } else {
                        formatter.format(strSubFormat, datumThisOutput.getStringValue());
                    }
                    break;
                } default: {
                    if ((int)dConversionType == 4) {    // output a string
                        formatter.format(strSubFormat, datumThisOutput.output());
                    } else {
                        datumThisOutput.changeDataType(DATATYPES.DATUM_DOUBLE);
                        MFPNumeric mfpValue = datumThisOutput.getDataValue();
                        if ((int)dConversionType == 0) {
                            formatter.format(strSubFormat, mfpValue.isActuallyTrue());
                        } else if ((int)dConversionType == 1) { // 1 or 1.1
                            formatter.format(strSubFormat, mfpValue.toBigInteger());
                        } else {    // double or other cases.
                            formatter.format(strSubFormat, mfpValue.toBigDecimal());
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_OUTPUT_FORMAT);
        }
    }
    
    public static String sPrintf(LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (listParams.size() == 0)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumFormatString = new DataClass();
        datumFormatString.copyTypeValueDeep(listParams.removeLast());
        datumFormatString.changeDataType(DATATYPES.DATUM_STRING);
        String strFormat = datumFormatString.getStringValue();
        return sPrintf(strFormat, listParams);
    }
    
    public static String sPrintf(String strFormat, LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (strFormat.length() == 0) {
            if (listParams.size() > 0) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_OUTPUT_FORMAT);
            } else {
                return "";  // it is printf("");
            }
        }
        
        int nLastStrFormatIdx = 0;
        boolean bPercentInit = false;
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        for (int idx = 0; idx < strFormat.length(); idx ++) {
            if (strFormat.charAt(idx) == '%') {
                if (idx < strFormat.length() - 1 && strFormat.charAt(idx + 1) == '%') {
                    // this is a escape '%'
                    idx ++;
                    continue;
                } else {
                    if (idx > nLastStrFormatIdx) {
                        // this is a start of format input
                        String strSubFormat = strFormat.substring(nLastStrFormatIdx, idx);
                        if (!bPercentInit) {    // just output a string
                            formatter.format(strSubFormat); // need not to worry about exception when format output string.
                        } else {
                            sIndividualOutputf(formatter, strSubFormat, listParams);
                        }
                    }
                    nLastStrFormatIdx = idx;
                    bPercentInit = true;
                }
            }
        }
        String strSubFormat = strFormat.substring(nLastStrFormatIdx, strFormat.length());
        if (!bPercentInit) {    // just output a string
            formatter.format(strSubFormat); // need not to worry about exception when format output string.
        } else {
            sIndividualOutputf(formatter, strSubFormat, listParams);
        }
        if (listParams.size() > 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_OUTPUT_FORMAT);
        }
        return sb.toString();
    }
    
    public static void fPrintf(LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (listParams.size() <= 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }

        DataClass datumFD = new DataClass();
        datumFD.copyTypeValueDeep(listParams.removeLast());
        datumFD.changeDataType(DATATYPES.DATUM_INTEGER);
        int idxFD = datumFD.getDataValue().intValue();
        
        DataClass datumFormat = new DataClass();
        datumFormat.copyTypeValueDeep(listParams.removeLast());
        datumFormat.changeDataType(DATATYPES.DATUM_STRING);
        
        fPrintf(idxFD, datumFormat.getStringValue(), listParams);
    }
    
    public static void fPrintf(int idxFD, String strFormat, LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }
        
        FileDescriptor fd = msfileDescriptors[idxFD];
        if (fd == null || fd.moutputStreamWriter == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        fd.setLastAccessedTime();   // update last accessed time
        
        String strPrinted = sPrintf(strFormat, listParams);
        try {
            fd.moutputStreamWriter.write(strPrinted);
            return;
        } catch (IOException e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_WRITE_FILE);
        }
    }
    
    // cannot use this function to replace getNumber(String strExpression, CurPos curpos)
    public static DataClass readNumber(BufferedString bstrExpression, int nPN, boolean bIgnoreInitial, boolean bGetFloat, boolean bSupportImage) throws JFCALCExpErrException
    {
        // first of all, find positional notation if notation is not explicitly set (nPN == 0).
        if (nPN == 0) {
            nPN = 10;
            if (bstrExpression.getCurrentChar() == '0')   {
                if (bstrExpression.getChar(1) == 'b' || bstrExpression.getChar(1) == 'B')  {
                    nPN = 2;
                } else if (bstrExpression.getChar(1) == 'x' || bstrExpression.getChar(1) == 'X')    {
                    nPN = 16;
                } else if (bstrExpression.getChar(1) >= '0' && bstrExpression.getChar(1) <= '7')    {
                    nPN = 8;
                }
            }
        } else if (nPN != 2 && nPN != 8 && nPN != 16 && nPN != 10) { // can be 2, 8, 10 or 16, other value is invalid.
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_NUMBER_FORMAT);
        }
        
        DataClass datumReturnNum = new DataClass();
        if (nPN == 2 || nPN == 8 || nPN == 16)   {  // these does not support scientific notation like 0.11e11
            MFPNumeric mfpNumCurNum = MFPNumeric.ZERO;
            if (!bIgnoreInitial) {
                for (int idx = 0; idx < ((nPN == 8)?1:2); idx ++) {
                    bstrExpression.moveIdxForward();
                }
            }
            boolean bIsStartPosition = true;
            int nPosAfterDecimalPnt = -1;
            boolean boolIsComplexImage = false;
            while (!bstrExpression.isEnd(true) && boolIsComplexImage == false) {
                char cThis = (char)bstrExpression.getCurrentChar();
                if (ElemAnalyzer.isDigitChar(cThis, nPN)) {
                    int nThisDigVal = cThis - '0';
                    if (nPN == 16)  {
                        if (cThis >= 'A' && cThis <= 'F')   {
                            nThisDigVal = cThis - 'A' + 10;
                        } else if (cThis >= 'a' && cThis <= 'f')    {
                            nThisDigVal = cThis - 'a' + 10;
                        }
                    }
                    if (nPosAfterDecimalPnt < 0)   {
                        mfpNumCurNum = mfpNumCurNum.multiply(new MFPNumeric(nPN)).add(new MFPNumeric(nThisDigVal));
                    } else  {
                        mfpNumCurNum = mfpNumCurNum.add(new MFPNumeric(nThisDigVal).divide(MFPNumeric.pow(new MFPNumeric(nPN), new MFPNumeric(nPosAfterDecimalPnt))));
                    }
                } else if (bGetFloat && cThis == '.')   {
                    if (nPosAfterDecimalPnt == -1)  {
                        int nNextChar = bstrExpression.getChar(1);
                        if (nNextChar == -1 || ElemAnalyzer.isDigitChar((char)nNextChar, nPN) == false) {
                            /* Based on tradition, there should be some digits after the radix point. */
                            break;
                        }
                        nPosAfterDecimalPnt = 0;
                    } else {
                        /* If there are two radix points, then we know it is the end of the number. */
                        break;
                    }
                } else if (bSupportImage && (cThis == 'i' || cThis == 'I'))    {/* otherwise it is i or I */
                    if (bIsStartPosition)    /* this is the first character */
                    {
                        // 0xi is not supported.
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
                    }
                    boolIsComplexImage = true;
                } else  {
                    break;  // we may arrive at the end.
                }
                bstrExpression.moveIdxForward();
                bIsStartPosition = false;
                if (nPosAfterDecimalPnt >= 0)   {
                    nPosAfterDecimalPnt ++;
                }
            }

            if (bIsStartPosition)    {
                // there is no digit following 0x or 0b.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }
            else if (nPosAfterDecimalPnt < 0) /* If there is no radix piont, it is an integer. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_INTEGER, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            else    /* If there is a radix point, it is a flow number. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_DOUBLE, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            if (boolIsComplexImage)    {
                // this is a complex number
                DataClass datumReturnComplex = new DataClass();
                DataClass datumReturnReal = new DataClass();
                datumReturnReal.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                datumReturnComplex.setComplex(datumReturnReal, datumReturnNum);
                datumReturnNum = datumReturnComplex;
            }
        } else  {   // nPN == 10
            MFPNumeric mfpNumCurNum = MFPNumeric.ZERO;
            boolean bIsStartPosition = true;
            if (bstrExpression.getCurrentChar() == '0' && bstrExpression.getChar(1) >= '0' && bstrExpression.getChar(1) <= '9')
            {
                /* if 0 is the initial digit, it must appear exactly before the radix point. Otherwise, we stop at here.*/
                bstrExpression.moveIdxForward();    // should not be the end after moving forward.
                datumReturnNum = new DataClass(DATATYPES.DATUM_INTEGER, MFPNumeric.ZERO);
                return datumReturnNum;
            }

            boolean boolBeforeDecimalPnt = true;
            boolean boolBeforeE = true;
            boolean boolSign4E = false;
            boolean boolIsComplexImage = false;
            String strNumberB4I = "";
            int nCurrentChar = bstrExpression.getCurrentChar();
            while (((nCurrentChar >= '0' && nCurrentChar <= '9')
                    || nCurrentChar == '+' || nCurrentChar == '-' || nCurrentChar == '.'
                    || nCurrentChar == 'e' || nCurrentChar == 'E'
                    || nCurrentChar == 'i' || nCurrentChar == 'I')
                    && !boolIsComplexImage)
            {
                if (nCurrentChar != '.' && nCurrentChar != 'e' && nCurrentChar != 'E'
                    && nCurrentChar != 'i' && nCurrentChar != 'I')
                {
                    if (nCurrentChar == '+' || nCurrentChar == '-')
                    {
                        if (!boolBeforeE && boolSign4E)
                        {
                            boolSign4E = false; // add + or - only if the sign if for E
                            strNumberB4I += (char)nCurrentChar;
                        }
                        else
                        {
                            break;
                        }
                    }
                    else
                    {
                        strNumberB4I += (char)nCurrentChar;
                    }
                }
                else if (bGetFloat && nCurrentChar == '.')    /* If it is a radix point. */
                {
                    if (boolBeforeE == false)  {
                        // after E, there should not be decimal point
                        break;
                    }
                    else if (boolBeforeDecimalPnt)
                    {
                        boolBeforeDecimalPnt = false;
                        int nNextChar = bstrExpression.getChar(1);
                        if (nNextChar == -1 || nNextChar > '9' || nNextChar < '0')
                        {
                            /* Based on tradition, there should be some digits after the radix point. */
                            break;
                        }
                    }
                    else
                    {
                        /* If there are two radix points */
                        break;
                    }
                    strNumberB4I += (char)nCurrentChar;
                }
                else if (bGetFloat && (nCurrentChar == 'E' || nCurrentChar == 'e'))    /* If it is a scientific notation e or E. */
                {
                    if (boolBeforeE)
                    {
                        boolBeforeE = false;
                        int nNextChar = bstrExpression.getChar(1);
                        int nCharAfterNext = bstrExpression.getChar(2);
                        if (nNextChar == -1) {
                            // should be something after 'E'
                            break;
                        } else if (nNextChar == '+' || nNextChar == '-')    {
                            if (nCharAfterNext == -1 || nCharAfterNext > '9' || nCharAfterNext < '0')   {
                                // should be E+... or E-... (... means digits).
                                break;
                            }
                            boolSign4E = true;
                        } else if (nNextChar > '9' || nNextChar < '0')  {
                            /* should be E... (... means digits). */
                            break;
                        }
                    }
                    else
                    {
                        /* Should not be two 'E's. */
                        break;
                    }
                    strNumberB4I += (char)nCurrentChar;
                }
                else if (bSupportImage && (nCurrentChar == 'I' || nCurrentChar == 'i'))    /* otherwise it is i or I */
                {
                    if (bIsStartPosition)    /* this is the first character */
                    {
                        strNumberB4I = "1";
                    }
                    boolIsComplexImage = true;
                }
                else    // it is i or I or e or E or ., but either image is not supported or we dont want to get float
                {
                    break;
                }
                bstrExpression.moveIdxForward();
                bIsStartPosition = false;
                nCurrentChar = bstrExpression.getCurrentChar();
            }
            if (strNumberB4I.length() == 0) {   // no digit in the data string.
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }
            try {
                mfpNumCurNum = new MFPNumeric(strNumberB4I);
            } catch(NumberFormatException e)    {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_NUM_DO_NOT_ACCORD_NUM_WRITING_STANDARD);
            }
            if (boolBeforeDecimalPnt && boolBeforeE) /* If there is no radix piont or Scientific notation, it is an integer. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_INTEGER, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            else    /* If there is a radix point, it is a float number. */
            {
                datumReturnNum.setDataClass(DATATYPES.DATUM_DOUBLE, mfpNumCurNum, "", "", new DataClass[0], AEInvalid.AEINVALID, "");
            }
            if (boolIsComplexImage)    {
                // this is a complex number
                DataClass datumReturnComplex = new DataClass();
                DataClass datumReturnReal = new DataClass();
                datumReturnReal.setDataValue(MFPNumeric.ZERO, DATATYPES.DATUM_INTEGER);
                datumReturnComplex.setComplex(datumReturnReal, datumReturnNum);
                datumReturnNum = datumReturnComplex;
            }
        }
        return datumReturnNum;
    }
        
    public static DataClass sfScanf(LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (listParams.size() != 2)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        
        DataClass datumInput = new DataClass();
        datumInput.copyTypeValueDeep(listParams.removeLast());
        
        DataClass datumFormatString = new DataClass();
        datumFormatString.copyTypeValueDeep(listParams.removeLast());
        datumFormatString.changeDataType(DATATYPES.DATUM_STRING);
        String strFormat = datumFormatString.getStringValue();
        return sfScanf(datumInput, strFormat);
    }
    
    public static DataClass sfScanf(DataClass datumInput, String strFormat) throws JFCALCExpErrException    {
        BufferedString bstrInput;
        if (datumInput.getDataType() == DATATYPES.DATUM_STRING) {
            // if scanf from string
            String strInput = datumInput.getStringValue();
            bstrInput = new BufferedString(strInput);
        } else {
            // if scanf from a file
            datumInput.changeDataType(DATATYPES.DATUM_INTEGER);

            int idxFD = datumInput.getDataValue().intValue();
            if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
            }

            FileDescriptor fd = msfileDescriptors[idxFD];
            if (fd == null || fd.mbufferedString == null) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
            }
            fd.setLastAccessedTime();   // update last accessed time
            bstrInput = fd.mbufferedString;
        }
        
        int idxFormat = 0;
        boolean bIsSpecifier = false;
        LinkedList<DataClass> listReturns = new LinkedList<DataClass>();
        bstrInput.setReadCharLimit(-1);  // initially no read char limit.
        int nCurrentChar = bstrInput.getCurrentChar();
        try {
            while (idxFormat < strFormat.length()) {
                if (!bIsSpecifier) {
                    switch (strFormat.charAt(idxFormat)) {
                        case '\0':
                        case '\n':
                        case '\t':
                        case '\f':
                        case '\r':
                        case ' ': {
                            while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                bstrInput.moveIdxForward();
                                nCurrentChar = bstrInput.getCurrentChar();
                            }
                            idxFormat ++;
                            break;
                        } case '%': {
                            if (idxFormat < strFormat.length() - 1 && strFormat.charAt(idxFormat + 1) == '%') {
                                if (nCurrentChar != '%') {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_INPUT_FORMAT);
                                } else {
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                    idxFormat += 2;
                                }
                            } else if (idxFormat < strFormat.length() - 1) {
                                // we start a specifier
                                bIsSpecifier = true;
                                idxFormat ++;
                            } else {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_INPUT_FORMAT); // incomplete specifier
                            }
                            break;
                        } default: {
                            if (nCurrentChar != strFormat.charAt(idxFormat)) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_INPUT_FORMAT);
                            }
                            bstrInput.moveIdxForward();
                            nCurrentChar = bstrInput.getCurrentChar();
                            idxFormat ++;
                            break;
                        }
                    }
                } else {
                    int nReadCharCnt = -1;
                    if (strFormat.charAt(idxFormat - 1) == '%' && strFormat.charAt(idxFormat) >= '0' && strFormat.charAt(idxFormat) <= '9') {
                        // start number of counts
                        nReadCharCnt = strFormat.charAt(idxFormat) - '0';
                        idxFormat ++;
                        if (nReadCharCnt != 0) {
                            while (idxFormat < strFormat.length() && strFormat.charAt(idxFormat) >= '0' && strFormat.charAt(idxFormat) <= '9') {
                                nReadCharCnt *= 10;
                                nReadCharCnt += strFormat.charAt(idxFormat) - '0';
                                idxFormat ++;
                            }
                        }
                    }
                    bstrInput.setReadCharLimit(nReadCharCnt);
                    DataClass datumThisInput = null;
                    if (idxFormat < strFormat.length()) {   // this is not the tail
                        if (nCurrentChar == -1 && strFormat.charAt(idxFormat) != 's') {
                            break;  // if we are not input a string and we are in the end of input.
                        }
                        // do not look on something like -   3 as -3!
                        switch (strFormat.charAt(idxFormat)) {
                            case 'i': { // integer, including decimal integer, octal integer or hex integer
                                while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                    // skip all the ' ', '\r', '\f', '\t', '\n' and '\0'.
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                }
                                boolean bIsNegative = false;
                                if (nCurrentChar == '+') {
                                    bstrInput.moveIdxForward();
                                } else if (nCurrentChar == '-') {
                                    bstrInput.moveIdxForward();
                                    bIsNegative = true;
                                }
                                datumThisInput = readNumber(bstrInput, 0, false, false, false); // do not ignore initial like 0x or 0
                                if (bIsNegative) {
                                    datumThisInput = BuiltinProcedures.evaluateNegSign(datumThisInput);
                                }
                                idxFormat ++;
                                break;
                            } case 'd': {
                            } case 'u': {   // decimal integer
                                while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                    // skip all the ' ', '\f', '\r', '\t', '\n' and '\0'.
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                }
                                boolean bIsNegative = false;
                                if (nCurrentChar == '+') {
                                    bstrInput.moveIdxForward();
                                } else if (nCurrentChar == '-') {
                                    bstrInput.moveIdxForward();
                                    bIsNegative = true;
                                }
                                datumThisInput = readNumber(bstrInput, 10, true, false, false);
                                if (bIsNegative) {
                                    datumThisInput = BuiltinProcedures.evaluateNegSign(datumThisInput);
                                }
                                idxFormat ++;
                                break;
                            } case 'o': {   // octal integer
                                while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                    // skip all the ' ', '\f', '\r', '\t', '\n' and '\0'.
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                }
                                boolean bIsNegative = false;
                                if (nCurrentChar == '+') {
                                    bstrInput.moveIdxForward();
                                } else if (nCurrentChar == '-') {
                                    bstrInput.moveIdxForward();
                                    bIsNegative = true;
                                }
                                datumThisInput = readNumber(bstrInput, 8, true, false, false);  // ignore intial here
                                if (bIsNegative) {
                                    datumThisInput = BuiltinProcedures.evaluateNegSign(datumThisInput);
                                }
                                idxFormat ++;
                                break;
                            } case 'x': {   // hex integer
                                while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                    // skip all the ' ', '\f', '\r', '\t', '\n' and '\0'.
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                }
                                boolean bIsNegative = false;
                                if (nCurrentChar == '+') {
                                    bstrInput.moveIdxForward();
                                } else if (nCurrentChar == '-') {
                                    bstrInput.moveIdxForward();
                                    bIsNegative = true;
                                }
                                datumThisInput = readNumber(bstrInput, 16, true, false, false); // ignore intial here
                                if (bIsNegative) {
                                    datumThisInput = BuiltinProcedures.evaluateNegSign(datumThisInput);
                                }
                                idxFormat ++;
                                break;
                            } case 'f': {
                            } case 'e': {
                            } case 'g': {
                            } case 'a': {   // float
                                while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                    // skip all the ' ', '\f', '\r', '\t', '\n' and '\0'.
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                }
                                boolean bIsNegative = false;
                                if (nCurrentChar == '+') {
                                    bstrInput.moveIdxForward();
                                } else if (nCurrentChar == '-') {
                                    bstrInput.moveIdxForward();
                                    bIsNegative = true;
                                }
                                datumThisInput = readNumber(bstrInput, 0, false, true, false);
                                datumThisInput.changeDataType(DATATYPES.DATUM_DOUBLE); // datumThisInput can be integer, so convert it to float.
                                if (bIsNegative) {
                                    datumThisInput = BuiltinProcedures.evaluateNegSign(datumThisInput);
                                }
                                idxFormat ++;
                                break;
                            } case 'c': {   // single character string
                                String strThisInput = "" + (char)nCurrentChar;
                                datumThisInput = new DataClass(DATATYPES.DATUM_STRING, strThisInput);
                                bstrInput.moveIdxForward();
                                idxFormat ++;
                                break;
                            } case 's': {   // string finished by blank or \t or \n or \0
                                while (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                    // ' ', '\f', '\r', '\t', '\n' and '\0' are string divisor, no matter how many are they.
                                    bstrInput.moveIdxForward();
                                    nCurrentChar = bstrInput.getCurrentChar();
                                }
                                String strThisInput = "";
                                while (nCurrentChar != -1) {
                                    if (nCurrentChar == ' ' || nCurrentChar == '\r' || nCurrentChar == '\f' || nCurrentChar == '\t' || nCurrentChar == '\n' || nCurrentChar == '\0') {
                                        break;
                                    } else {
                                        strThisInput += (char)nCurrentChar;
                                        bstrInput.moveIdxForward();
                                        nCurrentChar = bstrInput.getCurrentChar();
                                    }
                                }
                                datumThisInput = new DataClass(DATATYPES.DATUM_STRING, strThisInput);
                                idxFormat ++;
                                break;
                            } default: {
                                if (nReadCharCnt != -1) {
                                    for (int idx = 0; idx < nReadCharCnt; idx ++) {
                                        bstrInput.moveIdxForward();
                                    }
                                } else { // read char count is not set, but we don't have a valid conversion.
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_INPUT_FORMAT);
                                }
                            }
                        }
                    } else {
                        // have arrived at the tail of format string. this must mean it is like %20, i.e.
                        // last char is a number and nReadCharCnt != -1. skip the number
                        for (int idx = 0; idx < nReadCharCnt; idx ++) {
                            bstrInput.moveIdxForward();
                        }
                    }
                    bstrInput.removeReadCharLimit();
                    nCurrentChar = bstrInput.getCurrentChar();  // have to update current char after remove read char limit.
                    if (datumThisInput != null) {
                        listReturns.add(datumThisInput);
                    }
                    bIsSpecifier = false;
                }
            }
            if (idxFormat < strFormat.length()) {
                // idxFormat hasn't finished yet
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_ILLEGAL_INPUT_FORMAT);
            }
        } catch (JFCALCExpErrException e) {
            // do not throw any exception, just gives less return data.
        }
        DataClass datumReturn = new DataClass();
        datumReturn.setDataList(listReturns.toArray(new DataClass[0])); // Zero size listReturns is allowed
        return datumReturn;
    }
    
    public static DataClass sScanf(String strInput, String strFormat)  throws JFCALCExpErrException {
        return sfScanf(new DataClass(DATATYPES.DATUM_STRING, strInput), strFormat);
    }
    
    public static DataClass fScanf(int idxFD, String strFormat)  throws JFCALCExpErrException {
        return sfScanf(new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(idxFD)), strFormat);
    }

    public static DataClass fReadLine(LinkedList<DataClass> listParams) throws JFCALCExpErrException    {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        
        DataClass datumFD = new DataClass();
        datumFD.copyTypeValueDeep(listParams.removeLast());
        datumFD.changeDataType(DATATYPES.DATUM_INTEGER);
        int idxFD = datumFD.getDataValue().intValue();
        
        String strLine = fReadLine(idxFD);
        
        if (strLine != null) {
            return new DataClass(DATATYPES.DATUM_STRING, strLine);
        } else {
            DataClass datumReturn = new DataClass();
            datumReturn.changeDataType(DATATYPES.DATUM_NULL);
            return datumReturn;
        }
    }
    
    public static String fReadLine(int idxFD)  throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }

        FileDescriptor fd = msfileDescriptors[idxFD];
        if (fd == null || fd.mbufferedString == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
        }
        fd.setLastAccessedTime();   // update last accessed time
        BufferedString bstrInput = fd.mbufferedString;
        bstrInput.setReadCharLimit(-1);  // no read char limit
        
        return bstrInput.readLine();
    }
    
    public static DataClass createFile(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 2 && listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bIsFolder = false;
        if (listParams.size() > 0) {
            DataClass datumIsFolder = new DataClass();
            datumIsFolder.copyTypeValueDeep(listParams.removeLast());
            datumIsFolder.changeDataType(DATATYPES.DATUM_BOOLEAN);
            bIsFolder = datumIsFolder.getDataValue().booleanValue();
        }
        boolean bReturn = createFile(strPath, bIsFolder);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean createFile(String strPath, boolean bIsFolder) {
        try {
            File f = getFileFromCurrentDir(strPath);
            if (f.getParentFile() != null) {
                f.getParentFile().mkdirs();
            }
            if (bIsFolder) {
                return f.mkdir();
            } else {
                return f.createNewFile();
            }
        } catch (IOException ex) {
            return false;   // something happend
        }
    }
    
    public static DataClass deleteFile(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1 && listParams.size() != 2)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        
        boolean bDeleteFilesInFolder = false;
        if (listParams.size() > 0) {
            DataClass datumDeleteFilesInFolder = new DataClass();
            datumDeleteFilesInFolder.copyTypeValueDeep(listParams.removeLast());
            datumDeleteFilesInFolder.changeDataType(DATATYPES.DATUM_BOOLEAN);
            bDeleteFilesInFolder = datumDeleteFilesInFolder.getDataValue().booleanValue();
        }
        
        boolean bReturn = deleteFile(strPath, bDeleteFilesInFolder);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean deleteFile(String strPath, boolean bDeleteFilesInFolder) {
        File f = getFileFromCurrentDir(strPath);
        try {
            if (!f.exists()) {
                return true;    // file not exist, no need to delete.
            } else if (f.isDirectory() && isSymbolLink(strPath) == false) { // delete symbol link as a file.
                //f = f.getCanonicalFile();   // if path is " ", then child will be something like .../ /..., which is invalid
                // cannot delete " " is fine because " " is current folder.
                boolean bReturn = true;
                File[] files = f.listFiles();
                if (files != null && bDeleteFilesInFolder) { // some JVMs return null for empty dirs
                    for(File fChild: files) {
                        boolean bThisReturn = deleteFile(fChild.getPath(), bDeleteFilesInFolder);
                        if (!bThisReturn) {
                            bReturn = false;
                        }
                    }
                }
                try {
                    boolean bThisReturn = f.delete();
                    if (!bThisReturn) {
                        bReturn = false;    // still continue although single file throws an exception.
                    }
                } catch (Exception e) {
                    bReturn = false;
                }
                return bReturn;
            } else {
                return f.delete();
            }
        } catch (Exception e) {
            return false;   // exception implies that we cannot access the file.
        }       
    }
    
    public static DataClass listFiles(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() > 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        String strPath = ".";
        if (listParams.size() > 0) {
            DataClass datumPath = new DataClass();
            datumPath.copyTypeValueDeep(listParams.removeLast());
            datumPath.changeDataType(DATATYPES.DATUM_STRING);
            strPath = datumPath.getStringValue();
        }
        
        LinkedList<String> listNames = new LinkedList<String>();
        int nReturn = listFiles(strPath, listNames);
        DataClass datumReturn = new DataClass();
        if (nReturn < 0) {
            // the file does not exist or cannot access, so return NULL
            datumReturn.changeDataType(DATATYPES.DATUM_NULL);
        } else if (nReturn == 0) {  // this is a normal file, not a directory
            if (listNames.size() != 1) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
            }
            datumReturn.setStringValue(listNames.getFirst());
        } else {    // this is a folder
            DataClass[] arrayFileNames = new DataClass[listNames.size()];
            for (int idx = 0; idx < listNames.size(); idx ++) {
                DataClass datumThis = new DataClass();
                datumThis.setStringValue(listNames.get(idx));
                arrayFileNames[idx] = datumThis;
            }
            datumReturn.setDataList(arrayFileNames, DATATYPES.DATUM_REF_DATA);
        }
        return datumReturn;
    }
    
    public static int listFiles(String strPath, LinkedList<String> listNames) {
        File f = getFileFromCurrentDir(strPath);
        try {
            if (!f.exists()) {
                return -1;    // file not exist
            } else if (f.isDirectory()) {
                f = f.getCanonicalFile();     // if path is " ", then child will be something like .../ /..., which is invalid          
                File[] files = f.listFiles();
                if (files != null) { // some JVMs return null for empty dirs
                    for(File fChild: files) {
                        String strChildName = fChild.getName();
                        listNames.add(strChildName);
                    }
                }
                return 1;   // folder
            } else {    // this is a file
                String strName = f.getName();
                listNames.add(strName);
                return 0;   // file
            }
        } catch (Exception e) {
            return -1;  // file cannot access.
        }
    }
    
    public static int outputFileList(String strPath, LinkedList<String> listOutputs) {
        File f = getFileFromCurrentDir(strPath);
        try {
            if (!f.exists()) {
                return -1;    // file not exist
            } else if (f.isDirectory()) {
                f = f.getCanonicalFile();    // if path is " ", then child will be something like .../ /..., which is invalid
                File[] files = f.listFiles();
                if (files != null) { // some JVMs return null for empty dirs
                    for(File fChild: files) {
                        try {
                            long lSize = 0;
                            char cIsFolder = 'd';
                            if (fChild.exists() && !fChild.isDirectory()) {
                                lSize = fChild.length();
                                cIsFolder = '-';
                            }
                            //fChild = fChild.getCanonicalFile(); // shouldn't use canonicalFile because it can be a symbol link.
                            String strName = fChild.getName();
                            long lTime = fChild.lastModified();
                            Date date = new Date(lTime);
                            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String strDateFormatted = (lTime >= 0)?dateFormatter.format(date):"????-??-?? ??:??:??";	//== 0 actually means invalid date, but it is allowed here for apperance.
                            char cIsReadable = fChild.canRead()?'r':'-';
                            char cIsWritable = fChild.canWrite()?'w':'-';
                            char cIsExecutable = fChild.canExecute()?'x':'-';
                            StringBuilder sb = new StringBuilder();
                            Formatter stringFormatter = new Formatter(sb, Locale.US);
                            stringFormatter.format("%c%c%c%c    %32s%c   %8d    %s", cIsFolder, cIsReadable, cIsWritable, cIsExecutable, strName,
                                    (cIsFolder == 'd')?MFPAdapter.STRING_PATH_DIVISOR.charAt(0):' ', lSize, strDateFormatted);
                            listOutputs.add(sb.toString());
                        } catch(Exception e) {
                            // ignore this file if we canot format output it.
                        }
                    }
                }
                return (files == null)?0:files.length;   // folder
            } else {    // this is a file
                String strName = f.getName();
                long lSize = f.length();
                long lTime = f.lastModified();
                Date date = new Date(lTime);
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String strDateFormatted = (lTime >= 0)?dateFormatter.format(date):"????-??-?? ??:??:??";	//== 0 actually means invalid date, but it is allowed here for apperance.
                char cIsFolder = '-';
                char cIsReadable = f.canRead()?'r':'-';
                char cIsWritable = f.canWrite()?'w':'-';
                char cIsExecutable = f.canExecute()?'x':'-';
                StringBuilder sb = new StringBuilder();
                Formatter stringFormatter = new Formatter(sb, Locale.US);
                stringFormatter.format("%c%c%c%c    %32s    %8d    %s", cIsFolder, cIsReadable, cIsWritable, cIsExecutable, strName, lSize, strDateFormatted);
                listOutputs.add(sb.toString());
                return 1;   // file
            }
        } catch (Exception e) {
            return -1;  // file cannot access.
        }
    }
    
    public static DataClass copyFile(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() < 2 || listParams.size() > 3)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumSrc = new DataClass();
        datumSrc.copyTypeValueDeep(listParams.removeLast());
        datumSrc.changeDataType(DATATYPES.DATUM_STRING);
        String strSrcPath = datumSrc.getStringValue();
        
        DataClass datumDest = new DataClass();
        datumDest.copyTypeValueDeep(listParams.removeLast());
        datumDest.changeDataType(DATATYPES.DATUM_STRING);
        String strDestPath = datumDest.getStringValue();
        
        boolean bReplaceExisting = false;
        if (listParams.size() > 0) {
            DataClass datumOption = new DataClass();
            datumOption.copyTypeValueDeep(listParams.removeLast());
            datumOption.changeDataType(DATATYPES.DATUM_BOOLEAN);
            bReplaceExisting = datumOption.getDataValue().booleanValue();
        }
        // at this moment, we dont support symbolic link or copy attributes.
        boolean bReturn = copyFile(strSrcPath, strDestPath, bReplaceExisting);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean copyFile(String strSrcPath, String strDestPath, boolean bReplaceExisting) {
        File fSrc = getFileFromCurrentDir(strSrcPath);
        File fDest = getFileFromCurrentDir(strDestPath);
        try {
            if (fSrc.isFile() && fDest.isDirectory()) {
                // destination is a directory, copy into the directory
                strDestPath += MFPAdapter.STRING_PATH_DIVISOR + fSrc.getName();           
            }
            return copyFileRecursive(strSrcPath, strDestPath, bReplaceExisting);
        } catch (Exception e) {
            return false;
        }
    }    
    
    public static boolean copyFileRecursive(String strSrcPath, String strDestPath, boolean bReplaceExisting) {
        // if any file cannot be copied, return false but continue.
        File fSrc = getFileFromCurrentDir(strSrcPath);
        File fDest = getFileFromCurrentDir(strDestPath);
        try {
            boolean bReturnValue = true;
            if (fSrc.exists() == false) {
                return false;
            } else if (comparePath(strSrcPath, strDestPath) == 0) {
                return false;    // cannot copy to itself
            } else if (fSrc.isDirectory())   {
                if (isParentPath(strSrcPath, strDestPath)) {
                    return false;   // cannot copy to its children
                } else if (!fDest.exists())    {
                    if (!fDest.mkdirs())    {
                        return false;    // cannot create destination folder
                    }
                } else if (!fDest.isDirectory()) {
                    // the destination file exists but not a dir, in most of the OSes, file and folder cannot share
                    // the same name. But there are exceptionis. So return false anyway.
                    return false;
                }   // do not worry about replace existing for diretory copy.
                String strChildFiles[] = null;
                strChildFiles = fSrc.list();
                for (int i = 0; i < strChildFiles.length; ++i) {
                    boolean bThisCpyReturn = copyFileRecursive(strSrcPath + MFPAdapter.STRING_PATH_DIVISOR + strChildFiles[i],
                                strDestPath + MFPAdapter.STRING_PATH_DIVISOR + strChildFiles[i], bReplaceExisting);
                    if (!bThisCpyReturn) {
                        // copy failed.
                        bReturnValue = false;
                    }
                }
            } else {
                if (fDest.exists() && !bReplaceExisting) {
                    return false;  // destination does exist but we can not replace existinf ile.
                } else if (fDest.isDirectory()) {
                    // cannot replace dir by a file
                    return false;
                }

                InputStream in = null;
                OutputStream out = null;
                try {
                    in = new FileInputStream(fSrc.getAbsoluteFile());
                    out = new FileOutputStream(fDest.getAbsoluteFile());

                    int read;
                    byte[] byteBuffer = new byte[65536];
                    while ((read = in.read(byteBuffer)) != -1) {
                        out.write(byteBuffer, 0, read);
                    }
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
            return bReturnValue;
        } catch (Exception e) {
            return false;   // exist and isDirectory can both throw security access exception
        }   
    }
    
    public static DataClass moveFile(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() < 2 || listParams.size() > 3)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumSrc = new DataClass();
        datumSrc.copyTypeValueDeep(listParams.removeLast());
        datumSrc.changeDataType(DATATYPES.DATUM_STRING);
        String strSrcPath = datumSrc.getStringValue();
        
        DataClass datumDest = new DataClass();
        datumDest.copyTypeValueDeep(listParams.removeLast());
        datumDest.changeDataType(DATATYPES.DATUM_STRING);
        String strDestPath = datumDest.getStringValue();
        
        boolean bReplaceExisting = false;
        if (listParams.size() > 0) {
            DataClass datumOption = new DataClass();
            datumOption.copyTypeValueDeep(listParams.removeLast());
            datumOption.changeDataType(DATATYPES.DATUM_BOOLEAN);
            bReplaceExisting = datumOption.getDataValue().booleanValue();
        }
        // at this moment, we dont support symbolic link or copy attributes.
        boolean bReturn = moveFile(strSrcPath, strDestPath, bReplaceExisting);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean moveFile(String strSrcPath, String strDestPath, boolean bReplaceExisting) {
        File fSrc = getFileFromCurrentDir(strSrcPath);
        File fDest = getFileFromCurrentDir(strDestPath);
        try {
            if (fDest.isDirectory()) {
                // destination is a directory, copy into the directory
                strDestPath += MFPAdapter.STRING_PATH_DIVISOR + fSrc.getName();           
            }
            boolean bReturn = copyFileRecursive(strSrcPath, strDestPath, bReplaceExisting);
            if (bReturn) {  // only if copy successfully we delete
                bReturn = deleteFile(strSrcPath, true);
            }
            return bReturn;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static DataClass isExistingFile(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isExistingFile(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isExistingFile(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.exists();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
    
    public static DataClass isDirectory(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isDirectory(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isDirectory(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.isDirectory();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass isExecutable(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isExecutable(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isExecutable(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.canExecute();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass isHidden(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isHidden(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isHidden(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.isHidden();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass isReadable(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isReadable(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isReadable(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.canRead();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass isWritable(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isWritable(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isWritable(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.canWrite();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass isNormalFile(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isNormalFile(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isNormalFile(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            return f.isFile();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
    
    public static DataClass isSymbolLink(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isSymbolLink(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    // this function does not support windows mklink
    public static boolean isSymbolLink(String strPath) throws JFCALCExpErrException {
        File f = getFileFromCurrentDir(strPath);
        try {
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
        try {
            File canon;
            if (f.getParent() == null) {
                canon = f;
            } else {
                File canonDir = f.getParentFile().getCanonicalFile();
                canon = new File(canonDir, f.getName());
            }
            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass isAbsolutePath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isAbsolutePath(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isAbsolutePath(String strPath) {
        File f = new File(strPath); // cannot use get file from directory
        return f.isAbsolute();
    }
    
    /* isCanonicalPath cannot work because upper case lower case issue in windows.
     * in other words, windows automatically convert c: to C: when getting canonical path.
    public static DataClass isCanonicalPath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        boolean bReturn = isCanonicalPath(strPath);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isCanonicalPath(String strPath) {
        File f = new File(strPath); // cannot use get file from directory
        try {
            String strCanPath = f.getCanonicalPath();
            return strPath.equals(strCanPath);
        } catch (Exception e) {
            return false;
        }
    } */
        
    public static DataClass getAbsolutePath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumParam = new DataClass();
        datumParam.copyTypeValueDeep(listParams.removeLast());
        if (datumParam.getDataType() == DATATYPES.DATUM_STRING) {
            String strPath = datumParam.getStringValue();
            String strAbsPath = getAbsolutePath(strPath);
            return new DataClass(DATATYPES.DATUM_STRING, strAbsPath);
        } else {
            datumParam.changeDataType(DATATYPES.DATUM_INTEGER);
            int idxFD = datumParam.getDataValue().intValue();
            String strAbsPath = getAbsolutePath(idxFD);
            return new DataClass(DATATYPES.DATUM_STRING, strAbsPath);
        }
    }
    
    public static String getAbsolutePath(int idxFD)  throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0 || msfileDescriptors[idxFD] == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }
        return msfileDescriptors[idxFD].getFileAbsolutePath();
    }
    
    public static String getAbsolutePath(String strPath) throws JFCALCExpErrException {
        try {
            File f = getFileFromCurrentDir(strPath);
            return f.getAbsolutePath();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass getCanonicalPath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumParam = new DataClass();
        datumParam.copyTypeValueDeep(listParams.removeLast());
        if (datumParam.getDataType() == DATATYPES.DATUM_STRING) {
            String strPath = datumParam.getStringValue();
            String strAbsPath = getCanonicalPath(strPath);
            return new DataClass(DATATYPES.DATUM_STRING, strAbsPath);
        } else {
            datumParam.changeDataType(DATATYPES.DATUM_INTEGER);
            int idxFD = datumParam.getDataValue().intValue();
            String strAbsPath = getCanonicalPath(idxFD);
            return new DataClass(DATATYPES.DATUM_STRING, strAbsPath);
        }
    }
    
    public static String getCanonicalPath(int idxFD)  throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0 || msfileDescriptors[idxFD] == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }
        return msfileDescriptors[idxFD].getFileCanonicalPath();
    }
    
    public static String getCanonicalPath(String strPath) throws JFCALCExpErrException {
        try {
            File f = getFileFromCurrentDir(strPath);
            return f.getCanonicalPath();
        } catch (Exception e) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_ACCESS_FILE);
        }
    }
        
    public static DataClass getPath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumParam = new DataClass();
        datumParam.copyTypeValueDeep(listParams.removeLast());
        datumParam.changeDataType(DATATYPES.DATUM_INTEGER);
        int idxFD = datumParam.getDataValue().intValue();
        String strAbsPath = getPath(idxFD);
        return new DataClass(DATATYPES.DATUM_STRING, strAbsPath);
    }
    
    public static String getPath(int idxFD)  throws JFCALCExpErrException {
        if (idxFD >= msfileDescriptors.length || idxFD <= 0 || msfileDescriptors[idxFD] == null) {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);  // invalid file descriptor
        }
        return msfileDescriptors[idxFD].getFileFullPath();
    }
        
    public static DataClass comparePath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 2)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath1 = new DataClass();
        datumPath1.copyTypeValueDeep(listParams.removeLast());
        datumPath1.changeDataType(DATATYPES.DATUM_STRING);
        String strPath1 = datumPath1.getStringValue();
        DataClass datumPath2 = new DataClass();
        datumPath2.copyTypeValueDeep(listParams.removeLast());
        datumPath2.changeDataType(DATATYPES.DATUM_STRING);
        String strPath2 = datumPath2.getStringValue();
        int nReturn = comparePath(strPath1, strPath2);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(nReturn));
    }
    
    public static int comparePath(String strPath1, String strPath2) {
        File f1 = getFileFromCurrentDir(strPath1);
        File f2 = getFileFromCurrentDir(strPath2);
        String strCanPath1 = strPath1, strCanPath2 = strPath2;
        try {
            strCanPath1 = f1.getCanonicalPath();
            f1 = getFileFromCurrentDir(strCanPath1);
        } catch(Exception e) {
        }
        try {
            strCanPath2 = f2.getCanonicalPath();
            f2 = getFileFromCurrentDir(strCanPath2);
        } catch(Exception e) {
        }
        return f1.compareTo(f2);
    }
    
    public static DataClass isParentPath(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 2)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath1 = new DataClass();
        datumPath1.copyTypeValueDeep(listParams.removeLast());
        datumPath1.changeDataType(DATATYPES.DATUM_STRING);
        String strPath1 = datumPath1.getStringValue();
        DataClass datumPath2 = new DataClass();
        datumPath2.copyTypeValueDeep(listParams.removeLast());
        datumPath2.changeDataType(DATATYPES.DATUM_STRING);
        String strPath2 = datumPath2.getStringValue();
        boolean bReturn = isParentPath(strPath1, strPath2);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean isParentPath(String strPath1, String strPath2) {  // is path1 parent of path2?
        try {
            File f1 = getFileFromCurrentDir(strPath1).getCanonicalFile();
            File f2 = getFileFromCurrentDir(strPath2).getCanonicalFile();
            String strParent = f2.getParent();
            while (strParent != null) {
                if (comparePath(strPath1, strParent) == 0) {
                    return true;
                }
                f2 = getFileFromCurrentDir(strParent).getCanonicalFile();
                strParent = f2.getParent();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static DataClass getWorkingDir(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 0)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        String strWorkingDir = getWorkingDir();
        return new DataClass(DATATYPES.DATUM_STRING, strWorkingDir);
    }
    
    public static String getWorkingDir() {
        File fCurrentDirectory = new File(msstrWorkingDir); // cannot use get file from directory
        try {
            msstrWorkingDir = fCurrentDirectory.getCanonicalPath();
            return msstrWorkingDir;
        } catch (Exception ex) {
            return msstrWorkingDir;
        }
    }

    public static DataClass changeDir(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumNewDir = new DataClass();
        datumNewDir.copyTypeValueDeep(listParams.removeLast());
        datumNewDir.changeDataType(DATATYPES.DATUM_STRING);
        String strNewDir = datumNewDir.getStringValue();
        
        boolean bReturn = changeDir(strNewDir);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }
    
    public static boolean changeDir(String strNewDir) {
        File fCurrentDirectory = getFileFromCurrentDir(strNewDir);
        try {
            if (!fCurrentDirectory.isDirectory()) {
                return false;
            } else {
                msstrWorkingDir = fCurrentDirectory.getCanonicalPath();
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }
    
    public static File getFileFromCurrentDir(String strPath) {
        File fp = new File(strPath);    // this should be the only place we use new File
        if (fp.isAbsolute()) {
            return fp;  // if this is absolute path
        } else {
            fp = new File(msstrWorkingDir, strPath);  // if this is relative path
            return fp;
        }
    }

    public static DataClass getFileSize(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        
        long lSize = getFileSize(strPath);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(lSize));
    }
    
    public static long getFileSize(String strPath) {
        File fp = getFileFromCurrentDir(strPath);
        try {
            if (fp.exists() && !fp.isDirectory()) {
                return fp.length();
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static DataClass getFileLastModifiedTime(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        
        long lTime = getFileLastModifiedTime(strPath);
        return new DataClass(DATATYPES.DATUM_INTEGER, new MFPNumeric(lTime));
    }

    public static long getFileLastModifiedTime(String strPath) {
        File fp = getFileFromCurrentDir(strPath);
        try {
            if (fp.exists()) {
                return fp.lastModified();
            }
            return -1;
        } catch (Exception ex) {
            return -1;
        }
    }

    public static DataClass setFileLastModifiedTime(LinkedList<DataClass> listParams) throws JFCALCExpErrException {
        if (listParams.size() != 1 && listParams.size() != 2)   {
            throw new JFCALCExpErrException(ERRORTYPES.ERROR_INCORRECT_NUM_OF_PARAMETER);
        }
        DataClass datumPath = new DataClass();
        datumPath.copyTypeValueDeep(listParams.removeLast());
        datumPath.changeDataType(DATATYPES.DATUM_STRING);
        String strPath = datumPath.getStringValue();
        
        long lTime = -1;
        if (listParams.size() > 0) {
            DataClass datumModifiedTime = new DataClass();
            datumModifiedTime.copyTypeValueDeep(listParams.removeLast());
            datumModifiedTime.changeDataType(DATATYPES.DATUM_INTEGER);
            lTime = datumModifiedTime.getDataValue().longValue();
        }
        
        boolean bReturn = setFileLastModifiedTime(strPath, lTime);
        return new DataClass(DATATYPES.DATUM_BOOLEAN, new MFPNumeric(bReturn));
    }

    public static boolean setFileLastModifiedTime(String strPath, long ltime) {
        File fp = getFileFromCurrentDir(strPath);
        try {
            if (fp.exists()) {
                if (ltime < 0) {
                    fp.setLastModified(System.currentTimeMillis());
                } else {
                    fp.setLastModified(ltime);
                }
                return true;
            }
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
