/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyzapps.Jfcalc;

import com.cyzapps.Jfcalc.ErrProcessor.ERRORTYPES;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jfcalc.FuncEvaluator.ConsoleInputStream;
import com.cyzapps.Jfcalc.FuncEvaluator.LogOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tonyc
 */
public class SysExecProcIOE implements Runnable {
    public int mnIOEType = 0;   // 0 is output, 1 is input and 2 is error stream. By default it is 0.
    public Process mp = null;
    public ConsoleInputStream minputStream = null;
    public LogOutputStream moutputStream = null;
    public LogOutputStream merrorStream = null;
    
    public SysExecProcIOE(Process p, int nIOEType, ConsoleInputStream inputStream, LogOutputStream outputStream, LogOutputStream errorStream) {
        mp = p;
        mnIOEType = nIOEType;
        minputStream = inputStream;
        moutputStream = outputStream;
        merrorStream = errorStream;
    }
    
    public void run() {
        if (mp == null) {
            return;
        }
        if (mnIOEType == 2 && merrorStream != null) {   // error stream of mp
            try {
                InputStream stderr = mp.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    merrorStream.outputString(line + "\n");
                }
            } catch (Exception e) {
                // exception do nothing.
                mnIOEType = 2;
            }
        } else if (mnIOEType == 1 && moutputStream != null) {    // output stream of mp
            try {
                InputStream stdin = mp.getInputStream();
                InputStreamReader isi = new InputStreamReader(stdin);
                BufferedReader br = new BufferedReader(isi);
                String line = null;
                while ((line = br.readLine()) != null) {
                    moutputStream.outputString(line + "\n");
                }
            } catch (Exception e) {
                // if any exception, throw exception and exit.
                mnIOEType = 1;
            }            
        } else if (minputStream != null) {
            try {
                // input stream of mp
                OutputStream stdout = mp.getOutputStream();
                while(true) {
                    OutputStreamWriter os = new OutputStreamWriter(stdout);
                    minputStream.doBeforeInput();
                    String strInput = minputStream.inputString();
                    if (strInput == null) {
                        os.close();
                        break;
                    }
                    if (mp!= null) {
                        os.write(strInput);
                        os.close();
                    } else {
                        break;
                    }
                }
                minputStream.doAfterInput();
                
            } catch (Exception e) {
                // if any exception, throw exception and exit.
                mnIOEType = 0;
            }
        }
    }
    
}
