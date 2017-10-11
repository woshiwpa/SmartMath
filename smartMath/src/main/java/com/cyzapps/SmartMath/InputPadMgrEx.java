package com.cyzapps.SmartMath;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Locale;

import org.w3c.dom.*;

import com.cyzapps.SmartMath.InputPadManager.InputPad;
import com.cyzapps.VisualMFP.Color;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder; 

public class InputPadMgrEx {
	
	public static String escapeXMLSpecialChar(String strInput)	{
		String strOutput = null;
		if (strInput != null)	{
			strOutput = strInput.replaceAll("&", "&amp;")
								.replaceAll("<", "&lt;")
								.replaceAll(">", "&gt;")
								.replaceAll("'", "&apos;")
								.replaceAll("\"", "&quot;");
		}
		return strOutput;
	}
	
	public static class InputKey	{
		public final static String DEFAULT_KEY_NAME = "";	// key name is not id, which can be duplicated or empty.
		public final static String DEFAULT_KEY_INPUT = "";	// means no input
		public final static String DEFAULT_KEY_SHOWN = " ";
		public final static String DEFAULT_DRAWABLE = "";
		public final static String DEFAULT_KEY_FUNCTION = "";	// means no function
		public final static Color DEFAULT_FOREGROUND_COLOR = new Color(0,0,0);
		public final static int DEFAULT_CURSOR_PLACE = 0;
		public final static double DEFAULT_GAP_TO_PREVIOUS = 0;
		public final static double DEFAULT_GAP_TO_NEXT = 0;
		public final static double AUTO_SPAN = 0;
		public final static double DEFAULT_SPAN = 0;	// 0 means auto span, 1 means the span is 1/number of columns of width
		public String mstrKeyName = DEFAULT_KEY_NAME;
		public String mstrKeyInput = DEFAULT_KEY_INPUT;
		public String mstrKeyShown = DEFAULT_KEY_SHOWN;	// has to include at least an blank otherwise in Android 3.1 the height of button is 0.
		public String mstrDrawable = DEFAULT_DRAWABLE;
		public String mstrKeyFunction = DEFAULT_KEY_FUNCTION;
		public Color mcolorForeground = new Color(DEFAULT_FOREGROUND_COLOR);	// by default is black
		public int mnCursorPlace = DEFAULT_CURSOR_PLACE;	// by default is 0
		public double mdGap2Prev = DEFAULT_GAP_TO_PREVIOUS;	// gap to previous of 1/number of columns of width
		public double mdGap2Next = DEFAULT_GAP_TO_NEXT;	// gap to next of 1/number of columns of width
		public double mdSpan = DEFAULT_SPAN;	// span of total width, if negative, means determined by number of columns.
		
		public InputKeyRow mparent = null;	// parent must be an inputkey row
		public boolean isFunctionKey()	{
			if (mstrKeyFunction.trim().equalsIgnoreCase(DEFAULT_KEY_FUNCTION))	{
				return false;
			}
			return true;
		}
		
		public boolean isTextInputKey()	{
			if (mstrKeyInput.equalsIgnoreCase(DEFAULT_KEY_INPUT))	{	// no trim here
				return false;
			}
			return true;
		}
		
		public boolean isSameFunctionalityKey(InputKey keyAnother)	{
			String strKeyInputWithoutBrackets = mstrKeyInput.trim().split("\\(")[0];
			String strAnotherKeyInputWithoutBrackets = keyAnother.mstrKeyInput.trim().split("\\(")[0];
			if (strKeyInputWithoutBrackets.equalsIgnoreCase(strAnotherKeyInputWithoutBrackets)
					&& mstrKeyFunction.trim().equalsIgnoreCase(keyAnother.mstrKeyFunction.trim()))	{
				return true;
			}
			return false;
		}
		
		public String convertToXMLString()	{
			String strHeadOutput = "<InputKey";
			String strKeyNameOutput = "";
			if (!mstrKeyName.equals(DEFAULT_KEY_NAME))	{
				strKeyNameOutput = " KeyName=\"" + escapeXMLSpecialChar(mstrKeyName) + "\"";
			}
			String strKeyInputOutput = "";
			if (!mstrKeyInput.equals(DEFAULT_KEY_INPUT))	{
				strKeyInputOutput = " KeyInput=\"" + escapeXMLSpecialChar(mstrKeyInput) + "\"";
			}
			String strKeyShownOutput = "";
			if (!mstrKeyShown.equals(DEFAULT_KEY_SHOWN))	{
				strKeyShownOutput = " KeyShown=\"" + escapeXMLSpecialChar(mstrKeyShown) + "\"";
			}
			String strKeyDrawableOutput = "";
			if (!mstrDrawable.equals(DEFAULT_DRAWABLE))	{
				strKeyDrawableOutput = " Drawable=\"" + escapeXMLSpecialChar(mstrDrawable) + "\"";
			}
			String strKeyFunctionOutput = "";
			if (!mstrKeyFunction.equals(DEFAULT_KEY_FUNCTION))	{
				strKeyFunctionOutput = " KeyFunction=\"" + escapeXMLSpecialChar(mstrKeyFunction) + "\"";
			}
			String strColorForegroundOutput = "";
			if (!mcolorForeground.isEqual(DEFAULT_FOREGROUND_COLOR))	{
				String strColorForeground = Integer.toHexString(mcolorForeground.getARGB());
				strColorForegroundOutput = " ForegroundColor=\"" + escapeXMLSpecialChar(strColorForeground) + "\"";
			}
			String strCursorPlaceOutput = "";
			if (mnCursorPlace != DEFAULT_CURSOR_PLACE)	{
				String strCursorPlace= Integer.toString(mnCursorPlace);
				strCursorPlaceOutput = " CursorPlace=\"" + escapeXMLSpecialChar(strCursorPlace) + "\"";
			}
			String strGap2PrevOutput = "";
			if (mdGap2Prev != DEFAULT_GAP_TO_PREVIOUS)	{
				String strGap2Prev= Double.toString(mdGap2Prev);
				strGap2PrevOutput = " GapToPrevious=\"" + escapeXMLSpecialChar(strGap2Prev) + "\"";
			}
			String strGap2NextOutput = "";
			if (mdGap2Next != DEFAULT_GAP_TO_NEXT)	{
				String strGap2Next = Double.toString(mdGap2Next);
				strGap2NextOutput = " GapToNext=\"" + escapeXMLSpecialChar(strGap2Next) + "\"";
			}
			String strSpanOutput = "";
			if (mdSpan != DEFAULT_SPAN)	{
				String strSpan= Double.toString(mdSpan);
				strSpanOutput = " Span=\"" + escapeXMLSpecialChar(strSpan) + "\"";
			}
			String strTailOutput = "/>\n";
			return strHeadOutput + strKeyNameOutput + strKeyInputOutput + strKeyShownOutput + strKeyDrawableOutput + strKeyFunctionOutput + strColorForegroundOutput
					+ strCursorPlaceOutput + strGap2PrevOutput + strSpanOutput + strGap2NextOutput + strTailOutput;
		}
		
		public static InputKey readFromXMLNode(Node nodeInputKey)	{
			if (nodeInputKey != null && nodeInputKey.getNodeType() == Node.ELEMENT_NODE &&
					nodeInputKey.getNodeName().equals("InputKey"))	{
				InputKey inputKey = new InputKey();
				NamedNodeMap namedNodeMapInputKey = nodeInputKey.getAttributes();
				Node nodeAttr = namedNodeMapInputKey.getNamedItem("KeyName");
				if (nodeAttr != null)	{
					inputKey.mstrKeyName = nodeAttr.getNodeValue();
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("KeyInput");
				if (nodeAttr != null)	{
					inputKey.mstrKeyInput = nodeAttr.getNodeValue();
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("KeyShown");
				if (nodeAttr != null)	{
					inputKey.mstrKeyShown = nodeAttr.getNodeValue();
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("Drawable");
				if (nodeAttr != null)	{
					inputKey.mstrDrawable = nodeAttr.getNodeValue();
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("KeyFunction");
				if (nodeAttr != null)	{
					inputKey.mstrKeyFunction = nodeAttr.getNodeValue();
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("ForegroundColor");
				if (nodeAttr != null)	{
					String strForegroundColor = nodeAttr.getNodeValue();
					try	{
						long nColor = Long.parseLong(strForegroundColor, 16);
						inputKey.mcolorForeground.setARGB(nColor);
					} catch(NumberFormatException e)	{
						// do nothing here because inputKey member has been set as default.
					}
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("CursorPlace");
				if (nodeAttr != null)	{
					try	{
						inputKey.mnCursorPlace = Integer.parseInt(nodeAttr.getNodeValue());
					} catch(NumberFormatException e)	{
						// do nothing here because inputKey member has been set as default.
					}
					if (inputKey.mnCursorPlace > 0)	{
						inputKey.mnCursorPlace = 0;
					} else if (inputKey.mnCursorPlace < -1 * inputKey.mstrKeyInput.length())	{
						inputKey.mnCursorPlace = -1 * inputKey.mstrKeyInput.length();
					}
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("GapToPrevious");
				if (nodeAttr != null)	{
					try	{
						inputKey.mdGap2Prev = Double.parseDouble(nodeAttr.getNodeValue());
					} catch(NumberFormatException e)	{
						// do nothing here because inputKey member has been set as default.
					}
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("GapToNext");
				if (nodeAttr != null)	{
					try	{
						inputKey.mdGap2Next = Double.parseDouble(nodeAttr.getNodeValue());
					} catch(NumberFormatException e)	{
						// do nothing here because inputKey member has been set as default.
					}
				}
				nodeAttr = namedNodeMapInputKey.getNamedItem("Span");
				if (nodeAttr != null)	{
					try	{
						inputKey.mdSpan = Double.parseDouble(nodeAttr.getNodeValue());
					} catch(NumberFormatException e)	{
						// do nothing here because inputKey member has been set as default.
					}
				}
				return inputKey;
			}
			return null;					
		}
		
		public void copy(InputKey from)	{
			if (from != null)	{
				mstrKeyName = from.mstrKeyName;
				mstrKeyInput = from.mstrKeyInput;
				mstrKeyShown = from.mstrKeyShown;
				mstrDrawable = from.mstrDrawable;
				mstrKeyFunction = from.mstrKeyFunction;
				mcolorForeground.copy(from.mcolorForeground);
				mnCursorPlace = from.mnCursorPlace;
				if (mnCursorPlace > 0)	{
					mnCursorPlace = 0;
				} else if (mnCursorPlace < -1 * mstrKeyInput.length())	{
					mnCursorPlace = -1 * mstrKeyInput.length();
				}
				mdGap2Prev = from.mdGap2Prev;
				mdGap2Next = from.mdGap2Next;
				mdSpan = from.mdSpan;
				// do not copy parent.
			}
		}
		
		public void copyDiscardPosition(InputKey from)	{
			if (from != null)	{
				mstrKeyName = from.mstrKeyName;
				mstrKeyInput = from.mstrKeyInput;
				mstrKeyShown = from.mstrKeyShown;
				mnCursorPlace = from.mnCursorPlace;
				if (mnCursorPlace > 0)	{
					mnCursorPlace = 0;
				} else if (mnCursorPlace < -1 * mstrKeyInput.length())	{
					mnCursorPlace = -1 * mstrKeyInput.length();
				}
				mstrDrawable = from.mstrDrawable;
				mstrKeyFunction = from.mstrKeyFunction;
				mcolorForeground.copy(from.mcolorForeground);
				/*// position information is discarded.
				mdGap2Prev = from.mdGap2Prev;
				mdGap2Next = from.mdGap2Next;
				mdSpan = from.mdSpan; */
				// do not copy parent.
			}
		}
	}
	
	public static class InputKeyRow	{
		public LinkedList<InputKey> mlistInputKeys = new LinkedList<InputKey>();
		
		public TableInputPad mparent = null;
		public boolean mbInLandScape = false;
		public double getTotalSpan(int nNumofColumns)	{
			double dTotalSpan = 0;
			boolean bHaveAutoSpanKeys = false;
			for (InputKey inputKey:mlistInputKeys)	{
				dTotalSpan += inputKey.mdGap2Prev + inputKey.mdSpan + inputKey.mdGap2Next;
				if (inputKey.mdSpan == InputKey.AUTO_SPAN)	{
					bHaveAutoSpanKeys = true;
				}
			}
			if (bHaveAutoSpanKeys)	{
				dTotalSpan = (dTotalSpan > nNumofColumns)?dTotalSpan:nNumofColumns;
			}
			return dTotalSpan;
		}
		
		public double getTotalNoAutoSpans()	{
			double dTotalSpan = 0;
			for (InputKey inputKey:mlistInputKeys)	{
				dTotalSpan += inputKey.mdGap2Prev + inputKey.mdSpan + inputKey.mdGap2Next;
			}
			return dTotalSpan;
		}
		
		public int getNumberOfAutoSpanKeys()	{
			int n = 0;
			for (InputKey inputKey:mlistInputKeys)	{
				if (inputKey.mdSpan == InputKey.AUTO_SPAN)	{
					n++;
				}
			}
			return n;
		}
		
		public double getAutoSpanValue(int nNumofColumns)	{
			double dTotalSpan = 0;
			int nNumberOfAutoSpanKeys = 0;
			for (InputKey inputKey:mlistInputKeys)	{
				dTotalSpan += inputKey.mdGap2Prev + inputKey.mdSpan + inputKey.mdGap2Next;
				if (inputKey.mdSpan == InputKey.AUTO_SPAN)	{
					nNumberOfAutoSpanKeys ++;
				}
			}
			if (nNumberOfAutoSpanKeys > 0)	{
				double dTotalAutoSpan = (dTotalSpan > nNumofColumns)?0:(nNumofColumns - dTotalSpan);
				return dTotalAutoSpan/nNumberOfAutoSpanKeys;
			} else	{
				return 0;
			}
		}
		
		public String convertToXMLString()	{
			String strXML = "";
			strXML += "<InputKeyRow>\n";
			for (int idx1 = 0; idx1 < mlistInputKeys.size(); idx1 ++)	{
				strXML += mlistInputKeys.get(idx1).convertToXMLString();					
			}
			strXML += "</InputKeyRow>\n";
			return strXML;
		}
		
		public static InputKeyRow readFromXMLNode(Node nodeKeyRow, int nNumOfColumnsDefInTable)	{
			if (nodeKeyRow != null && nodeKeyRow.getNodeType() == Node.ELEMENT_NODE &&
					nodeKeyRow.getNodeName().equals("InputKeyRow"))	{
				InputKeyRow inputKeyRow = new InputKeyRow();
				NodeList listOfInputKeys = nodeKeyRow.getChildNodes();
				int indexKey = 0;
				while (indexKey < listOfInputKeys.getLength())	{
					Node nodeInputKey = listOfInputKeys.item(indexKey);
					if (nodeInputKey.getNodeType() == Node.ELEMENT_NODE &&
							nodeInputKey.getNodeName().equals("InputKey"))	{
	    				InputKey inputKey = InputKey.readFromXMLNode(nodeInputKey);
	    				if (inputKey != null)	{
	    					inputKeyRow.mlistInputKeys.addLast(inputKey);
	    					inputKey.mparent = inputKeyRow;
	    				}
					}
					indexKey ++;
	    		}
				return inputKeyRow;
			}
			return null;
		}

		public static InputKeyRow readFromXMLNode(Node nodeKeyRow)	{
			return readFromXMLNode(nodeKeyRow, TableInputPad.DEFAULT_NUMBER_OF_COLUMNS);
		}
	}
	
	public static class TableInputPad	{
		public String mstrName = "";	// unique name for each input pad.
		public String mstrLongName = "";
		public String mstrWrappedName1 = "";
		public String mstrWrappedName2 = "";
		public String mstrShortName = "";
		public boolean mbVisible = true;
		public boolean mbHaveDictionary = false;

		public final static int DEFAULT_NUMBER_OF_COLUMNS = 4;
		public final static int DEFAULT_NUMBER_OF_COLUMNS_LAND = 6;
		public int mnNumofColumns = DEFAULT_NUMBER_OF_COLUMNS;
		public LinkedList<InputKeyRow> mlistKeyRows = new LinkedList<InputKeyRow>();
		public double mdKeyHeight = 1.0;
		public int mnNumofColumnsLand = DEFAULT_NUMBER_OF_COLUMNS_LAND;
		public LinkedList<InputKeyRow> mlistKeyRowsLand = new LinkedList<InputKeyRow>();
		public double mdKeyHeightLand = 1.0;
		
		public LinkedList<InputKey> getListOfKeys()	{
			LinkedList<InputKey> listOfKeys = new LinkedList<InputKey>();
			for (int idx = 0; idx < mlistKeyRows.size(); idx ++)	{
				listOfKeys.addAll(mlistKeyRows.get(idx).mlistInputKeys);
			}
			return listOfKeys;
		}
		
		public LinkedList<InputKey> getListOfKeysLand()	{
			LinkedList<InputKey> listOfKeys = new LinkedList<InputKey>();
			for (int idx = 0; idx < mlistKeyRowsLand.size(); idx ++)	{
				listOfKeys.addAll(mlistKeyRowsLand.get(idx).mlistInputKeys);
			}
			return listOfKeys;
		}
		
		public void addNewKey(InputKey inputKey, boolean bAdd2Default, boolean bAdd2Land)	{
			if (inputKey == null)	{
				return;
			}
			if (bAdd2Default)	{
				InputKeyRow inputKeyRow;
				if (mlistKeyRows.size() == 0)	{
					inputKeyRow = new InputKeyRow();
					inputKeyRow.mparent = this;
					inputKeyRow.mbInLandScape = false;
					mlistKeyRows.add(inputKeyRow);
				} else	{
					inputKeyRow = mlistKeyRows.getLast();
				}
				double dTotalWidth = 0;
				int nNumOfAutoSpanKeys = 0;
				for (int idx = 0; idx < inputKeyRow.mlistInputKeys.size(); idx ++)	{
					dTotalWidth += inputKeyRow.mlistInputKeys.get(idx).mdGap2Prev
							+ inputKeyRow.mlistInputKeys.get(idx).mdSpan
							+ inputKeyRow.mlistInputKeys.get(idx).mdGap2Next;
					if (inputKeyRow.mlistInputKeys.get(idx).mdSpan == InputKey.AUTO_SPAN)	{
						nNumOfAutoSpanKeys++;
					}
				}
				double dThisKeyWidth = inputKey.mdGap2Prev + inputKey.mdGap2Next + ((inputKey.mdSpan == 0)?1:inputKey.mdSpan);
				if ((mnNumofColumns - dTotalWidth - nNumOfAutoSpanKeys) >= dThisKeyWidth)	{
					// add to the existing row
					inputKeyRow.mlistInputKeys.addLast(inputKey);
					inputKey.mparent = inputKeyRow;
				} else	{
					// add to the new row
					InputKeyRow inputKeyRowNew = new InputKeyRow();
					inputKeyRowNew.mlistInputKeys.addLast(inputKey);
					inputKey.mparent = inputKeyRowNew;
					mlistKeyRows.addLast(inputKeyRowNew);
					inputKeyRowNew.mparent = this;
					inputKeyRowNew.mbInLandScape = false;
				}
			}
			if (bAdd2Land)	{
				InputKeyRow inputKeyRow;
				if (mlistKeyRowsLand.size() == 0)	{
					inputKeyRow = new InputKeyRow();
					inputKeyRow.mparent = this;
					inputKeyRow.mbInLandScape = true;
					mlistKeyRowsLand.add(inputKeyRow);
				} else	{
					inputKeyRow = mlistKeyRowsLand.getLast();
				}
				double dTotalWidth = 0;
				int nNumOfAutoSpanKeys = 0;
				for (int idx = 0; idx < inputKeyRow.mlistInputKeys.size(); idx ++)	{
					dTotalWidth += inputKeyRow.mlistInputKeys.get(idx).mdGap2Prev
							+ inputKeyRow.mlistInputKeys.get(idx).mdSpan + inputKeyRow.mlistInputKeys.get(idx).mdGap2Next;
					if (inputKeyRow.mlistInputKeys.get(idx).mdSpan == InputKey.AUTO_SPAN)	{
						nNumOfAutoSpanKeys++;
					}
				}
				double dThisKeyWidth = inputKey.mdGap2Prev + inputKey.mdGap2Next + ((inputKey.mdSpan == 0)?1:inputKey.mdSpan);
				if ((mnNumofColumnsLand - dTotalWidth - nNumOfAutoSpanKeys) >= dThisKeyWidth)	{
					// add to the existing row
					inputKeyRow.mlistInputKeys.addLast(inputKey);
					inputKey.mparent = inputKeyRow;
				} else	{
					// add to the new row
					InputKeyRow inputKeyRowNew = new InputKeyRow();
					inputKeyRowNew.mlistInputKeys.addLast(inputKey);
					inputKey.mparent = inputKeyRowNew;
					mlistKeyRowsLand.addLast(inputKeyRowNew);
					inputKeyRowNew.mparent = this;
					inputKeyRowNew.mbInLandScape = true;
				}
			}
		}
		
		public String convertToXMLString()	{
			String strVisible = mbVisible?"true":"false";
			String strHaveDictionary = mbHaveDictionary?"true":"false";
			String strXML = "<InputPad Name=\"" + escapeXMLSpecialChar(mstrName) + "\" Type=\"TableInputPad\" LongName=\"" + escapeXMLSpecialChar(mstrLongName)
							+ "\" WrappedNameLine1=\"" + escapeXMLSpecialChar(mstrWrappedName1) + "\" WrappedNameLine2=\"" + escapeXMLSpecialChar(mstrWrappedName2)
							+ "\" ShortName=\"" + escapeXMLSpecialChar(mstrShortName) + "\" Visible=\"" + strVisible + "\" HaveDictionary=\"" + strHaveDictionary + "\">\n";
			// default orientation
			strXML += "<KeyTable Orientation=\"Default\" NumberOfColumns=\"" + mnNumofColumns + "\" KeyHeight=\"" + mdKeyHeight + "\">\n";
			
			for (int index = 0; index < mlistKeyRows.size(); index ++)	{
				strXML += mlistKeyRows.get(index).convertToXMLString();
			}
			strXML += "</KeyTable>\n";
			// Landscape orientation
			strXML += "<KeyTable Orientation=\"Landscape\" NumberOfColumns=\"" + mnNumofColumnsLand + "\" KeyHeight=\"" + mdKeyHeightLand + "\">\n";
			for (int index = 0; index < mlistKeyRowsLand.size(); index ++)	{
				strXML += mlistKeyRowsLand.get(index).convertToXMLString();
			}
			strXML += "</KeyTable>\n";
			strXML += "</InputPad>\n";
			return strXML;
		}
		
		public static TableInputPad readFromXMLNode(Node inputPadNode)	{
            if(inputPadNode != null && inputPadNode.getNodeType() == Node.ELEMENT_NODE){
            	NamedNodeMap namedNodeMap = inputPadNode.getAttributes();
            	Node nodeAttrType = namedNodeMap.getNamedItem("Type");
            	if (nodeAttrType != null && nodeAttrType.getNodeValue().equals("TableInputPad"))	{
            		try	{
	            		//This is what we want
	            		TableInputPad tableInputPad = new TableInputPad();
	            		Node nodeAttr = namedNodeMap.getNamedItem("Name");
	            		tableInputPad.mstrName = nodeAttr.getNodeValue();
	            		nodeAttr = namedNodeMap.getNamedItem("LongName");
	            		if (nodeAttr != null)	{
	            			tableInputPad.mstrLongName = nodeAttr.getNodeValue();
	            		}
	            		nodeAttr = namedNodeMap.getNamedItem("WrappedNameLine1");
	            		if (nodeAttr != null)	{
	            			tableInputPad.mstrWrappedName1 = nodeAttr.getNodeValue();
	            		}
	            		nodeAttr = namedNodeMap.getNamedItem("WrappedNameLine2");
	            		if (nodeAttr != null)	{
	            			tableInputPad.mstrWrappedName2 = nodeAttr.getNodeValue();
	            		}
	               		nodeAttr = namedNodeMap.getNamedItem("ShortName");
	            		if (nodeAttr != null)	{
	            			tableInputPad.mstrShortName = nodeAttr.getNodeValue();
	            		}
	            		nodeAttr = namedNodeMap.getNamedItem("Visible");
	            		if (nodeAttr != null)	{
	            			tableInputPad.mbVisible = Boolean.parseBoolean(nodeAttr.getNodeValue());
	            		}
	            		nodeAttr = namedNodeMap.getNamedItem("HaveDictionary");
	            		if (nodeAttr != null)	{
	            			tableInputPad.mbHaveDictionary = Boolean.parseBoolean(nodeAttr.getNodeValue());
	            		}
            			NodeList listOfKeyTables = inputPadNode.getChildNodes();
            			for (int idxKeyTbl = 0; idxKeyTbl < listOfKeyTables.getLength(); idxKeyTbl ++)	{
            				if (listOfKeyTables.item(idxKeyTbl).getNodeType() == Node.ELEMENT_NODE &&
            						listOfKeyTables.item(idxKeyTbl).getNodeName().equals("KeyTable"))	{
            					Node nodeKeyTable = listOfKeyTables.item(idxKeyTbl);
            					LinkedList<InputKeyRow> listKeyRows = null;
            					NamedNodeMap namedNodeMapKeyTable = nodeKeyTable.getAttributes();
            					nodeAttr = namedNodeMapKeyTable.getNamedItem("Orientation");
            					int nNumofColumnsDefInTable = TableInputPad.DEFAULT_NUMBER_OF_COLUMNS;
            					boolean bInLandScape = false;
            					if (nodeAttr != null && nodeAttr.getNodeValue().equalsIgnoreCase("Landscape"))	{
            						bInLandScape = true;
        		               		nodeAttr = namedNodeMapKeyTable.getNamedItem("NumberOfColumns");
        		               		nNumofColumnsDefInTable = tableInputPad.mnNumofColumnsLand = Integer.parseInt(nodeAttr.getNodeValue());
        		            		if (tableInputPad.mnNumofColumnsLand <= 0)	{
        		            			continue;	// something wrong here, ignore this key table.
        		            		}
        		            		listKeyRows = tableInputPad.mlistKeyRowsLand;
        		            		nodeAttr = namedNodeMapKeyTable.getNamedItem("KeyHeight");
        		            		if (nodeAttr != null)	{
        		            			try {
        		            				tableInputPad.mdKeyHeightLand = Math.abs(Double.parseDouble(nodeAttr.getNodeValue()));
        		            				if (tableInputPad.mdKeyHeightLand < 0.25)	{
        		            					tableInputPad.mdKeyHeightLand = 0.25;
        		            				} else if (tableInputPad.mdKeyHeightLand > 4)	{
        		            					tableInputPad.mdKeyHeightLand = 4;
        		            				}
        		            			} catch (NumberFormatException e)	{
        		            				tableInputPad.mdKeyHeightLand = 1.0;
        		            			}
        		            		}
            					} else {
            						bInLandScape = false;
        		               		nodeAttr = namedNodeMapKeyTable.getNamedItem("NumberOfColumns");
        		               		nNumofColumnsDefInTable = tableInputPad.mnNumofColumns = Integer.parseInt(nodeAttr.getNodeValue());
        		            		if (tableInputPad.mnNumofColumns <= 0)	{
        		            			continue;	// something wrong here, ignore this key table.
        		            		}
        		            		listKeyRows = tableInputPad.mlistKeyRows;
        		            		nodeAttr = namedNodeMapKeyTable.getNamedItem("KeyHeight");
        		            		if (nodeAttr != null)	{
        		            			try {
        		            				tableInputPad.mdKeyHeight = Math.abs(Double.parseDouble(nodeAttr.getNodeValue()));
        		            				if (tableInputPad.mdKeyHeight < 0.25)	{
        		            					tableInputPad.mdKeyHeight = 0.25;
        		            				} else if (tableInputPad.mdKeyHeight > 4)	{
        		            					tableInputPad.mdKeyHeight = 4;
        		            				}
        		            			} catch (NumberFormatException e)	{
        		            				tableInputPad.mdKeyHeight = 1.0;
        		            			}
        		            		}
            					}
            					NodeList listOfInputKeyRows = nodeKeyTable.getChildNodes();
            					int idxRow = 0;
            					while (idxRow < listOfInputKeyRows.getLength())	{
            						Node nodeKeyRow = listOfInputKeyRows.item(idxRow);
            						if (nodeKeyRow.getNodeType() == Node.ELEMENT_NODE &&
            								nodeKeyRow.getNodeName().equals("InputKeyRow"))	{
            							InputKeyRow inputKeyRow = InputKeyRow.readFromXMLNode(nodeKeyRow, nNumofColumnsDefInTable);
            							if (inputKeyRow != null)	{
            								listKeyRows.addLast(inputKeyRow);
            								inputKeyRow.mparent = tableInputPad;
            								inputKeyRow.mbInLandScape = bInLandScape;
            							}
            						}
	    	            			idxRow ++;
            					}
            				}
            			}
	            		return tableInputPad;
            		} catch (Exception e)	{
            			//something wrong happens so return null.
            		}
            	}
            }
        	return null;
		}
	}
	
	public static int getInputPadsVersion(InputStream is)	{
		int nVersion = 0;
		try {
	        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse(is);

	        // normalize text representation
	        doc.getDocumentElement().normalize();
	        Node inputPadsNode = doc.getElementsByTagName("InputPads").item(0);
	        NamedNodeMap namedNodeMap = inputPadsNode.getAttributes();
        	Node nodeAttr = namedNodeMap.getNamedItem("Version");
        	String strVersion = "";
			if (nodeAttr != null)	{
				strVersion = nodeAttr.getNodeValue();
			}
	        nVersion = Integer.parseInt(strVersion);
		} catch (Exception e)	{
		}
		return nVersion;
	}
	
	public static LinkedList<TableInputPad> readInputPadsFromXML(InputStream is)	{
        Document doc;
        LinkedList<TableInputPad> listOfPads = new LinkedList<TableInputPad>();
        if (is == null)	{
        	return listOfPads;
        }
		try {
	        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(is);

	        // normalize text representation
	        doc.getDocumentElement().normalize();
	        NodeList listOfInputPads = doc.getElementsByTagName("InputPads").item(0).getChildNodes();
	        for(int indexPad = 0; indexPad < listOfInputPads.getLength(); indexPad ++){
	            Node inputPadNode = listOfInputPads.item(indexPad);
	            TableInputPad tableInputPad = TableInputPad.readFromXMLNode(inputPadNode);
	            if (tableInputPad != null)	{
	            	listOfPads.add(tableInputPad);
	            }
	        }
		} catch (Exception e)	{
		}
		
		return listOfPads;
	}
	
	public static LinkedList<TableInputPad> filterOffInvisibleInputPads(LinkedList<TableInputPad> listInputPads)	{
		LinkedList<TableInputPad> listShownInputPads = new LinkedList<TableInputPad>();
		if (listInputPads == null)	{
			return listShownInputPads;
		}
		for (int idx = 0; idx < listInputPads.size(); idx ++)	{
			TableInputPad inputPad = listInputPads.get(idx);
			if (inputPad.mbVisible)	{
				listShownInputPads.add(inputPad);
			}
		}
		return listShownInputPads;
	}
	
	public static String writeInputPadsToXML(LinkedList<TableInputPad> listInputPads)	{
		String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		str += "<InputPads Version=\"39\">\n";
		for (int index = 0; index < listInputPads.size(); index ++)	{
			str += listInputPads.get(index).convertToXMLString();
		}
		str += "</InputPads>\n";
		return str;
	}

	public static LinkedList<TableInputPad> convtOldInputPadCfg2New(LinkedList<InputPad> listInputPadsOld)	{
		LinkedList<TableInputPad> listNewInputPads = new LinkedList<TableInputPad>();
		if (listInputPadsOld == null || listInputPadsOld.size() == 0)	{
			return listNewInputPads;
		}
		for (InputPad inputPadOld : listInputPadsOld)	{
			if (!(inputPadOld instanceof InputPadManager.TableInputPad))	{
				continue;
			}
			InputPadManager.TableInputPad tableInputPadOld = (InputPadManager.TableInputPad)inputPadOld;
			TableInputPad inputPadNew = new TableInputPad();
			inputPadNew.mbVisible = tableInputPadOld.mbVisible;
			inputPadNew.mstrLongName = tableInputPadOld.mstrLongName;
			inputPadNew.mstrName = tableInputPadOld.mstrName;
			inputPadNew.mstrWrappedName1 = tableInputPadOld.mstrWrappedName1;
			inputPadNew.mstrWrappedName2 = tableInputPadOld.mstrWrappedName2;
			inputPadNew.mstrShortName = tableInputPadOld.mstrShortName;
			inputPadNew.mnNumofColumns = tableInputPadOld.mnNumofColumns;
			inputPadNew.mnNumofColumnsLand = tableInputPadOld.mnNumofColumnsLand;
			for (int idxKeyTbl = 0; idxKeyTbl < tableInputPadOld.mlistInputKeys.size(); idxKeyTbl ++)	{
				InputPadManager.InputKey inputKeyOld = tableInputPadOld.mlistInputKeys.get(idxKeyTbl);
				InputKey inputKeyNew = new InputKey();
				inputKeyNew.mstrKeyInput = inputKeyOld.mstrKeyInput;
				inputKeyNew.mstrKeyShown = inputKeyOld.mstrKeyShown;
				if (inputKeyNew.mstrKeyInput.indexOf('(') == -1 && inputKeyNew.mstrKeyInput.indexOf(')') == -1
						&& inputKeyNew.mnCursorPlace == 0)	{
					// the inputKey definition is still based on old format, so I have to add "( )" to the tail of it.
					inputKeyNew.mstrKeyInput += "(  )";
					inputKeyNew.mnCursorPlace = -2;
				}
				inputPadNew.addNewKey(inputKeyNew, true, false);
			}
			for (int idxKeyTbl = 0; idxKeyTbl < tableInputPadOld.mlistInputKeysLand.size(); idxKeyTbl ++)	{
				InputPadManager.InputKey inputKeyOld = tableInputPadOld.mlistInputKeysLand.get(idxKeyTbl);
				InputKey inputKeyNew = new InputKey();
				inputKeyNew.mstrKeyInput = inputKeyOld.mstrKeyInput;
				inputKeyNew.mstrKeyShown = inputKeyOld.mstrKeyShown;
				if (inputKeyNew.mstrKeyInput.indexOf('(') == -1 && inputKeyNew.mstrKeyInput.indexOf(')') == -1
						&& inputKeyNew.mnCursorPlace == 0)	{
					// the inputKey definition is still based on old format, so I have to add "( )" to the tail of it.
					inputKeyNew.mstrKeyInput += "(  )";
					inputKeyNew.mnCursorPlace = -2;
				}
				inputPadNew.addNewKey(inputKeyNew, false, true);
			}
			if (inputPadNew.getListOfKeys().size() + inputPadNew.getListOfKeysLand().size() > 0)	{
				listNewInputPads.add(inputPadNew);
			}
		}
		return listNewInputPads;
	}
	
	// merge listInputPads1 into listInputPads2, but will discard all position information.
	public static void mergeInputPadCfgs(LinkedList<TableInputPad> listInputPads1,
														LinkedList<TableInputPad> listInputPads2)	{
		if (listInputPads1 == null || listInputPads1.size() == 0)	{
			return;
		}
		if (/*listInputPads2 == null || */listInputPads2.size() == 0)	{	// if listInputPads is null will throw null pointer exception
			TableInputPad inputPad = new TableInputPad();
			inputPad.mbVisible = true;
			inputPad.mstrLongName = "others";
			inputPad.mstrName = "others";
			inputPad.mstrWrappedName1 = "others";
			inputPad.mstrWrappedName2 = "...";
			inputPad.mstrShortName = "more..";
			inputPad.mnNumofColumns = 4;
			inputPad.mnNumofColumnsLand = 6;
			listInputPads2.addLast(inputPad);
		}
		TableInputPad inputPadOthers = (TableInputPad) listInputPads2.getLast();
		for (int idx = 0; idx < listInputPads2.size(); idx ++)	{
			if (listInputPads2.get(idx).mstrName.equalsIgnoreCase("others")
					&& listInputPads2.get(idx) instanceof TableInputPad)	{
				inputPadOthers = (TableInputPad) listInputPads2.get(idx);
				break;
			}
		}
		if (inputPadOthers.mstrName.equalsIgnoreCase("others") == false)	{
			TableInputPad inputPad = new TableInputPad();
			inputPad.mbVisible = true;
			inputPad.mstrLongName = "others";
			inputPad.mstrName = "others";
			inputPad.mstrWrappedName1 = "others";
			inputPad.mstrWrappedName2 = "...";
			inputPad.mstrShortName = "more..";
			listInputPads2.add(inputPad);
			inputPadOthers = inputPad;
		}
		for (int nLandOrNot = 0; nLandOrNot < 2; nLandOrNot ++)	{
			for (int idx = 0; idx < listInputPads1.size(); idx ++)	{
				if (listInputPads1.get(idx) instanceof TableInputPad)	{
					LinkedList<InputKey> listKeys = new LinkedList<InputKey>();
					if (nLandOrNot == 1)	{
						listKeys = ((TableInputPad)listInputPads1.get(idx)).getListOfKeysLand();
					} else	{
						listKeys = ((TableInputPad)listInputPads1.get(idx)).getListOfKeys();
					}
					for (int idx1 = 0; idx1 < listKeys.size(); idx1++)	{
						boolean bFound = false;
						for (int idx2 = 0; idx2 < listInputPads2.size(); idx2 ++)	{
							if (listInputPads2.get(idx2) instanceof TableInputPad)	{
								LinkedList<InputKey> listPadInputKeys = ((TableInputPad)listInputPads2.get(idx2)).getListOfKeys();
								for (int idx3 = 0; idx3 < listPadInputKeys.size(); idx3++)	{
									if (listKeys.get(idx1).isSameFunctionalityKey(listPadInputKeys.get(idx3)))	{
										bFound = true;
										break;
									}
								}
							}
							if (bFound)	{
								break;
							}
						}
						if (bFound)	{
							continue;
						}
						for (int idx2 = 0; idx2 < listInputPads2.size(); idx2 ++)	{
							if (listInputPads2.get(idx2) instanceof TableInputPad)	{
								LinkedList<InputKey> listPadInputKeysLand = ((TableInputPad)listInputPads2.get(idx2)).getListOfKeysLand();
								for (int idx3 = 0; idx3 < listPadInputKeysLand.size(); idx3++)	{
									if (listKeys.get(idx1).isSameFunctionalityKey(listPadInputKeysLand.get(idx3)))	{
										bFound = true;
										break;
									}
								}
							}
							if (bFound)	{
								break;
							}
						}
						if (!bFound)	{
							String strInput2Add = listKeys.get(idx1).mstrKeyInput;
							strInput2Add = strInput2Add.split("\\(")[0].trim();
							if (strInput2Add.length() != 0 && !strInput2Add.equalsIgnoreCase("round")
									&& !strInput2Add.equalsIgnoreCase("ceil") && !strInput2Add.equalsIgnoreCase("floor")
									&& !strInput2Add.equalsIgnoreCase("int2to10") && !strInput2Add.equalsIgnoreCase("int10to2")
									&& !strInput2Add.equalsIgnoreCase("int16to10") && !strInput2Add.equalsIgnoreCase("int10to16")
									&& !strInput2Add.equalsIgnoreCase("int2to16") && !strInput2Add.equalsIgnoreCase("int16to2"))	{
								// only if the inputKey does not contain the above disprecated or not shown functions, add the key.
								InputKey inputKey = new InputKey();
								inputKey.copyDiscardPosition(listKeys.get(idx1));	// position information is discarded.
								/*// when convert old format to new format, we have already done this, need not to do it again.
								if (inputKey.mstrKeyInput.indexOf('(') == -1 && inputKey.mstrKeyInput.indexOf(')') == -1
										&& inputKey.mnCursorPlace == 0)	{
									// the inputKey definition is still based on old format, so I have to add "( )" to the tail of it.
									inputKey.mstrKeyInput += "(  )";
									inputKey.mnCursorPlace = -2;
								}*/
								if (nLandOrNot == 0)	{
									inputPadOthers.addNewKey(inputKey, true, false);
								} else	{
									inputPadOthers.addNewKey(inputKey, false, true);
								}
							}
						}
					}
				}
			}
		}
	}
}
