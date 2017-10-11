/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.adapter.MFPAdapter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tonyc
 */
public class FileDescriptor {

    public static class BufferedString {
        protected boolean mbIsSimpleString = false;
        protected String mstr = null;
        protected int mnCurrentIdx = 0;
        protected BufferedReader mbufferedReader = null;
        protected char[] mcarrayInternalBuf = new char[0];  // make sure this is always not null.
        
        protected int mnReadCharLimit = -1;    // -1 means no limit
        protected int mnRemainingChar2Read = -1;
        
        public BufferedString() {
            
        }
        
        public BufferedString(String str) {
            mbIsSimpleString = true;
            mstr = str;
            mnCurrentIdx = 0;
        }
        
        public BufferedString(String str, int nCurrentIdx) {
            mbIsSimpleString = true;
            mstr = str;
            mnCurrentIdx = nCurrentIdx;
        }
        
        public BufferedString(BufferedReader bufferedReader) {
            mbIsSimpleString = false;
            mbufferedReader = bufferedReader;
            mcarrayInternalBuf = new char[0];
        }
        
        public boolean isSimpleString() {
            return mbIsSimpleString;
        }
        
        public String getString() {
            return mstr;
        }
        
        public BufferedReader getBufferedReader() {
            return mbufferedReader;
        }
        
        public int getCurrentIdx() {
            return mnCurrentIdx;
        }
        
        public void setReadCharLimit(int nReadCharLimit) {
            mnRemainingChar2Read = mnReadCharLimit = nReadCharLimit;
        }
        
        public void removeReadCharLimit() {
            mnRemainingChar2Read = mnReadCharLimit = -1;
        }
        
        public int getReadCharLimit() {
            return mnReadCharLimit;
        }
        
        public int getRemainingChar2Read() {
            return mnRemainingChar2Read;
        }
        
        // return -1 if arrive at the end.
        public int getCurrentChar() throws JFCALCExpErrException {
            return getChar(0);
        }
        
        public int getChar(int nOffset) throws JFCALCExpErrException {
            if (nOffset < 0) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else if (mbIsSimpleString) {
                if ((mnRemainingChar2Read >= 0 && nOffset >= mnRemainingChar2Read) || mnCurrentIdx + nOffset >= mstr.length()) {
                    return -1;
                } else {
                    return mstr.charAt(mnCurrentIdx + nOffset);
                }
            } else {    // whether mark is supported or not, we support
                if (mcarrayInternalBuf.length <= nOffset) {
                    try {
                        char[] cBuf = new char[nOffset + 1 - mcarrayInternalBuf.length];
                        int n = mbufferedReader.read(cBuf, 0, cBuf.length);  // do not check if it is on the tail
                        if (n != -1) {
                            char[] cNewBuf = new char[nOffset + 1 + n - cBuf.length];
                            System.arraycopy(mcarrayInternalBuf, 0, cNewBuf, 0, mcarrayInternalBuf.length);
                            System.arraycopy(cBuf, 0, cNewBuf, mcarrayInternalBuf.length, n);
                            mcarrayInternalBuf = cNewBuf;
                            if (n == cBuf.length) {
                                if (mnRemainingChar2Read >= 0 && nOffset >= mnRemainingChar2Read) {
                                    return -1;
                                } else {
                                    return mcarrayInternalBuf[nOffset];
                                }
                            } else {    // we have arrived at the end of the file before read nOffset char
                                return -1;
                            }
                        } else {
                            return -1;
                        }
                    } catch (IOException ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                    }                    
                } else {    // read from buffer.
                    if (mnRemainingChar2Read >= 0 && nOffset >= mnRemainingChar2Read) {
                        return -1;
                    } else {
                        return mcarrayInternalBuf[nOffset];
                    }
                }
            }
        }
        
        public int moveIdxForward() throws JFCALCExpErrException {
            if (mbIsSimpleString) {
                mnCurrentIdx++;
                if (mnRemainingChar2Read > 0) {
                    mnRemainingChar2Read --;
                }
                return mnCurrentIdx;
            } else {
                if (mcarrayInternalBuf.length == 0) {
                    try {
                        if (mbufferedReader.read() == -1) {
                            return -1;  // have reached the end of the file.
                        } else {
                            if (mnRemainingChar2Read > 0) {
                                mnRemainingChar2Read --;
                            }
                            return mnCurrentIdx;
                        }
                    } catch (IOException ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                    }
                } else {
                    char[] cNewBuf = new char[mcarrayInternalBuf.length - 1];
                    System.arraycopy(mcarrayInternalBuf, 1, cNewBuf, 0, cNewBuf.length);
                    mcarrayInternalBuf = cNewBuf;
                    if (mnRemainingChar2Read > 0) {
                        mnRemainingChar2Read --;
                    }
                    return mnCurrentIdx;
                }
            }
        }
        
        public boolean isEnd(boolean bConsiderReadLmt) throws JFCALCExpErrException {
            if (mbIsSimpleString) {
                if (bConsiderReadLmt && mnRemainingChar2Read == 0) {
                    return true;
                } else if (mnCurrentIdx >= mstr.length()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (bConsiderReadLmt && mnRemainingChar2Read == 0) {
                    return true;
                } else if (mcarrayInternalBuf.length == 0) {
                    try {
                        int c = mbufferedReader.read();
                        if (c == -1) {
                            return true;
                        } else {
                            mcarrayInternalBuf = new char[1];
                            mcarrayInternalBuf[0] = (char)c;
                            return false;
                        }
                    } catch (IOException ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                    }
                } else {
                    return false;
                }
            }
        }
        
        public String readLine() throws JFCALCExpErrException {
            if (mbIsSimpleString) {
                if (mnRemainingChar2Read == 0) {
                    return null;
                } else if (mnCurrentIdx >= mstr.length()) {
                    return null;
                } else {
                    String strMaxRead = "";
                    if (mnRemainingChar2Read > 0) {
                        strMaxRead = mstr.substring(mnCurrentIdx, Math.min(mnCurrentIdx + mnRemainingChar2Read, mstr.length()));
                    } else {
                        // mnRemainingChar2Read < 0, cannot == 0
                        strMaxRead = mstr.substring(mnCurrentIdx, mstr.length());
                    }
                    int nIdxReturnLnN = strMaxRead.indexOf('\n');
                    int nIdxReturnLnR = strMaxRead.indexOf('\r');
                    int nIdxReturnLn = -1;
                    if (nIdxReturnLnN == -1) {
                        nIdxReturnLn = nIdxReturnLnR;
                    } else if (nIdxReturnLnR == -1) {
                        nIdxReturnLn = nIdxReturnLnN;
                    } else {
                        nIdxReturnLn = Math.min(nIdxReturnLnN, nIdxReturnLnR);
                    }
                    
                    
                    if (nIdxReturnLn == -1) {
                        // cannot find '\n' or '\r', the whole strMaxRead will be one line.
                        if (mnRemainingChar2Read > 0) {
                            mnRemainingChar2Read -= strMaxRead.length();
                        }
                        mnCurrentIdx += strMaxRead.length();
                        return strMaxRead;
                    } else if (nIdxReturnLn == nIdxReturnLnN
                            || (nIdxReturnLn == nIdxReturnLnR
                                && (nIdxReturnLn >= strMaxRead.length() - 1 || strMaxRead.charAt(nIdxReturnLn + 1) != '\n'))) {
                        // '\n' found or single '\t' found
                        String str2Return = strMaxRead.substring(mnCurrentIdx, nIdxReturnLn);
                        if (mnRemainingChar2Read > 0) {
                            mnRemainingChar2Read -= nIdxReturnLn + 1;
                        }
                        mnCurrentIdx += nIdxReturnLn + 1;
                        return str2Return;
                    } else  {
                        // '\r\n' found
                        String str2Return = strMaxRead.substring(mnCurrentIdx, nIdxReturnLn);
                        if (mnRemainingChar2Read > 0) {
                            mnRemainingChar2Read -= nIdxReturnLn + 2;
                        }
                        mnCurrentIdx += nIdxReturnLn + 2;
                        return str2Return;
                    }
                }
            } else {
                if (mnRemainingChar2Read == 0) {
                    return null;
                } else if (mnRemainingChar2Read < 0) {
                    // no remaining char limit
                    String str = "";
                    int nInternlBufLenB4Read = mcarrayInternalBuf.length;
                    for (int idx = 0; idx < mcarrayInternalBuf.length; idx ++) {
                        if (mcarrayInternalBuf[idx] == '\n') {  // single '\n'
                            char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 1];
                            System.arraycopy(mcarrayInternalBuf, idx + 1, carrayNewBuf, 0, carrayNewBuf.length);
                            mcarrayInternalBuf = carrayNewBuf;
                            return str;
                        } else if (idx < mcarrayInternalBuf.length - 1 && mcarrayInternalBuf[idx] == '\r' && mcarrayInternalBuf[idx + 1] != '\n') {
                            // single '\r'
                            char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 1];
                            System.arraycopy(mcarrayInternalBuf, idx + 1, carrayNewBuf, 0, carrayNewBuf.length);
                            mcarrayInternalBuf = carrayNewBuf;
                            return str;
                        } else if (idx < mcarrayInternalBuf.length - 1 && mcarrayInternalBuf[idx] == '\r' && mcarrayInternalBuf[idx + 1] == '\n') {
                            // '\r\n'
                            char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 2];
                            System.arraycopy(mcarrayInternalBuf, idx + 2, carrayNewBuf, 0, carrayNewBuf.length);
                            mcarrayInternalBuf = carrayNewBuf;
                            return str;
                        } else if (idx == mcarrayInternalBuf.length - 1 && mcarrayInternalBuf[idx] == '\r') {
                            int nChar;
                            try {
                                nChar = mbufferedReader.read();
                            } catch (IOException ex) {
                                throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                            }
                            if (nChar == '\n') {    // '\r\n'
                                mcarrayInternalBuf = new char[0];
                                return str;
                            } else if (nChar == -1) {   // end of input
                                mcarrayInternalBuf = new char[0];
                                return str;
                            } else {    // other normal character.
                                mcarrayInternalBuf = new char[1];
                                mcarrayInternalBuf[0] = (char) nChar;
                                return str;
                            }
                        }
                        str += mcarrayInternalBuf[idx];
                    }
                    // cannot find '\n' or '\r' or '\r\n' from buf
                    mcarrayInternalBuf = new char[0];
                    String strFromReader = "";
                    try {
                        strFromReader = mbufferedReader.readLine();
                    } catch (IOException ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                    }
                    if (strFromReader == null) {
                        if (nInternlBufLenB4Read == 0) {
                            return null;    // means we have arrived at end of the file
                        } else {
                            return str;
                        }
                    } else {
                        return str + strFromReader;
                    }
                } else { // remaining char to read is positive.
                    String str = "";
                    for (int idx = 0; idx < mcarrayInternalBuf.length; idx ++) {
                        if (mcarrayInternalBuf[idx] == '\n') {  // single '\n'
                            char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 1];
                            System.arraycopy(mcarrayInternalBuf, idx + 1, carrayNewBuf, 0, carrayNewBuf.length);
                            mcarrayInternalBuf = carrayNewBuf;
                            mnRemainingChar2Read --;
                            return str;
                        } else if (mcarrayInternalBuf[idx] == '\r') {
                            if (mnRemainingChar2Read == 1) {
                                // single '\r'
                                char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 1];
                                System.arraycopy(mcarrayInternalBuf, idx + 1, carrayNewBuf, 0, carrayNewBuf.length);
                                mcarrayInternalBuf = carrayNewBuf;
                                mnRemainingChar2Read --;
                                return str;
                            } else if (idx < mcarrayInternalBuf.length - 1 && mcarrayInternalBuf[idx + 1] != '\n') {
                                // still single '\r'
                                char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 1];
                                System.arraycopy(mcarrayInternalBuf, idx + 1, carrayNewBuf, 0, carrayNewBuf.length);
                                mcarrayInternalBuf = carrayNewBuf;
                                mnRemainingChar2Read --;
                                return str;
                            } else if (idx < mcarrayInternalBuf.length - 1 && mcarrayInternalBuf[idx + 1] == '\n') {
                                // '\r\n'
                                char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 2];
                                System.arraycopy(mcarrayInternalBuf, idx + 2, carrayNewBuf, 0, carrayNewBuf.length);
                                mcarrayInternalBuf = carrayNewBuf;
                                mnRemainingChar2Read -= 2;
                                return str;
                            } else {    //idx == mcarrayInternalBuf.length - 1
                                int nChar;
                                try {
                                    nChar = mbufferedReader.read();
                                } catch (IOException ex) {
                                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                                }
                                if (nChar == '\n') {    // '\r\n'
                                    mcarrayInternalBuf = new char[0];
                                    mnRemainingChar2Read -= 2;
                                    return str;
                                } else if (nChar == -1) {   // end of input
                                    mcarrayInternalBuf = new char[0];
                                    mnRemainingChar2Read --;
                                    return str;
                                } else {    // other normal character.
                                    mcarrayInternalBuf = new char[1];
                                    mcarrayInternalBuf[0] = (char) nChar;
                                    mnRemainingChar2Read --;
                                    return str;
                                }
                            }
                        }
                        str += mcarrayInternalBuf[idx];
                        mnRemainingChar2Read --;
                        if (mnRemainingChar2Read == 0) {
                            char[] carrayNewBuf = new char[mcarrayInternalBuf.length - idx - 1];
                            System.arraycopy(mcarrayInternalBuf, idx + 1, carrayNewBuf, 0, carrayNewBuf.length);
                            mcarrayInternalBuf = carrayNewBuf;
                            return str;
                        }
                    }
                    // cannot find '\n' or '\t' from buf
                    mcarrayInternalBuf = new char[0];
                    try {
                        while (mnRemainingChar2Read > 0) {
                            int nChar = mbufferedReader.read();
                            if (nChar == -1) {
                                // end of file.
                                if (str.length() == 0) {
                                    return null;
                                } else {
                                    return str;
                                }
                            } else if (nChar == '\n' || (nChar == '\r' && mnRemainingChar2Read == 1)) {
                                mnRemainingChar2Read --;
                                return str;
                            } else if (nChar == '\r' && mnRemainingChar2Read > 1) {
                                int nChar1 = mbufferedReader.read();
                                if (nChar1 == -1) {
                                    // end of file
                                    mnRemainingChar2Read --;
                                    return str;
                                } else if (nChar1 == '\n') {
                                    mnRemainingChar2Read -= 2;
                                    return str;
                                } else {    // other characters
                                    mcarrayInternalBuf = new char[1];
                                    mcarrayInternalBuf[0] = (char)nChar1;
                                    mnRemainingChar2Read --;
                                    return str;
                                }
                            } else {
                                mnRemainingChar2Read --;
                                str += (char) nChar;
                            }
                        }
                        return str;
                    } catch (IOException ex) {
                        throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                    }
                }
            }
        }
    }
    
    public static class BufferedInputStream {
        protected FileInputStream mfileInputStream = null;
        protected byte[] mbytearrayInternalBuf = new byte[0];  // make sure this is always not null.
        
        public BufferedInputStream() {}
        
        public BufferedInputStream(FileInputStream fileInputStream) {
            mfileInputStream = fileInputStream;
        }
        
        public FileInputStream getFileInputStream() {
            return mfileInputStream;
        }
        
        public int read() throws JFCALCExpErrException {
            if (mbytearrayInternalBuf.length == 0) {
                try {
                    return mfileInputStream.read();
                } catch (IOException ex) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                }
            } else {
                byte[] byteNewBuf = new byte[mbytearrayInternalBuf.length - 1];
                System.arraycopy(mbytearrayInternalBuf, 1, byteNewBuf, 0, byteNewBuf.length);
                byte byteReturn = mbytearrayInternalBuf[0];
                mbytearrayInternalBuf = byteNewBuf;
                return byteReturn;
            }
        }
        
        public int read(byte[] byteArray) throws JFCALCExpErrException {
            return read(byteArray, 0, byteArray.length);
        }
        
        public int read(byte[] byteArray, int nOffset, int nCopyLen) throws JFCALCExpErrException {
            if (nOffset < 0 || nCopyLen < 0 || (nOffset + nCopyLen) > byteArray.length) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_PARAMETER);
            } else if (nCopyLen == 0) {
                return 0;
            } else if (mbytearrayInternalBuf.length < nCopyLen) {
                System.arraycopy(mbytearrayInternalBuf, 0, byteArray, nOffset, mbytearrayInternalBuf.length);
                try {
                    int n = mfileInputStream.read(byteArray, mbytearrayInternalBuf.length + nOffset, nCopyLen - mbytearrayInternalBuf.length);
                    if (n == -1) {
                        n = mbytearrayInternalBuf.length;
                        if (n == 0) {
                            n = -1;
                        }
                        mbytearrayInternalBuf = new byte[0];
                        return n;
                    } else {
                        n += mbytearrayInternalBuf.length;
                        mbytearrayInternalBuf = new byte[0];
                        return n;
                    }
                } catch (IOException ex) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                }
                
            } else {
                byte[] byteNewBuf = new byte[mbytearrayInternalBuf.length - nCopyLen];
                System.arraycopy(mbytearrayInternalBuf, 0, byteArray, nOffset, nCopyLen);
                System.arraycopy(mbytearrayInternalBuf, nCopyLen, byteNewBuf, 0, byteNewBuf.length);
                mbytearrayInternalBuf = byteNewBuf;
                return nCopyLen;
             }
        }
        
        public void close() throws JFCALCExpErrException {
            try {
                mfileInputStream.close();
            } catch (IOException ex) {
                throw new JFCALCExpErrException(ERRORTYPES.ERROR_INVALID_FILE);
            }
        }
        
        public boolean isEnd() throws JFCALCExpErrException {
            if (mbytearrayInternalBuf.length > 0) {
                return false;
            } else {
                try {
                    int c = mfileInputStream.read();
                    if (c == -1) {
                        return true;
                    } else {
                        mbytearrayInternalBuf = new byte[1];
                        mbytearrayInternalBuf[0] = (byte)c;
                        return false;
                    }
                } catch (IOException ex) {
                    throw new JFCALCExpErrException(ERRORTYPES.ERROR_CANNOT_READ_FILE);
                }
            }
        }
    }
    
    protected int mnFileId = 0;
    protected String mstrFileName;
    protected String mstrFilePath; //exclude name
    protected String mstrFileFullPath;	//include name
    protected String mstrFileAbsolutePath;  //absolute path
    protected String mstrFileCanonicalPath; //canonical path is still needed consider than a symbol link is deleted.
    protected BufferedString mbufferedString = null;
    protected OutputStreamWriter moutputStreamWriter = null;
    protected BufferedInputStream mbufferedInputStream = null;
    protected FileOutputStream mfileOutputStream = null;
    protected long mlLastAccessedTime = System.currentTimeMillis();

    public FileDescriptor() {
        mnFileId = 0;
        mstrFileName = "";
        mstrFilePath = "";
        mstrFileFullPath = "";
        mstrFileAbsolutePath = "";
        mstrFileCanonicalPath = "";
        mbufferedString = null;
        moutputStreamWriter = null;
        mbufferedInputStream = null;
        mfileOutputStream = null;
        mlLastAccessedTime = System.currentTimeMillis();
    }
    
    public FileDescriptor(int nFileId, String strFileName, String strFilePath, String strFileFullPath, String strFileAbsolutePath, String strFileCanonicalPath,
            BufferedString bufferedString, OutputStreamWriter outputStreamWriter, BufferedInputStream bufferedInputStream, FileOutputStream fileOutputStream) {
        mnFileId = nFileId;
        mstrFileName = strFileName;
        mstrFilePath = strFilePath;
        mstrFileFullPath = strFileFullPath;
        mstrFileAbsolutePath = strFileAbsolutePath;
        mstrFileCanonicalPath = strFileCanonicalPath;
        mbufferedString = bufferedString;
        moutputStreamWriter = outputStreamWriter;
        mbufferedInputStream = bufferedInputStream;
        mfileOutputStream = fileOutputStream;
        mlLastAccessedTime = System.currentTimeMillis();
    }
    
    public int getFileId() {
        return mnFileId;
    }

    public String getFileName() {
        return mstrFileName;
    }

    public String getFileExtension() {
        int nDotIdx = mstrFileName.lastIndexOf(".");
        if (nDotIdx == -1) {
            return "";
        } else {
            return mstrFileName.substring(nDotIdx + 1);
        }
    }

    public String getFileNameWithoutExtension() {
        int nDotIdx = mstrFileName.lastIndexOf(".");
        if (nDotIdx == -1) {
            return mstrFileName;
        } else {
            return mstrFileName.substring(0, nDotIdx);
        }
    }

    public String getFilePath() {
        return mstrFilePath;
    }

    public String getFileFullPath() {
        return mstrFileFullPath;
    }

    public String getFileAbsolutePath() {
        return mstrFileAbsolutePath;
    }

    public String getFileCanonicalPath() {
        return mstrFileCanonicalPath;
    }

    public BufferedString getBufferedString() {
        return mbufferedString;
    }
    
    public OutputStreamWriter getOutputStreamWriter() {
        return moutputStreamWriter;
    }
    
    public BufferedInputStream getBufferedInputStream() {
        return mbufferedInputStream;
    }
    
    public FileOutputStream getFileOutputStream() {
        return mfileOutputStream;
    }
    
    public long setLastAccessedTime() {
        mlLastAccessedTime = System.currentTimeMillis();
        return mlLastAccessedTime;
    }
    
    public long getLastAccessedTime() {
        return mlLastAccessedTime;
    }
}
