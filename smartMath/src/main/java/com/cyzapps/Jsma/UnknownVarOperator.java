package com.cyzapps.Jsma;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

import com.cyzapps.Jfcalc.BaseData.DataClass;
import com.cyzapps.Jfcalc.ErrProcessor.JFCALCExpErrException;
import com.cyzapps.Jmfp.VariableOperator.Variable;
import com.cyzapps.Jsma.SMErrProcessor.ERRORTYPES;
import com.cyzapps.Jsma.SMErrProcessor.JSmartMathErrException;

public class UnknownVarOperator {
	public static class UnknownVariable	extends Variable {
		protected boolean mbValueAssigned = false;
		
		public UnknownVariable(){}
		public UnknownVariable(String strName)	{
			setName(strName);
		}
		public UnknownVariable(String strName, DataClass datumValue)	{
			setVariable(strName, datumValue);
		}
		
		public boolean isValueAssigned()	{
			return mbValueAssigned;
		}
        
        public void setValueAssigned(boolean bSetValueAssigned)  {
            mbValueAssigned = bSetValueAssigned;
        }
		
		public DataClass getSolvedValue() throws JSmartMathErrException	{
			if (!mbValueAssigned) {
				throw new JSmartMathErrException(ERRORTYPES.ERROR_VARIABLE_VALUE_NOT_KNOWN);
			}
			return super.getValue();
		}
		
		public DataClass setValue(DataClass datum)	{
			mbValueAssigned = true;
			return super.setValue(datum);
		}
		
		public void setVariable(String strName, DataClass datumValue)	{
			super.setVariable(strName, datumValue);
			mbValueAssigned = true;
		}
	}
	
	public static UnknownVariable lookUpList(String strName, LinkedList<UnknownVariable> lUnknownVars)	{
		ListIterator<UnknownVariable> itr = lUnknownVars.listIterator();
		String strLowerCaseName = strName.toLowerCase(Locale.US);
		while (itr.hasNext())	{
			UnknownVariable var = itr.next();
			if(var.getName().equals(strLowerCaseName))	{
				return var;
			}
		}
		return null;
	}
	
	public static DataClass lookUpList4Value(String strName, LinkedList<UnknownVariable> lUnknownVars) throws JSmartMathErrException	{
		UnknownVariable v = lookUpList(strName, lUnknownVars);
		if (v == null)
			return null;
		else
			return v.getSolvedValue();
	}
    
    public static LinkedList<UnknownVariable> cloneUnknownVarList(LinkedList<UnknownVariable> listUnknownVars) throws JFCALCExpErrException   {
        LinkedList<UnknownVariable> listUnknownVarsNew = new LinkedList<UnknownVariable>();
        for (int idx = 0; idx < listUnknownVars.size(); idx ++)    {
            UnknownVariable var = new UnknownVariable(listUnknownVars.get(idx).getName());
            DataClass datumValue = new DataClass();
            datumValue.copyTypeValueDeep(listUnknownVars.get(idx).getValue());
            var.setValue(datumValue);
            var.setValueAssigned(listUnknownVars.get(idx).isValueAssigned());
            listUnknownVarsNew.add(var);
        }
        return listUnknownVarsNew;
    }

    public static void mergeUnknowns2VarSpaces(LinkedList<UnknownVariable> listUnknowns, LinkedList<LinkedList<Variable>> lVarSpaces)   {
        for (int idx = 0; idx < listUnknowns.size(); idx ++)   {
            boolean bFoundVar = false;
            for (int idx1 = 0; idx1 < lVarSpaces.size(); idx1 ++)    {
                for (int idx2 = 0; idx2 < lVarSpaces.get(idx1).size(); idx2 ++)  {
                    if (lVarSpaces.get(idx1).get(idx2).getName().equalsIgnoreCase(listUnknowns.get(idx).getName())) {
                        // find the variable in the name space
                        lVarSpaces.get(idx1).set(idx2, listUnknowns.get(idx));
                        bFoundVar = true;
                        break;                                            
                    }
                }
                if (bFoundVar)  {
                    break;
                }
            }
        }
    }
}
