package com.cyzapps.SmartMath;

import java.io.InputStream;
import java.util.LinkedList;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder; 

public class InputPadManager {
	
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
	
	public static class InputPad	{
		public String mstrName = "";	// unique name for each input pad.
		public String mstrLongName = "";
		public String mstrWrappedName1 = "";
		public String mstrWrappedName2 = "";
		public String mstrShortName = "";
		public boolean mbVisible = true;
		public String convertToXMLString()	{
			String strVisible = mbVisible?"true":"false";	// need not to escape special chars.
			String strXML = "<InputPad Name=\"" + escapeXMLSpecialChar(mstrName) + "\" LongName=\"" + escapeXMLSpecialChar(mstrLongName)
				+ "\" WrappedNameLine1=\"" + escapeXMLSpecialChar(mstrWrappedName1) + "\" WrappedNameLine2=\"" + escapeXMLSpecialChar(mstrWrappedName2)
				+ "\" ShortName=\"" + escapeXMLSpecialChar(mstrShortName) + "\" Visible=\"" + strVisible + "\">\n";
			strXML += "</InputPad>\n";
			return strXML;
		}
	}
	
	public static class InputKey	{
		public String mstrKeyInput = "";
		public String mstrKeyShown = " ";	// has to include at least an blank otherwise in Android 3.1 the height of button is 0.
		public String convertToXMLString()	{
			return "<InputKey KeyInput=\"" + escapeXMLSpecialChar(mstrKeyInput)
					+ "\" KeyShown=\"" + escapeXMLSpecialChar(mstrKeyShown) + "\"/>\n";
		}
		public void copy(InputKey from)	{
			if (from != null)	{
				mstrKeyInput = from.mstrKeyInput;
				mstrKeyShown = from.mstrKeyShown;
			}
		}
	}
	
	public static class TableInputPad extends InputPad	{
		public int mnNumofColumns = 1;
		public LinkedList<InputKey> mlistInputKeys = new LinkedList<InputKey>();
		public int mnNumofColumnsLand = 1;
		public LinkedList<InputKey> mlistInputKeysLand = new LinkedList<InputKey>();
		
		@Override
		public String convertToXMLString()	{
			String strVisible = mbVisible?"true":"false";
			String strXML = "<InputPad Name=\"" + escapeXMLSpecialChar(mstrName) + "\" Type=\"TableInputPad\" LongName=\"" + escapeXMLSpecialChar(mstrLongName)
							+ "\" WrappedNameLine1=\"" + escapeXMLSpecialChar(mstrWrappedName1) + "\" WrappedNameLine2=\"" + escapeXMLSpecialChar(mstrWrappedName2)
							+ "\" ShortName=\"" + escapeXMLSpecialChar(mstrShortName) + "\" Visible=\"" + strVisible + "\">\n";
			// default orientation
			strXML += "\n<KeyTable Orientation=\"Default\" NumberOfColumns=\"" + mnNumofColumns + "\">\n";
			for (int index = 0; index < mlistInputKeys.size(); index ++)	{
				strXML += mlistInputKeys.get(index).convertToXMLString();
			}
			strXML += "\n</KeyTable>";
			// Landscape orientation
			strXML += "\n<KeyTable Orientation=\"Landscape\" NumberOfColumns=\"" + mnNumofColumnsLand + "\">\n";
			for (int index = 0; index < mlistInputKeysLand.size(); index ++)	{
				strXML += mlistInputKeysLand.get(index).convertToXMLString();
			}
			strXML += "\n</KeyTable>";
			strXML += "</InputPad>\n";
			return strXML;
		}
	}
	
	public static LinkedList<InputPad> readInputPadsFromXML(InputStream is)	{
        Document doc;
        LinkedList<InputPad> listOfPads = new LinkedList<InputPad>();
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
	            if(inputPadNode.getNodeType() == Node.ELEMENT_NODE){
	            	NamedNodeMap namedNodeMap = inputPadNode.getAttributes();
	            	Node nodeAttrType = namedNodeMap.getNamedItem("Type");
	            	if (nodeAttrType != null && nodeAttrType.getNodeValue().equals("TableInputPad"))	{
	            		try	{
		            		//This is what we want
		            		TableInputPad tableInputPad = new TableInputPad();
		            		Node nodeAttr = namedNodeMap.getNamedItem("Name");
		            		tableInputPad.mstrName = nodeAttr.getNodeValue();
		            		nodeAttr = namedNodeMap.getNamedItem("LongName");
		            		tableInputPad.mstrLongName = nodeAttr.getNodeValue();
		            		nodeAttr = namedNodeMap.getNamedItem("WrappedNameLine1");
		            		tableInputPad.mstrWrappedName1 = nodeAttr.getNodeValue();
		            		nodeAttr = namedNodeMap.getNamedItem("WrappedNameLine2");
		            		tableInputPad.mstrWrappedName2 = nodeAttr.getNodeValue();
		               		nodeAttr = namedNodeMap.getNamedItem("ShortName");
		            		tableInputPad.mstrShortName = nodeAttr.getNodeValue();
		            		nodeAttr = namedNodeMap.getNamedItem("Visible");
	            			tableInputPad.mbVisible = Boolean.parseBoolean(nodeAttr.getNodeValue());
	            			NodeList listOfKeyTables = inputPadNode.getChildNodes();
	            			for (int idxKeyTbl = 0; idxKeyTbl < listOfKeyTables.getLength(); idxKeyTbl ++)	{
	            				if (listOfKeyTables.item(idxKeyTbl).getNodeType() == Node.ELEMENT_NODE &&
	            						listOfKeyTables.item(idxKeyTbl).getNodeName().equals("KeyTable"))	{
	            					Node nodeKeyTable = listOfKeyTables.item(idxKeyTbl);
	            					LinkedList<InputKey> listInputKeys = null;
	            					NamedNodeMap namedNodeMapKeyTable = nodeKeyTable.getAttributes();
	            					nodeAttr = namedNodeMapKeyTable.getNamedItem("Orientation");
	            					if (nodeAttr != null && nodeAttr.getNodeValue().equalsIgnoreCase("Landscape"))	{
	        		               		nodeAttr = namedNodeMapKeyTable.getNamedItem("NumberOfColumns");
	        		               		tableInputPad.mnNumofColumnsLand = Integer.parseInt(nodeAttr.getNodeValue());
	        		            		if (tableInputPad.mnNumofColumnsLand <= 0)	{
	        		            			continue;	// something wrong here, ignore this key table.
	        		            		}
	        		            		listInputKeys = tableInputPad.mlistInputKeysLand;
	            					} else {
	        		               		nodeAttr = namedNodeMapKeyTable.getNamedItem("NumberOfColumns");
	        		               		tableInputPad.mnNumofColumns = Integer.parseInt(nodeAttr.getNodeValue());
	        		            		if (tableInputPad.mnNumofColumns <= 0)	{
	        		            			continue;	// something wrong here, ignore this key table.
	        		            		}
	        		            		listInputKeys = tableInputPad.mlistInputKeys;
	            					}
	    	            			NodeList listOfInputKeys = nodeKeyTable.getChildNodes();
	    	            			int indexKey = 0;
	    	            			while (indexKey < listOfInputKeys.getLength())	{
	    	            				if (listOfInputKeys.item(indexKey).getNodeType() == Node.ELEMENT_NODE &&
	    		            					listOfInputKeys.item(indexKey).getNodeName().equals("InputKey"))	{
	    		            				InputKey inputKey = new InputKey();
	    		            				Node nodeInputKey = listOfInputKeys.item(indexKey);
	    		            				NamedNodeMap namedNodeMapInputKey = nodeInputKey.getAttributes();
	    		            				nodeAttr = namedNodeMapInputKey.getNamedItem("KeyInput");
	    		            				inputKey.mstrKeyInput = nodeAttr.getNodeValue();
	    		            				nodeAttr = namedNodeMapInputKey.getNamedItem("KeyShown");
	    		            				inputKey.mstrKeyShown = nodeAttr.getNodeValue();
	    	                   				listInputKeys.addLast(inputKey);
	    	            				}
	    	            				indexKey ++;
	    		            		}
	            				}
	            			}
		            		listOfPads.add(tableInputPad);
	            		} catch (Exception e)	{
	            			continue;	//something wrong happens when read this table input pad.
	            		}
	            	}
	            }
	        }
		} catch (Exception e)	{
		}
		
		return listOfPads;
	}
	
	public static LinkedList<InputPad> filterOffInvisibleInputPads(LinkedList<InputPad> listInputPads)	{
		LinkedList<InputPad> listShownInputPads = new LinkedList<InputPad>();
		if (listInputPads == null)	{
			return listShownInputPads;
		}
		for (int idx = 0; idx < listInputPads.size(); idx ++)	{
			InputPad inputPad = listInputPads.get(idx);
			if (inputPad.mbVisible)	{
				listShownInputPads.add(inputPad);
			}
		}
		return listShownInputPads;
	}
	
	public static String writeInputPadsToXML(LinkedList<InputPad> listInputPads)	{
		String str = "<InputPads>";
		for (int index = 0; index < listInputPads.size(); index ++)	{
			str += listInputPads.get(index).convertToXMLString();
		}
		str += "</InputPads>";
		return str;
	}

	// merge listInputPads1 into listInputPads2
	public static void mergeInputPadCfgs(LinkedList<InputPad> listInputPads1,
														LinkedList<InputPad> listInputPads2)	{
		if (listInputPads1 == null || listInputPads1.size() == 0)	{
			return;
		}
		if (listInputPads2 == null || listInputPads2.size() == 0)	{
			InputPad inputPad = new TableInputPad();
			inputPad.mbVisible = true;
			inputPad.mstrLongName = "others";
			inputPad.mstrName = "others";
			inputPad.mstrWrappedName1 = "others";
			inputPad.mstrWrappedName2 = "...";
			inputPad.mstrShortName = "more..";
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
			InputPad inputPad = new TableInputPad();
			inputPad.mbVisible = true;
			inputPad.mstrLongName = "others";
			inputPad.mstrName = "others";
			inputPad.mstrWrappedName1 = "others";
			inputPad.mstrWrappedName2 = "...";
			inputPad.mstrShortName = "more..";
			listInputPads2.add(inputPad);
			inputPadOthers = (TableInputPad) inputPad;
		}
		for (int nLandOrNot = 0; nLandOrNot < 2; nLandOrNot ++)	{
			for (int idx = 0; idx < listInputPads1.size(); idx ++)	{
				if (listInputPads1.get(idx) instanceof TableInputPad)	{
					LinkedList<InputKey> listKeys = ((TableInputPad)listInputPads1.get(idx)).mlistInputKeys;
					if (nLandOrNot == 1)	{
						listKeys = ((TableInputPad)listInputPads1.get(idx)).mlistInputKeysLand;
					}
					for (int idx1 = 0; idx1 < listKeys.size(); idx1++)	{
						boolean bFound = false;
						for (int idx2 = 0; idx2 < listInputPads2.size(); idx2 ++)	{
							if (listInputPads2.get(idx2) instanceof TableInputPad)	{
								for (int idx3 = 0; idx3 < ((TableInputPad)listInputPads2.get(idx2)).mlistInputKeys.size(); idx3++)	{
									if (listKeys.get(idx1).mstrKeyInput.equalsIgnoreCase(
											((TableInputPad)listInputPads2.get(idx2)).mlistInputKeys.get(idx3).mstrKeyInput))	{
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
								for (int idx3 = 0; idx3 < ((TableInputPad)listInputPads2.get(idx2)).mlistInputKeysLand.size(); idx3++)	{
									if (listKeys.get(idx1).mstrKeyInput.equalsIgnoreCase(
											((TableInputPad)listInputPads2.get(idx2)).mlistInputKeysLand.get(idx3).mstrKeyInput))	{
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
							InputKey inputKey = new InputKey();
							inputKey.copy(listKeys.get(idx1));
							if (nLandOrNot == 0)	{
								inputPadOthers.mlistInputKeys.add(inputKey);
							} else	{
								inputPadOthers.mlistInputKeysLand.add(inputKey);
							}
						}
					}
				}
			}
		}
	}
}
