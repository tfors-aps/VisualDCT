package com.cosylab.vdct.vdb;

import java.util.*;
import com.cosylab.vdct.Constants;
import com.cosylab.vdct.dbd.DBDConstants;
import com.cosylab.vdct.graphics.objects.Group;

/**
 * Handles PV_LINK properties
 * Creation date: (1.2.2001 22:24:28)
 * @author: Matej Sekoranja
 */
 
public final class LinkProperties {
	private static final String tokenizerSettings	= " .\t";
	
	private static final String nullString 		= "";
	private static final String spaceString 	= " ";
	
	private static final String defaultVarName	= "VAL";		// defaults
	private static final String defaultProcess	= "NPP";			
	private static final String defaultMaximize	= "NMS";

	public final static int NOT_VALID = -1;
	public final static int INLINK_FIELD = 0;
	public final static int OUTLINK_FIELD = 1;
	public final static int FWDLINK_FIELD = 2;
	public final static int VARIABLE_FIELD = 3;

	private int type;
	private String varName;
	private String process;
	private String maximize;
	private String record;
	private boolean isInterGroupLink;
	
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:53:33)
 */
public LinkProperties(VDBFieldData fd) {
	setDefaults();
	setProperties(fd);
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @return java.lang.String
 */
public java.lang.String getMaximize() {
	return maximize;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 14:01:58)
 * @return java.lang.String
 */
public String getOptions() {
	return process+spaceString+maximize;
}
/**
 * This method was created in VisualAge.
 * @return java.lang.String
 * @param fd com.cosylab.vdct.vdb.VDBFieldData
 */
public static String getOptions(VDBFieldData fd) {

	if ((fd.getValue()==null) || fd.getValue().equals(nullString)) return null;

	String options;
	String value = fd.getValue();

	StringTokenizer tokenizer = new StringTokenizer(value, tokenizerSettings);

	String process = defaultProcess;
	String maximize = defaultMaximize;
	
	if (tokenizer.hasMoreTokens()) tokenizer.nextToken();		// read record name 
	if (value.indexOf(Constants.FIELD_SEPARATOR) > -1) 
		if (tokenizer.hasMoreTokens()) tokenizer.nextToken(); // read var variable

	if (tokenizer.hasMoreTokens()) process=tokenizer.nextToken(); // read var variable process
	if (tokenizer.hasMoreTokens()) maximize=tokenizer.nextToken(); // read var variable maxmimize
	else {
		// checks if process variable is actually maximize variable?!
		if (process.equalsIgnoreCase("NMS") || 
			process.equalsIgnoreCase("MS")) {
			maximize=process;
			process=defaultProcess;
		}
	}

	return process+spaceString+maximize;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @return java.lang.String
 */
public java.lang.String getProcess() {
	return process;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @return java.lang.String
 */
public java.lang.String getRecord() {
	return record;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:43:15)
 * @return java.lang.String
 * @param fd com.cosylab.vdct.vdb.VDBFieldData
 */
public static String getTarget(VDBFieldData fd) {
	
	if ((fd.getValue()==null) || fd.getValue().equals(nullString)) return null;

	String target = nullString;
	String value = fd.getValue();

	StringTokenizer tokenizer = new StringTokenizer(value, tokenizerSettings);

	if (tokenizer.hasMoreTokens()) target=tokenizer.nextToken();		// read record name 
	if (value.indexOf(Constants.FIELD_SEPARATOR) > -1) {
		if (tokenizer.hasMoreTokens()) {
			String var = tokenizer.nextToken(); 				// read var variable
			if ((LinkProperties.getType(fd)==LinkProperties.FWDLINK_FIELD) &&		// !!! proc
				!var.equalsIgnoreCase("PROC"))
				return nullString;
			else
				target+=Constants.FIELD_SEPARATOR+var;
		}
	}

	return target;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:43:15)
 * @return java.lang.String
 * @param value java.lang.String
 */
public static String getTargetFromString(String value) {
	
	if ((value==null) || value.equals(nullString)) return null;

	String target = nullString;

	StringTokenizer tokenizer = new StringTokenizer(value, tokenizerSettings);

	if (tokenizer.hasMoreTokens()) target=tokenizer.nextToken();		// read record name 
	if (value.indexOf(Constants.FIELD_SEPARATOR) > -1) {
		if (tokenizer.hasMoreTokens()) {
			String var = tokenizer.nextToken(); 				// read var variable
				target+=Constants.FIELD_SEPARATOR+var;
		}
	}

	return target;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @return int
 */
public int getType() {
	return type;
}
/**
 * Insert the method's description here.
 * Creation date: (29.1.2001 22:55:09)
 * @return int
 * @param fd com.cosylab.vdct.vdb.VDBFieldData
 */
public static int getType(VDBFieldData fd) {
	switch (fd.getType()) {
		case DBDConstants.DBF_INLINK : return INLINK_FIELD;
		case DBDConstants.DBF_OUTLINK : return OUTLINK_FIELD;
		case DBDConstants.DBF_FWDLINK : return FWDLINK_FIELD;
		default: return VARIABLE_FIELD;
	}
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @return java.lang.String
 */
public java.lang.String getVarName() {
	return varName;
}
/**
 * Insert the method's description here.
 * Creation date: (31.1.2001 20:36:39)
 * @return boolean
 */
public boolean isIsInterGroupLink() {
	return isInterGroupLink;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 10:02:52)
 */
private void setDefaults() {
	setType(NOT_VALID);
	setRecord(nullString);
	setProcess(defaultProcess);
	setMaximize(defaultMaximize);
	setVarName(defaultVarName);
	setIsInterGroupLink(false);
}
/**
 * Insert the method's description here.
 * Creation date: (31.1.2001 20:36:39)
 * @param newIsInterGroupLink boolean
 */
public void setIsInterGroupLink(boolean newIsInterGroupLink) {
	isInterGroupLink = newIsInterGroupLink;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @param newMaximize java.lang.String
 */
public void setMaximize(java.lang.String newMaximize) {
	maximize = newMaximize;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @param newProcess java.lang.String
 */
public void setProcess(java.lang.String newProcess) {
	process = newProcess;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:54:05)
 * @param fd com.cosylab.vdct.vdb.VDBFieldData
 */
private void setProperties(VDBFieldData fd) {

	if ((fd.getValue()==null) || fd.getValue().equals(nullString)) {
		setType(NOT_VALID);
		setRecord(null);
		return;
	}
	
	setType(getType(fd));

	String value = fd.getValue();

	StringTokenizer tokenizer = new StringTokenizer(value, tokenizerSettings);

	if (tokenizer.hasMoreTokens()) setRecord(tokenizer.nextToken());	// read record name 
	if (value.indexOf(Constants.FIELD_SEPARATOR) > -1) {
		if (tokenizer.hasMoreTokens()) {
			setVarName(tokenizer.nextToken()); // read var variable
			if ((getType()==FWDLINK_FIELD) &&
				getVarName().equalsIgnoreCase("PROC")) {		// !!! proc
				//setType(NOT_VALID); ?!!
				//return;
			}
		}
	}

	if (tokenizer.hasMoreTokens()) setProcess(tokenizer.nextToken()); // read var variable process
	if (tokenizer.hasMoreTokens()) setMaximize(tokenizer.nextToken()); // read var variable maxmimize
	else {
		// checks if process variable is actually maximize variable?!
		if (getProcess().equalsIgnoreCase("NMS") || 
			getProcess().equalsIgnoreCase("MS")) {
			setMaximize(getProcess());
			setProcess(defaultProcess);
		}
	}

	if (Group.substractParentName(fd.getRecord().getName()).equals(Group.substractParentName(getRecord())))
		setIsInterGroupLink(false);
	else
		setIsInterGroupLink(true);
	
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @param newRecord java.lang.String
 */
public void setRecord(java.lang.String newRecord) {
	record = newRecord;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @param newType int
 */
public void setType(int newType) {
	type = newType;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 9:56:35)
 * @param newVarName java.lang.String
 */
public void setVarName(java.lang.String newVarName) {
	varName = newVarName;
}
/**
 * Insert the method's description here.
 * Creation date: (30.1.2001 12:38:32)
 */
public void update(VDBFieldData fd) {
	setProperties(fd);
}
}